 /**
  * 
  */
 package com.diycomputerscience.slides.service;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.ejb.embeddable.EJBContainer;
 
 import com.diycomputerscience.slides.model.Category;
 import com.diycomputerscience.slides.model.SlideShow;
 import com.diycomputerscience.slides.view.dto.CategoryTO;
 import com.diycomputerscience.slides.view.dto.SlideShowTO;
 
 import junit.framework.TestCase;
 
 /**
  * @author pshah
  *
  */
 public class SlideServiceTest extends TestCase {
 
 	EJBContainer ejbContainer;
 	private SlideService slideService;
 	
 	/**
 	 * @param name
 	 */
 	public SlideServiceTest(String name) {
 		super(name);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		final Properties p = new Properties();
         p.put("myds", "new://Resource?type=DataSource");
         p.put("myds.JdbcDriver", "org.hsqldb.jdbcDriver");
         p.put("myds.JdbcUrl", "jdbc:hsqldb:mem:slidedb");
         
		this.ejbContainer = EJBContainer.createEJBContainer(p);
 		Object oSlideService = ejbContainer.getContext().lookup("java:global/slides/SlideService");
 		assertNotNull(oSlideService);
 		this.slideService = (SlideService)oSlideService;
 		this.slideService.initDb();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		if(ejbContainer != null) {
 			ejbContainer.close();
 		}
 	}
 	
 	public void testFetchSlideShowsBycategory() {
 		Map<CategoryTO, List<SlideShowTO>> slideShowsByCategory = this.slideService.fetchSlideShowsByCategory();
 		Set<CategoryTO> categories = slideShowsByCategory.keySet();
 		assertNotNull(slideShowsByCategory);
 		//assertEquals(2, categories);
 	}
 
 }
