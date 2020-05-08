 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 
 import filmeUtils.Downloader;
 import filmeUtils.FilmeUtilsFolder;
 import filmeUtils.VerboseSysOut;
 import filmeUtils.extraction.ExtractorImpl;
 import filmeUtils.fileSystem.FileSystem;
 import filmeUtils.fileSystem.FileSystemImpl;
 import filmeUtils.http.MagnetLinkHandler;
 import filmeUtils.http.OSMagnetLinkHandler;
 import filmeUtils.http.SimpleHttpClient;
 import filmeUtils.http.SimpleHttpClientImpl;
 import filmeUtils.subtitleSites.LegendasTv;
 import filmeUtils.subtitleSites.NewSubtitleLinkFoundCallback;
 import filmeUtils.subtitleSites.SubtitleAndLink;
 import filmeUtils.torrentSites.TorrentSearcher;
 import filmeUtils.torrentSites.TorrentSearcherImpl;
 
 
 public class Daemon {
 
 	public static void main(String[] args) throws IOException {
 		new Daemon();
 	}
 	
 	public Daemon() throws IOException {
 		File filmeUtilsFolder = FilmeUtilsFolder.get();
 		File subtitlesToDownloadFile = new File(filmeUtilsFolder,"downloadThis");
 		if(!subtitlesToDownloadFile.exists()){
 			System.err.println("O arquivo "+subtitlesToDownloadFile.getAbsolutePath()+" tem que existir.");
 			System.err.println("Esse arquivo deve ter uma regex por linha.");
 			System.err.println("Quando uma nova legenda aparecer no legendas tv, o programa vai \n" +
 					"baixar a legenda em " +FilmeUtilsFolder.getSubtitlesDestinationOrNull()+" e adicionar o torrent no cliente registrado.");
 			throw new RuntimeException(subtitlesToDownloadFile.getAbsolutePath()+" not found.");
 		}
 		
		final File cookieFile = new File(FilmeUtilsFolder.get(),"cookies.serialized");
		final SimpleHttpClient httpclient = new SimpleHttpClientImpl(cookieFile);
		
 		final VerboseSysOut output = new VerboseSysOut();
		LegendasTv legendasTv = new LegendasTv(httpclient, output);
 		legendasTv.login();
 		
     	final ExtractorImpl extract = new ExtractorImpl();
     	
     	final MagnetLinkHandler magnetLinkHandler = new OSMagnetLinkHandler();
         final TorrentSearcher torrentSearcher = new TorrentSearcherImpl(httpclient);
 		final FileSystem fileSystem = new FileSystemImpl();
     	
 		final Downloader downloader = new Downloader(extract, fileSystem, httpclient, torrentSearcher, magnetLinkHandler, legendasTv, output);
 		@SuppressWarnings("unchecked")
 		final List<String> subsToDownload = FileUtils.readLines(subtitlesToDownloadFile);
 		final File fileContainingAlreadyDownloaded = new File(filmeUtilsFolder,"alreadyDownloaded");
 		if(!fileContainingAlreadyDownloaded.exists()){
 			fileContainingAlreadyDownloaded.createNewFile();
 		}
 		@SuppressWarnings("unchecked")
 		final List<String> alreadyDownloadedFiles = FileUtils.readLines(fileContainingAlreadyDownloaded);
 		
 		final ArrayList<String> alreadyChecked = new ArrayList<String>();
 		while(true){
			output.out("Procurando novas legendas.");
 			try {
 				int checkInterval = 60000 * 10;
 				legendasTv.getNewer(23*3, new NewSubtitleLinkFoundCallback() {
 					@Override
 					public void processAndReturnIfMatches(SubtitleAndLink subAndLink) {
 						String name = subAndLink.name;
 						String link = subAndLink.link;
 						if (!alreadyChecked.contains(name)) {
 							for (String pattern : subsToDownload) {
 								if (name.toLowerCase().matches(pattern) && !alreadyDownloadedFiles.contains(name)) {
 									output.out("Pattern matched: "+name);
 									boolean success = downloader.download(name, link);
 									if(success){
 										alreadyDownloadedFiles.add(name);						
 										try {
 											String filesAlreadyDownloaded = StringUtils.join(alreadyDownloadedFiles, '\n');
 											FileUtils.writeStringToFile(fileContainingAlreadyDownloaded, filesAlreadyDownloaded);
 										} catch (IOException e) {
 											e.printStackTrace();
 										}
 									}
 								}
 							}
 							alreadyChecked.add(name);
 						}
 					}
 				});
 				Thread.sleep(checkInterval);
 			} catch (Exception e) {
 				e.printStackTrace();// ignore and go on
 			}
 		}
 	}	
 }
