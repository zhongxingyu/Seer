 package net.citizensnpcs.adventures.dialog;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
 
 import java.util.Map;
 
 import net.citizensnpcs.adventures.dialog.evaluators.VariableSource;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.collect.Maps;
 
 public class ExpressionTest {
     private VariableSource source;
     private Map<String, Object> variables;
 
     @Test
     public void arrayLiteral() {
         assertThat((String[]) parse("['test']"), equalTo(new String[] { "test" }));
         assertThat((Integer[]) parse("[0, 1]"), equalTo(new Integer[] { 0, 1 }));
     }
 
     @Test
     public void booleanLiteral() {
         assertThat((Boolean) parse("false"), equalTo(false));
         assertThat((Boolean) parse("true"), equalTo(true));
     }
 
     @Test
     public void math() {
         assertThat((Integer) parse("1+2"), equalTo(1 + 2));
         assertThat((Integer) parse("1+(2*3)"), equalTo(1 + (2 * 3)));
         assertThat((Double) parse("7.0/2.0"), equalTo(7.0 / 2.0));
         assertThat((Integer) parse("5%2"), equalTo(5 % 2));
         assertThat((Integer) parse("6+(2*(3/2)) + 100 / 20 % 2 - 10"), equalTo(6 + (2 * (3 / 2)) + 100 / 20 % 2 - 10));
     }
 
     @Test
     public void numberLiteral() {
         assertThat((Double) parse("1.5"), equalTo(1.5D));
         assertThat((Double) parse("-1.15"), equalTo(-1.15D));
         assertThat((Integer) parse("100"), equalTo(100));
         assertThat((Long) parse("100000000000000000"), equalTo(100000000000000000L));
     }
 
     @SuppressWarnings("unchecked")
     private <T> T parse(String string) {
         DialogLexer lexer = new DialogLexer(new ANTLRStringStream(string));
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         DialogParser parser = new DialogParser(tokens);
         parser.setVariableSource(source);
         try {
             return (T) parser.expression().get();
         } catch (RecognitionException e) {
             e.printStackTrace();
         } catch (DialogException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     @Before
     public void setUp() {
         variables = Maps.newHashMap();
         source = new VariableSource() {
             @Override
             public Object getVariable(String key) {
                 return variables.get(key);
             }
         };
     }
 
     @Test
     public void stringLiteral() {
         assertThat((String) parse("'test'"), equalTo("test"));
         assertThat((String) parse("\"test\""), equalTo("test"));
     }
 
     @Test
     public void testMap() {
         Map<String, Boolean> test = Maps.newHashMap();
         test.put("false", false);
         test.put("true", true);
         Map<String, Boolean> res = parse("{\"false\" : false, 'true' : true}");
         assertThat(res, equalTo(test));
     }
 
     @Test
     public void variable() {
         variables.put("test", false);
         variables.put("test2.a.p", true);
         for (String key : variables.keySet()) {
             assertThat(parse(key), equalTo(variables.get(key)));
         }
     }
 }
