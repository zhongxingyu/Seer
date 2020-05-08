 package ecologylab.serialization.deserializers.pullhandlers.stringformats;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.Collection;
 import java.util.Map;
 
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonToken;
 
 import ecologylab.generic.Debug;
 import ecologylab.net.ParsedURL;
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.DeserializationHookStrategy;
 import ecologylab.serialization.FieldDescriptor;
 import ecologylab.serialization.FieldTypes;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.ScalarUnmarshallingContext;
 import ecologylab.serialization.TranslationContext;
 import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.deserializers.pullhandlers.PullDeserializer;
 import ecologylab.serialization.types.element.IMappable;
 
 /**
  * JSON deserialization handler class. Uses the pull API for parsing the input JSON documents.
  * 
  * @author nabeelshahzad
  * 
  */
 public class JSONPullDeserializer extends StringPullDeserializer
 {
 
 	/**
 	 * JsonParser object from the Jackson JSON parsing library. Implements a pull API for parsing JSON
 	 */
 	JsonParser	jp	= null;
 
 	public JSONPullDeserializer(SimplTypesScope translationScope,
 			TranslationContext translationContext, DeserializationHookStrategy deserializationHookStrategy)
 	{
 		super(translationScope, translationContext, deserializationHookStrategy);
 	}
 
 	public JSONPullDeserializer(SimplTypesScope translationScope,
 			TranslationContext translationContext)
 	{
 		super(translationScope, translationContext);
 	}
 
 	@Override
 	public Object parse(InputStream inputStream, Charset charSet) throws SIMPLTranslationException
 	{
 		try
 		{
 			configure(inputStream, charSet);
 			return parse();
 		}
 		catch (Exception ex)
 		{
 			throw new SIMPLTranslationException("exception occurred in deserialzation ", ex);
 		}
 	}
 
 	@Override
 	public Object parse(InputStream inputStream) throws SIMPLTranslationException
 	{
 		try
 		{
 			configure(inputStream);
 			return parse();
 		}
 		catch (Exception ex)
 		{
 			throw new SIMPLTranslationException("exception occurred in deserialzation ", ex);
 		}
 	}
 	
 	/**
 	 * The main parse method accepts a CharSequence and creates a corresponding object model. Sets up
 	 * the root object and creates instances of the root object before calling a recursive method that
 	 * creates the complete object model
 	 * 
 	 * @param charSequence
 	 * @return
 	 * @throws SIMPLTranslationException
 	 * @throws JsonParseException
 	 * @throws IOException
 	 * @throws SIMPLTranslationException
 	 */
 	public Object parse(CharSequence charSequence) throws SIMPLTranslationException
 	{
 		try
 		{
 			configure(charSequence);
 			return parse();
 		}
 		catch (Exception ex)
 		{
 			throw new SIMPLTranslationException("exception occurred in deserialzation ", ex);
 		}
 	}
 
 	private void configure(InputStream inputStream, Charset charSet) throws IOException, JsonParseException
 	{
 		// configure the json parser
 		JsonFactory f = new JsonFactory();
 		InputStreamReader tmpReader = new InputStreamReader(inputStream, charSet);
 		jp = f.createJsonParser(tmpReader);
 	}
 	
 	private void configure(InputStream inputStream) throws IOException, JsonParseException
 	{
 		// configure the json parser
 		JsonFactory f = new JsonFactory();
 		jp = f.createJsonParser(inputStream);
 	}
 
 	private void configure(CharSequence charSequence) throws IOException, JsonParseException
 	{
 		// configure the json parser
 		JsonFactory f = new JsonFactory();
 		jp = f.createJsonParser(charSequence.toString());
 	}
 
 	private Object parse() throws IOException, JsonParseException, SIMPLTranslationException
 	{
 		// all JSON documents start with an opening brace.
 		if (jp.nextToken() != JsonToken.START_OBJECT)
 		{
 			println("JSON Translation ERROR: not a valid JSON object. It should start with {");
 		}
 
 		// move the first field in the document. typically it is the root element.
 		jp.nextToken();
 
 		Object root = null;
 
 		// find the classdescriptor for the root element.
 		ClassDescriptor rootClassDescriptor = translationScope.getClassDescriptorByTag(jp
 				.getCurrentName());
 
 		root = rootClassDescriptor.getInstance();
 
 		// root.setupRoot();
 		// root.deserializationPreHook();
 		// if (deserializationHookStrategy != null)
 		// deserializationHookStrategy.deserializationPreHook(root, null);
 
 		// move to the first field of the root element.
 		jp.nextToken();
 		jp.nextToken();
 
 		// complete the object model from root and recursively of the fields it is composed of
 		createObjectModel(root, rootClassDescriptor);
 
 		return root;
 	}
 
 	/**
 	 * Recursive method that moves forward in the CharSequence through JsonParser to create a
 	 * corresponding object model
 	 * 
 	 * @param root
 	 *          instance of the root element created by the calling method
 	 * @param rootClassDescriptor
 	 *          instance of the classdescriptor of the root element created by the calling method
 	 * @throws JsonParseException
 	 * @throws IOException
 	 * @throws SIMPLTranslationException
 	 */
 	private void createObjectModel(Object root, ClassDescriptor rootClassDescriptor)
 			throws JsonParseException, IOException, SIMPLTranslationException
 	{
 		FieldDescriptor currentFieldDescriptor = null;
 		Object subRoot = null;
 
 		// iterate through each element of the current composite element.
 		while (jp.getCurrentToken() != JsonToken.END_OBJECT)
 		{
 			if (!handleSimplId(jp.getText(), root))
 			{
 				currentFieldDescriptor = (currentFieldDescriptor != null)
 						&& (currentFieldDescriptor.getType() == IGNORED_ELEMENT) ? FieldDescriptor.IGNORED_ELEMENT_FIELD_DESCRIPTOR
 						: (currentFieldDescriptor != null && currentFieldDescriptor.getType() == WRAPPER) ? currentFieldDescriptor
 								.getWrappedFD()
 								: rootClassDescriptor.getFieldDescriptorByTag(jp.getText(), translationScope, null);
 
 				int fieldType = currentFieldDescriptor.getType();
 
 				switch (fieldType)
 				{
 				case SCALAR:
 					jp.nextToken();
 					currentFieldDescriptor.setFieldToScalar(root, jp.getText(), translationContext);
 					break;
 				case COMPOSITE_ELEMENT:
 					jp.nextToken();
 
 					String tagName = jp.getCurrentName();
 					subRoot = getSubRoot(currentFieldDescriptor, tagName);
 
 					ClassDescriptor subRootClassDescriptor = currentFieldDescriptor
 							.getChildClassDescriptor(tagName);
 
 					// if (subRoot != null)
 					// subRoot.setupInParent(root, subRootClassDescriptor);
 
 					currentFieldDescriptor.setFieldToComposite(root, subRoot);
 					break;
 				case COLLECTION_ELEMENT:
 					jp.nextToken();
 					if (currentFieldDescriptor.isPolymorphic())
 					{
 						// ignore the wrapper tag
 						if (!currentFieldDescriptor.isWrapped())
 							jp.nextToken();
 
 						while (jp.getCurrentToken() != JsonToken.END_ARRAY)
 						{
 							jp.nextToken();
 							jp.nextToken();
 
 							subRoot = getSubRoot(currentFieldDescriptor, jp.getCurrentName());
 							Collection collection = (Collection) currentFieldDescriptor
 									.automaticLazyGetCollectionOrMap(root);
 							collection.add(subRoot);
 
 							jp.nextToken();
 							jp.nextToken();
 						}
 					}
 					else
 					{
 						while (jp.nextToken() != JsonToken.END_ARRAY)
 						{
 							subRoot = getSubRoot(currentFieldDescriptor, jp.getCurrentName());
 							Collection collection = (Collection) currentFieldDescriptor
 									.automaticLazyGetCollectionOrMap(root);
 							collection.add(subRoot);
 						}
 					}
 					break;
 				case MAP_ELEMENT:
 					jp.nextToken();
 					if (currentFieldDescriptor.isPolymorphic())
 					{
 						// ignore the wrapper tag
 						if (!currentFieldDescriptor.isWrapped())
 							jp.nextToken();
 
 						while (jp.getCurrentToken() != JsonToken.END_ARRAY)
 						{
 							jp.nextToken();
 							jp.nextToken();
 
 							subRoot = getSubRoot(currentFieldDescriptor, jp.getCurrentName());
 							if (subRoot instanceof IMappable)
 							{
 								final Object key = ((IMappable) subRoot).key();
 								Map map = (Map) currentFieldDescriptor.automaticLazyGetCollectionOrMap(root);
 								map.put(key, subRoot);
 							}
 
 							jp.nextToken();
 							jp.nextToken();
 						}
 
 					}
 					else
 					{
 						while (jp.nextToken() != JsonToken.END_ARRAY)
 						{
 							subRoot = getSubRoot(currentFieldDescriptor, jp.getCurrentName());
 							if (subRoot instanceof IMappable)
 							{
 								final Object key = ((IMappable) subRoot).key();
 								Map map = (Map) currentFieldDescriptor.automaticLazyGetCollectionOrMap(root);
 								map.put(key, subRoot);
 							}
 						}
 					}
 					break;
 				case COLLECTION_SCALAR:
 					jp.nextToken();
 					while (jp.nextToken() != JsonToken.END_ARRAY)
 					{
 						currentFieldDescriptor.addLeafNodeToCollection(root, jp.getText(), translationContext);
 					}
 					break;
 				case WRAPPER:
 					if (!currentFieldDescriptor.getWrappedFD().isPolymorphic())
 						jp.nextToken();
 					break;
 				}
 			}
 
 			jp.nextToken();
 		}
 
 		deserializationPostHook(root, translationContext);
 		if (deserializationHookStrategy != null)
			deserializationHookStrategy.deserializationPostHook(root, currentFieldDescriptor);
 	}
 
 	/**
 	 * Gets the sub root of the object model if its a composite object. Does graph handling Handles
 	 * simpl.ref tag to assign an already created instance of the composite object instead of creating
 	 * a new one
 	 * 
 	 * @param currentFieldDescriptor
 	 * @return
 	 * @throws SIMPLTranslationException
 	 * @throws JsonParseException
 	 * @throws IOException
 	 */
 	private Object getSubRoot(FieldDescriptor currentFieldDescriptor, String tagName)
 			throws SIMPLTranslationException, JsonParseException, IOException
 	{
 		jp.nextToken();
 
 		Object subRoot = null;
 
 		if (jp.getCurrentToken() == JsonToken.FIELD_NAME)
 		{
 			// check for simpl.ref if exists that we need an already created instance
 			if (jp.getText().equals(TranslationContext.JSON_SIMPL_REF))
 			{
 				jp.nextToken();
 				subRoot = translationContext.getFromMap(jp.getText());
 				jp.nextToken();
 			}
 			else
 			{
 				ClassDescriptor subRootClassDescriptor = currentFieldDescriptor
 						.getChildClassDescriptor(tagName);
 
 				subRoot = subRootClassDescriptor.getInstance();
 				
 				deserializationPreHook(subRoot, translationContext);
 				if (deserializationHookStrategy != null)
 					deserializationHookStrategy.deserializationPreHook(subRoot, currentFieldDescriptor);
 
 				createObjectModel(subRoot, subRootClassDescriptor);
 			}
 		}
 
 		return subRoot;
 	}
 
 	/**
 	 * Function used for handling graph's simpl.id tag. If the tag is present the current ElementState
 	 * object is marked as unmarshalled. Therefore, later simpl.ref can be used to extract this
 	 * instance
 	 * 
 	 * @param tagName
 	 * @param root
 	 * @return
 	 * @throws JsonParseException
 	 * @throws IOException
 	 */
 	private boolean handleSimplId(String tagName, Object root) throws JsonParseException, IOException
 	{
 		if (tagName.equals(TranslationContext.JSON_SIMPL_ID))
 		{
 			jp.nextToken();
 			translationContext.markAsUnmarshalled(jp.getText(), root);
 			return true;
 		}
 		return false;
 	}
 
 }
