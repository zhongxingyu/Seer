 package pt.unl.fct.di.suggestskos.resources;
 
 import java.io.IOException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicLong;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import pt.unl.fct.di.suggestskos.core.Expansions;
 
 import at.ac.univie.mminf.luceneSKOS.skos.SKOSEngine;
 
 import com.google.common.base.Optional;
import com.yammer.dropwizard.logging.Log;
 import com.yammer.metrics.annotation.Timed;
 
 @Path("/expansions")
 @Produces(MediaType.APPLICATION_JSON)
 public class ExpansionsResource {
  private static final Log LOG = Log.forClass(ExpansionsResource.class);
  
   private final AtomicLong counter;
   private final SKOSEngine skosEngine;
   
   public ExpansionsResource(SKOSEngine skosEngine) throws IOException {
     this.counter = new AtomicLong();
     this.skosEngine = skosEngine;
   }
   
   @GET
   @Timed
   public Expansions getExpansions(@QueryParam("q") Optional<String> query,
       @QueryParam("limit") Optional<Integer> limit) throws IOException {
     String term = URLDecoder.decode(query.or("").toLowerCase(), "UTF-8");
     
     List<String> items = new ArrayList<String>();
     
     try {
       String[] conceptURIs = skosEngine.getConcepts(term);
       
       for (String conceptURI : conceptURIs) {
         String[] prefLabels = skosEngine.getPrefLabels(conceptURI);
         items.addAll(Arrays.asList(prefLabels));
         String[] altLabels = skosEngine.getAltLabels(conceptURI);
         items.addAll(Arrays.asList(altLabels));
       }
     } catch (Exception e) {
      LOG.error("Error when accessing SKOS Engine.\n" + e.getMessage());
     }
     items.remove(term);
     
     Expansions exps = new Expansions(counter.incrementAndGet(), items);
     return exps;
   }
 }
