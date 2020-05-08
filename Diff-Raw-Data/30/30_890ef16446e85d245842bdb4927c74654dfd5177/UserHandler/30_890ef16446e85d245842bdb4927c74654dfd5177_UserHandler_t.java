 package at.tomtasche.crxify;
 
import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.UUID;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.handler.DefaultHandler;
 
 public class UserHandler extends DefaultHandler {
 
 	@Override
 	public void handle(String target, Request baseRequest,
 			HttpServletRequest request, HttpServletResponse response)
 					throws IOException, ServletException {
 		if (!request.getMethod().equals("GET") || request.getRequestURI().endsWith(".crx")) return;
 
 		if (!request.getRequestURI().endsWith("pack") || !request.getParameterMap().containsKey("url")) {
 			serveHtml("index.html", response);
 		} else {
 			String url = request.getParameter("url");
 			String id = UUID.randomUUID().toString();
 
 			try {
 				Process process = Runtime.getRuntime().exec("wget -O " + id + ".zip " + url);
 
 				File file = new File(id);
 				file.mkdir();
 
 				try {
 					process.waitFor();
 				} catch (InterruptedException e) {}
 
 				process = Runtime.getRuntime().exec("unzip " + id + ".zip -d " + id);
 
 				try {
 					process.waitFor();
 				} catch (InterruptedException e) {}
 
 				file = new File(id + ".zip");
 				file.delete();
 
 				file = new File(id);
 
				process = Runtime.getRuntime().exec("./crxi " + file.listFiles()[0] + " pem.pem " + id);
 
 				try {
 					process.waitFor();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 				file = new File(id);
 				deleteDir(file);
 
				String html = getHtml("download.html").replace("{id}", id);
				
				response.getWriter().write(html);
				response.getWriter().flush();
				
 				//			response.sendRedirect(id + ".crx");
 			} catch (IOException e) {
 				e.printStackTrace();
 
 				throw new IOException("made a big boo-boo :(", e);
 			}
 		}
 	}
 	
 	// taken from: http://www.javapractices.com/topic/TopicAction.do?Id=232
 	public static void serveHtml(String file, HttpServletResponse response) throws IOException {
		InputStream input = UserHandler.class.getResourceAsStream(file);
 		OutputStream output = response.getOutputStream();
 
 		byte[] buffer = new byte[2048];
 		int bytesRead;    
 		while ((bytesRead = input.read(buffer)) != -1) {
 			output.write(buffer, 0, bytesRead);
 		}
 		
 		output.flush();
 	}
	
	public static String getHtml(String file) throws IOException {
		InputStreamReader reader = new InputStreamReader(UserHandler.class.getResourceAsStream(file));
		BufferedReader bufferedReader = new BufferedReader(reader);
		
		StringBuilder builder = new StringBuilder();
		for (String s = bufferedReader.readLine(); s != null; s = bufferedReader.readLine()) {
			builder.append(s);
		}
		
		return builder.toString();
	}
 
 	// taken from: http://www.exampledepot.com/egs/java.io/DeleteDir.html
 	// Deletes all files and subdirectories under dir.
 	// Returns true if all deletions were successful.
 	// If a deletion fails, the method stops attempting to delete and returns false.
 	public static boolean deleteDir(File dir) {
 		if (dir.isDirectory()) {
 			String[] children = dir.list();
 			for (int i=0; i<children.length; i++) {
 				boolean success = deleteDir(new File(dir, children[i]));
 				if (!success) {
 					return false;
 				}
 			}
 		}
 
 		// The directory is now empty so delete it
 		return dir.delete();
 	}
 }
