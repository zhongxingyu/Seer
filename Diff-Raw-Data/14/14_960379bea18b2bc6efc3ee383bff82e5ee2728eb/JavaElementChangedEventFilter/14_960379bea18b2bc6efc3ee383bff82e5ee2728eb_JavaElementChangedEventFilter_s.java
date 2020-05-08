 package org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder;
 
 import static org.eclipse.jdt.core.IJavaElement.ANNOTATION;
 import static org.eclipse.jdt.core.IJavaElement.COMPILATION_UNIT;
 import static org.eclipse.jdt.core.IJavaElement.FIELD;
 import static org.eclipse.jdt.core.IJavaElement.JAVA_PROJECT;
 import static org.eclipse.jdt.core.IJavaElement.METHOD;
 import static org.eclipse.jdt.core.IJavaElement.PACKAGE_FRAGMENT_ROOT;
 import static org.eclipse.jdt.core.IJavaElement.TYPE;
 import static org.eclipse.jdt.core.IJavaElementDelta.ADDED;
 import static org.eclipse.jdt.core.IJavaElementDelta.CHANGED;
 import static org.eclipse.jdt.core.IJavaElementDelta.F_CONTENT;
 import static org.eclipse.jdt.core.IJavaElementDelta.F_PRIMARY_RESOURCE;
 import static org.eclipse.jdt.core.IJavaElementDelta.F_SUPER_TYPES;
 import static org.eclipse.jdt.core.IJavaElementDelta.REMOVED;
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.IJavaElementDeltaFlag.F_MARKER_ADDED;
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.IJavaElementDeltaFlag.F_MARKER_REMOVED;
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.IJavaElementDeltaFlag.F_SIGNATURE;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaElementDelta;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.ConstantUtils;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.Logger;
 import org.jboss.tools.ws.jaxrs.core.jdt.JdtUtils;
 
 public class JavaElementChangedEventFilter {
 
 	private final static int WORKING_COPY = 0x1;
 	private final static int PRIMARY_COPY = 0x2;
 
 	/** The 'table' of rules for which the metamodel should be notified of Java
 	 * elements changes events. */
 	private final List<Rule> rules = new ArrayList<Rule>();
 
 	public JavaElementChangedEventFilter() {
 		addRule(JAVA_PROJECT, ADDED, PRIMARY_COPY);
 		addRule(JAVA_PROJECT, REMOVED, PRIMARY_COPY);
 
 		addRule(PACKAGE_FRAGMENT_ROOT, ADDED, PRIMARY_COPY);
 		addRule(PACKAGE_FRAGMENT_ROOT, REMOVED, PRIMARY_COPY);
 
 		addRule(COMPILATION_UNIT, ADDED, PRIMARY_COPY);
 		addRule(COMPILATION_UNIT, CHANGED, PRIMARY_COPY, F_CONTENT, F_PRIMARY_RESOURCE);
		addRule(COMPILATION_UNIT, REMOVED, PRIMARY_COPY);
 
 		addRule(TYPE, ADDED, PRIMARY_COPY + WORKING_COPY);
 		// Supertypes changes. Renaming a type ends up with
 		// remove+add operations
 		addRule(TYPE, CHANGED, WORKING_COPY, F_SUPER_TYPES);
 		// addRule(TYPE, CHANGED, WORKING_COPY, F_PROBLEM_SOLVED);
 		addRule(TYPE, REMOVED, PRIMARY_COPY + WORKING_COPY);
 
 		addRule(METHOD, ADDED, PRIMARY_COPY + WORKING_COPY);
 		addRule(METHOD, CHANGED, PRIMARY_COPY + WORKING_COPY, F_SIGNATURE); // signature
 		addRule(METHOD, CHANGED, PRIMARY_COPY + WORKING_COPY, F_CONTENT); // signature,
 																			// too..
 																			// :-/
 		addRule(METHOD, CHANGED, PRIMARY_COPY + WORKING_COPY, F_MARKER_ADDED);
 		addRule(METHOD, CHANGED, PRIMARY_COPY + WORKING_COPY, F_MARKER_REMOVED);
 		// addRule(METHOD, CHANGED, PRIMARY_COPY + WORKING_COPY,
 		// F_PROBLEM_SOLVED);
 		addRule(METHOD, REMOVED, PRIMARY_COPY + WORKING_COPY);
 
 		addRule(FIELD, ADDED, PRIMARY_COPY + WORKING_COPY);
 		addRule(FIELD, CHANGED, PRIMARY_COPY + WORKING_COPY);
 		addRule(FIELD, REMOVED, PRIMARY_COPY + WORKING_COPY);
 
 		addRule(ANNOTATION, ADDED, WORKING_COPY);
 		addRule(ANNOTATION, CHANGED, WORKING_COPY);
 		addRule(ANNOTATION, REMOVED, WORKING_COPY);
 
 	}
 
 	/** Add a scope for an element kind/delta kind, meaning that when this given
 	 * kind of element has this given kind of change and already belongs or not
 	 * to the metamodel, this later one will be notified of the change.
 	 * 
 	 * @param elementKind
 	 *            the kind of element that change
 	 * @param deltaKind
 	 *            the kind of change that occurred (added/changed/removed)
 	 * @param compilationUnitContext
 	 *            should this element instance be already part of the metamodel,
 	 *            or not ?
 	 * @see IJavaElementDelta, IJavaElementKind */
 	private void addRule(int elementKind, int deltaKind, int compilationUnitContext) {
 		addRule(elementKind, deltaKind, compilationUnitContext, null);
 	}
 
 	/** Add a scope for an element kind/delta kind, meaning that when this given
 	 * kind of element has this given kind of change and already belongs or not
 	 * to the metamodel, this later one will be notified of the change.
 	 * 
 	 * @param elementKind
 	 *            the kind of element that change
 	 * @param deltaKind
 	 *            the kind of change that occurred (added/changed/removed)
 	 * @param compilationUnitContext
 	 *            should this element instance be already part of the metamodel,
 	 *            or not ?
 	 * @see IJavaElementDelta, IJavaElementKind */
 	private void addRule(int elementKind, int deltaKind, int compilationUnitContext, int... flags) {
 		rules.add(new Rule(elementKind, deltaKind, compilationUnitContext, flags));
 	}
 
 	/** Attempts to retrieve the CompilationUnitContext value matching the given
 	 * parameters.
 	 * 
 	 * @param elementKind
 	 *            the kind of Java element that changed.
 	 * @param deltaKind
 	 *            the kind of change.
 	 * @param workingCopy
 	 * @return the scope defined by the rules, or PRIMARY_COPY if nothing was
 	 *         set.
 	 * @see IJavaElementDelta, IJavaElementKind */
 	protected boolean applyRules(int elementKind, int deltaKind, int[] flags, boolean workingCopy) {
 		Rule matcher = new Rule(elementKind, deltaKind, workingCopy ? WORKING_COPY : PRIMARY_COPY, flags);
 		for (Iterator<Rule> iterator = rules.iterator(); iterator.hasNext();) {
 			Rule rule = iterator.next();
 			if (rule.match(matcher)) {
 				return true;
 			}
 
 		}
 		return false;
 
 	}
 
 	public boolean apply(JavaElementChangedEvent event) {
 		int elementKind = event.getElementType();
 		int deltaKind = event.getDeltaKind();
 		IJavaElement element = event.getElement();
 		int[] flags = event.getFlags();
 		if (flags.length == 1 && (flags[0] & IJavaElementDelta.F_ANNOTATIONS) != 0) {
 			return false;
 		}
 		boolean workingCopy = JdtUtils.isWorkingCopy(element);
 		final boolean match = applyRules(elementKind, deltaKind, flags, workingCopy);
 		if (match) {
 			Logger.debug("Applied filters on event {} -> {}", event, match);
 		}
 		return match;
 	}
 
 	static class Rule {
 
 		private final int elementKind;
 		private final int deltaKind;
 		private final int unitContext;
 		private final int[] flags;
 
 		Rule(int elementKind, int deltaKind, int unitContext, int[] flags) {
 			this.elementKind = elementKind;
 			this.deltaKind = deltaKind;
 			this.unitContext = unitContext;
 			this.flags = flags;
 		}
 
 		public boolean match(Rule matcher) {
 			return this.elementKind == matcher.elementKind && this.deltaKind == matcher.deltaKind
 					&& ((this.unitContext & matcher.unitContext) != 0)
 					&& (this.flags == null || this.flags.length == 0 || Arrays.equals(this.flags, matcher.flags));
 		}
 
 		@Override
 		public String toString() {
 			return new StringBuilder(ConstantUtils.getStaticFieldName(IJavaElement.class, elementKind)).append(" [")
 					.append(ConstantUtils.getStaticFieldName(IJavaElementDelta.class, deltaKind)).append("]: ")
 					.append(ConstantUtils.getStaticFieldName(JavaElementChangedEventFilter.class, this.unitContext))
 					.toString();
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Object#hashCode()
 		 */
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + deltaKind;
 			result = prime * result + elementKind;
 			result = prime * result + Arrays.hashCode(flags);
 			result = prime * result + unitContext;
 			return result;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			Rule other = (Rule) obj;
 			if (deltaKind != other.deltaKind)
 				return false;
 			if (elementKind != other.elementKind)
 				return false;
 			if (!Arrays.equals(flags, other.flags))
 				return false;
 			if (unitContext != other.unitContext)
 				return false;
 			return true;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((rules == null) ? 0 : rules.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		JavaElementChangedEventFilter other = (JavaElementChangedEventFilter) obj;
 		if (rules == null) {
 			if (other.rules != null)
 				return false;
 		} else if (!Arrays.equals(rules.toArray(), other.rules.toArray()))
 			return false;
 		return true;
 	}
 
 }
