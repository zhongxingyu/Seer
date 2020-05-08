 /**
  * 
  */
 package net.anyflow.menton.http;
 
 
 /**
  * @author anyflow
  */
 public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
 
 	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HttpServerHandler.class);
 
 	private HttpRequest request;
 	private RequestHandler requestHandler;
 	private String requestHandlerPackageRoot;
 
 	public HttpServerHandler(String requestHandlerPackageRoot) {
 		this.requestHandlerPackageRoot = requestHandlerPackageRoot;
 	}
 
 	public HttpServerHandler(RequestHandler requestHandler) {
 		this.requestHandler = requestHandler;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see io.netty.channel.SimpleChannelInboundHandler#channelRead0(io.netty.channel.ChannelHandlerContext, java.lang.Object)
 	 */
 	@Override
 	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
 
 		if(HttpHeaders.is100ContinueExpected(request)) {
 			ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
 			return;
 		}
 
 		request = new HttpRequest(ctx.channel(), msg);
 
 		debugRequest(request);
 
 		logger.info(request.getUri().toString() + " requested.");
 
 		HttpResponse response = HttpResponse.createServerDefault(ctx.channel(), request.headers().get(HttpHeaders.Names.COOKIE));
 
 		if(Configurator.instance().getProperty("allow_cross_domain", "no").equalsIgnoreCase("yes")) {
 			response.headers().add("Access-Control-Allow-Origin", "*");
 			response.headers().add("Access-Control-Allow-Methods", "POST, GET");
 			response.headers().add("Access-Control-Allow-Headers", "X-PINGARUNER");
 			response.headers().add("Access-Control-Max-Age", "1728000");
 		}
 
 		try {
 			String path = (new URI(request.getUri())).getPath();
 
 			String content = requestHandler != null ? handleMethodTypeHandler(request, response, path) : handleClassTypeHandler(request, response,
 					path);
 
 			response.setContent(content);
 		}
 		catch(URISyntaxException e) {
 			response.setStatus(HttpResponseStatus.NOT_FOUND);
 			logger.info("unexcepted URI : {}", request.getUri().toString());
 		}
 		catch(InstantiationException e) {
 			response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
 			logger.error("Generating new request handler instance failed.", e);
 		}
 		catch(IllegalAccessException e) {
 			response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
 			logger.error("Generating new request handler instance failed.", e);
 		}
 		catch(Exception e) {
 			response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
 			logger.error("Unknown exception was thrown", e);
 		}
 
 		boolean keepAlive = request.headers().get(HttpHeaders.Names.CONNECTION) == HttpHeaders.Values.KEEP_ALIVE;
 		if(keepAlive) {
 			response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
 			response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
 		}
 
 		// ctx.write(response);
 		ChannelFuture future = ctx.writeAndFlush(response);
 		future.addListener(ChannelFutureListener.CLOSE);
 	}
 
 	private String handleClassTypeHandler(HttpRequest request, HttpResponse response, String requestedPath) throws DefaultException,
 			InstantiationException, IllegalAccessException {
 
 		Class<? extends RequestHandler> handlerClass = RequestHandler.find(requestedPath, request.getMethod().toString(),
 				this.requestHandlerPackageRoot);
 
 		if(handlerClass == null) {
 			response.setStatus(HttpResponseStatus.NOT_FOUND);
 			logger.info("unexcepted URI : {}", request.getUri().toString());
 
 			return "Failed to find the request handler.";
 		}
 		else {
 			RequestHandler handler = handlerClass.newInstance();
 
 			handler.initialize(request, response);
 
 			return handler.call();
 		}
 	}
 
 	private String handleMethodTypeHandler(HttpRequest request, HttpResponse response, String requestedPath) throws DefaultException,
 			IllegalAccessException, InvocationTargetException {
 
 		requestHandler.initialize(request, response);
 		Method handler = requestHandler.findHandler(requestedPath, request.getMethod().toString());
 
 		if(handler == null) {
 			response.setStatus(HttpResponseStatus.NOT_FOUND);
 			logger.info("unexcepted URI : {}", request.getUri().toString());
 
 			return "Failed to find the request handler.";
 		}
 		else {
 			return handler.invoke(requestHandler, (Object[])null).toString();
 		}
 	}
 
 	@Override
 	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
 		ctx.fireChannelReadComplete();
 		ctx.flush();
 	}
 
 	@Override
 	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
 		logger.error(cause.getMessage(), cause);
 		ctx.close();
 	}
 
 	private void debugRequest(HttpRequest request) {
 		StringBuilder buf = new StringBuilder();
 
 		buf.setLength(0);
 		buf.append("VERSION: ").append(request.getProtocolVersion()).append("\r\n");
 		buf.append("HOSTNAME: ").append(HttpHeaders.getHost(request, "unknown")).append("\r\n");
 		buf.append("REQUEST_URI: ").append(request.getUri()).append("\r\n\r\n");
 
 		List<Entry<String, String>> headers = request.headers().entries();
 		if(!headers.isEmpty()) {
 			for(Entry<String, String> h : request.headers().entries()) {
 				String key = h.getKey();
 				String value = h.getValue();
 				buf.append("HEADER: ").append(key).append(" = ").append(value).append("\r\n");
 			}
 			buf.append("\r\n");
 		}
 
 		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
 		Map<String, List<String>> params = queryStringDecoder.parameters();
 		if(!params.isEmpty()) {
 			for(Entry<String, List<String>> p : params.entrySet()) {
 				String key = p.getKey();
 				List<String> vals = p.getValue();
 				for(String val : vals) {
 					buf.append("PARAM: ").append(key).append(" = ").append(val).append("\r\n");
 				}
 			}
 			buf.append("\r\n");
 		}
 
 		DecoderResult result = request.getDecoderResult();
 
 		if(result.isSuccess() == false) {
 			buf.append(".. WITH DECODER FAILURE: ");
 			buf.append(result.cause());
 			buf.append("\r\n");
 		}
 	}
 }
