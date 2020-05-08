 import handbook.Chapter;
 import handbook.HandbookParser;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.StringField;
 import org.apache.lucene.document.TextField;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.RAMDirectory;
 import org.apache.lucene.util.Version;
 
 import owl.OntologyClassificator;
 import patientCase.PatientCase;
 import patientCase.PatientCaseParser;
 
 public class Program {
 	// private String fileName = "atc.owl";
 	private static int numPatientCases = 8;
 
 	private static void preprocess() {
 		new HandbookParser().createJSONFile();
 	}
 
 	private static void task1b() {
 		OntologyClassificator oc = new OntologyClassificator("owlFiles/",
 				"icd10no.owl");
 		ArrayList<Document> hits;
 		HandbookParser hp = new HandbookParser();
 
 		// gets main chapters
 		ArrayList<Chapter> allMainChapters = hp.getMainChapters();
 		// gets all chapters incl. subchapters
 		ArrayList<Chapter> allChapters = new ArrayList<Chapter>();
 		for (Chapter chapter : allMainChapters) {
 			allChapters.addAll(chapter.getAllChapters());
 		}
 
 		// find and add icd codes to each chapter
 		ArrayList<String> icdCodes;
 		for (Chapter chapter : allChapters) {
 			icdCodes = new ArrayList<String>();
 			for (String line : chapter.getTextLines()) {
 				hits = oc.search(line);
 				for (Document hit : hits) {
 					icdCodes.add(hit.get("code"));
 				}
 			}
 			chapter.setIcdCodes(icdCodes);
 		}
 		hp.createJSONFile(allMainChapters);
 	}
 
 	private static void task1a() {
 		System.out.print("Clinical note	Sentence	ICD-10\n");
 		OntologyClassificator oc = new OntologyClassificator("owlFiles/",
 				"icd10no.owl");
 		for (int numCase = 1; numCase <= numPatientCases; numCase++) {
 			ArrayList<String> textLines = new PatientCaseParser().getCase(
 					"Case " + numCase).getTextLines();
 			System.out.print(numCase + "\t\t");
 			for (int numLine = 0; numLine < textLines.size(); numLine++) {
 				System.out.print((numLine + 1) + "\t\t");
 				String line = textLines.get(numLine);
 				ArrayList<Document> hits = oc.search(line);
 				for (int i = 0; i < hits.size(); ++i) {
 					System.out.print(hits.get(i).get("code") + ", ");
 				}
 				System.out.println();
 				System.out.print("\t\t");
 			}
 			System.out.println();
 		}
 	}
 
 	private static void task1c() {
 		System.out.print("Clinical note	Sentence	ATC\n");
 		OntologyClassificator oc = new OntologyClassificator("owlFiles/",
 				"atc.owl");
 		for (int numCase = 1; numCase <= numPatientCases; numCase++) {
 			ArrayList<String> textLines = new PatientCaseParser().getCase(
 					"Case " + numCase).getTextLines();
 			System.out.print(numCase + "\t\t");
 			for (int numLine = 0; numLine < textLines.size(); numLine++) {
 				System.out.print((numLine + 1) + "\t\t");
 				String line = textLines.get(numLine);
 				ArrayList<Document> hits = oc.search(line);
 				for (int i = 0; i < hits.size(); ++i) {
 					System.out.print(hits.get(i).get("code") + ", ");
 				}
 				System.out.println();
 				System.out.print("\t\t");
 			}
 			System.out.println();
 		}
 
 	}
 
 	private static void task2() throws IOException {
 		HandbookParser hb = new HandbookParser();
 		ArrayList<Chapter> allChapters = new ArrayList<Chapter>();
 		for (Chapter chapter : hb.getMainChapters()) {
 			allChapters.addAll(chapter.getAllChapters());
 		}
 		Directory index = OntologyClassificator.createIndex(allChapters);
 		PatientCaseParser pcp = new PatientCaseParser();
 		ArrayList<PatientCase> cases = new ArrayList<PatientCase>();
 		for (int numCase = 1; numCase <= numPatientCases; numCase++) {
 			cases.add(pcp.getCase("Case " + numCase));
 		}
 
 		OntologyClassificator oc = new OntologyClassificator("owlFiles/",
 				"icd10no.owl");
 		for (PatientCase pc : cases) {
 			String patientCaseIcdCodes = "";
 			for (String line : pc.getTextLines()) {
 				for (Document hit : oc.search(line)) {
 					patientCaseIcdCodes += hit.get("code") + " ";
 				}
 			}
			System.out.println(pc.getcaseName());
			System.out.println("Icd codes: " + patientCaseIcdCodes);
			int i = 0;
 			for (Document hit : OntologyClassificator.search(
 					patientCaseIcdCodes, index)) {
				System.out.println(i + ": " + hit.get("path") + " \t:\t" + hit.get("name"));
				i++;
 			}
 			System.out.println("--------------------------------");
 		}
 
 	}
 
 	public static void main(String[] args) throws IOException {
//		task1b();
 		task2();
 	}
 
 }
