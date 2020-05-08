 package wicket.contrib.datepicker;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import wicket.contrib.datepicker.datepicker.DatepickerField;
 import wicket.markup.html.WebPage;
 import wicket.markup.html.form.DropDownChoice;
 import wicket.markup.html.form.Form;
 import wicket.model.PropertyModel;
 
 public class IndexPage extends WebPage {
 
 	private static final long serialVersionUID = 1L;
 
 	Date date = new Date();
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public IndexPage() {
 		Form form = new Form(this, "form");
 		new DatepickerField(form, "dp", new PropertyModel<Date>(this, "date"),
 				getLocale());
		List<Locale> locales = Arrays.asList(Locale.getAvailableLocales());
 		new DropDownChoice<Locale>(form, "locale", new PropertyModel<Locale>(
 				getSession(), "locale"), locales);
 	}
 }
