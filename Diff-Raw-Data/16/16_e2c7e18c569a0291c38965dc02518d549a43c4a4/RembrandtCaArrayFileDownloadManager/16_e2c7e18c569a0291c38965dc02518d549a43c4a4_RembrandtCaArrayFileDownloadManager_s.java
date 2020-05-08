 package gov.nih.nci.rembrandt.download.caarray;
 import gov.nih.nci.caarray.services.ServerConnectionException;
 import gov.nih.nci.caintegrator.application.download.caarray.CaArrayFileDownloadManager;
 
 import java.io.File;
 import java.net.MalformedURLException;
 
 import javax.security.auth.login.LoginException;
 
 
 public class RembrandtCaArrayFileDownloadManager extends CaArrayFileDownloadManager{
     public static final String SERVER_URL = "rembrandt.caarray.server.url";
     
     public static final String GE_EXPERIMENT_NAME = "rembrandt.caarray.ge.experiment.name";
     
     public static final String CN_EXPERIMENT_NAME = "rembrandt.caarray.cn.experiment.name";
     
     public static final String USER_NAME = "rembrandt.caarray.user.name";
     
     public static final String PWD = "rembrandt.caarray.user.pwd";
     
     public static final String INPUT_DIR = "rembrandt.caarray.download.input.dir";
     
     public static final String OUTPUT_ZIP_DIR = "rembrandt.caarray.download.output.zip.dir";
     
     public static final String DIR_IN_ZIP = "rembrandt.caarray.download.dirInZip";
     
     public static final String ZIP_FILE_URL = "rembrandt.caarray.download.zip.url";
     
 	protected RembrandtCaArrayFileDownloadManager(String caarrayUrl,
 			String username, String password,
 			String inputDirectory, String outputZipDirectory, String directoryInZip) 
 			throws MalformedURLException, LoginException, ServerConnectionException {
 		super(caarrayUrl, username, password, inputDirectory, outputZipDirectory, directoryInZip); 
 	}
	public synchronized static CaArrayFileDownloadManager getInstance() throws MalformedURLException, LoginException, ServerConnectionException 
 	{
 		if (instance == null)
 		{
 			  //Get the caArray properties from the rembrandt.properties file
 			  String caarrayUrl = System.getProperty(SERVER_URL);
 			  String username = System.getProperty(USER_NAME);
 			  String password = System.getProperty(PWD);
 			  String inputDirectory = System.getProperty(INPUT_DIR);
 			  String outputZipDirectory = System.getProperty(OUTPUT_ZIP_DIR);
 			  String directoryInZip = System.getProperty(DIR_IN_ZIP);
 			  String geExperimentName = System.getProperty(RembrandtCaArrayFileDownloadManager.GE_EXPERIMENT_NAME);
 			  String cnExperimentName  = System.getProperty(RembrandtCaArrayFileDownloadManager.CN_EXPERIMENT_NAME);
 			  if((geExperimentName == null) ||
 				 (cnExperimentName == null) ||
 				 (caarrayUrl == null) || 
 				 (username== null) ||
 				 (password == null) ||
 				 (inputDirectory == null) ||
 				 (outputZipDirectory == null) ||
 				 (directoryInZip == null)){
 				  logger.error("One of required input parameters is null");
 				  throw new IllegalStateException();
 			  }
 			  File inputDir = new File(inputDirectory);
 			  if(!inputDir.isDirectory()){
 				  inputDir.mkdir();
 			  }
 			  File outputZipDir = new File(outputZipDirectory);
 			  if(!outputZipDir.isDirectory()){
 				  outputZipDir.mkdir();
 			  }
			instance = new RembrandtCaArrayFileDownloadManager(caarrayUrl, username, password,inputDirectory, outputZipDirectory, directoryInZip); 
 		}
 		return instance;
 	}
 }
