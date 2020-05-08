 package de.hswt.hrm.inspection.dao.core;
 
 import java.util.Collection;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.inspection.model.Biological;
 
 /**
  * Defines all the public methods to interact with the storage system for activitys.
  */
 public interface IBiologicalDao {
 
     /**
     * @return All biologicals from storage.
      */
     Collection<Biological> findAll() throws DatabaseException;
 
     /**
      * @param id of the target biological.
      * @return biological with the given id.
      * @throws ElementNotFoundException If the given id is not present in the database.
      */
     Biological findById(int id) throws DatabaseException, ElementNotFoundException;
 
     /**
      * Add a new biological to storage.
      * @param biological Biological that should be stored.
      * @return Newly generated biological (also holding the correct id).
      * @throws SaveException If the biological could not be inserted.
      */
     Biological insert(Biological biological) throws SaveException;
 
     /**
      * Update an existing biological in storage.
      * 
      * @param biological Biological that should be updated.
      * @throws ElementNotFoundException If the given Biological is not present in the database.
      * @throws SaveException If the biological could not be updated.
      */
     void update(Biological biological) throws ElementNotFoundException, SaveException;
 }
 
