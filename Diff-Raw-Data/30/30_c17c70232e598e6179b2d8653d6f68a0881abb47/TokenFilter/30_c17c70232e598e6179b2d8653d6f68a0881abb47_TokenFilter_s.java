 package com.googlecode.whatswrong;
 
 import java.util.*;
 
 /**
  * A Tokenfilter removes certain properties from each token and removes tokens that do not contain certain property
  * values. The filter also removes all edges that were connecting one or more removed tokens.
  *
  * @author Sebastian Riedel
  */
 public class TokenFilter implements NLPInstanceFilter {
 
     /**
      * The set of properties we should not see.
      */
     private HashSet<TokenProperty> forbiddenProperties = new HashSet<TokenProperty>();
 
     /**
      * A token needs to have at least one property value contained in this set (if {@link
      * com.googlecode.whatswrong.TokenFilter#wholeWord} is true) or needs to have one value that contains a string in
      * this set (otherwise).
      */
     private HashSet<String> allowedStrings = new HashSet<String>();
 
     /**
      * Should tokens be allowed only if they have a property value that equals one of the allowed strings or is it
      * sufficient if one value contains one of the allowed strings.
      */
     private boolean wholeWord = false;
 
     /**
      * Creates a new TokenFilter.
      */
     public TokenFilter() {
     }
 
     /**
      * Are tokens allowed only if they have a property value that equals one of the allowed strings or is it sufficient
      * if one value contains one of the allowed strings.
      *
      * @return true iff tokens are allowed based on exact matches with allowed strings, false otherwise.
      */
 
     public boolean isWholeWord() {
         return wholeWord;
     }
 
     /**
      * Should tokens be allowed only if they have a property value that equals one of the allowed strings or is it
      * sufficient if one value contains one of the allowed strings.
      *
      * @param wholeWord true iff tokens should be allowed based on exact matches with allowed strings, false otherwise.
      */
     public void setWholeWord(final boolean wholeWord) {
         this.wholeWord = wholeWord;
     }
 
     /**
      * Add a an allowed property value.
      *
      * @param string the allowed property value.
      */
     public void addAllowedString(final String string) {
         allowedStrings.add(string);
     }
 
     /**
      * Remove all allowed strings. In this state the filter allows all tokens.
      */
     public void clearAllowedStrings() {
         allowedStrings.clear();
     }
 
     /**
      * Add a property that is forbidden so that the corresponding values are removed from each token.
      *
      * @param name the name of the property to forbid.
      */
     public void addForbiddenProperty(String name) {
         forbiddenProperties.add(new TokenProperty(name));
     }
 
     /**
      * Remove a property that is forbidden so that the corresponding values shown again.
      *
      * @param name the name of the property to show again.
      */
     public void removeForbiddenProperty(String name) {
         forbiddenProperties.remove(new TokenProperty(name));
     }
 
     /**
      * Returns an unmodifiable view on the set of all allowed token properties.
      *
      * @return an unmodifiable view on the set of all allowed token properties.
      */
     public Set<TokenProperty> getForbiddenTokenProperties() {
         return Collections.unmodifiableSet(forbiddenProperties);
     }
 
     /**
      * Filter a set of tokens by removing property values and individual tokens according to the set of allowed strings
      * and forbidden properties.
      *
      * @param original the original set of tokens.
      * @return the filtered set of tokens.
      */
     public List<Token> filterTokens(Collection<Token> original) {
         ArrayList<Token> result = new ArrayList<Token>(original.size());
         for (Token vertex : original) {
             Token copy = new Token(vertex.getIndex());
             for (TokenProperty property : vertex.getPropertyTypes()) {
                 if (!forbiddenProperties.contains(property))
                     copy.addProperty(property, vertex.getProperty(property));
             }
             result.add(copy);
         }
         return result;
     }
 
     /**
      * Filter an NLP instance by first filtering the tokens and then removing edges that have tokens which were filtered
      * out.
      *
      * @param original the original nlp instance.
      * @return the filtered nlp instance.
      * @see NLPInstanceFilter#filter(NLPInstance)
      */
     public NLPInstance filter(NLPInstance original) {
 
         if (allowedStrings.size() > 0) {
             //first filter out tokens not containing allowed strings
             HashMap<Token, Token> old2new = new HashMap<Token, Token>();
             HashMap<Token, Token> new2old = new HashMap<Token, Token>();
             ArrayList<Token> tokens = new ArrayList<Token>();
             main:
             for (Token t : original.getTokens()) {
                 for (TokenProperty property : t.getPropertyTypes()) {
                     String prop = t.getProperty(property);
                     for (String allowed : allowedStrings)
                         //todo: this can surely be implemented in a nicer way (e.g. no reparsing of interval)
                         if (property.getName().equals("Index") && allowed.matches("\\d+-\\d+")) {
                             String[] split = allowed.split("[-]");
                             int from = Integer.parseInt(split[0]);
                             int to = Integer.parseInt(split[1]);
                             for (int i = from; i <= to; ++i)
                                 if (prop.equals(String.valueOf(i))) {
                                     Token newVertex = new Token(tokens.size());
                                     newVertex.merge(t);
                                     tokens.add(newVertex);
                                     old2new.put(t, newVertex);
                                     new2old.put(newVertex, t);
                                     continue main;
                                 }
                         } else if (wholeWord ? prop.equals(allowed) : prop.contains(allowed)) {
                             Token newVertex = new Token(tokens.size());
                             newVertex.merge(t);
                             tokens.add(newVertex);
                             old2new.put(t, newVertex);
                             new2old.put(newVertex, t);
                             continue main;
                         }
                 }
             }
             //update edges and remove those that have vertices not in the new vertex set
             ArrayList<Edge> edges = new ArrayList<Edge>();
             for (Edge e : original.getEdges()) {
                 Token newFrom = old2new.get(e.getFrom());
                 Token newTo = old2new.get(e.getTo());
                 if (newFrom == null || newTo == null) continue;
                edges.add(new Edge(newFrom, newTo, e.getLabel(), e.getType(), e.getRenderType()));
             }
             //find new split points (have to be changed because new instance
             //has new token sequence
             ArrayList<Integer> splitPoints = new ArrayList<Integer>();
             int newTokenIndex = 0;
             for (Integer oldSplitPoint : original.getSplitPoints()) {
 
                 Token newToken = tokens.get(newTokenIndex);
                 Token oldToken = new2old.get(newToken);
                 while (newTokenIndex + 1 < tokens.size()
                     && oldToken.getIndex() < oldSplitPoint) {
                     ++newTokenIndex;
                     newToken = tokens.get(newTokenIndex);
                     oldToken = new2old.get(newToken);
                 }
                 splitPoints.add(newTokenIndex);
             }
 
             return new NLPInstance(filterTokens(tokens),
                 edges, original.getRenderType(), splitPoints);
 
         } else {
             List<Token> filteredTokens = filterTokens(original.getTokens());
             return new NLPInstance(filteredTokens, original.getEdges(),
                 original.getRenderType(), original.getSplitPoints());
         }
     }
 
 }
