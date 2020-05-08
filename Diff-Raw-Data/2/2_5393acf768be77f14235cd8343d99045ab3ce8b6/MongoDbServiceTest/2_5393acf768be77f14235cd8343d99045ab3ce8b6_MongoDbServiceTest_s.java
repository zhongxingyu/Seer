 package org.offlike.server.service;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.net.UnknownHostException;
 import java.util.List;
 
 import org.bson.types.ObjectId;
 import org.junit.Before;
 import org.junit.Test;
 import org.offlike.server.data.Campaign;
 import org.offlike.server.data.QrCode;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 
 public class MongoDbServiceTest {
 
 	public MongoDbService mongoDbService;
 	public DB database;
 
 	@Before
 	public void setup() throws UnknownHostException, MongoException {
 		Mongo m = new Mongo("localhost", 27017);
 		database = m.getDB("offlike");
 		database.dropDatabase();
 		database = m.getDB("offlike");
 		
 		mongoDbService = new MongoDbService(database);
 	}
 
 	@Test
 	public void testCreateCampaign() {
 		assertEquals(0, mongoDbService.countCampaigns());
 
 		Campaign campaignWales = new Campaign();
 		campaignWales.setTitle("Save the whales");
 		campaignWales.setDescription("Let us save the whales");
 		campaignWales.setExternalLink("http://...");

 		mongoDbService.createCampaign(campaignWales);
 		assertEquals(1, mongoDbService.countCampaigns());
 
 		List<Campaign> allCampaigns = mongoDbService.findAllCampaigns();
 		Campaign campaignFromDb = allCampaigns.get(0);
 		assertNotNull(campaignFromDb.getId());
 		assertEquals("Save the whales", campaignFromDb.getTitle());
 		assertEquals("Let us save the whales", campaignFromDb.getDescription());
 		assertEquals("http://...", campaignFromDb.getExternalLink());
 
 		Campaign campaignPeople = new Campaign();
 		campaignPeople.setTitle("Better people");
 		mongoDbService.createCampaign(campaignPeople);
 		assertEquals(2, mongoDbService.countCampaigns());
 	}
 	
 	@Test
 	public void testFindCampaignById() {
 		BasicDBObject camp = new BasicDBObject();
 		camp.put("title", "test-title");
 		DBCollection campaigns = database.getCollection("campaigns");
 		campaigns.insert(camp);
 		Campaign campaign = mongoDbService.findCampaignById(camp.getString("_id"));
 		assertEquals("test-title", campaign.getTitle());
 	}
 
 	@Test
 	public void testCreateQrCode() {
 		Campaign campaignA = new Campaign();
 		campaignA.setTitle("Save the whales");
 		campaignA.setDescription("Let us save the whales");
 		campaignA.setExternalLink("http://...");
 		mongoDbService.createCampaign(campaignA);
 		
 		Campaign campaignB = new Campaign();
 		campaignB.setTitle("Don't save the whales");
 		campaignA.setDescription("Let us not save the whales");
 		mongoDbService.createCampaign(campaignB);
 		
 		// no QrCodes for the campaign
 		List<QrCode> allQrCodesA = mongoDbService.findQrCodesForCampaign(campaignA.getId());
 		assertEquals(0, allQrCodesA.size());
 		List<QrCode> allQrCodesB = mongoDbService.findQrCodesForCampaign(campaignB.getId());
 		assertEquals(0, allQrCodesB.size());
 		
 		QrCode qrCode = new QrCode();
 		mongoDbService.createQrCode(campaignA, qrCode);
 
 		// one QrCodes for the campaign A
 		allQrCodesA = mongoDbService.findQrCodesForCampaign(campaignA.getId());
 		assertEquals(1, allQrCodesA.size());
 		allQrCodesB = mongoDbService.findQrCodesForCampaign(campaignB.getId());
 		assertEquals(0, allQrCodesB.size());
 		
 		QrCode qrCodeOfCampaignA = allQrCodesA.get(0);
 		assertNotNull(qrCodeOfCampaignA.getId());
 		assertEquals(campaignA.getId(), qrCodeOfCampaignA.getCampaignId());
 	}
 	
 	@Test
 	public void testFindQrCodeById() {
 		BasicDBObject qrCode = new BasicDBObject();
 		qrCode.put("counter", 0);
 		qrCode.put("campaignId", "123456");
 		qrCode.put("latitude", 12345.67);
 		qrCode.put("longitude", 98765.43);
 		qrCode.put("accuracy", 10);
 		
 		DBCollection qrCodes = database.getCollection("qrCodes");
 		qrCodes.insert(qrCode);
 		QrCode campaign = mongoDbService.findQrCodeById(qrCode.getString("_id"));
 		assertEquals("123456", campaign.getCampaignId());
 		assertEquals(new Double(12345.67), campaign.getLatitude());
 		assertEquals(new Double(98765.43), campaign.getLongitude());
 		assertEquals(new Integer(10), campaign.getAccuracy());
 	}
 	
 	@Test
 	public void testActivateQrCode() {
 		BasicDBObject qrCode = new BasicDBObject();
 		qrCode.put("counter", 0);
 		qrCode.put("campaignId", "123456");
 		
 		DBCollection qrCodes = database.getCollection("qrCodes");
 		qrCodes.insert(qrCode);
 		String qrCodeId = qrCode.getString("_id");
 		
 		mongoDbService.activateQrCode(qrCodeId, 12345.67, 98765.43, 10);
 		assertEquals(1, database.getCollection("qrCodes").find().count());
 
 		DBCollection allQrCodes = database.getCollection("qrCodes");
 		DBObject query = new BasicDBObject("_id", new ObjectId(qrCodeId));
 		DBObject updatedDbObject = allQrCodes.findOne(query);
 		assertEquals(new Double(12345.67), updatedDbObject.get("latitude"));
 		assertEquals(new Double(98765.43), updatedDbObject.get("longitude"));
 		assertEquals(new Integer(10), updatedDbObject.get("accuracy"));
 		
 		QrCode updatedQrCode = mongoDbService.findQrCodeById(qrCodeId);
 		assertEquals(0, updatedQrCode.getCounter());
 		assertEquals("123456", updatedQrCode.getCampaignId());
 		assertEquals(new Double(12345.67), updatedQrCode.getLatitude());
 		assertEquals(new Double(98765.43), updatedQrCode.getLongitude());
 		assertEquals(new Integer(10), updatedQrCode.getAccuracy());
 	}
 }
