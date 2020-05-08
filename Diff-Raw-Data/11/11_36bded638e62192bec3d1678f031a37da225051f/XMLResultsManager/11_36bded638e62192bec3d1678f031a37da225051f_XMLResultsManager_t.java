 package org.eclipse.emf.refactor.metrics.runtime.managers;
 
 import java.text.DecimalFormat;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.refactor.metrics.managers.XMLManager;
 import org.eclipse.emf.refactor.metrics.runtime.core.Result;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class XMLResultsManager extends XMLManager {
 
 	private static final String RESULTS_TAG = "results";
 	
 	public static void saveResults(String path, List<Result> results){
 		final DocumentBuilder builder = createDocumentBuilder();
 		if (builder != null) {
 			final Document doc = builder.newDocument();		
 			final Element root = doc.createElement(RESULTS_TAG);
 			doc.appendChild(root);	
 			createAllContextElements(root, doc, results);
 			final Transformer transformer = createTransformer();
 			final DOMSource source = new DOMSource(doc);		
 			final StreamResult result = new StreamResult(path);			
 			try {
 				transformer.transform(source, result);
 			} catch (final TransformerException e) {
 				e.printStackTrace();
 			}		
 		}
 	}
 	
 	private static void createAllContextElements(Element root, Document doc, List<Result> results) {
 		List<List<EObject>> allContexts = new LinkedList<List<EObject>>();
 		for(Result result : results)
 			if(!allContexts.contains(result.getContext()))
 				allContexts.add(result.getContext());
 		for(List<EObject> context : allContexts)
 			root.appendChild(createContextElement(doc, context, filterResults(results, context)));
 	}
 	
 	private static Element createContextElement(Document doc, List<EObject> cont, List<Result> results){
 		final Element context = doc.createElement("context");
 		final Element contextType = doc.createElement("contextType");
 		contextType.setTextContent(cont.get(0).eClass().getName());
 		context.appendChild(contextType);
 		final Element contextValue = doc.createElement("contextValue");
 		contextValue.setTextContent(getContextValue(cont.get(0)));
 		context.appendChild(contextValue);
 		for (Object rawResult : results){
 			Result result = (Result) rawResult;
 			Element resultEntry = doc.createElement("metricResult");
 			Element dateStamp = doc.createElement("date");
 			dateStamp.setTextContent(result.getTimeStamp());
 			resultEntry.appendChild(dateStamp);			
 			Element metricName = doc.createElement("metricName");
 			metricName.setTextContent(result.getMetric().getName());
 			resultEntry.appendChild(metricName);			
 			Element metricDescription = doc.createElement("metricDescription");
 			metricDescription.setTextContent(result.getMetric().getDescription());
 			resultEntry.appendChild(metricDescription);			
 			Element resultValue = doc.createElement("resultValue");
			DecimalFormat df = new DecimalFormat("##.##");
 			String valueStr = "";
 			Double value = result.getResultValue();
 			if (value.equals(Double.NaN)) {
 				valueStr = "NaN";
 			} else {
				Double doubleToString = Math.round(result.getResultValue()*100)/100.0;
				valueStr = Double.toString(doubleToString);
//				valueStr = df.format(result.getResultValue());
				
//				valueStr = String.format("%.2f", result.getResultValue());
 			}
			System.out.println("))) value: " + valueStr);
 			resultValue.setTextContent(valueStr);
 			resultEntry.appendChild(resultValue);
 			context.appendChild(resultEntry);
 		}
 		return context;
 	}
 	
 	private static String getContextValue(EObject eObject) {
 		String ret = "";
 		for(EAttribute attribute : eObject.eClass().getEAllAttributes()){
 			if (attribute.getName().equalsIgnoreCase("name")) {
 				ret = (String) eObject.eGet(attribute);
 				break;
 			}
 		}
 		if (! ret.isEmpty()) return ret;
 		return eObject.toString();
 	}
 
 	private static List<Result> filterResults(List<Result> results, List<EObject> context){
 		List<Result> filteredResults = new LinkedList<Result>();
 		for(Result result : results)
 			if(result.getContext().equals(context))
 				filteredResults.add(result);
 		return filteredResults;
 	}
 	
 }
