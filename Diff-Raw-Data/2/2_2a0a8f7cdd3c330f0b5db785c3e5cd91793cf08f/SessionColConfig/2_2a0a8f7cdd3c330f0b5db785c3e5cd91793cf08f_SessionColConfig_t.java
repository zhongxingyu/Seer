 package edu.nrao.dss.client.widget.explorers;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.DateField;
 import com.extjs.gxt.ui.client.widget.form.Field;
 import com.extjs.gxt.ui.client.widget.form.NumberField;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.grid.CellEditor;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 
 import edu.nrao.dss.client.util.Conversions;
 import edu.nrao.dss.client.util.JSONCallbackAdapter;
 import edu.nrao.dss.client.util.JSONRequestCache;
 import edu.nrao.dss.client.widget.form.CoordModeField;
 import edu.nrao.dss.client.widget.form.DateEditField;
 import edu.nrao.dss.client.widget.form.DegreeField;
 import edu.nrao.dss.client.widget.form.GradeField;
 import edu.nrao.dss.client.widget.form.HourField;
 import edu.nrao.dss.client.widget.form.PCodeField;
 import edu.nrao.dss.client.widget.form.STypeField;
 import edu.nrao.dss.client.widget.form.ScienceField;
 
 //Should extend an intermediate class for duplicate code
 //between SessionColConfig and PeriodColConfig
 public class SessionColConfig extends ColumnConfig {
 
 	@SuppressWarnings("unchecked")
 	public SessionColConfig(String fName, String name, int width, Boolean disabled, Class clasz) {
 		super(fName, name, width);
 		
 		this.clasz = clasz;
 
 		if (clasz == Integer.class) {
 			intField();
 		} else if (clasz == Double.class) {
 			doubleField();
 		} else if (clasz == Boolean.class) {
 			checkboxField();
 		} else if (clasz == CoordModeField.class) {
 			typeField(CoordModeField.values);
 		} else if (clasz == DateEditField.class) {
 			dateField();
 		} else if (clasz == DegreeField.class) {
 			degreeField();
 		} else if (clasz == GradeField.class) {
 			typeField(GradeField.values);
 		} else if (clasz == HourField.class) {
 			hourField();
 		} else if (clasz == ScienceField.class) {
 			typeField(ScienceField.values);
 		} else if (clasz == STypeField.class) {
 			typeField(STypeField.values);
 		} else if (clasz == PCodeField.class) {
 			setPCodeOptions();
 		} else {
 			textField(disabled);
 		}
 	};
 
 	@SuppressWarnings("serial")
 	public void setPCodeOptions() {
 		JSONRequestCache.get("/scheduler/sessions/options"
 				, new HashMap<String, Object>() {{
 	    	  put("mode", "project_codes");
         }}
 		, new JSONCallbackAdapter() {
 			@Override
 			public void onSuccess(JSONObject json) {
 				ArrayList<String> proj_codes = new ArrayList<String>();
 				JSONArray pcodes = json.get("project codes").isArray();
 				for (int i = 0; i < pcodes.size(); ++i){
 					proj_codes.add(pcodes.get(i).toString().replace('"', ' ').trim());
 				}
 				typeField(proj_codes.toArray(new String[] {}));
 			}
     	});
 	}
 	
 	public void updatePCodeOptions() {
 		JSONRequestCache.get("/scheduler/sessions/options"
 				, new HashMap<String, Object>() {{
 			    	  put("mode", "project_codes");
 			        }}
 				, new JSONCallbackAdapter() {
 		//JSONRequest.get("/scheduler/sessions/options", new JSONCallbackAdapter() {
 			@SuppressWarnings("unchecked")
 			@Override
 			public void onSuccess(JSONObject json) {
 				SimpleComboBox<String> typeCombo = (SimpleComboBox<String>) getEditor().getField();
 				typeCombo.removeAll();
 				JSONArray pcodes = json.get("project codes").isArray();
 				for (int i = 0; i < pcodes.size(); ++i){
 					typeCombo.add(pcodes.get(i).toString().replace('"', ' ').trim());
 				}
 			}
     	});
 	}
 	
 	private NumberField createDoubleField() {
 		NumberField field = new NumberField();
 		field.setPropertyEditorType(Double.class);
 		return field;
 	}
 
 	private void doubleField() {
 		NumberField field = createDoubleField();
 
 		setAlignment(HorizontalAlignment.RIGHT);
 		setEditor(new CellEditor(field));
 
 		setNumberFormat(NumberFormat.getFormat("0"));
 		setRenderer(new GridCellRenderer<BaseModelData>() {
 			public Object render(BaseModelData model, String property,
 					ColumnData config, int rowIndex, int colIndex,
 					ListStore<BaseModelData> store, Grid<BaseModelData> grid) {
 				if (model.get(property) != null) {
 					return model.get(property).toString();
 				} else {
 					return "";
 				}
 			}
 		});
 	}
 
 	private NumberField createIntegerField() {
 		NumberField field = new NumberField();
 		field.setPropertyEditorType(Integer.class);
 		return field;
 	}
 
 	private void intField() {
 		NumberField field = createIntegerField();
 
 		setAlignment(HorizontalAlignment.RIGHT);
 		setEditor(new CellEditor(field) {
 			@Override
 			public Object preProcessValue(Object value) {
 				if (value == null) {
 					return null;
 				}
 				return Integer.parseInt(value.toString());
 			}
 
 			@Override
 			public Object postProcessValue(Object value) {
 				if (value == null) {
 					return null;
 				}
 				return value.toString();
 			}
 		});
 		setNumberFormat(NumberFormat.getFormat("0"));
 		setRenderer(new GridCellRenderer<BaseModelData>() {
 			public Object render(BaseModelData model, String property,
 					ColumnData config, int rowIndex, int colIndex,
 					ListStore<BaseModelData> store, Grid<BaseModelData> grid) {
 				if (model.get(property) != null) {
 					return model.get(property).toString();
 				} else {
 					return "";
 				}
 			}
 		});
 	}
 
 	private Field<Boolean> createCheckboxField() {
 		return new CheckBox();
 	}
 
 	private void checkboxField() {
 		setEditor(new CellEditor(new CheckBox()));
 	}
 	
 	private void dateField() {
 		setEditor(new CellEditor(new DateField()){
 			@Override
 			public Object preProcessValue(Object value) {
 				if (value == null) {
 					return null;
 				}
 				//return DateFormat.getDateInstance().parse(value.toString());
 				String str = value.toString();
 				DateTimeFormat fmt = DateTimeFormat.getFormat("MM/dd/yyyy");
 				return fmt.parse(str);
 			}
 
 			@Override
 			public Object postProcessValue(Object value) {
 				if (value == null) {
 					return null;
 				}
 				DateTimeFormat fmt = DateTimeFormat.getFormat("MM/dd/yyyy");
 				Date d = (Date) value;
 				return fmt.format(d);
 			}
 		});
 	}
 	
 	private TextField<String> createTextField() {
 		TextField<String> field = new TextField<String>();
 		return field;
 	}
 
 	/** Construct an editable field supporting free-form text. */
 	private void textField(Boolean disabled) {
 		CellEditor editor = new CellEditor(new TextField<String>());
 		if (disabled) {
 			editor.disable();
 		}
 		setEditor(editor);
 	}
 
 	private void hourField() {
 		TextField<String> positionField = new TextField<String>();
 		positionField.setRegex("[0-2]\\d:[0-5]\\d:[0-5]\\d(\\.\\d+)?");
 
 		setAlignment(HorizontalAlignment.RIGHT);
 
 		setRenderer(new GridCellRenderer<BaseModelData>() {
 			public Object render(BaseModelData model, String property,
 					ColumnData config, int rowIndex, int colIndex,
 					ListStore<BaseModelData> store, Grid<BaseModelData> grid) {
 				Object val = model.get(property);
 				if (val != null) {
 					return Conversions.radiansToTime(((Double) val).doubleValue());
 				} else {
 					// display a blank string here in place of "00:00:00" so users no it is null
 					return ""; 
 				}
 			}
 		});
 
 		setEditor(new CellEditor(positionField) {
 			@Override
 			public Object preProcessValue(Object value) {
 				if (value == null) {
 					return Conversions.radiansToTime(0.0);
 				}
 				return Conversions.radiansToTime(((Double) value).doubleValue());
 			}
 
 			@Override
 			public Object postProcessValue(Object value) {
 				if (value == null) {
 					return 0.0;
 				}
 				return Conversions.timeToRadians(value.toString());
 			}
 		});
 	}
 
 	private void degreeField() {
 		TextField<String> degreeField = new TextField<String>();
 		degreeField.setRegex("-?\\d\\d:\\d\\d:\\d\\d(\\.\\d+)?");
 
 		setAlignment(HorizontalAlignment.RIGHT);
 
 		setRenderer(new GridCellRenderer<BaseModelData>() {
 			public Object render(BaseModelData model, String property,
 					ColumnData config, int rowIndex, int colIndex,
 					ListStore<BaseModelData> store, Grid<BaseModelData> grid) {
 				Object val = model.get(property);
 				if (val != null) {
 					return Conversions.radiansToSexagesimal(((Double) val).doubleValue());
 				} else {
 					// display a blank string here in place of "00:00:00" so users no it is null
 					return ""; 
 				}
 			}
 		});
 
 		setEditor(new CellEditor(degreeField) {
 			@Override
 			public Object preProcessValue(Object value) {
 				if (value == null) {
 					return Conversions.radiansToSexagesimal(0.0);
 				}
 				return Conversions.radiansToSexagesimal(((Double) value).doubleValue());
 			}
 
 			@Override
 			public Object postProcessValue(Object value) {
 				if (value == null) {
 					return 0.0;
 				}
 				return Conversions.sexagesimalToRadians(value.toString());
 			}
 		});
 	}
 
	// Note: this allows entries outside list of options
 	private SimpleComboBox<String> createSimpleComboBox(String[] options) {
 		SimpleComboBox<String> typeCombo = new SimpleComboBox<String>();
 		typeCombo.setTriggerAction(TriggerAction.ALL);
 
 		for (String o : options) {
 			typeCombo.add(o);
 		}
 
 		return typeCombo;
 	}
 	
 	private void typeField(String[] options) {
 		final SimpleComboBox<String> typeCombo = createSimpleComboBox(options);
 
 		setEditor(new CellEditor(typeCombo) {
 			@Override
 			public Object preProcessValue(Object value) {
 				if (value == null) {
 					return value;
 				}
 				return typeCombo.findModel(value.toString());
 			}
 
 			@Override
 			public Object postProcessValue(Object value) {
 				if (value == null) {
 					return value;
 				}
 				return ((ModelData) value).get("value");
 			}
 		});
 	}
 
 	@SuppressWarnings("unchecked")
 	protected final Class clasz;
 }
