 /**
  *  Copyright 2010 Society for Health Information Systems Programmes, India (HISP India)
  *
  *  This file is part of Hospital-core module.
  *
  *  Hospital-core module is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
 
  *  Hospital-core module is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Hospital-core module.  If not, see <http://www.gnu.org/licenses/>.
  *
  **/
 

 package org.openmrs.module.hospitalcore.util;
 
 import org.apache.commons.lang.StringUtils;
 import org.openmrs.Encounter;
 import org.openmrs.EncounterType;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.hospitalcore.model.PatientServiceBillItem;
 
 public class OrderUtil {
 	
 	private static final String RADIOLOGY_ORDER_TYPE = "billing.encounterType.radiology";
 	
	public static void saveRadiologyOrder(PatientServiceBillItem item){		
 		String radiologyEncounterTypeName = GlobalPropertyUtil.getString(RADIOLOGY_ORDER_TYPE, null);
		if(!StringUtils.isBlank(radiologyEncounterTypeName)){
 			EncounterType et = Context.getEncounterService().getEncounterType(radiologyEncounterTypeName);
 			//ghanshyam 27/06/2012 tag DLS_DEAD_LOCAL_STORE code Encounter encounter = new Encounter();
 			/*
 			if(et!=null){
 				Encounter encounter = new Encounter();
 				
 			}*/
 		}
 	}
 	
 	
 }
