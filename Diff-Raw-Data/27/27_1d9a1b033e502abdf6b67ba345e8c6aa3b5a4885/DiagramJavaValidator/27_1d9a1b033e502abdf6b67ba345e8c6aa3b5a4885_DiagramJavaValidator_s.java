 /*
  * Copyright 2011-2011 R.M.Morrien. 
  * Licensed under the Eclipse Public License, Version 1.0
  * Initial Developer: R.M.Morrien
  */
 package org.facttype.validation;
 
 import org.eclipse.xtext.validation.Check;
 import org.facttype.diagram.AbstractRule;
 import org.facttype.diagram.Column;
 import org.facttype.diagram.DerivationRule;
 import org.facttype.diagram.DiagramPackage;
 import org.facttype.diagram.EqualityRule;
 import org.facttype.diagram.EventRule;
 import org.facttype.diagram.ExclusionRule;
 import org.facttype.diagram.FactTypeDiagram;
 import org.facttype.diagram.OccurrenceFrequencyRule;
 import org.facttype.diagram.PartialEqualityRule;
 import org.facttype.diagram.SubsetRule;
 
 /**
  * Fact Type Diagram model checkers. These model checks are used to give
  * feedback to invalid models while you edit your fact type diagram model in the
  * IDE model editor.
  */
 public class DiagramJavaValidator extends AbstractDiagramJavaValidator {
 
 	/**
 	 * Checks if the column name starts with a capital. This is mainly here
 	 * because it was the xtext Check demo, but it also seems to be suitable.
 	 * 
 	 * @param column
 	 */
 	@Check
 	public void checkColumnStartsWithCapital(Column column) {
 		if (!Character.isUpperCase(column.getName().charAt(0))) {
 			warning("Name should start with a capital",
 					DiagramPackage.Literals.COLUMN__NAME);
 		}
 	}
 
 	/**
 	 * Checks if there is a column defined with a different number of example
 	 * values. All columns should have the same number of values.
 	 * 
 	 * @param diagram
 	 *            The diagram to validate.
 	 */
 	@Check
 	public void checkColumnHaveSameNumberOfValues(FactTypeDiagram diagram) {
 		Column previousColumn = null;
 		int i = 0;
 		for (Column column : diagram.getColumns()) {
 			if (previousColumn != null) {
 				if (previousColumn.getValues().size() != column.getValues()
 						.size()) {
 					error(previousColumn.getName() + ".values size "
 							+ previousColumn.getValues().size()
 							+ " is not equal to " + column.getName()
 							+ ".values size " + column.getValues().size(),
 							DiagramPackage.Literals.FACT_TYPE_DIAGRAM__COLUMNS,
 							i);
 				}
 			}
 			previousColumn = column;
 			i++;
 		}
 	}
 
 	/**
 	 * Checks if a rule has references. References are optional in the model 
 	 * to allow easy modeling, but there should be references. This check
 	 * warns you about missing references. 
 	 * @param abstractRule The rule to check for references.
 	 */
 	@Check
 	public void checkAbstractRuleForReferences(AbstractRule abstractRule) {
 		if (abstractRule instanceof SubsetRule) {
 			if (((SubsetRule)abstractRule).getReferences().size() == 0) {
 				warning("Rule " + abstractRule.getName() + " has no references.",
 						DiagramPackage.Literals.SUBSET_RULE__REFERENCES);
 			}
 		}
 		if (abstractRule instanceof EqualityRule) {
 			if (((EqualityRule)abstractRule).getReferences().size() == 0) {
 				warning("Rule " + abstractRule.getName() + " has no references.",
 						DiagramPackage.Literals.EQUALITY_RULE__REFERENCES);
 			}
 		}
 		if (abstractRule instanceof PartialEqualityRule) {
 			if (((PartialEqualityRule)abstractRule).getReferences().size() == 0) {
 				warning("Rule " + abstractRule.getName() + " has no references.",
 						DiagramPackage.Literals.PARTIAL_EQUALITY_RULE__REFERENCES);
 			}
 		}
 		if (abstractRule instanceof ExclusionRule) {
 			if (((ExclusionRule)abstractRule).getReferences().size() == 0) {
 				warning("Rule " + abstractRule.getName() + " has no references.",
 						DiagramPackage.Literals.EXCLUSION_RULE__REFERENCES);
 			}
 		}
 		if (abstractRule instanceof DerivationRule) {
 			if (((DerivationRule)abstractRule).getReferences().size() == 0) {
 				warning("Rule " + abstractRule.getName() + " has no references.",
 						DiagramPackage.Literals.DERIVATION_RULE__REFERENCES);
 			}
 		}
 	}
 	
 	/**
 	 * Checks if a EventRule has triggers. Triggers are optional in the model 
 	 * to allow easy modeling, but there should be triggers. This check
 	 * warns you about missing triggers. 
 	 * @param eventRule The rule to check for triggers.
 	 */
 	@Check
 	public void checkEventRuleForTriggers(EventRule eventRule) {
 		if (eventRule.getTriggers().size() == 0) {
 			warning("Rule " + eventRule.getName() + " has no triggers.",
 					DiagramPackage.Literals.EVENT_RULE__TRIGGERS);
 		}
 	}
 	
 	/**
 	 * Checks if a SubsetRule has a super side. 
 	 * @param rule The rule to check for a super side.
 	 */
 	@Check
 	public void checkSubsetRuleForSuper(SubsetRule rule) {
 		if (rule.isSuper()) {
 			// This is the super side, check that none of the references
 			// is a super side as well
 			for (SubsetRule referencedRule : rule.getReferences()) {
 				if (referencedRule.isSuper()) {
 					error("Rule [" + rule.getName() + "] is a super side, but references [" + referencedRule.getName() + "] " +
 							"which is also marked as super. Super must be on one side only.",
 							DiagramPackage.Literals.SUBSET_RULE__REFERENCES);
 							break;
 				}
 			}
 		} else {
 			// This is not the super side, one of the references must be a super side
 			boolean superFound = false;
 			for (SubsetRule referencedRule : rule.getReferences()) {
 				if (referencedRule.isSuper()) {
 					superFound = true;
 				}
 			}
 			if (!superFound) {
 				error("Rule [" + rule.getName() + "] is not marked as super, but does not reference a SubsetRule marked as super. Super must be set on exactly one side of the association.",
 					DiagramPackage.Literals.SUBSET_RULE__REFERENCES);
 			}
 		}
 	}
 	
 	/**
 	 * Checks if a EqualityRule has a super side. 
 	 * @param rule The rule to check for a super side.
 	 */
 	@Check
 	public void checkEqualityRuleForSuper(EqualityRule rule) {
 		if (rule.isSuper()) {
 			// This is the super side, check that none of the references
 			// is a super side as well
 			for (EqualityRule referencedRule : rule.getReferences()) {
 				if (referencedRule.isSuper()) {
 					error("Rule [" + rule.getName() + "] is a super side, but references [" + referencedRule.getName() + "] " +
 							"which is also marked as super. Super must be on one side only.",
 							DiagramPackage.Literals.EQUALITY_RULE__REFERENCES);
 							break;
 				}
 			}
 		} else {
 			// This is not the super side, one of the references must be a super side
 			boolean superFound = false;
 			for (EqualityRule referencedRule : rule.getReferences()) {
 				if (referencedRule.isSuper()) {
 					superFound = true;
 				}
 			}
 			if (!superFound) {
 				error("Rule [" + rule.getName() + "] is not marked as super, but does not reference a EqualityRule " +
 						"marked as super. Super must be on one side.",
 						DiagramPackage.Literals.EQUALITY_RULE__REFERENCES);
 			}
 		}
 	}
 	
 	/**
 	 * Checks if an OccurrenceFrequencyRule has a minimum or maximum. It should have at least one of these, or both.
 	 * @param occurrenceFrequencyRule The rule to check.
 	 */
 	@Check
 	public void checkOccurrenceFrequencyRuleHasMinimumOrMaximum(OccurrenceFrequencyRule occurrenceFrequencyRule) {
 		if (occurrenceFrequencyRule.getMinimum() == null && occurrenceFrequencyRule.getMaximum() == null) {
 			error("Rule " + occurrenceFrequencyRule.getName() + " must have a minimum or a maximum or both.",
 					DiagramPackage.Literals.OCCURRENCE_FREQUENCY_RULE__MINIMUM);
 		}
 	}
 	
 }
