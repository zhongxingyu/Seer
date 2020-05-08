 package org.morningcoffee.powerteam.plugin;
 
 import java.io.OutputStreamWriter;
 import java.net.ConnectException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.io.File;
 import java.util.Date;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.NodeList;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugins.annotations.Mojo;
 
 
 @Mojo(name = "report")
 public class PostTest extends AbstractMojo
 {
     public void execute() throws MojoExecutionException
     {
     	File testResultDir = new File(System.getProperty("user.dir") + "/target/surefire-reports/");
     	File[] listOfFiles = testResultDir.listFiles();
     	
     	String testResult = "success"; 
     	for (int i = 0; i < listOfFiles.length; i++) 
     	{
     		File testFile = listOfFiles[i];
     		
     		if (testFile.isFile() && testFile.getName().endsWith(".xml")) 
     		{
 		        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 		        Document doc = null;
 		        
 				try {
 					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 					doc = dBuilder.parse(testFile);
 				} 
 				catch (Exception e) { 
 					System.out.println("\nParsing test result error\n");
 		    		System.exit(-2);
 				}
 		        
 				doc.getDocumentElement().normalize();
 				NodeList nodes = doc.getElementsByTagName("testsuite");
 		        
 				for (int j = 0; j < nodes.getLength(); j++)
 				{
 					NamedNodeMap testAttributes = nodes.item(j).getAttributes();
 					String testsFailures = testAttributes.getNamedItem("failures").getNodeValue();
 					
					if(testsFailures != "0")
 						testResult = "failed";
 				}
 				
 				testFile.delete();
     		}
     	}
     	
     	long timeStamp = new Date().getTime();
 		RequestGen rg = new RequestGen(PreTest.time, timeStamp, testResult, GitInfo.getName());
 		String data = "data=" + rg.getJSON();	
    	
     	try {
 	        URL url = new URL("http://localhost:8080");
 	        URLConnection conn = url.openConnection();
 	        conn.setDoOutput(true);
 	        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
 	        wr.write(data);
 	        wr.flush();
 	        conn.getInputStream();
 	        wr.close();    
 		}
     	catch (ConnectException e) { 
     		System.out.println("\nCannot connect to the server\n");
     		System.exit(-1);
     	}
     	catch (Exception e) { e.printStackTrace(); }
     }
 }
