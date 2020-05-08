 package de.tr0llhoehle.buschtrommel.network;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 
 import de.tr0llhoehle.buschtrommel.HashFuncWrapper;
 import de.tr0llhoehle.buschtrommel.LoggerWrapper;
 import de.tr0llhoehle.buschtrommel.models.FileRequestResponseMessage;
 import de.tr0llhoehle.buschtrommel.models.GetFileMessage;
 import de.tr0llhoehle.buschtrommel.models.Host;
 import de.tr0llhoehle.buschtrommel.models.Message;
 
 /**
  * This class represents a download from a host to this client (single source).
  * It runs in an independend thread, restarts the process if neccesaray and
  * creates all local resources (such as target file and a new socket).
  * 
  * @author tobi
  * 
  */
 public class IncomingDownload extends MessageMonitor implements ITransferProgress, Runnable {
 
 	private String hash;
 	private long offset, totalTransferedVolume;
 	private TransferStatus status;
 	private boolean keepAlive;
 	private long expectedTransferVolume;
 	Host host;
 	boolean checkIntegrity;
 	File targetFile;
 	GetFileMessage sourceFile;
 	FileOutputStream targetFilestream;
 	java.net.Socket socket;
 	Thread self;
 
 	public IncomingDownload(GetFileMessage sourceFile, Host host, java.io.File target) {
 		this.offset = sourceFile.getOffset();
 		this.sourceFile = sourceFile;
 		this.hash = sourceFile.getHash();
 		this.host = host;
 		sourceFile.getLength();
 		expectedTransferVolume = sourceFile.getLength();
 		this.totalTransferedVolume = 0;
 		checkIntegrity = true;
 		this.targetFile = target;
 		status = TransferStatus.Initialized;
 	}
 
 	public void DisableIntegrityCheck() {
 		hash = "";
 	}
 
 	@Override
 	public void run() {
 		try {
 			if(status == TransferStatus.AssembleParts || status == TransferStatus.CheckingHash || status == TransferStatus.Connecting || status == TransferStatus.Finished || status == TransferStatus.Transfering)
 				LoggerWrapper.logError("Can't start download, state is " + status);
 			else
 				doTransfer();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		self = null;
 	}
 
 	protected void doTransfer() throws UnsupportedEncodingException {
 		LoggerWrapper.logInfo("Starting download");
 
 		if (status == TransferStatus.Initialized) {
 			try {
 				LoggerWrapper.logInfo("open stream to '" + targetFile.getPath() + "'");
 				targetFilestream = new FileOutputStream(targetFile, false);
 			} catch (FileNotFoundException e) {
 				LoggerWrapper.logError("Could not create target filestream: " + e.getMessage());
 				status = TransferStatus.LocalIOError;
 				targetFilestream = null;
 				return;
 			}
 		}
 
 		// connect and send request
 		status = TransferStatus.Connecting;
 		LoggerWrapper.logInfo("Connecting to " + host.getAddress() + ":" + host.getPort());
 		expectedTransferVolume = sourceFile.getLength() - totalTransferedVolume;
 		assert socket == null;
 		InputStream in = null;
 		OutputStream out = null;
 		try {
 			socket = new Socket();
 			socket.connect(new InetSocketAddress(host.getAddress(), host.getPort()));
 
 			out = socket.getOutputStream();
 			in = socket.getInputStream();
 			out.write(new GetFileMessage(sourceFile.getHash(), offset + totalTransferedVolume, sourceFile.getLength()
 					- totalTransferedVolume).Serialize().getBytes(Message.ENCODING));
 			out.flush();
 		} catch (IOException e) {
 			LoggerWrapper.logError("could not connect to host '" + e.getMessage() + "'");
 			status = TransferStatus.ConnectionFailed;
 		}
 
 		// check response
 		FileRequestResponseMessage rsp = handleResponse(in);
 		if (rsp != null && status == TransferStatus.Connecting) {
 			rsp.setSource(host.getAddress());
 			sendMessageToObservers(rsp);
 		} else {
 			closeSocket();
 			return;
 		}
 
 		// not ready for transfer? abort
 		switch (rsp.getResponseCode()) {
 		case NEVER_TRY_AGAIN:
 			status = TransferStatus.PermenentNotAvailable;
 			return;
 		case TRY_AGAIN_LATER:
 			status = TransferStatus.TemporaryNotAvailable;
 			return;
 		default:
 		}
 
 		// check returned expected transfer volume
 		if (rsp.getExpectedVolume() > expectedTransferVolume) {
 			LoggerWrapper.logInfo("Host sent a bigger transferVolume than expected ");
 			status = TransferStatus.InvalidContent;
 			closeSocket();
 			return;
 		} else if (rsp.getExpectedVolume() < expectedTransferVolume) {
 			LoggerWrapper.logInfo("Host announced that file will be smaller than expected: " + rsp.getExpectedVolume()
 					+ " bytes");
 			expectedTransferVolume = rsp.getExpectedVolume();
 		}
 
 		// Transfer bytes
 		status = TransferStatus.Transfering;
 		int next; // TODO make buffer bigger than 1 byte
 		keepAlive = true;
 		long transferedVolume = 0;
 		try {
 			while (transferedVolume < expectedTransferVolume && (next = in.read()) != -1 && keepAlive) {
 				totalTransferedVolume++;
 				transferedVolume++;
 				try {
 					targetFilestream.write(next);
 				} catch (IOException e) {
 					LoggerWrapper.logError("Could not write into target file: " + e.getMessage());
 					status = TransferStatus.LocalIOError;
 					closeSocket();
 					closeFile(true);
 					return;
 				}
 			}
 			if (transferedVolume < expectedTransferVolume) {
 				LoggerWrapper.logError("Lost connection");
 				status = TransferStatus.LostConnection;
 				closeSocket();
 				return;
 			} else { // download completed
 				status = TransferStatus.CheckingHash;
 				LoggerWrapper.logInfo("finished data transfer");
 				closeFile(false);
 				closeSocket();
 			}
 		} catch (IOException e2) {
 			LoggerWrapper.logError("Other side closed connection");
 			status = TransferStatus.LostConnection;
 			closeSocket();
 			return;
 		}
 
 		// integrity check
 		if (status == TransferStatus.CheckingHash) {
 			integrityCheck(targetFile);
 		}
 	}
 
 	/**
 	 * Closes the socket and logs exceptions
 	 */
 	private void closeSocket() {
 		try {
 			socket.close();
 		} catch (IOException e) {
 			LoggerWrapper.logError("Could not close network socket: " + e.getMessage());
 		}
 		socket = null;
 	}
 
 	/**
 	 * Closes the target filestream, if it is not null. If there occurs an
 	 * exception, the status is set to localIOError. Optional deletes the file
 	 * (if it exists).
 	 * 
 	 * @param delete
 	 *            wether to delete the target file or not
 	 */
 	private void closeFile(boolean delete) {
 		if (targetFilestream != null) {
 			try {
 				targetFilestream.close();
 				targetFilestream = null;
 			} catch (IOException e1) {
 				LoggerWrapper.logError("Could not close target file");
 				status = TransferStatus.LocalIOError;
 				return;
 			}
 		}
 
 		if (delete && targetFile.exists()) {
 			targetFile.delete();
 		}
 	}
 
 	/**
 	 * Checks the hash of the transfered file.
 	 * 
 	 * Skips checks if the expected hash is empty. If the file can't be read for
 	 * hashing, the state will set to LocalIOError
 	 * 
 	 * @param targetPath
 	 */
 	protected void integrityCheck(File targetPath) {
 		if (getExpectedHash() == "") {
 			status = TransferStatus.Finished;
 		} else {
 			try {
 				if (HashFuncWrapper.hash(targetPath.getAbsolutePath()) == getExpectedHash()) {
 					LoggerWrapper.logInfo("File has expected hash");
 					status = TransferStatus.Finished;
 				} else {
 					LoggerWrapper.logInfo("File has invalid hash");
 					status = TransferStatus.InvalidContent;
 				}
 			} catch (IOException e) {
 				LoggerWrapper.logError("Could not check file integrity: " + e.getMessage());
 				status = TransferStatus.LocalIOError;
 			}
 		}
 	}
 
 	/**
 	 * Reads the FileRequestResponse Message from a stream.
 	 * 
 	 * Parses the message and returns it. If there is a number-exception the
 	 * response will be treated as TRY AGAIN. If the message can't be parsed, it
 	 * will be treated as TRY AGAIN.
 	 * 
 	 * May update the state, if the connection is lost.
 	 * 
 	 * @param in
 	 *            the network stream
 	 * @return FileRequestResponseMessage or null
 	 * @throws UnsupportedEncodingException
 	 *             if needed encoding is not available
 	 */
 	protected FileRequestResponseMessage handleResponse(InputStream in) throws UnsupportedEncodingException {
 		FileRequestResponseMessage try_again = new FileRequestResponseMessage(
 				FileRequestResponseMessage.ResponseCode.TRY_AGAIN_LATER, 0);
 		int probe_len = FileRequestResponseMessage.TYPE_FIELD.length() + 4;
 
 		byte[] buffer = new byte[probe_len];
 		try {
 			in.read(buffer, 0, probe_len);
 		} catch (IOException e) {
 			LoggerWrapper.logError("Could not read response stream: '" + e.getMessage()
 					+ "' - assuming 'NEVER TRY AGAIN'");
 			return try_again;
 		}
 		String responseHeader = new String(buffer, Message.ENCODING).trim();
 		switch (responseHeader.toUpperCase()) {
 		case FileRequestResponseMessage.TYPE_FIELD + Message.FIELD_SEPERATOR + "OK":
 			String str_expectedTransferVolume = "";
 			int next;
 			try {
 				while ((next = in.read()) != -1 && next != Message.MESSAGE_SPERATOR)
 					str_expectedTransferVolume += new String(new byte[] { (byte) next }, Message.ENCODING);
 			} catch (IOException e) {
 				LoggerWrapper.logError("Could not understand 'expected transfer volume' in response");
 				status = TransferStatus.LostConnection;
 				return null;
 			}
 			try {
 				long expectedTransferVolume = Long.valueOf(str_expectedTransferVolume);
 				return new FileRequestResponseMessage(FileRequestResponseMessage.ResponseCode.OK,
 						expectedTransferVolume);
 			} catch (NumberFormatException ex) {
 				LoggerWrapper
 						.logError("Response contained invalid 'expected transfer volume' - assuming 'TRY AGAIN LATER'");
 				return try_again;
 			}
 		case FileRequestResponseMessage.TYPE_FIELD + Message.FIELD_SEPERATOR + "TRY":
 			LoggerWrapper.logInfo("Received 'try again later from host'");
 			return try_again;
 		case FileRequestResponseMessage.TYPE_FIELD + Message.FIELD_SEPERATOR + "NEV":
 			LoggerWrapper.logInfo("Received 'never try agiain from host'");
 			return new FileRequestResponseMessage(FileRequestResponseMessage.ResponseCode.NEVER_TRY_AGAIN, 0);
 		default:
 			LoggerWrapper.logInfo("Received garbage: '" + responseHeader + "'");
 			return try_again;
 		}
 	}
 
 	@Override
 	public void reset() {
 		if(isActive()) {
 			LoggerWrapper.logError("Can't reset an active transfer. Cancel it before");
 			return;
 		}
 		closeSocket();
 		closeFile(targetFile.exists());
 		status = TransferStatus.Initialized;
 	}
 
 	@Override
 	public void resumeTransfer() {
 		if(isActive()) {
 			LoggerWrapper.logError("Can't resume an active transfer. Cancel it before");
 			return;
 		}
 		LoggerWrapper.logInfo("Resume transfer");
 		if (socket != null) {
 			LoggerWrapper.logError("Can't resume transfer if socket still exists");
 			return;
 		}
 
 		if (targetFilestream == null) {
 			LoggerWrapper.logError("target filestream doesn't exist anymore!");
 			status = TransferStatus.LocalIOError;
 		}
		self = new Thread(this);
		self.start();
 	}
 	
 	
 	public void start() {
 		self = new Thread(this);
 		self.start();
 	}
 
 	@Override
 	public TransferType getType() {
 		return TransferType.Singlesource;
 	}
 
 	@Override
 	public long getLength() {
 		return expectedTransferVolume;
 	}
 
 	@Override
 	public long getOffset() {
 		return offset;
 	}
 
 	@Override
 	public long getTransferedAmount() {
 		return totalTransferedVolume;
 	}
 
 	@Override
 	public String getExpectedHash() {
 		return hash;
 	}
 
 	@Override
 	public TransferStatus getStatus() {
 		return status;
 	}
 
 	@Override
 	public void cancel() {
 		keepAlive = false;
 	}
 
 	@Override
 	public List<ITransferProgress> getSubTransfers() {
 		return new ArrayList<>();
 	}
 
 	@Override
 	public boolean isActive() {
 		return self != null;
 	}
 }
