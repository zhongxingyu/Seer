 package net.meneame.fisgodroid;
 
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.xml.sax.XMLReader;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.text.Html;
 import android.text.Spannable;
 import android.util.DisplayMetrics;
 import android.util.TypedValue;
 import android.widget.TextView;
 
 public class Smileys
 {
     private static final Map<String, Smiley> msSmileys = new HashMap<String, Smiley>();
     private static final Map<Smiley, AnimatedGifDrawable> msDrawables = new HashMap<Smiley, AnimatedGifDrawable>();
 
     private static void addSmiley(Smiley smiley)
     {
         msSmileys.put(smiley.getChatText(), smiley);
     }
 
     private static void initializeResources()
     {
         // Initialize the resource map
         if ( msSmileys.size() == 0 )
         {
             addSmiley(new Smiley(">:(", "angry", R.drawable.angry));
             addSmiley(new Smiley(":|", "blank", R.drawable.blank));
             addSmiley(new Smiley(":>", "cheesy", R.drawable.cheesy));
             addSmiley(new Smiley(":S", "confused", R.drawable.confused));
             addSmiley(new Smiley("8-)", "cool", R.drawable.cool));
             addSmiley(new Smiley(":'(", "cry", R.drawable.cry));
             addSmiley(new Smiley(":$", "oops", R.drawable.embarassed));
             addSmiley(new Smiley(":ffu:", "ffu", R.drawable.fu));
             addSmiley(new Smiley(":goatse:", "goatse", R.drawable.goat));
             addSmiley(new Smiley(":D", "grin", R.drawable.grin));
             addSmiley(new Smiley(":hug:", "hug", R.drawable.hug));
             addSmiley(new Smiley("?(", "huh", R.drawable.huh));
             addSmiley(new Smiley(":*", "kiss", R.drawable.kiss));
             addSmiley(new Smiley("xD", "lol", R.drawable.laugh));
             addSmiley(new Smiley(":X", "lipssealed", R.drawable.lipsrsealed));
             addSmiley(new Smiley(":palm:", "palm", R.drawable.palm));
             addSmiley(new Smiley(":roll:", "roll", R.drawable.rolleyes));
             addSmiley(new Smiley(":(", "sad", R.drawable.sad));
             addSmiley(new Smiley("", "shame", R.drawable.shame));
             addSmiley(new Smiley(":shit:", "shit", R.drawable.shit));
             addSmiley(new Smiley(":O", "shocked", R.drawable.shocked));
             addSmiley(new Smiley(":)", "smiley", R.drawable.smiley));
             addSmiley(new Smiley(":P", "tongue", R.drawable.tongue));
             addSmiley(new Smiley(":troll:", "troll", R.drawable.trollface2));
             addSmiley(new Smiley(":/", "undecided", R.drawable.undecided));
             addSmiley(new Smiley(":wall:", "wall", R.drawable.wall));
             addSmiley(new Smiley(";)", "wink", R.drawable.wink));
             addSmiley(new Smiley(":wow:", "wow", R.drawable.wow));
         }
     }
 
     public static Collection<Smiley> getSmileys()
     {
         initializeResources();
         return msSmileys.values();
     }
 
     public static Smiley getSmiley(String tag)
     {
         if ( tag.startsWith("{") && tag.endsWith("}") )
         {
             tag = tag.substring(1, tag.length() - 1);
         }
         return msSmileys.get(tag);
     }
 
     public static AnimatedGifDrawable getAnimatedDrawable(Context context, Smiley smiley)
     {
         AnimatedGifDrawable drawable = msDrawables.get(smiley);
         if ( drawable == null )
         {
             int desiredSize = context.getResources().getInteger(R.integer.smiley_size);
 
             DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
             int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, desiredSize, metrics);
 
             InputStream is = context.getResources().openRawResource(smiley.getResource());
             drawable = new AnimatedGifDrawable(context, is, height / (float) desiredSize);
             msDrawables.put(smiley, drawable);
         }
         return drawable;
     }
 
     public static String parseMessage(String message)
     {
         initializeResources();
 
         // Search smileys in the string and replace them by an <img> tag
         StringBuilder builder = new StringBuilder();
         int pos = 0;
         int cur;
 
         while ((cur = message.indexOf("{", pos)) > -1)
         {
             int end = message.indexOf("}", cur);
             if ( end == -1 )
             {
                 builder.append(message.substring(pos, cur + 1));
                 pos = cur + 1;
             }
             else
             {
                 String smileyName = message.substring(cur + 1, end);
                 if ( msSmileys.containsKey(smileyName) == false )
                 {
                     builder.append(message.substring(pos, end + 1));
                     pos = end + 1;
                 }
                 else
                 {
                     builder.append(message.substring(pos, cur));
                     builder.append("<smiley>{" + smileyName + "}</smiley>");
                     pos = end + 1;
                 }
             }
         }
         builder.append(message.substring(pos));
        return "<body>" + builder.toString() + "</body>";
     }
 
     public static Html.TagHandler getTagHandler(final Context context, final TextView ownerView)
     {
         initializeResources();
 
         return new Html.TagHandler()
         {
             @Override
             public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader)
             {
                 if ( tag.equals("smiley") )
                 {
                     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                     boolean animated = prefs.getBoolean("enable_animated_smileys", false);
 
                     int len = output.length();
 
                     if ( opening )
                     {
                         SmileySpan span = new SmileySpan(animated == false ? null : new SmileySpan.Listener()
                         {
                             @Override
                             public void update()
                             {
                                 ownerView.setText(ownerView.getText());
                                 ownerView.postInvalidate();
                             }
                         });
                         output.setSpan(span, len, len, Spannable.SPAN_MARK_MARK);
                     }
                     else
                     {
                         SmileySpan span = (SmileySpan) getLast(output, SmileySpan.class);
                         if ( span != null )
                         {
                             int pos = output.getSpanStart(span);
                             output.removeSpan(span);
 
                             String smileyText = output.subSequence(pos, len).toString();
                             Smiley smiley = getSmiley(smileyText);
 
                             if ( pos != len && smiley != null )
                             {
                                 AnimatedGifDrawable drawable = getAnimatedDrawable(context.getApplicationContext(), smiley);
                                 if ( !animated )
                                 {
                                     drawable.rewind();
                                 }
                                 span.setDrawable(drawable);
                                 output.setSpan(span, pos, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                             }
                         }
                     }
                 }
             }
 
             private Object getLast(Editable text, Class kind)
             {
                 @SuppressWarnings("unchecked")
                 Object[] objs = text.getSpans(0, text.length(), kind);
 
                 if ( objs.length == 0 )
                 {
                     return null;
                 }
                 else
                 {
                     for (int i = objs.length; i > 0; i--)
                     {
                         if ( text.getSpanFlags(objs[i - 1]) == Spannable.SPAN_MARK_MARK )
                         {
                             return objs[i - 1];
                         }
                     }
                     return null;
                 }
             }
         };
     }
 }
