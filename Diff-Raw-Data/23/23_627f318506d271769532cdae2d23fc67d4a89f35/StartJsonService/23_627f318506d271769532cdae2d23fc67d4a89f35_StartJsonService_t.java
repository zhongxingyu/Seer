 package jp.co.nttcom.eai.webadmin.service.apps;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
import javax.servlet.http.HttpServletResponse;
 import jp.co.nttcom.eai.webadmin.client.RestClient;
 import jp.co.nttcom.eai.webadmin.config.AppConfig;
 import jp.co.nttcom.eai.webadmin.service.AbstractJsonService;
 import jp.co.nttcom.eai.webadmin.service.ServiceException;
 
 /**
  * <p>[概 要] アプリケーション起動クラス。</p>
  * <p>[詳 細] 指定したアプリケーションを起動します。</p>
  * <p>[備 考] </p>
  * <p>[環 境] JavaSE 6.0</p>
  *
  * @author Seiji Sogabe
  */
 public class StartJsonService extends AbstractJsonService {
 
    public HttpServletResponse response;
    
     /**
      * <p>[概 要] アプリケーション起動処理</p>
      * <p>[詳 細] 指定されたアプリケーションに該当するサーバにアクセスし、起動します。
      * </p>
      * <p>クエリーパラメータ<code>params</code>には、アプリケーション名(
      * <code>id</code>)を 指定する必要があります。
      * </p>
      *
      * @param url 接続先URL
      * @param params クエリーパラメータ
      * @return レスポンス
      * @throws IOException 入出力エラー
      */
    public String[] create(Map<String, String> map) throws ServiceException, IOException {
         // 接続先アプリケーション名
         String id = map.get("id");
         if (id == null) {
             throw new IllegalArgumentException();
         }
         
         Map<String, String> data = new HashMap<String, String>();
         data.put("id", id);
 
         // WebadminConfigs取得
         AppConfig appConfig = getAppConfig(id);
         RestClient.doPost(appConfig.getUrl() + "apps/start.json", Collections.EMPTY_MAP, data);
        return new String[] { "success" };
     }
 }
