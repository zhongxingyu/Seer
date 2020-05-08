 package fuschia.tagger.common;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.Deflater;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import fuschia.tagger.common.csv.CSVReader;
 
 public class DocumentRepository {
 	private HashMap<String, Document> docs = null;
 	private ParticipantMap participantMap = null;
 
 	public DocumentRepository() {
 		docs = new HashMap<String, Document>();
 		participantMap = new ParticipantMap();
 	}
 
 	public void setDocuments(HashMap<String, Document> docs) {
 		this.docs.clear();
 		this.docs.putAll(docs);
 	}
 
 	@SuppressWarnings("unchecked")
 	public static DocumentRepository get911() {
 		try{
 			DocumentRepository repo = new DocumentRepository();
 			repo.readParticipantMapCSV(repo.getClass().getResourceAsStream("911-participant-map.csv"));
 			InputStream is = repo.getClass().getResourceAsStream("911.cmap.gz");
 			GZIPInputStream gzipis = new GZIPInputStream(is);
 			ObjectInputStream oisCompressed = new ObjectInputStream(gzipis);
 
 			Object obj = oisCompressed.readObject();
 
 			if (obj instanceof HashMap<?, ?>) {
 				repo.setDocuments((HashMap<String, Document>)obj);
 			} else {
 				throw new Exception("Invalid object format");
 			}
 			
 			return repo;					
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public void readParticipantMapCSV(InputStream inputStream) throws Exception {
 		CSVReader csv = new CSVReader(inputStream, Charset.forName("UTF-8"));
 		csv.setTrimWhitespace(true);
 		csv.readHeaders();
 		while(csv.readRecord()) {
 			String keyS1 = csv.get("S1");
 			String keyS2 = csv.get("S2");
 			String keyS3 = csv.get("S3");
 			this.participantMap.put(keyS1, keyS2, keyS3);
 		}
 		csv.close();
 	}
 	
 	public ParticipantMap getParticipantMap() {
 		return participantMap;
 	}
 	
 	public Map<String, Document> getAll() {
 		return docs;
 	}
 
 	public Document getDocumentById(String documentId) {
 		return docs.get(documentId);
 	}
 
 	public void addDocument(String documentId, Document document) {
 		docs.put(documentId, document);
 	}
 	
 	public void saveToFile(String strFilePath) throws Exception{
 		FileOutputStream fout = new FileOutputStream(new File(strFilePath), false);
 		GZIPOutputStream gzipos = new GZIPOutputStream(fout) {
 			{
 				def.setLevel(Deflater.BEST_COMPRESSION);
 			}
 		};
 
 		ObjectOutputStream oosCompressed = new ObjectOutputStream(gzipos);
 		oosCompressed.writeObject(docs);
 		oosCompressed.flush();
 		oosCompressed.close();
 		fout.flush();
 		fout.close();		
 	}
 
 	@SuppressWarnings("unchecked")
 	public void loadFromFile(String strFilePath) throws Exception {
 		FileInputStream fis = new FileInputStream(new File(strFilePath));
 		GZIPInputStream gzipis = new GZIPInputStream(fis);
 		ObjectInputStream oisCompressed = new ObjectInputStream(gzipis);
 
 		docs.clear();
 		docs = null;
 
 		Object obj = oisCompressed.readObject();
 
 		if (obj instanceof HashMap<?, ?>) {
 			docs = (HashMap<String, Document>)obj;
 		} else {
			oisCompressed.close();
 			throw new Exception("Invalid object format");
 		}
		
		oisCompressed.close();
 	}
 
 	public void clear() {
 		docs.clear();
 	}
 }
