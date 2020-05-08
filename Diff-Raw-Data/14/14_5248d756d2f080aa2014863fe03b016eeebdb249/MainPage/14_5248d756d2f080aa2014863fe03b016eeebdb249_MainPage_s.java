 package servlets;
 
 import html.HTMLWriter;
 import items.Command;
 import items.Role;
 import items.User;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Denna klassen bygger ut ServletBase och renderar huvudsidan, via HTMLWriter,
  * som användare hamnar på efter inloggning. På sidan kan man välja vilken
  * projektgrupp man ska använda, samt tar sig vidare till funktionalitet, tex
  * skapa en ny tidrapport, administrera över användare eller visa statistik.
  */
 @WebServlet("/mainpage")
 public class MainPage extends ServletBase {
 
 	@Override
 	protected void doWork(HttpServletRequest request, HTMLWriter html) {
 		HttpSession session = request.getSession();
 		Statistics.done=true;
 
 		User user = (User) session.getAttribute(USER);
 		List<String> projects = database.getProjects(user);
 		
		String currentProjectGroup = (String) session.getAttribute(PROJECT);
		
		if (currentProjectGroup == null) {
			currentProjectGroup = projects.get(0);					
			session.setAttribute(PROJECT, projects.get(0));	
		}

		if (projects.size() > 0) {
 			
 			html.printProjectChooser(projects.indexOf(currentProjectGroup), projects);
 			Role role = database.getRole(user.getUsername(), currentProjectGroup);
 			
 			html.printLink("timereport?"+HTMLWriter.LIST_COMMAND+"="+Command.create.toString(), "Skapa en ny tidrapport");
 			html.printLink("listreports?"+HTMLWriter.LIST_COMMAND + "=" + Command.update.toString(), "Uppdatera en tidrapport");
 			html.printLink("listreports?"+HTMLWriter.LIST_COMMAND + "=" + Command.show.toString(), "Visa tidrapporter");
 			html.printLink("listreports?"+HTMLWriter.LIST_COMMAND + "=" + Command.delete.toString(), "Ta bort tidrapporter");
 			if (Role.Manager.equals(role)){
 				html.printLink("listreports?"+HTMLWriter.LIST_COMMAND + "=" + Command.sign.toString(), "Signera tidrapporter");
 			}
 			html.printLink("showmembers", "Visa medlemmar");
 			html.printLink("statistics", "Statistik");
 		} else {
 			html.printErrorMessage("Du är inte medlem i någon grupp");
 		}
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws IOException {
 		HttpSession session = request.getSession();
 
 		String chosen = (String) request.getParameter(HTMLWriter.PROJECT_CHOOSER);
 
 		System.out.println("chosen " + chosen);
 		if (chosen != null) {
 			session.setAttribute(PROJECT, chosen);
 		}
 
 		doGet(request, response);
 	}
 }
