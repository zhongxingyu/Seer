 /**
    (C) Kyle Kamperschroer 2013
 */
 
 package com.kylek.ripe.core;
 
 import java.util.Vector;
 
 public class User {
 
    ////////////////////////////////////////
    // Members
    ////////////////////////////////////////
 
    // This user's username
    private String mUsername;
 
    // This user's passhash
    private String mPassword;
 
    // This user's current session id
    private String mSessionId;
    
    // A vector of this user's recipes
    private Vector<Recipe> mRecipes;
 
    // Is this user an administrator?
    private boolean mIsAdmin;
 
    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////
 
    // Default constructor
    public User(){
       mUsername = "";
       mPassword = "";
       mSessionId = "";
       mRecipes = new Vector<Recipe>();
       mIsAdmin = false;
    }
 
    // Constructor with params
    public User(String username,
                String password,
                String sessionId,
                Vector<Recipe> recipes,
                boolean isAdmin){
       mUsername = username;
       mPassword = password;
       mSessionId = sessionId;
       mRecipes = recipes;
       mIsAdmin = isAdmin;
    }
 
    ////////////////////////////////////////
    // Accessors
    ////////////////////////////////////////
 
    public String getUsername() { return mUsername; }
    public String getPassword() { return mPassword; }
    public String getSessionId() { return mSessionId; }
    public Vector<Recipe> getRecipes() { return mRecipes; }
    public boolean isAdmin() { return mIsAdmin; }
 
    public void setUsername(String username) { mUsername = username; }
    public void setPassword(String password) { mPassword = password; }
    public void setSessionId(String sessionId) { mSessionId = sessionId; }
    public void setRecipes(Vector<Recipe> recipes) { mRecipes = recipes; }
    public void setIsAdmin(boolean isAdmin) { mIsAdmin = isAdmin; }
 
    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////
 
    // Add a single recipe
    public boolean addRecipe(Recipe r){
       // Recipes must be unique, so first check to see if it exists
       for (int i=0; i<mRecipes.size(); i++){
          if (mRecipes.get(i).getName().equals(r.getName())){
             return false;
          }
       }
 
       // All is good, add the recipe
       mRecipes.add(r);
       return true;
    }
 
    // Remove a recipe at a specific index
    public boolean removeRecipe(int index){
      if (index >= mRecipes.size() ||
          index < 0){
          return false;
       }
       
       // Remove the recipe
       mRecipes.remove(index);
       return true;
    }
 
    // Get a recipe id from the recipe
    public int getRecipeId(Recipe recipe){
       for (int i=0; i<mRecipes.size(); i++){
          if (mRecipes.get(i) == recipe){
             return i;
          }
       }
       // This isn't our recipe
       return -1;
    }
 }
