 package ClassAdminBackEnd;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.LinkedList;
 
 import javax.activity.InvalidActivityException;
 
 import org.tmatesoft.sqljet.core.SqlJetException;
 import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
 import org.tmatesoft.sqljet.core.table.ISqlJetTable;
 import org.tmatesoft.sqljet.core.table.SqlJetDb;
 
 public class EntityType {
 	private String name;
 	private LinkedList<Format> formatting;
 	private LinkedList<BorderCase> borderCasing;
 	private LinkedList<SuperEntity> entityList;
 	private EntityType parentEntitytype;
 	private LinkedList<EntityType> subEntityType;
 	private Boolean isTextField;
 	private Boolean isRule = false;
 	private Date date;
 	private Double defaultWeight;
 	private long ID;
 	private int maxValue = 100;
 	
 	public Boolean getIsRule() {
 		return isRule;
 	}
 	public void setIsRule(Boolean isRule) {
 		this.isRule = isRule;
 	}
 	public int getMaxValue() {
 		return maxValue;
 	}
 	public void setMaxValue(int maxValue) {
 		this.maxValue = maxValue;
 	}
 	
 
 	public EntityType getParentEntitytype() {
 		return parentEntitytype;
 	}
 
 	public void setParentEntitytype(EntityType parentEntitytype) {
 		this.parentEntitytype = parentEntitytype;
 	}
 
 	public LinkedList<EntityType> getSubEntityType() {
 		if (this.subEntityType == null)
 			this.subEntityType = new LinkedList<EntityType>();
 		return subEntityType;
 	}
 
 	/**
 	 * @return the iD
 	 */
 	public long getID() {
 		return ID;
 	}
 
 	public EntityType(String n) {
 		name = n;
 	}
 
 	/**
 	 * @param name
 	 * @param fields
 	 * @param visibleFields
 	 * @param fieldDefaults
 	 * @param formatting
 	 * @param borderCasing
 	 * @param entityList
 	 * @param isTextField
 	 * @param date
 	 * @param isVisible
 	 * @param defaultWeight
 	 */
 	public EntityType(String name, LinkedList<Format> formatting,
 			LinkedList<BorderCase> borderCasing,
 			LinkedList<SuperEntity> entityList, Boolean isTextField, Date date,
 			Double defaultWeight) {
 		this.name = name;
 		this.formatting = formatting;
 		this.borderCasing = borderCasing;
 		this.entityList = entityList;
 		this.isTextField = isTextField;
 		this.date = date;
 		this.defaultWeight = defaultWeight;
 	}
 
 	/**
 	 * @param name
 	 * @param parentEntitytype
 	 * @param isTextField
 	 * @param date
 	 * @param defaultWeight
 	 */
 	public EntityType(String name, EntityType parentEntitytype,
 			Boolean isTextField, Date date, Double defaultWeight) {
 		this.name = name;
 		this.parentEntitytype = parentEntitytype;
 		if(parentEntitytype != null)
 		parentEntitytype.getSubEntityType().add(this);
 		this.isTextField = isTextField;
 		this.date = date;
 		this.defaultWeight = defaultWeight;
 	}
 
 	public EntityType() {
 		// TODO Auto-generated constructor stub
 	}
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param formatting the formatting to set
 	 */
 	public void setFormatting(LinkedList<Format> formatting) {
 		this.formatting = formatting;
 	}
 	/**
 	 * @param borderCasing the borderCasing to set
 	 */
 	public void setBorderCasing(LinkedList<BorderCase> borderCasing) {
 		this.borderCasing = borderCasing;
 	}
 	/**
 	 * @param entityList the entityList to set
 	 */
 	public void setEntityList(LinkedList<SuperEntity> entityList) {
 		this.entityList = entityList;
 	}
 	/**
 	 * @param subEntityType the subEntityType to set
 	 */
 	public void setSubEntityType(LinkedList<EntityType> subEntityType) {
 		this.subEntityType = subEntityType;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public LinkedList<Format> getFormatting() {
 		if (formatting == null)
 			formatting = new LinkedList<Format>();
 		return formatting;
 	}
 
 	public LinkedList<BorderCase> getBorderCasing() {
 		if (borderCasing == null)
 			borderCasing = new LinkedList<BorderCase>();
 		return borderCasing;
 	}
 
 	public LinkedList<SuperEntity> getEntityList() {
 		if (entityList == null)
 			entityList = new LinkedList<SuperEntity>();
 		return entityList;
 	}
 
 	public Boolean getIsTextField() {
 		return isTextField;
 	}
 
 	public void setIsTextField(Boolean isTextField) {
 		this.isTextField = isTextField;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public Double getDefaultWeight() {
 		return defaultWeight;
 	}
 
 	public void setDefaultWeight(Double defaultWeight) {
 		this.defaultWeight = defaultWeight;
 	}
 
 	public void saveToDB(SqlJetDb db, Long parentID, PDatIDGenerator idgen)
 			throws SqlJetException {
 		db.beginTransaction(SqlJetTransactionMode.WRITE);
 
 		// TODO
 		ISqlJetTable table = db.getTable(PDatExport.ENTITY_TYPE_TABLE);
 		// insert statements
 		this.ID = idgen.getID();
 		table.insert(this.ID, this.name, parentID, this.isTextField, this.date,
 				this.defaultWeight);
 
 
 		for (int x = 0; x < this.getBorderCasing().size(); ++x) {
 			this.getBorderCasing().get(x).saveToDB(db, this.ID, idgen);
 		}
 		for (int x = 0; x < this.getFormatting().size(); ++x) {
 			this.getFormatting().get(x).saveToDB(db, this.ID, idgen);
 		}
 
 		for (int x = 0; x < this.getSubEntityType().size(); ++x) {
 			this.getSubEntityType().get(x).saveToDB(db, this.ID, idgen);
 		}
 	}
 
 	public void changeParent(EntityType newParent) {
 		boolean fail = false;
 		for (int x = 0; x < this.getEntityList().size(); ++x) {
 			try {
 				this.getEntityList().get(x).changeParentTotype(newParent);
 			} catch (InvalidActivityException e) {
 				undoChange(x);
 				x = this.getEntityList().size();
 				fail = true;
 			}
 
 		}
 		
 		if(!fail){
 			this.getParentEntitytype().getSubEntityType().remove(this);
 			this.setParentEntitytype(newParent);
 			newParent.getSubEntityType().add(this);
 		}
 	}
 	
 	public void undoChange(int num){
 		for (int x = 0; x < num; ++x) {
 			try {
 				this.getEntityList().get(x).changeParentTotype(this);
 			} catch (InvalidActivityException e) {
 
 			}
 
 		}
 	}
 
 	public String createTreeFromHead(LinkedList<EntityType> treeLinkedList)
 	{
 		treeLinkedList.add(this);
 		if(this.getSubEntityType().size()>0){
 			String str = "";
 			str += "<branch>" +
 					"<attribute name = \"name\" value= \"" + this.getName() + "\" />";
 			for (int i = 0; i < this.getSubEntityType().size(); i++)
 			{
 				str += this.getSubEntityType().get(i).createTreeFromHead(treeLinkedList);
 			}
 			str +="</branch>";
 			return str;
 		} else{
 			String str = "";
 			str += "<leaf>" +
 					"<attribute name = \"name\" value= \"" + this.getName() + "\" />";
 			str +="</leaf>";
 			return str;
 		}
 	}
 	
 	public void populateTreeWithEntities(){
 		for(int x = 0;x<this.getParentEntitytype().getEntityList().size();++x){
 			SuperEntity parent = this.getParentEntitytype().getEntityList().get(x);
 			if(this.getIsRule()){
 				if(this.getIsTextField()){
 					new StringRuleEntity(this, parent, "");
 					
 				} else{
 					new floatRuleEntity(this, parent);
 				}
 			} else {
 				if(this.getIsTextField()){
 					new LeafStringEntity(this, parent,"<"+this.getName()+">");
 				} else{
 					new LeafMarkEntity(this, parent, 0);
 				}
 			}
 			
 		}
 	}
 	public void removeDeletingChildren(){
 		for(int x = 0;x<this.getEntityList().size();++x){
 			this.getEntityList().get(x).getParentEntity().getSubEntity().remove(this.getEntityList().get(x));
 			this.getParentEntitytype().getSubEntityType().remove(this);
 			this.getEntityList().clear();
 		}
		this.getParentEntitytype().getSubEntityType().remove(this);
		this.setParentEntitytype(null);
 	}
 	
 	public void removeSavingChildren(){
 		for(int x = 0;x<this.getEntityList().size();++x){
 			for(int y = 0;y<this.getEntityList().get(x).getSubEntity().size();++x){
 				this.getEntityList().get(x).getParentEntity().getSubEntity().add(this.getEntityList().get(x).getSubEntity().get(y));
 				this.getEntityList().get(x).getSubEntity().get(y).setParentEntity(this.getEntityList().get(x).getParentEntity());
 			}
 		}
 		for(int x = 0;x<this.getSubEntityType().size();++x){
 			this.getSubEntityType().get(x).setParentEntitytype(this.getParentEntitytype());
 			this.getParentEntitytype().getSubEntityType().add(this.getSubEntityType().get(x));
 		}
 		removeDeletingChildren();
 	}
 
 }
