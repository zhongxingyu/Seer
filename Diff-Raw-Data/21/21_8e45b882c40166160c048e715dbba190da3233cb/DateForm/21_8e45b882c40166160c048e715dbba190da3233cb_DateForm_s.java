 package com.redshape.servlet.form.impl.support;
 
 import com.redshape.servlet.form.builders.BuildersFacade;
 import com.redshape.servlet.form.fields.InputField;
 import com.redshape.servlet.form.impl.Form;
 import com.redshape.utils.Commons;
 import com.redshape.utils.range.IntervalRange;
 import com.redshape.utils.range.RangeBuilder;
 import com.redshape.validators.impl.common.NumericStringValidator;
 import com.redshape.validators.impl.common.RangeValidator;
 
 import java.util.*;
 
 /**
  * @author Cyril A. Karpenko <self@nikelin.ru>
  * @package com.redshape.servlet.form.impl.support
  * @date 8/15/11 12:41 PM
  */
 public class DateForm extends Form {
 
 	public DateForm() {
 		this(null);
 	}
 
 	public DateForm(String id) {
 		this(id, null);
 	}
 
 	public DateForm(String id, String name) {
 		super(id, name);
 
 		this.buildForm();
 	}
 
 	protected void buildForm() {
 		this.setAttribute("class", "date-form");
 
 		this.addField(
 				BuildersFacade.newFieldBuilder()
 					.withValue( Calendar.getInstance().get( Calendar.DAY_OF_MONTH ))
 					.withValidator(new NumericStringValidator())
 					.withValidator(new RangeValidator(
 							RangeBuilder.createInterval(IntervalRange.Type.INCLUSIVE, 1, 31)
 					))
 					.withName("day")
 					.withAttribute("class", "day-element")
 				.asFieldBuilder()
 				.newInputField(InputField.Type.TEXT)
 		);
 
 		this.addField(
 				BuildersFacade.newFieldBuilder()
 					.withValue( Calendar.getInstance().get( Calendar.MONTH ) )
 					.withValidator(new RangeValidator(
 							RangeBuilder.createInterval(IntervalRange.Type.INCLUSIVE, 0, 11)
 					))
 					.withValidator(new NumericStringValidator() )
 					.withName("month")
 					.withAttribute("class", "month-element")
 				.asFieldBuilder()
 				.newSelectField(this.createMonthsList())
 		);
 
 		this.addField(
 				BuildersFacade.newFieldBuilder()
 					.withValue( Calendar.getInstance().get( Calendar.YEAR ) )
 					.withValidator(new NumericStringValidator())
 					.withName("year")
 					.withAttribute("class", "year-element")
 				.asFieldBuilder()
 				.newInputField(InputField.Type.TEXT)
 		);
 	}
 
 	protected Map<String, Object> createMonthsList() {
 		Map<String, Object> result = new LinkedHashMap<String, Object>();
 
 		Calendar calendar = Calendar.getInstance();
 		for ( int i = 0; i <= 11; i++ ) {
 			calendar.set(Calendar.MONTH, i);
 			result.put( calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()), i );
 		}
 
 		return result;
 	}
 
 	public void setMonth( Integer value ) {
 		this.setValue("month", Commons.select(value, Calendar.getInstance().get(Calendar.MONTH) ) );
 	}
 
 	public Integer getMonth() {
 		String value = this.getValue("month");
		if ( value == null || value.equals("null") ) {
 			value = String.valueOf( Calendar.getInstance().get( Calendar.MONTH ) );
 		}
 
 		return Integer.valueOf( value );
 	}
 
 	public void setDay( Integer value ) {
 		this.setValue("day", Commons.select( value, Calendar.getInstance().get(Calendar.DAY_OF_MONTH) ) );
 	}
 
 	public Integer getDay() {
 		String value = this.getValue("day");
		if ( value == null || value.equals("null") ) {
 			value = String.valueOf( Calendar.getInstance().get( Calendar.DAY_OF_MONTH ) );
 		}
 
 		return Integer.valueOf( value );
 	}
 
 	public void setYear( Integer value ) {
 		this.setValue( "year", Commons.select( value, Calendar.getInstance().get(Calendar.YEAR) ) );
 	}
 
 	public Integer getYear() {
 		String value = this.getValue("year");
		if ( value == null || value.equals("null") ) {
 			value = String.valueOf( Calendar.getInstance().get( Calendar.YEAR ) );
 		}
 
 		return Integer.valueOf( value );
 	}
 
 	public Date prepareDate() {
 		return new Date(
 			Integer.valueOf( this.getYear() ),
 			Integer.valueOf( this.getMonth() ),
 			Integer.valueOf( this.getDay() )
 		);
 	}
 }
