 package com.photon.phresco.plugins.nodejs;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.maven.plugin.logging.Log;
 import org.codehaus.plexus.util.FileUtils;
 
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugins.PhrescoBasePlugin;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 
 public class NodeJsPlugin extends PhrescoBasePlugin {
 
 	public NodeJsPlugin(Log log) {
 		super(log);
 	}
 
 	@Override
 	public void pack(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		try {
 			File targetDir = new File(mavenProjectInfo.getBaseDir() + DO_NOT_CHECKIN_FOLDER + File.separator + TARGET);
 			if (targetDir.exists()) {
 				FileUtils.deleteDirectory(targetDir);
 				log.info("Target Folder Deleted Successfully");
 			}
 			Package pack = new Package();
 			pack.pack(configuration, mavenProjectInfo, log);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	@Override
 	public void startServer(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		Start start = new Start();
 		start.start(configuration, mavenProjectInfo, log);
 	}
 
 	@Override
 	public void stopServer(MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		Stop stop = new Stop();
 		stop.stop(mavenProjectInfo, log);
 	}
 }
