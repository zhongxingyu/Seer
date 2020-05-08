 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.palava.jobs.captcha;
 
 import java.io.ByteArrayInputStream;
 import java.util.Map;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 import de.cosmocode.palava.bridge.MimeType;
 import de.cosmocode.palava.bridge.Server;
 import de.cosmocode.palava.bridge.call.Call;
 import de.cosmocode.palava.bridge.command.Job;
 import de.cosmocode.palava.bridge.command.Response;
 import de.cosmocode.palava.bridge.content.StreamContent;
 import de.cosmocode.palava.bridge.session.HttpSession;
 import de.cosmocode.palava.captcha.CaptchaService;
 import de.cosmocode.palava.captcha.Create;
 
 /**
  * Creates a captcha image and returns the binary image data
  * to the browser.
  * 
  * @deprecated use {@link Create} instead
  *
  * @author Willi Schoenborn
  */
 @Deprecated
 @Singleton
 public class createCaptcha implements Job {
 
     @Inject
     private CaptchaService captcha;
     
     @Override
     public void process(Call request, Response response, HttpSession session,
             Server server, Map<String, Object> caddy) throws  Exception {
 
        final byte[] bytes = captcha.getCaptcha(session.getSessionId());
         
         final ByteArrayInputStream input = new ByteArrayInputStream(bytes);
         final StreamContent content = new StreamContent(input, bytes.length, MimeType.JPEG);
         
         response.setContent(content);
     }
 
 }
