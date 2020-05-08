 package org.triple_brain.service.resources;
 
 import com.google.inject.Inject;
import org.triple_brain.module.model.graph.GraphTransactional;
 import org.triple_brain.module.search.GraphIndexer;
 
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.Response;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class AdminResource {
 
     @Inject
     GraphIndexer graphIndexer;
 
     @Path("reindex")
    @GraphTransactional
     @POST
     public Response reindexAll(){
         graphIndexer.indexWholeGraph();
         return Response.ok().build();
     }
 
 
 }
