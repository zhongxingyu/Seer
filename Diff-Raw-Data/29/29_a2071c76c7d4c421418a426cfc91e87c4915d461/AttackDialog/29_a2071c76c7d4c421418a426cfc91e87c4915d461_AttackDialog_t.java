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
 
 public class AttackDialog extends Dialog implements OnClickListener{
 	Controller control;
 	
 	LayoutInflater inflater;
 	View layout;
 	
 	Territory source, target;
 	Battle battle;
 	Button fightBtn, retreatBtn;
 	TextView attackingTerritory, attackingArmy, defendingTerritory, defendingArmy;
 	ImageView attackDice1, attackDice2, attackDice3, defendDice1, defendDice2;
 	ArrayList<ImageView> attackDices, defendDices;
 	Context context;
 	
 	public AttackDialog(Context context,Controller control, ViewGroup rootElement)
 	{
 		super(context);
 		
 		this.context = context;
 		this.control = control;
 		this.battle = control.getBattle();
 		this.source = battle.getAttackingTerritory();
 		this.target = battle.getDefendingTerritory();
 		
		setCanceledOnTouchOutside(false);
		
 		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		layout = inflater.inflate(R.layout.dialog_attack, rootElement);
 		setContentView(layout);
 		setTitle(source.getOwner().getName() + " is attacking " + target.getOwner().getName() + "@" + target.getName());	
 		
 		fightBtn = (Button) findViewById(R.id.fightBtn);
 		fightBtn.setOnClickListener(this);
 		
 		retreatBtn = (Button) findViewById(R.id.retreatBtn);
 		retreatBtn.setOnClickListener(this);
 		
 		attackingTerritory = (TextView) findViewById(R.id.attackTerritory);
 		attackingTerritory.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
 		
 		attackingArmy = (TextView) findViewById(R.id.attackArmy);
 		attackingArmy.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
 		
 		defendingTerritory = (TextView) findViewById(R.id.defendTerritory);
 		defendingTerritory.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
 		
 		defendingArmy = (TextView) findViewById(R.id.defendArmy);
 		defendingArmy.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
 
 		initDices();
 		update();
 		
 	}
 
 	
 	
 	private void initDices() {
 		attackDices = new ArrayList<ImageView>();
 		attackDice1 = (ImageView) findViewById(R.id.attackDice1);
 		attackDices.add(attackDice1);
 		attackDice2 = (ImageView) findViewById(R.id.attackDice2);
 		attackDices.add(attackDice2);
 		attackDice3 = (ImageView) findViewById(R.id.attackDice3);
 		attackDices.add(attackDice3);
 
 		defendDices = new ArrayList<ImageView>();
 		defendDice1 = (ImageView) findViewById(R.id.defendDice1);
 		defendDices.add(defendDice1);
 		defendDice2 = (ImageView) findViewById(R.id.defendDice2);
 		defendDices.add(defendDice2);
 
 		hideDices();
 		
 		ArrayList<Dice> initialAttackDices = battle.getInitialAttackDices();
 		int numAttackDices = initialAttackDices.size(); 
 		for (int i = 0;i < numAttackDices; i++)
 		{
 			attackDices.get(i).setImageResource(getDiceId(initialAttackDices.get(i)));
 			attackDices.get(i).setVisibility(View.VISIBLE);
 		}
 		
 		ArrayList<Dice> initialDefendDices = battle.getInitialDefendDices();
 		int numDefendDices = initialDefendDices.size();
 		for (int i = 0;i < numDefendDices; i++)
 		{
 			defendDices.get(i).setImageResource(getDiceId(initialDefendDices.get(i)));
 			defendDices.get(i).setVisibility(View.VISIBLE);
 		}
 	}
 
 	private void hideDices()
 	{
 		for (ImageView dice : defendDices)
 		{
 			dice.setVisibility(View.INVISIBLE);
 		}
 		for (ImageView dice : attackDices)
 		{
 			dice.setVisibility(View.INVISIBLE);
 		}
 	}
 
 
 	public void onClick(View v) {
 		if(((Button) v).getId() == R.id.retreatBtn)
 		{
 			dismiss();
 			control.retreat();
 		}
 		else if (((Button) v).getId() == R.id.fightBtn)
 		{
 			fight();
 		}
 	}
 	
 	public void update()
 	{
 	attackingTerritory.setText(source.getName());
 	attackingArmy.setText(battle.getAttackingArmy() + "");
 	defendingTerritory.setText(target.getName());
 	defendingArmy.setText(target.getArmySize() + "");
 	}
 	
 	private void updateDices()
 	{
 		hideDices();
 		
 		ArrayList<Dice> newAttackDices = battle.getAttackDices();
 		int numAttackDices = newAttackDices.size(); 
 		for (int i = 0;i < numAttackDices; i++)
 		{
 			attackDices.get(i).setImageResource(getDiceId(newAttackDices.get(i)));
 			attackDices.get(i).setVisibility(View.VISIBLE);
 		}
 		
 		ArrayList<Dice> newDefendDices = battle.getDefendDices();
 		int numDefendDices = newDefendDices.size();
 		for (int i = 0;i < numDefendDices; i++)
 		{
 			defendDices.get(i).setImageResource(getDiceId(newDefendDices.get(i)));
 			defendDices.get(i).setVisibility(View.VISIBLE);
 		}
 	}
 	
 	private void fight()
 	{
 		battle.fight();
 		update();
 		updateDices();
 		if (battle.battleIsOver())
 		{
 			fightBtn.setOnClickListener(null);
 			retreatBtn.setOnClickListener(null);
 			setTitle(battle.getLoser().getName() + " gets owned by " + battle.getWinner().getName());
 			
 			Handler handler = new Handler(); 
 			    handler.postDelayed(new Runnable() { 
 			         public void run() { 
 			              dismiss();
 			              control.postBattle();
 			              ((Main)context).update();
 			         } 
 			    }, 2000);
 			    
 		}
 		
 		
 	}
 	
 	
 	
 	private int getDiceId(Dice dice)
 	{
 		switch (dice.getDice()) {
 		case 1:
 			return dice.isAttackDice() ? dk.stacktrace.risk.R.drawable.a1 : dk.stacktrace.risk.R.drawable.d1;
 		case 2:
 			return dice.isAttackDice() ? dk.stacktrace.risk.R.drawable.a2 : dk.stacktrace.risk.R.drawable.d2;
 		case 3:
 			return dice.isAttackDice() ? dk.stacktrace.risk.R.drawable.a3 : dk.stacktrace.risk.R.drawable.d3;
 		case 4:
 			return dice.isAttackDice() ? dk.stacktrace.risk.R.drawable.a4 : dk.stacktrace.risk.R.drawable.d4;
 		case 5:
 			return dice.isAttackDice() ? dk.stacktrace.risk.R.drawable.a5 : dk.stacktrace.risk.R.drawable.d5;
 		case 6:
 			return dice.isAttackDice() ? dk.stacktrace.risk.R.drawable.a6 : dk.stacktrace.risk.R.drawable.d6;
 
 		default:
 			return 0;
 		}
 	}
 }
