 /*******************************************************************************
  * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
  * Copyright (C) 2012  John Lawson
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You may obtain a copy of the GNU General Public License at 
  * <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     John Lawson - initial API and implementation
  ******************************************************************************/
 package org.nof1trial.nof1.client;
 
import java.util.ArrayList;
import java.util.List;
 
 import org.nof1trial.nof1.shared.ConfigProxy;
 import org.nof1trial.nof1.shared.DataProxy;
 
 /**
  * @author John Lawson
  * 
  */
 public class PatientData {
 
 	private List<String> mDataHeaders = new ArrayList<String>();
 
 	private List<DataProxy> mData;
 
	private List<String> mConfigHeaders = new ArrayList<String>();
 
	private List<String> mConfigData = new ArrayList<String>();
 
	private ConfigProxy mConfig;
 
 	public PatientData(ConfigProxy config, List<DataProxy> data) {
 
 		if (config == null) {
 			throw new IllegalArgumentException("Config cannot be null in " + getClass().getName());
 		}
 
 		mData = data;
 		mConfig = config;
 
 		List<String> list = config.getQuestionList();
 		mDataHeaders = new ArrayList<String>();
 		mDataHeaders.add("Date");
 		mDataHeaders.add("Day");
 		for (String str : list) {
 			mDataHeaders.add(str);
 		}
 		mDataHeaders.add("Comment");
 
 		if (config.getDocEmail() != null) {
 			mConfigHeaders.add("Doctor email");
 			mConfigData.add(config.getDocEmail());
 		}
 		if (config.getDoctorName() != null) {
 			mConfigHeaders.add("Doctor name");
 			mConfigData.add(config.getDoctorName());
 		}
 		if (config.getPatientEmail() != null) {
 			mConfigHeaders.add("Patient email");
 			mConfigData.add(config.getPatientEmail());
 		}
 		if (config.getPatientName() != null) {
 			mConfigHeaders.add("Patient email");
 			mConfigData.add(config.getPatientName());
 		}
 		if (config.getStartDate() != null) {
 			mConfigHeaders.add("Start date");
			mConfigData.add(config.getStartDate());
 		}
 		if (config.getLengthPeriods() != null) {
 			mConfigHeaders.add("Treatment period length");
 			mConfigData.add(String.valueOf(config.getLengthPeriods()));
 		}
 		if (config.getNumberPeriods() != null) {
 			mConfigHeaders.add("Number of periods");
 			mConfigData.add(String.valueOf(config.getNumberPeriods()));
 		}
 		if (config.getTreatmentA() != null) {
 			mConfigHeaders.add("Treatment A");
 			mConfigData.add(config.getTreatmentA());
 		}
 		if (config.getTreatmentB() != null) {
 			mConfigHeaders.add("Treatment B");
 			mConfigData.add(config.getTreatmentB());
 		}
 		if (config.getTreatmentNotes() != null) {
 			mConfigHeaders.add("Treatment notes");
 			mConfigData.add(config.getTreatmentNotes());
 		}
 
 	}
 
 	public void setData(List<DataProxy> data) {
 		mData = data;
 
 		if (mData != null) {
 			List<String> list = mConfig.getQuestionList();
 			mDataHeaders = new ArrayList<String>();
 			mDataHeaders.add("Date");
 			mDataHeaders.add("Day");
 			for (String str : list) {
 				mDataHeaders.add(str);
 			}
 			mDataHeaders.add("Comment");
 
 		} else {
 			mDataHeaders.clear();
 		}
 	}
 
 	public List<String> getConfigHeaders() {
 		return mConfigHeaders;
 	}
 
 	public List<String> getConfigData() {
 		return mConfigData;
 	}
 
 	public List<DataProxy> getData() {
 		return mData;
 	}
 
 	public List<String> getDataHeaders() {
 		return mDataHeaders;
 	}
 
 }
