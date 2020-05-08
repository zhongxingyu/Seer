 package de.zib.gndms.GORFX.service;
 
 import de.zib.gndms.GORFX.ORQ.service.globus.resource.ExtORQResourceHome;
 import de.zib.gndms.GORFX.ORQ.service.globus.resource.ORQResource;
 import de.zib.gndms.GORFX.context.service.globus.resource.ExtTaskResourceHome;
 import de.zib.gndms.GORFX.offer.service.globus.resource.ExtOfferResourceHome;
 import de.zib.gndms.GORFX.service.globus.GORFXAuthorization;
 import de.zib.gndms.GORFX.service.globus.resource.ExtGORFXResourceHome;
 import de.zib.gndms.infra.access.ServiceHomeProvider;
 import de.zib.gndms.infra.system.GNDMSystem;
 import de.zib.gndms.infra.system.WSMaintenance;
 import de.zib.gndms.kit.util.WidAux;
 import de.zib.gndms.shared.ContextTAux;
 import de.zib.gndms.typecon.util.AxisTypeFromToXML;
 import org.apache.log4j.Logger;
 import org.globus.wsrf.ResourceKey;
 import org.jetbrains.annotations.NotNull;
 
 import java.io.StringWriter;
 import java.rmi.RemoteException;
 
 
 /**
  * TODO:I am the service side implementation class.  IMPLEMENT AND DOCUMENT ME
  * 
  * @created by Introduce Toolkit version 1.2
  * 
  */
 public class GORFXImpl extends GORFXImplBase {
 
     private static final Logger logger;
     private final GNDMSystem system;
     private WSMaintenance maintenance;
 
 
     static {
         logger = Logger.getLogger(GORFXImpl.class);
     }
 
 
     @SuppressWarnings({ "FeatureEnvy" })
     public GORFXImpl() throws RemoteException {
         super();
         try {
             final @NotNull ExtGORFXResourceHome home = getResourceHome();
             system = home.getSystem();
             ServiceHomeProvider instanceDir = system.getInstanceDir();
             instanceDir.addHome(home);
             instanceDir.addHome(getORQResourceHome());
             instanceDir.addHome(getOfferResourceHome());
             instanceDir.addHome(getTaskResourceHome());
         }
         catch (Exception e) {
             e.printStackTrace(System.err);
             throw new RuntimeException(e);
         }
         finally {
             WidAux.removeWid();
         }
     }
 
 
     public org.apache.axis.message.addressing.EndpointReferenceType createOfferRequest(types.DynamicOfferDataSeqT offerRequestArguments,types.ContextT context) throws RemoteException, de.zib.gndms.GORFX.stubs.types.UnsupportedOfferType {
 
         try{
             ContextTAux.initWid(getSystem().getModelUUIDGen(), context);
             logSecInfo( "createOfferRequest" );
 
             @NotNull ExtORQResourceHome home = getORQResourceHome();
             @NotNull ResourceKey key = home.createResource();            
             ORQResource orqr = (ORQResource) home.find( key );
             orqr.setCachedWid(WidAux.getWid());
             orqr.setOfferRequestArguments( offerRequestArguments, context);
 
 	        StringWriter wr = new StringWriter();
 	        AxisTypeFromToXML.toXML(wr, offerRequestArguments, false, true);
            wr.write( "\nassigned GORFXId: " + orqr.getID() );
 	        logger.info("ORQ is: " + wr.toString());
 
             return home.getResourceReference( key ).getEndpointReference();
         } catch ( Exception e ) {
             e.printStackTrace();
             throw new RemoteException( e.getMessage(), e );
         }
         finally {
             WidAux.removeWid();
         }
     }
 
 
     private void logSecInfo( String s ) throws org.globus.wsrf.security.SecurityException {
 
         logger.debug( "Method " +s + " called by: " + GORFXAuthorization.getCallerIdentity() );
         String[] l = org.globus.wsrf.security.SecurityManager.getManager(  ).getLocalUsernames();
         if( l == null )
             logger.debug( "No mappings found" );
         else
             logger.debug( "Mapped to" + ( l.length > 0 ? l[0] : "none" ) );
     }
 
 
     public org.apache.axis.types.URI[] getSupportedOfferTypes(types.ContextT context) throws RemoteException {
 
         try{
             ContextTAux.initWid(getSystem().getModelUUIDGen(), context);
             return getResourceHome().getAddressedResource().getSupportedOfferTypes( );
         } catch ( Exception e ) {
             throw new RemoteException(e.getMessage(), e);
         }
         finally {
             WidAux.removeWid();
         }
     }
 
 
     public java.lang.Object callMaintenanceAction(java.lang.String action, types.ContextT context) throws RemoteException {
         
         logger.debug( "called with: " + action );
         try {
             ContextTAux.initWid(getSystem().getModelUUIDGen(), context);
 
                 if( maintenance == null  )
                     maintenance = new WSMaintenance( system );
 
                 return maintenance.callMaintenenceAction( action );
         } catch ( Exception e ) {
             logger.warn( e.getMessage(), e );
             throw new RemoteException( e.getMessage(), e );
         }
         finally {
             WidAux.removeWid();
         }
     }
 
 
     @Override
     public ExtGORFXResourceHome getResourceHome() throws Exception {
         return (ExtGORFXResourceHome) super.getResourceHome();    // Overridden method
     }
 
 
     @Override
     public ExtORQResourceHome getORQResourceHome() throws Exception {
         return (ExtORQResourceHome) super.getORQResourceHome();    // Overridden method
     }
 
 
     @Override
     public ExtOfferResourceHome getOfferResourceHome() throws Exception {
         return (ExtOfferResourceHome) super.getOfferResourceHome();    // Overridden method
     }
 
 
     @Override
     public ExtTaskResourceHome getTaskResourceHome() throws Exception {
         return (ExtTaskResourceHome) super.getTaskResourceHome();    // Overridden method
     }
 
 
     public @NotNull GNDMSystem getSystem() {
         return system;
     }
 }
 
