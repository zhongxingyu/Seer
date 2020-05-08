 /**
  * Copyright 2013 Tommi S.E. Laukkanen
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
 package biz.eelis.translation;
 
 import biz.eelis.translation.model.Entry;
 import com.vaadin.data.Validator;
 import com.vaadin.ui.TextArea;
 import com.vaadin.ui.TextField;
 import org.vaadin.addons.sitekit.grid.FieldDescriptor;
 import org.vaadin.addons.sitekit.grid.field.TimestampField;
 import org.vaadin.addons.sitekit.grid.formatter.TimestampFormatter;
 import org.vaadin.addons.sitekit.site.LocalizationProvider;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 /**
  * AgoControl Site field descriptors.
  *
  * @author Tommi S.E. Laukkanen
  */
 public final class TranslationSiteFields {
 
     /**
      * Private default constructor to disable construction.
      */
     private TranslationSiteFields() {
     }
 
     /**
      * Flag reflecting whether initialization of field descriptors has been done
      * for JVM.
      */
     private static boolean initialized = false;
 
     /**
      * Map of entity class field descriptors.
      */
     private static Map<Class<?>, List<FieldDescriptor>> fieldDescriptors = new HashMap<Class<?>, List<FieldDescriptor>>();
 
     /**
      * Adds a field descriptor for given entity class.
      * @param entityClass The entity class.
      * @param fieldDescriptor The field descriptor to add.
      */
     public static void add(final Class<?> entityClass, final FieldDescriptor fieldDescriptor) {
         if (!fieldDescriptors.containsKey(entityClass)) {
             fieldDescriptors.put(entityClass, new ArrayList<FieldDescriptor>());
         }
         fieldDescriptors.get(entityClass).add(fieldDescriptor);
     }
 
     /**
      * Adds a field descriptor for given entity class.
      * @param entityClass The entity class.
      * @param fieldDescriptor The field descriptor to add.
      * @param validator The field validator.
      */
     public static void add(final Class<?> entityClass, final FieldDescriptor fieldDescriptor, final Validator validator) {
         fieldDescriptor.addValidator(validator);
         add(entityClass, fieldDescriptor);
     }
 
     /**
      * Gets field descriptors for given entity class.
      * @param entityClass The entity class.
      * @return an unmodifiable list of field descriptors.
      */
     public static List<FieldDescriptor> getFieldDescriptors(final Class<?> entityClass) {
         return Collections.unmodifiableList(fieldDescriptors.get(entityClass));
     }
 
     /**
      * Initialize field descriptors if not done yet.
      * @param localizationProvider the localization provider
      * @param locale the locale
      */
     public static synchronized void initialize(final LocalizationProvider localizationProvider, final Locale locale) {
         if (initialized) {
             return;
         }
         initialized = true;
 
         TranslationSiteFields.add(Entry.class, new FieldDescriptor(
                 "entryId", "Entry ID",
                 TextField.class, null,
                 100, null, String.class, null,
                 true, false, false));
 
         TranslationSiteFields.add(Entry.class, new FieldDescriptor(
                 "path", "Path",
                 TextField.class, null,
                 250, null, String.class, "",
                 true, true, true));
         TranslationSiteFields.add(Entry.class, new FieldDescriptor(
                 "basename", "Basename",
                 TextField.class, null,
                 200, null, String.class, "",
                 true, true, true));
         TranslationSiteFields.add(Entry.class, new FieldDescriptor(
                 "language", "Language",
                 TextField.class, null,
                 25, null, String.class, "",
                 true, true, true));
         TranslationSiteFields.add(Entry.class, new FieldDescriptor(
                 "country", "Country",
                 TextField.class, null,
                 25, null, String.class, "",
                true, true, false));
         TranslationSiteFields.add(Entry.class, new FieldDescriptor(
                 "key", "Key",
                 TextField.class, null,
                 -1, null, String.class, "",
                 true, true, true));
         TranslationSiteFields.add(Entry.class, new FieldDescriptor(
                 "value", "Value",
                 TextArea.class, null,
                 400, null, String.class, "",
                 false, true, true));
         TranslationSiteFields.add(Entry.class, new FieldDescriptor(
                 "author", "Author",
                 TextField.class, null,
                 150, null, String.class, "",
                 true, true, false));
 
         TranslationSiteFields.add(Entry.class, new FieldDescriptor(
                 "created", "Created",
                 TimestampField.class, TimestampFormatter.class,
                 150, null, Date.class, null, true,
                 true, true));
         TranslationSiteFields.add(Entry.class, new FieldDescriptor(
                 "modified", "Modified",
                 TimestampField.class, TimestampFormatter.class,
                 150, null, Date.class, null,
                 true, true, true));
 
     }
 }
