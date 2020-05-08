 package com.github.kenji0717.a3cs;
 
 /**
  * CarRaceを実行するためのクラスです。mainメソッドしかありません。
  */
 public class CarRace {
     /**
      * CarRaceを実行します。第1引数でレースさせたい
      * クラス名(パッケージ名も含めたもの)
      * を指定することができます。
      */
     public static void main(String args[]) {
         new CarRaceImpl(args);
     }
 }
