 package scripts;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.trackstudio.app.adapter.AuthAdapter;
 import com.trackstudio.app.adapter.auth.SimpleAuthAdapter;
 import com.trackstudio.exception.GranException;
 import com.trackstudio.kernel.cache.UserRelatedInfo;
 import com.trackstudio.kernel.cache.UserRelatedManager;
 import com.trackstudio.startup.Config;
 
 /**
  * Разрешаем входить в систему только пользователям определенных групп 
  */
 
 public class GroupPermittedAuthAdapter implements AuthAdapter{
 
 	protected List<String> allowed=null, denied=null;
 
 	    public GroupPermittedAuthAdapter() {
 	    	
 
 	        
 	    }
 
 	    
 	    public boolean init() {
 	    	
 	        return true;
 	    }
 
 	    /**
 	     * Возвращает текстовое описание адаптера
 	     *
 	     * @return adapter's description
 	     */
 	    public String getDescription() {
 	        return "Group Permitted Database Authentication Adapter";
 	    }
 
 	    /**
 	     * Производит авторизацию
 	     *
 	     * @param userId   ID пользователя
 	     * @param password пароль
 	     * @param result   Результат авторизации
 	     * @return TRUE - если авторизация прошла удачно, FALSE - если нет
 	     * @throws GranException при необходимости
 	     */
 	    public boolean authorizeImpl(String userId, String password, boolean result, HttpServletRequest request) throws GranException {
 	    	String allowedString = Config.getInstance().getProperty("trackstudio.security.allowed");
 	        String deniedString =  Config.getInstance().getProperty("trackstudio.security.denied");
 	        if (allowedString!=null){
 	        	allowed = new ArrayList<String>();
 	        	for (String a : allowedString.split(";")){
 	        		allowed.add(a);
 	        	}
 	        }
 	        if (deniedString!=null){
 	        	denied = new ArrayList<String>();
 	        	for (String a : deniedString.split(";")){
 	        		denied.add(a);
 	        	}
 	        }
 	        if (result) return result;
	        if (allowed==null && denied==null) return new SimpleAuthAdapter().authorizeImpl(userId, password, result, request);
 	        UserRelatedInfo user = UserRelatedManager.getInstance().find(userId);
 	        if (denied!=null && denied.contains(user.getPrstatusId())) return false;
 	        if (allowed!=null && allowed.contains(user.getPrstatusId())){
 	        	return new SimpleAuthAdapter().authorizeImpl(userId, password, result, request);
 	        } else if (allowed==null) return new SimpleAuthAdapter().authorizeImpl(userId, password, result, request);
 	        	else return false;
 	    }
 
 	    /**
 	     * Меняет пароль пользователя. Реализация не нужна
 	     *
 	     * @param userId   ID пользователя
 	     * @param password пароль
 	     * @throws GranException при необходимости
 	     */
 	    public void changePasswordImpl(String userId, String password) throws GranException {
 	        
 	    }
 	}
 
