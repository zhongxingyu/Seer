 /**
  * Copyright (C) 2013 Sappenin Inc. (developers@sappenin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
  * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
  */
 package com.sappenin.objectify.translate;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Type;
 import java.util.Date;
 
 import org.joda.time.DateTimeZone;
 import org.joda.time.ReadableInstant;
 import org.joda.time.base.AbstractInstant;
 
 import com.googlecode.objectify.impl.Path;
 import com.googlecode.objectify.impl.Property;
 import com.googlecode.objectify.impl.TypeUtils;
 import com.googlecode.objectify.impl.translate.CreateContext;
 import com.googlecode.objectify.impl.translate.LoadContext;
 import com.googlecode.objectify.impl.translate.SaveContext;
 import com.googlecode.objectify.impl.translate.ValueTranslator;
 import com.googlecode.objectify.impl.translate.ValueTranslatorFactory;
 import com.googlecode.objectify.repackaged.gentyref.GenericTypeReflector;
 
 /**
  * An extension to {link com.googlecode.objectify.impl.translate.opt.joda.
  * ReadableInstantTranslatorFactory} that sets the TimeZone of the returned
  * {@link ReadableInstant} to {@link DateTimeZone#UTC}.
  * 
  * @author Jeff Schnitzer <jeff@infohazard.org>
  * @author David Fuelling <sappenin@gmail.com>
  * 
 * @see https://groups.google.com/forum/?fromgroups=#!searchin/objectify-appengine/joda$20ReadableInstant/objectify-appengine/_uLY3xQk0EA/b2v-Vqb1OfQJ
  */
 public class UTCReadableInstantTranslatorFactory extends ValueTranslatorFactory<ReadableInstant, Date>
 {
 	public UTCReadableInstantTranslatorFactory()
 	{
 		super(ReadableInstant.class);
 	}
 
 	@Override
 	protected ValueTranslator<ReadableInstant, Date> createSafe(Path path, Property property, Type type,
 			CreateContext ctx)
 	{
 		final Class<?> clazz = GenericTypeReflector.erase(type);
 
 		return new ValueTranslator<ReadableInstant, Date>(path, Date.class)
 		{
 			@Override
 			protected ReadableInstant loadValue(Date value, LoadContext ctx)
 			{
 				// All the Joda instants have a constructor that will take a
 				// Date
 				Constructor<?> ctor = TypeUtils.getConstructor(clazz, Object.class);
 				ReadableInstant instance = (ReadableInstant) TypeUtils.newInstance(ctor, value);
 
 				// If possible, ensure that the return ReadableInstant is in UTC
 				if (AbstractInstant.class.isAssignableFrom(clazz))
 				{
 					instance = ((AbstractInstant) instance).toDateTime(DateTimeZone.UTC);
 				}
 
 				return instance;
 			}
 
 			@Override
 			protected Date saveValue(ReadableInstant value, SaveContext ctx)
 			{
 				return value.toInstant().toDate();
 			}
 		};
 	}
 }
