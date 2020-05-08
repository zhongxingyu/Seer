 /*
  * Copyright © 2012, Source Tree, All Rights Reserved
  * 
  * QuestionServiceImpl.java
  * Modification History
  * *************************************************************
  * Date				Author						Comment
  * Nov 06, 2012		Chalam Pavuluri				Created
  * *************************************************************
  */
 package org.sourcetree.interview.service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.sourcetree.interview.dao.CategoryDAO;
 import org.sourcetree.interview.dao.QuestionDAO;
 import org.sourcetree.interview.dto.CategoryDTO;
 import org.sourcetree.interview.dto.QuestionDTO;
 import org.sourcetree.interview.entity.Category;
 import org.sourcetree.interview.entity.Question;
 import org.sourcetree.interview.support.CoreUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Question service implementation
  * 
  * @author Chalam Pavuluri
  */
 @Service
 @Transactional(readOnly = true)
 public class QuestionServiceImpl implements QuestionService
 {
 	@Autowired
 	private QuestionDAO questionDAO;
 
 	@Autowired
 	private CategoryDAO categoryDAO;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@Transactional(readOnly = false)
 	public void create(QuestionDTO questionDTO)
 	{
 		Question question = copyDTOtoEntity(questionDTO, null);
 
 		if (question != null)
 		{
 			questionDAO.save(question);
 			return;
 		}
 
 		throw new IllegalArgumentException("Invalid Data: Null");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@Transactional(readOnly = false)
 	public void update(QuestionDTO questionDTO)
 	{
 		if (questionDTO.getId() != null)
 		{
 			Question question = findQuestionById(questionDTO.getId());
 			if (question != null)
 			{
 				question = copyDTOtoEntity(questionDTO, question);
 
 				questionDAO.update(question);
 			}
 			return;
 		}
 
 		throw new IllegalArgumentException("Invalid ID for the Question.");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@Transactional(readOnly = false)
 	public void update(QuestionDTO questionDTO, Long questionId)
 	{
 		questionDTO.setId(questionId);
 		update(questionDTO);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Question findQuestionById(Long questionId)
 	{
 		return questionDAO.find(questionId);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public List<Question> getQuestionsByCategoryId(Long categoryId)
 	{
 		return questionDAO.getQuestionsByCategoryId(categoryId);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@Transactional(readOnly = false)
 	public boolean deleteQuestionById(Long questionId)
 	{
 		return questionDAO.deleteById(questionId);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public List<QuestionDTO> getQuestionsByCategoryName(String categoryName)
 	{
 		Category category = categoryDAO.findByParameter("categoryName",
 				categoryName);
 		if (category != null)
 		{
 			List<Question> questionList = category.getQuestions();
 			if (!CoreUtil.isEmpty(questionList))
 			{
 				List<QuestionDTO> questionDtos = new ArrayList<QuestionDTO>();
 				for (Question question : questionList)
 				{
 					QuestionDTO questionDTO = copyEntitytoDTO(question);
 					questionDtos.add(questionDTO);
 
 				}
 				return questionDtos;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * To Convert from DTO list to Entity list
 	 * 
 	 * @param categoryDTOs
 	 * @return category list
 	 */
 	private List<Category> processCategoryDto(
 			final List<CategoryDTO> categoryDTOs)
 	{
		if (CoreUtil.isEmpty(categoryDTOs))
 		{
 			List<Category> categories = new ArrayList<Category>();
 			Map<String, String> catMap = new HashMap<String, String>();
 			for (CategoryDTO categoryDto : categoryDTOs)
 			{
 				if (!catMap.containsKey(categoryDto.getId().toString()))
 				{
 					Category category = categoryDAO.find(categoryDto.getId());
 					categories.add(category);
 
 					catMap.put(category.getId().toString(),
 							category.getCategoryName());
 
 					// Checks for Sub Category
 					if (category.getParentCategory() != null)
 					{
 						if (!catMap.containsKey(category.getParentCategory()
 								.getId().toString()))
 						{
 							categories.add(category.getParentCategory());
 							catMap.put(category.getParentCategory().getId()
 									.toString(), category.getParentCategory()
 									.getCategoryName());
 						}
 					}
 				}
 			}
 			return categories;
 		}
 		return null;
 	}
 
 	/**
 	 * copies Question DTO data into Question entity
 	 * 
 	 * @param questionDTO
 	 *            Question DTO. cannot be empty
 	 * @param question
 	 *            Question entity. cannot be empty
 	 */
 	private Question copyDTOtoEntity(final QuestionDTO questionDTO,
 			final Question question)
 	{
 		if (questionDTO == null)
 		{
 			return null;
 		}
 
 		Question localQuestion = question;
 		if (localQuestion == null)
 		{
 			localQuestion = new Question();
 		}
 
 		localQuestion.setQuestion(questionDTO.getQuestion().trim());
 
 		localQuestion.setCategories(processCategoryDto(questionDTO
 				.getCategoryDTOs()));
 
 		localQuestion.setAnswer(questionDTO.getAnswer());
 
 		return localQuestion;
 	}
 
 	/**
 	 * copies Question entity data into Question dto
 	 * 
 	 * @param questionDTO
 	 *            Question DTO. cannot be empty
 	 * @param question
 	 *            Question entity. cannot be empty
 	 */
 	private QuestionDTO copyEntitytoDTO(final Question question)
 	{
 		if (question == null)
 		{
 
 			return null;
 		}
 		QuestionDTO questionDTO = new QuestionDTO();
 
 		questionDTO.setQuestion(question.getQuestion().trim());
 
 		questionDTO.setCategoryDTOs(processCategoryEntity(question
 				.getCategories()));
 		questionDTO.setAnswer(question.getAnswer());
 
 		return questionDTO;
 
 	}
 
 	/**
 	 * To Convert from Entity list to DTO list
 	 * 
 	 * @param category
 	 * @return categoryDTOs
 	 */
 	private List<CategoryDTO> processCategoryEntity(
 			final List<Category> categories)
 	{
 		if (CoreUtil.isEmpty(categories))
 		{
 			List<CategoryDTO> categoryDTOs = new ArrayList<CategoryDTO>();
 			for (Category category : categories)
 			{
 				CategoryDTO categoryDto = new CategoryDTO();
 				categoryDto.setId(category.getId());
 				categoryDto.setCategoryName(category.getCategoryName());
 				categoryDTOs.add(categoryDto);
 			}
 			return categoryDTOs;
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public QuestionDTO getQuestionDTOById(Long Id)
 	{
 		if (Id != null)
 		{
 			return questionDAO.getQuestionDTOById(Id);
 		}
 		return null;
 	}
 }
