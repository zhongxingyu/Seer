 package org.smartsnip.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.smartsnip.core.Snippet;
 import org.smartsnip.shared.XSnippet;
 
 import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
 
 /**
  * SnippetOfDay servlet. This provides a servlet, that lets a (mobile) client
  * access a snippet of the day
  * 
  * 
  */
 @RemoteServiceRelativePath("snippetofday")
 public class SnippetOfDay extends HttpServlet {
 
 	/** Serialisation ID */
 	private static final long serialVersionUID = 7786134724230388825L;
 
 	@SuppressWarnings("unused")
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
 
 		PrintWriter writer = null;
 		try {
 			writer = resp.getWriter();
 
 			writer.println("<head>");
 			writer.println("<title>Smartsnip.org - Snippet of the day</title>");
 			writer.println("</head>");
 
 			writer.println("<body>");
 
 			// Get the snippet of the day
 			Snippet snippetOfday = Snippet.getSnippetOfDay();
 			if (snippetOfday == null) {
 				// Internal server error
 				writer.println("<p><b>Error 500</b></p>");
 				writer.println("<p>The server has faced an internal server error</p>");
 				writer.println("<p>Unfortunately today there is no snippet of the day :-(</p>");
 				writer.println("<hr>");
 				writer.println("<p>We're working on this problem. Please try again later.</p>");
 			} else {
 				XSnippet result = snippetOfday.toXSnippet();
 
 				// Print Add meta data. Every item has to be added
 				// as a comment for a mobile client and as HTML page item
 				writer.println("<!--TITLE=" + result.title + "-->");
 				writer.println("<p><b>" + result.title + "</b><p>");
 
 				writer.println("<!--OWNER=" + result.owner + "-->");
 				writer.println("<p>Creator: " + result.owner + "<p>");
 
 				writer.println("<!--LANG=" + result.language + "-->");
 				writer.println("<p>Language: " + result.language + "<p>");
 
				writer.println("<!--RATING=" + result.rating + "-->");
				writer.println("<p>Rating: " + result.rating + "<p>");

 				writer.println("<!--CATEGORY=" + result.category + "-->");
 				writer.println("<p>Category: " + result.category + "<p>");
 
 				writer.println("<!--LICENSE=" + result.license + "-->");
 				writer.println("<p>License: " + result.license + "<p>");
 
 				writer.println("<!--DESC=" + result.description + "-->");
 				writer.println("<hr>");
 				writer.println("<p>" + result.description + "<p>");
 				writer.println("<hr>");
 
 				writer.println("<!--CODE=" + result.code + "-->");
 				writer.println("<p>" + result.codeHTML + "<p>");
 
 				// Check for source code
 				String downloadLink = SourceDownloader.getSnippetOfDaySourceURL();
 				if (downloadLink != null) {
 					writer.println("<!--LINK=" + result.code + "-->");
 				}
 
 				writer.println("<hr>");
 			}
 
 			writer.println("</body>");
 		} catch (IOException e) {
 			// Ignore exception
 			writer = null;
 			return;
 		} catch (Exception e) {
 			writer.println("<head>");
 			writer.println("<title>org.smartsnip - Code downloader</title>");
 			writer.println("</head>");
 
 			writer.println("<body>");
 			writer.println("<h1>500 - Server error</h1>");
 			writer.println("<p>There was an unhandled exception on the server:</p>");
 			writer.println("<p>" + e.getClass().getName() + ": <b>" + e.getMessage() + "</b></p>");
 			e.printStackTrace(writer);
 			writer.println("</body>");
 			resp.setStatus(500);
 		} finally {
 			if (writer != null) {
 				writer.flush();
 				writer.close();
 			}
 		}
 	}
 
 }
