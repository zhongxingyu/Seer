 package listeners;
 
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.sql.SQLException;
 import java.text.Collator;
 import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JTextField;
 import utils.DBConnection;
 import utils.Station;
 
 /**
  *
  * @author Ahmed Mf
  */
 public class SearchStationListener extends KeyAdapter {
 
     /** Le JTextField ou l'on rentre la recherche. */
     private JComboBox<String> search;
     /** Une liste chainée de stations. */
     private ArrayList<Station> stations;
     /** Utilisé pour les compraisons de chaînes de caractères. */
     private static Collator mycoll = Collator.getInstance();
 
     /**
      * Le constructeur.
      * @param search La JComboBox à utiliser pour donner des suggestions
      */
     public SearchStationListener(JComboBox<String> search) {
         this.search = search;
         try {
             stations = DBConnection.getMetroStations();
         } catch (SQLException ex) {
             System.err.println(ex);
         } catch (ClassNotFoundException ex) {
             System.err.println(ex);
         } catch (Exception ex) {
             System.err.println(ex);
         }
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
         if ((e.getKeyCode() != KeyEvent.VK_UP)
                 && (e.getKeyCode() != KeyEvent.VK_DOWN)
                 && (e.getKeyCode() != KeyEvent.VK_ENTER)) {
             String srch = ((JTextField) search.getEditor().getEditorComponent())
                     .getText();
             search.setModel(filterStations(srch));
             search.setPopupVisible(false);
             if (!srch.isEmpty()) {
                 search.setPopupVisible(true);
             }
         }
     }
 
     /**
      * Filtre les stations pour ne garder que celles qui correspondent à la
      * recherche.
      *
      * @param srch La recherche entrée par l'utilisateur
      * @return Un DefaultComboBoxModel contenant les stations retenues
      */
     private DefaultComboBoxModel<String> filterStations(String srch) {
         DefaultComboBoxModel<String> model;
        Set<String> selected = new HashSet<>();
         model = (DefaultComboBoxModel<String>)search.getModel();
         model.removeAllElements();
         model.addElement(srch);
        
         if (srch.length() > 1) {
             /* Cherche les stations commencant par la recherche */
             for (Station st : stations) {
                 String arret = st.getNom();
                 if (SearchStationListener.areAlike(srch, arret)) {
                    selected.add(arret);
                 }
             }
             
             /* Regarde si la station contiens la recherche */
             for (Station st : stations) {
                 String arret = st.getNom();
                 if(arret.toLowerCase().contains(srch)) {
                    selected.add(arret);
                 }
             }
            
            for (String str : selected) {
                model.addElement(str);
            }
         }
         return model;
     }
 
     /**
      * Compare deux chaines de caractère pour voir si elles sont semblables.
      *
      * @param src La chaine source
      * @param dest La chaine destination
      * @return Un booléen qui indique si les chaines sont semblables ou non
      */
     private static boolean areAlike(String src, String dest) {
         /* Reglage de la force de comparaison */
         mycoll.setStrength(Collator.PRIMARY);
         
         /* Comparaison */
         if ((mycoll.compare(src,
                 dest.substring(0, Math.min(dest.length(), src.length()))) == 0)
                 && (!dest.equals(src))) {
             return true;
         }
 
         return false;
     }
 }
