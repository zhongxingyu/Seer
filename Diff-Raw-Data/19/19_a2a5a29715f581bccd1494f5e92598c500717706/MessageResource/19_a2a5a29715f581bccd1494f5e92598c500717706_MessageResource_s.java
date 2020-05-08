 package de.team55.mms.server.ctrl;
 
 import java.util.ArrayList;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import de.team55.mms.data.Modul;
 import de.team55.mms.data.Modulhandbuch;
 import de.team55.mms.data.StellvertreterList;
 import de.team55.mms.data.Studiengang;
 import de.team55.mms.data.User;
 import de.team55.mms.data.UserRelation;
 import de.team55.mms.data.UserUpdateContainer;
 import de.team55.mms.data.Zuordnung;
 import de.team55.mms.server.db.sql;
 
 @Path("")
 public class MessageResource {
 
 	/**
 	 * returns a User
 	 * 
 	 * @param user
 	 *            e-Mail of the User
 	 * @param pass
 	 *            Password of the User
 	 * @return Data of User
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/login/{user}/{pass}")
 	public User userLogin(@PathParam("user") String user, @PathParam("pass") String pass) {
 		if(user.equals("gast@gast.gast")){
 			System.out.println("User " + user + " hat sich angemeldet");
 		} else{
 			System.out.println("Gast hat sich angemeldet");
 		}
 		User tmp = new sql().getUser(user, pass);
 		if (tmp == null)
 			return new User();
 		else
 			return tmp;
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/user/get/{user}")
 	public Response userLogin(@PathParam("user") String mail) {
 		System.out.println("User " + mail + " abgefragt");
 		int status = new sql().getUser(mail);
 		if (status == 1)
 			return Response.status(201).build();
 		else
 			return Response.status(500).build();
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/user/getRelation/{user}")
 	public UserRelation userRelation(@PathParam("user") String eMail) {
 		System.out.println("Stellvertreter und Vorgesetzte von " + eMail + " abgefragt");
 		return new UserRelation(new sql().getUserRelation(eMail));
 	}
 
 	/**
 	 * returns all Users
 	 * 
 	 * @return List with Data of all Users
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/user/getall")
 	public ArrayList<User> getAllUsers() {
 		System.out.println("Userliste abgefragt");
 		return new sql().userload();
 	}
 
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
 
 	@POST
 	@Path("/user/stellv/post/")
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response stellvPost(StellvertreterList sl) {
 		int status = new sql().setStellvertreter(sl);
 		if (status == 1) {
 			System.out.println("Stellvertreter fr " + sl.geteMail() + " aktualisiert");
 			return Response.status(201).build();
 		} else {
 			System.out.println("Stellvertreter fr " + sl.geteMail() + " wurde nicht aktualisiert");
 			return Response.status(500).build();
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/user/stellv/{user}")
 	public ArrayList<User> getStellv(@PathParam("user") String mail) {
 		System.out.println("Stellvertreter und Vorgesetzte von " + mail + " abgefragt");
 		return new sql().getStellv(mail);
 	}
 
 	@DELETE
 	@Path("/user/delete/{mail}")
 	@Consumes(MediaType.APPLICATION_XML)
 	public void userDelete(@PathParam("mail") String mail) {
 		System.out.println("User " + mail + " wurde gelscht");
 		new sql().deluser(mail);
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/modul/getVersion/{name}")
 	public String getModulVersion(@PathParam("name") String name) {
 		System.out.println("Version vom Modul " + name + " abgefragt");
 		return Integer.toString(new sql().getModulVersion(name));
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/modul/get/{name}")
 	public Modul getModul(@PathParam("name") String name) {
 		System.out.println("Modul " + name + " abgefragt");
 		return new sql().getModul(name);
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/modul/getList/{accepted}")
 	public ArrayList<Modul> getModulList(@PathParam("accepted") String a) {
 		boolean b = false;
 		if (a.equals("true")) {
 			b = true;
 			System.out.println("Alle akzeptierten Module abfragen");
 		} else {
 			System.out.println("Alle nicht akzeptierten Module abfragen");
 		}
 		return new sql().getModule(b);
 	}
 
 	@POST
 	@Path("/modul/post/")
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response modulPost(Modul m) {
 		int status = new sql().setModul(m);
 		if (status == 1) {
 			System.out.println("Modul " + m.getName() + " hinzugefgt");
 			return Response.status(201).build();
 		} else {
 			System.out.println("Modul " + m.getName() + " wurde nicht hinzugefgt");
 			return Response.status(500).build();
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/zuordnung/getList/")
 	public ArrayList<Zuordnung> getZurdnungen() {
 		System.out.println("Alle Zuordnungen abfragen");
 		return new sql().getZuordnungen();
 	}
 
 	@POST
 	@Path("/zuordnung/post/")
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response zuordnungPost(Zuordnung z) {
 		int status = new sql().setZuordnung(z);
 		if (status == 1) {
 			System.out.println("Zuordnung " + z.getName() + " hinzugefgt");
 			return Response.status(201).build();
 		} else {
 			System.out.println("Zuordnung " + z.getName() + " nicht hinzugefgt");
 			return Response.status(500).build();
 		}
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/studiengang/getID/{name}")
 	public String getStudiengangID(@PathParam("name") String name) {
 		System.out.println("Studiengang ID von " + name + " abgefragt");
 		return Integer.toString(new sql().getStudiengangID(name));
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/modulhandbuch/getallat/{studiengang}")
 	public ArrayList<Modulhandbuch> getModulhandbuch(@PathParam("studiengang") String studiengang) {
 		System.out.println("Alle Modulhandbcher vom Studiengang " + studiengang + " abgefragt");
 		return new sql().getModulhandbuch(studiengang);
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/studiengang/getall")
 	public ArrayList<Studiengang> getStudiengaenge() {
 		System.out.println("Alle Studiengnge abgefragt");
 		return new sql().getStudiengaenge();
 	}
 
 	@POST
 	@Path("/studiengang/post/")
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response studiengangPost(String name) {
 		int status = new sql().setStudiengang(name);
 		if (status == 1) {
 			System.out.println("Studiengang " + name + " hinzugefgt");
 			return Response.status(201).build();
 		} else {
 			System.out.println("Studiengang " + name + " wurde nicht hinzugefgt");
 			return Response.status(500).build();
 		}
 	}
 	
	@POST
 	@Produces(MediaType.APPLICATION_XML)
 	@Path("/modul/getselectedModul/{studiengang}/{modultyp}/{modulhandbuch}")
 	public ArrayList<Modul> getselectedModul(@PathParam("studiengang") String studiengang, @PathParam("modultyp") String modultyp, @PathParam("modulhandbuch") String modulhandbuch){
 		System.out.println("Ausgewhlte Module abfragen");
 		return new sql().getselectedModul(modulhandbuch, modultyp, modulhandbuch);
 	}
 
 }
