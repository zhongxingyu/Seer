 /* 
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.config.hibernate;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.filefilter.SuffixFileFilter;
 import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.StyleInfo;
 import org.geoserver.catalog.Wrapper;
 import org.geoserver.config.GeoServer;
 import org.geoserver.config.GeoServerInfo;
 import org.geoserver.config.GeoServerInitializer;
 import org.geoserver.config.JAIInfo;
 import org.geoserver.config.LoggingInfo;
 import org.geoserver.config.ServiceInfo;
 import org.geoserver.config.ServiceLoader;
 import org.geoserver.platform.GeoServerExtensions;
 import org.geoserver.platform.GeoServerResourceLoader;
 import org.geoserver.wcs.WCSInfo;
 import org.geoserver.wfs.WFSInfo;
 import org.geoserver.wms.WMSInfo;
 import org.geotools.util.logging.Logging;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.web.context.WebApplicationContext;
 import org.vfny.geoserver.global.GeoserverDataDirectory;
 
 /**
  * Base class for initializing based on GeoServer configuration.
  * <p>
  * This class should  be subclassed in cases where some sort of system 
  * initialization must be carried out, and aspects of the initialization depend 
  * on GeoServer configuration.
  * </p>
  * <p>
  * Instances of this class should be registered in a spring context. Example:
  * <pre>
  *  &lt;bean id="geoServerLoader" class="GeoServerLoader"/>
  * </pre>
  * </p>
  
  * @author Justin Deoliveira, The Open Planning Project
  *
  */
 public class HibGeoServerLoader
         implements 
 //        BeanPostProcessor,
         DisposableBean, ApplicationContextAware {
 
     private final static Logger LOGGER = Logging.getLogger( HibGeoServerLoader.class );
     
     ApplicationContext applicationContext;
     GeoServerResourceLoader resourceLoader;
 
     GeoServer geoserver = null;
     Catalog catalog = null;
     boolean initDone = false;
     boolean tmInitted = false;
     
 //    public HibGeoServerLoader( GeoServerResourceLoader resourceLoader ) {
 //        this.resourceLoader = resourceLoader;
 //    }
     public HibGeoServerLoader( GeoServerResourceLoader resourceLoader, HibGeoServerImpl geoserver, Catalog catalog ) {
         this.resourceLoader = resourceLoader;
         this.geoserver = geoserver.getProxy();
         this.catalog = catalog;
 
         initialize();
     }
 
     public void setApplicationContext(ApplicationContext applicationContext)
             throws BeansException {
 
 //        if( ! (applicationContext instanceof WebApplicationContext))
 //            throw new IllegalArgumentException("Expecting a WebApplicationContext, got a " + applicationContext.getClass().getName());
 
         GeoserverDataDirectory.init((WebApplicationContext)applicationContext);
         this.applicationContext = applicationContext;
     }
     
     public final Object postProcessAfterInitialization(Object bean, String beanName)
             throws BeansException {
 //        LOGGER.warning("postProcessAfterInitialization " + beanName + " : " + bean.getClass().getSimpleName());
         return bean;
     }
 
     public final Object postProcessBeforeInitialization(Object bean, String beanName)
             throws BeansException {
 //        LOGGER.warning("postProcessBeforeInitialization " + beanName + " : " + bean.getClass().getSimpleName());
 //
 //        if ( bean instanceof GeoServer ) {
 //            LOGGER.warning("Initializing "+bean.getClass().getSimpleName()+" with " + resourceLoader + " ("+resourceLoader.getClass().getName()+") " );
 //            geoserver = (GeoServer) bean;
 //        } else if ( bean instanceof Catalog ) {
 //            LOGGER.warning("Initializing "+bean.getClass().getSimpleName()+" with " + resourceLoader + " ("+resourceLoader.getClass().getName()+") " );
 //            catalog = (Catalog) bean;
 //        }
 //
 //        if( "transactionManager".equals(beanName))
 //            tmInitted = true;
 //
 //        if(geoserver != null && catalog != null && tmInitted && ! initDone) {
 //            initialize();
 //            initDone = true;
 //        }
 //
         return bean;
     }
     
     protected void initialize() {
         LOGGER.warning("Initializing " + this.getClass().getSimpleName() );
 
         GeoServerInfo global = geoserver.getGlobal();
         if(global == null) {            
             global =  HibDefaultsFactoryImpl.createDefaultGeoServer(geoserver);
 //            geoserver.setGlobal(global);
 
             HibDefaultsFactoryImpl.createDefaultServices(geoserver, global);
             HibDefaultsFactoryImpl.createDefaultXXXXspace(geoserver);
 
             initializeStyles(catalog);
 
         } else {
             JAIInfo jai = global.getJAI();
             // we are missing the JAI info. This can be due by the fact that previous
             // run was made by a unit test
             if(jai == null) {
                 LOGGER.warning("Global is missing JAI info. Setting default ones...");
                 global.setJAI(geoserver.getFactory().createJAI());
                 geoserver.setGlobal(global);
 
 //                JAIInitializer initializer = new JAIInitializer();
 //                initializer.initialize(geoserver);
 
             }
 
             WMSInfo wms = geoserver.getService(WMSInfo.class);
             if(wms == null) {
                 LOGGER.warning("Missing WMS service. Creating default one.");
                 HibDefaultsFactoryImpl.createWMS(global, geoserver);
             }
 
             WCSInfo wcs = geoserver.getService(WCSInfo.class);
             if(wcs == null) {
                 LOGGER.warning("Missing WCS service. Creating default one.");
                 HibDefaultsFactoryImpl.createWCS(global, geoserver);
             }
 
             WFSInfo wfs = geoserver.getService(WFSInfo.class);
             if(wfs == null) {
                 LOGGER.warning("Missing WFS service. Creating default one.");
                 HibDefaultsFactoryImpl.createWFS(global, geoserver);
             }
             
         }
 
         // LOGGING
         LoggingInfo loggingInfo = geoserver.getLogging();
         if(loggingInfo == null ) {
             LOGGER.warning("Missing logging info. Creating default.");
             loggingInfo = this.geoserver.getFactory().createLogging();
             geoserver.setLogging(loggingInfo);
         }
 
 /*
         //first thing to do is load the logging configuration
         LegacyLoggingImporter loggingImporter = new LegacyLoggingImporter( geoserver );
         try {
             loggingImporter.imprt( resourceLoader.getBaseDirectory() );
         } 
         catch (Exception e) {
             throw new RuntimeException( e );
         }
         
         try {
             LoggingInitializer loggingIniter = new LoggingInitializer();
             loggingIniter.setResourceLoader( resourceLoader );
             loggingIniter.initialize( geoserver, applicationContext );
         } 
         catch (Exception e) {
             throw new RuntimeException(e);
         }
 */
 
         // load catalog
 //        LegacyCatalogImporter catalogImporter = new LegacyCatalogImporter();
 //        catalogImporter.setResourceLoader(resourceLoader);
         Catalog catalog = geoserver.getCatalog();
         catalog.setResourceLoader(resourceLoader);
 
         if(catalog instanceof Wrapper && ((Wrapper) catalog).isWrapperFor(Catalog.class)) {
             catalog = ((Wrapper) catalog).unwrap(Catalog.class);
         }
 //        catalogImporter.setCatalog(catalog);
 //
 //        try {
 //            catalogImporter.imprt( resourceLoader.getBaseDirectory() );
 //            initializeStyles(catalog);
 //        }
 //        catch(Exception e) {
 //            throw new RuntimeException( e );
 //        }
 
 /*
         //load configuration
         LegacyConfigurationImporter importer = new LegacyConfigurationImporter();
         importer.setConfiguration(geoserver);
         
         try {
             importer.imprt( resourceLoader.getBaseDirectory() );
         } 
         catch (Exception e) {
             throw new RuntimeException( e );
         }
 */
         System.out.println("---------------_> geoserver is " + geoserver.getClass().getName());
         System.out.println("---------------_> global is is " + geoserver.getGlobal().getClass().getName());
         System.out.println("---------------_> metadata is  " + geoserver.getGlobal().getMetadata());
 
         if(geoserver.getGlobal().getMetadata() != null) {
             for (String key : geoserver.getGlobal().getMetadata().keySet()) {
                 System.out.println("--- METADATA KEY: " + key);
             }
         }
 
         //load initializer extensions
         List<GeoServerInitializer> initializers = GeoServerExtensions.extensions( GeoServerInitializer.class );
         for ( GeoServerInitializer initer : initializers ) {
             try {
                 initer.initialize( geoserver );
             }
             catch( Throwable t ) {
                 //TODO: log this
                 t.printStackTrace();
             }
         }
   
         //load listeners
 //        List<CatalogListener> catalogListeners = GeoServerExtensions.extensions( CatalogListener.class );
 //        for ( CatalogListener l : catalogListeners ) {
 //            catalog.addListener( l );
 //        }
 //        List<ConfigurationListener> configListeners = GeoServerExtensions.extensions( ConfigurationListener.class );
 //        for ( ConfigurationListener l : configListeners ) {
 //            geoserver.addListener( l );
 //        }
  
     }
     
     /**
      * Does some post processing on the catalog to ensure that the "well-known" styles
      * are always around.
      */
     void initializeStyles( Catalog catalog ) {
         if ( catalog.getStyleByName( StyleInfo.DEFAULT_POINT ) == null ) {
             initializeStyle( catalog, StyleInfo.DEFAULT_POINT, "default_point.sld" );
         }
         if ( catalog.getStyleByName( StyleInfo.DEFAULT_LINE ) == null ) {
             initializeStyle( catalog, StyleInfo.DEFAULT_LINE, "default_line.sld" );
         }
         if ( catalog.getStyleByName( StyleInfo.DEFAULT_POLYGON ) == null ) {
             initializeStyle( catalog, StyleInfo.DEFAULT_POLYGON, "default_line.sld" );
         }
         if ( catalog.getStyleByName( StyleInfo.DEFAULT_RASTER ) == null ) {
            initializeStyle( catalog, StyleInfo.DEFAULT_RASTER, "default_raster.sld" );
         }
 
         File styleDir = null;
         try {
             styleDir = resourceLoader.find("styles");
         } catch (IOException ex) {
             Logger.getLogger(HibGeoServerLoader.class.getName()).log(Level.SEVERE, null, ex);
         }
         if(styleDir == null) {
             LOGGER.severe("Can't find styles dir.");
             return;
         }
 
         String[] sldlist = styleDir.list(new SuffixFileFilter(".sld"));
         for (String sldfilename : sldlist) {
             String stylename = FilenameUtils.getBaseName(sldfilename);
             if(catalog.getStyleByName(stylename) != null) {
                 LOGGER.info("Style " + stylename + "exists.");
                 continue;
             }
 
             StyleInfo style = this.catalog.getFactory().createStyle();
             style.setName(stylename);
             style.setFilename(FilenameUtils.getName(sldfilename));
 
             try {
                 LOGGER.warning("Importing SLD " + style.getName() + " in file " + sldfilename);
                 catalog.add(style);
             } catch (Exception e) {
                 LOGGER.severe("Could not import SLD " + style.getName() + " in file " + sldfilename);
             }
         }
 
     }
     
     /**
      * Copies a well known style out to the data directory and adds a catalog entry for it.
      */
     private void initializeStyle( Catalog catalog, String styleName, String sld ) {
         
         //copy the file out to the data directory if necessary
         try {
             if (resourceLoader.find("styles", sld) == null) {
                FileUtils.copyURLToFile(getClass().getResource(sld),
                        new File(resourceLoader.find("styles"), sld));
             }
        } catch (Exception ex) {
             LOGGER.log(Level.SEVERE, "Could not import SLD " + styleName, ex);
             return;
         }
         
         //create a style for it
         StyleInfo s = catalog.getFactory().createStyle();
         s.setName( styleName );
         s.setFilename( sld );
         catalog.add( s );
     }
     
     public void reload() throws Exception {
         destroy();
         initialize();
     }
     
     public void destroy() throws Exception {
         //TODO: persist catalog
         //TODO: persist global
 
         //persist services
         Collection services = geoserver.getServices();
         List<ServiceLoader> loaders = GeoServerExtensions.extensions( ServiceLoader.class );
         
         for ( Iterator s = services.iterator(); s.hasNext(); ) {
             ServiceInfo service = (ServiceInfo) s.next();
             for ( ServiceLoader loader : loaders ) {
                if ( loader.getServiceClass().equals(service.getClass()) ) { // ETJ FIXMEEEEEEE
                     try {
                         loader.save( service, geoserver );
                         break;
                     }
                     catch( Throwable t ) {
                         LOGGER.warning( "Error persisting service: " + service.getId() );
                         LOGGER.log( Level.INFO, "", t );
                     }
                 }
             }
         }
         
         //dispose
         geoserver.dispose();
     }
 }
