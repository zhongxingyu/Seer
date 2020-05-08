 package org.sakaiproject.contentreview2.compatibility;
 
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.contentreview.exception.QueueException;
 import org.sakaiproject.contentreview.exception.ReportException;
 import org.sakaiproject.contentreview.exception.SubmissionException;
 import org.sakaiproject.contentreview.exception.TransientSubmissionException;
 import org.sakaiproject.contentreview.model.ContentReviewItem;
 import org.sakaiproject.contentreview.service.ContentReviewService;
 import org.sakaiproject.contentreview2.Review;
 import org.sakaiproject.contentreview2.ReviewService;
 import org.sakaiproject.site.api.Site;
 
 import java.util.*;
 
 /**
  * Implementation of the previous ContentReview API which delegates calls to the new API.
  */
 public class BackwardContentReviewService implements ContentReviewService {
     private static final long STATUS_QUEUED = 1L;
     private static final long STATUS_SUBMITTED = 2L;
     private static final long STATUS_RETRIEVED = 3L;
     private static final long STATUS_FAILED = 8L;
     private ReviewService reviewService;
 
     private static List<ContentReviewItem> convertReviews(Collection<Review> reviews) {
         List<ContentReviewItem> contentReviewItems = new ArrayList<ContentReviewItem>(reviews.size());
 
         for (Review review : reviews) {
             contentReviewItems.add(convertReview(review));
         }
 
         return Collections.unmodifiableList(contentReviewItems);
     }
 
     private static ContentReviewItem convertReview(Review review) {
         ContentReviewItem contentReviewItem = new ContentReviewItem();
         contentReviewItem.setContentId(review.getSubmissionId());
         contentReviewItem.setUserId(review.getUserId());
         contentReviewItem.setTaskId(review.getAssignmentId());
         contentReviewItem.setReviewScore(review.getScore());
         return contentReviewItem;
     }
 
     @Override
     public void queueContent(String userId, String siteId, String taskId, String contentId) throws QueueException {
         Review review = new Review(contentId, taskId, userId, siteId, Collections.singleton(contentId));
         reviewService.startReview(review);
     }
 
     @Override
     public int getReviewScore(String contentId) throws Exception {
         return reviewService.getReview(contentId).getScore();
     }
 
     /**
      * {@inheritDoc}
      * <p>
      * Always throws an {@link UnsupportedOperationException}.
      * </p>
      *
      * @deprecated use {@link #getReviewReportInstructor(String)} or {@link #getReviewReportStudent(String)} instead.
      */
     @Deprecated
     @Override
     public String getReviewReport(String contentId) throws QueueException, ReportException {
         throw new UnsupportedOperationException("Deprecated method that should be replaced.");
     }
 
     @Override
    public String getReviewReportStudent(String s) throws QueueException, ReportException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
    public String getReviewReportInstructor(String s) throws QueueException, ReportException {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public Long getReviewStatus(String contentId) throws QueueException {
         switch (reviewService.getReview(contentId).getStatus()) {
             case QUEUED:
                 return STATUS_QUEUED;
             case SUBMITTED:
                 return STATUS_SUBMITTED;
             case RETRIEVED:
                 return STATUS_RETRIEVED;
             case FAILED:
             default:
                 return STATUS_FAILED;
         }
     }
 
     /**
      * {@inheritDoc}
      * <p>
      * Always throws an {@link UnsupportedOperationException}.
      * </p>
      *
      * @deprecated There is no reason to rely on this method, there shouldn't be a notion of queuing system externally.
      */
     @Deprecated
     @Override
     public Date getDateQueued(String contextId) throws QueueException {
         throw new UnsupportedOperationException();
     }
 
     /**
      * {@inheritDoc}
      * <p>
      * Always throws an {@link UnsupportedOperationException}.
      * </p>
      *
      * @deprecated There is no reason to rely on this method, there shouldn't be a notion of external submission.
      */
     @Deprecated
     @Override
     public Date getDateSubmitted(String contextId) throws QueueException, SubmissionException {
         throw new UnsupportedOperationException();
     }
 
     /**
      * {@inheritDoc}
      * <p>
      * Always throws an {@link UnsupportedOperationException}.
      * </p>
      *
      * @deprecated There is no reason to call that externally. The queuing system is specific to the implementation.
      *             If a job (cron) needs to be run, it should be created/provided by the implementation.
      */
     @Deprecated
     @Override
     public void processQueue() {
         throw new UnsupportedOperationException();
     }
 
     /**
      * {@inheritDoc}
      * <p>
      * Always throws an {@link UnsupportedOperationException}.
      * </p>
      *
      * @deprecated There is no reason to call that externally. The report system is specific to the implementation.
      *             If a job (cron) needs to be run, it should be created/provided by the implementation.
      */
     @Deprecated
     @Override
     public void checkForReports() {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public List<ContentReviewItem> getReportList(String siteId, String taskId)
             throws QueueException, SubmissionException, ReportException {
         Collection<Review> reviewsForAssignment = new LinkedList<Review>();
         for (Review review : reviewService.getReviewsForAssignment(taskId)) {
             if (review.getStatus().equals(Review.Status.RETRIEVED))
                 reviewsForAssignment.add(review);
         }
         return convertReviews(reviewsForAssignment);
     }
 
     /**
      * {@inheritDoc}
      * <p>
      * Always throws an {@link UnsupportedOperationException}.
      * </p>
      *
      * @deprecated There shouldn't be any reason to get reports by site.
      *             Instead use {@link #getReportList(String, String)}.
      */
     @Deprecated
     @Override
     public List<ContentReviewItem> getReportList(String siteId)
             throws QueueException, SubmissionException, ReportException {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public List<ContentReviewItem> getAllContentReviewItems(String siteId, String taskId)
             throws QueueException, SubmissionException, ReportException {
         return convertReviews(reviewService.getReviewsForAssignment(taskId));
     }
 
     @Override
     public String getServiceName() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /**
      * {@inheritDoc}
      * <p>
      * Always throws an {@link UnsupportedOperationException}.
      * </p>
      *
      * @deprecated There shouldn't be any reason to call this method.
      */
     @Deprecated
     @Override
     public void resetUserDetailsLockedItems(String userId) {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public boolean isAcceptableContent(ContentResource contentResource) {
         return reviewService.isFileSupported(contentResource.getId());
     }
 
     @Override
     public boolean isSiteAcceptable(Site site) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
    public String getIconUrlforScore(Long aLong) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean allowResubmission() {
         return reviewService.isResubmissionSupported();
     }
 
     /**
      * {@inheritDoc}
      * <p>
      * Always throws an {@link UnsupportedOperationException}.
      * </p>
      *
      * @deprecated There shouldn't be any reason to call this method.
      */
     @Deprecated
     @Override
     public void removeFromQueue(String contentId) {
         throw new UnsupportedOperationException();
     }
 
     @Override
    public String getLocalizedStatusMessage(String s, String s2) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
    public String getLocalizedStatusMessage(String s) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
    public String getLocalizedStatusMessage(String s, Locale locale) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public Map<Object, Object> getAssignment(String siteId, String taskId)
             throws SubmissionException, TransientSubmissionException {
         //TODO: rely on reviewService.executeOnReviewSystem();
         throw new UnsupportedOperationException();
     }
 
     @Override
    public void createAssignment(String s, String s2, Map map)
             throws SubmissionException, TransientSubmissionException {
         //TODO: rely on reviewService.executeOnReviewSystem();
         throw new UnsupportedOperationException();
     }
 
     public void setReviewService(ReviewService reviewService) {
         this.reviewService = reviewService;
     }
 }
