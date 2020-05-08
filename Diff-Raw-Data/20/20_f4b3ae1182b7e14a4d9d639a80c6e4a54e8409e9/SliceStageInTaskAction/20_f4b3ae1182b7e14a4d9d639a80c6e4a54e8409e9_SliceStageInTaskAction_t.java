 package de.zib.gndms.GORFX.action.dms;
 
 import de.zib.gndms.GORFX.context.client.TaskClient;
 import de.zib.gndms.logic.model.gorfx.ORQTaskAction;
 import de.zib.gndms.model.dspace.types.SliceRef;
 import de.zib.gndms.model.gorfx.AbstractTask;
 import de.zib.gndms.model.gorfx.types.SliceStageInORQ;
 import de.zib.gndms.model.gorfx.types.SliceStageInResult;
 import de.zib.gndms.model.gorfx.types.TaskState;
 import de.zib.gndms.typecon.common.type.ContextXSDTypeWriter;
 import de.zib.gndms.typecon.common.type.ContractXSDTypeWriter;
 import de.zib.gndms.typecon.common.type.ProviderStageInORQXSDTypeWriter;
 import de.zib.gndms.typecon.common.type.SliceRefXSDReader;
 import de.zib.gndms.infra.system.SystemHolder;
 import de.zib.gndms.infra.system.GNDMSystem;
 import org.apache.axis.message.addressing.EndpointReferenceType;
 import org.jetbrains.annotations.NotNull;
 import types.*;
 
 /**
  * @author: Maik Jorra <jorra@zib.de>
  * @version: $Id$
  * <p/>
  * User: mjorra, Date: 11.11.2008, Time: 13:49:57
  */
 public class SliceStageInTaskAction extends ORQTaskAction<SliceStageInORQ>
 	implements SystemHolder {
 
 	private GNDMSystem system;
 
 
 	@NotNull
 	public GNDMSystem getSystem() {
 		return system;
 	}
 
 
 	public void setSystem(@NotNull final GNDMSystem systemParam) {
 		system = systemParam;
 	}
 
 
     @Override
     @NotNull
     protected Class<SliceStageInORQ> getOrqClass() {
         return SliceStageInORQ.class;
     }
 
 
     @SuppressWarnings( { "ThrowableInstanceNeverThrown" } )
     @Override
     protected void onInProgress( @NotNull AbstractTask model ) {
 
         try {
             EndpointReferenceType epr;
             if( model.getData( ) == null ) {
                 String uri = ( (SliceStageInORQ) model.getOrq()).getActGridSiteURI();
                 if( uri == null )
                     fail ( new RuntimeException( "GORFX uri is null" ) );
                 else {
                     ProviderStageInORQT p_orq = ProviderStageInORQXSDTypeWriter.write( getOrq() );
                     ContextT ctx = ContextXSDTypeWriter.writeContext( getOrq().getActContext() );
                     OfferExecutionContractT con = ContractXSDTypeWriter.write( model.getContract().toTransientContract() );
                     
                     epr = GORFXClientUtils.commonTaskPreparation( uri, p_orq, ctx, con  );
                     model.setData( epr );
      //               transitToState( TaskState.IN_PROGRESS );
                 }
             }
 
             epr = (EndpointReferenceType) model.getData( );
 
             TaskClient cnt = new TaskClient( epr );
             trace( "Starting remote staging.", null );
             boolean finished = GORFXClientUtils.waitForFinish( cnt, 1000 );
 
             if (finished) {
                 trace( "Remote staging finished.", null );
                 ProviderStageInResultT res =  cnt.getExecutionResult( ProviderStageInResultT.class );
                 SliceReference sk = (SliceReference) res.get_any()[0].getObjectValue( SliceReference.class );
                 SliceRef sr = SliceRefXSDReader.read( sk );
                trace( "Remote staging finished. SliceId: " + sr.getResourceKeyValue() + "@"  + sr.getGridSiteId() , null );
                 finish( new SliceStageInResult( sr ) );
             } else {
                 TaskExecutionFailure f = cnt.getExecutionFailure();
                 trace( "Remote staging failed with: \n"
                     +  GORFXClientUtils.taskExecutionFailureToString( f ), null );
                 fail( new RuntimeException( f.toString() ) );
             }
         } catch( RuntimeException e ) {
             honorOngoingTransit( e );
             boxException( e );
         } catch ( Exception e ) {
             boxException( e );
         }
     }
 
 
     @SuppressWarnings( { "ThrowableInstanceNeverThrown" } )
     private void boxException( Exception e ) {
         fail( new RuntimeException( e.getMessage(), e ) );
     }
 }
