 package devflow.hoseotable;
 
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * <p>ä ¼ ϱ Class</p>
  * @author Ȱ輺
  */
 public class Chapel
 {
 	private JSONArray seatData ;
 	
 	public Chapel() {
 		try
 		{
 			seatData = new JSONArray("[{S:1,C:8,L:'A',LS:1},{S:17,C:8,L:'A',LS:9},{S:49,C:8,L:'A',LS:17},{S:83,C:9,L:'A',LS:25},{S:119,C:9,L:'A',LS:34},{S:155,C:9,L:'A',LS:43},{S:191,C:10,L:'A',LS:52},{S:231,C:10,L:'A',LS:62},{S:271,C:10,L:'A',LS:72},{S:311,C:11,L:'A',LS:82},{S:355,C:11,L:'A',LS:93},{S:399,C:11,L:'A',LS:104},{S:443,C:11,L:'A',LS:115},{S:487,C:12,L:'A',LS:126},{S:25,C:8,L:'B',LS:1},{S:57,C:9,L:'B',LS:9},{S:92,C:9,L:'B',LS:18},{S:128,C:9,L:'B',LS:27},{S:164,C:9,L:'B',LS:36},{S:201,C:10,L:'B',LS:45},{S:241,C:10,L:'B',LS:55},{S:281,C:10,L:'B',LS:65},{S:322,C:11,L:'B',LS:75},{S:366,C:11,L:'B',LS:86},{S:410,C:11,L:'B',LS:97},{S:454,C:11,L:'B',LS:108},{S:499,C:12,L:'B',LS:119},{S:33,C:8,L:'C',LS:1},{S:66,C:9,L:'C',LS:9},{S:101,C:9,L:'C',LS:18},{S:137,C:9,L:'C',LS:27},{S:173,C:9,L:'C',LS:36},{S:211,C:10,L:'C',LS:45},{S:251,C:10,L:'C',LS:55},{S:291,C:10,L:'C',LS:65},{S:333,C:11,L:'C',LS:75},{S:377,C:11,L:'C',LS:86},{S:421,C:11,L:'C',LS:97},{S:465,C:11,L:'C',LS:108},{S:511,C:12,L:'C',LS:119},{S:9,C:8,L:'D',LS:1},{S:41,C:8,L:'D',LS:9},{S:75,C:8,L:'D',LS:17},{S:110,C:9,L:'D',LS:25},{S:146,C:9,L:'D',LS:34},{S:182,C:9,L:'D',LS:43},{S:221,C:10,L:'D',LS:52},{S:261,C:10,L:'D',LS:62},{S:301,C:10,L:'D',LS:72},{S:344,C:11,L:'D',LS:82},{S:388,C:11,L:'D',LS:93},{S:432,C:11,L:'D',LS:104},{S:476,C:11,L:'D',LS:115},{S:523,C:12,L:'D',LS:126},{S:535,C:7,L:'E',LS:1},{S:581,C:8,L:'E',LS:8},{S:629,C:8,L:'E',LS:16},{S:677,C:8,L:'E',LS:24},{S:725,C:8,L:'E',LS:32},{S:773,C:9,L:'E',LS:40},{S:827,C:9,L:'E',LS:49},{S:881,C:9,L:'E',LS:58},{S:935,C:9,L:'E',LS:67},{S:989,C:9,L:'E',LS:76},{S:1043,C:10,L:'E',LS:85},{S:1103,C:10,L:'E',LS:95},{S:1163,C:10,L:'E',LS:105},{S:1223,C:9,L:'E',LS:115},{S:542,C:8,L:'F',LS:1},{S:589,C:8,L:'F',LS:9},{S:637,C:8,L:'F',LS:17},{S:685,C:8,L:'F',LS:25},{S:733,C:8,L:'F',LS:33},{S:782,C:9,L:'F',LS:41},{S:836,C:9,L:'F',LS:50},{S:890,C:9,L:'F',LS:59},{S:944,C:9,L:'F',LS:68},{S:998,C:9,L:'F',LS:77},{S:1053,C:10,L:'F',LS:86},{S:1113,C:10,L:'F',LS:96},{S:1173,C:10,L:'F',LS:106},{S:1232,C:8,L:'F',LS:116},{S:550,C:8,L:'G',LS:1},{S:597,C:8,L:'G',LS:9},{S:645,C:8,L:'G',LS:17},{S:693,C:8,L:'G',LS:25},{S:741,C:8,L:'G',LS:33},{S:791,C:9,L:'G',LS:41},{S:845,C:9,L:'G',LS:50},{S:899,C:9,L:'G',LS:59},{S:953,C:9,L:'G',LS:68},{S:1007,C:9,L:'G',LS:77},{S:1063,C:10,L:'G',LS:86},{S:1123,C:10,L:'G',LS:96},{S:1183,C:10,L:'G',LS:106},{S:1240,C:7,L:'G',LS:116},{S:558,C:8,L:'H',LS:1},{S:605,C:8,L:'H',LS:9},{S:653,C:8,L:'H',LS:17},{S:701,C:8,L:'H',LS:25},{S:749,C:8,L:'H',LS:33},{S:800,C:9,L:'H',LS:41},{S:854,C:9,L:'H',LS:50},{S:908,C:9,L:'H',LS:59},{S:962,C:9,L:'H',LS:68},{S:1016,C:9,L:'H',LS:77},{S:1073,C:10,L:'H',LS:86},{S:1133,C:10,L:'H',LS:96},{S:1193,C:10,L:'H',LS:106},{S:1247,C:7,L:'H',LS:116},{S:566,C:8,L:'I',LS:1},{S:613,C:8,L:'I',LS:9},{S:661,C:8,L:'I',LS:17},{S:709,C:8,L:'I',LS:25},{S:757,C:8,L:'I',LS:33},{S:809,C:9,L:'I',LS:41},{S:863,C:9,L:'I',LS:50},{S:917,C:9,L:'I',LS:59},{S:971,C:9,L:'I',LS:68},{S:1025,C:9,L:'I',LS:77},{S:1083,C:10,L:'I',LS:86},{S:1143,C:10,L:'I',LS:96},{S:1203,C:10,L:'I',LS:106},{S:1254,C:8,L:'I',LS:116},{S:574,C:7,L:'J',LS:1},{S:621,C:8,L:'J',LS:8},{S:669,C:8,L:'J',LS:16},{S:717,C:8,L:'J',LS:24},{S:765,C:8,L:'J',LS:32},{S:818,C:9,L:'J',LS:40},{S:872,C:9,L:'J',LS:49},{S:926,C:9,L:'J',LS:58},{S:980,C:9,L:'J',LS:67},{S:1034,C:9,L:'J',LS:76},{S:1093,C:10,L:'J',LS:85},{S:1153,C:10,L:'J',LS:95},{S:1213,C:10,L:'J',LS:105},{S:1262,C:9,L:'J',LS:115},]");
 			//   äð ¼ǥ  JSON  ۼ
 			/*
 			 *   S :  ȣ
 			 *   C : Ⱦ ¼ 
 			 *   L : ¼ 
 			 *   LS : ¼  ȣ
 			 */
 		}catch (JSONException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	/**
 	 * <p> ¼  ¼ ȯ. </p>
 	 * @author Ȱ輺
 	 * @return + ¼ String
 	 * @throws ã  ų,  ״  ǥ
 	 */
 	public String getSeat(int numbericSeat) {
 		try
 		{
 			for(int i = 0; i <= seatData.length(); i++ ){
 
 				JSONObject lData =  seatData.getJSONObject(i);
 				
 				int StartNumber = lData.getInt("S");
 				int LineCount = lData.getInt("C");
 				
				if( StartNumber <= numbericSeat && StartNumber+LineCount > numbericSeat ) {
 					String SeatLetter = lData.getString("L");
 					int LetterStart = lData.getInt("LS");
 					int offset = numbericSeat - StartNumber;
 					offset =  (LetterStart + offset);
 					return SeatLetter + offset;
 				}
 
 			}
 			return "ã   ";
 		}catch (JSONException e)
 		{
 			return "߻";
 		}
 
 	}
 }
