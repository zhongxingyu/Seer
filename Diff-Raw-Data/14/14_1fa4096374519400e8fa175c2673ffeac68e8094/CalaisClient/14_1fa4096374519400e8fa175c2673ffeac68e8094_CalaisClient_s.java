 package client;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URLEncoder;
 
 import org.restlet.data.Form;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 
 public class CalaisClient {
 	
 	public static String TEST_TEXT = "This is a test, my name is Aitor Almeida and I'm in Bilbao, Spain. I'm writing a longer text because the previous one was too short. London, IBM";
 	
 	public String getAnalysis(String content) throws IOException{
 		Form form = new Form();
		form.add("licenseID", URLEncoder.encode(ConfigValues.KEY, "UTF-8"));
 		form.add("content", URLEncoder.encode(content, "UTF-8"));
 		//form.add("paramsXML",URLEncoder.encode(ConfigValues.getParameters(), "UTF-8"));
 		
		ClientResource resource = new ClientResource(ConfigValues.CALAIS_URL);
 
 		Representation r = resource.post(form.getWebRepresentation());

 		if (resource.getStatus().isSuccess()) {
 			System.out.println("Success: " + resource.getStatus().getCode());
 			if (resource.getStatus().getCode() == 200){
				String response = r.getText();
 				this.processResponse(response);
 			}
 			
 		} else {
 			System.out.println("Fail");
 		}
 		
		String response = r.getText();
 		return response;
 	}
 	
 	private void processResponse(String content){
 		System.out.println(content);
 		this.writeContentToFile(content);		
 	}
 	
 	private void writeContentToFile(String content){
 		String path = "./output/test/outputContent.rdf";
 		File file = new File(path);
 		if (file.exists()){
 			file.delete();
 		}
 		FileWriter outFile;
 		try {
 			outFile = new FileWriter(path);
 			PrintWriter out = new PrintWriter(outFile);
 			out.print(content);
 			out.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 }
