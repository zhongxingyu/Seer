 package edu.mit.cci.amtprojects.solver;
 
 import edu.mit.cci.amtprojects.DbProvider;
 import edu.mit.cci.amtprojects.GenericTask;
 import edu.mit.cci.amtprojects.HomePage;
 import edu.mit.cci.amtprojects.util.CayenneUtils;
 import org.apache.cayenne.DataObjectUtils;
 import org.apache.cayenne.PersistenceState;
 import org.apache.log4j.Logger;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.RestartResponseException;
 import org.apache.wicket.ajax.AjaxEventBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.json.JSONException;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.basic.MultiLineLabel;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.Check;
 import org.apache.wicket.markup.html.form.CheckGroup;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.HiddenField;
 import org.apache.wicket.markup.html.form.Radio;
 import org.apache.wicket.markup.html.form.RadioGroup;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.panel.Fragment;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.markup.repeater.data.ListDataProvider;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 
 /**
  * User: jintrone
  * Date: 10/15/12
  * Time: 2:53 PM
  */
 public class SolverGenerationTask extends GenericTask {
 
 
     public static enum Mode {
         CHOOSE, GENERATE, COMBINE, IMPROVE
     }
 
     private Mode mode = Mode.CHOOSE;
 
 
     private SolverTaskModel model;
 
     private List<Solution> solutions;
 
 
 
     private static Logger log = Logger.getLogger(SolverGenerationTask.class);
 
     public String getPhase() {
         return mode.name();
     }
 
     public SolverGenerationTask(PageParameters param) {
         super(param, true, true);
         try {
             model = new SolverTaskModel(batch());
         } catch (JSONException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             throw new RestartResponseException(HomePage.class);
         }
 
         solutions = new ArrayList<Solution>(model.getCurrentStatus().getCurrentAnswers());
         for (Iterator<Solution> sit = solutions.iterator(); sit.hasNext(); ) {
 
             Solution s = sit.next();
             log.info("Got solution: "+s.getText());
             if (s.getRound() == model.getCurrentStatus().getCurrentRound()) {
                 log.info("Removing");
                 sit.remove();
             }
         }
 
         add(new Label("question", model.getQuestion().getText()));
 
 
         Form<?> form = getForm();
 
 
         form.add(new AttributeModifier("action", batch().getIsReal() ? "https://www.mturk.com/mturk/externalSubmit" : "http://workersandbox.mturk.com/mturk/externalSubmit"));
         form.add(new AttributeModifier("method", "POST"));
         form.setOutputMarkupId(true);
 
 
         form.add(new ImproveFragment("improveId", "improveMarkup", this));
         form.add(new ChooseFragment("chooseId", "chooseMarkup", this));
         form.add(new CombineFragment("combineId", "combineMarkup", this));
         form.add(new GenerateFragment("generateId", "generateMarkup", this));
 
         form.add(new HiddenField<String>("phase", new Model<String>(model.getCurrentStatus().getPhase().name())));
         form.add(new HiddenField<Integer>("round", new Model<Integer>(model.getCurrentStatus().getCurrentRound())));
 
         add(new Label("maxCreateBonus", String.format("$%.2f", model.getMaxGeneratingBonus())));
         add(new Label("maxCombiningBonus", String.format("$%.2f", model.getMaxCombiningBonus())));
 
 
     }
 
     private class ChooseFragment extends Fragment {
 
         public ChooseFragment(String id, String markupId, MarkupContainer markupProvider) {
             super(id, markupId, markupProvider);
 
             DataView<Solution> dataView = new DataView<Solution>("answers", new ListDataProvider<Solution>(solutions)) {
                 @Override
                 protected void populateItem(Item<Solution> solutionItem) {
                     solutionItem.add(new Label("index",""+(solutionItem.getIndex()+1)));
                     final Solution sol = solutionItem.getModelObject();
                     Solution sdata = sol;
                     if (sdata.getPersistenceState() == PersistenceState.HOLLOW) {
                         sdata = (Solution) DataObjectUtils.objectForPK(DbProvider.getContext(), sol.getObjectId());
                     }
 
 
                     solutionItem.add(new MultiLineLabel("text", sdata.getText()));
 
                 }
 
 
             };
 
             add(dataView);
 
             AjaxLink<?> create = new AjaxLink<Object>("generate") {
                 @Override
                 public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                     SolverGenerationTask.this.mode = Mode.GENERATE;
                     ajaxRequestTarget.add(getForm());
                 }
             };
 
 
             AjaxLink<?> improve = new AjaxLink<Object>("improve") {
                 @Override
                 public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                     SolverGenerationTask.this.mode = Mode.IMPROVE;
                     ajaxRequestTarget.add(getForm());
                 }
             };
 
             AjaxLink<?> combine = new AjaxLink<Object>("combine") {
                 @Override
                 public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                     SolverGenerationTask.this.mode = Mode.COMBINE;
                     ajaxRequestTarget.add(getForm());
                 }
             };
 
             create.add(new Label("maxBonus", String.format("$%.2f", model.getMaxGeneratingBonus())));
             improve.add(new Label("maxBonus", String.format("$%.2f", model.getMaxImprovingBonus())));
             combine.add(new Label("maxBonus", String.format("$%.2f", model.getMaxCombiningBonus())));
 
             add(create, improve, combine);
 
         }
 
         public boolean isVisible() {
             return mode == Mode.CHOOSE;
         }
     }
 
     private class ImproveFragment extends Fragment {
 
         TextArea<String> improvementText;
 
         Button submit;
 
         RadioGroup<Solution> group;
 
         public ImproveFragment(String id, String markupId, MarkupContainer markupProvider) {
             super(id, markupId, markupProvider);
 
             AjaxLink<?> choose = new AjaxLink<Object>("choose") {
 
                 @Override
                 public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                     SolverGenerationTask.this.mode = Mode.CHOOSE;
                     group.setModelObject(null);
                     improvementText.setModelObject("");
                     ajaxRequestTarget.add(getForm());
                 }
             };
 
 
             Serializable textModel = new Serializable() {
 
                 String text;
 
                 public void setText(String text) {
                     this.text = text;
                 }
 
                 public String getText() {
                     return text;
                 }
             };
 
             add(improvementText = new TextArea<String>("improvementText", new PropertyModel<String>(textModel, "text")) {
                 public boolean isEnabled() {
                     return group.getModelObject() != null;
                 }
             });
             improvementText.add(new AttributeModifier("class", new Model<String>() {
                 public String getObject() {
                     return group.getModelObject() != null ? "done" : "notdone";
                 }
             }));
            add(new Label("maxImprovingBonus", String.format("$%.2f", model.getMaxImprovingBonus())));
             improvementText.add(new AttributeModifier("name", "solutiontext"));
 
 
             Serializable radiomodel = new Serializable() {
 
                 Solution sol;
 
                 public void setSolution(Solution sol) {
                     this.sol = sol;
                 }
 
                 public Solution getSolution() {
                     return sol;
                 }
             };
             group = new RadioGroup<Solution>("progenitor", new PropertyModel<Solution>(radiomodel, "solution"));
 
             DataView<Solution> dataView = new DataView<Solution>("answers", new ListDataProvider<Solution>(solutions)) {
                 @Override
                 protected void populateItem(Item<Solution> solutionItem) {
                     final Solution sol = solutionItem.getModelObject();
                     Solution sdata = sol;
                     if (sdata.getPersistenceState() == PersistenceState.HOLLOW) {
                         sdata = (Solution) DataObjectUtils.objectForPK(DbProvider.getContext(), sol.getObjectId());
                     }
 
 
                     solutionItem.add(new Radio<Solution>("radio", solutionItem.getModel(), group).add(new AjaxEventBehavior("change") {
 
                         @Override
                         protected void onEvent(AjaxRequestTarget target) {
                             log.info("Got event");
 
                             if (group.getModelObject() == null || !group.getModelObject().equals(sol)) {
                                 log.info("Trying to set text");
                                 Solution s = CayenneUtils.inflate(DbProvider.getContext(),sol);
                                 group.setModelObject(sol);
                                 improvementText.setModelObject(s.getText());
 
                             }
 
                             target.add(getForm());
                         }
                     }).add(new AttributeModifier("name", "parents")).add(new AttributeModifier("value", sol.getId() + "")));
                     solutionItem.add(new MultiLineLabel("text", sdata.getText()));
 
                 }
 
 
             };
 
             group.add(dataView);
 
             add(group);
 
 
             add(choose);
 
             add(submit = new Button("Submit") {
 
                 public boolean isVisible() {
                     return !isPreview();
                 }
 
                 public boolean isEnabled() {
                     return group.getModelObject() != null;
                 }
 
 
             });
             submit.setOutputMarkupId(true);
 
 
         }
 
         public boolean isVisible() {
             return mode == Mode.IMPROVE;
         }
     }
 
     private class CombineFragment extends Fragment {
 
         TextArea<String> improvementText;
 
         Button submit;
 
         CheckGroup<Solution> group;
 
         public CombineFragment(String id, String markupId, MarkupContainer markupProvider) {
             super(id, markupId, markupProvider);
 
             group = new CheckGroup<Solution>("progenitors", new HashSet<Solution>());
 
             DataView<Solution> dataView = new DataView<Solution>("answers", new ListDataProvider<Solution>(solutions)) {
                 @Override
                 protected void populateItem(Item<Solution> solutionItem) {
                     final Solution sol = solutionItem.getModelObject();
                     Solution sdata = sol;
                     if (sdata.getPersistenceState() == PersistenceState.HOLLOW) {
                         sdata = (Solution) DataObjectUtils.objectForPK(DbProvider.getContext(), sol.getObjectId());
                     }
 
 
                     solutionItem.add(new Check<Solution>("check", solutionItem.getModel(), group).add(new AjaxEventBehavior("change") {
 
                         @Override
                         protected void onEvent(AjaxRequestTarget target) {
                             log.info("Got event");
 
                             if (!group.getModelObject().contains(sol)) {
                                 group.getModelObject().add(sol);
 
 
                             } else {
                                 group.getModelObject().remove(sol);
                             }
                             String txt = "";
 
                             for (Solution s : group.getModelObject()) {
                                 s = CayenneUtils.inflate(DbProvider.getContext(),s);
                                 txt += (!txt.isEmpty() ? " " : "") + s.getText();
                             }
                             improvementText.setModelObject(txt);
 
                             target.add(getForm());
                         }
                     }).add(new AttributeModifier("name", "parents")).add(new AttributeModifier("value", sol.getId() + "")));
                     solutionItem.add(new MultiLineLabel("text", sdata.getText()));
 
                 }
 
 
             };
 
             group.add(dataView);
             add(group);
 
             AjaxLink<?> choose = new AjaxLink<Object>("choose") {
 
                 @Override
                 public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                     SolverGenerationTask.this.mode = Mode.CHOOSE;
                     group.getModelObject().clear();
                     improvementText.setModelObject("");
                     ajaxRequestTarget.add(getForm());
                 }
             };
 
 
 
 
 
 
             add(choose);
 
             Serializable textModel = new Serializable() {
 
                 String text;
 
                 public void setText(String text) {
                     this.text = text;
                 }
 
                 public String getText() {
                     return text;
                 }
             };
             add(improvementText = new TextArea<String>("improvementText", new PropertyModel<String>(textModel, "text")) {
                 public boolean isEnabled() {
                     return group.getModelObject().size() > 1;
                 }
             });
             improvementText.add(new AttributeModifier("class", new Model<String>() {
                 public String getObject() {
                     return group.getModelObject().size() > 1 ? "done" : "notdone";
                 }
             }));
             add(new Label("maxCombiningBonus", String.format("$%.2f", model.getMaxCombiningBonus())));
             improvementText.add(new AttributeModifier("name", "solutiontext"));
 
             add(submit = new Button("Submit") {
 
                 public boolean isVisible() {
                     return !isPreview();
                 }
 
                 public boolean isEnabled() {
                     return group.getModelObject().size() > 0;
                 }
 
 
             });
             submit.setOutputMarkupId(true);
 
 
         }
 
         public boolean isVisible() {
             return mode == Mode.COMBINE;
         }
     }
 
 
     private class GenerateFragment extends Fragment {
 
 
         public GenerateFragment(String id, String markupId, MarkupContainer markupProvider) {
             super(id, markupId, markupProvider);
             add(new Label("maxBonus", String.format("$%.2f", model.getMaxGeneratingBonus())));
             final TextArea<String> ta = new TextArea<String>("generateText");
 
             AjaxLink<?> choose = new AjaxLink<Object>("choose") {
 
                 @Override
                 public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                     SolverGenerationTask.this.mode = Mode.CHOOSE;
                     if (ta.getModel() !=null) ta.setModelObject("");
                     ajaxRequestTarget.add(getForm());
                 }
             };
 
             add(choose);
 
 
 
             add(ta.add(new AttributeModifier("name", "solutiontext")));
 
             add(new Button("Submit") {
                 public boolean isVisible() {
                     return !isPreview();
                 }
             });
 
 
         }
 
         public boolean isVisible() {
             return mode == Mode.GENERATE;
         }
     }
 }
