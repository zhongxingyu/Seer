 package fr.xebia.confluence2wordpress.wp;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.xmlrpc.XmlRpcClient;
 import org.apache.xmlrpc.XmlRpcException;
 
 import fr.xebia.confluence2wordpress.util.collections.CollectionUtils;
 import fr.xebia.confluence2wordpress.wp.transport.DefaultProxyAwareXmlRpcTransportFactory;
 
 /**
  * 
  * @see "http://codex.wordpress.org/XML-RPC_wp"
  * 
  * @author Alexandre Dutra
  *
  */
 public class WordpressClient {
 
     private static final String CREATE_POST_METHOD_NAME = "metaWeblog.newPost";
 
     private static final String UPDATE_POST_METHOD_NAME = "metaWeblog.editPost";
 
     private static final String FIND_POST_BY_ID_METHOD_NAME = "metaWeblog.getPost"; //"blogger.getPost";
 
     private static final String GET_USERS_METHOD_NAME = "c2w.getAuthors"; //"wp.getAuthors";
 
     private static final String GET_CATEGORIES_METHOD_NAME = "wp.getCategories";
 
     private static final String GET_TAGS_METHOD_NAME = "wp.getTags";
 
     private static final String UPLOAD_FILE_METHOD_NAME = "c2w.uploadFile"; //"wp.uploadFile";
     
     private static final String FIND_PAGE_ID_BY_SLUG_METHOD_NAME = "c2w.findPageIdBySlug";
     
 
 
     /*
         Sample of XML-RPC invocation with curl:
         curl -d "<methodCall> \
            <methodName>wp.getAuthors</methodName> \
               <params> \
                  <param> \
                     <value><int>1</int></value> \
                     <value><string>admin</string></value> \
                     <value><string>admin</string></value> \
                  </param> \
               </params> \
         </methodCall>" http://localhost/~alexandre/wordpress/xmlrpc.php
      */
     
     /**
      * Very, VERY old version of the lib bundled with Confluence.
      * Does not even know about Lists and Maps,
      * only Vectors and Hashtables.
      */
     private XmlRpcClient client;
 
     private WordpressConnection wordpressConnection;
 
     public WordpressClient(WordpressConnection wordpressConnection) {
         this.wordpressConnection = wordpressConnection;
     }
 
 
     
     /**
      * @see "http://codex.wordpress.org/XML-RPC_wp#wp.getAuthors"
      * @return the list of {@link WordpressUser}s of the blog.
      * @throws WordpressXmlRpcException
      */
     public List<WordpressUser> getUsers() throws WordpressXmlRpcException {
 
         Vector<Object> params = new Vector<Object>();
 
         params.add(wordpressConnection.getBlogId());
         params.add(wordpressConnection.getUsername());
         params.add(wordpressConnection.getPassword());
 
         List<Map<String, Object>> rows = invoke(GET_USERS_METHOD_NAME, params);
 
         List<WordpressUser> users = new ArrayList<WordpressUser>(rows.size());
         for (Map<String, Object> row : rows) {
             WordpressUser user = new WordpressUser();
             user.setId(Integer.valueOf(row.get("user_id").toString()));
             user.setLogin(row.get("user_login").toString());
             user.setDisplayName((String)row.get("display_name"));
             user.setFirstName((String)row.get("first_name"));
             user.setLastName((String)row.get("last_name"));
             user.setNiceName((String) row.get("user_nicename"));
             if(row.get("user_level") != null && StringUtils.isNotEmpty(row.get("user_level").toString())) {
                 user.setLevel(Integer.valueOf(row.get("user_level").toString()));
             }
             users.add(user);
         }
 
         return users;
     }
 
 
 
     /**
      * @see "http://codex.wordpress.org/XML-RPC_wp#wp.getCategories"
      * @return the list of {@link WordpressCategory}s of the blog.
      * @throws WordpressXmlRpcException
      */
     public List<WordpressCategory> getCategories() throws WordpressXmlRpcException {
 
         Vector<Object> params = new Vector<Object>();
 
         params.add(wordpressConnection.getBlogId());
         params.add(wordpressConnection.getUsername());
         params.add(wordpressConnection.getPassword());
 
         List<Map<String, Object>> rows = invoke(GET_CATEGORIES_METHOD_NAME, params);
 
         List<WordpressCategory> categories = new ArrayList<WordpressCategory>(rows.size());
         for (Map<String, Object> row : rows) {
             WordpressCategory category = new WordpressCategory();
             category.setId(Integer.valueOf(row.get("categoryId").toString()));
             category.setParentId(Integer.valueOf(row.get("parentId").toString()));
             category.setDescription((String) row.get("description"));
             category.setCategoryName((String) row.get("categoryName"));
             category.setHtmlUrl((String) row.get("htmlUrl"));
             category.setRssUrl((String) row.get("rssUrl"));
             categories.add(category);
         }
 
         return categories;
     }
 
 
     /**
      * @see "http://codex.wordpress.org/XML-RPC_wp#wp.getTags"
      * @return the list of {@link WordpressCategory}s of the blog.
      * @throws WordpressXmlRpcException
      */
     public List<WordpressTag> getTags() throws WordpressXmlRpcException {
 
         Vector<Object> params = new Vector<Object>();
 
         params.add(wordpressConnection.getBlogId());
         params.add(wordpressConnection.getUsername());
         params.add(wordpressConnection.getPassword());
 
         List<Map<String, Object>> rows = invoke(GET_TAGS_METHOD_NAME, params);
 
         List<WordpressTag> tags = new ArrayList<WordpressTag>(rows.size());
         for (Map<String, Object> row : rows) {
             WordpressTag tag = new WordpressTag();
             tag.setId(Integer.valueOf(row.get("tag_id").toString()));
             tag.setName((String) row.get("name"));
             tag.setCount(Integer.valueOf(row.get("count").toString()));
             tag.setSlug((String) row.get("slug"));
             tag.setHtmlUrl((String) row.get("html_url"));
             tag.setRssUrl((String) row.get("rss_url"));
             tags.add(tag);
         }
 
         return tags;
     }
 
     /**
      * http://xmlrpc.free-conversant.com/docs/bloggerAPI#getPost
      * http://joysofprogramming.com/blogger-getpost/
      * http://stackoverflow.com/questions/3083039/how-can-i-get-a-post-with-xml-rpc-in-wordpress
      * 
      * @param postId
      * @return post
      * @throws WordpressXmlRpcException
      */
     public WordpressPost findPostById(int postId) throws WordpressXmlRpcException {
         Vector<Object> params = new Vector<Object>();
         //params.add(wordpressConnection.getBlogId());
         params.add(postId);
         params.add(wordpressConnection.getUsername());
         params.add(wordpressConnection.getPassword());
         Map<String, Object> map = invoke(FIND_POST_BY_ID_METHOD_NAME, params);
 
         /*Sample of the response structure:
          * 
          * userid=3,
          * mt_allow_pings=1,
          * postid=40,
          * wp_author_id=3,
          * date_created_gmt=Wed May 04 14:32:57 CEST 2011,
          * wp_password=,
          * link=http://localhost/wordpress/2011/05/04/revue-de-presse-xebia-9/,
          * mt_keywords=tag1, tag2,
          * dateCreated=Wed May 04 16:32:57 CEST 2011,
          * categories=[test],
          * post_status=publish,
          * mt_allow_comments=1,
          * wp_slug=revue-de-presse-xebia-9,
          * permaLink=http://localhost/wordpress/2011/05/04/revue-de-presse-xebia-9/,
          * description=coucou c'est un draft,
          * custom_fields=[],
          * mt_text_more=,
          * mt_excerpt=,
          * sticky=false,
          * title=Revue de Presse Xebia,
          * wp_author_display_name=xebia-france
          */
 
         WordpressPost post = new WordpressPost();
         post.setPostId(postId);
         post.setDraft(false);
 
         Object authorId = map.get("wp_author_id");
         post.setAuthorId(authorId == null ? null : Integer.valueOf(authorId.toString()));
 
         Date dateCreated = (Date) map.get("dateCreated");
         post.setDateCreated(dateCreated);
 
        String body = (String) map.get("description");
        post.setBody(body);
 
         String title = (String) map.get("title");
         post.setTitle(title);
 
         post.setDraft( ! "publish".equals(map.get("post_status")));
 
         @SuppressWarnings("unchecked")
         List<String> categoryNames = (List<String>) map.get("categories");
         post.setCategoryNames(categoryNames);
 
         List<String> tagNames = CollectionUtils.split((String) map.get("mt_keywords"), ",");
         post.setTagNames(tagNames);
 
         String slug = (String) map.get("wp_slug");
         post.setPostSlug(slug);
 
         String permaLink = (String) map.get("permaLink");
         post.setLink(permaLink);
 
         return post;
     }
 
     /**
      * 
      * @param postSlug
      * @return postId
      * @throws WordpressXmlRpcException
      */
     public Integer findPageIdBySlug(String postSlug) throws WordpressXmlRpcException {
         Vector<Object> params = new Vector<Object>();
         params.add(wordpressConnection.getBlogId());
         params.add(wordpressConnection.getUsername());
         params.add(wordpressConnection.getPassword());
         params.add(postSlug);
         Integer result = (Integer) invoke(FIND_PAGE_ID_BY_SLUG_METHOD_NAME, params);
         if(result != null && result == 0){
             return null;
         }
         return result;
     }
 
     /**
      * 
      * http://www.xmlrpc.com/metaWeblogApi
      * http://mindsharestrategy.com/wp-xmlrpc-metaweblog/
      * http://www.perkiset.org/forum/perl/metaweblognewpost_to_wordpress_blog_xmlrpcphp-t1307.0.html
      * http://joysofprogramming.com/wordpress-xmlrpc-metaweblog-newpost/
      * http://life.mysiteonline.org/archives/161-Automatic-Post-Creation-with-Wordpress,-PHP,-and-XML-RPC.html
      * 
      * @param post to create
      * @return the created post
      * @throws WordpressXmlRpcException
      */
     public WordpressPost post(WordpressPost post) throws WordpressXmlRpcException {
 
         Vector<Object> params = new Vector<Object>();
 
         if(post.getPostId() == null) {
             params.add(wordpressConnection.getBlogId());
         } else {
             params.add(post.getPostId());
         }
         params.add(wordpressConnection.getUsername());
         params.add(wordpressConnection.getPassword());
 
         Hashtable<String,Object> map = new Hashtable<String,Object>();
         map.put("title", post.getTitle());
         map.put("description", post.getBody());
         map.put("wp_author_id", post.getAuthorId());
         map.put("wp_slug", post.getPostSlug());
         if (post.getCategoryNames() != null) {
             map.put("categories", new Vector<String>(post.getCategoryNames()));
         } else {
             map.put("categories", new Vector<String>());
         }
         if (post.getTagNames() != null) {
             map.put("mt_keywords", new Vector<String>(post.getTagNames()));
         } else {
             map.put("mt_keywords", new Vector<String>());
         }
         params.add(map);
 
         //to publish ?
         params.add( ! post.isDraft());
 
         if(post.getPostId() == null) {
             Object ret = invoke(CREATE_POST_METHOD_NAME, params);
             int postId = Integer.parseInt(ret.toString());
             post.setPostId(postId);
         } else {
             Boolean ret = invoke(UPDATE_POST_METHOD_NAME, params);
             if( ! ret) {
                 throw new WordpressXmlRpcException("Post edit failed");
             }
         }
 
         return findPostById(post.getPostId());
     }
 
     /**
      * @param file
      * @return
      * @throws WordpressXmlRpcException
      */
     public WordpressFile uploadFile(WordpressFile file) throws WordpressXmlRpcException {
 
         Vector<Object> params = new Vector<Object>();
         params.add(wordpressConnection.getBlogId());
         params.add(wordpressConnection.getUsername());
         params.add(wordpressConnection.getPassword());
 
         Hashtable<String,Object> map = new Hashtable<String,Object>();
         map.put("name", file.getFileName());
         map.put("type", file.getMimeType());
         map.put("bits", file.getData());
         //see http://core.trac.wordpress.org/ticket/17604
         map.put("overwrite", true);
         params.add(map);
 
         Map<String, String> response = invoke(UPLOAD_FILE_METHOD_NAME, params);
 
         file.setFileName(response.get("file"));
         file.setMimeType(response.get("type"));
         file.setUrl(response.get("url"));
 
         return file;
 
     }
 
     @SuppressWarnings("unchecked")
     private <T> T invoke(String methodName, Vector<Object> params) throws WordpressXmlRpcException {
         try {
             return (T) getClientInstance().execute(methodName, params);
         } catch (XmlRpcException e) {
             throw new WordpressXmlRpcException("Error invoking method: " + methodName, e);
         } catch (IOException e) {
             throw new WordpressXmlRpcException("Error invoking method: " + methodName, e);
         }
     }
 
     private XmlRpcClient getClientInstance() throws MalformedURLException {
         if(this.client == null){
             URL wordpressUrl = new URL(wordpressConnection.getUrl());
             if(this.wordpressConnection.getProxyHost() != null && this.wordpressConnection.getProxyPort() != null){
                 this.client = new XmlRpcClient(wordpressUrl,
                     new DefaultProxyAwareXmlRpcTransportFactory(
                         wordpressUrl, 
                         wordpressConnection.getProxyHost(), 
                         wordpressConnection.getProxyPort()));
             } else {
                 this.client = new XmlRpcClient(wordpressUrl);
             }
         }
         return this.client;
     }
 }
