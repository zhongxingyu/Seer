 package storage.dight;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import se.sics.dight.data.model.EntryId;
 import se.sics.dight.data.model.Objekt;
import se.sics.dight.data.model.templates.ValueTemplate;
 import se.sics.dight.data.model.values.Value;
 
 public class ApplicationRecord extends BaseRecord{
 	
 	private static ApplicationKlass klassContainer;
 	
 	public ApplicationRecord(){
 		super();
 		klassContainer = new ApplicationKlass(e);
 		record = klassContainer.getKlass();		
 	}
 	
 	public String newEntry(String application, String type, Object data){
 		klassContainer = new ApplicationKlass(e);
 		Value vApplication = e.makeStringValue(application);
 		Value vType = e.makeStringValue(type);
 		
 		int value=1;
 		byte[] byts = new byte[]{(byte)(value >>> 24),(byte)(value >>> 16),(byte)(value >>> 8),(byte)value,(byte)(value >>> 24),(byte)(value >>> 16),(byte)(value >>> 8),(byte)value,(byte)(value >>> 24),(byte)(value >>> 16),(byte)(value >>> 8),(byte)value,(byte)(value >>> 24),(byte)(value >>> 16),(byte)(value >>> 8),(byte)value,(byte)(value >>> 24),(byte)(value >>> 16),(byte)(value >>> 8),(byte)value};
 		EntryId eid = e.makeEntryId(byts);
 		
 		byte[] payload = null;
 		try {
 			payload = serialize(data);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		Objekt entry = e.createObjekt(klassContainer.getKlass(), eid, payload );
 		entry.setValue(klassContainer.type, vType);
 		entry.setValue(klassContainer.application, vApplication);		
 		entry.commit(klassContainer.getEntryAuthenticationAlgorithm());
 		//Send to server
 		return eid.getId().toString();
 	}
 	
 	public Set<Object> getApplicationData(String application, String type){
 		HashMap<String, Object> h = new HashMap<String, Object>();
 		Set<HashMap> s = new HashSet<HashMap>();
		ValueTemplate vApplication = e.makeStringValueTemplate(application);
		ValueTemplate vType = e.makeStringValueTemplate(type);
 		
 		h.put("attribute", klassContainer.type);
 		h.put("value", vType);
 		s.add(h);
 		
		h.put("attribute", klassContainer.application);
		h.put("value", vApplication);
		s.add(h);
		
	
		
 		return attributesQueryToObject(s);
 		
 	}
 
 }
