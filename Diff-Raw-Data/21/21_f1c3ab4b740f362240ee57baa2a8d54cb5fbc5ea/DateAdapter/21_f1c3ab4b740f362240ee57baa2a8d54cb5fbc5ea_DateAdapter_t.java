 /**
  * Copyright 2010 CosmoCode GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.cosmocode.palava.ipc.xml.rpc.adapters;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.xml.bind.JAXBElement;
 
 import com.google.common.base.Preconditions;
 import com.google.inject.Inject;
 import com.google.inject.TypeLiteral;
 
 import de.cosmocode.palava.ipc.xml.rpc.XmlRpc;
 import de.cosmocode.palava.ipc.xml.rpc.generated.ObjectFactory;
 import de.cosmocode.palava.ipc.xml.rpc.generated.Value;
 
 /**
  * A {@link DateTimeIso8601} to {@link Date} adapter.
  *
  * @since 1.0
  * @author Willi Schoenborn
  */
 final class DateAdapter implements Adapter<Value, Date> {
     
     static final TypeLiteral<Adapter<Value, Date>> LITERAL =
         new TypeLiteral<Adapter<Value, Date>>() { };
        
    private final DateFormat pseudoIso8061 = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
 
     private final ObjectFactory factory;
     
     @Inject
     public DateAdapter(@XmlRpc ObjectFactory factory) {
         this.factory = Preconditions.checkNotNull(factory, "Factory");
     }
 
     @Override
     public Date decode(Value input) {
         Preconditions.checkNotNull(input, "Input");
         try {
             @SuppressWarnings("unchecked")
             final JAXBElement<String> element = JAXBElement.class.cast(input.getContent().get(0));
            return pseudoIso8061.parse(element.getValue());
         } catch (ParseException e) {
             throw new IllegalArgumentException(e);
         }
     }
     
     @Override
     public Value encode(Date input) {
         Preconditions.checkNotNull(input, "Input");
         final Value value = factory.createValue();
        value.getContent().add(factory.createValueDateTimeIso8601(pseudoIso8061.format(input)));
         return value;
     }
     
 }
