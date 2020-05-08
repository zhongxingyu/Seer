 package name.webdizz.clt.crx.contentscript.client.connection;
 
 import name.webdizz.clt.crx.client.event.message.ShowTranslatedTextMessage;
 import name.webdizz.clt.crx.client.translation.TranslationResultJs;
 import name.webdizz.clt.crx.contentscript.client.translation.view.TranslationView;
 
 import com.google.gwt.chrome.crx.client.events.Message;
 import com.google.gwt.chrome.crx.client.events.RequestEvent.Listener;
 import com.google.gwt.user.client.ui.PopupPanel;
 
 /**
  * Listener for request communications from extension or another content script.
  * 
  * @author webdizz
  * 
  */
 public final class ContentScriptRequestListener implements Listener {
 
 	private static class TranslationResultPanel extends PopupPanel {
 		public TranslationResultPanel(ShowTranslatedTextMessage message) {
			// enable auto hide
 			super(true);
 			TranslationResultJs translationResult = message.getTranslation();
 			if (null != translationResult) {
 				setAnimationEnabled(true);
 				setGlassEnabled(true);
 				setWidget(new TranslationView(translationResult).asWidget());
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.google.gwt.chrome.crx.client.events.RequestEvent.Listener#onRequest
 	 * (com.google.gwt.chrome.crx.client.events.Message)
 	 */
 	public void onRequest(final Message message) {
 		if (null != message) {
 			final name.webdizz.clt.crx.client.event.message.Message castedMessage = message.cast();
 			handleShowTranslatedTextMessage(castedMessage);
 		}
 	}
 
 	private void handleShowTranslatedTextMessage(final name.webdizz.clt.crx.client.event.message.Message castedMessage) {
 		if (ShowTranslatedTextMessage.TYPE.equals(castedMessage.getType())) {
 			final ShowTranslatedTextMessage translation = castedMessage.cast();
 			final TranslationResultPanel resultPanel = new TranslationResultPanel(translation);
 			resultPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
 				public void setPosition(int offsetWidth, int offsetHeight) {
 					int selectionOffsetX = translation.getMessage().getOffsetX();
 					int selectionOffsetY = translation.getMessage().getOffsetY();
					int offsetX = translation.getTranslation().getSrc().length()*5;
					resultPanel.setPopupPosition(selectionOffsetX + offsetX, selectionOffsetY - 15);
 				}
 			});
 		}
 	}
 }
