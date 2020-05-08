 package net.feiying.web.mvc.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.feiying.bean.VideoSearchBean;
 import com.feiying.impl.VideoSearch;
 import com.feiying.utity.MobileList;
 import com.feiying.utity.Pager;
 
 @Controller
 public class SearchMobile {
 	private static Log log = LogFactory.getLog(SearchMobile.class);
 	public static final int pageSize = 20;
 
 	@RequestMapping("/mobile/search")
 	public void search(
 			@RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
 			@RequestParam(value = "searchTitle", required = false, defaultValue = "") String searchTitle,
 			HttpServletResponse response) throws IOException{ 
 		String url = "/mobile/search?searchTitle=" + searchTitle;
 		VideoSearchBean videoSearchBean = null;
 		int count = 0;
 		if (searchTitle.equals("")) {
 			videoSearchBean = new VideoSearchBean();
 			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
 			videoSearchBean.setList(list);
 		} else {
 //			videoSearchBean = VideoSearch
 //					.getList(offset, pageSize, searchTitle);
 			
 			try {
 				videoSearchBean = VideoSearch.queryInSphinx(offset, pageSize, searchTitle);
 				Map<String, Integer> countMap = videoSearchBean.getCountMap();
 				count =  countMap.get("total_found");
 			} catch (Exception e) {
 				log.info(e.getMessage());
 				videoSearchBean = new VideoSearchBean();
 				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
 				videoSearchBean.setList(list);
 			} 
 			
 		}
		if (count > 0 && offset == 1) {
 			VideoSearch.countKeyword(searchTitle);
 		}
 		Pager pager = new Pager(offset, pageSize, count, url);
 		response.getWriter().print(
 				MobileList.getList(videoSearchBean.getList(), pager));
 	}
 	
 	@RequestMapping("/keywords")
 	public void getHotKeywords(HttpServletResponse response) throws IOException {
 		response.getWriter().print(
 				MobileList.getList(VideoSearch.getHotKeywords()));
 	}
 
 }
