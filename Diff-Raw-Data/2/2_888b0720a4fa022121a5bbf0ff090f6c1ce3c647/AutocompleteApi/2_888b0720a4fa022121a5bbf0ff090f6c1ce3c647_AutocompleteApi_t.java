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
 
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Response;
 
 import de.anycook.api.util.MediaType;
 import org.apache.log4j.Logger;
 
 import de.anycook.autocomplete.Autocomplete;
 import de.anycook.user.User;
 
 
 /**
  * Handles all autocomplete calls
  * @author Jan Graßegger <jan@anycook.de>
  *
  */
 @Path("autocomplete")
 public class AutocompleteApi {
 
     private final Logger logger = Logger.getLogger(getClass());
 	
 	/**
 	 * Completion for all categories
 	 */
	@GET
 	@Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.APPLICATION_JSON)
 	public Map<String, Object> autocomplete(@QueryParam("query") String query,
                                             @QueryParam("excludedCategory") String excludedCategory,
                                             @QueryParam("excludedIngredients") StringSet excludedIngredients,
                                             @QueryParam("excludedTags") StringSet excludedTags,
                                             @QueryParam("excludedUsers") IntSet excludedUsers,
                                             @QueryParam("maxResults") @DefaultValue("10") int maxResults){
 
         if(query == null) throw new WebApplicationException(Response.Status.BAD_REQUEST);
         try {
             return Autocomplete.autoCompleteAll(query, excludedCategory, excludedIngredients, excludedTags,
                     excludedUsers, maxResults);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 	}
 	
 	@GET
 	@Path("ingredient")
 	@Produces(MediaType.APPLICATION_JSON)
 	public List<String> autocompleteIngredient(@QueryParam("query") String query,
 			                                   @QueryParam("exclude") StringSet exclude,
 			                                   @QueryParam("maxResults") @DefaultValue("10") int maxResults){
 		if(query == null)
 			throw new WebApplicationException(Response.Status.BAD_REQUEST);
         try {
             return Autocomplete.autocompleteIngredient(query, maxResults, exclude);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 	}
 	
 	@GET
 	@Path("user")
 	@Produces(MediaType.APPLICATION_JSON)
 	public List<User> autoCompleteUser(@QueryParam("q") String query,
                                        @QueryParam("exclude") IntSet exclude,
                                        @QueryParam("maxResults") @DefaultValue("10") int maxResults){
 		if(query == null)
 			throw new WebApplicationException(Response.Status.BAD_REQUEST);
 
         try {
             return Autocomplete.autocompleteUsers(query, maxResults, exclude);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 	}
 	
 	@GET
 	@Path("tag")
 	@Produces(MediaType.APPLICATION_JSON)
 	public List<String> autocompleteTags(@QueryParam("q") String query,
 			                             @QueryParam("exclude") StringSet exclude,
 			                             @QueryParam("maxResults") @DefaultValue("10") int maxResults){
 		if(query == null)
 			throw new WebApplicationException(401);
         try {
             return Autocomplete.autocompleteTag(query, maxResults, exclude);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 	}
 	
 	public static class IntSet extends HashSet<Integer>{
 		private static final long serialVersionUID = 1L;
 
 		public IntSet(String in) {
 			super();
 			if(in != null){
 				for(String split : in.split(","))
 					add(Integer.parseInt(split));
 			}
 		}
 	}
 
     public static class StringSet extends HashSet<String>{
         private static final long serialVersionUID = 1L;
 
         public StringSet(String in) {
             super();
             if(in != null){
                 for(String split : in.split(","))
                     add(split);
             }
         }
     }
 
 
 }
