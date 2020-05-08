 package edu.mit.cci.amtprojects.solver;
 
 import edu.mit.cci.amtprojects.GenericTask;
 import edu.mit.cci.amtprojects.HomePage;
 import edu.mit.cci.amtprojects.util.Utils;
 import org.apache.log4j.Logger;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.RestartResponseException;
 import org.apache.wicket.ajax.AjaxEventBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.json.JSONException;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.basic.MultiLineLabel;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.Check;
 import org.apache.wicket.markup.html.form.CheckGroup;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.HiddenField;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.panel.Fragment;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.markup.repeater.data.ListDataProvider;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 
 
 /**
  * User: jintrone
  * Date: 10/15/12
  * Time: 2:53 PM
  */
 public class SolverGenerationTask extends GenericTask {
 
 
     public static enum Mode {
         CHOOSE, GENERATE, IMPROVE
     }
 
     private Mode mode = Mode.CHOOSE;
 
     CheckGroup<Solution> group;
 
     private SolverTaskModel model;
 
     private static Logger log = Logger.getLogger(SolverGenerationTask.class);
 
     public String getPhase() {
         return mode.name();
     }
 
     public SolverGenerationTask(PageParameters param) {
         super(param,true,true);
         try {
             model = new SolverTaskModel(batch());
         } catch (JSONException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             throw new RestartResponseException(HomePage.class);
         }
         add(new Label("question", model.getQuestion().getText()));
 
         List<Solution> sols = new ArrayList<Solution>(model.getCurrentStatus().getCurrentAnswers());
         for (Iterator<Solution> sit = sols.iterator();sit.hasNext();) {
             Solution s =  sit.next();
             if (s.getRound() == model.getCurrentStatus().getCurrentRound()) {
                 sit.remove();
             }
         }
 
 
         group = new CheckGroup<Solution>("progenitors", new HashSet<Solution>()) {
 
             public boolean isEnabled() {
                 return mode == Mode.IMPROVE;
             }
 
 
         };
 
         final ImproveFragment improveFragment = new ImproveFragment("improveId", "improveMarkup", this);
         Form<?> form = getForm();
         form.add(improveFragment);
 
 
         DataView<Solution> dataView = new DataView<Solution>("answers", new ListDataProvider<Solution>(sols)) {
             @Override
             protected void populateItem(Item<Solution> solutionItem) {
                 final Solution sol = solutionItem.getModelObject();
 
                 solutionItem.add(new Check<Solution>("check", solutionItem.getModel(), group).add(new AjaxEventBehavior("change") {
 
                     @Override
                     protected void onEvent(AjaxRequestTarget target) {
                         log.info("Got event");
 
                         if (!group.getModelObject().contains(sol)) {
                             group.getModelObject().add(sol);
 
                         } else {
                             group.getModelObject().remove(sol);
                         }
 
                         target.add(getForm());
                     }
                 }).add(new AttributeModifier("name", "parents")).add(new AttributeModifier("value", sol.getId() + "")));
                 solutionItem.add(new MultiLineLabel("text", sol.getText()));
 
                 solutionItem.add(new Label("score", sol.getToRanks().size() ==0?"<none>":String.format("%.2f", Utils.last(sol.getToRanks()).getRankValue())));
 
             }
 
 
         };
 
         group.add(dataView);
 
 
         form.add(new AttributeModifier("action", batch().getIsReal() ? "https://www.mturk.com/mturk/externalSubmit" : "http://workersandbox.mturk.com/mturk/externalSubmit"));
         form.add(new AttributeModifier("method", "POST"));
         form.setOutputMarkupId(true);
 
         form.add(group);
 
        form.add(new ChooseFragment("chooseId", "chooseMarkup", this));
 
         form.add(new GenerateFragment("generateId", "generateMarkup", this));
         form.add(new HiddenField<String>("phase", new Model<String>(model.getCurrentStatus().getPhase().name())));
         form.add(new HiddenField<Integer>("round", new Model<Integer>(model.getCurrentStatus().getCurrentRound())));
 
 
 
     }
 
     private class ChooseFragment extends Fragment {
 
         public ChooseFragment(String id, String markupId, MarkupContainer markupProvider) {
             super(id, markupId, markupProvider);
 
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
 
             add(new Label("maxGenerateBonus", String.format("$%.2f", model.getMaxGeneratingBonus())));
             add(new Label("maxCombiningBonus", String.format("$%.2f", model.getMaxCombiningBonus())));
 
             add(create, improve);
 
         }
 
         public boolean isVisible() {
             return mode == Mode.CHOOSE;
         }
     }
 
     private class ImproveFragment extends Fragment {
 
         TextArea<String> improvementText;
 
         Button submit;
 
         public ImproveFragment(String id, String markupId, MarkupContainer markupProvider) {
             super(id, markupId, markupProvider);
 
             AjaxLink<?> choose = new AjaxLink<Object>("choose") {
 
                 @Override
                 public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                     SolverGenerationTask.this.mode = Mode.CHOOSE;
                     group.getModelObject().clear();
                     ajaxRequestTarget.add(getForm());
                 }
             };
 
             Label instructionsStep1 = new Label("step1","Select some answers to improve or combine. Please improve or combine the ideas in the selected answers. It is not necessary to use the same wording.");
             instructionsStep1.add(new AttributeModifier("class", new Model<String>() {
                 public String getObject() {
                     return group.getModelObject().size() > 0 ? "done" : "notdone";
                 }
             }));
 
 
             add(instructionsStep1);
             Label instructionsStep2 = new Label("step2","Add your answer below");
             instructionsStep2.add(new AttributeModifier("class", new Model<String>() {
                 public String getObject() {
                     return group.getModelObject().size() > 0 ? "enabled" : "disabled";
                 }
             }));
 
 
             add(instructionsStep2);
             add(choose);
             add(improvementText = new TextArea<String>("improvementText") {
                 public boolean isEnabled() {
                     return group.getModelObject().size() > 0;
                 }
             });
             improvementText.add(new AttributeModifier("class",new Model<String>() {
                 public String getObject() {
                     return  group.getModelObject().size() > 0?"done":"notdone";
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
             return mode == Mode.IMPROVE;
         }
     }
 
     private class GenerateFragment extends Fragment {
 
 
 
         public GenerateFragment(String id, String markupId, MarkupContainer markupProvider) {
             super(id, markupId, markupProvider);
             AjaxLink<?> choose = new AjaxLink<Object>("choose") {
 
                 @Override
                 public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                     SolverGenerationTask.this.mode = Mode.CHOOSE;
                     ajaxRequestTarget.add(getForm());
                 }
             };
 
             add(choose);
             add(new Label("maxBonus", String.format("$%.2f", model.getMaxGeneratingBonus())));
              TextArea<String> ta = new TextArea<String>("generateText");
 
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
