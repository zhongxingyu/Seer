 /**
  * 
  */
 package name.webdizz.clt.crx.client.translation.medeniye;
 
 import name.webdizz.clt.crx.client.translation.ITranslationHandler;
 import name.webdizz.clt.crx.client.translation.TranslationResult;
 
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 
 /**
  * Requests translations from medeniye.org dictionary and call
  * {@link ITranslationHandler} with translation result.
  * 
  * @author webdizz
  * 
  */
 public class MedeniyeRequestBuilder {
 
 	private class TranslationRequestCallback implements RequestCallback {
 		private ITranslationHandler handler;
 
 		private TranslationRequestCallback(ITranslationHandler handler) {
 			this.handler = handler;
 		}
 
 		public void onResponseReceived(final Request request, final Response response) {
 			String translationHtmlText = response.getText();
 			TranslationResult translationResult = new TranslationResultCreator().parse(translationHtmlText);
 			handler.onTranslate(translationResult);
 		}
 
 		public void onError(final Request request, final Throwable exception) {
 			// TODO: handle error [webdizz]
 		}
 
 	}
 
	private static final String URL = "http://qlugat.appspot.com/lugat/get";
 
 	private RequestBuilder requestBuilder;
 
 	/**
 	 * Creates default {@link RequestBuilder}.
 	 */
	protected void createDefaultRequestBuilder() {
		requestBuilder = new RequestBuilder(RequestBuilder.GET, URL);
 	}
 
 	/**
 	 * @param requestData
 	 * @param handler
 	 * @return ` * @throws RequestException
 	 */
 	public Request send(final String requestData, final ITranslationHandler handler) throws RequestException {
 		if (null == requestBuilder) {
			createDefaultRequestBuilder();
 		}
 		TranslationRequestCallback callback = new TranslationRequestCallback(handler);
 		requestBuilder.setCallback(callback);
 		return requestBuilder.send();
 	}
 
 	/**
 	 * @param requestBuilder
 	 *            the {@link RequestBuilder} to set
 	 */
 	public void setRequestBuilder(final RequestBuilder requestBuilder) {
 		this.requestBuilder = requestBuilder;
 	}
 
 }
