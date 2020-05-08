 package de.puzzles.webapp.page.newrequest.steps;
 
 import de.puzzles.core.domain.CreditRequest;
 import de.puzzles.core.domain.RepaymentPlan;
 import de.puzzles.core.domain.Transaction;
 import de.puzzles.webapp.page.newrequest.RepaymentPlanPanel;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.extensions.wizard.WizardStep;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 
 import java.util.List;
 
 /**
  * @author Patrick Groß-Holtwick
  *         Date: 07.03.13
  */
 public class OverviewStep extends WizardStep {
 
     private CompoundPropertyModel<CreditRequest> requestModel;
     private CompoundPropertyModel<List<Transaction>> incomeModel;
     private CompoundPropertyModel<List<Transaction>> spendingsModel;
     private TextField<Double> rateField;
    private TextField<Double> durationField;
     private RepaymentPlanPanel repaymentPlan;
     private Label rateLabel;
     private Label totalLabel;
 
     public OverviewStep(IModel<CreditRequest> model, IModel<List<Transaction>> incomes, IModel<List<Transaction>> spendings) {
         super();
         requestModel = new CompoundPropertyModel<CreditRequest>(model);
         incomeModel = new CompoundPropertyModel<List<Transaction>>(incomes);
         spendingsModel = new CompoundPropertyModel<List<Transaction>>(spendings);
 
         Label interestLabel = new Label("interest", new AbstractReadOnlyModel<String>() {
             @Override
             public String getObject() {
                 return Math.round(requestModel.getObject().getRepaymentPlan().getInterest() * 10000) / 100 + " %";
             }
         });
         add(interestLabel);
         TextField<Double> amount = new TextField<Double>("creditAmount", requestModel.<Double>bind("repaymentPlan.amount"));
         amount.add(new AjaxFormComponentUpdatingBehavior("onchange") {
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
                 updateFields(target);
             }
         });
         amount.setRequired(true);
         add(amount);
 
        durationField = new TextField<Double>("creditDuration", requestModel.<Double>bind("repaymentPlan.duration"));
        durationField.setRequired(true);
         durationField.setOutputMarkupId(true);
        durationField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                updateFields(target);
            }
        });
         add(durationField);
 
         rateField = new TextField<Double>("creditRate", new AbstractReadOnlyModel<Double>() {
             @Override
             public Double getObject() {
                 return requestModel.getObject().getRepaymentPlan().getRate();
             }
         });
         rateField.setOutputMarkupId(true);
         rateField.setEnabled(false);
         add(rateField);
 
         final IModel<Double> incomeTotal = new AbstractReadOnlyModel<Double>() {
             @Override
             public Double getObject() {
                 Double d = 0.0;
                 for (Transaction t : incomeModel.getObject()) {
                     d += t.getValue();
                 }
                 return d;
             }
         };
         final IModel<Double> spendingTotal = new
             AbstractReadOnlyModel<Double>() {
                 @Override
                 public Double getObject() {
                     Double d = 0.0;
                     for (Transaction t : spendingsModel.getObject()) {
                         d += t.getValue();
                     }
                     return d;
                 }
             };
         add(new Label("incomeTotal", incomeTotal));
         add(new Label("spendingTotal", spendingTotal));
         rateLabel = new Label("rateTotal", rateField.getModel());
         rateLabel.setOutputMarkupId(true);
         add(rateLabel);
 
         totalLabel = new Label("total", new AbstractReadOnlyModel<Double>() {
             @Override
             public Double getObject() {
                 return incomeTotal.getObject() - spendingTotal.getObject() - rateField.getModelObject();
             }
         });
         totalLabel.setOutputMarkupId(true);
         add(totalLabel);
 
         add(new Label("name", new AbstractReadOnlyModel<String>() {
             @Override
             public String getObject() {
                 return requestModel.getObject().getCustomer().getFirstname() + " " + requestModel.getObject().getCustomer().getLastname();
             }
         }));
         add(new Label("adress", new AbstractReadOnlyModel<String>() {
             @Override
             public String getObject() {
                 return requestModel.getObject().getCustomer().getStreet() + ", "+ requestModel.getObject().getCustomer().getZipcode() + " " + requestModel.getObject().getCustomer().getCity();
             }
         }));
         add(new Label("phone", requestModel.<String>bind("customer.telephone")));
         add(new Label("mail", requestModel.<String>bind("customer.email")));
 
         repaymentPlan = new RepaymentPlanPanel("repaymentPlan", new AbstractReadOnlyModel<List<RepaymentPlan.Entry>>() {
             @Override
             public List<RepaymentPlan.Entry> getObject() {
                 return requestModel.getObject().getRepaymentPlan().generateRepaymentPlan();
             }
         });
         repaymentPlan.setOutputMarkupId(true);
         add(repaymentPlan);
     }
 
     public void updateFields(AjaxRequestTarget target) {
         requestModel.getObject().getRepaymentPlan().setAmount(requestModel.getObject().getRepaymentPlan().getAmount());
         target.add(rateField);
         target.add(rateLabel);
         target.add(totalLabel);
         target.add(repaymentPlan);
     }
 }
