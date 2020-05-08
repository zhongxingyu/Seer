 package ro.undef.patois;
 
 import android.content.Context;
 import android.text.Spannable;
 import android.text.SpannableStringBuilder;
 import android.text.style.TextAppearanceSpan;
 
 
 public class ScoreRenderer {
     private Context mContext;
     private int mStyleGood;
     private int mStyleAverage;
     private int mStyleBad;
     private long mTimeNow;
 
     public ScoreRenderer(Context context, int styleGood, int styleAverage, int styleBad) {
         mContext = context;
         mStyleGood = styleGood;
         mStyleAverage = styleAverage;
         mStyleBad = styleBad;
         mTimeNow = System.currentTimeMillis() / 1000;
     }
 
    public Spannable renderScore(Trainer.PracticeInfo info) {
         SpannableStringBuilder ssb = new SpannableStringBuilder();
         renderStarsTo(ssb, info.level, info.nextPractice);
         return ssb;
     }
 
     public Spannable renderScores(int levelFrom, long nextPracticeFrom,
                                   int levelTo, long nextPracticeTo) {
         SpannableStringBuilder ssb = new SpannableStringBuilder();
 
         renderStarsTo(ssb, levelFrom, nextPracticeFrom);
         ssb.append('\n');
         renderStarsTo(ssb, levelTo, nextPracticeTo);
 
         return ssb;
     }
 
     private Spannable renderStarsTo(SpannableStringBuilder ssb,
                                     int level, long nextPractice) {
         final int MAX_STARS = 4;
         int numStars = Trainer.getNumStars(level, MAX_STARS);
         int start = ssb.length();
 
         if (numStars > 0) {
             for (int i = 0; i < numStars; i++)
                 ssb.append('\u2605');   // A full star.
         } else {
             ssb.append('\u2666');   // A full diamond.
         }
 
         final long ONE_WEEK = 7 * 24 * 60 * 60;
 
         int style = mStyleBad;
         if (nextPractice >= mTimeNow)
             style = mStyleGood;
         else if (nextPractice >= mTimeNow - ONE_WEEK)
             style = mStyleAverage;
 
         ssb.setSpan(new TextAppearanceSpan(mContext, style), start, ssb.length(), 0);
 
         return ssb;
     }
 }
