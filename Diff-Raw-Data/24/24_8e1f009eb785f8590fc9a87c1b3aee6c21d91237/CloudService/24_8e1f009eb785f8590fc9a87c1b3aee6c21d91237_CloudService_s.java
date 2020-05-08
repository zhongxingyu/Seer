 package icloude;
 
 import icloude.contents.FileContent;
 import icloude.request_handlers.DownloadCodeRequestHandler;
 import icloude.request_handlers.NewFileRequestHandler;
 import icloude.request_handlers.NewProjectRequestHandler;
 import icloude.request_handlers.UploadFileRequestHandler;
 import icloude.requests.DownloadCodeRequest;
 import icloude.requests.NewFileRequest;
 import icloude.requests.NewProjectRequest;
 import icloude.requests.UploadFileRequest;
 import icloude.responses.IDResponse;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Date;
 
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import storage.DatabaseException;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 
 @Path("/api/info")
 public class CloudService {
 
 	Gson gson = new Gson();
 
 	private class SimpleMessage {
 		public String text;
 
 		public SimpleMessage(String inpWord) {
 			text = inpWord;
 		}
 	}
 
 	@GET
	@Produces("application/x-zip-compressed")
	public InputStream getInfoJSON() throws IOException, DatabaseException {
 		String json;
 		
 		NewProjectRequestHandler nprh = new NewProjectRequestHandler();
 		NewFileRequestHandler nfrh = new NewFileRequestHandler();
 		UploadFileRequestHandler ufrh = new UploadFileRequestHandler();
		DownloadCodeRequestHandler dcrh = new DownloadCodeRequestHandler();
 		
 		NewProjectRequest npr = new NewProjectRequest(2, "NewProjectRequest", "newproject", "userIDZIP", "projectZIP", "typeZIP");
 		json = nprh.post(gson.toJson(npr));
 		IDResponse idrProj = gson.fromJson(json, IDResponse.class);
 		NewFileRequest nfr = new NewFileRequest(2, "NewFileRequest", "newfile", "userIDZIP", idrProj.getProjectID(), idrProj.getEntityID(), "fileZIP", "typeZIP");
 		json = nfrh.post(gson.toJson(nfr));
 		IDResponse idrFile = gson.fromJson(json, IDResponse.class);
 		FileContent content = new FileContent("file", idrFile.getEntityID(), "Hello, I am text of this file!!!", "textFile", "userIDZIP", "ZIPRevision", (new Date()).getTime(), (new Date()).getTime());
 		UploadFileRequest ufr = new UploadFileRequest(2, "UploadFileRequest", "uploadfile", "userIDZIP", idrProj.getProjectID(), content);
 		json = ufrh.post(gson.toJson(ufr));
		DownloadCodeRequest dcr = new DownloadCodeRequest(2,"DownloadCodeRequest", "downloadcode", "UserIDZIP", idrProj.getProjectID());
 		
		return dcrh.get(gson.toJson(dcr));
 	} 
 
 	@POST
 	@Produces(MediaType.APPLICATION_JSON)
 	// @Consumes(MediaType.TEXT_PLAIN)
 	public String postInfoJSON(@FormParam("json") String inpJSON) {
 		System.err.println("post");
 		SimpleMessage msg;
 		if (inpJSON == null) {
 			msg = new SimpleMessage("No 'json' parameter.");
 		} else {
 			try {
 				SimpleMessage fromJSON = gson.fromJson(inpJSON,
 						SimpleMessage.class);
 				msg = new SimpleMessage(
 						"Recieved 'json' parameter containing:'" + inpJSON
 								+ "'. After encoding JSON got: '"
 								+ fromJSON.text + "'");
 			} catch (JsonSyntaxException e) {
 				msg = new SimpleMessage(
 						"Recieved 'json' parameter containing:'" + inpJSON
 								+ "'. But can't decode JSON.");
 			}
 		}
 		return gson.toJson(msg);
 	}
 }
 
