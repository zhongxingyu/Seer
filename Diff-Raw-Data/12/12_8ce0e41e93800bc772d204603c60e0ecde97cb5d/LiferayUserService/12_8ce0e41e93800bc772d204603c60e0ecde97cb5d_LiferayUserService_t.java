 package ua.dp.primat.portlet.userinform.services;
 
 import com.liferay.portal.model.User;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  *
  * @author fdevelop
  */
 public interface LiferayUserService {
     User getUserInfo(HttpServletRequest req);
 }
