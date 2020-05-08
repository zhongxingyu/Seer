 package it.xpug.tai.lesson09.chat;
 
 import java.io.IOException;
 import java.util.List;
 
 import static java.lang.String.format;
 
 import it.xpug.tai.lesson09.chat.jetty.TaiController;
 import it.xpug.tai.lesson09.chat.jetty.TaiRequest;
 import it.xpug.tai.lesson09.chat.jetty.TaiResponse;
 
 public class RoomsPage implements TaiController {
 
 	private final List<Room> rooms;
 
 	public RoomsPage(List<Room> rooms) {
 		this.rooms = rooms;
 	}
 
 	@Override
 	public boolean wantsToHandle(String target) {
 		return "/rooms".equals(target);
 	}
 
 	@Override
 	public void handle(TaiRequest request, TaiResponse taiResponse) throws IOException {
		String html = "<ul>";
 		for (Room room : rooms) {
			html += format("<li><a id=\"%d\" href=\"#\">%s</a></li>", room.get("id"), room.get("description"));
 		}
		html += "</ul>";
		taiResponse.setContentType("text/html");
 		taiResponse.copyThisTextToOutput(html);
 	}
 
 }
