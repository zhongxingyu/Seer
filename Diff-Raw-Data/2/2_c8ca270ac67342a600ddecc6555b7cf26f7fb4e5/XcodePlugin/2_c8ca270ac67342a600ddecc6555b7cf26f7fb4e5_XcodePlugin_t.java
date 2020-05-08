 package com.photon.phresco.plugins.xcode;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.logging.Log;
 
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.plugin.commons.MavenProjectInfo;
 import com.photon.phresco.plugins.PhrescoBasePlugin;
 import com.photon.phresco.plugins.model.Mojos.Mojo.Configuration;
 
 public class XcodePlugin extends PhrescoBasePlugin {
 
 	public XcodePlugin(Log log) {
 		super(log);
 	}
 
 	@Override
 	public void pack(Configuration configuration,
 			MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		Package pack = new Package();
 		pack.pack(configuration, mavenProjectInfo, getLog());
 		
 	}
 
 	@Override
 	public void deploy(Configuration configuration,
 			MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		Deploy deploy = new Deploy();
 		try {
 			deploy.deploy(configuration, mavenProjectInfo, getLog());
 		} catch (MojoExecutionException e) {
 		} catch (MojoFailureException e) {
 		}
 	}
 	
 	@Override
	public void validate(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		ClangCodeValidator validator = new ClangCodeValidator();
 		validator.validate(configuration);
 	}
 	
 	@Override
 	public void runUnitTest(Configuration configuration, MavenProjectInfo mavenProjectInfo) throws PhrescoException {
 		UnitTest test = new UnitTest();
 		test.unitTest(configuration);
 	}
 }
