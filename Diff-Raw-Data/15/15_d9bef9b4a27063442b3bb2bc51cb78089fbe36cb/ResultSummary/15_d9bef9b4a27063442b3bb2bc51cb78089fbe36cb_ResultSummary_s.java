 package hudson.plugins.cpptest;
 
 /**
  * Represents the result summary of the Cpptest parser. This summary will be
  * shown in the summary.jelly script of the Cpptest result action.
  *
  * @author Ulli Hafner
  * 
  * NQH: adapt for Cpptest
  */
 public final class ResultSummary {
     /**
      * Returns the message to show as the result summary.
      *
      * @param result
      *            the result
      * @return the message
      */
     public static String createSummary(final CpptestResult result) {
         StringBuilder summary = new StringBuilder();
         int bugs = result.getNumberOfAnnotations();
 
         summary.append("Code standard: ");
         if (bugs > 0) {
            summary.append("<a href=\"cpptestResult\">");
         }
         if (bugs == 1) {
             summary.append(Messages.Cpptest_ResultAction_OneWarning());
         }
         else {
             summary.append(Messages.Cpptest_ResultAction_MultipleWarnings(bugs));
         }
         if (bugs > 0) {
             summary.append("</a>");
         }
         summary.append(" ");
         if (result.getNumberOfModules() == 1) {
             summary.append(Messages.Cpptest_ResultAction_OneFile());
         }
         else {
             summary.append(Messages.Cpptest_ResultAction_MultipleFiles(result.getNumberOfModules()));
         }
         return summary.toString();
     }
 
     /**
      * Returns the message to show as the result summary.
      *
      * @param result
      *            the result
      * @return the message
      */
     public static String createDeltaMessage(final CpptestResult result) {
         StringBuilder summary = new StringBuilder();
         if (result.getNumberOfNewWarnings() > 0) {
            summary.append("<li><a href=\"cpptestResult/new\">");
             if (result.getNumberOfNewWarnings() == 1) {
                 summary.append(Messages.Cpptest_ResultAction_OneNewWarning());
             }
             else {
                 summary.append(Messages.Cpptest_ResultAction_MultipleNewWarnings(result.getNumberOfNewWarnings()));
             }
             summary.append("</a></li>");
         }
         if (result.getNumberOfFixedWarnings() > 0) {
             summary.append("<li><a href=\"CpptestResult/fixed\">");
             if (result.getNumberOfFixedWarnings() == 1) {
                 summary.append(Messages.Cpptest_ResultAction_OneFixedWarning());
             }
             else {
                 summary.append(Messages.Cpptest_ResultAction_MultipleFixedWarnings(result.getNumberOfFixedWarnings()));
             }
             summary.append("</a></li>");
         }
 
         return summary.toString();
     }
 
     /**
      * Instantiates a new result summary.
      */
     private ResultSummary() {
         // prevents instantiation
     }
 }
 
