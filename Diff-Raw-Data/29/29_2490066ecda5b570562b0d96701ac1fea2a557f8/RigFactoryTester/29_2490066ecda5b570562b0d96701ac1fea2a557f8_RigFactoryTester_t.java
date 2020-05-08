 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
 * @author Michael Diponio (mdiponio)
 * @date 3rd December 2009
  *
  * Changelog:
  * - 02/12/2009 - mdiponio - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.type.tests;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.reset;
 import static org.easymock.EasyMock.verify;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRig;
 import au.edu.uts.eng.remotelabs.rigclient.type.RigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Tests the rig factory class.
  */
 public class RigFactoryTester extends TestCase
 {
     /** Mock configuration. */
     private IConfig mockConfig;
     
     /**
      * @throws java.lang.Exception
      */
     @Override
     @Before
     public void setUp() throws Exception
     {
         this.mockConfig = createMock(IConfig.class);
         expect(this.mockConfig.getProperty("Log_Level"))
         	.andReturn("DEBUG");
         expect(this.mockConfig.getProperty("Logger_Type"))
             .andReturn("SystemErr");
         replay(this.mockConfig);
         
         ConfigFactory.getInstance();
         Field f = ConfigFactory.class.getDeclaredField("instance");
         f.setAccessible(true);
         f.set(null, this.mockConfig);
         
         /* Force reading of logger config. */
         LoggerFactory.getLoggerInstance();
     }
 
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.type.RigFactory#getRigInstance()}.
      */
     @Test
     public void testGetRigInstance()
     {
         reset(this.mockConfig);
         expect(this.mockConfig.getProperty("Rig_Class"))
             .andReturn("au.edu.uts.eng.remotelabs.rigclient.type.tests.MockIRig");
         replay(this.mockConfig);
         
         Object rig = RigFactory.getRigInstance();
         assertNotNull(rig);
         assertTrue(rig instanceof IRig);
         
         verify(this.mockConfig);
     }
 
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.type.RigFactory#getRigInstance()}.
      */
     @Test
     public void testGetRigInstanceNotFound()
     {
         reset(this.mockConfig);
         expect(this.mockConfig.getProperty("Rig_Class"))
             .andReturn("au.edu.uts.eng.remotelabs.rigclient.type.tests.DoesNotExist");
         replay(this.mockConfig);
         
         try
         {
             Method meth = RigFactory.class.getDeclaredMethod("loadInstance");
             meth.setAccessible(true);
             Object obj  = meth.invoke(null);
             assertNull(obj);
         }
         catch (Exception e)
         {
             fail("Exception: " + e.getClass().getCanonicalName() + ", Message: " + e.getMessage());
         }
 
         verify(this.mockConfig);
     }
     
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.type.RigFactory#getRigInstance()}.
      */
     @Test
     public void testGetRigInstanceWrongType()
     {
         reset(this.mockConfig);
         expect(this.mockConfig.getProperty("Rig_Class"))
             .andReturn("au.edu.uts.eng.remotelabs.rigclient.type.tests.WrongType");
         replay(this.mockConfig);
         
         try
         {
             Method meth = RigFactory.class.getDeclaredMethod("loadInstance");
             meth.setAccessible(true);
             Object obj  = meth.invoke(null);
             assertNull(obj);
         }
         catch (Exception e)
         {
             fail("Exception: " + e.getClass().getCanonicalName() + ", Message: " + e.getMessage());
         }
 
         verify(this.mockConfig);
     }
 }
