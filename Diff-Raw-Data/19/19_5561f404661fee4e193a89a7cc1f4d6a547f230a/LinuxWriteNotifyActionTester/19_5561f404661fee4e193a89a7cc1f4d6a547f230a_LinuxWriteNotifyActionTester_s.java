 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
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
  * @date 12th March 2010
  */
 package au.edu.uts.eng.remotelabs.rigclient.action.notify.tests;
 
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 
 import java.lang.reflect.Field;
 
 import junit.framework.TestCase;
 
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 
 import au.edu.uts.eng.remotelabs.rigclient.action.notify.LinuxWriteNotifyAction;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 
 /**
  * Tests the {@link LinuxWriteNotifyAction} class.
  */
 public class LinuxWriteNotifyActionTester extends TestCase
 {
     /** Object of class under test. */
     private LinuxWriteNotifyAction writeAction;
     
     /** Mock configuration. */
     private IConfig mockConfig;
 
     @Override
     @Before
     public void setUp() throws Exception
     {
         this.mockConfig = EasyMock.createMock(IConfig.class);
         expect(this.mockConfig.getProperty("Logger_Type")).andReturn("SystemErr");
         expect(this.mockConfig.getProperty("Log_Level")).andReturn("DEBUG");
         expect(this.mockConfig.getProperty("Default_Log_Format", "[__LEVEL__] - [__ISO8601__] - __MESSAGE__"))
                 .andReturn("[__LEVEL__] - [__ISO8601__] - __MESSAGE__");
         expect(this.mockConfig.getProperty("FATAL_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("PRIORITY_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("ERROR_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("WARN_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("INFO_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("DEBUG_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("Linux_Write_Command")).andReturn(null);
         replay(this.mockConfig);
 
         final Field configField = ConfigFactory.class.getDeclaredField("instance");
         configField.setAccessible(true);
         configField.set(null, this.mockConfig);
         
         if (System.getProperty("os.name").startsWith("Linux"))
         {
             this.writeAction = new LinuxWriteNotifyAction();
         }
     }
     
     @Test
     public void testWrite()
     {
         if (this.writeAction == null) return;
         
         assertTrue(this.writeAction.notify("Write Message", new String[] {"mdiponio", "tmachet", "teja"}));
     }
     
     @Test
     public void testGetType()
     {
         if (this.writeAction == null) return;
         assertEquals("Linux write notify action", this.writeAction.getActionType());
     }
     
 
 }
