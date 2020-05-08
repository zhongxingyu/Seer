 package de.hszg.atocc.kfgedit.transform.internal;
 
 import de.hszg.atocc.core.util.AbstractXmlTransformationService;
 import de.hszg.atocc.core.util.CollectionHelper;
 import de.hszg.atocc.core.util.SerializationException;
 import de.hszg.atocc.core.util.XmlTransformationException;
 import de.hszg.atocc.core.util.XmlValidationException;
 import de.hszg.atocc.core.util.grammar.Grammar;
 import de.hszg.atocc.core.util.grammar.GrammarService;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.jgrapht.DirectedGraph;
 import org.jgrapht.alg.CycleDetector;
 import org.jgrapht.alg.DijkstraShortestPath;
 import org.jgrapht.graph.DefaultDirectedGraph;
 import org.jgrapht.graph.DefaultEdge;
 
 public class RemoveUnneededChainRules extends AbstractXmlTransformationService {
 
     private GrammarService grammarService;
     private Grammar grammar;
 
     @Override
     protected void transform() throws XmlTransformationException {
         tryToGetRequiredServices();
 
         try {
             validateInput("GRAMMAR");
 
             grammar = grammarService.grammarFrom(getInput());
 
             removeUnneccessaryChainRules();
 
             setOutput(grammarService.grammarToXml(grammar));
         } catch (XmlValidationException e) {
             throw new XmlTransformationException("RemoveUnneededChainRules|INVALID_INPUT", e);
         } catch (SerializationException e) {
             throw new XmlTransformationException("RemoveUnneededChainRules|SERVICE_ERROR", e);
         }
 
         try {
             validateOutput("GRAMMAR");
         } catch (XmlValidationException e) {
             throw new XmlTransformationException("RemoveUnneededChainRules|INVALID_OUTPUT", e);
         }
     }
 
     private void removeUnneccessaryChainRules() {
         removeCycles();
         removeChains();
     }
 
     private void removeCycles() {
         final DirectedGraph<String, DefaultEdge> graph = grammarService.grammarToGraph(grammar);
         final CycleDetector<String, DefaultEdge> detector = new CycleDetector<>(graph);
 
        final Set<String> cycles = detector.findCycles();
 
         if (cycles.isEmpty()) {
             return;
         }
 
         String cycle = cycles.iterator().next();
 
         for (DefaultEdge incomingEdge : graph.incomingEdgesOf(cycle)) {
             final List<DefaultEdge> path = DijkstraShortestPath.findPathBetween(graph, cycle,
                     graph.getEdgeSource(incomingEdge));
 
             if (path != null) {
                 for (DefaultEdge edge : path) {
                     final String nonterminal = graph.getEdgeTarget(edge);
                     grammar.renameNonterminal(nonterminal, cycle);
                 }
             }
         }
 
         grammar.remove(cycle, cycle);
 
         removeCycles();
     }
 
     private void removeChains() {
         final DirectedGraph<String, DefaultEdge> graph = createGraphOnlyWithNonterminals();
 
         final Set<String> nonTerminals = grammar.findNonTerminals();
 
         final List<String> sortedNonterminals = new ArrayList<>();
 
         sortedNonterminals.add(grammar.getStartSymbol());
 
         for (int i = 1; i < nonTerminals.size(); ++i) {
             final Set<DefaultEdge> outgoingEdges = graph.outgoingEdgesOf(sortedNonterminals.get(i-1));
 
             for (DefaultEdge edge : outgoingEdges) {
                 sortedNonterminals.add(graph.getEdgeTarget(edge));
             }
         }
         // System.out.println("Should size: " + nonTerminals.size());
         // System.out.println("Is size: " + sortedNonterminals.size());
         // System.out.println("Rename: " +
         // CollectionHelper.makeString(sortedNonterminals, ","));
         int k = nonTerminals.size() - 1;
 
         for (int i = k - 1; i >= 0; i--) {
             for (int j = i + 1; j <= k; j++) {
                 final String xi = sortedNonterminals.get(i);
                 final String xj = sortedNonterminals.get(j);
                 
                 if (graph.containsEdge(xi, xj)) {
                     grammar.remove(xi, xj);
                     
                     final List<String> rightHandSidesForXj = grammar.getRightHandSidesFor(xj);
                     
                     grammar.appendRule(xi, rightHandSidesForXj);
                 }
             }
         }
     }
 
     private DirectedGraph<String, DefaultEdge> createGraphOnlyWithNonterminals() {
         final DirectedGraph<String, DefaultEdge> graph = grammarService.grammarToGraph(grammar);
 
         final Set<String> nonterminals = grammar.findNonTerminals();
 
         for (String rhs : grammar.getAllRightHandSides()) {
             if (!nonterminals.contains(rhs)) {
                 graph.removeVertex(rhs);
             }
         }
 
         return graph;
     }
 
     private void tryToGetRequiredServices() {
         grammarService = getService(GrammarService.class);
     }
 }
