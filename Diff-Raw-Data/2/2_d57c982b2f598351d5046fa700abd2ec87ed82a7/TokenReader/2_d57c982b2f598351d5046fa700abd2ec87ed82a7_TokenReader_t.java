 package parser.java;
 
 
 public abstract class TokenReader {
   final public static int End   = -1;
   final public static int Error = -2;
   
   final public byte[] data;
   final public String encoding;
 
   public int  position = -1;
   public int  line     =  0;
   
   public TokenReader(parser.Source src) {
    this.data = utils.ByteArrayReader.apply(src.uri());
     this.encoding = src.encoding();
   }
 	  
   public TokenReader(byte[] data, String encoding) {
 	this.data = data;
 	this.encoding = encoding;
   }
   
   /**
    * Used to write a Token. Useful for debug.
    */
   protected abstract String tokenName(int kind);
   
   /** All fields should be seen as final for practical use. */
   public abstract class TokenBuf {
     public int pos    = 0;
     public int kind   = TokenReader.End;
     public int length = 0;
     public final int end()    { return pos+length-1; } //last  character position in data flow
     public final String infoString() { return baseString() + " ["+pos+","+end()+"]"; }
     public final String longString() { return infoString() + " " + tokenName(kind); }
     public String baseString() { //the String as read
       try {
     	if (kind==TokenReader.End) return "<<EOF>>";
     	if (data[pos]=='"') {
        	    byte[] b = new byte[length];
     	    int i=0;
     	    int j=0;
     	    for (i=0; i<length; i++) { byte c=data[pos+i]; if (c=='\\' && (data[pos+i+1]=='\\' || data[pos+i+1]=='"')) i+=1; b[j++]=data[pos+i]; }
             return new String(b,0,j,encoding);
     	} else {
     		return new String(data,pos,length,encoding);
     	}
       } catch (Exception e) {
         throw new RuntimeException(e);
       }
     }
     public String unquotedString() { //the String unquoted
       String s = baseString();
       return (s.charAt(0)=='"' && s.charAt(s.length()-1)=='"') ? s.substring(1,s.length()-1) : s;
     }
     public String toString() { return baseString(); }
     
     /** Fills this TokenBuf. */
     abstract public void fill();
   }
 
 }
