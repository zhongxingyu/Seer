 package Tippspiel;
 
 
 
 import javax.ws.rs.*;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import java.io.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Oli
  * Date: 08.05.13
  * Time: 13:16
  * To change this template use File | Settings | File Templates.
  */
 public class TippspielService {
 
     @Path("/test/")
     @GET
 
     public void getTippspiel(@PathParam("tipp/user") String tipp) throws JAXBException, IOException {
 
         JAXBContext jc = JAXBContext.newInstance(Tippspiel.class);
         Unmarshaller unmarshaller = jc.createUnmarshaller();
         Tippspiel tippspiel = (Tippspiel) unmarshaller.unmarshal(new File("/Users/Oli/git/WBA2_SS13_Phase2/WBA2_Phase2/src/Tippspiel/Tippspiel.xml"));
 
         int i = 0;
         int laufen = 0;
         String ein;
         while (laufen != 1) {
             ein = tippspiel.getTipp().get(i).getName();
             if (ein.equalsIgnoreCase(tipp)) {
                // Schleife benötigt
                 for (int j = 0; j < tippspiel.getTipp().size(); j++) {
                     System.out.println("Heimmannschaft: " + tippspiel.getTipp().get(i).getSpiel().getHeimmannschaft());
                     System.out.println("Gastmannschaft: " + tippspiel.getTipp().get(i).getSpiel().getGastmannschaft());
                     System.out.println("Ergebnis: " + tippspiel.getTipp().get(i).getSpiel().getErgebnis());
                     laufen = 1;
                 }
 
             } else {
                 i++;
 
             }
 
         }
 
     }
 
     @Path("/test/")
     @GET
 
     public void getErgebnis(@PathParam("tipp/ergebnis") String erg) throws JAXBException, IOException {
 
         JAXBContext jc = JAXBContext.newInstance(Tippspiel.class);
         Unmarshaller unmarshaller = jc.createUnmarshaller();
         Tippspiel tippspiel = (Tippspiel) unmarshaller.unmarshal(new File("/Users/Oli/git/WBA2_SS13_Phase2/WBA2_Phase2/src/Tippspiel/Tippspiel.xml"));
 
         int i = 0;
         int laufen = 0;
         String ein;
         while (laufen != 1) {
             ein = tippspiel.getTipp().get(i).getName();
             for (int j = 0; j < tippspiel.getTipp().size(); j++) {
                 if (tippspiel.getTipp().get(i).getSpiel().getErgebnis().compareTo(erg) == 0) {
                     System.out.println("Tipper: " + ein);
                     laufen = 1;
                 } else {
                     j++;
 
                 }
             }
         }
 
     }
 
     @POST
     @Path("tippspiel/user")
     @Produces("application/xml")
     public Tippspiel newTipp(@FormParam("Name") String name, @FormParam("Heimmannschaft") String heim, @FormParam("Gastmannschaft") String gast, @FormParam("Ergebnis") String erg) throws JAXBException, IOException {
 
         Tippspiel tippspiel = new Tippspiel();
         Tippspiel.Tipp tipp = new Tippspiel.Tipp();
 
 
 
         Tippspiel.Tipp.Spiel spiel = new Tippspiel.Tipp.Spiel();
 
         tipp.setSpiel(spiel);
         tipp.setName(name);
         spiel.setHeimmannschaft(heim);
         spiel.setGastmannschaft(gast);
         spiel.setErgebnis(erg);
 
 
         ObjectFactory ob = new ObjectFactory();
         tippspiel = ob.createTippspiel();
         JAXBContext context = JAXBContext.newInstance(Tippspiel.class);
         Unmarshaller um = context.createUnmarshaller();
         tippspiel = (Tippspiel) um.unmarshal(new FileReader("/Users/Oli/git/WBA2_SS13_Phase2/WBA2_Phase2/src/Tippspiel/Tippspiel_Test.xml"));

         tippspiel.getTipp().add(tippspiel.getTipp().size(), tipp);
 
         Marshaller m = context.createMarshaller();
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         m.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");
         m.marshal(tippspiel, System.out);
 
         Writer w = new FileWriter("/Users/Oli/git/WBA2_SS13_Phase2/WBA2_Phase2/src/Tippspiel/Tippspiel_Test.xml");
         m.marshal(tippspiel, w);
         w.close();
 
         return tippspiel;
 
     }
 
 
 
 
     @DELETE
     @Path("tippspiel/user")
     @Produces("application/xml")
     public Tippspiel deleteTipp(@PathParam("user") String x) throws JAXBException, IOException {
         ObjectFactory ob = new ObjectFactory();
         Tippspiel ts = ob.createTippspiel();
         JAXBContext context = JAXBContext.newInstance(Tippspiel.class);
         Unmarshaller um = context.createUnmarshaller();
         Tippspiel tippspiel = (Tippspiel) um.unmarshal(new FileReader("/Users/Oli/git/WBA2_SS13_Phase2/WBA2_Phase2/src/Tippspiel/Tippspiel_Test.xml"));
         int i = 0;
        if (tippspiel.getTipp().get(i).getName().equalsIgnoreCase(x)) {
 
             ts.getTipp().addAll(tippspiel.getTipp());
             ts.getTipp().remove(tippspiel.getTipp().get(i));
 

             // Marshall content to XML-File.
             Marshaller m = context.createMarshaller();
             m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
             m.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");
             m.marshal(ts, System.out);
 
 
             Writer w = new FileWriter("/Users/Oli/git/WBA2_SS13_Phase2/WBA2_Phase2/src/Tippspiel/Tippspiel_Test.xml");
             m.marshal(ts, w);
             w.close();
             return ts;
         }
         return ts;
 
 
         }
 
 }
