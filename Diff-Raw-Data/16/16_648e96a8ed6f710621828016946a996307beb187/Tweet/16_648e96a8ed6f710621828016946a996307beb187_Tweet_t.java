 package toctep.skynet.backend.dal.domain;
 
 import java.sql.Date;
 
 public class Tweet extends DomainLongPk {
 
 	private String text;
 	private Geo geo;
 	private boolean truncated;
 	private long twitterId;
 	private SourceType sourceType;
 	private boolean favorited;
 	private long inReplyToTweetTwitterId;
 	private long inReplyToUserTwitterId;
 	private long retweetCount;
 	private Date createdAt;
 	private Place place;
 	private User user;
 	private String coordinates;
 	
 	public String getText() {
 		return text;
 	}
 
 	public void setText(String text) {
 		this.text = text;
 	}
 
 	public Geo getGeo() {
 		return geo;
 	}
 
 	public void setGeo(Geo geo) {
 		this.geo = geo;
 	}
 
 	public boolean isTruncated() {
 		return truncated;
 	}
 
 	public void setTruncated(boolean truncated) {
 		this.truncated = truncated;
 	}
 
 	public long getTwitterId() {
 		return twitterId;
 	}
 
 	public void setTwitterId(long twitterId) {
 		this.twitterId = twitterId;
 	}
 
 	public SourceType getSourceType() {
 		return sourceType;
 	}
 
 	public void setSourceType(SourceType sourceType) {
 		this.sourceType = sourceType;
 	}
 
 	public boolean isFavorited() {
 		return favorited;
 	}
 
 	public void setFavorited(boolean favorited) {
 		this.favorited = favorited;
 	}
 
 	public long getInReplyToTweetTwitterId() {
 		return inReplyToTweetTwitterId;
 	}
 
 	public void setInReplyToTweetTwitterId(long inReplyToTweetTwitterId) {
 		this.inReplyToTweetTwitterId = inReplyToTweetTwitterId;
 	}
 
 	public long getInReplyToUserTwitterId() {
 		return inReplyToUserTwitterId;
 	}
 
 	public void setInReplyToUserTwitterId(long inReplyToUserTwitterId) {
 		this.inReplyToUserTwitterId = inReplyToUserTwitterId;
 	}
 
 	public long getRetweetCount() {
 		return retweetCount;
 	}
 
 	public void setRetweetCount(long retweetCount) {
 		this.retweetCount = retweetCount;
 	}
 
 	public Date getCreatedAt() {
 		return createdAt;
 	}
 
 	public void setCreatedAt(Date createdAt) {
 		this.createdAt = createdAt;
 	}
 
 	public Place getPlace() {
 		return place;
 	}
 
 	public void setPlace(Place place) {
 		this.place = place;
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	public String getCoordinates() {
 		return coordinates;
 	}
 
 	public void setCoordinates(String coordinates) {
 		this.coordinates = coordinates;
 	}
 
 	@Override
 	public void setDao() {
 		dao = daoFacade.getTweetDao();
 	}
 	
 	@Override
 	public void save() {
		sourceType.save();
 		geo.save();
 		place.save();
 		user.save();
		this.sourceType.setId(sourceType.getId());
 		this.geo.setId(geo.getId());
 		this.place.setId(place.getId());
 		this.user.setId(user.getId());
 		super.save();
 	}	
 }
