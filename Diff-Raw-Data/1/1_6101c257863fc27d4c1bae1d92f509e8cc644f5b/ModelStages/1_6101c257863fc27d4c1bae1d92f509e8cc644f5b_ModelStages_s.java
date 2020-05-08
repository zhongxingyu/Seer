 package data;
 
 import java.awt.Component;
 import java.awt.Font;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 import javax.swing.plaf.FontUIResource;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumnModel;
 import javax.swing.DefaultCellEditor;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.ImageIcon;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JTable;
 import javax.swing.ListCellRenderer;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 
 import pack.Config;
 import pack.Module;
 import pack.PasserelleStage;
 import pack.Stage;
 import ui.TableModelSorter;
 
 public class ModelStages extends AbstractTableModel
 {
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = -1298050647157966932L;
 	
 	private ArrayList<Stage> stagesAll = new ArrayList<Stage>();
 	private ArrayList<Stage> stages = new ArrayList<Stage>();
 	public ModelCombo dateModel = new ModelCombo();	
 	public ModelCombo cieModel = new ModelCombo();
 	public ModelCombo stageModel = new ModelCombo();
 	
 	private TableModelSorter sorter;
 	private JTable table;
 	public String filterDate;
 	
 	public enum MODE { ALL_COLS, CODE_ONLY };
 	private MODE mode = MODE.ALL_COLS;
 	
 	private boolean mod = false;
 	
     private static final String[] entetes = {"Cie", "Code" , "Libell", "Leader", "Salle", "Heure"};
     
 	private static final int[][] colWidths = { 
 			{ 50, 80, 65},
 			{ 60,250,100},
 			{ 80,500,150},
 			{ 60,180, 90},
 			{ 60,180, 90},
 			{ 60, 80, 60}
 			};
 
 	private static final boolean logoCie = Config.getB("data.logo.disp");
 
     public ModelStages(JTable table, MODE ... param) {
         super();
 		//chargement des stages J et J+1
 		stagesAll = PasserelleStage.lectureStageObj();
 		//initialisation de dateList
 		//dates = new ArrayList<String>();
 		for (Stage s : stagesAll) {
 			dateModel.add(s.getDateStr());
 		}
 		
 		if (param.length == 1)
 			this.mode = param[0];
     	this.table = table;
 		sorter = new TableModelSorter(this);
 		table.setModel(sorter);
 		TableColumnModel tcm = table.getColumnModel();
 		if (mode == MODE.ALL_COLS) {
 			for (int i =0; i<tcm.getColumnCount(); i++) {
 				tcm.getColumn(i).setMinWidth(colWidths[i][0]);
 				tcm.getColumn(i).setMaxWidth(colWidths[i][1]);
 				tcm.getColumn(i).setPreferredWidth(colWidths[i][2]);
 			}
 		}
 		sorter.setTableHeader(table.getTableHeader());
 		
 		table.setRowHeight(18);
 		FontUIResource uiFont = (FontUIResource) UIManager.get("Table.font");
 		Font font = new Font(uiFont.getName(), uiFont.getStyle(), uiFont.getSize()+2);
 		table.setFont(font);
 		
 		// Renderer
 		/*
 		table.getColumnModel().getColumn(0).setCellRenderer(new CieCellRenderer());
 		table.getColumnModel().getColumn(0).setCellEditor(new CieCellEditor(cieModel));
 		*/
 		if (logoCie) {
 			cieModel.add("AFR");
 			cieModel.add("EST");
 			cieModel.add("CRL");
 			cieModel.add("MDG");
 			cieModel.add("XLF");
 			cieModel.add("CCM");
 			cieModel.add("FWI");
 		
 
 			CieCellRenderer renderer = new CieCellRenderer();
 			JComboBox jcb = new JComboBox(cieModel);
 			//jcb.setEditable(true);
 			jcb.setRenderer(renderer);
 			table.getColumnModel().getColumn(0).setCellRenderer(renderer);
 			table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(jcb));
 		}
     }
             
     public void saveStages() {
 		PasserelleStage.ecritureStageObj(stagesAll);	//ecriture des stages avec les modifs
     }
     
     /*
     public TableModelSorter getSorter() {
     	return sorter;
     }
     */
     
     public int getRowCount() {
 		//TypedQuery<Long> q = DB.em.createQuery("SELECT COUNT(s) FROM Stage s", Long.class);
 	    //return q.getSingleResult().intValue();
     	return stages.size();
     }
 
     public int getColumnCount() {
     	switch (mode) {
 	    	case ALL_COLS:
 	    		return entetes.length;
 	    	case CODE_ONLY:
 	    	default:
 	    		return 1;
     	}
     }
 
     public String getColumnName(int columnIndex) {
     	int i=0;
     	switch (mode) {
 	    	case ALL_COLS:
 	    		i=columnIndex;
 	    		break;
 	    	case CODE_ONLY:
 	    	default:
 	    		i=1;
     	}
 		return entetes[i];
     }
         
     public Object getValueAt(int rowIndex, int columnIndex) {
     	int i=0;
     	switch (mode) {
 	    	case ALL_COLS:
 	    		i=columnIndex;
 	    		break;
 	    	case CODE_ONLY:
 	    	default:
 	    		i=1;
     	}
 		switch(i){
     	case 0:
         	return stages.get(rowIndex).getCompagnie();
     	case 1:
         	return stages.get(rowIndex).getCode();
     	case 2:
         	return stages.get(rowIndex).getLibelle();
     	case 3:
         	return stages.get(rowIndex).getLeader();
     	case 4:
         	return stages.get(rowIndex).getFirstModule().getSalle();
     	case 5:
         	return stages.get(rowIndex).getFirstModule().getHeureDebut();
     	default:
     		System.out.println("[ERR] ModelStages.getValueAt("+columnIndex+")");
     		return null;
        }
     }
     
     @Override
     public boolean isCellEditable(int rowIndex, int columnIndex) {
     	boolean write = false;
     	if (mode == MODE.CODE_ONLY) {
     		write = false;
     	}
     	else {
 	    	switch (columnIndex) {
 	    		case 0:
 	    		case 1:
 		    	case 2:
 		    	case 3:
 		    	case 4:
 		    	case 5:
 		    		write=true;
 	    	}
     	}
     	return write;
     }
 
     @Override
     public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
         if(aValue != null){
         	int i=0;
         	switch (mode) {
     	    	case ALL_COLS:
     	    		i=columnIndex;
     	    		break;
     	    	case CODE_ONLY:
     	    	default:
     	    		i=1;
         	}
 
             Stage stage = stages.get(rowIndex);
             
             switch(i){
                 case 0:
                 	stage.setCompagnie((String)aValue);
                     break;
                 case 1:
                 	stage.setCode((String)aValue);
                     break;
                 case 2:
                 	stage.setLibelle((String)aValue);
                     break;
                 case 3:
 					stage.setLeader((String)aValue);
                    break;
                 case 4:
                 	stage.getFirstModule().setSalle((String)aValue);
                 	break;
                 case 5:
                 	stage.getFirstModule().setHeureDebut(((String)aValue).replace(" ", ""));
                 	break;
                 default:
                     System.out.println("[ERR] ModelStages.setValueAt("+columnIndex+")");
             }
             setMod(true);
             System.out.println("Set Value " + columnIndex + " of " + rowIndex);
         }
     }
 
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
     public Class getColumnClass(int columnIndex){
         switch(columnIndex){
             default:
                 return Object.class;
         }
     }
     
     public Stage getStage(String c) {
 		return null;
     }
     
     public void selDate(String date) {
     	filterDate = date;
 		stages = new ArrayList<Stage>();
 		stageModel = new ModelCombo();
 		for (Stage s : stagesAll) {
     		if(s.getDateStr().equals(filterDate)){
     			stages.add(s);
     		}
     	}
 		Collections.sort(stages);
 		for (Stage s : stages) {
 			stageModel.add(s);
 		}
 		/*
 		//tri des stages par ordre alphabetique
 		Stage stgTmp ;
 		boolean good = false;
 		//tant que le tri n'est pas bon
 		while (! good) {
 			good = true;
 			for (int i = 0; i < stages.size()-1; i++) {
 				if(stages.get(i).getCode().compareToIgnoreCase(stages.get(i+1).getCode()) > 0){
 					good = false;
 					//echange
 					stgTmp = stages.get(i);
 					stages.set(i, stages.get(i+1));
 					stages.set(i+1, stgTmp);
 				}
 			}
 		}
 		*/
 		fireTableDataChanged();
 		//fireTableRowsInserted(stages.size() -1, stages.size() -1);
     }
     
     @SuppressWarnings("unchecked")
 	public ArrayList<Stage> getStagesInList() {
     	return (ArrayList<Stage>) stages.clone();
     }
 
         
     public Stage getSelectedStage() {
     	int idx;
     	idx = table.getSelectedRow();
     	if (idx >= 0) {
     		return stages.get(sorter.modelIndex(idx));
     	}
     	else {
     		return null;
     	}
     }
     
     public void removeStage(Stage s) {
     	stagesAll.remove(s);
     	stages.remove(s);
 		setMod(true);
 		fireTableDataChanged();
     }
 
     public void removeSelectedStage() {
     	removeStage(getSelectedStage());
 		//fireTableDataChanged();
     }
     
     public ArrayList<Stage> getSelectedStages() {
     	ArrayList<Stage> sl = new ArrayList<Stage>();
     	int[] idx = table.getSelectedRows();
     	for (int i : idx) {
     		if (i >= 0) {
     			sl.add(stages.get(sorter.modelIndex(i)));
     		}
     	}
     	return sl;
     }
 
     public void removeStages(ArrayList<Stage> sl) {
     	stagesAll.removeAll(sl);
     	stages.removeAll(sl);
 		setMod(true);
 		fireTableDataChanged();
     }
 
     public void removeSelectedStages() {
     	removeStages(getSelectedStages());
 		//fireTableDataChanged();
     }
 
 	public void newStage() {
 		Module m = new Module((long) 0, "", "", filterDate, "08:00", "10:00");
 		Stage s = new Stage(m);
 		s.setCompagnie("AFR");
 		stagesAll.add(s);
 		stages.add(s);
         setMod(true);
 		fireTableDataChanged();
 	}
 
 	public boolean isMod() {
 		return mod;
 	}
 
 	public void setMod(boolean mod) {
 		this.mod = mod;
 	}
 }
 
 
 class CieCellRenderer implements TableCellRenderer, ListCellRenderer {
 	private static final String logoPath = Config.get("data.logos");
 
 	DefaultTableCellRenderer tableR = new DefaultTableCellRenderer();
 	DefaultListCellRenderer listR = new DefaultListCellRenderer();
 
 	//private static final long serialVersionUID = -5550185168514788951L;
 	private static HashMap<String,ImageIcon> logoIcons = new HashMap<String,ImageIcon>(); 
 
 	/*
     public CieCellRenderer() {
         super();
     }
     */
 
     private void configureRenderer(JLabel renderer, Object value) {
         if ((value != null) && (value instanceof String)) {
             String cie = (String) value;
 			if (! logoIcons.containsKey(cie)) {
 				logoIcons.put(cie, new ImageIcon(logoPath + value + ".jpg"));
 			}
 			renderer.setText(value.toString());
 			renderer.setAlignmentX(SwingConstants.CENTER);
 			renderer.setIcon(logoIcons.get(cie));
         }
         else {
 			renderer.setText("???");
         }
     }
     
     //@Override
     public Component getTableCellRendererComponent(JTable table, Object value,
     		boolean isSelected, boolean hasFocus, int row, int column) {
     	tableR = (DefaultTableCellRenderer) tableR.getTableCellRendererComponent(table, 
     			value, isSelected, hasFocus, row, column);
     	configureRenderer(tableR, value);
     	return tableR;
     }
 
 	public Component getListCellRendererComponent(JList list, Object value,
 			int index, boolean isSelected, boolean cellHasFocus) {
 		listR = (DefaultListCellRenderer) listR.getListCellRendererComponent(list, 
 				value, index, isSelected, cellHasFocus);
     	configureRenderer(listR, value);
 		return listR;
 	}
 }
 /*
 class CieCellEditor extends DefaultCellEditor  {
 	private static final long serialVersionUID = -6147400529158086950L;
 
 	public CieCellEditor(JComboBox jcb) {
 		//JComboBox jcb = new JComboBox(mc);
 		//jcb.setRenderer(new CieCellRenderer());
         //super(new JComboBox(mc));
 		lab = new JLabel();
     }
 }
 */
