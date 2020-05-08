 package com.example.xpsearchliu.dao;
 
 import java.util.Map;
 
 import com.example.xpsearchliu.util.DataReader;
 import com.google.inject.Singleton;
 
 @Singleton
 public class PostDao {
 
	public void init(){
 		StringBuffer sql = new StringBuffer();
		sql.append("insert into xpsearchyao_schema.").append("post")
 		.append(" (id,acceptedanswerid,answercount,body,commentcount,communityowneddate,")
 		.append("creationdate,favoritecount,lasteditoruserid,owneruserid,posttypeid,score,tag,title,viewcount)")
 		.append(" values ");
 		for(Map m:DataReader.readXML("Posts.xml")){
 			sql.append("(").append(m.get("id")).append(",")
 				.append(m.get("acceptedanswerid")).append(",")
 				.append(m.get("answercount")).append(",")
 				.append(getString(m.get("body"))).append(",")
 				.append(m.get("commentcount")).append(",")
 				.append(getDateString(m.get("communityowneddate"))).append(",")
 				.append(getDateString(m.get("creationdate"))).append(",")
 				.append(m.get("favoritecount")).append(",")
 				.append(m.get("lasteditoruserid")).append(",")
 				.append(m.get("owneruserid")).append(",")
 				.append(m.get("posttypeid")).append(",")
 				.append(m.get("score")).append(",")
 				.append(getString(m.get("tag"))).append(",")
 				.append(getString(m.get("title"))).append(",")
 				.append(m.get("viewcount")).append("),");
			break;
 		}
 	
 	}
 	
 	private String getDateString(Object src){
 		if(src==null){
 			return null;
 		}else {
 			return "'"+(String)src+"'";
 			}
 	}
 	
 	private String getString(Object src){
 		if(src==null){
 			return "''";
 		}else {
 			return "'"+(String)src+"'";
 			}
 	}
 }
