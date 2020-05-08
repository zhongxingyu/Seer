 package de.uniluebeck.itm.icontrol.communication;
 
 /* ----------------------------------------------------------------------
  * This file is part of the WISEBED project.
  * Copyright (C) 2009 by the Institute of Telematics, University of Luebeck
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the BSD License. Refer to bsd-licence.txt
  * file in the root of the source tree for further details.
  ------------------------------------------------------------------------*/
 
 /**
  * @class Gateway
  * @author Moehlmann, Schneider, Witt
  * @brief Is responsible for the communication with the robots/sensors
  * @detailed This class is for sending and  receiving messages, also to decode and encode the messages
  */
 
 import ishell.device.MessagePacket;
 import ishell.plugins.Plugin;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 
 import de.uniluebeck.itm.icontrol.communication.listener.FeatureListener;
 import de.uniluebeck.itm.icontrol.communication.listener.MessageListener;
 
 public class Gateway implements Communication {
 
 	private Set<FeatureListener> featureListener;
 	private Set<MessageListener> messageListener;
 	private Plugin ishellPlugin;
 	private final int sendType = 10;
 	private HashMap<Integer, Set<MessagePart>> msgQueue;
 
 	public Gateway(final Plugin ishellPlugin) {
 		this.ishellPlugin = ishellPlugin;
 		featureListener = new HashSet<FeatureListener>();
 		messageListener = new HashSet<MessageListener>();
 		msgQueue = new HashMap<Integer, Set<MessagePart>>();
 	}
 
 	public void addFeatureListener(final FeatureListener listener) {
 		featureListener.add(listener);
 	}
 
 	public void addMessageListener(final MessageListener listener) {
 		messageListener.add(listener);
 	}
 
 	public void removeFeatureListener(final FeatureListener listener) {
 		featureListener.remove(listener);
 	}
 
 	public void removeMessageListener(final MessageListener listener) {
 		messageListener.remove(listener);
 	}
 
 	/**
 	 * Sends a task to the sensor-network
 	 * 
 	 * @param robotId
 	 *            The ID of the robot, which is supposed to receive the message (integer value must
 	 *            not be higher than 2 bytes)
 	 * @param taskName
 	 *            The name of the task, which should be proceeded
 	 * @param paramLength
 	 *            Amount of values in the parameters array(integer value must not be higher than 2
 	 *            bytes)
 	 * @param parameters
 	 *            The values of the parameters (integer values must not be higher than 2 bytes)
 	 */
 	public void doTask(final int robotId, final String taskName, final int paramLength, final int[] parameters) {
 		final byte[] idArray = intToByteArray(robotId, 2);
 		byte[] taskNameArray = null;
 		try {
 			taskNameArray = taskName.getBytes("US-ASCII");
 		} catch (final UnsupportedEncodingException e) {
 		}
 		final byte paramLengthByte = intToByte(paramLength);
 		final byte[] parametersArray = new byte[parameters.length * 2];
 		for (int i = 0; i < parameters.length; i++) {
 			byte[] temp = new byte[2];
 			temp = intToByteArray(parameters[i], 2);
 			parametersArray[i * 2] = temp[0];
 			parametersArray[i * 2 + 1] = temp[1];
 		}
 		final byte terminator = (byte) '\0';
 		final ByteBuffer bb = ByteBuffer.allocate(2 + idArray.length + parametersArray.length + taskNameArray.length + 1);
 		final byte messageType = (byte) 200;
 		bb.put(messageType);
 		bb.put(idArray);
 		bb.put(paramLengthByte);
 		bb.put(parametersArray);
 		bb.put(taskNameArray);
 		bb.put(terminator);
 		ishellPlugin.sendPacket(sendType, bb.array());
 	}
 
 	/**
 	 * Calls every robot, to send the list of available features
 	 */
 	public void showMeWhatYouGot() {
 		final byte[] send = new byte[1];
 		send[0] = (byte) 201;
 		ishellPlugin.sendPacket(sendType, send);
 	}
 
 	/**
 	 * Is called, when a message was received. This methods checks the type and calls the according
 	 * method It reads the first byte which should be the type byte
 	 * 
 	 * @param message
 	 *            The message, which was received
 	 */
 	public void receive(final MessagePacket packet) {
 		if (packet.getType() == sendType) {
 			final byte[] message = packet.getContent();
 			final int type = unsignedSingleByteToInt(message[0]);
 			switch (type) {
 				case (202):
 					onMessage(cutByteArray(message, 1, message.length));
 					break;
 				case (203):
 					checkMessageParts(cutByteArray(message, 1, message.length));
 					break;
 				default:
 			}
 		}
 	}
 
 	/**
 	 * Checks if a received message is complete or just a part of splitted messages. If the message
 	 * is a one-part message onFeature will called, otherwise the message queue (msgQueue) will be
 	 * checked if there are already parts of the message. If all message parts have been received,
 	 * the message parts will be build together and onFeature will be called. If there are still
 	 * parts of the message missing, the current message part will be added to the message queue.
 	 */
 	private void checkMessageParts(final byte[] message) {
 		int pos = 0;
 		final int robotId = unsignedDualByteToInt(cutByteArray(message, pos, pos + 2));
 		pos += 2;
 		final int msgId = unsignedSingleByteToInt(cutByteArray(message, pos, pos + 1)[0]);
 		pos++;
 		final int msgAmount = unsignedSingleByteToInt(cutByteArray(message, pos, pos + 1)[0]);
 		pos++;
 		final int msgNumber = unsignedSingleByteToInt(cutByteArray(message, pos, pos + 1)[0]);
 		pos++;
 		final byte[] byteMsgPart = cutByteArray(message, pos, message.length);
 		if (msgAmount == 1) {
 			// One-part message
 			onFeature(robotId, cutByteArray(message, pos, message.length));
 		} else {
 			final MessagePart msgPart = new MessagePart(robotId, msgId, msgAmount, msgNumber, byteMsgPart);
 			final int hashValue = msgPart.hashCode();
 			Set<MessagePart> msgSet = msgQueue.get(hashValue);
 			if (null == msgSet) {
 				// First message part -> create new message set and add it to the queue
 				msgSet = new TreeSet<MessagePart>();
 				msgSet.add(msgPart);
 				msgQueue.put(hashValue, msgSet);
 			} else if (msgSet.size() == msgAmount - 1) {
 				// All message parts arrived, build complete message
 				// and call onFeature
 				msgSet.add(msgPart);
 				int byteArraySize = 0;
 				for (final MessagePart messagePart : msgSet) {
 					byteArraySize += messagePart.getMessagePart().length;
 				}
 				final byte[] completeMsg = new byte[byteArraySize];
 				int index = -1;
 
 				for (final MessagePart messagePart : msgSet) {
 					for (final byte b : messagePart.getMessagePart()) {
 						completeMsg[++index] = b;
 					}
 				}
 				msgQueue.remove(msgSet);
 				onFeature(robotId, completeMsg);
 			} else {
 				// add message part to message set
 				msgSet.add(msgPart);
 			}
 		}
 	}
 
 	/**
 	 * If a normal message of the robots is received, this message will be called It decodes the
 	 * message and informs the observers
 	 * 
 	 * The first two byte should be the robot ID Followed by the length of the value array (1 byte)
 	 * After that the array with the values, of the previous given length and 2 bytes for each value
 	 * Finally the rest of the bytes for the name of the task
 	 * 
 	 * @param message
 	 *            The message, which was received
 	 */
 	private void onMessage(final byte[] message) {
 		// position in message
 		int pos = 0;
 		// Decodes the robot ID
 		final int robotId = unsignedDualByteToInt(cutByteArray(message, pos, pos + 2));
 		pos += 4;// 2 byte robot id and 2 byte destination id
 		// Decodes the amount of parameters
 		final byte[] lengthByteArray = cutByteArray(message, pos, pos + 1);
 		pos += 1;
 		final int valueLength = unsignedSingleByteToInt(lengthByteArray[0]);
 		// Decodes the parameter array
 		final byte[] valuesByteArray = cutByteArray(message, pos, valueLength * 2 + pos);
 		final int[] values = new int[valuesByteArray.length / 2];
 		for (int i = 0; i < values.length; i++) {
 			values[i] = unsignedDualByteToInt(cutByteArray(valuesByteArray, i * 2, i * 2 + 2));
 		}
 		// Decodes the name of the task
 		String taskName = "";
 		try {
 			taskName = new String(cutByteArray(message, valueLength * 2 + pos, message.length - 1), "US-ASCII");
 		} catch (final UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		// Calls the observers
 		for (final MessageListener listener : messageListener) {
 			listener.onMessage(robotId, taskName, valueLength, values);
 		}
 
 	}
 
 	/**
 	 * Is called when a feature list is received. The method decodes the features and informs the
 	 * observers
 	 * 
 	 * @param message
 	 *            The message, which was received
 	 * 
 	 * @param robotId
 	 *            The source robot Id
 	 */
 	private void onFeature(final int robotId, final byte[] message) {
 		// position in message
 		int pos = 0;
 		// Decodes the length of the task list
 		final byte[] taskListLengthArray = cutByteArray(message, pos, pos + 1);
 		pos++;
 		final int taskListLength = unsignedSingleByteToInt(taskListLengthArray[0]);
 		// Decodes the amount of parameters for each task
 		final byte[] paramListLengthArray = cutByteArray(message, pos, taskListLength + pos);
 		pos += taskListLength;
 		final int[] paramListLength = new int[paramListLengthArray.length];
 		for (int i = 0; i < paramListLength.length; i++) {
 			paramListLength[i] = unsignedSingleByteToInt(paramListLengthArray[i]);
 		}
 
 		// sensor amount
 		final byte[] tmp = cutByteArray(message, pos, pos + 1);
 		pos++;
 		final int sensorLength = unsignedSingleByteToInt(tmp[0]);
 		// sensor ranges
 		final int[] sensorRanges = new int[sensorLength * 2];
 		final byte[] sensorRangesArray = cutByteArray(message, pos, pos + sensorLength * 2);
 		pos += sensorLength * 2;
 		for (int i = 0; i < sensorRanges.length; i++) {
 			sensorRanges[i] = unsignedSingleByteToInt(sensorRangesArray[i]);
 		}
 		// Decodes the name of the tasks, the name of parameters and the sensor names
 		final String tasksParams = new String(cutByteArray(message, pos, message.length));
 		final byte[] seperatorByte = new byte[1];
 		seperatorByte[0] = '\0';
 		final String seperator = new String(seperatorByte);
 		final StringTokenizer st = new StringTokenizer(tasksParams, seperator);
 		final String[] taskList = new String[taskListLength];
 		for (int i = 0; i < taskListLength; i++) {
 			taskList[i] = st.nextToken();
 		}
 		final String[][] paramList = new String[taskListLength][maxValueOfArray(paramListLength)];
 		for (int i = 0; i < taskListLength; i++) {
 			for (int j = 0; j < paramListLength[i]; j++) {
 				paramList[i][j] = st.nextToken();
 			}
 		}
 		final String[] sensorNames = new String[sensorLength];
 		for (int i = 0; i < sensorLength; i++) {
 			sensorNames[i] = st.nextToken();
 		}
 		for (final FeatureListener listener : featureListener) {
 			listener.onAction(robotId, taskListLength, taskList, paramListLength, paramList, sensorLength, sensorNames, sensorRanges);
 		}
 	}
 
 	/**
 	 * Converts an integer value into a byte array of a given size
 	 * 
 	 * @param integer
 	 *            The integer value which should be converted
 	 * @param byteLength
 	 *            The wished size of the byte array
 	 * @return The byte array with the converted integer value
 	 */
 	private byte[] intToByteArray(final int integer, final int byteLength) {
 		if ((integer > -128) && (integer < 0)) {
 			final byte[] byteArray = new byte[byteLength];
			byteArray[0] = (byte) ((integer & 0xff00) >> 8);
			byteArray[1] = (byte) (0x00ff & integer); 
 			return byteArray;
 		}
 		final int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer : integer)) / 8;
 		final int trueLength = (integer >= java.lang.Math.pow(2, byteLength * 7) ? byteLength + 1 : byteLength);
 		final byte[] byteArray = new byte[trueLength];
 		for (int n = 0; n < byteNum; n++) {
 			byteArray[(trueLength - 1) - n] = (byte) (integer >>> (n * 8));
 		}
 		for (int i = 0; i < byteArray.length; i++) {
 		}
 		if (trueLength > byteLength) {
 			return cutByteArray(byteArray, 1, trueLength);
 		} else {
 			return (byteArray);
 		}
 	}
 
 	/**
 	 * Converts an integer value into a single byte
 	 * 
 	 * @param integer
 	 *            The integer value which should be converted
 	 * @return The according byte value
 	 */
 	private byte intToByte(final int integer) {
 		if (integer > 255) {
 			return 0;
 		}
 		byte[] array;
 		array = intToByteArray(integer, 1);
 		return array[0];
 	}
 
 	/**
 	 * Converts a 2 dimensional byte array into an unsigned integer value
 	 * 
 	 * @param byteArray
 	 *            The byte array, which should be converted
 	 * @return The unsigned integer value
 	 */
 	private int unsignedDualByteToInt(final byte[] byteArray) {
 		int i = 0;
 		i |= byteArray[0] & 0xFF;
 		i <<= 8;
 		i |= byteArray[1] & 0xFF;
 		return i;
 	}
 
 	/**
 	 * Converts a single byte into an unsigned integer value
 	 * 
 	 * @param unsignedByte
 	 *            The byte, which should be converted
 	 * @return The unsigned integer value
 	 */
 	private int unsignedSingleByteToInt(final byte unsignedByte) {
 		int i = 0;
 		i |= unsignedByte & 0xFF;
 		return i;
 	}
 
 	/**
 	 * Cuts a byte array from a given start- and endpoint
 	 * 
 	 * @param array
 	 *            The original byte array
 	 * @param start
 	 *            The startpoint
 	 * @param end
 	 *            The endpoint
 	 * @return The stripped-down byte array
 	 */
 	private byte[] cutByteArray(final byte[] array, final int start, final int end) {
 		if (end < start) {
 			return null;
 		}
 		if (end > array.length) {
 			return null;
 		}
 		final byte[] strippedDown = new byte[end - start];
 		for (int i = start; i < end; i++) {
 			strippedDown[i - start] = array[i];
 		}
 		return strippedDown;
 	}
 
 	/**
 	 * Looks for the highest value in an integer array
 	 * 
 	 * @param array
 	 *            The integer array which is supposed to be browsed
 	 * @return The highest value of the array
 	 */
 	private int maxValueOfArray(final int[] array) {
 		int max = 0;
 		for (int i = 0; i < array.length; i++) {
 			if (array[i] > max) {
 				max = array[i];
 			}
 		}
 		return max;
 	}
 
 	private class MessagePart implements Comparable<MessagePart> {
 
 		private int robotId;
 		private int messageId;
 		private int messageAmount;
 		private int messageNumber;
 		private byte[] messagePart;
 
 		MessagePart(final int robotId, final int msgId, final int msgAmount, final int msgNr, final byte[] message) {
 			this.robotId = robotId;
 			this.messageId = msgId;
 			this.messageAmount = msgAmount;
 			this.messageNumber = msgNr;
 			this.messagePart = message;
 		}
 
 		public int getRobotId() {
 			return robotId;
 		}
 
 		public int getMessageId() {
 			return messageId;
 		}
 
 		public int getMessageAmount() {
 			return messageAmount;
 		}
 
 		public int getMessageNumber() {
 			return messageNumber;
 		}
 
 		public byte[] getMessagePart() {
 			return messagePart;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + messageId;
 			result = prime * result + robotId;
 			return result;
 		}
 
 		// --------------------------------------------------------------------------------
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Comparable#compareTo(java.lang.Object)
 		 */
 		@Override
 		public int compareTo(final MessagePart o) {
 			if (o.getMessageNumber() > messageNumber) {
 				return -1;
 			} else if (o.getMessageNumber() == messageNumber) {
 				return 0;
 			} else {
 				return 1;
 			}
 		}
 	}
 
 }
