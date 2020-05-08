 package org.integratedmodelling.thinklab.client.modelling;
 
 import java.util.Map;
 
 import org.integratedmodelling.thinklab.api.modelling.parsing.IFunctionDefinition;
 
 public class FunctionDefinition extends ModelObject implements IFunctionDefinition {
 
 	String _type;
 	Map<String, Object> _parameters;
 	
 	@Override
 	public void set(String type, Map<String, Object> parms) {
 		_type = type;
 		_parameters  = parms;
 	}
 
	@Override
	public Map<String, Object> getParameters() {
		return _parameters;
	}

	@Override
	public String getId() {
		return _type;
	}
	
 }
