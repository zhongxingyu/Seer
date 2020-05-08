 package org.hazelwire.test;
 
import java.util.ArrayList;

import org.hazelwire.main.Generator;
import org.hazelwire.modules.ManifestGenerator;
 
 /** This example demonstrates uploading of a file over SCP to the SSH server. */
 public class MainTest {
 
     public static void main(String[] args) throws Exception
     {
    	Generator generator = Generator.getInstance();
    	generator.getModuleSelector().selectModule(0);
    	generator.generateVM();
     }
 }
