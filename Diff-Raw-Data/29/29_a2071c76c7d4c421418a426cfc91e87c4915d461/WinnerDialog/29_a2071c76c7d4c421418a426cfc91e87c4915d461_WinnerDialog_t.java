 package dk.stacktrace.risk.gui;
 
 import java.util.ArrayList;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.os.Handler;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import dk.stacktrace.risk.Main;
 import dk.stacktrace.risk.R;
 import dk.stacktrace.risk.controller.Controller;
 import dk.stacktrace.risk.game_logic.Territory;
 import dk.stacktrace.risk.game_logic.battle.Battle;
 import dk.stacktrace.risk.game_logic.battle.Dice;
 
 public class WinnerDialog extends Dialog implements OnClickListener{
 	Controller control;
 	
 	LayoutInflater inflater;
 	View layout;
 
 	Button rematchBtn, newGameBtn, quitBtn;
 	TextView winnerText;
 	Context context;
 	
 	
 	public WinnerDialog(Context context,Controller control, ViewGroup rootElement)
 	{
 		super(context);
 		
 		this.context = context;
 		this.control = control;
 		
		setCanceledOnTouchOutside(false);
		
 		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		layout = inflater.inflate(R.layout.dialog_winner, rootElement);
 		setContentView(layout);
 		setTitle("World Domination");	
 		
 		rematchBtn = (Button) findViewById(R.id.rematchBtn);
 		rematchBtn.setOnClickListener(this);
 		
 		newGameBtn = (Button) findViewById(R.id.newGameBtn);
 		newGameBtn.setOnClickListener(this);
 		
 		quitBtn = (Button) findViewById(R.id.quitBtn);
 		quitBtn.setOnClickListener(this);
 		
 		winnerText = (TextView) findViewById(R.id.winnerText);
 		winnerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
 		winnerText.setText("The Winner Is " + control.getGameWinner().getName());
 	}
 
 	
 	public void onClick(View v) {
 		if(((Button) v).getId() == R.id.rematchBtn)
 		{
 			dismiss();
 		}
 		else if (((Button) v).getId() == R.id.newGameBtn)
 		{
 			dismiss();
 		}
 		else if (((Button) v).getId() == R.id.quitBtn)
 		{
 			dismiss();
 		}
 	}
 }
