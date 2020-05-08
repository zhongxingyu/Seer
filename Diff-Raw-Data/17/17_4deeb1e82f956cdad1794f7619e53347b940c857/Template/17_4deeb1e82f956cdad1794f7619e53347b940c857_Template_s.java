 /*
  * Copyright 2010 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fast.templates;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.List;
 
 import fast.Dictionary;
 import fast.Encoder;
 import fast.FieldContainer;
 import fast.elements.Field;
 import fast.elements.PresenceMap;
 
 public abstract class Template<T extends FieldContainer> {
   private final List<Field<?>> fields = new ArrayList<Field<?>>();
 
   protected void add(Field<?> field) {
     fields.add(field);
   }
  
   protected void add(Template<?> template) {
    for(Field<?> field : template.fields)
       add(field);
   }
  
   public T decode(ByteBuffer buffer, PresenceMap pmap, Dictionary dictionary) {
     T result = newFieldContainer();
     for (Field<?> field : fields)
       result.set(field, field.decode(buffer, pmap, dictionary));
     return result;
   }
 
   public void encode(ByteBuffer buffer, FieldContainer container, Encoder encoder) {
     for (Field<?> field : fields)
      field.encode(buffer, container.get(field), encoder);
   }

  protected abstract T newFieldContainer();
 }
