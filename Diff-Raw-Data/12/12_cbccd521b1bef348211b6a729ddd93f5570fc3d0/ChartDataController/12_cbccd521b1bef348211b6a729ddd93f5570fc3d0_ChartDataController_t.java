 package de.enwida.web.controller;
 
 import java.security.Principal;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import de.enwida.chart.LineManager;
 import de.enwida.transport.Aspect;
 import de.enwida.transport.DataResolution;
 import de.enwida.transport.IDataLine;
 import de.enwida.transport.LineRequest;
 import de.enwida.web.dao.interfaces.INavigationDao;
 import de.enwida.web.model.ChartLinesRequest;
 import de.enwida.web.model.ChartNavigationData;
 import de.enwida.web.service.implementation.LineService;
 import de.enwida.web.service.interfaces.INavigationService;
 import de.enwida.web.utils.CalendarRange;
 import de.enwida.web.utils.NavigationDefaults;
 
 /**
  * Handles chart data requests
  */
 @Controller
 @RequestMapping("/data")
 public class ChartDataController {
 	
 	@Autowired
 	private LineService lineService;
 	
 	@Autowired
 	private INavigationService navigationService;
 	
 	@RequestMapping(value="/lines", method = RequestMethod.GET)
 	@ResponseBody
 	public List<IDataLine> getLines (
 								@RequestParam int chartId,
 								@RequestParam int product,
 								@RequestParam int tso,
								@RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Calendar startTime,
								@RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Calendar endTime,
 								@RequestParam DataResolution resolution,
 								Locale locale
 							   )
 	{
 	    final ChartLinesRequest request = new ChartLinesRequest(
 	            chartId,
 	            product,
 	            tso,
 	            new CalendarRange(startTime, startTime),
 	            resolution,
 	            locale
 	            );
 	    
 	    return lineService.getLines(request);
 	}
 	
 	@RequestMapping(value="/chart", method=RequestMethod.GET)
 	public String exampleChart(Principal principal) {
 		if(principal!=null){
 			System.out.println(principal.getName());
 		}
 		return "charts/index";
 	}
 	
 	@RequestMapping(value = "/navigation", method = RequestMethod.GET)
 	@ResponseBody
 	public ChartNavigationData getNavigationData(@RequestParam int chartId, Principal principal, Locale locale) {
 	    // FIXME: get user / submit correct role
 	    return navigationService.getNavigationData(chartId, 0, locale);
 	}
 	
 	/*
 	 * =======================================================================================
 	 * CAUTION:
 	 * ! Never ever leave the methods below in production code !
 	 * 
 	 * These get navigation data and lines bypassing the security layer in order to test frontend
 	 * JavaScript code
 	 * =======================================================================================
 	 */
 	
 	@Autowired
 	private LineManager lineManager;
 	
 	@Autowired
 	private INavigationDao navigationDao;
 	
 	@RequestMapping(value = "/navigation.test", method = RequestMethod.GET)
 	@ResponseBody
 	public ChartNavigationData getNavigationDataTest(@RequestParam int chartId, Principal principal, Locale locale) throws ParseException {
 	    final ChartNavigationData result = navigationDao.getDefaultNavigation(chartId, locale);
 	    final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 	    final Date from = dateFormat.parse("2010-12-29");
 	    final Date to = dateFormat.parse("2010-12-31");
 	    final Calendar cFrom = Calendar.getInstance();
 	    final Calendar cTo = Calendar.getInstance();
 	    cFrom.setTime(from);
 	    cTo.setTime(to);
 	    result.setDefaults(new NavigationDefaults(99, DataResolution.HOURLY, 211, new CalendarRange(cFrom, cTo)));
 	    result.setIsDateScale(true);
 	    result.addTso(99, "Standard");
 	    result.addTso(1, "Test");
 	    return result;
 	}
 	
 	@RequestMapping(value="/lines.test", method = RequestMethod.GET)
 	@ResponseBody
 	public List<IDataLine> getLinesTest (
 								@RequestParam int chartId,
 								@RequestParam int product,
 								@RequestParam int tso,
								@RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Calendar startTime,
								@RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Calendar endTime,
 								@RequestParam DataResolution resolution,
 								Locale locale
 							   )
 	{
 	    final List<IDataLine> result = new ArrayList<IDataLine>();
 
	    System.out.println(startTime.get(Calendar.YEAR) + "-" + startTime.get(Calendar.MONTH) + "-" + startTime.get(Calendar.DATE));

 	    for (final Aspect aspect : Arrays.asList(new Aspect[] { Aspect.CR_DEGREE_OF_ACTIVATION })) {
 	        final LineRequest req = new LineRequest(aspect, product, tso, startTime, endTime, resolution, locale);
 	        try {
                 result.add(lineManager.getLine(req));
             } catch (Exception e) {
                 e.printStackTrace();
             }
 	    }
 	    return result;
 	}
 	
 }
