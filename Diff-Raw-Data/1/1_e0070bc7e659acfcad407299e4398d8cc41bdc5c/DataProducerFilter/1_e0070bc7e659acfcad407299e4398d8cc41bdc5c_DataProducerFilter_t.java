 /**
  * 
  */
 package org.concord.data.stream;
 
 import org.concord.framework.data.stream.DataListener;
 import org.concord.framework.data.stream.DataProducer;
 import org.concord.framework.data.stream.DataStreamDescription;
 import org.concord.framework.data.stream.DataStreamEvent;
 import org.concord.framework.data.stream.DefaultDataProducer;
 
 /**
  * @author scott
  *
  */
 public abstract class DataProducerFilter extends DefaultDataProducer
 {
     private DataProducer source;
 	private DataListener dataListener;
 	private int sourceChannel;
 
     
 	public DataProducerFilter(){
 		/*
 		 * Send the data listening to this inner class so
 		 * it hides that from users of this class.
 		 */
 		this.dataListener = new DataListener() {
 			public void dataReceived(DataStreamEvent dataEvent)
 			{
 				DataProducerFilter.this.dataReceived(dataEvent);
 			}  
 
 			public void dataStreamEvent(DataStreamEvent dataEvent)
 			{
 				updateDescription();
 				// need to check for description updates
 				notifyDataStreamEvent(dataEvent.getType());
 			}
 		};
 		
 		// make sure values has enough space for our data
 		// this array is the same one used in dataEvent.data
 		values = new float [100];
		dataEvent.data = values;
 	}
 
 	/* (non-Javadoc)
      * @see org.concord.framework.data.stream.DataConsumer#addDataProducer(org.concord.framework.data.stream.DataProducer)
      */
     public void setSource(DataProducer source)
     {
         if(this.source != null) {
             this.source.removeDataListener(dataListener);
         }
         this.source = source;
 
         if(source == null) {
             return;
         }
         
         updateDescription();
         this.source.addDataListener(dataListener);
     }
 
     protected void updateDescription()
     {
     	DataStreamDescription srcDesc = source.getDataDescription();
     	dataDesc.setDt(srcDesc.getDt());
     	dataDesc.setChannelsPerSample(srcDesc.getChannelsPerSample());
     	dataDesc.setDataType(srcDesc.getDataType());
     	dataDesc.setDataOffset(srcDesc.getDataOffset());
     	dataDesc.setNextSampleOffset(srcDesc.getNextSampleOffset());
     }
     
     public DataProducer getSource()
     {
     	return source;
     }
     
     /* (non-Javadoc)
      * @see org.concord.framework.data.DataFlow#reset()
      */
     public void reset()
     {
         if(source != null) {
             source.reset();
         }
     }
 
     /* (non-Javadoc)
      * @see org.concord.framework.data.DataFlow#stop()
      */
     public void stop()
     {
         if(source != null) {
             source.stop();
         }
     }
 
     /* (non-Javadoc)
      * @see org.concord.framework.data.DataFlow#start()
      */
     public void start()
     {
         if(source != null) {
             source.start();
         }
     }
 
     protected void dataReceived(DataStreamEvent dataEvent)
     {
         int producerChannel = getTranslatedSourceChannel();
         int numSamples = dataEvent.getNumSamples();
         int offset = dataEvent.getDataDescription().getDataOffset();
         int stride = dataEvent.getDataDescription().getNextSampleOffset();
         int channelsPerSample = dataEvent.getDataDescription().getChannelsPerSample();
         
         // send the event to our listeners
         // however we need to do the actual filter
         // here and adjust the values
         for(int i=0; i<numSamples; i++){
         	
         	// initialize the other channels
         	for(int j=0; j<channelsPerSample; j++){
         		dataEvent.data[offset+j] = dataEvent.data[offset+j];
         	}
         	
         	float value = dataEvent.data[offset + producerChannel];    
         	        	
         	this.dataEvent.data[offset + producerChannel] = 
         		filter(value);
         	
         	offset += stride;
 
         }
         
         notifyDataReceived();        
     }
 
     
 	/**
      * @param value
      * @return
      */
     protected abstract float filter(float value);
 
     /**
      * get the channel number that will be filtered.  This translates
      * the sourceChannel to the actual channel in the source data producer
      * If the sourceChannel is 0 it means the first channel.  If the dataProducer
      * has a dt then this will substract 1 from the sourceChannel.  So sourceChannel
      * of 0 returns a translatedSourceChannel of -1.  -1 is the dt value in a data
      * producer.   
      * @return
      */
     protected int getTranslatedSourceChannel()
     {
         if(source == null) {
             throw new RuntimeException("Null source");
         }
         
         if(source.getDataDescription().getDataType() ==
             DataStreamDescription.DATA_SEQUENCE) {
             // has a dt
             return sourceChannel - 1;
         } else {
             return sourceChannel;
         }
     }
     
     /**
      * set the channel number that will be filtered
      * this uses the virtual channel like is used by the DataGraphable
      * class.  So channel 0 means the first channel.  Even if the 
      * DataProducer has a dt which makes the first channel -1.  If the
      * DataProducer doesn't have a dt then the first channel is 0.  
      * 
      * @param channelNumber
      */
     public void setSourceChannel(int channelNumber)
     {
         // set the channel number 
     	sourceChannel = channelNumber;
     }   
     
     public int getSourceChannel()
     {
     	return sourceChannel;
     }
     
 }
