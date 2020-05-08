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
 package org.nof1trial.nof1.containers;
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.acra.ACRA;
 import org.nof1trial.nof1.shared.DataProxy;
 
 import com.google.web.bindery.requestfactory.shared.ServerFailure;
 
 /**
  * @author John Lawson
  * 
  */
 public class DataUploadQueue implements Data.OnDataRequestListener {
 
 	private ArrayList<Data> mDataList;
 	private ArrayList<Data> mFailedList;
 	private ArrayList<Data> mSuccessList;
 	private int mDataCount;
 	private int mResponseCount;
 	private OnDataUploadedListener listener;
 
 	private boolean mBusy;
 
 	public interface OnDataUploadedListener {
 		public void onDataUploaded(List<Data> successList, List<Data> failedList);
 	}
 
 	public DataUploadQueue(OnDataUploadedListener listener) {
 		mDataList = new ArrayList<Data>();
 		mFailedList = new ArrayList<Data>();
 		mSuccessList = new ArrayList<Data>();
 		mDataCount = 0;
 		this.listener = listener;
 	}
 
 	public void addData(Data data) {
 		mDataList.add(data);
 	}
 
 	public void clear() {
 		mDataList.clear();
 	}
 
 	public void start() {
 		if (!mBusy) {
 			mBusy = true;
 
 			mResponseCount = 0;
 			mDataCount = mDataList.size();
 
 			mFailedList.clear();
 			mSuccessList.clear();
 
 			for (Data data : mDataList) {
 				data.upload();
 			}
 		}
 	}
 
 	public void onDataUploadSuccess(Data data, DataProxy dataProxy) {
 		mResponseCount++;
 		mSuccessList.add(data);
 
		sendResultIfFinished();
 	}
 
 	public void onDataUploadFailure(Data data, ServerFailure error) {

		if ("Auth Failure".equals(error.getMessage())) {
			ACRA.getErrorReporter().handleSilentException(new Throwable(error.getMessage()));
		}

 		mResponseCount++;
 		mFailedList.add(data);
 
		sendResultIfFinished();
	}

	private void sendResultIfFinished() {
 		if (isFinished()) {
 			mBusy = false;
 			sendResultToListener();
 		}
 	}
 
 	private boolean isFinished() {
 		return mResponseCount >= mDataCount;
 	}

	private void sendResultToListener() {
		listener.onDataUploaded(mSuccessList, mFailedList);
	}
 }
