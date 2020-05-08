 package com.selfdiagnosis.web.admin;
 
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.binding.mapping.Mapper;
 import org.springframework.binding.mapping.MappingResults;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.webflow.config.FlowDefinitionResource;
 import org.springframework.webflow.config.FlowDefinitionResourceFactory;
 import org.springframework.webflow.core.collection.LocalAttributeMap;
 import org.springframework.webflow.engine.EndState;
 import org.springframework.webflow.engine.Flow;
 import org.springframework.webflow.test.MockExternalContext;
 import org.springframework.webflow.test.MockFlowBuilderContext;
 
 import com.selfdiagnosis.core.entity.SelfDiagnosisEntity;
 import com.selfdiagnosis.core.service.AdminService;
 import com.selfdiagnosis.test.SpringWebContextTest;
 
 /**
  * Parent test for all list flows.
  * 
  * @author mmieszkowski
  * 
  */
 public abstract class ParentListFlowExecutionTest extends SpringWebContextTest {
 
     /**
      * {@link AdminService} to mock.
      */
     private AdminService adminServiceMock;
 
     /**
      * EasyMock set up.
      */
     @Before
     public void setUp() {
         setAdminServiceMock(EasyMock.createMock(AdminService.class));
     }
 
     @Override
     protected abstract FlowDefinitionResource getResource(FlowDefinitionResourceFactory resourceFactory);
 
     @Override
     protected FlowDefinitionResource[] getModelResources(FlowDefinitionResourceFactory resourceFactory) {
         return new FlowDefinitionResource[] { resourceFactory
                 .createFileResource("src/main/webapp/WEB-INF/jsp/admin/parent-list-flow.xml") };
     }
 
     @Override
     protected void configureFlowBuilderContext(MockFlowBuilderContext builderContext) {
         builderContext.registerBean("adminService", getAdminServiceMock());
     }
 
     /**
      * Tests flow start.
      */
     @Test
     public void testStartFlow() {
 
         entityListOnRender();
         EasyMock.replay(getAdminServiceMock());
 
         MockExternalContext context = new MockExternalContext();
         startFlow(context);
 
         assertCurrentStateEquals("entityList");
         assertResponseWrittenEquals(getFormName(), context);
         EasyMock.verify(getAdminServiceMock());
     }
 
     /**
     * Universal method for entity deletion.
     * 
     */
    @Test
    @Transactional
    public void testFlowDeleteEntity() {
        SelfDiagnosisEntity entity = createEntity();
        adminServiceMock.deleteEntity(entity);
//        entityListOnRender();
        EasyMock.replay(getAdminServiceMock());

        setCurrentState("entityList");
        getFlowScope().put("entity", entity);

        MockExternalContext context = new MockExternalContext();
        context.setEventId("deleteEntity");
        resumeFlow(context);

        assertFlowExecutionActive();
        assertCurrentStateEquals("entityList");
        assertResponseWrittenEquals(getFormName(), context);

        EasyMock.verify(getAdminServiceMock());
        
    }

    /**
      * Universal method for subflow test.
      * 
      * @param subflow
      *            subflow to test
      * @return disease entity processed by the flow
      */
     public SelfDiagnosisEntity testFlowEditEntity(Flow subflow) {
         SelfDiagnosisEntity entity = createEntity();
         setCurrentState("entityList");
         getFlowScope().put("entity", entity);
 
         getFlowDefinitionRegistry().registerFlowDefinition(subflow);
         MockExternalContext context = new MockExternalContext();
         context.setEventId("editEntity");
         resumeFlow(context);
 
         assertFlowExecutionActive();
         assertCurrentStateEquals("entityList");
         assertResponseWrittenEquals(getFormName(), context);
         return entity;
     }
 
     /**
      * Tests edit action for all subflow output options.
      */
     @Test
     @Transactional
     public void testFlowEdit() {
         entityListOnRender();
         EasyMock.expectLastCall().times(2);
         EasyMock.replay(getAdminServiceMock());
 
         testFlowEditEntity(createMockSubflowBackOutput());
         testFlowEditEntity(createMockSubflowSaveAndBackOutput());
 
         EasyMock.verify(getAdminServiceMock());
     }
 
     /**
      * @return subflow mock with back output event
      */
     public Flow createMockSubflowBackOutput() {
         Flow mockFlow = new Flow(getSubflowName());
         new EndState(mockFlow, "back");
 
         return mockFlow;
     }
 
     /**
      * @return subflow mock with saveAndBack output event
      */
     public Flow createMockSubflowSaveAndBackOutput() {
         Flow mockFlow = new Flow(getSubflowName());
         EndState endState = new EndState(mockFlow, "saveAndBack");
         endState.setOutputMapper(new Mapper() {
 
             @Override
             public MappingResults map(Object source, Object target) {
                 ((LocalAttributeMap) target).put("entity", createEntity());
                 return null;
             }
         });
 
         return mockFlow;
     }
 
     /**
      * 
      * @return form subflow name
      */
     public abstract String getSubflowName();
 
     /**
      * Represents action in <on-render> block of entityForm.
      */
     public abstract void entityListOnRender();
 
     /**
      * Creates entity - with no id.
      * 
      * @return new entity
      */
     public abstract SelfDiagnosisEntity createEntity();
 
     /**
      * @return jsp form file name
      */
     public abstract String getFormName();
 
     public AdminService getAdminServiceMock() {
         return adminServiceMock;
     }
 
     public void setAdminServiceMock(AdminService adminServiceMock) {
         this.adminServiceMock = adminServiceMock;
     }
 
 }
