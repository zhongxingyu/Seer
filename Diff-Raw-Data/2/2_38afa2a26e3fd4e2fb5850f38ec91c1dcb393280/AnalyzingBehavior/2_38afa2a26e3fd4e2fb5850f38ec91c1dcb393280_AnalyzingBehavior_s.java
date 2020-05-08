 package galapagos;
 
 import java.util.*;
 
 /**
  * An abstract behavior type that stores knowledge about interactions
  * and permit later analysis to choose an optimal strategy.
  */
public abstract class AnalyzingBehavior extends MemoryBehavior<Analyzer.Analysis> {
     /**
      * An interaction history for a specific finch.
      */
     protected class Analysis implements Iterable<Analysis.Interaction>{
         /**
          * The action by this AnalyzingBehavior that is immediately
          * waiting for a response.
          */
         private Action awaitingAction;
 
         /**
          * The most recent action that has been performed by this
          * AnalyzingBehavior.
          */
         private Action justMadeAction;
         
         /**
          * All previous interactions.
          */
         private List<Interaction> interactions;
 
         private Analysis() {
             interactions = new LinkedList<Interaction>();
         }
         
         protected class Interaction {
             /**
              * The action performed by the AnalyzingBehavior in an
              * interaction.
              */
             protected final Action action;
 
             /**
              * The reaction the action caused.
              */
             protected final Action reaction;
             
             protected Interaction(Action action, Action reaction) {
                 this.action = action;
                 this.reaction = reaction;
             }
         }
         
         /**
          * Register a reaction from the finch this Analysis is about.
          *
          * @param reaction The reaction from the finch this Analysis
          * is about.
          */
         protected void registerReaction(Action reaction) {
             if (awaitingAction != null)
                 interactions.add(new Interaction(awaitingAction, reaction));
             awaitingAction = null;
         }
 
         /**
          * Register than an action has been performed by the
          * AnalyzingBehavior has been performed upon the finch this
          * Analysis is about.
          *
          * @param action The Action performed by this AnalyzingBehavior.
          */
         protected void registerAction(Action action) {
             assert (awaitingAction == null) 
                 : "Cannot register new action, is still pending a reaction";
             awaitingAction = justMadeAction;
             justMadeAction = action;
         }
 
         public Iterator<Interaction> iterator() {
             return interactions.iterator();
         }
     }
 
     /**
      * Make sure we have memory about the provided finch. Will create
      * memory object if we do not, will not do anything if we do.
      *
      * @param finch The finch whose memory reference should be
      * ensured.
      */
     private void ensureFinchMemory(Finch finch) {
         if (recall(finch) == null)
             remember(finch, new Analysis());
     }
 
     /**
      * Register the fact that the provided finch has reacted with the
      * provided reaction. For proper function of the class, this
      * method should always be called in the response() method of
      * subclasses.
      */
     protected void registerReaction(Finch finch, Action reaction) {
         ensureFinchMemory(finch);
         recall(finch).registerReaction(reaction);
     }
 
     /**
      * Register the fact that this finch has performed the provided
      * action on the provided finch. For proper function of the class,
      * this method should always be called in the decide() method of
      * subclasses with the decided-upon action.
      */
     protected void registerAction(Finch finch, Action action) {
         ensureFinchMemory(finch);
         recall(finch).registerAction(action);
     }
 }
