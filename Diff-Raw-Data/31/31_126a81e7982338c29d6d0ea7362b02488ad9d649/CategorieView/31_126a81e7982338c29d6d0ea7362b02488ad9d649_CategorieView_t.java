 package heig.igl3.roc2.GUI;
 
 import heig.igl3.roc2.Business.Budget;
 import heig.igl3.roc2.Business.Categorie;
 import heig.igl3.roc2.Business.SousCategorie;
 import heig.igl3.roc2.Data.Roc2DB;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 @SuppressWarnings("serial")
 public class CategorieView extends JPanel implements ActionListener, ListSelectionListener{
 	
 	DefaultListModel<Categorie> catModel;
 	DefaultListModel<SousCategorie> sousCatModel;
 	JList<Categorie> catList;
 	JList<SousCategorie> sousCatList;
 	JScrollPane catScroll, sousCatScroll;
 	JButton btAddCat, btEditCat, btDelCat, btAddSousCat, btEditSousCat, btDelSousCat;
 	Categorie selectedCat;
 	SousCategorie selectedSousCat;
 	int selectedCatIndex, selectedSousCatIndex;
 	Budget budget;
 	
 	public CategorieView(Budget budget) {
 		this.budget = budget;
 		this.setLayout(new BorderLayout(0,0));
 		
 		selectedCat = null;
 		selectedSousCat = null;
 		
 		catModel = new DefaultListModel<Categorie>();
 		catList = new JList<Categorie>(catModel);
 		catList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		catList.setName("categories");
 		catList.addListSelectionListener(this);
 		catScroll = new JScrollPane(catList);
 		
 		sousCatModel = new DefaultListModel<SousCategorie>();
 		sousCatList = new JList<SousCategorie>(sousCatModel);
 		sousCatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		sousCatList.setName("sousCategories");
 		sousCatList.addListSelectionListener(this);
 		sousCatScroll = new JScrollPane(sousCatList);
 		
 		for(Categorie categorie : budget.categories) {
 			this.catModel.addElement(categorie);
 		}
 
 		// Structure
 		JPanel splitPanel = new JPanel(new GridLayout(1,2));
 		JPanel catPanel = new JPanel(new BorderLayout(0,0));
 		JPanel sousCatPanel = new JPanel(new BorderLayout(0,0));
 		JPanel catButtons = new JPanel();
 		JPanel sousCatButtons = new JPanel();
 		
 		// Buttons
 		btAddCat = new JButton("Ajouter");
 		btAddCat.setName("addCat");
 		btAddCat.addActionListener(this);
 		
 		btEditCat = new JButton("Editer");
 		btEditCat.setName("editCat");
 		btEditCat.addActionListener(this);
 		btEditCat.setEnabled(false);
 		
 		btDelCat = new JButton("Supprimer");
 		btDelCat.setName("delCat");
 		btDelCat.addActionListener(this);
 		btDelCat.setEnabled(false);
 		
 		btAddSousCat = new JButton("Ajouter");
 		btAddSousCat.setName("addSousCat");
 		btAddSousCat.addActionListener(this);
 		
 		btEditSousCat = new JButton("Editer");
 		btEditSousCat.setName("editSousCat");
 		btEditSousCat.addActionListener(this);
 		btEditSousCat.setEnabled(false);
 		
 		btDelSousCat = new JButton("Supprimer");
 		btDelSousCat.setName("delSousCat");
 		btDelSousCat.addActionListener(this);
 		btDelSousCat.setEnabled(false);
 		
 		splitPanel.add(catPanel);
 		splitPanel.add(sousCatPanel);
 		
 		catPanel.add(catScroll, BorderLayout.CENTER);
 		catPanel.add(catButtons, BorderLayout.SOUTH);
 		
 		sousCatPanel.add(sousCatScroll, BorderLayout.CENTER);
 		sousCatPanel.add(sousCatButtons, BorderLayout.SOUTH);
 		
 		catButtons.add(btAddCat);
 		catButtons.add(btEditCat);
 		catButtons.add(btDelCat);
 		
 		sousCatButtons.add(btAddSousCat);
 		sousCatButtons.add(btEditSousCat);
 		sousCatButtons.add(btDelSousCat);
 		
 		add(splitPanel);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		JButton source = (JButton) e.getSource();
 		String actionCommand = source.getName();
 		if (!catList.isSelectionEmpty()) {
 			selectedCat = catModel.get(catList.getSelectedIndex());
 		}
 		if (!sousCatList.isSelectionEmpty()) {
 			selectedSousCat = sousCatModel.get(sousCatList.getSelectedIndex());
 		}
 		CategoryEditor frame;
 		SousCategoryEditor frameSC;
 		switch(actionCommand) {
 		case "addCat":
 			frame = new CategoryEditor(null, true, budget);
             frame.setSize(300,80);
             frame.setLocationRelativeTo(null);
             frame.setUndecorated(false);
             frame.setVisible(true);
             frame.dispose();
             if(frame.categorie != null){
             	catModel.addElement(frame.categorie);
                 budget.categories.add(frame.categorie);
             }
 			break;
 		case "editCat":
 			frame = new CategoryEditor(null, true, budget, selectedCat);
             frame.setSize(300,80);
             frame.setLocationRelativeTo(null);
             frame.setUndecorated(false);
             frame.setVisible(true);
             frame.dispose();
             if(frame.categorie != selectedCat) {
                 catModel.removeElement(selectedCat);
                 catModel.addElement(frame.categorie);
                 budget.categories.remove(selectedCat);
                 budget.categories.add(frame.categorie);
                 catList.setModel(catModel);
                 catList.ensureIndexIsVisible(selectedCatIndex);
             }
             
             
             
 			break;
 		case "delCat":
 			
 			//	Une fois l'item supprimé, il y a valueChanged
 			//	Création d'un runnable qui sera appelé par invokeLater quand la fenêtre sera au repos 
 			if(JOptionPane.showConfirmDialog(this, "Voulez vous supprimer définitivement cet élément ?", "ROC2",
 				    JOptionPane.YES_NO_OPTION) == 0){	
 				Runnable run = new Runnable(){
 					public void run(){
 						if(Roc2DB.delCategorie(selectedCat.id)){
 							budget.categories.remove(selectedCat);
 							catModel.removeElement(selectedCat);
 						}
 					}
 				};
 				SwingUtilities.invokeLater(run);
 			}
 			break;
 		case "addSousCat":
 			frameSC = new SousCategoryEditor(null, true, selectedCat);
 			frameSC.setSize(300,80);
 			frameSC.setLocationRelativeTo(null);
 			frameSC.setUndecorated(false);
 			frameSC.setVisible(true);
 			frameSC.dispose();
 
             if(frameSC.sousCategorie != null){
             	sousCatModel.addElement(frameSC.sousCategorie);
             	sousCatList.setModel(sousCatModel);
             	for (Categorie cat : budget.categories){
             		if (cat.id == frameSC.sousCategorie.idCategorie){
             			cat.addSousCategorie(frameSC.sousCategorie);;
             		}
             	}
             }
 			break;
 		case "editSousCat":
 			frameSC = new SousCategoryEditor(null, true, selectedCat);
 			frameSC.setSize(300,80);
 			frameSC.setLocationRelativeTo(null);
 			frameSC.setUndecorated(false);
 			frameSC.setVisible(true);
 			frameSC.dispose();
             if(frameSC.sousCategorie != selectedSousCat && frameSC.sousCategorie != null ) {
                 sousCatModel.removeElement(selectedSousCat);
                 sousCatModel.addElement(frameSC.sousCategorie);                
                 for (Categorie cat : budget.categories){
             		if (cat.id == selectedSousCat.idCategorie){
             			cat.sousCategories.remove(selectedSousCat);
             			cat.addSousCategorie(frameSC.sousCategorie);;
             		}
             	}
                 sousCatList.setModel(sousCatModel);
                 sousCatList.ensureIndexIsVisible(selectedSousCatIndex);
             }
 			break;
 		case "delSousCat":
 			//	Une fois l'item supprimé, il y a valueChanged
 			//	Création d'un runnable qui sera appelé par invokeLater quand la fenêtre sera au repos 
 			if(JOptionPane.showConfirmDialog(this, "Voulez vous supprimer définitivement cet élément ?", "ROC2",
 				    JOptionPane.YES_NO_OPTION) == 0){			
 				Runnable run2 = new Runnable(){
 					public void run(){
 						if(Roc2DB.delSousCategorie(selectedSousCat.id)){
 							for (Categorie cat : budget.categories){
 			            		if (cat.id == selectedSousCat.idCategorie){
 			            			cat.sousCategories.remove(selectedSousCat);
 			            		}
 			            	}
 							sousCatModel.removeElement(selectedSousCat);
 						}
 					}
 				};
 				SwingUtilities.invokeLater(run2);
				
 			}
 				
 			break;
 		}
 		this.repaint();
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public void valueChanged(ListSelectionEvent e) {
 		final JList source = (JList) e.getSource();
 		switch(source.getName()){
 		case "categories":
 			
 			
 			selectedCatIndex = source.getSelectedIndex();
 			if (selectedCatIndex > -1){
 				Categorie newSelectedCat = catModel.get(selectedCatIndex);
 				if(selectedCat == null || selectedCat != newSelectedCat) {
 					selectedCat = newSelectedCat;
 					btEditCat.setEnabled(true);
 					btDelCat.setEnabled(true);
 					sousCatModel.removeAllElements();
 					if(!selectedCat.sousCategories.isEmpty()){
 						for(SousCategorie sousCat : selectedCat.sousCategories)
 							sousCatModel.addElement(sousCat);
 					}
 				}
 			}
 			break;
 		case "sousCategories":
 			selectedSousCatIndex = source.getSelectedIndex();
 			if(selectedSousCatIndex > -1){
 				SousCategorie newSelectedSousCat = sousCatModel.get(selectedSousCatIndex);
 				
 				if(selectedSousCat == null || selectedSousCat != newSelectedSousCat) {
 					selectedSousCat = newSelectedSousCat;
 					btEditSousCat.setEnabled(true);
 					btDelSousCat.setEnabled(true);
 				}
 			}
 			break;
 		}
 	}
 }
