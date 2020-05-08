 package de.uni_leipzig.imise.visualization.view;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 
 import org.apache.log4j.Logger;
 
 import de.uni_leipzig.imise.data.CRFVersion;
 import de.uni_leipzig.imise.data.DiffVersion;
 import de.uni_leipzig.imise.data.Item;
 import de.uni_leipzig.imise.data.VersionPair;
 import de.uni_leipzig.imise.data.constants.CategoryConstants;
 import de.uni_leipzig.imise.data.managment.DiffVersionManager;
 import de.uni_leipzig.imise.data.managment.VersionManager;
 import de.uni_leipzig.imise.visualization.controller.DiffTreeController;
 import de.uni_leipzig.imise.visualization.controller.DiffVersionController;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 
 public final class DiffPanel extends JPanel implements PropertyChangeListener{
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static final Logger log = Logger.getLogger(DiffPanel.class);
 	
 	
 	private VersionManager vm ;
 	private DiffVersionManager dvm;
 	private DiffVersionController dvc;
 	private DiffTreeController dtc;
 	private VersionTree diffTree;
 	private JPanel diffTreePanel;
 	private JScrollPane view;
 	private JSplitPane splitPane;
 	private JPanel deletedPanel;
 	private CRFTableModel deletedItemModel;
 	private JTable deletedTable;
 	private JMenu mnNewMenu;
 	private List <JCheckBoxMenuItem> catItems;
 
 	private JMenuBar menuBar;
 	private JComboBox<Integer> fromBox;
 	private JComboBox<Integer> toBox;
 	private JButton btnShow;
 	private DefaultComboBoxModel<Integer> fromModel;
 	private DefaultComboBoxModel<Integer> toModel;
 	private JLabel lblFrom;
 	private JLabel lblTo;
 	public DiffPanel(DiffTreeController dtc) {
 		super();
 		this.vm = VersionManager.getInstance();
 		vm.addPropertyChangeListener(this);
 		this.dvm = DiffVersionManager.getInstance();
 		this.dvc = new DiffVersionController(vm,dvm,this);
 		this.dtc = dtc;
 		this.catItems =new ArrayList<JCheckBoxMenuItem>();
 		this.initGui();
 	}
 	
 	public void initGui(){
 		
 		this.setLayout(new BorderLayout(0, 0));
 		JPanel calcPanel = new JPanel();
 		calcPanel.setLayout(new FlowLayout(FlowLayout.LEFT,2,2));
 		
 		JButton btnDiffCalc = new JButton("show changes");
 		btnDiffCalc.setActionCommand(EventConstants.DIFF_CALC);
 		btnDiffCalc.addActionListener(dvc);
 		calcPanel.add(btnDiffCalc);
 		this.add(calcPanel, BorderLayout.NORTH);
 		
 		fromBox = new JComboBox<Integer>();
 		fromModel = new DefaultComboBoxModel<Integer>();
 		
 		lblFrom = new JLabel("from");
 		calcPanel.add(lblFrom);
 		fromBox.setModel(fromModel);
 		calcPanel.add(fromBox);
 		
 		toBox = new JComboBox<Integer>();
 		toModel = new DefaultComboBoxModel <Integer>();
 		
 		lblTo = new JLabel("to");
 		calcPanel.add(lblTo);
 		toBox.setModel(toModel);
 		calcPanel.add(toBox);
 		
 		btnShow = new JButton("show");
 		btnShow.setActionCommand(EventConstants.SHOW);
 		btnShow.addActionListener(dvc);
 		calcPanel.add(btnShow);
 		
 		menuBar = new JMenuBar();
 		calcPanel.add(menuBar);
 		
 		mnNewMenu = new JMenu("category selection");
 		menuBar.add(mnNewMenu);
 		this.initCategoryItems();
 		
 		TitledBorder tb =new TitledBorder("diff-tree");
 		
 		splitPane = new JSplitPane();
 		splitPane.setResizeWeight(0.8);
 		
 		diffTreePanel = new JPanel(new BorderLayout());
 		diffTreePanel.setBorder(tb);
 		diffTreePanel.setPreferredSize(new Dimension(600,400));
 		
 		diffTree = new VersionTree(dtc);
 		diffTree.getSelectionModel().addTreeSelectionListener(dtc);
 		view = new JScrollPane();
 		diffTreePanel.add(view,BorderLayout.CENTER);
 		view.setViewportView(diffTree);
 		splitPane.setLeftComponent(diffTreePanel);
 		TitledBorder tb2 = new TitledBorder("deleted items");
 		deletedPanel = new JPanel (new BorderLayout());
 		deletedPanel.setBorder(tb2);
 		deletedItemModel =new CRFTableModel(CellConstants.VERSION_COL,CellConstants.ITEM_COL);
 		deletedTable = new JTable(deletedItemModel);
 		deletedTable.getSelectionModel().addListSelectionListener(dvc);
 		deletedTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		JScrollPane pane =new JScrollPane(deletedTable);	
 		deletedPanel.add(pane,BorderLayout.CENTER);
 		splitPane.setRightComponent(deletedPanel);
 		add(splitPane, BorderLayout.CENTER);
 		
 		
 	}
 	
 	
 	
 	private void initCategoryItems() {
 		for (String cat : CategoryConstants.DEFAULT_CATS){
 			JCheckBoxMenuItem cbi = new JCheckBoxMenuItem(cat);
 			cbi.setActionCommand(cat);
 			cbi.setSelected(true);
 			cbi.addActionListener(dvc);
 			this.mnNewMenu.add(cbi);
 			this.catItems.add(cbi);
 			
 			
 		}
 	}
 	
 	
 	/**
 	 * add the available versions as options for the combo boxes
 	 */
 	private void initVersions (){
 		for (Integer v : this.vm.getVersions().keySet()){
 			this.fromModel.addElement(v);
 			this.toModel.addElement(v);
 		}
 		this.fromBox.setSelectedIndex(0);
 		this.toBox.setSelectedIndex(toModel.getSize()-1);
 	}
 	
 	
 	private void clearVersionBoxes(){
 		this.fromModel.removeAllElements();
 		this.toModel.removeAllElements();
 	}
 	
 	public void  updateColorTree(){
 		List<String> selCats = new ArrayList <String>();
 		if (!dvm.isEmpty()){
 			for (JCheckBoxMenuItem bi :this.catItems){
 				if (bi.isSelected()){
 					selCats.add(bi.getActionCommand());
 				}
 			}
 			this.diffTree.updateCategoryColors(
 					dvm.getCategoryItemMap(), selCats.toArray(new String[]{}));
 			this.diffTree.updateUI();
 		}
 		
 	}
 
 	public void updateDiffTree() {
 		
		Integer lastKey = (Integer) this.toBox.getSelectedItem();
 		Integer firstKey = (Integer) this.fromBox.getSelectedItem();
 		if (firstKey<lastKey){
 			this.diffTree.release();
 			CRFVersion firstVersion = vm.getVersions().get(firstKey);
 			CRFVersion lastVersion=vm.getVersions().get(lastKey);
 			diffTree.loadVersion(lastVersion);
 			dtc.setVersion(lastKey);
 			HashMap<String,List<String>> changeGraph = dvm.getChangeGraph();
 			HashMap<Item,List<VersionPair>> itemChangedPerVersion = dvm.getDiffVersionsPerItem( firstVersion,lastVersion);
 			diffTree.updateCategoryColors(dvm.getCategoryItemMap(),
 					CategoryConstants.DEFAULT_CATS);
 			
 			for (Entry<Item,List<VersionPair>> e: itemChangedPerVersion.entrySet()){
 				List<VersionPair> diffVersions = e.getValue();
 				Item i = e.getKey();
 				List<String> items = changeGraph.get(i.getItemLabel());
 				for (int ver = 0;ver<diffVersions.size();ver++){
 					VersionPair vp = diffVersions.get(ver);
 					String itemLabel = items.get(ver);
 					DiffVersion dv = dvm.getDiffVersionMap().get(vp);
 					CRFVersion v = vm.getVersions().get(vp.getOldVersion());
 					Item oldItem = v.getItems().get(itemLabel);
 					if(!dv.getOldNewItemMap().containsKey(oldItem)){
 						diffTree.addDiffForItem(itemLabel, CellConstants.ADD_TYPE, vp.getOldVersion(), vp.getNewVersion(), itemLabel);
 					}else{	
 						diffTree.addDiffForItem(e.getKey().getItemLabel(), CellConstants.MOD_TYPE, vp.getOldVersion(), vp.getNewVersion(), itemLabel);
 					}
 				}
 			}
 			diffTree.updateUI();
 		}else {
 			JOptionPane.showConfirmDialog(null, "The first version might be smaller than the second version", "version error", JOptionPane.OK_OPTION,JOptionPane.WARNING_MESSAGE);
 		}
 		
 	}
 	
 	public void updateDeletedTable(){
 		
 		Integer lastKey = (Integer) this.toBox.getSelectedItem();
 		Integer firstKey = (Integer) this.fromBox.getSelectedItem();
 		if (firstKey<lastKey){
 			deletedItemModel.clear();
 			VersionPair lastVersion = null;
 			VersionPair firstVersion = null;
 			for (VersionPair vp : dvm.getDiffVersionMap().keySet()){
 				if (vp.getNewVersion()==lastKey){
 					lastVersion = vp;
 					break;
 				}
 			}
 			
 			for (VersionPair vp : dvm.getDiffVersionMap().keySet()){
 				if (vp.getOldVersion()==firstKey){
 					firstVersion = vp;
 					break;
 				}
 			}
 			boolean isFirst =true;
 			
 			do{
 				if (!isFirst){
 					lastVersion = dvm.getDiffVersionMap().lowerKey(lastVersion);
 				}else isFirst = false;
 				DiffVersion dv = dvm.getDiffVersionMap().get(lastVersion);
 				List<Item> delItems = dv.getDeletedItems();
 				for (Item di : delItems){
 					int r = this.deletedItemModel.addRow();
 					this.deletedItemModel.setValueAt(lastVersion.getOldVersion(), r, CellConstants.VERSION_COL);
 					this.deletedItemModel.setValueAt(di.getItemLabel(), r, CellConstants.ITEM_COL);
 				}
 				
 				
 			}while (!lastVersion.equals(firstVersion));
 			log.debug("delete Table is ready");
 		}
 		
 	}
 	
 	public void clearAllComponents(){
 		this.diffTree.clearSelection();
 		this.diffTree.release();
 		deletedItemModel.clear();
 		this.clearVersionBoxes();
 		for (JCheckBoxMenuItem bi : catItems){
 			bi.setSelected(true);
 		}
 		
 		
 	}
 	
 	public static void  main(String[] arg){
 		JFrame f = new JFrame();
 		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		f.setContentPane(new DiffPanel(null));
 		f.pack();
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		if(evt.getSource() instanceof VersionManager){
 			if (evt.getPropertyName().equals(VersionManager.CLEAR_VERSIONS)){
 				log.info("release Diff Panel components");
 				this.dvm.clearAll();
 				this.clearAllComponents();
 			}else if (evt.getPropertyName().equals(VersionManager.ADD_VERSIONS)) {
 				this.initVersions();
 			}
 		}
 		
 	}
 
 	public JTable getDeletedTable() {
 		// TODO Auto-generated method stub
 		return this.deletedTable;
 	}
 
 	public DiffVersionController getDiffController() {
 		return this.dvc;
 		
 	}
 	
 	public Integer getFromVersion(){
 		return (Integer) this.fromBox.getSelectedItem();
 	}
 
 	public Integer getUntilVersion(){
 		return (Integer) this.toBox.getSelectedItem();
 	}
 	
 }
