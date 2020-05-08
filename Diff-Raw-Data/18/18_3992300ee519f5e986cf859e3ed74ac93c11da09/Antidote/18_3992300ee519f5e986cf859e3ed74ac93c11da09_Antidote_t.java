 package com.soc.game.objects;
 
 import com.artemis.utils.Bag;
 import com.soc.core.SoC;
 import com.soc.game.components.Debuff;
 import com.soc.game.states.alterations.Alteration;
 import com.soc.game.states.alterations.Burn;
 
 public class Antidote extends Item {
 	public Bag<Class<? extends Alteration>> debuffs;
 	String iconPath;
 	public Antidote(int num,String name, String iconPath, String tooltip, Class<? extends Alteration>... debuffs) {
 		super(name, iconPath, tooltip,num);
 		this.debuffs = new Bag<Class<? extends Alteration>>();
 		for(Class<? extends Alteration> clazz: debuffs){
 			this.debuffs.add(clazz);
 		}
		this.iconPath=iconPath;
 
 	}
 
 	@Override
 	public void use() {
 		if(SoC.game.debuffmapper.has(SoC.game.player)){
 			Debuff d = SoC.game.debuffmapper.get(SoC.game.player);
 			for(int i = 0; i < debuffs.size(); i++){
 				d.removeDebuff(debuffs.get(i), SoC.game.player);
 			}
 		}
 		SoC.game.playermapper.get(SoC.game.player).removeFromInventary(this);
 	}
 	
 	@Override
 	public Object clone() throws CloneNotSupportedException {
 		Antidote clone=new Antidote(this.num,this.name,this.iconPath,this.tooltip,Burn.class);
 		this.debuffs=clone.debuffs;
 		return clone;
 	}
 }
