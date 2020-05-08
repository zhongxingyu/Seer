 package edu.northwestern.bioinformatics.studycalendar.web.activity;
 
 import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
 import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Source;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.beans.factory.annotation.Required;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Map;
 import java.util.HashMap;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nshurupova
  * Date: Aug 5, 2008
  * Time: 11:00:35 AM
  * To change this template use File | Settings | File Templates.
  */
 public class AddSourceController extends PscAbstractController {
     private SourceDao sourceDao;
 
     public AddSourceController() {
     }
 
     @Override
     protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
         Map<String, Object> model = new HashMap<String, Object>();
         String sourceName = ServletRequestUtils.getRequiredStringParameter(request, "source");
         Source sourceInDB = sourceDao.getByName(sourceName);
         if (sourceInDB == null) {
             Source source = new Source();
             source.setName(sourceName);
             sourceDao.save(source);
             model.put("source", source);
        } 
         return new ModelAndView("template/ajax/activityTableUpdate", model);
      }
 
 
      //// CONFIGURATION
      @Required
      public void setSourceDao(SourceDao sourceDao) {
          this.sourceDao = sourceDao;
      }
 
  }
