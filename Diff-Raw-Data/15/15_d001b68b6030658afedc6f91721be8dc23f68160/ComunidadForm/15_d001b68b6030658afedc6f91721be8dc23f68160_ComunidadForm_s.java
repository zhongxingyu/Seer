 package es.udc.cartolab.gvsig.pmf.forms;
 
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JInternalFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.event.InternalFrameEvent;
 import javax.swing.event.InternalFrameListener;
 import javax.swing.table.TableModel;
 
 import org.apache.log4j.Logger;
 
 import com.hardcode.driverManager.DriverLoadException;
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.hardcode.gdbms.engine.data.DataSourceFactory;
 import com.hardcode.gdbms.engine.instruction.EvaluationException;
 import com.hardcode.gdbms.engine.instruction.SemanticException;
 import com.hardcode.gdbms.engine.strategies.FilteredDataSource;
 import com.hardcode.gdbms.parser.ParseException;
 import com.iver.andami.PluginServices;
 import com.iver.andami.ui.mdiManager.IWindow;
 import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
 import com.iver.cit.gvsig.fmap.edition.IEditableSource;
 import com.iver.cit.gvsig.fmap.edition.IRowEdited;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
 import com.iver.cit.gvsig.project.documents.table.gui.Table;
 import com.jeta.forms.components.panel.FormPanel;
 
 import es.udc.cartolab.gvsig.navtable.AlphanumericNavTable;
 import es.udc.cartolab.gvsig.navtableforms.AbstractForm;
 import es.udc.cartolab.gvsig.navtableforms.Utils;
 import es.udc.cartolab.gvsig.navtableforms.validation.FormBinding;
 import es.udc.cartolab.gvsig.navtableforms.validation.FormModel;
 import es.udc.cartolab.gvsig.pmf.forms.table.NonEditableTableModel;
 import es.udc.cartolab.gvsig.pmf.forms.validation.binding.ComunidadBinding;
 import es.udc.cartolab.gvsig.pmf.forms.validation.model.ComunidadModel;
 import es.udc.cartolab.gvsig.pmf.preferences.Preferences;
 
 public class ComunidadForm extends AbstractForm implements MouseListener,
 		InternalFrameListener {
 	private JTable orgBaseTable;
 	private JTable presInstTable;
 
 	private long csaludPosition;
 	private long creunionPosition;
 	private long centJardPosition;
 	private long centEscPosition;
 	private long centCCPosition;
 	private long centDIVPosition;
 
 	private boolean chbChanged = false;
 	private final String COD_COM = "cod_com";
 
 	public ComunidadForm(FLyrVect layer) {
 		super(layer);
 		viewInfo.setHeight(800);
 		viewInfo.setWidth(575);
 		viewInfo.setTitle(PluginServices.getText(this, "_comunidades"));
 		orgBaseTable = (JTable) formBody.getComponentByName("orgBase");
 		presInstTable = (JTable) formBody.getComponentByName("presInst");
 	}
 
 	@Override
 	public boolean init() {
 		boolean r = super.init();
 		setChangedValues(getCHBChanged());
 
 		fillJTable(orgBaseTable, "organizacion_base");
 		fillJTable(presInstTable, "presencia_institucional");
 
 		return r;
 	}
 
 	@Override
 	public FormPanel getFormBody() {
 		return new FormPanel("comunidad.xml");
 	}
 
 	@Override
 	public FormModel getFormModel(Container c) {
 		return new ComunidadModel(c);
 	}
 
 	@Override
 	public FormBinding getFormBinding(FormModel model) {
 		return new ComunidadBinding(model);
 	}
 
 	@Override
 	public Logger getLoggerName() {
 		return Logger.getLogger("PMF");
 	}
 
 	protected String getXmlFileName() {
 		return Preferences.getXMLFileName();
 	}
 
 	protected String getAliasInXML() {
 		return "comunidad";
 	}
 
 	protected boolean isPKAlreadyInUse() {
 		try {
 			if (recordset.getRowCount() == 0) {
 				return false;
 			} else {
 				String nameOfKeyInModel = "cod_com";
 				String nameOfKeyInRecordSet = "cod_com";
 				String valueFromModel = formModel.getWidgetValues().get(
 						nameOfKeyInModel);
 				for (int index = 0; index < recordset.getRowCount(); index++) {
 					int indiceCampo = recordset
 							.getFieldIndexByName(nameOfKeyInRecordSet);
 					String valueFromRecordSet = recordset.getFieldValue(index,
 							indiceCampo).toString();
 					if (valueFromRecordSet.equals(valueFromModel)) {
 						if (isUpdatingTheSameRegister(index, currentPosition)) {
 							return false;
 						} else {
 							return true;
 						}
 					}
 				}
 				return false;
 			}
 		} catch (ReadDriverException e) {
 			logger.error(e.getMessage(), e);
 			return false;
 		}
 	}
 
 	public void fillJTable(JTable table, String sourcename) {
 
 		IWindow[] windows = PluginServices.getMDIManager().getAllWindows();
 		FieldDescription[] columns = {};
 		String codCom = formModel.getWidgetValues().get(COD_COM);
 		for (int i = 0; i < windows.length; i++) {
 			if (windows[i] instanceof Table) {
 				String name = ((Table) windows[i]).getModel().getName();
 				if (name.endsWith(".dbf")) {
 					name = name.substring(0, name.lastIndexOf(".dbf"));
 					if (name.equals(sourcename)) {
 						IEditableSource source = ((Table) windows[i])
 								.getModel().getModelo();
 						columns = source.getFieldsDescription();
 
 						ArrayList<String> columnNames = new ArrayList<String>();
 
 						int codComPos = -1;
 
 						for (int j = 0; j < columns.length; j++) {
 							columnNames.add(PluginServices.getText(this,
 									columns[j].getFieldName()));
 							if (columns[j].getFieldName().equals(COD_COM))
 								codComPos = j;
 						}
 
 						ArrayList<Object[]> rows = new ArrayList<Object[]>();
 						Object[] row;
 
 						try {
 							for (int j = 0; j < source.getRowCount(); j++) {
 								IRowEdited sourceRow = source.getRow(j);
 								row = sourceRow.getAttributes();
 								if (codComPos >= 0) {
 									if (((com.hardcode.gdbms.engine.values.StringValue) row[codComPos])
 											.getValue().equals(codCom)) {
 										rows.add(row);
 									}
 								} else {
 									rows.add(row);
 								}
 							}
 						} catch (ReadDriverException e) {
 							e.printStackTrace();
 						}
 
 						Object[][] data = new Object[1][1];
 						table.setModel(new NonEditableTableModel(rows
 								.toArray(data), columnNames.toArray()));
 						break;
 					}
 				}
 			}
 		}
 
 		table.removeMouseListener(this);
 		table.addMouseListener(this);
 
 	}
 
 	private boolean isUpdatingTheSameRegister(int index, long currentPosition) {
 		if (index == (int) currentPosition) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	protected boolean primaryKeyHasErrors() {
 		if (isPKAlreadyInUse()) {
 			JOptionPane.showMessageDialog(this, PluginServices.getText(this,
 					"choose_other_pk"), PluginServices.getText(this,
 					"pk_already_used"), JOptionPane.ERROR_MESSAGE);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	protected boolean isSaveable() {
 		if (validationHasErrors()) {
 			return false;
 		} else if (primaryKeyHasErrors()) {
 			return false;
 		}
 		return true;
 	}
 
 	private long doFilter(IEditableSource recordset, ArrayList<String> where)
 			throws ReadDriverException, DriverLoadException, ParseException,
 			SemanticException, EvaluationException, IOException {
 		long recordPosition = 0;
 		for (int i = 0; i < recordset.getRowCount(); i++) {
 			IRowEdited row = recordset.getRow(i);
 			boolean same = true;
 			for (int j = 0; j < where.size(); j++) {
 				if (!where.get(j).equals(row.getAttribute(j).toString())) {
 					same = false;
 					break;
 				}
 			}
 			if (same) {
 				recordPosition = i;
 				break;
 			}
 		}
 		return recordPosition + 1;
 	}
 
 	private long doFilter(String layerName, String where)
 			throws ReadDriverException, DriverLoadException, ParseException,
 			SemanticException, EvaluationException, IOException {
 		long recordPosition = -1;
 		SelectableDataSource recordset = Utils.getFlyrVect(view, layerName)
 				.getRecordset();
 		String sql = "select * from " + recordset.getName() + " " + where + ";";
 		recordset.start();
 		FilteredDataSource result = (FilteredDataSource) recordset
 				.getDataSourceFactory().executeSQL(sql,
 						DataSourceFactory.MANUAL_OPENING);
 		if (result.getRowCount() == 1) {
 			recordPosition = result.getWhereFilter()[0];
 		}
 		recordset.stop();
 
 		return recordPosition;
 	}
 
 	private long enableButton(String elementName, String tableName, String where) {
 		long rowPos = 0;
 
 		JButton button = (JButton) formBody.getComponentByName(elementName
 				+ ".BT");
 		JCheckBox chb = (JCheckBox) formBody.getComponentByName(elementName
 				+ ".CHB");
 
 		try {
 			rowPos = doFilter(tableName, where) + 1;
 		} catch (Exception e) {
 			getLoggerName().error(e.getMessage(), e);
 		}
 
 		if (rowPos != 0) {
 			button.setEnabled(true);
 			if (!chb.isSelected()) {
 				setCHBChanged(true);
 			}
 			chb.setSelected(true);
 
 		} else {
 			button.setEnabled(false);
 			if (chb.isSelected()) {
 				setCHBChanged(true);
 			}
 			chb.setSelected(false);
 		}
 		return rowPos;
 	}
 
 	@Override
 	protected void fillSpecificValues() {
 		String foreingKey = formModel.getWidgetValues().get("cod_com");
 		String where = "where cod_com = '" + foreingKey + "'";
 		csaludPosition = enableButton("csalud", "centros_salud", where);
 		creunionPosition = enableButton("creunion", "centros_reuniones", where);
 		centJardPosition = enableButton("cent_jard", "centros_educativos",
 				where + " and tipo_cedu = 'Jardn de nios'");
 		centEscPosition = enableButton("cent_esc", "centros_educativos", where
 				+ " and tipo_cedu = 'Centro educativo escolar'");
 		centCCPosition = enableButton("cent_cc", "centros_educativos", where
 				+ " and tipo_cedu='Centro educativo de ciclo comn'");
 		centDIVPosition = enableButton("cent_div", "centros_educativos", where
 				+ " and tipo_cedu = 'Centro educativo diversificado'");
 	}
 
 	@Override
 	protected void setListeners() {
 		super.setListeners();
 
 		// fpuga: maybe this should be a for
 		JButton csaludBT = (JButton) formBody.getComponentByName("csalud.BT");
 		csaludBT.setActionCommand("csalud.BT");
 		csaludBT.addActionListener(this);
 
 		JButton creunionBT = (JButton) formBody
 				.getComponentByName("creunion.BT");
 		creunionBT.setActionCommand("creunion.BT");
 		creunionBT.addActionListener(this);
 
 		JButton cent_jardBT = (JButton) formBody
 				.getComponentByName("cent_jard.BT");
 		cent_jardBT.setActionCommand("cent_jard.BT");
 		cent_jardBT.addActionListener(this);
 
 		JButton cent_escBT = (JButton) formBody
 				.getComponentByName("cent_esc.BT");
 		cent_escBT.setActionCommand("cent_esc.BT");
 		cent_escBT.addActionListener(this);
 
 		JButton cent_ccBT = (JButton) formBody.getComponentByName("cent_cc.BT");
 		cent_ccBT.setActionCommand("cent_cc.BT");
 		cent_ccBT.addActionListener(this);
 
 		JButton cent_divBT = (JButton) formBody
 				.getComponentByName("cent_div.BT");
 		cent_divBT.setActionCommand("cent_div.BT");
 		cent_divBT.addActionListener(this);
 	}
 
 	@Override
 	protected void removeListeners() {
 		super.removeListeners();
 		JButton csaludBT = (JButton) formBody.getComponentByName("csalud.BT");
 		csaludBT.removeActionListener(this);
 		JButton creunionBT = (JButton) formBody
 				.getComponentByName("creunion.BT");
 		creunionBT.removeActionListener(this);
 		JButton cent_jardBT = (JButton) formBody
 				.getComponentByName("cent_jard.BT");
 		cent_jardBT.removeActionListener(this);
 		JButton cent_escBT = (JButton) formBody
 				.getComponentByName("cent_esc.BT");
 		cent_escBT.removeActionListener(this);
 		JButton cent_ccBT = (JButton) formBody.getComponentByName("cent_cc.BT");
 		cent_ccBT.removeActionListener(this);
 		JButton cent_divBT = (JButton) formBody
 				.getComponentByName("cent_div.BT");
 		cent_divBT.removeActionListener(this);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		super.actionPerformed(e);
 
 		AbstractForm dialog = null;
 		long pos = 0;
 		if (e.getActionCommand().equals("csalud.BT")) {
 			dialog = new CentroSaludForm(Utils.getFlyrVect(view,
 					"centros_salud"));
 			pos = csaludPosition;
 		} else if (e.getActionCommand().equals("creunion.BT")) {
 			dialog = new CentroReunionesForm(Utils.getFlyrVect(view,
 					"centros_reuniones"));
 			pos = creunionPosition;
 		} else if (e.getActionCommand().equals("cent_jard.BT")) {
 			dialog = new CentroEducativoForm(Utils.getFlyrVect(view,
 					"centros_educativos"));
 			pos = centJardPosition;
 		} else if (e.getActionCommand().equals("cent_esc.BT")) {
 			dialog = new CentroEducativoForm(Utils.getFlyrVect(view,
 					"centros_educativos"));
 			pos = centEscPosition;
 		} else if (e.getActionCommand().equals("cent_cc.BT")) {
 			dialog = new CentroEducativoForm(Utils.getFlyrVect(view,
 					"centros_educativos"));
 			pos = centCCPosition;
 		} else if (e.getActionCommand().equals("cent_div.BT")) {
 			dialog = new CentroEducativoForm(Utils.getFlyrVect(view,
 					"centros_educativos"));
 			pos = centDIVPosition;
 		}
 
 		if (dialog != null) {
 			if (dialog.init()) {
 				dialog.setPosition(pos);
 				PluginServices.getMDIManager().addWindow(dialog);
 			}
 		}
 	}
 
 	private void setCHBChanged(boolean changed) {
 		chbChanged = changed;
 	}
 
 	private boolean getCHBChanged() {
 		return chbChanged;
 	}
 
 	private void displayNavTable(JTable refTable, String dbfName) {
 		IWindow[] windows = PluginServices.getMDIManager().getAllWindows();
 		boolean found = false;
 		for (int i = 0; i < windows.length; i++) {
 			if (windows[i] instanceof Table) {
 				String name = ((Table) windows[i]).getModel().getName();
 				if (name.endsWith(".dbf")) {
 					name = name.substring(0, name.lastIndexOf(".dbf"));
 					if (name.equals(dbfName)) {
 						IEditableSource source = ((Table) windows[i])
 								.getModel().getModelo();
 						found = true;
 						AlphanumericNavTable navTable;
 						try {
 							navTable = new AlphanumericNavTable(source, dbfName);
 
 							if (navTable.init()) {
 								int selected = refTable.getSelectedRow();
 								ArrayList<String> where = new ArrayList<String>();
 								TableModel model = refTable.getModel();
 								for (int j = 0; j < model.getColumnCount(); j++) {
									where.add(model.getValueAt(selected, j)
											.toString());
 								}
 								try {
 									navTable
 											.setPosition(doFilter(source, where));
 								} catch (Exception e) {
 									e.printStackTrace();
 								}
 								PluginServices.getMDIManager()
 										.addCentredWindow(navTable);
 								JInternalFrame parent = (JInternalFrame) navTable
 										.getRootPane().getParent();
 								parent.addInternalFrameListener(this);
 							}
 						} catch (ReadDriverException e) {
 							e.printStackTrace();
 						}
 						break;
 					}
 				}
 			}
 		}
 		if (!found) {
 			JOptionPane.showMessageDialog(this, "La tabla \"" + dbfName
 					+ "\" no esta cargada");
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent event) {
 
 		if ((event.getSource() == orgBaseTable) && (event.getClickCount() == 2)) {
 			displayNavTable(orgBaseTable, "organizacion_base");
 		} else if ((event.getSource() == presInstTable)
 				&& (event.getClickCount() == 2)) {
 			displayNavTable(presInstTable, "presencia_institucional");
 		}
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 
 	}
 
 	@Override
 	public void internalFrameActivated(InternalFrameEvent e) {
 
 	}
 
 	@Override
 	public void internalFrameClosed(InternalFrameEvent e) {
 
 	}
 
 	@Override
 	public void internalFrameClosing(InternalFrameEvent e) {
 
 	}
 
 	@Override
 	public void internalFrameDeactivated(InternalFrameEvent e) {
 
 		fillJTable(orgBaseTable, "organizacion_base");
 		fillJTable(presInstTable, "presencia_institucional");
 
 	}
 
 	@Override
 	public void internalFrameDeiconified(InternalFrameEvent e) {
 
 	}
 
 	@Override
 	public void internalFrameIconified(InternalFrameEvent e) {
 
 	}
 
 	@Override
 	public void internalFrameOpened(InternalFrameEvent e) {
 
 	}
 
 }
