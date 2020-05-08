 package org.ebayopensource.turmeric.repository.wso2;
 
 import static org.junit.Assert.*;
 
 import org.ebayopensource.turmeric.common.v1.types.AckValue;
 import org.ebayopensource.turmeric.repository.v2.services.Artifact;
 import org.ebayopensource.turmeric.repository.v2.services.ArtifactCriteria;
 import org.ebayopensource.turmeric.repository.v2.services.AssetQuery;
 import org.ebayopensource.turmeric.repository.v2.services.SearchAssetsRequest;
 import org.ebayopensource.turmeric.repository.v2.services.SearchAssetsResponse;
 import org.ebayopensource.turmeric.services.common.error.RepositoryServiceErrorDescriptor;
 import org.ebayopensource.turmeric.services.repositoryservice.impl.RepositoryServiceProvider;
 import org.junit.Before;
 import org.junit.Test;
 
 public class SearchAssetTest extends Wso2Base {
 
    private RepositoryServiceProvider provider = null;
 
    @Before
    @Override
    public void setUp() throws Exception {
       super.setUp();
       if (preloaded_asset_id == null) {
          createRequiredAssetsInWso2();
       }
       provider = new RepositoryServiceProviderImpl();
    }
 
    @Test
    public void testResponseNotNull() {
       SearchAssetsRequest request = new SearchAssetsRequest();
       new AssetQuery();
       new ArtifactCriteria();
       new Artifact();
 
       SearchAssetsResponse response = provider.searchAssets(request);
       assertNotNull("Response returned null", response);
    }
 
    @Test
    public void testMissingQuery() {
       SearchAssetsRequest request = new SearchAssetsRequest();
 
       SearchAssetsResponse response = provider.searchAssets(request);
       assertEquals(AckValue.FAILURE, response.getAck());
       assertEquals(RepositoryServiceErrorDescriptor.INVALID_INPUT_EXCEPTION.getErrorId(), response.getErrorMessage()
                .getError().get(0).getErrorId());
    }
 
    @Test
    public void testSearchAssetByID() {
       SearchAssetsRequest request = new SearchAssetsRequest();
       AssetQuery query = new AssetQuery();
       ArtifactCriteria acriteria = new ArtifactCriteria();
       Artifact artifact = new Artifact();
       artifact.setArtifactIdentifier(preloaded_asset_id);
       acriteria.getArtifact().add(artifact);
       query.setArtifactCriteria(acriteria);
       request.setAssetQuery(query);
 
       SearchAssetsResponse response = provider.searchAssets(request);
       assertEquals(AckValue.SUCCESS, response.getAck());
       assertNull(response.getErrorMessage());
    }
 
 }
