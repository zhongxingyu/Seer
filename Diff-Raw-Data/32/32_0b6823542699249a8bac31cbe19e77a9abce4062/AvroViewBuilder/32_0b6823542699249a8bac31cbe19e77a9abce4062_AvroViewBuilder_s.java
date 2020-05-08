 package interdroid.vdb.avro.view.factory;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import interdroid.util.view.ViewUtil;
 import interdroid.vdb.avro.AvroSchemaProperties;
 import interdroid.vdb.avro.control.handler.value.ValueHandler;
 import interdroid.vdb.avro.model.AvroRecordModel;
 import interdroid.vdb.avro.model.NotBoundException;
 
 import org.apache.avro.Schema;
 import org.apache.avro.Schema.Field;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.view.View;
 import android.view.ViewGroup;
 
 /**
  * This class knows how to build view of various types using
  * subclasses.
  *
  * @author nick &lt;palmer@cs.vu.nl&gt;
  *
  */
 public final class AvroViewBuilder {
 	/**
 	 * Access to logging interface.
 	 */
 	private static final Logger LOG =
 			LoggerFactory.getLogger(AvroViewBuilder.class);
 
 	/**
 	 * No construction.
 	 */
 	private AvroViewBuilder() {
 		// No construction
 	}
 
 	// =-=-=-=- Static Interface -=-=-=-=
 
 	/**
 	 * A hash of builders by type so we can find them quickly.
 	 */
 	private static Map<AvroViewType, AvroTypedViewBuilder> sBuilders =
 			new HashMap<AvroViewType, AvroTypedViewBuilder>();
 
 	/**
 	 * The various builder subclasses we can use to assist us.
 	 */
 	private static AvroTypedViewBuilder[] sBuilderInstances =
 			new AvroTypedViewBuilder[] {
 		new AvroArrayBuilder(),
 		new AvroBooleanBuilder(),
 		new AvroDateBuilder(),
 		new AvroEnumBuilder(),
 		new AvroLocationBuilder(),
 		new AvroNullBuilder(),
 		new AvroNumericBuilder(),
 		new AvroPhotoBuilder(),
 		new AvroRecordBuilder(),
 		new AvroStringBuilder(),
 		new AvroTimeBuilder(),
 		new AvroTimestampBuilder(),
 		new AvroUnionBuilder(),
 	};
 
 	static {
 		// Initialize the hash for quick access to builders
 		for (AvroTypedViewBuilder builder : sBuilderInstances) {
 			for (AvroViewType type : builder.getViewTypes()) {
 				sBuilders.put(type, builder);
 			}
 		}
 	}
 
 	/**
 	 * Binds data for a given field to a view.
 	 * @param view the view to bind with
 	 * @param cursor the cursor with the data
 	 * @param field the field to bind
 	 */
 	public static void bindListView(final View view, final Cursor cursor,
 			final Field field) {
 		// Find the builder for this type
 		LOG.debug("Getting builder for: {}", field);
 		AvroTypedViewBuilder builder = sBuilders.get(new AvroViewType(field));

 
 		if (builder != null) {
 			LOG.debug("Binding with: {} {}", builder, field.name());
 			builder.bindListView(view, cursor, field);
 		} else {
 			LOG.error("No builder for field: {}", field);
 		}
 
 	}
 
 	/**
 	 * Returns a view for use in a list context.
 	 * @param context the context we are building for
 	 * @param field the field to build a view for
 	 * @return the built view
 	 */
 	public static View getListView(final Context context,
 			final Field field) {
 		if (field.getProp(AvroSchemaProperties.UI_RESOURCE) != null) {
 
 			LOG.debug("Inflating custom resource: {}",
 					field.getProp(AvroSchemaProperties.UI_LIST_RESOURCE));
 			try {
 
 				View view = ViewUtil.getLayoutInflater(context).inflate(
 						Integer.valueOf(
 								field.getProp(
 										AvroSchemaProperties.UI_LIST_RESOURCE)),
 										null);
 				return view;
 
 			} catch (Exception e) {
 				LOG.error("Unable to inflate resource: {}",
 						field.getProp(AvroSchemaProperties.UI_LIST_RESOURCE));
 				throw new IllegalArgumentException(
 						"Unable to inflate UI resource: "
 						+ field.getProp(
 								AvroSchemaProperties.UI_LIST_RESOURCE), e);
 			}
 
 		} else {
 
 			// Find the builder for this type
 			LOG.debug("Getting builder for: {}", field);
 			AvroTypedViewBuilder builder =
 					sBuilders.get(new AvroViewType(field));
 
 			LOG.debug("Building with: {} {}", builder, field.name());
 
 			if (builder == null) {
 				LOG.error("No builder for field: {}", field);
 				return null;
 			}
 
 			return builder.buildListView(context, field);
 		}
 	}
 
 	/**
 	 * Builds an edit view.
 	 * @param activity the activity the view goes in
 	 * @param dataModel the data model to get data from
 	 * @param viewGroup the view group to add the view to
 	 * @param schema the schema for the data
 	 * @param field the field
 	 * @param uri the uri for the field
 	 * @param valueHandler the value handler to set data with
 	 * @return The view.
 	 * @throws NotBoundException if the model is not bound
 	 */
 	public static View getEditView(final Activity activity,
 			final AvroRecordModel dataModel, final ViewGroup viewGroup,
 			final Schema schema, final Field field, final Uri uri,
 			final ValueHandler valueHandler)
 					throws NotBoundException {
 
 		if (field != null
 				&& field.getProp(AvroSchemaProperties.UI_RESOURCE) != null) {
 
 			LOG.debug("Inflating custom resource: {}",
 					field.getProp(AvroSchemaProperties.UI_RESOURCE));
 			try {
 
 				View view = ViewUtil.getLayoutInflater(activity).inflate(
 						Integer.valueOf(
 								field.getProp(
 										AvroSchemaProperties.UI_RESOURCE)),
 										null);
 				return view;
 
 			} catch (Exception e) {
 				LOG.error("Unable to inflate resource: {}",
 						field.getProp(AvroSchemaProperties.UI_RESOURCE));
 				throw new IllegalArgumentException(
 						"Unable to inflate UI resource: "
 						+ field.getProp(AvroSchemaProperties.UI_RESOURCE), e);
 			}
 
 		} else {
 
 			// Find the builder for this type
 			AvroTypedViewBuilder builder = null;
 
 			AvroViewType type = new AvroViewType(schema);
 			LOG.debug("Getting builder for: {}", type);
 			builder = sBuilders.get(type);
 
 			if (builder == null) {
 				LOG.error("No builder for schema: {}", schema);
 				throw new IllegalArgumentException(
 						"Don't know how to build a view for: " + schema);
 			}
 
 			LOG.debug("Building with: {} {}", builder, schema.getName());
 
 
 			return builder.buildEditView(activity, dataModel, viewGroup,
 					schema, field, uri, valueHandler);
 		}
 	}
 
 	/**
 	 * @param field the field to get the projection for
 	 * @return the column names required to project this field
 	 */
 	public static List<String> getProjectionFields(final Field field) {
 		// Find the builder for this type
		LOG.debug("Getting builder for projection: {}", field);
		AvroTypedViewBuilder builder = sBuilders.get(new AvroViewType(field));
 		if (builder != null) {
 			return builder.getProjectionFields(field);
 		} else {
 			LOG.debug("No builder for that type.");
 			return null;
 		}
 	}
 
 }
