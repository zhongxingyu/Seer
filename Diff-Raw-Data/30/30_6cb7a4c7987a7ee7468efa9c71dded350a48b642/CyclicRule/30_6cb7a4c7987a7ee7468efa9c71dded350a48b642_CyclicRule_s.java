 package dfh.grammar;
 
 import java.util.Map;
 
 /**
  * A rule that depends on some other or others in a cycle of dependence. These
  * rules must be defined in two steps.
  * <p>
  * <b>Creation date:</b> Mar 25, 2011
  * 
  * @author David Houghton
  * 
  */
 public class CyclicRule extends Rule {
 	private static final long serialVersionUID = 1L;
 	private Rule r;
 
 	public CyclicRule(Label label) {
 		super(label);
 	}
 
 	@Override
 	public Matcher matcher(CharSequence cs, Integer offset, Match parent,
 			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
 		return r.matcher(cs, offset, parent, cache, master);
 	}
 
 	@Override
 	protected String uniqueId() {
 		return label.toString();
 	}
 
 	public void setRule(Rule r) {
 		if (this.r != null)
 			throw new GrammarException("rule " + label + "already defined");
 		this.r = r;
 	}
 
 	@Override
 	public String description() {
		return r.description();
 	}
 }
