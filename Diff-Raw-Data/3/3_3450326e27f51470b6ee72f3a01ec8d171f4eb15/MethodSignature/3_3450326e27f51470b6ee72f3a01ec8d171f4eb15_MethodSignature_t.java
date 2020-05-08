 package watson.glen.pseudocode.constructs;
 
 import java.util.LinkedList;
 import java.util.List;
 
 public class MethodSignature
 {
 	private AccessModifier modifier;
 	private boolean isStatic;
 	private String returnType;
 	private String methodName;
 	private List<VariableDeclaration> parameters;
 	public MethodSignature(AccessModifier modifier, boolean isStatic, String returnType, String methodName, List<VariableDeclaration> parameters)
 	{
 		super();
 		this.modifier = modifier;
 		this.isStatic = isStatic;
 		this.returnType = returnType;
 		this.methodName = methodName;
 		this.parameters = parameters;
 	}
 	public MethodSignature(AccessModifier modifier, boolean isStatic, String returnType, String methodName)
 	{
 		super();
 		this.modifier = modifier;
 		this.isStatic = isStatic;
 		this.returnType = returnType;
 		this.methodName = methodName;
 		this.parameters = new LinkedList<>();
 	}
 	public AccessModifier getModifier()
 	{
 		return modifier;
 	}
 	public void setModifier(AccessModifier modifier)
 	{
 		this.modifier = modifier;
 	}
 	public boolean isStatic()
 	{
 		return isStatic;
 	}
 	public void setStatic(boolean isStatic)
 	{
 		this.isStatic = isStatic;
 	}
 	public String getReturnType()
 	{
 		return returnType;
 	}
 	public void setReturnType(String returnType)
 	{
 		this.returnType = returnType;
 	}
 	public String getMethodName()
 	{
 		return methodName;
 	}
 	public void setMethodName(String methodName)
 	{
 		this.methodName = methodName;
 	}
 	public List<VariableDeclaration> getParameters()
 	{
 		return parameters;
 	}
 	public void setParameters(List<VariableDeclaration> parameters)
 	{
 		this.parameters = parameters;
 	}
 	
 	@Override
 	public String toString()
 	{
 		StringBuilder sb = new StringBuilder();
 		sb.append("\t");
 		sb.append(modifier);
 		if(isStatic)
 		{
 			sb.append("static ");
 		}
 		sb.append(returnType);
 		sb.append(" ");
 		sb.append(methodName);
 		sb.append("(");
		int beforeLength = sb.length();
 		sb.append(parameters);
		sb.delete(beforeLength, beforeLength+1);
 		sb.delete(sb.length()-2, sb.length());
 		sb.append(")");
 		return sb.toString();
 	}
 }
