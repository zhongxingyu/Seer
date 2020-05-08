 package interdroid.vdb.content.avro;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.avro.Schema;
 import org.apache.avro.Schema.Field;
 import org.apache.avro.Schema.Type;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import interdroid.vdb.content.GenericContentProvider;
 import interdroid.vdb.content.metadata.EntityInfo;
 import interdroid.vdb.content.metadata.FieldInfo;
 
 public class AvroEntityInfo extends EntityInfo {
 	private static final Logger logger = LoggerFactory.getLogger(AvroEntityInfo.class);
 
 	// TODO: (nick) Support sort order from the schema as default sort order.
 	// TODO: (nick) Support default values from the schema
 	// TODO: (nick) Support properties to specify what the key is instead of/in addition to supporting implicit keys we use now
 	// TODO: (nick) Support cross namespace entities. We could embed the URI for the entity in the parent_id instead of an integer id.
 	// TODO: (nick) Fixed are named. Those should have their own table probably or they will break.
 
 	private Schema schema_;
 
 	public AvroEntityInfo(Schema schema, AvroMetadata avroMetadata) {
 		this(schema, avroMetadata, null);
 	}
 
 	public AvroEntityInfo(Schema schema, AvroMetadata avroMetadata, EntityInfo parentEntity) {
 		schema_ = schema;
 		this.parentEntity = parentEntity;
 		if (parentEntity != null && !this.schema_.getNamespace().equals(parentEntity.namespace())) {
 			throw new RuntimeException("Only entities in the same namespace are currently supported");
 		}
 		avroMetadata.put(this);
 		parseSchema(avroMetadata, parentEntity);
 		if (parentEntity != null) {
 			parentEntity.children.add(this);
 		}
 	}
 
 	private void parseSchema(AvroMetadata avroMetadata, EntityInfo parentEntity) {
 		if (logger.isDebugEnabled())
 			logger.debug("Constructing avro entity with name: " + name() + " in namespace: " + namespace() + " schema: " + schema_);
 
 		// Every entity gets an _id int field as a primary key
 		FieldInfo keyField = new AvroFieldInfo(new Field(AvroContentProvider.ID_COLUMN_NAME, Schema.create(Schema.Type.INT), null, null), true);
 		fields.put(keyField.fieldName, keyField);
 		this.key.add(keyField);
 
 		// Sub entities get columns which reference their parent key
 		// TODO: (nick) For cross namespace this should be a string with the URI probably.
 		if (parentEntity != null) {
 			for (FieldInfo field : parentEntity.key) {
 				if (logger.isDebugEnabled())
 					logger.debug("Adding parent key field.");
 				keyField = new AvroFieldInfo(new Field(GenericContentProvider.PARENT_COLUMN_PREFIX + field.fieldName, ((AvroFieldInfo)field).schema_, null, null), false);
 				keyField.targetEntity = parentEntity;
 				keyField.targetField = field;
 				fields.put(keyField.fieldName, keyField);
 			}
 		}
 
 		switch (schema_.getType()) {
 		case ENUM:
 			parseEnum(avroMetadata);
 			break;
 		case RECORD:
 			parseRecord(avroMetadata);
 			break;
 		default:
 			throw new RuntimeException("Unsupported entity type: " + schema_);
 		}
 	}
 
 	private void parseEnum(AvroMetadata avroMetadata) {
 		AvroFieldInfo field = new AvroFieldInfo(new Schema.Field(AvroContentProvider.VALUE_COLUMN_NAME, Schema.create(Schema.Type.STRING), null, null), false);
 		fields.put(field.fieldName, field);
 		setEnumValues(schema_);
 	}
 
 	private void parseRecord(AvroMetadata avroMetadata) {
 		// Walk the fields in the record constructing either primitive fields or entity fields
 		for (Field field: schema_.getFields()) {
 			switch (field.schema().getType()) {
 			case ARRAY:
 			case MAP:
 			case ENUM:
 			case RECORD:
 			{
 				FieldInfo fieldInfo = new AvroFieldInfo(field);
 				fields.put(fieldInfo.fieldName, fieldInfo);
 				EntityInfo innerType = fetchOrBuildEntity(avroMetadata, field.schema(), field.name(), this);
 				fieldInfo.targetEntity = innerType;
 				// TODO: Support for complex keys.
 				fieldInfo.targetField = innerType.key.get(0);
 				if (logger.isDebugEnabled())
 					logger.debug("Adding sub-table field: " + fieldInfo.fieldName);
 				break;
 			}
 			case UNION:
 			{
				// Unions get three fields, one to hold the type the value has, one to hold the name if it is a named type and one for the value.
 				// We are abusing SQLite manifest typing on the value column with good reason
 				FieldInfo typeField = new AvroFieldInfo(new Field(field.name() + AvroContentProvider.TYPE_COLUMN_NAME, Schema.create(Schema.Type.STRING), null, null), true);
 				fields.put(typeField.fieldName, typeField);
 				FieldInfo typeNameField = new AvroFieldInfo(new Field(field.name() + AvroContentProvider.TYPE_NAME_COLUMN_NAME, Schema.create(Schema.Type.STRING), null, null), true);
 				fields.put(typeNameField.fieldName, typeNameField);
 				if (logger.isDebugEnabled())
 					logger.debug("Adding union field: " + field.name());
 
 				// Make sure all of the possible inner types for the union exist
 				for (Schema innerType : field.schema().getTypes()) {
 					switch (innerType.getType()) {
 					case ARRAY:
 					case MAP:
 						fetchOrBuildEntity(avroMetadata, innerType, field.name(), this);
 						break;
 					case ENUM:
 					case RECORD:
 						fetchOrBuildEntity(avroMetadata, innerType, innerType.getName(), this);
 						break;
 					}
 				}
 
				// Intentional fall through to construct the value field.
 			}
 			case FIXED:
 			case FLOAT:
 			case INT:
 			case LONG:
 			case BOOLEAN:
 			case BYTES:
 			case DOUBLE:
 			case STRING:
 			case NULL:
 			{
 				FieldInfo fieldInfo = new AvroFieldInfo(field, false);
 				if (logger.isDebugEnabled())
 					logger.debug("Adding field: " + fieldInfo.fieldName);
 				fields.put(fieldInfo.fieldName, fieldInfo);
 				break;
 			}
 			default:
 				throw new RuntimeException("Unsupported type: " + field.schema());
 			}
 		}
 	}
 
 	private EntityInfo fetchOrBuildEntity(AvroMetadata avroMetadata, Schema fieldSchema, String fieldName, EntityInfo parent) {
 		EntityInfo innerType;
 
 		switch (fieldSchema.getType()) {
 		case ARRAY:
 			// Build the association type
 			innerType = buildArrayAssociationTable(avroMetadata, fieldSchema, fieldName, parent);
 
 			// Construct the target type if required.
 			switch (fieldSchema.getElementType().getType()) {
 			case RECORD:
 			case ENUM:
 				// Make sure the target type exists.
 				fetchOrBuildEntity(avroMetadata, fieldSchema.getElementType(), fieldName, innerType);
 				break;
 			case ARRAY:
 				// Make sure the target type exists.
 				fetchOrBuildEntity(avroMetadata, fieldSchema.getElementType(), fieldName, innerType);
 				break;
 			case MAP:
 				// Make sure the target type exists.
 				fetchOrBuildEntity(avroMetadata, fieldSchema.getElementType(), fieldName, innerType);
 				break;
 			case BOOLEAN:
 			case BYTES:
 			case DOUBLE:
 			case FIXED:
 			case FLOAT:
 			case INT:
 			case LONG:
 			case NULL:
 			case STRING:
 			case UNION:
 				break;
 			default:
 				throw new RuntimeException("Unsupported type: " + fieldSchema);
 			}
 			break;
 		case ENUM:
 		{
 			innerType = avroMetadata.getEntity(fieldSchema.getFullName());
 
 			if (innerType == null) {
 				// Enums are built with no parent since we point to them with an integer key
 				innerType = new AvroEntityInfo(fieldSchema, avroMetadata, null);
 			}
 			break;
 		}
 		case MAP:
 			// Now we need to build an association table
 			innerType = buildMapAssociationTable(avroMetadata, fieldSchema, fieldName, parent);
 
 			// Construct the target type if required.
 			switch (fieldSchema.getElementType().getType()) {
 			case RECORD:
 			case ENUM:
 			case ARRAY:
 			case MAP:
 				// Make sure the target type exists.
 				fetchOrBuildEntity(avroMetadata, fieldSchema.getValueType(), fieldName, innerType);
 				break;
 			case BOOLEAN:
 			case BYTES:
 			case DOUBLE:
 			case FIXED:
 			case FLOAT:
 			case INT:
 			case LONG:
 			case NULL:
 			case STRING:
 			case UNION:
 				break;
 			default:
 				throw new RuntimeException("Unsupported type: " + fieldSchema);
 			}
 		case RECORD:
 			innerType = avroMetadata.getEntity(fieldSchema.getFullName());
 
 			// Construct the inner type entity.
 			if (innerType == null) {
 				innerType = new AvroEntityInfo(fieldSchema, avroMetadata);
 			}
 			break;
 		case UNION:
 		case BOOLEAN:
 		case BYTES:
 		case DOUBLE:
 		case FIXED:
 		case FLOAT:
 		case INT:
 		case LONG:
 		case NULL:
 		case STRING:
 		default:
 			throw new RuntimeException("Unsupported type: " + fieldSchema);
 		}
 
 		return innerType;
 	}
 
 	private EntityInfo buildMapAssociationTable(AvroMetadata avroMetadata, Schema fieldSchema,
 			String fieldName, EntityInfo parent) {
 		List<Field>mapFields = new ArrayList<Field>();
 		mapFields.add(new Schema.Field(AvroContentProvider.KEY_COLUMN_NAME, Schema.create(Schema.Type.STRING), null, null));
 		// Maps of unions get an extra type field
 		if (fieldSchema.getType() == Type.UNION) {
 			mapFields.add(new Schema.Field(AvroContentProvider.TYPE_COLUMN_NAME, Schema.create(Schema.Type.STRING), null, null));
 		}
 		mapFields.add(new Schema.Field(fieldName, Schema.create(Schema.Type.BYTES), null, null));
 		Schema mapSchema = Schema.createRecord(getFullName() + AvroContentProvider.MAP_TABLE_INFIX + fieldName, null, schema_.getNamespace(), false);
 		mapSchema.setFields(mapFields);
 		return new AvroEntityInfo(mapSchema, avroMetadata, parent);
 	}
 
 	private EntityInfo buildArrayAssociationTable(AvroMetadata avroMetadata, Schema fieldSchema,
 			String fieldName, EntityInfo parent) {
 		List<Field>arrayFields = new ArrayList<Field>();
 		// Arrays of unions get an extra type field
 		if (fieldSchema.getType() == Type.UNION) {
 			arrayFields.add(new Schema.Field(AvroContentProvider.TYPE_COLUMN_NAME, Schema.create(Schema.Type.STRING), null, null));
 		}
 		arrayFields.add(new Schema.Field(fieldName, Schema.create(Schema.Type.BYTES), null, null));
 		Schema mapSchema = Schema.createRecord(getFullName() + AvroContentProvider.ARRAY_TABLE_INFIX + fieldName, null, schema_.getNamespace(), false);
 		mapSchema.setFields(arrayFields);
 		return new AvroEntityInfo(mapSchema, avroMetadata, parent);
 	}
 
 	@Override
 	public String name() {
 		return schema_.getName();
 	}
 
 	public String namespace() {
 		return schema_.getNamespace();
 	}
 
 	@Override
 	public String contentType() {
 		return "vnd.android.cursor.dir/vnd." + namespaceDot()  + name();
 	}
 
 	@Override
 	public String itemContentType() {
 		return "vnd.android.cursor.item/vnd." + namespaceDot() + name();
 	}
 
 	private void setEnumValues(Schema fieldSchema) {
 		enumValues = new HashMap<Integer, String>();
 		for (String value : fieldSchema.getEnumSymbols()) {
 			enumValues.put(fieldSchema.getEnumOrdinal(value), value);
 		}
 	}
 
 }
