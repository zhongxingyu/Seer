 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
  * $Revision: 1.9 $
  * $Date: 2007-10-05 14:45:50 $
  * $Author: imoncada $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.data.state;
 
 import java.io.IOException;
 import java.lang.ref.Reference;
 import java.lang.ref.WeakReference;
 import java.util.Vector;
 
 import org.concord.data.Unit;
 import org.concord.data.stream.DataStoreUtil;
 import org.concord.data.stream.ProducerDataStore;
 import org.concord.framework.data.stream.DataChannelDescription;
 import org.concord.framework.data.stream.DataProducer;
 import org.concord.framework.data.stream.DataStoreEvent;
 import org.concord.framework.data.stream.DataStoreListener;
 import org.concord.framework.data.stream.DataStreamDescription;
 import org.concord.framework.data.stream.WritableArrayDataStore;
 import org.concord.framework.otrunk.OTChangeEvent;
 import org.concord.framework.otrunk.OTChangeListener;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.OTResourceList;
 
 
 /**
  * OTDataStoreRealObject
  * 
  * This object is synchronized with a OTDataStore object using the OTDataStoreController class
  * it partially supports the use of virtual or incrementing channels.  But   
  *
  * Date created: Nov 18, 2004
  *
  * @author scytacki<p>
  *
  */
 public class OTDataStoreRealObject extends ProducerDataStore
 	implements WritableArrayDataStore
 {	
 	protected OTDataStore otDataStore;
 	DataStoreEvent changeEvent = new DataStoreEvent(this, DataStoreEvent.DATA_CHANGED);
 	DataStoreEvent removeEvent = new DataStoreEvent(this, DataStoreEvent.DATA_REMOVED);
 		
 	private OTChangeListener myListener = new OTChangeListener(){
 
 		public void stateChanged(OTChangeEvent e) {
 			if("values".equals(e.getProperty())){
 			    String op = e.getOperation();
 			    if(OTChangeEvent.OP_ADD == op){
 			    	notifyDataAdded();
 			    } else if(OTChangeEvent.OP_CHANGE == op || OTChangeEvent.OP_SET == op){
 			    	notifyDataChanged();
 			    } else if(OTChangeEvent.OP_REMOVE == op ||
 			    		OTChangeEvent.OP_REMOVE_ALL == op){
 			    	notifyDataRemoved();
 			    }
 			} else {
 				// any other changes are being ignored right now
 				
 			}			
 		}
 		
 	};
 	
 	/**
 	 * 
 	 */
 	public void setOTDataStore(OTDataStore otDataStore)
 	{
 		this.otDataStore = otDataStore;
 		
 		// We need to listen to the otDataStore so when it changes we can 
 		// throw a datastore change or remove event
 		otDataStore.addOTChangeListener(myListener);
 
 		String valueStr = otDataStore.getValuesString();
 		if(valueStr == null) return;
 						
 		otDataStore.setValuesString(null);
 		try {
 			DataStoreUtil.loadData(valueStr, this, false);
 		} catch ( IOException e) {
 			e.printStackTrace();
 		}		
 		
 	}
 	    	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataStore#clearValues()
 	 */
 	public void clearValues() 
 	{
 		otDataStore.getValues().removeAll();
 		
 		// this will happen when otDataStore notifies our listener that the data was removed
 		// notifyDataRemoved();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataStore#getTotalNumChannels()
 	 */
 	public int getTotalNumChannels() 
 	{
 		int otNumberOfChannels = otDataStore.getNumberChannels();
 		if(otNumberOfChannels == -1) return 1;
 		
 		// If virtual channels is turned on and there is a dt then
 		// the first channel is not stored directly in the otDataStore.  
 		// the numberChannels property refers to the number of channels actually
 		// in the values property.
 		if(isIncrementalChannel(0)){
 			otNumberOfChannels++;
 		}
 		
 		return otNumberOfChannels;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataStore#getTotalNumSamples()
 	 */
 	public synchronized int getTotalNumSamples() 
 	{
 		int dataArrayStride = getDataArrayStride();
 		
 		if(dataArrayStride == 0){
 			System.err.println("Warning OTDataStoreRealObject is being used without initializing the number of channels");
 			return 0;
 		}
 		
 		OTResourceList values = otDataStore.getValues();
 		int size = values.size();
 		int rows = size / dataArrayStride;
 		if (size % dataArrayStride > 0){
 			rows = rows + 1;
 		}
 		return rows;
 	}
 	
 	/**
 	 * @see org.concord.framework.data.stream.DataStore#getValueAt(int, int)
 	 */
 	public synchronized Object getValueAt(int numSample, int numChannel) 
 	{
 		//Special case: when dt is a channel, it's the channel -1
 		if (isIncrementalChannel(numChannel)){
 			return new Float(numSample * getIncrement());
 		}
 	
 		int index = getIndex(numSample, numChannel);
 		if(index >= otDataStore.getValues().size()) {
 			return null;
 		}
 				
 		return otDataStore.getValues().get(index);
 	}
 
 	/**
 	 * This method returns the index into the dataArray of a particular
 	 * channel and sample.  It takes virtual channels into account.  
 	 * 
 	 * @param sampleNumber
 	 * @param channelNumber
 	 * @return
 	 */
 	public int getIndex(int sampleNumber, int channelNumber)
 	{
 		if(isIncrementalChannel(0)){
 			// the auto incrementing channel is 0 so we aren't storing that
 			// in the data array, so the channel being searched for should
 			// be reduced by one.
 			if(channelNumber == 0){
 				System.err.println("Trying to lookup the auto increment channel");
 				return Integer.MIN_VALUE;
 			}
 			
 			channelNumber--;
 		}
 		
 		int dataArrayStride = getDataArrayStride();
 		
 		if(channelNumber >= dataArrayStride) {
			throw new IndexOutOfBoundsException("Trying to lookup an invalid channel: " + channelNumber);
 		}
 		
 		return sampleNumber * dataArrayStride + channelNumber;		
 	}
 
 	/**
 	 * Return the size of a row in the values list (dataArray).
 	 * 
 	 * @return
 	 */
 	protected int getDataArrayStride()
 	{
 		// this used to always be the totalNumChannels but with virtual
 		// channels this can be one less than that.
 		int numChannels = getTotalNumChannels();
 		
 		if(isIncrementalChannel(0)){
 			return numChannels - 1;
 		}
 		
 		return numChannels;
 	}
 	
 	public void setValueAt(int numSample, int numChannel, Object value) 
 	{
 		OTResourceList values = otDataStore.getValues();
 		int numChannels = getTotalNumChannels();
 		
 		if(numChannel >= numChannels) {
 			// FIXME
 			// increase the number of channels
 			// if we have existing data then we need to insert a lot of nulls
 			// or something to fill the space.
 			numChannels = numChannel+1;
 			
 			if(isIncrementalChannel(0)){
 				numChannels--;
 			}
 			otDataStore.setNumberChannels(numChannels);
 		}
 		
 		int index = getIndex(numSample, numChannel); 
 		if(index >= values.size()) {
 			//System.out.println("add new value at "+index);
 			//Add empty values until we get to the desired index
 			int j = values.size();
 			for (; j < index; j++){
 				values.add(j, null);
 			}
 			values.add(index, value);			
 		} else {
 			values.set(index, value);
 		}
 		
 		/*
 		 * This real object doesn't need to send out notifications directly.  Because the line:
 		 * values.set() or values.add() modify the OTResourceList, that causes an event to be 
 		 * sent out by the OTDataStore object.  Which is then caught by our inner class "myListener", 
 		 * and at that point the standard data store event is sent out.
 		 * 
  		 */		
 	}	
 
 	
 	public void setValues(int numbChannels,float []values)
 	{
         otDataStore.setDoNotifyChangeListeners(false);
 
         // If this datastore is using virtual channels then should
         // the passed in values actually start at channel 1 not channel 0
 	    for(int i=0;i<values.length;i++) {
 	        int channelNumber = i%numbChannels;
 	        setValueAt(i/numbChannels, channelNumber, 
 	                new Float(values[i]));
 	    }
 	    
 	    
         otDataStore.setDoNotifyChangeListeners(true);
 
         notifyOTValuesChange();
 	}
 	
 	/**
 	 * Adds a value to the channel indicated
 	 * If the channel doesn't exist, it doesn't do anything
 	 *
 	 * @param numChannel	channel number, starting from 0, >0
 	 * @param value			value to add
 	 */
 	protected void addValue(int numSample, int numChannel, Object value)
 	{
 	    setValueAt(numSample, numChannel, value);	    
 	}
 		
 	protected void addSamples(float [] values, int offset, 
 	        int numberOfSamples, int localNextSampleOffset)
 	{
 		synchronized(otDataStore) {
 	        otDataStore.setDoNotifyChangeListeners(false);
 
 			int numChannels = getNumberOfProducerChannels();
 			int firstSample = getTotalNumSamples();
 
 			int firstChannelOffset = 0;
 			if(isIncrementalChannel(0)){
 				firstChannelOffset = 1;
 			}
 			
 		    for(int i=0; i<numberOfSamples; i++) {
 		        for(int j=0; j<numChannels; j++) {
 		            Float value = new Float(values[offset+(i*localNextSampleOffset)+j]);
 		            setValueAt(firstSample + i, firstChannelOffset + j, value);
 		        }
 		    }
 		    
 	        otDataStore.setDoNotifyChangeListeners(true);
 		}
 		
 		notifyOTValuesChange();
 	}
 
 	protected void notifyOTValuesChange()
 	{
 		otDataStore.notifyOTChange("values", OTChangeEvent.OP_CHANGE, null, null);		
 	}
 	
 	protected void notifyOTValuesRemove()
 	{
 		otDataStore.notifyOTChange("values", OTChangeEvent.OP_REMOVE, null, null);		
 	}
 
 	/**
 	 * @see org.concord.framework.data.stream.WritableDataStore#removeSampleAt(int)
 	 */
 	public void removeSampleAt(int numSample) 
 	{
 		OTResourceList values = otDataStore.getValues();
 		
 		int index = getIndex(numSample, 0);
 
         otDataStore.setDoNotifyChangeListeners(false);
 
 		int dataArrayStride = getDataArrayStride();
         for(int i=0; i<dataArrayStride; i++) {
 		    values.remove(index);
 		}
 
         otDataStore.setDoNotifyChangeListeners(true);
         
         notifyOTValuesRemove();        
 	}
 	
 	/**
 	 * @see org.concord.framework.data.stream.WritableDataStore#insertSampleAt(int)
 	 */
 	public void insertSampleAt(int numSample)
 	{
 		OTResourceList values = otDataStore.getValues();
 		
 		int index = getIndex(numSample, 0);
 
         otDataStore.setDoNotifyChangeListeners(false);
 
         int dataArrayStride = getDataArrayStride();
         for(int i=0; i<dataArrayStride; i++) {
 		    values.add(index, null);
 		}
 
         otDataStore.setDoNotifyChangeListeners(true);
         
         notifyOTValuesChange();
 	}	
 	
 	public void setDataProducer(DataProducer dataProducer) 
 	{
 		super.setDataProducer(dataProducer);
 
 
 	}
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.WritableDataStore#setDataChannelDescription(int, org.concord.framework.data.stream.DataChannelDescription)
 	 */
 	public void setDataChannelDescription(int channelIndex,
 			DataChannelDescription desc) 
 	{
 		// FIXME this is not supported yet
 		//throw new UnsupportedOperationException("org.concord.framework.data.stream.WritableDataStore.setDataChannelDescription not supported yet");
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataStore#getDataChannelDescription(int)
 	 */
 	public DataChannelDescription getDataChannelDescription(int numChannel) 
 	{
 	    if(getDataProducer() != null) {
 	        // need to make sure that these values are saved
 	        // so if this data store is disconnected from the 
 	        // the data producer it will preserve this info
 			if (dataStreamDesc == null) return null;
 			
 			//Special case: if the channel equals the incrementing channel
 			//  then return the dt channel description.
 			if (isIncrementalChannel(numChannel)){
 				return dataStreamDesc.getDtChannelDescription();
 			}
 			
 			// shift the channel down if the incremental channel is 0
 			// in that case channel 0 of the data store is actually the 
 			// dt channel, and channel 1 of the data store is actually
 			// channel 0 of the dataProducer.
 			if (isIncrementalChannel(0)){
 				numChannel --;
 			}
 			
 			return dataStreamDesc.getChannelDescription(numChannel);
 	    }
 	    
 	    // If we have an incremental channel, then ot channel description at index 
 	    // 0 describes the incremental channel, and the ot channel description at 1 is
 	    // the first real channel description.
 	    // A caller of this method can request the incremental channel description either
 	    // by passing in -1 or 0 depending of virtualChannels are enabled.
 	    // If virtualChannels are enabled and there is a incremental channel, 
 	    // then the caller passes in 0 to get the incremental description.
 	    // If virtualChannels are not enabled and there is a incremental channel, 
 	    // then the caller passes in -1 to get the incremental description.
 	    int otChannelDescriptionIndex = numChannel;
 
 	    // If -1 is the incremental channel then increase the channel description so
 	    // the correct one in the OT channel descriptions list is used
 	    if(isIncrementalChannel(-1)){
 	    	otChannelDescriptionIndex++;
 	    }
 
 	    OTObjectList channelDescriptions = otDataStore.getChannelDescriptions();
 		if(otChannelDescriptionIndex >= channelDescriptions.size()) {
 			return null;
 		}
 				
 		OTDataChannelDescription otChDesc = 
 			(OTDataChannelDescription)channelDescriptions.get(otChannelDescriptionIndex);
 		
 		DataChannelDescription chDesc = new DataChannelDescription();
 		chDesc.setAbsoluteMax(otChDesc.getAbsoluteMax());
 		chDesc.setAbsoluteMin(otChDesc.getAbsoluteMin());
 		chDesc.setNumericData(otChDesc.getNumericData());
 		chDesc.setName(otChDesc.getName());
 		int precision = otChDesc.getPrecision();
 		if(precision != Integer.MAX_VALUE) {
 			chDesc.setPrecision(precision);
 		}
 		chDesc.setRecommendMax(otChDesc.getRecommendMax());
 		chDesc.setRecommendMin(otChDesc.getRecommendMin());
 		String unitStr = otChDesc.getUnit();
 		Unit unit = Unit.findUnit(unitStr);
 		chDesc.setUnit(unit);
 
 		return chDesc;
 	}
 	
 	protected OTDataChannelDescription createOTDataChannelDescription(DataChannelDescription dCDesc) 
 		throws Exception
 	{
 		OTObjectService objService = otDataStore.getOTObjectService();
 
 		OTDataChannelDescription otDCDesc = 
 			(OTDataChannelDescription) objService.createObject(OTDataChannelDescription.class);
 
 		otDCDesc.setName(dCDesc.getName());
 		if(dCDesc.getUnit() != null){
 			otDCDesc.setUnit(dCDesc.getUnit().getDimension());
 		}
 
 		float absMax = dCDesc.getAbsoluteMax();
 		if(!Float.isNaN(absMax)){
 			otDCDesc.setAbsoluteMax(absMax);
 		}
 		float absMin = dCDesc.getAbsoluteMin();
 		if(!Float.isNaN(absMin)){
 			otDCDesc.setAbsoluteMin(absMin);
 		}
 		float recMax = dCDesc.getRecommendMax();
 		if(!Float.isNaN(recMax)){
 			otDCDesc.setRecommendMax(recMax);
 		}
 		float recMin = dCDesc.getRecommendMin();
 		if(!Float.isNaN(recMin)){
 			otDCDesc.setRecommendMin(recMin);					
 		}
 
 		if(dCDesc.isUsePrecision()){
 			otDCDesc.setPrecision(dCDesc.getPrecision());
 		}
 		otDCDesc.setNumericData(dCDesc.isNumericData());
 
 		return otDCDesc;
 	}
 	
 	protected void updateDataDescription(DataStreamDescription desc) 
 	{
 		super.updateDataDescription(desc);
 		
 		// Save all the dataChannelDescriptions
 		if(desc == null){
 			return;
 		}
 		
 		try {
 			otDataStore.getChannelDescriptions().removeAll();
 						
 			if(isAutoIncrementing()){
 				// if we are using the dt as a channel then the first element in the channelDescriptions list
 				// is the channel description of the dt
 				DataChannelDescription dCDesc = desc.getDtChannelDescription();
 				OTDataChannelDescription otDCDesc = createOTDataChannelDescription(dCDesc);
 				otDataStore.getChannelDescriptions().add(otDCDesc);				
 			}
 			
 			for(int i=0; i<desc.getChannelsPerSample(); i++){
 				DataChannelDescription dCDesc = desc.getChannelDescription(i);
 				OTDataChannelDescription otDCDesc = createOTDataChannelDescription(dCDesc);
 				otDataStore.getChannelDescriptions().add(otDCDesc);
 			}
 			
 			// initialize the number of samples the datastore is going to be using, if this 
 			// data store is using virtual channels and there is a dt channel this should 
 			// be the actual number of channels - 1.  
 	        int channelsPerSample = desc.getChannelsPerSample();
 	        otDataStore.setNumberChannels(channelsPerSample);
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/* (non-Javadoc)
      * @see org.concord.framework.data.stream.WritableArrayDataStore#setDt(float)
      */
     public void setDt(float dt)
     {
         otDataStore.setDt(dt);
     }
 	
     /**
      * This returns the dt of the datastore.  If there is no 
      * dt it returns Float.NaN
      */
     public float getIncrement()
     {
         return otDataStore.getDt();
     }
     
     /**
      * @return Returns the useDtAsChannel.
      */
     public boolean isAutoIncrementing()
     {
         return !Float.isNaN(otDataStore.getDt());
     }
     
     /**
      * @param useDtAsChannel The useDtAsChannel to set.
      */
     public void setUseDtAsChannel(boolean useDtAsChannel)
     {
         if(!useDtAsChannel) {
             setDt(Float.NaN);
         } else {
             if(Float.isNaN(getIncrement())) {
                 System.err.println("Warning: trying to use dt as a channel without a valid value");
             }
         }
     }
     
 
     /* (non-Javadoc)
      * @see org.concord.framework.data.stream.WritableArrayDataStore#setValues(int, float[], int, int, int)
      */
     public void setValues(int numChannels, float[] values, int offset,
             int numSamples, int nextSampleOffset)
     {
         otDataStore.setDoNotifyChangeListeners(false);
 
         // If this data store is using virtual channels what do we do here?
         // assume the values are starting at channel 1?
         for(int i=0; i<numSamples*nextSampleOffset; 
             i+=nextSampleOffset) {
             for(int j=0;j<numChannels;j++) {
                 Float fValue = new Float(values[offset+i+j]);
                 setValueAt(i/nextSampleOffset, j, fValue);
             }
         }
 
         otDataStore.setDoNotifyChangeListeners(true);
         
         notifyOTValuesChange();
     }
     
 	public void setUseVirtualChannels(boolean flag)
 	{
 		otDataStore.setVirtualChannels(flag);
 	}
 	
 	public boolean useVirtualChannels()
 	{
 	    return otDataStore.isVirtualChannels();
 	}
 
 	/**
 	 * @see org.concord.framework.data.stream.DataStore#addDataStoreListener(org.concord.framework.data.stream.DataStoreListener)
 	 */
 	public void addDataStoreListener(DataStoreListener l)
 	{
 	    WeakReference ref = new WeakReference(l);
 		if (!dataStoreListeners.contains(ref)){
 			dataStoreListeners.add(ref);
 		}
 	}
 
 	/**
 	 * @see org.concord.framework.data.stream.DataStore#removeDataStoreListener(org.concord.framework.data.stream.DataStoreListener)
 	 */
 	public void removeDataStoreListener(DataStoreListener l)
 	{
 	    Vector listenersClone = (Vector) dataStoreListeners.clone();
 	    
 	    for(int i=0; i<listenersClone.size(); i++){
 	    	WeakReference listenerRef = (WeakReference) listenersClone.get(i);
 	    	if(listenerRef.get() == l){
 	    		dataStoreListeners.remove(listenerRef);
 	    	}
 	    }
 	}
 
 	protected void notifyDataAdded()
 	{
 		DataStoreEvent evt = new DataStoreEvent(this, DataStoreEvent.DATA_ADDED);
 		DataStoreListener l;
 		for (int i=0; i<dataStoreListeners.size(); i++){
 		    Reference ref = (Reference)dataStoreListeners.elementAt(i);
 			l = (DataStoreListener)ref.get();
 			
 			// ignore references that have been gc'd
 			if(l == null) continue;
 
 			l.dataAdded(evt);
 		}
 	}
 	
 	protected void notifyDataRemoved()
 	{
 		DataStoreEvent evt = new DataStoreEvent(this, DataStoreEvent.DATA_REMOVED);
 		DataStoreListener l;
 		
 		// Clone our listeners so they can remove them selves from the list 
 		// without the vector up.
 		Vector listenersClone = (Vector) dataStoreListeners.clone();
 		
 		for (int i=0; i<listenersClone.size(); i++){
 		    Reference ref = (Reference)listenersClone.elementAt(i);
 			l = (DataStoreListener)ref.get();
 			
 			// ignore references that have been gc'd
 			if(l == null) continue;
 
 			l.dataRemoved(evt);
 		}
 	}
 	
 	protected void notifyDataChanged()
 	{
 		DataStoreEvent evt = new DataStoreEvent(this, DataStoreEvent.DATA_ADDED);
 		DataStoreListener l;
 		for (int i=0; i<dataStoreListeners.size(); i++){
 		    Reference ref = (Reference)dataStoreListeners.elementAt(i);
 			l = (DataStoreListener)ref.get();
 			
 			// ignore references that have been gc'd
 			if(l == null) continue;
 
 			l.dataChanged(evt);
 		}
 	}
 	
 	protected void notifyChannelDescChanged()
 	{
 		DataStoreEvent evt = new DataStoreEvent(this, DataStoreEvent.DATA_DESC_CHANGED);
 		DataStoreListener l;
 		for (int i=0; i<dataStoreListeners.size(); i++){
 		    Reference ref = (Reference)dataStoreListeners.elementAt(i);
 			l = (DataStoreListener)ref.get();
 			
 			// ignore references that have been gc'd
 			if(l == null) continue;
 
 			l.dataChannelDescChanged(evt);
 		}
 	}
 
 }
