 /**
  * 
  */
 package AP2DX.usarsim.specialized;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import AP2DX.usarsim.UsarSimMessage;
 
 /**
  * Not nessecary to convert this type to a new Usarsim string.
  * @author Jasper Timmer
  * 
  */
 public class UOdometrySensorMessage extends UsarSimSensorMessage {
 	
 	private String type;
 	private String name;
 	private double[] Pose;
 
 	public UOdometrySensorMessage(UsarSimMessage msg) throws Exception {
 		super(msg.getMessageString());
 		this.parseMessage();
 	}
 
 	public UOdometrySensorMessage(String string) throws Exception {
 		super(string);
 		this.parseMessage();
 	}
 
 	/**
 	 * @see AP2DX.usarsim.specialized.SensorMessage#parseMessage() Parses all
 	 *      the fields in the message and sets the values of the fields with the
 	 *      same name to the values.
 	 * @throws IllegalAccessException
 	 * @throws IllegalArgumentException
 	 * @see AP2DX.Message#parseMessage()
 	 */
 	@Override
 	public void parseMessage() throws IllegalArgumentException,
 			IllegalAccessException {
 		String typePatternStr = "\\{Type ([a-zA-Z0-9,._\\-]+)\\}";
 		Pattern typePattern = Pattern.compile(typePatternStr);
 		Matcher typeMatcher = typePattern.matcher(this.getMessageString());
 
 		if (typeMatcher.find()) {
 			String type = typeMatcher.group(1);
 			this.setType(type);
 		}
 
 		String namePatternStr = "\\{Name ([a-zA-Z0-9,._\\-]+)\\}";
 		Pattern namePattern = Pattern.compile(namePatternStr);
 		Matcher nameMatcher = namePattern.matcher(this.getMessageString());
 
 		if (nameMatcher.find()) {
 			String name = nameMatcher.group(1);
 			this.setName(name);
 
 		}
 		
 		String posPatternStr = "\\{Pose ([0-9.,\\-_]+)\\}";
 		Pattern posPattern = Pattern.compile(posPatternStr);
 		Matcher posMatcher = posPattern.matcher(this.getMessageString());
 
 		double[] data = null;
 		if (posMatcher.find()) {
 			String[] loc = posMatcher.group(1).split(",");
 			data = new double[loc.length];
 
 			for (int i = 0; i < data.length; i++) {
 				data[i] = Double.parseDouble(loc[i]);
 			}
 			
 		}
 		this.setPose(data);
 	}
 
 	/**
 	 * @param type the type to set
 	 */
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	/**
 	 * @return the type
 	 */
 	public String getType() {
 		return type;
 	}
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param pose the pose to set
 	 */
 	public void setPose(double[] pose) {
 		Pose = pose;
 	}
 
 	/**
 	 * @return the pose
 	 */
 	public double[] getPose() {
 		return Pose;
 	}
 }
