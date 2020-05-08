 package linkki.taskswitching.service;
 
 import java.util.List;
 import linkki.taskswitching.dto.Reaction;
 import linkki.taskswitching.dto.TestResult;
 import linkki.taskswitching.view.AggregateResult;
 import org.springframework.stereotype.Service;
 
 @Service
 public class AggregateResultService {
     
 
     public AggregateResult calculateResult(TestResult result) {
         switch (result.getTestType()) {
             case "REACTION":
                 return calculateReactionAggregateResult(result);
             case "NUMBERREACTION":
                 return calculateNumberReactionAggregateResult(result);
             case "CHARACTERREACTION":
                 return calculateCharacterReactionAggregateResult(result);
             case "TASKSWITCHING":
                 return calculateTaskSwitchingAggregateResult(result);
         }
 
         return new AggregateResult();
 
     }
 
     private AggregateResult calculateReactionAggregateResult(TestResult result) {
         AggregateResult aggregateResult = new AggregateResult();
         if (result.getReactions() == null) {
             return aggregateResult;
         }
 
         double correct = 0;
         double reactionTime = 0;
         for (Reaction reaction : result.getReactions()) {
             if (reaction.getPressedTime() == null) {
                 continue;
             }
 
             reactionTime += reaction.getReactionTimeInMs();
             correct++;
         }
 
         aggregateResult.setParticipantId(result.getParticipant().getId());
         aggregateResult.setInfo(result.getInfo());
         aggregateResult.setTestType(result.getTestType());
         aggregateResult.setHitsPercentage(100.0 * correct / result.getReactions().size());
         aggregateResult.setReactionTime(Math.round(reactionTime / correct));
         aggregateResult.setHitsOutsideTimespan(result.getAdditionalKeyPresses().size());
 
         return aggregateResult;
     }
 
     private AggregateResult calculateNumberReactionAggregateResult(TestResult result) {
 
         AggregateResult aggregateResult = new AggregateResult();
         if (result.getReactions() == null) {
             return aggregateResult;
         }
 
         double correct = 0;
         double reactionTime = 0;
         for (Reaction reaction : result.getReactions()) {
             if (reaction.getCorrect() == null || !reaction.getCorrect()) {
                 continue;
             }
 
             reactionTime += reaction.getReactionTimeInMs();
             correct++;
         }
 
         aggregateResult.setParticipantId(result.getParticipant().getId());
         aggregateResult.setInfo(result.getInfo());
         aggregateResult.setTestType(result.getTestType());
         aggregateResult.setHitsPercentage(100.0 * correct / result.getReactions().size());
         aggregateResult.setReactionTime(Math.round(reactionTime / correct));
         aggregateResult.setHitsOutsideTimespan(result.getAdditionalKeyPresses().size());
 
         return aggregateResult;
     }
 
     private AggregateResult calculateCharacterReactionAggregateResult(TestResult result) {
         // same functionality as for prev
         return calculateNumberReactionAggregateResult(result);
     }
 
     private AggregateResult calculateTaskSwitchingAggregateResult(TestResult result) {
 
         AggregateResult aggregateResult = new AggregateResult();
         if (result.getReactions() == null) {
             return aggregateResult;
         }
 
         double correct = 0;
         double reactionTime = 0;
         for (Reaction reaction : result.getReactions()) {
             if (reaction.getCorrect() == null || !reaction.getCorrect()) {
                 continue;
             }
 
             reactionTime += reaction.getReactionTimeInMs();
             correct++;
         }
 
         aggregateResult.setParticipantId(result.getParticipant().getId());
         aggregateResult.setInfo(result.getInfo());
         aggregateResult.setTestType(result.getTestType());
         aggregateResult.setHitsPercentage(100.0 * correct / result.getReactions().size());
         aggregateResult.setReactionTime(Math.round(reactionTime / correct));
         aggregateResult.setHitsOutsideTimespan(result.getAdditionalKeyPresses().size());
 
         addRepeatedAggregateResults(result, aggregateResult);
         addChangedAggregateResults(result, aggregateResult);
 
         return aggregateResult;
     }
 
     private void addRepeatedAggregateResults(TestResult result, AggregateResult aggregateResult) {
         List<Reaction> reactions = result.getReactions();
 
         int count = 0;
         double correct = 0;
         double reactionTime = 0;
         for (int i = 1; i < reactions.size(); i++) {
             Reaction reaction = reactions.get(i);
             Reaction previous = reactions.get(i - 1);
             if (!reaction.getElementType().equals(previous.getElementType())) {
                 continue;
             }
 
             count++;
             if (reaction.getCorrect() == null || !reaction.getCorrect()) {
                 continue;
             }
 
             reactionTime += reaction.getReactionTimeInMs();
             correct++;
         }
 
         aggregateResult.setHitsRepeated(100.0 * correct / count);
         aggregateResult.setRepeatedReactionTime(reactionTime / correct);
     }
 
     private void addChangedAggregateResults(TestResult result, AggregateResult aggregateResult) {
         List<Reaction> reactions = result.getReactions();
 
         int count = 0;
         double correct = 0;
         double reactionTime = 0;
         for (int i = 1; i < reactions.size(); i++) {
             Reaction reaction = reactions.get(i);
             Reaction previous = reactions.get(i - 1);
             if (reaction.getElementType().equals(previous.getElementType())) {
                 continue;
             }
 
             count++;
             if (reaction.getCorrect() == null || !reaction.getCorrect()) {
                 continue;
             }
 
             reactionTime += reaction.getReactionTimeInMs();
             correct++;
         }
 
         aggregateResult.setHitsChanged(100.0 * correct / count);
         aggregateResult.setChangedReactionTime(reactionTime / correct);
     }
 }
