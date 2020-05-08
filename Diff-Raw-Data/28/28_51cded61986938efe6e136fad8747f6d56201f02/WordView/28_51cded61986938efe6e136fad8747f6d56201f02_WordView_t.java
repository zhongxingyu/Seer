 package org.apelikecoder.bgdictum;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.text.Layout;
 import android.text.Spannable;
 import android.text.SpannableString;
 import android.text.TextPaint;
 import android.text.method.LinkMovementMethod;
 import android.text.style.ClickableSpan;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
import android.view.ViewParent;
 import android.view.View.OnTouchListener;
import android.widget.ScrollView;
 import android.widget.TextView;
 
 public class WordView extends TextView implements OnTouchListener {
 
     private PopupView popup;
     private String infoWord;
    private ScrollView parent;
 
     private static class Type1Span extends ClickableSpan {
         String word;
         public Type1Span(String word) {
             this.word = word;
         }
         @Override
         public void updateDrawState(TextPaint ds) {
             super.updateDrawState(ds);
             ds.setUnderlineText(touchedInstance == this);
         }
         @Override
         public void onClick(View widget) {
             Uri uri = Uri.parse("bgdictum://" + word);
             Intent intent = new Intent(Intent.ACTION_VIEW, uri);
             intent.addCategory(Intent.CATEGORY_DEFAULT);
             widget.getContext().startActivity(intent);
         }
     };
 
     private static class Type2Span extends Type1Span {
         public Type2Span(String word) {
             super(word);
         }
         @Override
         public void updateDrawState(TextPaint ds) {
             super.updateDrawState(ds);
             ds.setTypeface(Typeface.DEFAULT_BOLD);
         }
     }
 
     private static Type1Span touchedInstance;
 
     public WordView(Context context) {
         super(context);
         init();
     }
 
     public WordView(Context context, AttributeSet attrs) {
         super(context, attrs);
         init();
     }
 
     public WordView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         init();
     }
 
     private void init() {
         setMovementMethod(LinkMovementMethod.getInstance());
         setOnTouchListener(this);
     }
 
     @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ViewParent p = getParent();
        if (p instanceof ScrollView)
            parent = (ScrollView) p;
    }

    @Override
     public boolean onTouchEvent(MotionEvent event) {
         return event.getAction() == MotionEvent.ACTION_DOWN ?
                 true : super.onTouchEvent(event);
     }
 
     public void setWordInfo(String word, String text) {
         infoWord = word;
         setText(infoWord.trim().toUpperCase() + "\n\n" + text);
     }
 
     @Override
     public void setText(CharSequence text, BufferType type) {
         Integer currentType = LinksFinder.getType(infoWord != null ? infoWord : ""); //XXX
         ArrayList<LinksFinder.LinkSpec> links = LinksFinder.getLinks(text.toString());
         if (links == null) {
             super.setText(text, type);
             return;
         }
         SpannableString ss = new SpannableString(text);
         int links_length = links.size();
         for (int i = 0; i < links_length; ++i) {
             LinksFinder.LinkSpec l = links.get(i);
             Type1Span span;
             if (currentType == l.type)
                 span = new Type2Span(l.url);
             else
                 span = new Type1Span(l.url);
             ss.setSpan(span, l.start, l.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
         }
         super.setText(ss, type);
     }
 
     public boolean onTouch(View v, MotionEvent event) {
         if (event.getAction() == MotionEvent.ACTION_DOWN
                 || event.getAction() == MotionEvent.ACTION_MOVE) {
 
             int x = (int) event.getX();
             int y = (int) event.getY();
 
             x -= getTotalPaddingLeft();
             y -= getTotalPaddingTop();
 
            int xx = getRealPosX(x);
            int yy = getRealPosY(y);
 
             x += getScrollX();
             y += getScrollY();
 
             Layout layout = getLayout();
             int line = layout.getLineForVertical(y);
             int off = layout.getOffsetForHorizontal(line, x);
 
             Type1Span[] candidates = ((Spannable) getText()).getSpans(off, off, Type1Span.class);
             if (candidates.length > 0) {
                 touchedInstance = candidates[0];
             } else {
                 touchedInstance = null;
             }
             if (popup != null && touchedInstance != null)
                 popup.setPopupText(xx, yy, touchedInstance.word);
         } else {
             popup.clear();
             if (touchedInstance != null) {
                 touchedInstance = null;
             }
         }
         return false;
     }
 
     public void setPopup(PopupView popup) {
         this.popup = popup;
     }
 
    private int getRealPosX(int val) {
        return parent == null ? val : val - parent.getScrollX();
    }

    private int getRealPosY(int val) {
        return parent == null ? val : val - parent.getScrollY();
    }

 }
