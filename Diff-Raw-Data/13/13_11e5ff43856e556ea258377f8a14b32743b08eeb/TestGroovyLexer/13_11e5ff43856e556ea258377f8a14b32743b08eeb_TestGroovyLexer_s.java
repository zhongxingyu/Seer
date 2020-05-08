 package scripting.groovy;
 
 import junit.framework.TestCase;
 import net.intensicode.idea.core.FakeSystemContext;
 import net.intensicode.idea.scripting.GroovyContext;
 import net.intensicode.idea.syntax.SimpleLexer;
 import net.intensicode.idea.syntax.SimpleToken;
 import net.intensicode.idea.util.StreamUtils;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 public final class TestGroovyLexer extends TestCase
 {
     public final void testLexer() throws IOException
     {
         final ArrayList<SimpleToken> tokens = getLexerOutput();
 
         assertNotNull( tokens );
 
         assertEquals( 3, tokens.size() );
 
         final SimpleToken t1 = tokens.get( 0 );
         assertEquals( 2, t1.start );
         assertEquals( 11, t1.end );
         assertEquals( "LINE_COMMENT", t1.id );
 
         final SimpleToken t2 = tokens.get( 1 );
         assertEquals( 15, t2.start );
         assertEquals( 21, t2.end );
         assertEquals( "KEYWORD", t2.id );
 
         final SimpleToken t3 = tokens.get( 2 );
         assertEquals( 22, t3.start );
         assertEquals( 28, t3.end );
         assertEquals( "DOUBLE_QUOTED_STRING", t3.id );
     }
 
     // Implemention
 
     private final ArrayList<SimpleToken> getLexerOutput() throws IOException
     {
        final GroovyContext context = new GroovyContext( new FakeSystemContext( this, "config" ), new ArrayList<String>() );
         final SimpleLexer lexer = ( SimpleLexer ) context.createObject( "Ruby/Syntax.groovy", SimpleLexer.class );
 
         final String testData = StreamUtils.loadIntoString( getClass().getResourceAsStream( "TestScript.rb" ) );
         lexer.start( testData.toCharArray(), 0, testData.length() );
 
         final ArrayList<SimpleToken> tokens = new ArrayList<SimpleToken>();
         storeTokens( tokens, lexer, 0 );
         return tokens;
     }
 
     private static final void storeTokens( final ArrayList<SimpleToken> aTokens, final SimpleLexer aLexer, final int aStartOffset )
     {
         final SimpleToken token = aLexer.findToken( aStartOffset );
         if ( token == null ) return;
 
         aTokens.add( token );
         storeTokens( aTokens, aLexer, token.end );
     }
 }
