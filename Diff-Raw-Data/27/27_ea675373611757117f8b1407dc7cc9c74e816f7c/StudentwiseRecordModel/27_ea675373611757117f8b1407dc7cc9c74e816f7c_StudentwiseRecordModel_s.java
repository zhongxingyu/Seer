 /**
  * 
  */
 package org.mklab.taskit.client.model;
 
 import org.mklab.taskit.shared.AttendanceProxy;
 import org.mklab.taskit.shared.LectureProxy;
 import org.mklab.taskit.shared.ReportProxy;
 import org.mklab.taskit.shared.SubmissionProxy;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 /**
  * 単一生徒の成績情報のモデルです。
  * 
  * @author ishikura
  */
 public class StudentwiseRecordModel {
 
   private List<LectureScore> lectureScores;
   private Map<Integer, LectureScore> lectureToScore;
 
   /**
    * {@link StudentwiseRecordModel}オブジェクトを構築します。
    * 
    * @param lectures 講義データ
    */
   public StudentwiseRecordModel(List<LectureProxy> lectures) {
     this.lectureScores = new ArrayList<LectureScore>();
     this.lectureToScore = new HashMap<Integer, LectureScore>();
     for (LectureProxy lecture : lectures) {
      final LectureScore lectureScore = new LectureScore(lecture);
       this.lectureScores.add(lectureScore);
       this.lectureToScore.put(lecture.getId(), lectureScore);
     }
   }
 
   void setAttendances(List<AttendanceProxy> attendances) {
     for (AttendanceProxy attendance : attendances) {
       final LectureScore score = this.lectureToScore.get(attendance.getLecture().getId());
       score.setAttendance(attendance);
     }
   }
 
   void setSubmissions(List<SubmissionProxy> submissions) {
     for (SubmissionProxy submission : submissions) {
       final LectureScore score = this.lectureToScore.get(submission.getReport().getLecture().getId());
       score.addSubmission(submission);
     }
   }
 
   /**
    * 講義数を取得します。
    * 
    * @return 講義数
    */
   public int getLectureCount() {
     return this.lectureScores.size();
   }
 
   /**
    * すべての講義中で最も課題数が多い講義の課題数を取得します。
    * 
    * @return 最大課題数
    */
   public int getMaximumReportCount() {
     int max = 0;
     for (LectureScore score : this.lectureScores) {
       int cnt = score.getReportCount();
       if (cnt > max) {
         max = cnt;
       }
     }
     return max;
   }
 
   /**
    * 講義情報と成績情報のペアを取得します。
    * 
    * @param lectureIndex 講義のインデックス
    * @return 講義情報と成績情報のペア
    */
   public LectureScore getLectureScore(int lectureIndex) {
     return this.lectureScores.get(lectureIndex);
   }
 
   /**
    * リストとして成績データを取得します。
    * 
    * @return 成績データ
    */
   public List<LectureScore> asList() {
     return this.lectureScores;
   }
 
   /**
    * 講義情報と成績情報のペアを表すクラスです。
    * 
    * @author ishikura
    */
   public static class LectureScore {
 
     int index;
     Map<ReportProxy, SubmissionProxy> reportToSubmission;
     LectureProxy lecture;
     AttendanceProxy attendance;
 
    LectureScore(LectureProxy lecture) {
       this.lecture = lecture;
       this.reportToSubmission = new HashMap<ReportProxy, SubmissionProxy>();
     }
 
     void addSubmission(SubmissionProxy submission) {
       this.reportToSubmission.put(submission.getReport(), submission);
     }
 
     void setAttendance(AttendanceProxy attendance) {
       this.attendance = attendance;
     }
 
     /**
      * 講義のインデックスを取得します。
      * 
      * @return 講義のインデックス
      */
     public int getIndex() {
       return this.index;
     }
 
     /**
      * attendanceを取得します。
      * 
      * @return attendance
      */
     public AttendanceProxy getAttendance() {
       return this.attendance;
     }
 
     /**
      * lectureを取得します。
      * 
      * @return lecture
      */
     public LectureProxy getLecture() {
       return this.lecture;
     }
 
     /**
      * 課題数を取得します。
      * 
      * @return 課題数
      */
     public int getReportCount() {
       final List<ReportProxy> reports = this.lecture.getReports();
       return reports.size();
     }
 
     /**
      * 課題を取得します。
      * 
      * @param index 課題のインデックス
      * @return 課題
      */
     public ReportProxy getReport(@SuppressWarnings("hiding") int index) {
       return getLecture().getReports().get(index);
     }
 
     /**
      * 提出物を取得します。
      * 
      * @param report 課題
      * @return 提出物
      */
     public SubmissionProxy getSubmission(ReportProxy report) {
       if (report == null) throw new IllegalArgumentException();
       final SubmissionProxy submission = this.reportToSubmission.get(report);
       return submission;
     }
 
   }
 
 }
