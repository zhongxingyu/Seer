 /*******************************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.bpel.ui.util;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.wsdl.Input;
 import javax.wsdl.Output;
 import javax.xml.namespace.QName;
 
 import org.eclipse.bpel.common.extension.model.ExtensionMap;
 import org.eclipse.bpel.common.extension.model.ExtensionmodelFactory;
 import org.eclipse.bpel.common.extension.model.adapters.ExtendedObjectUserAdapter;
 import org.eclipse.bpel.model.Activity;
 import org.eclipse.bpel.model.BPELFactory;
 import org.eclipse.bpel.model.BPELPackage;
 import org.eclipse.bpel.model.Case;
 import org.eclipse.bpel.model.Catch;
 import org.eclipse.bpel.model.CatchAll;
 import org.eclipse.bpel.model.Compensate;
 import org.eclipse.bpel.model.CompensationHandler;
 import org.eclipse.bpel.model.Condition;
 import org.eclipse.bpel.model.Copy;
 import org.eclipse.bpel.model.CorrelationSet;
 import org.eclipse.bpel.model.Correlations;
 import org.eclipse.bpel.model.EventHandler;
 import org.eclipse.bpel.model.Expression;
 import org.eclipse.bpel.model.FaultHandler;
 import org.eclipse.bpel.model.From;
 import org.eclipse.bpel.model.Invoke;
 import org.eclipse.bpel.model.Link;
 import org.eclipse.bpel.model.OnAlarm;
 import org.eclipse.bpel.model.OnEvent;
 import org.eclipse.bpel.model.OnMessage;
 import org.eclipse.bpel.model.Otherwise;
 import org.eclipse.bpel.model.PartnerActivity;
 import org.eclipse.bpel.model.PartnerLink;
 import org.eclipse.bpel.model.Pick;
 import org.eclipse.bpel.model.Process;
 import org.eclipse.bpel.model.Receive;
 import org.eclipse.bpel.model.Reply;
 import org.eclipse.bpel.model.Scope;
 import org.eclipse.bpel.model.Source;
 import org.eclipse.bpel.model.Switch;
 import org.eclipse.bpel.model.Targets;
 import org.eclipse.bpel.model.Throw;
 import org.eclipse.bpel.model.Variable;
 import org.eclipse.bpel.model.Variables;
 import org.eclipse.bpel.model.Wait;
 import org.eclipse.bpel.model.While;
 import org.eclipse.bpel.model.messageproperties.Property;
 import org.eclipse.bpel.model.messageproperties.PropertyAlias;
 import org.eclipse.bpel.model.partnerlinktype.PartnerLinkType;
 import org.eclipse.bpel.model.partnerlinktype.PartnerlinktypeFactory;
 import org.eclipse.bpel.model.partnerlinktype.PartnerlinktypePackage;
 import org.eclipse.bpel.model.partnerlinktype.Role;
 import org.eclipse.bpel.ui.BPELEditor;
 import org.eclipse.bpel.ui.IBPELUIConstants;
 import org.eclipse.bpel.ui.Policy;
 import org.eclipse.bpel.ui.adapters.IContainer;
 import org.eclipse.bpel.ui.adapters.IExtensionFactory;
 import org.eclipse.bpel.ui.adapters.INamedElement;
 import org.eclipse.bpel.ui.commands.AddRoleCommand;
 import org.eclipse.bpel.ui.commands.CreatePartnerLinkTypeCommand;
 import org.eclipse.bpel.ui.commands.SetPartnerLinkTypeCommand;
 import org.eclipse.bpel.ui.commands.SetRoleCommand;
 import org.eclipse.bpel.ui.commands.SetUniqueNameCommand;
 import org.eclipse.bpel.ui.expressions.IEditorConstants;
 import org.eclipse.bpel.ui.uiextensionmodel.ActivityExtension;
 import org.eclipse.bpel.ui.uiextensionmodel.CaseExtension;
 import org.eclipse.bpel.ui.uiextensionmodel.OnAlarmExtension;
 import org.eclipse.bpel.ui.uiextensionmodel.OnEventExtension;
 import org.eclipse.bpel.ui.uiextensionmodel.OnMessageExtension;
 import org.eclipse.bpel.ui.uiextensionmodel.ProcessExtension;
 import org.eclipse.bpel.ui.uiextensionmodel.StartNode;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.commands.CompoundCommand;
 import org.eclipse.wst.wsdl.Definition;
 import org.eclipse.wst.wsdl.ExtensibilityElement;
 import org.eclipse.wst.wsdl.ExtensibleElement;
 import org.eclipse.wst.wsdl.Fault;
 import org.eclipse.wst.wsdl.Message;
 import org.eclipse.wst.wsdl.Operation;
 import org.eclipse.wst.wsdl.Part;
 import org.eclipse.wst.wsdl.PortType;
 import org.eclipse.wst.wsdl.WSDLPackage;
 import org.eclipse.xsd.XSDElementDeclaration;
 import org.eclipse.xsd.XSDTypeDefinition;
 
 
 /**
  * This class provides a common interface (i.e. setXX/getXX/isXXAffected) to certain
  * properties which exist across several model object types.
  * 
  * Generally, you should only call the interfaces methods on model objects which
  * support the particular property; generally, these helpers will throw
  * IllegalArgumentException on objects which don't support the property in question.
  * This is analogous to the way casting an object to an interface type would throw
  * a ClassCastException if the object didn't support (implement) that interface.
  */
 public class ModelHelper {
 	
 	public static Object[] EMPTY_ARRAY = new Object[0];
 
 	// These constants are used by getVariable() and setVariable().
 	public static final int OUTGOING = 0;
 	public static final int INCOMING = 1;
 	
 	// These constants are used by getExpression() and setExpression().
 	// expression kinds
 	public static final int DEFAULT_EXPR = 0;
 	public static final int JOIN_EXPR = 1;
 	public static final int TRANSITION_EXPR = 2;
 	public static final int REPEATEVERY_EXPR = 3;
 	public static final int WAIT_EXPR = 4;
 
 	// expression sub-kinds
 	public static final int ESUB_DEFAULT = 0;
 	public static final int ESUB_FOR = 1;
 	public static final int ESUB_UNTIL = 2;
 	
 	// These constants are used by getRole() and setRole().	
 	public static final int MY_ROLE = 0;
 	public static final int PARTNER_ROLE = 1;
 
 	public static String getDisplayName(Object context) {
 		if (context instanceof Case) {
 			CaseExtension extension = (CaseExtension)getExtension((Case)context);
 			return extension==null? null : extension.getDisplayName();
 		}
 		if (context instanceof OnMessage) {
 			OnMessageExtension extension = (OnMessageExtension)getExtension((OnMessage)context);
 			return extension==null? null : extension.getDisplayName();
 		}
 		if (context instanceof OnEvent) {
 			OnEventExtension extension = (OnEventExtension)getExtension((OnEvent)context);
 			return extension==null? null : extension.getDisplayName();
 		}
 		if (context instanceof OnAlarm) {
 			OnAlarmExtension extension = (OnAlarmExtension)getExtension((OnAlarm)context);
 			return extension==null? null : extension.getDisplayName();
 		}
 		return null;
 	}
 	
 	public static boolean supportsUIExtensionDisplayName(Object context) {
 		if (context instanceof Case) return true;
 		if (context instanceof OnAlarm) return true;
 		if (context instanceof OnEvent) return true;
 		if (context instanceof OnMessage) return true;
 		return false;
 	}
 	
 	public static void setBPELUIExtensionDisplayName(Object context, String newDisplayName) {
 		if (context instanceof Case) {
 			CaseExtension extension = (CaseExtension)getExtension((Case)context);
 			if (extension == null) throw new IllegalStateException();
 			extension.setDisplayName(newDisplayName);
 			return;
 		}
 		if (context instanceof OnMessage) {
 			OnMessageExtension extension = (OnMessageExtension)getExtension((OnMessage)context);
 			if (extension == null) throw new IllegalStateException();
 			extension.setDisplayName(newDisplayName);
 			return;
 		}
 		if (context instanceof OnEvent) {
 			OnEventExtension extension = (OnEventExtension)getExtension((OnEvent)context);
 			if (extension == null) throw new IllegalStateException();
 			extension.setDisplayName(newDisplayName);
 			return;
 		}
 		if (context instanceof OnAlarm) {
 			OnAlarmExtension extension = (OnAlarmExtension)getExtension((OnAlarm)context);
 			if (extension == null) throw new IllegalStateException();
 			extension.setDisplayName(newDisplayName);
 			return;
 		}
 		throw new IllegalArgumentException();
 	}
 
 	public static boolean supportsJoinFailure(Object context) {
 		if (context instanceof Activity)  return true;
 		if (context instanceof Process)  return true;
 		return false;
 	}
 	
 	public static boolean isSetSuppressJoinFailure(Object context)  {
 		if (context instanceof Activity)  return ((Activity)context).isSetSuppressJoinFailure();
 		if (context instanceof Process)  return ((Process)context).isSetSuppressJoinFailure();
 		throw new IllegalArgumentException();
 	}
 	
 	public static Boolean getSuppressJoinFailure2(Object context)  {
 		if (context instanceof Activity)  return ((Activity)context).getSuppressJoinFailure();
 		if (context instanceof Process)  return ((Process)context).getSuppressJoinFailure();
 		throw new IllegalArgumentException();
 	}
 	
 	// Hack to hide the fact that null is not the same as unset.
 	public static Boolean getSuppressJoinFailure(Object context)  {
 		return isSetSuppressJoinFailure(context)? getSuppressJoinFailure2(context) : null;
 	}
 	
 	public static void setSuppressJoinFailure(Object context, Boolean value)  {
 		if (context instanceof Activity)  {
 			if (value == null) {
 				((Activity)context).unsetSuppressJoinFailure(); return;
 			} else {
 				((Activity)context).setSuppressJoinFailure(value); return;
 			}
 		}
 		if (context instanceof Process)  {
 			if (value == null) {
 				((Process)context).unsetSuppressJoinFailure(); return;
 			} else {
 				((Process)context).setSuppressJoinFailure(value); return;
 			}
 		}
 		throw new IllegalArgumentException();
 	}
 
 	public static boolean isSuppressJoinFailureAffected(Object context, Notification n) {
 		if (context instanceof Activity)  {
 			return (n.getFeatureID(Activity.class) == BPELPackage.ACTIVITY__SUPPRESS_JOIN_FAILURE);
 		}
 		if (context instanceof Process)  {
 			return (n.getFeatureID(Process.class) == BPELPackage.PROCESS__SUPPRESS_JOIN_FAILURE);
 		}
 		throw new IllegalArgumentException();
 	}
 	
 	public static boolean isMessageActivity(Object context, int direction) {
 		switch (direction)  {
 		case OUTGOING:
 			if (context instanceof Reply)  return true;
 			if (context instanceof Invoke)  return true;
 			break;
 		case INCOMING:
 			if (context instanceof Receive)  return true;
 			if (context instanceof OnMessage)  return true;
 			if (context instanceof OnEvent)  return true;
 			if (context instanceof Invoke) {
 				// check if our operation exists and is two way!
 				// If it doesn't exist, return true anyways.
 				Operation op = ModelHelper.getOperation(context);
 				if (op != null) 
 					if (op.getOutput() == null) return false;
 				return true;
 			}
 			break;
 		}
 		return false;
 	}			
 
 	public static EObject getCompensated(Object context){
 		if (context instanceof Compensate) return (((Compensate)context).getScope());
 		throw new IllegalArgumentException();
 	}
 	public static void setCompensated(Object context, EObject e){
 		if (context instanceof Compensate) {
 			((Compensate)context).setScope(e);
 			return;
 		}
 		throw new IllegalArgumentException();
 	}	
 	
 	public static Variable getVariable(Object context, int direction) {
 		switch (direction)  {
 		case OUTGOING:
 			if (context instanceof Reply)  return ((Reply)context).getVariable();
 			if (context instanceof Invoke)  return ((Invoke)context).getInputVariable();
 			if (context instanceof Throw)  return ((Throw)context).getFaultVariable();
 			break;
 		case INCOMING:
 			if (context instanceof Receive)  return ((Receive)context).getVariable();
 			if (context instanceof OnMessage)  return ((OnMessage)context).getVariable();
 			if (context instanceof OnEvent)  return ((OnEvent)context).getVariable();
 			if (context instanceof Invoke)  return ((Invoke)context).getOutputVariable();
 			if (context instanceof Catch)  return ((Catch)context).getFaultVariable(); 
 			break;
 		}
 		throw new IllegalArgumentException();
 	}	
 	public static void setVariable(Object context, Variable v, int direction) {
 		switch (direction) {
 		case OUTGOING:
 			if (context instanceof Reply) {
 				((Reply)context).setVariable(v); return;
 			}
 			if (context instanceof Invoke) {
 				((Invoke)context).setInputVariable(v); return;
 			}
 			if (context instanceof Throw) {
 				((Throw)context).setFaultVariable(v); return;
 			}
 			break; 
 		case INCOMING:
 			if (context instanceof Receive) {
 				((Receive)context).setVariable(v); return;
 			}
 			if (context instanceof OnMessage) {
 				((OnMessage)context).setVariable(v); return;
 			}
 			if (context instanceof OnEvent) {
 				((OnEvent)context).setVariable(v); return;
 			}
 			if (context instanceof Invoke) {
 				((Invoke)context).setOutputVariable(v); return;
 			}
 			if (context instanceof Catch) {
 				((Catch)context).setFaultVariable(v); return;
 			}
 			break; 
 		}
 		throw new IllegalArgumentException();
 	}
 	public static boolean isVariableAffected(Object context, Notification n, int direction) {
 		switch (direction) {
 		case OUTGOING:
 			if (context instanceof Reply) {
 				return (n.getFeatureID(Reply.class) == BPELPackage.REPLY__VARIABLE);
 			}
 			if (context instanceof Invoke) {
 				return (n.getFeatureID(Invoke.class) == BPELPackage.INVOKE__INPUT_VARIABLE);
 			}
 			if (context instanceof Throw) {
 				return (n.getFeatureID(Throw.class) == BPELPackage.THROW__FAULT_VARIABLE);
 			}
 			break; 
 		case INCOMING:
 			if (context instanceof Receive) {
 				return (n.getFeatureID(Receive.class) == BPELPackage.RECEIVE__VARIABLE);
 			}
 			if (context instanceof OnMessage) {
 				return (n.getFeatureID(OnMessage.class) == BPELPackage.ON_MESSAGE__VARIABLE);
 			}
 			if (context instanceof OnEvent) {
 				return (n.getFeatureID(OnEvent.class) == BPELPackage.ON_EVENT__VARIABLE);
 			}
 			if (context instanceof Invoke) {
 				return (n.getFeatureID(Invoke.class) == BPELPackage.INVOKE__OUTPUT_VARIABLE);
 			}
 			if (context instanceof Catch) {
 				return (n.getFeatureID(Catch.class) == BPELPackage.CATCH__FAULT_VARIABLE);
 			}
 			break; 
 		}
 		throw new IllegalArgumentException();
 	}
 
 	public static PartnerLink getPartnerLink(Object context) {
 		if (context instanceof Invoke)  return ((Invoke)context).getPartnerLink();
 		if (context instanceof Receive)  return ((Receive)context).getPartnerLink();
 		if (context instanceof OnMessage)  return ((OnMessage)context).getPartnerLink();
 		if (context instanceof OnEvent)  return ((OnEvent)context).getPartnerLink();
 		if (context instanceof Reply)  return ((Reply)context).getPartnerLink();
 		throw new IllegalArgumentException();
 	}
 
 	public static void setPartnerLink(Object context, PartnerLink partner) {
 		if (context instanceof Invoke) {
 			((Invoke)context).setPartnerLink(partner); return;
 		}
 		if (context instanceof Receive) {
 			((Receive)context).setPartnerLink(partner); return;
 		}
 		if (context instanceof OnMessage) {
 			((OnMessage)context).setPartnerLink(partner); return;
 		}
 		if (context instanceof OnEvent) {
 			((OnEvent)context).setPartnerLink(partner); return;
 		}
 		if (context instanceof Reply) {
 			((Reply)context).setPartnerLink(partner); return;
 		}
 		throw new IllegalArgumentException();
 	}
 
 	public static boolean isPartnerAffected(Object context, Notification n) {
 		if (context instanceof Invoke) {
 			return (n.getFeatureID(Invoke.class) == BPELPackage.INVOKE__PARTNER_LINK);
 		}
 		if (context instanceof Receive) {
 			return (n.getFeatureID(Receive.class) == BPELPackage.RECEIVE__PARTNER_LINK);
 		}
 		if (context instanceof OnMessage) {
 			return (n.getFeatureID(OnMessage.class) == BPELPackage.ON_MESSAGE__PARTNER_LINK);
 		}
 		if (context instanceof OnEvent) {
 			return (n.getFeatureID(OnEvent.class) == BPELPackage.ON_EVENT__PARTNER_LINK);
 		}
 		if (context instanceof Reply) {
 			return (n.getFeatureID(Reply.class) == BPELPackage.REPLY__PARTNER_LINK);
 		}
 		throw new IllegalArgumentException();
 	}
 
 	public static PortType getPortType(Object context) {
 		if (context instanceof Invoke)  return ((Invoke)context).getPortType();
 		if (context instanceof Receive)  return ((Receive)context).getPortType();
 		if (context instanceof OnMessage)  return ((OnMessage)context).getPortType();
 		if (context instanceof OnEvent)  return ((OnEvent)context).getPortType();
 		if (context instanceof Reply)  return ((Reply)context).getPortType();
 		if (context instanceof Role) return (PortType)((Role)context).getPortType();
 		throw new IllegalArgumentException();
 	}
 
 	public static void setPortType(Object context, PortType portType) {
 		if (context instanceof Invoke) {
 			((Invoke)context).setPortType(portType); return;
 		}
 		if (context instanceof Receive) {
 			((Receive)context).setPortType(portType); return;
 		}
 		if (context instanceof OnMessage) {
 			((OnMessage)context).setPortType(portType); return;
 		}
 		if (context instanceof OnEvent) {
 			((OnEvent)context).setPortType(portType); return;
 		}
 		if (context instanceof Reply) {
 			((Reply)context).setPortType(portType); return;
 		}
 		if (context instanceof Role) {
 			((Role)context).setPortType(portType); return;
 		}
 		throw new IllegalArgumentException();
 	}
 
 	public static boolean isPortTypeAffected(Object context, Notification n) {
 		if (context instanceof Invoke) {
 			return (n.getFeatureID(Invoke.class) == BPELPackage.INVOKE__PORT_TYPE);
 		}
 		if (context instanceof Receive) {
 			return (n.getFeatureID(Receive.class) == BPELPackage.RECEIVE__PORT_TYPE);
 		}
 		if (context instanceof OnMessage) {
 			return (n.getFeatureID(OnMessage.class) == BPELPackage.ON_MESSAGE__PORT_TYPE);
 		}
 		if (context instanceof OnEvent) {
 			return (n.getFeatureID(OnEvent.class) == BPELPackage.ON_EVENT__PORT_TYPE);
 		}
 		if (context instanceof Reply) {
 			return (n.getFeatureID(Reply.class) == BPELPackage.REPLY__PORT_TYPE);
 		}
 		if (context instanceof Role) {
 			return (n.getFeatureID(Role.class) == PartnerlinktypePackage.ROLE__PORT_TYPE);
 		}
 		throw new IllegalArgumentException();
 	}
 
 	public static Operation getOperation(Object context) {
 		if (context instanceof Invoke)  return ((Invoke)context).getOperation();
 		if (context instanceof Receive)  return ((Receive)context).getOperation();
 		if (context instanceof OnMessage)  return ((OnMessage)context).getOperation();
 		if (context instanceof OnEvent)  return ((OnEvent)context).getOperation();
 		if (context instanceof Reply)  return ((Reply)context).getOperation();
 		throw new IllegalArgumentException();
 	}
 
 	public static void setOperation(Object context, Operation operation) {
 		if (context instanceof Invoke) {
 			((Invoke)context).setOperation(operation); return;
 		}
 		if (context instanceof Receive) {
 			((Receive)context).setOperation(operation); return;
 		}
 		if (context instanceof OnMessage) {
 			((OnMessage)context).setOperation(operation); return;
 		}
 		if (context instanceof OnEvent) {
 			((OnEvent)context).setOperation(operation); return;
 		}
 		if (context instanceof Reply) {
 			((Reply)context).setOperation(operation); return;
 		}
 		throw new IllegalArgumentException();
 	}
 	
 	public static boolean isOperationAffected(Object context, Notification n) {
 		if (context instanceof Invoke) {
 			return (n.getFeatureID(Invoke.class) == BPELPackage.INVOKE__OPERATION);
 		}
 		if (context instanceof Receive) {
 			return (n.getFeatureID(Receive.class) == BPELPackage.RECEIVE__OPERATION);
 		}
 		if (context instanceof OnMessage) {
 			return (n.getFeatureID(OnMessage.class) == BPELPackage.ON_MESSAGE__OPERATION);
 		}
 		if (context instanceof OnEvent) {
 			return (n.getFeatureID(OnEvent.class) == BPELPackage.ON_EVENT__OPERATION);
 		}
 		if (context instanceof Reply) {
 			return (n.getFeatureID(Reply.class) == BPELPackage.REPLY__OPERATION);
 		}
 		throw new IllegalArgumentException();
 	}
 
 	public static Boolean getCreateInstance(Object context) {
 		if (context instanceof Receive)  return ((Receive)context).getCreateInstance();
 		if (context instanceof Pick)  return ((Pick)context).getCreateInstance();
 		throw new IllegalArgumentException();
 	}
 
 	public static void setCreateInstance(Object context, Boolean createInstance) {
 		if (context instanceof Receive) {
 			((Receive)context).setCreateInstance(createInstance); return;
 		}
 		if (context instanceof Pick) {
 			((Pick)context).setCreateInstance(createInstance); return;
 		}
 		throw new IllegalArgumentException();
 	}
 	
 	public static boolean isCreateInstanceAffected(Object context, Notification n) {
 		if (context instanceof Receive) {
 			return (n.getFeatureID(Receive.class) == BPELPackage.RECEIVE__CREATE_INSTANCE);
 		}
 		if (context instanceof Pick) {
 			return (n.getFeatureID(Pick.class) == BPELPackage.PICK__CREATE_INSTANCE);
 		}
 		throw new IllegalArgumentException();
 	}
 	
 	public static int getExpressionSubKind(Object context, int exprKind) {
 		switch (exprKind) {
 		case WAIT_EXPR:
 			if (context instanceof Wait) {
 				if (((Wait)context).getFor() != null) return ESUB_FOR;
 				if (((Wait)context).getUntil() != null) return ESUB_UNTIL;
 			}
 			if (context instanceof OnAlarm) {
 				if (((OnAlarm)context).getFor() != null) return ESUB_FOR;
 				if (((OnAlarm)context).getUntil() != null) return ESUB_UNTIL;
 			}
 			break;
 		}
 		return ESUB_DEFAULT;
 	}
 
 	public static Expression getExpression(Object context, int exprKind) {
 		switch (exprKind) {
 		case DEFAULT_EXPR:
 			if (context instanceof Case)  return ((Case)context).getCondition();
 			if (context instanceof While)  return ((While)context).getCondition();
 			if (context instanceof From)  return ((From)context).getExpression();
 			// TODO: is this hack unnecessary?
 			if (context instanceof Copy) {
 				From from = ((Copy)context).getFrom();
 				return (from == null)? null : from.getExpression();
 			}
 			break;
 		case JOIN_EXPR:
 			if (context instanceof Activity) {
 				Targets targets = ((Activity)context).getTargets();
 				if (targets == null) return null;
 				return targets.getJoinCondition();
 			}
 			break;
 		case TRANSITION_EXPR:
 			if (context instanceof Link) {
 				return ((Source)((Link)context).getSources().get(0)).getTransitionCondition();
 			}
 			break;
 		case REPEATEVERY_EXPR:
 	        if (context instanceof OnAlarm) {
 	        	return ((OnAlarm)context).getRepeatEvery();
 	        }
 			break;
 		case WAIT_EXPR:
 			if (context instanceof Wait) {
 				if (((Wait)context).getFor() != null) return ((Wait)context).getFor();
 				if (((Wait)context).getUntil() != null) return ((Wait)context).getUntil();
 			}
 			if (context instanceof OnAlarm) {
 				if (((OnAlarm)context).getFor() != null) return ((OnAlarm)context).getFor();
 				if (((OnAlarm)context).getUntil() != null) return ((OnAlarm)context).getUntil();
 			}
 			break;
 		}
 		throw new IllegalArgumentException();
 	}
 
 	protected static Condition makeCondition(Expression expr) {
 		if ((expr == null) || (expr instanceof Condition)) return (Condition)expr;
 		Condition cond = BPELFactory.eINSTANCE.createCondition();
 		cond.setExpressionLanguage(expr.getExpressionLanguage());
 		cond.setBody(expr.getBody());
 		return cond;
 	}
 	
 	public static void setExpression(Object context, int exprKind, int exprSubKind, Expression expr) {
 		switch (exprKind) {
 		case DEFAULT_EXPR:
 			if (context instanceof Case) {
 				((Case)context).setCondition(makeCondition(expr)); return;
 			}
 			if (context instanceof While) {
 				((While)context).setCondition(makeCondition(expr)); return;
 			}
 			if (context instanceof From) {
 				((From)context).setExpression(expr); return;
 			}
 			// TODO: is this hack unnecessary?
 			if (context instanceof Copy) {
 				From from = ((Copy)context).getFrom();
 				if (from == null) throw new IllegalStateException();
 				from.setExpression(expr);
 			}
 			break;
 		case JOIN_EXPR:
 			if (context instanceof Activity) {
 				Targets targets = ((Activity)context).getTargets();
 				if (targets == null) throw new IllegalArgumentException();
 				targets.setJoinCondition(makeCondition(expr)); return;
 			}
 			break;
 		case TRANSITION_EXPR:
 			if (context instanceof Link) {
 				Source source = ((Source)(((Link)context).getSources().get(0)));
 				source.setTransitionCondition(makeCondition(expr));
 				return;
 			}
 			break;
 		case REPEATEVERY_EXPR:
 			if (context instanceof OnAlarm) {
 				((OnAlarm)context).setRepeatEvery(expr);
 				return;
 			}
 			break;
 		case WAIT_EXPR:
 			if (context instanceof Wait) {
 				((Wait)context).setFor(null);
 				((Wait)context).setUntil(null);
 			}
 			if (context instanceof OnAlarm) {
 				((OnAlarm)context).setFor(null);
 				((OnAlarm)context).setUntil(null);
 			}
 			if (expr == null) return;
 			if (exprSubKind == ESUB_DEFAULT) throw new IllegalArgumentException();
 			
 			if (context instanceof Wait) {
 				if (exprSubKind == ESUB_FOR) { ((Wait)context).setFor(expr); return; }
 				if (exprSubKind == ESUB_UNTIL) { ((Wait)context).setUntil(expr); return; }
 			}
 			if (context instanceof OnAlarm) {
 				if (exprSubKind == ESUB_FOR) { ((OnAlarm)context).setFor(expr); return; }
 				if (exprSubKind == ESUB_UNTIL) { ((OnAlarm)context).setUntil(expr); return; }
 			}
 			break;
 		}
 	}
 
 	/*
 	 * These 2 methods translate the supported set of string type/context constants used
 	 * by IExpressionEditors into the integer constants expected by the getExpression()
 	 * and setExpression() methods.  This method will return DEFAULT_EXPR for any
 	 * unrecognized contexts, but note that setExpression() and getExpression() do not
 	 * currently support any unrecognized contexts (so details sections must implement
 	 * their own loading/storing support as appropriate).  
 	 */
 	public static int expressionTypeAndContext2Kind(String exprType, String exprContext) {
 		if (IEditorConstants.EC_JOIN.equals(exprContext)) return JOIN_EXPR;
 		if (IEditorConstants.EC_TRANSITION.equals(exprContext)) return TRANSITION_EXPR;
 		if (IEditorConstants.EC_ONALARM_REPEATEVERY.equals(exprContext)) return REPEATEVERY_EXPR;
 		if (IEditorConstants.EC_WAIT.equals(exprContext) ||	IEditorConstants.EC_ONALARM.equals(exprContext)) {
 			return WAIT_EXPR;
 		}
 		// everything else
 		return DEFAULT_EXPR;
 	}
 	// TODO: do these 2 methods make any sense??  How crufty.
 	public static int expressionTypeAndContext2SubKind(String exprType, String exprContext) {
 		if (IEditorConstants.EC_WAIT.equals(exprContext) ||	IEditorConstants.EC_ONALARM.equals(exprContext)) {
 			if (IEditorConstants.ET_DURATION.equals(exprType)) return ESUB_FOR;
 			if (IEditorConstants.ET_DATETIME.equals(exprType)) return ESUB_UNTIL;
 		}
 		// everything else
 		return ESUB_DEFAULT;
 	}
 	
 	public static boolean isExpressionAffected(Object context, Notification n, int exprType) {
 		// TODO!
 		return true;
 	}
 	
 	public static String getFaultName(Object context) {
 		QName qname = getFaultQName(context);
 		return (qname == null)? null : qname.getLocalPart();
 	}
 	
 	public static QName getFaultQName(Object context) {
 		if (context instanceof Throw) {
 			return ((Throw)context).getFaultName();
 		}
 		if (context instanceof Catch) {
 			return ((Catch)context).getFaultName();
 		}
 		if (context instanceof Reply) {
 			return ((Reply)context).getFaultName();
 		}
 		throw new IllegalArgumentException();
 	}
 	
 	public static void setFaultName(Object context, String faultName) {
 		if (context instanceof Throw) {
 			String oldNS = getFaultNamespace(context);
 			QName newQName = null;
 			if (oldNS != null || faultName != null)  newQName = new QName(oldNS, faultName);
 			((Throw)context).setFaultName(newQName); return;
 		}
 		if (context instanceof Catch) {
 			String oldNS = getFaultNamespace(context);
 			QName newQName = null;
 			if (oldNS != null || faultName != null)  newQName = new QName(oldNS, faultName);
 			((Catch)context).setFaultName(newQName); return;
 		}
 		if (context instanceof Reply) {
 			String oldNS = getFaultNamespace(context);
 			QName newQName = null;
 			if (oldNS != null || faultName != null)  newQName = new QName(oldNS, faultName);
 			((Reply)context).setFaultName(newQName); return;
 		}
 		throw new IllegalArgumentException();
 	}
 	
 	public static boolean isFaultNameAffected(Object context, Notification n) {
 		if (context instanceof Throw) {
 			return (n.getFeatureID(Throw.class) == BPELPackage.THROW__FAULT_NAME);
 		}
 		if (context instanceof Catch) {
 			return (n.getFeatureID(Catch.class) == BPELPackage.CATCH__FAULT_NAME);
 		}
 		if (context instanceof Reply) {
 			return (n.getFeatureID(Reply.class) == BPELPackage.REPLY__FAULT_NAME);
 		}
 		throw new IllegalArgumentException();
 	}
 	
 	public static String getFaultNamespace(Object context) {
 		if (context instanceof Throw) {
 			QName qname = ((Throw)context).getFaultName();
 			return (qname == null)? null : qname.getNamespaceURI();
 		}
 		if (context instanceof Catch) {
 			QName qname = ((Catch)context).getFaultName();
 			return (qname == null)? null : qname.getNamespaceURI();
 		}
 		if (context instanceof Reply) {
 			QName qname = ((Reply)context).getFaultName();
 			return (qname == null)? null : qname.getNamespaceURI();
 		}
 		throw new IllegalArgumentException();
 	}
 	
 	public static void setFaultNamespace(Object context, String faultNS) {
 		if (context instanceof Throw) {
 			String oldName = getFaultName(context);
 			QName newQName = null;
 			if (oldName != null || faultNS != null)  newQName = new QName(faultNS, oldName);
 			((Throw)context).setFaultName(newQName); return;
 		}
 		if (context instanceof Catch) {
 			String oldName = getFaultName(context);
 			QName newQName = null;
 			if (oldName != null || faultNS != null)  newQName = new QName(faultNS, oldName);
 			((Catch)context).setFaultName(newQName); return;
 		}
 		if (context instanceof Reply) {
 			String oldName = getFaultName(context);
 			QName newQName = null;
 			if (oldName != null || faultNS != null)  newQName = new QName(faultNS, oldName);
 			((Reply)context).setFaultName(newQName); return;
 		}
 		throw new IllegalArgumentException();
 	}
 	
 	public static boolean isFaultNamespaceAffected(Object context, Notification n) {
 		return isFaultNameAffected(context, n);
 	}
 
 	public static boolean isSingleActivityContainer(Object context) {
 		if (context instanceof Case)  return true;
 		if (context instanceof Otherwise)  return true;
 		if (context instanceof Catch)  return true;
 		if (context instanceof CatchAll)  return true;
 		if (context instanceof OnAlarm)  return true;
 		if (context instanceof OnMessage)  return true;
 		if (context instanceof OnEvent)  return true;
 		if (context instanceof Process)  return true;
 		if (context instanceof While)  return true;
 		return false;
 	}
 
 	public static Activity getActivity(Object context) {
 		if (context instanceof Case)  return ((Case)context).getActivity();
 		if (context instanceof Otherwise)  return ((Otherwise)context).getActivity();
 		if (context instanceof Catch)  return ((Catch)context).getActivity();
 		if (context instanceof CatchAll)  return ((CatchAll)context).getActivity();
 		if (context instanceof OnAlarm)  return ((OnAlarm)context).getActivity();
 		if (context instanceof OnMessage)  return ((OnMessage)context).getActivity();
 		if (context instanceof OnEvent)  return ((OnEvent)context).getActivity();
 		if (context instanceof Process)  return ((Process)context).getActivity();
 		if (context instanceof While)  return ((While)context).getActivity();
 		if (context instanceof Scope)  return ((Scope)context).getActivity();
 		if (context instanceof FaultHandler) return getCatchAll((FaultHandler)context);
 		if (context instanceof CompensationHandler)  return ((CompensationHandler)context).getActivity();
 		if (context instanceof Switch) return getOtherwise((Switch)context);
 		throw new IllegalArgumentException();
 	}
 
 	public static void setActivity(Object context, Activity activity) {
 		if (context instanceof Case) {
 			((Case)context).setActivity(activity); return;
 		}
 		if (context instanceof Otherwise) {
 			((Otherwise)context).setActivity(activity); return;
 		}
 		if (context instanceof Catch) {
 			((Catch)context).setActivity(activity); return;
 		}
 		if (context instanceof CatchAll) {
 			((CatchAll)context).setActivity(activity); return;
 		}
 		if (context instanceof OnAlarm) {
 			((OnAlarm)context).setActivity(activity); return;
 		}
 		if (context instanceof OnMessage) {
 			((OnMessage)context).setActivity(activity); return;
 		}
 		if (context instanceof OnEvent) {
 			((OnEvent)context).setActivity(activity); return;
 		}
 		if (context instanceof Process) {
 			((Process)context).setActivity(activity); return;
 		}
 		if (context instanceof While) {
 			((While)context).setActivity(activity); return;
 		}
 		if (context instanceof Scope) {
 			((Scope)context).setActivity(activity); return;
 		}
 		if (context instanceof FaultHandler) {
 			setCatchAll((FaultHandler)context, activity); return;
 		}
 		if (context instanceof CompensationHandler) {
 			((CompensationHandler)context).setActivity(activity); return;
 		}
 		if (context instanceof Switch) {
 			setOtherwise((Switch)context, activity); return;
 		}
 		throw new IllegalArgumentException();
 	}
 	
 	public static boolean isActivityAffected(Object context, Notification n) {
 		if (context instanceof Case) {
 			return (n.getFeatureID(Case.class) == BPELPackage.CASE__ACTIVITY);
 		}
 		if (context instanceof Catch) {
 			return (n.getFeatureID(Catch.class) == BPELPackage.CATCH__ACTIVITY);
 		}
 		if (context instanceof OnAlarm) {
 			return (n.getFeatureID(OnAlarm.class) == BPELPackage.ON_ALARM__ACTIVITY);
 		}
 		if (context instanceof OnMessage) {
 			return (n.getFeatureID(OnMessage.class) == BPELPackage.ON_MESSAGE__ACTIVITY);
 		}
 		if (context instanceof OnEvent) {
 			return (n.getFeatureID(OnEvent.class) == BPELPackage.ON_EVENT__ACTIVITY);
 		}
 		if (context instanceof Process) {
 			return (n.getFeatureID(Process.class) == BPELPackage.PROCESS__ACTIVITY);
 		}
 		if (context instanceof While) {
 			return (n.getFeatureID(While.class) == BPELPackage.WHILE__ACTIVITY);
 		}
 		if (context instanceof Scope) {
 			return (n.getFeatureID(Scope.class) == BPELPackage.SCOPE__ACTIVITY);
 		}
 		if (context instanceof CompensationHandler) {
 			return (n.getFeatureID(CompensationHandler.class) == BPELPackage.COMPENSATION_HANDLER__ACTIVITY);
 		}
 		if (context instanceof FaultHandler) {
 			return isCatchAllAffected((FaultHandler)context, n);
 		}
 		if (context instanceof Switch) {
 			return isOtherwiseAffected((Switch)context, n);
 		}
 		throw new IllegalArgumentException();
 	}
 	
 	public static Activity getCatchAll(FaultHandler faultHandler) {
 		CatchAll catchAll = faultHandler.getCatchAll();
 		return (catchAll == null)? null : catchAll.getActivity();
 	}
 	public static void setCatchAll(FaultHandler faultHandler, Activity activity) {
 		if (activity == null)  {
 			faultHandler.setCatchAll(null);
 		} else if (faultHandler.getCatchAll() == null) {
 			CatchAll catchAll = BPELFactory.eINSTANCE.createCatchAll();
 			faultHandler.setCatchAll(catchAll);
 			catchAll.setActivity(activity);
 		} else {
 			faultHandler.getCatchAll().setActivity(activity);
 		}
 	}
 	public static boolean isCatchAllAffected(FaultHandler faultHandler, Notification n) {
 		if ((n.getNewValue() instanceof FaultHandler) || (n.getOldValue() instanceof FaultHandler)) {
 			return (n.getFeatureID(FaultHandler.class) == BPELPackage.FAULT_HANDLER__CATCH_ALL);
 		}
 		return (n.getFeatureID(CatchAll.class) == BPELPackage.CATCH_ALL__ACTIVITY);
 	}
 	
 	public static Activity getOtherwise(Switch modelSwitch) {
 		Otherwise otherwise = modelSwitch.getOtherwise();
 		return (otherwise == null)? null : otherwise.getActivity();
 	}
 	public static void setOtherwise(Switch modelSwitch, Activity activity) {
 		if (activity == null)  {
 			modelSwitch.setOtherwise(null);
 		} else if (modelSwitch.getOtherwise() == null) {
 			Otherwise otherwise = BPELFactory.eINSTANCE.createOtherwise();
 			modelSwitch.setOtherwise(otherwise);
 			otherwise.setActivity(activity);
 		} else {
 			modelSwitch.getOtherwise().setActivity(activity);
 		}
 	}
 	public static boolean isOtherwiseAffected(Switch modelSwitch, Notification n) {
 		if ((n.getNewValue() instanceof Switch) || (n.getOldValue() instanceof Switch)) {
 			return (n.getFeatureID(Switch.class) == BPELPackage.SWITCH__OTHERWISE);
 		}
 		return (n.getFeatureID(Otherwise.class) == BPELPackage.OTHERWISE__ACTIVITY);
 	}
 	
 	public static boolean isRoleAffected(Object context, Notification n, int who)  {
 		if (context instanceof PartnerLink)  {
 			switch (who)  {
 			case MY_ROLE:
 				return (n.getFeatureID(PartnerLink.class) == BPELPackage.PARTNER_LINK__MY_ROLE);
 			case PARTNER_ROLE:
 				return (n.getFeatureID(PartnerLink.class) == BPELPackage.PARTNER_LINK__PARTNER_ROLE);
 			}
 		}
 		throw new IllegalArgumentException();
 	}
 
 	public static Message getMessageType(Object context)  {
 		if (context instanceof Variable) {
 			return ((Variable)context).getMessageType();
 		}
 		if (context instanceof PropertyAlias) {
 			return (Message)((PropertyAlias)context).getMessageType();
 		}
 		throw new IllegalArgumentException();
 	}
 
 	public static void setMessageType(Object context, Message messageType)  {
 		if (context instanceof Variable) {
 			((Variable)context).setMessageType(messageType); return;
 		}
 		if (context instanceof PropertyAlias) {
 			((PropertyAlias)context).setMessageType(messageType); return;
 		}
 		throw new IllegalArgumentException();
 	}
 
 	public static Correlations getCorrelations(Object context)  {
 		if (context instanceof Receive) return ((Receive)context).getCorrelations();
 		if (context instanceof Invoke) return ((Invoke)context).getCorrelations();
 		if (context instanceof Reply) return ((Reply)context).getCorrelations();
 		if (context instanceof OnMessage) return ((OnMessage)context).getCorrelations();
 		if (context instanceof OnEvent) return ((OnEvent)context).getCorrelations();
 		throw new IllegalArgumentException();
 	}
 	
 	public static Variables getVariables(Object context)  {
 		if (context instanceof Process) return ((Process)context).getVariables();
 		if (context instanceof Scope) return ((Scope)context).getVariables();
 		throw new IllegalArgumentException();
 	}
 	
 	public static void setCorrelations(Object context, Correlations correlations) {
 		if (context instanceof Receive) {
 			((Receive)context).setCorrelations(correlations); return;
 		}
 		if (context instanceof Invoke) {
 			((Invoke)context).setCorrelations(correlations); return;
 		}
 		if (context instanceof Reply) {
 			((Reply)context).setCorrelations(correlations); return;
 		}
 		if (context instanceof OnMessage) {
 			((OnMessage)context).setCorrelations(correlations); return;
 		}
 		if (context instanceof OnEvent) {
 			((OnEvent)context).setCorrelations(correlations); return;
 		}
 		throw new IllegalArgumentException();
 	}
 
 	public static PortType getRolePortType(Role role) {
 		return (PortType)role.getPortType();
 	}
 
 	public static PortType getPartnerPortType(PartnerLink partner, int direction)  {
 		if (partner != null) {
 			if (direction == ModelHelper.INCOMING) {
 				return getRolePortType(partner.getMyRole());
 			} else if (direction == ModelHelper.OUTGOING) {
 				return getRolePortType(partner.getPartnerRole());
 			}
 		}
 		return null;
 	}
 
 	public static Fault getWSDLFault(Object context) {
 		Operation operation = getOperation(context);
 		if (operation == null) return null;
 		String operationNS = operation.getEnclosingDefinition() != null?
 			operation.getEnclosingDefinition().getTargetNamespace() : null;
 		if (operationNS == null) return null;
 		if (!operationNS.equals(ModelHelper.getFaultNamespace(context))) return null;
 		String faultName = ModelHelper.getFaultName(context);
 		if (faultName == null || "".equals(faultName)) return null;  //$NON-NLS-1$
 		for (Iterator it = operation.getEFaults().iterator(); it.hasNext(); ) {
 			Fault fault = (Fault)it.next();
 			if (faultName.equals(fault.getName())) return fault;
 		}
 		return null;
 	}
 
 	public static void setWSDLFault(Object context, Fault fault) {
 		QName newQName = fault==null? null : new QName(
 			fault.getEnclosingDefinition().getTargetNamespace(), fault.getName());
 
 		if (context instanceof Throw) {
 			((Throw)context).setFaultName(newQName); return;
 		}
 		if (context instanceof Catch) {
 			((Catch)context).setFaultName(newQName); return;
 		}
 		if (context instanceof Reply) {
 			((Reply)context).setFaultName(newQName); return;
 		}
 		throw new IllegalArgumentException();
 	}
 
 	/**
 	 * Helper to get model extensions for a model object (e.g. BPEL Plus extensions).
 	 */
 	public static ExtensibilityElement getExtensibilityElement(Object input, Class clazz) {
 		if (!(input instanceof ExtensibleElement))  throw new IllegalArgumentException();
 		ExtensibleElement element = (ExtensibleElement)input;
 		for (Iterator it = element.getExtensibilityElements().iterator(); it.hasNext(); ) {
 			Object extension = it.next();
 			if (clazz.isInstance(extension))  return (ExtensibilityElement)extension;
 		}
 		return null;
 	}
 
 	/**
 	 * Another helper to get model extensions for a model object (e.g. BPEL Plus extensions).
 	 * This one accepts an EClass instead of a Class.
 	 */
 	public static ExtensibilityElement getExtensibilityElement(Object input, EClass clazz) {
 		if (!(input instanceof ExtensibleElement))  throw new IllegalArgumentException();
 		ExtensibleElement element = (ExtensibleElement)input;
 		for (Iterator it = element.getExtensibilityElements().iterator(); it.hasNext(); ) {
 			ExtensibilityElement extension = (ExtensibilityElement)it.next();
 			if (clazz.isSuperTypeOf(extension.eClass()))  return extension;
 		}
 		return null;
 	}
 
 	public static boolean isExtensionListAffected(Notification n) {
 		return (n.getFeatureID(ExtensibleElement.class) ==
 			WSDLPackage.EXTENSIBLE_ELEMENT__EEXTENSIBILITY_ELEMENTS);
 	}
 
 	/**
 	 * Helper to create an extension (if necessary) and add it to the ExtensionMap.
 	 * The ModelAutoUndoRecorder will do this automatically for any object that is
 	 * inserted into the ResourceSet, so you should only need to call this if you
 	 * have created new model objects and not yet added them to the ResourceSet and
 	 * you want to do things to the extension.
 	 */
 	public static void createExtensionIfNecessary(ExtensionMap extensionMap, EObject input) {
 		if (extensionMap == null || input == null || input.eIsProxy()) return;
 		try {
 			if (extensionMap.get(input) != null) return;
 		} catch (NullPointerException e) {
 			// this is a bug in the ExtensionMapImpl.
 			// Just assume there is no extension (fall through)
 		}
 		
 		// If it supports IExtensionFactory, create an extension and add it to the map.
 		IExtensionFactory extensionFactory = (IExtensionFactory)BPELUtil.adapt(
 			input, IExtensionFactory.class);
 		if (extensionFactory != null) {
 			if (Policy.DEBUG) System.out.println("creating extension for: "+input); //$NON-NLS-1$
 			Object extension = extensionFactory.createExtension(input);
 			if (extension != null) extensionMap.put(input, extension);
 		}
 	}
 	
 	/**
 	 * Helper to return the UIExtensionModel object (i.e. from our ExtensionMap) for a
 	 * given object, without requiring the ExtensionMap as a parameter.  If the object
 	 * does not have a UIExtension, null is returned.
 	 */
 	public static EObject getExtension(EObject input) {
 		ExtendedObjectUserAdapter adapter = (ExtendedObjectUserAdapter)
 			ExtensionmodelFactory.eINSTANCE.getExtensionAdapter(input,
 			IBPELUIConstants.MODEL_EXTENSIONS_NAMESPACE);
 		return (adapter==null)? null : (EObject)adapter.get(input);
 	}
 	
 	public static FaultHandler getContainingFaultHandler(Object object) {
 		if (object instanceof Process) return null;
 		if (object instanceof FaultHandler) return (FaultHandler)object;
 		if (object instanceof EObject) {
 			return getContainingFaultHandler(((EObject)object).eContainer());
 		}
 		// Don't know what it is, probably not in a fault handler.
 		return null;
 		
 	}
 	
 	public static CompensationHandler getContainingCompensationHandler(Object object) {
 		if (object instanceof Process) return null;
 		if (object instanceof CompensationHandler) return (CompensationHandler)object;
 		if (object instanceof EObject) {
 			return getContainingCompensationHandler(((EObject)object).eContainer());
 		}
 		// Don't know what it is, probably not in a compensation handler.
 		return null;
 	
 	}
 	
 	public static EventHandler getContainingEventHandler(Object object) {
 		if (object instanceof Process) return null;
 		if (object instanceof EventHandler) return (EventHandler)object;
 		if (object instanceof EObject) {
 			return getContainingEventHandler(((EObject)object).eContainer());
 		}
 		// Don't know what it is, probably not in an event handler.
 		return null;
 	
 	}
 	
 	/** 
 	 * Returns the root process for any model object 
 	 **/	
 	public static Process getProcess(Object object) {
 		if (object instanceof EObject) {
 			// check if *this* is already the process object
 			if (object instanceof Process)
 				return (Process)object;
 			EObject cont = ((EObject)object).eContainer();
 			while (cont != null) {
 				if (cont.eClass() == BPELPackage.eINSTANCE.getProcess())
 					return (Process)cont;
 				cont = cont.eContainer();
 			}
 		}
 		return null;
 	}
 	
 	/** 
 	 * Checks to see if an object is contained by a specified parent 
 	 */
 	public static boolean isChildContainedBy(Object modelParent, Object object) {
 		EObject cont = ((EObject)object).eContainer();
 		while (cont != null) {
 			if (modelParent == cont)
 				return true;
 			cont = cont.eContainer();
 		}
 		return false;
 	}
 
 	/**
 	 * Adds the given modelObject and all of its contained objects to the given collection
 	 * (where containment is determined by the IContainer heirarchy).
 	 */
 	public static void addSubtreeToCollection(Object modelObject, Collection collection) {
 		collection.add(modelObject);
 		IContainer container = (IContainer)BPELUtil.adapt(modelObject, IContainer.class);
 		if (container != null) {
 			for (Iterator it = container.getChildren(modelObject).iterator(); it.hasNext(); ) {
 				addSubtreeToCollection(it.next(), collection);
 			}
 		}
 	}
 
 	public static void setLocation(Activity activity, Point pos) {
 		ActivityExtension extension = (ActivityExtension)getExtension(activity);
 		extension.setX(pos.x);
 		extension.setY(pos.y);
 	}
 	
 	public static Point getLocation(Activity activity) {
 		ActivityExtension extension = (ActivityExtension)getExtension(activity);
 		// HACK!  Sometimes we are refreshing the appearance in a batched adapter
 		// after the extension has been removed (when Undoing its creation for example).
 		if (extension == null) return new Point(0,0);
 		
 		return new Point(extension.getX(), extension.getY());
 	}
 	
 	public static void setSize(Activity activity, Dimension size) {
 		ActivityExtension extension = (ActivityExtension)getExtension(activity);
 		extension.setWidth(size.width);
 		extension.setHeight(size.height);
 	}
 	
 	public static Dimension getSize(Activity activity) {
 		ActivityExtension extension = (ActivityExtension)getExtension(activity);
 		// HACK!  Sometimes we are refreshing the appearance in a batched adapter
 		// after the extension has been removed (when Undoing its creation for example).
 		if (extension == null) return new Dimension(1,1);
 		return new Dimension(extension.getWidth(), extension.getHeight());
 	}
 	
 	/**
 	 * Given a model object (or Resource or ResourceSet), return the BPELEditor
 	 * that this model belongs to.
 	 * 
 	 * @throws IllegalArgumentException if modelObject is not EObject, Resource or ResourceSet.
 	 */
 	public static BPELEditor getBPELEditor(Object modelObject) {
 		if (modelObject instanceof StartNode) {
 			modelObject = ((StartNode)modelObject).getProcess();
 		}
 		if (modelObject instanceof EObject) {
 			Resource r = ((EObject)modelObject).eResource();
 			if (r != null) {
 				return BPELEditor.getBPELEditor(r.getResourceSet());
 			}
 		} else if (modelObject instanceof Resource) {
 			return BPELEditor.getBPELEditor(((Resource)modelObject).getResourceSet());
 		} else if (modelObject instanceof ResourceSet) {
 			return BPELEditor.getBPELEditor((ResourceSet)modelObject);
 		}
 		throw new IllegalArgumentException();
 	}
 
 	public static TreeIterator getAllContents(Object modelObject) {
 		if (modelObject instanceof EObject) {
 			return ((EObject)modelObject).eAllContents();
 		} else if (modelObject instanceof Resource) {
 			return ((Resource)modelObject).getAllContents();
 		} else if (modelObject instanceof ResourceSet) {
 			return ((ResourceSet)modelObject).getAllContents();
 		}
 		throw new IllegalArgumentException();
 	}
 	
 	/**
 	 * Searches the specified modelObject for a contained element with the
 	 * specified name and of the specified type. Candidates are matched by
 	 * using the INamedElement adapter.
 	 * 
 	 * @param modelObject
 	 *            the object to search, must not be <code>null</code>
 	 * @param name
 	 *            the name to look for, must not be <code>null</code>
 	 * @param type
 	 *            the type to look for, may be <code>null</code> which
 	 *            matches any type
 	 * @return the matching object or <code>null</code> if not found.
 	 */
 	static public Object findElementByName(EObject modelObject, String name, Class type)
 	{
 		Object result = null;
 		if (modelObject != null && name != null)
 		{
 			for (Iterator i = modelObject.eAllContents(); result == null && i.hasNext();)
 			{
 				EObject model = (EObject) i.next();
 				// Check type.
 				if (type == null || type.isInstance(model))
 				{
 					// Check name.
 					INamedElement namedElement = (INamedElement) BPELUtil.adapt(model, INamedElement.class);
 					if (namedElement != null && name.equals(namedElement.getName(model)))
 					{
 						result = model;
 					}
 				}
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Gets the variable type from the model object. The direction can be INCOMING or OUTGOING.
 	 */
 	public static EObject getVariableType(EObject container, int direction) {
 		if (container instanceof Invoke) {
 			Operation operation = ModelHelper.getOperation(container);
 			if (operation == null) return null;
 			if (direction == INCOMING) {
 				Output output = operation.getOutput();
 				return (output != null) ? (EObject)output.getMessage() : null;
 			} else if (direction == OUTGOING) {
 				Input input = operation.getInput();
 				return (input != null) ? (EObject)input.getMessage() : null;
 			} else {
 				return null;
 			}
 		}
 		if (container instanceof PartnerActivity
 				|| container instanceof OnMessage
 				|| container instanceof OnEvent) {
 			Operation operation = ModelHelper.getOperation(container);
 			if (operation == null) return null;
 			if (direction == INCOMING) {
 				Input input = operation.getInput();
 				return (input != null) ? (EObject)input.getMessage() : null;
 			} else if (direction == OUTGOING) {
 				Output output = operation.getOutput();
 				return (output != null) ? (EObject)output.getMessage() : null;
 			} else {
 				return null;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Given a Part or XSDElementDeclaration we figure out what would be a
 	 * good type for a bundled/unbundled variable.
 	 */
 	public static Object getVariableTypeFrom(Object target) {
 		if (target instanceof Part) {
 			// A part can be typed as an element or a type
 			Part part = (Part)target;
 			if (part.getElementDeclaration() != null) {
 				return part.getElementDeclaration();
 			} else if (part.getTypeDefinition() != null) {
 				return part.getTypeDefinition();
 			}
 		} else if (target instanceof XSDElementDeclaration) {
 			XSDElementDeclaration element = (XSDElementDeclaration) BPELUtil.resolveXSDObject(target);
 			XSDTypeDefinition type = element.getTypeDefinition();
 			if (isAnonymousType(type)) {
 				return element;
 			}
 			return element.getTypeDefinition();
 		}
 		return null;
 	}
 	
 	public static boolean isAnonymousType(XSDTypeDefinition type) {
 		return type.getName() == null || "".equals(type.getName()); //$NON-NLS-1$
 	}
 
 	/**
 	 * Given a model object returns whether the process is supposed to be spec-compliant.
 	 */
 	public static boolean isSpecCompliant(EObject model) {
 		return ((ProcessExtension)getExtension(getProcess(model))).isSpecCompliant();
 	}
 
 	public static boolean isReferencePartnerLink(PartnerLink partner) {
 		return partner.getPartnerRole() != null;
 	}
 	
 	public static boolean isInterfacePartnerLink(PartnerLink partner) {
 		return partner.getMyRole() != null;
 	}
 	
 	public static CompoundCommand getCreatePartnerLinkTypeCommand(Process process, PartnerLink partner, PartnerLinkType plt, Definition artifactsDefinition, int whichRole) {
 		CompoundCommand compound = new CompoundCommand();
 		compound.add(new SetUniqueNameCommand(process, partner));
 		compound.add(new CreatePartnerLinkTypeCommand(artifactsDefinition, plt, partner));
 		compound.add(new SetPartnerLinkTypeCommand(partner, plt));
 		compound.add(createSetRoleCommand(partner, plt, whichRole));
 		return compound;
 	}
 
 	public static Command createSetRoleCommand(PartnerLink partnerLink, PartnerLinkType plt, int whichRole) {
 		CompoundCommand cmd = new CompoundCommand();
 		Role role = PartnerlinktypeFactory.eINSTANCE.createRole();
 		role.setName((whichRole == ModelHelper.MY_ROLE) ? IBPELUIConstants.ROLE_NAME_MYROLE : IBPELUIConstants.ROLE_NAME_PARTNERROLE);
 		cmd.add(new AddRoleCommand(plt, role));
 		cmd.add(new SetRoleCommand(partnerLink, role, whichRole));
 		return cmd;
 	}
 	
 	public static Set getAvailableProperties(Process process) {
 		Set properties = new HashSet();
 		 
 		// search in current process
 		List sets = process.getCorrelationSets().getChildren();
 		for (Iterator iter = sets.iterator(); iter.hasNext();) {
 			CorrelationSet set = (CorrelationSet) iter.next();
 			for (Iterator iterator = set.getProperties().iterator(); iterator.hasNext();) {
 				properties.add(iterator.next());
 			}
 		}
 		
 		// search in artifacts wsdl
 		BPELEditor bpelEditor = ModelHelper.getBPELEditor(process);
 		for (Iterator iter = bpelEditor.getArtifactsDefinition().eAllContents(); iter.hasNext();) {
 			Object element = iter.next();
 			if (element instanceof Property) {
 				properties.add(element);
 			}
 		}
 		
 		return properties;
 	}
 }
