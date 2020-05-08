 package fr.upmc.aladyn.dyn_generics.tests;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 
 import fr.upmc.aladyn.dyn_generics.annotations.DynamicGenericType;
 import fr.upmc.aladyn.dyn_generics.annotations.DynamicGenericTypeParameters;
 import fr.upmc.aladyn.dyn_generics.exceptions.LatentTypeCheckException;
 
 public class Generics2 {
	public static void checkTypesParams(Class<?> classinfo, Class<?>[] types, Object[] args) throws LatentTypeCheckException
 	{
 		try {
 				String methodName = Thread.currentThread().getStackTrace()[4].getMethodName();
 				System.out.println("[checkTypesParams]methodName: "+methodName);
 				Method methlist[] = classinfo.getMethods();
 				int num = 0, num2 = 0;
 				while ((!methlist[num].getName().contains(methodName)) || methodName.equals(methlist[num].getName())){
 					num++;
 				}
 
 				while ((methodName.equals(methlist[num].getName()))){
 					num2++;
 				}
 				System.out.println("[checkTypesParams]methodfound: "+methlist[num].getName());
 				String[] typeParams = classinfo.getAnnotation(DynamicGenericTypeParameters.class).typeParams();
 					
 				Annotation[][] listannot = methlist[num].getParameterAnnotations();
 				
 				if(listannot.length > 0)
 				{
 					for (int j=0; j < listannot.length; j++)
 					{
 						for (int i = 0; i < typeParams.length; i++) {
 							if ( ((DynamicGenericType)listannot[j][0]).value().equals(typeParams[i]) )
 							{
								if (!args[i].getClass().equals(types[i]))
 								{
									throw new LatentTypeCheckException("["+methodName+"(parameter "+(i+1)+")]bad type "+args[i].getClass().getSimpleName()+", waiting "+types[i].getSimpleName());
 								}
 							}
 						}
 					}
 				}
 			} catch (LatentTypeCheckException e){
 				e.printStackTrace();
 				System.exit(0);
 			}
 	}
 	
 	public static void checkTypeReturn(Class<?> classinfo, Class<?>[] types) throws LatentTypeCheckException
 	{
 		String methodName = Thread.currentThread().getStackTrace()[4].getMethodName();
 		System.out.println("[checkTypeReturn]methodName: "+methodName);
 		Method methlist[] = classinfo.getMethods();
 		int num = 0;
 		while ((!methlist[num].getName().contains(methodName)) || methodName.equals(methlist[num].getName())){
 			num++;
 		}
 	
 		System.out.println("[checkTypeReturn]methodNameFound: "+methlist[num].getName());
 		String type;
 		String[] typeParams = classinfo.getAnnotation(DynamicGenericTypeParameters.class).typeParams();
 		Class<?> returntype = methlist[num].getReturnType();
 		if (!methlist[num].getGenericReturnType().toString().equals("void"))
 		{
 			System.out.println("[checkTypeReturn]methodAnnot: "+methlist[num].getAnnotation(DynamicGenericType.class));
 			type = methlist[num].getAnnotation(DynamicGenericType.class).value();
 			for (int i = 0; i < typeParams.length; i++) {
 				if ( type.equals(typeParams[i]) )
 				{
 
 					if (!(returntype.equals(types[i])))
 					{
 						throw new LatentTypeCheckException("["+methodName+"]bad returntype "+returntype+", waiting "+types[i].getSimpleName());
 					}
 				}
 			}
 		}
 	}
 	
 	public static void checkTypeField(Class<?> classinfo, String name, Class<?>[] types)
 	{
 		String[] typeParams = classinfo.getAnnotation(DynamicGenericTypeParameters.class).typeParams();
 		Field[] fields = classinfo.getFields();
 		
 		boolean found = false;
 		int num = 0;
 		System.out.println("[checkTypeField]FieldName: "+name);
 		for(num=0;num<fields.length;num++)
 		{
 			if (fields[num].getName().equals(name))
 			{
 				found = true;
 				break;
 			}
 		}
 		
 		if (!found)
 			return;
 		
 		String annotation = fields[num].getAnnotation(DynamicGenericType.class).value();
 		for (int i = 0; i < typeParams.length; i++) {
 			if ( annotation.equals(typeParams[i]) )
 			{
 
 				if (!(fields[num].getType().equals(types[i])))
 				{
 					throw new LatentTypeCheckException("bad type for field "+fields[num].getName()+", waiting "+types[i].getSimpleName());
 				}
 			}
 		}
 		
 	
 	}
 }
