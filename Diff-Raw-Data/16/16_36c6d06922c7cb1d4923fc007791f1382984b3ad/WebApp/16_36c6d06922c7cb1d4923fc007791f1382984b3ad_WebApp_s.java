 /**
  * Copyright to the original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at http://www.apache.org/licenses/LICENSE-2.0.
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.hbs.neo4j.jneovigator;
 
 import static com.google.common.collect.Iterables.getFirst;
 import static java.util.Arrays.asList;
 
import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.InetSocketAddress;
 import java.util.List;
 
 import org.simpleframework.http.Request;
 import org.simpleframework.http.Response;
 import org.simpleframework.http.core.Container;
 import org.simpleframework.transport.connect.SocketConnection;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import com.google.common.base.Charsets;
import com.google.common.io.Files;
 import com.google.common.util.concurrent.AbstractService;
 
 /**
  * @author Christophe Heubès
  */
 public class WebApp extends AbstractService implements Container {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(WebApp.class);
 
 	private static final int HTTP_404 = 404;
 
 	private int port;
 	private NeovigatorProvider neovigatorProvider;
 	private SocketConnection socketConnection;
 
 	/**
 	 * @param webServerPort Port the web server will be listening to.
 	 * @param nodeProvider JSON node provider.
 	 */
 	public WebApp(int webServerPort, NeovigatorProvider nodeProvider) {
 		this.port = webServerPort;
 		this.neovigatorProvider = nodeProvider;
 	}
 
 	@Override
 	public void handle(Request request, Response response) {
 		String pathAsString = request.getPath().toString();
 		LOGGER.debug(pathAsString);
 		List<String> path = asList(request.getPath().getSegments());
 		try {
 			PrintStream body = response.getPrintStream();
 			long time = System.currentTimeMillis();
 			response.set("Server", "JNeovigator/1.0 (Simple 4.0)");
 			response.setDate("Date", time);
 			response.setDate("Last-Modified", time);
 
			StringBuilder fileToServePath = new StringBuilder("web-app");
 			if ("/resources/show".equalsIgnoreCase(pathAsString)) {
 				String nodeId = request.getParameter("id");
 				body.println(neovigatorProvider.getNodeAsNeovigatorJson(new Long(nodeId).longValue()));
 			} else {
 				String firstSegment = getFirst(path, "/");
 				if ("/".equalsIgnoreCase(firstSegment)) {
 					fileToServePath.append("/index.html");
 				} else {
 					fileToServePath.append(pathAsString);
 				}
 				try {
 
 					if (pathAsString.endsWith(".css")) {
 						response.set("Content-Type", "text/css");
 					} else if (pathAsString.endsWith(".js")) {
 						response.set("Content-Type", "application/javascript");
 					} else if (pathAsString.endsWith(".pjs")) {
 						response.set("Content-Type", "application/javascript");
 					} else if (pathAsString.endsWith(".otf")) {
 						response.set("Content-Type", "font/opentype");
 					}
 
					String fileToServe = Files.toString(new File(fileToServePath.toString()), Charsets.UTF_8);
 					body.println(fileToServe);
 				} catch (FileNotFoundException fnfe) {
 					response.setCode(HTTP_404);
 				}
 			}
 
 			body.close();
 		} catch (IOException ioe) {
 			LOGGER.error("Unable to handle request on " + request.getPath().toString() + ".", ioe);
 		}
 	}
 
 	@Override
 	protected void doStart() {
 		LOGGER.info("Running JNeovigator Web App...");
 		try {
 			socketConnection = new SocketConnection(this);
 			socketConnection.connect(new InetSocketAddress(port));
 			// neovigatorProvider = new NeovigatorProvider(graphDb);
 			LOGGER.info("JNeovigator Web App is running on port " + port + ".");
 			notifyStarted();
 		} catch (IOException ioe) {
 			LOGGER.error("Unable to start JNeovigator Web App.", ioe);
 			notifyFailed(ioe);
 		}
 	}
 
 	@Override
 	protected void doStop() {
 		LOGGER.info("Tearing down JNeovigator Web App...");
 		try {
 			socketConnection.close();
 			LOGGER.info("JNeovigator Web App torn down.");
 			notifyStopped();
 		} catch (IOException ioe) {
 			LOGGER.error("Unable to stop JNeovigator Web App.", ioe);
 			notifyFailed(ioe);
 		}
 	}
 
 }
