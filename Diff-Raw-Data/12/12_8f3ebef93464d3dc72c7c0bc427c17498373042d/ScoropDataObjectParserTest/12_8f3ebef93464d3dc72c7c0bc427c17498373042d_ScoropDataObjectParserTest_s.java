 package net.kaczmarzyk.moose.core.parser;
 
 import static net.kaczmarzyk.moose.core.utils.DataObjectUtil.relObjAddr;
 import static org.junit.Assert.assertEquals;
 
 import java.util.Arrays;
 
 import net.kaczmarzyk.moose.core.document.Dimension;
 import net.kaczmarzyk.moose.core.document.Document;
 import net.kaczmarzyk.moose.core.document.Formula;
 import net.kaczmarzyk.moose.core.document.Path;
 import net.kaczmarzyk.moose.core.document.Sheet;
 import net.kaczmarzyk.moose.core.expression.AreaReference;
 import net.kaczmarzyk.moose.core.expression.Expression;
 import net.kaczmarzyk.moose.core.function.Abs;
 import net.kaczmarzyk.moose.core.function.Add;
 import net.kaczmarzyk.moose.core.function.SpringFunctionRegistry;
 import net.kaczmarzyk.moose.support.utils.ReflectionUtil;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class ScoropDataObjectParserTest {
 
 	private Document doc = new Document("test doc");
 	private Sheet sheet1 = doc.getSheet("Sheet1");
 	private Dimension<?> s1dimension1 = sheet1.getDimensions().get(0);
 	private Dimension<?> s1dimension2 = sheet1.getDimensions().get(1);
 	private Sheet sheet2 = doc.getSheet("Sheet2");
 	private Dimension<?> s2dimension1 = sheet2.getDimensions().get(0);
 	private Dimension<?> s2dimension2 = sheet2.getDimensions().get(1);
 	
 	private ScoropDataObjectParser parser = new ScoropDataObjectParser();
 	
 	
 	@Before
 	public void init() {
 		parser.funRegistry = new SpringFunctionRegistry(Arrays.asList(new Add(), new Abs()));
 	}
 	
 	@Test
 	public void parse_shouldRecognizeAreaReference() {
 		Formula parsed = (Formula) parser.parse(sheet1, "=C0R0:C2R2");
 		
 		Expression expression = (Expression) ReflectionUtil.get(parsed, "expression");
 		assertEquals(AreaReference.class, expression.getClass());
 		
 		assertEquals(relObjAddr(sheet1, Path.IN_PLACE, 0, 0), ReflectionUtil.get(expression, "leftUpAddress"));
 		assertEquals(relObjAddr(sheet1, Path.IN_PLACE, 2, 2), ReflectionUtil.get(expression, "rightDownAddress"));
 	}
 }
