 package agai.manager;
 
 import java.util.Iterator;
 
 import com.springrts.ai.oo.CurrentCommand;
 
 import agai.AGAI;
 import agai.task.THelp;
 import agai.unit.AGUnit;
 
 public class MHelper extends Manager{
 
 	public MHelper(AGAI ai) {
 		super(ai);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see agai.manager.Manager#check()
 	 */
 	@Override
 	public void check() {
 		
 		
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see agai.manager.Manager#assignTask(agai.unit.AGUnit)
 	 */
 	@Override
 	public boolean assignTask(AGUnit u){
 		if (!u.getUnit().getDef().isAbleToAssist())
 			return false;
 		if (u.getUnit().getDef().getBuildSpeed()<=0)
 			return false;
		if (u.getUnit().getDef().getBuildOptions().size()<=0)
			return false;
 		Iterator<AGUnit> it = ai.getUnits().getUnits().iterator();
 		while(it.hasNext()){
 			AGUnit unit =it.next();
 			if (unit.getUnit().getCurrentCommands().size()>0){
 				CurrentCommand command = unit.getUnit().getCurrentCommands().get(0);
 				System.out.println(unit.toString() + command.getCommandId() +" " +command.getId());
 				if (command.getId()==-173){ //FIXME, this is the internal command id, not the id from c-interface
 					u.setTask(new THelp(ai, this, unit));
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 }
