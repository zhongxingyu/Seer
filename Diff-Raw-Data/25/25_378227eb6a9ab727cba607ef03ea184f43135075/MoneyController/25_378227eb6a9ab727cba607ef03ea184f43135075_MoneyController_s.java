 package com.gemantic.killer.controller;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.gemantic.common.util.MyListUtil;
 import com.gemantic.common.util.http.cookie.CookieUtil;
 import com.gemantic.killer.model.User;
 import com.gemantic.labs.killer.model.MoneyFlow;
 import com.gemantic.labs.killer.service.MoneyFlowService;
 import com.gemantic.labs.killer.service.UsersService;
 
 /**
  * 提供游戏房间的创建,删除,玩家列表等功能
  * 
  * @author xdyl
  * 
  */
 @Controller
 public class MoneyController {
 	private static final Log log = LogFactory.getLog(MoneyController.class);
 
 	@Autowired
 	private UsersService userSevice;
 
 	@Autowired
 	private MoneyFlowService moneyFlowService;
 
 	@Autowired
 	private CookieUtil cookieUtil;
 
 	/**
 	 * 游戏准备
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/money/flow")
 	public String list(HttpServletRequest request, HttpServletResponse response, ModelMap model, Integer page, Integer size, Long uid, String type) throws Exception {
 
 		if (uid == null) {
 			uid = cookieUtil.getID(request, response);
 			if(uid==null){
				return "redirect/";
 			}
 		}
 		if ("out".equals(type)) {
 
 		} else {
 			type = "in";
 		}
 		log.info(uid + " start get money list " + type);
 		if (page == null) {
 			page = 1;
 		}
 		if (page < 1) {
 			page = 1;
 		}
 		if (size == null) {
 			size = 21;
 		}
 		Integer start = (page - 1) * size;
 		List<Long> userIDS = new ArrayList();
 		List<User> users = new ArrayList();
 		List<Long> ids = new ArrayList();
 		if ("in".equals(type)) {
 			ids = this.moneyFlowService.getMoneyFlowIdsByUid(uid, start, size);
 		} else {
 			ids = this.moneyFlowService.getMoneyFlowIdsByFid(uid, start, size);
 		}
 
 		List<MoneyFlow> mfs = this.moneyFlowService.getObjectsByIds(ids);
 		Set<Long> uids = new HashSet();
 		uids.add(uid);
 		for (MoneyFlow mf : mfs) {
 			uids.add(mf.getFid());
 			uids.add(mf.getUid());
 		}
 
 		users = this.userSevice.getObjectsByIds(new ArrayList(uids));
 		Map<Long, User> ids_user = MyListUtil.convert2Map(User.class.getDeclaredField("id"), users);
 
 		//log.info("money all " + ids_user);
 
 		users = this.userSevice.getObjectsByIds(userIDS);
 
 		model.addAttribute("uid", uid);
 		model.addAttribute("current", ids_user.get(uid));
 		model.addAttribute("mfs", mfs);
 		model.addAttribute("id_users", ids_user);
 		model.addAttribute("type", type);
 		model.addAttribute("page", page);
 		model.addAttribute("size", size);
 
 		return "/room/financial/moneyFlow";
 	}
 
 	/**
 	 * 游戏准备
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/money/trade", method = RequestMethod.POST)
 	public String trade(HttpServletRequest request, HttpServletResponse response, ModelMap model, MoneyFlow mf) throws Exception {
 
 		log.info("start trade "+mf);
 		Long currentID = cookieUtil.getID(request, response);
 
 		if (mf==null||currentID == null||mf.getUid()==null) {
 			log.warn(mf+" not exist or not login ");
 			model.addAttribute("code", -9001);
 			return "/message/accept/show";
 		}
 		mf.setFid(currentID);
         User fuser=this.userSevice.getObjectById(currentID);
         if(fuser==null){
         	log.warn(currentID+"not exist ");
         	model.addAttribute("code", -9001);
         	return "/room/person/trade";
         }
         if(mf.getMoney()<=0){
         	log.warn(mf+" incorrect money ");
         	model.addAttribute("code", -9004);
         	return "/room/person/trade";
         }
         if(fuser.getMoney()<mf.getMoney()){
         	log.warn(fuser+" no money  "+mf);
         	model.addAttribute("code", -9002);
         	return "/room/person/trade";
         }
         User tuser=this.userSevice.getObjectById(mf.getUid());
         if(tuser==null){
         	log.warn(mf.getId()+" to use not exist   ");
         	model.addAttribute("code", -9003);
         	return "/room/person/trade";
         }
         mf.setHappenAt(System.currentTimeMillis());
         
         //s@163.com sogou@163.com qq        
         if(currentID.equals(284L)||currentID.equals(256L)||currentID.equals(245L)){
         	log.info("admin give money ~ "+mf);
         }else{
         	 fuser.setMoney(fuser.getMoney()-mf.getMoney());
         }
        
         Integer tax=mf.getMoney()/20;
         log.info("tax is "+tax);
         tuser.setMoney(tuser.getMoney()+mf.getMoney()-tax);
         List<User> users=new ArrayList();
         users.add(fuser);
         users.add(tuser);
         this.userSevice.updateList(users);     
         this.moneyFlowService.insert(mf);
 
 
         model.addAttribute("code", 0);
         model.addAttribute("money", fuser.getMoney());
     	return "/room/person/trade";
 	}
 	
 	
 	@RequestMapping(value = "/money/trade")
 	public String getTrade(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
 
 		Long uid = cookieUtil.getID(request, response);
 		if(uid==null){
 			return "redirect:/";
 		}
 
 		
         User user=this.userSevice.getObjectById(uid);
        
 
         model.addAttribute("current", user);
     	return "/room/financial/trade";
 	}
 
 }
