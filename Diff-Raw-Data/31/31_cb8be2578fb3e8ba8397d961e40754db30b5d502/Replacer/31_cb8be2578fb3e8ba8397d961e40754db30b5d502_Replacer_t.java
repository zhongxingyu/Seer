 package com.pate.diablo.string;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.os.Parcel;
 import android.text.Html;
 import android.text.SpannableStringBuilder;
 import android.text.TextUtils;
 import android.util.Log;
 
 // see http://stackoverflow.com/a/7365113
 public class Replacer {
     private final CharSequence mSource;
     private final String mColor;
     private final Matcher mMatcher;
     private int mAppendPosition;
 
     public static CharSequence replace(CharSequence source, String regex, String color) {
 
         Pattern pattern = Pattern.compile(regex);
         Matcher matcher = pattern.matcher(source);
         return new Replacer(source, matcher, color).doReplace();
     }
 
     private Replacer(CharSequence source, Matcher matcher, String color) {
         mSource = source;
         mColor = color;
         mMatcher = matcher;
         mAppendPosition = 0;
     }
 
     private CharSequence doReplace() {
         SpannableStringBuilder buffer = new SpannableStringBuilder();
         int i = 0;
         while (mMatcher.find()) {
            //Log.i("Match " + i, mMatcher.group());
             appendReplacement(buffer, mMatcher.group());
             i++;
         }
         SpannableStringBuilder retval = appendTail(buffer);
        //Log.i("Returning", retval.toString());
         return retval;
     }
 
     private void appendReplacement(SpannableStringBuilder buffer, String match) {
         buffer.append(mSource.subSequence(mAppendPosition, mMatcher.start()));
 
         String html = "<font color=\"" + mColor + "\">" + match + "</font>";
         buffer.append(Html.fromHtml(html));
        //Log.i("Buffer3", buffer.toString());
 
         mAppendPosition = mMatcher.end();
     }
 
     public SpannableStringBuilder appendTail(SpannableStringBuilder buffer) {
         buffer.append(mSource.subSequence(mAppendPosition, mSource.length()));
         return buffer;
     }
 
     // This is a weird way of copying spans, but I don't know any better way.
     private CharSequence copyCharSequenceWithSpans(CharSequence string) {
         Parcel parcel = Parcel.obtain();
         TextUtils.writeToParcel(string, parcel, 0);
         parcel.setDataPosition(0);
         return TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
     }
 }
