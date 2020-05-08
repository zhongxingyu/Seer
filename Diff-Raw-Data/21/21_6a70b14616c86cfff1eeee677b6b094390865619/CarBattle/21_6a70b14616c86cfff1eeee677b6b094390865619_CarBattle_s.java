 package com.github.kenji0717.a3cs;
 
 /**
  * CarBattleを実行するためのクラスです。mainメソッドしかありません。
  */
 public class CarBattle {
     /**
      * CarBattleを実行します。第1引数と第2引数でバトルさせたい
      * 2つのクラス名(パッケージ名も含めたもの)
      * を指定することができます。
      */
     public static void main(String args[]) {
         new CarBattleImpl(args);
     }
 }
