 package pleocmd.pipe.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import pleocmd.pipe.val.StringValue;
 import pleocmd.pipe.val.Value;
 import pleocmd.pipe.val.ValueType;
 
 public class CommandData extends Data {
 
 	private static List<Value> l;
 
 	public CommandData(final String command, final String argument,
 			final Data parent, final byte priority, final long time) {
		super(l = new ArrayList<Value>(2), parent, priority, time);
 		Value val = Value.createForType(ValueType.NullTermString);
 		((StringValue) val).set(command);
 		l.add(val);
 		val = Value.createForType(ValueType.NullTermString);
 		((StringValue) val).set(argument);
 		l.add(val);
 	}
 
 	public CommandData(final String command, final String argument,
 			final Data parent) {
		super(l = new ArrayList<Value>(2), parent);
 		Value val = Value.createForType(ValueType.NullTermString);
 		((StringValue) val).set(command);
 		l.add(val);
 		val = Value.createForType(ValueType.NullTermString);
 		((StringValue) val).set(argument);
 		l.add(val);
 	}
 
 	public static boolean isCommandData(final Data data) {
 		return data.size() >= 2 && data.get(0) instanceof StringValue;
 	}
 
 	public static boolean isCommandData(final Data data, final String command) {
 		return data.size() >= 2 && data.get(0) instanceof StringValue
 				&& command.equals(data.get(0).asString());
 	}
 
 	public static String getCommand(final Data data) {
 		return data.get(0).asString();
 	}
 
 	public static String getArgument(final Data data) {
 		return data.get(1).asString();
 	}
 
 }
