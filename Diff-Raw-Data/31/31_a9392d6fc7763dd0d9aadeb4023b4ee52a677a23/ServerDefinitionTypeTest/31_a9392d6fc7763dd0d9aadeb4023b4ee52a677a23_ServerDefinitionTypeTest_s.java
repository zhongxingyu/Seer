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
 
import java.util.Iterator;
import java.util.List;
 import junit.framework.TestCase;
 import org.eclipse.jst.server.generic.core.CorePlugin;
 import org.eclipse.jst.server.generic.internal.core.ServerTypeDefinitionManager;
import org.eclipse.jst.server.generic.internal.xml.ServerTypeDefinition;
 
 /**
  * @author naci
  * 
  * TODO To change the template for this generated type comment go to Window -
  * Preferences - Java - Code Style - Code Templates
  */
 public class ServerDefinitionTypeTest extends TestCase {
 
 	/*
 	 * @see TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
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
 	public ServerDefinitionTypeTest(String name) {
 		super(name);
 	}
 
 	public void testGetTypes() {
 		ServerTypeDefinitionManager serverTypeDefinitionManager = CorePlugin
 				.getDefault().getServerTypeDefinitionManager();
 		assertNotNull(serverTypeDefinitionManager);
		ServerTypeDefinition[] types = serverTypeDefinitionManager
 				.getServerTypeDefinitions();
 		assertNotNull(types);
 		assertEquals(3, types.length);
 	}
 
 	public void testResolve() {
 		ServerTypeDefinitionManager serverTypeDefinitionManager = CorePlugin
 				.getDefault().getServerTypeDefinitionManager();
 		assertNotNull(serverTypeDefinitionManager);
		ServerTypeDefinition[] types = serverTypeDefinitionManager
 				.getServerTypeDefinitions();
 		assertNotNull(types);
 		assertTrue(types.length > 0);
 		for (int i = 0; i < types.length; i++) {
			ServerTypeDefinition definition = types[i];

			definition.setPropertyValue("classPath", "/home/test/nowhere");
			List aList = definition.getServerClassPath();
			assertNotNull(aList);
			Iterator iterator = aList.iterator();
			while (iterator.hasNext()) {
				String cpItem = (String) iterator.next();
				assertNotNull(cpItem);
				assertTrue(cpItem.indexOf("/home/test/nowhere") >= 0);
				}
			}
 	}
 }
