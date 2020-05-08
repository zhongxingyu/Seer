 /**
  * 
  */
 package org.mklab.taskit.client.model;
 
 import org.mklab.taskit.client.ClientFactory;
 import org.mklab.taskit.client.LocalDatabase;
 import org.mklab.taskit.shared.AttendanceProxy;
 import org.mklab.taskit.shared.LectureProxy;
 import org.mklab.taskit.shared.SubmissionProxy;
 
 import java.util.List;
 
 import com.google.web.bindery.requestfactory.shared.Receiver;
 
 
 /**
  * 成績データの非同期取得を行うクラスです。
  * 
  * @author ishikura
  */
 public class StudentwiseRecordQuery {
 
   private ClientFactory clientFactory;
 
   /**
    * {@link StudentwiseRecordQuery}オブジェクトを構築します。
    * 
    * @param clientFactory クライアントファクトリ
    */
   public StudentwiseRecordQuery(ClientFactory clientFactory) {
     if (clientFactory == null) throw new NullPointerException();
     this.clientFactory = clientFactory;
   }
 
   ClientFactory getClientFactory() {
     return this.clientFactory;
   }
 
   /**
    * 成績データを取得します。
    * 
    * @param accountId 生徒アカウントID
    * @param handler 取得結果を処理するハンドラ
    */
   public void query(final String accountId, final Handler handler) {
     this.clientFactory.getLocalDatabase().execute(LocalDatabase.LECTURE_LIST, new Receiver<List<LectureProxy>>() {
 
       @SuppressWarnings("nls")
       @Override
       public void onSuccess(List<LectureProxy> response) {
         final StudentwiseRecordModel model = new StudentwiseRecordModel(response);
         handler.handleResult(model);
 
        getClientFactory().getRequestFactory().submissionRequest().getSubmissionsByAccountId(accountId).with("submitter", "report").fire(new Receiver<List<SubmissionProxy>>() {
 
           @Override
           public void onSuccess(@SuppressWarnings("hiding") List<SubmissionProxy> response) {
             model.setSubmissions(response);
             handler.handleResult(model);
           }
 
         });
         getClientFactory().getRequestFactory().attendanceRequest().getAttendancesByAccountId(accountId).with("lecture").fire(new Receiver<List<AttendanceProxy>>() {
 
           @Override
           public void onSuccess(@SuppressWarnings("hiding") List<AttendanceProxy> response) {
             model.setAttendances(response);
             handler.handleResult(model);
           }
         });
       }
     });
   }
 
   /**
    * 成績データを取得します。
    * 
    * @param handler 取得結果を処理するハンドラ
    */
   public void query(final Handler handler) {
     this.clientFactory.getLocalDatabase().execute(LocalDatabase.LECTURE_LIST, new Receiver<List<LectureProxy>>() {
 
       @SuppressWarnings("nls")
       @Override
       public void onSuccess(List<LectureProxy> response) {
         final StudentwiseRecordModel model = new StudentwiseRecordModel(response);
         handler.handleResult(model);
 
        getClientFactory().getRequestFactory().submissionRequest().getMySubmissions().with("submitter", "report").fire(new Receiver<List<SubmissionProxy>>() {
 
           @Override
           public void onSuccess(@SuppressWarnings("hiding") List<SubmissionProxy> response) {
             model.setSubmissions(response);
             handler.handleResult(model);
           }
 
         });
         getClientFactory().getRequestFactory().attendanceRequest().getMyAttendances().with("lecture").fire(new Receiver<List<AttendanceProxy>>() {
 
           @Override
           public void onSuccess(@SuppressWarnings("hiding") List<AttendanceProxy> response) {
             model.setAttendances(response);
             handler.handleResult(model);
           }
         });
       }
     });
   }
 
   /**
    * 取得した成績データを処理するインターフェースです。
    * 
    * @author ishikura
    */
   public interface Handler {
 
     /**
      * 取得結果を処理します。
      * 
      * @param model 取得した成績データ
      */
     void handleResult(StudentwiseRecordModel model);
   }
 
 }
