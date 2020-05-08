 package dfh.grammar;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.Reader;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 /**
  * Base class that parses strings according to a given set of rules.
  * <p>
  * A {@link Grammar} is constructed from a set of rules such as
  * 
  * <pre>
 * &lt;ROOT&gt; = &lt;a> | &lt;b&gt;
  * 
 *    &lt;a&gt; = (foo) (s) (bar)
 *    &lt;b&gt; = (quux) (s) (baz)
 *  (bar) =bar
 *  (baz) =baz
 *  (foo) =foo
 * (quux) =quux
 *    (s) =\s++
  * </pre>
  * 
  * And in turn constructs {@link Matcher} objects that can be applied to a
  * {@link CharSequence} to produce a sequence of {@link Match} objects
  * representing how this pattern matches the characters. A {@link Grammar} has
  * several advantages over a {@link Pattern} and one chief disadvantage.
  * <p>
  * <h3>Advantages</h3>
  * <ul>
  * <li>Ease of composition, reading, maintenance, anddebugging. See
  * {@link #setTrace(PrintStream)} and {@link #describe()}.
  * <li>One can compose grammars from component {@link Rule rules}, regular
  * expressions, and other grammars. See {@link #defineTerminal(String, Pattern)}, {@link #defineTerminal(String, Rule)}.
  * <li>One can iterate over all possible ways of matching a {@link CharSequence}
  * , not just a non-overlapping subset.
  * <li>Given a {@link Match} one can find the precise way the pattern matches,
  * each rule and subrule that participated in the match and which offsets is
  * applied to.
  * </ul>
  * <h3>Disadvantages</h3>
  * I am certain that someone who didn't write these classes would find
  * infelicities to enumerate in the API. The chief disadvantage that I am aware
  * of is that matchign with a {@link Grammar} is about an order of magnitude
  * slower than matching with a simple {@link Pattern}. For one thing, fewer
  * people have spent less time optimizing the code. But even were Oracle to take
  * over this project the scope for efficiency is less simply because the task
  * attempted is greater.
  * <h2>Rule Definition</h2>
  * <h2>Acknowledgements</h2> This class and its supporting classes was inspired
  * by the recursive regular expressions available in Perl 5.10+, and especially
  * their sugared up form in <a
  * href="http://search.cpan.org/search?query=Regexp%3A%3AGrammars&mode=module"
  * target="_blank">Regexp::Grammars</a> and <a
  * href="http://en.wikipedia.org/wiki/Perl_6_rules" target="_blank">Perl 6
  * rules</a>.
  * <p>
  * <b>Creation date:</b> Feb 19, 2011
  * 
  * @author David Houghton
  */
 public class Grammar implements Serializable {
 	/**
 	 * A structure to hold matching options and define defaults.
 	 * <p>
 	 * <b>Creation date:</b> Mar 28, 2011
 	 * 
 	 * @author David Houghton
 	 * 
 	 */
 	public static class Options implements Cloneable {
 		/**
 		 * Clones options from a prototype. Use this instead of {@link #clone()}
 		 * and you don't have to cast.
 		 * 
 		 * @param opt
 		 *            prototype
 		 */
 		public Options(Options opt) {
 			this.allowOverlap = opt.allowOverlap;
 			this.study = opt.study;
 			this.startOffset = opt.startOffset;
 		}
 
 		/**
 		 * Gives you all the defaults.
 		 */
 		public Options() {
 		}
 
 		@Override
 		public Object clone() {
 			return new Options(this);
 		}
 
 		/**
 		 * Whether matches may overlap.
 		 */
 		public static final boolean ALLOW_OVERLAP = false;
 		/**
 		 * Whether to study the character sequence before matching to accelerate
 		 * the matching of terminal rules.
 		 */
 		public static final boolean STUDY = true;
 		/**
 		 * Character offset at which to begin matching.
 		 */
 		public static final int START_OFFSET = 0;
 		boolean allowOverlap = ALLOW_OVERLAP;
 		boolean study = STUDY;
 		int startOffset = START_OFFSET;
 
 		public boolean allowOverlap() {
 			return allowOverlap;
 		}
 
 		public void allowOverlap(boolean allowOverlap) {
 			this.allowOverlap = allowOverlap;
 		}
 
 		public boolean study() {
 			return study;
 		}
 
 		public void study(boolean study) {
 			this.study = study;
 		}
 
 		public int startOffset() {
 			return startOffset;
 		}
 
 		public void startOffset(int startOffset) {
 			if (startOffset < 0)
 				throw new GrammarException("text offsets must be positive");
 			this.startOffset = startOffset;
 		}
 	}
 
 	/**
 	 * Special debugging API.
 	 * <p>
 	 * <b>Creation date:</b> Mar 21, 2011
 	 * 
 	 * @author David Houghton
 	 * 
 	 */
 	private abstract class GrammarMatcher extends Matcher {
 
 		protected GrammarMatcher(CharSequence s, final Options options) {
 			super(s, options.startOffset, null);
 		}
 
 		protected abstract String name();
 
 		@Override
 		Rule rule() {
 			return null;
 		}
 	}
 
 	private class FindMatcher extends GrammarMatcher {
 		private int index;
 		private boolean firstMatch;
 		private Matcher m;
 		private Options options;
 		private LinkedList<Integer> startOffsets;
 		private Map<Label, Map<Integer, CachedMatch>> cache;
 		private Match next;
 
 		FindMatcher(CharSequence s, Options options,
 				LinkedList<Integer> startOffsets,
 				Map<Label, Map<Integer, CachedMatch>> cache) {
 			super(s, options);
 			this.options = options;
 			this.startOffsets = startOffsets;
 			this.cache = cache;
 			index = options.study && !startOffsets.isEmpty() ? startOffsets
 					.removeFirst() : options.startOffset;
 			firstMatch = true;
 			m = rules.get(root).matcher(s, index, cache, null);
 			next = fetchNext();
 		}
 
 		@Override
 		public synchronized Match match() {
 			if (mightHaveNext()) {
 				Match n = next;
 				next = fetchNext();
 				if (n != null)
 					n.establishParentage();
 				return n;
 			}
 			return null;
 		}
 
 		private Match fetchNext() {
 			if (options.study && index == -1)
 				return null;
 			boolean firstNull = true;
 			while (true) {
 				Match n;
 				if (firstMatch) {
 					n = m.match();
 					firstMatch = false;
 				} else if (firstNull && !options.allowOverlap)
 					n = null;
 				else
 					n = m.match();
 				if (n != null) {
 					if (!options.allowOverlap) {
 						if (options.study) {
 							index = -1;
 							while (!startOffsets.isEmpty()) {
 								index = startOffsets.removeFirst();
 								if (index >= n.end())
 									break;
 							}
 						} else
 							index = n.end();
 					}
 					return n;
 				}
 				if (!(firstNull && !options.allowOverlap)) {
 					if (options.study) {
 						if (startOffsets.isEmpty())
 							break;
 						index = startOffsets.removeFirst();
 					} else
 						index++;
 				}
 				firstNull = false;
 				if (index == s.length())
 					break;
 				m = rules.get(root).matcher(s, index, cache, this);
 			}
 			return null;
 		}
 
 		@Override
 		public boolean mightHaveNext() {
 			return next != null;
 		}
 
 		@Override
 		protected String name() {
 			return "find";
 		}
 	}
 
 	private static final long serialVersionUID = 1L;
 	private final Label root;
 	private final Map<Label, Rule> rules;
 	private final Map<String, Label> terminalLabelMap;
 	/**
 	 * Keeps track of terminals not defined in initial rule set.
 	 */
 	private final HashSet<Label> undefinedTerminals;
 	transient PrintStream trace;
 
 	/**
 	 * Delegates to {@link #Grammar(LineReader)}.
 	 * 
 	 * @param lines
 	 *            rule source
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Grammar(String[] lines) throws GrammarException, IOException {
 		this(new ArrayLineReader(lines));
 	}
 
 	/**
 	 * Delegates to {@link #Grammar(Reader)} and ultimately
 	 * {@link #Grammar(LineReader)}.
 	 * 
 	 * @param f
 	 *            rule source
 	 * @throws GrammarException
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	public Grammar(File f) throws GrammarException, FileNotFoundException,
 			IOException {
 		this(new FileReader(f));
 	}
 
 	/**
 	 * Delegates to {@link #Grammar(Reader)} and ultimately
 	 * {@link #Grammar(LineReader)}.
 	 * 
 	 * @param is
 	 *            rule source
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Grammar(InputStream is) throws GrammarException, IOException {
 		this(new InputStreamReader(is));
 	}
 
 	/**
 	 * Delegates to {@link #Grammar(BufferedReader)} and ultimately
 	 * {@link #Grammar(LineReader)}.
 	 * 
 	 * @param r
 	 *            rule source
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Grammar(Reader r) throws GrammarException, IOException {
 		this(new BufferedReader(r));
 	}
 
 	/**
 	 * Delegates to {@link #Grammar(BufferedReader)} and ultimately to
 	 * {@link #Grammar(LineReader)}.
 	 * 
 	 * @param r
 	 *            rule source
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Grammar(BufferedReader r) throws GrammarException, IOException {
 		this(new BufferedLineReader(r));
 	}
 
 	/**
 	 * Creates a {@link Compiler} to parse input and prepares a {@link Grammar}
 	 * to generate {@link Matcher} objects.
 	 * 
 	 * @param reader
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Grammar(LineReader reader) throws GrammarException, IOException {
 		Compiler c = new Compiler(reader);
 		root = c.root();
 		rules = c.rules();
 		for (Rule r : rules.values())
 			r.g = this;
 		terminalLabelMap = c.terminalLabelMap();
 		undefinedTerminals = c.undefinedTerminals();
 	}
 
 	/**
 	 * For defining a terminal rule left undefined by the initial rule set.
 	 * Useful when terminals are unwieldy regular expressions such as TRIEs.
 	 * 
 	 * @param label
 	 * @param p
 	 * @throws GrammarException
 	 */
 	public void defineTerminal(String label, Pattern p) throws GrammarException {
 		Label l = terminalLabelMap.get(label);
 		if (l == null)
 			throw new GrammarException("unknown terminal rule: " + label);
 		Rule r = rules.get(l);
 		if (!(r instanceof DeferredDefinitionRule))
 			throw new GrammarException("rule " + label + " already defined");
 		((DeferredDefinitionRule) r).setRegex(p);
 		undefinedTerminals.remove(r.label());
 	}
 
 	public Matcher matches(CharSequence s) throws GrammarException {
 		return matches(s, new Options());
 	}
 
 	/**
 	 * Checks to make sure all rules have been defined.
 	 * 
 	 * @throws GrammarException
 	 */
 	private void checkComplete() throws GrammarException {
 		if (!undefinedTerminals.isEmpty()) {
 			LinkedList<Label> list = new LinkedList<Label>(undefinedTerminals);
 			Collections.sort(list);
 			StringBuilder b = new StringBuilder(
 					"terminal rules remaining undefined: ");
 			b.append(list.pollFirst().id);
 			for (Label l : list) {
 				b.append(", ");
 				b.append(l.id);
 			}
 			throw new GrammarException(b.toString());
 		}
 	}
 
 	public Matcher lookingAt(CharSequence s) throws GrammarException {
 		return lookingAt(s, new Options());
 	}
 
 	public Matcher lookingAt(final CharSequence cs, Options opt)
 			throws GrammarException {
 		checkComplete();
 		final Options options = verifyOptions(cs, opt);
 		final Set<Integer> startOffsets = new HashSet<Integer>();
 		Map<Label, Map<Integer, CachedMatch>> cache = offsetCache();
 		if (options.study) {
 			Set<Rule> studiedRules = new HashSet<Rule>();
 			startOffsets.addAll(rules.get(root).study(cs, cache,
 					options.startOffset, studiedRules));
 		}
 		final Matcher m = rules.get(root).matcher(cs, options.startOffset,
 				cache, null);
 		abstract class LookingAtMatcher extends GrammarMatcher {
 			LookingAtMatcher() {
 				super(cs, options);
 			}
 
 			@Override
 			public String name() {
 				return "lookingAt";
 			}
 		}
 		// synchronization wrappers
 		return !options.allowOverlap ? new LookingAtMatcher() {
 			boolean matchedOnce = false;
 
 			@Override
 			public synchronized boolean mightHaveNext() {
 				if (options.study && startOffsets.isEmpty())
 					return false;
 				return matchedOnce ? false : m.mightHaveNext();
 			}
 
 			@Override
 			public synchronized Match match() {
 				Match n = null;
 				if (!(matchedOnce || options.study && startOffsets.isEmpty())) {
 					matchedOnce = true;
 					n = m.match();
 					if (n != null)
 						n.establishParentage();
 				}
 				return n;
 			}
 		} : new LookingAtMatcher() {
 
 			@Override
 			public synchronized boolean mightHaveNext() {
 				if (options.study && startOffsets.isEmpty())
 					return false;
 				return m.mightHaveNext();
 			}
 
 			@Override
 			public synchronized Match match() {
 				Match n = null;
 				if (!(options.study && startOffsets.isEmpty())) {
 					n = m.match();
 					if (n != null)
 						n.establishParentage();
 				}
 				return n;
 			}
 		};
 	}
 
 	public Matcher find(CharSequence s) throws GrammarException {
 		return find(s, new Options());
 	}
 
 	/**
 	 * Generates iterator over matches anywhere in sequence.
 	 * 
 	 * @param s
 	 *            sequence against which to match
 	 * @param opt
 	 *            matching parameters
 	 * @return {@link Matcher} object for iterating over matches
 	 * @throws GrammarException
 	 */
 	public Matcher find(final CharSequence s, Options opt)
 			throws GrammarException {
 		checkComplete();
 		final Options options = verifyOptions(s, opt);
 		final Map<Label, Map<Integer, CachedMatch>> cache = offsetCache();
 		final LinkedList<Integer> startOffsets = new LinkedList<Integer>();
 		if (options.study) {
 			ArrayList<Integer> list = new ArrayList<Integer>();
 			Set<Rule> studiedRules = new HashSet<Rule>();
 			list.addAll(rules.get(root).study(s, cache, options.startOffset,
 					studiedRules));
 			Collections.sort(list);
 			startOffsets.addAll(list);
 		}
 		return new FindMatcher(s, options, startOffsets, cache);
 	}
 
 	/**
 	 * Generates a cache to keep track of failing offsets for particular rules.
 	 * 
 	 * @return map from labels to sets of offsets where the associated rules are
 	 *         known not to match
 	 */
 	private Map<Label, Map<Integer, CachedMatch>> offsetCache() {
 		Map<Label, Map<Integer, CachedMatch>> offsetCache = new HashMap<Label, Map<Integer, CachedMatch>>(
 				rules.size());
 		for (Label l : rules.keySet()) {
 			// might use TreeMap to save a little memory
 			offsetCache.put(l, new HashMap<Integer, CachedMatch>());
 		}
 		return offsetCache;
 	}
 
 	/**
 	 * Setting this non-null turns on match debugging output.
 	 * 
 	 * @param trace
 	 *            sink for debugging trace
 	 */
 	public void setTrace(PrintStream trace) {
 		this.trace = trace;
 	}
 
 	/**
 	 * The canonical order, at the moment, is to sort them primarily by their
 	 * degree of independence from other rules and secondarily by their label.
 	 * The root rule comes first, separated by a blank line, then they descend
 	 * in order of abstraction and alphabet. Terminal rules are always last.
 	 * <p>
 	 * I may change the canonical order in the future. It has the advantage of
 	 * putting rules immediately above those rules they depend on most directly
 	 * but the disadvantage of making it hard to find an arbitrary rule.
 	 * 
 	 * @return canonical description of {@link Grammar}
 	 */
 	public String describe() {
 		List<Rule> ruleList = new ArrayList<Rule>(rules.values());
 		int maxLabel = -1;
 		for (Iterator<Rule> i = ruleList.iterator(); i.hasNext();) {
 			Rule r = i.next();
 			if (r.generation == -1) {
 				i.remove();
 				continue;
 			}
 			int l = r.label().toString().length();
 			if (l > maxLabel)
 				maxLabel = l;
 			if (r == rules.get(root))
 				i.remove();
 		}
 		String format = "%" + maxLabel + "s =";
 		// put rules in canonical order
 		Collections.sort(ruleList, new Comparator<Rule>() {
 			@Override
 			public int compare(Rule o1, Rule o2) {
 				int comparison = o2.generation - o1.generation;
 				if (comparison == 0)
 					comparison = o1.label().toString()
 							.compareTo(o2.label().toString());
 				return comparison;
 			}
 		});
 
 		StringBuilder b = new StringBuilder();
 		b.append(String.format(format, root));
 		b.append(' ');
 		b.append(rules.get(root).description());
 		b.append("\n\n");
 		for (Rule r : ruleList) {
 			b.append(String.format(format, r.label()));
 			b.append(' ');
 			b.append(r.description());
 			b.append("\n");
 		}
 		return b.toString();
 	}
 
 	/**
 	 * Assigns arbitrary rule to given label.
 	 * 
 	 * @param label
 	 * @param rule
 	 */
 	public void defineTerminal(String label, Rule rule) {
 		Label l = terminalLabelMap.get(label);
 		if (l == null)
 			throw new GrammarException("unknown terminal rule: " + label);
 		Rule r = rules.get(l);
 		if (!(r instanceof DeferredDefinitionRule))
 			throw new GrammarException("rule " + label + " already defined");
 		((DeferredDefinitionRule) r).setRule(rule);
 		undefinedTerminals.remove(r.label());
 	}
 
 	/**
 	 * Generates iterator over matches beginning on the first character of the
 	 * sequence (or at the offset specified in the {@link Options} object) and
 	 * ending on the last character.
 	 * 
 	 * @param s
 	 *            sequence against which to match
 	 * @param opt
 	 *            matching parameters
 	 * @return {@link Matcher} object with which one can iterate over matches
 	 * @throws GrammarException
 	 */
 	public Matcher matches(final CharSequence s, Options opt)
 			throws GrammarException {
 		checkComplete();
 		final Options options = verifyOptions(s, opt);
 		final Map<Label, Map<Integer, CachedMatch>> cache = offsetCache();
 		final Set<Integer> startOffsets = new HashSet<Integer>();
 		if (options.study) {
 			Set<Rule> studiedRules = new HashSet<Rule>();
 			startOffsets.addAll(rules.get(root).study(s, cache,
 					options.startOffset, studiedRules));
 		}
 		final Matcher m = rules.get(root).matcher(s, options.startOffset,
 				cache, null);
 		return new GrammarMatcher(s, options) {
 			boolean matchedOnce = false;
 			Match next = fetchNext();
 
 			@Override
 			public boolean mightHaveNext() {
 				if (options.study && startOffsets.isEmpty())
 					return false;
 				return !options.allowOverlap && matchedOnce || next != null;
 			}
 
 			private Match fetchNext() {
 				if (options.study && startOffsets.isEmpty())
 					return null;
 				Match n;
 				while ((n = m.match()) != null) {
 					if (n.end() == s.length())
 						return n;
 				}
 				return null;
 			}
 
 			@Override
 			public synchronized Match match() {
 				if (mightHaveNext()) {
 					Match n = next;
 					next = fetchNext();
 					matchedOnce = true;
 					if (n != null)
 						n.establishParentage();
 					return n;
 				}
 				return null;
 			}
 
 			@Override
 			protected String name() {
 				return "matches";
 			}
 		};
 	}
 
 	/**
 	 * Validates options and clones object to make it thread safe.
 	 * 
 	 * @param s
 	 * @param opt
 	 * @return clone of given options
 	 */
 	private Options verifyOptions(CharSequence s, Options opt) {
 		if (opt.startOffset() >= s.length())
 			throw new GrammarException(
 					"start offset specified beyond end of string");
 		return new Options(opt);
 	}
 }
