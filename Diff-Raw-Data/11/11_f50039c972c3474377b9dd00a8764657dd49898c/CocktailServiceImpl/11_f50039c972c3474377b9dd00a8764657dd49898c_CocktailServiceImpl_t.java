 package ua.kiev.naiv.drinkit.cocktail.service.impl;
 
 import org.springframework.stereotype.Service;
 import ua.kiev.naiv.drinkit.cocktail.model.Cocktail;
 import ua.kiev.naiv.drinkit.cocktail.repository.CocktailRepository;
 import ua.kiev.naiv.drinkit.cocktail.service.CocktailService;
 
 import javax.annotation.Resource;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Pavel Kolmykov
  * Date: 20.07.13
  * Time: 21:56
  */
 @Service
 public class CocktailServiceImpl implements CocktailService{
 
     @Resource
     CocktailRepository cocktailRepository;
 
     @Override
     public Cocktail create(Cocktail cocktail) {
        throw new IllegalStateException("Not implemented yet"); //TODO Not implemented
     }
 
     @Override
     public Cocktail delete(int id) {
        throw new IllegalStateException("Not implemented yet"); //TODO Not implemented
     }
 
     @Override
     public List<Cocktail> findAll() {
        throw new IllegalStateException("Not implemented yet"); //TODO Not implemented
     }
 
     @Override
     public Cocktail update(Cocktail cocktail) {
        throw new IllegalStateException("Not implemented yet"); //TODO Not implemented
     }
 
     @Override
     public Cocktail findById(int id) {
         return cocktailRepository.findOne(id);
     }
 }
