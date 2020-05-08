 package ecologylab.bigsemantics.documentparsers;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.namespace.QName;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import ecologylab.appframework.types.prefs.Pref;
 import ecologylab.bigsemantics.actions.SemanticActionHandler;
 import ecologylab.bigsemantics.actions.SemanticActionsKeyWords;
 import ecologylab.bigsemantics.collecting.DocumentDownloadedEventHandler;
 import ecologylab.bigsemantics.collecting.DocumentDownloadingMonitor;
 import ecologylab.bigsemantics.collecting.DownloadStatus;
 import ecologylab.bigsemantics.collecting.LinkedMetadataMonitor;
 import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
 import ecologylab.bigsemantics.html.utils.StringBuilderUtils;
 import ecologylab.bigsemantics.metadata.Metadata;
 import ecologylab.bigsemantics.metadata.MetadataBase;
 import ecologylab.bigsemantics.metadata.MetadataClassDescriptor;
 import ecologylab.bigsemantics.metadata.MetadataFieldDescriptor;
 import ecologylab.bigsemantics.metadata.builtins.Document;
 import ecologylab.bigsemantics.metadata.scalar.MetadataParsedURL;
 import ecologylab.bigsemantics.metadata.scalar.types.MetadataParsedURLScalarType;
 import ecologylab.bigsemantics.metadata.scalar.types.MetadataScalarType;
 import ecologylab.bigsemantics.metametadata.DefVar;
 import ecologylab.bigsemantics.metametadata.FieldParser;
 import ecologylab.bigsemantics.metametadata.FieldParserElement;
 import ecologylab.bigsemantics.metametadata.FieldParserForRegexSplit;
 import ecologylab.bigsemantics.metametadata.FilterLocation;
 import ecologylab.bigsemantics.metametadata.MetaMetadata;
 import ecologylab.bigsemantics.metametadata.MetaMetadataCollectionField;
 import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
 import ecologylab.bigsemantics.metametadata.MetaMetadataField;
 import ecologylab.bigsemantics.metametadata.MetaMetadataNestedField;
 import ecologylab.bigsemantics.metametadata.MetaMetadataRepository;
 import ecologylab.bigsemantics.metametadata.MetaMetadataScalarField;
 import ecologylab.bigsemantics.metametadata.MetaMetadataValueField;
 import ecologylab.bigsemantics.metametadata.ScalarDependencyException;
 import ecologylab.bigsemantics.metametadata.ScalarDependencyManager;
 import ecologylab.bigsemantics.namesandnums.DocumentParserTagNames;
 import ecologylab.collections.Scope;
 import ecologylab.generic.HashMapArrayList;
 import ecologylab.generic.ReflectionTools;
 import ecologylab.generic.StringTools;
 import ecologylab.net.ParsedURL;
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.DeserializationHookStrategy;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.ScalarUnmarshallingContext;
 import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.XMLTools;
 import ecologylab.serialization.formatenums.Format;
 import ecologylab.serialization.formatenums.StringFormat;
 import ecologylab.serialization.types.ScalarType;
 
 /**
  * This is the base class for the all the document type which we create using meta-metadata.
  * 
  * @author amathur
  * 
  */
 @SuppressWarnings("rawtypes")
 public abstract class ParserBase<D extends Document> extends HTMLDOMParser<D> implements ScalarUnmarshallingContext,
 		SemanticActionsKeyWords, DeserializationHookStrategy<Metadata, MetadataFieldDescriptor>
 {
 
 	public static boolean			DONOT_LOOKUP_DOWNLOADED_DOCUMENT			= Pref.lookupBoolean("donot_lookup_downloaded_documents", false);
 
 	public static boolean			DONOT_SETUP_DOCUMENT_GRAPH_CALLBACKS	= Pref.lookupBoolean("donot_setup_document_graph_callbacks", false);
 
 	public static boolean			DONOT_LOOK_FOR_FAVICON                = Pref.lookupBoolean("donot_look_for_favicon", false);
 
 	protected XPath									xpath;
 
 	protected ParsedURL							truePURL;
 
 	protected SemanticActionHandler	handler;
 
 	public ParserBase(SemanticsGlobalScope infoCollector)
 	{
 		super(infoCollector);
 		xpath = XPathFactory.newInstance().newXPath();
 	}
 
 	/**
 	 * populate associated metadata with the container and handler.
 	 * @param dom 
 	 * @param metaMetadata 
 	 * @param document 
 	 * 
 	 * @param handler
 	 * @return
 	 */
 	public abstract Document populateMetadata(
 			Document document,
 			MetaMetadataCompositeField metaMetadata,
 			org.w3c.dom.Document dom,
 			SemanticActionHandler handler) throws IOException;
 
 	public final Document parse(Document document, MetaMetadataCompositeField metaMetadata, org.w3c.dom.Document dom) throws IOException
 	{
 		// init
 		handler = new SemanticActionHandler(semanticsScope, this);
 		truePURL = document.getLocation();
 		initializeParameterScope(metaMetadata);
 
 		// build the metadata object
 //		DomTools.prettyPrint(DOM);
 		Document resultingMetadata = populateMetadata(document, metaMetadata, dom, handler);
 		resultingMetadata.setMetadataChanged(true);
 
 		if (!DONOT_LOOK_FOR_FAVICON)
   		findFaviconPath(resultingMetadata, xpath);
 		
 		try
 		{
 			debug("Metadata parsed from: " + document.getLocation());
 			if (resultingMetadata != null)
 			{
 				debug(SimplTypesScope.serialize(resultingMetadata, StringFormat.XML));
 			}
 		}
 		catch (Exception e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 		if (resultingMetadata != null)
 		{
 			handler.takeSemanticActions(resultingMetadata);
 
 			// make sure termVector is built here
 			if(!resultingMetadata.ignoreInTermVector())
 			{
 				//debug("Building term vector for a metadata of type: "+resultingMetadata.getMetaMetadataName());
 				resultingMetadata.rebuildCompositeTermVector();
 			}
 			else
 			{
 				debug("Do not build term vector because ignore_in_term_vector is true");
 			}
 			// linking
 			MetaMetadataRepository metaMetaDataRepository = semanticsScope.getMetaMetadataRepository();
 			LinkedMetadataMonitor monitor = metaMetaDataRepository.getLinkedMetadataMonitor();
 			monitor.tryLink(metaMetaDataRepository, resultingMetadata);
 			monitor.addMonitors(resultingMetadata);
 		}
 
 		return  resultingMetadata;
 	}
 	
 	/**
 	 * (1) Populate Metadata. (2) Rebuild composite term vector. (3) Take semantic actions.
 	 * @throws IOException 
 	 */
 	@Override
 	public void parse() throws IOException
 	{
 		parse(getDocument(), getMetaMetadata(), getDom());
 	}
 
 	/**
 	 * Instantiate MetaMetadata variables that are used during XPath information extraction, and in
 	 * semantic actions.
 	 * 
 	 * @param metaMetadata
 	 */
 	private void initializeParameterScope(MetaMetadataCompositeField metaMetadata)
 	{
 		Node documentRoot = null;
 		try
 		{
 			documentRoot = getDom();
 			if (documentRoot != null)
 			{
 				Scope<Object> parameters = handler.getSemanticActionVariableMap();
 				parameters.put(DOCUMENT_ROOT_NODE, documentRoot);
 				updateDefVars(metaMetadata, documentRoot);
 			}
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void updateDefVars(MetaMetadataNestedField nestedField, Node contextNode)
 	{
 		assert(handler != null);
 		Scope<Object> parameters = handler.getSemanticActionVariableMap();
 		ArrayList<DefVar> defVars = nestedField.getDefVars();
 		if (defVars != null)
 		{
 			for (DefVar defVar : defVars)
 			{
 				String xpathExpression = ".";
 				String varName = "NO_VAR_NAME";
 				QName varType = null;
 				try
 				{
 					xpathExpression = defVar.getXpath();
 					varName = defVar.getName();
 					varType = defVar.getType();
 					String contextNodeName = defVar.getContextNode();
 					String varValue = defVar.getValue();
 					
 					// Does the var have a constant value?
 					if(varValue == null)
 					{
 						// No. Evaluate the Xpath to obtain the value. 
 						if (contextNodeName != null)
 						{
 							// get the context node from parameters
 							contextNode = (Node) parameters.get(contextNodeName);
 						}
 	
 						if (varType != null)
 						{
 							Object evalResult = xpath.evaluate(xpathExpression, contextNode, varType);
 							parameters.put(varName, evalResult);
 						}
 						else
 						{
 							// its gonna be a simple string evaluation
 							String evaluation = xpath.evaluate(xpathExpression, contextNode);
 							parameters.put(varName, evaluation);
 						}
 					}
 					else
 					{
 						// If we have a variable value, we'll just use it! 
 						parameters.put(varName, varValue);
 					}
 				}
 				catch (Exception e)
 				{
 					StringBuilder buffy = StringBuilderUtils.acquire();
 					buffy.append("################### ERROR IN VARIABLE DEFINTION ##############################\n");
 					buffy.append("Variable Name::\t").append(varName).append("\n");
 					buffy.append("Check if the context node is not null::\t").append(contextNode).append("\n");
 					buffy.append("Check if the XPath Expression is valid::\t").append(xpathExpression).append("\n");
 					buffy.append("Check if the return object type of Xpath evaluation is corect::\t").append(varType).append("\n");
 					debug(buffy);
 					StringBuilderUtils.release(buffy);
 				}
 			}
 		}
 	}
 
 	/**
 	 * This helper class is used for returning information from extractNestedHelper().
 	 * 
 	 * @author quyin
 	 * 
 	 */
 	private static class NestedFieldHelper
 	{
 		public Node												node;
 
 		public Map<String, String>				fieldParserContext;
 
 		public NodeList										nodeList;
 
 		public List<Map<String, String>>	fieldParserContextList;
 
 		private int												listSize	= -1;
 
 		public int getListSize()
 		{
 			if (listSize < 0)
 			{
 				if (fieldParserContextList != null)
 					listSize = fieldParserContextList.size();
 				else if (nodeList != null)
 					listSize = nodeList.getLength();
 				else
 					listSize = 0;
 			}
 			return listSize;
 		}
 	}
 	
 	/**
 	 * Recursively extract information from the sub DOM tree rooted at current context node to a given
 	 * field on the given metadata, using given meta-metadata field information.
 	 * 
 	 * @param mmdField
 	 *          The guiding meta-metadata field, indicating which field of <code>metadata</code>
 	 *          should be extracted, and containing extraction rules.
 	 * @param metadata
 	 *          The metadata object holding the field to be extracted.
 	 * @param contextNode
 	 *          The context node for extraction.
 	 * @param fieldParserContext
 	 *          The context of field parsers, if any.
 	 * @param params
 	 *          The scope containing variables during parsing and semantic actions
 	 * @return true if some information is extracted, and every required field has value. false if
 	 *         nothing is extracted or a required field doesn't have value.
 	 */
 	protected boolean recursiveExtraction(MetaMetadataNestedField mmdField, Metadata metadata,
 			Node contextNode, Map<String, String> fieldParserContext, Scope<Object> params)
 	{
 		HashMapArrayList<String, MetaMetadataField> fieldSet = mmdField.getChildMetaMetadata();
 		if (fieldSet == null || fieldSet.isEmpty())
 			return false;
 		
 		Stack<MetaMetadataField> surroundingMmdStack = (Stack<MetaMetadataField>) params.get(SURROUNDING_META_METADATA_STACK);
 		if (surroundingMmdStack == null)
 		{
 			surroundingMmdStack = new Stack<MetaMetadataField>();
 			params.put(SURROUNDING_META_METADATA_STACK, surroundingMmdStack);
 		}
 		surroundingMmdStack.push(mmdField);
 		
 		updateDefVars(mmdField, contextNode);
 //		DomTools.prettyPrint(contextNode);
 
 		MetaMetadataNestedField targetParent = mmdField.isUsedForInlineMmdDef() ? mmdField.getInheritedMmd() : mmdField;
 		
 		boolean result = true;
 		
 		synchronized (fieldSet)
 		{
 			if(fieldsetContainsFieldsWithDependencies(fieldSet))
 			{
 				try
 				{
 					ScalarDependencyManager d = new ScalarDependencyManager(fieldSet);
 					fieldSet = d.sortFieldSetByDependencies(metadata);
 				}
 				catch(ScalarDependencyException e)
 				{
 					error(e.getMessage() + " \n Proceeding with extraction anyways; will result in null values.");
 					e.printStackTrace();
 				}
 			}
 			
 			for (MetaMetadataField field : fieldSet)
 			{
 				if (!field.isAuthoredChildOf(targetParent))
 				{
 					// if 'field' is purely inherited, we ignore it to prevent infinite loops.
 					// infinite loops can happen when 'field' uses the same mmd type as where it is defined,
 					// e.g. google_patent.references are google_patent too.
 					// this behavior is not necessarily required to prevent infinite loops, but it works
 					// for our use cases now.
 					// -- yin qu, 2/21/2012
 					continue;
 				}			
 			
 				try
 				{
 					boolean suc = false;
 					if (field instanceof MetaMetadataCompositeField)
 					{
 						MetaMetadataCompositeField mmcf = (MetaMetadataCompositeField) field;
 						suc = extractComposite(mmcf, metadata, contextNode, fieldParserContext, params);
 					}
 					else if (field instanceof MetaMetadataCollectionField)
 					{
 						MetaMetadataCollectionField mmcf = (MetaMetadataCollectionField) field;
 						if (mmcf != null)
 							suc = extractCollection(mmcf, metadata, contextNode, fieldParserContext, params);
 					}
 					else
 					{
 						// scalar
 						MetaMetadataScalarField mmsf = (MetaMetadataScalarField) field;
 						suc = extractScalar(mmsf, metadata, contextNode, fieldParserContext, params);
 					}
 					if (field.isRequired() && !suc)
 					{
 						result = false;
 						break;
 					}
 				}
 				catch (Exception e)
 				{
 					error(String.format("EXCEPTION when extracting %s: %s", field, e.getMessage()));
 					e.printStackTrace();
 				}
 			}
 		}
 
 		surroundingMmdStack.pop();
 		return result;
 	}
 	
 	/**
 	 * Determines if a given collection of metametadata fields contain dependencies (for concatenation or other value semantics)
 	 * on other mmd fields.
 	 * @param fieldSet The set of metametadata fields to check.
 	 * @return True if there are dependencies that need to be handled correctly
 	 */
 	private Boolean fieldsetContainsFieldsWithDependencies(HashMapArrayList<String, MetaMetadataField> fieldSet)
 	{
 		// TODO: refactor to predicate w/ Google Guava
 		Boolean hasDependency = false;
 		for (MetaMetadataField field : fieldSet)
 		{
 			if(field instanceof MetaMetadataScalarField)
 			{
 				MetaMetadataScalarField m = (MetaMetadataScalarField)field;
 				if(m.hasValueDependencies())
 					return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * This helper method builds the context for extracting a nested field, e.g. context node and
 	 * field parser context.
 	 * 
 	 * @param mmdField
 	 * @param contextNode
 	 * @param fieldParserContext
 	 * @param params
 	 * @return A helper object holding necessary information, or null if no information is obtained.
 	 */
 	private NestedFieldHelper extractNestedHelper(MetaMetadataNestedField mmdField, Node contextNode,
 			Map<String, String> fieldParserContext, Scope<Object> params)
 	{
 		// get xpath, context node, field parser defintion & key: basic information for following
 		
 		String xpathString = mmdField.getXpath();
 		
 //		if (xpathString != null && xpathString.length() > 0)		
 //			xpathString = provider.xPathTagNamesToLower(xpathString);
 		
 		contextNode = findContextNodeIfNecessary(mmdField, contextNode, params);
 		
 		FieldParserElement fieldParserElement = mmdField.getFieldParserElement();
 		String fieldParserKey = mmdField.getFieldParserKey();
 
 		// init result
 		NestedFieldHelper result = new NestedFieldHelper();
 
 		if (mmdField instanceof MetaMetadata) // this should not happen, currently
 		{
 			result.node = contextNode;
 			return result;
 		}
 
 		try
 		{
 			if (fieldParserElement == null)
 			{
 			  xpathString = evaluateXpathForField(mmdField, contextNode, params, xpathString, result);
 			}
 			else
 			{
 				FieldParser fieldParser = this.getSemanticsScope().getFieldParserFactory().get(fieldParserElement.getName());
 
 				if (mmdField instanceof MetaMetadataCompositeField)
 				{
 					String valueString = null;
 					if (fieldParserKey != null && fieldParserKey.length() > 0)
 						valueString = getFieldParserValueByKey(fieldParserContext, fieldParserKey);
 					else
 				  {
         		xpathString = evaluateXpathForField(mmdField, contextNode, params, xpathString, result);
 					  if (result.node != null)
   					{
   						if (mmdField.isExtractAsHtml())
   							valueString = getInnerHtml(result.node);
   						else
   							valueString = result.node.getTextContent();
   					}
 				  }
 						
 					if (valueString != null && valueString.length() > 0)
 						result.fieldParserContext = fieldParser.getKeyValuePairResult(fieldParserElement, valueString.trim());
 				}
 				else if (mmdField instanceof MetaMetadataCollectionField)
 				{
 					if (!((MetaMetadataCollectionField) mmdField).isCollectionOfScalars() && fieldParserElement.isForEachElement())
 					{
         		xpathString = evaluateXpathForField(mmdField, contextNode, params, xpathString, result);
 						result.fieldParserContextList = new ArrayList<Map<String,String>>();
 						for (int i = 0; i < result.nodeList.getLength(); ++i)
 						{
 							Node node = result.nodeList.item(i);
 							String valueString = null;
 							if (mmdField.isExtractAsHtml())
 							{
 								valueString = getInnerHtml(node);
 							}
 							else
 							{
 								valueString = node.getTextContent();
 							}
 							if (valueString != null && valueString.length() > 0)
 							{
 								Map<String, String> aContext = fieldParser.getKeyValuePairResult(fieldParserElement, valueString.trim());
 								result.fieldParserContextList.add(aContext);
 							}
 						}
 					}
 					else
 					{
 						String valueString = null;
 						if (fieldParserKey != null && fieldParserKey.length() > 0)
 							valueString = getFieldParserValueByKey(fieldParserContext, fieldParserKey);
 						else
 						{  
           		xpathString = evaluateXpathForField(mmdField, contextNode, params, xpathString, result);
 						  if (result.nodeList != null && result.nodeList.getLength() >= 1)
   						{
   							if (mmdField.isExtractAsHtml())
   								valueString = getInnerHtml(result.nodeList.item(0));
   							else
   								valueString = result.nodeList.item(0).getTextContent();
   						}
 						}
 						
 						if (valueString != null && valueString.length() > 0)
 							result.fieldParserContextList = fieldParser.getCollectionResult(fieldParserElement, valueString.trim());
 					}
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			String msg = getErrorMessage(mmdField, contextNode, xpathString, e);
 			debug(msg);
 			e.printStackTrace();
 		}
 
 		if (result.node == null && result.nodeList == null && result.fieldParserContext == null && result.fieldParserContextList == null)
 			return null;
 
 		return result;
 	}
 
   String evaluateXpathForField(MetaMetadataNestedField mmdField,
                                Node contextNode,
                                Scope<Object> params,
                                String xpathString,
                                NestedFieldHelper result) throws XPathExpressionException
   {
     if (contextNode != null)
     {
     	MetaMetadataField surroundingField = getCurrentSurroundingField(params);
     	if ((xpathString == null || xpathString.length() == 0)
     			&& mmdField.parent() == surroundingField)
     	{
     		// the condition above after '&&' holds when this field is actually authored there,
     		// but not purely inherited.
     		xpathString = ".";
     	}
     	
     	if (xpathString != null)
     	{
     		// if at this point of time xpathString is null, this field must be purely inherited,
     		// thus we may want to ignore it.
     		// this behavior, as documented in recursiveExtraction(), is not necessarily required.
     		// it basically prevents xpaths to be inherited by a subtype meta-metadata.
     		// further extension may allow this inheritance, e.g. by explicitly saying 'I want to
     		// inherit xpaths from the super wrapper', using some attribute on <meta-metadata>.
     		// -- yin qu, 2/23/2012
     	  
     	  // change absolute path to relative path, so that when we are doing evaluate() we don't
     	  // go through the whole document again
     	  if (xpathString.startsWith("//"))
     	    xpathString = "." + xpathString;
     	  
     		if (mmdField instanceof MetaMetadataCompositeField)
     			result.node = (Node) xpath.evaluate(xpathString, contextNode, XPathConstants.NODE);
     		else if (mmdField instanceof MetaMetadataCollectionField)
     			result.nodeList = (NodeList) xpath.evaluate(xpathString, contextNode, XPathConstants.NODESET);
     	}
     }
     return xpathString;
   }
 
 	private MetaMetadataField getCurrentSurroundingField(Scope<Object> params)
 	{
 		Stack<MetaMetadataField> stack = (Stack<MetaMetadataField>) params.get(SURROUNDING_META_METADATA_STACK);
 		if (stack != null && stack.size() > 0)
 		{
 			return stack.peek();
 		}
 		return null;
 	}
 
 	private Node findContextNodeIfNecessary(MetaMetadataField mmdField, Node currentContextNode,
 			Scope<Object> params)
 	{
 		String contextNodeName = mmdField.getContextNode();
 		if (contextNodeName != null)
 		{
 			currentContextNode = (Node) params.get(contextNodeName);
 		}
 		if (currentContextNode == null)
 		{
 			currentContextNode = (Node) params.get(DOCUMENT_ROOT_NODE);
 		}
 		return currentContextNode;
 	}
 	
 	private String getFieldParserValueByKey(Map<String, String> fieldParserContext, String fieldParserKey)
 	{
 		int pos = fieldParserKey.indexOf('|');
 		if (pos < 0)
 			return fieldParserContext.get(fieldParserKey);
 		String[] keys = fieldParserKey.split("\\|");
 		for (String key : keys)
 			if (fieldParserContext.containsKey(key))
 				return fieldParserContext.get(key);
 		return null;
 	}
 
 	/**
 	 * Extract a composite field of the given metadata object.
 	 * 
 	 * @param mmdField
 	 * @param metadata
 	 * @param contextNode
 	 * @param fieldParserContext
 	 * @param params
 	 * @return
 	 */
 	private boolean extractComposite(MetaMetadataCompositeField mmdField, Metadata metadata,
 			Node contextNode, Map<String, String> fieldParserContext, Scope<Object> params)
 	{
 		NestedFieldHelper helper = extractNestedHelper(mmdField, contextNode, fieldParserContext,
 				params);
 		if (helper == null)
 			return false;
 
 		// will be used for child fields
 		Node thisNode = helper.node;
 		Map<String, String> thisFieldParserContext = helper.fieldParserContext;
 
 		// create a metadata instance for this field
 		Class<? extends Metadata> metadataClass = mmdField.getMetadataClass();
 		Class[] argClasses = new Class[] { MetaMetadataCompositeField.class };
 		Object[] argObjects = new Object[] { mmdField };
 		Metadata thisMetadata = ReflectionTools.getInstance(metadataClass, argClasses, argObjects);
 		thisMetadata.setSemanticsSessionScope(semanticsScope);
 		
 		if (recursiveExtraction(mmdField, thisMetadata, thisNode, thisFieldParserContext, params))
 		{
 			if (!DONOT_LOOKUP_DOWNLOADED_DOCUMENT)
 			{
 				Document downloadedMetadata = lookupDownloadedDocument(thisMetadata);
 				if (downloadedMetadata != null)
 				{
 					thisMetadata = downloadedMetadata;
 				}
 			}
 			
 			if (!DONOT_SETUP_DOCUMENT_GRAPH_CALLBACKS)
 			{
 				setupDocumentChangedEventListener(mmdField, metadata, thisMetadata);
 			}
 			
 			thisMetadata.setMetaMetadata(mmdField);
 			Metadata changedMetadata = lookupTrueMetaMetadata(mmdField.getRepository(), thisMetadata);
 			if (changedMetadata != null)
 			{
 			  Metadata.fieldWiseCopy(changedMetadata, thisMetadata);
 			  thisMetadata = changedMetadata;
 			}
 			
 			// TODO check for polymorphism. if this is an inherent polymorphic fields, we may need to
 			// replace thisMetadata completely if its type changes.
 			
 			// here everything seems ok. assign result composite back to input metadata object
 			Field javaField = mmdField.getMetadataFieldDescriptor().getField();
 			ReflectionTools.setFieldValue(metadata, javaField, thisMetadata);
 
 			// try to link result metadata
 			MetaMetadataRepository repository = semanticsScope.getMetaMetadataRepository();
 			LinkedMetadataMonitor monitor = repository.getLinkedMetadataMonitor();
 			monitor.tryLink(repository, thisMetadata);
 
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param mmdField
 	 * @param hostMetadata
 	 * @param docToDownload
 	 * @param isCollection
 	 */
 	private void setupDocumentChangedEventListener(MetaMetadataNestedField mmdField,
 			Metadata hostMetadata, Metadata docToDownload)
 	{
 		if (docToDownload instanceof Document)
 		{
 			Document doc = (Document) docToDownload;
 			if (doc.getDownloadStatus() != DownloadStatus.DOWNLOAD_DONE)
 			{
 				ParsedURL listeningLoc = doc.getLocation();
 				DocumentDownloadingMonitor monitor = semanticsScope.getDocumentDownloadingMonitor();
 				DocumentDownloadedEventHandler downloadedEventListener = new DocumentDownloadedEventHandler();
 				monitor.listenForDocumentDownloading(hostMetadata, listeningLoc, mmdField
 						.getMetadataFieldDescriptor().getField(), downloadedEventListener);
 			}
 		}
 	}
 
 	/**
 	 * looking at the global document collection, and reuse exising document object if it is already
 	 * downloaded.
 	 * 
 	 * @param metadata
 	 * @return
 	 */
 	protected Document lookupDownloadedDocument(Metadata metadata)
 	{
 		if (metadata instanceof Document)
 		{
 			Document doc = (Document) metadata;
 			ParsedURL location = doc.getLocationOrFirstAdditionLocation();
 			if (location != null)
 			{
 				Document existingDoc = semanticsScope.lookupDocument(location);
 				if (existingDoc != null && existingDoc.getDownloadStatus() == DownloadStatus.DOWNLOAD_DONE)
 				{
 					existingDoc.addMixin(doc); // add the replaced Document object as a mixin in the downloaded Document.
 					return existingDoc;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * if we got a document, we may want to look up its true meta-metadata type by location.
 	 * before doing connect(), what we can do to find out the true meta-metadata type is quite limited
 	 * (location, suffix, tag name). here we do location & suffix. tag name is mainly used by direct
 	 * binding cases.
 	 * 
 	 * @param repository
 	 * @param thisMetadata
 	 */
 	protected Metadata lookupTrueMetaMetadata(MetaMetadataRepository repository, Metadata thisMetadata)
 	{
 		if (thisMetadata instanceof Document)
 		{
 			ParsedURL thisMetadataLocation = thisMetadata.getLocation();
 			if (thisMetadataLocation != null)
 			{
 				MetaMetadata locMmd = repository.getCompoundDocumentMM(thisMetadataLocation);
 				if (locMmd != null && !locMmd.getName().equals(DocumentParserTagNames.COMPOUND_DOCUMENT_TAG))
 				{
 				  Class thisMetadataClass = thisMetadata.getClass();
 				  Class trueMetadataClass = locMmd.getMetadataClass();
 					if (thisMetadataClass.isAssignableFrom(trueMetadataClass))
 					{
//						debug("changing meta-metadata for extracted value " + thisMetadata + " to " + locMmd);
 					  if (thisMetadataClass == trueMetadataClass)
 					  {
 					    // when the two metadata classes are the same, we can safely change the meta-metadata
 					    // since they have exactly the same set of fields, and thus no binding errors will
 					    // occur.
 					    thisMetadata.setMetaMetadata(locMmd);
 					  }
 					  else
 					  {
 					    // when the two metadata classes are not the same, we need to be careful. create
 					    // the right metadata object and copy values.
   						Metadata changedMetadata = locMmd.constructMetadata();
   						changedMetadata.setMetaMetadata(locMmd);
   						return changedMetadata;
 					  }
 					}
 					else
 					{
 						error("cannot change meta-metadata fro extracted value " + thisMetadata + " to " + locMmd + " because the type doesn't match!\n"
 								  + "expected type: " + thisMetadata.getMetaMetadata() + "\n"
 								  + "check the <selector> to see if it is not specific enough!");
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Extract a collection field of the given metadata object.
 	 * 
 	 * @param mmdField
 	 * @param metadata
 	 * @param contextNode
 	 * @param fieldParserContext
 	 * @param params
 	 * @return true if the result collection is not empty, or false. The result collection field will
 	 *         not contain null references or failed elements (elements that has no actual information
 	 *         or lacks required field values).
 	 */
 	private boolean extractCollection(MetaMetadataCollectionField mmdField, Metadata metadata,
 			Node contextNode, Map<String, String> fieldParserContext, Scope<Object> params)
 	{
 		NestedFieldHelper helper = extractNestedHelper(mmdField, contextNode, fieldParserContext,
 				params);
 		if (helper == null)
 			return false;
 
 		// will be used for child fields
 		NodeList nodeList = helper.nodeList;
 		List<Map<String, String>> fieldParserContextList = helper.fieldParserContextList;
 		int size = helper.getListSize();
 
 		// get class of elements in the collection
 		SimplTypesScope tscope = semanticsScope.getMetadataTypesScope();
 		Class elementClass = null;
 		MetadataScalarType scalarType = null;
 		MetadataFieldDescriptor metadataFieldDescriptor = mmdField.getMetadataFieldDescriptor();
 		if (mmdField.isCollectionOfScalars())
 		{
 			// registered at MetadataScalarScalarType.init()
 			ScalarType theScalarType = mmdField.getChildScalarType();
 			if (theScalarType != null && theScalarType instanceof MetadataScalarType)
 			{
 				scalarType = (MetadataScalarType) theScalarType;
 				elementClass = scalarType.getJavaClass();
 			}
 			else
 			{
 				// error!
 				throw new RuntimeException("child_scalar_type not specified or registered: " + mmdField);
 			}
 		}
 		else
 		{
 //			elementClass = tscope.getClassByTag(mmdField.getChildType());
 		  if (metadataFieldDescriptor != null)
 		  {
   			ClassDescriptor elementClassDescriptor = metadataFieldDescriptor.getElementClassDescriptor();
   			if (metadataFieldDescriptor.isPolymorphic())
   			{
   				String polymorphTagName = mmdField.getChildComposite().getInheritedMmd().getTagForTypesScope();
   				if (polymorphTagName != null)
   					elementClassDescriptor = metadataFieldDescriptor.elementClassDescriptor(polymorphTagName);
   			}
   			if (elementClassDescriptor != null)
   				elementClass = elementClassDescriptor.getDescribedClass();
 		  }
 		  else
 		  {
 		    warning("metadataFieldDescriptor not found in " + mmdField);
 		  }
 		}
 		
 		if (elementClass == null)
 		{
 			// we cannot determine the class of this collection. this may be due to lack of type
 			// specification there, but it may also be correct, e.g. for polymorphic fields
 			return false;
 		}
 
 		// build the result list and populate
 		ArrayList elements = new ArrayList();
 		Class[] argClasses = new Class[] { MetaMetadataCompositeField.class };
 		Object[] argObjects = new Object[] { mmdField.getChildComposite() };
 		String[] fieldParserContextValues = null;
 		for (int i = 0; i < size; ++i)
 		{
 			Node thisNode = (nodeList == null) ? null : nodeList.item(i);
 			Map<String, String> thisFieldParserContext = (fieldParserContextList == null) ? null
 					: fieldParserContextList.get(i);
 			
 			if (!mmdField.isCollectionOfScalars())
 			{
 				Metadata element = (Metadata) ReflectionTools.getInstance(elementClass, argClasses, argObjects);
 				element.setSemanticsSessionScope(semanticsScope);
 				
 //				if (recursiveExtraction(mmdField.getChildComposite(), element, thisNode, thisFieldParserContext, params))
 				if (recursiveExtraction(mmdField, element, thisNode, thisFieldParserContext, params))
 				{
 					if (!DONOT_LOOKUP_DOWNLOADED_DOCUMENT)
 					{
 						Document downloadedDocument = lookupDownloadedDocument(element);
 						if (downloadedDocument != null)
 						{
 							element = downloadedDocument;
 						}
 					}
 					
 					if (!DONOT_SETUP_DOCUMENT_GRAPH_CALLBACKS)
 					{
 						setupDocumentChangedEventListener(mmdField, metadata, element);
 					}
 			
 					element.setMetaMetadata(mmdField);
     			Metadata changedElement = lookupTrueMetaMetadata(mmdField.getRepository(), element); 
     			if (changedElement != null)
     			{
     			  Metadata.fieldWiseCopy(changedElement, element);
     			  element = changedElement;
     			}
 			
 					// TODO check for polymorphism. if this is an inherent polymorphic fields, we may need to
 					// replace element completely if its type changes.
 					
 					elements.add(element);
 				}
 			}
 			else
 			{
 				String value = null;
 				if (fieldParserContextList != null)
 					value = thisFieldParserContext == null ? null : thisFieldParserContext.get(FieldParserForRegexSplit.DEFAULT_KEY);
 				else if (thisNode != null)
 				{
 					if (mmdField.isExtractAsHtml())
 					{
 						value = getInnerHtml(thisNode);
 					}
 					else
 					{
 						value = thisNode.getTextContent();
 					}
 				}
 				
 				if (value != null)
 				{
 					value = applyPrefixAndRegExOnEvaluation(value, mmdField);
 					
 					MetadataBase element;
 					element = (MetadataBase) scalarType.getInstance(value, null, this);
 					if (element != null)
 						elements.add(element);
 				}
 			}
 		}
 
 		// if more than 0 elements are extracted, assign the collection back
 		if (elements.size() > 0)
 		{
 			Field javaField = metadataFieldDescriptor.getField();
 			ReflectionTools.setFieldValue(metadata, javaField, elements);
 			return true;
 		}
 
 		return false;
 	}
 	
 	private static Properties	innerHtmlProps	= new Properties();
 	static
 	{
 		innerHtmlProps.put(OutputKeys.METHOD, "html");
 		innerHtmlProps.put(OutputKeys.INDENT, "yes");
 	}
 	
 	/**
 	 * using javax.xml.transform.Transformer to get the inner HTML of a node.
 	 * 
 	 * @param node
 	 * @return
 	 */
 	private String getInnerHtml(Node node)
 	{
 		node.normalize();
 		StringWriter w = new StringWriter();
 		try
 		{
 			Transformer t = XmlTransformerPool.get().acquire();
 			t.setOutputProperties(innerHtmlProps);
 			t.transform(new DOMSource(node), new StreamResult(w));
 			XmlTransformerPool.get().release(t);
 		}
 		catch (TransformerConfigurationException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (TransformerException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return w.toString();
 	}
 
 	/**
 	 * Extract a scalar field of a given metadata object.
 	 * 
 	 * @param mmdField
 	 * @param metadata
 	 * @param contextNode
 	 * @param fieldParserContext
 	 * @param params
 	 * @return true if the scalar value is not null / empty, or false. If &lt;filter&gt; defined it
 	 *         will be applied before checking null / empty value.
 	 */
 	private boolean extractScalar(MetaMetadataScalarField mmdField, Metadata metadata,
 			Node contextNode, Map<String, String> fieldParserContext, Scope<Object> params)
 	{
 		String xpathString = mmdField.getXpath();
 
 		String fieldParserKey = mmdField.getFieldParserKey();
 		
 		contextNode = findContextNodeIfNecessary(mmdField, contextNode, params);
 
 		String evaluation = null;
 		if (xpathString != null && xpathString.length() > 0 && contextNode != null && fieldParserKey == null)
 		{
 			try
 			{
 			  // absolute path to relative path, so that we don't go through the document again
 			  if (xpathString.startsWith("//"))
 			    xpathString = "." + xpathString;
 			  
 				if (mmdField.isExtractAsHtml())
 				{
 					Node targetNode = (Node) xpath.evaluate(xpathString, contextNode, XPathConstants.NODE);
 					if (targetNode != null)
 						evaluation = getInnerHtml(targetNode);
 				}
 				else
 				{
 					evaluation = xpath.evaluate(xpathString, contextNode);
 				}
 			}
 			catch (Exception e)
 			{
 				String msg = getErrorMessage(mmdField, contextNode, xpathString, e);
 				debug(msg);
 				e.printStackTrace();
 			}
 		}
 		else if (fieldParserKey != null)
 		{
 			evaluation = fieldParserContext == null ? null : fieldParserContext.get(fieldParserKey);
 		}
 		else if (!mmdField.hasConcatenateValues())			
 			return false; // This is the final catch all. 
 		
 		if(evaluation == null)
 		{
 			evaluation = ""; // Set an empty string to concatenate to.
 		}
 		
 		evaluation = concatenateValues(evaluation, mmdField, metadata, params);
 				
 
 		// after we have evaluated the expression we might need to modify it.
 		evaluation = applyPrefixAndRegExOnEvaluation(evaluation, mmdField);
 		if (StringTools.isNullOrEmpty(evaluation))
 			return false;
 		
 	  MetadataFieldDescriptor fd = mmdField.getMetadataFieldDescriptor();
 	  ScalarType fdScalarType = fd == null ? null : fd.getScalarType();
 		if (fdScalarType != null && fdScalarType instanceof MetadataParsedURLScalarType)
 		{
 		  // if this is a ParsedURL, we try to filter it using <filter_location>, if applicable.
 	    MetadataParsedURL metadataPurl = (MetadataParsedURL) fdScalarType.getInstance(evaluation, null, this);
 	    if (metadataPurl != null)
 	    {
 	      ParsedURL filteredPurl = 
 	          FilterLocation.filterIfNeeded(metadataPurl.getValue(), null, semanticsScope);
 	      if (!metadataPurl.getValue().equals(filteredPurl))
   	      metadataPurl.setValue(filteredPurl);
 	    }
 	    fd.setField(metadata, metadataPurl);
 		}
 		else
 		{
       // for other scalar types, we only need to create and assign the value.
   		metadata.setByFieldName(mmdField.getFieldNameInJava(false), evaluation, this);
 		}
 		
 		return true;
 	}
 
 
 
 	/**
 	 * Handles concatenation semantics for a field value. 
 	 * @param evaluation String originally in the scalar value (will be appended at the beginning of the string
 	 * @param mmdField The scalar field with values to concatenate
 	 * @param metadata Metadata object that contains the field
 	 * @param params Scope of parsing with variables / etc
 	 * @return String value concatenated to pass onto other tasks (like regexing)
 	 */
 	private String concatenateValues(String evaluation, MetaMetadataScalarField mmdField, 
 			Metadata metadata, Scope<Object> params) 
 	{
 		if(mmdField.hasConcatenateValues())
 		{
 			List<MetaMetadataValueField> fields = mmdField.getConcatenateValues();
 			
 			StringBuffer buffy = new StringBuffer();
 			
 			buffy.append(evaluation); //If we have a value already for the mmd field, append it at the beginning
 			
 			for(MetaMetadataValueField v : fields)
 			{
 				String varValue = v.getReferencedValue(mmdField, metadata, params);			
 
 				if(varValue == null)
 				{
 					varValue = "";
 					warning("Attempted to concatenate null value from value referenced as: " +v.getReferenceName());
 				}
 				
 				buffy.append(varValue);
 			}
 
 			return buffy.toString();
 		}
 		else
 		{
 			return evaluation;
 		}
 	}
 
 	/**
 	 * Generate an error message containing parsing context information.
 	 * 
 	 * @param mmdField
 	 * @param contextNode
 	 * @param xpathString
 	 * @param e
 	 * @return
 	 */
 	private String getErrorMessage(MetaMetadataField mmdField, Node contextNode, String xpathString,
 			Exception e)
 	{
 		StringBuilder buffy = StringBuilderUtils.acquire();
 		buffy.append("################# ERROR IN EVALUATION OF A FIELD########################\n");
 		buffy.append("Field Name::\t").append(mmdField.getName()).append("\n");
 		buffy.append("ContextNode::\t").append(contextNode.getTextContent()).append("\n");
 		buffy.append("XPath Expression::\t").append(xpathString).append("\n");
 		buffy.append("Exception Message::\t").append(e.getMessage()).append("\n");
 		String msg = buffy.toString();
 		StringBuilderUtils.release(buffy);
 		e.printStackTrace();
 		return msg;
 	}
 
 	/**
 	 * This function does all the modifications on the evalaution based on string prefix as well as on
 	 * regular expressions. TODO we might not even need the string prefix if we can write good regular
 	 * expressions.
 	 * 
 	 * @param evaluation
 	 * @param mmdElement
 	 * @return
 	 */
 	// FIXME -- make this operate directly on a StringBuilder (which will also change the return type
 	// to void
 	private String applyPrefixAndRegExOnEvaluation(String evaluation, MetaMetadataField field)
 	{
 		if (evaluation == null)
 			return null;
 
 		// regex replacing should happen only to scalar fields
 
 		// to remove unwanted XML characters
 		evaluation = XMLTools.unescapeXML(evaluation);
 
 		// get the regular expression
 		Pattern regularExpression = field.getRegexPattern();
 
 		if (regularExpression != null)
 		{
 			int group = field.getRegexGroup();
 			if (field.isNormalizeText())
 			{
 				evaluation = evaluation.replaceAll("\\s+", " ").trim();
 				evaluation = evaluation.replaceAll("\u00A0", " ").trim(); // &nbsp;
 			}
 			Matcher matcher = regularExpression.matcher(evaluation);
 			String replacementString = field.getRegexReplacement();
 			if (replacementString == null)
 			{
 				// without replacementString, we search for the input pattern as the evaluation result
 				if (matcher.find())
 				{
 					if (group <= matcher.groupCount())
 						evaluation = matcher.group(field.getRegexGroup());
 //					debug(String.format("regex pattern hit: regex=%s", regularExpression));
 					else
 					{
 						warning("RegEx capturing group not found: regex=" + regularExpression + ", content=" + evaluation);
 						evaluation = "";
 					}
 				}
 				else
 				{
 					evaluation = ""; // because nothing matched, we return an empty string
 //					debug(String.format("regex pattern not hit: regex=%s", regularExpression));
 				}
 			}
 			else
 			{
 				// with replacementString, we replace occurrences of input pattern with the replacementString
 				evaluation = matcher.replaceAll(replacementString);
 //				debug(String.format("regex replacement: regex=%s, replace=%s", regularExpression, replacementString));
 			}
 		}
 
 		// remove white spaces if any
 		evaluation = evaluation.trim();
 		return evaluation;
 	}
 
 	@Override
 	public ParsedURL purlContext()
 	{
 		return purl();
 	}
 
 	@Override
 	public File fileContext()
 	{
 		return null;
 	}
 
 	@Override
 	public ParsedURL getTruePURL()
 	{
 		return (truePURL != null) ? truePURL : super.getTruePURL();
 	}
 
 	/**
 	 * @return Document subclass metadata resulting from s.im.pl deserialization of the input stream.
 	 * @throws IOException 
 	 */
 	protected Document directBindingPopulateMetadata() throws IOException
 	{
 		Document newDocument = null;
 		try
 		{
 			// this must be a top-level metadata object (i.e. not a field)
 			// thus it must have a MetaMetadata attached (i.e. not a MetaMetadataCompositeField)
 			// thus this conversion is safe
 			MetaMetadata metaMetadata = (MetaMetadata) this.getMetaMetadata();
 			
 			SimplTypesScope tscope = metaMetadata.getLocalMetadataTypesScope();
 			newDocument = (Document) tscope.deserialize(purlConnection.getPurl(), this, Format.XML);
 			
 			SimplTypesScope.serialize(newDocument, System.out,  StringFormat.XML);
 			
 			System.out.println();
 			// the old document is basic, so give it basic meta-metadata (so recycle does not tank)
 			Document oldDocument	= documentClosure.getDocument();
 			oldDocument.setMetaMetadata(semanticsScope.DOCUMENT_META_METADATA);
 			documentClosure.changeDocument(newDocument);
 
 			System.out.println();
 		}
 		catch (SIMPLTranslationException e)
 		{
 			warning("Direct binding failed " + e);
 		}
 		return newDocument;
 	}
 
 	Stack<MetaMetadataNestedField>	currentMMstack	= new Stack<MetaMetadataNestedField>();
 
 	boolean													deserializingRoot	= true;
 
 	boolean													polymorphMmd			= false;
 	
 	/**
 	 * For the root, compare the meta-metadata from the binding with the one we started with. Down the
 	 * hierarchy, try to perform similar bindings.
 	 */
 	@Override
 	public void deserializationPreHook(Metadata deserializedMetadata, MetadataFieldDescriptor mfd)
 	{
 		if (deserializingRoot)
 		{
 			deserializingRoot													= false;
 			Document document													= getDocument();
 			MetaMetadataCompositeField preMM					= document.getMetaMetadata();
 			MetadataClassDescriptor mcd								= (MetadataClassDescriptor) ClassDescriptor.getClassDescriptor(deserializedMetadata);;
 			MetaMetadataCompositeField metaMetadata;
 			String tagName 														= mcd.getTagName();
 			if (preMM.getTagForTypesScope().equals(tagName))
 			{
 				metaMetadata														= preMM;
 			}
 			else
 			{	// just match in translation scope
 				//TODO use local TranslationScope if there is one
 				metaMetadata														= semanticsScope.getMetaMetadataRepository().getMMByName(tagName);
 			}
 			deserializedMetadata.setMetaMetadata(metaMetadata);
 			
 			polymorphMmd = true;
 
 			currentMMstack.push(metaMetadata);
 		}
 		else
 		{
 			String mmName = mfd.getMmName();
 			MetaMetadataNestedField currentMM = currentMMstack.peek();
 			MetaMetadataNestedField childMMNested = (MetaMetadataNestedField) currentMM
 					.lookupChild(mmName); // this fails for collections :-(
 			if (childMMNested == null)
 				throw new RuntimeException("Can't find composite child meta-metadata for " + mmName + " amidst "+ mfd +
 						"\n\tThis probably means there is a conflict between the meta-metadata repository and the runtime."+
 						"\n\tProgrammer: Have you Changed the fields in built-in Metadata subclasses without updating primitives.xml???!");
 			MetaMetadataCompositeField childMMComposite = null;
 			if (childMMNested.isPolymorphicInherently())
 			{
 				String tagName = ClassDescriptor.getClassDescriptor(deserializedMetadata).getTagName();
 				childMMComposite	= semanticsScope.getMetaMetadataRepository().getMMByName(tagName);
 				polymorphMmd = true;
 			}
 			else
 			{
 				childMMComposite	= childMMNested.metaMetadataCompositeField();
 			}
 			deserializedMetadata.setMetaMetadata(childMMComposite);
 			currentMMstack.push(childMMComposite);
 		}
 	}
 
 	@Override
 	public void deserializationInHook(Metadata deserializedMetadata, MetadataFieldDescriptor mfd)
 	{
 		if (polymorphMmd) // for efficiency; if it is not polymorphic case we don't have to look up mmd at this point of time
 		{
 			String mmName = deserializedMetadata.getMetaMetadataName();
 			if (mmName != null && mmName.length() > 0)
 			{
 				MetaMetadata trueMm = semanticsScope.getMetaMetadataRepository().getMMByName(mmName);
 				if (trueMm != null)
 				{
 					debug(String.format("setting [%s].metaMetadata to %s (mm_name=%s)...", deserializedMetadata, trueMm, mmName)); 
 					deserializedMetadata.setMetaMetadata(trueMm);
 				}
 				else
 				{
 					warning("polymorphicly looking up meta-metadata failed: cannot find mmd named as " + mmName);
 				}
 			}
 			polymorphMmd = true;
 		}
 	}
 	
 	@Override
 	public void deserializationPostHook(Metadata deserializedMetadata, MetadataFieldDescriptor mfd)
 	{
 		currentMMstack.pop();
 	}
 	
 	@Override
 	public Metadata changeObjectIfNecessary(Metadata deserializedMetadata, MetadataFieldDescriptor mdf)
 	{
 		if (!DONOT_LOOKUP_DOWNLOADED_DOCUMENT)
 		{
 			Document downloadedDoc = lookupDownloadedDocument(deserializedMetadata);
 			return downloadedDoc == null ? deserializedMetadata : downloadedDoc;
 		}
 		return deserializedMetadata;
 	}
 
 }
