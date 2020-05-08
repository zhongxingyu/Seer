 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.Map;
 
 import syntaxtree.*;
 
 public class J2V {
 	
 	public static void main(String [] args) 
 	{
 		try {
			FileInputStream fileInput = new FileInputStream("test/TreeVisitor.java");
 			System.setIn(fileInput);
 			Node root = new MiniJavaParser(System.in).Goal();
 
 			PopulateClassesVisitor firstPass = new PopulateClassesVisitor();
 			Map<String, MJClass> noIndicesOrParentsClassMap = (Map<String, MJClass>) root.accept(firstPass, null);
 			Map<String, MJClass> noIndicesClassMap = linkParentClasses(noIndicesOrParentsClassMap);
 			Map<String, MJClass> classMap = generateMethodAndFieldIndices(noIndicesClassMap);
 			
 			TranslateToVaporVisitor secondPass = new TranslateToVaporVisitor(classMap);
 			String code = (String) root.accept(secondPass, null);
 			
 			System.out.print(code);
 		}
 		catch (ParseException e) 
 		{
 			System.out.println(e.toString());
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private static Map<String, MJClass> linkParentClasses(Map<String, MJClass> oldMap) 
 	{
 		Map<String, MJClass> newMap = new HashMap<String, MJClass>();
 		for (MJClass mjclass : oldMap.values())
 		{
 			linkParentClass(mjclass, oldMap, newMap);
 		}
 		return newMap;
 	}
 
 	private static void linkParentClass(MJClass mjclass, Map<String, MJClass> oldMap, Map<String, MJClass> newMap) 
 	{
 		if (mjclass.parentClass == null)
 		{
 			newMap.put(mjclass.name, mjclass);
 			return;
 		}
 		
 		if (newMap.containsKey(mjclass.parentClass.name))
 			linkParentClass(oldMap.get(mjclass.parentClass.name), oldMap, newMap);
 		
 		MJClass newClass = new MJClass(mjclass.methods, mjclass.fields, mjclass.name, newMap.get(mjclass.parentClass.name));
 		newMap.put(newClass.name, newClass);
 	}
 
 	private static Map<String, MJClass> generateMethodAndFieldIndices(Map<String, MJClass> oldMap) 
 	{
 		Map<String, MJClass> newMap = new HashMap<String, MJClass>();
 		for (MJClass mjclass : oldMap.values())
 		{
 			generateIndices(mjclass, oldMap, newMap);
 		}
 		return newMap;
 	}
 
 	private static void generateIndices(MJClass mjclass, Map<String, MJClass> oldMap, Map<String, MJClass> newMap) 
 	{
 		// If the class already has generated indices, skip
 		if (newMap.containsKey(mjclass.name))
 			return;
 		
 		// If the parent's class does not have generated indices yet, do the parent first
 		if (mjclass.parentClass != null && !newMap.containsKey(mjclass.parentClass))
 			generateIndices(oldMap.get(mjclass.parentClass.name), oldMap, newMap);
 		
 		// Everything is set, generate indices for this MJClass
 		Map<String, MJMethod> newMethodMap = new HashMap<String, MJMethod>();
 		Map<String, MJField> newFieldMap = new HashMap<String, MJField>();
 		MJClass newClass = new MJClass(newMethodMap, newFieldMap, mjclass.name, mjclass.parentClass);
 		
 		int methodIndex = 0;
 		if (mjclass.parentClass != null)
 		{
 			int numParentMethods = oldMap.get(mjclass.parentClass.name).getMethods().size();
 			methodIndex += numParentMethods;
 		}
 		
 		for (MJMethod mjmethod : mjclass.methods.values())
 		{
 			// If this method is overwriting parent's, then use parent's index
 			if (mjclass.parentClass != null && mjclass.parentClass.methods.containsKey(mjmethod.name))
 			{
 				int parentIndex = newMap.get(mjclass.parentClass.name).methods.get(mjmethod.name).methodTableIndex;
 				MJMethod newMethod = new MJMethod(mjmethod.name, mjmethod.parameters, mjmethod.returnType, parentIndex);
 				newClass.methods.put(newMethod.name, newMethod);
 			}
 			else
 			{
 				MJMethod newMethod = new MJMethod(mjmethod.name, mjmethod.parameters, mjmethod.returnType, methodIndex);
 				methodIndex++;
 				newClass.methods.put(newMethod.name, newMethod);
 			}
 		}
 		
 		int fieldIndex = 1;
 		if (mjclass.parentClass != null)
 		{
 			int numParentFields = oldMap.get(mjclass.parentClass.name).getFields().size();
 			fieldIndex += numParentFields;
 		}
 		
 		for (MJField mjfield : mjclass.fields.values())
 		{
 			if (mjclass.parentClass != null && mjclass.parentClass.fields.containsKey(mjfield.name))
 			{
 				int parentIndex = newMap.get(mjclass.parentClass.name).fields.get(mjfield.name).index;
 				MJField newField = new MJField(mjfield.type, mjfield.name, parentIndex);
 				newClass.fields.put(newField.name, newField);
 			}
 			else
 			{
 				MJField newField = new MJField(mjfield.type, mjfield.name, fieldIndex);
 				fieldIndex++;
 				newClass.fields.put(newField.name, newField);
 			}
 		}
 		
 		newMap.put(newClass.name, newClass);
 	}
 }
