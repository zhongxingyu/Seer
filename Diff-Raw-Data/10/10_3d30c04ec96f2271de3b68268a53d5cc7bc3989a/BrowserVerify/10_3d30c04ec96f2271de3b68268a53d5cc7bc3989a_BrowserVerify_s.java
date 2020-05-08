 package com.google.codes.dryvalidator;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import junit.framework.Assert;
 import net.arnx.jsonic.JSON;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.validator.Field;
 import org.apache.commons.validator.Form;
 import org.apache.commons.validator.ValidatorResources;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.server.handler.ResourceHandler;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.ie.InternetExplorerDriver;
 
 import com.google.codes.dryvalidator.dto.FormItem;
 import com.google.codes.dryvalidator.dto.Validation;
 
 public class BrowserVerify {
 
 	public static class FormItemHandler extends AbstractHandler {
 		@SuppressWarnings("unchecked")
 		private List<FormItem> getFormItem(String formName) {
 			InputStream in = this.getClass().getResourceAsStream(
 					"/validation.xml");
 			ValidatorResources resources = null;
 			try {
 				resources = new ValidatorResources(in);
 			} catch (Exception e) {
 
 			} finally {
 				IOUtils.closeQuietly(in);
 			}
 			Form form = resources.getForm(Locale.JAPANESE, formName);
 
 			List<FormItem> formItemList = new ArrayList<FormItem>();
 			for (Field field : (List<Field>) form.getFields()) {
 				FormItem formItem = new FormItem();
 				formItem.setId(field.getProperty());
 				formItem.setLabel(field.getArg(0).getKey());
 				for (String depend : (List<String>) field.getDependencyList()) {
 					String value = null;
 					if (StringUtils.equals(depend, "required")) {
 						value = "true";
 					} else {
 						value = field.getVarValue(depend);
 					}
 					if (StringUtils.equals(depend, "maxlength")) {
 						depend = "maxLength";
 					}
 					Validation validation = new Validation(depend, value);
 					formItem.getValidations().addValidation(validation);
 				}
 				formItemList.add(formItem);
 			}
 			return formItemList;
 		}
 
		@Override
 		public void handle(String target, Request baseRequest,
 				HttpServletRequest request, HttpServletResponse response)
 				throws IOException, ServletException {
 			if (!StringUtils.equals("/formItem", target)) {
 				baseRequest.setHandled(false);
 				return;
 			}
 			response.setContentType("application/json;charset=utf-8");
 			response.setStatus(HttpServletResponse.SC_OK);
 			baseRequest.setHandled(true);
 			response.getWriter().println(
 					JSON.encode(getFormItem("MemberForm")));
 		}
 	}
 
 	@BeforeClass
 	public static void ウェブサーバを起動する() throws Exception {
 		Server server = new Server();
 		SelectChannelConnector connector = new SelectChannelConnector();
 		connector.setPort(8888);
 		server.addConnector(connector);
 
 		ResourceHandler rh = new ResourceHandler();
 		rh.setDirectoriesListed(true);
 		rh.setResourceBase(".");
 
 		HandlerList handlerList = new HandlerList();
 		handlerList.setHandlers(new Handler[] { new FormItemHandler(), rh });
 		server.setHandler(handlerList);
 		server.start();
 	}
 
 	@Test
 	public void ブラウザテスト() {
 		WebDriver driver = new InternetExplorerDriver();
 		driver.get("http://127.0.0.1:8888/src/test/resources/validation.html");
 		driver.findElement(By.name("familyName")).sendKeys("長い文字ながーーーーい文字");
 		driver.findElement(By.name("firstName")).sendKeys("長い文字");
 		driver.findElement(By.id("doValidate")).click();
 		List<WebElement> messages = driver.findElement(By.id("messageArea"))
 				.findElements(By.tagName("li"));
		Assert.assertEquals(1, messages.size());
 		Assert.assertEquals("氏名は10文字以内で入力してください。", messages.get(0).getText());
 
 		driver.findElement(By.name("familyName")).clear();
 		driver.findElement(By.name("familyName")).sendKeys("hahaha");
 		driver.findElement(By.id("doValidate")).click();
 		messages = driver.findElement(By.id("messageArea")).findElements(
 				By.tagName("li"));
		Assert.assertEquals(1, messages.size());
 		Assert.assertEquals("氏名は全角文字で入力してください。", messages.get(0).getText());
 
 		driver.findElement(By.name("hasSpouse")).click();
 		driver.findElement(By.id("doValidate")).click();
 		messages = driver.findElement(By.id("messageArea")).findElements(
 				By.tagName("li"));
		Assert.assertEquals(2, messages.size());
 
 
 		driver.quit();
 	}
 
 	public static void main(String[] args) throws Exception {
 		ウェブサーバを起動する();
 	}
 }
