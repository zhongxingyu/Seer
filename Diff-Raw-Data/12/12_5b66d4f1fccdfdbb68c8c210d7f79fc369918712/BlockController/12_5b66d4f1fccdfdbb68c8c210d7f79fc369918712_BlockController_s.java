 package controllers;
 
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 import models.Block;
 import models.Cong;
 import models.Record;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 import play.Logger;
 import play.data.Form;
 import play.libs.Json;
 import play.mvc.Result;
 
 public class BlockController extends BaseController {
 	static Form<Block> blockForm = form(Block.class);
 
 	public static Result index(String cong) {
 		if (Cong.find(cong) != null) {
 			return ok(views.html.index.render(Block.find(cong), cong));
 		} else {
 			return ok(views.html.first.render(cong));
 		}
 
 	}
 
 	public static Result newBlock() {
 		Form<Block> filledForm = blockForm.bindFromRequest();
 		Block blockToSave = filledForm.get();
 
 		try {
 			if (Block.find(blockToSave.cong, blockToSave.block, blockToSave.number.toString()) != null) {
 				return ok("ALREADY_EXISTS", "The same block already exists : " + blockToSave);
 			} else {
 				Block.create(blockToSave);
 			}
 
 			return ok("OK", "Block saved successfully.");
 		} catch (Exception e) {
 			return badRequest("ERROR", "Could not save block : " + filledForm);
 		}
 
 	}
 	
 	public static Result updateBlock() {
 		Form<Block> filledForm = blockForm.bindFromRequest();
 		Block blockToSave = filledForm.get();
 		Block existingBlock = Block.find(blockToSave.cong, blockToSave.block, blockToSave.number.toString());
 		try {
			// If the user is updating fields except cong, block and number, allow update
			if(blockToSave.isEqualCongBlockAndNumber(existingBlock)) {
 				Block.update(blockToSave);
			} else { // If the user is updating cong, block or number, and they clash, do not allow update
				return ok("ALREADY_EXISTS", "The same block already exists : " + blockToSave);
 			}
 			
 			return ok("OK", "Block saved successfully.");
 		} catch (Exception e) {
 			return badRequest("ERROR", "Could not update block : " + filledForm);
 		}
 		
 	}
 
 	public static Result findAll(String cong) {
 		try {
 			List<Block> blockList = Block.find(cong);
 			ObjectMapper mapper = new ObjectMapper();
 			ArrayNode data = mapper.createArrayNode();
 			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 			
 			for (Block block : blockList) {
 				ObjectNode blockNode = Json.newObject();
 				blockNode.put("id", block.id);
 				blockNode.put("cong", block.cong);
 				blockNode.put("block", block.block);
 				blockNode.put("number", block.number);
 				blockNode.put("recommendedWorkerNum", block.recommendedWorkerNum);
 				blockNode.put("lastWorkedDate", block.lastWorkedDate == null ? "" : format.format(block.lastWorkedDate));
 				blockNode.put("coord", block.coord);
 				data.add(blockNode);
 			}
 
 			return ok("OK", data);
 		} catch (Exception e) {
 			Logger.error(e.getMessage());
 			e.printStackTrace();
 			return badRequest("KO", e.getMessage());
 		}
 	}
 
 	public static Result find(String cong, String block) {
 		try {
 			List<Block> blockList = Block.find(cong, block);
 			ObjectMapper mapper = new ObjectMapper();
 			ArrayNode data = mapper.createArrayNode();
 			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 
 			for (Block b : blockList) {
 				ObjectNode blockNode = Json.newObject();
 				blockNode.put("id", b.id);
 				blockNode.put("cong", b.cong);
 				blockNode.put("block", b.block);
 				blockNode.put("number", b.number);
 				blockNode.put("recommendedWorkerNum", b.recommendedWorkerNum);
 				blockNode.put("lastWorkedDate", b.lastWorkedDate == null ? "" : format.format(b.lastWorkedDate));
 				blockNode.put("coord", b.coord);
 				data.add(blockNode);
 			}
 
 			return ok("OK", data);
 		} catch (Exception e) {
 			Logger.error(e.getMessage());
 			
 			return badRequest("KO", e.getMessage());
 		}
 	}
 
 	public static Result deleteBlock(String cong, String block) {
 		try {
 			String blockName = block.split("-")[0];
 			String blockNumber = block.split("-")[1];
 			Block.delete(cong, blockName, blockNumber);
 			Record.delete(cong, blockName, blockNumber);
 			return ok("OK", cong + ":" + block + " deleted successfully.");
 		} catch (Exception e) {
 			return ok("KO", cong + ":" + block + " not deleted :(");
 		}
 
 	}
 
 }
