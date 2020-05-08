 package dk.stacktrace.risk.gui;
 
 import dk.stacktrace.risk.R;
 import dk.stacktrace.risk.controller.Controller;
 import dk.stacktrace.risk.game_logic.Territory;
 import android.app.Dialog;
 import android.content.Context;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class TacticalMoveDialog extends Dialog implements OnSeekBarChangeListener, android.view.View.OnClickListener{
 	Controller control;
 	
 	LayoutInflater inflater;
 	View layout;
 	
 	Territory source, target;
 	
 	SeekBar troopSeekBar;
 	TextView sourceName, sourceTroops, targetName, targetTroops;
 	Button moveBtn, cancelBtn;
 	
 	int numOfTroopsToMove;
 	
 	public TacticalMoveDialog(Context context,Controller control, ViewGroup rootElement, Territory source, Territory target)
 	{
 		super(context);
 		this.source = source;
 		this.target = target;
 		this.control = control;
 		
		setCanceledOnTouchOutside(false);
		
 		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		layout = inflater.inflate(R.layout.dialog_tacticalmove, rootElement);
 		setContentView(layout);
 		setTitle("Tactical move");	
 		
 		troopSeekBar = (SeekBar) layout.findViewById(R.id.troopsSeekBar1);
 		troopSeekBar.setMax(this.source.getArmySize()-1);
     	troopSeekBar.setOnSeekBarChangeListener(this);
 		
 		sourceName = (TextView) layout.findViewById(R.id.textSource);
 		sourceName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
 		sourceName.setText(this.source.getName());
 		
 		sourceTroops = (TextView) layout.findViewById(R.id.textSourceTroops);
 		sourceTroops.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
 		sourceTroops.setText(this.source.getArmySize() + "");
 		
 		targetName = (TextView) layout.findViewById(R.id.textTarget);
 		targetName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
 		targetName.setText(this.target.getName());
 		
 		targetTroops = (TextView) layout.findViewById(R.id.textTargetTroops);
 		targetTroops.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
     	targetTroops.setText(this.target.getArmySize() + "");
     	
     	moveBtn = (Button) layout.findViewById(R.id.buttonMove);
     	moveBtn.setOnClickListener(this);
     	
     	cancelBtn = (Button) layout.findViewById(R.id.buttonCancel);
     	cancelBtn.setOnClickListener(this);
 	}
 
 	
 	public void onProgressChanged(SeekBar seekBar, int progress,
 			boolean fromUser) {
 		sourceTroops.setText((source.getArmySize() - progress) + "");
 		targetTroops.setText((target.getArmySize() + progress) + "");
 		numOfTroopsToMove = progress;
 	}
 
 	public void onStartTrackingTouch(SeekBar seekBar) {
 		
 		
 	}
 	
 
 	public void onStopTrackingTouch(SeekBar seekBar) {
 		
 		
 	}
 
 
 	public void onClick(View v) {
 		if(((Button) v).getId() == R.id.buttonCancel)
 		{
 			control.resetTerritorySelections();
 			dismiss();
 		}
 		else if (((Button) v).getId() == R.id.buttonMove)
 		{
 			control.tacticalMove(source, target, numOfTroopsToMove);
 			control.resetTerritorySelections();
 			control.endTurn();
 			dismiss();
 		}
 	}
 }
