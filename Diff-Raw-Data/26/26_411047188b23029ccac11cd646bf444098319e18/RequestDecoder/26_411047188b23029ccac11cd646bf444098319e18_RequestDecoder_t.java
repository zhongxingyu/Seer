 package com.virtualdisk.network.codec;
 
 import com.virtualdisk.network.request.*;
 import com.virtualdisk.network.util.Sendable.MessageType;
 
 import org.jboss.netty.channel.*;
 import org.jboss.netty.handler.codec.frame.*;
 import org.jboss.netty.buffer.*;
 
 public class RequestDecoder
 extends FrameDecoder
 {
     protected Object decode( ChannelHandlerContext context
                            , Channel channel
                            , ChannelBuffer buffer
                            )
     throws Exception
     {
         if (buffer.readableBytes() < 5)
         {
             return null;
         }
 
         MessageType type = MessageType.fromByte(buffer.readByte());
         int length = buffer.readInt();
 
         if (buffer.readableBytes() < length)
         {
             buffer.resetReaderIndex();
             return null;
         }
 
         switch (type)
         {
             case emptyBuffer:
                 return null;
 
             case unknownRequest:
                 return null;
 
             case orderRequest:
                 OrderRequest orderRequest = new OrderRequest(0, 0, 0, null);
                 orderRequest.decode(buffer);
                 return orderRequest;
 
             case orderRequestResult:
                 OrderRequestResult orderRequestResult = new OrderRequestResult(0, false, false);
                 orderRequestResult.decode(buffer);
                 return orderRequestResult;
 
             case readRequest:
                 ReadRequest readRequest = new ReadRequest(0, 0, 0);
                 readRequest.decode(buffer);
                 return readRequest;
 
             case readRequestResult:
                 ReadRequestResult readRequestResult = new ReadRequestResult(0, false, false, null, null);
                 readRequestResult.decode(buffer);
                 return readRequestResult;
 
             case writeRequest:
                 WriteRequest writeRequest = new WriteRequest(0, 0, 0, null, null);
                 writeRequest.decode(buffer);
                System.out.println("Decoded " + writeRequest.getRequestId());
                 return writeRequest;
 
             case writeRequestResult:
                 WriteRequestResult writeRequestResult = new WriteRequestResult(0, false, false);
                 writeRequestResult.decode(buffer);
                 return writeRequestResult;
 
             case volumeExistsRequest:
                 VolumeExistsRequest volumeExistsRequest = new VolumeExistsRequest(0, 0);
                 volumeExistsRequest.decode(buffer);
                 return volumeExistsRequest;
 
             case volumeExistsRequestResult:
                 VolumeExistsRequestResult volumeExistsRequestResult = new VolumeExistsRequestResult(0, false, false, false);
                 volumeExistsRequestResult.decode(buffer);
                 return volumeExistsRequestResult;
 
             case createVolumeRequest:
                 CreateVolumeRequest createVolumeRequest = new CreateVolumeRequest(0, 0);
                 createVolumeRequest.decode(buffer);
                 return createVolumeRequest;
 
             case createVolumeRequestResult:
                 CreateVolumeRequestResult createVolumeRequestResult = new CreateVolumeRequestResult(0, false, false);
                 createVolumeRequestResult.decode(buffer);
                 return createVolumeRequestResult;
 
             case deleteVolumeRequest:
                 DeleteVolumeRequest deleteVolumeRequest = new DeleteVolumeRequest(0, 0);
                 deleteVolumeRequest.decode(buffer);
                 return deleteVolumeRequest;
 
             case deleteVolumeRequestResult:
                 DeleteVolumeRequestResult deleteVolumeRequestResult = new DeleteVolumeRequestResult(0, false, false);
                 deleteVolumeRequestResult.decode(buffer);
                 return deleteVolumeRequestResult;
 
             default:
                 return null;
         }
     }
 }
 
