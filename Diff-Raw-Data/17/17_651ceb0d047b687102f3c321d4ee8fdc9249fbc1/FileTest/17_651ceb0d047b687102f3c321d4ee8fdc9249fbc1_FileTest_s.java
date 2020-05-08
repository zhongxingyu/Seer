 /*
  * Copyright 2005-2006 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.components.file;
 
 import java.io.File;
 
 import javax.xml.namespace.QName;
 
 import org.apache.servicemix.jbi.util.FileUtil;
 import org.apache.servicemix.tck.TestSupport;
 import org.springframework.context.support.AbstractXmlApplicationContext;
 import org.xbean.spring.context.ClassPathXmlApplicationContext;
 
 /**
  * @version $Revision$
  */
 public class FileTest extends TestSupport {
 
 	protected void setUp() throws Exception {
		super.setUp();
 		FileUtil.deleteFile(new File("target/test-data/file"));
 	}
 	
     public void testSendMessagesToFileSystemThenPoollThem() throws Exception {
         QName service = new QName("http://servicemix.org/cheese/", "fileSender");
 
         assertSendAndReceiveMessages(service);
     }
 
     protected AbstractXmlApplicationContext createBeanFactory() {
         return new ClassPathXmlApplicationContext("org/apache/servicemix/components/file/example.xml");
     }
 }
