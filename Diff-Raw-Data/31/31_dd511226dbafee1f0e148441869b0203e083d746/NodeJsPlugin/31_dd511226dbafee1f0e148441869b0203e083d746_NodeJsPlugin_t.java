 package com.photon.phresco.plugins.nodejs;
 
 import java.io.File;
 import java.io.IOException;
import java.util.Map;
 
 import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.FileUtils;
 
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugins.PhrescoBasePlugin;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
import com.photon.phresco.plugins.util.MojoUtil;
import com.photon.phresco.util.Utility;
 
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
	
	@Override
	public void validate(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
		Map<String, String> configs = MojoUtil.getAllValues(configuration);
		MavenProject project = mavenProjectInfo.getProject();
		String workingDir = project.getBasedir().getPath();
		String skipTest = configs.get(SKIP);
		StringBuilder sb = new  StringBuilder();
		sb.append(TEST_COMMAND).
		append(STR_SPACE).
		append(SONARCOMMAND).
		append(STR_SPACE).
		append("-Dskip=").
		append(skipTest);
		Utility.executeStreamconsumer(sb.toString(), workingDir);
	}
 }
