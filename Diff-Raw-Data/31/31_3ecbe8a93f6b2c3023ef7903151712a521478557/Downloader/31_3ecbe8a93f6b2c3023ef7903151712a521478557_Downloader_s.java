 package filmeUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.zip.ZipException;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.http.client.ClientProtocolException;
 
 import filmeUtils.extraction.Extractor;
 import filmeUtils.fileSystem.FileSystem;
 import filmeUtils.http.MagnetLinkHandler;
 import filmeUtils.http.SimpleHttpClient;
 import filmeUtils.subtitleSites.LegendasTv;
 import filmeUtils.torrentSites.SiteOfflineException;
 import filmeUtils.torrentSites.TorrentSearcher;
 
 public class Downloader {
 	
 	private static final String ZIP = "zip";
 	private static final String RAR = "rar";
 	private final Extractor extract;
 	private final SimpleHttpClient httpclient;
 	private final TorrentSearcher torrentSearcher;
 	private final LegendasTv legendasTv;
 	private final MagnetLinkHandler magnetLinkHandler;
 	private final FileSystem fileSystem;
 	private OutputListener outputListener;
 	private boolean isLazy = false;
 	private boolean shouldRefuseHD = false;
 	private File subtitlesDestinationFolder = FilmeUtilsFolder.getSubtitlesDestinationOrNull();
 	private boolean shouldRefuseNonHD = false;
 	
 	public Downloader(final Extractor extract,final FileSystem fileSystem,final SimpleHttpClient httpclient,final TorrentSearcher torrentSearcher,final MagnetLinkHandler magnetLinkHandler,final LegendasTv legendasTv, final OutputListener outputListener) {
 		this.fileSystem = fileSystem;
 		this.httpclient = httpclient;
 		this.torrentSearcher = torrentSearcher;
 		this.magnetLinkHandler = magnetLinkHandler;
 		this.legendasTv = legendasTv;
 		this.extract = extract;
 		this.setOutputListener(outputListener);
 	}
 
 	public boolean download(final String name,final String link){
 		return unzipSearchMagnetsAndReturnSuccess(name,link);
 	}
 	
 	public void setOptions(final FilmeUtilsOptions options) {
 		isLazy = options.isLazy();
 		shouldRefuseHD = options.shouldRefuseHD();
 		subtitlesDestinationFolder = options.getSubtitlesDestinationFolderOrNull();
 		shouldRefuseNonHD = options.shouldRefuseNonHD();
 	}
 	
 	private boolean unzipSearchMagnetsAndReturnSuccess(final String name,final String link){
 		final String validNameForFile = name.replaceAll("[/ \\\\?]", "_");
 		final String tempDir = System.getProperty("java.io.tmpdir");
 		final File currentSubtitleFolder = new File(tempDir,validNameForFile);
 		fileSystem.mkdir(currentSubtitleFolder);
 		getOutputListener().outVerbose("Extraindo legendas para "+currentSubtitleFolder.getAbsolutePath());
 		try {
 			downloadLinkToFolder(link, currentSubtitleFolder);
 		} catch (Exception e) {
 			getOutputListener().outVerbose("Erro fazendo download de legenda de "+link);
 			File errorFile = writeErrorFileOrCry(e);
 			getOutputListener().outVerbose("Mais detalhes em "+errorFile);
 		}			
 		getOutputListener().outVerbose("Extraido com sucesso para "+currentSubtitleFolder.getAbsolutePath());
 		final boolean success = openTorrentsAndReturnSuccess(currentSubtitleFolder);
 		try {
 			FileUtils.deleteDirectory(currentSubtitleFolder);
 		} catch (IOException e) {
 			//don't care
 		}
 		return success;
 	}
 
 	private File writeErrorFileOrCry(Exception e) {
 		File errorFile = new File(FilmeUtilsFolder.get(), Calendar.getInstance().getTimeInMillis()+".error");
 		try {
 			FileUtils.writeStringToFile(errorFile, e.getMessage()+"\n"+e.getStackTrace());
 		} catch (IOException e1) {
 			throw new RuntimeException(e1);
 		}
 		return errorFile;
 	}
 
 	private void downloadLinkToFolder(final String link, final File folder) throws IOException, ClientProtocolException, ZipException {
 		String contentType;
 		final File destFile = File.createTempFile("filmeUtils", "filmeUtils");
 		
 		contentType = httpclient.getToFile(link, destFile);
 		
 		if(isNotAFile(contentType)){
 			legendasTv.login(); 
 			destFile.delete();
 			contentType = httpclient.getToFile(link, destFile);
 		}
 		extract(destFile, folder, contentType);
 		destFile.delete();
 	}
 
 	private boolean isNotAFile(String contentType) {
 		return contentType.contains("text/html");
 	}
 
 	private boolean openTorrentsAndReturnSuccess(final File currentSubtitleFolder) {
 		boolean successfull = false;
 		@SuppressWarnings("unchecked")
 		final Iterator<File> iterateFiles = FileUtils.iterateFiles(currentSubtitleFolder, new String[]{"srt"}, true);
 		while(iterateFiles.hasNext()){
 			final File next = iterateFiles.next();
 			final boolean success = downloadTorrentAndCopySubtitle(next);
 			if(isLazy && success){
 				return true;
 			}
 			successfull = successfull || success;
 		}
 		return successfull;
 	}
 	
 	private void extract(final File compressedFile,final File destinationFolder, final String contentType)throws ZipException, IOException {
 		if(contentType.contains(RAR)){
 			extract.unrar(compressedFile, destinationFolder);
 			return;
 		}
 		if(contentType.contains(ZIP)){
 			extract.unzip(compressedFile, destinationFolder);
 			return;
 		}
 		throw new RuntimeException("Tipo desconhecido: "+contentType);
 	}
 
 	private boolean downloadTorrentAndCopySubtitle(final File next) {
 		String magnetLinkForFile;
 		final String subtitleName = next.getName().replaceAll("\\.[Ss][Rr][Tt]", "");
 		final boolean shouldRefuse = shouldRefuseSubtitleFile(subtitleName);
 		if(shouldRefuse){
 			return false;
 		}
		try {
			getOutputListener().outVerbose("Procurando melhor torrent para "+subtitleName);
			magnetLinkForFile = torrentSearcher.getMagnetLinkForTermOrNull(subtitleName,getOutputListener());
		} catch (SiteOfflineException e1) {
			getOutputListener().out("Erro procurando torrent para "+next.getName()+" : "+e1.getMessage());
			return false;
		}
 		if(magnetLinkForFile == null){
 			getOutputListener().outVerbose("Nenhum torrent para "+subtitleName);
 			return false;
 		}
 		getOutputListener().outVerbose("Abrindo magnet link no cliente: "+magnetLinkForFile);
 		magnetLinkHandler.openURL(magnetLinkForFile);
 		getOutputListener().out("Magnet link '"+magnetLinkForFile+"' de "+subtitleName+" enviado ao client de torrent.");
 		try {
 			if(subtitlesDestinationFolder != null){				
 				FileUtils.copyFileToDirectory(next, subtitlesDestinationFolder);
 			}
 		} catch (final IOException e) {
 			throw new RuntimeException(e);
 		}
 		return true;
 	}
 	
 	private boolean shouldRefuseSubtitleFile(final String subtitleName) {
 		boolean shouldRefuse = false;
 		final boolean isHiDef = subtitleName.contains("720") || subtitleName.contains("1080");
 		if(shouldRefuseHD ){
 			if(isHiDef){
 				shouldRefuse = true;
 			}
 		}
 		if(shouldRefuseNonHD ){
 			if(!isHiDef){
 				shouldRefuse = true;
 			}
 		}
 		return shouldRefuse;
 	}
 
 	public OutputListener getOutputListener() {
 		return outputListener;
 	}
 
 	public void setOutputListener(final OutputListener outputListener) {
 		this.outputListener = outputListener;
 	}
 }
