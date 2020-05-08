 package de.team55.mms.server.ctrl;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import de.team55.mms.data.Feld;
 import de.team55.mms.data.Modul;
 import de.team55.mms.data.Modulhandbuch;
 import de.team55.mms.data.Nachricht;
 import de.team55.mms.data.StellvertreterList;
 import de.team55.mms.data.Studiengang;
 import de.team55.mms.data.User;
 import de.team55.mms.data.UserRelation;
 import de.team55.mms.data.UserUpdateContainer;
 import de.team55.mms.data.pordnung;
 //import de.team55.mms.data.Zuordnung;
 import de.team55.mms.server.db.sql;
 
 @Path("")
 public class MessageResource {
 
 	/**
 	 * gibt alle User aus
 	 * 
 	 * @return Liste von Usern
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/user/get/all/{ack}")
 	public ArrayList<User> getAllUsers(@PathParam("ack") String s) {
 		System.out.println("Userliste abgefragt");
 		if(s.equals("true")){
 		return new sql().userload(true);
 		} else{
 			return new sql().userload(false);
 		}
 	}
 
 	/**
 	 * Gibt Anzahl an Studiengaengen aus
 	 * 
 	 * @return Anzhal
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/studiengang/get/Anzahl")
 	public String getAnzahlStudiengaenge() {
 		System.out.println("Anzahl an Studiengaengen wurde abgefragt");
 		return Integer.toString(new sql().getAnzahlStudiengaenge());
 	}
 
 	/**
 	 * Gibt eine Liste von Versionen eines Moduls aus
 	 * 
 	 * @param name
 	 *            Name des Moduls
 	 * @return Liste von Versionen eines Module
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/modul/get/{name}")
 	public ArrayList<Modul> getModul(@PathParam("name") String name) {
 		System.out.println("Modul " + name + " abgefragt");
 		return new sql().getModul(name);
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/feld/get")
 	public ArrayList<Feld> getFelder() {
 		System.out.println("Standard Felder abgefragt");
 		return new sql().getDefaultFelder();
 	}
 	
 	@POST
 	@Path("/feld/post")
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response setFelder(ArrayList<Feld> felder) {
 		int status = new sql().setDefaultFelder(felder);
 		if (status == 1) {
 			System.out.println("Neue Standardfelder hinzugefgt");
 			return Response.status(201).build();
 		} else {
 			System.out.println("Neue Standardfelder wurden nicht hinzugefgt");
 			return Response.status(500).build();
 		}
 	}
 
 	/**
 	 * Gibt ein Modul mit bestimmter Version aus
 	 * 
 	 * @param name
 	 *            Name des Moduls
 	 * @param version
 	 *            Versionsnummer
 	 * @return Modul
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/modul/get/{name}/{version}")
 	public Modul getModul(@PathParam("name") String name, @PathParam("version") String version) {
 		int v = 0;
 		try {
 			v = Integer.parseInt(version);
 		} catch (NumberFormatException nf) {
 			System.out.println("Modul " + name + " ,Version " + version + " nicht erfolgreich abgefragt");
 			return null;
 		}
 		System.out.println("Modul " + name + " ,Version " + version + " abgefragt");
 		return new sql().getModul(name, v);
 	}
 
 	/**
 	 * Gibt alle Modulhandbcher eines Studienganges aus
 	 * 
 	 * @param studiengang
 	 *            Name des Studienganges
 	 * @return Liste von Modulhandbchern
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/modulhandbuch/get/allat/{studiengang}")
 	public ArrayList<Modulhandbuch> getModulhandbuch(@PathParam("studiengang") String studiengang) {
 		System.out.println("Alle Modulhandbcher vom Studiengang " + studiengang + " abgefragt");
 		return new sql().getModulhandbuch(studiengang);
 	}
 
 	@POST
 	@Path("/modulhandbuch/post/accept/")
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response modulhanbuchAccept(Modulhandbuch mh) {
 		int status = new sql().acceptModulHandBuch(mh.getId());
 		if (status == 1) {
 			System.out.println("Modulhandbuch " + mh.toString() + " akzeptiert");
 			return Response.status(201).build();
 		} else {
 			System.out.println("Modulhandbuch " + mh.toString() + " wurde nicht akzeptiert");
 			return Response.status(500).build();
 		}
 	}
 	
 	@POST
 	@Path("/modulhandbuch/post/")
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response setModulhanbuch(Studiengang s) {
 		Modulhandbuch mh = s.getModbuch().get(0);
 		String[] tmp = mh.getJahrgang().split("/");
 		String semester = tmp[0];
 		String jahr = tmp[1];
 		String studiengang =s.getName();
 		String abschluss =s.getAbschluss();
 		int status = new sql().setModulhandbuch(studiengang, abschluss, mh.getPruefungsordnungsjahr(), mh.getProsa(), semester, jahr);
 		if (status == 1) {
 			System.out.println("Modulhandbuch " + mh.toString() + " akzeptiert");
 			return Response.status(201).build();
 		} else {
 			System.out.println("Modulhandbuch " + mh.toString() + " wurde nicht akzeptiert");
 			return Response.status(500).build();
 		}
 	}
 
 	/**
 	 * Gibt an, ob ein Modul in Bearbeitung ist
 	 * 
 	 * @param name
 	 *            Name des Moduls
 	 * @return Status des Moduls
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/modul/get/InEdit/{name}")
 	public String getModulInEdit(@PathParam("name") String name) {
 		System.out.println("Abfrage, ob " + name + " in Bearbeitung ist");
 		boolean b = new sql().getModulInEdit(name);
 		if (b) {
 			return "true";
 		} else {
 			return "false";
 		}
 	}
 
 	/**
 	 * Gibt eine Liste von Modulen aus, die akzeptiert oder nicht akzeptiert
 	 * sind
 	 * 
 	 * @param a
 	 *            Gibt an, ob akzeptiert oder nicht
 	 * @return Liste von Modulen
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/modul/get/List/{accepted}")
 	public ArrayList<Studiengang> getModulList(@PathParam("accepted") String a) {
 		boolean b = false;
 		if (a.equals("true")) {
 			b = true;
 			System.out.println("Alle akzeptierten Module abfragen");
 		} else {
 			System.out.println("Alle nicht akzeptierten Module abfragen");
 		}
 		return new sql().getAllActiveData(b);
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/nachrichten/get/{User}")
 	public ArrayList<Nachricht> getNachrichten(@PathParam("User") String mail){
 		System.out.println("Nachrichten von "+mail+" abgefragt");
 		int id = new sql().getUserID(mail);
 		return new sql().readMessages(id);
 	}
 	
 	@POST
 	@Path("/nachrichten/post/")
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response setNachricht(Nachricht n) {
 		int status = new sql().createMessage(n);
 		System.out.println(n.getDatum());
 		if (status == 1) {
 			System.out.println("Nachricht von " + n.getAbsender() + " hinzugefgt");
 			return Response.status(201).build();
 		} else {
 			System.out.println("Nachricht von " + n.getAbsender() + " nicht hinzugefgt");
 			return Response.status(500).build();
 		}
 	}
 	
 	@DELETE
 	@Path("/nachrichten/delete/{id}")
 	@Consumes(MediaType.APPLICATION_XML)
 	public void deleteNachricht(@PathParam("id") String id) {
 		System.out.println("Nachricht " + id + " wurde gelscht");
 		new sql().deleteNachricht(id);
 	}
 	
 	@PUT
 	@Path("/nachrichten/update/")
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response UpdateNachricht(Nachricht n) {
 		int status = new sql().updateNachricht(n);
 		if (status == 1) {
 			System.out.println("Nachricht " + n.getId() + " aktualisiert");
 			return Response.status(201).build();
 		} else {
 			System.out.println("Nachricht " + n.getId() + " wurde nicht aktualisiert");
 			return Response.status(500).build();
 		}
 	}
 
 
 	/**
 	 * Gibt die neuste Version eines Moduls aus
 	 * 
 	 * @param name
 	 *            Name des Moduls
 	 * @return Versionsnummer
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/modul/get/Version/{name}")
 	public String getModulVersion(@PathParam("name") String name) {
 		System.out.println("Version vom Modul " + name + " abgefragt");
 		return Integer.toString(new sql().getModulVersion(name));
 	}
 
 	/**
 	 * Gibt eine Liste von Modulen aus
 	 * 
 	 * @param studiengang
 	 *            Studiengang des Moduls
 	 * @param modultyp
 	 *            Zuordnung des Moduls
 	 * @param modulhandbuch
 	 *            Jahrgang des Moduls
 	 * @return Liste von Modulen
 	 */
 //	@GET
 //	@Produces(MediaType.APPLICATION_XML)
 //	@Path("/modul/get/selectedModul/{studiengang}/{modultyp}/{modulhandbuch}")
 //	public ArrayList<Modul> getselectedModul(@PathParam("studiengang") String studiengang, @PathParam("modultyp") String modultyp,
 //			@PathParam("modulhandbuch") String modulhandbuch) {
 //		System.out.println("Ausgewhlte Module abfragen");
 //		return new sql().getselectedModul(studiengang, modultyp, modulhandbuch);
 //	}
 
 	/**
 	 * Liefert eine Liste von Stellverteren
 	 * 
 	 * @param mail
 	 *            e-Mail des Users
 	 * @return Liste von Benutzern
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/user/get/stellv/{modul}")
 	public ArrayList<User> getStellv(@PathParam("modul") String modul) {
 		System.out.println("Stellvertreter fr " + modul + " abgefragt");
 		return new sql().getStellv(modul);
 	}
 
 	/**
 	 * Gibt alle Studiengnge aus
 	 * 
 	 * @return Liste von Studiengngen
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/studiengang/get/all/{bool}")
 	public ArrayList<Studiengang> getStudiengaenge(@PathParam("bool") String bool) {
 		System.out.println("Alle Studiengnge abgefragt");
 		if(bool.equalsIgnoreCase("true"))
 			return new sql().getAllActiveData(true);
 		if(bool.equalsIgnoreCase("false"))
 			return new sql().getAllActiveData(false);
 		return null;
 	}
 
 	/**
 	 * Gibt die ID eines Studienganges aus
 	 * 
 	 * @param name
 	 *            Name des Studienganges
 	 * @return ID des Studienganges
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/studiengang/get/ID/{s}")
 	public String getStudiengangID(@PathParam("s") Studiengang s) {
 		System.out.println("Studiengang ID von " + s.getAbschluss() + ", " + s.getName() + " abgefragt");
 		return Integer.toString(new sql().getStudiengangID(s.getName(), s.getAbschluss()));
 	}
 
 	// /**
 	// * Gibt eine Lisete von Zuordnungen aus
 	// *
 	// * @return Liste von Zuordnungen
 	// */
 	// @GET
 	// @Produces(MediaType.APPLICATION_XML)
 	// @Path("/zuordnung/get/List/")
 	// public ArrayList<Zuordnung> getZurdnungen() {
 	// System.out.println("Alle Zuordnungen abfragen");
 	// return new sql().getZuordnungen();
 	// }
 
 	/**
 	 * Akzeptiert ein Modul
 	 * 
 	 * @param m
 	 *            Das Modul
 	 * @return Response Code
 	 */
 	@POST
 	@Path("/modul/post/accept/")
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response modulAccept(Modulhandbuch m) {
 		int status = new sql().acceptModulHandBuch(m.getId());
 		if (status == 1) {
 			System.out.println("Modul " + m.getName() + " akzeptiert");
 			return Response.status(201).build();
 		} else {
 			System.out.println("Modul " + m.getName() + " wurde nicht akzeptiert");
 			return Response.status(500).build();
 		}
 	}
 
 	/**
 	 * Reicht ein Modu ein
 	 * 
 	 * @param m
 	 *            Das Modul
 	 * @return Response Code
 	 */
 	@POST
	@Path("/modul/post/{fach}/{buchid}")
 	@Consumes(MediaType.APPLICATION_XML)
	public Response modulPost(Modul m, @PathParam("fach") String fach, @PathParam("buchid") String buchid) {
		int zws = Integer.parseInt(buchid);
		int status = new sql().setModul(m, fach, zws);
 		if (status == 1) {
 			System.out.println("Modul " + m.getName() + " hinzugefgt");
 			return Response.status(201).build();
 		} else {
 			System.out.println("Modul " + m.getName() + " wurde nicht hinzugefgt");
 			return Response.status(500).build();
 		}
 	}
 
 	/**
 	 * Markiert ein Modul als in Bearbeitung
 	 * 
 	 * @param m
 	 *            Das Modul
 	 * @return Response Code
 	 */
 	@POST
 	@Path("/modul/post/setInEdit/")
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response modulSetInEdit(Modul m) {
 		int status = new sql().setInEdit(m);
 		if (status == 1) {
 			System.out.println("Modul " + m.getName() + " Bearbeitungsstatus gendert");
 			return Response.status(201).build();
 		} else {
 			System.out.println("Modul " + m.getName() + " Bearbeitungsstatus wurde nicht gendert");
 			return Response.status(500).build();
 		}
 	}
 
 	/**
 	 * Reicht eine Liste von Stellvertretern ein
 	 * 
 	 * @param sl
 	 *            Liste von Stellvertretern
 	 * @return Response COde
 	 */
 	@POST
 	@Path("/user/post/stellv/")
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response stellvPost(StellvertreterList sl) {
 		int status = new sql().setStellvertreter(sl.getUsr(), sl.getModul());
 		if (status == 1) {
 			System.out.println("Stellvertreter fr " + sl.getModul() + " aktualisiert");
 			return Response.status(201).build();
 		} else {
 			System.out.println("Stellvertreter fr " + sl.getModul() + " wurde nicht aktualisiert");
 			return Response.status(500).build();
 		}
 	}
 
 	/**
 	 * Reicht einen Studiengang ein
 	 * 
 	 * @param s
 	 *            Studiengang, der angelegt werden soll
 	 * @return Response Code
 	 */
 	@POST
 	@Path("/studiengang/post/")
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response studiengangPost(Studiengang s) {
 		int status = new sql().setStudiengang(s.getName(), s.getAbschluss());
 		if (status == 1) {
 			System.out.println("Studiengang " + s.getAbschluss() + ", " + s.getName() + " hinzugefgt");
 			return Response.status(201).build();
 		} else {
 			System.out.println("Studiengang " + s.getAbschluss() + ", " + s.getName() + " wurde nicht hinzugefgt");
 			return Response.status(500).build();
 		}
 	}
 
 	/**
 	 * Prft, ob ein User exsistiert
 	 * 
 	 * @param mail
 	 *            e-Mail des Users
 	 * @return Reponse Code
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/user/get/{user}")
 	public Response userCheck(@PathParam("user") String mail) {
 		System.out.println("User " + mail + " abgefragt");
 		int status = new sql().getUser(mail);
 		if (status == 1)
 			return Response.status(201).build();
 		else
 			return Response.status(500).build();
 	}
 
 	/**
 	 * Lscht einen Benutzer
 	 * 
 	 * @param mail
 	 *            e-Mail des Benutzers
 	 */
 	@DELETE
 	@Path("/user/delete/{mail}")
 	@Consumes(MediaType.APPLICATION_XML)
 	public void userDelete(@PathParam("mail") String mail) {
 		System.out.println("User " + mail + " wurde gelscht");
 		new sql().deluser(mail);
 	}
 
 	/**
 	 * login Funktion
 	 * 
 	 * @param user
 	 *            e-Mail des Users
 	 * @param pass
 	 *            Passwort des User
 	 * @return eingeloggter User
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/login/{user}/{pass}")
 	public User userLogin(@PathParam("user") String user, @PathParam("pass") String pass) {
 		if (user.equals("gast@gast.gast")) {
 			System.out.println("Gast hat sich angemeldet");
 		} else {
 			System.out.println("User " + user + " hat sich angemeldet");
 		}
 		User tmp = new sql().getUser(user, pass);
 		if (tmp == null)
 			return new User();
 		else
 			return tmp;
 	}
 
 	/**
 	 * Reicht einen User ein
 	 * 
 	 * @param user
 	 *            der User
 	 * @return Response Code
 	 */
 	@POST
 	@Path("/user/post/")
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response userPost(User user) {
 		int status = new sql().usersave(user);
 		if (status == 1) {
 			System.out.println("Neuer User " + user.geteMail() + " hinzugefgt");
 			return Response.status(201).build();
 		} else {
 			System.out.println("Neuer User " + user.geteMail() + " wurde nicht hinzugefgt");
 			return Response.status(500).build();
 		}
 	}
 
 	/**
 	 * Fragt eine Userrelation ab
 	 * 
 	 * @param eMail
 	 *            e-Mail des Users
 	 * @return Userrelation
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/user/get/Relation/{user}")
 	public ArrayList<Modul> userRelation(@PathParam("user") String eMail) {
 		System.out.println("Stellvertreter und Vorgesetzte von " + eMail + " abgefragt");
 		return new sql().getUserModulRelation(eMail);
 	}
 
 	/**
 	 * Updated einen User
 	 * 
 	 * @param uuc
 	 *            Container mit User und alter e-Mail des Users
 	 * @return Response Code
 	 */
 	@POST
 	@Path("/user/update/")
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response userUpdate(UserUpdateContainer uuc) {
 		int status = new sql().userupdate(uuc.getUser(), uuc.getEmail());
 		if (status == 1) {
 			System.out.println("User " + uuc.getEmail() + " aktualisiert");
 			return Response.status(201).build();
 		} else {
 			System.out.println("User " + uuc.getEmail() + " wurde nicht aktualisiert");
 			return Response.status(500).build();
 		}
 	}
 
 //	/**
 //	 * Reicht eine Zuordnung ein
 //	 * 
 //	 * @param z
 //	 *            Zuordnung
 //	 * @return Response Code
 //	 */
 //	@POST
 //	@Path("/zuordnung/post/")
 //	@Consumes(MediaType.APPLICATION_XML)
 //	public Response zuordnungPost(Zuordnung z) {
 //		int status = new sql().setZuordnung(z);
 //		if (status == 1) {
 //			System.out.println("Zuordnung " + z.getName() + " hinzugefgt");
 //			return Response.status(201).build();
 //		} else {
 //			System.out.println("Zuordnung " + z.getName() + " nicht hinzugefgt");
 //			return Response.status(500).build();
 //		}
 //	}
 
 	
 	@POST
 	@Path("/datum/set")
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response setDatum(String d) {
 		long l = Long.parseLong(d);
 		Date date = new Date(l);
 		int status = new sql().setDate(date);
 		if (status == 1) {
 			System.out.println("Deadline hinzugefgt");
 			return Response.status(201).build();
 		} else {
 			System.out.println("Deadline nicht hinzugefgt");
 			return Response.status(500).build();
 		}
 	}
 	
 	
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/datum/get")
 	public String getDatum() {
 		System.out.println("Deadline abgefragt");
 		return Long.toString(new sql().getDate().getTime());
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/modverwa/getall/{modname}")
 	public ArrayList<ArrayList<User>> getModVerwa(@PathParam("modname") String modname) {
 		System.out.println("Modulverwalter des "+modname+" abgefragt");
 		return new sql().getVerwalterLis(modname);
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/modnames/getall")
 	public ArrayList<String> getallModulnames() {
 		System.out.println("Modulnamen abgefragt");
 		return new sql().getallModulnames();
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/po/get")
 	public ArrayList<pordnung> getPOs() {
 		System.out.println("Prfungsordnungen abgefragt");
 		return new sql().getallpo();
 	}
 	
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/fach/get")
 	public ArrayList<String> getFach(){
 		System.out.println("Fcher abgefragt");
 		return new sql().getFach();
 	}
 	
 }
