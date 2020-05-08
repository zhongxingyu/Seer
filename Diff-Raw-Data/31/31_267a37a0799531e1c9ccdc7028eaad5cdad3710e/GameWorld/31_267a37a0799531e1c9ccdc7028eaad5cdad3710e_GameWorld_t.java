 package sfs2x.extensions.projectsasha.game.entities;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import sfs2x.extensions.projectsasha.game.entities.gateways.*;
 
 public class GameWorld 
 {
 	public int id;
 	public Region[] regions;
 	public static Map<String,Gateway> gateways = new HashMap<String,Gateway>();
 	
 	public GameWorld(int id)
 	{
 		this.id = id;
 	
 		gateways.put("iceland",				new Base(null, "nome", "Iceland"));
 		gateways.put("great britain",		new Base(null, "nome", "Great Britain"));
 		gateways.put("scandinavia", 		new Base(null, "nome", "Scandinavia"));
 		gateways.put("northern europe",		new Base(null, "nome", "Northern Europe"));
 		gateways.put("ukraine", 			new Base(null, "nome", "Ukraine"));
 		gateways.put("western europe",		new Base(null, "nome", "Western Europe"));
 		gateways.put("southern europe",		new Base(null, "nome", "Southern Europe"));
 		gateways.put("north africa",		new Base(null, "nome", "Northern Europe"));
 		gateways.put("egypt",				new Base(null, "nome", "Egypt"));
 		gateways.put("east africa",			new Base(null, "nome", "East Africa"));
 		gateways.put("congo",				new Base(null, "nome", "Congo"));
 		gateways.put("madagascar",			new Base(null, "nome", "Madagascar"));
		gateways.put("south africa",		new Base(null, "nome", "South Africa"));
 		gateways.put("middle east",			new Base(null, "nome", "Middle East"));
 		gateways.put("afghanistan",			new Base(null, "nome", "Afghanistan"));
 		gateways.put("india",				new Base(null, "nome", "India"));
 		gateways.put("siam",				new Base(null, "nome", "Siam"));
 		gateways.put("china",				new Base(null, "nome", "China"));
 		gateways.put("ural",				new Base(null, "nome", "Ural"));
 		gateways.put("siberia",				new Base(null, "nome", "Siberia"));
 		gateways.put("mongolia",			new Base(null, "nome", "Mongolia"));
 		gateways.put("irkutsk",				new Base(null, "nome", "Irkutsk"));
 		gateways.put("yakutsk",				new Base(null, "nome", "Yakutsk"));
 		gateways.put("kamchatka",			new Base(null, "nome", "Kamchatka"));
 		gateways.put("japan",				new Base(null, "nome", "Japan"));
 		gateways.put("indonesia",			new Base(null, "nome", "Indonesia"));
 		gateways.put("western australia", 	new Base(null, "nome", "Western Australia"));
 		gateways.put("eastern australia", 	new Base(null, "nome", "Eastern Australia"));
 		gateways.put("new guinea", 			new Base(null, "nome", "New Guinea"));
 		gateways.put("alaska", 				new Base(null, "nome", "Alaska"));
 		gateways.put("north west territory",new Base(null, "nome", "North West Territory"));
 		gateways.put("alberta", 			new Base(null, "nome", "Alberta"));
 		gateways.put("western usa", 		new Base(null, "nome", "Western USA"));
 		gateways.put("eastern usa", 		new Base(null, "nome", "Eastern USA"));
 		gateways.put("ontario", 			new Base(null, "nome", "Ontario"));
 		gateways.put("central america", 	new Base(null, "nome", "Central America"));
 		gateways.put("quebec",				new Base(null, "nome", "Quebec"));
 		gateways.put("greenland",			new Base(null, "nome", "Greenland"));
 		gateways.put("venezuela",			new Base(null, "nome", "Venezuela"));
 		gateways.put("peru",				new Base(null, "nome", "peru"));
 		gateways.put("brazil",				new Base(null, "nome", "Brazil"));
 		gateways.put("argentina",			new Base(null, "nome", "Argentina"));	
 		
 		gateways.get("iceland").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("greenland"), 
 			gateways.get("great britain")
 		});
 		gateways.get("great britain").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("island"), 
 			gateways.get("western europe"),
 			gateways.get("northern europe")
 		});
 		gateways.get("scandinavia").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("northern europe"), 
 			gateways.get("ukraine")
 		});
 		gateways.get("northern europe").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("scandinavia"), 
 			gateways.get("great britain"),
 			gateways.get("ukraine"),
 			gateways.get("western europe"),
 			gateways.get("southern europe")
 		});
 		gateways.get("ukraine").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("scandinavia"), 
 			gateways.get("northern europe"),
 			gateways.get("southern europe"),
 			gateways.get("middle east"),
 			gateways.get("ural"),
 			gateways.get("afghanistan")
 		});
 		gateways.get("western europe").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("northern europe"), 
 			gateways.get("great britain"),
 			gateways.get("southern europe"),
 			gateways.get("north africa")
 		});
 		gateways.get("southern europe").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("western europe"), 
 			gateways.get("northen europe"),
 			gateways.get("ukraine"),
 			gateways.get("middle east"),
 			gateways.get("egypt"),
 			gateways.get("north africa"),
 		});
 		gateways.get("north africa").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("western europe"), 
 			gateways.get("southern europe"),
 			gateways.get("egypt"),
 			gateways.get("east africa"),
 			gateways.get("congo"),
 			gateways.get("brazil"),
 		});
 		gateways.get("egypt").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("north africa"), 
 			gateways.get("southern europe"),
 			gateways.get("middle east"),
 			gateways.get("east africa")
 		});
 		gateways.get("east africa").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("north africa"), 
 			gateways.get("south africa"),
 			gateways.get("egypt"),
 			gateways.get("congo"),
 			gateways.get("madagascar"),
 			gateways.get("middle east"),
 		});
 		gateways.get("congo").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("north africa"), 
 			gateways.get("south africa"),
 			gateways.get("east africa")
 		});
 		gateways.get("south africa").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("madagascar"), 
 			gateways.get("east africa"),
 			gateways.get("congo")
 		});
 		gateways.get("madagascar").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("south africa"), 
 			gateways.get("east africa")
 		});
 		gateways.get("middle east").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("ukraine"), 
 			gateways.get("east africa"),
 			gateways.get("southern europe"),
 			gateways.get("egypt"),
 			gateways.get("afghanistan"),
 			gateways.get("india")
 		});
 		gateways.get("afghanistan").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("ukraine"), 
 			gateways.get("middle east"),
 			gateways.get("china"),
 			gateways.get("ural"),
 			gateways.get("india")
 		});
 		gateways.get("india").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("middle east"), 
 			gateways.get("siam"),
 			gateways.get("china"),
 			gateways.get("afghanistan"),
 		});
 		gateways.get("siam").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("indonesia"), 
 			gateways.get("china"),
 			gateways.get("india")
 		});
 		gateways.get("china").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("siam"), 
 			gateways.get("india"),
 			gateways.get("afghanistan"),
 			gateways.get("ural"),
 			gateways.get("siberia"),
 			gateways.get("mongolia")
 		});
 		gateways.get("ural").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("ukraine"), 
 			gateways.get("siberia"),
 			gateways.get("china"),
 			gateways.get("afghanistan")
 		});
 		gateways.get("siberia").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("ural"), 
 			gateways.get("china"),
 			gateways.get("mongolia"),
 			gateways.get("irkutsk"),
 			gateways.get("yakutsk")
 		});
 		gateways.get("mongolia").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("china"), 
 			gateways.get("siberia"),
 			gateways.get("irkutsk"),
 			gateways.get("kamchatka"),
 			gateways.get("japan")
 		});
 		gateways.get("irkutsk").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("mongolia"), 
 			gateways.get("siberia"),
 			gateways.get("yakutsk"),
 			gateways.get("kamchatka")
 		});
 		gateways.get("yakutsk").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("siberia"), 
 			gateways.get("irkutsk"),
 			gateways.get("kamchatka")
 		});
 		gateways.get("kamchatka").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("yakutsk"), 
 			gateways.get("irkutsk"),
 			gateways.get("mongolia"),
 			gateways.get("japan"),
 			gateways.get("alaska"),
 		});
 		gateways.get("japan").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("kamchatka"), 
 			gateways.get("mongolia")
 		});
 		gateways.get("indonesia").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("western australia"), 
 			gateways.get("new guinea"),
 			gateways.get("siam")
 
 		});
 		gateways.get("western australia").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("indonesia"), 
 			gateways.get("new guinea"),
 			gateways.get("eastern australia")
 		});
 		gateways.get("eastern australia").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("new guinea"), 
 			gateways.get("western australia")
 		});
 		gateways.get("new guinea").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("western australia"), 
 			gateways.get("indonesia"),
 			gateways.get("eastern australia")
 		});
 		gateways.get("alaska").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("kamcatcha"), 
 			gateways.get("north west territory"),
 			gateways.get("alberta")
 		});
 		gateways.get("alberta").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("alaska"), 
 			gateways.get("north west territory"),
 			gateways.get("ontario"),
 			gateways.get("western usa")
 		});
 		gateways.get("western usa").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("alberta"), 
 			gateways.get("ontario"),
 			gateways.get("eastern usa"),
 			gateways.get("central america")
 		});
 		gateways.get("north west territory").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("alaska"), 
 			gateways.get("alberta"),
 			gateways.get("ontario"),
 			gateways.get("greenland")
 		});
 		gateways.get("central america").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("western usa"), 
 			gateways.get("venezuela"),
 			gateways.get("eastern usa")
 		});
 		gateways.get("eastern usa").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("quebec"), 
 			gateways.get("ontario"),
 			gateways.get("western usa"),
 			gateways.get("central america")
 		});
 		gateways.get("ontario").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("alberta"), 
 			gateways.get("quebec"),
 			gateways.get("eastern usa"),
 			gateways.get("north west territory"),
 			gateways.get("western usa"),
 			gateways.get("greenland")
 		});
 		gateways.get("quebec").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("greenland"), 
 			gateways.get("ontario"),
 			gateways.get("north west territory"),
 			gateways.get("iceland")
 		});
 		gateways.get("greenland").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("quebec"), 
 			gateways.get("ontario"),
 			gateways.get("north west territory"),
 			gateways.get("iceland")
 		});
 		gateways.get("venezuela").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("peru"), 
 			gateways.get("brazil"),
 			gateways.get("central america")
 		});
 		gateways.get("peru").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("venezuela"), 
 			gateways.get("brazil"),
 			gateways.get("argentina")
 		});
 		gateways.get("brazil").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("venezuela"), 
 			gateways.get("peru"),
 			gateways.get("argentina")
 		});
 		gateways.get("argentina").setNeighborhoods(new Gateway[]
 		{
 			gateways.get("brazil"), 
 			gateways.get("peru")
 		});
 
 		
 		regions = new Region[]
 			{
 				new Region("The Frozen Lands", new Gateway[]
 						{
 							gateways.get("greenland"), 
 							gateways.get("alaska"),
 							gateways.get("north west territory")
 						}),
 				new Region("Atlantic Coast", new Gateway[]
 						{
 							gateways.get("ontario"), 
 							gateways.get("quebec"),
 							gateways.get("eastern usa")
 						}),
 				new Region("Pacific Coast", new Gateway[]
 						{
 							gateways.get("alberta"), 
 							gateways.get("central america"),
 							gateways.get("western usa")
 						}),
 				new Region("The Andes", new Gateway[]
 						{
 							gateways.get("peru"), 
 							gateways.get("argentina")
 						}),
 				new Region("Amazon", new Gateway[]
 						{
 							gateways.get("brazil"), 
 							gateways.get("venezuela")
 						}),
 				new Region("The Great Kingdom", new Gateway[]
 						{
 							gateways.get("great britain"), 
 							gateways.get("iceland")
 						}),
 				new Region("The New World Order", new Gateway[]
 						{
 							gateways.get("ukraine"), 
 							gateways.get("scandinavia"),
 							gateways.get("northern europe")
 						}),
 				new Region("The Mediterranean Confederation", new Gateway[]
 						{
 							gateways.get("western europe"), 
 							gateways.get("southern europe")
 						}),
 				new Region("The Desert", new Gateway[]
 						{
 							gateways.get("north africa"), 
 							gateways.get("east africa"),
 							gateways.get("egypt")
 						}),
 				new Region("The Jungle", new Gateway[]
 						{
 							gateways.get("central africa"), 
 							gateways.get("south africa"),
 							gateways.get("madagascar")
 						}),
 				new Region("The Gate to the West", new Gateway[]
 						{
 							gateways.get("japan"), 
 							gateways.get("kamchatka"),
 							gateways.get("mongolia")
 						}),
 				new Region("The Russian Rejects", new Gateway[]
 						{
 							gateways.get("siberia"), 
 							gateways.get("irkutsk"),
 							gateways.get("yakutsk")
 						}),
 				new Region("The Harabs", new Gateway[]
 						{
 							gateways.get("middle east"), 
 							gateways.get("ural"),
 							gateways.get("afghanistan")
 						}),
 				new Region("Buddhack Lodge", new Gateway[]
 						{
 							gateways.get("india"), 
 							gateways.get("china"),
 							gateways.get("siam")
 						}),
 				new Region("Austrislands", new Gateway[]
 						{
 							gateways.get("new guinea"), 
 							gateways.get("indonesia")
 						}),
 				new Region("The Giant Spiders Land", new Gateway[]
 						{
 							gateways.get("western australia"), 
 							gateways.get("eastern australia")
 						}),
 			};
 	}
 }
