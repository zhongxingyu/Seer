 package ecologylab.semantics.compiler;
 
 import java.io.File;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import ecologylab.semantics.generated.library.RepositoryMetadataTranslationScope;
 import ecologylab.semantics.generated.test.test_yahoo_geo_code.Item;
 import ecologylab.semantics.generated.test.test_yahoo_geo_code.MyItem;
 import ecologylab.semantics.generated.test.test_yahoo_geo_code.YahooGeoResult;
 import ecologylab.semantics.generated.test.test_yahoo_geo_code.YahooResult;
 import ecologylab.semantics.metadata.MetadataFieldDescriptor;
 import ecologylab.semantics.metametadata.MetaMetadata;
 import ecologylab.semantics.metametadata.MetaMetadataCollectionField;
 import ecologylab.semantics.metametadata.MetaMetadataField;
 import ecologylab.semantics.metametadata.MetaMetadataRepository;
 import ecologylab.serialization.ClassDescriptor;
 
 @SuppressWarnings("rawtypes")
 public class TestBindingProcess
 {
 	
 	@Test
 	public void testBindYahooGeoCode()
 	{
		MetaMetadataRepository repository = MetaMetadataRepository.loadXmlFromFiles(new File("../simplTranslators/data/testRepository/testYahooGeoCode.xml"));
 		repository.bindMetadataClassDescriptorsToMetaMetadata(RepositoryMetadataTranslationScope.get());
 		
 		MetaMetadata yahoo_result_set = repository.getMMByName("yahoo_result_set");
 		MetaMetadataCollectionField yahoo_result_set__results = (MetaMetadataCollectionField) yahoo_result_set.getChildMetaMetadata().get("results"); 
 		MetadataFieldDescriptor resultsFd1 = yahoo_result_set__results.getMetadataFieldDescriptor();
 		Assert.assertEquals("Result", resultsFd1.getCollectionOrMapTagName());
 		Assert.assertFalse(resultsFd1.isWrapped());
 		ClassDescriptor resultType1 = resultsFd1.getElementClassDescriptor();
 		Assert.assertSame(ClassDescriptor.getClassDescriptor(YahooResult.class), resultType1);
 		
 		MetaMetadata yahoo_geo_code = repository.getMMByName("yahoo_geo_code");
 		MetaMetadataCollectionField yahoo_geo_code__results = (MetaMetadataCollectionField) yahoo_geo_code.getChildMetaMetadata().get("results"); 
 		MetadataFieldDescriptor resultsFd2 = yahoo_geo_code__results.getMetadataFieldDescriptor();
 		Assert.assertEquals("Result", resultsFd2.getCollectionOrMapTagName());
 		Assert.assertFalse(resultsFd2.isWrapped());
 		ClassDescriptor resultType2 = resultsFd2.getElementClassDescriptor();
 		Assert.assertSame(ClassDescriptor.getClassDescriptor(YahooGeoResult.class), resultType2);
 		
 		MetaMetadata base_set = repository.getMMByName("base_set");
 		// items:
 		MetaMetadataCollectionField base_set__items = (MetaMetadataCollectionField) base_set.getChildMetaMetadata().get("items"); 
 		MetadataFieldDescriptor itemsFd1 = base_set__items.getMetadataFieldDescriptor();
 		Assert.assertEquals("items", itemsFd1.getTagName());
 		Assert.assertEquals("item", itemsFd1.getCollectionOrMapTagName());
 		Assert.assertTrue(itemsFd1.isWrapped());
 		ClassDescriptor itemType1 = itemsFd1.getElementClassDescriptor();
 		Assert.assertSame(ClassDescriptor.getClassDescriptor(Item.class), itemType1);
 		// the_item:
 		MetaMetadataField base_set__the_item = base_set.getChildMetaMetadata().get("the_item");
 		MetadataFieldDescriptor theItemFd1 = base_set__the_item.getMetadataFieldDescriptor();
 		Assert.assertEquals("Item", theItemFd1.getTagName());
 		ClassDescriptor theItemType1 = theItemFd1.getElementClassDescriptor();
 		Assert.assertSame(ClassDescriptor.getClassDescriptor(Item.class), theItemType1);
 		
 		MetaMetadata derived_set = repository.getMMByName("derived_set");
 		MetaMetadataCollectionField derived_set__items = (MetaMetadataCollectionField) derived_set.getChildMetaMetadata().get("items"); 
 		MetadataFieldDescriptor itemsFd2 = derived_set__items.getMetadataFieldDescriptor();
 		Assert.assertEquals("my_items", itemsFd2.getTagName());
 		Assert.assertEquals("my_item", itemsFd2.getCollectionOrMapTagName());
 		Assert.assertFalse(itemsFd2.isWrapped());
 		ClassDescriptor itemType2 = itemsFd2.getElementClassDescriptor();
 		Assert.assertSame(ClassDescriptor.getClassDescriptor(MyItem.class), itemType2);
 		// the_item:
 		MetaMetadataField derived_set__the_item = derived_set.getChildMetaMetadata().get("the_item");
 		MetadataFieldDescriptor theItemFd2 = derived_set__the_item.getMetadataFieldDescriptor();
 		Assert.assertEquals("MyItem", theItemFd2.getTagName());
 		ClassDescriptor theItemType2 = theItemFd2.getElementClassDescriptor();
 		Assert.assertSame(ClassDescriptor.getClassDescriptor(MyItem.class), theItemType2);
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		TestBindingProcess tbp = new TestBindingProcess();
 		tbp.testBindYahooGeoCode();
 	}
 
 }
