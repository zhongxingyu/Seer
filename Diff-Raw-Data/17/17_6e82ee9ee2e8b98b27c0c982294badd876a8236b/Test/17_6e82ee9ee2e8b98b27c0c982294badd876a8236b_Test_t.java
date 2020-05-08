import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
 
 public class Test {
    public static void main(String[] args) throws Exception {
        PureLexer lex = new PureLexer(new ANTLRStringStream("7-3*5+2"));
       	CommonTokenStream tokens = new CommonTokenStream(lex);
 
       	PureParser parser = new PureParser(tokens);

        try {
            System.out.println(parser.expr());
        } catch (RecognitionException e)  {
            e.printStackTrace();
        }
    }
 }
