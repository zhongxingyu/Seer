 /**
  * The MIT License
  *
  * Original work sponsored and donated by National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  *
  * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package dk.nsi.minlog.export.config;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 
 import com.splunk.Service;
 
 @Configuration
@ComponentScan({"dk.nsi.minlog.export.dao.splunk"})
 public class SplunkConfig {
 	static final Logger logger = Logger.getLogger(SplunkConfig.class);
 
 	@Value("${minlog.splunk.host}")
 	String host;
 
 	@Value("${minlog.splunk.port}")
 	Integer port;
 	
 	@Value("${minlog.splunk.user}")
 	String user;
 	
 	@Value("${minlog.splunk.password}")
 	String password;
 	
 	@Value("${minlog.splunk.schema}")
 	String schema;
 	
 	@Bean
 	public Service splunkService(){
 		logger.debug("Creating Splunk service with host=" + host + " port=" + port + " schema=" + schema + " user=" + user);
 		Map<String, Object> args = new HashMap<String, Object>();
 		args.put("host", host);
 		args.put("port", port);
 		args.put("schema", schema);
 		
 		Service service = new Service(args);
 		service.login(user, password);
 		
 		return service;
 	}
 }
