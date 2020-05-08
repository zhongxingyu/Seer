 package fedora.client.export;
 
 import java.io.ByteArrayOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.rmi.RemoteException;
 
 import javax.xml.rpc.ServiceException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 
 import org.apache.xml.serialize.OutputFormat;
 import org.apache.xml.serialize.XMLSerializer;
 
 import fedora.client.APIMStubFactory;
 import fedora.server.management.FedoraAPIM;
 import fedora.server.utilities.StreamUtility;
 
 /**
  *
  * <p><b>Title:</b> AutoExporter.java</p>
  * <p><b>Description:</b> </p>
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002, 2003 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public class AutoExporter {
 
     private FedoraAPIM m_apim;
 
     public AutoExporter(String host, int port, String user, String pass)
             throws MalformedURLException, ServiceException {
         m_apim=APIMStubFactory.getStub(host, port, user, pass);
     }
 
     public void export(String pid, OutputStream outStream) throws RemoteException, IOException {
         export(m_apim, pid, outStream);
     }
 
     public static void export(FedoraAPIM skeleton, String pid, OutputStream outStream)
             throws RemoteException, IOException {
         byte[] bytes=skeleton.getObjectXML(pid);
         try {
             // use xerces to pretty print the xml, assuming it's well formed
             OutputFormat fmt=new OutputFormat("XML", "UTF-8", true);
             fmt.setIndent(2);
             fmt.setLineWidth(120);
             fmt.setPreserveSpace(false);
             XMLSerializer ser=new XMLSerializer(outStream, fmt);
             DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
             factory.setNamespaceAware(true);
             DocumentBuilder builder=factory.newDocumentBuilder();
             Document doc=builder.parse(new ByteArrayInputStream(bytes));
             ser.serialize(doc);
         } catch (Exception e) {
             System.out.println("ERROR: " + e.getClass().getName() + " : " + e.getMessage());
         }
     }
 
     public static void showUsage(String errMessage) {
         System.out.println("Error: " + errMessage);
         System.out.println("");
         System.out.println("Usage: AutoExporter host port username password filename pid");
     }
 
     public static void main(String[] args) {
         try {
             if (args.length!=6) {
                 AutoExporter.showUsage("You must provide six arguments.");
             } else {
                 String hostName=args[0];
                 int portNum=Integer.parseInt(args[1]);
                 String username=args[2];
                 String password=args[3];
                 String pid=args[5];
                 // third arg==file... must exist
                 File f=new File(args[4]);
                 if (f.exists()) {
                     AutoExporter.showUsage("Third argument must be the path to a non-existing file.");
                 } else {
                     AutoExporter a=new AutoExporter(hostName, portNum, username, password);
                     a.export(pid, new FileOutputStream(f));
                 }
             }
         } catch (Exception e) {
             AutoExporter.showUsage(e.getClass().getName() + " - "
                 + (e.getMessage()==null ? "(no detail provided)" : e.getMessage()));
         }
     }
 
 }
