 package net.cheney.motown.common.api;
 
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ReflectionToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 import org.apache.commons.lang.time.FastDateFormat;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Multimap;
 
 import static net.cheney.motown.common.api.Header.CONTENT_TYPE;
 import static net.cheney.motown.common.api.Header.DATE;
 import static net.cheney.motown.common.api.Status.*;
 import static net.cheney.motown.common.api.Version.HTTP_1_1;
 
 public final class Response extends Message {
 
 	protected static final FastDateFormat RFC1123_DATE_FORMAT = FastDateFormat.getInstance("EEE, dd MMM yyyy HH:mm:ss zzz", TimeZone.getTimeZone("GMT"), Locale.US);
 	
 	protected final StatusLine statusLine;
 
 	private Response(StatusLine statusLine, Multimap<Header, String> headers, ByteBuffer body) {
 		super(headers, body);
 		header(DATE).set(RFC1123_DATE_FORMAT.format(System.currentTimeMillis()));
 		this.statusLine = statusLine;
 	}
 	
 	private Response(StatusLine statusLine, Multimap<Header, String> headers, FileChannel body) {
 		super(headers, body);
 		header(DATE).set(RFC1123_DATE_FORMAT.format(System.currentTimeMillis()));
 		this.statusLine = statusLine;
 	}
 
 	public static Response build(Status status) {
 		return new Response(new StatusLine(HTTP_1_1, status), ArrayListMultimap.<Header, String>create(), ByteBuffer.allocate(0));
 	}
 	
 	public Response(Status status, Multimap<Header, String> headers, ByteBuffer body) {
 		this(new StatusLine(HTTP_1_1, status), headers, body);
 	}
 
 	public Status status() {
 		return this.statusLine.status();
 	}
 	
 	@Override
 	public Version version() {
 		return this.statusLine.version();
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
 	
 	public static Response successCreated() {
 		return Response.build(SUCCESS_CREATED);
 	}
 	
 	public static Response successNoContent() {
 		return Response.build(SUCCESS_NO_CONTENT);
 	}
 
 	public static Response serverErrorInternal() {
 		return Response.build(SERVER_ERROR_INTERNAL);
 	}
 
 	public static Response serverErrorNotImplemented() {
 		return Response.build(SERVER_ERROR_NOT_IMPLEMENTED);
 	}
 
	public static Message clientErrorNotFound() {
 		return Response.build(CLIENT_ERROR_NOT_FOUND);
 	}
 
	public static Message clientErrorPreconditionFailed() {
 		return Response.build(CLIENT_ERROR_PRECONDITION_FAILED);
 	}
 	
	public static Message clientErrorMethodNotAllowed() {
 		return Response.build(CLIENT_ERROR_METHOD_NOT_ALLOWED);
 	}
 	
	public static Message clientErrorConflict() {
 		return Response.build(CLIENT_ERROR_CONFLICT);
 	}
 	
	public static Message clientErrorLocked() {
 		return Response.build(CLIENT_ERROR_LOCKED);
 	}
 
	public static Message clientErrorUnsupportedMediaType() {
 		return Response.build(CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
 	}
 	
	public static Message redirectionNotModified() {
 		return Response.build(REDIRECTION_NOT_MODIFIED);
 	}
 	
 
	public static Message success(MimeType mime, ByteBuffer buffer) {
 		return Response.build(SUCCESS_OK).header(CONTENT_TYPE).set(mime.toString()).setBody(buffer);
 	}
 
 	public boolean mayContainBody() {
 		// http://tools.ietf.org/html/draft-ietf-httpbis-p1-messaging-08#section-3.4
 		return (!status().isInformational() && !status().equals(SUCCESS_NO_CONTENT) && !status().equals(REDIRECTION_NOT_MODIFIED));
 	}
 }
