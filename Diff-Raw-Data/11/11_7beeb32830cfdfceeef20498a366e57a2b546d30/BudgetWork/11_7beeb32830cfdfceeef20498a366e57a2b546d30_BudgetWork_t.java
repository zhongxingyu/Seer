 package com.four_envelope.android.budget;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import android.util.Log;
 
 import com.four_envelope.android.model.Account;
 import com.four_envelope.android.model.ActualExpense;
 import com.four_envelope.android.model.ActualGoalCredit;
 import com.four_envelope.android.model.ActualIncome;
 import com.four_envelope.android.model.DailyExpense;
 import com.four_envelope.android.model.Execution;
 import com.four_envelope.android.model.Person;
 import com.four_envelope.android.model.User;
 
 /**
  * Work operations with envelope budget
  * 
  * @author VMaximenko
  * 
  */
 public class BudgetWork {
 
 	public static User userData;
 	
 	private static final DateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
 	private static final DateFormat dfTitle = new SimpleDateFormat("E, dd MMM");
 	private static final String moneyFormat = "%1$.0f %2$s"; // "%1$.2f %2$s"
 
 	/**
 	 * Prepare budget for selected day of week
 	 * 
 	 * @param showDate
 	 * @param execution
 	 * @return
 	 */
 	public static DailyBudget prepareDailyBudget(Date showDate, Execution execution) {
 		DailyBudget dailyBudget = new DailyBudget();
 
 		dailyBudget.executionDate = showDate;
 		dailyBudget.date = dfDate.format(showDate);
 		dailyBudget.title = dfTitle.format( dailyBudget.executionDate );
 		dailyBudget.execution = execution;
 		dailyBudget.processEnvelope();
 
 		ArrayList<ActualIncome> actualIncomes = execution.getActualIncomes();
 		if (actualIncomes != null)
 			for (ActualIncome actualIncome : actualIncomes)
 				if (dailyBudget.date.equals( actualIncome.getActualDate() ))
 					dailyBudget.actualIncomes.add(actualIncome);
 
 		ArrayList<ActualExpense> actualExpenses = execution.getActualExpenses();
 		if (actualExpenses != null)
 			for (ActualExpense actualExpense : actualExpenses)
 				if (dailyBudget.date.equals( actualExpense.getActualDate() ))
 					dailyBudget.actualExpenses.add(actualExpense);
 
 		ArrayList<ActualGoalCredit> actualGoalCredits = execution.getActualGoalCredits();
 		if (actualGoalCredits != null)
 			for (ActualGoalCredit actualGoalCredit : actualGoalCredits)
 				if (dailyBudget.date.equals( actualGoalCredit.getActualDate() ))
 					dailyBudget.actualGoalCredits.add(actualGoalCredit);
 
 		return dailyBudget;
 	}
 
 	public static Calendar envelopeBegin(Date date) {
 		int firstDayOfWeek = new Integer(1).equals( userData.getFirstDayOfWeek() ) ? Calendar.SUNDAY : Calendar.MONDAY;
 		
 		Calendar cal = Calendar.getInstance();
 		cal.clear();
 		cal.setFirstDayOfWeek(firstDayOfWeek);
 		cal.setTime(date);
 		
 		cal.set(Calendar.DAY_OF_WEEK, firstDayOfWeek );
 
 		return cal;
 	}
 
 	public static String calcEnvelopeBegin(Date date) {
 		return dfDate.format( envelopeBegin(date).getTime() );
 	}
 
 	public static String calcPreviousEnvelopeBegin(Date date) {
 		Calendar cal = envelopeBegin(date);
 		cal.add( Calendar.DATE, -7 );
 		
 		return dfDate.format( cal.getTime() );
 	}
 
 	public static String calcNextEnvelopeBegin(Date date) {
 		Calendar cal = envelopeBegin(date);
 		cal.add( Calendar.DATE, 7 );
 		
 		return dfDate.format( cal.getTime() );
 	}
 	
 	/**
 	 * Format money value
 	 * @param sum
 	 * @param currency
 	 * @return
 	 */
 	public static CharSequence formatMoney(Float sum, String currency) {
 		return String.format( moneyFormat, sum, currency );
 	}
 	public static CharSequence formatMoney(Float sum) {
 		return formatMoney( sum, userData.getCurrency().getValue() );
 	}
 
 	/**
 	 * Reformat date
 	 * @param showDate
 	 * @return
 	 * @throws ParseException
 	 */
 	public static CharSequence formatDate(String showDate) {
 		try {
 			return dfTitle.format( dfDate.parse( showDate ) );
 			
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return showDate;
 		}
 	}
 
 	public static String formatDate(Date date) {
 			return dfTitle.format(date);
 	}
 
 	
 	public static DailyExpense getPersonDailyExpense(Person person, String date) {
 		ArrayList<DailyExpense> expenses = person.getDailyExpenses();
 		if (expenses == null || expenses.size() == 0)
 			return null;
 		
 		for (DailyExpense dailyExpense : expenses)
 			if ( dailyExpense.getDate().equals( date ) )
 				return dailyExpense;
 				
 		return null;
 	}
 	
 	/**
 	 * Count one day person sum expense
 	 */
 	public static CharSequence getPersonDailyExpenseSum(DailyExpense expense) {
 		if (expense == null)
 			return "";
 		
 		Float sum = expense.getSum();
 		if (sum == null || sum.equals( new Float(0f) ))
 			return "";
 		else
 			return formatMoney( sum, userData.getCurrency().getValue() );
 	}
 
 	/**
 	 * Check person hasn't set daily expenses for the execution day earlier
 	 * than today
 	 */
 	public static boolean checkPersonHasntSetDailyExpense(DailyExpense expense) {
 		if (expense == null)
			return true;
 		
 		Float sum = expense.getSum();
		if (sum > 0)
			return false;
 		
 		Date today = new Date();
 		Date executionDate;
 		try {
 			executionDate = dfDate.parse( expense.getDate() );
 		} catch (ParseException e) {
 			executionDate = today;
 		}
 
 // is date before today
 		boolean executionBeforeToday = false;
 		if ( !(executionDate.getYear() == today.getYear() &&
 				executionDate.getMonth() == today.getMonth() &&
 				executionDate.getDate() == today.getDate() )	)
 			executionBeforeToday = executionDate.before(today);
 		
 		return sum.equals( new Float(0f) ) && executionBeforeToday;
 	}
 
 // user accounts information
 	public static ArrayList<String> getUserAccounts() {
 		ArrayList<String> userAccounts = new ArrayList<String>();
 		
 		for (Account account : userData.getAccounts()) {
 			StringBuffer str = new StringBuffer( account.getName() );
 			str.append(" [ ");
 			str.append( account.getValue() );
 			str.append(" ]");
 
 			userAccounts.add( str.toString() );
 		}
 		
 		return userAccounts;
 	}	
 
 }
