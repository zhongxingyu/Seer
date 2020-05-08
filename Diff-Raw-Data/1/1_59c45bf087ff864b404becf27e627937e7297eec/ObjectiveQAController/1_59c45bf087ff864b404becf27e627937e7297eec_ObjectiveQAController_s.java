 package com.sd.web.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.sd.web.dto.ObjectiveQADTO;
 import com.sd.web.dto.TechnologyDTO;
 import com.sd.web.exception.DatabaseException;
 import com.sd.web.form.LogonForm;
 import com.sd.web.form.ObjectiveQAForm;
 import com.sd.web.security.Ticket;
 import com.sd.web.services.ObjectiveQAService;
 import com.sd.web.services.SolutionDeskService;
 import com.sd.web.util.FormDTOFiller;
 import com.sd.web.util.JsonResponse;
 import com.sd.web.util.Parameters;
 
 @Controller
 public class ObjectiveQAController extends BaseController {
 	private final ObjectiveQAService objectiveQAService = getObjectiveQAService();
 
 	@RequestMapping(value = "/deleteObjectiveQAData.do", method = RequestMethod.GET)
 	public ModelAndView delete(HttpServletRequest request) throws ServletException {
 		try {
 			Ticket ticket = getTicket(request);
 			if (ticket != null) {
 				String parameter[] = { request.getParameter("Id") };
 				objectiveQAService.delete(ticket, new ObjectiveQADTO(), parameter);
 				getDataList(request, request.getParameter("entityId"));
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return new ModelAndView("objectiveQADisplay");
 	}
 
 	@RequestMapping(value = "objectiveQAForm.do", method = RequestMethod.GET)
 	public String formBackingObject(Model model, HttpServletRequest request) throws ServletException {
 		ObjectiveQAForm objectiveQAForm = new ObjectiveQAForm();
 		System.out.println("-------formBackingObject");
 		objectiveQAForm.setTechnologyId(request.getParameter("entityId"));
 		model.addAttribute("objectiveQAForm", objectiveQAForm);
 		return "objectiveQAForm";
 	}
 
 	private void getDataList(HttpServletRequest request, String entityId) {
 
 		request.setAttribute("entityId", entityId);
 		String spec = "technologyId=" + entityId;
 		List<ObjectiveQADTO> objectiveQAList = null;
 		try {
 			objectiveQAList = objectiveQAService.findBySpec(getTicket(request), new ObjectiveQADTO(), spec);
 		} catch (DatabaseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		request.setAttribute("ObjectiveQAList", objectiveQAList);
 
 	}
 
 	private ObjectiveQAService getObjectiveQAService() {
 		ObjectiveQAService objectiveQAService = (ObjectiveQAService) SolutionDeskService.getService("com.sd.web.services.ObjectiveQAServiceImpl");
 		return objectiveQAService;
 	}
 
 	@RequestMapping(value = "/getDetaDetailList.do", method = RequestMethod.POST)
 	public @ResponseBody
 	JsonResponse getTechDetailList(Parameters param, BindingResult result) {
 		JsonResponse res = new JsonResponse();
 		List<String> techList = new ArrayList<String>();
 		System.out.println("TechName " + param.getTechName());
 		System.out.println("ChapterName " + param.getChapterName());
 
 		List<TechnologyDTO> findAll = new ArrayList<TechnologyDTO>();
 		try {/*
 				 * String spec = null; if ((null != param.getTechName()) &&
 				 * (null != param.getChapterName())) { spec = "techName = '" +
 				 * param.getTechName() + "' AND chapterName = '" +
 				 * param.getChapterName() + "'"; findAll =
 				 * technologyService.findBySpec(ticket, new TechnologyDTO(),
 				 * spec); } else if ((null != param.getTechName()) && (null ==
 				 * param.getChapterName())) { spec = "techName = '" +
 				 * param.getTechName() + "'"; findAll =
 				 * technologyService.findBySpec(ticket, new TechnologyDTO(),
 				 * spec); } else {
 				 * 
 				 * findAll = technologyService.findAll(ticket, new
 				 * TechnologyDTO()); }
 				 */
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		for (TechnologyDTO techDTO : findAll) {
 			if (null != techDTO.getTechName()) {
 				System.out.println("techDTO.getTechName()...: " + techDTO.getTechName());
 				if ((null != param.getTechName()) && (null != param.getChapterName())) {
 					if (techDTO.getChapterName() != null) {
 						techList.add(techDTO.getChapterName());
 					}
 				} else if ((null != param.getTechName()) && (null == param.getChapterName())) {
 					if (techDTO.getTopicName() != null) {
 						techList.add(techDTO.getTopicName());
 					}
 
 				} else {
 					techList.add(techDTO.getTechName());
 				}
 			}
 		}
 		res.setStatus("SUCCESS");
 		res.setResult(techList);
 
 		return res;
 	}
 	
 	@RequestMapping(value = "objectiveQAList.do", method = RequestMethod.GET)
 	public String objectiveQAList(Model model, HttpServletRequest request) throws ServletException {
 		System.out.println("test this ques");
 		getDataList(request, request.getParameter("entityId"));
 		return "objectiveQADisplay";
 	}
 
 	@RequestMapping(value = "/saveObjectiveQAData.do", method = RequestMethod.POST)
 	public ModelAndView save(Model model, ObjectiveQAForm objectiveQAForm, BindingResult result, HttpServletRequest request) throws ServletException {
 		try {
 			Ticket ticket = getTicket(request);
 			if (ticket != null) {
 				ObjectiveQADTO objectiveQADTO = (ObjectiveQADTO) FormDTOFiller.getDTO(objectiveQAForm, new ObjectiveQADTO());
 				objectiveQADTO.setTechnologyId(Long.parseLong(objectiveQAForm.getTechnologyId()));
 				objectiveQAService.save(ticket, objectiveQADTO);
 
 				getDataList(request, objectiveQAForm.getTechnologyId());
 			} else {
 				model.addAttribute("LogonForm", new LogonForm());
 				return new ModelAndView("logonForm");
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return new ModelAndView("objectiveQADisplay");
 	}
 
 	@RequestMapping(value = "/editObjectiveQAData.do", method = RequestMethod.POST)
 	public ModelAndView update(Model model, ObjectiveQAForm objectiveQAForm, BindingResult result, HttpServletRequest request) throws ServletException {
 		try {
 			Ticket ticket = getTicket(request);
 			if (ticket != null) {
 				ObjectiveQADTO objectiveQADTO = (ObjectiveQADTO) FormDTOFiller.getDTO(objectiveQAForm, new ObjectiveQADTO());
 				objectiveQADTO.setId(Long.parseLong(objectiveQAForm.getId()));
 				objectiveQAService.update(ticket, objectiveQADTO);
 
 				getDataList(request, objectiveQAForm.getTechnologyId().toString());
 			} else {
 				model.addAttribute("LogonForm", new LogonForm());
 				return new ModelAndView("logonForm");
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return new ModelAndView("objectiveQADisplay");
 	}
 
 	@RequestMapping(value = "/editModeObjectiveQAData.do", method = RequestMethod.GET)
 	public ModelAndView updateMode(Model model, HttpServletRequest request) throws ServletException {
 		try {
 			Ticket ticket = getTicket(request);
 			if (ticket != null) {
 				ObjectiveQADTO objectiveQADTO = objectiveQAService.findById(ticket, new ObjectiveQADTO(), request.getParameter("Id"));
 				ObjectiveQAForm objectiveQAForm = (ObjectiveQAForm) FormDTOFiller.getForm(objectiveQADTO);
 				objectiveQAForm.setTechnologyId(objectiveQADTO.getTechnologyId().toString());
 				objectiveQAForm.setId(request.getParameter("Id"));
 				request.setAttribute("mode", "edit");
 				model.addAttribute("objectiveQAForm", objectiveQAForm);
 
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return new ModelAndView("objectiveQAForm");
 	}
 
 }
