 package edu.chl.dat076.foodfeed.controller;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import edu.chl.dat076.foodfeed.model.dao.RecipeDao;
 import edu.chl.dat076.foodfeed.model.entity.Recipe;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Handles requests for recipes.
  */
 @Controller
 @RequestMapping("/recipes")
 @Transactional
 public class RecipeController {
 
     @Autowired
     private RecipeDao recipeDao;
     private static final Logger logger = LoggerFactory
             .getLogger(RecipeController.class);
 
     /**
      * Shows a list of recipes
      */
     @RequestMapping(value = "", method = RequestMethod.GET)
     public String list(Model model) {
         List<Recipe> recipes = recipeDao.findAll();
         logger.info("Listing " + recipes.size() + " recipes.");
         model.addAttribute("recipes", recipes);
         return "recipes/list";
     }
 
     /**
      * Form to add a recipe
      */
     @RequestMapping(value = "/add", method = RequestMethod.GET)
     @Secured("ROLE_USER")
     public String addForm(Model model) {
         logger.info("Showing form to add a Recipe.");
         model.addAttribute("recipe", new Recipe());
         return "recipes/add";
     }
 
     /**
      * Creates a recipe
      */
    @RequestMapping(value = "/", method = RequestMethod.POST)
     @Secured("ROLE_USER")
    public String add(Model model, @Validated Recipe recipe) {
         logger.info("Saving a new recipe.");
         recipeDao.create(recipe);
         return "redirect:/recipes/" + recipe.getId();
     }
 
     /**
      * Shows a recipe
      */
     @RequestMapping(value = "/{id}", method = RequestMethod.GET)
     public String show(@PathVariable long id, Model model) {
         logger.info("Showing recipe with ID " + id + ".");
         model.addAttribute("recipe", recipeDao.find(id));
         return "recipes/show";
     }
 
     /**
      * Deletes a recipe
      */
     @RequestMapping(value = "/{id}/delete", method = RequestMethod.GET)
     public String delete(@PathVariable long id, Model model) {
         logger.info("Deleting recipe with ID " + id + ".");
         recipeDao.delete(id);
         return "redirect:/recipes";
     }
 }
