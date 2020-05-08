 package dfh.grammar;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import dfh.grammar.Label.Type;
 
 /**
  * Node in an AST tree.
  * <p>
  * <b>Creation date:</b> Feb 19, 2011
  * 
  * @author David Houghton
  */
 public class Match {
 	private final Rule r;
 	private final int start;
	private int end = -1;
 	private Match parent;
 	private Match[] children;
 	/**
 	 * Used as a placeholder in offset cache.
 	 */
 	public static final Match dummy = new Match();
 
 	/**
 	 * Makes a dummy node. This serves as the opposite of <code>null</code> in
 	 * matching caches.
 	 */
 	private Match() {
 		r = null;
 		start = end = -1;
 		parent = this;
 	}
 
	public Match(Rule r, int start) {
 		this.r = r;
 		this.start = start;
 	}
 
	public Match(Rule r, int start, int end) {
 		this(r, start);
 		setEnd(end);
 	}
 
 	public Rule rule() {
 		return r;
 	}
 
 	public int start() {
 		return start;
 	}
 
 	public int end() {
 		return end;
 	}
 
	public void setEnd(int end) {
		if (this.end > -1)
			throw new GrammarException("match end cannot be reset");
 		this.end = end;
 	}
 
 	public Match parent() {
 		return parent;
 	}
 
 	/**
 	 * Recursively defines link from child to parent.
 	 */
 	void establishParentage() {
 		if (children == null)
 			children = new Match[0];
 		else {
 			for (Match child : children) {
 				if (child.parent != null)
 					return;
 				else {
 					child.parent = this;
 					child.establishParentage();
 				}
 			}
 		}
 	}
 
	public void setChildren(Match[] children) {
		if (this.children != null)
			throw new GrammarException("match children cannot be reset");
 		this.children = children;
 	}
 
 	public Match[] children() {
 		return children;
 	}
 
 	@Override
 	public String toString() {
 		if (this == dummy)
 			return "[DUMMY]";
 		StringBuilder b = new StringBuilder();
 		b.append('[').append(r.label).append(' ');
 		b.append(start).append(", ").append(end);
 		if (children != null && children.length > 0) {
 			b.append(' ');
 			boolean nonInitial = false;
 			for (Match n : children) {
 				if (nonInitial)
 					b.append(", ");
 				else
 					nonInitial = true;
 				b.append(n);
 			}
 		}
 		b.append(']');
 		return b.toString();
 	}
 
 	/*
 	 * some tests
 	 */
 
 	/**
 	 * @return whether this {@link Match} has no children
 	 */
 	public boolean isTerminal() {
 		return children == null || children.length == 0;
 	}
 
 	/**
 	 * @return whether this {@link Match} has no parent
 	 */
 	public boolean isRoot() {
 		return parent == null;
 	}
 
 	/**
 	 * Returns whether the {@link Rule} that generated this {@link Match} has
 	 * the given name. Note, rules of different types may have the same name.
 	 * For example, one may have both rules <code>&lt;a&gt;</code> and
 	 * <code>(a)</code>.
 	 * 
 	 * @param name
 	 *            a {@link Label#id}
 	 * @return whether the {@link Rule} that generated this {@link Match} has
 	 *         the given id
 	 */
 	public boolean hasLabel(String name) {
 		return r.label().id.equals(name);
 	}
 
 	/**
 	 * Returns whether the {@link Rule} that generated this {@link Match} has
 	 * the given name and type.
 	 * 
 	 * @param name
 	 *            a {@link Label#id}
 	 * @param t
 	 *            a {@link Type}
 	 * @return whether the {@link Rule} that generated this {@link Match} has
 	 *         the given name and type
 	 */
 	public boolean hasLabel(String name, Type t) {
 		return hasLabel(name) && r.label().t == t;
 	}
 
 	/**
 	 * @param l
 	 * @return whether the {@link Rule} that generated this {@link Match} has
 	 *         the given label
 	 */
 	public boolean hasLabel(Label l) {
 		return r.label().equals(l);
 	}
 
 	/**
 	 * Returns list of matches dominated by this that pass the given test. The
 	 * search is depth first. Children are returned before parents.
 	 * 
 	 * @param t
 	 *            test
 	 * @return list of matches dominated by this that pass the given test
 	 */
 	public LinkedList<Match> passes(MatchTest t) {
 		LinkedList<Match> list = new LinkedList<Match>();
 		passes(t, list);
 		return list;
 	}
 
 	private void passes(MatchTest t, List<Match> accumulator) {
 		if (children != null) {
 			for (Match m : children)
 				m.passes(t, accumulator);
 		}
 		if (t.test(this))
 			accumulator.add(this);
 	}
 
 	/**
 	 * Returns whether {@link Match} corresponds to a region with no width.
 	 * <code>(?=fred)</code> and <code>'fred'*</code> can both produce
 	 * zero-width matches.
 	 * 
 	 * @return whether {@link Match} corresponds to a region with no width
 	 */
 	public boolean zeroWidth() {
 		return start == end;
 	}
 
 	/**
 	 * Returns whether this {@link Match} corresponds to a named {@link Rule},
 	 * as opposed to an implicit rule. For example, in
 	 * 
 	 * <pre>
 	 * &lt;ROOT&gt; = &lt;fred&gt;++
 	 * &lt;fred&gt; = 'fred' | 'Fred'
 	 * </pre>
 	 * 
 	 * <code>&lt;ROOT&gt;</code> and <code>&lt;fred&gt;</code> are explicit
 	 * while <code>'fred'</code>, <code>&lt;fred&gt;++</code> and so forth are
 	 * implicit.
 	 * <p>
 	 * Any rule defined on its own line with its own label to the left of the
 	 * equals sign is an explicit rule.
 	 * 
 	 * @return whether this {@link Match} corresponds to a named {@link Rule}
 	 */
 	public boolean explicit() {
 		return r.generation > -1;
 	}
 }
