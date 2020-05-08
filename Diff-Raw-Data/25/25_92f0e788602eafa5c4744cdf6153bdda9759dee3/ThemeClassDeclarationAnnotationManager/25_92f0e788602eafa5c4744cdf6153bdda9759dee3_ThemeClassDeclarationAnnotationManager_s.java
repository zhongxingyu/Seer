 /*******************************************************************************
  * Copyright (c) 2012 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.core.themes.annotation.managers;
 
 import java.util.Hashtable;
 
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.wazaabi.engine.core.annotations.managers.AnnotationManager;
 import org.eclipse.wazaabi.engine.core.editparts.AbstractWidgetEditPart;
 import org.eclipse.wazaabi.mm.core.annotations.Annotation;
 import org.eclipse.wazaabi.mm.core.annotations.AnnotationContent;
 import org.eclipse.wazaabi.mm.core.themes.Themes.Theme;
 import org.eclipse.wazaabi.mm.core.widgets.Widget;
 import org.eclipse.wazaabi.mm.edp.handlers.EventHandler;
 import org.eclipse.wazaabi.mm.edp.handlers.Parameter;
 import org.eclipse.wazaabi.mm.edp.handlers.StringParameter;
 
 public class ThemeClassDeclarationAnnotationManager extends AnnotationManager {
 
 	public static final String CORE_THEMES_CLASS_ANNOTATION_SOURCE = "http://www.wazaabi.org/core/themes/class"; //$NON-NLS-1$
 	// TODO : move this
 	public static final String CORE_THEMES_PARAMETER_ANNOTATION_SOURCE = "http://www.wazaabi.org/core/themes/parameter"; //$NON-NLS-1$
 	protected static final String CLASS_KEY = "class"; //$NON-NLS-1$
 
 	public ThemeClassDeclarationAnnotationManager(Annotation annotation) {
 		super(annotation);
 	}
 
 	public void processAnnotation(AbstractWidgetEditPart host) {
 		if (getAnnotation() == null)
 			return;
 		for (AnnotationContent content : getAnnotation().getContents()) {
 			if (CLASS_KEY.equals(content.getKey()))
 				processClassDeclaration(host, content.getValue());
 		}
 	}
 
 	protected void processMergeFirst(AbstractWidgetEditPart host, Theme theme) {
 	}
 
 	protected boolean checkSourceCorrectness(String source) {
 		return CORE_THEMES_CLASS_ANNOTATION_SOURCE.equals(source);
 	}
 
 	public static String getCoreThemeClassDeclaration(Widget widget) {
 		for (Annotation annotation : widget.getAnnotations())
 			if (CORE_THEMES_CLASS_ANNOTATION_SOURCE.equals(annotation
 					.getSource()))
 				for (AnnotationContent annotationContent : annotation
 						.getContents())
 					if (CLASS_KEY.equals(annotationContent.getKey()))
 						return annotationContent.getValue();
 		return null;
 	}
 
 	protected void processClassDeclaration(AbstractWidgetEditPart host,
 			String className) {
 		if (className == null || "".equals(className)) //$NON-NLS-1$
 			return;
 		if (!(host.getModel() instanceof Widget))
 			return;
 		Widget target = (Widget) host.getModel();
 		Hashtable<String, Object> variables = getVariables(target);
 		Theme firstMergedTheme = ThemeDeclarationAnnotationManager
 				.resolveFirstMergedTheme(target, className);
 		Theme insertedTheme = ThemeDeclarationAnnotationManager
 				.resolveInsertedTheme(target, className);
		for (Widget source : insertedTheme.getChildren())
			applyInsert(source, target, variables);

 	}
 
 	protected void applyInsert(Widget source, Widget destination,
 			Hashtable<String, Object> variables) {
 		// First we process EventHandlers
 		for (EventHandler eventHandler : source.getHandlers()) {
 			EventHandler clone = (EventHandler) EcoreUtil.copy(eventHandler);
 			replaceVariables(clone, destination, variables);
 			destination.getHandlers().add(clone);
 		}
 	}
 
 	protected void replaceVariables(EventHandler eventHandler,
 			Widget destination, Hashtable<String, Object> variables) {
 		for (Parameter parameter : eventHandler.getParameters()) {
 			if (parameter instanceof StringParameter) {
 				final String value = ((StringParameter) parameter).getValue();
 				if (value != null && !"".equals(value)
 						&& value.startsWith("${") && value.endsWith("}")
 						&& value.length() > 2) {
 					String variableName = value
 							.substring(2, value.length() - 1);
 					((StringParameter) parameter).setValue((String) variables
 							.get(variableName));
 				}
 			}
 		}
 	}
 
 	protected Hashtable<String, Object> getVariables(Widget widget) {
 		Hashtable<String, Object> variables = new Hashtable<String, Object>();
 		for (Annotation annotation : widget.getAnnotations()) {
 			if (CORE_THEMES_PARAMETER_ANNOTATION_SOURCE.equals(annotation
 					.getSource())) {
 				for (AnnotationContent content : annotation.getContents()) {
 					if (content.getKey() != null
 							&& !"".equals(content.getKey())) {
 						variables.put(content.getKey(), content.getValue());
 					}
 				}
 			}
 		}
 		return variables;
 	}
 }
