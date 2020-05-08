 /* 
  * Copyright (c) Ericsson AB, 2013
  * All rights reserved.
  *
  * License terms:
  *
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  *     * Redistributions of source code must retain the above 
  *       copyright notice, this list of conditions and the 
  *       following disclaimer.
  *     * Redistributions in binary form must reproduce the 
  *       above copyright notice, this list of conditions and 
  *       the following disclaimer in the documentation and/or 
  *       other materials provided with the distribution.
  *     * Neither the name of the copyright holder nor the names 
  *       of its contributors may be used to endorse or promote 
  *       products derived from this software without specific 
  *       prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.caltoopia.codegen;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.caltoopia.ir.AbstractActor;
 import org.caltoopia.ir.Action;
 import org.caltoopia.ir.Actor;
 import org.caltoopia.ir.ActorInstance;
 import org.caltoopia.ir.Annotation;
 import org.caltoopia.ir.AnnotationArgument;
 import org.caltoopia.ir.Assign;
 import org.caltoopia.ir.BinaryExpression;
 import org.caltoopia.ir.Block;
 import org.caltoopia.ir.BooleanLiteral;
 import org.caltoopia.ir.Connection;
 import org.caltoopia.ir.Declaration;
 import org.caltoopia.ir.Expression;
 import org.caltoopia.ir.ExternalActor;
 import org.caltoopia.ir.FloatLiteral;
 import org.caltoopia.ir.ForEach;
 import org.caltoopia.ir.ForwardDeclaration;
 import org.caltoopia.ir.FromSource;
 import org.caltoopia.ir.FunctionCall;
 import org.caltoopia.ir.Generator;
 import org.caltoopia.ir.Guard;
 import org.caltoopia.ir.IfExpression;
 import org.caltoopia.ir.IfStatement;
 import org.caltoopia.ir.IntegerLiteral;
 import org.caltoopia.ir.IrFactory;
 import org.caltoopia.ir.LambdaExpression;
 import org.caltoopia.ir.ListExpression;
 import org.caltoopia.ir.Member;
 import org.caltoopia.ir.Namespace;
 import org.caltoopia.ir.Network;
 import org.caltoopia.ir.Point2PointConnection;
 import org.caltoopia.ir.Port;
 import org.caltoopia.ir.PortInstance;
 import org.caltoopia.ir.PortPeek;
 import org.caltoopia.ir.PortRead;
 import org.caltoopia.ir.PortWrite;
 import org.caltoopia.ir.ProcCall;
 import org.caltoopia.ir.ProcExpression;
 import org.caltoopia.ir.ReturnValue;
 import org.caltoopia.ir.Schedule;
 import org.caltoopia.ir.Scope;
 import org.caltoopia.ir.State;
 import org.caltoopia.ir.Statement;
 import org.caltoopia.ir.StringLiteral;
 import org.caltoopia.ir.TaggedExpression;
 import org.caltoopia.ir.ToSink;
 import org.caltoopia.ir.Type;
 import org.caltoopia.ir.TypeActor;
 import org.caltoopia.ir.TypeConstructor;
 import org.caltoopia.ir.TypeConstructorCall;
 import org.caltoopia.ir.TypeDeclaration;
 import org.caltoopia.ir.TypeDeclarationImport;
 import org.caltoopia.ir.TypeInt;
 import org.caltoopia.ir.TypeLambda;
 import org.caltoopia.ir.TypeProc;
 import org.caltoopia.ir.TypeRecord;
 import org.caltoopia.ir.TypeUint;
 import org.caltoopia.ir.UnaryExpression;
 import org.caltoopia.ir.Variable;
 import org.caltoopia.ir.VariableExpression;
 import org.caltoopia.ir.VariableExternal;
 import org.caltoopia.ir.VariableImport;
 import org.caltoopia.ir.VariableReference;
 import org.caltoopia.ir.WhileLoop;
 import org.caltoopia.types.TypeSystem;
 import org.caltoopia.ast2ir.PriorityGraph;
 import org.caltoopia.ast2ir.Util;
 import org.eclipse.emf.ecore.EObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class IrXmlReader {
 
 	private Map<String, EObject> objectMap = new HashMap<String, EObject>();
 	
 	private Map<ForwardDeclaration, String> forwardDeclarationMap = new HashMap<ForwardDeclaration, String>();
 	
 	public AbstractActor readActor(String path) {
 		try {		  
 			File fXmlFile = new File(path);
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			Document doc = dBuilder.parse(fXmlFile);
 			doc.getDocumentElement().normalize();
 	 
 			String topTag = doc.getDocumentElement().getNodeName();
 			AbstractActor result;
 			
 			if (topTag.equals("Actor")) {
 				result = createActor(doc.getDocumentElement());
 			} else if (topTag.equals("Network")) {
 				result = createNetwork(doc.getDocumentElement());
 			} else if (topTag.equals("ExternalActor")) {
 				result = createExternalActor(doc.getDocumentElement());
 			} else {
 				throw new RuntimeException("Invalid top tag in XML ir document");
 			}
 			
 			// As a last step, patch the forward declarations
 			for (ForwardDeclaration decl : forwardDeclarationMap.keySet()) {
 				decl.setDeclaration((Declaration)findIrObject(forwardDeclarationMap.get(decl)));
 			}
 			
 			return result;			
 		} catch (Exception x) {
 			System.err.println("[ActorDirectory]Error reading '" + path + "' x " + x.getMessage()); 
 			return null;
 		}
 		
 	}
 	
 	public Namespace readNamespace(String path) {
 		try {		  
 			File fXmlFile = new File(path);
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			Document doc = dBuilder.parse(fXmlFile);
 			doc.getDocumentElement().normalize();
 	 
 			String topTag = doc.getDocumentElement().getNodeName();
 
 			if (topTag.equals("Namespace")) {
 				Namespace result = createNamespace(doc.getDocumentElement());
 				for (ForwardDeclaration decl : forwardDeclarationMap.keySet()) {
 					decl.setDeclaration((Declaration)findIrObject(forwardDeclarationMap.get(decl)));
 				}
 				return result;
 			} else {
 				throw new RuntimeException("Invalid top tag in XML ir document");
 			}
 			
 			// As a last step, patch the forward declarations
 			
 			
 		} catch (Exception e) {
 			System.err.println("[IrXmlReader. Error reading '" + path + "' message = " + e.getMessage()); 
 			e.printStackTrace();
 			return null;
 		}
 		
 	}
 	
 	private Namespace createNamespace(Element element) {
 		Namespace namespace = IrFactory.eINSTANCE.createNamespace();
 		String id = element.getAttribute("id");
 		namespace.setId(id);
 		doAnnotations(namespace, element);
 
 		for (String s : Util.unpackQualifiedName(element.getAttribute("name"))) {
 			namespace.getName().add(s);
 		}
 		
 		addIrObject(id, namespace);
 				
 		List<Element> declarations = getChildren(element, "Decl");
 		for (Element e : declarations) {
 			Declaration var =  createDeclaration(e);
 			namespace.getDeclarations().add(var);
 		}
 		
 		return namespace;
 	}
 
 	
 	private Network createNetwork(Element element) {
 		Network network = IrFactory.eINSTANCE.createNetwork();
 		String id = element.getAttribute("id");
 		network.setId(id);
 		doAnnotations(network, element);
 				
 		addIrObject(id, network);			
 		
 		network.setType((TypeActor) createType(getChild(element, "Type")));
 		
 		List<Element> declarations = getChildren(element, "Decl");
 		for (Element e : declarations) {
 			Declaration decl = (Declaration) createDeclaration(e);
 			network.getDeclarations().add(decl);
 			if (decl instanceof Variable && ((Variable) decl).isParameter()) { 
 					network.getParameters().add((Variable) decl);
 			}
 		}
 				
 		List<Element> ports = getChildren(element, "Port");
 		for (Element e : ports) {
 			Port port = createPort(e);
 			if (e.getAttribute("direction").equals("in")) {
 				network.getInputPorts().add(port);
 			} else {
 				network.getOutputPorts().add(port);
 			}
 		}
 
 		List<Element> instances = getChildren(element, "Instance");
 		for (Element e : instances) {
 			ActorInstance instance = createActorInstance(e);
 			network.getActors().add(instance);
 		}
 		 
 		List<Element> connections = getChildren(element, "Connection");
 		for (Element e : connections) {
 			Connection connection = createConnection(e, network);
 			network.getConnections().add(connection);
 		}
 		
 		return network;
 	}
 	
 	private Connection createConnection(Element element, Network network) {
 		String kind = element.getAttribute("kind");
 		
 		if (kind.equals("point2point")) {
 			Point2PointConnection connection = IrFactory.eINSTANCE.createPoint2PointConnection();
 			connection.setId(element.getAttribute("id"));
 			doAnnotations(connection, element);
 			doAttributes(connection, element);
 			ActorInstance sourceInstance = (ActorInstance) findIrObject(element.getAttribute("src"));
 			String portName = element.getAttribute("src-port");
 			connection.setSource(findPortInstance(sourceInstance, portName, true));
 			ActorInstance targetInstance = (ActorInstance) findIrObject(element.getAttribute("dst"));
 			portName = element.getAttribute("dst-port");
 			connection.setTarget(findPortInstance(targetInstance, portName, false));
 
 			connection.getTarget().getConnections().add(connection);
 			connection.getSource().getConnections().add(connection);
 
 			return connection;
 		} else if (kind.equals("fromSource")) {
 			FromSource connection = IrFactory.eINSTANCE.createFromSource();
 			connection.setId(element.getAttribute("id"));
 			doAnnotations(connection, element);
 			doAttributes(connection, element);
 			String portName = element.getAttribute("src-port");
 			connection.setSource(findPort(network, portName, false));
 			ActorInstance targetInstance = (ActorInstance) findIrObject(element.getAttribute("dst"));
 			portName = element.getAttribute("dst-port");
 			connection.setTarget(findPortInstance(targetInstance, portName, false));
 
 			connection.getTarget().getConnections().add(connection);
 			
 			return connection;
 		} else if (kind.equals("toSink")) {
 			ToSink connection = IrFactory.eINSTANCE.createToSink();
 			connection.setId(element.getAttribute("id"));
 			doAnnotations(connection, element);
 			doAttributes(connection, element);
 			ActorInstance sourceInstance = (ActorInstance) findIrObject(element.getAttribute("src"));
 			String portName = element.getAttribute("src-port");
 			connection.setSource(findPortInstance(sourceInstance, portName, true));
 			portName = element.getAttribute("dst-port");
 			connection.setSink(findPort(network, portName, true));
 
 			connection.getSource().getConnections().add(connection);
 			
 			return connection;
 		}
 		
 		assert(false);
 		return null;
 	}
 	
 	private Actor createActor(Element element) {
 		Actor actor = IrFactory.eINSTANCE.createActor();
 		String id = element.getAttribute("id");
 		actor.setId(id);
 		doAnnotations(actor, element);
 
 		addIrObject(id, actor);
 						
 		actor.setType((TypeActor) createType(getChild(element, "Type")));
 		
 		List<Element> declarations = getChildren(element, "Decl");
 		for (Element e : declarations) {
 			Declaration var = (Declaration ) createDeclaration(e);
 			actor.getDeclarations().add(var);
 			if (var instanceof Variable && ((Variable) var).isParameter())
 				actor.getParameters().add((Variable) var);
 		}
 				
 		List<Element> ports = getChildren(element, "Port");
 		for (Element e : ports) {
 			Port port = createPort(e);
 			if (e.getAttribute("direction").equals("in")) {
 				actor.getInputPorts().add(port);
 			} else {
 				actor.getOutputPorts().add(port);
 			}
 		}	
 		
 		List<Element> inits = getChildren(element, "Initializer");
 		for (Element e : inits) {
 			Action init = createAction(e);
 			actor.getInitializers().add(init);
 		}	
 		
 		List<Element> actions = getChildren(element, "Action");
 		for (Element e : actions) {
 			Action action = createAction(e);
 			actor.getActions().add(action);
 		}	
 		
 		actor.setSchedule(createSchedule(getChild(element, "Schedule")));
 				
 		return actor;
 	}
 
 	private ExternalActor createExternalActor(Element element) {
 		ExternalActor actor = IrFactory.eINSTANCE.createExternalActor();
 		String id = element.getAttribute("id");
 		doAnnotations(actor, element);
 
 		addIrObject(id, actor);
 				
 		actor.setType((TypeActor) createType(getChild(element, "Type")));
 		
 		List<Element> declarations = getChildren(element, "Decl");
 		for (Element e : declarations) {
 			Variable var = (Variable) createDeclaration(e);
 			actor.getDeclarations().add(var);
 			if (var.isParameter())
 				actor.getParameters().add(var);
 		}
 				
 		List<Element> ports = getChildren(element, "Port");
 		for (Element e : ports) {
 			Port port = createPort(e);
			if (element.getAttribute("direction").equals("in")) {
 				actor.getInputPorts().add(port);
 			} else {
 				actor.getOutputPorts().add(port);
 			}
 		}
 		
 		return actor;
 	}
 	
 	private Action createAction(Element element) {
 		Action action = IrFactory.eINSTANCE.createAction();
 		String id = element.getAttribute("id");
 		action.setId(id);
 		doAnnotations(action, element);
 
 		addIrObject(id, action);
 						
 		action.setOuter((Actor) findIrObject(element.getAttribute("outer-scope")));
 		List<Element> qualifiedName = getChildren(getChild(element, "QID"), "ID");
 		for (Element e : qualifiedName) {
 			action.getTag().add(e.getAttribute("name"));
 		}
 
 		List<Element> decls = getChildren(element, "Decl");
 		for (Element e : decls) {
 			Declaration decl = createDeclaration(e);
 			action.getDeclarations().add(decl);
 		}	
 		
 		List<Element> guards = getChildren(element, "Guard");
 		for (Element e : guards) {
 			Guard guard = createGuard(e);
 			action.getGuards().add(guard);
 		}	
 
 		List<Element> inputs = getChildren(element, "PortRead");
 		for (Element e : inputs) {
 			PortRead portRead = createPortRead(action, e);
 			action.getInputs().add(portRead);
 		}	
 		
 		List<Element> stmts = getChildren(element, "Stmt");
 		for (Element e : stmts) {
 			Statement stmt = createStatement(e);
 			action.getStatements().add(stmt);
 		}	
 
 		List<Element> outputs = getChildren(element, "PortWrite");
 		for (Element e : outputs) {
 			PortWrite portWrite = createPortWrite(action, e);
 			action.getOutputs().add(portWrite);
 		}	
 		
 		return action;
 	}
 
 	private Guard createGuard(Element element) {
 		Guard guard = IrFactory.eINSTANCE.createGuard();
 		String id = element.getAttribute("id");
 		guard.setId(id);
 		doAnnotations(guard, element);
 		
 		addIrObject(id, guard);
 				
 		Action action = (Action) findIrObject(element.getAttribute("outer-scope"));
 		guard.setOuter(action);
 		
 		List<Element> decls = getChildren(element, "Decl");
 		for (Element e : decls) {
 			Declaration decl = createDeclaration(e);
 			guard.getDeclarations().add(decl);
 		}	
 		
 		List<Element> peeks = getChildren(element, "PortPeek");
 		for (Element e : peeks) {
 			PortPeek peek = createPortPeek(action, e);
 			guard.getPeeks().add(peek);
 		}
 		
 		Expression body = createExpression(getChild(element, "Expr"));
 		guard.setBody(body);		
 		
 		guard.setType(TypeSystem.createTypeBool());
 		
 		return guard;
 	}
 	
 	private ActorInstance createActorInstance(Element element) {
 		ActorInstance instance = IrFactory.eINSTANCE.createActorInstance();
 		String id = element.getAttribute("id");		
 		instance.setId(id);		
 		doAnnotations(instance, element);
 		
 		addIrObject(id, instance);
 		
 		instance.setName(element.getAttribute("name"));
 		instance.setType(createType(getChild(element, "Type")));
 		
 		List<Element> ports = getChildren(element, "PortInstance");
 		for (Element e : ports) {
 			PortInstance portInstance = IrFactory.eINSTANCE.createPortInstance();
 			portInstance.setId(e.getAttribute("id"));
 			portInstance.setName(e.getAttribute("name"));
 			portInstance.setActor(instance);
 			if (e.getAttribute("direction").equals("in")) {
 				instance.getInputs().add(portInstance);
 			} else {
 				instance.getOutputs().add(portInstance);
 			}
 		}
 		
 		List<Element> actualParameters = getChildren(element, "ActualParameter");		
 		for (Element p : actualParameters) {
 			TaggedExpression te = IrFactory.eINSTANCE.createTaggedExpression();
 			te.setTag(p.getAttribute("name"));
 			te.setExpression(createExpression(getChild(p, "Expr")));
 			instance.getActualParameters().add(te);
 		}
 				
 		return instance;
 	}
 	
 	private Declaration createDeclaration(Element element) {
 		String kind = element.getAttribute("kind");
 		if (kind.equals("Variable")) {
 			Variable variable = IrFactory.eINSTANCE.createVariable();
 			variable.setName(element.getAttribute("name"));
 			String id = element.getAttribute("id");
 			variable.setId(id);	
 			doAnnotations(variable, element);
 
 			addIrObject(id, variable);
 						
 			variable.setScope((Scope) findIrObject(element.getAttribute("scope")));
 			
 			if (element.getAttribute("constant").equals("true")) {
 				variable.setConstant(true); 
 			} else {
 				variable.setConstant(false); 
 			}
 			if (element.getAttribute("parameter").equals("true")) {
 				variable.setParameter(true); 
 			} else {
 				variable.setParameter(false); 
 			}
 		
 			Element  typeElement = getChild(element, "Type");
 			Type type = createType(typeElement);
 			variable.setType(type);
 
 			Element initialValueElement = getChild(element, "InitialValue");
 			if (initialValueElement != null) {
 				Expression initalValue = createExpression(getChild(initialValueElement, "Expr"));
 				variable.setInitValue(initalValue);
 			}
 			
 			return variable; 
 		} else if (kind.equals("VariableExternal")) {
 			VariableExternal variableExternal = IrFactory.eINSTANCE.createVariableExternal();
 			variableExternal.setName(element.getAttribute("name"));
 			String id = element.getAttribute("id");
 			variableExternal.setId(id);		
 			doAnnotations(variableExternal, element);
 
 			addIrObject(id, variableExternal);			
 			
 			variableExternal.setScope((Scope) findIrObject(element.getAttribute("scope")));
 			Element  typeElement = getChild(element, "Type");
 			Type type = createType(typeElement);
 			variableExternal.setType(type);		
 									
 			return variableExternal;
 		} else if (kind.equals("VariableImport")) {
 			VariableImport variableImport = IrFactory.eINSTANCE.createVariableImport();
 			variableImport.setName(element.getAttribute("name"));			
 			String id = element.getAttribute("id");
 			variableImport.setId(id);	
 			doAnnotations(variableImport, element);
 			
 			addIrObject(id, variableImport);
 			
 			for (String s : Util.unpackQualifiedName(element.getAttribute("namespace"))) {
 				variableImport.getNamespace().add(s);
 			}					
 			
 			return variableImport;
 		} else if (kind.equals("TypeImport")) {
 			TypeDeclarationImport typeImport = IrFactory.eINSTANCE.createTypeDeclarationImport();
 			typeImport.setName(element.getAttribute("name"));
 			String id = element.getAttribute("id");
 			typeImport.setId(id);	
 			doAnnotations(typeImport, element);
 
 			addIrObject(id, typeImport);
 						
 			for (String s : Util.unpackQualifiedName(element.getAttribute("namespace"))) {
 				typeImport.getNamespace().add(s);
 			}
 			
 			return typeImport;
 		} else if (kind.equals("Forward")) {
 			ForwardDeclaration forwardDeclaration = IrFactory.eINSTANCE.createForwardDeclaration();
 			forwardDeclaration.setName(element.getAttribute("name"));
 			String id = element.getAttribute("id");
 			forwardDeclaration.setId(id);	
 			doAnnotations(forwardDeclaration, element);
 
 			addIrObject(id, forwardDeclaration);
 						
 			forwardDeclaration.setScope((Scope) findIrObject(element.getAttribute("scope")));
 			Element  typeElement = getChild(element, "Type");
 			Type type = createType(typeElement);
 			forwardDeclaration.setType(type);
 					
 			// Since the declaration that the ForwardDeclaration 
 			// is pointing at does not yet exists. That value must 
 			// be set in a second pass after the whole actor/namespace
 			// is evaluted. The id of the actual declaration is stored 
 			// temprary map.
 			
 			forwardDeclarationMap.put(forwardDeclaration, element.getAttribute("forward-id"));
 			
 			return forwardDeclaration;
 		} else if (kind.equals("Type")) {
 			TypeDeclaration typeDeclaration = IrFactory.eINSTANCE.createTypeDeclaration();
 			String id = element.getAttribute("id");
 			typeDeclaration.setId(id);
 			doAnnotations(typeDeclaration, element);
 
 			addIrObject(id, typeDeclaration);
 						
 			typeDeclaration.setName(element.getAttribute("name"));
 		
 			typeDeclaration.setScope((Scope) findIrObject(element.getAttribute("scope")));
 
 			Element typeElement = getChild(element, "Type");
 			Type type = createType(typeElement);
 			typeDeclaration.setType(type);
 					
 			Element ctorElement = getChild(element, "TypeConstructor");
 			TypeConstructor ctor = IrFactory.eINSTANCE.createTypeConstructor();
 			ctor.setName(ctorElement.getAttribute("name"));
 			ctor.setId(ctorElement.getAttribute("id"));
 			doAnnotations(ctor, ctorElement);
 			ctor.setScope((Scope) findIrObject(ctorElement.getAttribute("scope")));
 			ctor.setTypedef(typeDeclaration);
 
 			typeDeclaration.setConstructor(ctor);
 			
 			return typeDeclaration;			
 		}
 		
 		assert(false);
 		return null;
 	}	
 		
 	private Expression createExpression(Element element) {
 		String kind = element.getAttribute("kind");
 		
 		if (kind.equals("Unary")) {
 			UnaryExpression expr = IrFactory.eINSTANCE.createUnaryExpression();
 			expr.setId(element.getAttribute("id"));			
 			doAnnotations(expr, element);	
             Element child = getChild(element,"Type");
             if(child!=null) {
                 expr.setType(createType(child));
             }
 			expr.setContext((Scope) findIrObject(element.getAttribute("context-scope"))); 
 			expr.setOperator(UtilIR.unmarshall(element.getAttribute("operator")));			
 			expr.setOperand(createExpression(getChild(element, "Expr")));
 			// expr.setType(createType(getChild(element, "Type")));
 			
 			return expr;
 		} else if (kind.equals("Binary")) {
 			BinaryExpression expr = IrFactory.eINSTANCE.createBinaryExpression();
 			expr.setId(element.getAttribute("id"));
 			doAnnotations(expr, element);			
             Element child = getChild(element,"Type");
             if(child!=null) {
                 expr.setType(createType(child));
             }
 			expr.setContext((Scope) findIrObject(element.getAttribute("context-scope"))); 
 			expr.setOperator(UtilIR.unmarshall(element.getAttribute("operator")));			
 			List<Element> operands = getChildren(element, "Expr");
 			expr.setOperand1(createExpression(operands.get(0)));
 			expr.setOperand2(createExpression(operands.get(1)));			
 			// expr.setType(createType(getChild(element, "Type")));
 			
 			return expr;			
 		} else if (kind.equals("Call")) {
 			FunctionCall expr = IrFactory.eINSTANCE.createFunctionCall();
 			expr.setId(element.getAttribute("id"));
 			doAnnotations(expr, element);
             Element child = getChild(element,"Type");
             if(child!=null) {
                 expr.setType(createType(child));
             }
 			expr.setContext((Scope) findIrObject(element.getAttribute("context-scope"))); 
 			expr.setFunction(createExpression(getChild(element, "Expr")));
 			List<Element> args = getChildren(getChild(element, "Args"), "Expr");
 			for (Element arg : args) {
 				expr.getParameters().add(createExpression(arg));
 			}
 			// expr.setType(createType(getChild(element, "Type")));
 			
 			return expr;			
 		} else if (kind.equals("Construction")) {
 			TypeConstructorCall expr = IrFactory.eINSTANCE.createTypeConstructorCall();
 			expr.setId(element.getAttribute("id"));
 			doAnnotations(expr, element);
             Element child = getChild(element,"Type");
             if(child!=null) {
                 expr.setType(createType(child));
             }
 			expr.setName(element.getAttribute("name"));
 			expr.setContext((Scope) findIrObject(element.getAttribute("context-scope"))); 
 			expr.setTypedef((Declaration) findIrObject(element.getAttribute("typedef-id")));
 			List<Element> args = getChildren(getChild(element, "Args"), "Expr");
 			for (Element arg : args) {
 				expr.getParameters().add(createExpression(arg));
 			}
 			// expr.setType(createType(getChild(element, "Type")));
 			
 			return expr;						
 		} else if (kind.equals("If")) {
 			IfExpression expr = IrFactory.eINSTANCE.createIfExpression();
 			expr.setId(element.getAttribute("id"));
 			doAnnotations(expr, element);
             Element child = getChild(element,"Type");
             if(child!=null) {
                 expr.setType(createType(child));
             }
 			expr.setContext((Scope) findIrObject(element.getAttribute("context-scope"))); 
 			List<Element> exprs = getChildren(element, "Expr");
 			expr.setCondition(createExpression(exprs.get(0)));
 			expr.setThenExpression(createExpression(exprs.get(1)));
 			expr.setElseExpression(createExpression(exprs.get(2)));
 			//expr.setType(createType(getChild(element, "Type")));
 			
 			return expr;
 		} else if (kind.equals("Var")) {
 			VariableExpression expr = IrFactory.eINSTANCE.createVariableExpression();
 			expr.setId(element.getAttribute("id"));
 			doAnnotations(expr, element);
 			Element child = getChild(element,"Type");
 			if(child!=null) {
 				expr.setType(createType(child));
 			}
 			expr.setContext((Scope) findIrObject(element.getAttribute("context-scope"))); 
 			
 			expr.setVariable((Declaration) findIrObject(element.getAttribute("decl-id")));
 						
 			Element indices = getChild(element, "Indices");
 			if (indices != null) {
 				for (Element index : getChildren(indices, "Expr")) {
 					expr.getIndex().add(createExpression(index));
 				}				
 			}
 
 			Element members = getChild(element, "Members");
 			if (members != null) {
 				for (Element member :  getChildren(members, "Member")) {
 					expr.getMember().add(createMember(member));
 				}
 			}
 			
 			return expr;						
 		} else if (kind.equals("Lambda")) {
 			LambdaExpression expr = IrFactory.eINSTANCE.createLambdaExpression();
 			String id = element.getAttribute("id");
 			expr.setId(id);
 			doAnnotations(expr, element);
 
 			addIrObject(id, expr);
 
 			expr.setContext((Scope) findIrObject(element.getAttribute("context-scope"))); 
 			expr.setOuter((Scope) findIrObject(element.getAttribute("outer-scope")));
 			
 			expr.setType(createType(getChild(element, "Type")));
 			
 			List<Element> declarations = getChildren(element, "Decl");
 			for (Element e : declarations) {
 				Declaration var = (Declaration ) createDeclaration(e);
 				expr.getDeclarations().add(var);
 				if (var instanceof Variable && ((Variable) var).isParameter())
 					expr.getParameters().add((Variable) var);
 			}
 						
 			expr.setBody(createExpression(getChild(element, "Expr")));
 			
 			return expr;
 		} else if (kind.equals("Proc")) {
 			ProcExpression expr = IrFactory.eINSTANCE.createProcExpression();
 			String id = element.getAttribute("id");
 			expr.setId(id);
 			doAnnotations(expr, element);
 
 			addIrObject(id, expr);
 			
 			expr.setContext((Scope) findIrObject(element.getAttribute("context-scope"))); 
 			expr.setOuter((Scope) findIrObject(element.getAttribute("outer-scope")));
 			
 			expr.setType(createType(getChild(element, "Type")));
 			
 			List<Element> declarations = getChildren(element, "Decl");
 			for (Element e : declarations) {
 				Declaration var = (Declaration ) createDeclaration(e);
 				expr.getDeclarations().add(var);
 				if (var instanceof Variable && ((Variable) var).isParameter())
 					expr.getParameters().add((Variable) var);
 			}
 			
 			expr.setBody((Block) createStatement(getChild(element, "Stmt")));
 			
 			return expr;
 		} else if (kind.equals("List")) {
 			ListExpression expr = IrFactory.eINSTANCE.createListExpression();
 			expr.setId(element.getAttribute("id"));
 			doAnnotations(expr, element);
             Element child = getChild(element,"Type");
             if(child!=null) {
                 expr.setType(createType(child));
             }
 
 			expr.setContext((Scope) findIrObject(element.getAttribute("context-scope"))); 
 			
 			List<Element> generators = getChildren(element, "Generator");
 			for (Element g : generators) {	
 				expr.getGenerators().add(createGenerator(g));
 			}
 			
 			List<Element> exprs = getChildren(element, "Expr");
 			for (Element e : exprs) {
 				expr.getExpressions().add(createExpression(e));
 			}
 			// expr.setType(createType(getChild(element, "Type")));
 			
 			return expr;	
 		} else if (kind.equals("literal-integer")) {
 			IntegerLiteral expr = IrFactory.eINSTANCE.createIntegerLiteral();
 			String value = element.getAttribute("value");
 			expr.setValue(Long.parseLong(value));
 			expr.setType(TypeSystem.createTypeInt());
 			
 			return expr;	
 		} else if (kind.equals("literal-float")) {
 			FloatLiteral expr = IrFactory.eINSTANCE.createFloatLiteral();
 			String value = element.getAttribute("value");
 			expr.setValue(Double.parseDouble(value));
 			expr.setType(TypeSystem.createTypeFloat());
 			
 			return expr;	
 			
 		} else if (kind.equals("literal-bool")) {
 			BooleanLiteral expr = IrFactory.eINSTANCE.createBooleanLiteral();
 			String value = element.getAttribute("value");
 			expr.setValue(Boolean.parseBoolean(value));
 			expr.setType(TypeSystem.createTypeBool());
 			
 			return expr;	
 			
 		} else if (kind.equals("literal-string")) {
 			StringLiteral expr = IrFactory.eINSTANCE.createStringLiteral();
 			String value = element.getAttribute("value");
 			expr.setValue(value);
 			expr.setType(TypeSystem.createTypeString());
 			
 			return expr;				
 		} 
 		
 		assert(false);
 		return null;
 	}
 	
 	private Generator createGenerator(Element element) {
 		Generator generator = IrFactory.eINSTANCE.createGenerator();
 		String id = element.getAttribute("id");
 		generator.setId(id);
 		doAnnotations(generator, element);
 
 		addIrObject(id, generator);
 		
 		generator.setOuter((Scope) findIrObject(element.getAttribute("outer-scope")));
 
 		List<Element> declarations = getChildren(element, "Decl");
 		for (Element e : declarations) {
 			Declaration var = (Declaration ) createDeclaration(e);
 			generator.getDeclarations().add(var);
 		}
 		generator.setSource(createExpression(getChild(element, "Expr")));
 		
 		return generator;
 	}
 
 	
 	private VariableReference createVariableReference(Element element) {
 		VariableReference varRef = IrFactory.eINSTANCE.createVariableReference();
 		varRef.setId(element.getAttribute("id"));
 		doAnnotations(varRef, element);
 		Element child = getChild(element,"Type");
 		if(child!=null) {
 			varRef.setType(createType(child));
 		}
 		varRef.setDeclaration((Variable) findIrObject(element.getAttribute("decl-id")));
 		
 		Element indices = getChild(element, "Indices");
 		if (indices != null) {
 			for (Element index : getChildren(indices, "Expr")) {
 				varRef.getIndex().add(createExpression(index));
 			}				
 		}
 
 		Element members = getChild(element, "Members");
 		if (members != null) {
 			for (Element member :  getChildren(members, "Member")) {
 				varRef.getMember().add(createMember(member));
 			}
 		}	
 			
 		return varRef;
 	}
 	
 	private Member createMember(Element element) {
 		Member member = IrFactory.eINSTANCE.createMember();
 		member.setId(element.getAttribute("id"));
 		doAnnotations(member, element);
 		Element child = getChild(element,"Type");
 		if(child!=null) {
 			member.setType(createType(child));
 		}
 		member.setName(element.getAttribute("name"));
 		Element indicesElement = getChild(element, "Indices");
 		
 		if (indicesElement != null) {		
 			List<Element> indices = getChildren(indicesElement, "Expr");
 			for (Element index : indices) {
 				member.getIndex().add(createExpression(index));
 			}
 			//member.setType(createType(getChild(element, "Type")));
 		}
 		
 		return member;
 	}
 
 	private Statement createStatement(Element element) {
 		String kind = element.getAttribute("kind");
 		
 		if (kind.equals("Assign")) {
 			Assign stmt = IrFactory.eINSTANCE.createAssign();
 			doAnnotations(stmt, element);
 			stmt.setTarget(createVariableReference(getChild(element, "Var")));
 			stmt.setExpression(createExpression(getChild(element, "Expr")));
 			
 			return stmt;
 		} if (kind.equals("Call")) {
 			ProcCall stmt = IrFactory.eINSTANCE.createProcCall();
 			doAnnotations(stmt, element);
 			stmt.setProcedure((Declaration) findIrObject(element.getAttribute("decl-id")));
 			List<Element> args = getChildren(getChild(element, "Args"), "Expr");
 			for (Element arg : args) {
 				stmt.getInParameters().add(createExpression(arg));
 			}
 			
 			return stmt;			
 		} if (kind.equals("While")) {
 			WhileLoop stmt = IrFactory.eINSTANCE.createWhileLoop();
 			doAnnotations(stmt, element);
 			stmt.setCondition(createExpression(getChild(element, "Expr")));
 			stmt.setBody((Block) createStatement(getChild(element, "Stmt")));
 			
 			return stmt;
 		} if (kind.equals("ForEach")) {
 			ForEach stmt = IrFactory.eINSTANCE.createForEach();		
 			doAnnotations(stmt, element);
 			List<Element> generators = getChildren(element, "Generator");
 			for (Element g : generators) {	
 				stmt.getGenerators().add(createGenerator(g));
 			}			
 			stmt.setBody((Block) createStatement(getChild(element, "Stmt")));
 
 			return stmt;
 		} if (kind.equals("If")) {
 			IfStatement stmt = IrFactory.eINSTANCE.createIfStatement();
 			doAnnotations(stmt, element);
 			stmt.setCondition(createExpression(getChild(element, "Expr")));
 			List<Element> stmts = getChildren(element, "Stmt");
 			stmt.setThenBlock((Block) createStatement(stmts.get(0)));
 			if (stmts.size() == 2) {
 				stmt.setElseBlock((Block) createStatement(stmts.get(1)));
 			}
 			return stmt;						
 		} if (kind.equals("Block")) {
 			Block stmt = IrFactory.eINSTANCE.createBlock();
 			String id = element.getAttribute("id");
 			stmt.setId(id);
 			doAnnotations(stmt, element);
 			stmt.setOuter((Scope) findIrObject(element.getAttribute("outer-scope")));
 			
 			this.addIrObject(id, stmt);
 			
 			List<Element> decls = getChildren(element, "Decl");
 			for (Element e : decls) {
 				stmt.getDeclarations().add(createDeclaration(e));
 			}
 			
 			List<Element> stmts = getChildren(element, "Stmt");
 			for (Element e : stmts) {
 				stmt.getStatements().add(createStatement(e));
 			}
 		   
 			return stmt;
 		} else if (kind.equals("ReturnValue")) {
 			ReturnValue stmt = IrFactory.eINSTANCE.createReturnValue();
             doAnnotations(stmt, element);
 			stmt.setValue(createExpression(getChild(element, "Expr"))); 
 			
 			return stmt;
 		}
 		
 		assert(false);
 		return null;
 	}	
 
 	private PortRead createPortRead(Action action, Element element) {
 		PortRead portRead = IrFactory.eINSTANCE.createPortRead();
 		portRead.setId(element.getAttribute("id"));
 		doAnnotations(portRead, element);
 		portRead.setPort(findPort((Actor) action.getOuter(), element.getAttribute("port"), false));
 		
 		List<Element> vars = getChildren(element, "Var");
 		for (Element e : vars) {
 			VariableReference var = createVariableReference(e);
 			portRead.getVariables().add(var);
 		}
 		
 		Element repeat = getChild(element, "Repeat");
 		if (repeat != null) {
 			portRead.setRepeat(createExpression(getChild(repeat, "Expr")));
 		}
 
 		
 		return portRead;
 	}
 
 	private PortPeek createPortPeek(Action action, Element element) {
 		PortPeek portPeek = IrFactory.eINSTANCE.createPortPeek();
 		portPeek.setId(element.getAttribute("id"));
 		doAnnotations(portPeek, element);
 		portPeek.setPort(findPort((Actor) action.getOuter(), element.getAttribute("port"), false));
 		portPeek.setPosition(Integer.parseInt(element.getAttribute("position")));
 		Element repeat = getChild(element, "Repeat");
 		if (repeat != null) {
 			portPeek.setRepeat(createExpression(getChild(repeat, "Expr")));
 		}
 
 		portPeek.setVariable(createVariableReference(getChild(element, "Var")));
 		
 		return portPeek;
 	}
 
 	private PortWrite createPortWrite(Action action, Element element) {
 		PortWrite portWrite = IrFactory.eINSTANCE.createPortWrite();
 		String id = element.getAttribute("id");
 		portWrite.setId(id);			
 		doAnnotations(portWrite, element);
 		addIrObject(id, portWrite);
 
 		portWrite.setOuter(action);
 		
 		portWrite.setPort(findPort((Actor) action.getOuter(), element.getAttribute("port"), true));
 		
 		List<Element> decls = getChildren(element, "Decl");
 		for (Element e : decls) {
 			Declaration decl = createDeclaration(e);
 			portWrite.getDeclarations().add(decl);
 		}
 		
 		List<Element> stmts = getChildren(element, "Stmt");
 		for (Element e : stmts) {
 			Statement stmt = createStatement(e);
 			portWrite.getStatements().add(stmt);
 		}	
 		
 		List<Element> exprs = getChildren(element, "Expr");
 		for (Element e : exprs) {
 			Expression expr = createExpression(e);
 			portWrite.getExpressions().add(expr);
 		}
 		
 		Element repeat = getChild(element, "Repeat");
 		if (repeat != null) {
 			portWrite.setRepeat(createExpression(getChild(repeat, "Expr")));
 		}
 		
 		return portWrite;
 	}
 
 	private Port createPort(Element element) {
 		Port port = IrFactory.eINSTANCE.createPort();
 		port.setId(element.getAttribute("id"));
 		port.setName(element.getAttribute("name"));
 		port.setType(createType(getChild(element, "Type")));
 
 		return port;
 	}
 	
 	private Type createType(Element element) {
 		String kind = element.getAttribute("kind");
 		
 		if (kind.equals("int")) {
 			TypeInt type = TypeSystem.createTypeInt();
 			List<Element> expressions = getChildren(element, "Expr");
 			if (expressions.size() > 0) {
 				Expression size = createExpression(expressions.get(0));
 				type.setSize(size);
 			}
 			return type;
 		} else if (kind.equals("uint")) {
 			TypeUint type = TypeSystem.createTypeUint();
 			List<Element> expressions = getChildren(element, "Expr");
 			if (expressions.size() > 0) {
 				Expression size = createExpression(expressions.get(0));
 				type.setSize(size);
 			}
 			return type;
 		} else if (kind.equals("bool")) {
 			return TypeSystem.createTypeBool();			
 		} else if (kind.equals("float")) {
 			return TypeSystem.createTypeFloat();
 		} else if (kind.equals("string")) {
 			return TypeSystem.createTypeString();
 		} else if (kind.equals("List")) {
 			Expression size = null;
 			Element sizeElement = getChild(element, "Expr");
 			if (sizeElement != null) {
 				size = createExpression(sizeElement);
 			}
 			List<Element> types = getChildren(element, "Type");
 			Type type = createType(types.get(0));
 			return TypeSystem.createTypeList(size, type);
 		} else if (kind.equals("user")) {
 			Declaration typeDeclaration = (Declaration) findIrObject(element.getAttribute("type-declaration-id"));
 			return TypeSystem.createTypeUser(typeDeclaration);
 		} else if (kind.equals("record")) {
 			TypeRecord typeRecord = IrFactory.eINSTANCE.createTypeRecord();
 			String id = element.getAttribute("id");
 			typeRecord.setId(id);
 			doAnnotations(typeRecord, element);
 			
 			List<Element> members = getChildren(element, "Decl");			
 			for (Element member : members) {
 				Variable m = (Variable) createDeclaration(member);
 				typeRecord.getMembers().add(m);
 			}
 			
 			addIrObject(id, typeRecord);
 			
 			return typeRecord;
 		} else if (kind.equals("lambda")) {
 			TypeLambda type = IrFactory.eINSTANCE.createTypeLambda();
 			Element input = getChild(element, "Input");
 			for (Element e : getChildren(input, "Type"))  {
 				type.getInputTypes().add(createType(e));
 			}
 			Element output = getChild(element, "Output");
 			if (output != null) {				
 				type.setOutputType(createType(getChild(output, "Type")));
 			}
 			return type;					
 		} else if (kind.equals("proc")) {
 			TypeProc type = IrFactory.eINSTANCE.createTypeProc();
 			Element input = getChild(element, "Input");
 			for (Element e : getChildren(input, "Type"))  {
 				type.getInputTypes().add(createType(e));
 			}
 			Element output = getChild(element, "Output");
 			if (output != null) {	
 				for (Element e : getChildren(input, "Type"))  {
 					type.getOutputTypes().add(createType(e));
 				}
 			}
 			return type;					
 		} else if (kind.equals("actor")) {
 			String name = element.getAttribute("name");			
 			String namespace = element.getAttribute("namespace"); 
 			return TypeSystem.createTypeActor(name, Util.unpackQualifiedName(namespace));					
 		} else if (kind.equals("undef")) {
 			return TypeSystem.createTypeUndef();			
 		}
 		
 		assert(false);
 		return null;
 	}
 	
  
 	private Schedule createSchedule(Element element) {
  		
  		Schedule schedule = IrFactory.eINSTANCE.createSchedule();
  		
  		String initialState = getChild(element, "InitialState").getAttribute("name");
  		// schedule.setInitialState(createState());
  	
  		for (Element e : getChildren(element, "State")) {
  			State state = IrFactory.eINSTANCE.createState();
  			String name = e.getAttribute("name");
  			state.setName(name);
  			if (name.equals(initialState)) {
  				schedule.setInitialState(state);
  			} 			
  			
  			Element graphElement = getChild(e, "PriorityGraph");
  			PriorityGraph graph = new PriorityGraph();
 
  			for (Element vertex : getChildren(graphElement, "Vertex")) {
  				Action action = (Action) findIrObject(vertex.getAttribute("action"));
  				graph.addVertex(action);
  			}
  			
  			for (Element edge : getChildren(graphElement, "Edge")) {
  				Action source = (Action) findIrObject(edge.getAttribute("source"));
  				Action target = (Action) findIrObject(edge.getAttribute("target"));
  				graph.addEdge(source, target);
  			}
  			
  			state.setPriorityGraph(graph);
  			
 			Map<Action, String> action2TargetMap = new HashMap<Action, String>();
 			for (Element targetState : getChildren(graphElement, "TargetState")) {
 				action2TargetMap.put((Action) findIrObject(targetState.getAttribute("action")), targetState.getAttribute("target"));
 			}
  			
 			state.setAction2TargetMap(action2TargetMap);
 			
  			schedule.getStates().add(state);
  		}
  		
  		for (Element freeRunner : getChildren(element, "FreeRunner")) {
  			schedule.getFreeRunners().add((Action) findIrObject(freeRunner.getAttribute("action")));
  		}	
 
  		Element graphElement = getChild(element, "PriorityGraph");
  		PriorityGraph graph = new PriorityGraph();
 		
  		for (Element vertex : getChildren(graphElement, "Vertex")) {
  			Action action = (Action) findIrObject(vertex.getAttribute("action"));
  			graph.addVertex(action);
  		}
 
  		for (Element edge : getChildren(graphElement, "Edge")) {
  			Action source = (Action) findIrObject(edge.getAttribute("source"));
  			Action target = (Action) findIrObject(edge.getAttribute("target"));
  			graph.addEdge(source, target);
  		}			
 
  		schedule.setPriorityGraph(graph);
  		
  		return schedule;
  	}
 
 
  	
 	private PortInstance findPortInstance(ActorInstance instance, String portName, boolean outputs) {
 		List<PortInstance> ports = outputs ? instance.getOutputs() : instance.getInputs();
 		for (PortInstance port : ports) {
 			if (port.getName().equals(portName))
 				return port;
 		}
 		
 		assert(false);
 		return null;
 	}
 
 	private Port findPort(AbstractActor actor, String portName, boolean output) {
 		List<Port> ports = output ? actor.getOutputPorts() : actor.getInputPorts();
 		for (Port port : ports) {
 			if (port.getName().equals(portName))
 				return port;
 		}
 		
 		assert(false);
 		return null;
 	}
 
 	private void doAnnotations(org.caltoopia.ir.Node node, Element element) {
 		List<Element> annotations = getChildren(element, "Annotation");
 		for (Element a : annotations) {
 			Annotation annotation = IrFactory.eINSTANCE.createAnnotation();
 			annotation.setName(a.getAttribute("name"));
 			List<Element> annotationArguments = getChildren(a, "AnnotationArgument");	
 			for (Element arg : annotationArguments) {
 				AnnotationArgument annotationArgument = IrFactory.eINSTANCE.createAnnotationArgument();
 				annotationArgument.setId(arg.getAttribute("id"));
 				annotationArgument.setValue(arg.getAttribute("value"));
 				annotation.getArguments().add(annotationArgument);
 			}
 			
 			node.getAnnotations().add(annotation);
 		}
 	}
  	
 	private void doAttributes(Connection connection, Element element) {
 		List<Element> attributes = getChildren(element, "Attribute");
 		for (Element a : attributes) {
 			TaggedExpression attribute = IrFactory.eINSTANCE.createTaggedExpression();
 			attribute.setTag(a.getAttribute("tag"));
 			attribute.setExpression(createExpression(getChild(a, "Expr")));
 			connection.getAttributes().add(attribute);
 		}
 	}
 	
 	private List<Element> getChildren(Element parent, String tagName) {
 		NodeList children = parent.getChildNodes();
 		List<Element> result = new ArrayList<Element>();
 
 		for (int i = 0; i < children.getLength(); i++) {
 			Node node = children.item(i);
 			if (node.getNodeType() == Node.ELEMENT_NODE) {	
 				Element element = (Element) node;
 				if (element.getTagName().equals(tagName)) {
 					result.add(element);
 				}
 			}
 		}
 		return result;
 	}
 	
 	private Element getChild(Element parent, String tagName) {
 		NodeList children = parent.getChildNodes();
 		List<Element> result = new ArrayList<Element>();
 		
 		for (int i = 0; i < children.getLength(); i++) {
 			Node node = children.item(i);
 			if (node.getNodeType() == Node.ELEMENT_NODE) {	
 				Element element = (Element) node;
 				if (element.getTagName().equals(tagName)) {
 					result.add(element);
 				}
 			}
 		}
 		assert(result.size() <= 1);
 		if (result.size() == 1) {	
 			return result.get(0);
 		} else {
 			return null;
 		}
 	}
 	
 	private EObject findIrObject(String key) {
 		EObject obj = objectMap.get(key);
 		if (obj == null) {
 			System.err.println("........ ObjectMap ........");
 			for (String k : objectMap.keySet()) {
 				System.err.println(k + " : " +  objectMap.get(k));
 			}
 			System.err.println("Failed for key=" + key + " ==> " + obj);
 			new Throwable().printStackTrace();
 			assert(obj != null);
 		}
 
 		return obj;
 	}
 
 	private void addIrObject(String key, EObject obj) {
 		assert(!objectMap.containsKey(key));
 		objectMap.put(key, obj);
 	}
 	
 }
