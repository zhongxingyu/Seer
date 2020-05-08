 package ai.ilikeplaces.logic.crud.unit;
 
 import ai.ilikeplaces.doc.License;
 import ai.ilikeplaces.entities.HumansIdentity;
 import ai.ilikeplaces.entities.Url;
 import ai.ilikeplaces.exception.DBDishonourCheckedException;
 import ai.ilikeplaces.jpa.CrudServiceLocal;
 import ai.ilikeplaces.util.AbstractSLBCallbacks;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 
 /**
  * @author Ravindranath Akila
  */
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 @Stateless
 public class UHumansIdentity extends AbstractSLBCallbacks implements UHumansIdentityLocal {
 
     @EJB
     private RHumansIdentityLocal rHumansIdentityLocal_;
 
     @EJB
     private CrudServiceLocal<Url> urlCrudServiceLocal_;
 
     public static final DBDishonourCheckedException DB_DISHONOUR_CHECKED_EXCEPTION = new DBDishonourCheckedException("Updating to same value is absurd");
 
     public UHumansIdentity() {
     }
 
 
     @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
     @Override
     public HumansIdentity doUHumansProfilePhoto(final String humanId, final String url) throws DBDishonourCheckedException {
         final HumansIdentity humansIdentity = rHumansIdentityLocal_.doRHumansIdentity(humanId);
         humansIdentity.setHumansIdentityProfilePhoto(url);
         return humansIdentity;
     }
 
     @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
     @Override
     public HumansIdentity doUHumansPublicURL(final String humanId, final String url) throws DBDishonourCheckedException {
 
         doUHumansPublicURLDelete(humanId);
         doUHumansPublicURLAdd(humanId,url);
 
        return rHumansIdentityLocal_.doRHumansIdentity(humanId);
     }
 
    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
     @Override
     public void doUHumansPublicURLDelete(final String humanId) throws DBDishonourCheckedException {
        final String oldUrl =  rHumansIdentityLocal_.doRHumansIdentity(humanId).getUrl().getUrl();
        urlCrudServiceLocal_.delete(Url.class,oldUrl);
     }
 
 
    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
     @Override
     public void doUHumansPublicURLAdd(final String humanId, final String url) throws DBDishonourCheckedException {
         rHumansIdentityLocal_.doRHumansIdentity(humanId).setUrl(new Url().setUrlR(url).setMetadataR(humanId).setTypeR(Url.typeHUMAN));
     }
 }
