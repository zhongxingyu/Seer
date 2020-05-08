 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.tud.cs.tk.zeiterfassung.ws;
 
 import de.tud.cs.tk.zeiterfassung.PojoMapper;
import de.tud.cs.tk.zeiterfassung.dao.AufgabeDAO;
 import de.tud.cs.tk.zeiterfassung.dao.FachgebietDAO;
 import de.tud.cs.tk.zeiterfassung.dao.PersonDAO;
import de.tud.cs.tk.zeiterfassung.entities.Aufgabe;
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
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
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
 
     @GET
     @Produces({"text/javascript", "application/json"})
     public String processGetList(@Context HttpServletRequest req, @Context HttpServletResponse resp) {
         ResultSet<FachgebietEntry> fgs = getList(req);
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
 
     public ResultSet<FachgebietEntry> getList(@Context HttpServletRequest req) {
         OpenIdPrincipal principal = null;
         if (req.getUserPrincipal() instanceof OpenIdPrincipal) {
             principal = (OpenIdPrincipal) req.getUserPrincipal();
         }
         List<Fachgebiet> fgDAO = FachgebietDAO.retrieveAll();
         ResultSet<FachgebietEntry> result = new ResultSet<FachgebietEntry>();
         result.results = new ArrayList<FachgebietEntry>();
         result.success = false;
         result.total = 0;
         if (principal != null) { // Benutzer ist per OpenID identifiziert
             List<Person> people = PersonDAO.findByPrincipal(principal.getIdentity());
             if (people == null || people.size() != 1) {
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
 
     /*
      * Example: 
      * ws/fachgebiete/insert?authorID=5&edFachgebiet=Telekoop&edKuerzel=TK&edBudget=4&id=&name=&budget=&leiter=&stellv=&callback=Ext.data.JsonP.callback19 HTTP/1.1 
      */
     @GET
     @Path("/insert")
     public void addFachgebiet(@Context HttpServletRequest req, @QueryParam("authorID") String admin, @QueryParam("name") String name, @QueryParam("budget") int budget, @QueryParam("leiter") long leiter, @QueryParam("stellv") long stellv) {
         Fachgebiet f = new Fachgebiet();
         // Name zuweisen
         f.name = name;
         // Id des Leiters zuweisen
         f.leiter = leiter;
         f.stellv = stellv;
 
         f.budget = budget;
 
         FachgebietDAO.create(f);
     }
 
     @GET
     @Path("/update")
     public void updateFachgebiet(@QueryParam("authorID") String admin, @QueryParam("name") String name, @QueryParam("budget") int budget, @QueryParam("leiter") long leiter, @QueryParam("stellv") long stellv, @QueryParam("id") long id) {
         Fachgebiet f = FachgebietDAO.retrieve(id);
         if(f==null) {
             return;
         }
         if(name!=null && !name.equals("")) {
             f.name = name;
         }
         if(leiter!=f.leiter) { // Was ist, wenn neuer Leiter = Administrator??
             f.leiter = leiter;
         }
         if(stellv!=f.stellv) {
             f.stellv = stellv;
         }
         if(budget!=f.budget) {
             f.budget = budget;
         }
         FachgebietDAO.update(f);
     }
     
     @GET
     @Path("/remove")
     public void deleteFachgebiet(@QueryParam("id") long id) {
         Fachgebiet f = FachgebietDAO.retrieve(id);
         if(f!=null) {
             FachgebietDAO.delete(f);
 //            for(Person p : f.people) {
 //                PersonDAO.delete(p);
 //                for(Aufgabe a : p.getAufgaben()) {
 //                    AufgabeDAO.delete(a);
 //                }
 //            }
         }
         
         
     }
 }
