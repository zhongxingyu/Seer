 package com.gemantic.killer.controller;
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.gemantic.common.exception.ServiceException;
 import com.gemantic.common.util.DESUtil;
 import com.gemantic.common.util.FileUtil;
 import com.gemantic.common.util.MyTimeUtil;
 import com.gemantic.common.util.http.cookie.CookieUtil;
 import com.gemantic.killer.common.model.Message;
 import com.gemantic.killer.common.model.Status;
 import com.gemantic.killer.exception.ServiceErrorCode;
 import com.gemantic.killer.model.Room;
 import com.gemantic.killer.model.User;
 import com.gemantic.killer.service.MemberService;
 import com.gemantic.killer.service.RoomService;
 
 import com.gemantic.killer.util.MailUtil;
 import com.gemantic.killer.util.PunchUtil;
 import com.gemantic.labs.killer.service.UsersService;
 
 /**
  * 提供游戏房间的创建,删除,玩家列表等功能
  * 
  * @author xdyl
  * 
  */
 @Controller
 public class PlayerController {
 	private static final Log log = LogFactory.getLog(PlayerController.class);
 	@Autowired
 	private RoomService roomService;
 	@Autowired
 	private MemberService memberService;
 	@Autowired
 	private UsersService userService;
 
 	@Autowired
 	private CookieUtil cookieUtil;
 
 	@Autowired
 	private JavaMailSender sender;
 
 	/**
 	 * 玩家登入
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/player/openID")
 	public String getLoginThird(HttpServletRequest request, HttpServletResponse response, ModelMap model, String type) throws Exception {
 
 		log.info(type+" login at of user ");
 		cookieUtil.clearCookie(response);
 		// 登录不成功,重新登录
 		model.addAttribute("code", "-0");		
 		model.addAttribute("type", type);
 		
 		return "/room/player/third";
 
 	}
 
 	/**
 	 * 玩家登入
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/player/openID", method = RequestMethod.POST)
 	public String loginThird(HttpServletRequest request, HttpServletResponse response, ModelMap model, String type, String openID,String name) throws Exception {
 		Long uid = null;
 		String uname = null;
 		boolean success = false;
 
 		// 首先判断email
 		if (StringUtils.isBlank(type) || StringUtils.isBlank(openID)) {
 
 			model.addAttribute("code", "-6003");
 			return "common/success";
 		} else {
 
 		}
 
 		// 没有Email再判断是否是cookie
 		uid = this.userService.getUsersIdByOpenID(openID);
 		if (uid == null) {	
 			log.info("openID "+openID+" of name "+ name+" is a new user ,create at ");
 			//create user
 			User user=new User();
 			user.setName(name);
 			user.setLoginAt(System.currentTimeMillis());
 			
 			user.setOpenID(openID);
 			uid=this.userService.insert(user);
 			success=true;
 			
 			
 			
 		}else{
 			log.info("openID "+openID+" of name "+ name+" is a old user of "+uid);
 			success=true;
 			
 			
 		}
 		log.info(uid + " loging in " + success);
 		if (success) {
 			User user = this.userService.getObjectById(uid);
 			if (user == null) {
 				// clear cookie
 				// 怎么清除还不知道
 
 				model.addAttribute("code", "-6003");
 				return "common/success";
 			} else {
 				uname = user.getName();
 				// loging success we should set cookie;
 				// 这些能否在Filter里处理呢
 				// 什么时候把Cookie种下呢.这里的Cookie怎么那么快就失效了.为什么程序一重新启动就么有了.
 				cookieUtil.setIdentity(request, response, uname, uid);
 				user.setLoginAt(System.currentTimeMillis());
 				this.userService.update(user);
 				String url = "/m/list.do";
 				log.info(url);
 				model.addAttribute("code", "0");
 				return "common/success";
 
 			}
 
 		} else {
 			cookieUtil.clearCookie(response);
 			// 登录不成功,重新登录
 			model.addAttribute("code", "-6002");
 
 			return "common/success";
 
 		}
 
 	}
 
 	/**
 	 * 玩家登入
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/player/login")
 	public String login(HttpServletRequest request, HttpServletResponse response, ModelMap model, String email, String password, String third) throws Exception {
 		Long uid = null;
 		String uname = null;
 		boolean success = false;
 
 		// 首先判断email
 		if (email == null) {
 			// 没有Email再判断是否是cookie
 			uid = cookieUtil.getID(request, response);
 			log.debug(uname + "  id: " + uid);
 			if (uid == null) {
 				log.info(uid + " not in cookie,check password ");
 				// 什么都没有,只好返回了
 
 			} else {
 				log.info(uid + " in cookie ");
 				uname = cookieUtil.getName(request, response);
 				success = true;
 			}
 
 		} else {
 			uid = this.userService.getUsersIdByEmail(email);
 			if (uid == null) {
 				model.addAttribute("code", "-6003");
 				return "redirect:/";
 			}
 			success = this.userService.verify(uid, password);
 
 		}
 
 		log.info(uid + " loging in " + success);
 		if (success) {
 			User user = this.userService.getObjectById(uid);
 			if (user == null) {
 				// clear cookie
 				// 怎么清除还不知道
 
 				model.addAttribute("code", "-6003");
 				return "redirect:/";
 			} else {
 				uname = user.getName();
 				// loging success we should set cookie;
 				// 这些能否在Filter里处理呢
 				// 什么时候把Cookie种下呢.这里的Cookie怎么那么快就失效了.为什么程序一重新启动就么有了.
 				cookieUtil.setIdentity(request, response, uname, uid);
 				user.setLoginAt(System.currentTimeMillis());
 				this.userService.update(user);
 				String url = "/";
 				log.info(url);
 				return "redirect:" + url;
 
 			}
 
 		} else {
 			cookieUtil.clearCookie(response);
 			// 登录不成功,重新登录
 			model.addAttribute("code", "-6002");
 
 			return "redirect:/";
 
 		}
 
 	}
 
 	/**
 	 * 玩家登入
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/player/verify", method = RequestMethod.POST)
 	public String verify(HttpServletRequest request, HttpServletResponse response, ModelMap model, String email) throws Exception {
 
 		int code = 0;
 
 		Long id = this.userService.getUsersIdByEmail(email);
 		if (id == null) {
 			code = ServiceErrorCode.Email_Already_Exist;
 		}
 
 		model.addAttribute("code", code);
 		return "/player/email/verify";
 
 	}
 
 	/**
 	 * 玩家退出游戏房间
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/player/logout")
 	public String logout(HttpServletRequest request, HttpServletResponse response, ModelMap model, Long uid, Long rid) throws Exception {
 
 		uid = cookieUtil.getID(request, response);
 		log.debug(uid + " logout room " + rid);
 		this.memberService.userLogOut(rid, uid);
 		String url = "/m/list.do?uid=" + uid;
 		log.info(url);
 		return "redirect:" + url;
 	}
 
 	/**
 	 * 玩家退出网站
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/player/offline")
 	public String offline(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
 		
 		Long uid = cookieUtil.getID(request, response);
 		log.info(" uid "+ uid +" want offline ");
 		// mock a message of logout
 		Room r = this.memberService.getRoomOfUser(uid);
 		if (r == null) {
 
 		} else {
 			Message m = new Message();
 			m.setVersion(r.getVersion());
 			m.setSubject(uid.toString());
 			m.setPredict("logout");
 			m.setWhere(r.getId().toString());
 			r.getMessages().offer(m);
 			this.roomService.updateRoom(r);
 			this.memberService.userLogOut(r.getId(), uid);
 		}
 
 		log.info(uid + " offline  ");
 		cookieUtil.clearCookie(response);
 
 		// 怎么把用户所在的房间踢掉
 		String url = "/";
 		log.info(url);
 		return "redirect:" + url;
 	}
 
 	/**
 	 * 玩家准备
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/a/player/ready")
 	public String ready(HttpServletRequest request, HttpServletResponse response, ModelMap model, @RequestParam Long rid, @RequestParam Long uid)
 			throws Exception {
 		log.debug(uid + " want get ready of " + rid);
 
 		int code = 0;
 		String message = "success";
 
 		try {
 
 			memberService.ready(rid, uid);
 		} catch (ServiceException e) {
 
 			code = e.getErrorCode();
 
 		}
 		model.addAttribute("code", code);
 
 		return "/room/play/ready";
 	}
 
 	/**
 	 * 玩家退出
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/player/exit")
 	public String exit(HttpServletRequest request, HttpServletResponse response, ModelMap model, @RequestParam Long roomID) throws Exception {
 		log.debug("start get room list ");
 		List<Room> rooms = roomService.getList();
 		model.addAttribute("rooms", rooms);
 		return "/room/play/list";
 	}
 
 	/**
 	 * 玩家发言
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/player/speak")
 	public String speak(HttpServletRequest request, HttpServletResponse response, ModelMap model, String name) throws Exception {
 		log.debug("HI");
 
 		return "/game/room/show";
 	}
 
 	/**
 	 * 玩家动作
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/player/action")
 	public String action(HttpServletRequest request, HttpServletResponse response, ModelMap model, String name) throws Exception {
 		log.debug("HI");
 
 		return "/game/room/show";
 	}
 
 	/**
 	 * 找回密码
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/player/forget", method = RequestMethod.POST)
 	public String forget(HttpServletRequest request, HttpServletResponse response, ModelMap model, String mail) throws Exception {
 		log.info(mail + " forget password ");
 		Long uid = userService.getUsersIdByEmail(mail);
 		String token = DESUtil.encrypt((String.valueOf(uid) + "," + String.valueOf(System.currentTimeMillis())).getBytes());
 		String link = "http://42.121.113.70//player/regedit.do?type=forget&token=" + URLEncoder.encode(token, "utf8");
 		// 邮件内容，注意加参数true，表示启用html格式
 		String content = "此邮件为葡萄藤轻游戏系统自动发送,无须回复.点此链接找回密码,此链接在五分钟之内有效,如果不是您发起的,请直接忽视" + link;
 		MailUtil.send(sender, mail, content);
 		return "/room/player/forget";
 	}
 
 	/**
 	 * 获取玩家的状态信息
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/player/status")
 	public String getStatus(HttpServletRequest request, HttpServletResponse response, ModelMap model, String name) throws Exception {
 		log.debug("HI");
 
 		return "/game/room/show";
 	}
 
 	/**
 	 * 获取玩家的状态信息
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/player/info")
 	public String getInfo(HttpServletRequest request, HttpServletResponse response, ModelMap model, @RequestParam Long[] uids, Long rid) throws Exception {
 
 		log.info(uids);
 		List<Long> userIDS = Arrays.asList(uids);
 
 		List<User> users = userService.getObjectsByIds(userIDS);
 		log.info(" get user info " + users);
 
 		// get status
 		Map<Long, Status> userID_Status = new HashMap();
 		if (rid == null) {
 			// 无房间号的是不能获取玩家状态的
 
 		} else {
 
 			// World w=this.worldService.getWorld(rid);
 			List<Status> status = new ArrayList();// w.getStatus();
 
 			for (Status s : status) {
 				if ("user".equals(s.getName())) {
 					userID_Status.put(s.getId(), s);
 				}
 			}
 
 		}
 		/*
 		 * Set<Long> votes=new HashSet(); votes.add(333L); votes.add(444L);
 		 * u.setVotes(votes);
 		 */
 		Map<Long, Long> uid_rid = this.memberService.batchGetRoomOfUser(userIDS);
 		Map<Long, Room> rid_room = new HashMap();
 		for (Long roomID : uid_rid.values()) {
 			if (roomID != null) {
 				Room r = this.roomService.getRoom(roomID);
 				rid_room.put(roomID, r);
 			}
 
 		}
 
 		model.addAttribute("users", users);
 		model.addAttribute("uid_rid", uid_rid);
 		model.addAttribute("rid_room", rid_room);
 		model.addAttribute("status", userID_Status);
 		return "/room/person/info";
 
 	}
 
 	/**
 	 * 获取玩家的状态信息
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/player/detail")
 	public String getInfo(HttpServletRequest request, HttpServletResponse response, ModelMap model, Long uid) throws Exception {
 
 		log.info(uid + " detail ");
 
 		Long selfID = cookieUtil.getID(request, response);
 
 		if (selfID == null) {
 			return "redirect:/?";
 		}
 
 		if (uid == null) {
 			uid = selfID;
 		}
 		User u = this.userService.getObjectById(uid);
 		log.info(" get user info " + u);
 
 		int punchCount = PunchUtil.getContinueDay(PunchUtil.Punch_Time_Start, Integer.MAX_VALUE, PunchUtil.Punch_Time_Start, u.getPunch());
 
 		model.addAttribute("user", u);
 		model.addAttribute("punchCount", punchCount);
 		model.addAttribute("selfID", selfID);
 
 		return "/room/player/detail";
 
 	}
 
 	/**
 	 * 获取玩家的状态信息
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/player/punch", method = RequestMethod.POST)
 	public String punch(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
 
 		Long uid = cookieUtil.getID(request, response);
 		log.info(uid + " punch ");
 		User user = this.userService.getObjectById(uid);
 		if (user == null) {
 
 			return "redirect:/";
 		}
 		boolean isPunch = PunchUtil.isPunched(user);
 		if (isPunch) {
 			model.addAttribute("code", "-1");
 			return "/room/person/punch";
 		}
 		String str = PunchUtil.punchTheClock(System.currentTimeMillis(), PunchUtil.Punch_Time_Start, user.getPunch());
 
 		user.setPunch(str);
 		user.setPunchAt(MyTimeUtil.getPreZeroTimeMillions(0));
 		int m = 2000;
 		user.setMoney(user.getMoney() + m);
 		this.userService.update(user);
 		model.addAttribute("money", m);
 		model.addAttribute("code", "0");
 		return "/room/person/punch";
 
 	}
 
 	/**
 	 * 获取玩家的状态信息
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/player/update", method = RequestMethod.POST)
 	public String update(HttpServletRequest request, HttpServletResponse response, ModelMap model, User user) throws Exception {
 
 		Long uid = cookieUtil.getID(request, response);
 		log.info(uid + " update " + user);
 
 		if (user == null) {
 
 			return "redirect:/player/detail.do?uid=" + uid;
 		}
 		if (uid.longValue() != user.getId()) {
 			return "redirect:/player/detail.do?uid=" + uid;
 		}
 		User oldUser = this.userService.getObjectById(uid);
 
 		String path = request.getRealPath(request.getContextPath());
 		String fullName = "/data/user_info/" + uid;
		log.info(fullName);
 		try {
 			FileUtil.saveImage(user.getIcon(), fullName);
 		} catch (Exception e) {
 			log.error(fullName + " save failure " + user);
 		}
		oldUser.setIcon("/r/user_info/" + uid);
 		oldUser.setName(user.getName());
 		oldUser.setSign(user.getSign());
 		oldUser.setMusic(user.getMusic());
 		this.userService.update(oldUser);
 
 		return "redirect:/player/detail.do?uid=" + uid;
 
 	}
 
 	/**
 	 * 获取玩家的状态信息
 	 * 
 	 * @param request
 	 * @param response
 	 * @param model
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/player/email")
 	public String getIDByEmail(HttpServletRequest request, HttpServletResponse response, ModelMap model, @RequestParam String email) throws Exception {
 
 		log.info(email + " want get id ");
 
 		Long userID = this.userService.getUsersIdByEmail(email);
 		log.info(email + " get user id by email " + userID);
 
 		model.addAttribute("userID", userID);
 
 		return "/room/person/email";
 
 	}
 
 }
