 package org.iucn.sis.server.extensions.offline;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.util.Properties;
 
 import org.gogoego.api.plugins.GoGoEgo;
 import org.hibernate.Session;
 import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ResourceException;
 
 public class OfflineBackupRestlet extends BaseServiceRestlet {
 	
 	public OfflineBackupRestlet(Context context) {
 		super(context);
 	}
 	
 	@Override
 	public void definePaths() {
 		paths.add("/offline/backupOffline");
 	}
 	
 	@Override
 	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
 		
 		Properties init = GoGoEgo.getInitProperties();
 		
		String dbUri = init.getProperty("dbsession.sis_offline.uri");
 		String dbLocation =  dbUri.substring(dbUri.indexOf("file:")+5,dbUri.length());	
 		
 		return new StringRepresentation(backUpFiles(dbLocation), MediaType.TEXT_XML);
 	}
 	
	private String backUpFiles(String location){
 		String message = "";
 		try{
 			File tempfile = new File(location);
 			String sourceDir = location.substring(0, location.indexOf(tempfile.getName())-1);
 			
			File destDir = new File(location+"_backup");
 			destDir.mkdir();
 			
 			File sourceFile = new File(sourceDir);
 			File[] fileList = sourceFile.listFiles();
 			
 			for (int i = 0; i < fileList.length; i++) {				
 				if(fileList[i].getName().contains(".lock"))
 					continue;
 					
				if (fileList[i].isFile()) 
					copyFile(fileList[i].getAbsolutePath(), destDir.getAbsolutePath()+"\\"+fileList[i].getName());			    
 			} 
 			message = "Backup Successfully! Location -"+destDir.getAbsolutePath();
 		}catch (Exception e) {
 			message = "Backup Error!";
 		}
 		return message;
 	}
 	
	private static void copyFile(String sourcePath, String destPath) throws IOException {
 		
 		File sourceFile = new File(sourcePath);
		File destFile = new File(destPath);
 		
 		if (!sourceFile.exists()) {
 		    return;
 		}
 		if (!destFile.exists()) {
 		    destFile.createNewFile();
 		}
 		FileChannel source = null;
 		FileChannel destination = null;
 		source = new FileInputStream(sourceFile).getChannel();
 		destination = new FileOutputStream(destFile).getChannel();
 		if (destination != null && source != null) {
 		    destination.transferFrom(source, 0, source.size());
 		}
 		if (source != null) {
 		    source.close();
 		}
 		if (destination != null) {
 		    destination.close();
 		}
 	
 	}
 			
 }
