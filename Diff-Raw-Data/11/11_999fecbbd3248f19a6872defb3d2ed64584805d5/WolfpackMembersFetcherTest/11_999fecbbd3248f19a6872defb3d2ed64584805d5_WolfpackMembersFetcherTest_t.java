 package il.technion.ewolf.server.jsonDataHandlers;
 
 import il.technion.ewolf.chunkeeper.ChunKeeper;
 import il.technion.ewolf.chunkeeper.ChunKeeperModule;
 import il.technion.ewolf.dht.SimpleDHTModule;
 import il.technion.ewolf.ewolf.EwolfAccountCreator;
 import il.technion.ewolf.ewolf.EwolfAccountCreatorModule;
 import il.technion.ewolf.ewolf.EwolfModule;
 import il.technion.ewolf.ewolf.WolfPack;
 import il.technion.ewolf.ewolf.WolfPackLeader;
 import il.technion.ewolf.http.HttpConnector;
 import il.technion.ewolf.http.HttpConnectorModule;
 import il.technion.ewolf.kbr.KeybasedRouting;
 import il.technion.ewolf.kbr.openkad.KadNetModule;
 import il.technion.ewolf.msg.PokeMessage;
 import il.technion.ewolf.msg.SocialMail;
 import il.technion.ewolf.msg.SocialMessage;
import il.technion.ewolf.server.EWolfResponse;
 import il.technion.ewolf.server.jsonDataHandlers.WolfpackMembersFetcher;
 import il.technion.ewolf.server.jsonDataHandlers.WolfpackMembersFetcher.WolfpackMembersResponse;
 import il.technion.ewolf.socialfs.Profile;
 import il.technion.ewolf.socialfs.SocialFS;
 import il.technion.ewolf.socialfs.SocialFSCreatorModule;
 import il.technion.ewolf.socialfs.SocialFSModule;
 import il.technion.ewolf.socialfs.UserID;
 import il.technion.ewolf.stash.StashModule;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.junit.After;
 import org.junit.Test;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonElement;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 public class WolfpackMembersFetcherTest {
 	private static final int BASE_PORT = 10000;
 	private List<Injector> injectors = new LinkedList<Injector>();
 
 	@SuppressWarnings("unused")
 	private class JsonReqWolfpackMembersParams {
 //		If wolfpackName field wasn't sent with the request then
 //		the response list will contain all the members of all the "logged in" user wolfpacks
 		String wolfpackName;
 	}
 	@After
 	public void cleanup() {
 		for (Injector inj: injectors) {
 			inj.getInstance(KeybasedRouting.class).shutdown();
 			inj.getInstance(HttpConnector.class).shutdown();
 		}
 		injectors.clear();
 	}
 
 	private JsonElement setWolfpackMembersParams(String wolfpackName) {
 
 		JsonReqWolfpackMembersParams params = new JsonReqWolfpackMembersParams();
 		params.wolfpackName = wolfpackName;
 		Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
 		JsonElement jElem = gson.toJsonTree(params);
 		return jElem;
 	}
 
 	@Test
 	public void getAllMembers() throws Exception {
 		for (int i=0; i < 5; ++i) {
 			Injector injector = Guice.createInjector(
 					new KadNetModule()
 						.setProperty("openkad.keyfactory.keysize", "20")
 						.setProperty("openkad.bucket.kbuckets.maxsize", "20")
 						.setProperty("openkad.net.udp.port", ""+(BASE_PORT+i)),
 						
 					new HttpConnectorModule()
 						.setProperty("httpconnector.net.port", ""+(BASE_PORT+i)),
 					
 					new SimpleDHTModule(),
 						
 					new ChunKeeperModule(),
 					
 					new StashModule(),
 						
 					new SocialFSCreatorModule()
 						.setProperty("socialfs.user.username", "user_"+i)
 						.setProperty("socialfs.user.password", "1234"),
 					
 					new SocialFSModule(),
 					
 					new EwolfAccountCreatorModule(),
 					
 					new EwolfModule()
 			);
 			injectors.add(injector);
 		}
 		
 		for (Injector injector : injectors) {
 			
 			// start the Keybased routing
 			KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
 			kbr.create();
 			
 			// bind the http connector
 			HttpConnector connector = injector.getInstance(HttpConnector.class);
 			connector.bind();
 			connector.start();
 			
 			// bind the chunkeeper
 			ChunKeeper chnukeeper = injector.getInstance(ChunKeeper.class);
 			chnukeeper.bind();
 		}
 		
 		
 		for (int i=1; i < injectors.size(); ++i) {
 			int port = BASE_PORT + i - 1;
 			System.out.println(i+" ==> "+(i-1));
 			KeybasedRouting kbr = injectors.get(i).getInstance(KeybasedRouting.class);
 			kbr.join(Arrays.asList(new URI("openkad.udp://127.0.0.1:"+port+"/")));
 		}
 		
 		
 		for (Injector injector : injectors) {
 			System.out.println("creating...");
 			EwolfAccountCreator accountCreator = injector.getInstance(EwolfAccountCreator.class);
 			accountCreator.create();
 			System.out.println("done\n");
 			
 		}
 		/*
 		for (Injector injector : injectors) {
 			System.out.println("login ...");
 			SocialNetwork osn = injector.getInstance(SocialNetwork.class);
 			osn.login("1234");
 		}
 		*/
 		Thread.sleep(1000);
 		
 		List<SocialFS> sfsList = new ArrayList<SocialFS>();
 		List<UserID> userIDList = new ArrayList<UserID>();
 		List<String> strUidListBefore = new ArrayList<String>();
 		List<Profile> profileList = new ArrayList<Profile>();
 		for (int i=0; i<injectors.size(); i++) {
 			sfsList.add(i, injectors.get(i).getInstance(SocialFS.class));
 			userIDList.add(i, sfsList.get(i).getCredentials().getProfile().getUserId());
 			strUidListBefore.add(i, userIDList.get(i).toString());
 			profileList.add(i, sfsList.get(i).findProfile(userIDList.get(i)));
 		}
 		
 		WolfPackLeader sgm1 = injectors.get(0).getInstance(WolfPackLeader.class);
 
 		//add all users to user0 friends
 		WolfPack osn1Friends = sgm1.findOrCreateSocialGroup("friends");
 		WolfPack wallReaders = sgm1.findSocialGroup("wall-readers");
 		for (int i=1; i<injectors.size(); i++) {
 			Profile profile = profileList.get(i);
 			osn1Friends.addMember(profile);
 			wallReaders.addMember(profile);
 		}
 		
 		System.out.println(sgm1.getAllSocialGroups());
 
 		List<SocialMail> smList = new ArrayList<SocialMail>();
 		smList.add(0, null);
 		for (int i=1; i<injectors.size(); i++) {
 			smList.add(i, injectors.get(i).getInstance(SocialMail.class));
 			List<SocialMessage> inbox = smList.get(i).readInbox();
 			Assert.assertEquals(2, inbox.size());
 			Assert.assertEquals(PokeMessage.class, inbox.get(0).getClass());
 			((PokeMessage)inbox.get(0)).accept();
 			((PokeMessage)inbox.get(1)).accept();
 		}
 
 		JsonElement params = setWolfpackMembersParams(null);
 		WolfpackMembersResponse obj = (WolfpackMembersResponse) (injectors.get(0).getInstance(WolfpackMembersFetcher.class).handleData(params));
		Assert.assertEquals(obj.result(), EWolfResponse.RES_SUCCESS);
 		List<String> strUidListAfter = new ArrayList<String>();
 		for (int i=0; i<obj.membersList.size(); i++) {
 			strUidListAfter.add(obj.membersList.get(i).id);
 		}
 		strUidListAfter.add(userIDList.get(0).toString());
 		Assert.assertTrue(CollectionUtils.isEqualCollection(strUidListBefore, strUidListAfter));
 		
 	}
 
 	@Test
 	public void getWolfPackMembers() throws Exception {
 		for (int i=0; i < 5; ++i) {
 			Injector injector = Guice.createInjector(
 					new KadNetModule()
 						.setProperty("openkad.keyfactory.keysize", "20")
 						.setProperty("openkad.bucket.kbuckets.maxsize", "20")
 						.setProperty("openkad.net.udp.port", ""+(BASE_PORT+i)),
 						
 					new HttpConnectorModule()
 						.setProperty("httpconnector.net.port", ""+(BASE_PORT+i)),
 					
 					new SimpleDHTModule(),
 						
 					new ChunKeeperModule(),
 					
 					new StashModule(),
 						
 					new SocialFSCreatorModule()
 						.setProperty("socialfs.user.username", "user_"+i)
 						.setProperty("socialfs.user.password", "1234"),
 					
 					new SocialFSModule(),
 					
 					new EwolfAccountCreatorModule(),
 					
 					new EwolfModule()
 			);
 			injectors.add(injector);
 		}
 		
 		for (Injector injector : injectors) {
 			
 			// start the Keybased routing
 			KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
 			kbr.create();
 			
 			// bind the http connector
 			HttpConnector connector = injector.getInstance(HttpConnector.class);
 			connector.bind();
 			connector.start();
 			
 			// bind the chunkeeper
 			ChunKeeper chnukeeper = injector.getInstance(ChunKeeper.class);
 			chnukeeper.bind();
 		}
 		
 		
 		for (int i=1; i < injectors.size(); ++i) {
 			int port = BASE_PORT + i - 1;
 			System.out.println(i+" ==> "+(i-1));
 			KeybasedRouting kbr = injectors.get(i).getInstance(KeybasedRouting.class);
 			kbr.join(Arrays.asList(new URI("openkad.udp://127.0.0.1:"+port+"/")));
 		}
 		
 		
 		for (Injector injector : injectors) {
 			System.out.println("creating...");
 			EwolfAccountCreator accountCreator = injector.getInstance(EwolfAccountCreator.class);
 			accountCreator.create();
 			System.out.println("done\n");
 			
 		}
 		/*
 		for (Injector injector : injectors) {
 			System.out.println("login ...");
 			SocialNetwork osn = injector.getInstance(SocialNetwork.class);
 			osn.login("1234");
 		}
 		*/
 		Thread.sleep(1000);
 		
 		List<SocialFS> sfsList = new ArrayList<SocialFS>();
 		List<UserID> userIDList = new ArrayList<UserID>();
 		List<String> strUidListBefore = new ArrayList<String>();
 		List<Profile> profileList = new ArrayList<Profile>();
 		for (int i=0; i<injectors.size(); i++) {
 			sfsList.add(i, injectors.get(i).getInstance(SocialFS.class));
 			userIDList.add(i, sfsList.get(i).getCredentials().getProfile().getUserId());
 			strUidListBefore.add(i, userIDList.get(i).toString());
 			profileList.add(i, sfsList.get(i).findProfile(userIDList.get(i)));
 		}
 		
 		WolfPackLeader sgm1 = injectors.get(0).getInstance(WolfPackLeader.class);
 
 		//add all users to user0 friends
 		WolfPack osn1Friends = sgm1.findOrCreateSocialGroup("friends");
 		WolfPack wallReaders = sgm1.findSocialGroup("wall-readers");
 		//add all users to wall-readers 
 		for (int i=1; i<injectors.size(); i++) {
 			wallReaders.addMember(profileList.get(i));
 		}
 		//add users 1,2,3 to friends
 		for (int i=1; i<4; i++) {
 			osn1Friends.addMember(profileList.get(i));
 		}
 		
 		//System.out.println(sgm1.getAllSocialGroups());
 
 		List<SocialMail> smList = new ArrayList<SocialMail>();
 		smList.add(0, null);
 		for (int i=1; i<4; i++) {
 			smList.add(i, injectors.get(i).getInstance(SocialMail.class));
 			List<SocialMessage> inbox = smList.get(i).readInbox();
 			Assert.assertEquals(2, inbox.size());
 			Assert.assertEquals(PokeMessage.class, inbox.get(0).getClass());
 			((PokeMessage)inbox.get(0)).accept();
 			((PokeMessage)inbox.get(1)).accept();
 		}
 		{
 			smList.add(4, injectors.get(4).getInstance(SocialMail.class));
 			List<SocialMessage> inbox = smList.get(4).readInbox();
 			Assert.assertEquals(1, inbox.size());
 			Assert.assertEquals(PokeMessage.class, inbox.get(0).getClass());
 			((PokeMessage)inbox.get(0)).accept();
 		}
 
 		JsonElement params = setWolfpackMembersParams("wall-readers");
 		WolfpackMembersResponse obj1 = (WolfpackMembersResponse) (injectors.get(0).getInstance(WolfpackMembersFetcher.class).handleData(params));
 		List<String> strUidListAfter = new ArrayList<String>();
 		for (int i=0; i<obj1.membersList.size(); i++) {
 			strUidListAfter.add(obj1.membersList.get(i).id);
 		}
 		strUidListAfter.add(userIDList.get(0).toString());
 		Assert.assertTrue(CollectionUtils.isEqualCollection(strUidListBefore, strUidListAfter));
		Assert.assertEquals(obj1.result(), EWolfResponse.RES_SUCCESS);
 		
 		params = setWolfpackMembersParams("friends");
 		WolfpackMembersResponse obj2 = ((WolfpackMembersResponse)injectors.get(0).getInstance(WolfpackMembersFetcher.class).handleData(params));
		Assert.assertEquals(obj2.result(), EWolfResponse.RES_SUCCESS);
 		List<String> strUidList2After = new ArrayList<String>();
 		for (int i=0; i<obj2.membersList.size(); i++) {
 			strUidList2After.add(obj2.membersList.get(i).id);
 		}
 		List<String> strUid2ListBefore = new ArrayList<String>();
 		strUid2ListBefore.add(userIDList.get(1).toString());
 		strUid2ListBefore.add(userIDList.get(2).toString());
 		strUid2ListBefore.add(userIDList.get(3).toString());
 		Assert.assertTrue(CollectionUtils.isEqualCollection(strUid2ListBefore, strUidList2After));
 		
 	}
 }
