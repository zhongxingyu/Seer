 package org.libreSubsApplet;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.libreSubsApplet.utils.Downloader;
 import org.libreSubsApplet.utils.IOUtils;
 import org.libreSubsApplet.utils.Uploader;
 
 public class ActionForDroppedFilesResolver {
 
 	private static final String SUBTITLE_EXTENSION = "srt";
 	private final static String[] VIDEO_EXTENSIONS = "mpeg,mpg,avi,mov,wmv,rm,rmvb,mp4,3gp,ogm,ogg,mkv,asf".split(",");
 	private final Downloader downloader;
 	private final Uploader uploader;
 	
 	public ActionForDroppedFilesResolver(final List<File> droppedList,final Downloader downloader, final Uploader uploader,final OutputListener outputListener) {
 		this.downloader = downloader;
 		this.uploader = uploader;
 		final List<File> videoFiles = new ArrayList<File>();
 		final List<File> subtitlesFiles = new ArrayList<File>();
 		new Thread(){
 			@Override
 			public void run() {
 				outputListener.info("Processando "+droppedList);
 				for (final File fileOrDir : droppedList) {
 					sortFiles(outputListener, videoFiles, subtitlesFiles, fileOrDir);
 				}
 				for (final File videoFile : videoFiles) {
 					downloadAndUploadSubtitles(outputListener, subtitlesFiles, videoFile);
 				}
 				outputListener.info("Terminado");
 			};
 		}.start();
 	}
 
 	private void downloadAndUploadSubtitles(final OutputListener outputListener, final List<File> subtitlesFiles,
 			final File videoFile) {
 		final File sub = getSubtitleForVideoOnSubtitleListOrNull(videoFile, subtitlesFiles);
 		if(sub == null){
 			downloader.download(outputListener, videoFile);
 		}else{
 			uploader.upload(outputListener,new VideoWithSubtitle(videoFile, sub));
 		}
 	}
 
 	private void sortFiles(final OutputListener outputListener,
 			final List<File> videoFiles, final List<File> subtitlesFiles,
 			final File fileOrDir) {
 		if(fileOrDir.isDirectory()){
 			final File[] filesInDir = fileOrDir.listFiles();
 			for (final File file : filesInDir) {
 				sortFiles(outputListener,videoFiles,subtitlesFiles,file);
 			}
 			return;
 		}
 		final File file = fileOrDir;
 		final String extension = IOUtils.getExtension(file.getName()).toLowerCase();
 		if(extension.equals(SUBTITLE_EXTENSION)){
 			subtitlesFiles.add(file);
 		}else {
			for (final String allowedVideoExtension : VIDEO_EXTENSIONS) {
				if(extension.equals(allowedVideoExtension)){					
					videoFiles.add(file);
				}
			}
 		}
 	}
 
 	private File getSubtitleForVideoOnSubtitleListOrNull(final File videoFile,
 			final List<File> subtitlesFiles) {
 		final String videoName = IOUtils.getBaseName(videoFile.getAbsolutePath()).toLowerCase();
 		for (final File sub : subtitlesFiles) {
 			final String subName = IOUtils.getBaseName(sub.getAbsolutePath()).toLowerCase();
 			if(videoName.equals(subName)){
 				return sub;
 			}
 		}
 		return null;
 	}
 }
