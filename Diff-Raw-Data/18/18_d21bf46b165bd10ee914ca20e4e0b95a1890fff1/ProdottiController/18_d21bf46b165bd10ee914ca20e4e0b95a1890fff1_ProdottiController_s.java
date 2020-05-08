 package it.aip.controller;
 
 import it.aip.models.*;
 import it.aip.service.OrganicProductService;

 import org.slim3.controller.Controller;
 import org.slim3.controller.Navigation;
 import org.slim3.datastore.Datastore;

 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 
 public class ProdottiController extends Controller {
     
     private OrganicProductService organicProductService = new OrganicProductService();
 
     @Override
     public Navigation run() throws Exception {
        
         List<OrganicProduct> products;
         String categoria = request.getParameter("categoria");
         if(categoria != null){
             if(categoria.equals("all")){
                 products = organicProductService.getAllOrganicProducts();
             }
             else {
                 products = organicProductService.getProductByCategory(categoria);
             }
             requestScope("prodotti", products);
             requestScope("categoria", categoria);
             return forward("prodottiByCategory.jsp");
         }
         List<String> categories = organicProductService.getAllCategories();
         requestScope("categorie", categories);
         
         return forward("prodotti.jsp");
     }
 }
