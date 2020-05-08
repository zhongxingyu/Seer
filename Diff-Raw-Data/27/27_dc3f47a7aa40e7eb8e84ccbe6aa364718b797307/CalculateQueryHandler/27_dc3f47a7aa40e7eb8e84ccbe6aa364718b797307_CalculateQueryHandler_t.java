 package fr.ybonnel.codestory.query;
 
 
 import com.google.common.base.Throwables;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.Locale;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class CalculateQueryHandler extends AbstractQueryHandler {
 
     private Pattern patternParenthesis = Pattern.compile("\\((.*)\\)");
     private static final String NOMBRE = "\\-?\\d+\\.?\\d*";
     private Pattern patternPlus = Pattern.compile("(" + NOMBRE + ")\\+(" + NOMBRE + ")");
     private Pattern patternMultiple = Pattern.compile("(" + NOMBRE + ")\\*(" + NOMBRE + ")");
     private Pattern patternDivide = Pattern.compile("(" + NOMBRE + ")/(" + NOMBRE + ")");
 
    private NumberFormat format = new DecimalFormat("#0.#", new DecimalFormatSymbols(Locale.FRANCE));

    public CalculateQueryHandler() {
        format.setMaximumFractionDigits(500);
    }
 
     @Override
     public String getResponse(String query) {
         String calculateQuery = query.replace(' ', '+').replace(',', '.');
 
 
         try {
             calculateQuery = calculateWithParenthesis(calculateQuery);
         } catch (ParseException e) {
             Throwables.propagate(e);
         }
 
         try {
             BigDecimal retour = new BigDecimal(calculateQuery);
             return format.format(retour);
         } catch (NumberFormatException numberFormatException) {
             numberFormatException.printStackTrace();
             return null;
         }
     }
 
     private String calculateWithParenthesis(String calculateQuery) throws ParseException {
         Matcher matcherParenthsis = patternParenthesis.matcher(calculateQuery);
 
         while (matcherParenthsis.find()) {
             SearchParanthesis searchParanthesis = new SearchParanthesis(calculateQuery).invoke();
             int start = searchParanthesis.getStart();
             int end = searchParanthesis.getEnd();
 
             String queryBetweenParenthesis = calculateQuery.substring(start + 1, end - 1);
             String result = calculateWithoutParenthesis(queryBetweenParenthesis);
             calculateQuery = calculateQuery.substring(0, start) + result + calculateQuery.substring(end);
             matcherParenthsis = patternParenthesis.matcher(calculateQuery);
         }
 
         calculateQuery = calculateWithoutParenthesis(calculateQuery);
         return calculateQuery;
     }
 
     private String calculateWithoutParenthesis(String calculateQuery) throws ParseException {
         Matcher matcherDivide = patternDivide.matcher(calculateQuery);
 
         while (matcherDivide.find()) {
 
             BigDecimal a = new BigDecimal(matcherDivide.group(1));
             BigDecimal b = new BigDecimal(matcherDivide.group(2));
             BigDecimal result;
             try {
                 result = a.divide(b);
             } catch (ArithmeticException exception) {
                result = a.divide(b, 1000, RoundingMode.HALF_UP);
             }
             calculateQuery = calculateQuery.substring(0, matcherDivide.start()) + result.toString() + calculateQuery.substring(matcherDivide.end());
             matcherDivide = patternDivide.matcher(calculateQuery);
         }
 
         Matcher matcherMultiple = patternMultiple.matcher(calculateQuery);
 
         while (matcherMultiple.find()) {
             BigDecimal a = new BigDecimal(matcherMultiple.group(1));
             BigDecimal b = new BigDecimal(matcherMultiple.group(2));
             BigDecimal result = a.multiply(b);
             calculateQuery = calculateQuery.substring(0, matcherMultiple.start()) + result.toString() + calculateQuery.substring(matcherMultiple.end());
             matcherMultiple = patternMultiple.matcher(calculateQuery);
         }
 
         Matcher matcherPlus = patternPlus.matcher(calculateQuery);
 
         while (matcherPlus.find()) {
             BigDecimal a = new BigDecimal(matcherPlus.group(1));
             BigDecimal b = new BigDecimal(matcherPlus.group(2));
             BigDecimal result = a.add(b);
             calculateQuery = calculateQuery.substring(0, matcherPlus.start()) + result.toString() + calculateQuery.substring(matcherPlus.end());
             matcherPlus = patternPlus.matcher(calculateQuery);
         }
         return calculateQuery;
     }
 
 
     private class SearchParanthesis {
         private String calculateQuery;
         private int start;
         private int end;
 
         public SearchParanthesis(String calculateQuery) {
             this.calculateQuery = calculateQuery;
         }
 
         public int getStart() {
             return start;
         }
 
         public int getEnd() {
             return end;
         }
 
         public SearchParanthesis invoke() {
             start = -1;
             end = -1;
             int currentProf = 0;
             int currentStart = -1;
             int maxProf = -1;
             for (int index = 0; index < calculateQuery.length(); index++) {
                 char car = calculateQuery.charAt(index);
                 if (car == '(') {
                     currentStart = index;
                     currentProf++;
                 }
 
                 if (car == ')') {
                     if (currentProf > maxProf) {
                         start = currentStart;
                         end = index + 1;
                         maxProf = currentProf;
                     }
                     currentProf--;
                 }
             }
             return this;
         }
     }
 }
