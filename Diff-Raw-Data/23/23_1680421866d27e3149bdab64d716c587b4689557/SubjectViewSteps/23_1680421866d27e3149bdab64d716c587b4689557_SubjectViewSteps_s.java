 package org.seamoo.webapp.client.admin.ui;
 
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 import static org.testng.Assert.*;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.List;
 
 import org.jbehave.scenario.annotations.Alias;
 import org.jbehave.scenario.annotations.Given;
 import org.jbehave.scenario.annotations.Then;
 import org.jbehave.scenario.annotations.When;
 import org.powermock.api.mockito.PowerMockito;
 import org.seamoo.entities.Subject;
 import org.seamoo.webapp.client.admin.SubjectListPresenter.SubjectDisplay.SubjectDisplayEventListener;
 import org.seamoo.webapp.client.admin.SubjectListPresenter.SubjectDisplay.SubjectDisplayMode;
 import org.seamoo.webapp.client.admin.ui.SubjectView.SubjectViewUiBinder;
 import org.seamoo.webapp.client.uimocker.GwtUiMocker;
 import org.seamoo.webapp.client.uimocker.MockedClickable;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.ui.Widget;
 
 public class SubjectViewSteps {
 	SubjectView subjectView;
 	SubjectDisplayEventListener listener;
 	Subject subject;
 
 	@Given("A SubjectView")
 	public void initSubjectDisplay() {
 		PowerMockito.mockStatic(GWT.class);
 		when(GWT.create(SubjectViewUiBinder.class)).thenReturn(new SubjectViewUiBinder() {
 
 			@Override
 			public Widget createAndBindUi(SubjectView subjectView) {
 				// TODO Auto-generated method stub
 				return GwtUiMocker.mockUiField(subjectView, Widget.class);
 			}
 		});
 		listener = mock(SubjectDisplayEventListener.class);
 		subjectView = new SubjectView();
 		subjectView.addEventListener(listener);
 	}
 
 	@Given("An $enabled Subject")
 	@Alias("A $enabled Subject")
 	public void initSubject(String enabled) {
 		subject = new Subject(1L, "English", "images/subjects/english.png", "English for everyone", enabled.equals("enabled"));
 		subjectView.setSubject(subject);
 	}
 
 	@When("Mode is switched to $mode")
 	public void switchMode(String mode) {
 		subjectView.setMode(SubjectDisplayMode.valueOf(mode));
 	}
 
 	private Widget getWidget(String widgetName) {
 		try {
 			Field field = null;
 			field = SubjectView.class.getDeclaredField(widgetName);
 			field.setAccessible(true);
 			return (Widget) field.get(subjectView);
 		} catch (Exception ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	@Then("$widgets are visible")
 	@Alias("$widgets is visible")
 	public void assertFieldsShown(List<String> widgets) {
 		assertFieldsVisibility(widgets, true);
 
 	}
 
 	@Then("$widgets are invisible")
 	@Alias("$widgets is invisible")
 	public void assertFieldsHidden(List<String> widgets) {
 		assertFieldsVisibility(widgets, false);
 	}
 
 	public void assertFieldsVisibility(List<String> fields, boolean visible) {
 		for (int i = 0; i < fields.size(); i++) {
 			String field = fields.get(i);
 			assertEquals(getWidget(field).isVisible(), visible, String.format("%s.visble <> %b", field, visible));
 		}
 	}
 
 	@When("$widgetName is clicked")
 	public void clickWidget(String widgetName) {
 		Widget control = getWidget(widgetName);
 		((MockedClickable) control).click();
 	}
 
 	@Then("$event event is triggered")
 	public void assertEventTriggered(String event) {
 		if (event.equals("edit"))
 			verify(listener).edit(subjectView);
 		else if (event.equals("edit-cancel"))
 			verify(listener).cancelEdit(subjectView);
 		else if (event.equals("delete"))
 			verify(listener).delete(subjectView, subject);
 		else if (event.equals("create"))
 			verify(listener).create(eq(subjectView), (Subject) any());
 		else if (event.equals("update"))
 			verify(listener).update(eq(subjectView), (Subject) any());
 		else
 			throw new RuntimeException("Event " + event + " is not supported");
 	}
 
 	public static String capitalize(String s) {
 		if (s.length() == 0)
 			return s;
 		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
 	}
 
 	@Then("$property of $widget is \"$value\"")
 	public void assertWidgetProperty(String property, String widget, String value) throws SecurityException,
 			NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
 		Widget control = getWidget(widget);
 		Method method = control.getClass().getMethod("get" + capitalize(property));
 		Object actualValue = method.invoke(control);
 		assertEquals(actualValue.toString(), value);
 	}
 
 	@When("$property of $widget is changed to \"$value\"")
 	public void changeWidgetProperty(String property, String widget, String value) throws SecurityException,
 			NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
 		Object v = value.equals("true") ? true : value.equals("false") ? false : value;
 		Widget control = getWidget(widget);
 		Class<?> paramClass = property.equals("value") ? Object.class : String.class;
 		Method method = control.getClass().getMethod("set" + capitalize(property), paramClass);
 		method.invoke(control, v);
 	}
 
 	@Then("editedSubject.$field is \"$value\"")
 	public void assertEditedSubject(String field, String value) {
 		Subject editedSubject = subjectView.getEditedSubject();
 		Object v = null;
 		if (field.equals("name"))
 			v = editedSubject.getName();
 		else if (field.equals("description"))
 			v = editedSubject.getDescription();
 		else if (field.equals("enabled"))
 			v = editedSubject.isEnabled();
 		else if (field.equals("logoUrl"))
 			v = editedSubject.getLogoUrl();
 		else
 			throw new RuntimeException("Unsupported field Subject." + field);
 		assertEquals(v.toString(), value);
 	}
	
 	@When("setLogoUrl is invoked with \"$url\"")
	public void setLogoUrl(String url){
 		subjectView.setLogoUrl(url);
 	}
 }
