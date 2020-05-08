 package de.zib.gndms.GORFX.ORQ.service;
 
 import de.zib.gndms.GORFX.ORQ.service.globus.resource.ExtORQResourceHome;
 import de.zib.gndms.GORFX.ORQ.service.globus.resource.ORQResource;
 import de.zib.gndms.GORFX.common.type.io.ContractToXSDTypeWriter;
 import de.zib.gndms.GORFX.offer.service.globus.resource.ExtOfferResourceHome;
 import de.zib.gndms.GORFX.offer.service.globus.resource.OfferResource;
 import org.globus.wsrf.ResourceKey;
 
 import java.rmi.RemoteException;
 
 /** 
  * TODO:I am the service side implementation class.  IMPLEMENT AND DOCUMENT ME
  * 
  * @created by Introduce Toolkit version 1.2
  * 
  */
 public class ORQImpl extends ORQImplBase {
 
 
     public ORQImpl() throws RemoteException {
         super();
     }
 
 
     public org.apache.axis.message.addressing.EndpointReferenceType getOfferAndDestroyRequest(types.OfferExecutionContractT offerExecutionContract,types.ContextT context) throws RemoteException, de.zib.gndms.GORFX.ORQ.stubs.types.UnfullfillableRequest, de.zib.gndms.GORFX.ORQ.stubs.types.PermissionDenied {
 
         try {
             ExtORQResourceHome home = (ExtORQResourceHome ) getResourceHome();
             ORQResource orq = home.getAddressedResource();
             ExtOfferResourceHome ohome = ( ExtOfferResourceHome) getOfferResourceHome();
             ResourceKey key = ohome.createResource();
             OfferResource ores = ohome.getResource( key );
             ores.setOfferRequestArguments( orq.getOfferRequestArguments() );
             ores.setOfferExecutionContract(
             ContractToXSDTypeWriter.fromContract(
            orq.getOfferExecutionContract( offerExecutionContract ) ) );
             ores.setOrqCalc(orq.getORQCalculator());
             home.remove( orq.getResourceKey() );
             
             return ohome.getResourceReference( key ).getEndpointReference();
         } catch ( Exception e ) {
             throw new RemoteException( e.getMessage( ) );
         }
     }
     
 
     public types.OfferExecutionContractT permitEstimateAndDestroyRequest(types.OfferExecutionContractT offerExecutionContract,types.ContextT context) throws RemoteException, de.zib.gndms.GORFX.ORQ.stubs.types.UnfullfillableRequest, de.zib.gndms.GORFX.ORQ.stubs.types.PermissionDenied {
 
         try {
             ExtORQResourceHome home = (ExtORQResourceHome) getResourceHome();
             ORQResource res = home.getAddressedResource();
             return ContractToXSDTypeWriter.fromContract(
                 res.estimatedExecutionContract( offerExecutionContract ) );
         } catch ( Exception e ) {
             throw new RemoteException( e.getMessage( ) );
         }
 
     }
 
 }
 
