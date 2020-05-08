 package de.hszg.atocc.kfgedit.transform.internal;
 
 import de.hszg.atocc.core.util.AbstractXmlTransformationService;
 import de.hszg.atocc.core.util.CollectionHelper;
 import de.hszg.atocc.core.util.SerializationException;
 import de.hszg.atocc.core.util.XmlTransformationException;
 import de.hszg.atocc.core.util.XmlValidationException;
 import de.hszg.atocc.core.util.automaton.Automaton;
 import de.hszg.atocc.core.util.grammar.Grammar;
 import de.hszg.atocc.core.util.grammar.GrammarService;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class RemoveEpsilonRules extends AbstractXmlTransformationService {
 
     private static final String SPACE = " ";
     private GrammarService grammarService;
     private Grammar grammar;
 
     @Override
     protected void transform() throws XmlTransformationException {
 
         tryToGetRequiredServices();
 
         try {
             validateInput("GRAMMAR");
 
             grammar = grammarService.grammarFrom(getInput());
 
             final Set<String> nonTerminalsDerivableToEpsilon = grammarService
                     .findNonTerminalsDerivableToEpsilon(grammar);
 
             for (String nonTerminal : nonTerminalsDerivableToEpsilon) {
                 grammar.remove(nonTerminal, Automaton.EPSILON);
             }
 
             final Map<String, List<String>> rulesToAdd = new HashMap<>();
 
             for (String nonTerminal : nonTerminalsDerivableToEpsilon) {
                 for (String lhs : grammar.getLeftHandSides()) {
                     final Collection<String> rightHandSides = grammar.getRightHandSidesFor(lhs);
 
                     for (String rhs : rightHandSides) {
                         List<String> parts = Arrays.asList(rhs.split(SPACE));
 
                         if (parts.contains(nonTerminal)) {
                             final int positionOfNonTerminal = findPositionOf(nonTerminal, parts);
                             final List<String> beta = parts.subList(0, positionOfNonTerminal);
                             final List<String> gammar = parts.subList(positionOfNonTerminal + 1,
                                     parts.size());
 
                             if (beta.isEmpty() && gammar.isEmpty()) {
                                 continue;
                             }
 
                             if (!rulesToAdd.containsKey(lhs)) {
                                 rulesToAdd.put(lhs, new LinkedList<String>());
                             }
 
                             rulesToAdd.get(lhs).add(
                                     String.format("%s %s",
                                             CollectionHelper.makeString(beta, SPACE),
                                             CollectionHelper.makeString(gammar, SPACE)).trim());
                         }
                     }
                 }
             }
 
             for (String lhs : rulesToAdd.keySet()) {
                 grammar.appendRule(lhs, rulesToAdd.get(lhs));
             }
 
             setOutput(grammarService.grammarToXml(grammar));
         } catch (XmlValidationException e) {
             throw new XmlTransformationException("RemoveEpsilonRules|INVALID_INPUT", e);
         } catch (SerializationException e) {
             throw new XmlTransformationException("RemoveEpsilonRules|SERVICE_ERROR", e);
         }
 
         try {
             validateOutput("GRAMMAR");
         } catch (XmlValidationException e) {
             throw new XmlTransformationException("RemoveEpsilonRules|INVALID_OUTPUT", e);
         }
 
     }
 
     private int findPositionOf(String nonTerminal, List<String> parts) {
         for (int i = 0; i < parts.size(); ++i) {
             final String part = parts.get(i);
 
             if (part.equals(nonTerminal)) {
                 return i;
             }
         }
 
         throw new RuntimeException("Should not be reached");
     }
 
     private void tryToGetRequiredServices() {
         grammarService = getService(GrammarService.class);
         // xmlUtils = createService(XmlUtilService.class);
         // xmlValidator = getService(XmlValidatorService.class);
     }
 
 }
