 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Iterator;
 
 
 import org.icenigrid.gridsam.client.common.ClientSideJobManager;
 
 import org.icenigrid.gridsam.core.JobInstance;
 import org.icenigrid.gridsam.core.JobManagerException;
 import org.icenigrid.gridsam.core.JobStage;
 import org.icenigrid.gridsam.core.SubmissionException;
 import org.icenigrid.gridsam.core.UnknownJobException;
 import org.icenigrid.gridsam.core.UnsupportedFeatureException;
 import org.icenigrid.schema.jsdl.y2005.m11.JobDefinitionDocument;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Text;
 import org.w3c.dom.ls.DOMImplementationLS;
 import org.w3c.dom.ls.LSSerializer;
 import org.xml.sax.SAXException;
 
 import org.apache.xmlbeans.XmlException; 
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import static org.joox.JOOX.$;
 
 public class GridSAMExample {
 
 	private static String ftpServer = System.getProperty("ftp.server");
 	private static String gridsamServer = System.getProperty("gridsam.server");
 
 	public static void main(String[] args)
 		throws JobManagerException, SubmissionException, UnsupportedFeatureException, UnknownJobException,
 			IOException, XmlException, InterruptedException {
 		
 		System.out.println("Creating a new client Job Manager...");
 		ClientSideJobManager jobManager = new ClientSideJobManager(
 			new String[] { "-s", gridsamServer },
 			ClientSideJobManager.getStandardOptions());
 
 		System.out.println("Creating JSDL description...");
 		String xJSDLString  = createJSDLDescription("/usr/bin/sort", "/etc/hosts");
 		
 
 		System.out.println(xJSDLString);
 
 		JobDefinitionDocument xJSDLDocument =
 			JobDefinitionDocument.Factory.parse(xJSDLString);
 
 		System.out.println("Submitting job to Job Manager...");
 		JobInstance job = jobManager.submitJob(xJSDLDocument);
 		String jobID = job.getID();
 
 		// Get and report the status of job until complete
 		System.out.println("Job ID: " + jobID);
 
 		int lastPrintedJobStageIndex = 0;
 		while (true){
			JobInstance job = jobManager.findJobInstance(jobID)

			Iterator<JobStage> it = job.getJobStages().listIterator(lastPrintedJobStageIndex);
			while(it.hasNext()){
				System.out.printLn(it.next());
				lastPrintedJobStageIndex++;
 			}
 
			if (job.getLastKnownStage().getState().isTerminal()){
 				break;
 			}
 		}



 	}
 
 	private static String createJSDLDescription(String execName, String args) {
 		String s_jsdl = "";
 		try {
 			InputStream in = GridSAMExample.class.getResourceAsStream("posix.jsdl");
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			DocumentBuilder db;
 			db = dbf.newDocumentBuilder();
 			Document jsdl = db.parse(in);
 			
 			$(jsdl).find("#execName").text(execName).removeAttr("id");
 			$(jsdl).find("#args").text(args).removeAttr("id");
 
 		    DOMImplementationLS domImplementation = (DOMImplementationLS) jsdl.getImplementation();
 		    LSSerializer lsSerializer = domImplementation.createLSSerializer();
 		    s_jsdl = lsSerializer.writeToString(jsdl); 
 
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SAXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return s_jsdl;
 	}
 }
