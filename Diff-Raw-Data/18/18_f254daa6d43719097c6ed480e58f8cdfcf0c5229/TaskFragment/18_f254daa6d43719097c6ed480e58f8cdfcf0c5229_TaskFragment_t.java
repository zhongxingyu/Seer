 package com.gris.ege.pager;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import com.gris.ege.R;
 import com.gris.ege.activity.CalculateActivity;
 import com.gris.ege.db.ResultsOpenHelper;
 import com.gris.ege.other.GlobalData;
 import com.gris.ege.other.Task;
 import com.gris.ege.other.Utils;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.text.InputType;
 import com.gris.ege.other.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.webkit.WebSettings;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewAnimator;
 
 public class TaskFragment extends Fragment implements OnClickListener
 {
     private static final String TAG="TaskFragment";
 
     private static final int PAGE_DOWNLOAD    = 0;
     private static final int PAGE_RETRY       = 1;
     private static final int PAGE_IMAGE       = 2;
 
 
 
     private TextView       mTaskHeaderView;
     private TextView       mTaskStatusView;
     private ViewAnimator   mTaskViewAnimator;
     private Button         mRetryButton;
     private TaskWebView    mTaskWebView;
     private TextView       mAnswerTextView;
     private RelativeLayout mBottomLayout;
     private EditText       mAnswerEditText;
     private Button         mAnswerButton;
 
     private Task mTask;
 
 
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         Bundle aArgs=getArguments();
 
         if (aArgs!=null)
         {
             mTask=GlobalData.tasks.get(aArgs.getInt(GlobalData.TASK_ID));
         }
         else
         {
             Log.w(TAG, "There is no arguments");
             mTask=GlobalData.tasks.get(0);
         }
     }
 
     @Override
     public View onCreateView(LayoutInflater aInflater, ViewGroup aContainer, Bundle aSavedInstanceState)
     {
         View aView=aInflater.inflate(R.layout.task_page_item, aContainer, false);
 
         // Get controls
         mTaskHeaderView   = (TextView)      aView.findViewById(R.id.taskHeaderTextView);
         mTaskStatusView   = (TextView)      aView.findViewById(R.id.taskStatusTextView);
         mTaskViewAnimator = (ViewAnimator)  aView.findViewById(R.id.taskViewAnimator);
         mRetryButton      = (Button)        aView.findViewById(R.id.retryButton);
         mTaskWebView      = (TaskWebView)   aView.findViewById(R.id.taskWebView);
         mAnswerTextView   = (TextView)      aView.findViewById(R.id.answerTextView);
         mBottomLayout     = (RelativeLayout)aView.findViewById(R.id.bottomLayout);
         mAnswerEditText   = (EditText)      aView.findViewById(R.id.answerEditText);
         mAnswerButton     = (Button)        aView.findViewById(R.id.answerButton);
 
         // Set listeners
         mRetryButton.setOnClickListener(this);
         mAnswerButton.setOnClickListener(this);
 
         // Initialize controls
         mTaskHeaderView.setText(getString(R.string.task_header, mTask.getCategory(), mTask.getId()+1));
         updateStatus();
 
         mTaskWebView.setInitialScale(30);
 
         WebSettings aSettings= mTaskWebView.getSettings();
         aSettings.setBuiltInZoomControls(true);
         aSettings.setSupportZoom(true);
         aSettings.setUseWideViewPort(true);
 
         downloadImage();
 
         switch (getCalculateActivity().getMode())
         {
             case CalculateActivity.MODE_VIEW_TASK:
                 mAnswerTextView.setVisibility(View.GONE);
             break;
             case CalculateActivity.MODE_TEST_TASK:
             case CalculateActivity.MODE_VERIFICATION:
                 mAnswerTextView.setVisibility(View.GONE);
                 mAnswerButton.setVisibility(View.GONE);
             break;
             case CalculateActivity.MODE_VIEW_RESULT:
                 mBottomLayout.setVisibility(View.GONE);
                 mAnswerTextView.setText(getString(R.string.answer, mTask.getAnswer()));
             break;
         }
 
         if (mTask.getCategory().charAt(0)=='A')
         {
             mAnswerEditText.setRawInputType(
                                             InputType.TYPE_CLASS_NUMBER
                                             |
                                             InputType.TYPE_NUMBER_FLAG_SIGNED
                                             |
                                             InputType.TYPE_NUMBER_FLAG_DECIMAL
                                            );
 
             mAnswerEditText.setSingleLine(true);
         }
         else
         if (mTask.getCategory().charAt(0)=='B')
         {
             mAnswerEditText.setRawInputType(
                                             InputType.TYPE_CLASS_NUMBER
                                             |
                                             InputType.TYPE_NUMBER_FLAG_SIGNED
                                             |
                                             InputType.TYPE_NUMBER_FLAG_DECIMAL
                                            );
 
             mAnswerEditText.setSingleLine(true);
         }
         else
         if (mTask.getCategory().charAt(0)=='C')
         {
             mAnswerEditText.setRawInputType(
                                             InputType.TYPE_CLASS_TEXT
                                             |
                                             InputType.TYPE_TEXT_VARIATION_NORMAL
                                             |
                                             InputType.TYPE_TEXT_FLAG_MULTI_LINE
                                            );
 
             mAnswerEditText.setSingleLine(false);
         }
         else
         {
             Log.e(TAG, "Invalid category \""+mTask.getCategory()+"\" for task № "+String.valueOf(mTask.getId()));
         }
 
         return aView;
     }
 
     public void updateStatus()
     {
         if (
             getCalculateActivity().getMode()==CalculateActivity.MODE_TEST_TASK
             ||
             getCalculateActivity().getMode()==CalculateActivity.MODE_VERIFICATION
            )
         {
             mTaskStatusView.setVisibility(View.GONE);
         }
         else
         {
             mTaskStatusView.setVisibility(View.VISIBLE);
 
             if (mTask.isFinished())
             {
                 mTaskStatusView.setText(getString(getCalculateActivity().getMode()==CalculateActivity.MODE_VIEW_TASK? R.string.finished : R.string.correct));
                 mTaskStatusView.setTextColor(getResources().getColor(R.color.good));
             }
             else
             {
                String aBadText=getString(getCalculateActivity().getMode()==CalculateActivity.MODE_VIEW_TASK? R.string.not_finished : R.string.not_correct);

                if (mTask.getScore()>0)
                {
                    aBadText=aBadText+" ("+String.valueOf(mTask.getScore())+"/"+String.valueOf(mTask.getMaxScore())+")";
                }

                mTaskStatusView.setText(aBadText);
                 mTaskStatusView.setTextColor(getResources().getColor(R.color.bad));
             }
         }
     }
 
     public void downloadImage()
     {
         new DownloadImageTask().execute();
     }
 
     // Only allowed in MODE_VIEW_TASK and MODE_VERIFICATION
     public void checkAnswer(byte aScore)
     {
        if (mTask.getScore()<aScore)
        {
            mTask.setScore(aScore);
            updateStatus();
        }
 
         if (getCalculateActivity().getMode()==CalculateActivity.MODE_VIEW_TASK)
         {
             Toast.makeText(getActivity(), mTask.isFinished()? R.string.correct : R.string.not_correct, Toast.LENGTH_SHORT).show();
         }
 
         if (mTask.isFinished())
         {
             new ResultsOpenHelper(getActivity()).setTaskFinished(
                                                                  getCalculateActivity().getUserId(),
                                                                  getCalculateActivity().getLessonId(),
                                                                  mTask.getId()
                                                                 );
 
             if (getCalculateActivity().getMode()==CalculateActivity.MODE_VIEW_TASK)
             {
                 InputMethodManager imm=(InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                 imm.hideSoftInputFromWindow(mAnswerEditText.getWindowToken(), 0);
             }
         }
 
         if (getCalculateActivity().getMode()==CalculateActivity.MODE_VERIFICATION)
         {
             getCalculateActivity().getHandler().sendEmptyMessage(CalculateActivity.NEXT_AND_VERIFY);
         }
     }
 
     // Only allowed in MODE_VIEW_TASK and MODE_VERIFICATION
     public void checkAnswer()
     {
         if (mTask.getCategory().charAt(0)=='A')
         {
             checkAnswer(mTask.getAnswer().equalsIgnoreCase(getAnswer().trim()) ? mTask.getMaxScore() : (byte)0);
         }
         else
         if (mTask.getCategory().charAt(0)=='B')
         {
             checkAnswer(mTask.getAnswer().equalsIgnoreCase(getAnswer().trim()) ? mTask.getMaxScore() : (byte)0);
         }
         else
         if (mTask.getCategory().charAt(0)=='C')
         {
             final String aAnswer=mTask.getAnswer().trim();
 
             getCalculateActivity().removeProgressDialog();
 
             DialogFragment aCheckDialog;
 
             if (mTask.isSelfRating())
             {
                 aCheckDialog=new DialogFragment()
                 {
                     private SeekBar mResultSeekBar;
                     private TextView mResultScores;
                     private TextView mText;
                     private Button mOkButton;
 
                     public Dialog onCreateDialog(Bundle savedInstanceState)
                     {
                         final Dialog aDialog = new Dialog(getCalculateActivity());
                         aDialog.setContentView(R.layout.self_rating_dialog);
                         aDialog.setTitle(R.string.self_rating);
                         aDialog.setCancelable(getCalculateActivity().getMode()!=CalculateActivity.MODE_VERIFICATION);
 
                         // Get controls
                         mResultSeekBar = (SeekBar) aDialog.findViewById(R.id.resultSeekBar);
                         mResultScores  = (TextView)aDialog.findViewById(R.id.resultScores);
                         mText          = (TextView)aDialog.findViewById(R.id.dialogText);
                         mOkButton      = (Button)  aDialog.findViewById(R.id.okButton);
 
                         // Initialize controls
                         mResultSeekBar.setMax(mTask.getMaxScore());
                         mResultSeekBar.setProgress(mTask.getMaxScore());
                         mResultScores.setText(String.valueOf(mResultSeekBar.getProgress())+"/"+String.valueOf(mResultSeekBar.getMax()));
                         mText.setText(getString(R.string.is_it_correct, aAnswer));
 
                         // Set listeners
                         mResultSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
                         {
                             @Override
                             public void onStartTrackingTouch(SeekBar aSeekBar)
                             {}
 
                             @Override
                             public void onStopTrackingTouch(SeekBar aSeekBar)
                             {}
 
                             @Override
                             public void onProgressChanged(SeekBar aSeekBar, int aProgress, boolean aFromUser)
                             {
                                 mResultScores.setText(String.valueOf(mResultSeekBar.getProgress())+"/"+String.valueOf(mResultSeekBar.getMax()));
                             }
                         });
 
                         mOkButton.setOnClickListener(new OnClickListener()
                         {
                             @Override
                             public void onClick(View v)
                             {
                                 checkAnswer((byte)mResultSeekBar.getProgress());
                                 dismiss();
                             }
                         });
 
                         return aDialog;
                     }
                 };
             }
             else
             {
                 aCheckDialog=new DialogFragment()
                 {
                     public Dialog onCreateDialog(Bundle savedInstanceState)
                     {
                         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 
                         builder.setMessage(getString(R.string.is_it_correct, aAnswer))
                                .setPositiveButton(R.string.correct, new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        checkAnswer(mTask.getMaxScore());
                                    }
                                })
                                .setNegativeButton(R.string.not_correct, new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        checkAnswer((byte)0);
                                    }
                                })
                                .setCancelable(getCalculateActivity().getMode()!=CalculateActivity.MODE_VERIFICATION);
 
                         return builder.create();
                     }
                 };
             }
 
             aCheckDialog.show(getFragmentManager(), "CheckDialog");
         }
         else
         {
             Log.e(TAG, "Invalid category \""+mTask.getCategory()+"\" for task № "+String.valueOf(mTask.getId()));
         }
     }
 
     @Override
     public void onClick(View v)
     {
         switch (v.getId())
         {
             case R.id.retryButton:
                 downloadImage();
             break;
             case R.id.answerButton:
                 checkAnswer();
             break;
         }
     }
 
     public Task getTask()
     {
         return mTask;
     }
 
     public String getAnswer()
     {
         return mAnswerEditText.getText().toString();
     }
 
     public boolean isScaled()
     {
         return mTaskWebView.isScaled();
     }
 
     public CalculateActivity getCalculateActivity()
     {
         return (CalculateActivity)getActivity();
     }
 
     private class DownloadImageTask extends AsyncTask<Void, Void, String>
     {
         @Override
         protected void onPreExecute()
         {
             mTaskViewAnimator.setDisplayedChild(PAGE_DOWNLOAD);
         }
 
         @Override
         protected String doInBackground(Void... aNothing)
         {
             String res=null;
 
             try
             {
                 res=getImage();
             }
             catch (Exception e)
             {
                 Log.i(TAG, "Problem while downloading image", e);
             }
 
             return res;
         }
 
         private String getImage() throws IOException
         {
             String aFileName=GlobalData.selectedLesson.getId()+"/"+String.valueOf(mTask.getId()+1)+".png";
 
             if (new File(GlobalData.PATH_ON_SD_CARD+aFileName).exists())
             {
                 Drawable aDrawable=Drawable.createFromPath(GlobalData.PATH_ON_SD_CARD+aFileName);
 
                 if (aDrawable!=null)
                 {
                     return GlobalData.PATH_ON_SD_CARD+aFileName;
                 }
                 else
                 {
                     Log.i(TAG, "Invalid file on sdcard: "+GlobalData.PATH_ON_SD_CARD+aFileName);
                 }
             }
 
             if (Utils.checkWifiOrNet(getCalculateActivity()))
             {
                 // Download file
                 URL aUrl=new URL(GlobalData.PATH_ON_NET+aFileName);
 
                 HttpURLConnection aConnection=(HttpURLConnection)aUrl.openConnection();
                 aConnection.setReadTimeout(300000);
                 aConnection.setConnectTimeout(300000);
                 aConnection.setRequestMethod("GET");
                 aConnection.setDoInput(true);
 
                 aConnection.connect();
                 InputStream in=aConnection.getInputStream();
 
                 boolean aFromInternet=true;
 
                 try
                 {
                     byte[] aBuffer=new byte[4096];
 
                     new File(GlobalData.PATH_ON_SD_CARD+GlobalData.selectedLesson.getId()).mkdirs();
                     new File(GlobalData.PATH_ON_SD_CARD+".nomedia").createNewFile();
 
                     FileOutputStream aNewFile=new FileOutputStream(GlobalData.PATH_ON_SD_CARD+aFileName);
 
                     do
                     {
                         int aBytes=in.read(aBuffer);
 
                         if (aBytes<=0)
                         {
                             break;
                         }
 
                         aNewFile.write(aBuffer, 0, aBytes);
                     } while(true);
 
                     aNewFile.close();
 
                     aFromInternet=false;
                 }
                 catch (Exception e)
                 {
                     Log.w(TAG, "Problem while saving image on sd card", e);
                 }
 
                 try
                 {
                     in.close();
                 }
                 catch (Exception e)
                 {
                     Log.w(TAG, "Problem while saving image on sd card", e);
                 }
 
                 if (aFromInternet)
                 {
                     return GlobalData.PATH_ON_NET+aFileName;
                 }
                 else
                 {
                     Drawable aDrawable=Drawable.createFromPath(GlobalData.PATH_ON_SD_CARD+aFileName);
 
                     if (aDrawable!=null)
                     {
                         return GlobalData.PATH_ON_SD_CARD+aFileName;
                     }
                     else
                     {
                         Log.w(TAG, "Invalid file on SD card after downloading: "+GlobalData.PATH_ON_SD_CARD+aFileName);
                     }
                 }
             }
 
             return null;
         }
 
         @Override
         protected void onPostExecute(String aResult)
         {
             if (aResult!=null)
             {
                 mTaskWebView.loadUrl("file://"+aResult);
                 mTaskViewAnimator.setDisplayedChild(PAGE_IMAGE);
             }
             else
             {
                 mTaskViewAnimator.setDisplayedChild(PAGE_RETRY);
             }
         }
     }
 }
