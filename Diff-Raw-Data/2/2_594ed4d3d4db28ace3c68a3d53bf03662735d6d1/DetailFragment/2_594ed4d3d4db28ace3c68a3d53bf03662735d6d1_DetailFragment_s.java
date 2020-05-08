 package com.frca.vsexam.fragments;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.frca.vsexam.R;
 import com.frca.vsexam.adapters.ClassmateAdapter;
 import com.frca.vsexam.entities.exam.Exam;
 import com.frca.vsexam.entities.classmate.ClassmateList;
 import com.frca.vsexam.exceptions.NoAuthException;
 import com.frca.vsexam.helper.DataHolder;
 import com.frca.vsexam.helper.Helper;
 import com.frca.vsexam.network.HttpRequestBuilder;
 import com.frca.vsexam.network.Response;
 import com.frca.vsexam.network.tasks.BaseNetworkTask;
 import com.frca.vsexam.network.tasks.TextNetworkTask;
 import com.frca.vsexam.network.tasks.UserImageNetworkTask;
 
 import org.apache.http.client.methods.HttpRequestBase;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 public class DetailFragment extends BaseFragment {
 
     private final Exam exam;
 
     private View layoutView;
 
     public DetailFragment(Exam exam) {
         this.exam = exam;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceBundle) {
         super.onCreate(savedInstanceBundle);
 
         if (!(getParentFragment() instanceof BrowserPaneFragment)) {
             Log.e(getClass().getName(), "This class must be child of BrowserPaneFramgnet");
             getActivity().finish();
         }
 
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 
         layoutView = inflater.inflate(R.layout.exam_list_details, container, false);
 
         updateView();
 
         View returnView = layoutView;
         layoutView = null;
 
         return returnView;
     }
 
     @Override
     public void onViewCreated (View view, Bundle savedInstanceState) {
         layoutView = view;
     }
 
     public void updateView() {
         View view = getView();
 
         TextView text_courseCode = ((TextView) view.findViewById(R.id.text_courseCode));
         TextView text_courseName = ((TextView) view.findViewById(R.id.text_courseName));
         TextView text_authorName = ((TextView) view.findViewById(R.id.text_authorName));
         TextView text_type = ((TextView) view.findViewById(R.id.text_type));
         TextView text_isRegistered = ((TextView) view.findViewById(R.id.text_isRegistered));
         TextView text_capacity = ((TextView) view.findViewById(R.id.text_capacity));
         TextView text_examDate = ((TextView) view.findViewById(R.id.text_examDate));
         TextView text_registerStart = ((TextView) view.findViewById(R.id.text_registerStart));
         TextView text_registerEnd = ((TextView) view.findViewById(R.id.text_registerEnd));
         TextView text_unregisterEnd = ((TextView) view.findViewById(R.id.text_unregisterEnd));
         View button_author = view.findViewById(R.id.button_author);
         View logo_author = view.findViewById(R.id.logo_author);
 
         text_courseCode.setText(exam.getCourseCode());
         text_courseName.setText(exam.getCourseName());
         text_authorName.setText(exam.getAuthorName());
         text_type.setText(exam.getType());
         text_isRegistered.setText(exam.isRegistered() ? R.string.registered : R.string.unregistered);
         text_capacity.setText(String.valueOf(exam.getCurrentCapacity()) + "/" + String.valueOf(exam.getMaxCapacity()));
         text_examDate.setText(Helper.getDateOutput(exam.getExamDate(), Helper.DateOutputType.DATE_TIME));
         text_registerStart.setText(Helper.getDateOutput(exam.getRegisterStart(), Helper.DateOutputType.DATE_TIME));
         text_registerEnd.setText(Helper.getDateOutput(exam.getRegisterEnd(), Helper.DateOutputType.DATE_TIME));
         text_unregisterEnd.setText(Helper.getDateOutput(exam.getUnregisterEnd(), Helper.DateOutputType.DATE_TIME));
 
         button_author.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 String authorUrl = HttpRequestBuilder.completeURLString("lide/clovek.pl?id=" + String.valueOf(exam.getAuthorId()));
                 Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authorUrl));
                 startActivity(browserIntent);
             }
         });
 
         BaseNetworkTask.run(new UserImageNetworkTask(getActivity(), exam.getAuthorId(), logo_author));
 
         setupButtons();
 
         if (exam.getCurrentCapacity() != 0)
             loadClassmates();
         else {
             View left_layout = view.findViewById(R.id.layout_detail_left);
             View right_layout = view.findViewById(R.id.layout_detail_right);
             right_layout.setVisibility(View.GONE);
             LinearLayout parent = (LinearLayout)left_layout.getParent();
             if (parent.getOrientation() == LinearLayout.HORIZONTAL) {
                 LinearLayout.LayoutParams left_params = (LinearLayout.LayoutParams)left_layout.getLayoutParams();
                 ((LinearLayout)left_layout.getParent()).setGravity(Gravity.CENTER_HORIZONTAL);
                 left_params.weight = 0;
                 left_params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 500, getResources().getDisplayMetrics());
             }
         }
     }
 
     private void setupButtons() {
         View view = getView();
 
         Button button_left = (Button) view.findViewById(R.id.button_left);
         Button button_right = (Button) view.findViewById(R.id.button_right);
 
         long currentServerTime = DataHolder.getInstance(getActivity()).getNetworkInterface().getCurrentServerTime();
         long timeUntilRegistration = exam.getRegisterStart().getTime() - currentServerTime;
 
         if (timeUntilRegistration > 0) {
             if (!exam.isToBeRegistered())
                 setButton(button_left, new OnRegisterASAPClick(), true);
             else
                 setButton(button_left, new OnCancelRegisterASAPClick(), true);
 
             boolean registrationSoon = timeUntilRegistration - (60 * 1000) < 0;
             setButton(button_right, new OnRegisterClick(), registrationSoon);
 
             if (!registrationSoon) {
                 new Handler().postDelayed(new Runnable() {
                     @Override
                     public void run() {
                         setupButtons();
                     }
                 }, timeUntilRegistration - (30 * 1000));
             }
         } else {
             setButton(button_left, new OnRegisterASAPClick(), false);
 
             if (!exam.isRegistered())
                 setButton(button_right, new OnRegisterClick(), true);
             else
                 setButton(button_right, new OnUnregisterClick(), true);
         }
     }
 
     private void loadClassmates() {
         if (Helper.isValid(exam.getClassmates())) {
             onClassmatesLoaded(exam.getClassmates());
             return;
         }
 
          BaseNetworkTask.run(
             new TextNetworkTask(
                 getActivity(), "student/terminy_info.pl?termin=" + exam.getId() + ";spoluzaci=1;studium=" + exam.getStudyId() + ";obdobi=" + exam.getPeriodId(),
                 new TextNetworkTask.ResponseCallback() {
 
                     @Override
                     public void onSuccess(Response response) {
                         if (response.getStatusCode() == 401) {
                             Toast.makeText(getActivity(), "Access denied", Toast.LENGTH_LONG).show();
                             return;
                         }
 
                         Document doc = Jsoup.parse(response.getText());
                         Elements elements = doc.body().select("table#studenti tbody tr");
 
                         ClassmateList classmates = new ClassmateList(elements);
 
                         exam.setClassmates(classmates);
                         if (getView() != null)
                             onClassmatesLoaded(classmates);
 
                     }
                 }, new BaseNetworkTask.ExceptionCallback() {
                     @Override
                     public void onException(Exception e) {
                         if (e instanceof NoAuthException) {
                             Toast.makeText(getActivity(), "No auth data set.", Toast.LENGTH_LONG).show();
                             getMainActivity().setFragment(new LoginFragment());
                         }
                     }
             }));
     }
 
     private void onClassmatesLoaded(ClassmateList classmates) {
         ClassmateAdapter classmateAdapter = new ClassmateAdapter(getActivity(), classmates);
         View classmateProgress = getView().findViewById(R.id.classmates_progress);
 
         LinearLayout classmateLayout = (LinearLayout) getView().findViewById(R.id.layout_classmates);
 
         for (int i = 0; i < classmateAdapter.getCount(); ++i) {
             View view = classmateAdapter.getView(i, null, null);
             classmateLayout.addView(view);
 
             if (i != classmateAdapter.getCount() - 1)
                 classmateLayout.addView(Helper.getDivider(getLayoutInflater(), Helper.Orientation.VERTICAL));
         }
 
         classmateProgress.setVisibility(View.GONE);
         classmateLayout.setVisibility(View.VISIBLE);
     }
 
     @Override
     public View getView() {
         if (layoutView != null)
             return layoutView;
         else
             return super.getView();
 
     }
 
     private void setButton(Button button, BaseOnClick onClick, boolean enabled) {
         button.setText(onClick.getTextResource());
         if (enabled)
             button.setOnClickListener(onClick);
         else
             button.setOnClickListener(null);
 
         button.setEnabled(enabled);
     }
 
     private class OnRegisterClick extends BaseOnClick {
 
         @Override
         public void onClick() {
             HttpRequestBase requestBase = HttpRequestBuilder.getRegisterRequest(DataHolder.getInstance(getActivity()), exam, true);
             BaseNetworkTask.run(new TextNetworkTask(getActivity(), requestBase, new BaseNetworkTask.ResponseCallback() {
                 @Override
                 public void onSuccess(Response response) {
                     if (getBrowserPaneFragment().getExams().onRegistrationResponse(getActivity(), exam, response)) {
                         Toast.makeText(getActivity(), "Přihlášení úspěšné", Toast.LENGTH_LONG).show();
                         Helper.appendLog("Register success!");
                     } else {
                         Toast.makeText(getActivity(), "Přihlášení nebylo úspěšné", Toast.LENGTH_LONG).show();
                         Helper.appendLog("Register unsuccessful");
                     }
                 }
             }));
         }
 
         @Override
         int getTextResource() {
             return R.string.action_register;
         }
     }
 
     private class OnUnregisterClick extends BaseOnClick {
 
         @Override
         public void onClick() {
             AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 
             builder.setTitle(getTextResource())
                 .setMessage("Opravdu chcete odhlásit z tohoto termínu?")
                 .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         runUnregistering();
                         dialogInterface.dismiss();
                     }
                 }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         dialogInterface.dismiss();
                     }
                 });
 
             builder.create().show();
         }
 
         private void runUnregistering() {
            HttpRequestBase requestBase = HttpRequestBuilder.getRegisterRequest(DataHolder.getInstance(getActivity()), exam, true);
             BaseNetworkTask.run(new TextNetworkTask(getActivity(), requestBase, new BaseNetworkTask.ResponseCallback() {
                 @Override
                 public void onSuccess(Response response) {
                     if (getBrowserPaneFragment().getExams().onUngistrationResponse(exam, response)) {
                         Toast.makeText(getActivity(), "Odhlášení úspěšné", Toast.LENGTH_LONG).show();
                         Helper.appendLog("Unregister success!");
                     } else {
                         Toast.makeText(getActivity(), "Odhlášení nebylo úspěšné", Toast.LENGTH_LONG).show();
                         Helper.appendLog("Unregister unsuccessful");
                     }
                 }
             }));
         }
 
         @Override
         int getTextResource() {
             return R.string.action_unregister;
         }
     }
 
     private class OnRegisterASAPClick extends BaseOnClick {
 
         @Override
         public void onClick() {
             Helper.appendLog("User clicked onto register on time");
             AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 
             builder.setTitle(getTextResource())
                 .setMessage("Pomocí této funkce Vás aplikace automaticky přihlásí na vybraný termín ihned, jakmile bude v systému ISIS spuštěno přihlašování. \n Upozornění: Toto zařízení musí být v době registrace zapnuté.")
                 .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         exam.setToBeRegistered(getActivity(), true);
                         updateView();
                         dialogInterface.dismiss();
                     }
                 }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         dialogInterface.dismiss();
                     }
                 });
 
             builder.create().show();
         }
 
         @Override
         int getTextResource() {
             return R.string.action_registerASAP;
         }
     }
 
     private class OnCancelRegisterASAPClick extends BaseOnClick {
 
         @Override
         public void onClick() {
             Helper.appendLog("User clicked onto cancel register on time");
             AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 
             builder.setTitle(getTextResource())
                 .setMessage("Opravdu chcete zrušit přihlášení co nejdříve?")
                 .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         exam.setToBeRegistered(getActivity(), false);
                         updateView();
                         dialogInterface.dismiss();
                     }
                 }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         dialogInterface.dismiss();
                     }
                 });
 
             builder.create().show();
         }
 
         @Override
         int getTextResource() {
             return R.string.action_cancel_registerASAP;
         }
     }
 
     private abstract class BaseOnClick implements View.OnClickListener {
 
         abstract void onClick();
         abstract int getTextResource();
 
         @Override
         public void onClick(View view) {
             onClick();
         }
     }
 
     public Exam getExam() {
         return exam;
     }
 
     private BrowserPaneFragment getBrowserPaneFragment() {
         return (BrowserPaneFragment) getParentFragment();
     }
 
     private LayoutInflater getLayoutInflater() {
         return (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     }
 }
