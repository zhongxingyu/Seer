 package cadenza.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumnModel;
 
 import cadenza.control.CadenzaController;
 import cadenza.control.CadenzaController.Mode;
 import cadenza.core.CadenzaData;
 import cadenza.core.Cue;
 import cadenza.core.Patch;
 import cadenza.core.patchusage.PatchUsage;
 import cadenza.gui.common.CadenzaTable;
 import cadenza.gui.common.SinglePatchSelectionDialog;
 import cadenza.gui.patch.PatchEditDialog;
 import cadenza.gui.patch.PatchPickerDialog;
 
 import common.collection.ListAdapter;
 import common.collection.ListEvent;
 import common.swing.ColorUtils;
 import common.swing.SwingUtils;
 import common.swing.table.ListTableModel;
 
 public class PatchEditor extends JPanel {
 	private static final Color ODD_BACKGROUND = new Color(200, 210, 255);
 	private static final Color EVEN_BACKGROUND = Color.WHITE;
 	
 	private final Component _parent;
 	private final CadenzaData _data;
 	private final CadenzaController _controller;
 	
 	private PatchTable _table;
 	
 	public PatchEditor(Component parent, CadenzaData data, CadenzaController controller) {
 		super();
 		_parent = parent;
 		_data = data;
 		_controller = controller;
 		init();
 	}
 	
 	public void setSelectedPatch(Patch patch) {
 		if (patch == null) {
 			_table.accessTable().clearSelection();
 			return;
 		}
 		
 		final int row = _data.patches.indexOf(patch);
 		_table.accessTable().setRowSelectionInterval(row, row);
 	}
 	
 	private void init() {
		final JButton selectButton = SwingUtils.iconButton(ImageStore.SEARCH, new SelectPatchAction());
 		final JButton replaceButton = SwingUtils.iconButton(ImageStore.REPLACE, new ReplacePatchAction());
 		replaceButton.setEnabled(false);
 		
 		_table = new PatchTable(selectButton, replaceButton);
 		
 		final TableColumnModel tcm = _table.accessTable().getColumnModel();
 		tcm.getColumn(0).setPreferredWidth(400);
 		tcm.getColumn(1).setPreferredWidth(200);
 		tcm.getColumn(2).setPreferredWidth(100);
 		tcm.getColumn(3).setPreferredWidth(100);
 		tcm.getColumn(4).setPreferredWidth(200);
 
 		_table.accessTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent _) {
 				final boolean one = _table.accessTable().getSelectedRowCount() == 1;
 				replaceButton.setEnabled(one);
 				if (one) {
 					_controller.setMode(Mode.PREVIEW);
 					_controller.setPatch(_table.getSelectedRows().get(0));
 				}
 			}
 		});
 		
 		setLayout(new BorderLayout());
 		add(_table, BorderLayout.CENTER);
 		
 		_data.patches.addListener(new ListAdapter<Patch>() {
 			@Override
 			public void anyChange(ListEvent<Patch> _) {
 				_table.accessTableModel().setList(_data.patches);
 			}
 		});
 	}
 	
 	private class PatchTable extends CadenzaTable<Patch> {
 		public PatchTable(final JButton selectButton, final JButton replaceButton) {
 			super(_data.patches, true, false, "Patches:", Box.createHorizontalStrut(16), selectButton, replaceButton);
 			
 			accessTable().setDefaultRenderer(Object.class, new PatchTableRenderer());
 		}
 		
 		@Override
 		protected ListTableModel<Patch> createTableModel() {
 			return new ListTableModel<Patch>() {
 				@Override
 				public String[] declareColumns() {
 					return new String[] {"Name", "Synth", "Bank", "Number", "Default Volume"};
 				}
 				
 				@Override
 				public Object resolveValue(Patch row, int column) {
 					switch (column) {
 						case 0: return row.name;
 						case 1: return row.getSynthesizer().getName();
 						case 2: return row.bank;
 						case 3: return Integer.valueOf(row.number);
 						case 4: return Integer.valueOf(row.defaultVolume);
 						default: throw new IllegalStateException("Unknown Column!");
 					}
 				}
 			};
 		}
 		
 		@Override
 		protected String declareTypeName() {
 			return "patch";
 		}
 		
 		@Override
 		protected void takeActionOnAdd() {
 			final PatchEditDialog dialog = new PatchEditDialog(_parent, _data.synthesizers, null, _data.patches);
 			dialog.showDialog();
 			if (dialog.okPressed()) {
 				_data.patches.add(dialog.getPatch());
 				Collections.sort(_data.patches);
 			}
 		}
 		
 		@Override
 		protected void takeActionOnEdit(Patch patch) {
 			final PatchEditDialog dialog = new PatchEditDialog(_parent, _data.synthesizers, patch, _data.patches);
 			dialog.showDialog();
 			if (dialog.okPressed()) {
 				final Patch edit = dialog.getPatch();
 				if (patch.equals(edit)) {
 					return;
 				}
 				
 				if (patch.defaultVolume != edit.defaultVolume) {
 					int result = JOptionPane.showConfirmDialog(_parent,
 							"Persist change to default volume?",
 							"Choose", JOptionPane.YES_NO_OPTION);
 					if (result == JOptionPane.OK_OPTION) {
 						for (Cue cue : _data.cues) {
 							for (PatchUsage patchUsage : cue.patches) {
 								if (patchUsage.patch == patch && patchUsage.volume == patch.defaultVolume) {
 									patchUsage.volume = edit.defaultVolume;
 								}
 							}
 						}
 					}
 				}
 				
 				patch.copyFrom(edit, true);
 				Collections.sort(_data.patches);
 			}
 		}
 		
 		@Override
 		protected String declareAdditionalDeleteWarning(List<Patch> toDelete) {
 			return "Patches will be removed from any cue that uses them.";
 		}
 		
 		@Override
 		protected void takeActionAfterDelete(List<Patch> removed) {
 			for (final Patch patch : removed) {
 				for (final Cue cue : _data.cues) {
 					final List<PatchUsage> remove = new LinkedList<>();
 					for (final PatchUsage usage : cue.patches) {
 						if (usage.patch == patch) {
 							remove.add(usage);
 						}
 					}
 					for (final PatchUsage usage : remove) {
 						cue.patches.remove(usage);
 					}
 				}
 			}
 		}
 		
 		private class PatchTableRenderer extends DefaultTableCellRenderer {
 			@Override
 			public Component getTableCellRendererComponent(JTable table,
 					Object value, boolean isSelected, boolean hasFocus,
 					int row, int column) {
 				final JLabel label = (JLabel) super.getTableCellRendererComponent(
 						table, value, isSelected, hasFocus, row, column);
 				if (isSelected) {
 					label.setForeground(table.getSelectionForeground());
 					label.setBackground(table.getSelectionBackground());
 				}
 				else {
 					if (column == 0) {
 						final Color bg = _data.patches.get(row).getDisplayColor();
 						label.setBackground(bg);
 						label.setForeground(ColorUtils.getBrightness(bg) > 0.5 ? Color.BLACK : Color.WHITE);
 					}
 					else {
 						label.setForeground(Color.BLACK);
 						label.setBackground(row % 2 == 0 ? EVEN_BACKGROUND : ODD_BACKGROUND);
 					}
 				}
 				
 				return label;
 			}
 		}
 	}
 	
 	private class SelectPatchAction extends AbstractAction {
 		public SelectPatchAction() {
 			putValue(SHORT_DESCRIPTION, "Select a patch from a searchable list");
 		}
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			final PatchPickerDialog dialog = new PatchPickerDialog(_parent, _data.synthesizers);
 			dialog.showDialog();
 			if (dialog.okPressed()) {
 				_data.patches.add(dialog.getSelectedPatch());
 				Collections.sort(_data.patches);
 			}
 		}
 	}
 	
 	private class ReplacePatchAction extends AbstractAction {
 		public ReplacePatchAction() {
 			putValue(SHORT_DESCRIPTION, "Replace all occurrences with another patch");
 		}
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			final Patch patch = _data.patches.get(_table.accessTable().getSelectedRow());
 			final SinglePatchSelectionDialog dialog = new SinglePatchSelectionDialog(_parent, patch, _data.patches, _data.synthesizers);
 			dialog.showDialog();
 			if (dialog.okPressed()) {
 				final Patch replacement = dialog.getSelectedPatch();
 				if (replacement.equals(patch)) {
 					return;
 				}
 				
 				_data.patches.remove(patch);
 				
 				if (!_data.patches.contains(replacement)) {
 					_data.patches.add(replacement);
 				}
 				
 				Collections.sort(_data.patches);
 				
 				for (final Cue cue : _data.cues) {
 					for (final PatchUsage patchUsage : cue.patches) {
 						if (patchUsage.patch.equals(patch)) {
 							patchUsage.patch = replacement;
 						}
 					}
 				}
 			}
 		}
 	}
 }
