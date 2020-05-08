 package com.exalead.codesearch.connectors;
 
 import java.io.File;
 
 import junit.framework.TestCase;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.BasicConfigurator;
 
import com.exalead.mercury.plugin.simple.SimplePluginManager;

public abstract class ConnectorTests extends TestCase {
     
     protected String rootPath;
     
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         rootPath = FileUtils.getTempDirectory() + "/tests/connectors/" + this.getName();
         FileUtils.deleteDirectory(new File(rootPath));
         new File(rootPath).mkdirs();
         BasicConfigurator.configure();
        SimplePluginManager.getCurrentInstance();
    }
    
    protected String getRepositoryFile(String repositoryName) {
        return "file://" + getClass().getResource("/repositories/" + repositoryName).getFile();
     }
     
 }
