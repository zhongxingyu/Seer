 package xtremweb.serv.dt.ftp;
 
 /**
  * Ftpsender.java
  *
  *
  *
  *
  * @author <a href="mailto:fedak@lri.fr">Gilles Fedak</a>
  * @version 1.0
  */
 
 import org.apache.commons.net.ftp.*;
 import xtremweb.core.log.*;
 import xtremweb.core.uid.*;
 import xtremweb.serv.dt.*;
 import xtremweb.core.obj.dr.Protocol;
 import xtremweb.core.obj.dt.Transfer;
 import xtremweb.core.obj.dc.Data;
 import xtremweb.core.obj.dc.Locator;
 import xtremweb.api.transman.*;
 import java.io.*;
 
 public class FtpTransfer extends BlockingOOBTransferImpl implements
 		BlockingOOBTransfer, OOBTransfer {
 
 	protected FTPClient ftp;
 
 	protected static Logger log = LoggerFactory.getLogger(FtpTransfer.class);
 
 	public FtpTransfer(Data d, Transfer t, Locator rl, Locator ll, Protocol rp,
 			Protocol lp) {
 		super(d, t, rl, ll, rp, lp);
 		transfer.setoob(this.getClass().toString());
 	} // Ftpsender constructor
 
 	public String ftptoString() {
 		return "ftp://[" + remote_protocol.getlogin() + "@"
 				+ remote_protocol.getserver() + ":" + remote_protocol.getport();
 
 	}
 
 	public void connect() throws OOBException {
 		log.debug("connect " + ftptoString());
 		ftp = new FTPClient();
 
 		try {
 			int reply;
 			ftp.connect(remote_protocol.getserver(), remote_protocol.getport());
 			log.debug(ftp.getReplyString());
 
 			// After connection attempt, you should check the reply code to
 			// verify
 			// success.
 			reply = ftp.getReplyCode();
 
 			if (!FTPReply.isPositiveCompletion(reply)) {
 				ftp.disconnect();
 				log.debug("FTP server refused connection : " + ftptoString());
 				throw new OOBException("Server refused connection "
 						+ ftptoString());
 
 			}
 
 			// login as anonymous
 			if (!ftp.login(remote_protocol.getlogin(),
 					remote_protocol.getpassword())) {
 				log.debug("FTP server wrong login " + ftptoString());
 				throw new OOBException("Wrong login " + ftptoString());
 			} else
 
 				log.debug("Succesfully logged into " + ftptoString());
 
 			// FIXME, make this configurable
 			ftp.enterLocalPassiveMode();
 		} catch (Exception e) {
 			log.debug("" + e);
 			throw new OOBException("FTP Cannot open ftp session "
 					+ ftptoString());
 		}
 	}
 
 	public void blockingSendSenderSide() throws OOBException {
 		try {
 			if (remote_protocol.getpath() != null)
 				ftp.changeWorkingDirectory(remote_protocol.getpath());
 
 			FileInputStream is = new FileInputStream(new File(
 					local_locator.getref()));
 			ftp.setFileType(FTP.BINARY_FILE_TYPE);
 			if (!ftp.storeFile(remote_locator.getref(), is)) {
				log.debug("Upload Error");
 			} else {
 				log.debug("Upload Success");
 			} // end of else
 		} catch (Exception e) {
 			log.debug("Error" + e);
 			throw new OOBException("FTP errors when sending  " + ftptoString()
 					+ "/" + remote_locator.getref());
 		} // end of try-catch
 	}
 
 	public void blockingSendReceiverSide() throws OOBException {
 	}
 
 	public void blockingReceiveReceiverSide() throws OOBException {
 		log.debug("start receive receiver size");
 		try {
 			if (remote_protocol.getpath() != null)
 				ftp.changeWorkingDirectory(remote_protocol.getpath());
 			log.debug("changed directory to " + remote_protocol.getpath());
 			FileOutputStream os = new FileOutputStream(new File(
 					local_locator.getref()));
 
 			ftp.setFileType(FTP.BINARY_FILE_TYPE);
 			log.debug("going to get " + remote_locator.getref() + "to "
 					+ local_locator.getref());
 			if (!ftp.retrieveFile(remote_locator.getref(), os)) {
 				log.debug("Download Error : " + ftp.getReplyString());
 				error = true;
 			} else {
 				log.debug("Download Success");
 			} // end of else
 		} catch (Exception e) {
 			log.debug("Error" + e);
 			throw new OOBException("FTP errors when receiving receive "
 					+ ftptoString() + "/" + remote_locator.getref());
 		} // end of try-catch
 
 		log.debug("FIN du transfer");
 	}
 
 	public void blockingReceiveSenderSide() throws OOBException {
 	}
 
 	public void disconnect() throws OOBException {
 		if (ftp != null) {
 			if (ftp.isConnected()) {
 				try {
 					ftp.logout();
 					ftp.disconnect();
 				} catch (IOException ioe) {
 					System.out.println("Error" + ioe);
 				}
 			}
 		}
 	}
 
 	public static void main(String[] args) {
 		// IT4S BROKEN
 		Data data = new Data();
 
 		// Preparer le local
 		Protocol local_proto = new Protocol();
 		local_proto.setname("local");
 
 		Locator local_locator = new Locator();
 		local_locator.setdatauid(data.getuid());
 		local_locator.setdrname("localhost");
 		local_locator.setprotocoluid(local_proto.getuid());
 		local_locator.setref("/tmp/localcopy");
 
 		// Preparer le proto pour l'acces remote
 		Protocol remote_proto = new Protocol();
 		remote_proto.setserver("localhost");
 		remote_proto.setname("ftp");
 		remote_proto.setpath("pub/incoming");
 		remote_proto.setport(21);
 		remote_proto.setlogin("anonymous");
 		remote_proto.setpassword("fedak@lri.fr");
 
 		Locator remote_locator = new Locator();
 		remote_locator.setdatauid(data.getuid());
 		remote_locator.setdrname("localhost");
 		remote_locator.setprotocoluid(remote_proto.getuid());
 		remote_locator.setref("binaryFile");
 
 		// prepar
 		Transfer t = new Transfer();
 		t.setlocatorremote(remote_locator.getuid());
 		t.setlocatorlocal(local_locator.getuid());
 		t.settype(TransferType.UNICAST_RECEIVE_RECEIVER_SIDE);
 		// Data data = DataUtil.fileToData(file);
 
 		FtpTransfer ftp = new FtpTransfer(data, t, remote_locator,
 				local_locator, remote_proto, local_proto);
 
 		System.out.println(ftp.ftptoString());
 
 		try {
 			ftp.connect();
 			ftp.receiveReceiverSide();
 			ftp.disconnect();
 		} catch (OOBException oobe) {
 			System.out.println(oobe);
 		}
 
 		remote_locator.setref("copy_test.ps");
 		remote_proto.setpath("pub/incoming");
 		t.settype(TransferType.UNICAST_SEND_SENDER_SIDE);
 		ftp = new FtpTransfer(data, t, remote_locator, local_locator,
 				remote_proto, local_proto);
 		System.out.println(ftp.ftptoString());
 		try {
 			ftp.connect();
 			ftp.sendSenderSide();
 			ftp.disconnect();
 		} catch (OOBException oobe) {
 			System.out.println(oobe);
 		}
 
 	}
 
 	public boolean poolTransfer() {
 		return !isTransfering();
 	}
 
 } // Ftpsender
