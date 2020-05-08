 package ch.zhaw.simulation.window.xy.sidebar;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.undo.UndoManager;
 
 import org.jdesktop.swingx.JXTaskPane;
 import org.jdesktop.swingx.VerticalLayout;
 
 import butti.javalibs.config.Settings;
 import butti.javalibs.gui.messagebox.Messagebox;
 import butti.javalibs.listener.ListenerList;
 import butti.javalibs.util.StringUtil;
 import ch.zhaw.simulation.densitydraw.DensityLegendView;
 import ch.zhaw.simulation.editor.density.DensityListModel;
 import ch.zhaw.simulation.editor.xy.density.DensityEditorDialog;
 import ch.zhaw.simulation.frame.sidebar.SidebarPosition;
 import ch.zhaw.simulation.icon.IconLoader;
 import ch.zhaw.simulation.model.listener.XYSimulationAdapter;
 import ch.zhaw.simulation.model.xy.DensityData;
 import ch.zhaw.simulation.model.xy.SimulationXYModel;
 import ch.zhaw.simulation.sysintegration.Sysintegration;
 import ch.zhaw.simulation.undo.action.xy.ChangeDensityUndoAction;
 import ch.zhaw.simulation.undo.action.xy.CreateDensityUndoAction;
 import ch.zhaw.simulation.undo.action.xy.DeleteDensityUndoAction;
 
 public class DensitySidebar extends JXTaskPane implements SidebarPosition {
 	private static final long serialVersionUID = 1L;
 
 	private DensityListModel listModel;
 	private JComboBox cbDensity;
 
 	private DensityEditorDialog densityEditor;
 
 	private DensityData lastSelected = null;
 	protected ListenerList listenerList = new ListenerList();
 
 	private DensityLegendView legendView;
 
 	private JCheckBox cbLog;
 
 	public DensitySidebar(final JFrame parent, final Settings settings, final SimulationXYModel model, JComponent comp, Sysintegration sys,
 			final UndoManager undo) {
 		setTitle("Dichte");
 
 		listModel = new DensityListModel(model);
 		cbDensity = new JComboBox(listModel);
 		cbDensity.setRenderer(new DensityListCellRenderer());
 
 		densityEditor = new DensityEditorDialog(parent, model, sys);
 
 		setLayout(new VerticalLayout());
 
 		add(cbDensity);
 
 		cbDensity.addItemListener(new ItemListener() {
 
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					selectionChanged();
 				}
 			}
 		});
 
 		JPanel p = new JPanel();
		p.setLayout(new FlowLayout());
 		add(p);
 
 		JButton btCreate = new JButton("[+]");
 		btCreate.addActionListener(new ActionListener() {
 
 			int id = 1;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String selectedName = null;
 				for (int i = 0; i < 100; i++) {
 					String name = "d" + (id++);
 					if (model.isValidDensityName(name, null)) {
 						selectedName = name;
 						break;
 					}
 				}
 
 				if (selectedName == null) {
 					Messagebox.showError(parent, "Kein Name", "<html><b>Sorry, die Applikation konnte keinen Namen finden für die Dichte,</b><br>"
 							+ "versuchen Sie es erneut, oder kontaktieren Sie den Entwickler...</html>");
 					return;
 				}
 
 				DensityData d = new DensityData();
 				d.setName(selectedName);
 				d.setDescription("");
 				model.addDensity(d);
 				cbDensity.setSelectedItem(d);
 
 				undo.addEdit(new CreateDensityUndoAction(d, model));
 			}
 		});
 
 		p.add(btCreate);
 
 		JButton btDelete = new JButton("[-]");
 
 		btDelete.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				DensityData d = (DensityData) cbDensity.getSelectedItem();
 				if (d != null) {
 					model.removeDensity(d);
 					undo.addEdit(new DeleteDensityUndoAction(d, model));
 				}
 			}
 		});
 
 		p.add(btDelete);
 
 		JButton btEdit = new JButton(IconLoader.getIcon("edit"));
 		btEdit.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				DensityData d = (DensityData) cbDensity.getSelectedItem();
 				if (d != null) {
 					String oldName = d.getName();
 					String oldDescription = d.getDescription();
 					String oldFormula = d.getFormula();
 
 					densityEditor.setSelectedDensity(d);
 					densityEditor.setVisible(true);
 
 					String newName = d.getName();
 					String newDescription = d.getDescription();
 					String newFormula = d.getFormula();
 
 					if (!StringUtil.equals(oldName, newName) || !StringUtil.equals(oldDescription, newDescription)
 							|| !StringUtil.equals(oldFormula, newFormula)) {
 						undo.addEdit(new ChangeDensityUndoAction(d, oldName, oldDescription, oldFormula, newName, newDescription, newFormula, model));
 					}
 
 				} else {
 					Messagebox.showWarning(parent, "Kein Eintrag gwählt", "Bitte wählen / erstellen Sie zuerst einen Eintrag mit dem [+]");
 				}
 			}
 		});
 
 		p.add(btEdit);
 
 		model.addListener(new XYSimulationAdapter() {
 
 			@Override
 			public void densityChanged(DensityData d) {
 				if (d == lastSelected) {
 					listenerList.actionPerformed(null);
 				}
 			}
 		});
 
 		this.legendView = new DensityLegendView();
 		add(legendView);
 
 		this.cbLog = new JCheckBox("Logarithmisch");
 		add(cbLog);
 
 		cbLog.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (lastSelected != null) {
 					lastSelected.setDisplayLogarithmic(cbLog.isSelected());
 					fireDensityChanged();
 				}
 			}
 		});
 
 		lastSelected = (DensityData) listModel.getSelectedItem();
 	}
 
 	public DensityLegendView getLegendView() {
 		return legendView;
 	}
 
 	protected void selectionChanged() {
 		if (lastSelected == cbDensity.getSelectedItem()) {
 			return;
 		}
 		lastSelected = (DensityData) cbDensity.getSelectedItem();
 		this.cbLog.setSelected(lastSelected.isDisplayLogarithmic());
 
 		fireDensityChanged();
 	}
 
 	private void fireDensityChanged() {
 		listenerList.actionPerformed(null);
 	}
 
 	public DensityData getSelected() {
 		return (DensityData) listModel.getSelectedItem();
 	}
 
 	public void dispose() {
 		listModel.dispose();
 	}
 
 	@Override
 	public int getSidebarPosition() {
 		return 100;
 	}
 
 	public void addActionListener(ActionListener l) {
 		listenerList.addListener(l);
 	}
 
 	public void removeActionListener(ActionListener l) {
 		listenerList.removeListener(l);
 	}
 }
