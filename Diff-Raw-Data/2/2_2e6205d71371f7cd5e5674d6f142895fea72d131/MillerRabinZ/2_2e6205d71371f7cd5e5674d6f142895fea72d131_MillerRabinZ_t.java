 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package kryptoprojekt.model;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.*;
 
 /**
  * Runs the Miller-Rabin primality test for natural numbers (PrimeType Z).
  *
  * @author Michael
  */
 public class MillerRabinZ extends MillerRabinTest<Z>{
 
     /**
      * Constructs a new MillerRabinZ object for natural numbers by using the given argumets.
      *
      * @param bases Bases to be used for the Miller-Rabin-Test per modul
      * @param moduls determine if these numbers are probably primes
      * @param calcProb true if 'moduls' is probably prime, otherwise false
      */
     public MillerRabinZ(Collection<Z> bases, Collection<Z> moduls, boolean calcProb){
             super(bases, moduls, calcProb);
         }
 
     /**
      * Starts the Miller-Rabin-Test for natural numbers.
      * @return List of results by using the Miller-Rabin-Test (whether 'modul' is probably prime, probability, intermediate values).
      * @throws IllegalArgumentException if the paramters are incorrect (bases have to be: 1 < base < moduls, moduls have to be: 1 < modul > bases
      */
     public ArrayList<Triple<Boolean, Double, LinkedList<String>>> test()
                 throws IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassCastException {
             
             boolean checkPrimeArgAnswer = checkPrimeArguments().first();
             String argsCorrectMessage = checkPrimeArguments().second();
             
             ArrayList<Triple<Boolean, Double, LinkedList<String>>> primeResult = new ArrayList<Triple<Boolean, Double, LinkedList<String>>>();
             if (checkPrimeArgAnswer) {
                 double probability = calculateProbability(bases);
                 for (Z checkPrime : moduls){
                     boolean isPrime = millerRabinCheck(checkPrime);
                     if (isPrime){
                         if (calcProb) {
                             //Postcondition
                             assert checkPrimeArgAnswer == true && isPrime == true && calcProb == true: "checkPrimeArgAnswer or isPrime have a false state: checkPrimeArgAnswer = " +checkPrimeArgAnswer+ ", isPrime = " +isPrime;
                             primeResult.add(new Triple<Boolean, Double, LinkedList<String>>(isPrime, probability, intermediateValues));
                             continue;                            
                         } else{
                             assert checkPrimeArgAnswer == true && isPrime == true && calcProb == false: "checkPrimeArgAnswer or isPrime have a false state: checkPrimeArgAnswer = " +checkPrimeArgAnswer+ ", isPrime = " +isPrime;
                             //return no probability
                             primeResult.add(new Triple<Boolean, Double, LinkedList<String>>(isPrime, -1.0, intermediateValues));
                             continue;
                         }
                     }else{
                         //Postcondition
                         assert checkPrimeArgAnswer == true && isPrime == false: "checkPrimeArgAnswer or isPrime have a false state: checkPrimeArgAnswer = " +checkPrimeArgAnswer+ ", isPrime = " +isPrime;
                         if(calcProb){
                             primeResult.add(new Triple<Boolean, Double, LinkedList<String>>(false, 1.0, intermediateValues));
                             continue;
                         }else{
                             primeResult.add(new Triple<Boolean, Double, LinkedList<String>>(false, -1.0, intermediateValues));
                             continue;
                         }
                     }
                 }                
                 return primeResult;
             } else{
                 throw new IllegalArgumentException(argsCorrectMessage);
             }
         }
 
         //checks whether the parameter values are correct: probably prime greater than 1 and base '1 < base < modul'
         private Tuple<Boolean, String> checkPrimeArguments()
             throws IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassCastException {
         //Precondition
         assert Set.class.isAssignableFrom(bases.getClass()): "check that bases contains no dublicate elements: Liste hs = " +bases;
 
         boolean argsCorrect = true;
         String argsAnswer = "Arguments are correct.";
         Z one = new Z("1");
         Z two = new Z("2");
 
         //checks whether the probably primes are greater than 1
         if (argsCorrect && getLowestModul().compareTo(new Z(1)) <= 0) {
             argsCorrect = false;
             argsAnswer = "There are only prime numbers >1";
         }
 
         //checks whether 'bases' > 1 && bases < checkPrime
         if (argsCorrect && !bases.isEmpty()){
             if (getLowestBase().compareTo(new Z(1)) < 1) {
                 argsCorrect = false;
                 argsAnswer = "Base 'a' too small. Miller-Rabin-Test requires a base:  1 < a < prime";
             }
             //if the smallest 'modul' is 2, go to the next else-if
             if (argsCorrect && getHighestBase().compareTo(getLowestModul())>=0 && !getLowestModul().equals(two)){
                 argsCorrect = false;
                 argsAnswer = "Base 'a' too large. Miller-Rabin-Test requires a base:  1 < a < prime";
             }else if(getHighestBase().compareTo(getLowestModul())>=0 && getLowestModul().equals(two)){
                 if(moduls.size() > 1){
                     Iterator<Z> itModuls = moduls.iterator();
                     itModuls.next();
                     if(getHighestBase().compareTo(itModuls.next()) >=0){
                         argsCorrect = false;
                         argsAnswer = "Base 'a' too large. Miller-Rabin-Test requires a base:  1 < a < prime";
                     }
                 }
             }
         }else if(argsCorrect && bases.isEmpty()){
             argsCorrect = false;
             argsAnswer = "It requires at least one base >1 and <n.";
         }
         //Postcondition
         assert getLowestModul().compareTo(new Z(1)) >0 || !argsCorrect: "checkprime isn't > 1: checkPrime = " +getLowestModul();
         assert getLowestBase().compareTo(new Z(1)) >0 || getLowestModul().equals(new Z(2)) || !argsCorrect: "base isn't > 1: base = " +getLowestBase();
         return new Tuple<Boolean, String>(argsCorrect, argsAnswer);
         }
 
         //checks wheter the parameter is a prime number
         private boolean millerRabinCheck(Z checkPrime)
                  throws IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassCastException {
             //the exponent will be substracted from 1
             Z oneObj = new Z(1);
             //the exponent will be multiplied by 2
             Z twoObj = new Z(2);
             intermediateValues = new LinkedList<String>();
 
             if(!checkPrime.mod(new Z("2")).isZERO()){
                 //1. return value: power to the base 2, 2. return value: odd factor
                 Tuple<Z, Z> factors = factorizeEven(checkPrime.subtract(oneObj));
                 if(factors.first().equals(new Z(-1)) || factors.second().equals(new Z(-1))){
                     throw new RuntimeException("The passed number couldn't be factored.");
                 }
                 intermediateValues.add("n-1 = (2^" +factors.first()+ ") * " +factors.second());
                 //contains the power after factorization
                 Z exponent = factors.first();
                 //contain the odd factor after factorization
                 Z oddFactor = factors.second();
                 //contains the max. power that can be use to potentiate checkBasis with oddFactor after factorization
                 Z maxPower = Basic.squareAndMultiply(twoObj, exponent.subtract(oneObj)).first();
                 int assertPostCondCounter = 0;
                 //contains the value after the FermatTest step
                 Z firstTest;
                 Z result;
                 nextBase:
                 for (Z base : bases) {
                     ++assertPostCondCounter;
                     firstTest = Basic.squareAndMultiply(base, oddFactor, checkPrime).first();
                     intermediateValues.add(base+ "^" +oddFactor+ " mod " +checkPrime+ " = " +firstTest);
                     if((firstTest).isONE() || firstTest.equals(checkPrime.subtract(oneObj))) {
                         intermediateValues.add("");
                         continue;
                     } else{
                         Z newBase = firstTest;
                         int potenzK = 1; //is the 'k' power
                         for (Z twoFactor = twoObj; twoFactor.compareTo(maxPower) <= 0; twoFactor=twoFactor.multiply(twoObj)){
                             assert potenzK <= maxPower.intValue(): "Too many Iterations. assertZaehler: "+potenzK+ ", maxPower: " +maxPower ;
                             result = Basic.squareAndMultiply(newBase, twoFactor, checkPrime).first();
                             intermediateValues.add("(" +newBase+ ")^2^" +potenzK+ " mod " +checkPrime+ " = " +result);
                             //n-1 than checkPrime is a prime number
                             if (result.equals(new Z(checkPrime.subtract(oneObj).toString()))){
                                 intermediateValues.add("");
                                 continue nextBase;
                             }
                             ++potenzK;
                         }
                         return false; //the test result was always not equal to n-1
                     }
                 }
                 //Postcondition
                 assert assertPostCondCounter == bases.size(): "There have not tested all bases.";
                 return true;
             }else if(checkPrime.equals(twoObj)){
                 //Precondition
                 assert Integer.parseInt(checkPrime.toString()) == 2: "Error, checkPrime != 2. checkPrime: " +checkPrime.toString();
                 intermediateValues.add(checkPrime+ " = 1");
                 return true; //2 is a prime (100%)
             }else{
                intermediateValues.add("n-1 = odd number --> you passed an even number.");
                 return false; //it was passed an even number, it's not a prime (100%)
             }
         }
 }
