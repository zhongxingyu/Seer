 package ua.dp.primat.portlet.userinform.services;
 
 import com.liferay.portal.model.User;
 import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
 
 /**
  *
  * @author fdevelop
  */

@Service
 public interface LiferayUserService {
     User getUserInfo(HttpServletRequest req);
 }
