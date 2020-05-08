 package be.butskri.automail;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 
 public class GezinParser {
 
 	private Iterator<String> linesIterator;
 
	@SuppressWarnings("unchecked")
 	public GezinParser(File file) {
 		try {
 			this.linesIterator = FileUtils.readLines(file, "UTF-8").iterator();
 		} catch (IOException exception) {
 			throw new RuntimeException();
 		}
 	}
 
 	public Gezin nextGezin() {
 		if (!linesIterator.hasNext()) {
 			return null;
 		}
 		Gezin result = Gezin.create(getGezinLines());
 		if (result == null) {
 			return nextGezin();
 		}
 		return result;
 	}
 
 	private List<String> getGezinLines() {
 		List<String> lines = new ArrayList<String>();
 		while (linesIterator.hasNext()) {
 			String line = linesIterator.next();
 			if (StringUtils.isBlank(line)) {
 				return lines;
 			}
 			lines.add(line);
 		}
 		return lines;
 	}
 }
