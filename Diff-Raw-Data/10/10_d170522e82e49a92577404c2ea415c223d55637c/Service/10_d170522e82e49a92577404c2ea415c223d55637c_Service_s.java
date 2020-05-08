 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package service;
 
 import api.Api;
 import dao.*;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.List;
 import modele.*;
 import modele.admin.Client;
 import serverghome.ServerGHome;
 import util.JpaUtil;
 
 /**
  *
  * @author anis
  */
 public class Service {
 
     protected Api monApi;
 // Méthodes relatives aux Clicks
 
     public void addClick(Click aClick) throws Exception {
         JpaUtil.openEntityManager();
         JpaUtil.getEntityManagerTransaction().begin();
         ClickDao hisData = new ClickDao();
         hisData.create(aClick);
         JpaUtil.getEntityManagerTransaction().commit();
         JpaUtil.closeEntityManager();
     }
 
     public int getNbClick() {
         ClickDao hisData = new ClickDao();
         JpaUtil.openEntityManager();
         int nbClick = hisData.getNBClick();
         JpaUtil.closeEntityManager();
         return nbClick;
     }
 
     // Ajout d'un client
     public void addClient(String id, String mdp, Client.ClientType t) throws Exception {
         ClientDao hisClient = new ClientDao();
         JpaUtil.openEntityManager();
         JpaUtil.getEntityManagerTransaction().begin();
         hisClient.create(id, mdp, t);
         JpaUtil.getEntityManagerTransaction().commit();
         JpaUtil.closeEntityManager();
     }
     // Ajout d'un client
 
     public void addClient(String id, String mdp, String t) throws Exception {
         ClientDao hisClient = new ClientDao();
         JpaUtil.openEntityManager();
         JpaUtil.getEntityManagerTransaction().begin();
         hisClient.create(id, mdp, t);
         JpaUtil.getEntityManagerTransaction().commit();
         JpaUtil.closeEntityManager();
     }
 
     public boolean testClient(String id, String mdp) throws Exception {
         ClientDao hisClient = new ClientDao();
         JpaUtil.openEntityManager();
         boolean value = hisClient.testIfRegistred(id, mdp);
         JpaUtil.closeEntityManager();
         return value;
 
     }
 
     public String getData(String idCapteur, String typeData) {
 
 
         // Ouverture de l'entity manager
         JpaUtil.openEntityManager();
 
         //Dans le cas ou nous sommes dans le cas d'une data de type Temperature
         if (typeData.contains("T")) {
             TempDao td = new TempDao();
             try {
                 JpaUtil.openEntityManager();
                 Temperature T = td.getLastTemp(idCapteur);
 
                 if (T != null) {
                     JpaUtil.closeEntityManager();
 
                     return "G1 " + T.getValue();
                 } else {
                     JpaUtil.closeEntityManager();
                     return "G0 NOTINIT";
                 }
             } catch (Exception ex) {
                 JpaUtil.closeEntityManager();
                 System.out.println("Error while looking for temperature");
                 return "G0 TNOTFOUND";
             }
         } // Dans le cas ou nous sommes dans une data de type click
         else if (typeData.contains("C")) {
 
             ClickDao cd = new ClickDao();
             try {
                 Click c = cd.getLastClick(idCapteur);
                 if (c != null) {
                     JpaUtil.closeEntityManager();
 
                     return "G1 " + c.getValue();
                 } else {
                     JpaUtil.closeEntityManager();
                     return "G0 UNKNOWN";
                 }
 
             } catch (Exception ex) {
                 JpaUtil.closeEntityManager();
                 return "G0 CNOTFOUND";
             }
         } // Dans le cas ou nous sommes dans le cas d'un contact
         else if (typeData.contains("O")) {
             ContactDao cd = new ContactDao();
             try {
                 Contact c = cd.getIsInContact(idCapteur);
                 if (c != null) {
                     return "G1 " + c.getValue();
                 } else {
                     JpaUtil.closeEntityManager();
                     return "G0 UNKNOWN";
                 }
 
             } catch (Exception ex) {
                 JpaUtil.closeEntityManager();
                 return "G0 CNOTFOUND";
             }
         } // Dans le cas ou nous sommes dans le cas d'une data présence
         else if (typeData.contains("P")) {
             PresenceDao cd = new PresenceDao();
             try {
                 Presence c = cd.getPresence(idCapteur);
                 if (c != null) {
                     JpaUtil.closeEntityManager();
                     return "G1 " + c.isValue();
                 } else {
                     JpaUtil.closeEntityManager();
                     return "G0 UNKNOWN";
                 }
 
             } catch (Exception ex) {
                 JpaUtil.closeEntityManager();
                 return "G0 PNOTFOUND";
             }
         } // Dans le cas ou nous sommes dans le cas d'une data luminosité
         else if (typeData.contains("L")) {
             LuminositeDao ld = new LuminositeDao();
             try {
                 Luminosite c = ld.getLastLumi(idCapteur);
                 if (c != null) {
                     JpaUtil.closeEntityManager();
                     return "G1 " + c.getValue();
                 } else {
                     JpaUtil.closeEntityManager();
                     return "G0 UNKNOWN";
                 }
 
             } catch (Exception ex) {
                 return "G0 PNOTFOUND";
             }
         } // Dans le cas ou nous sommes dans le cas d'une data tension
         else if (typeData.contains("V")) {
             TensionDao td = new TensionDao();
             try {
                 Tension c = td.getLastTens(idCapteur);
                 if (c != null) {
                     JpaUtil.closeEntityManager();
                     return "G1 " + c.getValue();
                 } else {
                     JpaUtil.closeEntityManager();
                     return "G0 UNKNOWN";
                 }
 
             } catch (Exception ex) {
                 JpaUtil.closeEntityManager();
                 return "G0 PNOTFOUND";
             }
         }
         JpaUtil.closeEntityManager();
         return "G0 NOTFOUND";
     }
 
     public void sendOrder(String idCapteur, String typeData, String optionalValue, ServerGHome s) {
         Api a = s.getApi();
         a.Actionner(typeData, "FF9F1E05", optionalValue);
     }
 
     public void manageData(String typeData, String idCapteur, String value) throws Exception {
         // Ouverture de l'entity manager
         JpaUtil.openEntityManager();
         JpaUtil.getEntityManagerTransaction().begin();
         if (typeData.contains("T")) {
 
             Temperature temp = new Temperature(idCapteur, Float.valueOf(value));
             TempDao td = new TempDao();
             try {
                 td.create(temp);
             } catch (Exception ex) {
             }
         } else if (typeData.contains("C")) {
 
             Click c = new Click(idCapteur, Integer.valueOf(value));
             ClickDao cd = new ClickDao();
             try {
                 cd.create(c);
             } catch (Exception ex) {
             }
         } else if (typeData.contains("O")) {
 
             Contact c = new Contact(idCapteur, Integer.valueOf(value));
             ContactDao cd = new ContactDao();
             try {
                 cd.create(c);
             } catch (Exception ex) {
             }
         } else if (typeData.contains("P")) {
 
             Presence c = new Presence(idCapteur, Integer.valueOf(value));
             PresenceDao cd = new PresenceDao();
             try {
                 cd.create(c);
             } catch (Exception ex) {
             }
         } else if (typeData.contains("L")) {
 
             Luminosite l = new Luminosite(idCapteur, Float.valueOf(value));
             LuminositeDao ld = new LuminositeDao();
             try {
                 ld.create(l);
             } catch (Exception ex) {
             }
         } else if (typeData.contains("V")) {
 
             Tension c = new Tension(idCapteur, Float.valueOf(value));
             TensionDao cd = new TensionDao();
             try {
                 cd.create(c);
             } catch (Exception ex) {
             }
         }
         JpaUtil.getEntityManagerTransaction().commit();
         JpaUtil.closeEntityManager();
     }
 
     public boolean checkIDAvailable(String id) {
         JpaUtil.openEntityManager();
         ClientDao cd = new ClientDao();
         boolean v = !cd.checkIDExists(id);
         JpaUtil.closeEntityManager();
         return v;
     }
 
     public List<Client> getAllUsers() {
         JpaUtil.openEntityManager();
         ClientDao cd = new ClientDao();
         List<Client> v = cd.getAll();
         JpaUtil.closeEntityManager();
         return v;
     }
 
     public void deleteUser(String id) throws Exception {
         JpaUtil.openEntityManager();
         JpaUtil.getEntityManagerTransaction().begin();
 
         ClientDao cd = new ClientDao();
         cd.deleteUser(id);
         JpaUtil.getEntityManagerTransaction().commit();
 
         JpaUtil.closeEntityManager();
     }
 
     public String getMusic() {
         try {
             File repertoire = new File("../music");
             File[] files = repertoire.listFiles();
             System.out.println(files.length);
             String fs = "V1 " + (new Integer(files.length)).toString();
             for (File f : files) {
                 fs += (" \"" + f.getName() + "\"");
             }
 
             return fs;
         } catch (Exception ex) {
             return "V0 NOSUCHDIRECTORY";
         }
     }
 
     
         public String getWeather() throws UnsupportedEncodingException, MalformedURLException, IOException {
         String result="W1";
         String data = URLEncoder.encode("ptrigger2", "UTF-8") + "=" + URLEncoder.encode("10027", "UTF-8");
  
         // Send data
         URL url = new URL("http://weather.yahooapis.com/forecastrss?w=609125&u=c");
         URLConnection conn = url.openConnection();
         conn.setDoOutput(true);
         OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
         wr.write(data);
         wr.flush();
  
         // Get the response
         BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         String line;
         while ((line = rd.readLine()) != null) {
             if(line.contains("<yweather:condition"))
             {
                 result+=" "+line.split("\"")[1];
                 result+=" "+line.split("\"")[5];
 
             }
         }
         wr.close();
         rd.close();
    //} catch (Exception e) {
    //}
         return result;
     }
 
         
         
     public String playMusic(String name) {
         String path="";
         try {
             BufferedReader br = new BufferedReader(new FileReader("../conf.GHOME"));
             try {
                 String line = br.readLine();
 
                 while (line.contains("#VLC")) {
                     line = br.readLine();
                 }
                 path = br.readLine();
             } finally {
                 br.close();
             }
             ProcessBuilder pb = new ProcessBuilder(path, "../music/" + name);
             Process start = pb.start();
             return "P1";
         } catch (Exception ex) {
             ex.printStackTrace();
             return "P0 ERROR";
         }
     }
 }
