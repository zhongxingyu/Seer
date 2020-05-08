 package org.bestgrid.virtscreen.view;
 
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.JComboBox;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.border.TitledBorder;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.bestgrid.virtscreen.model.GoldConfFile;
 import org.bestgrid.virtscreen.model.LigandFiles;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
 import org.vpac.grisu.frontend.view.swing.jobcreation.widgets.AbstractWidget;
 import org.vpac.grisu.model.FileManager;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class GoldLibrarySelectPanel extends AbstractWidget {
 
 	public static final String N_A_MESSAGE = "Specified library not available.";
 	private final DefaultListModel ligandModel;
 	private final JList list;
 
 	private final DefaultComboBoxModel ligandModelCombo;
 	private final JComboBox combo;
 
 	private final ServiceInterface si = null;
 	private GoldConfFile confFile = null;
 	private JScrollPane scrollPane;
 	private final Boolean useComboBox;
 	private JComboBox comboBox;
 	private JComboBox comboBox_1;
 
 	public GoldLibrarySelectPanel() {
 		this(true);
 	}
 
 	public GoldLibrarySelectPanel(boolean useCombo) {
 		this.useComboBox = useCombo;
 		if (useCombo) {
 			ligandModelCombo = new DefaultComboBoxModel();
 			combo = new JComboBox(ligandModelCombo);
 			combo.addItemListener(new ItemListener() {
 
 				public void itemStateChanged(ItemEvent e) {
 					if (e.getStateChange() == ItemEvent.SELECTED) {
 						if (!e.getItem().equals(N_A_MESSAGE)) {
 							ligandModelCombo.removeElement(N_A_MESSAGE);
 						} else {
 							return;
 						}
 						if (confFile == null) {
 							return;
 						}
 						confFile.setCostumLigandDataFiles(new String[] { (String) e
 								.getItem() });
 					}
 				}
 			});
 			ligandModel = null;
 			list = null;
 		} else {
 			ligandModel = new DefaultListModel();
 			list = new JList(ligandModel);
 			ligandModelCombo = null;
 			combo = null;
 		}
 
 		String title = "Library";
 		if (!useCombo) {
 			title = "Libraries";
 		}
 		setBorder(new TitledBorder(null, title, TitledBorder.LEADING,
 				TitledBorder.TOP, null, null));
 
 		if (useComboBox) {
 			setLayout(new FormLayout(new ColumnSpec[] {
 					FormFactory.RELATED_GAP_COLSPEC,
 					ColumnSpec.decode("max(36dlu;default)"),
 					FormFactory.RELATED_GAP_COLSPEC,
 					ColumnSpec.decode("default:grow"),
 					FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC, }));
			add(combo, "2, 2, 3, 1, fill, fill");
 		} else {
 			setLayout(new FormLayout(new ColumnSpec[] {
 					FormFactory.RELATED_GAP_COLSPEC,
 					ColumnSpec.decode("max(36dlu;default)"),
 					FormFactory.RELATED_GAP_COLSPEC,
 					ColumnSpec.decode("default:grow"),
 					FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
 					FormFactory.RELATED_GAP_ROWSPEC,
 					RowSpec.decode("max(58dlu;default):grow"),
 					FormFactory.RELATED_GAP_ROWSPEC, }));
 			add(getScrollPane(), "2, 2, 3, 1, fill, fill");
 		}
 
 	}
 
 	@Override
 	public void setValue(String value) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void loadLibraries() {
 		if (confFile == null || getServiceInterface() == null) {
 			if (useComboBox) {
 				combo.setEnabled(false);
 			} else {
 				list.setEnabled(false);
 			}
 			return;
 		}
 
 		if (useComboBox) {
 			combo.setEnabled(true);
 		} else {
 			list.setEnabled(true);
 		}
 
 		String[] ligandFiles = confFile.getLigandUrls();
 
 		if (useComboBox) {
 			// only use first one...
 			if (ligandFiles.length > 0) {
 				String lib = FileManager.getFilename(ligandFiles[0]);
 				if (ligandModelCombo.getIndexOf(lib) < 0) {
 					if (ligandModelCombo.getIndexOf(N_A_MESSAGE) < 0) {
 						ligandModelCombo.addElement(N_A_MESSAGE);
 					}
 					ligandModelCombo.setSelectedItem(N_A_MESSAGE);
 				} else {
 					combo.setSelectedItem(FileManager
 							.getFilename(ligandFiles[0]));
 				}
 			}
 		} else {
 			int[] temp = {};
 			list.setSelectedIndices(temp);
 			List<Integer> selected = new LinkedList<Integer>();
 			for (String ligand : ligandFiles) {
 				int i = ligandModel.indexOf(FileManager.getFilename(ligand));
 				if (i >= 0) {
 					selected.add(i);
 				}
 			}
 
 			list.setSelectedIndices(ArrayUtils.toPrimitive(selected
 					.toArray(new Integer[] {})));
 		}
 	}
 
 	public String[] getSelectedLibraryFiles() {
 
 		if (useComboBox) {
 			String libFile = (String) combo.getSelectedItem();
 			return new String[] { libFile };
 		} else {
 			Object[] o = list.getSelectedValues();
 			String[] result = new String[o.length];
 			for (int i = 0; i < o.length; i++) {
 				result[i] = (String) o[i];
 			}
 			return result;
 		}
 	}
 
 	@Override
 	public void setServiceInterface(ServiceInterface si) {
 		super.setServiceInterface(si);
 		List<String> allLigands = null;
 
 		try {
 			allLigands = getFileManager().listAllChildrenFilesOfRemoteFolder(
 					LigandFiles.VS_LIBRARY_FILES_URL);
 			if (useComboBox) {
 				ligandModelCombo.removeAllElements();
 				for (String ligand : allLigands) {
 					ligandModelCombo
 							.addElement(FileManager.getFilename(ligand));
 				}
 			} else {
 				ligandModel.removeAllElements();
 				for (String ligand : allLigands) {
 					ligandModel.addElement(FileManager.getFilename(ligand));
 				}
 			}
 		} catch (RemoteFileSystemException e) {
 			e.printStackTrace();
 		}
 		loadLibraries();
 	}
 
 	public void setGoldConfFile(GoldConfFile confFile) {
 		this.confFile = confFile;
 		loadLibraries();
 	}
 
 	@Override
 	public String getValue() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private JScrollPane getScrollPane() {
 		if (scrollPane == null) {
 			scrollPane = new JScrollPane();
 			// scrollPane.setViewportView(getComboBox_1());
 
 			if (useComboBox) {
 				scrollPane.setViewportView(combo);
 			} else {
 				scrollPane.setViewportView(list);
 			}
 		}
 		return scrollPane;
 	}
 
 	private JComboBox getComboBox_1() {
 		if (comboBox_1 == null) {
 			comboBox_1 = new JComboBox();
 		}
 		return comboBox_1;
 	}
 }
