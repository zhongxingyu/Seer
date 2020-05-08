 /*
  * dfh.grammar -- a recursive descent parser library for Java
  * 
  * Copyright (C) 2012 David F. Houghton
  * 
  * This software is licensed under the LGPL. Please see accompanying NOTICE file
  * and lgpl.txt.
  */
 package dfh.grammar;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.regex.Pattern;
 
 import dfh.grammar.Label.Type;
 
 /**
  * Base class that parses strings according to a given set of rules.
  * <p>
  * A {@link Grammar} is constructed from a set of rules such as
  * 
  * <pre>
  * &lt;ROOT&gt; = &lt;a&gt; | &lt;b&gt;
  * 
  *    &lt;a&gt; = &lt;foo&gt; &lt;s&gt; &lt;bar&gt;
  *    &lt;b&gt; = &lt;quux&gt; &lt;s&gt; &lt;baz&gt;
  *  &lt;bar&gt; = /bar/
  *  &lt;baz&gt; = /baz/
  *  &lt;foo&gt; = /foo/
  * &lt;quux&gt; = /quux/
  *    &lt;s&gt; = /\s++/
  * </pre>
  * 
  * And in turn constructs {@link Matcher} objects that can be applied to a
  * {@link CharSequence} to produce a sequence of {@link Match} objects
  * representing how this pattern matches the characters. A {@link Grammar} has
  * several advantages over a {@link Pattern} and one chief disadvantage.
  * <p>
  * <h3>Advantages</h3>
  * <ul>
  * <li>Ease of composition, reading, maintenance, and debugging. See
  * {@link Options#log(java.io.PrintStream)},
  * {@link Options#keepRightmost(boolean)}, and {@link #describe()}.
  * <li>One can compose grammars from component {@link Rule rules}, regular
  * expressions, and other grammars. See {@link #defineRule(String, Pattern)},
  * {@link #defineRule(String, Rule)}.
  * <li>One can iterate over all possible ways of matching a {@link CharSequence}
  * , not just a non-overlapping subset.
  * <li>Given a {@link Match} one can find the precise way the pattern matches,
  * each rule and subrule that participated in the match and which offsets it
  * applied to.
  * </ul>
  * <h3>Disadvantages</h3>
  * I am certain that someone who hadn't written these classes would find
  * infelicities to enumerate in the API. The chief disadvantage that I am aware
  * of is that matching with a {@link Grammar} is considerably slower than
  * matching with a simple {@link Pattern}. For one thing, fewer people have
  * spent time optimizing the code. But even were Oracle to take over this
  * project the scope for efficiency is less simply because the task attempted is
  * greater.
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
 	 * Special debugging API.
 	 * <p>
 	 * <b>Creation date:</b> Mar 21, 2011
 	 * 
 	 * @author David Houghton
 	 * 
 	 */
 	private abstract class GrammarMatcher extends Matcher {
 
 		protected GrammarMatcher(GlobalState options) {
 			super(options.start, null, options);
 		}
 
 		protected abstract String name();
 
 		@Override
 		protected Rule rule() {
 			return null;
 		}
 
 		/**
 		 * Callback that fixes up returned nodes before returning them.
 		 * 
 		 * @param n
 		 *            possibly good node
 		 * @param m
 		 *            {@link Matcher} that produced the possibly good node
 		 * @return possibly good node
 		 */
 		protected Match maybeGood(Match n, Matcher m) {
 			if (n == null)
 				return bad(m);
 			n.done(s);
 			return n;
 		}
 
 		/**
 		 * Callback that fixes up rightmost match, if any, before returning
 		 * <code>null</code> when grammar fails to match.
 		 * 
 		 * @param m
 		 *            {@link Matcher} that failed to match
 		 * @return null
 		 */
 		protected Match bad(Matcher m) {
 			if (options.keepRightmost && m.rightmost != null)
 				m.rightmost.done(s);
 			return null;
 		}
 	}
 
 	/**
 	 * {@link Matcher} wrapper to implement LTM.
 	 * <p>
 	 * <b>Creation date:</b> Apr 13, 2011
 	 * 
 	 * @author David Houghton
 	 * 
 	 */
 	private static class LTMMatcher {
 		private final LinkedList<Match> matches = new LinkedList<Match>();
 
 		LTMMatcher(Matcher m) {
 			int max = -1;
 			try {
 				Match n;
 				while ((n = m.match()) != null) {
 					int w = n.end() - n.start();
 					if (w > max) {
 						matches.clear();
 						max = w;
 					}
 					if (w == max)
 						matches.add(n);
 				}
 			} catch (DoubleColonBarrier e) {
 			}
 		}
 
 		Match match() {
 			if (hasNext())
 				return matches.removeFirst();
 			return null;
 		}
 
 		boolean hasNext() {
 			return !matches.isEmpty();
 		}
 	}
 
 	/**
 	 * For starting the chain of inheritance (in terms of handing down from
 	 * parent to child, not OO) of the rule state map.
 	 * <p>
 	 * <b>Creation date:</b> Mar 30, 2011
 	 * 
 	 * @author David Houghton
 	 * 
 	 */
 	private class DummyMatcher extends Matcher {
 		DummyMatcher(GlobalState options) {
 			super(options.start, null, options);
 		}
 
 		@Override
 		public Match match() {
 			return null;
 		}
 
 		@Override
 		protected boolean mightHaveNext() {
 			return false;
 		}
 
 		@Override
 		protected Rule rule() {
 			return null;
 		}
 
 	}
 
 	/**
 	 * {@link Matcher} for {@link Grammar#find(CharSequence, Options)}.
 	 * 
 	 * @author David Houghton
 	 * 
 	 */
 	private class FindMatcher extends GrammarMatcher {
 		private int index;
 		private boolean firstMatch;
 		private Matcher m;
 		private LTMMatcher ltmm;
 		private LinkedList<Integer> startOffsets;
 		private Map<Integer, CachedMatch>[] cache;
 		private Match next;
 		private final boolean ltm;
 
 		FindMatcher(LinkedList<Integer> startOffsets,
 				Map<Integer, CachedMatch>[] cache, GlobalState options,
 				boolean ltm) {
 			super(options);
 			this.startOffsets = startOffsets;
 			this.cache = cache;
 			this.ltm = ltm;
 			index = options.study && !startOffsets.isEmpty() ? startOffsets
 					.removeFirst() : options.start;
 			firstMatch = true;
 			m = root.matcher(index, cache, this);
 			if (ltm)
 				ltmm = new LTMMatcher(m);
 			next = fetchNext();
 		}
 
 		@Override
 		public synchronized Match match() {
 			if (mightHaveNext()) {
 				Match n = next;
 				next = fetchNext();
 				return maybeGood(n, m);
 			}
 			return bad(m);
 		}
 
 		@Override
 		public Match rightmostMatch() {
 			return m.rightmost;
 		}
 
 		private Match fetchNext() {
 			if (options.study && index == -1)
 				return null;
 			boolean firstNull = true;
 			while (true) {
 				Match n = null;
 				if (firstMatch) {
 					try {
 						n = ltm ? ltmm.match() : m.match();
 						firstMatch = false;
 					} catch (DoubleColonBarrier e) {
 					}
 				} else if (firstNull && !options.allowOverlap)
 					n = null;
 				else {
 					try {
 						n = ltm ? ltmm.match() : m.match();
 					} catch (DoubleColonBarrier e) {
 					}
 				}
 				if (n != null) {
 					if (!options.allowOverlap) {
 						if (options.study) {
 							index = -1;
 							while (!startOffsets.isEmpty()) {
 								index = startOffsets.removeFirst();
 								if (index >= n.end())
 									break;
 								else
 									index = -1;
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
 				if (index >= options.end())
 					break;
 				m = root.matcher(index, cache, this);
 				if (ltm)
 					ltmm = new LTMMatcher(m);
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
 
 	private static final long serialVersionUID = 6L;
 	/**
 	 * {@link Label} of root {@link Rule}.
 	 */
 	protected Label rootLabel;
 	/**
 	 * The root {@link Rule}.
 	 */
 	protected Rule root;
 	/**
 	 * Collection of labels for rules.
 	 */
 	protected final Map<String, Label> terminalLabelMap;
 	/**
 	 * Keeps track of terminals not defined in initial rule set.
 	 */
 	protected final HashSet<Label> undefinedRules;
 	protected final Map<String, Set<Label>> undefinedConditions;
 	private final Map<String, Set<Rule>> knownConditions;
 	private final Map<String, Condition> conditionMap = new HashMap<String, Condition>();
 	/**
 	 * Whether the grammar's rules contain alternation.
 	 */
 	private boolean containsAlternation = false;
 	/**
 	 * Whether the grammar has been validated and may be matched against
 	 * character sequences.
 	 */
 	private boolean validated = false;
 	private Set<Rule> ruleSet;
 	/**
 	 * The set of rules that can begin a match.
 	 */
 	private Set<String> initialRules;
 	/**
 	 * Whether the grammar contains any lookbehinds.
 	 */
 	private boolean containsReversal = false;
 	/**
 	 * Collection of terminal rules to be used in studying.
 	 */
 	private HashSet<String> terminalRules = null;
 
 	/**
 	 * Delegates to {@link #Grammar(String[], Map)}, setting the second
 	 * parameter to <code>null</code>.
 	 * 
 	 * @param lines
 	 *            rule source
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Grammar(String[] lines) throws GrammarException {
 		this(lines, null);
 	}
 
 	/**
 	 * Delegates to {@link #Grammar(LineReader, Map)}.
 	 * 
 	 * @param lines
 	 *            rule source
 	 * @param precompiledRules
 	 *            definitions for {@link Rule} symbols
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Grammar(String[] lines, Map<String, Rule> precompiledRules)
 			throws GrammarException {
 		this(new ArrayLineReader(lines), precompiledRules);
 	}
 
 	/**
 	 * Delegates to {@link #Grammar(File, Map)}, setting the second parameter to
 	 * <code>null</code>.
 	 * 
 	 * @param f
 	 *            rule source
 	 * @throws GrammarException
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	public Grammar(File f) throws GrammarException, FileNotFoundException {
 		this(f, null);
 	}
 
 	/**
 	 * Delegates to {@link #Grammar(Reader, Map)}.
 	 * 
 	 * @param f
 	 *            rule source
 	 * @param precompiledRules
 	 *            definitions for {@link Rule} symbols
 	 * @throws GrammarException
 	 * @throws FileNotFoundException
 	 * @throws IOException
 	 */
 	public Grammar(File f, Map<String, Rule> precompiledRules)
 			throws GrammarException, FileNotFoundException {
 		this(new FileReader(f), precompiledRules);
 	}
 
 	/**
 	 * Delegates to {@link #Grammar(InputStream, Map)}, setting the second
 	 * parameter to <code>null</code>.
 	 * 
 	 * @param is
 	 *            rule source
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Grammar(InputStream is) throws GrammarException, IOException {
 		this(is, null);
 	}
 
 	/**
 	 * Delegates to {@link #Grammar(Reader, Map)}.
 	 * 
 	 * @param is
 	 *            rule source
 	 * @param precompiledRules
 	 *            definitions for {@link Rule} symbols
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Grammar(InputStream is, Map<String, Rule> precompiledRules)
 			throws GrammarException, IOException {
 		this(new InputStreamReader(is), precompiledRules);
 	}
 
 	/**
 	 * Delegates to {@link #Grammar(Reader, Map)}, setting the second parameter
 	 * to <code>null</code>.
 	 * 
 	 * @param r
 	 *            rule source
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Grammar(Reader r) throws GrammarException, IOException {
 		this(r, null);
 	}
 
 	/**
 	 * Delegates to {@link #Grammar(BufferedReader, Map)}.
 	 * 
 	 * @param r
 	 *            rule source
 	 * @param precompiledRules
 	 *            definitions for {@link Rule} symbols
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Grammar(Reader r, Map<String, Rule> precompiledRules)
 			throws GrammarException {
 		this(new BufferedReader(r), precompiledRules);
 	}
 
 	/**
 	 * Delegates to {@link #Grammar(BufferedReader, Map)}, setting the second
 	 * parameter to <code>null</code>.
 	 * 
 	 * @param r
 	 *            rule source
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Grammar(BufferedReader r) throws GrammarException {
 		this(r, null);
 	}
 
 	/**
 	 * Delegates to {@link #Grammar(LineReader, Map)}.
 	 * 
 	 * @param r
 	 *            rule source
 	 * @param precompiledRules
 	 *            definitions for {@link Rule} symbols
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Grammar(BufferedReader r, Map<String, Rule> precompiledRules)
 			throws GrammarException {
 		this(new BufferedLineReader(r), precompiledRules);
 	}
 
 	/**
 	 * Delegates to {@link #Grammar(LineReader, Map)}, setting the second
 	 * parameter to <code>null</code>.
 	 * 
 	 * @param reader
 	 *            rule source
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Grammar(LineReader reader) throws GrammarException, IOException {
 		this(reader, null);
 	}
 
 	/**
 	 * Creates a {@link Compiler} to parse the rules, intializing a
 	 * {@link Grammar} with which to generate {@link Matcher} objects. The
 	 * optional <code>precompiledRules</code> parameter contains implementations
 	 * of what would other wise be {@link DeferredDefinitionRule} objects --
 	 * rules whose body is not specified in the grammar. It is necessary to
 	 * provide this parameter if you wish to use your own {@link Reversible}
 	 * {@link Rule} in a backwards {@link Assertion}, as rules defined by
 	 * {@link #defineRule(String, Rule)} cannot be used in backwards assertions
 	 * -- assertions equivalent to <code>(?&lt;=...)</code> and
 	 * <code>(?&lt;!...)</code> in ordinary Java regular expressions.
 	 * <p>
 	 * <em>NOTE:</em> precompiled rules must implement {@link Cloneable}. They
 	 * will be cloned. This protects state variables such as
 	 * {@link Rule#cacheIndex} from being munged if the rules are reused in
 	 * multiple grammars.
 	 * 
 	 * @param reader
 	 *            rule source
 	 * 
 	 * @param precompiledRules
 	 *            definitions for {@link Rule} symbols
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Grammar(LineReader reader, Map<String, Rule> precompiledRules)
 			throws GrammarException {
 		Compiler c = new Compiler(reader, precompiledRules);
 		rootLabel = c.root();
 		root = c.rules().get(rootLabel);
 		terminalLabelMap = c.terminalLabelMap();
 		undefinedRules = c.undefinedTerminals();
 		undefinedConditions = c.undefinedConditions();
 		knownConditions = new HashMap<String, Set<Rule>>(
 				undefinedConditions.size() * 2);
 	}
 
 	/**
 	 * For when the entire grammar is defined in a single string.
 	 * <p>
 	 * Delegates to {@link #Grammar(LineReader, Map)}.
 	 * 
 	 * @param multiline
 	 *            rule source
 	 * @param precompiledRules
 	 *            definitions for {@link Rule} symbols
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Grammar(String multiline, Map<String, Rule> precompiledRules)
 			throws GrammarException {
 		this(new StringLineReader(multiline), precompiledRules);
 	}
 
 	/**
 	 * Delegates to {@link #Grammar(String, Map)}, setting the second parameter
 	 * to <code>null</code>.
 	 * 
 	 * @param multiline
 	 *            rule source
 	 * @throws GrammarException
 	 * @throws IOException
 	 */
 	public Grammar(String multiline) throws GrammarException {
 		this(multiline, null);
 	}
 
 	/**
 	 * For defining a terminal rule left undefined by the initial rule set. In
 	 * this case you are defining the rule to be a {@link LeafRule}. This is
 	 * useful when the regular expression is too unwieldy to fit in the grammar
 	 * file or when you generate it in the program.
 	 * 
 	 * @param label
 	 * @param p
 	 * @param id
 	 *            unique identifier for condition, if any
 	 * @param c
 	 *            optional {@link Condition}
 	 * @throws GrammarException
 	 */
 	public synchronized void defineRule(String label, Pattern p, String id,
 			Condition c) throws GrammarException {
 		DeferredDefinitionRule r = checkRuleDefinition(label);
 		String oldId = r.uniqueId();
 		Label l = new Label(Type.explicit, label);
 		LeafRule lr = new LeafRule(l, p, false);
 		if (c != null) {
 			lr = (LeafRule) conditionCheck(id, c, lr);
 		}
 		redefinitionCheck(r, lr);
 		fixAlternationTags(oldId, r);
 	}
 
 	/**
 	 * Makes sure we keep things which should be unique unique.
 	 * 
 	 * @param id
 	 * @param c
 	 * @param lr
 	 * @return
 	 */
 	private Rule conditionCheck(String id, Condition c, Rule lr) {
 		if (c == null) {
 			if (id != null)
 				throw new GrammarException(
 						"null condition given for non-null condition identifier "
 								+ id);
 			return lr;
 		} else {
 			Condition other = conditionMap.get(id);
 			if (other == null)
 				conditionMap.put(id, c);
 			else if (!other.equals(c))
 				throw new GrammarException("condition identifier " + id
 						+ " already in use");
 			Set<Rule> set = knownConditions.get(id);
 			if (!(set == null || set.isEmpty()))
 				throw new GrammarException("condition identifier " + id
 						+ " already in use");
 			c.setName(id);
 			if (set == null) {
 				set = new HashSet<Rule>();
 				knownConditions.put(id, set);
 			} else if (set.contains(lr)) {
 				return lr;
 			}
 			// remove lr from any set already containing it
 			for (Set<Rule> s : knownConditions.values())
 				s.remove(lr);
 			lr = lr.conditionalize(c, id);
 			set.add(lr);
 			return lr;
 		}
 	}
 
 	/**
 	 * @param r
 	 * @param lr
 	 * @return whether rule is novel
 	 */
 	private boolean redefinitionCheck(DeferredDefinitionRule r, Rule lr) {
 		Label l;
 		Map<Label, Rule> erules = explicitRules();
 		Map<String, Label> idMap = new HashMap<String, Label>(erules.size() * 2);
 		for (Entry<Label, Rule> e : erules.entrySet())
 			idMap.put(e.getValue().uniqueId(), e.getKey());
 		String id = lr.uniqueId();
 		l = idMap.get(id);
 		if (l == null) {
 			r.setRule(lr);
 		} else
 			r.setRule(erules.get(idMap.get(id)));
 		undefinedRules.remove(r.label());
 		return l == null;
 	}
 
 	private Map<Label, Rule> explicitRules() {
 		Set<Rule> set = new HashSet<Rule>();
 		root.subRules(set, new HashSet<Rule>(), false);
 		Map<Label, Rule> explicitRules = new HashMap<Label, Rule>(
 				set.size() * 2);
 		for (Rule r : set) {
 			if (r.generation > 0 || r instanceof DeferredDefinitionRule)
 				explicitRules.put(r.label(), r);
 		}
 		return explicitRules;
 	}
 
 	private DeferredDefinitionRule checkRuleDefinition(String label) {
 		Label l = terminalLabelMap.get(label);
 		if (l == null)
 			throw new GrammarException("unknown terminal rule: " + label);
 		Rule r = explicitRules().get(l);
 		if (!(r instanceof DeferredDefinitionRule))
 			throw new GrammarException("rule " + label + " already defined");
 		return (DeferredDefinitionRule) r;
 	}
 
 	/**
 	 * Generates iterator over matches beginning on the first character of the
 	 * sequence (or at the offset specified in the {@link Options} object) and
 	 * ending on the last character.
 	 * 
 	 * {@link Options#study()} is set to <code>false</code>. If different
 	 * behavior is desired, use {@link #matches(CharSequence, Options)}.
 	 * 
 	 * @param s
 	 * @return {@link Matcher} iterating over matches
 	 * @throws GrammarException
 	 */
 	public Matcher matches(CharSequence s) throws GrammarException {
 		return matches(s, new Options().study(false));
 	}
 
 	/**
 	 * Checks to make sure all rules have been defined. If they have, it checks
 	 * for alternation.
 	 * 
 	 * @throws GrammarException
 	 */
 	private synchronized void checkComplete() throws GrammarException {
 		if (validated)
 			return;
 		if (!undefinedRules.isEmpty()) {
 			LinkedList<Label> list = new LinkedList<Label>(undefinedRules);
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
 		if (!undefinedConditions.isEmpty()) {
 			LinkedList<String> list = new LinkedList<String>();
 			list.addAll(undefinedConditions.keySet());
 			Collections.sort(list);
 			StringBuilder b = new StringBuilder(
 					"conditions remaining undefined: ");
 			b.append(list.pollFirst());
 			for (String s : list) {
 				b.append(", ");
 				b.append(s);
 			}
 			throw new GrammarException(b.toString());
 		}
 		for (Rule r : rules()) {
 			r.setUid();
 			if (r.isReversed())
 				containsReversal = true;
 			if (r instanceof AlternationRule)
 				containsAlternation = true;
 		}
 		// fix tag maps in alternations
 		root.fixAlternation();
 		// create actual offset cache
 		root.setCacheIndex(new HashMap<String, Integer>());
 
 		validated = true;
 	}
 
 	/**
 	 * Generates iterator over matches whose start offset is the beginning of
 	 * the given {@link CharSequence}.
 	 * 
 	 * {@link Options#study()} is set to <code>false</code>. If different
 	 * behavior is desired, use {@link #lookingAt(CharSequence, Options)}.
 	 * 
 	 * @param s
 	 * @return iterator over matches
 	 * @throws GrammarException
 	 */
 	public Matcher lookingAt(CharSequence s) throws GrammarException {
 		return lookingAt(s, new Options().study(false));
 	}
 
 	/**
 	 * Generates iterator over matches whose start offset is the beginning of
 	 * the given {@link CharSequence}.
 	 * 
 	 * @param cs
 	 * @param opt
 	 *            {@link Options} to apply in matching
 	 * @return iterator over matches
 	 * @throws GrammarException
 	 */
 	public Matcher lookingAt(final CharSequence cs, Options opt)
 			throws GrammarException {
 		checkComplete();
 		final GlobalState co = verifyOptions(cs, opt);
 		final boolean ltm = containsAlternation && opt.longestMatch();
 		final Map<Integer, CachedMatch>[] cache = offsetCache(opt, co.length);
 		final Set<Integer> startOffsets = startOffsets(cs, co, cache);
 		final Matcher m = root.matcher(co.start, cache, new DummyMatcher(co));
 		final LTMMatcher ltmm = ltm ? new LTMMatcher(m) : null;
 		abstract class LookingAtMatcher extends GrammarMatcher {
 			LookingAtMatcher() {
 				super(co);
 			}
 
 			@Override
 			public String name() {
 				return "lookingAt";
 			}
 
 		}
 		// synchronization wrappers
 		return !co.allowOverlap ? new LookingAtMatcher() {
 			boolean matchedOnce = false;
 
 			@Override
 			public synchronized boolean mightHaveNext() {
 				if (options.study && startOffsets.isEmpty())
 					return false;
 				try {
 					return matchedOnce ? false : (ltm ? ltmm.hasNext() : m
 							.mightHaveNext());
 				} catch (DoubleColonBarrier e) {
 					return false;
 				}
 			}
 
 			@Override
 			public synchronized Match match() {
 				Match n = null;
 				if (!(matchedOnce || options.study && startOffsets.isEmpty())) {
 					matchedOnce = true;
 					try {
 						n = ltm ? ltmm.match() : m.match();
 					} catch (DoubleColonBarrier e) {
 						return bad(m);
 					}
 					return maybeGood(n, m);
 				}
 				return bad(m);
 			}
 
 			@Override
 			public Match rightmostMatch() {
 				return m.rightmost;
 			}
 
 		} : new LookingAtMatcher() {
 
 			@Override
 			public synchronized boolean mightHaveNext() {
 				if (options.study && startOffsets.isEmpty())
 					return false;
 				try {
 					return ltm ? ltmm.hasNext() : m.mightHaveNext();
 				} catch (DoubleColonBarrier e) {
 					return false;
 				}
 			}
 
 			@Override
 			public synchronized Match match() {
 				Match n = null;
 				if (!(options.study && startOffsets.isEmpty())) {
 					try {
 						n = ltm ? ltmm.match() : m.match();
 					} catch (DoubleColonBarrier e) {
 						return bad(m);
 					}
 					return maybeGood(n, m);
 				}
 				return bad(m);
 			}
 
 			@Override
 			public Match rightmostMatch() {
 				return m.rightmost;
 			}
 		};
 	}
 
 	/**
 	 * Creates iterator over matches occurring anywhere in given
 	 * {@link CharSequence}.
 	 * 
 	 * {@link Options#study()} is set to <code>true</code>. If different
 	 * behavior is desired, use {@link #find(CharSequence, Options)}.
 	 * 
 	 * @param s
 	 * @return object iterating over matches
 	 * @throws GrammarException
 	 */
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
 		final GlobalState options = verifyOptions(s, opt);
 		final boolean ltm = containsAlternation && opt.longestMatch();
 		final Map<Integer, CachedMatch>[] cache = offsetCache(opt,
 				options.length);
 		List<Integer> list = new ArrayList<Integer>(startOffsets(s, options,
 				cache));
 		Collections.sort(list);
 		final LinkedList<Integer> startOffsets = new LinkedList<Integer>(list);
 		return new FindMatcher(startOffsets, cache, options, ltm);
 	}
 
 	/**
 	 * Generates a cache to keep track of failing offsets for particular rules.
 	 * This method is also where other post-validation, pre-match preparations
 	 * take place.
 	 * 
 	 * @param options
 	 * @param length
 	 *            length of character sequence
 	 * 
 	 * @return map from labels to sets of offsets where the associated rules are
 	 *         known not to match
 	 */
 	private Map<Integer, CachedMatch>[] offsetCache(Options options, int length) {
 		if (initialRules == null) {
 			synchronized (this) {
 				// look for reversals
 				for (Rule r : rules()) {
 					r.setUid();
 					if (r.isReversed())
 						containsReversal = true;
 				}
 				// fix tag maps in alternations
 				root.fixAlternation();
 				// create actual offset cache
 				root.setCacheIndex(new HashMap<String, Integer>());
 			}
 		}
 		int max = root.maxCacheIndex(-1, new HashSet<Rule>());
 		@SuppressWarnings("unchecked")
 		Map<Integer, CachedMatch>[] offsetCache = new Map[max + 1];
 		boolean lean = options.leanMemory, fat = options.fatMemory;
 		if (!(lean || fat))
 			fat = length < options.longStringLength;
 		for (int i = 0; i < offsetCache.length; i++) {
 			Map<Integer, CachedMatch> m;
 			if (lean)
 				m = new TreeMap<Integer, CachedMatch>();
 			else if (fat)
 				m = new MatchCache(length);
 			else
 				m = new HashMap<Integer, CachedMatch>();
 			offsetCache[i] = m;
 		}
 		return offsetCache;
 	}
 
 	/**
 	 * Prints out nicely formatted rule definitions for grammar, ordering the
 	 * rules in descending order by independence. The least independent rule is
 	 * <code>ROOT</code>, which is listed first. The most independent rules are
 	 * the terminal rules, which are listed last.
 	 * 
 	 * This method delegates to {@link #describe(boolean)}, setting the
 	 * <code>alphabetized</code> parameter to <code>false</code>.
 	 * 
 	 * @return pretty-printed grammar definition
 	 */
 	public String describe() {
 		return describe(false);
 	}
 
 	/**
 	 * Prints out nicely formatted rule definitions for grammar, ordering the
 	 * rules either alphabetically or in order of dependence. In the latter
 	 * case, terminal rules are listed last, preceded by rules that depend only
 	 * on terminal rules, and so forth up the to the root rule. Rules of equal
 	 * independence are listed in alphabetical order.
 	 * <p>
 	 * This method is particularly useful if you produce a grammar by composing
 	 * other grammars. Grammar composition may necessitate rule name
 	 * readjustment to prevent conflicts and this method will facilitated
 	 * discovering these adjustments.
 	 * 
 	 * @param alphabetized
 	 *            whether non-root rules should be listed in alphabetical order
 	 *            or in order of independence
 	 * @return pretty-printed grammar definition
 	 */
 	public String describe(final boolean alphabetized) {
 		// checkComplete();
 		Set<Rule> set = new HashSet<Rule>();
 		root.subRules(set, new HashSet<Rule>(), true);
 		List<Rule> rules = new ArrayList<Rule>(set);
 		for (Iterator<Rule> i = rules.iterator(); i.hasNext();) {
 			Rule r = i.next();
 			if (r == root) {
 				i.remove();
 				continue;
 			}
 			if (r instanceof DeferredDefinitionRule) {
 				DeferredDefinitionRule ddr = (DeferredDefinitionRule) r;
 				if (set.contains(ddr.r))
 					i.remove();
 			}
 		}
 		int maxLabel = root.label().id.length();
 		for (Rule r : rules) {
 			int l = r.label().id.length();
 			if (l > maxLabel)
 				maxLabel = l;
 		}
 		String format = "%" + maxLabel + "s =";
 		// put rules in canonical order
 		Collections.sort(rules, new Comparator<Rule>() {
 			@Override
 			public int compare(Rule o1, Rule o2) {
 				int comparison = alphabetized ? 0 : o2.generation
 						- o1.generation;
 				if (comparison == 0)
 					comparison = o1.label().toString()
 							.compareTo(o2.label().toString());
 				return comparison;
 			}
 		});
 
 		StringBuilder b = new StringBuilder();
 		b.append(String.format(format, rootLabel.id));
 		b.append(' ');
 		b.append(root.description());
 		b.append('\n');
 		if (!rules.isEmpty()) {
 			b.append('\n');
 			for (Rule r : rules) {
 				b.append(String.format(format, r.label().id));
 				b.append(' ');
 				b.append(r.description());
 				b.append("\n");
 			}
 		}
 		return b.toString();
 	}
 
 	/**
 	 * Assigns arbitrary rule to given label. This arbitrary rule must be
 	 * clonable, as defining the deferred definition rule requires modifying the
 	 * state of the rule object that will replace it.
 	 * 
 	 * @param label
 	 * @param rule
 	 */
 	public synchronized void defineRule(String label, Rule rule) {
 		if (rule instanceof DeferredDefinitionRule)
 			throw new GrammarException(
 					"you cannot define a rule to be a DeferredDefinitionRule");
 		if (!(rule instanceof Cloneable))
 			throw new GrammarException("rule must be clonable");
 		try {
 			Method m = rule.getClass().getMethod("clone");
 			rule = (Rule) m.invoke(rule);
 		} catch (Exception e1) {
 			throw new GrammarException("rule must be clonable: " + e1);
 		}
 		rule.setLabel(label);
 		DeferredDefinitionRule r = checkRuleDefinition(label);
 		String oldId = r.uniqueId();
 		Map<Label, Rule> erules = explicitRules();
 		Map<String, Label> idMap = new HashMap<String, Label>(erules.size() * 2);
 		for (Entry<Label, Rule> e : erules.entrySet())
 			idMap.put(e.getValue().uniqueId(), e.getKey());
 		String id = rule.uniqueId();
 		Label l = idMap.get(id);
 		if (l == null) {
 			r.setRule(rule);
 		} else
 			r.setRule(erules.get(idMap.get(id)));
 		fixAlternationTags(oldId, r);
 		undefinedRules.remove(r.label());
 	}
 
 	/**
 	 * Reassigns tags in structures such as
 	 * 
 	 * <pre>
 	 * foo = [{bar} &lt;a&gt; ] | 'b'
 	 * </pre>
 	 * 
 	 * where a tag is assigned to a {@link DeferredDefinitionRule}.
 	 * 
 	 * @param oldId
 	 *            the unique id of the {@link DeferredDefinitionRule} before
 	 *            definition
 	 * @param r
 	 *            the fully defined rule
 	 */
 	private void fixAlternationTags(String oldId, DeferredDefinitionRule r) {
 		Set<Rule> set = root.subRules(false);
 		String newId = null;
 		for (Rule rule : set) {
 			if (rule == r)
 				continue;
 			if (rule instanceof AlternationRule) {
 				AlternationRule ar = (AlternationRule) rule;
 				Set<String> tags = ar.tagMap.remove(oldId);
 				if (tags != null) {
 					if (newId == null)
 						newId = r.r.uniqueId();
 					ar.tagMap.put(newId, tags);
 				}
 			}
 		}
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
 		final GlobalState options = verifyOptions(s, opt);
 		final Map<Integer, CachedMatch>[] cache = offsetCache(opt,
 				options.length);
 		final Set<Integer> startOffsets = startOffsets(s, options, cache);
 		final Matcher m = root.matcher(options.start, cache, new DummyMatcher(
 				options));
 		return new GrammarMatcher(options) {
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
 				try {
 					while ((n = m.match()) != null) {
 						if (n.end() == options.end())
 							return n;
 					}
 				} catch (DoubleColonBarrier e) {
 				}
 				return null;
 			}
 
 			@Override
 			public synchronized Match match() {
 				if (mightHaveNext()) {
 					Match n = next;
 					next = fetchNext();
 					matchedOnce = true;
 					return maybeGood(n, m);
 				}
 				return bad(m);
 			}
 
 			@Override
 			public Match rightmostMatch() {
 				return m.rightmost;
 			}
 
 			@Override
 			protected String name() {
 				return "matches";
 			}
 		};
 	}
 
 	private Set<Integer> startOffsets(final CharSequence s,
 			final GlobalState options, final Map<Integer, CachedMatch>[] cache) {
 		final Set<Integer> startOffsets = new HashSet<Integer>();
 		if (options.study) {
 			Set<String> done = new HashSet<String>(rules().size() * 2);
 			// collect offsets from initial rules
 			initialRules();
 			for (Rule r : rules()) {
 				if (initialRules.contains(r.uid())) {
 					if (!done.contains(r.uid()))
 						startOffsets.addAll(r.study(s, cache, options));
 					done.add(r.uid());
 				}
 			}
 			if (containsReversal || done.size() < terminalRules.size()) {
 				String reversed = null;
 				// study terminal rules, skipping initials and reversed rules
 				for (Rule r : rules()) {
 					if (terminalRules.contains(r.uid())) {
 						if (!done.contains(r.uid())) {
 							if (containsReversal && r.isReversed())
 								continue;
 							r.study(s, cache, options);
 							done.add(r.uid());
 						}
 					}
 				}
 				if (containsReversal) {
 					// study reversed rules, reverse caches of their
 					// non-reversed
 					// counterparts where possible
 					for (Rule r : rules()) {
 						if (terminalRules.contains(r.uid())) {
 							if (!done.contains(r.uid())) {
 								String uid = r.uid();
 								uid = uid.substring(0, uid.length()
 										- Assertion.REVERSAL_SUFFIX.length());
 								if (done.contains(uid)) {
 									Rule counterpart = null;
 									for (Rule r2 : rules()) {
 										if (r2.uid().equals(uid)) {
 											counterpart = r2;
 											break;
 										}
 									}
 									Map<Integer, CachedMatch> countercache = cache[counterpart.cacheIndex];
 									Map<Integer, CachedMatch> owncache = reverse(
 											r, countercache, options.rcs);
 									cache[r.cacheIndex] = owncache;
 								} else if (r instanceof LeafRule
 										|| (r instanceof LiteralRule && ((LiteralRule) r).c == null)) {
 									if (reversed == null)
 										reversed = options.rcs.toString();
 									r.study(reversed, cache, options);
 								} else
 									r.study(options.rcs, cache, options);
 								done.add(r.uid());
 							}
 						}
 					}
 				}
 			}
 		}
 		return startOffsets;
 	}
 
 	/**
 	 * Reverses the cached matches of a terminal rule to accelerate studying.
 	 * 
 	 * @param r
 	 *            reversed rule
 	 * @param countercache
 	 *            non-reversed cache
 	 * @param rcs
 	 *            {@link ReversedCharSequence} used for translating offsets
 	 * @return reversed cache
 	 */
 	private Map<Integer, CachedMatch> reverse(Rule r,
 			Map<Integer, CachedMatch> countercache, ReversedCharSequence rcs) {
 		Map<Integer, CachedMatch> reverse = new HashMap<Integer, CachedMatch>(
 				countercache.size() * 2);
 		for (Entry<Integer, CachedMatch> e : countercache.entrySet()) {
 			CachedMatch cm = e.getValue();
 			int start = rcs.translate(cm.m.end() - 1), end = start
 					+ cm.m.length();
 			int offset = start;
 			Match m = new Match(r, start, end);
 			CachedMatch rcm = new CachedMatch(m);
 			reverse.put(offset, rcm);
 		}
 		return reverse;
 	}
 
 	/**
 	 * completes rule initialization
 	 */
 	private synchronized void initialRules() {
 		if (initialRules == null) {
 			Map<String, Boolean> zeroMap = new HashMap<String, Boolean>(rules()
 					.size() * 2);
 			root.mayBeZeroWidth(zeroMap);
 			for (Rule r : rules()) {
 				Boolean b = zeroMap.get(r.uid());
 				if (b == null)
 					throw new GrammarException("logic error: rule " + r
 							+ " never evaluated by Rule.mightBeZeroWidth()");
 				r.mayBeZeroWidth = b;
 			}
 			initialRules = new HashSet<String>(rules().size() * 2);
 			terminalRules = new HashSet<String>(rules().size() * 2);
 			root.initialRules(initialRules);
 			for (Rule r : rules()) {
 				if (r instanceof NonterminalRule) {
 					initialRules.remove(r.uid());
 					if (r.cycle) {
 						r.cycle = r.findLeftCycle(r, new HashSet<Rule>(rules()
 								.size() * 2));
 					}
 				} else
 					terminalRules.add(r.uid());
 			}
 		}
 	}
 
 	private synchronized Set<Rule> rules() {
 		if (ruleSet == null) {
 			Set<Rule> set = new HashSet<Rule>();
 			root.subRules(set, new HashSet<Rule>(), false);
 			if (!validated)
 				return set;
 			ruleSet = new HashSet<Rule>(set);
 		}
 		return ruleSet;
 	}
 
 	/**
 	 * Validates options and clones object to make it thread safe.
 	 * 
 	 * @param s
 	 * @param opt
 	 * @return clone of given options
 	 */
 	private GlobalState verifyOptions(CharSequence s, Options opt) {
 		if (opt.start() > 0 && opt.start() >= s.length())
 			throw new GrammarException(
 					"start offset specified beyond end of string");
 		return new GlobalState(s, opt);
 	}
 
 	public synchronized void defineRule(String label, Grammar g, String id,
 			Condition c) {
 		DeferredDefinitionRule r = checkRuleDefinition(label);
 		String oldId = r.uniqueId();
 		g.checkComplete();
 		// figure out how to adjust the generation numbers so describe()
 		// reflects the dependencies among rules
 		int greatestGeneration = -1;
 		for (Rule sr : g.rules()) {
 			if (sr.generation > greatestGeneration)
 				greatestGeneration = sr.generation;
 		}
 		// increment once more for the root rule
 		greatestGeneration++;
 		// now copy the rule tree
 		Set<Rule> explicitRules = root.subRules(true);
 		Set<String> knownLabels = new HashSet<String>(explicitRules.size() * 2), knownConditions = new HashSet<String>(
 				explicitRules.size() * 2);
 		for (Rule er : explicitRules) {
 			knownLabels.add(er.label().id);
 			knownLabels.addAll(er.conditionNames());
 		}
 		Rule rc = g.root.deepCopy(label, new HashMap<String, Rule>(g.rules()
 				.size() * 2), knownLabels, knownConditions);
 		if (c != null)
 			rc = conditionCheck(id, c, rc);
 		if (redefinitionCheck(r, rc)) {
 			if (ruleSet != null) {
 				ruleSet.clear();
 				ruleSet = null;
 			}
 			r.generation = greatestGeneration;
 			Set<Rule> set = new HashSet<Rule>();
 			root.subRules(set, new HashSet<Rule>(), false);
 			for (Rule sr : set) {
 				if (sr.generation > 0 && sr.dependsOn(r))
 					sr.generation += greatestGeneration;
 			}
 			rc.generation = -1;
 			fixAlternationTags(oldId, r);
 		}
 	}
 
 	/**
 	 * Replace all instances of old {@link Rule} with new.
 	 * 
 	 * @param g
 	 * @param ru
 	 *            old rule
 	 * @param nru
 	 *            new rule
 	 */
 	private void fix(Grammar g, Rule ru, Rule nru) {
 		nru.generation = ru.generation;
 		Set<Rule> set = new HashSet<Rule>();
 		root.subRules(set, new HashSet<Rule>(), false);
 		for (Rule r : set) {
 			fix(ru, nru, r);
 		}
 	}
 
 	/**
 	 * Replace all instances of old {@link Rule} with new.
 	 * 
 	 * @param ru
 	 *            old rule
 	 * @param nru
 	 *            new rule
 	 * @param r
 	 */
 	private void fix(Rule ru, Rule nru, Rule r) {
 		if (r instanceof AlternationRule) {
 			AlternationRule ar = (AlternationRule) r;
 			for (int i = 0; i < ar.alternates.length; i++) {
 				if (ar.alternates[i] == ru)
 					ar.alternates[i] = nru;
 				else
 					fix(ru, nru, ar.alternates[i]);
 			}
 		} else if (r instanceof SequenceRule) {
 			SequenceRule sr = (SequenceRule) r;
 			for (int i = 0; i < sr.sequence.length; i++) {
 				if (sr.sequence[i] == ru)
 					sr.sequence[i] = nru;
 				else
 					fix(ru, nru, sr.sequence[i]);
 			}
 		} else if (r instanceof RepetitionRule) {
 			RepetitionRule rr = (RepetitionRule) r;
 			if (rr.r == ru)
 				rr.r = nru;
 			else
 				fix(ru, nru, rr.r);
 		} else if (r instanceof DeferredDefinitionRule) {
 			DeferredDefinitionRule ddr = (DeferredDefinitionRule) r;
 			if (ddr.r == ru)
 				ddr.r = nru;
 		} else if (r instanceof CyclicRule) {
 			CyclicRule cr = (CyclicRule) r;
 			if (cr.r == ru)
 				cr.r = nru;
 		} else if (r instanceof Assertion) {
 			Assertion a = (Assertion) r;
 			if (a.r == ru)
 				a.r = nru;
 			else
 				fix(ru, nru, a.r);
 		}
 	}
 
 	/**
 	 * For defining a terminal rule left undefined by the initial rule set. In
 	 * this case you are defining the rule to be a {@link LiteralRule}.
 	 * 
 	 * @param label
 	 *            id of undefined rule
 	 * @param literal
 	 *            String to match
 	 * @param id
 	 *            unique identifier for condition, if any
 	 * @param c
 	 *            optional {@link Condition}
 	 * @throws GrammarException
 	 */
 	public synchronized void defineRule(String label, String literal,
 			String id, Condition c) throws GrammarException {
 		DeferredDefinitionRule r = checkRuleDefinition(label);
 		String oldId = r.uniqueId();
 		Label l = new Label(Type.explicit, label);
 		LiteralRule lr = new LiteralRule(l, literal);
 		if (c != null)
 			lr = (LiteralRule) conditionCheck(label, c, lr);
 		redefinitionCheck(r, lr);
 		fixAlternationTags(oldId, r);
 	}
 
 	/**
 	 * Assign a condition to an arbitrary {@link Rule}.
 	 * 
 	 * @param labelId
 	 * @param conditionId
 	 * @param c
 	 */
 	public synchronized void assignCondition(String labelId,
 			String conditionId, Condition c) {
 		if (undefinedConditions.containsKey(conditionId))
 			throw new GrammarException(conditionId
 					+ " belongs to an as yet undefined condition");
 		// clear some caches
 		initialRules = null;
 		ruleSet = null;
 
 		Label l = null;
 		Map<Label, Rule> erules = explicitRules();
 		for (Label label : erules.keySet()) {
 			if (label.id.equals(labelId)) {
 				l = label;
 				break;
 			}
 		}
 		if (l == null)
 			throw new GrammarException("unknown rule: " + labelId);
 		Rule r = erules.remove(l);
 		Rule nr = conditionCheck(conditionId, c, r);
 		if (l.equals(rootLabel))
 			root = nr;
 		fix(this, r, nr);
 		erules.put(l, nr);
 	}
 
 	/**
 	 * @param label
 	 *            condition identifier
 	 * @param c
 	 */
 	public synchronized void defineCondition(String label, Condition c) {
 		Set<Label> set = undefinedConditions.remove(label);
 		if (set == null)
 			throw new GrammarException("no undefined condition " + label);
 		Set<Rule> rset = new HashSet<Rule>(set.size() * 2);
 		knownConditions.put(label, rset);
 		Set<Rule> allRules = new HashSet<Rule>();
 		root.subRules(allRules, new HashSet<Rule>(), true);
 		root.subRules(allRules, new HashSet<Rule>(), false);
 		Map<Label, Rule> ruleMap = new HashMap<Label, Rule>(allRules.size() * 2);
 		for (Rule r : allRules)
 			ruleMap.put(r.label, r);
 		for (Label l : set) {
 			Rule r = ruleMap.remove(l);
 			if (r == null)
 				continue;
 			Rule nr = r.conditionalize(c, label);
 			if (r != nr)
 				fix(this, r, nr);
 			ruleMap.put(l, nr);
 			if (l.equals(rootLabel))
 				root = nr;
 			rset.add(nr);
 			String reversedId = l.id + Assertion.REVERSAL_SUFFIX;
 			for (Rule rr : findRulesById(reversedId)) {
 				Rule nrr = rr.conditionalize(c, label);
 				if (rr != nrr)
 					fix(this, rr, nrr);
 			}
 		}
 		c.setName(label);
 	}
 
 	/**
 	 * Find all the rules with the given label id.
 	 * 
 	 * @param reversedId
 	 * @return
 	 */
 	private Set<Rule> findRulesById(String reversedId) {
 		Set<Rule> set = new HashSet<Rule>(), cycleSet = new HashSet<Rule>();
 		for (Rule r : rules()) {
 			findRulesById(r, set, reversedId, cycleSet);
 		}
 		return set;
 	}
 
 	private void findRulesById(Rule r, Set<Rule> set, String reversedId,
 			Set<Rule> cycleSet) {
 		if (r.label.id.equals(reversedId))
 			set.add(r);
 		// recursively search non-terminal rules
 		if (r instanceof SequenceRule) {
 			SequenceRule sr = (SequenceRule) r;
 			for (Rule srr : sr.sequence)
 				findRulesById(srr, set, reversedId, cycleSet);
 		} else if (r instanceof AlternationRule) {
 			AlternationRule ar = (AlternationRule) r;
 			for (Rule sr : ar.alternates)
 				findRulesById(sr, set, reversedId, cycleSet);
 		} else if (r instanceof RepetitionRule) {
 			findRulesById(((RepetitionRule) r).r, set, reversedId, cycleSet);
 		} else if (r instanceof Assertion) {
 			findRulesById(((Assertion) r).r, set, reversedId, cycleSet);
 		} else if (r instanceof CyclicRule) {
 			if (!cycleSet.contains(r)) {
 				cycleSet.add(r);
 				findRulesById(((CyclicRule) r).r, set, reversedId, cycleSet);
 			}
 		}
 	}
 
 	/**
 	 * Assign a complete grammar to an undefined symbol. Delegates to
 	 * {@link #defineRule(String, Grammar, String, Condition)}, using
 	 * <code>null</code> for last two parameters.
 	 * 
 	 * @param label
 	 * @param g
 	 */
 	public synchronized void defineRule(String label, Grammar g) {
 		defineRule(label, g, null, null);
 	}
 
 	/**
 	 * For defining a terminal rule left undefined by the initial rule set.
 	 * Delegates to {@link #defineRule(String, Pattern, String, Condition)},
 	 * using <code>null</code> for last two parameters.
 	 * 
 	 * @param label
 	 * @param p
 	 * @throws GrammarException
 	 */
 	public synchronized void defineRule(String label, Pattern p)
 			throws GrammarException {
 		defineRule(label, p, null, null);
 	}
 
 	/**
 	 * For defining a terminal rule left undefined by the initial rule set.
 	 * Delegates to {@link #defineRule(String, String, String, Condition)},
 	 * using <code>null</code> for last two parameters.
 	 * 
 	 * @param label
 	 *            id of undefined rule
 	 * @param literal
 	 *            String to match
 	 * @throws GrammarException
 	 */
 	public synchronized void defineRule(String label, String literal)
 			throws GrammarException {
 		defineRule(label, literal, null, null);
 	}
 }
