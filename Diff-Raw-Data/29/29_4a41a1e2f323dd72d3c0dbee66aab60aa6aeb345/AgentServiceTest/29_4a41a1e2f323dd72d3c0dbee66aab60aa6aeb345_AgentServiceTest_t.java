 package com.ruyicai.actioncenter;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.annotation.DirtiesContext;
 import org.springframework.test.annotation.DirtiesContext.ClassMode;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
 
 import com.ruyicai.actioncenter.consts.ActionJmsType;
import com.ruyicai.actioncenter.dao.TactivityDao;
 import com.ruyicai.actioncenter.domain.Tagent;
 import com.ruyicai.actioncenter.jms.listener.FundJmsListener;
 import com.ruyicai.actioncenter.service.MemcachedService;
 import com.ruyicai.actioncenter.service.TactionService;
 import com.ruyicai.actioncenter.util.JsonUtil;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
 @ContextConfiguration(locations = { "classpath:/META-INF/spring/applicationContext.xml",
 		"classpath:/META-INF/spring/applicationContext-jms.xml",
 		"classpath:/META-INF/spring/applicationContext-memcache.xml" })
 @DirtiesContext(classMode = ClassMode.AFTER_CLASS)
 public class AgentServiceTest {
 
 	@Autowired
 	TactionService agentService;
 
 	@Autowired
 	FundJmsListener fundJmsListener;
 
 	@Autowired
 	MemcachedService<Tagent> memcachedService;
	
	@Autowired
	TactivityDao tactivityDao;
 
 	@Before
 	public void clearCache() {
 		memcachedService.flushAll();
 	}
 
 	@Test
 	public void testRegAgent() {
 		agentService.registerAgent("00000619", "15847754382");
 	}
 
 	// @Test
 	public void testCreateTactivity() {
 		Map<String, Object> chongzhiActivity = new HashMap<String, Object>();
 		chongzhiActivity.put("borderamt", 10000);
 		chongzhiActivity.put("ltPrizeRate", 5);
 		chongzhiActivity.put("gtPrizeRate", 7);
 		String jsonChongzhi = JsonUtil.toJson(chongzhiActivity);
		tactivityDao.saveOrUpdate(null, null, "00092493", null, ActionJmsType.CHONGZHI_SUCCESS.value, jsonChongzhi, 1,
 				null);
 		Map<String, Object> goucaiActivity = new HashMap<String, Object>();
 		goucaiActivity.put("totalamt", 2000);
 		goucaiActivity.put("prizeamt", 200);
 		goucaiActivity.put("effectiveTime", "2011-1-1 00:00:00");
 		String jsonGoucai = JsonUtil.toJson(goucaiActivity);
		tactivityDao.saveOrUpdate(null, null, "00092493", "545", ActionJmsType.GOUCAI_SUCCESS.value, jsonGoucai, 1, null);
		tactivityDao.saveOrUpdate(null, null, "00092493", "651", ActionJmsType.GOUCAI_SUCCESS.value, jsonGoucai, 1, null);
 	}
 
 	@Test
 	public void testFundJmsListener() throws InterruptedException {
 		fundJmsListener
 				.fundJmsCustomer(null, 0L, "00000619", 10000L, ActionJmsType.CHONGZHI_SUCCESS.value, "123456", 1);
 		fundJmsListener.fundJmsCustomer(null, 0L, "00000619", 2000L, ActionJmsType.GOUCAI_SUCCESS.value, "111111", 3);
 		Thread.sleep(1000 * 5);
 		memcachedService.flushAll();
 	}
 
 }
