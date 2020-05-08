 /*******************************************************************************
  * Copyright (c) 2010 Technical University of Denmark.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors:
  *    Patrick Koenemann, DTU Informatics - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.mpatch.generalize.impl;
 
 import java.util.regex.Pattern;
 
 import org.eclipse.emf.compare.mpatch.IElementReference;
 import org.eclipse.emf.compare.mpatch.MPatchModel;
 import org.eclipse.emf.compare.mpatch.common.util.MPatchConstants;
 import org.eclipse.emf.compare.mpatch.extension.IMPatchTransformation;
 import org.eclipse.emf.compare.mpatch.symrefs.Condition;
 import org.eclipse.emf.compare.mpatch.symrefs.ElementSetReference;
 import org.eclipse.emf.compare.mpatch.symrefs.OclCondition;
 
 /**
  * Transformation for extending the scope of changes by weakening OCL conditions of {@link ElementSetReference}s.
  * 
  * Example:<br>
  * <code>&lt;attribute name&gt; = '&lt;value&gt;'</code><br>
  * is replaced with<br>
  * <code>&lt;attribute name&gt;.containsIgnoreCase('&lt;value&gt;')</code>
  * 
  * @author Patrick Koenemann (pk@imm.dtu.dk)
  * 
  */
 public class ScopeExpansion implements IMPatchTransformation {
 
 	/**
 	 * The regular expression for searching substrings that should be replaced.
 	 * 
 	 * All {@link String} comparisons, example:<br>
 	 * <code>&lt;attribute name&gt; = '&lt;value&gt;'</code>
 	 */
 	protected static final String EQUAL_REPLACE = "([a-zA-Z0-9]+)\\s*=\\s*'([^']*)'";
 
 	/**
 	 * All {@link String} comparisons matching {@link ScopeExpansion#EQUAL_REPLACE} are replaced by.<br>
 	 * <code>&lt;attribute name&gt;.containsIgnoreCase('&lt;value&gt;')</code>
 	 */
 	protected static final String EQUAL_REPLACEMENT = "$1.checkSimilarity('$2', 0.7)";
 
 	/** Compiled pattern of {@link ScopeExpansion#EQUAL_REPLACE}. */
 	protected static final Pattern EQUAL_PATTERN = Pattern.compile(EQUAL_REPLACE);
 
 	/**
 	 * Additional weakening for UML models: removal of the attribute 'qualifiedName' which requires the element being
 	 * located in the original place.
 	 */
 	protected static final String UML_REPLACE = "and\\s*qualifiedName\\s*=\\s*'[^']*'";
 
 	/**
 	 * Replace it with an empty string.
 	 */
 	protected static final String UML_REPLACEMENT = "";
 
 	/** Compiled pattern of {@link ScopeExpansion#UML_REPLACE}. */
 	protected static final Pattern UML_PATTERN = Pattern.compile(UML_REPLACE);
 
 	/** Label for this transformation. */
 	private static final String LABEL = "Scope Expansion";
 
 	/** Description for this transformation. */
	private static final String DESCRIPTION = "This transformation expands the scope of changes by weakens the OCL condition of ElementSetReferences (consition-based "
 			+ MPatchConstants.SYMBOLIC_REFERENCES_NAME
 			+ "). This is an optional transformation and might change the result of "
 			+ MPatchConstants.MPATCH_SHORT_NAME
 			+ " application!\n\n"
			+ "It widens the conditions for applying changes which makes changes also applicable to slightly changed models. "
 			+ "The default OCL condition produced for condition-based symbolic references is "
 			+ "strictly bound to the original model element by primarily checking its attributes; "
			+ "this transformation weakens the conditions from equality to a case-insensitive containment check.\n"
			+ "Example: \"self.name = 'Library'\" becomes \"self.name.containsIgnoreCase('Library')\"";
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getLabel() {
 		return LABEL;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getDescription() {
 		return DESCRIPTION;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getPriority() {
 		return 20;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean isOptional() {
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int transform(MPatchModel mpatch) {
 		return weakenOCLConditions(mpatch);
 	}
 
 	/**
 	 * This iterates over all {@link ElementSetReference}s of the given {@link MPatchModel} and modifies the OCL
 	 * conditions as follows.
 	 * 
 	 * <code>&lt;attribute name&gt; = '&lt;value&gt;'</code><br>
 	 * is replaced with<br>
 	 * <code>&lt;attribute name&gt;.containsIgnoreCase('&lt;value&gt;')</code>
 	 * 
 	 * @param mpatch
 	 *            An {@link MPatchModel}.
 	 * @return The number of modified symbolic references.
 	 */
 	public static int weakenOCLConditions(MPatchModel mpatch) {
 		int counter = 0;
 		for (IElementReference ref : WeakeningHelper.getWeakenableSymbolicReferences(mpatch)) {
 			if (ref instanceof ElementSetReference) {
 				for (Condition condition : ((ElementSetReference) ref).getConditions()) {
 					if (weakenStringCondition(condition))
 						counter++;
 				}
 			}
 		}
 		return counter;
 	}
 
 	/**
 	 * Match {@link ScopeExpansion#EQUAL_REPLACE} and replace with {@link ScopeExpansion#EQUAL_REPLACEMENT}.
 	 * 
 	 * @param condition
 	 *            The condition to weaken.
 	 * @return <code>true</code>, if the condition was updated. <code>false</code> otherwise.
 	 */
 	protected static boolean weakenStringCondition(Condition condition) {
 		if (condition instanceof OclCondition) {
 			final OclCondition oclCondition = (OclCondition) condition;
 
 			final String oldExpr = oclCondition.getExpression();
 			final String umlExpr = UML_PATTERN.matcher(oldExpr).replaceAll(UML_REPLACEMENT);
 			final String equalExpr = EQUAL_PATTERN.matcher(umlExpr).replaceAll(EQUAL_REPLACEMENT);
 			if (!equalExpr.equals(oldExpr)) {
 				oclCondition.setExpression(equalExpr);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// /**
 	// * Just for testing purposes!
 	// */
 	// public static void main(String[] args) {
 	// final String[] input = new String[] {
 	// "singleAttribute = 'addAttribute' and multiAttribute->asSequence() = Sequence{'a','b'} and additionalAttribute->asSet()->isEmpty()",
 	// "singleAttribute = 'MoveContainmentModelElement' and multiAttribute->asSequence() = Sequence{} and additionalAttribute->asSet()->isEmpty()",
 	// "singleAttribute = 'UpdateReference' and multiAttribute->asSequence() = Sequence{} and additionalAttribute->asSet()->isEmpty()",
 	// "name = 'Book' and instanceClassName->asSet()->isEmpty() and instanceClass->asSet()->isEmpty() and defaultValue->asSet()->isEmpty() and instanceTypeName->asSet()->isEmpty() and abstract = false and interface = false",
 	// "name = 'Author' and instanceClassName->asSet()->isEmpty() and instanceClass->asSet()->isEmpty() and defaultValue->asSet()->isEmpty() and instanceTypeName->asSet()->isEmpty() and abstract = false and interface = false",
 	// "name = 'author' and ordered = true and unique = true and lowerBound = 0 and upperBound = 1 and many = false and required = false and changeable = true and volatile = false and transient = false and defaultValueLiteral->asSet()->isEmpty() and defaultValue->asSet()->isEmpty() and unsettable = false and derived = false and containment = false and container = false and resolveProxies = true",
 	// };
 	//
 	// final Pattern pattern = Pattern.compile(TO_REPLACE);
 	// for (int i = 0; i < input.length; i++) {
 	// final String output = pattern.matcher(input[i]).replaceAll(REPLACEMENT);
 	//
 	// // some output
 	// System.out.println(" IN> " + input[i]);
 	// System.out.println("OUT> " + output);
 	// System.out.println();
 	// }
 	// }
 }
