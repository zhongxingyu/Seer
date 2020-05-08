 package com.github.stkurilin.routes;
 
 import org.mockito.ArgumentMatcher;
 import org.mockito.Matchers;
 import org.testng.annotations.Test;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import static com.github.stkurilin.routes.RulesReaderTest.Checker.call;
 import static com.google.common.collect.ImmutableList.of;
 import static org.mockito.Matchers.argThat;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 /**
  * @author Stanislav Kurilin
  */
 public class RulesReaderTest {
     @Test
     public void testSimplestCase() {
         when("GET /foo com.github.stkurilin.routes.Foo#foo")
                 .then(call().method(Method.GET)
                         .uriSpec(literal("foo"))
                         .methodId("foo")
                         .clazz(Foo.class));
 
     }
 
     @Test
     public void testSeveralRules() {
         when("GET /foo com.github.stkurilin.routes.Foo#foo", "GET /bar com.github.stkurilin.routes.Foo#bar")
                 .then(
                         call()
                                 .method(Method.GET)
                                 .uriSpec(literal("foo"))
                                 .methodId("foo")
                                 .clazz(Foo.class),
                         call()
                                 .method(Method.GET)
                                 .uriSpec(literal("bar"))
                                 .methodId("bar")
                                 .clazz(Foo.class)
                 );
     }
 
 
     @Test
     public void testImport() {
         when("import com.github.stkurilin.routes.Foo", "GET /foo Foo#foo")
                 .then(call()
                         .methodId("foo")
                         .clazz(Foo.class));
     }
 
     @Test
     public void testMatcher() {
         when("GET /foo/{id} com.github.stkurilin.routes.Foo#foo")
                 .then(call()
                         .method(Method.GET)
                         .uriSpec(literal("foo"), matcher("id")));
     }
 
     @Test
     public void testNoArgsDeclaration() {
         when("GET /foo/ com.github.stkurilin.routes.Foo#foo")
                 .then(call().args(new ArrayList<String>()));
     }
 
     @Test
     public void testEmptyArgsDeclaration() {
         when("GET /foo/ com.github.stkurilin.routes.Foo#foo()")
                 .then(call().args(new ArrayList<String>()));
     }
 
     @Test
     public void testSingleArg() {
         when("GET /foo/{id} com.github.stkurilin.routes.Foo#foo(id)")
                 .then(call()
                         .uriSpec(literal("foo"), matcher("id"))
                         .args(of("id")));
     }
 
     @Test
     public void testSeveralArgs() {
         when("GET /foo/{year}/{month} com.github.stkurilin.routes.Foo#foo(year, month)")
                 .then(call()
                         .uriSpec(literal("foo"), matcher("year"), matcher("month"))
                         .args(of("year", "month")));
     }
 
 
     @Test
     public void testTemplate() {
         when("GET /foo com.github.stkurilin.routes.Foo#foo() foo.template")
                 .then(call().template("foo.template"));
     }
 
    @Test
     public void testTemplateInNonArgedMethod() {
         when("GET /foo com.github.stkurilin.routes.Foo#foo foo.template")
                 .then(call().template("foo.template"));
     }
 
    @Test
     public void testTemplateWorksWellInSeveralRulesCase() {
        when("GET /foo com.github.stkurilin.routes.Foo#foo foo.template",
                "GET /bar com.github.stkurilin.routes.Foo#bar")
                 .then(call()
                         .methodId("foo")
                         .template("foo.template"),
                         call()
                                .methodId("bar")
                 );
     }
 
 
     //Infrastructure goes here
 
     static UriSpec.Item literal(final String value) {
         return new UriSpec.Item() {
             @Override
             public <R> R apply(UriSpec.ItemVisitor<R> visitor) {
                 return visitor.literal(value);
             }
         };
     }
 
     static UriSpec.Item matcher(final String name) {
         return new UriSpec.Item() {
             @Override
             public <R> R apply(UriSpec.ItemVisitor<R> visitor) {
                 return visitor.matcher(name);
             }
         };
     }
 
     private Fixture when(String... fileLines) {
         return new Fixture(fileLines);
     }
 
     private static class Fixture {
         private final RuleCreator ruleCreator = mock(RuleCreator.class);
         private final RulesReader rulesReader = new RulesReader(ruleCreator);
         private final String[] inp;
 
         private Fixture(String[] inp) {
             this.inp = inp;
         }
 
         public void then(Checker... calls) {
             for (String each : inp) rulesReader.apply(new StringReader(each));
             for (Checker each : calls) {
                 each.check(ruleCreator);
 
             }
         }
     }
 
 
     public static class Checker {
         private Method method;
         private UriSpecMatcher uriSpec;
         private Class<?> clazz;
         private String methodId;
         private List<String> args;
        private String template = "";
 
         private Checker() {
         }
 
         public static Checker call() {
             return new Checker();
         }
 
         public Checker method(Method method) {
             this.method = method;
             return this;
         }
 
         public Checker uriSpec(UriSpec.Item... items) {
             this.uriSpec = new UriSpecMatcher(Arrays.asList(items));
             return this;
         }
 
         public Checker clazz(Class<?> clazz) {
             this.clazz = clazz;
             return this;
         }
 
         public Checker methodId(String methodId) {
             this.methodId = methodId;
             return this;
         }
 
         public Checker args(List<String> args) {
             this.args = args;
             return this;
         }
 
         public Checker template(String template) {
             this.template = template;
             return this;
         }
 
         public void check(RuleCreator ruleCreator) {
             verify(ruleCreator).apply(
                     method == null ? Matchers.<Method>any() : eq(method),
                     uriSpec == null ? Matchers.<UriSpec>any() : argThat(uriSpec),
                     argThat(new TargetSpecMatcher(clazz, methodId, args)),
                    template.isEmpty() ? Matchers.<String>any() : eq(template));
         }
     }
 
     private static class UriSpecMatcher extends ArgumentMatcher<UriSpec> {
         private final Iterable<UriSpec.Item> items;
 
         private UriSpecMatcher(Iterable<UriSpec.Item> items) {
             this.items = items;
         }
 
         @Override
         public boolean matches(Object argument) {
             if (!(argument instanceof UriSpec)) return false;
             final Iterator<UriSpec.Item> rightIt = ((UriSpec) argument).path().iterator();
             for (UriSpec.Item left : items) {
                 if (!rightIt.hasNext()) return false;
                 final UriSpec.Item right = rightIt.next();
                 if (!left.apply(new UriSpec.ItemVisitor<Boolean>() {
                     @Override
                     public Boolean literal(final String leftValue) {
                         return right.apply(new UriSpec.ItemVisitor<Boolean>(false) {
                             @Override
                             public Boolean literal(String value) {
                                 return value.equals(leftValue);
                             }
                         });
                     }
 
                     @Override
                     public Boolean matcher(final String leftName) {
                         return right.apply(new UriSpec.ItemVisitor<Boolean>(false) {
                             @Override
                             public Boolean matcher(String name) {
                                 return leftName.equals(name);
                             }
                         });
                     }
                 })) return false;
             }
             return !rightIt.hasNext();
         }
     }
 
     private static class TargetSpecMatcher extends ArgumentMatcher<TargetSpec> {
         private Class<?> clazz;
         private String methodId;
         private List<String> args;
 
         private TargetSpecMatcher(Class<?> clazz, String methodId, List<String> args) {
             this.clazz = clazz;
             this.methodId = methodId;
             this.args = args;
         }
 
         @Override
         public boolean matches(Object argument) {
             if (!(argument instanceof TargetSpec))
                 return false;
             final TargetSpec right = (TargetSpec) argument;
             if (clazz != null && !clazz.equals(right.clazz()))
                 return false;
             if (methodId != null && !methodId.equals(right.methodId()))
                 return false;
             if (args != null && !args.equals(right.args()))
                 return false;
             return true;
         }
     }
 }
