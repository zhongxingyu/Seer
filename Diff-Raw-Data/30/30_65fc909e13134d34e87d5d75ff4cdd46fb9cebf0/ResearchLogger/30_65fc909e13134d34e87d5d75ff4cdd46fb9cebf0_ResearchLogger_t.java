 /*
  * Copyright (C) 2012 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.android.inputmethod.latin;
 
 import static com.android.inputmethod.latin.Constants.Subtype.ExtraValue.KEYBOARD_LAYOUT_SET;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.inputmethodservice.InputMethodService;
 import android.os.Build;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.Process;
 import android.os.SystemClock;
 import android.text.TextUtils;
 import android.util.JsonWriter;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.inputmethod.CompletionInfo;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputConnection;
 import android.widget.Toast;
 
 import com.android.inputmethod.keyboard.Key;
 import com.android.inputmethod.keyboard.Keyboard;
 import com.android.inputmethod.keyboard.KeyboardId;
import com.android.inputmethod.latin.RichInputConnection.Range;
 import com.android.inputmethod.latin.SuggestedWords.SuggestedWordInfo;
 import com.android.inputmethod.latin.define.ProductionFlag;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Map;
 import java.util.UUID;
 
 /**
  * Logs the use of the LatinIME keyboard.
  *
  * This class logs operations on the IME keyboard, including what the user has typed.
  * Data is stored locally in a file in app-specific storage.
  *
  * This functionality is off by default. See {@link ProductionFlag#IS_EXPERIMENTAL}.
  */
 public class ResearchLogger implements SharedPreferences.OnSharedPreferenceChangeListener {
     private static final String TAG = ResearchLogger.class.getSimpleName();
     private static final boolean DEBUG = false;
     /* package */ static boolean sIsLogging = false;
     private static final int OUTPUT_FORMAT_VERSION = 1;
     private static final String PREF_USABILITY_STUDY_MODE = "usability_study_mode";
     private static final String FILENAME_PREFIX = "researchLog";
     private static final String FILENAME_SUFFIX = ".txt";
     private static final JsonWriter NULL_JSON_WRITER = new JsonWriter(
             new OutputStreamWriter(new NullOutputStream()));
     private static final SimpleDateFormat TIMESTAMP_DATEFORMAT =
             new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
 
     // constants related to specific log points
     private static final String WHITESPACE_SEPARATORS = " \t\n\r";
     private static final int MAX_INPUTVIEW_LENGTH_TO_CAPTURE = 8192; // must be >=1
     private static final String PREF_RESEARCH_LOGGER_UUID_STRING = "pref_research_logger_uuid";
 
     private static final ResearchLogger sInstance = new ResearchLogger();
     private HandlerThread mHandlerThread;
     /* package */ Handler mLoggingHandler;
     // to write to a different filename, e.g., for testing, set mFile before calling start()
     private File mFilesDir;
     /* package */ File mFile;
     private JsonWriter mJsonWriter = NULL_JSON_WRITER; // should never be null
 
     private int mLoggingState;
     private static final int LOGGING_STATE_OFF = 0;
     private static final int LOGGING_STATE_ON = 1;
     private static final int LOGGING_STATE_STOPPING = 2;
 
     // set when LatinIME should ignore an onUpdateSelection() callback that
     // arises from operations in this class
     private static boolean sLatinIMEExpectingUpdateSelection = false;
 
     private static class NullOutputStream extends OutputStream {
         /** {@inheritDoc} */
         @Override
         public void write(byte[] buffer, int offset, int count) {
             // nop
         }
 
         /** {@inheritDoc} */
         @Override
         public void write(byte[] buffer) {
             // nop
         }
 
         @Override
         public void write(int oneByte) {
         }
     }
 
     private ResearchLogger() {
         mLoggingState = LOGGING_STATE_OFF;
     }
 
     public static ResearchLogger getInstance() {
         return sInstance;
     }
 
     public void init(final InputMethodService ims, final SharedPreferences prefs) {
         assert ims != null;
         if (ims == null) {
             Log.w(TAG, "IMS is null; logging is off");
         } else {
             mFilesDir = ims.getFilesDir();
             if (mFilesDir == null || !mFilesDir.exists()) {
                 Log.w(TAG, "IME storage directory does not exist.");
             }
         }
         if (prefs != null) {
             sIsLogging = prefs.getBoolean(PREF_USABILITY_STUDY_MODE, false);
             prefs.registerOnSharedPreferenceChangeListener(this);
         }
     }
 
     public synchronized void start() {
         Log.d(TAG, "start called");
         if (!sIsLogging) {
             // Log.w(TAG, "not in usability mode; not logging");
             return;
         }
         if (mFilesDir == null || !mFilesDir.exists()) {
             Log.w(TAG, "IME storage directory does not exist.  Cannot start logging.");
         } else {
             if (mHandlerThread == null || !mHandlerThread.isAlive()) {
                 mHandlerThread = new HandlerThread("ResearchLogger logging task",
                         Process.THREAD_PRIORITY_BACKGROUND);
                 mHandlerThread.start();
                 mLoggingHandler = null;
                 mLoggingState = LOGGING_STATE_OFF;
             }
             if (mLoggingHandler == null) {
                 mLoggingHandler = new Handler(mHandlerThread.getLooper());
                 mLoggingState = LOGGING_STATE_OFF;
             }
             if (mFile == null) {
                 final String timestampString = TIMESTAMP_DATEFORMAT.format(new Date());
                 mFile = new File(mFilesDir, FILENAME_PREFIX + timestampString + FILENAME_SUFFIX);
             }
             if (mLoggingState == LOGGING_STATE_OFF) {
                 try {
                     mJsonWriter = new JsonWriter(new BufferedWriter(new FileWriter(mFile)));
                     mJsonWriter.setLenient(true);
                     mJsonWriter.beginArray();
                     mLoggingState = LOGGING_STATE_ON;
                 } catch (IOException e) {
                     Log.w(TAG, "cannot start JsonWriter");
                     mJsonWriter = NULL_JSON_WRITER;
                     e.printStackTrace();
                 }
             }
         }
     }
 
     public synchronized void stop() {
         Log.d(TAG, "stop called");
         if (mLoggingHandler != null && mLoggingState == LOGGING_STATE_ON) {
             mLoggingState = LOGGING_STATE_STOPPING;
             // put this in the Handler queue so pending writes are processed first.
             mLoggingHandler.post(new Runnable() {
                 @Override
                 public void run() {
                     try {
                         Log.d(TAG, "closing jsonwriter");
                         mJsonWriter.endArray();
                         mJsonWriter.flush();
                         mJsonWriter.close();
                     } catch (IllegalStateException e1) {
                         // assume that this is just the json not being terminated properly.
                         // ignore
                         e1.printStackTrace();
                     } catch (IOException e) {
                         e.printStackTrace();
                     } finally {
                         mJsonWriter = NULL_JSON_WRITER;
                         mFile = null;
                         mLoggingState = LOGGING_STATE_OFF;
                         if (DEBUG) {
                             Log.d(TAG, "logfile closed");
                         }
                         Log.d(TAG, "finished stop(), notifying");
                         synchronized (ResearchLogger.this) {
                             ResearchLogger.this.notify();
                         }
                     }
                 }
             });
             try {
                 wait();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     }
 
     public synchronized boolean abort() {
         Log.d(TAG, "abort called");
         boolean isLogFileDeleted = false;
         if (mLoggingHandler != null && mLoggingState == LOGGING_STATE_ON) {
             mLoggingState = LOGGING_STATE_STOPPING;
             try {
                 Log.d(TAG, "closing jsonwriter");
                 mJsonWriter.endArray();
                 mJsonWriter.close();
             } catch (IllegalStateException e1) {
                 // assume that this is just the json not being terminated properly.
                 // ignore
                 e1.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 mJsonWriter = NULL_JSON_WRITER;
                 // delete file
                 final boolean isDeleted = mFile.delete();
                 if (isDeleted) {
                     isLogFileDeleted = true;
                 }
                 mFile = null;
                 mLoggingState = LOGGING_STATE_OFF;
                 if (DEBUG) {
                     Log.d(TAG, "logfile closed");
                 }
             }
         }
         return isLogFileDeleted;
     }
 
     /* package */ synchronized void flush() {
         try {
             mJsonWriter.flush();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
         if (key == null || prefs == null) {
             return;
         }
         sIsLogging = prefs.getBoolean(PREF_USABILITY_STUDY_MODE, false);
         if (sIsLogging == false) {
             abort();
         }
     }
 
     /* package */ void presentResearchDialog(final LatinIME latinIME) {
         final CharSequence title = latinIME.getString(R.string.english_ime_research_log);
         final CharSequence[] items = new CharSequence[] {
                 latinIME.getString(R.string.note_timestamp_for_researchlog),
                 latinIME.getString(R.string.do_not_log_this_session),
         };
         final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface di, int position) {
                 di.dismiss();
                 switch (position) {
                     case 0:
                         ResearchLogger.getInstance().userTimestamp();
                         Toast.makeText(latinIME, R.string.notify_recorded_timestamp,
                                 Toast.LENGTH_LONG).show();
                         break;
                     case 1:
                         Toast toast = Toast.makeText(latinIME,
                                 R.string.notify_session_log_deleting, Toast.LENGTH_LONG);
                         toast.show();
                         final ResearchLogger logger = ResearchLogger.getInstance();
                         boolean isLogDeleted = logger.abort();
                         toast.cancel();
                         if (isLogDeleted) {
                             Toast.makeText(latinIME, R.string.notify_session_log_deleted,
                                     Toast.LENGTH_LONG).show();
                         } else {
                             Toast.makeText(latinIME,
                                     R.string.notify_session_log_not_deleted, Toast.LENGTH_LONG)
                                     .show();
                         }
                         break;
                 }
             }
         };
         final AlertDialog.Builder builder = new AlertDialog.Builder(latinIME)
                 .setItems(items, listener)
                 .setTitle(title);
         latinIME.showOptionDialog(builder.create());
     }
 
     private static final String CURRENT_TIME_KEY = "_ct";
     private static final String UPTIME_KEY = "_ut";
     private static final String EVENT_TYPE_KEY = "_ty";
     private static final Object[] EVENTKEYS_NULLVALUES = {};
 
     /**
      * Write a description of the event out to the ResearchLog.
      *
      * Runs in the background to avoid blocking the UI thread.
      *
      * @param keys an array containing a descriptive name for the event, followed by the keys
      * @param values an array of values, either a String or Number.  length should be one
      * less than the keys array
      */
     private synchronized void writeEvent(final String[] keys, final Object[] values) {
         assert values.length + 1 == keys.length;
         if (mLoggingState == LOGGING_STATE_ON) {
             mLoggingHandler.post(new Runnable() {
                 @Override
                 public void run() {
                     try {
                         mJsonWriter.beginObject();
                         mJsonWriter.name(CURRENT_TIME_KEY).value(System.currentTimeMillis());
                         mJsonWriter.name(UPTIME_KEY).value(SystemClock.uptimeMillis());
                         mJsonWriter.name(EVENT_TYPE_KEY).value(keys[0]);
                         final int length = values.length;
                         for (int i = 0; i < length; i++) {
                             mJsonWriter.name(keys[i + 1]);
                             Object value = values[i];
                             if (value instanceof String) {
                                 mJsonWriter.value((String) value);
                             } else if (value instanceof Number) {
                                 mJsonWriter.value((Number) value);
                             } else if (value instanceof Boolean) {
                                 mJsonWriter.value((Boolean) value);
                             } else if (value instanceof CompletionInfo[]) {
                                 CompletionInfo[] ci = (CompletionInfo[]) value;
                                 mJsonWriter.beginArray();
                                 for (int j = 0; j < ci.length; j++) {
                                     mJsonWriter.value(ci[j].toString());
                                 }
                                 mJsonWriter.endArray();
                             } else if (value instanceof SharedPreferences) {
                                 SharedPreferences prefs = (SharedPreferences) value;
                                 mJsonWriter.beginObject();
                                 for (Map.Entry<String,?> entry : prefs.getAll().entrySet()) {
                                     mJsonWriter.name(entry.getKey());
                                     final Object innerValue = entry.getValue();
                                     if (innerValue == null) {
                                         mJsonWriter.nullValue();
                                     } else if (innerValue instanceof Boolean) {
                                         mJsonWriter.value((Boolean) innerValue);
                                     } else if (innerValue instanceof Number) {
                                         mJsonWriter.value((Number) innerValue);
                                     } else {
                                         mJsonWriter.value(innerValue.toString());
                                     }
                                 }
                                 mJsonWriter.endObject();
                             } else if (value instanceof Key[]) {
                                 Key[] keys = (Key[]) value;
                                 mJsonWriter.beginArray();
                                 for (Key key : keys) {
                                     mJsonWriter.beginObject();
                                     mJsonWriter.name("code").value(key.mCode);
                                     mJsonWriter.name("altCode").value(key.mAltCode);
                                     mJsonWriter.name("x").value(key.mX);
                                     mJsonWriter.name("y").value(key.mY);
                                     mJsonWriter.name("w").value(key.mWidth);
                                     mJsonWriter.name("h").value(key.mHeight);
                                     mJsonWriter.endObject();
                                 }
                                 mJsonWriter.endArray();
                             } else if (value instanceof SuggestedWords) {
                                 SuggestedWords words = (SuggestedWords) value;
                                 mJsonWriter.beginObject();
                                 mJsonWriter.name("typedWordValid").value(words.mTypedWordValid);
                                 mJsonWriter.name("hasAutoCorrectionCandidate")
                                     .value(words.mHasAutoCorrectionCandidate);
                                 mJsonWriter.name("isPunctuationSuggestions")
                                     .value(words.mIsPunctuationSuggestions);
                                 mJsonWriter.name("allowsToBeAutoCorrected")
                                     .value(words.mAllowsToBeAutoCorrected);
                                 mJsonWriter.name("isObsoleteSuggestions")
                                     .value(words.mIsObsoleteSuggestions);
                                 mJsonWriter.name("isPrediction")
                                     .value(words.mIsPrediction);
                                 mJsonWriter.name("words");
                                 mJsonWriter.beginArray();
                                 final int size = words.size();
                                 for (int j = 0; j < size; j++) {
                                     SuggestedWordInfo wordInfo = words.getWordInfo(j);
                                     mJsonWriter.value(wordInfo.toString());
                                 }
                                 mJsonWriter.endArray();
                                 mJsonWriter.endObject();
                             } else if (value == null) {
                                 mJsonWriter.nullValue();
                             } else {
                                 Log.w(TAG, "Unrecognized type to be logged: " +
                                         (value == null ? "<null>" : value.getClass().getName()));
                                 mJsonWriter.nullValue();
                             }
                         }
                         mJsonWriter.endObject();
                     } catch (IOException e) {
                         e.printStackTrace();
                         Log.w(TAG, "Error in JsonWriter; disabling logging");
                         try {
                             mJsonWriter.close();
                         } catch (IllegalStateException e1) {
                             // assume that this is just the json not being terminated properly.
                             // ignore
                         } catch (IOException e1) {
                             e1.printStackTrace();
                         } finally {
                             mJsonWriter = NULL_JSON_WRITER;
                         }
                     }
                 }
             });
         }
     }
 
     private static final String[] EVENTKEYS_LATINKEYBOARDVIEW_PROCESSMOTIONEVENT = {
         "LatinKeyboardViewProcessMotionEvent", "action", "eventTime", "id", "x", "y", "size",
         "pressure"
     };
     public static void latinKeyboardView_processMotionEvent(final MotionEvent me, final int action,
             final long eventTime, final int index, final int id, final int x, final int y) {
         if (me != null) {
             final String actionString;
             switch (action) {
                 case MotionEvent.ACTION_CANCEL: actionString = "CANCEL"; break;
                 case MotionEvent.ACTION_UP: actionString = "UP"; break;
                 case MotionEvent.ACTION_DOWN: actionString = "DOWN"; break;
                 case MotionEvent.ACTION_POINTER_UP: actionString = "POINTER_UP"; break;
                 case MotionEvent.ACTION_POINTER_DOWN: actionString = "POINTER_DOWN"; break;
                 case MotionEvent.ACTION_MOVE: actionString = "MOVE"; break;
                 case MotionEvent.ACTION_OUTSIDE: actionString = "OUTSIDE"; break;
                 default: actionString = "ACTION_" + action; break;
             }
             final float size = me.getSize(index);
             final float pressure = me.getPressure(index);
             final Object[] values = {
                 actionString, eventTime, id, x, y, size, pressure
             };
             getInstance().writeEvent(EVENTKEYS_LATINKEYBOARDVIEW_PROCESSMOTIONEVENT, values);
         }
     }
 
     private static final String[] EVENTKEYS_LATINIME_ONCODEINPUT = {
         "LatinIMEOnCodeInput", "code", "x", "y"
     };
     public static void latinIME_onCodeInput(final int code, final int x, final int y) {
         final Object[] values = {
             Keyboard.printableCode(code), x, y
         };
         getInstance().writeEvent(EVENTKEYS_LATINIME_ONCODEINPUT, values);
     }
 
     private static final String[] EVENTKEYS_CORRECTION = {
         "LogCorrection", "subgroup", "before", "after", "position"
     };
     public static void logCorrection(final String subgroup, final String before, final String after,
             final int position) {
         final Object[] values = {
             subgroup, before, after, position
         };
         getInstance().writeEvent(EVENTKEYS_CORRECTION, values);
     }
 
     private static final String[] EVENTKEYS_LATINIME_COMMITCURRENTAUTOCORRECTION = {
         "LatinIMECommitCurrentAutoCorrection", "typedWord", "autoCorrection"
     };
     public static void latinIME_commitCurrentAutoCorrection(final String typedWord,
             final String autoCorrection) {
         final Object[] values = {
             typedWord, autoCorrection
         };
         getInstance().writeEvent(EVENTKEYS_LATINIME_COMMITCURRENTAUTOCORRECTION, values);
     }
 
     private static final String[] EVENTKEYS_LATINIME_COMMITTEXT = {
         "LatinIMECommitText", "typedWord"
     };
     public static void latinIME_commitText(final CharSequence typedWord) {
         final Object[] values = {
             typedWord.toString()
         };
         getInstance().writeEvent(EVENTKEYS_LATINIME_COMMITTEXT, values);
     }
 
     private static final String[] EVENTKEYS_LATINIME_DELETESURROUNDINGTEXT = {
         "LatinIMEDeleteSurroundingText", "length"
     };
     public static void latinIME_deleteSurroundingText(final int length) {
         final Object[] values = {
             length
         };
         getInstance().writeEvent(EVENTKEYS_LATINIME_DELETESURROUNDINGTEXT, values);
     }
 
     private static final String[] EVENTKEYS_LATINIME_DOUBLESPACEAUTOPERIOD = {
         "LatinIMEDoubleSpaceAutoPeriod"
     };
     public static void latinIME_doubleSpaceAutoPeriod() {
         getInstance().writeEvent(EVENTKEYS_LATINIME_DOUBLESPACEAUTOPERIOD, EVENTKEYS_NULLVALUES);
     }
 
     private static final String[] EVENTKEYS_LATINIME_ONDISPLAYCOMPLETIONS = {
         "LatinIMEOnDisplayCompletions", "applicationSpecifiedCompletions"
     };
     public static void latinIME_onDisplayCompletions(
             final CompletionInfo[] applicationSpecifiedCompletions) {
         final Object[] values = {
             applicationSpecifiedCompletions
         };
         getInstance().writeEvent(EVENTKEYS_LATINIME_ONDISPLAYCOMPLETIONS, values);
     }
 
     /* package */ static boolean getAndClearLatinIMEExpectingUpdateSelection() {
         boolean returnValue = sLatinIMEExpectingUpdateSelection;
         sLatinIMEExpectingUpdateSelection = false;
         return returnValue;
     }
 
     private static final String[] EVENTKEYS_LATINIME_ONWINDOWHIDDEN = {
         "LatinIMEOnWindowHidden", "isTextTruncated", "text"
     };
     public static void latinIME_onWindowHidden(final int savedSelectionStart,
             final int savedSelectionEnd, final InputConnection ic) {
         if (ic != null) {
             ic.beginBatchEdit();
             ic.performContextMenuAction(android.R.id.selectAll);
             CharSequence charSequence = ic.getSelectedText(0);
             ic.setSelection(savedSelectionStart, savedSelectionEnd);
             ic.endBatchEdit();
             sLatinIMEExpectingUpdateSelection = true;
             Object[] values = new Object[2];
             if (TextUtils.isEmpty(charSequence)) {
                 values[0] = false;
                 values[1] = "";
             } else {
                 if (charSequence.length() > MAX_INPUTVIEW_LENGTH_TO_CAPTURE) {
                     int length = MAX_INPUTVIEW_LENGTH_TO_CAPTURE;
                     // do not cut in the middle of a supplementary character
                     final char c = charSequence.charAt(length - 1);
                     if (Character.isHighSurrogate(c)) {
                         length--;
                     }
                     final CharSequence truncatedCharSequence = charSequence.subSequence(0, length);
                     values[0] = true;
                     values[1] = truncatedCharSequence.toString();
                 } else {
                     values[0] = false;
                     values[1] = charSequence.toString();
                 }
             }
             getInstance().writeEvent(EVENTKEYS_LATINIME_ONWINDOWHIDDEN, values);
         }
     }
 
     private static final String[] EVENTKEYS_LATINIME_ONSTARTINPUTVIEWINTERNAL = {
         "LatinIMEOnStartInputViewInternal", "uuid", "packageName", "inputType", "imeOptions",
         "fieldId", "display", "model", "prefs", "outputFormatVersion"
     };
     public static void latinIME_onStartInputViewInternal(final EditorInfo editorInfo,
             final SharedPreferences prefs) {
         if (editorInfo != null) {
             final Object[] values = {
                 getUUID(prefs), editorInfo.packageName, Integer.toHexString(editorInfo.inputType),
                 Integer.toHexString(editorInfo.imeOptions), editorInfo.fieldId, Build.DISPLAY,
                 Build.MODEL, prefs, OUTPUT_FORMAT_VERSION
             };
             getInstance().writeEvent(EVENTKEYS_LATINIME_ONSTARTINPUTVIEWINTERNAL, values);
         }
     }
 
     private static String getUUID(final SharedPreferences prefs) {
         String uuidString = prefs.getString(PREF_RESEARCH_LOGGER_UUID_STRING, null);
         if (null == uuidString) {
             UUID uuid = UUID.randomUUID();
             uuidString = uuid.toString();
             Editor editor = prefs.edit();
             editor.putString(PREF_RESEARCH_LOGGER_UUID_STRING, uuidString);
             editor.apply();
         }
         return uuidString;
     }
 
     private static final String[] EVENTKEYS_LATINIME_ONUPDATESELECTION = {
         "LatinIMEOnUpdateSelection", "lastSelectionStart", "lastSelectionEnd", "oldSelStart",
         "oldSelEnd", "newSelStart", "newSelEnd", "composingSpanStart", "composingSpanEnd",
         "expectingUpdateSelection", "expectingUpdateSelectionFromLogger", "context"
     };
     public static void latinIME_onUpdateSelection(final int lastSelectionStart,
             final int lastSelectionEnd, final int oldSelStart, final int oldSelEnd,
             final int newSelStart, final int newSelEnd, final int composingSpanStart,
             final int composingSpanEnd, final boolean expectingUpdateSelection,
             final boolean expectingUpdateSelectionFromLogger,
             final RichInputConnection connection) {
        String word = "";
        if (connection != null) {
            Range range = connection.getWordRangeAtCursor(WHITESPACE_SEPARATORS, 1);
            if (range != null) {
                word = range.mWord;
            }
        }
         final Object[] values = {
             lastSelectionStart, lastSelectionEnd, oldSelStart, oldSelEnd, newSelStart,
             newSelEnd, composingSpanStart, composingSpanEnd, expectingUpdateSelection,
            expectingUpdateSelectionFromLogger, word
         };
         getInstance().writeEvent(EVENTKEYS_LATINIME_ONUPDATESELECTION, values);
     }
 
     private static final String[] EVENTKEYS_LATINIME_PERFORMEDITORACTION = {
         "LatinIMEPerformEditorAction", "imeActionNext"
     };
     public static void latinIME_performEditorAction(final int imeActionNext) {
         final Object[] values = {
             imeActionNext
         };
         getInstance().writeEvent(EVENTKEYS_LATINIME_PERFORMEDITORACTION, values);
     }
 
     private static final String[] EVENTKEYS_LATINIME_PICKAPPLICATIONSPECIFIEDCOMPLETION = {
         "LatinIMEPickApplicationSpecifiedCompletion", "index", "text", "x", "y"
     };
     public static void latinIME_pickApplicationSpecifiedCompletion(final int index,
            final CharSequence cs, int x, int y) {
         final Object[] values = {
            index, cs, x, y
         };
         getInstance().writeEvent(EVENTKEYS_LATINIME_PICKAPPLICATIONSPECIFIEDCOMPLETION, values);
     }
 
     private static final String[] EVENTKEYS_LATINIME_PICKSUGGESTIONMANUALLY = {
         "LatinIMEPickSuggestionManually", "replacedWord", "index", "suggestion", "x", "y"
     };
     public static void latinIME_pickSuggestionManually(final String replacedWord,
             final int index, CharSequence suggestion, int x, int y) {
         final Object[] values = {
            replacedWord, index, suggestion, x, y
         };
         getInstance().writeEvent(EVENTKEYS_LATINIME_PICKSUGGESTIONMANUALLY, values);
     }
 
     private static final String[] EVENTKEYS_LATINIME_PUNCTUATIONSUGGESTION = {
         "LatinIMEPunctuationSuggestion", "index", "suggestion", "x", "y"
     };
     public static void latinIME_punctuationSuggestion(final int index,
             final CharSequence suggestion, int x, int y) {
         final Object[] values = {
            index, suggestion, x, y
         };
         getInstance().writeEvent(EVENTKEYS_LATINIME_PUNCTUATIONSUGGESTION, values);
     }
 
     private static final String[] EVENTKEYS_LATINIME_REVERTDOUBLESPACEWHILEINBATCHEDIT = {
         "LatinIMERevertDoubleSpaceWhileInBatchEdit"
     };
     public static void latinIME_revertDoubleSpaceWhileInBatchEdit() {
         getInstance().writeEvent(EVENTKEYS_LATINIME_REVERTDOUBLESPACEWHILEINBATCHEDIT,
                 EVENTKEYS_NULLVALUES);
     }
 
     private static final String[] EVENTKEYS_LATINIME_REVERTSWAPPUNCTUATION = {
         "LatinIMERevertSwapPunctuation"
     };
     public static void latinIME_revertSwapPunctuation() {
         getInstance().writeEvent(EVENTKEYS_LATINIME_REVERTSWAPPUNCTUATION, EVENTKEYS_NULLVALUES);
     }
 
     private static final String[] EVENTKEYS_LATINIME_SENDKEYCODEPOINT = {
         "LatinIMESendKeyCodePoint", "code"
     };
     public static void latinIME_sendKeyCodePoint(final int code) {
         final Object[] values = {
             code
         };
         getInstance().writeEvent(EVENTKEYS_LATINIME_SENDKEYCODEPOINT, values);
     }
 
     private static final String[] EVENTKEYS_LATINIME_SWAPSWAPPERANDSPACEWHILEINBATCHEDIT = {
         "LatinIMESwapSwapperAndSpaceWhileInBatchEdit"
     };
     public static void latinIME_swapSwapperAndSpaceWhileInBatchEdit() {
         getInstance().writeEvent(EVENTKEYS_LATINIME_SWAPSWAPPERANDSPACEWHILEINBATCHEDIT,
                 EVENTKEYS_NULLVALUES);
     }
 
     private static final String[] EVENTKEYS_LATINIME_SWITCHTOKEYBOARDVIEW = {
         "LatinIMESwitchToKeyboardView"
     };
     public static void latinIME_switchToKeyboardView() {
         getInstance().writeEvent(EVENTKEYS_LATINIME_SWITCHTOKEYBOARDVIEW, EVENTKEYS_NULLVALUES);
     }
 
     private static final String[] EVENTKEYS_LATINKEYBOARDVIEW_ONLONGPRESS = {
         "LatinKeyboardViewOnLongPress"
     };
     public static void latinKeyboardView_onLongPress() {
         getInstance().writeEvent(EVENTKEYS_LATINKEYBOARDVIEW_ONLONGPRESS, EVENTKEYS_NULLVALUES);
     }
 
     private static final String[] EVENTKEYS_LATINKEYBOARDVIEW_SETKEYBOARD = {
         "LatinKeyboardViewSetKeyboard", "elementId", "locale", "orientation", "width",
         "modeName", "action", "navigateNext", "navigatePrevious", "clobberSettingsKey",
         "passwordInput", "shortcutKeyEnabled", "hasShortcutKey", "languageSwitchKeyEnabled",
         "isMultiLine", "tw", "th", "keys"
     };
     public static void latinKeyboardView_setKeyboard(final Keyboard keyboard) {
         if (keyboard != null) {
             final KeyboardId kid = keyboard.mId;
             final Object[] values = {
                     KeyboardId.elementIdToName(kid.mElementId),
                     kid.mLocale + ":" + kid.mSubtype.getExtraValueOf(KEYBOARD_LAYOUT_SET),
                     kid.mOrientation,
                     kid.mWidth,
                     KeyboardId.modeName(kid.mMode),
                     kid.imeAction(),
                     kid.navigateNext(),
                     kid.navigatePrevious(),
                     kid.mClobberSettingsKey,
                     kid.passwordInput(),
                     kid.mShortcutKeyEnabled,
                     kid.mHasShortcutKey,
                     kid.mLanguageSwitchKeyEnabled,
                     kid.isMultiLine(),
                     keyboard.mOccupiedWidth,
                     keyboard.mOccupiedHeight,
                     keyboard.mKeys
                 };
             getInstance().writeEvent(EVENTKEYS_LATINKEYBOARDVIEW_SETKEYBOARD, values);
         }
     }
 
     private static final String[] EVENTKEYS_LATINIME_REVERTCOMMIT = {
         "LatinIMERevertCommit", "originallyTypedWord"
     };
     public static void latinIME_revertCommit(final String originallyTypedWord) {
         final Object[] values = {
             originallyTypedWord
         };
         getInstance().writeEvent(EVENTKEYS_LATINIME_REVERTCOMMIT, values);
     }
 
     private static final String[] EVENTKEYS_POINTERTRACKER_CALLLISTENERONCANCELINPUT = {
         "PointerTrackerCallListenerOnCancelInput"
     };
     public static void pointerTracker_callListenerOnCancelInput() {
         getInstance().writeEvent(EVENTKEYS_POINTERTRACKER_CALLLISTENERONCANCELINPUT,
                 EVENTKEYS_NULLVALUES);
     }
 
     private static final String[] EVENTKEYS_POINTERTRACKER_CALLLISTENERONCODEINPUT = {
         "PointerTrackerCallListenerOnCodeInput", "code", "outputText", "x", "y",
         "ignoreModifierKey", "altersCode", "isEnabled"
     };
     public static void pointerTracker_callListenerOnCodeInput(final Key key, final int x,
             final int y, final boolean ignoreModifierKey, final boolean altersCode,
             final int code) {
         if (key != null) {
             CharSequence outputText = key.mOutputText;
             final Object[] values = {
                Keyboard.printableCode(code), outputText, x, y, ignoreModifierKey, altersCode,
                key.isEnabled()
             };
             getInstance().writeEvent(EVENTKEYS_POINTERTRACKER_CALLLISTENERONCODEINPUT, values);
         }
     }
 
     private static final String[] EVENTKEYS_POINTERTRACKER_CALLLISTENERONRELEASE = {
         "PointerTrackerCallListenerOnRelease", "code", "withSliding", "ignoreModifierKey",
         "isEnabled"
     };
     public static void pointerTracker_callListenerOnRelease(final Key key, final int primaryCode,
             final boolean withSliding, final boolean ignoreModifierKey) {
         if (key != null) {
             final Object[] values = {
                 Keyboard.printableCode(primaryCode), withSliding, ignoreModifierKey,
                 key.isEnabled()
             };
             getInstance().writeEvent(EVENTKEYS_POINTERTRACKER_CALLLISTENERONRELEASE, values);
         }
     }
 
     private static final String[] EVENTKEYS_POINTERTRACKER_ONDOWNEVENT = {
         "PointerTrackerOnDownEvent", "deltaT", "distanceSquared"
     };
     public static void pointerTracker_onDownEvent(long deltaT, int distanceSquared) {
         final Object[] values = {
             deltaT, distanceSquared
         };
         getInstance().writeEvent(EVENTKEYS_POINTERTRACKER_ONDOWNEVENT, values);
     }
 
     private static final String[] EVENTKEYS_POINTERTRACKER_ONMOVEEVENT = {
         "PointerTrackerOnMoveEvent", "x", "y", "lastX", "lastY"
     };
     public static void pointerTracker_onMoveEvent(final int x, final int y, final int lastX,
             final int lastY) {
         final Object[] values = {
             x, y, lastX, lastY
         };
         getInstance().writeEvent(EVENTKEYS_POINTERTRACKER_ONMOVEEVENT, values);
     }
 
     private static final String[] EVENTKEYS_SUDDENJUMPINGTOUCHEVENTHANDLER_ONTOUCHEVENT = {
         "SuddenJumpingTouchEventHandlerOnTouchEvent", "motionEvent"
     };
     public static void suddenJumpingTouchEventHandler_onTouchEvent(final MotionEvent me) {
         if (me != null) {
             final Object[] values = {
                 me.toString()
             };
             getInstance().writeEvent(EVENTKEYS_SUDDENJUMPINGTOUCHEVENTHANDLER_ONTOUCHEVENT,
                     values);
         }
     }
 
     private static final String[] EVENTKEYS_SUGGESTIONSVIEW_SETSUGGESTIONS = {
         "SuggestionsViewSetSuggestions", "suggestedWords"
     };
     public static void suggestionsView_setSuggestions(final SuggestedWords suggestedWords) {
         if (suggestedWords != null) {
             final Object[] values = {
                 suggestedWords
             };
             getInstance().writeEvent(EVENTKEYS_SUGGESTIONSVIEW_SETSUGGESTIONS, values);
         }
     }
 
     private static final String[] EVENTKEYS_USER_TIMESTAMP = {
         "UserTimestamp"
     };
     public void userTimestamp() {
         getInstance().writeEvent(EVENTKEYS_USER_TIMESTAMP, EVENTKEYS_NULLVALUES);
     }
 }
