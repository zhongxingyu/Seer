 package edu.berkeley.gamesman.database;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.Scanner;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.ErrorThread;
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Util;
 import edu.berkeley.gamesman.util.ZipChunkInputStream;
 
 /**
  * A connection to an already solved database on another computer (via ssh and
  * dd). This can be thought of sort of like a DatabaseWrapper. The URL for
  * RemoteDatabases is username@server:GamesmanJavaPath:DatabasePath (The
  * username@ is optional)
  * 
  * @author dnspies
  */
 public class RemoteDatabase extends Database {
 	private final String user, server, confFile, path, remoteFile;
 	private final boolean readZipped;
 	private int maxCommandLen = -1;
 
 	/**
 	 * The default constructor
 	 * 
 	 * @param uri
 	 *            The name of the file on this machine (if any)
 	 * @param conf
 	 *            The configuration object
 	 * @param solve
 	 *            Should always be false
 	 * @param firstRecord
 	 *            The index of the first record contained in this database
 	 * @param numRecords
 	 *            The number of records contained in this database
 	 * @param header
 	 *            The header
 	 */
 	public RemoteDatabase(String uri, Configuration conf, boolean solve,
 			long firstRecord, long numRecords, DatabaseHeader header) {
 		this(uri, conf, solve, firstRecord, numRecords, header, null, null,
 				null, null);
 	}
 
 	/**
 	 * @param uri
 	 *            The name of the file on this machine (if any)
 	 * @param conf
 	 *            The configuration object
 	 * @param solve
 	 *            Should always be false
 	 * @param firstRecord
 	 *            The index of the first record contained in this database
 	 * @param numRecords
 	 *            The number of records contained in this database
 	 * @param header
 	 *            The header
 	 * @param user
 	 *            The user name
 	 * @param server
 	 *            The server name
 	 * @param path
 	 *            The GamesmanJava path
 	 * @param remoteFile
 	 *            The file uri on the remote host
 	 */
 	public RemoteDatabase(String uri, Configuration conf, boolean solve,
 			long firstRecord, long numRecords, DatabaseHeader header,
 			String user, String server, String path, String remoteFile) {
 		super(uri, conf, solve, firstRecord, numRecords, header);
 		if (user == null)
 			user = conf.getProperty("gamesman.remote.user", null);
 		this.user = user;
 		if (server == null)
 			server = conf.getProperty("gamesman.remote.server");
 		this.server = server;
 		if (path == null)
 			path = conf.getProperty("gamesman.remote.path");
 		this.path = path;
 		if (remoteFile == null)
 			remoteFile = conf.getProperty("gamesman.remote.db.uri");
 		if (!remoteFile.startsWith("/") && !remoteFile.startsWith(path))
 			remoteFile = path + "/" + remoteFile;
 		readZipped = conf.getBoolean("gamesman.remote.zipped", false);
 		this.remoteFile = remoteFile;
 		String confFile = conf.getProperty("gamesman.remote.job", null);
 		if (confFile != null && !confFile.startsWith("/")
 				&& !confFile.startsWith(path))
 			confFile = path + "/" + confFile;
 		this.confFile = confFile;
 	}
 
 	@Override
 	public void close() {
 	}
 
 	@Override
 	protected void getBytes(DatabaseHandle dh, long loc, byte[] arr, int off,
 			int numBytes) {
 		getRecordsAsBytes(dh, loc, 0, arr, off, numBytes, 0, true);
 	}
 
 	@Override
 	protected void prepareRange(DatabaseHandle dh, long byteIndex,
 			int firstNum, long numBytes, int lastNum) {
 		super.prepareRange(dh, byteIndex, firstNum, numBytes, lastNum);
 		StringBuilder command;
 		if (maxCommandLen >= 0)
 			command = new StringBuilder(maxCommandLen);
 		else
 			command = new StringBuilder();
 		command.append("ssh -q ");
 		if (user != null) {
 			command.append(user);
 			command.append("@");
 		}
 		command.append(server);
 		command.append(" java -cp ");
 		command.append(path);
 		command.append("/bin ");
 		if (readZipped)
 			command.append(ReadZippedRecords.class.getName());
 		else
 			command.append(ReadRecords.class.getName());
 		command.append(" ");
 		if (confFile != null) {
 			command.append(confFile);
 			command.append(" ");
 		}
 		command.append(remoteFile);
 		command.append(" ");
 		long firstRecord = toFirstRecord(byteIndex);
 		long lastRecord = toLastRecord(byteIndex + numBytes);
 		firstRecord += firstNum;
 		if (lastNum > 0)
 			lastRecord -= recordsPerGroup - lastNum;
 		long numRecords = lastRecord - firstRecord;
 		command.append(firstRecord);
 		command.append(" ");
 		command.append(numRecords);
 		if (maxCommandLen < command.length())
 			maxCommandLen = command.length();
 		try {
 			Process p = Runtime.getRuntime().exec(command.toString());
 			new ErrorThread(p.getErrorStream(), server).start();
 			RemoteHandle rh = ((RemoteHandle) dh);
 			rh.is = p.getInputStream();
 			if (readZipped) {
 				int skipNum = 0;
 				for (int i = 0; i < 4; i++) {
 					skipNum <<= 8;
 					skipNum |= rh.is.read() & 255;
 				}
 				rh.is = new ZipChunkInputStream(rh.is);
 				Database.skipFully(rh.is, skipNum);
 			}
 		} catch (IOException e) {
			throw new Error(e);
 		}
 	}
 
 	@Override
 	protected int getBytes(DatabaseHandle dh, byte[] arr, int off, int maxLen,
 			boolean overwriteEdgesOk) {
 		if (!overwriteEdgesOk) {
 			return super.getBytes(dh, arr, off, maxLen, false);
 		} else {
 			final int numBytes = (int) Math.min(maxLen, dh.lastByteIndex
 					- dh.location);
 			try {
 				readFully(((RemoteHandle) dh).is, arr, off, numBytes);
 				dh.location += numBytes;
 				if (dh.location == dh.lastByteIndex)
 					((RemoteHandle) dh).is.close();
 			} catch (IOException e) {
 				throw new Error(e);
 			}
 			return numBytes;
 		}
 	}
 
 	@Override
 	public RemoteHandle getHandle() {
 		return new RemoteHandle(recordGroupByteLength);
 	}
 
 	@Override
 	protected void putBytes(DatabaseHandle dh, long loc, byte[] arr, int off,
 			int len) {
 		throw new UnsupportedOperationException();
 	}
 
 	// public static void main(String[] args) throws IOException {
 	// byte[] headerBytes = new byte[18];
 	// FileInputStream fis = new FileInputStream(args[0]);
 	// readFully(fis, headerBytes, 0, 18);
 	// System.out.write(headerBytes);
 	// if (args.length > 1 && Boolean.parseBoolean(args[1])) {
 	// byte[] confBytes = Configuration.loadBytes(fis);
 	// System.out.write(confBytes);
 	// }
 	// fis.close();
 	// System.out.flush();
 	// }
 
 	private static Pair<DatabaseHeader, Configuration> remoteHeaderConf(
 			String user, String host, String file, boolean withConf) {
 		try {
 			int numBytes = withConf ? 22 : 18;
 			String commandString = "ssh -q "
 					+ (user == null ? host : (user + "@" + host)) + " dd if="
 					+ file + " count=";
 			byte[] headerBytes = new byte[numBytes];
 			String command = commandString + 1;
 			assert Util.debug(DebugFacility.DATABASE, command);
 			Process p = Runtime.getRuntime().exec(command);
 			new ErrorThread(p.getErrorStream(), host).start();
 			InputStream is = p.getInputStream();
 			Database.readFully(is, headerBytes, 0, numBytes);
 			is.close();
 			DatabaseHeader dh = new DatabaseHeader(headerBytes);
 			if (withConf) {
 				int confLength = 0;
 				for (int i = 18; i < 22; i++) {
 					confLength <<= 8;
 					confLength |= headerBytes[i] & 255;
 				}
 				command = commandString + ((numBytes + confLength + 511) >> 9);
 				assert Util.debug(DebugFacility.DATABASE, command);
 				p = Runtime.getRuntime().exec(command);
 				new ErrorThread(p.getErrorStream(), host).start();
 				is = p.getInputStream();
 				Database.readFully(is, headerBytes, 0, numBytes);
 				byte[] confBytes = new byte[confLength];
 				Database.readFully(is, confBytes, 0, confLength);
 				is.close();
 				ByteArrayInputStream bais = new ByteArrayInputStream(confBytes);
 				Properties props = new Properties();
 				props.load(bais);
 				Configuration conf = new Configuration(props);
 				return new Pair<DatabaseHeader, Configuration>(dh, conf);
 			} else
 				return new Pair<DatabaseHeader, Configuration>(dh, null);
 		} catch (IOException e) {
 			throw new Error(e);
 		} catch (ClassNotFoundException e) {
 			throw new Error(e);
 		}
 	}
 
 	protected static Pair<DatabaseHeader, Configuration> remoteHeaderConf(
 			String user, String host, String file) {
 		return remoteHeaderConf(user, host, file, true);
 	}
 
 	protected static DatabaseHeader remoteHeader(String user, String host,
 			String file) {
 		return remoteHeaderConf(user, host, file, false).car;
 	}
 
 	/**
 	 * Creates a local file for connecting to a remote database
 	 * 
 	 * @param args
 	 *            The full remote uri of the database
 	 *            username@server:GamesmanJavaPath:DatabasePath and the name of
 	 *            the local file
 	 * @throws IOException
 	 *             If there's an exception creating the local file
 	 */
 	public static void main(String[] args) throws IOException {
 		RemoteDatabase rd = (RemoteDatabase) Database.openDatabase(args[0]);
 		rd.addRemoteProperties();
 		FileOutputStream fos = new FileOutputStream(args[1]);
 		rd.store(fos, args[1]);
 		fos.close();
 		rd.close();
 	}
 
 	private void addRemoteProperties() {
 		if (user != null)
 			conf.setProperty("gamesman.remote.user", user);
 		conf.setProperty("gamesman.remote.server", server);
 		conf.setProperty("gamesman.remote.path", path);
 		conf.setProperty("gamesman.remote.db.uri", remoteFile);
 	}
 
 	@Override
 	public long getSize() {
 		StringBuilder command;
 		if (maxCommandLen >= 0)
 			command = new StringBuilder();
 		else
 			command = new StringBuilder();
 		command.append("ssh -q ");
 		if (user != null) {
 			command.append(user);
 			command.append("@");
 		}
 		command.append(server);
 		command.append(" java -cp ");
 		command.append(path);
 		command.append("/bin ");
 		command.append(ReadLength.class.getName());
 		command.append(" ");
 		command.append(remoteFile);
 		try {
 			Process p = Runtime.getRuntime().exec(command.toString());
 			new ErrorThread(p.getErrorStream(), server).start();
 			InputStream is = p.getInputStream();
 			Scanner scan = new Scanner(is);
 			return scan.nextLong();
 		} catch (IOException e) {
 			throw new Error(e);
 		}
 	}
 }
 
 class RemoteHandle extends DatabaseHandle {
 	InputStream is;
 
 	public RemoteHandle(int recordGroupByteLength) {
 		super(recordGroupByteLength);
 	}
 }
