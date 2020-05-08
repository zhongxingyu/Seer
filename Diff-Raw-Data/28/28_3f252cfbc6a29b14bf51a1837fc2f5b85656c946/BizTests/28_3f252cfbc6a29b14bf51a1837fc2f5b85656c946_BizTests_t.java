 package com.nbcedu.function.schoolmaster2.tests;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Test;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import com.google.gson.JsonObject;
 import com.nbcedu.function.schoolmaster2.biz.SM2MasterSubBiz;
 import com.nbcedu.function.schoolmaster2.biz.Sm2ProgressBiz;
 import com.nbcedu.function.schoolmaster2.core.pager.PagerModel;
 import com.nbcedu.function.schoolmaster2.data.model.TSm2Subject;
 import com.nbcedu.function.schoolmaster2.data.vo.ProgressVo;
 import com.nbcedu.function.schoolmaster2.vo.MasterSubSearchVO;
 import com.nbcedu.function.schoolmaster2.vo.SubjectZanVo;
 
 public class BizTests {
 	
 	static ApplicationContext context;
 	static{
 		context = new ClassPathXmlApplicationContext(new String[]{
 				"applicationContext.xml",
 				"com/nbcedu/function/schoolmaster2/config/spring-conf/actionContext.xml",
 				"com/nbcedu/function/schoolmaster2/config/spring-conf/bizContext.xml",
 				"com/nbcedu/function/schoolmaster2/config/spring-conf/daoContext.xml"});
 	}
 	@Test
 	public void masterSubBizTest(){
 		SM2MasterSubBiz biz = (SM2MasterSubBiz) context.getBean("masterSubBiz");
 		biz.findByMaster("guanzhu", "1");
 	}
 	
 	
 	@Test
 	public void progTest(){
 		Sm2ProgressBiz biz = (Sm2ProgressBiz)context.getBean("sm2ProgressBiz");
 		List<ProgressVo>result = biz.findVoByStepId("2");
 		for (ProgressVo progressVo : result) {
 			System.out.println(progressVo.getContent());
 		}
 		System.out.println(result.toString());
 	}
 	
 	
 	@Test
 	public void masterPmTest(){
 		SM2MasterSubBiz biz = (SM2MasterSubBiz) context.getBean("masterSubBiz");
 		MasterSubSearchVO vo = new MasterSubSearchVO();
 		//vo.setCreaterName("");
 		vo.setModuleId("guanzhu");
 		//vo.setReceiverUid("1");
 		PagerModel pm = biz.findBySearchVo(vo);
 		for (Object sub :pm.getDatas()) {
 			TSm2Subject su = (TSm2Subject)sub;
 			System.out.println(su.getTitle());
 		}
 	}
 	
 	public static void main(String[] args) {
 		SM2MasterSubBiz biz = (SM2MasterSubBiz) context.getBean("masterSubBiz");
 		List<TSm2Subject> list = biz.findByMsterModule(
 				"1",
 				new LinkedList<String>(){{
 					add("jinjizhongyao");
 					add("qingshibaopi");
 					add("zongjiehuibao");
 				}}
 				, 5);
 		for (TSm2Subject tSm2Subject : list) {
 			tSm2Subject.getId();
 		}
 	}
 	
 	@Test
 	public void findSubVoByProg(){
 		SM2MasterSubBiz biz = (SM2MasterSubBiz) context.getBean("masterSubBiz");
 		String progId = "1";
 		SubjectZanVo vo = biz.findByProgId(progId);
 		System.out.println(vo.getId());
 	}
 	
 	@Test
 	public void findCountByModule(){
 		SM2MasterSubBiz biz = (SM2MasterSubBiz) context.getBean("masterSubBiz");
 		Map<String, Integer> result = biz.findNewCountByModule("1");
 		for (Map.Entry<String, Integer> entry : result.entrySet()) {
 			System.out.println(entry.getKey() +":"+ entry.getValue());
 		}
 		Map<String, Integer> results = biz.findNewCountByModule("1");
 		JsonObject json = new JsonObject();
 		for (Map.Entry<String, Integer> entry : results.entrySet()) {
 			json.addProperty(entry.getKey(),entry.getValue());
 		}
 		System.out.println(json.toString());
 	}

	
	@Test
	public void findByMasterAndCountTest(){
		SM2MasterSubBiz biz = (SM2MasterSubBiz) context.getBean("masterSubBiz");
		biz.findByMasterAndCount("linshi", "1", 6);
	}
 }


