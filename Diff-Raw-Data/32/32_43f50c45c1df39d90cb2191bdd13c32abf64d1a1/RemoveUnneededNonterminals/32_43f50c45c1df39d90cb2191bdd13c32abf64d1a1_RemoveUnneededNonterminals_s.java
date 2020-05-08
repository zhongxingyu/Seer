 package de.hszg.atocc.kfgedit.transform.internal;
 
 import de.hszg.atocc.core.util.AbstractXmlTransformationService;
 import de.hszg.atocc.core.util.GrammarService;
 import de.hszg.atocc.core.util.SerializationException;
 import de.hszg.atocc.core.util.XmlTransformationException;
 import de.hszg.atocc.core.util.XmlValidationException;
 import de.hszg.atocc.core.util.grammar.Grammar;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 public class RemoveUnneededNonterminals extends AbstractXmlTransformationService {
 
     private GrammarService grammarService;
     private Grammar grammar;
 
     private Set<String> neccessaryNonterminals = new HashSet<>();
 
     @Override
     protected void transform() throws XmlTransformationException {
         tryToGetRequiredServices();
 
         try {
             validateInput("GRAMMAR");
 
             grammar = grammarService.grammarFrom(getInput());
 
             removeUnneccessaryNonterminals();
 
             setOutput(grammarService.grammarToXml(grammar));
         } catch (XmlValidationException e) {
             throw new XmlTransformationException("RemoveUnneededNonterminals|INVALID_INPUT", e);
         } catch (SerializationException e) {
             throw new XmlTransformationException("RemoveUnneededNonterminals|SERVICE_ERROR", e);
         }
 
         try {
             validateOutput("GRAMMAR");
         } catch (XmlValidationException e) {
             throw new XmlTransformationException("RemoveUnneededNonterminals|INVALID_OUTPUT", e);
         }
     }
 
     private void removeUnneccessaryNonterminals() {
         findNeccessaryNonterminalsForCondition1();
         findNeccessaryNonterminalsForCondition2a();
 
         // remove unneccessary nonterminals
         final Set<String> nonterminals = grammar.findNonTerminals();
         nonterminals.removeAll(neccessaryNonterminals);
 
         for (String nonterminal : nonterminals) {
             grammar.remove(nonterminal);
             grammar.removeRightHandSideContaining(nonterminal);
         }
 
         findNeccessaryNonterminalsForCondition2b();
     }
 
     private void findNeccessaryNonterminalsForCondition1() {
         // condition 1 - starting symbol
        final String startingSymbol = grammar.getLeftHandSides().get(0);
         neccessaryNonterminals.add(startingSymbol);
     }
 
     private void findNeccessaryNonterminalsForCondition2a() {
         final Set<String> m1 = calculateM1();
 
         Set<String> mi = m1;
         Set<String> miPlus1 = calculateNextM(mi);
 
         while (true) {
             if (mi.equals(miPlus1)) {
                 break;
             }
 
             mi = miPlus1;
             miPlus1 = calculateNextM(mi);
         }
 
         neccessaryNonterminals.addAll(miPlus1);
     }
 
     private void findNeccessaryNonterminalsForCondition2b() {

     }
 
     private Set<String> calculateM1() {
         final Set<String> m1 = new HashSet<>();
 
         final Set<String> nonterminals = grammar.findNonTerminals();
 
         for (String nonterminal : nonterminals) {
             final Collection<String> rightHandSides = grammar.getRightHandSidesFor(nonterminal);
 
             boolean containsOnlyTerminals = true;
 
             for (String rhs : rightHandSides) {
                 for (String part : rhs.split(" ")) {
                     if (nonterminals.contains(part)) {
                         containsOnlyTerminals = false;
                     }
                 }
             }
 
             if (containsOnlyTerminals && !rightHandSides.isEmpty()) {
                 m1.add(nonterminal);
             }
         }
 
         return m1;
     }
 
     private Set<String> calculateNextM(Set<String> lastM) {
         final Set<String> m = new HashSet<>(lastM);
 
         final Set<String> nonterminals = grammar.findNonTerminals();
         final Set<String> terminals = grammar.findTerminals();
 
         for (String nonterminal : nonterminals) {
             final Collection<String> rightHandSides = grammar.getRightHandSidesFor(nonterminal);
 
             boolean containsOnlyTerminalsOrElementsFromLastM = true;
 
             for (String part : rightHandSides) {
                 if (!terminals.contains(part) && !lastM.contains(part)) {
                     containsOnlyTerminalsOrElementsFromLastM = false;
                 }
             }
 
             if (containsOnlyTerminalsOrElementsFromLastM && !rightHandSides.isEmpty()) {
                 m.add(nonterminal);
             }
         }
 
         return m;
     }
 
     private void tryToGetRequiredServices() {
         grammarService = getService(GrammarService.class);
     }
 
 }
