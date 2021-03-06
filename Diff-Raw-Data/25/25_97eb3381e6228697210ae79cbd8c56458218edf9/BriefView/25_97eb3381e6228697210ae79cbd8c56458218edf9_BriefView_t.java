 package eu.europeana.api2.web.model.json;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
 
 import eu.europeana.api2.web.model.json.common.Profile;
 import eu.europeana.api2.web.util.OptOutDatasetsUtil;
 import eu.europeana.corelib.definitions.model.ThumbSize;
 import eu.europeana.corelib.definitions.solr.DocType;
 import eu.europeana.corelib.definitions.solr.beans.BriefBean;
 import eu.europeana.corelib.solr.bean.impl.IdBeanImpl;
 
 @JsonSerialize(include = Inclusion.NON_EMPTY)
 public class BriefView extends IdBeanImpl implements BriefBean {
 
 	protected final Logger log = Logger.getLogger(getClass().getName());
 
 	protected static final String RECORD_PATH = "/v2/record/";
	protected static final String PORTAL_PATH = "/record/";
	protected static final String RECORD_EXT = ".json";
	protected static final String PORTAL_PARAMS = ".html?utm_source=api&utm_medium=api&utm_campaign=";
	protected static final String WSKEY_PARAM = "?wskey=";
 	protected static final String IMAGE_SITE = "http://europeanastatic.eu/api/image";
 	protected static final String URI_PARAM = "?uri=";
 	protected static final String SIZE_PARAM = "&size=";
 	protected static final String TYPE_PARAM = "&type=";
 
 	protected static String apiUrl;
	protected static String portalUrl;
 
 	private String id;
 	private Date timestamp;
 	private String[] provider;
 	private String[] edmDataProvider;
 	private String[] edmObject;
 	private int europeanaCompleteness;
 	private DocType docType;
 	private String[] language;
 	private String[] year;
 	private String[] rights;
 	private String[] title;
 	private String[] dcCreator;
 	private String[] dcContributor;
 	private String[] edmPlace;
 	private List<Map<String, String>> edmPlacePrefLabel;
 	private Float edmPlaceLatitude;
 	private Float edmPlaceLongitude;
 	private String[] edmTimespan;
 	private List<Map<String, String>> edmTimespanLabel;
 	private String[] edmTimespanBegin;
 	private String[] edmTimespanEnd;
 	private String[] edmAgentTerm;
 	private List<Map<String, String>> edmAgentLabel;
 	private String[] dctermsHasPart;
 	private String[] dctermsSpatial;
 	private String[] edmPreview;
 	private boolean isOptedOut;
 
 	protected String profile;
 	private List<String> thumbnails;
 	protected String wskey;
 
 	public BriefView(BriefBean bean, String profile, String wskey) {
 		this.profile = profile;
 		this.wskey = wskey;
 
 		id = bean.getId();
 		timestamp = bean.getTimestamp();
 		title = bean.getTitle();
 		dcCreator = bean.getDcCreator();
 		docType = bean.getType();
 		year = bean.getYear();
 		edmPlaceLatitude = bean.getEdmPlaceLatitude();
 		edmPlaceLongitude = bean.getEdmPlaceLongitude();
 		provider = bean.getProvider();
 		edmDataProvider = bean.getDataProvider();
 		rights = bean.getRights();
 		edmObject = bean.getEdmObject();
 		europeanaCompleteness = bean.getEuropeanaCompleteness();
 		language = bean.getLanguage();
 		dcContributor = bean.getDcContributor();
 		edmPlace = bean.getEdmPlace();
 		edmPlacePrefLabel = bean.getEdmPlaceLabel();
 		edmTimespan = bean.getEdmTimespan();
 		edmTimespanLabel = bean.getEdmTimespanLabel();
 		edmTimespanBegin = bean.getEdmTimespanBegin();
 		edmTimespanEnd = bean.getEdmTimespanEnd();
 		edmAgentTerm = bean.getEdmAgent();
 		edmAgentLabel = bean.getEdmAgentLabel();
 		dctermsHasPart = bean.getDctermsHasPart();
 		dctermsSpatial = bean.getDctermsSpatial();
 		isOptedOut = bean.isOptedOut();
 		edmPreview = bean.getEdmPreview();
 	}
 
 	public String getProfile() {
 		return null; // profile;
 	}
 
 	public void setProfile(String profile) {
 		this.profile = profile;
 	}
 
 	@Override
 	public String[] getTitle() {
 		return title;
 	}
 
 	@Override
 	public String[] getEdmObject() {
 		return null;
 	}
 
 	@Override
 	public String[] getYear() {
 		return year;
 	}
 
 	@Override
 	public String[] getProvider() {
 		return provider;
 	}
 
 	@Override
 	public String[] getDataProvider() {
 		return edmDataProvider;
 	}
 
 	@Override
 	public String[] getLanguage() {
 		if (isProfile(Profile.MINIMAL)) {
 			return null;
 		}
 		return language;
 	}
 
 	@Override
 	public String[] getRights() {
 		return rights;
 	}
 
 	@Override
 	public DocType getType() {
 		return docType;
 	}
 
 	@Override
 	public String[] getDctermsSpatial() {
 		if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
 			return null;
 		}
 		return dctermsSpatial;
 	}
 
 	@Override
 	public int getEuropeanaCompleteness() {
 		return europeanaCompleteness;
 	}
 
 	@Override
 	public String[] getEdmPlace() {
 		if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
 			return null;
 		}
 		return edmPlace;
 	}
 
 	@Override
 	public List<Map<String, String>> getEdmPlaceLabel() {
 		if (isProfile(Profile.MINIMAL)) {
 			return null;
 		}
 		return edmPlacePrefLabel;
 	}
 
 	@Override
 	public Float getEdmPlaceLatitude() {
 		return edmPlaceLatitude;
 	}
 
 	@Override
 	public Float getEdmPlaceLongitude() {
 		return edmPlaceLongitude;
 	}
 
 	@Override
 	public String[] getEdmTimespan() {
 		if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
 			return null;
 		}
 		return edmTimespan;
 	}
 
 	@Override
 	public List<Map<String, String>> getEdmTimespanLabel() {
 		if (isProfile(Profile.MINIMAL)) {
 			return null;
 		}
 		return edmTimespanLabel;
 	}
 
 	@Override
 	public String[] getEdmTimespanBegin() {
 		if (isProfile(Profile.MINIMAL)) {
 			return null;
 		}
 		return edmTimespanBegin;
 	}
 
 	@Override
 	public String[] getEdmTimespanEnd() {
 		if (isProfile(Profile.MINIMAL)) {
 			return null;
 		}
 		return edmTimespanEnd;
 	}
 
 	@Override
 	public String[] getEdmAgent() {
 		if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
 			return null;
 		}
 		return edmAgentTerm;
 	}
 
 	@Override
 	public List<Map<String, String>> getEdmAgentLabel() {
 		if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
 			return null;
 		}
 		return edmAgentLabel;
 	}
 
 	@Override
 	public String[] getDctermsHasPart() {
 		return null; // dctermsHasPart;
 	}
 
 	@Override
 	public String[] getDcCreator() {
 		return dcCreator;
 	}
 
 	@Override
 	public String[] getDcContributor() {
 		if (isProfile(Profile.MINIMAL) || isProfile(Profile.STANDARD)) {
 			return null;
 		}
 		return dcContributor;
 	}
 
 	@Override
 	public Date getTimestamp() {
 		return null; // timestamp;
 	}
 
 	@Override
 	public String getId() {
 		return id;
 	}
 
 	@Override
 	public Boolean isOptedOut() {
 		return null; // isOptedOut;
 	}
 
 	public List<String> getThumbnails() {
 		if (thumbnails == null) {
 			thumbnails = new ArrayList<String>();
 
 			if (!OptOutDatasetsUtil.checkById(getId()) && edmObject != null) {
 				for (String object : edmObject) {
 					String tn = StringUtils.defaultIfBlank(object, "");
 					StringBuilder url = new StringBuilder(IMAGE_SITE);
 					try {
 						url.append(URI_PARAM).append(URLEncoder.encode(tn, "UTF-8"));
 					} catch (UnsupportedEncodingException e) {
 						e.printStackTrace();
 					}
 					url.append(SIZE_PARAM).append(ThumbSize.LARGE);
 					url.append(TYPE_PARAM).append(getType().toString());
 					thumbnails.add(url.toString());
 				}
 			}
 		}
 		return thumbnails;
 	}
 
 	public String getLink() {
 		StringBuilder url = new StringBuilder(apiUrl);
		url.append(RECORD_PATH).append(getId()).append(RECORD_EXT);
 		url.append(WSKEY_PARAM).append(wskey);
 		return url.toString();
 	}
 
	public String getGuid() {
		StringBuilder url = new StringBuilder(portalUrl);
		url.append(PORTAL_PATH).append(getId());
		url.append(PORTAL_PARAMS).append(wskey);
		return url.toString();
	}

 	@Override
 	public String[] getEdmPreview() {
 		return edmPreview;
 	}
 
 	protected boolean isProfile(Profile _profile) {
 		return profile.toLowerCase().equals(_profile.getName());
 	}
 
 	public static void setApiUrl(String _apiUrl) {
 		apiUrl = _apiUrl;
 	}

	public static void setPortalUrl(String _portalUrl) {
		portalUrl = _portalUrl;
	}
 }
