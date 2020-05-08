 package angafe.controller.angafe;
 
 import java.util.List;
 
 import org.slim3.controller.Controller;
 import org.slim3.controller.Navigation;
 import org.slim3.datastore.Datastore;
 
 import angafe.model.Product;
 import angafe.model.Recipe;
 import angafe.service.ProductService;
 import angafe.service.RecipeService;
 
 import com.google.appengine.api.datastore.Key;
 
 public class RecipeController extends Controller {
 
     RecipeService recipeService = new RecipeService();
     ProductService productService = new ProductService();
     
     @Override
     public Navigation run() throws Exception {
         
         String backLinkTitle = "";
         String backLink = "";
         String backLinkVisibility = "hidden";
         
         String index = request.getParameter("index");
         if(index != null && index.equals("true")) {
             backLinkTitle = "back to all recipes";
             backLink = "/angafe/recipes";
             backLinkVisibility = "visibile";
         }
 
         String groupLinkBackTitle = "";
         String groupLinkBack = "#";
         String groupLinkBackVisibility = "hidden";
         String groupLinkForwardTitle = "";
         String groupLinkForward = "#";
         String groupLinkForwardVisibility = "hidden";
         
         //Recupero l'id dalla request e lo trasformo in long
         long id = Long.parseLong((String)request.getAttribute("id"));
         //Creo una chiave con quell'id
         Key recKey = Datastore.createKey(Recipe.class, id);
         
         //Ottengo la ricetta dal datastore tramite la chiave
         Recipe recipe = recipeService.getRecipe(recKey);
         List<Product> products = productService.getProducts(recipe);
         
         String tour = request.getParameter("tour");
         if(tour != null) {
            if(tour.equals("product")) {
                 ProductService productService = new ProductService();
                 long productId = Long.parseLong((String)request.getParameter("productId"));
                 Key productKety = Datastore.createKey(Product.class, productId);
                 Product product = productService.getProduct(productKety);
                 List<Recipe> productRecipes = recipeService.getRecipes(product);
                 backLinkTitle = "back to "+product.getName();
                 backLink = "/angafe/product?id="+productId;
                 backLinkVisibility = "visibile";
                System.out.println(productRecipes);
                 
                if (productRecipes.indexOf(recipe) != 0) {
                     //Se il prodotto corrente NON  il primo della lista
                     Recipe prevRecipe = productRecipes.get(productRecipes.indexOf(recipe) - 1);
                     groupLinkBack = "/angafe/recipe?id="+prevRecipe.getKey().getId()+"&tour=product&productId="+productId;
                     groupLinkBackTitle = "Previous product";
                     groupLinkBackVisibility = "visible";
                 }
 
                if (productRecipes.indexOf(recipe) != productRecipes.size() - 1 ) {
                     //Se il prodotto corrente NON  l'ultimo della lista
                     Recipe nextRecipe = productRecipes.get(productRecipes.indexOf(recipe) + 1);
                     groupLinkForward = "/angafe/recipe?id="+nextRecipe.getKey().getId()+"&tour=product&productId="+productId;
                     groupLinkForwardTitle = "Next product";
                     groupLinkForwardVisibility = "visible";
                 }
             }
         }
         
         //Rendo accessibile la variabile
         requestScope("recipe",recipe);
         requestScope("products",products);
         
         requestScope("backLink",backLink);
         requestScope("backLinkTitle", backLinkTitle);
         requestScope("backLinkVisibility", backLinkVisibility);
 
         requestScope("groupLinkBackVisibility", groupLinkBackVisibility);
         requestScope("groupLinkForwardVisibility", groupLinkForwardVisibility);
         requestScope("groupLinkBack",groupLinkBack);
         requestScope("groupLinkBackTitle",groupLinkBackTitle);
         requestScope("groupLinkForward",groupLinkForward);
         requestScope("groupLinkForwardTitle",groupLinkForwardTitle);
         
         //Mostro il jsp
         return forward("recipe.jsp");
     }
 }
