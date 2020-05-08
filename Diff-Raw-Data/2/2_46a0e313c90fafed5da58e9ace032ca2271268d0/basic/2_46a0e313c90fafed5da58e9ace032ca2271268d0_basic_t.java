 // Tags: JDK1.1
 
 // Copyright (C) 2003 John Leuner
 
 // This file is part of Mauve.
 
 // Mauve is free software; you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation; either version 2, or (at your option)
 // any later version.
 
 // Mauve is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with Mauve; see the file COPYING.  If not, write to
 // the Free Software Foundation, 59 Temple Place - Suite 330,
 // Boston, MA 02111-1307, USA.  */
 
 package gnu.testlet.java.util.jar.JarFile;
 
 import gnu.testlet.Testlet;
 import gnu.testlet.TestHarness;
 import gnu.testlet.ResourceNotFoundException;
 import java.util.Enumeration;
 import java.util.zip.*;
 import java.util.jar.*;
 import java.io.*;
 
 public class basic implements Testlet
 {
     public void test_jar(TestHarness harness, JarFile jarfile)
     {
 	try {
 	harness.checkPoint("manifest tests");
 	Manifest m = jarfile.getManifest();
 
 	harness.checkPoint("entries");
 
 	/* I'm not sure if the order of these entries has to be in this order or not. 
 	   But it would seem likely that someone will write a program expecting the files to be enumerated the same way every time
 	*/
 
 	int item = 1;
 	Enumeration e = jarfile.entries();
 	while(e.hasMoreElements())
 	    {
 		JarEntry je = (JarEntry) e.nextElement();
 		String s = new BufferedReader(new InputStreamReader(jarfile.getInputStream(je))).readLine();
 		switch(item)
 		    {
 		    case 1:
 			harness.check(s == null);
 			break;
 		    case 2:
 			harness.check(s.equals("Manifest-Version: 1.0"));
 			break;
 		    case 3:
 			harness.check(s.equals("this jar file was created with the jar tool for IBM JDK 1.3"));
 			break;
 		    case 4:
 			harness.check(s.equals("We seek peace. We strive for peace. And sometimes peace must be defended. A future lived at the mercy of terrible threats is no peace at all. If war is forced upon us, we will fight in a just cause and by just means -- sparing, in every way we can, the innocent. And if war is forced upon us, we will fight with the full force and might of the United States military -- and we will prevail."));
 			break;
 		    case 5:
 			harness.check(s.equals("I am he as you are he as you are me and we are all together."));
 			break;
 		    }
 		//		System.out.println("item " + item + " and string '" + s + "'");
		item++;
 	    }
 
 	jarfile.close();
 
 	} catch(IOException e)
 	    {
 		harness.check(false, "all jarfile tests failed");
 	    }
     }
 
   public void test (TestHarness harness)
   {
       try
 	  {
 	      test_jar(harness,
 			     new JarFile (harness.getResourceFile("gnu#testlet#java#util#jar#JarFile#jartest.jar")));
 	  }
       catch (gnu.testlet.ResourceNotFoundException _)
 	  {
 	      // FIXME: all tests should fail.
 	      harness.check(false, "all basic tests failed");
 	  }
       catch (IOException _)
 	  {
 	      // FIXME: all tests should fail.
 	      harness.check(false, "all basic tests failed (ioexception)");
 	  }
       
   }
 }
