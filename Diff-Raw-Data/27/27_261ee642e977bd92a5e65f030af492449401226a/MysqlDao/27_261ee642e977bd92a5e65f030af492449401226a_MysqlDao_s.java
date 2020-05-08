 package dao;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import model.Aeroport;
 import model.Vol;
 
 public class MysqlDao {
 	// infos pour la connexion
 	private String datasource = "jdbc:mysql://localhost:3306/DEVFLY";
 	private String user = "greta";
 	private String password = "gretatest";
 
 	// retourne les vols "programmés" sous forme d'une liste
 	public List<Vol> getAllVolsProgrammes() throws SQLException {
 		List<Vol> vols = new ArrayList<>();
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource, user, password);
 		// on crée et exécute une requête préparée pour récupérer
 		// les informations sur le vol et ses employés
 		// (note : il n'y a pas de vols programmés sans employés affectés)
 		String sql1 = "SELECT V.numvol, V.lieudep, V.lieuarriv, V.dateheuredep,"
 				+ "V.dateheurearrivee, V.tarif, T.pilote, T.copilote,"
 				+ "T.hotesse_steward1, T.hotesse_steward2, T.hotesse_steward3 FROM vol V INNER JOIN travailler T ON V.numvol = T.vol";
 		PreparedStatement stmt1 = connection.prepareStatement(sql1);
 		ResultSet result1 = stmt1.executeQuery();
 		// on parcourt le résultat
 		while (result1.next()) {
 			String id = result1.getString("numvol");
 			String villeDepart = result1.getString("lieudep");
 			String villeArrivee = result1.getString("lieuarriv");
 			
 			// On récupère les dates sous forme de timestamp. On formatera à l'affichage.
 			Date dateHeureDepart = result1.getTimestamp("dateheuredep");
 			Date dateHeureArrivee = result1.getTimestamp("dateheurearrivee");
 			
 			// on calcule la durée du vol
 			int duree = util.Donnees.getDureeVol(dateHeureDepart, dateHeureArrivee);
 			
 			float tarif = result1.getFloat("tarif");
 			String codePilote = result1.getString("pilote");
 			String codeCopilote = result1.getString("copilote");
 			String codeHotesseSt1 = result1.getString("hotesse_steward1");
 			String codeHotesseSt2 = result1.getString("hotesse_steward2");
 			String codeHotesseSt3 = result1.getString("hotesse_steward3");
 			
 			// on récupère les aéroports de départ et d'arrivée
 			Aeroport aeroportDepart = getAeroportByVille(villeDepart);
 			Aeroport aeroportArrivee = getAeroportByVille(villeArrivee);
 			
 			// on crée un objet Vol avec les informations récupérées
 			Vol v = new Vol(id, aeroportDepart, aeroportArrivee, dateHeureDepart, dateHeureArrivee, duree, tarif, codePilote, codeCopilote, codeHotesseSt1, codeHotesseSt2, codeHotesseSt3);
 			// on l'ajoute à la liste
 			vols.add(v);
 		}
 		connection.close();
 		return vols;
 	}
 
 	// retourne les vols "en attente" sous forme d'une liste
 	public List<Vol> getAllVolsEnAttente() throws SQLException {
 		List<Vol> vols = new ArrayList<>();
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource, user, password);
 		// on crée et exécute une requête préparée pour récupérer les informations sur le vol
 		String sql1 = "SELECT * FROM vol_tmp";
 		PreparedStatement stmt1 = connection.prepareStatement(sql1);
 		ResultSet result1 = stmt1.executeQuery();
 		// on parcourt le résultat
 		while (result1.next()) {
 			String id = result1.getString("numvol");
 			String villeDepart = result1.getString("lieudep");
 			String villeArrivee = result1.getString("lieuarriv");
 			
 			// On récupère les dates sous forme de timestamp. On formatera à l'affichage.
 			Date dateHeureDepart = result1.getTimestamp("dateheuredep");
 			Date dateHeureArrivee = result1.getTimestamp("dateheurearrivee");
 			
 			// on calcule la durée du vol
 			int duree = util.Donnees.getDureeVol(dateHeureDepart, dateHeureArrivee);
 			
 			float tarif = result1.getFloat("tarif");
 			
 			String codePilote = result1.getString("pilote");
 			String codeCopilote = result1.getString("copilote");
 			String codeHotesseSt1 = result1.getString("hotesse_steward1");
 			String codeHotesseSt2 = result1.getString("hotesse_steward2");
 			String codeHotesseSt3 = result1.getString("hotesse_steward3");
 			
 			// on récupère les aéroports de départ et d'arrivée
 			Aeroport aeroportDepart = getAeroportByVille(villeDepart);
 			Aeroport aeroportArrivee = getAeroportByVille(villeArrivee);
 			
 			// on crée un objet Vol avec les informations récupérées
 			Vol v = new Vol(id, aeroportDepart, aeroportArrivee, dateHeureDepart, dateHeureArrivee, duree, tarif,
 					codePilote, codeCopilote, codeHotesseSt1, codeHotesseSt2, codeHotesseSt3);
 			// on l'ajoute à la liste
 			vols.add(v);
 		}
 		connection.close();
 		return vols;
 	}
 
 	// retourne les aéroports sous forme d'une liste
 	public List<Aeroport> getAllAeroports() throws SQLException {
 		List<Aeroport> aeroports = new ArrayList<>();
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource, user, password);
 		// on crée et exécute une requête préparée
 		String sql = "SELECT * FROM destination";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		ResultSet result = stmt.executeQuery();
 		while (result.next()) {
 			String code = result.getString("codeaeroport");
 			String ville = result.getString("ville");
 			String pays = result.getString("pays");
 			// on crée un objet aéroport avec les informations récupérées
 			Aeroport a = new Aeroport(code, ville, pays);
 			// on l'ajoute à la liste
 			aeroports.add(a);
 		}
 		connection.close();
 		return aeroports;
 	}
 
 	// ajoute un nouvel aéroport en base et renvoie "vrai" si l'insertion s'est bien passée
 	public boolean addNewAeroport(Aeroport a) throws SQLException{
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource, user, password);
 		// on regarde si le code aéroport existe déjà en base
 		if(doesAirportAlreadyExist(a.getCodeAeroport())){
 			connection.close();
 			return false; // s'il existe déjà, l'ajout ne se fait pas, on renvoie "false"
 		}else{
 			// sinon, on crée et exécute une requête préparée pour insérer l'aéroport
 			String sql = "INSERT INTO destination values(?, ?, ?)";
 			PreparedStatement stmt = connection.prepareStatement(sql);
 			stmt.setString(1, a.getCodeAeroport());
 			stmt.setString(2, a.getVille());
 			stmt.setString(3, a.getPays());
 			if(stmt.executeUpdate() == 1){ // si le nombre d'enregistrements impactés est "1", c'est ok
 				connection.close();
 				return true;
 			}
 			// S'il y a eu un problème quelconque
 			connection.close();
 			return false;
 		}
 	}
 
 	// ajoute un nouveau vol en base (table vol_tmp)
 	public int addNewVol(Vol v) throws SQLException{
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource, user, password);
 		// on crée et exécute une requête préparée pour insérer le vol
 		String sql = "INSERT INTO vol_tmp VALUES(?, ?, ?, ?, ?, ?, '', '', '', '', '')";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		
 		// On récupère la valeur du prochain id à insérer dans la table vol_tmp.
 		stmt.setString(1, getNextIdVolTmp());
 		
 		stmt.setString(2, v.getAeroportDepart().getVille());
 		stmt.setString(3, v.getAeroportArrivee().getVille());
		// on transforme la date util en timestamp SQL. Pour cela, on utilise le timestamp des dates.
 		// (rq : avec une java.sql.Date, on ne récupèrerait pas les heures et minutes)
 		stmt.setTimestamp(4, new java.sql.Timestamp(v.getDateHeureDepart().getTime()));
 		stmt.setTimestamp(5, new java.sql.Timestamp(v.getDateHeureArrivee().getTime()));
 		
 		stmt.setFloat(6, v.getTarif());
 		
 		int result = stmt.executeUpdate(); // renvoie le nb d'enregistrements impactés
 		connection.close();
 		return result; // doit renvoyer "1"
 	}
 
 	// renvoie un objet Aeroport correspondant à la ville en paramètre
 	public Aeroport getAeroportByVille(String ville) throws SQLException{
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource, user, password);
 		// on crée et exécute une requête préparée
 		String sql = "SELECT * FROM destination WHERE ville = ?";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		// on valorise le paramètre
 		stmt.setString(1, ville);
 		ResultSet result = stmt.executeQuery();
 		// On récupère le résultat de la requête
 		result.next();
 		String code = result.getString("codeaeroport");
 		String pays = result.getString("pays");
 		connection.close();
 		// on crée un objet Aeroport avec les éléments récupérés :
 		return new Aeroport(code, ville, pays);
 	}
 
 	// renvoie un objet Aeroport correspondant au code AITA en paramètre
 	public Aeroport getAeroportByCode(String code) throws SQLException{
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource, user, password);
 		// on crée et exécute une requête préparée
 		String sql = "SELECT * FROM destination WHERE codeaeroport = ?";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		// on valorise le paramètre
 		stmt.setString(1, code);
 		ResultSet result = stmt.executeQuery();
 		// On récupère le résultat de la requête
 		result.next();
 		String ville = result.getString("ville");
 		String pays = result.getString("pays");
 		connection.close();
 		// on crée un objet Aeroport avec les éléments récupérés :
 		return new Aeroport(code, ville, pays);
 	}
 
 	// renvoie vrai si l'aéroport existe déjà en base
 	public boolean doesAirportAlreadyExist(String codeAeroport) throws SQLException{
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource, user, password);
 		// on cherche l'aéroport, avec une requête préparée (rappel = le code aéroport est la clé primaire)
 		String sql = "SELECT * from destination WHERE codeaeroport = ?";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		// on valorise le paramètre
 		stmt.setString(1, codeAeroport);
 		ResultSet result = stmt.executeQuery();
 		// si l'aéroport existe déjà, on renvoie vrai.
 		if (result.next()) {
 			connection.close();
 			return true;
 		}
 		connection.close();
 		return false;
 	}
 
 	// renvoie le prochain ID à insérer dans la table vol_tmp
 	public String getNextIdVolTmp() throws SQLException{
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource, user, password);
 		// On récupère les numvol de la table vol_tmp.
 		String sql = "SELECT numvol FROM vol_tmp";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		ResultSet result = stmt.executeQuery();
 		// On va chercher l'id max de la table. On initialise idMax à zéro.
 		int idMax = 0;
 		// On parcourt les résultats de la requête.
 		while (result.next()) {
 			// On ne prend que la fin de la chaîne. Ex : pour le vol "TMP12", on veut récupérer "12".
 			// On récupère donc la chaine à partir du 4ème caractère (on enlève "TMP")
 			String numvol = result.getString("numvol").substring(3);
 
 			// On transforme la chaîne récupérée en int
 			int nb = Integer.parseInt(numvol);
 			
 			// On récupère la plus grande valeur de la liste
 			if(nb > idMax){
 				idMax = nb;
 			}
 		};		
 		
 		// le prochain ID à insérer correspondra à l'idMax + 1
 		int prochainId = idMax + 1;
 		String prochainIdString = "TMP" + prochainId; // on ajoute le préfixe "TMP"
 		connection.close();
 		return prochainIdString;
 	}
 	
 	// renvoie le prochain ID à insérer dans la table vol
 	public String getNextIdVol() throws SQLException{
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource, user, password);
 		// On récupère les numvol de la table vol
 		String sql = "SELECT numvol FROM vol";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		ResultSet result = stmt.executeQuery();
 		// On va chercher l'id max de la table. On initialise idMax à zéro.
 		int idMax = 0;
 		// On parcourt les résultats de la requête.
 		while (result.next()) {
 			// On ne prend que la fin de la chaîne. Ex : pour le vol "DF10", on veut récupérer "10".
 			// On récupère donc la chaine à partir du 3ème caractère (on enlève "DF")
 			String numvol = result.getString("numvol").substring(2);
 
 			// On transforme la chaîne récupérée en int
 			int nb = Integer.parseInt(numvol);
 			
 			// On récupère la plus grande valeur de la liste
 			if(nb > idMax){
 				idMax = nb;
 			}
 		};		
 		
 		// le prochain ID à insérer correspondra à l'idMax + 1
 		int prochainId = idMax + 1;
 		String prochainIdString = "DF" + prochainId; // on ajoute le préfixe "DF"
 		connection.close();
 		return prochainIdString;
 	}
 
 	// met à jour l'aéroport en paramètre
 	// renvoie "vrai" si la mise à jour s'est bien passée
 	public boolean updateAeroport(Aeroport a) throws SQLException {
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource,user,password);
 		// On vérifie que l'aéroport n'est pas déjà utilisé
 		if(isAirportUsed(a.getCodeAeroport())){ // si c'est le cas, on empêche sa mise à jour
 			return false;
 		}
 		// requête SQL pour mettre à jour l'aéroport :
 		String sql = "UPDATE destination SET ville=?, pays=? WHERE codeaeroport=?";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		stmt.setString(1, a.getVille());
 		stmt.setString(2, a.getPays());
 		stmt.setString(3, a.getCodeAeroport());
 		int result = stmt.executeUpdate(); // retourne le nb d'enregistrements impactés
 		connection.close();
 		if(result == 1){ // la mise à jour s'est bien passée
 			return true;
 		}
 		return false; 
 	}
 
 	// supprime l'aéroport dont le code est passé en paramètre
 	// renvoie vrai si la suppression s'est bien passée
 	public boolean deleteAeroport(String code) throws SQLException {
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource,user,password);
 		// On vérifie que l'aéroport n'est pas déjà utilisé
 		if(isAirportUsed(code)){ // si c'est le cas, on empêche sa suppression
 			connection.close();
 			return false;
 		}
 		// requête SQL pour supprimer l'aéroport :
 		String sql = "DELETE FROM destination WHERE codeaeroport=?";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		// on valorise le paramètre
 		stmt.setString(1, code);
 		int result = stmt.executeUpdate(); // retourne le nb d'enregistrements impactés
 		connection.close();
 		if(result == 1){ // la suppression s'est bien passée
 			return true;
 		}		
 		return false; 
 	}
 
 	// renvoie vrai si un aéroport est utilisé au moins 1 fois (dans les vols programmés ou en attente)
 	public boolean isAirportUsed(String codeAeroport) throws SQLException{
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource, user, password);
 		// on cherche l'aéroport dans les 2 tables
 		String sql1 = "SELECT numvol FROM vol V INNER JOIN destination D ON V.lieudep = D.ville WHERE D.codeaeroport = ?";
 		String sql2 = "SELECT numvol FROM vol_tmp V INNER JOIN destination D ON V.lieudep = D.ville WHERE D.codeaeroport = ?";
 		PreparedStatement stmt1 = connection.prepareStatement(sql1);
 		PreparedStatement stmt2 = connection.prepareStatement(sql2);
 		// on valorise le paramètre
 		stmt1.setString(1, codeAeroport);
 		stmt2.setString(1, codeAeroport);
 		// On exécute les 2 requêtes :
 		ResultSet result1 = stmt1.executeQuery();
 		ResultSet result2 = stmt2.executeQuery();
 		// si l'aéroport est utilisé, on renvoie vrai.
 		if (result1.next()) {
 			connection.close();
 			return true;
 		}else if (result2.next()){
 			connection.close();
 			return true;
 		}else{
 			connection.close();
 			return false;
 		}
 	}
 	
 	// renvoie l'objet Vol correspondant à l'id en paramètre, pour un vol "en attente"
 	public Vol getVolEnAttenteById(String id) throws SQLException{
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource, user, password);
 		// on crée et exécute une requête préparée
 		String sql = "SELECT * FROM vol_tmp WHERE numvol = ?";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		// on valorise le paramètre
 		stmt.setString(1, id);
 		ResultSet result = stmt.executeQuery();
 		// On récupère le résultat de la requête
 		result.next();
 		String villeDepart = result.getString("lieudep");
 		// On récupère l'objet Aeroport correspondant à la ville de départ :
 		Aeroport aeroportDepart = getAeroportByVille(villeDepart);
 		String villeArrivee = result.getString("lieuarriv");
 		// On récupère l'objet Aeroport correspondant à la ville d'arrivée :
 		Aeroport aeroportArrivee = getAeroportByVille(villeArrivee);
 		// On récupère les dates sous forme de timestamp pour avoir les minutes et secondes
 		// (on formatera à l'affichage)
 		Date dateHeureDepart = result.getTimestamp("dateheuredep");
 		Date dateHeureArrivee = result.getTimestamp("dateheurearrivee");
 		// on calcule la durée du vol
 		int duree = util.Donnees.getDureeVol(dateHeureDepart, dateHeureArrivee);
 		float tarif = result.getFloat("tarif");
 		String codePilote = result.getString("pilote");
 		String codeCopilote = result.getString("copilote");
 		String codeHotesseSt1 = result.getString("hotesse_steward1");
 		String codeHotesseSt2 = result.getString("hotesse_steward2");
 		String codeHotesseSt3 =	result.getString("hotesse_steward3");			
 		
 		connection.close();
 		// on crée un objet Vol avec les éléments récupérés :
 		return new Vol(id, aeroportDepart, aeroportArrivee, dateHeureDepart, dateHeureArrivee, duree, tarif, codePilote, codeCopilote, codeHotesseSt1, codeHotesseSt2, codeHotesseSt3);
 	}
 	
 	// renvoie l'objet Vol correspondant à l'id en paramètre, pour un vol "programmé"
 	public Vol getVolProgrammeById(String id) throws SQLException{
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource, user, password);
 		// on crée et exécute une requête préparée
 		String sql = "SELECT * FROM vol V INNER JOIN travailler T ON V.numvol = T.vol WHERE V.numvol = ?";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		// on valorise le paramètre
 		stmt.setString(1, id);
 		ResultSet result = stmt.executeQuery();
 		// On récupère le résultat de la requête
 		result.next();
 		String villeDepart = result.getString("lieudep");
 		// On récupère l'objet Aeroport correspondant à la ville de départ :
 		Aeroport aeroportDepart = getAeroportByVille(villeDepart);
 		String villeArrivee = result.getString("lieuarriv");
 		// On récupère l'objet Aeroport correspondant à la ville d'arrivée :
 		Aeroport aeroportArrivee = getAeroportByVille(villeArrivee);
 		// On récupère les dates sous forme de timestamp pour avoir les minutes et secondes
 		// (on formatera à l'affichage)
 		Date dateHeureDepart = result.getTimestamp("dateheuredep");
 		Date dateHeureArrivee = result.getTimestamp("dateheurearrivee");
 		// on calcule la durée du vol
 		int duree = util.Donnees.getDureeVol(dateHeureDepart, dateHeureArrivee);
 		float tarif = result.getFloat("tarif");
 		String codePilote = result.getString("pilote");
 		String codeCopilote = result.getString("copilote");
 		String codeHotesseSt1 = result.getString("hotesse_steward1");
 		String codeHotesseSt2 = result.getString("hotesse_steward2");
 		String codeHotesseSt3 =	result.getString("hotesse_steward3");			
 		
 		connection.close();
 		// on crée un objet Vol avec les éléments récupérés :
 		return new Vol(id, aeroportDepart, aeroportArrivee, dateHeureDepart, dateHeureArrivee, duree, tarif, codePilote, codeCopilote, codeHotesseSt1, codeHotesseSt2, codeHotesseSt3);
 	}
 	
 	// supprime le vol "en attente" dont le code est passé en paramètre
 	// renvoie vrai si la suppression s'est bien passée
 	public boolean deleteVolEnAttente(String code) throws SQLException {
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource,user,password);
 		// requête SQL pour supprimer le vol "en attente" :
 		String sql = "DELETE FROM vol_tmp WHERE numvol=?";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		// on valorise le paramètre
 		stmt.setString(1, code);
 		int result = stmt.executeUpdate(); // retourne le nb d'enregistrements impactés
 		connection.close();
 		if(result == 1){ // la suppression s'est bien passée
 			return true;
 		}		
 		return false; 
 	}
 	
 	// met à jour le vol "en attente" en paramètre
 	// renvoie "vrai" si la mise à jour s'est bien passée
 	public boolean updateVolEnAttente(Vol v) throws SQLException {
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource,user,password);
 		// requête SQL pour mettre à jour le vol
 		String sql = "UPDATE vol_tmp SET lieudep=?, lieuarriv=?, dateheuredep=?, dateheurearrivee=?, tarif=?,"
 				+ "pilote=?, copilote=?, hotesse_steward1=?, hotesse_steward2=?, hotesse_steward3=?"
 				+ "WHERE numvol=?";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		// On valorise les paramètres
 		stmt.setString(1, v.getAeroportDepart().getVille());
 		stmt.setString(2, v.getAeroportArrivee().getVille());
 		// on transforme la date util en timestamp SQL. Pour cela, on utilise le timestamp des dates.
 		// (rq : avec une java.sql.Date, on ne récupèrerait pas les heures et minutes)
 		stmt.setTimestamp(3, new java.sql.Timestamp(v.getDateHeureDepart().getTime()));
 		stmt.setTimestamp(4, new java.sql.Timestamp(v.getDateHeureArrivee().getTime()));
 		stmt.setFloat(5, v.getTarif());
 		stmt.setString(6, v.getCodePilote());
 		stmt.setString(7, v.getCodeCopilote());
 		stmt.setString(8, v.getCodeHotesseSt1());
 		stmt.setString(9, v.getCodeHotesseSt2());
 		stmt.setString(10, v.getCodeHotesseSt3());
 		stmt.setString(11, v.getId());
 		
 		int result = stmt.executeUpdate(); // retourne le nb d'enregistrements impactés
 		connection.close();
 		if(result == 1){ // la mise à jour s'est bien passée
 			return true;
 		}
 		return false; 
 	}
 	
 	// renvoie une liste des codes des pilotes de la compagnie
 	public List<String> getPilotes() throws SQLException{
 		List<String> pilotes = new ArrayList<>();
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource,user,password);
 		// requête SQL pour récupérer les pilotes
 		String sql = "SELECT numemploye FROM employe WHERE lower(fonction) = 'pilote'";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		ResultSet result = stmt.executeQuery();
 		while (result.next()) {
 			pilotes.add(result.getString("numemploye")); // on ajoute chaque numéro de pilote à la liste
 		}
 		connection.close();
 		return pilotes;
 	}
 	
 	// renvoie une liste des codes des copilotes de la compagnie
 	public List<String> getCopilotes() throws SQLException{
 		List<String> copilotes = new ArrayList<>();
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource,user,password);
 		// requête SQL pour récupérer les copilotes
 		String sql = "SELECT numemploye FROM employe WHERE lower(fonction) = 'copilote'";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		ResultSet result = stmt.executeQuery();
 		while (result.next()) {
 			copilotes.add(result.getString("numemploye")); // on ajoute chaque numéro de copilote à la liste
 		}
 		connection.close();
 		return copilotes;
 	}
 	
 	// renvoie une liste des codes des hotesses et stewards de la compagnie
 	public List<String> getHotessesSt() throws SQLException{
 		List<String> hotessesSt = new ArrayList<>();
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource,user,password);
 		// requête SQL pour récupérer les hôtesses et stewards
 		String sql = "SELECT numemploye FROM employe WHERE lower(fonction) IN ('hotesse','steward')";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		ResultSet result = stmt.executeQuery();
 		while (result.next()) {
 			hotessesSt.add(result.getString("numemploye")); // on ajoute chaque numéro d'hôtesse/steward à la liste
 		}
 		connection.close();
 		return hotessesSt;
 	}
 	
 	// met à jour le tarif pour un vol programmé
 	// prend en paramètre le numéro de vol et le nouveau tarif
 	// renvoie vrai si la mise à jour s'est bien passée
 	public boolean updateTarifVolProgramme(String numVol, float tarif) throws SQLException{
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource,user,password);
 		// requête SQL pour mettre à jour le tarif
 		String sql = "UPDATE vol SET tarif=? WHERE numvol=?";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		// On valorise les paramètres
 		stmt.setFloat(1, tarif);
 		stmt.setString(2, numVol);
 		
 		int result = stmt.executeUpdate(); // retourne le nb d'enregistrements impactés
 		connection.close();
 		if(result == 1){ // la mise à jour s'est bien passée
 			return true;
 		}
 		return false; 
 	}
 	
 	// supprime le vol "programmé" dont le code est passé en paramètre
 	// si il n'y a pas de réservation dessus
 	// renvoie vrai si la suppression s'est bien passée
 	public boolean deleteVolProgramme(String numVol) throws SQLException {
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource,user,password);
 		// on vérifie au préalable qu'aucune réservation n'a été faite sur le vol
 		// si c'est le cas, on renvoie "false" et on ne supprime pas le vol
 		if(volReserve(numVol)){
 			return false;
 		}
 		// requête SQL pour supprimer le lien entre les employés et le vol à supprimer :
 		// (rappel : il y a forcément des employés affectés sur un vol "programmé")
 		String sql1 = "DELETE FROM travailler WHERE vol = ?";
 		// requête SQL pour supprimer le vol
 		String sql2 = "DELETE FROM vol WHERE numvol = ?";
 		// on prépare les 2 requêtes
 		PreparedStatement stmt1 = connection.prepareStatement(sql1);
 		PreparedStatement stmt2 = connection.prepareStatement(sql2);
 		// on valorise le paramètre pour les 2 requêtes
 		stmt1.setString(1, numVol);
 		stmt2.setString(1, numVol);
 		if(stmt1.executeUpdate() == 0){ // renvoie le nb de lignes impactées. Si aucune, il y a eu un pb, on renvoie false.
 			connection.close();
 			return false;
 		}
 		if(stmt2.executeUpdate() == 0){ // renvoie le nb de lignes impactées. Si aucune, il y a eu un pb, on renvoie false.
 			connection.close();
 			return false;
 		}
 		connection.close();
 		return true; // la suppression s'est bien passée
 	}
 	
 	// renvoie vrai s'il y a au moins une réservation sur le vol dont l'id est en paramètre
 	public boolean volReserve(String numVol) throws SQLException{
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource,user,password);
 		// on cherche une réservation sur le vol
 		String sql = "SELECT numreservation FROM place WHERE numvol = ?";
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		// on valorise le paramètre
 		stmt.setString(1, numVol);
 		// On exécute la requête :
 		ResultSet result = stmt.executeQuery();
 		// si au moins une réservation a été trouvée, on renvoie vrai.
 		if (result.next()) {
 			connection.close();
 			return true;
 		}
 		connection.close();
 		return false;
 	}
 	
 	// ajoute les 5 codes employés en paramètres au vol programmé
 	// renvoie vrai si tout s'est bien passé
 	private boolean addEmployeeToVolProgramme(String numVol, String pilote, String copilote, String HS1, String HS2, String HS3, java.sql.Date dateVol) throws SQLException{
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource, user, password);
 		// requête préparée pour indiquer les employés travaillant sur ce vol
 		String sql = "INSERT INTO travailler VALUES(?, ?, ?, ?, ?, ?, ?)";
 		// on prépare la requête
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		// on valorise les paramètres
 		stmt.setString(1, numVol);
 		stmt.setString(2, pilote);
 		stmt.setString(3, copilote);
 		stmt.setString(4, HS1);
 		stmt.setString(5, HS2);
 		stmt.setString(6, HS3);
 		stmt.setDate(7, dateVol); // date SQL !
 		if(stmt.executeUpdate() == 0){ // renvoie le nb de lignes impactées. Si aucune, il y a eu un pb, on renvoie false.
 			connection.close();
 			return false;
 		}
 		connection.close();
 		return true;
 	}
 	
 	// pour passer un vol de "vol en attente" (vol_tmp) à "vol programmé" (vol) :
 	// la méthode ci-dessous insère simplement un vol programmé en base,
 	// et un trigger fait en sorte de supprimer le vol en attente correspondant
 	public boolean confirmVol(Vol v) throws SQLException{
 		// on se connecte à la BDD
 		Connection connection = DriverManager.getConnection(datasource, user, password);
 		// requête préparée pour insérer le vol
 		String sql = "INSERT INTO vol VALUES(?, ?, ?, ?, ?, ?)";
 		// on prépare la requête
 		PreparedStatement stmt = connection.prepareStatement(sql);
 		// on valorise les paramètres
 		String idVol = getNextIdVol(); // on récupère le prochain id à insérer
 		stmt.setString(1, idVol);
 		stmt.setString(2, v.getAeroportDepart().getVille());
 		stmt.setString(3, v.getAeroportArrivee().getVille());
 		// on transforme la date util en timestamp SQL. Pour cela, on utilise le timestamp des dates.
 		// (rq : avec une java.sql.Date, on ne récupèrerait pas les heures et minutes)
 		stmt.setTimestamp(4, new java.sql.Timestamp(v.getDateHeureDepart().getTime()));
 		stmt.setTimestamp(5, new java.sql.Timestamp(v.getDateHeureArrivee().getTime()));
 		stmt.setFloat(6, v.getTarif());
 		if(stmt.executeUpdate() == 0){ // renvoie le nb de lignes impactées. Si aucune, il y a eu un pb, on renvoie false.
 			connection.close();
 			return false; // on ne continue pas
 		}
 		// si l'ajout du vol s'est bien passé, on lie les employés au vol
 		boolean employe = addEmployeeToVolProgramme(idVol, v.getCodePilote(), v.getCodeCopilote(), v.getCodeHotesseSt1(), v.getCodeHotesseSt2(), v.getCodeHotesseSt3(), new java.sql.Date(v.getDateHeureDepart().getTime()));
 		if(!employe){
 			connection.close();
 			return false; // si ça s'est mal passé, on sort de la boucle
 		}
 		return true; // si tout s'est bien passé
 	}
 }
