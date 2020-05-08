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
 import java.util.StringTokenizer;
import java.util.HashMap;
 
 import javax.xml.rpc.ServiceException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 
 import org.apache.xml.serialize.OutputFormat;
 import org.apache.xml.serialize.XMLSerializer;
 
 import fedora.client.APIMStubFactory;
 import fedora.client.APIAStubFactory;
 import fedora.server.management.FedoraAPIM;
 import fedora.server.access.FedoraAPIA;
 import fedora.server.utilities.StreamUtility;
 import fedora.server.types.gen.RepositoryInfo;
 
 /**
  *
  * <p><b>Title:</b> AutoExporter.java</p>
  * <p><b>Description: Utility class to make API-M export calls to a repository.</b> </p>
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
  * <p>The entire file consists of original code.  Copyright &copy; 2002-2004 by The
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
 	private FedoraAPIA m_apia;
    private static HashMap s_repoInfo=new HashMap();
 
     public AutoExporter(String host, int port, String user, String pass)
             throws MalformedURLException, ServiceException {
 		m_apia=APIAStubFactory.getStub(host, port, user, pass);
         m_apim=APIMStubFactory.getStub(host, port, user, pass);
     }
     
 	public void export(String pid, String format, 
 			OutputStream outStream, boolean internal) 
 			throws RemoteException, IOException {
 		export(m_apia, m_apim, pid, format, outStream, internal);
 	}

 	
     public static void export(FedoraAPIA apia, FedoraAPIM apim, String pid, String format,
             OutputStream outStream, boolean internal)
             throws RemoteException, IOException {

        // Get the repository info from the hash for the repository that APIA is using,
        // unless it isn't in the hash yet... in which case, ask the server.
		RepositoryInfo repoinfo=(RepositoryInfo) s_repoInfo.get(apia);
        if (repoinfo == null) {
            repoinfo=apia.describeRepository();
            s_repoInfo.put(apia, repoinfo);
        }
 				
         byte[] bytes;
         if (internal) {
             bytes=apim.getObjectXML(pid);
         } else {
         	// For backward compatibility:
         	// For pre-2.0 repositories, use the "exportObject" APIM method. 
         	// Also pre-2.0 repositories will only export "metslikefedora1".
         	// For 2.0+ repositories use the "export" method which takes a format arg.
 			StringTokenizer stoken = new StringTokenizer(repoinfo.getRepositoryVersion(), ".");
 			if (new Integer(stoken.nextToken()).intValue() < 2){
 				if (format==null ||
 					format.equals("metslikefedora1") ||
 					format.equals("default")) {
 					bytes=apim.exportObject(pid);					
 				} else {
 					throw new IOException("You are connected to a pre-2.0 Fedora repository " +
 						"which will only export the format \"metslikefedora1\".");
 				}
 			} else {
 				validateFormat(format);				
 				bytes=apim.export(pid, format);
 			}
         }
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
         } finally
         {
           outStream.close();
         }
     }
 
 	public static void validateFormat(String format)
 		throws IOException {
 			if (!format.equals("foxml1.0") && !format.equals("metslikefedora1")) {
 				throw new IOException("Invalid export format. Valid FORMAT values are: " +
 					"'foxml1.0' or 'metslikefedora1'");
 			}
 		}
 
 /*
     public static void showUsage(String errMessage) {
         System.out.println("Error: " + errMessage);
         System.out.println("");
         System.out.println("Usage: AutoExporter host port username password filename pid format");
     }
 
     public static void main(String[] args) {
         try {
             if (args.length!=7) {
                 AutoExporter.showUsage("You must provide seven arguments.");
             } else {
                 String hostName=args[0];
                 int portNum=Integer.parseInt(args[1]);
                 String username=args[2];
                 String password=args[3];
                 String pid=args[5];
                 String format=args[6];
                 // third arg==file... must exist
                 File f=new File(args[4]);
                 if (f.exists()) {
                     AutoExporter.showUsage("Third argument must be the path to a non-existing file.");
                 } else {
                     AutoExporter a=new AutoExporter(hostName, portNum, username, password);
                     a.export(pid, format, new FileOutputStream(f), false);
                 }
             }
         } catch (Exception e) {
             AutoExporter.showUsage(e.getClass().getName() + " - "
                 + (e.getMessage()==null ? "(no detail provided)" : e.getMessage()));
         }
     }
 */
}
