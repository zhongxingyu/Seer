 package servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import login.Logger;
 
 import utils.BeanUtilities;
 import utils.Config;
 import utils.GenericUtilities;
 import utils.Tern;
 import view.Style;
 
 import dataconnection.MySQLConnection;
 import entities.ChampionshipEntity;
 import entities.DayEntity;
 import entities.FormationEntity;
 import entities.PlayerEntity;
 import entities.TeamEntity;
 
 /**
  * Servlet implementation class FormationHandling
  */
 @WebServlet("/FormationHandling")
 public class FormationHandling extends HttpServlet {
 	
 	private static final String TITLE = "Gestione delle Formazioni";
 
 	
     /**
      * @see HttpServlet#HttpServlet()
      */
     public FormationHandling() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	public void doGet(HttpServletRequest request, HttpServletResponse response) 
 			throws ServletException, IOException {
 		response.setContentType("text/html");
 		PrintWriter out = response.getWriter();
 		out.println(Style.pageHeader(TITLE));
 		Logger logger = GenericUtilities.checkLoggedIn(request, response, false);
 		
 		// connessione al database
 		MySQLConnection dbc = new MySQLConnection();
 		dbc.init();
 
 		// --- stampa le giornate ---
 		try {			
 			// codice del form da stampare
 			StringBuffer code = new StringBuffer();
 			code.append("<form name=\"formationhandling\" method=\"POST\">\n");
 			code.append("Scegli la giornata:\n");
 			code.append("<select name=\"day\">\n");
 
 			// flag vero se esistono sono giornate
 			Boolean anyDays = false; 
 			// lista dei campionati dell'utente
 			List<ChampionshipEntity> lc = dbc.getDefChampOfUser(logger.getUser().getId());
 			// stampa le giornate aperte divise per campionato
 			for(Iterator<ChampionshipEntity> i = lc.iterator();i.hasNext();){
 				// fissa un campionato c
 				ChampionshipEntity c = i.next(); 
 				List<DayEntity> ld = dbc.getDayOfChampionship(c.getId());
 				// TODO far scegliere checkbox se stampare solo le giornate modificabili
 				// --- List<DayEntity> lod = dbc.getOpenDayOfChampionship(c.getId()); ---
 				if(ld.size() > 0){
 					// segnala che ci sono giornate da stampare
 					anyDays = true;
 					// stampa le giornate del campionato c
 					code.append(Style.optionGroup(c.getName()));	
 					for(Iterator<DayEntity> j = ld.iterator(); j.hasNext(); ){
 						DayEntity d = j.next();
 						code.append(Style.option(d.getId().toString(),d.getFormatDate()));		
 					}
 				}
 			}
 			code.append("</select>\n");
 			code.append("<input type=\"hidden\" name=\"todo\" value=\"viewday\">\n");
 			code.append("<input type=\"submit\" value=\"Visualizza formazione\">\n");
 			code.append("</form>\n");
 			if(anyDays){
 				out.println(code.toString());
 			}else{
 				out.println(Style.infoMessage(
 					"Non ci sono giornate disponibili per questo utente"));
 			}
 			
 		} catch (SQLException e) {
 			out.println(Style.alertMessage("Errore SQL: "+e.getMessage()));
 		}
 		
 		String todoParam = request.getParameter("todo");
 		if(todoParam != null){
 			// TODO insformation e modformation hanno parti da fattorizzare
 
 			if(todoParam.equalsIgnoreCase("insformation")){
 				// --- inserimento della formazione ---
 				try{
 					FormationEntity fe = new FormationEntity();
 					BeanUtilities.populateBean(fe,request);
 					ChampionshipEntity c = dbc.getChampionshipOfTeam(fe.getTeam());
 					List<DayEntity> ld = dbc.getOpenDayOfChampionship(c.getId());
 					// lista degli id delle giornate aperte
 					List<Integer> lid = new ArrayList<Integer>();
 					for(Iterator<DayEntity> it = ld.iterator(); it.hasNext();){
 						lid.add(((DayEntity)it.next()).getId());
 					}
 
 					if(fe.isComplete()){
 						// se la formazione ricevuta  corretta, non  gi presente una formazione
 						// e la giornata  ancora aperta a modifiche						
 						if(fe.isCorrect() && 
 							dbc.getFormation(fe.getTeam(), fe.getDay()).isEmpty() && 
 							lid.contains(fe.getDay()))
 						{
 							// inserisci i dati della convocazione
 							dbc.insertFormation(fe);
 							out.println(Style.successMessage("Formazione salvata"));
 						}else{
 							// dati inseriti scorretti
 							// TODO leggere i numeri da file di configurazione
 							out.println(Style.alertMessage("I dati inseriti non sono corretti: "+ 
 								"le formazioni possibili sono 3-4-3, 3-5-2, 4-5-1, 4-4-2, 4-3-3, 5-4-1, 5-3-2"));
 						}
 					}else{
 						out.println(Style.alertMessage("Inserire tutti i dati"));
 					}
 				}catch(SQLException sqle){
 					out.println(Style.alertMessage("Errore SQL: "+sqle.getMessage()));
 				}
 			}else if(todoParam.equalsIgnoreCase("modformation")){
 				// --- modifica della formazione ---
 				try{
 					FormationEntity fe = new FormationEntity();
 					BeanUtilities.populateBean(fe,request);
 					// se la formazione ricevuta  corretta e la giornata  ancora aperta a modifiche
 					// (in questo caso la formazione DEVE essere gi presente in quanto da modificare)
 					ChampionshipEntity c = dbc.getChampionshipOfTeam(fe.getTeam());
 					List<DayEntity> ld = dbc.getOpenDayOfChampionship(c.getId());
 					// lista degli id delle giornate aperte
 					List<Integer> lid = new ArrayList<Integer>();
 					for(Iterator<DayEntity> it = ld.iterator(); it.hasNext();){
 						lid.add(((DayEntity)it.next()).getId());
 					}
 					if(!fe.isComplete()){
 						out.println(Style.alertMessage("Inserire tutti i dati"));
 					}else if(!fe.isCorrect()){
 						// dati inseriti scorretti
 						// TODO leggere i numeri da file di configurazione
 						out.println(Style.alertMessage("I dati inseriti non sono corretti: "+ 
 							"le formazioni possibili sono 3-4-3, 3-5-2, 4-5-1, 4-4-2, 4-3-3, 5-4-1, 5-3-2"));
 					}else if(!lid.contains(fe.getDay())){
 						// la giornata  chiusa alle modifiche
 						out.println(Style.alertMessage("Non  possibile fare modifiche a una giornata chiusa"));						
 					}else{
 						// aggiorna la formazione passando al metodo il nuovo e il vecchio schieramento
 						dbc.updateFormation(fe,dbc.getFormation(fe.getTeam(), fe.getDay()));			
 						out.println(Style.successMessage("Formazione modificata"));
 					}
 				}catch(SQLException sqle){
 					out.println(Style.alertMessage("Errore SQL: "+sqle.getMessage()));					
 				}
 			}
 			// --- stampa lo stato della giornata selezionata ---
 			try{
 				// id della giornata selezionata
 				Integer did = Integer.parseInt(request.getParameter("day"));
 				// determina se la giornata  aperta alle modifiche
 				List<DayEntity> openDays = 
 					dbc.getOpenDayOfChampionship(dbc.getChampionshipOfDay(did).getId());
 				// flag true se la giornata  aperta alle modifiche
 				Boolean isOpenDay = false;
 				for(Iterator<DayEntity> it = openDays.iterator(); it.hasNext();){
 					if(((DayEntity)it.next()).getId().equals(did)){
 						isOpenDay = true;
 						break;
 					}
 				}
 
 				// stampa messaggio giornata chiusa
 				if(!isOpenDay)
 					out.println(Style.infoMessage("La giornata non &egrave; aperta alle modifiche"));
 				List<TeamEntity> lTeam = dbc.getTeamsOfUserInDay(logger.getUser().getId(),did);
 				if(lTeam.size() > 0){
 					// per ogni squadra
 					for(Iterator<TeamEntity> it = lTeam.iterator(); it.hasNext();){
 						TeamEntity team = it.next();
 						out.println("<hr>\n<h2>"+team.getName()+"</h2>");						
 						// prendi le convocazioni della squadra da database
 						List<PlayerEntity> hiredPlayers = dbc.getHiredPlayers(team.getId());
 						//se sono stati convocati giocatori per questa squadra
 						if(hiredPlayers.size() > 0){
 							// recupera da database la formazione della squadra nel giorno
 							FormationEntity formation = dbc.getFormation(team.getId(), did);
 							if(!formation.isEmpty()){
 								// --- formazione attuale ---
 								out.println(printFormation(
 										dbc.getPlayersById(formation.getDef()), 
 										dbc.getPlayersById(formation.getCen()), 
 										dbc.getPlayersById(formation.getAtt()), 
 										dbc.getPlayersById(formation.getGolkeep()), 
 										dbc.getPlayersById(formation.getResDef()),
 										dbc.getPlayersById(formation.getResCen()),
 										dbc.getPlayersById(formation.getResAtt()),
 										dbc.getPlayersById(formation.getResGolkeep()),
 										team));
 							}
 							
 							if(isOpenDay){
 								// --- form inserimento formazione ---
 								if(!formation.isEmpty()){
									out.println(Style.infoMessage(
										"&Egrave; ancora possibile modificare la formazione: "));
 								}else{
									out.println(Style.infoMessage("La formazione della squadra <b>"+
										team.getName()+"</b> deve ancora essere inserita: "));
 								}
 								out.println("<form name=\"formationhandling\" method=\"POST\">");
 								out.println("<h3>Modulo</h3>");
 								for(Integer i = 0; i < Config.getFormations().size(); i++){
 									Tern<Integer,Integer,Integer> tern = Config.getFormations().get(i);
 									out.println(Style.inputRadio("formation", i.toString(), 
 										formation.getFormation() != null && formation.getFormation().equals(i)) + 
 										tern.getFirst() + " - " + tern.getSecond() + " - " + tern.getThird() + "<br>");
 								}
 								out.println("<h3>Difensori</h3>");
 								out.println(radioButtonPlayers(
 										GenericUtilities.getPlayersListByRule(hiredPlayers,'D'),
 										"def","idDef",formation.getDef(),formation.getResDef()));
 								out.println("<h3>Centrocampisti</h3>");
 								out.println(radioButtonPlayers(
 										GenericUtilities.getPlayersListByRule(hiredPlayers,'C'),
 										"cen","idCen",formation.getCen(),formation.getResCen()));
 								out.println("<h3>Attaccanti</h3>");
 								out.println(radioButtonPlayers(
 										GenericUtilities.getPlayersListByRule(hiredPlayers,'A'),
 										"att","idAtt",formation.getAtt(),formation.getResAtt()));
 								out.println("<h3>Portieri</h3>");								
 								out.println(radioButtonPlayers(
 										GenericUtilities.getPlayersListByRule(hiredPlayers,'P'),
 										"golkeep","idGolkeep",formation.getGolkeep(),formation.getResGolkeep()));
 								out.println(Style.hidden("day", did.toString()));
 								out.println(Style.hidden("team", team.getId().toString()));
 					
 								// se esisteva gi una formazione
 								if(!formation.isEmpty()){
 									out.println(Style.hidden("todo", "modformation"));								
 									out.println("<input type=\"submit\" value=\"Modifica formazione\">");
 								}else{
 									out.println(Style.hidden("todo", "insformation"));
 									out.println("<input type=\"submit\" value=\"Inserisci formazione\">");
 								}
 								out.println("<input type=\"reset\">");	
 								out.println("</form>");
 							}else if(formation.isEmpty()){
 								out.println(Style.alertMessage("La giornata  chiusa senza che la squadra "+
 									team.getName()+" abbia ricevuto una formazione."));
 							}
 						}else{
 							// se non sono stati convocati giocatori
 							out.println(Style.alertMessage(
 								"Non sono ancora stati convocati giocatori per la squadra "+team.getName()));
 						}
 					}
 				}else{
 					out.println(Style.alertMessage(
 						"Non hai squadre che giocano in questa giornata di campionato"));
 				}
 			}catch(SQLException sqle){
 				out.println(Style.alertMessage("Errore SQL: "+sqle.getMessage()));				
 			}
 		}
 
 		// chiusura database
 		dbc.destroy();
 
 		out.println(Style.pageFooter());
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	public void doPost(HttpServletRequest request, HttpServletResponse response) 
 			throws ServletException, IOException {
 		this.doGet(request, response);
 	}
 	
 	/**
 	 * metodo che restituisce il codice html di 3 radio button per ogni calciatore
 	 * @param players lista di calciatori
 	 * @param name nome dei gruppi di radio button
 	 * @return codice html dei radio button
 	 */
 	private String radioButtonPlayers(
 			List<PlayerEntity> players, String name, String nameHidden, 
 			List<Integer> selectedPlayers, List<Integer> reservePlayers){
 		StringBuffer code = new StringBuffer("<ul>\n");
 		// contatore per i nomi dei gruppi
 		Integer i = 1;
 		for(Iterator<PlayerEntity> it = players.iterator(); it.hasNext(); i++){
 			PlayerEntity p = it.next();
 			// flag giocatore selezionato
 			Boolean isSelected = selectedPlayers.contains(p.getId());
 			// flag giocatore riserva
 			Boolean isReserve = reservePlayers.contains(p.getId());
 			// flag giocatore non selezionato
 			Boolean isNotSelected = !isSelected && !isReserve;
 			String serialName = name+i.toString();
 			String serialHidden = nameHidden+i.toString();
 			code.append("<li>"+p.getName()+" - "+p.getTeam()+": ");	
 			code.append(Style.inputRadio(serialName, "2", isSelected)+"Titolare");
 			code.append(Style.inputRadio(serialName, "1", isReserve)+"Riserva");
 			code.append(Style.inputRadio(serialName, "0", isNotSelected)+"Panchina\n");
 			code.append(Style.hidden(serialHidden, p.getId().toString()));
 		}
 		code.append("</ul>\n");
 		return code.toString();
 	}
 	
 	/**
 	 * metodo che stampa il codice html della formazione
 	 * @param def lista difensori
 	 * @param cen lista centrocampisti
 	 * @param att lista attaccanti
 	 * @param golkeep lista portieri
 	 * @param resDef lista difensori riserve
 	 * @param resCen lista centrocampisti riserve
 	 * @param resAtt lista attaccanti riserve
 	 * @param resGolkeep lista portieri riserve
 	 * @param team squadra
 	 * @return codice html di stampa della formazione
 	 */
 	private String printFormation(List<PlayerEntity> def, List<PlayerEntity> cen,
 			List<PlayerEntity> att, List<PlayerEntity> golkeep, 
 			List<PlayerEntity> resDef, List<PlayerEntity> resCen, List<PlayerEntity> resAtt,
 			List<PlayerEntity> resGolkeep, TeamEntity team){
 		StringBuffer code = new StringBuffer();
 		code.append("<h3>Formazione</h3>");
 		
 		code.append("<ul>");
 		code.append("<li><b>Modulo:</b> "+ def.size() + " - " + 
 			cen.size() + " - " + att.size());
 		code.append("<li><b>Difensori Titolari:</b>"+
 			Style.showPlayersList(def,false));
 		code.append("<li><b>Difensori Riserve:</b>"+
 				Style.showPlayersList(resDef,false));
 		code.append("<li><b>Centrocampisti Titolari:</b>"+
 				Style.showPlayersList(cen,false));
 		code.append("<li><b>Centrocampisti Riserve:</b>"+
 				Style.showPlayersList(resCen,false));	
 		code.append("<li><b>Attaccanti Titolari:</b>"+
 				Style.showPlayersList(att,false));								
 		code.append("<li><b>Attaccanti Riserve:</b>"+
 				Style.showPlayersList(resAtt,false));		
 		code.append("<li><b>Portiere Titolare:</b>"+
 				Style.showPlayersList(golkeep,false));
 		code.append("<li><b>Portiere Riserva:</b>"+
 				Style.showPlayersList(resGolkeep,false));								
 		code.append("</ul>");
 		return code.toString();
 	}
 
 }
