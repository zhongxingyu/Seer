 /*******************************************************************************
  * Copyright (c) 2000, 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Julien Ruaux: jruaux@octo.com
  * 	   Vincent Massol: vmassol@octo.com
  *******************************************************************************/
 package org.jenkinsci.testinprogress.server.messages;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 /**
  * Handles the marshaling of the different messages.
  * 
  * copied from org.eclipse.jdt.internal.junit.model.RemoteTestRunnerClient and
  * modified
  */
 public class TestMessagesParser {
 	private final static Logger LOG = Logger
 			.getLogger(TestMessagesParser.class.getName());	
 
 	/**
 	 * An array of listeners that are informed about test events.
 	 */
 	private ITestRunListener[] listeners;
 
 	private BufferedReader reader;
 	
 	private long startTime;
 	
 	/**
 	 * The protocol version
 	 */
 	private String fVersion;
 	
 	
 	public TestMessagesParser(ITestRunListener[] listeners) {
 		this.listeners = listeners;
 	}
 
 	public void processTestMessages(Reader reader) {
 		this.reader = new BufferedReader(reader);
 		try {
 			JSONObject message;
 			while ((message = readMessage(this.reader)) != null)
 				processMessage(message);
 		} catch (JSONException e) {
 			LOG.log(Level.WARNING, "Could not read message", e);
 			notifyTestRunTerminated();
 		} catch (IOException e) {
 			LOG.log(Level.WARNING, "Could not read message", e);
 			notifyTestRunTerminated();
 		}
 		shutDown();
 	}
 
 	private void shutDown() {
 		try {
 			if (reader != null) {
 				reader.close();
 				reader = null;
 			}
 		} catch (IOException e) {
 		}
 	}
 
 	private JSONObject readMessage(BufferedReader in) throws IOException, JSONException {
 		String line = in.readLine();
 		if (line == null) {
 			return null;
 		} else {
 			try {
 				return new JSONObject(line);
 			} catch (JSONException e) {
 				throw new IOException("Message is not a valid json object : '"+line+"'",e);
 			}
 		}
 	}
 
 	private void processMessage(JSONObject jsonMsg) {
 		String msgId = getValue(jsonMsg, "messageId", "").toString().trim();
 
 		if (msgId.contentEquals(MessageIds.TEST_RUN_START.trim())) {
 			int count = 0;
 			fVersion = getValue(jsonMsg, "fVersion", "v2").toString();
 			count = jsonMsg.getInt("testCount");
 			notifyTestRunStarted(jsonMsg, count);
 		} else if (msgId.contentEquals(MessageIds.TEST_START.trim())) {
 			notifyTestStarted(jsonMsg);
 
 		} else if (msgId.contentEquals(MessageIds.TEST_END.trim())) {
 			notifyTestEnded(jsonMsg);
 
 		} else if (msgId.contentEquals(MessageIds.TEST_ERROR.trim())) {
 			notifyTestFailed(jsonMsg, ITestRunListener.STATUS_ERROR);
 
 		} else if (msgId.contentEquals(MessageIds.TEST_FAILED.trim())) {
 			notifyTestFailed(jsonMsg, ITestRunListener.STATUS_FAILURE);
 
 		} else if (msgId.contentEquals(MessageIds.TEST_RUN_END.trim())) {
 			String elTime = getValue(jsonMsg, "elapsedTime", "").toString();
 
 			long elapsedTime = Long.parseLong(elTime);
 			testRunEnded(jsonMsg, elapsedTime);
 
 		} else if (msgId.contentEquals(MessageIds.TEST_TREE.trim())) {
 			notifyTestTreeEntry(jsonMsg);
 		}
 	}
 
 	private void notifyTestTreeEntry(final JSONObject jsonMsg) {
 		String testName = jsonMsg.getString("testName");
 		String testId = getStringValue(jsonMsg, "testId", testName);
 		String parentId = getStringValue(jsonMsg, "parentId", null);
 		boolean isSuite = (Boolean)getValue(jsonMsg, "isSuite", false);
 		
 		long timeStamp = getTimeStamp(jsonMsg);
 		for (int i = 0; i < listeners.length; i++) {
 			ITestRunListener listener = listeners[i];
			listener.testTreeEntry(timeStamp, testId, testName, parentId, isSuite);
 		}
 	}
 
 	private void testRunEnded(JSONObject jsonMsg, final long elapsedTime) {
 		long timeStamp = getTimeStamp(jsonMsg);
 		for (int i = 0; i < listeners.length; i++) {
 			ITestRunListener listener = listeners[i];
 			listener.testRunEnded(timeStamp, elapsedTime);
 		}
 	}
 
 	private void notifyTestEnded(final JSONObject jsonMsg) {
 		String testName = jsonMsg.getString("testName");
 		String testId = getStringValue(jsonMsg, "testId", testName);
 		boolean ignored = (Boolean) getValue(jsonMsg, "ignored", false);
 		long timeStamp = getTimeStamp(jsonMsg);
 		for (int i = 0; i < listeners.length; i++) {
 			ITestRunListener listener = listeners[i];			
 			listener.testEnded(timeStamp, testId, testName, ignored);
 		}
 	}
 
 	private void notifyTestStarted(final JSONObject jsonMsg) {
 		String testName = jsonMsg.getString("testName");
 		String testId = getStringValue(jsonMsg, "testId", testName);
 		boolean ignored = (Boolean) getValue(jsonMsg, "ignored", false);
 		long timeStamp = getTimeStamp(jsonMsg);
 		for (int i = 0; i < listeners.length; i++) {
 			ITestRunListener listener = listeners[i];
 			listener.testStarted(timeStamp,  testId, testName, ignored);
 		}
 	}
 
 	private void notifyTestRunStarted(JSONObject jsonMsg, final int count) {
 		long timeStamp = getTimeStamp(jsonMsg);
 		String runId = getStringValue(jsonMsg, "runId", null);
 		startTime = timeStamp;
 		for (int i = 0; i < listeners.length; i++) {
 			ITestRunListener listener = listeners[i];
 			listener.testRunStarted(timeStamp, count, runId);
 		}
 	}
 
 	private void notifyTestFailed(JSONObject jsonMsg, int failureKind) {
 		String testName = jsonMsg.getString("testName");
 		String testId = getStringValue(jsonMsg, "testId", testName);
 		String errorTrace = getStringValue(jsonMsg, "errorTrace", "");
 		String expectedMsg = getStringValue(jsonMsg, "expectedMsg", "");
 		String actualMsg = getStringValue(jsonMsg, "actualMsg", "");
 		boolean assumptionFailed = (Boolean) getValue(jsonMsg, "assumptionFailed", false);
 		long timeStamp = getTimeStamp(jsonMsg);
 		for (int i = 0; i < listeners.length; i++) {
 			ITestRunListener listener = listeners[i];
 			listener.testFailed(timeStamp, failureKind, testId, testName,
 					errorTrace, expectedMsg,
 					actualMsg, assumptionFailed);
 		}
 	}
 
 
 	private void notifyTestRunTerminated() {
 		for (int i = 0; i < listeners.length; i++) {
 			ITestRunListener listener = listeners[i];
 			listener.testRunTerminated();
 		}
 	}
 	
 	public Object getValue(JSONObject jsonData, String key, Object defaultValue) {
 		try {
 			Object data = jsonData.get(key);
 			return data;
 		} catch (JSONException e) {
 			return defaultValue;
 		}
 	}
 	
 	public String getStringValue(JSONObject jsonData,String key, Object defaultValue){
 		Object value = this.getValue(jsonData, key, defaultValue);
 		if (value == null) {
 			return null;
 		} else {
 			return value.toString();
 		}
 	}
 	
 	
 	private long getTimeStamp(JSONObject jsonMsg){
 		long timeStamp;
 		String timeAsString = getStringValue(jsonMsg, "timeStamp", null);
 		
 		if (timeAsString != null) {
 			timeStamp = Long.parseLong(timeAsString);				
 		} else {
 			timeStamp = System.currentTimeMillis();
 		}
 		
 		return timeStamp;
 	}
 }
