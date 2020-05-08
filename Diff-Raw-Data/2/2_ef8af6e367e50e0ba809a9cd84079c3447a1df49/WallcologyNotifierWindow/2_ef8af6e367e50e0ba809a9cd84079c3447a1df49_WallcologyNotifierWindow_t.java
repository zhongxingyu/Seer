 package ltg.ps.phenomena.wallcology.windows;
 
 import ltg.ps.api.phenomena.Phenomena;
 import ltg.ps.api.phenomena.PhenomenaWindow;
 import ltg.ps.phenomena.wallcology.Wall;
 import ltg.ps.phenomena.wallcology.Wallcology;
 import ltg.ps.phenomena.wallcology.population_calculators.PopulationCalculator;
 
 public class WallcologyNotifierWindow extends PhenomenaWindow {
 	
 	private String wallId = null;
 	private String reqId = null;
 	private Wall w = null;
 	
 	public WallcologyNotifierWindow(String windowName) {
 		super(windowName);
 	}
 	
 	public void setWallId(String w) {
 		this.wallId = w;
 	}
 	
 	public void setReqParam(String w, String r) {
 		this.wallId = w;
 		this.reqId = r;
 	}
 
 	@Override
 	public String toXML() {
 		String xml = null;
 		if (w==null)
 			return xml;
 		int bluebugs = w.getPopulation().get("blueBug_s1") + w.getPopulation().get("blueBug_s2") 
 				+ w.getPopulation().get("blueBug_s3") + w.getPopulation().get("blueBug_s4");
		int greenbugs = w.getPopulation().get("greenBug_s1") + w.getPopulation().get("greenBug_s2");
 		int predator = w.getPopulation().get("fuzzPredator_s1") + w.getPopulation().get("fuzzPredator_s2"); 
 		xml =	"<getCount reqId=\"" + reqId + "\" wall=\"" + wallId + "\" >" + 
 					"<temperature>" + w.getTemperature() + "</temperature>" +
 					"<humidity>" + w.getHumidity() +"</humidity>" +
 					"<light>" + w.getLight() + "</light>" +
 					"<noiseStd>" + PopulationCalculator.noisePercent + "</noiseStd>" +
 					"<greenScum>" +
 						"<amount>" + w.getPopulation().get("greenScum") + "</amount>" +
 					"</greenScum>" +
 					"<fluffyMold>" +
 						"<amount>" + w.getPopulation().get("fluffyMold") + "</amount>" +
 					"</fluffyMold>" +
 					"<blueBug>" +
 						"<amount>" + bluebugs + "</amount>" +
 					"</blueBug>" +
 					"<greenBug>" +
 						"<amount>" + greenbugs + "</amount>" +
 					"</greenBug>" +
 					"<fuzzPredator>" +
 						"<amount>" + predator + "</amount>" +
 					"</fuzzPredator>" +
 				"</getCount>";
 		return xml;
 	}
 
 	
 	@Override
 	public void update(Phenomena p) {
 		if (wallId!=null)
 			w = ((Wallcology)p).getWall(wallId);
 	}
 }
