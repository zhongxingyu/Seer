 package de.deepamehta.plugins.kiezatlas;
 
 import de.deepamehta.plugins.geomaps.service.GeomapsService;
 import de.deepamehta.plugins.facets.service.FacetsService;
 
import de.deepamehta.core.Association;
 import de.deepamehta.core.AssociationDefinition;
 import de.deepamehta.core.RelatedTopic;
 import de.deepamehta.core.ResultSet;
 import de.deepamehta.core.Topic;
import de.deepamehta.core.model.AssociationModel;
import de.deepamehta.core.model.AssociationRoleModel;
import de.deepamehta.core.model.CompositeValue;
 import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.model.TopicRoleModel;
 import de.deepamehta.core.osgi.PluginActivator;
 import de.deepamehta.core.service.ClientState;
 import de.deepamehta.core.service.Directives;
 import de.deepamehta.core.service.PluginService;
 import de.deepamehta.core.service.annotation.ConsumesService;
 import de.deepamehta.core.service.event.PostUpdateTopicListener;
 import de.deepamehta.core.service.event.PreSendTopicListener;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.POST;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.WebApplicationException;
 
 import java.io.InputStream;
import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 
 
 @Path("/site")
 @Consumes("application/json")
 @Produces("application/json")
 public class KiezatlasPlugin extends PluginActivator implements PostUpdateTopicListener, PreSendTopicListener {
 
     // ------------------------------------------------------------------------------------------------------- Constants
 
     // Website-Geomap association
     private static final String WEBSITE_GEOMAP = "dm4.core.association";
     private static final String ROLE_TYPE_WEBSITE = "dm4.core.default";     // Note: used for both associations
     private static final String ROLE_TYPE_GEOMAP = "dm4.core.default";
     // Website-Facet Types association
     private static final String WEBSITE_FACET_TYPES = "dm4.core.association";
     // private static final String ROLE_TYPE_WEBSITE = "dm4.core.default";
     private static final String ROLE_TYPE_FACET_TYPE = "dm4.core.default";
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     private GeomapsService geomapsService;
     private FacetsService facetsService;
 
     private Logger logger = Logger.getLogger(getClass().getName());
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
 
 
 
     // **********************
     // *** Plugin Service ***
     // **********************
 
 
 
     @GET
     @Path("/{url}")
     @Produces("text/html")
     public InputStream launchWebclient() {
         // Note: the template parameters are evaluated at client-side
         try {
             return dms.getPlugin("de.deepamehta.webclient").getResourceAsStream("web/index.html");
         } catch (Exception e) {
             throw new WebApplicationException(e);
         }
     }
 
     @GET
     @Path("/geomap/{geomap_id}")
     public Topic getWebsite(@PathParam("geomap_id") long geomapId) {
         try {
             return dms.getTopic(geomapId, false, null).getRelatedTopic(WEBSITE_GEOMAP,
                 ROLE_TYPE_WEBSITE, ROLE_TYPE_GEOMAP, "dm4.kiezatlas.website", false, false, null);
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("Finding the geomap's website topic failed " +
                 "(geomapId=" + geomapId + ")", e));
         }
     }
 
     @GET
     @Path("/{website_id}/facets")
     public ResultSet<RelatedTopic> getFacetTypes(@PathParam("website_id") long websiteId) {
         try {
             return dms.getTopic(websiteId, false, null).getRelatedTopics(WEBSITE_FACET_TYPES,
                 ROLE_TYPE_WEBSITE, ROLE_TYPE_FACET_TYPE, "dm4.core.topic_type", false, false, 0, null);
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("Finding the website's facet types failed " +
                 "(websiteId=" + websiteId + ")", e));
         }
     }
 
     @GET
     @Path("/geomap/{geomap_id}/objects")
     public Set<Topic> getGeoObjects(@PathParam("geomap_id") long geomapId,
                                     @HeaderParam("Cookie") ClientState clientState) {
         try {
             return fetchGeoObjects(geomapId, clientState);
         } catch (Exception e) {
             throw new WebApplicationException(new RuntimeException("Fetching the geomap's geo objects failed " +
                 "(geomapId=" + geomapId + ")", e));
         }
     }
 
 
 
     // ****************************
     // *** Hook Implementations ***
     // ****************************
 
 
 
     @Override
     @ConsumesService({
         "de.deepamehta.plugins.geomaps.service.GeomapsService",
        "de.deepamehta.plugins.facets.service.FacetsService"
     })
     public void serviceArrived(PluginService service) {
         if (service instanceof GeomapsService) {
             geomapsService = (GeomapsService) service;
         } else if (service instanceof FacetsService) {
             facetsService = (FacetsService) service;
         }
     }
 
     @Override
     public void serviceGone(PluginService service) {
         if (service == geomapsService) {
             geomapsService = null;
         } else if (service == facetsService) {
             facetsService = null;
         }
     }
 
 
 
     // ********************************
     // *** Listener Implementations ***
     // ********************************
 
 
 
     @Override
     public void preSendTopic(Topic topic, ClientState clientState) {
         if (!topic.getTypeUri().equals("dm4.kiezatlas.geo_object")) {
             return;
         }
         //
         ResultSet<RelatedTopic> facetTypes = getFacetTypes(clientState);
         if (facetTypes == null) {
             return;
         }
         //
         enrichWithFacets(topic, facetTypes);
     }
 
     @Override
     public void postUpdateTopic(Topic topic, TopicModel newModel, TopicModel oldModel, ClientState clientState,
                                                                                        Directives directives) {
         if (!topic.getTypeUri().equals("dm4.kiezatlas.geo_object")) {
             return;
         }
         //
         ResultSet<RelatedTopic> facetTypes = getFacetTypes(clientState);
         if (facetTypes == null) {
             return;
         }
         //
         updateFacets(topic, facetTypes, newModel, clientState, directives);
     }
 
     // ------------------------------------------------------------------------------------------------- Private Methods
 
 
 
 
     // === Enrich with facets ===
 
     private void enrichWithFacets(Topic topic, ResultSet<RelatedTopic> facetTypes) {
         for (Topic facetType : facetTypes) {
             String facetTypeUri = facetType.getUri();
             String cardinalityUri = getAssocDef(facetTypeUri).getPartCardinalityUri();
             if (cardinalityUri.equals("dm4.core.one")) {
                 enrichWithSingleFacet(topic, facetTypeUri);
             } else if (cardinalityUri.equals("dm4.core.many")) {
                 enrichWithMultiFacet(topic, facetTypeUri);
             } else {
                 throw new RuntimeException("\"" + cardinalityUri + "\" is an unsupported cardinality URI");
             }
         }
     }
 
     // ---
 
     private void enrichWithSingleFacet(Topic topic, String facetTypeUri) {
         Topic facet = facetsService.getFacet(topic, facetTypeUri);
         // Note: facet is null in 2 cases:
         // 1) The geo object has just been created (no update yet)
         // 2) The geo object has been created outside a geomap and then being revealed in a geomap.
         if (facet == null) {
             logger.info("### Enriching geo object " + topic.getId() + " with its \"" + facetTypeUri + "\" facet " +
                 "ABORTED -- no such facet in DB");
             return;
         }
         //
         logger.info("### Enriching geo object " + topic.getId() + " with its \"" + facetTypeUri + "\" facet (" +
             facet + ")");
         topic.getCompositeValue().put(facet.getTypeUri(), facet.getModel());
     }
 
     private void enrichWithMultiFacet(Topic topic, String facetTypeUri) {
         Set<RelatedTopic> facets = facetsService.getFacets(topic, facetTypeUri);
         logger.info("### Enriching geo object " + topic.getId() + " with its \"" + facetTypeUri + "\" facets (" +
             facets + ")");
         for (Topic facet : facets) {
             topic.getCompositeValue().add(facet.getTypeUri(), facet.getModel());
         }
     }
 
 
 
     // === Update facets ===
 
     private void updateFacets(Topic topic, ResultSet<RelatedTopic> facetTypes, TopicModel newModel,
                                                                        ClientState clientState, Directives directives) {
         for (Topic facetType : facetTypes) {
             String facetTypeUri = facetType.getUri();
             AssociationDefinition assocDef = getAssocDef(facetTypeUri);
            String assocDefUri = assocDef.getUri();
             String cardinalityUri = assocDef.getPartCardinalityUri();
             if (cardinalityUri.equals("dm4.core.one")) {
                 TopicModel facetValue = newModel.getCompositeValue().getTopic(assocDefUri);
                 logger.info("### Storing facet of type \"" + facetTypeUri + "\" for geo object " + topic.getId() +
                     " (facetValue=" + facetValue + ")");
                 facetsService.updateFacet(topic, facetTypeUri, facetValue, clientState, directives);
             } else if (cardinalityUri.equals("dm4.core.many")) {
                 List<TopicModel> facetValues = newModel.getCompositeValue().getTopics(assocDefUri);
                 logger.info("### Storing facets of type \"" + facetTypeUri + "\" for geo object " + topic.getId() +
                     " (facetValues=" + facetValues + ")");
                 facetsService.updateFacets(topic, facetTypeUri, facetValues, clientState, directives);
             } else {
                 throw new RuntimeException("\"" + cardinalityUri + "\" is an unsupported cardinality URI");
             }
         }
     }
 
 
 
     // === Helper ===
 
     /**
      * Determines the facet types of the selected topicmap.
      *
      * @return  The facet types (as a result set, may be empty), or <code>null</code> if
      *              a) the selected topicmap is not a geomap, or
      *              b) the geomap is not associated to a Kiezatlas Website.
      */
     private ResultSet<RelatedTopic> getFacetTypes(ClientState clientState) {
         long topicmapId = clientState.getLong("dm4_topicmap_id");
         //
         if (!isGeomap(topicmapId)) {
             logger.info("### Finding geo object facet types for topicmap " + topicmapId + " ABORTED -- not a geomap");
             return null;
         }
         //
         Topic website = getWebsite(topicmapId);
         if (website == null) {
             logger.info("### Finding geo object facet types for geomap " + topicmapId + " ABORTED -- not part of a " +
                 "Kiezatlas website");
             return null;
         }
         //
         logger.info("### Finding geo object facet types for geomap " + topicmapId);
         return getFacetTypes(website.getId());
     }
 
     private Set<Topic> fetchGeoObjects(long geomapId, ClientState clientState) {
         Set<Topic> geoObjects = new HashSet();
         ResultSet<RelatedTopic> geomapTopics = geomapsService.getGeomapTopics(geomapId);
         for (RelatedTopic topic : geomapTopics) {
             Topic geoTopic = geomapsService.getGeoTopic(topic.getId(), clientState);
             geoObjects.add(geoTopic);
             // ### TODO: optimization. Include only name and address in returned geo objects.
             // ### For the moment the entire objects are returned, including composite values and facets.
         }
         return geoObjects;
     }
 
     // ---
 
     private boolean isGeomap(long topicmapId) {
         Topic topicmap = dms.getTopic(topicmapId, true, null);
         String rendererUri = topicmap.getCompositeValue().getString("dm4.topicmaps.topicmap_renderer_uri");
         return rendererUri.equals("dm4.geomaps.geomap_renderer");
     }
 
     // ### FIXME: there is a copy in FacetsPlugin.java
     private AssociationDefinition getAssocDef(String facetTypeUri) {
         // Note: a facet type has exactly *one* association definition
         return dms.getTopicType(facetTypeUri, null).getAssocDefs().iterator().next();
     }
 }
