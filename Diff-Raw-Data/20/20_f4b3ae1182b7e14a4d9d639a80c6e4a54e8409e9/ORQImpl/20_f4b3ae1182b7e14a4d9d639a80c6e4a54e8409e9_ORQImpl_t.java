 package de.zib.gndms.GORFX.ORQ.service;
 
 import de.zib.gndms.GORFX.ORQ.service.globus.resource.ExtORQResourceHome;
 import de.zib.gndms.GORFX.ORQ.service.globus.resource.ORQResource;
 import de.zib.gndms.GORFX.ORQ.stubs.types.PermissionDenied;
 import de.zib.gndms.GORFX.ORQ.stubs.types.UnfullfillableRequest;
 import de.zib.gndms.GORFX.offer.service.globus.resource.ExtOfferResourceHome;
 import de.zib.gndms.GORFX.offer.service.globus.resource.OfferResource;
 import de.zib.gndms.kit.util.WidAux;
 import de.zib.gndms.logic.model.gorfx.PermissionDeniedORQException;
 import de.zib.gndms.logic.model.gorfx.UnfulfillableORQException;
 import de.zib.gndms.model.common.types.TransientContract;
 import de.zib.gndms.shared.ContextTAux;
 import de.zib.gndms.typecon.common.type.ContractXSDTypeWriter;
 import de.zib.gndms.typecon.util.AxisTypeFromToXML;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.globus.wsrf.ResourceContext;
 import org.globus.wsrf.ResourceKey;
 import org.globus.wsrf.impl.ResourceContextImpl;
 
 import java.rmi.RemoteException;
 import java.io.StringWriter;
 import java.io.IOException;
 
 import types.OfferExecutionContractT;
 
 
 /**
  * TODO:I am the service side implementation class.  IMPLEMENT AND DOCUMENT ME -- document yourself 
  *
  * @created by Introduce Toolkit version 1.2
  * 
  */
 public class ORQImpl extends ORQImplBase {
     private static final Log logger = LogFactory.getLog(ORQImpl.class);
 
     public ORQImpl() throws RemoteException {
         super();
     }
 
 
     @SuppressWarnings({ "FeatureEnvy" })
     public org.apache.axis.message.addressing.EndpointReferenceType getOfferAndDestroyRequest(types.OfferExecutionContractT offerExecutionContract,types.ContextT context) throws RemoteException, de.zib.gndms.GORFX.ORQ.stubs.types.UnfullfillableRequest, de.zib.gndms.GORFX.ORQ.stubs.types.PermissionDenied {
 
         try {
             logger.warn("ORQImpl");
             ExtORQResourceHome home = (ExtORQResourceHome ) getResourceHome();
             ResourceContextImpl impl = (ResourceContextImpl) ResourceContext.getResourceContext();
             logger.debug(impl.getServiceURL());
             logger.debug(impl.getResourceKeyHeader());
             logger.debug(impl.getResourceHomeLocation());
             logger.debug(impl.getResourceKey());
             logger.debug( "Context: " + loggableXSDT( context ) );
             ORQResource orq = home.getAddressedResource();
             WidAux.initWid(orq.getCachedWid());
             try {
                 ExtOfferResourceHome ohome = ( ExtOfferResourceHome) getOfferResourceHome();
                 ResourceKey key = ohome.createResource();
                 OfferResource ores = ohome.getResource( key );
                 ores.setCachedWid(WidAux.getWid());
                 ores.setOfferRequestArguments( orq.getOfferRequestArguments() );
                 final TransientContract contract = orq.getOfferExecutionContract(offerExecutionContract);
 
                 OfferExecutionContractT oec =
                     ContractXSDTypeWriter.write( contract );
                 ores.setOfferExecutionContract( oec );
                 ores.setOrqCalc(orq.getORQCalculator());
 
                 home.remove( orq.getResourceKey() );
 
                 // log contract
                 logger.debug( "Calculated contract: " + loggableXSDT( oec ) );
 
                 return ohome.getResourceReference( key ).getEndpointReference();
             }
             finally {
                 WidAux.removeWid();
             }
         }
         catch (UnfulfillableORQException e) {
             logger.error( "UnfulfillableORQException: " + e.getMessage() + "\n" + e.getStackTrace().toString() );
             throw new UnfullfillableRequest();
         }
         catch (PermissionDeniedORQException e) {
             logger.error( "PermissionDeniedORQException: " + e.getMessage() + "\n" + e.getStackTrace().toString() );
             throw new PermissionDenied();
         }
         catch ( Exception e ) {
             logger.error( "Exception: " + e.getMessage() + "\n" + e.getStackTrace().toString() );
             throw new RemoteException(e.getMessage(), e);
         }
     }
     
 
     public types.OfferExecutionContractT permitEstimateAndDestroyRequest(types.OfferExecutionContractT offerExecutionContract,types.ContextT context) throws RemoteException, de.zib.gndms.GORFX.ORQ.stubs.types.UnfullfillableRequest, de.zib.gndms.GORFX.ORQ.stubs.types.PermissionDenied {
 
         try {
             ContextTAux.initWid(getResourceHome().getModelUUIDGen(), context);
             ExtORQResourceHome home = (ExtORQResourceHome) getResourceHome();
             ORQResource res = home.getAddressedResource();
 
             OfferExecutionContractT oec =
                 ContractXSDTypeWriter.write( res.estimatedExecutionContract( offerExecutionContract ) );
 
             // log contract
             logger.debug( "Calculated contract: " + loggableXSDT( oec ) );
 
             return oec;
         }
         catch (UnfulfillableORQException e) {
             logger.error( "UnfulfillableORQException: " + e.getMessage() + "\n" + e.getStackTrace().toString() );
             throw new UnfullfillableRequest();
         }
         catch (PermissionDeniedORQException e) {
             logger.error( "PermissionDeniedORQException: " + e.getMessage() + "\n" + e.getStackTrace().toString() );
             throw new PermissionDenied();
         }
         catch ( Exception e ) {
             logger.error( "Exception: " + e.getMessage() + "\n" + e.getStackTrace().toString() );
             throw new RemoteException(e.getMessage(), e);
         }
         finally {
             WidAux.removeWid();
         }
 
     }
 
 
     @Override
     public ExtORQResourceHome getResourceHome() throws Exception {
         return (ExtORQResourceHome) super.getResourceHome();    // Overridden method
     }
 
 
     private static String loggableXSDT( Object o ) {
 
         if( o == null )
             return "NULL";
         
         try {
             StringWriter sw = new StringWriter( );
             AxisTypeFromToXML.toXML( sw, o, false, true );
             return sw.toString();
         } catch ( IOException e ) { // can hardly occure
             return "Object to xml conversion error. " + e.getMessage();
         }
     }
 }
 
