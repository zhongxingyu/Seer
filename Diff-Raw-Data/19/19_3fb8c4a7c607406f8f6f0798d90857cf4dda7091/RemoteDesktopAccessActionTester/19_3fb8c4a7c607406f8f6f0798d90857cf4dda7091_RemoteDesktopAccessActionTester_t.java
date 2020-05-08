 /**
  * SAHARA Rig Client
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  * 
  * @license See LICENSE in the top level directory for complete license terms.
  *          Copyright (c) 2010, University of Technology, Sydney
  *          All rights reserved.
  *          Redistribution and use in source and binary forms, with or without
  *          modification, are permitted provided that the following conditions are met:
  *          * Redistributions of source code must retain the above copyright notice,
  *          this list of conditions and the following disclaimer.
  *          * Redistributions in binary form must reproduce the above copyright
  *          notice, this list of conditions and the following disclaimer in the
  *          documentation and/or other materials provided with the distribution.
  *          * Neither the name of the University of Technology, Sydney nor the names
  *          of its contributors may be used to endorse or promote products derived from
  *          this software without specific prior written permission.
  *          THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  *          AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  *          IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *          DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
  *          FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  *          DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  *          SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *          CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  *          OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  *          OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * @author <First> <Last> (tmachet)
  * @date <Day> <Month> 2010
  *       Changelog:
  *       - 19/02/2010 - tmachet - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.action.access.tests;
 
 import static org.easymock.EasyMock.reset;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 import org.easymock.EasyMock;
 
 import au.edu.uts.eng.remotelabs.rigclient.action.access.RemoteDesktopAccessAction;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * @author tmachet
  */
@Deprecated
 public class RemoteDesktopAccessActionTester extends TestCase
 {
     /** Object of class under test. */
     private RemoteDesktopAccessAction action;
 
     /** Mock configuration class. */
     private IConfig mockConfig;
 
     @Override
     protected void setUp() throws Exception
     {
         this.mockConfig = EasyMock.createMock(IConfig.class);
         EasyMock.expect(this.mockConfig.getProperty("Remote_Desktop_Windows_Domain","")).andReturn("");
         EasyMock.expect(this.mockConfig.getProperty("Logger_Type")).andReturn("SystemErr");
         EasyMock.expect(this.mockConfig.getProperty("Log_Level")).andReturn("DEBUG");
         EasyMock.expect(this.mockConfig.getProperty("Default_Log_Format", "[__LEVEL__] - [__ISO8601__] - __MESSAGE__"))
                 .andReturn("[__LEVEL__] - [__ISO8601__] - __MESSAGE__");
         EasyMock.expect(this.mockConfig.getProperty("FATAL_Log_Format")).andReturn(null);
         EasyMock.expect(this.mockConfig.getProperty("PRIORITY_Log_Format")).andReturn(null);
         EasyMock.expect(this.mockConfig.getProperty("ERROR_Log_Format")).andReturn(null);
         EasyMock.expect(this.mockConfig.getProperty("WARN_Log_Format")).andReturn(null);
         EasyMock.expect(this.mockConfig.getProperty("INFO_Log_Format")).andReturn(null);
         EasyMock.expect(this.mockConfig.getProperty("DEBUG_Log_Format")).andReturn(null);
         EasyMock.replay(this.mockConfig);
 
         final Field configField = ConfigFactory.class.getDeclaredField("instance");
         configField.setAccessible(true);
         configField.set(null, this.mockConfig);
 
         LoggerFactory.getLoggerInstance();
         final String os = System.getProperty("os.name");
         if (os.startsWith("Windows"))
         {
             this.action = new RemoteDesktopAccessAction();
         }
         else
         {
             this.action = null;
         }
             
     }
 
     /*
      * (non-Javadoc)
      * @see junit.framework.TestCase#tearDown()
      */
     @Override
     protected void tearDown() throws Exception
     {
         super.tearDown();
         final List<String> comm = new ArrayList<String>();
         comm.add(RemoteDesktopAccessAction.DEFAULT_COMMAND);
         comm.add(RemoteDesktopAccessAction.DEFAULT_LOCALGROUP);
         comm.add(RemoteDesktopAccessAction.DEFAULT_GROUPNAME);
         comm.add("/DELETE");
         comm.add("tmachet");
         final String os = System.getProperty("os.name");
 
         if (os.startsWith("Windows"))
         {
             final Method me = RemoteDesktopAccessAction.class.getDeclaredMethod("executeCommand", List.class);
             me.setAccessible(true);
             me.invoke(this.action, comm);
         }
     }
 
     /**
      * Test method for
      * {@link au.edu.uts.eng.remotelabs.rigclient.action.access.RemoteDesktopAccessAction#RemoteDesktopAccessAction()}.
      * 
      * @throws NoSuchFieldException
      * @throws xception
      */
     public void testRemoteDesktopAccessAction() throws Exception
     {
         if (this.action != null)
         {
             reset(this.mockConfig);
             EasyMock.expect(this.mockConfig.getProperty("Remote_Desktop_Groupname","\"Remote Desktop Users\""))
             .andReturn("Remote Desktop Users");
             EasyMock.replay(this.mockConfig);
             
             Field f = RemoteDesktopAccessAction.class.getDeclaredField("domainName");
             f.setAccessible(true);
             assertNull(f.get(this.action));
         }
     }
 
     /**
      * Test method for
      * {@link au.edu.uts.eng.remotelabs.rigclient.action.access.RemoteDesktopAccessAction#assign(java.lang.String)}.
      * 
      * @throws Exception
      */
     public void testAssign() throws Exception
     {
         if (this.action != null)
         {
             reset(this.mockConfig);
             EasyMock.expect(this.mockConfig.getProperty("Remote_Desktop_Groupname","\"Remote Desktop Users\""))
             .andReturn("Remote Desktop Users");
             EasyMock.replay(this.mockConfig);
             
             final List<String> comm = new ArrayList<String>();
             comm.add(RemoteDesktopAccessAction.DEFAULT_COMMAND);
             comm.add(RemoteDesktopAccessAction.DEFAULT_LOCALGROUP);
             comm.add(RemoteDesktopAccessAction.DEFAULT_GROUPNAME);
             final String name = "tmachet";
             final String os = System.getProperty("os.name");
     
             if (os.startsWith("Windows"))
             {
                 Method me = RemoteDesktopAccessAction.class.getDeclaredMethod("executeCommand", List.class);
                 me.setAccessible(true);
                 Process proc = (Process) me.invoke(this.action, comm);
                 Assert.assertNotNull(proc);
     
                 me = RemoteDesktopAccessAction.class.getDeclaredMethod("isUserInGroup", Process.class, String.class);
                 me.setAccessible(true);
                 Boolean result = (Boolean) me.invoke(this.action, proc, name);
                 Assert.assertFalse(result);
     
                 Boolean assign = this.action.assign(name);
                 Assert.assertTrue(assign);
     
                 me = RemoteDesktopAccessAction.class.getDeclaredMethod("executeCommand", List.class);
                 me.setAccessible(true);
                 proc = (Process) me.invoke(this.action, comm);
                 Assert.assertNotNull(proc);
     
                 me = RemoteDesktopAccessAction.class.getDeclaredMethod("isUserInGroup", Process.class, String.class);
                 me.setAccessible(true);
                 result = (Boolean) me.invoke(this.action, proc, name);
                 Assert.assertTrue(result);
             }
         }
     }
 
     /**
      * Test method for
      * {@link au.edu.uts.eng.remotelabs.rigclient.action.access.RemoteDesktopAccessAction#revoke(java.lang.String)}.
      * 
      * @throws Exception
      */
     public void testRevoke() throws Exception
     {
         if (this.action != null)
         {
             reset(this.mockConfig);
             EasyMock.expect(this.mockConfig.getProperty("Remote_Desktop_Groupname","\"Remote Desktop Users\""))
             .andReturn("Remote Desktop Users");
             EasyMock.replay(this.mockConfig);
     
             final List<String> comm = new ArrayList<String>();
             comm.add(RemoteDesktopAccessAction.DEFAULT_COMMAND);
             comm.add(RemoteDesktopAccessAction.DEFAULT_LOCALGROUP);
             comm.add(RemoteDesktopAccessAction.DEFAULT_GROUPNAME);
             final String name = "tmachet";
             final String os = System.getProperty("os.name");
     
             if (os.startsWith("Windows"))
             {
                 Method me = RemoteDesktopAccessAction.class.getDeclaredMethod("executeCommand", List.class);
                 me.setAccessible(true);
                 Process proc = (Process) me.invoke(this.action, comm);
                 Assert.assertNotNull(proc);
     
                 me = RemoteDesktopAccessAction.class.getDeclaredMethod("isUserInGroup", Process.class, String.class);
                 me.setAccessible(true);
                 Boolean result = (Boolean) me.invoke(this.action, proc, name);
                 Assert.assertFalse(result);
     
                 Boolean assign = this.action.assign(name);
                 Assert.assertTrue(assign);
     
                 me = RemoteDesktopAccessAction.class.getDeclaredMethod("executeCommand", List.class);
                 me.setAccessible(true);
                 proc = (Process) me.invoke(this.action, comm);
                 Assert.assertNotNull(proc);
     
                 me = RemoteDesktopAccessAction.class.getDeclaredMethod("isUserInGroup", Process.class, String.class);
                 me.setAccessible(true);
                 result = (Boolean) me.invoke(this.action, proc, name);
                 Assert.assertTrue(result);
     
                 Boolean revoke = this.action.revoke(name);
                 Assert.assertTrue(revoke);
     
                 me = RemoteDesktopAccessAction.class.getDeclaredMethod("executeCommand", List.class);
                 me.setAccessible(true);
                 proc = (Process) me.invoke(this.action, comm);
                 Assert.assertNotNull(proc);
     
                 me = RemoteDesktopAccessAction.class.getDeclaredMethod("isUserInGroup", Process.class, String.class);
                 me.setAccessible(true);
                 result = (Boolean) me.invoke(this.action, proc, name);
                 Assert.assertFalse(result);
             }
         }
 
     }
  
     /**
      * Test method for
      * {@link au.edu.uts.eng.remotelabs.rigclient.action.access.RemoteDesktopAccessAction#getActionType()}.
      * 
      * @throws Exception
      */
     public void testGetActionType() throws Exception
     {
         if (this.action != null)
         {
             final Method me = RemoteDesktopAccessAction.class.getDeclaredMethod("getActionType");
             me.setAccessible(true);
             final String type = (String) me.invoke(this.action);
     
             Assert.assertEquals(type, "Windows Remote Desktop Access");
         }
     }
 
     /**
      * Test method for
      * {@link au.edu.uts.eng.remotelabs.rigclient.action.access.RemoteDesktopAccessAction#executeCommand()}.
      * 
      * @throws Exception
      */
     public void testExecuteCommand() throws Exception
     {
         if (this.action != null)
         {
             final List<String> comm = new ArrayList<String>();
             comm.add("net");
             comm.add("localgroup");
             comm.add("\"Remote Desktop Users\"");
     
             final String os = System.getProperty("os.name");
     
             if (os.startsWith("Windows"))
             {
                 final Method me = RemoteDesktopAccessAction.class.getDeclaredMethod("executeCommand", List.class);
                 me.setAccessible(true);
                 final Process proc = (Process) me.invoke(this.action, comm);
     
                 Assert.assertNotNull(proc);
             }
         }
     }
 
     /**
      * Test method for
      * {@link au.edu.uts.eng.remotelabs.rigclient.action.access.RemoteDesktopAccessAction#isUserInGroup()}.
      * 
      * @throws Exception
      */
     public void testIsUserInGroup() throws Exception
     {
         if (this.action != null)
         {
             final List<String> comm = new ArrayList<String>();
             comm.add(RemoteDesktopAccessAction.DEFAULT_COMMAND);
             comm.add(RemoteDesktopAccessAction.DEFAULT_LOCALGROUP);
             comm.add(RemoteDesktopAccessAction.DEFAULT_GROUPNAME);
             final String name = "tmachet";
             final String os = System.getProperty("os.name");
     
             if (os.startsWith("Windows"))
             {
                 Method me = RemoteDesktopAccessAction.class.getDeclaredMethod("executeCommand", List.class);
                 me.setAccessible(true);
                 final Process proc = (Process) me.invoke(this.action, comm);
                 Assert.assertNotNull(proc);
     
                 me = RemoteDesktopAccessAction.class.getDeclaredMethod("isUserInGroup", Process.class, String.class);
                 me.setAccessible(true);
                 final Boolean result = (Boolean) me.invoke(this.action, proc, name);
                 Assert.assertFalse(result);
             }
     
         }
     }
  }
