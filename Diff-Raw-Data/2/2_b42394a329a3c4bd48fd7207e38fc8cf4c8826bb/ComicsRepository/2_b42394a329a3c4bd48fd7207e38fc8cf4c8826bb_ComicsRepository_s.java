 package mdettlaff.comics.repository;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import mdettlaff.comics.domain.Comic;
 import mdettlaff.comics.domain.FileDownload;
 
 import org.springframework.stereotype.Component;
 
 @Component
 public class ComicsRepository {
 
 	private static List<FileDownload> downloads = new ArrayList<FileDownload>();
 	private static List<String> errors = new ArrayList<String>();
 
 	public List<Comic> getAllKnownComics() {
 		List<Comic> comics = new ArrayList<Comic>();
 		comics.add(new Comic("Dilbert", "http://www.dilbert.com/fast/", "^<img src=\"(.*?)\" />"));
 		comics.add(new Comic("xkcd", "http://xkcd.com/", "embedding\\): (.*?)$"));
 		comics.add(new Comic("Not Invented Here", "http://notinventedhe.re/", "<img alt=\"Not Invented Here.*? src=\"(.*?)\""));
 		comics.add(new Comic("Sinfest", "http://sinfest.net/", "<img src=\"(.*?comikaze.*?)\""));
 		comics.add(new Comic("Freefall", "http://freefall.purrsia.com/", "<img src=\"(.*?)\""));
 		comics.add(new Comic("Questionable Content", "http://questionablecontent.net/", "<img id=\"strip\" src=\"(.*?)\">$"));
 		comics.add(new Comic("PHD Comics", "http://phdcomics.com/comics.php", "comic src=(.*?comics/archive/phd.*?) "));
 		comics.add(new Comic("Penny Arcade", "http://www.penny-arcade.com/comic/", "\\s+<img src=\"(http://art.*?)\""));
 		comics.add(new Comic("Cyanide & Happiness", "http://explosm.net/comics/", ".*<img.*? src=\"(.*?net/db/files/Comics.*?)\""));
 		comics.add(new Comic("SMBC", "http://www.smbc-comics.com/", "\\s+<img src=\'(.*?comic.*?)\'>\\s+$"));
 		comics.add(new Comic("SMBC bonus", "http://www.smbc-comics.com/", "<img src=\'(.*?after.*?)\'>$"));
 		comics.add(new Comic("Abstruse Goose", "http://abstrusegoose.com/", "<img.*? src=\"(.*?strips.*?)\""));
 		comics.add(new Comic("Dinosaur Comics", "http://qwantz.com/index.php", ".*<img src=\"(.*?)\" class=\"comic\""));
 		comics.add(new Comic("User Friendly", "http://userfriendly.org/", "<IMG ALT=\"Latest Strip\".*? SRC=\"(.*?)\""));
 		comics.add(new Comic("Cowbirds in Love", "http://cowbirdsinlove.com/", "<img src=\"(.*?)\".*id=\"comic\""));
		comics.add(new Comic("Chainsawsuit", "http://www.chainsawsuit.com", "<div id=\"comic.*?<img src=\"(.*?)\""));
 		comics.add(new Comic("Nedroid", "http://www.nedroid.com", "<img src=\"(.*?nedroid.com/comics.*?)\""));
 		comics.add(new Comic("FLOABC", "http://www.forlackofabettercomic.com", "<img src=\"(.*?img/comic.*?)\""));
 		comics.add(new Comic("Sequential Art", "http://www.collectedcurios.com/sequentialart.php", "<img id=\"strip\" src=\"(.*?)\""));
 		comics.add(new Comic("Liberty Meadows", "http://www.creators.com/today-comics.html", "liberty-meadows.*<img src=\"(.*?)\""));
 		comics.add(createGoComicsComic("Garfield", "garfield"));
 		comics.add(createGoComicsComic("Monty", "monty"));
 		comics.add(createGoComicsComic("Pearls Before Swine", "pearlsbeforeswine"));
 		comics.add(createGoComicsComic("Scary Gary", "scarygary"));
 		comics.add(createGoComicsComic("Meaning of Lila", "meaningoflila"));
 		comics.add(createGoComicsComic("On a Claire Day", "onaclaireday"));
 		comics.add(createGoComicsComic("Daddy's Home", "daddyshome"));
 		comics.add(createGoComicsComic("Cafe con Leche", "cafeconleche"));
 		comics.add(createGoComicsComic("Calvin and Hobbes", "calvinandhobbes"));
 		Collections.sort(comics);
 		return comics;
 	}
 
 	private Comic createGoComicsComic(String name, String urlPart) {
 		return new Comic(name, "http://gocomics.com/" + urlPart, "class=\"strip\".*?src=\"(.*?)\"");
 	}
 
 	public void clear() {
 		downloads.clear();
 		errors.clear();
 	}
 
 	public void addDownload(FileDownload download) {
 		downloads.add(download);
 	}
 
 	public List<FileDownload> getDownloads() {
 		return downloads;
 	}
 
 	public void logError(String error) {
 		errors.add(error);
 	}
 
 	public List<String> getErrors() {
 		return errors;
 	}
 }
