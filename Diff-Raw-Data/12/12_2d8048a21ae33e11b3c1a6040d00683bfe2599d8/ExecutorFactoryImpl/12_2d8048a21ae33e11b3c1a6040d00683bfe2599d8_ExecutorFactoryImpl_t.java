 /*******************************************************************************
  * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package beans;
 
 import models.ServerNode;
 
 import org.apache.commons.exec.ExecuteWatchdog;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import play.modules.spring.Spring;
 import server.ApplicationContext;
 import server.ProcExecutor;
 import beans.ProcExecutorImpl.ProcessStreamHandler;
 import beans.api.ExecutorFactory;
 
 /**
  * A factory class for generating different process executors.
  * 
  * @author adaml
  *
  */
 public class ExecutorFactoryImpl implements ExecutorFactory {
 	
 	private static Logger logger = LoggerFactory.getLogger( ExecutorFactoryImpl.class );
 	
 	@Override
 	public ProcExecutor getBootstrapExecutor( String key ) {
 
 		logger.info("Creating bootstrap executor.");
 		ProcessStreamHandler streamHandler = new ProcessStreamHandler(key);
 		
 		ExecuteWatchdog watchdog = new ExecuteWatchdog(ApplicationContext.get().conf().cloudify.bootstrapCloudWatchDogProcessTimeoutMillis);
 		ProcExecutor executor = (ProcExecutor) Spring.getBean( "bootstrapExecutor" );
 		executor.setExitValue(0);
 		executor.setWatchdog(watchdog);
 		executor.setStreamHandler(streamHandler);
 		executor.setId(key);
 		return executor ;
 	}
 
 	@Override
 	public ProcExecutor getDeployExecutor( ServerNode server ) {
 	
 		logger.info("Creating deploy executor.");
		ProcessStreamHandler streamHandler = new ProcessStreamHandler(server.getNodeId());
 		
 		ExecuteWatchdog watchdog = new ExecuteWatchdog( ApplicationContext.get().conf().cloudify.bootstrapCloudWatchDogProcessTimeoutMillis );
 		ProcExecutor executor = (ProcExecutor) Spring.getBean( "deployExecutor" );
 		executor.setExitValue(1);
 		executor.setWatchdog(watchdog);
 		executor.setStreamHandler(streamHandler);
 		executor.setId(server.getNodeId());
 		return  executor;
 	}
 
 
 
 }
