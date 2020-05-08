 package pkg_Room;
 
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Set;
 
 import pkg_Characters.Bots;
 import pkg_Items.Beamer;
 import pkg_Items.Pierre;
 import pkg_Items.Item;
 import pkg_Items.ItemListe;
 import pkg_Items.Potion;
 
 //import java.util.Iterator;
 
 /**
  * Class Room - une salle ou un terrain dans le jeux d'aventure.
  * 
  * Cette classe fait parti du jeux " Water Games" "Water Games" est un jeux très
  * simple à prendre en main, c'est un jeux textuel.
  * 
  * Une salle représente un endroit dans le jeux. Chaque salle est connecté à une
  * autre grâce à des sorties. Les sorties sont libellé par les directions
  * suivantes: nord, sud, est, ouest, monter. Pour chaque direction saisie, le
  * jeux vous envoi vers la salle voisine ou vous renvoi null si aucune salle
  * voisine existe dans la direction voulu.
  * 
  */
 public class Room {
 	public String description;
 	// public Room northExit;
 	// public Room southExit;
 	// public Room eastExit;
 	// public Room westExit;
 	private HashMap<String, Room> exits;
 	// private HashMap<String, Item> items;
 	private ArrayList<Potion> potion;
 
 	// private HashMap<String, Room> room;
 	private String imageName;
 	private ItemListe items;
 	private String nomRoom;
 	
 	private HashMap<String, Bots> bots;
 
 	/**
 	 * Constructeur qui permet d'initialiser la description d'une salle. "
 	 * Exemple d'utilisation : "Une forêt" , "une belle cuisine"
 	 * 
 	 * @param description
 	 *            La description de la salle.
 	 */
 	public Room(String description, String image, String pNomRoom) {
 		this.description = description;
 		exits = new HashMap<String, Room>();
 		// items = new HashMap<String, Item>();
 		imageName = image;
 		items = new ItemListe();
 		potion = new ArrayList<Potion>();
 		nomRoom = pNomRoom;
 		
 		bots = new HashMap<String, Bots>();
 	}
 
 	// /**HashMap pour contenir les rooms, chaque room est lié à son nom
 	// *
 	// */
 	// public void setRoom(String nameRoom, Room currentRoom)
 	// {
 	// room.put(nameRoom, currentRoom);
 	// }
 
 	public void addBot(String nomBot, Bots bot)
 	{
 		bots.put(nomBot, bot);
 	}
 	
 	public void removeBot(String nomBot)
 	{
 		bots.remove(nomBot);
 	}
 
 	public void addItem(Pierre pierre)
 	{
 		items.putItem("EnderPearl", pierre);
 	}
 
 	/**
 	 * Ajouter un objet dans un HashMap
 	 */
 	public void addItem(String nomItem, Item item) {
 		items.putItem(nomItem, item);
 	}
 	
 	public void addItem(Beamer beamer)
 	{
 		items.putItem("beamer", beamer);
 	}
 
 	public void addPotion(final Potion pPotion) {
 		potion.add(pPotion);
 	}
 
 	/**
 	 * Cette méthode est pour vérifier si une potion appartient à l'ArrayList
 	 * des potions. Manques de meilleure idée, la méthode ici fait simplement
 	 * une boucle de comparaison entre le nom de potion passé en paramètre et
 	 * tous les noms de potion dans l'ArrayList.
 	 * 
 	 * C'est une méthode a priori moche donc à refaire si on a assez de temps.
 	 * 
 	 * @param nomPotion
 	 * @return
 	 */
 	public boolean containPotion(String nomPotion) {
 		boolean contain = false;
 
 		for (int i = 0; i < potion.size(); i++) {
 			if (potion.get(i).getNomPotion() == nomPotion)
 				contain = true;
 		}
 
 		return contain;
 	}
 
 	/**
 	 * Retirer un objet du HashMap
 	 */
 	public void deleteItem(String nomItem) {
 		items.removeItem(nomItem);
 	}
 	
 	public ArrayList<Potion> getArrayList() {
 		return potion;
 	}
 
 	/**
 	 * Fonction qui permet de retourné la description de la salle
 	 * 
 	 * @return la description de la salle
 	 */
 	public String getDescription() {
 		return description;
 	}
 	
 	/**
 	 * Défini les sorties possibles de la salle courante. Soit la direction mène
 	 * à une salle, soit mène nulle part ( = NULL ).
 	 * 
 	 * @param direction
 	 *            Une direction, par exemple north,east ...
 	 */
 	public Room getExit(String direction) {
 		return exits.get(direction);
 	}
 
 	/*
 	 * Exo 7.9 : keySet() : retourner dans un Set les clés de la HashMap des
 	 * sorties
 	 * 
 	 * Methode getExitString() : On crée un Set des clés de la HashMap des
 	 * sorties. Pour chaque clé, on ajoute à la variable locale exitString (un
 	 * String) le nom de la sortie, et on retourne la valeur de exitString
 	 */
 	/**
 	 * Fonction qui permet d'énumerer les sorties possibles.
 	 * 
 	 * @return Les sorties disponibles
 	 */
 	public String getExitString() 
 	{
 		String exitString = "Les autres endroits : ";
 
 		Set<String> keys = exits.keySet();
 		for (String exit : keys)
 			exitString += " " + "-" + exit + "-";
 		return exitString;
 	}
 
 	public String getImageName() {
 		return imageName;
 	}
 
 	public ItemListe getItemListe() {
 		return items;
 	}
 
 	/**
 	 * Retourner le nom, la description et le poids de tous les objets dans la
 	 * salle
 	 * 
 	 * @return Nom, description et poids des objets
 	 */
 	public String getItemString() 
 	{
 		String itemString = "Regarde s'il y a des objets ici: ";
 		
 		if (!items.getHashMap().isEmpty())
 		{
 			Set<String> cles = items.getKeys();
 			for (String nom : cles) 
 			{
 				if(nom != "beamer")
 				{
 					Item valeurs = items.getValue(nom);
 					itemString += "\n" + valeurs.getDescriptionItem() + " qui pèse " + valeurs.toString() + " kg";
 				}
 			}
 		}
 		else
 		{
 			itemString = "Il n'y a pas d'objet ici";
 		}
 		
 
 	
 		/********** L'ArrayList des potions **********/
 		itemString += "\n";
 		for (int i = 0; i < potion.size(); i++) {
 			itemString += "\n" + potion.get(i).getNomPotion();
 		}
 		/****************************************************************/
 
 		return itemString;
 
 	}
 
 	/**
 	 * Retourner une description plus detaillée de la salle
 	 * 
 	 * @return Description detaillee de la salle
 	 */
 	public String getLongDescription() 
 	{
 		return "Tu es " + description + "\n" + getExitString() + "\n" + "\n"+ getItemString() + "\n" + "\n" + getBotString();
 	}
 
 	public String getNomRoom()
 	{
 		return nomRoom;
 	}
 
 	/**
 	 * Méthode aussi moche que la précédente pour retourner l'index de la potion
 	 * voulue
 	 * 
 	 * @param nomPotion
 	 * @return
 	 */
 	public int indexPotion(String nomPotion) {
 		int k = 0;
 		for (int i = 0; i < potion.size(); i++) {
 			if (potion.get(i).getNomPotion() == nomPotion)
 				k = i;
 		}
 
 		return k;
 	}
 
 	public void removePotion(final Potion pPotion) {
 		potion.remove(pPotion);
 	}
 	
 	/**
 	 * Défini les sorties possibles de la salle courante. Soit la direction mène
 	 * à une salle, soit mène nulle part ( = NULL ).
 	 * 
 	 * @param direction
 	 *            Une direction, par exemple north,east ...
 	 * @param voisin
 	 *            Salle voisine ??
 	 */
 	public void setExit(String direction, Room voisin) {
 		exits.put(direction, voisin);
 	}
 	
 	public String getBotString()
 	{
 		String nomBots = "";
 		Set<String> keys = bots.keySet();
 		for(String nom : keys)
 			nomBots += nom + "  ";
 		
 		if(nomBots != "")
 			return "!!!! Fais attention, il y a d'autre(s) ennemi(s) dans cet endroit: " + nomBots + "!!!!";
 		else 
 			return "Il n'y a pas d'ennemi ici!";
 	}
 	
 	public Bots getBot()
 	{
 		Bots bot = null;
 		Set<String> keys = bots.keySet();
 		for(String nom : keys)
 		{
 			if(nom != null)
 				bot = bots.get(nom);
 			else
 				bot = null;
 		}
 		return bot;
 	}
 	
 	public void setImage(String image)
 	{
 		imageName = image;
 	}
 	
 	public boolean containBot(String nomBot)
 	{
 		boolean contain = false;
 		
		Set<String> noms = bots.keySet();
		
		for(String nom : noms)
		{
			if(nom == nomBot)
				contain = true;
		}
 		
 		return contain;
 	}
 }
