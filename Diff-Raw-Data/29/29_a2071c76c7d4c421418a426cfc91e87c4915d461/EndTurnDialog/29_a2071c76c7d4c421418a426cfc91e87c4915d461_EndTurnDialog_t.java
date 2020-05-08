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
 
 public class EndTurnDialog extends Dialog implements OnClickListener{
 	Controller control;
 	
 	LayoutInflater inflater;
 	View layout;
 
 	Button tacticalMoveBtn, endTurnBtn, cancelBtn;
 	TextView questionText;
 	Context context;
 	
 	
 	public EndTurnDialog(Context context,Controller control, ViewGroup rootElement)
 	{
 		super(context);
 		
 		this.context = context;
 		this.control = control;
 		
		setCanceledOnTouchOutside(false);
		
 		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		layout = inflater.inflate(R.layout.dialog_endturn, rootElement);
 		setContentView(layout);
 		setTitle("End turn ?");	
 		
 		tacticalMoveBtn = (Button) findViewById(R.id.tacticalMoveBtn1);
 		tacticalMoveBtn.setOnClickListener(this);
 		
 		endTurnBtn = (Button) findViewById(R.id.endTurnBtn1);
 		endTurnBtn.setOnClickListener(this);
 		
 		cancelBtn = (Button) findViewById(R.id.cancelEndTurnBtn1);
 		cancelBtn.setOnClickListener(this);
 		
 		questionText = (TextView) findViewById(R.id.endTurnText);
 		questionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
 		questionText.setText("What do you command sir ?");
 	}
 
 	
 	public void onClick(View v) {
 		if(((Button) v).getId() == R.id.endTurnBtn1)
 		{
 			dismiss();
 			control.endTurn();
 		}
 		else if (((Button) v).getId() == R.id.tacticalMoveBtn1)
 		{
 			dismiss();
 			control.gotoTacticalMovePhase();
 		}
 		else if (((Button) v).getId() == R.id.cancelEndTurnBtn1)
 		{
 			dismiss();
 		}
 	}
 }
