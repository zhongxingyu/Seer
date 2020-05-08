 package controllers;
 
 import java.io.IOException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import models.User;
 import models.raidtracker.Raid;
 import models.raidtracker.RaidItem;
 import models.raidtracker.RaidMember;
 import models.wowapi.character.Avatar;
 import models.wowapi.character.AvatarItem;
 import models.wowapi.resources.Item;
 import play.db.DB;
 import play.mvc.Before;
 import play.mvc.Controller;
 
 public class Char extends Controller {
 
 	@Before
 	static void addDefaults() {
 		Application.addDefaults();
 	}
 
 	public static void show(Long id, String name, String realm) throws IOException, SQLException {
 
 		if (id == null && name.trim().length() > 0) {
 			Service.search(name);
 		}
 		
 		if (id == 0L && name.trim().length() > 0 && realm.trim().length() > 0) {
			Avatar avatar = Avatar.createAvatar(java.net.URLDecoder.decode(name, "UTF-8"), realm);
 			show(avatar.id, java.net.URLEncoder.encode(name, "UTF-8"), realm);
 		}
 
 		session.put("lastPage", request.url);
 		
 		List<RaidItem> raidItems = new ArrayList<RaidItem>();
 		RaidMember raidMember = RaidMember.findByName(name);
 		List<RaidItem> mitems = RaidItem.find("order by raid desc").fetch();
 
 		List<Raid> raids = new ArrayList<Raid>();
 
 		if (raidMember != null) {
 			PreparedStatement ps = DB.getConnection().prepareStatement("select r.id raidId from Raid r join RaidMember rm on (r.id = rm.raid_id) where BINARY rm.name = ? order by r.id desc");
 			ps.setString(1, raidMember.name);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				raids.add((Raid) Raid.findById(rs.getLong("raidId")));
 			}
 
 			for (RaidItem item : mitems) {
 				if (raidMember.name.equals(item.member.name)) {
 					raidItems.add(item);
 				}
 			}
 		}
 
 		User avatarUser = User.find("select distinct u from User u join u.alts as a where a.id = ?", id).first();
 		
 		Avatar avatar = Avatar.findById(id);
 		List<AvatarItem> items = AvatarItem.getOrderedItemList(avatar);
 		render(avatar, items, raidMember, raidItems, raids, avatarUser);
 	}
 
 	public static void showArmoryItemTooltip(Long avatarItemId) {
 		AvatarItem item = AvatarItem.findById(avatarItemId);
 		render(item);
 	}
 
 	public static void showItem(Long id) {
 		Item item = Item.find("byItemId", id).first();
 		if (item == null) {
 			item = Item.setItem(id);
 		}
 		List<RaidItem> items = RaidItem.find("itemId = ?", item.itemId).fetch();
 		List<AvatarItem> wearedItems = AvatarItem.find("itemId = ?", item.itemId).fetch();
 		render(item, items, wearedItems);
 	}
 
 	public static void showItemTooltip(Long id) {
 		Item item = Item.find("byItemId", id).first();
 		if (item == null) {
 			item = Item.setItem(id);
 		}
 		render(item);
 	}
 
 }
