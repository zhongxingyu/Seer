 package com.zhiweiwang.datong.controller;
 
 import com.zhiweiwang.datong.DTContants;
 import com.zhiweiwang.datong.DTMessage;
 import com.zhiweiwang.datong.DTUtils;
 import com.zhiweiwang.datong.mapper.StudentMapper;
 import com.zhiweiwang.datong.model.User;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 @Controller
 @SessionAttributes(DTContants.USER_IN_SESSION)
 public class FillController {
 	private Logger logger = LoggerFactory.getLogger(this.getClass());
 
 	@Autowired
 	protected StudentMapper studentMapper;
 	
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@RequestMapping(value = "/fillin", method = RequestMethod.POST)
 	public ModelAndView fillin(HttpServletRequest request, @ModelAttribute(DTContants.USER_IN_SESSION) User user) {
 		
 		logger.info("user is:  {} ",user.getId());
 		
 		Map map = new HashMap(request.getParameterMap());
 		Set<String> keys = map.keySet();
         for (Iterator it = keys.iterator(); it.hasNext();) {
             String key = (String) it.next();
             Object student = request.getParameter(key);           
             map.put(key, student.toString());
             logger.info("params:  {} - {}",new Object[]{key, student});
         }
         
       	DTUtils.makeMap2Json(map, HONOR_NAMES, "honors");
       	DTUtils.makeMap2Json(map, PRICE_NAMES, "prices");
         map.put(DTContants.DT_ID, user.getId());
         map.put(DTContants.DT_USERNAME, user.getUsername());
         
         Object imgpath = request.getSession().getAttribute(DTContants.IMG_PATH);
        
        // TODO
        // bad coding  
        // using user id to sure if the img belong to one
        // 
        if(imgpath != null && imgpath.toString().indexOf(""+user.getId())>0){
         	map.put(DTContants.IMG_PATH, imgpath);
         }
         logger.info(map.toString());
         
         ModelAndView mav = new ModelAndView();
         
         Map<?, ?> student = studentMapper.getStudent(user.getId());
         if( student == null || student.size()<1){
         	studentMapper.insertRow(map);
         	mav.addObject(DTContants.MSG_ERRER, DTMessage.FILLIN_SUCCESS);
         }else{
         	studentMapper.update(map);
         	mav.addObject(DTContants.MSG_ERRER, DTMessage.FILLIN_UPATE_SUCCESS);
         }
 		
 		mav.setViewName("redirect:/message");
 		return mav;
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@ModelAttribute(DTContants.DT_STUDENT)
 	@RequestMapping(value = "/fillin", method = GET)
 	public Map<?,?> get(@ModelAttribute(DTContants.USER_IN_SESSION) User user) {
 		logger.info("user:  {} ",user.getId());
 		Map model = new HashMap();
 		Map<?, ?> result = studentMapper.getStudent(user.getId());
 		if(result != null)
 			model.putAll(result);
         
         DTUtils.makeJson2Map(model, "honors");
         DTUtils.makeJson2Map(model, "prices");
         return model;
 	}
 	
 	private static final String[] PRICE_NAMES = {"pname","plev","pcell","ptime"};
 	private static final String[] HONOR_NAMES = {"hname","hlev"};
 }
