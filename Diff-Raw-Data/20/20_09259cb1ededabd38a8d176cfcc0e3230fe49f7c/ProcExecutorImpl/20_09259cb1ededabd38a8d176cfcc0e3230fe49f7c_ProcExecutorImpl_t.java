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
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import models.ServerNode;
 
 import org.apache.commons.exec.DefaultExecutor;
 import org.apache.commons.exec.PumpStreamHandler;
 
 import play.cache.Cache;
 import play.modules.spring.Spring;
 import server.DeployManager;
 import server.ProcExecutor;
 import server.ProcOutputStream;
 import server.WriteEventListener;
 
 
 /**
  * This class extends a {@link DefaultExecutor} and provides the ability to listen on the process output stream.
  * an Id property is saved for accessing the output from the play cache. 
  * 
  * @author Igor Goldenberg
  * @author Adaml
  * @see DeployManager
  */
 public class ProcExecutorImpl extends DefaultExecutor implements ProcExecutor 
 {
     private String id;
 
     final static class ProcessStreamHandler extends PumpStreamHandler
 	 {
 		private WriteEventListener writeEventListener;
 		
 		public ProcessStreamHandler(String key) {
 			WriteEventListener writeEventListener = createWriteEventListener(key);
 			this.setWriteEventListener(writeEventListener);
 		}
 		
 		@Override
 		protected void createProcessOutputPump(InputStream is, OutputStream os)
 		{
 			ProcOutputStream procOutputStream = createOutputStream();
 			super.createProcessOutputPump(is, procOutputStream);
 		}
 		
 		
 		@Override
 		protected void createProcessErrorPump(InputStream is, OutputStream os)
 		{
 			ProcOutputStream procOutputStream = createOutputStream();
 			super.createProcessErrorPump(is, procOutputStream);
 		}
 		
 		/**
 		 * 
 		 * enables the option to listen on the process output stream.
 		 * 
 		 * @param wel see {@link WriteEventListener}
 		 */
 		public void setWriteEventListener(final WriteEventListener wel) {
 			this.writeEventListener = wel;
 		}
 		
 		private ProcOutputStream createOutputStream() {
 			ProcOutputStream procOutputStream = new ProcOutputStream();
 			procOutputStream.setProcEventListener(this.writeEventListener);
 			return procOutputStream;
 		}
 		
 		private WriteEventListener createWriteEventListener(String key) {
 			WriteEventListener writeEventListener = ( WriteEventListener  ) Spring.getBean("executorFactoryWriteEventListener");
 			writeEventListener.setKey(key);
 			return writeEventListener;
 		}
 	 }
     
     public ProcExecutorImpl() { }
 
     public ProcExecutorImpl( ServerNode server, File recipe, String... args )
     {
        this.id = server.getId().toString();
         
         Cache.set( "output-" + this.id,  new StringBuilder());
     }
 	
 	@Override
     public String getId()
     {
         return id;
     }
 	
 	@Override
 	public void setId(String id) {
 		this.id = id;
 	}
 
 }
