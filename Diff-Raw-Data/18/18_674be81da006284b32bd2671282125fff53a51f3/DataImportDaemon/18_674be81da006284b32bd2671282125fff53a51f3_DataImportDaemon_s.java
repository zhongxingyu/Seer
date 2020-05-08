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
 package org.chartsearch.server;
 
 import java.util.concurrent.BlockingQueue;
 
 import org.apache.solr.logging.log4j.Log4jInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  */
 public class DataImportDaemon implements Runnable {
 
 	private static final Logger log = LoggerFactory
 			.getLogger(DataImportDaemon.class);
 	private final BlockingQueue<PatientInfo> queue;
 	private final int id;
 
 	public DataImportDaemon(BlockingQueue<PatientInfo> queue, int id) {
 		this.queue = queue;
 		this.id = id;
 	}
 
 	@Override
 	public void run() {
 		log.info("Daemon #{} is running", id);
 		while (true) {
 			try {
 				PatientInfo patientInfo = queue.take();
 				log.info(String.format(
 						"Daemon #{} taked from queue patient #{}", id,
 						patientInfo.getPatientId()));
 			} catch (InterruptedException e) {
 				log.error("The thread #{} is interrupted", id);
 			}
 		}
 	}
 
 }
