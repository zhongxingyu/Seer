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
 
 package de.zib.gndms.dspace.service;
 
 import de.zib.gndms.common.dspace.SliceConfiguration;
 import de.zib.gndms.common.dspace.SubspaceConfiguration;
 import de.zib.gndms.common.dspace.service.SliceKindService;
 import de.zib.gndms.common.dspace.service.SubspaceInformation;
 import de.zib.gndms.common.dspace.service.SubspaceService;
 import de.zib.gndms.common.logic.config.Configuration;
 import de.zib.gndms.common.logic.config.SetupMode;
 import de.zib.gndms.common.logic.config.WrongConfigurationException;
 import de.zib.gndms.common.rest.*;
 import de.zib.gndms.dspace.service.utils.UnauthorizedException;
 import de.zib.gndms.gndmc.gorfx.TaskClient;
 import de.zib.gndms.kit.config.ParameterTools;
 import de.zib.gndms.logic.model.dspace.NoSuchElementException;
 import de.zib.gndms.logic.model.dspace.*;
 import de.zib.gndms.model.common.NoSuchResourceException;
 import de.zib.gndms.model.dspace.SliceKind;
 import de.zib.gndms.model.dspace.Subspace;
 import de.zib.gndms.model.util.TxFrame;
 import de.zib.gndms.neomodel.gorfx.Taskling;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.client.RestTemplate;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.PersistenceUnit;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.*;
 
 @Controller
 @RequestMapping(value = "/dspace")
 public class SubspaceServiceImpl implements SubspaceService {
     private final Logger logger = LoggerFactory.getLogger( this.getClass() );
 	private EntityManagerFactory emf;
     private String localBaseUrl;
     private String baseUrl;
     private RestTemplate restTemplate;
 	private SubspaceProvider subspaceProvider;
     private SliceKindProvider slicekindProvider;
     private SliceProvider sliceProvider;
 
 	private UriFactory uriFactory;
 	private List< String > subspaceFacetNames;
     private SliceKindService sliceKindService;
 
 
     @PostConstruct
 	public void init() {
         setUriFactory( new UriFactory( baseUrl ) );
 	}
 
 
     public void setUriFactory(UriFactory uriFactory) {
         this.uriFactory = uriFactory;
     }
 
 
 	@Override
 	@RequestMapping( value = "/_{subspace}", method = RequestMethod.GET )
     @Secured( "ROLE_USER" )
 	public ResponseEntity<Facets> listAvailableFacets(
 			@PathVariable final String subspace,
 			@RequestHeader( "DN" ) final String dn ) {
 
         GNDMSResponseHeader headers = getSubspaceHeaders( subspace, dn );
 
         if( !subspaceProvider.exists( subspace ) ) {
             logger.warn( "Subspace " + subspace + " does not exist." );
             return new ResponseEntity< Facets >(null, headers, HttpStatus.NOT_FOUND);
         }
 
         List<Facet> facets = listFacetsOfSubspace(subspace);
         return new ResponseEntity< Facets >( new Facets( facets ), headers, HttpStatus.OK );
 	}
 
 
     @Override
 	@RequestMapping( value = "/_{subspace}", method = RequestMethod.PUT )
     @Secured( "ROLE_ADMIN" )
     public ResponseEntity< Facets > createSubspace(
             @PathVariable final String subspace,
             @RequestBody final String config,
             @RequestHeader( "DN" ) final String dn) {
 
         GNDMSResponseHeader headers = getSubspaceHeaders( subspace, dn );
 
         if( subspaceProvider.exists( subspace ) ) {
             logger.info("Subspace " + subspace + " cannot be created because it already exists.");
             return new ResponseEntity< Facets >( null, headers, HttpStatus.FORBIDDEN );
         }
 
         // TODO: catch creation errors and return appropriate HttpStatus
         logger.info( "Creating supspace " + subspace + "." );
         subspaceProvider.create( "subspace: " + subspace + "; " + config );
 
         List<Facet> facets = listFacetsOfSubspace(subspace);
         return new ResponseEntity< Facets >( new Facets( facets ), headers, HttpStatus.CREATED );
 	}
 
 	@Override
 	@RequestMapping( value = "/_{subspaceId}", method = RequestMethod.DELETE )
     @Secured( "ROLE_ADMIN" )
 	public ResponseEntity< Specifier< Facets > > deleteSubspace(
 			@PathVariable final String subspaceId,
 			@RequestHeader("DN") final String dn) {
 		GNDMSResponseHeader headers = getSubspaceHeaders( subspaceId, dn );
 
 		if ( !subspaceProvider.exists( subspaceId ) ) {
 			logger.warn( "Subspace " + subspaceId + " not found" );
 			return new ResponseEntity< Specifier< Facets > >(
                     null,
                     headers,
 					HttpStatus.NOT_FOUND );
 		}
         
         final EntityManager em = emf.createEntityManager();
         TxFrame tx = new TxFrame( em );
         try {
             final Taskling taskling = subspaceProvider.delete( subspaceId );
 
             final TaskClient client = new TaskClient( localBaseUrl );
             client.setRestTemplate( restTemplate );
             final Specifier< Facets > spec =
                     TaskClient.TaskServiceAux.getTaskSpecifier( client, taskling.getId(), uriFactory, null, dn );
             return new ResponseEntity< Specifier< Facets > >( spec, headers, HttpStatus.OK );
        	}
         catch( NoSuchElementException e ) {
             return new ResponseEntity< Specifier< Facets > >(
                     null,
                     headers,
                     HttpStatus.NOT_FOUND
             );
         }
         finally {
        		tx.finish();
        		if (em != null && em.isOpen()) {
        			em.close();
        		}
        	}
 	}
 
 
 	@Override
 	@RequestMapping( value = "/_{subspace}/slicekinds", method = RequestMethod.GET )
     @Secured( "ROLE_USER" )
 	public ResponseEntity<List<Specifier<Void>>> listSliceKinds(
 			@PathVariable final String subspace,
 			@RequestHeader("DN") final String dn) {
 		GNDMSResponseHeader headers = getSubspaceHeaders( subspace, dn );
 
 		if ( !subspaceProvider.exists( subspace ) ) {
 			logger.info( "Illegal Access: subspace " + subspace + " not found" );
 			return new ResponseEntity<List<Specifier<Void>>>(null, headers,
 					HttpStatus.NOT_FOUND);
 		}
 
 		try {
             List< String > sliceKinds = slicekindProvider.list( subspace );
             List<Specifier<Void>> list = new ArrayList<Specifier<Void>>( sliceKinds.size() );
 
             HashMap<String, String> urimap = new HashMap<String, String>(2);
             urimap.put( UriFactory.SERVICE, "dspace" );
             urimap.put( UriFactory.SUBSPACE, subspace );
 
             for( String sk : sliceKinds ) {
                 Specifier< Void > spec = new Specifier< Void >();
                 spec.setUriMap( new HashMap< String, String >( urimap ) );
                 spec.addMapping( UriFactory.SLICE_KIND, sk );
                 spec.setUrl( uriFactory.sliceKindUri( spec.getUriMap(), null ) );
                 list.add( spec );
             }
 
             return new ResponseEntity<List<Specifier<Void>>>(list, headers,
                     HttpStatus.OK);
         } catch (NoSuchElementException e) {
             logger.warn("Subspace " + subspace + " not found");
             return new ResponseEntity<List<Specifier<Void>>>(null, headers,
                     HttpStatus.NOT_FOUND);
 
         }
 	}
 
 
     @Override
     @RequestMapping( value = "/_{subspaceId}/_{slicekindId}", method = RequestMethod.PUT )
     @Secured( "ROLE_ADMIN" )
     public ResponseEntity< Specifier< Void > > createSliceKind(
             @PathVariable final String subspaceId,
             @PathVariable final String slicekindId,
             @RequestBody final String config,
             @RequestHeader("DN") final String dn) {
         GNDMSResponseHeader headers = getSubspaceHeaders( subspaceId, dn );
 
         if ( !subspaceProvider.exists( subspaceId ) ) {
             logger.info("Illegal Access: subspace " + subspaceId + " not found");
             return new ResponseEntity< Specifier< Void >>( null, headers,
                     HttpStatus.NOT_FOUND );
         }
         if( slicekindProvider.exists( subspaceId, slicekindId ) ) {
             logger.info("Illegal Access: slicekind " + slicekindId + " could not be created because it already exists.");
             return new ResponseEntity< Specifier< Void > >( null, headers,
                     HttpStatus.PRECONDITION_FAILED );
         }
 
         // TODO: catch creation errors and return appropriate HttpStatus
         slicekindProvider.create( slicekindId, "subspace:" + subspaceId + "; " + config );
 
         // generate specifier and return it
         Specifier< Void > spec = new Specifier< Void >();
 
         HashMap< String, String > urimap = new HashMap< String, String >( 2 );
         urimap.put( UriFactory.SERVICE, "dspace" );
         urimap.put( UriFactory.SUBSPACE, subspaceId );
         urimap.put( UriFactory.SLICE_KIND, slicekindId );
         urimap.put( UriFactory.BASE_URL, baseUrl );
         spec.setUriMap( new HashMap< String, String >( urimap ) );
         spec.setUrl( uriFactory.sliceKindUri( urimap, null ) );
 
         return new ResponseEntity< Specifier< Void > >( spec, headers, HttpStatus.CREATED );
     }
 
 
     @Override
 	@RequestMapping(value = "/_{subspaceId}/config", method = RequestMethod.GET)
     @Secured( "ROLE_USER" )
 	public ResponseEntity< SubspaceInformation > getSubspaceInformation(
             @PathVariable final String subspaceId,
             @RequestHeader("DN") final String dn) {
 		GNDMSResponseHeader headers = getSubspaceHeaders( subspaceId, dn );
 		
 		if ( !subspaceProvider.exists( subspaceId ) ) {
 			logger.warn("Subspace " + subspaceId + " not found");
 			return new ResponseEntity< SubspaceInformation >( null, headers, HttpStatus.NOT_FOUND );
 		}
 
 		Subspace subspaceModel = subspaceProvider.get( subspaceId );
         de.zib.gndms.infra.dspace.Subspace subspace = new de.zib.gndms.infra.dspace.Subspace( subspaceModel );
     	SubspaceInformation information = subspace.getInformation();
 
 		return new ResponseEntity< SubspaceInformation >( information, headers, HttpStatus.OK );
 	}
 
 
 	@Override
 	@RequestMapping(value = "/_{subspace}/config", method = RequestMethod.PUT)
     @Secured( "ROLE_ADMIN" )
 	public ResponseEntity<Void> setSubspaceConfiguration(
 			@PathVariable final String subspace,
 			@RequestBody final Configuration config,
 			@RequestHeader("DN") final String dn) {
 
 		GNDMSResponseHeader headers = getSubspaceHeaders( subspace, dn );
 
 		try {
 			SubspaceConfiguration subspaceConfig = SubspaceConfiguration.checkSubspaceConfig(config);
 
 			if( !subspaceProvider.exists(subspace)
 					|| subspaceConfig.getMode() != SetupMode.UPDATE ) {
 				logger.warn("Subspace " + subspace + " cannot be updated");
 				return new ResponseEntity<Void>(null, headers,
 						HttpStatus.FORBIDDEN);
 			}
 		   	final EntityManager em = emf.createEntityManager();
 	       	TxFrame tx = new TxFrame(em);
 
 	       	try {
 
 	       		SetupSubspaceAction action = new SetupSubspaceAction(subspaceConfig);
 	       		action.setOwnEntityManager(em);
 	       		logger.info("Calling action for updating the supspace "
 					+ subspace + ".");
 
 	       		action.call();
 	      	} finally {
 	       		tx.finish();
 	       		if (em != null && em.isOpen()) {
 	       			em.close();
 	       		}
 	       	}
 			return new ResponseEntity< Void >( null, headers, HttpStatus.OK );
 		} catch (WrongConfigurationException e) {
 			logger.warn(e.getMessage());
 			return new ResponseEntity< Void >( null, headers, HttpStatus.BAD_REQUEST );
 		}
 	}
 
 
     @Override
     @RequestMapping( value = "/_{subspaceId}/_{sliceKindId}", method = RequestMethod.POST )
     @Secured( "ROLE_USER" )
     public ResponseEntity< Specifier< Void > > createSlice(
             @PathVariable final String subspaceId,
             @PathVariable final String sliceKindId,
             @RequestBody final String config,
             @RequestHeader( "DN" ) final String dn ) {
         GNDMSResponseHeader headers = getSliceKindHeaders( subspaceId, sliceKindId, dn );
 
         SliceKind sliceKind;
         try {
             sliceKind = slicekindProvider.get( subspaceId, sliceKindId );
         }
         catch( NoSuchElementException e ) {
             logger.warn( "Tried to access non existing SliceKind " + subspaceId + "/" + sliceKindId, e );
             return new ResponseEntity< Specifier< Void > >( null, headers, HttpStatus.NOT_FOUND );
         }
 
         try {
             Map< String, String > parameters = new HashMap< String, String >( );
             ParameterTools.parseParameters( parameters, config, null );
 
             DateTimeFormatter fmt = ISODateTimeFormat.dateTimeParser();
             DateTime terminationTime;
             long sliceSize;
 
             if( parameters.containsKey( SliceConfiguration.TERMINATION_TIME ) )
                 terminationTime = fmt.parseDateTime( parameters.get( SliceConfiguration.TERMINATION_TIME ) );
             else
                 terminationTime = new DateTime().plus( sliceKind.getDefaultTimeToLive() );
             
             if( parameters.containsKey( SliceConfiguration.SLICE_SIZE ) )
                 sliceSize = Long.parseLong( parameters.get( SliceConfiguration.SLICE_SIZE ) );
             else
                 sliceSize = sliceKind.getDefaultTimeToLive();
 
             // use provider to create slice
             String slice = sliceProvider.createSlice( subspaceId, sliceKindId, dn,
                     terminationTime, sliceSize );
            
            subspaceProvider.invalidate( subspaceId );
 
             // generate specifier and return it
             Specifier<Void> spec = new Specifier<Void>();
 
             HashMap<String, String> urimap = new HashMap<String, String>( 2 );
             urimap.put( UriFactory.SERVICE, "dspace" );
             urimap.put( UriFactory.SUBSPACE, subspaceId );
             urimap.put( UriFactory.SLICE_KIND, sliceKindId );
             urimap.put( UriFactory.SLICE, slice );
             urimap.put( UriFactory.BASE_URL, baseUrl );
             spec.setUriMap( new HashMap<String, String>( urimap ) );
             spec.setUrl( uriFactory.sliceUri( urimap, null ) );
 
             return new ResponseEntity< Specifier< Void > >( spec, headers,
                                                         HttpStatus.CREATED );
         }
         catch( WrongConfigurationException e ) {
             logger.warn( e.getMessage() );
             return new ResponseEntity<Specifier<Void>>( null, headers, HttpStatus.BAD_REQUEST );
         }
         catch( NoSuchElementException e ) {
             logger.warn( e.getMessage() );
             return new ResponseEntity<Specifier<Void>>( null, headers, HttpStatus.NOT_FOUND );
         }
         catch( ParameterTools.ParameterParseException e ) {
             logger.info( "Illegal request: Could not parse paramter string \""
                     + ParameterTools.escape( config ) + "\". " + e.getMessage() );
             return new ResponseEntity<Specifier<Void>>( null, headers, HttpStatus.BAD_REQUEST );
         }
     }
 
 
     // delegated to SliceKindServiceImpl, due to mapping conflicts
     @RequestMapping( value = "/_{subspace}/_{sliceKind}", method = RequestMethod.GET )
     @Secured( "ROLE_USER" )
     public ResponseEntity<Configuration> getSliceKindInfo( @PathVariable final String subspace,
                                                            @PathVariable final String sliceKind,
                                                            @RequestHeader( "DN" ) final String dn )
     {
         return sliceKindService.getSliceKindInfo( subspace, sliceKind, dn );
     }
 
 
     // delegated to SliceKindServiceImpl, due to mapping conflicts
     @RequestMapping( value = "/_{subspace}/_{sliceKind}", method = RequestMethod.DELETE )
     @Secured( "ROLE_ADMIN" )
     public ResponseEntity<Specifier<Void>> deleteSliceKind( @PathVariable final String subspace,
                                                             @PathVariable final String sliceKind,
                                                             @RequestHeader( "DN" ) final String dn )
     {
 
         return sliceKindService.deleteSliceKind( subspace, sliceKind, dn );
     }
 
     private List< Facet > listFacetsOfSubspace( String subspace ) {
         Map< String, String > vars = new HashMap< String, String >( );
         vars.put( "service", "dspace" );
         vars.put( "subspace", subspace );
 
         List< Facet > facets = new LinkedList< Facet >( );
 
         for( String facetName: subspaceFacetNames ) {
             Facet facet = new Facet( facetName, uriFactory.subspaceUri( vars, facetName ) );
             facets.add( facet );
         }
         return facets;
     }
 
 
     /**
      * Sets the GNDMS response header for a given subspace, slice kind and dn
      * using the base URL.
      *
      * @param subspace  The subspace id.
      * @param sliceKind The slice kind id.
      * @param dn        The dn.
      * @return The response header for this subspace.
      */
     private GNDMSResponseHeader getSliceKindHeaders( final String subspace,
                                                      final String sliceKind, final String dn ) {
         GNDMSResponseHeader headers = new GNDMSResponseHeader();
         headers.setResourceURL( baseUrl + "/dspace/_" + subspace + "/_"
                                         + sliceKind );
         headers.setParentURL( baseUrl + "/dspace/_" + subspace );
         if( dn != null ) {
             headers.setDN( dn );
         }
         return headers;
     }
 
 	/**
 	 * Sets the GNDMS response header for a given subspace and dn using the base
 	 * URL.
 	 * 
 	 * @param subspace
 	 *            The subspace id.
 	 * @param dn
 	 *            The dn.
 	 * @return The response header for this subspace.
 	 */
 	private GNDMSResponseHeader getSubspaceHeaders( final String subspace,
                                                     final String dn ) {
 		GNDMSResponseHeader headers = new GNDMSResponseHeader();
 		headers.setResourceURL(baseUrl + "/dspace/_" + subspace);
 		headers.setParentURL(baseUrl);
 		if (dn != null) {
 			headers.setDN(dn);
 		}
 		return headers;
 	}
 
 	/**
 	 * Returns the base url of this subspace service.
 	 * @return the baseUrl
 	 */
 	public String getBaseUrl() {
 		return baseUrl;
 	}
 
 
 	/**
 	 * Sets the base url of this subspace service.
 	 * @param baseUrl the baseUrl to set
 	 */
 	public void setBaseUrl(final String baseUrl) {
 		this.baseUrl = baseUrl;
 	}
 
 
     /**
      * Sets the local base url of this subspace service.
      * @param localBaseUrl the localBaseUrl to set
      */
     public void setLocalBaseUrl( String localBaseUrl ) {
         this.localBaseUrl = localBaseUrl;
     }
 
     /**
 	 * Returns the subspace provider of this subspace service.
 	 * @return the subspaceProvider
 	 */
 	public SubspaceProvider getSubspaceProvider() {
 		return subspaceProvider;
 	}
 
 
 	/**
 	 * Sets the subspace provider of this subspace service.
 	 * @param subspaceProvider the subspaceProvider to set
 	 */
     @Inject
 	public void setSubspaceProvider(final SubspaceProvider subspaceProvider) {
 		this.subspaceProvider = subspaceProvider;
 	}
 
 
 	/**
 	 * Returns the facets of this subspace service.
 	 * @return the dspaceFacets
 	 */
 	public List< String > getSubspaceFacetNames() {
 		return subspaceFacetNames;
 	}
 
 
 	/**
 	 * Sets the facets of this subspace service.
 	 * @param subspaceFacetNames the names of the subspaceFacets to set
 	 */
 	public void setSubspaceFacetNames( final List< String > subspaceFacetNames ) {
 		this.subspaceFacetNames = subspaceFacetNames;
 	}
 
 
 	/**
 	 * Returns the entity manager factory.
 	 * @return the factory.
 	 */
 	public EntityManagerFactory getEmf() {
 		return emf;
 	}
 
 
 	/**
 	 * Sets the entity manager factory.
 	 * @param emf the factory to set.
 	 */
 	@PersistenceUnit
 	public void setEmf(final EntityManagerFactory emf) {
 		this.emf = emf;
 	}
 
 
     @Inject
     public void setSliceProvider( SliceProviderImpl sliceProvider ) {
         this.sliceProvider = sliceProvider;
     }
 
 
     public void setSliceKindProvider( SliceKindProviderImpl sliceKindProvider ) {
         this.slicekindProvider = sliceKindProvider;
     }
 
 
     public SliceKindService getSliceKindService() {
 
         return sliceKindService;
     }
 
 
     @Inject
     public void setRestTemplate( RestTemplate restTemplate ) {
         this.restTemplate = restTemplate;
     }
 
 
     @Inject
     public void setSliceKindService( final SliceKindService sliceKindService ) {
 
         this.sliceKindService = sliceKindService;
     }
 
 
     @ExceptionHandler( NoSuchResourceException.class )
     public ResponseEntity<Void> handleNoSuchResourceException(
             NoSuchResourceException ex,
             HttpServletResponse response )
             throws IOException
     {
         logger.debug("handling exception for: " + ex.getMessage());
         response.setStatus( HttpStatus.NOT_FOUND.value() );
         response.sendError( HttpStatus.NOT_FOUND.value() );
         return new ResponseEntity<Void>(
                 null,
                 getSliceKindHeaders(ex.getMessage(), null, null),
                 HttpStatus.NOT_FOUND );
     }
 
 
     @ExceptionHandler( UnauthorizedException.class )
     public ResponseEntity<Void> handleUnAuthorizedException(
             UnauthorizedException ex,
             HttpServletResponse response )
             throws IOException
     {
         logger.debug( "handling exception for: " + ex.getMessage() );
         response.setStatus( HttpStatus.UNAUTHORIZED.value() );
         response.sendError(HttpStatus.UNAUTHORIZED.value());
         return new ResponseEntity<Void>(
                 null,
                 getSliceKindHeaders(ex.getMessage(), null, null),
                 HttpStatus.UNAUTHORIZED );
     }
 }
