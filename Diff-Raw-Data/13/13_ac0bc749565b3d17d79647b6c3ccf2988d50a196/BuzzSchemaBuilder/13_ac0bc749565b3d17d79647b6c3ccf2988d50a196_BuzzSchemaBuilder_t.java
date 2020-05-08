 /** ==== BEGIN LICENSE =====
    Copyright 2012 - BeeQueue.org
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  
  *  ===== END LICENSE ====== */
 package org.beequeue.json;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Method;
 import java.util.List;
 
 import org.beequeue.util.BeeException;
 import org.beequeue.util.ToStringUtil;
 
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.BeanDescription;
 import com.fasterxml.jackson.databind.JavaType;
 import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
 import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
 import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
 
 public class BuzzSchemaBuilder {
 	
 	public BuzzSchema schema = new BuzzSchema();
 	
 	public BuzzAttribute add(Class<?> cls) {
 		return this.recurseSchema(ToStringUtil.MAPPER.getTypeFactory().constructType(cls));
 	}
 
 	public <T> BuzzAttribute add(TypeReference<?> tr) {
 		return this.recurseSchema(ToStringUtil.MAPPER.getTypeFactory().constructType(tr));
 	}
 
 	private BuzzAttribute recurseSchema(JavaType jt) {
 		BuzzAttribute type = new BuzzAttribute();
 		type.type = BuiltInType.detect(jt);
		if(type.type.isPrimitive()){
		    if(type.type == BuiltInType.ENUM ){
 				Class<?> rawClass = jt.getRawClass();
 				type.className = rawClass.getName() ;
 				BuzzClass classDef = schema.types.map().get(type.className);
 				if(classDef==null){
 					classDef = new BuzzClass();
 					classDef.className = type.className ;
 					this.schema.types.add(classDef);
 					buildEnumClassDef(type, classDef, rawClass);
 				}
		    }
	    }else{
			if(type.type == BuiltInType.MAP || type.type == BuiltInType.ARRAY ){
				JavaType contentType = jt.getContentType();
				type.copyTypeIdAsContent(recurseSchema(contentType));
 			}else if(type.type == BuiltInType.OBJECT ){
 				BeanDescription beanDescriptor = ToStringUtil.MAPPER.getSerializationConfig().introspect(jt);
 				AnnotatedMethod valueMethod = beanDescriptor.findJsonValueMethod();
 				if(valueMethod != null){
 					JavaType valueJavaType = valueMethod.getType(beanDescriptor.bindingsForBeanType());
 					type = recurseSchema(valueJavaType);
 				}else{
 					type.className = jt.toCanonical() ;
 					BuzzClass classDef = this.schema.types.map().get(type.className);
 					if(classDef==null){
 						classDef = new BuzzClass();
 						classDef.className = type.className ;
 						this.schema.types.add(classDef);
 						buildObjectClassDef(type, classDef, beanDescriptor);
 					}
 				}
 			}else{
 				throw new BeeException("don't know what to do:"+ToStringUtil.toString(type));
 			}
 		}
 		return type;
 	}
 
 	private void buildObjectClassDef(BuzzAttribute typeId, BuzzClass classDef, BeanDescription bd) {
 		List<BeanPropertyDefinition> properties = bd.findProperties();
 		classDef.objectAttributes = new BuzzAttribute[properties.size()];
 		for (int i = 0; i < classDef.objectAttributes.length; i++) {
 			BuzzAttribute oa = new BuzzAttribute();
 			BeanPropertyDefinition pd = properties.get(i);
 			oa.name = pd.getName();
 			AnnotatedMember accessor = pd.getAccessor();
 			JavaType attributeJavaType = accessor.getType(bd.bindingsForBeanType());
 			oa.copyTypeId(recurseSchema(attributeJavaType));
 			classDef.objectAttributes[i] = oa;
 		}
 	}
 
 	private void buildEnumClassDef(BuzzAttribute typeId, BuzzClass classDef, Class<?> rawClass) {
 		try {
 			Method valuesMethod = rawClass.getMethod("values", new Class[0]);
 			Object arrayOfEnums = valuesMethod.invoke(null, new Object[0]);
 			classDef.enumValues = new String[Array.getLength(arrayOfEnums)];
 			for (int i = 0; i < classDef.enumValues.length; i++) {
 				classDef.enumValues[i] = Array.get(arrayOfEnums,i).toString() ;
 			}
 		} catch (Exception e) {
 			throw new BeeException("extracting values out of enum:",e)
 			.memo("typeId",typeId);
 		}
 	}
 
 }
