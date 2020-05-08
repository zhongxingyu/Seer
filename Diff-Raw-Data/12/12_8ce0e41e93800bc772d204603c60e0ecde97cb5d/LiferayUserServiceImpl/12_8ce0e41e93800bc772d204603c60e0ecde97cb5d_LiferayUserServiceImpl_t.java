 package ua.dp.primat.portlet.userinform.services;
 
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
 import com.liferay.portal.kernel.util.WebKeys;
 import com.liferay.portal.model.Group;
 import com.liferay.portal.model.User;
 import com.liferay.portal.service.GroupLocalServiceUtil;
 import com.liferay.portal.service.UserLocalServiceUtil;
 import com.liferay.portal.theme.ThemeDisplay;
 import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
 
 /**
  *
  * @author fdevelop
  */
@Service
 public class LiferayUserServiceImpl implements LiferayUserService {
 
     public User getUserInfo(HttpServletRequest req) {
         try {
             final ThemeDisplay themeDisplay = (ThemeDisplay) req.getAttribute(
                     WebKeys.THEME_DISPLAY);
 
             final Group group = GroupLocalServiceUtil.getGroup(
                     themeDisplay.getScopeGroupId());
 
             if (group.isUser()) {
                 return UserLocalServiceUtil.getUserById(group.getClassPK());
             } else {
                 return null;
             }
         } catch (PortalException pe) {
             return null;
         } catch (SystemException se) {
             return null;
         }
     }
 }
