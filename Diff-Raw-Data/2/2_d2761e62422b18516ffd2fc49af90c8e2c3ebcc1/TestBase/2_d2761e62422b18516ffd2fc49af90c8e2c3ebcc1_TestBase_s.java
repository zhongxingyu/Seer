 /*******************************************************************************
  * Copyright (c) 2011 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.core;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Map;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EValidator;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.resource.XtextResourceSet;
 import org.eclipse.xtext.util.CancelIndicator;
 import org.eclipse.xtext.validation.CancelableDiagnostician;
 import org.eclipse.xtext.validation.CheckMode;
 import org.eclipse.xtext.validation.impl.ConcreteSyntaxEValidator;
 
 import com.google.common.collect.Maps;
 
 /**
  * Base class for tests helps with getting diagnostics from a model.
  *
  * @author Henrik Rentz-Reichert initial contribution and API
  *
  */
 public class TestBase {
 
 	private String basePath;
 
 	protected void prepare() {
 		try {
 			URL modelsDir = CoreTestsActivator.getInstance().getBundle().getEntry("models");
 			URL fileURL = FileLocator.toFileURL(modelsDir);
 			basePath = fileURL.getFile();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected Resource getResource(String modelName) {
 		XtextResourceSet rs = new XtextResourceSet();
 		rs.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
 		String path = basePath + modelName;
 		URI uri = URI.createFileURI(path);
 		return rs.getResource(uri, true);
 	}
 
 	public Diagnostic getDiag(EObject ele) {
 		Map<Object, Object> options = Maps.newHashMap();
 		options.put(CheckMode.KEY, CheckMode.ALL);
		options.put(CancelableDiagnostician.CANCEL_INDICATOR, new CancelIndicator.NullImpl());
 		// disable concrete syntax validation, since a semantic model that has been parsed 
 		// from the concrete syntax always complies with it - otherwise there are parse errors.
 		options.put(ConcreteSyntaxEValidator.DISABLE_CONCRETE_SYNTAX_EVALIDATOR, Boolean.TRUE);
 		// see EObjectValidator.getRootEValidator(Map<Object, Object>)
 		options.put(EValidator.class, CoreTestsActivator.getInstance().getDiagnostician());
 		return CoreTestsActivator.getInstance().getDiagnostician().validate(ele, options);
 	}
 }
