 package de.hszg.atocc.core.util.internal;
 
 import de.hszg.atocc.core.util.CollectionHelper;
 import de.hszg.atocc.core.util.GrammarService;
 import de.hszg.atocc.core.util.SerializationException;
 import de.hszg.atocc.core.util.SetService;
 import de.hszg.atocc.core.util.XmlUtilService;
 import de.hszg.atocc.core.util.grammar.Grammar;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public final class GrammarServiceImpl implements GrammarService {
 
     private static final String SPACE = " ";
     private static final String EPSILON = "EPSILON";
     private XmlUtilService xmlUtils;
     private SetService setService;
 
     @Override
     public Grammar grammarFrom(Document document) throws SerializationException {
         final Grammar grammar = new Grammar();
 
         final String grammarText = document.getDocumentElement().getTextContent().trim();
 
         final String[] lines = grammarText.split("\\r?\\n");
 
         for (String line : lines) {
             final String[] sides = line.split("->");
 
             final String lhs = sides[0];
 
             for (String rhs : sides[1].split("\\|")) {
                 grammar.appendRule(lhs.trim(), rhs.trim());
             }
         }
 
         return grammar;
     }
 
     @Override
     public Document grammarToXml(Grammar grammar) throws SerializationException {
         final Document grammarDocument = xmlUtils.createEmptyDocument();
 
         final Element grammarElement = grammarDocument.createElement("grammar");
         grammarElement.setTextContent(grammar.toString());
         grammarDocument.appendChild(grammarElement);
 
         return grammarDocument;
     }
 
     @Override
     public Set<String> calculateFirstSetFor(String terminalOrNonterminal, Grammar grammar) {
         final Set<String> terminals = grammar.findTerminals();
 
         return firstSetFor(terminalOrNonterminal, terminals, grammar);
     }
 
     @Override
     public Set<String> calculateFollowSetFor(String x, Grammar grammar) {
         if (grammar.findTerminals().contains(x)) {
             throw new IllegalArgumentException(x + " is not a non-terminal in the given grammar");
         }
 
         final Set<String> rulesContainingX = grammar.findRulesContaining(x, false);
 
         final Set<String> followSet = new HashSet<>();
 
         for (String rightHandSide : rulesContainingX) {
             final List<String> parts = Arrays.asList(rightHandSide.split(SPACE));
 
             final int indexOfX = parts.indexOf(x);
 
             String beta = "";
 
             if (indexOfX < (parts.size() - 1)) {
                 beta = parts.get(indexOfX + 1);
             }
 
             final Set<String> betaFirst = calculateFirstSetFor(beta, grammar);
 
             final boolean containsEpsilon = betaFirst.contains(EPSILON);
 
             betaFirst.remove(EPSILON);
 
             followSet.addAll(betaFirst);
 
             if (containsEpsilon || "".equals(beta)) {
                 final Set<String> leftHandSides = grammar.findLeftHandSidesFor(rightHandSide);
 
                 for (String a : leftHandSides) {
                     followSet.addAll(calculateFollowSetFor(a, grammar));
                 }
 
             }
 
         }
 
         return followSet;
     }
 
     @Override
     public Set<String> findNonTerminalsDerivableToEpsilon(Grammar grammar) {
         final Set<String> nonTerminals = new HashSet<>();
 
         final Map<String, List<String>> rules = grammar.getRules();
 
         for (int oldSize = 0; oldSize < nonTerminals.size(); oldSize = nonTerminals.size()) {
 
             final Set<String> lhsToRemove = new HashSet<>();
 
             for (Entry<String, List<String>> entry : rules.entrySet()) {
                 for (String rhs : entry.getValue()) {
                    if (rhs.contains(EPSILON)) {
                         lhsToRemove.add(entry.getKey());
                         nonTerminals.add(entry.getKey());
                     }
                 }
             }
 
             for (String lhs : lhsToRemove) {
                 rules.remove(lhs);
             }
         }
 
         return nonTerminals;
     }
 
     public synchronized void setXmlUtilService(XmlUtilService service) {
         xmlUtils = service;
     }
 
     public synchronized void unsetXmlUtilService(XmlUtilService service) {
         if (xmlUtils == service) {
             xmlUtils = null;
         }
     }
 
     public synchronized void setSetService(SetService service) {
         setService = service;
     }
 
     public synchronized void unsetSetService(SetService service) {
         if (setService == service) {
             setService = null;
         }
     }
 
     private Set<String> firstSetFor(String terminalOrNonterminal, Set<String> terminals,
             Grammar grammar) {
         final String[] parts = terminalOrNonterminal.split(SPACE);
         final String alpha = parts[0];
 
         Set<String> firstSet = null;
 
         if (terminalOrNonterminal.trim().isEmpty()) {
             firstSet = firstCase();
         } else if (EPSILON.equals(terminalOrNonterminal)) {
             firstSet = thirdCase();
         } else if (terminals.contains(alpha)) {
             firstSet = secondCase(alpha);
         } else if (parts.length == 1 && !terminals.contains(alpha)) {
             firstSet = fourthCase(terminals, grammar, alpha);
         } else if (parts.length >= 1) {
             firstSet = fifthCase(terminals, grammar, alpha);
 
             if (firstSet.contains(EPSILON)) {
                 firstSet = sixthAndSeventhCase(grammar, parts, alpha);
             }
         }
 
         return firstSet;
     }
 
     private Set<String> sixthAndSeventhCase(Grammar grammar, final String[] parts,
             final String alpha) {
         final Set<String> terminals = grammar.findTerminals();
 
         // EPSILON in FIRST(Yi)
         boolean epsilonInEveryFirstSet = true;
 
         for (String part : parts) {
             // prevent infinite loop
             if (!firstSetFor(part, terminals, grammar).contains(EPSILON)) {
                 epsilonInEveryFirstSet = false;
                 break;
             }
         }
 
         final String[] restArray = Arrays.copyOfRange(parts, 1, parts.length);
         final String rest = CollectionHelper.makeString(Arrays.asList(restArray), SPACE);
 
         final Set<String> firstSet = setService.unionOf(firstSetFor(alpha, terminals, grammar),
                 firstSetFor(rest, terminals, grammar));
 
         if (!epsilonInEveryFirstSet) {
             firstSet.remove(EPSILON);
         }
 
         return firstSet;
     }
 
     private Set<String> fifthCase(Set<String> terminals, Grammar grammar, final String alpha) {
         return firstSetFor(alpha, terminals, grammar);
     }
 
     private Set<String> fourthCase(Set<String> terminals, Grammar grammar, final String alpha) {
         final Collection<String> betas = grammar.getRightHandSidesFor(alpha);
 
         final Set<String> firstSet = new HashSet<>();
 
         for (String beta : betas) {
             if (!beta.split(SPACE)[0].equals(alpha)) {
                 firstSet.addAll(firstSetFor(beta, terminals, grammar));
             }
         }
 
         return firstSet;
     }
 
     private Set<String> secondCase(final String alpha) {
         return setService.createSetWith(alpha);
     }
 
     private Set<String> thirdCase() {
         return setService.createSetWith(EPSILON);
     }
 
     private Set<String> firstCase() {
         return setService.createEmptySet(String.class);
     }
 
 }
