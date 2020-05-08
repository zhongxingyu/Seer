 package org.stwerff.mslagents;
 
 import java.io.BufferedReader;
import java.io.File;
 import java.io.InputStreamReader;
 
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
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
					String path = getSession().getRequest().getServletContext().getRealPath("/");
					path+="/c_bin/";
					File pathFile = new File(path);
					File test = new File(path+"msl_lmst");
					if (!test.canExecute()){
						Runtime.getRuntime().exec("chmod +x msl_lmst",null,pathFile);
					}
					DateTime time = new DateTime(image.getUnixTimeStamp());
					Process proc = Runtime.getRuntime().exec("./msl_lmst "+time.toString(ISODateTimeFormat.dateHourMinuteSecond()), null,pathFile);
					String lmst = new BufferedReader(new InputStreamReader(proc.getInputStream())).readLine();
 					proc.destroy();
					String[] fields = lmst.split(":");
					String solString = String.format("%05d", image.getSol());
					if (("1/"+solString).equals(fields[0])){
						image.setLmst(fields[1]+":"+fields[2]+":"+fields[3]);	
					} else {
						System.err.println("Failed to convert to correct LMST, wrong sol?");
					}
					
					result.add(om.valueToTree(image));
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
