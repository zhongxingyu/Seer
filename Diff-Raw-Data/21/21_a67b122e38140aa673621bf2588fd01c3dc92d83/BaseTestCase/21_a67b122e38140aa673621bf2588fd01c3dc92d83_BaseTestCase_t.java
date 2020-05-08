 package com.pms.service.base;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.FileSystemXmlApplicationContext;
 
 import com.pms.service.cfg.ConfigurationManager;
 import com.pms.service.dao.ICommonDao;
 import com.pms.service.dao.impl.mongo.CommonDaoMongoImpl;
 import com.pms.service.mockbean.ApiConstants;
 import com.pms.service.mockbean.DBBean;
 import com.pms.service.mockbean.SalesContractBean;
 import com.pms.service.service.IProjectService;
 import com.pms.service.service.IReportService;
 import com.pms.service.service.ISalesContractService;
 import com.pms.service.service.IUserService;
 import com.pms.service.service.impl.ProjectServiceImpl;
 import com.pms.service.service.impl.ReportServiceImpl;
 import com.pms.service.service.impl.UserServiceImpl;
 import com.pms.service.util.EmailUtil;
 import com.pms.service.util.ExcleUtil;
 
 public class BaseTestCase extends TestCase {
 
     protected static ApplicationContext ac;
     protected static ICommonDao commonDao;
     protected static IUserService userService;
     protected static IProjectService projectService;
     protected static IReportService reportService;
     protected static ISalesContractService scService;
     public static final String TEST_ID = "123456789012345678901234";
 
     public BaseTestCase() {
         ac = new FileSystemXmlApplicationContext("/src/main/webapp/WEB-INF/applicationContext.xml");
         commonDao = (CommonDaoMongoImpl) ac.getBean("commonDao");
         userService = (UserServiceImpl) ac.getBean("userService");
         projectService = (ProjectServiceImpl) ac.getBean("projectService");
         reportService = (ReportServiceImpl) ac.getBean("reportService");
         scService = (ISalesContractService) ac.getBean("salesContractService");
         ConfigurationManager.setProperties(ConfigurationManager.DB_NAME, "pms");
     }
 
     public void testEmpty() throws IOException {
 
         assertTrue(true);
 
         List emails = new ArrayList();
        emails.add("dong_zhengfeng@thtf.com.cn");
         emails.add("251148471@qq.com");
        
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put(ApiConstants.LIMIT_KEYS, SalesContractBean.SC_EQ_LIST);
 
         List<Object> results = this.commonDao.listLimitKeyValues(parameters, DBBean.REPOSITORY);
 
         for (Object obj : results) {
             if (obj != null) {
                List<Map<String, Object>> eqlistMap = new ArrayList<Map<String, Object>>();
                EmailUtil.sendMail("test", emails, "contract approved", null);
 
             }
         }
 
     }
     
     public void testImportPurchaseContract(){
 //    	reportService.importPurchaseContract(null);
     }
     
     public void setUp(){
 //        commonDao.deleteByQuery(null, DBBean.USER);
     }
     
 }
