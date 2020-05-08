 package lucene.demo.business;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class RawDocumentDatabase {
 
 	private static List<RawDocument> sRawDocuments;
 
 	/**
 	 * 
 	 * @return All RawDocument in a List<RawDocument>
 	 */
 	public static List<RawDocument> getRawDocuments() {
 		if (sRawDocuments == null) {
 			File dir = new File("data_project1");
 			File[] files = dir.listFiles();
 			for (File file : files) {
 				String content = getFileContent(file);
 				String fileName = file.getName();
 				String Id = fileName.split("\\.")[0];
 				String[] paragraphs = content.split("\n\n");	
 				int titleIndex = 1;
 				int CACMIndex = 0;
 				int CAIndex = 0;
 				Pattern CACMPattern = Pattern.compile("(CACM)");
 				Pattern CAPattern = Pattern.compile("(CA\\d+)");
 				
 				for (int i=0; i<paragraphs.length; i++) {
 					paragraphs[i] = paragraphs[i].trim();
 					
 					Matcher CACMMatcher = CACMPattern.matcher(paragraphs[i]);
 					Matcher CAMatcher = CAPattern.matcher(paragraphs[i]);
 					if (CACMMatcher.find()){
 						CACMIndex = i;
 					} else if (CAMatcher.find()){
 						CAIndex = i;
 					}
 				}
 				
 				RawDocument doc = new RawDocument();
 				doc.setId(Id);
 				doc.setTitle(paragraphs[titleIndex]);
 				
 				if (CACMIndex - titleIndex > 1) {
 					// If text exists
 					doc.setText(paragraphs[CACMIndex-1]);
 				} else {
 					doc.setText("");
 				}
 				
 				if (CAIndex - CACMIndex > 1) {
 					// If author exists
 					doc.setAuthor(paragraphs[CAIndex-1]);
 				} else {
 					doc.setAuthor("");
 				}
 				
 				doc.setOthers(paragraphs[CACMIndex] + " " +
 						paragraphs[CAIndex]);
 				
 				sRawDocuments.add(doc);
 			}
 		}
 		
 		return sRawDocuments;
 	}
 
 	/**
 	 * 
 	 * @param id
 	 *            the String that represents the raw docuemnt
 	 * @return RawDocument with id, or null
 	 */
 
 	public static RawDocument getRawDocumentById(String id) {
 		RawDocument result = null;
 
 		for (RawDocument rd : sRawDocuments) {
 			if (rd.getId().equals(id)) {
 				result = rd;
 				break;
 			}
 		}
 
 		return result;
 	}
 
 	private static String getFileContent(File file) {
 		String content = null;
 
 		try {
 			FileReader reader = new FileReader(file);
 			char[] chars = new char[(int) file.length()];
 			reader.read(chars);
 			content = new String(chars);
 			reader.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return content;
 	}
 
 }
