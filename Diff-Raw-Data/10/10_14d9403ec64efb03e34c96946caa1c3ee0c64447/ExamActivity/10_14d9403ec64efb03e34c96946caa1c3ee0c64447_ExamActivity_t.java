 package com.osastudio.newshub;
 
 import com.huadi.azker_phone.R;
 import com.osastudio.newshub.data.NewsResult;
 import com.osastudio.newshub.data.exam.Exam;
 import com.osastudio.newshub.data.exam.ExamAnswer;
 import com.osastudio.newshub.data.exam.ExamInfo;
 import com.osastudio.newshub.data.exam.ExamReport;
 import com.osastudio.newshub.data.exam.Question;
 import com.osastudio.newshub.data.exam.QuestionList;
 import com.osastudio.newshub.net.ExamApi;
 import com.osastudio.newshub.utils.UIUtils;
 import com.osastudio.newshub.widgets.ExamIntroView;
 import com.osastudio.newshub.widgets.ExamLayout;
 import com.osastudio.newshub.widgets.ExamReportView;
 import com.osastudio.newshub.widgets.ExamView;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ViewSwitcher;
 import android.widget.ViewSwitcher.ViewFactory;
 
 public class ExamActivity extends NewsBaseActivity implements ViewFactory {
 
    public static final String EXTRA_EXAM_ID = "exam_id";
    public static final String EXTRA_EXAM_TITLE = "exam_title";
    
    private static final int NONE = 0;
    private static final int INTRO = 1;
    private static final int EXAM = 2;
    private static final int REPORT = 3;
 
    private ViewSwitcher mViewSwitcher;
    private String mUserId;
    private String mExamId;
    private String mExamTitle;
    private String mReportId;
    private ExamInfo mExamInfo;
    private ExamReport mExamReport;
    private Exam mExam;
    private ExamAnswer mExamAnswer;
    private LoadExamInfoTask mExamInfoTask;
    private LoadExamTask mExamTask;
    private CommitExamAnswerTask mExamAnswerTask;
    private ProgressDialog mWaitingDialog;
    private int mStatus = NONE;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
 
       setContentView(R.layout.activity_exam);
 
       findViews();
       init();
    }
 
    @Override
    public void onNewIntent(Intent intent) {
       super.onNewIntent(intent);
       
       cleanup();
       init();
    }
 
    private void findViews() {
       mViewSwitcher = (ViewSwitcher) findViewById(R.id.ex_switcher);
    }
 
    private void initViews() {
       mViewSwitcher.setFactory(this);
       ExamLayout layout = (ExamLayout) mViewSwitcher.getCurrentView();
       layout.getTitleView().setText(mExamTitle);
       layout.showLoadingBar();
       layout.hideViewContainer();
    }
 
    private void init() {
       mUserId = getPrefsManager().getUserId();
       mExamId = getIntent().getStringExtra(EXTRA_EXAM_ID);
       mExamTitle = getIntent().getStringExtra(EXTRA_EXAM_TITLE);
       if (TextUtils.isEmpty(mUserId) || TextUtils.isEmpty(mExamId)) {
          finish();
          return;
       }
 
       initViews();
       loadExamInfo();
    }
 
   private void cleanTasks() {
       if (mExamInfoTask != null) {
          mExamInfoTask.cancel(true);
          mExamInfoTask = null;
       }
 
       if (mExamTask != null) {
          mExamTask.cancel(true);
          mExamTask = null;
       }
 
       if (mExamAnswerTask != null) {
          mExamAnswerTask.cancel(true);
          mExamAnswerTask = null;
       }
   }
   
   private void cleanup() {
      cleanTasks();
       
       mUserId = null;
       mExamId = null;
       mExamTitle = null;
       mReportId = null;
       mExamInfo = null;
       mExamReport = null;
       mExam = null;
       mExamAnswer = null;
       mStatus = NONE;
    }
 
    @Override
    public void onBackPressed() {
       if (mStatus == EXAM) {
          showExitDialog();
          return;
       }
       super.onBackPressed();
    }
 
    @Override
    public void onDestroy() {
       super.onDestroy();
 
       cleanup();
    }
 
    @Override
    public View makeView() {
       return new ExamLayout(this);
    }
 
    private void showExamInfoView() {
       ExamLayout layout = getCurrView();
       if (mExamInfo.isIntroduction()) {
          setupExamIntroView(layout);
       } else if (mExamInfo.isReport()) {
          setupExamReportView(layout);
       }
    }
 
    private void setupExamIntroView(ExamLayout layout) {
       mStatus = INTRO;
       layout.hideLoadingBar();
       layout.showViewContainer();
       layout.getTitleView().setText(mExamInfo.getTitle());
       ViewGroup viewContainer = layout.getViewContainer();
       if (viewContainer.getChildCount() > 0) {
          View view = viewContainer.getChildAt(0);
          if (!(view instanceof ExamIntroView)) {
             viewContainer.removeAllViews();
          }
       }
 
       if (viewContainer.getChildCount() <= 0) {
          viewContainer.addView(new ExamIntroView(this));
       }
       viewContainer.scrollTo(0, 0);
 
       ExamIntroView view = (ExamIntroView) viewContainer.getChildAt(0);
       view.setContent(mExamInfo.getIntroduction());
       if (mExamInfo.allowAnswer()) {
          layout.getActionBtn().setText(R.string.ex_continue);
          layout.getActionBtn().setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                showWaitingDialog();
                loadExam();
             }
          });
          layout.showActionBtn();
       }
    }
 
    private void setupExamView(ExamLayout layout, Question question,
          boolean isLast) {
       if (question == null) {
          return;
       }
 
       mStatus = EXAM;
       layout.hideLoadingBar();
       layout.showViewContainer();
       layout.getTitleView().setText(mExamInfo.getTitle());
       ViewGroup viewContainer = layout.getViewContainer();
       if (viewContainer.getChildCount() > 0) {
          View view = viewContainer.getChildAt(0);
          if (!(view instanceof ExamView)) {
             viewContainer.removeAllViews();
          }
       }
 
       if (viewContainer.getChildCount() <= 0) {
          viewContainer.addView(new ExamView(this));
       }
       viewContainer.scrollTo(0, 0);
 
       ExamView view = (ExamView) viewContainer.getChildAt(0);
       view.setContent(question);
       layout.getActionBtn().setTag(view);
       layout.getActionBtn().setText(
             isLast ? R.string.ex_commit : R.string.ex_next);
       layout.getActionBtn().setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
             if (v.getTag() != null && v.getTag() instanceof ExamView) {
                ExamView examView = (ExamView) v.getTag();
                if (!examView.hasAnswered()) {
                   UIUtils.showToast(ExamActivity.this,
                         getString(R.string.ex_not_answered));
                   return;
                }
 
                Question question = examView.getQuestion();
                mExamAnswer.addQuestionAnswer(examView.getAnswer());
                int order = question.getOrder();
                QuestionList questions = mExam.getQuestions();
                if (order < questions.getCount()) {
                   setupExamView(getNextView(), questions.getList().get(order),
                         (order + 1) == questions.getCount());
                   showNextView();
                } else {
                   showWaitingDialog();
                   commitExamAnswer();
                }
             }
          }
       });
       layout.showActionBtn();
    }
 
    private void setupExamReportView(ExamLayout layout) {
       mStatus = REPORT;
       layout.hideLoadingBar();
       layout.showViewContainer();
       layout.getTitleView().setText(R.string.ex_report);
       ViewGroup viewContainer = layout.getViewContainer();
       if (viewContainer.getChildCount() > 0) {
          View view = viewContainer.getChildAt(0);
          if (!(view instanceof ExamReportView)) {
             viewContainer.removeAllViews();
          }
       }
 
       if (viewContainer.getChildCount() <= 0) {
          viewContainer.addView(new ExamReportView(this));
       }
       viewContainer.scrollTo(0, 0);
 
       ExamReportView view = (ExamReportView) viewContainer.getChildAt(0);
       view.setContent(mExamInfo.getReport());
       if (mExamInfo.allowAnswer()) {
          layout.getActionBtn().setText(R.string.ex_do_again);
          layout.getActionBtn().setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                showWaitingDialog();
                loadExam();
             }
          });
          layout.showActionBtn();
       }
    }
 
    private ExamLayout getCurrView() {
       return (ExamLayout) mViewSwitcher.getCurrentView();
    }
 
    private ExamLayout getNextView() {
       return (ExamLayout) mViewSwitcher.getNextView();
    }
 
    private void showNextView() {
       mViewSwitcher.setInAnimation(this, R.anim.push_left_in);
       mViewSwitcher.setOutAnimation(this, R.anim.push_right_out);
       mViewSwitcher.showNext();
    }
 
    private void showWaitingDialog() {
       if (mWaitingDialog == null) {
          mWaitingDialog = new ProgressDialog(this);
          mWaitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
          mWaitingDialog.setMessage(getString(R.string.wait));
          mWaitingDialog.setMax(100);
          mWaitingDialog.setOnDismissListener(new OnDismissListener() {
             public void onDismiss(DialogInterface dialog) {
               cleanTasks();
             }
          });
          mWaitingDialog.setCancelable(true);
       }
       mWaitingDialog.show();
    }
 
    private void hideWaitingDialog() {
       if (mWaitingDialog != null) {
          mWaitingDialog.dismiss();
       }
    }
 
    private void showExitDialog() {
       UIUtils
             .getAlertDialogBuilder(this)
             .setMessage(R.string.ex_exit)
             .setPositiveButton(R.string.ok,
                   new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int which) {
                         finish();
                      }
                   }).setNegativeButton(R.string.cancel, null)
             .setCancelable(false).show();
    }
 
    private void showMessageDialog(String msg, boolean finishActivity) {
       UIUtils
             .getAlertDialogBuilder(this)
             .setMessage(msg)
             .setPositiveButton(R.string.ok,
                   finishActivity ? new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int which) {
                         finish();
                      }
                   } : null).setCancelable(!finishActivity).show();
    }
 
    private void loadExamInfo() {
       if (mExamInfoTask == null) {
          mExamInfoTask = new LoadExamInfoTask(this, mUserId, mExamId);
          mExamInfoTask.setFinishActivityOnNetworkError(true);
          mExamInfoTask.setFinishActivityOnFailure(true);
          mExamInfoTask.execute(this);
       }
    }
 
    private void loadExam() {
       if (mExamTask == null) {
          mExamTask = new LoadExamTask(this, mUserId, mExamId);
          mExamTask.execute(this);
       }
    }
 
    private void commitExamAnswer() {
       if (mExamAnswerTask == null && mExamAnswer != null) {
          mExamAnswerTask = new CommitExamAnswerTask(this, mUserId, mExamAnswer,
                mReportId);
          mExamAnswerTask.execute(this);
       }
    }
 
    class LoadExamInfoTask extends ExamAsyncTask<Context, Void, ExamInfo> {
 
       protected String userId;
       protected String examId;
 
       public LoadExamInfoTask(Context context, String userId, String examId) {
          super(context);
          this.userId = userId;
          this.examId = examId;
       }
 
       @Override
       protected ExamInfo doInBackground(Context... params) {
          return ExamApi.getExamInfo(this.context, this.userId, this.examId);
       }
 
       @Override
       protected void onPostExecute(ExamInfo result) {
          super.onPostExecute(result);
 
          getCurrView().hideLoadingBar();
          if (result != null && result.isSuccess()) {
             mExamInfo = result;
             mExamTitle = result.getTitle();
             showExamInfoView();
          }
       }
 
    }
 
    class LoadExamTask extends ExamAsyncTask<Context, Void, Exam> {
 
       protected String userId;
       protected String examId;
 
       public LoadExamTask(Context context, String userId, String examId) {
          super(context);
          this.userId = userId;
          this.examId = examId;
       }
 
       @Override
       protected Exam doInBackground(Context... params) {
          return ExamApi.getExam(this.context, this.userId, this.examId);
       }
 
       @Override
       protected void onPostExecute(Exam result) {
          super.onPostExecute(result);
 
          if (result != null && result.isSuccess()) {
             mExam = result;
             
             if (mExamInfo == null) {
                mExamInfo = new ExamInfo();
             }
             mExamTitle = result.getTitle();
             mExamInfo.setId(result.getId());
             mExamInfo.setTitle(result.getTitle());
             
             mExamAnswer = new ExamAnswer();
             mExamAnswer.setExamId(result.getId());
             QuestionList questions = result.getQuestions();
             if (questions != null && questions.getCount() > 0) {
                setupExamView(getNextView(), questions.getList().get(0),
                      questions.getCount() == 1);
                showNextView();
             }
          }
       }
 
    }
 
    class CommitExamAnswerTask extends ExamAsyncTask<Context, Void, ExamReport> {
 
       protected String userId;
       protected ExamAnswer answer;
       protected String reportId;
 
       public CommitExamAnswerTask(Context context, String userId,
             ExamAnswer answer) {
          super(context);
          this.userId = userId;
          this.answer = answer;
       }
 
       public CommitExamAnswerTask(Context context, String userId,
             ExamAnswer answer, String reportId) {
          this(context, userId, answer);
          this.reportId = reportId;
       }
 
       @Override
       protected ExamReport doInBackground(Context... params) {
          return ExamApi.commitExamAnswer(this.context, this.userId,
                this.answer, this.reportId);
       }
 
       @Override
       protected void onPostExecute(ExamReport result) {
          super.onPostExecute(result);
 
          if (result != null && result.isSuccess()) {
             mReportId = result.getId();
             mExamReport = result;
             mExamInfo.setReport(result);
             setupExamReportView(getNextView());
             showNextView();
          }
       }
 
    }
 
    abstract class ExamAsyncTask<Params, Progress, Result extends NewsResult>
          extends AsyncTask<Params, Progress, Result> {
 
       protected Context context;
       protected boolean finishActivityOnNetworkError = false;
       protected boolean finishActivityOnFailure = false;
 
       public ExamAsyncTask(Context context) {
          this.context = context;
       }
 
       public void setFinishActivityOnNetworkError(boolean finish) {
          this.finishActivityOnNetworkError = finish;
       }
 
       public void setFinishActivityOnFailure(boolean finish) {
          this.finishActivityOnFailure = finish;
       }
 
       @Override
       protected void onPostExecute(Result result) {
          hideWaitingDialog();
          if (result != null && result.isFailure()) {
             if (result.isNetworkError()) {
                showMessageDialog(getString(R.string.net_isonline_tip_msg),
                      this.finishActivityOnNetworkError);
             } else {
                showMessageDialog(result.getResultDescription(),
                      this.finishActivityOnFailure);
             }
          }
       }
 
    }
 
 }
