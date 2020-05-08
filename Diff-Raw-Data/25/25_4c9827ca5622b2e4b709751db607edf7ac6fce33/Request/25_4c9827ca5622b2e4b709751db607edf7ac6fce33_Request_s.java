 package net.cheney.motown.api;
 
 import java.net.URI;
 import java.nio.ByteBuffer;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ReflectionToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Multimap;
 
 public class Request extends Message {
 
 	private final RequestLine requestLine;
 
 	private Request(RequestLine requestLine, Multimap<Header, String> headers, ByteBuffer body) {
 		super(headers, body);
 		this.requestLine = requestLine;
 	}
 	
 	public Request(Method method, String uri, Version version) {
 		this(new RequestLine(method, uri, version));
 	}
 	
 	public Request(RequestLine requestLine) {
 		this(requestLine, ArrayListMultimap.<Header, String>create(), null);	
 	}
 	
 	public Method method() {
 		return this.requestLine.method();	
 	}
 	
 	public URI uri() {
 		return this.requestLine.uri();
 	}
 	
 	@Override
 	public Version version() {
 		return this.requestLine.version();
 	}
 	
 	public static Request.Builder builder(Method method, String uri) {
 		return new Builder(method, uri);
 	}
 	
 	@Override
 	public String toString() {
 		return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
 	}
 	
 	@Override
 	public boolean equals(Object that) {
 		return EqualsBuilder.reflectionEquals(this, that);
 	}
 	
 	public int hashCode() {
 		return HashCodeBuilder.reflectionHashCode(this);
 	}
 	
 	public static class Builder extends Message.Builder {
 
 		private Method method;
 		private URI uri;
 		private Multimap<Header, String> headers = ArrayListMultimap.create();
 		private ByteBuffer body = ByteBuffer.allocate(0);
 
 		public Builder(Method method, String uri) {
 			this(method, URI.create(uri));
 		}
 		
 		public Builder(Method method, URI uri) {
 			this.method = method;
 			this.uri = uri;
 		}
 
 		Method method() {
 			return method;
 		}
 		
 		URI uri() {
 			return uri;
 		}
 		
 		@Override
 		Version version() {
 			return Version.HTTP_1_1;
 		}
 		
 		@Override
 		Multimap<Header, String> headers() {
 			return headers;
 		}
 		
 		@Override
 		ByteBuffer body() {
 			return body;
 		}
 
 		@Override
 		public Message build() {
 			return new Request(new RequestLine(method(), uri(), version()), headers(), body());
 		}
 
 		@Override
 		public Builder setBody(ByteBuffer buffer) {
 			this.body = buffer;
 			return this;
 		}
 		
 	}
 
 	@Override
 	public Message setBody(final ByteBuffer body) {
 		return new Builder(method(), uri()) {
 			@Override
 			ByteBuffer body() {
 				return body;
 			}
 		}.build();
 	}
 
 	public Depth getDepth(Depth defaultDepth) {
		return Depth.parse(Iterables.getOnlyElement(headers().get(Header.DEPTH), ""), defaultDepth);
 	}
 
 	public URI getDestination() {
 		return URI.create(getOnlyElement(Header.DESTINATION, ""));
 	}
 
 	public boolean isOverwrite() {
 		return getOnlyElement(Header.OVERWRITE, "T").equals("T");
 	}	
 	
 }
