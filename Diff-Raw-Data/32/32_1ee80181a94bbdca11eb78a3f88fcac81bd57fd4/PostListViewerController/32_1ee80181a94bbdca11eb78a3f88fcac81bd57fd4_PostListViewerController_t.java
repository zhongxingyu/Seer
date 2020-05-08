 /*************************************************************************************
  Copyright (c) 2006. Centre for e-Science. Lancaster University. United Kingdom.
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 
  *************************************************************************************/
 
 package uk.ac.lancs.e_science.sakai.tools.blogger;
 
 
 import javax.faces.context.FacesContext;
 import javax.faces.el.ValueBinding;
 import javax.faces.model.SelectItem;
 import javax.servlet.ServletRequest;
 
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.Blogger;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.Member;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.SakaiProxy;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.State;
 
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.searcher.QueryBean;
 import uk.ac.lancs.e_science.sakaiproject.impl.blogger.BloggerManager;
 
 
 import java.util.*;
 
 import com.sun.faces.util.Util;
 
 public class PostListViewerController extends BloggerController {
 
     private Blogger blogger;	
     private List postList;
     private List filteredPostList;
 
 
     private int pagerNumItems = 10;
     private int pagerFirstItem;
     private int pagerTotalItems;
     private int currentVisibilityFilter;
     
     private boolean showComments;
     private boolean showFullContent;
     
     private String lastView = "main";
     
     
     private Member selectedMember;
     
     /*  these variables are used as part of a trick
      * because of we want to process the page mainAccess when the checkbox for showcomment and showFullContent we had put 'this.form.submit' in the javascript event onchange.
      * when submit is executed, because is part of javascript and not jsf, the behavior is not the one we spect. The system will call the only action present in the page (ShowPostFromListOfPostsJSFComponent) but we don't want to
      * do anything with that action. With these flag we can know when this action has been invoked correctly or as part of the this.form.submit process.
     */
     private boolean showCommentsHasChanged=false; 
     private boolean showFullContentHasChanged=false; 
     
     private boolean firstLoad = true;
 
     public PostListViewerController(){
     	blogger = BloggerManager.getBlogger();
 
     }
     public Collection getPostList(){
         List listInUse = null;
         if (filteredPostList==null)
             listInUse = postList;
         else if (filteredPostList!=null)
             listInUse = filteredPostList;
 
         if (listInUse == null){
             loadAllPost();
             listInUse = postList;
         }
         if (listInUse.size()==0)
             return listInUse;
         
         if (pagerNumItems==0)//show all
         	return listInUse;
         
         int pagerLastItem=pagerFirstItem+pagerNumItems;
         if (pagerLastItem>listInUse.size())
             pagerLastItem=listInUse.size();
 
         return listInUse.subList(pagerFirstItem,pagerLastItem);
     }
 
 
     public void reloadPosts(){
     	if (lastView.equals("main"))
     		loadAllPost();
     	else
     		loadAllPostsOfTheSelectedMember();
     }
     
     private void loadAllPost(){
     	
     	Post[] posts = blogger.getPosts(SakaiProxy.getCurrentSiteId(),SakaiProxy.getCurrentUserId());
     	if (posts!=null)
     		postList = Arrays.asList(posts);
     	else
     		postList = new ArrayList();
     	
         updatePagerValues();
     }
     
     private void loadAllPostsOfTheSelectedMember(){
     	
   		String userEid = selectedMember.getUserEid();
    		QueryBean query = new QueryBean();
   		query.setUser(SakaiProxy.getUserId(userEid));
    		
    		Post[] posts = blogger.searchPosts(query, SakaiProxy.getCurrentSiteId(), SakaiProxy.getCurrentUserId());
     	if (posts!=null)
     		postList = Arrays.asList(posts);
     	else
     		postList = new ArrayList();
     	
         updatePagerValues();  	
 
     } 
 
     //this method is used when the action comes from a standar jsf component, like a table
     public String doShowPost(){
       ServletRequest request = (ServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
       Post post = (Post) request.getAttribute("post");
 
       // The lookup key needs to be specified in faces-config ie: postViewerController.
       ValueBinding binding =  Util.getValueBinding("#{postViewerController}");
       PostViewerController postViewerController = (PostViewerController)binding.getValue(FacesContext.getCurrentInstance());
       postViewerController.setPost(post);
 
       return "viewPost";
     }
     
     public String doShowPostOfMember(){
         ServletRequest request = (ServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
         selectedMember = (Member) request.getAttribute("member");
         lastView = "userBlog";
         reloadPosts();
         return "userBlog";
     }     
     public String doShowMyBlogger(){
         selectedMember = new Member();
        selectedMember.setUserEid(SakaiProxy.getEid(SakaiProxy.getCurrentUserId()));
         selectedMember.setUserDisplayId(SakaiProxy.getDiplayNameForTheUser(SakaiProxy.getCurrentUserId()));
         lastView = "userBlog";
         reloadPosts();
         return "userBlog";
     }
     
     //this method is used when the action comes from a PostListing jsf component
     public String showPostFromListOfPostsJSFComponent(){
     	Post post = (Post) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("post");
     	if (showCommentsHasChanged || showFullContentHasChanged)
     		return "";
        
     	ValueBinding binding =  Util.getValueBinding("#{postViewerController}");
     	PostViewerController postViewerController = (PostViewerController)binding.getValue(FacesContext.getCurrentInstance());
     	postViewerController.setPost(post);
 
       	return "viewPost";
     }
     public String doShowAll(){
         loadAllPost();
         updatePagerValues();
         lastView = "main";
         return "main";
     }
 
      
     public String doSearch(){
 
         QueryBean query = (QueryBean) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("query");
         Post[] result = blogger.searchPosts(query, SakaiProxy.getCurrentSiteId(), SakaiProxy.getCurrentUserId());
         if (result==null)
         	postList = new ArrayList(); //empty list
         else
         	postList = Arrays.asList(result);
         updatePagerValues();
         lastView = "main";
         return "main";
     }
     
     public String doSearchInMemberBlog(){
 
         QueryBean query = (QueryBean) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("query");
         query.setUser(selectedMember.getUserEid());
         Post[] result = blogger.searchPosts(query, SakaiProxy.getCurrentSiteId(), SakaiProxy.getCurrentUserId());
         if (result==null)
         	postList = new ArrayList(); //empty list
         else
         	postList = Arrays.asList(result);
         updatePagerValues();
         lastView = "userBlog";
         return "userBlog";
     }
  
 
     //----- METHODS USED BY PAGER ----------------------------
 
     public void setPagerFirstItem(int firstItem){
         pagerFirstItem = firstItem;
     }
     public int getPagerFirstItem(){
 
         return pagerFirstItem;
     }
     public void setPagerNumItems(int num){
         pagerNumItems = num;
         reloadPosts();
     }
     public int getPagerNumItems(){
         return pagerNumItems;
     }
     public int getPagerTotalItems(){
     	if (firstLoad) //this is when the page is loaded. The blogger didn't have the chance of loading the posts. This is because the pager is render before the list of posts
     		loadAllPost();
     	firstLoad=false;
         return pagerTotalItems;
     }
     private void updatePagerValues(){
         List listInUse;
         if (filteredPostList==null)
             listInUse = postList;
         else{
             listInUse = filteredPostList;
         }
 
 
         pagerTotalItems = listInUse.size();
 
         pagerFirstItem=0;
 
         int pagerLastItem=pagerFirstItem+pagerNumItems;
         if (pagerLastItem>listInUse.size())
             pagerLastItem=listInUse.size();
 
     }
 
 
 
     //----- END METHODS USED BY PAGER ------------------------
     public List getVisibilityList(){
 
         ArrayList result = new ArrayList();
         result.add(new SelectItem(new Integer(4),"ALL"));
         result.add(new SelectItem(new Integer(State.PRIVATE),"PRIVATE"));
         result.add(new SelectItem(new Integer(State.SITE),"SITE"));
         result.add(new SelectItem(new Integer(State.TUTOR),"TUTOR"));
         //result.add(new SelectItem(new Integer(State.PUBLIC),"PUBLIC"));
         return result;
     }
      public void setShowComments(boolean s){
     	if (s!=showComments)
     		showCommentsHasChanged=true; //we know that the checkbox has changed. See comment about variable declaration
     	else 
     		showCommentsHasChanged=false;
     	showComments=s;
     }
     public boolean getShowComments(){
     	return showComments;
     }
     public void setShowFullContent(boolean s){
     	if (s!=showFullContent)
     		showFullContentHasChanged=true; //we know that the checkbox has changed. See comment about variable declaration
     	else
     		showFullContentHasChanged=false;
     	showFullContent = s;
     	 
     }
     public String getSelectedMemberId(){
     	if (selectedMember!=null)
     		return selectedMember.getUserDisplayId();
     	return SakaiProxy.getDiplayNameForTheUser(SakaiProxy.getCurrentUserId());
     }    
     public boolean getShowFullContent(){
     	return showFullContent;
     }
     public String getLastView(){
     	return lastView;
     }
 
 
 }
