 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package de.unioninvestment.eai.portal.portlet.crud.scripting.domain.events;
 
 import groovy.lang.Closure;
 
 import java.sql.Timestamp;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.vaadin.data.util.converter.Converter;
 
 import de.unioninvestment.eai.portal.portlet.crud.domain.container.EditorSupport;
 import de.unioninvestment.eai.portal.portlet.crud.domain.events.CreateEvent;
 import de.unioninvestment.eai.portal.portlet.crud.domain.events.CreateEventHandler;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.ContainerRow;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.DataContainer;
 import de.unioninvestment.eai.portal.support.vaadin.context.Context;
 
 /**
  * 
  * Handler, der für das Setzen der Standardwerte in einer neuen Zeile auf der
  * Tabelle zuständig ist. Wird ein leerer String zurückgeliefert, so wird kein
  * Default gesetzt.
  * 
  */
 public class NewRowDefaultsSetterHandler implements CreateEventHandler {
 	private static final long serialVersionUID = 42L;
 	private final Map<String, Closure<?>> defaultValues;
 
 	/**
 	 * Konstruktor.
 	 * 
 	 * @param defaultValues
 	 *            Standardwerte für die Spalten
 	 */
 	public NewRowDefaultsSetterHandler(Map<String, Closure<?>> defaultValues) {
 
 		this.defaultValues = defaultValues;
 	}
 
 	@Override
 	public void onCreate(CreateEvent event) {
 		ContainerRow row = event.getRow();
 
 		for (Entry<String, Closure<?>> entry : defaultValues.entrySet()) {
 			String columnName = entry.getKey();
 			Closure<?> closure = entry.getValue();
 
 			Class<?> type = event.getSource().getType(columnName);
 			String now = null;
 			if (Timestamp.class.isAssignableFrom(type)) {
 				now = calculateNow(event.getSource(), columnName);
 			}
 
 			Object result = closure.call(now);
 
 			String value = result != null ? result.toString() : null;
 			if (value == null || !value.isEmpty()) {
 				row.setText(columnName, value);
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private String calculateNow(DataContainer databaseContainer,
 			String columnName) {
 		EditorSupport editor = databaseContainer.findEditor(columnName);
 		Converter<String, Object> formatter = (Converter<String, Object>) editor
 				.createFormatter(Timestamp.class,
 						databaseContainer.getFormat(columnName));
 		return formatter.convertToPresentation(
 				new Timestamp(System.currentTimeMillis()), String.class,
 				Context.getLocale());
 	}
 }
