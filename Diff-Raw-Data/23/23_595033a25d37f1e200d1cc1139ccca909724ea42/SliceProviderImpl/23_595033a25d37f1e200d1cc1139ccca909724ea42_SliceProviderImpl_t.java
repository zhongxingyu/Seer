 package de.zib.gndms.logic.model.dspace;
 
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import de.zib.gndms.common.model.gorfx.types.Order;
 import de.zib.gndms.infra.grams.LinuxDirectoryAux;
 import de.zib.gndms.infra.system.GNDMSystem;
 import de.zib.gndms.logic.action.ActionConfigurer;
 import de.zib.gndms.logic.model.ModelIdHoldingOrder;
 import de.zib.gndms.logic.model.ModelUpdateListener;
 import de.zib.gndms.model.common.GridResource;
 import de.zib.gndms.model.common.NoSuchResourceException;
 import de.zib.gndms.model.dspace.Slice;
 import de.zib.gndms.model.dspace.SliceKind;
 import de.zib.gndms.model.dspace.Subspace;
 import de.zib.gndms.model.util.GridResourceCache;
 import de.zib.gndms.neomodel.gorfx.Taskling;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManagerFactory;
 import java.util.Calendar;
 import java.util.List;
 import java.util.UUID;
 
 /**
  * The slice provider which handles the available subspaces providing 
  * a mapping of slice ids and slices.
  * 
  * @author Ulrike Golas
  */
 
 public class SliceProviderImpl implements SliceProvider {
     private final Logger logger = LoggerFactory.getLogger( this.getClass() );
 
     private GNDMSystem system;
 
     private SubspaceProvider subspaceProvider;
     private SliceKindProvider sliceKindProvider;
 
     final private ActionConfigurer actionConfigurer;
 
     final private GridResourceCache< Slice > cache;
 
     public SliceProviderImpl( EntityManagerFactory emf ) {
         this.actionConfigurer = new ActionConfigurer( emf );
         this.actionConfigurer.setEntityUpdateListener( new Invalidator() );
         this.cache = new GridResourceCache<Slice>( Slice.class, emf );
     }
 
     public SliceProviderImpl( ) {
         this.actionConfigurer = new ActionConfigurer( );
         this.actionConfigurer.setEntityUpdateListener( new Invalidator() );
         this.cache = new GridResourceCache<Slice>( Slice.class );
     }
 
     @Inject
     public void setSystem(GNDMSystem system) {
         this.system = system;
     }
 
     @Inject
     public void setEntityManagerFactory( final EntityManagerFactory emf ) {
         actionConfigurer.setEntityManagerFactory( emf );
         cache.setEmf( emf );
     }
 
     @Inject
     public void setSubspaceProvider( SubspaceProvider subspaceProvider ) {
         this.subspaceProvider = subspaceProvider;
     }
 
     @Inject
     public void setSliceKindProvider( SliceKindProvider sliceKindProvider ) {
         this.sliceKindProvider = sliceKindProvider;
     }
 
 	@Override
 	public final boolean exists(final String subspace, final String slice) {
         try{
             cache.get( subspace );
         }
         catch( NoSuchResourceException e )
         {
             return false;
         }
         return true;
 	}
 
     @Override
     public final List<String> listSlices( final String subspace ) throws NoSuchElementException {
         // TODO: query for all slices
         return null;
     }
 
     @Override
     public final Slice getSlice( final String subspace, final String sliceId ) throws NoSuchElementException {
         try {
             return cache.get( sliceId );
         }
         catch( NullPointerException e ) {
             throw new NoSuchElementException( e.getMessage() );
         }
     }
 
     @Override
     public String createSlice(
             final String subspaceId,
             final String sliceKindId,
             final String dn,
             final Calendar ttm,
             final long sliceSize ) throws NoSuchElementException {
 
         if( !sliceKindProvider.exists( subspaceId, sliceKindId ) ) {
             logger.info( "Illegal Access: slicekind " + sliceKindId + " in subspace " + subspaceId + " not available." );
             throw new NoSuchElementException( "SliceKind " + sliceKindId + " does not exist in subspace " + subspaceId + "." );
         }
 
         Subspace subspace = subspaceProvider.get( subspaceId );
         SliceKind sliceKind = sliceKindProvider.get( subspaceId, sliceKindId );
 
         final CreateSliceAction createSliceAction = new CreateSliceAction( dn, ttm, sliceKind, sliceSize );
         actionConfigurer.configureAction( createSliceAction );
         createSliceAction.setModel( subspace );
         createSliceAction.setDirectoryAux( new LinuxDirectoryAux() );
 
         final Slice slice = createSliceAction.call();
 
         // TODO: could cache the slice here
 
         return slice.getId();
     }
 
     public Taskling deleteSlice(
             final String subspaceId,
             final String sliceId ) throws NoSuchElementException {

         if( !cache.exists( sliceId ) ) {
             logger.info( "Illegal Access: slice " + sliceId + " in subspace " + subspaceId + " cannot be deleted because it is not available." );
             throw new NoSuchElementException( "Slice " + sliceId + " does not exist in subspace " + subspaceId + "." );
         }
 
         final Subspace subspace = subspaceProvider.get( subspaceId );
         final Slice slice = cache.get( sliceId );
         
         final DeleteSliceTaskAction deleteAction = new DeleteSliceTaskAction();
         deleteAction.setDirectoryAux(new LinuxDirectoryAux());
         system.getInstanceDir().getSystemAccessInjector().injectMembers( deleteAction );
         //actionConfigurer.configureAction(deleteAction);
 
         final Order order = new ModelIdHoldingOrder( sliceId );
         final String wid = UUID.randomUUID().toString();
         logger.info( "Delete sliceID (" + sliceId + ") with tracking number " + wid );
         final Taskling ling = system.submitTaskAction( deleteAction, order, wid );
 
         cache.invalidate(sliceId);
 
         return ling;
     }
 
     private class Invalidator implements ModelUpdateListener< GridResource > {
         public void onModelChange( GridResource model ) {
             SliceProviderImpl.this.cache.invalidate( model.getId() );
         }
     }
 }
