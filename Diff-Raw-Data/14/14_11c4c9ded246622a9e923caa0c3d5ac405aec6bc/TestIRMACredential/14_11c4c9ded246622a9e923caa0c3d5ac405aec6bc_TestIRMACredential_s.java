 /**
  * TestIRMACredential.java
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  * Copyright (C) Wouter Lueks, Radboud University Nijmegen, September 2012.
  */
 
 
 package org.irmacard.credentials.idemix.test;
 
 import static org.junit.Assert.fail;
 
 import java.io.File;
 
 import javax.smartcardio.CardException;
 
 import net.sourceforge.scuba.smartcards.CardService;
 import net.sourceforge.scuba.smartcards.CardServiceException;
 import net.sourceforge.scuba.smartcards.CommandAPDU;
 
 import org.irmacard.credentials.Attributes;
 import org.irmacard.credentials.CredentialsException;
 import org.irmacard.credentials.idemix.IdemixCredentials;
 import org.irmacard.credentials.idemix.IdemixPrivateKey;
 import org.irmacard.credentials.idemix.spec.IdemixIssueSpecification;
 import org.irmacard.credentials.idemix.spec.IdemixVerifySpecification;
 import org.irmacard.credentials.idemix.util.CredentialInformation;
 import org.irmacard.credentials.idemix.util.IssueCredentialInformation;
 import org.irmacard.credentials.idemix.util.VerifyCredentialInformation;
 import org.junit.Test;
 import org.junit.BeforeClass;
 
 import service.IdemixService;
 
 
 public class TestIRMACredential {
 	@BeforeClass
 	public static void initializeInformation() {
 		CredentialInformation.setCoreLocation(new File(System
 				.getProperty("user.dir")).toURI()
 				.resolve("irma_configuration/"));
 	}
 
 	@Test
 	public void issueRootCredential() throws CardException, CredentialsException, CardServiceException {
 		IssueCredentialInformation ici = new IssueCredentialInformation("Surfnet", "root");
 		IdemixIssueSpecification spec = ici.getIdemixIssueSpecification();
 		IdemixPrivateKey isk = ici.getIdemixPrivateKey();
 		
 		IdemixService is = new IdemixService(TestSetup.getCardService());
 		IdemixCredentials ic = new IdemixCredentials(is);
 		ic.issuePrepare();
 		is.sendPin(TestSetup.DEFAULT_PIN);
 		Attributes attributes = getSurfnetAttributes();
 
 		ic.issue(spec, isk, attributes, null);
 	}
 
 	@Test
 	public void verifyRootCredentialAll() throws CardException, CredentialsException {
 		VerifyCredentialInformation vci = new VerifyCredentialInformation(
 				"Surfnet", "root", "Surfnet", "rootAll");
 		IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();
 
 		CardService cs = TestSetup.getCardService();
 		IdemixCredentials ic = new IdemixCredentials(cs);
 
 		Attributes attr = ic.verify(vspec);
 		
 		if (attr == null) {
 			fail("The proof does not verify");
 		} else {
 			System.out.println("Proof verified");
 		}
 		
 		attr.print();
 	}
 
 	@Test
 	public void verifyRootCredentialNone() throws CardException, CredentialsException {
 		VerifyCredentialInformation vci = new VerifyCredentialInformation(
 				"Surfnet", "root", "Surfnet", "rootNone");
 		IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();
 
 		CardService cs = TestSetup.getCardService();
 		IdemixCredentials ic = new IdemixCredentials(cs);
 
 		Attributes attr = ic.verify(vspec);
 		
 		if (attr == null) {
 			fail("The proof does not verify");
 		} else {
 			System.out.println("Proof verified");
 		}
 		
 		attr.print();
 	}
 
 	@Test
 	public void removeRootCredential() throws CardException, CredentialsException, CardServiceException {
 		IssueCredentialInformation ici = new IssueCredentialInformation("Surfnet", "root");
 		IdemixIssueSpecification spec = ici.getIdemixIssueSpecification();
 		
 		IdemixService cs = TestSetup.getIdemixService();
 		cs.open();
 		cs.selectApplet();
		cs.sendPin(TestSetup.DEFAULT_PIN);
		cs.transmit(new CommandAPDU(0x80, 0x30, 0x00, spec.getIdemixId()));
		cs.transmit(new CommandAPDU(0x80, 0x31, 0x00, spec.getIdemixId()));
 	}
 
 	@Test
 	public void issueStudentCredential() throws CardException, CredentialsException, CardServiceException {
 		IssueCredentialInformation ici = new IssueCredentialInformation("RU", "studentCard");
 		IdemixIssueSpecification spec = ici.getIdemixIssueSpecification();
 		IdemixPrivateKey isk = ici.getIdemixPrivateKey();
 		
 		IdemixService is = new IdemixService(TestSetup.getCardService());
 		IdemixCredentials ic = new IdemixCredentials(is);
 		ic.issuePrepare();
 		is.sendPin(TestSetup.DEFAULT_PIN);
 		Attributes attributes = getStudentCardAttributes();
 
 		ic.issue(spec, isk, attributes, null);
 	}
 
 	@Test
 	public void verifyStudentCredentialAll() throws CardException, CredentialsException {
 		VerifyCredentialInformation vci = new VerifyCredentialInformation("RU",
 				"studentCard", "RU", "studentCardAll");
 		IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();
 
 		IdemixCredentials ic = new IdemixCredentials(TestSetup.getCardService());
 
 		Attributes attr = ic.verify(vspec);
 		
 		if (attr == null) {
 			fail("The proof does not verify");
 		} else {
 			System.out.println("Proof verified");
 		}
 		
 		attr.print();
 	}
 
 	@Test
 	public void verifyStudentCredentialNone() throws CardException, CredentialsException {
 		VerifyCredentialInformation vci = new VerifyCredentialInformation("RU",
 				"studentCard", "RU", "studentCardNone");
 		IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();
 
 		CardService cs = TestSetup.getCardService();
 		IdemixCredentials ic = new IdemixCredentials(cs);
 
 		Attributes attr = ic.verify(vspec);
 		
 		if (attr == null) {
 			fail("The proof does not verify");
 		} else {
 			System.out.println("Proof verified");
 		}
 		
 		attr.print();
 	}
 	@Test
 	public void removeStudentCredential() throws CardException, CredentialsException, CardServiceException {
 		IssueCredentialInformation ici = new IssueCredentialInformation("RU", "studentCard");
 		IdemixIssueSpecification spec = ici.getIdemixIssueSpecification();
 		
 		IdemixService cs = TestSetup.getIdemixService();
 		cs.open();
 		cs.selectApplet();
 		cs.sendPin(TestSetup.DEFAULT_PIN);
 		cs.transmit(new CommandAPDU(0x80, 0x30, 0x00, spec.getIdemixId()));
 		cs.transmit(new CommandAPDU(0x80, 0x31, 0x00, spec.getIdemixId()));
 	}
 
 	@Test
 	public void issueAgeCredential() throws CardException, CredentialsException, CardServiceException {
 		IssueCredentialInformation ici = new IssueCredentialInformation("MijnOverheid", "ageLower");
 		IdemixIssueSpecification spec = ici.getIdemixIssueSpecification();
 		IdemixPrivateKey isk = ici.getIdemixPrivateKey();
 		
 		IdemixService is = new IdemixService(TestSetup.getCardService());
 		IdemixCredentials ic = new IdemixCredentials(is);
 		ic.issuePrepare();
 		is.sendPin(TestSetup.DEFAULT_PIN);
 		Attributes attributes = getAgeAttributes();
 		ic.issue(spec, isk, attributes, null);
 	}
 
 	@Test
 	public void verifyAgeCredentialAll() throws CardException, CredentialsException {
 		VerifyCredentialInformation vci = new VerifyCredentialInformation("MijnOverheid",
 				"ageLower", "MijnOverheid", "ageLowerAll");
 		IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();
 
 		CardService cs = TestSetup.getCardService();
 		IdemixCredentials ic = new IdemixCredentials(cs);
 
 		Attributes attr = ic.verify(vspec);
 		
 		if (attr == null) {
 			fail("The proof does not verify");
 		} else {
 			System.out.println("Proof verified");
 		}
 		
 		attr.print();
 	}
 
 	@Test
 	public void verifyAgeCredentialNone() throws CardException, CredentialsException {
 		VerifyCredentialInformation vci = new VerifyCredentialInformation("MijnOverheid",
 				"ageLower", "MijnOverheid", "ageLowerNone");
 		IdemixVerifySpecification vspec = vci.getIdemixVerifySpecification();
 
 		CardService cs = TestSetup.getCardService();
 		IdemixCredentials ic = new IdemixCredentials(cs);
 
 		Attributes attr = ic.verify(vspec);
 		
 		if (attr == null) {
 			fail("The proof does not verify");
 		} else {
 			System.out.println("Proof verified");
 		}
 		
 		attr.print();
 	}
 	
 	@Test
 	public void removeAgeCredential() throws CardException, CredentialsException, CardServiceException {
 		IssueCredentialInformation ici = new IssueCredentialInformation("MijnOverheid", "ageLower");
 		IdemixIssueSpecification spec = ici.getIdemixIssueSpecification();
 		
 		IdemixService cs = TestSetup.getIdemixService();
 		cs.open();
 		cs.selectApplet();
 		cs.sendPin(TestSetup.DEFAULT_PIN);
 		cs.transmit(new CommandAPDU(0x80, 0x30, 0x00, spec.getIdemixId()));
 		cs.transmit(new CommandAPDU(0x80, 0x31, 0x00, spec.getIdemixId()));
 	}
 
     private Attributes getStudentCardAttributes() {
         // Return the attributes that have been revealed during the proof
         Attributes attributes = new Attributes();
         
         System.out.println("Data: " + "Radboud University".getBytes().toString() + " Length: " + "Radboud University".getBytes().length);
 
 		attributes.add("university", "Radboud University".getBytes());
 		attributes.add("studentCardNumber", "0812345673".getBytes());
 		attributes.add("studentID", "s1234567".getBytes());
 		attributes.add("level", "PhD".getBytes());
 		
 		return attributes;
 	}
 
     private Attributes getSurfnetAttributes() {
         // Return the attributes that have been revealed during the proof
         Attributes attributes = new Attributes();
 
 		attributes.add("userID", "s123456@ru.nl".getBytes());
 		attributes.add("securityHash", "DEADBEEF".getBytes());
 		
 		return attributes;
 	}
     
     private Attributes getAgeAttributes () {
         Attributes attributes = new Attributes();
 
 		attributes.add("over12", "yes".getBytes());
 		attributes.add("over16", "yes".getBytes());
 		attributes.add("over18", "no".getBytes());
 		attributes.add("over21", "no".getBytes());
 		
 		return attributes;
     }
     
 }
