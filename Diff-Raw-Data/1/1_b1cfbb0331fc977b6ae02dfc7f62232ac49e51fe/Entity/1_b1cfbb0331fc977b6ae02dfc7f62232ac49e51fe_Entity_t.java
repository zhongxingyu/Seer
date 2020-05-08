 package fearlesscode.model.entity;
 
 import fearlesscode.gui.*;
 import fearlesscode.model.core.*;
 import fearlesscode.model.physics.*;
 import fearlesscode.model.container.*;
 import fearlesscode.model.block.*;
 import fearlesscode.model.misc.*;
import fearlesscode.model.block.*;
 
 /**
  * Az Entity egy blokkon található statikus (nem mozgó) objektumot reprezentál,
  * minden leszármazottnak kötelessége viselkedést definiálni a játékossal való találkozásra.
  */
 public abstract class Entity implements Info, Collideable
 {
 	/**
 	 * Privát statikus számláló.
 	 */
 	private static int count;
 	
 	/**
 	 * Protected azonosító.
 	 */
 	protected int ID;
 	
 	/**
 	 * Referencia a Block-ra.
 	 */
 	protected Block block;
 
 	/**
 	 * PlayField referencia, a leszármazottak a viselkedés leírásának megkönnyítése
 	 * érdekében felhasználhatják.
 	 */
 	protected PlayField playField;
 
 	/**
 	 * A befoglaló konténer.
 	 */
 	protected EntityContainer container;
 
 	/**
 	 * Entity konstruktor
 	 * @param playField A tartalmazó playField referenciája.
 	 */
 	public Entity(PlayField playField)
 	{
 		this.playField = playField;
 		this.block = null;
 		count++;
 		this.ID = count;
 	}
 
 	/**
 	 * Visszaadja a tartalmazó objektumot.
 	 * @return Az objektum, amiben az Entity van.
 	 */
 	public EntityContainer getContainer()
 	{
 		return container;
 	}
 
 	/**
 	 * Beállítja a konténer objektumot.
 	 * @param con Az objektum, ami tartalmazza az Entity-t.
 	 */
 	public void setContainer(EntityContainer con)
 	{
 		container=con;
 	}
 
 	/**
 	 * A tartalmazó Block referenciáját beállító metódus.
 	 */
 	public void setBlock(Block block)
 	{
 		this.block=block;
 	}
 
 	/**
 	 * Kötelezően implementálandó metódus, a játékossal
 	 * való találkozás forgatókönyvét írja le.
 	 * @param p A Player tárolója.
 	 */
 	public abstract void meetPlayer(PlayerContainer p);
 
 	/**
 	 * Az azonosító gettere.
 	 */
 	public int getID()
 	{
 		return ID;
 	}
 
 	/**
 	 * A visitor pattern alapján egy BlockMatcherrel kommunikál. Alapértelmezetten kivételt dob,
 	 * Ezzel jelezve, hogy az illeszkedés vizsgálatában nincs szerepe.
 	 * @param matcher A kommunikációt kezdeményező objektum.
 	 */
 	public void accept(BlockMatcher matcher) throws Exception
 	{
 		throw new Exception();
 	}
 
 	public abstract EntityDrawer getEntityDrawer();
 }
