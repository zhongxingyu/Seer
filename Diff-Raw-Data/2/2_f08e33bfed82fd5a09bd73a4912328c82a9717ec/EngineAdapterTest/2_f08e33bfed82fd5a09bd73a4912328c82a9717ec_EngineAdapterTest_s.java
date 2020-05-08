 /******************************************************************************
     Copyright (c) 2009 Flextao
 
     Permission is hereby granted, free of charge, to any person obtaining a copy
     of this software and associated documentation files (the "Software"), to deal
     in the Software without restriction, including without limitation the rights
     to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
     copies of the Software, and to permit persons to whom the Software is
     furnished to do so, subject to the following conditions:
 
     The above copyright notice and this permission notice shall be included in
     all copies or substantial portions of the Software.
 
     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
     THE SOFTWARE.
 ***********************************************************************/
 
 package com.flextao.jruote;
 
 import static com.flextao.jruote.Helpers.read;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.jruby.Ruby;
 import org.jruby.javasupport.JavaEmbedUtils;
 import org.jruby.runtime.builtin.IRubyObject;
 import org.junit.After;
 import org.junit.Ignore;
 import org.junit.Test;
 
public class EntineAdapterTest {
 
     private EngineAdapter engine;
 
     @After
     public void clearLoggerParticipant() {
         LoggerParticipant.clear();
         if (engine != null) {
             engine.stop();
         }
     }
 
     @Test
     public void runEngineWithJavaParticipant() throws IOException {
         InputStream engineDefinition = getClass().getResourceAsStream("/engine_with_running_a_simple_process.rb");
         new EngineAdapter(engineDefinition);
         assertEquals("consumed participant[alice];consumed participant[bob];", LoggerParticipant.log);
     }
 
     @Test
     public void registerSyncJavaParticipant() throws IOException, InterruptedException {
         InputStream engineDefinition = getClass().getResourceAsStream("/simple_engine.rb");
         URL processDefinition = getClass().getResource("/simplest_process_definition.rb");
 
         engine = new EngineAdapter(engineDefinition);
         engine.registerSyncParticipant("alice", new LoggerParticipant("alice"));
         engine.registerSyncParticipant("bob", new LoggerParticipant("bob"));
         engine.ready();
         engine.launch(processDefinition);
         sleepAMoment();
         assertEquals("consumed participant[alice];consumed participant[bob];", LoggerParticipant.log);
     }
 
     @Test
     public void runEngineAsAsynParticipant() throws IOException, InterruptedException {
         InputStream engineDefinition = getClass().getResourceAsStream("/simple_engine.rb");
         URL processDefinition = getClass().getResource("/simplest_process_definition.rb");
         assertNotNull(engineDefinition);
         assertNotNull(processDefinition);
 
         engine = new EngineAdapter(engineDefinition);
         engine.registerParticipant("alice", new LoggerParticipant("alice"));
         engine.registerParticipant("bob", new LoggerParticipant("bob"));
         engine.ready();
 
         engine.launch(processDefinition);
         sleepAMoment();
         assertNotNull(LoggerParticipant.workitems.get("alice"));
         assertNull(LoggerParticipant.workitems.get("bob"));
 
         engine.reply(LoggerParticipant.workitems.get("alice"));
         sleepAMoment();
         assertNotNull(LoggerParticipant.workitems.get("bob"));
 
         engine.reply(LoggerParticipant.workitems.get("bob"));
         sleepAMoment();
         assertEquals("consumed participant[alice];consumed participant[bob];", LoggerParticipant.log);
     }
 
     @Test
     public void runEngineWithVariables() throws IOException, InterruptedException {
         InputStream engineDefinition = getClass().getResourceAsStream("/simple_engine.rb");
         URL processDefinition = getClass().getResource("/leave_request_process_definition.rb");
 
         engine = new EngineAdapter(engineDefinition);
         engine.registerParticipant("tom", new LoggerParticipant("tom"));
         engine.registerParticipant("bob", new LoggerParticipant("bob"));
         engine.registerParticipant("alice", new LoggerParticipant("alice"));
         engine.ready();
 
         engine.launch(processDefinition, "'launcher' => 'tom'");
         sleepAMoment();
         assertNotNull(LoggerParticipant.workitems.get("tom"));
         assertNull(LoggerParticipant.workitems.get("bob"));
         assertNull(LoggerParticipant.workitems.get("alice"));
 
         engine.reply(LoggerParticipant.workitems.get("tom"));
         sleepAMoment();
         assertNotNull(LoggerParticipant.workitems.get("bob"));
         assertNull(LoggerParticipant.workitems.get("alice"));
 
         IRubyObject workitem = LoggerParticipant.workitems.get("bob");
         new WorkItemAdapter(workitem).setAttribute("boss_should_have_a_look", "true");
         engine.reply(workitem);
         sleepAMoment();
         assertNotNull(LoggerParticipant.workitems.get("alice"));
 
         engine.reply(LoggerParticipant.workitems.get("bob"));
         sleepAMoment();
         assertEquals("consumed participant[tom];consumed participant[bob];consumed participant[alice];consumed participant[tom];",
                 LoggerParticipant.log);
     }
 
     @Test
     public void runEngineWithProcessDefinitionSpecifiedByLaunchItemDefinitionUrl() throws IOException, InterruptedException {
         InputStream engineDefinition = getClass().getResourceAsStream("/simple_engine.rb");
 
         engine = new EngineAdapter(engineDefinition);
         engine.registerParticipant("alice", new LoggerParticipant("alice"));
         engine.registerParticipant("bob", new LoggerParticipant("bob"));
         engine.ready();
 
         engine.launch(getClass().getResource("/simplest_process_definition.rb"));
         sleepAMoment();
         engine.reply(LoggerParticipant.workitems.get("alice"));
         sleepAMoment();
         engine.reply(LoggerParticipant.workitems.get("bob"));
         sleepAMoment();
         assertEquals("consumed participant[alice];consumed participant[bob];", LoggerParticipant.log);
     }
 
     @Test
     public void fileBasedEngine() throws IOException, InterruptedException {
         InputStream engineDefinition = getClass().getResourceAsStream("/file_based_engine.rb");
 
         engine = new EngineAdapter(engineDefinition);
         engine.registerParticipant("alice", new LoggerParticipant("alice"));
         engine.registerParticipant("bob", new LoggerParticipant("bob"));
         engine.ready();
 
         engine.launch(getClass().getResource("/simplest_process_definition.rb"));
         sleepAMoment();
         engine.reply(LoggerParticipant.workitems.get("alice"));
         sleepAMoment();
         engine.reply(LoggerParticipant.workitems.get("bob"));
         sleepAMoment();
         assertEquals("consumed participant[alice];consumed participant[bob];", LoggerParticipant.log);
     }
 
     @Ignore
     @Test
     public void stopEngineShouldNotCauseProcessBroken() throws IOException, InterruptedException {
         String engineDefinition = read(getClass().getResourceAsStream("/file_based_engine.rb"));
 
         Ruby runtime = JavaEmbedUtils.initialize(new ArrayList<String>());
         engine = new EngineAdapter(runtime, engineDefinition);
         engine.registerParticipant("alice", new LoggerParticipant("alice"));
         engine.registerParticipant("bob", new LoggerParticipant("bob"));
         engine.ready();
         engine.launch(getClass().getResource("/simplest_process_definition.rb"));
         sleepAMoment();
 
         String aliceWorkItemDump = new JRubyObject(LoggerParticipant.workitems.get("alice")).dump();
         engine.stop();
 
         runtime = JavaEmbedUtils.initialize(new ArrayList<String>());
         engine = new EngineAdapter(runtime, engineDefinition);
         engine.registerParticipant("alice", new LoggerParticipant("alice"));
         engine.registerParticipant("bob", new LoggerParticipant("bob"));
         engine.ready();
         sleepAMoment();
         engine.reply(JRubyObject.load(runtime, aliceWorkItemDump));
         sleepAMoment();
         engine.reply(LoggerParticipant.workitems.get("bob"));
         sleepAMoment();
         assertEquals("consumed participant[alice];consumed participant[bob];", LoggerParticipant.log);
     }
 
     @Test
     public void cancelProcessByWorkItem() throws Exception {
         InputStream engineDefinition = getClass().getResourceAsStream("/simple_engine.rb");
 
         engine = new EngineAdapter(engineDefinition);
         engine.registerParticipant("alice", new LoggerParticipant("alice"));
         engine.registerParticipant("bob", new LoggerParticipant("bob"));
         engine.ready();
 
         engine.launch(getClass().getResource("/simplest_process_definition.rb"));
         sleepAMoment();
         engine.reply(LoggerParticipant.workitems.get("alice"));
         sleepAMoment();
         WorkItemAdapter itemAdapter = new WorkItemAdapter(LoggerParticipant.workitems.get("bob"));
         engine.cancelProcess(itemAdapter.getWfid());
         assertEquals("consumed participant[alice];consumed participant[bob];canceled participant[bob];", LoggerParticipant.log);
     }
 
     private void sleepAMoment() throws InterruptedException {
         Thread.sleep(300);
     }
 }
