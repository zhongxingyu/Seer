 package com.whysearchtwice.rexster.extension;
 
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import com.thinkaurelius.titan.core.TitanGraph;
 import com.tinkerpop.blueprints.Graph;
 import com.tinkerpop.blueprints.Vertex;
 import com.tinkerpop.frames.FramedGraph;
 import com.tinkerpop.gremlin.groovy.Gremlin;
 import com.tinkerpop.pipes.Pipe;
 import com.tinkerpop.pipes.util.iterators.SingleIterator;
 import com.tinkerpop.rexster.RexsterResourceContext;
 import com.tinkerpop.rexster.extension.ExtensionDefinition;
 import com.tinkerpop.rexster.extension.ExtensionDescriptor;
 import com.tinkerpop.rexster.extension.ExtensionNaming;
 import com.tinkerpop.rexster.extension.ExtensionPoint;
 import com.tinkerpop.rexster.extension.ExtensionRequestParameter;
 import com.tinkerpop.rexster.extension.ExtensionResponse;
 import com.tinkerpop.rexster.extension.HttpMethod;
 import com.tinkerpop.rexster.extension.RexsterContext;
 import com.whysearchtwice.frames.PageView;
 import com.whysearchtwice.utils.PageViewUtils;
 
 @ExtensionNaming(name = SearchExtension.NAME, namespace = AbstractParsleyExtension.NAMESPACE)
 public class SearchExtension extends AbstractParsleyExtension {
     public static final String NAME = "search";
 
     @ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH, method = HttpMethod.GET)
     @ExtensionDescriptor(description = "Get the results of a search")
     public ExtensionResponse searchVertices(
             @RexsterContext RexsterResourceContext context,
             @RexsterContext Graph graph,
             @ExtensionRequestParameter(name = "userGuid", defaultValue = "", description = "The user to retrieve information for") String userGuid,
             @ExtensionRequestParameter(name = "domain", defaultValue = "", description = "Retrieve pages with this domain") String domain,
             @ExtensionRequestParameter(name = "openRange", defaultValue = "", description = "The start time of a search range (furthest back in history)") String openRange,
             @ExtensionRequestParameter(name = "closeRange", defaultValue = "", description = "The end time of a search range (furthest forward in history)") String closeRange,
             @ExtensionRequestParameter(name = "includeSuccessors", defaultValue = "false", description = "Whether or not to include all successors to a search result") Boolean successors,
             @ExtensionRequestParameter(name = "includeChildren", defaultValue = "false", description = "Whether or not to include all children of a search result") Boolean children) {
 
         // Catch some errors
         long openRangeL;
         long closeRangeL;
         try {
             openRangeL = Long.parseLong(openRange);
             closeRangeL = Long.parseLong(closeRange);
         } catch (Exception e) {
             return ExtensionResponse.error("openRange and closeRange should be convertable to longs");
         }
 
         Vertex user = graph.getVertex(userGuid);
         if (user == null) {
             return ExtensionResponse.error("Invalid userGuid");
         }
 
         // Create the framed graph'
         FramedGraph<TitanGraph> manager = new FramedGraph<TitanGraph>((TitanGraph) graph);
 
         JSONObject results = new JSONObject();
 
         // Build the search
         String gremlinQuery = "_().out('owns').outE('viewed')";
        gremlinQuery += ".has('pageOpenTime', T.gte, " + openRangeL + ").inV";
         gremlinQuery += ".has('pageOpenTime', T.lte, " + closeRangeL + ").inV";
         if (!domain.equals("")) {
             gremlinQuery += ".out('under').has('domain', T.eq, '" + domain + "').back(2)";
         }
 
         // Perform search
         try {
             @SuppressWarnings("unchecked")
             Pipe<Vertex, Vertex> pipe = (Pipe<Vertex, Vertex>) Gremlin.compile(gremlinQuery);
             pipe.setStarts(new SingleIterator<Vertex>(user));
             for (PageView pv : manager.frameVertices(pipe, PageView.class)) {
                 addVertexToList(results, pv, successors, children, openRangeL, closeRangeL);
             }
         } catch (JSONException e) {
             return ExtensionResponse.error("Failed to create search results");
         }
 
         return ExtensionResponse.ok(results);
     }
 
     /**
      * Adds a vertex to the list of search results. Will recurse on children or
      * successors based on parameters.
      * 
      * @param pages
      * @param pv
      * @param successors
      * @param children
      * @throws JSONException
      */
     private void addVertexToList(JSONObject results, PageView pv, boolean successors, boolean children, long openRange, long closeRange) throws JSONException {
         // Check that this PageView is within the time range. If not return
         if (!PageViewUtils.inTimeRange(pv, openRange, closeRange)) {
             return;
         }
 
         // Add this vertex to the results list
         results.accumulate("results", PageViewUtils.asJSON(pv));
 
         // Recursively search if children or successors should be included
         if (successors) {
             for (PageView successor : pv.getSuccessors()) {
                 addVertexToList(results, successor, successors, children, openRange, closeRange);
             }
 
             for (PageView predecessor : pv.getPredecessors()) {
                 addVertexToList(results, predecessor, successors, children, openRange, closeRange);
             }
         }
 
         if (children) {
             for (PageView child : pv.getChildren()) {
                 addVertexToList(results, child, successors, children, openRange, closeRange);
             }
 
             for (PageView parent : pv.getParents()) {
                 addVertexToList(results, parent, successors, children, openRange, closeRange);
             }
         }
     }
 }
