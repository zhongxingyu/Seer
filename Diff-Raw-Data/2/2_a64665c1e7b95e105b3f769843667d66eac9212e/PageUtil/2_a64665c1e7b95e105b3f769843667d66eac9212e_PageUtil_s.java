 package com.bgg.storyfarm.common;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.stereotype.Component;
 
 @Component
 public class PageUtil {
 
 	//key name
 	public static final String ROWNUM_KEY = "rownum";
 	public static final String PER_KEY = "perpage"; 
 	
 	public static final int PAGE_NUM = 1; // 기본 페이지 번호
 	public static final int PAGE_SIZE = 5; // 한 화면에 보여질 **페이지** 갯수
 	public static final int PER_PAGE = 10; // 한 페이지에 보여질 **글** 갯수
 
 	public int getPerPage() {
 		return PER_PAGE;
 	}
 
 	public int getTotalPageCnt(int totalCnt) {
 		return ((totalCnt - 1) / PER_PAGE) + 1;
 	}
 
 	public int getFirstPageNum(int pageNum) {
 		// ( (페이지 번호 -1) / 총 페이지 사이즈 ) * 총 페이지 사이즈 + 1
 		return ((pageNum - 1) / PAGE_SIZE) * PAGE_SIZE + 1;
 	}
 
 	public int getLastPageNum(int fistPageNum) {
 		// 시작 페이지 번호 + 페이지 총 사이즈 -1
 		return (fistPageNum + PAGE_SIZE) - 1;
 	}
 
 	public int getStartRowNum(int pageNum, int perPage) {
 		return (pageNum - 1) * perPage;
 	}
 
 	public Map<String, Object> setPageLink(int totalCnt, int pageNum) {
 		Map<String, Object> pageLinkMap = new HashMap<String, Object>();
 		int firstPageNum = this.getFirstPageNum(pageNum);
 		int lastPageNum = this.getLastPageNum(firstPageNum);
 		int totalPage = this.getTotalPageCnt(totalCnt);
 		
 		lastPageNum = totalPage < lastPageNum ? totalPage : lastPageNum;
 
 		List<Map<String, String>> pageList = new ArrayList<Map<String, String>>();
 		for (int pageNumSeq = firstPageNum; pageNumSeq <= lastPageNum; pageNumSeq++) {
 			Map<String, String> pageMap = new HashMap<String, String>();
 			pageMap.put("pageNum", pageNumSeq + "");
 			pageList.add(pageMap);
 		}
 		
		pageLinkMap.put("lastPage", (int)Math.ceil(totalPage / firstPageNum));
 		pageLinkMap.put("totalCnt", totalCnt);
 		pageLinkMap.put("pageList", pageList);// 페이지 리스트
 		pageLinkMap.put("pageNum", pageNum);// 현재 페이지 번호
 		
 		// 1페이지가 아니라면
 		if (1 != firstPageNum) {
 			pageLinkMap.put("pagePrev", firstPageNum - 1);// 이전
 		}
 		// 마지막 페이지 리스트가 아니라면
 		if (totalPage != lastPageNum) {
 			pageLinkMap.put("pageNext", lastPageNum + 1);// 다음
 		}
 		return pageLinkMap;
 	}
 	
 }
