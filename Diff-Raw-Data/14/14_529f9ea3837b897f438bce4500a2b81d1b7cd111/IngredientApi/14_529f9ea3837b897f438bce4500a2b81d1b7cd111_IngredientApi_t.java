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
 import de.anycook.db.mysql.DBIngredient;
 import de.anycook.recipe.ingredient.Ingredient;
 import org.apache.log4j.Logger;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.Response;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Set;
 
 
 @Path("/ingredient")
 public class IngredientApi {
 
     private Logger logger = Logger.getLogger(getClass());
 	
 	@SuppressWarnings("unchecked")
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
	public List<Ingredient> getAll(@QueryParam("parent") boolean parent){
 		 try {
             if(!parent) return Ingredient.getAll();
             return Ingredient.loadParents();
         } catch (SQLException e) {
             logger.error(e);
              throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
 	}
 	
 	/**
 	 * Number of ingredients
 	 * @return
 	 */
 	@GET
 	@Path("number")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Integer getNum(){
         try {
             return Ingredient.getTotal();
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 	
 	@GET
 	@Path("extract")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Set<String> extractIngredients(@QueryParam("q") String query){
         try {
             return Ingredient.searchNGram(query, 3);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 	
 	@GET
	@Path("{ingredientName}")
 	@Produces(MediaType.APPLICATION_JSON)
	public Ingredient getIngredient(@PathParam("ingredientName") String ingredientName){
         try {
             return Ingredient.init(ingredientName);
         } catch (SQLException e) {
            logger.error(e, e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         } catch (DBIngredient.IngredientNotFoundException e) {
             logger.info(e,e);
             throw new WebApplicationException(Response.Status.NOT_FOUND);
         }
     }
 }
