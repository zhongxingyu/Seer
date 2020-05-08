 package factory;
 
 import java.util.Collection;
 import java.util.HashMap;
 import net.phys2d.raw.World;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.tiled.TiledMap;
 
 import blocs.BlocsBlessant;
 import blocs.Plateforme;
 import blocs.Point;
 import blocs.Switch;
 
 /**
  * 
  * @author Equipe RoboTech 
  * Crateur de Plateforme
  */
 public class PlateformeFactory extends AbstractFactory {
 
 	
 	/**
 	 * Plateformes en cours de cration
 	 *
 	 */
 	private HashMap<String, Plateforme> chaine;
 
 	/**
 	 * Fabrique de bouton
 	 */
 	private SwitchFactory s;
 
 	/**
 	 * Cre une fabrique
 	 * @param world Monde Physique
 	 * @param map Carte du niveau
 	 * @param s Fabrique de Bouton
 	 */
 	public PlateformeFactory(World world, TiledMap map, SwitchFactory s) {
 		super(world, map);
 		chaine = new HashMap<String, Plateforme>();
 		this.s = s;
 	}
 
 	/**
 	 * Ajoute un point dans la trajectoire de la plateforme
 	 * @param i Abscisse en Tile
 	 * @param j Ordonne en Tile
 	 */
 	public void addPosition(int i, int j) {
 		
 		String name = map.getObjectName(i, j);
 		assert(!name.equals("NOT_FOUND"));
 		
 		Plateforme p = getProduit(name);
 		System.out.println("Name = "+name);
 		assert(p!=null);
 		int last_size = p.getTrajectoire().size();
 		
 		Point position = getPosition(i, j);
 		int width = map.getObjectWidth(i, j);
 		int height = map.getObjectHeight(i, j);
 		assert(width != -1 && height!= -1);
 		p.addPoint(position.getX() + width / 2, position.getY() + height / 2);
 		assert(p.getTrajectoire().size()==last_size+1);
 
 	}
 
 	private Plateforme getProduit(String name) {
 		return chaine.get(name);
 	}
 
 	public Collection<Plateforme> getProduit() {
 		return chaine.values();
 	}
 
 	/**
 	 * Cre une nouvelle plateforme et l'intgre dans l'environnement de jeu
 	 * @param i Abscisse de l'objet dans la carte
 	 * @param j Ordonne de l'objet dans la carte
 	 */
 	public void createPlateforme(int i, int j) {
 		
 		int last_size = chaine.values().size();
 		Point position = getPosition(i, j);
 		int width = getWidth(i, j);
 		int height = getHeight(i, j);
 		
 		//Valeur d'erreur
 		assert(width != -1 && height != -1);
 		position.add(width / 2, height / 2);
 		Image image_box = getImage(i, j, "Image");
 		boolean signal = Boolean
 				.parseBoolean(recupererPropriete(i, j, "Signal"));
 		boolean reverse = Boolean.parseBoolean(recupererPropriete(i, j,
 				"Reverse"));
 
 		String name = getName(i, j);
 		String type = recupererPropriete(i, j, "Type");
 		System.out.println("Name "+name+"Type = "+type);
 		Plateforme p;
 		if(type.equals("pics"))
 		p = new Plateforme(position,new BlocsBlessant(image_box,width, height, position,4));
 		else
 		p = new Plateforme(position, image_box, width, height);
 		
 				
 		// Objet bien allou
 		assert(p!=null);
 		
 		p.addPoint(position.getX(), position.getY());
 		p.setSignal(signal);
 		p.setReverse(reverse);
 		world.add(p.getBody());
 		chaine.put(name, p);
 		// Ajout d'un point dans la trajectoire
 		assert(p.getTrajectoire().size()==1);
 		// Une nouvelle plateforme a t ajout
 		assert(last_size+1 == chaine.values().size());
 		
 		String sender_name = recupererPropriete(i, j, "Sender");
 		boolean draw = Boolean.parseBoolean(recupererPropriete(i, j, "draw"));
 		p.setTrajectoireDraw(draw);
		boolean receiver = Boolean.parseBoolean(recupererPropriete(i,j,"Receiver"));
		if (!sender_name.equals("NOT_FOUND") && receiver) {
 			Switch sender = s.get_produit(sender_name);
 			last_size = sender.getReceiver().size();
 			sender.add(p);
 			// Un receveur de signal a t ajout
 			assert(last_size+1 == sender.getReceiver().size());
 		}
 		
 
 	}
 
 	@Override
 	public String toString() {
 		String res = super.toString();
 		res = res + "Longueur = " + chaine.size() + "\n";
 		for (int i = 0; i < chaine.size(); i++)
 			res = res + "[" + i + "]" + chaine.get(i).toString();
 		res = res + "\n";
 		return res;
 	}
 
 }
