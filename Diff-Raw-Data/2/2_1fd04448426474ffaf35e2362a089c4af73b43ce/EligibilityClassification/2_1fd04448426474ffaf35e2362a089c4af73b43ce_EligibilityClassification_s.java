 // © Maastro Clinic, 2013
 package nl.maastro.eureca.aida.search.zylabpatisclient.classification;
 
 /**
  * Based on the found concepts and their modifiers a Patient's eligibilty is
  * classified.
  * 
  * @author Kasper van den Berg <kasper.vandenberg@maastron.nl> <kasper@kaspervandenberg.net>
  */
 public enum EligibilityClassification {
 	/**
 	 * The patient is not eligible for a clinical trial or the document marks
 	 * the patient as not eligible.
 	 */
 	NOT_ELIGIBLE,
 
 	/**
 	 * An exclusion criterion is found but the criterion is modified by a
 	 * {@link SemanticModifier}, making the patient's eligibility uncertain.
 	 * 
 	 * <p><em>NOTE: {@code UNCERTAIN} is reserved for semantically modified
 	 * criteria.  If different documents or different matches within one
 	 * document result in different {@code Classifications}, use a set of 
 	 * {@code Classifications} and do not classify as {@code UNCERTAIN}.</em></p>
 	 * 
 	 * <p><em>NOTE: compare with {@link #UNKNOWN}</em></p>
 	 */
 	UNCERTAIN,
 
 	/**
 	 * When the searched criterion is not found in documents about the patient,
 	 * the patient is classified as {@code NO_EXCLUSION_CRITERION_FOUND}; an
 	 * expert must decide whether the patient is eligible for a trial.
 	 */
	NO_EXCLUSION_CRITERION_FOUND,
 
 	/**
 	 * Used for {@link SearchResult} based on queries with unknown criterion/
 	 * semantically modified criterion source.
 	 */	
 	UNKNOWN
 }
