 package com.tantaman.ferox.route_middelware;
 
 import io.netty.handler.codec.http.HttpContent;
 import io.netty.handler.codec.http.HttpRequest;
 import io.netty.handler.codec.http.multipart.Attribute;
 import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
 import io.netty.handler.codec.http.multipart.FileUpload;
 import io.netty.handler.codec.http.multipart.HttpDataFactory;
 import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
 import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
 import io.netty.handler.codec.http.multipart.InterfaceHttpData;
 
 import java.util.List;
 import java.util.Map;
 
 import com.tantaman.ferox.api.IDisposable;
 import com.tantaman.ferox.api.request_response.IHttpContent;
 import com.tantaman.ferox.api.request_response.IHttpRequest;
 import com.tantaman.ferox.api.request_response.IRequestChainer;
 import com.tantaman.ferox.api.request_response.IResponse;
 import com.tantaman.ferox.api.router.IRouteHandler;
 
 public class BodyParserHandler implements IRouteHandler {
 	private static final HttpDataFactory factory = new DefaultHttpDataFactory(4096);
 	private HttpPostRequestDecoder decoder;
 	
 	@Override
 	public void request(IHttpRequest request, IResponse response,
 			IRequestChainer next) {
 		decoder = new HttpPostRequestDecoder(factory, (HttpRequest)request.getRaw());
 		request.addDisposable(new IDisposable() {
 			@Override
 			public void dispose() {
 				decoder.cleanFiles();
 			}
 		});
 		
 		next.request(request);
 	}
 	
 	@Override
 	public void content(IHttpContent content, IResponse response,
 			IRequestChainer next) {
 		offer(content, response);
 		next.content(content);
 	}
 	
 	private void offer(IHttpContent content, IResponse response) {
 		HttpContent chunk = (HttpContent) content.getRaw();
 		try {
 			decoder.offer(chunk);
 		} catch (ErrorDataDecoderException e1) {
 			response.fineGrained().close();
 			decoder.cleanFiles();
 			e1.printStackTrace();
 			return;
 		}
 	}
 	
 	@Override
 	public void exceptionCaught(Throwable cause, IResponse response,
 			IRequestChainer next) {
		cause.printStackTrace();
 		decoder.cleanFiles();
 		response.fineGrained().close();
 	}
 	
 	@Override
 	public void lastContent(IHttpContent content, IResponse response,
 			IRequestChainer next) {
 		offer(content, response);
 		Map<String, Attribute> body = content.getBody();
 		List<FileUpload> files = content.getFiles();
 		
 		for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
 			if (data instanceof Attribute) {
 				body.put(data.getName(), (Attribute)data);
 			} else {
 				files.add((FileUpload)data);
 			}
 		}
 		
 		next.lastContent(content);
 	}
 }
