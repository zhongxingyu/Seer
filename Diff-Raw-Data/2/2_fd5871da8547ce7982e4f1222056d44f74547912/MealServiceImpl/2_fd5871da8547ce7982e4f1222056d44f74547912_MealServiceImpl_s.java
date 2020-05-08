 package com.kcalculator.ejb;
 
 import javax.ejb.Remote;
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceException;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.kcalculator.api.MealService;
 import com.kcalculator.api.data.TOMeal;
 import com.kcalculator.conversion.MealConverter;
 import com.kcalculator.domain.DailyMealPlan;
 import com.kcalculator.domain.Meal;
 import com.kcalculator.ex.DeletionException;
 import com.kcalculator.ex.ObjectNotFoundException;
 import com.kcalculator.ex.ValidationException;
 
 @Stateless
 @Remote(MealService.class)
 @Path("/meal")
 @TransactionAttribute(TransactionAttributeType.REQUIRED)
 public class MealServiceImpl extends ValidatingService implements MealService {
 
     private static final Logger logger = LoggerFactory.getLogger(MealServiceImpl.class);
 
     @PersistenceContext(unitName = "kcalculator")
     EntityManager em;
 
     @POST
     @Consumes("application/json")
     @Produces("application/json")
     @Override
     public TOMeal addMeal(TOMeal mealTO) throws ValidationException, ObjectNotFoundException {
         validateItem(mealTO);
 
         Meal mealToAdd = MealConverter.toDomainObject(mealTO);
 
         mealToAdd.setId(null);
         em.persist(mealToAdd);
 
         return MealConverter.toTransferObject(mealToAdd);
     }
 
     @PUT
     @Consumes("application/json")
     @Produces("application/json")
     @Override
     public TOMeal updateMeal(TOMeal meal) throws ValidationException, ObjectNotFoundException {
         validateItem(meal);
 
         // find meal and apply update
         Meal mealToUpdate = MealConverter.toDomainObject(meal);
         ensureExistence(mealToUpdate, em);
         em.merge(mealToUpdate);
 
         em.flush();
         return MealConverter.toTransferObject(mealToUpdate);
     }
 
     @DELETE
     @Path("{mealId}")
     @Override
     public void deleteMeal(@PathParam("mealId") Long mealId) throws ValidationException, ObjectNotFoundException,
             DeletionException {
         if (mealId == null) {
             throw new IllegalArgumentException("Parameters must not be null");
         }
 
         // find Meal and dailyMealPlan
        Meal mealToDelete = ensureExistence(MealConverter.toDomainObject(toMeal), em);
         DailyMealPlan dailyMealPlan = mealToDelete.getDailyMealPlan();
 
         // remove Meal form DailyMelaPlan
         if (dailyMealPlan != null) {
             dailyMealPlan.getMeals().remove(mealToDelete);
         }
 
         try {
             logger.info("Meal deletion with ID : " + mealToDelete.getId());
             if (dailyMealPlan != null) {
                 em.persist(dailyMealPlan);
             }
             em.remove(mealToDelete);
             em.flush();
         } catch (PersistenceException e) {
             throw new DeletionException(String.format("Failed to delete Meal with id %s", mealToDelete.getId()), e);
         }
     }
 
     @GET
     @Produces("application/json")
     @Path("/{mealId}")
     @Override
     public TOMeal getMeal(@PathParam("mealId") Long mealId) throws ObjectNotFoundException {
         if (mealId == null) {
             throw new IllegalArgumentException("Parameters must not be null");
         }
         Meal result = ensureExistence(Meal.class, mealId, em);
         return MealConverter.toTransferObject(result);
     }
 }
