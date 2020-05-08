 package ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.JProgressBar;
 import javax.swing.JTextPane;
 
 import org.jdesktop.swingworker.SwingWorker;
 
 import data.ModelStages;
 
 import pack.PasserellePDF;
 import pack.PasserellePDF.DocTypes;
 import pack.PasserellePDF.FSS_Modules;
 import pack.Stage;
 
 
 /**
  * fenetre qui permet a l'utilisateur de gnrer les document pour les stages de J et J+1
  * @author BERON Jean-Sbastien
  *
  */
 public class FenetreGenerationPDFNew extends JFrame implements ActionListener,
 					PropertyChangeListener 
 {
 	
 	private static final long serialVersionUID = 7185100043755309312L;
 	
 	//atributs IHM
 	private JPanel contentPane;//conteneur principal
 	//private JPanel headerPane;//conteneur en haut de page
 	private JPanel centerPane;//conteneur au centre de la fenetre
 	private JPanel stagePane;
 	private JPanel bluePane;//conteneur servant de dcor
 	private JPanel pdfPane;//conteneur contenant les formulaires pour la gnration des documents
 	
 	//listes droulantes
 	private JPanel choixPane;
 	private JComboBox dateBox;
 	
 	//composants des formulaires pour la gnration des documents
 	private JScrollPane scrollPane;
 	private JTable tableStages;
 	private JLabel dateLabel;
 	private JButton listStagiaireBtn;
 	private JButton allListStagiaireBtn;
 	private JButton listEmargBtn;
 	private JButton allListEmargBtn;
 	private JButton frepBtn;
 	private JButton surbookBtn;
 	private JButton checkListBtn;
 	private JButton fssBtn;
 	
 	// Models
 	private ModelStages ms;
 	private JPanel allPane;
 	private JLabel allLabel;
 	private JButton allActionsBtn;
 	private JPanel listStagiairePane;
 	private JLabel listStagiaireLabel;
 	private JPanel listEmargementPane;
 	private JLabel listEmargementLabel;
 	private JPanel fssPane;
 	private JLabel fssLabel;
 	private JPanel checkListPane;
 	private JLabel checkListLabel;
 	private JPanel frepPane;
 	private JLabel frepLabel;
 	private JPanel surbookPane;
 	private JLabel surbookLabel;
 	private JPanel barPane;
 	private JProgressBar progressBar;
 	private JTextPane statusPane;
 
 	private StatusBar statusBar;
 
 	private Task task;
 	
 	class Task extends SwingWorker<Void, Void> {
 		
 		JComponent source = null;
         /*
          * Main task. Executed in background thread.
          */
         public Task(JComponent src) {
         	super();
         	source = src;
 		}
 
 		@Override
         public Void doInBackground() {
 			boolean genAll = false;
             int progress = 0;
             int max = 0;
             //Initialize progress property.
             setProgress(0);
             ArrayList<Stage> allStages = ms.getStagesInList();
 			ArrayList<FSS_Modules>FssModules = PasserellePDF.getFSSModules(allStages);
 
 			if (source.equals(allActionsBtn)) {
 				genAll = true;
				max = allStages.size()*3+FssModules.size()+2;
 			}
 			else {
				max=Math.max(allStages.size(),FssModules.size()+1);
 			}
 			//statusBar.setMax(max);
 
 			//si le bouton est le bouton de la gnration d'un liste de stagiaires
     		if(source.equals(listStagiaireBtn)){
     			for (Stage leStage :  ms.getSelectedStages()) {
     				PasserellePDF.creationDoc(leStage, DocTypes.LISTE_STAGIAIRES);
    				//setProgress(100*progress++/max);
     				PasserellePDF.creationDoc(leStage, DocTypes.AFFICHAGE_SALLE);
                		setProgress(100*progress++/max);
     			}
     		}//finsi
     		
     		//si le bouton est le bouton de la gnration d'un liste d'emargement
     		if(source.equals(listEmargBtn)){
     			for (Stage leStage :  ms.getSelectedStages()) {
     				PasserellePDF.creationDoc(leStage, DocTypes.LISTE_EMARGEMENTS);
                		setProgress(100*progress++/max);
     			}
     		}//finsi
     		
     		//si le bouton est le bouton de la gnration de toutes les listes de stagiaires
     		if(source.equals(allListStagiaireBtn) || genAll){
     			for (Stage leStage : allStages) {
     				PasserellePDF.creationDoc(leStage, DocTypes.LISTE_STAGIAIRES);
    				//setProgress(100*progress++/max);
     				PasserellePDF.creationDoc(leStage, DocTypes.AFFICHAGE_SALLE);
              		setProgress(100*progress++/max);
     			}//finpour
     		}//finsi
     		
     		//si le bouton est le bouton de la gnration de toutes les listes d'emargement
     		if(source.equals(allListEmargBtn) || genAll){
     			for (Stage leStage : allStages) {
     				PasserellePDF.creationDoc(leStage, DocTypes.LISTE_EMARGEMENTS);
                		setProgress(100*progress++/max);
     			}//finpour
     		}//finsi
     		
     		//si le bouton est le bouton de la gnration d'une FREP
     		if(source.equals(frepBtn) || genAll){
     			for (Stage leStage : allStages) {
     				PasserellePDF.creationDoc(leStage, DocTypes.FREP);
                		setProgress(100*progress++/max);
     			}
     		}//finsi
     		
     		//si le bouton est le bouton de la gnration d'un surbook
     		if(source.equals(surbookBtn)){
     			for (Stage leStage : ms.getSelectedStages()) {
     				PasserellePDF.creationDoc(leStage, DocTypes.SURBOOK);
                		setProgress(100*progress++/max);
     			}
     		}//finsi
     		
     		//si le bouton est le bouton de la gnration dde la checkList
     		if(source.equals(checkListBtn) || genAll){
         		PasserellePDF.creationDoc(allStages, DocTypes.CHECKLIST);
         		setProgress(100*progress++/max);
     		}//finsi
     		
     		//si le bouton est le bouton pour les feuilles de routes
     		if(source.equals(fssBtn) || genAll){
     			for (FSS_Modules fss : FssModules) {
     				PasserellePDF.creationDoc(fss, DocTypes.FEUILLE_ROUTE_FSS);
         			setProgress(100*progress++/max);
     			}
     		}
     		
     		return null;
         }
  
         /*
          * Executed in event dispatching thread
          */
         @Override
         public void done() {
             Toolkit.getDefaultToolkit().beep();
             setCursor(null); //turn off the wait cursor
             statusBar.update("Termin !", 0);
         }
     }
        
 	/**
 	 * contructeur de la ffenetre
 	 */
 	public FenetreGenerationPDFNew(){
 		
 		//chargement des listes
 		//chargerList();
 		
 		//formation de la fenetre
 		this.setTitle(Messages.getString("FenetreGenerationPDFNew.Titre")); //$NON-NLS-1$
 		this.setSize(700, 700);
 		//this.setResizable(false);
 		this.setLocationRelativeTo(null);
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		
 		//construction du contentPane
 		constructionContentPane();
 		
 		//visibilit de la fenetre
 		this.setContentPane(contentPane);
 		this.setVisible(true);
 		
 	}//fin FenetreGnrationPDF()
 	
 	/**
 	 * procedure qui charge les differents objet temporaires pour les modifications
 	 */
 	/*
 	private void chargerList(){
 		
 		//chargement des stages J et J+1
 		stageList = PasserelleStage.lectureStageObj();
 		dateList = new ArrayList<String>();
 		//remplissage de la liste des dates de stages
 		for (Stage leStage : stageList) {
 			if(! dateList.contains(leStage.getDateStr())){
 				dateList.add(leStage.getDateStr());
 			}//finsi
 		}//finpour
 		
 	}//fin chargerList()
 	*/
 	/**
 	 * procedure qui construit le contentPane
 	 */
 	private void constructionContentPane() {
 		
 		//construction du contentPane
 		contentPane = new JPanel();
 		contentPane.setLayout(new BorderLayout());
 		contentPane.setBackground(Color.WHITE);
 				
 		// Titre
 		JLabel titreLabel = new JLabel();
 		titreLabel.setText(Messages.getString("FenetreGenerationPDFNew.Titre")); //$NON-NLS-1$
 		titreLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		titreLabel.setFont(new Font("arial", 1, 35)); //$NON-NLS-1$
 		contentPane.add(titreLabel, BorderLayout.PAGE_START);
 		
 		//construction du centerPane
 		constructionCenterPane();
 		contentPane.add(centerPane);
 		
 	}//fin constructionContentPane()
 
 	/**
 	 * procedure qui construit le centerPane
 	 */
 	private void constructionCenterPane() {
 		
 		// centerPane
 		centerPane = new JPanel();
 		centerPane.setBackground(Color.WHITE);
 		centerPane.setLayout(new BorderLayout(0, 0));
 		
 		// bluePane
 		bluePane = new JPanel();
 		bluePane.setBackground(Color.BLUE);
 		bluePane.add(new JLabel("               "));
 		centerPane.add(bluePane,BorderLayout.WEST);
 		
 		// stagePane
 		stagePane = new JPanel();
 		stagePane.setLayout(new BorderLayout(0, 0));
 		centerPane.add(stagePane,BorderLayout.CENTER);
 		
 		// choixPane
 		choixPane = new JPanel();
 		stagePane.add(choixPane, BorderLayout.NORTH);
 		choixPane.setBackground(Color.WHITE);
 		choixPane.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
 		
 		// Status
 		constructionStatusPane();
 		
 		// Date
 		dateBox = new JComboBox();
 		dateBox.addActionListener(this);
 		dateLabel = new JLabel("Date :");
 		choixPane.add(dateLabel);
 		choixPane.add(dateBox);
 		
 		tableStages = new JTable();
 		ms = new ModelStages(tableStages,ModelStages.MODE.CODE_ONLY);
 		tableStages.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		scrollPane = new JScrollPane(tableStages);
 		ms.selDate("1/1/1");
 		dateBox.setModel(ms.dateModel);
 		dateBox.setSelectedIndex(ms.dateModel.getSize()-1);
 		scrollPane.setPreferredSize(new Dimension(300,200));
 		tableStages.selectAll();
 		stagePane.add(scrollPane, BorderLayout.WEST);
 		
 		constructionPdfPane();
 		stagePane.add(pdfPane, BorderLayout.CENTER);
 		
 
 		// Slction dfaut
 		//dateBox.setSelectedIndex(dateBox.getItemCount()-1);
 
 	}//fin constructionCenterPane()
 	
 	private void constructionStatusPane() {
 		barPane = new JPanel();
 		barPane.setBackground(Color.WHITE);
 		stagePane.add(barPane, BorderLayout.SOUTH);
 		barPane.setLayout(new BorderLayout(5, 5));
 		
 		progressBar = new JProgressBar();
 		barPane.add(progressBar, BorderLayout.EAST);
 		
 		statusPane = new JTextPane();
 		barPane.add(statusPane, BorderLayout.CENTER);
 		
 		statusBar = new StatusBar(statusPane, progressBar);
 		statusBar.select();
 		statusBar.update(null, 0);
 	}
 	
 	/**
 	 * construction du pdfPane
 	 */
 	private void constructionPdfPane(){
 		
 		pdfPane = new JPanel();
 		pdfPane.setLayout(new GridLayout(7,1,0,0));
 		pdfPane.setBackground(Color.WHITE);
 		afficherPDFPane();
 	}
 	
 	/**
 	 * procedure qui remplit le conteneur pdfpane avec les informations du stage
 	 */
 	@SuppressWarnings("deprecation")
 	public void afficherPDFPane(){
 		
 		//clear du Panel
 		pdfPane.removeAll();
 		
 		//police des composants
 		Font font = new Font("arial", 1, 12); //$NON-NLS-1$
 		
 		//titre : "document pour le JJ/MM/YYYY"
 		/*
 		JLabel titreLabel = new JLabel(Messages.getString("FenetreGenerationPDFNew.Titre2")+date); //$NON-NLS-1$
 		titreLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		titreLabel.setFont(font);
 		pdfPane.add(titreLabel);
 		 */
 		Dimension btDim = new Dimension(270, 25);
 		Dimension lbDim = new Dimension(270, 15);
 		
 		// All actions
 		allPane = new JPanel();
 		allPane.setBackground(Color.WHITE);
 		pdfPane.add(allPane);
 		
 		allLabel = new JLabel(Messages.getString("FenetreGenerationPDFNew.allLabel")); //$NON-NLS-1$
 		allLabel.setPreferredSize(lbDim);
 		allLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		allLabel.setFont(new Font("Arial", Font.BOLD, 12));
 		allPane.add(allLabel);
 		
 		allActionsBtn = new JButton(Messages.getString("FenetreGenerationPDFNew.allActions"));
 		allActionsBtn.addActionListener(this);
 		allActionsBtn.setPreferredSize(btDim);
 		allPane.add(allActionsBtn);
 		
 		//conteneur pour la gnration des listes de stagiaires
 		listStagiairePane = new JPanel();
 		listStagiairePane.setBackground(Color.WHITE);
 		listStagiaireLabel = new JLabel(Messages.getString("FenetreGenerationPDFNew.ListeStg")); //$NON-NLS-1$
 		listStagiaireLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		listStagiaireLabel.setFont(font);
 		listStagiaireLabel.setPreferredSize(lbDim);
 		listStagiairePane.add(listStagiaireLabel);
 		
 		allListStagiaireBtn = new JButton(Messages.getString("FenetreGenerationPDFNew.DoAllStg")); //$NON-NLS-1$
 		allListStagiaireBtn.addActionListener(this);
 		allListStagiaireBtn.setPreferredSize(btDim);
 		listStagiairePane.add(allListStagiaireBtn);
 		
 		listStagiaireBtn = new JButton(Messages.getString("FenetreGenerationPDFNew.DoSelStg")); //$NON-NLS-1$
 		listStagiaireBtn.addActionListener(this);
 		listStagiaireBtn.setPreferredSize(btDim);
 		listStagiairePane.add(listStagiaireBtn);
 		pdfPane.add(listStagiairePane);
 		
 		//conteneur pour la gnration des listes d'Emargement
 		listEmargementPane = new JPanel();
 		listEmargementPane.setBackground(Color.WHITE);
 		
 		listEmargementLabel = new JLabel(Messages.getString("FenetreGenerationPDFNew.ListeEm")); //$NON-NLS-1$
 		listEmargementLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		listEmargementLabel.setFont(font);
 		listEmargementLabel.setPreferredSize(lbDim);
 		listEmargementPane.add(listEmargementLabel);
 		 
 		allListEmargBtn = new JButton(Messages.getString("FenetreGenerationPDFNew.DoAllStg")); //$NON-NLS-1$
 		allListEmargBtn.addActionListener(this);
 		allListEmargBtn.setPreferredSize(btDim);
 		listEmargementPane.add(allListEmargBtn);
 		
 		listEmargBtn = new JButton(Messages.getString("FenetreGenerationPDFNew.DoSelStg")); //$NON-NLS-1$
 		listEmargBtn.addActionListener(this);
 		listEmargBtn.setPreferredSize(btDim);
 		listEmargementPane.add(listEmargBtn);
 		pdfPane.add(listEmargementPane);
 		
 		//conteneur pour la gnration des surbook
 		surbookPane = new JPanel();
 		surbookPane.setBackground(Color.WHITE);
 		
 		surbookLabel = new JLabel(Messages.getString("FenetreGenerationPDFNew.Surbook")); //$NON-NLS-1$
 		surbookLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		surbookLabel.setPreferredSize(lbDim);
 		surbookLabel.setFont(font);
 		surbookPane.add(surbookLabel);
 		
 		surbookBtn = new JButton(Messages.getString("FenetreGenerationPDFNew.DoSurbook")); //$NON-NLS-1$
 		surbookBtn.addActionListener(this);
 		surbookBtn.setPreferredSize(btDim);
 		surbookPane.add(surbookBtn);
 		pdfPane.add(surbookPane);
 		
 		//conteneur pour la gnration des FREP
 		frepPane = new JPanel();
 		frepPane.setBackground(Color.WHITE);
 		frepLabel = new JLabel(Messages.getString("FenetreGenerationPDFNew.FREP")); //$NON-NLS-1$
 		frepLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		frepLabel.setFont(font);
 		frepLabel.setPreferredSize(lbDim);
 		frepPane.add(frepLabel);
 		
 		frepBtn = new JButton(Messages.getString("FenetreGenerationPDFNew.DoAllFREP")); //$NON-NLS-1$
 		frepBtn.addActionListener(this);
 		frepBtn.setPreferredSize(btDim);
 		frepPane.add(frepBtn);
 		pdfPane.add(frepPane);
 		
 		//conteneur pour la gnration de la checkList
 		checkListPane = new JPanel();
 		checkListPane.setBackground(Color.WHITE);
 		checkListLabel = new JLabel(Messages.getString("FenetreGenerationPDFNew.CL_Adm")); //$NON-NLS-1$
 		checkListLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		checkListLabel.setPreferredSize(lbDim);
 		checkListLabel.setFont(font);
 		checkListPane.add(checkListLabel);
 		
 		checkListBtn = new JButton(Messages.getString("FenetreGenerationPDFNew.DoCL")); //$NON-NLS-1$
 		checkListBtn.addActionListener(this);
 		checkListBtn.setPreferredSize(btDim);
 		checkListPane.add(checkListBtn);
 		pdfPane.add(checkListPane);
 		
 		//conteneur pour la gnration des feuilles de routes
 		fssPane = new JPanel();
 		fssPane.setBackground(Color.WHITE);
 		fssLabel = new JLabel(Messages.getString("FenetreGenerationPDFNew.F_Route_FSS")); //$NON-NLS-1$
 		fssLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		fssLabel.setPreferredSize(lbDim);
 		fssLabel.setFont(font);
 		fssPane.add(fssLabel);
 		fssBtn = new JButton(Messages.getString("FenetreGenerationPDFNew.DoAllFSS")); //$NON-NLS-1$
 		fssBtn.addActionListener(this);
 		fssBtn.setPreferredSize(btDim);
 		fssPane.add(fssBtn);
 		pdfPane.add(fssPane);
 		
 		//actualisation de la fenetre
 		this.show();
 		
 	}//fin afficherPDFPane()
 
 	/**
 	 * procedure de l'interface ActionListener<br/>
 	 * execute les modifications par rapport au bouton selectionn
 	 */
 	public void actionPerformed(ActionEvent e) {
 		
 		// Status vide
 		statusBar.setMax(100);
 		statusBar.update(null, 0);
 		
 		//recuperation de la source
 		JComponent source = (JComponent) e.getSource();
 		
 		//si le bouton est le bouton du choix de la date
 		if (source.equals(dateBox)) {
 			ms.selDate((String) dateBox.getSelectedItem());
 		}
 		else {
 			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 			task = new Task(source);
 			task.addPropertyChangeListener(this);
 			task.execute();
 		}
 	}//fin actionPerformed()
 
 	public void propertyChange(PropertyChangeEvent evt) {
 	    if ("progress" == evt.getPropertyName()) {
             int progress = (Integer) evt.getNewValue();
             statusBar.update(progress);
         }
 	}
 	
 }//fin class
