 package edu.mit.cci.amtprojects.solver;
 
 import edu.mit.cci.amtprojects.DbProvider;
 import edu.mit.cci.amtprojects.HomePage;
 import edu.mit.cci.amtprojects.kickball.cayenne.Batch;
 import edu.mit.cci.amtprojects.util.CayenneUtils;
 import org.apache.cayenne.DataObjectUtils;
 import org.apache.cayenne.PersistenceState;
 import org.apache.log4j.Logger;
 import org.apache.wicket.RestartResponseException;
 import org.apache.wicket.ajax.json.JSONArray;
 import org.apache.wicket.ajax.json.JSONException;
 import org.apache.wicket.ajax.json.JSONObject;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.basic.MultiLineLabel;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.markup.repeater.data.ListDataProvider;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.util.io.IClusterable;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * User: jintrone
  * Date: 1/18/13
  * Time: 8:26 AM
  */
 @AuthorizeInstantiation("ADMIN")
 public class SolverApprovalAdmin extends WebPage {
 
     SolverTaskModel model;
     Long batchId;
     Form<?> form;
 
     private static Logger logger = Logger.getLogger(SolverApprovalAdmin.class);
 
     public SolverApprovalAdmin(PageParameters params) {
         batchId = params.get("batch").toLong();
         if (batch() == null) {
             params.set("error", "No such batch");
             throw new RestartResponseException(HomePage.class, params);
         }
         try {
             model = new SolverTaskModel(batch());
         } catch (JSONException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             throw new RestartResponseException(HomePage.class);
         }
         form = new Form<Void>("approvalForm");
 
 
         ListDataProvider<Solution> sols = new ListDataProvider<Solution>() {
 
             protected List<Solution> data = null;
 
             protected List<Solution> getData() {
 
                 return CayenneUtils.findSolutions(DbProvider.getContext(), batchId, Solution.Valid.NEEDS_APPROVAL);
 
             }
 
 
         };
 
 
         DataView<Solution> dataView = new DataView<Solution>("answers", sols) {
 
             @Override
             protected void populateItem(Item<Solution> solutionItem) {
                 final Solution sol = solutionItem.getModelObject();
                 Solution sdata = sol;
                 if (sdata.getPersistenceState() == PersistenceState.HOLLOW) {
 
                     sdata = CayenneUtils.findSolution(DbProvider.getContext(),sol.getId());
                 }
                 String isBlank = "<error>";
                 String nonesense = "<error>";
                 List<Solution> copies = Collections.emptyList();
 
                 try {
                     JSONObject obj = new JSONObject(sdata.getMeta());
                    isBlank = obj.has("empty") ? obj.getBoolean("empty")+"" : "false";
                    nonesense = obj.has("nonsense") ? obj.getBoolean("nonsense")+"" : "false";
                     copies = extractSolutions(obj.getJSONArray("copyof"));
 
                 } catch (JSONException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     logger.warn("Could not get solution meta data");
                 }
                 solutionItem.add(new MultiLineLabel("solutionText", sdata.getText()));
                 solutionItem.add(new Label("empty", isBlank));
                 solutionItem.add(new Label("nonsense", nonesense));
                 solutionItem.add(new DataView<Solution>("copies", new ListDataProvider<Solution>(copies)) {
 
                     @Override
                     protected void populateItem(Item<Solution> solutionItem1) {
                         solutionItem1.add(new Label("copytext", solutionItem1.getModelObject().getText()));
                     }
                 });
 
 
                 solutionItem.add(new DropDownChoice("choice", new SolutionValidationModel(sdata), Arrays.asList("Choose one", "Accept", "Reject")));
 
             }
 
 
         };
 
         form.add(dataView);
         form.add(new Button("Submit"));
         add(form);
 
 
     }
 
     private List<Solution> extractSolutions(JSONArray copyof) {
         List<Solution> result = new ArrayList<Solution>();
         for (int i = 0; i < copyof.length(); i++) {
             Long l = 0l;
             try {
                 l = copyof.getLong(i);
             } catch (JSONException e) {
                 logger.warn("Couldn't extract long value");
                 continue;
             }
 
 
             result.add(CayenneUtils.findSolution(DbProvider.getContext(), l));
         }
         return result;
     }
 
     public SolverTaskModel getModel() {
         return model;
     }
 
     protected Batch batch() {
         return CayenneUtils.findBatch(DbProvider.getContext(), batchId);
     }
 
     public static class SolutionValidationModel implements IModel<String>, IClusterable {
 
         String value = "Choose one";
         Long sol;
 
         public SolutionValidationModel(Solution s) {
             this.sol = s.getId();
         }
 
         public void detach() {
             //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public String getObject() {
             return value;
         }
 
         public void setObject(String object) {
             if ("Reject".equals(object)) {
                 updateSolution(object);
             } else if ("Accept".equals(object)) {
                 updateSolution(object);
             }
         }
 
         private void updateSolution(String validity) {
             Solution s = CayenneUtils.findSolution(DbProvider.getContext(), sol);
             s.setValid(validity.equals("Accept") ? Solution.Valid.VALID : Solution.Valid.INVALID);
             DbProvider.getContext().commitChanges();
         }
 
 
     }
 }
