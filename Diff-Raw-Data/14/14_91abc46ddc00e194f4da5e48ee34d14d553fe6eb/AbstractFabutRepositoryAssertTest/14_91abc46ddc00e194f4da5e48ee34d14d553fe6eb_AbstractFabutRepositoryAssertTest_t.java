 package eu.execom.fabut;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 
 import eu.execom.fabut.model.A;
 import eu.execom.fabut.model.B;
 import eu.execom.fabut.model.C;
 import eu.execom.fabut.model.DoubleLink;
 import eu.execom.fabut.model.EntityTierOneType;
 import eu.execom.fabut.model.EntityTierTwoType;
 import eu.execom.fabut.model.IgnoredMethodsType;
 import eu.execom.fabut.model.IgnoredType;
 import eu.execom.fabut.model.NoDefaultConstructorEntity;
 import eu.execom.fabut.model.NoGetMethodsType;
 import eu.execom.fabut.model.Start;
 import eu.execom.fabut.model.TierFiveType;
 import eu.execom.fabut.model.TierFourType;
 import eu.execom.fabut.model.TierOneType;
 import eu.execom.fabut.model.TierSixType;
 import eu.execom.fabut.model.TierThreeType;
 import eu.execom.fabut.model.TierTwoType;
 import eu.execom.fabut.model.TierTwoTypeWithIgnoreProperty;
 import eu.execom.fabut.model.TierTwoTypeWithListProperty;
 import eu.execom.fabut.model.TierTwoTypeWithPrimitiveProperty;
 
 /**
  * TODO add comments
  * 
  * @author Dusko Vesin
  * @author Nikola Olah
  * @author Bojan Babic
  * @author Nikola Trkulja
  */
public abstract class AbstractFabutRepositoryAssertTest extends Assert implements IFabutRepositoryTest {
     // mock lists
     private List<Object> entityTierOneTypes = new ArrayList<Object>();
     private List<Object> entityTierTwoTypes = new ArrayList<Object>();
     private List<Object> noDefaultConstructorEntities = new ArrayList<Object>();
     private FabutRepositoryAssert fabutRepositoryAssert;
 
     public AbstractFabutRepositoryAssertTest() {
 
     }
 
     @Override
     public List<Class<?>> getEntityTypes() {
         final List<Class<?>> entityTypes = new LinkedList<Class<?>>();
         entityTypes.add(EntityTierOneType.class);
         entityTypes.add(EntityTierTwoType.class);
         entityTypes.add(NoDefaultConstructorEntity.class);
         return entityTypes;
     }
 
     @Override
     public List<Object> findAll(final Class<?> entityClass) {
         if (entityClass == EntityTierOneType.class) {
             return entityTierOneTypes;
         }
         if (entityClass == EntityTierTwoType.class) {
             return entityTierTwoTypes;
         }
         if (entityClass == NoDefaultConstructorEntity.class) {
             return noDefaultConstructorEntities;
         }
         return null;
     }
 
     @Override
     public Object findById(final Class<?> entityClass, final Object id) {
         if (entityClass == EntityTierOneType.class) {
             for (final Object entity : entityTierOneTypes) {
                 if (((EntityTierOneType) entity).getId().equals(id)) {
                     return entity;
                 }
             }
         }
         if (entityClass == NoDefaultConstructorEntity.class) {
             for (final Object entity : noDefaultConstructorEntities) {
                 if (((NoDefaultConstructorEntity) entity).getId().equals(id)) {
                     return entity;
                 }
             }
         }
         return null;
     }
 
     public List<Object> getEntityTierOneTypes() {
         return entityTierOneTypes;
     }
 
     public void setEntityTierOneTypes(final List<Object> list1) {
         entityTierOneTypes = list1;
     }
 
     public List<Object> getEntityTierTwoTypes() {
         return entityTierTwoTypes;
     }
 
     public void setEntityTierTwoTypes(final List<Object> list2) {
         entityTierTwoTypes = list2;
     }
 
     public List<Object> getNoDefaultConstructorEntities() {
         return noDefaultConstructorEntities;
     }
 
     public void setNoDefaultConstructorEntities(final List<Object> noDefaultConstructorEntities) {
         this.noDefaultConstructorEntities = noDefaultConstructorEntities;
     }
 
     @Override
     @Before
     public void fabutBeforeTest() {
         fabutRepositoryAssert = new FabutRepositoryAssert(this);
     }
 
     @Override
     @After
     public void fabutAfterTest() {
     }
 
     @Override
     public List<Class<?>> getComplexTypes() {
         final List<Class<?>> complexTypes = new LinkedList<Class<?>>();
         complexTypes.add(A.class);
         complexTypes.add(B.class);
         complexTypes.add(C.class);
         complexTypes.add(TierOneType.class);
         complexTypes.add(TierTwoType.class);
         complexTypes.add(TierThreeType.class);
         complexTypes.add(TierFourType.class);
         complexTypes.add(TierFiveType.class);
         complexTypes.add(TierSixType.class);
         complexTypes.add(NoGetMethodsType.class);
         complexTypes.add(IgnoredMethodsType.class);
         complexTypes.add(TierTwoTypeWithIgnoreProperty.class);
         complexTypes.add(TierTwoTypeWithListProperty.class);
         complexTypes.add(TierTwoTypeWithPrimitiveProperty.class);
         complexTypes.add(DoubleLink.class);
         complexTypes.add(Start.class);
         return complexTypes;
     }
 
     @Override
     public List<Class<?>> getIgnoredTypes() {
         final List<Class<?>> ignoredTypes = new LinkedList<Class<?>>();
         ignoredTypes.add(IgnoredType.class);
         return ignoredTypes;
     }
 
     @Override
     public void customAssertEquals(final Object expected, final Object actual) {
         Assert.assertEquals(expected, actual);
 
     }
 
     public FabutRepositoryAssert getFabutRepositoryAssert() {
         return fabutRepositoryAssert;
     }
 
 }
