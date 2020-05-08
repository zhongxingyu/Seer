 package ccalculator;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.regex.*;
 
 public class Function extends Value
 {
     protected boolean minus = false;
     private ArrayList<Block> args = new ArrayList<Block>();
     private java.lang.reflect.Method method;
     
     public Function(String str) throws ParseException
     {
     	this(str, true);
     }
     
     public Function(String str, boolean sign) throws ParseException
     {
         Matcher matcher = Pattern.compile("\\s*([A-Za-z]+)\\(").matcher(str);
         if (!matcher.lookingAt())
         {
             throw new ParseException("function", offset);
         }
         Class<?> functionClass;
         String functionName = matcher.group(1).toLowerCase();
         try
         {
         	String functionNameCapitalized = functionName.substring(0, 1).toUpperCase() + functionName.substring(1);
         	functionClass = Class.forName("ccalculator.functions." + functionNameCapitalized);
         }
         catch (ClassNotFoundException e)
         {
         	throw new ParseException("Unknown function: " + functionName, offset);
         }
         length = matcher.end();
         offset += matcher.end();
         args.add(findBlock(str.substring(length)));
         do
         {
             if (findComma(str.substring(length)))
             {
             	args.add(findBlock(str.substring(length)));
             }
             else
             {
             	findClosingBracket(str.substring(length));
             	break;
             }
         }
         while (true);
         
     	ArrayList<Class<?>> argsTypes = new ArrayList<Class<?>>();
     	for (int i = 0; i < args.size(); i++)
     	{
     		argsTypes.add(double.class);
     	}
     	Class<?>[] argsTypesArray = argsTypes.toArray(new Class<?>[0]);
     	try
         {
             method = functionClass.getMethod(functionName, argsTypesArray);
         }
     	catch (NoSuchMethodException e)
     	{
    		throw new ParseException("Wrong number of arguments for function " + functionName, offset);
     	}
     }
     
     public double value()
     {
     	ArrayList<Object> argsValues = new ArrayList<Object>();
     	for (Block arg: args)
     	{
     		argsValues.add(arg.value());
     	}
     	try
         {
             return (Double) method.invoke(null, argsValues.toArray());
         } catch (IllegalArgumentException e) {
 		} catch (IllegalAccessException e) {
 		} catch (java.lang.reflect.InvocationTargetException e) {
 		}
         return 0;
     }
 }
