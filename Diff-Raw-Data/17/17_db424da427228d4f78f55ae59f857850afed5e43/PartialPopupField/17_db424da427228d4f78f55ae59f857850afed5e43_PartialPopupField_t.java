 /*
  * Copyright 2010 Instituto Superior Tecnico
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the vaadin-framework.
  *
 *   The vaadin-framework Infrastructure is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version
  *   3 of the License, or (at your option) any later version.*
  *
  *   vaadin-framework is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with vaadin-framework. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package pt.ist.vaadinframework.ui.fields;
 
 import java.util.Date;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeFieldType;
 import org.joda.time.Partial;
 
 import com.vaadin.data.Property;
 import com.vaadin.ui.PopupDateField;
 
 /**
  * @author Pedro Santos (pedro.miguel.santos@ist.utl.pt)
  * 
  */
 @SuppressWarnings("serial")
 public class PartialPopupField extends PopupDateField {
     public static class PartialProperty implements Property {
 	private final PartialPopupField field;
 
 	private final Property property;
 
 	public PartialProperty(PartialPopupField field, Property dataSource) {
 	    this.field = field;
 	    this.property = dataSource;
 	    if (!Partial.class.isAssignableFrom(dataSource.getType())) {
 		throw new IllegalArgumentException("Can't use " + dataSource.getType().getName()
 			+ " typed property as datasource");
 	    }
 	}
 
 	@Override
 	public Object getValue() {
 	    Partial date = (Partial) property.getValue();
 	    return date == null ? null : date.toDateTime(new DateTime()).toDate();
 	}
 
 	@Override
 	public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
 	    Date date = (Date) newValue;
 	    Partial partial = (Partial) property.getValue();
 	    if (date != null) {
		if (partial == null) {
		    partial = new Partial();
		}
 		DateTime dateTime = new DateTime(date);
 		if (field.getResolution() <= RESOLUTION_YEAR) {
 		    partial = partial.with(DateTimeFieldType.year(), dateTime.get(DateTimeFieldType.year()));
 		}
 		if (field.getResolution() <= RESOLUTION_MONTH) {
 		    partial = partial.with(DateTimeFieldType.monthOfYear(), dateTime.get(DateTimeFieldType.monthOfYear()));
 		}
 		if (field.getResolution() <= RESOLUTION_DAY) {
 		    partial = partial.with(DateTimeFieldType.dayOfMonth(), dateTime.get(DateTimeFieldType.dayOfMonth()));
 		}
 		if (field.getResolution() <= RESOLUTION_HOUR) {
 		    partial = partial.with(DateTimeFieldType.hourOfDay(), dateTime.get(DateTimeFieldType.hourOfDay()));
 		}
 		if (field.getResolution() <= RESOLUTION_MIN) {
 		    partial = partial.with(DateTimeFieldType.minuteOfHour(), dateTime.get(DateTimeFieldType.minuteOfHour()));
 		}
 		if (field.getResolution() <= RESOLUTION_SEC) {
 		    partial = partial.with(DateTimeFieldType.secondOfMinute(), dateTime.get(DateTimeFieldType.secondOfMinute()));
 		}
 		if (field.getResolution() <= RESOLUTION_MSEC) {
 		    partial = partial.with(DateTimeFieldType.millisOfSecond(), dateTime.get(DateTimeFieldType.millisOfSecond()));
 		}
	    } else {
		partial = null;
 	    }
 	    property.setValue(partial);
 	}
 
 	@Override
 	public Class<?> getType() {
 	    return Date.class;
 	}
 
 	@Override
 	public boolean isReadOnly() {
 	    return property.isReadOnly();
 	}
 
 	@Override
 	public void setReadOnly(boolean newStatus) {
 	    property.setReadOnly(newStatus);
 	}
     }
 
     public PartialPopupField() {
 	super();
     }
 
     @Override
     public void setPropertyDataSource(Property dataSource) {
 	super.setPropertyDataSource(new PartialProperty(this, dataSource));
     }
 }
