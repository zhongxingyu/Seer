 package dfh.grammar;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.junit.Test;
 
 import dfh.grammar.Label.Type;
 
 public class Regression {
 
 	@Test
 	public void optionReuse() {
 		String[] rules = { "ROOT = /\\w++/" };
 		Grammar g = null;
 		try {
 			g = new Grammar(rules);
 		} catch (Exception e) {
 			fail("threw exception");
 		}
 		Options opt = new Options().keepRightmost(true);
 		g.matches("foo", opt);
 		g.matches("alphabet", opt);
 	}
 
 	@Test
 	public void finalNonmatch() {
 		String[] rules = {
 				//
 				"ROOT = <word> /\\s*+/",//
 				"word = /\\w++/",//
 		};
 		try {
 			Grammar g = new Grammar(rules);
 			Matcher m = g.find("foo", new Options());
 			assertNotNull(m.match());
 		} catch (Exception e) {
 			fail("threw exception: " + e);
 		}
 	}
 
 	@Test
 	public void missingFirstOffset() {
 		String[] rules = {
 				//
 				"ROOT = /[*_]/+ <word> 1 /(?![\\p{L}\\d])/",// works if you use
 															// native assertion
 				"word = /\\w++/",//
 		};
 		try {
 			Grammar g = new Grammar(rules);
 			Matcher m = g.find("*terrible*", new Options().allowOverlap(true));
 			assertNotNull(m.match());
 		} catch (Exception e) {
 			fail("threw exception: " + e);
 		}
 	}
 
 	@Test
 	public void labelsAndDeferredRuleDefinition() {
 		try {
 			String[] rules = { "ROOT = <foo> | 'quux'" };
 			Grammar g = new Grammar(rules);
 			g.defineRule("foo", "bar");
 			Match m = g.matches("bar").match();
 			m.first("foo");
 		} catch (Exception e) {
 			fail("threw exception: " + e);
 		}
 	}
 
 	@Test
 	public void backwardAssertionWordBreak() {
 		Grammar g = new Grammar("ROOT = after /\\b/r 'a'");
 		assertNotNull("found initial word break in backwards assertion", g
 				.matches("a").match());
 	}
 
 	@Test
 	public void quantifiedRegex() {
 		Grammar g = new Grammar("rule = /a/? '1'");
 		assertNotNull(g.matches("1").match());
 	}
 
 	@Test
 	public void infiniteQuantification() {
 		Grammar g = new Grammar("rule = [ 'a'* | 'b'* ]+");
 		try {
 			g.matches("c").match();
 		} catch (GrammarException e) {
 			assertTrue(e.getMessage().startsWith("non-advancing repetition"));
 		}
 	}
 
 	/**
 	 * For diagnosing an infinite loop.
 	 * 
 	 * @throws InterruptedException
 	 */
 //	@Test
 	public void studyPlusStinginess() throws InterruptedException {
 		final boolean[] success = { false };
 		Runnable r = new Runnable() {
 			@SuppressWarnings("serial")
 			@Override
 			public void run() {
 				Grammar g = new Grammar("rule = 'a'*? (2)");
 				g.defineCondition("2", new Condition() {
 					@Override
 					public boolean passes(Match m, Matcher n, CharSequence s) {
 						return m.length() == 2;
 					}
 				});
 				String s = "aa a";
 				Options opt = new Options().study(false);
 				Matcher m = g.find(s, opt);
 				int count = 0;
 				while (m.match() != null)
 					count++;
 				assertEquals(1, count);
 				synchronized (success) {
 					success[0] = true;
 					success.notify();
 				}
 			}
 		};
 		final Thread t = new Thread(r);
 		t.start();
 		synchronized (success) {
 			// if the match doesn't complete in 1 second, we've got an infinite
 			// loop
 			success.wait();
 			if (t.isAlive()) {
 				t.interrupt();
 			}
 			assertTrue(success[0]);
 		}
 	}
 
 	@Test
 	public void descriptionOfIncompleteGrammar() {
 		String[] rules = {
 				//
 				"time = <at_time> | <full_time> | <otime>",//
 				"o_time = [{hour} <phour>] [<period> | <oclock>]",//
 				"oclock = /\\s*+o'?+clock/i",//
 				"at_time = <at> <partial_time>",//
 				"at = /(?:@|at)\\s*+/i",//
 				"partial_time = [{hour} <phour>] [ ':' <minute> ]? <period>?",//
 				"phour = /t(?>w(?>o|elve)|hree|en)|s(?>ix|even)|one|nine|f(?>our|ive)|e(?>leven|ight)|9|8|7|6|5|4|3|2|1[0-2]?+|0/i",//
 				"full_time = [{hour} <fhour>] ':' <minute> <period>?",//
 				"fhour = /\\d{1,2}/",//
 				"minute = /\\d{2}/",//
 				"period = /\\s*+[ap]m?+/i",//
 		};
 		Grammar g = new Grammar(rules);
 		g.describe();
 	}
 
 	@Test
 	public void stingySequence() {
 		Grammar g = new Grammar("foo = 'a'*?");
 		String s = "aaaa";
 		Matcher m = g.matches(s);
 		Match n = m.match();
 		assertNotNull(n);
 	}
 
 	@Test
 	public void grammarDescriptionWithPredefinedRules() {
 		LeafRule r = new LeafRule(new Label(Type.explicit, "foo"),
 				Pattern.compile("a"), true);
 		Map<String, Rule> predefinedRules = new HashMap<String, Rule>();
 		predefinedRules.put("foo", r);
 		Grammar g = new Grammar("bar = after <foo> 'b'", predefinedRules);
 		String s = g.describe();
 		assertTrue(s.indexOf('/') > -1);
 	}
 }
