 package net.cheney.motown.protocol.http.common;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.nio.ByteBuffer;
 import java.util.ArrayDeque;
 import java.util.Deque;
 
 import net.cheney.motown.api.Method;
 import net.cheney.motown.api.RequestLine;
 import net.cheney.motown.api.Version;
 
 public class RequestLineParser extends HttpParser<RequestLine> {
 	
 	private enum State {
 		REQUEST_LINE_END, HTTP_VERSION, REQUEST_URI, METHOD
 	}
 	
 	private final Deque<State> stateStack = new ArrayDeque<State>();
 	private Version version;
 	private String url;
 	private Method method;
 	private int offset;
 
 	public RequestLineParser() {
 		reset();
 	}
 	
	@Override
	public void reset() {
 		this.version = null;
 		this.url = null;
 		this.method = null;
 		stateStack.addFirst(State.REQUEST_LINE_END);
 		stateStack.addFirst(State.HTTP_VERSION);
 		stateStack.addFirst(State.REQUEST_URI);
 		stateStack.addFirst(State.METHOD);
 	}
 
	@Override
 	public final RequestLine parse(final ByteBuffer buffer) {
 		// mark the current position of the buffer
 		// if the parse is not complete, we will rewind to the mark()ed position
 		offset = buffer.position();
 		while(buffer.hasRemaining()) {
 			switch (stateStack.peek()) {
 			case METHOD:
 				byte c = buffer.get();
 				if(isTokenChar(c)) {
 					// valid char for HTTP method
 					continue;
 				} else if (c == ' ') {
 					int length = buffer.position() - offset;
 					String s = new String(buffer.array(), buffer.arrayOffset() + offset, --length, US_ASCII);
 					this.method = Method.parse(s);
 					offset = buffer.position();
 					stateStack.pop();
 				} else {
 					throw new IllegalArgumentException(String.format("Illegal character '%s' in %s", (char)c, stateStack.peek()));
 				}
 				break;
 
 			case REQUEST_URI:
 				switch (buffer.get()) {
 				case ' ':
 					int length = buffer.position() - offset;
 					String s = new String(buffer.array(), buffer.arrayOffset() + offset, --length, US_ASCII);
 					try {
 						this.url = URLDecoder.decode(s, UTF_8.displayName());
 					} catch (UnsupportedEncodingException e) {
 						throw new IllegalArgumentException(e);
 					}
 					offset = buffer.position();
 					stateStack.pop();
 					break;
 
 				default:
 					break;
 				}
 				break;
 
 			case HTTP_VERSION:
 				final byte p = buffer.get();
 				switch (p) {
 				case '\r':
 					int length = buffer.position() - offset;
 					String s = new String(buffer.array(), buffer.arrayOffset() + offset, --length, US_ASCII);
 					this.version = Version.parse(s);
 					offset = buffer.position();
 					stateStack.pop();
 					break;
 
 				case 'H':
 				case 'T':
 				case 'P':
 				case '/':
 				case '0':
 				case '1':
 				case '9':
 				case '.':
 					// valid char for HTTP Version
 					break;
 					
 				default:
 					throw new IllegalArgumentException(String.format("Illegal character '%s' in %s", (char)p, stateStack.peek()));
 				}
 				break;
 
 			case REQUEST_LINE_END:
 				switch (buffer.get()) {
 				case '\n':
 					return new RequestLine(method, url, version);
 
 				default:
 					throw new IllegalArgumentException(
 							"\\n was not encountered");
 				}
 			}
 		}
 		buffer.position(offset);
 		return null;
 	}
 	
 }
