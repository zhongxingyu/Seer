 /* 
  * Copyright 2008 The Corner Team.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package corner.internal.services;
 
 
 import org.apache.tapestry5.services.Cookies;
import org.slf4j.Logger;
 
 import corner.services.FlashFacade;
 
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

 /**
  * 实现flash消息机制
  * @author <a href="mailto:jun.tsai@ganshane.net">Jun Tsai</a>
  * @version $Revision: 158 $
  * @since 0.0.1
  */
 public class FlashFacadeImpl implements FlashFacade {
 	private Cookies cookies;
    private Logger logger;
 
    public FlashFacadeImpl(Cookies cookies, Logger logger){
 		this.cookies = cookies;
        this.logger = logger;
 	}
 
 	/**
 	 */
 	public String get(String key) {
 		String v = this.cookies.readCookieValue(key);
        try {
            if(v!=null)
                v=URLDecoder.decode(v,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.warn("cant' support utf-8",e);

        }
        this.cookies.removeCookieValue(key);
 		return v;
 	}
 
 
     /**
      * 
      * @param key key的键值
      * @param value 对应的消息值
      */
 	public void push(String key, String value) {
        try {
            cookies.writeCookieValue(key, URLEncoder.encode(value,"UTF-8"), 30);
        } catch (UnsupportedEncodingException e) {
            cookies.writeCookieValue(key, value, 30);
        }
 	}
     public void notice(String message) {
         this.push(NOTICE_KEY,message);
     }
 
     public void warn(String message) {
         this.push(WARN_KEY,message);
     }
 
     public void error(String message) {
         this.push(ERROR_KEY,message);
     }
 
 }
