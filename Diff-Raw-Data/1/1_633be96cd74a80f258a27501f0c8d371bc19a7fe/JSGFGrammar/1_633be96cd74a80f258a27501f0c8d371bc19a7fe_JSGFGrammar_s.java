 /*
  * Copyright 1999-2002 Carnegie Mellon University.  
  * Portions Copyright 2002 Sun Microsystems, Inc.  
  * Portions Copyright 2002 Mitsubishi Electric Research Laboratories.
  * All Rights Reserved.  Use is subject to license terms.
  * 
  * See the file "license.terms" for information on usage and
  * redistribution of this file, and for a DISCLAIMER OF ALL 
  * WARRANTIES.
  *
  */
 
 package edu.cmu.sphinx.jsapi;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.speech.EngineException;
 import javax.speech.recognition.GrammarException;
 import javax.speech.recognition.GrammarSyntaxDetail;
 import javax.speech.recognition.Recognizer;
 import javax.speech.recognition.Rule;
 import javax.speech.recognition.RuleAlternatives;
 import javax.speech.recognition.RuleCount;
 import javax.speech.recognition.RuleGrammar;
 import javax.speech.recognition.RuleName;
 import javax.speech.recognition.RuleParse;
 import javax.speech.recognition.RuleSequence;
 import javax.speech.recognition.RuleTag;
 import javax.speech.recognition.RuleToken;
 
 import com.sun.speech.engine.recognition.BaseRecognizer;
 
 import edu.cmu.sphinx.linguist.dictionary.Dictionary;
 import edu.cmu.sphinx.linguist.language.grammar.Grammar;
 import edu.cmu.sphinx.linguist.language.grammar.GrammarNode;
 import edu.cmu.sphinx.util.LogMath;
 import edu.cmu.sphinx.util.props.ConfigurationManagerUtils;
 import edu.cmu.sphinx.util.props.PropertyException;
 import edu.cmu.sphinx.util.props.PropertySheet;
 import edu.cmu.sphinx.util.props.S4Component;
 import edu.cmu.sphinx.util.props.S4String;
 
 /**
  * Defines a BNF-style grammar based on JSGF grammar rules in a file.
  * <p/>
  * <p/>
  * The Java Speech Grammar Format (JSGF) is a BNF-style, platform-independent, and vendor-independent textual
  * representation of grammars for use in speech recognition. It is used by the <a href="http://java.sun.com/products/java-media/speech/">Java
  * Speech API (JSAPI) </a>.
  * <p/>
  * <p/>
  * Here we only intend to give a couple of examples of grammars written in JSGF, so that you can quickly learn to write
  * your own grammars. For more examples and a complete specification of JSGF, go to <p><a
  * href="http://java.sun.com/products/java-media/speech/forDevelopers/JSGF/">http://java.sun.com/products/java-media/speech/forDevelopers/JSGF/
  * </a>.
  * <p/>
  * <p/>
  * <b>Example 1: "Hello World" in JSGF </b>
  * <p/>
  * <p/>
  * The example below shows how a JSGF grammar that generates the sentences "Hello World":
  * <p/>
  * <p/>
  * <table width="100%" cellpadding="10"> <tr> <td bgcolor="#DDDDDD">
  * <p/>
  * <pre>
  *  #JSGF V1.0
  * <p/>
  *  public &lt;helloWorld&gt; = Hello World;
  * </pre>
  * <p/>
  * </td> </tr> </table>
  * <p/>
  * <i>Figure 1: Hello grammar that generates the sentences "Hello World". </i>
  * <p/>
  * <p/>
  * The above grammar is saved in a file called "hello.gram". It defines a public grammar rule called "helloWorld". In
  * order for this grammar rule to be publicly accessible, we must be declared it "public". Non-public grammar rules are
  * not visible outside of the grammar file.
  * <p/>
  * <p/>
  * The location of the grammar file(s) is(are) defined by the {@link #PROP_BASE_GRAMMAR_URL baseGrammarURL}property.
  * Since all JSGF grammar files end with ".gram", it will automatically search all such files at the given URL for the
  * grammar. The name of the grammar to search for is specified by {@link #PROP_GRAMMAR_NAME grammarName}. In this
  * example, the grammar name is "helloWorld".
  * <p/>
  * <p/>
  * <b>Example 2: Command Grammar in JSGF </b>
  * <p/>
  * <p/>
  * This examples shows a grammar that generates basic control commands like "move a menu thanks please", "close file",
  * "oh mighty computer please kindly delete menu thanks". It is the same as one of the command & control examples in the
  * <a href="http://java.sun.com/products/java-media/speech/forDevelopers/JSGF/">JSGF specification </a>. It is
  * considerably more complex than the previous example. It defines the public grammar called "basicCmd".
  * <p/>
  * <p/>
  * <table width="100%" cellpadding="10"> <tr> <td bgcolor="#DDDDDD">
  * <p/>
  * <pre>
  *  #JSGF V1.0
  * <p/>
  *  public &lt;basicCmd&gt; = &lt;startPolite&gt; &lt;command&gt; &lt;endPolite&gt;;
  * <p/>
  *  &lt;command&gt; = &lt;action&gt; &lt;object&gt;;
  *  &lt;action&gt; = /10/ open |/2/ close |/1/ delete |/1/ move;
  *  &lt;object&gt; = [the | a] (window | file | menu);
  * <p/>
  *  &lt;startPolite&gt; = (please | kindly | could you | oh mighty computer) *;
  *  &lt;endPolite&gt; = [ please | thanks | thank you ];
  * </pre>
  * <p/>
  * </td> </tr> </table>
  * <p/>
  * <i>Figure 2: Command grammar that generates simple control commands. </i>
  * <p/>
  * <p/>
  * The features of JSGF that are shown in this example includes: <ul> <li>using other grammar rules within a grammar
  * rule. <li>the OR "|" operator. <li>the grouping "(...)" operator. <li>the optional grouping "[...]" operator. <li>the
  * zero-or-many "*" (called Kleene star) operator. <li>a probability (e.g., "open" is more likely than the others).
  * </ul>
  * <p/>
  * <p/>
  * <h3>From JSGF to Grammar Graph</h3>
  * <p/>
  * After the JSGF grammar is read in, it is converted to a graph of words representing the grammar. Lets call this the
  * grammar graph. It is from this grammar graph that the eventual search structure used for speech recognition is built.
  * Below, we show the grammar graphs created from the above JSGF grammars. The nodes <code>"&lt;sil&gt;"</code> means
  * "silence".
  * <p/>
  * <p/>
  * <img src="doc-files/helloWorld.jpg"> <br> <i>Figure 3: Grammar graph created from the Hello World grammar. </i>
  * <p/>
  * <p/>
  * <img src="doc-files/commandGrammar.jpg"> <br> <i>Figure 4: Grammar graph created from the Command grammar. </i>
  * <p/>
  * <p/>
  * <h3>Limitations</h3>
  * <p/>
  * There is a known limitation with the current JSGF support. Grammars that contain non-speech loops currently cause the
  * recognizer to hang.
  * <p/>
  * For example, in the following grammar
  * <p/>
  * <pre>
  *  #JSGF V1.0
  *  grammar jsgf.nastygram;
  *  public <nasty> = I saw a ((cat* | dog* | mouse*)+)+;
  * </pre>
  * <p/>
  * the production: ((cat* | dog* | mouse*)+)+ can result in a continuous loop, since (cat* | dog* | mouse*) can
  * represent no speech (i.e. zero cats, dogs and mice), this is equivalent to ()+. To avoid this problem, the grammar
  * writer should ensure that there are no rules that could possibly match no speech within a plus operator or kleene
  * star operator.
  * <p/>
  * <h3>Dynamic grammar behavior</h3> It is possible to modify the grammar of a running application. Some rules and
  * notes: <ul> <li> Unlike a JSAPI recognizer, the JSGF Grammar only maintains one Rule Grammar. This restriction may be
  * relaxed in the future. <li> The grammar should not be modified while a recognition is in process <li> The call to
  * JSGFGrammar.loadJSGF will load in a completely new grammar, tossing any old grammars or changes. No call to
  * commitChanges is necessary (although such a call would be harmless in this situation). <li> RuleGrammars can be
  * modified  via calls to RuleGrammar.setEnabled and RuleGrammar.setRule). In order for these changes to take place,
  * JSGFGrammar.commitChanges must be called after all grammar changes have been made. </ul>
  * <p/>
  * <h3>Implementation Notes</h3> <ol> <li>All internal probabilities are maintained in LogMath log base. </ol>
  */
 
 public class JSGFGrammar extends Grammar {
 
     /** Sphinx property that defines the location of the JSGF grammar file. */
     @S4String
     public final static String PROP_BASE_GRAMMAR_URL = "grammarLocation";
 
     /** Sphinx property that defines the location of the JSGF grammar file. */
     @S4String(defaultValue = "default.gram")
     public final static String PROP_GRAMMAR_NAME = "grammarName";
 
     /** Sphinx property that defines the logMath component. */
     @S4Component(type = LogMath.class)
     public final static String PROP_LOG_MATH = "logMath";
 
 
     // ---------------------
     // Configurable data
     // ---------------------
     private RuleGrammar ruleGrammar;
     private RuleStack ruleStack;
     private Recognizer recognizer;
     private String grammarName;
     private URL baseURL;
     private LogMath logMath;
 
     private boolean loadGrammar = true;
     private GrammarNode firstNode;
     private Logger logger;
 
     public JSGFGrammar(URL baseURL, LogMath logMath, String grammarName, boolean showGrammar,boolean optimizeGrammar,boolean addSilenceWords, boolean addFillerWords, Dictionary dictionary ) {
         super(showGrammar,optimizeGrammar,addSilenceWords,addFillerWords,dictionary );
         this.baseURL = baseURL;
         this.logMath = logMath;
         this.grammarName = grammarName;
         loadGrammar = true;
     }
 
     public JSGFGrammar() {
 
     }
 
 
     /*
     * (non-Javadoc)
     *
     * @see edu.cmu.sphinx.util.props.Configurable#newProperties(edu.cmu.sphinx.util.props.PropertySheet)
     */
     public void newProperties(PropertySheet ps) throws PropertyException {
         super.newProperties(ps);
         baseURL = ConfigurationManagerUtils.getResource(PROP_BASE_GRAMMAR_URL, ps);
         logMath = (LogMath) ps.getComponent(PROP_LOG_MATH);
         logger = ps.getLogger();
         grammarName = ps.getString(PROP_GRAMMAR_NAME);
         loadGrammar = true;
     }
 
 
     /**
      * Returns the RuleGrammar of this JSGFGrammar.
      *
      * @return the RuleGrammar
      */
     public RuleGrammar getRuleGrammar() {
         return ruleGrammar;
     }
 
 
     /**
      * Sets the URL context of the JSGF grammars.
      *
      * @param url the URL context of the grammars
      */
     public void setBaseURL(URL url) {
         baseURL = url;
     }
 
 
     /** Returns the name of this grammar. */
     public String getGrammarName() {
         return grammarName;
     }
 
 
     /**
      * The JSGF grammar specified by grammarName will be loaded from the base url (tossing out any previously loaded
      * grammars)
      *
      * @param grammarName the name of the grammar
      * @throws IOException if an error occurs while loading or compiling the grammar
      */
     public void loadJSGF(String grammarName) throws IOException {
         this.grammarName = grammarName;
         loadGrammar = true;
         commitChanges();
     }
 
 
     /**
      * Creates the grammar.
      *
      * @return the initial node of the Grammar
      */
     protected GrammarNode createGrammar() throws IOException {
         commitChanges();
         return firstNode;
     }
 
 
     /**
      * Returns the initial node for the grammar
      *
      * @return the initial grammar node
      */
     public GrammarNode getInitialNode() {
         return firstNode;
     }
 
 
     /**
      * Parses the given Rule into a network of GrammarNodes.
      *
      * @param rule the Rule to parse
      * @return a grammar graph
      */
     private GrammarGraph parseRule(Rule rule) throws GrammarException {
         GrammarGraph result;
 
         if (rule != null) {
             logger.fine("parseRule: " + rule);
         }
 
         if (rule instanceof RuleAlternatives) {
             result = parseRuleAlternatives((RuleAlternatives) rule);
         } else if (rule instanceof RuleCount) {
             result = parseRuleCount((RuleCount) rule);
         } else if (rule instanceof RuleName) {
             result = parseRuleName((RuleName) rule);
         } else if (rule instanceof RuleSequence) {
             result = parseRuleSequence((RuleSequence) rule);
         } else if (rule instanceof RuleTag) {
             result = parseRuleTag((RuleTag) rule);
         } else if (rule instanceof RuleToken) {
             result = parseRuleToken((RuleToken) rule);
         } else if (rule instanceof RuleParse) {
             throw new IllegalArgumentException(
                     "Unsupported Rule type: RuleParse: " + rule);
         } else {
             throw new IllegalArgumentException("Unsupported Rule type: " + rule);
         }
         return result;
     }
 
 
     /**
      * Parses the given RuleName into a network of GrammarNodes.
      *
      * @param initialRuleName the RuleName rule to parse
      * @return a grammar graph
      */
     private GrammarGraph parseRuleName(RuleName initialRuleName)
             throws GrammarException {
         logger.fine("parseRuleName: " + initialRuleName);
         GrammarGraph result = ruleStack.contains(initialRuleName.getRuleName());
 
         if (result != null) { // its a recursive call
             return result;
         } else {
             result = new GrammarGraph();
             ruleStack.push(initialRuleName.getRuleName(), result);
         }
         RuleName ruleName = ruleGrammar.resolve(initialRuleName);
 
         if (ruleName == RuleName.NULL) {
             result.getStartNode().add(result.getEndNode(), 0.0f);
         } else if (ruleName == RuleName.VOID) {
             // no connection for void
         } else {
             if (ruleName == null) {
                 throw new GrammarException("Can't resolve " + initialRuleName
                         + " g " + initialRuleName.getFullGrammarName());
             }
             RuleGrammar rg = recognizer.getRuleGrammar(ruleName
                     .getFullGrammarName());
             if (rg == null) {
                 throw new GrammarException("Can't resolve grammar name "
                         + ruleName.getFullGrammarName());
             }
 
             Rule rule = rg.getRule(ruleName.getSimpleRuleName());
             if (rule == null) {
                 throw new GrammarException("Can't resolve rule: "
                         + ruleName.getRuleName());
             }
             GrammarGraph ruleResult = parseRule(rule);
             if (result != ruleResult) {
                 result.getStartNode().add(ruleResult.getStartNode(), 0.0f);
                 ruleResult.getEndNode().add(result.getEndNode(), 0.0f);
             }
         }
         ruleStack.pop();
         return result;
     }
 
 
     /**
      * Parses the given RuleCount into a network of GrammarNodes.
      *
      * @param ruleCount the RuleCount object to parse
      * @return a grammar graph
      */
     private GrammarGraph parseRuleCount(RuleCount ruleCount)
             throws GrammarException {
         logger.fine("parseRuleCount: " + ruleCount);
         GrammarGraph result = new GrammarGraph();
         int count = ruleCount.getCount();
         GrammarGraph newNodes = parseRule(ruleCount.getRule());
 
         result.getStartNode().add(newNodes.getStartNode(), 0.0f);
         newNodes.getEndNode().add(result.getEndNode(), 0.0f);
 
         // if this is optional, add a bypass arc
 
         if (count == RuleCount.ZERO_OR_MORE || count == RuleCount.OPTIONAL) {
             result.getStartNode().add(result.getEndNode(), 0.0f);
         }
 
         // if this can possibly occur more than once, add a loopback
 
         if (count == RuleCount.ONCE_OR_MORE || count == RuleCount.ZERO_OR_MORE) {
             newNodes.getEndNode().add(newNodes.getStartNode(), 0.0f);
         }
         return result;
     }
 
 
     /**
      * Parses the given RuleAlternatives into a network of GrammarNodes.
      *
      * @param ruleAlternatives the RuleAlternatives to parse
      * @return a grammar graph
      */
     private GrammarGraph parseRuleAlternatives(RuleAlternatives ruleAlternatives)
             throws GrammarException {
         logger.fine("parseRuleAlternatives: " + ruleAlternatives);
         GrammarGraph result = new GrammarGraph();
 
         Rule[] rules = ruleAlternatives.getRules();
         float[] weights = ruleAlternatives.getWeights();
         normalizeWeights(weights);
 
         // expand each alternative, and connect them in parallel
         for (int i = 0; i < rules.length; i++) {
             Rule rule = rules[i];
             float weight = 0.0f;
             if (weights != null) {
                 weight = weights[i];
             }
             logger.fine("Alternative: " + rule);
             GrammarGraph newNodes = parseRule(rule);
             result.getStartNode().add(newNodes.getStartNode(), weight);
             newNodes.getEndNode().add(result.getEndNode(), 0.0f);
         }
 
         return result;
     }
 
 
     /**
      * Normalize the weights. The weights should always be zero or greater. We need to convert the weights to a log
      * probability.
      *
      * @param weights the weights to normalize
      */
     private void normalizeWeights(float[] weights) {
         if (weights != null) {
             double sum = 0.0;
             for (float weight : weights) {
                 if (weight < 0) {
                     throw new IllegalArgumentException("negative weight");
                 }
                 sum += weight;
             }
             for (int i = 0; i < weights.length; i++) {
                 if (sum == 0.0f) {
                     weights[i] = LogMath.getLogZero();
                 } else {
                     weights[i] = logMath.linearToLog(weights[i] / sum);
                 }
             }
         }
     }
 
 
     /**
      * Parses the given RuleSequence into a network of GrammarNodes.
      *
      * @param ruleSequence the RuleSequence to parse
      * @return the first and last GrammarNodes of the network
      */
     private GrammarGraph parseRuleSequence(RuleSequence ruleSequence)
             throws GrammarException {
 
         GrammarNode startNode = null;
         GrammarNode endNode = null;
         logger.fine("parseRuleSequence: " + ruleSequence);
 
         Rule[] rules = ruleSequence.getRules();
 
         GrammarNode lastGrammarNode = null;
 
         // expand and connect each rule in the sequence serially
         for (int i = 0; i < rules.length; i++) {
             Rule rule = rules[i];
             GrammarGraph newNodes = parseRule(rule);
 
             // first node
             if (i == 0) {
                 startNode = newNodes.getStartNode();
             }
 
             // last node
             if (i == (rules.length - 1)) {
                 endNode = newNodes.getEndNode();
             }
 
             if (i > 0) {
                 lastGrammarNode.add(newNodes.getStartNode(), 0.0f);
             }
             lastGrammarNode = newNodes.getEndNode();
         }
 
         return new GrammarGraph(startNode, endNode);
     }
 
 
     /**
      * Parses the given RuleTag into a network GrammarNodes.
      *
      * @param ruleTag the RuleTag to parse
      * @return the first and last GrammarNodes of the network
      */
     private GrammarGraph parseRuleTag(RuleTag ruleTag) throws GrammarException {
         logger.fine("parseRuleTag: " + ruleTag);
         Rule rule = ruleTag.getRule();
         return parseRule(rule);
     }
 
 
     /**
      * Creates a GrammarNode with the word in the given RuleToken.
      *
      * @param ruleToken the RuleToken that contains the word
      * @return a GrammarNode with the word in the given RuleToken
      */
     private GrammarGraph parseRuleToken(RuleToken ruleToken) {
 
         GrammarNode node = createGrammarNode(ruleToken.getText());
         return new GrammarGraph(node, node);
     }
 
 
     /**
      * Dumps out a grammar exception
      *
      * @param ge the grammar exception
      */
     private void dumpGrammarException(GrammarException ge) {
         System.out.println("Grammar exception " + ge);
         GrammarSyntaxDetail[] details = ge.getDetails();
         if (details != null) {
             for (GrammarSyntaxDetail gsd : details) {
                 System.out.println("Grammar Name: " + gsd.grammarName);
                 System.out.println("Grammar Loc : " + gsd.grammarLocation);
                 System.out.println("Import Name : " + gsd.importName);
                 System.out.println("Line number : " + gsd.lineNumber);
                 System.out.println("char number : " + gsd.charNumber);
                 System.out.println("Rule name   : " + gsd.ruleName);
                 System.out.println("Message     : " + gsd.message);
             }
         }
     }
 
 
     /** Commit changes to all loaded grammars and all changes of grammar since the last commitChange */
     public void commitChanges() throws IOException {
         try {
             if (loadGrammar) {
                 recognizer = new BaseRecognizer();
                 recognizer.allocate();
                 ruleGrammar = recognizer.loadJSGF(baseURL, grammarName);
                 ruleGrammar.setEnabled(true);
                 loadGrammar = false;
             }
 
             recognizer.commitChanges();
             ruleStack = new RuleStack();
             newGrammar();
 
             firstNode = createGrammarNode("<sil>");
             GrammarNode finalNode = createGrammarNode("<sil>");
             finalNode.setFinalNode(true);
 
             // go through each rule and create a network of GrammarNodes
             // for each of them
 
             for (String ruleName : ruleGrammar.listRuleNames()) {
                 if (ruleGrammar.isRulePublic(ruleName)) {
                     String fullName = getFullRuleName(ruleName);
                     GrammarGraph publicRuleGraph = new GrammarGraph();
                     ruleStack.push(fullName, publicRuleGraph);
                     Rule rule = ruleGrammar.getRule(ruleName);
                     GrammarGraph graph = parseRule(rule);
                     ruleStack.pop();
 
                     firstNode.add(publicRuleGraph.getStartNode(), 0.0f);
                     publicRuleGraph.getEndNode().add(finalNode, 0.0f);
                     publicRuleGraph.getStartNode().add(
                         graph.getStartNode(), 0.0f);
                     graph.getEndNode().add(publicRuleGraph.getEndNode(), 0.0f);
                 }
             }
             postProcessGrammar();
             if (logger.isLoggable(Level.FINEST)) {
             	dumpGrammar();
             }
         } catch (EngineException ee) {
             // ee.printStackTrace();
             throw new IOException(ee.toString());
         } catch (GrammarException ge) {
             // ge.printStackTrace();
             dumpGrammarException(ge);
             throw new IOException("GrammarException: " + ge);
         } catch (MalformedURLException mue) {
             throw new IOException("bad base grammar url " + baseURL + ' '
                     + mue);
         }
     }
 
 
     /**
      * Gets the fully resolved rule name
      *
      * @param ruleName the partial name
      * @return the fully resolved name
      * @throws GrammarException
      */
     private String getFullRuleName(String ruleName) throws GrammarException {
         RuleName rname = ruleGrammar.resolve(new RuleName(ruleName));
         return rname.getRuleName();
     }
 
     /** Dumps interesting things about this grammar */
     private void dumpGrammar() {
         System.out.println("Imported rules { ");
         RuleName[] imports = ruleGrammar.listImports();
 
         for (int i = 0; i < imports.length; i++) {
             System.out
                     .println("  Import " + i + ' ' + imports[i].getRuleName());
         }
         System.out.println("}");
 
         System.out.println("Rulenames { ");
         String[] names = ruleGrammar.listRuleNames();
 
         for (int i = 0; i < names.length; i++) {
             System.out.println("  Name " + i + ' ' + names[i]);
         }
         System.out.println("}");
     }
 
 
     /** Represents a graph of grammar nodes. A grammar graph has a single starting node and a single ending node */
     class GrammarGraph {
 
         private GrammarNode startNode;
         private GrammarNode endNode;
 
 
         /**
          * Creates a grammar graph with the given nodes
          *
          * @param startNode the staring node of the graph
          * @param endNode   the ending node of the graph
          */
         GrammarGraph(GrammarNode startNode, GrammarNode endNode) {
             this.startNode = startNode;
             this.endNode = endNode;
         }
 
 
         /** Creates a graph with non-word nodes for the start and ending nodes */
         GrammarGraph() {
             startNode = createGrammarNode(false);
             endNode = createGrammarNode(false);
         }
 
 
         /**
          * Gets the starting node
          *
          * @return the starting node for the graph
          */
         GrammarNode getStartNode() {
             return startNode;
         }
 
 
         /**
          * Gets the ending node
          *
          * @return the ending node for the graph
          */
         GrammarNode getEndNode() {
             return endNode;
         }
     }
 
 
     /** Manages a stack of grammar graphs that can be accessed by grammar name */
     class RuleStack {
 
         private List<String> stack;
         private HashMap<String, GrammarGraph> map;
 
 
         /** Creates a name stack */
         public RuleStack() {
             clear();
         }
 
 
         /** Pushes the grammar graph on the stack */
         public void push(String name, GrammarGraph g) {
             stack.add(0, name);
             map.put(name, g);
         }
 
 
         /** remove the top graph on the stack */
         public void pop() {
             map.remove(stack.remove(0));
         }
 
 
         /**
          * Checks to see if the stack contains a graph with the given name
          *
          * @param name the graph name
          * @return the grammar graph associated with the name if found, otherwise null
          */
         public GrammarGraph contains(String name) {
             if (stack.contains(name)) {
                 return map.get(name);
             } else {
                 return null;
             }
         }
 
 
         /** Clears this name stack */
         public void clear() {
             stack = new LinkedList<String>();
             map = new HashMap<String, GrammarGraph>();
         }
     }
 }
