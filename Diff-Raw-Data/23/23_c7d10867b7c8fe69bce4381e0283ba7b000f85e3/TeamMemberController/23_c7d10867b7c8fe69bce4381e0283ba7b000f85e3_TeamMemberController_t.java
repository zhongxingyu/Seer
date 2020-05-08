 package controllers;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import views.ErrorMessage;
 import database.DatabaseManager;
 import database.MalformedQueryException;
 import database.SQLQueryInsert;
 import database.SQLQuerySelect;
 import database.SQLQueryUpdate;
 
 public class TeamMemberController {
 	private OperationController _operation;
 	private DatabaseManager _dbm;
 	private int _id;
 	private String _name;
 	private String _firstName;
 	private String _phoneNumber;
 	private String _othersInformations;
 	private EntityController _entity;
 	private boolean _available;
 
 	private int _temporaryEntityId;
 
 	/**
 	 * Constructor for a team -member. All team-members are already define in database.
 	 * @param id Id in the database
 	 * @param name Name in the database
 	 * @param firstName First name in the database
 	 * @param phoneNumber Phone number in the database
 	 * @param othersInformations Others informations about the team-member in the database.
 	 * @param entityId Id of the entity which team member belongs.
 	 * @param available
 	 */
 	public TeamMemberController(OperationController operation, DatabaseManager dbm, int id, String name, String firstName, String phoneNumber, String othersInformations, int entityId){
 		_operation = operation;
 		_dbm = dbm;
 
 		_id = id;
 		_name = name;
 		_firstName = firstName;
 		_phoneNumber = phoneNumber;
 		_othersInformations = othersInformations;		
 		_temporaryEntityId = entityId;
 		
 		if (entityId == 0)
 			_available = true;
 		else
 			_available = false;
 	}
 
 	public boolean joinEntity(EntityController entity) {
 		if(_entity == null){
 			java.util.Date date = new java.util.Date();
 			java.sql.Timestamp datetime = new java.sql.Timestamp(date.getTime());
 			int entityId = entity.getId();
 
 			//Update 'EntiteHistorique'
 			try{
 				_dbm.executeQueryInsert(new SQLQueryInsert("EntiteHistorique", "(NULL,"+_id+","+entityId+",'"+datetime+"',NULL)"));	
 			}catch(MalformedQueryException e){
				new ErrorMessage(_operation.getGlobalPanel().getMapPanel(),"Erreur interne - Ajout équipier" ,"Une erreur est survenue lors de l'assignation de "+_firstName+" "+_name+" à l'équipe "+entity.getName()+".");
 			}
 
 			//Update 'EntityId' pour l'equipier
 			try{
 				_dbm.executeQueryUpdate(new SQLQueryUpdate("Equipier", "entite_id="+entityId+", operation_id='"+_operation.getId()+"'","id = "+_id));
 			}catch(MalformedQueryException e){ 
				new ErrorMessage(_operation.getGlobalPanel().getMapPanel(),"Erreur interne - Ajout équipier" ,"Une erreur est survenue lors de l'assignation de "+_firstName+" "+_name+" à l'équipe "+entity.getName()+".");
 			}
 			_entity = entity;
 			System.out.println("ENTITY A CHANGE: Elle est maintenant ---> "+_entity.getName());
 			_available = false;
 			return true;
 		}
 		else{
 			new ErrorMessage(_operation.getGlobalPanel().getMapPanel(), _firstName+" "+_name+" est déjà assigné à l'entité "+_entity.getName()+". Il doit d'abord quitter cette équipe pour être réassigné");
 			return false;
 		}
 	}
 
 	public boolean leaveEntity(){
 		java.util.Date date = new java.util.Date();
 		java.sql.Timestamp datetime = new java.sql.Timestamp(date.getTime());
 		ResultSet result;
 
 		try{
 			//Get the id of the line in "EntiteHistorique" table
 			System.out.println("Enite ID = "+_entity.getId());
 			result = _dbm.executeQuerySelect(new SQLQuerySelect("id", "EntiteHistorique", "equipier_id ="+_id+" AND entite_id="
 					+_entity.getId()+" AND date_fin is NULL"));
 
 			try{
 				if(result.next()){
 					int idEntityHistory = result.getInt("id");
 					//Update "date_depart" in table "EntiteHistorique" for the team-member
 					_dbm.executeQueryUpdate(new SQLQueryUpdate("EntiteHistorique", "date_fin='"+datetime+"'","id="+idEntityHistory));
 
 					//Update a jour 'Entity_id' in Equipier table
 					try{
 						_dbm.executeQueryUpdate(new SQLQueryUpdate("Equipier", "entite_id=NULL, operation_id=NULL","id="+_id));	
 					}catch(MalformedQueryException e){
 						new ErrorMessage(_operation.getGlobalPanel().getMapPanel(),"Erreur interne - Base de donnees" ,"Une erreur est survenue lors de la desafectation de l'entite_id à l'équipier "+_firstName+" "+_name);
 					}
 				}
 				else{
 					return false;
 				}
 			}catch(SQLException e){ System.err.println("EntityController#removeTeamMember(): Error on finding entityHistory_id for teamMemberId "+_id+" and entity ID "+_entity.getId()); }
 
 		}catch(MalformedQueryException e){e = new MalformedQueryException("EntityController#removeTeamMember() : Error creation history");}
 
 		_entity = null;
 		_available = true;
 		return true;
 	}
 
 	public String show(){
 		System.out.println("On est ici pour l'equipier : "+_firstName);
 		String result;
 		result = "Nom :"+_name+"\t";
 		result += "Prenom:"+_firstName+"\t";
 		result += "Tel: "+_phoneNumber+"\t";
 		System.out.println("1");
 		if(_othersInformations.length() != 0){
 			System.out.println("2");
 			result+= "Informations supplementaires :"+_othersInformations+"\n";
 		}
 		System.out.println("3");
 		result += "Disponibilite : "+_available+"\n";
 		if(_entity != null){
 			result += "Entite : "+_entity.getName();
 		}
 
 		return result;
 	}
 	/**
 	 * @return the _id
 	 */
 	public int getId() {
 		return _id;
 	}
 
 	/**
 	 * @return the _nom
 	 */
 	public String getName() {
 		return _name;
 	}
 
 	/**
 	 * @return the _prenom
 	 */
 	public String getFirstName() {
 		return _firstName;
 	}
 
 
 	/**
 	 * @return the _tel
 	 */
 	public String getPhoneNumber() {
 		return _phoneNumber;
 	}
 
 
 	/**
 	 * @return the _autres
 	 */
 	public String getOthersInformation() {
 		return _othersInformations;
 	}
 
 	/**
 	 * @return the entite
 	 */
 	public EntityController getEntity() {
 		return _entity;
 	}
 
 	public boolean isActive() {
 		return ! _available;
 	}
 
 	public void set_disponible(boolean _disponible) {
 		_available = _disponible;
 	}
 
 	public void setEntity(EntityController entity){
 		_entity = entity;
 	}
 
 	public void loadEntity() {
 		if(_temporaryEntityId == 0){
 			_entity = null;
 		}
 		else{
 			_entity = _operation.getEntity(_temporaryEntityId);
 		}
 	}
 
 	public void updateFields() {
 		ResultSet result;
 		try {
 			result = _dbm.executeQuerySelect(new SQLQuerySelect("*","Equipier", "id="+_id));
 			while(result.next()){
 				_name = result.getString("nom");
 				_firstName = result.getString("prenom");
 				_phoneNumber = result.getString("tel");
 				_othersInformations = result.getString("autres");
 				if(_othersInformations == null){
 					_othersInformations = "";
 				}
 				System.out.println("ON UPDATE ENTITE");
 				_entity = _operation.getEntity(result.getInt("entite_id"));
 
				if(result.getInt("entite_id") == 0)
 					_available = true;					
 				else
 					_available = false;
 			}
 
 			result.getStatement().close();
 		}catch (SQLException e) { 
 			new ErrorMessage(_operation.getGlobalPanel().getMapPanel(), "Erreur dans la mise à jour des attributs de l'équipier '"+_name+"'.");
 		}catch(MalformedQueryException e1) {
 			new ErrorMessage(_operation.getGlobalPanel().getMapPanel(), "Erreur dans la mise à jour des attributs de l'équipier '"+_name+"'.");
 		}		
 	}
 
 }
