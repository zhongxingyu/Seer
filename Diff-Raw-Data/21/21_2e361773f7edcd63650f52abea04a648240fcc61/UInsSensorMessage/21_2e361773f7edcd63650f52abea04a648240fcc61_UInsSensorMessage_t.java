 /**
  * 
  */
 package AP2DX.usarsim.specialized;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import AP2DX.usarsim.UsarSimMessage;
 import AP2DX.*;
 import AP2DX.specializedMessages.*;
 
 /**
  * Not nessecary to convert this type to a new Usarsim string.
  * @author Jasper Timmer
  * 
  */
 public class UInsSensorMessage extends UsarSimSensorMessage {
 	
 	private String type;
 	private String name;
 	private double[] location;
 	private double[] orientation;
 
 	public UInsSensorMessage(UsarSimMessage msg) throws Exception {
 		super(msg.getMessageString());
 		this.parseMessage();
 	}
 
 	public UInsSensorMessage(String string) throws Exception {
 		super(string);
 		this.parseMessage();
 	}
 
 
     public AP2DXMessage toAp2dxMessage()
     {
         InsSensorMessage insSensorMessage = 
             new InsSensorMessage(Module.COORDINATOR, Module.SENSOR);
         insSensorMessage.setOrientation(orientation);
         insSensorMessage.setLocation(location);
        insSensorMessage.compileMessage();
         return insSensorMessage;
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
 		
 		String locPatternStr = "\\{Location ([0-9.,\\-_]+)\\}";
 		Pattern locPattern = Pattern.compile(locPatternStr);
 		Matcher locMatcher = locPattern.matcher(this.getMessageString());
 
 		double[] data = null;
 		if (locMatcher.find()) {
 			String[] loc = locMatcher.group(1).split(",");
 			data = new double[loc.length];
 
 			for (int i = 0; i < data.length; i++) {
 				data[i] = Double.parseDouble(loc[i]);
 			}
 			
 		}
 		this.setLocation(data);
 		
 		String oriPatternStr = "\\{Orientation ([0-9.,\\-_]+)\\}";
 		Pattern oriPattern = Pattern.compile(oriPatternStr);
 		Matcher oriMatcher = oriPattern.matcher(this.getMessageString());
 		
 		double[] data2 = null;
 		if (oriMatcher.find()) {
 			String[] ori = oriMatcher.group(1).split(",");
 			data2 = new double[ori.length];
 
 			for (int i = 0; i < data2.length; i++) {
 				data2[i] = Double.parseDouble(ori[i]);
 			}
 		}
 		this.setOrientation(data2);
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
 	 * @param location the location to set
 	 */
 	public void setLocation(double[] location) {
 		this.location = location;
 	}
 
 	/**
 	 * @return the location
 	 */
 	public double[] getLocation() {
 		return location;
 	}
 
 	/**
 	 * @param orientation the orientation to set
 	 */
 	public void setOrientation(double[] orientation) {
 		this.orientation = orientation;
 	}
 
 	/**
 	 * @return the orientation
 	 */
 	public double[] getOrientation() {
 		return orientation;
 	}
 }
