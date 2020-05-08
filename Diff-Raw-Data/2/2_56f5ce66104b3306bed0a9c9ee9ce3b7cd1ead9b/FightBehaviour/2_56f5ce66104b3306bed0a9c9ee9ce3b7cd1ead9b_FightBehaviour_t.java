 package pl.edu.agh.megamud.mechanix;
 
 import java.util.List;
 
 import com.sun.org.apache.xml.internal.security.Init;
 
 import pl.edu.agh.megamud.base.Behaviour;
 import pl.edu.agh.megamud.base.BehaviourHolderInterface;
 import pl.edu.agh.megamud.base.Controller;
 import pl.edu.agh.megamud.base.Creature;
 
 public class FightBehaviour extends Behaviour {
 	private boolean active;
 	private Creature opponent;
 	
 	public FightBehaviour(BehaviourHolderInterface o) {
 		super(o,0L);
 	}
 		
 	public FightBehaviour(BehaviourHolderInterface o, long delay) {
 		super(o, delay);
 		active = false;
 	}
 
 	@Override
 	protected void action() {
 		if(((Creature)owner).getHp()<=0 || !((Creature)owner).getLocation().getCreatures().containsKey(opponent.getName()))
 			return ;
 		FightBehaviour oppFightBeh = getOpponentFightBehaviour();
 		if(oppFightBeh!= null && !oppFightBeh.isActive() && isOpponentAlive()){
 			oppFightBeh.setOpponent((Creature)owner);
 			oppFightBeh.init();
 		}
 		attack();
 		write();
 		if(isOpponentAlive()){
 			put();	
 		}else{
 			setActive(false);
 			setOpponent(null);
 		}
 		
 	}
 	@Override
 	public final Behaviour init(){
 		setDelay(200);
 		setActive(true);
 		return super.init();
 	}
 	
 	private void write(){
 		Controller c = ((Creature)owner).getController();
 		if(c!= null){
			c.write("Your enemy has "+opponent.getHp());
 		}
 		Controller cOpp = opponent.getController();
 		if(c!= null){
 			c.write("You got "+((Creature)owner).getHp()+" hp");
 		}
 	}
 	
 	private void attack(){
 		Mechanix.attack((Creature)owner, opponent);
 	}
 
 	private boolean isOpponentAlive(){
 		if(opponent.getHp()<=0)
 			return false;
 		return true;
 	}
 	
 	public boolean isActive() {
 		return active;
 	}
 
 	public void setActive(boolean active) {
 		this.active = active;
 	}
 
 	public Creature getOpponent() {
 		return opponent;
 	}
 
 	public void setOpponent(Creature opponent) {
 		this.opponent = opponent;
 	}
 	private FightBehaviour getOpponentFightBehaviour(){
 		List<Behaviour> list = opponent.getBehaviourByType(FightBehaviour.class);
 		if(list.isEmpty())
 			return null;
 		return (FightBehaviour)list.get(0);
 	}
 
 }
