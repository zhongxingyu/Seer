 package libreSubs.libreSubsSite.upload;
 
 import java.io.File;
 import java.io.IOException;
 
 import libreSubs.libreSubsSite.WicketApplication;
 
 import org.apache.log4j.Logger;
 import org.libreSubsEngine.subtitleRepository.repository.SubtitlesRepositoryHandler;
 import org.subtitleDownloadLogic.utils.IOUtils;
 import org.subtitleDownloadLogic.utils.LocaleUtil;
 
 public class SubtitleUploader {
 
 	public void upload(final String id, final String lang,
 			final File subtitle) throws SubtitleUploadingException {
 		testArguments(id, lang, subtitle);
 
 		final SubtitlesRepositoryHandler subtitlesRepositoryHandler = WicketApplication
 				.getSubtitlesRepositoryHandler();
 
 		if (subtitlesRepositoryHandler.subtitleExists(id, lang)) {
 			throw new SubtitleUploadingException("Legenda já existe.");
 		}
 
		if (id.length()!=40) {
			throw new SubtitleUploadingException("Id deve ter 40 caracteres.");
		}
		
 		try {
 			subtitlesRepositoryHandler.addSubtitleFromFileAndDeleteIt(id, lang,
 					subtitle);
 		} catch (final IOException e) {
 			final String error = "Erro ao adicionar legenda a base";
 			Logger.getLogger(SubtitleUploader.class).error(
 					error, e);
 			throw new SubtitleUploadingException(
 					error);
 		}
 	}
 
 	private void testArguments(final String id, final String lang,
 			final File subtitle) throws SubtitleUploadingException {
 		if (id == null) {
 			throw new SubtitleUploadingException("SHA1 dos primeiros "
 					+ IOUtils.getPartialSHA1SizeAsHumanReadable()
 					+ " devem ser informados.");
 		}
 		if (!LocaleUtil.isValidLanguage(lang)) {
 			throw new SubtitleUploadingException("Idioma inválido.");
 		}
 		if (subtitle == null) {
 			throw new SubtitleUploadingException("Arquivo deve ser informado.");
 		}
 	}
 
 }
