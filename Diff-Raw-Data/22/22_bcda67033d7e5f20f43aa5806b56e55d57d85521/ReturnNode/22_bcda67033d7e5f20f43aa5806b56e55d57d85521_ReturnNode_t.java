 package smartest;
 
 public class ReturnNode extends ASTNode{
 
 	private String returnType;
 	
 	ReturnNode(String returnType, ASTNode returnExpression, int yyline, int yycolumn)
 	{
 		super(yyline,yycolumn);
 		this.returnType = returnType;
 		this.addChild(returnExpression);
 	}
 	
 	public void checkSemantics() throws Exception
 	{
 			for (ASTNode child : this.getChildren())
 			{
 				if (child != null)
 					child.checkSemantics();
 			}
                         if (returnType.equalsIgnoreCase("void") && this.getChildAt(0) != null )
                         {
                                 throw new Exception("Cannot return a value in a void function");
                         }
 
                         String returnExpressionType;
                         if (this.getChildAt(0) == null)
                                 returnExpressionType = "void";
                         else
                                 returnExpressionType = this.getChildAt(0).getType();
 
 
 			if (!returnType.equalsIgnoreCase(returnExpressionType))
 			{
                                 if(!("double".equalsIgnoreCase(returnType) && "int".equalsIgnoreCase(returnExpressionType)))
 				        throw new Exception("Expecting return type " + returnType + " not " + returnExpressionType);
 			}
                         this.setType("return");
 	}
 	public StringBuffer generateCode() 
 	{
 		StringBuffer output = new StringBuffer();
                 if (this.getChildAt(0) != null)
                 {
                         output.append(this.getChildAt(0).getType());
                         output.append(" temp_var = ");
                         output.append(initialValueForType(this.getChildAt(0).getType()));
                         output.append(";\ntry {\ntemp_var = ");
                         output.append(this.getChildAt(0).generateCode());
                        output.append(";\n} catch (Expression e) {\n System.out.println(\"SmarTest: Runtime error at line: " + getYyline() + " \" \" + e.getMessage() + \"); System.exit(0);}");
                 }
 		output.append("return ");
 		if (this.getChildAt(0) != null)
			output.append("temp_var");
 		return output;
 	}
 	public String getReturnType()
 	{
 		return returnType; 
 	}
 	public void setReturnType(String returnType)
 	{
 		this.returnType = returnType;
 	}
        private String initialValueForType(String type)
        {
                if ("string".equalsIgnoreCase(type))
                        return "\"\"";
                if ("double".equalsIgnoreCase(type))
                        return "" + 0.0;
                if ("int".equalsIgnoreCase(type))
                        return "" + 0;
                if ("boolean".equalsIgnoreCase(type))
                        return "false";
                if ("question".equalsIgnoreCase(type))
                        return "null";
                if ("set".equalsIgnoreCase(type))
                        return "null";
                return "(" + type + ")" + 0;
        }
 	
 }
