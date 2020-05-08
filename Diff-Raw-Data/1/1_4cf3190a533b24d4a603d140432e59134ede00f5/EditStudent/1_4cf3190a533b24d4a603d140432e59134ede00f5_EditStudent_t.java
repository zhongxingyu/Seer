 
 // JUnit Assert framework can be used for verification
 
 import static junit.framework.Assert.assertEquals;
 import net.sf.sahi.client.Browser;
 
 public class EditStudent {
 
 	private Browser browser;
 
 	public EditStudent(Browser browser) {
 		this.browser = browser;
 	}
 
 	public void clickOnStudentRecord(String name) throws Exception {
 		browser.link(name).click();
 	}
 
 	public void clickOnEditButton() throws Exception {
 		browser.submit("student-edit").click();
 	}
 
 	public void verifyThatBackgroundIs(String string1) throws Exception {
 		assertEquals(string1,browser.textarea("edit-background").text());
 
 	}
 
 	public void verifyThatPerformanceIs(String string1) throws Exception {
 		assertEquals(string1,browser.textarea("edit-performance").text());
 	}
 
 	public void verifyThatDisciplinaryIs(String string1) throws Exception {
 		assertEquals(string1,browser.textarea("edit-disciplinary").text());
 	}
 
 	public void click(String label) throws Exception {
 		browser.button(label).click();
	}
 
 	public void verifyThatStatusIs(String status) throws Exception {
 		assertEquals(status,browser.byId("status").selectedText());
 	}
 }
