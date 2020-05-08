 package org.bh.tests.junit.branchSpecificRepresentative;
 
 import org.bh.plugin.branchSpecificRepresentative.nace.ReadNACE;
 
 import junit.framework.TestCase;
 
 /**
  * This is just a test!
  * DO NOT USE! THIS IS NOT A UNIT TEST!
  *
  * @author Matthias Beste
  * @version 1.0, 03.01.2012
  *
  */
 public class NaceTest extends TestCase {	
 	
 	public void testImport()
 	{	
 		
		ReadNACE naceobj = new ReadNACE("src/org/bh/companydata/nace/de.xml");
 		naceobj.parseXML();
 //		two of the following parameters need to have an empty String
 		//System.out.println(naceobj.getName("", "", "05"));
 		System.out.println(naceobj.getBranch());
 	}
 
 }
