 package com.googlecode.whatswrong;
 
 import java.util.*;
 
 /**
  * An EdgeTokenFilter filters out edges based on the properties of their tokens. For example, we can filter out all
  * edges that do not contain at least one token with the word "blah". The filter can also be configured to filter out
  * all edges which are not on a path between tokens with certain properties. For example, we can filter out all edges
  * that are not on the paths between a token with word "blah" and a token with word "blub".
  * <p/>
  * <p>This filter can also filter out the tokens for which all edges have been filtered out via the edge filtering
  * process. This mode is called "collapsing" because the graph is collapsed to contain only connected components.
  * <p/>
  * <p>Note that if no allowed property values are defined ({@link com.googlecode.whatswrong.EdgeTokenFilter#addAllowedProperty(String)})
  * then the filter does nothing and keeps all edges.
  *
  * @author Sebastian Riedel
  */
 public class EdgeTokenFilter implements NLPInstanceFilter {
 
     /**
      * Set of property values that one of the tokens of an edge has to have so that the edge is not going to be filtered
      * out.
      */
     private HashSet<String> allowedProperties = new HashSet<String>();
     /**
      * Should we only allow edges that are on the path of tokens that have the allowed properties.
      */
     private boolean usePaths = false;
 
     /**
      * If active this property will cause the filter to filter out all tokens for which all edges where filtered out in
      * the edge filtering step.
      */
     private boolean collaps = false;
 
     /**
      * If true at least one edge tokens must contain at least one property value that matches one of the allowed
      * properties. If false it sufficient for the property values to contain an allowed property as substring.
      */
     private boolean wholeWords = false;
 
     /**
      * Creates a new filter with the given allowed property values.
      *
      * @param allowedProperties A var array of allowed property values. An Edge will be filtered out if none of its tokens
      *                          has a property with an allowed property value (or a property value that contains an
      *                          allowed value, if {@link com.googlecode.whatswrong.EdgeTokenFilter#isWholeWords()} is
      *                          false).
      */
     public EdgeTokenFilter(final String... allowedProperties) {
         this.allowedProperties.addAll(Arrays.asList(allowedProperties));
     }
 
     /**
      * Creates a new filter with the given allowed property values.
      *
      * @param allowedPropertyValues A set of allowed property values. An Edge will be filtered out if none of its tokens
      *                              has a property with an allowed property value (or a property value that contains an
      *                              allowed value, if {@link com.googlecode.whatswrong.EdgeTokenFilter#isWholeWords()} is
      *                              false).
      */
     public EdgeTokenFilter(final Set<String> allowedPropertyValues) {
         this.allowedProperties.addAll(allowedPropertyValues);
     }
 
     /**
      * If active this property will cause the filter to filter out all tokens for which all edges where filtered out in
      * the edge filtering step.
      *
      * @return true if the filter collapses the graph and removes tokens without edge.
      */
     public boolean isCollaps() {
         return collaps;
     }
 
     /**
      * If active this property will cause the filter to filter out all tokens for which all edges where filtered out in
      * the edge filtering step.
      *
      * @param collaps true if the filter should collapse the graph and remove tokens without edge.
      */
     public void setCollaps(final boolean collaps) {
         this.collaps = collaps;
     }
 
     /**
      * Usually the filter allows all edges that have tokens with allowed properties. However, if it "uses paths" an edge
      * will only be allowed if it is on a path between two tokens with allowed properties. This also means that if there
      * is only one token with allowed properties all edges will be filtered out.
      *
      * @return true if the filter uses paths.
      */
     public boolean isUsePaths() {
         return usePaths;
     }
 
     /**
      * Sets whether the filter uses paths.
      *
      * @param usePaths should the filter use paths.
      * @see EdgeTokenFilter#isUsePaths()
      */
     public void setUsePaths(final boolean usePaths) {
         this.usePaths = usePaths;
     }
 
     /**
      * Adds an allowed property value. An Edge must have a least one token with at least one property value that either
      * matches one of the allowed property values or contains one of them, depending on {@link
      * EdgeTokenFilter#isWholeWords()}.
      *
      * @param propertyValue the property value to allow.
      */
     public void addAllowedProperty(final String propertyValue) {
         allowedProperties.add(propertyValue);
     }
 
     /**
      * Remove an allowed property value.
      *
      * @param propertyValue the property value to remove from the set of allowed property values.
      */
     public void removeAllowedProperty(final String propertyValue) {
         allowedProperties.remove(propertyValue);
     }
 
 
     /**
      * Removes all allowed words. Note that if no allowed words are specified the filter changes it's behaviour and allows
      * all edges.
      */
     public void clear() {
         allowedProperties.clear();
     }
 
     /**
      * A Path represents a path of edges. Right it is simply a HashSet of edges.
      */
     private static class Path extends HashSet<Edge> {
 
     }
 
     /**
      * A Paths object is a mapping from token pairs to all paths between the corresponding tokens.
      */
     private static class Paths
         extends HashMap<Token, HashMap<Token, HashSet<Path>>> {
 
         /**
          * Returns the set of paths between the given tokens.
          *
          * @param from the start token.
          * @param to   the end token.
          * @return the set of paths between the tokens.
          */
         public Set<Path> getPaths(Token from, Token to) {
             HashMap<Token, HashSet<Path>> paths = get(from);
             return paths == null ? null : paths.get(to);
         }
 
         /**
          * Get all tokens with paths that end in this token and start at the given from token.
          *
          * @param from the token the paths should start at.
          * @return all tokens that have a paths that end in it and start at the provided token.
          */
         public Set<Token> getTos(Token from) {
             HashMap<Token, HashSet<Path>> result = get(from);
             return result != null ? result.keySet() : new HashSet<Token>();
         }
 
         /**
          * Adds a path between the given tokens.
          *
          * @param from the start token.
          * @param to   the end token.
          * @param path the path to add.
          */
         public void addPath(Token from, Token to, Path path) {
             HashMap<Token, HashSet<Path>> paths = get(from);
             if (paths == null) {
                 paths = new HashMap<Token, HashSet<Path>>();
                 put(from, paths);
             }
             HashSet<Path> set = paths.get(to);
             if (set == null) {
                 set = new HashSet<Path>();
                 paths.put(to, set);
             }
             set.add(path);
         }
 
 
     }
 
     /**
      * Calculates all paths between all tokens of the provided edges.
      *
      * @param edges the edges (graph) to use for getting all paths.
      * @return all paths defined through the provided edges.
      */
     private Paths calculatePaths(Collection<Edge> edges) {
         List<Paths> pathsPerLength = new ArrayList<Paths>();
 
         Paths paths = new Paths();
         //initialize
         for (Edge edge : edges) {
             Path path = new Path();
             path.add(edge);
             paths.addPath(edge.getFrom(), edge.getTo(), path);
             paths.addPath(edge.getTo(), edge.getFrom(), path);
         }
         pathsPerLength.add(paths);
         Paths previous = paths;
         Paths first = paths;
         do {
             paths = new Paths();
             //go over each paths of the previous length and increase their size by one
             for (Token from : previous.keySet())
                 for (Token over : previous.getTos(from))
                     for (Token to : first.getTos(over))
                         for (Path path1 : previous.getPaths(from, over))
                             for (Path path2 : first.getPaths(over, to)) {
                                 if (!path1.containsAll(path2) &&
                                     path1.iterator().next().getTypePrefix().equals(path2.iterator().next().getTypePrefix())) {
                                     Path path = new Path();
                                     path.addAll(path1);
                                     path.addAll(path2);
                                     paths.addPath(from, to, path);
                                     paths.addPath(to, from, path);
                                 }
                             }
             if (!paths.isEmpty()) pathsPerLength.add(paths);
             previous = paths;
         } while (paths.size() > 0);
         Paths result = new Paths();
         for (Paths p : pathsPerLength)
             for (Token from : p.keySet())
                 for (Token to : p.getTos(from))
                     for (Path path : p.getPaths(from, to))
                         result.addPath(from, to, path);
         return result;
     }
 
 
     /**
      * If true at least one edge tokens must contain at least one property value that matches one of the allowed
      * properties. If false it sufficient for the property values to contain an allowed property as substring.
      *
      * @return whether property values need to exactly match the allowed properties or can contain them as a substring.
      */
     public boolean isWholeWords() {
         return wholeWords;
     }
 
     /**
      * Sets whether the filter should check for whole word matches of properties.
      *
      * @param wholeWords true iff the filter should check for whold words.
      * @see EdgeTokenFilter#isWholeWords()
      */
     public void setWholeWords(final boolean wholeWords) {
         this.wholeWords = wholeWords;
     }
 
     /**
      * Filters out all edges that do not have at least one token with an allowed property value. If the set of allowed
      * property values is empty this method just returns the original set and does nothing.
      *
      * @param original the input set of edges.
      * @return the filtered out set of edges.
      */
     public Collection<Edge> filterEdges(final Collection<Edge> original) {
         if (allowedProperties.size() == 0) return original;
         if (usePaths) {
             Paths paths = calculatePaths(original);
             HashSet<Edge> result = new HashSet<Edge>();
             for (Token from : paths.keySet())
                 if (from.propertiesContain(allowedProperties, wholeWords))
                     for (Token to : paths.getTos(from))
                         if (to.propertiesContain(allowedProperties, wholeWords))
                             for (Path path : paths.getPaths(from, to))
                                 result.addAll(path);
             return result;
         } else {
             ArrayList<Edge> result = new ArrayList<Edge>(original.size());
             for (Edge edge : original) {
                 if (edge.getFrom().propertiesContain(allowedProperties, wholeWords) ||
                     edge.getTo().propertiesContain(allowedProperties, wholeWords))
                     result.add(edge);
             }
             return result;
         }
 
     }
 
     /**
      * Returns whether the given value is an allowed property value.
      *
      * @param propertyValue the value to test.
      * @return whether the given value is an allowed property value.
      */
     public boolean allows(final String propertyValue) {
         return allowedProperties.contains(propertyValue);
     }
 
     /**
      * First filters out edges and then filters out tokens without edges if {@link EdgeTokenFilter#isCollaps()} is true.
      *
      * @param original the original nlp instance.
      * @return the filtered instance.
      * @see NLPInstanceFilter#filter(NLPInstance)
      */
     public NLPInstance filter(NLPInstance original) {
         Collection<Edge> edges = filterEdges(original.getEdges());
         if (!collaps)
             return new NLPInstance(original.getTokens(), edges,
                 original.getRenderType(), original.getSplitPoints());
         else {
             HashSet<Token> tokens = new HashSet<Token>();
             for (Edge e : edges) {
                 if (e.getRenderType() == Edge.RenderType.dependency) {
                     tokens.add(e.getFrom());
                     tokens.add(e.getTo());
                 } else if (e.getRenderType() == Edge.RenderType.span) {
                     for (int i = e.getFrom().getIndex(); i <= e.getTo().getIndex(); ++i) {
                         tokens.add(original.getToken(i));
                     }
                 }
             }
             ArrayList<Token> sorted = new ArrayList<Token>(tokens);
             Collections.sort(sorted, new Comparator<Token>() {
                 /**
                  * Compares tokens by index
                  * @param token the first token
                  * @param tokenVertex1 the second token.
                  * @return an integer indicating whether the first token is before
                  * or after the second one in terms of their indices.
                  */
                 public int compare(Token token, Token tokenVertex1) {
                     return token.getIndex() - tokenVertex1.getIndex();
                 }
             });
             ArrayList<Token> updatedTokens = new ArrayList<Token>();
             HashMap<Token, Token> old2new = new HashMap<Token, Token>();
             HashMap<Token, Token> new2old = new HashMap<Token, Token>();
             for (Token t : sorted) {
                 Token newToken = new Token(updatedTokens.size());
                 newToken.merge(original.getTokens().get(t.getIndex()));
                 old2new.put(t, newToken);
                 new2old.put(newToken, t);
                 updatedTokens.add(newToken);
             }
 
             HashSet<Edge> updatedEdges = new HashSet<Edge>();
             for (Edge e : edges) {
                 updatedEdges.add(new Edge(old2new.get(e.getFrom()),
                    old2new.get(e.getTo()), e.getLabel(), e.getNote(), e.getType(),
                     e.getRenderType()));
             }
             //find new split points
             ArrayList<Integer> splitPoints = new ArrayList<Integer>();
             int newTokenIndex = 0;
             for (Integer oldSplitPoint : original.getSplitPoints()) {
 
                 Token newToken = updatedTokens.get(newTokenIndex);
                 Token oldToken = new2old.get(newToken);
                 while (newTokenIndex + 1 < tokens.size()
                     && oldToken.getIndex() < oldSplitPoint) {
                     ++newTokenIndex;
                     newToken = updatedTokens.get(newTokenIndex);
                     oldToken = new2old.get(newToken);
                 }
                 splitPoints.add(newTokenIndex);
             }
 
             return new NLPInstance(updatedTokens, updatedEdges,
                 original.getRenderType(), splitPoints);
         }
     }
 
 }
