 package uy.com.leecenso;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import nl.knaw.dans.common.dbflib.CorruptedTableException;
 import nl.knaw.dans.common.dbflib.IfNonExistent;
 import nl.knaw.dans.common.dbflib.Record;
 import nl.knaw.dans.common.dbflib.Table;
 
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.impl.HttpSolrServer;
 import org.apache.solr.common.SolrInputDocument;
 
 public class EcribirCenso {
 	
	final private static String SOLR_URL = "http://localhost:8983/solr/collection1";
 
 	public static void main(String[] args) {
 		HttpSolrServer solrServer = new HttpSolrServer(SOLR_URL);
 		solrServer.setMaxRetries(1);
 		solrServer.setConnectionTimeout(5000);
 		
 		Table table = new Table(new File("/home/vector/Desarrollo/leecenso/dbf-data/Viviendas.dbf"));
 		try {
 			table.open(IfNonExistent.ERROR);
 			int i = 0;
 			Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
 			for (Iterator<Record> iterator = table.recordIterator(); iterator.hasNext();) {
 				Record record = iterator.next();
 				SolrInputDocument document = new SolrInputDocument();
 				for (ViviendasAttr attr : ViviendasAttr.values()) {
 					switch (attr.type()) {
 					case NUMBER:
 						try {
 							document.addField(attr.name(), record.getNumberValue(attr.name()));
 						}
 						catch (NumberFormatException ex) {
							System.out.println(attr.name());
							ex.printStackTrace();
 						}
 						break;
 					case CHARACTER:
 						document.addField(attr.name(), record.getStringValue(attr.name()));
 						break;
 					default:
 						break;
 					}
 				}
 				docs.add(document);
 				if (i == 100000 || !iterator.hasNext()) {
 					i = 0;
 					solrServer.add(docs);
 					solrServer.commit();
 					docs.clear();
 				}
 			}
 		} 
 		catch (CorruptedTableException | IOException | SolrServerException e) {
 			e.printStackTrace();
 		}
 		finally {
 			try {
 				table.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
