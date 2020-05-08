 package engine;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import util.FloatOption;
 import util.IntOption;
 
 public class Options {
 	public static final int DEBUG_STRIP = 52;
 
 	public boolean SUPERVISOR_SIGNAL = false;
 	public boolean SEQUENTIAL_LEARNING = false;
 	public int ROUND_TIME = 10 * 10 * 30;
 
	@IntOption(min = 1, max = 3)
 	public int LAYERS = 2;
 
 	@IntOption(min = 1, max = 10)
 	public int LEARN_ROUNDS = 4;
 
 	@IntOption(min = 1, max = 40)
 	public int NEURON_ACTIVATION_THRESHOLD = 10;
 
 	@IntOption(min = 1, max = 8)
 	public int NEURON_CELLS = 2;
 
 	@IntOption(min = 1, max = 40)
 	public int NEURON_MIN_THRESHOLD = 3;
 
 	@IntOption(min = 1, max = 40)
 	public int NEURON_NEW_SYNAPSES = 20;
 
 	@IntOption(min = 1, max = 100)
 	public int NEURON_PERMANENCE_CONNECTED = 60;
 
 	@IntOption(min = 1, max = 100)
 	public int NEURON_PERMANENCE_DEC = 15;
 
 	@IntOption(min = 1, max = 100)
 	public int NEURON_PERMANENCE_INC = 20;
 
 	@IntOption(min = 1, max = 100)
 	public int NEURON_PERMANENCE_INITIAL = 20;
 
 	@IntOption(min = 16, max = 1024)
 	public int SENSORS = 1024;
 
 	@IntOption(min = 1, max = 25)
 	public int SENSORS_RADIUS = 4;
 
 	@FloatOption(min = 0.1, max = 4)
 	public double SENSORS_ZONE_DISTANCE = 0.9;
 
 	@IntOption(min = 1, max = 10)
 	public int SENSORS_WINNERS = 10;
 
 	@IntOption(min = 1, max = 10)
 	public int SENSORS_ZONE_WINNERS = 3;
 
 	@FloatOption(min = 0, max = 20)
 	public double SENSOR_BOOST = 2;
 
 	@IntOption(min = 100, max = 10000)
 	public int SENSOR_BOOST_TIME = 1000;
 
 	@IntOption(min = 1, max = 255 * 32 * 32 / 10)
 	public int SENSOR_ZONE_OVERLAP = 1500;
 
 	@IntOption(min = 1, max = 255 * 32 * 32 / 10)
 	public int SENSOR_FIELD_OVERLAP = 1500;
 
 	@IntOption(min = 1, max = 100)
 	public int SENSOR_PERMANENCE_CONNECTED = 70;
 
 	@IntOption(min = 1, max = 100)
 	public int SENSOR_PERMANENCE_DEC = 7;
 
 	@IntOption(min = 1, max = 100)
 	public int SENSOR_PERMANENCE_INC = 10;
 
 	public int MAX_SAMPLES = 16;
 
 	public int SENSOR_ZONE_MULTIPLIER = 65536;
 
 //	@IntOption(min = 1, max = 100)
 //	public int SENSOR_PERMANENCE_INITIAL = 10;
 
 	static Map<Class<?>, String> availableTypes = new HashMap<Class<?>, String>();
 	static {
 		availableTypes.put(int.class, "int");
 		availableTypes.put(double.class, "float");
 		availableTypes.put(boolean.class, "boolean");
 	}
 
 	private static String fieldInfo(Field field) {
 		String type = availableTypes.get(field.getType());
 		if (type == null) {
 			throw new IllegalArgumentException("Unsupported field " + field.getName() + " of type "
 					+ field.getType());
 		}
 		for (Annotation ann : field.getAnnotations()) {
 			if (ann instanceof IntOption) {
 				IntOption opt = (IntOption) ann;
 				if (!type.equals("int")) {
 					throw new IllegalArgumentException("Can't use IntField on " + field.getName()
 							+ " of type " + field.getType());
 				}
 				type = "int (" + opt.min() + " .. " + opt.max() + ")";
 			} else if (ann instanceof FloatOption) {
 				FloatOption opt = (FloatOption) ann;
 				if (!type.equals("float")) {
 					throw new IllegalArgumentException("Can't use FloatField on " + field.getName()
 							+ " of type " + field.getType());
 				}
 				type = "float (" + opt.min() + " .. " + opt.max() + ")";
 			}
 		}
 		return type;
 	}
 
 	public String[] fields() {
 		ArrayList<String> output = new ArrayList<String>();
 		for (Field field : Options.class.getDeclaredFields()) {
 			String name = field.getName();
 			if (isValidName(name)) {
 				Object value = null;
 				try {
 					value = field.get(this);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				output.add(name + ": " + fieldInfo(field) + " = " + value);
 			}
 		}
 		return output.toArray(new String[0]);
 	}
 
 	public int learnTime() {
 		return LEARN_ROUNDS * ROUND_TIME;
 	}
 
 	public double boostTimeRev() {
 		return 1. / SENSOR_BOOST_TIME;
 	}
 
 	private static boolean isValidName(String name) {
 		return name.equals(name.toUpperCase());
 	}
 
 	public void setOption(String key, String value) throws IllegalArgumentException, SecurityException,
 			IllegalAccessException, NoSuchFieldException {
 		Field field = this.getClass().getField(key);
 		String type = availableTypes.get(field.getType());
 		if ("int".equals(type)) {
 			int intValue = Integer.parseInt(value);
 			checkIntValue(field, intValue);
 			field.setInt(this, intValue);
 		} else if ("float".equals(type)) {
 			double doubleValue = Double.parseDouble(value);
 			checkDoubleValue(field, doubleValue);
 			field.setDouble(this, doubleValue);
 		} else if ("bool".equals(type)) {
 			String bool = value.toLowerCase();
 			if ("1".equals(bool) || "true".equals(bool)) {
 				field.setBoolean(this, true);
 			} else if ("0".equals(bool) || "false".equals(bool)) {
 				field.setBoolean(this, false);
 			} else {
 				throw new IllegalArgumentException("Value for field " + field.getName() + " of type "
 						+ field.getType() + " is out of range!");
 			}
 		} else {
 			throw new IllegalArgumentException("Unsupported field " + field.getName() + " of type "
 					+ field.getType());
 		}
 	}
 
 	private void checkIntValue(Field field, int intValue) {
 		for (Annotation ann : field.getAnnotations()) {
 			if (ann instanceof IntOption) {
 				IntOption opt = (IntOption) ann;
 				if (intValue < opt.min() || intValue > opt.max()) {
 					throw new IllegalArgumentException("Value for field " + field.getName() + " of type "
 							+ field.getType() + " is out of range!");
 				}
 			}
 		}
 	}
 
 	private void checkDoubleValue(Field field, double doubleValue) {
 		for (Annotation ann : field.getAnnotations()) {
 			if (ann instanceof FloatOption) {
 				FloatOption opt = (FloatOption) ann;
 				if (doubleValue < opt.min() || doubleValue > opt.max()) {
 					throw new IllegalArgumentException("Value for field " + field.getName() + " of type "
 							+ field.getType() + " is out of range!");
 				}
 			}
 		}
 	}
 
 	@Override
 	public String toString() {
 		return configuration();
 	}
 
 	private String configuration() {
 		StringBuilder sb = new StringBuilder();
 		for (Field field : this.getClass().getDeclaredFields()) {
 			String name = field.getName();
 			if (isValidName(name)) {
 				String type = fieldInfo(field);
 				Object value = "(error)";
 				try {
 					value = field.get(this);
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				}
 				sb.append(name).append(": ");
 				sb.append(type).append(" = ");
 				sb.append(value);
 				sb.append("\n");
 			}
 		}
 		return sb.toString();
 	}
 }
