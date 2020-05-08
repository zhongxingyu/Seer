 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.tud.cs.tk.zeiterfassung.ws;
 
 import de.tud.cs.tk.zeiterfassung.PojoMapper;
 import de.tud.cs.tk.zeiterfassung.dao.FachgebietDAO;
 import de.tud.cs.tk.zeiterfassung.dao.PersonDAO;
 import de.tud.cs.tk.zeiterfassung.entities.Fachgebiet;
 import de.tud.cs.tk.zeiterfassung.entities.Person;
 import de.tud.cs.tk.zeiterfassung.jopenid.OpenIdPrincipal;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 
 /**
  *
  * @author letzkus
  */
 @Path("/fachgebiete")
 public class Fachgebiete {
 
     public class FachgebietEntry {
 
         public String name, leiter, stellvertreter;
         public int budget;
         public long id;
 
         public FachgebietEntry(long id, String name, String leiter, String stellvertreter, int budget) {
             this.id = id;
             this.name = name;
             this.leiter = leiter;
             this.stellvertreter = stellvertreter;
             this.budget = budget;
         }
     }
 
     public class FachgebietList {
 
         public boolean success;
         public int total;
         public ArrayList<FachgebietEntry> results = new ArrayList<FachgebietEntry>();
     }
 
     @GET
     @Produces({"text/javascript", "application/json"})
     public String processGetList(@Context HttpServletRequest req, @Context HttpServletResponse resp) {
         FachgebietList fgs = getList(req);
         String cb = req.getParameter("callback");
         String result = "";
         try {
             result = PojoMapper.toJson(fgs, true);
         } catch (JsonMappingException ex) {
             Logger.getLogger(Personen.class.getName()).log(Level.SEVERE, null, ex);
         } catch (JsonGenerationException ex) {
             Logger.getLogger(Personen.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(Personen.class.getName()).log(Level.SEVERE, null, ex);
         }
         if (cb != null) {
             resp.setContentType("text/javascript");
             return cb + "(" + result + ");";
         }
         resp.setContentType("application/json");
         return result;
     }
 
     public FachgebietList getList(@Context HttpServletRequest req) {
         OpenIdPrincipal principal = null;
         if (req.getUserPrincipal() instanceof OpenIdPrincipal) {
             principal = (OpenIdPrincipal) req.getUserPrincipal();
         }
         List<Fachgebiet> fgDAO = FachgebietDAO.retrieveAll();
         FachgebietList result = new FachgebietList();
         result.success = false;
         result.total = 0;
         if (principal != null) { // Benutzer ist per OpenID identifiziert
             List<Person> people = PersonDAO.findByPrincipal(principal.getIdentity());
            if (people != null && people.size() != 1) {
                 return result;
             }
             Person me = people.get(0);
             Person leiter, stellv;
             String sleiter, sstellv;
             for (Fachgebiet fg : fgDAO) {
                 if (me.getFachgebiet().id == fg.id || me.getRolle().significance <= 10) {
                     leiter = PersonDAO.retrieve(fg.leiter);
                     stellv = PersonDAO.retrieve(fg.stellv);
                     result.results.add(new FachgebietEntry(fg.id, fg.name, (leiter != null) ? leiter.firstName + " " + leiter.givenName : null, (stellv != null) ? stellv.firstName + " " + stellv.givenName : null, fg.budget));
                 }
             }
             result.success = true;
             result.total = result.results.size();
         }
         return result;
     }
 
     @POST
     @Path("/")
     @Consumes("application/json")
     public long addFachgebiet(@Context HttpServletRequest req, @QueryParam("name") String name, @QueryParam("budget") int budget, @QueryParam("leiter") String leiter, @QueryParam("stellv") String stellv) {
         OpenIdPrincipal principal = null;
         if (req.getUserPrincipal() instanceof OpenIdPrincipal) {
             principal = (OpenIdPrincipal) req.getUserPrincipal();
         }
         if (principal != null) { // Benutzer ist per OpenID identifiziert
             List<Person> people = PersonDAO.findByPrincipal(principal.getIdentity());
             if (people != null && people.size() != 1 && people.get(0).getRolle().significance > 10) {
                 return -1;
             }
             Person me = people.get(0);
             Fachgebiet f = new Fachgebiet();
             // Name zuweisen
             f.name = name;
             // Id des Leiters zuweisen
             String firstName = "", givenName = "";
             String[] names = leiter.split(" ");
             if (names.length >= 1) {
                 firstName = names[0];
                 if (names.length >= 2) {
                     givenName = names[1];
                     if (names.length >= 3) {
                         for (int i = 2; i < names.length - 3; i++) {
                             givenName = givenName.concat(names[i]);
                         }
                     }
                 }
             }
 
 
             List<Person> ps = PersonDAO.findByName(firstName, givenName);
             if (ps != null && ps.size() == 1) {
                 f.leiter = ps.get(0).id;
             } else {
                 f.leiter = -1;
             }
 
             // Id des Stellvertreters zuweisen
             names = stellv.split(" ");
             if (names.length >= 1) {
                 firstName = names[0];
                 if (names.length >= 2) {
                     givenName = names[1];
                     if (names.length >= 3) {
                         for (int i = 2; i < names.length - 3; i++) {
                             givenName = givenName.concat(names[i]);
                         }
                     }
                 }
             }
 
 
             ps = PersonDAO.findByName(firstName, givenName);
             if (ps != null && ps.size() == 1) {
                 f.stellv = ps.get(0).id;
             } else {
                 f.stellv = -1;
             }
 
             f.budget = budget;
             
             return FachgebietDAO.create(f);
         }
         return -1;
     }
 }
