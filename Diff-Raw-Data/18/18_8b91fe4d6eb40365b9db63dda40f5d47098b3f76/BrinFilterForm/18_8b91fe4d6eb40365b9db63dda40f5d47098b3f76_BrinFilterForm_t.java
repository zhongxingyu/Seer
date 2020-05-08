 package net.codjo.tools.pyp.pages;
 import java.util.ArrayList;
 import java.util.List;
 import net.codjo.tools.pyp.model.filter.BrinFilter;
 import net.codjo.tools.pyp.model.filter.BrinFilterEnum;
 import org.apache.log4j.Logger;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.model.Model;
 import org.joda.time.DateTime;
 /**
  * TODO[codjo-web] - see LanguagePanel in Magic
  */
 public class BrinFilterForm extends Form {
     private static final Logger LOG = Logger.getLogger(BrinFilterForm.class);
 
 
     public BrinFilterForm(String id, BrinFilter filter, final CallBack<BrinFilter> callBack) {
         super(id);
         ChoiceRenderer<BrinFilter> choiceRenderer = new ChoiceRenderer<BrinFilter>("displayLabel", "brinId") {
             @Override
             public Object getDisplayValue(BrinFilter object) {
                 return object.getDisplayLabel();
             }
         };
         DateTime now = new DateTime();
         List<BrinFilter> brinFilters = new ArrayList<BrinFilter>();
         brinFilters.add(BrinFilterEnum.LAST_WEEK.get(now));
         brinFilters.add(BrinFilterEnum.CURRENT_MONTH.get(now));
         brinFilters.add(BrinFilterEnum.SLIDING_MONTH.get(now));
         brinFilters.add(BrinFilterEnum.CURRENT_YEAR.get(now));
         brinFilters.add(BrinFilterEnum.ALL_BRIN.get(now));
 
        if (filter == null) {
            filter = BrinFilterEnum.ALL_BRIN.get(now);
        }

         DropDownChoice choice = new DropDownChoice<BrinFilter>("brinFilters",
                                                                new Model<BrinFilter>(filter),
                                                                brinFilters,
                                                                choiceRenderer);
         choice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
                 BrinFilter brinFilter = (BrinFilter)getFormComponent().getModelObject();
                 callBack.onClickCallBack(brinFilter);
                 addComponentToTarget("brinListContainer", target);
                 addComponentToTarget("leftPanel:summaryPanel", target);
             }
         });
         add(choice);
     }
 
 
     private void addComponentToTarget(String componentId, AjaxRequestTarget target) {
         Component component = getPage().get(componentId);
         if (component != null) {
             if (component.getOutputMarkupId()) {
                 target.addComponent(component);
             }
             else {
                 LOG.warn("Component " + componentId + " outputMarkup is set to false: it has not been refreshed");
             }
         }
     }
 }
