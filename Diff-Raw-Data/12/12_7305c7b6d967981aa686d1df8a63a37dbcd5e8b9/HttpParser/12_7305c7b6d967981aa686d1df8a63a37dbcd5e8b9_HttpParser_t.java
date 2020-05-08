 package net.cheney.motown.protocol.http.common;
 
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 
 public abstract class HttpParser<V> {
 
 	static final Charset US_ASCII = Charset.forName("US-ASCII");
 	static final Charset UTF_8 = Charset.forName("UTF-8");
 	
 	public abstract V parse(ByteBuffer buffer);
 
 	final boolean isWhitespace(byte t) {
 		return (t == ' ' || t == '\t');
 	}
 
 	final boolean isVisibleCharacter(byte t) {
 		return (t >= '\u0021' && t <= '\u007E');
 	}
 
	public abstract void reset();
 
 	final boolean isTokenChar(byte b) {
 		return ((b >= '\u0030' && b <= '\u0039')
 				|| (b >= '\u0041' && b <= '\u005A')
 				|| (b >= '\u0061' && b <= '\u007a') || b == '!' || b == '#'
 				|| b == '$' || b == '%' || b == '&' || b == '\'' || b == '*'
 				|| b == '+' || b == '-' || b == '.' || b == '^' || b == '_'
 				|| b == '`' || b == '|' || b == '~');
 	}
 }
