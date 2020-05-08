 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package supervision;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import javax.swing.*;
 import model.Schedule;
 
 
 /**
  *
  * @author Mignot
  */
 public class Fenetre extends Frame {
 
         private static enum Mode {CREATION, MODIFICATION};
         private Mode mode;
 	private final String TITRE = "Supervision des Livraisons Itinérantes Planifiés";
         private final String CHAMP_INDISP_CREA = "(ce champ est indisponible en"
                 + " mode création)";
         private final String CHAMP_NO_LIV_SELEC = "Aucune livraison n'est sélectionnée";
 	private Fenetre fenetre;
 	private int selectedZone;
 	private boolean masquerPopUpZone = false;
 	private Controleur controleur;
 	private JFileChooser jFileChooserXML;
 	private JFileChooser jFileChooserA;
	private Menu menuFichier;
	private Menu menuEdition;

 
         private ArrayList<Schedule> schedules;
         private ArrayList<JToggleButton> jToggleButtonSchedules;
         
         private ListLivraison listeLivraison;
 
 	/**
 	 * Creates new form Fenetre
 	 */
 	public Fenetre() {
 		fenetre=this;
 		initComponents();
 		this.setTitle(TITRE);
 		creeMenu();
 		setFonts();
 		setPopups();
 		setMode(Mode.CREATION);
 	}
 
     public void setSchedules(ArrayList<Schedule> aschedules) {
         this.schedules = aschedules;
         jToggleButtonSchedules = new ArrayList<JToggleButton>();
         jPanelHoraires.removeAll();
         
             System.out.println(schedules.size()+" fe");
         for(int i =0;i<schedules.size();i++)
         {
             String s = ""+(schedules.get(i).getStartTime()/60)+"h - "+
                     +(schedules.get(i).getEndTime()/60)+"h";
             jToggleButtonSchedules.add(new JToggleButton(s));
             ActionListener a = new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     int j=0;
                     JToggleButton jtb = (JToggleButton)e.getSource();
                     for(j=0;j<jToggleButtonSchedules.size();j++)
                     {
                         JToggleButton jtb2 = jToggleButtonSchedules.get(j);
                         if(jtb!=jtb2){
                             jtb2.setSelected(false);
                         }
                     }
                 }
             };
             jToggleButtonSchedules.get(i).addActionListener(a);
             jPanelHoraires.add(jToggleButtonSchedules.get(i));
             listeLivraison = new ListLivraison();
             listeLivraison.setSchedule(schedules);
             jPaneLivraisons.add(listeLivraison);
         }
     }
     
  
         private void setMode(Mode amode) {
             mode=amode;
 		switch(mode){
                     case CREATION:
                         jLabelAddLivPrec.setEnabled(false);
                         jLabelAddLivSuiv.setEnabled(false);
                         jLabelLivPrec.setEnabled(false);
                         jLabelLivSuiv.setEnabled(false);
                         jLabelAddLivPrec.setText(CHAMP_NO_LIV_SELEC+
                                 " "+CHAMP_INDISP_CREA);
                         jLabelAddLivSuiv.setText(CHAMP_NO_LIV_SELEC+
                                 " "+CHAMP_INDISP_CREA);
                         
                         break;
                     case MODIFICATION:
                         jLabelAddLivPrec.setEnabled(true);
                         jLabelAddLivSuiv.setEnabled(true);
                         jLabelLivPrec.setEnabled(true);
                         jLabelLivSuiv.setEnabled(true);
                         jLabelAddLivPrec.setText(CHAMP_NO_LIV_SELEC);
                         jLabelAddLivSuiv.setText(CHAMP_NO_LIV_SELEC);
                         break;
                 }
 	}
 
 	public Dessin getDessin() {
 		return (Dessin)jPanelPlan;
 	}
 
 	public void setControleur(Controleur ctrl) {
 		controleur = ctrl;
 	}
 
 	/**
 	 *  Associe les messages popups aux différentes actions qui les déclenchent
 	 * 
 	 */
 	private void setPopups(){
 		/*Combo box de la zone
 		 * On demande confirmation quand l'utilisateur veut changer de zone
 		 */
 		selectedZone = jComboBoxZone.getSelectedIndex();
 		ItemListener itemListenerZone = new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if(e.getStateChange()==e.SELECTED)
 				{     
 					if(masquerPopUpZone)
 					{
 						masquerPopUpZone=false;
 					}
 					else
 					{
 						Object[] options = { "Confirmer", "Annuler" };
 						int optionChoisie = JOptionPane.showOptionDialog(new JFrame(),
 								"Etes-vous sûr de vouloir changer de zone ?"
 										+ "\n (les données de la tournée en cours seront définitivement effacées)",
 										"Confirmation de changement de zone"+(masquerPopUpZone==true),
 										JOptionPane.OK_CANCEL_OPTION, 
 										JOptionPane.WARNING_MESSAGE, null,
 										options, options[1]);
 
                                                 //Si l'utilisateur annule, on ne fait aucun changement
 						if(optionChoisie==1)
 						{
 							masquerPopUpZone=true;
 							jComboBoxZone.setSelectedIndex(selectedZone);
 						}
 						else
 						{
                                                         setMode(Mode.CREATION);
 							selectedZone=jComboBoxZone.getSelectedIndex();
 						}
 					}
 				}
 			}
 		};
 		jComboBoxZone.addItemListener(itemListenerZone);
 	}
 
 	/**
 	 *  Associe les polices aux différents labels
 	 * 
 	 */
 	private void setFonts(){
 		//Titre edition livraison
 		Font fontEdLivTitre = new Font("Serif", Font.PLAIN, 20);
 		jLabelEdLivTitre.setFont(fontEdLivTitre);
 
 		//Titre livraisons
 		jLabelTitreLivraisons.setFont(fontEdLivTitre);
 	}
 
 	private void creeMenu(){
 		// Creation de deux menus, chaque menu ayant plusieurs items
 		// et association d'un ecouteur d'action a chacun de ces items
 
		menuFichier = new Menu("Fichier");
		menuEdition = new Menu("Edition");
 		ActionListener a4 = new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) 
 			{
                 controleur.exportReport(trouverCheminRapport());
                 setControleur(controleur);
 
 			}
 		};
 		ajoutItem("Generer rapport", menuFichier, a4);
		menuFichier.getItem(0).disable();
 		ActionListener a5 = new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
                             controleur.loadZone(ouvrirFichierXML());
                             setControleur(controleur);
 			}
 		};
 		ajoutItem("Ouvrir un fichier XML", menuFichier, a5);
 
 		ActionListener a6 = new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				throw new UnsupportedOperationException("Not supported yet.");
 			}
 		};
 		ajoutItem("Undo", menuEdition, a6);
 
 		ActionListener a7 = new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				throw new UnsupportedOperationException("Not supported yet.");
 			}
 		};
 		ajoutItem("Redo", menuEdition, a7);
 
 		MenuBar barreDeMenu = new MenuBar();
 		barreDeMenu.add(menuFichier);
 		barreDeMenu.add(menuEdition);
 		this.setMenuBar(barreDeMenu);
 	}
         
 	private File trouverCheminRapport(){
 
 		//Opens filechooser
 		jFileChooserA  = new JFileChooser(JFileChooser.FILE_FILTER_CHANGED_PROPERTY); 
 		jFileChooserA.setDialogTitle("Choisir le dossier ou enregister le rapport");
 		jFileChooserA.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 
 		
         if (jFileChooserA.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) 
         	return fileSaver(jFileChooserA);
 return null;
 	}
 	
 	private File fileSaver(JFileChooser fc)
 	//Saves string to file, pass in FileChooser
 	{
 		File file = fc.getSelectedFile();
 		String textToSave = " ";
 		BufferedWriter writer = null;
 
 		//Check for legal file extension (.txt)	
 		String fileExtension = file.getPath();
 		
 		//Set extension to .txt if not already	
 		if(!fileExtension.toLowerCase().endsWith(".txt"))
 		{
 			Calendar c = Calendar.getInstance ();
 			file = new File(fileExtension + "/Rapport - "+ c.getTime().toString()+".txt");
 		}
 		
 		try
 		{
 			writer = new BufferedWriter( new FileWriter(file));
 			writer.write(textToSave.replaceAll("\n", System.getProperty("line.seperator")));
 			
 			JOptionPane.showMessageDialog(this, "Message saved. (" + file.getName() + ")", "Page Saved Successfully", JOptionPane.INFORMATION_MESSAGE);
 		}
 		catch (IOException e)
 		{ }
 
 		//Close writer
 		finally
 		{
 			try
 			{
 				if(writer != null)
 				{
 					writer.close();
 				}
 			}
 			catch (IOException e)
 			{e.printStackTrace(); }
 		}
 		return file;
 	}
 	//End fileSaver
 	private File ouvrirFichierXML(){
         jFileChooserXML = new JFileChooser();
         // Note: source for ExampleFileFilter can be found in FileChooserDemo,
         // under the demo/jfc directory in the JDK.
         ExampleFileFilter filter = new ExampleFileFilter();
         filter.addExtension("xml");
         filter.setDescription("Fichier XML");
         jFileChooserXML.setFileFilter(filter);
         jFileChooserXML.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
         if (jFileChooserXML.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) 
                 return new File(jFileChooserXML.getSelectedFile().getAbsolutePath());
         return null;
 	}
 
 	private void ajoutItem(String intitule, Menu menu, ActionListener a){
 		MenuItem item = new MenuItem(intitule);
 		menu.add(item);
 		item.addActionListener(a);
 	}
 
 	/**
 	 * This method is called from within the constructor to initialize the form.
 	 * WARNING: Do NOT modify this code. The content of this method is always
 	 * regenerated by the Form Editor.
 	 */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPanelGauche = new javax.swing.JPanel();
         jPanelBoutonsGen = new javax.swing.JPanel();
         jButtonUndo = new javax.swing.JButton();
         jButtonRedo = new javax.swing.JButton();
         jComboBoxZone = new javax.swing.JComboBox();
         jFormattedTextFieldDate = new javax.swing.JFormattedTextField();
         jButtonGenTourn = new javax.swing.JButton();
         jButtonFinal = new javax.swing.JButton();
         jPanelPlan = new Dessin();
         jPanelEditionLivraison = new javax.swing.JPanel();
         jLabelEdLivTitre = new javax.swing.JLabel();
         jButtonSupprimerLiv = new javax.swing.JButton();
         jLabelAddLivCurr = new javax.swing.JLabel();
         jButtonValiderLiv = new javax.swing.JButton();
         jPanelHoraires = new javax.swing.JPanel();
         jLabelLivCurr = new javax.swing.JLabel();
         jLabelLivPrec = new javax.swing.JLabel();
         jLabelAddLivPrec = new javax.swing.JLabel();
         jLabelLivSuiv = new javax.swing.JLabel();
         jLabelAddLivSuiv = new javax.swing.JLabel();
         jPanelDroite = new javax.swing.JPanel();
         jLabelTitreLivraisons = new javax.swing.JLabel();
         jPaneLivraisons = new javax.swing.JPanel();
 
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 exitForm(evt);
             }
         });
 
         jPanelGauche.setBackground(new java.awt.Color(102, 0, 153));
 
         jPanelBoutonsGen.setBackground(new java.awt.Color(255, 204, 102));
 
         jButtonUndo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonUndoActionPerformed(evt);
             }
         });
 
         jComboBoxZone.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
         jComboBoxZone.setToolTipText("Changer de zone");
 
         jFormattedTextFieldDate.setText("Date de la tournée");
         jFormattedTextFieldDate.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jFormattedTextFieldDateActionPerformed(evt);
             }
         });
 
         jButtonGenTourn.setText("Générer tournée");
         jButtonGenTourn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonGenTournActionPerformed(evt);
             }
         });
 
         jButtonFinal.setText("Finaliser tournée");
 
         javax.swing.GroupLayout jPanelBoutonsGenLayout = new javax.swing.GroupLayout(jPanelBoutonsGen);
         jPanelBoutonsGen.setLayout(jPanelBoutonsGenLayout);
         jPanelBoutonsGenLayout.setHorizontalGroup(
             jPanelBoutonsGenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelBoutonsGenLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jButtonUndo, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jButtonRedo, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jComboBoxZone, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jFormattedTextFieldDate, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jButtonGenTourn, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jButtonFinal, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         jPanelBoutonsGenLayout.setVerticalGroup(
             jPanelBoutonsGenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jButtonRedo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addComponent(jButtonUndo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addGroup(jPanelBoutonsGenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jComboBoxZone, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(jFormattedTextFieldDate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(jButtonGenTourn)
                 .addComponent(jButtonFinal))
         );
 
         jPanelPlan.setFenetre(this);
         jPanelPlan.setBackground(new java.awt.Color(102, 102, 255));
 
         jPanelEditionLivraison.setBackground(new java.awt.Color(0, 153, 153));
 
         jLabelEdLivTitre.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabelEdLivTitre.setText("Edition de livraison");
 
         jButtonSupprimerLiv.setText("Supprimer");
         jButtonSupprimerLiv.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonSupprimerLivActionPerformed(evt);
             }
         });
 
         jLabelAddLivCurr.setText("Aucune livraison sélectionnée");
 
         jButtonValiderLiv.setText("Valider");
 
         jPanelHoraires.setBackground(new java.awt.Color(255, 255, 0));
         jPanelHoraires.setLayout(new java.awt.GridLayout(1, 0));
 
         jLabelLivCurr.setText("Adresse de livraison :");
 
         jLabelLivPrec.setText("Livraison précedente :");
 
         jLabelAddLivPrec.setText("Aucune livraison sélectionnée");
 
         jLabelLivSuiv.setText("Livraison suivante :");
 
         jLabelAddLivSuiv.setText("Aucune livraison sélectionnée");
 
         javax.swing.GroupLayout jPanelEditionLivraisonLayout = new javax.swing.GroupLayout(jPanelEditionLivraison);
         jPanelEditionLivraison.setLayout(jPanelEditionLivraisonLayout);
         jPanelEditionLivraisonLayout.setHorizontalGroup(
             jPanelEditionLivraisonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanelHoraires, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addGroup(jPanelEditionLivraisonLayout.createSequentialGroup()
                 .addGroup(jPanelEditionLivraisonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanelEditionLivraisonLayout.createSequentialGroup()
                         .addGroup(jPanelEditionLivraisonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabelLivCurr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jLabelLivPrec, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jLabelLivSuiv, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanelEditionLivraisonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(jLabelAddLivPrec, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                             .addComponent(jLabelAddLivCurr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jLabelAddLivSuiv, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                     .addGroup(jPanelEditionLivraisonLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jLabelEdLivTitre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButtonValiderLiv)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButtonSupprimerLiv)))
                 .addContainerGap())
         );
         jPanelEditionLivraisonLayout.setVerticalGroup(
             jPanelEditionLivraisonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelEditionLivraisonLayout.createSequentialGroup()
                 .addGroup(jPanelEditionLivraisonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabelEdLivTitre, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButtonSupprimerLiv, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButtonValiderLiv, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGroup(jPanelEditionLivraisonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabelLivCurr, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabelAddLivCurr, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGroup(jPanelEditionLivraisonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabelLivPrec, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabelAddLivPrec, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGroup(jPanelEditionLivraisonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabelLivSuiv, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabelAddLivSuiv, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(1, 1, 1)
                 .addComponent(jPanelHoraires, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
 
         javax.swing.GroupLayout jPanelGaucheLayout = new javax.swing.GroupLayout(jPanelGauche);
         jPanelGauche.setLayout(jPanelGaucheLayout);
         jPanelGaucheLayout.setHorizontalGroup(
             jPanelGaucheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanelPlan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jPanelBoutonsGen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jPanelEditionLivraison, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanelGaucheLayout.setVerticalGroup(
             jPanelGaucheLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelGaucheLayout.createSequentialGroup()
                 .addComponent(jPanelBoutonsGen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanelPlan, javax.swing.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanelEditionLivraison, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         jPanelDroite.setBackground(new java.awt.Color(51, 255, 51));
 
         jLabelTitreLivraisons.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabelTitreLivraisons.setText("Liste des livraisons");
 
         jPaneLivraisons.setBackground(new java.awt.Color(255, 0, 0));
         jPaneLivraisons.setLayout(new java.awt.BorderLayout());
 
         javax.swing.GroupLayout jPanelDroiteLayout = new javax.swing.GroupLayout(jPanelDroite);
         jPanelDroite.setLayout(jPanelDroiteLayout);
         jPanelDroiteLayout.setHorizontalGroup(
             jPanelDroiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelDroiteLayout.createSequentialGroup()
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(jLabelTitreLivraisons, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(64, 64, 64))
             .addGroup(jPanelDroiteLayout.createSequentialGroup()
                 .addComponent(jPaneLivraisons, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE))
         );
         jPanelDroiteLayout.setVerticalGroup(
             jPanelDroiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelDroiteLayout.createSequentialGroup()
                 .addComponent(jLabelTitreLivraisons, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPaneLivraisons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jPanelGauche, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanelDroite, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanelDroite, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jPanelGauche, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
         
     public void nodeClicked(int id)
     {
         jLabelAddLivCurr.setText(""+id);
     }
     
     
     
 	/**
 	 * Exit the Application
 	 */
 	private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
 		System.exit(0);
 	}//GEN-LAST:event_exitForm
 
 	private void jFormattedTextFieldDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextFieldDateActionPerformed
 		// TODO add your handling code here:
 	}//GEN-LAST:event_jFormattedTextFieldDateActionPerformed
 
 	private void jButtonUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUndoActionPerformed
 		// TODO add your handling code here:
 	}//GEN-LAST:event_jButtonUndoActionPerformed
 
     private void jButtonGenTournActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGenTournActionPerformed
         setMode(Mode.MODIFICATION);
        //controleur.genererTournee();
        menuFichier.getItem(0).enable();
     }//GEN-LAST:event_jButtonGenTournActionPerformed
     
     private void jButtonValiderLivActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonValiderLivActionPerformed
     	if (controleur.getEtat() == Etat.REMPLISSAGE) {
     		controleur.add();
     	}
     }//GEN-LAST:event_jButtonValiderLivActionPerformed
 
    
     private void jButtonSupprimerLivActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSupprimerLivActionPerformed
 
             //This method can be called only if
             //there's a valid selection
             //so go ahead and remove whatever's selected.
             int index = listeLivraison.getList().getSelectedIndex();
             listeLivraison.getListModel().remove(index);
 
             int size = listeLivraison.getListModel().getSize();
 
             if (size == 0) { //Nobody's left, disable firing.
                 jButtonSupprimerLiv.setEnabled(false);
 
             } else { //Select an index.
                 if (index == listeLivraison.getListModel().getSize()) {
                     //removed item in last position
                     index--;
                 }
 
                 listeLivraison.getList().setSelectedIndex(index);
                 listeLivraison.getList().ensureIndexIsVisible(index);
             }
     }//GEN-LAST:event_jButtonSupprimerLivActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton jButtonFinal;
     private javax.swing.JButton jButtonGenTourn;
     private javax.swing.JButton jButtonRedo;
     private javax.swing.JButton jButtonSupprimerLiv;
     private javax.swing.JButton jButtonUndo;
     private javax.swing.JButton jButtonValiderLiv;
     private javax.swing.JComboBox jComboBoxZone;
     private javax.swing.JFormattedTextField jFormattedTextFieldDate;
     private javax.swing.JLabel jLabelAddLivCurr;
     private javax.swing.JLabel jLabelAddLivPrec;
     private javax.swing.JLabel jLabelAddLivSuiv;
     private javax.swing.JLabel jLabelEdLivTitre;
     private javax.swing.JLabel jLabelLivCurr;
     private javax.swing.JLabel jLabelLivPrec;
     private javax.swing.JLabel jLabelLivSuiv;
     private javax.swing.JLabel jLabelTitreLivraisons;
     private javax.swing.JPanel jPaneLivraisons;
     private javax.swing.JPanel jPanelBoutonsGen;
     private javax.swing.JPanel jPanelDroite;
     private javax.swing.JPanel jPanelEditionLivraison;
     private javax.swing.JPanel jPanelGauche;
     private javax.swing.JPanel jPanelHoraires;
     private Dessin jPanelPlan;
     // End of variables declaration//GEN-END:variables
 
 }
