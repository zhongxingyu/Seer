 package me.kukkii.janken;
 
 import me.kukkii.janken.bot.BotManager;
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.Button;
 import android.view.View;
import android.widget.Toast;
 
 public class Janken extends Activity
 {
   public void onCreate(Bundle savedInstanceState){
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
   }
 
   public void hand(View view){
    // Button button = (Button) findViewById(R.id.button_BOT);
     Hand userHand = null;
     int id = view.getId();
     if(id==R.id.button_ROCK){
       userHand = Hand.ROCK;
     }
     if(id==R.id.button_SCISSOR){
       userHand = Hand.SCISSOR;
     }
     if(id==R.id.button_PAPER){
       userHand = Hand.PAPER;
     }
     Player bot = BotManager.getManager().next();
     Hand botHand = bot.hand2();
     Judge judge = new Judge();
     Result result = judge.judge(userHand, botHand);

    String text = bot.getName() + "\n" + userHand.toString() +"\n" + botHand.toString() + "\n" + result.toString();
    // button.setText(text);
    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
   }
 }
