 package dk.stacktrace.risk.gui;
 
 import dk.stacktrace.risk.Main;
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
 
 public class PostBattleMoveDialog extends Dialog implements OnSeekBarChangeListener, android.view.View.OnClickListener{
 	Controller control;
 	Context context;
 	LayoutInflater inflater;
 	View layout;
 	
 	Territory source, target;
 	
 	SeekBar troopSeekBar;
 	TextView sourceName, sourceTroops, targetName, targetTroops;
 	Button moveBtn;
 	
 	int numOfTroopsToMove;
 	
 	public PostBattleMoveDialog(Context context,Controller control, ViewGroup rootElement)
 	{
 		super(context);
 		
 		this.context = context;
 		this.control = control;
 		this.source = control.getBattle().getAttackingTerritory();
 		this.target = control.getBattle().getDefendingTerritory();
 		
		setCancelable(false);
 		
 		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		layout = inflater.inflate(R.layout.dialog_postbattletacticalmove, rootElement);
 		setContentView(layout);
 		setTitle("Post Battle Tactical Move");	
 		
 		troopSeekBar = (SeekBar) layout.findViewById(R.id.troopsSeekBar1);
 		troopSeekBar.setMax(control.getBattle().getAttackingArmy());
 		troopSeekBar.setProgress(troopSeekBar.getMax());
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
     	targetTroops.setText(this.target.getArmySize() + troopSeekBar.getMax() + "");
     	
     	moveBtn = (Button) layout.findViewById(R.id.buttonMove);
     	moveBtn.setOnClickListener(this);
 	}
 
 	
 	public void onProgressChanged(SeekBar seekBar, int progress,
 			boolean fromUser) {
 		sourceTroops.setText((source.getArmySize() + (troopSeekBar.getMax() - progress)) + "");
 		targetTroops.setText((target.getArmySize() + progress) + "");
 		numOfTroopsToMove = progress;
 	}
 
 	public void onStartTrackingTouch(SeekBar seekBar) {
 		
 		
 	}
 	
 
 	public void onStopTrackingTouch(SeekBar seekBar) {
 		
 		
 	}
 
 
 	public void onClick(View v) {
 		if (((Button) v).getId() == R.id.buttonMove)
 		{
 			source.reinforce(troopSeekBar.getMax() - troopSeekBar.getProgress());
 			target.reinforce(troopSeekBar.getProgress());
 			control.resetTerritorySelections();
 			((Main) context).update();
 		
 			dismiss();
 		}
 	}
 }
