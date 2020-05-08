 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.core.test;
 
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import cc.warlock.core.client.ICommand;
 import cc.warlock.core.client.IStream;
 import cc.warlock.core.client.IStreamListener;
 import cc.warlock.core.client.WarlockString;
 import cc.warlock.core.client.IWarlockStyle.StyleType;
 import cc.warlock.core.client.internal.Stream;
 import cc.warlock.core.client.internal.WarlockStyle;
 
 public class StreamTest {
 
 	protected static class StreamExt extends Stream {
 		public StreamExt() { super(null,null); }
 		public static Stream createStream (String name) { return new Stream(null, name); } 
 	}
 	
 	protected static class Listener implements IStreamListener {
 		public boolean cleared, receivedText, receivedCommand, prompted, echoed, donePrompting, flushed, componentUpdated;
 		public String echo, prompt, command;
 		public WarlockString text;
 		
 		protected void handleEvent() {
 			cleared = receivedText = receivedCommand = prompted = echoed = donePrompting = flushed = componentUpdated = false;
 			text = null;
 			echo = prompt = null;
 		}
 		
 		public void streamReceivedText(IStream stream, WarlockString text) {
 			handleEvent();
 			receivedText = true;
 			this.text = text;
 		}
 		
 		public void streamCleared(IStream stream) {
 			handleEvent();
 			cleared = true;
 		}
 
 		public void streamDonePrompting(IStream stream) {
 			handleEvent();
 			donePrompting = true;
 		}
 
 		public void streamEchoed(IStream stream, String text) {
 			handleEvent();
 			echoed = true;
 			echo = text;
 		}
 
 		public void streamPrompted(IStream stream, String prompt) {
 			handleEvent();
 			prompted = true;
 			this.prompt = prompt;
 		}
 		
 		public void streamFlush(IStream stream) {
 			handleEvent();
 			flushed = true;
 		}
 		
 		public void componentUpdated(IStream stream, String id, WarlockString value) {
 			handleEvent();
 			componentUpdated = true;
 		}
 
 		public void streamReceivedCommand(IStream stream, ICommand command) {
 			handleEvent();
 			this.receivedCommand = true;
 			this.command = command.getText();
 		}
 
 		public void streamCreated(IStream stream) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void streamTitleChanged(IStream stream, String title) {
 			// TODO Auto-generated method stub
 			
 		}
 	}
 	
 	protected static Stream stream;
 	protected static final String STREAM_NAME = "testStream";
 	protected static final String TEST_STRING= "testing stream send--\n";
 	protected static final WarlockStyle TEST_STYLE = new WarlockStyle(new StyleType[] { StyleType.BOLD });
 	protected static Listener listener;
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		stream = StreamExt.createStream(STREAM_NAME);
 		listener = new Listener();
 		stream.addStreamListener(listener);
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 		stream.removeStreamListener(listener);
 		stream = null;
 	}
 
 	@Test
 	public void testClear() {
 		stream.clear();
 		Assert.assertTrue(listener.cleared);
 	}
 
 	@Test
 	public void testSendString() {
 		stream.put(new WarlockString(TEST_STRING));
 		Assert.assertTrue(listener.receivedText);
 		Assert.assertEquals(listener.text.toString(), TEST_STRING);
 		Assert.assertEquals(listener.text.getStyles().size(), 0);
 	}
 
 	@Test
 	public void testSendWarlockString() {
 		WarlockString string = new WarlockString();
 		string.addStyle(TEST_STYLE);
 		string.append(TEST_STRING);
 		
 		stream.put(string);
 		Assert.assertTrue(listener.receivedText);
 		Assert.assertEquals(listener.text.toString(), string.toString());
 		Assert.assertEquals(listener.text.toString(), TEST_STRING);
 		Assert.assertEquals(listener.text.getStyles().size(), 1);
		Assert.assertEquals(listener.text.getStyles().get(0).style, TEST_STYLE);
 	}
 
 	@Test
 	public void testPrompt() {
 		stream.prompt(">");
 		Assert.assertTrue(listener.prompted);
 		Assert.assertEquals(listener.prompt, ">");
 	}
 
 	@Test
 	public void testIsPrompting() {
 		stream.prompt(">");
 		
 		Assert.assertTrue(stream.isPrompting());
 	}
 
 	@Test
 	public void testEcho() {
 		stream.echo(TEST_STRING);
 		
 		Assert.assertTrue(listener.echoed);
 		Assert.assertEquals(listener.echo, TEST_STRING);
 	}
 
 	@Test
 	public void testGetName() {
 		Assert.assertEquals(stream.getName(), STREAM_NAME);
 	}
 	
 	@Test
 	public void testFlush () {
 		stream.flush();
 		
 		Assert.assertTrue(listener.flushed);
 	}
 
 }
