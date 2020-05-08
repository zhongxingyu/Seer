 package eu.larkc.iris.rules.compiler;
 
 import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.terms.IStringTerm;
 import org.deri.iris.api.terms.IVariable;
import org.deri.iris.api.terms.concrete.IIri;
 
 import eu.larkc.iris.rules.compiler.LiteralFields.TermId;
 
 public class StreamItem extends Field {
 
 	TermId id;
 	
 	public StreamItem(TermId termId, Comparable source) {
 		super(source);
 		this.id = termId;
 		
 	}
 	
 	public String getValue() {
 		if (source instanceof IPredicate) {
 			return ((IPredicate) source).getPredicateSymbol();
 		} else if (source instanceof IVariable) {
 			return ((IVariable) source).getValue();
		} else if (source instanceof IIri) {
			return ((IIri) source).getValue();
		} else if (source instanceof IStringTerm) {
			return ((IStringTerm) source).getValue();
 		}
 		return super.getValue();
 	}
 	
 	/* (non-Javadoc)
 	 * @see eu.larkc.iris.rules.compiler.Field#getName()
 	 */
 	@Override
 	public String getName() {
 		return id.toString();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return id.toString();
 	}
 
 }
