 package com.tantaman.ferox.middleware;
 
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelInboundHandlerAdapter;
 import io.netty.channel.MessageList;
 import io.netty.handler.codec.http.HttpContent;
 import io.netty.handler.codec.http.HttpRequest;
 import io.netty.handler.codec.http.LastHttpContent;
 import io.netty.handler.codec.http.multipart.Attribute;
 import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
 import io.netty.handler.codec.http.multipart.DiskAttribute;
 import io.netty.handler.codec.http.multipart.DiskFileUpload;
 import io.netty.handler.codec.http.multipart.FileUpload;
 import io.netty.handler.codec.http.multipart.HttpDataFactory;
 import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
 import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
 import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
 import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.IncompatibleDataDecoderException;
 import io.netty.handler.codec.http.multipart.InterfaceHttpData;
 import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
 
 import com.tantaman.ferox.middleware.message_types.TrackedHttpRequest;
 
 public class BodyParser extends ChannelInboundHandlerAdapter {
 	private HttpPostRequestDecoder decoder;
 	private static final HttpDataFactory factory = new DefaultHttpDataFactory(262144);
 	private TrackedHttpRequest trackedRequest;
 
 	static {
 		DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
 		// on exit (in normal exit)
 		DiskFileUpload.baseDirectory = null; // system temp directory
 		DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
 		// exit (in normal exit)
 		DiskAttribute.baseDirectory = null; // system temp directory
 	}
 
 	@Override
 	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
 		if (decoder != null) {
 			decoder.cleanFiles();
 		}
 	}
 
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageList<Object> msgs) throws Exception {
 		try {
 			for (int i = 0; i < msgs.size(); i++) {
 				Object msg = msgs.get(i);
 				if (msg instanceof TrackedHttpRequest) {
 					trackedRequest = (TrackedHttpRequest)msg;
 					HttpRequest request = trackedRequest.getRawRequest();
 
 					try {
 						decoder = new HttpPostRequestDecoder(factory, request);
 						trackedRequest.setDecoder(decoder);
 					} catch (ErrorDataDecoderException e1) {
 						e1.printStackTrace();
 						ctx.channel().close();
 						return;
 					} catch (IncompatibleDataDecoderException e1) {
 						return;
 					}
 				}
 
 				if (decoder != null) {
 					if (msg instanceof HttpContent) {
 						// New chunk is received
 						HttpContent chunk = (HttpContent) msg;
 						try {
 							decoder.offer(chunk);
 						} catch (ErrorDataDecoderException e1) {
 							e1.printStackTrace();
 							ctx.channel().close();
 							return;
 						}
 
 						readHttpDataChunkByChunk();
 
 						if (chunk instanceof LastHttpContent) {
 							reset();
 						}
 					}
 				}
 			}
 		} finally {
 			ctx.fireMessageReceived(msgs);
 		}
 	}
 
 	private void readHttpDataChunkByChunk() {
 		try {
 			while (decoder.hasNext()) {
 				InterfaceHttpData data = decoder.next();
 				if (data != null) {
 					// new value
 					handleHttpData(data);
 				}
 			}
 		} catch (EndOfDataDecoderException e1) {
 			// no more data.
 		}
 	}
 
 	private void handleHttpData(InterfaceHttpData data) {
 		if (data.getHttpDataType() == HttpDataType.Attribute) {
 			Attribute attribute = (Attribute) data;
 			trackedRequest.addAttribute(attribute);
 		} else {
 			if (data.getHttpDataType() == HttpDataType.FileUpload) {
 				FileUpload fileUpload = (FileUpload) data;
 				trackedRequest.addFile(fileUpload);
 			}
 		}
 	}
 
 	private void reset() {
 		decoder = null;
 	}
 }
