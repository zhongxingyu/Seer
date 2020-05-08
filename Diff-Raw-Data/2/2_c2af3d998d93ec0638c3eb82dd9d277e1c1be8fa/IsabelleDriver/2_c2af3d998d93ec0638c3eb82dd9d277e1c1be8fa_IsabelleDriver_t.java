 /*
  * File name: IsabelleDriver.java
  *    Author: Matej Urbas [matej.urbas@gmail.com]
  * 
  *  Copyright © 2012 Matej Urbas
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package diabelli.isabelle;
 
 import diabelli.components.DiabelliComponent;
 import diabelli.components.FormulaFormatsProvider;
 import diabelli.components.FormulaPresenter;
 import diabelli.components.GoalAcceptingReasoner;
 import diabelli.components.util.BareGoalProvidingReasoner;
 import diabelli.isabelle.pure.lib.TermYXML;
 import diabelli.isabelle.terms.*;
 import diabelli.logic.*;
 import isabelle.Term.Term;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyVetoException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.Timer;
 import org.isabelle.iapp.facade.CentralEventDispatcher;
 import org.isabelle.iapp.facade.IAPP;
 import org.isabelle.iapp.files.FileState;
 import org.isabelle.iapp.process.Message;
 import org.isabelle.iapp.process.ProverManager;
 import org.isabelle.iapp.process.features.InjectedCommands;
 import org.isabelle.iapp.process.features.InjectionContext;
 import org.isabelle.iapp.process.features.InjectionFinishListener;
 import org.isabelle.iapp.process.features.InjectionResult;
 import org.isabelle.iapp.process.features.InjectionResultListener;
 import org.isabelle.iapp.proofdocument.ProofDocument;
 import org.isabelle.iapp.proofdocument.StateChangeEvent;
 import org.isabelle.iapp.proofdocument.StateListener;
 import org.isabelle.resultdisplay.MarkedupTextDisplay;
 import org.isabelle.theoryeditor.TheoryEditor;
 import org.openide.util.Exceptions;
 import org.openide.util.NbBundle;
 import org.openide.util.lookup.ServiceProvider;
 import org.openide.windows.TopComponent;
 
 /**
  * This is the main class of the Isabelle driver for Diabelli. It provides
  * current Isabelle's goals to Diabelli and gives changed goals back to the
  * active Isabelle script.
  *
  * @author Matej Urbas [matej.urbas@gmail.com]
  */
 @ServiceProvider(service = DiabelliComponent.class)
 public class IsabelleDriver extends BareGoalProvidingReasoner implements
         GoalAcceptingReasoner,
         FormulaFormatsProvider,
         FormulaPresenter<StringFormula> {
 
     //<editor-fold defaultstate="collapsed" desc="Fields">
     private IsabelleMessageListener isabelleListener;
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Constructors">
     public IsabelleDriver() {
         isabelleListener = new IsabelleMessageListener();
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="GoalProvidingReasoner Interface Implementation">
     @Override
     public String getName() {
         return org.openide.util.NbBundle.getBundle(IsabelleDriver.class).getString("ISA_DRIVER_NAME");
     }
     //</editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Formula Format Provider Implementation">
     @Override
     public Collection<FormulaFormat<?>> getFormulaFormats() {
         return FormulaFormatsContainer.IsabelleFormats;
     }
     //</editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Formula Presenter Implementation">
     @Override
     public FormulaFormat<StringFormula> getPresentedFormat() {
         return StringFormat.getInstance();
     }
 
     @Override
     public Component createVisualiserFor(Goal goal) throws VisualisationException {
         if (goal == null) {
             return null;
         }
         return createVisualiserFor(goal.asFormula());
     }
 
     @Override
     public Component createVisualiserFor(Formula<?> formula) throws VisualisationException {
         if (formula == null) {
             return null;
         }
         for (FormulaFormat<?> formulaFormat : formula.getFormats()) {
             if (canPresent(formulaFormat)) {
                 return createVisualiserFor(formula.getRepresentation(formulaFormat));
             }
         }
         return null;
     }
 
     public boolean canPresent(FormulaFormat<?> format) {
         return StringFormat.getInstance() == format;
     }
 
     @Override
     public Component createVisualiserFor(FormulaRepresentation<?> formula) throws VisualisationException {
         if (canPresent(formula.getFormat()) && formula.getFormula() instanceof StringFormula) {
             StringFormula f = (StringFormula) formula.getFormula();
             if (f.getMarkedUpFormula() == null) {
                 return null;
             }
             MarkedupTextDisplay mtd = new MarkedupTextDisplay(true);
             mtd.addMessages(new Message[]{f.getMarkedUpFormula()});
             return mtd;
         } else {
             return null;
         }
     }
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="GoalAcceptingReasoner Implementation">
     /**
      * The Isabelle driver currently understands the following step results:
      *
      * <ul>
      *
      * <li>{@link GoalTransformationResult}.</li>
      *
      * </ul>
      *
      * @param step
      * @throws UnsupportedOperationException
      */
     @Override
     @NbBundle.Messages({
         "ID_multiple_goals_unsupported=The Isabelle driver does currently not support changes to multiple goals.",
         "ID_transformed_goal_unknown=The Isabelle driver canno commit the inference step since the transformed goal is in an unknown format."
     })
     public void commitTransformedGoals(InferenceStepResult step) throws UnsupportedOperationException {
         // Insert the changed goals if the committed inference step transformed them:
         if (step instanceof GoalTransformationResult) {
             GoalTransformationResult goalTransformationResult = (GoalTransformationResult) step;
             Goals originalGoals = goalTransformationResult.getOriginalGoals();
             if (originalGoals == null || originalGoals.isEmpty() || !(originalGoals.get(0) instanceof TermGoal))
                 return;
             // Get the proof script document into which we will insert the
             // Isabelle command:
             ProofDocument proofDocument = ((TermGoal)originalGoals.get(0)).getProofContext();
             if (proofDocument != null) {
                 // Get the transformed goals:
                 // TODO: Handle multiple goals:
                 if (goalTransformationResult.getGoalChangesCount() > 1) {
                     throw new UnsupportedOperationException(Bundle.ID_multiple_goals_unsupported());
                 }
                 // Find the only goal that was changed:
                 List<Goal> transformedGoals = null;
                 for (int i = 0; i < originalGoals.size(); i++) {
                     if (goalTransformationResult.isGoalChanged(i)) {
                         transformedGoals = goalTransformationResult.getTransformedGoalsFor(i);
                         break;
                     }
                 }
                 // Has the goal been discharged?
                 if (transformedGoals == null || transformedGoals.isEmpty()) {
                     throw new UnsupportedOperationException("Discharging of goals not yet supported by the Isabelle driver.");
                 } else {
                     // No, the goal been changed.
                     if (transformedGoals.size() > 1) {
                         throw new UnsupportedOperationException("The Isabelle driver does not yet support creation of multiple sub-goals from a single one.");
                     }
                     // Okay, now get the Isabelle string formula and submit it to
                     // the proof script:
                     Formula<?> goalFormula = transformedGoals.get(0).asFormula();
                     if (goalFormula == null) {
                         throw new RuntimeException(Bundle.ID_transformed_goal_unknown());
                     }
                     ArrayList<? extends FormulaRepresentation<StringFormula>> isabelleStringRepresentation = goalFormula.fetchRepresentations(StringFormat.getInstance());
                     if (isabelleStringRepresentation == null || isabelleStringRepresentation.isEmpty()) {
                         throw new RuntimeException(Bundle.ID_transformed_goal_unknown());
                     }
                     // We now have the corresponding Isabelle string formula, which
                     // can be printed back to the proof script:
                     try {
                         // Commit the rule application result back to the Isabelle's proof
                         // script:
                         proofDocument.insertAfter(proofDocument.getLastLockedElement(), String.format("apply (diabelli \"%s\")\n", isabelleStringRepresentation.get(0).getFormula()));
                     } catch (InterruptedException ex) {
                         Exceptions.printStackTrace(ex);
                     }
                 }
             }
         }
     }
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Protected Properties">
     /**
      * Sets the goals and fires the goals changed event if the new goals differ
      * from the current ones.
      *
      * @param goals the new goals to be set.
      * @throws PropertyVetoException thrown if the new goals could not be set
      * for any reason.
      */
     @NbBundle.Messages(value = {"BGPR_goals_change_vetoed="})
     public void setGoals(Goals goals) throws PropertyVetoException {
         if (this.goals != goals) {
             preCurrentGoalsChanged(this.goals, goals);
             Goals oldGoals = this.goals;
             this.goals = goals;
             fireCurrentGoalsChangedEvent(oldGoals);
         }
     }
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Property Changed Event Stuff">
     protected void fireCurrentGoalsChangedEvent(Goals oldGoals) {
         pcs.firePropertyChange(CurrentGoalsChangedEvent, oldGoals, goals);
     }
 
     /**
      * This method is invoked by the default implementation of
      * {@link BareGoalProvidingReasoner#setGoals(diabelli.logic.Goals)} just
      * before it actually changes the goals. Subclasses may override this method
      * to veto the change (by throwing a {@link PropertyVetoException}).
      *
      * @param oldGoals goals before the change.
      * @param newGoals goals after the change.
      * @throws PropertyVetoException thrown if the new goals could not be set
      * for any reason.
      */
     protected void preCurrentGoalsChanged(Goals oldGoals, Goals newGoals) throws PropertyVetoException {
     }
 
     private static class FormulaFormatsContainer {
 
         private static final List<FormulaFormat<?>> IsabelleFormats;
 
         static {
             ArrayList<FormulaFormat<?>> tmp = new ArrayList<>();
             tmp.add(TermFormatDescriptor.getInstance());
             tmp.add(StringFormat.getInstance());
             IsabelleFormats = Collections.unmodifiableList(tmp);
         }
     }
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Isabelle Goal-Change Monitoring">
     /**
      * Tries to fetch the current prover. It will throw an exception if the
      * prover could not have been obtained for any reason. <p>This prover is
      * needed to listen for messages from Isabelle to I3P.</p>
      *
      * @return the current prover instance. It never returns {@code null}, it
      * rather throws exceptions.
      * @throws IllegalStateException thrown if the prover instance could not
      * have been obtained for some reason.
      */
     @NbBundle.Messages({
         "ID_no_iapp=Could not obtain a communication channel with Isabelle.",
         "ID_no_prover=Could not obtain the prover interface to Isabelle."
     })
     public static ProverManager getProver() throws IllegalStateException {
         IAPP iapp = IAPP.getInstance();
         if (iapp == null) {
             throw new IllegalStateException(Bundle.ID_no_iapp());
         }
         ProverManager prover = iapp.getProver();
         if (prover == null) {
             throw new IllegalStateException(Bundle.ID_no_prover());
         }
         return prover;
     }
 
     /**
      * Issues an ML command to the prover and returns an injection result.
      * <p>You may wish to add an {@link InjectionResultListener} to the returned
      * {@link InjectionResult}. With this you will receive the response of the
      * prover.</p>
      *
      * @param cmd the ML command to be issued to the prover.
      * @return the <span style="font-style:italic;">future</span> result (the
      * answer of the prover).
      * @throws UnsupportedOperationException
      */
     @NbBundle.Messages({
         "ID_no_commands=Support for issuing commands to Isabelle is missing. This is needed to obtain goals from it."
     })
     public static InjectionResult executeCommand(String cmd) throws UnsupportedOperationException {
         ProverManager prover = IAPP.getInstance().getProver();
         InjectedCommands inj = prover.getFeature(InjectedCommands.class);
         if (inj == null) {
             throw new UnsupportedOperationException();
         }
         return inj.ML(null, cmd);
     }
 
     /**
      * This class listens for messages from Isabelle. There are plenty of
      * messages being exchanged all the time. This listener uses a delay
      * mechanism to prevent redundant goal updates when there is an onslaught of
      * messages and goals changes (for example, when the user tells Isabelle to
      * evaluate commands somewhere late in the theory file). This class receives
      * notifications of the completion of the request to Isabelle that it should
      * return current goals.
      */
     private class IsabelleMessageListener extends InjectionFinishListener implements StateListener, ActionListener, PropertyChangeListener {
 
         private static final String DIABELLI_ISABELLE_RESPONSE = "DiabelliResponse: ";
         private static final String DIABELLI_ISABELLE_RESPONSE_GOAL = DIABELLI_ISABELLE_RESPONSE + "Goal: ";
         private static final String DIABELLI_ISABELLE_RESPONSE_GOAL_STRING = DIABELLI_ISABELLE_RESPONSE + "Goal string: ";
         private static final int DelayMillis = 500;
         private final Timer delayer;
 
         /**
          * <span style="font-weight:bold">Do not create new instances of this
          * class.</span> Use the one provided in
          * {@link IsabelleDriver#isabelleListener}.
          */
         IsabelleMessageListener() {
             // Start listening for goal changes in Isabelle:
             CentralEventDispatcher centralEvents = IAPP.getInstance().getCentralEvents();
             centralEvents.addStateListener(this);
             TopComponent.getRegistry().addPropertyChangeListener(this);
             delayer = new Timer(DelayMillis, this);
             delayer.setRepeats(false);
             delayer.setCoalesce(true);
             delayer.setInitialDelay(DelayMillis);
         }
 
         /**
          * This is invoked by the ``delay timer'', which is started (and reset)
          * upon every
          * {@link IsabelleMessageListener#stateChanged(org.isabelle.iapp.proofdocument.StateChangeEvent) state change event}.
          * The reason for delaying this action is to merge an avalanche of state
          * change events into a single request to Isabelle to give us its
          * current goals.
          *
          * @param e
          */
         @Override
         public void actionPerformed(ActionEvent e) {
             delayer.stop();
             fetchGoalsFromIsabelle();
         }
 
         /**
          * This method receives Isabelle's response, which gives us the YXML
          * format of the current goals in Isabelle.
          *
          * @param inj
          */
         @Override
         @NbBundle.Messages({
             "ID_isabelle_goals_not_obtained=Could not fetch the list of goals from Isabelle. A communication error occurred."
         })
         public void injectedFinished(InjectionResult inj) {
             ArrayList<Goal> goals = null;
             try {
                 Message[] results = inj.getResults();
                 InjectionContext injectionContext = inj.getInjectionContext();
                 if (results != null && (results.length & 1) == 0 && injectionContext != null) {
                     goals = new ArrayList<>();
                     for (int i = 0; i < results.length; i += 2) {
                         Message message = results[i];
                         if (message.getText() != null && message.getText().startsWith(DIABELLI_ISABELLE_RESPONSE_GOAL)) {
                             String escapedYXML = message.getText().substring(DIABELLI_ISABELLE_RESPONSE_GOAL.length());
                             String unescapedYXML = TermYXML.unescapeControlChars(escapedYXML);
                             Term term = TermYXML.parseYXML(unescapedYXML);
                             final TermGoal toGoal = TermsToDiabelli.toGoal(term, injectionContext.getProofDocument());
                             // Get the string version of this goal:
                             toGoal.asFormula().addRepresentation(new FormulaRepresentation<>(new StringFormula(results[i + 1]), StringFormat.getInstance()));
                             goals.add(toGoal);
                         }
                     }
                 }
             } catch (InterruptedException ex) {
                 throw new RuntimeException(Bundle.ID_isabelle_goals_not_obtained(), ex);
             } finally {
                 setGoals(goals);
             }
         }
 
         /**
          * This method is invoked by I3P when the state of the Isabelle prover
          * changes. Typically, only user's interaction (like issuing commands
          * etc.) triggers an avalanche of state changes. After the avalanche is
          * over, no state changes should happen before another user-interaction.
          *
          * @param ev
          */
         @Override
         public void stateChanged(StateChangeEvent ev) {
             delayer.restart();
         }
 
         /**
          * Listens for TopComponent activations. It looks whether an I3P
          * Isabelle theory editor has been activated by the user (i.e., whether
          * the user has started editing an Isabelle theory editor).
          *
          * @param evt
          */
         @Override
         public void propertyChange(PropertyChangeEvent evt) {
             if ("activated".equals(evt.getPropertyName())) {
                 if (getActiveTheoryEditor() != null) {
                     requestActive();
                 }
             }
         }
     }
 
     /**
      * Asks Isabelle to return all its current goals. Isabelle will return an
      * XML dump of the term trees.
      */
     private void fetchGoalsFromIsabelle() {
         try {
             InjectionResult cmd = executeCommand("GoalsExport.i3p_write_sds_goals ()");
             cmd.addInjectionResultListener(isabelleListener);
         } catch (UnsupportedOperationException uoex) {
             Exceptions.attachSeverity(uoex, Level.INFO);
         }
     }
 
     private void setGoals(ArrayList<Goal> goals) {
         try {
            if (goals == null || goals.isEmpty()) {
                 setGoals((Goals) null);
             } else {
                 setGoals(new Goals(this, goals));
             }
         } catch (PropertyVetoException ex) {
             Logger.getLogger(IsabelleDriver.class.getName()).log(Level.WARNING, "Goals could not have been set.", ex);
         }
     }
     // </editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Private Helper Methods">
     private static TheoryEditor getActiveTheoryEditor() {
         TopComponent activated = TopComponent.getRegistry().getActivated();
         if (activated instanceof TheoryEditor) {
             return (TheoryEditor) activated;
         }
         return null;
     }
     //</editor-fold>
 }
