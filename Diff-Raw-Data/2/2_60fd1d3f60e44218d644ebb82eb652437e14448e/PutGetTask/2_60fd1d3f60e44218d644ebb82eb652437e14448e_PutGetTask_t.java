 package com.amm.nosql.vtest.keyvalue;
 
 import com.amm.nosql.data.KeyValue;
 import com.amm.vtest.FailureException;
 import com.amm.vtest.services.callstats.CallStats;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicLong;
 import org.apache.log4j.Logger;
 
 public class PutGetTask extends KeyValueTask {
 	private static final Logger logger = Logger.getLogger(PutGetTask.class);
 	private volatile AtomicLong count = new AtomicLong(0);
 	private boolean debug = logger.isDebugEnabled();
 	private boolean checkValue ;
 
 	public PutGetTask(KeyValueTaskConfig config) throws Exception {
 		super(config);
		setName("PutGet");
 	}
 
 	public PutGetTask(KeyValueTaskConfig config, String name) throws Exception {
 		super(config);
 		setName(name);
 	}
 
 	public void execute(CallStats stats) throws FailureException, Exception {
 		try {
 			KeyValue keyValue = put();
 			get(keyValue);
 		} finally  {
 			count.getAndIncrement();
 		}
 	}
 
 	private KeyValue put() throws FailureException, Exception {
 		String key = getNextKey();
 		byte [] value = getValue().getBytes();
 
 		KeyValue keyValue = new KeyValue(key,value);
 		if (debug)
 			logger.debug("count="+count + " keySize="+key.length() +" valueSize="+value.length
 				+" key="+key +" value="+new String(value)
 				);
 
 		getKeyValueDao().put(keyValue);
 		return keyValue;
 	}
 
 	private void get(KeyValue keyValue) throws FailureException, Exception {
 		String key = keyValue.getKey();
 		KeyValue keyValue2 = getKeyValueDao().get(key);
 		if (keyValue2 == null)
 			throw new FailureException("Cannot get key="+key);
 
 		if (checkValue) {
 			if (!keyValue.getKey().equals(keyValue2.getKey()))
 				throw new FailureException("Keys do not match. key1="+key+" key2="+keyValue2.getKey());
 
 			byte [] value1 = keyValue.getValue();
 			byte [] value2 = keyValue2.getValue();
 			if (! value1.equals(value2))
 				throw new FailureException("Value does not match for key="+key);
 		}
 	}
 
 	public void setCheckValue(boolean checkValue) { this.checkValue = checkValue ; }
 }
