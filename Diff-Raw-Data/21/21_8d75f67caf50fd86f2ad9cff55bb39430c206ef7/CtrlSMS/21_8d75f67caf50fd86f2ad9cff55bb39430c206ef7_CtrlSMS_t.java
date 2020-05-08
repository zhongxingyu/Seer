 package com.huhuo.cmsystem.ms;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.huhuo.carservicecore.cust.ms.ModelSMS;
 import com.huhuo.carservicecore.sys.user.ModelUser;
 import com.huhuo.cmsystem.SystemBaseCtrl;
 import com.huhuo.cmsystem.constant.Constant;
 import com.huhuo.integration.web.Message;
 import com.huhuo.integration.web.Message.Status;
@Controller("cmsystemCtrlSMS")
@RequestMapping(value="/cmsystem/ms/sms")
 public class CtrlSMS extends SystemBaseCtrl {
 
	@Resource(name = "cmsystemServSMS")
 	private IServSMS iServSMS;
 	
	@RequestMapping(value = "/send.do")
 	public String get(Model model, HttpSession session, ModelSMS msg) {
 		logger.info("send message --> {}", msg);
 		ModelUser sender = (ModelUser) session.getAttribute(Constant.SESSION_USER);
 		iServSMS.send(sender.getId(), msg.getRecieverId(), msg.getPhoneNum(), msg.getContent());
 		return render(model, new Message<ModelSMS>(Status.SUCCESS, "send messsage successfully!", msg));
 	}
 	
 }
