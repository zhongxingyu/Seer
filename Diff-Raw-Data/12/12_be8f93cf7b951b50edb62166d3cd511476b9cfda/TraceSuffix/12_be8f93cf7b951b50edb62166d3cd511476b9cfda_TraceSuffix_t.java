 package net.sf.pipitrace.core.model;
 
 import java.util.Map;
 
 import net.sf.commonstringutil.StringUtil;
 
 import com.google.common.base.Splitter;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 import com.google.common.collect.UnmodifiableIterator;
 
 public class TraceSuffix extends TraceBiMap{
 
 	public static final String KEY_PREV_COMM  = "prev_comm";
 	public static final String KEY_PREV_PID   = "prev_pid";
 	public static final String KEY_PREV_PRIO  = "prev_prio";
 	public static final String KEY_PREV_STATE = "prev_state";
 	public static final String KEY_NEXT_COMM  = "next_comm";
 	public static final String KEY_NEXT_PID   = "next_pid";
 	public static final String KEY_NEXT_PRIO  = "next_prio";
 
 	private ImmutableMap<Integer, Integer> im;
 	
 	public TraceSuffix(ImmutableMap<Integer, Integer> im) {
 		super();
 		this.im = im;
 	}
 
 	public String getValue(String key){
 		
 		int iKey = getIntValue(key);
 		int iValue = this.im.get(iKey);
 		return getStringValue(iValue);
 		
 	}
 	
 	public static TraceSuffix create(String traceSuffStr){
 		
 		traceSuffStr = StringUtil.replace(traceSuffStr, " ", ",");
 		
		Map<String, String> map = null;
		
		try{
			
			map = Splitter.on(',').trimResults().withKeyValueSeparator("=").split(traceSuffStr);
		
		}catch(Exception e){
			
			System.out.println("traceSuffStr : "+ traceSuffStr);
			
		}
 		
 		ImmutableMap<String, String> im = ImmutableMap.<String, String>builder().putAll(map).build();
 		
 		UnmodifiableIterator<String> it = im.keySet().iterator();
 		
 		Builder<Integer, Integer> builder = ImmutableMap.<Integer, Integer>builder();
 		
 		while(it.hasNext()){
 			
 			String key = it.next();
 			String value = im.get(key);
 			
 			int iKey = getIntValue(key.trim());
 			
 			int iValue = getIntValue(value.trim());
 			
 			builder.put(iKey, iValue);
 		}
 		
 		ImmutableMap<Integer, Integer> vm = builder.build();
 		
 		return new TraceSuffix(vm);
 	}
 }
