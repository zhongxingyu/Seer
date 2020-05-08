 package com.anthavio.httl.util;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import com.anthavio.httl.HttpSender;
 import com.anthavio.httl.HttpURLConfig;
 import com.anthavio.httl.SenderRequest;
 import com.anthavio.httl.SenderResponse;
 
 /**
  * Sometimes we need to test what is sent remote server without actualy sending it...
  * 
  * @author martin.vanek
  *
  */
 public class FakeSender extends HttpSender {
 
 	private SenderRequest request; //from last doExecute invocation
 	private String path;//from last doExecute invocation
 	private String query;//from last doExecute invocation
 
 	private SenderResponse response; //what to return form doExecute
 
 	private boolean closed;
 
 	private AtomicInteger executions = new AtomicInteger(0);
 
 	public FakeSender(SenderResponse response) {
 		super(new HttpURLConfig("http://never.really.sent.anywhere/"));
 		if (response == null) {
 			throw new IllegalArgumentException("response is null");
 		}
 		this.response = response;
 	}
 
 	public FakeSender(String responseBody) {
 		this(200, "text/plain", responseBody);
 	}
 
 	public FakeSender(int responseCode, String contentType, String responseBody) {
 		super(new HttpURLConfig("http://never.really.sent.anywhere/"));
 		setResponse(responseCode, contentType, responseBody);
 	}
 
 	/**
 	 * Test Sender closing
 	 */
 	public boolean isClosed() {
 		return closed;
 	}
 
 	@Override
 	public void close() {
 		this.closed = true;
 	}
 
 	@Override
 	protected SenderResponse doExecute(SenderRequest request, String path, String query) throws IOException {
 		if (closed) {
 			throw new IllegalStateException("Sender is closed");
 		}
 		this.request = request;
 		this.path = path;
 		this.query = query;
 		executions.incrementAndGet();
 		return this.response;
 	}
 
 	/**
 	 * Change Response returned from doExecute
 	 */
 	public void setResponse(int code, String contentType, String body) {
 		Multival headers = new Multival();
 		headers.add("Content-Type", contentType);
 		this.response = new FakeResponse(code, headers, body);
 	}
 
 	/**
 	 * @return Response returned from doExecute
 	 */
 	public SenderResponse getResponse() {
 		return response;
 	}
 
 	/**
 	 * Test for number of executions
 	 */
 	public int getExecutionCount() {
 		return executions.intValue();
 	}
 
 	/**
 	 * @return request from last doExecute invocation
 	 */
 	public SenderRequest getRequest() {
 		return request;
 	}
 
 	/**
 	 * @return url path from last doExecute invocation
 	 */
 	public String getPath() {
 		return path;
 	}
 
 	/**
 	 * @return url query from last doExecute invocation
 	 */
 	public String getQuery() {
 		return query;
 	}
 
 	/**
 	 * Response from FakeSender
 	 * 
 	 * @author martin.vanek
 	 *
 	 */
 	public static class FakeResponse extends SenderResponse {
 
 		private static final long serialVersionUID = 1L;
 
 		private byte[] bodyBytes;
 
 		private boolean closed;
 
 		public FakeResponse(int code, String contentType, String body) {
 			this(code, new Multival(), body);
 			super.getHeaders().add("Content-Type", contentType);
			String[] strings = HttpHeaderUtil.splitContentType(contentType, encoding);
			super.mediaType = strings[0];
			super.encoding = strings[1];
 		}
 
 		public FakeResponse(int code, Multival headers, String body) {
 			super(code, "fake " + code + " http response", headers, null);
 			this.bodyBytes = body.getBytes(Charset.forName("utf-8"));
 		}
 
 		public InputStream getStream() {
 			return new ByteArrayInputStream(bodyBytes);
 		}
 
 		@Override
 		public void close() {
 			super.close();
 			this.closed = true;
 		}
 
 		public boolean isClosed() {
 			return closed;
 		}
 
 	}
 
 }
