 package com.virtualdisk.network;
 
 import com.virtualdisk.coordinator.*;
 import com.virtualdisk.network.request.*;
 import com.virtualdisk.network.request.base.*;
 import com.virtualdisk.network.util.*;
 import com.virtualdisk.network.util.Sendable.MessageType;
 
 import org.jboss.netty.channel.*;
 
 import java.util.*;
 
 public class CoordinatorHandler
 extends SimpleChannelHandler
 {
     Coordinator coordinator = SingletonCoordinator.getCoordinator();
     NetworkServer server = SingletonCoordinator.getServer();
 
     public void messageReceived( ChannelHandlerContext context
                                , MessageEvent event
                                )
     {
         Sendable result = (Sendable) event.getMessage();
         MessageType type = result.messageType();
 
         switch (type)
         {
             case orderRequestResult:
             case readRequestResult:
             case writeRequestResult:
             case volumeExistsRequestResult:
             case createVolumeRequestResult:
             case deleteVolumeRequestResult:
                 SingletonCoordinator.setResult(((RequestResult)result).getRequestId(), (RequestResult)result);
                 break;
 
             case writeRequest: {
                 WriteRequest request = (WriteRequest) result;
                 int volumeId = request.getVolumeId();
                 long logicalOffset = request.getLogicalOffset();
                 byte[] block = request.getBlock();
                 int requestId = coordinator.write(volumeId, logicalOffset, block);
 
                 SingletonCoordinator.registerCallback(requestId, event.getChannel());
                System.out.println("Registered callback for write request " + request.getRequestId());
                 } break;
 
             case readRequest: {
                 ReadRequest request = (ReadRequest) result;
                 int volumeId = request.getVolumeId();
                 long logicalOffset = request.getLogicalOffset();
                 int requestId = coordinator.read(volumeId, logicalOffset);
 
                 SingletonCoordinator.registerCallback(requestId, event.getChannel());
                 } break;
 
             case createVolumeRequest: {
                 CreateVolumeRequest request = (CreateVolumeRequest) result;
                 int volumeId = request.getVolumeId();
                 int requestId = coordinator.createVolume(volumeId);
 
                 SingletonCoordinator.registerCallback(requestId, event.getChannel());
                System.out.println("Registered callback for create request " + request.getRequestId());
                 } break;
 
             case deleteVolumeRequest: {
                 DeleteVolumeRequest request = (DeleteVolumeRequest) result;
                 int volumeId = request.getVolumeId();
                 int requestId = coordinator.deleteVolume(volumeId);
 
                 SingletonCoordinator.registerCallback(requestId, event.getChannel());
                 } break;
 
             default:
                 break;
         }
     }
 
     public void channelConnected( ChannelHandlerContext context
                                 , ChannelStateEvent event
                                 )
     {
         Channel clientChannel = event.getChannel();
         SingletonCoordinator.registerNewClient(clientChannel);
     }
 
     public void exceptionCaught( ChannelHandlerContext context
                                , ExceptionEvent event
                                )
     {
         event.getCause().printStackTrace();
         event.getChannel().close();
         System.exit(1);
     }
 }
 
