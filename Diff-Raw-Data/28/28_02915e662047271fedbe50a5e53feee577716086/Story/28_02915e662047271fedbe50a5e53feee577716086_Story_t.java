 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2011, Janrain, Inc.
  *
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without modification,
  *  are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *  * Neither the name of the Janrain, Inc. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  */
 package com.janrain.android.quickshare;
 
 import android.util.Log;
 import com.janrain.android.engage.types.JRActivityObject;
import com.janrain.android.engage.types.JREmailObject;
 import com.janrain.android.engage.types.JRImageMediaObject;
import com.janrain.android.engage.types.JRSmsObject;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Story implements Serializable, Comparable<Story> {
     private static final String TAG = Story.class.getSimpleName();
 
     private String mTitle;
     private Date mRawDate;
     private String mFormattedDate;
     private String mPostedBy;
     private String mDescription;
     private String mPlainText;
     private String mLink;
     private ArrayList<String> mImageUrls;
 
     public static Story dummyStory() {
         return new Story();
     }
 
     public Story() {}
 
     public Story(String title, Date rawDate, String formattedDate, String postedBy, String description,
                  String plainText, String link, ArrayList<String> imageUrls) {
         mTitle = title;
         mRawDate= rawDate;
         mFormattedDate = formattedDate;
         mPostedBy = postedBy;
         mDescription = description;
         mPlainText = plainText;
         mLink = link;
         mImageUrls = imageUrls;
     }
 
     public String getTitle() {
         return mTitle;
     }
 
     public String getFormattedDate() {
         return mFormattedDate;
     }
 
     public String getDescription() {
         return mDescription;
     }
 
     private String newWidthAndHeight(String style) {
         Pattern patternWidth = Pattern.compile("(.*?)width:(.+?)px(.*)", Pattern.CASE_INSENSITIVE |
                 Pattern.DOTALL);
         Pattern patternHeight = Pattern.compile("(.*?)height:(.+?)px(.*)", Pattern.CASE_INSENSITIVE);
 
         Matcher matcherWidth = patternWidth.matcher(style);
         Matcher matcherHeight = patternHeight.matcher(style);
 
         //Log.d(TAG, "[newWidthAndHeight] matchers match style (" + style + ")?: " +
         //        (matcherWidth.matches() ? "width=yes and " : "width=no and ") +
         //        (matcherHeight.matches() ? "height=yes" : "height=no"));
 
         if (!matcherWidth.matches() || !matcherHeight.matches()) return style;
 
         Integer width;
         Integer height;
 
         try {
             width = new Integer(matcherWidth.group(2).trim());
             height = new Integer(matcherHeight.group(2).trim());
         } catch (NumberFormatException e) {
             return style;
         }
 
         if (width <= 280) return style;
 
         Double ratio = width / 280.0;
         Integer newHeight = (new Double(height / ratio)).intValue();
 
         //Log.d(TAG, "[newWidthAndHeight] style before: " + style);
         style = style.replace("width:" + matcherWidth.group(2) + "px", "width: 280px");
         style = style.replace("height:" + matcherHeight.group(2) + "px", "height: " + newHeight.toString() +
                 "px");
         //Log.d(TAG, "[newWidthAndHeight] style after: " + style);
 
         return style;
     }
 
     public String getPlainText() {
         return mPlainText;
     }
 
     public String getLink() {
         return mLink;
     }
 
     public ArrayList<String> getImageUrls() {
         return mImageUrls;
     }
 
     public String getThumbnailUrl() {
         if (mImageUrls.size() > 0) return mImageUrls.get(0);
         else return null;
     }
 
     public JRActivityObject toJRActivityObject() {
         JRActivityObject activityObject =
                 new JRActivityObject("shared an article from the Janrain Blog", getLink());
 
        activityObject.setTitle(mTitle);
        activityObject.setDescription(mPlainText);
         if (getImageUrls().size() > 0) {
             activityObject.addMedia(new JRImageMediaObject(getImageUrls().get(0), getImageUrls().get(0)));
         }
 
        JREmailObject jreo = new JREmailObject(mTitle,
                "I thought you might find this blog post interesting.\n\nFind the full article here: " +
                mLink + "\nLearn more about Janrain here: http://www.janrain.com\n\nHere's a preview:\n" +
                mPlainText.substring(0, Math.min(300, mPlainText.length())) + " ...");
        jreo.addUrl(mLink);
        jreo.addUrl("http://www.janrain.com");
        activityObject.setEmail(jreo);

        JRSmsObject jrso = new JRSmsObject("Check this out: " + mLink);
        jrso.addUrl(mLink);
        activityObject.setSms(jrso);

         return activityObject;
     }
 
     public String getPostedBy() {
         return mPostedBy;
     }
 
     public void setTitle(String title) {
         mTitle = title;
     }
 
     public void setLink(String link) {
         mLink = link;
     }
 
     public void setDescription(String description) {
         mDescription = description;
         scaleDescriptionImages();
     }
 
     private void scaleDescriptionImages() {
         String[] splitDescription = mDescription.split("<img ", -1);
 
         int length = splitDescription.length;
 
         String newDescription = splitDescription[0];
 
         for (int i = 1; i < length; i++) {
             //Log.d(TAG, "[scaleDescriptionImages] " + ((Integer) i).toString() + ": " + splitDescription[i]);
 
             try {
                 Pattern pattern = Pattern.compile("(.+?)style=\"(.+?)\"(.+?)/>(.+)", Pattern.CASE_INSENSITIVE
                         | Pattern.DOTALL);
                 Matcher matcher = pattern.matcher(splitDescription[i]);
 
                 //Log.d(TAG, "[scaleDescriptionImages] matcher matches?: " + (matcher.matches() ? "yes" :
                 //        "no"));
 
                 //for (int j = 1; j <= matcher.groupCount(); j++)
                 //    Log.d(TAG, "[scaleDescriptionImages] matched group " + ((Integer) j).toString() + ": " +
                 //            matcher.group(j));
 
                 newDescription += "<img " + matcher.group(1) +
                         "style=\"" + newWidthAndHeight(matcher.group(2)) + "\"" +
                         matcher.group(3) + "/>" + matcher.group(4);
             } catch (IllegalStateException e) {
                 Log.d(TAG, "[scaleDescriptionImages] exception: " + e);
                 newDescription += "<img " + splitDescription[i];
             }
         }
 
         //Log.d(TAG, "[scaleDescriptionImages] newDescription: " + newDescription);
 
         mDescription = newDescription;
     }
 
     public void setFormattedDate(String date) {
         mFormattedDate = date;
     }
 
     public void setCreator(String creator) {
         mPostedBy = creator;
     }
 
     public Story copy() {
         return new Story(mTitle, mRawDate, mFormattedDate, mPostedBy, mDescription, mPlainText, mLink,
                 mImageUrls);
     }
 
     public void setPlainText(String plainText) {
         mPlainText = plainText;
     }
 
     public void setImageUrls(ArrayList<String> imageUrls) {
         mImageUrls = imageUrls;
 
         if (getThumbnailUrl() != null) {
             FeedData.getInstance().getImageLoader().prefetch(getThumbnailUrl());
         }
     }
 
     public int compareTo(Story another) {
         if (another == null) return 1;
         Date anotherDate = another.getRawDate();
         if (anotherDate == null) return 1;
         if (mRawDate == null) return -1;
         // sort descending, most recent first
         return anotherDate.compareTo(mRawDate);
     }
 
     private Date getRawDate() {
         return mRawDate;
     }
 
     public void setRawDate(Date date) {
         mRawDate = date;
     }
 }
