 /*
  * Copyright (c) 2012, Thomas Kuehne <thomas@kuehne.cn>
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package cn.kuehne.wsg50;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.lang.reflect.Method;
 import java.util.List;
 
 import org.junit.Test;
 
 import cn.kuehne.wsg50.helper.AbstractPacket;
 import cn.kuehne.wsg50.helper.PacketBuilderImpl;
 
 public class PacketIDTests {
 	private void checkAbstractPacket(AbstractPacket packet, String label) {
 		final List<Method> inMethods = packet.findInMethods();
 		final List<Method> outMethods = packet.findOutMethods();
 
 		assertEquals(label + " inOutSize", inMethods.size(), outMethods.size());
 		for (int methodIndex = 0; methodIndex < inMethods.size(); methodIndex++) {
 			final Method in = inMethods.get(methodIndex);
 			assertNotNull(label + " in[" + methodIndex + "]", in);
			assertTrue(label + " in[" + methodIndex + "]in", in.getName().startsWith("set"));
 			
 			final Method out = outMethods.get(methodIndex);
 			assertNotNull(label + " out[" + methodIndex + "]", out);
			assertTrue(label + " out[" + methodIndex + "]get", out.getName().startsWith("get"));
 
 			final Class<?>[] params = in.getParameterTypes();
			assertEquals(label + " out[" + methodIndex + "]num", 1, params.length);
 
			assertEquals(label + " type[" + methodIndex + "]", out.getReturnType(), params[0]);
 			
			assertEquals(label + " type[" + methodIndex + "]name", in.getName().substring(3), out.getName().substring(3)); 
 		}
 		
 		assertNotNull(label + " string", packet.toString());
 	}
 
 	private void checkAcknowledge(Acknowledge acknowledge, PacketID id) {
 		final String label = id.toString() + " Acknowledge";
 		try {
 			final byte rawId = id.getId();
 			final byte acknowledgeId = acknowledge.getPacketID();
 
 			assertEquals(label + " id", rawId, acknowledgeId);
 
 			assertTrue(label + " name", acknowledge.getClass().getName().endsWith("Acknowledge"));
 
 			if (acknowledge instanceof AbstractPacket) {
 				checkAbstractPacket((AbstractPacket) acknowledge, label);
 			}
 
 			final PacketBuilderImpl builder = new PacketBuilderImpl(id.getId());
 			acknowledge.writePayload(builder);
 			byte[] payload = builder.getPayload();
 			assertNotNull(label + " get", payload);
 			acknowledge.setPayload(payload);
 		} catch (Exception e) {
 			throw new BugException(label, e);
 		}
 	}
 
 	private void checkCommand(Command command, PacketID id) {
 
 		final String label = id.toString() + " Command";
 		try {
 			final byte rawId = id.getId();
 			final byte commandId = command.getPacketID();
 
 			assertEquals(label + " id", rawId, commandId);
 
 			assertTrue(label + " name", command.getClass().getName().endsWith("Command"));
 
 			if (command instanceof AbstractPacket) {
 				checkAbstractPacket((AbstractPacket) command, label);
 			}
 			final PacketBuilderImpl builder = new PacketBuilderImpl(id.getId());
 			command.writePayload(builder);
 			byte[] payload = builder.getPayload();
 			assertNotNull(label + " get", payload);
 			command.setPayload(payload);
 		} catch (Exception e) {
 			throw new BugException(label, e);
 		}
 	}
 
 	@Test
 	public void testAcknowledgements() {
 		for (final PacketID id : PacketID.values()) {
 			final Acknowledge acknowledge;
 			try {
 				acknowledge = id.getAcknowledge();
 			} catch (TodoException e) {
 				continue;
 			}
 			checkAcknowledge(acknowledge, id);
 		}
 	}
 
 	@Test
 	public void testCommands() {
 		for (final PacketID id : PacketID.values()) {
 			final Command command;
 			try {
 				command = id.getCommand();
 			} catch (TodoException e) {
 				continue;
 			}
 			checkCommand(command, id);
 		}
 	}
 
 	@Test
 	public void testUniqueIDs() {
 		final PacketID[] ids = new PacketID[256];
 
 		for (final PacketID p : PacketID.values()) {
 			final int id = p.getId();
 			final int index = id - Byte.MIN_VALUE;
 			assertNull("duplicate packet id 0x" + Integer.toHexString(0xFF & id), ids[index]);
 			ids[index] = p;
 		}
 	}
 }
