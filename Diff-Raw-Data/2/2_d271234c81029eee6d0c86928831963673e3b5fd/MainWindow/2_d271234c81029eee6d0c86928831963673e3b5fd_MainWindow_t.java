 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fr.Interface;
 
 /**
  *
  * @author Florian
  */
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 
 import net.fortuna.ical4j.data.ParserException;
 import fr.Controler.EventOperationObserver;
 import fr.Controler.GestionnaireEDT;
 import fr.Model.ICalEvent;
 import fr.utilities.MyModel;
 
 public class MainWindow extends JFrame {
 
	private static final String TEXTINFORMATIONDEFAULT = "--- Informations sur le module selectionné dans le tableau de gauche---";
 	
 	private GestionnaireEDT mon_gestionnaire = GestionnaireEDT.getInstance();
 	/* Menu déroulant*/
 	private JMenuBar my_menuBar = new JMenuBar();
 	/* Onglets */           
 	private JMenu tab1 = new JMenu("Configuration Calendrier");  
 	private JMenu tab2 = new JMenu("Aide");
 	/* Items pour onglets */
 	private JMenuItem item1_1 = new JMenuItem("Créer un Calendrier");
 	private JMenuItem item1_2 = new JMenuItem("Charger Calendrier");
 	private JMenuItem item1_3 = new JMenuItem("Importer un ICS");
 	private JMenuItem item1_4 = new JMenuItem("Exporter en ICS");
 	private JMenuItem item2_1 = new JMenuItem("Equipe de Developpement");
 	/* Boutons */
 	private JButton addButton = new JButton("Ajouter");
 	private JButton modifyButton = new JButton("Modifier");
 	private JButton deleteButton = new JButton("Supprimer");
 
 	private JTextArea textInformation = new JTextArea();
 	private JPanel my_panel1 = new JPanel();
 	private JPanel my_panel2 = new JPanel();
 
 	private JTable board;
 	
 	private ImageIcon developpeur;
 
 	public MainWindow(){
 		this.setTitle("Logiciel de gestion d'emploi du temps");
 		this.setSize(900,550);
 		this.setLocationRelativeTo(null);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
 		
 		this.addButton.setEnabled(false);
 		this.modifyButton.setEnabled(false);
 		this.deleteButton.setEnabled(false);
 
 		//addind items to JBarMenu
 		this.tab1.add(item1_1);
 		this.tab1.add(item1_2);
 		this.tab1.add(item1_3);
 		this.tab1.add(item1_4);
 		this.tab2.add(item2_1);
 		
 		item1_1.addActionListener(new connectToCalendar());
 		item1_2.addActionListener(new connectForLoading());
 		//item1_2.addActionListener(new SyncronizeWithCalendar());
 		item1_3.addActionListener(new loadingFile());
 		//item1_4.addActionListener();
 		item2_1.addActionListener(new ItemListener());
 
 		this.my_menuBar.add(tab1);
 		this.my_menuBar.add(tab2);
 
 		this.setJMenuBar(my_menuBar);
 
 		//ActionListeners and JButtons
 		addButton.addActionListener(new AddEvent());
 		modifyButton.addActionListener(new ModifyEvent());
 		deleteButton.addActionListener(new DelRowListener());
 
 		//Les données du tableau
 		Object[][] donnees = {
 		};
 
 		//Les titres des colonnes
 		String titres[] = {"Date", "Horaires", "Matières"};
 
 		textInformation = new JTextArea(TEXTINFORMATIONDEFAULT);  
 		textInformation.setEditable(false);
 
 		//my_panel1.add(textInformation);
 		my_panel2.add(addButton);
 		my_panel2.add(modifyButton);
 		my_panel2.add(deleteButton);
 
 		MyModel model = new MyModel(donnees, titres);
 		board = new JTable(model);
 
 		//Definition d'un nouveau MouseListener
 		board.addMouseListener(new MyMouseListener());
 
 
 		//my_panel2.add(new JScrollPane(board));
 
 		this.getContentPane().add(new JScrollPane(board), BorderLayout.WEST);
 		this.getContentPane().add(new JScrollPane(textInformation), BorderLayout.EAST);
 		this.getContentPane().add(my_panel2, BorderLayout.SOUTH);
 		
 		//ajout de l'observateur de GestionnaireEDT
 		EventOperationObserver eoo = new EventOperationObserver();
 		mon_gestionnaire.addObserver(eoo);
 		
 	}
 	
 	private JFrame getMainWindow(){
 		return this;
 	}
 	
 	public void addButtonEnable(boolean b){
 		addButton.setEnabled(b);
 	}
 
 	class MyMouseListener implements MouseListener{
 		//Redefinition d'un MouseListener
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			int selectedRow = board.getSelectedRow();
 			MyModel modelTemp = (MyModel)board.getModel();
 			HashMap<String,ICalEvent> listTemp = mon_gestionnaire.getICalEvents();
 			ICalEvent event = listTemp.get(modelTemp.getValueAt(selectedRow, 3));
 			textInformation.setText("---------- Informations ----------" + "\n" +
 					event.toString() + "\n" +
 					"Uid : " + event.getUID() + "\n");
 			modifyButton.setEnabled(true);
 			deleteButton.setEnabled(true);
 		}
 
 		@Override
 		public void mousePressed(MouseEvent me) {
 			//throw new UnsupportedOperationException("Not supported yet.");
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent me) {
 			//throw new UnsupportedOperationException("Not supported yet.");
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent me) {
 			//throw new UnsupportedOperationException("Not supported yet.");
 		}
 
 		@Override
 		public void mouseExited(MouseEvent me) {
 			//throw new UnsupportedOperationException("Not supported yet.");
 		}
 	};
 
 	class loadingFile implements ActionListener{
 		@Override 
 		public void actionPerformed(ActionEvent arg0){
 			try {						
 				mon_gestionnaire.remplirList("myEDT");
 				MyModel modelTemp = (MyModel)board.getModel();
 				board.removeAll();
 				for (ICalEvent e : mon_gestionnaire.getICalEvents().values()){
 					String date = e.getdBegin().toDate();
 					String hour = e.getdBegin().toHour() + "-" + e.getdEnd().toHour();
 					String module = e.getModule();
 					String uid = e.getUID();
 					Object[] contenu = {date, hour, module,uid}; 
 					modelTemp.addRow(contenu); 	
 				}               
 			} catch (FileNotFoundException ex) {
 				Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
 			} catch (IOException ex) {
 				Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
 			} catch (ParserException ex) {
 				Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
 			}          
 		}
 	}
 	
 	class connectToCalendar implements ActionListener{
 		@Override 
 		public void actionPerformed(ActionEvent arg0){
 			CreateCalendarWindow connecWind = new CreateCalendarWindow(getMainWindow(), board);
 			connecWind.setVisible(true);
 		}
 	}
 
 
 	class AddEvent implements ActionListener{
 		//Redefintion de la methode actionPerformed()
 		@Override
 		public void actionPerformed(ActionEvent arg0) {  
 			EventWindow ev = new AddEventWindow(null, true, board);
 			ev.setVisible(true);
 		}
 	}
 	
 	class connectForLoading implements ActionListener{
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			LoadCalendarWindow ev = new LoadCalendarWindow(getMainWindow(), board);
 			ev.setVisible(true);
 		}
 	}
 	
 //	class SyncronizeWithCalendar implements ActionListener{
 //		@Override 
 //		public void actionPerformed(ActionEvent arg0){
 //			mon_gestionnaire.remplirList();
 //			MyModel modelTemp = (MyModel)board.getModel();
 //			board.removeAll();
 //			for (ICalEvent e : mon_gestionnaire.getICalEvents().values()){
 //				String date = e.getdBegin().toDate();
 //				String hour = e.getdBegin().toHour() + "-" + e.getdEnd().toHour();
 //				String module = e.getModule();
 //				String uid = e.getUID();
 //				Object[] contenu = {date, hour, module,uid}; 
 //				modelTemp.addRow(contenu); 	
 //			}  
 //			addButton.setEnabled(true);
 //		}
 //	}
 	
 	class ModifyEvent implements ActionListener{
 		//Redefintion de la methode actionPerformed()
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			MyModel modelTemp = (MyModel)board.getModel();
 			HashMap<String,ICalEvent> listTemp = mon_gestionnaire.getICalEvents();
 			ICalEvent event = listTemp.get(modelTemp.getValueAt(board.getSelectedRow(), 3));
 			EventWindow ev = new ModifEventWindow(null, true, board, event);
 			ev.setVisible(true);
 		}
 	}
 
 
 	class DelRowListener implements ActionListener{
 		//Redefintion de la methode actionPerformed()
 		@Override
 		public void actionPerformed(ActionEvent arg0){   
 			int selectedRow = board.getSelectedRow(); 
 			MyModel modelTemp = (MyModel)board.getModel();
 			boolean rec = mon_gestionnaire.eventIsRecurent(modelTemp.getValueAt(selectedRow, 3));
 			if(rec)
 			{
 				ArrayList<String> asup = mon_gestionnaire.removeRecurentEvent(modelTemp.getValueAt(selectedRow, 3));
 				for(String s: asup){
 					modelTemp.removeRow(modelTemp.getARowOf(s, 3));
 				}
 			} else {
 				mon_gestionnaire.removeEvent(modelTemp.getValueAt(selectedRow, 3));
 				modelTemp.removeRow(selectedRow);
 			}
 			
 			//suppression du contenu de la zone de description de l'event
 			textInformation.setText(TEXTINFORMATIONDEFAULT);
 			board.clearSelection();
 			deleteButton.setEnabled(false);
 			modifyButton.setEnabled(false);
 		}
 	}
 
 	class ItemListener implements ActionListener{
 		//Redefinition de la methode actionPerformed()
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			developpeur = new ImageIcon("resources/developpeur.png");
 			JOptionPane about = new JOptionPane();
 			about.showMessageDialog(null, "Ce logiciel à été développé par : Florian FAGNIEZ,\nGuillaume Coutable et Noémie RULLIER \nM1 ALMA - TPA", "Equipe de développement", JOptionPane.INFORMATION_MESSAGE, developpeur);
 		}
 	}
 
 	public static void main(String[] args){
 		MainWindow fen = new MainWindow();
 		fen.setVisible(true);
 	}
 
 }
