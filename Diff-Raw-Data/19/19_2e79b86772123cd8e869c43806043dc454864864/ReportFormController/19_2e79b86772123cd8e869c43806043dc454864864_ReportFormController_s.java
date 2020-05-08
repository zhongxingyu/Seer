 package org.huamuzhen.oa.server.controller;
 
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 
 import org.huamuzhen.oa.biz.ReportFormManager;
 import org.huamuzhen.oa.biz.ReportFormTypeManager;
 import org.huamuzhen.oa.domain.entity.OrgUnit;
 import org.huamuzhen.oa.domain.entity.ReportFormType;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 @RequestMapping("/reportForm")
 public class ReportFormController {
 	
 	@Resource
 	private ReportFormManager reportFormManager;
 	
 	@Resource
 	private ReportFormTypeManager reportFormTypeManager;
 	
 	@RequestMapping("/addReportForm")
 	public ModelAndView addReportForm(){
 		ModelAndView mav = new ModelAndView("addReportForm");
 		List<ReportFormType> reportFormTypeList = reportFormTypeManager.findAll();
 		mav.addObject("reportFormTypeList", reportFormTypeList);
 		return mav;
 	}
 	
 	@RequestMapping("/add")
 	public ModelAndView add(HttpServletRequest request){
 		String reportFormTypeId = request.getParameter("reportFormTypeId");
 		ReportFormType reportFormType = reportFormTypeManager.findOne(reportFormTypeId);
 		Set<OrgUnit> requiredOrgUnits = reportFormType.getRequiredOrgUnits();
 		ModelAndView mav = new ModelAndView("checkRequiredOrgUnits");
 		mav.addObject("requiredOrgUnits", requiredOrgUnits);
 		for (OrgUnit orgUnit: requiredOrgUnits){
 System.out.println(orgUnit.getId());
 System.out.println(orgUnit.getName());
 System.out.println("##############");
 		}
 		
 		return mav;
 	}
 
 }
