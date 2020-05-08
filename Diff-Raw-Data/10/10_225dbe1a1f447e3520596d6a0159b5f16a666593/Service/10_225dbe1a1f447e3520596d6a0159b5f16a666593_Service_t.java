 package controllers;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import org.im4java.core.CompositeCmd;
 import org.im4java.core.ConvertCmd;
 import org.im4java.core.IM4JavaException;
 import org.im4java.core.IMOperation;
 import org.im4java.core.Info;
 import org.im4java.core.InfoException;
 import org.im4java.process.ProcessStarter;
 import org.jsoup.Jsoup;
 import org.jsoup.safety.Whitelist;
 
 import models.Config;
 import models.Message;
 import models.PrivateMessage;
 import models.User;
 import models.wowapi.ApiException;
 import models.wowapi.ArmoryTooltip;
 import models.wowapi.AuctionData;
 import models.wowapi.auction.Auction;
 import models.wowapi.auction.AuctionDump;
 import models.wowapi.character.Avatar;
 import models.wowapi.core.FetchSite;
 import models.wowapi.core.FetchType;
 import models.wowapi.guild.Guild;
 import models.wowapi.resources.Item;
 import models.wowapi.resources.Realm;
 import models.wowapi.resources.Recipe;
 import models.wowapi.resources.helper.RecipeShoppingCart;
 import models.wowapi.types.Faction;
 import play.Logger;
 import play.db.DB;
 import play.db.jpa.GenericModel.JPAQuery;
 import play.libs.WS.HttpResponse;
 import play.modules.pusher.BasicUserInfo;
 import play.modules.pusher.PresenceChannelData;
 import play.modules.pusher.Pusher;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.Http.Cookie;
 import utils.OnlineUser;
 import utils.StringUtils;
 
 import com.google.gson.Gson;
 
 import flexjson.JSONSerializer;
 
 public class Service extends Controller {
 	@Before(unless={"pusherAuth", "checkPrivateMessages", "getShoppingCart", "showTooltip", "getRecipeCrafter"})
 	static void addDefaults() {
 		Application.addDefaults();
 	}
 	
 	public static void robots() {
 		renderText("User-agent: *\nAllow: /");
 	}
 	
 	public static void search(String search) {
 		render(search);
 	}
 
 	
 
 	
 	public static void checkPrivateMessages() {
 		User user = User.getConnectedUser(session.get("username"));
 		
 		PrivateMessage pm = PrivateMessage.find("toUser IN (?1) order by sendDate desc", user.alts).first();
 		
 		JSONSerializer userSerializer = new JSONSerializer().include("mailCount").exclude("*").prettyPrint(true);
 		JSONSerializer pmSerializer = new JSONSerializer().include("sendDate","subject","fromUser.avatarLink","fromUser.avatarLink","fromUser.image" ).exclude("*").prettyPrint(true);
 		renderJSON("{ \"message\":" + pmSerializer.serialize(pm) + ", \n \"user\":" + userSerializer.serialize(user) + "}");
 	}
 	
 	public static void pusherAuth(String channel_name, String socket_id) {
 
 		User user = User.getConnectedUser(session.get("username"));
 		if (user != null) {
 			Pusher pusher = new Pusher(Config.getConfig("pusher.appId"), Config.getConfig("pusher.key"), Config.getConfig("pusher.secret"));
 			OnlineUser userInfo = new OnlineUser();
 			userInfo.userId = user.id;
 			userInfo.userName = user.fullname;
 			userInfo.avatarName = user.avatar.name;
 			userInfo.avatarEmail = user.avatar.getAvatarMail();
 			userInfo.avatarLink = user.avatar.getAvatarLink();
 			
 			PresenceChannelData channelData = new PresenceChannelData(user.id.toString(), userInfo);
 			renderJSON(pusher.createAuthString(socket_id, channel_name, channelData));
 		}
 
 	}
 
 	public static void getRecipeCrafter(Long id) {
 
 		List<Avatar> avatars = Avatar.find("SELECT DISTINCT ar.avatar FROM AvatarRecipe ar join ar.avatar a WHERE ar.recipe = ?", Recipe.findById(id)).fetch();
 		render(avatars);
 	}
 
 	private static RecipeShoppingCart getShoppingCart(List<String> recipeIdsList, HashMap<Long, Integer> recipeIdCounts, RecipeShoppingCart recipeShoppingCart) {
 		Cookie savedRecipes = request.cookies.get("savedRecipes");
 		if (savedRecipes == null) {
 			savedRecipes = new Cookie();
 			savedRecipes.value = "";
 			response.setCookie("savedRecipes", "");
 		}
 
 		recipeIdsList.addAll(Arrays.asList(savedRecipes.value.split(",")));
 
 		try {
 			if (recipeIdsList.size() > 0) {
 				for (String string : recipeIdsList) {
 					Integer test = recipeIdCounts.get(Long.parseLong(string));
 					if (test == null) {
 						test = 1;
 					} else {
 						test = test + 1;
 					}
 					recipeIdCounts.put(Long.parseLong(string), test);
 				}
 				recipeShoppingCart = new RecipeShoppingCart(recipeIdsList);
 			}
 		} catch (NumberFormatException e) {
 			// TODO: handle exception
 		}
 		return recipeShoppingCart;
 	}
 
 	public static void showRecipe(Long id) {
 		Recipe recipe = Recipe.findById(id);
 		render(recipe);
 	}
 
 	public static void getMessage(Long id) {
 		PrivateMessage message = PrivateMessage.findById(id);
 		if (!message.readed) {
 			message.readed = true;
 			message.readDate = new Date();
 			message.save();
 		}
 		render(message);
 	}
 
 	public static void getMessageQuote(Long id) {
 		PrivateMessage message = PrivateMessage.findById(id);
 		render(message);
 	}
 
 	public static void showRecipeDataTable(Long id, Long sEcho, String sSearch, Integer iDisplayStart, Integer iDisplayLength, Boolean iSortingCols, Integer iSortCol_0, String sSortDir_0) {
 
 		List<String> recipeIdsList = new ArrayList<String>();
 		HashMap<Long, Integer> recipeIdCounts = new HashMap<Long, Integer>();
 		RecipeShoppingCart recipeShoppingCart = null;
 		recipeShoppingCart = getShoppingCart(recipeIdsList, recipeIdCounts, recipeShoppingCart);
 
 		String order = "order by ";
 		if (iSortingCols) {
 			switch (iSortCol_0) {
 			case 1:
 				order += " r.name " + sSortDir_0 + ", ";
 				break;
 			case 2:
 				order += " r.reagentcost " + sSortDir_0 + ", ";
 				break;
 			case 3:
 				order += " r.profLevel " + sSortDir_0 + ", ";
 				break;
 			}
 		}
 
 		order += "r.item.level desc, r.reagentcost desc";
 
 		String sql = "SELECT DISTINCT r FROM Recipe AS r,IN(r.reagents) AS rr WHERE r.profId = :id AND (:search IS NULL OR (r.name LIKE :search OR rr.item.name LIKE :search)) " + order;
 
 		JPAQuery query = Recipe.find(sql).bind("id", id);
 
 		if (sSearch.length() > 0) {
 			sSearch = "%" + sSearch + "%";
 			System.out.println(sSearch);
 			query.query.setParameter("search", sSearch);
 		} else {
 			query.query.setParameter("search", null);
 		}
 
 		Integer filterCount = query.query.getResultList().size();
 
 		List<Recipe> recipes = query.from(iDisplayStart).fetch(iDisplayLength);
 
 		Long recipeCount = Recipe.count("Select DISTINCT count(*) from Recipe where profId = ?", id);
 
 		render("/Service/showRecipeDataTable.json", recipes, sEcho, recipeCount, recipeShoppingCart, recipeIdCounts, filterCount);
 	}
 
 	public static void showTooltip(Long id, String type) {
 		if (type.equals("Spell")) {
 			ArmoryTooltip toolTip = ArmoryTooltip.findById(id);
 			render("/Service/show" + type + "Tooltip.html", toolTip);
 		}
 		if (type.equals("RecipeOptions")) {
 			Recipe recipe = Recipe.findById(id);
 			render("/Service/show" + type + "Tooltip.html", recipe);
 		}
 	}
 
 	public static void update() throws ApiException, SQLException, IOException, InterruptedException, IM4JavaException {
 
 		String myPath = "C:\\Program Files (x86)\\ImageMagick-6.7.6-Q16";
 		ProcessStarter.setGlobalSearchPath(myPath);
 
 		String image1 = "C:\\nom\\GuildPress\\public\\tabard\\png\\hooks.png";
 		String image2 = "C:\\nom\\GuildPress\\public\\tabard\\png\\emblem_164.png";
 		String image3 = "C:\\nom\\GuildPress\\public\\tabard\\png\\border_00.png";
 		String image4 = "C:\\nom\\GuildPress\\public\\tabard\\png\\overlay_00.png";
 		String image5 = "C:\\nom\\GuildPress\\public\\tabard\\png\\bg_00.png";
 		String image6 = "C:\\nom\\GuildPress\\public\\tabard\\png\\shadow_00.png";
 		String image7 = "C:\\nom\\GuildPress\\public\\tabard\\png\\ring-alliance.png";
 
 		String imageOut = "C:\\nom\\GuildPress\\public\\tabard\\png\\test.png";
 
 		Info imageInfo = new Info(image1, true);
 		System.out.println("Format: " + imageInfo.getImageFormat());
 		System.out.println("Width: " + imageInfo.getImageWidth());
 		System.out.println("Height: " + imageInfo.getImageHeight());
 		System.out.println("Geometry: " + imageInfo.getImageGeometry());
 		System.out.println("Depth: " + imageInfo.getImageDepth());
 		System.out.println("Class: " + imageInfo.getImageClass());
 
 		// Long[] _position = new Long[0]{};
 		// [
 		// [ 0, 0, (_width*216/240), (_width*216/240) ],
 		// [ (_width*18/240), (_width*27/240), (_width*179/240),
 		// (_width*216/240) ],
 		// [ (_width*18/240), (_width*27/240), (_width*179/240),
 		// (_width*210/240) ],
 		// [ (_width*18/240), (_width*27/240), (_width*179/240),
 		// (_width*210/240) ],
 		// [ (_width*31/240), (_width*40/240), (_width*147/240),
 		// (_width*159/240) ],
 		// [ (_width*33/240), (_width*57/240), (_width*125/240),
 		// (_width*125/240) ],
 		// [ (_width*18/240), (_width*27/240), (_width*179/240), (_width*32/240)
 		// ]
 		// ];
 
 		// create the operation, add images and operators/options
 		IMOperation op = new IMOperation();
 		op.addImage("[179x32+10+20]"); // read and crop second image
 		op.addImage("[216x216+0+0]"); // read and crop first image
 
 		op.addImage();
 
 		CompositeCmd composite = new CompositeCmd();
 
 		composite.run(op, image1, image7, imageOut);
 
 		// Gson gson = new Gson();
 		// AuctionDump result =
 		// gson.fromJson(FetchSite.fetch("http://eu.battle.net/auction-data/04dcab1403d283a261d5d416a1e151a7/auctions.json",
 		// FetchType.API).response, AuctionDump.class);
 		// List<Auction> auctions = result.get(Faction.ALLIANCE).getAuctions();
 		// Connection con = DB.getConnection();
 		// con.setAutoCommit(false);
 		// PreparedStatement ps =
 		// con.prepareStatement("REPLACE INTO Auction (`id`, `itemId`, `owner`, `bid`, `buyout`, `quantity`, `timeLeft`, `faction`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
 		// int cnt = 0;
 		// int comcnt = 0;
 		// for (Auction auction : auctions) {
 		// cnt++;
 		//
 		// ps.setLong(1, auction.getId());
 		// ps.setLong(2, auction.getItemId());
 		// ps.setString(3, auction.getOwner());
 		// ps.setLong(4, auction.getBid());
 		// ps.setLong(5, auction.getBuyout());
 		// ps.setLong(6, auction.getQuantity());
 		// ps.setString(7, auction.getTimeLeft());
 		// ps.setInt(8, Faction.ALLIANCE.getFaction());
 		// ps.executeUpdate();
 		// comcnt++;
 		//
 		// if (comcnt == 500) {
 		// comcnt = 0;
 		// con.commit();
 		// }
 		// }
 		// Logger.info("[Auction][insert] " + cnt + " eingefügt ");
 		// con.commit();
 
 		// Recipe recipe = Recipe.find("spellId = ?", 17635L).first();
 		// recipe.setTooltip();
 
 		// List<Recipe> recipes = Recipe.findAll();
 		// for (Recipe recipe : recipes) {
 		// recipe.setTooltip();
 		// }
 
 		// Item item = Item.find("jsonEquip like ? and name = 'Zaubertuch'",
 		// "%avgbuyout%").first();
 		//
 		// double teyt = item.avgbuyout;
 		//
 		// renderHtml(NumberUtils.formatGold(item.avgbuyout));
 
 		// List<Recipe> recipes = Recipe.findAll();
 		//
 		// for (Recipe recipe : recipes) {
 		// List<RecipeReagent> recipeReagents = recipe.reagents;
 		// for (RecipeReagent recipeReagent : recipeReagents) {
 		// recipe.reagentcost = recipe.reagentcost +
 		// (recipeReagent.item.avgbuyout * recipeReagent.count);
 		// }
 		// System.out.println(recipe.reagentcost);
 		// recipe.save();
 		// }
 
 		// List<Item> items = Item.findAll();
 		// for (Item item : items) {
 		// item.setAuctionData();
 		// }
 
 		// List<Recipe> recipes = Recipe.findAll();
 		// for (Recipe recipe : recipes) {
 		// recipe.reagentcost = 0;
 		// for (RecipeReagent reagent : recipe.reagents) {
 		// if (reagent.item.buyout > 0) {
 		// recipe.reagentcost = recipe.reagentcost + (reagent.item.buyout *
 		// reagent.count);
 		// System.out.println("new data");
 		// } else {
 		// recipe.reagentcost = recipe.reagentcost + (reagent.item.avgbuyout *
 		// reagent.count);
 		// }
 		// }
 		// recipe.save();
 		// }
 		//
 
 		// this.reagentcost = this.reagentcost + (recipeReagent.item.avgbuyout *
 		// recipeReagent.count);
 
 		// List<Item> items = Item.find("jsonEquip like ?",
 		// "%avgbuyout%").fetch();
 		// for (Item item : items) {
 		// if (!item.jsonEquip.contains("{null}")) {
 		// JsonObject oJson = new
 		// JsonParser().parse(item.jsonEquip).getAsJsonObject();
 		// String memberName = "avgbuyout";
 		// if (oJson.has(memberName)) {
 		// item.avgbuyout = oJson.get(memberName).getAsFloat();
 		// System.out.println(item.name + " Auktionskosten: " + item.avgbuyout);
 		// item.save();
 		// }
 		// }
 		// }
 
 		// List<Recipe> recipes =
 		// Recipe.find("item_id is null and name is not null").fetch();
 		// for (Recipe recipe : recipes) {
 		// recipe.setTooltip();
 		// }
 	}
 
 	
 	public static void updateAvatar(Long id) throws IOException, SQLException {
 		Avatar avatar = Avatar.findById(id);
 		avatar.updateAvatar(avatar,true);

		Char.show(id, java.net.URLEncoder.encode(avatar.name, "UTF-8"), avatar.realm.name);
 	}
 	
 	public static void updateAvatar(String name, String realm) {
 
 		List<Recipe> recipes = Recipe.find("spellId = ?", 101937L).fetch(1);
 		for (Recipe recipe : recipes) {
 			recipe.setTooltip();
 			// System.out.println(recipe.armoryTooltip);
 		}
 		// renderHtml("");
 
 		Logger.info("[Service][Request][updateAvatar] " + name + " (" + realm + ")");
 		JSONSerializer avatarSerializer = new JSONSerializer().prettyPrint(true);
 		renderJSON(avatarSerializer.serialize(Avatar.createAvatar(name, realm)));
 	}
 
 	
 	
 	public static void updateGuild(String name, String realm) {
 		Logger.info("[Service][Request][updateGuild] " + name + " (" + realm + ")");
 		JSONSerializer guildSerializer = new JSONSerializer().prettyPrint(true);
 		renderJSON(guildSerializer.serialize(Guild.createGuild(name, realm)));
 	}
 
 	public static void updateItem(Long itemId) {
 		Logger.info("[Service][Request][updateItem] " + itemId);
 		JSONSerializer itemSerializer = new JSONSerializer().prettyPrint(true);
 		renderJSON(itemSerializer.serialize(Item.setItem(itemId)));
 	}
 
 	public static void updateItemPrices(File lua) throws FileNotFoundException {
 		AuctionData.parseFile(lua);
 	}
 
 	public static void updateRealms() {
 		Logger.info("[Service][Request][updateRealms] all Realms");
 		Realm.fetchRealms();
 		JSONSerializer realmSerializer = new JSONSerializer().prettyPrint(true);
 		renderJSON(realmSerializer.serialize(Realm.findAll()));
 	}
 
 	public static void updateRecipe(Long spellId) {
 		Logger.info("[Service][Request][updateRecipe] " + spellId);
 
 		Recipe recipe = Recipe.find("spellId = ?", spellId).first();
 		recipe.setTooltip();
 
 		JSONSerializer spellSerializer = new JSONSerializer().prettyPrint(true);
 		renderJSON(spellSerializer.serialize(recipe));
 	}
 
 	public static void upLoadItemPrices() {
 		render();
 	}
 
 }
