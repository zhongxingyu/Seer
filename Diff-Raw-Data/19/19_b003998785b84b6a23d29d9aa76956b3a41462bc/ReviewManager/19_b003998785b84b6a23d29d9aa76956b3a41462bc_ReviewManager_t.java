 package actions;
 
 
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.project.DumbAware;
 import content.Review;
 import ui.ReviewBalloonBuilder;
import ui.forms.ReviewForm;
 import utils.ReviewBundle;
 
 import javax.swing.*;
 
 /**
  * User: ktisha
  */
 public class ReviewManager implements DumbAware {
   private static ReviewManager ourInstance;
 
   private ReviewManager() {
   }
 
   public static ReviewManager getInstance() {
     if (ourInstance == null) {
       ourInstance = new ReviewManager();
     }
     return ourInstance;
   }
 
   public void addReview(final Editor editor, int startOffset, int endOffset) {
     final Review review = new Review(startOffset, endOffset);
     final JComponent reviewForm = new ReviewForm(review, editor.getProject());
     final ReviewBalloonBuilder reviewBalloonBuilder = new ReviewBalloonBuilder();
     reviewBalloonBuilder.showBalloon(review, editor, reviewForm, ReviewBundle.message("review.addReview"));
     reviewForm.requestFocus();
   }
 }
