 package zemberek3.parser.morphology;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Lists;
 import zemberek3.lexicon.DictionaryItem;
 import zemberek3.lexicon.TurkishDictionaryLoader;
 import zemberek3.lexicon.TurkishSuffixes;
 import zemberek3.lexicon.graph.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class DumbParser {
 
     LexiconGraph graph;
     ArrayListMultimap<String, StemNode> stemNodes = ArrayListMultimap.create();
 
     public DumbParser(LexiconGraph graph) {
         this.graph = graph;
         List<StemNode> stems = graph.getStems();
         for (StemNode stem : stems) {
             stemNodes.put(stem.surfaceForm, stem);
         }
     }
 
     public List<ParseToken> parse(String input) {
         List<StemNode> candidates = new ArrayList<StemNode>();
         for (int i = 1; i < input.length(); i++) {
             String stem = input.substring(0, i);
             if (stemNodes.containsKey(stem)) {
                 candidates.addAll(stemNodes.get(stem));
             }
         }
 
         List<ParseToken> parseTokens = new ArrayList<ParseToken>();
         for (StemNode candidate : candidates) {
             String rest = input.substring(candidate.surfaceForm.length());
             parseTokens.add(new ParseToken(candidate, Lists.<SuffixNode>newArrayList(candidate.getSuffixRootNode()), rest));
         }
         List<ParseToken> result = new ArrayList<ParseToken>();
         matchingSuccessors(parseTokens, result);
         return result;
     }
 
     class ParseToken {
         StemNode stemNode;
         SuffixNode currentNode;
         List<SuffixNode> nodeHistory;
         String rest;
         boolean terminal = false;
 
         ParseToken(StemNode stemNode, List<SuffixNode> nodeHistory, String rest) {
             this.stemNode = stemNode;
             this.currentNode = stemNode.getSuffixRootNode();
             this.nodeHistory = nodeHistory;
             this.rest = rest;
             this.terminal = stemNode.termination == TerminationType.TERMINAL;
         }
 
         ParseToken(StemNode stemNode, SuffixNode suffixNode, List<SuffixNode> nodeHistory, String rest, boolean terminal) {
             this.stemNode = stemNode;
             this.currentNode = stemNode.getSuffixRootNode();
             this.nodeHistory = nodeHistory;
             this.rest = rest;
             this.terminal = terminal;
             this.currentNode = suffixNode;
         }
 
         ParseToken getCopy(SuffixNode node) {
             boolean t = terminal;
             switch (node.termination) {
                 case TERMINAL:
                     t = true;
                     break;
                 case NON_TERMINAL:
                     t = false;
                     break;
             }
             ArrayList<SuffixNode> hist = new ArrayList<SuffixNode>(nodeHistory);
             hist.add(node);
             return new ParseToken(stemNode, node, hist, rest.substring(node.surfaceForm.length()), t);
         }
 
         @Override
         public String toString() {
             return stemNode.surfaceForm + ":" + nodeHistory;
         }
 
         public String asParseString() {
             StringBuilder sb = new StringBuilder("[" + stemNode.surfaceForm + ":" + stemNode.getDictionaryItem().lemma + "-" + stemNode.getDictionaryItem().primaryPos + "]");
             sb.append("[");
             int i = 0;
             for (SuffixNode suffixNode : nodeHistory) {
                 sb.append(suffixNode.getSuffixSet().getSuffix()).append(":").append(suffixNode.surfaceForm);
                 if (i++ < nodeHistory.size() - 1)
                     sb.append(" + ");
             }
             sb.append("]");
             return sb.toString();
         }
     }
 
     void matchingSuccessors(List<ParseToken> tokens, List<ParseToken> finished) {
         List<ParseToken> newtokens = new ArrayList<ParseToken>();
         for (ParseToken token : tokens) {
             List<SuffixNode> matches = new ArrayList<SuffixNode>();
             for (SuffixNode successor : token.currentNode.getSuccessors()) {
                 if (token.rest.startsWith(successor.surfaceForm)) {
                     matches.add(successor);
                 }
             }
             if (matches.size() == 0) {
                 if (token.rest.length() == 0 && token.terminal)
                     finished.add(token);
             }
             for (SuffixNode match : matches) {
                 newtokens.add(token.getCopy(match));
             }
         }
         if (newtokens.size() > 0)
             matchingSuccessors(newtokens, finished);
     }
 
     public static void main(String[] args) throws IOException {
         List<DictionaryItem> items = new TurkishDictionaryLoader().load(new File("test/data/dev-lexicon.txt"));
         TurkishSuffixes suffixes = new TurkishSuffixes();
         LexiconGraph graph = new LexiconGraph(items, suffixes);
         graph.generate();
         DumbParser parser = new DumbParser(graph);
        String[] kelimeler = {"kitabımızın", "elmalarda", "kepekçiğin", "arının", "elmalardakinden","kaba","lütfu","nakde"};
         long start = System.currentTimeMillis();
        final long iteration = 10000;
         for (int i = 0; i < iteration; i++) {
             for (String s : kelimeler) {
                 List<ParseToken> results = parser.parse(s);
                 if (i == 0) {
                     for (ParseToken result : results) {
                         System.out.println(s + " = " + result.asParseString());
                     }
                 }
             }
         }
         long elapsed= System.currentTimeMillis() - start;
         System.out.println("Elapsed:" + elapsed);
         System.out.println("Speed:" + (iteration*1000*kelimeler.length/elapsed));
     }
 }
