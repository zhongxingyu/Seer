 package core.common.cfg.javacfg;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.Assignment;
 import org.eclipse.jdt.core.dom.Block;
 import org.eclipse.jdt.core.dom.BreakStatement;
 import org.eclipse.jdt.core.dom.CatchClause;
 import org.eclipse.jdt.core.dom.ContinueStatement;
 import org.eclipse.jdt.core.dom.DoStatement;
 import org.eclipse.jdt.core.dom.EmptyStatement;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.ExpressionStatement;
 import org.eclipse.jdt.core.dom.ForStatement;
 import org.eclipse.jdt.core.dom.IfStatement;
 import org.eclipse.jdt.core.dom.InfixExpression;
 import org.eclipse.jdt.core.dom.LabeledStatement;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.NumberLiteral;
 import org.eclipse.jdt.core.dom.ReturnStatement;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
 import org.eclipse.jdt.core.dom.SwitchCase;
 import org.eclipse.jdt.core.dom.SwitchStatement;
 import org.eclipse.jdt.core.dom.ThrowStatement;
 import org.eclipse.jdt.core.dom.TryStatement;
 import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
 import org.eclipse.jdt.core.dom.WhileStatement;
 import org.eclipse.jdt.core.dom.InfixExpression.Operator;
 
 import core.common.cfg.interfaces.IBasicBlock;
 import core.common.cfg.interfaces.IBranchNode;
 import core.common.cfg.interfaces.ICfgData;
 import core.common.cfg.interfaces.IConnectorNode;
 import core.common.cfg.interfaces.IDecisionNode;
 import core.common.cfg.interfaces.IExitNode;
 import core.common.cfg.interfaces.IJumpNode;
 import core.common.cfg.interfaces.ISingleOutgoing;
 import core.common.cfg.interfaces.IStartNode;
 import core.common.cfg.model.AbstractBasicBlock;
 import core.common.cfg.model.JumpNode;
 import core.common.model.jobflow.JobConst;
 import core.common.util.Abacus;
 
 public class ControlFlowGraphBuilder {
 	JavaStartNode start;
 	Collection<IExitNode> exits;
 	Collection<IBasicBlock> dead;
 	JavaExitNode returnExit;
 	JavaNodeFactory factory = new JavaNodeFactory();
 	IConnectorNode outerBreak;
 	IConnectorNode outerContinue;
 	HashMap<String, IBasicBlock> labels = new HashMap<String, IBasicBlock>(0);
 	
 	boolean cut = false;
 	// only deal with NumberLiteral field
 	private Map<String, Integer> fields = new HashMap<String, Integer>();
 
 	public JavaControlFlowGraph build(MethodDeclaration def, boolean cutUnaccessibleBranch) {
 		cut = cutUnaccessibleBranch;
 		return build(def);
 	}
 	
 	public JavaControlFlowGraph build(MethodDeclaration def) {
 		if(cut){
 			parseParameters(def.parameters());
 		}
 		
 		Block body = def.getBody();
 		start = new JavaStartNode();
 		exits = new ArrayList<IExitNode>();
 		dead = new ArrayList<IBasicBlock>();
 		IBasicBlock last = createSubGraph(start, body);
 		if (!(last instanceof IExitNode) && !deadConnector(last)) {
 			returnExit = factory.createExitNode(null);
 			returnExit.setStartNode(start);
 			addOutgoing(last, returnExit);
 			exits.add(returnExit);
 			if (dead.size() > 0) {
 				for (Iterator<IBasicBlock> iterator = dead.iterator(); iterator.hasNext();) {
 					IBasicBlock ds = iterator.next();
 					IBasicBlock dl = findLast(ds);
 					if (dl != null && dl.getOutgoingSize() == 0 && dl != returnExit) {
 						((AbstractBasicBlock) dl).addOutgoing(returnExit);
 					}
 				}
 			}
 		}
 		JavaControlFlowGraph graph = new JavaControlFlowGraph(start, exits);
 		graph.setUnconnectedNodes(dead);
 		return graph;
 	}
 
 	public IBasicBlock findLast(IBasicBlock node) {
 		if (node instanceof IJumpNode)
 			return null;
 		if (node.getOutgoingSize() == 0)
 			return node;
 		if (node instanceof ISingleOutgoing) {
 			return findLast(((ISingleOutgoing) node).getOutgoing());
 		} else if (node instanceof IDecisionNode) {
 			return findLast(((IDecisionNode) node).getMergeNode().getOutgoing());
 		}
 		return node;
 	}
 	
 	private boolean deadConnector(IBasicBlock conn) {
 		if (conn instanceof IJumpNode || conn instanceof IConnectorNode) {
 			if (conn.getIncomingSize() == 0) {
 				return true;
 			}
 			if (conn instanceof IJumpNode) {
 				IJumpNode jm = (IJumpNode) conn;
 				if (jm.isBackwardArc())
 					return false;
 			}
 			IBasicBlock[] conns = conn.getIncomingNodes();
 			for (int i = 0; i < conns.length; i++) {
 				IBasicBlock bb = conns[i];
 				if (!deadConnector(bb))
 					return false;
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	private IBasicBlock createSubGraph(IBasicBlock prev, ASTNode body) {
 		if (body instanceof Block) {
 			Block block = (Block) body;
 			List children = block.statements();
 			for (int i = 0; i < children.size(); i++) {
 				ASTNode node = (ASTNode) children.get(i);
 				IBasicBlock last = createSubGraph(prev, node);
 				prev = last;
 			}
 		} 
 		/**
 		 * VariableDeclarationStatement 
 		 * deals with statement like " int x = a + b; "
 		 */
 		else if (body instanceof VariableDeclarationStatement){		
 			if(cut){
 				parseField((VariableDeclarationStatement)body);
 			}
 			
 			JavaPlainNode node = factory.createPlainNode(body);
 			addOutgoing(prev, node);
 			return node;
 		} 
 		/**
 		 * ExpressionStatement 
 		 * deals with statement like " x = a + b; "
 		 * 
 		 * NOTE: this doesn't care about Variable declaration
 		 */
 		else if (body instanceof ExpressionStatement || body instanceof TypeDeclarationStatement || body instanceof EmptyStatement) {
 			if (isThrowStatement(body)) {
 				JavaExitNode node = createExitNode(prev, body);
 				return node;
 			}
 			
 			if(cut){
 				parseField((ExpressionStatement)body);
 			}
 			
 			JavaPlainNode node = factory.createPlainNode(body);
 			addOutgoing(prev, node);
 			return node;
 		} else if (body instanceof IfStatement) {
 			return createIf(prev, (IfStatement) body);
 		} else if (body instanceof WhileStatement) {
 			return createWhile(prev, (WhileStatement) body);
 		} else if (body instanceof ForStatement) {
 			return createFor(prev, (ForStatement) body);
 		} else if (body instanceof DoStatement) {
 			return createDoWhile(prev, (DoStatement) body);
 		} else if (body instanceof ReturnStatement) {
 			JavaExitNode node = createExitNode(prev, body);
 			return node;
 		} else if (body instanceof BreakStatement) {
 			if (outerBreak != null)
 				return addJump(prev, outerBreak);
 			return prev;
 		} else if (body instanceof ContinueStatement) {
 			if (outerContinue != null)
 				return addJump(prev, outerContinue);
 			return prev;
 		} else if (body instanceof SwitchStatement) {
 			return createSwitch(prev, (SwitchStatement) body);
 		} else if (body instanceof LabeledStatement) {
 			LabeledStatement ast = (LabeledStatement) body;
 			String labelName = ast.getLabel().getFullyQualifiedName();
 			IBranchNode labNode = (IBranchNode) labels.get(labelName);
 			IConnectorNode conn;
 			if (labNode != null) {
 				conn = (IConnectorNode) labNode.getOutgoing();
 				addOutgoing(prev, labNode);
 			} else {
 				// labeled statement contains of connector for jumps, branch for
 				// label
 				// and nested statement
 				conn = createLabelNodes(prev, labelName);
 			}
 			return createSubGraph(conn, ast.getBody());
 		} else if (body == null) {
 			// skip - sometimes body is empty such as no else
 		} else if (body instanceof TryStatement) {
 			return createTry(prev, (TryStatement) body);
 		} else {
 			System.err.println("unknown statement for cfg: " + body); 
 		}
 		return prev;
 	}
 
 	private IJumpNode addJump(IBasicBlock prev, IConnectorNode conn) {
 		return addJump(prev, conn, false);
 	}
 
 	private IJumpNode addJump(IBasicBlock prev, IConnectorNode conn, boolean backward) {
 		if (prev instanceof IJumpNode)
 			return (IJumpNode) prev;
 		if (prev instanceof IExitNode)
 			return null;
 		IJumpNode jump = factory.createJumpNode();
 		addOutgoing(prev, jump);
 		addOutgoing(jump, conn);
 		((JumpNode) jump).setBackward(backward);
 		return jump;
 	}
 
 	private void addOutgoing(IBasicBlock prev, IBasicBlock node) {
 		if (prev instanceof IExitNode || prev == null) {
 			dead.add(node);
 			return;
 		} else if (prev instanceof ICfgData) {
 			if (prev instanceof IDecisionNode && !(node instanceof IBranchNode)) {
 				dead.add(node);
 				return;
 			}
 			((AbstractBasicBlock) prev).addOutgoing(node);
 		}
 		if (!(node instanceof IStartNode))
 			((AbstractBasicBlock) node).addIncoming(prev);
 	}
 	
 	protected IBasicBlock createIf(IBasicBlock prev, IfStatement body) {
 		
 		JavaDecisionNode ifNode = factory.createDecisionNode(body.getExpression());
 		addOutgoing(prev, ifNode);
 		
 		IConnectorNode mergeNode = factory.createConnectorNode();
 		ifNode.setMergeNode(mergeNode);
 		
 		IBranchNode thenNode = factory.createBranchNode(IBranchNode.IF_THEN, body.getExpression());
 		addOutgoing(ifNode, thenNode);
 		IBasicBlock then = createSubGraph(thenNode, body.getThenStatement());
 		addJump(then, mergeNode);
 		
 		IBranchNode elseNode = factory.createBranchNode(IBranchNode.IF_ELSE, body.getExpression());
 		addOutgoing(ifNode, elseNode);
 		IBasicBlock els = createSubGraph(elseNode, body.getElseStatement());
 		addJump(els, mergeNode);
 		
 		return mergeNode;
 	}
 	
 	protected IBasicBlock createWhile(IBasicBlock prev, WhileStatement body) {
 		if(cut && !parseDecision(body.getExpression())){
 			return prev;
 		}
 		
 		// add continue connector
 		IConnectorNode nContinue = factory.createConnectorNode();
 		addOutgoing(prev, nContinue);
 		// decision node
 		JavaDecisionNode decision = factory.createDecisionNode(body.getExpression());
 		addOutgoing(nContinue, decision);
 		// add break connector
 		IConnectorNode nBreak = factory.createConnectorNode();
 		decision.setMergeNode(nBreak);
 		// create body and jump to continue node
 		IBranchNode loopStart = factory.createBranchNode(IBranchNode.WHILE_THEN, body.getExpression());
 		addOutgoing(decision, loopStart);
 		// set break/continue
 		IConnectorNode savedContinue = outerContinue;
 		IConnectorNode savedBreak = outerBreak;
 		outerContinue = nContinue;
 		outerBreak = nBreak;
 		IBasicBlock endBody = createSubGraph(loopStart, body.getBody());
 		// restore
 		outerContinue = savedContinue;
 		outerBreak = savedBreak;
 		// backward jump
 		addJump(endBody, nContinue, true);
 		// connect with else branch
 		IBranchNode loopEnd = factory.createBranchNode(IBranchNode.WHILE_ELSE, body.getExpression());
 		addOutgoing(decision, loopEnd);
 		addJump(loopEnd, nBreak);
 		return nBreak;
 	}
 
 	protected IBasicBlock createDoWhile(IBasicBlock prev, DoStatement body) {
 		// create body and jump to continue node
 		IConnectorNode loopStart = factory.createConnectorNode();
 		addOutgoing(prev, loopStart);
 		// continue/break
 		IConnectorNode nContinue = factory.createConnectorNode();
 		IConnectorNode nBreak = factory.createConnectorNode();
 		IConnectorNode savedContinue = outerContinue;
 		IConnectorNode savedBreak = outerBreak;
 		outerContinue = nContinue;
 		outerBreak = nBreak;
 		IBasicBlock endBody = createSubGraph(loopStart, body.getBody());
 		// restore
 		outerContinue = savedContinue;
 		outerBreak = savedBreak;
 		// add continue connector
 		addOutgoing(endBody, nContinue);
 		// decision node
 		JavaDecisionNode decision = factory.createDecisionNode(body.getExpression());
 		addOutgoing(nContinue, decision);
 		// then branch
 		IBranchNode thenNode = factory.createBranchNode(IBranchNode.WHILE_THEN, body.getExpression());
 		addOutgoing(decision, thenNode);
 		IJumpNode jumpToStart = factory.createJumpNode();
 		addOutgoing(thenNode, jumpToStart);
 		((JumpNode) jumpToStart).setBackward(true);
 		// connect with backward link
 		addOutgoing(jumpToStart, loopStart);
 		// connect with else branch
 		IBranchNode loopEnd = factory.createBranchNode(IBranchNode.WHILE_ELSE, body.getExpression());
 		addOutgoing(decision, loopEnd);
 		// add break connector
 		decision.setMergeNode(nBreak);
 		addJump(loopEnd, nBreak);
 		return nBreak;
 	}
 	
 	private IBasicBlock createFor(IBasicBlock prev, ForStatement forNode) {
 		// add initializer
 		List initializers = forNode.initializers();
 		for (int i = 0; i < initializers.size(); i++) {
 			JavaPlainNode init = factory.createPlainNode((ASTNode) initializers.get(i));
 			addOutgoing(prev, init);
 			prev = init;
 		}
 		
 		// add continue connector
 		IConnectorNode beforeCheck = factory.createConnectorNode();
 		addOutgoing(prev, beforeCheck);
 		// decision node
 		JavaDecisionNode decision = factory.createDecisionNode(forNode.getExpression());
 		addOutgoing(beforeCheck, decision);
 		// add break connector
 		IConnectorNode nBreak = factory.createConnectorNode();
 		decision.setMergeNode(nBreak);
 		// create body and jump to continue node
 		IBranchNode loopStart = factory.createBranchNode(IBranchNode.WHILE_THEN, forNode.getExpression());
 		addOutgoing(decision, loopStart);
 		// set break/continue
 		IConnectorNode nContinue = factory.createConnectorNode();
 		IConnectorNode savedContinue = outerContinue;
 		IConnectorNode savedBreak = outerBreak;
 		outerContinue = nContinue;
 		outerBreak = nBreak;
 		IBasicBlock endBody = createSubGraph(loopStart, forNode.getBody());
 		outerContinue = savedContinue;
 		outerBreak = savedBreak;
 
 		List updates = forNode.updaters();
 		JavaPlainNode firstNode = null, endNode = null;
 		JavaPlainNode prevNode = null, tempNode = null;
 		for (int i = 0; i < updates.size(); i++) {
 			if(i==0){
 				firstNode = factory.createPlainNode((ASTNode)updates.get(i));
 				prevNode = firstNode;
 				continue;
 			}else if(i==updates.size()-1){
 				endNode = factory.createPlainNode((ASTNode)updates.get(i));
 				break;
 			}else{
 				tempNode  = factory.createPlainNode((ASTNode)updates.get(i));
 				addOutgoing(prevNode, tempNode);
 				prev = tempNode;
 			}
 		}
 		
 		addOutgoing(endBody, nContinue);
 		addOutgoing(nContinue, firstNode);
 		// connect with backward link
 		addJump(endNode, beforeCheck, true);
 		// add "else" branch
 		IBranchNode loopEnd = factory.createBranchNode(IBranchNode.WHILE_ELSE, forNode.getExpression());
 		addOutgoing(decision, loopEnd);
 		addJump(loopEnd, nBreak);
 		return nBreak;
 	}
 	
 	protected JavaExitNode createExitNode(IBasicBlock prev, ASTNode body) {
 		JavaExitNode node = factory.createExitNode(body);
 		node.setStartNode(start);
 		addOutgoing(prev, node);
 		exits.add(node);
 		return node;
 	}
 	
 	private IBasicBlock createSwitch(IBasicBlock prev, SwitchStatement body) {
 		JavaDecisionNode node = factory.createDecisionNode(body.getExpression());
 		addOutgoing(prev, node);
 		IConnectorNode conn = factory.createConnectorNode();
 		node.setMergeNode(conn);
 		createSwitchBody(node, conn, body);
 		return conn;
 	}
 	
 	private void createSwitchBody(JavaDecisionNode switchNode, IConnectorNode mergeNode, SwitchStatement body) {
 		List children = body.statements();
 		if(children == null || children.size() == 0){
 			return;
 		}
 		
 		IBasicBlock prev = switchNode;
 		for (int i = 0; i < children.size(); i++) {
 			ASTNode elem = (ASTNode) children.get(i);
 			if (elem instanceof SwitchCase) {
 				IBranchNode lbl = null;
 				if (!((SwitchCase) elem).isDefault()) {
 					SwitchCase caseSt = (SwitchCase) elem;
 					lbl = factory.createBranchNode(caseSt);
 				} else {
 					lbl = factory.createBranchNode(IBranchNode.DEFAULT);
 				}
 				if (!(prev instanceof IExitNode) && prev != switchNode) {
 					IConnectorNode here = factory.createConnectorNode();
 					addJump(prev, here);
 					addOutgoing(lbl, here);
 					prev = here;
 				} else {
 					prev = lbl;
 				}
 				addOutgoing(switchNode, lbl);
 				continue;
 			}
 			if (elem instanceof BreakStatement) {
 				prev = addJump(prev, mergeNode);
 				continue;
 			}
 			IBasicBlock last = createSubGraph(prev, elem);
 			prev = last;
 		}
 		addJump(prev, mergeNode);
 	}
 	
 	protected IConnectorNode createLabelNodes(IBasicBlock prev, String labelName) {
 		IBranchNode branch = factory.createBranchNode(labelName);
 		if (prev != null)
 			addOutgoing(prev, branch);
 		labels.put(labelName, branch);
 		IConnectorNode conn = factory.createConnectorNode();
 		addOutgoing(branch, conn);
 		return conn;
 	}
 	
 	private boolean isThrowStatement(ASTNode body) {
 		if(body instanceof ThrowStatement){
 			return true;
 		}
 		return false;
 	}
 
 	private IBasicBlock createTry(IBasicBlock prev, TryStatement body) {
 		JavaDecisionNode ifNode = factory.createDecisionNode(body);
 		addOutgoing(prev, ifNode);
 		IConnectorNode mergeNode = factory.createConnectorNode();
 		ifNode.setMergeNode(mergeNode);
 		IBranchNode thenNode = factory.createBranchNode(IBranchNode.IF_THEN);
 		addOutgoing(ifNode, thenNode);
 		IBasicBlock then = createSubGraph(thenNode, body.getBody());
 		addJump(then, mergeNode);
 		List catchHandlers = body.catchClauses();
 		for (int i = 0; i < catchHandlers.size(); i++) {
 			CatchClause handler = (CatchClause) catchHandlers.get(i);
 			IBranchNode handlerNode = factory.createBranchNode(handler.getException());
 			addOutgoing(ifNode, handlerNode);
 			IBasicBlock els = createSubGraph(handlerNode, handler.getBody());
 			addJump(els, mergeNode);
 		}
 		return mergeNode;
 	}
 	
 
 	private void parseField(VariableDeclarationStatement body) {
 		// deal with int field only
 		if(!body.getType().toString().equals(JobConst.INT)){
 			return;
 		}
 		
 		@SuppressWarnings("unchecked")
 		List<VariableDeclarationFragment> fragments = body.fragments();
 		for(int i=0;i< fragments.size();i++){
 			SimpleName var = fragments.get(i).getName();
 			Expression exp = fragments.get(i).getInitializer();
 			int value = Abacus.computeExpression(exp, fields);
 			fields.put(var.toString(), value);
 		}
 	}
 	
 	private void parseField(ExpressionStatement body){
 		Expression exp = body.getExpression();
 		if(exp instanceof Assignment){
 			Assignment assignment = (Assignment) exp;
 			Expression var = assignment.getLeftHandSide();
			if(fields.get(var.toString()) == null){
				return;
			}
 			Expression rightHandExp = assignment.getRightHandSide();
 			int value = Abacus.computeExpression(rightHandExp, fields);
 			fields.put(var.toString(), value);
 		}
 	}
 
 	private void parseParameters(List<SingleVariableDeclaration> parameters) {
 		for(SingleVariableDeclaration declaration: parameters){
 			if(!declaration.getType().toString().equals(JobConst.INT)){
 				continue;
 			}
 			fields.put(declaration.getName().toString(), 0);
 		}
 	}
 	
 	private boolean parseDecision(Expression exp) {
 		if(exp instanceof InfixExpression){
 			Expression left = ((InfixExpression) exp).getLeftOperand();
 			Expression right = ((InfixExpression) exp).getRightOperand();
 			
 			int leftVal = Abacus.computeExpression(left, fields);
 			int rightVal = Abacus.computeExpression(right, fields);
 			
 			Operator op = ((InfixExpression) exp).getOperator();
 			
 			return Abacus.compareValue(op, leftVal, rightVal);
 		}
 		return false;
 	}
 }
