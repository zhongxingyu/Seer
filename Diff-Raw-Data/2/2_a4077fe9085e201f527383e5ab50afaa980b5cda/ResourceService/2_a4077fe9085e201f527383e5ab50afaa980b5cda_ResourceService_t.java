 package org.deepamehta.plugins.eduzen.service;
 
 
 import de.deepamehta.core.Topic;
 import de.deepamehta.core.RelatedTopic;
 import de.deepamehta.core.ResultSet;
 import de.deepamehta.core.model.TopicModel;
 import de.deepamehta.core.service.ClientState;
 import de.deepamehta.core.service.PluginService;
 
 
 /**
  *
  * @author malt
  */
 public interface ResourceService extends PluginService {
 
   Topic createResource(TopicModel topic, ClientState clientState);
 
   Topic updateResource(TopicModel topic, ClientState clientState);
 
   ResultSet<RelatedTopic> getResourcesByTag(long tagId, ClientState clientState);
 
   ResultSet<RelatedTopic> getResourcesByTags(String tags, ClientState clientState);
 
   ResultSet<RelatedTopic> getAllResources(long from, ClientState clientState);
 
   Topic upvoteResourceById(long resourceId, ClientState clientState);
 
   Topic downvoteResourceById(long resourceId, ClientState clientState);
 
 }
