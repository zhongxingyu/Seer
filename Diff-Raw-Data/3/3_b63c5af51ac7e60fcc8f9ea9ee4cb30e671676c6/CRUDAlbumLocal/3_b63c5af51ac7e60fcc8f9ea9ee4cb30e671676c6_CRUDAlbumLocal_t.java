 package ai.ilikeplaces.logic.crud.unit;
 
 import ai.ilikeplaces.doc.License;
 import ai.ilikeplaces.entities.Album;
 import ai.ilikeplaces.exception.DBDishonourCheckedException;
 import ai.ilikeplaces.exception.DBFetchDataException;
 import ai.ilikeplaces.util.jpa.RefreshException;
 import ai.ilikeplaces.util.jpa.RefreshSpec;
 
 import javax.ejb.Local;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 
 /**
  * @author Ravindranath Akila
  */
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 @Local
 public interface CRUDAlbumLocal {
 
     /**
      *
      * @param privateEventId
      * @param humanId
      * @param photoUrl
      * @return
      * @throws DBDishonourCheckedException
      * @throws DBFetchDataException
      */
     public Album doUAlbumOfPrivateEventAddEntry(final long privateEventId, final String humanId, final String photoUrl) throws DBDishonourCheckedException, DBFetchDataException;
 
     /**
      * @param humanId
      * @param privateEventId by which to fetch the album
      * @param eager
      * @return
      * @throws DBDishonourCheckedException
      */
     @Deprecated
     public Album doRAlbumByPrivateEvent(final String humanId, final long privateEventId, final boolean eager) throws DBDishonourCheckedException, DBFetchDataException;
 
 
     /**
      * @param humanId
      * @param privateEventId by which to fetch the album
      * @param refreshSpec
      * @return
      * @throws DBDishonourCheckedException
      */
     public Album doRAlbumByPrivateEvent(final String humanId, final long privateEventId, final RefreshSpec refreshSpec) throws DBDishonourCheckedException, DBFetchDataException;
 
 
     /**
      *
      * @param tribeId
      * @param humanId
      * @param photoUrl
      * @return
      * @throws DBDishonourCheckedException
      * @throws DBFetchDataException
      */
     public Album doUAlbumOfTribeAddEntry(final long tribeId, final String humanId, final String photoUrl) throws DBDishonourCheckedException, DBFetchDataException, RefreshException;
 
 
     /**
      * @param humanId
      * @param tribeId by which to fetch the album
      * @param refreshSpec
      * @return
      * @throws DBDishonourCheckedException
      */
     public Album doRAlbumByTribe(final String humanId, final long tribeId, final RefreshSpec refreshSpec) throws DBDishonourCheckedException, DBFetchDataException;
 }
