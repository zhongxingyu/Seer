 package parcodb.gui;
 
import com.trolltech.qt.gui.QListWidget;
import com.trolltech.qt.gui.QListWidgetItem;
 import com.trolltech.qt.gui.QWidget;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
import parcodb.database.objects.Struttura;
 import parcodb.database.objects.Zona;
 import parcodb.gui.builders.Ui_widget_ricerca_common;
 
 /**
  *
  * @author stengun
  */
 public class Widget_Ricerca extends Widget_Centrale{
 
     Ui_widget_ricerca_common ric_com;
     Macro_widget_struttura strut_com;
     Zona tipo;
     
     
     public Widget_Ricerca(MainGUI mainui) {
         super(mainui);
         ric_com = new Ui_widget_ricerca_common();
         strut_com = new Macro_widget_struttura();
     }
     
     @Override
     public void setupUi(QWidget widget_ricerca){
         super.setupUi(widget_ricerca);
         ric_com.setupUi(this.widget_common);
         ric_com.widget_3.hide();
     }
     
     @Override
     protected void set_caratteristica() {
         String messaggio = "Ricerca terminata.";
         toggle_button(bottone_terreno);
         try {
             maiunui.popolaListaCaratt(ric_com.listWidget_trovati);
         } catch (SQLException ex) {
             Logger.getLogger(Widget_Ricerca.class.getName()).log(Level.SEVERE, null, ex);
             messaggio = "Errore nella ricezione dei terreni dal Database";
         } finally {
             MainGUI.statusMessage(messaggio);
         }        
     }
 
     @Override
     protected void set_struttura() {
         String messaggio = "Ricerca terminata.";
         toggle_button(bottone_struttura);
         strut_com.setupUi(ric_com.widget_common);
         setupStrut();
         try {
             maiunui.popolaListaStrutture(ric_com.listWidget_trovati);
         } catch (SQLException ex) {
             Logger.getLogger(Widget_Ricerca.class.getName()).log(Level.SEVERE, null, ex);
             messaggio = "Errore nella ricezione delle strutture dal Database";
         } finally {
             MainGUI.statusMessage(messaggio);
         }
     }
 
     @Override
     protected void set_iniziativa() {
         String messaggio = "Ricerca terminata.";
         toggle_button(bottone_iniziativa);
         try {
             maiunui.popolaListaIniziative(ric_com.listWidget_trovati);
         } catch (SQLException ex) {
             Logger.getLogger(Widget_Ricerca.class.getName()).log(Level.SEVERE, null, ex);
             messaggio = "Errore nella ricezione delle iniziative dal Database";
         } finally {
             MainGUI.statusMessage(messaggio);
         }
     }
 
     @Override
     protected void set_tappa() {
         String messaggio = "Ricerca terminata.";
         toggle_button(bottone_tappa);
         try {
             maiunui.popolaListaTappe(ric_com.listWidget_trovati);
         } catch (SQLException ex) {
             Logger.getLogger(Widget_Ricerca.class.getName()).log(Level.SEVERE, null, ex);
             messaggio = "Errore nella ricezione delle tappe dal Database";
         } finally {
             MainGUI.statusMessage(messaggio);
         }
     }
 
     @Override
     protected void set_sentiero() {
         String messaggio = "Ricerca terminata.";
         toggle_button(bottone_sentiero);
         try {
             maiunui.popolaListaSentieri(ric_com.listWidget_trovati);
         } catch (SQLException ex) {
             Logger.getLogger(Widget_Ricerca.class.getName()).log(Level.SEVERE, null, ex);
             messaggio = "Errore nella ricezione dei sentieri dal Database";
         } finally {
             MainGUI.statusMessage(messaggio);
         }
     }
 
     @Override
     protected void set_comune() {
         String messaggio = "Ricerca terminata.";
         toggle_button(bottone_comune);
         try {
             maiunui.popolaListaComune(ric_com.listWidget_trovati);
         } catch (SQLException ex) {
             Logger.getLogger(Widget_Ricerca.class.getName()).log(Level.SEVERE, null, ex);
             messaggio = "Errore nella ricezione dei comuni dal Database";
         } finally {
             MainGUI.statusMessage(messaggio);
         }
     }
 
     @Override
     protected void showdialog() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     protected void inserisci() {
         modifica();
     }
     
     private void modifica(){
         
     }
     
     private void setupStrut(){
        strut_com.lineEdit_nome.setEnabled(false);
        strut_com.lineEdit_indirizzo.setEnabled(false);
        strut_com.lineEdit_orario.setEnabled(false);
        strut_com.dateEdit_apertura.setEnabled(false);
        strut_com.dateEdit_chiusura.setEnabled(false);
        strut_com.combo_situato.setEnabled(false);
        strut_com.combo_tipo.setEnabled(false);
    }
     
 }
