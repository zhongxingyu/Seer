 package com.sound.service.endpoint;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.annotation.security.RolesAllowed;
 import javax.json.JsonArray;
 import javax.json.JsonObject;
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.constraints.NotNull;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.sound.constant.Constant;
 import com.sound.exception.SoundException;
 import com.sound.filter.authentication.ResourceAllowed;
 import com.sound.model.Tag;
 import com.sound.model.Tag.TagCategory;
 import com.sound.model.User;
 import com.sound.service.sound.itf.SoundService;
 
 @Component
 @Path("/tag")
 @RolesAllowed({Constant.ADMIN_ROLE, Constant.GUEST_ROLE, Constant.USER_ROLE, Constant.PRO_ROLE,
     Constant.SPRO_ROLE})
 public class TagServiceEndpoint {
 
   Logger logger = Logger.getLogger(TagServiceEndpoint.class);
 
   @Autowired
   com.sound.service.sound.itf.TagService tagService;
 
   @Autowired
   SoundService soundService;
 
   @Autowired
   com.sound.service.user.itf.UserService userService;
 
   @Context
   HttpServletRequest req;
 
   @PUT
   @Path("/{categoryName}/{tag}/create")
   @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
   public Response createTag(@NotNull @PathParam("tag") String label,
       @NotNull @QueryParam("curated") Boolean curated,
       @PathParam("categoryName") String categoryName) {
     try {
       Tag tag = new Tag();
       tag.setLabel(label);
       tag.setCurated(curated);
       
       TagCategory category = new TagCategory();
       category.setName(categoryName);
       tag.setCategory(category);
       tag.setCreatedDate(new Date());
       tagService.get(tag, true);
     } catch (SoundException e) {
       logger.error(e);
       return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
     } catch (Exception e) {
       return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
     }
 
     return Response.status(Response.Status.CREATED).build();
   }
 
   @PUT
   @Path("/sound/{soundAlias}")
   @Consumes(MediaType.APPLICATION_JSON)
   @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
   public List<Tag> attachTagsToSound(@NotNull @PathParam("soundAlias") String soundAlias,
       @NotNull JsonObject inputJsonObj) {
 
     User curUser = null;
     List<Tag> results = null;
     try {
       curUser = userService.getCurrentUser(req);
       JsonArray tags = inputJsonObj.getJsonArray("tags");
       List<String> tagList = new ArrayList<String>();
       for (int i = 0; i < tags.size(); ++i) {
         tagList.add(tags.getString(i));
       }
       results = tagService.attachToSound(soundAlias, tagList, curUser);
     } catch (Exception e) {
       logger.error(e);
       throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
     }
 
     return results;
   }
 
   @DELETE
   @Path("/sound/{soundAlias}/{tag}")
   @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
   public Response detachTagsFromSound(@NotNull @PathParam("soundAlias") String soundAlias,
       @NotNull @PathParam("tag") String tag) {
     User curUser = null;
     try {
       curUser = userService.getCurrentUser(req);
 
       List<String> tagList = new ArrayList<String>();
       tagList.add(tag);
 
       tagService.detachFromSound(soundAlias, tagList, curUser);
     } catch (Exception e) {
       logger.error(e);
       return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
     }
 
     return Response.status(Response.Status.OK).build();
   }
 
   @GET
   @Path("/list")
   @Produces(MediaType.APPLICATION_JSON)
   @ResourceAllowed
   public List<Tag> getTagsContains(@NotNull @QueryParam("term") String term) {
     List<Tag> tags = null;
     try {
       tags = tagService.listTagsContains(term);
     } catch (SoundException e) {
       logger.error(e);
       throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
     }
 
     return tags;
   }
 
   @GET
   @Path("/user/{userAlias}")
   @Produces(MediaType.APPLICATION_JSON)
   @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
   public List<Tag> getTagsByUser(@NotNull @PathParam("userAlias") String userAlias) {
     List<Tag> tags = null;
     try {
       User user = userService.getUserByAlias(userAlias);
       tags = user.getTags();
     } catch (Exception e) {
       logger.error(e);
       throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
     }
 
     return tags;
   }
   
   @GET
   @Path("/list/curated")
   @Produces(MediaType.APPLICATION_JSON)
   @ResourceAllowed
   public List<Tag> getCuratedTags() {
     List<Tag> tags = null;
     User curUser = null;
     try {
      curUser = userService.getCurrentUser(req);
       tags = tagService.findCurated();
       
      for(Tag tag: tags)
       {
        tag.setInterested(curUser.containTag(tag));
       }
     } catch (SoundException e) {
       logger.error(e);
       throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
     }
 
     return tags;
   }
 
   @GET
   @Path("/list/categories")
   @Produces(MediaType.APPLICATION_JSON)
   @ResourceAllowed
   public List<TagCategory> getCategories() {
     List<TagCategory> categories = null;
     try {
       categories = tagService.listCategories();
     } catch (Exception e) {
       logger.error(e);
       throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
     }
 
     return categories;
   }
 }
