 package Vista;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.table.DefaultTableModel;
 import controladors.*;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author ivanmartinez
  */
 public class vistaGrafica extends JFrame {
     
     private DefaultTableModel tablaVivienda;
     private DefaultTableModel tablaPublico;
     private DefaultTableModel tablaComercio;
     private DefaultTableModel tablaUbic;
     private DefaultTableModel tablaDem;
     private DefaultTableModel tablaEco;
     private DefaultTableModel tablaViviendaUser;
     private DefaultTableModel tablaPublicoUser;
     private DefaultTableModel tablaComercioUser;
     private DefaultTableModel tablaUbicUser;
     private DefaultTableModel tablaDemUser;
     private DefaultTableModel tablaEcoUser;
     private CtrlDomBarrios CtrlBarrio;
     private CtrlDomElementos CtrlElem;
     private CtrlDomRestricciones CtrlRest;
     private boolean computando;
         
 
     /**
      * Creates new form vistaGrafica
      */
     public vistaGrafica() {
         CtrlBarrio = CtrlDomBarrios.getInstance();
         CtrlElem = CtrlDomElementos.getInstance();
         CtrlRest = CtrlDomRestricciones.getInstance();
         /*Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
         int height = pantalla.height;
         int width = pantalla.width;
         setLocation(width/2, height/2);		
         setLocationRelativeTo(null);*/
         initComponents();
         jToolBar1.setFloatable(false);
         setResizable(false);
         Console.setText("Cargando el Sistema...\n");
         initModelTablasSistema();
         initTablasBarrioModel();
         initViviendas();
         initPublicos();
         initComercios();
         initUbicacion();
         initDemograficas();
         initEconomicas();
         initTablasEnVista();
         barrioNoCargado();
         computando = false;
         Console.setText("Bienvenido a UPcity\n");
         
     }
     
     private void initTablasEnVista(){
         tabViv.setModel(tablaVivienda);
         tabPub.setModel(tablaPublico);
         tabCom.setModel(tablaComercio);
         tabUbi.setModel(tablaUbic);
         tabDem.setModel(tablaDem);
         tabEco.setModel(tablaEco);
         tabVivU.setModel(tablaViviendaUser);
         tabPubU.setModel(tablaPublicoUser);
         tabComU.setModel(tablaComercioUser);
         tabUbiU.setModel(tablaUbicUser);
         tabDemU.setModel(tablaDemUser);
         tabEcoU.setModel(tablaEcoUser);
     }
     
     private void initModelTablasSistema(){
         tablaVivienda=new DefaultTableModel();
         tablaVivienda.addColumn("Nombre");
         tablaVivienda.addColumn("TBar");
         tablaVivienda.addColumn("Precio");
         tablaVivienda.addColumn("Capacidad");
         tablaVivienda.addColumn("TamX");
         tablaVivienda.addColumn("TamY");
         tablaVivienda.setNumRows(25);
         
         tablaPublico=new DefaultTableModel();
         tablaPublico.addColumn("Nombre");
         tablaPublico.addColumn("TBar");
         tablaPublico.addColumn("Precio");
         tablaPublico.addColumn("TipoPub");
         tablaPublico.addColumn("Capacidad");
         tablaPublico.addColumn("TamX");
         tablaPublico.addColumn("TamY");
         tablaPublico.setNumRows(25);
         
         tablaComercio=new DefaultTableModel();
         tablaComercio.addColumn("Nombre");
         tablaComercio.addColumn("TBar");
         tablaComercio.addColumn("Precio");
         tablaComercio.addColumn("Capacidad");
         tablaComercio.addColumn("TamX");
         tablaComercio.addColumn("TamY");
         tablaComercio.setNumRows(25);
         
         tablaUbic = new DefaultTableModel();
         tablaUbic.addColumn("Nombre");
         tablaUbic.addColumn("Elemento 1");
         tablaUbic.addColumn("Elemento 2");
         tablaUbic.addColumn("Distancia");
         tablaUbic.setNumRows(25);
         
         tablaDem=new DefaultTableModel();
         tablaDem.addColumn("Nombre");
         tablaDem.addColumn("Elemento");
         tablaDem.addColumn("Poblacion minima");
         tablaDem.setNumRows(25);
         
         tablaEco=new DefaultTableModel();
         tablaEco.addColumn("Nombre");
         tablaEco.addColumn("Total");
         tablaEco.addColumn("Viviendas");
         tablaEco.addColumn("Publicos");
         tablaEco.addColumn("Comercios");
         tablaEco.setNumRows(25);
     }
     
     private void initViviendas(){
         for(int i=0;i<tablaVivienda.getRowCount();++i){
             tablaVivienda.removeRow(i);
         }
         tablaVivienda.setNumRows(25);
         int indiceV=0;
         String[][] viv1;
         for(int j=0;j<=3;++j){
             viv1 = CtrlElem.listarElemTipo(j, 1);
             if(viv1!=null){
                 int i;
                 for(i=0;i<viv1.length;++indiceV){
                     tablaVivienda.setValueAt(viv1[i][0], indiceV, 0);
                     tablaVivienda.setValueAt(viv1[i][1], indiceV, 1);
                     tablaVivienda.setValueAt(viv1[i][2], indiceV, 2);
                     tablaVivienda.setValueAt(viv1[i][3], indiceV, 3);
                     tablaVivienda.setValueAt(viv1[i][4], indiceV, 4);
                     tablaVivienda.setValueAt(viv1[i][5], indiceV, 5);
                     ++i;
                 }
             }
         }   
     }
     
     private void initPublicos(){
         for(int i=0;i<tablaPublico.getRowCount();++i){
             tablaPublico.removeRow(i);
         }
         tablaPublico.setNumRows(25);
         int indiceP=0;
         String[][] viv1;
         for(int j=0;j<=3;++j){
             viv1 = CtrlElem.listarElemTipo(j, 2);
             if(viv1!=null){
                 int i;
                 for(i=0;i<viv1.length;++indiceP){
                     tablaPublico.setValueAt(viv1[i][0], indiceP, 0);
                     tablaPublico.setValueAt(viv1[i][1], indiceP, 1);
                     tablaPublico.setValueAt(viv1[i][2], indiceP, 2);
                     tablaPublico.setValueAt(viv1[i][3], indiceP, 3);
                     tablaPublico.setValueAt(viv1[i][4], indiceP, 4);
                     tablaPublico.setValueAt(viv1[i][5], indiceP, 5);
                     tablaPublico.setValueAt(viv1[i][6], indiceP, 6);
                     ++i;
                 }
             }
         }
         
     }
         
     private void initComercios(){
         for(int i=0;i<tablaComercio.getRowCount();++i){
             tablaComercio.removeRow(i);
         }
         tablaComercio.setNumRows(25);
         int indiceC=0;
         String[][] viv1;
         for(int j=0;j<=3;++j){
             viv1 = CtrlElem.listarElemTipo(j, 3);
             if(viv1!=null){
                 int i;
                 for(i=0;i<viv1.length;++indiceC){
                     tablaComercio.setValueAt(viv1[i][0], indiceC, 0);
                     tablaComercio.setValueAt(viv1[i][1], indiceC, 1);
                     tablaComercio.setValueAt(viv1[i][2], indiceC, 2);
                     tablaComercio.setValueAt(viv1[i][3], indiceC, 3);
                     tablaComercio.setValueAt(viv1[i][4], indiceC, 4);
                     tablaComercio.setValueAt(viv1[i][5], indiceC, 5);
                     ++i;
                 }
             }
         }
     }
     
     private void initUbicacion(){
         for(int i=0;i<tablaUbic.getRowCount();++i){
             tablaUbic.removeRow(i);
         }
         tablaUbic.setNumRows(25);
         int indiceU;
         String[][] viv1;
         viv1 = CtrlRest.listarRestTipo("ubicacion");
         if(viv1!=null){
             for(indiceU=0;indiceU<viv1.length;++indiceU){
                 tablaUbic.setValueAt(viv1[indiceU][0], indiceU, 0);
                 tablaUbic.setValueAt(viv1[indiceU][1], indiceU, 1);
                 tablaUbic.setValueAt(viv1[indiceU][2], indiceU, 2);
                 tablaUbic.setValueAt(viv1[indiceU][3], indiceU, 3);
             }
         }
     }
     
     private void initDemograficas(){
         for(int i=0;i<tablaDem.getRowCount();++i){
             tablaDem.removeRow(i);
         }
         tablaDem.setNumRows(25);
         int indiceD;
         String[][] viv1;
         viv1 = CtrlRest.listarRestTipo("demografica");
         if(viv1!=null){
             for(indiceD=0;indiceD<viv1.length;++indiceD){
                 tablaDem.setValueAt(viv1[indiceD][0], indiceD, 0);
                 tablaDem.setValueAt(viv1[indiceD][1], indiceD, 1);
                 tablaDem.setValueAt(viv1[indiceD][2], indiceD, 2);
             } 
         }
     }
     
     private void initEconomicas(){
         for(int i=0;i<tablaEco.getRowCount();++i){
             tablaEco.removeRow(i);
         }
         tablaEco.setNumRows(25);
         int indiceE;
         String[][] viv1;
         viv1 = CtrlRest.listarRestTipo("economica");
         if(viv1!=null){
             for(indiceE=0;indiceE<viv1.length;++indiceE){
                 tablaEco.setValueAt(viv1[indiceE][0], indiceE, 0);
                 tablaEco.setValueAt(viv1[indiceE][1], indiceE, 1);
                 tablaEco.setValueAt(viv1[indiceE][2], indiceE, 2);
                 tablaEco.setValueAt(viv1[indiceE][3], indiceE, 3);
                 tablaEco.setValueAt(viv1[indiceE][4], indiceE, 4);
             }
         }
     }
     
     private void initTablasBarrioModel(){
         tablaViviendaUser = new DefaultTableModel();
         tablaViviendaUser.addColumn("Nombre");
         tablaViviendaUser.addColumn("Cantidad");
         tablaViviendaUser.setNumRows(25);
         
         tablaPublicoUser = new DefaultTableModel();
         tablaPublicoUser.addColumn("Nombre");
         tablaPublicoUser.addColumn("Cantidad");
         tablaPublicoUser.setNumRows(25);
         
         tablaComercioUser = new DefaultTableModel();
         tablaComercioUser.addColumn("Nombre");
         tablaComercioUser.addColumn("Cantidad");
         tablaComercioUser.setNumRows(25);
         
         tablaUbicUser = new DefaultTableModel();
         tablaUbicUser.addColumn("Nombre");
         tablaUbicUser.addColumn("Elemento 1");
         tablaUbicUser.addColumn("Elemento 2");
         tablaUbicUser.addColumn("Distancia");
         tablaUbicUser.setNumRows(25);
         
         tablaDemUser=new DefaultTableModel();
         tablaDemUser.addColumn("Nombre");
         tablaDemUser.addColumn("Elemento");
         tablaDemUser.addColumn("Poblacion minima");
         tablaDemUser.setNumRows(25);
         
         tablaEcoUser=new DefaultTableModel();
         tablaEcoUser.addColumn("Nombre");
         tablaEcoUser.addColumn("Total");
         tablaEcoUser.addColumn("Viviendas");
         tablaEcoUser.addColumn("Publicos");
         tablaEcoUser.addColumn("Comercios");
         tablaEcoUser.setNumRows(25);
         
         
     }
     
     private void initTablasBarrioElem(){
         for(int i=0;i<tablaViviendaUser.getRowCount();++i){
             tablaViviendaUser.removeRow(i);
         }
         for(int i=0;i<tablaComercioUser.getRowCount();++i){
             tablaComercioUser.removeRow(i);
         }
         for(int i=0;i<tablaPublicoUser.getRowCount();++i){
             tablaPublicoUser.removeRow(i);
         }
         tablaViviendaUser.setNumRows(25);
         tablaComercioUser.setNumRows(25);
         tablaPublicoUser.setNumRows(25);
         String[][] list = CtrlBarrio.listarCjtElemBarrio();
         int indiceVU=0;
         int indicePU=0;
         int indiceCU=0;
         if(list!=null){
             for(int i=0;i<list.length;++i){
                 if(list[i][0].equals("v")){
                     tablaViviendaUser.setValueAt(list[i][3], indiceVU, 0);
                     tablaViviendaUser.setValueAt(list[i][1], indiceVU, 1);
                     ++indiceVU;
                 }
                 if(list[i][0].equals("p")){
                     tablaPublicoUser.setValueAt(list[i][3], indicePU, 0);
                     tablaPublicoUser.setValueAt(list[i][1], indicePU, 1);
                     ++indicePU;
                 }
                 if(list[i][0].equals("c")){
                     tablaComercioUser.setValueAt(list[i][3], indiceCU, 0);
                     tablaComercioUser.setValueAt(list[i][1], indiceCU, 1);
                     ++indiceCU;
                 }
             }
         }
         
     }
     
     private void initTablasBarrioRest(){
         for(int i=0;i<tablaUbicUser.getRowCount();++i){
             tablaUbicUser.removeRow(i);
         }
         for(int i=0;i<tablaDemUser.getRowCount();++i){
             tablaDemUser.removeRow(i);
         }
         for(int i=0;i<tablaEcoUser.getRowCount();++i){
             tablaEcoUser.removeRow(i);
         }
         tablaUbicUser.setNumRows(25);
         tablaDemUser.setNumRows(25);
         tablaEcoUser.setNumRows(25);
         String [][] list = CtrlBarrio.listarCjtRestBarrio();
         int indiceUU=0;
         int indiceDU=0;
         int indiceEU=0;
         System.out.println("CHIVATOO");
         if(list!=null){
             for(int i=0;i<list.length;++i){
                 System.out.println("CHIVATOO");
                 if(list[i][0].equals("u")){
                     tablaUbicUser.setValueAt(list[i][1], indiceUU, 0);
                     tablaUbicUser.setValueAt(list[i][2], indiceUU, 1);
                     tablaUbicUser.setValueAt(list[i][3], indiceUU, 2);
                     tablaUbicUser.setValueAt(list[i][4], indiceUU, 3);
                     ++indiceUU;
                 }
                 if(list[i][0].equals("d")){
                     tablaDemUser.setValueAt(list[i][1], indiceUU, 0);
                     tablaDemUser.setValueAt(list[i][2], indiceUU, 1);
                     tablaDemUser.setValueAt(list[i][3], indiceUU, 2);
                     ++indiceDU;
                 }
                 if(list[i][0].equals("e")){
                     tablaEcoUser.setValueAt(list[i][1], indiceUU, 0);
                     tablaEcoUser.setValueAt(list[i][2], indiceUU, 1);
                     tablaEcoUser.setValueAt(list[i][3], indiceUU, 2);
                     tablaEcoUser.setValueAt(list[i][4], indiceUU, 3);
                     tablaEcoUser.setValueAt(list[i][5], indiceUU, 4);
                     ++indiceEU;
                 }
             }
         }
         
     }
     
     private void barrioNoCargado(){
        addElem.setEnabled(false);
         jButton2.setEnabled(false);
         jButton2.setEnabled(false);
         jButton3.setEnabled(false);
         jButton4.setEnabled(false);
         jButton5.setEnabled(false);
         jButton6.setEnabled(false);
         InfoBarrio.setEnabled(false);
         jButton8.setEnabled(false);
         jButton9.setEnabled(false);
         jButton10.setEnabled(false);
         guardarBarrio.setEnabled(false);
     }
     
     private void barrioCargado(){
          addElem.setEnabled(true);
         jButton2.setEnabled(true);
         jButton2.setEnabled(true);
         jButton3.setEnabled(true);
         jButton4.setEnabled(true);
         jButton5.setEnabled(true);
         jButton6.setEnabled(true);
         InfoBarrio.setEnabled(true);
         jButton8.setEnabled(true);
         jButton9.setEnabled(true);
         jButton10.setEnabled(true);
         guardarBarrio.setEnabled(true);
     }
     
     private void modoLibre(){
         addElem.setEnabled(false);
         jButton2.setEnabled(false);
         jButton2.setEnabled(false);
         jButton3.setEnabled(false);
         jButton4.setEnabled(false);
         jButton5.setEnabled(false);
         jButton6.setEnabled(false);
         
     }
     
     private void mostrarBarrio(){
         Integer mat[][] = CtrlBarrio.vistaMapa();
         String p = "Su barrio es el siguiente\n";
         for(int i=0;i < mat.length;++i){
             for(int j=0;j<mat[0].length;++j){
                 p=p.concat(mat[i][j] +"   ");
             }
             p=p.concat("\n");    
         }
         MapaVista mapa = new MapaVista(mat.length, mat[0].length,true);
         mapa.leerMapa(mat);
         jSplitPane3.setTopComponent(mapa);
     }
     
     private void lanzathread(final boolean b){
         System.out.println("Entro");
         Thread hilo = new Thread()
         {
             public void run()
             {
                 try
                 {
                     Console.setText("Vamos a generar su barrio!\n"
                             + "Por favor no realice cambios en el sistema mientras se genera,"
                             + "ya le avisaremos cuando este este terminado!");
                     CtrlBarrio.generarBarrio(b);
                     guardaBarrioActualizacion();
                     computando=false;
                     Console.setText("Mira, ya hemos terminado tu barrio!");
                     
                 }
                 catch ( Exception e )
                 {
                     Console.setText(e.getMessage());
                 }
             }
         };
         hilo.start();
         System.out.println("salgo");
     }
     
     private void mensajeEnGeneracion(){
         System.out.println("Mensaje que no saleeee");
         int contador=0;
         while(computando){
             if(contador==0)Console.setText("Generando Barrio.");
             if(contador==1)Console.setText("Generando Barrio..");
             if(contador==2){
                 Console.setText("Generando Barrio...");
                 contador=-1;
             }
             ++contador;
         }
     }
     
     private void guardaBarrioActualizacion(){
         try{
             CtrlBarrio.guardarBarrio();
         }
         catch(Exception e){
             Console.setText(e.getMessage());
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPopupMenu1 = new javax.swing.JPopupMenu();
         jPopupMenu2 = new javax.swing.JPopupMenu();
         jPopupMenu3 = new javax.swing.JPopupMenu();
         jToolBar1 = new javax.swing.JToolBar();
         addElem = new javax.swing.JButton();
         jButton2 = new javax.swing.JButton();
         jButton3 = new javax.swing.JButton();
         jButton4 = new javax.swing.JButton();
         jButton5 = new javax.swing.JButton();
         jButton6 = new javax.swing.JButton();
         InfoBarrio = new javax.swing.JButton();
         jButton8 = new javax.swing.JButton();
         jButton9 = new javax.swing.JButton();
         jButton10 = new javax.swing.JButton();
         jSplitPane1 = new javax.swing.JSplitPane();
         ScrollsysRest = new javax.swing.JScrollPane();
         jTabbedPane2 = new javax.swing.JTabbedPane();
         jPanel1 = new javax.swing.JPanel();
         jPanel2 = new javax.swing.JPanel();
         elemSisTab = new javax.swing.JTabbedPane();
         jScrollPane15 = new javax.swing.JScrollPane();
         tabViv = new javax.swing.JTable();
         jScrollPane2 = new javax.swing.JScrollPane();
         tabPub = new javax.swing.JTable();
         jScrollPane3 = new javax.swing.JScrollPane();
         tabCom = new javax.swing.JTable();
         jTabbedPane4 = new javax.swing.JTabbedPane();
         jScrollPane5 = new javax.swing.JScrollPane();
         tabUbi = new javax.swing.JTable();
         jScrollPane6 = new javax.swing.JScrollPane();
         tabDem = new javax.swing.JTable();
         jScrollPane7 = new javax.swing.JScrollPane();
         tabEco = new javax.swing.JTable();
         jLayeredPane1 = new javax.swing.JLayeredPane();
         jSplitPane3 = new javax.swing.JSplitPane();
         jScrollPane1 = new javax.swing.JScrollPane();
         Console = new javax.swing.JTextPane();
         viewBarrio = new javax.swing.JPanel();
         jSplitPane4 = new javax.swing.JSplitPane();
         ScrollsysRest1 = new javax.swing.JScrollPane();
         jTabbedPane5 = new javax.swing.JTabbedPane();
         jPanel3 = new javax.swing.JPanel();
         jPanel4 = new javax.swing.JPanel();
         jTabbedPane6 = new javax.swing.JTabbedPane();
         jScrollPane8 = new javax.swing.JScrollPane();
         tabVivU = new javax.swing.JTable();
         jScrollPane9 = new javax.swing.JScrollPane();
         tabPubU = new javax.swing.JTable();
         jScrollPane10 = new javax.swing.JScrollPane();
         tabComU = new javax.swing.JTable();
         jTabbedPane7 = new javax.swing.JTabbedPane();
         jScrollPane11 = new javax.swing.JScrollPane();
         tabUbiU = new javax.swing.JTable();
         jScrollPane12 = new javax.swing.JScrollPane();
         tabDemU = new javax.swing.JTable();
         jScrollPane13 = new javax.swing.JScrollPane();
         tabEcoU = new javax.swing.JTable();
         filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 3), new java.awt.Dimension(0, 3), new java.awt.Dimension(32767, 3));
         filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 5), new java.awt.Dimension(0, 5), new java.awt.Dimension(32767, 5));
         jMenuBar1 = new javax.swing.JMenuBar();
         gestBarrio = new javax.swing.JMenu();
         newBar = new javax.swing.JMenuItem();
         Eliminar_barrio = new javax.swing.JMenuItem();
         guardarBarrio = new javax.swing.JMenuItem();
         cargaBarrioMenu = new javax.swing.JMenuItem();
         newElem = new javax.swing.JMenu();
         CreaElem = new javax.swing.JMenuItem();
         jMenuItem1 = new javax.swing.JMenuItem();
         newRes = new javax.swing.JMenu();
         jMenuItem4 = new javax.swing.JMenuItem();
         jMenuItem2 = new javax.swing.JMenuItem();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setLocation(new java.awt.Point(80, 100));
         setPreferredSize(new java.awt.Dimension(1115, 640));
 
         jToolBar1.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED, java.awt.Color.darkGray, java.awt.Color.gray));
         jToolBar1.setForeground(new java.awt.Color(51, 51, 51));
         jToolBar1.setRollover(true);
 
         addElem.setText("+Elem");
         addElem.setFocusable(false);
         addElem.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         addElem.setMaximumSize(new java.awt.Dimension(57, 18));
         addElem.setMinimumSize(new java.awt.Dimension(57, 18));
         addElem.setPreferredSize(new java.awt.Dimension(57, 18));
         addElem.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         addElem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 anadirElemento(evt);
             }
         });
         jToolBar1.add(addElem);
 
         jButton2.setText("-Elem");
         jButton2.setFocusable(false);
         jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton2.setMaximumSize(new java.awt.Dimension(57, 18));
         jButton2.setMinimumSize(new java.awt.Dimension(57, 18));
         jButton2.setPreferredSize(new java.awt.Dimension(57, 18));
         jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 QuitarElemento(evt);
             }
         });
         jToolBar1.add(jButton2);
 
         jButton3.setText("+Res");
         jButton3.setFocusable(false);
         jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton3.setMaximumSize(new java.awt.Dimension(57, 18));
         jButton3.setMinimumSize(new java.awt.Dimension(57, 18));
         jButton3.setPreferredSize(new java.awt.Dimension(57, 18));
         jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButton3.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 anadirRestBarrio(evt);
             }
         });
         jToolBar1.add(jButton3);
 
         jButton4.setText("-Res");
         jButton4.setFocusable(false);
         jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton4.setMaximumSize(new java.awt.Dimension(57, 18));
         jButton4.setMinimumSize(new java.awt.Dimension(57, 18));
         jButton4.setPreferredSize(new java.awt.Dimension(57, 18));
         jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButton4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 elimResBarrio(evt);
             }
         });
         jToolBar1.add(jButton4);
 
         jButton5.setText("+Car");
         jButton5.setFocusable(false);
         jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton5.setMaximumSize(new java.awt.Dimension(57, 18));
         jButton5.setMinimumSize(new java.awt.Dimension(50, 18));
         jButton5.setPreferredSize(new java.awt.Dimension(50, 18));
         jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButton5.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 anadirCarretera(evt);
             }
         });
         jToolBar1.add(jButton5);
 
         jButton6.setText("-Car");
         jButton6.setFocusable(false);
         jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton6.setMaximumSize(new java.awt.Dimension(57, 18));
         jButton6.setMinimumSize(new java.awt.Dimension(50, 18));
         jButton6.setPreferredSize(new java.awt.Dimension(50, 18));
         jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButton6.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 eliminarCarretera(evt);
             }
         });
         jToolBar1.add(jButton6);
 
         InfoBarrio.setText("Info barrio");
         InfoBarrio.setFocusable(false);
         InfoBarrio.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         InfoBarrio.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         InfoBarrio.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 infoBarrio(evt);
             }
         });
         jToolBar1.add(InfoBarrio);
 
         jButton8.setText("Presupuesto");
         jButton8.setFocusable(false);
         jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButton8.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 editPresupuesto(evt);
             }
         });
         jToolBar1.add(jButton8);
 
         jButton9.setText("Poblacion");
         jButton9.setFocusable(false);
         jButton9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton9.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButton9.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 editPoblacion(evt);
             }
         });
         jToolBar1.add(jButton9);
 
         jButton10.setText("Generar");
         jButton10.setFocusable(false);
         jButton10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton10.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButton10.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 generarBarrio(evt);
             }
         });
         jToolBar1.add(jButton10);
 
         jSplitPane1.setDividerLocation(292);
         jSplitPane1.setDividerSize(2);
         jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
         jSplitPane1.setMinimumSize(new java.awt.Dimension(125, 125));
 
         org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 100, Short.MAX_VALUE)
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 100, Short.MAX_VALUE)
         );
 
         org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .add(33, 33, 33)
                 .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .add(39, 39, 39)
                 .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jTabbedPane2.addTab("tab1", jPanel1);
 
         ScrollsysRest.setViewportView(jTabbedPane2);
 
         jSplitPane1.setRightComponent(ScrollsysRest);
 
         tabViv.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         tabViv.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
         jScrollPane15.setViewportView(tabViv);
 
         elemSisTab.addTab("VIV", jScrollPane15);
 
         tabPub.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         tabPub.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
         jScrollPane2.setViewportView(tabPub);
 
         elemSisTab.addTab("PUB", jScrollPane2);
 
         tabCom.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         tabCom.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
         tabCom.setAutoscrolls(false);
         jScrollPane3.setViewportView(tabCom);
 
         elemSisTab.addTab("COM", jScrollPane3);
 
         jSplitPane1.setTopComponent(elemSisTab);
 
         tabUbi.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Nombre", "Edificio 1", "Edificio 2", "Distancia"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Integer.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, false, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         tabUbi.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
         jScrollPane5.setViewportView(tabUbi);
 
         jTabbedPane4.addTab("UBI", jScrollPane5);
 
         tabDem.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null},
                 {null, null},
                 {null, null},
                 {null, null}
             },
             new String [] {
                 "Nombre", "Poblacion Minima"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jScrollPane6.setViewportView(tabDem);
 
         jTabbedPane4.addTab("DEM", jScrollPane6);
 
         tabEco.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Nombre", "P.V", "P.P", "P.C"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, true, true
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         tabEco.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
         jScrollPane7.setViewportView(tabEco);
 
         jTabbedPane4.addTab("ECO", jScrollPane7);
 
         jSplitPane1.setRightComponent(jTabbedPane4);
 
         jLayeredPane1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 102, 102), 3, true));
 
         jSplitPane3.setDividerLocation(450);
         jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
 
         Console.setEditable(false);
         jScrollPane1.setViewportView(Console);
 
         jSplitPane3.setBottomComponent(jScrollPane1);
 
         org.jdesktop.layout.GroupLayout viewBarrioLayout = new org.jdesktop.layout.GroupLayout(viewBarrio);
         viewBarrio.setLayout(viewBarrioLayout);
         viewBarrioLayout.setHorizontalGroup(
             viewBarrioLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 596, Short.MAX_VALUE)
         );
         viewBarrioLayout.setVerticalGroup(
             viewBarrioLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 448, Short.MAX_VALUE)
         );
 
         jSplitPane3.setLeftComponent(viewBarrio);
 
         jSplitPane3.setBounds(4, 3, 600, 550);
         jLayeredPane1.add(jSplitPane3, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         jSplitPane4.setDividerLocation(292);
         jSplitPane4.setDividerSize(2);
         jSplitPane4.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
         jSplitPane4.setMinimumSize(new java.awt.Dimension(125, 125));
 
         org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 100, Short.MAX_VALUE)
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 100, Short.MAX_VALUE)
         );
 
         org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel3Layout.createSequentialGroup()
                 .add(33, 33, 33)
                 .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel3Layout.createSequentialGroup()
                 .add(39, 39, 39)
                 .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jTabbedPane5.addTab("tab1", jPanel3);
 
         ScrollsysRest1.setViewportView(jTabbedPane5);
 
         jSplitPane4.setRightComponent(ScrollsysRest1);
 
         tabVivU.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Nombre", "Precio", "Nº H Max", "Tipo Barrio"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, true, true
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jScrollPane8.setViewportView(tabVivU);
 
         jTabbedPane6.addTab("VIV", jScrollPane8);
 
         tabPubU.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane9.setViewportView(tabPubU);
 
         jTabbedPane6.addTab("PUB", jScrollPane9);
 
         tabComU.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Nombre", "Precio", "Nº H Max", "Tipo Barrio"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, false, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jScrollPane10.setViewportView(tabComU);
 
         jTabbedPane6.addTab("COM", jScrollPane10);
 
         jSplitPane4.setTopComponent(jTabbedPane6);
 
         tabUbiU.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Nombre", "Edificio 1", "Edificio 2", "Distancia"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Integer.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, false, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         tabUbiU.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
         jScrollPane11.setViewportView(tabUbiU);
 
         jTabbedPane7.addTab("UBI", jScrollPane11);
 
         tabDemU.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null},
                 {null, null},
                 {null, null},
                 {null, null}
             },
             new String [] {
                 "Nombre", "Poblacion Minima"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jScrollPane12.setViewportView(tabDemU);
 
         jTabbedPane7.addTab("DEM", jScrollPane12);
 
         tabEcoU.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Nombre", "P.V", "P.P", "P.C"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, true, true
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         tabEcoU.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
         jScrollPane13.setViewportView(tabEcoU);
 
         jTabbedPane7.addTab("ECO", jScrollPane13);
 
         jSplitPane4.setRightComponent(jTabbedPane7);
 
         gestBarrio.setText("Barrios");
 
         newBar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
         newBar.setText("Crear Barrio");
         newBar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 newBarActionPerformed(evt);
             }
         });
         gestBarrio.add(newBar);
 
         Eliminar_barrio.setText("Eliminar Barrio");
         Eliminar_barrio.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 Eliminar_barrioActionPerformed(evt);
             }
         });
         gestBarrio.add(Eliminar_barrio);
 
         guardarBarrio.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
         guardarBarrio.setText("Guardar Barrio");
         guardarBarrio.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 guardaBarrio(evt);
             }
         });
         gestBarrio.add(guardarBarrio);
 
         cargaBarrioMenu.setText("Cargar Barrio");
         cargaBarrioMenu.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cargarBarrio(evt);
             }
         });
         gestBarrio.add(cargaBarrioMenu);
 
         jMenuBar1.add(gestBarrio);
 
         newElem.setText("Elementos");
 
         CreaElem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
         CreaElem.setText("Nuevo Elemento");
         CreaElem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 CreaElemActionPerformed(evt);
             }
         });
         newElem.add(CreaElem);
 
         jMenuItem1.setText("Eliminar Elemento");
         jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 elimElem(evt);
             }
         });
         newElem.add(jMenuItem1);
 
         jMenuBar1.add(newElem);
 
         newRes.setText("Restricciones");
 
         jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
         jMenuItem4.setText("Nueva Restriccion");
         jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 creaRes(evt);
             }
         });
         newRes.add(jMenuItem4);
 
         jMenuItem2.setText("Eliminar Restriccion");
         jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 elimRest(evt);
             }
         });
         newRes.add(jMenuItem2);
 
         jMenuBar1.add(newRes);
 
         setJMenuBar(jMenuBar1);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                         .addContainerGap()
                         .add(jSplitPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 240, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                             .add(jLayeredPane1)
                             .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 611, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .add(jSplitPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 240, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(layout.createSequentialGroup()
                         .add(492, 492, 492)
                         .add(filler1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(0, 0, Short.MAX_VALUE))
                     .add(filler2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                     .add(layout.createSequentialGroup()
                         .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jLayeredPane1))
                     .add(jSplitPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 590, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jSplitPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 590, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(filler2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 6, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(filler1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(25, 25, 25))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void newBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newBarActionPerformed
         formNewBar formulario = new formNewBar(this,true);
         formulario.setVisible(true);
         if(formulario.aceptado()){
             try{
             String Nombre = formulario.getNombre();
             int X=formulario.get_X();
             int Y=formulario.get_Y();
             if(formulario.getModo()==1){
                 try{
                     CtrlBarrio.crearBarrio(Nombre, 0,1);
                     CtrlBarrio.crearMapaBarrio(X, Y);
                     barrioCargado();
                     modoLibre();
                 }
                 catch(Exception e){
                     Console.setText(e.getMessage());
                 }
                 
             }
             else if(formulario.getModo()==2){
                 int TB = formulario.getTB();
                 try{
                     CtrlBarrio.crearBarrio(Nombre, TB,0);
                     CtrlBarrio.crearMapaBarrio(X, Y);
                     barrioCargado();
                 }
                 catch(Exception e){
                     Console.setText(e.getMessage());
                 }
             }
             else if(formulario.getModo()==3){
                 int TB = formulario.getTB();
                 int presupuesto = formulario.getPresupuesto();
                 int poblacion = formulario.getPoblacion();
                 try{
                     CtrlBarrio.crearBarrio(Nombre, TB,0);
                     CtrlBarrio.crearMapaBarrio(X, Y);
                     CtrlBarrio.setPresupuestoBarrio(presupuesto);
                     CtrlBarrio.setPoblacionbarrio(poblacion);
                     barrioCargado();
                 }
                 catch(Exception e){
                     Console.setText(e.getMessage());
                 }
             }
         }
         catch(Exception e){
             Console.setText("No ha podido crearse el barrio por falta de algun dato");
         }
         }
     }//GEN-LAST:event_newBarActionPerformed
 
     private void CreaElemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreaElemActionPerformed
         formNewElem formulario = new formNewElem(this,true);
         formulario.setVisible(true);
         if(formulario.aceptado()){
             try{
             String nombre = formulario.getNombre();
             String des = formulario.getDesc();
             int TE = formulario.getTE();
             int TB = formulario.getTB();
             int x = formulario.get_X();
             int y = formulario.get_Y();
             int precio = formulario.getPrecio();
             int capacidad = formulario.getCapacidad();
             if(TE==1){
                 try {
                     CtrlElem.CrearElemento(nombre, des, TE, TB, x, y, precio, capacidad,0);
                     initViviendas();
                     Console.setText("Elemento creado correctamente");
                 } catch (Exception ex) {
                     Console.setText(ex.getMessage());
                 }
             }
             else if(TE==2){
                 int TEP = formulario.tipoDePublico();
                 try {
                     CtrlElem.CrearElemento(nombre, des, TE, TB, x, y, precio, capacidad, TEP);
                     initPublicos();
                     Console.setText("Elemento creado correctamente");
                 } catch (Exception ex) {
                     Console.setText(ex.getMessage());
                 }
             }
             else if(TE==3){
                 try {
                     CtrlElem.CrearElemento(nombre, des, TE, TB, x, y, precio, capacidad,0);
                     initComercios();
                     Console.setText("Elemento creado correctamente");
                 } catch (Exception ex) {
                     Console.setText(ex.getMessage());
                 }
             }
            
         }
         catch(Exception e){
             Console.setText("No ha podido crearse el elemento por falta de algun dato");
         }
         
 }
     }//GEN-LAST:event_CreaElemActionPerformed
 
     private void Eliminar_barrioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Eliminar_barrioActionPerformed
         
         ArrayList<String> barrios = CtrlBarrio.listarBarrios();
         Object[] list = new Object[barrios.size()];
         for(int i=0;i<barrios.size();++i){
             list[i] = barrios.get(i);
         }
         
         Object seleccion = JOptionPane.showInputDialog(
                             this,
                             "Eliminar Barrio",
                             "Seleccione el barrio que desea eliminar",
                             JOptionPane.QUESTION_MESSAGE,
                             null,  // null para icono defecto
                              list," ");
         if(seleccion!=null){
             try{
                 //String Nombre = formulario.getNombre();
                 try{
                     CtrlBarrio.eliminarBarrio(seleccion.toString());
                     Console.setText("El Barrio "+seleccion.toString()+" ha sido eliminado correctamente");
                 }
                 catch(Exception e){
                     Console.setText(e.getMessage());
                 }
                 
             }
             catch(Exception e){
                 Console.setText("Introduzca un nombre porfavor");
                 JOptionPane warning = new JOptionPane();
                 //warning.
             }
         }
     }//GEN-LAST:event_Eliminar_barrioActionPerformed
 
     private void anadirElemento(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anadirElemento
           addElemBar formulario = new addElemBar(this,true);
           formulario.setVisible(true);
           if(formulario.Aceptado()){
                 String id = formulario.getNombre();
                 int cant = formulario.getCantidad();
             try{
                 CtrlBarrio.anadirElemBarrio(id, cant);
                 initTablasBarrioElem();
                 guardaBarrioActualizacion();
                 Console.setText("Elemento añadido correctamente");
             }catch(Exception e){
                 Console.setText("No se pudo añadir el elemento: "+e.getMessage());
                 
             }
           }
     }//GEN-LAST:event_anadirElemento
 
     private void guardaBarrio(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guardaBarrio
         
         try{
             CtrlBarrio.guardarBarrio();
             Console.setText("Guardado Correctamente");
         }
         catch(Exception e){
             Console.setText(e.getMessage());
         }
     }//GEN-LAST:event_guardaBarrio
 
     private void infoBarrio(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoBarrio
         
         try{
             String[] info = CtrlBarrio.getInfoBarrio();
             String f = new String("LA INFORMACION ACTUAL DEL BARRIO ES LA SIGUIENTE:\n");
             f = f.concat("Nombre del Barrio: " + info[0]+"\n");
             int t = (int) Integer.parseInt(info[1]);
             String tipo=null;
             switch(t){
                 case 0: tipo = "Sin Tipo";
                         break;
                 case 1: tipo = "Gama Baja";
                         break;
                 case 2: tipo = "Gama Media";
                         break;
                 case 3: tipo = "Gama Alta";
                         break;
             }
             f = f.concat("Tipo de Barrio: " + tipo+"\n");
             f = f.concat("Poblacion deseada: "+info[3]+"\n");
             f = f.concat("Poblacion posible: " + info[8]+"\n");
             f = f.concat("Presupuesto total: " + info[2]+"\n");
             f = f.concat("Presupuesto gastado: " + info[4]+"\n");
             f = f.concat("---Presupuesto gastado en Viviendas:" + info[5]+"\n");
             f = f.concat("---Presupuesto gastado en Servicios Publicos: " + info[6]+"\n");
             f = f.concat("---Presupuesto gastado en Comercios: " + info[7]+"\n");
             f = f.concat("Capacidad de Servicios de Sanidad: " + info[9]+"\n");
             f = f.concat("Capacidad de Servicios de Educacion: " + info[10]+"\n");
             f = f.concat("Capacidad de Servicios de Seguridad: " + info[11]+"\n");
             f = f.concat("Capacidad de Servicios de Comunicacion: " + info[12]+"\n");
             f = f.concat("Capacidad de Servicios de Ocio: " + info[13]+"\n");
             f = f.concat("Capacidad de Comercio: " + info[14]+"\n");
             
             Console.setText(f); 
         }
         catch(Exception e){
             
         }
     }//GEN-LAST:event_infoBarrio
 
     private void elimElem(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_elimElem
         Set<String> elementos = CtrlElem.ListaNombreElementos();
         Object[] list = new Object[elementos.size()+1];
         Iterator j = elementos.iterator();
         for(int i=0;j.hasNext();++i){
             list[i] = j.next();
             //elementos.iterator().
         }
         
         Object seleccion = JOptionPane.showInputDialog(
                             this,
                             "Seleccione el elemento que desea eliminar ",
                             "Eliminar Elemento",
                             JOptionPane.QUESTION_MESSAGE,
                             null,  // null para icono defecto
                              list," ");
         if(seleccion!=null){
             try{
                 try{
                     CtrlElem.eliminarElemento(seleccion.toString());
                     initViviendas();
                     initPublicos();
                     initComercios();
                     Console.setText("El Elemento "+seleccion.toString()+" ha sido eliminado correctamente");
                 }
                 catch(Exception e){
                     Console.setText(e.getMessage());
                 }
                 
             }
             catch(Exception e){
                 Console.setText("Introduzca un nombre porfavor");
                 JOptionPane warning = new JOptionPane();
                 //warning.
             }
         }
     }//GEN-LAST:event_elimElem
 
     private void elimRest(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_elimRest
     Set<String> rest = CtrlRest.ListaNombreRestricciones();
         Object[] list = new Object[rest.size()+1];
         Iterator j = rest.iterator();
         for(int i=0;j.hasNext();++i){
             list[i] = j.next();
             //elementos.iterator().
         }
         
         Object seleccion = JOptionPane.showInputDialog(
                             this,
                             "Eliminar restriccion",
                             "Seleccione la restriccion que desea eliminar",
                             JOptionPane.QUESTION_MESSAGE,
                             null,  // null para icono defecto
                              list,"  ");
         if(seleccion!=null){
             try{
                 try{
                     CtrlRest.Eliminar_Restriccion(seleccion.toString());
                     initUbicacion();
                     initDemograficas();
                     initEconomicas();
                     /*for(int i = 0;i<tablaUbic.getRowCount();++i){
                         if(tablaUbic.getValueAt(i, 0).equals(seleccion)){
                             tablaUbic.removeRow(i);
                             tablaUbic.moveRow(i+1, tablaUbic.getRowCount()-1, i);
                             --indiceU;
                         }
                     }
                     for(int i = 0;i<tablaDem.getRowCount();++i){
                         if(tablaDem.getValueAt(i, 0).equals(seleccion)){
                             tablaDem.removeRow(i);
                             tablaDem.moveRow(i+1, tablaDem.getRowCount()-1, i);
                             --indiceD;
                         }
                     }
                     for(int i = 0;i<tablaEco.getRowCount();++i){
                         if(tablaEco.getValueAt(i, 0).equals(seleccion)){
                             tablaEco.removeRow(i);
                             tablaEco.moveRow(i+1, tablaEco.getRowCount()-1, i);
                             --indiceE;
                         }
                     }*/
                     Console.setText("La restriccion "+seleccion.toString()+" ha sido eliminado correctamente");
                 }
                 catch(Exception e){
                     Console.setText(e.getMessage());
                 }
                 
             }
             catch(Exception e){
                 Console.setText("Introduzca un nombre porfavor");
                 JOptionPane warning = new JOptionPane();
                 //warning.
             }
         }
     }//GEN-LAST:event_elimRest
 
     private void creaRes(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_creaRes
         Set<String> elementos = CtrlElem.ListaNombreElementos();
         Object[] list = new Object[elementos.size()+1];
         Iterator j = elementos.iterator();
         for(int i=0;j.hasNext();++i){
             list[i] = j.next();
             //elementos.iterator().
         }
         
         formNewRestriccion formulario = new formNewRestriccion(this,true,list);
         formulario.setVisible(true);
         
         if(formulario.aceptado()){
             try{
                 String nombre = formulario.Nombre();
                 System.out.println(formulario.getTipo());
                 if(formulario.getTipo() == 1) {
                     try{
                         String e1 = formulario.getElemUb1();
                         String e2 = formulario.getElemUb2();
                         int distancia = formulario.getDistancia();
                         try{
                             CtrlRest.CrearRestriccion(nombre, "ubicacion", e1, e2, distancia, 0, 0);
                             initUbicacion();
                             Console.setText("Restriccion de ubicacion creada correctamente");
                         }
                         catch(Exception e){
                             Console.setText(e.getMessage());
                         }
                     }
                     catch(Exception e){
                         Console.setText("Parametros no introducidos correctamente");
                     }
                         
                 }
                 
                 if(formulario.getTipo() == 2) {
                     try{
                         String e1 = formulario.getElemDem();
                         int hab = formulario.getHabitantes();
                         try{
                             CtrlRest.CrearRestriccion(nombre, "demografica", e1, "-1", hab, 0, 0);
                             initDemograficas();
                             Console.setText("Restriccion de ubicacion creada correctamente");
                         }
                         catch(Exception e){
                             Console.setText(e.getMessage());
                         }
                     }
                     catch(Exception e){
                         Console.setText("Parametros no introducidos correctamente");
                     }
                         
                 }
                 
                 if(formulario.getTipo() == 3) {
                     try{
                         int [] pres = formulario.getPresupuestos();
                         try{
                             CtrlRest.CrearRestriccion(nombre, "economica", "-1", "-1", pres[0], pres[1], pres[2]);
                             initEconomicas();
                             Console.setText("Restriccion de ubicacion creada correctamente");
                         }
                         catch(Exception e){
                             Console.setText(e.getMessage());
                         }
                     }
                     catch(Exception e){
                         Console.setText("Parametros no introducidos correctamente");
                     }
                         
                 }
 
             }
             catch(Exception e){
                 Console.setText("Introduzca un nombre para la restriccion");
             }
         }   
     }//GEN-LAST:event_creaRes
 
     private void cargarBarrio(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cargarBarrio
         ArrayList<String> barrios = CtrlBarrio.listarBarrios();
         Object[] list = new Object[barrios.size()];
         for(int i=0;i<barrios.size();++i){
             list[i] = barrios.get(i);
         }
         
         Object seleccion = JOptionPane.showInputDialog(
                             this,
                             "Eliminar Barrio",
                             "Seleccione el barrio que desea eliminar",
                             JOptionPane.QUESTION_MESSAGE,
                             null,  // null para icono defecto
                              list," ");
         if(seleccion!=null){
             try {
                 int tipo = CtrlBarrio.cargarBarrio(seleccion.toString());
                 initTablasBarrioElem();
                 initTablasBarrioRest();
                 barrioCargado();
                 if(tipo==1)modoLibre();
                 Console.setText("Barrio Cargado correctamentes");
             } catch (Exception ex) {
                 System.out.println("CHIVATOO");
                 Console.setText(ex.getMessage());
             }
         }
     }//GEN-LAST:event_cargarBarrio
 
     private void anadirRestBarrio(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anadirRestBarrio
         Set<String> rest = CtrlRest.ListaNombreRestricciones();
         Object[] list = new Object[rest.size()+1];
         Iterator j = rest.iterator();
         for(int i=0;j.hasNext();++i){
             list[i] = j.next();
             //elementos.iterator().
         }
         
         Object seleccion = JOptionPane.showInputDialog(
                             this,
                             "Añadir restriccion al barrio",
                             "Seleccione la restriccion que desea añadir",
                             JOptionPane.QUESTION_MESSAGE,
                             null,  // null para icono defecto
                              list,"  ");
         if(seleccion!=null){
             try{
                 CtrlBarrio.anadirRestBarrio(seleccion.toString());
                 initTablasBarrioRest();
                 guardaBarrioActualizacion();
                 Console.setText("La Restriccion "+seleccion.toString()+" ha sido añadida a tu barrio!");
                 
             }catch(Exception e){
                 Console.setText(e.getMessage());
             }
         }
         
     }//GEN-LAST:event_anadirRestBarrio
 
     private void anadirCarretera(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anadirCarretera
         // TODO add your handling code here:
     }//GEN-LAST:event_anadirCarretera
 
     private void eliminarCarretera(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminarCarretera
         // TODO add your handling code here:
     }//GEN-LAST:event_eliminarCarretera
 
     private void editPresupuesto(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editPresupuesto
         FormPresPob formulario = new FormPresPob(this,true,null);
         formulario.setVisible(true);
         if(formulario.aceptado()){
             try{
                 int cantidad = formulario.getCantidad();
                 try{
                     CtrlBarrio.setPresupuestoBarrio(cantidad);
                     guardaBarrioActualizacion();
                 }catch(Exception e){
                     Console.setText(e.getMessage());
                 }
             }catch(Exception e){
                 Console.setText("Introduza una cantidad porfavor");
                 JOptionPane.showMessageDialog(
                             this,
                             "NO VAYAS DE TROLL Y PON UNA CANTIDAD!!");
             }
         }
     }//GEN-LAST:event_editPresupuesto
 
     private void editPoblacion(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editPoblacion
         FormPresPob formulario = new FormPresPob(this,true,null);
         formulario.setVisible(true);
         if(formulario.aceptado()){
             try{
                 int cantidad = formulario.getCantidad();
                 try{
                     CtrlBarrio.setPoblacionbarrio(cantidad);
                     guardaBarrioActualizacion();
                 }catch(Exception e){
                     Console.setText(e.getMessage());
                 }
             }catch(Exception e){
                 Console.setText("Introduza una cantidad porfavor");
                 JOptionPane.showMessageDialog(
                             this,
                             "NO VAYAS DE TROLL Y PON UNA CANTIDAD!!");
             }
         }
     }//GEN-LAST:event_editPoblacion
 
     private void generarBarrio(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generarBarrio
         int seleccion = JOptionPane.showOptionDialog(
                         this,
                         "El barrio actual se perdera, quiere continuar de todos modos?", 
                         "Generar Barrio",
                         JOptionPane.YES_NO_CANCEL_OPTION,
                         JOptionPane.QUESTION_MESSAGE,
                         null,    // null para icono por defecto.
                         new Object[] { "Si", "No"},   // null para YES, NO y CANCEL
                         "Si");
         if(seleccion==0){
             int seleccion2 = JOptionPane.showOptionDialog(
                             this,
                             "Desea comprobar si dispone de suficientes servicios publicos para atender a la poblacion? ", 
                             "Generar Barrio",
                             JOptionPane.YES_NO_CANCEL_OPTION,
                             JOptionPane.QUESTION_MESSAGE,
                             null,    // null para icono por defecto.
                             new Object[] { "Si", "No"},   // null para YES, NO y CANCEL
                             "Si");
             if(seleccion2==0){
                 try {
                     computando = true;
                     lanzathread(true);
                     //mensajeEnGeneracion();
                 } catch (Exception ex) {
                     Console.setText(ex.getMessage());
                 }
             }
             else {
                try {
                     computando = true;
                     lanzathread(false);
                     //mensajeEnGeneracion();                   
                 } catch (Exception ex) {
                     Console.setText(ex.getMessage());
                 } 
             }
             
         }
         mostrarBarrio();
     }//GEN-LAST:event_generarBarrio
 
     private void elimResBarrio(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_elimResBarrio
         
         String[][] rest = CtrlBarrio.listarCjtRestBarrio();
         Object[] list = new Object[rest.length+1];
         for(int i=0;i<rest.length;++i){
             list[i] = rest[i][1];
         }
         
         Object seleccion = JOptionPane.showInputDialog(
                             this,
                             "Añadir restriccion al barrio",
                             "Seleccione la restriccion que desea añadir",
                             JOptionPane.QUESTION_MESSAGE,
                             null,  // null para icono defecto
                              list,"  ");
         if(seleccion!=null){
             try{
                 CtrlBarrio.quitarRestBarrio(seleccion.toString());
                 initTablasBarrioRest();
                 guardaBarrioActualizacion();
                 Console.setText("La Restriccion "+seleccion.toString()+" se elimino correctamente de tu barrio");
             }catch(Exception e){
                 Console.setText(e.getMessage());
             }
         }
     }//GEN-LAST:event_elimResBarrio
 
     private void QuitarElemento(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_QuitarElemento
         String[][] rest = CtrlBarrio.listarCjtElemBarrio();
         Object[] list = new Object[rest.length+1];
         for(int i=0;i<rest.length;++i){
             list[i] = rest[i][3];
         }
         
         Object seleccion = JOptionPane.showInputDialog(
                             this,
                             "Añadir restriccion al barrio",
                             "Seleccione la restriccion que desea añadir",
                             JOptionPane.QUESTION_MESSAGE,
                             null,  // null para icono defecto
                              list,"  ");
         FormPresPob formulario = new FormPresPob(this,true,"Cuantos quieres eliminar?");
         formulario.setVisible(true);
         if(seleccion!=null && formulario.aceptado()){
             try{
                 CtrlBarrio.quitarElemento(seleccion.toString(),formulario.getCantidad());
                 initTablasBarrioElem();
                 guardaBarrioActualizacion();
                 Console.setText("Se han quitado "+formulario.getCantidad()+" "+seleccion.toString()+" de tu barrio");
             }catch(Exception e){
                 Console.setText(e.getMessage());
             }
         }
     }//GEN-LAST:event_QuitarElemento
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(vistaGrafica.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(vistaGrafica.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(vistaGrafica.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(vistaGrafica.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new vistaGrafica().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JTextPane Console;
     private javax.swing.JMenuItem CreaElem;
     private javax.swing.JMenuItem Eliminar_barrio;
     private javax.swing.JButton InfoBarrio;
     private javax.swing.JScrollPane ScrollsysRest;
     private javax.swing.JScrollPane ScrollsysRest1;
     private javax.swing.JButton addElem;
     private javax.swing.JMenuItem cargaBarrioMenu;
     private javax.swing.JTabbedPane elemSisTab;
     private javax.swing.Box.Filler filler1;
     private javax.swing.Box.Filler filler2;
     private javax.swing.JMenu gestBarrio;
     private javax.swing.JMenuItem guardarBarrio;
     private javax.swing.JButton jButton10;
     private javax.swing.JButton jButton2;
     private javax.swing.JButton jButton3;
     private javax.swing.JButton jButton4;
     private javax.swing.JButton jButton5;
     private javax.swing.JButton jButton6;
     private javax.swing.JButton jButton8;
     private javax.swing.JButton jButton9;
     private javax.swing.JLayeredPane jLayeredPane1;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JMenuItem jMenuItem2;
     private javax.swing.JMenuItem jMenuItem4;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPopupMenu jPopupMenu1;
     private javax.swing.JPopupMenu jPopupMenu2;
     private javax.swing.JPopupMenu jPopupMenu3;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane10;
     private javax.swing.JScrollPane jScrollPane11;
     private javax.swing.JScrollPane jScrollPane12;
     private javax.swing.JScrollPane jScrollPane13;
     private javax.swing.JScrollPane jScrollPane15;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane5;
     private javax.swing.JScrollPane jScrollPane6;
     private javax.swing.JScrollPane jScrollPane7;
     private javax.swing.JScrollPane jScrollPane8;
     private javax.swing.JScrollPane jScrollPane9;
     private javax.swing.JSplitPane jSplitPane1;
     private javax.swing.JSplitPane jSplitPane3;
     private javax.swing.JSplitPane jSplitPane4;
     private javax.swing.JTabbedPane jTabbedPane2;
     private javax.swing.JTabbedPane jTabbedPane4;
     private javax.swing.JTabbedPane jTabbedPane5;
     private javax.swing.JTabbedPane jTabbedPane6;
     private javax.swing.JTabbedPane jTabbedPane7;
     private javax.swing.JToolBar jToolBar1;
     private javax.swing.JMenuItem newBar;
     private javax.swing.JMenu newElem;
     private javax.swing.JMenu newRes;
     private javax.swing.JTable tabCom;
     private javax.swing.JTable tabComU;
     private javax.swing.JTable tabDem;
     private javax.swing.JTable tabDemU;
     private javax.swing.JTable tabEco;
     private javax.swing.JTable tabEcoU;
     private javax.swing.JTable tabPub;
     private javax.swing.JTable tabPubU;
     private javax.swing.JTable tabUbi;
     private javax.swing.JTable tabUbiU;
     private javax.swing.JTable tabViv;
     private javax.swing.JTable tabVivU;
     private javax.swing.JPanel viewBarrio;
     // End of variables declaration//GEN-END:variables
 }
