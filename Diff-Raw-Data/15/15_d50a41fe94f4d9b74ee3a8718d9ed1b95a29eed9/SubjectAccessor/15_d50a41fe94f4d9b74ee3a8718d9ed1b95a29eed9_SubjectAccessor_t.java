 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.security.common;
 
 import javax.security.auth.Subject;
 
 import org.eclipse.riena.core.injector.Inject;
 import org.eclipse.riena.internal.security.common.Activator;
 
 /**
  * A convenient class to access the current Subject from the
  * SubjectHolderService
  */
public final class SubjectAccessor {
 
 	private ISubjectHolderService subjectHolderService;
 	private static SubjectAccessor myself;
 
 	private SubjectAccessor() {
 		super();
 		Inject.service(ISubjectHolderService.class.getName()).useRanking().into(this).andStart(
 				Activator.getDefault().getContext());
 	}
 
 	public static Subject getSubject() {
 		return getInstance().getCurrentSubject();
 	}
 
 	public static void setSubject(Subject subject) {
 		getInstance().setCurrentSubject(subject);
 	}
 
 	/**
 	 * @return
 	 */
 	private static SubjectAccessor getInstance() {
 		if (myself == null) {
 			myself = new SubjectAccessor();
 		}
 		return myself;
 	}
 
 	public void bind(ISubjectHolderService subjectHolderService) {
 		this.subjectHolderService = subjectHolderService;
 	}
 
 	public void unbind(ISubjectHolderService subjectHolderService) {
 		this.subjectHolderService = null;
 	}
 
 	public Subject getCurrentSubject() {
 		return subjectHolderService.fetchSubjectHolder().getSubject();
 	}
 
 	public void setCurrentSubject(Subject subject) {
 		subjectHolderService.fetchSubjectHolder().setSubject(subject);
 	}
 
 }
