 package fr.cormier.domain.xml.getuser;
 
 import org.simpleframework.xml.Attribute;
 import org.simpleframework.xml.Element;
 
 public class Position {
 
 	@Attribute(required=false)
 	private String date;
 	
 	@Attribute(required=false)
 	private String dateGMT;
 	
 	@Element
 	private String latitude;
 	
 	@Element
 	private String longitude;
 	
 	@Element
 	private int voile;
 	
 	@Element
 	private int cap;
 	
 	@Element
 	private float distancerestante;
 	
 	@Element
 	private float distanceparcourue;
 	
 	@Element
 	private int classement;
 	
 	@Element(required=false)
 	private int checkpoint;
 	
 	@Element(required=false)
 	private MessagesSystem MessagesSystem;
 	
 	@Element(required=false)
 	private int diffClassement;
 	
 	@Element(required=false)
 	private String IsArrived;
 
 	@Element(required=false)
 	private String temps_etape;
 	
 	@Element(required=false)
 	private int id_parcours;
 	
 	@Element(required=false)
 	private String id_parcours_xml;
 	
	@Element(required=false)
	private boolean option_voile_auto;
	
	@Element(required=false)
	private boolean option_voile_auto_activated;
 	
 	
	public boolean isOption_voile_auto() {
		return option_voile_auto;
	}

	public void setOption_voile_auto(boolean option_voile_auto) {
		this.option_voile_auto = option_voile_auto;
	}

	public boolean isOption_voile_auto_activated() {
		return option_voile_auto_activated;
	}

	public void setOption_voile_auto_activated(boolean option_voile_auto_activated) {
		this.option_voile_auto_activated = option_voile_auto_activated;
	}

 	public String getId_parcours_xml() {
 		return id_parcours_xml;
 	}
 
 	public void setId_parcours_xml(String id_parcours_xml) {
 		this.id_parcours_xml = id_parcours_xml;
 	}
 
 	public String getDateGMT() {
 		return dateGMT;
 	}
 
 	public void setDateGMT(String dateGMT) {
 		this.dateGMT = dateGMT;
 	}
 
 	public int getId_parcours() {
 		return id_parcours;
 	}
 
 	public void setId_parcours(int id_parcours) {
 		this.id_parcours = id_parcours;
 	}
 
 	public String getIsArrived() {
 		return IsArrived;
 	}
 
 	public String getTemps_etape() {
 		return temps_etape;
 	}
 
 	public void setTemps_etape(String temps_etape) {
 		this.temps_etape = temps_etape;
 	}
 
 	public void setIsArrived(String isArrived) {
 		this.IsArrived = isArrived;
 	}
 
 	public MessagesSystem getMessagesSystem() {
 		return MessagesSystem;
 	}
 
 	public void setMessagesSystem(MessagesSystem messagesSystem) {
 		this.MessagesSystem = messagesSystem;
 	}
 
 	public String getDate() {
 		return date;
 	}
 
 	public void setDate(String date) {
 		this.date = date;
 	}
 
 	public String getLatitude() {
 		return latitude;
 	}
 
 	public void setLatitude(String latitude) {
 		this.latitude = latitude;
 	}
 
 	public String getLongitude() {
 		return longitude;
 	}
 
 	public void setLongitude(String longitude) {
 		this.longitude = longitude;
 	}
 
 	public int getVoile() {
 		return voile;
 	}
 
 	public void setVoile(int voile) {
 		this.voile = voile;
 	}
 
 	public int getCap() {
 		return cap;
 	}
 
 	public void setCap(int cap) {
 		this.cap = cap;
 	}
 
 	public float getDistancerestante() {
 		return distancerestante;
 	}
 
 	public void setDistancerestante(float distancerestante) {
 		this.distancerestante = distancerestante;
 	}
 
 	public float getDistanceparcourue() {
 		return distanceparcourue;
 	}
 
 	public void setDistanceparcourue(float distanceparcourue) {
 		this.distanceparcourue = distanceparcourue;
 	}
 
 	public int getClassement() {
 		return classement;
 	}
 
 	public void setClassement(int classement) {
 		this.classement = classement;
 	}
 
 	public int getCheckpoint() {
 		return checkpoint;
 	}
 
 	public void setCheckpoint(int checkpoint) {
 		this.checkpoint = checkpoint;
 	}
 
 	public int getDiffClassement() {
 		return diffClassement;
 	}
 
 	public void setDiffClassement(int diffClassement) {
 		this.diffClassement = diffClassement;
 	}
 	
 	
 }
