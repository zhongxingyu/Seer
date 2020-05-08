 package org.eclipse.emf.refactor.smells.runtime.managers;
 
 import java.util.LinkedList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.refactor.smells.managers.XMLManager;
 import org.eclipse.emf.refactor.smells.runtime.core.EObjectGroup;
 import org.eclipse.emf.refactor.smells.runtime.core.ModelSmellResult;
 import org.eclipse.emf.refactor.smells.runtime.core.ResultModel;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * Class for writing the search results to an xml file. Currently used by the old
  * <br>- ModelSmellResultsView.
  * @author Pawel Stepien
  * @author Matthias Burhenne
  *
  */
 
 public class XMLResultsManager extends XMLManager {
 
 	private static final String RESULTS_TAG = "results";
 	private static final String RESULT_TAG = "result";
 	private static final String SMELL_TAG = "smell";
 	private static final String ELEMENTS_TAG = "elements";
 	private static final String ELEMENT_TAG = "element";
 	
 	private static final String FILE_ATT = "file";
 	private static final String DATE_ATT = "date";
 	private static final String NUMBER_ATT = "number";
 	private static final String NAME_ATT = "name";
 	private static final String DESCRIPTION_ATT = "description";
 	
 	public static void saveResults(String path, LinkedList<ResultModel> results, Shell shell, boolean showDialog){
 		final DocumentBuilder builder = createDocumentBuilder();
 		if (builder != null){
 			final Document doc = builder.newDocument();		
 			final Element root = doc.createElement(RESULTS_TAG);
 			doc.appendChild(root);	
 			createElements(root, doc, results);
 			final Transformer transformer = createTransformer();
 			final DOMSource source = new DOMSource(doc);		
 			final StreamResult result = new StreamResult(path);			
 			try {
 				transformer.transform(source, result);
 			} catch (final TransformerException e) {
 				MessageDialog.openInformation(shell, "EMF Smell", "Error when saving XML file: " + e.getMessage());
 				e.printStackTrace();
 			}	
 			if (showDialog) {
 				MessageDialog.openInformation(shell, "EMF Smell", "XML file successfully saved.");
 			}
 		}
 	}
 
 	private static void createElements(Element root, Document doc, LinkedList<ResultModel> results) {
 		for (ResultModel resultModel : results) {
 			Element result = doc.createElement(RESULT_TAG);
 			result.setAttribute(NUMBER_ATT, Integer.toString(results.indexOf(resultModel)+1));
 			result.setAttribute(FILE_ATT, resultModel.getIFile().getName());
 			result.setAttribute(DATE_ATT, resultModel.getDate().toString());
 			for (ModelSmellResult modelSmellResult: resultModel.getModelSmellResults()) {
 				Element smell = doc.createElement(SMELL_TAG);
 				smell.setAttribute(NAME_ATT, modelSmellResult.getModelSmell().getName());
 				smell.setAttribute(DESCRIPTION_ATT, modelSmellResult.getModelSmell().getDescription());
 				smell.setAttribute(NUMBER_ATT, Integer.toString(modelSmellResult.getEObjectGroups().size()));
 				for (EObjectGroup eObjectGroup : modelSmellResult.getEObjectGroups()) {
 					Element elements = doc.createElement(ELEMENTS_TAG);
 					elements.setAttribute(NUMBER_ATT, Integer.toString(modelSmellResult.getEObjectGroups().indexOf(eObjectGroup)+1));
 					for (EObject eObject : eObjectGroup.getEObjects()) {
 						Element element = doc.createElement(ELEMENT_TAG);
 						element.setTextContent(getNameOfObject(eObject));
 						elements.appendChild(element);
 					}
 					smell.appendChild(elements);
 				}
 				result.appendChild(smell);
 			}
 			root.appendChild(result);
 		}
 	}
 	
 	private static String getNameOfObject(EObject eObject) {
 		String ret = "";
 		for(EAttribute attribute : eObject.eClass().getEAllAttributes()){
			if (attribute.getName().equals(NAME_ATT)) {
 				ret = (String) eObject.eGet(attribute);
 				if (ret == null) ret = "";
 				break;
 			}
 		}
 		System.out.println("name: " + ret);
 		if (! ret.isEmpty()) return ret;	
 		return eObject.toString();
 	}
 	
 	
 	
 	
 //	private static void createAllContextElements(Element root, Document doc, List<Result> results){
 //		List<List<EObject>> allContexts = new LinkedList<List<EObject>>();
 //		for(Result result : results)
 //			if(!allContexts.contains(result.getContext()))
 //				allContexts.add(result.getContext());
 //		for(List<EObject> context : allContexts)
 //			root.appendChild(createContextElement(doc, context, filterResults(results, context)));
 //	}
 	
 //	private static Element createContextElement(Document doc, List<EObject> cont, List<Result> results){
 //		final Element context = doc.createElement("context");
 //		final Element contextValue = doc.createElement("contextValue");
 //		contextValue.setTextContent(cont.get(0).toString());
 //		context.appendChild(contextValue);
 //		for (Object rawResult : results){
 //			Result result = (Result)rawResult;
 //			Element resultEntry = doc.createElement("metricResult");
 //			Element dateStamp = doc.createElement("date");
 //			dateStamp.setTextContent(result.getTimeStamp());
 //			resultEntry.appendChild(dateStamp);
 //			Element smellEntry = doc.createElement(MODELSMELL_TAG);
 //			Element smellName = doc.createElement(MODELSMELL_NAME_TAG);
 //			smellName.setTextContent(result.getSmell().getName());
 //			smellEntry.appendChild(smellName);
 //			Element smellDescription = doc.createElement("metricDescription");
 //			smellDescription.setTextContent(result.getSmell().getDescription());
 //			smellEntry.appendChild(smellDescription);
 //			resultEntry.appendChild(smellEntry);
 //			Element resultValue = doc.createElement("resultValue");
 //			resultValue.setTextContent(""+result.getResultValue());
 //			resultEntry.appendChild(resultValue);
 //			context.appendChild(resultEntry);
 //		}
 //		return context;
 //	}
 //	
 //	private static List<Result> filterResults(List<Result> results, List<EObject> context){
 //		List<Result> filteredResults = new LinkedList<Result>();
 //		for(Result result : results)
 //			if(result.getContext().equals(context))
 //				filteredResults.add(result);
 //		return filteredResults;
 //	}
 //	
 }
