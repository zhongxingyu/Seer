 /*
  * Copyright (C) 2011 by Alexandre Jasmin <alexandre.jasmin@gmail.com>
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
 
 package com.github.ajasmin.telususageandroidwidget;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.content.Context;
 import android.widget.RemoteViews;
 
 public class CallingCardsPresenter extends DataPresenter {
 
     private static final HashMap<String, List<String>> requiredData;
     static {
         requiredData = new HashMap<String, List<String>>();
        requiredData.put("Account Activity", Arrays.asList(new String[] {"Current Balance", "Balance Expires"}));
     }
 
     @Override
     protected Map<String, List<String>> getRequiredData() {
         return requiredData;
     }
 
     @Override
     public RemoteViews buildUpdate(Context context, Map<String, Map<String, String>> data) {
         // Build an update that holds the updated widget contents
         RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_calling_cards_layout);
 
        Map<String, String> accountActivity = data.get("Account Activity");
         String currentBalance = accountActivity.get("Current Balance");
        String balanceExpires = accountActivity.get("Balance Expires");
 
         updateViews.setTextViewText(R.id.current_balance, currentBalance);
         updateViews.setTextViewText(R.id.balance_expires, balanceExpires);
 
         return updateViews;
     }
 }
