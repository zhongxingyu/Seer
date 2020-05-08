 package cz.cuni.mff.odcleanstore.datanormalization.rules;
 
 import java.util.Locale;
 
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.XSD;
 
 import cz.cuni.mff.odcleanstore.datanormalization.exceptions.DataNormalizationException;
import cz.cuni.mff.odcleanstore.vocabulary.XPathFunctions;
 
 public class DataNormalizationBooleanRule extends DataNormalizationRule {
 	private static final long serialVersionUID = 1L;
 	
 	private static final String boolTruePattern = "?o = '1' OR lcase(str(?o)) = 'true' OR lcase(str(?o)) = 'yes' OR lcase(str(?o)) = 't' OR lcase(str(?o)) = 'y'";
 	private static final String boolFalsePattern = "?o = '0' OR lcase(str(?o)) = 'false' OR lcase(str(?o)) = 'no' OR lcase(str(?o)) = 'f' OR lcase(str(?o)) = 'n'";
 	
	private static final String convertedTruePropertyValueFormat = "DELETE {?s <%s> ?o} INSERT {?s <%s> ?t} WHERE {GRAPH $$graph$$ {SELECT ?s <%s>(1) AS ?t ?o WHERE {?s <%s> ?o. FILTER (" + boolTruePattern + ")}}}";
	private static final String convertedFalsePropertyValueFormat = "DELETE {?s <%s> ?o} INSERT {?s <%s> ?f} WHERE {GRAPH $$graph$$ {SELECT ?s <%s>(0) AS ?f ?o WHERE {?s <%s> ?o. FILTER (" + boolFalsePattern + ")}}}";
 
 	public DataNormalizationBooleanRule (Integer id, Integer groupId, Resource property) throws DataNormalizationException {
 		super(id, groupId,
 				property.getLocalName() + "-bool-conversion",
 				"Convert " + property.getLocalName() + " into " + XSD.xboolean.getLocalName(),
 				
 				"MODIFY",
				String.format(Locale.ROOT, convertedTruePropertyValueFormat, property.getURI(), property.getURI(), XPathFunctions.boolFunction, property.getURI()),
				"Convert " + XSD.xboolean.getLocalName() + " value for the property " + property.getURI() + " (\"1\", \"true\", ...)",

				"MODIFY",
				String.format(Locale.ROOT, convertedFalsePropertyValueFormat, property.getURI(), property.getURI(), XPathFunctions.boolFunction, property.getURI()),
				"Convert " + XSD.xboolean.getLocalName() + " value for the property " + property.getURI() + " (\"0\", \"false\", ...)");
 	}
 }
