 package mustache.parser;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import mustache.core.Context;
 import mustache.core.Instruction;
 import mustache.core.Partial;
 import mustache.core.Processable;
 
 final class Tag {
 	
 	private static final String TAG_CONTENT_REGEX = "^(&|#|\\^|/|\\>|\\=|\\!)?\\s*(.*?)$";
 	private static final Pattern TAG_CONTENT_PATTERN = Pattern.compile(TAG_CONTENT_REGEX, Pattern.DOTALL);
 	
 	private final Type type;
 	private final String content;
 	
 	static Tag newTag(String string) throws ParseException, ChangeDelimiterException {
 		Matcher matcher = TAG_CONTENT_PATTERN.matcher(string);
 		
 		if ( !matcher.matches() ) {
 			throw new ParseException("Invalid tag content : " + string);
 		}
 		
 		Type type = Type.fromToken( matcher.group(1) );
 		String content = matcher.group(2);
 		
 		if ( type != Type.PARTIAL && type.action != null && !Context.isValidQuery(content) ) {
 			throw new ParseException("Invalid tag content : " + content);
 		}
 		
 		Tag tag = new Tag(type, content);
 		
 		if (type == Type.DELIMITER) {
 			throw new  ChangeDelimiterException(tag);
 		}
 		
 		return tag;
 	}
 	
 	static Tag newUnescapedTag(String string) throws ParseException {
 		String content = string.trim();
 		
 		if ( !Context.isValidQuery(content) ) {
 			throw new ParseException("Invalid tag content : " + content);
 		}
 		
 		return new Tag(Type.UNESCAPED_VARIABLE, content);
 	}
 	
 	private Tag(Type type, String content) {
 		this.type = type;
 		this.content = content;
 	}
 
 	boolean canBeStandalone() {
 		return type != Type.VARIABLE & type != Type.UNESCAPED_VARIABLE;
 	}
 
 	boolean isPartial() {
 		return type == Type.PARTIAL;
 	}
 	
 	Processable toProcessable() {
		if (type.action == null) {
			return null;
		}
 		if (type == Type.PARTIAL) {
 			return Partial.newInstance(content);
 		}
 		return Instruction.newInstance(type.action, content);
 	}
 	
 	private enum Type {
 		VARIABLE("", Instruction.Action.APPEND_VARIABLE),
 		UNESCAPED_VARIABLE("&", Instruction.Action.APPEND_UNESCAPED_VARIABLE),
 		SECTION("#", Instruction.Action.OPEN_SECTION),
 		INVERTED_SECTION("^", Instruction.Action.OPEN_INVERTED_SECTION),
 		SECTION_END("/", Instruction.Action.CLOSE_SECTION),
 		PARTIAL(">", null),
 		DELIMITER("=", null),
 		COMMENT("!", null);
 		
 		private final String token;
 		private final Instruction.Action action;
 		
 		private Type(String token, Instruction.Action action) {
 			this.token = token;
 			this.action = action;
 		}
 		
 		private static Type fromToken(String token) {
 			for (Type type : values()) {
 				if (type.token.equals(token)) {
 					return type;
 				}
 			}
 			return VARIABLE;
 		}
 	}
 }
