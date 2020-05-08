 package com.example.formidable;
 
 import org.codehaus.jackson.JsonNode;
 import org.ektorp.ViewQuery;
 import org.ektorp.ViewResult;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.UUID;
 
 public class FormidableTest extends FormidableTestCase {
 
     public void testReadingView() {
         String recordId = newId();
         createEvent(6, recordId, "surname", "Bhuwalka");
         createEvent(2, recordId, "name", "Angshu");
         createEvent(1, recordId, "name", "Chris");
 
         ViewQuery viewQuery = new ViewQuery()
                 .group(true)
                 .groupLevel(1)
                 .designDocId("_design/records")
                 .viewName("latest");
 
         Map<String, Object> patient = getOnlyResult(viewQuery);
         assertEquals("Angshu", patient.get("name"));
 
         createEvent(4, recordId, "name", "Vivek");
         createEvent(5, recordId, "surname", "Singh");
         
         Map<String, Object> skills = new HashMap<String, Object>();
        skills.put("abac339", "computer hacking");
		createEvent(8, recordId, "skills", skills );
		
        Map<String, Object> skills2 = new HashMap<String, Object>();
        skills2.put("34ac333", "nunchuku");
		createEvent(7, recordId, "skills", skills2 );
 
         patient = getOnlyResult(viewQuery);
         assertEquals("Vivek", patient.get("name"));
         assertEquals("Bhuwalka", patient.get("surname"));
         assertTrue(((Map<String, Object>) patient.get("skills"))
         		.containsValue("nunchuku"));
     }
 
     private Map<String, Object> getOnlyResult(ViewQuery viewQuery ) {
     	
         ViewResult result = events.queryView(viewQuery);
 
         ViewResult.Row record = result.getRows().get(0);     
         JsonNode data = record.getValueAsNode().get("data");
         JsonNode surname = data.get("surname");
         JsonNode name = data.get("name");
         
         Map<String, Object> patient = new HashMap<String, Object>();
         patient.put("name", name.getTextValue());
         patient.put("surname", surname.getTextValue());
         Map<String, Object> skills = new HashMap<String, Object>();
         
         if(data.get("skills") != null) {
         		for (Iterator<Entry<String, JsonNode>> iter = data.get("skills").getFields(); iter.hasNext(); ) {
         			Entry<String, JsonNode> entry = iter.next();
         			skills.put(entry.getKey(), entry.getValue().getTextValue());
         		}
 			patient.put("skills", skills);
         }
   
         return patient;
     }
 
     private void createEvent(int epoch, String recordId, String key, Object value) {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put(key, value);
         Event event = new Event(epoch, recordId, map);
         events.create(newId(), event);
     }
 
     private String newId() {
         return UUID.randomUUID().toString();
     }
 
 }
