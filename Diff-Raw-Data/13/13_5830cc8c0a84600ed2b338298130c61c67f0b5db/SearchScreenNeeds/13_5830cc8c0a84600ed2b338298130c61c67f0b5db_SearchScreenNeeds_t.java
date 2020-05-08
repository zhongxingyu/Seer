 package filmeUtils.swing;
 
 import java.io.File;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import filmeUtils.Downloader;
 import filmeUtils.FilmeUtilsFolder;
 import filmeUtils.OutputListener;
 import filmeUtils.subtitleSites.LegendasTv;
 import filmeUtils.subtitleSites.NewSubtitleLinkFoundCallback;
 import filmeUtils.subtitleSites.SubtitleAndLink;
 import filmeUtils.subtitleSites.SubtitleLinkSearchCallback;
 
 
 public class SearchScreenNeeds {
 
 	private static final String LOW   = "Resolu\u00E7\u00E3o normal";
 	private static final String HIGH  = "Alta defini\u00E7\u00E3o";
 	private static final String ALL   = "Todas as resolu\u00E7\u00F5es";
 	private final LegendasTv legendasTv;
 	private final Downloader downloader;
 	private final MutableFilmeUtilsOptions filmeUtilsOptions = new MutableFilmeUtilsOptions();
 
 	public SearchScreenNeeds(final LegendasTv legendasTv, final Downloader downloader) {
 		this.legendasTv = legendasTv;
 		this.downloader = downloader;
 	}
 	
 	public void download(final String item, final DownloadCallback callback) {
 		new Thread(){
 			@Override
 			public void run() {
 				final AtomicBoolean torrentWasFound = new AtomicBoolean(false);
 				legendasTv.search(item, new SubtitleLinkSearchCallback() {
 					public boolean processAndReturnIfMatches(final SubtitleAndLink subAndLink) {
 						String name = subAndLink.name;
 						String link = subAndLink.link;
 						final boolean success = downloader.download(name, link,filmeUtilsOptions);
 						if(success){
 							torrentWasFound.set(true);
 						}
 						return success;
 					}
 				});
 				callback.done(torrentWasFound.get());
 			}
 		}.start();
 	}
 
 	public void setSubtitleFolder(final File folder) {
		FilmeUtilsFolder.getInstance().setSubtitleDestinationFolder(folder.getAbsolutePath());
 	}
 
 	public String getSubtitleFolder() {
		return FilmeUtilsFolder.getInstance().getSubtitlesDestination().getAbsolutePath();
 	}
 
 	public void getNewAddsList(final GUISearchCallback callback) {
 		
 		new Thread(){
 			@Override
 			public void run() {				
 				legendasTv.getNewer(50, new NewSubtitleLinkFoundCallback(){
 					public void processAndReturnIfMatches(final SubtitleAndLink subAndLink) {
 						String name = subAndLink.name;
 						callback.found(name);
 					}
 				});
 				callback.done();
 			};
 		}.start();
 	}
 
 	public void getResultsFor(final String text, final GUISearchCallback callback) {
 		
 		new Thread(){
 			@Override
 			public void run() {				
 				legendasTv.search(text, new SubtitleLinkSearchCallback(){
 					public boolean processAndReturnIfMatches(final SubtitleAndLink subAndLink) {
 						String name = subAndLink.name;
 						callback.found(name);
 						return false;
 					}
 					
 				});
 				callback.done();
 			}
 		}.start();
 	}
 
 	public void setResolution(final String resolution) {
 		if(resolution.equals(ALL)){
 			filmeUtilsOptions.setShouldRefuseHD(false);
 			filmeUtilsOptions.setShouldRefuseNonHD(false);
 		}
 		if(resolution.equals(HIGH)){
 			filmeUtilsOptions.setShouldRefuseHD(false);
 			filmeUtilsOptions.setShouldRefuseNonHD(true);
 		}
 		if(resolution.equals(LOW)){
 			filmeUtilsOptions.setShouldRefuseHD(true);
 			filmeUtilsOptions.setShouldRefuseNonHD(false);
 		}
 	}
 
 	public String allResolutionsString() {
 		return ALL;
 	}
 
 	public String highResolutionString() {
 		return HIGH;
 	}
 
 	public String lowResolutionString() {
 		return LOW;
 	}
 
 	public void setOutputListener(final OutputListener outputListener) {
 		downloader.setOutputListener(outputListener);
 	}
 
 }
