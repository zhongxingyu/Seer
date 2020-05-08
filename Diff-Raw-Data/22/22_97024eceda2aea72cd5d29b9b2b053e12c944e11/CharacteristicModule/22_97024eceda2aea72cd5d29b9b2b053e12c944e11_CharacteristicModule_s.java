 package edu.rpi.tw.mobilehealth;
 
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.rdf.model.Model;
 
 import edu.rpi.tw.escience.semanteco.Domain;
 import edu.rpi.tw.escience.semanteco.Module;
 import edu.rpi.tw.escience.semanteco.ModuleConfiguration;
 import edu.rpi.tw.escience.semanteco.QueryMethod;
 import edu.rpi.tw.escience.semanteco.Request;
 import edu.rpi.tw.escience.semanteco.SemantEcoUI;
 import edu.rpi.tw.escience.semanteco.query.GraphComponentCollection;
 import edu.rpi.tw.escience.semanteco.query.Query;
 import edu.rpi.tw.escience.semanteco.query.QueryResource;
 import edu.rpi.tw.escience.semanteco.query.Query.Type;
 import edu.rpi.tw.escience.semanteco.query.Variable;
 import edu.rpi.tw.mobilehealth.util.HealthQueryVarUtils;
 
 public class CharacteristicModule implements Module {
 
     private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
     private static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
     private static final String HEALTH_NS = "http://mobilehealth.tw.rpi.edu/ontology/health.ttl#";
     private static final Logger LOG = Logger.getLogger(CharacteristicModule.class);
     private ModuleConfiguration config = null;
 
     @Override
     public void visit(final Model model, final Request request,
             final Domain domain) {
         // TODO populate data model
     }
 
     @Override
     public void visit(final OntModel model, final Request request,
             final Domain domain) {
         // TODO populate ontology model
     }
 
     @Override
     public void visit(final Query query, final Request request) {
         // TODO modify queries
         JSONArray characteristicUri = (JSONArray)request
                 .getParam("characteristic");
         if ( characteristicUri == null ) {
             return;
         }
         if ( characteristicUri.length() == 0 ) {
             return;
         }
         final HealthQueryVarUtils vars = new HealthQueryVarUtils(query);
        final QueryResource rdfType = query.getResource(RDF_NS + "type");
        final QueryResource bloodMeasurement =
                query.getResource(HEALTH_NS + "BloodMeasurement");
         final QueryResource ofCharacteristic =
                 query.getResource(HEALTH_NS + "ofCharacteristic");
         final List<GraphComponentCollection> graphs =
                 query.findGraphComponentsWithPattern(vars.measurement(),
                        rdfType, bloodMeasurement);
         if ( graphs.isEmpty() ) {
             return;
         }
         for(GraphComponentCollection coll : graphs) {
             for(int i=0; i<characteristicUri.length(); i++) {
                 final QueryResource characteristic =
                         query.getResource(characteristicUri.optString(i));
                 coll.addPattern(vars.measurement(), ofCharacteristic,
                         characteristic);
             }
         }
     }
 
     @Override
     public void visit(final SemantEcoUI ui, final Request request) {
         StringBuffer result = new StringBuffer(
                 "<div class=\"facet\" style=\"width: inherit; height: 200px; overflow: auto;\"><ul name=\"characteristic\">");
         JSONObject object = null;
         try {
             object = new JSONObject(listCharacteristics(request));
         } catch (JSONException e) {
             LOG.error("Unable to generate UI for the characteristic module.",
                     e);
             return;
         }
         JSONArray bindings = object.optJSONObject("results")
                 .optJSONArray("bindings");
 		// Katie TODO: - make these into a UL
 		// 			   - that is also draggable
         for (int i = 0; i < bindings.length(); i++) {
             JSONObject binding = bindings.optJSONObject(i);
             JSONObject var = binding.optJSONObject("uri");
             String uri = var.optString("value");
             var = binding.optJSONObject("label");
             String label = var.optString("value");
             result.append("<li class=\"draggable\" href=\"");
             result.append(uri);
             result.append("\">");
             result.append(label);
             result.append("</li>");
         }
         result.append("</ul></div>");
         ui.addFacet(config.generateStringResource(result.toString()));
     }
 
     @Override
     public String getName() {
         return "Characteristic";
     }
 
     @Override
     public int getMajorVersion() {
         return 1;
     }
 
     @Override
     public int getMinorVersion() {
         return 0;
     }
 
     @Override
     public String getExtraVersion() {
         return null;
     }
 
     @Override
     public void setModuleConfiguration(final ModuleConfiguration config) {
         this.config = config;
     }
 
     @QueryMethod
     public String listPatientTests(final Request request) {
         final String patientUri = (String) request.getParam("patient");
         final Query query = config.getQueryFactory().newQuery(Type.SELECT);
         final HealthQueryVarUtils vars = new HealthQueryVarUtils(query);
         query.setNamespace("health", HEALTH_NS);
         Set<Variable> variables = new LinkedHashSet<Variable>();
         if (patientUri == null) {
             variables.add(vars.uri());
         }
         variables.add(vars.characteristic());
         final QueryResource patient = patientUri == null ? vars.uri() : query
                 .getResource(patientUri);
         final QueryResource sampleMeasurementCharacteristic = query
                 .createPropertyPath("health:hasSample/health:hasMeasurement/health:ofCharacteristic");
         final QueryResource rdfsLabel = query.getResource(RDFS_NS + "label");
         query.addPattern(patient, sampleMeasurementCharacteristic,
                 vars.characteristic());
         query.addPattern(vars.characteristic(), rdfsLabel, vars.label());
         return config.getQueryExecutor(request).accept("application/json")
                 .execute(query);
     }
 
     @QueryMethod
     public String listCharacteristics(final Request request) {
         final Query query = config.getQueryFactory().newQuery(Type.SELECT);
         final QueryResource rdfType = query.getResource(RDF_NS + "type");
         final QueryResource healthBloodCharacteristic = query
                 .getResource(HEALTH_NS + "BloodCharacteristic");
         final QueryResource rdfsLabel = query.getResource(RDFS_NS + "label");
         final HealthQueryVarUtils vars = new HealthQueryVarUtils(query);
         Set<Variable> variables = new LinkedHashSet<Variable>();
         variables.add(vars.uri());
         variables.add(vars.label());
         query.setVariables(variables);
         query.addPattern(vars.uri(), rdfType,
                 healthBloodCharacteristic);
         query.addPattern(vars.uri(), rdfsLabel, vars.label());
         return config.getQueryExecutor(request).accept("application/json")
                 .execute(query);
     }
 
 }
