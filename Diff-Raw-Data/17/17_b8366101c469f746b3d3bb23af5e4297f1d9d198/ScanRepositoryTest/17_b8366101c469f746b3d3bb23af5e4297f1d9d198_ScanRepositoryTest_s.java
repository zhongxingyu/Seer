 package com.soebes.multithreading.cp.supose.scan;
 
 import java.io.File;
 import java.net.URI;
 
 import org.testng.annotations.Test;
 import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
 import org.tmatesoft.svn.core.wc.SVNWCUtil;
 
 public class ScanRepositoryTest {
 
     @Test
    public void firstTest() {
//        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager("", "");
         ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager();
 
         URI repositoryURI = URI.create("file:///home/kama/test-supose/testrepos");
         File indexDirectory = new File("/home/kama/test-supose/test-index");
        RepositoryScanParameter parameter = new RepositoryScanParameter(authManager, repositoryURI, indexDirectory);
         
         IScanBehaviour scan = new ScanRepositoryStrategy();
         scan.scanRepository(parameter);
     }
 }
