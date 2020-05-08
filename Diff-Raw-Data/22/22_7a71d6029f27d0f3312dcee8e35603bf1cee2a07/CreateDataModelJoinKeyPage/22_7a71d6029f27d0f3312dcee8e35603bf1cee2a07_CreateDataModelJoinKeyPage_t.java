 package jp.hishidama.xtext.dmdl_editor.ui.wizard.page;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import jp.hishidama.eclipse_plugin.util.StringUtil;
 import jp.hishidama.xtext.dmdl_editor.dmdl.ModelDefinition;
 import jp.hishidama.xtext.dmdl_editor.dmdl.Property;
 import jp.hishidama.xtext.dmdl_editor.ui.viewer.DMDLTreeData;
 import jp.hishidama.xtext.dmdl_editor.ui.wizard.page.CreateDataModelJoinPage.JoinKey;
 
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 class DataModelJoinKey extends DataModelRow {
 	private Table table;
 	private Map<String, String[]> comboMap;
 	private Map<String, String> descMap;
 
 	public DataModelJoinKey(Table table, Map<String, String[]> comboMap, Map<String, String> descMap) {
 		this.table = table;
 		this.comboMap = comboMap;
 		this.descMap = descMap;
 	}
 
 	private Map<String, String> keys = new HashMap<String, String>();
 
 	public void set(String property, String value) {
 		keys.put(property, value);
 	}
 
 	public String get(String property) {
 		return keys.get(property);
 	}
 
 	public int getColumns() {
 		return table.getColumnCount();
 	}
 
 	public String getModelName(int columnIndex) {
 		TableColumn c = table.getColumn(columnIndex);
 		return c.getText();
 	}
 
 	public String getModelDescription(int columnIndex) {
 		String name = getModelName(columnIndex);
 		return descMap.get(name);
 	}
 
 	@Override
 	public String getText(int columnIndex) {
 		String name = getModelName(columnIndex);
 		return keys.get(name);
 	}
 
 	public String[] getComboList(String property) {
 		return comboMap.get(property);
 	}
 
 	@Override
 	public String validate() {
 		for (TableColumn c : table.getColumns()) {
 			String name = c.getText();
 			String key = keys.get(name);
 			if (StringUtil.isEmpty(key)) {
 				return "キーは必須です。";
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public String getName() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public String getDescription() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public String getDataType() {
 		throw new UnsupportedOperationException();
 	}
 }
 
 public class CreateDataModelJoinKeyPage extends CreateDataModelPage<DataModelJoinKey> {
 
 	private List<DMDLTreeData> input;
 	private List<JoinKey> keyBuffer;
 	private Map<String, String[]> comboMap = new HashMap<String, String[]>();
 	private Map<String, String> descMap = new HashMap<String, String>();
 
 	public CreateDataModelJoinKeyPage() {
 		super("CreateDataModelJoinKeyPage", "結合キーの指定",
 				"結合データモデルのキーを定義して下さい。\n（結合元データモデルのそれぞれのプロパティーを同時に選択してcopyボタンを押すと定義が作れます）");
 	}
 
 	@Override
 	protected String getSourceLabelText() {
 		return "結合元データモデル";
 	}
 
 	@Override
 	protected String getTargetLabelText() {
 		return "結合キー定義";
 	}
 
 	@Override
 	protected void defineColumns(Table table) {
 		if (input == null) {
 			addColumn("key1", 256);
 			addColumn("key2", 256);
 		} else {
 			for (DMDLTreeData data : input) {
 				ModelDefinition model = (ModelDefinition) data.getData();
 				String modelName = model.getName();
 
 				List<DMDLTreeData> children = data.getChildren();
 				if (children == null) {
 					children = Collections.emptyList();
 				}
 				String[] combo = new String[children.size()];
 				int i = 0;
 				for (DMDLTreeData c : children) {
 					Property prop = (Property) c.getData();
 					String name = prop.getName();
 					String desc = prop.getDescription();
 					combo[i++] = StringUtil.isEmpty(desc) ? name : name + " " + desc;
 				}
 
 				addColumn(modelName, 128);
 
 				comboMap.put(modelName, combo);
 				descMap.put(modelName, model.getDescription());
 			}
 		}
 	}
 
 	public void setSourceList(List<DMDLTreeData> input, List<JoinKey> keys) {
 		this.input = input;
 		this.keyBuffer = keys;
 		if (sourceViewer != null) {
 			setInput();
 		}
 	}
 
 	@Override
 	protected void setInput() {
 		Table table = tableViewer.getTable();
 		for (TableColumn c : table.getColumns()) {
 			c.dispose();
 		}
 		defineColumns(table);
 
 		sourceViewer.setInputList(input);
 		sourceViewer.expandAll();
 
 		tableViewer.refresh();
 		validate(false);
 	}
 
 	@Override
 	protected void doVisible(boolean visible) {
 		if (visible) {
 			rebuildTable();
 		}
 	}
 
 	public void rebuildTable() {
 		Table table = tableViewer.getTable();
 		TableColumn[] cols = table.getColumns();
 		for (TableItem item : table.getItems()) {
 			JoinKey key = new JoinKey(0);
 			for (int i = 0; i < cols.length; i++) {
 				String mname = cols[i].getText();
 				String pname = item.getText(i);
 				key.add(mname, pname);
 			}
 			keyBuffer.remove(key);
 		}
 		int index = table.getItemCount();
 		for (JoinKey key : keyBuffer) {
 			DataModelJoinKey row = newAddRow();
 			for (Entry<String, String> entry : key.map.entrySet()) {
 				String mname = entry.getKey();
 				String pname = entry.getValue();
 				row.set(mname, pname);
 			}
 			index = addToList(index, row);
 		}
 		tableViewer.refresh();
 	}
 
 	@Override
 	protected DataModelJoinKey newAddRow() {
 		return new DataModelJoinKey(tableViewer.getTable(), comboMap, descMap);
 	}
 
 	@Override
 	protected boolean doEditDialog(DataModelJoinKey row) {
 		EditJoinKeyPropertyDialog dialog = new EditJoinKeyPropertyDialog(getShell(), row);
 		return dialog.open() == Window.OK;
 	}
 
 	@Override
 	protected String getCopyToolTipText() {
 		return "データモデルのプロパティーを結合キーにします。\n" + "例えばp1とp2をコピーすると、\n" + "joined model = src -> { ～ } % p1, p2\n" + "となります。";
 	}
 
 	@Override
 	protected boolean enableCopy(ModelDefinition model, Property prop) {
 		return prop != null;
 	}
 
 	protected void doCopy(int index, Iterator<DMDLTreeData> iterator) {
 		DataModelJoinKey row = null;
 
 		for (Iterator<DMDLTreeData> i = iterator; i.hasNext();) {
 			DMDLTreeData data = i.next();
 			Object obj = data.getData();
 			if (obj instanceof Property) {
 				ModelDefinition model = (ModelDefinition) data.getParent().getData();
 				Property p = (Property) obj;
 				String modelName = model.getName();
 				if (row == null) {
 					row = newCopyRow(null, null);
 				}
 				row.set(modelName, p.getName());
 			}
 		}
 
 		if (row != null) {
			boolean replace = false;

			DataModelJoinKey now = (0 <= index && index < defineList.size()) ? defineList.get(index) : null;
			if (now != null) {
				int size = now.getColumns();
				for (int i = 0; i < size; i++) {
					String key = now.getModelName(i);
					String nowValue = now.get(key);
					if (nowValue == null) {
						String newValue = row.get(key);
						if (newValue != null) {
							now.set(key, newValue);
							replace = true;
						}
					}
				}
			}

			if (!replace) {
				addToList(index, row);
			}
 		}
 	}
 
 	@Override
 	protected DataModelJoinKey newCopyRow(ModelDefinition model, Property prop) {
 		return new DataModelJoinKey(tableViewer.getTable(), comboMap, descMap);
 	}
 
 	@Override
 	protected DataModelJoinKey newDefCopyRow(ModelDefinition model, Property prop, boolean copyAttribute) {
 		return newCopyRow(model, prop);
 	}
 
 	@Override
 	protected boolean visibleReference() {
 		return false;
 	}
 
 	@Override
 	protected String getDefineEmptyMessage() {
 		return null;
 	}
 
 	@Override
 	protected String getModelType() {
 		return "joined";
 	}
 
 	@Override
 	protected void setGenerator(DataModelTextGenerator gen, TableItem[] items) {
 		for (DMDLTreeData data : input) {
 			ModelDefinition model = (ModelDefinition) data.getData();
 			String modelName = model.getName();
 			if ((Boolean) data.getOtherData()) {
 				List<DMDLTreeData> children = data.getChildren();
 				for (DMDLTreeData c : children) {
 					DataModelJoinRow row = (DataModelJoinRow) c.getOtherData();
 					gen.appendRefProperty(row.name, row.description, row.refModelName, row.refPropertyName,
 							row.attribute);
 				}
 			} else {
 				gen.appendRefProperty(modelName);
 			}
 			for (TableItem item : items) {
 				DataModelJoinKey row = (DataModelJoinKey) item.getData();
 				String value = row.get(modelName);
 				gen.appendKey(value, null);
 			}
 		}
 	}
 
 	@Override
 	protected void setGeneratorProperty(DataModelTextGenerator gen, DataModelJoinKey row) {
 		throw new UnsupportedOperationException();
 	}
 
 	public TableItem[] getTableItems() {
 		return tableViewer.getTable().getItems();
 	}
 }
