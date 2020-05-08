 package cse405.message;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @SuppressWarnings("serial")
 public class GetMessageListServlet extends HttpServlet {
 
 	private static void writeMessage(PrintWriter writer, Message message) {
 		String text = message.getText()
			.replaceAll("\\\\", "\\\\\\\\")
 			.replaceAll("\"", "\\\\\"")
 			.replaceAll("/", "\\\\/")
 			.replaceAll("\b", "\\\\b")
 			.replaceAll("\f", "\\\\f")
 			.replaceAll("\n", "\\\\n")
 			.replaceAll("\r", "\\\\r")
 			.replaceAll("\t", "\\\\t");		
 		writer.print("{ \"nickname\": \"");
 		writer.print(message.getNickname());
 		writer.print("\", \"text\": \"");		
 		writer.print(text);
 		writer.print("\" }");
 	}
 	
 	private static void writeMessageList(PrintWriter writer, List<Message> messageList) {
 		Iterator<Message> messageIter = messageList.iterator();
 
 		writer.println("[");
 
 		if (messageIter.hasNext()) {
 			writeMessage(writer, messageIter.next());
 		}
 		while (messageIter.hasNext()) {
 			writer.print(", ");
 			writeMessage(writer, messageIter.next());
 		}
 		writer.println("]");		
 	}
 	
 	public static void sendMessageList(HttpServletRequest req, HttpServletResponse resp) 
 	throws IOException {
 		resp.setContentType("text/json");
 		PrintWriter writer = resp.getWriter();
 		List<Message> messageList = Message.getAll();
 		writeMessageList(writer, messageList);		
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		sendMessageList(req, resp);
 	}
 }
