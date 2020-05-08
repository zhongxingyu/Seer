 
 package html;
 
 import items.Activity;
 import items.ActivityType;
 import items.Command;
 import items.GraphSettings;
 import items.Role;
 import items.TimeReport;
 import items.User;
 
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Denna klassen innehåller funktionalitet för att skriva ut all den HTML kod
  * som systemets olika delar behöver, t.ex. för listning av tidrapporter i en
  * tabell.
  * 
  */
 public class HTMLWriter {
 
 
 	final static public String LOGIN_USERNAME = "username";
 	final static public String LOGIN_PASSWORD = "password";
 	final static public String PROJECT_CHOOSER = "select";
 
 
 	private PrintWriter writer;
 
 	/**
 	 * HTMLs enda konstruktor. Tar emot en PrintWriter som allt kommer att
 	 * skrivas till.
 	 * 
 	 * @param writer
 	 *            dit allting kommer att skrivas.
 	 */
 	public HTMLWriter(PrintWriter writer) {
 		this.writer = writer;
 	}
 
 	/**
 	 * 
 	 Skriver ut en tidrapport. command är vilket commando som man vill utföra
 	 * och role är vilken roll som den inloggade användare har i projektet där
 	 * tidrapporten kommer ifrån.
 	 * 
 	 */
 	public void printTimeReport(TimeReport timereport, Command command,
 			Role role) {
 	}
 
 	/**
 	 * Skriver ut en lista med tidrapporter. Listan utökas med funktionalitet
 	 * för borttagning, uppdatering, signering, beroende på command och role.
 	 * 
 	 * @param timereports
 	 *            de tidrapporter som skall skrivas ut.
 	 * @param command
 	 *            det val man ska presentera för varje tidrapport.
 	 * @param role
 	 *            rollen för den användare som anropade metoden.
 	 * 
 	 * 
 	 */
 	public void printTimeReports(List<TimeReport> timereports, Command command,
 			Role role) {
 		writer.print("TEST");
 	}
 
 	/**
 	 * 
 	 Skriver ut en lista med användare. Listan utökas beroende på vilken typ
 	 * av användare det är.
 	 * 
 	 * @param users
 	 *            användarna som skall skrivas ut.
 	 * @param role
 	 *            rollen för den användare som anropade metoden.
 	 * 
 	 */
 	public void printUsers(List<User> users, Role role) {
 	}
 
 	/**
 	 * 
 	 Skriver ut en lista med val för vilken typ av graf som ska genereras.
 	 * 
 	 */
 	public void printGraphChoice() {
 	}
 
 	/**
 	 * Genererar en graf från timeReports med de inställningar som är satta i
 	 * gs.
 	 * 
 	 * @param timeReports
 	 *            lista med tidrapporter.
 	 * 
 	 * @param gs
 	 *            inställningar för grafen.
 	 * 
 	 * 
 	 * 
 	 */
 	public void printGraph(List<TimeReport> timeReports, GraphSettings gs) {
 		String xAxisName = gs.getXName();
 		String yAxisName = gs.getYName();
 		
 		String html = "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/jquery.min.js\"></script>";
 		html += "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/jquery.jqplot.min.js\"></script>";
 		html += "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/plugins/jqplot.barRenderer.min.js\"></script>";
 		html += "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/plugins/jqplot.categoryAxisRenderer.min.js\"></script>";
 		html += "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/plugins/jqplot.pointLabels.min.js\"></script>";
 		html += "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/plugins/jqplot.canvasTextRenderer.min.js\"></script>";
 		html += "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/plugins/jqplot.canvasAxisLabelRenderer.min.js\"></script>";
 		html += "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/plugins/jqplot.canvasAxisTickRenderer.min.js\"></script>";
 		html += "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/plugins/jqplot.dateAxisRenderer.min.js\"></script>";
 		html += "<link rel=\"stylesheet\" type=\"text/css\" href=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/jquery.jqplot.min.css\" />";
 
 		html += "<div id=\"chart\" ></div>";
 
 		html += "<script>";
 		html += "$.jqplot.config.enablePlugins = true;";
 		//html += "var s1 = [['a',2],['b',6],['c',7],['d',10]];";
 		
 		html += "var s1 = [";
 
 		switch (gs.getGraphType()) {
 		case "userWeekTime":
 			for (int i = 0; i < timeReports.size(); i++) {
 
 				List<Activity> activities = timeReports.get(i).getActivities();
 
 				int totalTime = 0;
 
 				for (Activity a : activities) {
 					totalTime += a.getLength();
 				}
 
 				html += "[" + timeReports.get(i).getWeek() + "," + totalTime
 						+ "],";
 			}
 			html = html.substring(0, html.length() - 1);
 
 			break;
 
 		case "ActivityTime":
 			HashMap<ActivityType, Integer> actTimes = new HashMap<ActivityType, Integer>();
 
 			for (int i = 0; i < timeReports.size(); i++) {
 
 				List<Activity> activities = timeReports.get(i).getActivities();
 
 				for (Activity a : activities) {
 					if (actTimes.containsKey(a.getType())) {
 						actTimes.put(a.getType(),
 								actTimes.get(a.getType()) + a.getLength());
 					} else {
 						actTimes.put(a.getType(), a.getLength());
 					}
 				}
 			}
 
 			Set<ActivityType> types = actTimes.keySet();
 
 			for (ActivityType t : types) {
 				html += "['" + t.toString() + "'," + actTimes.get(t).toString() + "],";
 			}
 			html = html.substring(0, html.length() - 1);
 
 			break;
 		case "PlUserTime":
 			HashMap<User, Integer> userTimes = new HashMap<User, Integer>();
 
 			for (int i = 0; i < timeReports.size(); i++) {
 
 				List<Activity> activities = timeReports.get(i).getActivities();
 
 				int totalTime = 0;
 
 				for (Activity a : activities) {
 					totalTime += a.getLength();
 				}
 
 				if (userTimes.containsKey(timeReports.get(i).getUser())) {
 					userTimes.put(timeReports.get(i).getUser(), userTimes.get(timeReports.get(i).getUser()) + totalTime);
 				} else {
 					userTimes.put(timeReports.get(i).getUser(), totalTime);
 				}
 			}
 			
 			Set<User> users = userTimes.keySet();
 
 			for (User u : users) {
 				html += "['" + u.toString() + "'," + userTimes.get(u).toString() + "],";
 			}
 			html = html.substring(0, html.length() - 1);
 			
 			break;
 		}
 		
 		html += "];";
 		  
 		html += "plot1 = $.jqplot('chart', [s1], {";
 		html += "	animate: !$.jqplot.use_excanvas,";
 		html += "	seriesDefaults:{";
 		html += "		renderer:$.jqplot.BarRenderer,";
 		html += "		pointLabels: { show: true }";
 		html += "	},";
 		html += "	legend: {show:false},";
 		html += "	axes: {";
 		html += "		xaxis: {";
 		html += "			tickOptions:{";
 		html += "				labelPosition: 'middle'";
 		html += "			},";
 		html += "			tickRenderer:$.jqplot.CanvasAxisTickRenderer,";
 		html += "			labelRenderer: $.jqplot.CanvasAxisLabelRenderer,";
 		
 		html += "			label:'" + xAxisName + "',";
 		
 		html += "			labelOptions:{";
 		html += "				fontFamily:'Helvetica',";
 		html += "				fontSize: '14pt'";
 		html += "			},";
 		html += "			renderer: $.jqplot.CategoryAxisRenderer,";
 		html += "		},";
 		html += "		yaxis: {";
 		html += "			tickOptions:{";
 		html += "				labelPosition: 'middle'";
 		html += "			},";
 		html += "			tickRenderer:$.jqplot.CanvasAxisTickRenderer,";
 		html += "			labelRenderer: $.jqplot.CanvasAxisLabelRenderer,";
 		
 		html += "			label:'" + yAxisName + "',";
 		
 		html += "			labelOptions:{";
 		html += "				fontFamily:'Helvetica',";
 		html += "				fontSize: '14pt'";
 		html += "			},";
 		html += "		}";
 		html += "	},";
 		html += "	highlighter: { show: false }";
 		html += "});";
 		html += "</script>";
 		
 		writer.print(html);
 	}
 
 	/**
 	 * 
 	 * Genererar en burn down chart från timeReports med de inställningar som är
 	 * satta i gs.
 	 * 
 	 * @param timeReports
 	 *            lista med tidrapporter.
 	 * @param gs
 	 *            inställningar för grafen.
 	 * 
 	 * 
 	 */
 	public void printBurndownChart(List<TimeReport> timeReports,
 			GraphSettings gs) {
 		
 		String xAxisName = gs.getXName();
 		String yAxisName = gs.getYName();
 		
 		String html = "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/jquery.min.js\"></script>";
 		html += "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/jquery.jqplot.min.js\"></script>";
 		html += "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/plugins/jqplot.highlighter.min.js\"></script>";
 		html += "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/plugins/jqplot.cursor.min.js\"></script>";
 		html += "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/plugins/jqplot.dateAxisRenderer.min.js\"></script>";
 
 		html += "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/plugins/jqplot.canvasTextRenderer.min.js\"></script>";
 		html += "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/plugins/jqplot.canvasAxisLabelRenderer.min.js\"></script>";
 		html += "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/plugins/jqplot.canvasAxisTickRenderer.min.js\"></script>";
 		html += "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/plugins/jqplot.dateAxisRenderer.min.js\"></script>";
 		html += "<script type=\"text/javascript\" src=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/plugins/jqplot.categoryAxisRenderer.min.js\"></script>";
 
 
 		html += "<link rel=\"stylesheet\" type=\"text/css\" href=\"/h/d6/w/dt09jn4/Desktop/PUSskit/dist/jquery.jqplot.min.css\" />";
 
 		html += "<div id=\"chart\" ></div>";
 
 		html += "<script>";
 
 		html += "var line=[['23-May-08', 578.55], ['20-Jun-08', 566.5], ['25-Jul-08', 480.88], ['22-Aug-08', 509.84],";
 		html += "	['26-Sep-08', 454.13], ['24-Oct-08', 379.75], ['21-Nov-08', 303], ['26-Dec-08', 308.56],";
 		html += "	['23-Jan-09', 299.14], ['20-Feb-09', 346.51], ['20-Mar-09', 325.99], ['24-Apr-09', 386.15]];";
 		
 		switch(gs.getGraphType()){
 		case "weekBurnDown":
 			for(int i = 0; i < timeReports.size(); i++){
 				
 			}
 			break;
 		case "activityBurnDown":
 			for(int i = 0; i < timeReports.size(); i++){
 				
 			}
 			break;
 		case "userBurnDown":
 			for(int i = 0; i < timeReports.size(); i++){
 				
 			}
 			break;
 		}
 		
 		
 		
 		html += "var plot1 = $.jqplot('chart', [line], {";
 		html += "	animate: !$.jqplot.use_excanvas,";
 		html += "	title:'Data Point Highlighting',";
 		html += "	axes:{";
 		html += "		xaxis:{";
 		html += "			renderer:$.jqplot.DateAxisRenderer,";
 		html += "			tickOptions:{";
 		html += "				formatString:'%b&nbsp;%#d',";
 		html += "				labelPosition: 'middle'";
 		html += "			},";
 		html += "			tickRenderer:$.jqplot.CanvasAxisTickRenderer,";
 		html += "			labelRenderer: $.jqplot.CanvasAxisLabelRenderer,";
 		
 		html += "				label:'" + xAxisName + "',";
 		
 		html += "			labelOptions:{";
 		html += "				fontFamily:'Helvetica',";
 		html += "				fontSize: '14pt'";
 		html += "			},";
 		html += "			renderer: $.jqplot.CategoryAxisRenderer,";
 		html += "		},";
 		html += "		yaxis:{";
 		html += "			tickOptions:{";
 		html += "				formatString:'$%.2f',";
 		html += "				labelPosition: 'middle'";
 		html += "			},";
 		html += "			tickRenderer:$.jqplot.CanvasAxisTickRenderer,";
 		html += "			labelRenderer: $.jqplot.CanvasAxisLabelRenderer,";
 		
 		html += "            	label:'" + yAxisName +"',";
 		
 		html += "			labelOptions:{";
 		html += "				fontFamily:'Helvetica',";
 		html += "				fontSize: '14pt'";
 		html += "			},";
 		html += "		}";
 		html += "	},";
 		html += "	highlighter: {";
 		html += "		show: true,";
 		html += "		sizeAdjust: 7.5";
 		html += "	},";
 		html += "	cursor: {";
 		html += "		show: false";
 		html += "	}";
 		html += "});";
 		html += "</script>";
 		
 		writer.print(html);
 	}
 
 	/**
 	 * 
 	 Skriver ut sidhuvudet.
 	 * 
 	 * @param user
 	 *            den inloggade användaren.
 	 * 
 	 */
 	public void printHead(User user) {
 		writer.print("<html><head><meta charset=\"latin1\"><title>E-PUSS 1301</title></head><body><h1>E-PUSS 1301</h1>");
 		if(user != null){
 			writer.print("<p>Hejsan " + user.getUsername()+ "! Tryck ");
 			printLink("login", "här");
 			writer.print(" om du vill logga ut.</p>");
 		}
 	}
 
 	/**
 	 * 
 	 Skriver ut sidfoten
 	 * 
 	 */
 	public void printFoot() {
 		writer.print("</body></html>");
 	}
 
 	/**
 	 * 
 	 Skriver ut en dropdown där användaren får välja projektgrupp.
 	 * 
 	 * @param i
 	 *            är nuvarande projektgrupp.
 	 * @param projects
 	 *            är samtliga tillgängliga projekt.
 	 * 
 	 */
 	public void printProjectChooser(int n, List<String> projects) {
 		n = n < 0 ? 0 : n;
 		writer.print("<form method=\"POST\" action=\"mainpage\" name=\"chooser\" id = \"chooser\">");
 		writer.print("<select name=\"" + PROJECT_CHOOSER + "\" id=\"" + PROJECT_CHOOSER + "\"  onchange=\"this.form.submit()\" >");
 		for (int i = 0; i < projects.size(); i++) {
 			writer.print("<option value = " + projects.get(i));
 			if (i == n) {
 				writer.print(" selected");
 			}
 			writer.print(" > " + projects.get(i));
 			writer.print("</option>");
 		}
 		writer.print("</select>");
 		writer.print("</form>");
 	}
 
 	/**
 	 * 
 	 Skriver ut ett meddelande för att meddela rätt resultat.
 	 * 
 	 * @param message
 	 *            det meddelande som skall skrivas ut.
 	 * 
 	 */
 	public void printSuccessMessage(String message) {
 		writer.print("<p style=\"color:green;\">" + message + "</p>");
 	}
 
 	/**
 	 * 
 	 @param users
 	 *            lista över användare i systemet. Skriver ut en lista över alla
 	 *            användare i systemet.
 	 * 
 	 */
 	public void printAdminUserList(List<User> users) {
 		if (users != null && users.size() > 0) {
 			writer.print("<table><tr><th>Användare</th><th>Passwords</th><th></th></tr>");
 			for (User u : users) {
 				writer.print("<tr><td>"
 						+ u.getUsername()
 						+ "</td><td>"
 						+ u.getPassword()
 						+ "</td><td>"
 						+ "<a href=\"/ProjectPUSS/administration?action=deleteUser&username="
 						+ u.getUsername() + "\">Ta bort</a></td></tr>");
 			}
 			writer.print("</table>");
 		}
 	}
 
 	/**
 	 * 
 	 Skriver ut ett formulär för att lägga till en användare i systemet.
 	 * 
 	 */
 	public void printAddUserForm() {
 		writer.print("<form method=\"POST\" action=\"/ProjectPUSS/administration?action=createUser\">"
 				+ "<label>Användarnamn</label><input name=\"username\" type=\"text\" />"
 				+ "<input type=\"submit\" value=\"Skapa\" />" + "</form>");
 	}
 
 	/**
 	 * 
 	 @param groups
 	 *            lista med grupper. Skriver ut listan över grupper.
 	 * 
 	 */
 	public void printProjectGroups(List<String> groups) {
 		if (groups != null && groups.size() > 0) {
 			writer.print("<table><tr><th>Projektgrupper</th><th></th></tr>");
 			for (String s : groups) {
 				writer.print("<tr><td>"
 						+ "<a href=\"projectoverview?project="
 						+ s
 						+ "\">" + s + "</a>"
 						+ "</td><td><a href=\"projectadmin?action=removeProjectGroup&projectName="
 						+ s + "\">Ta bort</a></td></tr>");
 			}
 
 			writer.print("</table>");
 		}
 	}
 
 	/**
 	 * 
 	 Skriver ut ett formulär för att lägga till en projektgrupp i systemet.
 	 * 
 	 */
 	public void printAddProjectGroupForm() {
 		writer.print("<form method=\"POST\" action=\"/ProjectPUSS/projectadmin?action=createProjectGroup\">"
 				+ "<label>Projekgruppnamn</label><input name=\"projectname\" type=\"text\" />"
 				+ "<input type=\"submit\" value=\"Skapa\" />" + "</form>");
 	}
 
 	/**
 	 * Skriver ut en lista över en projektgrupps medlemmar.
 	 * 
 	 * @param users
 	 *            lista över medlemmar i en projektgrupp.
 	 * 
 	 * @param projectManagers
 	 *            lista över projektledare i en projektgrupp.
 	 * 
 	 */
 	public void printProjectGroupMembers(List<User> users,
 			List<User> projectManagers, String projectName) {
 		if (!(users.size() == 0 && projectManagers.size() == 0)) {
			writer.print("<table><tr><th>Användarnamn</th><th></th><th></th></tr>");
 			for (User u : projectManagers) {
 				writer.print("<tr><td>"
 						+ u.getUsername()
 						+ "</td><td><a href=\"projectoverview?action=demoteUser&project="
 						+ projectName
 						+ "&username="
 						+ u.getUsername()
 						+ "\">Gör till användare</a></td><td>"
 						+ "<a href=\"projectoverview?action=deleteUser&project="
 						+ projectName + "&username=" + u.getUsername()
 						+ "\">Ta bort</a>" + "</td></tr>");
 			}
 			for (User u : users) {
 				if (!projectManagers.contains(u)) {
 					writer.print("<tr><td>"
 							+ u.getUsername()
 							+ "</td><td><a href=\"projectoverview?action=promoteUser&project="
 							+ projectName
 							+ "&username="
 							+ u.getUsername()
 							+ "\">Gör till projektledare</a></td><td>"
 							+ "<a href=\"projectoverview?action=deleteUser&project="
 							+ projectName + "&username=" + u.getUsername()
 							+ "\">Ta bort</a>" + "</td></tr>");
 				}
 			}
 			writer.print("</table>");
 		}
 	}
 
 	/**
 	 * 
 	 Skriver ut ett formulär för att lägga till en användare i en grupp.
 	 * 
 	 * @param projectName
 	 * 
 	 */
 	public void printAddUserToProjectGroupForm(String projectName) {
 		writer.print("<form method=\"POST\" action=\"/ProjectPUSS/projectoverview?action=addUser&project="
 				+ projectName
 				+ "\">"
 				+ "<label>Användarnamn</label><input name=\"username\" type=\"text\" />"
 				+ "<input type=\"submit\" value=\"Lägg till\" />" + "</form>");
 	}
 
 	/**
 	 * 
 	 @param message
 	 *            felmeddelande. Skriver ut ett felmeddelande.
 	 * 
 	 */
 	public void printErrorMessage(String message) {
 		writer.print("<p style=\"color:red;\">" + message + "</p>");
 	}
 
 	/**
 	 * Generates a form for login.
 	 * 
 	 * @return HTML code for the form
 	 */
 	public void printLoginRequestForm() {
 		writer.println("<p>Användarnamn och Lösenord, tack!</p>");
 		writer.println("<form method=\"POST\" action=\"login\">");
 		writer.println("<p>Namn: <input type=\"text\" name = " + LOGIN_USERNAME + "></p>");
 		writer.println("<p>Lösenord: <input type=\"password\" name = " + LOGIN_PASSWORD + "></p>");
 		writer.println("<input type=\"submit\" value=\"Skicka\">");
 		writer.println("</form>");
 
 	}
 	
 	/**
 	 * Generates a link.
 	 * 
 	 * @param url
 	 * 		the url to use for the link
 	 * @param text
 	 * 		the text to display
 	 * 
 	 * @return HTML code for the link
 	 */
 	public void printLink(String url, String text) {
 		writer.println("<a href=\"" + url + "\">" + text + "</a>");
 	}
 
 }
