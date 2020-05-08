 package org.libreSubsApplet;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.List;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.libreSubsApplet.dropFile.DropFileListener;
 import org.libreSubsCommons.FileUtils;
 import org.libreSubsCommons.Language;
 import org.libreSubsCommons.SHA1Utils;
 import org.libreSubsCommons.SubtitleResourceResolver;
 
 public class DroppedFilesProcessor implements DropFileListener {
 
 	private final SubtitleResourceResolver subtitleSource;
 	private final OutputListener outputListener;
 
 	public DroppedFilesProcessor(final SubtitleResourceResolver subtitleSource,
 			final OutputListener outputListener) {
 		this.subtitleSource = subtitleSource;
 		this.outputListener = outputListener;
 	}
 
 	@Override
 	public void droppedFiles(final List<File> files) {
 
 		final ActionForDroppedFilesResolver actionForDroppedFiles = new ActionForDroppedFilesResolver(
 				files, outputListener);
 
 		tryToDownloadSubtitlesForVideos(actionForDroppedFiles
 				.getVideosToDownloadSubtitles());
 		uploadFiles(actionForDroppedFiles.getFilesToUpload());
 	}
 
 	private void uploadFiles(final List<VideoWithSubtitle> filesToUpload) {
 		for (final VideoWithSubtitle videoWithSubtitle : filesToUpload) {
			outputListener.error("Upload ainda não foi implementado "
					+ videoWithSubtitle);
 		}
 	}
 
 	private void tryToDownloadSubtitlesForVideos(
 			final List<File> videosAskingSubtitles) {
 		for (final File video : videosAskingSubtitles) {
 			tryToDownloadSubtitleForVideo(video);
 		}
 	}
 
 	private void tryToDownloadSubtitleForVideo(final File video) {
 		outputListener.info("Baixando legenda para " + video.getName());
 		final String shaHex;
 		
 		try {
 			shaHex = SHA1Utils.getPartialSHA1ForFile(video);
 		} catch (final IOException e1) {
 			outputListener.error("Erro calculando SHA1; " + e1.getMessage());
 			return;
 		}
 
 		final String fileName = video.getName();
 		final String parent = video.getParent();
 		final String newStrFileName = FilenameUtils.removeExtension(fileName)
 				+ ".srt";
 		outputListener.info("Arquivo: " + fileName + " - SHA1: " + shaHex);
 
 		final DownloadedContent downloadedContent;
 
 		try {
 			downloadedContent = downloadAdressForString(subtitleSource.resolve(
 					shaHex, Language.pt_BR, newStrFileName));
 			if (downloadedContent.isError()) {
 				outputListener
 						.error("Ocorreu um erro ao tentar baixar a legenda: "
 								+ downloadedContent.getContent());
 				return;
 			}
 		} catch (final Exception e) {
 			outputListener.error("Ocorreu um erro ao tentar baixar a legenda: "
 					+ e.getMessage());
 			return;
 		}
 
 		final File srtFile = FileUtils.createFileOrCry(parent, newStrFileName);
 		try {
 			org.apache.commons.io.FileUtils.writeStringToFile(srtFile,
 					downloadedContent.getContent());
 			outputListener.info("Legenda salva com sucesso");
 		} catch (final IOException e) {
 			srtFile.delete();
 			outputListener
 					.error("Ocorreu um erro ao tentar escreve arquivo de legenda: "
 							+ e.getMessage());
 		}
 	}
 
 	private DownloadedContent downloadAdressForString(final String address)
 			throws IOException {
 		final URL url = new URL(address);
 		final URLConnection conn = url.openConnection();
 		final String contentType = conn.getContentType();
 		final String content = IOUtils.toString(conn.getInputStream());
 		return new DownloadedContent(content, contentType);
 	}
 
 }
