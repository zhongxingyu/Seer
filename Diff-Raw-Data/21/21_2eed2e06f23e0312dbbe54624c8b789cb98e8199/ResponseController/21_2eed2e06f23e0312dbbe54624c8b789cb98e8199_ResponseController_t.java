 /*
  * INSERT COPYRIGHT HERE
  */
 
 package com.wadpam.survey.web;
 
 import com.wadpam.docrest.domain.RestCode;
 import com.wadpam.docrest.domain.RestReturn;
 import com.wadpam.open.json.JCursorPage;
 import com.wadpam.open.mvc.CrudController;
import com.wadpam.open.mvc.CrudListener;
 import com.wadpam.survey.domain.DAnswer;
 import com.wadpam.survey.domain.DResponse;
 import com.wadpam.survey.domain.DSurvey;
 import com.wadpam.survey.domain.DVersion;
 import com.wadpam.survey.json.JAnswer;
 import com.wadpam.survey.json.JResponse;
 import com.wadpam.survey.service.ResponseService;
 import com.wadpam.survey.service.SurveyService;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import net.sf.mardao.core.CursorPage;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  *
  * @author sosandstrom
  */
 @Controller
 @RequestMapping("{domain}/survey/v10/{surveyId}/version/v10/{versionId}/response")
 public class ResponseController extends CrudController<JResponse, 
         DResponse, 
         Long, 
         ResponseService> {
     public static final int    ERR_RESPONSE_GET_NOT_FOUND = SurveyService.ERR_RESPONSE + 1;
     public static final int    ERR_CREATE_NOT_FOUND       = SurveyService.ERR_RESPONSE + 2;
     public static final int    ERR_CREATE_CONFLICT        = SurveyService.ERR_RESPONSE + 3;
 
     public static final String MODEL_NAME_SURVEYID = "surveyId";
     public static final String MODEL_NAME_VERSIONID = "versionId";
     public static final String MODEL_NAME_FORMANSWERS = "formAnswers";
     public static final String MODEL_NAME_QUESTIONIDS = "quetionIds";
     
     public static final String NAME_INNER_ANSWERS = "answers";
     
     protected AnswerController answerController;
     
     protected SurveyService surveyService;
 
     @ModelAttribute("surveyId")
     public Long addSurveyId(@PathVariable Long surveyId) {
         return surveyId;
     }
     
     @ModelAttribute("versionId")
     public Long addVersionId(@PathVariable Long versionId) {
         return versionId;
     }
     
     @Override
     public void addInnerObjects(HttpServletRequest request, 
             HttpServletResponse response,
             String domain,
             Model model,
             JResponse jEntity) {
         LOG.debug("addInnerObjects for {}...", jEntity);
         if (null != jEntity && 
                 (null != request.getParameter(NAME_INNER_ANSWERS) || 
                  null != request.getAttribute(NAME_INNER_ANSWERS))) {
             // add answers
             Long outerId = Long.parseLong(jEntity.getId());
             model.addAttribute("responseId", outerId);
             final JCursorPage<JAnswer> inners = answerController.getPage(request, response,
                     domain, model, 5, null);
             LOG.debug("found inners {}", inners.getItems());
             jEntity.setAnswers(inners.getItems());
         }
     }
 
     @Override
     protected DResponse create(HttpServletRequest request, 
             String domain, 
             JResponse body) {
         return surveyService.upsertResponse(body);
     }
     
     @Override
    protected DResponse update(HttpServletRequest request, HttpServletResponse response,
            String domain,
            Long id,
            String xRequestedWith,
            Model model,
            JResponse body) {
        
        LOG.debug(body.toString());

        JResponse amendedBody = populateRequestBody(request, model, body);
        DResponse d = convertJson(amendedBody);
        preService(request, domain, CrudListener.UPDATE, amendedBody, d, id);
        surveyService.upsertResponse(amendedBody);
        postService(request, domain, CrudListener.UPDATE, amendedBody, id, d);
        
        return d;
    }
    
    @Override
     protected Collection<String> getInnerParameterNames() {
         return Arrays.asList(NAME_INNER_ANSWERS);
     }
     
     /**
      * Queries for a (next) page of entities
      * @param pageSize default is 10
      * @param cursorKey null to get first page
      * @return a page of entities
      */
     @Override
     @RestReturn(value=JCursorPage.class, code={
         @RestCode(code=200, description="A CursorPage with JSON entities", message="OK")})
     @RequestMapping(value="v10", method= RequestMethod.GET)
     @ResponseBody
     public JCursorPage<JResponse> getPage(HttpServletRequest request, 
             HttpServletResponse response,
             @PathVariable String domain, 
             Model model, 
             @RequestParam(defaultValue="10") int pageSize, 
             @RequestParam(required=false) String cursorKey) {
         
         Long versionId = (Long) model.asMap().get("versionId");
         CursorPage<DResponse, Long> page = surveyService.getResponsesPage(versionId, pageSize, cursorKey);
         return convertPage(page);
     }
     
     /**
      * Queries for a (next) page of entities, all related to specified meeting.
      * @param extMeetingId the specified meeting's external id
      * @param answers set to true to get inner answers
      * @param pageSize default is 10
      * @param cursorKey null to get first page
      * @return a page of entities
      */
     @RestReturn(value=JCursorPage.class, entity=JResponse.class, code={
         @RestCode(code=200, description="A CursorPage with JSON entities", message="OK")})
     @RequestMapping(value="v10", method= RequestMethod.GET, params="extMeetingId")
     @ResponseBody
     public JCursorPage<JResponse> getPageByExtMeetingId(
             @RequestParam String extMeetingId,
             @RequestParam(defaultValue="false") boolean answers, 
             @RequestParam(defaultValue="10") int pageSize, 
             @RequestParam(required=false) String cursorKey) {
         final CursorPage<DResponse, Long> page = surveyService.getResponsesPageByExtMeetingId(
                 extMeetingId, pageSize, cursorKey);
         
         final JCursorPage<JResponse> body = (JCursorPage<JResponse>) convertPage(page);
 
         // inner answers to include?
         if (answers) {
             HashMap<Long, JResponse> ids = new HashMap<Long, JResponse>();
             for (JResponse j : body.getItems()) {
                 ids.put(Long.parseLong(j.getId()), j);
             }
             
             if (!ids.isEmpty()) {
                 Iterable<DAnswer> dAnswers = surveyService.getAnswersByResponseIds(ids.keySet());
                 List<JAnswer> jAnswers = (List<JAnswer>) answerController.convert(dAnswers);
                 
                 // distribute all answers to correct JResponse
                 JResponse jResponse;
                 Collection<JAnswer> inner;
                 for (JAnswer da : jAnswers) {
                     jResponse = ids.get(da.getResponseId());
                     inner = jResponse.getAnswers();
                     if (null == inner) {
                         inner = new ArrayList<JAnswer>();
                         jResponse.setAnswers(inner);
                     }
                     inner.add(da);
                 }
             }
         }
         
         return body;
     }
     
     
     
     @ModelAttribute
     public void populateModel(
         @PathVariable Long surveyId, 
         @PathVariable Long versionId,
         @RequestParam(required=false) String[] formAnswers,
         @RequestParam(required=false) Long[] questionIds,
         Model model) {
         
         model.addAttribute(MODEL_NAME_SURVEYID, surveyId);
         model.addAttribute(MODEL_NAME_VERSIONID, versionId);
         if (null != formAnswers) {
             model.addAttribute(MODEL_NAME_FORMANSWERS, formAnswers);
         }
         if (null != questionIds) {
             model.addAttribute(MODEL_NAME_QUESTIONIDS, questionIds);
         }
     }
 
     @Override
     protected JResponse populateRequestBody(HttpServletRequest request, 
             Model model, 
             JResponse body) {
         
         if (null == body.getSurveyId() && model.containsAttribute(MODEL_NAME_SURVEYID)) {
             Long surveyId = (Long) model.asMap().get(MODEL_NAME_SURVEYID);
             body.setSurveyId(surveyId);
         }
         
         if (null == body.getVersionId() && model.containsAttribute(MODEL_NAME_VERSIONID)) {
             Long versionId = (Long) model.asMap().get(MODEL_NAME_VERSIONID);
             body.setVersionId(versionId);
         }
         
         if (null == body.getAnswers() && 
                 model.containsAttribute(MODEL_NAME_FORMANSWERS) &&
                 model.containsAttribute(MODEL_NAME_QUESTIONIDS)) {
             String[] formAnswers = (String[]) model.asMap().get(MODEL_NAME_FORMANSWERS);
             Long[] questionIds = (Long[]) model.asMap().get(MODEL_NAME_QUESTIONIDS);
             
             ArrayList<JAnswer> answers = new ArrayList<JAnswer>();
             body.setAnswers(answers);
             int i = 0;
             for (Long id : questionIds) {
                 JAnswer a = new JAnswer();
                 a.setAnswer(formAnswers[i]);
                 a.setQuestionId(id);
                 a.setSurveyId(body.getSurveyId());
                 a.setVersionId(body.getVersionId());
                 
                 answers.add(a);
                 
                 i++;
             }
         }
         
         return body;
     }
     
     // -------------------------- Converter and setters ------------------------
 
     public ResponseController() {
         super(JResponse.class);
     }
     
     @Override
     public void convertDomain(DResponse from, JResponse to) {
         convertLongEntity(from, to);
         
         to.setState(from.getState());
         to.setSurveyId(null != from.getSurvey() ? from.getSurvey().getId() : null);
         to.setVersionId(null != from.getVersion() ? from.getVersion().getId() : null);
         to.setExtLocationId(from.getExtLocationId());
         to.setExtMeetingId(from.getExtMeetingId());
         to.setExtProductId(from.getExtProductId());
         to.setExtUserId(from.getExtUserId());
     }
 
     @Override
     public void convertJson(JResponse from, DResponse to) {
         convertJLong(from, to);
 
         to.setState(from.getState());
         if (null != from.getSurveyId()) {
             final DSurvey survey = new DSurvey();
             survey.setId(from.getSurveyId());
             to.setSurvey(survey);
         }
         if (null != from.getVersionId()) {
             final DVersion version = new DVersion();
             version.setId(from.getVersionId());
             to.setVersion(version);
         }
         to.setExtLocationId(from.getExtLocationId());
         to.setExtMeetingId(from.getExtMeetingId());
         to.setExtProductId(from.getExtProductId());
         to.setExtUserId(from.getExtUserId());
     }
 
     @Autowired
     public void setResponseService(ResponseService responseService) {
         this.service = responseService;
     }
 
     @Autowired
     public void setAnswerController(AnswerController answerController) {
         this.answerController = answerController;
     }
 
     @Autowired
     public void setSurveyService(SurveyService surveyService) {
         this.surveyService = surveyService;
     }
 }
