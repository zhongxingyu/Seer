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
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.SakaiProxy;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.State;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.sorter.DateComparator;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.sorter.IdCreatorComparator;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.sorter.TitleComparator;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.sorter.VisibilityComparator;
 import uk.ac.lancs.e_science.sakaiproject.api.blogger.searcher.QueryBean;
 import uk.ac.lancs.e_science.sakaiproject.impl.blogger.BloggerManager;
 
 
 import java.util.*;
 
 import com.sun.faces.util.Util;
 
 public class PostListViewerController extends BloggerController{
 
     private Blogger blogger;	
     private List postList;
     private List filteredPostList;
 
     //we will this values: 0 no sorter by this field. 1: descendant sort. 2: ascendant sort
     private int sortByDate;
     private int sortByTitle;
     private int sortByVisibility;
     private int sortByIdCreator;
     private int pagerNumItems = 10;
     private int pagerFirstItem;
     private int pagerTotalItems;
     private int currentVisibilityFilter;
 
     public PostListViewerController(){
     	blogger = BloggerManager.getBlogger();
     	
         restetSortState();
         resetFilters();
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
 
 
     public void loadAllPost(){
     	
     	Post[] posts = blogger.getPosts(SakaiProxy.getCurrentSiteId(),SakaiProxy.getCurretUserEid());
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
 
       ValueBinding binding =  Util.getValueBinding("#{postViewerController}");
       PostViewerController postViewerController = (PostViewerController)binding.getValue(FacesContext.getCurrentInstance());
       postViewerController.setPost(post);
 
       return "viewPost";
     }
     
     //this method is used when the action comes from a PostListing jsf component
    public String showPostFromListOfPostsJSFComponent(){
     	Post post = (Post) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("post");
         
     	ValueBinding binding =  Util.getValueBinding("#{postViewerController}");
     	PostViewerController postViewerController = (PostViewerController)binding.getValue(FacesContext.getCurrentInstance());
     	postViewerController.setPost(post);
 
       	return "viewPost";
     }
     public String doShowAll(){
         loadAllPost();
         resetFilters();
         updatePagerValues();
         restetSortState();
         return "viewPostList";
     }
     
     public Post[] getLastPosts(){
     	Post[] posts = blogger.getPosts(SakaiProxy.getCurrentSiteId(),SakaiProxy.getCurretUserEid());
     	if (posts==null)
     		return new Post[0];
     	if (posts!=null && posts.length>=20){
         	ArrayList postsAsList = new ArrayList(20);
         	for (int i=0;i<20;i++){
         		postsAsList.add(posts[i]);
         	}
     		return (Post[])postsAsList.toArray(new Post[20]);
     	}
     	return posts;
     }    
 
     public String doSearch(){
 
         QueryBean query = (QueryBean) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("query");
         Post[] result = blogger.searchPosts(query, SakaiProxy.getCurrentSiteId(), SakaiProxy.getCurretUserEid());
         if (result==null)
         	postList = new ArrayList(); //empty list
         else
         	postList = Arrays.asList(result);
         resetFilters();
         updatePagerValues();
         restetSortState();
         return "viewPostList";
     }
     public String doSortByTitle(){
         if (sortByTitle==0 || sortByTitle==2){
             Collections.sort(postList,new TitleComparator());
             sortByTitle=1;
         } else{
             Collections.reverse(postList);
             sortByTitle=2;
         }
         sortByDate=0;
         sortByIdCreator=0;
         sortByVisibility=0;
 
         return "viewPostList";
     }
     public String doSortByDate(){
         if (sortByDate==0 || sortByDate==2){
             Collections.sort(postList,new DateComparator());
             sortByDate=1;
         } else{
             Collections.reverse(postList);
             sortByDate=2;
         }
         sortByTitle=0;
         sortByIdCreator=0;
         sortByVisibility=0;
 
         return "viewPostList";
     }
     public String doSortByCreator(){
         if (sortByIdCreator==0 || sortByIdCreator==2){
             Collections.sort(postList,new IdCreatorComparator());
             sortByIdCreator=1;
         } else {
             Collections.reverse(postList);
             sortByIdCreator=2;
         }
         sortByDate=0;
         sortByTitle=0;
         sortByVisibility=0;
         return "viewPostList";
     }
     public String doSortByVisibility(){
         if (sortByVisibility==0 || sortByVisibility==2){
             Collections.sort(postList,new VisibilityComparator());
             sortByVisibility=1;
         } else {
             Collections.reverse(postList);
             sortByVisibility=2;
         }
         sortByDate=0;
         sortByTitle=0;
         sortByIdCreator=0;
         return "viewPostList";
     }
 
     private void restetSortState(){
         sortByDate=1;
         sortByTitle=0;
         sortByIdCreator=0;
         sortByVisibility=0;
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
     }
     public int getPagerNumItems(){
         return pagerNumItems;
     }
     public int getPagerTotalItems(){
         return pagerTotalItems;
     }
     private void updatePagerValues(){
         List listInUse;
         pagerNumItems = 10;
         if (filteredPostList==null)
             listInUse = postList;
         else{
         	filteredPostList = applyFilter(postList);
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
         //result.add(new SelectItem(new Integer(State.PUBLIC),"PUBLIC"));
         return result;
     }
     public void setFilterByVisibility(int filter){
         currentVisibilityFilter=filter;
     }
     public int getFilterByVisibility(){
         return currentVisibilityFilter;
     }
     private void resetFilters(){
         filteredPostList = null;
         currentVisibilityFilter=4;
     }
     public String doApplyFilters(){
         filteredPostList = applyFilter(postList);
         updatePagerValues();
         return "viewPostList";
     }
     private List applyFilter(List originalList){
         return applyVisibilityFilter(originalList);
     }
     private List applyVisibilityFilter(List originalList){
         List result = new ArrayList();
         if (currentVisibilityFilter==4)
             return originalList;
         Iterator it = originalList.iterator();
         while (it.hasNext()){
             Post post = (Post)it.next();
             if (post.getState().getVisibility()==currentVisibilityFilter)
                 result.add(post);
         }
         return result;
     }
 
 }
