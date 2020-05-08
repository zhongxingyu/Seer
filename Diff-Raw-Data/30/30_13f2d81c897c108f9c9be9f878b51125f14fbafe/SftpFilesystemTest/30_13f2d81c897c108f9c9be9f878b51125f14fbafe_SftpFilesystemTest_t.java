 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.javavfs.impl.sftp;
 
 import ch.ethz.ssh2.Connection;
 import ch.ethz.ssh2.SFTPv3Client;
 import ch.ethz.ssh2.SFTPv3DirectoryEntry;
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.Vector;
 import org.javavfs.Directory;
 import org.javavfs.File;
 import org.javavfs.Node;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  *
  * @author krog
  */
 public class SftpFilesystemTest {
 
     public SftpFilesystemTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     private SftpFileSystem fileSystem=null;
 
     @Test
     public void testList() throws Exception {
        SftpFileSystem fileSystem = new SftpFileSystem("appsrv291", 22, "systemwebtestmiljo", "wtest816");
 
        Directory dir = (Directory)fileSystem.getNode("/D:");
         List<Node> children = dir.getChildren();
         for(Node node:children){
             System.out.println(node.getName()+(node.isDirectory()?"(DIR)":""));
         }
 
        Node node = fileSystem.getNode("/D:/webtek/t8/tx/deployments/xportalen/current/deployment_info.xml");
         System.out.println(node.getName()+(node.isDirectory()?"(DIR)":""));
         
         InputStream is = ((File)node).getInputStream();
         BufferedReader br = new BufferedReader(new InputStreamReader(is));
         while(br.ready())
             System.out.println(br.readLine());
         is.close();
 
         fileSystem.close();
     }
 
 }
