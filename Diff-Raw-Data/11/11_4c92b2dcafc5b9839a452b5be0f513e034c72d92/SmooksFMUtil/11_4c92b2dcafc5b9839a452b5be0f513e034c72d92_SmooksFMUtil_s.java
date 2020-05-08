 /*
  *
  */
 package org.smooks.directmapping.gae.util;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.eclipse.emf.common.util.URI;
 
 import org.smooks.directmapping.mapping.model.util.Functions;
 import org.smooks.directmapping.mapping.model.util.FunctionsValues;
 import org.smooks.directmapping.model.ModelBuilder;
 import org.smooks.directmapping.model.ModelBuilderException;
 import org.smooks.directmapping.model.ModelBuilder.ElementType;
 import org.smooks.directmapping.model.xml.XMLSampleModelBuilder;
 import org.smooks.directmapping.model.xml.XSDModelBuilder;
 import org.smooks.directmapping.template.CollectionMapping;
 import org.smooks.directmapping.template.exception.InvalidMappingException;
 import org.smooks.directmapping.template.exception.TemplateBuilderException;
 import org.smooks.directmapping.template.freemarker.FreeMarkerTemplateBuilder;
 import org.smooks.directmapping.template.util.FreeMarkerUtil;
 import org.smooks.directmapping.template.xml.XMLFreeMarkerTemplateBuilder;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.stream.JsonReader;
 
 /**
  * SmooKs utility methods.
  * 
  * @author <a href="mailto:mskackov-at-google-mail">michal skackov</a>
  */
 public class SmooksFMUtil {
 
 	public static InputStream getSmooksConfiguration(String template) throws IOException
 	{
 		Writer templateWriter = new StringWriter();
 		
 		templateWriter.write("<?xml version=\"1.0\"?>");
 		templateWriter.write('\n');
 		templateWriter.write("<smooks-resource-list xmlns=\"http://www.milyn.org/xsd/smooks-1.1.xsd\" xmlns:core=\"http://www.milyn.org/xsd/smooks/smooks-core-1.3.xsd\" xmlns:ftl=\"http://www.milyn.org/xsd/smooks/freemarker-1.1.xsd\">");
 		templateWriter.write('\n');
 		templateWriter.write("<ftl:freemarker applyOnElement=\"#document\">");
 		templateWriter.write('\n');	
 		templateWriter.write("<ftl:template>");
 		templateWriter.write("<![CDATA[");
 
 		// ADD FREEMARKER TEMPLATE
 		templateWriter.write(template);
 	
 
 		templateWriter.write("]]>");
 		templateWriter.write("</ftl:template>");
 		templateWriter.write('\n');
 		templateWriter.write("</ftl:freemarker>");
 		templateWriter.write('\n');
 		templateWriter.write("<resource-config selector=\"#document\">");
 		templateWriter.write('\n');
 		templateWriter.write("<resource>org.milyn.delivery.DomModelCreator</resource>");
 		templateWriter.write('\n');
 		templateWriter.write("</resource-config>");
 		templateWriter.write('\n');
 		templateWriter.write("</smooks-resource-list>");
 		
 		InputStream is = new ByteArrayInputStream(templateWriter.toString().getBytes());
 		return is;
 	}
 
 	public static Writer getSmooksConfigurationWriter(String template) throws IOException
 	{
 		Writer templateWriter = new StringWriter();
 		
 		templateWriter.write("<?xml version=\"1.0\"?>");
 		templateWriter.write('\n');
 		templateWriter.write("<smooks-resource-list xmlns=\"http://www.milyn.org/xsd/smooks-1.1.xsd\" xmlns:core=\"http://www.milyn.org/xsd/smooks/smooks-core-1.3.xsd\" xmlns:ftl=\"http://www.milyn.org/xsd/smooks/freemarker-1.1.xsd\">");
 		templateWriter.write('\n');
 		templateWriter.write("<ftl:freemarker applyOnElement=\"#document\">");
 		templateWriter.write('\n');	
 		templateWriter.write("<ftl:template>");
 		templateWriter.write("<![CDATA[");
 
 		// ADD FREEMARKER TEMPLATE
 		templateWriter.write(template);
 	
 
 		templateWriter.write("]]>");
 		templateWriter.write("</ftl:template>");
 		templateWriter.write('\n');
 		templateWriter.write("</ftl:freemarker>");
 		templateWriter.write('\n');
 		templateWriter.write("<resource-config selector=\"#document\">");
 		templateWriter.write('\n');
 		templateWriter.write("<resource>org.milyn.delivery.DomModelCreator</resource>");
 		templateWriter.write('\n');
 		templateWriter.write("</resource-config>");
 		templateWriter.write('\n');
 		templateWriter.write("</smooks-resource-list>");
 		
 		
 		return templateWriter;
 	}
 	
 	/**
 	 * @param source
 	 * @param mapping
 	 * @param target
 	 * @return
 	 * @throws InvocationTargetException
 	 * @throws IOException
 	 * @throws ModelBuilderException
 	 * @throws TemplateBuilderException
 	 * @throws XPathExpressionException 
 	 * @throws TransformerException 
 	 */
 	public static String createTemplateURI( String source, String mapping, String target)
 			throws InvocationTargetException, IOException,
 			ModelBuilderException, TemplateBuilderException, XPathExpressionException, TransformerException {
 		FreeMarkerTemplateBuilder targetBuilder = null;
 		FreeMarkerTemplateBuilder sourceBuilder = null;
 		ModelBuilder targeModel;
 		ModelBuilder sourceModel;
 
 		targeModel = new XMLSampleModelBuilder(URI.createFileURI(target),false);
 		ModelBuilder.setStrictModel(targeModel.buildModel(), true)		;
 		ModelBuilder.setEnforceCollectionSubMappingRules((Element) targeModel.buildModel().getDocumentElement(), true)		;
 	
 		targeModel.configureModel();
 		
 		sourceModel = new XMLSampleModelBuilder(URI.createFileURI(source),false);
 		ModelBuilder.setStrictModel(sourceModel.buildModel(), true)		;
 		ModelBuilder.setEnforceCollectionSubMappingRules((Element) sourceModel.buildModel().getDocumentElement(), true)		;
 		sourceModel.configureModel();
 		
 		
 	
 		//printDocument(builder.buildModel(), System.out);
 	
 		targetBuilder = new XMLFreeMarkerTemplateBuilder(targeModel);
 		sourceBuilder =  new XMLFreeMarkerTemplateBuilder(sourceModel);
 		targetBuilder = addMappping(sourceBuilder, mapping, targetBuilder);
 		//printDocument(targetBuilder.getModel(), System.out);
 		targetBuilder.setNodeModelSource(true);
 		return targetBuilder.buildTemplate();
 		
 	}
 	
 	public static String createTemplateFromXML(String sourceXML, String mapping, String functions, String targetXML)
 			throws InvocationTargetException, ModelBuilderException,
 			TemplateBuilderException, XPathExpressionException, IOException, TransformerException {
 		FreeMarkerTemplateBuilder targetBuilder = null;
 		FreeMarkerTemplateBuilder sourceBuilder = null;
 		ModelBuilder targetModel;
 		ModelBuilder sourceModel;
 
 		targetModel = new XMLSampleModelBuilder(targetXML);
 		ModelBuilder.setStrictModel(targetModel.buildModel(), true)		;
 		ModelBuilder.setEnforceCollectionSubMappingRules((Element) targetModel.buildModel().getDocumentElement(), true)		;
 	
 		targetModel.configureModel();
 		
 		sourceModel = new XMLSampleModelBuilder(sourceXML);
 		ModelBuilder.setStrictModel(sourceModel.buildModel(), true)		;
 		ModelBuilder.setEnforceCollectionSubMappingRules((Element) sourceModel.buildModel().getDocumentElement(), true)		;
 		sourceModel.configureModel();
 		
 	
 		targetBuilder = new XMLFreeMarkerTemplateBuilder(targetModel);
 		sourceBuilder =  new XMLFreeMarkerTemplateBuilder(sourceModel);
 		targetBuilder = addMappping(sourceBuilder, mapping, targetBuilder);
 		targetBuilder = addFunctions(sourceBuilder, functions, targetBuilder);
 		targetBuilder.setNodeModelSource(true);
 		return targetBuilder.buildTemplate();
 
 		
 
 	}
 
 	public static String createTemplateFromXSD(String sourceXSD, String sourceRootElement, String mapping, String functions, String targetXSD, String targetRootElement)
 			throws InvocationTargetException, ModelBuilderException,
 			TemplateBuilderException, XPathExpressionException, IOException, TransformerException {
 		FreeMarkerTemplateBuilder targetBuilder = null;
 		FreeMarkerTemplateBuilder sourceBuilder = null;
 		ModelBuilder targetModel;
 		ModelBuilder sourceModel;
 
 		targetModel = new XSDModelBuilder(targetXSD,targetRootElement);
 		ModelBuilder.setStrictModel(targetModel.buildModel(), true);
 		
 		ModelBuilder.setEnforceCollectionSubMappingRules((Element) targetModel.buildModel().getDocumentElement(), true)		;
 		targetModel.configureModel();
 		
 		sourceModel = new XSDModelBuilder(sourceXSD,sourceRootElement);
 		ModelBuilder.setStrictModel(sourceModel.buildModel(), true)		;
 		ModelBuilder.setEnforceCollectionSubMappingRules((Element) sourceModel.buildModel().getDocumentElement(), true)		;
 		sourceModel.configureModel();
 		
 		targetBuilder = new XMLFreeMarkerTemplateBuilder(targetModel);
 		sourceBuilder =  new XMLFreeMarkerTemplateBuilder(sourceModel);
 		targetBuilder = addMappping(sourceBuilder, mapping, targetBuilder);
 		targetBuilder = addFunctions(sourceBuilder, functions, targetBuilder);
 		targetBuilder.setNodeModelSource(true);
 		return targetBuilder.buildTemplate();
 
 		
 
 	}
 
 	public static FreeMarkerTemplateBuilder addMappping(
 			FreeMarkerTemplateBuilder templateBuilder, String mapping)
 			throws InvalidMappingException, XPathExpressionException {
 
 		/*try {
 			printDocument(templateBuilder.getModel(),System.out);
 		} catch (IOException | TransformerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 		
 		
 		
 		JsonParser parser = new JsonParser();
 		JsonArray o = (JsonArray) parser.parse(mapping);
 
 		Iterator<JsonElement> iterator = o.iterator();
 
 		while (iterator.hasNext()) {
 			JsonObject json = (JsonObject) iterator.next();
 			String from = json.get("from").getAsString();
 			String to = json.get("to").getAsString();
 
 			templateBuilder.addValueMapping(from,
 					templateBuilder.getModelNode(to));
 
 		}
 
 		return templateBuilder;
 
 	}
 
 	public static FreeMarkerTemplateBuilder addMappping(FreeMarkerTemplateBuilder sourceBuilder,  String mapping,   FreeMarkerTemplateBuilder targetBuilder) throws InvalidMappingException, XPathExpressionException, TransformerException{
 	/*	try {
 			printDocument(targetBuilder.getModel(),System.out);
 		} catch (IOException | TransformerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 		
 		JsonParser parser = new JsonParser();
 		JsonArray o = (JsonArray)parser.parse(mapping);
 		List<CollectionMapping> collectionMappings = new ArrayList<CollectionMapping>();
 		
 		
 		Iterator<JsonElement> iterator = o.iterator();
 		//TODO identify the collection mappings mapped to complex types create list ie collection mappings get the collection names as last part of xpath
 		while(iterator.hasNext()){
 			JsonObject json = (JsonObject)iterator.next();
 		    String from = json.get("from").getAsString();
 		    String to = json.get("to").getAsString();
 		    if(!(from.contains(" Input") || to.contains(" Output"))){
 		    	
 		    
 		    
 		    
 		    Node source =  sourceBuilder.getModelNode(from);
 		    Node target =  targetBuilder.getModelNode(to);
 		    
 		    if(source == null || target == null)
 		    {
 		    	throw new InvalidMappingException("cannot find matching source-to-target dependency"); 
 		    }else{
 		   
 		    	if(source.getNodeType() == Node.ELEMENT_NODE) { 
				   if(ModelBuilder.getElementType((Element) source) == ElementType.complex && ModelBuilder.getMaxOccurs((Element) source) > 1){
 					   CollectionMapping collection = new CollectionMapping(from, target, source.getNodeName());
 					   collectionMappings.add(collection);
 				   }
 				   else{
 					   targetBuilder.addValueMapping(from,target);   
 				   }
 		    	}else{
 		    		   targetBuilder.addValueMapping(from,target);   
 		    	}
 		    }
 		    
 		    
 		    
 		    }
 		}
		
 		for(CollectionMapping collection : collectionMappings) {
 			//TODO change it ?
 			targetBuilder.addCollectionMapping(collection.getSrcPath(), (Element) collection.getMappingNode(), collection.getCollectionItemName());
 			ModelBuilder.setCollectionVariable((Element) collection.getMappingNode(), collection.getCollectionItemName(), collection.getSrcPath());
 		}
				
 		return targetBuilder;
 			
 	}
 	
 	public static String removeXmlStringNamespaceWithouthPrefix(String xmlString) {
 		  return xmlString.replaceAll("xmlns.(\"|\').*?(\"|\')", ""); /* remove xmlns declaration */
 		  
 		}
 	
 	public static String removeXmlStringNamespaceAndPreamble(String xmlString) {
 		  return xmlString.replaceAll("(<\\?[^<]*\\?>)?", ""). /* remove preamble */
 		  replaceAll("xmlns.*?(\"|\').*?(\"|\')", "") /* remove xmlns declaration */
 		  .replaceAll("(<)(\\w+:)(.*?>)", "$1$3") /* remove opening tag prefix */
 		  .replaceAll("(</)(\\w+:)(.*?>)", "$1$3"); /* remove closing tags prefix */
 		}
 	
 	public static FreeMarkerTemplateBuilder addFunctions(FreeMarkerTemplateBuilder sourceBuilder,  String functions,   FreeMarkerTemplateBuilder targetBuilder) throws InvalidMappingException, XPathExpressionException{
 		Gson gson = new Gson();
 		Functions[] obj = null;
 		
 		/**
 		try {
 			printDocument(targetBuilder.getModel(),System.out);
 		} catch (IOException | TransformerException e) {
 		
 			e.printStackTrace();
 		}
 		**/
 		
 		JsonReader reader = new JsonReader(new StringReader(functions));
 		reader.setLenient(true);
 
 		obj = gson.fromJson(reader, Functions[].class);
 
 		List<Node> inputParametersNodes = new ArrayList<Node>();
 		
 		
 		for (Functions iterator : obj){ 
 		
 		//TODO identify the collection mappings mapped to complex types create list ie collection mappings get the collection names as last part of xpath
 		
 			Functions function = iterator;
 			String functionValue = function.getValue();
 			
 			Iterator<FunctionsValues> inputparamIterator = function.getInput().iterator();
 			FunctionsValues inputparam;
 			
 			//loop through each elementand replace the value within the function with the new FreeMarker value - again problem in case of complex functions
 			
 			while(inputparamIterator.hasNext()){
 				inputparam = inputparamIterator.next();
 			
 				Node inputNode =  sourceBuilder.getModelNode(inputparam.getXpath());
 				ModelBuilder.getListVariable((Element)inputNode);
 				inputParametersNodes.add(inputNode);
 				String name = (String) inputparam.getName().subSequence(0, inputparam.getName().indexOf(":")+1);
 				
 				
 				// complex element part of function create list in FreeMarker
 				if(ModelBuilder.getElementType((Element) inputNode) == ElementType.complex && ModelBuilder.getMaxOccurs((Element) inputNode) > 1){
 					   
 					//TODO add support for complex functions
 				   }
 				   else{  // node is part of collection use the collection variable name
 					   if(ModelBuilder.getListVariable((Element) inputNode) != null && ModelBuilder.getListVariable((Element) inputNode) != "") {
 						   functionValue = functionValue.replace(name, "${" +  ModelBuilder.getListVariable((Element) inputNode) + "[0]!\"\"}");
 					   }  // node is not part of collection
 					   else{
 						   
 						   String freeMarkerVar = "${" + FreeMarkerUtil.toPathPerElement(inputparam.getXpath(), true) + "\"\"}";
 						   
 						   functionValue = functionValue.replace(name, freeMarkerVar);
 					   }
 						   
 				   }
 				
 				
 				
 			}
 			FunctionsValues output = null;
 			Iterator<FunctionsValues> outputparamIterator = function.getOutput().iterator();
 			Node targetNode = null;
 			//target node for the moment supported only one output per function 
 			while(outputparamIterator.hasNext()){
 				 output = outputparamIterator.next();
 				 targetNode =  targetBuilder.getModelNode(output.getXpath());
 			}
 			ModelBuilder.setFunctionValue((Element) targetNode,functionValue);
 			/**
 			 * 	try {
 			
 				printDocument(targetBuilder.getModel(),System.out);
 			} catch (IOException | TransformerException e) {
 			
 				e.printStackTrace();
 			}
 			 */
 			
 		}
 		return targetBuilder;
 			
 	}
 	
 	public static void printDocument(org.w3c.dom.Document document,
 			OutputStream out) throws IOException, TransformerException {
 		TransformerFactory tf = TransformerFactory.newInstance();
 		Transformer transformer = tf.newTransformer();
 		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
 		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
 		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
 		transformer.setOutputProperty(
 				"{http://xml.apache.org/xslt}indent-amount", "4");
 
 		transformer.transform(new DOMSource(document), new StreamResult(
 				new OutputStreamWriter(out, "UTF-8")));
 	}
 }
