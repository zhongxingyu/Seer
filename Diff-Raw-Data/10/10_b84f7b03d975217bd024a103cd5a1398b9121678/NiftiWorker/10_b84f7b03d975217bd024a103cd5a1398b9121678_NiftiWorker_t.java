 package daemon;
 
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
 
 import model.DicomImage;
 
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
 
 public class NiftiWorker extends DaemonWorker {
 
 	private Path path;
 	private Path niftiPath;
 	private NiftiDaemon niftiDaemon;
 	private DicomImage sourceDicomImage;
 	
 	public NiftiWorker(NiftiDaemon nDaemon, Path filename,DicomImage dimage) {
 		if(dimage==null) 
 			System.err.println("Error : sourceDicomImage is NULL in niftiWorker !");
 		setNiftiDaemon(nDaemon);
 		setPath(filename);
 		setServerInfo(getNiftiDaemon().getServerInfo());
 		setSourceDicomImage(dimage);
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
 		setProjectFolder(studyName);
 		Path patientName = path.getParent().getParent().getParent().getFileName();
 		Path acqDate = path.getParent().getParent().getFileName();
 		Path protocolAcqName = path.getParent().getFileName() ;
 		Path serieName = path.getFileName();
 		
 		Path studyDir = Paths.get(serverInfo.getNiftiDir().toString() + File.separator + studyName);
 		Path patientDir = Paths.get(studyDir + File.separator +  patientName);
 		Path acqDateDir = Paths.get(patientDir + File.separator +  acqDate);
 		Path protocolDir = Paths.get(acqDateDir + File.separator +  protocolAcqName);
 		Path serieDir = Paths.get(protocolDir + File.separator +  serieName);
 		
 		checkAndMakeDir(studyDir);
 		checkAndMakeDir(patientDir);
 		checkAndMakeDir(acqDateDir);
 		checkAndMakeDir(protocolDir);
 		checkAndMakeDir(serieDir);
 		
 		niftiPath = serieDir;
 		System.out.println("Nifti convert : "+path);
 
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
 			Path tempDicomPath = null;
 			Path tempNiftiPath = null;
 			if(getNiftiDaemon().isServerMode()){
 				aes = new AESCrypt(false, getAESPass());
 				tempDicomPath = Paths.get(getServerInfo().getTempDir() + "/Dicom" + serieName);
 				tempNiftiPath = Paths.get(getServerInfo().getTempDir() + "/Nifti" + serieName);
 				buildIfNotExist(tempDicomPath);
 				buildIfNotExist(tempNiftiPath);
 				for(String name:path.toFile().list()){
 					if(name.endsWith(AESCrypt.ENCRYPTSUFFIX)){
 						String dpath = path + "/" +  name;
 						String tpath = tempDicomPath + "/" + name.substring(0, name.length()-4); // on recupere le vrai nom du dicom (sans le .enc)
 						aes.decrypt(dpath, tpath);// on envoi la version decrypte dans le dossier temp
 					}
 				}
 				
 				// On cree la commande (on convertie dans un autre repertoire)
 				command = buildConvertCommandFor(tempDicomPath,tempNiftiPath);
 			}else{
 				command = buildConvertCommandFor(path,niftiPath);
 			}
 			// on convertie
 			process = Runtime.getRuntime().exec(command);
 			if(false){
 				InputStream stdin = process.getInputStream();
 	            InputStreamReader isr = new InputStreamReader(stdin);
 	            BufferedReader br = new BufferedReader(isr);
 	            String line = null;
 	            System.out.println("<OUTPUT>");
 	            while ( (line = br.readLine()) != null)
 	                System.out.println(line);
 	            System.out.println("</OUTPUT>");
 			}
 			process.waitFor();
 			
 			if(getNiftiDaemon().isServerMode()){
 				// On recupere les nom des fichiers nifti cree
 				// on les encrypt et on les deplace dans leur repertoire final
 				HashMap<String,Path> niftis = getNiftiListIn(tempNiftiPath);
 				for(String currNifti:niftis.keySet()){
 					Path finalNiftiPath = Paths.get(getNiftiPath() + "/" + niftis.get(currNifti).getFileName());
 					Path newPath = Paths.get(finalNiftiPath + AESCrypt.ENCRYPTSUFFIX);
 					aes.encrypt(2,niftis.get(currNifti).toString(), newPath.toString());
 					addEntryToDB(finalNiftiPath,"NiftiImage");
 				}
 			
 			
 				// On supprime tous les fichiers cree dans tempDir
 				delete(tempDicomPath.toFile());
 				delete(tempNiftiPath.toFile());
 			}
 			
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (GeneralSecurityException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	// supprime les fichiers renseignes dans une hashmap
 	private void removeFiles(HashMap<String, Path> niftis) {
 		for(String currNifti:niftis.keySet())
 			try {
 				Files.delete(niftis.get(currNifti));
 				if(getNiftiDaemon().isServerMode())
 					removeDBEntry(niftis.get(currNifti).getFileName());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		return;
 	}
 	
 	// Supprime une entree dans la table niftiimage de la bdd
 	// o le nom du fichier = fileName et les id correspondent
 	private void removeDBEntry(Path fileName) {
 		NiftiImageDAO ndao = new MySQLNiftiImageDAO();
 		try {
 			ndao.removeEntry(fileName.getFileName().toString(),sourceDicomImage.getMri_name(),sourceDicomImage.getProjet().getId(),sourceDicomImage.getPatient().getId(),
 					sourceDicomImage.getAcquistionDate().getId(),sourceDicomImage.getProtocole().getId(),sourceDicomImage.getSerie().getId());
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	// Construit la commande pour convertir un repertoire dicom (dicomPath) en nifti
 	private String buildConvertCommandFor(Path dicomPath, Path niftiPath) {
 		// -i id in filename | -p protocol in filename
 		String command = "dcm2nii.exe -i y -p y -e n -a n -d n -e n -f n -l 0 -r n -x n ";
 		switch(getNiftiDaemon().getFormat()){
 		case NiftiDaemon.ANALYZE_7_5:
 			command+=" -n n -s y -g n ";break;
 		case NiftiDaemon.SPM5_NIFTI:
 			command+=" -n n -g n ";break;
 		case NiftiDaemon.NIFTI_4D://A selectionner en prio ?
 			command+=" -n y -g n ";break;
 		case NiftiDaemon.FSL_NIFTI:
 			command+=" -n y -g y ";break;
 		default:
 			System.err.println("Unknow nifti format");
 		}
		command+=" -o \""+niftiPath+"\" \""+dicomPath+"\"";
 		return command;
 	}
 	
 	@Override
 	protected void addEntryToDB(Path name, String table) {
 		switch(table){
 		case "NiftiImage":
 			NiftiImageDAO dicdao = new MySQLNiftiImageDAO();
 			try {
 				dicdao.newNiftiImage(name.getFileName().toString(), sourceDicomImage.getMri_name(),sourceDicomImage.getProjet().getId(),sourceDicomImage.getPatient().getId(),
 						sourceDicomImage.getAcquistionDate().getId(),sourceDicomImage.getProtocole().getId(),sourceDicomImage.getSerie().getId());
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			break;
 		default:
 			System.err.println("I don't know table : "+table+" ... sorry");	
 		}
 	}
 
 	// renvoi une arrayList avec les nifti nouvellement cree
 	// compare le contenu de 2 hashmap issuent 
 	// de getNiftiListIn
 	@SuppressWarnings("unused")
 	private ArrayList<Path> extractNewNiftiFrom(HashMap<String, Path> niftiAfter,
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
 	private HashMap<String, Path> getNiftiListIn(Path niftiPath) {
 		HashMap<String, Path> niftiList = new HashMap<String, Path>();
 		String[] list = niftiPath.toFile().list();
 		String ext;
 		switch(getNiftiDaemon().getFormat()){
 		case NiftiDaemon.NIFTI_4D:
 			ext = ".nii";break;
 		case NiftiDaemon.FSL_NIFTI:
 			ext = ".nii.gz";break;
 		default:
 			ext = ".img";
 		}
 		for(String name:list){
 			String fullpath = niftiPath + "/" + name;
 			if(name.endsWith(ext))
 				niftiList.put(name+"_"+(new File(fullpath).lastModified()),Paths.get(fullpath));
 		}
 		return niftiList;
 	}
 	
 	/**
 	 * Cree un repertoire
 	 * @param p Path
 	 */
 	public void buildIfNotExist(Path p){
 		try {
 			if(Files.exists(p))
 				Files.delete(p);
 			Files.createDirectories(p);
 		} catch (IOException e) {
 			e.printStackTrace();
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
 
 }
