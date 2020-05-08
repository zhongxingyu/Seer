 package pleocmd.pipe.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import pleocmd.pipe.val.IntValue;
 import pleocmd.pipe.val.StringValue;
 import pleocmd.pipe.val.Value;
 import pleocmd.pipe.val.ValueType;
 
 class SingleValueData extends Data {
 
 	private static List<Value> l;
 
 	protected SingleValueData(final String ident, final Value value,
 			final long user, final Data parent, final byte priority,
 			final long time) {
 		super(l = new ArrayList<Value>(3), parent, priority, time, CTOR_DIRECT);
 		init(ident, value, user);
 	}
 
 	protected SingleValueData(final String ident, final Value value,
 			final long user, final Data parent) {
 		super(l = new ArrayList<Value>(3), parent, CTOR_DIRECT);
 		init(ident, value, user);
 	}
 
 	private static void init(final String ident, final Value value,
 			final long user) {
 		final Value valIdent = Value.createForType(ValueType.NullTermString);
 		((StringValue) valIdent).set(ident);
 		final Value valUser = Value.createForType(ValueType.Int64);
 		((IntValue) valUser).set(user);
 		l.add(valIdent);
 		l.add(value);
 		l.add(valUser);
 	}
 
 	public static Value getValueRaw(final Data data) {
 		return data.get(1);
 	}
 
 	public static long getUser(final Data data) {
		return data.size() < 3 ? 0 : data.get(2).asLong();
 	}
 
 }
