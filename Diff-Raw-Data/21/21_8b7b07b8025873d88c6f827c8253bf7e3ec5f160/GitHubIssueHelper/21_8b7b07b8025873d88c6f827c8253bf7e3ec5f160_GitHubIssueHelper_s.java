 package net.guto.jenkins.plugin;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLConnection;
 
 import net.sf.json.JSONObject;
 
 import org.apache.commons.codec.binary.Base64;
 
 public class GitHubIssueHelper {
 
 	public static void openIssue(Issue issue, String username, String password) throws IOException {
 		try {
 			String apiPage = "https://api.github.com/repos/" + issue.owner + "/" + issue.project + "/issues";
 
 			String authString = username + ":" + password;
 			byte[] authEncoded = Base64.encodeBase64(authString.getBytes());
 			URL url = new URL(apiPage);
 			URLConnection con = url.openConnection();
 
 			con.setRequestProperty("Authorization", "Basic " + new String(authEncoded));
 			con.setRequestProperty("Content-Type", "application/json");
 
 			con.setDoOutput(true);
 
 			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
 			wr.write(getJSON(issue).toString());
 			wr.flush();
 
 			InputStream is = con.getInputStream();
 			InputStreamReader isr = new InputStreamReader(is);
 
 			int numCharsRead;
 			char[] charArray = new char[1024];
 			StringBuffer sb = new StringBuffer();
 			while ((numCharsRead = isr.read(charArray)) > 0) {
 				sb.append(charArray, 0, numCharsRead);
 			}
 			// String result = sb.toString();
 			// System.out.println("*** BEGIN ***");
 			// System.out.println(result);
 			// System.out.println("*** END ***");
 		} catch (IOException e) {
			// TODO: threat the 401 Not authorized http status code
			// TODO: threat the 400 Bad request http status code
 			e.printStackTrace();
 		}
 	}
 
 	public static JSONObject getJSON(Issue issue) {
 		JSONObject json = new JSONObject();
 		json.element("title", issue.title);
 		json.element("body", issue.body);
 		json.element("assignee", issue.assignee);
 		return json;
 	}
 
 	public static void main(String[] args) throws IOException {
 		Issue issue = new Issue();
 		issue.owner = "gUTOnET";
 		issue.project = "test-git-issue";
 		issue.title = "teste do plugin do jenkins 2";
 		issue.body = "teste do plugin do jenkins epa 2";
 		issue.assignee = "gUTOnET";
 		openIssue(issue, args[0], args[1]);
 	}
 
 }
