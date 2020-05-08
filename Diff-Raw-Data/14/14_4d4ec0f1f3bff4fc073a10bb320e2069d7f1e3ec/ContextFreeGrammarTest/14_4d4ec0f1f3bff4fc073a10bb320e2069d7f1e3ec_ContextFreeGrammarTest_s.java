package com.github.tteofili.nlputils;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.github.tteofili.nlputils.cfg.ContextFreeGrammar;
 import com.github.tteofili.nlputils.cfg.Rule;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Testcase for {@link com.github.tteofili.nlputils.cfg.ContextFreeGrammar}
  */
 public class ContextFreeGrammarTest {
 
   private ContextFreeGrammar contextFreeGrammar;
   private Set<String> terminals;
 
   @Before
   public void setUp() throws Exception {
 
     Set<String> nonTerminals = new HashSet<String>(); // PoS + Parse tags
     nonTerminals.add("S");
     nonTerminals.add("NP");
     nonTerminals.add("VP");
     nonTerminals.add("PP");
     nonTerminals.add("DT");
     nonTerminals.add("Vi");
     nonTerminals.add("Vt");
     nonTerminals.add("NN");
     nonTerminals.add("IN");
     nonTerminals.add("NNP");
     nonTerminals.add("CJ");
     nonTerminals.add("DJ");
     nonTerminals.add("P");
 
     String startSymbol = "S";
 
     terminals = new HashSet<String>();
     terminals.add("sleeps");
     terminals.add("saw");
     terminals.add("man");
     terminals.add("woman");
     terminals.add("telescope");
     terminals.add("the");
     terminals.add("with");
     terminals.add("in");
     terminals.add("tommaso");
     terminals.add("simone");
     terminals.add("joao");
     terminals.add("tigro");
     terminals.add("michele");
     terminals.add("scarlett");
     terminals.add("and");
     terminals.add("but");
     terminals.add("while");
 
     Set<Rule> rules = new HashSet<Rule>();
     rules.add(new Rule("S", "NP", "VP"));
     rules.add(new Rule("P", "S", "CJ", "S"));
     rules.add(new Rule("P", "S", "DJ", "S"));
     rules.add(new Rule("VP", "Vi"));
     rules.add(new Rule("VP", "Vt", "NP"));
     rules.add(new Rule("VP", "VP", "PP"));
     rules.add(new Rule("NP", "DT", "NN"));
     rules.add(new Rule("NP", "NP", "PP"));
     rules.add(new Rule("NP", "NNP"));
     rules.add(new Rule("PP", "IN", "NP"));
     rules.add(new Rule("Vi", "sleeps"));
     rules.add(new Rule("Vt", "saw"));
     rules.add(new Rule("NN", "man"));
     rules.add(new Rule("NN", "woman"));
     rules.add(new Rule("NN", "telescope"));
     rules.add(new Rule("DT", "the"));
     rules.add(new Rule("IN", "with"));
     rules.add(new Rule("IN", "in"));
     rules.add(new Rule("NNP", "tommaso"));
     rules.add(new Rule("NNP", "simone"));
     rules.add(new Rule("NNP", "joao"));
     rules.add(new Rule("NNP", "tigro"));
     rules.add(new Rule("NNP", "michele"));
     rules.add(new Rule("NNP", "scarlett"));
     rules.add(new Rule("CJ", "and"));
     rules.add(new Rule("DJ", "but"));
     rules.add(new Rule("DJ", "while"));
 
     contextFreeGrammar = new ContextFreeGrammar(nonTerminals, terminals, rules, startSymbol);
   }
 
   @Test
   public void testSingleExpansion() throws Exception {
     String[] expansion = contextFreeGrammar.leftMostDerivation("S");
     checkExpansion(expansion);
   }
 
 
   @Test
   public void testMultipleSentencesExpansion() throws Exception {
     String[] expansion = contextFreeGrammar.leftMostDerivation("S", "CJ", "S");
     checkExpansion(expansion);
 
     expansion = contextFreeGrammar.leftMostDerivation("S", "DJ", "S", "CJ", "P");
     checkExpansion(expansion);
   }
 
   private void checkExpansion(String[] expansion) {
     assertNotNull(expansion);
     assertTrue(expansion.length > 0);
     for (String t : expansion) {
       assertTrue("term " + t + " is not a terminal symbol", terminals.contains(t));
     }
     System.err.println(Arrays.toString(expansion));
   }
 }
