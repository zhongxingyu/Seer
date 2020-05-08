 package edu.mit.cci.simulation.pangaea.servlet;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ws.rs.FormParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Response;
 
 
 import edu.mit.cci.simulation.pangaea.core.PangaeaConnection;
 import edu.mit.cci.simulation.pangaea.core.PangaeaException;
 import edu.mit.cci.simulation.pangaea.core.SimulationResults.ScalarElement;
 import edu.mit.cci.simulation.pangaea.core.SimulationInput;
 import edu.mit.cci.simulation.pangaea.core.SimulationResults;
 import edu.mit.cci.simulation.pangaea.core.Variable;
 
 import org.apache.log4j.Logger;
 
 @Path("/")
 @Produces("text/plain")
 public class  RootResource {
 
 	public static Logger log = Logger.getLogger(RootResource.class);
 
 
 	@POST
 	@Produces("text/plain")
 	public Response runSimulation(
 			@FormParam("Pct change in Developed FF emissions") String devdchange,
 			@FormParam("Pct change in Developing A FF emissions") String devingchange,
 			@FormParam("Pct change in Developing B FF emissions") String nonchange,
 			@FormParam("Global land use emissions change") String landUseChange,
 			@FormParam("Target Sequestration") String targSequestration,
 			@FormParam("Developed start year") String devdStart,
 			@FormParam("Developed target year") String devdTarget,
 			@FormParam("Developing A start year") String devingAStart,
 			@FormParam("Developing A target year") String devingATarget,
 			@FormParam("Developing B start year") String devingBStart,
 			@FormParam("Developing B target year") String devingBTarget,
 			@FormParam("Goal for CO2 in the atmosphere") String co2inAtm) {
 
 		SimulationInput input = new SimulationInput();
 		try {
			input.setVariable(SimulationInput.InputVariable.DEVELOPED_FF_CHANGE, getDouble(devdchange, "Pct change in Developed FF emissions", null));
			input.setVariable(SimulationInput.InputVariable.DEVELOPINGA_FF_CHANGE, getDouble(devingchange, "Pct change in Developing A FF emissions", null));
			input.setVariable(SimulationInput.InputVariable.DEVELOPINGB_FF_CHANGE, getDouble(nonchange, "Pct change in Developing B FF emissions", null));
 			input.setVariable(SimulationInput.InputVariable.DEFORESTATION, getDouble(landUseChange, "Global land use emissions change", null));
 			input.setVariable(SimulationInput.InputVariable.AFFORESTATION, getDouble(targSequestration, "Target Sequestration", null));
 			input.setVariable(SimulationInput.InputVariable.DEVELOPED_FF_START, getDouble(devdStart, "Developed start year", null));
 			input.setVariable(SimulationInput.InputVariable.DEVELOPED_FF_TARGET, getDouble(devdTarget, "Developed target year", null));
 			input.setVariable(SimulationInput.InputVariable.DEVELOPINGA_FF_START, getDouble(devingAStart, "Developing A start year", null));
 			input.setVariable(SimulationInput.InputVariable.DEVELOPINGA_FF_TARGET, getDouble(devingATarget, "Developing A target year", null));
 			input.setVariable(SimulationInput.InputVariable.DEVELOPINGB_FF_START, getDouble(devingBStart, "Developing B start year", null));
 			input.setVariable(SimulationInput.InputVariable.DEVELOPINGB_FF_TARGET, getDouble(devingBTarget, "Goal for CO2 in the atmosphere", null));
 		}
 		catch (NumberFormatException e) {
 			e.printStackTrace();
 		}
 		
 		//input.setVariable(SimulationInput.VensimVariable.AFFORESTATION,targSequestration);
 		//input.setVariable(SimulationInput.VensimVariable.C, co2inAtm);
 		Response r = null; 
 		try {
 			PangaeaConnection connection = new PangaeaConnection();
 			SimulationResults result = connection.submit(input);
 			r = Response.ok(createTextResult(result)).build();
 		}
 		catch (PangaeaException e) {
 			log.error("Exception has been thrown by pangaea core", e);
 			r = Response.serverError().build();
 		}
 
 		return r;
 
 	}
 
 	private Double getDouble(String str, String name, Double defaultVal) {
 		if (str != null) {
 			try {
				return Double.parseDouble(str);
 			}
 			catch (NumberFormatException e) {
 				log.warn("Can't parse parameter " + name + ", value: " + str);
 			}
 		}
 		return defaultVal;
     }
 
 	String createTextResult(SimulationResults result) {
 		StringBuilder buffer = new StringBuilder();
 		List<Variable> vars = new ArrayList<Variable>(result.getPopulatedVariables());
 		
 		Collections.sort(vars, new Comparator<Variable>() {
 			public int compare(Variable o1, Variable o2) {
 				return o1.getInternalName().compareTo(o2.getInternalName());
 			}
 		});
 
 
         Map<String,Object> tmp = new HashMap<String,Object>();
 		for (Variable v: vars) {
 
             List<ScalarElement> vresult = result.get(v);
             String[] array = new String[vresult.size()];
             for (int i = 0;i<vresult.size();i++) {
                 array[i] = vresult.get(i).val.doubleValue()+"";
             }
 			tmp.put(v.getInternalName(),array);
 
 		}
 		return U.stringify(tmp);
 	}
 
 }
