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
 
 package org.openmrs.module.iqchartimport.iq.model;
 
 import java.util.Date;
 
 /**
  * Physical adherence
  */
 public class PhyAdherence {
 
 	protected Date date;
	protected Short doseMiss7;
	protected Short pillsRemain;
 	protected Boolean visitRespected;
 	
 	/**
 	 * @param date
 	 * @param doseMiss7
 	 * @param pillsRemain
 	 * @param visitRespected
 	 */
	public PhyAdherence(Date date, Short DoseMiss7, Short pillsRemain, Boolean visitRespected) {
 		this.date = date;
 		this.doseMiss7 = DoseMiss7;
 		this.pillsRemain = pillsRemain;
 		this.visitRespected = visitRespected;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
	public Short getDoseMiss7() {
 		return doseMiss7;
 	}
 
	public Short getPillsRemain() {
 		return pillsRemain;
 	}
 
 	public Boolean isVisitRespected() {
 		return visitRespected;
 	}	
 }
