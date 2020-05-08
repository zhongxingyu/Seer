 package libreSubs.libreSubsSite.upload;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 import libreSubs.libreSubsSite.CommonsParameters;
 import libreSubs.libreSubsSite.TextPage;
 import libreSubs.libreSubsSite.WicketApplication;
 import libreSubs.libreSubsSite.text.TextWebResource;
 
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.RequestCycle;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.protocol.http.WebRequest;
 import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest;
 import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
 import org.apache.wicket.util.lang.Bytes;
 import org.apache.wicket.util.upload.FileItem;
 import org.libreSubsApplet.utils.LocaleUtil;
 import org.wicketstuff.annotation.mount.MountPath;
 
 @MountPath(path = "upload")
 public class UploadPage extends WebPage {
 
 	private static final Bytes MAX_SUB_SIZE = Bytes.kilobytes(200);
 	public static final String RESOURCE_NAME = "upload";
 
 	public UploadPage(final PageParameters pageParameters) {
 		if (pageParameters.size() <= 0) {
 			final MultipartServletWebRequest multipartWebRequest = (MultipartServletWebRequest) ((WebRequest) getRequest())
 					.newMultipartWebRequest(MAX_SUB_SIZE);
 
 			getRequestCycle().setRequest(multipartWebRequest);
 			@SuppressWarnings("unchecked")
 			final PageParameters postParameters = new PageParameters(
 					multipartWebRequest.getParameterMap());
 			
 			
 			final CommonsParameters parameters = new CommonsParameters(postParameters);
 			
 			if (!parameters.hasAllObrigatoryParameters()) {
 				redirectToMessage(parameters, "Os seguintes parâmetros devem ser informados: "
 								+ parameters.getLackingParametersNames());
 				return;
 			}
 
 			final String language = parameters.getLanguage();
 			if (!LocaleUtil.isValidLanguage(language)) {
 				redirectToMessage(parameters,"O idioma " + language
 						+ " não é suportado.");
 				return;
 			}
 
 			final Map<String, FileItem> files = multipartWebRequest.getFiles();
 			final FileItem fileParam = files.get("file");
 			if (fileParam == null || fileParam.getName().isEmpty()) {
 				redirectToMessage(parameters,"Arquivo precisa ser informado.");
 				return;
 			}
 
 			File tmpFile = null;
 			try {
 				tmpFile = File.createTempFile("libresub", ".tmp");
 			} catch (final IOException e) {
 				redirectToMessage(parameters,"Erro no servidor ao tentar criar arquivo temporário de legenda.");
 				return;
 			}
 
 			try {
 				fileParam.write(tmpFile);
 			} catch (final Exception e) {
 				redirectToMessage(parameters,"Erro no servidor ao tentar escrever em arquivo temporário de legenda.");
 				return;
 			}
 
 			try {
 				new SubtitleUploader().upload(parameters.getId(), parameters.getLanguage(), tmpFile);
 			} catch (final SubtitleUploadingException e) {
				redirectToMessage(parameters,e.getMessage());
 				return;
 			}
 			redirectToMessage(parameters,"Legenda enviada com sucesso.");
 		}
 	}
 
 	private void redirectToMessage(final CommonsParameters parameters, final String text) {
 		if(parameters.isCommandLine()){
 			final RequestCycle rc = RequestCycle.get();
 			final ResourceStreamRequestTarget requestTarget = new ResourceStreamRequestTarget(new TextWebResource(text).getResourceStream());
 			rc.setRequestTarget(requestTarget);
 		}else{
 			TextPage.redirectToPageWithText(text);
 		}
 	}
 
 	public static String getUploadURLPath() {
 		return WicketApplication.getBasePath() + RESOURCE_NAME;
 	}
 
 }
