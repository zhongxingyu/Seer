 /*
  * Copyright © 2012, Source Tree, All Rights Reserved
  * 
  * QuestionController.java
  * Modification History
  * *************************************************************
  * Date				Author						Comment
  * Nov 7, 2012	    Chalam Pavuluri				Created
  * *************************************************************
  */
 package org.sourcetree.interview.controller;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.sourcetree.interview.dto.CategoryDTO;
 import org.sourcetree.interview.dto.ListProp;
 import org.sourcetree.interview.dto.QuestionDTO;
 import org.sourcetree.interview.dto.QuestionExcelFileUploadDTO;
 import org.sourcetree.interview.dto.QuestionListDTO;
 import org.sourcetree.interview.dto.ResponseDTO;
 import org.sourcetree.interview.enums.OutcomeStatus;
 import org.sourcetree.interview.enums.UserRoleEnum;
 import org.sourcetree.interview.service.CategoryService;
 import org.sourcetree.interview.service.QuestionService;
 import org.sourcetree.interview.support.CoreUtil;
 import org.sourcetree.interview.support.WebUtil;
 import org.sourcetree.interview.support.annotation.Restricted;
 import org.sourcetree.interview.support.validation.ValidationUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.propertyeditors.CustomCollectionEditor;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Question Controller
  * 
  * @author Chalam Pavuluri
  */
 @Controller
 @RequestMapping("/question")
 public class QuestionController extends BaseController
 {
 	private static final Logger LOG = Logger
 			.getLogger(QuestionController.class);
 
 	@Autowired
 	private QuestionService questionService;
 
 	@Autowired
 	private CategoryService categoryService;
 
 	@RequestMapping(value = "/addNew", method = RequestMethod.GET)
 	@Restricted(rolesAllowed = { UserRoleEnum.ADMIN },
 			setSessionAttributes = false)
 	String questionForm(Model model)
 	{
 		model.addAttribute("categories", categoryService.findAllCategories());
 		return "question/questionForm";
 	}
 
 	/**
 	 * to handle new question request. this method will be invoked in the event
 	 * of form submissions from the client which will contains the
 	 * <b>application/x-www-form-urlencoded</b> as the content-type header
 	 * 
 	 * @param questionDTO
 	 *            question DTO object
 	 * @return <code>{@linkplain userDTO}</code> - response will be Serialised
 	 *         to either XML/JSON/text base on the request headers
 	 */
 	@RequestMapping(value = "/addNew", method = RequestMethod.POST)
 	@Restricted(rolesAllowed = { UserRoleEnum.ADMIN },
 			setSessionAttributes = false)
 	@ResponseBody
 	public ResponseDTO processQuestionAddNew(
 			@ModelAttribute QuestionDTO questionDTO)
 	{
 		ResponseDTO response = new ResponseDTO();
 
 		Map<String, String> errors = ValidationUtil.validate(questionDTO,
 				validator, messageSource);
 
 		if (!errors.isEmpty())
 		{
 			response.setErrors(errors);
 			response.setStatus(OutcomeStatus.FAILURE);
 		}
 		else
 		{
 			questionService.create(questionDTO);
 		}
 
 		return response;
 	}
 
 	/**
 	 * to navigate to question edit form.
 	 * 
 	 * @param questionId
 	 * @param model
 	 * @return question edit page
 	 */
 	@RequestMapping(value = "/edit/{questionId}", method = RequestMethod.GET)
 	@Restricted(rolesAllowed = { UserRoleEnum.ADMIN },
 			setSessionAttributes = false)
 	public String questionEditForm(@PathVariable Long questionId, Model model)
 	{
 		model.addAttribute("question",
 				questionService.getQuestionDTOById(questionId));
 		model.addAttribute("categories", categoryService.findAllCategories());
 
 		return "question/questionEdit";
 	}
 
 	/**
 	 * to handle question edit form request.
 	 * 
 	 * @param questionDTO
 	 *            Question DTO object
 	 * @return <code>{@linkplain QuestionDTO}</code> - response will be
 	 *         Serialised to either XML/JSON/text base on the request headers
 	 */
 	@RequestMapping(value = "/edit", method = RequestMethod.POST)
 	@Restricted(rolesAllowed = { UserRoleEnum.ADMIN },
 			setSessionAttributes = false)
 	@ResponseBody
 	public ResponseDTO updateQuestion(@ModelAttribute QuestionDTO questionDTO)
 	{
 		ResponseDTO response = new ResponseDTO();
 		Map<String, String> errors = ValidationUtil.validate(questionDTO,
 				validator, messageSource);
 
 		if (!errors.isEmpty())
 		{
 			response.setErrors(errors);
 			response.setStatus(OutcomeStatus.FAILURE);
 		}
 		else
 		{
 			questionService.update(questionDTO);
 		}
 
 		return response;
 	}
 
 	/**
 	 * To view the question details.
 	 * 
 	 * @param questionId
 	 * @param model
 	 * @return question details page
 	 */
 	@RequestMapping(value = "/view/{questionId}", method = RequestMethod.GET)
 	public String questionDetails(@PathVariable Long questionId, Model model)
 	{
 		model.addAttribute("question",
 				questionService.getQuestionDTOById(questionId));
 
 		return "question/questionDetails";
 	}
 
 	/**
 	 * 
 	 * @param model
 	 * @return
 	 */
 	@RequestMapping(value = "/manageQuestions", method = RequestMethod.GET)
 	@Restricted(rolesAllowed = { UserRoleEnum.ADMIN },
 			setSessionAttributes = false)
 	public String searchQuestionsByCategoryName(Model model)
 	{
 		model.addAttribute("categories", categoryService.findAllCategories());
 
 		return "question/manageQuestionsByCategoryName";
 	}
 
 	/**
 	 * To list and manage the questions based on category name.
 	 * 
 	 * @param categoryName
 	 * @param model
 	 * @return list all the questions
 	 */
 	@RequestMapping(value = "/manageQuestions/{categoryName}",
 			method = RequestMethod.GET)
 	@Restricted(rolesAllowed = { UserRoleEnum.ADMIN },
 			setSessionAttributes = false)
 	public String manageQuestionsByCategoryName(
 			@PathVariable String categoryName, Model model)
 	{
 		model.addAttribute("categories", categoryService.findAllCategories());
 
 		// Initialize ListProp with first page
 		ListProp listProp = WebUtil.initListProp("1", getDefaultPageSize(),
 				null, null);
 
		List<QuestionDTO> questionDTOs = null;
		CategoryDTO categoryDTO = null;
		categoryDTO = categoryService.getCategoryDTOByName(categoryName);

		questionDTOs = questionService.getQuestionSearchResult(null,
				categoryName, listProp);

		model.addAttribute("categoryDTO", categoryDTO);
		model.addAttribute("questionsList", new QuestionListDTO(questionDTOs,
				listProp));
 
 		return "question/manageQuestionsByCategoryName";
 	}
 
 	/**
 	 * 
 	 * @param model
 	 * @return
 	 */
 	@RequestMapping(value = "/export", method = RequestMethod.GET)
 	@Restricted(rolesAllowed = { UserRoleEnum.ADMIN },
 			setSessionAttributes = false)
 	public String exportForm(Model model)
 	{
 		model.addAttribute("categories", categoryService.findAllCategories());
 
 		return "question/exportQuestions";
 	}
 
 	/**
 	 * To export the questions to excel.
 	 * 
 	 * @param categoryName
 	 * @param model
 	 * @return excel with all the questions
 	 */
 	@RequestMapping(value = "/exportToExcel/{categoryName}",
 			method = RequestMethod.GET)
 	@Restricted(rolesAllowed = { UserRoleEnum.ADMIN },
 			setSessionAttributes = false)
 	public ModelAndView exportQuestionsToExcel(
 			@PathVariable String categoryName, Model model)
 	{
 		return new ModelAndView("qestionsExcel", "questions",
 				questionService.getQuestionsByCategoryName(categoryName, null));
 	}
 
 	@RequestMapping(value = "/uploadExcel", method = RequestMethod.GET)
 	@Restricted(rolesAllowed = { UserRoleEnum.ADMIN },
 			setSessionAttributes = false)
 	String excelUploadForm(Model model)
 	{
 		return "question/uploadQuestionExcelForm";
 	}
 
 	/**
 	 * Upload question excel file
 	 * 
 	 * @param questionExcelFileUploadDTO
 	 * @return
 	 * @throws IOException
 	 *             in case of any file input stream read errors.
 	 */
 	@RequestMapping(value = "/uploadExcel", method = RequestMethod.POST)
 	@Restricted(rolesAllowed = { UserRoleEnum.ADMIN },
 			setSessionAttributes = false)
 	@ResponseBody
 	public ResponseDTO processexcelUploadForm(
 			@ModelAttribute QuestionExcelFileUploadDTO questionExcelFileUploadDTO)
 			throws IOException
 	{
 		ResponseDTO response = new ResponseDTO();
 		MultipartFile multipartFile = null;
 		Map<String, String> errors = ValidationUtil.validate(
 				questionExcelFileUploadDTO, validator, messageSource);
 
 		if (!CoreUtil.isEmpty(errors))
 		{
 			multipartFile = questionExcelFileUploadDTO.getFile();
 			LOG.info("File Name = " + multipartFile.getOriginalFilename());
 			if (!multipartFile.getOriginalFilename().toLowerCase()
 					.endsWith(".xls")
 					|| !multipartFile.getOriginalFilename().toLowerCase()
 							.endsWith(".xlsx"))
 			{
 				errors.put("typeMismatch", "Please Enter Valid Excel File");
 				LOG.error(" Your file is invalid ");
 			}
 		}
 
 		if (!CoreUtil.isEmpty(errors))
 		{
 			questionService.uploadquestionExcel(new HSSFWorkbook(multipartFile
 					.getInputStream()));
 		}
 		else
 		{
 			response.setErrors(errors);
 			response.setStatus(OutcomeStatus.FAILURE);
 		}
 
 		return response;
 	}
 
 	/**
 	 * Custom property editor for the Category DTO
 	 * 
 	 * @param request
 	 *            http servlet request
 	 * @param binder
 	 *            spring's servlet request binder
 	 */
 	@InitBinder
 	protected void initBinder(HttpServletRequest request,
 			ServletRequestDataBinder binder)
 	{
 		binder.registerCustomEditor(List.class, "categoryDTOs",
 				new CustomCollectionEditor(List.class)
 				{
 					@Override
 					protected Object convertElement(Object element)
 					{
 						if (element != null)
 						{
 							CategoryDTO categoryDTO = new CategoryDTO();
 							categoryDTO.setId(Long.valueOf((String) element));
 							return categoryDTO;
 						}
 						return null;
 					}
 				});
 	}
 
 }
