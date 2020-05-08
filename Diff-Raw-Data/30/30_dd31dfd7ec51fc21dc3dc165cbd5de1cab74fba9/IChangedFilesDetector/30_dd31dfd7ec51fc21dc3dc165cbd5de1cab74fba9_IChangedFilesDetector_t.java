 package jp.ac.osaka_u.ist.sdl.ectec.analyzer.vcs;
 
 import java.util.Map;
 
import jp.ac.osaka_u.ist.sdl.ectec.data.Commit;
 import jp.ac.osaka_u.ist.sdl.ectec.settings.Language;
 
 /**
  * An interface for detecting added/deleted/changed files
  * 
  * @author k-hotta
  * 
  */
 public interface IChangedFilesDetector {
 
 	/**
 	 * detects added/deleted/changed files
 	 * 
 	 * @param revisionIdentifier
 	 * @param language
 	 * @return
 	 * @throws Exception
 	 */
 	public Map<String, Character> detectChangedFiles(
			final Commit commit, final Language language)
 			throws Exception;
 
 }
