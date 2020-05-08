 package org.kalidasya.sonar.erlang.lexer;
 
 import static com.sonar.sslr.api.GenericTokenType.LITERAL;
 
 import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.commentRegexp;
 import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.regexp;
 import static org.kalidasya.sonar.erlang.api.ErlangTokenType.NUMERIC_LITERAL;
 import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.or;
 
 import org.kalidasya.sonar.erlang.ErlangConfiguration;
 import org.kalidasya.sonar.erlang.api.ErlangKeyword;
 import org.kalidasya.sonar.erlang.api.ErlangPunctator;
 
 import com.sonar.sslr.impl.Lexer;
 import com.sonar.sslr.impl.channel.BlackHoleChannel;
 import com.sonar.sslr.impl.channel.IdentifierAndKeywordChannel;
 import com.sonar.sslr.impl.channel.PunctuatorChannel;
 import com.sonar.sslr.impl.channel.UnknownCharacterChannel;
 
 public final class ErlangLexer {
 
 	 private static final String EXP = "([Ee][-]?+[0-9_]++)";
 	
 	private ErlangLexer(){
 	}
 	
 	public static Lexer create(ErlangConfiguration conf){
 		return Lexer.builder()
 				.withChannel(new BlackHoleChannel("\\s++"))
 				.withChannel(commentRegexp("%[^\\n\\r]*+"))
 				.withChannel(regexp(LITERAL, "\"([^\"\\\\]*+(\\\\[\\s\\S])?+)*+\""))
 				.withChannel(regexp(NUMERIC_LITERAL, "[0-9]++\\.([0-9]++)?+" + EXP + "?"))
 				.withChannel(regexp(NUMERIC_LITERAL, "[0-9]++\\#([0-9A-Fa-f]++)?+"))
 				.withChannel(regexp(NUMERIC_LITERAL, "[0-9]++"))
 				.withChannel(new IdentifierAndKeywordChannel(or("('[^'\n\r]+')","(\\p{javaJavaIdentifierStart}++\\p{javaJavaIdentifierPart}*+)"), true, ErlangKeyword.values()))
 				.withChannel(new PunctuatorChannel(ErlangPunctator.values()))
 				.withChannel(new UnknownCharacterChannel(true))
 				.build();
 	}
 	
 }
