 package com.mbcdev.nextluas.constants;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.mbcdev.nextluas.model.LocationBuilder;
 import com.mbcdev.nextluas.model.StopInformationModel;
 
 public class StopConstants {
 	
 	public static final String BASE_URL = "http://www.luas.ie/luaspid.html?get=";
 	
 	public static CharSequence[] getStopNameArray(List<StopInformationModel> list) {
 		
 		CharSequence[] names = new CharSequence[list.size()];
 		
 		int i = 0;
 		
 		for (StopInformationModel model: list) {
 			names[i++] = model.getDisplayName();
 		}
 		
 		return names;
 	}
 	
 	private static final List<StopInformationModel> redStops = new ArrayList<StopInformationModel>(30);
 	
 	
 	private static final List<StopInformationModel> greenStops = new ArrayList<StopInformationModel>(30);
 	
 	static {
 		
 
 		
 		/**
 		 * Saggart
 		 */
 		redStops.add(
 			new StopInformationModel(
 				"Saggart", 
 				new LocationBuilder().latitude(53.284742).longitude(-6.438106).location(),0
 			)
 		);
 		
 		/**
 		 * Fortunestown
 		 */
 		redStops.add(
 			new StopInformationModel(
 				"Fortunestown", 
 				new LocationBuilder().latitude(53.28421).longitude(-6.424727).location(),1
 			)
 		);
 		
 		/**
 		 * Citywest Campus
 		 */
 		redStops.add(
 			new StopInformationModel(
 				"Citywest Campus", 
 				new LocationBuilder().latitude(53.287827).longitude(-6.418998).location(),2
 			)
 		);
 		
 		/**
 		 * Cheeverstown
 		 */
 		redStops.add(
 			new StopInformationModel(
 				"Cheeverstown", 
 				new LocationBuilder().latitude(53.291008).longitude(-6.406928).location(),3
 			)
 		);
 
 		/**
 		 * Fettercairn
 		 */
 		redStops.add(
 			new StopInformationModel(
 				"Fettercairn", 
 				new LocationBuilder().latitude(53.293586).longitude(-6.395642).location(),4
 			)
 		);
 		
 		/**
 		 * Tallaght
 		 */
 		redStops.add(
 			new StopInformationModel(
 				"Tallaght", 
 				new LocationBuilder().latitude(53.287718).longitude(-6.373658).location(),5
 			)
 		);
 		
 		/**
 		 * Hospital
 		 */
 		redStops.add(
 			new StopInformationModel(
 				"Hospital", 
 				new LocationBuilder().latitude(53.289636).longitude(-6.379259).location(),5+1
 			)
 		);
 		
 		/**
 		 * Cookstown
 		 */
 		redStops.add(
 			new StopInformationModel(
 				"Cookstown", 
 				new LocationBuilder().latitude(53.29878).longitude(-6.386018).location(),5+2
 			)
 		);
 		
 		/**
 		 * Belgard
 		 * 53.299306,-6.374852
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Belgard", 
 					new LocationBuilder().latitude(53.299306).longitude(-6.374852).location(),5+3
 				)
 			);
 		
 		/**
 		 * Kingswood
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Kingswood", 
 					new LocationBuilder().latitude(53.30246).longitude(-6.36861).location(),5+4
 				)
 			);
 		
 		/**
 		 * Red Cow
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Red Cow", 
 					new LocationBuilder().latitude(53.316781).longitude(-6.369839).location(),5+5
 				)
 			);
 		
 		/**
 		 * Kylemore
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Kylemore", 
 					new LocationBuilder().latitude(53.326673).longitude(-6.343601).location(),5+6
 				)
 			);
 		
 		/**
 		 * Bluebell
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Bluebell", 
 					new LocationBuilder().latitude(53.329281).longitude(-6.33401).location(),5+7
 				)
 			);
 		
 		/**
 		 * Blackhorse 53.334201,-6.32791
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Blackhorse", 
 					new LocationBuilder().latitude(53.334201).longitude(-6.32791).location(),5+8
 				)
 			);
 		
 		/**
 		 * Drimnagh 53.335355,-6.318362
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Drimnagh", 
 					new LocationBuilder().latitude(53.335355).longitude(-6.318362).location(),5+9
 				)
 			);
 		
 		/**
 		 * Goldenbridge 53.335899,-6.313663
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Goldenbridge", 
 					new LocationBuilder().latitude(53.335899).longitude(-6.313663).location(),5+10
 				)
 			);
 		
 		/**
 		 * Suir Road 53.336658,-6.307279
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Suir Road",
 					new LocationBuilder().latitude(53.336658).longitude(-6.307279).location(),5+11
 				)
 			);
 		
 		/**
 		 * Rialto 53.337863,-6.297505
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Rialto",
 					new LocationBuilder().latitude(53.337863).longitude(-6.297505).location(),5+12
 				)
 			);
 		
 		/**
 		 * Fatima 53.338378,-6.292795
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Fatima", 
 					new LocationBuilder().latitude(53.338378).longitude(-6.292795).location(),5+13
 				)
 			);
 		
 		/**
 		 * James 53.342103,-6.293503
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"James's", 
 					new LocationBuilder().latitude(53.342103).longitude(-6.293503).location(),5+14
 				)
 			);
 		
 		/**
 		 * Heuston 53.346603,-6.291872
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Heuston",
 					new LocationBuilder().latitude(53.346603).longitude(-6.291872).location(),5+15
 				)
 			);
 		
 		/**
 		 * Museum 53.347877,-6.286776
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Museum",
 					new LocationBuilder().latitude(53.347877).longitude(-6.286776).location(),5+16
 				)
 			);
 		
 		/**
 		 * Smithfield 53.347154,-6.278198
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Smithfield",
 					new LocationBuilder().latitude(53.347154).longitude(-6.278198).location(),5+17
 				)
 			);
 		
 		/**
 		 * Four Courts 53.346798,-6.272925
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Four Courts",
 					new LocationBuilder().latitude(53.346798).longitude(-6.272925).location(),5+18
 				)
 			);
 		
 		/**
 		 * Jervis 53.347689,-6.266112
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Jervis",
 					new LocationBuilder().latitude(53.347689).longitude(-6.266112).location(),5+19
 				)
 			);
 		
 		/**
 		 * Abbey Street 53.348475,-6.258999
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Abbey Street",
 					new LocationBuilder().latitude(53.348475).longitude(-6.258999).location(),5+20
 				)
 			);
 		
 		/**
		 * Busras 53.350289,-6.252712
 		 */
 		redStops.add(
 				new StopInformationModel(
					"Busras",
 					new LocationBuilder().latitude(53.350289).longitude(-6.252712).location(),5+21
 				)
 			);
 		
 		/**
 		 * Connolly 53.351339,-6.249971
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Connolly",
 					new LocationBuilder().latitude(53.351339).longitude(-6.249971).location(),5+22
 				)
 			);
 		
 		/**
 		 * George's Dock 53.349459,-6.24642
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"George's Dock",
 					new LocationBuilder().latitude(53.349459).longitude(-6.24642).location(),5+23
 				)
 			);
 		
 		/**
 		 * Mayor Square 53.349258,-6.24363
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Mayor Square - NCI",
 					new LocationBuilder().latitude(53.349258).longitude(-6.24363).location(),5+24
 				)
 			);
 		
 		/**
 		 * Spencer Dock 53.348941,-6.237611
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"Spencer Dock",
 					new LocationBuilder().latitude(53.348941).longitude(-6.237611).location(),5+25
 				)
 			);
 		
 		/**
 		 * The Point 53.347509,-6.22884
 		 */
 		redStops.add(
 				new StopInformationModel(
 					"The Point",
 					new LocationBuilder().latitude(53.347509).longitude(-6.22884).location(),5+26
 				)
 			);
 	
 		/*********************************************************************************
 		 * G R E E N  L I N E
 		 *********************************************************************************/
 						
 		/**
 		 * SSG 53.339704,-6.261038
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"St. Stephen's Green",
 					new LocationBuilder().latitude(53.339704).longitude(-6.261038).location(),0
 				)
 			);
 		
 		/**
 		 * Harcourt
 		 * 53.33358
 		 * -6.262878
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Harcourt",
 					new LocationBuilder().latitude(53.33358).longitude(-6.262878).location(),1
 				)
 			);
 		
 		/**
 		 * Charlemont
 		 * 53.33071
 		 * -6.258747
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Charlemont",
 					new LocationBuilder().latitude(53.33071).longitude(-6.258747).location(),2
 				)
 			);
 		
 		/**
 		 * Ranelagh
 		 * 53.3263
 		 * -6.256215
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Ranelagh",
 					new LocationBuilder().latitude(53.3263).longitude(-6.256215).location(),3
 				)
 			);
 		
 		/**
 		 * Beechwood
 		 * 53.320854
 		 * -6.254691
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Beechwood",
 					new LocationBuilder().latitude(53.320854).longitude(-6.254691).location(),4
 				)
 			);
 		
 		/**
 		 * Cowper
 		 * 53.316478
 		 * -6.253503
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Cowper",
 					new LocationBuilder().latitude(53.316478).longitude(-6.253503).location(),5
 				)
 			);
 		
 		/**
 		 * Milltown
 		 * 53.30986
 		 * -6.251746
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Milltown",
 					new LocationBuilder().latitude(53.30986).longitude(-6.251746).location(),6
 				)
 			);
 		
 		/**
 		 * Windy Arbour
 		 * 53.301558
 		 * -6.250818
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Windy Arbour",
 					new LocationBuilder().latitude(53.301558).longitude(-6.250818).location(),7
 				)
 			);
 		
 		/**
 		 * Dundrum
 		 * 53.293764
 		 * -6.246959
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Dundrum",
 					new LocationBuilder().latitude(53.293764).longitude(-6.246959).location(),8
 				)
 			);
 		
 		/**
 		 * Balally
 		 * 53.286123
 		 * -6.236807
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Balally",
 					new LocationBuilder().latitude(53.286123).longitude(-6.236807).location(),9
 				)
 			);
 		
 		/**
 		 * Kilmacud
 		 * 53.28301
 		 * -6.223996
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Kilmacud",
 					new LocationBuilder().latitude(53.28301).longitude(-6.223996).location(),10
 				)
 			);
 		
 		/**
 		 * Stillorgan
 		 * 53.279355
 		 * -6.210164
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Stillorgan",
 					new LocationBuilder().latitude(53.279355).longitude(-6.210164).location(),11
 				)
 			);
 		
 		/**
 		 * Sandyford
 		 * 53.277591
 		 * -6.20466
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Sandyford",
 					new LocationBuilder().latitude(53.277591).longitude(-6.20466).location(),12
 				)
 			);
 		
 		/**
 		 * Central Park
 		 * 53.270192
 		 * -6.20366
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Central Park",
 					new LocationBuilder().latitude(53.270192).longitude(-6.20366).location(),13
 				)
 			);
 		
 		/**
 		 * Glencairn
 		 * 53.267138
 		 * -6.209453
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Glencairn",
 					new LocationBuilder().latitude(53.267138).longitude(-6.209453).location(),14
 				)
 			);
 		
 		/**
 		 * The Gallops
 		 * 53.261087,-6.205983
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"The Gallops",
 					new LocationBuilder().latitude(53.261087).longitude(-6.205983).location(),15
 				)
 			);
 		
 		/**
 		 * Leopardstown Valley
 		 * 53.264725,-6.211009
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Leopardstown Valley",
 					new LocationBuilder().latitude(53.264725).longitude(-6.211009).location(),16
 				)
 			);
 		
 		/**
 		 * Ballyogan Wood
 		 * 53.25511,-6.184933
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Ballyogan Wood",
 					new LocationBuilder().latitude(53.25511).longitude(-6.184933).location(),17
 				)
 			);
 		
 		/**
 		 * Carrickmines
 		 * 53.25429,-6.171108
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Carrickmines",
 					new LocationBuilder().latitude(53.25429).longitude(-6.171108).location(),18
 				)
 			);
 		
 		/**
 		 * Laughanstown
 		 * 53.250936,-6.155852
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Laughanstown",
 					new LocationBuilder().latitude(53.250936).longitude(-6.155852).location(),19
 				)
 			);
 		
 		/**
 		 * Cherrywood
 		 * 53.244805,-6.145054
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Cherrywood",
 					new LocationBuilder().latitude(53.244805).longitude(-6.145054).location(),20
 				)
 			);
 		
 		/**
 		 * Brides Glen
 		 * 53.241752,-6.142741
 		 * 
 		 */
 		greenStops.add(
 				new StopInformationModel(
 					"Brides Glen",
 					new LocationBuilder().latitude(53.241752).longitude(-6.142741).location(),21
 				)
 			);
 		
 	}
 		
 	public static List<StopInformationModel> getRedStops() {
 		return redStops;
 	}
 	
 	public static List<StopInformationModel> getGreenStops() {
 		return greenStops;
 	}
 }
 
