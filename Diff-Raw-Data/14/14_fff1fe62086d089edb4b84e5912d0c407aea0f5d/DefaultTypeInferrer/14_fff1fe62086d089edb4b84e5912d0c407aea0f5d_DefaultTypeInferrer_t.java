 package org.rubypeople.rdt.internal.ti;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.jruby.ast.ArgsNode;
 import org.jruby.ast.ArgumentNode;
 import org.jruby.ast.CallNode;
 import org.jruby.ast.ConstNode;
 import org.jruby.ast.DefnNode;
 import org.jruby.ast.DefsNode;
 import org.jruby.ast.GlobalAsgnNode;
 import org.jruby.ast.GlobalVarNode;
 import org.jruby.ast.InstAsgnNode;
 import org.jruby.ast.InstVarNode;
 import org.jruby.ast.ListNode;
 import org.jruby.ast.LocalAsgnNode;
 import org.jruby.ast.LocalVarNode;
 import org.jruby.ast.Node;
 import org.jruby.ast.RootNode;
 import org.rubypeople.rdt.internal.core.parser.RubyParser;
 import org.rubypeople.rdt.internal.ti.data.ConstNodeTypeNames;
 import org.rubypeople.rdt.internal.ti.data.TypicalMethodReturnNames;
 import org.rubypeople.rdt.internal.ti.util.FirstPrecursorNodeLocator;
 import org.rubypeople.rdt.internal.ti.util.INodeAcceptor;
 import org.rubypeople.rdt.internal.ti.util.OffsetNodeLocator;
 
 public class DefaultTypeInferrer implements ITypeInferrer {
 
 	private RootNode rootNode;
 
 	/**
 	 * Infers type inside the source at given offset.
 	 * 
 	 * @return List of ITypeGuess objects.
 	 */
 	public List<ITypeGuess> infer(String source, int offset) {
 		RubyParser parser = new RubyParser();
 		rootNode = (RootNode) parser.parse(source);
 		Node node = OffsetNodeLocator.Instance().getNodeAtOffset(rootNode.getBodyNode(), offset);
 
 		if (node == null) {
 			return null;
 		}
 		return infer(node);
 	}
 
 	/**
 	 * Infers the type of the specified node.
 	 * 
 	 * @param node
 	 *            Node to infer type of.
 	 * @return List of ITypeGuess objects.
 	 */
 	private List<ITypeGuess> infer(Node node) {
 		List<ITypeGuess> guesses = new LinkedList<ITypeGuess>();
 		tryConstantNode(node, guesses);
 		tryAsgnNode(node, guesses);
 
		// TODO refactor these 3 by common features into 1 (or 1+3) method(s)
 		tryLocalVarNode(node, guesses);
 		tryInstVarNode(node, guesses);
 		tryGlobalVarNode(node, guesses);
 
 		tryWellKnownMethodCalls(node, guesses);
 		if (node instanceof ConstNode) { // if this is a constant, it may be the type name!
 			guesses.add(new BasicTypeGuess(((ConstNode)node).getName(), 100));
 		}
		if (guesses.isEmpty()) { // if we have no guesses..
			if (node instanceof CallNode) { // and it's a method call, try inferring receiver type
				CallNode call = (CallNode) node;
				return infer(call.getReceiverNode());
			}
		}
 		return guesses;
 	}
 
 	/**
 	 * Infers type if node is a constant node; i.e. 5, 'foo', [1,2,3]
 	 * 
 	 * @param node
 	 *            Node to infer type of.
 	 * @param guesses
 	 *            List of ITypeGuess objects to insert guesses into.
 	 */
 	private void tryConstantNode(Node node, List<ITypeGuess> guesses) {
 		// Try seeing if the rvalue is a constant (5, "foo", [1,2,3], etc.)
 		String concreteGuess = ConstNodeTypeNames.get(node.getClass().getSimpleName());
 		if (concreteGuess != null) {
 			guesses.add(new BasicTypeGuess(concreteGuess, 100));
 		}
 	}
 
 	/**
 	 * Infers type if node is an assignment node; i.e. x = 5,
 	 * 
 	 * @y = 'foo', $z = [1,2,3]
 	 * @param node
 	 *            Node to infer type of.
 	 * @param guesses
 	 *            List of ITypeGuess objects to insert guesses into.
 	 */
 	private void tryAsgnNode(Node node, List<ITypeGuess> guesses) {
 		Node valueNode = null;
 
 		if (node instanceof LocalAsgnNode) {
 			valueNode = ((LocalAsgnNode) node).getValueNode();
 		}
 		if (node instanceof InstAsgnNode) {
 			valueNode = ((InstAsgnNode) node).getValueNode();
 		}
 		if (node instanceof GlobalAsgnNode) {
 			valueNode = ((GlobalAsgnNode) node).getValueNode();
 		}
 		if (valueNode != null) {
 			guesses.addAll(infer(valueNode));
 		}
 	}
 
 	private void tryInstVarNode(Node node, List<ITypeGuess> guesses) {
 		if (node instanceof InstVarNode) {
 			final InstVarNode instVarNode = (InstVarNode) node;
 			int nodeStart = node.getPosition().getStartOffset();
 
 			// todo: see if there is attr_reader/attr_writer, maybe?
 			// todo: find calls to the reader/writers
 			// todo: for STI on InstVar, find references within this ClassNode
 			// to this InstVar... record 'em
 
 			// Find first assignment to this var name that occurs before the
 			// reference
 			// todo: This will find assignments in other local scopes that
 			// precede this reference but have the same variable name.
 			// To mitigate, ensure that the closest spanning ScopeNode for both
 			// this LocalVarNode and the AsgnNode are the name ScopeNode.
 			// Or scopingNode. Still not sure whether IterNodes count or not...
 			// silly block-local-var ambiguity ;)
 			Node initialAssignmentNode = FirstPrecursorNodeLocator.Instance().findFirstPrecursor(rootNode, nodeStart, new INodeAcceptor() {
 				public boolean doesAccept(Node node) {
 					String name = null;
 					if (node instanceof LocalAsgnNode)
 						name = ((LocalAsgnNode) node).getName();
 					if (node instanceof InstAsgnNode)
 						name = ((InstAsgnNode) node).getName();
 					if (node instanceof GlobalAsgnNode)
 						name = ((GlobalAsgnNode) node).getName();
 					return (name != null && name.equals(instVarNode.getName()));
 					/**
 					 * refactor to common INodeAcceptor for
 					 * instVarName,localVarName,globalVarName
 					 */
 				}
 			});
 			if (initialAssignmentNode != null) {
 				tryAsgnNode(initialAssignmentNode, guesses);
 			}
 		}
 	}
 
 	private void tryGlobalVarNode(Node node, List<ITypeGuess> guesses) {
 		if (node instanceof GlobalVarNode) {
 			final GlobalVarNode globalVarNode = (GlobalVarNode) node;
 			int nodeStart = node.getPosition().getStartOffset();
 
 			// todo: for STI on GlobalVar, find references within this ClassNode
 			// to this GlobalVar... record 'em
 			// todo: p.s. globals are low-priority.
 
 			// Find first assignment to this var name that occurs before the
 			// reference
 			// todo: This will find assignments in other local scopes that
 			// precede this reference but have the same variable name.
 			// To mitigate, ensure that the closest spanning ScopeNode for both
 			// this LocalVarNode and the AsgnNode are the name ScopeNode.
 			// Or scopingNode. Still not sure whether IterNodes count or not...
 			// silly block-local-var ambiguity ;)
 			Node initialAssignmentNode = FirstPrecursorNodeLocator.Instance().findFirstPrecursor(rootNode, nodeStart, new INodeAcceptor() {
 				public boolean doesAccept(Node node) {
 					String name = null;
 					if (node instanceof LocalAsgnNode)
 						name = ((LocalAsgnNode) node).getName();
 					if (node instanceof InstAsgnNode)
 						name = ((InstAsgnNode) node).getName();
 					if (node instanceof GlobalAsgnNode)
 						name = ((GlobalAsgnNode) node).getName();
 					return (name != null && name.equals(globalVarNode.getName()));
 					/**
 					 * refactor to common INodeAcceptor for
 					 * instVarName,localVarName,globalVarName
 					 */
 				}
 			});
 			if (initialAssignmentNode != null) {
 				tryAsgnNode(initialAssignmentNode, guesses);
 			}
 		}
 	}
 
 	private void tryLocalVarNode(Node node, List<ITypeGuess> guesses) {
 		// System.out.println(node.getClass().getName());
 		if (node instanceof LocalVarNode) {
 			LocalVarNode localVarNode = (LocalVarNode) node;
 			int nodeStart = node.getPosition().getStartOffset();
 			final String localVarName = TypeInferenceHelper.Instance().getVarName(localVarNode);
 
 			// See if it has been assigned to, earlier [todo: in this local
 			// scope].
 			// Find first assignment to this var name that occurs before the
 			// reference
 			// todo: This will find assignments in other local scopes that
 			// precede this reference but have the same variable name.
 			// To mitigate, ensure that the closest spanning ScopeNode for both
 			// this LocalVarNode and the AsgnNode are the name ScopeNode.
 			// Or scopingNode. Still not sure whether IterNodes count or not...
 			// silly block-local-var ambiguity ;)
 			Node initialAssignmentNode = FirstPrecursorNodeLocator.Instance().findFirstPrecursor(rootNode, nodeStart, new INodeAcceptor() {
 				public boolean doesAccept(Node node) {
 					String name = null;
 					if (node instanceof LocalAsgnNode)
 						name = ((LocalAsgnNode) node).getName();
 					if (node instanceof InstAsgnNode)
 						name = ((InstAsgnNode) node).getName();
 					if (node instanceof GlobalAsgnNode)
 						name = ((GlobalAsgnNode) node).getName();
 					return (name != null && name.equals(localVarName));
 				}
 			});
 			if (initialAssignmentNode != null) {
 				tryAsgnNode(initialAssignmentNode, guesses);
 			}
 			// See if it is a param into this scope
 			ArgsNode argsNode = (ArgsNode) FirstPrecursorNodeLocator.Instance().findFirstPrecursor(rootNode, nodeStart, new INodeAcceptor() {
 				public boolean doesAccept(Node node) {
 					return ((node instanceof ArgsNode) && (doesArgsNodeContainsVariable((ArgsNode) node, localVarName)));
 				}
 			});
 			// If so, find its enclosing method
 			if (argsNode != null) {
 				int argNumber = getArgumentIndex(argsNode, localVarName);
 				// System.out.println("Variable " + localVarName + " is the " +
 				// argNumber + "th argument to the enclosing method ");
 
 				// Find enclosing method
 				Node defNode = FirstPrecursorNodeLocator.Instance().findFirstPrecursor(rootNode, nodeStart, new INodeAcceptor() {
 					public boolean doesAccept(Node node) {
 						// System.out.println("Looking for enclosing method,
 						// checking: " + node.getClass().getName() + "[" +
 						// node.getPosition().getStartOffset() + ".." +
 						// node.getPosition().getEndOffset() + "]" );
 						ArgsNode argsNode = null;
 						if (node instanceof DefnNode)
 							argsNode = ((DefnNode) node).getArgsNode();
 						if (node instanceof DefsNode)
 							argsNode = ((DefsNode) node).getArgsNode();
 						return ((argsNode != null) && (doesArgsNodeContainsVariable(argsNode, localVarName)));
 					}
 				});
 				if (defNode != null) {
 					String methodName = null;
 					if (defNode instanceof DefnNode)
 						methodName = ((DefnNode) defNode).getName();
 					if (defNode instanceof DefsNode)
 						methodName = ((DefsNode) defNode).getName();
 
 					// System.out.println("Variable " + localVarName + " is the
 					// " + argNumber + "th argument to method " + methodName );
 
 					// Find all invocations of the surrounding method.
 					// todo: from easiest to hardest:
 					// It may be a global function, where simply a CallNode
 					// where method name must be matched.
 					// It may be a DefsNode static class method, where a
 					// CallNode whose receiverNode is a ConstNode whose name is
 					// the surrounding class
 					// It may be an DefnNode method defined in a class, where a
 					// CallNode whose receiverNode must be type-matched to the
 					// surrounding class
 
 				}
 			}
 		}
 	}
 
 	private void tryWellKnownMethodCalls(Node node, List<ITypeGuess> guesses) {
 		if (node instanceof CallNode) {
 			CallNode callNode = (CallNode) node;
 
 			String method = callNode.getName();
 			if (method.equals("new") && callNode.getReceiverNode() instanceof ConstNode) {
 				guesses.add(new BasicTypeGuess(((ConstNode) callNode.getReceiverNode()).getName(), 100));
 			} else {
 				// todo: this NEEDS to be done with a multimap and various
 				// confidences for each. i.e. X.slice, X is 50/50 Array or
 				// String
 				String methodReturnTypeGuess = TypicalMethodReturnNames.get(method);
 				if (methodReturnTypeGuess != null) {
 					guesses.add(new BasicTypeGuess(methodReturnTypeGuess, 100));
 				}
 			}
 		}
 	}
 
 	/**
 	 * Determine whether an ArgsNode contains a particular named argument
 	 * 
 	 * @param argsNode
 	 *            ArgsNode to search
 	 * @param argName
 	 *            Name of argument to find
 	 * @return
 	 */
 	private boolean doesArgsNodeContainsVariable(ArgsNode argsNode, String argName) {
 		if (argsNode == null) return false;
 		if (argName == null) return false;
 		return getArgumentIndex(argsNode, argName) >= 0;
 	}
 
 	/**
 	 * Finds the index of an argument in an ArgsNode by name, -1 if it is not
 	 * contained.
 	 * 
 	 * @param argsNode
 	 *            ArgsNode to search
 	 * @param argName
 	 *            Name of argument to find
 	 * @return Index of argName in argsNode or -1 if it is not there.
 	 */
 	private int getArgumentIndex(ArgsNode argsNode, String argName) {
 		int argNumber = 0;
 		ListNode args = argsNode.getArgs();
 		if (args == null) return -1; // no args. Maybe we should check arity instead?
 		for (Iterator iter = args.iterator(); iter.hasNext();) {
 			ArgumentNode arg = (ArgumentNode) iter.next();
 			if (arg.getName().equals(argName)) {
 				break;
 			}
 			argNumber++;
 		}
 		if (argNumber == argsNode.getArgsCount()) {
 			return -1;
 		}
 		return argNumber;
 	}
 
 	public static void main(String[] args) {
 		ITypeInferrer dti = new DefaultTypeInferrer();
 		List<ITypeGuess> guesses = dti.infer("'string'", 3);
 
 		for (ITypeGuess guess : guesses) {
 			System.out.println("Type guess: " + guess.getType() + ", " + guess.getConfidence() + "%");
 		}
 
 	}
 
 }
