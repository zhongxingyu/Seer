 package cd.semantic.ti;
 
import junit.framework.Assert;

 import org.junit.Before;
 import org.junit.Test;
 
 import cd.ir.symbols.ClassSymbol;
 import cd.ir.symbols.MethodSymbol;
 import cd.ir.symbols.TypeSymbol;
 import cd.ir.symbols.VariableSymbol;
 import cd.semantic.TypeSymbolTable;
 
 /**
  * Tests {@link LocalTypeEraser}.
  */
 public class TypeEraserTest {
 
 	private TypeSymbolTable symbolTable;
 	private ClassSymbol classSymbol;
 	private MethodSymbol methodSymbol;
 	private VariableSymbol paramSymbol;
 	private VariableSymbol fieldSymbol;
 	private VariableSymbol localSymbol;
 
 	@Before
 	public void setUp() {
 		symbolTable = new TypeSymbolTable();
 
 		fieldSymbol = new VariableSymbol("field", symbolTable.getObjectType());
 		paramSymbol = new VariableSymbol("param", symbolTable.getIntType());
 		localSymbol = new VariableSymbol("local", symbolTable.getBooleanType());
 
 		methodSymbol = new MethodSymbol("main", classSymbol);
 		methodSymbol.addParameter(paramSymbol);
 		methodSymbol.addLocal(localSymbol);
 		methodSymbol.returnType = symbolTable.getVoidType();
 
 		classSymbol = new ClassSymbol("Main");
 		classSymbol.addMethod(methodSymbol);
 		classSymbol.addField(fieldSymbol);
 
 		symbolTable.add(classSymbol);
 	}
 
 	@Test
 	public void testLocalTypeEraser() {
 		TypeEraser typeEraser = LocalTypeEraser.getInstance();
 		typeEraser.eraseTypesFrom(symbolTable);
 
 		assertIsNotBottomType(methodSymbol.returnType);
 		assertIsNotBottomType(fieldSymbol.getType());
 		assertIsNotBottomType(paramSymbol.getType());
 		assertIsBottomType(localSymbol.getType());
 	}
 
 	@Test
 	public void testGlobalTypeEraser() {
 		TypeEraser typeEraser = GlobalTypeEraser.getInstance();
 		typeEraser.eraseTypesFrom(symbolTable);
 
 		assertIsBottomType(methodSymbol.returnType);
 		assertIsBottomType(fieldSymbol.getType());
 		assertIsBottomType(paramSymbol.getType());
 		assertIsBottomType(localSymbol.getType());
 	}
 
 	private void assertIsBottomType(TypeSymbol actualType) {
 		Assert.assertSame(symbolTable.getBottomType(), actualType);
 	}
 
 	private void assertIsNotBottomType(TypeSymbol actualType) {
 		Assert.assertNotSame(symbolTable.getBottomType(), actualType);
 	}
 
 }
