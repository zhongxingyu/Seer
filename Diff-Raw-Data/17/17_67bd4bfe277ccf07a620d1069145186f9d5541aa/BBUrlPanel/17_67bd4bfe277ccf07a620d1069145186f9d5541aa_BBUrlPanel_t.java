 package burrito.client.crud.widgets;
 
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class BBUrlPanel extends BBPopupPanel {
 
 	private TextBox srcTextBox;
 	private TextBox titleTextBox;
 
 	public BBUrlPanel(SelectableTextArea rawEditor) {
 		super("Länk", rawEditor);
 	}
 
 	@Override
 	protected void onShow() {
 		Label titleLabel = new Label("title");
 		titleTextBox = new TextBox();
 		
 		Label srcLabel = new Label("src");
 		srcTextBox = new TextBox();
 		
 		HorizontalPanel titlePanel = new HorizontalPanel();
 		titlePanel.add(titleLabel);
 		titlePanel.add(titleTextBox);
 		addPanel(titlePanel);
 		
 		HorizontalPanel srcPanel = new HorizontalPanel();
 		srcPanel.add(srcLabel);
 		srcPanel.add(srcTextBox);
 		addPanel(srcPanel);
 		
		String selectedText2 = getSelectedText();
		
		titleTextBox.setText(selectedText2);
		
		if (!selectedText2.startsWith("http://")) {
			selectedText2 = "http://" + selectedText2;
		}
		
		srcTextBox.setText(selectedText2);
 	}
 
 	private boolean validate(String url) {
 		return url.matches("^(http(s?)://){1}.*$");
 	}
 	
 	@Override
 	protected boolean onClose() {
 		String url = srcTextBox.getText();
 		if (!validate(url)) {
 			Window.alert("Not valid image url!");
 			return false;
 		}
 		
 		String text = titleTextBox.getText();
 		if (!"".equals(text)) {
 			setSelectedText("[url=" + url + "]" + text + "[/url]");
 		} else {
 			setSelectedText("[url]" + url + "[/url]");
 		}
 		
 		return true;
 	}
 }
