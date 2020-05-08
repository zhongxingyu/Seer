 package it.aekidna.kama;
 
 import it.aekidna.kama.api.IJTTransform;
 import it.aekidna.kama.api.IJTTransformFactory;
 import it.aekidna.kama.api.IJTTransformSetupDelegate;
 import it.aekidna.kama.api.IJTTransformationSetup;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ObjectNode;
 
 public class JTDefaultTransformFactory implements IJTTransformFactory {
 	
 	private Map<String, Class<?>>	aliases; 
 	
 	public JTDefaultTransformFactory()
 	{
 		aliases = new HashMap<String, Class<?>>();
 		aliases.put("pipe", PipeTransform.class);
 		aliases.put("map", MapTransform.class);
 		aliases.put("each", EachTransform.class);
 		aliases.put("remove", RemoveTransform.class);
 		aliases.put("hash", HashTransform.class);
 		aliases.put("copy", CopyTransform.class);
 		aliases.put("sub", SubTransformSetupDelegate.class);
 	}
 	
 	public void addAlias( String inName, Class<?> inClass )
 	{
 		if( aliases.containsKey(inName))
 			throw new RuntimeException("Attempt to override alias '" + inName +"'");
		if( !IJTTransform.class.isAssignableFrom( inClass ) && !IJTTransformSetupDelegate.class.isAssignableFrom( inClass ))
 			throw new RuntimeException("Attempt to add an alias of class'" + inClass.getCanonicalName() +"' " +
 					"that is neither a IJTTransform nor a IJTTransformSetupDelegate implementation");
 		aliases.put( inName, inClass );
 	}
 	
 	
 	public IJTTransformationSetup getTransformByPath( String inPath )
 	{
 		ObjectMapper mapper = new ObjectMapper();
 		ObjectNode node = null;
 		try {
			System.out.println("Returning transformer from file '" + inPath +"'");
 			node = (ObjectNode) mapper.readTree( JTDefaultTransformFactory.class.getClassLoader().getResource( inPath ).openStream());
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException("cannot resolve transform by path '" + inPath +"'");
 		}
 		return resolveTransform( node );
 	}
 	
 	@Override
 	public IJTTransformationSetup resolveTransform( ObjectNode inNode )
 	{
 		if( !inNode.has("!"))
 			throw new RuntimeException("transformation node doesn't have a qualifier");
 		
 		final String qualifier = inNode.get("!").asText();
 		IJTTransformationSetup setup = null;
 		Class<?> clazz = null;
 		if( qualifier.matches("^[a-z]\\w*$") && aliases.containsKey(qualifier))
 		{
 			clazz = aliases.get(qualifier);
 		}
 		else
 		{
 			try {
 				clazz = Class.forName(qualifier);
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}		
 		}
 		if( clazz != null)
 		{
 			try {
				if( IJTTransform.class.isAssignableFrom( clazz ) )
 				{
 					IJTTransform transform = null;
 						transform = (IJTTransform) clazz.newInstance();
 						setup = new JTTransformationSetup( transform, transform.parseConfig(inNode));
 				}
				else if( IJTTransformSetupDelegate.class.isAssignableFrom( clazz ) )
 				{
 					setup = ((IJTTransformSetupDelegate) clazz.newInstance()).setupTransform( this, inNode);
 				}
 				else
 				{
					throw new RuntimeException( "Provided qualifier '"
							+ qualifier + "' is neither a IJTTransform " +
							"nor a IJTTransformSetupDelegate implementation" );
 				}
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		}
 		if( setup == null )
 		{
 			throw new RuntimeException("unknown transformation qualifier: " + qualifier );
 		}
		setup.getTransform().setup(this);
 		return setup;
 	}
 
 }
 
 class SubTransformSetupDelegate implements IJTTransformSetupDelegate
 {
 
 	@Override
 	public IJTTransformationSetup setupTransform( IJTTransformFactory inFactory, ObjectNode inNode) {
 		System.out.println("Invoking sub");
 
 		ObjectMapper mapper = new ObjectMapper();
 		String sourcePath = inNode.get("path").asText();
 		ObjectNode config = null;
 		try {
 			config = (ObjectNode) mapper.readTree( IJTTransform.class.getClassLoader().getResource( sourcePath ).openStream());
 			if( inNode.has("source"))
 				config.put("source", inNode.get("source"));
 			if( inNode.has("target"))
 				config.put("target", inNode.get("target"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return inFactory.resolveTransform(config);
 	}
 	
 }
