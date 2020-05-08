 package components;
 
import java.sql.SQLException;
 import javax.swing.JList;
 import javax.swing.DefaultListModel;
import utils.DBConnection;
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
    private List<Station> stations;
     private Metro metro;
 
     /**
      * Contructeur sans argument.
      */
     public StationList() {
        try {
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            listModel = new DefaultListModel<>();
            stations = DBConnection.getMetroStations();
            for (Station st : stations) {
                listModel.addElement(st);
            }
            setModel(listModel);
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace(System.err);
         }
     }
 
     public void filter(Ligne ligne) {
         listModel.removeAllElements();
         List<Station> sta = metro.getStations(ligne);
         for (Station st : sta) {
             listModel.addElement(st);
         }
     }
 }
