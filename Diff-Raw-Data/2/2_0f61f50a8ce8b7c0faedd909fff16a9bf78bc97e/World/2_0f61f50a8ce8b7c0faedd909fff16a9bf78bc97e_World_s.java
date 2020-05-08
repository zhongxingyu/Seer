 package objects;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.Vector;
 
 import util.annotations.Column;
 import util.annotations.Row;
 import util.annotations.StructurePattern;
 
 @StructurePattern("Bean Pattern")
 public class World {
 	private Player player;
 	private ItemTab itemTab;
 	private SceneTab sceneTab;
 	private EventTab eventTab;
 	private GameInfoTab gameInfoTab;
 //	private Vector<Event> events;
 	private Vector<Ctrl> ctrls;
 	
 	private Translator translator = new Translator();
 	
 	public World(){
 		player = new Player();
 		itemTab = new ItemTab();
 		sceneTab = new SceneTab();
 		eventTab = new EventTab();
 		gameInfoTab = new GameInfoTab();
 //		events = new Vector<Event>();
 		ctrls = new Vector<Ctrl>();
 	}
 
 //	public Player getPlayer() {
 //		return player;
 //	}
 	
 	@Row(0) @Column(0)
 	public ItemTab getItem(){
 		return itemTab;
 	}
 	
 	@Row(0) @Column(1)
 	public SceneTab getScene(){
 		return sceneTab;
 	}
 	
 	@Row(0) @Column(2)
 	public EventTab getEvent(){
 		return eventTab;
 	}
 
 	public GameInfoTab getGameInfo(){
 		return gameInfoTab;
 	}
 //	@Row(3) @Column(0)
 //	public Vector<Event> getEvents() {
 //		return events;
 //	}
 //	
 //	public void addEvent(Event event){
 //		//Check to make sure world does not already contain the event
 //		if(!events.contains(event)){
 //			events.add(event);
 //		}
 //		else
 //			System.out.println("World already contains "+event.getId());
 //	}
 //	
 //	public void removeEvent(Event event){
 //		int i=0;
 //		//Search for first occurrence of "event" and removes it from vector. We are guaranteed a single occurrence of
 //		//an event by addEvent(Event)
 //		while(!events.get(i).getId().equalsIgnoreCase(event.getId())){
 //			i++;
 //			if(i==events.size()){
 //				System.out.println("Event "+event.getId()+" was not found.");
 //				return;
 //			}
 //		}
 //		events.remove(event);
 //	}
 	
 	public void translate() throws FileNotFoundException{
 		File file = new File("world.json");
 		PrintWriter writer = new PrintWriter(file);
 	
 		translator.initialize(writer);
 		Iterator<Item> itemItr = itemTab.getItems().iterator();
 		if(itemItr.hasNext()){
 			writer.printf(",\r\n");
 		}
 		while(itemItr.hasNext()){
 			translator.itemTranslate(writer, itemItr.next());
 			if(itemItr.hasNext()){
 				writer.printf(",\r\n");
 			}
 		}
 		
 		Iterator<Scene> sceneItr = sceneTab.getScenes().iterator();
 		if(sceneItr.hasNext()){
 			writer.printf(",\r\n");
 		}
 		while(sceneItr.hasNext()){
 			translator.sceneTranslate(writer, sceneItr.next());
 			if(sceneItr.hasNext()){
 				writer.printf(",\r\n");
 			}
 		}
 		
 		Iterator<Event> eventItr = eventTab.getEvents().iterator();
		if(sceneItr.hasNext()){
 			writer.printf(",\r\n");
 		}
 		while(eventItr.hasNext()){
 			translator.eventTranslate(writer, eventItr.next());
 			if(eventItr.hasNext()){
 				writer.printf(",\r\n");
 			}
 		}
 		
 		writer.printf("\r\n]");
 		writer.close();
 	}
 	
 }
