 /**
  * 
  */
 package eu.larkc.iris.rules.compiler;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.deri.iris.api.basics.IAtom;
 import org.deri.iris.api.basics.ILiteral;
 import org.deri.iris.api.basics.IPredicate;
 import org.deri.iris.api.builtins.IBuiltinAtom;
 import org.deri.iris.api.terms.IConstructedTerm;
 import org.deri.iris.api.terms.ITerm;
 import org.deri.iris.api.terms.IVariable;
 import org.deri.iris.api.terms.concrete.IIri;
 
 import cascading.pipe.Each;
 import cascading.pipe.Pipe;
 import cascading.pipe.assembly.Rename;
 import eu.larkc.iris.evaluation.ConstantFilter;
 import eu.larkc.iris.evaluation.PredicateFilter;
 import eu.larkc.iris.rules.compiler.RuleStreams.LiteralId;
 
 /**
  * @author valer
  *
  */
 public class LiteralFields extends eu.larkc.iris.rules.compiler.PipeFields {
 
 	/**
 	 * serialVersionUID
 	 */
 	private static final long serialVersionUID = 6351207734635496829L;
 
 	private static final String PREDICATE_PREFIX = "P";
 	private static final String VARIABLE_PREFIX = "V";
 	
 	private Pipe mainPipe;
 	private LiteralId id;
 	
 	public class TermId {
 		private LiteralId literalId;
 		private String prefix;
 		private int index = -1;
 
 		public TermId(LiteralId literalId, String prefix) {
 			this.literalId = literalId;
 			this.prefix = prefix;
 		}
 		
 		public TermId(LiteralId literalId, String prefix, int index) {
 			this(literalId, prefix);
 			this.index = index;
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			if (index == -1) {
 				return literalId + prefix;
 			} else {
 				return literalId + prefix + index;
 			}
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		@Override
 		public boolean equals(Object obj) {
 			if (!(obj instanceof TermId)) {
 				return false;
 			}
 			TermId termId = (TermId) obj;
 			return new EqualsBuilder().append(literalId, termId.literalId)
 				.append(prefix, termId.prefix).append(index, termId.index).isEquals();
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Object#hashCode()
 		 */
 		@Override
 		public int hashCode() {
 			return new HashCodeBuilder().append(literalId).append(prefix).append(index).hashCode();
 		}
 	}
 	
 	LiteralFields(Pipe mainPipe, LiteralId literalId, ILiteral literal) {
 		this.mainPipe = mainPipe;
 		this.id = literalId;
 		IAtom atom = literal.getAtom();
 		add(new StreamItem(new TermId(literalId, PREDICATE_PREFIX), atom.getPredicate()));
 		for (int i = 0; i < atom.getTuple().size(); i++) {
 			ITerm term = atom.getTuple().get(i);
 			if (term instanceof IVariable) {
				add(new StreamItem(new TermId(literalId, VARIABLE_PREFIX, i), (IVariable) term));
 			}
 		}
 	}
 	
 	public LiteralId getId() {
 		return id;
 	}
 		
 	public boolean fromBuiltInAtom() {
 		return false;
 	}
 	
 	/**
 	 * Converts a built-in atom to a suitable cascading operation per the RIF
 	 * specs. This is currently a stub implementation.
 	 * 
 	 * @param atom
 	 */
 	protected void processBuiltin(IBuiltinAtom atom) {
 
 		boolean constructedTerms = false;
 		for (ITerm term : atom.getTuple()) {
 			if (term instanceof IConstructedTerm) {
 				constructedTerms = true;
 				break;
 			}
 		}
 		// TODO fisf: construct appropriate cascading operation
 		// once they are supported
 		if (constructedTerms) {
 			// function symbol
 			throw new NotImplementedException(
 					"Function Symbols are not supported");
 		} else {
 			// ordinary built-in, those WILL be handled
 			throw new NotImplementedException("Builtins not implemented yet");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.larkc.iris.rules.compiler.PipeFields#getPipe()
 	 */
 	@Override
 	public Pipe getPipe() {
 		if (pipe != null) {
 			return pipe;
 		}
 		
 		pipe = new Pipe(getId().toString(), mainPipe);
 		pipe = new Rename(pipe, new cascading.tuple.Fields(0, 1, 2), getFields());
 		IPredicate predicateValue = getPredicate();
 		if (predicateValue != null) {
 			pipe = new Each(pipe, getFields(), new PredicateFilter(predicateValue));
 		}
 		
 		pipe = filterConstants(pipe);
 
 		return pipe;
 	}
 
 	private IPredicate getPredicate() {
 		for (Field field : this) {
 			if (field.source instanceof IPredicate) {
 				return (IPredicate) field.getSource();
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * This filters constants by providing in tuple streams according to the
 	 * original rule defintion.
 	 * 
 	 * @param attachTo
 	 * @param tuple
 	 * @return
 	 */
 	protected Pipe filterConstants(Pipe attachTo) {
 
 		Map<String, Object> constantTerms = new HashMap<String, Object>();
 
 		for (Field field : this) {
 			if (!(field.getSource() instanceof ITerm)) {
 				continue;
 			}
 			ITerm term = (ITerm) field.getSource();
 
 			// not a variable, we filter the tuples
 			if (term.isGround()) {
 				if (term instanceof IIri) {
 					constantTerms.put(field.getName(), (IIri) term); //added one because of the predicate field
 				} else {
 					constantTerms.put(field.getName(), term.getValue()); //added one because of the predicate field
 				}
 					
 			}
 		}
 
 		// did we actually find at least one constant?
 		if (!constantTerms.isEmpty()) {
 			Pipe filter = new Each(attachTo, new ConstantFilter(constantTerms));
 			return filter;
 		}
 
 		// nothing changed
 		return attachTo;
 	}
 
 }
