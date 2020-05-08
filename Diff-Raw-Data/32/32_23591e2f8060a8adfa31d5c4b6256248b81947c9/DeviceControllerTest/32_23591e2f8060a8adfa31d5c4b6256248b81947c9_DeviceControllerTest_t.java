 package controllers;
 
 import models.Notification;
 import org.junit.Before;
 import org.junit.Test;
 import play.api.mvc.AnyContentAsText;
 import play.data.Form;
 import play.mvc.Content;
 import play.mvc.Result;
 import play.test.FakeRequest;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import static junit.framework.Assert.*;
 import static org.fest.assertions.Assertions.assertThat;
 import static play.test.Helpers.*;
 
 public class DeviceControllerTest {
     @Before
     public void setup() {
         DeviceController.getDevices().clear();
     }
 
     @Test
     public void testDevicesShowDevicesNumber() {
         Result result = callAction(controllers.routes.ref.DeviceController.devices(), fakeRequest());
         assertThat(status(result)).isEqualTo(OK);
         assertThat(contentType(result)).isEqualTo("text/html");
        assertThat(contentAsString(result)).contains("No devices registered at the moment");
     }
 
     @Test
     public void testRegisterNoDeviceIdAndRegistrationIdProvided_returnBadRequest() {
         Result result = callAction(controllers.routes.ref.DeviceController.register(), new FakeRequest(POST, "/register").withFormUrlEncodedBody(new HashMap<String, String>()));
         assertThat(status(result)).isEqualTo(BAD_REQUEST);
     }
 
     @Test
     public void testRegisterNoDeviceIdProvided_returnBadRequest() {
         Map<String, String> params = new HashMap<String, String>();
         params.put("registrationId", "abc");
 
         Result result = callAction(controllers.routes.ref.DeviceController.register(), new FakeRequest(POST, "/register").withFormUrlEncodedBody(params));
         assertThat(status(result)).isEqualTo(BAD_REQUEST);
     }
 
     @Test
     public void testRegisterNoRegisterationIdProvided_returnBadRequest() {
         Map<String, String> params = new HashMap<String, String>();
         params.put("deviceId", "device1");
 
         Result result = callAction(controllers.routes.ref.DeviceController.register(), new FakeRequest(POST, "/register").withFormUrlEncodedBody(params));
         assertThat(status(result)).isEqualTo(BAD_REQUEST);
     }
 
     @Test
     public void testRegisterRegisterationIdAndDeviceIdProvided_shouldRedirect() {
         Map<String, String> params = new HashMap<String, String>();
         params.put("deviceId", "device1");
         params.put("registrationId", "abc");
 
         Result result = callAction(controllers.routes.ref.DeviceController.register(), new FakeRequest(POST, "/register").withFormUrlEncodedBody(params));
         assertThat(status(result)).isEqualTo(SEE_OTHER);
     }
 
     @Test
     public void renderSendMessageTemplate() {
         Content content = views.html.sendMessage.render(Form.form(Notification.class), "");
         assertThat(contentType(content)).isEqualTo("text/html");
         assertThat(contentAsString(content)).contains("Send message to all registered devices");
     }
 
     @Test
    public void testshowSendForm() {
        Result result = callAction(controllers.routes.ref.DeviceController.showSendForm(), fakeRequest());
         assertThat(status(result)).isEqualTo(OK);
         assertThat(contentType(result)).isEqualTo("text/html");
         assertThat(contentAsString(result)).contains("Send message to all registered devices");
     }
 
     @Test
     public void testSend_returnBadRequestWhenNoMessage() {
         Map<String, String> params = new HashMap<String, String>();
         params.put("content", null);
 
         Result result = callAction(routes.ref.DeviceController.send(), new FakeRequest(POST, "/send").withFormUrlEncodedBody(params));
         assertThat(status(result)).isEqualTo(BAD_REQUEST);
     }
 }
