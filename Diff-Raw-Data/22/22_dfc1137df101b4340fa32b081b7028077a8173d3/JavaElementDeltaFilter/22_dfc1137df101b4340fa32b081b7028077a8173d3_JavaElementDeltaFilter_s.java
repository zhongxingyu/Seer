 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder;
 
 import static org.eclipse.jdt.core.ElementChangedEvent.POST_CHANGE;
 import static org.eclipse.jdt.core.ElementChangedEvent.POST_RECONCILE;
 import static org.eclipse.jdt.core.IJavaElement.ANNOTATION;
 import static org.eclipse.jdt.core.IJavaElement.COMPILATION_UNIT;
 import static org.eclipse.jdt.core.IJavaElement.FIELD;
 import static org.eclipse.jdt.core.IJavaElement.JAVA_PROJECT;
 import static org.eclipse.jdt.core.IJavaElement.METHOD;
 import static org.eclipse.jdt.core.IJavaElement.PACKAGE_FRAGMENT_ROOT;
 import static org.eclipse.jdt.core.IJavaElement.TYPE;
 import static org.eclipse.jdt.core.IJavaElementDelta.ADDED;
 import static org.eclipse.jdt.core.IJavaElementDelta.CHANGED;
 import static org.eclipse.jdt.core.IJavaElementDelta.F_ADDED_TO_CLASSPATH;
 import static org.eclipse.jdt.core.IJavaElementDelta.F_AST_AFFECTED;
 import static org.eclipse.jdt.core.IJavaElementDelta.F_CONTENT;
 import static org.eclipse.jdt.core.IJavaElementDelta.F_FINE_GRAINED;
 import static org.eclipse.jdt.core.IJavaElementDelta.F_PRIMARY_RESOURCE;
 import static org.eclipse.jdt.core.IJavaElementDelta.F_REMOVED_FROM_CLASSPATH;
 import static org.eclipse.jdt.core.IJavaElementDelta.F_SUPER_TYPES;
 import static org.eclipse.jdt.core.IJavaElementDelta.REMOVED;
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.IJavaElementDeltaFlag.F_MARKER_ADDED;
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.IJavaElementDeltaFlag.F_MARKER_REMOVED;
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.IJavaElementDeltaFlag.F_SIGNATURE;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jdt.core.ElementChangedEvent;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaElementDelta;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.ConstantUtils;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.Logger;
 import org.jboss.tools.ws.jaxrs.core.jdt.JdtUtils;
 
 public class JavaElementDeltaFilter {
 
 	private final static int WORKING_COPY = 0x1;
 	private final static int PRIMARY_COPY = 0x2;
 
 	/**
 	 * The 'table' of rules for which the metamodel should be notified of Java
 	 * elements changes events.
 	 */
 	private final List<Rule> rules = new ArrayList<Rule>();
 
 	public JavaElementDeltaFilter() {
 		accept().when(JAVA_PROJECT).is(ADDED).after(POST_RECONCILE).in(PRIMARY_COPY);
 		accept().when(JAVA_PROJECT).is(REMOVED).after(POST_RECONCILE).in(PRIMARY_COPY);
 
 		accept().when(PACKAGE_FRAGMENT_ROOT).is(ADDED).after(POST_CHANGE).in(PRIMARY_COPY);
 		accept().when(PACKAGE_FRAGMENT_ROOT).is(ADDED).withFlags(F_ADDED_TO_CLASSPATH).after(POST_CHANGE)
 				.in(PRIMARY_COPY);
 		accept().when(PACKAGE_FRAGMENT_ROOT).is(REMOVED).withFlags(F_REMOVED_FROM_CLASSPATH).after(POST_CHANGE)
 				.in(PRIMARY_COPY);
 
 		accept().when(COMPILATION_UNIT).is(ADDED).after(POST_RECONCILE).in(PRIMARY_COPY);
 		accept().when(COMPILATION_UNIT).is(CHANGED).withFlags(F_CONTENT + F_PRIMARY_RESOURCE).after(POST_RECONCILE)
 				.in(PRIMARY_COPY);
 		accept().when(COMPILATION_UNIT).is(CHANGED).withFlags(F_CONTENT + F_FINE_GRAINED + F_AST_AFFECTED)
 				.after(POST_RECONCILE).in(WORKING_COPY);
 		accept().when(COMPILATION_UNIT).is(REMOVED).after(POST_RECONCILE).in(PRIMARY_COPY + WORKING_COPY);
 
 		accept().when(TYPE).is(ADDED).after(POST_RECONCILE).in(PRIMARY_COPY + WORKING_COPY);
 		// Supertypes changes. Renaming a type ends up with
 		// remove+add operations
 		accept().when(TYPE).is(CHANGED).withFlags(F_SUPER_TYPES).after(POST_RECONCILE).in(WORKING_COPY);
 		accept().when(TYPE).is(CHANGED).withFlags(F_MARKER_ADDED).after(POST_RECONCILE).in(WORKING_COPY);
 		accept().when(TYPE).is(REMOVED).after(POST_RECONCILE).in(PRIMARY_COPY + WORKING_COPY);
 
 		accept().when(METHOD).is(ADDED).after(POST_RECONCILE).in(PRIMARY_COPY + WORKING_COPY);
 		accept().when(METHOD).is(CHANGED).withFlags(F_SIGNATURE).after(POST_RECONCILE).in(PRIMARY_COPY + WORKING_COPY);
 		accept().when(METHOD).is(CHANGED).withFlags(F_MARKER_ADDED).after(POST_RECONCILE)
 				.in(PRIMARY_COPY + WORKING_COPY);
 		accept().when(METHOD).is(CHANGED).withFlags(F_MARKER_REMOVED).after(POST_RECONCILE)
 				.in(PRIMARY_COPY + WORKING_COPY);
 		accept().when(METHOD).is(REMOVED).after(POST_RECONCILE).in(PRIMARY_COPY + WORKING_COPY);
 		accept().when(METHOD).is(REMOVED).withFlags(F_CONTENT).after(POST_RECONCILE).in(PRIMARY_COPY + WORKING_COPY);
 
 		accept().when(FIELD).is(ADDED).after(POST_RECONCILE).in(PRIMARY_COPY + WORKING_COPY);
 		accept().when(FIELD).is(CHANGED).withFlags(F_CONTENT).after(POST_RECONCILE).in(PRIMARY_COPY + WORKING_COPY);
 		accept().when(FIELD).is(REMOVED).after(POST_RECONCILE).in(PRIMARY_COPY + WORKING_COPY);
 
 		accept().when(ANNOTATION).is(ADDED).after(POST_RECONCILE).in(PRIMARY_COPY + WORKING_COPY);
 		accept().when(ANNOTATION).is(CHANGED).withFlags(F_CONTENT).after(POST_RECONCILE)
 				.in(PRIMARY_COPY + WORKING_COPY);
 		accept().when(ANNOTATION).is(REMOVED).after(POST_RECONCILE).in(PRIMARY_COPY + WORKING_COPY);
 
 	}
 
 	private RuleBuilder accept() {
 		return new RuleBuilder();
 	}
 
 	/**
	 * Attempts to retrieve the CompilationUnitContext value matching the given
	 * parameters.
	 * 
	 * @param elementKind
	 *            the kind of Java element that changed.
	 * @param deltaKind
	 *            the kind of change.
	 * @param workingCopy
	 * @return the scope defined by the rules, or PRIMARY_COPY if nothing was
	 *         set.
	 * @see IJavaElementDelta, IJavaElementKind
 	 */

 	public boolean apply(JavaElementDelta event) {
 		int elementKind = event.getElement().getElementType();
 		int deltaKind = event.getDeltaKind();
 		IJavaElement element = event.getElement();
 		// prevent processing java elements in a closed java project
 		// prevent processing of any file named 'package-info.java'
 		// prevent processing of any jar file
 		if (isProjectClosed(element) || isPackageInfoFile(element) || isJarArchive(element)) {
 			return false;
 		}
 		int flags = event.getFlags();
 		if (flags == IJavaElementDelta.F_ANNOTATIONS) {
 			return false;
 		}
 		boolean workingCopy = JdtUtils.isWorkingCopy(element);
 		final boolean match = apply(elementKind, deltaKind, event.getEventType(), flags, workingCopy);
 		if (match) {
 			Logger.trace("**accepted** {}", event);
 		} else {
 			Logger.trace("**rejected** {}", event);
 		}
 		return match;
 	}
 
 	/**
 	 * 
 	 * @param element
 	 * @return true if the given java element is a Jar archive.
 	 */
 	private boolean isJarArchive(IJavaElement element) {
 		return (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT && ((IPackageFragmentRoot)element).isArchive());
             
 	}
 
 	/**
 	 * Returns true if the element resource is a file named 'package-info.java' (whatever the containing folder)
 	 * @param element
 	 * @return
 	 */
 	private boolean isPackageInfoFile(IJavaElement element) {
 		return element.getResource() != null && element.getResource().getType() == IResource.FILE && element.getResource().getName().equals("package-info.java");
 	}
 
 	/**
 	 * Returns true if the enclosing project is closed
 	 * @param element
 	 * @return
 	 */
 	private boolean isProjectClosed(IJavaElement element) {
 		return element.getJavaProject() != null && !element.getJavaProject().getProject().isOpen();
 	}
 
 	protected boolean apply(int elementKind, int deltaKind, int eventType, int flags, boolean workingCopy) {
 		Rule matcher = new Rule(elementKind, deltaKind, eventType, workingCopy ? WORKING_COPY : PRIMARY_COPY, flags);
 		for (Iterator<Rule> iterator = rules.iterator(); iterator.hasNext();) {
 			Rule rule = iterator.next();
 			if (rule.match(matcher)) {
 				Logger.trace("Rule {} matched", rule);
 				return true;
 			}
 
 		}
 		return false;
 
 	}
 
 	/**
 	 * Fluent API (FTW)
 	 * 
 	 * @author Xavier Coulon
 	 * 
 	 */
 	class RuleBuilder {
 
 		private int elementKind;
 		private int deltaKind;
 		private int eventType;
 		private int unitContext;
 		private int flags;
 
 		private RuleBuilder() {
 		}
 
 		public RuleBuilder when(int elementKind) {
 			this.elementKind = elementKind;
 			return this;
 		}
 
 		public RuleBuilder withFlags(int flags) {
 			this.flags = flags;
 			return this;
 		}
 
 		public RuleBuilder is(int deltaKind) {
 			this.deltaKind = deltaKind;
 			return this;
 		}
 
 		public RuleBuilder after(int eventType) {
 			this.eventType = eventType;
 			return this;
 		}
 
 		public void in(int unitContext) {
 			this.unitContext = unitContext;
 			Rule rule = new Rule(this.elementKind, this.deltaKind, this.eventType, this.unitContext, this.flags);
 			rules.add(rule);
 		}
 	}
 
 	static class Rule {
 
 		private final int elementKind;
 		private final int deltaKind;
 		private final int eventType;
 		private final int unitContext;
 		private final int flags;
 
 		Rule(int elementKind, int deltaKind, int eventType, int unitContext, int flags) {
 			this.elementKind = elementKind;
 			this.deltaKind = deltaKind;
 			this.eventType = eventType;
 			this.unitContext = unitContext;
 			this.flags = flags;
 		}
 
 		public boolean match(Rule matcher) {
 			return this.elementKind == matcher.elementKind && this.deltaKind == matcher.deltaKind
 					&& this.eventType == matcher.eventType && ((this.unitContext & matcher.unitContext) != 0)
 					&& (this.flags == matcher.flags);
 		}
 
 		@Override
 		public String toString() {
 			return new StringBuilder().append("[")
 					.append(ConstantUtils.getStaticFieldName(ElementChangedEvent.class, eventType)).append("] ")
 					.append(ConstantUtils.getStaticFieldName(IJavaElement.class, elementKind)).append(" ").append("[")
 					.append(ConstantUtils.getStaticFieldName(IJavaElementDelta.class, deltaKind)).append("]: ")
 					.toString();
 		}
 
 	}
 
 }
