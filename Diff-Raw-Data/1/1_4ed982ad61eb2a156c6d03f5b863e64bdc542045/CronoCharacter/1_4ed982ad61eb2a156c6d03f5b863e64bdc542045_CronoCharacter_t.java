 package crono.type;
 
 public class CronoCharacter extends CronoPrimitive {
     public static final TypeId TYPEID = new TypeId(":char",
 						   CronoCharacter.class,
 						   CronoPrimitive.TYPEID);
     
     public final char ch;
     
     public CronoCharacter(String image) {
 	int strlen = image.length();
 	switch(strlen) {
 	case 3:
 	    ch = image.charAt(1);
 	    break;
 	case 4:
 	    ch = CronoCharacter.escape(image.charAt(2));
	    break;
 	default:
 	    throw new UnsupportedOperationException("Bad image length");
 	}
     }
     public CronoCharacter(char ch) {
 	this.ch = ch;
     }
     
     public TypeId typeId() {
 	return TYPEID;
     }
     
     public String toString() {
 	StringBuilder builder = new StringBuilder();
 	builder.append('\'');
 	builder.append(CronoCharacter.unescape(ch));
 	builder.append('\'');
 	return builder.toString();
     }
     
     public boolean equals(Object o) {
 	return ((o instanceof CronoCharacter) && ((CronoCharacter)o).ch == ch);
     }
     
     public static char escape(char ch) {
 	switch(ch) {
 	case 'n':
 	    return '\n';
 	case 'r':
 	    return '\r';
 	case 't':
 	    return '\t';
 	case 'b':
 	    return '\b';
 	case 'f':
 	    return '\f';
 	default:
 	    return ch;
 	}
     }
     public static String unescape(char ch) {
 	switch(ch) {
 	case '\n':
 	    return "\\n";
 	case '\r':
 	    return "\\r";
 	case '\t':
 	    return "\\t";
 	case '\b':
 	    return "\\b";
 	case '\f':
 	    return "\\f";
 	default:
 	    return ((Character)ch).toString();
 	}
     }
 }
