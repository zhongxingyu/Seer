 package org.glom.web.client;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import org.glom.web.client.ui.DocumentSelectionView;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.googlecode.gwt.test.GwtTestWithEasyMock;
 
 public class GwtTestOnlineGlom extends GwtTestWithEasyMock {
 
 	/**
 	 * Tell gwt-test-utils to use this mock when trying to create this class via GWT.create() in OnlineGlom.
 	 */
 	// @Mock
 	// private AppPlaceHistoryMapper mockAppPlaceHistoryMapper;
 
 	private OnlineGlom app;
 
 	@Test
 	public void testSomething() {
 		assertTrue(true);
 		/*
 		 * TODO: For instance: // Arrange Browser.fillText(app.nameField, "123");
 		 * 
 		 * // Act Browser.click(app.sendButton);
 		 * 
 		 * // Assert assertFalse(app.dialogBox.isShowing()); assertEquals("Please enter at least four characters",
 		 * app.errorLabel.getText());
 		 */
 	}
 
 	@Override
 	public String getModuleName() {
		return "org.glom.web.OnlineGlom";
 	}
 
 	@Before
 	public void beforeOnlineGlom() {
 		app = new OnlineGlom();
 		app.onModuleLoad();
 
 		assertNotNull(app.clientFactory);
 		final DocumentSelectionView view = app.clientFactory.getDocumentSelectionView();
 		assertNotNull(view);
 		assertNotNull(view.asWidget());
 		assertTrue(view.asWidget().isVisible());
 
 		// Some pre-assertions
 		assertTrue(app.docSelectionPanel.isVisible());
 		assertFalse(app.dataPanel.isVisible());
 		assertFalse(app.tableSelectionPanel.isVisible());
 		// TODO: For instance: assertEquals("", app.docSelectionPanel.getText());
 	}
 
 }
