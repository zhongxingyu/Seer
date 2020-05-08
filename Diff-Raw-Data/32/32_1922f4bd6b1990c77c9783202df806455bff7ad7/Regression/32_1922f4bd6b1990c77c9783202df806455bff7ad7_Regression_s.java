 package dfh.grammar;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import org.junit.Test;
 
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
 	@Test
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
 }
