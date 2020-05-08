 package com.github.kenji0717.a3cs;
 
import jp.sourceforge.acerola3d.A23;

 /**
  * CarBattleを実行するためのクラスです。mainメソッドしかありません。
 * これで起動されるアプリでは、シミュレーションが実行できる他、
 * プログラムの作成コンパイルもできます。
  */
 public class CarBattle {
     /**
      * CarBattleを実行します。第1引数と第2引数でバトルさせたい
      * 2つのクラス名(パッケージ名も含めたもの)
      * を指定することができます。
      */
     public static void main(String args[]) {
        System.setSecurityManager(null);//gahaここは後で要確認
        A23.setClassLoader(CarBattle.class.getClassLoader());
         new CarBattleImpl(args);
     }
 }
