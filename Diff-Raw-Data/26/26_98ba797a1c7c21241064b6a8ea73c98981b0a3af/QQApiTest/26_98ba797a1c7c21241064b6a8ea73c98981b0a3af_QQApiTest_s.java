 package com.chinarewards.qqgpvn.main.qqapi;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.persistence.Query;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.junit.Test;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.servlet.ServletHandler;
 import org.mortbay.jetty.servlet.ServletHolder;
 
 import com.chinarewards.qqgbpvn.main.test.JpaGuiceTest;
 import com.chinarewards.qqgbvpn.config.DatabaseProperties;
 import com.chinarewards.qqgbvpn.config.PosNetworkProperties;
 import com.chinarewards.qqgbvpn.domain.Agent;
 import com.chinarewards.qqgbvpn.domain.GrouponCache;
 import com.chinarewards.qqgbvpn.domain.PageInfo;
 import com.chinarewards.qqgbvpn.domain.Pos;
 import com.chinarewards.qqgbvpn.domain.PosAssignment;
 import com.chinarewards.qqgbvpn.domain.event.DomainEvent;
 import com.chinarewards.qqgbvpn.domain.status.PosDeliveryStatus;
 import com.chinarewards.qqgbvpn.domain.status.PosInitializationStatus;
 import com.chinarewards.qqgbvpn.domain.status.PosOperationStatus;
import com.chinarewards.qqgbvpn.main.QQApiModule;
 import com.chinarewards.qqgbvpn.main.dao.qqapi.GroupBuyingDao;
 import com.chinarewards.qqgbvpn.main.exception.CopyPropertiesException;
 import com.chinarewards.qqgbvpn.main.exception.SaveDBException;
 import com.chinarewards.qqgbvpn.main.logic.qqapi.GroupBuyingManager;
 import com.chinarewards.qqgbvpn.main.logic.qqapi.impl.HardCodedServlet;
 import com.chinarewards.qqgbvpn.qqapi.exception.MD5Exception;
 import com.chinarewards.qqgbvpn.qqapi.exception.ParseXMLException;
 import com.chinarewards.qqgbvpn.qqapi.exception.SendPostTimeOutException;
 import com.chinarewards.qqgbvpn.qqapi.util.GroupBuyingUtil;
 import com.chinarewards.qqgbvpn.qqapi.vo.GroupBuyingSearchListVO;
 import com.chinarewards.qqgbvpn.qqapi.vo.GroupBuyingUnbindVO;
 import com.chinarewards.qqgbvpn.qqapi.vo.GroupBuyingValidateResultVO;
 import com.google.inject.Module;
 import com.google.inject.persist.jpa.JpaPersistModule;
 
 public class QQApiTest extends JpaGuiceTest {
 	
 	private Server server = new Server(0);
 
 	@Override
 	protected Module[] getModules() {
 		return new Module[] {
				new QQApiModule(),
 				new JpaPersistModule("posnet")
 						.properties(new DatabaseProperties().getProperties()) };
 	}
 	
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
 		initTestServer();
 	}
 	
 	private void initTestServer() {
 		//build test server start
 		try {
 			//Server server = new Server(0);
 			if (!server.isStarted()) {
 				ServletHandler scHandler = new ServletHandler();
 				scHandler.addServletWithMapping(getInitGrouponCacheServletHolder(), "/initGrouponCache");
 				scHandler.addServletWithMapping(getGroupBuyingValidateServletHolder(), "/groupBuyingValidate");
 				scHandler.addServletWithMapping(getGroupBuyingUnbindServletHolder(), "/groupBuyingUnbind");
 				// add handler to server
 				server.addHandler(scHandler);
 				server.getConnectors()[0].setPort(8086);
 				server.start();
 			}
 		} catch (Exception e) {
 			System.err.println("build test server failed");
 		}
 		//build test server end
 	}
 	
 	private ServletHolder getInitGrouponCacheServletHolder() throws Exception {
 		HardCodedServlet s = new HardCodedServlet();
 		s.init();
 		StringBuffer sb = new StringBuffer();
 		sb.append("<?xml version=\"1.0\" encoding=\"GBK\" ?>");
 		sb.append("<tuan>");
 		sb.append("<resultCode>0</resultCode>");
 		sb.append("<groupon>");
 		sb.append("<item>");
 		sb.append("<grouponId>132127</grouponId>");
 		sb.append("<grouponName>400.01元套餐POST</grouponName>");
 		sb.append("<mercName>三人行骨头王ggggg</mercName>");
 		sb.append("<listName>40元套餐\r\n        (132127)</listName>");
 		sb.append("<detailName>1111111111111140元套餐(132127)</detailName>");
 		sb.append("</item>");
 		sb.append("<item>");
 		sb.append("<grouponId>132123</grouponId>");
 		sb.append("<grouponName>400.01元套餐</grouponName>");
 		sb.append("<mercName>三人行骨头王</mercName>");
 		sb.append("<listName>400.01元套餐\r\n        (132123)</listName>");
 		sb.append("<detailName>400.01元套餐</detailName>");
 		sb.append("</item>");
 		sb.append("<item>");
 		sb.append("<grouponId>132154</grouponId>");
 		sb.append("<grouponName>测试商品</grouponName>");
 		sb.append("<mercName>测试商品王</mercName>");
 		sb.append("<listName>测试商品套餐\r\n        (132154)</listName>");
 		sb.append("<detailName>测试商品套餐</detailName>");
 		sb.append("</item>");
 		sb.append("</groupon>");
 		sb.append("</tuan>");
 		s.setResponse(new String(sb.toString().getBytes("utf-8"), "iso-8859-1"));
 		
 		ServletHolder h = new ServletHolder();
 		h.setServlet(s);
 
 		/*String servletPath = "/initGrouponCache";
 		ServletHandler scHandler = new ServletHandler();
 		scHandler.addServletWithMapping(h, servletPath);*/
 		return h;
 	}
 	
 	private ServletHolder getGroupBuyingValidateServletHolder() throws Exception {
 		HardCodedServlet s = new HardCodedServlet();
 		s.init();
 		StringBuffer sb = new StringBuffer();
 		sb.append("<?xml version=\"1.0\" encoding=\"GBK\" ?>");
 		sb.append("<tuan>");
 		sb.append("<resultCode>0</resultCode>");
 		sb.append("<groupon>");
 		sb.append("<resultName>验证已成功</resultName>");
 		sb.append("<resultExplain>验证成功于\r\n08.03 11:10:23</resultExplain>");
 		sb.append("<currentTime>2011-08-03 11:10:23</currentTime>");
 		sb.append("<useTime>2011-08-03 11:10:23</useTime>");
 		sb.append("<validTime>2011-08-10</validTime>");
 		sb.append("</groupon>");
 		sb.append("<groupon>");
 		sb.append("<resultName>已退款</resultName>");
 		sb.append("<resultExplain>验证已退款于\r\n08.03 11:10:23</resultExplain>");
 		sb.append("<currentTime>2011-08-03 11:10:23</currentTime>");
 		sb.append("<validTime>2011-08-10</validTime>");
 		sb.append("<refundTime>2011-08-03 11:10:23</refundTime>");
 		sb.append("</groupon>");
 		sb.append("</tuan>");
 		s.setResponse(new String(sb.toString().getBytes("utf-8"), "iso-8859-1"));
 		
 		ServletHolder h = new ServletHolder();
 		h.setServlet(s);
 
 		/*String servletPath = "/groupBuyingValidate";
 		ServletHandler scHandler = new ServletHandler();
 		scHandler.addServletWithMapping(h, servletPath);*/
 		return h;
 	}
 	
 	private ServletHolder getGroupBuyingUnbindServletHolder() throws Exception {
 		HardCodedServlet s = new HardCodedServlet();
 		s.init();
 		StringBuffer sb = new StringBuffer();
 		sb.append("<?xml version=\"1.0\" encoding=\"GBK\" ?>");
 		sb.append("<tuan>");
 		sb.append("<resultCode>0</resultCode>");
 		sb.append("<groupon>");
 		sb.append("<item>");
 		sb.append("<posId>1</posId>");
 		sb.append("<resultStatus>0</resultStatus>");
 		sb.append("</item>");
 		sb.append("<item>");
 		sb.append("<posId>2</posId>");
 		sb.append("<resultStatus>1</resultStatus>");
 		sb.append("</item>");
 		sb.append("</groupon>");
 		sb.append("</tuan>");
 		s.setResponse(new String(sb.toString().getBytes("utf-8"), "iso-8859-1"));
 		
 		ServletHolder h = new ServletHolder();
 		h.setServlet(s);
 
 		/*String servletPath = "/groupBuyingUnbind";
 		ServletHandler scHandler = new ServletHandler();
 		scHandler.addServletWithMapping(h, servletPath);*/
 		return h;
 	}
 
 	@Test
 	public void testInitGrouponCache() {
 		
 		List<String> grouponIdList = new ArrayList<String>();
 		grouponIdList.add("132127");
 		grouponIdList.add("132123");
 		grouponIdList.add("132154");
 		
 		GroupBuyingManager gbm = getInjector().getInstance(
 				GroupBuyingManager.class);
 		GroupBuyingDao dao = getInjector().getInstance(
 				GroupBuyingDao.class);
 		HashMap<String, String> params = new HashMap<String, String>();
 		String posId = "rewards-0001";
 		params.put("posId", posId);
 		params.put("key", new PosNetworkProperties().getTxServerKey());
 		System.out.println("key-->" + params.get("key"));
 		try {
 			String resultCode = gbm.initGrouponCache(params);
 			System.out.println("resultCode--> " + resultCode);
 			//search groupon cache
 			PageInfo<GrouponCache> pageInfo = new PageInfo();
 			pageInfo.setPageId(1);
 			pageInfo.setPageSize(50);
 			List<GrouponCache> cacheList = dao.getGrouponCachePagination(pageInfo, posId).getItems();
 			if (cacheList != null && cacheList.size() > 0) {
 				for (int i = 0; i < cacheList.size(); i++) {
 					System.out.println("GrouponId-->" + cacheList.get(i).getGrouponId());
 					System.out.println("GrouponName-->" + cacheList.get(i).getGrouponName());
 					System.out.println("MercName-->" + cacheList.get(i).getMercName());
 					System.out.println("ListName-->" + cacheList.get(i).getListName());
 					System.out.println("DetailName-->" + cacheList.get(i).getDetailName());
 					//验证排序是否正确
 					assertEquals(cacheList.get(i).getGrouponId(),grouponIdList.get(i));
 				}
 			}
 			//check cache
 			Query jql = emp.get().createQuery(
 					"select j.eventDetail from Journal j where j.event = '"
 							+ DomainEvent.GROUPON_CACHE_INIT.toString()
 							+ "' and j.entityId = ?1 order by j.ts desc");
 			jql.setParameter(1, posId);
 			String result = (String) jql.getResultList().get(0);
 			System.out.println("result--->" + result);
 			assertTrue(result.startsWith("["));
 		} catch (JsonGenerationException e) {
 			System.err.println("生成JSON对象出错");
 			e.printStackTrace();
 		} catch (MD5Exception e) {
 			System.err.println("生成MD5校验位出错");
 			e.printStackTrace();
 		} catch (ParseXMLException e) {
 			System.err.println("解析XML出错");
 			e.printStackTrace();
 		} catch (SendPostTimeOutException e) {
 			System.err.println("POST连接出错");
 			e.printStackTrace();
 		} catch (SaveDBException e) {
 			System.err.println("后台保存数据库出错");
 			System.out.println("具体异常信息：" + e.getMessage());
 			e.printStackTrace();
 		} catch (CopyPropertiesException e) {
 			System.err.println("复制属性值出错");
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testGroupBuyingSearch() {
 		//init test data start
 		if (!this.emp.get().getTransaction().isActive()) {
 			this.emp.get().getTransaction().begin();
 		}
 		GrouponCache gc1 = new GrouponCache();
 		gc1.setCreateDate(new Date());
 		gc1.setPosId("rewards-0001");
 		gc1.setGrouponId("grouponId1");
 		gc1.setGrouponName("grouponName1");
 		gc1.setListName("listName1");
 		gc1.setMercName("mercName1");
 		gc1.setDetailName("detailName1");
 		this.emp.get().persist(gc1);
 		GrouponCache gc2 = new GrouponCache();
 		gc2.setCreateDate(new Date());
 		gc2.setPosId("rewards-0001");
 		gc2.setGrouponId("grouponId2");
 		gc2.setGrouponName("grouponName2");
 		gc2.setListName("listName2");
 		gc2.setMercName("mercName2");
 		gc2.setDetailName("detailName2");
 		this.emp.get().persist(gc2);
 		GrouponCache gc3 = new GrouponCache();
 		gc3.setCreateDate(new Date());
 		gc3.setPosId("rewards-0001");
 		gc3.setGrouponId("grouponId3");
 		gc3.setGrouponName("grouponName3");
 		gc3.setListName("listName3");
 		gc3.setMercName("mercName3");
 		gc3.setDetailName("detailName3");
 		this.emp.get().persist(gc3);
 		//init test data end
 		
 		GroupBuyingManager gbm = getInjector().getInstance(
 				GroupBuyingManager.class);
 		HashMap<String, String> params = new HashMap<String, String>();
 		params.put("posId", "rewards-0001");
 		//params.put("key", "456789000");
 		params.put("curpage", "2");
 		params.put("pageSize", "1");
 		try {
 			HashMap<String,Object> resultMap = gbm.groupBuyingSearch(params);
 			String resultCode = (String) resultMap.get("resultCode");
 			System.out.println("resultCode->" + resultCode);
 			PageInfo<GrouponCache> pageInfo = (PageInfo<GrouponCache>) resultMap.get("pageInfo");
 			System.out.println("totalnum->" + pageInfo.getRecordCount());
 			if (pageInfo.getItems() != null) {
 				System.out.println("curnum->" + pageInfo.getItems().size());
 			} else {
 				System.out.println("curnum->" + 0);
 			}
 			System.out.println("curpage->" + pageInfo.getPageId());
 			System.out.println("totalpage->" + pageInfo.getPageCount());
 			if (pageInfo.getItems() != null && pageInfo.getItems().size() > 0) {
 				for (GrouponCache item : pageInfo.getItems()) {
 					System.out.println("GrouponId-->" + item.getGrouponId());
 					System.out.println("GrouponName-->" + item.getGrouponName());
 					System.out.println("MercName-->" + item.getMercName());
 					System.out.println("ListName-->" + item.getListName());
 					System.out.println("DetailName-->" + item.getDetailName());
 				}
 			}
 		} catch (JsonGenerationException e) {
 			System.err.println("生成JSON对象出错");
 			e.printStackTrace();
 		} catch (MD5Exception e) {
 			System.err.println("生成MD5校验位出错");
 			e.printStackTrace();
 		} catch (ParseXMLException e) {
 			System.err.println("解析XML出错");
 			e.printStackTrace();
 		} catch (SendPostTimeOutException e) {
 			System.err.println("POST连接出错");
 			e.printStackTrace();
 		} catch (SaveDBException e) {
 			System.err.println("后台保存数据库出错");
 			System.out.println("具体异常信息：" + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testGroupBuyingValidate() {
 		
 		//init test data start
 		if (!this.emp.get().getTransaction().isActive()) {
 			this.emp.get().getTransaction().begin();
 		}
 		Pos pos = new Pos();
 		pos.setPosId("rewards-0001");
 		pos.setModel("model");
 		pos.setSn("sn");
 		pos.setSimPhoneNo("simphoneno");
 		pos.setDstatus(PosDeliveryStatus.DELIVERED);
 		pos.setIstatus(PosInitializationStatus.INITED);
 		pos.setOstatus(PosOperationStatus.ALLOWED);
 		pos.setSecret("secret");
 		this.emp.get().persist(pos);
 		Agent agent = new Agent();
 		agent.setName("agent");
 		this.emp.get().persist(agent);
 		PosAssignment pa = new PosAssignment();
 		pa.setAgent(agent);
 		pa.setPos(pos);
 		this.emp.get().persist(pa);
 		//init test data end
 		
 		GroupBuyingManager gbm = getInjector().getInstance(
 				GroupBuyingManager.class);
 		HashMap<String, String> params = new HashMap<String, String>();
 		params.put("posId", "rewards-0001");
 		params.put("grouponId", "456789");
 		params.put("token", "4567890");
 		params.put("key", new PosNetworkProperties().getTxServerKey());
 		try {
 			HashMap<String, Object> result = gbm.groupBuyingValidate(params);
 			String resultCode = (String) result.get("resultCode");
 			System.out.println("resultCode->" + resultCode);
 			if ("0".equals(resultCode)) {
 				List<GroupBuyingValidateResultVO> items = (List<GroupBuyingValidateResultVO>) result
 						.get("items");
 				for (GroupBuyingValidateResultVO item : items) {
 					System.out.println(item.getResultName());
 					System.out.println(item.getResultExplain());
 					System.out.println(item.getCurrentTime());
 					System.out.println(item.getUseTime());
 					System.out.println(item.getValidTime());
 					System.out.println(item.getRefundTime());
 				}
 			} else {
 				switch (Integer.valueOf(resultCode)) {
 				case -1:
 					System.out.println("服务器繁忙");
 					break;
 				case -2:
 					System.out.println("md5校验失败");
 					break;
 				case -3:
 					System.out.println("没有权限");
 					break;
 				default:
 					System.out.println("未知错误");
 					break;
 				}
 			}
 		} catch (JsonGenerationException e) {
 			System.err.println("生成JSON对象出错");
 			e.printStackTrace();
 		} catch (MD5Exception e) {
 			System.err.println("生成MD5校验位出错");
 			e.printStackTrace();
 		} catch (ParseXMLException e) {
 			System.err.println("解析XML出错");
 			e.printStackTrace();
 		} catch (SendPostTimeOutException e) {
 			System.err.println("POST连接出错");
 			e.printStackTrace();
 		} catch (SaveDBException e) {
 			System.err.println("后台保存数据库出错");
 			System.out.println("具体异常信息：" + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testGroupBuyingUnbind() {
 		
 		//init test data start
 		if (!this.emp.get().getTransaction().isActive()) {
 			this.emp.get().getTransaction().begin();
 		}
 		Pos pos = new Pos();
 		pos.setPosId("rewards-0001");
 		pos.setModel("model");
 		pos.setSn("sn");
 		pos.setSimPhoneNo("simphoneno");
 		pos.setDstatus(PosDeliveryStatus.DELIVERED);
 		pos.setIstatus(PosInitializationStatus.INITED);
 		pos.setOstatus(PosOperationStatus.ALLOWED);
 		pos.setSecret("secret");
 		this.emp.get().persist(pos);
 		Pos pos2 = new Pos();
 		pos2.setPosId("rewards-0002");
 		pos2.setModel("model2");
 		pos2.setSn("sn2");
 		pos2.setSimPhoneNo("simphoneno2");
 		pos2.setDstatus(PosDeliveryStatus.DELIVERED);
 		pos2.setIstatus(PosInitializationStatus.INITED);
 		pos2.setOstatus(PosOperationStatus.ALLOWED);
 		pos2.setSecret("secret2");
 		this.emp.get().persist(pos2);
 		Agent agent = new Agent();
 		agent.setName("agent");
 		this.emp.get().persist(agent);
 		PosAssignment pa = new PosAssignment();
 		pa.setAgent(agent);
 		pa.setPos(pos);
 		this.emp.get().persist(pa);
 		PosAssignment pa2 = new PosAssignment();
 		pa2.setAgent(agent);
 		pa2.setPos(pos2);
 		this.emp.get().persist(pa2);
 		//init test data end
 		
 		GroupBuyingManager gbm = getInjector().getInstance(
 				GroupBuyingManager.class);
 		HashMap<String, Object> params = new HashMap<String, Object>();
 		params.put("posId", new String[] { "rewards-0001", "rewards-0002"});
 		params.put("key", new PosNetworkProperties().getTxServerKey());
 		try {
 			HashMap<String, Object> result = gbm.groupBuyingUnbind(params);
 			String resultCode = (String) result.get("resultCode");
 			System.out.println("resultCode->" + resultCode);
 			if ("0".equals(resultCode)) {
 				List<GroupBuyingUnbindVO> items = (List<GroupBuyingUnbindVO>) result
 						.get("items");
 				for (GroupBuyingUnbindVO item : items) {
 					System.out.println(item.getPosId());
 					System.out.println(item.getResultStatus());
 				}
 			} else {
 				switch (Integer.valueOf(resultCode)) {
 				case -1:
 					System.out.println("服务器繁忙");
 					break;
 				case -2:
 					System.out.println("md5校验失败");
 					break;
 				case -3:
 					System.out.println("没有权限");
 					break;
 				default:
 					System.out.println("未知错误");
 					break;
 				}
 			}
 		} catch (JsonGenerationException e) {
 			System.err.println("生成JSON对象出错");
 			e.printStackTrace();
 		} catch (MD5Exception e) {
 			System.err.println("生成MD5校验位出错");
 			e.printStackTrace();
 		} catch (ParseXMLException e) {
 			System.err.println("解析XML出错");
 			e.printStackTrace();
 		} catch (SendPostTimeOutException e) {
 			System.err.println("POST连接出错");
 			e.printStackTrace();
 		} catch (SaveDBException e) {
 			System.err.println("后台保存数据库出错");
 			System.out.println("具体异常信息：" + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void testSendPostSuccess() throws Exception {
 		Server server = new Server(0);
 
 		HardCodedServlet s = new HardCodedServlet();
 		s.init();
 		s.setResponse("correct url");
 		
 		ServletHolder h = new ServletHolder();
 		h.setServlet(s);
 
 		String servletPath = "/qqapi";
 		ServletHandler scHandler = new ServletHandler();
 		scHandler.addServletWithMapping(h, servletPath);
 		
 		// add handler to server
 		server.addHandler(scHandler);
 		server.start();
 		
 		String url = "http://localhost:" + server.getConnectors()[0].getLocalPort() + servletPath;
 		GroupBuyingUtil.sendPost(url, null);
 		/*String url = "http://tuan-layenlin.qq.com/api/pos/query";
 		HashMap<String, Object> params = new HashMap<String, Object>();
 		String posId = "REWARDS-0001";
 		params.put("posId", posId);
 		params.put("key", new PosNetworkProperties().getTxServerKey());
 		
 		GroupBuyingUtil.sendPost(url, params);*/
 	}
 
 	@Test
 	public void testSendPostTimeOut() throws Exception {
 		Server server = new Server(0);
 
 		HardCodedServlet s = new HardCodedServlet();
 		s.init();
 		s.setResponse("error url");
 		
 		ServletHolder h = new ServletHolder();
 		h.setServlet(s);
 
 		String servletPath = "/qqapi";
 		ServletHandler scHandler = new ServletHandler();
 		scHandler.addServletWithMapping(h, servletPath);
 		
 		// add handler to server
 		server.addHandler(scHandler);
 		server.start();
 		
 		String url = "http://errorurl:" + server.getConnectors()[0].getLocalPort() + servletPath;
 		
 		try {
 			GroupBuyingUtil.sendPost(url, null);
 		} catch (SendPostTimeOutException e) {
 			System.out.println("connection time out");
 			return;
 		}
 		throw new Exception();
 	}
 	
 	@Test
 	public void testErrorXML() throws Exception {
 		Server server = new Server(0);
 
 		HardCodedServlet s = new HardCodedServlet();
 		s.init();
 		StringBuffer sb = new StringBuffer();
 		sb.append("<?xml version=\"1.0\" encoding=\"GBK\" ?>");
 		sb.append("<tuan>");
 		sb.append("<resultCode>0</resultCode>");
 		sb.append("<groupon>");
 		sb.append("<item>");
 		sb.append("<grouponId>132127</grouponId>");
 		sb.append("<grouponName>400.01元套餐POST</grouponName>");
 		sb.append("<mercName>三人行骨头王</mercName>");
 		sb.append("<listName>40元套餐\r\n        (132127)</listName>");
 		sb.append("<detailName>1111111111111140元套餐(132127)</detailName>");
 		sb.append("</item>");
 		sb.append("<item>");
 		sb.append("<grouponId>132123</grouponId>");
 		sb.append("<grouponName>400.01元套餐</grouponName>");
 		sb.append("<mercName>三人行骨头王</mercName>");
 		sb.append("<listName>400.01元套餐\r\n        (132123)</listName>");
 		sb.append("<detailName>400.01元套餐</detailName>");
 		sb.append("</item>");
 		s.setResponse(new String(sb.toString().getBytes("utf-8"), "iso-8859-1"));
 		
 		ServletHolder h = new ServletHolder();
 		h.setServlet(s);
 
 		String servletPath = "/qqapi";
 		ServletHandler scHandler = new ServletHandler();
 		scHandler.addServletWithMapping(h, servletPath);
 		
 		// add handler to server
 		server.addHandler(scHandler);
 		server.start();
 		
 		String url = "http://localhost:" + server.getConnectors()[0].getLocalPort() + servletPath;
 		
 		try {
 			HashMap<String, Object> result = GroupBuyingUtil.parseXML(
 					GroupBuyingUtil.sendPost(url, null), "//groupon/item",
 					GroupBuyingSearchListVO.class);
 			String resultCode = (String) result.get("resultCode");
 			System.out.println("resultCode->" + resultCode);
 			if ("0".equals(resultCode)) {
 				List<GroupBuyingSearchListVO> items = (List<GroupBuyingSearchListVO>) result
 						.get("items");
 				for (GroupBuyingSearchListVO item : items) {
 					System.out.println(item.getGrouponId());
 					System.out.println(item.getGrouponName());
 					System.out.println(item.getMercName());
 					System.out.println(item.getListName());
 					System.out.println(item.getDetailName());
 				}
 			} else {
 				switch (Integer.valueOf(resultCode)) {
 				case -1:
 					System.out.println("服务器繁忙");
 					break;
 				case -2:
 					System.out.println("md5校验失败");
 					break;
 				case -3:
 					System.out.println("没有权限");
 					break;
 				default:
 					System.out.println("未知错误");
 					break;
 				}
 			}
 		} catch (ParseXMLException e) {
 			System.out.println("xml contents format error");
 			return;
 		}
 		throw new Exception();
 		
 	}
 	
 	@Test
 	public void testGroupBuyingSearchParseXML() throws Exception {
 		Server server = new Server(0);
 
 		HardCodedServlet s = new HardCodedServlet();
 		s.init();
 		StringBuffer sb = new StringBuffer();
 		sb.append("<?xml version=\"1.0\" encoding=\"GBK\" ?>");
 		sb.append("<tuan>");
 		sb.append("<resultCode>0</resultCode>");
 		sb.append("<groupon>");
 		sb.append("<item>");
 		sb.append("<grouponId>132127</grouponId>");
 		sb.append("<grouponName>400.01元套餐POST</grouponName>");
 		sb.append("<mercName>三人行骨头王</mercName>");
 		sb.append("<listName>40元套餐\r\n        (132127)</listName>");
 		sb.append("<detailName>1111111111111140元套餐(132127)</detailName>");
 		sb.append("</item>");
 		sb.append("<item>");
 		sb.append("<grouponId>132123</grouponId>");
 		sb.append("<grouponName>400.01元套餐</grouponName>");
 		sb.append("<mercName>三人行骨头王</mercName>");
 		sb.append("<listName>400.01元套餐\r\n        (132123)</listName>");
 		sb.append("<detailName>400.01元套餐</detailName>");
 		sb.append("</item>");
 		sb.append("</groupon>");
 		sb.append("</tuan>");
 		s.setResponse(new String(sb.toString().getBytes("utf-8"), "iso-8859-1"));
 		
 		ServletHolder h = new ServletHolder();
 		h.setServlet(s);
 
 		String servletPath = "/qqapi";
 		ServletHandler scHandler = new ServletHandler();
 		scHandler.addServletWithMapping(h, servletPath);
 		
 		// add handler to server
 		server.addHandler(scHandler);
 		server.start();
 		
 		String url = "http://localhost:" + server.getConnectors()[0].getLocalPort() + servletPath;
 		
 		HashMap<String, Object> result = GroupBuyingUtil.parseXML(
 				GroupBuyingUtil.sendPost(url, null), "//groupon/item",
 				GroupBuyingSearchListVO.class);
 		String resultCode = (String) result.get("resultCode");
 		System.out.println("resultCode->" + resultCode);
 		if ("0".equals(resultCode)) {
 			List<GroupBuyingSearchListVO> items = (List<GroupBuyingSearchListVO>) result
 					.get("items");
 			for (GroupBuyingSearchListVO item : items) {
 				System.out.println(item.getGrouponId());
 				System.out.println(item.getGrouponName());
 				System.out.println(item.getMercName());
 				System.out.println(item.getListName());
 				System.out.println(item.getDetailName());
 			}
 		} else {
 			switch (Integer.valueOf(resultCode)) {
 			case -1:
 				System.out.println("服务器繁忙");
 				break;
 			case -2:
 				System.out.println("md5校验失败");
 				break;
 			case -3:
 				System.out.println("没有权限");
 				break;
 			default:
 				System.out.println("未知错误");
 				break;
 			}
 		}
 	}
 	
 	@Test
 	public void testGroupBuyingValidateParseXML() throws Exception {
 		Server server = new Server(0);
 
 		HardCodedServlet s = new HardCodedServlet();
 		s.init();
 		StringBuffer sb = new StringBuffer();
 		sb.append("<?xml version=\"1.0\" encoding=\"GBK\" ?>");
 		sb.append("<tuan>");
 		sb.append("<resultCode>0</resultCode>");
 		sb.append("<groupon>");
 		sb.append("<resultName>验证已成功</resultName>");
 		sb.append("<resultExplain>验证成功于\r\n08.03 11:10:23</resultExplain>");
 		sb.append("<currentTime>2011-08-03 11:10:23</currentTime>");
 		sb.append("<useTime>2011-08-03 11:10:23</useTime>");
 		sb.append("<validTime>2011-08-10</validTime>");
 		sb.append("</groupon>");
 		sb.append("<groupon>");
 		sb.append("<resultName>已退款</resultName>");
 		sb.append("<resultExplain>验证已退款于\r\n08.03 11:10:23</resultExplain>");
 		sb.append("<currentTime>2011-08-03 11:10:23</currentTime>");
 		sb.append("<validTime>2011-08-10</validTime>");
 		sb.append("<refundTime>2011-08-03 11:10:23</refundTime>");
 		sb.append("</groupon>");
 		sb.append("</tuan>");
 		s.setResponse(new String(sb.toString().getBytes("utf-8"), "iso-8859-1"));
 		
 		ServletHolder h = new ServletHolder();
 		h.setServlet(s);
 
 		String servletPath = "/qqapi";
 		ServletHandler scHandler = new ServletHandler();
 		scHandler.addServletWithMapping(h, servletPath);
 		
 		// add handler to server
 		server.addHandler(scHandler);
 		//server.getConnectors()[0].setPort(8087);
 		server.start();
 		
 		System.out.println("LocalPort: " + server.getConnectors()[0].getLocalPort());
 		
 		String url = "http://localhost:" + server.getConnectors()[0].getLocalPort() + servletPath;
 		
 		HashMap<String, Object> result = GroupBuyingUtil.parseXML(
 				GroupBuyingUtil.sendPost(url, null), "//groupon",
 				GroupBuyingValidateResultVO.class);
 		String resultCode = (String) result.get("resultCode");
 		System.out.println("resultCode->" + resultCode);
 		if ("0".equals(resultCode)) {
 			List<GroupBuyingValidateResultVO> items = (List<GroupBuyingValidateResultVO>) result
 					.get("items");
 			for (GroupBuyingValidateResultVO item : items) {
 				System.out.println(item.getResultName());
 				System.out.println(item.getResultExplain());
 				System.out.println(item.getCurrentTime());
 				System.out.println(item.getUseTime());
 				System.out.println(item.getValidTime());
 				System.out.println(item.getRefundTime());
 			}
 		} else {
 			switch (Integer.valueOf(resultCode)) {
 			case -1:
 				System.out.println("服务器繁忙");
 				break;
 			case -2:
 				System.out.println("md5校验失败");
 				break;
 			case -3:
 				System.out.println("没有权限");
 				break;
 			default:
 				System.out.println("未知错误");
 				break;
 			}
 		}
 	}
 	
 	@Test
 	public void testGroupBuyingUnbindParseXML() throws Exception {
 		Server server = new Server(0);
 
 		HardCodedServlet s = new HardCodedServlet();
 		s.init();
 		StringBuffer sb = new StringBuffer();
 		sb.append("<?xml version=\"1.0\" encoding=\"GBK\" ?>");
 		sb.append("<tuan>");
 		sb.append("<resultCode>0</resultCode>");
 		sb.append("<groupon>");
 		sb.append("<item>");
 		sb.append("<posId>1</posId>");
 		sb.append("<resultStatus>0</resultStatus>");
 		sb.append("</item>");
 		sb.append("<item>");
 		sb.append("<posId>2</posId>");
 		sb.append("<resultStatus>1</resultStatus>");
 		sb.append("</item>");
 		sb.append("</groupon>");
 		sb.append("</tuan>");
 		s.setResponse(new String(sb.toString().getBytes("utf-8"), "iso-8859-1"));
 		
 		ServletHolder h = new ServletHolder();
 		h.setServlet(s);
 
 		String servletPath = "/qqapi";
 		ServletHandler scHandler = new ServletHandler();
 		scHandler.addServletWithMapping(h, servletPath);
 		
 		// add handler to server
 		server.addHandler(scHandler);
 		server.start();
 		
 		String url = "http://localhost:" + server.getConnectors()[0].getLocalPort() + servletPath;
 		
 		HashMap<String, Object> result = GroupBuyingUtil.parseXML(
 				GroupBuyingUtil.sendPost(url, null), "//groupon/item",
 				GroupBuyingUnbindVO.class);
 		String resultCode = (String) result.get("resultCode");
 		System.out.println("resultCode->" + resultCode);
 		if ("0".equals(resultCode)) {
 			List<GroupBuyingUnbindVO> items = (List<GroupBuyingUnbindVO>) result
 					.get("items");
 			for (GroupBuyingUnbindVO item : items) {
 				System.out.println(item.getPosId());
 				System.out.println(item.getResultStatus());
 			}
 		} else {
 			switch (Integer.valueOf(resultCode)) {
 			case -1:
 				System.out.println("服务器繁忙");
 				break;
 			case -2:
 				System.out.println("md5校验失败");
 				break;
 			case -3:
 				System.out.println("没有权限");
 				break;
 			default:
 				System.out.println("未知错误");
 				break;
 			}
 		}
 	}
 
 }
