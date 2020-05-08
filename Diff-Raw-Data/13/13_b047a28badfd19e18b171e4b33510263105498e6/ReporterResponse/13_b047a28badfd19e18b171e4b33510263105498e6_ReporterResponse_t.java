 package me.heldplayer.ModeratorGui.WebGui;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.List;
 import java.util.TreeMap;
 
 import me.heldplayer.ModeratorGui.ModeratorGui;
 import me.heldplayer.ModeratorGui.ReportType;
 import me.heldplayer.ModeratorGui.tables.Bans;
 import me.heldplayer.ModeratorGui.tables.Demotions;
 import me.heldplayer.ModeratorGui.tables.Issues;
 import me.heldplayer.ModeratorGui.tables.Lists;
 import me.heldplayer.ModeratorGui.tables.Promotions;
 import me.heldplayer.ModeratorGui.tables.Unbans;
 
 import org.json.simple.JSONObject;
 
 import com.avaje.ebean.Expression;
 import com.avaje.ebean.ExpressionFactory;
 import com.avaje.ebean.Query;
 
 public class ReporterResponse extends WebResponse {
 	private String request;
 	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
 			"MM-dd-yyyy");
 
 	private final boolean issues;
 	private final boolean promotions;
 	private final boolean demotions;
 	private final boolean bans;
 	private final boolean unbans;
 
 	public ReporterResponse(String listFlags, String request)
 			throws IOException {
 		super();
 		this.request = request;
 
 		issues = listFlags.contains("i");
 		promotions = listFlags.contains("p");
 		demotions = listFlags.contains("d");
 		bans = listFlags.contains("b");
 		unbans = listFlags.contains("u");
 	}
 
 	@Override
 	public WebResponse writeResponse(RequestFlags flags) throws IOException {
 		dop.writeBytes("HTTP/1.0 200 Ok\r\n");
 		dop.writeBytes("Connection: close\r\n");
 		dop.writeBytes("Server: ModeratorGui\r\n");
 		dop.writeBytes("Content-Type: text/plain\r\n");
 		dop.writeBytes("\r\n");
 
 		Integer i = 0;
 
 		TreeMap<Integer, String> map = new TreeMap<Integer, String>();
 
 		Query<Lists> eLists = ModeratorGui.instance.getDatabase().find(
 				Lists.class);
 
 		ExpressionFactory factory = eLists.getExpressionFactory();
 
 		Expression lastExpr = null;
 
 		if (!issues && !bans && !unbans && !promotions && !demotions) {
 			lastExpr = factory.ne("type", 0);
 		}
 
 		if (issues) {
 			lastExpr = factory.eq("type", 1);
 		}
 
 		if (bans) {
 			if (lastExpr != null) {
 				lastExpr = factory.or(lastExpr, factory.eq("type", 2));
 			} else {
 				lastExpr = factory.eq("type", 2);
 			}
 		}
 
 		if (unbans) {
 			if (lastExpr != null) {
 				lastExpr = factory.or(lastExpr, factory.eq("type", 3));
 			} else {
 				lastExpr = factory.eq("type", 3);
 			}
 		}
 
 		if (promotions) {
 			if (lastExpr != null) {
 				lastExpr = factory.or(lastExpr, factory.eq("type", 4));
 			} else {
 				lastExpr = factory.eq("type", 4);
 			}
 		}
 
 		if (demotions) {
 			if (lastExpr != null) {
 				lastExpr = factory.or(lastExpr, factory.eq("type", 5));
 			} else {
 				lastExpr = factory.eq("type", 5);
 			}
 		}
 
 		if (lastExpr != null) {
 			lastExpr = factory.and(lastExpr,
 					factory.like("reporter", request + "%"));
 		} else {
 			lastExpr = factory.like("reporter", request + "%");
 		}
 
 		List<Lists> lists = eLists.where(lastExpr).setMaxRows(500).orderBy(
 				"id DESC").findList();
 
 		for (Lists list : lists) {
 			int id = list.getReportId();
 			ReportType type = ReportType.getType(list.getType());
 
 			String result = "";
 			switch (type) {
 			case ISSUE:
 				Issues issue = ModeratorGui.instance.getDatabase().find(
 						Issues.class).where().eq("id", id).findUnique();
 
 				result = "{ ";
 				result += "type: \"issue\", ";
				result += "closed: " + (issue.isClosed() ? "true" : "false") + ", ";
 				result += "reporter: \"" + JSONObject.escape(issue.getReporter()) + "\", ";
 				result += "reported: \"" + JSONObject.escape(issue.getReported()) + "\", ";
 				result += "time: \"" + JSONObject.escape(dateFormat.format(Long.valueOf(issue.getTimestamp()))) + "\", ";
 				result += "reason: \"" + JSONObject.escape(issue.getIssue()) + "\"";
 				result += " }";
 
 				map.put(i++, result);
 				break;
 			case BAN:
 				Bans ban = ModeratorGui.instance.getDatabase().find(Bans.class).where().eq(
 						"id", id).findUnique();
 
 				result = "{ ";
 				result += "type: \"ban\", ";
 				result += "reporter: \"" + JSONObject.escape(ban.getBanner()) + "\", ";
 				result += "reported: \"" + JSONObject.escape(ban.getBanned()) + "\", ";
 				result += "time: \"" + JSONObject.escape(dateFormat.format(Long.valueOf(ban.getTimestamp()))) + "\", ";
 				result += "reason: \"" + JSONObject.escape(ban.getReason()) + "\"";
 				result += " }";
 
 				map.put(i++, result);
 				break;
 			case UNBAN:
 				Unbans unban = ModeratorGui.instance.getDatabase().find(
 						Unbans.class).where().eq("id", id).findUnique();
 
 				result = "{ ";
 				result += "type: \"unban\", ";
 				result += "reporter: \"" + JSONObject.escape(unban.getUnbanner()) + "\", ";
 				result += "reported: \"" + JSONObject.escape(unban.getUnbanned()) + "\", ";
 				result += "time: \"" + JSONObject.escape(dateFormat.format(Long.valueOf(unban.getTimestamp()))) + "\", ";
 				result += "reason: \"" + JSONObject.escape(unban.getReason()) + "\"";
 				result += " }";
 
 				map.put(i++, result);
 				break;
 			case PROMOTE:
 				Promotions promote = ModeratorGui.instance.getDatabase().find(
 						Promotions.class).where().eq("id", id).findUnique();
 
 				result = "{ ";
 				result += "type: \"promote\", ";
 				result += "reporter: \"" + JSONObject.escape(promote.getPromoter()) + "\", ";
 				result += "reported: \"" + JSONObject.escape(promote.getPromoted()) + "\", ";
 				result += "time: \"" + JSONObject.escape(dateFormat.format(Long.valueOf(promote.getTimestamp()))) + "\", ";
 				result += "prev: \"" + JSONObject.escape(promote.getPrevRank()) + "\", ";
 				result += "new: \"" + JSONObject.escape(promote.getNewRank()) + "\", ";
 				result += "reason: \"" + JSONObject.escape(promote.getReason()) + "\"";
 				result += " }";
 
 				map.put(i++, result);
 				break;
 			case DEMOTE:
 				Demotions demote = ModeratorGui.instance.getDatabase().find(
 						Demotions.class).where().eq("id", id).findUnique();
 
 				result = "{ ";
 				result += "type: \"demote\", ";
 				result += "reporter: \"" + JSONObject.escape(demote.getDemoter()) + "\", ";
 				result += "reported: \"" + JSONObject.escape(demote.getDemoted()) + "\", ";
 				result += "time: \"" + JSONObject.escape(dateFormat.format(Long.valueOf(demote.getTimestamp()))) + "\", ";
 				result += "prev: \"" + JSONObject.escape(demote.getPrevRank()) + "\", ";
 				result += "new: \"" + JSONObject.escape(demote.getNewRank()) + "\", ";
 				result += "reason: \"" + JSONObject.escape(demote.getReason()) + "\"";
 				result += " }";
 
 				map.put(i++, result);
 				break;
 			default:
 				map.put(i++, "");
 				break;
 			}
 		}
 
 		String result = "[ ";
 		for (Integer I = 0; I < map.size(); I++) {
 			result += (I != 0 ? ", " : "") + map.get(I);
 		}
 		result += " ]";
 
 		dop.writeBytes(result);
 
 		return this;
 	}
 }
