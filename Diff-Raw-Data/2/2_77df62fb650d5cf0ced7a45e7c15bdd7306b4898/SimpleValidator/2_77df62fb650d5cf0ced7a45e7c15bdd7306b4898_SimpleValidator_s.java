 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.validators.core.tests;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.validators.core.AbstractValidator;
 import org.eclipse.dltk.validators.core.IValidatorType;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class SimpleValidator extends AbstractValidator {
 	private String value = "";
 	boolean valid = true;
 	protected SimpleValidator(String id, IValidatorType type) {
 		super(id, null, type);
 	}
 	protected SimpleValidator(String id, String name, IValidatorType type) {
 		super(id, name, type);
 	}
 	protected SimpleValidator(String id, Element element, IValidatorType type) throws IOException {
 		super(id, null, type);
 		this.value = element.getAttribute("simple_value");
		this.valid = Boolean.parseBoolean(element.getAttribute("simple_valid"));
 	}
 
 	public void storeTo(Document doc, Element element) {
 		element.setAttribute("simple_value", this.value);
 		element.setAttribute("simple_valid", Boolean.toString(this.valid));
 	}
 
 	public IStatus validate(ISourceModule module, OutputStream console) {
 		return Status.OK_STATUS;
 	}
 
 	public IStatus validate(IResource resource, OutputStream console) {
 		return Status.OK_STATUS;
 	}
 	public void setValid(boolean b) {
 		this.valid = b;
 	}
 	public boolean isValidatorValid() {
 		return this.valid;
 	}
 	public void clean(ISourceModule module) {
 		// TODO Auto-generated method stub
 		
 	}
 	public void clean(IResource resource) {
 		// TODO Auto-generated method stub
 	}
 }
