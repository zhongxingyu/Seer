 package de.uni_leipzig.imise.visualization.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import de.uni_leipzig.imise.classification.ChangeClassifier;
 import de.uni_leipzig.imise.data.CRFVersion;
 import de.uni_leipzig.imise.data.DiffVersion;
 import de.uni_leipzig.imise.data.PropertyMapping;
 import de.uni_leipzig.imise.data.VersionPair;
 import de.uni_leipzig.imise.data.managment.DiffVersionManager;
 import de.uni_leipzig.imise.data.managment.VersionManager;
 import de.uni_leipzig.imise.diff.calculation.DiffCalculator;
 import de.uni_leipzig.imise.visualization.view.CRFTableModel;
 import de.uni_leipzig.imise.visualization.view.CellConstants;
 import de.uni_leipzig.imise.visualization.view.DiffPanel;
 import de.uni_leipzig.imise.visualization.view.EventConstants;
 import de.uni_leipzig.imise.visualization.view.ItemPanel;
 
 public class DiffVersionController implements ActionListener, ListSelectionListener{
 
 	
 	private VersionManager vm;
 	private DiffVersionManager dvm;
 	private DiffPanel dvp;
 	private DiffCalculator diffCalculator;
 	private PropertyChangeSupport change;
 	private HashMap<String,String> selectMap;
 	private ChangeClassifier changeClassifier;
 
 	public DiffVersionController(VersionManager vm, DiffVersionManager dvm,
 			DiffPanel dvp){
 		this.vm = vm;
 		this.dvm = dvm;
 		this.dvp = dvp;
 		selectMap = new HashMap<String,String>();
 		this.change =new PropertyChangeSupport(this);
 		this.diffCalculator = new DiffCalculator();
 		this.changeClassifier = new ChangeClassifier();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals(EventConstants.DIFF_CALC)){
 			if (!vm.getVersions().isEmpty()){
 				if (dvm.isEmpty()){
 					for (Entry<Integer,CRFVersion> entry:vm.getVersions().entrySet()){
 						Integer nextKey = vm.getVersions().higherKey(entry.getKey());
 						if (nextKey!=null){
 							CRFVersion nextVersion = vm.getVersions().get(nextKey);
 							//diffCalculator.clearAll();
 							diffCalculator.calculateDiff(entry.getValue(), nextVersion);
 							HashMap<String, HashMap<String, PropertyMapping>>propMap = 
 									changeClassifier.classify(diffCalculator.getModifiedItems());
 							VersionPair vp = new VersionPair(entry.getKey(),nextKey);
 							DiffVersion dv = new DiffVersion();
 							
 							dv.setAddedItems(diffCalculator.getAddedItems());
 							dv.setDeletedItems(diffCalculator.getDeletedItems());
 							dv.setEqualItems(diffCalculator.getEqualItems());
 							dv.setModifiedItems(propMap);
 							dv.setNewOldItemMap(diffCalculator.getNewOldItemMap());
 							dv.setOldNewItemMap(diffCalculator.getOldNewItemMap());
 							dvm.addDiff(vp, dv);
 						}
 					}
 					dvp.updateDiffTree();
 					dvp.updateDeletedTable();
 				}
 			}else {
 				JOptionPane.showConfirmDialog(null, "Sie müssen eine Menge von CRF Versionen vorab laden", "Keine Versionen", JOptionPane.WARNING_MESSAGE);
 			}
 		}else if (e.getSource() instanceof JCheckBoxMenuItem){
 			dvp.updateColorTree();
 		}
 		
 	}
 
 	@Override
 	public void valueChanged(ListSelectionEvent e) {
 		JTable t = dvp.getDeletedTable();
 		CRFTableModel dtm = (CRFTableModel) t.getModel();
 		
 		if (!t.getSelectionModel().isSelectionEmpty()){
 			int index = t.getSelectionModel().getMinSelectionIndex();
 			
 			HashMap<String,String> oldMap = new HashMap<String,String>();
 			for (Entry <String,String>entry :this.selectMap.entrySet()){
 				oldMap.put(entry.getKey(), entry.getValue());
 			}
 			this.selectMap.clear();
 			this.selectMap.put(CellConstants.ITEM_COL, (String) dtm.getValueAt(index, CellConstants.ITEM_COL));
 			this.selectMap.put(CellConstants.VERSION_COL, (String) dtm.getValueAt(index, CellConstants.VERSION_COL).toString());
 			change.firePropertyChange(EventConstants.SELECT_ITEM,oldMap, selectMap);
 			
 		}
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener l) {
 		change.addPropertyChangeListener(l);
 	}
 	
 	public void removePropertyChangeListener(PropertyChangeListener l) {
 		change.removePropertyChangeListener(l);
 	}
 	
 	
 }
