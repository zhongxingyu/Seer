 package it.aekidna.kama;
 
 import it.aekidna.kama.api.IJTLocation;
 import it.aekidna.kama.api.IJTTransform;
 import it.aekidna.kama.api.IJTTransformConfig;
 import it.aekidna.kama.api.IJTTransformFactory;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ObjectNode;
 
public abstract class AbstractBaseTransform implements IJTTransform 
 {
 	protected IJTTransformFactory factory;
 
 	@Override
 	abstract public JsonNode transform(IJTTransformConfig inConfig,
 			IJTLocation inSourceLocation, IJTLocation inTargetLocation); 
 	
 	public void setup(IJTTransformFactory inFactory) {
 		factory = inFactory;
 	}
 	
 	protected final void merge( IJTLocation inTargetLocation, JsonNode inValue)
 	{
 		inTargetLocation.setValue( inValue );
 	}
 	
 	@Override
 	public IJTTransformConfig parseConfig(ObjectNode inNode) {
 		return new BaseTransformConfig( inNode );
 	}
 
 
 }
