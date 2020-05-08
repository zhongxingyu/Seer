 package nz.gen.geek_central.MemoryHog;
 /*
     Custom spinner showing digits 0 .. 9.
 
     Copyright 2011 by Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.
 
     Licensed under the Apache License, Version 2.0 (the "License"); you
     may not use this file except in compliance with the License. You may
     obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
     implied. See the License for the specific language governing
     permissions and limitations under the License.
 */
 
 public class DigitSpinner extends android.widget.RelativeLayout
   {
     protected android.content.Context Context;
     protected int CurDigit;
     protected android.widget.TextView RenderView;
 
     protected void Init
       (
         android.content.Context Context
       )
       /* common code for all constructors */
       {
         this.Context = Context;
         RenderView = (android.widget.TextView)inflate(Context, R.layout.digit_item, null);
         setGravity(CENTER_IN_PARENT);
         addView(RenderView);
         requestLayout();
         CurDigit = -1;
         SetDigit(0);
       } /*Init*/
 
     public DigitSpinner
       (
         android.content.Context Context
       )
       {
         super(Context);
         Init(Context);
       } /*DigitSpinner*/
 
     public DigitSpinner
       (
         android.content.Context Context,
         android.util.AttributeSet Attributes
       )
       {
         this(Context, Attributes, 0);
       } /*DigitSpinner*/
 
     public DigitSpinner
       (
         android.content.Context Context,
         android.util.AttributeSet Attributes,
         int DefaultStyle
       )
       {
         super(Context, Attributes, DefaultStyle);
         Init(Context);
       } /*DigitSpinner*/
 
     @Override
     public boolean onTouchEvent
       (
         android.view.MotionEvent TheEvent
       )
       {
         switch (TheEvent.getAction())
           {
         case android.view.MotionEvent.ACTION_DOWN:
               {
                 final android.widget.ArrayAdapter<String> Digits =
                     new android.widget.ArrayAdapter<String>(Context, R.layout.digit_item);
                 for (int d = 0; d < 10; ++d)
                   {
                     Digits.add(Integer.toString(d));
                   } /*for*/
                 final android.widget.ListView DigitsDisplay = new android.widget.ListView(Context);
                 DigitsDisplay.setAdapter(Digits);
                 DigitsDisplay.setChoiceMode(DigitsDisplay.CHOICE_MODE_SINGLE);
                 DigitsDisplay.setSelection(CurDigit);
                 DigitsDisplay.setBackgroundDrawable(RenderView.getBackground());
                 final android.widget.PopupWindow ThePopup =
                     new android.widget.PopupWindow(DigitsDisplay);
                 DigitsDisplay.setOnItemClickListener
                   (
                     new android.widget.AdapterView.OnItemClickListener()
                       {
                         public void onItemClick
                           (
                             android.widget.AdapterView<?> Parent,
                             android.view.View ItemView,
                             int ItemPosition,
                             long ItemId
                           )
                           {
                             SetDigit(ItemPosition);
                             ThePopup.dismiss();
                           } /*onItemClick*/
                       } /*OnItemClickListener*/
                   );
                 ThePopup.setWidth(RenderView.getWidth());
                ThePopup.setHeight(RenderView.getHeight() * 10);
                 ThePopup.setFocusable(true);
                 ThePopup.setOnDismissListener
                   (
                     new android.widget.PopupWindow.OnDismissListener()
                       {
                         public void onDismiss()
                           {
                             SetDigit(DigitsDisplay.getCheckedItemPosition());
                           } /*onDismiss*/
                       } /*OnDismissListener*/
                   );
                 ThePopup.showAsDropDown(DigitSpinner.this);
               }
         break;
       /* ACTION_CANCEL, ACTION_UP ignored */
           } /*switch*/
         return
             true;
       } /*onTouchEvent*/
 
     public int GetDigit()
       {
         return
             CurDigit;
       } /*GetDigit*/
 
     public void SetDigit
       (
         int NewDigit
       )
       {
         if (NewDigit != CurDigit && NewDigit >= 0 && NewDigit < 10)
           {
             CurDigit = NewDigit;
             RenderView.setText(Integer.toString(CurDigit));
           } /*if*/
       } /*SetDigit*/
 
   /*
     Implementation of saving/restoring instance state. Doing this
     allows me to transparently restore widget setting if system
     needs to kill me while I'm in the background, or on an orientation
     change while I'm in the foreground.
 
     Notes: View.onSaveInstanceState returns AbsSavedState.EMPTY_STATE,
     and View.onRestoreInstanceState expects to be passed this. Also,
     both superclass methods MUST be called in my overrides (the docs
     don't make this clear).
 
     This class subclasses RelativeLayout, which subclasses ViewGroup,
     neither of which overrides the above View methods. So those points
     still hold.
   */
     protected static class SavedDigitSpinnerState extends android.view.AbsSavedState
       {
 
         public static android.os.Parcelable.Creator<SavedDigitSpinnerState> CREATOR =
             new android.os.Parcelable.Creator<SavedDigitSpinnerState>()
               {
                 public SavedDigitSpinnerState createFromParcel
                   (
                     android.os.Parcel SavedState
                   )
                   {
                     final android.view.AbsSavedState SuperState =
                         android.view.AbsSavedState.CREATOR.createFromParcel(SavedState);
                     final android.os.Bundle MyState = SavedState.readBundle();
                     return
                         new SavedDigitSpinnerState
                           (
                             SuperState,
                             MyState.getInt("CurDigit", 0)
                           );
                   } /*createFromParcel*/
 
                 public SavedDigitSpinnerState[] newArray
                   (
                     int NrElts
                   )
                   {
                     return
                         new SavedDigitSpinnerState[NrElts];
                   } /*newArray*/
               } /*Parcelable.Creator*/;
 
         public final android.os.Parcelable SuperState;
       /* state that I'm actually interested in saving/restoring: */
         public final int CurDigit;
 
         public SavedDigitSpinnerState
           (
             android.os.Parcelable SuperState,
             int CurDigit
           )
           {
             super(SuperState);
             this.SuperState = SuperState;
             this.CurDigit = CurDigit;
           } /*SavedDigitSpinnerState*/
 
         public void writeToParcel
           (
             android.os.Parcel SavedState,
             int Flags
           )
           {
             super.writeToParcel(SavedState, Flags);
           /* put my state in a Bundle, where each item is associated with a
             keyword name (unlike the Parcel itself, where items are identified
             by order). I think this makes things easier to understand, and extend
             in future. */
             final android.os.Bundle MyState = new android.os.Bundle();
             MyState.putInt("CurDigit", CurDigit);
             SavedState.writeBundle(MyState);
           } /*writeToParcel*/
 
       } /*SavedDigitSpinnerState*/
 
     @Override
     public android.os.Parcelable onSaveInstanceState()
       {
         return
             new SavedDigitSpinnerState
               (
                 super.onSaveInstanceState(),
                 CurDigit
               );
       } /*onSaveInstanceState*/
 
     @Override
     public void onRestoreInstanceState
       (
         android.os.Parcelable SavedState
       )
       {
         final SavedDigitSpinnerState MyState = (SavedDigitSpinnerState)SavedState;
         super.onRestoreInstanceState(MyState.SuperState);
         SetDigit(MyState.CurDigit);
       } /*onRestoreInstanceState*/
 
   } /*DigitSpinner*/
