 package com.diycomputerscience.slides.service;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.ejb.Stateless;
 
 import com.diycomputerscience.slides.Application;
 import com.diycomputerscience.slides.model.Category;
 import com.diycomputerscience.slides.model.SlideShow;
 import com.diycomputerscience.slides.util.ModelTOConverter;
 import com.diycomputerscience.slides.view.dto.CategoryTO;
 import com.diycomputerscience.slides.view.dto.SlideShowTO;
 import com.diycomputerscience.slides.view.dto.SlideTO;
 
 @Stateless(mappedName="SlideService", name="SlideService")
 public class SlideService {
 	
 	public Map<CategoryTO, List<SlideShowTO>> fetchSlideShowsByCategory() {
 		Application app = Application.getInstance();
 		Map<Category, List<SlideShow>> slideShowsByCat =  app.retreiveAllSlideShowsByCategory();
 		Map<CategoryTO, List<SlideShowTO>> slideShowsTOByCat = ModelTOConverter.convertSlideShowsByCategory(slideShowsByCat); 
 		return slideShowsTOByCat;
 	}
 
 	public SlideShow fetchSlideShow(long id) {
 		return null;
 	}
 
 	public SlideShowTO fetchSlideShowsByTitle(String title) {
 		Application app = Application.getInstance();
 		SlideShow slideShow = app.retreiveSlideShow(title);
		System.out.println("slideShow slide count " + slideShow.slides.size());
 		SlideShowTO slideShowTO = ModelTOConverter.convertSlideShow(slideShow);
		System.out.println("slideShowTO slide count " + slideShowTO.getSlides().size());
 		return slideShowTO;
 	}
 	
 	//TODO: This method must take the slideShw id
 	public SlideTO fetchSlide(String title, SlideShowTO slideShow) {
 		
 		if(slideShow == null) {
 			throw new NullPointerException("slideShow is null");
 		}
 		
 		if(title == null) {
 			return null;
 		}
 		
 		SlideTO retVal = null;
 		for(SlideTO slide : slideShow.getSlides()) {
 			if(slide.getTitle().equals(title)) {
 				retVal = slide;
 				break;
 			}
 		}
 		return retVal;
 	}
 
 }
