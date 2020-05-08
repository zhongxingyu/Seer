 /**
 (C) Kyle Kamperschroer 2013
  */
 
 package com.kylek.ripe.core;
 
 public class TestDriver {
 
    // The hard coded recipe for testing things out
    private static final String TEST_RECIPE =
 		   /**
       "Baked Ziti I\n" +
       "Serves: 10\n" +
       "Prep Time: 35 Minutes\n" +
       "Cook Time: 20 Minutes\n" +
       "Ready In: 55 Minutes\n" +
       "1 pound dry ziti pasta\n" +
       "1 onion, chopped\n" +
       "1 pound lean ground beef\n" +
       "2 (26 ounce) jars spaghetti sauce\n" +
       "6 ounces provolone cheese, sliced\n" +
       "1 1/2 cups sour cream\n" +
       "6 ounces mozzarella cheese, shredded\n" +
       "2 tablespoons grated Parmesan cheese\n" +
       "Directions\n" +
       "1. Bring a large pot of lightly salted water to a boil. Add ziti pasta, and cook until al dente, about 8 minutes; drain.\n" +
       "2. In a large skillet, brown onion and ground beef over medium heat. Add spaghetti sauce, and simmer 15 minutes.\n" +
       "3. Preheat the oven to 350 degrees F (175 degrees C). Butter a 9x13 inch baking dish. Layer as follows: 1/2 of the ziti, Provolone cheese, sour cream, 1/2 sauce mixture, remaining ziti, mozzarella cheese and remaining sauce mixture. Top with grated Parmesan cheese.\n" +
       "4. Bake for 30 minutes in the preheated oven, or until cheeses are melted.\n";

    "Shiitake Mushroom and Potato Enchiladas\n" +
    "\n" +
    "Serves 4| Hands-On Time: 30m| Total Time: 45m\n" +
    "\n" +
    "Ingredients\n" +
    "1 	 ounce 	 dried shiitake mushrooms (about 1 cup)\n" +
    "2 	 tablespoons 	 olive oil\n" +
    "1 	 white onion, chopped\n" +
    "1 	 large russet potato (about 8 ounces), peeled and diced into 1/4-inch pieces\n" +
    "1 	 cup 	 frozen corn, thawed\n" +
    "kosher salt and black pepper\n" +
    "2 	 cups 	 grated Monterey Jack (about 8 ounces)\n" +
    "1 	 14-ounce can green chili enchilada sauce (about 1 1/2 cups)\n" +
    "8 	 corn tortillas, warmed\n" +
    "fresh cilantro, for serving\n" +
    "\n" +
    "Directions\n" +
    "Heat oven to 400° F. Combine the mushrooms and 2 cups boiling water in a small bowl and let sit until softened, 20 to 30 minutes. Remove the mushrooms and chop; reserve the soaking liquid.\n" +
    "Meanwhile, heat the oil in a large nonstick skillet over medium-high heat. Add the onion and potato and cook, tossing often, until the potato begins to soften, 6 to 8 minutes. Add the corn, mushrooms, 1 cup of the soaking liquid, ½ teaspoon salt, and ¼ teaspoon pepper. Reduce heat to medium and cook, stirring often, until the potatoes are tender and most of the liquid is absorbed, 6 to 8 minutes. Transfer to a large bowl and stir in 1 cup of the Monterey Jack.\n" +
    "Spread ½ cup of the enchilada sauce in the bottom of a 9-by-13-inch baking dish. Dividing evenly, roll up the mushroom mixture in the tortillas and place seam-side down in the dish. Top with the remaining enchilada sauce and sprinkle with the remaining Monterey Jack. Bake until the sauce is bubbling and the Monterey Jack is melted, 12 to 15 minutes. Sprinkle with the cilantro.\n";
**/
       "this tring of text should be the title\n" + 
       "serves 6 cups\n" +
       "cook time is 20 minutes\n" +
       "prep time is 1h overall time is 80mins\n" +
       "4 cups sweet potato, cubed\n" +
       "1/2 cup white sugar\n" +
       "2 eggs, beaten\n" +
       "1/2 teaspoon salt\n" +
       "4 tablespoons butter, softened\n" +
       "1/2 cup milk\n" +
       "1/2 teaspoon vanilla extract\n" +
       "1/2 cup packed brown sugar\n" +
       "1/3 cup all-purpose flour\n" +
       "3 tablespoons butter, softened\n" +
       "1/2 cup chopped pecans\n" +
       "directions and then some\neh?\n\nok then";
                                                                                                                         
    public static void main(String[] args){
 
      
       // Get our WaxeyeParserManager
       WaxeyeParserManager wpm = new WaxeyeParserManager();
 
       final boolean success = wpm.parseString(TEST_RECIPE);
      
       
       RecipeSplitter recSplit = new RecipeSplitter();
       
       try {
          recSplit.splitRecipe(TEST_RECIPE);
       } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
       }
 
       System.out.println("--Title:\n" + recSplit.getTitle() + "\n" +
                          "--Attributes:\n" + recSplit.getAttributes() + "\n" +
                          "--Ingredients List:\n" + recSplit.getIngredientsList() + "\n" +
                          "--Directions:\n" + recSplit.getDirections());
 
    }	
 }
