 /*
  *     Eclipse AST Validation, lite framework to validate java code
  *     
  *     Copyright (C) 2013 Atos Worldline or third-party contributors as
  *     indicated by the @author tags or express copyright attribution
  *     statements applied by the authors.
  *     
  *     This library is free software; you can redistribute it and/or
  *     modify it under the terms of the GNU Lesser General Public
  *     License as published by the Free Software Foundation; either
  *     version 2.1 of the License.
  *     
  *     This library is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  *     Lesser General Public License for more details.
  *     
  *     You should have received a copy of the GNU Lesser General Public
  *     License along with this library; if not, write to the Free Software
  *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package net.atos.jdt.ast.validation.engine;
 
 import net.atos.jdt.ast.validation.engine.internal.Activator;
 
 /**
  * API to handle rules enablement through preferences
  * 
  * @author mvanbesien
  * 
  */
 public class ASTRulesPreferences {
 
 	/**
 	 * Key for participant enablement/disablement
 	 */
 	private static final String PARTICIPANT_DISABLED = "participant.disabled";
 
 	/**
 	 * Key for participant enablement/disablement
 	 */
 	private static final String RULES_ARE_SINGLETONS = "rules.are.singletons";
 
 	/**
 	 * Enablement of Validation Participant
 	 */
 	public static void enableValidationParticipant() {
 		final String key = ASTRulesPreferences.getParticipantKey();
 		Activator.getDefault().getPreferenceStore().setValue(key, false);
 	}
 
 	/**
 	 * Enablement of Validation Participant
 	 */
 	public static void disableValidationParticipant() {
 		final String key = ASTRulesPreferences.getParticipantKey();
 		Activator.getDefault().getPreferenceStore().setValue(key, true);
 	}
 
 	/**
 	 * @return true if the Compilation Unit participant for validation is
 	 *         enabled.
 	 */
 	public static boolean isValidationParticipantEnabled() {
 		final String key = ASTRulesPreferences.getParticipantKey();
 		return !Activator.getDefault().getPreferenceStore().getBoolean(key);
 	}
 
 	/**
 	 * Enable rule in preferences
 	 */
 	public static void enable(final ASTRuleDescriptor descriptor) {
 		ASTRulesPreferences.setEnabled(descriptor, false);
 	}
 
 	/**
 	 * Disable rule in preferences
 	 */
 	public static void disable(final ASTRuleDescriptor descriptor) {
 		ASTRulesPreferences.setEnabled(descriptor, true);
 	}
 
 	/**
 	 * enable/disable rule in preferences
 	 */
 	public static void setEnabled(final ASTRuleDescriptor descriptor, final boolean value) {
 		final String key = ASTRulesPreferences.getRuleKeyFor(descriptor);
 		Activator.getDefault().getPreferenceStore().setValue(key, value);
 	}
 
 	/**
 	 * Checks rule's enablement in preferences
 	 */
 	public static boolean isEnabled(final ASTRuleDescriptor descriptor) {
 		final String key = ASTRulesPreferences.getRuleKeyFor(descriptor);
 		return !Activator.getDefault().getPreferenceStore().getBoolean(key);
 	}
 
 	/**
 	 * Returns the key for the current rule.
 	 * 
 	 * @param descriptor
 	 * @return
 	 */
 	private static String getRuleKeyFor(final ASTRuleDescriptor descriptor) {
 		return "rule.disabled." + descriptor.getRuleClassName();
 	}
 
 	/**
 	 * Returns the key for the current rule.
 	 * 
 	 * @param descriptor
 	 * @return
 	 */
 	private static String getParticipantKey() {
 		return ASTRulesPreferences.PARTICIPANT_DISABLED;
 	}
 
 	/**
 	 * returns true if same rule instance is used across executions.
 	 * 
 	 * @return
 	 */
 	public static boolean areRulesSingletons() {
 		final String key = ASTRulesPreferences.areRulesSingletonsKey();
 		return Activator.getDefault().getPreferenceStore().getBoolean(key);
 	}
 
 	/**
 	 * 
 	 * @param use
 	 *            true if same rule instance should be used across executions.
 	 */
 	public static void setUseRulesAsSingletons(final boolean use) {
 		final String key = ASTRulesPreferences.areRulesSingletonsKey();
 		Activator.getDefault().getPreferenceStore().setValue(key, use);
 	}
 
 	/**
 	 * Returns the key for the singleton option
 	 * 
 	 * @return
 	 */
 	private static String areRulesSingletonsKey() {
 		return ASTRulesPreferences.RULES_ARE_SINGLETONS;
 	}
 
 }
