 package com.wadpam.rnr.web;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.wadpam.docrest.domain.RestCode;
 import com.wadpam.docrest.domain.RestReturn;
 import com.wadpam.open.analytics.google.GoogleAnalyticsTracker;
 import com.wadpam.open.analytics.google.GoogleAnalyticsTrackerBuilder;
 import com.wadpam.open.exceptions.BadRequestException;
 import com.wadpam.open.exceptions.NotFoundException;
 import com.wadpam.open.json.JCursorPage;
 import com.wadpam.open.web.AbstractRestController;
 import com.wadpam.rnr.domain.DQuestion;
 import com.wadpam.rnr.json.JQuestion;
 import com.wadpam.rnr.json.JRating;
 import com.wadpam.rnr.service.FeedbackService;
 import net.sf.mardao.core.CursorPage;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.view.RedirectView;
 import org.springframework.web.util.UriComponentsBuilder;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 
 /**
  * The question controller implements all REST methods related to question and answers.
  * @author mattiaslevin
  */
 @Controller
 @RequestMapping(value="{domain}")
 @RestReturn(JQuestion.class)
 public class QuestionController extends AbstractRestController {
     private static final Logger LOG = LoggerFactory.getLogger(AbstractRestController.class);
 
     // Error codes
     private static final int ERR_BASE = FeedbackService.ERR_BASE_QUESTION;
     private static final int ERR_BAD_REQUEST = ERR_BASE + 1;
     private static final int ERR_NOT_FOUND = ERR_BASE + 2;
 
     private static final Converter CONVERTER = new Converter();
 
     private FeedbackService feedbackService;
 
 
     /**
      * Create a question and assign to a list of users.
      * @param productId the product id related to the question
      * @param opUsername the username of the original poster
      * @param question the question
      * @param targetUsername users that should receive the question
      * @return
      */
     @RestReturn(value=JQuestion.class, entity=JQuestion.class, code={
             @RestCode(code=302, message="OK", description="Redirect to newly created question")
     })
     @RequestMapping(value="question", method= RequestMethod.POST)
     public RedirectView addQuestion(HttpServletRequest request,
                                     HttpServletResponse response,
                                     UriComponentsBuilder uriBuilder,
                                     @ModelAttribute("trackingCode") String trackingCode,
                                     @PathVariable String domain,
                                     @RequestParam String productId,
                                     @RequestParam String opUsername,
                                     @RequestParam String question,
                                     @RequestParam String[] targetUsername) {
 
         // Minimum one target useranme must be provided
         if (targetUsername.length < 1) {
             throw new BadRequestException(ERR_BAD_REQUEST, "Creating a question must contain minimum one target user");
         }
 
 
         // Create a tracker if tracking code is set
         GoogleAnalyticsTracker tracker = null;
         if (null != trackingCode) {
             LOG.debug("Create tracker with tracking code:{}", trackingCode);
             tracker = new GoogleAnalyticsTrackerBuilder()
                     .withNameAndTrackingCode(domain, trackingCode)
                     .withDeviceFromRequest(request)
                     .withVisitorId(opUsername != null ? opUsername.hashCode() : "anonymous".hashCode())
                     .build();
         }
 
         final DQuestion body = feedbackService.addQuestion(domain, productId, opUsername,
                 question, Arrays.asList(targetUsername), tracker);
 
         JQuestion jQuestion = CONVERTER.convert(body);
         return new RedirectView(uriBuilder.path("/{domain}/question/{id}")
                 .buildAndExpand(domain, jQuestion.getId()).toUriString());
 
     }
 
 
     /**
      * Get question based on the unique question id.
      * @param id the id of the question
      * @return a question
      */
     @RestReturn(value=JQuestion.class, entity=JQuestion.class, code={
             @RestCode(code=200, message="OK", description="Question found"),
             @RestCode(code=404, message="NOK", description="Question not found")
     })
     @RequestMapping(value="question/{id}", method= RequestMethod.GET)
     public ResponseEntity<JQuestion> getQuestion(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  @PathVariable String domain,
                                                  @PathVariable String id) {
 
         // The id will be a serialize datastore key, convert into a Key
         Key key = KeyFactory.stringToKey(id);
 
         final DQuestion body = feedbackService.getQuestion(key);
         if (null == body) {
             throw new NotFoundException(ERR_NOT_FOUND,
                     String.format("Question not found with id:%s", id));
         }
 
         return new ResponseEntity<JQuestion>(CONVERTER.convert(body), HttpStatus.OK);
     }
 
 
     /**
      * Get all questions assigned to user.
      *
      * Filter based on answer state and product id.
      * @param username get all questions assigned to this user
      * @param answerState optional. only return question matching this answer state
      *                    1 - unanswered
      *                    2 - answered
      *                    3 - both
      *                    Both will be returned as default
      * @param productId optional. Only check for questions for this product id
      * @return a list of questions
      */
     @RestReturn(value=Collection.class, entity=JQuestion.class, code={
            @RestCode(code=200, message="OK", description="Questions found"),
    })
     @RequestMapping(value="question", method= RequestMethod.GET, params="username")
     public ResponseEntity<Collection<JQuestion>> getAssignedQuestions(
             HttpServletRequest request,
             HttpServletResponse response,
             @PathVariable String domain,
             @RequestParam String username,
             @RequestParam(required=false, defaultValue="3") int answerState,
             @RequestParam(required=false) String productId) {
 
         // Check that the answer state is a valid number
         if (answerState < 1 || answerState > 3) {
             throw new BadRequestException(ERR_BAD_REQUEST, "Invalid answer state parameter");
         }
 
         final Iterable<DQuestion> dIterable =
                 feedbackService.getQuestionsAssignedToUser(username, answerState, productId);
 
         return new ResponseEntity<Collection<JQuestion>>((Collection<JQuestion>) CONVERTER.convert(dIterable),
                 HttpStatus.OK);
     }
 
 
     /**
      * Answer a question.
      *
      * The application need to get hold of the question id from one of the other
      * methods first.
      * @param id the unique id of the question
      * @param answer the users answer.
      *               The answer must be an int, e.g. -1 = NO, 1 = YES, 0 = DO N0T CARE
      *               It is up to the app to decide the logic and allowed values
      * @return
      */
     @RestReturn(value=JQuestion.class, entity=JQuestion.class, code={
             @RestCode(code=200, message="OK", description="Question found"),
             @RestCode(code=404, message="NOK", description="Question not found")
     })
     @RequestMapping(value="question/{id}", method= RequestMethod.POST, params="answer")
     public ResponseEntity<JQuestion> answerQuestion(HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     @PathVariable String domain,
                                                     @PathVariable String id,
                                                     @RequestParam int answer) {
 
         // The id will be a serialize datastore key, convert into a Key
         Key key = KeyFactory.stringToKey(id);
 
         final DQuestion body = feedbackService.answerQuestion(key, answer);
         if (null == body) {
             throw new NotFoundException(ERR_NOT_FOUND,
                     String.format("Question not found with id:%s", id));
         }
 
         return new ResponseEntity<JQuestion>(CONVERTER.convert(body), HttpStatus.OK);
     }
 
 
     /**
      * Find all questions asked by a user.
      *
      * Filter based in product id.
      * @param opUsername the person who asked the question
      * @param productId optional. only return questions matching the product id
      * @return list of questions
      */
     @RestReturn(value=Collection.class, entity=JQuestion.class, code={
            @RestCode(code=200, message="OK", description="Questions found"),
     })
     @RequestMapping(value="question", method=RequestMethod.GET, params="opUsername")
     public ResponseEntity<Collection<JQuestion>> getAskedQuestions(
             HttpServletRequest request,
             HttpServletResponse response,
             @PathVariable String domain,
             @RequestParam String opUsername,
             @RequestParam(required=false) String productId) {
 
         final Iterable<DQuestion> dIterable =
                 feedbackService.getQuestionsAskedByUser(opUsername, productId);
 
         return new ResponseEntity<Collection<JQuestion>>((Collection<JQuestion>) CONVERTER.convert(dIterable),
                 HttpStatus.OK);
     }
 
 
     /**
      * Get all answers to a specific question.
      *
      * Will also returned unanswered questions.
      *
      * This request will also return the original question. The original question
      * will have tbe opUsername parameter set, the answers will not.
      *
      * The question is must be obtains using one of the other methods.
      * @param id the unique id of the question.
      * @return the original question and all answers
      */
     @RestReturn(value=Collection.class, entity=JQuestion.class, code={
            @RestCode(code=200, message="OK", description="Questions found"),
     })
     @RequestMapping(value="question/{id}/answers", method=RequestMethod.GET)
     public ResponseEntity<Collection<JQuestion>> getAnwsersToQuestion(
             HttpServletRequest request,
             HttpServletResponse response,
             @PathVariable String domain,
             @PathVariable String id) {
 
         // The id will be a serialize datastore key, convert into a Key
         Key key = KeyFactory.stringToKey(id);
 
         // Get answers
         final Iterable<DQuestion> dIterableAnswers = feedbackService.getAnwsers(key);
 
         return new ResponseEntity<Collection<JQuestion>>(
                 (Collection<JQuestion>) CONVERTER.convertToAnswers(dIterableAnswers),
                 HttpStatus.OK);
     }
 
     /**
      * Delete a question.
      *
      * Will delete both the original question and any answers.
      * @return HTTP response code to indicate the outcome
      */
     @RestReturn(value=JQuestion.class, entity=JQuestion.class, code={
             @RestCode(code=200, message="OK", description="Question found"),
             @RestCode(code=404, message="NOK", description="Question not found")
     })
     @RequestMapping(value="question/{id}", method=RequestMethod.DELETE)
     public ResponseEntity<JQuestion> deleteQuestion(HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     @PathVariable String domain,
                                                     @PathVariable String id) {
 
         // The id will be a serialize datastore key, convert into a Key
         Key key = KeyFactory.stringToKey(id);
 
         final DQuestion body = feedbackService.deleteQuestion(key);
         if (null == body) {
             throw new NotFoundException(ERR_NOT_FOUND,
                     String.format("Question not found with id:%s", id));
         }
 
         return  new ResponseEntity<JQuestion>(HttpStatus.OK);
     }
 
 
     // Setters and getters
     public void setFeedbackService(FeedbackService feedbackService) {
         this.feedbackService = feedbackService;
     }
 }
