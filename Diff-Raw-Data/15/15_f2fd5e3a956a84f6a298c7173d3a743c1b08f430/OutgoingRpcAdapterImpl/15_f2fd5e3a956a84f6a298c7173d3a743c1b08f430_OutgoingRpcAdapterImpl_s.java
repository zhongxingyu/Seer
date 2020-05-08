 /**
  * Copyright 2013 Bjørn Remseth (la3lma@gmail.com)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package no.rmz.blobee.rpc.peer.wireprotocol;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 import com.google.protobuf.ByteString;
 import com.google.protobuf.Message;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import no.rmz.blobeeproto.api.proto.Rpc;
 import no.rmz.blobeeproto.api.proto.Rpc.RpcControl;
 import org.jboss.netty.channel.Channel;
 
 /**
  * An adapter that translates an API style programming into
  * a wire-format style.
  */
 public final class OutgoingRpcAdapterImpl implements OutgoingRpcAdapter {
 
     private static  final Logger log =
             Logger.getLogger(OutgoingRpcAdapterImpl.class.getName());
 
     /**
      * A constant used when sending heartbeats.
      */
     private static final Rpc.RpcControl HEARTBEAT =
             Rpc.RpcControl.newBuilder()
             .setMessageType(Rpc.MessageType.HEARTBEAT).build();
 
 
     /**
      * The channel we are sending messages over.
      */
     private final Channel channel;
 
     /**
      * Construct a new adapter that will send all its messages
      * to the parameter channel.
      * @param channel the channel to send all messages to.
      */
     public OutgoingRpcAdapterImpl(final Channel channel) {
         this.channel = checkNotNull(channel);
     }
 
     /**
      * Conert a mesage to a bytestring that can then be packaged
      * as a payload field in an RpcControl instance.
      * @param msg The message to encode.
      * @return A ByteString representation of msg.
      */
    private ByteString messageToByteString(final Message msg) {
         checkNotNull(msg);
         final ByteArrayOutputStream baos =
                 new ByteArrayOutputStream(msg.getSerializedSize());
         try {
             msg.writeTo(baos);
             baos.close();
         } catch (IOException ex) {
             log.log(Level.SEVERE, "Couldn't serialize payload", ex);
         }
         ByteString payload;
         payload = ByteString.copyFrom(baos.toByteArray());
         checkNotNull(payload);
         return payload;
     }
 
 
     @Override
     public void sendInvocation(
             final String methodName,
             final String inputType,
             final String outputType,
             final Long rpcIndex,
             final Message rpParameter,
             final boolean multiReturn,
             final boolean noReturn) {
 
         checkNotNull(methodName);
         checkNotNull(inputType);
         checkNotNull(outputType);
         checkNotNull(rpParameter);
         checkArgument(rpcIndex >= 0);
 
         final ByteString payload =  messageToByteString(rpParameter);
         final Rpc.MethodSignature ms = Rpc.MethodSignature.newBuilder()
                 .setMethodName(methodName)
                 .setInputType(inputType)
                 .setOutputType(outputType)
                 .build();
 
         final Rpc.RpcControl rpcInvocationMessage =
                 Rpc.RpcControl.newBuilder()
                 .setMessageType(Rpc.MessageType.RPC_INV)
                 .setRpcIndex(rpcIndex)
                 .setMethodSignature(ms)
                 .setMultiReturn(multiReturn)
                 .setNoReturn(noReturn)
                 .setPayload(payload)
                 .build();
 
         channel.write(rpcInvocationMessage);
     }
 
     @Override
     public void returnRpcResult(
             final long rpcIndex,
             final Rpc.MethodSignature methodSignature,
             final Message result,
             final boolean multiReturn) {
 
         final ByteString payload =  messageToByteString(result);
         final Rpc.RpcControl returnValueMessage =
                 Rpc.RpcControl.newBuilder()
                 .setMessageType(Rpc.MessageType.RPC_RET)
                 .setRpcIndex(rpcIndex)
                 .setPayload(payload)
                 .setMethodSignature(methodSignature)
                 .setMultiReturn(multiReturn)
                 .build();
         channel.write(returnValueMessage);
     }
 
 
 
     @Override
     public void sendHeartbeat() {
         channel.write(HEARTBEAT);
     }
 
     @Override
     public void sendCancelMessage(final long rpcIndex) {
 
         final RpcControl cancelMessage =
                 Rpc.RpcControl.newBuilder()
                 .setMessageType(Rpc.MessageType.RPC_CANCEL)
                 .setRpcIndex(rpcIndex)
                 .build();
 
         channel.write(cancelMessage);
     }
 
     @Override
     public void sendInvocationFailedMessage(
             final long rpcIndex,
             final String reason) {
         checkNotNull(reason);
         checkArgument(rpcIndex >= 0);
 
         final RpcControl failedMessage = RpcControl.newBuilder()
                 .setMessageType(Rpc.MessageType.INVOCATION_FAILED)
                 .setRpcIndex(rpcIndex)
                 .setFailed(reason)
                 .build();
 
         channel.write(failedMessage);
     }
 
     @Override
     public void terminateMultiReturnSequence(long rpcIndex) {
         final RpcControl failedMessage = RpcControl.newBuilder()
                 .setMessageType(Rpc.MessageType.TERMINATE_MULTI_SEQUENCE)
                 .setRpcIndex(rpcIndex)
                 .build();
     }
 }
