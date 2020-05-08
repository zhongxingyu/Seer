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
 
 package org.eclipse.wazaabi.engine.core.annotations.factories.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.wazaabi.engine.core.annotations.factories.AnnotationManagerFactory;
 import org.eclipse.wazaabi.engine.core.annotations.factories.ComposedAnnotationManagerFactory;
 import org.eclipse.wazaabi.engine.core.annotations.managers.AnnotationManager;
 import org.eclipse.wazaabi.mm.core.annotations.Annotation;
 import org.eclipse.wazaabi.mm.core.widgets.Widget;
 
 public class ComposedAnnotationManagerFactoryImpl implements
 		ComposedAnnotationManagerFactory {
 
 	private List<AnnotationManagerFactory> factories = new ArrayList<AnnotationManagerFactory>();
 
 	public void addAnnotationManagerFactory(AnnotationManagerFactory factory) {
 		if (factory != null && !factories.contains(factory))
 			factories.add(factory);
 	}
 
 	public void removeAnnotationManagerFactory(AnnotationManagerFactory factory) {
 		if (factory != null)
 			factories.remove(factory);
 	}
 
 	public List<AnnotationManager> getRelevantAnnotationManagers(Widget widget) {
		List<AnnotationManager> annotationManagers = new ArrayList<>();
 		for (AnnotationManagerFactory factory : factories)
 			for (Annotation annotation : widget.getAnnotations()) {
 				AnnotationManager annotationManager = factory
 						.createAnnotationManager(annotation);
 				if (annotationManager != null)
 					annotationManagers.add(annotationManager);
 			}
 		return annotationManagers;
 	}
 
 }
