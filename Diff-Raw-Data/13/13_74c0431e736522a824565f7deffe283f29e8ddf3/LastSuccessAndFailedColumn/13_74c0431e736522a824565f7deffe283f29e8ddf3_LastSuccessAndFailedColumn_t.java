 /*
  * The MIT License
  * 
  * Copyright (c) 2009, Sun Microsystems, Inc., Jesse Glick
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.robestone.hudson.compactcolumns;
 
 import hudson.Extension;
 import hudson.Messages;
 import hudson.model.Run;
 import hudson.views.ListViewColumn;
 import hudson.views.ListViewColumnDescriptor;
 
 import java.math.BigDecimal;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 
 
 public class LastSuccessAndFailedColumn extends ListViewColumn {
 
 	// copied from hudson.Util because they were private
     private static final long ONE_SECOND_MS = 1000;
     private static final long ONE_MINUTE_MS = 60 * ONE_SECOND_MS;
     private static final long ONE_HOUR_MS = 60 * ONE_MINUTE_MS;
     static final long ONE_DAY_MS = 24 * ONE_HOUR_MS;
     private static final long ONE_MONTH_MS = 30 * ONE_DAY_MS;
     private static final long ONE_YEAR_MS = 365 * ONE_DAY_MS;
 
     @DataBoundConstructor
     public LastSuccessAndFailedColumn() {
     }

    public String getTimestamp(Run<?, ?> run) {
     	long now = System.currentTimeMillis();
     	long timestamp = run.getTimestamp().getTimeInMillis();
     	float diff = now - timestamp;
     	String stime = getShortTimestamp(diff);
     	return stime;
     }
     
     /**
      * Avoids having "2 days 3 hours" and instead does "2.1 days".
      * 
      * Additional strategy details:
      * < 1 sec = 0 sec
      * < 10 of anything = x.y of that (scale 1)
      * >= 10 of anything = x (scale 0)
      */
     protected String getShortTimestamp(float time) {
     	String ts;
     	float number;
     	if (time >= ONE_YEAR_MS) {
     		number = getRoundedNumber(time / ONE_YEAR_MS);
     		ts = Messages.Util_year(number);
     	} else if (time >= ONE_MONTH_MS) {
     		number = getRoundedNumber(time / ONE_MONTH_MS);
     		ts = Messages.Util_month(number);
     	} else if (time >= ONE_DAY_MS) {
     		number = getRoundedNumber(time / ONE_DAY_MS);
     		ts = Messages.Util_day(number);
     	} else if (time >= ONE_HOUR_MS) {
     		number = getRoundedNumber(time / ONE_HOUR_MS);
     		ts = Messages.Util_hour(number);
     	} else if (time >= ONE_MINUTE_MS) {
     		number = getRoundedNumber(time / ONE_MINUTE_MS);
     		ts = Messages.Util_minute(number);
     	} else if (time >= ONE_SECOND_MS) {
     		number = getRoundedNumber(time / ONE_SECOND_MS);
     		ts = Messages.Util_second(number);
     	} else {
         	ts = Messages.Util_second(0);
     	}
     	return ts;
     }
     
     protected float getRoundedNumber(float number) {
     	int scale;
     	if (number >= 10) {
     		scale = 0;
     	} else {
     		scale = 1;
     	}
     	return new BigDecimal(number).setScale(scale, BigDecimal.ROUND_HALF_DOWN).floatValue();
     }
 
     @Extension
     public static class DescriptorImpl extends ListViewColumnDescriptor {
         @Override
         public String getDisplayName() {
             return "Latest Builds (Compact)";
         }
 
         @Override
         public boolean shownByDefault() {
             return false;
         }
     }
 }
