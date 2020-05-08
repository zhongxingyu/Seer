 package cz.cuni.mff.odcleanstore.webfrontend.pages.pipelines;
 
 import cz.cuni.mff.odcleanstore.webfrontend.core.DaoLookupFactory;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.en.OIRuleAssignmentDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.en.QARuleAssignmentDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.oi.OIRulesGroupDao;
 import cz.cuni.mff.odcleanstore.webfrontend.dao.qa.QARulesGroupDao;
 import cz.cuni.mff.odcleanstore.webfrontend.pages.transformers.oi.OIGroupDetailPage;
 import cz.cuni.mff.odcleanstore.webfrontend.pages.transformers.qa.QAGroupDetailPage;
 
 public class AssignedGroupsListPageFactory 
 {
 	public static AssignedGroupsList createAssignedQAGroupsList(
 		DaoLookupFactory daoLookupFactory, Long transformerInstanceId
 	)
 	{
 		return new AssignedGroupsList(
 			"assignedGroupsListSection", 
 			transformerInstanceId, 
 			daoLookupFactory.getDaoForEntityWithSurrogateKey(QARulesGroupDao.class), 
 			daoLookupFactory.getDaoForEntityWithSurrogateKey(QARuleAssignmentDao.class), 
 			QAGroupDetailPage.class
 		);
 	}
 	
 	public static AssignedGroupsList createAssignedOIGroupsList(
 		DaoLookupFactory daoLookupFactory, Long transformerInstanceId
 	)
 	{
 		return new AssignedGroupsList(
 			"assignedGroupsListSection", 
 			transformerInstanceId, 
 			daoLookupFactory.getDaoForEntityWithSurrogateKey(OIRulesGroupDao.class), 
 			daoLookupFactory.getDaoForEntityWithSurrogateKey(OIRuleAssignmentDao.class), 
			QAGroupDetailPage.class
 		);
 	}
 }
