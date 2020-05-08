 package domain.db;
 
 import integration.IntegrationTest;
 import kabbadi.domain.Asset;
 import kabbadi.domain.db.GenericRepository;
 import org.hibernate.SessionFactory;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 public class AssetRepositoryIntegrationTest extends IntegrationTest {
 
     @Autowired
     private SessionFactory sessionFactory;
 
     @Autowired
     private GenericRepository<Asset> assetRepository;
 
     @Test
     public void should_generate_id_for_new_asset_on_save() {
         Asset asset = new Asset();
         assetRepository.saveOrUpdate(asset);
         assertThat(asset.getAsset_id(), notNullValue());
     }
 
     @Test
     public void should_retain_id_for_existing_asset_on_save() {
         Asset asset = new Asset();
         assetRepository.saveOrUpdate(asset);
         Integer asset_id = asset.getAsset_id();
         assetRepository.saveOrUpdate(asset);
         assertThat(asset.getAsset_id(), equalTo(asset_id));
     }
 
 }
