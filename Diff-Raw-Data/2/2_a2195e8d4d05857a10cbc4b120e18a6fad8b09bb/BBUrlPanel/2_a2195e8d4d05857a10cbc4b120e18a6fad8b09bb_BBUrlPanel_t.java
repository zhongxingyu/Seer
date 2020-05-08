 package burrito.client.crud.widgets;
 
 import burrito.client.dto.LinkJavaScriptObject;
 import burrito.client.util.LinkJavaScriptObjectFactory;
 
 public class BBUrlPanel extends BBPopupPanel {
 	
 	private LinkedEntityWidgetPopup popup;
 
 	public BBUrlPanel(SelectableTextArea rawEditor) {
 		super("Länk", rawEditor);
 	}
 
 	@Override
 	protected void onShow() {
 		//we override show instead.
 	}
 	
 	@Override
 	public void show() {
 		popup = new LinkedEntityWidgetPopup(new LinkedEntitySaveHandler());
 		
 		popup.center();
 		popup.show();
 		popup.setLinkText(getSelectedText());
 	}
 	
 	/*
 	private boolean validate(String url) {
 		return url.matches("^(http(s?)://){1}.*$");
 	}
 	*/
 	
 	@Override
 	protected boolean onClose() {
 		return true;
 	}
 	
 	private class LinkedEntitySaveHandler implements LinkedEntityWidgetPopup.SaveHandler {
 		
 		@Override
 		public void saved(String json) {
 			
 			String selectedText = getSelectedText();
 			LinkJavaScriptObject link = LinkJavaScriptObjectFactory.fromJson(json);
 
 			String linkText = link.getLinkText();
 			String absoluteUrl = link.getAbsoluteUrl();
 
 			if (absoluteUrl != null) {
				if (!absoluteUrl.startsWith("http")) {
 					absoluteUrl = "http://" + absoluteUrl;
 				}
 				
 				if (linkText.isEmpty()) {
 					selectedText = "[url]" + absoluteUrl + "[/url]";
 				}
 				else {
 					selectedText = "[url=" + absoluteUrl + "]" + linkText + "[/url]";
 				}
 			}
 			else {
 				selectedText = "[linkable=" + link.getTypeClassName() + ":" + ((long) link.getTypeId()) + "]" + linkText + "[/linkable]";
 			}
 
 			setSelectedText(selectedText);
 		}
 	}
 
 
 }
