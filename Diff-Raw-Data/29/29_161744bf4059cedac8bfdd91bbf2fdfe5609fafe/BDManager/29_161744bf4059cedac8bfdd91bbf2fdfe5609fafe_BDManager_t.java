 package com.rocel.fabapp.model;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class BDManager {
 
 	private static BDManager instance;
 	private Connection conn;
 	private Statement stat;
 
 	public static SimpleDateFormat formatterDate = new SimpleDateFormat("dd/MM/yyyy");
 	public static SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm");
 
 	/**.
 	 * Constructeur
 	 */
 	private BDManager(){
 		try {
 			Class.forName("org.sqlite.JDBC");
 			conn = DriverManager.getConnection("jdbc:sqlite:bdd.db");
 			stat = conn.createStatement();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**.
 	 * Singleton methode getInstance
 	 * @return instance de BDManager
 	 */
 	public static BDManager getInstance(){
 		if (instance == null){
 			instance = new BDManager();
 		}
 		return instance;
 	}
 	/**.
 	 * Sauvegarde un rameur dans la BDD
 	 * @param rameur a sauvegarder
 	 * @return true si l'operation s'est bien passée, sinon false.
 	 */
 	public boolean saveRameur(Rameur rameur) {
 		System.out.println("Saving ... " + rameur);
 
 		PreparedStatement prep;
 		try {
 			prep = conn.prepareStatement( "INSERT INTO rameur('nom','prenom','dateNaissance','categorie','type','adresse','email','phone','morphologie','pointure','nbLicence') " +
 					"VALUES(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11);");
 			prep.setString(1, rameur.getNom());
 			prep.setString(2, rameur.getPrenom());
 			prep.setString(3, formatterDate.format(rameur.getDateNaissance()));
 			prep.setString(4, rameur.getCategorie().toString());
 			prep.setString(5, rameur.getType().toString());
 			prep.setString(6, rameur.getAdresse());
 			prep.setString(7, rameur.getEmail());
 			prep.setString(8, rameur.getTelephone());
 			prep.setString(9, rameur.getMorphologie());
 			prep.setInt(10, rameur.getPointure());
 			prep.setString(11, rameur.getLicence());
 			prep.addBatch();
 			conn.setAutoCommit(false);
 			prep.executeBatch();
 			conn.setAutoCommit(true);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	/**.
 	 * Sauvegarde un bateau dans la BDD
 	 * @param bateau a sauvegarder
 	 * @return true si l'operation s'est bien passée, sinon false.
 	 */
 	public boolean saveBateau(Bateau bateau){
 		System.out.println("Bateau ... " + bateau);
 		PreparedStatement prep;
 		try {
 			prep = conn.prepareStatement("INSERT INTO Bateau('equipage','portant','type','portance','coulisse','materiaux','marque'," +
 					"										 'modele','dateFabrication','dateAcquisition','garantie')" +
 					"VALUES(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11);");
 			prep.setString(1, getEquipage(bateau.getEquipage()));
 			prep.setString(2, bateau.getPortant());
 			prep.setString(3, bateau.getType());
 			prep.setInt(4, bateau.getPortance());
 			prep.setInt(5, bateau.getCoulisse());
 			prep.setString(6, bateau.getMateriaux());
 			prep.setString(7, bateau.getMarque());
 			prep.setString(8, bateau.getModele());
 			prep.setString(9, new SimpleDateFormat("dd/MM/yyyy").format(bateau.getDateFabrication()));
 			prep.setString(10, new SimpleDateFormat("dd/MM/yyyy").format(bateau.getDateAcquisition()));
 			prep.setInt(11, bateau.getGarantie());
 			prep.addBatch();
 			conn.setAutoCommit(false);
 			prep.executeBatch();
 			conn.setAutoCommit(true);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 
 		return true;
 	}
 
 	/**.
 	 * Sauvegarde un DegatRep dans la BDD
 	 * @param degatRep a sauvegarder
 	 * @return  true si l'operation s'est bien passée, sinon false.
 	 */
 	public boolean saveDegatRep(DegatRep degatRep){
 		System.out.println("DegatRep ... :" + degatRep);
 		PreparedStatement prep;
 		try{
 			prep = conn.prepareStatement("INSERT INTO degatRep('dateDegat','observation','zoneDegat','licenceRameur','refBateau','importanceDegat'," +
 					"										'typeReparation','cout','reparePar')" +
 					"VALUES(?1,?2,?3,?4,?5,?6,?7,?8,?9);");
 			prep.setString(1, formatterDate.format(degatRep.getDateDegat()));
 			prep.setString(2, degatRep.getObservation());
 			prep.setString(3, degatRep.getZoneDegat());
 			prep.setString(4, degatRep.getRameur().getLicence());
 			prep.setInt(5, degatRep.getBateau().getRef());
 			prep.setString(6, degatRep.getImportanceDegat());
 			prep.setString(7, degatRep.getTypeReparation());
 			prep.setDouble(8, degatRep.getCout());
 			prep.setString(9, degatRep.getReparePar());
 			prep.addBatch();
 			conn.setAutoCommit(false);
 			prep.executeBatch();
 			conn.setAutoCommit(true);
 		}catch (SQLException e){
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	/**.
 	 * Sauvegarde un Jeux d'avirons dans la BDD
 	 * @param jeuxAvirons a sauvegarder
 	 * @return  true si l'operation s'est bien passée, sinon false.
 	 */
 	public boolean saveJeuxAvirons(JeuxAvirons jeuxAvirons){
 		System.out.println("JeuxAvirons ... :" + jeuxAvirons);
 		PreparedStatement prep;
 		try{
 			prep = conn.prepareStatement("INSERT INTO jeuxAvirons ('reglable' , 'marque' , 'modele' , 'garantie' , 'dateFabrication' , 'dateAcquisition'  , 'refBateau', 'quantite' )" +
 					"VALUES(?1,?2,?3,?4,?5,?6,?7,?8);");
 			prep.setBoolean(1, jeuxAvirons.isReglable());
 			prep.setString(2, jeuxAvirons.getMarque());
 			prep.setString(3, jeuxAvirons.getModele());
 			prep.setInt(4, jeuxAvirons.getGarantie());
 			prep.setString(5, formatterDate.format(jeuxAvirons.getDateFabrication()));
 			prep.setString(6, formatterDate.format(jeuxAvirons.getDateAcquisition()));
 			System.out.println(jeuxAvirons.getBateau());
 			prep.setInt(7, jeuxAvirons.getBateau().getRef());
 			prep.setInt(8, jeuxAvirons.getQuantite());
 			prep.addBatch();
 			conn.setAutoCommit(false);
 			prep.executeBatch();
 			conn.setAutoCommit(true);
 		}catch (SQLException e){
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 
 
 	/**.
 	 * Get Rameur dans la BDD par son numéro de licence
 	 * @param nbLicence du rameur
 	 * @return LE Rameur
 	 */
 	public Rameur getRameurByNBLicence(String nbLicence) {
 		ResultSet rs;
 		Rameur rameur = null;
 		try {
 			rs = this.stat.executeQuery("select * from rameur WHERE nbLicence = '"+nbLicence+"'");
 			while (rs.next()) {
 				String nom = rs.getString("nom");
 				String prenom = rs.getString("prenom");
 				Date dateNaissance = formatterDate.parse(rs.getString("dateNaissance"));
 				Categorie categorie = Categorie.valueOf(rs.getString("categorie"));
 				TypeRameur type = TypeRameur.valueOf(rs.getString("type"));
 				String adresse = rs.getString("adresse");
 				String email = rs.getString("email");
 				String telephone = rs.getString("phone");
 				String morphologie = rs.getString("morphologie");
 				int pointure = Integer.valueOf(rs.getString("pointure"));
 				String licence = rs.getString("nbLicence");
 				rameur = new Rameur(nom, prenom, dateNaissance, categorie, type, adresse, email, telephone, morphologie, pointure, licence);
 			}
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		} catch (ParseException e) {
 			//From SimpleDateFormat
 			e.printStackTrace();
 			return null;
 		}
 		return rameur;
 	}
 
 	/**.
 	 * Destructor de la classe qui ferme la connection a la bdd
 	 */
 	public void finalize(){
 		try {
 			this.conn.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**.
 	 * Get un bateau par son Ref id de la bdd
 	 * @param ref Reference id du bateau dans la BDD
 	 * @return LE Bateau
 	 */
 	public Bateau getBateauByRef(int refIn) {
 		ResultSet rs;
 		Bateau bateau= null;
 		try {
 			rs = this.stat.executeQuery("select * from bateau WHERE ref = "+refIn);
 			while (rs.next()) {
 				int ref = rs.getInt("ref");
 				String type = rs.getString("type");
 				int portance = rs.getInt("portance");
 				int coulisse = rs.getInt("coulisse");
 				String portant = rs.getString("portant");
 				String materiaux = rs.getString("materiaux");
 				String marque = rs.getString("marque");
 				String modele = rs.getString("modele");
 				Date dateFabrication = formatterDate.parse(rs.getString("dateFabrication")); 
 				Date dateAcquisition = formatterDate.parse(rs.getString("dateAcquisition"));
 				int garantie = rs.getInt("garantie");
 				List<Rameur> equipage = getEquipage(rs.getString("equipage"));
 				bateau = new Bateau(ref,type, portance, coulisse, portant, materiaux, marque, modele, dateFabrication, dateAcquisition, garantie, equipage);
 			}
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		} catch (ParseException e) {
 			//From SimpleDateFormat
 			e.printStackTrace();
 			return null;
 		}
 		return bateau;
 	}
 
 	/**
 	 * Get list de rameur formant un equipage
 	 * @param equipage au format "nbLicence/nblicence/..."
 	 * @return list de Rameurs
 	 */
 	public List<Rameur> getEquipage(String equipage) {
 		List<Rameur> equi = null;
 		if (! equipage.isEmpty()){
 			equi = new ArrayList<Rameur>();
 			for(String e : equipage.split("/")){
 				equi.add(getRameurByNBLicence(e));
 			}
 		}
 		return equi;
 	}
 
 	/**.
 	 * Get String list (format de la bdd) de rameurs formant un equipage
 	 * @param list de Rameurs
 	 * @return equipage au format "nbLicence/nblicence/..."
 	 */
 	public String getEquipage(List<Rameur> equipage) {
 		String equi = "";
 		if (equipage != null){
 			for (Rameur r: equipage) {
 				equi += r.getLicence() +"/";
 			}
 		}
 		return equi;
 	}
 
 	/**.
 	 * Get list de rameurs correspondant a la requete formée par les parametres
 	 * @param nomIN
 	 * @param prenomIN
 	 * @param dateNaissanceIN
 	 * @param categorieIN
 	 * @param typeIN
 	 * @param adresseIN
 	 * @param emailIN
 	 * @param telephoneIN
 	 * @param morphologieIN
 	 * @param pointureIN
 	 * @param licenceIN
 	 * @return list de Rameurs
 	 */
 	public List<Rameur> getRameurs(String nomIN, String prenomIN, Date dateNaissanceIN,
 			Categorie categorieIN, TypeRameur typeIN, String adresseIN, String emailIN,
 			String telephoneIN, String morphologieIN, int pointureIN,
 			String licenceIN){
 		List<Rameur> equi = new ArrayList<Rameur>();
 		StringBuffer query = new StringBuffer();
 		query.append("select * from rameur ");
 		boolean hasPrevious = false;
 
 		if (! nomIN.isEmpty() || ! prenomIN.isEmpty() || dateNaissanceIN != null || categorieIN != null || typeIN != null || ! adresseIN.isEmpty() || ! emailIN.isEmpty() || ! telephoneIN.isEmpty() || ! morphologieIN.isEmpty() || pointureIN>0 || ! licenceIN.isEmpty()) {
 			query.append("WHERE ");
 		}
 		if (! nomIN.isEmpty()){
 			query.append(" nom='"+nomIN+"'");
 			hasPrevious = true;
 		}
 		if (! prenomIN.isEmpty()){
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			hasPrevious = true;
 			query.append(" prenom='"+prenomIN+"'");
 		}
 		if (dateNaissanceIN != null){
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			hasPrevious = true;
 			query.append(" dateNaissance='"+formatterDate.format(dateNaissanceIN)+"'");
 		}
 		if (categorieIN != null){
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			hasPrevious = true;
 			query.append(" categorie='"+categorieIN.toString()+"'");
 		}
 		if (typeIN != null){
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			hasPrevious = true;
 			query.append(" type='"+typeIN.toString()+"'");
 		}
 		if (! adresseIN.isEmpty()){
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			hasPrevious = true;
 			query.append(" adresse='"+adresseIN+"'");
 		}
 		if (! emailIN.isEmpty()){
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			hasPrevious = true;
 			query.append(" email='"+emailIN+"'");
 		}
 		if (! telephoneIN.isEmpty()){
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			hasPrevious = true;
 			query.append(" telephone='"+telephoneIN+"'");
 		}
 		if (! morphologieIN.isEmpty()){
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			hasPrevious = true;
 			query.append(" morphologie='"+morphologieIN+"'");
 		}
 		if (pointureIN>0){
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			hasPrevious = true;
 			query.append(" pointure='"+String.valueOf(pointureIN)+"'");
 		}
 		if (! licenceIN.isEmpty()){
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			hasPrevious = true;
 			query.append(" licence='"+licenceIN+"'");
 		}
 
 		ResultSet rs;
 		try {
 			rs = this.stat.executeQuery(query.toString());
 			while (rs.next()) {
 				String nom = rs.getString("nom");
 				String prenom = rs.getString("prenom");
 				Date dateNaissance = formatterDate.parse(rs.getString("dateNaissance"));
 				Categorie categorie = Categorie.valueOf(rs.getString("categorie"));
 				TypeRameur type = TypeRameur.valueOf(rs.getString("type"));
 				String adresse = rs.getString("adresse");
 				String email = rs.getString("email");
 				String telephone = rs.getString("phone");
 				String morphologie = rs.getString("morphologie");
 				int pointure = Integer.valueOf(rs.getString("pointure"));
 				String licence = rs.getString("nbLicence");
 				equi.add(new Rameur(nom, prenom, dateNaissance, categorie, type, adresse, email, telephone, morphologie, pointure, licence));
 			}
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		} catch (ParseException e) {
 			//From SimpleDateFormat
 			e.printStackTrace();
 			return null;
 		}
 
 		return equi;
 	}
 
 	public List<Bateau> getBateaux(String typeIn, int portanceIn, int coulisseIn, String portantIn, String materiauxIn, String marqueIn, String modeleIn,
 			Date dateFabricationIn, Date dateAcquisitionIn, int garantieIn, List<Rameur> equipageIn){
 
 		List<Bateau> bat = new ArrayList<Bateau>();
 		StringBuffer query = new StringBuffer();
 		query.append("select * from bateau ");
 		boolean hasPrevious = false;
 
 		if(!typeIn.isEmpty() || portanceIn>0 || coulisseIn>0  || ! portantIn.isEmpty() || ! materiauxIn.isEmpty()  || ! marqueIn.isEmpty() || ! modeleIn.isEmpty() || dateFabricationIn!=null || dateAcquisitionIn != null || garantieIn>0 || equipageIn != null){
 			query.append("WHERE ");
 		}
 
 		if (! typeIn.isEmpty()){ 
 			query.append(" type='"+typeIn+"'");
 			hasPrevious = true;
 		}
 
 		if (portanceIn>0){ 
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			query.append(" portance='"+portanceIn+"'");
 			hasPrevious = true;
 		}
 
 		if (coulisseIn>0){ 
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			query.append(" coulisse='"+coulisseIn+"'");
 			hasPrevious = true;
 		}
 
 		if (! portantIn.isEmpty()){ 
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			query.append(" portant='"+portantIn+"'");
 			hasPrevious = true;
 		}
 		if (! materiauxIn.isEmpty()){ 
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			query.append(" materiaux='"+materiauxIn+"'");
 			hasPrevious = true;
 		}
 
 		if (! marqueIn.isEmpty()){ 
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			query.append(" marque='"+marqueIn+"'");
 			hasPrevious = true;
 		}
 
 		if (! modeleIn.isEmpty()){
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			query.append(" modele='"+modeleIn+"'");
 			hasPrevious = true;
 		}
 
 		if (dateFabricationIn!=null){ 
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			query.append(" dateFabrication='"+formatterDate.format(dateFabricationIn)+"'");
 			hasPrevious = true;
 		}
 
 		if (dateAcquisitionIn != null){ 
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			query.append(" dateAcquisition='"+formatterDate.format(dateAcquisitionIn)+"'");
 			hasPrevious = true;
 		}
 
 		if (garantieIn>0){ 
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			query.append(" garantie='"+garantieIn+"'");
 			hasPrevious = true;
 		}
 
 		if (equipageIn !=  null){
 			if (hasPrevious){
 				query.append(" AND ");
 			}
 			query.append(" equipage='"+equipageIn+"'");
 			hasPrevious = true;
 		}
 
 		System.out.println(query.toString());
 
 		ResultSet rs;
 		try {
 			rs = this.stat.executeQuery(query.toString());
 			while (rs.next()) {
 				int ref = rs.getInt("ref");
 				String type = rs.getString("type");
 				int portance = Integer.valueOf(rs.getInt("portance"));
 				int coulisse = Integer.valueOf(rs.getInt("coulisse"));
 				String portant = rs.getString("portant");
 				String materiaux = rs.getString("materiaux");
 				String marque = rs.getString("marque");
 				String modele = rs.getString("modele");
 				Date dateFabrication = formatterDate.parse(rs.getString("dateFabrication")); 
 				Date dateAcquisition = formatterDate.parse(rs.getString("dateAcquisition"));
 				int garantie = Integer.valueOf(rs.getInt("garantie"));
 //				System.out.println("***************************");
 //				System.out.println("--->" + rs.getString("equipage"));
 //				System.out.println("***>" + getEquipage(rs.getString("equipage")));
 //				System.out.println("***************************");
 				List<Rameur> equipage = getEquipage(rs.getString("equipage"));
 				bat.add(new Bateau(ref,type, portance, coulisse, portant, materiaux, marque, modele, dateFabrication, dateAcquisition, garantie, equipage));
 			}
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		} catch (ParseException e) {
 			//From SimpleDateFormat
 			e.printStackTrace();
 			return null;
 		}
 
 		return bat;
 	}
 
 	/**.
 	 * Get tout les bateaux de la BDD
 	 * @return List de tous les bateaux
 	 */
 	public List<Bateau> getAllBateaux() {
 		ResultSet rs;
 		List<Bateau> bateaux = new ArrayList<Bateau>();
 		try {
 			rs = this.stat.executeQuery("SELECT * FROM bateau;");
 			while (rs.next()) {
 				int ref = rs.getInt("ref");
 				String type = rs.getString("type");
 				int portance = rs.getInt("portance");
 				int coulisse = rs.getInt("coulisse");
 				String portant = rs.getString("portant");
 				String materiaux = rs.getString("materiaux");
 				String marque = rs.getString("marque");
 				String modele = rs.getString("modele");
 				Date dateFabrication = formatterDate.parse(rs.getString("dateFabrication")); 
 				Date dateAcquisition = formatterDate.parse(rs.getString("dateAcquisition"));
 				int garantie = rs.getInt("garantie");
 				List<Rameur> equipage = getEquipage(rs.getString("equipage"));
 				bateaux.add(new Bateau(ref,type, portance, coulisse, portant, materiaux, marque, modele, dateFabrication, dateAcquisition, garantie, equipage));
 			}
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		} catch (ParseException e) {
 			//From SimpleDateFormat
 			e.printStackTrace();
 			return null;
 		}
 		return bateaux;
 	}
 
 	public List<DegatRep> getAllDegatRep() {
 		ResultSet rs;
		Statement localStatement = null;
		try {
			localStatement = conn.createStatement();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
 		List<DegatRep> degatreps = new ArrayList<DegatRep>();
 		try {
			rs = localStatement.executeQuery("SELECT * FROM degatRep;");
 			while (rs.next()) {
 				int ref = rs.getInt("ref");
 				Date dateDegat= formatterDate.parse(rs.getString("dateDegat")); 
 				String observation = rs.getString("observation");
 				String zoneDegat= rs.getString("zoneDegat");
 				Rameur rameur = getRameurByNBLicence(rs.getString("licenceRameur"));
				Bateau refBateau = this.getBateauByRef(rs.getInt("refBateau"));
 				String importanceDegat= rs.getString("importanceDegat");
 				String typeReparation= rs.getString("typeReparation");
 				double cout = rs.getInt("cout");
 				String reparePar= rs.getString("reparePar");
 				degatreps.add(new DegatRep(ref, dateDegat, observation, zoneDegat, rameur, refBateau, importanceDegat, typeReparation, cout, reparePar));
 			}
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		} catch (ParseException e) {
 			//From SimpleDateFormat
 			e.printStackTrace();
 			return null;
 		}
 		return degatreps;
 	}
 	
 
 	/**.
 	 * Get tout les rameurs de la BDD
 	 * @return List de tous les bateaux
 	 */
 	public List<Rameur> getAllRameurs() {
 		ResultSet rs;
 		List<Rameur> rameurs = new ArrayList<Rameur>();
 		try {
 			rs = this.stat.executeQuery("SELECT * FROM rameur ORDER BY nom;");
 			while (rs.next()) {
 				String nom = rs.getString("nom");
 				String prenom = rs.getString("prenom");
 				Date dateNaissance = formatterDate.parse(rs.getString("dateNaissance"));
 				Categorie categorie = Categorie.valueOf(rs.getString("categorie"));
 				TypeRameur type = TypeRameur.valueOf(rs.getString("type"));
 				String adresse = rs.getString("adresse");
 				String email = rs.getString("email");
 				String telephone = rs.getString("phone");
 				String morphologie = rs.getString("morphologie");
 				int pointure = rs.getInt("pointure");
 				String licence = rs.getString("nbLicence");
 				rameurs.add(new Rameur(nom, prenom, dateNaissance, categorie, type, adresse, email, telephone, morphologie, pointure, licence));
 			}
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		} catch (ParseException e) {
 			//From SimpleDateFormat
 			e.printStackTrace();
 			return null;
 		}
 		return rameurs;
 	}
 
 }
