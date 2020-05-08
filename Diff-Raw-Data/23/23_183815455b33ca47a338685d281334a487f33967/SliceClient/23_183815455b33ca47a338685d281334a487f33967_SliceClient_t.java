 package de.zib.gndms.gndmc.dspace;
 
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
 
 import de.zib.gndms.common.dspace.SliceConfiguration;
 import de.zib.gndms.common.dspace.service.SliceInformation;
 import de.zib.gndms.common.dspace.service.SliceServiceClient;
 import de.zib.gndms.common.model.FileStats;
 import de.zib.gndms.common.rest.Facets;
 import de.zib.gndms.common.rest.GNDMSResponseHeader;
 import de.zib.gndms.common.rest.Specifier;
 import de.zib.gndms.common.rest.UriFactory;
 import de.zib.gndms.gndmc.AbstractClient;
 import de.zib.gndms.gndmc.utils.DefaultResponseExtractor;
 import de.zib.gndms.gndmc.utils.EnhancedResponseExtractor;
 import de.zib.gndms.gndmc.utils.HTTPGetter;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.http.client.ClientHttpResponse;
 import org.springframework.util.FileCopyUtils;
 import org.springframework.web.multipart.MultipartFile;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 
 /**
  * The slice client implementing the slice service.
  * 
  * @author Ulrike Golas
  */
 public class SliceClient extends AbstractClient implements SliceServiceClient {
 
 	/**
 	 * The constructor.
 	 */
 	public SliceClient() {
 	}
 
     /**
 	 * The constructor.
 	 * 
 	 * @param serviceURL The base url of the target gndms.
 	 */
 	public SliceClient(final String serviceURL) {
 		this.setServiceURL(serviceURL);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public final ResponseEntity< Facets > listSliceFacets(final String subspace,
 			final String sliceKind, final String slice, final String dn) {
 		return (ResponseEntity< Facets >) (Object) unifiedGet(Facets.class,
                 genSliceUrl( subspace, sliceKind, slice ), dn);
 	}
 
 
     @Override
     public ResponseEntity<Facets> listSliceFacets( final Specifier<Void> slice, final String dn ) {
         return (ResponseEntity< Facets >) (Object) unifiedGet(Facets.class, slice.getUrl(), dn);
     }
 	
 
 	@Override
 	public final ResponseEntity<Void> setSliceConfiguration(final String subspace,
 			final String sliceKind, final String slice, final SliceConfiguration config, final String dn) {
		return unifiedPut( Void.class, config, makeConfigFacet( genSliceUrl( subspace, sliceKind, slice ) ), dn );
 	}
 
 
     @Override
     public ResponseEntity<Void> setSliceConfiguration( final Specifier<Void> slice,
                                                        final SliceConfiguration config,
                                                        final String dn )
     {
 
		return unifiedPut( Void.class, config, makeConfigFacet( slice.getUrl() ), dn );
 	}
 
     @Override
     public ResponseEntity< SliceInformation > getSliceInformation( final String subspace,
                                                                           final String sliceKind,
                                                                           final String slice,
                                                                           final String dn ) {
         return (ResponseEntity< SliceInformation >) (Object)
                unifiedGet( SliceInformation.class, makeConfigFacet( genSliceUrl( subspace, sliceKind, slice ) ), dn );
     }
 
     @Override
     public ResponseEntity< SliceInformation > getSliceInformation( final Specifier< Void > slice,
                                                                           final String dn ) {
         return (ResponseEntity< SliceInformation >) (Object)
                unifiedGet( SliceInformation.class, makeConfigFacet( slice.getUrl() ), dn );
     }
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public final ResponseEntity<Specifier<Void>> transformSlice(final String subspace, final String sliceKind,
 			final String slice, final Specifier<Void> newSliceKind, final String dn) {
 		return (ResponseEntity<Specifier<Void>>) (Object) unifiedPost(Specifier.class, newSliceKind,
                 genSliceUrl( subspace, sliceKind, slice ), dn);
 	}
 
 
     @Override
     public ResponseEntity<Specifier<Void>> transformSlice( final Specifier<Void> slice,
                                                            final Specifier<Void> newSliceKind,
                                                            final String dn )
     {
 
 		return (ResponseEntity<Specifier<Void>>) (Object) unifiedPost(Specifier.class, newSliceKind, slice.getUrl(), dn);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public final ResponseEntity<Specifier<Facets>> deleteSlice(final String subspace, final String sliceKind,
                                                                final String slice, final String dn) {
 		return (ResponseEntity<Specifier<Facets>>) (Object) unifiedDelete(Specifier.class,
                 genSliceUrl( subspace, sliceKind, slice ), dn);
 	}
 
 
     @Override
     public ResponseEntity<Specifier<Facets>> deleteSlice( final Specifier<Void> slice, final String dn ) {
 
 		return (ResponseEntity<Specifier<Facets>>) (Object) unifiedDelete(Specifier.class, slice.getUrl(), dn);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public final ResponseEntity< List< FileStats > > listFiles(
 			final String subspace, final String sliceKind, final String slice, final String dn) {
 
         return (ResponseEntity< List< FileStats > >) (Object) unifiedGet(List.class,
                 makeFilesFacet( genSliceUrl( subspace, sliceKind, slice ) ), dn);
 	}
     
     
     public static Long getSliceDiskUsage( ResponseEntity< List< FileStats > > responseEntity ) {
         if( responseEntity.getHeaders().containsKey( "DiskUsage" ) )
             return Long.parseLong( responseEntity.getHeaders().get( "DiskUsage" ).get( 0 ) );
         else
             throw new IllegalArgumentException( "Response entity has no valid disk usage information. Check response entity first!" );
     }
 
 
     @Override
     public ResponseEntity<List<FileStats>> listFiles( final Specifier<Void> slice,
                                                       final String dn )
     {
 
 		return (ResponseEntity< List< FileStats > >) (Object) unifiedGet(List.class,
                 makeFilesFacet( slice.getUrl() ), dn);
 	}
 
 	@Override
 	public final ResponseEntity<Void> deleteFiles(final String subspace, final String sliceKind,
 			final String slice, final String dn) {
 		return unifiedDelete( makeFilesFacet( genSliceUrl( subspace, sliceKind, slice ) ), dn);
 	}
 
 
     @Override
     public ResponseEntity<Void> deleteFiles( final Specifier<Void> slice,  final String dn )
     {
 		return unifiedDelete( makeFilesFacet( slice.getUrl() ), dn);
 	}
 
     @Override
     public final ResponseEntity<String> getGridFtpUrl(final String subspace,
                                                       final String sliceKind, final String slice, final String dn ) {
 
         return unifiedGet( String.class, makeGsiFtpFacet(
                 genSliceUrl( subspace, sliceKind, slice ) ), dn);
     }
 
 
     @Override
     public final ResponseEntity<String> getRelativeDirectoryPath(final String subspace,
                                                       final String sliceKind, final String slice, final String dn ) {
 
         return unifiedGet( String.class, makeRelativeDirectoryFacet(
                 genSliceUrl( subspace, sliceKind, slice ) ), dn);
     }
 
 
     @Override
     public ResponseEntity<String> getGridFtpUrl( final Specifier<Void> slice, final String dn ) {
 
 		return unifiedGet( String.class, makeGsiFtpFacet( slice.getUrl() ), dn );
 	}
 
 	@Override
 	public final ResponseEntity<Void> listFileContent(final String subspace,
                                                       final String sliceKind, final String slice,
                                                       final String fileName, final List<String> attrs,
                                                       final String dn, final OutputStream out)
             throws NoSuchAlgorithmException, KeyManagementException, IOException
     {
         Specifier< Void > specifier = UriFactory.createSliceSpecifier(
                 getServiceURL(),
                 subspace,
                 sliceKind,
                 slice );
         return listFileContent( specifier, fileName, attrs, dn, out );
 	}
 
 
     @Override
     public ResponseEntity<Void> listFileContent( final Specifier<Void> slice, final String fileName,
                                                  final List<String> attrs, final String dn,
                                                  final OutputStream out )
             throws NoSuchAlgorithmException, KeyManagementException, IOException
     {
 
         HTTPGetter httpGetter = new HTTPGetter( );
         GNDMSResponseHeader headers = new GNDMSResponseHeader();
         
         if( dn != null )
             headers.setDN( dn );
 
         EnhancedResponseExtractor responseExtractor = new DefaultResponseExtractor() {
             @Override
             public void extractData( String url, ClientHttpResponse response ) throws IOException {
                 super.extractData( url, response );
                 FileCopyUtils.copy( getBody(), out );
             }
         };
         httpGetter.setExtractor( 200, responseExtractor );
 
         httpGetter.get( makeFileNameFacet( slice.getUrl(), fileName ), headers );
 
         return new ResponseEntity< Void >(
                 null,
                 responseExtractor.getHeaders(),
                 HttpStatus.valueOf( responseExtractor.getStatusCode() ) );
 	}
 
 	@Override
 	public final ResponseEntity<Void> setFileContent(final String subspace,
 			final String sliceKind, final String slice, final String fileName,
 			final MultipartFile file, final String dn ) {
 
         return postFile(Void.class, file.getName(), file.getOriginalFilename(),
                 makeFileNameFacet(genSliceUrl(subspace, sliceKind, slice), fileName), dn);
 	}
 
 
     @Override
     public ResponseEntity<Void> setFileContent( final Specifier<Void> slice, final String fileName,
                                                 final MultipartFile file, final String dn )
     {
 		return unifiedPut(Void.class, file, makeFileNameFacet( slice.getUrl(), fileName ), dn);
 	}
 
 	@Override
 	public final ResponseEntity<Void> deleteFile(final String subspace,
 			final String sliceKind, final String slice, final String fileName, final String dn ) {
 
         return unifiedDelete(
                 makeFileNameFacet( genSliceUrl( subspace, sliceKind, slice ), fileName ), dn );
 	}
 
 
     @Override
     public ResponseEntity<Void> deleteFile( final Specifier<Void> slice, final String fileName,
                                             final String dn ) {
 
         return unifiedDelete( makeFileNameFacet( slice.getUrl(), fileName ), dn);
 	}
 
 
     protected String genSliceUrl( final String subspace, final String sliceKind, 
                                final String slice ) {
 
         return new StringBuilder().append( getServiceURL() ).append( "/dspace/_" )
                 .append( subspace ).append( "/_" ).append( sliceKind ).append( "/_" )
                 .append( slice ).toString();
     }
 
 
     private String makeFilesFacet( final String uri ) {
 
         return uri + "/files";
     }
    
    
    private String makeConfigFacet( final String uri ) {
        return uri + "/config";
    }
    
    
     private String makeGsiFtpFacet( final String uri ) {
 
         return uri + "/gsiftp";
     }
 
 
     private String makeRelativeDirectoryFacet( final String uri ) {
 
         return uri + "/directory";
     }
 
 
     private String makeFileNameFacet( final String uri, final String fileName ) {
 
         return uri + "/_" + fileName;
     }
 }
