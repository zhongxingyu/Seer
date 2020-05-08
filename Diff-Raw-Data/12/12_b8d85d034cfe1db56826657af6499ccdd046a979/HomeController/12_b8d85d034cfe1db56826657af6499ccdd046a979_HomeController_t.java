 package de.enwida.web.controller;
 
 import java.util.Locale;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {	
 	
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Locale locale) {
 	
 		return "index";
 	}
 	
   @RequestMapping(value = "/403", method = RequestMethod.GET)
    public String error403(Locale locale) {
    
        return "403";
    }
   
   @RequestMapping(value = "/404", method = RequestMethod.GET)
   public String error404(Locale locale) {
   
       return "404";
   }
	
    @RequestMapping(value = "/j_spring_security_check", method = RequestMethod.POST)
     public String login(Locale locale) {
     
         return "index";
     }
 	
 //	@RequestMapping(value = "/export", method = RequestMethod.GET)
 //	@ResponseBody		
 //	public ModelAndView chart_csv(HttpServletResponse response, Locale locale, HttpServletRequest request) {
 //	    response.setStatus(HttpServletResponse.SC_OK);	    
 //	    final String completeUrl=""+request.getRequestURL().append('?').append(request.getQueryString());
 //		Map pMap =request.getParameterMap();
 //		DataRequest dr = new DataRequest("csv",request.getLocale(),completeUrl,pMap);
 //		final Calendar calStart = Calendar.getInstance();
 //		calStart.set(Calendar.YEAR, 2010);
 //		calStart.set(Calendar.MONTH, Calendar.DECEMBER);
 //		calStart.set(Calendar.DAY_OF_MONTH, 30);
 //		calStart.set(Calendar.HOUR, 0);
 //		calStart.set(Calendar.MINUTE, 0);
 //		
 //		final Calendar calEnd = Calendar.getInstance();
 //		calEnd.set(Calendar.YEAR, 2010);
 //		calEnd.set(Calendar.MONTH, Calendar.DECEMBER);
 //		calEnd.set(Calendar.DAY_OF_MONTH, 31);	
 //		calStart.set(Calendar.HOUR, 23);
 //		calStart.set(Calendar.MINUTE, 59);
 //		dr.setChartType("rl_ab1");
 //		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
 //		dr.setTime1(calStart.getTimeInMillis());
 //		dr.setTime2(calEnd.getTimeInMillis());
 //		dr.setProduct(211);
 //		dataLineRequestManager.setDatef(dateFormat);
 //		dataLineRequestManager.setDatef2(dateFormat);
 //		response.setContentType("text/plain");
 //		response.setHeader("Content-Disposition","attachment;filename="+dr.getChartType()+dataLineRequestManager.getDatef2().format(dr.getTime1())+".csv");
 //			
 //		dr.setDataFormat("csv");
 //		Map<String, String> map=new HashMap<String, String>();
 //
 //		map.put("title", "de.enwida.chart.pc2.title");
 //		map.put("description", "de.enwida.chart.pc1.text1");
 //		map.put("type", "0");
 //		map.put("sign", "0");
 //		map.put("block", "0");
 //		map.put("from", "0");
 //		map.put("to", "0");
 //	//	map.put("content", dataRequestManager.csv_pc1(dr).toString());
 //		ModelAndView mav=new  ModelAndView("csv", map);
 //		return mav;	
 //
 //	}
 }
