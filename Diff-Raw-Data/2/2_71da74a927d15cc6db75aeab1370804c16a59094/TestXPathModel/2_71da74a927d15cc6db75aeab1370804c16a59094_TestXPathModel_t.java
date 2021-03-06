 package org.eclipse.wst.xsl.internal.core.xpath.tests;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.wst.xml.xpath.core.internal.model.XPath.XPath;
 import org.eclipse.wst.xml.xpath.core.internal.model.XPath.Axis.Axis;
 import org.eclipse.wst.xml.xpath.core.internal.model.XPath.Axis.impl.AxisFactoryImpl;
 import org.eclipse.wst.xml.xpath.core.internal.model.XPath.DataTypes.DataTypes;
 import org.eclipse.wst.xml.xpath.core.internal.model.XPath.DataTypes.Variable;
 import org.eclipse.wst.xml.xpath.core.internal.model.XPath.DataTypes.impl.DataTypesFactoryImpl;
 import org.eclipse.wst.xml.xpath.core.internal.model.XPath.Function.Argument;
 import org.eclipse.wst.xml.xpath.core.internal.model.XPath.Function.Function;
 import org.eclipse.wst.xml.xpath.core.internal.model.XPath.Function.FunctionFactory;
 import org.eclipse.wst.xml.xpath.core.internal.model.XPath.Function.Occurrence;
 import org.eclipse.wst.xml.xpath.core.internal.model.XPath.Function.impl.FunctionFactoryImpl;
 import org.eclipse.wst.xml.xpath.core.internal.model.XPath.impl.xpathFactoryImpl;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 public class TestXPathModel extends TestCase {
 
 	public void testCreateXPathRoot() throws Exception {
 		XPath xpath = new xpathFactoryImpl().createXPath();
 		assertNotNull("Failed to create xpath class", xpath);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void testLoadXPathModel() throws Exception {
 		EList components = new BasicEList();
 		XPath xpath = new xpathFactoryImpl().createXPath();
 		Variable variable = new DataTypesFactoryImpl().createVariable();
 		variable.setName("test");
 		variable.setBeginColumn(1);
 		variable.setEndColumn(4);
 		variable.setBeginLineNumber(1);
 		variable.setEndLineNumber(1);
 		components.add(variable);
 		xpath.setComponents(components);
 		assertNotNull("XPath Components array not set.", xpath.getComponentsList());
 		EList xpathComp = xpath.getComponentsList();
 		assertEquals("Number of components not equal to 1.", 1, xpathComp.size());
 		if (!(xpathComp.get(0) instanceof Variable)) {
 			Assert.fail("Component that was not a variable.");
 		}
 		variable = (Variable)xpathComp.get(0);
 		assertEquals("Name does not equal test", "test", variable.getName());
 		assertEquals("Beginning Column Number not 1", 1, variable.getBeginColumn());
 		assertEquals("Ending Column Number not 4", 4, variable.getEndColumn());
		assertEquals("Begining Line Number incorrect.", 1, variable.getBeginLineNumber());
 		assertEquals("Ending Line Number incorrect.", 1, variable.getEndLineNumber());
 	}
 	
 	public void testFunctionNoArgs() {
 		Function function = new FunctionFactoryImpl().createFunction();
 		function.setName("true");
 		function.setReturns(DataTypes.BOOLEAN);
 		assertEquals("Arguments greater than zero.", 0, function.getArguments().length);
 	}
 
 	public void testFunctionWithStringArg() {
 		FunctionFactory funcFactory = new FunctionFactoryImpl();
 		Function function = funcFactory.createFunction();
 		function.setName("lang");
 		function.setReturns(DataTypes.BOOLEAN);
 		
 		Argument arg = funcFactory.createArgument();
 		arg.setDataType(DataTypes.STRING.getName());
 		arg.setRequired(Occurrence.YES);
 		
 		Argument[] args = { arg };
 		function.setArguments(args);
 		
 		assertEquals("Arguments not equal to 1", 1, function.getArguments().length);
 		arg = function.getArguments(0);
 		assertEquals("String argument not found.", DataTypes.STRING.getName(), arg.getDataType()); 
 	}
 	
 	public void testFunctionWithTwoRequiredOneOptional() {
 		FunctionFactory funcFactory = new FunctionFactoryImpl();
 		Function function = funcFactory.createFunction();
 		function.setName("concat");
 		function.setReturns(DataTypes.STRING);
 		
 		Argument arg = funcFactory.createArgument();
 		arg.setDataType(DataTypes.STRING.getName());
 		arg.setRequired(Occurrence.YES);
 		
 		Argument arg2 = funcFactory.createArgument();
 		arg2.setDataType(DataTypes.STRING.getName());
 		arg2.setRequired(Occurrence.YES);
 		
 		Argument arg3 = funcFactory.createArgument();
 		arg3.setDataType(DataTypes.STRING.getName());
 		arg3.setRequired(Occurrence.NO);
 		
 		
 		Argument[] args = { arg, arg2, arg3 };
 		function.setArguments(args);
 		
 		assertEquals("Arguments not equal to 3", 3, function.getArguments().length);
 		arg = function.getArguments(0);
 		assertEquals("Arg1 String argument not found.", DataTypes.STRING.getName(), arg.getDataType());
 		assertEquals("Should be required.", Occurrence.YES, arg.getRequired());
 		
 		arg = function.getArguments(1);
 		assertEquals("Arg2 String argument not found.", DataTypes.STRING.getName(), arg.getDataType());
 		assertEquals("Should be required.", Occurrence.YES, arg.getRequired());
 		
 		arg = function.getArguments(2);
 		assertEquals("Arg3 String argument not found.", DataTypes.STRING.getName(), arg.getDataType());
 		assertEquals("Should be optional.", Occurrence.NO, arg.getRequired());
 	}
 
 	public void testCreateAXIS() throws Exception {
 		Axis axis = new AxisFactoryImpl().createAxis();
 		axis.setName("following-sibling");
 		assertEquals("following-sibling", axis.getName());
 	}
 	
 }
