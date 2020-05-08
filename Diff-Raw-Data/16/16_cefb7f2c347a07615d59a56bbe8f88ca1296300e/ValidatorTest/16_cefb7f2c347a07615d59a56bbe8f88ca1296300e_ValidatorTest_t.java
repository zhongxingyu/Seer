 package xhl.core.validator;
 
 import java.io.IOException;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import xhl.core.EvaluationException;
 import xhl.core.LanguageProcessor;
 import xhl.core.Reader;
 import xhl.core.elements.Block;
 import xhl.core.elements.Symbol;
 import xhl.core.validator.ElementSchema.DefSpec;
 
 import static org.junit.Assert.*;
 
 import static xhl.core.validator.ElementSchema.ParamSpec.*;
 
 import static com.google.common.collect.Lists.newArrayList;
 
 public class ValidatorTest {
 
     private Schema schema;
 
     @Before
     public void setUp() {
         ElementSchema plus = new ElementSchema(new Symbol("+"));
         plus.setParams(newArrayList(val(Type.Number), val(Type.Number)));
         plus.setType(Type.Number);
         ElementSchema const_ = new ElementSchema(new Symbol("const"));
         const_.setParams(newArrayList(sym(Type.Symbol), val(Type.Number)));
         const_.setType(Type.Null);
         const_.addDefine(new DefSpec(1, Type.Number));
         schema = new Schema();
         schema.put(plus);
         schema.put(const_);
     }
 
     @Test
     public void validatorLanguage() throws EvaluationException, IOException {
         String code = "element (+):\n"
                     + "  params [val Number, val Number]\n"
                     + "  type Number\n"
                     + "element const:\n"
                     + "  params [sym Symbol, val Number]\n"
                     + "  defines 1 Number\n";
         ValidatorLanguage lang = new ValidatorLanguage();
         LanguageProcessor proc = new LanguageProcessor(lang);
 //        List<Error> errors = proc.validate(Reader.read(code));
 //        assertTrue(errors.isEmpty());
         proc.execute(Reader.read(code));
        Schema schema = lang.getReadSchema();
         Symbol plus = new Symbol("+");
         assertTrue(schema.containsKey(plus));
         ElementSchema elem = schema.get(plus);
         assertEquals(plus, elem.getSymbol());
         assertEquals(this.schema.get(plus).getParams(), elem.getParams());
         assertEquals(this.schema.get(plus).getType(), elem.getType());
         Symbol const_ = new Symbol("const");
         assertTrue(schema.containsKey(const_));
         elem = schema.get(const_);
         assertEquals("const", elem.getSymbol().getName());
         assertEquals(this.schema.get(const_).getParams(), elem.getParams());
         assertEquals(this.schema.get(const_).getType(), elem.getType());
     }
 
     @Test
     public void simpleLanguageValid() throws IOException {
         Block code = Reader.read("1+(2+3)");
         Validator v = new Validator();
         v.addElements(schema);
         assertEquals(Type.Block, v.check(code)); // FIXME Number?
         assertTrue(v.getErrors().isEmpty());
     }
 
     @Test
     public void simpleLanguageInvalid() throws IOException {
         Block code = Reader.read("1+(2+\"String\")");
         Validator v = new Validator();
         v.addElements(schema);
         assertEquals(Type.Block, v.check(code));
         assertFalse(v.getErrors().isEmpty());
     }
 
     @Test
     public void definitionValid() throws IOException {
         Block code = Reader.read("const pi 3.14\n1+(2+pi)");
         Validator v = new Validator();
         v.addElements(schema);
         assertEquals(Type.Block, v.check(code)); // FIXME Number?
         assertTrue(v.getErrors().isEmpty());
     }
 }
