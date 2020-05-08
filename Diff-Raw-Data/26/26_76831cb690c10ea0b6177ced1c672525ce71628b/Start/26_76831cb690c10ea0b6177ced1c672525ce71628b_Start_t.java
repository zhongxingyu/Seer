 package jobs;
 
 import graph.GraphDatabase;
 
 import java.io.File;
 
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.s3.AmazonS3Client;
 
 import play.Logger;
 import play.Play;
 import play.exceptions.ConfigurationException;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 import util.S3Config;
 
 @OnApplicationStart
 public class Start extends Job {
 	private static String IMAGE_PATH = null;
 	private static String GRAPH_DATA_PATH = null;
 	private static final String FS = System.getProperty("file.separator");
 	public void doJob(){
 		GraphDatabase.startGraphDatabase();
 		createImageFolder();
 //	    configS3();
 //		createImageFolder();
 //		if(!Play.configuration.get("application.mode").equals("dev")){
 //			GraphDatabase.startGraphDatabase();
 //		}
 	}
 	
 	private void createImageFolder(){
 		String home = System.getenv("HOME");
    	String imagePath = home + FS + ".tsg" + FS + "images" + FS;
     	File file = new File(imagePath);
     	if(!file.exists()){
     		file.mkdirs();
     	}
     	IMAGE_PATH = imagePath;
     	
 
    	String gdPath = home + FS + ".tsg" + FS + "graphdata" + FS;
     	file = new File(gdPath);
     	if(!file.exists()){
     		file.mkdirs();
     	}
     	GRAPH_DATA_PATH = gdPath;
 	}
 
 	public static String getImagePath(){
 		return IMAGE_PATH;
 	}
 	public static String getGraphJSONDataPath(){
 		return GRAPH_DATA_PATH;
 	}
 	
 	private void configS3(){
         Logger.info("Starting Amazon s3 client");
         if (!Play.configuration.containsKey("aws.access.key")) {
             throw new ConfigurationException("Bad configuration for s3: no access key");
         } else if (!Play.configuration.containsKey("aws.secret.key")) {
             throw new ConfigurationException("Bad configuration for s3: no secret key");
         } else if (!Play.configuration.containsKey("s3.bucket")) {
             throw new ConfigurationException("Bad configuration for s3: no s3 bucket");
         }
         S3Config.s3Bucket = Play.configuration.getProperty("s3.bucket");
         String accessKey = Play.configuration.getProperty("aws.access.key");
         String secretKey = Play.configuration.getProperty("aws.secret.key");
         AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
         S3Config.s3Client = new AmazonS3Client(awsCredentials); 
 	}
 
 }
