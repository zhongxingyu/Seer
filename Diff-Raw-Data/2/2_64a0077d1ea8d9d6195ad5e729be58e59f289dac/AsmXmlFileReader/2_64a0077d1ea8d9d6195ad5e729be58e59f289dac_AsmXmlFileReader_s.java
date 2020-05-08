 /*
  * Copyright 2010-2012 napile.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.napile.asm.io.xml.in;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.io.SAXReader;
 import org.jetbrains.annotations.NotNull;
 import org.napile.asm.Modifier;
 import org.napile.asm.resolve.name.FqName;
 import org.napile.asm.tree.members.AbstractMemberNode;
 import org.napile.asm.tree.members.ClassNode;
 import org.napile.asm.tree.members.ConstructorNode;
 import org.napile.asm.tree.members.LikeMethodNode;
 import org.napile.asm.tree.members.MethodNode;
 import org.napile.asm.tree.members.MethodParameterNode;
 import org.napile.asm.tree.members.StaticConstructorNode;
 import org.napile.asm.tree.members.TypeParameterNode;
 import org.napile.asm.tree.members.VariableNode;
 import org.napile.asm.tree.members.bytecode.Instruction;
 import org.napile.asm.tree.members.bytecode.MethodRef;
 import org.napile.asm.tree.members.bytecode.VariableRef;
 import org.napile.asm.tree.members.bytecode.impl.*;
 import org.napile.asm.tree.members.types.TypeNode;
 import org.napile.asm.tree.members.types.constructors.ClassTypeNode;
 import org.napile.asm.tree.members.types.constructors.ThisTypeNode;
 import org.napile.asm.tree.members.types.constructors.TypeConstructorNode;
 import org.napile.asm.tree.members.types.constructors.TypeParameterValueTypeNode;
 
 /**
  * @author VISTALL
  * @date 0:13/21.09.12
  */
 public class AsmXmlFileReader
 {
 	private final SAXReader reader = new SAXReader(false);
 
 	public ClassNode read(@NotNull Reader r) throws IOException
 	{
 		try
 		{
 			return read(reader.read(r));
 		}
 		catch(DocumentException e)
 		{
 			throw new IOException(e);
 		}
 	}
 
 	public ClassNode read(@NotNull InputStream stream) throws IOException
 	{
 		try
 		{
 			return read(reader.read(stream));
 		}
 		catch(DocumentException e)
 		{
 			throw new IOException(e);
 		}
 	}
 
 	private ClassNode read(Document document)
 	{
 		Element classElement = throwIfNotExpected(document.getRootElement(), "class");
 
 		FqName fqName = new FqName(classElement.attributeValue("name"));
 		Modifier[] modifiers = readModifiers(classElement);
 		ClassNode classNode = new ClassNode(modifiers, fqName);
 
 		readSupers(classElement, classNode.supers);
 
 		readTypeParameters(classElement, classNode);
 
 		for(Element child : classElement.elements())
 		{
 			AbstractMemberNode<?> node = null;
 
 			if("constructor".equals(child.getName()))
 				node = readConstructor(child);
 			else if("static_constructor".equals(child.getName()))
 				node = readStaticConstructor(child);
 			else if("variable".equals(child.getName()))
 				node = readVariable(child);
 			else if("method".equals(child.getName()))
 				node = readMethod(child);
 
 			if(node != null)
 				classNode.members.add(node);
 		}
 
 		return classNode;
 	}
 
 	private AbstractMemberNode<?> readConstructor(@NotNull Element child)
 	{
 		ConstructorNode constructorNode = new ConstructorNode(readModifiers(child));
 
 		readParameters(child, constructorNode.parameters);
 
 		readCode(child, constructorNode);
 
 		return constructorNode;
 	}
 
 	private AbstractMemberNode<?> readStaticConstructor(@NotNull Element child)
 	{
 		StaticConstructorNode staticConstructorNode = new StaticConstructorNode();
 
 		readCode(child, staticConstructorNode);
 
 		return staticConstructorNode;
 	}
 
 	private AbstractMemberNode<?> readVariable(@NotNull Element child)
 	{
 		VariableNode variableNode = new VariableNode(readModifiers(child), child.attributeValue("name"));
 
 		variableNode.returnType = readType(child.element("return_type").element("type"));
 
 		readTypeParameters(child, variableNode);
 
 		return variableNode;
 	}
 
 	private AbstractMemberNode<?> readMethod(@NotNull Element child)
 	{
 		MethodNode methodNode = new MethodNode(readModifiers(child), child.attributeValue("name"));
 
 		Element returnElement = child.element("return_type");
 		if(returnElement != null)
 			methodNode.returnType = readType(returnElement.element("type"));
 
 		readTypeParameters(child, methodNode);
 
 		readParameters(child, methodNode.parameters);
 
 		readCode(child, methodNode);
 
 		return methodNode;
 	}
 
 	private void readParameters(@NotNull Element parent, List<MethodParameterNode> parameters)
 	{
 		Element parametersElement = parent.element("parameters");
 		if(parametersElement != null)
 			for(Element parameterElement : parametersElement.elements())
 				parameters.add(new MethodParameterNode(readModifiers(parameterElement), parameterElement.attributeValue("name"), readType(parameterElement.element("type"))));
 	}
 
 	private void readCode(@NotNull Element parent, @NotNull LikeMethodNode<?> methodNode)
 	{
 		Element codeElement = parent.element("code");
 		if(codeElement != null)
 		{
 			int val = Integer.parseInt(codeElement.attributeValue("max_locals"));
 			//TODO [VISTALL] max stacks
 			methodNode.visitMaxs(val, val);
 
 			for(Element instructionElement : codeElement.elements())
 			{
 				Instruction instruction = null;
 				String instructionName = instructionElement.getName();
 
 				if("new_byte".equals(instructionName))
 					instruction = new NewByteInstruction(Byte.parseByte(instructionElement.attributeValue("val")));
 				else if("new_short".equals(instructionName))
 					instruction = new NewShortInstruction(Short.parseShort(instructionElement.attributeValue("val")));
 				else if("new_int".equals(instructionName))
 					instruction = new NewIntInstruction(Integer.parseInt(instructionElement.attributeValue("val")));
 				else if("new_long".equals(instructionName))
 					instruction = new NewLongInstruction(Long.parseLong(instructionElement.attributeValue("val")));
 				else if("new_char".equals(instructionName))
 					instruction = new NewCharInstruction(instructionElement.attributeValue("val").charAt(0));
 				else if("new_float".equals(instructionName))
 					instruction = new NewFloatInstruction(Float.parseFloat(instructionElement.attributeValue("val")));
 				else if("new_double".equals(instructionName))
 					instruction = new NewDoubleInstruction(Double.parseDouble(instructionElement.attributeValue("val")));
 				else if("new_object".equals(instructionName))
 					instruction = new NewObjectInstruction(readType(instructionElement.element("type"))) ;
 				else if("store".equals(instructionName))
 					instruction = new StoreInstruction(Integer.parseInt(instructionElement.attributeValue("val")));
 				else if("load".equals(instructionName))
 					instruction = new LoadInstruction(Integer.parseInt(instructionElement.attributeValue("val")));
 				else if("dup".equals(instructionName))
 					instruction = new DupInstruction();
 				else if("pop".equals(instructionName))
 					instruction = new PopInstruction();
 				else if("return".equals(instructionName))
 					instruction = new ReturnInstruction();
 				else if("throw".equals(instructionName))
 					instruction = new ThrowInstruction();
 				else if("invoke_static".equals(instructionName))
 					instruction = new InvokeStaticInstruction(readMethodRef(instructionElement));
 				else if("invoke_special".equals(instructionName))
 					instruction = new InvokeSpecialInstruction(readMethodRef(instructionElement));
 				else if("invoke_virtual".equals(instructionName))
 					instruction = new InvokeVirtualInstruction(readMethodRef(instructionElement));
 				else if("put_to_variable".equals(instructionName))
 					instruction = new PutToVariableInstruction(readVariableRef(instructionElement));
 				else if("put_to_static_variable".equals(instructionName))
 					instruction = new PutToStaticVariableInstruction(readVariableRef(instructionElement));
 				else if("get_variable".equals(instructionName))
 					instruction = new GetVariableInstruction(readVariableRef(instructionElement));
 				else if("get_static_variable".equals(instructionName))
 					instruction = new GetStaticVariableInstruction(readVariableRef(instructionElement));
 
 				if(instruction != null)
 					methodNode.instructions.add(instruction);
 				else
 					throw new IllegalArgumentException("Unknown instruction: " + instructionName);
 			}
 		}
 	}
 
 	private VariableRef readVariableRef(@NotNull Element child)
 	{
 		Element element = child.element("variable");
 		FqName fqName = new FqName(element.attributeValue("name"));
 
 		TypeNode typeNode = readType(element.element("type"));
 		return new VariableRef(fqName, typeNode);
 	}
 
 	private MethodRef readMethodRef(@NotNull Element child)
 	{
 		Element element = child.element("method");
 		FqName fqName = new FqName(element.attributeValue("name"));
 		Element returnElement = element.element("return_type");
 
 		TypeNode returnType = null;
 		if(returnElement != null)
 			returnType = readType(returnElement.element("type"));
 
 		List<TypeNode> parameterTypes = new ArrayList<TypeNode>();
 		Element parametersElement = element.element("parameters");
 		if(parametersElement != null)
 			for(Element parameterElement : parametersElement.elements())
 				parameterTypes.add(readType(parameterElement));
 
 		return new MethodRef(fqName, parameterTypes, returnType);
 	}
 
 	private void readSupers(@NotNull Element element, @NotNull List<TypeNode> list)
 	{
 		Element extendsElement = element.element("extends");
 		if(extendsElement == null)
 			return;
 
 		for(Element child : extendsElement.elements())
 			list.add(readType(child));
 	}
 
 	@NotNull
 	private Modifier[] readModifiers(Element element)
 	{
 		Element modifiersElement = element.element("modifiers");
 		if(modifiersElement == null)
 			return Modifier.EMPTY;
 
 		List<Element> children = modifiersElement.elements();
 		List<Modifier> modifierList = new ArrayList<Modifier>(children.size());
 		for(Element child : children)
 			modifierList.add(Modifier.valueOf(child.getName().toUpperCase()));
 
 		return modifierList.toArray(new Modifier[modifierList.size()]);
 	}
 
 	private void readTypeParameters(@NotNull Element element, @NotNull AbstractMemberNode<?> node)
 	{
 		Element typeParametersElement = element.element("type_parameters");
 		if(typeParametersElement == null)
 			return;
 
 		for(Element child : typeParametersElement.elements())
 		{
 			child = throwIfNotExpected(child, "type_parameter");
 
 			TypeParameterNode typeParameterNode = new TypeParameterNode(child.attributeValue("name"));
 
 			readSupers(child, typeParameterNode.supers);
 
 			node.typeParameters.add(typeParameterNode);
 		}
 	}
 
 	private TypeNode readType(@NotNull Element element)
 	{
 		element = throwIfNotExpected(element, "type");
 
 		boolean nullable = Boolean.parseBoolean(element.attributeValue("nullable"));
 		TypeConstructorNode typeConstructorNode = null;
 		Element constructorElement = element.element("class_type");
 		if(constructorElement != null)
 			typeConstructorNode = new ClassTypeNode(new FqName(constructorElement.attributeValue("name")));
 		else if((constructorElement = element.element("this_type")) != null)
 			typeConstructorNode = new ThisTypeNode();
 		else if((constructorElement = element.element("type_parameter_value_type")) != null)
 			typeConstructorNode = new TypeParameterValueTypeNode(constructorElement.attributeValue("name"));
 
 		if(typeConstructorNode == null)
 			throw new IllegalArgumentException("Unknown constructor of type: " + element.elements());
 
 		TypeNode typeNode = new TypeNode(nullable, typeConstructorNode);
 
 		Element argumentsElement = element.element("type_arguments");
 		if(argumentsElement != null)
 			for(Element child : argumentsElement.elements())
 				typeNode.arguments.add(readType(child));
 
 		return typeNode;
 	}
 
 	@NotNull
 	private Element throwIfNotExpected(@NotNull Element element, @NotNull String name)
 	{
 		if(!element.getName().equals(name))
 			throw new IllegalArgumentException("Invalid xml element. Expecting: " + name + ". Found: " + element.getName());
 		return element;
 	}
 }
