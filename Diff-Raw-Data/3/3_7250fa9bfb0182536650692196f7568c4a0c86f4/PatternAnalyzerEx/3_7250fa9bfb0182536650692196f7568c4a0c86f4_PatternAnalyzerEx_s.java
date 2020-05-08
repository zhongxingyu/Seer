 /*
  *   PatternAnalyzerEx.java
  * 	 @Author Oleg Gorobets
  *   Created: 06.09.2007
  *   CVS-ID: $Id: 
  *************************************************************************/
 
 package org.swfparser;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import org.springframework.util.Assert;
 import org.swfparser.exception.LabelsInitException;
 import org.swfparser.exception.PatternAnalyzerException;
 import org.swfparser.pattern.BreakPattern;
 import org.swfparser.pattern.ContinuePattern;
 import org.swfparser.pattern.DoWhilePattern;
 import org.swfparser.pattern.DoWhileSkipPattern;
 import org.swfparser.pattern.ForInPattern;
 import org.swfparser.pattern.IfElsePattern;
 import org.swfparser.pattern.IfPattern;
 import org.swfparser.pattern.Pattern;
 import org.swfparser.pattern.SkipPattern;
 import org.swfparser.pattern.SwitchPattern;
 import org.swfparser.pattern.SwitchSkipPattern;
 import org.swfparser.pattern.TellTargetPattern;
 import org.swfparser.pattern.WhilePattern;
 import org.swfparser.pattern.WhileSkipPattern;
 import org.swfparser.util.PrintfFormat;
 
 import org.apache.log4j.Logger;
 
 import com.jswiff.swfrecords.actions.Action;
 import com.jswiff.swfrecords.actions.ActionBlock;
 import com.jswiff.swfrecords.actions.ActionConstants;
 import com.jswiff.swfrecords.actions.Branch;
 import com.jswiff.swfrecords.actions.End;
 import com.jswiff.swfrecords.actions.Enumerate;
 import com.jswiff.swfrecords.actions.Enumerate2;
 import com.jswiff.swfrecords.actions.Equals;
 import com.jswiff.swfrecords.actions.Equals2;
 import com.jswiff.swfrecords.actions.If;
 import com.jswiff.swfrecords.actions.Jump;
 import com.jswiff.swfrecords.actions.SetTarget;
 import com.jswiff.swfrecords.actions.SetTarget2;
 import com.jswiff.swfrecords.actions.StrictEquals;
 
 public class PatternAnalyzerEx {
 	
 	private static Logger logger = Logger.getLogger(PatternAnalyzerEx.class);
 	
 	private static PrintfFormat actionFormat = new PrintfFormat("###A %08X: [%s] [%s] 0x%02X (%s) %s");
 	private static PrintfFormat offsetFormat = new PrintfFormat("0x%08X");
 	
 	private PatternContext context;
 	private List<Action> actions;
 	
 	private boolean enumerateStarted = false;
 	private int enumeratePointer = -1;
 	
 	private static int instanceCounter = 0;
 	private static int labelCounter = 1;
 	
 	private int startPointer;
 	private int endPointer;
 	
 	public PatternAnalyzerEx(PatternContext context, List<Action> actions) throws LabelsInitException {
 		super();
 		this.context = context;
 		this.actions = actions;
 		this.startPointer = 0;
 		this.endPointer = actions.size()-1;
         logger.debug("startPointer = 0, endPointer (actions.size) = " + this.endPointer);
 		init();
 	}
 	
 	protected PatternAnalyzerEx(PatternContext context, List<Action> actions, int startPointer, int endPointer) throws LabelsInitException {
 		super();
 		this.context = context;
 		this.actions = actions;
 		this.startPointer = startPointer;
 		this.endPointer = endPointer;
 		init();
 	}
 	
 	private void init() throws LabelsInitException {
 		instanceCounter++;
 		labelCounter = 1;
 		if (this.actions.isEmpty()) {
 			return;
 		}
         appendEndActionIfNeeded();
 		// build labels
 		if (isRootBlock()) {
 			initLabels();
 			checkLabels();
 		}
 	}
 
     private void appendEndActionIfNeeded() {
         Action lastAction = this.actions.get(this.actions.size()-1);
         if (! (lastAction instanceof End) ) {
             End end = new End();
             end.setLabel(ActionBlock.LABEL_END);
             end.setOffset(lastAction.getOffset()+lastAction.getSize());
             this.actions.add(end); // add virtual End action
         }
     }
 
     private void initLabels() {
 		logger.debug("in `initLabels()`");
 		int pointer = 0;
 		for (Action action : actions) {
 //			logger.debug("#init() action="+action+" label = "+action.getLabel());
             if (action.hasLabel()) {
 				// nothing...
 			} else if (action instanceof Branch) { // IF and JUMP actions extend `Branch`
 				action.setLabel(createLabel()); // set labels to all branch actions
 			} else if ((action instanceof SetTarget) || (action instanceof SetTarget2)) {
 				action.setLabel(createLabel("TELL_TARGET")); // set labels to all tellTarget() actions
 			} else if (action instanceof Enumerate || action instanceof Enumerate2) {
 				action.setLabel(createLabel("ENUMERATE"));
 			}
 			if (action.hasLabel()) {
 				context.getLabels().put(action.getLabel(), action); // Map `label => action`
 			}
 			Assert.isTrue(!context.getActionPointerMap().containsKey(action));
 			context.getActionPointerMap().put(action, pointer);
 			pointer++;
 		}
 		
 		// Only print actions
 		for (Action action :actions) {
 			logger.debug(getActionDebugString(action));
 		}
 		
 	}
 
 	private static String createLabel() {
 		return "JUMP_LABEL_"+ instanceCounter +"_"+(labelCounter++);
 	}
 	
 	private static String createLabel(String prefix) {
 		return prefix+"_"+ instanceCounter +"_"+(labelCounter++);
 	}
 	
 	public void checkLabels() throws LabelsInitException {
 		Map<String,Action> labels = new HashMap<String, Action>();
 		Set<String> branchLabels = new HashSet<String>();
 		for (Action action : actions) {
 			if (action.hasLabel()) {
 				labels.put(action.getLabel(), action);
 			}
 			if (action instanceof Branch) {
 				Branch branch = (Branch) action;
                 boolean hasBranchLabel = branch.getBranchLabel() != null;
                 if (hasBranchLabel) {
 					branchLabels.add(branch.getBranchLabel());
 				}
 			}
 		}
 		
 		logger.debug("++++ Branch labels +++++ ");
 		for (String branchLabel : branchLabels) {
 			logger.debug("BL:"+branchLabel);
 		}
 		logger.debug("++++ Labels +++++ ");
 		for (String label : labels.keySet()) {
 			logger.debug(" L:"+label);
 		}
 		
 		for (String branchLabel : branchLabels) {
 			if(! (labels.containsKey(branchLabel) || ActionBlock.LABEL_END.equals(branchLabel))) {
 				throw new LabelsInitException("Branch label "+branchLabel+" is not initialized.");
 			}
 		}
 
 	}
 
 	public void analyze() throws PatternAnalyzerException {
 		int pointer = startPointer;
 		while (pointer <= endPointer) {
 			int newPointer = pointer+1;
 			Action action = actions.get(pointer);
 			if (action.hasLabel() && !context.getPatterns().containsKey(action.getLabel())) {
 				if (action instanceof Branch) {
 					newPointer = analyzeBranch((Branch) action);
 				}
 				if (action instanceof Enumerate || action instanceof Enumerate2) {
 					enumerateStarted = true;
 					enumeratePointer = pointer;
 				}
 				if (action instanceof SetTarget) {
 					newPointer = analyzeSetTarget(action);
 				}
 				if (action instanceof SetTarget2) {
 					newPointer = analyzeSetTarget(action);
 				}
 			}
 			
 			pointer = newPointer;
 			
 		}
 		
 		if (isRootBlock()) {
 			// Write results
 			logger.debug("###### Analyze results #########");
 			for (Action action : actions) {
 				logger.debug(getActionDebugString(action));
 			}
 		}
 	}
 	
 	private int analyzeBranch(Branch action) throws PatternAnalyzerException {
 		if (action instanceof Jump) {
 			return analyzeJump((Jump) action);
 		} else if (action instanceof If) {
 			int ifPointer = context.getActionPointerMap().get( action );
 			int jumpPointer = context.getActionPointerMap().get( context.getLabels().get(action.getBranchLabel()) );
 			int ifOffset = actions.get(ifPointer).getOffset();
 			int jumpOffset = actions.get(jumpPointer).getOffset();
 			IfHandle ifHandle = new IfHandle((If)action, ifPointer, jumpPointer, ifOffset, jumpOffset);
 			return analyzeIf(ifHandle);
 		} else {
 			throw new IllegalArgumentException("Invalid branch action "+action);
 		}
 		
 	}
 
 	/**
 	 * @param handle
 	 */
 	private int analyzeIf(IfHandle handle) throws PatternAnalyzerException {
 		If action = handle.getIf();
 		
 		logger.debug("Analyzing IF: "+offsetFormat.sprintf(handle.getOffset())+" - "+offsetFormat.sprintf(handle.getJumpOffset())+" label:"+action.getLabel());
 		logger.debug("IF pointer: "+handle.getPointer()+", jump pointer: "+handle.getJumpPointer());
 		
 		if (handle.getJumpOffset() < handle.getOffset()) {
 			// PATTERN: DO-WHILE
 			logger.debug("Setting target to DO-WHILE");
 			List<Action> doWhileActions = actions.subList(handle.getJumpPointer(),handle.getPointer());
 //			context.getPatterns().put(actions.get(handle.getJumpPointer()).getLabel(), 
 //					new SkipForDoWhilePattern(doWhileActions)
 //			);
 			DoWhilePattern doWhilePattern = new DoWhilePattern(doWhileActions);
 //			context.getPatterns().put( handle.getIf().getLabel(), doWhilePattern );
 			
 			context.getPatterns().put( actions.get(handle.getJumpPointer()).getLabel(), doWhilePattern );
 			context.getPatterns().put( handle.getIf().getLabel(), new DoWhileSkipPattern() );
 			
 			setBreaksForDoWhile(doWhilePattern.getActions());
 			
 			// re-analyze block for "break" and "continue"
 			analyzeNewBlock(handle.getJumpPointer(),handle.getPointer()-1, doWhilePattern);
 			
 			return handle.getPointer();
 			
 		} else {
 			// analyze jumps
 			PatternJumpsInfo info = getJumpsInfo(handle);
 			
 			logger.debug("Jump info: Total.jumps before = "+info.getAllJumpsBefore().size());
 			logger.debug("Jump info: Uncond.jumps after = "+info.getUnconditionalJumpsAfter().size()+",Cond.jump after="+info.getConditionalJumpsAfter().size());
 			logger.debug("Jump info: Uncond.jumps end = "+info.getUnconditionalEndJumps().size()+",Cond.jump end="+info.getConditionalEndJumps().size());
 			
 			if (info.getAllJumpsBefore().size() > 0) {
 				// get latest jump
 //				if (info.getLatestJumpBefore() == actions.get(handle.getJumpPointer()-1)) {
 				
 					// there should be no jumps after the block, while jumps right at the end of the block may occur
 					Assert.isTrue(info.getAllJumpsAfter().isEmpty());
 					
 					// Evaluate the end of while/enumerate
 					int whileEndPointer;
 					String whileSkipLabel = info.getLatestJumpBefore().getLabel();
 					int trickyWhilePatternSizeAddon = 0;
 					if (info.getLatestJumpBefore() == actions.get(handle.getJumpPointer()-1)) {
 						// standard while
 						whileEndPointer = handle.getJumpPointer()-1;
 //						whileSkipLabel = info.getLatestJumpBefore().getLabel();
 					} else {
 						// tricky while
 						logger.error("Tricky while");
 						whileEndPointer = context.getActionPointerMap().get( info.getLatestJumpBefore() );
 						trickyWhilePatternSizeAddon = handle.getJumpPointer()-whileEndPointer-1;
 //						whileSkipLabel = info.getLatestJumpBefore().getLabel();
 						
 					}
 					
 					if (enumerateStarted) {
 						// PATTERN: FOR..IN
 						logger.debug("Setting target to FOR..IN");
 						int addedSize = handle.getPointer() - enumeratePointer + 1 + trickyWhilePatternSizeAddon;
 						List<Action> forInActions = actions.subList(handle.getPointer()+1, whileEndPointer);
 						List<Action> varActions = actions.subList(enumeratePointer+1, handle.getPointer());
 						ForInPattern forInPattern = new ForInPattern(forInActions,varActions,addedSize);
 						context.getPatterns().put( actions.get(enumeratePointer).getLabel(), forInPattern );
 						context.getPatterns().put(whileSkipLabel, new WhileSkipPattern());
 						
 						// set break's and continues
 						setBreaksForDoWhile(forInActions);
 						
 						// re-scan action inside for..in
 						analyzeNewBlock(handle.getPointer()+1, whileEndPointer-1, forInPattern);
 						
 						enumerateStarted = false;
 						
 						return handle.getJumpPointer();
 						
 					} else {
 						// PATTERN: WHILE
 						logger.debug("Setting target to WHILE");
 						List<Action> whileActions =  actions.subList(handle.getPointer()+1, whileEndPointer);
 						WhilePattern whilePattern = new WhilePattern(whileActions,trickyWhilePatternSizeAddon); 
 						context.getPatterns().put(handle.getIf().getLabel(), whilePattern );
 						context.getPatterns().put(whileSkipLabel, new WhileSkipPattern());
 						
 						// set break's and continues
 						setBreaks(whilePattern,info);
 						
 						// re-scan actions inside while
 						analyzeNewBlock(handle.getPointer()+1, whileEndPointer-1, whilePattern);
 						
 						return handle.getJumpPointer();
 						
 					}
 				
 //				} else {
 //					// skip by now, possibly "continue" inside do-while block
 //					logger.debug("Skipping "+info.getLatestJumpBefore().getLabel()+" by now... Maybe continue inside do-while");
 //					return handle.getJumpPointer();
 //				}
 				
 			} else if (info.getAllJumpsAfter().size() > 0) {
 				
 				boolean isIfElse = false;
 				if (info.getUnconditionalJumpsAfter().size() == 1 && info.getConditionalJumpsAfter().isEmpty()) {
 					// check if this jump is the last action in if block, that means we're inside if-else
 					Branch b = info.getUnconditionalJumpsAfter().keySet().iterator().next();
 					if (b == actions.get(handle.getJumpPointer()-1)) {
 						isIfElse = true;
 						// PATTERN: IF-ELSE
 						logger.debug("If-Else, setting target to IF-ELSE");
 						List<Action> ifActions = actions.subList(handle.getPointer()+1, handle.getJumpPointer()-1);
 						int elseEndsPointer = context.getActionPointerMap().get(info.getUnconditionalJumpsAfter().get(b));
 						List<Action> elseActions = actions.subList(handle.getJumpPointer(), elseEndsPointer);
 						IfElsePattern ifElsePattern = new IfElsePattern(ifActions, elseActions);
 						context.getPatterns().put(handle.getIf().getLabel(), ifElsePattern );
 						
 						// re-scan if and else clause
 						analyzeNewBlock(handle.getPointer()+1, handle.getJumpPointer()-2, ifElsePattern);
 						analyzeNewBlock(handle.getJumpPointer(), elseEndsPointer-1, ifElsePattern);
 						
 						return elseEndsPointer;
 					}  else {
 						throw new IllegalArgumentException("Tricky if-else...");
 					}
 				}
 				
 				if (!isIfElse) {
 					// PATTERN: SWITCH
 					return analyzeSwitch(handle,info);
 				} else {
 					throw new IllegalArgumentException("Tricky if-else...");
 				}
 				
 			} else {
 				// PATTERN: IF
 				logger.debug("No jumps outside the block, setting target to IF.");
 				List<Action> ifActions = actions.subList(handle.getPointer()+1, handle.getJumpPointer());
 				IfPattern ifPattern = new IfPattern( ifActions );
 				context.getPatterns().put(handle.getIf().getLabel(), ifPattern	);
 				
 				// re-scan if
 				analyzeNewBlock(handle.getPointer()+1, handle.getJumpPointer()-1, ifPattern);
 				
 				return handle.getJumpPointer();
 			}
 			
 			
 		}
 		
 		
 		
 	}
 
 //	private void setBreaksForEnumerate(List<Action> forInActions) {
 //		if (!forInActions.isEmpty()) {
 //			Action firstAction = doWhilePattern.getActions().get(0);
 //			Action lastAction = doWhilePattern.getActions().get( doWhilePattern.getActions().size()-1 );
 //		}
 //		
 //	}
 
 	private void setBreaksForDoWhile(List<Action> actions) {
 		if (!actions.isEmpty()) {
 			Action firstAction = actions.get(0);
 			Action lastAction = actions.get( actions.size()-1 );
 			for (Action action : actions) {
 				if (action instanceof Jump) {
 					Action jmpAction = context.getLabels().get( ((Jump)action).getBranchLabel() );
 					if (jmpAction == firstAction) {
 						// PATTERN: CONTINUE
 						logger.debug("Setting "+action.getLabel()+" to CONTINUE");
 						context.getPatterns().put(action.getLabel(), new ContinuePattern());
 					}
 					
 					if (jmpAction.getOffset() > lastAction.getOffset()) {
 						// PATTERN: BREAK
 						logger.debug("Setting "+action.getLabel()+" to BREAK");
						context.getPatterns().put(action.getLabel(), new BreakPattern());
 					}
 					
 				}
 			}
 		}
 		
 	}
 
 	private void setBreaks(WhilePattern whilePattern, PatternJumpsInfo info) {
 		for (Branch jumpBefore : info.getAllJumpsBefore().keySet()) {
 			if (jumpBefore != info.getLatestJumpBefore()) {
 				// PATTERN: CONTINUE
 				logger.debug("Setting "+jumpBefore.getLabel()+" to CONTINUE");
 				context.getPatterns().put(jumpBefore.getLabel(), new ContinuePattern());
 			}
 		}
 		for (Branch jumpEnd : info.getUnconditionalEndJumps().keySet()) {
 			// PATTERN: BREAK
 			logger.debug("Setting "+jumpEnd.getLabel()+" to BREAK");
 			context.getPatterns().put(jumpEnd.getLabel(), new BreakPattern());
 		}
 
 		
 	}
 
 	private PatternJumpsInfo getJumpsInfo(IfHandle handle) {
 		
 		int ifPointer = handle.getPointer();
 		int jumpPointer = handle.getJumpPointer();
 		
 		int startOffset = handle.getOffset();
 		int endOffset = handle.getJumpOffset();
 		
 		Map<String, Action> labels = context.getLabels();
 		
 		Map<Branch,Action> jumpsBefore = new LinkedHashMap<Branch,Action>();
 		Map<Branch,Action> conditionalJumpsAfter = new LinkedHashMap<Branch,Action>();
 		Map<Branch,Action> unconditionalJumpsAfter = new LinkedHashMap<Branch,Action>();
 		Map<Branch,Action> allJumpsAfter = new LinkedHashMap<Branch,Action>();
 		Map<Branch,Action> conditionalEndJumps = new LinkedHashMap<Branch,Action>();
 		Map<Branch,Action> unconditionalEndJumps= new LinkedHashMap<Branch,Action>();
 		Action latestJumpBefore = null;
 		for (int pointer = ifPointer+1; pointer<jumpPointer; pointer++) {
 			Action action = actions.get(pointer);
 			
 			// count only unlabeled jumps
 			if (context.getPatterns().containsKey(action.getLabel())) {
 				continue;
 			}
 			
 			if (action instanceof Jump) {
 				Jump a = (Jump) action;
 				Action jumpDestination = labels.get(a.getBranchLabel());
 				int jOffset = jumpDestination.getOffset();
 				
 				if (jOffset <= startOffset) {
 					jumpsBefore.put(a, jumpDestination);
 					latestJumpBefore = a;
 				}
 				if (jOffset == endOffset) {
 					unconditionalEndJumps.put(a, jumpDestination);
 				}
 				if (jOffset > endOffset) {
 					unconditionalJumpsAfter.put(a,jumpDestination);
 					allJumpsAfter.put(a,jumpDestination);
 				}
 				
 			}
 			
 			if (action instanceof If) {
 				If a = (If) action;
 				Action jumpDestination = labels.get(a.getBranchLabel());
 				if (ActionBlock.LABEL_END.equals(a.getBranchLabel())) {
 					jumpDestination = actions.get(jumpPointer);
 				}
 				int jOffset = jumpDestination.getOffset();
 				
 				if (jOffset <= startOffset) {
 					jumpsBefore.put(a, jumpDestination);
 					latestJumpBefore = a;
 				}
 				if (jOffset == endOffset) {
 					conditionalEndJumps.put(a, jumpDestination);
 				}
 				if (jOffset > endOffset) {
 					conditionalJumpsAfter.put(a,jumpDestination);
 					allJumpsAfter.put(a,jumpDestination);
 				}
 				
 			}
 		}
 		
 		PatternJumpsInfo info = new PatternJumpsInfo();
 		info.setAllJumpsBefore(jumpsBefore);
 		info.setConditionalJumpsAfter(conditionalJumpsAfter);
 		info.setUnconditionalJumpsAfter(unconditionalJumpsAfter);
 		info.setConditionalEndJumps(conditionalEndJumps);
 		info.setUnconditionalEndJumps(unconditionalEndJumps);
 		info.setLatestJumpBefore(latestJumpBefore);
 		info.setAllJumpsAfter(allJumpsAfter);
 		
 		return info;
 	}
 
 	private int analyzeJump(Jump action) {
 		Action jumpAction = context.getLabels().get(action.getBranchLabel());
 		
 		if (jumpAction.getOffset() > action.getOffset()) {
 			if (insideWhile()) {
 				logger.debug("Setting "+action.getLabel()+" to BREAK");
 				context.getPatterns().put(action.getLabel(), new BreakPattern());
 			} else {
 				logger.debug("Setting "+action.getLabel()+" to SkipPattern");
 				int startPointer = context.getActionPointerMap().get(action) + 1;
 				int endPointer = context.getActionPointerMap().get(jumpAction);
 				context.getPatterns().put(action.getLabel(), new SkipPattern(actions.subList(startPointer, endPointer)));
 			}
 		}
 		
 		return context.getActionPointerMap().get( action ) + 1; // next action
 		
 	}
 
 	public void analyze(boolean discardAllPatterns) throws PatternAnalyzerException {
 		for (int j=startPointer; j<=endPointer; j++) {
 			Action action = actions.get(j);
 			// do not remove BreakPattern and ContinuePattern, they are set from outer block and shouldn't be removed
 			
 			if (context.getPatterns().containsKey(action.getLabel()) 
 					&& !(context.getPatterns().get(action.getLabel()) instanceof BreakPattern 
 							|| context.getPatterns().get(action.getLabel()) instanceof ContinuePattern 
 							|| (insideDoWhile() && j==startPointer && context.getPatterns().get(action.getLabel()) instanceof DoWhilePattern) )
 							) {
 				context.getPatterns().remove(action.getLabel());
 			}
 		}
 		analyze();
 	}
 	
 //	private boolean insideWhile() {
 //		return !context.getStack().isEmpty() && context.getStack().peek().getClass().equals( WhilePattern.class );
 //	}
 	private boolean insideWhile() {
 		boolean insideWhile = false;
 		for (int j=context.getStack().size()-1; j>=0; j--) {
 			Pattern p = context.getStack().get(j);
 			if (p.getClass().equals( WhilePattern.class )) {
 				insideWhile = true;
 				break;
 			}
 		}
 		return insideWhile;
 	}
 	
 	private boolean insideDoWhile() {
 		return !context.getStack().isEmpty() && context.getStack().peek().getClass().equals( DoWhilePattern.class );
 	}
 	
 	private boolean insideEnumerate() {
 		return !context.getStack().isEmpty() && context.getStack().peek().getClass().equals( ForInPattern.class );
 	}
 	
 	private boolean insideLoop() {
 		return insideWhile() || insideDoWhile();
 	}
 	
 	private int analyzeSwitch(IfHandle handle, PatternJumpsInfo info) throws PatternAnalyzerException {
 		If ifAction = handle.getIf();
 		Map<String, Pattern> patterns = context.getPatterns();
 		Map<String, Action> labels = context.getLabels();
 		Map<Action, Integer> actionPointerMap = context.getActionPointerMap();
 		
 		boolean jmpIsTheLastAction = false;
 		if (info.getUnconditionalJumpsAfter().size() == 1) {
 			Action jumpAfterAction = info.getUnconditionalJumpsAfter().keySet().iterator().next();
 			if (jumpAfterAction == actions.get(handle.getJumpPointer()-1)) {
 				jmpIsTheLastAction = true;
 			}
 		}
 		
 		if (jmpIsTheLastAction) {
 			logger.debug("Check 1 passed. 1 jmp at the end of the block...");
 			
 			// check all conditional jumps, there should be EQUALS action before each
 			boolean eqTest = true;
 			for (Map.Entry<Branch, Action> cJmpAfterEntry : info.getConditionalJumpsAfter().entrySet()) {
 				int conditionalJumpPointer = context.getActionPointerMap().get(cJmpAfterEntry.getKey());
 				if (conditionalJumpPointer>=1) {
 					Action prevAction = actions.get(conditionalJumpPointer-1);
 					if (prevAction instanceof Equals || prevAction instanceof Equals2 || prevAction instanceof StrictEquals) {
 						// continue
 					} else {
 						eqTest = false;
 						break;
 					}
 				} else {
 					eqTest = false;
 					break;
 				}
 			}
 			
 			if (eqTest) {
 				logger.debug("Check 2 passed. EQ... Setting to SWITCH to "+ifAction.getLabel());
 				
 				
 				Jump unconditionalJump = (Jump) info.getUnconditionalJumpsAfter().keySet().iterator().next();
 				
 				logger.debug("Setting SWITCH-SKIP pattern to "+unconditionalJump.getLabel());
 				patterns.put(unconditionalJump.getLabel(), new SwitchSkipPattern());
 				
 				Action endOfSwitchAction = labels.get( unconditionalJump.getBranchLabel() );
 				
 				//
 				// check switch conditions actions
 				//
 				
 				List<Branch> jumps = new ArrayList<Branch>();
 				jumps.add( ifAction );
 				jumps.addAll( info.getConditionalJumpsAfter().keySet() );
 				
 				List<List<Action>> conditionBlocks = new ArrayList<List<Action>>();
 				logger.debug("Condition blocks:");
 				for (int j=0; j<jumps.size()-1; j++) {
 					Branch a = jumps.get(j);
 					int startPointer = actionPointerMap.get(a) + 1;
 					int endPointer = actionPointerMap.get(jumps.get(j+1)) - 1;
 					logger.debug(startPointer + "("+offsetFormat.sprintf( actions.get(startPointer).getOffset() )
 							+") --- "+endPointer+"("+offsetFormat.sprintf( actions.get(endPointer).getOffset() )+")");
 					
 					conditionBlocks.add(actions.subList(startPointer, endPointer+1));
 				}
 
 				for (Branch brnch : jumps) {
 					logger.debug("Setting SWITCH-SKIP pattern to "+brnch.getLabel());
 					patterns.put(brnch.getLabel(), new SwitchSkipPattern());
 				}
 				
 				//
 				// check switch statements
 				//
 				List<Action> jumpDestinations = new ArrayList<Action>();
 				jumpDestinations.add( labels.get( ifAction.getBranchLabel() ) );
 				jumpDestinations.addAll( info.getConditionalJumpsAfter().values() );
 				
 				List<List<Action>> switchBlocks = new ArrayList<List<Action>>();
 				logger.debug("Switch blocks:");
 				Action realEndOfSwitch = endOfSwitchAction; // if there is a default block, this should be after the endOfSwitchAction 
 				boolean hasDefaultBlock = false; 
 				for (int j=0; j<jumpDestinations.size(); j++) {
 					Action a = jumpDestinations.get(j);
 					int startPointer = actionPointerMap.get(a);
 					int endPointer = (j<jumpDestinations.size()-1) ? actionPointerMap.get(jumpDestinations.get(j+1)) - 1 : actionPointerMap.get( endOfSwitchAction ) - 1;
 					logger.debug(startPointer + "("+offsetFormat.sprintf( actions.get(startPointer).getOffset() )
 							+") --- "+endPointer+"("+offsetFormat.sprintf( actions.get(endPointer).getOffset() )+")");
 					
 					// analyze the last action in switch statement block
 					if (actions.get(endPointer) instanceof Jump) {
 						Jump jump = (Jump) actions.get(endPointer);
 						if (labels.get(jump.getBranchLabel()) == endOfSwitchAction) {
 							patterns.put(jump.getLabel(), new BreakPattern());
 						} else {
 							int jOffset = labels.get(jump.getBranchLabel()).getOffset();
 							if (jOffset > endOfSwitchAction.getOffset()) {
 								hasDefaultBlock = true;
 								if (realEndOfSwitch != null) {
 									if (jOffset > realEndOfSwitch.getOffset()) {
 										realEndOfSwitch = labels.get(jump.getBranchLabel());
 									}
 								} else {
 									realEndOfSwitch = labels.get(jump.getBranchLabel());
 								}
 							} else {
 								// jump to the next switch state, NO "break" at the end of this switch state
 							}
 							
 						}
 						
 					}
 					switchBlocks.add(actions.subList(startPointer, endPointer+1));
 				}
 				
 				
 
 				logger.debug("Has default block: "+hasDefaultBlock);
 				logger.debug("End 1: "+getActionDebugString(endOfSwitchAction));
 				logger.debug("End 2: "+(realEndOfSwitch != null ? getActionDebugString(realEndOfSwitch) : "null"));
 				List<Action> defaultActions = new ArrayList<Action>();
 				if (hasDefaultBlock && realEndOfSwitch!=null && realEndOfSwitch.getOffset() > endOfSwitchAction.getOffset()) {
 					defaultActions = actions.subList(actionPointerMap.get(endOfSwitchAction), actionPointerMap.get(realEndOfSwitch));
 				}
 
 				int switchPatternSize = actionPointerMap.get( realEndOfSwitch ) - actionPointerMap.get( ifAction ) - 1;
 				
 				SwitchPattern switchPattern = new SwitchPattern(conditionBlocks,switchBlocks,defaultActions,switchPatternSize);
 				patterns.put( ifAction.getLabel(), switchPattern );
 				
 				logger.debug("Re-scanning switch blocks...");
 				for (List<Action> switchActionBlock : switchBlocks) {
 					if (!switchActionBlock.isEmpty()) {
 						int actionBlockStartPointer = context.getActionPointerMap().get(switchActionBlock.get(0));
 						int actionBlockEndPointer = context.getActionPointerMap().get(switchActionBlock.get(switchActionBlock.size()-1));
 						analyzeNewBlock(actionBlockStartPointer, actionBlockEndPointer, switchPattern);
 					}
 				}
 				
 				if (hasDefaultBlock && !defaultActions.isEmpty()) {
 					logger.debug("Re-scanning default block");
 					analyzeNewBlock(context.getActionPointerMap().get(endOfSwitchAction), context.getActionPointerMap().get(realEndOfSwitch), switchPattern);
 				}
 				
 				// mark all jumps inside switch block as switch-skip pattern
 //				int startP = actionPointerMap.get( ifAction );
 //				int endP = actionPointerMap.get( realEndOfSwitch );
 //				for (int j=startP; j<endP; j++) {
 //					Action a = actions.get(j);
 //					if (a instanceof Branch) {
 //						Branch b = (Branch) a;
 //						if (!patterns.containsKey(b.getLabel())) {
 //							logger.debug("Added SWITCH-SKIP pattern for "+b.getLabel());
 //							patterns.put(b.getLabel(), new SwitchSkipPattern());
 //						}
 //					}
 //				}
 				
 				
 //				throw new RuntimeException("No handle");
 				
 				return actionPointerMap.get( realEndOfSwitch );
 				
 			} // eqTest
 			
 			
 		} // jmpIsTheLastAction
 		
 		throw new IllegalArgumentException("Unknown branch type, possibly tricky if-else...");
 	}
 	
 //	private void analyzeNewBlock(List<Action> actions, Pattern pattern) {
 //		context.getStack().push(pattern);
 //		new PatternAnalyzerEx(context,actions).analyze(true);
 //		context.getStack().pop();
 //	}
 	private void analyzeNewBlock(int startPointer, int endPointer, Pattern pattern) throws PatternAnalyzerException {
 		String blockDumpString = offsetFormat.sprintf(actions.get(startPointer).getOffset())+" - "+offsetFormat.sprintf(actions.get(endPointer).getOffset());
 		logger.debug("Re-scanning actions "+blockDumpString);
 		context.getStack().push(pattern);
 		try {
 			new PatternAnalyzerEx(this.context,this.actions,startPointer,endPointer).analyze(true);
 		} catch (LabelsInitException e) {
 			throw new PatternAnalyzerException("Error while re-scanning block "+blockDumpString);
 		}
 		context.getStack().pop();
 	}
 	
 	private String getActionDebugString(Action action) {
 		String actionInfo = "";
 		if (action instanceof Branch) {
 			Branch brnch = (Branch) action;
 			Action jumpDestination = context.getLabels().get(brnch.getBranchLabel());
 			actionInfo += brnch.getBranchLabel() +"("+ ( jumpDestination!=null ? offsetFormat.sprintf( jumpDestination.getOffset() ) : "[null]")+")";
 			actionInfo += ", branch offset = "+brnch.getBranchOffset();
 		}
 		return actionFormat.sprintf(new Object[]{
 				action.getOffset(),
 				(context.getPatterns().containsKey(action.getLabel())) ? context.getPatterns().get(action.getLabel()).getClass().getSimpleName() : "",
 				(action.getLabel()==null)?"":action.getLabel(),
 				action.getCode(),
 				ActionConstants.getActionName(action.getCode()),
 				actionInfo});
 	}
 	
 	public void clearBranchPattern(String label) {
 		context.getPatterns().remove(label);
 	}
 	
 	public Pattern getPatternByLabel(String label) {
 		return context.getPatterns().get(label);
 	}
 	
 	public boolean isRootBlock() {
 		return context.getStack().isEmpty();
 	}
 	
 	public Map<String, Action> getLabels() {
 		return context.getLabels();
 	}
 	
 	private boolean jumpOutsideLoop(int pointer) {
 		boolean r = false;
 		for (int j=context.getStack().size()-1; j>=0; j--) {
 			Pattern p = context.getStack().get(j);
 			if (p instanceof WhilePattern) {
 				WhilePattern whilePattern = (WhilePattern) p;
 				Action lastAction = whilePattern.getActions().get(whilePattern.getActions().size()-1);
 				int whileEnd = context.getActionPointerMap().get( lastAction ) + 2;
 				if (pointer == whileEnd) {
 					r = true;
 					break;
 				} else {
 					r = false;
 					break;
 				}
 			}
 		}
 		
 		return r;
 	}
 	
 	private boolean isNestedTarget() {
 		boolean isNested = false;
 		for (int j=context.getStack().size()-1; j>=0; j--) {
 			Pattern p = context.getStack().get(j);
 			if (p instanceof TellTargetPattern) {
 				isNested = true;
 				break;
 			}
 		}
 		
 		return isNested;
 	}
 	
 	private int analyzeSetTarget(Action t) throws PatternAnalyzerException {
 		logger.debug("Analyzing set-target "+t);
 		Stack<Action> targets = new Stack<Action>();
 		targets.push(t);
 		
 		int setTargetPointer = context.getActionPointerMap().get(t);
 		int setTragetEndPointer = -1;
 		for (int pointer = setTargetPointer+1; pointer<=endPointer;) {
 			Action action = actions.get(pointer);
 			if (action instanceof SetTarget) {
 				SetTarget setTarget = (SetTarget) action;
 				if ("".equals(setTarget.getName())) {
 					// end of target
 					targets.pop();
 					boolean endOfNestedTarget = !targets.isEmpty();
 					
 					if (endOfNestedTarget) {
 						pointer++;
 					} else {
 						// this is our target
 						logger.debug("Setting target to TELLTARGET");
 						setTragetEndPointer = pointer;
 						break;
 					}
 				} else {
 					targets.push(action);
 				}
 			} else if (action instanceof SetTarget2) {
 				targets.push(action);
 			}
 			
 			pointer++;
 			
 		}
 		
 		if (setTragetEndPointer!=-1) {
 			TellTargetPattern tellTargetPattern = new TellTargetPattern(actions.subList(setTargetPointer+1, setTragetEndPointer), isNestedTarget());
 			context.getPatterns().put(t.getLabel(), tellTargetPattern);
 			
 			analyzeNewBlock(setTargetPointer+1, setTragetEndPointer-1, tellTargetPattern);
 			
 			return setTragetEndPointer+1;
 		} else {
 			logger.error("No end of tellTarget found. Add all rest actions..");
 			List<Action> tellTargetActions = actions.subList(setTargetPointer+1, endPointer+1);
 			TellTargetPattern tellTargetPattern = new TellTargetPattern(tellTargetActions,isNestedTarget());
 			context.getPatterns().put(t.getLabel(), tellTargetPattern);
 			
 			analyzeNewBlock(setTargetPointer+1, endPointer, tellTargetPattern);
 			
 			return endPointer+1;
 		}
 		
 	}
 	
 }
