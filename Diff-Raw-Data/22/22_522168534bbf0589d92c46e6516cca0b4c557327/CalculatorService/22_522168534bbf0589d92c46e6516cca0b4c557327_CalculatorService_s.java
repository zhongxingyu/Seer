 package ws.calculator;
 
 import javax.jws.WebService;
 
 @WebService()
 public class CalculatorService {
 
     public int add(int op1, int op2) {
        return op1 + op2;
     }
 
     public int subtract(int op1, int op2) {
        return op1 - op2;
     }
 
     public int multiply(int op1, int op2) {
        return op1 * op2;
     }
 
     public int divide(int op1, int op2) throws ArithmeticException {
//       if (op2 == 0)
//           throw new ArithmeticException();
        
         return op1 / op2;
     }
 }
