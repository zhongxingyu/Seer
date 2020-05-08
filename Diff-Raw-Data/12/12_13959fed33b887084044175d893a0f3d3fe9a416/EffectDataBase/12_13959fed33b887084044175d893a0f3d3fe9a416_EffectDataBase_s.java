 
 package edu.ycp.cs320.fokemon_webApp.shared.MoveClasses;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import edu.ycp.cs320.fokemon_webApp.shared.PokemonClasses.Pokemon;
 import edu.ycp.cs320.fokemon_webApp.shared.PokemonClasses.Status;
 
 
 
 
 
 public class EffectDataBase {
 	private static Random rand=new Random();
 
 	
 	public static ArrayList<String> moveEffect(Pokemon Attacker, Pokemon Defender, ArrayList <Effect> effect){
 		int randomNum=0;
 		ArrayList<String> battleMessage=new ArrayList<String>();
 		for (int i=0; i<effect.size(); i++){
 			randomNum = rand.nextInt(100);
 			effect.get(i).setEffectText("");
 			if(effect.get(i).getEffectChance()>=randomNum){
 			
 				switch (effect.get(i).getEffectIndex()) {
 		        case NONE: //no effect
 		                 break;
 		        case PARALYZE:  //Paralyzed
 		        	if(Defender.getStats().getStatus()==Status.NRM){
 			        Defender.getStats().setStatus(Status.PRL);
 		        	battleMessage.add(Defender.getInfo().getNickname()+" was paralyzed.  ");
 		        	}
 		                 break;
 		        case POISON:  //Poisoned
 		        	if(Defender.getStats().getStatus()==Status.NRM){
 			        Defender.getStats().setStatus(Status.PSN);
 		        	battleMessage.add(Defender.getInfo().getNickname()+" was poisoned.  ");
 		        	}
 		                 break;
 		        case SLEEP:  //Sleep
 		        	if(Defender.getStats().getStatus()==Status.NRM){
 			        Defender.getStats().setStatus(Status.SLP);
 		        	//Defender.getTempBattleStats().setSLPCount(effect.get(i).getMagnitude());
		        	Defender.getTempBattleStats().setSLPCount(rand.nextInt(2)+2);
 		        	battleMessage.add(Defender.getInfo().getNickname()+" fell asleep.  ");
 		        	}
 		                 break;	
 		        case BURN:  //Burn
 		        	if(Defender.getStats().getStatus()==Status.NRM){
 			        Defender.getStats().setStatus(Status.BRN);
 		        	battleMessage.add(Defender.getInfo().getNickname()+" was burned.  ");
 		        	}
 		                 break;
 		        case FREEZE:  //Freeze
 		        	if(Defender.getStats().getStatus()==Status.NRM){
 			        Defender.getStats().setStatus(Status.FRZ);
 		        	battleMessage.add(Defender.getInfo().getNickname()+" was frozen solid.  ");
 		        	}
 		                 break;
 		        case KO:  //Death
 			        Defender.getStats().setCurHp(0);
 			        battleMessage.add(Defender.getInfo().getNickname()+" was One-Hit KOed.  ");
 		                 break;
 		        case RECOVERCONST:  //RecoverConstant HP
 			        Attacker.getStats().setCurHp(Attacker.getStats().getCurHp()+effect.get(i).getMagnitude());
 			        battleMessage.add(Attacker.getInfo().getNickname()+" has recovered HP.  ");
 		                 break;  
 		        default: //no effect
 		                 break;
 				}
 			
 			}	
 			
 		}	
 		return battleMessage;
 
 	}
 	}
 
 
 
 
