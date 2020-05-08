 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.odkconnector.serialization.web;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.Test;
 import org.openmrs.module.odkconnector.serialization.serializer.openmrs.ObsSerializer;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 
 public class PatientWebConnectorTest extends BaseModuleContextSensitiveTest {
 
 	private static final Log log = LogFactory.getLog(PatientWebConnectorTest.class);
 
 	private static final String SERVER_URL = "http://localhost:8080/openmrs";
 
 	@Test
 	public void serialize_shouldDisplayAllPatientInformation() throws Exception {
 
 		// compose url
 		URL u = new URL(SERVER_URL + "/module/odkconnector/download/patients.form");
 
 		// setup http url connection
 		HttpURLConnection connection = (HttpURLConnection) u.openConnection();
 		connection.setDoOutput(true);
 		connection.setRequestMethod("POST");
 		connection.setConnectTimeout(10000);
 		connection.setReadTimeout(10000);
 		connection.addRequestProperty("Content-type", "application/octet-stream");
 
 		// write auth details to connection
 		DataOutputStream outputStream = new DataOutputStream(new GZIPOutputStream(connection.getOutputStream()));
 		outputStream.writeUTF("admin");
 		outputStream.writeUTF("test");
 		outputStream.writeBoolean(false);
 		outputStream.writeInt(2);
 		outputStream.writeInt(1);
 		outputStream.close();
 
 		DataInputStream inputStream = new DataInputStream(new GZIPInputStream(connection.getInputStream()));
 		Integer responseStatus = inputStream.readInt();
 
 		if (responseStatus == HttpURLConnection.HTTP_OK) {
 
 			// total number of patients
 			Integer patientCounter = inputStream.readInt();
 			System.out.println("Patient Counter: " + patientCounter);
			for (int i = 0; i < patientCounter; i++) {
 				System.out.println("=================Patient=====================");
 				System.out.println("Patient Id: " + inputStream.readInt());
 				System.out.println("Family Name: " + inputStream.readUTF());
 				System.out.println("Middle Name: " + inputStream.readUTF());
 				System.out.println("Last Name: " + inputStream.readUTF());
 				System.out.println("Gender: " + inputStream.readUTF());
 				System.out.println("Birth Date: " + inputStream.readUTF());
 				System.out.println("Identifier: " + inputStream.readUTF());
                System.out.println("Patients: " + i + " out of " + patientCounter);
 			}
 
 			Integer obsCounter = inputStream.readInt();
 			System.out.println("Observation Counter: " + obsCounter);
 			for (int j = 0; j < obsCounter; j++) {
 				System.out.println("==================Observation=================");
 				System.out.println("Patient Id: " + inputStream.readInt());
 				System.out.println("Concept Name: " + inputStream.readUTF());
 
 				byte type = inputStream.readByte();
 				if (type == ObsSerializer.TYPE_STRING)
 					System.out.println("Value: " + inputStream.readUTF());
 				else if (type == ObsSerializer.TYPE_INT)
 					System.out.println("Value: " + inputStream.readInt());
 				else if (type == ObsSerializer.TYPE_DOUBLE)
 					System.out.println("Value: " + inputStream.readDouble());
 				else if (type == ObsSerializer.TYPE_DATE)
 					System.out.println("Value: " + inputStream.readUTF());
 				System.out.println("Time: " + inputStream.readUTF());
 			}
 			Integer formCounter = inputStream.readInt();
 			System.out.println("Form Counter: " + formCounter);
 			for (int j = 0; j < formCounter; j++) {
 				System.out.println("==================Observation=================");
 				System.out.println("Patient Id: " + inputStream.readInt());
 				System.out.println("Concept Name: " + inputStream.readUTF());
 
 				byte type = inputStream.readByte();
 				if (type == ObsSerializer.TYPE_STRING)
 					System.out.println("Value: " + inputStream.readUTF());
 				else if (type == ObsSerializer.TYPE_INT)
 					System.out.println("Value: " + inputStream.readInt());
 				else if (type == ObsSerializer.TYPE_DOUBLE)
 					System.out.println("Value: " + inputStream.readDouble());
 				else if (type == ObsSerializer.TYPE_DATE)
 					System.out.println("Value: " + inputStream.readUTF());
 				System.out.println("Time: " + inputStream.readUTF());
 			}
 		}
 		inputStream.close();
 	}
 
 }
