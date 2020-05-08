 /**
  * 
  */
 package com.k99k.testcenter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.k99k.khunter.Action;
 import com.k99k.khunter.ActionMsg;
 import com.k99k.khunter.DaoInterface;
 import com.k99k.khunter.DaoManager;
 import com.k99k.khunter.HttpActionMsg;
 import com.k99k.khunter.KFilter;
 import com.k99k.khunter.KObjManager;
 import com.k99k.khunter.KObjSchema;
 import com.k99k.khunter.KObject;
 import com.k99k.khunter.dao.StaticDao;
 import com.k99k.tools.JSON;
 import com.k99k.tools.StringUtil;
 
 /**
  * 测试项目
  * @author keel
  *
  */
 public class TestCase extends Action {
 
 	/**
 	 * @param name
 	 */
 	public TestCase(String name) {
 		super(name);
 	}
 	
 	static DaoInterface dao;
 	static KObjSchema schema;
 	
 	/**
 	 * 测试项缓存(内容为KObject),长度为50,下标为系统数字：0:java,1:android,2:WAP,3:brew,4:mobile,5:ce,6:other
 	 */
 	private static KObject[][] cases = new KObject[50][50];
 	/**
 	 * 缓存内容为Json String
 	 */
 	private static String[] casesJson = new String[50];
 	
 	/**
 	 * 所有系统的关键case数量
 	 */
 	private static int[] caseKeys= new int[50];
 	
 	/**
 	 * 初始化所有的TestCase并缓存起来
 	 * @see com.k99k.khunter.Action#init()
 	 */
 	@Override
 	public void init() {
 		dao = DaoManager.findDao("TCTestCaseDao");
 		schema = KObjManager.findSchema("TCTestCase");
 		
 		
 		ArrayList<KObject> cl = dao.queryKObj(StaticDao.prop_state_0, null,StaticDao.prop_id , 0, 0, null);
 		Iterator<KObject> it = cl.iterator();
 		int ks=0;
 		while (it.hasNext()) {
 			KObject kobj = it.next();
 			//type表示系统：0:java,1:android,2:WAP,3:brew,4:mobile,5:ce,6:other
 			int type = kobj.getType();
 			if (cases[type] == null) {
 				caseKeys[type] = ks;
				cases[type] = new KObject[100];
 				ks=0;
 			}
 			if (kobj.getLevel()>=10) {
 				ks++;
 			}
 			//caseId作为index插入cases
 			int index = Integer.parseInt(kobj.getProp("caseId").toString());
 			cases[type][index]=kobj;
 		}
 		
 		for (int i = 0; i < cases.length; i++) {
 			if (cases[i] != null) {
 				ArrayList<HashMap<String,Object>> tmp = new ArrayList<HashMap<String,Object>>();
 				for (int j = 0; j < cases[i].length; j++) {
 					if(cases[i][j]!=null){
 						tmp.add(cases[i][j].getPropMap());
 					}
 					
 				}
 				casesJson[i] = JSON.write(tmp);
 			}
 		}
 		
 		
 		super.init();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
 	 */
 	@Override
 	public ActionMsg act(ActionMsg msg) {
 		HttpActionMsg httpmsg = (HttpActionMsg)msg;
 		HttpServletRequest req = httpmsg.getHttpReq();
 		String subact = KFilter.actPath(msg, 2, "");
 		KObject u = Auth.checkCookieLogin(httpmsg);
 		if (u == null) {
 			msg.addData("[redirect]", "/login");
 			return super.act(msg);
 		}
 		if (StringUtil.isDigits(subact)) {
 			this.list(subact, req, u, httpmsg);
 		}
 		
 		return super.act(msg);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.k99k.khunter.Action#exit()
 	 */
 	@Override
 	public void exit() {
 		cases = new KObject[50][100];
 		super.exit();
 	}
 
 	/**
 	 * 根据系统查看TestCase列表,返回json形式的列表
 	 * @param msg
 	 */
 	private void list(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
 		int sys = Integer.parseInt(subact);
 		if (cases.length<=sys || cases[sys] == null) {
 			msg.addData("[print]", "");
 			return;
 		}
 		msg.addData("[print]", casesJson[sys]);
 	}
 	
 	/**
 	 * 验证并生成可保存到TestUnit的testCase数据,同时根据关键的case给出测试结果(state of testUnit),放在返回list的第一个
 	 * @param sys
 	 * @param json
 	 * @return
 	 */
 	static ArrayList<HashMap<String,Object>> checkJson(int sys,ArrayList<HashMap<String,Object>> json){
 		ArrayList<HashMap<String,Object>> ls = new ArrayList<HashMap<String,Object>>();
 		if (cases.length<=sys || cases[sys] == null) {
 			return null;
 		}
 		KObject[] ca = cases[sys];
 		Iterator<HashMap<String,Object>> it = json.iterator();
 		int i=0;
 		//是否通过的结果,放在ls的第一个
 		HashMap<String,Object> endRE = new HashMap<String, Object>();
 		int end = 0;
 		boolean hasEnd=false;
 		ls.add(0,endRE);
 		while (it.hasNext()) {
 			HashMap<java.lang.String, java.lang.Object> jm = it.next();
 			if (jm == null) {
 				i++;continue;
 			}
 			KObject aCase = ca[i];
 			if (aCase==null) {
 				return null;
 			}
 			if (StringUtil.isDigits(jm.get("re"))) {
 				int re = Integer.parseInt(jm.get("re").toString());
 				//判断是否通过标准为关键点必须全部通过
 				if (!hasEnd) {
 //					//如果关键点不为通过则直接认为不通过
 //					if (aCase.getLevel()>=10 && re != 2) {
 //						end=9;
 //						hasEnd=true;
 //					}
 //					//次关键点要求部分通过
 //					else if(aCase.getLevel()>=5){
 //						if (re > 4 || re==0) {
 //							end=4;
 //						}
 //					}
 //					if (re>=end) {
 //						end=re;
 //					}
 					switch (re) {
 					case 9:
 						//如果关键点不为通过则直接认为不通过
 						if (aCase.getLevel()>=5) {
 							end=9;
 							hasEnd=true;
 							break;
 						}
 						break;
 					case 4:
 						//次关键点要求部分通过
 						if (aCase.getLevel()>=5) {
 							end=4;
 						}else if (re>=end) {
 							end=re;
 						}
 						break;
 					//case 2:
 					//case 0:
 					default:
 						if (re>=end) {
 							end=re;
 						}
 					}
 				}
 				ls.add(jm);
 			}else{
 				return null;
 			}
 			i++;
 		}
 //		if (i<caseKeys[sys]) {
 //			//关键case数量不够
 //		}
 		//是否通过结果
 		endRE.put("re", end);
 		return ls;
 	}
 	
 	/**
 	 * 根据系统获取case列表
 	 * @param sys
 	 * @return
 	 */
 	public static KObject[] findCaseList(int sys){
 		if (cases.length<=sys || cases[sys] == null) {
 			return null;
 		}
 		return cases[sys];
 	}
 	
 	/**
 	 * 查找某个case
 	 * @param sys
 	 * @param caseId
 	 * @return
 	 */
 	public static HashMap<String,Object> findCase(int sys,int caseId){
 		if (cases.length<=sys || cases[sys] == null || cases[sys][caseId] == null) {
 			return null;
 		}
 		KObject kobj = cases[sys][caseId];
 		HashMap<String,Object> m = new HashMap<String, Object>(8);
 		m.put("caseId", kobj.getProp("caseId"));
 		m.put("name", kobj.getName());
 		m.put("level",kobj.getLevel());
 		m.put("info", kobj.getInfo());
 		return m;
 	}
 	
 }
