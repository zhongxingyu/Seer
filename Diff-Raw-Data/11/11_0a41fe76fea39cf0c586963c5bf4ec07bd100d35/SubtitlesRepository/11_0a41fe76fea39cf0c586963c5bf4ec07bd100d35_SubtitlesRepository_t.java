 package org.libreSubsEngine.subtitleRepository.repository;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.libreSubsEngine.subtitleRepository.SubtitleRepositoryLocation;
 import org.libreSubsEngine.subtitleRepository.fileUtils.RepositoryScanner;
 import org.libreSubsEngine.subtitleRepository.fileUtils.RepositoryScannerListener;
 import org.libreSubsEngine.subtitleRepository.git.GitRepoHandler;
 import org.subtitleDownloadLogic.utils.LocaleUtil;
 
 public class SubtitlesRepository implements RepositoryScannerListener{
 
 	private final Map<SubtitleKey, Subtitle> subtitles;
 	private final SubtitleRepositoryLocation repositoryLocation;
 	private final GitRepoHandler gitRepoHandler;
 	
 	public SubtitlesRepository(final SubtitleRepositoryLocation repoLocation){		
 		this.repositoryLocation = repoLocation;
 		subtitles = new LinkedHashMap<SubtitleKey, Subtitle>();
 		final File repoDir = repoLocation.getBaseDir();
 		try {
 			RepositoryScanner.scan(this, repoDir);
 		} catch (final IOException e) {
 			throw new RuntimeException("Error loading reposiory",e);
 		}
 		gitRepoHandler = new GitRepoHandler(repoDir);
 	}
 
 	void addSubtitle(final PartialSHA1 videoID, final String language,final File content) throws IOException {
 		final String videoIDAsString = videoID.toString();
 		final String strDirName = videoIDAsString.substring(0, 2);
 		final File srtDir = new File(repositoryLocation.getBaseDir(), strDirName);
 		if(!srtDir.exists()){
 			srtDir.mkdir();
 		}
 		final String fileName = videoIDAsString + "." + language;
 		final File subtitleFile = new File(srtDir, fileName);
 		if(subtitleFile.exists())
 			throw new IOException("File already exists");
 		else
 			FileUtils.copyFile(content, subtitleFile);
 		
 		gitRepoHandler.addFile(subtitleFile);
 		loadSubtitleFromRepositoryDecomposeName(subtitleFile);
 	}
 
 	public String getSubtitleContentsFromVideoIDAndLanguageOrNull(final String language,
 			final PartialSHA1 videoID) throws IOException {
 		final SubtitleKey subtitleKey = new SubtitleKey(language, videoID);
 		return getSubtitleContentsForKeyOrNull(subtitleKey);
 	}
 
 	public String getSubtitleContentsForKeyOrNull(final SubtitleKey subtitleKey){
 		if(subtitleKey == null)
 			return null;
 		final Subtitle subtitle = subtitles.get(subtitleKey);
 		if (subtitle == null)
 			return null;
 		final String content = subtitle.getContent();
 		return content;
 	}
 
 	public void changeContentsForSubtitle(final String newContent, final String language,final PartialSHA1 videoID) throws IOException {
 		changeContentsForSubtitle(newContent, new SubtitleKey(language, videoID) );
 	}
 
 	public void changeContentsForSubtitle( final String newContent, final SubtitleKey subtitleKey) throws IOException {
 		final String empty = "";
 		changeContentsForSubtitle(empty,empty,empty, newContent,subtitleKey);
 	}
 
 	public void changeContentsForSubtitle(final String commiter,final String email,final String message, final String newContent, final SubtitleKey subtitleKey) throws IOException {
 		final Subtitle subtitle = subtitles.get(subtitleKey);
 		final String language = subtitleKey.getLanguage();
 		subtitle.setContent(newContent,language);
 		commitAs(commiter,email,message);
 	}
 
 	public String listSubtitles() {
 		final StringBuilder subtitlesList = new StringBuilder();
 		final Set<SubtitleKey> keySet = subtitles.keySet();
 		for (final SubtitleKey subtitleKey : keySet) {
 			final Subtitle subtitle = subtitles.get(subtitleKey);
 			final File strFileOrNull = subtitle.getStrFileOrNull();
 			final String subKeyString = subtitleKey.toString();
 			if(strFileOrNull == null){
 				subtitlesList.append(subKeyString+" sem arquivo ?????????\n");
 			}else{
 				final long subLength = strFileOrNull.length();
 				final String formattedSubDescription = String.format("%s tamanho: %10d bytes\n", subKeyString, subLength);
 				subtitlesList.append(formattedSubDescription);
 			}
 		}
 		return subtitlesList.toString();
 	}
 
 	public File getBaseDir() {
 		return repositoryLocation.getBaseDir();
 	}
 
 	public boolean subtitleExists(final SubtitleKey key){
 		if(key == null)
 			return false;
 		return subtitles.containsKey(key);
 	}
 
 	public void commitAs(final String commiter,final String email, final String message) {
 		String commiterName = commiter;
 		if(commiterName == null || commiterName.isEmpty()){
 			commiterName = GitRepoHandler.DEFAULT_NAME; 
 		}
 		final String commiterEmail = email;
 		if(commiterEmail == null || commiterEmail.isEmpty()){
 			commiterName = GitRepoHandler.DEFAULT_EMAIL; 
 		}
 		String commiterMessage = message;
 		if(commiterMessage == null || commiterMessage.isEmpty()){
 			commiterMessage = GitRepoHandler.DEFAULT_COMMIT_MESSAGE; 
 		}
		gitRepoHandler.commitWith(commiterName, commiterEmail, commiterMessage);
 	}
 
 	@Override
 	public void addFile(final File child) {
 		try {
 			loadSubtitleFromRepositoryDecomposeName(child);
 		} catch (final IOException e) {
 			throw new RuntimeException("Error opening file from reposiory",e);
 		}
 	}
 
 	public String getLastNCommits(final int i) {
 		return gitRepoHandler.getLog(i);
 	}
 
 	public void addSubtitle(final String id, final String language, final File srtFile) throws IOException {
 		final File strPathOnRepo = getFileFor(id, language);
 		srtFile.renameTo(strPathOnRepo);
 		loadFileToRepo(strPathOnRepo);
 	}
 
 	private void loadFileToRepo(final File subtitleFile) throws IOException {
 		gitRepoHandler.addFile(subtitleFile);
 		loadSubtitleFromRepositoryDecomposeName(subtitleFile);
 	}
 
 	private File getFileFor(final String id, final String language) {
 		final String strDirName = id.substring(0, 2);
 		final File baseDir = repositoryLocation.getBaseDir();
 		final File srtDir = new File(baseDir, strDirName);
 		if(!srtDir.exists()){
 			srtDir.mkdir();
 		}
 		final String fileName = getFilenameForSubtitle(id,
 				language);
 		final File subtitleFile = new File(srtDir, fileName);
 		return subtitleFile;
 	}
 
 	private String getFilenameForSubtitle(final String id, final String language) {
 		final String fileName = id + "." + language;
 		return fileName;
 	}
 	
 	private void loadSubtitleFromRepositoryDecomposeName(final File srtFile) throws IOException {
 		final String fileName = srtFile.getName();
 		final String videoID = StringUtils.substringBeforeLast(fileName, ".");
 		final String language = StringUtils.substringAfterLast(fileName, ".");
 		final PartialSHA1 videoSHA1 = new PartialSHA1(videoID);
 		loadSubtitleFromRepository(videoSHA1, language, srtFile);
 	}
 
 	private void loadSubtitleFromRepository(final PartialSHA1 videoSHA1,final String language,final File srtFile) throws IOException {
 		final String encodingForLanguage = LocaleUtil.getEncodingForLanguage(language);
 		final String srtFileContent = FileUtils.readFileToString(srtFile,encodingForLanguage);
 		final SubtitleKey subtitleKey = new SubtitleKey(language, videoSHA1);
 		final Subtitle subtitle = new Subtitle(srtFileContent, srtFile);
 		subtitles.put(subtitleKey, subtitle);
 	}
 
 	public int subtitlesQuantity() {
 		return subtitles.size();
 	}
 
 	public long subtitlesRepoSize() {
 		final File baseDir = repositoryLocation.getBaseDir();
 		return FileUtils.sizeOfDirectory(baseDir);
 	}
 
 }
