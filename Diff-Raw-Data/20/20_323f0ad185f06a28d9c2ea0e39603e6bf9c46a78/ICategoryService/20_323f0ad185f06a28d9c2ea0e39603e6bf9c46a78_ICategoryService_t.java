 package fr.cg95.cvq.service.request;
 
 import java.util.List;
 
 import fr.cg95.cvq.business.authority.Agent;
 import fr.cg95.cvq.business.request.Category;
 import fr.cg95.cvq.business.request.CategoryProfile;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.service.request.annotation.IsCategory;
 import fr.cg95.cvq.service.request.annotation.IsRequestType;
 
 /**
  * @author bor@zenexity.fr
  */
 public interface ICategoryService {
 
     /**
      * Return agents that have a right (read or write) for the given category.
      */
     List<Agent> getAuthorizedForCategory(@IsCategory final Long categoryId)
         throws CvqObjectNotFoundException;
 
     /**
      * Return whether the given agent has at least a
      * {@link CategoryProfile#READ_ONLY} profile on the given category.
      */
     boolean hasProfileOnCategory(final Agent agent, @IsCategory final Long categoryId) 
         throws CvqObjectNotFoundException;
 
     /**
      * Return whether the given agent has at least a
      * {@link CategoryProfile#READ_WRITE} profile on the given category.
      */
     boolean hasWriteProfileOnCategory(Agent agent, @IsCategory Long categoryId) 
         throws CvqObjectNotFoundException;
 
    boolean hasManagerProfile(Agent agent);

     /**
      * Get current agent's profile for this category.
      */
     CategoryProfile getProfileForCategory(final Long categoryId) throws CvqObjectNotFoundException;
 
     /**
      * Get agent's profile for this category.
      */
     CategoryProfile getProfileForCategory(final Long agentId, final Long categoryId) 
         throws CvqObjectNotFoundException;
 
     /**
      * Return the categories for which the current agent has a
      * {@link CategoryProfile#MANAGER MANAGER profile}.
      */
     List<Category> getManaged();
 
     /**
      * Return the categories for which the current agent has at least a
      * {@link CategoryProfile#READ_ONLY profile}.
      */
     List<Category> getAssociated();
     
 
     /**
      * Get the list of all categories that current agent has the right to see.
      * 
      * An administrator can see all categories.
      * An agent can only see categories for which it has at least a
      * {@link CategoryProfile#READ_ONLY read access}.
      */
     List<Category> getAll();
 
     Category getById(@IsCategory final Long id)
         throws CvqObjectNotFoundException;
 
     List<Category> getAgentCategories(final Long agentId);
 
     /**
      * Add a request type to the given category.
      */
     Category addRequestType(@IsCategory final Long categoryId,
         @IsRequestType final Long requestTypeId)
         throws CvqException;
     
     /**
      * Remove a request type from the given category.
      */
     Category removeRequestType(@IsCategory final Long categoryId,
         @IsRequestType final Long requestTypeId)
         throws CvqException;
 
     /**
      * Give an agent a role on a category.
      */
     void addCategoryRole(final Long agentId, final Long categoryId,
         final CategoryProfile categoryProfile) throws CvqException;
 
     /**
      * Modify or add an agent's role on a category.
      */
     public void modifyCategoryRole(final Long agentId, final  Long categoryId,
         final CategoryProfile categoryProfile) throws CvqException;
 
     /**
      * Remove agent's role on a category.
      */
     public void removeCategoryRole(final Long agentId, final  Long categoryId) throws CvqException;
 
     /**
      * Create a category.
      * 
      * @throws CvqModelException if a category with the same name already exists
      * @throws CvqException if an unexpected error happens
      */
     Long create(final Category category)
         throws CvqException, CvqModelException;
 
     void modify(@IsCategory final Category category);
 
     void delete(@IsCategory final Long id)
         throws CvqObjectNotFoundException;
 }
