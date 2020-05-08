 /*******************************************************************************
  * This file is part of DITL.                                                  *
  *                                                                             *
  * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
  *                                                                             *
  * DITL is free software: you can redistribute it and/or modify                *
  * it under the terms of the GNU General Public License as published by        *
  * the Free Software Foundation, either version 3 of the License, or           *
  * (at your option) any later version.                                         *
  *                                                                             *
  * DITL is distributed in the hope that it will be useful,                     *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  * GNU General Public License for more details.                                *
  *                                                                             *
  * You should have received a copy of the GNU General Public License           *
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
  *******************************************************************************/
 package ditl.transfers.viz;
 
 import java.io.IOException;
 
 import ditl.*;
 import ditl.graphs.viz.GraphRunner;
 import ditl.transfers.*;
 
 @SuppressWarnings("serial")
 public class RoutingRunner extends GraphRunner 
 	implements TransferRunner, MessageRunner, BufferRunner {
 	
 	protected Bus<Transfer> transferBus = new Bus<Transfer>();
 	protected Bus<TransferEvent> transferEventBus = new Bus<TransferEvent>();
 	protected StatefulReader<TransferEvent, Transfer> transfer_reader;
 	
 	protected Bus<Message> messageBus = new Bus<Message>();
 	protected Bus<MessageEvent> messageEventBus = new Bus<MessageEvent>();
 	protected StatefulReader<MessageEvent, Message> message_reader;
 	
 	protected Bus<Buffer> bufferBus = new Bus<Buffer>();
 	protected Bus<BufferEvent> bufferEventBus = new Bus<BufferEvent>();
 	protected StatefulReader<BufferEvent, Buffer> buffer_reader;
 
 	@Override
 	public void addTransferHandler(TransferTrace.Handler handler) {
 		transferBus.addListener(handler.transferListener());
 		transferEventBus.addListener(handler.transferEventListener());
 	}
 
 	@Override
 	public void setTransferTrace(TransferTrace transfers) throws IOException {
 		transferEventBus.reset();
 		transferBus.reset();
 		if ( transfer_reader != null ){
 			runner.removeGenerator(transfer_reader);
 			transfer_reader.close();
 		}
		transfer_reader = transfers.getReader();
		if ( transfer_reader != null ){
 			transfer_reader.setBus(transferEventBus);
 			transfer_reader.setStateBus(transferBus);
 			transfer_reader.seek(runner.time());
 			transferBus.flush();
 			runner.addGenerator(transfer_reader);
 		}
 	}
 
 	@Override
 	public void addMessageHandler(MessageTrace.Handler handler) {
 		messageBus.addListener(handler.messageListener());
 		messageEventBus.addListener(handler.messageEventListener());
 	}
 
 	@Override
 	public void setMessageTrace(MessageTrace messages) throws IOException {
 		messageEventBus.reset();
 		messageBus.reset();
 		if ( message_reader != null ){
 			runner.removeGenerator(message_reader);
 			message_reader.close();
 		}
		message_reader = messages.getReader();
		if ( message_reader != null ){
 			message_reader.setBus(messageEventBus);
 			message_reader.setStateBus(messageBus);
 			message_reader.seek(runner.time());
 			messageBus.flush();
 			runner.addGenerator(message_reader);
 		}
 	}
 
 	@Override
 	public void addBufferHandler(BufferTrace.Handler handler) {
 		bufferBus.addListener(handler.bufferListener());
 		bufferEventBus.addListener(handler.bufferEventListener());
 	}
 
 	@Override
 	public void setBufferTrace(BufferTrace buffers) throws IOException {
 		bufferEventBus.reset();
 		bufferBus.reset();
 		if ( buffer_reader != null ){
 			runner.removeGenerator(buffer_reader);
 			buffer_reader.close();
 		}
		buffer_reader = buffers.getReader();
		if ( buffer_reader != null ){
 			buffer_reader.setBus(bufferEventBus);
 			buffer_reader.setStateBus(bufferBus);
 			buffer_reader.seek(runner.time());
 			bufferBus.flush();
 			runner.addGenerator(buffer_reader);
 		}
 		
 	}
 
 }
