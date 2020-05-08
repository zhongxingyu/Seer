 /**
  * 
  */
 package com.diycomputerscience.slides.util;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import com.diycomputerscience.slides.model.Category;
 import com.diycomputerscience.slides.model.Slide;
 import com.diycomputerscience.slides.model.SlideShow;
 import com.diycomputerscience.slides.view.dto.CategoryTO;
 import com.diycomputerscience.slides.view.dto.SlideShowTO;
 import com.diycomputerscience.slides.view.dto.SlideTO;
 
 /**
  * @author pshah
  *
  */
 public class ModelTOConverter {
 
 	public static Map<CategoryTO, List<SlideShowTO>> convertSlideShowsByCategory(Map<Category,List<SlideShow>> slideShowsByCategory) {
		//SlideShowPrintUtils.printSlideShowByCategory(slideShowsByCategory);
 		Map<CategoryTO, List<SlideShowTO>> retVal = new TreeMap<CategoryTO, List<SlideShowTO>>();
 		Set<Category> categories = slideShowsByCategory.keySet();
 		for(Category category : categories) {
 			CategoryTO categoryTo = new CategoryTO(category);
 			List<SlideShow> slideShows = slideShowsByCategory.get(category);
 			//Using TreeSet to get sorting for free
 			Set<SlideShowTO> slideShowTOs = new TreeSet<SlideShowTO>();
 			for(SlideShow slideShow : slideShows) {
 				SlideShowTO slideShowTO = new SlideShowTO(slideShow);
 				slideShowTOs.add(slideShowTO);
 			}
 			retVal.put(categoryTo, new ArrayList<SlideShowTO>(slideShowTOs));
 		}
		//SlideShowPrintUtils.printSlideShowTOByCategory(retVal);
 		return retVal;
 	}
 	
 	public static SlideShowTO convertSlideShow(SlideShow slideShow) {
 		return new SlideShowTO(slideShow);
 	}
 	
 	public static SlideTO convertSlide(Slide slide) {
 		return new SlideTO(slide);
 	}
 }
