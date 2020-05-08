 package com.jayway.khelg.rest;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.jayway.khelg.domain.Entry;
 import com.jayway.khelg.domain.Forum;
 import com.jayway.khelg.domain.Topic;
 import com.jayway.khelg.model.ForumImpl;
 import com.jayway.khelg.rest.dto.EntryDTO;
 import com.jayway.khelg.rest.dto.ForumDTO;
 import com.jayway.khelg.rest.dto.TopicDTO;
 import com.jayway.khelg.storage.ForumRepository;
 
 @Component
 @Path("/")
 public class ForumResource {
 
     @Autowired
     private ForumRepository forumRepository;
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public Collection<ForumDTO> listForums() {
         Collection<Forum> forums = forumRepository.getAll();
         return translateForumsToDTO(forums);
     }
 
     private Collection<ForumDTO> translateForumsToDTO(Collection<Forum> forums) {
         Collection<ForumDTO> dtos = new ArrayList<ForumDTO>();
         for (Forum forum : forums) {
             ForumDTO dto = new ForumDTO();
             dto.id = forum.getId();
             dto.name = forum.getName();
             dtos.add(dto);
         }
         return dtos;
     }
 
     private Collection<TopicDTO> translateTopicsToDTO(Collection<? extends Topic> topics) {
         Collection<TopicDTO> dtos = new ArrayList<TopicDTO>();
         for (Topic topic : topics) {
             TopicDTO dto = new TopicDTO();
             dto.id = topic.getId();
             dto.header = topic.getHeader();
             dtos.add(dto);
         }
         return dtos;
     }
 
     private Collection<EntryDTO> translateEntriesToDTO(Collection<? extends Entry> entries) {
         Collection<EntryDTO> dtos = new ArrayList<EntryDTO>();
         for (Entry entry : entries) {
             EntryDTO dto = new EntryDTO();
             dto.id = entry.getId();
             dto.header = entry.getHeader();
             dto.message = entry.getMessage();
            dto.date = entry.getDate();
             dtos.add(dto);
         }
         return dtos;
     }
 
     @POST
     public Response addForum(@QueryParam(value = "name") String name) {
         Forum forum = new ForumImpl(name);
         forumRepository.add(forum);
         return Response.ok("Added forum").build();
     }
 
     @GET
     @Path("/{id}")
     public ForumDTO get(@PathParam("id") long id) {
         Forum forum = forumRepository.get(id);
         ForumDTO dto = new ForumDTO();
         dto.id = forum.getId();
         dto.name = forum.getName();
         return dto;
     }
 
     @GET
     @Path("/{forumid}/topics")
     @Produces(MediaType.APPLICATION_JSON)
     @Transactional
     public Collection<TopicDTO> listTopics(@PathParam("forumid") long forumid) {
         Forum forum = forumRepository.get(forumid);
         return translateTopicsToDTO(forum.getTopics());
     }
 
     @GET
     @Path("/{forumid}/topic/{topicid}/entries")
     @Produces(MediaType.APPLICATION_JSON)
     @Transactional
     public Collection<EntryDTO> listEntries(@PathParam("forumid") long forumid, @PathParam("topicid") long topicid) {
         Forum forum = forumRepository.get(forumid);
         if (forum != null) {
             for (Topic topic : forum.getTopics()) {
                 if (topic.getId() == topicid) {
                     return translateEntriesToDTO(topic.getEntries());
                 }
             }
         }
         return null;
     }
 
 }
