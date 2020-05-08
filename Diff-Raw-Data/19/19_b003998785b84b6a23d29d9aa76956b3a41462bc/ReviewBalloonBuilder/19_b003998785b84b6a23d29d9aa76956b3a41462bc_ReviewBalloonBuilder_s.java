 package ui;
 
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.editor.colors.EditorColors;
 import com.intellij.openapi.editor.colors.EditorColorsManager;
 import com.intellij.openapi.editor.markup.HighlighterLayer;
 import com.intellij.openapi.editor.markup.HighlighterTargetArea;
 import com.intellij.openapi.editor.markup.RangeHighlighter;
 import com.intellij.openapi.editor.markup.TextAttributes;
 import com.intellij.openapi.ui.popup.*;
 import com.intellij.ui.awt.RelativePoint;
 import content.Review;
 import org.jetbrains.annotations.NotNull;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * User: ktisha
  *
  */
 public class ReviewBalloonBuilder {
 
   private static final TextAttributes ourReviewAttributes =
     EditorColorsManager.getInstance().getGlobalScheme().getAttributes(EditorColors.WRITE_SEARCH_RESULT_ATTRIBUTES);
 
   public ReviewBalloonBuilder() {
   }
 
  private Balloon createBalloon(@NotNull final Review review, @NotNull final Editor editor,
                                final JComponent balloonContent, final String title) {
 
     final RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(review.getStartOffset(),
                                                                                      review.getEndOffset(),
                                                                                      HighlighterLayer.SELECTION,
                                                                                      ourReviewAttributes,
                                                                                      HighlighterTargetArea.EXACT_RANGE);
 
     final BalloonBuilder balloonBuilder = JBPopupFactory.getInstance().createDialogBalloonBuilder(balloonContent, title);
     balloonBuilder.setHideOnClickOutside(true);
     balloonBuilder.setHideOnKeyOutside(true);
     Balloon balloon = balloonBuilder.createBalloon();
 
     balloon.addListener(new JBPopupAdapter() {
       @Override
       public void onClosed(LightweightWindowEvent event) {
         if (highlighter.isValid()) {
           highlighter.dispose();
         }
       }
     });
    return balloon;
  }

  public void showBalloon(@NotNull final Review review, @NotNull final Editor editor,
                          final JComponent balloonContent, final String title) {
    final Balloon balloon = createBalloon(review, editor, balloonContent, title);
     final Point targetPoint = editor.visualPositionToXY(editor.offsetToVisualPosition(review.getEndOffset()));
    balloon.show(new RelativePoint(balloonContent, targetPoint), Balloon.Position.below);
   }
 }
