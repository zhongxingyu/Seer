 package transfer.forms;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import services.ComboService;
 import transfer.bussiness.Publication;
 
 public class PublicationForm {
 
 	private int publicationId;
 	private int userId;
 	private int type;
 	private String typeDesc=null;
 	private int operation_type;
 	private String operationTypeDesc=null;
 	private String address;
 	private String city;
 	private String price;
 	private int environments;
 	private String covered;
 	private String uncovered;
 	private String age;
 	private boolean cable;
 	private boolean phone;
 	private boolean pool;
 	private boolean living;
 	private boolean paddle;
 	private boolean barbecue;
 	private String description;
 
 	public PublicationForm() {
 		publicationId = -1;
 		userId = -1;
 		type = 0;
 		operation_type = 0;
 		address = "";
 		city = "";
 		price = "";
 		environments = 0;
 		covered = "";
 		uncovered = "";
 		age = "";
 		cable = false;
 		phone = false;
 		pool = false;
 		living = false;
 		paddle = false;
 		barbecue = false;
 		description = "";
 	}
 
 	public PublicationForm(int publicationId, int userId, int type,
 			int operation_type, String address, String city, String price,
 			int environments, String covered, String uncovered, String age,
 			boolean cable, boolean phone, boolean pool, boolean living,
 			boolean paddle, boolean barbecue, String description) {
 		super();
 		this.publicationId = publicationId;
 		this.userId = userId;
 		this.type = type;
 		this.operation_type = operation_type;
 		this.address = address;
 		this.city = city;
 		this.price = price;
 		this.environments = environments;
 		this.covered = covered;
 		this.uncovered = uncovered;
 		this.age = age;
 		this.cable = cable;
 		this.phone = phone;
 		this.pool = pool;
 		this.living = living;
 		this.paddle = paddle;
 		this.barbecue = barbecue;
 		this.description = description;
 	}
 
 	public PublicationForm(Publication p) {
 		publicationId = p.getPublicationId();
 		userId = p.getUserId();
 		type = p.getType();
 		operation_type = p.getOperation_type();
 		address = p.getAddress();
 		city = p.getCity();
 		price = String.valueOf(p.getPrice());
 		environments = p.getEnvironments();
 		covered = String.valueOf(p.getCovered());
 		uncovered = String.valueOf(p.getUncovered());
 		age = String.valueOf(p.getAge());
 		cable = p.isCable();
 		phone = p.isPhone();
 		pool = p.isPool();
 		living = p.isLiving();
 		paddle = p.isPaddle();
 		barbecue = p.isBarbecue();
 		description = p.getDescription();
 	}
 
 	public Publication toBussiness() {
 		Publication p = new Publication(publicationId, userId, type,
 				operation_type, address, city, Float.valueOf(price),
 				environments, Float.valueOf(covered), Float.valueOf(uncovered),
 				Integer.valueOf(age), cable, phone, pool, living, paddle,
 				barbecue, description);
 		return p;
 	}
 
 	public int getPublicationId() {
 		return publicationId;
 	}
 
 	public void setPublicationId(int publicationId) {
 		this.publicationId = publicationId;
 	}
 
 	public int getUserId() {
 		return userId;
 	}
 
 	public void setUserId(int userId) {
 		this.userId = userId;
 	}
 
 	public int getType() {
 		return type;
 	}
 
 	public void setType(int type) {
 		this.type = type;
 	}
 
 	public int getOperation_type() {
 		return operation_type;
 	}
 
 	public void setOperation_type(int operation_type) {
 		this.operation_type = operation_type;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	public String getCity() {
 		return city;
 	}
 
 	public void setCity(String city) {
 		this.city = city;
 	}
 
 	public String getPrice() {
 		return price;
 	}
 
 	public void setPrice(String price) {
 		this.price = price;
 	}
 
 	public int getEnvironments() {
 		return environments;
 	}
 
 	public void setEnvironments(int environments) {
 		this.environments = environments;
 	}
 
 	public String getCovered() {
 		return covered;
 	}
 
 	public void setCovered(String covered) {
 		this.covered = covered;
 	}
 
 	public String getUncovered() {
 		return uncovered;
 	}
 
 	public void setUncovered(String uncovered) {
 		this.uncovered = uncovered;
 	}
 
 	public String getAge() {
 		return age;
 	}
 
 	public void setAge(String age) {
 		this.age = age;
 	}
 
 	public boolean isCable() {
 		return cable;
 	}
 
 	public void setCable(boolean cable) {
 		this.cable = cable;
 	}
 
 	public boolean isPhone() {
 		return phone;
 	}
 
 	public void setPhone(boolean phone) {
 		this.phone = phone;
 	}
 
 	public boolean isPool() {
 		return pool;
 	}
 
 	public void setPool(boolean pool) {
 		this.pool = pool;
 	}
 
 	public boolean isLiving() {
 		return living;
 	}
 
 	public void setLiving(boolean living) {
 		this.living = living;
 	}
 
 	public boolean isPaddle() {
 		return paddle;
 	}
 
 	public void setPaddle(boolean paddle) {
 		this.paddle = paddle;
 	}
 
 	public boolean isBarbecue() {
 		return barbecue;
 	}
 
 	public void setBarbecue(boolean barbecue) {
 		this.barbecue = barbecue;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getTypeDesc(){
 		if(typeDesc == null){
 			ComboService cs = ComboService.getInstance();
 			List<Combo> typeList = cs.getTypes();
 			List<Combo> oTypeList = cs.getOperationTypes();
 			typeDesc = typeList.get(type-1).getDescription();
 			operationTypeDesc = oTypeList.get(operation_type-1).getDescription();
 		}
 		return typeDesc;
 	}
 	
 	public String getOperationTypeDesc(){
 		if(operationTypeDesc == null){
 			ComboService cs = ComboService.getInstance();
 			List<Combo> typeList = cs.getTypes();
 			List<Combo> oTypeList = cs.getOperationTypes();
 			typeDesc = typeList.get(type-1).getDescription();
 			operationTypeDesc = oTypeList.get(operation_type-1).getDescription();
 		}
 		return operationTypeDesc;
 	}
 	
 	public List<String> validate(){
 		List<String> errors = new ArrayList<String>();
		
		if( address.length() >30 )
			errors.add("Direccin demasiado larga.");
		if( city.length() > 30 )
			errors.add("Ciudad demasiado larga.");
		try{
			Double.parseDouble(price);
		}catch(Exception e){
			errors.add("Error en el precio. Ingrese un nmero real.");
		}
		try{
			Double.parseDouble(covered);
		}catch(Exception e){
			errors.add("Error la superficie cubierta. Ingrese un nmero real.");
		}
		try{
			Double.parseDouble(uncovered);
		}catch(Exception e){
			errors.add("Error en la superficie descubierta. Ingrese un nmero real.");
		}
		try{
			Integer.parseInt(age);
		}catch(Exception e){
			errors.add("Error en la antigedad. Ingrese un nmero entero.");
		}
		
 		return errors;
 	}
 }
