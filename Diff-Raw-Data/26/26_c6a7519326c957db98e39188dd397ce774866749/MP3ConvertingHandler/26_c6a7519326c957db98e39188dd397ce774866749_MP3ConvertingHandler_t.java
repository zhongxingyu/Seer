 package lotia.av.dirclone;
 
 import java.nio.file.Path;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.Arrays;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import lotia.av.metadata.ffmpeg.FFMPEGAudioTranscoder;
 import lotia.av.metadata.ffmpeg.FFMPEGMetadata;
 import lotia.av.metadata.ffmpeg.FFMPEGMetadataCache;
 import lotia.av.metadata.ffmpeg.Stream;
 
 public class MP3ConvertingHandler implements FileHandler {
 	
 	private String[] m_arrSupportedExtensions;
 	private String[] m_arrSupportedCodecs;
 	private long[] m_arrSupportedCodecTags;
 	private CloneProgressReporter m_progress;
 	
 	static private final Pattern s_reFileExtension = Pattern.compile(".+\\.([^\\.]+?)");
 	
 	public MP3ConvertingHandler(String[] exts, String[] codecs, long[] codec_tags, CloneProgressReporter progress) {
 		m_arrSupportedExtensions = Arrays.copyOf(exts, exts.length);
 		Arrays.sort(m_arrSupportedExtensions);
 		m_arrSupportedCodecs = new String[codecs.length];
 		for (int i = 0; i != codecs.length; ++i) {
 			m_arrSupportedCodecs[i] = codecs[i].toLowerCase();
 		}
 		Arrays.sort(m_arrSupportedCodecs);
 		m_arrSupportedCodecTags = Arrays.copyOf(codec_tags, codec_tags.length);
 		Arrays.sort(m_arrSupportedCodecTags);
 		m_progress = progress;
 	}
 	
 	private boolean extensionHandled(String sExt) {
 		boolean bFoundInArray = (sExt != null) && (Arrays.binarySearch(m_arrSupportedExtensions, sExt.toLowerCase()) >= 0);
 		return bFoundInArray;
 	}
 	
 	private boolean codecHandled(String sCodec, long nCodecTag) {
 		boolean bFoundInCodecArray = (sCodec != null) && (Arrays.binarySearch(m_arrSupportedCodecs, sCodec) >= 0);
 		boolean bFoundInCodecTagArray = false;
 		if (!bFoundInCodecArray) {
 			bFoundInCodecTagArray = Arrays.binarySearch(m_arrSupportedCodecTags, nCodecTag) >= 0;
 		}
 		return bFoundInCodecArray || bFoundInCodecTagArray;
 	}
 	
 	private String[] splitPath(Path file) {
 		String[] split = new String[3];
 		
 		Path parent = file.getParent();
 		split[0] = parent == null ? null : parent.toString();
 		
 		Path name = file.getFileName();
 		if (name != null) {
 			String sName = name.toString();
 			Matcher m = s_reFileExtension.matcher(sName);
 			if (m.matches()) {
 				int extStart = m.start(1);
 				if (extStart >= 0) {
 					// "extStart - 1" is safe because of the way in which the pattern is defined
 					split[1] = sName.substring(0, extStart - 1);
 					split[2] = sName.substring(extStart);
 				}
 			}
 			if (split[1] == null) {
 				split[1] = sName;
 			}
 		}
 		
 		return split;
 	}
 
 	@Override
 	public Path canHandleFile(Path srcFile, BasicFileAttributes srcAttrs, Path destCandidate) throws FileCloningError {
 		return processFileImpl(srcFile, srcAttrs, destCandidate, RunType.DRY_RUN, false); 
 	}
 	
 	@Override
 	public Path processFile(Path srcFile, BasicFileAttributes srcAttrs, Path destCandidate, RunType runType) throws FileCloningError {
 		return processFileImpl(srcFile, srcAttrs, destCandidate, runType, true);
 	}
 
 	private Path processFileImpl(Path srcFile, BasicFileAttributes srcAttrs, Path destCandidate, RunType runType, boolean bReportDryRuns) throws FileCloningError {
 		String[] splitPath = splitPath(srcFile);
 		
		if (!extensionHandled(splitPath[2]))
			return null;
		
 		String sNewName = splitPath[1] + ".mp3";
 		
 		Path newPath = destCandidate.resolveSibling(sNewName);
 		
 		FFMPEGMetadata metadata;
 		try {
 			metadata = FFMPEGMetadataCache.getInstance().getMetadataForFile(srcFile,  srcAttrs);
 		} catch (Exception e) {
 			throw new FileCloningError(srcFile, newPath, e);
 		}
 		
 		// should have only one stream
 		if (metadata.getNumStreams() != 1)
 			return null;
 		
 		Stream stream = metadata.getStream(0);
 		
 		// stream should be an audio stream
 		if (stream.getCodecType() != Stream.Type.TYPE_AUDIO)
 			return null;
 		
 		// codec type should be supported
 		if (!codecHandled(stream.getCodecTagString(), stream.getCodecTag()))
 			return null;
 		
 		// right then ...
 		
 		if (runType == RunType.THE_REAL_DEAL) {
 			try {
 				FFMPEGAudioTranscoder.transcodeToMP3(srcFile, newPath);
 				m_progress.reportProgress(CloneProgressReporter.EventType.FILE_TRANSCODE, srcFile, newPath, null);
 			} catch (Exception e) {
 				throw new FileCloningError(srcFile, newPath, e);
 			}
 		} else if (bReportDryRuns) {
 			m_progress.reportProgress(CloneProgressReporter.EventType.FILE_TRANSCODE, srcFile, newPath, null);
 		}
 
 		return newPath;
 	}
 
 }
