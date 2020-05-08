 package daemon;
 
 import ij.IJ;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.security.GeneralSecurityException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 
 import org.apache.commons.io.FileUtils;
 
 import settings.SystemSettings;
 import settings.WindowManager;
 
 import model.DicomImage;
 import model.daemon.CustomConversionSettings;
 import model.daemon.CustomConversionSettings.ServerMode;
 
 import daemon.tools.nifti.Nifti_Reader;
 import dao.MySQLProjectDAO;
 import dao.ProjectDAO;
 import dao.project.AcquisitionDateDAO;
 import dao.project.DicomImageDAO;
 import dao.project.MySQLAcquisitionDateDAO;
 import dao.project.MySQLDicomImageDAO;
 import dao.project.MySQLNiftiImageDAO;
 import dao.project.MySQLPatientDAO;
 import dao.project.MySQLProtocolDAO;
 import dao.project.MySQLSerieDAO;
 import dao.project.NiftiImageDAO;
 import dao.project.PatientDAO;
 import dao.project.ProtocolDAO;
 import dao.project.SerieDAO;
 import es.vocali.util.AESCrypt;
 
 /**
  * Classe permettant de convertir en nifti
  * il utilise un decrypteur et un encrypteur daemon en interne
  * @author DEVERDUN Jeremy
  *
  */
 public class NiftiWorker extends DaemonWorker {
 
 	protected Path path;
 	protected Path niftiPath;
 	protected NiftiDaemon niftiDaemon;
 	protected DicomImage sourceDicomImage;
 	protected Nifti_Reader nr;
 	private Path tempDicomPath;
 	private Path tempNiftiPath;
 	private DecryptDaemon ddaemon;
 	private NiftiEncryptDaemon edaemon;
 	
 	public NiftiWorker(NiftiDaemon nDaemon, Path filename,DicomImage dimage) {
 		if(dimage==null) 
 			WindowManager.mwLogger.log(Level.SEVERE, "Error : sourceDicomImage is NULL in niftiWorker !");
 		setNiftiDaemon(nDaemon);
 		setPath(filename);
 		setServerInfo(getNiftiDaemon().getServerInfo());
 		setSourceDicomImage(dimage);
 		ddaemon = new DecryptDaemon();
 		edaemon = new NiftiEncryptDaemon();
 	}
 	public Path getPath() {
 		return path;
 	}
 	public void setPath(Path path) {
 		this.path = path;
 	}
 	public NiftiDaemon getNiftiDaemon() {
 		return niftiDaemon;
 	}
 	public void setNiftiDaemon(NiftiDaemon nDaemon) {
 		this.niftiDaemon = nDaemon;
 	}
 	public Path getNiftiPath() {
 		return niftiPath;
 	}
 	public void setNiftiPath(Path niftiPath) {
 		this.niftiPath = niftiPath;
 	}
 	public DicomImage getSourceDicomImage() {
 		return sourceDicomImage;
 	}
 	public void setSourceDicomImage(DicomImage sourceDicomImage) {
 		this.sourceDicomImage = sourceDicomImage;
 	}
 	@Override
 	public void start() {
 
 		// On recherche l'arborescence et on cr les rpertoire si besoin +
 		// NIFTIDIR / NOM_ETUDE / NOM_PATIENT / DATE_IRM / PROTOCOL / SERIE 
 		Path studyName = path.getParent().getParent().getParent().getParent().getFileName();
 		
 		Path patientName = path.getParent().getParent().getParent().getFileName();
 		Path acqDate = path.getParent().getParent().getFileName();
 		Path protocolAcqName = path.getParent().getFileName() ;
 		Path serieName = path.getFileName();
 		
 		Path studyDir = Paths.get(serverInfo.getServerDir()+"/"+serverInfo.NRI_ANALYSE_NAME + File.separator + studyName);
 		Path patientDir = Paths.get(studyDir + File.separator +  patientName);
 		Path acqDateDir = Paths.get(patientDir + File.separator +  acqDate);
 		Path protocolDir = Paths.get(acqDateDir + File.separator +  protocolAcqName);
 		Path serieDir = Paths.get(protocolDir + File.separator +  serieName);
 		// important pour le decryptage
 		setProjectFolder(studyDir);
 		setPatientFolder(patientDir);
 		
 		
 		checkAndMakeDir(studyDir);
 		checkAndMakeDir(patientDir);
 		checkAndMakeDir(acqDateDir);
 		checkAndMakeDir(protocolDir);
 		checkAndMakeDir(serieDir);
 		
 		niftiPath = serieDir;
 		WindowManager.mwLogger.log(Level.INFO, "Nifti convert : "+path);
 
 		Process process;
 		try {
 			//process = Runtime.getRuntime().exec("mcverter.exe "+ path +" -o "+ niftiPath.toString() + " -f fsl -x -r");//-x 
 			// On recupere la liste des nifti qui existait avant la conversion
 			// sous la forme "nom_datamodif"
 			HashMap<String,Path> niftiBefore = getNiftiListIn(niftiPath);
 			// on les efface (car dcm2nii n'overwrite pas !)
 			removeFiles(niftiBefore);
 			
 			// ------------------------------------------------------------------- //
 			// on decrypte les fichiers dicom temporairement (pour la conversion)  //
 			// que l'on place dans le repertoire temporaire (tempDir)              //
 			// ------------------------------------------------------------------- //
 			String command = "";
 			AESCrypt aes = null;
 			aes = new AESCrypt(false, getAESPass());
 			tempDicomPath = Paths.get(getServerInfo().getTempDir() + "/Dicom" + System.currentTimeMillis() + "_" + serieName);
 			tempNiftiPath = Paths.get(getServerInfo().getTempDir() + "/Nifti" + System.currentTimeMillis() + "_" + serieName);
 			buildIfNotExist(tempDicomPath);
 			buildIfNotExist(tempNiftiPath);
 			for(String name:path.toFile().list()){
 				if(name.endsWith(AESCrypt.ENCRYPTSUFFIX)){
 					String dpath = path + "/" +  name;
 					String tpath = tempDicomPath + "/" + name.substring(0, name.length()-4); // on recupere le vrai nom du dicom (sans le .enc)
 					ddaemon.addFileToDecrypt(Paths.get(dpath), tempDicomPath);
 					//aes.decrypt(dpath, tpath);// on envoi la version decrypte dans le dossier temp
 				}
 			}
 			// on lance le daemon pr decrypter
 			ddaemon.start();
 			// on attend que le decrypter ai fini son boulot
 			int iter = 0;
			Thread.sleep(1000);
			while(!ddaemon.isWaiting() || dirContainsPart(tempDicomPath)){
 				try{
 					Thread.sleep(1000);
 					iter++;
 					if(iter == 1000){
 						WindowManager.mwLogger.log(Level.SEVERE, "NiftiWorker in infinite loop ... stopping. ["+tempDicomPath+"]");
 						WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningniftidaemon().setText("NiftiWorker in infinite loop ... stopping");
 					}
 				}catch(InterruptedException e){
 					e.printStackTrace();
 				}
 			}
 			ddaemon.setStop(true);
 			// On cree la commande (on convertie dans un autre repertoire)
 			command = buildConvertizerConvertCommandFor(tempDicomPath,tempNiftiPath,false);
 			//command = buildDcm2niiConvertCommandFor(tempDicomPath,tempNiftiPath,false);
 
 			// on convertie
 			process = Runtime.getRuntime().exec(command);
 			if(getNiftiDaemon().getSettings().isNiftiDebugMode()){
 				InputStream stdin = process.getInputStream();
 	            InputStreamReader isr = new InputStreamReader(stdin);
 	            BufferedReader br = new BufferedReader(isr);
 	            String line = null;
 	            WindowManager.mwLogger.log(Level.FINE, "Converting "+path);
 	            WindowManager.mwLogger.log(Level.FINE, "<OUTPUT>");
 	            while ( (line = br.readLine()) != null)
 	            	WindowManager.mwLogger.log(Level.FINE, line);
 	            WindowManager.mwLogger.log(Level.FINE, "</OUTPUT>");
 			}
 			process.waitFor();
 			// On recupere les nom des fichiers nifti cree
 			// on les encrypt et on les deplace dans leur repertoire final
 			HashMap<String,Path> niftis = getNiftiListIn(tempNiftiPath);
 			for(String currNifti:niftis.keySet()){
 				Path finalNiftiPath = Paths.get(getNiftiPath() + "/" + niftis.get(currNifti).getFileName());
 				Path newPath = Paths.get(finalNiftiPath + AESCrypt.ENCRYPTSUFFIX);
 				edaemon.addNiftiToEncrypt(niftis.get(currNifti), getNiftiPath(), sourceDicomImage);
 				/*if(nr!=null)
 					nr = null;
 				nr = new Nifti_Reader(niftis.get(currNifti).toFile());
 				aes.encrypt(2,niftis.get(currNifti).toString(), newPath.toString());
 				addEntryToDB(finalNiftiPath,"NiftiImage");*/
 			}
 			// On recupere les noms des fichiers associe aux nifti cree (bval pour dti etc (cf niftiDaemon.suffixe...)
 			// on les encrypt et on les deplace dans leur repertoire final (mais pas d'ajout dans la bdd)
 			HashMap<String,Path> associatedInfoFiles = getAssociatedNiftiListIn(tempNiftiPath);
 			for(String assocNifti:associatedInfoFiles.keySet()){
 				Path finalNiftiPath = Paths.get(getNiftiPath() + File.separator + associatedInfoFiles.get(assocNifti).getFileName());
 				Path newPath = Paths.get(finalNiftiPath + AESCrypt.ENCRYPTSUFFIX);
 				edaemon.addNiftiToEncrypt(associatedInfoFiles.get(assocNifti), getNiftiPath());
 				//aes.encrypt(2,associatedInfoFiles.get(assocNifti).toString(), newPath.toString());
 			}
 			// on lance le daemon pour encrypter
 			edaemon.start();
 			// on attend que l'encrypter ai fini son boulot
 			while(!edaemon.isWaiting()){
 				try{
 					Thread.sleep(100);
 				}catch(InterruptedException e){
 					e.printStackTrace();
 				}
 			}
 			edaemon.setStop(true);
 			// On supprime tous les fichiers cree dans tempDir
 			delete(tempDicomPath.toFile());
 			delete(tempNiftiPath.toFile());
 
 			
 		} catch (IOException e1) {
 			WindowManager.mwLogger.log(Level.SEVERE, "IOException with niftiWorker ("+path+")",e1);
 			WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningniftidaemon().setText((path.getFileName()+"-"+e1.toString()).substring(0, Math.min(e1.toString().length(), 100)));
 		} catch (InterruptedException e) {
 			WindowManager.mwLogger.log(Level.SEVERE, "InterruptedException with niftiWorker ("+path+")",e);
 			WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningniftidaemon().setText((path.getFileName()+"-"+e.toString()).substring(0, Math.min(e.toString().length(), 100)));
 		} catch (GeneralSecurityException e) {
 			WindowManager.mwLogger.log(Level.SEVERE, "GeneralSecurityException with niftiWorker ("+path+")",e);
 			WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningniftidaemon().setText((path.getFileName()+"-"+e.toString()).substring(0, Math.min(e.toString().length(), 100)));
 		}
 
 	}
 
 
 	private boolean dirContainsPart(Path path) {
 		for(String s:path.toFile().list()){
 			if(s.endsWith(".part"))
 				return true;
 		}
 		return false;
 	}
 	// supprime les fichiers renseignes dans une hashmap
 	protected void removeFiles(HashMap<String, Path> niftis) {
 		for(String currNifti:niftis.keySet())
 			try {
 				Files.delete(niftis.get(currNifti));
 				if(getNiftiDaemon().getSettings().getServerMode() == ServerMode.SERVER)
 					removeDBEntry(niftis.get(currNifti).getFileName());
 			} catch (IOException e) {
 				WindowManager.mwLogger.log(Level.SEVERE, "removeFiles error",e);
 			}
 		return;
 	}
 	
 	// Supprime une entree dans la table niftiimage de la bdd
 	// o le nom du fichier = fileName et les id correspondent
 	private void removeDBEntry(Path fileName) {
 		NiftiImageDAO ndao = new MySQLNiftiImageDAO();
 		try {
 			ndao.removeEntry(fileName.getFileName().toString(),sourceDicomImage.getProjet().getId(),sourceDicomImage.getPatient().getId(),
 					sourceDicomImage.getAcquistionDate().getId(),sourceDicomImage.getProtocole().getId(),sourceDicomImage.getSerie().getId());
 		} catch (SQLException e) {
 			WindowManager.mwLogger.log(Level.WARNING, "removeDBEntry error",e);
 			WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningniftidaemon().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)));
 		}
 		
 	}
 	
 	// Construit la commande pour convertir un repertoire dicom (dicomPath) en nifti
 	// fourDim permet de renseigner si on stocke les fichiers dans un format 4d ou pas
 	protected String buildDcm2niiConvertCommandFor(Path dicomPath, Path niftiPath, boolean fourDim) {
 		// -i id in filename | -p protocol in filename
 		String command = "dcm2nii.exe -i y -p y -e n -a n -d n -e n -f n -l 0 -r n -x n ";
 		switch(getNiftiDaemon().getFormat()){
 		case NiftiDaemon.ANALYZE:
 			command+=" -n n -s y -g n ";break;
 		case NiftiDaemon.SPM:
 			command+=" -n n -g n ";break;
 		case NiftiDaemon.NIFTI://A selectionner en prio ?
 			command+=" -n y -g n ";break;
 		case NiftiDaemon.FSL:
 			command+=" -n y -g y ";break;
 		default:
 			WindowManager.mwLogger.log(Level.SEVERE, "Unknow nifti format");
 		}
 		if(fourDim)
 			command+=" -4D y ";
 		else
 			command+=" -4D n ";
 		command+=" -o \""+niftiPath+"\" \""+dicomPath+"\"";
 		return command;
 	}
 	
 	protected String buildConvertizerConvertCommandFor(Path dicomPath, Path niftiPath, boolean fourDim) {
 		// -i id in filename | -p protocol in filename
 		String command = "";
 		if(IJ.isWindows())
 			command = "\""+SystemSettings.APP_DIR+File.separator+"convertizer.exe\" \""+dicomPath+"\" -o \""+niftiPath+"\" ";
 		else
 			command = "convertizer \""+dicomPath+"\" -o \""+niftiPath+"\" ";
 		command+= " -r "; // apply rescale slope & intercept
 		command+= " -n "; //save as .nii files
 		switch(getNiftiDaemon().getFormat()){
 		case NiftiDaemon.ANALYZE:
 			command+=" -f analyze ";break;
 		case NiftiDaemon.SPM:
 			command+=" -f spm ";break;
 		case NiftiDaemon.NIFTI:
 			command+=" -f nifti ";break;
 		case NiftiDaemon.FSL://A selectionner en prio ?
 			command+=" -f fsl ";break;
 		default:
 			WindowManager.mwLogger.log(Level.SEVERE, "Unknow nifti format");
 		}
 		if(fourDim)
 			command+=" -d ";
 		return command;
 	}
 	
 	@Override
 	protected void addEntryToDB(Path name, String table) {
 		switch(table){
 		case "NiftiImage":
 			NiftiImageDAO dicdao = new MySQLNiftiImageDAO();
 			SerieDAO sdao = new MySQLSerieDAO();
 			try {
 				dicdao.newNiftiImage(name.getFileName().toString(), nr.getNSlices(),sourceDicomImage.getProjet().getId(),sourceDicomImage.getPatient().getId(),
 						sourceDicomImage.getAcquistionDate().getId(),sourceDicomImage.getProtocole().getId(),sourceDicomImage.getSerie().getId());
 				// on indique a la serie qu'elle est dispo en nifti
 				sdao.updateHasNifti(sourceDicomImage.getSerie().getId(),1);
 			} catch (SQLException e) {
 				WindowManager.mwLogger.log(Level.SEVERE,"SQLException with niftiWorker",e);
 				WindowManager.MAINWINDOW.getSstatusPanel().getLblWarningniftidaemon().setText(e.toString().substring(0, Math.min(e.toString().length(), 100)));
 			}
 			break;
 		default:
 			WindowManager.mwLogger.log(Level.SEVERE, "I don't know table : "+table+" ... sorry");	
 			
 		}
 	}
 
 	// renvoi une arrayList avec les nifti nouvellement cree
 	// compare le contenu de 2 hashmap issuent 
 	// de getNiftiListIn
 	@SuppressWarnings("unused")
 	protected ArrayList<Path> extractNewNiftiFrom(HashMap<String, Path> niftiAfter,
 			HashMap<String, Path> niftiBefore) {
 		ArrayList<Path> niftis = new ArrayList<Path>();
 		boolean oldIsEmpty = niftiBefore.isEmpty();
 		for(String name:niftiAfter.keySet()){
 			if(oldIsEmpty || !niftiBefore.containsKey(name))
 				niftis.add(niftiAfter.get(name));
 		}
 		return niftis;
 	}
 
 	// recupere la liste des nifti qui existe dans le repertoire "niftiPath"
 	// sous la forme couple Key/Value "nom_datamodif"/Path
 	protected HashMap<String, Path> getNiftiListIn(Path niftiPath) {
 		HashMap<String, Path> niftiList = new HashMap<String, Path>();
 		String[] list = niftiPath.toFile().list();
 		String ext;
 		switch(getNiftiDaemon().getFormat()){
 		case NiftiDaemon.NIFTI:
 			ext = ".nii";break;
 		case NiftiDaemon.FSL:
 			ext = ".nii";break;
 		default:
 			ext = ".nii";break;
 		}
 		for(String name:list){
 			String fullpath = niftiPath + "/" + name;
 			if(name.endsWith(ext))
 				niftiList.put(name+"_"+(new File(fullpath).lastModified()),Paths.get(fullpath));
 		}
 		return niftiList;
 	}
 	
 	/**
 	 * Recupere les fichiers associee a des nifti (type bval.txt etc)
 	 * @param tempNiftiPath
 	 * @return
 	 */
 	private HashMap<String, Path> getAssociatedNiftiListIn(Path tempNiftiPath) {
 		HashMap<String, Path> assocNiftiList = new HashMap<String, Path>();
 		String[] list = tempNiftiPath.toFile().list();
 		for(String name:list){
 			String fullpath = tempNiftiPath + "/" + name;
 			for(String suf:NiftiDaemon.suffixeToRemoveWithNifti){
 				if(name.endsWith(suf))
 					assocNiftiList.put(name+"_"+(new File(fullpath).lastModified()),Paths.get(fullpath));
 			}
 		}
 		return assocNiftiList;
 	}
 	
 	/**
 	 * Cree un repertoire
 	 * @param p Path
 	 */
 	public void buildIfNotExist(Path p){
 		try {
 			if(Files.exists(p)){
 				try{
 					FileUtils.deleteDirectory(p.toFile());
 				}catch(Exception e){
 					Files.delete(p);
 				}
 			}
 			Files.createDirectories(p);
 		} catch (IOException e) {
 			WindowManager.mwLogger.log(Level.WARNING, "buildIfNotExist error",e);
 		}
 	}
 	
 	/**
 	 * Supprime recursivement un repertoire
 	 * @param f
 	 * @throws IOException
 	 */
 	public void delete(File f) throws IOException {
 	  if (f.isDirectory()) {
 	    for (File c : f.listFiles())
 	      delete(c);
 	  }
 	  if (!f.delete())
 	    throw new FileNotFoundException("Failed to delete file: " + f);
 	}
 	
 	/**
 	 * Efface les fichiers (temporaires) cree par le worker 
 	 * @throws IOException 
 	 */
 	public void clean() throws IOException {
 		if(tempDicomPath.toFile().exists())
 			delete(tempDicomPath.toFile());
 		if(tempNiftiPath.toFile().exists())
 			delete(tempNiftiPath.toFile());
 	}
 
 }
