 import java.io.PrintWriter;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Steve
  * Date: 10/21/13
  * Time: 6:07 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ProviderReport implements iReport
 {
     private String reportContent;
     private double feeTotal = 0;
     private int consultations = 0;
 
     public void sendReport(String destination)
     {
         try
         {
             PrintWriter out = new PrintWriter(destination);
             out.write(reportContent);
             out.close();
         }
         catch (Exception ex)
         {
             // TODO: log exception
         }
     }
 
     public void executeReport(Provider provider, Set<ServiceRecord> tempSet, MemberController members, ServiceController serviceController)
     {
         StringBuilder reportBuilder = new StringBuilder();
         // Header
         reportBuilder.append(String.format("Provider: %-20s Provider Number: %09d%n", provider.getName(), provider.getIdentifier()));
         reportBuilder.append(String.format("Address: %s%n", provider.getStreetAddress()));
         reportBuilder.append(String.format("         %s, %s %d%n", provider.getCity(), provider.getState(), provider.getZipcode()));
 
         // Body
         reportBuilder.append(String.format("%-17s%-21s%-27s%-11s%-14s%-7s%n", "Date of Service", "Date of Entry", "Member", "Member ID", "Service Code", "Fee"));
         for(ServiceRecord sr : tempSet)
         {
             double fee = serviceController.getService(sr.getServiceCode()).getServiceFee();
             String memberName = members.getMember(sr.getMemberID()).getName();
             reportBuilder.append(String.format("%-17s%-21s%-25s%09d  %6d        $%,5.2f%n",
                     getDateFromCalendar(sr.getDateOfService()),
                     getDateAndTimeFromCalendar(sr.getDateAndTimeServiceEntered()),
                     memberName.length() > 25 ? memberName.substring(0,25) : memberName,
                     sr.getMemberID(), sr.getServiceCode(), fee));
             feeTotal += fee;
             consultations++;
         }
 
         // Summary
         reportBuilder.append(String.format("%n%nTotal consultations: %d%n", consultations));
         reportBuilder.append(String.format(Locale.ENGLISH, "Total Weekly Fee: $%,5.2f%n", feeTotal));
         reportContent = reportBuilder.toString();
     }
 
     public double getFeeTotal()
     {
         return feeTotal;
     }
 
     public int getConsultations()
     {
         return consultations;
     }
 
     public String ToString()
     {
         return reportContent;
     }
 
     private String getDateFromCalendar(Calendar date)
     {
        return String.format("%02d-%02d-%4d", date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.YEAR));
     }
 
     private String getDateAndTimeFromCalendar(Calendar date)
     {
        return String.format("%02d-%02d-%4d %02d:%02d:%02d", date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.YEAR), date.get(Calendar.HOUR), date.get(Calendar.MINUTE), date.get(Calendar.SECOND));
     }
 }
