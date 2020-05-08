 package calendar.calendar.view;
 
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.format.FormatterRegistry;
 import org.springframework.format.support.FormattingConversionServiceFactoryBean;
 import org.springframework.roo.addon.web.mvc.controller.RooConversionService;
 
 /**
  * A central place to register application converters and formatters. 
  */
 @RooConversionService
 public class ApplicationConversionServiceFactoryBean 
 	extends FormattingConversionServiceFactoryBean implements ApplicationContextAware{
 
	@SuppressWarnings("unused")
 	private ApplicationContext applicationContext;
 	
 	@Override
 	public void setApplicationContext(ApplicationContext applicationContext)
 			throws BeansException {
 		this.applicationContext = applicationContext;
 	}
 
 	@Override
 	protected void installFormatters(FormatterRegistry registry) {
 		super.installFormatters(registry);
 		//registry.addConverter(new DayOfWeekConverter(applicationContext));
 	}
 }
