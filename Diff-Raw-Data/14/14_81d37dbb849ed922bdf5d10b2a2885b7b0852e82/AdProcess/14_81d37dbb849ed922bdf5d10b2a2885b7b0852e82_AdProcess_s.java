 package com.pintu.facade;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 public class AdProcess {
 
 	private Logger log = Logger.getLogger(AdProcess.class);
 	// 由Spring注入
 	private ApiAdaptor apiAdaptor;
 
 	/**
 	 * 处理正常用户登录post的请求
 	 * 
 	 * @param action
 	 * @param req
 	 * @param res
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	public void doPostProcess(String action, HttpServletRequest req,
 			HttpServletResponse res) throws ServletException, IOException {
 		
 		//提供给前台的方法随时验证厂商状态
 		if(action.equals(AppStarter.VALIDATEVENDER)){
 			res.setContentType("text/plain;charset=UTF-8");
 			PrintWriter pw = res.getWriter();
 			String  venderId= req.getParameter("venderId");
 			String result = apiAdaptor.validateVender(venderId);
 			pw.print(result);
 			pw.close();
 		//登录
 		}else if(action.equals(AppStarter.LOGINAD)){
 			res.setContentType("text/plain;charset=UTF-8");
 			PrintWriter pw = res.getWriter();
 			String email = req.getParameter("email");
 			String pwd = req.getParameter("pwd");
 			String result = apiAdaptor.getExistVender(email, pwd);
 			pw.print(result);
 			pw.close();
 		//修改密码	
 		}else if(action.equals(AppStarter.CHANGEPWD)){
 			res.setContentType("text/plain;charset=UTF-8");
 			PrintWriter pw = res.getWriter();
 			String newPwd = req.getParameter("newPwd");
 			String venderId = req.getParameter("venderId");
 			int result = apiAdaptor.changePwd(newPwd,venderId);
 			pw.print(result);
 			pw.close();
 		//获取可用广告源	
 		}else if(action.equals(AppStarter.GETTODAYADS)){
 			res.setContentType("text/plain;charset=UTF-8");
 			PrintWriter pw = res.getWriter();
 			String venderId = req.getParameter("venderId");
 			String result = apiAdaptor.getTodayAds(venderId);
 			log.debug(result);
 			pw.print(result);
 			pw.close();
 		//根据条件查询广告	
 		}else if(action.equals(AppStarter.SEARCHADS)){
 			res.setContentType("text/plain;charset=UTF-8");
 			PrintWriter pw = res.getWriter();
 			String keys = req.getParameter("keys");
 			String time = req.getParameter("time");
 			String venderId = req.getParameter("venderId");
 			String result = apiAdaptor.searchAds(keys,time,venderId);
 			log.debug(result);
 			pw.print(result);
 			pw.close();
 		//删除某一广告	
 		}else if(action.equals(AppStarter.DELETEADSBYID)){
 			res.setContentType("text/plain;charset=UTF-8");
 			PrintWriter pw = res.getWriter();
 			String adId = req.getParameter("adId");
 			String result = apiAdaptor.deleteAdsById(adId);
 			log.debug(result);
 			pw.print(result);
 			pw.close();
 		//新建广告
 		}else if(action.equals(AppStarter.CREATEADS)){
 			res.setContentType("text/plain;charset=UTF-8");
 			PrintWriter pw = res.getWriter();
 			String publisher = req.getParameter("venderId");
 //			String vender = req.getParameter("vender");
 			String type = req.getParameter("type");
 			String priority = req.getParameter("priority");
 			String startTime = req.getParameter("startTime");
 			String endTime = req.getParameter("endTime");
 			String content = req.getParameter("content");
 			String link = req.getParameter("link");
 			String result = apiAdaptor.createAds(publisher,type,priority,startTime,endTime,content,link);
 			log.debug(result);
 			pw.print(result);
 			pw.close();
 		//获取某一条广告	
 		}else if(action.equals(AppStarter.GETADSBYID)){
 			res.setContentType("text/plain;charset=UTF-8");
 			PrintWriter pw = res.getWriter();
 			String adId = req.getParameter("adId");
 			String result = apiAdaptor.getAdsById(adId);
 			log.debug(result);
 			pw.print(result);
 			pw.close();
 		//更新某一条广告	
 		}else if(action.equals(AppStarter.UPDATEADSBYID)){
 			res.setContentType("text/plain;charset=UTF-8");
 			PrintWriter pw = res.getWriter();
 			String adId = req.getParameter("adId");
 			String publisher = req.getParameter("venderId");
 //			String vender = req.getParameter("vender");
 			String type = req.getParameter("type");
 			String priority = req.getParameter("priority");
 			String startTime = req.getParameter("startTime");
 			String endTime = req.getParameter("endTime");
 			String content = req.getParameter("content");
 			String link = req.getParameter("link");
			String result = apiAdaptor.updateAdsById(adId,publisher,type,priority,startTime,endTime,content,link);
 			log.debug(result);
 			pw.print(result);
 			pw.close();
 		//查询厂商	
 		}else if(action.equals(AppStarter.SEARCHVENDERS)){
 			res.setContentType("text/plain;charset=UTF-8");
 			PrintWriter pw = res.getWriter();
 			String keys = req.getParameter("keys");
 			String result = apiAdaptor.searchVenders(keys);
 			log.debug(result);
 			pw.print(result);
 			pw.close();
 		//获取某一厂商	
 		}else if(action.equals(AppStarter.GETVENDERSBYID)){
 			res.setContentType("text/plain;charset=UTF-8");
 			PrintWriter pw = res.getWriter();
 			String venderId = req.getParameter("venderId");
 			String result = apiAdaptor.getVendersById(venderId);
 			log.debug(result);
 			pw.print(result);
 			pw.close();
 		//更新厂商	
 		}else if(action.equals(AppStarter.UPDATEVENDERSBYID)){
 			res.setContentType("text/plain;charset=UTF-8");
 			PrintWriter pw = res.getWriter();
 			String venderId = req.getParameter("venderId");
 			String name = req.getParameter("name");
 			String email = req.getParameter("email");
 			String serviceLevel = req.getParameter("serviceLevel");
 			String effectiveTime = req.getParameter("effectiveTime");
 			String deadTime = req.getParameter("deadTime");
 			String deployDNS = req.getParameter("deployDNS");
 			String enable = req.getParameter("enable");
 			String result = apiAdaptor.updateVendersById(venderId,name,email,serviceLevel,effectiveTime,deadTime,deployDNS,enable);
 			log.debug(result);
 			pw.print(result);
 			pw.close();
 		//新建厂商	
 		}else if(action.equals(AppStarter.CREATEVENDERS)){
 			res.setContentType("text/plain;charset=UTF-8");
 			PrintWriter pw = res.getWriter();
 			String name = req.getParameter("name");
 			String email = req.getParameter("email");
 			String serviceLevel = req.getParameter("serviceLevel");
 			String effectiveTime = req.getParameter("effectiveTime");
 			String deadTime = req.getParameter("deadTime");
 			String deployDNS = req.getParameter("deployDNS");
 			String enable = req.getParameter("enable");
 			String result = apiAdaptor.createVenders(name,email,serviceLevel,effectiveTime,deadTime,deployDNS,enable);
 			log.debug(result);
 			pw.print(result);
 			pw.close();
 			
 		}else if(action.equals(AppStarter.REGISTVENDERS)){
 			res.setContentType("text/plain;charset=UTF-8");
 			PrintWriter pw = res.getWriter();
 			String name = req.getParameter("name");
 			String email = req.getParameter("email");
 			String pwd = req.getParameter("pwd");
 			String deployDNS = req.getParameter("deployDNS");
 			String result = apiAdaptor.registVenders(name,email,pwd,deployDNS);
 			log.debug(result);
 			pw.print(result);
 			pw.close();
 		
 		}else if(action.equals(AppStarter.CHECKOUTREGISTER)){
 			res.setContentType("text/plain;charset=UTF-8");
 			PrintWriter pw = res.getWriter();
 			String email = req.getParameter("email");
 			int result = apiAdaptor.checkoutRegister(email);
 			log.debug(result);
 			pw.print(result);
 			pw.close();
 		}
 		
 	}
 	
 
 	public void setApiAdaptor(ApiAdaptor apiAdaptor) {
 		this.apiAdaptor = apiAdaptor;
 	}
 
 	
 
 }
