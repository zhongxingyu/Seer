 import java.util.GregorianCalendar;
 
 public class NotieImberisseo {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		GregorianCalendar gcal = new GregorianCalendar();
 		//System.out.println(gcal.get(gcal.YEAR)+" "+gcal.get(gcal.MONTH)+" "+gcal.get(gcal.DAY_OF_MONTH));
		ImladrisCalendar cal = new ImladrisCalendar(gcal.get(GregorianCalendar.YEAR), gcal.get(GregorianCalendar.MONTH), gcal.get(GregorianCalendar.DAY_OF_MONTH));
 		//ImladrisCalendar cal = new ImladrisCalendar(new GregorianCalendar());
 		
 		// print info
 		/*System.out.println("yen: "+cal.getYen());
 		System.out.println("loa: "+cal.getLoa());
 		System.out.println("loaBeg: "+cal.getLoaBeginingDay());
 		System.out.println("dayOfLoa: "+cal.getDayOfLoa());
 		System.out.println("isLeapLoa: "+cal.isLeapLoa());
 		System.out.println("period: "+cal.getPeriodOfLoa());
 		System.out.println("isInMonth: "+cal.isInMonth());
 		System.out.println("month: "+cal.getMonthOfLoa());
 		System.out.println("dayOfPeriod: "+cal.getDayOfPeriod());
 		System.out.println("weekOfPeriod: "+cal.getWeekOfPeriod());
 		System.out.println("dayOfWeek: "+cal.getDayOfWeek());*/
 		// print date
 		//System.out.print("Today is ");
 		if(cal.isInMonth()) {
 			System.out.print(cal.getDayOfWeek()+", "+cal.getPeriodOfLoa()+" "+cal.getDayOfPeriod()+", ");
 		}
 		else if(cal.getPeriodOfLoaInt() == ImladrisCalendar.ENDERI) {
 			System.out.print(cal.getPeriodOfLoa()+" "+cal.getDayOfPeriod()+", ");
 		}
 		else {
 			System.out.print(cal.getPeriodOfLoa()+", ");
 		}
 		System.out.println(cal.getYen()+" "+cal.getLoa());
 	}
 	
 }
