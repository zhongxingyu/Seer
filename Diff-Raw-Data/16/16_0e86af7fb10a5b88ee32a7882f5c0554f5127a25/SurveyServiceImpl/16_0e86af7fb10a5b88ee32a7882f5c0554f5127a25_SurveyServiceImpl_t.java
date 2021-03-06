 package org.waterforpeople.mapping.app.gwt.server.survey;
 
 import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.waterforpeople.mapping.app.gwt.client.survey.OptionContainerDto;
 import org.waterforpeople.mapping.app.gwt.client.survey.QuestionDependencyDto;
 import org.waterforpeople.mapping.app.gwt.client.survey.QuestionDto;
 import org.waterforpeople.mapping.app.gwt.client.survey.QuestionGroupDto;
 import org.waterforpeople.mapping.app.gwt.client.survey.QuestionHelpDto;
 import org.waterforpeople.mapping.app.gwt.client.survey.QuestionOptionDto;
 import org.waterforpeople.mapping.app.gwt.client.survey.SurveyDto;
 import org.waterforpeople.mapping.app.gwt.client.survey.SurveyGroupDto;
 import org.waterforpeople.mapping.app.gwt.client.survey.SurveyService;
 import org.waterforpeople.mapping.app.gwt.client.survey.TranslationDto;
 import org.waterforpeople.mapping.app.gwt.client.survey.QuestionDto.QuestionType;
 import org.waterforpeople.mapping.app.util.DtoMarshaller;
 import org.waterforpeople.mapping.app.web.dto.SurveyAssemblyRequest;
 import org.waterforpeople.mapping.app.web.dto.SurveyTaskRequest;
 import org.waterforpeople.mapping.dao.SurveyContainerDao;
 import org.waterforpeople.mapping.dao.SurveyInstanceDAO;
 import org.waterforpeople.mapping.domain.SurveyInstance;
 
 import com.gallatinsystems.common.Constants;
 import com.gallatinsystems.device.app.web.DeviceManagerServlet;
 import com.gallatinsystems.framework.exceptions.IllegalDeletionException;
 import com.gallatinsystems.survey.dao.QuestionDao;
 import com.gallatinsystems.survey.dao.QuestionGroupDao;
 import com.gallatinsystems.survey.dao.SurveyDAO;
 import com.gallatinsystems.survey.dao.SurveyGroupDAO;
 import com.gallatinsystems.survey.dao.TranslationDao;
 import com.gallatinsystems.survey.domain.Question;
 import com.gallatinsystems.survey.domain.QuestionGroup;
 import com.gallatinsystems.survey.domain.QuestionHelpMedia;
 import com.gallatinsystems.survey.domain.QuestionOption;
 import com.gallatinsystems.survey.domain.Survey;
 import com.gallatinsystems.survey.domain.SurveyContainer;
 import com.gallatinsystems.survey.domain.SurveyGroup;
 import com.gallatinsystems.survey.domain.Translation;
 import com.gallatinsystems.survey.domain.Translation.ParentType;
 import com.gallatinsystems.survey.domain.xml.Dependency;
 import com.gallatinsystems.survey.domain.xml.Heading;
 import com.gallatinsystems.survey.domain.xml.Help;
 import com.gallatinsystems.survey.domain.xml.ObjectFactory;
 import com.gallatinsystems.survey.domain.xml.Option;
 import com.gallatinsystems.survey.domain.xml.Options;
 import com.gallatinsystems.survey.domain.xml.Text;
 import com.gallatinsystems.survey.domain.xml.ValidationRule;
 import com.gallatinsystems.survey.xml.SurveyXMLAdapter;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.labs.taskqueue.Queue;
 import com.google.appengine.api.labs.taskqueue.QueueFactory;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 public class SurveyServiceImpl extends RemoteServiceServlet implements
 		SurveyService {
 
 	public static final String FREE_QUESTION_TYPE = "free";
 	public static final String OPTION_QUESTION_TYPE = "option";
 	public static final String GEO_QUESTION_TYPE = "geo";
 	public static final String VIDEO_QUESTION_TYPE = "video";
 	public static final String PHOTO_QUESTION_TYPE = "photo";
 	public static final String SCAN_QUESTION_TYPE = "scan";
 	public static final String STRENGTH_QUESTION_TYPE = "strength";
 
 	private static final Logger log = Logger
 			.getLogger(DeviceManagerServlet.class.getName());
 
 	private static final long serialVersionUID = 5557965649047558451L;
 	private SurveyDAO surveyDao;
 
 	public SurveyServiceImpl() {
 		surveyDao = new SurveyDAO();
 	}
 
 	@Override
 	public SurveyDto[] listSurvey() {
 
 		List<Survey> surveys = surveyDao.list(Constants.ALL_RESULTS);
 		SurveyDto[] surveyDtos = null;
 		if (surveys != null) {
 			surveyDtos = new SurveyDto[surveys.size()];
 			for (int i = 0; i < surveys.size(); i++) {
 				SurveyDto dto = new SurveyDto();
 				Survey s = surveys.get(i);
 
 				dto.setName(s.getName());
 				dto.setVersion(s.getVersion() != null ? s.getVersion()
 						.toString() : "");
 				dto.setKeyId(s.getKey().getId());
 				surveyDtos[i] = dto;
 			}
 		}
 		return surveyDtos;
 	}
 
 	public ArrayList<SurveyGroupDto> listSurveyGroups(String cursorString,
 			Boolean loadSurveyFlag, Boolean loadQuestionGroupFlag,
 			Boolean loadQuestionFlag) {
 		ArrayList<SurveyGroupDto> surveyGroupDtoList = new ArrayList<SurveyGroupDto>();
 		SurveyGroupDAO surveyGroupDao = new SurveyGroupDAO();
 		for (SurveyGroup canonical : surveyGroupDao.list(cursorString,
 				loadSurveyFlag, loadQuestionGroupFlag, loadQuestionFlag)) {
 			SurveyGroupDto dto = new SurveyGroupDto();
 			DtoMarshaller.copyToDto(canonical, dto);
 			dto.setSurveyList(null);
 			if (canonical.getSurveyList() != null
 					&& canonical.getSurveyList().size() > 0) {
 				for (Survey survey : canonical.getSurveyList()) {
 					SurveyDto surveyDto = new SurveyDto();
 					DtoMarshaller.copyToDto(survey, surveyDto);
 					surveyDto.setQuestionGroupList(null);
 					if (survey.getQuestionGroupMap() != null
 							&& survey.getQuestionGroupMap().size() > 0) {
 						for (QuestionGroup questionGroup : survey
 								.getQuestionGroupMap().values()) {
 							QuestionGroupDto questionGroupDto = new QuestionGroupDto();
 							DtoMarshaller.copyToDto(questionGroup,
 									questionGroupDto);
 							if (questionGroup.getQuestionMap() != null
 									&& questionGroup.getQuestionMap().size() > 0) {
 								for (Entry<Integer, Question> questionEntry : questionGroup
 										.getQuestionMap().entrySet()) {
 									Question question = questionEntry
 											.getValue();
 									Integer order = questionEntry.getKey();
 									QuestionDto questionDto = new QuestionDto();
 									DtoMarshaller.copyToDto(question,
 											questionDto);
 									questionGroupDto.addQuestion(questionDto,
 											order);
 								}
 							}
 							surveyDto.addQuestionGroup(questionGroupDto);
 						}
 					}
 					dto.addSurvey(surveyDto);
 				}
 			}
 			surveyGroupDtoList.add(dto);
 		}
 		return surveyGroupDtoList;
 	}
 
 	/**
 	 * This method will return a list of all the questions that have a specific
 	 * type code
 	 */
 	public QuestionDto[] listSurveyQuestionByType(Long surveyId,
 			QuestionType type) {
 
 		QuestionDao questionDao = new QuestionDao();
 		List<Question> qList = questionDao.listQuestionByType(surveyId,
 				Question.Type.valueOf(type.toString()));
 		QuestionDto[] dtoList = null;
 		if (qList != null) {
 			dtoList = new QuestionDto[qList.size()];
 			for (int i = 0; i < qList.size(); i++) {
 				dtoList[i] = marshalQuestionDto(qList.get(i));
 			}
 		}
 		return dtoList;
 	}
 
 	/**
 	 * lists all surveys for a group
 	 */
 	@Override
 	public ArrayList<SurveyDto> listSurveysByGroup(String surveyGroupId) {
 		SurveyDAO dao = new SurveyDAO();
 		List<Survey> surveys = dao.listSurveysByGroup(Long
 				.parseLong(surveyGroupId));
 		ArrayList<SurveyDto> surveyDtos = null;
 		if (surveys != null) {
 			surveyDtos = new ArrayList<SurveyDto>();
 			for (Survey s : surveys) {
 				SurveyDto dto = new SurveyDto();
 
 				dto.setName(s.getName());
 				dto.setVersion(s.getVersion() != null ? s.getVersion()
 						.toString() : "");
 				dto.setKeyId(s.getKey().getId());
 				if (s.getStatus() != null) {
 					dto.setStatus(s.getStatus().toString());
 				}
 				surveyDtos.add(dto);
 			}
 		}
 		return surveyDtos;
 	}
 
 	@Override
 	public SurveyGroupDto save(SurveyGroupDto value) {
 		SurveyGroupDAO sgDao = new SurveyGroupDAO();
 		SurveyGroup surveyGroup = new SurveyGroup();
 		DtoMarshaller.copyToCanonical(surveyGroup, value);
 		surveyGroup.setSurveyList(null);
 		for (SurveyDto item : value.getSurveyList()) {
 			// SurveyDto item = value.getSurveyList().get(0);
 			Survey survey = new Survey();
 			DtoMarshaller.copyToCanonical(survey, item);
 			survey.setQuestionGroupMap(null);
 			int order = 1;
 			for (QuestionGroupDto qgDto : item.getQuestionGroupList()) {
 				QuestionGroup qg = new QuestionGroup();
 				DtoMarshaller.copyToCanonical(qg, qgDto);
 				survey.addQuestionGroup(order++, qg);
 				int qOrder = 1;
 				for (Entry<Integer, QuestionDto> qDto : qgDto.getQuestionMap()
 						.entrySet()) {
 					Question q = marshalQuestion(qDto.getValue());
 					qg.addQuestion(qOrder++, q);
 				}
 			}
 			surveyGroup.addSurvey(survey);
 		}
 
 		DtoMarshaller.copyToDto(sgDao.save(surveyGroup), value);
 		return value;
 	}
 
 	public static QuestionDto marshalQuestionDto(Question q) {
 		QuestionDto qDto = new QuestionDto();
 
 		qDto.setKeyId(q.getKey().getId());
 		qDto.setQuestionGroupId(q.getQuestionGroupId());
 		qDto.setPath(q.getPath());
 		qDto.setOrder(q.getOrder());
 		qDto.setSurveyId(q.getSurveyId());
 		if (q.getText() != null)
 			qDto.setText(q.getText());
 		if (q.getTip() != null)
 			qDto.setTip(q.getTip());
 		if (q.getType() != null)
 			qDto.setType(QuestionDto.QuestionType.valueOf(q.getType()
 					.toString()));
 		if (q.getValidationRule() != null)
 			qDto.setValidationRule(q.getValidationRule());
 
 		if (q.getQuestionHelpMediaMap() != null) {
 			for (QuestionHelpMedia qh : q.getQuestionHelpMediaMap().values()) {
 				QuestionHelpDto qhDto = new QuestionHelpDto();
 				// Beanutils throws a concurrent exception so need
 				// to copy props by hand
 				qhDto.setResourceUrl(qh.getUrl());
 				qhDto.setText(qhDto.getText());
 				qDto.addQuestionHelp(qhDto);
 			}
 		}
 
 		if (q.getQuestionOptionMap() != null) {
 			OptionContainerDto ocDto = new OptionContainerDto();
 			if (q.getAllowOtherFlag() != null)
 				ocDto.setAllowOtherFlag(q.getAllowOtherFlag());
 			if (q.getAllowMultipleFlag() != null)
 				ocDto.setAllowMultipleFlag(q.getAllowMultipleFlag());
 			for (QuestionOption qo : q.getQuestionOptionMap().values()) {
 				QuestionOptionDto ooDto = new QuestionOptionDto();
 				ooDto.setTranslationMap(marshalTranslations(qo
 						.getTranslationMap()));
 				ooDto.setKeyId(qo.getKey().getId());
 				if (qo.getCode() != null)
 					ooDto.setCode(qo.getCode());
 				if (qo.getText() != null)
 					ooDto.setText(qo.getText());
 				ooDto.setOrder(qo.getOrder());
 				ocDto.addQuestionOption(ooDto);
 
 			}
 			qDto.setOptionContainerDto(ocDto);
 		}
 
 		if (q.getDependentQuestionId() != null) {
 			QuestionDependencyDto qdDto = new QuestionDependencyDto();
 			qdDto.setQuestionId(q.getDependentQuestionId());
 			qdDto.setAnswerValue(q.getDependentQuestionAnswer());
 			qDto.setQuestionDependency(qdDto);
 		}
 
 		qDto.setTranslationMap(marshalTranslations(q.getTranslationMap()));
 
 		return qDto;
 	}
 
 	private static TreeMap<String, TranslationDto> marshalTranslations(
 			Map<String, Translation> translationMap) {
 		TreeMap<String, TranslationDto> transMap = null;
 		if (translationMap != null && translationMap.size() > 0) {
 			transMap = new TreeMap<String, TranslationDto>();
 			for (Translation trans : translationMap.values()) {
 				TranslationDto tDto = new TranslationDto();
 				tDto.setKeyId(trans.getKey().getId());
 				tDto.setLangCode(trans.getLanguageCode());
 				tDto.setText(trans.getText());
 				tDto.setParentId(trans.getParentId());
 				tDto.setParentType(trans.getParentType().toString());
 				transMap.put(tDto.getLangCode(), tDto);
 			}
 		}
 		return transMap;
 	}
 
 	private static TreeMap<String, Translation> marshalFromDtoTranslations(
 			Map<String, TranslationDto> translationMap) {
 		TreeMap<String, Translation> transMap = null;
 		if (translationMap != null && translationMap.size() > 0) {
 			transMap = new TreeMap<String, Translation>();
 			for (TranslationDto trans : translationMap.values()) {
 				Translation t = new Translation();
 				if (trans.getKeyId() != null)
 					t.setKey((KeyFactory.createKey(Translation.class
 							.getSimpleName(), trans.getKeyId())));
 				t.setLanguageCode(trans.getLangCode());
 				t.setText(trans.getText());
 				t.setParentId(trans.getParentId());
 				if (trans.getParentType().equals(
 						Translation.ParentType.QUESTION_TEXT.toString()))
 					t.setParentType(ParentType.QUESTION_TEXT);
 				else if (trans.getParentType().equals(
 						Translation.ParentType.QUESTION_OPTION.toString()))
 					t.setParentType(ParentType.QUESTION_OPTION);
 
 				transMap.put(t.getLanguageCode(), t);
 			}
 		}
 		return transMap;
 	}
 
 	public Question marshalQuestion(QuestionDto qdto) {
 		Question q = new Question();
 		if (qdto.getKeyId() != null)
 			q.setKey((KeyFactory.createKey(Question.class.getSimpleName(), qdto
 					.getKeyId())));
 
 		q.setQuestionGroupId(qdto.getQuestionGroupId());
 		q.setOrder(qdto.getOrder());
 		q.setPath(qdto.getPath());
 		q.setMandatoryFlag(qdto.getMandatoryFlag());
 		q.setAllowMultipleFlag(qdto.getAllowMultipleFlag());
 		q.setAllowOtherFlag(qdto.getAllowOtherFlag());
 		if (qdto.getText() != null) {
 			q.setText(qdto.getText());
 		}
 		if (qdto.getTip() != null)
 			q.setTip(qdto.getTip());
 		if (qdto.getType() != null)
 			q.setType(Question.Type.valueOf(qdto.getType().toString()));
 		if (qdto.getValidationRule() != null)
 			q.setValidationRule(qdto.getValidationRule());
 
 		if (qdto.getQuestionHelpList() != null) {
 			ArrayList<QuestionHelpDto> qHListDto = qdto.getQuestionHelpList();
 			int i = 1;
 			for (QuestionHelpDto qhDto : qHListDto) {
 				QuestionHelpMedia qh = new QuestionHelpMedia();
 				// Beanutils throws a concurrent exception so need
 				// to copy props by hand
 				qh.setUrl(qhDto.getResourceUrl());
 				qh.setText(qhDto.getText());
 				q.addHelpMedia(i++, qh);
 			}
 		}
 
 		if (qdto.getOptionContainerDto() != null) {
 			OptionContainerDto ocDto = qdto.getOptionContainerDto();
 
 			if (ocDto.getAllowOtherFlag() != null) {
 				q.setAllowOtherFlag(ocDto.getAllowOtherFlag());
 			}
 			if (ocDto.getAllowMultipleFlag() != null) {
 				q.setAllowMultipleFlag(ocDto.getAllowMultipleFlag());
 			}
 
 			if (ocDto.getOptionsList() != null) {
 				ArrayList<QuestionOptionDto> optionDtoList = ocDto
 						.getOptionsList();
 				for (QuestionOptionDto qoDto : optionDtoList) {
 					QuestionOption oo = new QuestionOption();
 					if (qoDto.getKeyId() != null)
 						oo.setKey((KeyFactory.createKey(QuestionOption.class
 								.getSimpleName(), qoDto.getKeyId())));
 					if (qoDto.getCode() != null)
 						oo.setCode(qoDto.getCode());
 					if (qoDto.getText() != null)
 						oo.setText(qoDto.getText());
 					oo.setOrder(qoDto.getOrder());
 					// Hack
 					if (qoDto.getTranslationMap() != null) {
 						TreeMap<String, Translation> transTreeMap = SurveyServiceImpl
 								.marshalFromDtoTranslations(qoDto
 										.getTranslationMap());
 
 						HashMap<String, Translation> transMap = new HashMap<String, Translation>();
 						for (Map.Entry<String, Translation> entry : transTreeMap
 								.entrySet()) {
 							transMap.put(entry.getKey(), entry.getValue());
 						}
 						oo.setTranslationMap(transMap);
 					}
 					q.addQuestionOption(oo);
 				}
 			}
 		}
 		if (qdto.getQuestionDependency() != null) {
 			q.setDependentQuestionId(qdto.getQuestionDependency()
 					.getQuestionId());
 			q.setDependentQuestionAnswer(qdto.getQuestionDependency()
 					.getAnswerValue());
 			q.setDependentFlag(true);
 		}
 
 		if (qdto.getTranslationMap() != null) {
 			TreeMap<String, Translation> transMap = SurveyServiceImpl
 					.marshalFromDtoTranslations(qdto.getTranslationMap());
 			q.setTranslationMap(transMap);
 		}
 
 		return q;
 	}
 
 	/**
 	 * fully hydrates a single survey object
 	 */
 	public SurveyDto loadFullSurvey(Long surveyId) {
 		Survey survey = surveyDao.loadFullSurvey(surveyId);
 		SurveyDto dto = null;
 		if (survey != null) {
 			dto = new SurveyDto();
 			DtoMarshaller.copyToDto(survey, dto);
 			dto.setQuestionGroupList(null);
 			if (survey.getQuestionGroupMap() != null) {
 				ArrayList<QuestionGroupDto> qGroupDtoList = new ArrayList<QuestionGroupDto>();
 				for (QuestionGroup qg : survey.getQuestionGroupMap().values()) {
 					QuestionGroupDto qgDto = new QuestionGroupDto();
 					DtoMarshaller.copyToDto(qg, qgDto);
 					qgDto.setQuestionMap(null);
 					qGroupDtoList.add(qgDto);
 					if (qg.getQuestionMap() != null) {
 						TreeMap<Integer, QuestionDto> qDtoMap = new TreeMap<Integer, QuestionDto>();
 						for (Entry<Integer, Question> entry : qg
 								.getQuestionMap().entrySet()) {
 							QuestionDto qdto = marshalQuestionDto(entry
 									.getValue());
 
 							qDtoMap.put(entry.getKey(), qdto);
 						}
 						qgDto.setQuestionMap(qDtoMap);
 					}
 				}
 				dto.setQuestionGroupList(qGroupDtoList);
 			}
 		}
 		return dto;
 	}
 
 	@Override
 	public List<SurveyDto> listSurveysForSurveyGroup(String surveyGroupId) {
 		List<Survey> surveyList = surveyDao.listSurveysByGroup(Long
 				.parseLong(surveyGroupId));
 		List<SurveyDto> surveyDtoList = new ArrayList<SurveyDto>();
 		for (Survey canonical : surveyList) {
 			SurveyDto dto = new SurveyDto();
 			DtoMarshaller.copyToDto(canonical, dto);
 			surveyDtoList.add(dto);
 		}
 		return surveyDtoList;
 	}
 
 	@Override
 	public ArrayList<QuestionGroupDto> listQuestionGroupsBySurvey(
 			String surveyId) {
 		QuestionGroupDao questionGroupDao = new QuestionGroupDao();
 		TreeMap<Integer, QuestionGroup> questionGroupList = questionGroupDao
 				.listQuestionGroupsBySurvey(new Long(surveyId));
 		ArrayList<QuestionGroupDto> questionGroupDtoList = new ArrayList<QuestionGroupDto>();
 		for (QuestionGroup canonical : questionGroupList.values()) {
 			QuestionGroupDto dto = new QuestionGroupDto();
 			DtoMarshaller.copyToDto(canonical, dto);
 			questionGroupDtoList.add(dto);
 		}
 		return questionGroupDtoList;
 	}
 
 	@Override
 	public ArrayList<QuestionDto> listQuestionsByQuestionGroup(
 			String questionGroupId, boolean needDetails) {
 		QuestionDao questionDao = new QuestionDao();
 		TreeMap<Integer, Question> questionList = questionDao
 				.listQuestionsByQuestionGroup(Long.parseLong(questionGroupId),
 						needDetails);
 		java.util.ArrayList<QuestionDto> questionDtoList = new ArrayList<QuestionDto>();
 		for (Question canonical : questionList.values()) {
 			QuestionDto dto = marshalQuestionDto(canonical);
 
 			questionDtoList.add(dto);
 		}
 		return questionDtoList;
 	}
 
 	@Override
 	public String deleteQuestion(QuestionDto value, Long questionGroupId) {
 		QuestionDao questionDao = new QuestionDao();
 		Question canonical = new Question();
 		DtoMarshaller.copyToCanonical(canonical, value);
 		try {
 			questionDao.delete(canonical);
 		} catch (IllegalDeletionException e) {
 
 			return e.getError();
 		}
 		return null;
 
 	}
 
 	@Override
 	public QuestionDto saveQuestion(QuestionDto value, Long questionGroupId) {
 		QuestionDao questionDao = new QuestionDao();
 		Question question = marshalQuestion(value);
 		question = questionDao.save(question, questionGroupId);
 
 		return marshalQuestionDto(question);
 	}
 
 	@Override
 	public QuestionGroupDto saveQuestionGroup(QuestionGroupDto dto,
 			Long surveyId) {
 		QuestionGroup questionGroup = new QuestionGroup();
 		DtoMarshaller.copyToCanonical(questionGroup, dto);
 		QuestionGroupDao questionGroupDao = new QuestionGroupDao();
		if(questionGroup.getOrder() == null || questionGroup.getOrder() == 0){
			Map<Integer, QuestionGroup>  items = questionGroupDao.listQuestionGroupsBySurvey(questionGroup.getSurveyId());
			if(items != null){
				questionGroup.setOrder(items.size()+1);
			}else{
				questionGroup.setOrder(1);
			}
		}
 		questionGroup = questionGroupDao.save(questionGroup, surveyId, null);
 		DtoMarshaller.copyToDto(questionGroup, dto);
 		return dto;
 	}
 
 	@Override
 	public SurveyDto saveSurvey(SurveyDto surveyDto, Long surveyGroupId) {
 		Survey canonical = new Survey();
 		DtoMarshaller.copyToCanonical(canonical, surveyDto);
 		canonical.setStatus(Survey.Status.NOT_PUBLISHED);
 		SurveyDAO surveyDao = new SurveyDAO();
 		if (canonical.getKey() != null && canonical.getSurveyGroupId() == 0) {
 			// fetch record from db so we don't loose assoc
 			Survey sTemp = surveyDao.getByKey(canonical.getKey());
 			canonical.setSurveyGroupId(sTemp.getSurveyGroupId());
 			canonical.setPath(sTemp.getPath());
 		}
 		canonical = surveyDao.save(canonical);
 		DtoMarshaller.copyToDto(canonical, surveyDto);
 
 		return surveyDto;
 
 	}
 
 	@Override
 	public SurveyGroupDto saveSurveyGroup(SurveyGroupDto dto) {
 		SurveyGroup canonical = new SurveyGroup();
 		SurveyGroupDAO surveyGroupDao = new SurveyGroupDAO();
 		DtoMarshaller.copyToCanonical(canonical, dto);
 		canonical = surveyGroupDao.save(canonical);
 		DtoMarshaller.copyToDto(canonical, dto);
 		return dto;
 	}
 
 	/**
 	 * saves or updates a list of translation objects and returns the saved
 	 * value. If the text is null or blank and the ID is populated, that
 	 * translation will be deleted since we shouldn't allow blank translations.
 	 */
 	@Override
 	public List<TranslationDto> saveTranslations(
 			List<TranslationDto> translations) {
 		TranslationDao translationDao = new TranslationDao();
 		List<TranslationDto> deletedItems = new ArrayList<TranslationDto>();
 		for (TranslationDto t : translations) {
 			Translation transDomain = new Translation();
 			// need to work around marshaller's inability to translate string to
 			// enumeration values. We need to set it back after the copy call
 			String parentType = t.getParentType();
 			t.setParentType(null);
 			DtoMarshaller.copyToCanonical(transDomain, t);
 			t.setParentType(parentType);
 			transDomain.setParentType(ParentType.valueOf(parentType));
 			transDomain.setLanguageCode(t.getLangCode());
 			if (transDomain.getKey() != null
 					&& (transDomain.getText() == null || transDomain.getText()
 							.trim().length() == 0)) {
 				Translation itemToDelete = translationDao.getByKey(transDomain
 						.getKey());
 				if (itemToDelete != null) {
 					translationDao.delete(itemToDelete);
 					deletedItems.add(t);
 				}
 			} else {
 				transDomain = translationDao.save(transDomain);
 			}
 			t.setKeyId(transDomain.getKey().getId());
 		}
 		translations.removeAll(deletedItems);
 		return translations;
 	}
 
 	@Override
 	public void publishSurveyAsync(Long surveyId) {
 		surveyDao.incrementVersion(surveyId);
 		Queue surveyAssemblyQueue = QueueFactory.getQueue("surveyAssembly");
 		surveyAssemblyQueue.add(url("/app_worker/surveyassembly").param(
 				"action", SurveyAssemblyRequest.ASSEMBLE_SURVEY).param(
 				"surveyId", surveyId.toString()));
 	}
 
 	@Override
 	public String publishSurvey(Long surveyId) {
 		try {
 			SurveyDAO surveyDao = new SurveyDAO();
 			Survey survey = surveyDao.loadFullSurvey(surveyId);
 			SurveyXMLAdapter sax = new SurveyXMLAdapter();
 			ObjectFactory objFactory = new ObjectFactory();
 
 			// System.out.println("XML Marshalling for survey: " + surveyId);
 			com.gallatinsystems.survey.domain.xml.Survey surveyXML = objFactory
 					.createSurvey();
 			ArrayList<com.gallatinsystems.survey.domain.xml.QuestionGroup> questionGroupXMLList = new ArrayList<com.gallatinsystems.survey.domain.xml.QuestionGroup>();
 			for (QuestionGroup qg : survey.getQuestionGroupMap().values()) {
 				// System.out.println("	QuestionGroup: " + qg.getKey().getId() +
 				// ":"
 				// + qg.getCode() + ":" + qg.getDescription());
 				com.gallatinsystems.survey.domain.xml.QuestionGroup qgXML = objFactory
 						.createQuestionGroup();
 				Heading heading = objFactory.createHeading();
 				heading.setContent(qg.getCode());
 				qgXML.setHeading(heading);
 
 				// TODO: implement questionGroup order attribute
 				// qgXML.setOrder(qg.getOrder());
 				ArrayList<com.gallatinsystems.survey.domain.xml.Question> questionXMLList = new ArrayList<com.gallatinsystems.survey.domain.xml.Question>();
 				if (qg.getQuestionMap() != null) {
 					for (Entry<Integer, Question> qEntry : qg.getQuestionMap()
 							.entrySet()) {
 						Question q = qEntry.getValue();
 						com.gallatinsystems.survey.domain.xml.Question qXML = objFactory
 								.createQuestion();
 						qXML.setId(new String("" + q.getKey().getId() + ""));
 						// ToDo fix
 						qXML.setMandatory("false");
 						if (q.getText() != null) {
 							Text text = new Text();
 							text.setContent(q.getText());
 							qXML.setText(text);
 						}
 						if (q.getTip() != null) {
 
 							Help tip = new Help();
 							Text t = new Text();
 							t.setContent(q.getTip());
 							tip.setText(t);
 							qXML.setHelp(tip);
 						}
 
 						if (q.getValidationRule() != null) {
 							ValidationRule validationRule = objFactory
 									.createValidationRule();
 
 							// TODO: set validation rule xml
 							// validationRule.setAllowDecimal(value)
 						}
 
 						if (q.getType().equals(QuestionType.FREE_TEXT))
 							qXML.setType(FREE_QUESTION_TYPE);
 						else if (q.getType().equals(QuestionType.GEO))
 							qXML.setType(GEO_QUESTION_TYPE);
 						else if (q.getType().equals(QuestionType.NUMBER)) {
 							qXML.setType(FREE_QUESTION_TYPE);
 							ValidationRule vrule = new ValidationRule();
 							vrule.setValidationType("numeric");
 							vrule.setSigned("false");
 							qXML.setValidationRule(vrule);
 						} else if (q.getType().equals(QuestionType.OPTION)) {
 							qXML.setType(OPTION_QUESTION_TYPE);
 						} else if (q.getType().equals(QuestionType.STRENGTH)) {
 							qXML.setType(STRENGTH_QUESTION_TYPE);
 						} else if (q.getType().equals(QuestionType.PHOTO))
 							qXML.setType(PHOTO_QUESTION_TYPE);
 						else if (q.getType().equals(QuestionType.VIDEO))
 							qXML.setType(VIDEO_QUESTION_TYPE);
 						else if (q.getType().equals(QuestionType.SCAN))
 							qXML.setType(SCAN_QUESTION_TYPE);
 
 						if (qEntry.getKey() != null)
 							qXML.setOrder(qEntry.getKey().toString());
 						// ToDo set dependency xml
 						Dependency dependency = objFactory.createDependency();
 						if (q.getDependentQuestionId() != null) {
 							dependency.setQuestion(q.getDependentQuestionId()
 									.toString());
 							dependency.setAnswerValue(q
 									.getDependentQuestionAnswer());
 							qXML.setDependency(dependency);
 						}
 
 						if (q.getQuestionOptionMap() != null
 								&& q.getQuestionOptionMap().size() > 0) {
 
 							Options options = objFactory.createOptions();
 
 							if (q.getAllowOtherFlag() != null) {
 								options.setAllowOther(q.getAllowOtherFlag()
 										.toString());
 							}
 
 							ArrayList<Option> optionList = new ArrayList<Option>();
 							for (QuestionOption qo : q.getQuestionOptionMap()
 									.values()) {
 								Option option = objFactory.createOption();
 								Text t = new Text();
 								t.setContent(qo.getText());
 								option.addContent(t);
 								option.setValue(qo.getCode());
 								optionList.add(option);
 							}
 							options.setOption(optionList);
 
 							qXML.setOptions(options);
 						}
 						questionXMLList.add(qXML);
 					}
 				}
 				qgXML.setQuestion(questionXMLList);
 				questionGroupXMLList.add(qgXML);
 			}
 			surveyXML.setQuestionGroup(questionGroupXMLList);
 			String surveyDocument = sax.marshal(surveyXML);
 			SurveyContainerDao scDao = new SurveyContainerDao();
 			SurveyContainer sc = new SurveyContainer();
 			sc.setSurveyId(surveyId);
 			sc.setSurveyDocument(new com.google.appengine.api.datastore.Text(
 					surveyDocument));
 			SurveyContainer scFound = scDao.findBySurveyId(sc.getSurveyId());
 			if (scFound != null) {
 				scFound.setSurveyDocument(sc.getSurveyDocument());
 				scDao.save(scFound);
 			} else
 				scDao.save(sc);
 			survey.setStatus(Survey.Status.PUBLISHED);
 			surveyDao.save(survey);
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			StringBuilder sb = new StringBuilder();
 			sb.append("Could not publish survey: \n cause: " + ex.getCause()
 					+ " \n message" + ex.getMessage() + "\n stack trace:  ");
 
 			return sb.toString();
 		}
 
 		return "Survey successfully published";
 	}
 
 	@Override
 	public QuestionDto loadQuestionDetails(Long questionId) {
 		QuestionDao questionDao = new QuestionDao();
 		Question canonical = questionDao.getByKey(questionId, true);
 		if (canonical != null) {
 			return marshalQuestionDto(canonical);
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public String deleteQuestionGroup(QuestionGroupDto value, Long surveyId) {
 		if (value != null) {
 			QuestionGroupDao qgDao = new QuestionGroupDao();
 			qgDao.delete(qgDao.getByKey(value.getKeyId()));
 		}
 		return null;
 	}
 
 	@Override
 	public String deleteSurvey(SurveyDto value, Long surveyGroupId) {
 		if (value != null) {
 			SurveyDAO surveyDao = new SurveyDAO();
 			try {
 				Survey s = new Survey();
 				DtoMarshaller.copyToCanonical(s, value);
 				surveyDao.delete(s);
 			} catch (IllegalDeletionException e) {
 				log.log(Level.SEVERE, "Could not delete survey", e);
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public String deleteSurveyGroup(SurveyGroupDto value) {
 		if (value != null) {
 			SurveyGroupDAO surveyGroupDao = new SurveyGroupDAO();
 			surveyGroupDao.delete(marshallSurveyGroup(value));
 		}
 		return null;
 	}
 
 	public static SurveyGroup marshallSurveyGroup(SurveyGroupDto dto) {
 		SurveyGroup sg = new SurveyGroup();
 		if (dto.getKeyId() != null)
 			sg.setKey(KeyFactory.createKey(SurveyGroup.class.getSimpleName(),
 					dto.getKeyId()));
 		if (dto.getCode() != null)
 			sg.setCode(dto.getCode());
 		return sg;
 	}
 
 	@Override
 	public void rerunAPMappings(Long surveyId) {
 		SurveyInstanceDAO siDao = new SurveyInstanceDAO();
 		List<SurveyInstance> siList = siDao.listSurveyInstanceBySurveyId(
 				surveyId, null);
 		if (siList != null && siList.size() > 0) {
 			Queue queue = QueueFactory.getDefaultQueue();
 			StringBuffer buffer = new StringBuffer();
 			for (int i = 0; i < siList.size(); i++) {
 				if (i > 0) {
 					buffer.append(",");
 				}
 				buffer.append(siList.get(i).getKey().getId());
 			}
 			queue.add(url("/app_worker/surveytask").param("action",
 					"reprocessMapSurveyInstance").param(
 					SurveyTaskRequest.ID_PARAM, surveyId.toString()).param(
 					SurveyTaskRequest.ID_LIST_PARAM, buffer.toString()).param(
 					SurveyTaskRequest.CURSOR_PARAM,
 					SurveyInstanceDAO.getCursor(siList)));
 		}
 	}
 
 }
