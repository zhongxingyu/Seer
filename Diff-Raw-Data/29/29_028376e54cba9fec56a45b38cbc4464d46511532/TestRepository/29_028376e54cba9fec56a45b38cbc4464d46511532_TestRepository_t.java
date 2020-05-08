 package fuschia.tagger.common;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.nio.charset.Charset;
 
 import fuschia.tagger.common.csv.CSVReader;
 
 public class TestRepository {
 
 	public static void main(String[] args) {
 /*		DocumentRepository repo = DocumentRepository.get911();
 		System.out.println(repo.getAll().size());
 		System.out.println("GU002-Q1 "+repo.getDocumentById("GU001-Q1").getText());
 		System.out.println(repo.getParticipantMap().containsKey("GU058"));
 		System.out.println(repo.getParticipantMap().getS1("GU058"));
 		System.out.println(repo.getParticipantMap().getS1("GU176B"));
 		System.out.println(repo.getParticipantMap().getS1("GU060C"));
 */		
 		System.out.println("Starting...");
 		new TestRepository().createParticipantAdjCSV();
 		System.out.println("DONE!");
 	}
 	
 	public void createParticipantAdjCSV() {
 		try {
 			
 			DocumentRepository repo = DocumentRepository.get911();
 
 			BufferedWriter fAdjectives = new BufferedWriter(new FileWriter(new File("/Users/morteza/adjectives.q41.csv")));
 			
 			CSVReader csv = new CSVReader(this.getClass().getResourceAsStream("911-participant-map.csv"), Charset.forName("UTF-8"));
 			
 			csv.setTrimWhitespace(true);
 			csv.readHeaders();
 			int p=0;
 			while(csv.readRecord()) {
 				System.out.print(++p + ": ");
 				String keyS1 = csv.get("S1");
 				String keyS2 = csv.get("S2");
 				String keyS3 = csv.get("S3");
 				
 				// 1, 11, 12, 38, 39, 40, 41
 				String s1qId = keyS1 + "-Q41";
 				String s2qId = keyS2 + "-Q41";
 				String s3qId = keyS3 + "-Q41";
 				
 				String pLine = keyS1 + ",";
 				
 				System.out.println(s1qId);
 				Document doc1 = repo.getDocumentById(s1qId);	
 				Document doc2 = repo.getDocumentById(s2qId);
 				Document doc3 = repo.getDocumentById(s3qId);
 
 				if (doc1!=null && doc2!=null && doc3!=null ) {
 					
 					int adjCount = 0;
 					for (int i = 0 ; i<doc1.size(); i++) {
 						if (doc1.tags[i].startsWith("JJ")
 								|| doc1.tags[i].startsWith("RB")
 								|| doc1.tags[i].startsWith("WRB")){
 							adjCount++;
 						}
 					}
 					pLine += String.valueOf(adjCount) + "," + String.valueOf(doc1.size()) + ",";
 
 					adjCount = 0;
 					for (int i = 0 ; i<doc2.size(); i++) {
 						if (doc2.tags[i].startsWith("JJ")
 								|| doc2.tags[i].startsWith("RB")
 								|| doc2.tags[i].startsWith("WRB")){
 							adjCount++;
 						}
 					}
 					pLine += String.valueOf(adjCount) + "," + String.valueOf(doc2.size()) + ",";
 
 					adjCount = 0;
 					for (int i = 0 ; i<doc3.size(); i++) {
 						if (doc3.tags[i].startsWith("JJ")
 								|| doc3.tags[i].startsWith("RB")
 								|| doc3.tags[i].startsWith("WRB")){
 							adjCount++;
 						}
 					}
 					pLine += String.valueOf(adjCount) + "," + String.valueOf(doc3.size());
 					
 					fAdjectives.write(pLine);
 					fAdjectives.newLine();					
 				}
 			}
 			csv.close();
 			fAdjectives.flush();
 			fAdjectives.close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}		
 	}
 
 }
