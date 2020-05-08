 /*
 * $Id: PlainResultFormatter.java,v 1.5 2003-08-31 12:17:46 o_rossmueller Exp $
  *
  * (c) 2002 Oliver Rossmueller
  *
  * This file is part of tuxerra.
  *
  * tuxerra is free software; you can redistribute it and/or modify
  * it under the terms of version 2 of the GNU General Public License
  * as published by the Free Software Foundation.
  *
  * tuxerra is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with tuxerra; if not, mailto:oliver@oross.net or have a look at
  * http://www.gnu.org/licenses/licenses.html#GPL
  */
 package org.junitee.anttask;
 
 
 import java.io.*;
 import java.text.NumberFormat;
 import java.util.*;
 
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 
 /**
 * @version $Revision: 1.5 $
  * @author <a href="mailto:oliver@oross.net">Oliver Rossmueller</a>
  */
 public class PlainResultFormatter extends AbstractResultFormatter {
 
   public void format(Node testSuiteNode) throws IOException {
     NamedNodeMap attributes = testSuiteNode.getAttributes();
     String testName = getTestName(testSuiteNode);
     String runs = attributes.getNamedItem("tests").getNodeValue();
     int errors = Integer.parseInt(attributes.getNamedItem("errors").getNodeValue());
     int failures = Integer.parseInt(attributes.getNamedItem("failures").getNodeValue());
     String time = attributes.getNamedItem("time").getNodeValue();
 
     StringBuffer buffer = new StringBuffer("Testsuite: ");
     buffer.append(testName).append("\n");
     buffer.append("Tests run: ").append(runs).append("\n");
     buffer.append("Errors: ").append(errors).append("\n");
     buffer.append("Failures: ").append(failures).append("\n");
     buffer.append("Time elapsed: ").append(time).append(" sec\n\n");
 
     StringBuffer errorBuffer = new StringBuffer();
     StringBuffer failureBuffer = new StringBuffer();
 
     NodeList tests = testSuiteNode.getChildNodes();
 
     for (int i = 0; i < tests.getLength(); i++) {
       Node node = tests.item(i);
 
       if (node.getNodeName() != null && node.getNodeName().equals("testcase")) {
         NamedNodeMap testAttributes = node.getAttributes();
         String testMethod = testAttributes.getNamedItem("name").getNodeValue();
         NodeList errorFailureNodes = node.getChildNodes();
 
         for (int j = 0; j < errorFailureNodes.getLength(); j++) {
           Node errorFailure = errorFailureNodes.item(j);
           StringBuffer current = null;
 
           if (errorFailure.getNodeName() != null) {
             if (errorFailure.getNodeName().equals("error")) {
               current = errorBuffer;
             } else if (errorFailure.getNodeName().equals("failure")) {
               current = failureBuffer;
             }
             if (current != null) {
               current.append("Test case: ").append(testMethod).append("\n\n");
               errorFailure.normalize();
               current.append(errorFailure.getFirstChild().getNodeValue().trim()).append("\n---");
             }
           }
         }
       }
     }
     if (errors > 0) {
       buffer.append("-------------- Errors ----------------\n");
      buffer.append(errorBuffer.toString()); // for pre1.4 JRE
       buffer.append("----------- ------ ----------------\n");
       if (failures > 0) {
         buffer.append("\n");
       }
     }
 
     if (failures > 0) {
       buffer.append("------------- Failures ---------------\n");
      buffer.append(failureBuffer.toString()); // for pre1.4 JRE
       buffer.append("---------- -------- ---------------\n");
     }
 
     OutputStream out = getOutput(testName);
     if (out == null) {
       return;
     }
 
     out.write(buffer.toString().getBytes());
     out.flush();
     out.close();
   }
 }
