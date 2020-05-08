 package org.motechproject.ghana.national.tools.seed.data;
 
 import org.motechproject.decisiontree.model.*;
 import org.motechproject.decisiontree.repository.AllTrees;
 import org.motechproject.ghana.national.domain.IVRClipManager;
 import org.motechproject.ghana.national.domain.ivr.AudioPrompts;
 import org.motechproject.ghana.national.domain.ivr.ConnectToCallCenter;
 import org.motechproject.ghana.national.domain.ivr.MotechIdValidationTransition;
 import org.motechproject.ghana.national.domain.mobilemidwife.Language;
 import org.motechproject.ghana.national.tools.seed.Seed;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.motechproject.ghana.national.domain.ivr.AudioPrompts.*;
 import static org.motechproject.ghana.national.domain.mobilemidwife.Language.*;
 
 @Component
 public class InboundDecisionTreeSeed extends Seed {
     @Value("#{ghanaNationalProperties['callcenter.number']}")
     private String callCenterPhoneNumber;
 
     @Value("#{ghanaNationalProperties['callcenter.dtmf.timeout']}")
     private String callCenterDtmfTimeout;
 
     @Value("#{ghanaNationalProperties['callcenter.dtmf.finishonkey']}")
     private String callCenterFinishOnKey;
 
     private ConnectToCallCenter connectToCallCenter = new ConnectToCallCenter();
 
     @Autowired
     AllTrees allTrees;
     @Autowired
     private IVRClipManager ivrClipManager;
 
     @Override
     protected void load() {
         Tree tree = new Tree();
         tree.setName("InboundDecisionTree");
         tree.setRootNode(chooseLanguageNodeWithRetry());
         allTrees.addOrReplace(tree);
     }
 
     private Node chooseLanguageNodeWithRetry(){
         Node chooseLanguageFirstChance = chooseLanguageNode();
         Node chooseLanguageSecondChance = chooseLanguageNode();
         chooseLanguageSecondChance.getTransitions().put("timeout", new Transition().setDestinationNode(connectToCallCenter.getAsNode(callCenterPhoneNumber)));
         chooseLanguageFirstChance.getTransitions().put("timeout", new Transition().setDestinationNode(chooseLanguageSecondChance));
         return chooseLanguageFirstChance;
     }
 
     private Node chooseLanguageNode() {
         Node node = prompt(LANGUAGE_PROMPT, EN);
         Map<String, ITransition> transitions = new HashMap<String, ITransition>();
         transitions.put("1", new Transition().setDestinationNode(chooseActionNode(EN)));
         transitions.put("2", new Transition().setDestinationNode(chooseActionNode(KAS)));
         transitions.put("3", new Transition().setDestinationNode(chooseActionNode(NAN)));
         transitions.put("4", new Transition().setDestinationNode(chooseActionNode(FAN)));
         transitions.put("?", connectToCallCenter.getAsTransition(callCenterPhoneNumber));
         node.setTransitionNumDigits("1");
         node.setTransitionTimeout(callCenterDtmfTimeout);
        node.setTransitionFinishOnKey(callCenterFinishOnKey);
         node.setTransitions(transitions);
         return node;
     }
 
     private Node chooseActionNode(Language language) {
         Node node = prompt(REASON_FOR_CALL_PROMPT, language);
         Map<String, ITransition> transitions = new HashMap<String, ITransition>();
         transitions.put("1", new Transition().setDestinationNode(validateMotechIdNode(language, 3)));
         transitions.put("timeout", new Transition().setDestinationNode(connectToCallCenter.getAsNode(callCenterPhoneNumber)));
         transitions.put("?", connectToCallCenter.getAsTransition(callCenterPhoneNumber));
         node.setTransitionNumDigits("1");
         node.setTransitionTimeout(callCenterDtmfTimeout);
         node.setTransitionFinishOnKey(callCenterFinishOnKey);
         node.setTransitions(transitions);
         return node;
     }
 
     private Node validateMotechIdNode(Language language, int pendingRetries) {
         Node node = prompt(MOTECH_ID_PROMPT, language);
         Map<String, ITransition> transitions = new HashMap<String, ITransition>();
         MotechIdValidationTransition motechIdValidationTransition = new MotechIdValidationTransition(language.name(), pendingRetries);
         if(pendingRetries != 0){
             transitions.put("timeout", new Transition().setDestinationNode(validateMotechIdNode(language, pendingRetries - 1)));
             transitions.put("?", motechIdValidationTransition);
         }
         node.setTransitionTimeout(callCenterDtmfTimeout);
         node.setTransitionFinishOnKey(callCenterFinishOnKey);
         node.setTransitions(transitions);
         return node;
     }
 
     private Node prompt(AudioPrompts clipName, Language language) {
         return new Node().addTransitionPrompts(audioPromptFor(clipName, language));
     }
 
 
     private AudioPrompt audioPromptFor(AudioPrompts prompt, Language language) {
         return new AudioPrompt().setAudioFileUrl(ivrClipManager.urlFor(prompt.value(), language));
     }
 }
