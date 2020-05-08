 /*
  * Copyright 2010 Softgress - http://www.softgress.com/
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
 
 package sim.server;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.OutputStream;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sim.data.Metrics;
 import sim.server.storage.StorageWriter;
 
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 
 /**
  * @author mcq
  * 
  */
 @SuppressWarnings("restriction")
 public class ServerHttpHandler implements HttpHandler {
 
 	private static final Logger logger = LoggerFactory.getLogger(ServerHttpHandler.class);
 	
	private static final int MAX_METRICS_READ = 100;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.sun.net.httpserver.HttpHandler#handle(com.sun.net.httpserver.HttpExchange
 	 * )
 	 */
 	@Override
 	public void handle(HttpExchange xchg) throws IOException {
 		ObjectInputStream ois = new ObjectInputStream(xchg.getRequestBody());
 		Object o = null;
 		try {
 			int count = 0;
 			while (true) {
 				if (count == MAX_METRICS_READ) {
 					logger.info("max object read of " + MAX_METRICS_READ + " was reached, closing connection");
 					break;
 				}
 				try {
 					o = ois.readObject();
 					count++;
 				} catch (EOFException e) {
 					logger.debug("no more data to read, closing connection ...");
 					break;
 				}
 				if (o instanceof Metrics) {
 					logger.info(o.toString());
 					StorageWriter.addMeasurement((Metrics) o);
 				}
 			}
 		} catch (ClassNotFoundException e) {
 			logger.error("class not found", e);
 			throw new RuntimeException("class not found", e);
 		}
 		xchg.sendResponseHeaders(200, "SUCCESS".length());
 		OutputStream os = xchg.getResponseBody();
 		os.write("SUCCESS".getBytes());
 		os.close();
 	}
 
 }
