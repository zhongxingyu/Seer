 package com.lorent.web.xmlrpc.handler;
 
 import java.net.URL;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.apache.xmlrpc.client.XmlRpcClient;
 import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Restrictions;
 
 import com.lorent.common.dto.LCMConferenceDto;
 import com.lorent.common.dto.LCMConferenceRoleBean;
 import com.lorent.common.dto.LCMConferenceTypeBean;
 import com.lorent.common.dto.LCMMobileBean;
 import com.lorent.common.util.PasswordUtil;
 import com.lorent.common.util.SMSUtil;
 import com.lorent.exception.RpcServerException;
 import com.lorent.model.ConferenceBean;
 import com.lorent.model.ConferenceNewBean;
 import com.lorent.model.ConferenceRoleBean;
 import com.lorent.model.ConferenceTypeBean;
 import com.lorent.model.CustomerBean;
 import com.lorent.model.McuMixerBean;
 import com.lorent.model.McuServerBean;
 import com.lorent.model.MonitorNetBean;
 import com.lorent.model.UserBean;
 import com.lorent.trigger.McuRestoreTrigger;
 import com.lorent.trigger.QuartzTrigger;
 import com.lorent.util.CSUtil;
 import com.lorent.util.Constant;
 import com.lorent.util.MailUtil;
 import com.lorent.util.McuUtil;
 import com.lorent.util.PropertiesUtil;
 import com.lorent.util.StringUtil;
 import com.lorent.xmlrpc.McuXmlrpc;
 
 
 
 public class ConferenecHandler extends BaseHandler {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private static Logger log = Logger.getLogger(ConferenecHandler.class);
 	
 	/**
 	 * 在mcu重启时将lcm上面相应的会议恢复到mcu上
 	 * @param mcuUrl
 	 */
 	public boolean restoreConference(String mcuIp,String validateCode)throws Exception {
 		System.out.println("============================= rpc restore conferences");
 		serviceFacade.getConferenceService().restoreConferenceToMcu(mcuIp);
 		return true;
 	}
 
 	
 	
 	public String[] test(String str, Object[] strs)throws Exception {
 		System.out.println("============================= rpc test");
 		String[] objs = new String[]{str, (String)strs[0]};
 		
 //		return str + ":" + (String)strs[0];
 		return objs;
 	}
 	
 	public String test()throws Exception{
 		List<MonitorNetBean> list = serviceFacade.getMonitorService().getAllNetData();
 		for(MonitorNetBean item : list){
 			String ipAddr = item.getIpAddr();
 			ipAddr = new String(ipAddr.getBytes("ISO-8859-1"),"GBK");
 			System.out.println(item.getId() + "&" + ipAddr + "&" + item.getChannelid() + "&" + item.getPort());
 		}
 		return "";
 	}
 	
 	/**
 	 * 添加将mixer和conference恢复到mcu上的任务
 	 *
 	 */
 	public boolean addRestoreTask(String mcuIP,String validateCode)throws Exception {
 		McuServerBean server = new McuServerBean();
 		server.setServerIp(mcuIP);
 		List<McuServerBean>servers = serviceFacade.getMcuServerService().getByExample(server);
 		if(servers==null||servers.size()==0)
 			return false;
 		server = servers.get(0);
 		QuartzTrigger.addTask(new McuRestoreTrigger(server), serviceFacade);
 		return true;
 	}
 	
 	public Object[] getLccMonitor(){
 		String sql = "select cmsip, cmsport, username, password, nodename, channelno, isaudio, volume, autoconnect, id, nickname from lcc_monitor order by id";
 		List list = serviceFacade.getStaticService().getDaoFacade().getStaticDao().queryBySql(sql);
 		return list.toArray();
 	}
 	
 	public String sendLccMonitor(String monitorid, String lcc) throws Exception{
 		//get Lcc ip
 		String sql = "select ipaddr from sip_conf where name = '" + lcc + "'";
 		List list = serviceFacade.getStaticService().getDaoFacade().getStaticDao().queryBySql(sql);	
 		String lccip = (String)list.get(0);
 		//get monitor data
 		sql = "select cmsip, cmsport, username, password, nodename, channelno, isaudio, volume, autoconnect, id, nickname from lcc_monitor where id = '" + monitorid + "' ";
 		list = serviceFacade.getStaticService().getDaoFacade().getStaticDao().queryBySql(sql);
 		//send data
 		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
 		config.setServerURL(new URL("http://" + lccip + ":8090"));
 		XmlRpcClient client = new XmlRpcClient();
 		client.setConfig(config);
 		System.out.println(client.execute("VideoShare", (Object[])list.get(0)));
 		
 		return "success";
 	}
 	
 	public String sendConfMonitor(String confno, String lcc, String monitorid) throws Exception{
 		//get confUid
 		ConferenceBean conf = new ConferenceBean();
 		conf.setConfno(confno);
 		conf = serviceFacade.getConferenceService().getByExample(conf).get(0);
 		String confuid = conf.getConfUID();
 		//get lcc in conf
 		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
 		config.setServerURL(new URL(conf.getCustomer().getMcuServer().getServerUrl()));
 		XmlRpcClient client = new XmlRpcClient();
 		client.setConfig(config);
 		Object[] lccs = (Object[])client.execute("ConfMgr.getConfParticipant", new Object[]{confuid});
 		//send data
 		for(Object o : lccs){
 			if(!lcc.equals((String)o)){
 				sendLccMonitor(monitorid, (String)o);
 			}
 		}
 		return "success";
 	}
 	
 	public Object[] getLCCNosInConf(String confno)throws Exception{
 		//get confUid
 		ConferenceBean conf = new ConferenceBean();
 		conf.setConfno(confno);
 		conf = serviceFacade.getConferenceService().getByExample(conf).get(0);
 		String confuid = conf.getConfUID();
 		//get lcc in conf
 		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
 		config.setServerURL(new URL(conf.getCustomer().getMcuServer().getServerUrl()));
 		XmlRpcClient client = new XmlRpcClient();
 		client.setConfig(config);
 		Object[] lccnos = (Object[])client.execute("ConfMgr.getConfParticipant", new Object[]{confuid});
 		return lccnos;
 	}
 	
 	public String createConf(Integer pUserId, Object[] pJoinUsers)throws Exception{
 		//get user
 		UserBean user = serviceFacade.getUserService().get(pUserId);
 		//get join users
 		Set<UserBean> joinUsers = new HashSet<UserBean>();
 		joinUsers.add(user);
 		for(Object o : pJoinUsers){
 			joinUsers.add(serviceFacade.getUserService().get((Integer)o));
 		}
 		//create conf bean
 		ConferenceBean conf = new ConferenceBean();
 		conf.setCustomer(user.getCustomer());
 		serviceFacade.getConferenceService().createConfNo(conf, user.getCustomer().getCustomerCode(), Constant.CONF_TYPE_IMDCONF);
 		conf.setOwner(user);
 		conf.setConfSubject(serviceFacade.getConferenceService().getDefaultConfSubject(user.getUsername()));
 		conf.setConfType(Constant.CONF_TYPE_IMDCONF);		
 		Calendar ca = Calendar.getInstance();
 		conf.setStartTime(ca.getTime());
 		ca.add(Calendar.MINUTE, 30);
 		conf.setEndTime(ca.getTime());
 		conf.setConfPublic(Constant.YES);
 		conf.setMcuMediaLayOut(getMediaoLayoutByPeopleNum(joinUsers.size()));
 		conf.setMcuMediaQuality(Constant.MCU_QUALITYS.keySet().iterator().next());
 		conf.setMcuMixerKey(user.getCustomer().getMcuServer().getMixers().iterator().next().getMixerKey());
 		conf.setUsers(joinUsers);
 		//create conf
 		int confId = serviceFacade.getConferenceService().createConf(conf);
 		return conf.getConfno();
 	}
 	
 	private Integer getMediaoLayoutByPeopleNum(int num){
 		if(num >= 5){
 			return 2;
 		}else{
 			return 1;
 		}
 	}
 	
 //	//--------------------------------------供IP调度台是用（带业务逻辑）begin--------------------------------------
 //	
 //	public int createConf2(String pOwner, Object[] pJoinUsers, int pConfLayout, String pConfQuality)throws Exception{
 //		try{
 //			//get user
 //			UserBean user = serviceFacade.getUserService().getByLccAccount(pOwner);
 //			//get join users
 //			Set<UserBean> joinUsers = new HashSet<UserBean>();
 //			joinUsers.add(user);
 //			for(Object o : pJoinUsers){
 //				UserBean temp = serviceFacade.getUserService().getByLccAccount((String)o);
 //				joinUsers.add(temp);
 //			}
 //			//create conf bean
 //			ConferenceBean conf = new ConferenceBean();
 //			conf.setCustomer(user.getCustomer());
 //			String confno = serviceFacade.getConferenceService().createConfNo(user.getCustomer().getCustomerCode(), Constant.CONF_TYPE_MEETING);
 //			conf.setOwner(user);
 //			conf.setConfno(confno);
 //			conf.setConfSubject(serviceFacade.getConferenceService().getDefaultConfSubject(user.getUsername()));
 //			conf.setConfType(Constant.CONF_TYPE_MEETING);		
 //			Calendar ca = Calendar.getInstance();
 //			conf.setStartTime(ca.getTime());
 ////			ca.add(Calendar.MINUTE, 60);
 ////			conf.setEndTime(ca.getTime());
 //			conf.setConfPublic(Constant.YES);
 //			conf.setMcuMediaLayOut(pConfLayout);
 //			conf.setMcuMediaQuality(pConfQuality);
 //			conf.setMcuMixerKey(user.getCustomer().getMcuServer().getMixers().iterator().next().getMixerKey());
 //			conf.setUsers(joinUsers);
 //			//create conf
 //			int confId = serviceFacade.getConferenceService().createConfMeeting(conf);
 //			System.out.println("================" + confId);
 //			return confId;
 //		}catch(Exception e){
 //			e.printStackTrace();
 //			throw e;
 //		}
 //	}
 //	
 //	public String removeConf2(int confId)throws Exception{
 //		try {
 //			ConferenceBean conf = serviceFacade.getConferenceService().get(confId);
 //			serviceFacade.getConferenceService().removeConfMeeting(conf);
 ////			serviceFacade.getConferenceService().removeConference(confId);
 //		} catch (Exception e) {
 //			e.printStackTrace();
 //			throw e;
 //		}
 //		return "";
 //	}
 //
 //	
 //	public boolean updateConf2(int confId, int layout)throws Exception{
 //		ConferenceBean conf = serviceFacade.getConferenceService().get(confId);
 //		conf.setMcuMediaLayOut(layout);
 ////		serviceFacade.getConferenceService().update(conf);
 //		McuUtil.updateMediaLayout(conf);
 //		return true;
 //	}
 //	
 //	public boolean addConfUser2(int confId, Object[] lccnos)throws Exception{
 //		ConferenceBean conf = serviceFacade.getConferenceService().get(confId);
 //		conf.setUsers(new HashSet<UserBean>());
 //		
 //		String[] temp = new String[lccnos.length];
 //		temp = StrUtil.parseObjectArrayToList(lccnos).toArray(temp);
 //		
 //		for(String lccno : temp){
 //			UserBean user = serviceFacade.getUserService().getByLccAccount(lccno);
 //			conf.getUsers().add(user);
 //		}
 ////		serviceFacade.getConferenceService().update(conf);
 //		McuUtil.click_to_dial(conf);
 //		return true;
 //	}
 //	
 //	public boolean removeConfUser2(int confId, Object[] lccnos)throws Exception{
 //		ConferenceBean conf = serviceFacade.getConferenceService().get(confId);
 //		conf.setUsers(new HashSet<UserBean>());
 //		
 //		String[] temp = new String[lccnos.length];
 //		temp = StrUtil.parseObjectArrayToList(lccnos).toArray(temp);
 //		
 //		for(String lccno : temp){
 //			UserBean user = serviceFacade.getUserService().getByLccAccount((String)lccno);
 //			conf.getUsers().add(user);
 //		}
 ////		serviceFacade.getConferenceService().update(conf);
 //		McuUtil.removeConfUser(conf, temp);
 //		return true;
 //	}
 //	
 //	public boolean setConfUserVideo2(int confId, Object[] lccnos, int open)throws Exception{
 //		boolean t = false;
 //		if(open == 1){
 //			t = true;
 //		}
 //		ConferenceBean conf = serviceFacade.getConferenceService().get(confId);
 //		
 //		String[] temp = new String[lccnos.length];
 //		temp = StrUtil.parseObjectArrayToList(lccnos).toArray(temp);
 //		
 //		McuUtil.setConfUserVideo(conf, temp, t);
 //		return true;
 //	}
 //	
 //	public boolean setConfUserAudio2(int confId, Object[] lccnos, int open)throws Exception{
 //		boolean t = false;
 //		if(open == 1){
 //			t = true;
 //		}
 //		ConferenceBean conf = serviceFacade.getConferenceService().get(confId);
 //		
 //		String[] temp = new String[lccnos.length];
 //		temp = StrUtil.parseObjectArrayToList(lccnos).toArray(temp);
 //		
 //		McuUtil.setConfUserAudio(conf, temp, t);
 //		return true;
 //	}
 //	
 //	//--------------------------------------供IP调度台是用（带业务逻辑）end--------------------------------------
 //	
 //	
 //	//--------------------------------------供IP调度台是用(不带业务逻辑）begin--------------------------------------
 	
 	public int createConf(String pOwner, Object[] pJoinUsers, int pConfLayout, String pConfQuality)throws Exception{
 		
 		try{
 			UserBean user = serviceFacade.getUserService().getByLccAccount(pOwner);
 			//create conf bean
 			ConferenceBean conf = new ConferenceBean();
 			conf.setCustomer(user.getCustomer());
 //			String confno = serviceFacade.getConferenceService().createConfNo(user.getCustomer().getCustomerCode(), Constant.CONF_TYPE_OTHER);
 			serviceFacade.getConferenceService().createConfNo(conf, user.getCustomer().getCustomerCode(), Constant.CONF_TYPE_OTHER);
 			
 			conf.setOwner(user);
 //			conf.setConfno(confno);
 			conf.setConfSubject(serviceFacade.getConferenceService().getDefaultConfSubject(user.getUsername()));
 			conf.setConfType(Constant.CONF_TYPE_OTHER);		
 			Calendar ca = Calendar.getInstance();
 			conf.setStartTime(ca.getTime());
 //			ca.add(Calendar.MINUTE, 60);
 //			conf.setEndTime(ca.getTime());
 			conf.setConfPublic(Constant.YES);
 			conf.setMcuMediaLayOut(pConfLayout);
 			conf.setMcuMediaQuality(pConfQuality);
 //			conf.setMcuMixerKey(user.getCustomer().getMcuServer().getMixers().iterator().next().getMixerKey());
 			conf.setConfStatus(Constant.CONF_STATUS_ONGOING);
 			conf.setStatus(Constant.RECORD_STATUS_VALID);
 			
 			McuUtil.createConference(conf);
 //			String confUID = McuUtil.createConferenceToMcu(conf);
 //			conf.setConfUID(confUID);
 			ConferenceBean confAddUser = new ConferenceBean();
 			confAddUser.setConfno(conf.getConfno());
 			confAddUser.setCustomer(conf.getCustomer());
 			Set<UserBean> users = new HashSet<UserBean>();
 			String[] temp = new String[pJoinUsers.length + 1];
 			temp[0] = pOwner;
 			UserBean userBean0 = new UserBean();
 			userBean0.setLccAccount(temp[0]);
 			userBean0.setUsername(temp[0]);
 			users.add(userBean0);
 			for(int i = 0; i < pJoinUsers.length; i++){
 				temp[i] = (String)pJoinUsers[i];
 				UserBean userBean = new UserBean();
 				userBean.setLccAccount(temp[i]);
 				userBean.setUsername(temp[i]);
 				users.add(userBean);
 			}
 			confAddUser.setUsers(users);
 //			McuUtil.addUserInConf(confno, temp, user.getCustomer().getMcuServer().getServerIp());
 			CSUtil.addUserInConf(confAddUser);
 //			int confId = serviceFacade.getConferenceService().save(conf);
 			int confId = serviceFacade.getConferenceService().createNoBusinessConf(conf);
 			return confId;
 		}catch(Exception e){
 			e.printStackTrace();
 			throw e;
 		}
 		
 		
 	}
 	
 	public String removeConf(int confId)throws Exception{
 		return serviceFacade.getConferenceService().removeNoBusinessConf(confId);
 	}
 	
 	public boolean updateConf(int confId, int layout)throws Exception{
 		return serviceFacade.getConferenceService().updateConf(confId, layout);
 	}
 	
 	public boolean addConfUser(int confId, Object[] lccnos)throws Exception{
 		ConferenceBean conf = serviceFacade.getConferenceService().get(confId);
 		
 //		String[] temp = new String[lccnos.length];
 //		temp = StrUtil.parseObjectArrayToList(lccnos).toArray(temp);
 
 //		McuUtil.addUserInConf(conf.getConfno(), temp, conf.getCustomer().getMcuServer().getServerIp());
 		ConferenceBean conferenceBean = new ConferenceBean();
 		conferenceBean.setCustomer(conf.getCustomer());
 		conferenceBean.setConfno(conf.getConfno());
 		Set<UserBean> users = new HashSet<UserBean>();
 		for (int i = 0; i < lccnos.length; i++) {
 			UserBean userBean  = new UserBean();
 			userBean.setLccAccount((String)lccnos[i]);
 			userBean.setUsername((String)lccnos[i]);
 			users.add(userBean);
 		}
 		conferenceBean.setUsers(users);
 		CSUtil.addUserInConf(conferenceBean);
 		return true;
 	}
 	
 	public boolean removeConfUser(int confId, Object[] lccnos)throws Exception{
 		ConferenceBean conf = serviceFacade.getConferenceService().get(confId);
 		
 //		ConferenceBean conferenceBean = new ConferenceBean();
 //		conferenceBean.setCustomer(conf.getCustomer());
 //		conferenceBean.setConfno(conf.getConfno());
 //		Set<UserBean> users = new HashSet<UserBean>();
 //		for (int i = 0; i < lccnos.length; i++) {
 //			UserBean userBean  = new UserBean();
 //			userBean.setLccAccount((String)lccnos[i]);
 //			userBean.setUsername((String)lccnos[i]);
 //			users.add(userBean);
 //		}
 //		conferenceBean.setUsers(users);
 		McuUtil.removeConfUser(conf, StringUtil.parseObjectArrayToArray(lccnos, String.class));
 //		CSUtil.removeUserInConf(conferenceBean);
 		return true;
 	}
 	
 	public boolean setConfUserVideo(int confId, Object[] lccnos, int open)throws Exception{
 //		boolean t = false;
 //		if(open == 1){
 //			t = true;
 //		}
 		ConferenceBean conf = serviceFacade.getConferenceService().get(confId);
 		String[] temp = new String[lccnos.length];
 		temp = StringUtil.parseObjectArrayToList(lccnos).toArray(temp);
 		McuUtil.setConfUserVideo(conf, temp, open);
 		return true;
 	}
 	
 	public boolean setConfUserAudio(int confId, Object[] lccnos, int open)throws Exception{
 //		boolean t = false;
 //		if(open == 1){
 //			t = true;
 //		}
 		ConferenceBean conf = serviceFacade.getConferenceService().get(confId);
 		String[] temp = new String[lccnos.length];
 		temp = StringUtil.parseObjectArrayToList(lccnos).toArray(temp);
 		McuUtil.setConfUserAudio(conf, temp, open);
 		return true;
 	}
 	
 	public boolean listenExtenSpy(String lccno,String lccnotarget) throws Exception{
 		UserBean user = serviceFacade.getUserService().getByLccAccount(lccno);
 		UserBean targetuser = serviceFacade.getUserService().getByLccAccount(lccnotarget);
 		McuServerBean mcuServer = user.getCustomer().getMcuServer();
 		CSUtil.listenExtenSpy(mcuServer, user, targetuser);
 		return true;
 	}
 	
 	public boolean joinExtenSpy(String lccno,String lccnotarget) throws Exception{
 		UserBean user = serviceFacade.getUserService().getByLccAccount(lccno);
 		UserBean targetuser = serviceFacade.getUserService().getByLccAccount(lccnotarget);
 		McuServerBean mcuServer = user.getCustomer().getMcuServer();
 		CSUtil.joinExtenSpy(mcuServer, user, targetuser);
 		return true;
 	}
 	
 	public boolean call(String lccno,String lccnotarget) throws Exception{
 		UserBean user = serviceFacade.getUserService().getByLccAccount(lccno);
 		UserBean targetuser = serviceFacade.getUserService().getByLccAccount(lccnotarget);
 		McuServerBean mcuServer = user.getCustomer().getMcuServer();
 		CSUtil.call(mcuServer, user, targetuser);
 		return true;
 	}
 	
 	public boolean forceDisconnect(String lccno) throws Exception{
 		UserBean user = serviceFacade.getUserService().getByLccAccount(lccno);
 		McuServerBean mcuServer = user.getCustomer().getMcuServer();
 		CSUtil.forceDisconnect(mcuServer, user);
 		return true;
 	}
 	
 	public boolean switchAnswer(String lccno,String lccnotarget) throws Exception{
 		UserBean user = serviceFacade.getUserService().getByLccAccount(lccno);
 		UserBean targetuser = serviceFacade.getUserService().getByLccAccount(lccnotarget);
 		McuServerBean mcuServer = user.getCustomer().getMcuServer();
 		CSUtil.switchAnswer(mcuServer, user, targetuser);
 		return true;
 	}
 	
 	public int getPeerStatus(String lccno) throws Exception{
 		UserBean user = serviceFacade.getUserService().getByLccAccount(lccno);
 		McuServerBean mcuServer = user.getCustomer().getMcuServer();
 		return CSUtil.getPeerStatus(mcuServer, user);
 	}
 	//查询可用的会议号码
 	public Map<String,String> getConfnoList(String constant){
 		Map<String,String> map=null;
 		try {
 			 map=serviceFacade.getConferenceNewService().getAllConferences();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return map;
 	}
 //	
 //	//--------------------------------------供IP调度台是用(不带业务逻辑）end--------------------------------------
 	
 	//--------------------------------------供 VOVO 调用(不带业务逻辑)--------------------------------------
 	
 	//获得会议相关选项
 	//conference_role:list(id,role_name)
 	public Map<String, Object> getConfDefaultOptions() throws Exception{
 		HashMap<String, Object> result = new HashMap<String, Object>();
 		List conferenceRoles = serviceFacade.getConferenceNewService().getConferenceRoles();
 		List conferenceTypes = serviceFacade.getConferenceNewService().getConferenceTypes();
 		
 		HashMap<Integer, LCMConferenceTypeBean> confTypesList = new HashMap<Integer, LCMConferenceTypeBean>();
 		for (Object object : conferenceTypes) {
 			ConferenceTypeBean bean = (ConferenceTypeBean)object;
 			LCMConferenceTypeBean lcmConferenceTypeBean = new LCMConferenceTypeBean();
 			lcmConferenceTypeBean.setId(bean.getId());
 			lcmConferenceTypeBean.setStatus(bean.getStatus());
 			lcmConferenceTypeBean.setTypeName(bean.getTypeName());
 			lcmConferenceTypeBean.setDel(bean.getDel());
 			confTypesList.put(lcmConferenceTypeBean.getId(), lcmConferenceTypeBean);
 		}
 		result.put("conference_type", confTypesList);
 		
 		HashMap<Integer, LCMConferenceRoleBean> confRoleList = new HashMap<Integer, LCMConferenceRoleBean>();
 		for (Object object : conferenceRoles) {
 			ConferenceRoleBean bean = (ConferenceRoleBean)object;
 			LCMConferenceRoleBean lcmConferenceRoleBean = new LCMConferenceRoleBean();
 			lcmConferenceRoleBean.setId(bean.getId());
 			lcmConferenceRoleBean.setStatus(bean.getStatus());
 			lcmConferenceRoleBean.setRoleName(bean.getRoleName());
 			lcmConferenceRoleBean.setDel(bean.getDel());
 			confRoleList.put(lcmConferenceRoleBean.getId(), lcmConferenceRoleBean);
 		}
 		result.put("conference_role", confRoleList);
 		
 		return result;
 	}
 	
 	//获得会议列表和相关属性
 	public Map<String, LCMConferenceDto> getConfList() throws Exception{
 		Map<String, LCMConferenceDto> allConferencesDto = serviceFacade.getConferenceNewService().getAllConferencesDto();
 		return allConferencesDto;
 	}
 	
 	//修改新会议(vovo)
 	public boolean modifyConference(Integer id,String lccno,String confNo,String confName,String confPsw,Integer defaultRoleId,Integer confTypeId,Integer needApply,String topic,String description,Object[] urIds) throws Exception{
 		ConferenceNewBean conferenceNewPo = new ConferenceNewBean();
 		
 		UserBean user = serviceFacade.getUserService().getByLccAccount(lccno);
 		String customerCode = user.getCustomer().getCustomerCode();
 		String[] sUrIds = new String[urIds.length];
 		for (int i = 0; i < sUrIds.length; i++) {
 			sUrIds[i] = (String) urIds[i];
 		}
 
 		conferenceNewPo.setCreator(user.getId());
 		conferenceNewPo.setConferenceName(confName);
 		conferenceNewPo.setNeedApply(needApply);
 		conferenceNewPo.setDel(1);
 		conferenceNewPo.setDefaultRoleId(defaultRoleId);
 		conferenceNewPo.setConferenceTypeId(confTypeId);
 		conferenceNewPo.setTopic(topic);
 		conferenceNewPo.setDescription(description);
 		conferenceNewPo.setPassword(PasswordUtil.getEncString(confPsw));
 		conferenceNewPo.setConfNo(confNo);
 		conferenceNewPo.setId(id);
 		try{
 			serviceFacade.getConferenceNewService().renewConferenceNew(user, conferenceNewPo, sUrIds);
 		}catch(Exception e){
 			throw e;
 		}
 		return true;
 	}
 	
 	//创建新会议(vovo) args 1:lccno 2:urIds(roleid_userid)
 	public String createConference(String lccno,String confName,String confPsw,Integer defaultRoleId,Integer confTypeId,Integer needApply,String topic,String description,Object[] urIds) throws Exception{
 		ConferenceNewBean conferenceNewPo = new ConferenceNewBean();
 		
 //		String lccno = (String) args[0];
 		UserBean user = serviceFacade.getUserService().getByLccAccount(lccno);
 		String customerCode = user.getCustomer().getCustomerCode();
 		String[] sUrIds = new String[urIds.length];
 		for (int i = 0; i < sUrIds.length; i++) {
 			sUrIds[i] = (String) urIds[i];
 		}
 		
 		conferenceNewPo.setCreator(user.getId());
 		conferenceNewPo.setConferenceName(confName);
 		conferenceNewPo.setNeedApply(needApply);
 		conferenceNewPo.setDel(1);
 		conferenceNewPo.setDefaultRoleId(defaultRoleId);
 		conferenceNewPo.setConferenceTypeId(confTypeId);
 		conferenceNewPo.setTopic(topic);
 		conferenceNewPo.setDescription(description);
 		conferenceNewPo.setPassword(PasswordUtil.getEncString(confPsw));
 		try{			
 			serviceFacade.getConferenceNewService().createConfNo(conferenceNewPo, customerCode, Constant.CONF_TYPE_NEWCONFERENCE);
 			serviceFacade.getConferenceNewService().createConferenceNew(user,conferenceNewPo, sUrIds, true);
 		}catch(Exception e){
 			serviceFacade.getConferenceNewService().removeSipConfByConfno(conferenceNewPo.getConfNo());
 			conferenceNewPo.setId(null);
 			throw e;
 		}
 		return conferenceNewPo.getConfNo();
 	}
 	
 	public boolean createUCSConf(String creatorLccno, String confno)throws Exception{
 		if(existConf(confno)){
 			return true;
 		}
 		ConferenceNewBean conferenceNewPo = new ConferenceNewBean();
 		UserBean creator = serviceFacade.getUserService().getByLccAccount(creatorLccno);
 		conferenceNewPo.setCreator(creator.getId());
 		conferenceNewPo.setConfNo(confno);
 		conferenceNewPo.setConferenceName(confno);
 		conferenceNewPo.setNeedApply(0);
 		conferenceNewPo.setDel(1);
 		conferenceNewPo.setDefaultRoleId(3);//TODO 普通会议人
 		conferenceNewPo.setConferenceTypeId(1);//TODO 讲座会议
 		conferenceNewPo.setTopic(confno);
 		conferenceNewPo.setDescription(confno);
 		String[] sTemp = new String[]{"1_" + creator.getId() + "_" + creator.getLccAccount(), "2_" + creator.getId() + "_" + creator.getLccAccount()};
 		serviceFacade.getConferenceNewService().createConferenceNew(creator, conferenceNewPo, sTemp, false);
 		return true;
 	}
 	
 	public boolean removeUCSConf(String lccno, String confno)throws Exception{
 		if(!existConf(confno)){
 			return true;
 		}
 		deleteConference(lccno, confno, false);
 		return true;
 	}
 	
 	private boolean existConf(String confno)throws Exception{
 		ConferenceNewBean bean = new ConferenceNewBean();
 		bean.setConfNo(confno);
 		List<ConferenceNewBean> all = serviceFacade.getConferenceNewService().getByExample(bean);
 		if(all == null || all.size() == 0){
 			return false;
 		}else{
 			return true;
 		}
 		
 	}
 	
 	public boolean deleteConference(String lccno,String confNo, boolean removeSipconf) throws Exception{
 		UserBean user = serviceFacade.getUserService().getByLccAccount(lccno);
 		ConferenceNewBean bean = new ConferenceNewBean();
 		bean.setConfNo(confNo);
 		List<ConferenceNewBean> all = serviceFacade.getConferenceNewService().getByExample(bean);
 		for (ConferenceNewBean conferenceNewBean : all) {
 			Integer id = conferenceNewBean.getId();
 			serviceFacade.getConferenceNewService().removeConferenceNew(user, new Integer[]{id}, removeSipconf);
 			log.info("deleteConference "+lccno+","+confNo);
 		}
 		return true;
 	}
 	
 	//删除新会议(vovo)
 	public boolean deleteConference(String lccno,String confNo) throws Exception{
 		return deleteConference(lccno, confNo, true);
 	}
 	//发送会议通知
 	public boolean sendConferenceNotice(String lccno,String confNo,Object[] lccnos,boolean vovoNotice,boolean smsNotice,boolean emailNotice) throws Exception{
 		String[] members = new String[lccnos.length];
 		for (int i = 0; i < lccnos.length; i++) {
 			members[i] = (String) lccnos[i];
 		}
 		ConferenceNewBean theConfBean = null;
 		
 		List<ConferenceNewBean> all = serviceFacade.getConferenceNewService().getAll();
 		for (ConferenceNewBean conferenceNewBean : all) {
 			if (conferenceNewBean.getConfNo().equals(confNo)) {
 				theConfBean = conferenceNewBean;
 			}
 		}
 		if (vovoNotice) {
 			String passDesStr = PasswordUtil.getDesString(theConfBean.getPassword());
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 			String formatdate = sdf.format(new Date());
 			String content = PropertiesUtil.getProperty("messageResource", "page.vovo.conference.invite.text",true);
 			content = MessageFormat.format(content, new String[]{lccno,theConfBean.getConferenceName(),formatdate,passDesStr,confNo});
 			com.lorent.util.OpenfireUtil.getInstance().sendConfNotice(confNo,members,content);
 //			System.out.println("vovoNotice: "+content);
 			log.info("vovoNotice: "+confNo+","+members+","+content);
 		}
 		if (emailNotice) {			
 			ConferenceBean conference = new ConferenceBean();
 			conference.setConfSubject(theConfBean.getConferenceName());
 			conference.setConfno(confNo);
 			conference.setConfPass(theConfBean.getPassword());
 //			System.out.println(PasswordUtil.getDesString(theConfBean.getPassword()));
 			conference.setStartTime(new Date());
 			conference.setEndTime(new Date());
 			
 			UserBean lccnoUserBean = serviceFacade.getUserService().getByLccAccount(lccno);
 			lccnoUserBean.setUsername(lccno);
 			conference.setConfHost(lccnoUserBean);
 			
 			
 			conference.setConfDesc(theConfBean.getDescription());
 			for (String thelccno : members) {
 				UserBean theUserBean = serviceFacade.getUserService().getByLccAccount(thelccno);
 				UserBean user = new UserBean();
 				user.setUsername(theUserBean.getUsername());
 				user.setEmail(theUserBean.getEmail());
 				conference.getUsers().add(theUserBean);
 			}
 			MailUtil.conferenceInviteAdvice(conference, lccnoUserBean);
 		}
 		if (smsNotice) {
 			//待定
 			System.out.println("java.library.path: "+System.getProperty("java.library.path"));
 			String passDesStr = PasswordUtil.getDesString(theConfBean.getPassword());
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 			String formatdate = sdf.format(new Date());
 			String content = PropertiesUtil.getProperty("messageResource", "page.sms.conference.invite.text",true);
 			final String serialport = PropertiesUtil.getConstant("sms.serialport");
 			content = MessageFormat.format(content, new String[]{lccno,theConfBean.getConferenceName(),formatdate,passDesStr,confNo});
 			final HashMap<String, String> hashMap = new HashMap<String, String>();
 			
 			final ArrayList<LCMMobileBean> arrayList = new ArrayList<LCMMobileBean>();
 			
 			for (String username : members) {
 				UserBean lccnoUserBean = serviceFacade.getUserService().getByLccAccount(username);
 				if (lccnoUserBean.getMobile() != null || !lccnoUserBean.getMobile().equals("")) {
 					LCMMobileBean lcmMobileBean = new LCMMobileBean();
 					lcmMobileBean.setMobile(lccnoUserBean.getMobile());
 					lcmMobileBean.setContent(content);
 					arrayList.add(lcmMobileBean);
 //					System.out.println("smsNotice: put "+lccnoUserBean.getMobile()+","+content);
 					log.info("smsNotice: put "+lccnoUserBean.getMobile()+","+content);
 				}
 			}
 			new Thread(){
 
 				@Override
 				public void run() {
 					try {
 						log.info("SMSUtil.sendSMS begin "+arrayList+","+serialport);
 						SMSUtil.sendSMS(arrayList, serialport);
 						log.info("SMSUtil.sendSMS end "+arrayList+","+serialport);
 					} catch (Exception e) {
 						log.error("SMSUtil.sendSMS", e);
 						e.printStackTrace();
 					}
 				}
 			}.start();
 			
 		}
 		return true;
 	}
 	
 	public boolean canCreateConf(String lccno) throws Exception{
 		return serviceFacade.getConferenceNewService().canCreateConf(lccno);
 	}
 	
 	public int getConfUserNum(){
 		try{
 			return serviceFacade.getCustomerService().getFirstValidCustomer().getConfPeopleLimit();
 		}catch(Exception e){
 			log.error("getConfUserNum", e);
 			return -1;
 		}
 	}
 	
 	public long getSystemTime(){
 		return System.currentTimeMillis();
 	}
 	
 	//通过mcu邀请用户进入会议
 	public boolean inviteUserFromMcu(String confNo,String lccno) throws Exception{
 		CustomerBean firstValidCustomer = serviceFacade.getCustomerService().getFirstValidCustomer();
 		String xmlrpcUrl = firstValidCustomer.getMcuServer().getServerUrl();
 		log.info("inviteUserFromMcu: "+xmlrpcUrl+" , "+confNo+" , "+lccno);
 		return McuUtil.inviteConfUser(xmlrpcUrl, confNo, lccno);
 	}
 	
 	//通过mcu从会议中踢出用户
 	public boolean removeUserFromMcu(String confNo,String lccno) throws Exception {
 		CustomerBean firstValidCustomer = serviceFacade.getCustomerService().getFirstValidCustomer();
 		String xmlrpcUrl = firstValidCustomer.getMcuServer().getServerUrl();
 		log.info("removeUserFromMcu: "+xmlrpcUrl+" , "+confNo+" , "+lccno);
 		ConferenceBean conferenceBean = new ConferenceBean();
 		conferenceBean.setConfno(confNo);
 		conferenceBean.setCustomer(firstValidCustomer);
 		McuUtil.removeConfUser(conferenceBean, new String[]{lccno});
 		return true;
 	}
 	
 	public Object[] getUCSConf()throws Exception{
 		ConferenceNewBean bean = new ConferenceNewBean();
 		bean.setDel(1);
 		List<ConferenceNewBean> confs = serviceFacade.getConferenceNewService().getByExample(bean);
		if(confs.size() == 0){
 			return null;
 		}
 		Object[] objs = new Object[confs.size()];
 		for(int i = 0; i < confs.size(); i++){
 			String[] strs = new String[1];
 			strs[0] = confs.get(i).getConfNo();
 			objs[i] = strs;
 		}
 		return objs;
 	}
 	
 	public boolean grantConfAuthority(String confNo,String lccno,String roleName) throws Exception{
 		return serviceFacade.getConferenceNewService().grantAuthority(confNo,lccno,roleName);
 	}
 	
 	public boolean revokeConfAuthority(String confNo,String lccno,String roleName) throws Exception{
 		return serviceFacade.getConferenceNewService().revokeAuthority(confNo,lccno,roleName);
 	}
 	
 	//--------------------------------------供 VOVO 调用(不带业务逻辑)end ----------------------------------
 	/**
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args)throws Exception {
 		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
 		config.setServerURL(new URL("http://127.0.0.1:9090/lcm/lcmRpc"));
 		XmlRpcClient client = new XmlRpcClient();
 		client.setConfig(config);
 		System.out.println(client.execute("lcmConf.test", new Object[]{}));
 //		System.out.println(client.execute("lcmConf.getConfnoList",new Object[]{}));
 //		System.out.println(client.execute("lcmConf.createConf", new Object[]{"1530", new String[]{"1666"}, 2, "H264-CIF-Profile@384:30:1"}));
 		int confid = 2;
 		String other = "1666";
 //		System.out.println(client.execute("lcmConf.removeConf", new Object[]{confid}));
 //		System.out.println(client.execute("lcmConf.updateConf", new Object[]{confid, 1}));
 //		System.out.println(client.execute("lcmConf.addConfUser", new Object[]{confid, new String[]{other}}));
 //		System.out.println(client.execute("lcmConf.removeConfUser", new Object[]{confid, new String[]{other}}));
 //		System.out.println(client.execute("lcmConf.setConfUserVideo", new Object[]{confid, new String[]{other}, 1}));
 //		System.out.println(client.execute("lcmConf.setConfUserAudio", new Object[]{confid, new String[]{other}, 0}));
 		
 	}
 }
