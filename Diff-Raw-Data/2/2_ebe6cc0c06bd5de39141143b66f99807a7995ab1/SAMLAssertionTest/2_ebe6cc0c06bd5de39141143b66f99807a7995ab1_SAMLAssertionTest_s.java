 /**
  * 
  * Copyright 2002 NCHELP
  * 
  * Author:		Tim Bornholtz, Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified 
  * by the National Council of Higher Education Loan Programs, Inc. 
  * (NCHELP) and the Meteor Sponsors, and developed by Priority 
  * Technologies, Inc. (PTI). 
  *
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *	
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *	
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  ********************************************************************************/
 
 package test.nchelp.meteor;
 
 import java.net.UnknownHostException;
 import java.util.Date;
 
 import junit.framework.TestCase;
 import junit.textui.TestRunner;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.security.saml.Assertion;
 import org.nchelp.meteor.security.saml.AssertionFactory;
 import org.nchelp.meteor.security.saml.AttributeStatement;
 import org.nchelp.meteor.security.saml.NameIdentifier;
 import org.nchelp.meteor.security.saml.SAMLUtil;
 import org.nchelp.meteor.security.saml.Subject;
 import org.nchelp.meteor.util.XMLParser;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class SAMLAssertionTest extends TestCase {
 
 	private final Log log = LogFactory.getLog(this.getClass());
 	
 	public SAMLAssertionTest(String name) {
 		super(name);
 	}
 
 	public static void main(String args[]) {
 		TestRunner.run(SAMLAssertionTest.class);
 	}
 
 	public void testSaml() {
 		String userid = "ED.303";
 	
 		try {
 			Assertion aa = AssertionFactory.newInstance(
				new String( new Long(System.currentTimeMillis()).toString() ),
 				"nchelp.org/meteor",
 				new Date(),
 				userid,
 				"nchelp.org/meteor",
 				"http://nchelp.org",
 				new Date(),
 				java.net.InetAddress.getLocalHost().getHostAddress(),
 				java.net.InetAddress.getLocalHost().getHostName()
 				);
 
 			NameIdentifier ni = new NameIdentifier();
 			ni.setSecurityDomain("nchelp.org/meteor");
 			ni.setName(userid);
 			
 			Subject subject = new Subject();
 			subject.setNameIdentifier(ni);
 
 			AttributeStatement as = new AttributeStatement();
 			as.setSubject(subject);
 			
 			as.addAttribute("Role", "nchelp.org/meteor", SecurityToken.roleFAA);
 				
 			aa.addStatement(as);
 			
 			aa.setAttributeValue("Level", "nchelp.org/meteor", "3");
 			
 			Document doc = SAMLUtil.newDocument();
 			Element e = doc.createElement("AssertionSpecifier");
 			doc.appendChild(e);
 			
 			aa.serialize(e);
 			
 			System.out.println(XMLParser.xmlToString(doc));
 
 		} catch(UnknownHostException e) {
 			e.printStackTrace();
 			fail();
 		}
 			
 	}
 	
 	public void testSamlDeserialization(){
 		String saml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><AssertionSpecifier>   <saml:Assertion AssertionID=\"1017033169855\" IssueInstant=\"2002-03-24T11:12:49CST\" Issuer=\"nchelp.org/meteor\"      MajorVersion=\"1\" MinorVersion=\"0\" xmlns:saml=\"http://www.oasis-open.org/committees/security/docs/draft-sstc-schema-assertion-27.xsd\">      <saml:AuthenticationStatement         AuthenticationInstant=\"2002-03-24T11:12:49CST\" AuthenticationMethod=\"http://nchelp.org\">         <saml:Subject>            <saml:NameIdentifier Name=\"ED.303\" SecurityDomain=\"nchelp.org/meteor\"/>         </saml:Subject>         <saml:AuthenticationLocality DNSAddress=\"GERTRUDE\" IPAddress=\"127.0.0.1\"/>      </saml:AuthenticationStatement>      <saml:AttributeStatement>         <saml:Subject>            <saml:NameIdentifier Name=\"ED.303\" SecurityDomain=\"nchelp.org/meteor\"/>         </saml:Subject>         <saml:Attribute AttributeName=\"Role\" AttributeNamespace=\"nchelp.org/meteor\">            <saml:AttributeValue>FAA</saml:AttributeValue>         </saml:Attribute>      </saml:AttributeStatement>   </saml:Assertion></AssertionSpecifier>";
 		
 		Assertion a = new Assertion();
 		
 		try {
 			Document doc1 = XMLParser.parseXML(saml);
 			
 			Element rootNode = doc1.getDocumentElement();
 			NodeList nl = rootNode.getChildNodes();
 			
 			for(int n = 0; n < nl.getLength(); n++){
 				Node node = nl.item(n);
 				if(node.getNodeType() == Node.ELEMENT_NODE){
 					if("Assertion".equals(node.getLocalName())){
 						a.deserialize((Element)node);
 					}
 				}
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 		
 		System.out.println("Role: " + a.getAttributeValue("Role", "nchelp.org/meteor"));
 		
 		
 		Document doc2 = SAMLUtil.newDocument();
 		Element e = doc2.createElement("AssertionSpecifier");
 		doc2.appendChild(e);
 		
 		a.serialize(e);
 			
 		System.out.println(XMLParser.xmlToString(doc2));
 		
 	}
 	
 
 }
 
 
 
 
 
 
