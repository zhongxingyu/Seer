 package vooga.fighter.view;
 
 import java.awt.Color;
 
 /**
  * Sets the color of a HUD Values. Can change a value's paint color from
  * startColor to endColor based on the ints maxValue and currentValue. For
  * example, a player's health may be displayed in green when at full health, and
  * change to red as player's health depletes.
  * 
  * @author Bill Muensterman
  * 
  */
 public class HUDPlayerValueColor extends HUDPlayerValue {
 
 	/**
 	 * Sets the color of a HUD value. Calls on maxValueToRangeRatio and
 	 * setRGBValues.
 	 * 
 	 * @param maxValue
 	 * @param currentValue
 	 * @param startColor
 	 * @param endColor
 	 * @return
 	 */
 	public Color setValueColor(int maxValue, int currentValue,
 			Color startColor, Color endColor) {
 
 		int ratio = maxValueToRangeRatio(maxValue, currentValue);
 
 		int[] RGB = setRGBValues(startColor, endColor, ratio);
 
 		Color c = new Color(RGB[0], RGB[1], RGB[2]);
 		return c;
 	}
 
 	/**
 	 * Calculates the ratio of the maximum value to the range between the
 	 * maximum and current values.
 	 * 
 	 * @param maxValue
 	 * @param currentValue
 	 * @return
 	 */
 	public int maxValueToRangeRatio(int maxValue, int currentValue) {
 		int ratio;
 		if (maxValue == currentValue) {
			ratio = 1;
 		} else {
 			ratio = maxValue / (maxValue - currentValue);
 		}
 		return ratio;
 	}
 
 	/**
 	 * Fills an int array with a Red value, a Green value, and a Blue value to
 	 * be used in making a new color.
 	 * 
 	 * @param startColor
 	 * @param endColor
 	 * @param maxValueToRangeRatio
 	 * @return
 	 */
 	public int[] setRGBValues(Color startColor, Color endColor,
 			int maxValueToRangeRatio) {
 
 		int[] RGB = new int[3];
 
 		int differenceInRedValues = (startColor.getRed() - endColor.getRed())
 				/ maxValueToRangeRatio;
 		int differenceInGreenValues = (startColor.getGreen() - endColor
 				.getGreen()) / maxValueToRangeRatio;
 		int differenceInBlueValues = (startColor.getBlue() - endColor.getBlue())
 				/ maxValueToRangeRatio;
 
 		RGB[0] = startColor.getRed() - differenceInRedValues;
 		RGB[1] = startColor.getGreen() - differenceInGreenValues;
 		RGB[2] = startColor.getBlue() - differenceInBlueValues;
 
 		return RGB;
 	}
 
 }
