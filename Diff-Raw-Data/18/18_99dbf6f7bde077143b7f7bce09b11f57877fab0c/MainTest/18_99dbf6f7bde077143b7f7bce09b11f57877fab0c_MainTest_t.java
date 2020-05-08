 package tests;
 
 import oop1.*;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 
 public class MainTest {
 
     @Test
     public void speedTest() throws Exception {
 //        String path = "tmp/1mb.java.resource";
         String path = "tests/Character.java";
         String content = readFile(path, StandardCharsets.UTF_8);
         SimpleLexer lexer = initializeLexer(content);
 
         while (lexer.hasNextTokens()) {
             lexer.readNextToken();
         }
     }
 
     @Test
     public void readEquals() throws Exception {
 //        String path = "tmp/1mb.java.resource";
         String path = "tests/Character.java";
         String content = readFile(path, StandardCharsets.UTF_8);
         SimpleLexer lexer = initializeLexer(content);
 
        StringBuilder read = new StringBuilder();
         while (lexer.hasNextTokens()) {
            read.append(lexer.readNextToken().getText());
         }
 
        assertEquals(content, read.toString());
     }
 
     private SimpleLexer initializeLexer(String content) throws IOException {
         TokenReader[] readers = {
                 new TraditionalCommentReader(),
                 new CharacterReader(),
                 new EndOfLineCommentReader(),
                 new FloatReader(),
                 new IdentifierReader(),
                 new IntReader(),
                 new StringReader(),
                 new WhitespaceReader(),
                 new KeywordReader(),
                 new OperatorReader(),
                 new SeparatorReader(),
                 new AnnotationReader(),
         };
         return new SimpleLexer(content, readers);
     }
 
     static String readFile(String path, Charset encoding) throws IOException {
         byte[] encoded = Files.readAllBytes(Paths.get(path));
         return encoding.decode(ByteBuffer.wrap(encoded)).toString();
     }
 }
