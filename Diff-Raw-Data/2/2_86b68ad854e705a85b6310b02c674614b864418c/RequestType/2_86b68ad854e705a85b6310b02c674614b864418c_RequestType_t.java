 package gov.usgs;
 
 public enum RequestType {
 	well_log("/wfs?request=GetFeature&typeName=gwml:WaterWell&INFO_FORMAT=text/xml&featureId=", "LOG"),
 	water_level("/sos?request=GetObservation&featureId=", "/cocoon/gin/gwdp/cache/agency/", "WATERLEVEL"),
 	water_quality("/qw?mimeType=xml&siteid=", "", "QUALITY"),
 	download("?featureId=", "/cocoon/gin/gwdp/cache/download/xls/", "ALL");
 	//download("?featureId=", "/cocoon/gin/gwdp/download/xls/");
 
 
 	// TODO: switch this to 
 	public static final String serverBase = DebugSettings.SERVER_BASE; // used to switch between local and prod development
 	public static final String mediatorPath = "/cocoon/gin/gwdp/agency/";
 
 	protected final String path;
 	protected final String serviceQuery;
 	protected final String typeName;
 
 	private RequestType(String serviceQuery, String typeName) {
 		// TODO: remove mediator path
 		this(serviceQuery, mediatorPath, typeName);
 	}
 
 	private RequestType(String serviceQuery, String path, String typeName) {
 		this.path = path;
 		this.serviceQuery = serviceQuery;
 		this.typeName = typeName;
 	}
 
 	public String makeCacheUrl(String agency, String siteId) {
 		assert(agency != null && siteId != null);
		return serverBase + "/ngwmn/data?agencyID=" + agency + "&featureID=" + siteId + "&type=REGISTRY&type=LITHOLOGY&type=CONSTRUCTION&type=WATERLEVEL&type=QUALITY&bundled=TRUE";
 	}
 	
 	public String makeRESTUrl(String agency, String siteId) {
 		assert(agency != null && siteId != null);
 		return serverBase + path + agency + serviceQuery + siteId;
 	}
 	
 	public String makeCacheRESTUrl(String agency, String siteId) {
 		assert(agency != null && siteId != null);
 		return serverBase + "/ngwmn/data/" + normalizeAgency(agency) + "/" + siteId + "/" + this.typeName;
 	}
 	
 	public static String normalizeAgency(String value) {
 		// TODO: eventually eliminate this business rule
 		return value.replaceAll("_", "%20");
 	}
 }
