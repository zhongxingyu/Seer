 /*******************************************************************************
  * Copyright (c) 2004 Eteration Bilisim A.S.
  * All rights reserved.  This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     Naci M. Dai - initial API and implementation
  * 
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL ETERATION A.S. OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  *
  * This software consists of voluntary contributions made by many
  * individuals on behalf of the Eteration Bilisim A.S.  For more
  * information on eteration, please see
  * <http://www.eteration.com/>.
  ***************************************************************************/
 
 package org.eclipse.jst.server.generic.tests;
 
 import java.util.HashMap;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jst.server.generic.core.internal.GenericServerRuntime;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.IRuntimeType;
 import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
 import org.eclipse.wst.server.core.IServerType;
 import org.eclipse.wst.server.core.IServerWorkingCopy;
 import org.eclipse.wst.server.core.ServerCore;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.internal.RuntimeType;
 
 public class ServerCreationTest extends TestCase {
 
 	RuntimeType j2eeRuntimeType = null;
	private final static String ID = "org.eclipse.jst.server.generic.jonas414";
 
 	/*
 	 * @see TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 		IRuntimeType[] listAll = ServerCore.getRuntimeTypes();
 		if (listAll != null) {
 			for (int i = 0; i < listAll.length; i++) {
 				IRuntimeType runtimeType = listAll[i];
 
 				if ("J2EE Runtime Library".equals(runtimeType.getName()))
 					j2eeRuntimeType = (RuntimeType) runtimeType;
 			}
 		}
 	}
 
 	/*
 	 * @see TestCase#tearDown()
 	 */
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	/**
 	 * Constructor for PluginIntegrityTest.
 	 * 
 	 * @param name
 	 */
 	public ServerCreationTest(String name) {
 		super(name);
 	}
 
 	public void testGetTypes() {
 		assertNotNull(j2eeRuntimeType);
 	}
 
 	public void testCreateServer() throws Exception {
 		
 		// Finds the generic server type
 		IServerType[] sTypes = ServerCore.getServerTypes();
 		IServerType serverType = null;
 		for (int i = 0; i < sTypes.length; i++) {
 			IServerType sType = sTypes[i];
 			if (ID.equals(sType.getId()))
 				serverType = sType;
 		}
		assertNotNull("Could not find org.eclipse.jst.server.generic.jonas414 server type",serverType);
 
 		//Finds the generic server runtime type
 		IRuntimeType runtimeType = serverType.getRuntimeType();
 		assertNotNull("Could not find runtime type for the generic server type",runtimeType);
 		
 	
 		//Create a new server instance from the type
 		IServerWorkingCopy server = serverType.createServer(ID+".Jonas.Server", null,
 				(IRuntime) null, null);
 		assertNotNull("Could not create server",server);
 
 		
 		//Create a new runtime instance from the type
 		IRuntime runtime  = runtimeType.createRuntime(ID+".Jonas.Runtime",null);
 
 		assertNotNull("Could not create runtime",runtime);
 	
 		
 		//Set the runtime for the server
 		server.setRuntime(runtime);
 		
 		//Save the server
 		server.save(false,null);
 		
 		// Set properties for the runtime
 		IRuntimeWorkingCopy runtimeWorkingCopy = runtime.createWorkingCopy();
 		assertNotNull("Could not create runtime working copy",runtimeWorkingCopy);
 		
 		// Set the JONAS runtime as the default runtime
 		ServerUtil.setRuntimeDefaultName(runtimeWorkingCopy);
 		assertNotNull("Runtime working copy has no name",runtimeWorkingCopy.getName());
 		
 		// Set properties for the JONAS runtime
 		GenericServerRuntime runtimeDelegate = (GenericServerRuntime)runtimeWorkingCopy.loadAdapter(GenericServerRuntime.class,new NullProgressMonitor());
 		assertNotNull("Could not obtain runtime delegate",runtimeDelegate);
 		
 		HashMap props = new HashMap();
 		props.put("mappernames", "");
 		props.put("classPathVariableName", "JONAS");
 		props.put("serverAddress", "127.0.0.1");
 		props.put("jonasBase", "C:\\nmd\\dev\\java\\appservers\\JOnAS-4.1.4");
 		props.put("jonasRoot", "C:\\nmd\\dev\\java\\appservers\\JOnAS-4.1.4");
 		props.put("classPath", "C:\\nmd\\dev\\java\\appservers\\JOnAS-4.1.4");
 		props.put("protocols", "C:\\nmd\\dev\\java\\appservers\\JOnAS-4.1.4");
 		props.put("port", "9000");		
 	    runtimeDelegate.setServerInstanceProperties(props);
 	    
 	    //Save the runtime working copy 
 		runtimeWorkingCopy.save(false,null);
 		
 	}
 
 	
 
 
 	public void testGetWebTypes() {
 
 		IRuntimeType listWeb[] = ServerUtil.getRuntimeTypes("j2ee.web", null);
 		assertNotNull(listWeb);
 
 		boolean found = listWeb.length>0;
 		assertTrue(found);
 	}
 
 }
