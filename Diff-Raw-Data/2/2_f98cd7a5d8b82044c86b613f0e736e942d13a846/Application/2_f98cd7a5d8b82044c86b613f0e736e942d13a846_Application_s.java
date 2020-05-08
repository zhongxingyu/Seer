 package no.automouse;
 
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.Robot;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 public class Application {
 	public static void main(String... args) throws Exception {
 		Map<String, Integer> prop = argumentsToMap(args);
 		System.out.println("Parameters can be changed with key:value arguments.");
 		System.out.println("Current parameters:");
 		System.out.println(prop);
 
 		Robot robot = new Robot();
 		Random random = new Random();
 		Point oldMouseLocation = MouseInfo.getPointerInfo().getLocation();
 		while (true) {
 			Point newMouseLocation = MouseInfo.getPointerInfo().getLocation();
 			int currentHour = new GregorianCalendar().get(Calendar.HOUR_OF_DAY);
			if (oldMouseLocation.equals(newMouseLocation) && prop.get("start") < currentHour
 					&& currentHour < prop.get("stop")) {
 				int x = prop.get("dx") * random.nextInt(2) - prop.get("dx") / 2 + oldMouseLocation.x;
 				int y = prop.get("dy") * random.nextInt(2) - prop.get("dy") / 2 + oldMouseLocation.y;
 				robot.mouseMove(x, y);
 			}
 			oldMouseLocation = MouseInfo.getPointerInfo().getLocation();
 			Thread.sleep(prop.get("dt"));
 		}
 	}
 
 	public static Map<String, Integer> argumentsToMap(String... arguments) {
 		Map<String, Integer> map = defaultArgs();
 		for (String string : arguments) {
 			String[] split = string.split(":");
 			map.put(split[0], Integer.parseInt(split[1]));
 		}
 		return map;
 	}
 
 	private static Map<String, Integer> defaultArgs() {
 		HashMap<String, Integer> arguments = new HashMap<String,Integer>();
 		arguments.put("dt", 60000);
 		arguments.put("dx", 2);
 		arguments.put("dy", 2);
 		arguments.put("start", 8);
 		arguments.put("stop", 17);
 		return arguments;
 	}
 }
