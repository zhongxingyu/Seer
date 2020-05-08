 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.javascript.typeinference;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 import java.util.StringTokenizer;
 
 import org.eclipse.dltk.compiler.ISourceElementRequestor;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.ReferenceResolverContext;
 
 import com.xored.org.mozilla.javascript.FunctionNode;
 import com.xored.org.mozilla.javascript.Node;
 import com.xored.org.mozilla.javascript.ScriptOrFnNode;
 import com.xored.org.mozilla.javascript.Token;
 import com.xored.org.mozilla.javascript.Node.Jump;
 import com.xored.org.mozilla.javascript.Node.StringNode;
 
 public class TypeInferencer {
 
 	public static final String RETURN_VALUE = "!!!returnValue";
 
 	private ReferenceResolverContext cs;
 
 	public TypeInferencer(ModelElement owner, ReferenceResolverContext cs) {
 		this.module = owner;
 		this.cs = cs;
 	}
 
 	private final class TypeInferencerSwitch extends NodeSwitch {
 
 		private TypeInferencerSwitch(ScriptOrFnNode module, int position) {
 			super(module, position);
 		}
 
 		public Object processNewNode(Node node, Object arg) {
 			try {
 				Node firstChild = node.getFirstChild();
 				String objId = getObjId(firstChild.getNext());
 				String call = getObjId(firstChild);
 				if (call == null)
 					return super.processNewNode(node, arg);
 				int length = call.length();
 				int indexOf = call.indexOf('.');
 				if (indexOf != -1)
 					call = call.substring(indexOf + 1);
 				if (requestor != null) {
 					if (call != null) {
 						if (firstChild.getPosition() != 0) {
 							requestor.acceptMethodReference(call, 0, firstChild
 									.getPosition()
 									- length, firstChild.getPosition() - 1);
 						}
 					}
 				}
 			} catch (Throwable e) {
 			}
 			return super.processScriptNode(node, arg);
 		}
 
 		public Object processCall(Node node, Object arg) {
 			try {
 				Node firstChild = node.getFirstChild();
 				String objId = getObjId(firstChild.getNext());
 				String call = getObjId(firstChild);
 				if (call == null)
 					return super.processNewNode(node, arg);
 				int length = call.length();
 				int indexOf = call.indexOf('.');
 				if (indexOf != -1)
 					call = call.substring(indexOf + 1);
 				cs.processCall(call, objId);
 				if (requestor != null) {
 					if (call != null) {
 						if (firstChild.getPosition() != 0) {
 							int start = firstChild.getPosition() - length;
 							int end = firstChild.getPosition() - 1;
 							// if (start < 0) {
 							// System.out.println("AA");
 							// }
 							requestor
 									.acceptMethodReference(call, 0, start, end);
 						}
 					}
 				}
 			} catch (Throwable e) {
 
 			}
 			return super.processCall(node, arg);
 		}
 
 		public Object processSetElemNode(Node node, Object arg) {
 			if (node.getFirstChild().getNext() instanceof StringNode) {
 				String objId = getObjId(node.getFirstChild());
 				String fieldId = node.getFirstChild().getNext().getString();
 				internalSetProp(node, objId, fieldId);
 			} else {
 				String string;
 				string = getObjId(node.getFirstChild()) + "[]";
 				collection.write(string, evaluateReference(string, node
 						.getLastChild(), cs));
 			}
 			return arg;
 		}
 
 		public Object processSwitch(Node node, Object arg) {
 			Object processScriptNode = processScriptNode(node, arg);
 			return processScriptNode;
 		}
 
 		public Object processBlock(Node node, Object arg) {
 			Node nm = node.getFirstChild();
 			if (nm instanceof Jump) {
 				Jump jm = (Jump) nm;
 				int type = nm.getType();
 				if (type == Token.IFNE) {
 					return processIf(arg, jm);
 				}
 			}
 			Object processScriptNode = processScriptNode(node, arg);
 			return processScriptNode;
 
 		}
 
 		public Object processReturn(Node node, Object arg) {
 			IReference r = evaluateReference(RETURN_VALUE,
 					node.getFirstChild(), cs);
 			collection.setReference(RETURN_VALUE, r);
 			return arg;
 		}
 
 		public Object processHookNode(Node node, Object arg) {
 			HostCollection root = collection;
 			contexts.push(root);
 			HostCollection ifContext = new HostCollection(root);
 			HostCollection elseContext = new HostCollection(root);
 			collection = ifContext;
 			Node ifNode = node.getFirstChild().getNext();
 			doAction(ifNode, arg);
 			collection = elseContext;
 			Node elseNode = node.getLastChild();
 			doAction(elseNode, arg);
 			collection = contexts.pop();
 			root.mergeElseIf(ifContext, elseContext);
 			return arg;
 		}
 
 		public Object processCatchScopeNode(Node node, Object arg) {
 			Node n1 = node.getFirstChild();
 			contexts.push(collection);
 			// TODO this seems wrong!
 			collection = new HostCollection(collection.getParent());
 			String name = n1.getString();
 			collection.setReference(name, new StandardSelfCompletingReference(
 					name, true));
 			node.getFirstChild();
 			processScriptNode(node, arg);
 			HostCollection pop = contexts.pop();
 			if (node.getPosition() >= this.position)
 				throw new PositionReachedException(null);
 			// pop.mergeIf(collection);
 			collection = pop;
 			return arg;
 		}
 
 		public Object processLoop(Node node, Object arg) {
 			contexts.push(collection);
 			collection = new HostCollection(collection);
 			processScriptNode(node, arg);
 			HostCollection pop = contexts.pop();
 			pop.mergeIf(collection);
 			collection = pop;
 			return arg;
 		}
 
 		private Object processIf(Object arg, Jump jm) {
 			contexts.push(collection);
 			collection = new HostCollection(collection);
 			HostCollection ifCollection = null;
 			Node n = jm;
 			while (n != null) {
 				doAction(n, arg);
 				n = n.getNext();
 				if (n instanceof Jump) {
 					Jump cm = (Jump) n;
 					if (cm.getType() == Token.GOTO) {
 						// else then lies.
 						HostCollection pop = contexts.peek();
 						ifCollection = collection;
 						HostCollection elseCollection = new HostCollection(pop);
 						collection = elseCollection;
 					}
 				}
 			}
 			HostCollection pop = contexts.pop();
 			if (ifCollection == null)
 				pop.mergeIf(collection);
 			else
 				pop.mergeElseIf(collection, ifCollection);
 			collection = pop;
 
 			// Object processScriptNode = processScriptNode(jm, arg);
 			return arg;
 		}
 
 		public Object processFunction(Node node, Object arg) {
 			ScriptOrFnNode oldC = context;
 			String sn = node.getString();
 			if (sn.length() > 0)
 				for (int a = 0; a < context.getFunctionCount(); a++) {
 					FunctionNode functionNode = context.getFunctionNode(a);
 
 					String name = functionNode.getFunctionName();
 					if (name != null)
 						if (name.equals(sn)) {
 							context = functionNode;
 							internalProcessFunctionNode(arg, functionNode, node);
 							break;
 						}
 
 				}
 			else {
 				int index = node.getIntProp(Node.FUNCTION_PROP, -1);
 				if (index >= 0 && index < context.getFunctionCount()) {
 					FunctionNode functionNode = context.getFunctionNode(index);
 					context = functionNode;
 					internalProcessFunctionNode(arg, functionNode, node);
 				}
 			}
 			context = oldC;
 			return null;
 		}
 
 		private final LinkedList<HostCollection> functionContexts = new LinkedList<HostCollection>();
 
 		private void internalProcessFunctionNode(Object arg,
 				FunctionNode functionNode, Node function) {
 			if (functionNode.getEncodedSourceStart() >= position)
 				throw new PositionReachedException(context);
 			HostCollection parent = collection;
 			functionContexts.addLast(collection);
 			contexts.push(parent);
 
 			collection = new HostCollection(functionContexts.getFirst(),
 					functionNode.getFunctionName(), HostCollection.FUNCTION);
 
 			String comment = functionNode.getFunctionComments();
 			Map paramTypes = parseComment(comment);
 			for (int am = 0; am < functionNode.getParamCount(); am++) {
 				String paramOrVarName = functionNode.getParamOrVarName(am);
 				String type = (String) paramTypes.get(paramOrVarName);
 				IReference reference = null;
 				if (type != null) {
 					reference = ReferenceFactory.createTypeReference(
 							paramOrVarName, type, cs);
 				}
 				if (reference == null) {
 					reference = new StandardSelfCompletingReference(
 							paramOrVarName, false);
 				}
 				reference.setLocationInformation(new ReferenceLocation(module,
 						functionNode.nameStart, functionNode.getFunctionName()
 								.length()));
 				if (reference instanceof StandardSelfCompletingReference) {
 					((StandardSelfCompletingReference) reference)
 							.setParameterIndex(am);
 				}
 				collection.write(paramOrVarName, reference);
 			}
 			processScriptNode(functionNode.getFirstChild(), arg);
 			functionNodes.put(
 					new Integer(functionNode.getEncodedSourceStart()),
 					collection);
 
 			if (functionNode.getEncodedSourceEnd() >= position)
 				throw new PositionReachedException(functionNode);
 			HostCollection pop = contexts.pop();
 			Iterator i = collection.getReferences().values().iterator();
 			while (i.hasNext()) {
 				Object o = i.next();
 				if (o instanceof IReference) {
 					IReference rf = (IReference) o;
 					if (!rf.isLocal() && !"this".equals(rf.getName())) {
 						// dont overwrite if of the parent.
 						pop.write(rf.getName(), rf);
 					}
 				}
 			}
			pop.recordFunction(functionNode, collection);
 			collection = pop;
 
 			functionContexts.removeLast();
 		}
 
 		/**
 		 * @param comment
 		 * @return
 		 */
 		private Map parseComment(String comment) {
 			if (comment == null)
 				return Collections.EMPTY_MAP;
 
 			HashMap map = new HashMap();
 			// TODO use JSDoc parser.
 			int paramIndex = comment.indexOf("@param");
 			while (paramIndex != -1) {
 				int endLineIndex = comment.indexOf("\n", paramIndex);
 				StringTokenizer st = new StringTokenizer(comment.substring(
 						paramIndex + 6, endLineIndex));
 				String type = "";
 				while (st.hasMoreTokens()) {
 					String token = st.nextToken();
 					if (token.startsWith("{") && token.endsWith("}")) {
 						type = token.substring(1, token.length() - 1);
 					} else {
 						// token is the name.
 						map.put(token, type);
 						break;
 					}
 				}
 				paramIndex = comment.indexOf("@param", endLineIndex);
 			}
 			return map;
 		}
 
 		public Object processVarDeclaration(Node node, Object arg) {
 			Node firstChild = node.getFirstChild();
 			Object processScriptNode = processScriptNode(node, arg);
 			while (firstChild != null) {
 				final String key = getKey(firstChild);
 				IReference evaluateReference = evaluateReference(key,
 						firstChild.getFirstChild(), cs);
 				if (evaluateReference == null) {
 					evaluateReference = new StandardSelfCompletingReference(
 							key, false);
 				}
 				evaluateReference.setLocationInformation(new ReferenceLocation(
 						module, firstChild.getPosition() + 1, key.length()));
 				evaluateReference.setLocal(true);
 				collection.write(key, evaluateReference);
 				firstChild = firstChild.getNext();
 			}
 
 			return processScriptNode;
 		}
 
 		public Object processSetPropNode(Node node, Object arg) {
 			Node obj = node.getFirstChild();
 			String objId = getObjId(obj);
 			if (objId == null)
 				return null;
 			Node id = node.getFirstChild().getNext();
 			Object processScriptNode = processScriptNode(node, arg);
 			final String fieldId = getKey(id);
 			internalSetProp(node, objId, fieldId);
 			return processScriptNode;
 		}
 
 		public Object processDelProp(Node node, Object arg) {
 			Node firstChild = node.getFirstChild();
 			String objId = getKey(firstChild);
 			if (firstChild.getType() == Token.BINDNAME) {
 				collection.recordDelete(objId);
 			} else {
 				Node id = node.getFirstChild().getNext();
 				final String fieldId = getKey(id);
 				IReference queryElement = collection.queryElement(objId, false);
 				if (queryElement != null)
 					queryElement.recordDelete(fieldId);
 			}
 			// System.out.println(objId);
 			return null;
 		}
 
 		private void internalSetProp(Node node, String objId,
 				final String fieldId) {
 			final IReference evaluateReference = evaluateReference(fieldId,
 					node.getLastChild(), cs);
 
 			int pos = objId.indexOf('.');
 			String rootName = pos == -1 ? objId : objId.substring(0, pos);
 			IReference root = collection.getReferenceNoParentContext(rootName);
 			if (root == null) {
 				root = new StandardSelfCompletingReference(rootName, true);
 				root.setLocationInformation(new ReferenceLocation(module, node
 						.getPosition(), fieldId.length()));
 				HostCollection parent = collection.getParent();
 				if (parent == null) {
 					collection.setReference(rootName, root);
 				} else {
 					IReference rm = parent.getReference(rootName);
 					if (rm != null) {
 						rm = new CombinedOrReference(rm, root);
 						collection.setReference(rootName, rm);
 					} else
 						collection.setReference(rootName, root);
 				}
 			}
 			pos += 1;
 			while (pos != 0) {
 				int p1 = objId.indexOf('.', pos);
 				String field;
 				if (p1 != -1)
 					field = objId.substring(pos, p1);
 				else
 					field = objId.substring(pos);
 				IReference child = root.getChild(field, false);
 				if (child == null) {
 					child = new StandardSelfCompletingReference(field, true);
 
 					child.setLocationInformation(new ReferenceLocation(module,
 							node.getPosition(), fieldId.length()));
 					root.setChild(field, child);
 				}
 				root = child;
 				pos = p1 + 1;
 			}
 			TransparentRef transparentRef = new TransparentRef(
 					TypeInferencer.this, evaluateReference,
 					node.getLastChild(), fieldId, module, cs);
 			collection.addTransparent(transparentRef);
 			transparentRef.setLocationInformation(new ReferenceLocation(module,
 					node.getPosition(), fieldId.length()));
 			if (root.getName().equals("this")) {
 				collection.add(transparentRef.getName(), transparentRef);
 			}
 			root.setChild(fieldId, transparentRef);
 		}
 
 		@Override
 		public Object processLeaveWith(Node node, Object arg) {
 
 			if (node.getPosition() >= position)
 				throw new PositionReachedException(null);
 			if (withContexts.isEmpty())
 				return arg;
 			IReference to = withContexts.pop();
 			if (to != null) {
 				Map mn = collection.getReferences();
 				Iterator i = mn.keySet().iterator();
 				while (i.hasNext()) {
 					String next = (String) i.next();
 					to.setChild(next, (IReference) mn.get(next));
 				}
 			}
 			collection = contexts.pop();
 
 			return null;
 		}
 
 		@Override
 		public Object processEnterWith(Node node, Object arg) {
 			Node firstChild = node.getFirstChild();
 			// try catch case
 			if (!(firstChild instanceof Node.StringNode)) {
 				contexts.push(collection);
 				withContexts.push(null);
 				return arg;
 			}
 			contexts.push(collection);
 			String name = firstChild.getString();
 			IReference ref = collection.queryElement(name, false);
 			withContexts.push(ref);
 			collection = new HostCollection(collection);
 			if (ref != null) {
 				IReference r = ref.getPrototype(false);
 				if (r != null) {
 					final Collection<IReference> lm = new LinkedHashSet<IReference>();
 					while (r != null && !lm.contains(r)) {
 						lm.add(r);
 						r = r.getPrototype(false);
 					}
 					final List<IReference> list = new ArrayList<IReference>(lm);
 					Collections.reverse(list);
 					for (IReference k : list) {
 						Set<IReference> sm = k.getChilds(false);
 						for (IReference k1 : sm) {
 							collection.setReference(k1.getName(), k1);
 						}
 					}
 				}
 				Set<IReference> sm = ref.getChilds(false);
 				for (IReference k : sm) {
 					collection.setReference(k.getName(), k);
 				}
 			}
 			return null;
 		}
 
 		public Object processSetNameNode(Node node, Object arg) {
 			String key = getKey(node.getFirstChild());
 			Node lastChild = node.getLastChild();
 			Object processScriptNode = processScriptNode(node, arg);
 			IReference evaluateReference = evaluateReference(key, lastChild, cs);
 			evaluateReference.setLocationInformation(new ReferenceLocation(
 					module, node.getFirstChild().getPosition() - key.length(),
 					key.length()));
 			collection.write(key, evaluateReference);
 			return processScriptNode;
 		}
 	}
 
 	private final Stack<HostCollection> contexts = new Stack<HostCollection>();
 	private final Stack<IReference> withContexts = new Stack<IReference>();
 	private final Map<Integer, HostCollection> functionNodes = new HashMap<Integer, HostCollection>();
 	HostCollection collection = new HostCollection();
 	IModelElement module;
 
 	public HostCollection getCollection() {
 		return collection;
 	}
 
 	public IReference evaluateReference(String key, Node expression,
 			ReferenceResolverContext cs) {
 		IReference result = internalEvaluate(collection, key, expression,
 				module, cs);
 		if (result != null) {
 			return result;
 		} else {
 			return new StandardSelfCompletingReference(key, false);
 		}
 	}
 
 	IReference internalEvaluate(HostCollection collection, String key,
 			Node expression, IModelElement parent, ReferenceResolverContext cs) {
 		if (expression == null)
 			return null;
 		int type = expression.getType();
 
 		switch (type) {
 		case Token.EXPR_RESULT:
 			return internalEvaluate(collection, key,
 					expression.getFirstChild(), parent, cs);
 		case Token.NUMBER:
 			return new CombinedOrReference(new NewReference(key, "Number", cs),
 					new StandardSelfCompletingReference(key, false));
 			// return ReferenceFactory.createNumberReference(key, expression
 			// .getDouble());
 		case Token.STRING:
 			return new CombinedOrReference(new NewReference(key, "String", cs),
 					new StandardSelfCompletingReference(key, false));
 		case Token.TRUE:
 		case Token.FALSE:
 			return new CombinedOrReference(
 					new NewReference(key, "Boolean", cs),
 					new StandardSelfCompletingReference(key, false));
 		case Token.OBJECTLIT:
 			return createObjectLiteral(collection, key, expression, parent, cs);
 		case Token.ARRAYLIT:
 			return createArrayLiteralReference(collection, key, expression, cs);
 		case Token.CALL:
 			return createCallResult(collection, key, expression, cs);
 		case Token.FUNCTION:
 			return createNewFunctionReference(collection, key, expression);
 		case Token.NEW:
 			return createNewObjectRefernce(collection, key, expression, cs);
 		case Token.NAME:
 
 		case Token.GETELEM:
 		case Token.GETPROP:
 			String key1 = getObjId(expression);
 			if (key1.endsWith(".e"))
 				key1 = key1.substring(0, key1.length() - 2);
 			IReference ref = collection.queryElement(key1, true);
 			if (ref == null) {
 				ref = resolveReferenceTree(key, cs, key1, ref);
 			}
 			if (ref != null) {
 				ref = new TransparentRef(this, ref, expression, key, parent, cs);
 			}
 			return ref;
 		default:
 			break;
 		}
 		return null;
 	}
 
 	/**
 	 * @param key
 	 * @param cs
 	 * @param key1
 	 * @param ref
 	 * @return
 	 */
 	private static IReference resolveReferenceTree(String key,
 			ReferenceResolverContext cs, String key1, IReference ref) {
 		Set resolveGlobals = cs.resolveGlobals(key1);
 		if (resolveGlobals.size() > 0) {
 			ref = new AutoCompleteReference(key, key1, cs);
 		}
 		return ref;
 	}
 
 	private static IReference createNewFunctionReference(
 			HostCollection collection2, final String key, Node expression) {
 		final HostCollection function = collection2.getFunction(expression);
 		if (function == null) {
 			return null;
 		}
 		return new ContextReference(function, key);
 	}
 
 	private static IReference createCallResult(HostCollection collection2,
 			String key, Node expression, ReferenceResolverContext cs) {
 		Node nm = expression.getFirstChild();
 		String id = getObjId(nm);
 		if (id == null)
 			return null;
 		CallResultReference ref = new CallResultReference(collection2, key, id,
 				cs);
 		CombinedOrReference ws = new CombinedOrReference(ref,
 				new StandardSelfCompletingReference(key, false));
 		return ws;
 	}
 
 	private IReference createNewObjectRefernce(HostCollection collection2,
 			String key, Node expression, ReferenceResolverContext cs) {
 		Node nm = expression.getFirstChild();
 		String id = getObjId(nm);
 		if (id == null)
 			return null;
 		NewReference ref = new NewReference(key, id, cs);
 		StandardSelfCompletingReference unknownReference = new StandardSelfCompletingReference(
 				key, false);
 		if (id.equals("XML")) {
 			try {
 				if (xmlInferencer == null) {
 					xmlInferencer = new XMLLiteralInferencer();
 				}
 				xmlInferencer.modifyReference(unknownReference, expression
 						.getLastChild().getString(), cs);
 			} catch (ClassCastException e) {
 			}
 		}
 		CombinedOrReference ws = new CombinedOrReference(ref, unknownReference);
 		return ws;
 	}
 
 	private XMLLiteralInferencer xmlInferencer = null;
 
 	private static IReference createArrayLiteralReference(
 			HostCollection collection2, String key, Node expression,
 			ReferenceResolverContext cs) {
 
 		String id = "Array";
 		NewReference ref = new NewReference(key, id, cs);
 		StandardSelfCompletingReference unknownReference = new StandardSelfCompletingReference(
 				key, false);
 		CombinedOrReference ws = new CombinedOrReference(ref, unknownReference);
 		return ws;
 
 	}
 
 	private IReference createObjectLiteral(HostCollection col, String key,
 			Node expression, IModelElement parent, ReferenceResolverContext cs) {
 		Object[] ids = (Object[]) expression.getProp(Node.OBJECT_IDS_PROP);
 		ArrayList positions = (ArrayList) expression
 				.getProp(Node.DESCENDANTS_FLAG);
 		Node child = expression.getFirstChild();
 		StandardSelfCompletingReference uRef = new StandardSelfCompletingReference(
 				key, false);
 		for (int a = 0; a < ids.length; a++) {
 			if (ids[a] instanceof String) {
 				String name = (String) ids[a];
 				IReference internalEvaluate = internalEvaluate(col, name,
 						child, parent, cs);
 				if (internalEvaluate == null)
 					internalEvaluate = new StandardSelfCompletingReference(
 							name, false);
 				internalEvaluate.setLocationInformation(new ReferenceLocation(
 						parent, ((Integer) positions.get(a)).intValue()
 								- name.length() - 1, name.length()));
 				uRef.setChild(name, internalEvaluate);
 				child = child.getNext();
 			}
 		}
 		return uRef;
 	}
 
 	public static String getObjId(Node id) {
 		if (id == null)
 			return "";
 		switch (id.getType()) {
 		case Token.CALL: {
 			Node n = id.getFirstChild();
 			return getObjId(n);
 		}
 		case Token.GETPROP: {
 			Node n = id.getFirstChild();
 			String result = getObjId(n) + '.' + id.getLastChild().getString();
 			return result;
 		}
 		case Token.GETELEM: {
 			Node n = id.getFirstChild();
 			String res = getObjId(n);
 			return res + "[]";
 		}
 		case Token.THIS:
 			return "this";
 		case Token.BINDNAME:
 		case Token.NAME:
 			return getKey(id);
 		default:
 			break;
 		}
 		return null;
 	}
 
 	public static String getKey(Node id) {
 		try {
 			return id.getString();
 		} catch (ClassCastException e) {
 			return "";
 		}
 	}
 
 	public HostCollection doInterferencing(ScriptOrFnNode node, int tillPosition) {
 		HostCollection hostCollection = new HostCollection();
 		NodeSwitch sw = new TypeInferencerSwitch(node, tillPosition);
 		sw.doAction(node, hostCollection);
 		return hostCollection;
 	}
 
 	public Map<Integer, HostCollection> getFunctionMap() {
 		return functionNodes;
 	}
 
 	ISourceElementRequestor requestor;
 
 	public void setRequestor(ISourceElementRequestor requestor) {
 		this.requestor = requestor;
 	}
 }
