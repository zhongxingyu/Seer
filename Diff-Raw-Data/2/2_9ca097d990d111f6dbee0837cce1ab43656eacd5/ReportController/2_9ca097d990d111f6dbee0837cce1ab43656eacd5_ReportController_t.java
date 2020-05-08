 /**
  *  Copyright 2011 Society for Health Information Systems Programmes, India (HISP India)
  *
  *  This file is part of SDMXDataExport module.
  *
  *  SDMXDataExport module is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
 
  *  SDMXDataExport module is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with SDMXDataExport module.  If not, see <http://www.gnu.org/licenses/>.
  *
  **/
 
 package org.openmrs.module.sdmxhddataexport.web.controller.report;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.sdmxhddataexport.SDMXHDDataExportService;
 import org.openmrs.module.sdmxhddataexport.model.Report;
 import org.openmrs.module.sdmxhddataexport.util.PagingUtil;
 import org.openmrs.module.sdmxhddataexport.util.RequestUtil;
 import org.openmrs.web.WebConstants;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.util.CollectionUtils;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.support.SessionStatus;
 
 
 @Controller("SDMXHDDataExportReportController")
 public class ReportController {
 	Log log = LogFactory.getLog(this.getClass());
 	 @RequestMapping(value="/module/sdmxhddataexport/report.form", method=RequestMethod.GET)
 		public String view(@RequestParam(value="reportId",required=false) Integer  reportId,@ModelAttribute("report") Report report, Model model){
 			SDMXHDDataExportService sDMXHDDataExportService =Context.getService(SDMXHDDataExportService.class);
 			if(reportId != null){
 				report = sDMXHDDataExportService.getReportById(reportId);
 				model.addAttribute("report",report);
 			}
 			return "/module/sdmxhddataexport/report/form";
 		}
 	 
 	 @RequestMapping(value="/module/sdmxhddataexport/report.form", method=RequestMethod.POST)
 		public String post(@ModelAttribute("report") Report report,BindingResult bindingResult, SessionStatus status, Model model){
 			new ReportValidator().validate(report, bindingResult);
 			if (bindingResult.hasErrors()) {
 				return "/module/sdmxhddataexport/report/form";
 			}else{
 				SDMXHDDataExportService sDMXHDDataExportService =Context.getService(SDMXHDDataExportService.class);
 				report.setCreatedOn(new java.util.Date());
				if(report.getId() != null){
 					report.setReportDataElements(sDMXHDDataExportService.getReportById(report.getId()).getReportDataElements());
 				}
 				report.setCreatedBy(Context.getAuthenticatedUser().getGivenName());
 				sDMXHDDataExportService.saveReport(report);
 				status.setComplete();
 				return "redirect:/module/sdmxhddataexport/listReport.form";
 			}
 		}
 	 
 	 @RequestMapping(value="/module/sdmxhddataexport/listReport.form", method=RequestMethod.GET)
 		public String list( 
 				@RequestParam(value="pageSize",required=false)  Integer pageSize, 
 	            @RequestParam(value="currentPage",required=false)  Integer currentPage,
 	            @RequestParam(value="searchName",required=false)  String name,
 	           
 	            Map<String, Object> model, HttpServletRequest request){
 		 	SDMXHDDataExportService sDMXHDDataExportService =Context.getService(SDMXHDDataExportService.class);
 			
 			int total = sDMXHDDataExportService.countListReport(name);
 			PagingUtil pagingUtil = new PagingUtil( RequestUtil.getCurrentLink(request) , pageSize, currentPage, total );
 			List<Report> reports =sDMXHDDataExportService.listReport (name, pagingUtil.getStartPos(), pagingUtil.getPageSize());
 
 			//process excel here
 			
 
 			model.put("reports", reports );
 			model.put("pagingUtil", pagingUtil);
 			model.put("searchName", name);
 			
 			
 			
 			return "/module/sdmxhddataexport/report/list";
 
 	 }
 	 
 	 	@RequestMapping(value="/module/sdmxhddataexport/listReport.form",  method=RequestMethod.POST)
 	    public String deleteReport(@RequestParam("ids") String[] ids,HttpServletRequest request){
 			String temp = "";
 	    	HttpSession httpSession = request.getSession();
 			Integer reportId  = null;
 			try{
 				SDMXHDDataExportService sDMXHDDataExportService =Context.getService(SDMXHDDataExportService.class);
 				if( ids != null && ids.length > 0 ){
 					for(String sId : ids )
 					{
 						reportId = Integer.parseInt(sId);
 						if( reportId!= null && reportId > 0 && CollectionUtils.isEmpty(sDMXHDDataExportService.listReportDataElement(reportId, null, null, 0, 1)))
 						{
 							sDMXHDDataExportService.deleteReport(sDMXHDDataExportService.getReportById(reportId));
 						}else{
 							//temp += "We can't delete store="+store.getName()+" because that store is using please check <br/>";
 							temp = "This report cannot be deleted as it is in use";
 						}
 					}
 				}
 			}catch (Exception e) {
 				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR,
 				"Can not delete report ");
 				log.error(e);
 			}
 			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, StringUtils.isBlank(temp) ?  "sdmxhddataexport.report.deleted" : temp);
 	    	
 	    	return "redirect:/module/sdmxhddataexport/listReport.form";
 	    }
 }
