 /**
  * 
  */
 package de.aidger.model.controlling;
 
import java.util.List;
 import java.util.ArrayList;
 
 import de.aidger.model.models.Assistant;
 import de.aidger.model.models.Employment;
 import de.aidger.utils.reports.BalanceHelper;
 import de.aidger.view.UI;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.IAssistant;
 
 /**
  * This class is used to get all controlling assistant objects for a given year,
  * month and fund.
  * 
  * @author aidGer Team
  */
 public class ControllingCreator {
 
     /**
      * The year of this controlling report.
      */
     private final int year;
 
     /**
      * The month of this controlling report.
      */
     private final int month;
 
     /**
      * The funds of this controlling report.
      */
     private final int funds;
 
     /**
      * Initializes a new controlling creator and sets the year, month and funds.
      * 
      * @param year
      *            The year of this controlling report.
      * @param month
      *            The month of this controlling report.
      * @param funds
      *            The funds of this controlling report.
      */
     public ControllingCreator(int year, int month, int funds) {
         this.year = year;
         this.month = month;
         this.funds = funds;
     }
 
     /**
      * Calculates all the assistants of this controlling report, that match the
      * criteria, as well as their costs.
      * 
      * @return The assistant objects of this controlling report.
      */
     public ArrayList<ControllingAssistant> getAssistants() {
         ArrayList<ControllingAssistant> controllingAssistants = new ArrayList<ControllingAssistant>();
         try {
             List<IAssistant> assistants = new Assistant().getAll();
             // Get all the employments that fit the time span.
             List<Employment> employments = new Employment().getEmployments(
                 (short) year, (byte) 1, (short) year, (byte) month);
             for (IAssistant assistant : assistants) {
                 /*
                  * There's no need to add the assistant, if there are no fitting
                  * employments for it.
                  */
                 ControllingAssistant controllingAssistant = null;
                 for (Employment employment : employments) {
                     if (assistant.getId().equals(employment.getAssistantId())
                             && employment.getFunds().equals(funds)) {
                         if (controllingAssistant == null) {
                             controllingAssistant = new ControllingAssistant();
                             controllingAssistant.setName(assistant
                                 .getFirstName()
                                     + " " + assistant.getLastName());
                         }
                         new BalanceHelper();
                         // Costs are needed pre-tax for this.
                         double costs = BalanceHelper
                             .calculatePreTaxBudgetCost(employment);
                         controllingAssistant.setCosts(controllingAssistant
                             .getCosts()
                                 + costs);
                     }
                 }
                 if (controllingAssistant != null) {
                     controllingAssistants.add(controllingAssistant);
                 }
             }
         } catch (AdoHiveException e) {
             UI.displayError(e.toString());
         }
         return controllingAssistants;
     }
 }
