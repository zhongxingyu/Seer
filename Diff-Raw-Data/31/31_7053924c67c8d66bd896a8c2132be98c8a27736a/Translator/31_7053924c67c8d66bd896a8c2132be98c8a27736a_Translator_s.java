 package org.javascool.proglets.cryptageRSA;
 
 /** Définit le traducteur de langage pour cette proglet.
  * 
  * @see <a href="Translator.java.html">code source</a>
  * @serial exclude
  */
 public class Translator extends org.javascool.core.Translator {
   @Override
   public String getImports() {
     return "import java.math.BigInteger;";
   }
   @Override
   public String translate(String code) {
    return code.replaceAll("\\s(createKeys|encrypt|decrypt)\\(", " cryptageRSA.$1(");
   }
 }
