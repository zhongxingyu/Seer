 package edu.rpi.tw.mobilehealth;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.json.JSONArray;
 
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.rdf.model.Model;
 
 import edu.rpi.tw.escience.semanteco.Domain;
 import edu.rpi.tw.escience.semanteco.Module;
 import edu.rpi.tw.escience.semanteco.ModuleConfiguration;
 import edu.rpi.tw.escience.semanteco.ProvidesDomain;
 import edu.rpi.tw.escience.semanteco.QueryMethod;
 import edu.rpi.tw.escience.semanteco.Request;
 import edu.rpi.tw.escience.semanteco.SemantEcoUI;
 import edu.rpi.tw.escience.semanteco.query.Query;
import edu.rpi.tw.escience.semanteco.query.Query.SortType;
 import edu.rpi.tw.escience.semanteco.query.Query.Type;
 import edu.rpi.tw.escience.semanteco.query.QueryResource;
 import edu.rpi.tw.escience.semanteco.query.Variable;
 import edu.rpi.tw.mobilehealth.model.DataModelBuilder;
 import edu.rpi.tw.mobilehealth.model.Patient;
 import edu.rpi.tw.mobilehealth.util.HealthQueryResourceUtils;
 import edu.rpi.tw.mobilehealth.util.HealthQueryVarUtils;
 import static edu.rpi.tw.escience.semanteco.util.QueryResourceUtils.RDFS_NS;
 import static edu.rpi.tw.mobilehealth.util.HealthQueryResourceUtils.HEALTH_NS;
 
 public class PatientModule implements Module, ProvidesDomain {
 
     private ModuleConfiguration config = null;
 
     @Override
     public void visit(final Model model, final Request request,
             final Domain domain) {
         // TODO populate data model
         Object patient = request.getParam("patient");
         DataModelBuilder builder = new DataModelBuilder( config, request );
         if ( patient instanceof JSONArray ) {
             JSONArray arr = (JSONArray) patient;
             for ( int i = 0; i < arr.length(); i++ ) {
                 builder.loadPatientData(arr.optString(i), model);
             }
         } else if ( patient instanceof String ) {
             builder.loadPatientData((String) patient, model);
         }
     }
 
     @Override
     public void visit(final OntModel model, final Request request,
             final Domain domain) {
         // TODO populate ontology model
         model.read("http://mobilehealth.tw.rpi.edu/ontology/health.ttl", "TTL");
     }
 
     @Override
     public void visit(final Query query, final Request request) {
         // TODO modify queries
     }
 
     @Override
     public void visit(final SemantEcoUI ui, final Request request) {
         // TODO add resources to display
         DataModelBuilder builder = new DataModelBuilder(config, request);
         List<Patient> patients = builder.getPatients();
         StringBuilder sb = new StringBuilder("<div class=\"facet\">" +
                 "<select name=\"patient\">");
         for(Patient patient : patients) {
             sb.append("<option value=\"");
             sb.append(patient.getUri());
             sb.append("\">");
             sb.append(patient.getId());
             sb.append("</option>");
         }
         sb.append("</select></div>");
         ui.addFacet(config.generateStringResource(sb.toString()));
     }
 
     @Override
     public String getName() {
         return "Patient";
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
     public String listPatients(final Request request) {
         final Query query = config.getQueryFactory().newQuery(Type.SELECT);
         final HealthQueryVarUtils vars = new HealthQueryVarUtils(query);
         final QueryResource rdfsLabel = query.getResource(RDFS_NS + "label");
         Set<Variable> variables = new LinkedHashSet<Variable>();
         variables.add(vars.uri());
         variables.add(vars.patientId());
         query.addPattern(vars.uri(), rdfsLabel, vars.patientId());
         return config.getQueryExecutor(request).accept("application/json")
                 .execute(query);
     }
 
     @QueryMethod
     public String getPatientMeasurements(final Request request) {
         final Query query = config.getQueryFactory().newQuery(Type.SELECT);
         final HealthQueryVarUtils vars = new HealthQueryVarUtils(query);
         final HealthQueryResourceUtils res = new HealthQueryResourceUtils(query);
         query.setDistinct(true);
         query.addPattern(vars.uri(), res.rdfType(), res.healthPatient());
         query.addPattern(vars.uri(), res.healthHasSample(), vars.sample());
         query.addPattern(vars.sample(), res.healthHasMeasurement(), vars.measurement());
         query.addPattern(vars.measurement(), res.healthOfCharacteristic(), vars.characteristic());
         query.addPattern(vars.measurement(), res.healthHasValue(), vars.value());
         query.addPattern(vars.measurement(), res.healthHasUnit(), vars.unit());
         query.addPattern(vars.measurement(), res.dcDate(), vars.date());
         query.addPattern(vars.characteristic(), res.rdfsLabel(), vars.label());
        query.addOrderBy(vars.date(), SortType.ASC);
         return config.getQueryExecutor(request).accept("application/json")
                 .executeLocalQuery(query);
     }
 
     @Override
     public List<Domain> getDomains(final Request request) {
         List<Domain> domains = new ArrayList<Domain>();
         Domain health = config.getDomain(URI.create(HEALTH_NS), true);
         health.setLabel("Health");
         health.addSource(URI.create("http://mobilehealth.tw.rpi.edu/source/amc-edu"), "Albany Medical");
         health.addRegulation(URI.create("http://mobilehealth.tw.rpi.edu/ontology/ref-range-female.ttl#"), "Blood Ref. Range (Female)");
         health.addDataType("bloodwork", "Bloodwork", config.getResource("128px-Blood_drop.svg.png"));
         domains.add(health);
         return domains;
     }
 
 }
