 package burrito.test;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import siena.Model;
 import siena.PersistenceManager;
 import siena.PersistenceManagerFactory;
 import siena.core.PersistenceManagerLifeCycleWrapper;
 import siena.gae.GaePersistenceManager;
 import burrito.Configurator;
 import burrito.EntityValidationException;
 import burrito.client.crud.CrudGenericException;
 import burrito.client.crud.CrudService;
 import burrito.client.crud.FieldValueNotUniqueException;
 import burrito.client.crud.generic.CrudEntityDescription;
 import burrito.client.crud.generic.CrudField;
 import burrito.client.crud.generic.fields.AdminLinkMethodField;
 import burrito.client.crud.generic.fields.DisplayableMethodField;
 import burrito.services.CrudServiceImpl;
 import burrito.services.SearchEntry;
 import burrito.services.SearchManager;
 import burrito.test.crud.ChildEntity;
 import burrito.test.crud.GrandParentEntity;
 import burrito.test.crud.ParentEntity;
 import burrito.util.ValidationUtil;
 
 public class CrudTest extends TestBase {
 
 	@Before
 	public void enableLifeTimeSupport() {
 		PersistenceManager pm = new GaePersistenceManager();
 		pm = new PersistenceManagerLifeCycleWrapper(pm);
 		pm.init(null);
 
 		PersistenceManagerFactory.install(pm, GrandParentEntity.class);
 		PersistenceManagerFactory.install(pm, ParentEntity.class);
 		PersistenceManagerFactory.install(pm, ChildEntity.class);
 
 		Configurator.crudables.clear();
 		Configurator.crudables.add(ChildEntity.class);
 	}
 	
 	@Test
 	public void testInheritance() throws FieldValueNotUniqueException, CrudGenericException {
 		
 		CrudService service = new CrudServiceImpl();
 		CrudEntityDescription desc = service.describe(ChildEntity.class.getName(), -1l, null);
 		CrudEntityDescription headers = service.getEntityHeaders(ChildEntity.class.getName());
 		
 		Assert.assertNotNull(desc.getField("childProperty"));
 		Assert.assertNotNull(headers.getField("childProperty"));
 		Assert.assertNotNull(desc.getField("parentProperty"));
 		Assert.assertNull(headers.getField("parentProperty"));
 		Assert.assertNotNull(desc.getField("grandParentProperty"));
 		Assert.assertNotNull(headers.getField("grandParentProperty"));
 		Assert.assertNull(desc.getField("childMethod"));
 		Assert.assertNull(desc.getField("parentMethod"));
 		Assert.assertNull(desc.getField("grandParentMethod"));
 		Assert.assertNotNull(desc.getField("displayableChildMethod"));
 		Assert.assertNotNull(headers.getField("displayableChildMethod"));
 		Assert.assertNotNull(desc.getField("displayableParentMethod"));
 		Assert.assertNotNull(headers.getField("displayableParentMethod"));
 		Assert.assertNotNull(desc.getField("displayableGrandParentMethod"));
 		Assert.assertNotNull(headers.getField("displayableGrandParentMethod"));
 		
 		Assert.assertNotNull(desc.getField("adminLinkChildMethod"));
 		Assert.assertNotNull(desc.getField("adminLinkParentMethod"));
 		Assert.assertNotNull(desc.getField("adminLinkGrandParentMethod"));
 		Assert.assertNotNull(headers.getField("adminLinkChildMethod"));
 		Assert.assertNotNull(headers.getField("adminLinkParentMethod"));
 		Assert.assertNotNull(headers.getField("adminLinkGrandParentMethod"));
 		
 		cleanDescriptionFromMethods(desc);
 		
 		service.save(desc, null);
 		
 		//Verify pre-inserts
 		ChildEntity entity = Model.all(ChildEntity.class).get();
 		Assert.assertEquals(Integer.valueOf(123), entity.getParentProperty());
 		Assert.assertEquals("automatic", entity.getGrandParentProperty());
 		
 		CrudEntityDescription copy = service.describe(ChildEntity.class.getName(), -1l, entity.getId());
 		cleanDescriptionFromMethods(copy);
 		service.save(copy, entity.getId());
 		
 		Assert.assertEquals(2, Model.all(ChildEntity.class).fetch().size());
 
 		entity.setChildProperty("This is a searchable value.");
 		entity.update();
 
 		SearchManager searchManager = SearchManager.get();
 		searchManager.insertOrUpdateSearchEntry(entity, entity.getId());
 		SearchEntry searchEntry = Model.all(SearchEntry.class).get();
 
 		String[] expectedTokens = {"searchabl", "is", "automatic", "a", "valu", "this"};
 		Assert.assertEquals(Arrays.asList(expectedTokens), searchEntry.tokens);
 
 		try {
 			ValidationUtil.assertNoMissingRequiredFields(entity);
 		}
 		catch (EntityValidationException e) {
 			Assert.fail();
 		}
 
 		entity.setGrandParentProperty(null);
 
 		try {
 			ValidationUtil.assertNoMissingRequiredFields(entity);
 			Assert.fail();
 		}
 		catch (EntityValidationException e) {
 			// all good
 		}
 	}
 
 	@Test
 	public void testCascadeDeleteWithInheritance() {
 		ChildEntity entity1 = new ChildEntity();
 		entity1.insert();
 
 		ChildEntity entity2 = new ChildEntity();
 		entity2.setSomeChildId(entity1.getId());
 		entity2.insert();
 
 		CrudService service = new CrudServiceImpl();
 		CrudEntityDescription desc = service.describe(ChildEntity.class.getName(), entity1.getId(), null);
 		List<CrudEntityDescription> descList = Arrays.asList(new CrudEntityDescription[]{desc});
 		service.deleteEntities(descList);
 
 		entity2 = Model.all(ChildEntity.class).filter("id", entity2.getId()).get();
 
 		Assert.assertNull(entity2.getSomeChildId());
 	}
 
 	@Test
 	public void testEntityUniquenessValidation() throws FieldValueNotUniqueException, CrudGenericException {
 		CrudService service = new CrudServiceImpl();
 
 		ChildEntity entity1 = new ChildEntity();
 		entity1.setUniqueValue(1L);
 		entity1.save();
 
 		ChildEntity entity2 = new ChildEntity();
 		entity2.setUniqueValue(1L);
 		entity2.save();
 
 		try {
 			CrudEntityDescription desc1 = service.describe(ChildEntity.class.getName(), entity1.getId(), null);
 			cleanDescriptionFromMethods(desc1);
 			service.save(desc1, null);
 
 			Assert.fail();
 		}
 		catch (FieldValueNotUniqueException e) {
 			// correct
 		}
 	}
 
 	private void cleanDescriptionFromMethods(CrudEntityDescription desc) {
 		ArrayList<CrudField> cleaned = new ArrayList<CrudField>();
 		for (CrudField field : desc.getFields()) {
 			if (field instanceof DisplayableMethodField || field instanceof AdminLinkMethodField) {
 				//skip methods
 				continue;
 			}
 			cleaned.add(field);
 		}
 		desc.setFields(cleaned);
 	}
 }
