 /*
  * 
  */
 package parcodb.gui;
 
 import com.trolltech.qt.gui.QApplication;
 import com.trolltech.qt.gui.QComboBox;
 import com.trolltech.qt.gui.QDialog;
 import com.trolltech.qt.gui.QListWidget;
 import com.trolltech.qt.gui.QListWidgetItem;
 import com.trolltech.qt.gui.QMainWindow;
 import com.trolltech.qt.gui.QWidget;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import parcodb.ParcoDB;
 import parcodb.database.DatabaseConnection;
 import parcodb.database.DatabaseConnector;
 import parcodb.database.PostgreSQLconnector;
 import parcodb.database.objects.Caratteristica;
 import parcodb.database.objects.Comune;
 import parcodb.database.objects.Iniziativa;
 import parcodb.database.objects.Paese;
 import parcodb.database.objects.Sentiero;
 import parcodb.database.objects.Struttura;
 import parcodb.database.objects.Tappa;
 import parcodb.gui.builders.Ui_Dialog_connetti;
 import parcodb.gui.builders.Ui_MainWindow;
 
 /**
  * Costruttore dell'interfaccia principale.
  * @author stengun
  */
 public class MainGUI {
     static Ui_MainWindow MAINUI = new Ui_MainWindow();
     private Widget_Centrale widget_centrale;
     public QMainWindow qMainWindow;
     public DatabaseConnection conn;
     private Ui_Dialog_connetti dial_conn;
     private QDialog connector;
     static String TITOLO_FINESTRA="Informaparchi";
     
     public MainGUI(String[] args) {
         QApplication.initialize(args);
         
         //inizilizzazione grafica dei widget personalizzati.
         dial_conn = new Ui_Dialog_connetti();
         qMainWindow = new QMainWindow();
     }
     
     public void SetupUi(){
         MAINUI.setupUi(qMainWindow);
         set_signals();
         changeMode();
         qMainWindow.show();
         MAINUI.actionCollega_a_database.triggered.emit(Boolean.TRUE);
     }
     
     private void set_signals(){
         MAINUI.actionModalit_avanzata.toggled.connect(this, "changeMode()");
         MAINUI.actionCollega_a_database.triggered.connect(this, "openConnect()");
     }
     
     private void openConnect(){
         if(connector == null){
             connector = new QDialog(qMainWindow);
             dial_conn.setupUi(connector);
             dial_conn.bottone_connetti.clicked.connect(this, "connect()");
         }
         dial_conn.bottone_annulla.clicked.connect(connector, "hide()");
         connector.show();
     }
     
     private void disconnect(){
         try {
             conn.close();
         } catch (SQLException ex) {
             Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
         }
         dial_conn.bottone_connetti.setText("Connetti");
         dial_conn.bottone_connetti.clicked.disconnect();
         dial_conn.bottone_connetti.clicked.connect(this, "connect()");
         dial_conn.lineEdit_indirizzo.setEnabled(true);
         dial_conn.lineEdit_porta.setEnabled(true);
         statusMessage("Disconnesso");
     }
     
     private void connect(){
         String indirizzo = dial_conn.lineEdit_indirizzo.text();
         int porta = Integer.decode(dial_conn.lineEdit_porta.text());
         
         Logger.getLogger(ParcoDB.class.getName()).setLevel(Level.ALL); 
         try {
             Logger.getLogger(ParcoDB.class.getName()).log(Level.INFO,"[1] Loading Driver...");
             //DatabaseConnector database = new MySQLconnector(indirizzo, porta);
             DatabaseConnector database = new PostgreSQLconnector(indirizzo, porta);
             
             Logger.getLogger(ParcoDB.class.getName()).log(Level.INFO,"[1] Driver Loaded");
             Logger.getLogger(ParcoDB.class.getName()).log(Level.INFO,"[2] Connecting...");
             
             conn = database.connect(dial_conn.lineEdit_username.text(), dial_conn.lineEdit_password.text());
             
             Logger.getLogger(ParcoDB.class.getName()).log(Level.INFO,"[2] Connected");
         } catch (SQLException | ClassNotFoundException ex) {
             databaseError();
             Logger.getLogger(ParcoDB.class.getName()).log(Level.SEVERE, null, ex);
             return;
         }
         
         dial_conn.bottone_connetti.setText("Disconnetti");
         dial_conn.lineEdit_indirizzo.setEnabled(false);
         dial_conn.lineEdit_porta.setEnabled(false);
         dial_conn.bottone_connetti.clicked.disconnect();
         dial_conn.bottone_connetti.clicked.connect(this, "disconnect()");
         dial_conn.bottone_annulla.clicked.emit(Boolean.TRUE);
         statusMessage("Connesso!");
     }
         
     private static void databaseError() {
         Logger.getLogger(ParcoDB.class.getName()).log(Level.SEVERE, "###Connessione al database non riscita");
         statusMessage("Connessione non riuscita");
     }
     
     private void changeMode(){
         String titolo= TITOLO_FINESTRA+" - Modalità Ricerca";
         if(MAINUI.actionModalit_avanzata.isChecked()){
             widget_centrale = new Widget_Inserimento(this);
             titolo = TITOLO_FINESTRA+" - Modalità Inserimento";
             statusMessage("Modalità inserimento attivata");
         }
         else{
             widget_centrale = new Widget_Ricerca(this);
             statusMessage("Modalità Ricerca attivata");
         }
         
         QWidget wid_central = new QWidget(qMainWindow);
         widget_centrale.setupUi(wid_central);
         qMainWindow.setCentralWidget(wid_central);
         qMainWindow.setWindowTitle(titolo);
 
     }
         //popolatori di widget
     
    public void popolaListaSentieri(QListWidget lista){
         Sentiero[] sentieri = Sentiero.getSentieri(conn);
         for(Sentiero sentiero:sentieri){
             QListWidgetItem list = new QListWidgetItem();
             list.setData(0, sentiero);
             lista.addItem(list);
         }
     }
     
     public void popolaListaIniziative(QListWidget lista) throws SQLException{
         Iniziativa[] iniziative = Iniziativa.getIniziative(conn);
         for(Iniziativa iniziativa:iniziative){
             QListWidgetItem list = new QListWidgetItem();
             list.setData(0, iniziativa);
             lista.addItem(list);
         }
     }
     
     public void popolaComboPaesi(QComboBox where) throws SQLException{
         Paese[] paesi = Paese.getPaesi(conn);
         for (Paese paese : paesi) {
            where.addItem(paese.toString(), paese);
         }
     }
     
     public void popolaListaStrutture(QListWidget lista) throws SQLException{
         Struttura[] strutture = Struttura.getStrutture(conn);
         for(Struttura struttura:strutture){
             QListWidgetItem list = new QListWidgetItem();
             list.setData(0, struttura);
             lista.addItem(list);
         }
     }
     
     public void popolaListaTappe(QListWidget where) throws SQLException{
         Tappa[] tappe = Tappa.getTappe(conn);
         for(Tappa tappa:tappe){
             QListWidgetItem list = new QListWidgetItem();
             list.setData(0, tappa);
             where.addItem(list);
         }
     }
     
     public void popolaListaCaratt(QListWidget where) throws SQLException{
         Caratteristica[] caratteristiche = Caratteristica.getCaratteristiche(conn);
         for (Caratteristica caratteristica : caratteristiche) {
             QListWidgetItem list = new QListWidgetItem();
             list.setData(0, caratteristica);
             where.addItem(list);
         }
     }
     
     public void popolaListaComune(QListWidget where) throws SQLException{
         Comune[] comuni = Comune.getComuni(conn);
         for(Comune comune:comuni){
             QListWidgetItem list = new QListWidgetItem();
             list.setData(0, comune);
             where.addItem(list);
         }
     }
     
     public void exec() {
         QApplication.exec();
     }
     
     public static void statusMessage(String messaggio){
         MAINUI.statusbar.showMessage(messaggio);
     }
     
 
     
 }
