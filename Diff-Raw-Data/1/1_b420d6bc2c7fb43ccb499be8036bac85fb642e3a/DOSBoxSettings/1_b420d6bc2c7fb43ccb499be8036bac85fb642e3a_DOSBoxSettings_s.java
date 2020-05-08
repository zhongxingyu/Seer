 /**
  * 
  */
 package org.hystudio.android.dosbox;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnShowListener;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * @author zhguo
  * 
  */
 public class DOSBoxSettings {
 
   // TODO this should be user configurable
   // Read function CPU_CycleIncrease for details.
   // If cpu cycle is set to "auto", 5% is increased/decreased each time.
   // Otherwise, if step is smaller than 100, it is considered as a percentage.
   // if step is not smaller than 100, it is considered as an absolute vlaue.
   private static int cpucycle_change = 200;
   private static AlertDialog dialog = null;
 
   protected static void showConfigMainMenu(final MainActivity p) {
     if (dialog != null) {
       dialog.show();
       return;
     }
 
     AlertDialog.Builder builder = new AlertDialog.Builder(p);
     builder.setTitle(p.getResources().getString(R.string.dosbox_settings));
 
     LayoutInflater inflater = (LayoutInflater) p
         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     final ViewGroup dosboxSettingView = (ViewGroup) inflater.inflate(
         R.layout.dosbox_settings, null);
 
     final LinearLayout cpuIncLL = (LinearLayout) dosboxSettingView
         .findViewById(R.id.cpucycle_inc_ll);
     cpuIncLL.setOnClickListener(new OnClickListener() {
       @Override
       public void onClick(View paramView) {
         increaseCPUCycles(p);
         updateView(dosboxSettingView);
       }
     });
 
     final LinearLayout cpuDecLL = (LinearLayout) dosboxSettingView
         .findViewById(R.id.cpucycle_dec_ll);
     cpuDecLL.setOnClickListener(new OnClickListener() {
       @Override
       public void onClick(View paramView) {
         decreaseCPUCycles(p);
         updateView(dosboxSettingView);
       }
     });
 
     final LinearLayout frameskipIncLL = (LinearLayout) dosboxSettingView
         .findViewById(R.id.frameskip_inc_ll);
     frameskipIncLL.setOnClickListener(new OnClickListener() {
       @Override
       public void onClick(View paramView) {
         increaseFrameskip(p);
         updateView(dosboxSettingView);
       }
     });
 
     final LinearLayout frameskipDecLL = (LinearLayout) dosboxSettingView
         .findViewById(R.id.frameskip_dec_ll);
     frameskipDecLL.setOnClickListener(new OnClickListener() {
       @Override
       public void onClick(View paramView) {
         decreaseFrameskip(p);
         updateView(dosboxSettingView);
       }
     });
 
     // CPU cycle change
     final LinearLayout cpuCycleUpLL = (LinearLayout) dosboxSettingView
         .findViewById(R.id.cyclechange_up_ll);
     cpuCycleUpLL.setOnLongClickListener(new OnLongClickListener() {
       @Override
       public boolean onLongClick(View paramView) {
         AlertDialog.Builder builder = new AlertDialog.Builder(p);
         LinearLayout ll = new LinearLayout(p);
         ll.setLayoutParams(new LinearLayout.LayoutParams(
             LinearLayout.LayoutParams.WRAP_CONTENT,
             LinearLayout.LayoutParams.WRAP_CONTENT));
         ll.setOrientation(LinearLayout.VERTICAL);
         
         final EditText cycleChangeET = new EditText(p);
         cycleChangeET.setSingleLine();
         cycleChangeET.setWidth(200);
         ll.addView(cycleChangeET);
         
         TextView helpView = new TextView(p);
         helpView.setText("hint: >= 100, absolute value; < 100, percentage");
         helpView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
         
         builder.setView(ll);
         AlertDialog dialog = builder.create();
         dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
             new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface paramDialogInterface,
                   int paramInt) {
                 String strCycleChange = cycleChangeET.getText().toString();
                 try {
                   int cycleChange = Integer.valueOf(strCycleChange);
                   int max = 1000, min = 5;
                   if (cycleChange > max || cycleChange < min) {
                     Toast.makeText(p, String.format("Entered value is not correct [%d-%d]", min, max), 2000).show();
                     return;
                   }
                   nativeSetCPUCycleUp(cycleChange);
                   updateView(dosboxSettingView);
                 } catch (Exception e) {
                   Log.e("aDOSBox", e.getMessage());
                 }
               }
             });
         dialog.show();
         return true;
       }
     });
 
     final LinearLayout cpuCycleDownLL = (LinearLayout) dosboxSettingView
         .findViewById(R.id.cyclechange_down_ll);
     cpuCycleDownLL.setOnLongClickListener(new OnLongClickListener() {
       @Override
       public boolean onLongClick(View paramView) {
         AlertDialog.Builder builder = new AlertDialog.Builder(p);
         LinearLayout ll = new LinearLayout(p);
         ll.setLayoutParams(new LinearLayout.LayoutParams(
             LinearLayout.LayoutParams.WRAP_CONTENT,
             LinearLayout.LayoutParams.WRAP_CONTENT));
         ll.setOrientation(LinearLayout.VERTICAL);
         
         final EditText cycleChangeET = new EditText(p);
         cycleChangeET.setSingleLine();
         cycleChangeET.setWidth(200);
         ll.addView(cycleChangeET);
         
         TextView helpView = new TextView(p);
         helpView.setText("hint: >= 100, absolute value; < 100, percentage");
         helpView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
         ll.addView(helpView);
         
         builder.setView(ll);
         AlertDialog dialog = builder.create();
         dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
             new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface paramDialogInterface,
                   int paramInt) {
                 String strCycleChange = cycleChangeET.getText().toString();
                 try {
                   int cycleChange = Integer.valueOf(strCycleChange);
                   int max = 1000, min = 5;
                   if (cycleChange > max || cycleChange < min) {
                     Toast.makeText(p, String.format("Entered value is not correct [%d-%d]", min, max), 2000).show();
                     return;
                   }
                   nativeSetCPUCycleDown(cycleChange);
                   updateView(dosboxSettingView);
                 } catch (Exception e) {
                   Log.e("aDOSBox", e.getMessage());
                 }
               }
             });
         dialog.show();
         return true;
       }
     });
 
     builder.setView(dosboxSettingView);
 
     dialog = builder.create();
     dialog.setOnShowListener(new OnShowListener() {
       @Override
       public void onShow(DialogInterface paramDialogInterface) {
         updateView(dosboxSettingView);
       }
     });
     dialog.show();
   }
 
   private static void updateView(ViewGroup dosboxSettingView) {
     TextView incTV = (TextView) dosboxSettingView
         .findViewById(R.id.cpucycleshow_inc_tv);
     TextView decTV = (TextView) dosboxSettingView
         .findViewById(R.id.cpucycleshow_dec_tv);
     TextView cycleUpTV = (TextView) dosboxSettingView
         .findViewById(R.id.cpucycleup_show_tv);
     TextView cycleDownTV = (TextView) dosboxSettingView
         .findViewById(R.id.cpucycledown_show_tv);
     TextView frameskipIncTV = (TextView) dosboxSettingView
         .findViewById(R.id.frameskip_show_inc_tv);
     TextView frameskipDecTV = (TextView) dosboxSettingView
         .findViewById(R.id.frameskip_show_dec_tv);
 
     int cycles = nativeGetCPUCycle();
     int cycleUp = nativeGetCPUCycleUp();
     int cycleDown = nativeGetCPUCycleDown();
     int frameskip = nativeGetFrameskip();
 
     incTV.setText(cycles + "");
     decTV.setText(cycles + "");
     cycleUpTV.setText(cycleUp + "");
     cycleDownTV.setText(cycleDown + "");
     frameskipIncTV.setText(frameskip + "");
     frameskipDecTV.setText(frameskip + "");
   }
 
   private static void increaseCPUCycles(Context p) {
     nativeCPUCycleIncrease();
   }
 
   private static void decreaseCPUCycles(Context p) {
     nativeCPUCycleDecrease();
   }
 
   private static void increaseFrameskip(Context p) {
     nativeFrameskipIncrease();
   }
 
   private static void decreaseFrameskip(Context p) {
     nativeFrameskipDecrease();
   }
 
   static {
     String libs[] = { "application", "sdl_main" };
     try {
       for (String l : libs) {
         System.loadLibrary(l);
       }
     } catch (UnsatisfiedLinkError e) {
       e.printStackTrace();
       Log.e("aDOSBox", e.getMessage());
     }
   }
 
   private static native void nativeCPUCycleIncrease();
 
   private static native void nativeCPUCycleDecrease();
 
   private static native void nativeFrameskipIncrease();
 
   private static native void nativeFrameskipDecrease();
 
   private static native int nativeGetCPUCycle();
 
   private static native int nativeGetCPUCycleUp();
 
   private static native int nativeGetCPUCycleDown();
 
   private static native void nativeSetCPUCycleUp(int cycleup);
 
   private static native void nativeSetCPUCycleDown(int cycledown);
 
   private static native int nativeGetFrameskip();
 }
