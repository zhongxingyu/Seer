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
 
 package de.anycook.db.mysql;
 
 import de.anycook.recipe.Recipe;
 import de.anycook.recipe.Time;
 import de.anycook.recipe.ingredient.Ingredient;
 import de.anycook.recipe.tag.Tag;
 import de.anycook.user.User;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.*;
 
 /**
  * Von DBHandler abgeleitet. Enthaelt alle Funktionen, die fuer die Gerichte zustaendig sind.
  *
  * @author Jan Grassegger
  * @see de.anycook.db.mysql.DBHandler
  */
 public class DBRecipe extends DBHandler {
 
 
     public DBRecipe() throws SQLException {
         super();
     }
 
     /**
      * Ueberprueft Gericht auf Existenz. Nutzt zur Ueberpruefung {@link de.anycook.db.mysql.DBRecipe#getName(String)}.
      *
      * @param q {@link String} mit zu uberpruefendem Gericht.
      * @return {@link Boolean} mit true, wenn vorhanden, sonst false.
      */
     public boolean check(String q) throws SQLException {
         try {
             getName(q);
             return true;
         } catch (RecipeNotFoundException e) {
             return false;
         }
     }
 
 
 
     /**
      * Gibt ein gegebenes Gericht q in der Schreibweise der Datenbank zurueck. Ist das Gericht nicht vorhanden wird null zurueckgegeben.
      *
      * @param q String mit dem gesuchten Gericht.
      * @return String mit dem Kategorienamen aus der Datenbank oder null.
      */
     public String getName(String q) throws SQLException, RecipeNotFoundException {
         PreparedStatement pStatement = connection.prepareStatement("SELECT name from gerichte WHERE name = ?");
         pStatement.setString(1, q);
         ResultSet data = pStatement.executeQuery();
         if (data.next()) return data.getString("name");
 
         throw new RecipeNotFoundException(q);
     }
 
     public int getTasteNum(String recipeName) throws SQLException {
         int count = 0;
         PreparedStatement pStatement = connection.prepareStatement("SELECT name, COUNT(users_id) AS counter FROM gerichte LEFT JOIN schmeckt ON name = gerichte_name WHERE name = ? GROUP BY name");
         pStatement.setString(1, recipeName);
         ResultSet data = pStatement.executeQuery();
         if (data.next())
             count = data.getInt("counter");
         return count;
     }
 
 
 
     //tags
 
 
     /**
      * Entfernt Tag von einem Gericht. Hat kein weiteres Gericht dieses Tag, wird der Tag komplett geloescht.
      *
      * @param recipeName Name des Gerichts
      * @param tag        Name des Tags
      * @return wenn mysql-anfragen erfolgreich, true
      * @throws java.sql.SQLException
      */
     public boolean removeTagFromRecipe(String recipeName, String tag) throws SQLException {
         PreparedStatement pStatement = connection.prepareStatement("DELETE FROM gerichte_has_tags " +
                 "WHERE gerichte_name = ? AND tags_name = ?");
 
         pStatement.setString(1, recipeName);
         pStatement.setString(2, tag);
         pStatement.executeUpdate();
 
         return true;
     }
 
     public boolean hasTag(String tag) throws SQLException {
         PreparedStatement pStatement = connection.prepareStatement("SELECT * FROM gerichte_has_tags WHERE tags_name = ?");
 
         pStatement.setString(1, tag);
         ResultSet data = pStatement.executeQuery();
         return data.next();
     }
 
     public boolean hasTag(String recipe, String tag) throws SQLException {
         PreparedStatement pStatement = connection.prepareStatement("SELECT * FROM gerichte_has_tags " +
                 "WHERE gerichte_name = ? AND tags_name = ?");
         pStatement.setString(1, recipe);
         pStatement.setString(2, tag);
         ResultSet data = pStatement.executeQuery();
         return data.next();
     }
 
     /**
      * Gibt die am meisten benutzten tags als map mit ihrer Anzahl zurueck.
      *
      * @param numTags Anzahl der Tags, die man bekommen moechte.
      * @return {@link java.util.Map} mit Tags und Anzahl ihres auftretens.
      */
     public List<Tag> getPopularTags(int numTags) throws SQLException {
         List<Tag> tags = new LinkedList<>();
         PreparedStatement pStatement = connection.prepareStatement("SELECT tags_name AS tag, COUNT(gerichte_name) " +
                 "AS count FROM gerichte_has_tags " +
                 "WHERE active = 1 GROUP BY tag ORDER BY count DESC LIMIT ?");
         pStatement.setInt(1, numTags);
         ResultSet data = pStatement.executeQuery();
 
         while (data.next()) {
             String tagName = data.getString("tag");
             int count = data.getInt("count");
             tags.add(new Tag(tagName, count));
         }
 
         return tags;
     }
 
     public List<Tag> getPopularTagsNotInRecipe(int numTags, String recipe) throws SQLException {
         List<Tag> tags = new LinkedList<>();
         PreparedStatement pStatement = connection.prepareStatement("SELECT tags_name, COUNT(gerichte_name) AS count FROM gerichte_has_tags " +
                 "WHERE active = 1 AND " +
                 "tags_name NOT IN (SELECT tags_name FROM gerichte_has_tags WHERE gerichte_name = ? GROUP BY tags_name) " +
                 "GROUP BY tags_name ORDER BY count DESC LIMIT ?");
         pStatement.setString(1, recipe);
         pStatement.setInt(2, numTags);
         ResultSet data = pStatement.executeQuery();
 
         while (data.next()) {
            String tagName = data.getString("tag");
             int count = data.getInt("count");
             tags.add(new Tag(tagName, count));
         }
 
         return tags;
     }
 
     //ingredients
     public Ingredient getIngredientForStem(String stem) throws SQLException, DBIngredient.IngredientNotFoundException {
         PreparedStatement pStatement = connection.prepareStatement("SELECT name from zutaten WHERE stem = ?");
         pStatement.setString(1, stem);
         ResultSet data = pStatement.executeQuery();
         if (data.next())
             return new Ingredient(data.getString("name"));
         throw new DBIngredient.IngredientNotFoundException(stem);
     }
 
     /**
      * ueberprueft ob ein filename bereits bei einem Gericht existiert
      *
      * @param filename zu checkender Filename
      * @return wenn bereits vorhanden false, sonst true
      */
     public boolean checkFilename(String filename) throws SQLException {
         PreparedStatement pStatement = connection.prepareStatement("SELECT * from versions WHERE imagename = ?");
         pStatement.setString(1, filename);
         ResultSet data = pStatement.executeQuery();
         return !data.next();
     }
 
     /**
      * increased den viewed eintrag eines bestimmten Gerichts um 1
      *
      * @param recipeName Gerichtename
      */
     public void viewRecipe(String recipeName) throws SQLException {
         PreparedStatement pStatement = connection.prepareStatement("UPDATE gerichte " +
                 "SET viewed = viewed + 1 WHERE name = ?");
         pStatement.setString(1, recipeName);
         pStatement.executeUpdate();
     }
 
     public int getActiveVersion(String recipeName) throws SQLException {
         PreparedStatement pStatement = connection.prepareStatement("SELECT active_id FROM gerichte WHERE name = ?");
         pStatement.setString(1, recipeName);
         ResultSet data = pStatement.executeQuery();
         if (data.next())
             return data.getInt("active_id");
 
         return -1;
     }
 
     protected List<Recipe> getRecipes(ResultSet data) throws SQLException {
         List<Recipe> recipes = new ArrayList<>();
         while (data.next()) {
             recipes.add(getRecipe(data));
         }
 
         return recipes;
     }
 
     protected Recipe getRecipe(ResultSet data) throws SQLException {
         String description = data.getString("beschreibung");
         Date created = data.getDate("created");
         Time time = new Time(data.getInt("std"), data.getInt("min"));
         int skill = data.getInt("skill");
         int calorie = data.getInt("kalorien");
         int person = data.getInt("personen");
         int id = data.getInt("id");
         int activeId = data.getInt("active_id");
         String name = data.getString("name");
 
         String image = data.getString("image");
         int userId = data.getInt("users_id");
         String username = data.getString("nickname");
         String userImage = data.getString("users.image");
         User user = new User(userId, username, userImage);
         String category = data.getString("kategorien_name");
         int views = data.getInt("viewed");
 
         return new Recipe(id, name, description, image, person, created, category,
                 skill, calorie, time, activeId, views, user);
     }
 
 
     public static class RecipeNotFoundException extends Exception {
         public RecipeNotFoundException(String queryRecipe) {
             super("recipe does not exist: " + queryRecipe);
         }
 
         public RecipeNotFoundException(String recipeName, int versionId) {
             super(String.format("recipe: \"%s\" versionId: %d does not exist", recipeName, versionId));
         }
     }
 
 
 }
