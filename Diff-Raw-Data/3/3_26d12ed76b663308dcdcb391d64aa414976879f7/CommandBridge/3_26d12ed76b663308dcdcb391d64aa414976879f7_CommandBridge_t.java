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
 package cn.kuehne.wsg50.roboter2012;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import cn.kuehne.wsg50.Acknowledge;
 import cn.kuehne.wsg50.Command;
 import cn.kuehne.wsg50.E;
 import cn.kuehne.wsg50.PacketBuilder;
 import cn.kuehne.wsg50.PacketID;
 import cn.kuehne.wsg50.Parameter;
 
 public class CommandBridge implements Command {
 	private Command command;
 	private int nextReplyIndex;
 	private List<Acknowledge> replies;
 	private Semaphore semaphore;
 
 	public CommandBridge() {
		semaphore = new Semaphore(0);
 	}
 
 	void addAcknowledge(Acknowledge acknowledge) {
 		if (acknowledge == null) {
 			throw new IllegalArgumentException("'acknowledge' is null");
 		}
 		if (acknowledge.getPacketID() != command.getPacketID()) {
 			throw new IllegalArgumentException("'acknowledge' has bad packet ID: " + acknowledge.getPacketID()
 					+ " instead of " + command.getPacketID());
 		}
 		replies.add(acknowledge);
 		semaphore.release(1);
 	}
 
 	public final CommandBridge getCommand() {
 		return this;
 	}
 
 	@Override
 	public final byte getPacketID() {
 		if (command == null) {
 			throw new IllegalStateException("'command' is null");
 		}
 		return command.getPacketID();
 	}
 
 	@Override
 	public final Parameter[] getParameters() {
 		if (command == null) {
 			throw new IllegalStateException("'command' is null");
 		}
 		return command.getParameters();
 	}
 
 	public final boolean isError() {
 		if (replies != null && 0 < replies.size()) {
 			final Acknowledge last = replies.get(replies.size());
 			final short status = last.getStatusCode();
 			return !(E.SUCCESS.getCode() == status || E.CMD_PENDING.getCode() == status);
 		}
 		return false;
 	}
 
 	public boolean isFinished() {
 		if (replies != null && 0 < replies.size()) {
 			final Acknowledge last = replies.get(replies.size());
 			final short status = last.getStatusCode();
 			return E.SUCCESS.getCode() == status;
 		}
 		return false;
 	}
 
 	public final boolean isStarted() {
 		return replies != null;
 	}
 
 	public final boolean isTouched() {
 		return command != null;
 	}
 
 	void outgoing() {
 		replies = new ArrayList<Acknowledge>();
 	}
 
 	public final void setCommand(final Command newCommand) {
 		if (isStarted()) {
 			throw new IllegalStateException("command has already been encoded");
 		}
 		if (newCommand == null) {
 			throw new IllegalArgumentException("'newCommand' is null");
 		}
 		command = newCommand;
 	}
 
 	public final void setCommand(final String name) {
 		if (isStarted()) {
 			throw new IllegalStateException("command has already been encoded");
 		}
 		final PacketID id = PacketID.valueOf(name);
 		command = id.getCommand();
 	}
 
 	@Override
 	public final void setPayload(byte[] p) {
 		if (command == null) {
 			throw new IllegalStateException("'command' is null");
 		}
 		command.setPayload(p);
 	}
 
 	public Acknowledge waitForNextAcknowledge() {
 		while (true) {
 			try {
 				semaphore.acquire();
 				return replies.get(nextReplyIndex++);
 			} catch (InterruptedException e) {
 				// noop
 			}
 		}
 	}
 
 	@Override
 	public final void writePayload(PacketBuilder builder) {
 		if (command == null) {
 			throw new IllegalStateException("'command' is null");
 		}
 		command.writePayload(builder);
 	}
 	
 	@Override
 	public String toString(){
 		if(command != null){
 			return command.toString();
 		}
 		return super.toString();
 	}
 }
