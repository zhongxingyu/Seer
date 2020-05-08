 package dfh.grammar;
 
 import java.util.Map;
 import java.util.regex.Pattern;
 
 /**
  * {@link Rule} defined over sequence of terminal objects rather than other
  * <code>Rules</code>.
  * <p>
  * <b>Creation date:</b> Feb 19, 2011
  * 
  * @author David Houghton
  */
 public class LeafRule extends Rule {
 	private class LeafMatcher implements Matcher {
 		private final Node parent;
 		private final int offset;
 		private final CharSequence chars;
 		private Map<Integer, Node> cache;
 
 		public LeafMatcher(CharSequence s, int offset, Node parent,
 				Map<Label, Map<Integer, Node>> cache) {
 			this.chars = s;
 			this.parent = parent;
 			this.offset = offset;
 			this.cache = cache.get(label);
 		}
 
 		@Override
 		public Node match() {
 			if (cache.containsKey(offset)) {
 				return cache.get(offset);
 			}
 			java.util.regex.Matcher m = p.matcher(chars);
 			m.region(offset, chars.length());
 			m.useTransparentBounds(true);
 			m.useAnchoringBounds(false);
 			Node n = null;
 			if (m.lookingAt())
				n = new Node(LeafRule.this, offset, m.end(), parent);
 			cache.put(offset, n);
 			return n;
 		}
 
 		@Override
 		public boolean mightHaveNext() {
 			return cache.containsKey(offset);
 		}
 
 		@Override
 		public String toString() {
 			return "M:" + LeafRule.this;
 		}
 
 		@Override
 		public Rule rule() {
 			return LeafRule.this;
 		}
 	}
 
 	private static final long serialVersionUID = 1L;
 	private final Pattern p;
 
 	public LeafRule(Label label, Pattern p) {
 		super(label);
 		this.p = p;
 	}
 
 	@Override
 	public Matcher matcher(CharSequence s, final int offset, Node parent,
 			Map<Label, Map<Integer, Node>> cache) {
 		return new LeafMatcher(s, offset, parent, cache);
 	}
 }
