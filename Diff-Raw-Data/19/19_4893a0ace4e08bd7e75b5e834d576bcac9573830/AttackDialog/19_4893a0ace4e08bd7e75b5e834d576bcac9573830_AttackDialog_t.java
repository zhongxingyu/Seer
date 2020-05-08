 package dk.stacktrace.risk.gui;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import dk.stacktrace.risk.R;
 import dk.stacktrace.risk.controller.Controller;
 import dk.stacktrace.risk.game_logic.Territory;
 
 public class AttackDialog extends Dialog implements OnClickListener{
 	Controller control;
 	
 	LayoutInflater inflater;
 	View layout;
 	
 	Territory source, target;
 	Button fightBtn, retreatBtn;
 	
 	
 	public AttackDialog(Context context,Controller control, ViewGroup rootElement, Territory source, Territory target)
 	{
 		super(context);
 		this.source = source;
 		this.target = target;
 		this.control = control;
 		
 		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		layout = inflater.inflate(R.layout.dialog_attack, rootElement);
 		setContentView(layout);
 		setTitle("You are attacking " + target.getOwner().getName() + " at " + target.getName());	
 		
 		fightBtn = (Button) findViewById(R.id.fightBtn);
 		fightBtn.setOnClickListener(this);
 		
 		retreatBtn = (Button) findViewById(R.id.retreatBtn);
 		retreatBtn.setOnClickListener(this);
 	}
 
 
 	public void onClick(View v) {
 		if(((Button) v).getId() == R.id.retreatBtn)
 		{
 			dismiss();
 		}
 		else if (((Button) v).getId() == R.id.fightBtn)
 		{
 			dismiss();
 		}
 	}
 }
