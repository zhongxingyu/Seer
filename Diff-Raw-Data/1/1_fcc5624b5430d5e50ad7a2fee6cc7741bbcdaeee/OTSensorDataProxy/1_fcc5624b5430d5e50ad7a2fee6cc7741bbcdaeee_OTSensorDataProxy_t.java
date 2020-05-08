 /*
  * Created on Jan 12, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.concord.sensor.state;
 
 import java.util.Vector;
 
 import org.concord.framework.data.stream.DataConsumer;
 import org.concord.framework.data.stream.DataListener;
 import org.concord.framework.data.stream.DataProducer;
 import org.concord.framework.data.stream.DataStreamDescription;
 import org.concord.framework.otrunk.DefaultOTObject;
 import org.concord.framework.otrunk.OTResourceSchema;
 import org.concord.sensor.ExperimentRequest;
 import org.concord.sensor.SensorDataManager;
 import org.concord.sensor.SensorDataProducer;
 import org.concord.sensor.device.impl.DataStreamDescUtil;
 
 /**
  * @author Informaiton Services
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class OTSensorDataProxy extends DefaultOTObject 
 	implements DataProducer, DataConsumer 
 {
 	public static interface ResourceSchema extends OTResourceSchema
 	{
 		OTExperimentRequest getRequest();		
 	}
 	
 	private ResourceSchema resources;
 	private SensorDataManager sensorManager;
 	private SensorDataProducer producer = null;
 
 	private boolean running = false;
 	
 	private Vector dataListeners = new Vector();
 	
 	/**
 	 * @param resources
 	 */
 	public OTSensorDataProxy(ResourceSchema resources,
 			SensorDataManager sdm) {
 		super(resources);
 		
 		this.resources = resources;
 		sensorManager = sdm;
 		// TODO Auto-generated constructor stub
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataProducer#addDataListener(org.concord.framework.data.stream.DataListener)
 	 */
 	public void addDataListener(DataListener listener) 
 	{
 		// people might add data listeners to us
 		// before the real sensor data producer is ready
 		// so we'll need to proxy these listeners
 		
 		// we can either proxy the list of listeners
 		// or we can proxy the events themselves.
 		// on issue might be the event source.  If someone
 		// is testing whether the source matches this object
 		// then they will get screwed up unless we change
 		// the source.
 		dataListeners.add(listener);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataProducer#removeDataListener(org.concord.framework.data.stream.DataListener)
 	 */
 	public void removeDataListener(DataListener listener) 
 	{
 		dataListeners.remove(listener);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataProducer#getDataDescription()
 	 */
 	public DataStreamDescription getDataDescription() 
 	{
 		DataStreamDescription dDesc = new DataStreamDescription();
 		
 		DataStreamDescUtil.setupDescription(dDesc, 
 				resources.getRequest(), null);
 		
 		// this should return a partially correct description
 		// before the real device is ready.  some fields
 		// will be missing or approximate: period, stepSize
 		
 		// TODO we should check if the device has been created
 		// and then return the correct description if it has
 		return dDesc;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.DataFlow#start()
 	 */
 	public void start() 
 	{
 		// ask the devicemanager to configure the device with
 		// our experimentrequest, once the producer from the 
 		// device is recieved we should start it and pass 
 		// connect up the currently attached data listeners
 		// the datamanager should be careful so it doens't
 		// start to requests at once.  
 
 		running = true;
 		ExperimentRequest request = resources.getRequest();		
 		sensorManager.prepareDataProducer(request, this);		
 
 		// FIXME we are assuming this method will be single threaded
 		// which might not always be true
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.DataFlow#stop()
 	 */
 	public void stop() 
 	{
 		// stop the proxied dataProducer
 		// the dataProducer might be stopped already this
 		// could happen if some other proxy started it.
 		
 		// FIXME we will a potential memory leak here unless
 		// we clean up these listeners.
 		running = false;
 		if(producer != null) {
 			producer.stop();
 		}
 	}	
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.DataFlow#reset()
 	 */
 	public void reset() 
 	{
 		// as far as I know these devices don't need a reset
 		// so this can be ignored
 	}
 	
 	public void init()
 	{
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataConsumer#addDataProducer(org.concord.framework.data.stream.DataProducer)
 	 */
 	public void addDataProducer(DataProducer source) 
 	{
 		producer = (SensorDataProducer)source;
 		if(running) {
 			// this data producer has been already started
 			// for now lets add all of our current data listeners
 			// to this new producer and then start it.
 
 			// FIXME we should proxy the data events too 
 			// because the source of the data event should be this
 			// instead of the sensorDataProducer;
 			for(int i=0; i<dataListeners.size(); i++) {
 				producer.addDataListener((DataListener)dataListeners.get(i));
 			}
 			producer.start();
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.data.stream.DataConsumer#removeDataProducer(org.concord.framework.data.stream.DataProducer)
 	 */
 	public void removeDataProducer(DataProducer source) {
 		// TODO Auto-generated method stub
 	}
 }
