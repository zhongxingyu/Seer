 package fi.vincit.jmobster.processor.defaults.validator;
 
 /*
  * Copyright 2012-2013 Juha Siponen
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
 
 import fi.vincit.jmobster.processor.languages.LanguageContext;
 import fi.vincit.jmobster.util.itemprocessor.ItemHandler;
 import fi.vincit.jmobster.util.itemprocessor.ItemProcessor;
 import fi.vincit.jmobster.util.itemprocessor.ItemStatus;
 import fi.vincit.jmobster.util.writer.DataWriter;
 
 import java.lang.annotation.Annotation;
import java.util.*;
 
 public abstract class DefaultValidatorWriterSet<C extends LanguageContext<? extends W>, W extends DataWriter> implements ValidatorWriterSet<C,W> {
     final private Set<ValidatorWriter<? super C, ? super W>> writers =
             new HashSet<ValidatorWriter<? super C, ? super W>>();
 
     private C context;
 
     public DefaultValidatorWriterSet(Collection<ValidatorWriter<? super C, ? super W>> requiredValidatorWriter,
                                      Collection<ValidatorWriter<? super C, ? super W>>... validatorWriters) {
         addValidatorWriters(requiredValidatorWriter);
         for (Collection<ValidatorWriter<? super C, ? super W>> validatorWriterCollection : validatorWriters) {
             addValidatorWriters(validatorWriterCollection);
         }
     }
 
     private void addValidatorWriters(Collection<ValidatorWriter<? super C, ? super W>> validatorWriterCollection) {
         for (ValidatorWriter<? super C, ? super W> validatorWriter : validatorWriterCollection) {
             this.writers.add(validatorWriter);
         }
     }
 
     @Override
     public void setValidator(ValidatorWriter<? super C, ? super W>... validatorWriters) {
        Collections.addAll(writers, validatorWriters);
     }
 
     @Override
     public void write(final Collection<? extends Annotation> annotations, ItemStatus status) {
         Collection<ValidatorWriter<? super C, ? super W>> supportedWriters =
                 new ArrayList<ValidatorWriter<? super C, ? super W>>(this.writers.size());
         for( ValidatorWriter<? super C, ? super W> writer : this.writers ) {
             if( writer.supportsAnnotations(annotations) ) {
                 supportedWriters.add(writer);
             }
         }
 
         ItemProcessor.process(supportedWriters).with(new ItemHandler<ValidatorWriter<? super C, ? super W>>() {
             @Override
             public void process(ValidatorWriter<? super C, ? super W> writer, ItemStatus status) {
                 beforeWrite(status);
                 writer.setContext(context);
 
                 writer.write(annotations);
                 // Reset context just to be sure if user does something stupid
                 writer.setContext(null);
                 afterWrite(status);
             }
         });
     }
 
     protected abstract void beforeWrite(ItemStatus status);
     protected abstract void afterWrite(ItemStatus status);
 
     protected C getContext() {
         return context;
     }
 
     protected W getWriter() {
         return context.getWriter();
     }
 
     @Override
     public void setLanguageContext(C context) {
         this.context = context;
     }
 
 }
