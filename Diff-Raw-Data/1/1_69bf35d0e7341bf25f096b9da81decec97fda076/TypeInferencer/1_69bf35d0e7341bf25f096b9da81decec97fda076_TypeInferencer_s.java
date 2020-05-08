 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.javascript.typeinference;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
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
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.eclipse.dltk.compiler.ISourceElementRequestor;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.ReferenceResolverContext;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.xored.org.mozilla.javascript.FunctionNode;
 import com.xored.org.mozilla.javascript.Node;
 import com.xored.org.mozilla.javascript.ScriptOrFnNode;
 import com.xored.org.mozilla.javascript.Token;
 import com.xored.org.mozilla.javascript.Node.Jump;
 import com.xored.org.mozilla.javascript.Node.StringNode;
 
 final class TransparentRef implements IReference {
 	IReference evaluateReference;
 
 	private final String fieldId;
 	private final Node node;
 
 	ReferenceResolverContext cs;
 
 	TransparentRef(IReference evaluateReference, Node objID, String fieldId,
 			ModelElement parent, ReferenceResolverContext cs) {
 		this.evaluateReference = evaluateReference;
 		this.fieldId = fieldId;
 		this.node = objID;
 		this.parent = parent;
 		this.cs = cs;
 	}
 
 	public IReference getChild(String key, boolean resolveLocals) {
 		IReference child = evaluateReference.getChild(key, resolveLocals);
 		return child;
 	}
 
 	public Set getChilds(boolean resolveLocals) {
 		return evaluateReference.getChilds(resolveLocals);
 	}
 
 	public String getName() {
 		return fieldId;
 	}
 
 	public String getParentName() {
 		return fieldId;
 	}
 
 	public void setChild(String key, IReference ref) {
 		evaluateReference.setChild(key, ref);
 	}
 
 	public boolean isChildishReference() {
 		return false;
 	}
 
 	ModelElement parent;
 
 	public void patchRef(HostCollection collection) {
 		Set s = evaluateReference.getChilds(false);
 		IReference queryElement = TypeInferencer.internalEvaluate(collection,
 				getName(), node, parent, cs);
 
 		if (queryElement != null && queryElement != this)
 			this.evaluateReference = queryElement;
 		Iterator it = s.iterator();
 		// TODO REVIEW IT;
 		while (it.hasNext()) {
 			Object next = it.next();
 			if (!(next instanceof IReference))
 				continue;
 			IReference r = (IReference) next;
 			evaluateReference.setChild(r.getName(), r);
 		}
 	}
 
 	public void recordDelete(String fieldId) {
 		evaluateReference.recordDelete(fieldId);
 	}
 
 	public IReference getPrototype(boolean resolveLocals) {
 		return evaluateReference.getPrototype(false);
 	}
 
 	public void setPrototype(IReference ref) {
 		evaluateReference.setPrototype(ref);
 	}
 
 	int length;
 	int location;
 
 	public void addModelElements(Collection toAdd) {
 		if (parent != null)
 			toAdd.add(new FakeField(parent, getName(), location, length));
 	}
 
 	public void setLocationInformation(ModelElement mo, int position, int length) {
 		this.parent = mo;
 		this.location = position;
 		this.length = length;
 	}
 
 	public boolean isFunctionRef() {
 		return evaluateReference.isFunctionRef();
 	}
 
 	public boolean isLocal() {
 		return evaluateReference.isLocal();
 	}
 
 	public void setLocal(boolean local) {
 		evaluateReference.setLocal(true);
 	}
 
 }
 
 public class TypeInferencer {
 
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
 							requestor.acceptMethodReference(call.toCharArray(),
 									0, firstChild.getPosition() - length,
 									firstChild.getPosition() - 1);
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
 							if (start < 0) {
 								System.out.println("AA");
 							}
 							requestor.acceptMethodReference(call.toCharArray(),
 									0, start, end);
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
 			IReference r = evaluateReference("!!!returnValue", node
 					.getFirstChild(), cs);
 			collection.setReference("!!!returnValue", r);
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
 			collection = (HostCollection) contexts.pop();
 			root.mergeElseIf(ifContext, elseContext);
 			return arg;
 		}
 
 		public Object processCatchScopeNode(Node node, Object arg) {
 			Node n1 = node.getFirstChild();
 			contexts.push(collection);
 			collection = new HostCollection(collection.getParent());
 			String name = n1.getString();
 			collection.setReference(name, new UncknownReference(name, true));
 			node.getFirstChild();
 			processScriptNode(node, arg);
 			HostCollection pop = (HostCollection) contexts.pop();
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
 			HostCollection pop = (HostCollection) contexts.pop();
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
 						HostCollection pop = (HostCollection) contexts.peek();
 						ifCollection = collection;
 						HostCollection elseCollection = new HostCollection(pop);
 						collection = elseCollection;
 					}
 				}
 			}
 			HostCollection pop = (HostCollection) contexts.pop();
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
 				FunctionNode functionNode = context.getFunctionNode(index);
 				context = functionNode;
 				internalProcessFunctionNode(arg, functionNode, node);
 			}
 			context = oldC;
 			return null;
 		}
 
 		LinkedList functionContexts = new LinkedList();
 
 		private void internalProcessFunctionNode(Object arg,
 				FunctionNode functionNode, Node function) {
 			if (functionNode.getEncodedSourceStart() >= position)
 				throw new PositionReachedException(context);
 			HostCollection parent = collection;
 			functionContexts.addLast(collection);
 			contexts.push(parent);
 
 			collection = new HostCollection((HostCollection) functionContexts
 					.getFirst());
 
 			collection.setType(HostCollection.FUNCTION);
 			collection.setName(functionNode.getFunctionName());
 			for (int am = 0; am < functionNode.getParamCount(); am++) {
 				String paramOrVarName = functionNode.getParamOrVarName(am);
 				UncknownReference uncknownReference = new UncknownReference(
 						paramOrVarName, false);
 				uncknownReference.setLocationInformation(module,
 						functionNode.nameStart, functionNode.getFunctionName()
 								.length());
 				collection.write(paramOrVarName, uncknownReference);
 			}
 			processScriptNode(functionNode.getFirstChild(), arg);
 			functionNodes.put(
 					new Integer(functionNode.getEncodedSourceStart()),
 					collection);
 
 			if (functionNode.getEncodedSourceEnd() >= position)
 				throw new PositionReachedException(functionNode);
 			HostCollection pop = (HostCollection) contexts.pop();
 			Iterator i = collection.getReferences().values().iterator();
 			while (i.hasNext()) {
 				Object o = i.next();
 				if (o instanceof IReference) {
 					IReference rf = (IReference) o;
 					if (!rf.isLocal()) {
 						pop.write(rf.getName(), rf);
 					}
 				}
 			}
 			pop.recordFunction(function, collection);
 			collection = pop;
 
 			functionContexts.removeLast();
 		}
 
 		public Object processVarDeclaration(Node node, Object arg) {
 			Node firstChild = node.getFirstChild();
 			Object processScriptNode = processScriptNode(node, arg);
 			while (firstChild != null) {
 				final String key = getKey(firstChild);
 				IReference evaluateReference = evaluateReference(key,
 						firstChild.getFirstChild(), cs);
 				if (evaluateReference == null) {
 					evaluateReference = new UncknownReference(key, false);
 				}
 				evaluateReference.setLocationInformation(module, firstChild
 						.getPosition(), key.length());
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
 			System.out.println(objId);
 			return null;
 		}
 
 		private void internalSetProp(Node node, String objId,
 				final String fieldId) {
 			final IReference evaluateReference = evaluateReference(fieldId,
 					node.getLastChild(), cs);
 
 			int pos = objId.indexOf('.');
 			String rootName = pos == -1 ? objId : objId.substring(0, pos);
 			IReference root = (IReference) collection
 					.getReferenceNoParentContext(rootName);
 			if (root == null) {
 				root = new UncknownReference(rootName, true);
 				root.setLocationInformation(module, node.getPosition(), fieldId
 						.length());
 				HostCollection parent = collection.getParent();
 				if (parent == null) {
 					collection.setReference(rootName, root);
 				} else {
 					IReference rm = parent.getReference(rootName);
 					if (rm != null) {
 						rm = new OrReferenceWriteSecond(rm, root);
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
 					child = new UncknownReference(field, true);
 
 					child.setLocationInformation(module, node.getPosition(),
 							fieldId.length());
 					root.setChild(field, child);
 				}
 				root = child;
 				pos = p1 + 1;
 			}
 			TransparentRef transparentRef = new TransparentRef(
 					evaluateReference, node.getLastChild(), fieldId, module, cs);
 			collection.addTransparent(transparentRef);
 			transparentRef.setLocationInformation(module, node.getPosition(),
 					fieldId.length());
 			if (root.getName().equals("this")) {
 				collection.add(transparentRef.getName(), transparentRef);
 			}
 			root.setChild(fieldId, transparentRef);
 		}
 
 		public Object processLeaveWidth(Node node, Object arg) {
			// TODO REMOVE THIS SHIT LATER
 
 			if (node.getPosition() >= position)
 				throw new PositionReachedException(null);
 			if (contexts.isEmpty())
 				return arg;
 			if (!(contexts.peek() instanceof IReference)
 					&& contexts.peek() != null)
 				return arg;
 			IReference to = (IReference) contexts.pop();
 			if (to != null) {
 				Map mn = collection.getReferences();
 				Iterator i = mn.keySet().iterator();
 				while (i.hasNext()) {
 					String next = (String) i.next();
 					to.setChild(next, (IReference) mn.get(next));
 				}
 			}
 			collection = (HostCollection) contexts.pop();
 
 			return null;
 		}
 
 		public Object processEnterWidth(Node node, Object arg) {
 			Node firstChild = node.getFirstChild();
 			// try catch case
 			if (!(firstChild instanceof Node.StringNode)) {
 				contexts.push(collection);
 				contexts.push(null);
 				return arg;
 			}
 			contexts.push(collection);
 			String name = firstChild.getString();
 			IReference ref = collection.queryElement(name, false);
 			contexts.push(ref);
 			collection = new HostCollection(collection);
 			if (ref != null) {
 				IReference r = ref.getPrototype(false);
 				if (r != null) {
 					Collection lm = new LinkedHashSet();
 					while (r != null && !lm.contains(r)) {
 						lm.add(r);
 						r = r.getPrototype(false);
 					}
 					lm = new LinkedList(lm);
 					Collections.reverse((List) lm);
 					Iterator i = lm.iterator();
 					while (i.hasNext()) {
 						IReference k = (IReference) i.next();
 						Set sm = k.getChilds(false);
 						Iterator i1 = sm.iterator();
 						while (i1.hasNext()) {
 							IReference k1 = (IReference) i1.next();
 							collection.setReference(k1.getName(), k1);
 						}
 					}
 				}
 				Set sm = ref.getChilds(false);
 				Iterator i = sm.iterator();
 				while (i.hasNext()) {
 					IReference k = (IReference) i.next();
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
 			evaluateReference.setLocationInformation(module, node
 					.getFirstChild().getPosition(), key.length());
 			collection.write(key, evaluateReference);
 			return processScriptNode;
 		}
 	}
 
 	Stack contexts = new Stack();
 	Map functionNodes = new HashMap();
 	HostCollection collection = new HostCollection(null);
 	ModelElement module;
 
 	public HostCollection getCollection() {
 		return collection;
 	}
 
 	public IReference evaluateReference(String key, Node expression,
 			ReferenceResolverContext cs) {
 		IReference internalEvaluate = internalEvaluate(collection, key,
 				expression, module, cs);
 		if (internalEvaluate == null)
 			return new UncknownReference(key, false);
 		return internalEvaluate;
 	}
 
 	static IReference internalEvaluate(HostCollection collection, String key,
 			Node expression, ModelElement parent, ReferenceResolverContext cs) {
 		if (expression == null)
 			return null;
 		int type = expression.getType();
 
 		switch (type) {
 		case Token.EXPR_RESULT:
 			return internalEvaluate(collection, key,
 					expression.getFirstChild(), parent, cs);
 		case Token.NUMBER: {
 			NewReference newReference = new NewReference(key, "Number", cs);
 			UncknownReference uncknownReference = new UncknownReference(key,
 					false);
 			return new OrReferenceWriteSecond(newReference, uncknownReference);
 		}
 // return ReferenceFactory.createNumberReference(key, expression
 // .getDouble());
 		case Token.STRING: {
 			NewReference newReference = new NewReference(key, "String", cs);
 			UncknownReference uncknownReference = new UncknownReference(key,
 					false);
 			return new OrReferenceWriteSecond(newReference, uncknownReference);
 		}
 		case Token.TRUE:
 		case Token.FALSE: {
 			NewReference newReference = new NewReference(key, "Boolean", cs);
 			UncknownReference uncknownReference = new UncknownReference(key,
 					false);
 			return new OrReferenceWriteSecond(newReference, uncknownReference);
 		}
 		case Token.OBJECTLIT:
 			return createObjectLiteral(collection, key, expression, parent, cs);
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
 			return (IReference) collection.queryElement(key1, false);
 		default:
 			break;
 		}
 		return null;
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
 		OrReferenceWriteSecond ws = new OrReferenceWriteSecond(ref,
 				new UncknownReference(key, false));
 		return ws;
 	}
 
 	private static IReference createNewObjectRefernce(
 			HostCollection collection2, String key, Node expression,
 			ReferenceResolverContext cs) {
 		Node nm = expression.getFirstChild();
 		String id = getObjId(nm);
 		if (id == null)
 			return null;
 		NewReference ref = new NewReference(key, id, cs);
 		UncknownReference uncknownReference = new UncknownReference(key, false);
 		if (id.equals("XML")) {
 			try {
 				String string = expression.getLastChild().getString();
 				modifyReferenceXML(uncknownReference, string);
 			} catch (ClassCastException e) {
 			}
 		}
 		OrReferenceWriteSecond ws = new OrReferenceWriteSecond(ref,
 				uncknownReference);
 		return ws;
 
 	}
 
 	static SAXParser parser;
 
 	static {
 		try {
 			parser = SAXParserFactory.newInstance().newSAXParser();
 		} catch (ParserConfigurationException e) {
 			throw new LinkageError();
 		} catch (SAXException e) {
 			throw new LinkageError();
 		}
 	}
 
 	private static void modifyReferenceXML(
 			final UncknownReference uncknownReference, String string) {
 		try {
 			parser.parse(new ByteArrayInputStream(string.getBytes()),
 					new DefaultHandler() {
 
 						boolean has = false;
 						UncknownReference curReference = uncknownReference;
 						Stack stack = new Stack();
 
 						public void endElement(String uri, String localName,
 								String name) throws SAXException {
 							if (!stack.isEmpty())
 								curReference = (UncknownReference) stack.pop();
 						}
 
 						public void startElement(String uri, String localName,
 								String name, Attributes attributes)
 								throws SAXException {
 							UncknownReference uncknownReference2 = new UncknownReference(
 									name, true);
 
 							int length = attributes.getLength();
 							if (has) {
 								curReference.setChild(name, uncknownReference2);
 								curReference = uncknownReference2;
 								stack.push(curReference);
 							}
 							for (int a = 0; a < length; a++) {
 								String val = "@" + attributes.getQName(a);
 								UncknownReference uncknownReference3 = new UncknownReference(
 										val, true);
 								curReference.setChild(val, uncknownReference3);
 							}
 // if (has) {
 // curReference.setChild(name, uncknownReference2);
 // curReference = uncknownReference2;
 // stack.push(curReference);
 // }
 							has = true;
 						}
 
 					});
 		} catch (SAXException e) {
 
 		} catch (IOException e) {
 
 		}
 	}
 
 	private static IReference createObjectLiteral(HostCollection col,
 			String key, Node expression, ModelElement parent,
 			ReferenceResolverContext cs) {
 		Object[] ids = (Object[]) expression.getProp(Node.OBJECT_IDS_PROP);
 		ArrayList positions = (ArrayList) expression
 				.getProp(Node.DESCENDANTS_FLAG);
 		Node child = expression.getFirstChild();
 		UncknownReference uRef = new UncknownReference(key, false);
 		for (int a = 0; a < ids.length; a++) {
 			if (ids[a] instanceof String) {
 				String name = (String) ids[a];
 				IReference internalEvaluate = internalEvaluate(col, name,
 						child, parent, cs);
 				if (internalEvaluate == null)
 					internalEvaluate = new UncknownReference(name, false);
 				internalEvaluate.setLocationInformation(parent,
 						((Integer) positions.get(a)).intValue() - name.length()
 								- 1, name.length());
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
 		} finally {
 		}
 	}
 
 	public HostCollection doInterferencing(ScriptOrFnNode node, int tillPosition) {
 		HostCollection hostCollection = new HostCollection(null);
 		NodeSwitch sw = new TypeInferencerSwitch(node, tillPosition);
 		sw.doAction(node, hostCollection);
 		return hostCollection;
 	}
 
 	public Map getFunctionMap() {
 		return functionNodes;
 	}
 
 	ISourceElementRequestor requestor;
 
 	public void setRequestor(ISourceElementRequestor requestor) {
 		this.requestor = requestor;
 	}
 }
