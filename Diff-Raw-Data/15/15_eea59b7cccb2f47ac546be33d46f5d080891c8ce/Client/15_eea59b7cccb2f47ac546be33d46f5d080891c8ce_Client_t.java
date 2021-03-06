 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package hsoa_1;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.apache.servicemix.util.FileUtil;
 
 public class Client {
 	public static void main(String[] args) {
 		try {
			new Client().sendRequest("http://149.156.97.217:80/SensorsService");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void sendRequest(String addr) throws Exception {
 		System.out.println("Trying to connect to " + addr);
 		URLConnection connection = new URL(addr).openConnection();
 		connection.setConnectTimeout(5000);
 		connection.setDoInput(true);
 		connection.setDoOutput(true);
 		OutputStream os = connection.getOutputStream();
 		// Post the request file.
 		InputStream fis = getClass().getClassLoader().getResourceAsStream("hsoa_1/request.xml");
 		FileUtil.copyInputStream(fis, os);
 		// Read the response.
 		InputStream is = connection.getInputStream();
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		FileUtil.copyInputStream(is, baos);
 		System.out.println("the response is =====>");
 		System.out.println(baos.toString());
 	}
 
 }
