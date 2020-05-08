 package net.unikernel.bummel.logic_elements.Analyzer;
 
 import java.util.Map;
 import net.unikernel.bummel.project_model.api.BasicElement;
 import net.unikernel.bummel.project_model.api.Element.ElementData;
 import org.openide.util.lookup.ServiceProvider;
 
 /**
  * <b>Pinout:</b>
  * <ol start='0'>
  * <li>Input</li>
  * </ol>
  * @author mcangel
  */
 @ServiceProvider(service=BasicElement.class)
 @ElementData(dataFile="element_info.xml")
 public class Analyzer extends BasicElement
 {
 	/**
 	 * Creates analyzer with one port
 	 */
 	public Analyzer()
 	{
 		super(new String[]{"input"});
 		setState(0);
 	}
 	/**Sets analyzers state same as on the input port
 	 * <b>Pinout:</b>
 	 * <ol start='0'>
 	 * <li>Input</li>
 	 * </ol>
 	 * @return
 	 */
 	@Override
 	public Map<String, Double> process(Map<String, Double> valuesOnPorts)
 	{
		valuesOnPorts = nullFreePortsOf(valuesOnPorts);
 		if(valuesOnPorts.get(getPorts().get(0)).compareTo(new Double(0)) != 0)
 			setState(1);	//light it
 		else
 			setState(0);	//douse it
 		valuesOnPorts.put("input", 0.);
 		return valuesOnPorts;
 	}
 }
