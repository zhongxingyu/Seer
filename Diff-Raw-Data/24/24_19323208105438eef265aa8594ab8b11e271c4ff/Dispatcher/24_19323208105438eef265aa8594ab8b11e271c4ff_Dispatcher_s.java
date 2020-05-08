 package dispatcherMonitor;
 
 import dispatcherMonitor.interfaces.IDispatcher;
 import typClasses.AngebotTyp;
 import typClasses.KundeTyp;
 import typClasses.ProduktTyp;
 
 import java.util.Date;
 import java.util.List;
 
 /**
  *
  */
 public class Dispatcher implements IDispatcher {
     @Override
     public AngebotTyp erstelleAngebot(Date gueltigAb, Date gueltigBis, float gesamtPreis, KundeTyp kunde) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public AngebotTyp sucheAngebot(int id) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public List<AngebotTyp> getAlleAngebote() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void addProduktMengeZuAngebot(AngebotTyp angebot, ProduktTyp produkt, int menge) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public KundeTyp erstelleKunde(String name, String adresse) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public ProduktTyp erstelleProdukt(String name, int lagerbestand) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public ProduktTyp sucheProdukt(String name) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public ProduktTyp sucheProdukt(int id) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public List<ProduktTyp> getAlleProdukte() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public KundeTyp sucheKunde(String name) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public KundeTyp sucheKunde(int id) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public List<KundeTyp> getAlleKunden() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 }
