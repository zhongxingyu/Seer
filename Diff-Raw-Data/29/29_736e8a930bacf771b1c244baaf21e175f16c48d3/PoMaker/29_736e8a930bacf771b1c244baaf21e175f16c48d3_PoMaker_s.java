 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.core.model;
 
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesBP.CompNameCreationContext;
 import org.eclipse.jubula.client.core.businessprocess.IParamNameMapper;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.PersistenceUtil;
 import org.eclipse.jubula.tools.objects.IComponentIdentifier;
 import org.eclipse.jubula.tools.xml.businessmodell.Profile;
 import org.eclipse.jubula.tools.xml.businessprocess.ProfileBuilder;
 
 
 /**
  * @author BREDEX GmbH
  * @created 20.12.2005
  */
 public abstract class PoMaker {
 
     /** hide default constructor */
     private PoMaker() {
         // hide
     }
 
     /**
      * factory method to replace constructor
      * @return AUTConfigPO
      */
     public static IAUTConfigPO createAUTConfigPO() {
         return createAUTConfigPO(PersistenceUtil.generateGuid());
     }
     
     /**
      * factory method to replace constructor
      * @param orig orig
      * @return AUTConfigPO
      */
     public static IAUTConfigPO createAUTConfigPO(IAUTConfigPO orig) {
         return new AUTConfigPO(orig);
     }
 
     /**
      * factory method to replace constructor
      * @param guid guid
      * @return AUTConfigPO
      */
     public static IAUTConfigPO createAUTConfigPO(String guid) {
         return new AUTConfigPO(guid);
     }
 
     /**
      * factory method to replace constructor
      * @param autName autName
      * @return AUTMainPO
      */
     public static IAUTMainPO createAUTMainPO(String autName) {
         return new AUTMainPO(autName);
     }
     
     /**
      * factory method to replace constructor
      * @param autName autName
      * @param guid guid
      * @return AUTMainPO
      */
     public static IAUTMainPO createAUTMainPO(String autName, String guid) {
         return new AUTMainPO(autName, guid);
     }
     
     /**
      * factory method to replace constructor
      * @param name name
      * @param type type
      * @return CompNamesPairPO
      */
     public static ICompNamesPairPO createCompNamesPairPO(String name,
             String type) {
         
         return new CompNamesPairPO(name, type);
     }
 
     /**
      * factory method to replace constructor
      * @param firstName firstName
      * @param secondName secondName
      * @param type type
      * @return CompNamesPairPO
      */
     public static ICompNamesPairPO createCompNamesPairPO(String firstName,
         String secondName, String type) {
         
         return new CompNamesPairPO(firstName, secondName, type);
     }
 
     /**
      * factory method to replace constructor
      * @param list list
      * @return DataSetPO
      */
     public static IDataSetPO createListWrapperPO(List<ITestDataPO> list) {
         return new DataSetPO(list);
     }
 
     /**
      * factory method to replace constructor
      * @param name name
      * @return ObjectMappingCategoryPO
      */
     public static IObjectMappingCategoryPO createObjectMappingCategoryPO(
             String name) {
         return new ObjectMappingCategoryPO(name);
     }
 
     /**
      * factory method to replace constructor
      * @param tech tech
      * @return ObjectMappingAssoziationPO
      */
     public static IObjectMappingAssoziationPO createObjectMappingAssoziationPO(
         IComponentIdentifier tech) {
         return new ObjectMappingAssoziationPO(tech);
     }
 
     /**
      * factory method to replace constructor
      * @param tec tec
      * @param logic logic
      * @return ObjectMAppingAssoziationPO
      */ 
     public static IObjectMappingAssoziationPO createObjectMappingAssoziationPO(
         IComponentIdentifier tec, List<String> logic) {
         return new ObjectMappingAssoziationPO(tec, logic);
     }
 
     /**
      * factory method to replace constructor
      * @param tec tec
      * @param logic logic
      * @return ObjectMappingAssoziationPO
      */
     public static IObjectMappingAssoziationPO createObjectMappingAssoziationPO(
         IComponentIdentifier tec, String logic) {
         return new ObjectMappingAssoziationPO(tec, logic);
     }
     
     /**
      * factory method to replace constructor
      * @return ObjectMappingPO
      */
     public static IObjectMappingPO createObjectMappingPO() {
         return createObjectMappingPO(createObjectMappingProfile()); 
     }
 
     /**
      * factory method to replace constructor
      * @param profilePo profilePo
      * @return ObjectMappingProfilePO
      */
     public static IObjectMappingPO createObjectMappingPO(
             IObjectMappingProfilePO profilePo) {
        
         IObjectMappingCategoryPO mappedCategory =
             createObjectMappingCategoryPO(
                     IObjectMappingCategoryPO.MAPPEDCATEGORY);
         IObjectMappingCategoryPO unmappedLogicalCategory =
             createObjectMappingCategoryPO(
                     IObjectMappingCategoryPO.UNMAPPEDLOGICALCATEGORY);
         IObjectMappingCategoryPO unmappedTechnicalCategory =
             createObjectMappingCategoryPO(
                     IObjectMappingCategoryPO.UNMAPPEDTECHNICALCATEGORY);
         
         IObjectMappingPO map = new ObjectMappingPO(
                 mappedCategory, unmappedLogicalCategory, 
                 unmappedTechnicalCategory); 
         map.setProfile(profilePo);
         return map;
     }
 
     /**
      * factory method to replace constructor
      * @return ObjectMappingProfilePO
      */
     public static IObjectMappingProfilePO createObjectMappingProfile() {
         return createObjectMappingProfile(ProfileBuilder.getDefaultProfile());
     }
 
     /**
      * factory method to replace constructor
      * @param template inital data for the IObjectMappingProfilePO
      * @return ObjectMappingProfilePO
      */
     public static IObjectMappingProfilePO createObjectMappingProfile(
             Profile template) {
 
         IObjectMappingProfilePO profilePo = new ObjectMappingProfilePO();
         profilePo.useTemplate(template);
         return profilePo;
     }
 
     /**
      * factory method to replace constructor
      * @param type type
      * @param uniqueId uniqueId
      * @return CapParamDescriptionPO
      */
     public static IParamDescriptionPO createCapParamDescriptionPO(
         String type, String uniqueId) {
         CapParamDescriptionPO description = 
             new CapParamDescriptionPO(type, uniqueId);
         IProjectPO currentProject = GeneralStorage.getInstance().getProject();
         if (currentProject != null) {
             description.setParentProjectId(currentProject.getId());
         }
         return description;
     }
     
     
     /**
      * use this method for parameters already have a guid (for example after import)
      * @param type parameter type
      * @param name parameter name
      * @param guid guid of parameter
      * @param mapper mapper to resolve param names
      * use for all new created (not persistent) ParamDescriptions in the context of a single session
      * a single instance of ParamNameBPDecorator 
      * @return TcParamDescription for parameter
      */
     public static IParamDescriptionPO createTcParamDescriptionPO (
         String type, String name, String guid, IParamNameMapper mapper) {
         mapper.addParamNamePO(new ParamNamePO(guid, name));
         IProjectPO currentProject = GeneralStorage.getInstance().getProject();
         TcParamDescriptionPO description = 
             new TcParamDescriptionPO(type, guid, mapper);
         if (currentProject != null) {
             description.setParentProjectId(currentProject.getId());
         }
 
         return description;
     }
     
 
     /**
      * factory method to replace constructor
      * @param projectGuid projectGuid
      * @param majorNumber majorNumber
      * @param minorNumber minorNumber
      * @return ReusedProjectPO
      */
     public static IReusedProjectPO createReusedProjectPO(String projectGuid,
             Integer majorNumber, Integer minorNumber) {
         return new ReusedProjectPO(projectGuid, majorNumber, minorNumber);
     }
 
     /**
      * factory method to replace constructor
      * @return AUTContPO
      */
     public static IAUTContPO createAUTContPO() {
         return new AUTContPO();
     }
     
     /**
      * factory method to replace constructor
      * @return TestResultSummaryPO
      */
     public static ITestResultSummaryPO createTestResultSummaryPO() {
         return createTestResultSummaryPO(PersistenceUtil.generateGuid());
     }
 
     /**
      * factory method to replace constructor
      * @param guid The GUID for the Test Result Summary.
      * @return TestResultSummaryPO
      */
     public static ITestResultSummaryPO createTestResultSummaryPO(String guid) {
         return new TestResultSummaryPO(guid);
     }
 
     /**
      * factory method to replace constructor
      * @return TestResultPO
      */
     public static ITestResultPO createTestResultPO() {
         return new TestResultPO();
     }
     
     /**
      * factory method to replace constructor
      * @param parameters parameters
      * @return TestResultPO
      */
     public static ITestResultPO createTestResultPO(
             List<IParameterDetailsPO> parameters) {
         return new TestResultPO(parameters);
     }
     
     /**
      * factory method to replace constructor
      * @return ParameterDetailsPO
      */
     public static IParameterDetailsPO createParameterDetailsPO() {
         return new ParameterDetailsPO();
     }
 
     /**
      * factory method to replace constructor
      * @return ProjectPropertiesPO
      */
     public static IProjectPropertiesPO createProjectPropertiesPO() {
         return new ProjectPropertiesPO();
     }
 
     /**
      * factory method to replace constructor
      * @param majorNumber majorNumber
      * @param minorNumber minorNumber
      * @return ProjectPropertiesPO
      */
     public static IProjectPropertiesPO createProjectPropertiesPO(
             Integer majorNumber, Integer minorNumber) {
         ProjectPropertiesPO projProp = 
             new ProjectPropertiesPO(majorNumber, minorNumber);
         projProp.setCheckConfCont(createCheckConfContPO());
         return projProp;
     }
 
     /**
      * factory method to replace constructor
      * @param projectGuid projectGuid
      * @param projectName projectName
      * @return ProjectNamePO
      */
     public static IProjectNamePO createProjectNamePO(
             String projectGuid, String projectName) {
         
         return new ProjectNamePO(projectGuid, projectName);
     }
     
 
     /**
      * @param proj The reused project
      * @return information that can be used to identify the reused project
      */
     public static IReusedProjectPO createReusedProjectPO(IProjectPO proj) {
         return createReusedProjectPO(proj.getGuid(), 
             proj.getMajorProjectVersion(), proj.getMinorProjectVersion());
     }
 
     /**
      * factory method to replace constructor
      * @return SpecObjContPO
      */
     public static ISpecObjContPO createSpecObjContPO() {
         return new SpecObjContPO();
     }
     
     /**
      * factory method to replace constructor
      * @return SpecObjContPO
      */
     public static IExecObjContPO createExecObjContPO() {
         return new ExecObjContPO();
     }
 
     /**
      * factory method to replace constructor
      * @param node node
      * @return TDManagerPO
      */
     public static ITDManager createTDManagerPO(IParameterInterfacePO node) {
         return new TDManagerPO(node);
     }
 
     /**
      * factory method to replace constructor
      * @param node node
      * @param uniqueIds uniqueIds
      * @return TDManagerPO
      */
     public static ITDManager createTDManagerPO(IParameterInterfacePO node, 
         List<String> uniqueIds) {
         
         return new TDManagerPO(node, uniqueIds);
     }
     
     /**
      * Creates and returns a Test Data Manager object using the arguments 
      * provided.
      * 
      * @param name
      *          Name to use in referencing the created object. 
      *          May not be <code>null</code>.
      * @return the created Test Data Manager.
      */
     public static ITestDataCubePO createTestDataCubePO(String name) {
         return new TestDataCubePO(name);
     }
     
     /**
      * factory method to replace constructor
      * @param initialValues The initial values for the created object.
      * @return TestDataPO
      */
     public static ITestDataPO createTestDataPO(
             Map<Locale, String> initialValues) {
         return new TestDataPO(initialValues);
     }
    
     /**
      * factory method to replace constructor
      * @return TestDataPO
      */
     public static ITestDataPO createTestDataPO() {
         return new TestDataPO();
     }
 
     /**
      * factory method to replace constructor
      * 
      * @return TestDataCategoryPO
      */
     public static ITestDataCategoryPO createTestDataCategoryPO() {
         return new TestDataCategoryPO();
     }
 
     /**
      * factory method to replace constructor
      * 
      * @param name The name of the category.
      * 
      * @return TestDataCategoryPO
      */
     public static ITestDataCategoryPO createTestDataCategoryPO(String name) {
         ITestDataCategoryPO testDataCategory = new TestDataCategoryPO();
         testDataCategory.setName(name);
         return testDataCategory;
     }
 
     /**
      * factory method to replace constructor
      * @param toolkitName toolkitName
      * @param majorVersion majorVersion
      * @param minorVersion minorVersion
      * @param projectID projectID
      * @return UsedToolkitPO
      */
     public static IUsedToolkitPO createUsedToolkitsPO(String toolkitName, 
         int majorVersion, int minorVersion, Long projectID) {
         
         return new UsedToolkitPO(toolkitName, majorVersion, minorVersion, 
             projectID);
     }
     
     
     
     /**
      * factory method to replace constructor.
      * {@inheritDoc}
      * @param guid
      * @param name
      * @param type
      * @param ctx the CompNameCreationContext.
      * @return
      */
     public static IComponentNamePO createComponentNamePO(String guid, 
             String name, String type, CompNameCreationContext ctx, 
             Long parentProjectId) {
         
         IComponentNamePO compNamePo = 
             new ComponentNamePO(guid, name, type, ctx);
         compNamePo.setParentProjectId(parentProjectId);
         return compNamePo;
     }
     
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<AUTConfigPO> getAUTConfigClass() {
         return AUTConfigPO.class;
     }
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<AUTMainPO> getAUTMainClass() {
         return AUTMainPO.class;
     }
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<CompIdentifierPO> getCompIdentifierClass() {
         return CompIdentifierPO.class;
     }
 
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<CompNamesPairPO> getCompNamesPairClass() {
         return CompNamesPairPO.class;
     }
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<DataSetPO> getListWrapperClass() {
         return DataSetPO.class;
     }

     /**
      * get the class instance of the PO (needed by Persistor)
      * 
      * @return the class instance of the PO
      */
     public static Class<ObjectMappingAssoziationPO> 
     getObjectMappingAssoziationClass() {
         return ObjectMappingAssoziationPO.class;
     }
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<ObjectMappingCategoryPO> 
     getObjectMappingCategoryClass() {
         return ObjectMappingCategoryPO.class;
     }
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<ObjectMappingPO> getObjectMappingClass() {
         return ObjectMappingPO.class;
     }
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<ParamDescriptionPO> getParamDescriptionClass() {
         return ParamDescriptionPO.class;
     }
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<CapParamDescriptionPO> getCapParamDescriptionClass() {
         return CapParamDescriptionPO.class;
     }
     
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<TcParamDescriptionPO> getTcParamDescriptionClass() {
         return TcParamDescriptionPO.class;
     }
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<ReusedProjectPO> getReusedProjectClass() {
         return ReusedProjectPO.class;
     }
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<ProjectPropertiesPO> getProjectPropertiesClass() {
         return ProjectPropertiesPO.class;
     }
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<ProjectNamePO> getProjectNameClass() {
         return ProjectNamePO.class;
     }
     
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<ParamNamePO> getParamNameClass() {
         return ParamNamePO.class;
     }
     
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<ParameterDetailsPO> getParameterDetailsClass() {
         return ParameterDetailsPO.class;
     }
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<AUTContPO> getAUTContClass() {
         return AUTContPO.class;
     }
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<TDManagerPO> getTDManagerClass() {
         return TDManagerPO.class;
     }
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<TestDataCubePO> getTestDataCubeClass() {
         return TestDataCubePO.class;
     }
     
     /**
      * get the class instance of the PO 
      * @return the class instance of the PO
      */
     public static Class<TestCasePO> getTestCasePOClass() {
         return TestCasePO.class;
     }
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<TestDataPO> getTestDataClass() {
         return TestDataPO.class;
     }
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<WrapperPO> getWrapperClass() {
         return WrapperPO.class;
     }
     
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<TestSuitePO> getTestSuiteClass() {
         return TestSuitePO.class;
     }
     
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<TestResultSummaryPO> getTestResultSummaryClass() {
         return TestResultSummaryPO.class;
     }
     
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<TestResultPO> getTestResultClass() {
         return TestResultPO.class;
     }
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<UsedToolkitPO> getUsedToolkitClass() {
         return UsedToolkitPO.class;
     }
 
     /**
      * get the class instance of the PO (needed by Persistor)
      * @return the class instance of the PO
      */
     public static Class<ObjectMappingProfilePO> 
     getObjectMappingProfileClass() {
         return ObjectMappingProfilePO.class;
     }
 
     /**
      * @return the class instance of the PO
      */
     public static Class<AbstractGuidNamePO> getAbstractGuidNameClass() {
         return AbstractGuidNamePO.class;
     }
     
     /**
      * @return the class instance of the PO
      */
     public static Class<ComponentNamePO> getComponentNameClass() {
         return ComponentNamePO.class;
     }
     
     /**
      * @return a new check conf container
      */
     public static ICheckConfContPO createCheckConfContPO() {
         return new CheckConfContPO();
     }
 
 }
