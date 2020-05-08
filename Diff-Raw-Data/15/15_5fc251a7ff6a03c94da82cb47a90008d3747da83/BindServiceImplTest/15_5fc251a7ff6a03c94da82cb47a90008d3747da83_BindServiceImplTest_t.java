 package org.otherobjects.cms.binding;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.otherobjects.cms.SingletonBeanLocator;
 import org.otherobjects.cms.config.OtherObjectsConfigurator;
 import org.otherobjects.cms.dao.MockDaoService;
 import org.otherobjects.cms.dao.MockGenericDao;
 import org.otherobjects.cms.dao.UniversalJcrDaoJackrabbit;
 import org.otherobjects.cms.datastore.JackrabbitDataStore;
 import org.otherobjects.cms.model.Role;
 import org.otherobjects.cms.model.Template;
 import org.otherobjects.cms.model.TemplateBlock;
 import org.otherobjects.cms.model.TemplateBlockReference;
 import org.otherobjects.cms.model.TemplateLayout;
 import org.otherobjects.cms.model.TemplateRegion;
 import org.otherobjects.cms.model.User;
 import org.otherobjects.cms.types.AnnotationBasedTypeDefBuilder;
 import org.otherobjects.cms.types.PropertyDefImpl;
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.types.TypeDefImpl;
 import org.otherobjects.cms.types.TypeService;
 import org.otherobjects.cms.types.TypeServiceImpl;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.mock.web.MockMultipartFile;
 import org.springframework.mock.web.MockMultipartHttpServletRequest;
 import org.springframework.validation.BindingResult;
 
 public class BindServiceImplTest extends TestCase
 {
     private final TypeService typeService = new TypeServiceImpl();
 
     @Override
     protected void setUp() throws Exception
     {
         //setup type service for Template, TemplateRegion TemplateBlock
         super.setUp();
         SingletonBeanLocator.registerTestBean("typeService", this.typeService);
         AnnotationBasedTypeDefBuilder typeDefBuilder = new AnnotationBasedTypeDefBuilder();
         OtherObjectsConfigurator otherObjectsConfigurator = new OtherObjectsConfigurator();
         otherObjectsConfigurator.setProperty("otherobjects.default.date.format", "yyy-MM-dd");
         otherObjectsConfigurator.setProperty("otherobjects.default.time.format", "yyy-MM-dd");
         otherObjectsConfigurator.setProperty("otherobjects.default.timestamp.format", "yyy-MM-dd");
         typeDefBuilder.setOtherObjectsConfigurator(otherObjectsConfigurator);
         typeDefBuilder.afterPropertiesSet();
 
         this.typeService.registerType(typeDefBuilder.getTypeDef(TestObject.class));
        this.typeService.registerType(typeDefBuilder.getTypeDef(TestReferenceObject.class));
         this.typeService.registerType(typeDefBuilder.getTypeDef(Template.class));
         this.typeService.registerType(typeDefBuilder.getTypeDef(TemplateLayout.class));
         this.typeService.registerType(typeDefBuilder.getTypeDef(TemplateRegion.class));
         this.typeService.registerType(typeDefBuilder.getTypeDef(TemplateBlock.class));
         this.typeService.registerType(typeDefBuilder.getTypeDef(TemplateBlockReference.class));
         ((TypeServiceImpl) this.typeService).reset();
 
         // Hibernate stored types
         this.typeService.registerType(typeDefBuilder.getTypeDef(User.class));
         this.typeService.registerType(typeDefBuilder.getTypeDef(Role.class));
 
         // Add DynaNode type
         TypeDefImpl td = new TypeDefImpl("ArticlePage");
         td.setLabelProperty("title");
         td.addProperty(new PropertyDefImpl("title", "string", null, null, true));
         this.typeService.registerType(td);
 
     }
 
     public void testEmptyStringBinding() throws Exception
     {
         MockHttpServletRequest req = new MockHttpServletRequest();
         req.addParameter("name", "");
         req.addParameter("testString", "  no trailing white space\n");
         req.addParameter("testDate", "");
         req.addParameter("testReference", "");
 
         TestObject t = new TestObject();
 
         BindServiceImpl binder = new BindServiceImpl();
         TypeDef typeDef = this.typeService.getType(TestObject.class);
 
         binder.bind(t, typeDef, req);
 
         assertEquals(null, t.getName());
         assertEquals("no trailing white space", t.getTestString());
         assertEquals(null, t.getTestReference());
         assertEquals(null, t.getTestDate());
     }
 
     public void testFairlyComplexOOTemplateBinding() throws Exception
     {
         MockHttpServletRequest req = new MockHttpServletRequest();
         req.addParameter("layout", "db6b724b-f696-419f-837c-f64796625efe");
         req.addParameter("label", "Client");
         req.addParameter("regions[1].code", "column-2");
         req.addParameter("regions[1].label", "");
         req.addParameter("regions[0].blocks[0]", "9bf23b21-2def-4039-8d6d-2182ca84aeb0");
         req.addParameter("regions[0].label", "");
         req.addParameter("regions[1].blocks[0]", "e90e78b7-fe89-43cf-bf6a-0ce4367206ca");
         req.addParameter("id", "c0a03a8a-d25c-435a-9d3c-9e181f7e9016");
         req.addParameter("regions[0].code", "column-1");
 
         //setup objects
         Template rootItem = new Template();
         rootItem.setId("c0a03a8a-d25c-435a-9d3c-9e181f7e9016");
 
         TemplateRegion column2 = new TemplateRegion();
         column2.setCode("column-2");
 
         TemplateLayout layout = new TemplateLayout();
         layout.setId("db6b724b-f696-419f-837c-f64796625efe");
         layout.setDescription("TEST lAYOUT");
 
         TemplateBlock block1 = new TemplateBlock();
         block1.setId("9bf23b21-2def-4039-8d6d-2182ca84aeb0");
         block1.setDescription("block1");
 
         TemplateBlock block2 = new TemplateBlock();
         block2.setId("e90e78b7-fe89-43cf-bf6a-0ce4367206ca");
         block2.setDescription("block2");
 
         List<TemplateRegion> regions = new ArrayList<TemplateRegion>();
         regions.add(null);
         regions.add(column2);
         rootItem.setRegions(regions);
 
         // setup mock dao
         Map<String, Object> objects = new HashMap<String, Object>();
         objects.put(layout.getId(), layout);
         objects.put(block1.getId(), block1);
         objects.put(block2.getId(), block2);
         MockGenericDao dao = new MockGenericDao(objects);
 
         BindServiceImpl bs = new BindServiceImpl();
         JackrabbitDataStore jackrabbitDataStore = new JackrabbitDataStore();
         UniversalJcrDaoJackrabbit universalJcrDao = new UniversalJcrDaoJackrabbit();
         universalJcrDao.setTypeService(this.typeService);
         jackrabbitDataStore.setUniversalJcrDao(universalJcrDao);
         bs.setJackrabbitDataStore(jackrabbitDataStore);
         //        bs.setHibernateDataStore(new HibernateDataStore(daoService));
 
         bs.setDaoService(new MockDaoService(dao));
 
         TypeDef templateTypeDef = this.typeService.getType(Template.class);
 
         bs.bind(rootItem, templateTypeDef, req);
 
         assertEquals("Client", rootItem.getLabel());
         assertNotNull(PropertyUtils.getNestedProperty(rootItem, "regions[0]"));
         assertNotNull(PropertyUtils.getNestedProperty(rootItem, "regions[1]"));
         assertNotNull(PropertyUtils.getNestedProperty(rootItem, "regions[0].blocks[0]"));
         assertEquals("column-1", PropertyUtils.getNestedProperty(rootItem, "regions[0].code"));
         assertEquals("column-2", PropertyUtils.getNestedProperty(rootItem, "regions[1].code"));
         assertNotNull(PropertyUtils.getNestedProperty(rootItem, "regions[1].blocks[0]"));
 
         // assertEquals("block1", PropertyUtils.getNestedProperty(rootItem, "regions[0].blocks[0].description"));
         //assertEquals("block2", PropertyUtils.getNestedProperty(rootItem, "regions[1].blocks[0].description"));
 
     }
 
     public void testFairlyComplexOOTemplateBindingMultipart() throws Exception
     {
         MockMultipartHttpServletRequest req = new MockMultipartHttpServletRequest();
         req.addFile(new MockMultipartFile("testFile", "This is the file content".getBytes("UTF-8")));
 
         req.addParameter("layout", "db6b724b-f696-419f-837c-f64796625efe");
         req.addParameter("label", "Client");
         req.addParameter("regions[1].code", "column-2");
         req.addParameter("regions[1].label", "");
         req.addParameter("regions[0].blocks[0]", "9bf23b21-2def-4039-8d6d-2182ca84aeb0");
         req.addParameter("regions[0].label", "");
         req.addParameter("regions[1].blocks[0]", "e90e78b7-fe89-43cf-bf6a-0ce4367206ca");
         req.addParameter("id", "c0a03a8a-d25c-435a-9d3c-9e181f7e9016");
         req.addParameter("regions[0].code", "column-1");
 
         //setup objects
         Template rootItem = new Template();
         rootItem.setId("c0a03a8a-d25c-435a-9d3c-9e181f7e9016");
 
         TemplateRegion column2 = new TemplateRegion();
         column2.setCode("column-2");
 
         TemplateLayout layout = new TemplateLayout();
         layout.setId("db6b724b-f696-419f-837c-f64796625efe");
         layout.setDescription("TEST lAYOUT");
 
         TemplateBlock block1 = new TemplateBlock();
         block1.setId("9bf23b21-2def-4039-8d6d-2182ca84aeb0");
         block1.setDescription("block1");
 
         TemplateBlock block2 = new TemplateBlock();
         block2.setId("e90e78b7-fe89-43cf-bf6a-0ce4367206ca");
         block2.setDescription("block2");
 
         List<TemplateRegion> regions = new ArrayList<TemplateRegion>();
         regions.add(null);
         regions.add(column2);
         rootItem.setRegions(regions);
 
         // setup mock dao
         Map<String, Object> objects = new HashMap<String, Object>();
         objects.put(layout.getId(), layout);
         objects.put(block1.getId(), block1);
         objects.put(block2.getId(), block2);
         MockGenericDao dao = new MockGenericDao(objects);
 
         BindServiceImpl bs = new BindServiceImpl();
         JackrabbitDataStore jackrabbitDataStore = new JackrabbitDataStore();
         UniversalJcrDaoJackrabbit universalJcrDao = new UniversalJcrDaoJackrabbit();
         universalJcrDao.setTypeService(this.typeService);
         jackrabbitDataStore.setUniversalJcrDao(universalJcrDao);
         bs.setJackrabbitDataStore(jackrabbitDataStore);
         bs.setDaoService(new MockDaoService(dao));
 
         TypeDef templateTypeDef = this.typeService.getType(Template.class);
 
         bs.bind(rootItem, templateTypeDef, req);
 
         assertEquals("Client", rootItem.getLabel());
         assertNotNull(PropertyUtils.getNestedProperty(rootItem, "regions[0]"));
         assertNotNull(PropertyUtils.getNestedProperty(rootItem, "regions[1]"));
         assertNotNull(PropertyUtils.getNestedProperty(rootItem, "regions[0].blocks[0]"));
         assertEquals("column-1", PropertyUtils.getNestedProperty(rootItem, "regions[0].code"));
         assertEquals("column-2", PropertyUtils.getNestedProperty(rootItem, "regions[1].code"));
         assertNotNull(PropertyUtils.getNestedProperty(rootItem, "regions[1].blocks[0]"));
 
         //assertEquals("block1", PropertyUtils.getNestedProperty(rootItem, "regions[0].blocks[0].description"));
         //assertEquals("block2", PropertyUtils.getNestedProperty(rootItem, "regions[1].blocks[0].description"));
 
     }
 
     public void testSetValue()
     {
         TestObject to = new TestObject();
         BindServiceImpl bs = new BindServiceImpl();
         bs.setValue(to, "name", "Test");
     }
 
     public void testHibernateBinding() throws Exception
     {
         MockHttpServletRequest req = new MockHttpServletRequest();
         req.addParameter("email", "user@email.com");
         req.addParameter("username", "test");
         req.addParameter("roles[0]", "org.otherobjects.cms.model.Role-1");
 
         //setup objects
         Role role = new Role();
         role.setId(1L);
         role.setName("Test Role");
 
         Map<Serializable, Object> objects = new HashMap<Serializable, Object>();
         objects.put(1L, role);
         MockGenericDao roleDao = new MockGenericDao(objects);
 
         BindServiceImpl bs = new BindServiceImpl();
         bs.setDaoService(new MockDaoService(roleDao));
 
         TypeDef userTypeDef = this.typeService.getType(User.class);
 
         User u = new User();
 
         BindingResult result = bs.bind(u, userTypeDef, req);
 
         assertEquals(0, result.getErrorCount());
 
     }
 
 }
