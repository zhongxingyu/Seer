 package org.dejava.component.ejb.test.service;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.inject.Inject;
 
 import org.dejava.component.ejb.test.auxiliary.FakeEntity;
 import org.dejava.component.ejb.test.auxiliary.FakeEntityService;
 import org.dejava.component.exception.localized.unchecked.EmptyParameterException;
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
 import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * Tests for the generic EJB service.
  */
 @RunWith(Arquillian.class)
 public class GenericServiceTest {
 
 	/**
 	 * Defines what should be done during deployment (before tests).
 	 * 
 	 * @return The configuration for the archive.
 	 */
 	@Deployment
 	public static Archive<?> createTestArchive() {
 		final String aPom = "pom.xml";
 		final MavenDependencyResolver dependencyResolver = DependencyResolvers
 				.use(MavenDependencyResolver.class).loadMetadataFromPom(aPom);
 		return ShrinkWrap
 				.create(WebArchive.class, "test.war")
 				.addPackages(true, "org.dejava.component.ejb")
 				.addAsLibraries(
						dependencyResolver.artifact("org.dejava.component.exception").resolveAsFiles())
 				.addAsLibraries(
						dependencyResolver.artifact("org.dejava.component.i18n.message").resolveAsFiles())
 				.addAsLibraries(
						dependencyResolver.artifact("org.dejava.component.reflection").resolveAsFiles())
 				.addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
 				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
 				.addAsWebInfResource("test-ds.xml", "test-ds.xml");
 	}
 
 	/**
 	 * Fake entity service.
 	 */
 	@Inject
 	private FakeEntityService fakeEntityService;
 
 	/**
 	 * Gets the fakeEntityService.
 	 * 
 	 * @return The fakeEntityService.
 	 */
 	public FakeEntityService getFakeEntityService() {
 		return fakeEntityService;
 	}
 
 	/**
 	 * Default names for fake entities.
 	 */
 	private static final String[] ENTITIES_NAMES = { "Entity1", "Entity2", "Entity3", "Entity4", "Entity5" };
 
 	/**
 	 * Gets new instances for the test entities.
 	 * 
 	 * @return New instances for the test entities.
 	 */
 	private Collection<FakeEntity> getTestEntities() {
 		// Creates a new list of entities.
 		final Collection<FakeEntity> entities = new ArrayList<>();
 		// For each default entity name.
 		for (final String currentName : ENTITIES_NAMES) {
 			// Creates an entity for the current name.
 			entities.add(new FakeEntity(currentName));
 		}
 		// Returns the list.
 		return entities;
 	}
 
 	/**
 	 * Assert that there are no entities persisted before the test.
 	 */
 	@Before
 	public void assertNoEntitiesPersisted() {
 		// Gets all entities.
 		final Collection<FakeEntity> allEntities = getFakeEntityService().getAllEntities(null, null);
 		// Assert that there are no entities persisted before the test.
 		Assert.assertTrue(allEntities.isEmpty());
 	}
 
 	/**
 	 * Removes all test entities.
 	 */
 	@After
 	public void removeTestEntities() {
 		// Gets all entities.
 		final Collection<FakeEntity> allEntities = getFakeEntityService().getAllEntities(null, null);
 		// Removes all persisted entities.
 		getFakeEntityService().removeAll(allEntities);
 	}
 
 	/**
 	 * Tests a successful approach for the method addOrUpdate.
 	 */
 	@Test
 	public void testAddOrUpdateSuccess() {
 		// Tries to add a fake entity.
 		final FakeEntity fakeEntity = getFakeEntityService().addOrUpdate(new FakeEntity(ENTITIES_NAMES[0]));
 		// Tries to get the same entity by its new id.
 		final FakeEntity sameFakeEntity = getFakeEntityService().getEntityById(fakeEntity.getIdentifier());
 		// Assert that the two entities are the same.
 		Assert.assertEquals(fakeEntity, sameFakeEntity);
 	}
 
 	/**
 	 * Tests the method addOrUpdate with null entity.
 	 */
	@Test(expected = EmptyParameterException.class)
 	public void testAddOrUpdateNullEntity() {
 		// Tries to add a fake entity.
 		getFakeEntityService().addOrUpdate(null);
 	}
 
 	/**
 	 * Tests a successful approach for the method addOrUpdateAll.
 	 */
 	@Test
 	public void testAddOrUpdateAllSuccess() {
 		// Tries to add fake entities (there are no persistent entities yet).
 		final Collection<FakeEntity> entities = getFakeEntityService().addOrUpdateAll(getTestEntities());
 		// Tries to get all persisted entities.
 		final Collection<FakeEntity> sameFakeEntities = getFakeEntityService().getAllEntities(null, null);
 		// Assert that the two collections have the same entities (there were no persistent entities before).
 		Assert.assertEquals(entities, sameFakeEntities);
 	}
 
 	/**
 	 * Tests a successful approach for the method remove.
 	 */
 	@Test
 	public void testRemoveSuccess() {
 		// Tries to add a fake entity.
 		final FakeEntity fakeEntity = getFakeEntityService().addOrUpdate(new FakeEntity(ENTITIES_NAMES[0]));
 		// Tries to remove the entity.
 		getFakeEntityService().remove(fakeEntity);
 		// Gets all entities.
 		final Collection<FakeEntity> allEntities = getFakeEntityService().getAllEntities(null, null);
 		// Assert that there are no entities persisted.
 		Assert.assertTrue(allEntities.isEmpty());
 	}
 
 	/**
 	 * Tests a successful approach for the method removeAll.
 	 */
 	@Test
 	public void testRemoveAllSuccess() {
 		// Tries to add fake entities.
 		final Collection<FakeEntity> fakeEntities = getFakeEntityService().addOrUpdateAll(getTestEntities());
 		// Tries to remove the entities.
 		getFakeEntityService().removeAll(fakeEntities);
 		// Gets all entities.
 		final Collection<FakeEntity> allEntities = getFakeEntityService().getAllEntities(null, null);
 		// Assert that there are no entities persisted.
 		Assert.assertTrue(allEntities.isEmpty());
 	}
 
 	/**
 	 * Tests a successful approach for the method getEntityById.
 	 */
 	@Test
 	public void testGetEntityByIdSuccess() {
 		// Tries to add a fake entity.
 		final FakeEntity fakeEntity = getFakeEntityService().addOrUpdate(new FakeEntity(ENTITIES_NAMES[0]));
 		// Tries to get the same entity by its new id.
 		final FakeEntity sameFakeEntity = getFakeEntityService().getEntityById(fakeEntity.getIdentifier());
 		// Assert that the two entities are the same.
 		Assert.assertEquals(fakeEntity, sameFakeEntity);
 	}
 
 	/**
 	 * Tests a successful approach for the method getEntitiesByAttribute.
 	 */
 	@Test
 	public void testGetEntitiesByAttributeSuccess() {
 		// Tries to add fake entities.
 		final Collection<FakeEntity> fakeEntities = getFakeEntityService().addOrUpdateAll(getTestEntities());
 		// Gets the first entity.
 		final FakeEntity firstEntity = fakeEntities.iterator().next();
 		// Tries to get an entity by the name.
 		final Collection<FakeEntity> sameFirstEntity = getFakeEntityService().getEntitiesByAttribute("name",
 				firstEntity.getName(), null, null);
 		// Assert that only one element is retrieved.
 		Assert.assertEquals(1, sameFirstEntity.size());
 		// Assert that the two entities are the same.
 		Assert.assertEquals(firstEntity, sameFirstEntity.iterator().next());
 	}
 
 	/**
 	 * Tests a successful approach for the method getAllEntities.
 	 */
 	@Test
 	public void testGetAllEntitiesSuccess() {
 		// Tries to add fake entities (there are no persistent entities yet).
 		final Collection<FakeEntity> entities = getFakeEntityService().addOrUpdateAll(getTestEntities());
 		// Tries to get all persisted entities.
 		final Collection<FakeEntity> sameFakeEntities = getFakeEntityService().getAllEntities(null, null);
 		// Assert that the two collections have the same entities (there were no persistent entities before).
 		Assert.assertEquals(entities, sameFakeEntities);
 	}
 
 }
