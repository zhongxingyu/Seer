 package components;
 
 import javax.swing.JList;
 import javax.swing.DefaultListModel;
 import utils.Station;
 import java.util.List;
 import javax.swing.ListSelectionModel;
 import utils.Ligne;
 import utils.Metro;
 
 /**
  * Classe permettant d'utiliser une liste avec les stations de metro.
  */
 public class StationList extends JList<Station> {
 
     private DefaultListModel<Station> listModel;
     private Metro metro;
 
     /**
      * Contructeur sans argument.
      */
     public StationList() {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listModel = new DefaultListModel<>();
        metro = new Metro();
        
        /* On rempli le model */
        for (Station st : metro.getAllStations()) {
            listModel.addElement(st);
         }
        
        setModel(listModel);
     }
 
     public void filter(Ligne ligne) {
         listModel.removeAllElements();
         List<Station> sta = metro.getStations(ligne);
         for (Station st : sta) {
             listModel.addElement(st);
         }
     }
 }
