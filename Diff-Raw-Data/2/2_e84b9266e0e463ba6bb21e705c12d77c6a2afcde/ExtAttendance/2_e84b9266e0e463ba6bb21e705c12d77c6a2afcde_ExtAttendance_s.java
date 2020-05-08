 package ext;
 
 import models.ins.Attendance;
 import models.ins.AttendanceStatus;
 import models.ins.Employer;
 import models.security.User;
 import play.templates.JavaExtensions;
 import utils.Dates;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 public class ExtAttendance extends JavaExtensions {
 
     public static boolean canUpdateAttenndance(Calendar date, Attendance attendance) {
         if (User.loadFromSession().isRH()) {
             return true;
         }
 
         if (attendance == null || !attendance.person.isRHPerson()) {
             return true;
         }
 
         if (attendance.updateAt == null) {
             return true;
         }
 
         Calendar today = new GregorianCalendar();
         if (Dates.compareDate(attendance.updateAt, today) == 0) {
             return true;
         }
 
         return false;
     }
 
     public static boolean isSunday(Calendar date) {
         return date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
     }
 
     public static int fromHolidays(int holidayType) {
         switch (holidayType) {
             case 0:
                 return AttendanceStatus.V.value;
             case 1:
                 return AttendanceStatus.JRF.value;
             case 2:
                 return AttendanceStatus.PR.value;
             case 3:
                 return AttendanceStatus.NM.value;
             case 4:
                 return AttendanceStatus.NM45.value;
             case 5:
                 return AttendanceStatus.ANC.value;
             case 6:
                 return AttendanceStatus.PC.value;
             case 7:
                 return AttendanceStatus.PC.value;
             case 8:
                 return AttendanceStatus.PC.value;
             case 9:
                 return AttendanceStatus.PC.value;
             case 10:
                 return AttendanceStatus.PC.value;
             case 11:
                 return AttendanceStatus.PC.value;
             case 12:
                 return AttendanceStatus.PC.value;
             case 13:
                 return AttendanceStatus.PC.value;
             case 14:
                 return AttendanceStatus.PC.value;
             case 15:
                 return AttendanceStatus.PC.value;
             case 16:
                 return AttendanceStatus.V.value;
             case 17:
                 return AttendanceStatus.PA.value;
             case 18:
                 return AttendanceStatus.VJ.value;
             case 19:
                 return AttendanceStatus.VS.value;
             case 21:
                 return AttendanceStatus.AC.value;
             case 22:
                 return AttendanceStatus.SS.value;
             case 23:
                 return AttendanceStatus.RE.value;
         }
 
         return AttendanceStatus.V.value;
     }
 
     public static String statusToAbrev(Integer status) {
         switch (status) {
             case -1:
                 return "";
             case 1:
                 return " P ";
             case 2:
                 return " M ";
             case 3:
                 return "AI ";
             case 4:
                 return "BS ";
             case 5:
                 return " R ";
             case 6:
                 return "CA ";
             case 7:
                 return "CC ";
             case 8:
                 return "CL ";
             case 9:
                 return "CCI";
 
             case 20:
                 return " V ";
             case 21:
                 return "JRF";
             case 22:
                 return "PC ";
             case 23:
                 return "SS ";
             case 24:
                 return "RI ";
             case 25:
                 return "MAT";
             case 26:
                 return "PA ";
             case 27:
                 return " E ";
             case 28:
                 return "ALL";
             case 29:
                 return " G ";
             case 30:
                 return "PR ";
             case 31:
                 return "NM ";
             case 32:
                 return "+45";
             case 33:
                 return "ANC";
             case 34:
                 return "VJ ";
             case 35:
                 return "VS ";
             case 36:
                 return "JF ";
             case 37:
                 return "CT ";
             case 38:
                 return "FI ";
             case 39:
                 return "RE ";
 
             case 10:
                 return "AC ";
             case 11:
                 return "FE ";
             case 12:
                 return " AJ ";
 
         }
 
        return "";
     }
 
     public static String statusToEmployerAbrev(Integer status, Employer employer) {
         switch (status) {
             case -1:
                 return " - ";
             case 0:
                 return " - ";
             default:
                 return employer.getPersonnalisedStatus(status);
         }
     }
 
     public static String absencesType(Integer holidayType) {
         switch (holidayType) {
             case 2:
                 return "M (Maladie)";
             case 4:
                 return "BS (Bon de sortie)";
             case 6:
                 return "CA (Congés annuel)";
             case 7:
                 return "CC (Congés compensatoire)";
             case 8:
                 return "CL (Congés légal CPAS)";
             case 9:
                 return "CCI (Congés de circonstance)";
 
             case 20:
                 return "V (Congés annuel)";
             case 21:
                 return "JRF (Jours fériés à récuperer)";
             case 22:
                 return "PC (Petit chômage : mariage, etc...)";
             case 23:
                 return "SS (Sans solde)";
 
             case 24:
                 return "RI (Raison impérieuse)";
             case 25:
                 return "MAT (Maternité)";
             case 26:
                 return "PA (Paternité)";
             case 27:
                 return "E (Ecartement)";
             case 28:
                 return "ALL (Allaitement)";
             case 29:
                 return "G (Grêve)";
             case 30:
                 return "PR (Petits Riens)";
             case 31:
                 return "NM (Non marchand)";
             case 32:
                 return "+45 (Non marchand +45)";
             case 33:
                 return "ANC (Congé d'ancienneté)";
             case 34:
                 return "VJ (Vancances jeunes)";
             case 35:
                 return "VS (Vancances seniors)";
             case 36:
                 return "JF (Jours fériés)";
             case 37:
                 return "CT (Crédit temps)";
             case 38:
                 return "FI (Formation interne)";
             case 39:
                 return "RE (Recherche emploi)";
 
             case 11:
                 return "FE (Formation externe)";
             case 12:
                 return " AJ (Absences justifiées)";
         }
 
         return "-";
     }
 
     public static String getStatusStyle(Integer status) {
         String white = "color:white";
         String black = "color:black";
         switch (status) {
             case 1:
                 return "background-color: #FFF";
             case 2:
                 return "background-color: #FF6600;" + black;
             case 3:
                 return "background-color: #F79646;" + black;
             case 4:
                 return "background-color: #5e73c8;" + black;
             case 5:
                 return "background-color: #FF00FF;" + black;
             case 6:
                 return "background-color: #95f27c;" + black;
             case 7:
                 return "background-color: #154607;" + white;
             case 8:
                 return "background-color: #57e813;" + black;
             case 9:
                 return "background-color: #348a0b;" + white;
 
             case 10:
                 return "background-color: #76933C;" + black;
             case 11:
                 return "background-color: #99CCFF;" + black;
             case 12:
                 return "background-color: #ef7d59;" + black;
             case 13:
                 return "background-color: #17e9e7;" + black;
 
             case 20:
                 return "background-color: #95f27c;" + black;
             case 21:
                 return "background-color: #154607;" + white;
             case 22:
                 return "background-color: #2C930F;" + white;
             case 23:
                 return "background-color: #17e9e7;" + black;
             case 24:
                 return "background-color: #5fb1a5;" + black;
             case 25:
                 return "background-color: #5fb1a5;" + black;
             case 26:
                 return "background-color: #5fb1a5;" + black;
             case 27:
                 return "background-color: #5fb1a5;" + black;
             case 28:
                 return "background-color: #5fb1a5;" + black;
             case 29:
                 return "background-color: #5fb1a5;" + black;
             case 30:
                 return "background-color: #f3ac02;" + black;
             case 31:
                 return "background-color: #cb4c4e;" + black;
             case 32:
                 return "background-color: #cb4c4e;" + black;
             case 33:
                 return "background-color: #61649b;" + black;
             case 34:
                 return "background-color: #0172bd;" + white;
             case 35:
                 return "background-color: #af774a;" + black;
             case 36:
                 return "background-color: #af774a;" + black;
             case 37:
                 return "background-color: #af774a;" + black;
             case 38:
                 return "background-color: #af774a;" + black;
             case 39:
                 return "background-color: #af774a;" + black;
         }
 
         return "";
     }
 
     public static String getStatusStyleNoBorder(Integer status) {
         String white = "color:white";
         String black = "color:black";
         switch (status) {
             case 1:
                 return "background-color: #FFF";
             case 2:
                 return "background-color: #FF6600;" + black;
             case 3:
                 return "background-color: #F79646;" + black;
             case 4:
                 return "background-color: #5e73c8;" + black;
             case 5:
                 return "background-color: #FF00FF;" + black;
             case 6:
                 return "background-color: #95f27c;" + black;
             case 7:
                 return "background-color: #154607;" + white;
             case 8:
                 return "background-color: #57e813;" + black;
             case 9:
                 return "background-color: #348a0b;" + white;
 
             case 10:
                 return "background-color: #76933C;" + black;
             case 11:
                 return "background-color: #99CCFF;" + black;
             case 12:
                 return "background-color: #ef7d59;" + black;
             case 13:
                 return "background-color: #17e9e7;" + black;
 
             case 20:
                 return "background-color: #95f27c;" + black;
             case 21:
                 return "background-color: #154607;" + white;
             case 22:
                 return "background-color: #2C930F;" + white;
             case 23:
                 return "background-color: #17e9e7;" + black;
             case 24:
                 return "background-color: #5fb1a5;" + black;
             case 25:
                 return "background-color: #5fb1a5;" + black;
             case 26:
                 return "background-color: #5fb1a5;" + black;
             case 27:
                 return "background-color: #5fb1a5;" + black;
             case 28:
                 return "background-color: #5fb1a5;" + black;
             case 29:
                 return "background-color: #5fb1a5;" + black;
             case 30:
                 return "background-color: #f3ac02;" + black;
             case 31:
                 return "background-color: #cb4c4e;" + black;
             case 32:
                 return "background-color: #cb4c4e;" + black;
             case 33:
                 return "background-color: #61649b;" + black;
             case 34:
                 return "background-color: #0172bd;" + white;
             case 35:
                 return "background-color: #af774a;" + black;
             case 36:
                 return "background-color: #af774a;" + black;
             case 37:
                 return "background-color: #af774a;" + black;
             case 38:
                 return "background-color: #af774a;" + black;
             case 39:
                 return "background-color: #af774a;" + black;
         }
 
         return "";
     }
 
     public static String attendanceNotDone(Attendance attendance, Calendar day) {
         if (attendance == null) {
             return "";
         }
 
         boolean check = false;
         if (attendance.halfDay && (attendance.statusAM == 0 || attendance.statusPM == 0)) {
             check = true;
         }
 
         if (!attendance.halfDay && attendance.status == 0) {
             check = true;
         }
 
         if(check && Dates.isBeforeToday(day)){
             return "error";
         }
 
         return "";
     }
 }
