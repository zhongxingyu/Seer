 package org.bitpipeline.lib.friendlyjson;
 
 import static org.junit.Assert.assertEquals;
 
 import org.bitpipeline.lib.friendlyjson.complexdata.EquipmentBoard;
 import org.bitpipeline.lib.friendlyjson.complexdata.EquipmentBrand;
 import org.bitpipeline.lib.friendlyjson.complexdata.EquipmentCollection;
 import org.bitpipeline.lib.friendlyjson.complexdata.FinFittingType;
 import org.bitpipeline.lib.friendlyjson.complexdata.WindsurfClass;
 import org.json.JSONObject;
 import org.junit.Test;
 
 public class EquipmentTest {
 
 	@Test
 	public void testEquipmentEntities () throws JSONMappingException {
 		EquipmentBrand brand = new EquipmentBrand ("Brand", null, "This is a BRAND!");
 		EquipmentCollection coll = new EquipmentCollection ("2012", null, "2012 collection", brand);
 		EquipmentBoard board = new EquipmentBoard (
 				"Beginner", coll, null, "A beginners board",
 				new WindsurfClass[] {WindsurfClass.Kids, WindsurfClass.Beginner},
 				new EquipmentBoard.BoardSpecs (150f, 2.70f, 0.70f, 2.0f,  8.0f, FinFittingType.DaggerBoard, FinFittingType.PowerBox),
 				new EquipmentBoard.BoardSpecs (155f, 2.70f, 0.75f, 2.0f,  8.0f, FinFittingType.DaggerBoard, FinFittingType.PowerBox),
 				new EquipmentBoard.BoardSpecs (160f, 2.75f, 0.75f, 2.0f, 10.0f, FinFittingType.DaggerBoard, FinFittingType.PowerBox),
 				new EquipmentBoard.BoardSpecs (175f, 2.75f, 0.80f, 2.0f, 10.0f, FinFittingType.DaggerBoard, FinFittingType.PowerBox),
 				new EquipmentBoard.BoardSpecs (190f, 2.80f, 0.80f, 2.0f, 10.0f, FinFittingType.DaggerBoard, FinFittingType.PowerBox),
 				new EquipmentBoard.BoardSpecs (205f, 2.80f, 0.85f, 2.0f, 10.0f, FinFittingType.DaggerBoard, FinFittingType.PowerBox),
 				new EquipmentBoard.BoardSpecs (220f, 2.85f, 0.85f, 2.0f, 10.0f, FinFittingType.DaggerBoard, FinFittingType.PowerBox),
 				new EquipmentBoard.BoardSpecs (240f, 2.90f, 0.85f, 2.0f, 10.0f, FinFittingType.DaggerBoard, FinFittingType.PowerBox),
 				new EquipmentBoard.BoardSpecs (250f, 2.90f, 0.90f, 2.0f, 10.0f, FinFittingType.DaggerBoard, FinFittingType.PowerBox));
 
//		brand.putInfo ("Founded", "2012");
 //		System.out.println ("board : " + board.toString (4));
 		
 		JSONObject json = board.toJson ();
 		EquipmentBoard copy = new EquipmentBoard (json);
 
 		assertEquals (board.toString (), copy.toString ());
 	}
 
 }
