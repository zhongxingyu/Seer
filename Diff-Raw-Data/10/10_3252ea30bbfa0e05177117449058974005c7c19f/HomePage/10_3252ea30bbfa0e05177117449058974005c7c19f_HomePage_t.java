 package libreSubs.libreSubsSite;
 
 import libreSubs.libreSubsSite.download.DownloadSubtitle;
 import libreSubs.libreSubsSite.upload.UploadPage;
 import libreSubs.libreSubsSite.wicketComponents.DeployJava;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.libreSubsApplet.utils.SubtitleResourceResolver;
 
 public class HomePage extends BasePage {
 
 	public HomePage() {
 		setStatelessHint(true);
 		addSubtitleFinderApplet();
 //		add(new Label("siteBaseURL", WicketApplication.getBasePath()));
 	}
 
 	private void addSubtitleFinderApplet() {
 		if (WicketApplication.get().getConfigurationType().equals(
 				WicketApplication.DEVELOPMENT)) {
 			add(new Label("appletDiv", "APPLET GOES HERE"));
 			return;
 		}
 
 		final DeployJava div = new DeployJava("appletDiv");
 		div.setWidth(802);
		div.setHeight(331);
 		div.setCode("org.libreSubsApplet.MainApplet.class");
 		div.setCodebase(WicketApplication.getBasePath() + "applets");
 		div.setArchive("subFinder.jar");
 		final String idParam = SubtitleResourceResolver.idParameter;
 		final String langParam = SubtitleResourceResolver.langParameter;
 		div.addParameter("download", DownloadSubtitle
 				.getDownloadURLPath()
 				+ "?" + idParam + "=%id&" + langParam + "=%lang");
 		div.addParameter("upload", UploadPage.getUploadURLPath());
 		div.setMinimalVersion("1.6");
 		add(div);
 	}
 }
