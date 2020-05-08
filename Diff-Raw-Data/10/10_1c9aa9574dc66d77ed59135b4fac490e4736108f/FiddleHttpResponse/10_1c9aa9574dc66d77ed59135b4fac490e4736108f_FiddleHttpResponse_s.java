 package fiddle.httpclient;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.annotation.Nullable;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.entity.ContentType;

import wiremock.org.mortbay.log.Log;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.collect.Lists;
 
 public class FiddleHttpResponse {
 
 	private final int status;
 	private final String statusReason;
 	private final byte[] body;
 	private final Optional<Charset> charset;
 
 	private final HttpResponse response;
 
 	protected FiddleHttpResponse(final HttpResponse response) {
 		this.status = response.getStatusLine().getStatusCode();
 		this.statusReason = response.getStatusLine().getReasonPhrase();
 
 		this.response = response;
 
 		final HttpEntity entity = response.getEntity();
 
 		this.charset = Optional.fromNullable(ContentType.getOrDefault(entity)
 				.getCharset());
 
 		if (this.is2XX()) {
 			try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
 				entity.writeTo(baos);
 				body = baos.toByteArray();
 				// text =
 				// Optional.of(baos.toString((charset.or(Charset.defaultCharset())).name()));
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		} else {
 			body = new byte[] {};
 		}
 
 	}
 
 	public String body() {
 		try {
 			return new String(body,
 					(charset.or(Charset.defaultCharset())).name());
 		} catch (UnsupportedEncodingException ee) {
			Log.warn(
 					"failed to create String from found charset ("
 							+ charset.toString()
 							+ "), will return default encoded string", ee);
 			return new String(body);
 		}
 	}
 
 	public Header[] headers() {
 		return response.getAllHeaders();
 	}
 
 	public List<String> headerValues(final String type) {
 		return Lists.transform(Arrays.asList(response.getHeaders(type)),
 				new Function<Header, String>() {
 
 					@Override
 					public String apply(@Nullable Header header) {
 						return header.getValue();
 					}
 				});
 	}
 
 	public Optional<String> firstHeader(final String type) {
 		return Optional.fromNullable(response.getFirstHeader(type)).transform(
 				new Function<Header, String>() {
 
 					@Override
 					public String apply(@Nullable Header header) {
 						return header.getValue();
 					}
 				});
 	}
 
 	public boolean is2XX() {
 		return status >= 200 && status < 300;
 	}
 
 	public boolean is3XX() {
 		return status >= 300 && status < 400;
 	}
 
 	public boolean is4XX() {
 		return status >= 400 && status < 500;
 	}
 
 	public boolean is5XX() {
 		return status >= 500 && status < 600;
 	}
 	
 	public int status() {
 		return status;
 	}
 	
 	public String statusReason() {
 		return statusReason;
 	}
 
 	@Override
 	public String toString() {
 		return status + " [" + body() + "]";
 	}
 }
