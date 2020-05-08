 package net.minecraft.src.wirelessredstone.smp.network.packet;
 
 import java.util.Arrays;
 
 import net.minecraft.src.wirelessredstone.data.LoggerRedstoneWireless;
 import net.minecraft.src.wirelessredstone.smp.IndexInPayload;
 
 /**
  * Payload for data transfer in packets
  * 
  * @author Eurymachus
  * 
  */
 public class PacketPayload {
 	public static <T> T[] concat(T[] first, T[] second) {
 		T[] result = Arrays.copyOf(first, first.length + second.length);
 		System.arraycopy(second, 0, result, first.length, second.length);
 		return result;
 	}
 
 	public static int[] concat(int[] first, int[] second) {
 		int[] result = Arrays.copyOf(first, first.length + second.length);
 		System.arraycopy(second, 0, result, first.length, second.length);
 		return result;
 	}
 
 	public static float[] concat(float[] first, float[] second) {
 		float[] result = Arrays.copyOf(first, first.length + second.length);
 		System.arraycopy(second, 0, result, first.length, second.length);
 		return result;
 	}
 
 	public static boolean[] concat(boolean[] first, boolean[] second) {
 		boolean[] result = Arrays.copyOf(first, first.length + second.length);
 		System.arraycopy(second, 0, result, first.length, second.length);
 		return result;
 	}
 
 	/**
 	 * Array of int values
 	 */
 	private int[] intPayload;
 
 	/**
 	 * Array of float values
 	 */
 	private float[] floatPayload;
 
 	/**
 	 * Array of String values
 	 */
 	private String[] stringPayload;
 
 	/**
 	 * Array of boolean values
 	 */
 	private boolean[] boolPayload;
 
 	public PacketPayload() {
 	}
 
 	/**
 	 * Retrieves the intPayload size
 	 * 
 	 * @return intPayload.length or 0 if null
 	 */
 	public int getIntSize() {
 		if (this.intPayload != null)
 			return this.intPayload.length;
 		LoggerRedstoneWireless.getInstance(
 				LoggerRedstoneWireless.filterClassName(this.getClass()
 						.toString())).write("getIntSize(): null or OOB!",
 				LoggerRedstoneWireless.LogLevel.WARNING);
 		return 0;
 	}
 
 	/**
 	 * Retrieves the floatPayload size
 	 * 
 	 * @return floatPayload.length or 0 if null
 	 */
 	public int getFloatSize() {
 		if (this.floatPayload != null)
 			return this.floatPayload.length;
 		LoggerRedstoneWireless.getInstance(
 				LoggerRedstoneWireless.filterClassName(this.getClass()
 						.toString())).write("getFloatSize(): null or OOB!",
 				LoggerRedstoneWireless.LogLevel.WARNING);
 		return 0;
 	}
 
 	/**
 	 * Retrieves the stringPayload size
 	 * 
 	 * @return stringPayload.length or 0 if null
 	 */
 	public int getStringSize() {
 		if (this.stringPayload != null)
 			return this.stringPayload.length;
 		LoggerRedstoneWireless.getInstance(
 				LoggerRedstoneWireless.filterClassName(this.getClass()
 						.toString())).write("getStringSize(): null or OOB!",
 				LoggerRedstoneWireless.LogLevel.WARNING);
 		return 0;
 	}
 
 	/**
 	 * Retrieves the boolPayload size
 	 * 
 	 * @return boolPayload.length or 0 if null
 	 */
 	public int getBoolSize() {
 		if (this.boolPayload != null)
 			return this.boolPayload.length;
 		LoggerRedstoneWireless.getInstance(
 				LoggerRedstoneWireless.filterClassName(this.getClass()
 						.toString())).write("getBoolSize(): null or OOB!",
 				LoggerRedstoneWireless.LogLevel.WARNING);
 		return 0;
 	}
 
 	/**
 	 * Adds a new int value to intPayload
 	 * 
 	 * @param index
 	 *            The index in the array
 	 * @param newInt
 	 *            The value to be added
 	 * @return true if successful or false if unsuccessful
 	 */
 	public boolean setIntPayload(int index, int newInt) {
		if (this.intPayload != null && index < this.getFloatSize()) {
 			this.intPayload[index] = newInt;
 			return true;
 		}
 		LoggerRedstoneWireless.getInstance(
 				LoggerRedstoneWireless.filterClassName(this.getClass()
 						.toString())).write(
 				"setIntPayload(" + index + ", " + newInt + "): null or OOB!",
 				LoggerRedstoneWireless.LogLevel.WARNING);
 		return false;
 	}
 
 	/**
 	 * Adds a new float value to floatPayload
 	 * 
 	 * @param index
 	 *            The index in the array
 	 * @param newFloat
 	 *            The value to be added
 	 * @return true if successful or false if unsuccessful
 	 */
 	public boolean setFloatPayload(int index, float newFloat) {
 		if (this.floatPayload != null && index < this.getFloatSize()) {
 			this.floatPayload[index] = newFloat;
 			return true;
 		}
 		LoggerRedstoneWireless.getInstance(
 				LoggerRedstoneWireless.filterClassName(this.getClass()
 						.toString())).write(
 				"setFloatPayload(" + index + ", " + newFloat
 						+ "): null or OOB!",
 				LoggerRedstoneWireless.LogLevel.WARNING);
 		return false;
 	}
 
 	/**
 	 * Adds a new String value to stringPayload
 	 * 
 	 * @param index
 	 *            The index in the array
 	 * @param newString
 	 *            The value to be added
 	 * @return true if successful or false if unsuccessful
 	 */
 	public boolean setStringPayload(int index, String newString) {
 		if (this.stringPayload != null && index < this.getStringSize()) {
 			this.stringPayload[index] = newString;
 			return true;
 		}
 		LoggerRedstoneWireless.getInstance(
 				LoggerRedstoneWireless.filterClassName(this.getClass()
 						.toString())).write(
 				"setStringPayload(" + index + ", " + newString
 						+ "): null or OOB!",
 				LoggerRedstoneWireless.LogLevel.WARNING);
 		return false;
 	}
 
 	/**
 	 * Adds a new boolean value to boolPayload
 	 * 
 	 * @param index
 	 *            The index in the array
 	 * @param newBool
 	 *            The value to be added
 	 * @return true if successful or false if unsuccessful
 	 */
 	public boolean setBoolPayload(int index, boolean newBool) {
 		if (this.boolPayload != null && index < this.getBoolSize()) {
 			this.boolPayload[index] = newBool;
 			return true;
 		}
 		LoggerRedstoneWireless.getInstance(
 				LoggerRedstoneWireless.filterClassName(this.getClass()
 						.toString())).write(
 				"setBoolPayload(" + index + ", " + newBool + "): null or OOB!",
 				LoggerRedstoneWireless.LogLevel.WARNING);
 		return false;
 	}
 
 	/**
 	 * Retrieves an int value stored in intPayload
 	 * 
 	 * @param index
 	 *            The index in the array
 	 * @return intPayload[index] or 0 if null
 	 */
 	public int getIntPayload(int index) {
 		if (this.intPayload != null && index < this.getIntSize())
 			return this.intPayload[index];
 		LoggerRedstoneWireless.getInstance(
 				LoggerRedstoneWireless.filterClassName(this.getClass()
 						.toString())).write(
 				"getIntPayload(" + index + "): null or OOB!",
 				LoggerRedstoneWireless.LogLevel.WARNING);
 		return 0;
 	}
 
 	/**
 	 * Retrieves a float value stored in floatPayload
 	 * 
 	 * @param index
 	 *            The index in the array
 	 * @return floatPayload[index] or 0 if null
 	 */
 	public float getFloatPayload(int index) {
 		if (this.floatPayload != null && index < this.getFloatSize())
 			return this.floatPayload[index];
 		LoggerRedstoneWireless.getInstance(
 				LoggerRedstoneWireless.filterClassName(this.getClass()
 						.toString())).write(
 				"getFloatPayload(" + index + "): null or OOB!",
 				LoggerRedstoneWireless.LogLevel.WARNING);
 		return 0;
 	}
 
 	/**
 	 * Retrieves a String value stored in stringPayload
 	 * 
 	 * @param index
 	 *            The index in the array
 	 * @return stringPayload[index] or "null" if null
 	 */
 	public String getStringPayload(int index) {
 		if (this.stringPayload != null && index < this.getStringSize()
 				&& this.stringPayload[index] != null)
 			return this.stringPayload[index];
 		LoggerRedstoneWireless.getInstance(
 				LoggerRedstoneWireless.filterClassName(this.getClass()
 						.toString())).write(
 				"getStringPayload(" + index + "): null or OOB!",
 				LoggerRedstoneWireless.LogLevel.WARNING);
 		return "null";
 	}
 
 	/**
 	 * Retrieves a boolean value stored in boolPayload
 	 * 
 	 * @param index
 	 *            The index in the array
 	 * @return boolPayload[index] or false if null
 	 */
 	public boolean getBoolPayload(int index) {
 		if (this.boolPayload != null && index < this.getBoolSize())
 			return this.boolPayload[index];
 		LoggerRedstoneWireless.getInstance(
 				LoggerRedstoneWireless.filterClassName(this.getClass()
 						.toString())).write(
 				"getBoolPayload(" + index + "): null or OOB!",
 				LoggerRedstoneWireless.LogLevel.WARNING);
 		return false;
 	}
 
 	/**
 	 * Constructor Create a new PacketPayload
 	 * 
 	 * @param intSize
 	 *            The size of the new intPayload array
 	 * @param floatSize
 	 *            The size of the new floatPayload array
 	 * @param stringSize
 	 *            The size of the new stringPayload array
 	 * @param boolSize
 	 *            The size of the new boolPayload array
 	 */
 	public PacketPayload(int intSize, int floatSize, int stringSize,
 			int boolSize) {
 		this.intPayload = new int[intSize];
 		this.floatPayload = new float[floatSize];
 		this.stringPayload = new String[stringSize];
 		this.boolPayload = new boolean[boolSize];
 	}
 
 	public void append(PacketPayload other) {
 		if (other == null)
 			return;
 
 		if (other.intPayload.length > 0)
 			this.intPayload = concat(this.intPayload, other.intPayload);
 		if (other.floatPayload.length > 0)
 			this.floatPayload = concat(this.floatPayload, other.floatPayload);
 		if (other.stringPayload.length > 0)
 			this.stringPayload = concat(this.stringPayload, other.stringPayload);
 		if (other.boolPayload.length > 0)
 			this.boolPayload = concat(this.boolPayload, other.boolPayload);
 	}
 
 	public void append(int[] other) {
 		if (other == null || other.length < 0)
 			return;
 
 		this.intPayload = concat(this.intPayload, other);
 	}
 
 	public void splitTail(IndexInPayload index) {
 		PacketPayload payload = new PacketPayload(intPayload.length
 				- index.intIndex, floatPayload.length - index.floatIndex,
 				stringPayload.length - index.stringIndex, boolPayload.length
 						- index.boolIndex);
 
 		if (intPayload.length > 0)
 			System.arraycopy(intPayload, index.intIndex, payload.intPayload, 0,
 					payload.intPayload.length);
 		if (floatPayload.length > 0)
 			System.arraycopy(floatPayload, index.floatIndex,
 					payload.floatPayload, 0, payload.floatPayload.length);
 		if (stringPayload.length > 0)
 			System.arraycopy(stringPayload, index.stringIndex,
 					payload.stringPayload, 0, payload.stringPayload.length);
 		if (boolPayload.length > 0)
 			System.arraycopy(boolPayload, index.boolIndex, payload.boolPayload,
 					0, payload.boolPayload.length);
 	}
 }
