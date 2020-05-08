 package me.kukkii.cointoss;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 public class Cointoss extends Activity{
 
   private Coin guess;
   private Coin coin;
   private int result = -1;
 
 
   public void onCreate(Bundle savedInstanceState){
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
   }
 
   public void guess(View view){
     guess = null;
     int id = view.getId();
     if(id==R.id.button_HEAD){
       guess = Coin.HEAD;
     }
     if(id==R.id.button_TAIL){
       guess = Coin.TAIL;
     }
     game();
   }
 
   public void coin(){
     int random = (int)(Math.random()*2);
     if(random == 1){
       coin = Coin.HEAD;
     }
     else{
       coin = Coin.TAIL;
     }
   }
 
   public void compare(Coin guess,Coin coin){
     if(guess == coin){
       result = 1;   //win
     }
     if(guess != coin){
       result = 0;   //lose
     }
   }
 
   public void showResult(int result){
     TextView textView = (TextView) findViewById(R.id.text);
     if(result == 1){
       textView.setText("win");
     }
     if(result == 0){
       textView.setText("lose");
     }
   }
 
   public void game(){
    coin();
     compare(guess,coin);
     showResult(result);
   }
 
 }
