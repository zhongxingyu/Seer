 package eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime;
 
 import static eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime.VarNameManager.getVarNameManager;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.sparql.expr.Expr;
 import com.hp.hpl.jena.sparql.syntax.Element;
 import com.hp.hpl.jena.sparql.syntax.ElementEventBinOperator;
 import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
 
 import eu.play_project.play_platformservices.QueryTemplateImpl;
 import eu.play_project.play_platformservices.api.QueryTemplate;
 import eu.play_project.play_platformservices_querydispatcher.AgregatedVariableTypes;
 import eu.play_project.play_platformservices_querydispatcher.AgregatedVariableTypes.AgregatedEventType;
 import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
 import eu.play_project.querydispatcher.epsparql.Test.helpers.GenerateConstructResultVisitor;
 
 
 public class EleGeneratorForConstructQuery implements EleGenerator {
 	private Query inputQuery;
 	private String elePattern;
 	private Element currentElement = null;
 	private VarNameManager varNameManager;
 	private Iterator<Element> eventQueryIter;
 	private EventTypeVisitor eventTypeVisitor;
 	private BinOperatorVisitor binOperatorVisitor;
 	private FilterExpressionCodeGenerator filterExpressionVisitor;
 	private HavingVisitor havingVisitor;
 	private TriplestoreQueryVisitor triplestoreQueryVisitor;
 	private Iterator<ElementEventBinOperator> binOperatorIter;
 	private String patternId;
 	
 	//Helper methods.
 	private Map<String, AgregatedEventType> variableAgregatedType;
 	private QueryTemplate queryTemplate;
 
 	@Override
 	public void generateQuery(Query inQuery) {
 		//Detect eventtypes
 		variableAgregatedType = new AgregatedVariableTypes().detectType(inQuery);
 
 		elePattern = "";
 		this.inputQuery = inQuery;
 		eventQueryIter = inQuery.getEventQuery().iterator();
 		binOperatorIter = inQuery.getEventBinOperator().iterator();
 		varNameManager = getVarNameManager();
 		varNameManager.newQuery();
 		eventTypeVisitor = new EventTypeVisitor();
 		triplestoreQueryVisitor = new TriplestoreQueryVisitor(varNameManager);
 		filterExpressionVisitor = new FilterExpressionCodeGenerator();
 		binOperatorVisitor =  new BinOperatorVisitor();
 		havingVisitor =  new HavingVisitor();
 		queryTemplate = new QueryTemplateImpl();
 		
 		varNameManager.setWindowTime(inQuery.getWindowTime());
 		ElePattern();
 	}
 	
 	private void ElePattern(){
 		Complex();
 		elePattern += "<-";
 		SimpleEventPattern();
 		
 		while(binOperatorIter.hasNext()){
 			binOperatorIter.next().visit(binOperatorVisitor);
 			elePattern += binOperatorVisitor.getBinOperator();
 			SimpleEventPattern();
 		}
 	}
 	
 	private void Complex() {
 		elePattern += "complex(" + varNameManager.getNextCeid() + "," + patternId + ") do (";
 		GenerateConstructResult();
 		SaveSharedVariabelValues();
 		Having();
		PrintStatisticsData();
 		DecrementReferenceCounter();
 		elePattern += ")";
 	}
 
 	private void GenerateConstructResult() {
 		GenerateConstructResultVisitor generateConstructResultVisitor = new GenerateConstructResultVisitor();
 		Iterator<Triple> iter = inputQuery.getConstructTemplate().getTriples()
 				.iterator();
 		Triple triple;
 		while (iter.hasNext()) {
 			triple = iter.next();
 			if (!containsSharedVariablesTest(triple)) {
 				elePattern += "generateConstructResult("
 						+ triple.getSubject().visitWith(
 								generateConstructResultVisitor)
 						+ ","
 						+ triple.getPredicate().visitWith(
 								generateConstructResultVisitor)
 						+ ","
 						+ triple.getObject().visitWith(
 								generateConstructResultVisitor) + ","
 						+ varNameManager.getCeid() + ")";
 				if (iter.hasNext()) {
 					elePattern += ",";
 				}
 			}
 		}
 	}
 
 	private boolean containsSharedVariablesTest(Triple triple){
 		boolean result = false;
 		if(triple.getSubject().isVariable()){
 			if(variableAgregatedType.keySet().contains(triple.getSubject().toString().substring(1))){
 				if(variableAgregatedType.get(triple.getSubject().toString().substring(1)).equals(AgregatedEventType.CRH) || variableAgregatedType.get(triple.getSubject().toString().substring(1)).equals(AgregatedEventType.RH)){
 					result = true;
 				}
 			}
 
 		}else if(triple.getPredicate().isVariable()){
 			if(variableAgregatedType.keySet().contains(triple.getPredicate().toString().substring(1))){
 				if(variableAgregatedType.get(triple.getPredicate().toString().substring(1)).equals(AgregatedEventType.CRH) || variableAgregatedType.get(triple.getPredicate().toString().substring(1)).equals(AgregatedEventType.RH)){
 					result = true;
 				}
 			}
 		}else if(triple.getObject().isVariable()){
 			if(variableAgregatedType.keySet().contains(triple.getObject().toString().substring(1))){
 				if(variableAgregatedType.get(triple.getObject().toString().substring(1)).equals(AgregatedEventType.CRH) || variableAgregatedType.get(triple.getObject().toString().substring(1)).equals(AgregatedEventType.RH)){
 					result = true;
 				}
 			}
 		}
 		return result;
 	}
 		
 	public void SaveSharedVariabelValues() {
 		StringBuffer tmpEle = new StringBuffer();
 
 		Iterator<String> iter = variableAgregatedType.keySet().iterator();
 
 		while (iter.hasNext()) {
 			String key = iter.next();
 
 			// Check if variable is in real time and historical part.
 			if (variableAgregatedType.get(key).equals(AgregatedEventType.RH) || variableAgregatedType.get(key).equals(AgregatedEventType.CRH)) {
 				if (!elePattern.endsWith(",")) {
 					elePattern += ",";
 				}
 				tmpEle.append("variabeValuesAdd(" + patternId + ",'" + key + "'," + "V" + key + ")");
 			}
 
 		}
 
 		elePattern += tmpEle.toString();
 	}
 	
 	private void DecrementReferenceCounter(){
 		StringBuffer tmp = new StringBuffer();
 		
 		for (String var : varNameManager.getAllTripleStoreVariablesOfThisQuery()) {
 			tmp.append(", decrementReferenceCounter( "+ var + ")");
 		}
 		
 		elePattern += tmp.toString();
 	}
 	
 	//Call prolog methods which echos statistics data to the console.
 	private void PrintStatisticsData(){
 		elePattern += ", printRdfStat, printRefCountN";
 	}
 	private void SimpleEventPattern() {
 	
 		elePattern += "(";
 		currentElement = eventQueryIter.next();
 		currentElement.visit(eventTypeVisitor);
 		elePattern += eventTypeVisitor.getEventType();
 		elePattern += "(";
 		String triplestoreVariable = varNameManager.getNextTriplestoreVariable();
 		elePattern += triplestoreVariable;
 		elePattern += ") 'WHERE' (";
 		AdditionalConditions();
 		elePattern += "))";
 	}
 	
 	private void AdditionalConditions(){
 		TriplestoreQuery();
 		FilterExpression();
 		ReferenceCounter();
 		//elePattern += ", ";
 		//PerformanceMeasurement();
 		
 		if(!binOperatorIter.hasNext()){
 			elePattern += ",";
 			GenerateCEID();
 		}
 	}
 
 	private void ReferenceCounter(){
 		elePattern += " incrementReferenceCounter(" + varNameManager.getTriplestoreVariable() + ")";
 	}
 	
 	private void PerformanceMeasurement(){
 		elePattern += "measure(" +  patternId + ")";
 	}
 	
 	private void TriplestoreQuery(){
 		currentElement.visit(triplestoreQueryVisitor);
 		elePattern += triplestoreQueryVisitor.getTriplestoreQueryGraphTerms();
 	}
 
 	private void FilterExpression(){
 		filterExpressionVisitor.startVisit(((ElementEventGraph)currentElement).getFilterExp());
 		if(!elePattern.endsWith(",") && !filterExpressionVisitor.getEle().equals("")){ // This filter is optional. No value needed.
 			elePattern += "," + filterExpressionVisitor.getEle();
 		}else if(!filterExpressionVisitor.getEle().equals("")){
 			elePattern += filterExpressionVisitor.getEle();
 		}
 	}
 		
 	private void GenerateCEID(){
 		elePattern += "random(1000000, 9000000, " + varNameManager.getCeid() + ")";
 	}
 
 	@Override
 	public String getEle() {
 		return elePattern;
 	}
 
 	@Override
 	public ArrayList<String[]> getEventProperties() {
 		return null;
 	}
 	
 	@Override
 	public void setPatternId(String patternId) {
 		this.patternId = "'" + patternId + "'";
 	}
 
 	@Override
 	public QueryTemplate getQueryTemplate() {
 		
 		return this.queryTemplate;
 	}
 	
 	
 	public void Having(){
 		
 		if(!inputQuery.getHavingExprs().isEmpty()){
 			elePattern += ", ";
 		}
 		
 		for (Expr el : inputQuery.getHavingExprs()) {
 			el.visit(havingVisitor);
 		}
 		
 		elePattern += havingVisitor.getCode().toString();
 	}
 }
