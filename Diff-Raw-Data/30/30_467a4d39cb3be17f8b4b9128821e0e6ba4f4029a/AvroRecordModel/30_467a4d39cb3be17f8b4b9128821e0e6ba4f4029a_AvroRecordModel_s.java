 package interdroid.vdb.avro.model;
 
 import java.util.HashMap;
 
 import interdroid.vdb.content.EntityUriBuilder;
 import interdroid.vdb.content.EntityUriMatcher;
 import interdroid.vdb.content.EntityUriMatcher.UriMatch;
 import interdroid.vdb.content.GenericContentProvider;
 import interdroid.vdb.content.avro.AvroContentProvider;
 
 import org.apache.avro.Schema;
 import org.apache.avro.Schema.Field;
 import org.apache.avro.Schema.Type;
 import org.apache.avro.generic.GenericData;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.DataSetObserver;
 import android.net.Uri;
 import android.os.Bundle;
 
 /* Handy snipit for building case statements
  switch (valueSchema.getType()) {
  case ARRAY:
  break;
  case BOOLEAN:
  break;
  case BYTES:
  break;
  case DOUBLE:
  break;
  case ENUM:
  break;
  case FIXED:
  break;
  case FLOAT:
  break;
  case INT:
  break;
  case LONG:
  break;
  case MAP:
  break;
  case NULL:
  break;
  case RECORD:
  break;
  case STRING:
  break;
  case UNION:
  break;
  default:
  throw new RuntimeException("Unsupported type: " + valueType);
  }
  */
 
 // TODO: Call verify to verify the data is of the right type?
 
 /**
  * A class which knows how to load and store an arbitrary avro schema to both a
  * cursor and a Bundle and has a model for the original values and updated
  * values.
  */
 public class AvroRecordModel extends DataSetObserver {
 	private static final Logger logger = LoggerFactory.getLogger(AvroRecordModel.class);
 
 	/* =-=-=-= Helper Constants For More Readable Code In This Class =-=-=-= */
 	private static final String SEPARATOR = AvroContentProvider.SEPARATOR;
 	private static final String _COUNT = SEPARATOR + "count";
 	private static final String _KEY = AvroContentProvider.KEY_COLUMN_NAME;
 	private static final String _VALUE = AvroContentProvider.VALUE_COLUMN_NAME;
 	private static final String _TYPE = AvroContentProvider.TYPE_COLUMN_NAME;
 	private static final String _TYPE_NAME = AvroContentProvider.TYPE_NAME_COLUMN_NAME;
 	private static final String _URI_NAME = AvroContentProvider.TYPE_URI_COLUMN_NAME;
 
 	/* =-=-=-= Model State =-=-=-= */
 	private final Schema mSchema;
 	private final Uri mUri;
 	private final Activity mActivity;
 
 	private ContentResolver mResolver;
 	private UriRecord mCurrentStateModel;
 	private UriRecord mOriginalModel;
 	private URIHandler uriHandler = new URIHandler();
 	private boolean mDirty;
 
 	// TODO: It would be really nice to have fine grained dirty flags at all levels.
 
 	private static interface UriBound {
 		public Uri getUri();
 		public void setUri(Uri uri);
 	}
 
 	public static class UriUnion implements UriBound {
 		private Uri mUri;
 		private Schema mSchema;
 		private Object mValue;
 		private Type mType;
 		private String mName;
 
 		public UriUnion(Uri uri, Schema schema) {
 			mSchema = schema;
 			mUri = uri;
 		}
 
 		public Object getValue() {
 			return mValue;
 		}
 
 		public void setValue(Object v, Schema schema) {
 			mValue = v;
 			mType = schema.getType();
 			mName = schema.getFullName();
 		}
 
 		private void setType(Type t) {
 			mType = t;
 		}
 
 		public Type getType() {
 			return mType;
 		}
 
 		private void setTypeName(String name) {
 			mName = name;
 		}
 
 		public String getTypeName() {
 			return mName;
 		}
 
 		public Uri getUri() {
 			return mUri;
 		}
 
 		public void setUri(Uri uri) {
 			mUri = uri;
 		}
 
 		public Schema getValueSchema() {
 			for(Schema type : mSchema.getTypes()) {
 				if (type.getType() == mType) {
 					switch (type.getType()) {
 					// TODO: Fixed is a named type
 					case RECORD:
 					case ENUM:
 						if (type.getName().equals(mName)) {
 							return type;
 						}
 						break;
 					default:
 						return type;
 					}
 				}
 			}
 			return Schema.create(Type.NULL);
 		}
 	};
 
 	public static class UriRecord extends GenericData.Record implements UriBound {
 		private Uri mUri;
 
 		public UriRecord(Uri uri, Schema schema) {
 			super(schema);
 			mUri = uri;
 		}
 
 		public Uri getUri() {
 			return mUri;
 		}
 
 		public void setUri(Uri uri) {
 			mUri = uri;
 		}
 	};
 
 	public static class UriMap<K, V> extends HashMap<K, V> implements UriBound {
 		private static final long serialVersionUID = 1L;
 		private Uri mUri;
 
 		public UriMap(Uri uri) {
 			mUri = uri;
 		}
 
 		public Uri getUri() {
 			return mUri;
 		}
 
 		public void setUri(Uri uri) {
 			mUri = uri;
 		}
 	}
 
 	public static class UriFixed extends GenericData.Fixed implements UriBound {
 		private Uri mUri;
 
 		public Uri getUri() {
 			return mUri;
 		}
 
 		public void setUri(Uri uri) {
 			mUri = uri;
 		}
 	}
 
 	public static class UriArray<T> extends GenericData.Array<T> implements UriBound {
 		private static final int DEFAULT_ARRAY_SIZE = 10;
 		private Uri mUri;
 
 		public UriArray(Uri uri, Schema schema) {
 			super(DEFAULT_ARRAY_SIZE, schema);
 			mUri = uri;
 		}
 
 		public UriArray(int count, Uri uri, Schema schema) {
 			super(count, schema);
 			mUri = uri;
 		}
 
 		public Uri getUri() {
 			return mUri;
 		}
 
 		public void setUri(Uri uri) {
 			mUri = uri;
 		}
 	};
 
 	/**
 	 * Constructs a Model for the given Schema. The Schema must be of type
 	 * RECORD.
 	 *
 	 * @param schema
 	 *            The schema to model
 	 */
 	public AvroRecordModel(Activity activity, Uri rootUri, Schema schema) {
 		if (schema.getType() != Type.RECORD) {
 			throw new RuntimeException("Not a record!");
 		}
 		logger.debug("Constructed model for: " + schema);
 		mSchema = schema;
 		mUri = rootUri;
 		mResolver = activity.getContentResolver();
 		mActivity = activity;
 	}
 
 	/**
 	 * Loads the original state of the model from the bundle
 	 *
 	 * @param savedInstanceState
 	 *            the bundle to store to
 	 */
 	public void loadOriginals(Bundle savedInstanceState) {
 		if (savedInstanceState != null) {
 			logger.debug("Loading from bundle.");
 			mCurrentStateModel = BundleHandler.loadRecordFromBundle(savedInstanceState, null,
 					mSchema);
 			if (mOriginalModel == null) {
 				mOriginalModel = BundleHandler.loadRecordFromBundle(savedInstanceState, null,
 						mSchema);
 			}
 		}
 	}
 
 	/**
 	 * Restores the original values stored by the model to the database.
 	 */
 	public void storeOriginalValue() {
 		logger.debug("Storing original values.");
 		if (mDirty && mOriginalModel != null) {
 			uriHandler.storeRecordToUri(mOriginalModel);
 		}
 	}
 
 	/**
 	 * Stores the current values held by the model to the database.
 	 */
 	public void storeCurrentValue() {
 		logger.debug("Storing current state to uri: " + mUri);
 		if (mDirty && mCurrentStateModel != null) {
 			uriHandler.storeRecordToUri(mCurrentStateModel);
 		}
 	}
 
 	/**
 	 * Loads the model from the database.
 	 */
 	public void loadData() {
 		logger.debug("Loading data from: " + mUri);
 		mCurrentStateModel = uriHandler.loadRecordFromUri(mUri, mSchema);
 		mDirty = false;
 		// If there is no original model then load another copy
 		// TODO: Can we do a clone here?
 		if (mOriginalModel == null) {
 			mOriginalModel = uriHandler.loadRecordFromUri(mUri, mSchema);
 		}
 	}
 
 
 	public UriRecord loadRecordFromUri(Uri rootUri, Schema schema) {
 		return uriHandler.loadRecordFromUri(rootUri, schema);
 	}
 
 	/**
 	 * Saves the model to the given bundle.
 	 *
 	 * @param outState
 	 *            the bundle to save to
 	 */
 	public void saveState(Bundle outState) {
 		if (mDirty && mCurrentStateModel != null) {
 			logger.debug("Saving current state to bundle.");
 			BundleHandler.storeRecordToBundle(outState, null, mCurrentStateModel);
 		}
 	}
 
 	/**
 	 * Deletes the data for this model from the database.
 	 */
 	public void delete() {
 		uriHandler.deleteUri(mUri);
 	}
 
 
 	public Schema schema() {
 		return mSchema;
 	}
 
 	public void put(String mFieldName, Object value) {
 		if (mCurrentStateModel != null) {
 			logger.debug("Updating field: " + mFieldName + " to: " + value);
 			mCurrentStateModel.put(mFieldName, value);
 		}
 	}
 
 	public void setResolver(ContentResolver contentResolver) {
 		mResolver = contentResolver;
 	}
 
 	/* =-=-=-= Some Name Helpers =-=-=-= */
 
 	private static class NameHelper {
 
 		private static String getFieldFullName(String dataFullName, String fieldName) {
 			return dataFullName + SEPARATOR + fieldName;
 		}
 
 		private static String getCountName(String fieldFullName) {
 			return fieldFullName + _COUNT;
 		}
 
 		private static String getIndexedFieldName(String fieldFullName, int i) {
 			return fieldFullName + SEPARATOR + i;
 		}
 
 		private static String getPrefixName(String prefix, String fullName) {
 			String dataFullName = fullName;
 			if (prefix != null) {
 				dataFullName = prefix + SEPARATOR + fullName;
 			}
 			return dataFullName;
 		}
 
 		private static String getMapValueName(String fieldFullName) {
 			return fieldFullName + _KEY;
 		}
 
 		private static String getMapKeyName(String fieldFullName) {
 			return fieldFullName + _VALUE;
 		}
 
 		private static String getTypeName(String fieldName) {
 			return fieldName + _TYPE;
 		}
 
 		private static String getTypeNameName(String fieldName) {
 			return fieldName + _TYPE_NAME;
 		}
 
 		private static String getTypeNameUri(String fieldName) {
 			return fieldName + _URI_NAME;
 		}
 
 		public static String getTypeUriName(String fieldName) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 	}
 
 	/* =-=-=-= Load From Bundle =-=-=-= */
 
 	private static class BundleHandler {
 
 		private static UriRecord loadRecordFromBundle(Bundle saved, String prefix,
 				Schema schema) {
 			logger.debug(
 					"Loading data from bundle: " + prefix + " : "
 					+ schema.getFullName());
 			String dataFullName = NameHelper.getPrefixName(prefix, schema.getFullName());
 			UriRecord data = new UriRecord((Uri)saved.get(dataFullName + _URI_NAME), schema);
 
 			for (Field field : schema.getFields()) {
 				String fieldName = field.name();
 				logger.debug("Loading field: " + fieldName);
 				String fieldFullName = NameHelper.getFieldFullName(dataFullName, fieldName);
 				data.put(fieldName,
 						loadDataFromBundle(saved, fieldFullName, field.schema()));
 			}
 
 			return data;
 		}
 
 		private static Object loadDataFromBundle(Bundle saved, String fieldFullName,
 				Schema fieldSchema) {
 			logger.debug("Loading data from bundle: " + fieldFullName);
 			Object value;
 			switch (fieldSchema.getType()) {
 			case ARRAY:
 				value = loadArrayFromBundle(saved, fieldFullName,
 						fieldSchema);
 				break;
 			case BOOLEAN:
 				value = saved.getBoolean(fieldFullName);
 				break;
 			case BYTES:
 				value = saved.getByteArray(fieldFullName);
 				break;
 			case DOUBLE:
 				value = saved.getDouble(fieldFullName);
 				break;
 			case ENUM:
 				value = saved.getString(fieldFullName);
 				break;
 			case FIXED:
 				value = saved.getByteArray(fieldFullName);
 				break;
 			case FLOAT:
 				value = saved.getFloat(fieldFullName);
 				break;
 			case INT:
 				value = saved.getInt(fieldFullName);
 				break;
 			case LONG:
 				value = saved.getLong(fieldFullName);
 				break;
 			case MAP:
 				value = loadMapFromBundle(saved, fieldFullName,
 						fieldSchema.getValueType());
 				break;
 			case NULL:
 				value = null;
 				break;
 			case RECORD:
 				value = loadRecordFromBundle(saved, fieldFullName, fieldSchema);
 				break;
 			case STRING:
 				value = saved.getString(fieldFullName);
 				break;
 			case UNION:
 				value = loadUnionFromBundel(saved, fieldFullName,
 						fieldSchema.getElementType());
 				break;
 			default:
 				throw new RuntimeException("Unsupported type: " + fieldSchema);
 			}
 			return value;
 		}
 
 		private static UriMap<String, Object> loadMapFromBundle(Bundle saved,
 				String fieldFullName, Schema valueType) {
 			String keyName = NameHelper.getMapKeyName(fieldFullName);
 			String valueName = NameHelper.getMapValueName(fieldFullName);
 
 			UriMap<String, Object> map = new UriMap<String, Object>((Uri)saved.get(fieldFullName + _URI_NAME));
 
 			int count = saved.getInt(NameHelper.getCountName(fieldFullName));
 			for (int i = 0; i < count; i++) {
 				String key = saved.getString(NameHelper.getIndexedFieldName(keyName, i));
 				map.put(key, loadDataFromBundle(saved, valueName, valueType));
 			}
 
 			return map;
 		}
 
 		private static UriArray<Object> loadArrayFromBundle(Bundle saved,
 				String fieldFullName, Schema elementSchema) {
 			UriArray<Object> array = null;
 			Uri uri = (Uri) saved.get(fieldFullName + _URI_NAME);
 			switch (elementSchema.getType()) {
 			case ARRAY: {
 				int count = saved.getInt(NameHelper.getCountName(fieldFullName));
 				array = new UriArray<Object>(count, uri, elementSchema);
 				for (int i = 0; i < count; i++) {
 					array.add(loadArrayFromBundle(saved,
 							NameHelper.getIndexedFieldName(fieldFullName, i),
 							elementSchema));
 				}
 				break;
 			}
 			case BOOLEAN: {
 				boolean[] savedData = saved.getBooleanArray(fieldFullName);
 				array = new UriArray<Object>(savedData.length, uri, elementSchema);
 				for (boolean value : savedData) {
 					array.add(value);
 				}
 				break;
 			}
 			case BYTES: {
 				int count = saved.getInt(NameHelper.getCountName(fieldFullName));
 				array = new UriArray<Object>(count, uri, elementSchema);
 				for (int i = 0; i < count; i++) {
 					byte[] data = saved.getByteArray(NameHelper.getIndexedFieldName(
 							fieldFullName, i));
 					array.add(data);
 				}
 				break;
 			}
 			case DOUBLE: {
 				double[] savedData = saved.getDoubleArray(fieldFullName);
 				array = new UriArray<Object>(savedData.length, uri, elementSchema);
 				for (double value : savedData) {
 					array.add(value);
 				}
 				break;
 			}
 			case ENUM: {
 				String[] savedData = saved.getStringArray(fieldFullName);
 				array = new UriArray<Object>(savedData.length, uri, elementSchema);
 				for (String value : savedData) {
 					array.add(value);
 				}
 				break;
 			}
 			case FIXED: {
 				int count = saved.getInt(NameHelper.getCountName(fieldFullName));
 				array = new UriArray<Object>(count, uri, elementSchema);
 				for (int i = 0; i < count; i++) {
 					byte[] data = saved.getByteArray(NameHelper.getIndexedFieldName(
 							fieldFullName, i));
 					array.add(data);
 				}
 				break;
 			}
 			case FLOAT: {
 				float[] savedData = saved.getFloatArray(fieldFullName);
 				array = new UriArray<Object>(savedData.length, uri, elementSchema);
 				for (float value : savedData) {
 					array.add(value);
 				}
 				break;
 			}
 			case INT: {
 				int[] savedData = saved.getIntArray(fieldFullName);
 				array = new UriArray<Object>(savedData.length, uri, elementSchema);
 				for (int value : savedData) {
 					array.add(value);
 				}
 				break;
 			}
 			case LONG: {
 				long[] savedData = saved.getLongArray(fieldFullName);
 				array = new UriArray<Object>(savedData.length, uri, elementSchema);
 				for (long value : savedData) {
 					array.add(value);
 				}
 				break;
 			}
 			case MAP: {
 				int count = saved.getInt(NameHelper.getCountName(fieldFullName));
 				array = new UriArray<Object>(count, uri, elementSchema);
 				for (int i = 0; i < count; i++) {
 					array.add(loadMapFromBundle(saved,
 							NameHelper.getIndexedFieldName(fieldFullName, i),
 							elementSchema.getValueType()));
 				}
 				break;
 			}
 			case NULL: {
 				int count = saved.getInt(NameHelper.getCountName(fieldFullName));
 				array = new UriArray<Object>(count, uri, elementSchema);
 				for (int i = 0; i < count; i++) {
 					array.add(null);
 				}
 				break;
 			}
 			case RECORD: {
 				int count = saved.getInt(NameHelper.getCountName(fieldFullName));
 				array = new UriArray<Object>(count, uri, elementSchema);
 				for (int i = 0; i < count; i++) {
 					array.add(loadRecordFromBundle(saved,
 							NameHelper.getIndexedFieldName(fieldFullName, i), elementSchema));
 				}
 				break;
 			}
 			case STRING: {
 				String[] savedData = saved.getStringArray(fieldFullName);
 				array = new UriArray<Object>(savedData.length, uri, elementSchema);
 				for (String value : savedData) {
 					array.add(value);
 				}
 				break;
 			}
 			case UNION: {
 				int count = saved.getInt(NameHelper.getCountName(fieldFullName));
 				array = new UriArray<Object>(count, uri, elementSchema);
 				for (int i = 0; i < count; i++) {
 					array.add(loadUnionFromBundel(saved,
 							NameHelper.getIndexedFieldName(fieldFullName, i),
 							elementSchema));
 				}
 				break;
 			}
 			default:
 				throw new RuntimeException("Unsupported array type: "
 						+ elementSchema);
 			}
 
 			return array;
 		}
 
 		private static Object loadUnionFromBundel(Bundle saved, String fieldName,
 				Schema elementType) {
 			Type type = Type.valueOf(saved.getString(NameHelper.getTypeName(fieldName)));
 			String typeName = saved.getString(NameHelper.getTypeNameName(fieldName));
 			Uri uri = saved.getParcelable(NameHelper.getTypeUriName(fieldName));
 			UriUnion union = new UriUnion(uri, elementType);
 			union.setType(type);
 			union.setTypeName(typeName);
 
 			Schema fieldType = null;
 			for (Schema unionType : elementType.getTypes()) {
 				if (unionType.getType().equals(type)) {
 					switch (unionType.getType()) {
 					// TODO: Fixed are named types?
 					case RECORD:
 					case ENUM:
 						if (unionType.getFullName().equals(typeName)) {
 							fieldType = unionType;
							break;
 						}
 					default:
 						fieldType = unionType;
 						break;
 					}
 				}
 			}
 			if (fieldType == null) {
 				throw new RuntimeException("Unable to find union inner type: "
 						+ type + " : " + typeName);
 			}
 			union.setValue(loadDataFromBundle(saved, fieldName, fieldType), fieldType);
 			return union;
 		}
 
 		/* =-=-=-= Store To Bundle =-=-=- */
 
 		private static void storeRecordToBundle(Bundle outState, String prefix, UriRecord data) {
 			if (data != null ) {
 				String dataFullName = NameHelper.getPrefixName(prefix, data.getSchema()
 						.getFullName());
 
 				outState.putParcelable(dataFullName + _URI_NAME, data.getUri());
 
 				for (Field field : data.getSchema().getFields()) {
 					String fieldName = field.name();
 					String fieldFullName = NameHelper.getFieldFullName(dataFullName, fieldName);
 					storeDataToBundle(outState, fieldFullName, field.schema(),
 							data.get(fieldName));
 				}
 			}
 		}
 
 		@SuppressWarnings("unchecked")
 		private static void storeDataToBundle(Bundle outState, String fieldFullName,
 				Schema fieldSchema, Object data) {
 			if (data != null) {
 				switch (fieldSchema.getType()) {
 				case ARRAY:
 					storeArrayToBundle(outState, fieldFullName,
 							fieldSchema.getElementType(), (UriArray<Object>) data);
 					break;
 				case BOOLEAN:
 					outState.putBoolean(fieldFullName, (Boolean) data);
 					break;
 				case BYTES:
 					outState.putByteArray(fieldFullName, (byte[]) data);
 					break;
 				case DOUBLE:
 					outState.putDouble(fieldFullName, (Double) data);
 					break;
 				case ENUM:
 					outState.putInt(fieldFullName, (Integer) data);
 					break;
 				case FIXED:
 					outState.putByteArray(fieldFullName, (byte[]) data);
 					break;
 				case FLOAT:
 					outState.putFloat(fieldFullName, (Float) data);
 					break;
 				case INT:
 					outState.putInt(fieldFullName, (Integer) data);
 					break;
 				case LONG:
 					outState.putLong(fieldFullName, (Long) data);
 					break;
 				case MAP:
 					storeMapToBundle(outState, fieldFullName,
 							fieldSchema.getValueType(), (UriMap<String, Object>) data);
 					break;
 				case NULL:
 					// No need to do anything.
 					break;
 				case RECORD:
 					storeRecordToBundle(outState, fieldFullName, (UriRecord) data);
 					break;
 				case STRING:
 					outState.putString(fieldFullName, (String) data);
 					break;
 				case UNION:
 					storeUnionToBundle(outState, fieldFullName, fieldSchema, data);
 					break;
 				default:
 					throw new RuntimeException("Unsupported type: " + fieldSchema);
 				}
 			}
 		}
 
 		private static void storeUnionToBundle(Bundle outState, String fieldFullName,
 				Schema schema, Object object) {
 			if (object != null) {
 				UriUnion union = (UriUnion)object;
 
 				outState.putString(NameHelper.getTypeName(fieldFullName), union.getType().toString());
 				outState.putString(NameHelper.getTypeNameName(fieldFullName), union.getTypeName());
 				outState.putParcelable(NameHelper.getTypeUriName(fieldFullName), union.getUri());
 
 				storeDataToBundle(outState, fieldFullName, union.getValueSchema(), union.getValue());
 			} else {
 				outState.putString(NameHelper.getTypeName(fieldFullName), Type.NULL.toString());
 				outState.putString(NameHelper.getTypeNameName(fieldFullName), null);
 				outState.putParcelable(NameHelper.getTypeUriName(fieldFullName), null);
 			}
 		}
 
 		private static void storeMapToBundle(Bundle outState, String fieldFullName,
 				Schema valueSchema, UriMap<String, Object> map) {
 			if (map != null) {
 				String keyName = NameHelper.getMapKeyName(fieldFullName);
 				String valueName = NameHelper.getMapValueName(fieldFullName);
 				outState.putParcelable(NameHelper.getTypeNameUri(fieldFullName), map.getUri());
 				outState.putInt(NameHelper.getCountName(fieldFullName), map.size());
 				int i = 0;
 				for (String key : map.keySet()) {
 					String keyId = NameHelper.getIndexedFieldName(keyName, i);
 					String valueId = NameHelper.getIndexedFieldName(valueName, i++);
 
 					outState.putString(keyId, key);
 					storeDataToBundle(outState, valueId, valueSchema, map.get(key));
 				}
 			}
 		}
 
 		@SuppressWarnings("unchecked")
 		private static void storeArrayToBundle(Bundle outState, String fieldFullName,
 				Schema elementSchema, UriArray<Object> array) {
 			logger.debug("Storing to bundle: " + outState + " field: " + fieldFullName + " value: " + array);
 			if (array != null) {
 
 				switch (elementSchema.getType()) {
 				case ARRAY: {
 					// WTF? Why is array.size() returning a long while the array
 					// constructor only takes an int?
 					outState.putInt(NameHelper.getCountName(fieldFullName), (int) array.size());
 					Schema subSchema = elementSchema.getElementType();
 					int i = 0;
 					for (Object element : array) {
 						storeArrayToBundle(outState,
 								NameHelper.getIndexedFieldName(fieldFullName, i++), subSchema,
 								(UriArray<Object>) element);
 					}
 					break;
 				}
 				case BOOLEAN: {
 					boolean[] bools = new boolean[(int) array.size()];
 					int i = 0;
 					for (Object element : array) {
 						bools[i++] = (Boolean) element;
 					}
 					outState.putBooleanArray(fieldFullName, bools);
 					break;
 				}
 				case BYTES: {
 					outState.putInt(NameHelper.getCountName(fieldFullName), (int) array.size());
 					int i = 0;
 					for (Object element : array) {
 						outState.putByteArray(NameHelper.getIndexedFieldName(fieldFullName, i++),
 								(byte[]) element);
 					}
 					break;
 				}
 				case DOUBLE: {
 					double[] bools = new double[(int) array.size()];
 					int i = 0;
 					for (Object element : array) {
 						bools[i++] = (Double) element;
 					}
 					outState.putDoubleArray(fieldFullName, bools);
 					break;
 				}
 				case ENUM: {
 					String[] enums = new String[(int) array.size()];
 					int i = 0;
 					for (Object element : array) {
 						enums[i++] = (String) element;
 					}
 					break;
 				}
 				case FIXED: {
 					outState.putInt(NameHelper.getCountName(fieldFullName), (int) array.size());
 					int i = 0;
 					for (Object element : array) {
 						outState.putByteArray(NameHelper.getIndexedFieldName(fieldFullName, i++),
 								(byte[]) element);
 					}
 					break;
 				}
 				case FLOAT: {
 					float[] enums = new float[(int) array.size()];
 					int i = 0;
 					for (Object element : array) {
 						enums[i++] = (Float) element;
 					}
 					break;
 				}
 				case INT: {
 					int[] enums = new int[(int) array.size()];
 					int i = 0;
 					for (Object element : array) {
 						enums[i++] = (Integer) element;
 					}
 					break;
 				}
 				case LONG: {
 					long[] enums = new long[(int) array.size()];
 					int i = 0;
 					for (Object element : array) {
 						enums[i++] = (Long) element;
 					}
 					break;
 				}
 				case MAP: {
 					outState.putInt(NameHelper.getCountName(fieldFullName), (int) array.size());
 					Schema subSchema = elementSchema.getElementType();
 					int i = 0;
 					for (Object element : array) {
 						storeMapToBundle(outState,
 								NameHelper.getIndexedFieldName(fieldFullName, i++), subSchema,
 								(UriMap<String, Object>) element);
 					}
 					break;
 				}
 				case NULL: {
 					outState.putInt(NameHelper.getCountName(fieldFullName), (int) array.size());
 					break;
 				}
 				case RECORD: {
 					outState.putInt(NameHelper.getCountName(fieldFullName), (int) array.size());
 					int i = 0;
 					for (Object element : array) {
 						storeRecordToBundle(outState,
 								NameHelper.getIndexedFieldName(fieldFullName, i++),
 								(UriRecord) element);
 					}
 					break;
 				}
 				case STRING: {
 					String[] enums = new String[(int) array.size()];
 					int i = 0;
 					for (Object element : array) {
 						enums[i++] = (String) element;
 					}
 					break;
 				}
 				case UNION: {
 					outState.putInt(NameHelper.getCountName(fieldFullName), (int) array.size());
 					int i = 0;
 					for (Object element : array) {
 						storeUnionToBundle(outState,
 								NameHelper.getIndexedFieldName(fieldFullName, i++),
 								elementSchema.getElementType(), element);
 					}
 					break;
 				}
 				default:
 					throw new RuntimeException("Unsupported array type: "
 							+ elementSchema);
 				}
 			}
 		}
 	}
 	/* =-=-=-= Load From URI =-=-=-= */
 
 	private class URIHandler {
 
 		private UriRecord loadRecordFromUri(Uri rootUri, Schema schema) {
 			logger.debug("Loading record from uri: " + rootUri + " : " + schema);
 
 			Cursor cursor = mResolver.query(rootUri, null, null, null, null);
 			UriRecord data = new UriRecord(rootUri, schema);
 			try {
				logger.debug("Cursor is: {} {}", cursor, cursor.getCount());
 				if (cursor != null && cursor.getCount() == 1) {
 					cursor.moveToFirst();
 
 					for (Field field : schema.getFields()) {
 						String fieldName = field.name();
 						Object value = loadDataFromUri(rootUri, cursor, fieldName,
 								field.schema());
 						logger.debug("Loaded: " + fieldName + " : " + value);
 						data.put(fieldName, value);
 					}
 				}
 			} finally {
 				if (cursor != null) {
 					cursor.close();
 				}
 			}
 			return data;
 		}
 
 		private Object loadDataFromUri(Uri rootUri, Cursor cursor,
 				String fieldName, Schema fieldSchema) {
 			logger.debug("Loading field: " + fieldName + " : " + fieldSchema);
 			Object value = null;
 			switch (fieldSchema.getType()) {
 			case ARRAY:
 				value = loadArrayFromUri(Uri.withAppendedPath(rootUri, fieldName), fieldName,
 						fieldSchema.getElementType());
 				break;
 			case BOOLEAN:
 				value = (cursor.getInt(cursor.getColumnIndex(fieldName)) == 1);
 				break;
 			case BYTES:
 				// TODO: Should these be handled using streams?
 				value = cursor.getBlob(cursor.getColumnIndex(fieldName));
 				break;
 			case DOUBLE:
 				value = cursor.getDouble(cursor.getColumnIndex(fieldName));
 				break;
 			case ENUM:
 				value = cursor.getInt(cursor.getColumnIndex(fieldName));
 				break;
 			case FIXED:
 				// TODO: Should these be handled using streams?
 				value = cursor.getBlob(cursor.getColumnIndex(fieldName));
 				break;
 			case FLOAT:
 				value = cursor.getFloat(cursor.getColumnIndex(fieldName));
 				break;
 			case INT:
 				value = cursor.getInt(cursor.getColumnIndex(fieldName));
 				break;
 			case LONG:
 				value = cursor.getLong(cursor.getColumnIndex(fieldName));
 				break;
 			case MAP:
 				value = loadMapFromUri(Uri.withAppendedPath(rootUri, fieldName), fieldName,
 						fieldSchema.getValueType());
 				break;
 			case NULL:
 				value = null;
 				break;
 			case RECORD:
 				int recordId = cursor.getInt(cursor.getColumnIndex(fieldName));
 				if (recordId > 0) {
 					Uri recordUri = getRecordUri(rootUri, recordId, fieldSchema);
 					value = loadRecordFromUri(recordUri, fieldSchema);
 				} else {
 					value = null;
 				}
 				break;
 			case STRING:
 				logger.debug("Loading {} : columns: {}", fieldName, cursor.getColumnNames());
 				value = cursor.getString(cursor.getColumnIndex(fieldName));
 				logger.debug("Loaded value: " + value);
 				break;
 			case UNION:
 				value = loadUnionFromUri(rootUri, cursor, fieldName, fieldSchema);
 				break;
 			default:
 				throw new RuntimeException("Unsupported type: " + fieldSchema);
 			}
 			return value;
 		}
 
 		private Uri getRecordUri(Uri rootUri, int recordId, Schema fieldSchema) {
 			UriMatch match = EntityUriMatcher.getMatch(rootUri);
 			return Uri.withAppendedPath(Uri.withAppendedPath(match.getCheckoutUri(), fieldSchema.getFullName()), String.valueOf(recordId));
 		}
 
 		private Object loadUnionFromUri(Uri rootUri, Cursor cursor,
 				String fieldName, Schema elementType) {
 			UriUnion union = new UriUnion(rootUri, elementType);
 			byte[] value = cursor.getBlob(cursor.getColumnIndex(NameHelper.getTypeName(fieldName)));
 
 			if (value != null) {
 				String typeType = cursor.getString(cursor.getColumnIndex(
 						NameHelper.getTypeName(fieldName)));
 				String typeName = cursor.getString(cursor.getColumnIndex(
 						NameHelper.getTypeNameName(fieldName)));
 				union.setTypeName(typeName);
 				logger.debug("Type Type: " + typeType);
 				logger.debug("Type Name: " + typeName);
 
 				Type type = Type.NULL;
 				if (typeType != null) {
 					type = Type.valueOf(typeType);
 				}
 
 				Schema fieldType = null;
 				for (Schema unionType : elementType.getTypes()) {
 					logger.debug("Checking type: " + unionType);
 					if (unionType.getType().equals(type)) {
 						switch (type) {
 						// TODO: Fixed are named types?
 						case RECORD:
 						case ENUM:
 							if (unionType.getFullName().equals(typeName)) {
 								fieldType = unionType;
								break;
 							}
 						default:
 							fieldType = unionType;
 							break;
 						}
 					}
 				}
 				if (fieldType == null) {
 					throw new RuntimeException("Unable to find union inner type: "
 							+ typeName);
 				}
 				union.setType(fieldType.getType());
 				union.setValue(loadDataFromUri(rootUri, cursor, fieldName, fieldType), fieldType);
 			}
 			return union;
 		}
 
 		private UriMap<String, Object> loadMapFromUri(Uri uri, String fieldName, Schema elementType) {
 			logger.debug("Loading map from uri: " + uri + " : " + fieldName + " : " + elementType);
 			Cursor cursor = mResolver.query(uri, null, null, null, null);
 			UriMap<String, Object> map;
 			try {
 				if (cursor != null) {
 					map = new UriMap<String, Object>(uri);
 					int keyIndex = cursor
 					.getColumnIndex(AvroContentProvider.KEY_COLUMN_NAME);
 					int valueIndex = cursor
 					.getColumnIndex(fieldName);
 					while (cursor.moveToNext()) {
 						Uri dataUri = Uri.withAppendedPath(uri, cursor.getString(keyIndex));
 						Cursor dataCursor = cursor;
 						try {
 							switch (elementType.getType()) {
 							case ARRAY:
 							case MAP:
 							case RECORD:
 								int recordId = cursor.getInt(valueIndex);
 								if (recordId > 0) {
 									dataUri = getRecordUri(uri, recordId, elementType);
 									dataCursor = mResolver.query(dataUri, null, null, null, null);
 									if (dataCursor != null) {
 										dataCursor.moveToFirst();
 									}
 								}
 								// FALL THROUGH!
 							default:
 								map.put(cursor.getString(keyIndex),
 										loadDataFromUri(dataUri,
 												dataCursor, fieldName, elementType));
 							}
 						} finally {
 							switch (elementType.getType()) {
 							case ARRAY:
 							case MAP:
 							case RECORD:
 								safeClose(dataCursor);
 							default:
 								// Do nothing;
 							}
 						}
 					}
 				} else {
 					throw new RuntimeException("Unable to load: " + uri);
 				}
 			} finally {
 				if (cursor != null) {
 					cursor.close();
 				}
 			}
 			return map;
 		}
 
 		private UriArray<Object> loadArrayFromUri(Uri uri, String fieldName, Schema elementType) {
 			logger.debug("Loading array from uri: " + uri + " : " + elementType);
 			Cursor cursor = mResolver.query(uri, null, null, null, null);
 			UriArray<Object> data;
 			try {
 				if (cursor != null) {
 					data = new UriArray<Object>(cursor.getCount(), uri, Schema.createArray(elementType));
 					while (cursor.moveToNext()) {
 						data.add(loadDataFromUri(uri, cursor, fieldName, elementType));
 					}
 				} else {
 					throw new RuntimeException("Unable to load: " + uri);
 				}
 			} finally {
 				if (cursor != null) {
 					cursor.close();
 				}
 			}
 			return data;
 		}
 
 		/* =-=-=-= Store To URI =-=-=-= */
 
 		private void storeRecordToUri(UriRecord record) {
 			logger.debug("Storing Record: " + record.getSchema().getName() + " to uri: " + record.getUri());
 			if (record != null) {
 				ContentValues values = new ContentValues();
 
 				for (Field field : record.getSchema().getFields()) {
 					String fieldName = field.name();
 					Uri dataUri = storeDataToUri(record.getUri(), values, field.name(), field.schema(),
 							record.get(fieldName));
 					if (field.schema().getType() == Type.RECORD && dataUri != null) {
 						UriMatch match = EntityUriMatcher.getMatch(dataUri);
 						values.put(fieldName, match.entityIdentifier);
 					}
 				}
 
 				updateUriOrThrow(record.getUri(), values);
 			}
 		}
 
 		private void updateUriOrThrow(Uri rootUri, ContentValues values) {
 			logger.debug("Updating: " + rootUri);
 			if (values.size() > 0) {
 				// Turns out update returns 0 if nothing changed in the row. Ah well. Nice try.
 				//			int count =
 				mResolver.update(rootUri, values, null, null);
 				//			if (count != 1) {
 				//				throw new RuntimeException("Error updating record. Count was: "
 				//						+ count);
 				//			}
 			}
 		}
 
 		private int deleteUri(Uri rootUri) {
 			logger.debug("Deleting URI: " + rootUri);
 			return mResolver.delete(rootUri, null, null);
 		}
 
 		private Uri insertUri(Uri rootUri, ContentValues values) {
 			logger.debug("Inserting into: {} : {}", rootUri, values);
 			return mResolver.insert(rootUri, values);
 		}
 
 		@SuppressWarnings("unchecked")
 		private Uri storeDataToUri(Uri rootUri, ContentValues values,
 				String fieldName, Schema fieldSchema, Object data) {
 			logger.debug("Storing to: " + rootUri + " fieldName: " + fieldName + " schema: " + fieldSchema);
 			Uri dataUri = null;
 			switch (fieldSchema.getType()) {
 			case ARRAY:
 				Uri arrayUri = Uri.withAppendedPath(rootUri, fieldName);
 				storeArrayToUri(arrayUri,
 						fieldName, fieldSchema.getElementType(),
 						(UriArray<Object>) data);
 				dataUri = arrayUri;
 				break;
 			case BOOLEAN:
 				values.put(fieldName, (Boolean) data);
 				break;
 			case BYTES:
 				values.put(fieldName, (byte[]) data);
 				break;
 			case DOUBLE:
 				values.put(fieldName, (Double) data);
 				break;
 			case ENUM:
 				values.put(fieldName, (Integer) data);
 				break;
 			case FIXED:
 				values.put(fieldName, (byte[]) data);
 				break;
 			case FLOAT:
 				values.put(fieldName, (Float) data);
 				break;
 			case INT:
 				values.put(fieldName, (Integer) data);
 				break;
 			case LONG:
 				values.put(fieldName, (Long) data);
 				break;
 			case MAP:
 				Uri mapUri = Uri.withAppendedPath(rootUri, fieldName);
 				storeMapToUri(mapUri, fieldName,
 						fieldSchema.getValueType(), (UriMap<String, Object>) data);
 				dataUri = mapUri;
 				break;
 			case NULL:
 				// No need to do anything. Defaults to null.
 				break;
 			case RECORD:
 				// What is the right URI for this record?
 				if (data != null) {
 					UriRecord record = (UriRecord)data;
 					UriMatch match = EntityUriMatcher.getMatch(rootUri);
 
 					// Is there an existing record at this location.
 
 					Uri recordUri;
 					Uri baseUri;
 					switch (match.type) {
 					case LOCAL_BRANCH:
 						baseUri = Uri.withAppendedPath(EntityUriBuilder.branchUri(match.authority, match.repositoryName, match.reference),
 								GenericContentProvider.escapeName(match.repositoryName, record.getSchema().getNamespace(), record.getSchema().getName()));
 						if (match.entityIdentifier != null) {
 							recordUri = Uri.withAppendedPath(baseUri, match.entityIdentifier);
 						} else {
 							recordUri = insertUri(baseUri, new ContentValues());
 							record.setUri(recordUri);
 						}
 						storeRecordToUri(record);
 						dataUri = recordUri;
 						break;
 					default:
 						throw new RuntimeException("Can only store to local branches.");
 					}
 				}
 				break;
 			case STRING:
 				values.put(fieldName, (String) data);
 				break;
 			case UNION:
 				storeUnionToUri(Uri.withAppendedPath(rootUri, fieldName), values,
 						fieldName, fieldSchema, data);
 				break;
 			default:
 				throw new RuntimeException("Unsupported type: " + fieldSchema);
 			}
 
 			return dataUri;
 		}
 
 		private void storeUnionToUri(Uri rootUri, ContentValues values,
 				String fieldName, Schema schema, Object object) {
 			// If the object is null we store it as NULL since NULL may
 			// not be part of the union type, but we don't always
 			// require that everything is valid yet at this point.
 			UriUnion union = null;
 			if (object != null) {
 				union = (UriUnion)object;
 				values.put(NameHelper.getTypeName(fieldName), union.getType().toString());
 				values.put(NameHelper.getTypeNameName(fieldName), union.getTypeName());
 			} else {
 				values.put(NameHelper.getTypeName(fieldName), Type.NULL.toString());
 				values.putNull(NameHelper.getTypeNameName(fieldName));
 			}
 			storeDataToUri(rootUri, values, fieldName, union == null ? Schema.create(Type.NULL) : union.getValueSchema(), object);
 		}
 
 		private void storeMapToUri(Uri rootUri, String fieldFullName,
 				Schema valueSchema, UriMap<String, Object> map) {
 
 			deleteMap(rootUri, fieldFullName, valueSchema);
 
 			ContentValues values = new ContentValues();
 
 			for (String key : map.keySet()) {
 				values.clear();
 				values.put(_KEY, key);
 				// First insert a row with just the key so we can get the ID of the
 				// row in
 				// case the row is really an array or some other table based row
 				Uri idUri = insertUri(rootUri, values);
 				logger.debug("Got id uri for map row: " + idUri);
 				Uri dataUri = storeDataToUri(idUri, values, fieldFullName,
 						valueSchema, map.get(key));
 				if (dataUri != null) {
 					UriMatch match = EntityUriMatcher.getMatch(dataUri);
 					values.put(fieldFullName, match.entityIdentifier);
 				}
 				updateUriOrThrow(idUri, values);
 			}
 		}
 
 
 		private void storeArrayToUri(Uri rootUri, String fieldFullName,
 				Schema elementSchema, UriArray<Object> array) {
 
 			deleteArray(rootUri, fieldFullName, elementSchema);
 
 			ContentValues values = new ContentValues();
 			if (array != null) {
 				for (Object value : array) {
 					values.clear();
 					// First insert a null row
 					Uri idUri = insertUri(rootUri, values);
 					logger.debug("Got id uri for array row: " + idUri);
 					Uri dataUri = storeDataToUri(rootUri, values, fieldFullName,
 							elementSchema, value);
 					if (dataUri != null) {
 						UriMatch match = EntityUriMatcher.getMatch(dataUri);
 						values.put(fieldFullName, match.entityIdentifier);
 					}
 					updateUriOrThrow(idUri, values);
 				}
 			}
 		}
 
 
 		private void safeClose(Cursor cursor) {
 			if (null != cursor) {
 				try {
 					cursor.close();
 				} catch (Exception e) {
 					// Ignored
 				}
 			}
 		}
 
 		private void deleteArray(Uri rootUri, String fieldFullName, Schema elementSchema) {
 			logger.debug("Deleting Array: " + rootUri);
 			switch (elementSchema.getType()) {
 			case ARRAY:
 			{
 				Cursor cursor = null;
 				try {
 					cursor = mResolver.query(rootUri, new String[] {fieldFullName}, null, null, null);
 					if (null != cursor && cursor.moveToFirst()) {
 						do {
 							String arrayUri = cursor.getString(0);
 							if (arrayUri != null) {
 								deleteArray(Uri.parse(arrayUri), fieldFullName, elementSchema.getElementType());
 							}
 						} while (cursor.moveToNext());
 					}
 				} finally {
 					safeClose(cursor);
 				}
 				break;
 			}
 			case MAP:
 			{
 				Cursor cursor = null;
 				try {
 					cursor = mResolver.query(rootUri, new String[] {fieldFullName}, null, null, null);
 					if (null != cursor && cursor.moveToFirst()) {
 						do {
 							String arrayUri = cursor.getString(0);
 							if (arrayUri != null) {
 								deleteMap(Uri.parse(arrayUri), fieldFullName, elementSchema.getValueType());
 							}
 						} while (cursor.moveToNext());
 					}
 				} finally {
 					safeClose(cursor);
 				}
 				break;
 			}
 			case RECORD:
 			{
 				Cursor cursor = null;
 				try {
 					cursor = mResolver.query(rootUri, new String[] {fieldFullName}, null, null, null);
 					if (null != cursor && cursor.moveToFirst()) {
 						do {
 							int id = cursor.getInt(0);
 							Uri recordUri = getRecordUri(rootUri, id, elementSchema);
 							deleteRecord(recordUri, elementSchema);
 						} while (cursor.moveToNext());
 					}
 				} finally {
 					safeClose(cursor);
 				}
 				break;
 			}
 			default:
 				break;
 			}
 			mResolver.delete(rootUri, null, null);
 		}
 
 		private void deleteMap(Uri rootUri, String fieldFullName, Schema valueSchema) {
 			logger.debug("Deleting Map: " + rootUri);
 			switch (valueSchema.getType()) {
 			case ARRAY:
 			{
 				Cursor cursor = null;
 				try {
 					cursor = mResolver.query(rootUri, new String[] {fieldFullName}, null, null, null);
 					if (null != cursor && cursor.moveToFirst()) {
 						do {
 							int id = cursor.getInt(0);
 							Uri recordUri = getRecordUri(rootUri, id, valueSchema);
 							deleteRecord(recordUri, valueSchema);
 						} while (cursor.moveToNext());
 					}
 				} finally {
 					safeClose(cursor);
 				}
 				break;
 			}
 			case MAP:
 			{
 				Cursor cursor = null;
 				try {
 					cursor = mResolver.query(rootUri, new String[] {fieldFullName}, null, null, null);
 					if (null != cursor && cursor.moveToFirst()) {
 						do {
 							String arrayUri = cursor.getString(0);
 							if (arrayUri != null) {
 								deleteMap(Uri.parse(arrayUri), fieldFullName, valueSchema.getValueType());
 							}
 						} while (cursor.moveToNext());
 					}
 				} finally {
 					safeClose(cursor);
 				}
 				break;
 			}
 			case RECORD:
 			{
 				Cursor cursor = null;
 				try {
 					cursor = mResolver.query(rootUri, new String[] {fieldFullName}, null, null, null);
 					if (null != cursor && cursor.moveToFirst()) {
 						do {
 							String arrayUri = cursor.getString(0);
 							if (arrayUri != null) {
 								deleteRecord(Uri.parse(arrayUri), valueSchema);
 							}
 						} while (cursor.moveToNext());
 					}
 				} finally {
 					safeClose(cursor);
 				}
 				break;
 			}
 			default:
 				break;
 			}
 			mResolver.delete(rootUri, null, null);
 		}
 
 		private void deleteRecord(Uri rootUri, Schema schema) {
 			logger.debug("Deleting Record: " + rootUri);
 			for (Field field : schema.getFields()) {
 				String fieldName = field.name();
 				switch (field.schema().getType()) {
 				case ARRAY:
 					deleteArray(Uri.withAppendedPath(rootUri, fieldName), fieldName, field.schema());
 					break;
 				case MAP:
 					deleteMap(Uri.withAppendedPath(rootUri, fieldName), fieldName, field.schema());
 					break;
 				case RECORD:
 				{
 					Cursor cursor = null;
 					int id = -1;
 					try {
 						cursor = mResolver.query(rootUri, new String[] {fieldName}, null, null, null);
 						if (null != cursor && cursor.moveToFirst()) {
 							id = cursor.getInt(0);
 						}
 					} finally {
 						safeClose(cursor);
 					}
 					UriMatch result = EntityUriMatcher.getMatch(rootUri);
 					Uri recordUri = Uri.withAppendedPath(Uri.withAppendedPath(result.getCheckoutUri(), field.schema().getName()), String.valueOf(id));
 					deleteRecord(recordUri, field.schema());
 					break;
 				}
 				}
 			}
 
 			mResolver.delete(rootUri, null, null);
 		}
 	}
 
 	public UriRecord getCurrentModel() {
 		return mCurrentStateModel;
 	}
 
 	public Object get(String nameField) {
 		return mCurrentStateModel.get(nameField);
 	}
 
 	public void runOnUI(Runnable runnable) {
 		mActivity.runOnUiThread(runnable);
 	}
 
 	public void onChanged() {
 		mDirty = true;
 	}
 
 	public void onInvalidated() {
 		mDirty = true;
 	}
 }
