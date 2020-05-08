 package edu.wheaton.simulator.expression;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sourceforge.jeval.VariableResolver;
 import net.sourceforge.jeval.function.FunctionException;
 import edu.wheaton.simulator.entity.Entity;
 
 public class EntityFieldResolver implements VariableResolver {
 
 	private Map<String, Entity> entityMap;
 
 	/*
 	 * default constructor
 	 */
 	public EntityFieldResolver() {
 		entityMap = new HashMap<String, Entity>();
 	}
 
 	/*
 	 * copy constructor
 	 */
 	public EntityFieldResolver(EntityFieldResolver resolver) {
 		entityMap.putAll(resolver.entityMap);
 	}
 
 	@Override
 	public String resolveVariable(String variableName)
 			throws FunctionException {
		// TODO method stub
		throw new FunctionException(variableName);
 	}
 
 	public void addEntity(String alias, Entity entity) {
 		entityMap.put(alias, entity);
 	}
 
 }
