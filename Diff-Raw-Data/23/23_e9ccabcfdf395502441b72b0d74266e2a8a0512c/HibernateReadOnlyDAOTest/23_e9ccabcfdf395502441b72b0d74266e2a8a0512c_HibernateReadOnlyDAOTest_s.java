 package com.jtbdevelopment.e_eye_o.hibernate.DAO;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.jtbdevelopment.e_eye_o.entities.*;
 import com.jtbdevelopment.e_eye_o.entities.Observable;
 import com.jtbdevelopment.e_eye_o.entities.impl.AppUserOwnedObjectImpl;
 import com.jtbdevelopment.e_eye_o.entities.reflection.IdObjectReflectionHelper;
 import com.jtbdevelopment.e_eye_o.entities.wrapper.DAOIdObjectWrapperFactory;
 import com.jtbdevelopment.e_eye_o.hibernate.entities.impl.HibernateAppUserOwnedObject;
 import com.jtbdevelopment.e_eye_o.hibernate.entities.impl.HibernateDeletedObject;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.metadata.ClassMetadata;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.joda.time.DateTime;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import javax.annotation.Nullable;
 import java.util.*;
 
 import static org.testng.AssertJUnit.*;
 
 /**
  * Date: 1/5/13
  * Time: 9:39 PM
  */
 public class HibernateReadOnlyDAOTest {
     private Mockery context;
     private DAOIdObjectWrapperFactory wrapperFactory;
     private IdObjectReflectionHelper idObjectReflectionHelper;
     private SessionFactory sessionFactory;
     private ClassMetadata hibernateData;
     private AppUser appUser;
     private Session session;
     private Query query;
     private HibernateReadOnlyDAO dao;
 
     private static interface LocalInterface extends AppUserOwnedObject {
     }
 
     private static class LocalImpl extends AppUserOwnedObjectImpl implements LocalInterface {
         public LocalImpl() {
             super(null);
         }
     }
 
     private static class HibernateWrapper extends HibernateAppUserOwnedObject<LocalInterface> implements LocalInterface, AppUserOwnedObject {
         public HibernateWrapper(final String id, final boolean archived) {
             super(new LocalImpl());
             setId(id);
             setArchived(archived);
         }
     }
 
     private static final String HQLNAME = "HQLNAME";
     private static final String SOMEID = "AnId";
     private static final HibernateWrapper ACTIVE_WRAPPER = new HibernateWrapper("1", false);
     private static final HibernateWrapper ARCHIVED_WRAPPER = new HibernateWrapper("2", true);
     private DeletedObject DELETED_WRAPPER;
 
     @BeforeMethod
     public void initializeMockery() {
         context = new Mockery();
         wrapperFactory = context.mock(DAOIdObjectWrapperFactory.class);
         sessionFactory = context.mock(SessionFactory.class);
         idObjectReflectionHelper = context.mock(IdObjectReflectionHelper.class);
         hibernateData = context.mock(ClassMetadata.class);
         session = context.mock(Session.class);
         appUser = context.mock(AppUser.class);
         query = context.mock(Query.class);
         DELETED_WRAPPER = context.mock(DeletedObject.class);
         dao = new HibernateReadOnlyDAO(sessionFactory, wrapperFactory, idObjectReflectionHelper);
         context.checking(new Expectations() {{
             allowing(DELETED_WRAPPER).isArchived();
             will(returnValue(false));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(LocalInterface.class);
             will(returnValue(LocalInterface.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(DeletedObject.class);
             will(returnValue(DeletedObject.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(AppUser.class);
             will(returnValue(AppUser.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(AppUserOwnedObject.class);
             will(returnValue(AppUserOwnedObject.class));
             allowing(wrapperFactory).getWrapperForEntity(LocalInterface.class);
             will(returnValue(HibernateWrapper.class));
             allowing(wrapperFactory).getWrapperForEntity(DeletedObject.class);
             will(returnValue(HibernateDeletedObject.class));
             allowing(wrapperFactory).getWrapperForEntity(HibernateWrapper.class);
             will(returnValue(null));
             allowing(wrapperFactory).getWrapperForEntity(AppUserOwnedObject.class);
             will(returnValue(HibernateAppUserOwnedObject.class));
             allowing(sessionFactory).getClassMetadata(HibernateWrapper.class);
             will(returnValue(hibernateData));
             allowing(sessionFactory).getClassMetadata(HibernateDeletedObject.class);
             will(returnValue(hibernateData));
             allowing(sessionFactory).getClassMetadata(HibernateAppUserOwnedObject.class);
             will(returnValue(hibernateData));
             allowing(hibernateData).getEntityName();
             will(returnValue(HQLNAME));
             allowing(sessionFactory).getCurrentSession();
             will(returnValue(session));
         }});
     }
 
     @Test
     public void testGetUserIds() {
         final List A_LIST = Arrays.asList(ACTIVE_WRAPPER);
         final Set A_SET = new HashSet(A_LIST);
         context.checking(new Expectations() {{
             allowing(session).createQuery("from AppUser");
             will(returnValue(query));
             one(query).list();
             will(returnValue(A_LIST));
         }});
 
         assertEquals(A_SET, dao.getUsers());
     }
 
     @Test
     public void testGetUserIdByEmail() {
         final String emailAddress = "x@y";
         final AppUser appUser = context.mock(AppUser.class, "Email");
        final List A_LIST = Arrays.asList(appUser);
         context.checking(new Expectations() {{
             allowing(session).createQuery("from AppUser where emailAddress = :emailAddress");
             will(returnValue(query));
             one(query).setParameter("emailAddress", emailAddress);
             will(returnValue(query));
            one(query).list();
            will(returnValue(A_LIST));
         }});
 
         assertSame(appUser, dao.getUser(emailAddress));
     }
 
    @Test(expectedExceptions = IllegalStateException.class)
     public void testGetUserIdByEmailExceptionsIfDupes() {
         final String emailAddress = "x@y";
         final AppUser appUser = context.mock(AppUser.class, "Email");
         final List A_LIST = Arrays.asList(appUser, context.mock(AppUser.class, "Email2"));
         context.checking(new Expectations() {{
             allowing(session).createQuery("from AppUser where emailAddress = :emailAddress");
             will(returnValue(query));
             one(query).setParameter("emailAddress", emailAddress);
             will(returnValue(query));
            one(query).list();
            will(returnValue(A_LIST));
         }});
         dao.getUser(emailAddress);
     }
 
     @Test
     public void testGetUserReturnsNullIffEmptyList() {
         final String emailAddress = "x@y";
        final List A_LIST = new LinkedList<>();
         context.checking(new Expectations() {{
             allowing(session).createQuery("from AppUser where emailAddress = :emailAddress");
             will(returnValue(query));
             one(query).setParameter("emailAddress", emailAddress);
             will(returnValue(query));
            one(query).list();
            will(returnValue(A_LIST));
         }});
 
         assertSame(null, dao.getUser(emailAddress));
     }
 
     @Test
     public void testGet() throws Exception {
         context.checking(new Expectations() {{
             allowing(session).get(HQLNAME, SOMEID);
             will(returnValue(ACTIVE_WRAPPER));
         }});
         assertSame(ACTIVE_WRAPPER, dao.get(LocalInterface.class, SOMEID));
         assertSame(ACTIVE_WRAPPER, dao.get(HibernateWrapper.class, SOMEID));
     }
 
     @Test
     public void testGetStudentsForClass() throws Exception {
         final List<Student> result = Collections.unmodifiableList(Arrays.asList(context.mock(Student.class)));
         final ClassList cl = context.mock(ClassList.class);
         context.checking(new Expectations() {{
             allowing(session).createQuery("from Student as S where :classList member of S.classLists");
             will(returnValue(query));
             allowing(query).setParameter("classList", cl);
             allowing(query).list();
             will(returnValue(result));
         }});
         assertSame(result, dao.getAllStudentsForClassList(cl));
     }
 
     @Test
     public void testGetObservationsForCategory() throws Exception {
         final List<Observation> result = Collections.unmodifiableList(Arrays.asList(context.mock(Observation.class)));
         final ObservationCategory oc = context.mock(ObservationCategory.class);
         context.checking(new Expectations() {{
             allowing(session).createQuery("from Observation as O where :category member of O.categories");
             will(returnValue(query));
             allowing(query).setParameter("category", oc);
             allowing(query).list();
             will(returnValue(result));
         }});
         assertSame(result, dao.getAllObservationsForObservationCategory(oc));
     }
 
     @Test
     public void testGetObservationsForEntity() throws Exception {
         final List<Observation> result = Collections.unmodifiableList(Arrays.asList(context.mock(Observation.class)));
         final Observable oc = context.mock(Observable.class);
         context.checking(new Expectations() {{
             allowing(session).createQuery("from Observation where observationSubject = :observationSubject");
             will(returnValue(query));
             allowing(query).setParameter("observationSubject", oc);
             allowing(query).list();
             will(returnValue(result));
         }});
         assertSame(result, dao.getAllObservationsForEntity(oc));
     }
 
     @Test
     public void testGetPhotosForEntity() throws Exception {
         final List<Photo> result = Collections.unmodifiableList(Arrays.asList(context.mock(Photo.class)));
         final AppUserOwnedObject o = context.mock(AppUserOwnedObject.class);
         context.checking(new Expectations() {{
             allowing(session).createQuery("from Photo where photoFor = :photoFor");
             will(returnValue(query));
             allowing(query).setParameter("photoFor", o);
             allowing(query).list();
             will(returnValue(result));
         }});
         assertSame(result, dao.getAllPhotosForEntity(o));
     }
 
     @Test
     public void testGetEntitiesForUser() throws Exception {
         final List<AppUserOwnedObject> fromDB = Arrays.asList(ACTIVE_WRAPPER, ARCHIVED_WRAPPER, DELETED_WRAPPER);
         context.checking(new Expectations() {{
             allowing(session).createQuery("from " + HQLNAME + " where appUser = :user");
             will(returnValue(query));
             allowing(query).setParameter("user", appUser);
             allowing(query).list();
             will(returnValue(fromDB));
         }});
         for (Class<? extends LocalInterface> c : Arrays.asList(LocalInterface.class, HibernateWrapper.class)) {
             Set entitiesForUser = dao.getEntitiesForUser(c, appUser);
             assertTrue(entitiesForUser.containsAll(fromDB));
             assertTrue(fromDB.containsAll(entitiesForUser));
         }
     }
 
     @Test
     public void testGetEntitiesForUserForDeletedObjects() throws Exception {
         final List<DeletedObject> fromDB = Arrays.asList(DELETED_WRAPPER);
         final List<DeletedObject> expected = Arrays.asList(DELETED_WRAPPER);
         context.checking(new Expectations() {{
             allowing(session).createQuery("from " + HQLNAME + " where appUser = :user");
             will(returnValue(query));
             allowing(query).setParameter("user", appUser);
             allowing(query).list();
             will(returnValue(fromDB));
         }});
         Set entitiesForUser = dao.getEntitiesForUser(DeletedObject.class, appUser);
         assertTrue(entitiesForUser.containsAll(expected));
         assertTrue(expected.containsAll(entitiesForUser));
     }
 
     @Test
     public void testGetActiveEntitiesForUser() throws Exception {
         testGetArchivableEntitiesForUser(false);
     }
 
     @Test
     public void testGetArchivedEntitiesForUser() throws Exception {
         testGetArchivableEntitiesForUser(true);
     }
 
     private void testGetArchivableEntitiesForUser(final boolean archived) {
         final List<AppUserOwnedObject> fromDB = Arrays.asList(ACTIVE_WRAPPER, ARCHIVED_WRAPPER, DELETED_WRAPPER);
         final Collection<AppUserOwnedObject> expected = Collections2.filter(fromDB, new Predicate<AppUserOwnedObject>() {
             @Override
             public boolean apply(@Nullable final AppUserOwnedObject input) {
                 return input.isArchived() == archived;
             }
         });
         context.checking(new Expectations() {{
             allowing(session).createQuery("from " + HQLNAME + " where appUser = :user");
             will(returnValue(query));
             allowing(query).setParameter("user", appUser);
             allowing(query).list();
             will(returnValue(fromDB));
         }});
         for (Class<? extends LocalInterface> c : Arrays.asList(LocalInterface.class, HibernateWrapper.class)) {
             Set entitiesForUser = archived ? dao.getArchivedEntitiesForUser(c, appUser) : dao.getActiveEntitiesForUser(c, appUser);
             assertTrue(entitiesForUser.containsAll(expected));
             assertTrue(expected.containsAll(entitiesForUser));
 
         }
     }
 
     @Test
     public void testGetHibernateEntityNameWhereFactoryProvidesMapping() throws Exception {
         assertEquals(HQLNAME, dao.getHibernateEntityName(LocalInterface.class));
     }
 
     @Test
     public void testGetHibernateEntityNameWhereFactoryDoesNotProvidesMapping() throws Exception {
         assertEquals(HQLNAME, dao.getHibernateEntityName(HibernateWrapper.class));
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void testGetHibernateEntityNameWhereFactoryDoesNotProvidesMappingAndNeitherDoesHibernate() throws Exception {
         context.checking(new Expectations() {{
             //  Intentional return of impl not interface to generate failed mapping
             one(idObjectReflectionHelper).getIdObjectInterfaceForClass(LocalImpl.class);
             will(returnValue(LocalImpl.class));
             one(wrapperFactory).getWrapperForEntity(LocalImpl.class);
             will(returnValue(null));
             one(sessionFactory).getClassMetadata(LocalImpl.class);
             will(returnValue(null));
         }});
         dao.getHibernateEntityName(LocalImpl.class);
     }
 
     @Test
     public void testGetEntitiesModifiedSinceReturnsSortedOrderList() {
         final DateTime baseTS = new DateTime();
         final DateTime ts1 = baseTS.minusMillis(1);
         final DateTime ts2 = baseTS;
         final DateTime ts3 = baseTS.plusMillis(1);
         final DateTime since = baseTS.minusMillis(2);
         final HibernateHistory h1 = new HibernateHistory();
         final HibernateHistory h2 = new HibernateHistory();
         final HibernateHistory h3 = new HibernateHistory();
         String serializedVersion1 = "1";
         h1.setSerializedVersion(serializedVersion1);
         String serializedVersion2 = "2";
         h2.setSerializedVersion(serializedVersion2);
         String serializedVersion3 = "3";
         h3.setSerializedVersion(serializedVersion3);
         h1.setModificationTimestamp(ts1);
         h2.setModificationTimestamp(ts2);
         h3.setModificationTimestamp(ts3);
 
         context.checking(new Expectations() {{
 
             allowing(DELETED_WRAPPER).getModificationTimestamp();
             will(returnValue(ts1));
 
             one(sessionFactory).getCurrentSession();
             will(returnValue(session));
             one(session).createQuery("from HistoricalFeed where appUser = :user and modificationTimestamp > :since");
             will(returnValue(query));
             one(query).setParameter("user", appUser);
             one(query).setParameter("since", since.getMillis());
             one(query).list();
             will(returnValue(Arrays.asList(h3, h1, h2)));
         }});
 
         List<String> set = dao.getModificationsSince(appUser, since);
         Iterator<String> iter = set.iterator();
         assertSame(serializedVersion1, iter.next());
         assertSame(serializedVersion2, iter.next());
         assertSame(serializedVersion3, iter.next());
         assertFalse(iter.hasNext());
     }
 }
 
 
