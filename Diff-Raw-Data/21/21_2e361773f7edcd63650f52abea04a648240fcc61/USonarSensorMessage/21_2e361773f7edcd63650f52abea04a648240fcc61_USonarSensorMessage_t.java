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
 public class USonarSensorMessage extends UsarSimSensorMessage {
 	
 	private String type;
 
 	private double time;
 	
 	private double[] data = new double[8];
 
 	public USonarSensorMessage(UsarSimMessage msg) throws Exception {
 		super(msg.getMessageString());
 		this.parseMessage();
 	}
 
 	public USonarSensorMessage(String string) throws Exception {
 		super(string);
 		this.parseMessage();
 	}
 
     public AP2DXMessage toAp2dxMessage()
     {
         SonarSensorMessage sonarSensorMessage = 
             new SonarSensorMessage(Module.COORDINATOR, Module.SENSOR);
         sonarSensorMessage.setTime(time);   
         sonarSensorMessage.setRangeArray(data);
        sonarSensorMessage.compileMessage();
         return sonarSensorMessage;
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
 	 * @param time the time to set
 	 */
 	public void setTime(double time) {
 		this.time = time;
 	}
 
 	/**
 	 * @return the time
 	 */
 	public double getTime() {
 		return time;
 	}
 
 	/**
 	 * @param data the data to set
 	 */
 	public void setData(double[] data) {
 		this.data = data;
 	}
 
 	/**
 	 * @return the data
 	 */
 	public double[] getData() {
 		return data;
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
 
 		String timePatternStr = "\\{Time ([a-zA-Z0-9,._\\-]+)\\}";
 		Pattern timePattern = Pattern.compile(timePatternStr);
 		Matcher timeMatcher = timePattern.matcher(this.getMessageString());
 
 		if (timeMatcher.find()) {
 			String time = timeMatcher.group(1);
 			this.setTime(Double.parseDouble(time));
 
 		}
 
 		String groupPatternStr = "\\{Name ([\\w\\d]+) Range ([0-9.,\\-_]+)\\}";
 		Pattern groupPattern = Pattern.compile(groupPatternStr);
 		Matcher groupMatcher = groupPattern.matcher(this.getMessageString());
 		
 		double[] data = new double[8];
 		int i = 0;
 		
 		while (groupMatcher.find()) {
 			if (data.length <= i) {
 				double[] data2 = new double[i*2];
 				for(int j = 0; j < data.length; j++) {
 					data2[j] = data[j];
 					i = j;
 				}
 				data = data2;
 			}
 			double value = Double.parseDouble(groupMatcher.group(2));
 			data[i] = value;
 			i++;
 		}
 		
 		this.setData(data);
 	}
 }
