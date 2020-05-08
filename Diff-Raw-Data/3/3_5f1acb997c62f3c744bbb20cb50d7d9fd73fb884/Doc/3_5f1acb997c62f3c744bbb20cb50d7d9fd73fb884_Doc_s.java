 package com.wehuibao.json;
 
 import java.util.List;
 
 import com.google.gson.annotations.SerializedName;
 
 public class Doc {
 	public String abbrev_text;
 	public String abbrev;
 	public String absolute_url;
 	public int cnt_follower;
 	@SerializedName("id")
 	public String docId;
 	public String major_title;
 	public int seqid;
 	public Thumbnail thumb;
 	public String title;
 	public String url;
 	public int vote_count;
 	public List<User> sharers;
 	
 	public String get_absolute_url() {
		return "http://wehuibao.com/api/doc" + this.docId;
 	}
 }
