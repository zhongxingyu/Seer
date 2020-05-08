 package com.redshape.servlet.form.impl.support;
 
 import com.redshape.servlet.form.builders.BuildersFacade;
 import com.redshape.servlet.form.fields.InputField;
 import com.redshape.utils.range.IRange;
 import com.redshape.utils.range.IntervalRange;
 import com.redshape.utils.range.RangeBuilder;
 import com.redshape.validators.impl.common.LengthValidator;
 import com.redshape.validators.impl.common.NumericStringValidator;
 import com.redshape.validators.impl.common.RangeValidator;
 
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * @author Cyril A. Karpenko <self@nikelin.ru>
  * @package com.redshape.servlet.form.impl.support
  * @date 8/16/11 8:46 PM
  */
 public class TimeForm extends DateForm {
 	private IRange<Integer> timeRange;
 
 	public TimeForm() {
 		super();
 	}
 
 	public TimeForm(String id) {
 		this(id, null);
 	}
 
 	public TimeForm(String id, String name) {
 		super(id, name);
 	}
 
 	@Override
 	protected void buildForm() {
 		super.buildForm();
 
		this.timeRange = RangeBuilder.createInterval(IntervalRange.Type.INCLUSIVE, 1, 60);
 
 		this.addField(
 				BuildersFacade.newFieldBuilder()
 					.withValidator(new NumericStringValidator())
 					.withValidator(new LengthValidator(0, 2))
 					.withValidator(new RangeValidator(this.timeRange))
 					.withName("hour")
 					.withAttribute("class", "hour-element")
 				.asFieldBuilder()
 				.newInputField(InputField.Type.TEXT)
 		);
 
 		this.addField(
 			BuildersFacade.newFieldBuilder()
 					.withValidator(new NumericStringValidator())
 					.withValidator( new LengthValidator( 0, 2 ) )
 					.withValidator( new RangeValidator( this.timeRange ) )
 					.withName("minute")
 					.withAttribute("class", "minute-element")
 				.asFieldBuilder()
 				.newInputField( InputField.Type.TEXT )
 		);
 
 		this.addField(
 			BuildersFacade.newFieldBuilder()
 					.withValidator(new NumericStringValidator())
 					.withValidator( new LengthValidator( 0, 2 ) )
 					.withValidator( new RangeValidator( this.timeRange ) )
 					.withName("second")
 					.withAttribute("class", "second-element")
 				.asFieldBuilder()
 				.newInputField( InputField.Type.TEXT )
 		);
 	}
 
 	public Integer getHour() {
 		String hour = this.getValue("hour");
 		if ( hour == null || hour.isEmpty() ) {
 			return Calendar.getInstance().get( Calendar.HOUR_OF_DAY );
 		}
 
 		return Integer.valueOf( hour );
 	}
 
 	public Integer getMinute() {
 		String minute = this.getValue("minute");
 		if ( minute == null || minute.isEmpty() ) {
 			return Calendar.getInstance().get( Calendar.MINUTE );
 		}
 
 		return Integer.valueOf( minute );
 	}
 
 	public Integer getSecond() {
 		String second = this.getValue("second");
 		if ( second == null || second.isEmpty() ) {
 			return Calendar.getInstance().get( Calendar.SECOND );
 		}
 
 		return Integer.valueOf( second );
 	}
 
 	@Override
 	public Date prepareDate() {
 		Calendar calendar = Calendar.getInstance();
 		calendar.set( Calendar.YEAR, this.getYear() );
 		calendar.set( Calendar.MONTH, this.getMonth() );
 		calendar.set( Calendar.DAY_OF_MONTH,  this.getDay() );
 		calendar.set( Calendar.HOUR, this.getHour() );
 		calendar.set( Calendar.MINUTE, this.getMinute() );
 		calendar.set( Calendar.SECOND, this.getSecond() );
 
 		return calendar.getTime();
 	}
 }
