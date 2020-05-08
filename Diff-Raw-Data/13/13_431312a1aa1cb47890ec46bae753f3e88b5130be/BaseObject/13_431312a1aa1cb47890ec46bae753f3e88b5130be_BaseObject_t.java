 package com.podio.crunchbaseapi;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.joda.time.DateTime;
 
 public class BaseObject {
 
 	private String permalink;
 
 	private String crunchbaseUrl;
 
 	private String homepageUrl;
 
 	private String blogUrl;
 
 	private String blogFeedUrl;
 
 	private String twitterUsername;
 
 	private String tagList;
 
 	private String aliasList;
 
 	private DateTime createdAt;
 
 	private DateTime updatedAt;
 
 	private String overview;
 
 	public String getPermalink() {
 		return permalink;
 	}
 
 	public void setPermalink(String permalink) {
 		this.permalink = permalink;
 	}
 
 	@JsonProperty("crunchbase_url")
 	public String getCrunchbaseUrl() {
 		return crunchbaseUrl;
 	}
 
 	@JsonProperty("crunchbase_url")
 	public void setCrunchbaseUrl(String crunchbaseUrl) {
 		this.crunchbaseUrl = crunchbaseUrl;
 	}
 
 	@JsonProperty("homepage_url")
 	public String getHomepageUrl() {
 		return homepageUrl;
 	}
 
 	@JsonProperty("homepage_url")
 	public void setHomepageUrl(String homepageUrl) {
 		this.homepageUrl = homepageUrl;
 	}
 
 	@JsonProperty("blog_url")
 	public String getBlogUrl() {
 		return blogUrl;
 	}
 
 	@JsonProperty("blog_url")
 	public void setBlogUrl(String blogUrl) {
 		this.blogUrl = blogUrl;
 	}
 
 	@JsonProperty("blog_feed_url")
 	public String getBlogFeedUrl() {
 		return blogFeedUrl;
 	}
 
 	@JsonProperty("blog_feed_url")
 	public void setBlogFeedUrl(String blogFeedUrl) {
 		this.blogFeedUrl = blogFeedUrl;
 	}
 
 	@JsonProperty("twitter_username")
 	public String getTwitterUsername() {
 		return twitterUsername;
 	}
 
 	@JsonProperty("twitter_username")
 	public void setTwitterUsername(String twitterUsername) {
 		this.twitterUsername = twitterUsername;
 	}
 
 	@JsonProperty("tag_list")
 	public String getTagList() {
 		return tagList;
 	}
 
 	@JsonProperty("tag_list")
 	public void setTagList(String tagList) {
 		this.tagList = tagList;
 	}
 
 	private List<String> toCSV(String in) {
 		if (in == null) {
 			return Collections.<String> emptyList();
 		}
 
 		List<String> split = new ArrayList<String>();
 		String[] split2 = in.split(",");
 		for (String v : split2) {
			String trimmed = v.trim();
			if (trimmed.length() > 0) {
				split.add(trimmed);
			}
 		}
 
 		return split;
 	}
 
 	public List<String> getTags() {
 		return toCSV(this.tagList);
 	}
 
 	@JsonProperty("alias_list")
 	public String getAliasList() {
 		return aliasList;
 	}
 
 	@JsonProperty("alias_list")
 	public void setAliasList(String aliasList) {
 		this.aliasList = aliasList;
 	}
 
 	public List<String> getAliases() {
 		return toCSV(this.aliasList);
 	}
 
 	@JsonProperty("created_at")
 	public DateTime getCreatedAt() {
 		return createdAt;
 	}
 
 	@JsonProperty("created_at")
 	public void setCreatedAt(DateTime createdAt) {
 		this.createdAt = createdAt;
 	}
 
 	@JsonProperty("updated_at")
 	public DateTime getUpdatedAt() {
 		return updatedAt;
 	}
 
 	@JsonProperty("updated_at")
 	public void setUpdatedAt(DateTime updatedAt) {
 		this.updatedAt = updatedAt;
 	}
 
 	public String getOverview() {
 		return overview;
 	}
 
 	public void setOverview(String overview) {
 		this.overview = overview;
 	}
 }
