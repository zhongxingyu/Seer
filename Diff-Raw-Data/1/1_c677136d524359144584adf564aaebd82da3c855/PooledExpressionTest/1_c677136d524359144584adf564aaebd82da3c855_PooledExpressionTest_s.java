 package layr.core.expressions;
 
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.util.regex.Matcher;
 
 import javax.servlet.ServletException;
 
 import layr.core.RequestContext;
 import layr.core.test.stubs.StubsFactory;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class PooledExpressionTest {
 
 	private static final String CONTEXT = "#{usuario.context}";
 	private static final String NAME = "#{usuario.nome}";
 	private static final String EQUATION = "#{usuario.nome} == 'Miere Teixeira'";
 
 	private RequestContext layrContext;
 
 	@Before
 	public void setup() throws IOException, ClassNotFoundException, ServletException{
 		layrContext = StubsFactory.createFullRequestContext();
 	}
 
 	@Test
 	public void testGetValueSpeed() {
 		layrContext.put("usuario", new Usuario("Miere Teixeira", layrContext));
 		
 		long prev_time, total_time=0, loop_times=1;
 		for (int j=0; j<loop_times; j++) {
 		    prev_time = System.currentTimeMillis();
 			for (int i=0;i<10000; i++) {
 				Object value = ComplexExpressionEvaluator.getValue(CONTEXT, layrContext);
 				assertSame(layrContext, value);
 			}
 			long time = System.currentTimeMillis() - prev_time;
 			total_time+= time;
 		}
 		System.out.println("Total Time:" + total_time);
 	}
 
 	@Test
 	public void testGetValueSpeedWithAnEquation() {
 		layrContext.put("usuario", new Usuario("Miere Teixeira", layrContext));
 		
 		long prev_time, total_time=0, loop_times=1;
 		for (int j=0; j<loop_times; j++) {
 		    prev_time = System.currentTimeMillis();
 			for (int i=0;i<10000; i++) {
 				Object value = ComplexExpressionEvaluator.getValue(EQUATION, layrContext);
 				assertTrue((Boolean)value);
 			}
 			long time = System.currentTimeMillis() - prev_time;
 			total_time+= time;
 		}
 		System.out.println("Total Time:" + total_time);
 	}
 
 	@Test
 	public void notSameMatcher() {
 		Matcher matcher = ComplexExpressionEvaluator.getMatcher(ExpressionEvaluator.RE_IS_VALID_EXPRESSION, NAME);
 		Matcher matcher2 = ComplexExpressionEvaluator.getMatcher(ExpressionEvaluator.RE_IS_VALID_EXPRESSION, NAME);
 		assertNotSame(matcher, matcher2);
 	}
 
 	public class Usuario {
 		private String nome;
 		private RequestContext context;
 		
 		public Usuario(String nome, RequestContext context) {
 			setContext(context);
 			setNome(nome);
 		}
 
 		public void setNome(String nome) {
 			this.nome = nome;
 		}
 
 		public String getNome() {
 			return nome;
 		}
 
 		public void setContext(RequestContext context) {
 			this.context = context;
 		}
 
 		public RequestContext getContext() {
 			return context;
 		}
 	}
 }
