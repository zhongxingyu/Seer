 package ch.epfl.ad.db.parsing;
 
 public abstract class Field implements Operand {
 	
 	protected String alias;
 	
 	public abstract Field setAlias(String alias);
 	public abstract boolean isAggregate();
 	
 	public String getAlias() {
 		return this.alias;
 	}
 	
 	public abstract String toString(QueryType type);
 	
 	@Override
 	public String toString() {
 		return this.toString(QueryType.REGULAR);
 	}
 	
 	public String toAliasedString(QueryType type) {
 		return this.alias != null ? this.alias : this.toString(type);
 	}
 	
 	public String toFullString(QueryType type) {
		return this.alias != null ? String.format("%s AS %s", this.toString(type), this.alias) : this.toString(type);
 	}
 }
