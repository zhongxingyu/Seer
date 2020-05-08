 package com.epam.kharkiv.cdp.oleshchuk.calc.algorithm;
 
 import com.epam.kharkiv.cdp.oleshchuk.calc.exception.CalcException;
 import com.epam.kharkiv.cdp.oleshchuk.calc.model.Sign;
 import com.epam.kharkiv.cdp.oleshchuk.calc.util.ListUtil;
 import com.epam.kharkiv.cdp.oleshchuk.calc.util.StringCalc;
 
import java.text.DecimalFormat;
 import java.util.List;
 import java.util.Stack;
 import java.util.StringTokenizer;
 
 public class ShuntingYard {
 
    public static String calculate(String inputString) {
         Stack<String> valuesStack = parseString(inputString);
         if (!valuesStack.empty() && valuesStack.size() == 1) {
            return (valuesStack.pop() + ".00");
         }
        DecimalFormat dtime = new DecimalFormat("0.00");
        Double result = makeCalculations(valuesStack);
        return dtime.format(result).replaceAll("\\," ,".");
     }
 
     private static Stack<String> parseString (String inputString) {
         List<String> numbersStack = new Stack<String>();
         Stack<Sign> signsStack = new Stack<Sign>();
         StringTokenizer stringTokenizer = new StringTokenizer(inputString, "+-/*()", true);
         while (stringTokenizer.hasMoreElements()) {
             String token = stringTokenizer.nextElement().toString();
             try {
                 Integer number = Integer.valueOf(token);
                 numbersStack.add(number.toString());
             } catch (NumberFormatException e) {
 
                 Sign sign = Sign.getSignFromString(token);
 
                 if (sign == Sign.LEFT) {
                     signsStack.push(sign);
                 } else if (sign == Sign.RIGHT) {
                     while (signsStack.peek() != Sign.LEFT) {
                         numbersStack.add(signsStack.pop().getValue());
                     }
                     signsStack.pop();
                 } else {
                     pushSign(numbersStack, signsStack, sign);
                 }
             }
         }
         while (!signsStack.empty()) {
             numbersStack.add(signsStack.pop().getValue());
         }
         return ListUtil.createStackFromList(numbersStack);
     }
 
     private static void pushSign(List<String> numbersStack, Stack<Sign> signsStack, Sign sign) {
             if (!signsStack.empty() && (signsStack.peek().getPriority() >= sign.getPriority())) {
                 numbersStack.add(signsStack.pop().getValue());
                 pushSign(numbersStack, signsStack, sign);
             } else {
                 signsStack.push(sign);
             }
     }
 
 
     private static Double makeCalculations(Stack<String> sourceStack) {
         Stack<String> numbersStack = new Stack<String>();
         Double result = 0.;
         while (!sourceStack.empty()) {
             String token = sourceStack.pop();
             try {
                 Double number = Double.valueOf(token);
                 numbersStack.push(number.toString());
             } catch (NumberFormatException e) {
                 Double lastNumber = Double.valueOf(numbersStack.pop());
                 Double preLastNumber = Double.valueOf(numbersStack.pop());
                 result = StringCalc.calculate(preLastNumber, lastNumber, token);
                 numbersStack.push(result.toString());
             }
         }
         if (numbersStack.size()!=1) {
             throw new CalcException("Too many numbers");
         }
         return result;
     }
 
 
 }
