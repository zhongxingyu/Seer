 /**
  * Copyright (C) 2013 Jean-Philippe Ricard.
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
 package com.arcanix.introspection.wrapper;
 
 import com.arcanix.convert.ConversionException;
 import com.arcanix.convert.Converters;
 import com.arcanix.introspection.Property;
 
 /**
  * @author ricardjp@arcanix.com (Jean-Philippe Ricard)
  */
 public abstract class AbstractWrapper implements PropertyWrapper {
 
 	private final Converters converters;
 	
 	protected AbstractWrapper(final Converters converters) {
 		if (converters == null) {
 			throw new NullPointerException("Converters cannot be null");
 		}
 		this.converters = converters;
 	}
 	
 	protected final Converters getConverters() {
 		return this.converters;
 	}
 	
 	@Override
 	public final void setProperty(final Property property) throws ConversionException {
 		if (property.getNextProperty() != null) {
 			final Object initialValue = getValue(property);
 			final PropertyWrapper nextWrapper = PropertyWrapperFactory.getPropertyWrapper(
 					initialValue, getPropertyType(property), this.converters);
 			setLocalProperty(property, nextWrapper);
 			nextWrapper.setProperty(property.getNextProperty());
 		} else {
 			
 			// verify if no embedded wrappers (i.e.: map of lists or map of sets)
 			Property previousProperty = property.getPreviousProperty();
			if (previousProperty != null && (previousProperty.isMapped() || previousProperty.isIndexed())) {
 				final Object initialValue = getValue(previousProperty);
 				final PropertyWrapper nextWrapper = PropertyWrapperFactory.getPropertyWrapper(
 						initialValue, getPropertyType(property), this.converters);
 				
 				if (initialValue == null) {
 					setLocalProperty(previousProperty, nextWrapper);
 				}
 				nextWrapper.setLocalProperty(property);
 			} else {
 				setLocalProperty(property);
 			}
 		}
 	}
 	
 }
