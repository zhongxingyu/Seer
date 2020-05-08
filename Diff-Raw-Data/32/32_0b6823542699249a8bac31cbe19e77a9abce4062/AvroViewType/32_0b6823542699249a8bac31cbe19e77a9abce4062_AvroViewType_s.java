 package interdroid.vdb.avro.view.factory;
 
 import interdroid.vdb.avro.AvroSchemaProperties;
 
 import org.apache.avro.Schema;
 import org.apache.avro.Schema.Field;
 import org.apache.avro.Schema.Type;
 
 /**
  * Represents the type of a given view.
  * @author nick &lt;palmer@cs.vu.nl&gt;
  *
  */
 class AvroViewType {
 
 	/**
 	 * A prime we use in hashing.
 	 */
 	private static final int	HASH_PRIME	= 71;
 
 	/**
 	 * The Avro Type this builder knows how to build.
 	 */
 	private final Type mType;
 
 	/**
 	 * The ui.widget this view builder knows how to build.
 	 */
 	private final String mWidget;
 
 	/**
 	 * Constructs a new view type represented by the given field.
 	 * @param field the field to represent
 	 */
 	public AvroViewType(final Field field) {
 		mType = field.schema().getType();
		mWidget = field.getProp(AvroSchemaProperties.UI_WIDGET);
 	}
 
 	/**
 	 * @return the type for this ViewType
 	 */
 	public final Type getType() {
 		return mType;
 	}
 
 	/**
 	 * @return the widget for this ViewType. May be null.
 	 */
 	public final String getWidget() {
 		return mWidget;
 	}
 
 	/**
 	 * Constructs a new view type represented by the given parameters.
 	 * @param type the type
 	 * @param widget the widget type or null
 	 */
 	public AvroViewType(final Type type, final String widget) {
 		mType = type;
 		mWidget = widget;
 	}
 
 	/**
 	 * Constructs a new view type represented by the given type
 	 * with a null widget.
 	 * @param type the type to support
 	 */
 	public AvroViewType(final Type type) {
 		mType = type;
 		mWidget = null;
 	}
 
 	/**
 	 * Construct from only a schema.
 	 * @param schema the schema to repreent the type of
 	 */
 	public AvroViewType(final Schema schema) {
 		mType = schema.getType();
 		mWidget = schema.getProp(AvroSchemaProperties.UI_WIDGET);
 	}
 
 	/**
 	 * @see Object.equals()
 	 * @param o the object to compare to
 	 * @return true if they are equal
 	 */
 	@Override
 	public final boolean equals(final Object o) {
 		try {
 			AvroViewType other = (AvroViewType) o;
 
 			if (mType.equals(other.mType)
 				&& ((mWidget == null && other.mWidget == null)
 					|| (mWidget != null
 						&& mWidget.equals(other.mWidget)))) {
 					return true;
 				}
 		} catch (Exception e) {
 			return false;
 		}
 		return false;
 	}
 
 	/**
 	 * @see Object.hashCode()
 	 * @return the hash code
 	 */
 	@Override
 	public final int hashCode() {
 		if (mWidget == null) {
 			return mType.hashCode();
 		} else {
 			return mType.hashCode() + (HASH_PRIME * mWidget.hashCode());
 		}
 	}
 
 	@Override
 	public final String toString() {
		return "Type: " + mType + " : " + mWidget;
 	}
 }
