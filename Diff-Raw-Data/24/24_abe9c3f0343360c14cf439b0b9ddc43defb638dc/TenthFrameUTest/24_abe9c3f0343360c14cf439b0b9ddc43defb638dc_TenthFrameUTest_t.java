 package tenpin.frame;
 
 import junit.framework.Assert;
 import org.junit.Test;
 
 public class TenthFrameUTest {
 
     @Test
     public void testSubmitBallScoreForTwoBalls() throws Exception {
         Frame frame = new TenthFrame();
         frame.submitBallScore(0);
         Assert.assertNull(frame.getFrameScore().getTotalScore());
         frame.submitBallScore(1);
         Integer expected = 1;
         Assert.assertEquals(expected, frame.getFrameScore().getTotalScore());
 
         frame = new TenthFrame();
         frame.submitBallScore(9);
         Assert.assertNull(frame.getFrameScore().getTotalScore());
         frame.submitBallScore(0);
         expected = 9;
         Assert.assertEquals(expected, frame.getFrameScore().getTotalScore());
     }
 
     @Test
     public void testSubmitBallScoreForThreeBalls() throws Exception {
         Frame frame = new TenthFrame();
         frame.submitBallScore(0);
         Assert.assertNull(frame.getFrameScore().getTotalScore());
         frame.submitBallScore(10);
         Assert.assertNull(frame.getFrameScore().getTotalScore());
         frame.submitBallScore(0);
         Assert.assertTrue(frame.getFrameScore().isASpare());
 
         frame = new TenthFrame();
         frame.submitBallScore(5);
         Assert.assertNull(frame.getFrameScore().getTotalScore());
         frame.submitBallScore(5);
         Assert.assertNull(frame.getFrameScore().getTotalScore());
         frame.submitBallScore(0);
         Assert.assertTrue(frame.getFrameScore().isASpare());
 
         frame = new TenthFrame();
         frame.submitBallScore(5);
         Assert.assertNull(frame.getFrameScore().getTotalScore());
         frame.submitBallScore(5);
         Assert.assertNull(frame.getFrameScore().getTotalScore());
         frame.submitBallScore(1);
         Integer expected = 11;
         Assert.assertEquals(expected, frame.getFrameScore().getTotalScore());
 
         frame = new TenthFrame();
         frame.submitBallScore(10);
         frame.submitBallScore(10);
         frame.submitBallScore(10);
         expected = 30;
         Assert.assertEquals(expected, frame.getFrameScore().getTotalScore());
 
         frame = new TenthFrame();
         frame.submitBallScore(0);
         frame.submitBallScore(10);
         frame.submitBallScore(10);
         expected = 20;
         Assert.assertEquals(expected, frame.getFrameScore().getTotalScore());
 
         frame = new TenthFrame();
         frame.submitBallScore(0);
         frame.submitBallScore(10);
         frame.submitBallScore(1);
         expected = 11;
         Assert.assertEquals(expected, frame.getFrameScore().getTotalScore());
 
         frame = new TenthFrame();
         frame.submitBallScore(10);
         frame.submitBallScore(0);
        frame.submitBallScore(0);
         expected = 10;
         Assert.assertEquals(expected, frame.getFrameScore().getTotalScore());
 
         frame = new TenthFrame();
         frame.submitBallScore(10);
         frame.submitBallScore(0);
        frame.submitBallScore(1);
        expected = 11;
         Assert.assertEquals(expected, frame.getFrameScore().getTotalScore());
 
         frame = new TenthFrame();
         frame.submitBallScore(10);
         frame.submitBallScore(10);
         frame.submitBallScore(0);
         expected = 20;
         Assert.assertEquals(expected, frame.getFrameScore().getTotalScore());
 
         frame = new TenthFrame();
         frame.submitBallScore(10);
         frame.submitBallScore(10);
         frame.submitBallScore(1);
         expected = 21;
         Assert.assertEquals(expected, frame.getFrameScore().getTotalScore());
 
         frame = new TenthFrame();
         frame.submitBallScore(1);
         frame.submitBallScore(9);
         frame.submitBallScore(1);
         expected = 11;
         Assert.assertEquals(expected, frame.getFrameScore().getTotalScore());
 
     }
 
     @Test(expected = UnsupportedOperationException.class)
     public void testSubmitThirdBallWhenDoesntHaveOneWithLowScores() throws Exception {
         Frame frame = new TenthFrame();
         frame.submitBallScore(0);
         frame.submitBallScore(1);
         frame.submitBallScore(0);
     }
 
     @Test
     public void isFrameInPlay() {
         Frame frame = new TenthFrame();
         frame.submitBallScore(1);
         Assert.assertTrue(frame.isFrameInPlay());
         frame.submitBallScore(1);
         Assert.assertFalse(frame.isFrameInPlay());
 
         frame = new TenthFrame();
         frame.submitBallScore(5);
         frame.submitBallScore(5);
         Assert.assertTrue(frame.isFrameInPlay());
 
         frame = new TenthFrame();
         frame.submitBallScore(10);
         frame.submitBallScore(0);
        Assert.assertTrue(frame.isFrameInPlay());
 
         frame = new TenthFrame();
         frame.submitBallScore(0);
         frame.submitBallScore(10);
         Assert.assertTrue(frame.isFrameInPlay());
     }
 
 }
