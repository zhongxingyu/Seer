 package info.micdm.munin_client.tasks;
 
 import info.micdm.utils.Log;
 
 import java.io.IOException;
 import java.net.URI;
 
 import org.apache.http.HttpException;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.AuthState;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.client.protocol.RequestTargetAuthentication;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.message.BasicHttpResponse;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.util.EntityUtils;
 
 import android.net.http.AndroidHttpClient;
 import android.os.AsyncTask;
 
 /**
  * Абстрактный загрузчик данных с веб-сервера.
  * @author Mic, 2011
  *
  */
 public abstract class DownloadTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
 
     /**
      * Возвращает адрес страницы, которую надо скачать.
      */
     protected abstract URI _getUri();
     
     /**
      * Возвращает объект для работы с аутентификацией.
      */
     protected AuthState _getAuthState(String userInfo) {
         AuthState state = new AuthState();
         state.setAuthScheme(new BasicScheme());
         state.setAuthScope(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT));
         state.setCredentials(new UsernamePasswordCredentials(userInfo));
         return state;
     }
     
     /**
      * Возвращает контекст, заполненный аутентификационными данными.
      */
     protected BasicHttpContext _getHttpContext(String userInfo) {
         BasicHttpContext context = new BasicHttpContext();
         context.setAttribute(ClientContext.TARGET_AUTH_STATE, _getAuthState(userInfo));
         return context;
     }
     
     /**
      * Навешивает на запрос обработчик для аутентификации.
      */
     protected void _addAuthInterceptor(HttpGet request, String userInfo) throws HttpException, IOException {
         RequestTargetAuthentication interceptor = new RequestTargetAuthentication();
         interceptor.process(request, _getHttpContext(userInfo));
     }
     
     /**
      * Конструирует запрос.
      */
     protected HttpGet _getRequest(URI uri) throws HttpException, IOException {
         HttpGet request = new HttpGet(uri);
         String userInfo = uri.getRawUserInfo();
         if (userInfo != null) {
             _addAuthInterceptor(request, userInfo);
         }
         return request;
     }
     
     /**
      * Загружает данные.
      */
     protected String _downloadData() {
         try {
             URI uri = _getUri();
             if (Log.isEnabled) {
                 Log.debug("downloading page " + uri);
             }
             AndroidHttpClient client = AndroidHttpClient.newInstance("Android Munin Client");
             HttpGet request = _getRequest(uri);
             BasicHttpResponse response = (BasicHttpResponse)client.execute(request);
             String body = EntityUtils.toString(response.getEntity(), "utf8");
            client.close();
             if (Log.isEnabled) {
                 Log.debug("downloaded: " + body.length());
             }
             return body;
         } catch (Exception e) {
             if (Log.isEnabled) {
                 Log.error("failed to download", e);
             }
             return null;
         }
     }
 }
