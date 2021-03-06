 // JxpConverter.java
 
 package ed.appserver.templates;
 
 import java.util.*;
 
 import ed.util.*;
 
 public class JxpConverter extends HtmlLikeConverter {
 
     public JxpConverter(){
         this( false );
     }
 
     public JxpConverter( boolean dotHtmlMode ){
         super( dotHtmlMode ? ".html" : ".jxp" ,
                dotHtmlMode ? _codeTagsHtml : _codeTagsJxp );
         _dotHtmlMode = dotHtmlMode;
     }
 
     protected void start( Generator g ){
         if ( _dotHtmlMode )
             g.append( "var obj = arguments[0];\n" );
     }
 
     protected Generator createGenerator( Template t , State s ){
         return new MyGenerator( s );
     }
     
     protected String getNewName( Template t ){
         return t.getName().replaceAll( "\\.(jxp|html)+$" , "_$1.js" );
     }
 
     protected void gotCode( Generator g , CodeMarker cm , String code ){
 
         if ( cm._startTag.equals( "<%=" ) ){
             g.append( "print( " );
             g.append( code );
             g.append( " );\n" );
             return;
         }
 
         if ( cm._startTag.equals( "<%" ) ){
             g.append( code );
             g.append( "\n" );
             return;
         }
         
         if ( cm._startTag.equals( "$" ) ){
             g.append( "print( " );
             g.append( "obj." );
             g.append( code );
             g.append( ");\n" );
             return;
         }
         
         throw new RuntimeException( "can't handle : " + cm._startTag );
     }
 
     protected boolean gotStartTag( Generator gg , String tag , State state ){
         MyGenerator g = (MyGenerator)gg;
         
         TagEnd te = null;
         
         try {
             if ( state.peek() == '?' ){
                 state.next();
                 state.eatWhiteSpace();
                 
                 int end = getJSTokenEnd( state.data , state.pos );
                 
                 StringBuilder cond = new StringBuilder();
                 while ( state.pos < end )
                     cond.append( state.next() );
                 
                 g.append( "if ( " + cond + " ){\n" );
                 
                 te = new TagEnd( " }\n" , true );
                 return false;
             }
             
             final String[] macro = _tags.get( tag );
             
             if ( macro != null ){
                 String open = macro[0];
                 String close = macro[1];
                 
                 int tokenNumbers = 1;
 
                 String restOfTag = state.readRestOfTag();
                 
                 while ( restOfTag.length() > 0 ){
                     
                     while ( restOfTag.length() > 0 && 
                             Character.isWhitespace( restOfTag.charAt( 0 ) ) )
                         restOfTag = restOfTag.substring( 1 );
                     
                     if ( restOfTag.length() == 0 )
                         break;
                     
                     int end = getJSTokenEnd( restOfTag , 0 );
                     final String token = restOfTag.substring( 0 , end ).trim();
                     restOfTag = restOfTag.substring( end ).trim();
                     
                     if ( token.length() == 0 )
                         break;
                     
                     open = open.replaceAll( "\\$" + tokenNumbers , token );
                     
                     tokenNumbers++;
                 }
                 
                 g.append( open );
                 te = new TagEnd( close , false );
                 
                 return true;
             }
             
             return false;
         }
         finally {
             g.tagPush( tag , te );
         }
     }
     
     protected boolean gotEndTag( Generator gg , String tag , State state ){
         state.readRestOfTag();
         
         MyGenerator g = (MyGenerator)gg;
 
         TagEnd te = g.tagPop( tag );
         
         if ( te == null || te._printTag )
             gotText( g , "</" + tag + ">" );
 
         if ( te != null )
             g.append( te._code );
 
         return true;
     }
     
     protected void gotText( Generator g , String text ){
         
        final boolean endsWithNewLine = text.endsWith( " " );
 
         final String lines[] = text.split( "[\r\n]+" );
         for ( int i=0; i<lines.length; i++ ){
             String line = lines[i];
 
             line = StringUtil.replace( line , "\\" , "\\\\" );
             line = StringUtil.replace( line , "\"" , "\\\"" );
             g.append( "print( \"" + line + ( i + 1 < lines.length || endsWithNewLine ? "\\n" : "" )  + "\" );\n" );
         }
     }
 
     static int getJSTokenEnd( String data , final int start ){
         
         int parens = 0;
         int end = start;
 
         for ( ; end < data.length(); end++ ){
             char temp = data.charAt( end );
             
             if ( temp == '(' ){
                 parens++;
                 continue;
             }
             
             if ( temp == ')' ){
                 parens--;
                 continue;
             }
             
             if ( parens > 0 )
                 continue;
 
             if ( Character.isLetterOrDigit( temp ) 
                  || temp == '.' 
                  || temp == '_' )
                 continue;
 
             if ( Character.isWhitespace( temp ) )
                 return end;
             
             if ( temp == '\''
 		 || temp == ';' 
 		 || temp == '#' 
                  || temp == '<'
                  || temp == '>'
                  || temp == '"'
                  || temp == ':' )
                 return end;
         }
         
         return end;
     }
 
     class TagEnd {
         TagEnd( String code , boolean printTag ){
             _code = code;
             _printTag = printTag;
         }
         
         final String _code;
         final boolean _printTag;
     }
 
     class MyGenerator extends Generator {
         MyGenerator( State s ){
             super( s );
         }
         
         void tagPush( String tag , TagEnd te ){
             tag = tag.toLowerCase();
 
             Stack<TagEnd> s = _tagToStack.get( tag );
             if ( s == null ){
                 s = new Stack<TagEnd>();
                 _tagToStack.put( tag , s );
             }
             s.push( te );
         }
         
         TagEnd tagPop( String tag ){ 
             tag = tag.toLowerCase();
             
             Stack<TagEnd> s = _tagToStack.get( tag );
             if ( s == null )
                 return null;
             if ( s.size() == 0 )
                 return null;
             return s.pop();
         }
 
         final Map<String,Stack<TagEnd>> _tagToStack = new HashMap<String,Stack<TagEnd>>();
     }
     
     final boolean _dotHtmlMode;
 
     static List<CodeMarker> _codeTagsJxp = new ArrayList<CodeMarker>();
     static List<CodeMarker> _codeTagsHtml = new ArrayList<CodeMarker>();
     static {
         _codeTagsJxp.add( new CodeMarker( "<%=" , "%>" ) );
         _codeTagsJxp.add( new CodeMarker( "<%" , "%>" ) );
 
         _codeTagsHtml.addAll( _codeTagsJxp );
         _codeTagsHtml.add( new CodeMarker( "$" , null ){
                 int findEnd( String data , int start ){
                     return getJSTokenEnd( data , start );
                 }
             } );
     }
 
     static Map<String,String[]> _tags = Collections.synchronizedMap( new StringMap<String[]>() );
     
     static {
         _tags.put( "if" , new String[]{ 
                 " if ( $1 ){ " , " } "
             } );
         
         _tags.put( "forin" , new String[]{
                 " for ( $1 in $2 ){\n  " , "\n } \n "
             } );
         
         _tags.put( "forarray" , new String[]{
                 " for ( var $3=0; $3 < $2 .length; $3 ++ ){\n var $1 = $2[$3]; \n " , " \n } \n " 
             } );
     }
 }
