 package jp.recruit.hps.movie.server.api;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.inject.Named;
 
 import jp.recruit.hps.movie.common.CommonConstant;
 import jp.recruit.hps.movie.server.api.container.StringListContainer;
 import jp.recruit.hps.movie.server.api.dto.InterviewV1Dto;
 import jp.recruit.hps.movie.server.api.dto.QuestionWithCountV1Dto;
 import jp.recruit.hps.movie.server.api.dto.ResultV1Dto;
 import jp.recruit.hps.movie.server.model.Company;
 import jp.recruit.hps.movie.server.model.Interview;
 import jp.recruit.hps.movie.server.model.InterviewQuestionMap;
 import jp.recruit.hps.movie.server.model.Question;
 import jp.recruit.hps.movie.server.model.User;
 import jp.recruit.hps.movie.server.service.CompanyService;
 import jp.recruit.hps.movie.server.service.InterviewQuestionMapService;
 import jp.recruit.hps.movie.server.service.InterviewService;
 import jp.recruit.hps.movie.server.service.QuestionService;
 import jp.recruit.hps.movie.server.service.UserService;
 
 import org.slim3.datastore.Datastore;
 
 import com.google.api.server.spi.config.Api;
 import com.google.appengine.api.datastore.Key;
 
 /**
  *
  */
 @Api(name = "interviewEndpoint", version = "v1")
 public class InterviewV1EndPoint {
     private final static Logger logger = Logger
         .getLogger(InterviewV1EndPoint.class.getName());
 
     private static final String SUCCESS = CommonConstant.SUCCESS;
     private static final String FAIL = CommonConstant.FAIL;
 
     public InterviewV1Dto getInterview(@Named("userKey") String userKey,
             @Named("companyKey") String companyKey) {
         InterviewV1Dto result = new InterviewV1Dto();
         List<QuestionWithCountV1Dto> resultList =
             new ArrayList<QuestionWithCountV1Dto>();
 
         Set<Interview> interviewSet = new HashSet<Interview>();
 
         Map<Key, QuestionWithCountV1Dto> questionMap =
             new HashMap<Key, QuestionWithCountV1Dto>();
         List<InterviewQuestionMap> interviewQuestionMapList =
             InterviewQuestionMapService
                 .getInterviewQuestionMapListByCompanyKey(Datastore
                     .stringToKey(companyKey));
 
         for (InterviewQuestionMap interviewQuestionMap : interviewQuestionMapList) {
             Question question =
                 interviewQuestionMap.getQuestionRef().getModel();
             if (questionMap.get(question.getKey()) == null) {
                 QuestionWithCountV1Dto dto = new QuestionWithCountV1Dto();
                 dto.setKey(Datastore.keyToString(question.getKey()));
                 dto.setName(question.getName());
                 questionMap.put(question.getKey(), dto);
             }
             interviewSet.add(interviewQuestionMap.getInterviewRef().getModel());
         }
         /* 質問数集計 */
         for (InterviewQuestionMap interviewQuestionMap : interviewQuestionMapList) {
             Question question =
                 interviewQuestionMap.getQuestionRef().getModel();
             QuestionWithCountV1Dto dto = questionMap.get(question.getKey());
             dto.setCount(dto.getCount() + 1);
         }
         for (InterviewQuestionMap interviewQuestionMap : interviewQuestionMapList) {
             Question question =
                 interviewQuestionMap.getQuestionRef().getModel();
             QuestionWithCountV1Dto dto = questionMap.get(question.getKey());
             dto.setPercent((double) dto.getCount()
                 / (double) interviewSet.size());
         }
         Collections.sort(resultList, new Comparator<QuestionWithCountV1Dto>() {
             public int compare(QuestionWithCountV1Dto o1,
                     QuestionWithCountV1Dto o2) {
                 return Integer.valueOf(o1.getCount()).compareTo(o2.getCount());
             }
         });
 
         resultList.addAll(questionMap.values());
         result.setQuestionList(resultList);
 
         return result;
     }
 
     public ResultV1Dto insertInterview(@Named("userKey") String userKey,
             @Named("companyKey") String companyKey,
             @Named("startTime") Long startTime) {
         ResultV1Dto result = new ResultV1Dto();
         User user = UserService.getUserByKey(Datastore.stringToKey(userKey));
         Company company =
             CompanyService.getCompany(Datastore.stringToKey(companyKey));
         try {
             if (user == null) {
                 logger.warning("user not found");
                 result.setResult(FAIL);
             } else if (company == null) {
                 logger.warning("company not found");
                 result.setResult(FAIL);
             } else {
                 InterviewService.createInterview(user, company, new Date(
                     startTime));
                 result.setResult(SUCCESS);
             }
         } catch (Exception e) {
             logger.warning("Exception" + e);
             result.setResult(FAIL);
         }
         return result;
     }
 
     public ResultV1Dto updateInterview(
             @Named("interviewKey") String interviewKey,
             @Named("startTime") Long startTime) {
         ResultV1Dto result = new ResultV1Dto();
         Interview interview =
             InterviewService.getInterview(Datastore.stringToKey(interviewKey));
         try {
             if (interview == null) {
                 logger.warning("interview not found");
                 result.setResult(FAIL);
             } else {
                 InterviewService
                     .updateInterview(interview, new Date(startTime));
                 result.setResult(SUCCESS);
             }
         } catch (Exception e) {
             logger.warning("Exception" + e);
             result.setResult(FAIL);
         }
         return result;
     }
 
     public ResultV1Dto updateInterviewQuestions(
             @Named("interviewKey") String interviewKey,
             StringListContainer questionKeyListContainer) {
         ResultV1Dto result = new ResultV1Dto();
         Interview interview =
             InterviewService.getInterview(Datastore.stringToKey(interviewKey));
 
         try {
             if (interview == null) {
                 logger.warning("interview not found");
                 result.setResult(FAIL);
             } else {
                 User user = interview.getUserRef().getModel();
                 List<Key> questionKeyList = new ArrayList<Key>();
                 for (String questionKey : questionKeyListContainer.getList()) {
                     questionKeyList.add(Datastore.stringToKey(questionKey));
                 }
                 List<Question> questionList =
                     QuestionService.getQuestionListByKeyList(questionKeyList);
                 for (Question question : questionList) {
                     InterviewQuestionMapService.createInterviewQuestionMap(
                         interview.getCompanyRef().getModel(),
                         question,
                         interview);
                 }
                 UserService.addPoint(user);
                 result.setResult(SUCCESS);
             }
         } catch (Exception e) {
             logger.warning("Exception" + e);
             result.setResult(FAIL);
         }
         return result;
     }
 }
