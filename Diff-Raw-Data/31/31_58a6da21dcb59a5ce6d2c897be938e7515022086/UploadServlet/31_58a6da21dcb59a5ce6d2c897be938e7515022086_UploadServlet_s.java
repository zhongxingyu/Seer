 package web;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import master.AbstractMaster;
 import master.WebSocketChannelManager;
 import model.BSPlayer;
 import model.STATUS;
 
 import common.BSUtil;
 import common.Config;
 import common.HibernateUtil;
 
 
 /**
  * Upload players
  * @author stevearc
  *
  */
 public class UploadServlet extends HttpServlet {
 	private static final long serialVersionUID = 2147508188812654640L;
 	public static final String NAME = "/upload.html";
 
 	private void warn(HttpServletResponse response, String warning) throws IOException {
 		response.getWriter().println("<p class='ui-state-error' style='padding:10px'>" + warning + "</p>");
 	}
 	
 	private void highlight(HttpServletResponse response, String msg) throws IOException {
 		response.getWriter().println("<p class='ui-state-highlight' style='padding:10px'>" + msg + "</p>");
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("text/html");
 		response.setStatus(HttpServletResponse.SC_OK);
 		PrintWriter out = response.getWriter();
 		out.println("<html><head>");
 		out.println("<title>Battlecode Tester</title>");
 		out.println("<link rel=\"stylesheet\" href=\"css/table.css\" />");
 		out.println("</head>");
 		out.println("<body>");
 
 		WebUtil.writeTabs(request, response, NAME);
 		out.println("<div id='player-info-dialog' style='text-align:center'>Compile your player using" +
 		"<div class='code'>ant jar -Dteam=teamXXX</div>" + 
 		"where XXX is your team number.</div>");
 		out.println("<div id='map-info-dialog' style='text-align:center'>If one at a time is too slow, you can manually copy the " +
 				"maps into the \"maps\" directory and they will be automatically detected</div>");
 
 		if (request.getParameter("submit-player") != null) {
 			File player = (File) request.getAttribute("player");
 			String playerName = request.getParameter("player_name").trim();
 			if (player == null || !player.exists()) {
 				// file doesn't exist
 				warn(response, "Please select a valid file");
 			} else if (player.isDirectory()) {
 				// what?
 				warn(response, "Please select a valid file");
 			} else if (playerName == null || playerName.isEmpty() || playerName.contains("/") || playerName.contains(" ")) {
 				warn(response, "Please enter a valid name for your player");
 			} else if (playerName.length() > 45) {
 				warn(response, "Player name is too long");
 			} else {
 				BSUtil.writeFileData(player, Config.teamsDir + playerName + ".jar");
 				BSPlayer bsPlayer = new BSPlayer();
 				bsPlayer.setPlayerName(playerName);
 				EntityManager em = HibernateUtil.getEntityManager();
 				em.persist(bsPlayer);
 				em.getTransaction().begin();
 				try {
 					em.flush();
 					highlight(response, "Successfully uploaded player: " + playerName);
 				} catch (PersistenceException e) {
 					// player name already exists
 					warn(response, "Player name already exists");
 				}
 				if (em.getTransaction().getRollbackOnly()) {
 					em.getTransaction().rollback();
 				} else {
 					em.getTransaction().commit();
 					em.refresh(bsPlayer);
 					WebSocketChannelManager.broadcastMsg("index", "ADD_PLAYER", bsPlayer.getId() + "," + bsPlayer.getPlayerName());
 				}
 				em.close();
 			}
 		} else if (request.getParameter("submit-update") != null) {
 			File battlecode_server = (File) request.getAttribute("battlecode-server");
 			File allowedPackages = (File) request.getAttribute("allowedPackages");
 			File disallowedClasses = (File) request.getAttribute("disallowedClasses");
 			File methodCosts = (File) request.getAttribute("methodCosts");
 			if (allowedPackages == null || !allowedPackages.exists() || allowedPackages.isDirectory()) {
 				warn(response, "Please select a valid AllowedPackages.txt file");
 			} 
 			else if (disallowedClasses == null || !disallowedClasses.exists() || disallowedClasses.isDirectory()) {
 				warn(response, "Please select a valid DisallowedClasses.txt file");
 			} 
 			else if (methodCosts == null || !methodCosts.exists() || methodCosts.isDirectory()) {
 				warn(response, "Please select a valid MethodCosts.txt file");
 			} 
 			else if (battlecode_server == null || !battlecode_server.exists() || battlecode_server.isDirectory()) {
 				warn(response, "Please select a valid battlecode-server.jar file");
 			} 
 			else {
 				AbstractMaster.getMaster().updateBattlecodeFiles(battlecode_server, allowedPackages, disallowedClasses, methodCosts);
 				EntityManager em = HibernateUtil.getEntityManager();
 				Long numRunning = em.createQuery("select count(*) from BSRun run where run.status = ?", Long.class)
 				.setParameter(1, STATUS.RUNNING)
 				.getSingleResult();
 				if (numRunning == 0) {
 					highlight(response, "Successfully updated battlecode version!");
 				} else {
 					highlight(response, "Successfully updated battlecode version!  Changes will take place after current run finishes.");
 				}
 				em.close();
 			}
 		} else if (request.getParameter("submit-map") != null) {
 			File map = (File) request.getAttribute("map");
 			String mapName = request.getParameter("mapName").trim();
 			if (map == null || !map.exists() || map.isDirectory()) {
 				warn(response, "Please select a valid map file");
 			} else if (mapName == null || mapName.isEmpty() || mapName.contains("/") || mapName.contains(" ")) {
 				warn(response, "Map file has invalid name"); 
 			} else {
 				if (mapName.toLowerCase().endsWith(".xml")) {
 					mapName = mapName.substring(0, mapName.length() - 4);
 				}
 				String mapPath = Config.mapsDir + mapName + ".xml";
 				if (new File(mapPath).exists()) {
 					warn(response, "Map " + mapName + ".xml already exists!");
 				} else {
 					BSUtil.writeFileData(map, mapPath);
 					highlight(response, "Successfully uploaded map: " + mapName + ".xml");
 					AbstractMaster.kickoffUpdateMaps();
 				}
 			}
 		}
 		if (!BSUtil.initializedBattlecode()) {
 			warn(response, "You must upload battlecode files, maps, and a player");
 		}
 
 		// Form for uploading your player
 		out.println("<div style='margin:20px 10px 10px 10px; font-size:12px; float:left'>");
 		out.println("<form action='" + response.encodeURL(NAME) + "' method='post' enctype='multipart/form-data'>");
 		out.println("<table>");
 		out.println("<tr><th colspan='2'>Upload your player<span id='player-info'></span></th></tr>");
 		out.println("<tr>" +
 				"<td style='text-align:right'>Player jar:</td>" +
 				"<td><input type='file' name='player' /></td>" +
 				"</tr>");
 		out.println("<tr>" +
 				"<td style='text-align:right'>Player Name:</td>" +
 				"<td><input type='text' name='player_name' size='20' /></td>" +
 				"</tr>");
 		out.println("<tr><td></td>" +
 				"<td><input type='submit' name='submit-player' value='Upload'/></td>" +
 				"</tr>");
 		out.println("</table>");
 		out.println("</form>");
 		
 		// Form for uploading a map
 		out.println("<form id='mapForm' action='" + response.encodeURL(NAME) + "' method='post' enctype='multipart/form-data'>");
 		out.println("<table>");
 		out.println("<tr><th colspan='2'>Upload a map<span id='map-info'></span></th></tr>");
 		out.println("<tr>" +
 				"<td style='text-align:right'>Map:</td>" +
 				"<td><input type='file' name='map' id='mapFile' /></td>" +
 				"</tr>");
 		out.println("<tr><td></td>" +
 				"<td><input type='submit' name='submit-map' value='Upload'/></td>" +
 				"</tr>");
 		out.println("</table>");
 		out.println("<input id='mapName' type='hidden' name='mapName' />");
 		out.println("</form>");
 		out.println("</div>");
 		
 		// Form for uploading a new battlecode version
 		out.println("<div style='margin:20px 10px 10px 10px; font-size:12px; float:right;'>");
 		out.println("<form action='" + response.encodeURL(NAME) + "' method='post' enctype='multipart/form-data'>");
 		out.println("<table>");
 		out.println("<tr><th colspan='2'>Upload battlecode files</th></tr>");
 		out.println("<tr>" +
 				"<td style='text-align:right'>battlecode-server.jar file:</td>" +
 				"<td><input type='file' name='battlecode-server'/></td>" +
 				"</tr>");
 		out.println("<tr>" +
 				"<td style='text-align:right'>AllowedPackages.txt:</td>" +
 				"<td><input type='file' name='allowedPackages'/></td>" +
 				"</tr>");
 		out.println("<tr>" +
 				"<td style='text-align:right'>DisallowedClasses.txt:</td>" +
 				"<td><input type='file' name='disallowedClasses'/></td>" +
 				"</tr>");
 		out.println("<tr>" +
 				"<td style='text-align:right'>MethodCosts.txt:</td>" +
 				"<td><input type='file' name='methodCosts'/></td>" +
 				"</tr>");
 		out.println("<tr><td></td>" +
 				"<td><input type='submit' name='submit-update' value='Upload'/></td>" +
 				"</tr>");
 		out.println("</table>");
 		out.println("</form>");
 		out.println("</div>");
 
 		out.println("<script src='js/upload.js'></script>");
 		out.println("</body>" +
 		"</html>");
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doGet(request, response);
 	}
 	
 }
