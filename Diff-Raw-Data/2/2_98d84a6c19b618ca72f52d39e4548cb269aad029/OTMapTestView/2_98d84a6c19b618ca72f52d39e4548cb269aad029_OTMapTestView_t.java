 package org.concord.otrunk.test;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTResourceMap;
 import org.concord.framework.otrunk.view.OTObjectView;
 
 public class OTMapTestView implements OTObjectView {
 
 	HashMap map;
 	
 	public OTMapTestView()
 	{
 		map = new HashMap();
 		map.put("myString", "hello world");
 		map.put("myInteger", new Integer("10"));
 		map.put("myFloat", new Float("33.1"));
 		String testBlobStr = "hello world,";
 		map.put("myBlob", testBlobStr.getBytes());
 		testBlobStr = "This is longer so we can see how line breaks " +
 			"work.  Because it is so compress it will probably take " +
 			"a lot to make two lines.";
 		map.put("myLongBlob", testBlobStr.getBytes());		
 	}
 	
 	public JComponent getComponent(OTObject otObject, boolean editable) {
 		OTResourceMap otMap = ((OTMapTestObject)otObject).getResourceMap();
 		if(otMap.size() == 0){
 			Set entries = map.entrySet();
 			Iterator iter = entries.iterator();
 			while(iter.hasNext()){
 				Map.Entry entry = (Map.Entry)iter.next();
				otMap.put((String)entry.getKey(), entry.getValue());
 			}
 			
 			return new JLabel("Map Initialized");			
 		}
 
 		if(otMap.size() != map.size()) {
 			return new JLabel("Map Size doesn't match");						
 		}
 		
 		Set entries = map.entrySet();
 		Iterator iter = entries.iterator();
 		while(iter.hasNext()){
 			Map.Entry entry = (Map.Entry)iter.next();
 			Object value = otMap.get((String)entry.getKey());
 			if(value instanceof byte[]) {
 				if(!checkBytes(value, entry.getValue())){
 					return new JLabel("Map entries (byte []) doen't match");									
 				}
 			} else if(!value.equals(entry.getValue())){
 				return new JLabel("Map entries doen't match");				
 			}
 		}
 		
 		return new JLabel("Map passes test");
 	}
 
 	public static boolean checkBytes(Object bytes1, Object bytes2)
 	{
 		byte [] otBytes = (byte[])bytes2;
 		byte [] bytes = (byte[])bytes2;
 		if(otBytes.length != bytes.length) {
 			return false;
 		}
 		for(int i=0; i<otBytes.length; i++){
 			if(otBytes[i] != bytes[i]){
 				return false;
 			}
 		}
 
 		return true;
 	}
 	
 	public void viewClosed() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
