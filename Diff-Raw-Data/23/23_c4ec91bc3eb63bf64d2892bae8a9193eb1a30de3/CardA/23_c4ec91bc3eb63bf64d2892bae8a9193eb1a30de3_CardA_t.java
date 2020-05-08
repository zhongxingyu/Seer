 package dominion.android;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.TextView;
 import dominion.Card;
 import dominion.CardTypes;
 import dominion.Game;
 import dominion.Kingdom;
 
 
 public class CardA extends Activity {
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
 		setContentView(R.layout.card);
 		
		Bundle payload = this.getIntent().getExtras();
		Integer indexI = (Integer) payload.get("index");
		int index = indexI.intValue();
		
 		Kingdom k = (Kingdom) Game.instance().kingdom().get(index);
 		Card card = k.card();
 		
 		TextView name = (TextView) findViewById(R.id.cardTitle);
 		name.setText(card.name());
 		
 		TextView typesTV = (TextView) findViewById(R.id.cardTypes);
 		ArrayList<String> types = new ArrayList<String>();
 		int t = card.types();
 		if((t & CardTypes.ACTION) > 0) types.add("Action");
 		if((t & CardTypes.ATTACK) > 0) types.add("Attack");
 		if((t & CardTypes.CURSE) > 0) types.add("Curse");
 		if((t & CardTypes.REACTION) > 0) types.add("Reaction");
 		if((t & CardTypes.TREASURE) > 0) types.add("Treasure");
 		if((t & CardTypes.VICTORY) > 0) types.add("Victory");
 		StringBuffer sb = new StringBuffer();
 		for(int i = 0; i < types.size(); i++) {
 			sb.append(types.get(i));
 			if(i+1 < types.size())
 				sb.append(", ");
 		}
 		typesTV.setText(sb.toString());
 		
 		TextView cost = (TextView) findViewById(R.id.cardCost);
 		cost.setText("Cost: " + card.cost() + " Coin" + (card.cost() == 1 ? "" : "s"));
 		
 		TextView remaining = (TextView) findViewById(R.id.cardCount);
 		remaining.setText("Remaining: " + k.count());
 		
 		TextView rules = (TextView) findViewById(R.id.cardDescription);
 		rules.setText(card.text());
 	}
 }
