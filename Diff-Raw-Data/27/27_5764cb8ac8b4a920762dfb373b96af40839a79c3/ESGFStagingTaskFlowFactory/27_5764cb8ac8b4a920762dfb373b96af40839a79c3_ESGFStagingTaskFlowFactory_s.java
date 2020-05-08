 package de.zib.gndms.taskflows.esgfStaging.server;
 
 
 import de.zib.gndms.common.model.gorfx.types.TaskFlowInfo;
 import de.zib.gndms.common.model.gorfx.types.TaskStatistics;
 import de.zib.gndms.infra.GridConfig;
 import de.zib.gndms.infra.SettableGridConfig;
 import de.zib.gndms.kit.config.MandatoryOptionMissingException;
 import de.zib.gndms.kit.config.MapConfig;
 import de.zib.gndms.logic.model.gorfx.taskflow.DefaultTaskFlowFactory;
 import de.zib.gndms.neomodel.common.Dao;
 import de.zib.gndms.neomodel.common.Session;
 import de.zib.gndms.neomodel.gorfx.TaskFlow;
 import de.zib.gndms.neomodel.gorfx.TaskFlowType;
 import de.zib.gndms.stuff.threading.PeriodicalJob;
 import de.zib.gndms.taskflows.esgfStaging.client.ESGFStagingTaskFlowMeta;
 import de.zib.gndms.taskflows.esgfStaging.client.model.ESGFStagingOrder;
 import de.zib.gndms.voldmodel.Adis;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.inject.Inject;
 import java.util.*;
 
 /**
  * @author bachmann@zib.de
  * @date 08.03.12  16:10
  */
 public class ESGFStagingTaskFlowFactory extends DefaultTaskFlowFactory< ESGFStagingOrder, ESGFStagingQuoteCalculator > {
 
     private VoldRegistrar registrar;
     private Adis adis;
     private GridConfig gridConfig;
 
     private Dao dao;
 
     private TaskStatistics stats = new TaskStatistics();
 
 
     public ESGFStagingTaskFlowFactory() {
         super( ESGFStagingTaskFlowMeta.TASK_FLOW_TYPE_KEY, ESGFStagingQuoteCalculator.class, ESGFStagingOrder.class );
     }
 
 
     @Override
     public int getVersion() {
         return 0;  // not required here
     }
 
 
     public ESGFStagingQuoteCalculator getQuoteCalculator() throws MandatoryOptionMissingException {
        ESGFStagingTFAction action = createAction();

        final String trustStoreLocation = action.getOfferTypeConfig().getOption( "trustStoreLocation" );
        final String trustStorePassword = action.getOfferTypeConfig().getOption( "trustStorePassword" );
 
         ESGFStagingQuoteCalculator qc = new ESGFStagingQuoteCalculator( trustStoreLocation, trustStorePassword );
 
         getInjector().injectMembers( qc );
 
         return qc;
     }
 
 
     public TaskFlowInfo getInfo() {
         return new TaskFlowInfo() {
             private TaskStatistics statistics = stats;
             public TaskStatistics getStatistics() {
                 return statistics;
             }
 
 
             public String getDescription() {
                 return null;  // not required here
             }
 
 
             @Override
             public List<String> requiredAuthorization() {
 
                 return ESGFStagingTaskFlowMeta.REQUIRED_AUTHORIZATION;
             }
         };
     }
 
 
     public TaskFlow< ESGFStagingOrder > create() {
         stats.setActive( stats.getActive() + 1 );
         return super.create();
     }
 
 
     @Override
     protected TaskFlow< ESGFStagingOrder > prepare( TaskFlow< ESGFStagingOrder > esgfStagingOrderTaskFlow ) {
         return esgfStagingOrderTaskFlow;
     }
 
 
     public void delete( String id ) {
         stats.setActive( stats.getActive() - 1 );
         super.delete( id );
     }
 
 
     @Override
     protected Map<String, String> getDefaultConfig() {
         final Map< String, String > defaultConfig = new HashMap<String, String>( );
 
         final SettableGridConfig gridConfig = getInjector().getInstance(SettableGridConfig.class);
         String trustStoreLocation;
         try {
             trustStoreLocation = gridConfig.getGridPath() + "/esgf.truststore";
         } catch( Exception e ) {
             logger.debug( "This is not happening!", e );
             trustStoreLocation = "";
         }
 
         defaultConfig.put( "subspace", "esgfStaging" );
         defaultConfig.put( "sliceKind", "esgfKind");
         defaultConfig.put( "trustStoreLocation", trustStoreLocation );
         defaultConfig.put( "trustStorePassword", "gndmstrust" );
         
         return defaultConfig;
     }
 
 
     @Override
     public ESGFStagingTFAction createAction() {
         ESGFStagingTFAction action = new ESGFStagingTFAction(  );
         getInjector().injectMembers( action );
 
         try {
             action.setQuoteCalculator( getQuoteCalculator() );
         } catch (MandatoryOptionMissingException e) {
             throw new IllegalStateException( "Could not create Action, since it is not configured properly.", e );
         }
         action.setOwnDao( this.dao );
 
         return action;
     }
 
 
     @SuppressWarnings( "SpringJavaAutowiringInspection" )
     @Inject
     public void setGridConfig( final SettableGridConfig gridConfig ) {
         this.gridConfig = gridConfig;
     }
 
 
     @SuppressWarnings( "SpringJavaAutowiringInspection" )
     @Inject
     public void setAdis( final Adis adis ) {
         this.adis = adis;
     }
 
 
     public Dao getDao() {
         return dao;
     }
 
 
     @SuppressWarnings("SpringJavaAutowiringInspection")
     @Inject
     public void setDao( final Dao dao ) {
         this.dao = dao;
     }
 
 
     @PostConstruct
     public void startVoldRegistration() throws Exception {
         MapConfig config = new MapConfig( getConfigMapData() );
         if( !config.hasOption( "oidPrefixe" ) ) {
             logger.error( "Dataprovider not configured: no OID_PREFIXE given." );
         }
 
         registrar = new VoldRegistrar( adis, gridConfig.getBaseUrl() );
         registrar.start();
     }
 
 
     @PreDestroy
     public void stopVoldRegistration() {
         registrar.stop();
     }
 
 
     private class VoldRegistrar extends PeriodicalJob {
         final private Adis adis;
         final private String gorfxEP;
         public VoldRegistrar( final Adis adis, final String gorfxEP ) {
             this.adis = adis;
             this.gorfxEP = gorfxEP;
         }
 
 
         @Override
         public String getName() {
             return "ESGFStagingVoldRegistrar";
         }
 
 
         @Override
         public Long getPeriod() {
             final MapConfig config = new MapConfig( getConfigMapData() );
 
             return config.getLongOption( "updateInterval", 60000 );
         }
 
 
         @Override
         public void call() throws Exception {
             final MapConfig config = new MapConfig( getConfigMapData() );
 
             adis.setESGFStager( getName(), gorfxEP );
         }
     }
 
 
     public Map< String, String > getConfigMapData() {
         final Session session = getDao().beginSession();
         try {
             final TaskFlowType taskFlowType = session.findTaskFlowType( ESGFStagingTaskFlowMeta.TASK_FLOW_TYPE_KEY );
             final Map< String, String > configMapData = taskFlowType.getConfigMapData();
             session.finish();
             return configMapData;
         }
         finally { session.success(); }
     }
 }
