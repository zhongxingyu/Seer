 package lobos.andrew.aztec.plugin;
 
 import java.io.File;
 import java.io.FileReader;
 
 import javax.xml.bind.DatatypeConverter;
 
 import lobos.andrew.aztec.Config;
 import lobos.andrew.aztec.http.ErrorFactory;
 import lobos.andrew.aztec.http.HTTPRequest;
 import lobos.andrew.aztec.http.HTTPResponse;
 
 public class VirtualHost extends Plugin {
 
 	private String docRoot;
 	public VirtualHost(String hostRegex, String docRoot)
 	{
 		this(docRoot);
 		HOST = hostRegex;
 	}
 	
 	public VirtualHost(String docRoot)
 	{
 		PATH = ".+";
 		this.docRoot = docRoot;
 	}
 	
 	@Override
	public HTTPResponse handle(HTTPRequest req) 
	{
 		String HTTPAuthUsername = Config.getString(HOST, "AuthUsername", "");
 		if ( !HTTPAuthUsername.equals("") )
 		{
 			String responseAuthHeader = req.getHeader("Authorization");
 			if ( responseAuthHeader.equals("") )
 			{
 				HTTPResponse response = ErrorFactory.notAuthorized();
 				response.addResponseHeader("WWW-Authenticate", "Basic realm=\""+Config.getString(HOST, "AuthRealm", "Secure Site")+"\"");
 				return response;
 			}
 			else
 			{
 				String authString = responseAuthHeader.split(" ")[0];
 				String correctString = HTTPAuthUsername+":"+Config.getString(HOST, "AuthPassword", "");
 				correctString = DatatypeConverter.printBase64Binary(correctString.getBytes());
 				if ( authString.equals(correctString) )
 					return ErrorFactory.notAuthorized();
 			}
 		}
 		
 		String path = req.getPath();
 		if ( path.substring(path.length()-1, path.length()).equals("/") )
 			path += "index.html";
 		File f = new File(docRoot+"/"+path);
 		
 		if ( f.canExecute() && f.getName().split("\\.")[1].equals("cgi") )
 			return new CGIExecute(f.getAbsolutePath()).handle(req);
 		else if ( f.exists() )
 		{
 			try {
 				@SuppressWarnings("resource")
 				FileReader reader = new FileReader(f);
 				String fileData = "";
 				while ( reader.ready() )
 				{
 					char[] data = new char[50];
 					reader.read(data, 0, data.length);
 					fileData += new String(data);
 				}
 				return new HTTPResponse(200, fileData);
 			} catch (Exception e) {
 				return ErrorFactory.internalServerError();
 			}
 		}
 		return ErrorFactory.notFound();
 	}
 
 }
