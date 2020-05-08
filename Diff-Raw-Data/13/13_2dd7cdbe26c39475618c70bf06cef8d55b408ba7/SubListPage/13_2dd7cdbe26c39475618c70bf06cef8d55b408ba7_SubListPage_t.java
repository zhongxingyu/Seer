 package libreSubs.libreSubsSite;
 
 import org.apache.wicket.markup.html.basic.MultiLineLabel;
 import org.libreSubsEngine.subtitleRepository.repository.SubtitlesRepositoryHandler;
 import org.wicketstuff.annotation.mount.MountPath;
 
 @MountPath(path = "subList")
 public class SubListPage extends BasePage {
 
 	public SubListPage() {;
 		addSubtitlesListPrintForDebug();
 	}
 
 	private void addSubtitlesListPrintForDebug() {
 		final SubtitlesRepositoryHandler subtitlesRepositoryHandler = WicketApplication
 				.getSubtitlesRepositoryHandler();
		final String listSubtitles = subtitlesRepositoryHandler.listSubtitles();
		add(new MultiLineLabel("message", "SubCount: "+listSubtitles.length()+" SubList: \n"
				+ listSubtitles));
 	}
 }
