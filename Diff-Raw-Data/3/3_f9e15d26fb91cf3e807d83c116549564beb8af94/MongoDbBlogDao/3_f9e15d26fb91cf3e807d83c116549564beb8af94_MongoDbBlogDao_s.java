 /**
   * Copyright (c) <2011>, <NetEase Corporation>
   * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *    3. Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.netease.webbench.blogbench.kv.mongodb;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.BasicDBObjectBuilder;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.WriteResult;
 import com.netease.webbench.blogbench.dao.BlogDAO;
 import com.netease.webbench.blogbench.model.Blog;
 import com.netease.webbench.blogbench.model.BlogIdWithTitle;
 import com.netease.webbench.blogbench.model.BlogInfoWithPub;
 import com.netease.webbench.blogbench.model.SiblingPair;
 import com.netease.webbench.common.DynamicArray;
 
 public class MongoDbBlogDao implements BlogDAO {
 	public static final String ID_FIELD = "ID";
 	public static final String UID_FIELD = "UserID";
 	public static final String TITLE_FIELD = "Title";
 	public static final String ABS_FIELD = "Abstract";
 	public static final String ALLOWVIEW_FIELD = "AllowView";
 	public static final String PTIME_FIELD = "PublishTime";
 	public static final String ACCESS_FIELD = "AccessCount";
 	public static final String COMMENT_FIELD = "CommentCount";
 	public static final String CONTENT_FIELD = "Content";
 	
 	private Mongo mongo;
 	private DB db;
 	private DBCollection collection;
 	
 	public MongoDbBlogDao(String host, int port) throws UnknownHostException {
 		this.mongo = new Mongo(host, port);
 		this.db = mongo.getDB("test");
 		this.collection = db.getCollection("Blog");
 	}
 	
 	@Override
 	public void close() {
 		//this.mongo.close();
 		this.mongo = null;
 	}
 	
 	private Blog obj2Blog(DBObject obj) {
 		Blog blog = new Blog();		
 		blog.setId(Long.parseLong((String)obj.get(ID_FIELD)));
 		blog.setUid(Long.parseLong((String)obj.get(UID_FIELD)));
 		blog.setTitle((String)obj.get(TITLE_FIELD));
 		blog.setAbs((String)obj.get(ABS_FIELD));
 		blog.setAllowView(Integer.parseInt((String)obj.get(ALLOWVIEW_FIELD)));
 		blog.setPublishTime(Long.parseLong((String)obj.get(PTIME_FIELD)));
 		blog.setAccessCount(Integer.parseInt((String)obj.get(ACCESS_FIELD)));
 		blog.setCommentCount(Integer.parseInt((String)obj.get(COMMENT_FIELD)));
 		blog.setCnt((String)obj.get(CONTENT_FIELD));
 		return blog;
 	}
 	
 	private DBObject blog2Obj(Blog blog) {
 		BasicDBObject document = new BasicDBObject();
 		document.append(ID_FIELD, blog.getId())
 			.append(UID_FIELD, blog.getUid())
 			.append(TITLE_FIELD, blog.getTitle())
 			.append(ABS_FIELD, blog.getAbs())
 			.append(ALLOWVIEW_FIELD, blog.getAllowView())
 			.append(PTIME_FIELD, blog.getPublishTime())
 			.append(ACCESS_FIELD, blog.getAccessCount())
 			.append(COMMENT_FIELD, blog.getCommentCount())
 			.append(CONTENT_FIELD, blog.getCnt());
 		return document;
 	}
 
 	@Override
 	public Blog selectBlog(long blogId, long uId) throws Exception {
 		BasicDBObject searchQuery = new BasicDBObject();
 		searchQuery.put(ID_FIELD, blogId);
 		
 		DBObject obj = collection.findOne(searchQuery);
 		return obj != null ? obj2Blog(obj) : null;
 	}
 
 	@Override
 	public List<Long> selBlogList(long uId) throws Exception {
 		BasicDBObject searchQuery = new BasicDBObject();
 		searchQuery.put(UID_FIELD, uId);
 		DBCursor cursor = collection.find(searchQuery).sort(
 				new BasicDBObject(PTIME_FIELD, 1)).limit(10);		
 		try {
 			List<Long> list = new ArrayList<Long>();
 			while (cursor.hasNext()) {
 				Long id = Long.parseLong((String)cursor.next().get(ID_FIELD));
 				list.add(id);
 			}
			cursor.close();
 			return list;
 		} finally {
 			cursor.close();
 		}
 	}
 	
 	private BlogIdWithTitle getSibling(long uId, long time, boolean pre) {
 		BasicDBObject searchQuery = new BasicDBObject();
 		searchQuery.put(UID_FIELD, uId);
 		
 		searchQuery.put(PTIME_FIELD, BasicDBObjectBuilder.start(pre ? "gt" : "lt", 
 				time).get());	
 		DBCursor cursor = collection.find(searchQuery).sort(
 				new BasicDBObject(PTIME_FIELD, 1)).limit(1);
 		try {
 			if (cursor.hasNext()) {
 				DBObject o = cursor.next();
 				long id = Long.parseLong((String)o.get(ID_FIELD));
 				String title = (String)o.get(TITLE_FIELD);
 				return new BlogIdWithTitle(id, uId, title);
 			}		
 			return null;
 		} finally {
 			cursor.close();
 		}
 	}
 
 	@Override
 	public SiblingPair selSiblings(long uId, long time) throws Exception {
 		return new SiblingPair(getSibling(uId, time, true), 
 				getSibling(uId, time, false));
 	}
 
 	@Override
 	public DynamicArray<BlogInfoWithPub> selAllBlogIds() throws Exception {
 		DynamicArray<BlogInfoWithPub> arr = new DynamicArray<BlogInfoWithPub>(
 				selBlogNums());
 		BasicDBObject searchQuery = new BasicDBObject();
 		searchQuery.put(ID_FIELD, "*");
 		DBCursor cursor = collection.find(searchQuery); 
 		try {
 			while (cursor.hasNext()) {
 				BlogInfoWithPub b = new BlogInfoWithPub();
 				DBObject o = cursor.next();
 				b.setBlogId(Long.parseLong((String)o.get(ID_FIELD)));
 				b.setUId(Long.parseLong((String)o.get(UID_FIELD)));
 				b.setPublishTime(Long.parseLong((String)o.get(PTIME_FIELD)));
 				arr.append(b);
 			}
			cursor.close();
 			return arr;
 		} finally {
 			cursor.close();
 		}
 	}
 
 	@Override
 	public long selBlogNums() throws Exception {
 		return collection.count();
 	}
 
 	@Override
 	public int insertBlog(Blog blog) throws Exception {
 		WriteResult r = collection.insert(blog2Obj(blog));
 		return r.getN();
 	}
 
 	@Override
 	public int batchInsert(List<Blog> blogList) throws Exception {
 		List<DBObject> list = new ArrayList<DBObject>(blogList.size());
 		for (Blog b : blogList)
 			list.add(blog2Obj(b));
 		WriteResult r = collection.insert(list);
 		return r.getN();
 	}
 
 	@Override
 	public int updateAccess(long blogId, long uId) throws Exception {
 		BasicDBObject searchQuery = new BasicDBObject();
 		searchQuery.put(ID_FIELD, blogId);
 		
 		DBObject change = new BasicDBObject(ACCESS_FIELD, 1);
 		DBObject update = new BasicDBObject("$inc", change);
 		
 		WriteResult r = collection.update(searchQuery, update);
 		return r.getN();
 	}
 
 	@Override
 	public int updateComment(long blogId, long uId) throws Exception {
 		BasicDBObject searchQuery = new BasicDBObject();
 		searchQuery.put(ID_FIELD, blogId);
 		
 		DBObject change = new BasicDBObject(COMMENT_FIELD, 1);
 		DBObject update = new BasicDBObject("$inc", change);
 		
 		WriteResult r = collection.update(searchQuery, update);
 		return r.getN();
 	}
 
 	@Override
 	public int updateBlog(Blog blog) throws Exception {
 		BasicDBObject searchQuery = new BasicDBObject();
 		searchQuery.put(ID_FIELD, blog.getId());
 		
 		DBObject change = new BasicDBObject();
 		change.put(CONTENT_FIELD, blog.getCnt());
 		change.put(PTIME_FIELD, blog.getPublishTime());
 		DBObject update = new BasicDBObject("$set", change);
 		
 		WriteResult r = collection.update(searchQuery, update);
 		return r.getN();
 	}
 }
