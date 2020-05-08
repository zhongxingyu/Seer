 /*
  * Copyright (c) 2006 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Michael Golubev (Borland) - initial API and implementation
  */
 package org.eclipse.uml2.diagram.clazz.parser.stereotype;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.gmf.runtime.common.core.command.ICommand;
 import org.eclipse.gmf.runtime.common.core.command.UnexecutableCommand;
 import org.eclipse.gmf.runtime.common.ui.services.parser.IParserEditStatus;
 import org.eclipse.gmf.runtime.common.ui.services.parser.ParserEditStatus;
 import org.eclipse.gmf.runtime.emf.ui.services.parser.ISemanticParser;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
 import org.eclipse.uml2.diagram.parser.assist.FixedSetCompletionProcessor;
 import org.eclipse.uml2.uml.Element;
 import org.eclipse.uml2.uml.Extension;
 import org.eclipse.uml2.uml.NamedElement;
 import org.eclipse.uml2.uml.Stereotype;
 
 public class AppliedStereotypeParser implements ISemanticParser {
 
 	
 	public boolean areSemanticElementsAffected(EObject listener, Object notification) {
 		if (notification instanceof Notification){
 			Object feature = ((Notification)notification).getFeature();
 			if (feature instanceof EStructuralFeature){
 				EStructuralFeature featureImpl = (EStructuralFeature)feature;
 				return featureImpl.getName().startsWith(Extension.METACLASS_ROLE_PREFIX);
 			}
 		}
 		return false;
 	}
 
 	
 	public List getSemanticElementsBeingParsed(EObject eObject) {
 		Element element = (Element)eObject;
 		List<EObject> result = new LinkedList<EObject>();
 		//result.add(element);
 		result.addAll(element.getStereotypeApplications());
 		return result;
 	}
 
 	
 	public IContentAssistProcessor getCompletionProcessor(IAdaptable subject) {
 		Element element = doAdapt(subject);
 		List<Stereotype> remaining = new LinkedList<Stereotype>();
 		remaining.addAll(element.getApplicableStereotypes());
 		remaining.removeAll(element.getAppliedStereotypes());
 		
 		List<String> names = new LinkedList<String>();
 		for (Stereotype next : remaining){
 			names.add(next.getName());
 		}
 		return new FixedSetCompletionProcessor(names);
 	}
 
 	
 	public String getEditString(IAdaptable element, int flags) {
 		NamedElement subject = doAdapt(element);
 		List<Stereotype> stereos = subject.getAppliedStereotypes();
 		if (stereos.isEmpty()){
 			return "";
 		}
 		StringBuffer result = new StringBuffer();
 		for (Stereotype next : stereos){
 			if (result.length() > 0){
 				result.append(", ");
 			}
 			result.append(next.getName());
 		}
 		return result.toString();
 	}
 
 	
 	public ICommand getParseCommand(IAdaptable element, String newString, int flags) {
 		return UnexecutableCommand.INSTANCE;
 	}
 
 	
 	public String getPrintString(IAdaptable element, int flags) {
 		String editString = getEditString(element, flags);
		return editString == null || editString.length() == 0 ? editString : "<" + editString + ">";
 	}
 
 	
 	public boolean isAffectingEvent(Object event, int flags) {
 		return false;
 	}
 
 	
 	public IParserEditStatus isValidEditString(IAdaptable element, String editString) {
 		return ParserEditStatus.UNEDITABLE_STATUS;
 	}
 
 	private NamedElement doAdapt(IAdaptable adaptable) {
 		NamedElement element = (NamedElement)adaptable.getAdapter(EObject.class);
 		return element;
 	}
 	
 
 }
