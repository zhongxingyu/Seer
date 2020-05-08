 package controllers;
 
 import java.awt.Color;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import observer.Observer;
 import observer.Subject;
 
 import views.ErrorMessage;
 
 import database.DatabaseManager;
 import database.MalformedQueryException;
 import database.SQLQueryInsert;
 import database.SQLQuerySelect;
 import database.SQLQueryUpdate;
 
 public class EntityController implements Subject {
 	//Ceci est un test
 	private OperationController _operation;	
 	private DatabaseManager _dbm;
 
 	private String _name;
 	private String _type;
 	private String _infos;
 	private int _id;
 	private java.sql.Timestamp _dateArriveeLocalisation;
 	private String _color;
 
 	private boolean _available;
 	private String _state;
 	private int _stateId;
 
 	private List<Observer> _observers = new ArrayList<Observer>();
 
 	private int _posCurrentId;
 
 	private List<TeamMemberController> _teamMemberList = new ArrayList<TeamMemberController>();
 
 
 	/**
 	 * Constructor for creation of an entity which is not in the database
 	 * @param operation The current operation
 	 * @param name Name of the entity
 	 * @param type Type of the entity
 	 * @param infos Other informations about the entity
 	 * @param color 
 	 */
 	public EntityController(OperationController operation, DatabaseManager dbm, String name, String type, String infos, String color){
 		_dbm = dbm;
 		_name = name;
 		_type = type;
 		_infos = infos;
 		_available = true;
 		_posCurrentId = operation.getIdPcm();
 		_operation = operation;
 		_color = color;
 
 		int idOperation = operation.getId();
 		java.util.Date date = new java.util.Date();
 		java.sql.Timestamp datetime = new java.sql.Timestamp(date.getTime());
 
 		_dateArriveeLocalisation = datetime;
 
 		//Creation of a new state in database
 		int result;
 
 		try {
 			result = _dbm.executeQueryInsert(new SQLQueryInsert("Statut" ,"(NULL,NULL,'1','"+datetime+"','Creation entite')"));
 			_stateId = result;
 		} catch (MalformedQueryException e) { 
 			new ErrorMessage(_operation.getGlobalPanel().getMapPanel(),"Erreur interne - Base de donnees" ,"Une erreur est survenue lors de la creation du statut pour l'entité '"+name+"'");}
 
 
 		//Creation of the entity in database
 		int result2;
 
 		try {
 			System.out.println("here");
 			result2 = _dbm.executeQueryInsert(new SQLQueryInsert("Entite", "(NULL,'"+_stateId+"','"+_posCurrentId+"',"+operation.getIdOperateur()+",'"+idOperation+"','"+name+"','"+datetime+"','"+type+"','"+color+"','"+infos+"')"));
 			System.out.println("here2");
 			_id = result2;
 			System.out.println("ID de l'entite qui vient d'être créé :"+_id);
 		} catch (MalformedQueryException e) { e.printStackTrace(); }	
 
 
 		//Update of the status of the entity
 		System.out.println("Mise a jour du statut "+_stateId+" qui vient d'etre cree avec IDEntite = "+_id);
 		try {
 			_dbm.executeQueryUpdate(new SQLQueryUpdate("Statut","entite_id = '"+_id+"'", "id='"+_stateId+"'"));
 		} catch (MalformedQueryException e) { new ErrorMessage(_operation.getGlobalPanel().getMapPanel(), "H"); }
 
 		_operation.addEntite(this);
 	}
 
 
 	/**
 	 * Constructor for creation of an instance for an existant entity in database
 	 * @param dbm The database manager
 	 * @param id The id of the entity in the database
 	 * @param statut_id The id of the status in the database 
 	 * @param position_id The id of the position in the database
 	 * @param name The name of the entity in the database
 	 * @param type The type of the entity in the database
 	 * @param infos The informations about the entity in database
 	 */
 	public EntityController(OperationController operation, DatabaseManager dbm, int id, int stateId, int positionId, java.sql.Timestamp dateArriveeLocalisation, String name, String type, String infos, String color){
 		System.out.println("Creation instance Entite deja existante (id = "+id+" ).");
 
 		_operation = operation;
 		_dbm = dbm;
 		_id = id;
 		_stateId = stateId;
 		_posCurrentId = positionId;
 		_name = name;
 		_type = type;
 		_infos = infos;
 		_dateArriveeLocalisation = dateArriveeLocalisation;
 		_color = color;
 
 		ResultSet result;
 
 		//On va regarder le statut pour indiquer la disponibilite de l'equipe
 		try {
 			result = _dbm.executeQuerySelect(new SQLQuerySelect("dispo", "Statut", "id='"+stateId+"'"));
 			try{
 				while(result.next()){
 					_available = result.getBoolean("dispo");
 				}
 			}catch(SQLException e){	System.err.println("EntityController#EntityController(): Error in the recovery of 'dispo' in table STATUT for id "+stateId);}
 		} catch (MalformedQueryException e1) { e1.printStackTrace();}
 
 
 		//On ajoute les equipiers presents dans l'entite
 		try {
 			System.out.println("Chargement equipiers déjà présents dans l'équipe "+_name);
 			result = _dbm.executeQuerySelect(new SQLQuerySelect("id", "Equipier", "entite_id='"+_id+"'"));
 			try{
 				while(result.next()){
 					int idEquipier = result.getInt("id");
 					System.out.println("Ajout team member dans "+_name+" de "+operation.getEquipier(idEquipier).getFirstName()+" "+operation.getEquipier(idEquipier).getName());
 					_teamMemberList.add(operation.getEquipier(idEquipier));
 				}
 			}catch (SQLException e) { System.err.println("EntityController#EntityController(): Error on adding teamMember"+stateId);}
 		} catch (MalformedQueryException e1) { e1.printStackTrace();}
 
 	} 
 
 	/**
 	 * Add a team-member to the entity and update "Entity_historique" table
 	 * @param teamMember The teamMember to add
 	 */
 	public void addTeamMember(TeamMemberController teamMember){
 		if(teamMember.joinEntity(this)){
 			_teamMemberList.add(teamMember);
 		}
 	}
 
 	public void removeTeamMember(TeamMemberController teamMember){
 		if(teamMember.leaveEntity()){
 			_teamMemberList.remove(teamMember);
 		}
 		notifyObservers();
 	}
 
 	public List<TeamMemberController> getTeamMemberList(){
 		return _teamMemberList;
 	}
 
 	public boolean isAvailable(){
 		return _available;
 	}
 
 
 	public void setAvailable(boolean state, String infos){
 		java.util.Date date = new java.util.Date();
 		java.sql.Timestamp datetime = new java.sql.Timestamp(date.getTime());
 		int result;
 
 		//Create new status for the entity and update of the variable class "_available" and "stateId"
 		try {
 			result = _dbm.executeQueryInsert(new SQLQueryInsert("Statut" ,"(NULL,"+_id+","+state+",'"+datetime+"','"+infos+"')"));
 			_stateId = result;
 		} catch (MalformedQueryException e) { 
 			new ErrorMessage(_operation.getGlobalPanel().getMapPanel(),"Erreur interne" ,"Impossible de mettre à jour le statut pour l'entite \n"+_name);
 		}
 
 		try {
 			_dbm.executeQueryUpdate(new SQLQueryUpdate("Entite","statut_id ="+_stateId,"id ="+_id));
 		} catch (MalformedQueryException e) { 
 			new ErrorMessage(_operation.getGlobalPanel().getMapPanel(),"Erreur interne" ,"Impossible de mettre à jour l'ID statut pour l'entite \n"+_name);
 		}
 
 		_available = state;
 		_state = infos;
 		_operation.notifyObservers();
 		//genererMessageStatut();
 	}
 
 	public String show(){
 		String result = "---------------------------\n";
 		result+= "Nom : "+_name+" \n";
 		result+= "Type: "+_type+"\n";
 		result+= "Infos:"+_infos+"\n";
 		result+= "Id:"+_id+"\n";
 		result+= "PosCurrantID"+_posCurrentId+"\n";
 		result+= "Disponibilite:"+_available+" (ID statut = "+_stateId+")\n";
 		if(!_available){
 			result+="Cause : "+_state+"\n";
 		}
 		if(_teamMemberList.size() != 0){
 			result += "Liste des equipiers: \n";
 			for(TeamMemberController teammember : _teamMemberList){
 				result += teammember.show();
 			}
 		}
 
 		result += "\n---------------------------\n";
 		return result;
 	}
 
 	public int getId() {
 		return _id;
 	}
 
 	public String getName() {
 		return _name;
 	}
 
 	public int getIdPosCurrent(){
 		return _posCurrentId;
 	}
 
 	public boolean getStatut(){
 		return _available;
 	}
 
 	public String getColor() {
 		// TODO Auto-generated method stub
 		return _color;
 	}
 
 	public java.sql.Timestamp getDateArriveeLocalisation(){
 		return _dateArriveeLocalisation;
 	}
 
 	public void setColor(String newColor){
 		try{
 			_dbm.executeQueryUpdate(new SQLQueryUpdate("Entite", "couleur='"+newColor+"'","id ="+_id));
 		}catch(MalformedQueryException e) {
 			new ErrorMessage(_operation.getGlobalPanel().getMapPanel(),"Erreur interne" ,"Impossible de mettre à jour la couleur pour l'entite \n"+_name);
 		}
 
 		_color = newColor;
 	}
 
 	public void setLocation(LocationController location) {	
 		int lastPosCurrentId = _posCurrentId;
 
 		LocationController loc = _operation.getLocation(_posCurrentId);
 		if(loc != null) loc.removeEntity(this);
 
 		if(location == null){
 			_posCurrentId = _operation.getIdPcm();
 
 		}else{
 			_posCurrentId = location.getId();
 		}
 
 		try{
 			_dbm.executeQueryUpdate(new SQLQueryUpdate("Entite", "pos_courante_id ="+_posCurrentId,"id ="+_id));
 		}catch(MalformedQueryException e) {
 			new ErrorMessage(_operation.getGlobalPanel().getMapPanel(),"Erreur interne" ,"Impossible de mettre à jour la position courante pour l'entite \n"+_name);
 		}
 
 		int idDeplacement = _operation.getLocation(location.getId()).addEntity(this);
 
 		java.util.Date date = new java.util.Date();
 		java.sql.Timestamp _dateArriveeLocalisation = new java.sql.Timestamp(date.getTime());
 
 		try{
 			_dbm.executeQueryUpdate(new SQLQueryUpdate("Entite", "date_depart ='"+_dateArriveeLocalisation+"'","id ="+_id));
 		}catch(MalformedQueryException e) {
 			new ErrorMessage(_operation.getGlobalPanel().getMapPanel(),"Erreur interne" ,"Impossible de mettre à jour la date d'arrivée à la localisation \n pour l'entite "+_name);
 		}
 
 		genererMessageDeplacement(lastPosCurrentId, idDeplacement);
 
 		_operation.notifyObservers();
 	}
 
 	public void setName(String newName){
 		try{
 			_dbm.executeQueryUpdate(new SQLQueryUpdate("Entite", "nom='"+newName+"'","id ="+_id));
 		}catch(MalformedQueryException e) {
 			new ErrorMessage(_operation.getGlobalPanel().getMapPanel(),"Erreur interne" ,"Impossible de mettre à jour le nom pour l'entite \n"+_name);
 		}
 
 		_name = newName;
 	}
 
 	/***
 	 * Generate message in database for table "Messages", to alert daybook of the move of the entity.
 	 * @param lastPosCurrentId
 	 */
 	public void genererMessageDeplacement(int lastPosCurrentId,int idDeplacement){
 		java.util.Date date = new java.util.Date();
 		java.sql.Timestamp datetime = new java.sql.Timestamp(date.getTime());
 		String message =  _name+" a quitté \""+_operation.getLocation(lastPosCurrentId).getName()+"\" pour \""+_operation.getLocation(_posCurrentId).getName()+"\".";
 
 		try {
 			_dbm.executeQueryInsert(new SQLQueryInsert("Message" ,"(NULL,NULL,NULL,'-1','-1',"+_operation.getIdOperateur()+", NULL, "+_operation.getId()+",NULL, '"+idDeplacement+"', '"+datetime+"','"+message+"','0')"));
 		} catch (MalformedQueryException e) {
 			new ErrorMessage(_operation.getGlobalPanel().getMapPanel(),"Erreur génération message" ,"Une erreur est survenue lors de la génération du message pour la main courante \n"+
 					"Message : "+message);
 		}
 	}
 
 	/***
 	 * Generate message in database for table "Messages", to alert daybook of the change of the status.
 	 */
 	public void genererMessageStatut(){
 		String disponibility;
 
 		java.util.Date date = new java.util.Date();
 		java.sql.Timestamp datetime = new java.sql.Timestamp(date.getTime());
 
 		if(_available){
 			disponibility = "disponible";
 		}else{
 			disponibility = "indisponible";
 		}
 
 		String message =  _name+" est maintenant "+disponibility+" (Informations: "+_state+" ).";
 
 		try {
 			int id = _operation.getId();
 			_dbm.executeQueryInsert(new SQLQueryInsert("Message" ,"(NULL,NULL,NULL,'-1','-1',"+_operation.getIdOperateur()+",NULL,"+id+",NULL,NULL,'"+datetime+"','"+message+"',0)"));
 		} catch (MalformedQueryException e) {
 			new ErrorMessage(_operation.getGlobalPanel().getMapPanel(),"Erreur generation message" ,"Une erreur est survenue lors de la génération du message pour la main courante \n"+
 					"Message : "+message);
 		}
 	}
 
 
 	@Override
 	public void addObserver(Observer observer) {
 		_observers.add(observer);
 	}
 
 
 	@Override
 	public void removeObserver(Observer observer) {
 		_observers.remove(observer);
 	}
 
 	@Override
 	public void notifyObservers() {
 		for(Observer oneObserver : _observers) {
 			oneObserver.update();
 		}
 	}
 
 	public boolean isTypeInterne() {
 		if(_type.compareTo("Interne") == 0)
 			return true;
 		else
 			return false;
 	}
 
 
 	public void updateFields() {
 		ResultSet result;
 		try {
 			result = _dbm.executeQuerySelect(new SQLQuerySelect("*", "Entite", "id="+_id));
 			while(result.next()){
 				_stateId = result.getInt("statut_id");
 				_name = result.getString("nom");
 				_dateArriveeLocalisation = result.getTimestamp("date_depart");
 				_posCurrentId = result.getInt("pos_courante_id");
 
 				try {
 					ResultSet result2 = _dbm.executeQuerySelect(new SQLQuerySelect("dispo", "Statut", "id='"+_stateId+"'"));
 					try{
 						while(result.next()){
 							_available = result2.getBoolean("dispo");
 							_state = result2.getString("infos");
 						}
 						result2.close();
 					}catch(SQLException e){
 						new ErrorMessage(_operation.getGlobalPanel().getMapPanel(),"Erreur interne", "Une erreur est survenue lors de la récupération des attributs du statut de l'entité '"+_name+"'.");
 					}
 				}catch (MalformedQueryException e1) {
 					new ErrorMessage(_operation.getGlobalPanel().getMapPanel(),"Erreur interne", "Une erreur est survenue lors de la récupération des attributs du statut de l'entité '"+_name+"'.");
 				}
 
 				//On ajoute les equipiers presents dans l'entite
 				_teamMemberList.clear();
 				try {
 					ResultSet result2 = _dbm.executeQuerySelect(new SQLQuerySelect("id", "Equipier", "entite_id='"+_id+"'"));
 					try{
 						while(result2.next()){
 							int idEquipier = result2.getInt("id");
 							_teamMemberList.add(_operation.getEquipier(idEquipier));
 						}
 						result2.close();
 					}catch (SQLException e) { 
 						new ErrorMessage(_operation.getGlobalPanel().getMapPanel(),"Erreur interne", "Une erreur est survenue lors de la récupération des attributs de composition de l'équipe '"+_name+"'.");
 						e.printStackTrace();
 					}
 				} catch (MalformedQueryException e1) { 
 					new ErrorMessage(_operation.getGlobalPanel().getMapPanel(),"Erreur interne", "Une erreur est survenue lors de la récupération des attributs de composition de l'équipe '"+_name+"'.");
 				}
 			}
 			result.getStatement().close();
 		}catch(SQLException e){
 			e.printStackTrace();
 			new ErrorMessage(_operation.getGlobalPanel().getMapPanel(), "Erreur interne", "Une erreur est survenue lors de la mise à jour des informartions \n pour l'entité +'"+_name+"'.");
 		}catch(MalformedQueryException e){
 			new ErrorMessage(_operation.getGlobalPanel().getMapPanel(), "Erreur interne", "Une erreur est survenue lors de la mise à jour des informartions \n pour l'entité +'"+_name+"'.");
 		}
 	}
 
 
 	public void loadLocation() {
		System.out.println("Entity : "+_name+"POS CURANTE ID : "+_posCurrentId);
		if( (_posCurrentId != _operation.getIdPcm()) && (!_operation.getLocation(_posCurrentId).getEntityList().contains(this)) )
 			_operation.getLocation(_posCurrentId).addEntity(this);		
 	}
 
 }
 
