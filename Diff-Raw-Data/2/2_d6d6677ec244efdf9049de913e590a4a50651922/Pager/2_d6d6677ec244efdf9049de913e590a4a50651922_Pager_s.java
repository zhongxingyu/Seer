 package com.eyougo.blog.comm;
 
 /**
  * 分页信息封装
  * 
  * @author MeiHongyuan
  * 
  */
 public class Pager {
 	int page = 1; // 页号
 
 	long totalNum = -1; // 记录总数
 
	int perPageNum = 6; // 每页显示记录数
 
 	private int navigatePageNum=6; //导航页码数
 	
 	public Pager() {
 	}
 
 	public int getPage() {
 		return page;
 	}
 
 	public void setPage(int page) {
 		this.page = page;
 	}
 
 	public long getTotalNum() {
 		return totalNum;
 	}
 
 	public void setTotalNum(long totalNum) {
 		this.totalNum = totalNum;
 	}
 
 	public int getPerPageNum() {
 		return perPageNum;
 	}
 
 	public void setPerPageNum(int perPageNum) {
 		this.perPageNum = perPageNum;
 	}
 
 	public int getAllPage() {
 		int allPage = 0;
 		if (totalNum != 0 && totalNum % perPageNum == 0) {
 			allPage = (int)(totalNum / perPageNum);
 		} else {
 			allPage = (int)(totalNum / perPageNum) + 1;
 		}
 		return allPage;
 	}
 
 	public int getOffset() {
         return (page - 1) * perPageNum;
     }
 	
     public Integer getPrePage() {
         return page == 1 ? null : page - 1;
     }
     
     public Integer getNextPage() {
     	int allPage = getAllPage();
         return page == allPage || allPage == 0 ? null : page+1;
     }
 
 	public int[] getNaviPages() {
 		int[] naviPages;  //所有导航页号
 		int allPage = getAllPage();
 		//当总页数小于或等于导航页码数时
         if(allPage<=navigatePageNum){
         	naviPages=new int[allPage];
             for(int i=0;i<allPage;i++){
             	naviPages[i]=i+1;
             }
         }else{ //当总页数大于导航页码数时
         	naviPages=new int[navigatePageNum];
             int startNum=page-navigatePageNum/2;
             int endNum=page+navigatePageNum/2;
             
             if(startNum<1){
                 startNum=1;
                 //最前navPageCount页
                 for(int i=0;i<navigatePageNum;i++){
                 	naviPages[i]=startNum++;
                 }
             }else if(endNum>allPage){
                 endNum=allPage;
                 //最后navPageCount页
                 for(int i=navigatePageNum-1;i>=0;i--){
                 	naviPages[i]=endNum--;
                 }
             }else{
                 //所有中间页
                 for(int i=0;i<navigatePageNum;i++){
                 	naviPages[i]=startNum++;
                 }
             }
         }
 		return naviPages;
 	}
 
 	public void setNavigatePageNum(int navigatePageNum) {
 		this.navigatePageNum = navigatePageNum;
 	}
 
 }
