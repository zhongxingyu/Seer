 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 import java.util.Scanner;
 
 public class InitialisationData {
 
 	public static void main(String[] args) throws Exception {
 		IDatabase data;
 		if(args.length > 0 && "db4o".equals(args[0])) {
 			data = new DB4ODatabase();
 		} else {
 			data = new VersantDatabase();
 		}
		data.open();
 		
 		System.out.println("Initialisation des données");
 		
 		List<Gare> gares = new ArrayList<>();
 		List<Trajet> trajets = new ArrayList<>();
 		List<Passager> passagers = new ArrayList<>();
 		List<Billet> billets = new ArrayList<>();
 
 		// La graine est un canard
 		Random r = new Random("canard".hashCode());
 		long currentTimestamp = new Date().getTime();
 
 		// Données opendata :
 		// http://www.data.gouv.fr/donnees/view/Liste-des-gares-de-voyageurs-du-RFN-avec-coordonn%C3%A9es-30383099
 		FileReader fichierGares = new FileReader("gares.csv");
 		Scanner sc = new Scanner(fichierGares);
 		
 		int i = 0;
 		while (sc.hasNextLine()) {
 			String line = sc.nextLine();
 			String[] infosGare = line.split(",");
 
 			Gare gare = new Gare();
 			gare.setCode(Integer.toString(i++, 36).toUpperCase());
 			gare.setNom(infosGare[0] + " - " + infosGare[1]);
 			gare.setLat(Double.valueOf(infosGare[2]));
 			gare.setLon(Double.valueOf(infosGare[3]));
 			
 			gares.add(gare);
 			data.persist(gare);
 		}
 		
 		sc.close();
 
 		int nbGares = gares.size();
 		System.out.print(nbGares);
 		System.out.println(" gares chargees.");
 
 		for (i = 0; i < 15000; ++i) {
 			Trajet trajet = new Trajet();
 			trajet.setCode(Integer.toString(i, 36).toUpperCase());
 			
 			trajet.setDepart(gares.get(r.nextInt(nbGares)));
 			trajet.setArrivee(gares.get(r.nextInt(nbGares)));
 
 			// Génération aléatoire du nombre de places
 			trajet.setNbPlaces(100 + r.nextInt(5) * 100 + r.nextInt(5) * 10);
 
 			// Tout les trains partent maintenant
 			trajet.setDateDepart(new Date(currentTimestamp + r.nextInt(10000)*1000));
 
 			// Date d"arrivée en fonction de la distance
 			trajet.setDateArrivee(new Date((trajet.getDateDepart().getTime())
 					+ ((long) trajet.distance() * 15)));
 
 			trajets.add(trajet);
 			data.persist(trajet);
 		}
 
 		System.out.println("15000 trajets ajoutes");
 
 		// Les noms proviennent de
 		// https://github.com/fzaninotto/Faker/blob/master/src/Faker/Provider/fr_FR/Person.php
 		String[] prenoms = { "Adrien", "Aimé", "Alain", "Alexandre", "Alfred",
 				"Alphonse", "André", "Antoine", "Arthur", "Auguste",
 				"Augustin", "Benjamin", "Benoît", "Bernard", "Bertrand",
 				"Charles", "Christophe", "Daniel", "David", "Denis", "Édouard",
 				"Émile", "Emmanuel", "Éric", "Étienne", "Eugène", "François",
 				"Franck", "Frédéric", "Gabriel", "Georges", "Gérard",
 				"Gilbert", "Gilles", "Grégoire", "Guillaume", "Guy", "William",
 				"Henri", "Honoré", "Hugues", "Isaac", "Jacques", "Jean",
 				"Jérôme", "Joseph", "Jules", "Julien", "Laurent", "Léon",
 				"Louis", "Luc", "Lucas", "Marc", "Marcel", "Martin",
 				"Matthieu", "Maurice", "Michel", "Nicolas", "Noël", "Olivier",
 				"Patrick", "Paul", "Philippe", "Pierre", "Raymond", "Rémy",
 				"René", "Richard", "Robert", "Roger", "Roland", "Sébastien",
 				"Stéphane", "Théodore", "Théophile", "Thibaut", "Thibault",
 				"Thierry", "Thomas", "Timothée", "Tristan", "Victor",
 				"Vincent", "Xavier", "Yves", "Zacharie", "Adélaïde", "Adèle",
 				"Adrienne", "Agathe", "Agnès", "Aimée", "Alexandrie", "Alix",
 				"Alexandria", "Alex", "Alice", "Amélie", "Anaïs", "Anastasie",
 				"Andrée", "Anne", "Anouk", "Antoinette", "Arnaude", "Astrid",
 				"Audrey", "Aurélie", "Aurore", "Bernadette", "Brigitte",
 				"Capucine", "Caroline", "Catherine", "Cécile", "Céline",
 				"Célina", "Chantal", "Charlotte", "Christelle", "Christiane",
 				"Christine", "Claire", "Claudine", "Clémence", "Colette",
 				"Constance", "Corinne", "Danielle", "Denise", "Diane",
 				"Dorothée", "Édith", "Éléonore", "Élisabeth", "Élise",
 				"Élodie", "Émilie", "Emmanuelle", "Françoise", "Frédérique",
 				"Gabrielle", "Geneviève", "Hélène", "Henriette", "Hortense",
 				"Inès", "Isabelle", "Jacqueline", "Jeanne", "Jeannine",
 				"Joséphine", "Josette", "Julie", "Juliette", "Laetitia",
 				"Laure", "Laurence", "Lorraine", "Louise", "Luce", "Lucie",
 				"Lucy", "Madeleine", "Manon", "Marcelle", "Margaux", "Margaud",
 				"Margot", "Marguerite", "Margot", "Margaret", "Maggie",
 				"daisy", "Marianne", "Marie", "Marine", "Marthe", "Martine",
 				"Maryse", "Mathilde", "Michèle", "Michelle", "Michelle",
 				"Monique", "Nathalie", "Nath", "Nathalie", "Nicole", "Noémi",
 				"Océane", "Odette", "Olivie", "Patricia", "Paulette",
 				"Pauline", "Pénélope", "Philippine", "Renée", "Sabine",
 				"Simone", "Sophie", "Stéphanie", "Susanne", "Suzanne", "Susan",
 				"Suzanne", "Sylvie", "Thérèse", "Valentine", "Valérie",
 				"Véronique", "Victoire", "Virginie", "Zoé", "Camille",
 				"Claude", "Dominique" };
 		String[] noms = { "Martin", "Bernard", "Thomas", "Robert", "Petit",
 				"Dubois", "Richard", "Garcia", "Durand", "Moreau", "Lefebvre",
 				"Simon", "Laurent", "Michel", "Leroy", "Martinez", "David",
 				"Fontaine", "Da Silva", "Morel", "Fournier", "Dupont",
 				"Bertrand", "Lambert", "Rousseau", "Girard", "Roux", "Vincent",
 				"Lefevre", "Boyer", "Lopez", "Bonnet", "Andre", "Francois",
 				"Mercier", "Muller", "Guerin", "Legrand", "Sanchez", "Garnier",
 				"Chevalier", "Faure", "Perez", "Clement", "Fernandez", "Blanc",
 				"Robin", "Morin", "Gauthier", "Pereira", "Perrin", "Roussel",
 				"Henry", "Duval", "Gautier", "Nicolas", "Masson", "Marie",
 				"Noel", "Ferreira", "Lemaire", "Mathieu", "Riviere", "Denis",
 				"Marchand", "Rodriguez", "Dumont", "Payet", "Lucas", "Dufour",
 				"Dos Santos", "Joly", "Blanchard", "Meunier", "Rodrigues",
 				"Caron", "Gerard", "Fernandes", "Brunet", "Meyer", "Barbier",
 				"Leroux", "Renard", "Goncalves", "Gaillard", "Brun", "Roy",
 				"Picard", "Giraud", "Roger", "Schmitt", "Colin", "Arnaud",
 				"Vidal", "Gonzalez", "Lemoine", "Roche", "Aubert", "Olivier",
 				"Leclercq", "Pierre", "Philippe", "Bourgeois", "Renaud",
 				"Martins", "Leclerc", "Guillaume", "Lacroix", "Lecomte",
 				"Benoit", "Fabre", "Carpentier", "Vasseur", "Louis", "Hubert",
 				"Jean", "Dumas", "Rolland", "Grondin", "Rey", "Huet", "Gomez",
 				"Dupuis", "Guillot", "Berger", "Moulin", "Hoarau", "Menard",
 				"Deschamps", "Fleury", "Adam", "Boucher", "Poirier", "Bertin",
 				"Charles", "Aubry", "Da Costa", "Royer", "Dupuy", "Maillard",
 				"Paris", "Baron", "Lopes", "Guyot", "Carre", "Jacquet",
 				"Renault", "Herve", "Charpentier", "Klein", "Cousin", "Collet",
 				"Leger", "Ribeiro", "Hernandez", "Bailly", "Schneider",
 				"Le Gall", "Ruiz", "Langlois", "Bouvier", "Gomes", "Prevost",
 				"Julien", "Lebrun", "Breton", "Germain", "Millet", "Boulanger",
 				"Remy", "Le Roux", "Daniel", "Marques", "Maillot", "Leblanc",
 				"Le Goff", "Barre", "Perrot", "Leveque", "Marty", "Benard",
 				"Monnier", "Hamon", "Pelletier", "Alves", "Etienne", "Marchal",
 				"Poulain", "Tessier", "Lemaitre", "Guichard", "Besson",
 				"Mallet", "Hoareau", "Gillet", "Weber", "Jacob", "Collin",
 				"Chevallier", "Perrier", "Michaud", "Carlier", "Delaunay",
 				"Chauvin", "Alexandre", "Marechal", "Antoine", "Lebon",
 				"Cordier", "Lejeune", "Bouchet", "Pasquier", "Legros",
 				"Delattre", "Humbert", "De Oliveira", "Briand", "Lamy",
 				"Launay", "Gilbert", "Perret", "Lesage", "Gay", "Nguyen",
 				"Navarro", "Besnard", "Pichon", "Hebert", "Cohen", "Pons",
 				"Lebreton", "Sauvage", "De Sousa", "Pineau", "Albert",
 				"Jacques", "Pinto", "Barthelemy", "Turpin", "Bigot",
 				"Lelievre", "Georges", "Reynaud", "Ollivier", "Martel",
 				"Voisin", "Leduc", "Guillet", "Vallee", "Coulon", "Camus",
 				"Marin", "Teixeira", "Costa", "Mahe", "Didier", "Charrier",
 				"Gaudin", "Bodin", "Guillou", "Gregoire", "Gros", "Blanchet",
 				"Buisson", "Blondel", "Paul", "Dijoux", "Barbe", "Hardy",
 				"Laine", "Evrard", "Laporte", "Rossi", "Joubert", "Regnier",
 				"Tanguy", "Gimenez", "Allard", "Devaux", "Morvan", "Levy",
 				"Dias", "Courtois", "Lenoir", "Berthelot", "Pascal",
 				"Vaillant", "Guilbert", "Thibault", "Moreno", "Duhamel",
 				"Colas", "Masse", "Baudry", "Bruneau", "Verdier", "Delorme",
 				"Blin", "Guillon", "Mary", "Coste", "Pruvost", "Maury",
 				"Allain", "Valentin", "Godard", "Joseph", "Brunel", "Marion",
 				"Texier", "Seguin", "Raynaud", "Bourdon", "Raymond", "Bonneau",
 				"Chauvet", "Maurice", "Legendre", "Loiseau", "Ferrand",
 				"Toussaint", "Techer", "Lombard", "Lefort", "Couturier",
 				"Bousquet", "Diaz", "Riou", "Clerc", "Weiss", "Imbert",
 				"Jourdan", "Delahaye", "Gilles", "Guibert", "Begue",
 				"Descamps", "Delmas", "Peltier", "Dupre", "Chartier",
 				"Martineau", "Laroche", "Leconte", "Maillet", "Parent",
 				"Labbe", "Potier", "Bazin", "Normand", "Pottier", "Torres",
 				"Lagarde", "Blot", "Jacquot", "Lemonnier", "Grenier", "Rocher",
 				"Bonnin", "Boutin", "Fischer", "Munoz", "Neveu", "Lacombe",
 				"Mendes", "Delannoy", "Auger", "Wagner", "Fouquet", "Mace",
 				"Ramos", "Pages", "Petitjean", "Chauveau", "Foucher", "Peron",
 				"Guyon", "Gallet", "Rousset", "Traore", "Bernier", "Vallet",
 				"Letellier", "Bouvet", "Hamel", "Chretien", "Faivre", "Boulay",
 				"Thierry", "Samson", "Ledoux", "Salmon", "Gosselin", "Lecoq",
 				"Pires", "Leleu", "Becker", "Diallo", "Merle", "Valette" };
 		
 		for (i = 0; i < 50000; ++i) {
 			Passager passager = new Passager();
 			passager.setNumSecu(Integer.toString(i, 36).toUpperCase());
 			
 			passager.setNom(noms[r.nextInt(noms.length)]);
 			passager.setPrenom(prenoms[r.nextInt(prenoms.length)]);
 			
 			// Le passager est né entre le 1er janvier 1970 et maintenant
 			passager.setDateNaissance(new Date(((long) r.nextInt((int) (currentTimestamp/1000))*1000)));
 			
 			passagers.add(passager);
 			data.persist(passager);
 		}
 		
 		System.out.println("Création de 50000 passagers");
 		
 		for (i = 0; i < 200000; ++i) {
 			Billet billet = new Billet();
 			billet.setCode(Integer.toString(i, 36).toUpperCase());
 			billet.setPassager(passagers.get(r.nextInt(passagers.size())));
 			Trajet trajet = trajets.get(r.nextInt(trajets.size()));
 			billet.setPrix(trajet.prixActuel());
 			billet.setTrajet(trajet);
 			trajet.setNbPlacesReservees(trajet.getNbPlacesReservees()+1);
 			
 			billets.add(billet);
 			data.persist(billet);
 			data.persist(trajet);
 		}
 		
 		System.out.println("Création de 200000 billets");
 		
 		data.commit();
 		data.close();
 		
 		System.out.println("Initialisation des données terminée");
		
		/*Console c = new Console();
		c.gares = gares;
		c.trajets = trajets;
		c.billets = billets;
		c.start();*/
 	}
 }
