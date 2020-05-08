 package com.jtbdevelopment.e_eye_o.hibernate.DAO;
 
 import com.jtbdevelopment.e_eye_o.DAO.helpers.IdObjectUpdateHelper;
 import com.jtbdevelopment.e_eye_o.entities.*;
 import com.jtbdevelopment.e_eye_o.entities.Observable;
 import com.jtbdevelopment.e_eye_o.entities.reflection.IdObjectReflectionHelper;
 import com.jtbdevelopment.e_eye_o.entities.wrapper.DAOIdObjectWrapperFactory;
 import com.jtbdevelopment.e_eye_o.hibernate.entities.impl.*;
 import com.jtbdevelopment.e_eye_o.serialization.IdObjectSerializer;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.metadata.ClassMetadata;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDateTime;
 import org.springframework.context.ApplicationContext;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.*;
 
 import static org.testng.AssertJUnit.*;
 
 /**
  * Date: 1/6/13
  * Time: 3:32 PM
  */
 public class HibernateReadWriteDAOTest {
     public static final String TN = "TN";
     public static final String DN = "DN";
     private Mockery context;
     private SessionFactory sessionFactory;
     private IdObjectReflectionHelper idObjectReflectionHelper;
     private IdObjectUpdateHelper idObjectUpdateHelper;
     private Session session;
     private DAOIdObjectWrapperFactory daoIdObjectWrapperFactory;
     private HibernateReadWriteDAO dao;
     private ClassMetadata metadata, deletedMetaData;
     private ApplicationContext appContext;
     private IdObjectSerializer serializer;
     private Query query1, query2, query3;
 
     private ClassList classListImpl, classListWrapped, classListLoaded;
     private Observation observationImpl, observationWrapped, observationLoaded;
     private ObservationCategory observationCategoryImpl, observationCategoryWrapped, observationCategoryLoaded;
     private Photo photoImpl, photoWrapped, photoLoaded;
     private Student studentImpl, studentWrapped, studentLoaded;
     private AppUser appUserImpl, appUserWrapped, appUserLoaded;
     private DeletedObject deletedImpl, deletedWrapped, deletedLoaded;
     private List<IdObject> wrapped = new ArrayList<>();
     private List<IdObject> impl = new ArrayList<>();
     private List<IdObject> loaded = new ArrayList<>();
 
     private static final LocalDateTime now = new LocalDateTime();
     private static final DateTime dtNow = new DateTime();
     private static final LocalDateTime past = now.minusHours(1);
 
     @BeforeMethod
     public void setUp() throws Exception {
         context = new Mockery();
         serializer = context.mock(IdObjectSerializer.class);
         appContext = context.mock(ApplicationContext.class);
         sessionFactory = context.mock(SessionFactory.class);
         idObjectReflectionHelper = context.mock(IdObjectReflectionHelper.class);
         session = context.mock(Session.class);
         idObjectUpdateHelper = context.mock(IdObjectUpdateHelper.class);
         query1 = context.mock(Query.class, "Q1");
         query2 = context.mock(Query.class, "Q2");
         query3 = context.mock(Query.class, "Q3");
         daoIdObjectWrapperFactory = context.mock(DAOIdObjectWrapperFactory.class);
         classListImpl = context.mock(ClassList.class, "CLI");
         classListLoaded = context.mock(ClassList.class, "CLL");
         classListWrapped = context.mock(ClassList.class, "CLW");
         observationImpl = context.mock(Observation.class, "OI");
         observationLoaded = context.mock(Observation.class, "OL");
         observationWrapped = context.mock(Observation.class, "OW");
         observationCategoryImpl = context.mock(ObservationCategory.class, "OCI");
         observationCategoryLoaded = context.mock(ObservationCategory.class, "OCL");
         observationCategoryWrapped = context.mock(ObservationCategory.class, "OCW");
         photoImpl = context.mock(Photo.class, "PI");
         photoLoaded = context.mock(Photo.class, "PL");
         photoWrapped = context.mock(Photo.class, "PW");
         studentImpl = context.mock(Student.class, "SI");
         studentLoaded = context.mock(Student.class, "SL");
         studentWrapped = context.mock(Student.class, "SW");
         appUserImpl = context.mock(AppUser.class, "AUI");
         appUserWrapped = context.mock(AppUser.class, "AUL");
         appUserLoaded = context.mock(AppUser.class, "AUW");
         deletedImpl = context.mock(DeletedObject.class, "DOI");
         deletedLoaded = context.mock(DeletedObject.class, "DOL");
         deletedWrapped = context.mock(DeletedObject.class, "DOW");
         metadata = context.mock(ClassMetadata.class, "MD");
         deletedMetaData = context.mock(ClassMetadata.class, "DMD");
 
         impl.clear();
         Collections.addAll(impl, classListImpl, appUserImpl, studentImpl, photoImpl, observationImpl, observationCategoryImpl);
         loaded.clear();
         Collections.addAll(loaded, classListLoaded, appUserLoaded, studentLoaded, photoLoaded, observationLoaded, observationCategoryLoaded);
         wrapped.clear();
         Collections.addAll(wrapped, classListWrapped, appUserWrapped, studentWrapped, photoWrapped, observationWrapped, observationCategoryWrapped);
         context.checking(new Expectations() {{
             allowing(appContext).getBean(IdObjectSerializer.class);
             will(returnValue(serializer));
             allowing(idObjectUpdateHelper).validateUpdates(with(any(AppUser.class)), with(any(IdObject.class)), with(any(IdObject.class)));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(deletedImpl.getClass());
             will(returnValue(DeletedObject.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(deletedLoaded.getClass());
             will(returnValue(DeletedObject.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(deletedWrapped.getClass());
             will(returnValue(DeletedObject.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(observationLoaded.getClass());
             will(returnValue(Observation.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(observationWrapped.getClass());
             will(returnValue(Observation.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(classListLoaded.getClass());
             will(returnValue(ClassList.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(classListWrapped.getClass());
             will(returnValue(ClassList.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(studentWrapped.getClass());
             will(returnValue(Student.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(studentLoaded.getClass());
             will(returnValue(Student.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(observationCategoryLoaded.getClass());
             will(returnValue(ObservationCategory.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(observationCategoryWrapped.getClass());
             will(returnValue(ObservationCategory.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(photoWrapped.getClass());
             will(returnValue(Photo.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(photoLoaded.getClass());
             will(returnValue(Photo.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(appUserLoaded.getClass());
             will(returnValue(AppUser.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(appUserWrapped.getClass());
             will(returnValue(AppUser.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(DeletedObject.class);
             will(returnValue(DeletedObject.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(AppUser.class);
             will(returnValue(AppUser.class));
             allowing(idObjectReflectionHelper).getIdObjectInterfaceForClass(AppUserOwnedObject.class);
             will(returnValue(AppUserOwnedObject.class));
             allowing(sessionFactory).getCurrentSession();
             will(returnValue(session));
             allowing(daoIdObjectWrapperFactory).wrap(deletedImpl);
             will(returnValue(deletedWrapped));
             allowing(daoIdObjectWrapperFactory).wrap(deletedWrapped);
             will(returnValue(deletedWrapped));
             allowing(daoIdObjectWrapperFactory).wrap(classListImpl);
             will(returnValue(classListWrapped));
             allowing(daoIdObjectWrapperFactory).wrap(classListWrapped);
             will(returnValue(classListWrapped));
             allowing(daoIdObjectWrapperFactory).wrap(observationImpl);
             will(returnValue(observationWrapped));
             allowing(daoIdObjectWrapperFactory).wrap(observationWrapped);
             will(returnValue(observationWrapped));
             allowing(daoIdObjectWrapperFactory).wrap(observationCategoryImpl);
             will(returnValue(observationCategoryWrapped));
             allowing(daoIdObjectWrapperFactory).wrap(observationCategoryWrapped);
             will(returnValue(observationCategoryWrapped));
             allowing(daoIdObjectWrapperFactory).wrap(photoImpl);
             will(returnValue(photoWrapped));
             allowing(daoIdObjectWrapperFactory).wrap(photoWrapped);
             will(returnValue(photoWrapped));
             allowing(daoIdObjectWrapperFactory).wrap(photoLoaded);
             will(returnValue(photoWrapped));
             allowing(daoIdObjectWrapperFactory).wrap(studentImpl);
             will(returnValue(studentWrapped));
             allowing(daoIdObjectWrapperFactory).wrap(studentWrapped);
             will(returnValue(studentWrapped));
             allowing(daoIdObjectWrapperFactory).wrap(studentLoaded);
             will(returnValue(studentWrapped));
             allowing(daoIdObjectWrapperFactory).wrap(appUserImpl);
             will(returnValue(appUserWrapped));
             allowing(daoIdObjectWrapperFactory).wrap(appUserWrapped);
             will(returnValue(appUserWrapped));
             allowing(daoIdObjectWrapperFactory).wrap(impl);
             will(returnValue(wrapped));
             allowing(daoIdObjectWrapperFactory).wrap(wrapped);
             will(returnValue(wrapped));
             allowing(daoIdObjectWrapperFactory).getWrapperForEntity(AppUser.class);
             will(returnValue(HibernateAppUser.class));
             allowing(daoIdObjectWrapperFactory).getWrapperForEntity(AppUserOwnedObject.class);
             will(returnValue(HibernateAppUserOwnedObject.class));
             allowing(daoIdObjectWrapperFactory).getWrapperForEntity(studentImpl.getClass());
             will(returnValue(HibernateStudent.class));
             allowing(daoIdObjectWrapperFactory).getWrapperForEntity(Student.class);
             will(returnValue(HibernateStudent.class));
             allowing(daoIdObjectWrapperFactory).getWrapperForEntity(Photo.class);
             will(returnValue(HibernatePhoto.class));
             allowing(daoIdObjectWrapperFactory).getWrapperForEntity(photoImpl.getClass());
             will(returnValue(HibernatePhoto.class));
             allowing(daoIdObjectWrapperFactory).getWrapperForEntity(ClassList.class);
             will(returnValue(HibernateClassList.class));
             allowing(daoIdObjectWrapperFactory).getWrapperForEntity(classListImpl.getClass());
             will(returnValue(HibernateClassList.class));
             allowing(daoIdObjectWrapperFactory).getWrapperForEntity(Observation.class);
             will(returnValue(HibernateObservation.class));
             allowing(daoIdObjectWrapperFactory).getWrapperForEntity(observationImpl.getClass());
             will(returnValue(HibernateObservation.class));
             allowing(daoIdObjectWrapperFactory).getWrapperForEntity(ObservationCategory.class);
             will(returnValue(HibernateObservationCategory.class));
             allowing(daoIdObjectWrapperFactory).getWrapperForEntity(observationCategoryImpl.getClass());
             will(returnValue(HibernateObservationCategory.class));
             allowing(daoIdObjectWrapperFactory).getWrapperForEntity(deletedImpl.getClass());
             will(returnValue(HibernateDeletedObject.class));
             allowing(daoIdObjectWrapperFactory).getWrapperForEntity(DeletedObject.class);
             will(returnValue(HibernateDeletedObject.class));
             allowing(sessionFactory).getClassMetadata(HibernateDeletedObject.class);
             will(returnValue(deletedMetaData));
             allowing(sessionFactory).getClassMetadata(HibernateClassList.class);
             will(returnValue(metadata));
             allowing(sessionFactory).getClassMetadata(HibernateAppUserOwnedObject.class);
             will(returnValue(metadata));
             allowing(sessionFactory).getClassMetadata(HibernatePhoto.class);
             will(returnValue(metadata));
             allowing(sessionFactory).getClassMetadata(HibernateObservationCategory.class);
             will(returnValue(metadata));
             allowing(sessionFactory).getClassMetadata(HibernateObservation.class);
             will(returnValue(metadata));
             allowing(sessionFactory).getClassMetadata(HibernateStudent.class);
             will(returnValue(metadata));
             allowing(sessionFactory).getClassMetadata(HibernateAppUser.class);
             will(returnValue(metadata));
             allowing(metadata).getEntityName();
             will(returnValue(TN));
             allowing(deletedMetaData).getEntityName();
             will(returnValue(DN));
             allowing(observationImpl).getObservationSubject();
             will(returnValue(studentImpl));
             allowing(observationWrapped).getObservationSubject();
             will(returnValue(studentWrapped));
             allowing(observationLoaded).getObservationSubject();
             will(returnValue(studentLoaded));
             allowing(observationImpl).getObservationTimestamp();
             will(returnValue(now));
             allowing(observationWrapped).getObservationTimestamp();
             will(returnValue(now));
             allowing(observationLoaded).getObservationTimestamp();
             will(returnValue(now));
             allowing(studentImpl).getLastObservationTimestamp();
             will(returnValue(past));
             allowing(studentWrapped).getLastObservationTimestamp();
             will(returnValue(past));
             allowing(studentLoaded).getLastObservationTimestamp();
             will(returnValue(past));
             allowing(studentLoaded).setLastObservationTimestamp(now);
             allowing(studentImpl).setLastObservationTimestamp(now);
             allowing(studentWrapped).setLastObservationTimestamp(now);
 
             allowing(classListWrapped).getAppUser();
             will(returnValue(appUserWrapped));
             allowing(studentWrapped).getAppUser();
             will(returnValue(appUserWrapped));
             allowing(photoWrapped).getAppUser();
             will(returnValue(appUserWrapped));
             allowing(studentWrapped).getAppUser();
             will(returnValue(appUserWrapped));
             allowing(observationCategoryWrapped).getAppUser();
             will(returnValue(appUserWrapped));
             allowing(observationWrapped).getAppUser();
             will(returnValue(appUserWrapped));
             allowing(classListWrapped).getModificationTimestamp();
             will(returnValue(dtNow));
             allowing(photoWrapped).getModificationTimestamp();
             will(returnValue(dtNow));
             allowing(studentWrapped).getModificationTimestamp();
             will(returnValue(dtNow));
             allowing(studentWrapped).getModificationTimestamp();
             will(returnValue(dtNow));
             allowing(observationCategoryWrapped).getModificationTimestamp();
             will(returnValue(dtNow));
             allowing(observationWrapped).getModificationTimestamp();
             will(returnValue(dtNow));
         }});
 
         dao = new HibernateReadWriteDAO(null, null, sessionFactory, daoIdObjectWrapperFactory, idObjectReflectionHelper, idObjectUpdateHelper);
         dao.setApplicationContext(appContext);
     }
 
     @Test
     public void testCreateSingleWithImpls() {
         setCreateExpections();
 
         for (IdObject i : impl) {
             IdObject r = dao.create(i);
             assertNotSame(i, r);
             assertTrue(wrapped.contains(r));
         }
     }
 
     private void setCreateExpections() {
         final String content = "X";
         context.checking(new Expectations() {{
             one(session).save(classListWrapped);
             one(serializer).write(classListWrapped);
             will(returnValue(content));
             one(session).save(studentWrapped);
             one(serializer).write(studentWrapped);
             will(returnValue(content));
             one(session).save(observationCategoryWrapped);
             one(serializer).write(observationCategoryWrapped);
             will(returnValue(content));
             one(session).save(observationWrapped);
             one(serializer).write(observationWrapped);
             will(returnValue(content));
             one(session).save(photoWrapped);
             one(serializer).write(photoWrapped);
             will(returnValue(content));
             one(session).save(appUserWrapped);
             one(session).save(deletedWrapped);
             one(serializer).write(deletedWrapped);
             will(returnValue(content));
 
             allowing(session).update(studentWrapped);
             allowing(serializer).write(studentWrapped);
             will(returnValue(content));
             one(serializer).write(photoWrapped);
             will(returnValue(content));
             one(serializer).write(observationCategoryWrapped);
             will(returnValue(content));
             allowing(session).save(with(any(HibernateHistory.class)));
             allowing(session).flush();
         }});
     }
 
     @Test
     public void testCreateSingleWithWrapped() {
         setCreateExpections();
 
         for (IdObject i : wrapped) {
             IdObject r = dao.create(i);
             assertSame(i, r);
             assertTrue(wrapped.contains(r));
         }
     }
 
     @Test
     public void testUpdateSingleWithImpls() {
         final String id = "id";
         context.checking(new Expectations() {{
             allowing(session).clear();
             one(classListImpl).getId();
             will(returnValue(id));
             one(studentImpl).getId();
             will(returnValue(id));
             one(observationCategoryImpl).getId();
             will(returnValue(id));
             one(observationImpl).getId();
             will(returnValue(id));
             one(photoImpl).getId();
             will(returnValue(id));
             one(appUserImpl).getId();
             will(returnValue(id));
             one(session).get(TN, id);
             will(returnValue(classListLoaded));
             one(session).get(TN, id);
             will(returnValue(studentLoaded));
             one(session).get(TN, id);
             will(returnValue(observationCategoryLoaded));
             one(session).get(TN, id);
             will(returnValue(observationLoaded));
             one(session).get(TN, id);
             will(returnValue(photoLoaded));
             one(session).get(TN, id);
             will(returnValue(appUserLoaded));
             one(session).update(classListWrapped);
             one(session).update(studentWrapped);
             one(session).update(observationCategoryWrapped);
             one(session).update(observationWrapped);
             one(session).update(photoWrapped);
             one(session).update(appUserWrapped);
             one(session).createQuery("select max(observationTimestamp) from Observation where observationSubject = :observationSubject");
             will(returnValue(query3));
             one(query3).setParameter("observationSubject", studentWrapped);
             will(returnValue(query3));
             one(query3).uniqueResult();
             will(returnValue(now));
             one(session).update(studentWrapped);
             allowing(session).flush();
             String content = "Content";
             one(serializer).write(classListWrapped);
             will(returnValue(content));
             allowing(serializer).write(studentWrapped);
             will(returnValue(content));
             one(serializer).write(photoWrapped);
             will(returnValue(content));
             one(serializer).write(observationWrapped);
             will(returnValue(content));
             one(serializer).write(observationCategoryWrapped);
             will(returnValue(content));
             allowing(session).save(with(any(HibernateHistory.class)));
         }});
 
         for (IdObject i : impl) {
             IdObject r = dao.update(appUserImpl, i);
             assertNotSame(i, r);
             assertTrue(wrapped.contains(r));
         }
     }
 
     @Test
     public void testUpdateSingleWithWrapped() {
         final String id = "id";
         context.checking(new Expectations() {{
             allowing(session).clear();
             one(classListWrapped).getId();
             will(returnValue(id));
             one(studentWrapped).getId();
             will(returnValue(id));
             one(observationCategoryWrapped).getId();
             will(returnValue(id));
             one(observationWrapped).getId();
             will(returnValue(id));
             one(photoWrapped).getId();
             will(returnValue(id));
             one(appUserWrapped).getId();
             will(returnValue(id));
             one(session).get(TN, id);
             will(returnValue(classListLoaded));
             one(session).get(TN, id);
             will(returnValue(studentLoaded));
             one(session).get(TN, id);
             will(returnValue(observationCategoryLoaded));
             one(session).get(TN, id);
             will(returnValue(observationLoaded));
             one(session).get(TN, id);
             will(returnValue(photoLoaded));
             one(session).get(TN, id);
             will(returnValue(appUserLoaded));
             one(session).update(classListWrapped);
             one(session).update(studentWrapped);
             one(session).update(observationCategoryWrapped);
             one(session).update(observationWrapped);
             one(session).update(photoWrapped);
             one(session).update(appUserWrapped);
             one(session).createQuery("select max(observationTimestamp) from Observation where observationSubject = :observationSubject");
             will(returnValue(query3));
             one(query3).setParameter("observationSubject", studentWrapped);
             will(returnValue(query3));
             one(query3).uniqueResult();
             will(returnValue(now));
             one(session).update(studentWrapped);
 
             allowing(session).flush();
             String content = "Content";
             one(serializer).write(classListWrapped);
             will(returnValue(content));
             allowing(serializer).write(studentWrapped);
             will(returnValue(content));
             one(serializer).write(photoWrapped);
             will(returnValue(content));
             one(serializer).write(observationWrapped);
             will(returnValue(content));
             one(serializer).write(observationCategoryWrapped);
             will(returnValue(content));
             allowing(session).save(with(any(HibernateHistory.class)));
         }});
 
         for (IdObject i : wrapped) {
             IdObject r = dao.update(appUserImpl, i);
             assertSame(i, r);
             assertTrue(wrapped.contains(r));
         }
     }
 
     @Test
     public void testDeleteNonLoadableUserReturnsFromImpl() {
         context.checking(new Expectations() {{
             one(appUserWrapped).getId();
             will(returnValue("X"));
             one(session).get(TN, "X");
             will(returnValue(null));
         }});
 
         dao.deleteUser(appUserImpl);
     }
 
     @Test
     public void testDeleteNonLoadableUserReturnsFromWrapped() {
         context.checking(new Expectations() {{
             one(appUserWrapped).getId();
             will(returnValue("X"));
             one(session).get(TN, "X");
             will(returnValue(null));
         }});
 
         dao.deleteUser(appUserWrapped);
     }
 
     @Test
     public void testDeleteNonLoadableObjectReturnsFromImpl() {
         context.checking(new Expectations() {{
             one(observationCategoryWrapped).getId();
             will(returnValue("X"));
             one(session).get(TN, "X");
             will(returnValue(null));
         }});
 
         dao.delete(observationCategoryImpl);
     }
 
     @Test
     public void testDeleteNonLoadableObjectReturnsFromWrapped() {
         context.checking(new Expectations() {{
             one(studentWrapped).getId();
             will(returnValue("X"));
             one(session).get(TN, "X");
             will(returnValue(null));
         }});
 
         dao.delete(studentWrapped);
     }
 
     @Test
     public void testDeleteObsCategoryLooksForObsWithItAndUpdatesThem() {
         final ObservationCategory impl = observationCategoryImpl;
         final ObservationCategory wrapped = observationCategoryWrapped;
         final ObservationCategory loaded = observationCategoryLoaded;
         final List<Observation> relatedOCObservations = Arrays.asList(observationLoaded);
         final List<Photo> relatedPhotos = Collections.emptyList();
         final List<Observation> relatedObservations = Collections.emptyList();
 
         context.checking(new Expectations() {{
             one(session).createQuery("from Observation as O where :category member of O.categories");
             will(returnValue(query1));
             one(query1).setParameter("category", loaded);
             will(returnValue(query1));
             one(query1).list();
             will(returnValue(relatedOCObservations));
             one(observationLoaded).removeCategory(loaded);
             one(session).update(observationLoaded);
         }});
         createStandardDeleteExpectations(wrapped, loaded, relatedPhotos, relatedObservations);
 
         dao.delete(impl);
     }
 
     @Test
     public void testDeleteClassListLooksForRelatedObjectsAndUpdatesOrDeletesThem() {
         final ClassList impl = classListImpl;
         final ClassList wrapped = classListWrapped;
         final ClassList loaded = classListLoaded;
         final List<Student> relatedStudents = Arrays.asList(studentLoaded);
         final List<Photo> relatedPhotos = Arrays.asList(photoLoaded);
         final List<Observation> relatedObservations = Arrays.asList(observationLoaded);
 
         context.checking(new Expectations() {{
             one(session).createQuery("from Student as S where :classList member of S.classLists");
             will(returnValue(query1));
             one(query1).setParameter("classList", loaded);
             will(returnValue(query1));
             one(query1).list();
             will(returnValue(relatedStudents));
             one(studentLoaded).removeClassList(loaded);
             one(session).update(studentLoaded);
         }});
         createStandardDeleteExpectations(wrapped, loaded, relatedPhotos, relatedObservations);
         createPhotoQueryMopUp();
 
         dao.delete(impl);
     }
 
     @Test
     public void testDeleteObservationLooksForRelatedObjectsAndUpdatesOrDeletesThem() {
         final Observation impl = observationImpl;
         final Observation wrapped = observationWrapped;
         final Observation loaded = observationLoaded;
         final List<Photo> relatedPhotos = Arrays.asList(photoLoaded);
         final List<Observation> relatedObservations = Arrays.asList(observationLoaded);
 
         createStandardDeleteExpectations(wrapped, loaded, relatedPhotos, relatedObservations);
         context.checking(new Expectations() {{
             //  Last Observation Time Update
             one(session).update(studentWrapped);
         }});
 
         dao.delete(impl);
     }
 
     @Test
     public void testDeleteStudentLooksForRelatedObjectsAndUpdatesOrDeletesThem() {
         final Student impl = studentImpl;
         final Student wrapped = studentWrapped;
         final Student loaded = studentLoaded;
         final List<Photo> relatedPhotos = Arrays.asList(photoLoaded);
         final List<Observation> relatedObservations = Arrays.asList(observationLoaded);
 
         createStandardDeleteExpectations(wrapped, loaded, relatedPhotos, relatedObservations);
         createPhotoQueryMopUp();
 
         dao.delete(impl);
     }
 
     @Test
     public void testDeletePhotoLooksForRelatedObjectsAndUpdatesOrDeletesThem() {
         final Photo impl = photoImpl;
         final Photo wrapped = photoWrapped;
         final Photo loaded = photoLoaded;
         final List<Photo> relatedPhotos = Collections.emptyList();
         final List<Observation> relatedObservations = Collections.emptyList();
 
         createStandardDeleteExpectations(wrapped, loaded, relatedPhotos, relatedObservations);
 
         dao.delete(impl);
     }
 
     @Test
     public void testDeletingAUser() {
         createDeleteUserExpectations();
 
         dao.deleteUser(appUserImpl);
     }
 
     private void createDeleteUserExpectations() {
         final List<AppUserOwnedObject> ownedObjects = Arrays.asList(photoLoaded, studentLoaded, deletedLoaded);
         final List<DeletedObject> deletedList = Arrays.asList(deletedLoaded);
         context.checking(new Expectations() {{
             one(appUserWrapped).getId();
             will(returnValue("X"));
             one(session).get(TN, "X");
             will(returnValue(appUserLoaded));
             one(session).createQuery("from TN where appUser = :user");
             will(returnValue(query1));
             one(query1).setParameter("user", appUserLoaded);
             will(returnValue(query1));
             one(query1).list();
             will(returnValue(ownedObjects));
             one(session).createQuery("from DN where appUser = :user");
             will(returnValue(query2));
             one(query2).setParameter("user", appUserLoaded);
             will(returnValue(query2));
             one(query2).list();
             will(returnValue(deletedList));
             one(session).delete(appUserLoaded);
             one(session).delete(deletedLoaded);
         }});
 
         final Photo pWrapped = photoWrapped;
         final Photo pLoaded = photoLoaded;
         final List<Photo> pRelatedPhotos = Collections.emptyList();
         final List<Observation> pRelatedObservations = Collections.emptyList();
 
         createStandardDeleteExpectations(pWrapped, pLoaded, pRelatedPhotos, pRelatedObservations);
 
         final Student sWrapped = studentWrapped;
         final Student sLoaded = studentLoaded;
         final List<Photo> sRelatedPhotos = Arrays.asList(photoLoaded);
         final List<Observation> sRelatedObservations = Arrays.asList(observationLoaded);
 
         createStandardDeleteExpectations(sWrapped, sLoaded, sRelatedPhotos, sRelatedObservations);
 
         createPhotoQueryMopUp();
     }
 
     private void createPhotoQueryMopUp() {
         context.checking(new Expectations() {{
             Query query = context.mock(Query.class, "Q" + new Random().nextInt());
             allowing(session).createQuery("from Photo where photoFor = :photoFor");
             will(returnValue(query));
             one(query).setParameter(with(equal("photoFor")), with(any(Object.class)));
             will(returnValue(query));
             one(query).list();
             will(returnValue(Collections.emptyList()));
         }});
     }
 
     private void createStandardDeleteExpectations(final IdObject wrapped, final IdObject loaded, final List<Photo> relatedPhotos, final List<Observation> relatedObservations) {
         context.checking(new Expectations() {{
             Query query = context.mock(Query.class, "Q" + new Random().nextInt());
             one(wrapped).getId();
             will(returnValue("X"));
             one(session).get(TN, "X");
             will(returnValue(loaded));
             one(session).createQuery("from Photo where photoFor = :photoFor");
             will(returnValue(query));
             one(query).setParameter("photoFor", loaded);
             will(returnValue(query));
             one(query).list();
             will(returnValue(relatedPhotos));
             for (Photo p : relatedPhotos) {
                 one(session).delete(p);
             }
             if (loaded instanceof Observable) {
                 one(session).createQuery("from Observation where observationSubject = :observationSubject");
                 will(returnValue(query1));
                 one(query1).setParameter("observationSubject", loaded);
                 will(returnValue(query1));
                 one(query1).list();
                 will(returnValue(relatedObservations));
                 for (Observation o : relatedObservations) {
                     one(session).delete(o);
                 }
             }
             one(session).createQuery("select max(observationTimestamp) from Observation where observationSubject = :observationSubject");
             will(returnValue(query3));
             one(query3).setParameter("observationSubject", studentLoaded);
             will(returnValue(query3));
             one(query3).uniqueResult();
             will(returnValue(now));
             one(session).delete(loaded);
             one(session).flush();
         }});
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void testCreatingDeletedObjectExceptions() {
         dao.create(deletedImpl);
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void testDeletingDeletedObjectExceptions() {
         dao.delete(deletedImpl);
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void testUdpdateingDeletedObjectExceptions() {
         dao.update(appUserImpl, deletedImpl);
     }
 }
