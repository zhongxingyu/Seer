 package com.basicapps.redditpics.model;
 
 import android.text.Html;
 import android.text.Spanned;
 
 import com.basicapps.redditpics.adapter.ItemId;
 import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 import com.fasterxml.jackson.annotation.JsonProperty;
 
 @JsonIgnoreProperties({ "kind" })
 public class Post extends ItemId {
 
 	@JsonProperty("data")
 	private PostData mPostData;
 	public PostData getPostData() { return mPostData; }
 
 	@JsonIgnoreProperties({ "domain", "banned_by", "media_embed", "subreddit",
 			"selftext_html", "selftext", "likes", "link_flair_text", "clicked",
 			"score", "approved_by", "hidden", "subreddit_id", "edited",
 			"link_flair_css_class", "author_flair_css_class", "saved",
			"is_self", "created", "author_flair_text", "media", "num_reports","distinguished" })
 	public static class PostData {
 		@JsonProperty("id")
 		private String mId;
 
 		@JsonProperty("title")
 		String mTitle;
 		public Spanned getTitle() { return Html.fromHtml(mTitle); }
 
 		@JsonProperty("num_comments")
 		private Integer mComments;
 
 		@JsonProperty("over_18")
 		private Boolean mOverEighteen;
 
 		@JsonProperty("thumbnail")
 		private String mThumbnailUrl;
 		public String getThumbnailUrl() { return mThumbnailUrl; }
 
 		@JsonProperty("downs")
 		private Integer mDownvotes;
 		public Integer getDownvotes() { return mDownvotes; }
 		
 		@JsonProperty("permalink")
 		private String mPermalinkSuffix;
 
 		@JsonProperty("name")
 		private String mName;
 
 		@JsonProperty("created_utc")
 		private Long mTimestamp;
 		public Long getTimestamp() { return (mTimestamp * 1000); }
 
 		@JsonProperty("url")
 		private String mLinkUrl;
 		public String getLinkUrl() { return mLinkUrl; }
 		
 		public String getImageUrl() {
 			if ( mLinkUrl.startsWith("http://imgur.com") ) {
 				return mLinkUrl + ".png";
 			} else {
 				return mLinkUrl;
 			}
 		}
 
 		@JsonProperty("author")
 		private String mAuthorName;
 		public String getAuthorName() { return mAuthorName; }
 
 		@JsonProperty("ups")
 		private Integer mUpvotes;
 		public Integer getUpvotes() { return mUpvotes; }
 	}
 }
