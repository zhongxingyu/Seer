 package com.reeltwo.jumble.ui;
 


 import com.reeltwo.jumble.fast.JumbleResult;
 import com.reeltwo.jumble.fast.MutationResult;
 import java.io.PrintStream;
 import java.util.List;
 
 /**
 * Prints the results of a Jumble run to a <code>PrintStream</code>, this
 * will usually be <code>System.out</code>.
  * 
  * @author Tin Pavlinic
  * @version $Revision$
  */
 public class JumbleScorePrinterListener implements JumbleListener {
   private static final int DOTS_PER_LINE = 50;
 
   private PrintStream mStream;
 
   private int mCovered = 0;
 
   private int mMutationCount;
 
   private String mClassName;
 
  private List < String > mTestNames;
 
   private boolean mInitialTestsPassed;
 
   private int mDotCount = 0;
 
   public JumbleScorePrinterListener() {
     this(System.out);
   }
 
   public JumbleScorePrinterListener(PrintStream output) {
     mStream = output;
   }
 
   public void jumbleRunEnded() {
     if (mInitialTestsPassed) {
       getStream().println();
       printResultsForNormalRun();
     }
   
  }
 
 
   public void finishedMutation(MutationResult res) {
     if (res.isPassed()) {
       getStream().print(".");
       mCovered++;
       newDot();
     } else if (res.isTimedOut()) {
       getStream().print("T");
       mCovered++;
       newDot();
     } else {
       getStream().println("M FAIL: " + res.getDescription());
       mDotCount = 0;
     }
   }
 
   private void newDot() {
     mDotCount++;
     if (mDotCount == DOTS_PER_LINE) {
       mDotCount = 0;
       getStream().println();
     }
   }
 
  public void jumbleRunStarted(String className, List < String > testClassNames) {
	mMutationCount = 0;
	mCovered = 0;
     mClassName = className;
     mTestNames = testClassNames;
   }
 
   public void performedInitialTest(JumbleResult result, int mutationCount) {
     mInitialTestsPassed = result.initialTestsPassed();
     mMutationCount = mutationCount;
     getStream().println("Mutating " + mClassName);
 
     if (result.isInterface()) {
       printResultsForInterface();
       return;
     }
 
     getStream().print("Tests:");
     for (int i = 0; i < mTestNames.size(); i++) {
       getStream().print(" " + mTestNames.get(i));
     }
     getStream().println();
 
     if (result.isMissingTestClass()) {
       getStream().println("Score: 0% (NO TEST CLASS)");
       getStream().println("Mutation points = " + mMutationCount);
       return;
     }
 
     if (!mInitialTestsPassed) {
       getStream().println("Score: 0% (TEST CLASS IS BROKEN)");
       getStream().println("Mutation points = " + mMutationCount);
       return;
     }
 
     getStream().print("Mutation points = " + mMutationCount);
     getStream().println(", unit test time limit " + (double) result.getTimeoutLength() / 1000 + "s");
   }
 
   protected void printResultsForNormalRun() {
     if (mMutationCount == 0) {
       getStream().println("Score: 100% (NO MUTATIONS POSSIBLE)");
     } else {
       getStream().println("Score: " + (mCovered) * 100 / mMutationCount + "%");
     }
   }
 
   protected void printResultsForInterface() {
     getStream().println("Score: 100% (INTERFACE)");
     getStream().println("Mutation points = 0");
   }
 
   public void error(String errorMsg) {
     getStream().println("ERROR: " + errorMsg);
   }
 
   public PrintStream getStream() {
     return mStream;
   }
 }
