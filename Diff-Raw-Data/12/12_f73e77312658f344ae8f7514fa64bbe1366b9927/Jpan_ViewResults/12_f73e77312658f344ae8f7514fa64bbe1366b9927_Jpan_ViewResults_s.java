 package ContextForest;
 
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.LinkedList;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 
 import moduls.frm.ContextLeaf;
 import moduls.frm.PostSearchAnalyses;
 import moduls.frm.QueryData;
 
 public class Jpan_ViewResults extends JPanel implements ActionListener{
 
 	//Data
 	private FrmScanOutputWindow fsow;
 	//private LinkedList<QueryData> CurrentlySelectedQueries;
 	
 	//GUI components
 	private JButton btnSelectQR, btnDrawCT;
 	private JTextField selectQueryResults;
 	private String strDrawCT = "Draw Context Trees";
 	private String strSelectQR = "Select Query Results";
 	
 	//Fonts
 	private Font fontStandard = new Font("Dialog", Font.BOLD, 10);
 	private Font fontSearch = new Font("Dialog", Font.PLAIN, 14);
 	
 	
 	//Constructor
 	public Jpan_ViewResults(FrmScanOutputWindow fsow){
 		
 		//Data
 		this.fsow = fsow;
 		
 		//Create panel
 		getPanel();
 		
 	}
 	
 	//create panel
 	public void getPanel(){
 		
 		//initialize panel
 		this.setLayout(new GridBagLayout());
 		this.setBorder(BorderFactory.createTitledBorder("Select Query Results"));
 		final GridBagConstraints c = new GridBagConstraints();
 		
 		//initial GridBagLayout parameters
 		c.anchor = GridBagConstraints.FIRST_LINE_START;
 		c.weightx = 1;
 		
 		//search for nodes bar
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 3;
 		c.gridheight = 1;
 		c.ipady = 7;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(1,1,1,1);
 		selectQueryResults = new JTextField("");
 		selectQueryResults.setFont(fontSearch);
 		selectQueryResults.setPreferredSize(new Dimension(400, 26));
 		selectQueryResults.addActionListener(this);
 		add(selectQueryResults, c);
 		
 		//search for nodes button
 		c.ipady = 5;
 		c.gridx = 3;
 		c.gridy = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(1,1,1,1);
 		btnSelectQR = new JButton(strSelectQR);
 		btnSelectQR.setFont(fontStandard);
 		btnSelectQR.addActionListener(this);
 		add(btnSelectQR, c);
 		
 		//Select All
 		c.gridx = 4;
 		c.gridy = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(1, 1, 1, 1);
 		btnDrawCT = new JButton(strDrawCT);
 		btnDrawCT.addActionListener(this);
 		btnDrawCT.setFont(fontStandard);
 		add(btnDrawCT, c);
 		
 	}
 
 	// ======== Actions ======== //
 	
 	//main actions
 	@Override
 	public void actionPerformed(ActionEvent e) {
 
 		//Select Query Results
 		if (e.getSource().equals(btnSelectQR) || e.getSource().equals(selectQueryResults)){
 
 			if (fsow.getPan_ScanResults() != null){
 				//Scan results
 				SelectRowsInTable();
 			} else {
 				//Context forest
 				SelectLeaves();
 				
 			}
 		}
 		
 		//Draw Context Trees
 		if (e.getSource().equals(btnDrawCT)){
 			if (fsow.getPan_ScanResults() != null){
 				//Scan results
 				DrawContextTrees(RetrieveSelectedQueryResultsFromTable());
 			} else {
 				//Context forest
 				DrawContextTrees(RetrieveSelectedQueryResultsFromTree());
 			}
 
 		}
 	}
 	
 	//retrieve selected query sets from table
 	public LinkedList<QueryData> RetrieveSelectedQueryResultsFromTable(){
 		
 		//Initialize output
 		LinkedList<QueryData> SelectedQS = new LinkedList<QueryData>();
 		LinkedList<String> SelectedQSNames = new LinkedList<String>();
 		
 		//Retrieve names from table
 		int[] SelectedRows = fsow.getPan_ScanResults().getTable().getSelectedRows();
 		for (int i = 0; i < SelectedRows.length; i++){
 			String query 
 				= (String) fsow.getPan_ScanResults().getTable().getValueAt(SelectedRows[i], 0);
 			SelectedQSNames.add(query);
 		}
 		
 		//Retrieve queries by name from QuerySet object
 		for (QueryData QD : fsow.getQS().getContextTrees()){
 			if (SelectedQSNames.contains(QD.getName())){
 				SelectedQS.add(QD);
 			}
 		}
 				
 		return SelectedQS;
 	}
 	
 	//retrieve selected query sets from tree
 	public LinkedList<QueryData> RetrieveSelectedQueryResultsFromTree(){
 		
 		//Initialize output
 		LinkedList<QueryData> SelectedQS = new LinkedList<QueryData>();
 		LinkedList<String> SelectedQSNames = new LinkedList<String>();
 		
 		//retrieve appropriate QS
 		for (ContextLeaf CL : fsow.getfPiz().getCSD().getGraphicalContexts())
 			if (CL.isSelected()){
 				SelectedQSNames.add(CL.getContextForestOriginalName());
 			}
 		
 		//Retrieve queries by name from QuerySet object
 		for (QueryData QD : fsow.getQS().getContextTrees()){
 			if (SelectedQSNames.contains(QD.getName())){
 				SelectedQS.add(QD);
 			}
 		}
 				
 		return SelectedQS;
 	}
 	
 	//select rows in a table
 	public void SelectRowsInTable(){
 		//recover query, split by semicolon, comma, or white space
 		String Query = selectQueryResults.getText();
 		String[] Queries = Query.split(";");
 		if (Queries.length == 1){
 			Queries = Query.split(",");
 		}
 		if (Queries.length == 1) {
 			Queries = Query.split("\\s+");
 		}
 		
 		//Determine queries to select
 		LinkedList<String> SelectedQS = new LinkedList<String>();
 		for (String s : Queries){
 			for (QueryData QD : fsow.getQS().getContextTrees()){
 				if (QD.getName().contains(s)){
 					SelectedQS.add(QD.getName());
 				}
 			}
 		}
 		
 		//Deselect all
 		fsow.getPan_ScanResults().getTable().clearSelection();
 		
 		//Select queries
 		for (int i = 0; i < fsow.getPan_ScanResults().getTable().getRowCount(); i++){
 			if (SelectedQS.contains(fsow.getPan_ScanResults().getTable().getValueAt(i,0))){
 				fsow.getPan_ScanResults().getTable().addRowSelectionInterval(i,i);
 			}
 		}
 	}
 	
 	//select leaves on tree
 	public void SelectLeaves(){
 		
 		//recover query, split by semicolon, comma, or white space
 		String Query = selectQueryResults.getText();
 		String[] Queries = Query.split(";");
 		if (Queries.length == 1){
 			Queries = Query.split(",");
 		}
 		if (Queries.length == 1) {
 			Queries = Query.split("\\s+");
 		}
 		
 		//Determine queries to select
 		LinkedList<String> SelectedQS = new LinkedList<String>();
 		for (String s : Queries){
 			for (ContextLeaf CL : fsow.getfPiz().getCSD().getGraphicalContexts()){
 				if (CL.getName().contains(s)){
 					CL.setSelected(true);
 				} else {
 					CL.setSelected(false);
 				}
 			}
 		}
 		
 		//repaint
 		fsow.getfPiz().repaint();
 		
 	}
 	
 	//draw a bunch of context trees from selected queries
 	public void DrawContextTrees(LinkedList<QueryData> L){
 		
 		//switch cursor
 		Component glassPane = getRootPane().getGlassPane();
 		glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 		glassPane.setVisible(true);
 		
 		//Turn view context trees to on
 		fsow.getF().getPanMenuTab().getJpo().getDrawContextTree().setSelected(true);
 		fsow.getF().getPanDisplayOptions().getDrawContextTree().setSelected(true);
 		
 		for (QueryData QD : L){
 			
 			//Adjust search analyses
 			PostSearchAnalyses P = new PostSearchAnalyses(
 					fsow.getF().getPanMenuTab().getJpo().getDrawSearchResults().isSelected(), //search results
 					true, //draw context tree
 					fsow.getF().getPanMenuTab().getJpo().getDrawContextGraph().isSelected(), //draw context graph
 					fsow.getF().getPanMenuTab().getJpo().getDrawPhylogeneticTree().isSelected() //phylogeny
 					);
 			
 			//update query list
 			QD.setAnalysesList(P);
 			
 			//Update aspects of drawing frame
			fsow.getF().getPanBtn().setSearchResultsFrame(QD.getSRF());
 			fsow.getF().getPanBtn().setMultiDendro(QD.getMultiDendro());
 			fsow.getF().getPanBtn().setDe(QD.getDe());
 			
 			//draw trees
 			fsow.getF().getPanBtn().showCalls("Load", QD);
 		}
 		
 		//switch cursor to normal
 		glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 		glassPane.setVisible(false);
 	}
 }
