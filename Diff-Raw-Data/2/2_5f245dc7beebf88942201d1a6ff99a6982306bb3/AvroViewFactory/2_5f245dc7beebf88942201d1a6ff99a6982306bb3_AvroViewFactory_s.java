 package interdroid.vdb.avro.view;
 
 import interdroid.util.view.DraggableListView;
 import interdroid.vdb.R;
 import interdroid.vdb.avro.control.handler.ArrayHandler;
 import interdroid.vdb.avro.control.handler.ArrayValueHandler;
 import interdroid.vdb.avro.control.handler.CameraHandler;
 import interdroid.vdb.avro.control.handler.CheckboxHandler;
 import interdroid.vdb.avro.control.handler.DateHandler;
 import interdroid.vdb.avro.control.handler.EditTextHandler;
 import interdroid.vdb.avro.control.handler.EnumHandler;
 import interdroid.vdb.avro.control.handler.RecordTypeSelectHandler;
 import interdroid.vdb.avro.control.handler.RecordValueHandler;
 import interdroid.vdb.avro.control.handler.TimeHandler;
 import interdroid.vdb.avro.control.handler.UnionHandler;
 import interdroid.vdb.avro.control.handler.ValueHandler;
 import interdroid.vdb.avro.model.AvroRecordModel;
 import interdroid.vdb.avro.model.NotBoundException;
 import interdroid.vdb.avro.model.UriRecord;
 import interdroid.vdb.avro.model.UriArray;
 import interdroid.vdb.avro.model.UriUnion;
 import interdroid.vdb.content.EntityUriBuilder;
 import interdroid.vdb.content.EntityUriMatcher;
 import interdroid.vdb.content.EntityUriMatcher.UriMatch;
 
 import java.text.BreakIterator;
 
 import org.apache.avro.Schema;
 import org.apache.avro.Schema.Field;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.net.Uri;
 import android.text.InputType;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.AbsListView.LayoutParams;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.ScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 public class AvroViewFactory {
 	private static final Logger logger = LoggerFactory.getLogger(AvroViewFactory.class);
 
 	private static final int LEFT_INDENT = 3;
 
 	private static LayoutInflater getLayoutInflater(Activity activity) {
 		return (LayoutInflater) activity.getApplicationContext()
 		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	public static void buildRootView(final AvroBaseEditor activity, AvroRecordModel dataModel) throws NotBoundException {
 		logger.debug("Constructing root view: " + dataModel.schema());
 		ViewGroup viewGroup = (ViewGroup) getLayoutInflater(activity).inflate(
 				R.layout.avro_base_editor, null);
 		final ScrollView scroll = new ScrollView(activity);
 		scroll.setId(Integer.MAX_VALUE);
 		scroll.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 		scroll.addView(viewGroup);
 		activity.runOnUiThread(new Runnable() {public void run() {activity.setContentView(scroll);}});
 		buildRecordView(true, activity, dataModel, dataModel.getCurrentModel(), viewGroup);
 	}
 
 	public static View buildRecordView(boolean isRoot, AvroBaseEditor activity, AvroRecordModel dataModel, UriRecord record, ViewGroup viewGroup) throws NotBoundException {
 		logger.debug("Building record view: {}", isRoot);
 
 		// Construct a view for each field
 		for (Field field : record.getSchema().getFields()) {
 			logger.debug("Building view for: " + field.name() + " in: " + record.getSchema() + " schema:" + field.schema());
 			buildFieldView(isRoot, activity, dataModel, record, viewGroup, field);
 		}
 
 		return viewGroup;
 	}
 
 	private static View buildView(boolean isRoot, AvroBaseEditor activity, AvroRecordModel dataModel, final ViewGroup viewGroup, Schema schema, String field, Uri uri, ValueHandler valueHandler) throws NotBoundException {
 		View view;
 		switch (schema.getType()) {
 		case ARRAY:
 			logger.debug("Building array view of: {} {}", schema.getName(), schema.getElementType());
 			view = buildArrayList(activity, dataModel, viewGroup, schema.getElementType(), field, getArray(uri, valueHandler, schema));
 			break;
 		case BOOLEAN:
 			logger.debug("Building checkbox {}", schema.getName());
 			view = buildCheckbox(activity, viewGroup, new CheckboxHandler(dataModel, valueHandler));
 			break;
 		case BYTES:
 			if (schema.getProp("ui.widget") != null) {
 				if (schema.getProp("ui.widget").equals("photo")) {
 					view = buildCameraView(activity, viewGroup, new CameraHandler(dataModel, activity, schema, valueHandler, false), false);
 				} else if (schema.getProp("ui.widget").equals("video")) {
 					view = buildCameraView(activity, viewGroup, new CameraHandler(dataModel, activity, schema, valueHandler, true), true);
 				} else {
 					throw new RuntimeException("Unknown widget type: " + schema.getProp("ui.widget"));
 				}
 			} else {
 				// TODO: What to do about generic Bytes types?
 				view = null;
 			}
 			break;
 		case ENUM:
 			logger.debug("Building enum {}", schema.getName());
 			view = buildEnum(activity, dataModel, viewGroup, schema, valueHandler);
 			break;
 		case FIXED:
 			// TODO:
 			view = null;
 			break;
 		case DOUBLE:
 		case FLOAT:
 		{
 			logger.debug("Building float/double {}", schema.getName());
 
 			view = buildEditText(activity, viewGroup, schema,
 					InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED,
 					new EditTextHandler(dataModel, schema.getType(), valueHandler)
 			);
 			break;
 		}
 		case INT:
 		case LONG:
 		{
 			logger.debug("Building int/long: {}", schema.getName());
 
 			if (schema.getProp("ui.widget") != null) {
 				logger.debug("Building custom ui widget for long/int");
 				if (schema.getProp("ui.widget").equals("date")) {
 					logger.debug("Building date view.");
 					view = buildDateView(activity, viewGroup, valueHandler);
 				} else if (schema.getProp("ui.widget").equals("time")) {
 					logger.debug("Building time view.");
 					view = buildTimeView(activity, viewGroup, valueHandler);
 				} else {
 					throw new RuntimeException("Unknown widget type: " + schema.getProp("ui.widget"));
 				}
 			} else {
 				view = buildEditText(activity, viewGroup, schema,
 						InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED,
 						new EditTextHandler(dataModel, schema.getType(), valueHandler)
 				);
 			}
 			break;
 		}
 		case MAP:
 			// TODO:
 			view = buildTextView(activity, viewGroup, R.string.not_implemented);
 			break;
 		case NULL:
 		{
 			logger.debug("Building null view: {}", schema.getName());
 			view = buildTextView(activity, viewGroup, R.string.null_text);
 			break;
 		}
 		case RECORD:
 			logger.debug("Building record view: {} {}", schema.getName(), isRoot);
 			if (isRoot) {
 				view = buildRecordView(false, activity, dataModel, getRecord(activity, valueHandler, uri, schema), viewGroup);
 			} else {
 				final Button button = new Button(activity);
 				UriRecord record = (UriRecord) valueHandler.getValue();
 				if (record == null) {
 					button.setText(activity.getString(R.string.label_create) + " " + toTitle(schema));
 				} else {
 					button.setText(activity.getString(R.string.label_edit) + " " + toTitle(schema));
 				}
 				button.setOnClickListener(getRecordTypeSelectorHandler(activity, dataModel, schema, valueHandler, viewGroup, button));
 				addView(activity, viewGroup, button);
 				view = button;
 			}
 			break;
 		case STRING:
 		{
 			logger.debug("Building string view: {}", schema.getName());
 			view = buildEditText(activity, viewGroup, schema, InputType.TYPE_CLASS_TEXT,
 					new EditTextHandler(dataModel, schema.getType(), valueHandler));
 			break;
 		}
 		case UNION:
 		{
 			logger.debug("Building union view: {}", schema.getName());
 			view = buildUnion(activity, dataModel, viewGroup, schema, field, uri, new UnionHandler(dataModel, valueHandler,
 					getUnion(uri, valueHandler, schema)));
 			break;
 		}
 		default:
 			throw new RuntimeException("Unsupported type: " + schema);
 		}
 
 		if (schema.getProp("ui.visible") != null) {
 			logger.debug("Hiding view.");
 			view.setVisibility(View.GONE);
 		}
 		if (schema.getProp("ui.enabled") != null) {
 			logger.debug("Disabling view.");
 			view.setEnabled(false);
 		}
 
 		return view;
 	}
 
 	private static View buildCameraView(AvroBaseEditor activity,
 			ViewGroup viewGroup, CameraHandler cameraHandler, boolean isVideo) {
 		LinearLayout layout = new LinearLayout(activity);
 		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 		layout.setOrientation(LinearLayout.VERTICAL);
 
 		ImageView image = new ImageView(activity);
 		layout.addView(image);
 
 		Button cameraButton = new Button(activity);
 		if (!isVideo) {
 			cameraButton.setText(activity.getString(R.string.label_take_photo));
 		} else {
 			cameraButton.setText(activity.getString(R.string.label_take_video));
 		}
 		layout.addView(cameraButton);
 
 		addView(activity, viewGroup, layout);
 		cameraHandler.setButton(cameraButton);
 		cameraHandler.setImageView(image);
 
 		return layout;
 	}
 
 	private static void addView(Activity activity, final ViewGroup viewGroup, final View view) {
 		if (viewGroup != null) {
 			activity.runOnUiThread(new Runnable() {public void run(){viewGroup.addView(view);}});
 		}
 	}
 
 	private static View buildDateView(AvroBaseEditor activity,
 			ViewGroup viewGroup, ValueHandler valueHandler) {
 		DatePicker view;
 		view = new DatePicker(activity);
 		DateHandler handler = new DateHandler(view, valueHandler);
 		view.setOnClickListener(handler);
 		addView(activity, viewGroup, view);
 		return view;
 	}
 
 	private static View buildTimeView(AvroBaseEditor activity,
 			ViewGroup viewGroup, ValueHandler valueHandler) {
 		TimePicker view;
 		view = new TimePicker(activity);
 		TimeHandler handler = new TimeHandler(view, valueHandler);
 		view.setOnClickListener(handler);
 		addView(activity, viewGroup, view);
 		return view;
 	}
 
 	private static OnClickListener getRecordTypeSelectorHandler(AvroBaseEditor activity, AvroRecordModel dataModel,
 			Schema schema, ValueHandler valueHandler, ViewGroup container, Button button) {
 		return new RecordTypeSelectHandler(activity, dataModel, schema, valueHandler, button);
 	}
 
 	private static UriRecord getRecord(Activity activity, ValueHandler valueHandler, Uri uri, Schema schema) {
 		UriRecord subRecord = (UriRecord) valueHandler.getValue();
 		if (subRecord == null) {
 			UriMatch match = EntityUriMatcher.getMatch(uri);
 			uri = Uri.withAppendedPath(EntityUriBuilder.branchUri(match.authority, match.repositoryName, match.reference), schema.getName());
 			uri = activity.getContentResolver().insert(uri, new ContentValues());
 			subRecord = new UriRecord(uri, schema);
 			valueHandler.setValue(subRecord);
 		}
 		return subRecord;
 	}
 
 	private static UriArray<Object> getArray(Uri uri, ValueHandler valueHandler, Schema schema) throws NotBoundException {
 		@SuppressWarnings("unchecked")
 		UriArray<Object> array = (UriArray<Object>)valueHandler.getValue();
 		if (array == null) {
			array = new UriArray<Object>(valueHandler.getValueUri(), schema);
 			valueHandler.setValue(array);
 		}
 		return array;
 	}
 
 	private static UriUnion getUnion(Uri uri, ValueHandler valueHandler, Schema schema) {
 		UriUnion value = (UriUnion)valueHandler.getValue();
 		if (value == null) {
 			value = new UriUnion(schema);
 		}
 		return value;
 	}
 
 	private static View buildEnum(AvroBaseEditor activity, AvroRecordModel dataModel,
 			ViewGroup viewGroup, Schema schema, ValueHandler valueHandler) {
 		Button selectedText = new Button(activity);
 		addView(activity, viewGroup, selectedText);
 		new EnumHandler(activity, dataModel, schema, selectedText, valueHandler);
 		return selectedText;
 	}
 
 	private static View buildFieldView(boolean isRoot, AvroBaseEditor activity, AvroRecordModel dataModel, UriRecord record, ViewGroup viewGroup, Field field) throws NotBoundException {
 		//		LinearLayout layout = new LinearLayout(activity);
 		//		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 		//		layout.setOrientation(LinearLayout.VERTICAL);
 
 		// TODO: Add field comment as pressed text on field
 		if (field.getProp("ui.visible") == null) {
 			TextView label = new TextView(activity);
 			label.setText(toTitle(field));
 			label.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
 					LayoutParams.WRAP_CONTENT));
 			label.setGravity(Gravity.LEFT);
 			addView(activity, viewGroup, label);
 		}
 
 		buildView(isRoot, activity, dataModel, viewGroup, field.schema(), field.name(), record.getInstanceUri(), new RecordValueHandler(dataModel, record, field.name()));
 
 		//		if (viewGroup != null)
 		//			viewGroup.addView(layout);
 
 		return viewGroup;
 	}
 
 	public static View buildArrayView(boolean isRoot, AvroBaseEditor activity, AvroRecordModel dataModel, UriArray<Object> array, Schema elementSchema, String field, Uri uri, int offset) throws NotBoundException {
 		View layout = LayoutInflater.from(activity).inflate(R.layout.avro_array_item, null);
 		buildView(isRoot, activity, dataModel, (ViewGroup)layout.findViewById(R.id.array_layout), elementSchema, field, uri, new ArrayValueHandler(dataModel, field, array, offset));
 		return layout;
 	}
 
 	private static View buildUnion(AvroBaseEditor activity, AvroRecordModel dataModel, ViewGroup viewGroup, Schema schema, String field, Uri uri,
 			UnionHandler handler) throws NotBoundException {
 		TableLayout table = new TableLayout(activity);
 		for (Schema innerType : schema.getTypes()) {
 			TableRow row = new TableRow(activity);
 
 			RadioButton radioButton = new RadioButton(activity);
 			radioButton.setFocusableInTouchMode(false);
 
 			row.addView(radioButton);
 
 			handler.addType(radioButton, innerType,
 					buildView(false, activity, dataModel, row, innerType, field, uri, handler.getHandler(radioButton, innerType)));
 
 			table.addView(row);
 		}
 		addView(activity, viewGroup, table);
 		return table;
 	}
 
 	private static View buildArrayList(AvroBaseEditor activity, AvroRecordModel dataModel, ViewGroup viewGroup, Schema schema, String field, UriArray<Object> array) {
 		DraggableListView layout = new DraggableListView(activity);
 		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 		layout.setPadding(LEFT_INDENT, 0, 0, 0);
 
 		ArrayHandler adapter = new ArrayHandler(activity, dataModel, layout, array, schema, field);
 		layout.setAdapter(adapter);
 		layout.setAddListener(adapter);
 
 		addView(activity, viewGroup, layout);
 
 		return layout;
 	}
 
 	private static View buildCheckbox(AvroBaseEditor activity, ViewGroup viewGroup, CheckboxHandler changeListener) {
 		CheckBox text = new CheckBox(activity);
 		text.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 		text.setGravity(Gravity.FILL_HORIZONTAL);
 		changeListener.setWatched(text);
 		addView(activity, viewGroup, text);
 
 		return text;
 	}
 
 	private static View buildEditText(AvroBaseEditor activity, ViewGroup viewGroup, Schema schema, int inputType, EditTextHandler textWatcher) {
 		logger.debug("Building edit text for: " + schema);
 		EditText text = null;
 		if (schema.getProp("ui.resource") != null) {
 			logger.debug("Inflating custom resource: " + schema.getProp("ui.resource"));
 			try {
 				LayoutInflater inflater = (LayoutInflater)activity.getSystemService(
 						Context.LAYOUT_INFLATER_SERVICE);
 				text = (EditText) inflater.inflate(Integer.valueOf(schema.getProp("ui.resource")), null);
 			} catch (Exception e) {
 				logger.error("Unable to inflate resource: " + schema.getProp("ui.resource"));
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				throw new RuntimeException("Unable to inflate UI resource: " + schema.getProp("ui.resource"), e);
 			}
 		} else {
 			text = new EditText(activity);
 			text.setLayoutParams(new LayoutParams(
 					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 			text.setGravity(Gravity.FILL_HORIZONTAL);
 			text.setInputType(inputType);
 		}
 		addView(activity, viewGroup, text);
 		textWatcher.setWatched(text);
 		return text;
 	}
 
 	private static View buildTextView(AvroBaseEditor activity, ViewGroup viewGroup, int textId) {
 		TextView text = new TextView(activity);
 		text.setText(textId);
 		addView(activity, viewGroup, text);
 		return text;
 	}
 
 	public static String toTitle(Schema schema) {
 		return toTitle(schema.getProp("ui.label"), schema.getName(), false);
 	}
 
 	public static String toTitle(Field theField) {
 		return toTitle(theField.getProp("ui.label"), theField.name(), true);
 	}
 
 	private static String toTitle(String label, String name, boolean includeColon) {
 
 		StringBuffer sb = new StringBuffer();
 		if (label != null) {
 			sb.append(label);
 		} else {
 			String field = name.toLowerCase().replace('_', ' ');
 			BreakIterator boundary = BreakIterator.getWordInstance();
 			boundary.setText(field);
 			boolean first = true;
 			int start = boundary.first();
 			for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary
 					.next()) {
 				if (first) {
 					first = false;
 				} else {
 					sb.append(" ");
 				}
 				sb.append(field.substring(start, start + 1).toUpperCase());
 				sb.append(field.substring(start + 1, end));
 			}
 		}
 		if (includeColon) {
 			sb.append(":");
 		}
 		return sb.toString();
 	}
 
 }
