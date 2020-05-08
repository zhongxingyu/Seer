 package org.offlike.server.service;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.bson.types.ObjectId;
 import org.offlike.server.data.Campaign;
 import org.offlike.server.data.QrCode;
import org.springframework.beans.factory.annotation.Autowired;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 public class MongoDbService {
 
 	private final DB database;
 
	@Autowired
 	public MongoDbService(DB database) {
 		this.database = database;
         }
 	
 	public int countCampaigns() {
 		DBCollection allCampaigns = database.getCollection("campaigns");
 		DBCursor cursor = allCampaigns.find();
 		return cursor.size();
 	}
 
 	public void createCampaign(Campaign campaign) {
 		BasicDBObject camp = new BasicDBObject();
 		camp.put("title", campaign.getTitle());
 		camp.put("description", campaign.getDescription());
 		camp.put("externalLink", campaign.getExternalLink());
 
 		DBCollection campaigns = database.getCollection("campaigns");
 		campaigns.insert(camp);
 
 		campaign.setId(((ObjectId) camp.get("_id")).toString());
 	}
 
 	public void createQrCode(Campaign campaign, QrCode qrCode) {
 		BasicDBObject dbQrCode = new BasicDBObject();
 		dbQrCode.put("campaignId", campaign.getId());
 		dbQrCode.put("imageData", qrCode.getImageData());
 
 		DBCollection dbQrCodes = database.getCollection("qrCodes");
 		dbQrCodes.insert(dbQrCode);
 
 		qrCode.setId(((ObjectId) dbQrCode.get("_id")).toString());		
 	}
 	
 	public QrCode findQrCodeById(String qrCodeId) {
 		return null;
 	}
 	
 	public List<Campaign> findAllCampaigns() {
 		ArrayList<Campaign> allCampaigns = new ArrayList<Campaign>();
 
 		DBCollection dbCampaigns = database.getCollection("campaigns");
 		DBCursor cursor = dbCampaigns.find();
 		while (cursor.hasNext()) {
 			DBObject dbObject = (DBObject) cursor.next();
 			Campaign campaign =createCampaignFromDbObject(dbObject);
 			allCampaigns.add(campaign);
 		}
 		return allCampaigns;
 	}
 
 	public Campaign findCampaignById(String campaignId) {
 		DBCollection allCampaigns = database.getCollection("campaigns");
 		DBObject query = new BasicDBObject("_id", new ObjectId(campaignId));
 		
 		DBObject found = allCampaigns.findOne(query);
 		return createCampaignFromDbObject(found);
 	}
 
 	private Campaign createCampaignFromDbObject(DBObject found) {
 		Campaign campaign = new Campaign();
 		campaign.setId(found.get("_id").toString());
 		campaign.setTitle(found.get("title").toString());
 		String description = found.containsField("description") ? found.get("description").toString() : null;
 		campaign.setDescription(description);
 		String externalLink = found.containsField("externalLink") ? found.get("externalLink").toString() : null;
 		campaign.setExternalLink(externalLink);
 		return campaign;
 	}
 	
 	private void printObjects(DB db) {
 		System.out.println("aktueller Datenbestand:");
 
 		Set<String> colls = db.getCollectionNames();
 
 		System.out.println("-------------------------------");
 		for (String s : colls) {
 			System.out.println(s);
 		}
 	}
 
 	public List<QrCode> findQrCodesForCampaign(String campaignId) {
 		ArrayList<QrCode> qrCodes = new ArrayList<QrCode>();
 		
 		DBCollection allQrCodes = database.getCollection("qrCodes");
 		DBObject query = new BasicDBObject("campaignId", new ObjectId(campaignId));
 		
 		DBCursor found = allQrCodes.find(query);
 		while (found.hasNext()) {
 			DBObject dbObject = (DBObject) found.next();
 			
 			System.out.println(dbObject);
 			
 			QrCode qrCode = new QrCode();
 			qrCode.setCampaignId(dbObject.get("campaignId").toString());
 			qrCode.setImageData(dbObject.get("imageData").toString());
 			qrCodes.add(qrCode);
 		}
 		
 		System.out.println("Ende");
 		
 		return qrCodes;
 	}
 
 }
