 import java.io.File;
 import java.io.IOException;
 
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.no.NorwegianAnalyzer;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.SimpleFSDirectory;
 import org.apache.lucene.util.Version;
 
 import datatypes.Case;
 import parser.CaseReader;
 import parser.NLHParser;
 import search.IndexFiles;
 import search.SearchFiles;
 
 
 public class Main {
 	public static void main(String[] args) {
 		PropertyConfigurator.configure("lib/jena-log4j.properties");
 		Case[] cases = CaseReader.readCases();
 		Directory dirICD10;
 		Directory dirAtc;
 		Directory dirNLH;
 		Analyzer ana = new NorwegianAnalyzer(Version.LUCENE_CURRENT);
 		try {
 			dirICD10 = new SimpleFSDirectory(new File("Index/icd10"));
 			dirAtc = new SimpleFSDirectory(new File("Index/atc"));
 			dirNLH = new SimpleFSDirectory(new File("Index/NLH"));
 			IndexFiles index = new IndexFiles(dirICD10,dirAtc,dirNLH, ana);
 			index.index();
 			SearchFiles sf = new SearchFiles();
			int caseNum = 0;
 			for(Case c:cases){
 //			Case c = cases[0];
				//		for(String s:c.getSentences()){
				caseNum++;
				System.out.println("");
				System.out.println("Case : " + caseNum);
 				String s = c.getCaseText();
 				sf.Search(s, dirNLH, ana);
 				//				sf.Search(s, dirICD10, ana);
 				//				sf.Search(s, dirAtc, ana);
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 
 	}
 }
