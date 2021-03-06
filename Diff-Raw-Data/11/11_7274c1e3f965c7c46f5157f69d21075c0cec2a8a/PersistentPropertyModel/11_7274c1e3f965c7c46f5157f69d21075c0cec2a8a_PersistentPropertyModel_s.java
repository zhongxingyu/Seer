 /*
  * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package org.qi4j.runtime.property;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.qi4j.api.common.MetaInfo;
 import org.qi4j.api.entity.Queryable;
 import org.qi4j.api.property.GenericPropertyInfo;
 import org.qi4j.api.property.Property;
 import org.qi4j.api.property.PropertyInfo;
 import org.qi4j.api.util.Classes;
 import org.qi4j.api.value.ValueComposite;
 import org.qi4j.runtime.composite.ValueConstraintsInstance;
 import org.qi4j.runtime.structure.ModuleInstance;
 import org.qi4j.runtime.value.ValueInstance;
 import org.qi4j.runtime.value.ValueModel;
 import org.qi4j.spi.entity.EntityState;
 import org.qi4j.spi.property.PropertyDescriptor;
 import org.qi4j.spi.property.PropertyType;
 import org.qi4j.spi.property.PropertyTypeDescriptor;
 import org.qi4j.spi.value.CollectionType;
 import org.qi4j.spi.value.CompoundType;
 import org.qi4j.spi.value.SerializableType;
 import org.qi4j.spi.value.ValueState;
 import org.qi4j.spi.value.ValueType;
 
 /**
  * JAVADOC
  */
 public abstract class PersistentPropertyModel
     extends AbstractPropertyModel
     implements PropertyTypeDescriptor
 {
     private final boolean queryable;
     private final PropertyType propertyType;
     protected final PropertyInfo propertyInfo;
 
     public PersistentPropertyModel( Method accessor, boolean immutable, ValueConstraintsInstance constraints, MetaInfo metaInfo, Object initialValue )
     {
         super( accessor, immutable, constraints, metaInfo, initialValue );
 
         final Queryable queryable = accessor.getAnnotation( Queryable.class );
         this.queryable = queryable == null || queryable.value();
 
         PropertyType.PropertyTypeEnum type;
         if( isComputed() )
         {
             type = PropertyType.PropertyTypeEnum.COMPUTED;
         }
         else if( isImmutable() )
         {
             type = PropertyType.PropertyTypeEnum.IMMUTABLE;
         }
         else
         {
             type = PropertyType.PropertyTypeEnum.MUTABLE;
         }
 
        Type valueType = Classes.getRawClass( type() );

        propertyType = new PropertyType( qualifiedName(), createValueType( valueType ), toURI(), toRDF(), this.queryable, type );
 
         propertyInfo = new GenericPropertyInfo( metaInfo, isImmutable(), isComputed(), name(), qualifiedName(), type() );
     }
 
     public PropertyType propertyType()
     {
         return propertyType;
     }
 
     public boolean isQueryable()
     {
         return queryable;
     }
 
 
     public Object toValue( Object value, EntityState entityState )
     {
         if (value == null)
             return null;
 
         ValueType valueType = propertyType().type();
         return toValue(valueType, value, entityState );
     }
 
     public Object toValue( ValueType valueType, Object value, EntityState entityState )
     {
         if (value == null)
             return null;
 
         if( valueType instanceof CompoundType )
         {
             Map<String, Object> values = new HashMap<String, Object>();
 
             ValueComposite valueComposite = (ValueComposite) value;
             ValueInstance instance = ValueInstance.getValueInstance( valueComposite );
             List<PropertyDescriptor> properties = instance.compositeModel().state().properties();
             for( PropertyDescriptor property : properties )
             {
 
                 PersistentPropertyModel persistentPropertyModel = (PersistentPropertyModel) property;
                 Property valueProperty = instance.state().getProperty( property.accessor() );
                 Object propertyValue = persistentPropertyModel.toValue(valueProperty.get(), entityState );
                 values.put( persistentPropertyModel.qualifiedName(), propertyValue );
             }
             value = entityState.newValueState( values );
         }
         else if( valueType instanceof SerializableType )
         {
             // Serialize value
             try
             {
                 ByteArrayOutputStream bout = new ByteArrayOutputStream();
                 ObjectOutputStream out = new ObjectOutputStream( bout );
                 out.writeObject( value );
                 out.close();
                 value = bout.toByteArray();
             }
             catch( IOException e )
             {
                 throw new IllegalArgumentException( "Could not serialize value", e );
             }
         } else if (valueType instanceof CollectionType )
         {
             CollectionType collectionType = (CollectionType)valueType;
             Collection persistentCollection = null;
             if (value instanceof List)
             {
                 List listValue = (List) value;
                 persistentCollection = new ArrayList(listValue.size());
             } else if (value instanceof Set )
             {
                 Set setValue = (Set) value;
                 persistentCollection = new HashSet(setValue.size());
             }
             for( Object item : (Collection)value )
             {
                 persistentCollection.add( toValue(collectionType.collectedType(), item, entityState ));
             }
             value = persistentCollection;
 
         }
 
         return value;
     }
 
 
     public <T> T fromValue( ModuleInstance moduleInstance, Object value )
     {
         ValueType valueType = propertyType().type();
         return this.<T>fromValue(valueType, moduleInstance, value);
     }
 
     public <T> T fromValue( ValueType valueType, ModuleInstance moduleInstance, Object value )
     {
         if (value == null)
             return null;
 
         T result;
         if( valueType instanceof CompoundType )
         {
             CompoundType compoundType = (CompoundType) propertyType().type();
             Class valueClass = moduleInstance.findClassForName( compoundType.type() );
             ValueModel model = (ValueModel) moduleInstance.findModuleForValue( valueClass ).findValueFor( valueClass );
             result = model.newValueInstance( moduleInstance, ((ValueState)value) ).<T>proxy();
         } else if (valueType instanceof SerializableType )
         {
             try
             {
                 byte[] bytes  = (byte[]) value;
                 ByteArrayInputStream bin = new ByteArrayInputStream( bytes );
                 ObjectInputStream oin = new ObjectInputStream(bin);
                 result = (T) oin.readObject();
                 oin.close();
             }
             catch( IOException e )
             {
                 throw new IllegalStateException("Could not deserialize value", e);
             }
             catch( ClassNotFoundException e )
             {
                 throw new IllegalStateException("Could not find class for serialized value", e);
             }
         } else if (valueType instanceof CollectionType)
         {
             CollectionType collectionType = (CollectionType) valueType;
             Collection loadedCollection = null;
             if (value instanceof List)
             {
                 List listValue = (List) value;
                 loadedCollection = new ArrayList(listValue.size());
             } else if (value instanceof Set )
             {
                 Set setValue = (Set) value;
                 loadedCollection = new HashSet(setValue.size());
             }
             for( Object item : (Collection)value )
             {
                 loadedCollection.add( fromValue(collectionType.collectedType(), moduleInstance, item ));
             }
             result = (T) loadedCollection;
 
         } else
         {
             result = (T) value;
         }
 
         return result;
     }
 
 }
