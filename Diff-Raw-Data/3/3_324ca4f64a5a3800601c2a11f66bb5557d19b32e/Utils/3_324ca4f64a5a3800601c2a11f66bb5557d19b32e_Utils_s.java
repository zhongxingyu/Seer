 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package datamining;
 
 import java.util.LinkedList;
 import static java.lang.Math.*;
 
 /**
  *
  * @author igor
  */
 public class Utils {
     
     /**
      * Retorna uma opcao sinalizada por uma flag  no array de opcoes.
      * 
      * @param flag    sinalizador da opcao desejada
      * @param options array com sinalizadores e opcoes
      * 
      * @return o valor da opcao desejada
      */
     public static String getOption(String flag, String[] options) throws Exception {
         int i = 0;
         String readFlag = options[i];
 
         while ((i < options.length) && !readFlag.equals("-" + flag)) {
             readFlag = options[i];
             i = i + 2;
         }
 
         if (!readFlag.equals("-" + flag)) {
             throw new Exception("A opcao " + flag + " nao pode ser encontrada!");
         }
         return options[i + 1];
     }
     
     /**
      * Efetua o calculo da entropia de Shannon para uma colecao de
      * probabilidades
      * 
      * @param probs colecao de probabilidades
      * 
      * @return o valor da entropia para as probabilidades passadas
      */
     public static Double entropy(LinkedList<Double> probs) {
         double entropyValue = 0;
         while (!probs.isEmpty()) {
             double px = probs.poll().doubleValue();
             entropyValue += (log(px)/log(2));
         }
         return Double.valueOf(entropyValue);
     }
 
 }
