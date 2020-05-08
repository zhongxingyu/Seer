 package pleocmd.pipe.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import pleocmd.pipe.val.FloatValue;
 import pleocmd.pipe.val.StringValue;
 import pleocmd.pipe.val.Value;
 import pleocmd.pipe.val.ValueType;
 
 public class MultiValueData extends Data {
 
 	public static final String IDENT = "Multi";
 
 	private static List<Value> l;
 
 	public MultiValueData(final double[] values, final Data parent,
 			final byte priority, final long time) {
 		super(l = new ArrayList<Value>(1 + values.length), parent, priority,
 				time, true);
 		Value val = Value.createForType(ValueType.NullTermString);
 		((StringValue) val).set(IDENT);
 		l.add(val);
 		for (final double v : values) {
 			val = Value.createForType(ValueType.Float64);
 			((FloatValue) val).set(v);
 			l.add(val);
 		}
 	}
 
 	public MultiValueData(final double[] values, final Data parent) {
 		super(l = new ArrayList<Value>(1 + values.length), parent,
 				PRIO_DEFAULT, TIME_NOTIME, true);
 		Value val = Value.createForType(ValueType.NullTermString);
 		((StringValue) val).set(IDENT);
 		l.add(val);
 		for (final double v : values) {
 			val = Value.createForType(ValueType.Float64);
 			((FloatValue) val).set(v);
 			l.add(val);
 		}
 	}
 
 	public MultiValueData(final Data parent) {
		super(l = new ArrayList<Value>(1 + parent.size()), parent);
 		final Value val = Value.createForType(ValueType.NullTermString);
 		((StringValue) val).set(IDENT);
 		l.add(val);
 		for (final Value pv : parent)
 			l.add(pv);
 	}
 
 	public static boolean isMultiValueData(final Data data) {
 		return IDENT.equals(data.getSafe(0).asString());
 	}
 
 	public static int getValueCount(final Data data) {
 		return data.size() - 1;
 	}
 
 	public static double getValue(final Data data, final int index) {
 		return data.get(index + 1).asDouble();
 	}
 
 }
