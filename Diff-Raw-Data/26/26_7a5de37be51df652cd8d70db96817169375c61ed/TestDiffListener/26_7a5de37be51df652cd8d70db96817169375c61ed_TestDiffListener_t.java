 package net.sf.clirr.core.internal.checks;
 
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import net.sf.clirr.core.internal.ApiDiffDispatcher;
 import net.sf.clirr.core.internal.checks.ExpectedDiff;
 import net.sf.clirr.core.ApiDifference;
 import net.sf.clirr.core.MessageTranslator;
 import junit.framework.TestCase;
 
 class TestDiffListener implements ApiDiffDispatcher
 {
         private MessageTranslator translator = new MessageTranslator();
 
         private Set diffs = new HashSet();
 
         public void fireDiff(ApiDifference difference)
         {
             diffs.add(difference);
         }
 
         public void checkExpected(ExpectedDiff[] expectedDiffs)
         {
             for (int i=0; i<expectedDiffs.length; ++i)
             {
                 ExpectedDiff expected = expectedDiffs[i];
 
                 // now see if the expected diff is in fact in the set of
                 // diffs that occurred during the test comparison
                 boolean found = false;
                 for(Iterator j = diffs.iterator(); j.hasNext() && !found;)
                 {
                     ApiDifference actual = (ApiDifference) j.next();
                     found = expected.matches(actual);
                 }
 
                 if (!found)
                 {
                     // build a useful failure message
                     MessageTranslator translator = new MessageTranslator();
 
                     StringBuffer buf = new StringBuffer();
                     buf.append("Expected diff " + expected + " was not generated.");
                     buf.append(" Actual diffs generated were: ");
                     for(Iterator diffIter = diffs.iterator(); diffIter.hasNext();)
                     {
                         ApiDifference diff = (ApiDifference) diffIter.next();
 
                         buf.append(diff.toString(translator));
                         if (diffIter.hasNext())
                         {
                             buf.append(", ");
                         }
                     }
 
                     TestCase.fail(buf.toString());
                 }
             }
 
            StringBuffer buf = null;
            for (Iterator it = diffs.iterator(); it.hasNext();) 
            {
                 ApiDifference actual = (ApiDifference) it.next();
 
                // see if the actual (generated) diff is in the expected set
                 boolean found = false;
                 for(int i=0; i<expectedDiffs.length && !found; ++i)
                 {
                     found = expectedDiffs[i].matches(actual);
                 }
 
                 if (!found)
                 {
                    if (buf == null)
                    {
                        buf = new StringBuffer();
                        buf.append("Unexpected diffs: ");
                    }
                    else
                    {
                        buf.append(", ");
                    }
                    buf.append(actual.toString(translator));
                 }
             }

            if (buf != null)
            {
                // we must have found at least one unexpected diff
                TestCase.fail(buf.toString());
            }
         }
 }
