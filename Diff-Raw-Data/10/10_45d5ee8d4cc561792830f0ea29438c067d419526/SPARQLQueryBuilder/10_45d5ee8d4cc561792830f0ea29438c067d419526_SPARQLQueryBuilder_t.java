 package org.weso.wesearch.domain;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.weso.utils.OntoModelException;
 import org.weso.utils.QueryBuilderException;
 import org.weso.wesearch.domain.impl.filters.Filter;
 import org.weso.wesearch.domain.impl.filters.Operator;
 import org.weso.wesearch.domain.impl.filters.SPARQLFilter;
 import org.weso.wesearch.domain.impl.filters.SPARQLFilters;
 import org.weso.wesearch.model.OntoModelWrapper;
 import org.weso.wesearch.model.OntologyHelper;
 
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 /**
  * It's an auxiliary class that helps wesearchs to build SPARQL queries
  * @author Ignacio Fuertes Bernardo
  *
  */
 public class SPARQLQueryBuilder {
 	
 	private static Logger logger = Logger.getLogger(SPARQLQueryBuilder.class);
 	
 	/**
 	 * This method creates the type clause from a given name of variable and the
 	 * name of the auxiliary variable. An example of SPARQL type clause is "
 	 * ?x rdf:type ?y ."
 	 * @param name The name of the filtered variable
 	 * @param objName The name of the auxiliary variable that will be filtered
 	 * to limit the type of the main variable of the clause
 	 * @return The type clause
 	 * @throws QueryBuilderException This exception is thrown if some of the
 	 * parameters is null
 	 */
 	public static String getTypeClause(String name, String objName) 
 			throws QueryBuilderException {
 		if(name == null || objName == null) {
 			logger.error("Some part of the query are null");
 			throw new QueryBuilderException("Some part of the query are null");
 		}
 		String clause = "?" + name + " <" + RDF.type.toString() + "> ?" +
 				objName;
 		return clause;
 	}
 	
 	/**
 	 * This method generates a property clause for a query from a given variable
 	 * name, property and auxiliary variable. An example of SPARQL property
 	 * clause is "?x foaf:name ?y ." 
 	 * @param name The name of the main variable of the clause
 	 * @param property The property that must be used in the clause
 	 * @param objName The name of the auxiliary variable used in object part of
 	 * a clause 
 	 * @return The property clause generated
 	 * @throws QueryBuilderException This exception is thrown if some of the
 	 * parameters is null
 	 */
 	public static String getPropertyClause(String name, Property property, 
 			String objName) throws QueryBuilderException {
 		if(name == null || property == null || objName == null) {
 			logger.error("Some part of the query are null");
 			throw new QueryBuilderException("Some part of the query are null");
 		}
 		String query = "?" + name + " <" + property.getUri() + "> " 
 				+ "?" + objName;
 		return query;
 	}
 	
 	/**
 	 * This method generates a filter for a given variable name and a value 
 	 * selector
 	 * @param name The name of the variable that must be filtered
 	 * @param selector The value selector that contains the values to filter
 	 * @return The generated filter
 	 * @throws QueryBuilderException This exception is thrown if one or more 
 	 * parameters are null
 	 */
 	public static Filter getFilter(String name, ValueSelector selector) 
 			throws QueryBuilderException {
 		if(name == null || selector == null || selector.getValue() == null || 
 				selector.getValue().getValue() == null) {
 			logger.error("Some part of the query are null");
 			throw new QueryBuilderException("Some part of the query are null");
 		}
 		String clause = generateFilterClause(name, selector);
 		if(clause.equals("")) {
 			logger.error("Invalid value selector for filter clause");
 			throw new QueryBuilderException("Invalid value selector for " +
 					"filter clause");
 		}
 		return new SPARQLFilter(clause);
 	}
 
 	/**
 	 * This method has to generate a filter clause corresponding with the type
 	 * of the value selector
 	 * @param name The name of the variable that has to be filtered
 	 * @param selector The selector to use in the filter
 	 * @return The filter clause generated
 	 */
 	private static String generateFilterClause(String name,
 			ValueSelector selector) {
 		String clause = "";
 		if(selector.getType().equals(ValueSelector.TEXT) ||
 				selector.getType().equals(ValueSelector.UNDEFINED)) {
 			clause += "regex(?" + name + ", \"" + 
 				selector.getValue().getValue() + "\", \"i\")";			
 		} else if (selector.getType().equals(ValueSelector.NUMERIC)) {
 			clause += "xsd:decimal(?" + name + ") = xsd:decimal('" + 
 					selector.getValue().getValue() + "')";
 		} else if (selector.getType().equals(ValueSelector.DATE)) {
 			clause += "xsd:date(?" + name + ") = xsd:date('"
 					+ selector.getValue().getValue() + "')";
 		}
 		return clause;
 	}
 	
 	/**
 	 * This method generates a class filter given the name of a variable, a 
 	 * matter and the model to add subclasses
 	 * @param varName The name of the variable that must be filtered
 	 * @param matter The matter that the variable that has to be
 	 * @param model The model to extract subclasses of the matter
 	 * @return The generated filters
 	 * @throws OntoModelException This exception is thrown if there is a problem
 	 * extracting all subclassesof the given matter
 	 * @throws QueryBuilderException This exception is thrown if there is a 
 	 * problem building the filter
 	 */
 	public static SPARQLFilters getClassFilter(String varName, Matter matter, 
 			OntoModelWrapper model) throws OntoModelException, 
 			QueryBuilderException {
 		if(varName == null || matter == null || model == null) {
 			logger.error("Some part of the query are null");
 			throw new QueryBuilderException("Some part of the query are null");
 		}
 		List<String> classes = OntologyHelper.extractSubclasses(matter, 
 				(OntModel)model.getModel());
 		String clause = "?" + varName + " = <" + matter.getUri() + "> ";
 		SPARQLFilters result = new SPARQLFilters(new SPARQLFilter(clause));
 		SPARQLFilters aux = result;
 		concatAllClassFilter(varName, classes, aux);
 		return result;
 	}
 
 	/**
 	 * This method has to concatenate all calls filter over one variable
 	 * @param varName The name of the variable that has to be filtered
 	 * @param classes The list of all classes to filter the variable
 	 * @param aux The first class filter
 	 */
 	private static void concatAllClassFilter(String varName,
 			List<String> classes, SPARQLFilters aux) {
 		for(int i = 0; i < classes.size(); i++) {
 			String clazz = classes.get(i);
			if(clazz != null) {
				aux.setOp(Operator.OR);
				String auxClause = "?" + varName + " = <" + clazz + "> ";
				aux.setFilters(new SPARQLFilters(new SPARQLFilter(auxClause)));
				aux = (SPARQLFilters)aux.getFilters();
			}
 		}
 	}
 	
 }
