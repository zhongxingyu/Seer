 package com.googlecode.icontents.web.controller.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.googlecode.icontents.bean.Article;
 import com.googlecode.icontents.bean.component.ArticleComponent;
 import com.googlecode.icontents.bean.component.FlashComponent;
 import com.googlecode.icontents.service.ArticleService;
 
 @Controller
 public class ImageRotatorDataController {
     
    private Log logger = LogFactory.getLog(ImageRotatorDataController.class);
     
 	@Resource
 	private ArticleService articleService;
 	
 	@RequestMapping(value="data/imageRotatorData.do")
 	public ModelAndView data(HttpServletRequest request, ModelMap model)
 			throws Exception {
 	    long articleId = ServletRequestUtils.getLongParameter(request, "articleId", 1l);
	    int sequence = ServletRequestUtils.getIntParameter(request, "sequence", 0);
	    int position = ServletRequestUtils.getIntParameter(request, "position", 0);
 	    
 		Article article = articleService.getObjectById(articleId);
 		List<ArticleComponent> articleList = article.getBodyComponentList();
 		
 		FlashComponent flashComponent = null;
 		for(ArticleComponent comp:articleList){
 		    if(FlashComponent.class.getSimpleName().equals(comp.getComponentType())){
 		        flashComponent = new FlashComponent(comp);
 		        break;
 		    }
 		}
 		
		logger.error("++++++++++" + flashComponent);
		
 		if(flashComponent==null){
 		    return null;
 		}
 		String images = flashComponent.getImages();
 		String links = flashComponent.getContent();
 		
 		List<Track> trackList = new ArrayList<Track>();
 		
 		if(StringUtils.isNotBlank(images)&&StringUtils.isNotBlank(links)){
 			String[] imageArray = images.split(",");
 			String[] linkArray = links.split(",");
 			
 			int length = Math.min(imageArray.length, linkArray.length);
 			
 			for(int i=0;i<length;i++){
 				trackList.add(new Track(imageArray[i],linkArray[i]));
 			}
 		}
 		model.put("trackList",trackList);
 		model.put("flashComponent",flashComponent);
 		return new ModelAndView("data/imageRotatorData",model);
 	}
 
 	public static class Track {
 		private String image;
 		private String link;
 		
 		public Track(String image,String link){
 			this.image = image;
 			this.link = link;
 		}
 		public String getImage() {
 			return image;
 		}
 		public void setImage(String image) {
 			this.image = image;
 		}
 		public String getLink() {
 			return link;
 		}
 		public void setLink(String link) {
 			this.link = link;
 		}
 	};
 }
