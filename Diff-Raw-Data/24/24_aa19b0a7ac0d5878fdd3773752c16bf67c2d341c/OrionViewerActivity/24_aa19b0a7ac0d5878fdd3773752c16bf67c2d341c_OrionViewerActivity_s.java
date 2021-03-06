 package com.google.code.orion_viewer;
 
 /*Orion Viewer is a pdf viewer for Nook Classic based on mupdf
 
 Copyright (C) 2011  Michael Bogdanov
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.*/
 
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Debug;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.*;
 import android.widget.*;
 
 
 import com.google.code.orion_viewer.djvu.DjvuDocument;
 import com.google.code.orion_viewer.pdf.PdfDocument;
 import com.google.code.orion_viewer.prefs.OrionKeyBinderActivity;
 import com.google.code.orion_viewer.prefs.OrionPreferenceActivity;
 import com.google.code.orion_viewer.prefs.OrionTapActivity;
 import pl.polidea.customwidget.TheMissingTabHost;
 
 import java.io.*;
 
 public class OrionViewerActivity extends OrionBaseActivity {
 
     private Dialog dialog;
 
     private static final int ROTATION_SCREEN = 0;
 
     private static final int MAIN_SCREEN = 0;
 
     private static final int PAGE_SCREEN = 1;
 
     private static final int ZOOM_SCREEN = 2;
 
     private static final int CROP_SCREEN = 3;
 
     private static final int NAVIGATION_SCREEN = 4;
 
     private static final int HELP_SCREEN = 5;
 
     private static final int OPTIONS_SCREEN = 6;
 
     private static final int CROP_RESTRICTION_MIN = -10;
 
     private static final int CROP_DELTA = 10;
 
     private static final int CROP_RESTRICTION_MAX = 40;
 
     private final SubscriptionManager manager = new SubscriptionManager();
 
     private OrionView view;
 
     private ViewAnimator animator;
 
     private LastPageInfo pageInfo;
 
     //left, right, top, bottom
     private int [] cropBorders = new int[4];
 
     private Controller controller;
 
     private OperationHolder operation = new OperationHolder();
 
     private GlobalOptions globalOptions;
 
     private Intent myIntent;
 
     private boolean isFullScreen;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         loadGlobalOptions();
         isFullScreen = globalOptions.isFullScreen();
         if (isFullScreen) {
             getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         }
 
         setContentView(device.getLayoutId());
 
         super.onCreate(savedInstanceState);
 
         view = (OrionView) findViewById(R.id.view);
 
         if (!device.optionViaDialog()) {
             initAnimator();
             initMainScreen();
             //initHelpScreen();
         } else {
             initOptionDialog();
             initRotationScreen();
         }
 
         //page chooser
         initPagePeekerScreen();
 
         initZoomScreen();
 
         initCropScreen();
 
         initNavigationScreen();
 
         myIntent = getIntent();
     }
 
 
 
     protected void initHelpScreen() {
         TheMissingTabHost host = (TheMissingTabHost) findMyViewById(R.id.helptab);
 
         host.setup();
 
         TheMissingTabHost.TheMissingTabSpec spec = host.newTabSpec("general_help");
         spec.setContent(R.id.general_help);
         spec.setIndicator("", getResources().getDrawable(R.drawable.help));
         host.addTab(spec);
         TheMissingTabHost.TheMissingTabSpec recent = host.newTabSpec("app_info");
         recent.setContent(R.id.app_info);
         recent.setIndicator("", getResources().getDrawable(R.drawable.info));
         host.addTab(recent);
         host.setCurrentTab(0);
 
         ImageButton btn = (ImageButton) findMyViewById(R.id.help_close);
         btn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 //animator.setDisplayedChild(MAIN_SCREEN);
                 onAnimatorCancel();
             }
         });
 
         btn = (ImageButton) findMyViewById(R.id.info_close);
         btn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 onAnimatorCancel();
                 //animator.setDisplayedChild(MAIN_SCREEN);
             }
         });
     }
 
     public void updateLabels() {
 //        updateOptions();
 //        updateCrops();
     }
 
     public void updateCrops() {
         controller.getMargins(cropBorders);
         TableLayout cropTable = (TableLayout) findMyViewById(R.id.crop_borders);
         for (int i = 0; i < cropTable.getChildCount(); i++) {
             TableRow row = (TableRow) cropTable.getChildAt(i);
             TextView valueView = (TextView) row.findViewById(R.id.crop_value);
             valueView.setText(cropBorders[i] + "%");
         }
     }
 
     public void updateOptions() {
         int did = controller.getDirection();
         int lid = controller.getLayout();
         ((RadioGroup) findMyViewById(R.id.layoutGroup)).check(lid == 0 ? R.id.layout1 : lid == 1 ? R.id.layout2 : R.id.layout3);
         ((RadioGroup) findMyViewById(R.id.directionGroup)).check(did == 0 ? R.id.direction1 : R.id.direction2);
     }
 
     protected void onNewIntent(Intent intent) {
         Common.d( "Runtime.getRuntime().totalMemory() = " + Runtime.getRuntime().totalMemory());
         Common.d("Debug.getNativeHeapSize() = " + Debug.getNativeHeapSize());
         Common.stopLogger();
         if (controller != null) {
             controller.destroy();
             controller = null;
         }
 
         if (intent.getData() != null) {
             Uri uri = intent.getData();
             Common.d("File URI  = " + uri.toString());
             String file = uri.getPath();
 
             openFile(file);
         } else /*if (intent.getAction().endsWith("MAIN"))*/ {
             //TODO error
         }
     }
 
     public void openFile(String filePath) {
         DocumentWrapper doc = null;
         Common.d("File URI  = " + filePath);
         Common.startLogger(filePath + ".trace");
         try {
             if (filePath.toLowerCase().endsWith("pdf")) {
                 doc = new PdfDocument(filePath);
             } else {
                 doc = new DjvuDocument(filePath);
             }
 
             LayoutStrategy str = new SimpleLayoutStrategy(doc);
 
             int idx = filePath.lastIndexOf('/');
             String fileData = filePath.substring(idx + 1) + ".userData";
 
             controller = new Controller(this, doc, str, view);
             try {
                 ObjectInputStream inp = new ObjectInputStream(openFileInput(fileData));
                 pageInfo = (LastPageInfo) inp.readObject();
                 inp.close();
             } catch (Exception e) {
                 pageInfo = new LastPageInfo();
 
                 int defaultRotation = globalOptions.getDefaultOrientation();
                 switch (defaultRotation) {
                     case 90: defaultRotation = -1; break;
                     case 270: defaultRotation = 1; break;
                     default: defaultRotation = 0; break;
                 }
                 pageInfo.rotation = defaultRotation;
             }
 
             pageInfo.fileName = fileData;
             controller.init(pageInfo);
 
             getSubscriptionManager().sendDocOpenedNotification(controller);
 
             controller.drawPage();
 
             getView().setController(controller);
             String title = doc.getTitle();
             if (title == null || "".equals(title)) {
                 title = filePath.substring(idx + 1);
                 title = title.substring(0, title.lastIndexOf("."));
             }
 
             device.updateTitle(title);
             globalOptions.addRecentEntry(new GlobalOptions.RecentEntry(new File(filePath).getAbsolutePath()));
         } catch (Exception e) {
             Common.d(e);
             if (doc != null) {
                 doc.destroy();
             }
         }
     }
 
 
     public void onPause() {
         super.onPause();
         if (controller != null) {
             controller.onPause();
             saveData();
         }
     }
 
     public void initPagePeekerScreen() {
         final SeekBar pageSeek = (SeekBar) findMyViewById(R.id.page_picker_seeker);
 
         getSubscriptionManager().addDocListeners(new DocumentViewAdapter() {
             @Override
             public void documentOpened(Controller controller) {
                 pageSeek.setMax(controller.getPageCount() - 1);
                 pageSeek.setProgress(controller.getCurrentPage());
             }
 
             @Override
             public void pageChanged(int newPage, int pageCount) {
                 pageSeek.setProgress(newPage);
             }
         });
 
 
         final TextView pageNumberText = (TextView) findMyViewById(R.id.page_picker_message);
         //initial state
         pageNumberText.setText("" + 1);
 
         pageSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 pageNumberText.setText("" + (progress + 1));
             }
 
             public void onStartTrackingTouch(SeekBar seekBar) {
             }
 
             public void onStopTrackingTouch(SeekBar seekBar) {
             }
         });
 
         ImageButton closePagePeeker = (ImageButton) findMyViewById(R.id.page_picker_close);
 
         ImageButton plus = (ImageButton) findMyViewById(R.id.page_picker_plus);
         plus.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 pageSeek.incrementProgressBy(1);
             }
         });
 
         ImageButton minus = (ImageButton) findMyViewById(R.id.page_picker_minus);
         minus.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if (pageSeek.getProgress() != 0) {
                     pageSeek.incrementProgressBy(-1);
                 }
             }
         });
 
         closePagePeeker.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 //controller.drawPage(Integer.valueOf(pageNumberText.getText().toString()) - 1);
                 //main menu
                 onAnimatorCancel();
                 updatePageSeeker();
                 //animator.setDisplayedChild(MAIN_SCREEN);
             }
         });
 
         ImageButton page_preview = (ImageButton) findMyViewById(R.id.page_preview);
         page_preview.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 onApplyAction();
                 controller.drawPage(Integer.valueOf(pageNumberText.getText().toString()) -1);
             }
         });
     }
 
     public void updatePageSeeker() {
         SeekBar pageSeek = (SeekBar) findMyViewById(R.id.page_picker_seeker);
         pageSeek.setProgress(controller.getCurrentPage());
     }
 
     public void initZoomScreen() {
         //zoom screen
         final TextView zoomText = (TextView) findMyViewById(R.id.zoom_picker_message);
 
         final SeekBar zoomSeek = (SeekBar) findMyViewById(R.id.zoom_picker_seeker);
         zoomSeek.setMax(300);
         zoomSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
             public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 zoomText.setText(progress + "%");
             }
 
             public void onStartTrackingTouch(SeekBar seekBar) {
             }
 
             public void onStopTrackingTouch(SeekBar seekBar) {
             }
         });
 
         getSubscriptionManager().addDocListeners(new DocumentViewAdapter(){
             @Override
             public void documentOpened(Controller controller) {
                 zoomText.setText(controller.getZoomFactor() + "%");
                 zoomSeek.setProgress(controller.getZoomFactor());
             }
         });
 
         ImageButton zplus = (ImageButton) findMyViewById(R.id.zoom_picker_plus);
         zplus.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 zoomSeek.incrementProgressBy(1);
             }
         });
 
         ImageButton zminus = (ImageButton) findMyViewById(R.id.zoom_picker_minus);
         zminus.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if (zoomSeek.getProgress() != 0) {
                     zoomSeek.incrementProgressBy(-1);
                 }
             }
         });
 
         ImageButton closeZoomPeeker = (ImageButton) findMyViewById(R.id.zoom_picker_close);
         closeZoomPeeker.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 //controller.changeZoom(zoomSeek.getProgress());
                 //main menu
                 onAnimatorCancel();
                 updateZoom();
                 //animator.setDisplayedChild(MAIN_SCREEN);
             }
         });
 
         ImageButton zoom_preview = (ImageButton) findMyViewById(R.id.zoom_preview);
         zoom_preview.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 onApplyAction();
                 controller.changeZoom(zoomSeek.getProgress());
             }
         });
     }
 
     public void updateZoom() {
         SeekBar zoomSeek = (SeekBar) findMyViewById(R.id.zoom_picker_seeker);
         zoomSeek.setProgress(controller.getZoomFactor());
     }
 
 
     public void initNavigationScreen() {
         ImageButton close = (ImageButton) findMyViewById(R.id.options_close);
         close.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 //                int did = ((RadioGroup) findMyViewById(R.id.directionGroup)).getCheckedRadioButtonId();
 //                int lid = ((RadioGroup) findMyViewById(R.id.layoutGroup)).getCheckedRadioButtonId();
 //                controller.setDirectionAndLayout(did == R.id.direction1 ? 0 : 1, lid == R.id.layout1 ? 0 : lid == R.id.layout2 ? 1 : 2);
                 //main menu
                 onAnimatorCancel();
                 updateOptions();
                 //animator.setDisplayedChild(MAIN_SCREEN);
             }
         });
 
 
         ImageButton view = (ImageButton) findMyViewById(R.id.options_apply);
         view.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 onApplyAction();
                 int did = ((RadioGroup) findMyViewById(R.id.directionGroup)).getCheckedRadioButtonId();
                 int lid = ((RadioGroup) findMyViewById(R.id.layoutGroup)).getCheckedRadioButtonId();
                 controller.setDirectionAndLayout(did == R.id.direction1 ? 0 : 1, lid == R.id.layout1 ? 0 : lid == R.id.layout2 ? 1 : 2);
             }
         });
 
         getSubscriptionManager().addDocListeners(new DocumentViewAdapter() {
             public void documentOpened(Controller controller) {
                 updateOptions();
             }
         });
     }
 
 
     public void initCropScreen() {
         TableLayout cropTable = (TableLayout) findMyViewById(R.id.crop_borders);
 
 
         getSubscriptionManager().addDocListeners(new DocumentViewAdapter(){
             @Override
             public void documentOpened(Controller controller) {
                 updateCrops();
             }
         });
         for (int i = 0; i < cropTable.getChildCount(); i++) {
             TableRow row = (TableRow) cropTable.getChildAt(i);
             row.findViewById(R.id.crop_plus);
 
             TextView valueView = (TextView) row.findViewById(R.id.crop_value);
             ImageButton plus = (ImageButton) row.findViewById(R.id.crop_plus);
             ImageButton minus = (ImageButton) row.findViewById(R.id.crop_minus);
             linkCropButtonsAndText(minus, plus, valueView, i);
         }
 
 
 //        cropList.setAdapter(new ArrayAdapter(this, R.layout.crop, new String[] {"Left  ", "Right ", "Top   ", "Bottom"}) {
 //
 //            public View getView(int position, View convertView, ViewGroup parent) {
 //                View v = convertView;
 //                if (v == null) {
 //                    LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 //                    v = vi.inflate(R.layout.crop, null);
 //                }
 //
 //                String cropHeader = (String) getItem(position);
 //
 //                //crop header
 //                TextView header = (TextView) v.findViewById(R.id.crop_text);
 //                header.setText(cropHeader);
 //
 //                TextView valueView = (TextView) v.findViewById(R.id.crop_value);
 //
 //                valueView.setText("" + cropBorders[position]);
 //
 //                ImageButton plus = (ImageButton) v.findViewById(R.id.crop_plus);
 //                ImageButton minus = (ImageButton) v.findViewById(R.id.crop_minus);
 //                linkCropButtonsAndText(minus, plus, (TextView) v.findViewById(R.id.crop_value), position);
 //
 //                return v;
 //            }
 //        });
 
         ImageButton preview = (ImageButton) findMyViewById(R.id.crop_preview);
         preview.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 onApplyAction();
                 controller.changeMargins(cropBorders[0], cropBorders[2], cropBorders[1], cropBorders[3]);
             }
         });
 
         ImageButton close = (ImageButton) findMyViewById(R.id.crop_close);
         close.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 //main menu
                 //controller.changeMargins(cropBorders[0], cropBorders[2], cropBorders[1], cropBorders[3]);
                 //animator.setDisplayedChild(MAIN_SCREEN);
                 onAnimatorCancel();
                 //reset if canceled
                 updateCrops();
             }
         });
     }
 
     public void linkCropButtonsAndText(final ImageButton minus, final ImageButton plus, final TextView text, final int cropIndex) {
         minus.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 //main menu
                 if (cropBorders[cropIndex] != CROP_RESTRICTION_MIN) {
                     cropBorders[cropIndex] = cropBorders[cropIndex] - 1;
                     text.setText(cropBorders[cropIndex] + "%");
                 }
             }
         });
 
         minus.setOnLongClickListener(new View.OnLongClickListener() {
             public boolean onLongClick(View v) {
                 cropBorders[cropIndex] = cropBorders[cropIndex] - CROP_DELTA;
                 if (cropBorders[cropIndex] < CROP_RESTRICTION_MIN) {
                     cropBorders[cropIndex] = CROP_RESTRICTION_MIN;
                 }
                 text.setText(cropBorders[cropIndex] + "%");
                 return true;
             }
         });
 
         plus.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 //main menu
                 //int value = Integer.valueOf(text.getText().toString());
                 cropBorders[cropIndex] = cropBorders[cropIndex] + 1;
                 if (cropBorders[cropIndex] > CROP_RESTRICTION_MAX) {
                     cropBorders[cropIndex] = CROP_RESTRICTION_MAX;
                 }
                 text.setText(cropBorders[cropIndex]  + "%");
             }
         });
 
         plus.setOnLongClickListener(new View.OnLongClickListener() {
             public boolean onLongClick(View v) {
                 cropBorders[cropIndex] = cropBorders[cropIndex] + CROP_DELTA;
                 if (cropBorders[cropIndex] > CROP_RESTRICTION_MAX) {
                     cropBorders[cropIndex] = CROP_RESTRICTION_MAX;
                 }
                 text.setText(cropBorders[cropIndex] + "%");
                 return true;
             }
         });
     }
 
     private void initMainScreen() {
         ImageButton btn = (ImageButton) findMyViewById(R.id.exit);
         btn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 //controller.destroy();
                 finish();
             }
         });
         btn = (ImageButton) findMyViewById(R.id.prev_page);
         btn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 changePage(-1);
             }
         });
 
         btn.setOnLongClickListener(new View.OnLongClickListener() {
 
             public boolean onLongClick(View v) {
                 //page seeker
                 animator.setDisplayedChild(PAGE_SCREEN);
                 return true;
             }
         });
 
         btn = (ImageButton) findMyViewById(R.id.next_page);
         btn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 changePage(1);
             }
         });
         btn.setOnLongClickListener(new View.OnLongClickListener() {
 
             public boolean onLongClick(View v) {
                 //page seeker
                 animator.setDisplayedChild(PAGE_SCREEN);
                 return true;
             }
         });
 
         btn = (ImageButton) findMyViewById(R.id.switch_page);
         btn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 controller.setRotation((controller.getRotation() - 1) % 2);
             }
         });
         btn.setOnLongClickListener(new View.OnLongClickListener() {
             public boolean onLongClick(View v) {
                 controller.setRotation((controller.getRotation() + 1) % 2);
                 return true;
             }
         });
 
         btn = (ImageButton) findMyViewById(R.id.zoom);
         btn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 animator.setDisplayedChild(ZOOM_SCREEN);
             }
         });
 
         btn = (ImageButton) findMyViewById(R.id.crop_menu);
         btn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 updateCrops();
                 animator.setDisplayedChild(CROP_SCREEN);
             }
         });
 
         btn = (ImageButton) findMyViewById(R.id.help);
         btn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 //animator.setDisplayedChild(HELP_SCREEN);
                 Intent intent = new Intent();
                 intent.setClass(OrionViewerActivity.this, OrionHelpActivity.class);
                 startActivity(intent);
             }
         });
 
 
         btn = (ImageButton) findMyViewById(R.id.navigation);
         btn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 updateOptions();
                 animator.setDisplayedChild(NAVIGATION_SCREEN);
             }
         });
 
         btn = (ImageButton) findMyViewById(R.id.options);
         btn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Intent in = new Intent(OrionViewerActivity.this, OrionPreferenceActivity.class);
                 startActivity(in);
             }
         });
     }
 
     protected void onResume() {
         super.onResume();
         boolean newFullScreen = globalOptions.isFullScreen();
         if (isFullScreen != newFullScreen) {
             if (newFullScreen) {
                 getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
             } else {
                 getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
             }
             isFullScreen = newFullScreen;
         }
 
         Common.d("onResume");
         if (myIntent != null) {
             //starting creation intent
             onNewIntent(myIntent);
             myIntent = null;
         } else {
             if (controller != null) {
                 //controller.startRenderer();
                 controller.drawPage();
             }
         }
     }
 
     protected void onDestroy() {
         super.onDestroy();
         Common.d("onDestroy");
         Common.stopLogger();
         if (controller != null) {
             controller.destroy();
         }
         if (dialog != null) {
             dialog.dismiss();
         }
         globalOptions.onDestroy(this);
     }
 
     private void saveData() {
        if (controller != null) {
             try {
                 controller.serialize(pageInfo);
                 ObjectOutputStream out =
                     new ObjectOutputStream(OrionViewerActivity.this.openFileOutput(pageInfo.fileName,
                         Context.MODE_PRIVATE));
                 out.writeObject(pageInfo);
                 out.close();
             } catch (Exception ex) {
                 Log.e(Common.LOGTAG, ex.getMessage(), ex);
             }
        }
         saveGlobalOptions();
     }
 
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         System.out.println("key " + keyCode);
         if (keyCode == KeyEvent.KEYCODE_BACK) {
             if (!device.optionViaDialog() && animator.getDisplayedChild() != MAIN_SCREEN) {
                 onAnimatorCancel();
                 return true;
             }
         }
 
         if (device.onKeyDown(keyCode, event, operation)) {
             changePage(operation.value);
             return true;
         }
 
         return super.onKeyDown(keyCode,  event);
     }
 
     public void changePage(int operation) {
         boolean swapKeys = globalOptions.isSwapKeys();
         if (controller != null) {
             if (operation == Device.NEXT && !swapKeys || swapKeys && operation == 1 && controller.getRotation() != -1 || swapKeys && operation == - 1 && controller.getRotation() == -1) {
                 controller.drawNext();
             } else {
                 controller.drawPrev();
             }
         }
     }
 
     public void loadGlobalOptions() {
         globalOptions = new GlobalOptions(this, device);
         device.updateOptions(globalOptions);
     }
 
     public void saveGlobalOptions() {
 //        ObjectOutputStream out = null;
 //        try {
 //            out = new ObjectOutputStream(OrionViewerActivity.this.openFileOutput(GLOBAL_OPTIONS_FILE,
 //                    Context.MODE_PRIVATE));
 //            out.writeObject(globalOptions);
 //            out.close();
 //        } catch (Exception ex) {
 //            Log.e(Common.LOGTAG, ex.getMessage(), ex);
 //        } finally {
 //            if (out != null) {
 //                try {
 //                    out.close();
 //                } catch (IOException e) {
 //                    Common.d(e);
 //                }
 //            }
 //        }
         Common.d("Saving global options...");
         globalOptions.saveRecents();
         Common.d("Done!");
     }
 
     public OrionView getView() {
         return view;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         boolean result = super.onCreateOptionsMenu(menu);
         if (result) {
             getMenuInflater().inflate(R.menu.menu, menu);
         }
         return result;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         int screenId = HELP_SCREEN;
         switch (item.getItemId()) {
             case R.id.exit_menu_item:
                 finish();
                 return true;
             case R.id.crop_menu_item: screenId = CROP_SCREEN; break;
             case R.id.zoom_menu_item: screenId = ZOOM_SCREEN; break;
             case R.id.goto_menu_item: screenId = PAGE_SCREEN; break;
             case R.id.navigation_menu_item: screenId = NAVIGATION_SCREEN; break;
             case R.id.rotation_menu_item: screenId = ROTATION_SCREEN; break;
             case R.id.keybinder_menu_item:
                 Intent in = new Intent(this, OrionKeyBinderActivity.class);
                 startActivity(in);
                 return true;
             case R.id.options_menu_item:
                 Intent intent = new Intent(this, OrionPreferenceActivity.class);
                 startActivity(intent);
                 return true;
 
             case R.id.tap_menu_item:
                 Intent tap = new Intent(this, OrionTapActivity.class);
                 startActivity(tap);
                 return true;
         }
 
         if (screenId != HELP_SCREEN) {
             updateRotation();
             updateCrops();
             updateOptions();
             updatePageSeeker();
             animator.setDisplayedChild(screenId);
             dialog.show();
         } else {
             Intent intent = new Intent();
             intent.setClass(this, OrionHelpActivity.class);
             startActivity(intent);
         }
         return true;
     }
 
     public void initAnimator() {
         animator = (ViewAnimator) findMyViewById(R.id.viewanim);
         getSubscriptionManager().addDocListeners(new DocumentViewAdapter() {
             @Override
             public void documentOpened(Controller controller) {
                 animator.setDisplayedChild(MAIN_SCREEN);
             }
 
             public void pageChanged(final int newPage, final int pageCount) {
                 TextView tv = (TextView) findMyViewById(R.id.page_number_view);
                 tv.setText(newPage + 1 + "/" + pageCount);
                 device.updatePageNumber(newPage + 1, pageCount);
             }
         });
     }
 
     public SubscriptionManager getSubscriptionManager() {
         return manager;
     }
 
     public void initOptionDialog() {
         dialog = new Dialog(this);
         dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
         dialog.setContentView(R.layout.android_dialog);
         animator = ((ViewAnimator)dialog.findViewById(R.id.viewanim));
 
         getView().setOnTouchListener(new View.OnTouchListener() {
             private int lastX = -1;
             private int lastY = -1;
             long startTime = 0;
             private static final long TIME_DELTA = 600;
             public boolean onTouch(View v, MotionEvent event) {
                 Common.d("Even/t " + event.getAction());
 //                System.out.println(SystemClock.uptimeMillis() - startTime);
                 if (event.getAction() == MotionEvent.ACTION_DOWN) {
                     Common.d("DOWN " + event.getAction());
                     startTime = SystemClock.uptimeMillis();
                     lastX = (int) event.getX();
                     lastY = (int) event.getY();
                     return true;
                 } else {
                     Common.d("ev " + event.getAction());
                     boolean doAction = false;
                     if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_UP) {
                         if (event.getAction() == MotionEvent.ACTION_UP) {
                             Common.d("UP " + event.getAction());
                             doAction = true;
                         } else {
                             boolean isLongClick = (SystemClock.uptimeMillis() - startTime) > TIME_DELTA;
                             doAction = isLongClick;
                         }
 
                         if (doAction) {
                             Common.d("Do Action  on " + event.getAction());
                             boolean isLongClick = (SystemClock.uptimeMillis() - startTime) > TIME_DELTA;
 
                             if (lastX != -1 && lastY != -1) {
                                 int width = getView().getWidth();
                                 int height = getView().getHeight();
 
                                 int i = 3 * lastY / height;
                                 int j = 3 * lastX / width;
 
                                 int code = globalOptions.getActionCode(i, j, isLongClick);
                                 doAction(code);
 
                                 startTime = 0;
                                 lastX = -1;
                                 lastY = -1;
                             }
                         }
                         return true;
                     }
 
                     startTime = 0;
                     lastX = -1;
                     lastY = -1;
                 }
 
                 return false;
             }
         });
 
 //        getView().setOnClickListener(new View.OnClickListener() {
 //            public void onClick(View v) {
 //                v.get
 //                globalOptions.getActionCode()
 //                controller.drawNext();
 //            }
 //        });
 //
 //        getView().setOnLongClickListener(new View.OnLongClickListener(){
 //            public boolean onLongClick(View v) {
 //                controller.drawPrev();
 //                return true;
 //            }
 //        });
 
 //        getView().setOnTouchListener(new View.OnTouchListener() {
 //            public boolean onTouch(View v, MotionEvent event) {
 //
 //            }
 //        });
     }
 
     private void doAction(int code) {
         Action action = Action.getAction(code);
         doAction(action);
         Common.d("Code action " + code);
     }
 
 
     public void doAction(Action action) {
         int screenId = -1;
         switch (action) {
             case NONE: break;
             case MENU: openOptionsMenu(); return;
             case CROP: screenId = CROP_SCREEN; break;
             case ZOOM: screenId = ZOOM_SCREEN; break;
             case GOTO: screenId = PAGE_SCREEN; break;
             case ROTATION: screenId = ROTATION_SCREEN; break;
             case OPTIONS:
                 Intent intent = new Intent(this, OrionPreferenceActivity.class);
                 startActivity(intent);
             break;
             case NEXT:
                 if (controller != null) {
                     controller.drawNext();
                 }
                 break;
             case PREV:
                 if (controller != null) {
                     controller.drawPrev();
                 }
                 break;
         }
 
         if (screenId != -1) {
             updateRotation();
             updateCrops();
             updateOptions();
             updatePageSeeker();
             animator.setDisplayedChild(screenId);
             dialog.show();
         } else {
             action.doAction(controller, this);
         }
     }
 
 
     protected View findMyViewById(int id) {
         if (device.optionViaDialog()) {
             return dialog.findViewById(id);
         } else {
             return findViewById(id);
         }
     }
 
     public void onAnimatorCancel() {
         if (!device.optionViaDialog()){
             animator.setDisplayedChild(MAIN_SCREEN);
         } else {
             dialog.cancel();
         }
     }
 
     @Override
     protected void onApplyAction() {
         if (globalOptions.isApplyAndClose()) {
             onAnimatorCancel();
         }
     }
 
     public void initRotationScreen() {
         final RadioGroup rotationGroup = (RadioGroup) findMyViewById(R.id.rotationGroup);
 
         rotationGroup.check(R.id.rotate0);
 
         getSubscriptionManager().addDocListeners(new DocumentViewAdapter() {
             @Override
             public void documentOpened(Controller controller) {
                 updateRotation();
             }
         });
 
 
         ImageButton apply = (ImageButton) findMyViewById(R.id.rotation_apply);
         apply.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 onApplyAction();
                 int id = rotationGroup.getCheckedRadioButtonId();
                 controller.setRotation(id == R.id.rotate0 ? 0 : id == R.id.rotate90 ? -1 : 1);
             }
         });
 
         ImageButton cancel = (ImageButton) findMyViewById(R.id.rotation_close);
         cancel.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 onAnimatorCancel();
                 updateRotation();
             }
         });
     }
 
     void updateRotation() {
         RadioGroup rotationGroup = (RadioGroup) findMyViewById(R.id.rotationGroup);
         rotationGroup.check(controller.getRotation() == 0 ? R.id.rotate0 : controller.getRotation() == -1 ? R.id.rotate90 : R.id.rotate270);
     }
 
     @Override
     public int getViewerType() {
         return Device.VIEWER_ACTIVITY;
     }
 
     public GlobalOptions getGlobalOptions() {
         return globalOptions;
     }
 
     public Controller getController() {
         return controller;
     }
 }
