 package daemon;
 
 import java.nio.file.Path;
 import java.util.LinkedList;
 
 import javax.swing.JOptionPane;
 
import display.MainWindow;

import settings.WindowManager;

 import model.DicomImage;
 import model.ServerInfo;
 
 
 /**
  * Gere le cryptage des fichiers dicom (uniquement)
  * @author Mobilette
  *
  */
 public class EncryptDaemon extends Thread {
 
 	private LinkedList<Path> dicomToEncrypt;
 	private LinkedList<DicomImage> dicomImageToEncrypt;
 	private DicomEncryptWorker dEncryptWorker;
 	private ServerInfo serverInfo;
 	private DicomDaemon dicomDaemon;
 	private boolean stop;
 	private boolean waitingToStop;
 	
 	public EncryptDaemon(DicomDaemon dicomDaemon){
 		dicomToEncrypt = new LinkedList<Path>();
 		dicomImageToEncrypt = new  LinkedList<DicomImage>();
 		stop = false;
 		setDicomDaemon(dicomDaemon);
 		setServerInfo(getDicomDaemon().getServerInfo());
 		waitingToStop = false;
 	}
 	
 	
 	@Override
 	public void run() {
 		System.out.println("Encrypter Online.");
 		while(!isStop()){
 			// check si il y a des donnees a encrypter
 			while(dicomToEncrypt.isEmpty()){
 				if(waitingToStop)
 					setStop(true);
 				try {
 					Thread.sleep(5000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			// on lance l'encryptage du fichier
 			dEncryptWorker = new DicomEncryptWorker(this, (Path)dicomToEncrypt.pop(), (DicomImage)dicomImageToEncrypt.pop());
 			dEncryptWorker.start();  
 
 		}
 	}
 
 	public boolean isStop() {
 		return stop;
 	}
 	public void setStop(boolean stop) {
 		if(stop = true){
 			if(!dicomToEncrypt.isEmpty()){
 				Object[] options = {"Finish & stop",
 	                    "Force stop"};
				int r = JOptionPane.showOptionDialog(WindowManager.MAINWINDOW,
 					    ""+dicomToEncrypt.size() + " need to be encrypt ...",
					    "Warning",
 					    JOptionPane.YES_NO_CANCEL_OPTION,
 					    JOptionPane.QUESTION_MESSAGE,
 					    null,
 					    options,
 					    options[0]);
				//String result = (String)optionPane.getValue();
				/*if(r==null)
					return;*/
				if(r == 0)
 					waitingToStop = true;
 				else
 					this.stop = true;
 			}
 		}else{
 			this.stop = stop;
 		}
 	}
 	public boolean isWaitingToStop() {
 		return waitingToStop;
 	}
 
 
 	public void setWaitingToStop(boolean waitingToStop) {
 		this.waitingToStop = waitingToStop;
 	}
 
 
 	public LinkedList<Path> getDicomToEncrypt() {
 		return dicomToEncrypt;
 	}
 	public void setDicomToEncrypt(LinkedList<Path> dicomToEncrypt) {
 		this.dicomToEncrypt = dicomToEncrypt;
 	}
 	public DicomEncryptWorker getdEncryptWorker() {
 		return dEncryptWorker;
 	}
 	public void setdEncryptWorker(DicomEncryptWorker dEncryptWorker) {
 		this.dEncryptWorker = dEncryptWorker;
 	}
 	public ServerInfo getServerInfo() {
 		return serverInfo;
 	}
 	public LinkedList<DicomImage> getDicomImageToEncrypt() {
 		return dicomImageToEncrypt;
 	}
 	public void setDicomImageToEncrypt(LinkedList<DicomImage> dicomImageToEncrypt) {
 		this.dicomImageToEncrypt = dicomImageToEncrypt;
 	}
 	public void setServerInfo(ServerInfo serverInfo) {
 		this.serverInfo = serverInfo;
 	}
 	public DicomDaemon getDicomDaemon() {
 		return dicomDaemon;
 	}
 	public void setDicomDaemon(DicomDaemon dicomDaemon) {
 		this.dicomDaemon = dicomDaemon;
 	}
 	public void addDicomToEncrypt(Path p, DicomImage di){
 		dicomToEncrypt.push(p);
 		dicomImageToEncrypt.push(di);
 	}
 	
 	/**
 	 * Indique au serveur nifti qu'il faut convertir ce repertoire
 	 * @param dicomWorker
 	 */
 	public void sendToNiftiDaemon(DicomEncryptWorker dEncryptWorker) {
 		if(dEncryptWorker.getPatientFolder() != null){
 			getDicomDaemon().getNiftiDaemon().addDir(dEncryptWorker.getSerieFolder(),dEncryptWorker.getDicomImage());
 		}
 		dEncryptWorker = null;
 	}
 
 }
