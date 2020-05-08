 package mdettlaff.comics.service;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.List;
 import java.util.regex.Matcher;
 
 import mdettlaff.comics.domain.Comic;
 import mdettlaff.comics.domain.ContentType;
 import mdettlaff.comics.domain.FileDownload;
 import mdettlaff.comics.domain.exception.ComicNotFoundException;
 import mdettlaff.comics.repository.ComicsRepository;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class ComicsService {
 
 	private static final ContentType DEFAULT_CONTENT_TYPE = ContentType.PNG;
 
 	private final ComicsRepository repository;
 	private final HttpServiceFactory httpServiceFactory;
 
 	@Autowired
 	public ComicsService(ComicsRepository repository, HttpServiceFactory httpServiceFactory) {
 		this.repository = repository;
 		this.httpServiceFactory = httpServiceFactory;
 	}
 
 	public void downloadComics() {
 		List<Comic> comics = repository.getAllKnownComics();
 		repository.clear();
 		for (Comic comic : comics) {
 			try {
 				FileDownload download = downloadComic(comic);
 				repository.addDownload(download);
 			} catch (Exception e) {
 				e.printStackTrace();
 				repository.logError(String.format("%s: %s: %s",
 						comic.getName(), e.getClass().getSimpleName(), e.getMessage()));
 			}
 		}
 	}
 
 	public List<FileDownload> getImages() {
 		return repository.getDownloads();
 	}
 
 	public List<String> getErrors() {
 		return repository.getErrors();
 	}
 
	private FileDownload downloadComic(Comic comic) throws IOException, ComicNotFoundException {
 		String imageUrl = getComicImageUrl(comic);
 		byte[] imageContent = httpServiceFactory.getInstance().download(imageUrl);
 		ContentType contentType = getContentTypeByUrl(imageUrl);
 		return new FileDownload(comic.getName(), imageContent, contentType);
 	}
 
	private String getComicImageUrl(Comic comic) throws IOException, ComicNotFoundException {
 		HttpService httpService = httpServiceFactory.getInstance();
 		BufferedReader webpageReader = httpService.read(comic.getUrl());
 		try {
 			String currentLine;
 			while ((currentLine = webpageReader.readLine()) != null) {
 				Matcher matcher = comic.getImageUrlPattern().matcher(currentLine);
 				if (matcher.find()) {
 					String comicImageUrl = matcher.group(1);
 					return getFullComicImageUrl(comicImageUrl, comic.getUrl());
 				}
 			}
 		} finally {
 			httpService.closeReader(webpageReader);
 		}
 		throw new ComicNotFoundException(comic.getUrl());
 	}
 
 	private String getFullComicImageUrl(String comicImageUrl, String comicUrl) {
 		if (comicImageUrl.startsWith("http://")) {
 			return comicImageUrl;
 		} else {
 			String urlHost = comicUrl.replaceFirst("(http://.*?)/.*", "$1");
 			if (comicImageUrl.startsWith("/")) {
 				return urlHost + comicImageUrl;
 			} else {
 				return urlHost + "/" + comicImageUrl;
 			}
 		}
 	}
 
 	private ContentType getContentTypeByUrl(String imageUrl) {
 		if (imageUrl.matches(".*\\....$")) {
 			String fileExtension = imageUrl.substring(imageUrl.length() - 3);
 			return ContentType.getByFileExtension(fileExtension);
 		}
 		return DEFAULT_CONTENT_TYPE;
 	}
 }
