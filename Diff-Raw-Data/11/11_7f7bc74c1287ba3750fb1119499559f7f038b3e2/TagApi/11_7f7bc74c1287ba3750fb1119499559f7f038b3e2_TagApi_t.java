 /*
  * This file is part of anycook. The new internet cookbook
  * Copyright (C) 2014 Jan Graßegger
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see [http://www.gnu.org/licenses/].
  */
 
 package de.anycook.api;
 
 import de.anycook.api.util.MediaType;
 import de.anycook.db.mysql.DBTag;
 import de.anycook.recipe.Recipes;
 import de.anycook.recipe.tag.Tag;
 import org.apache.log4j.Logger;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.Response;
 import java.sql.SQLException;
 import java.util.List;
 
 
 @Path("tag")
 public class TagApi {
     private final Logger logger = Logger.getLogger(getClass());
 	
 	@SuppressWarnings("unchecked")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public List<Tag> getAll(){
         try {
             return Tag.getAll();
         } catch (SQLException e) {
            logger.error(e, e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 	
 	/**
 	 * Number of tags
 	 * @return
 	 */
 	@GET
 	@Path("number")
 	@Produces(MediaType.APPLICATION_JSON)
 	public int getNum(){
         try {
             return Tag.getTotal();
         } catch (SQLException e) {
            logger.error(e, e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 	
 	/**
 	 * Tags ordered by popularity.
 	 * @param recipe If set tags of this recipe are excluded
 	 * @return Map of tags ordered by popularity with number of recipes
 	 */
 	@GET
 	@Path("popular")
 	@Produces(MediaType.APPLICATION_JSON)
 	public List<Tag> getPopularTags(@QueryParam("recipe") String recipe){
 		try {
             if(recipe==null)
                 return Recipes.getPopularTags();
             return Recipes.getPopularTags(recipe);
         } catch (SQLException e){
            logger.error(e, e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 	}
 	
 	@GET
 	@Path("{tagName}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Tag getTag(@PathParam("tagName") String tagName){
         try {
             return Tag.init(tagName);
         } catch (SQLException e) {
            logger.error(e, e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         } catch (DBTag.TagNotFoundException e) {
             logger.warn(e);
             throw new WebApplicationException(Response.Status.NOT_FOUND);
         }
     }
 }
