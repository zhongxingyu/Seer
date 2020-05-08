 package lobos.andrew.aztec.plugin;
 
 import java.io.InputStreamReader;
 import java.util.Map;
 
 import lobos.andrew.aztec.http.ErrorFactory;
 import lobos.andrew.aztec.http.HTTPRequest;
 import lobos.andrew.aztec.http.HTTPResponse;
 
 public class CGIExecute extends Plugin {
 	ProcessBuilder cgiApp;
 	public CGIExecute(String binPath)
 	{	
 		cgiApp = new ProcessBuilder(binPath);
 	}
 
 	@Override
	public HTTPResponse handle(HTTPRequest req) 
	{
 		Map<String, String> env = cgiApp.environment();
 		env.put("PATH_TRANSLATED", req.getPath());
 		env.put("PATH_INFO", req.getPath());
 		env.put("REMOTE_ADDR", req.getClientIP());
 		env.put("QUERY_STRING", req.getQueryString());
 		String responseData = "";
 		try {
 			Process cgiProcess = cgiApp.start();
 			InputStreamReader reader = new InputStreamReader(cgiProcess.getInputStream());
 			
 			cgiProcess.waitFor();
 			
 			while ( reader.ready() )
 			{
 				char[] buffer = new char[50];
 				reader.read(buffer, 0, 50);
 				responseData += new String(buffer);
 			}
 			
 		} catch (Exception e) {
 			return ErrorFactory.internalServerError("CGI process failed to start.");
 		}
 		return new HTTPResponse(200, responseData);
 	}
 }
