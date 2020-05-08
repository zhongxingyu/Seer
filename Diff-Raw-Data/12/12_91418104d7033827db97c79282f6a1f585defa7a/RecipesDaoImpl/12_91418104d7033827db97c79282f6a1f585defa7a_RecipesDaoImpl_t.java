 package com.github.russ4stall.reciperoots.recipes.dao;
 
 import com.github.russ4stall.reciperoots.recipes.Ingredient;
 import com.github.russ4stall.reciperoots.recipes.MeasurementType;
 import com.github.russ4stall.reciperoots.recipes.Recipe;
 import com.github.russ4stall.reciperoots.users.User;
 import com.github.russ4stall.reciperoots.utilities.SqlUtilities;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Date: 6/17/13
  * Time: 4:06 PM
  *
  * @author Russ Forstall
  */
 public class RecipesDaoImpl implements RecipesDao {
 
     Connection connection = null;
     PreparedStatement preparedStatement = null;
     ResultSet resultSet = null;
 
     @Override
     public void updateRecipe(Recipe recipe) {
         SqlUtilities.jbdcUtil();
         User user = recipe.getUser();
         try {
             connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/recipes", "recipe", "recipe");
 
 
             String query = "UPDATE recipes " +
                     "SET title=?, description=?, instructions=?, prep_time=?, cook_time=?, tags=?, created_on=now()" +
                     "WHERE id=?";
 
             preparedStatement = connection.prepareStatement(query);
             preparedStatement.setString(1, recipe.getTitle());
             preparedStatement.setString(2, recipe.getDescription());
             preparedStatement.setString(3, recipe.getInstructions());
             preparedStatement.setInt(4, recipe.getPrepTime());
             preparedStatement.setInt(5, recipe.getCookTime());
             preparedStatement.setString(6, recipe.getTags());
             preparedStatement.setInt(7, recipe.getId());
             preparedStatement.executeUpdate();
 
 
             PreparedStatement preparedStatement1 = null;
             try {
                 preparedStatement1 = connection.prepareStatement("DELETE FROM ingredients WHERE recipe_id = ?");
                 preparedStatement1.setInt(1, recipe.getId());
                 preparedStatement1.executeUpdate();
             } catch (SQLException e) {
                 throw new RuntimeException(e);
             } finally {
                 SqlUtilities.closePreparedStatement(preparedStatement1);
             }
 
             query = "INSERT INTO ingredients (user_id, recipe_id, ingredient, quantity, measurement) " +
                     "VALUES (?, ?, ?, ?, ?)";
 
             for (Ingredient ingredient : recipe.getIngredients()) {
                 PreparedStatement preparedStatement2 = null;
                 try {
                     preparedStatement2 = connection.prepareStatement(query);
                     preparedStatement2.setInt(1, user.getId());
                     preparedStatement2.setInt(2, recipe.getId());
                     preparedStatement2.setString(3, ingredient.getIngredient());
                     preparedStatement2.setDouble(4, ingredient.getQuantity());
                     preparedStatement2.setString(5, ingredient.getMeasurementType().toString());
                     preparedStatement2.executeUpdate();
                 } catch (SQLException e) {
                     throw new RuntimeException(e);
                 } finally {
                     SqlUtilities.closePreparedStatement(preparedStatement2);
                 }
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             SqlUtilities.closePreparedStatement(preparedStatement);
             SqlUtilities.closeConnection(connection);
 
         }
     }
 
 
     @Override
     public int addRecipe(Recipe recipe) {
         SqlUtilities.jbdcUtil();
         int recipeId = 1;
         User user = recipe.getUser();
         try {
             connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/recipes", "recipe", "recipe");
 
 
             String query = "INSERT INTO recipes (user_id, title, description, instructions, prep_time, cook_time, tags, created_on ) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, now())";
 
             preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
             preparedStatement.setInt(1, user.getId());
             preparedStatement.setString(2, recipe.getTitle());
             preparedStatement.setString(3, recipe.getDescription());
             preparedStatement.setString(4, recipe.getInstructions());
             preparedStatement.setInt(5, recipe.getPrepTime());
             preparedStatement.setInt(6, recipe.getCookTime());
             preparedStatement.setString(7, recipe.getTags());
 
             preparedStatement.executeUpdate();
 
             resultSet = preparedStatement.getGeneratedKeys();
             if (resultSet.next()) {
                 recipeId = resultSet.getInt(1);
             }
 
             query = "INSERT INTO ingredients (user_id, recipe_id, ingredient, quantity, measurement) " +
                     "VALUES (?, ?, ?, ?, ?)";
 
             for (Ingredient ingredient : recipe.getIngredients()) {
                 PreparedStatement preparedStatement1 = null;
                 try {
                     preparedStatement1 = connection.prepareStatement(query);
                     preparedStatement1.setInt(1, user.getId());
                     preparedStatement1.setInt(2, recipeId);
                     preparedStatement1.setString(3, ingredient.getIngredient());
                     preparedStatement1.setDouble(4, ingredient.getQuantity());
                     preparedStatement1.setString(5, ingredient.getMeasurementType().toString());
                     preparedStatement1.executeUpdate();
                 } catch (SQLException e) {
                     throw new RuntimeException(e);
                 } finally {
                     SqlUtilities.closePreparedStatement(preparedStatement1);
                 }
             }
 
 
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             SqlUtilities.closePreparedStatement(preparedStatement);
             SqlUtilities.closeConnection(connection);
         }
         return recipeId;
     }
 
 
     @Override
     public void deleteRecipe(int id) {
         SqlUtilities.jbdcUtil();
 
         try {
             connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/recipes", "recipe", "recipe");
 
             String query = "DELETE FROM recipes WHERE id = ?";
 
             preparedStatement = connection.prepareStatement(query);
             preparedStatement.setInt(1, id);
             preparedStatement.executeUpdate();
 
             PreparedStatement preparedStatement1 = null;
             try {
                 preparedStatement1 = connection.prepareStatement("DELETE FROM ingredients WHERE recipe_id = ?");
                 preparedStatement1.setInt(1, id);
                 preparedStatement1.executeUpdate();
             } catch (SQLException e) {
                 throw new RuntimeException(e);
             } finally {
                 SqlUtilities.closePreparedStatement(preparedStatement1);
             }
 
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             SqlUtilities.closePreparedStatement(preparedStatement);
             SqlUtilities.closeConnection(connection);
         }
     }
 
     @Override
     public Recipe getRecipe(int id) {
         Recipe recipe = new Recipe();
         User user = new User();
         List<Ingredient> ingredientList = new ArrayList<Ingredient>();
         SqlUtilities.jbdcUtil();
 
         try {
             connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/recipes", "recipe", "recipe");
 
             preparedStatement = connection.prepareStatement("SELECT * FROM recipes " +
                     "JOIN users ON recipes.user_id = users.id WHERE recipes.id=?");
 
             preparedStatement.setInt(1, id);
             resultSet = preparedStatement.executeQuery();
 
             resultSet.next();
             recipe.setTitle(resultSet.getString("title"));//get title from db
             recipe.setId(resultSet.getInt("id"));
             recipe.setUser(user);
             recipe.setDescription(resultSet.getString("description"));
             recipe.setPrepTime(resultSet.getInt("prep_time"));
             recipe.setCookTime(resultSet.getInt("cook_time"));
             recipe.setTags(resultSet.getString("tags"));
             recipe.setInstructions(resultSet.getString("instructions"));//get recipes string from db
             recipe.setCreatedOn(resultSet.getDate("created_on"));//get date from db
             user.setId(resultSet.getInt("user_id"));
             user.setName(resultSet.getString("name"));
             user.setEmail(resultSet.getString("email"));
             user.setLastLoginOn(resultSet.getDate("last_login_on"));
             user.setRegisteredOn(resultSet.getDate("registered_on"));
 
 
             PreparedStatement preparedStatement1 = null;
             ResultSet resultSet1 = null;
 
             try{
                 preparedStatement1 = connection.prepareStatement("SELECT * FROM ingredients " +
                         "WHERE recipe_id=?");
                 preparedStatement1.setInt(1, id);
                 resultSet1 = preparedStatement1.executeQuery();
 
                 while (resultSet1.next()){
                     Ingredient ingredient = new Ingredient();
                     ingredient.setIngredient(resultSet1.getString("ingredient"));
                     ingredient.setQuantity(resultSet1.getDouble("quantity"));
                     ingredient.setMeasurementType(MeasurementType.valueOf(resultSet1.getString("measurement")));
                     ingredientList.add(ingredient);
                 }
 
             } catch (SQLException e){
                 throw new RuntimeException(e);
             } finally {
                 SqlUtilities.closeResultSet(resultSet1);
                 SqlUtilities.closePreparedStatement(preparedStatement1);
             }
 
             recipe.setIngredients(ingredientList);
 
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             SqlUtilities.closePreparedStatement(preparedStatement);
             SqlUtilities.closeResultSet(resultSet);
             SqlUtilities.closeConnection(connection);
         }
 
 
         return recipe;
     }
 
     @Override
     public List getAllRecipes() {
         List<Recipe> recipeList = new ArrayList<Recipe>();
 
         SqlUtilities.jbdcUtil();
 
         try {
             connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/recipes", "recipe", "recipe");
 
             preparedStatement = connection.prepareStatement("SELECT * " +
                     "FROM recipes " +
                     "JOIN users " +
                     "ON recipes.user_id = users.id " +
                     "ORDER BY recipes.title");
             resultSet = preparedStatement.executeQuery();
 
             while (resultSet.next()) {
                 Recipe recipe = new Recipe();
                 User user = new User();
                 List<Ingredient> ingredientList = new ArrayList<Ingredient>();
                 recipe.setTitle(resultSet.getString("title"));//get title from db
                 recipe.setUser(user);
                 recipe.setId(resultSet.getInt("id"));//get id from db
                 recipe.setInstructions(resultSet.getString("instructions"));//get recipes string from db
                 recipe.setTags(resultSet.getString("tags"));
                 recipe.setPrepTime(resultSet.getInt("prep_time"));
                 recipe.setCookTime(resultSet.getInt("cook_time"));
                 recipe.setCreatedOn(resultSet.getDate("created_on"));//get date from db
                 user.setName(resultSet.getString("name"));
                 user.setRegisteredOn(resultSet.getDate("registered_on"));
                 user.setLastLoginOn(resultSet.getDate("last_login_on"));
 
 
 
                 PreparedStatement preparedStatement1 = null;
                 ResultSet resultSet1 = null;
 
                 try{
                     preparedStatement1 = connection.prepareStatement("SELECT * FROM ingredients " +
                             "WHERE recipe_id=?");
                     preparedStatement1.setInt(1, recipe.getId());
                     resultSet1 = preparedStatement1.executeQuery();
 
                     while (resultSet1.next()){
                         Ingredient ingredient = new Ingredient();
                         ingredient.setIngredient(resultSet1.getString("ingredient"));
                         ingredient.setQuantity(resultSet1.getDouble("quantity"));
                         ingredient.setMeasurementType(MeasurementType.valueOf(resultSet1.getString("measurement")));
                         ingredientList.add(ingredient);
                     }
 
                 } catch (SQLException e){
                     throw new RuntimeException(e);
                 } finally {
                     SqlUtilities.closeResultSet(resultSet1);
                     SqlUtilities.closePreparedStatement(preparedStatement1);
                 }
 
                 recipe.setIngredients(ingredientList);
 
 
                 recipeList.add(recipe);
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             SqlUtilities.closeConnection(connection);
             SqlUtilities.closePreparedStatement(preparedStatement);
             SqlUtilities.closeResultSet(resultSet);
         }
 
         return recipeList;
     }
 
     @Override
     public List getMyRecipes(int userId) {
         List<Recipe> recipeList = new ArrayList();
 
         SqlUtilities.jbdcUtil();
 
         try {
             connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/recipes", "recipe", "recipe");
 
             preparedStatement = connection.prepareStatement("SELECT * " +
                     "FROM recipes " +
                     "JOIN users " +
                     "ON recipes.user_id = users.id " +
                     "WHERE user_id=? " +
                     "ORDER BY recipes.title");
 
             preparedStatement.setInt(1, userId);
             resultSet = preparedStatement.executeQuery();
 
             while (resultSet.next()) {
                 Recipe recipe = new Recipe();
                 User user = new User();
                 recipe.setTitle(resultSet.getString("title"));//get title from db
                 recipe.setUser(user);
                 recipe.setId(resultSet.getInt("id"));//get id from db
                 recipe.setInstructions(resultSet.getString("instructions"));//get recipes string from db
                 recipe.setCreatedOn(resultSet.getDate("created_on"));//get date from db
                 user.setName(resultSet.getString("name"));
                 user.setRegisteredOn(resultSet.getDate("registered_on"));
                 user.setLastLoginOn(resultSet.getDate("last_login_on"));
                 recipeList.add(recipe);
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             SqlUtilities.closeConnection(connection);
             SqlUtilities.closePreparedStatement(preparedStatement);
             SqlUtilities.closeResultSet(resultSet);
         }
 
         return recipeList;
     }
 
     @Override
     public Recipe getRandomRecipe() {
         Recipe recipe = new Recipe();
         User user = new User();
         List<Ingredient> ingredientList = new ArrayList<Ingredient>();
         SqlUtilities.jbdcUtil();
 
         try {
             connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/recipes", "recipe", "recipe");
 
             preparedStatement = connection.prepareStatement("SELECT * FROM recipes " +
                     "JOIN users ON recipes.user_id = users.id ORDER BY RAND() LIMIT 1");
 
 
             resultSet = preparedStatement.executeQuery();
 
             resultSet.next();
             recipe.setTitle(resultSet.getString("title"));//get title from db
             user.setId(resultSet.getInt("user_id"));
             recipe.setId(resultSet.getInt("id"));
             recipe.setUser(user);
             recipe.setTags(resultSet.getString("tags"));
             recipe.setPrepTime(resultSet.getInt("prep_time"));
             recipe.setCookTime(resultSet.getInt("cook_time"));
             recipe.setDescription(resultSet.getString("description"));
             recipe.setInstructions(resultSet.getString("instructions"));//get recipes string from db
             recipe.setCreatedOn(resultSet.getDate("created_on"));//get date from db
             user.setId(resultSet.getInt("id"));
             user.setName(resultSet.getString("name"));
             user.setEmail(resultSet.getString("email"));
             user.setLastLoginOn(resultSet.getDate("last_login_on"));
             user.setRegisteredOn(resultSet.getDate("registered_on"));
 
 
             PreparedStatement preparedStatement1 = null;
             ResultSet resultSet1 = null;
 
             try{
                 preparedStatement1 = connection.prepareStatement("SELECT * FROM ingredients " +
                         "WHERE recipe_id=?");
                 preparedStatement1.setInt(1, recipe.getId());
                 resultSet1 = preparedStatement1.executeQuery();
 
                 while (resultSet1.next()){
                     Ingredient ingredient = new Ingredient();
                     ingredient.setIngredient(resultSet1.getString("ingredient"));
                     ingredient.setQuantity(resultSet1.getDouble("quantity"));
                     ingredient.setMeasurementType(MeasurementType.valueOf(resultSet1.getString("measurement")));
                     ingredientList.add(ingredient);
                 }
 
             } catch (SQLException e){
                 throw new RuntimeException(e);
             } finally {
                 SqlUtilities.closeResultSet(resultSet1);
                 SqlUtilities.closePreparedStatement(preparedStatement1);
             }
 
             recipe.setIngredients(ingredientList);
 
 
 
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             SqlUtilities.closeConnection(connection);
             SqlUtilities.closePreparedStatement(preparedStatement);
             SqlUtilities.closeResultSet(resultSet);
         }
 
         return recipe;
     }
 
     @Override
     public Recipe getLatestRecipe() {
         Recipe recipe = new Recipe();
         User user = new User();
 
         SqlUtilities.jbdcUtil();
 
         try {
             connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/recipes", "recipe", "recipe");
 
             preparedStatement = connection.prepareStatement("SELECT * FROM recipes " +
                     "JOIN users ON recipes.user_id = users.id ORDER BY recipes.created_on DESC LIMIT 1");
 
 
             resultSet = preparedStatement.executeQuery();
 
             resultSet.next();
             recipe.setTitle(resultSet.getString("title"));//get title from db
             user.setId(resultSet.getInt("user_id"));
             recipe.setId(resultSet.getInt("id"));
             recipe.setUser(user);
            recipe.setPrepTime(resultSet.getInt("prep_time"));
            recipe.setCookTime(resultSet.getInt("cook_time"));
             recipe.setInstructions(resultSet.getString("instructions"));//get recipes string from db
             recipe.setCreatedOn(resultSet.getDate("created_on"));//get date from db
             user.setId(resultSet.getInt("id"));
             user.setName(resultSet.getString("name"));
             user.setEmail(resultSet.getString("email"));
             user.setLastLoginOn(resultSet.getDate("last_login_on"));
             user.setRegisteredOn(resultSet.getDate("registered_on"));
 
 
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             SqlUtilities.closeConnection(connection);
             SqlUtilities.closePreparedStatement(preparedStatement);
             SqlUtilities.closeResultSet(resultSet);
         }
 
         return recipe;
     }
 }
 
 
