 package gov.usgs.cida.ncetl.servlet;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeFieldType;
 import org.joda.time.Days;
 import org.joda.time.Hours;
 import org.joda.time.Interval;
 import org.joda.time.Minutes;
 import org.joda.time.Months;
 import org.joda.time.Partial;
 import org.joda.time.MutableDateTime;
 import org.joda.time.ReadableInterval;
 import org.joda.time.ReadablePeriod;
 import org.joda.time.Seconds;
 import org.joda.time.Weeks;
 import org.joda.time.Years;
 
 /**
  *
  * @author jwalker
  */
 public class DateIndexer extends HttpServlet {
 
     //public static final String USAGE = "java DateIndexer \"${StartDate}\" \"${EndDate}\"";
     private static final long serialVersionUID = 1L;
     private static final int[] LEAP_DAY = {2, 29};
     public static final Map<String, ReadablePeriod> descriptionMap = Maps.newHashMap();
 
     static {
         descriptionMap.put("second", Seconds.ONE);
         descriptionMap.put("sec", Seconds.ONE);
         descriptionMap.put("s", Seconds.ONE);
         descriptionMap.put("minute", Minutes.ONE);
         descriptionMap.put("min", Minutes.ONE);
         descriptionMap.put("hour", Hours.ONE);
         descriptionMap.put("hr", Hours.ONE);
         descriptionMap.put("h", Hours.ONE);
         descriptionMap.put("day", Days.ONE);
         descriptionMap.put("d", Days.ONE);
         descriptionMap.put("week", Weeks.ONE);
         descriptionMap.put("month", Months.ONE);
         descriptionMap.put("quarter", Months.FOUR);
         descriptionMap.put("qtr", Months.FOUR);
         descriptionMap.put("year", Years.ONE);
         descriptionMap.put("yr", Years.ONE);
         descriptionMap.put("y", Years.ONE);
         descriptionMap.put("decade", Years.years(10));
         descriptionMap.put("century", Years.years(100));
     }
 
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         //options.addOption("s", "skip-day", true, "Do not output index for this day");
         String stride = request.getParameter("stride");
         String origin = request.getParameter("origin");
         String end = request.getParameter("end");
         String steps = request.getParameter("steps");
         String stepLength = request.getParameter("stepLength");
         String[] skipIntervals = request.getParameterValues("skipInterval");
         boolean skipLeapDays = (null != request.getParameter("leapSkip"));
 
         if (null != origin && null != end) {
             DateTime startDate = new DateTime(origin);
             DateTime endDate = new DateTime(end);
             ReadablePeriod stepPeriod = descriptionMap.get(stepLength);
             List<Partial> skipMe = Lists.newLinkedList();
             List<ReadableInterval> skipUs = Lists.newLinkedList();
             if (skipLeapDays) {
                 DateTimeFieldType[] fields = {DateTimeFieldType.monthOfYear(), DateTimeFieldType.dayOfMonth()};
                 Partial leapDay = new Partial(fields, LEAP_DAY);
                 skipMe.add(leapDay);
             }
            for (String interval : skipIntervals) {
                skipUs.add(new Interval(interval));
             }
             printSuccess(response, getTimesNoStride(startDate, endDate, stepPeriod, skipMe, skipUs));
         } else {
             printNotImplemented(response);
         }
     }
 
     /**
      * Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     public static String getTimesNoStride(DateTime origin, DateTime end, ReadablePeriod period, 
             List<Partial> skippedPartials, List<ReadableInterval> skippedIntervals) {
         StringBuilder strBuild = new StringBuilder();
         MutableDateTime current = new MutableDateTime(origin);
         int index = 0;
        date:
         while (current.isBefore(end)) {
            index++;
            current.add(period);
             for (Partial skip : skippedPartials) {
                 if (skip.isMatch(current)) {
                    continue date; //skip this timestep
                 }
             }
             for (ReadableInterval skipInt : skippedIntervals) {
                 if (skipInt.contains(current)) {
                    continue date; //skip this timestep
                 }
             }
            strBuild.append(Integer.toString(index)).append(" ");
         }
         return strBuild.toString();
     }
 
     private void printNotImplemented(HttpServletResponse response) throws IOException {
         response.setContentType("text/plain;charset=UTF-8");
         response.setStatus(response.SC_NOT_IMPLEMENTED);
         PrintWriter out = response.getWriter();
         out.print("This request is not yet supported, try something more simple");
     }
 
     private void printSuccess(HttpServletResponse response, String timesNoStride) throws IOException {
         response.setContentType("text/plain;charset=UTF-8");
         PrintWriter out = response.getWriter();
         out.print(timesNoStride);
     }
 }
