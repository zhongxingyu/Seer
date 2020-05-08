 package com.ruyicai.actioncenter;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import org.apache.commons.collections.map.LinkedMap;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.ruyicai.actioncenter.consts.ActionJmsType;
import com.ruyicai.actioncenter.dao.TactivityDao;
 import com.ruyicai.actioncenter.dao.TuserPrizeDetailDao;
 import com.ruyicai.actioncenter.domain.TaddNumActivity;
 import com.ruyicai.actioncenter.domain.TuserPrizeDetail;
 import com.ruyicai.actioncenter.jms.listener.AddNumSuccessListener;
 import com.ruyicai.actioncenter.jms.listener.CaselotBetFullListener;
 import com.ruyicai.actioncenter.jms.listener.FundJmsListener;
 import com.ruyicai.actioncenter.service.TactionService;
 import com.ruyicai.actioncenter.util.JsonUtil;
 import com.ruyicai.actioncenter.util.Page;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/META-INF/spring/applicationContext.xml",
 		"classpath:/META-INF/spring/applicationContext-jms.xml",
 		"classpath:/META-INF/spring/applicationContext-memcache.xml" })
 public class TactivityTest {
 
 	@Autowired
 	CaselotBetFullListener caselotBetFullListener;
 
 	@Autowired
 	AddNumSuccessListener addNumSuccessListener;
 
 	@Autowired
 	TactionService tactionService;
 
 	@Autowired
 	FundJmsListener fundJmsListener;
 
 	@Autowired
 	TuserPrizeDetailDao tuserPrizeDetailDao;
	
	@Autowired
	TactivityDao tactivityDao;
 
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testCreateTactivity() {
 		Map<String, Object> map = new LinkedMap();
 		map.put("amt", 1000);
 		map.put("prize", 300);
 		String express = JsonUtil.toJson(map);
		tactivityDao.saveOrUpdate("T01015", null, "00092493", null, ActionJmsType.First_Order.value, express, 1,
 				ActionJmsType.First_Order.memo);
 	}
 
 	@Test
 	public void testCaselotBetFull() throws InterruptedException {
 		int nextInt = new Random().nextInt(10000);
 		String body = "{\"batchcode\":\"2011148\",\"buyAmtByFollower\":0,\"buyAmtByStarter\":10000,\"caselotinfo\":null,\"commisionRatio\":10,\"content\":null,\"description\":\"\",\"displayState\":2,\"displayStateMemo\":\"满员\",\"endTime\":null,\"full\":true,\"hasachievement\":0,\"id\":\"C"
 				+ nextInt
 				+ "00000026031\",\"isWinner\":0,\"lotno\":\"F47104\",\"lotsType\":0,\"minAmt\":100,\"orderid\":\"2011121300009030\",\"participantCount\":1,\"safeAmt\":0,\"sortState\":0,\"startTime\":1323763106050,\"starter\":\"00000619\",\"state\":3,\"title\":\"\",\"totalAmt\":10000,\"version\":3,\"visibility\":0,\"winBigAmt\":0,\"winDetail\":null,\"winEndTime\":null,\"winFlag\":null,\"winLittleAmt\":null,\"winPreAmt\":0,\"winStartTime\":null}";
 		caselotBetFullListener.encashCustomer(body);
 		Thread.sleep(1000 * 10);
 	}
 
 	@Test
 	public void testAddNumOneYear() throws Exception {
 		String userno = "00000619";
 		String body = "{\"accountnomoneysms\":0,\"amt\":400,\"batchcode\":\"2011343\",\"betRequests\":[{\"amt\":200,\"betcode\":\"0001060708^\"}],\"bettype\":\"0\",\"buyuserno\":\""
 				+ userno
 				+ "\",\"caseLotRequest\":null,\"channel\":\"2\",\"desc\":\"单期奖金≥1000元终止追号\",\"drawway\":0,\"endsms\":\"0\",\"leijiprizeendamt\":0,\"lotmulti\":1,\"lotno\":\"F47103\",\"lotsType\":0,\"memo\":\"直选单式\",\"oneamount\":200,\"paytype\":0,\"prizeend\":1,\"prizeendamt\":1000,\"subchannel\":\"00092493\",\"subscribeRequests\":[{\"amt\":200,\"batchcode\":\"2011343\",\"desc\":\"2_1_0\",\"endtime\":1313477552255,\"lotmulti\":1},{\"amt\":200,\"batchcode\":\"2011344\",\"desc\":\"4_1_0\",\"endtime\":1313477552255,\"lotmulti\":1}],\"userno\":\"00000205\"}";
 		String flowno = tactionService.addNumOneYear(body);
 		System.out.println("flowno=" + flowno);
 		addNumSuccessListener.addNumSuccessCustomer(flowno, userno);
 	}
 
 	@Test
 	public void testTaddNumActivity() {
 		TaddNumActivity activity = TaddNumActivity.createTaddNumActivity("123456", "json");
 		System.out.println(activity);
 		activity.updateTaddNumActivity("123456");
 		System.out.println(activity);
 		activity.remove();
 	}
 
 	@Test
 	public void testFundJmsListener() {
 		fundJmsListener.fundJmsCustomer(null, 0L, "00000619", 2000L, ActionJmsType.CHONGZHI_SUCCESS.value, "22222222",
 				1);
 	}
 
 	@Test
 	public void testChongZhiJmsListener() {
 		fundJmsListener.fundJmsCustomer(null, 0L, "00000193", 500000L, ActionJmsType.GOUCAI_SUCCESS.value,
 				"3333333333333333", 3);
 	}
 
 	@Test
 	public void testfindTuserPrizeDetailByPage() {
 		Page<TuserPrizeDetail> page = new Page<TuserPrizeDetail>(0, 20);
 		tuserPrizeDetailDao.findTuserPrizeDetailByPage(new HashMap<String, Object>(), page);
 		for (TuserPrizeDetail detail : page.getList()) {
 			System.out.println(detail);
 		}
 	}
 }
