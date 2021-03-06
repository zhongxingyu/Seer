 package uk.ac.ebi.gxa.web.export;
 
 import net.sf.json.JSONObject;
 import net.sourceforge.fluxion.ajax.Ajaxified;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.gxa.dao.AtlasDAO;
 import uk.ac.ebi.gxa.dao.LoadStatus;
 import uk.ac.ebi.microarray.atlas.model.LoadDetails;
 
 import javax.servlet.http.HttpSession;
 
 /**
  * An ajaxified bean that exports JSON objects representing load details for Experiment or Gene objects from the backend
  * to clients.  The resulting objects contain details about the experiment/gene accession, whether loading failed or not
  * (note that failedLoad only guarantees loading was attempted and failed, this is false is loading is pending), and the
  * current state of NetCDF generation, analytics calculations, and SOLR index entries.
  * <p/>
  * The general schema looks like:
  * <code><pre>
  *   {
  *     "accession": ****
  *     "failedLoad": true/false
  *     "netcdf": pending/working/failed/done
  *     "analytics": pending/working/failed/done
  *     "index": pending/working/failed/done
  *     "loadType": "experiment"/"gene"
  *   }
  * </pre></code>
  *
  * @author Tony Burdett
  * @date 14-Dec-2009
  */
 @Ajaxified
 public class LoadDetailsExporter {
     private AtlasDAO atlasDAO;
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public void setAtlasDAO(AtlasDAO atlasDAO) {
         this.atlasDAO = atlasDAO;
     }
 
     public AtlasDAO getAtlasDAO() {
         return atlasDAO;
     }
 
     /**
      * Exports a JSONObject containing the load details for the experiment accession specified in the JSONObject 'input'
      * parameter.  The input JSONObject should contain, as a minimum, an "accession" parameter.  Any other information
      * will be ignored.
      *
      * @param session the HTTP session this request was formulated in
      * @param input   the JSONObject representing the request
      * @return a JSON object that is the formulated response representing load details for the requested accession.
      */
     public JSONObject getLoadDetails(HttpSession session, JSONObject input) {
         log.debug("Getting load details for " + input.toString());
 
         // extract accession param
         String accession = input.getString("accession");
 
         // fetch the load details object from the dao
         LoadDetails details = getAtlasDAO().getLoadDetailsForExperimentsByAccession(accession);
 
         // translate LoadDetails into a JSONObject
         JSONObject json = new JSONObject();
         json.put("accession", details.getAccession());
         json.put("failedLoad", details.getStatus().equalsIgnoreCase(LoadStatus.FAILED.toString()));
         json.put("netcdf", details.getNetCDF().toLowerCase());
        json.put("analytics", details.getNetCDF().toLowerCase());
         json.put("index", details.getSearchIndex().toLowerCase());
         json.put("loadType", details.getLoadType().toLowerCase());
 
         log.debug("Response looks like: " + json.toString());
 
         return json;
     }
 }
