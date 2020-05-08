 package org.eclipse.uml2.diagram.sequence.model.builder;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.uml2.diagram.sequence.model.sequenced.SDAbstractMessage;
 import org.eclipse.uml2.diagram.sequence.model.sequenced.SDBehaviorSpec;
 import org.eclipse.uml2.diagram.sequence.model.sequenced.SDBracket;
 import org.eclipse.uml2.diagram.sequence.model.sequenced.SDBracketContainer;
 import org.eclipse.uml2.diagram.sequence.model.sequenced.SDExecution;
 import org.eclipse.uml2.diagram.sequence.model.sequenced.SDFactory;
 import org.eclipse.uml2.diagram.sequence.model.sequenced.SDFrame;
 import org.eclipse.uml2.diagram.sequence.model.sequenced.SDGate;
 import org.eclipse.uml2.diagram.sequence.model.sequenced.SDGateMessage;
 import org.eclipse.uml2.diagram.sequence.model.sequenced.SDGateMessageEnd;
 import org.eclipse.uml2.diagram.sequence.model.sequenced.SDInvocation;
 import org.eclipse.uml2.diagram.sequence.model.sequenced.SDLifeLine;
 import org.eclipse.uml2.diagram.sequence.model.sequenced.SDMessage;
 import org.eclipse.uml2.diagram.sequence.model.sequenced.SDSimpleNode;
 import org.eclipse.uml2.uml.CombinedFragment;
 import org.eclipse.uml2.uml.Continuation;
 import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
 import org.eclipse.uml2.uml.ExecutionSpecification;
 import org.eclipse.uml2.uml.Gate;
 import org.eclipse.uml2.uml.Interaction;
 import org.eclipse.uml2.uml.InteractionFragment;
 import org.eclipse.uml2.uml.InteractionOperand;
 import org.eclipse.uml2.uml.InteractionUse;
 import org.eclipse.uml2.uml.Lifeline;
 import org.eclipse.uml2.uml.Message;
 import org.eclipse.uml2.uml.MessageEnd;
 import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
 import org.eclipse.uml2.uml.MessageSort;
 import org.eclipse.uml2.uml.StateInvariant;
 
 public class SDBuilder {
 
 	private final Interaction myInteraction;
 	private final StartAndFinishRegistry myStartsAndFinishes;
 	private final LifeLineCallStack myCallStack;
 	private final SDBuilderTrace myTrace;
 	private SDFrame mySDFrame;
 	
 	public SDBuilder(Interaction interaction) {
 		myInteraction = interaction;
 		myStartsAndFinishes = new StartAndFinishRegistry(myInteraction);
 		myCallStack = new LifeLineCallStack();
 		myTrace = new SDBuilderTrace();
 	}
 	
 	public void updateMessageNumbers(){
 		List<SDAbstractMessage> orderedSDMessages = orderSDMessages();
 		Set<SDAbstractMessage> guard = new HashSet<SDAbstractMessage>();
 		
 		int rootNumber = 1;
 		for (SDAbstractMessage next : orderedSDMessages){
 			if (next instanceof SDMessage){
 				SDMessage nextMessage = (SDMessage)next;
				if (guard.contains(nextMessage)){
					continue;
				}
 				SDMessage firstInChain = findFirstMessageInChain(nextMessage);
 				firstInChain.setMessageNumber(String.valueOf(rootNumber++));
 				setupMessageNumbersForChain(firstInChain, guard);
 			}
 		}
 	}
 	
 	private void setupMessageNumbersForChain(SDMessage current, Set<SDAbstractMessage> guard){
 		if (guard.contains(current)){
 			return;
 		}
 		guard.add(current);
 		
 		SDExecution execution = current.getTarget();
 		int index = 1;
 		for (SDBracket nextBracket : execution.getBrackets()){
 			if (nextBracket instanceof SDInvocation){
 				SDInvocation nextInvocation = (SDInvocation)nextBracket;
 				SDMessage nextMessage = nextInvocation.getOutgoingMessage();
 				nextMessage.setMessageNumber(current.getMessageNumber() + "." + (index++));
 				setupMessageNumbersForChain(nextMessage, guard);
 			}
 		}
 	}
 	
 	private SDMessage findFirstMessageInChain(SDMessage message){
 		SDInvocation invocation = message.getSource();
 		if (invocation.getBracketContainer() instanceof SDLifeLine){
 			return message;
 		}
 		SDExecution containerExecution = (SDExecution)invocation.getBracketContainer();
 		return findFirstMessageInChain(containerExecution.getIncomingMessage());
 	}
 	
 	private List<SDAbstractMessage> orderSDMessages(){
 		Set<Message> matchedUMLMessages = new HashSet<Message>();
 		List<SDAbstractMessage> sdMessages = new LinkedList<SDAbstractMessage>();
 		
 		for (InteractionFragment next : myInteraction.getFragments()){
 			if (next instanceof MessageEnd){
 				MessageEnd nextMessageEnd = (MessageEnd)next;
 				Message umlMessage = nextMessageEnd.getMessage();
 				if (umlMessage == null){
 					continue;
 				}
 				if (matchedUMLMessages.contains(umlMessage)){
 					continue;
 				}
 				SDAbstractMessage sdMessage = SDModelHelper.findMessage(mySDFrame, umlMessage);
 				if (sdMessage == null){
 					continue;
 				}	
 				sdMessages.add(sdMessage);
 				matchedUMLMessages.add(umlMessage);
 			}
 		}
 		return sdMessages;
 	}
 	
 	/**
 	 * For tests only
 	 */
 	public LifeLineCallStack getCallStack() {
 		return myCallStack;
 	}
 	
 	public SDBuilderTrace getTrace(){
 		if (mySDFrame == null){
 			reBuildFrame();
 		}
 		return myTrace;
 	}
 	
 	public SDFrame getSDFrame(){
 		if (mySDFrame == null){
 			reBuildFrame();
 		}
 		return mySDFrame;
 	}
 	
 	public SDFrame reBuildFrame() {
 		myTrace.clear();
 		myCallStack.clear();
 		myStartsAndFinishes.forceRemap();
 		mySDFrame = SDFactory.eINSTANCE.createSDFrame();
 		mySDFrame.setUmlInteraction(myInteraction);
 		
 
 		buildGates(mySDFrame, myInteraction);
 		buildFrameLifeLines(mySDFrame, myInteraction);
 
 		for (Iterator<InteractionFragment> fragments = myInteraction.getFragments().iterator(); fragments.hasNext();) {
 			buildBrackets(fragments);
 		}
 		
 		updateMessageNumbers();
 
 		return mySDFrame;
 	}
 	
 	private void buildGates(SDFrame result, Interaction interaction) {
 		for (Gate umlGate : interaction.getFormalGates()) {
 			SDGate sdGate = SDFactory.eINSTANCE.createSDGate();
 			sdGate.setUmlGate(umlGate);
 			result.getGates().add(sdGate);
 		}
 	}
 
 	private void buildFrameLifeLines(SDFrame frame, Interaction interaction) {
 		assert frame.getLifelines().isEmpty();
 		for (Lifeline umlLifeline : interaction.getLifelines()) {
 			SDLifeLine sdLifeLine = myTrace.bindNewLifeline(umlLifeline);
 			frame.getLifelines().add(sdLifeLine);
 			myCallStack.push(umlLifeline, sdLifeLine);
 		}
 	}
 
 	private void buildBrackets(Iterator<InteractionFragment> orderedFragments) {
 		if (!orderedFragments.hasNext()) {
 			return;
 		}
 		InteractionFragment fragment = orderedFragments.next();
 		if (fragment instanceof StateInvariant) {
 			buildStateInvariant((StateInvariant) fragment);
 			return;
 		}
 		if (fragment instanceof CombinedFragment) {
 			unsupportedFragment(fragment);
 			return;
 		}
 		if (fragment instanceof Continuation) {
 			unsupportedFragment(fragment);
 			return;
 		}
 		if (fragment instanceof Interaction) {
 			unsupportedFragment(fragment);
 			return;
 		}
 		if (fragment instanceof InteractionOperand) {
 			unsupportedFragment(fragment);
 			return;
 		}
 		if (fragment instanceof InteractionUse) {
 			unsupportedFragment(fragment);
 			return;
 		}
 		if (fragment instanceof ExecutionOccurrenceSpecification) {
 			unsupportedFragment(fragment);
 			return;
 		}
 		if (fragment instanceof MessageOccurrenceSpecification) {
 			MessageOccurrenceSpecification messageEnd = (MessageOccurrenceSpecification) fragment;
 			Lifeline lifeline = ensureSingleCovered(messageEnd);
 			if (lifeline == null) {
 				warning("MessageOccurrenceSpecification without a lifeline: " + messageEnd);
 				return;
 			}
 			Message message = messageEnd.getMessage();
 			if (message == null) {
 				processPossibleExecutionFinish(orderedFragments, messageEnd, lifeline);
 				return;
 			}
 			if (message.getSendEvent() == messageEnd) {
 				buildMessageSource(orderedFragments, messageEnd);
 				return;
 			}
 			if (message.getReceiveEvent() == fragment) {
 				buildMessageTarget(orderedFragments, messageEnd);
 				return;
 			}
 		}
 		if (fragment instanceof ExecutionSpecification){
 			buildExecutionSpecification((ExecutionSpecification)fragment);
 			return;
 		}
 	}
 
 	private void processPossibleExecutionFinish(Iterator<InteractionFragment> orderedFragments, MessageOccurrenceSpecification messageEnd, Lifeline lifeline) {
 		ExecutionSpecification umlFinishedExecution = myStartsAndFinishes.findFinishedExecution(messageEnd);
 		if (umlFinishedExecution == null){
 			warning("Lost message end (no message) is found (will be ignored):" + messageEnd);
 			return;
 		}
 		
 		if (ensureSingleCovered(umlFinishedExecution) != lifeline){
 			throw new UMLModelProblem("Execution is finished at wrong lifeline: " + umlFinishedExecution + ", expected lifeline is: " + lifeline);
 		}
 		
 		SDBracketContainer sdFinishedContainer = myCallStack.peek(lifeline);
 		if (false == sdFinishedContainer instanceof SDBehaviorSpec && ((SDBehaviorSpec)sdFinishedContainer).getUmlExecutionSpec() != umlFinishedExecution){
 			throw new UMLModelProblem("ExecutionSpecification finished: " + umlFinishedExecution + ", while active bracket container was :" + sdFinishedContainer);
 		}
 		
 		if (sdFinishedContainer instanceof SDInvocation && ((SDInvocation)sdFinishedContainer).getUmlExecutionSpec() == null){
 			throw new SDBuilderInternalProblem("SDInvocation : " + sdFinishedContainer + " does not have uml counterpart. However, we have found finish for it: " + messageEnd + ", actual umlExecution: " + umlFinishedExecution);
 		}
 		
 		myCallStack.pop(lifeline); //
 		if (sdFinishedContainer instanceof SDExecution){
 			SDExecution sdFinishedExecution = (SDExecution)sdFinishedContainer;
 			SDInvocation sdInvocation = sdFinishedExecution.getInvocation();
 			if (sdInvocation != null && sdInvocation.getUmlExecutionSpec() == null){
 				//this invocation was created manually in builder without uml-counterpart, 
 				//it means that we won't find finish for it and should remove it manually
 				SDLifeLine invocationLifeLine = SDModelHelper.findLifeline(sdInvocation);
 				if (invocationLifeLine == null){
 					throw new SDBuilderInternalProblem("Can't find lifeline for 'auxiliary' SDInvocation: " + sdInvocation);
 				}
 				myCallStack.pop(invocationLifeLine.getUmlLifeline());
 			}
 		}
 	}
 
 	private void buildExecutionSpecification(ExecutionSpecification umlExecutionSpec) {
 		Lifeline umlLifeline = ensureSingleCovered(umlExecutionSpec);
 		if (umlLifeline == null){
 			warning("ExecutionSpecification without lifeline, ignored: " + umlExecutionSpec);
 			return;
 		}
 		SDBracketContainer active = myCallStack.peek(umlLifeline);
 		//it should be bracket for this execution spec;
 		if (false == active instanceof SDBehaviorSpec || ((SDBehaviorSpec)active).getUmlExecutionSpec() != umlExecutionSpec){
 			throw new UMLModelProblem("Lost ExecutionSpecification found: " + umlExecutionSpec + ", active bracket container :" + active);
 		}
 		//everything is fine, we already have behaviorSpec for this umlExecutionSpec -- nothing to do
 	}
 	
 	private void buildMessageTarget(Iterator<InteractionFragment> orderedFragments, MessageOccurrenceSpecification messageTarget) {
 		Message message = messageTarget.getMessage();
 		MessageEnd sendEvent = message.getSendEvent();
 		if (sendEvent == null){
 			buildFoundMessage(messageTarget);
 			return;
 		}
 		if (sendEvent instanceof Gate){
 			buildGateMessage(messageTarget, (Gate)sendEvent, true);
 			return;
 		}
 		
 		MessageOccurrenceSpecification messageSource = (MessageOccurrenceSpecification) sendEvent;
 		throw new UMLModelProblem("Message " + message + " is sent from the future: " + messageSource);
 	}
 
 	private void buildMessageSource(Iterator<InteractionFragment> orderedFragments, MessageOccurrenceSpecification messageSource) {
 		Message message = messageSource.getMessage();
 		MessageEnd receiveEvent = message.getReceiveEvent();
 		if (receiveEvent == null) {
 			buildLostMessage(messageSource);
 			return;
 		}
 		if (receiveEvent instanceof Gate) {
 			buildGateMessage(messageSource, (Gate) receiveEvent, false);
 			return;
 		}
 		
 		MessageOccurrenceSpecification messageTarget = (MessageOccurrenceSpecification) receiveEvent;
 		boolean targetFound = false;
 		while (orderedFragments.hasNext()) {
 			InteractionFragment nextBetweenSendAndReceive = orderedFragments.next();
 			if (nextBetweenSendAndReceive == messageTarget) {
 				targetFound = true;
 				break;
 			}
 			warning("Interaction fragment found between message send and receive for message: " + message + ", that is: " + nextBetweenSendAndReceive + ". Will be ignored");
 		}
 
 		if (!targetFound) {
 			throw new UMLModelProblem("Message " + message + " is sent to the past");
 		}
 		
 		if (message.getMessageSort() == MessageSort.REPLY_LITERAL){
 			buildReplyMessage(orderedFragments, messageSource, messageTarget);
 			return;
 		} else {
 			buildCompleteMessage(orderedFragments, messageSource, messageTarget);
 			return;
 		}
 	}
 
 	private void buildCompleteMessage(Iterator<InteractionFragment> orderedFragments, MessageOccurrenceSpecification messageSource, MessageOccurrenceSpecification messageTarget) {
 		Message umlMessage = messageSource.getMessage();
 		Lifeline umlSendingLifeline = ensureSingleCovered(messageSource);
 		Lifeline umlReceivingLifeline = ensureSingleCovered(messageTarget);
 		if (umlSendingLifeline == null){
 			throw new UMLModelProblem("Message " + umlMessage + " has start :" + messageSource + " which does not belong to lifeline");
 		}
 		if (umlReceivingLifeline == null){
 			throw new UMLModelProblem("Message " + umlMessage + " has target :" + messageTarget + " which does not belong to lifeline");
 		}
 		
 		ExecutionSpecification umlInvocation = myStartsAndFinishes.findStartedExecution(messageSource); //may be null
 		ExecutionSpecification umlExecution = myStartsAndFinishes.findStartedExecution(messageTarget);
 		if (umlExecution == null){
 			throw new UMLModelProblem("Message " + umlMessage + " does not have receiving ExecutionSpecification at receiveEvent: " + messageTarget);
 		}
 		
 		SDMessage sdMessage = myTrace.bindNewMessage(umlMessage);
 		SDInvocation sdInvocation = myTrace.bindNewInvocation(umlInvocation);
 		SDExecution sdExecution = myTrace.bindNewExecution(umlExecution);
 
 		sdInvocation.setOutgoingMessage(sdMessage);
 		sdExecution.setIncomingMessage(sdMessage);
 		
 		sdInvocation.setReceiveExecution(sdExecution);
 		//sdExecution.setInvocation(sdInvocation); -- auto (bidi)
 		
 		sdExecution.setUmlStart(messageTarget);
 		sdExecution.setUmlFinish(umlExecution.getFinish());
 		
 		if (umlInvocation != null){
 			sdInvocation.setUmlStart(messageSource);
 			sdInvocation.setUmlFinish(umlInvocation.getFinish());
 		}
 		
 		mySDFrame.getMessages().add(sdMessage);
 
 		SDBracketContainer sdSendingContainer = myCallStack.peek(umlSendingLifeline);
 		sdSendingContainer.getBrackets().add(sdInvocation);
 		myCallStack.push(umlSendingLifeline, sdInvocation);
 		
 		//important to call after push for sending lifeline (consider self calls)
 		SDBracketContainer sdReceivingContainer = myCallStack.peek(umlReceivingLifeline); 
 		sdReceivingContainer.getBrackets().add(sdExecution);
 		myCallStack.push(umlReceivingLifeline, sdExecution);
 	}
 
 	private void buildGateMessage(MessageOccurrenceSpecification umlMessageEnd, Gate umlGate, boolean fromNotToGate) {
 		Lifeline umlLifeline = ensureSingleCovered(umlMessageEnd);
 		if (umlLifeline == null) {
 			warning("MessageOccurrenceSpecification without a lifeline: " + umlMessageEnd);
 			return;
 		}
 		SDBracketContainer sdContainer = myCallStack.peek(umlLifeline);
 		SDGateMessageEnd sdPureOccurrence = SDFactory.eINSTANCE.createSDGateMessageEnd();
 		sdPureOccurrence.setUmlMessageEnd(umlMessageEnd);
 		sdPureOccurrence.setIsStartNotFinish(!fromNotToGate);
 		sdContainer.getBrackets().add(sdPureOccurrence);
 
 		SDGateMessage sdMessageToGate = SDFactory.eINSTANCE.createSDGateMessage();
 		sdMessageToGate.setUmlMessage(umlMessageEnd.getMessage());
 		sdMessageToGate.setFromNotToGate(fromNotToGate);
 		sdMessageToGate.setGate(SDModelHelper.findGate(mySDFrame, umlGate));
 		sdMessageToGate.setNormalEnd(sdPureOccurrence);
 
 		mySDFrame.getMessages().add(sdMessageToGate);
 	}
 
 	private void buildLostMessage(MessageOccurrenceSpecification messageSource) {
 		unsupportedFragment(messageSource);
 	}
 	
 	private void buildReplyMessage(Iterator<InteractionFragment> orderedFragments, MessageOccurrenceSpecification replySource, MessageOccurrenceSpecification replyTarget) {
 		throw new SDBuilderInternalProblem("Reply-message is not supported: " + replySource.getMessage());
 	}
 
 	private void buildFoundMessage(MessageOccurrenceSpecification messageTarget) {
 		unsupportedFragment(messageTarget);
 	}
 
 	private void unsupportedFragment(InteractionFragment fragment) {
 		String metaclass = fragment.eClass().getName();
 		throw new SDBuilderInternalProblem(metaclass + " is not supported: " + fragment);
 	}
 
 	private void buildStateInvariant(StateInvariant umlInvariant) {
 		Lifeline umlLifeline = ensureSingleCovered(umlInvariant);
 		if (umlLifeline == null) {
 			return;
 		}
 		SDBracketContainer sdContainer = myCallStack.peek(umlLifeline);
 		SDSimpleNode sdInvariant = SDFactory.eINSTANCE.createSDSimpleNode();
 		sdInvariant.setUmlFragment(umlInvariant);
 		sdContainer.getBrackets().add(sdInvariant);
 	}
 	
 	private static Lifeline ensureSingleCovered(InteractionFragment fragment) {
 		List<Lifeline> covered = fragment.getCovereds();
 		if (covered.size() > 1) {
 			throw new UMLModelProblem("Expected single covered lifeline for: " + fragment + ", actually: " + fragment.getCovereds());
 		}
 		return covered.isEmpty() ? null : covered.get(0);
 	}
 	
 	private static void warning(String message) {
 		//
 	}
 
 }
