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
 
 import java.io.FileInputStream;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.content.Context;
 import android.widget.RemoteViews;
 
 public class ReportParser {
     @SuppressWarnings("serial") public static class ServiceUnavailableException extends Exception {};
     @SuppressWarnings("serial") public static class ParsingError extends Exception {
         public ParsingError(String s) { super(s); }
     }
 
     public static String[] subscribers(int appWidgetId) throws ParsingError, ServiceUnavailableException {
         Document doc;
         try {
             FileInputStream inputStream = MyApp.getContext().openFileInput(""+appWidgetId);
             doc = Jsoup.parse(inputStream, "UTF-8", "");
         } catch (Exception e) {
             throw new Error(e);
         }
 
         if (!doc.select("form#loginInfo").isEmpty()) {
             throw new ServiceUnavailableException();
         }
 
         if (!doc.select("div:contains(There is a problem with your telusmobility.com account.)").isEmpty()) {
             throw new ServiceUnavailableException();
         }
 
         Elements numListSec = doc.select("div.numListSec");
         if (numListSec.isEmpty()) {
             throw new ParsingError("Not an usage report page");
         }
 
         Elements options = numListSec.select("option");
         if (options.isEmpty()) { return null; }
 
         String[] subscribers = new String[options.size()];
         int i = 0;
         for (Element o : options) {
             subscribers[i++] = o.text();
         }
         return subscribers;
     }
 
     public static RemoteViews buildView(int appWidgetId) throws ServiceUnavailableException, ParsingError {
         Document doc;
         try {
             FileInputStream inputStream = MyApp.getContext().openFileInput(""+appWidgetId);
             doc = Jsoup.parse(inputStream, "UTF-8", "");
         } catch (Exception e) {
             throw new Error(e);
         }
 
         List<String> sectionLabels = getSectionLabels(doc);
 
         if (!doc.select("form#loginInfo").isEmpty()) {
             throw new ServiceUnavailableException();
         }
 
         if (!doc.select("div:contains(There is a problem with your telusmobility.com account.)").isEmpty()) {
             throw new ServiceUnavailableException();
         }
 
         if (sectionLabels.equals(Arrays.asList(new String[] {"Account summary"}))) {
             return callingCardView(doc);
         } else if (sectionLabels.equals(Arrays.asList(new String[] {"Usage Summary", "Airtime Usage", "Data Usage", "Text Usage", "Billing"}))) {
             return smartPhoneView(doc);
        } else if (sectionLabels.equals(Arrays.asList(new String[] {"Bill Details", "Airtime Usage", "Data Usage", "Text Usage"}))) {
            return smartPhoneView(doc);
         } else {
             throw new ParsingError("Unknown sections");
         }
     }
 
     private static List<String> getSectionLabels(Document doc) {
         List<String> list = new ArrayList<String>();
         Elements result = doc.select("b");
         for (Element e : result) {
             list.add(e.text());
         }
         return list;
     }
 
     private static RemoteViews callingCardView(Document doc) throws ParsingError {
         String currentBalance = getValue(doc, "Current Balance");
         Elements result = doc.select("td.labelValueLabel:contains(Balance Expires)");
         String text = result.text();
         if (result.isEmpty()) {
             throw new ParsingError("No Expires");
         }
         String balanceExpires = text.substring(16);
 
         // Build an update that holds the updated widget contents
         RemoteViews updateViews = new RemoteViews(MyApp.getContext().getPackageName(), R.layout.widget_calling_cards_layout);
         updateViews.setTextViewText(R.id.current_balance, currentBalance);
         updateViews.setTextViewText(R.id.balance_expires, balanceExpires);
         return updateViews;
     }
 
     private static RemoteViews smartPhoneView(Document doc) throws ParsingError {
         // Build an update that holds the updated widget contents
         Context context = MyApp.getContext();
         RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_smart_phone_layout);
 
         Element airtimeUsage = getSection(doc, "Airtime Usage");
         if (isAvailable(airtimeUsage)) {
             assertLabels(airtimeUsage, new String[] {"Included Minutes", "Remaining Minutes", "Chargeable Minutes"});
             String includedMinutes = getMinutesValue(airtimeUsage, "Included Minutes");
             String remainingMinutes = getMinutesValue(airtimeUsage, "Remaining Minutes");
             String chargeableMinutes = getMinutesValue(airtimeUsage, "Chargeable Minutes");
 
             String airtimeRemainingMinutes;
             if (includedMinutes.equals("unlimited")) {
                 airtimeRemainingMinutes = context.getString(R.string.unlimited);
             } else {
                 airtimeRemainingMinutes = context.getString(R.string.airtime_remaining);
                 airtimeRemainingMinutes = String.format(airtimeRemainingMinutes, remainingMinutes, includedMinutes);
             }
             updateViews.setTextViewText(R.id.airtime_remaining, airtimeRemainingMinutes);
 
             String airtimeChargeableMinutes= context.getString(R.string.airtime_chargeable);
             airtimeChargeableMinutes = String.format(airtimeChargeableMinutes, chargeableMinutes);
             updateViews.setTextViewText(R.id.airtime_chargeable, airtimeChargeableMinutes);
         } else {
             String airtimeRemainingMinutes = context.getString(R.string.airtime_remaining);
             airtimeRemainingMinutes = String.format(airtimeRemainingMinutes, "--", "--");
             updateViews.setTextViewText(R.id.airtime_remaining, airtimeRemainingMinutes);
 
             String airtimeChargeableMinutes= context.getString(R.string.airtime_chargeable);
             airtimeChargeableMinutes = String.format("--", "--");
             updateViews.setTextViewText(R.id.airtime_chargeable, airtimeChargeableMinutes);
         }
 
         Element dataUsage = getSection(doc, "Data Usage");
         if (isAvailable(dataUsage)) {
             assertLabels(dataUsage, new String[] {"Usage", "Amount"});
             String usage = getValue(dataUsage, "Usage");
             String amount = getValue(dataUsage, "Amount");
 
             usage = formatMbUsageForCompactness(usage);
 
             updateViews.setTextViewText(R.id.data, usage);
             updateViews.setTextViewText(R.id.data_amount, amount);
         }
 
         Element textUsage = getSection(doc, "Text Usage");
         if (isAvailable(textUsage)) {
             assertLabels(textUsage, new String[] {"Usage", "Amount"});
             String usage = getValue(textUsage, "Usage");
             String amount = getValue(textUsage, "Amount");
 
             if (!usage.matches("\\d+ Messages")) {
                 throw new ParsingError("Bad text format.");
             }
 
             usage = usage.replace("Messages", "Msg");
 
             updateViews.setTextViewText(R.id.text, usage);
             updateViews.setTextViewText(R.id.text_amount, amount);
         }
 
         return updateViews;
     }
 
     private static Element getSection(Document doc, String label) {
         return doc.select("div.indexPairSec:has(div.headingBB:contains("+label+"))").first();
     }
 
     private static boolean isAvailable(Element e) {
         return
           e.select(":contains(currently not available), :contains(currently unavailable").isEmpty();
     }
 
     private static void assertLabels(Element e, String[] labels) throws ParsingError {
         if (!getValueLabels(e).equals(Arrays.asList(labels))) {
             throw new ParsingError("Unknow values");
         }
     }
 
     private static List<String> getValueLabels(Element element) {
         List<String> list = new ArrayList<String>();
         Elements result = element.select("td.labelValueLabel");
         for (Element e : result) {
             list.add(e.text().replace("\u2022", "").trim());
         }
         return list;
     }
 
     private static String getMinutesValue(Element element, String label) throws ParsingError {
         // Note: Why can we get a list of values for the minutes?
         // I hope summing them up is the right thing to do.
         Elements result = element.select("td.labelValueLabel:contains("+label+") ~ td.labelValueValue");
         String text = result.text();
         if (result.html().matches("\\s*\\d+\\s*((<br />)\\s*\\d+\\s*)*")) {
             int sum = 0;
             String[] values = result.html().split("<br />");
             for (String v : values) { sum += Integer.parseInt(v); }
             return "" + sum;
         } else if (text.equals("unlimited") || text.equals("-") | text.equals("")) {
             return result.text();
         } else {
             throw new ParsingError("weird string");
         }
     }
 
     private static String getValue(Element element, String label) throws ParsingError {
         Elements result = element.select("td.labelValueLabel:contains("+label+") ~ td.labelValueValue");
         if (result.isEmpty())
             throw new ParsingError("Empty value");
         return result.text();
     }
 
     /**
      * Make the data usage string smaller by replacing " MB" by "M"
      * and rounding off some of the decimals
      * @throws ParsingError
      */
     private static String formatMbUsageForCompactness(String usage) throws ParsingError {
         DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
         symbols.setGroupingSeparator(' ');
         symbols.setDecimalSeparator('.');
         if (usage.matches("\\d+(\\.\\d+)? MB")) {
             // Round to 1 decimal place
             String s = usage.replace(" MB", "");
             double d = Double.parseDouble(s);
             return new DecimalFormat("#0.#", symbols).format(d) + "M";
         } else if (usage.matches("\\d+,\\d\\d\\d(\\.\\d+)? MB")) {
             // Round of 0 decimal places
             String s = usage.replace(",", "").replace(" MB", "");
             double d = Double.parseDouble(s);
             return new DecimalFormat("#,###", symbols).format(d) + "M";
         } else {
             throw new ParsingError("Bad MB format");
         }
     }
 }
