 package ch.epfl.data.distribdb.parsing;
 
 /**
  * An SQL field.
  * 
  * @author Artyom Stetsenko
  */
 public abstract class Field implements Operand {
 	
 	public static final String ALIAS_ANONYMOUS_PREFIX = "f";
 	
 	/**
 	 * Field alias (e.g. "myfield" in SELECT t.id AS myfield FROM t")
 	 */
 	protected String alias;
 	
 	/**
 	 * Setter for field alias.
 	 * 
 	 * @param alias
 	 *                new field alias
 	 * @return this field
 	 */
 	public abstract Field setAlias(String alias);
 	
 	/**
 	 * Retrieves whether this field is aggregate.
 	 * 
 	 * @return true if this field is aggregate or contains aggregate subfields, false otherwise
 	 */
 	public abstract boolean isAggregate();
 	
 	/**
 	 * Getter for field alias.
 	 * 
 	 * @return this field's alias
 	 */
 	public String getAlias() {
 		return this.alias;
 	}
 	
 	@Override
 	public abstract String toString();
 	
 	/**
 	 * Retrieves the intermediate string representation of this field. Intermediate string
 	 * representations are used when an aggregate query is run on the worker nodes (i.e.
 	 * the first step of execution of this query, with the intent of running the final
 	 * query afterwards).
 	 *  
 	 * @return the intermediate string representation of this field
 	 */
 	public String toIntermediateString() {
 		return this.toString();
 	}
 	
 	/**
 	 * Retrieves the final string representation of this field. Final string representations
 	 * are used when an aggregate query is run on the master node to merge intermediate
 	 * aggregate results from the worker nodes (i.e. the second and final step of execution
 	 * of this query).
 	 * 
 	 * @param intermediateRelation
 	 *                the named relation on the master node holding intermediate results
 	 *                from the worker nodes
 	 * @param i
 	 *                unique sequential number of this field (used to identify intermediateRelation
 	 *                fields corresponding to this field
 	 * @return the final string representation of this field
 	 */
 	public String toFinalString(NamedRelation intermediateRelation, int i) {
 		return this.toFinalString(intermediateRelation, ALIAS_ANONYMOUS_PREFIX, i);
 	}
 	
 	/**
 	 * Retrieves the final string representation of this field. Final string representations
 	 * are used when an aggregate query is run on the master node to merge intermediate
 	 * aggregate results from the worker nodes (i.e. the second and final step of execution
 	 * of this query).
 	 * 
 	 * @param intermediateRelation
 	 *                the named relation on the master node holding intermediate results
 	 *                from the worker nodes
 	 * @param prefix
 	 *                the prefix of this field's name in intermediateRelation
 	 * @param i
 	 *                unique sequential number of this field (used to identify the intermediateRelation
 	 *                field corresponding to this field)
 	 * @return the final string representation of this field
 	 */
 	public String toFinalString(NamedRelation intermediateRelation, String prefix, int i) {
 		return String.format(
 				"%s.%s",
 				intermediateRelation.getAlias() != null ? intermediateRelation.getAlias() : intermediateRelation.getName(),
				prefix + i
 				);
 	}
 	
 	/**
 	 * Retrieves the aliased string representation of this field that is used in the GROUP BY, HAVING,
 	 * and ORDER BY clauses.
 	 * 
 	 * @return the field alias if this field has one, or the regular string representation otherwise
 	 */
 	public String toAliasedString() {
 		return this.alias != null ? this.alias : this.toString();
 	}
 	
 	/**
 	 * Retrieves the intermediate aliased string representation of this field that is used in
 	 * the GROUP BY clause of the intermediate string representation of this field's query.
 	 * 
 	 * @param i
 	 *                unique sequential number of this field (to be used later to identify the
 	 *                field in the resulting intermediate relation that corresponds to this field)
 	 * @return the field alias if this field has one, or anonymous prefix concatenated with i
 	 *         otherwise
 	 */
 	public String toAliasedIntermediateString(int i) {
 		if (this.isAggregate()) throw new IllegalStateException("Cannot convert an aggregate field to an aliased intermediate string.");
 		return this.alias != null ? this.alias : ALIAS_ANONYMOUS_PREFIX + i;
 	}
 	
 	/**
 	 * Retrieves the final aliased string representation of this field that is used in the GROUP BY,
 	 * HAVING, and ORDER BY clauses of the final string representation of this field's query.
 	 * 
 	 * @param intermediateRelation
 	 *                the named relation on the master node holding intermediate results
 	 *                from the worker nodes
 	 * @param i
 	 *                unique sequential number of this field (used to identify the intermediateRelation
 	 *                field corresponding to this field)
 	 * @return the field alias if this field has one, or the final string representation of this field
 	 *         otherwise
 	 */
 	public String toAliasedFinalString(NamedRelation intermediateRelation, int i) {
 		return this.alias != null ? this.alias : this.toFinalString(intermediateRelation, i);
 	}
 	
 	/**
 	 * Retrieves the full string representation of this field that is used in the SELECT clause.
 	 * 
 	 * @return this field's string representation concatenated with "AS [alias]" if this field
 	 *         has an alias
 	 */
 	public String toFullString() {
 		return this.alias != null ? String.format("%s AS %s", this.toString(), this.alias) : this.toString();
 	}
 	
 	/**
 	 * Retrieves the intermediate full string representation of this field that is used in the
 	 * SELECT clause of the intermediate string representation of this field's query.
 	 * 
 	 * @param i
 	 *                unique sequential number of this field (to be used later to identify the
 	 *                field in the resulting intermediate relation that corresponds to this field)
 	 * @return this field's intermediate string representation with the field's alias if it has one,
 	 *         or the default anonymous alias prefix concatenated with i
 	 */
 	public String toFullIntermediateString(int i) {
 		return this.toFullIntermediateString(ALIAS_ANONYMOUS_PREFIX, i);
 	}
 	
 	/**
 	 * Retrieves the intermediate full string representation of this field that is used in the
 	 * SELECT clause of the intermediate string representation of this field's query.
 	 * 
 	 * @param prefix
 	 *                the prefix of this field's name (to be used in the name of this field
 	 *                in the intermediate relation)
 	 * @param i
 	 *                unique sequential number of this field (to be used later to identify the
 	 *                field in the resulting intermediate relation that corresponds to this field)
 	 * @return this field's intermediate string representation with the field's alias if it has one,
 	 *         or prefix concatenated with i
 	 */
 	public String toFullIntermediateString(String prefix, int i) {
 		return String.format("%s AS %s", this.toIntermediateString(), this.alias != null ? this.alias : prefix + i);
 	}
 	
 	/**
 	 * Retrieves the final full string representation of this field that is used in the SELECT
 	 * clause of the final string representation of this field's query.
 	 * 
 	 * @param intermediateRelation
 	 *                the named relation on the master node holding intermediate results
 	 *                from the worker nodes
 	 * @param i
 	 *                unique sequential number of this field (used to identify the intermediateRelation
 	 *                field corresponding to this field)
 	 * @return this field's final string representation with default anonymous alias prefix
 	 *         concatenated with i, concatenated with "AS [alias]" if this field has an alias
 	 */
 	public String toFullFinalString(NamedRelation intermediateRelation, int i) {
 		return this.toFullFinalString(intermediateRelation, ALIAS_ANONYMOUS_PREFIX, i);
 	}
 	
 	/**
 	 * Retrieves the final full string representation of this field that is used in the SELECT
 	 * clause of the final string representation of this field's query.
 	 * 
 	 * @param intermediateRelation
 	 *                the named relation on the master node holding intermediate results
 	 *                from the worker nodes
 	 * @param prefix
 	 *                the prefix of this field's name in intermediateRelation
 	 * @param i
 	 *                unique sequential number of this field (used to identify the intermediateRelation
 	 *                field corresponding to this field)
 	 * @return this field's final string representation with prefix of prefix concatenated with i,
 	 *         concatenated with "AS [alias]" if this field has an alias
 	 */
 	public String toFullFinalString(NamedRelation intermediateRelation, String prefix, int i) {
 		return this.alias != null ?
 				String.format("%s AS %s", this.toFinalString(intermediateRelation, prefix, i), this.alias) :
 					this.toFinalString(intermediateRelation, prefix, i);
 	}
 }
