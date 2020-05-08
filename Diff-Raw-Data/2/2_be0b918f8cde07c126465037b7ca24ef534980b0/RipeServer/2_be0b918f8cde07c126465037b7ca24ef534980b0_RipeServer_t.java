 /**
 (C) Kyle Kamperschroer
 WARNING -- Prototyping code found below this point. Proceed with caution.
  */
 package com.kylek.ripe.ui;
 
 import java.util.*;
 import java.io.*;
 import com.kylek.ripe.core.*;
 
 public class RipeServer extends NanoHTTPD
 {
     ////////////////////////////////////
     // Members
     ////////////////////////////////////
     
     // The ripe controller
     private RIPEController mRipe;
 
     // The db file loc (BAD KYLE!)
     private static final String mDbLoc =
         "/media/Warehouse/Dropbox/School/Eclipse/workspace/ripe_v1_java/ripeDB.db";
 
     ////////////////////////////////////
     // Constructors
     ////////////////////////////////////
 
     // The default constructor
     public RipeServer() throws IOException
     {
         super(8778, new File("."));
 
         // This will take a second
         System.out.print("Initializing RIPE...");
         mRipe = new RIPEController(mDbLoc);
         System.out.println(" done!");
     }
 
     ////////////////////////////////////
     // Setters and getters
     ////////////////////////////////////
 
    // None for now
 
     ////////////////////////////////////
     // Other methods
     ////////////////////////////////////
 
     // Serve up a page!
     public Response serve(String uri, String method, Properties header, Properties parms, Properties files)
     {
         // This is the HTML we are constructing
         String msg = "";
 
         // Add the header stuff
         msg = addHeader(msg);
 
         try
         {
 	
 	        // Base the page on what is specified.
 	        String curPage = parms.getProperty("page");
 	
 	        // The recipe listing page
 	        if (curPage == null ||
 	            curPage.equals("recipes"))
 	        {
 	            // Render the listing of recipes
 	            msg += "<h2>Listing recipes:</h2>\n";
 	
 	            // Build a table of our recipes
 	            msg += 
 	                "<table border=1>\n" +
 	                "    <tr>\n" +
 	                "        <th>Recipe Name</th>\n" +
 	                "        <th colspan=2>Modify</th>\n" +
 	                "    </tr>\n";
 	
 	            // Add the recipes
 	            for (int i=0; i<mRipe.getAllRecipes().size(); i++)
 	            {
 	                Recipe cur = mRipe.getAllRecipes().get(i);
 	                msg +=
 	                    "<tr>\n" +
 	                    "   <td><a href=\"?page=view&recipe=" + i + "\">" + cur.getName() + "</a></td>\n" +
 	                    "   <td><a href=\"/?page=edit&recipe=" + i + "\">Edit</a>\n" +
 	                    "   <td><a href=\"/?page=remove&recipe=" + i + "\">X</a>\n" +
 	                    "</tr>\n";
 	            }
 	
 	            msg +=
 	                "</table>\n" +
 	                "<br/>\n" +
 	                "<a href=\"/?page=add_recipe\">Add a Recipe</a>\n";
 	        }
 	        // The view recipe page
 	        else if (curPage.equals("view"))
 	        {
 	            // Get the recipe id
 	            String recipeId = parms.getProperty("recipe");
 	            int recId = -1;
 	            if (recipeId != null)
 	            {
 	                recId = Integer.parseInt(recipeId);
 	            }
 	            
 	            if (recId < mRipe.getAllRecipes().size() &&
 	                recId >= 0)
 	            {
 	                // Get our current recipe
 	                Recipe cur = mRipe.getAllRecipes().get(recId);
 	
 	                // Display it!
 	
 	                // The recipe name
 	                msg +=
 	                    "<h3>" + cur.getName() + "</h3>\n";
 	                
 	                // The number of servings
 	                if (cur.getYield() != null && cur.getYield().getValue() > 0)
 	                {
 	                    msg += "<u>Serves:</u> " + cur.getYield().getValue() + "<br/>";
 	                }
 	
 	                // The cook and prep time
 	                String cookTime = cur.getCookTime();
 	                String prepTime = cur.getPrepTime();
 	                if (prepTime != null)
 	                {
 	                    msg += "<u>Prep Time:</u> " + prepTime + "<br/>";
 	                }
 	                if (cookTime != null)
 	                {
 	                    msg += "<u>Cook Time:</u> " + cookTime + "<br/><br/>";
 	                }
 	
 	                msg +=
 	                    "<b>Ingredients:</b>\n<br/>\n<p>\n";
 	
 	                // List the ingredients
 	                IngredientsList ings = cur.getIngredients();
 	                Vector<MeasurementAndIngredient> meaIngs = ings.getIngredients();
 	                for (int i=0; i<meaIngs.size(); i++)
 	                {
 	                    MeasurementAndIngredient curIng = meaIngs.get(i);
 	                    Measurement mea = curIng.getMeasurement();
                             Measurement mea2 = curIng.getMeasurement2();
 	                    Ingredient ing = curIng.getIngredient();
 
                             if (mea != null){
                                if (!mea.getAmount().equals("")){
                                   msg += mea.getAmount() + " ";
                                }
                                if (!mea.getSpecifier().equals("")){
                                   msg += mea.getSpecifier() + " ";
                                }
                                if (!mea.getUnit().equals("")){
                                   msg += mea.getUnit() + " ";
                                }
                             }
                             if (mea2 != null){
                                msg += " plus ";
                                if (!mea2.getAmount().equals("")){
                                   msg += mea2.getAmount() + " ";
                                }
                                if (!mea2.getSpecifier().equals("")){
                                   msg += mea2.getSpecifier() + " ";
                                }
                                if (!mea2.getUnit().equals("")){
                                   msg += mea2.getUnit() + " ";
                                }
                             }
                                
 	                    // Check if this ingredient has a product
 	                    if (!ing.getAmazonUrl().equals(""))
 	                    {
 	                        // Use the amazon url
 	                        msg +=
 	                            "<a href='" +
                                    ing.getAmazonUrl() +
 	                            "'>" + ing.getName() + "</a>";
 	                    }
 	                    else
 	                    {
 	                        // No product, No url.
 	                        msg +=
 	                            ing.getName();
 	                    }
 	                    if (!ing.getSpecialDirections().equals(""))
 	                    {
 	                        msg += ing.getSpecialDirections();
 	                    }
 	                    msg += "<br/>";
 	                }
 	                msg += "</p>\n";
 	
 	                // List the directions
 	                msg += 
 	                    "<b>Directions:</b>\n<br/>\n<p>\n";
 	
 	                String directions = cur.getDirections();
 	                directions = directions.replaceAll("(\r\n|\n)", "<br/>");
 	                msg += directions;
 	
 	                msg +=
 	                    "</p>\n";
 	
 	                // Link to the edit page for this recipe
 	                msg +=
 	                    "<br/>\n<a href='?page=edit&recipe=" + recId + "'>Edit</a>\n";
 	
 	                // Link to the remove page for this recipe
 	                msg +=
 	                    "<br/>\n<a href='?page=remove&recipe=" + recId + "'>Remove</a>\n";
 	
 	                // Link back to the listing
 	                msg +=
 	                    "<br/><br/><br/><a href='/'>Back to listing</a>\n";
 	            }
 	            else
 	            {
 	                msg += "<p>Invalid recipe: " + recipeId + "</p>\n";
 	            }
 	        }
 	        else if(curPage.equals("add_recipe"))
 	        {
 	            // Oh boy!
 	            
 	            // Headers
 	            msg +=
 	                "<h2>Add Recipe</h2>\n" +
 	                "<p>Just paste in the recipe!</p>\n";
 	
 	            // Build the form
 	            msg +=
 	                "<form action='?page=add_recipe_go' method='post'>\n" +
 	                "    Recipe:\n<br/>\n" +
 	                "    <textarea cols='80' rows='30' name='raw_recipe'>" +
 	                "</textarea>\n<br/>\n" +
 	                "    <input type='submit' value='Parse it!'/>\n" +
 	                "</form>\n";
 	
 	            // That's it! The parser will attempt to parse it on page add_recipe_go
 	
 	            // Link back to the listing
 	            msg +=
 	                "<br/><br/><br/><a href='/'>Back to listing</a>\n";
 	        }
 	        else if(curPage.equals("add_recipe_go"))
 	        {
 	            // Get the recipe from the post
 	            String recipe = parms.getProperty("raw_recipe");
 	
 	            int newId = 0;
 	
 	            // Ask mRipe to parse the recipe
 	            Recipe parsed = mRipe.parseRecipe(recipe);
 	
 	            // Hopefully that worked! 
 	            // Add the recipe to our db
 	            boolean retVal = false;
 	            if (parsed != null)
 	            {
 	                retVal = mRipe.addRecipe(parsed);
 	            }
 	
 	            if (retVal)
 	            {
 	                msg +=
 	                    "Recipe parsed successfully. Click " +
 	                    "<a href='?page=view&recipe=" + (mRipe.getAllRecipes().size()-1) +
 	                    "'>here</a>.";
 	            }
 	            else
 	            {
                        msg += mRipe.getErrorMessage();
 	            }
 	            // Link back to the listing
 	            msg +=
 	                "<br/><br/><br/>\n<a href='/'>Back to listing</a>\n";
 	
 	        }
 	        else if(curPage.equals("edit"))
 	        {
 	            msg += "Under construction.";
 	        }
 	        else if(curPage.equals("remove"))
 	        {
 	            // Get the current recipe id
 	            String recipeId = parms.getProperty("recipe");
 	            int recId = -1;
 	            if (recipeId != null)
 	            {
 	                recId = Integer.parseInt(recipeId);
 	            }
 	            
 	            if (recId < mRipe.getAllRecipes().size() &&
 	                recId >= 0)
 	            {
 	                // Remove the recipe
 	                boolean retVal = mRipe.removeRecipe(mRipe.getAllRecipes().get(recId));
 	                
 	                // Display a link back to the recipes page
 	                if (retVal)
 	                {
 	                    msg +=
 	                        "Recipe deleted!\n<br/>\n";
 	                }
 	                else
 	                {
 	                    msg +=
 	                        "Error removing recipe!\n<br/>\n";
 	                }
 	                
 	                // Link back to the listing
 	                msg +=
 	                    "<a href='/'>Back to listing</a>\n";
 	            }
 	            else
 	            {
 	                msg +=
 	                    "Invalid recipe id: " + recipeId;
 	            }
 	        }
 	        else
 	        {
 	            msg += "<p>Unknown page: " + curPage + "</p>\n";
 	        }
         }
         catch (Exception e)
         {
                 // Yikes. Keep running!
                 e.printStackTrace();
                 System.out.println(e);
                 msg += "Exception occured. Continuing anyways!";
         }
         /**
         System.out.println( method + " '" + uri + "' " );
         String msg = "<html><body><h1>Hello server</h1>\n";
         if ( parms.getProperty("username") == null )
             msg +=
                 "<form action='?' method='get'>\n" +
                 "  <p>Your name: <input type='text' name='username'></p>\n" +
                 "</form>\n";
         else
             msg += "<p>Hello, " + parms.getProperty("username") + "!</p>";
 
         msg += "</body></html>\n";
         **/
 
         // Add the footer to the message
         msg = addFooter(msg);
 
         return new NanoHTTPD.Response( HTTP_OK, MIME_HTML, msg );
     }
 
     //// Private methods ////
     
     // Add the header to our output page
     private String addHeader(String msg)
     {
         return msg +
             "<html>\n" +
             "    <head>\n" +
             "        <title>RIPE Prototype v1</title>\n" +
             "    </head>\n" +
             "    <body>\n" +
             "    <h1>Kyle's Recipe Parser</h1>\n" +
             "    <br/>\n";        
     }
 
     // Add the footer to our output page
     private String addFooter(String msg)
     {
         return msg +
             "    </body>\n" +
             "</html>\n";
     }
 
 
     // Main entry point
     public static void main( String[] args )
     {
         try
         {
             new RipeServer();
         }
         catch( IOException ioe )
         {
             System.err.println( "Couldn't start server:\n" + ioe );
             System.exit( -1 );
         }
         System.out.println( "Listening on port 8778. Hit Enter to stop.\n" );
         try
         {
             System.in.read(); 
         }
         catch( Throwable t ) {};
     }
 }
