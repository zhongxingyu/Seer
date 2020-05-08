 package controllers;
 
 import java.util.Map;
 
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import models.Piece;
import play.Logger;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 public class AltAdmin  extends Controller {
 	private static final int passHash = "swiebbeest".hashCode();
 	private static JSONParser parser = new JSONParser();
 	
 	public static Result admin(){
 		return ok( views.html.main.render(views.html.altadmin.render(),Application.getMeta("admin")) );
 	}
 	
 	public static Result superTest(){
 		try {
 			JSONObject test = (JSONObject) parser.parse(request().body().asText());
 			return ok(test.get("Thumb").toString());
 		} catch (ParseException | NullPointerException e) {
 			return badRequest();
 		}
 		
 	}
 	
 	public static Result getNumberOfPieces(){
 		return ok(String.valueOf(Piece.find.findIds()));
 	}
 	
 	public static Result checkpass(){
 		if(Integer.parseInt(ctx().request().body().asFormUrlEncoded().get("password")[0]) == passHash){
 			return ok();
 		}else{
 			return forbidden();
 		}
 	}
 	
 	public static Result getPiece(long pieceNr){
 		//ArrayNode pieces = (ArrayNode) ctx().request().body().asJson();
 		
 		Piece piece = Piece.find.byId(pieceNr);
 		if(piece == null){
 			return badRequest();
 		}else{
 			return ok(piece.getJson().toJSONString());
 		}
 	}
 	
 	public static Result updatePiece(long pieceNr){
 		Piece piece = Piece.find.byId(pieceNr);
 		Map<String, String[]> data = ctx().request().body().asFormUrlEncoded();
 		
 		if(piece == null){
 			return badRequest();
 		}else{
 			if(Integer.parseInt(data.get("password")[0]) == passHash){
 				try {
 					piece.updateWithJson((JSONObject) parser.parse(data.get("json")[0]));
 					return ok();
 				} catch (ParseException e) {
 					return badRequest();
 				}
 			}else{
 				return forbidden();
 			}
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static Result addPiece(){
 		if(Integer.parseInt(ctx().request().body().asFormUrlEncoded().get("password")[0]) == passHash){
			Piece piece = Application.createExamplePiece("sier");
 			
 			/*try {
 				piece.updateWithJson((JSONObject) parser.parse(ctx().request().body().asText()));
 			} catch (ParseException e) {
 				Logger.error(e.getLocalizedMessage());
 			}*/
 			JSONObject pieceJson = piece.getJson();
 			pieceJson.put("itemId", piece.id.toString());
 			
			Logger.info(pieceJson.get("itemId").toString());
			
			return ok(piece.id.toString());
 		}else{
 			return forbidden();
 		}
 	}
 	
 	public static Result removePiece(long pieceNr){
 		Piece piece = Piece.find.byId(pieceNr);
 		if(piece == null){
 			return badRequest();
 		}else{
 			if(Integer.parseInt(ctx().request().body().asFormUrlEncoded().get("password")[0]) == passHash){
 				piece.delete();
 				return ok();
 			}else{
 				return forbidden();
 			}
 		}
 	}
 }
