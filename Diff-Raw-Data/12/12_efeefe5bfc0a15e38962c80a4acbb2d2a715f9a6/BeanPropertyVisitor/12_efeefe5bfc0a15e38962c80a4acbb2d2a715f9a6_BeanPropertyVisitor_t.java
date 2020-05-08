 /*-
  * Copyright (C) 2011-2012 by Iwao AVE!
  * This program is made available under the terms of the MIT License.
  */
 
 package org.eclipselabs.stlipse.cache;
 
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.IAnnotationBinding;
 import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.Modifier;
 import org.eclipse.jdt.core.dom.PrimitiveType;
 import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
 import org.eclipse.jdt.core.dom.Type;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 import org.eclipselabs.stlipse.Activator;
 
 /**
  * @author Iwao AVE!
  */
 public class BeanPropertyVisitor extends ASTVisitor
 {
 	private IJavaProject project;
 
 	private final Map<String, String> readableFields;
 
 	private final Map<String, String> writableFields;
 
 	private final Map<String, EventProperty> eventHandlers;
 
 	public BeanPropertyVisitor(
 		IJavaProject project,
 		Map<String, String> readableFields,
 		Map<String, String> writableFields,
 		Map<String, EventProperty> eventHandlers)
 	{
 		super();
 		this.project = project;
 		this.readableFields = readableFields;
 		this.writableFields = writableFields;
 		this.eventHandlers = eventHandlers;
 	}
 
 	@Override
 	public boolean visit(FieldDeclaration node)
 	{
 		int modifiers = node.getModifiers();
 		if (Modifier.isPublic(modifiers))
 		{
 			@SuppressWarnings("unchecked")
 			List<VariableDeclarationFragment> fragments = node.fragments();
 			for (VariableDeclarationFragment fragment : fragments)
 			{
 				String fieldName = fragment.getName().toString();
 				String qualifiedName = getQualifiedNameFromType(node.getType());
 				if (qualifiedName == null)
 					; // ignore
 				else if (Modifier.isFinal(modifiers))
 					readableFields.put(fieldName, qualifiedName);
 				else
 					writableFields.put(fieldName, qualifiedName);
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean visit(MethodDeclaration node)
 	{
		// Resolve binding first to support Lombok generated methods.
		// node.getModifiers() returns incorrect access modifiers for them.
		// https://github.com/harawata/stlipse/issues/2
		IMethodBinding method = node.resolveBinding();
		if (Modifier.isPublic(method.getModifiers()))
 		{
 			final String methodName = node.getName().toString();
 			final int parameterCount = node.parameters().size();
 			final Type returnType = node.getReturnType2();
 			if (returnType == null)
 			{
 				// Ignore constructor
 			}
 			else if (isReturnVoid(returnType))
 			{
 				if (isSetter(methodName, parameterCount))
 				{
 					SingleVariableDeclaration param = (SingleVariableDeclaration)node.parameters().get(0);
 					String qualifiedName = getQualifiedNameFromType(param.getType());
 					String fieldName = getFieldNameFromAccessor(methodName);
 					writableFields.put(fieldName, qualifiedName);
 				}
 			}
 			else
 			{
 				if (isGetter(methodName, parameterCount))
 				{
 					String fieldName = getFieldNameFromAccessor(methodName);
 					String qualifiedName = getQualifiedNameFromType(returnType);
 					readableFields.put(fieldName, qualifiedName);
 				}
 				else if (isEventHandler(returnType.toString(), parameterCount))
 				{
 					ITypeBinding binding = returnType.resolveBinding();
 					if (binding == null)
 					{
 						Activator.log(Status.INFO,
 							"Couldn't resolve binding for return type " + returnType.toString()
 								+ " of method " + methodName);
 					}
 					else
 					{
 						String qualifiedName = binding.getQualifiedName();
 						try
 						{
 							if (TypeCache.isResolution(project, project.findType(qualifiedName)))
 							{
 								Object annotationValue = null;
 								boolean isDefaultHandler = false;
 								boolean isInterceptor = false;
								for (IAnnotationBinding annotation : method.getAnnotations())
 								{
 									String name = annotation.getName();
 
 									isInterceptor |= ("Before".equals(name) || "After".equals(name));
 									if (isInterceptor)
 										break;
 
 									isDefaultHandler |= "DefaultHandler".equals(name);
 									if ("HandlesEvent".equals(name))
 									{
 										IMemberValuePairBinding[] valuePairs = annotation.getAllMemberValuePairs();
 										for (IMemberValuePairBinding valuePair : valuePairs)
 										{
 											if ("value".equals(valuePair.getName()))
 											{
 												annotationValue = valuePair.getValue();
 											}
 										}
 									}
 								}
 								if (!isInterceptor)
 								{
 									EventProperty eventProperty = new EventProperty();
 									eventProperty.setDefaultHandler(isDefaultHandler);
 									eventProperty.setMethodName(methodName);
 									String eventName = (String)(annotationValue == null ? methodName
 										: annotationValue);
 									eventHandlers.put(eventName, eventProperty);
 								}
 							}
 						}
 						catch (JavaModelException e)
 						{
 							Activator.log(Status.WARNING, "Error occurred during event handler check", e);
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	private String getQualifiedNameFromType(Type type)
 	{
 		String qualifiedName = null;
 		ITypeBinding binding = type.resolveBinding();
 		if (binding != null)
 		{
 			if (binding.isParameterizedType())
 			{
 				ITypeBinding[] arguments = binding.getTypeArguments();
 				// length = 1 -> List, length > 1 -> Map
 				qualifiedName = arguments[arguments.length > 1 ? 1 : 0].getQualifiedName();
 			}
 			else
 			{
 				qualifiedName = binding.getQualifiedName();
 			}
 		}
 		return qualifiedName;
 	}
 
 	private boolean isEventHandler(String returnType, int parameterCount)
 	{
 		// Just a quick check here.
 		return returnType.endsWith("Resolution") && parameterCount == 0;
 	}
 
 	private boolean isGetter(String methodName, int parameterCount)
 	{
 		return (methodName.startsWith("get") && methodName.length() > 3)
 			|| (methodName.startsWith("is") && methodName.length() > 2) && parameterCount == 0;
 	}
 
 	private boolean isSetter(String methodName, int parameterCount)
 	{
 		return methodName.startsWith("set") && methodName.length() > 3 && parameterCount == 1;
 	}
 
 	private boolean isReturnVoid(Type type)
 	{
 		return type.isPrimitiveType()
 			&& PrimitiveType.VOID.equals(((PrimitiveType)type).getPrimitiveTypeCode());
 	}
 
 	@Override
 	public void endVisit(TypeDeclaration node)
 	{
 		try
 		{
 			Type superclassType = node.getSuperclassType();
 			if (superclassType != null)
 			{
 				ITypeBinding binding = superclassType.resolveBinding();
 				IType type = project.findType(binding.getQualifiedName());
 				ICompilationUnit unit = (ICompilationUnit)type.getAncestor(IJavaElement.COMPILATION_UNIT);
 				if (unit != null && unit.isStructureKnown())
 				{
 					ASTParser parser = ASTParser.newParser(AST.JLS4);
 					parser.setKind(ASTParser.K_COMPILATION_UNIT);
 					parser.setSource(unit);
 					parser.setResolveBindings(true);
 					CompilationUnit astUnit = (CompilationUnit)parser.createAST(null);
 					astUnit.accept(new BeanPropertyVisitor(project, readableFields, writableFields,
 						eventHandlers));
 				}
 			}
 		}
 		catch (JavaModelException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public static String getFieldNameFromAccessor(String methodName)
 	{
 		if (methodName == null)
 			return "";
 		StringBuilder sb = new StringBuilder();
 		if (methodName.startsWith("set") || methodName.startsWith("get"))
 		{
 			sb.append(Character.toLowerCase(methodName.charAt(3)));
 			if (methodName.length() > 4)
 				sb.append(methodName.substring(4));
 		}
 		else if (methodName.startsWith("is"))
 		{
 			sb.append(Character.toLowerCase(methodName.charAt(2)));
 			if (methodName.length() > 3)
 				sb.append(methodName.substring(3));
 		}
 		else
 		{
 			// No such accessor.
 		}
 		return sb.toString();
 	}
 }
