 package fhdw.ipscrum.client.utils;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import fhdw.ipscrum.shared.constants.TextConstants;
 
 public final class GwtUtils {
 
 	/**
 	 * Creates a new modal dialog box. Lays a dark transparent layer over the
 	 * background.
 	 * 
 	 * @param title
 	 *            The text in the caption field.
 	 * @return
 	 */
 	public static DialogBox createDialog(final String title) {
 		final DialogBox dialog = new DialogBox();
		dialog.setAnimationEnabled(true);
 		dialog.setModal(true);
 		dialog.setGlassEnabled(true);
 		dialog.setHTML("<b>" + title + "</b>");
 		return dialog;
 	}
 
 	/**
 	 * Displays an error in an dialog box with close button.
 	 * 
 	 * @param error
 	 */
 	public static void displayError(final String error) {
 		final DialogBox dialog = createDialog(TextConstants.ERROR);
 		final VerticalPanel panel = new VerticalPanel();
 		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		panel.setSpacing(5);
 		dialog.add(panel);
 
 		final Label errorText = new Label(escapeString(error));
 		panel.add(errorText);
 
 		final Button okayButton = new Button(TextConstants.CLOSE);
 		okayButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(final ClickEvent event) {
 				dialog.hide();
 			}
 		});
 		panel.add(okayButton);
 
 		dialog.center();
 	}
 
 	public static String escapeString(final String input) {
 		// TODO Funktioniert noch nicht richtig
 		final SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
 		htmlBuilder.appendEscaped(input);
 		return htmlBuilder.toSafeHtml().asString();
 	}
 
 }
