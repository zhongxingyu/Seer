 package polly.rx.core.orion.datasource;
 
 import polly.rx.core.orion.CachedQuadrantProvider;
 import polly.rx.core.orion.QuadrantProvider;
 import de.skuzzle.polly.sdk.PersistenceManagerV2;
 
 
 public final class DBOrionAccess {
 
     
     private final DBQuadrantUpdater quadUpdater;
     private final DBPortalProvider portalProvider;
     private final DBPortalUpdater portalUpdater;
     private final CachedQuadrantProvider cachedQuadProvider;
     
     
     public DBOrionAccess(PersistenceManagerV2 persistence) {
         final DBQuadrantProvider dbQuadProvider = new DBQuadrantProvider(persistence);
         this.quadUpdater = new DBQuadrantUpdater(persistence);
         this.portalProvider = new DBPortalProvider(persistence);
         this.portalUpdater = new DBPortalUpdater(persistence, this.quadUpdater);
         this.cachedQuadProvider = new CachedQuadrantProvider(dbQuadProvider);
         this.quadUpdater.addQuadrantListener(this.cachedQuadProvider);
     }
     
     
     
     
     public QuadrantProvider getQuadrantProvider() {
         return this.cachedQuadProvider;
     }
     
     
     
     public DBQuadrantUpdater getQuadrantUpdater() {
         return this.quadUpdater;
     }
     
     
     
     public DBPortalProvider getPortalProvider() {
         return this.portalProvider;
     }
     
     
     
     public DBPortalUpdater getPortalUpdater() {
         return this.portalUpdater;
     }
 }
