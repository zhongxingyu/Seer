 package com.flywet.platform.bi.di.function;
 
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.apache.log4j.Logger;
 import org.pentaho.di.core.Const;
 import org.pentaho.di.core.database.DatabaseMeta;
 import org.pentaho.di.core.row.ValueMeta;
 import org.pentaho.di.i18n.BaseMessages;
 import org.pentaho.di.trans.TransMeta;
 
 import com.flywet.platform.bi.component.components.combo.ComboBoxMeta;
 import com.flywet.platform.bi.component.components.grid.EditorObjectData;
 import com.flywet.platform.bi.component.components.grid.GridDataObject;
 import com.flywet.platform.bi.component.components.selectMenu.OptionsData;
 import com.flywet.platform.bi.component.utils.FLYFunctionMapper;
 import com.flywet.platform.bi.component.utils.HTML;
 import com.flywet.platform.bi.component.vo.NameValuePair;
 import com.flywet.platform.bi.core.exception.BIException;
 
 public class DIFunctions {
 
 	private static Class<?> PKG = DIFunctions.class;
 
 	private final static Logger logger = Logger.getLogger(DIFunctions.class);
 
 	private static String PREFIX = "di";
 
 	// valueTypes
 	private static final String OPTIONS_KEY_VALUE_TYPES = "valueTypes";
 	private static OptionsData valueTypesOptionsData = OptionsData
 			.instanceSimpleStrings(ValueMeta.getTypes());
 
 	// transStatus
 	private static final String OPTIONS_KEY_TRANS_STATUS = "transStatus";
 	private static OptionsData transStatusOptionsData = OptionsData
 			.instance(new String[] {
 					BaseMessages.getString(PKG, "Page.Option.Empty.Label"),
 					BaseMessages.getString(PKG,
 							"Page.Trans.Transstatus.Draft.Label"),
 					BaseMessages.getString(PKG,
 							"Page.Trans.Transstatus.Production.Label") });
 
 	// field type
 	private static EditorObjectData fieldType;
 
 	// field format
 	private static EditorObjectData fieldFormat;
 
 	// yes or no
 	private static EditorObjectData fieldBoolean;
 
 	private static Map<String, OptionsData> options = new ConcurrentHashMap<String, OptionsData>();
 	private static AtomicBoolean initCache = new AtomicBoolean(false);
 
 	static {
 		if (!initCache.getAndSet(true)) {
 			options.put(OPTIONS_KEY_VALUE_TYPES, valueTypesOptionsData);
 			options.put(OPTIONS_KEY_TRANS_STATUS, transStatusOptionsData);
 
 			initFieldType();
 
 			initFieldFormat();
 
 			initFieldBoolean();
 		}
 	}
 
 	/**
 	 * 获得列表字段布尔的编辑对象
 	 * 
 	 * @return
 	 */
 	public static EditorObjectData getFieldBoolean() {
 		return fieldBoolean;
 	}
 
 	private static void initFieldBoolean() {
 		fieldBoolean = new EditorObjectData();
 		fieldBoolean.initCheckbox("是", "否");
 	}
 
 	/**
 	 * 获得列表字段格式的编辑对象
 	 * 
 	 * @return
 	 */
 	public static EditorObjectData getFieldFormat() {
 		return fieldFormat;
 	}
 
 	private static void initFieldFormat() {
 		try {
 			fieldFormat = new EditorObjectData();
 			ComboBoxMeta cbm = new ComboBoxMeta();
 			cbm.setLocalDataWithNameValuePair(NameValuePair.instance(Const
 					.getDateFormats()));
 			fieldFormat.initCombobox(cbm);
 		} catch (BIException e) {
 			logger.error(e.getMessage());
 		}
 	}
 
 	/**
 	 * 获得列表字段类型的编辑对象
 	 * 
 	 * @return
 	 */
 	public static EditorObjectData getFieldType() {
 		return fieldType;
 	}
 
 	private static void initFieldType() {
 		try {
 			fieldType = new EditorObjectData();
 			ComboBoxMeta cbm = new ComboBoxMeta();
 			cbm.setLocalDataWithNameValuePair(NameValuePair.instance(ValueMeta
 					.getTypes()));
 			fieldType.initCombobox(cbm);
 		} catch (BIException e) {
 			logger.error(e.getMessage());
 		}
 	}
 
 	/**
 	 * 获得指定选项
 	 * 
 	 * @param key
 	 * @return
 	 */
 	public static List<String[]> getOptions(String key) {
 		return options.get(key).getOptions();
 	}
 
 	/**
 	 * 获得所有数据库连接的选项
 	 * 
 	 * @param transMeta
 	 * @return
 	 */
 	public static List<String[]> allDatabaseOptions(TransMeta transMeta) {
 		OptionsData od = OptionsData.instance();
 		DatabaseMeta dm;
 
 		for (int i = 0; i < transMeta.nrDatabases(); i++) {
 			dm = transMeta.getDatabase(i);
 			od.addOption(dm.getObjectId().getId(), dm.getName());
 		}
 
 		return od.getOptions();
 	}
 
 	/**
 	 * 创建数据表格数据集
 	 * 
 	 * @param keys
 	 * @param values
 	 * @return
 	 */
 	public static GridDataObject createDGDataSet(String[] keys,
 			Object[][] values) {
 		GridDataObject gd = GridDataObject.instance().setMinRows(
 				HTML.DEFAULT_GRID_ROW_NUMBER);
 		gd.putObjects(keys, values).transpose();
 		return gd;
 	}
 
 	public static void register() {
 		if (!FLYFunctionMapper.singleton.contains(PREFIX)) {
 			FLYFunctionMapper.singleton.register(PREFIX, DIFunctions.class);
 		}
 	}
 }
