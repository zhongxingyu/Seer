 package helper;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 /**
  * Every method in class need to done, these are just skeletons
  */
 public class Validation {
 
     static String _errMsg = "";
     
     public static String getErrMsg() {
         return _errMsg;
     }
     
     /**
      * Use this method if you need to manipulate the string
      * @param raw
      * @return
      */
     public static String prepare(String raw) {
         String ret;
         ret = raw.trim();
         return ret;
     }
     
     public static boolean checkEmpty(String target, String fieldName) {
         boolean flag = true;
         if (target.equals("")) {
             flag = false;
             _errMsg = fieldName + " cannot be empty";
         }
         return flag;
     }
     
     public static boolean isNumber(String str, String fieldName) {
         boolean flag = str.matches("^[0]*[1-9][0-9]*$");
         if (!flag) {
             _errMsg = fieldName + " must be a positive integer";
         }
         return flag;
     }
     
     public static boolean checkDeptCode(String deptCode) {
         boolean flag = checkEmpty(deptCode, "Department Code");
         return flag;
     }
     
     public static boolean checkDeptName(String deptName) {
         boolean flag = checkEmpty(deptName, "Department Name");
         return flag;
     }
     
     public static boolean checkMId(String m_id) {
         boolean flag = checkEmpty(m_id, "Meeting Id") && isNumber(m_id, "Meeting Id");
         return flag;
     }
     
     public static boolean checkMsId(String ms_id) {
         boolean flag = checkEmpty(ms_id, "Meeting Schedule Id") && isNumber(ms_id, "Meeting Schedule Id");
         return flag;
     }
     
     public static boolean checkLId(String l_id) {
         boolean flag = checkEmpty(l_id, "Lecture Id") && isNumber(l_id, "Lecture Id");
         return flag;
     }
     
     public static boolean checkLsId(String ls_id) {
         boolean flag = checkEmpty(ls_id, "Lecture Schedule Id") && isNumber(ls_id, "Lecture Schedule Id");
         return flag;
     }
     
     public static boolean checkBuId(String bu_id) {
         boolean flag = checkEmpty(bu_id, "User Id");
         return flag;
     }
     
     public static boolean checkPresentationTitle(String p_title) {
         boolean flag = checkEmpty(p_title, "Presentation Title");
        if (flag) {
            flag = p_title.matches("^[a-zA-Z0-9][a-zA-Z0-9: +-@&*()]*$");
            _errMsg = "Invalid symbols inside presentation title";
        }
         return flag;
     }
     
     public static boolean checkDescription(String description) {
         boolean flag = true;
         return flag;
     }
     
     public static boolean checkDuration(String duration) {
         boolean flag = checkEmpty(duration, "Duration") && isNumber(duration, "Duration");
         return flag;
     }
     
     public static boolean checkStartTime(String startTime) {
         boolean flag = checkEmpty(startTime, "Presentation Title");
         if (flag) {
             flag = startTime.matches("^[0-9]{2}:[0-9]{2}:[0-9]{2}$");
             _errMsg = "Start Time must in format: HH:MM:SS";
         }
         return flag;
     }
     
     public static boolean checkStartDateTime(String dateTime) throws ParseException {
         boolean flag = checkEmpty(dateTime, "Start Date Time");
         if (flag) {
             Date date = new Date();
             dateTime = dateTime.substring(0, 19);
             DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
             Date d1 = (Date) f.parse(dateTime);
             if (d1.compareTo(date) < 0) {
                 flag = false;
                 _errMsg = "Start Date Time must be later than current time";
             }
         }
         return flag;
     } 
 }    
