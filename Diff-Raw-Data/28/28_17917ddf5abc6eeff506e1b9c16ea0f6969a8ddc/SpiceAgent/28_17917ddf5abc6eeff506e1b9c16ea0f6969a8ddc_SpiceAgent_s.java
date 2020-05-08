 package org.stwerff.mslagents;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import org.stwerff.mslagents.data.Image;
 
 import com.almende.eve.agent.Agent;
 import com.almende.eve.json.annotation.Name;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 
 public class SpiceAgent extends Agent {
 	public static final ObjectMapper om = new ObjectMapper();
 	
 	public ArrayNode updateList(@Name("list") ArrayNode list){
 		ArrayNode result = om.createArrayNode();
 		try {
 			for (int i=0; i<list.size(); i++){
 				Image image = om.treeToValue(list.get(i),Image.class);
 				if (image.getLmst() == null){
					Process proc = Runtime.getRuntime().exec("pwd", null,null);
					System.err.println(new BufferedReader(new InputStreamReader(proc.getInputStream())).readLine());
 					proc.destroy();
					//result.add(om.valueToTree(image));
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 	@Override
 	public String getDescription() {
 		return "Converts image time to LMST time";
 	}
 
 	@Override
 	public String getVersion() {
 		return "1.0";
 	}
 
 }
