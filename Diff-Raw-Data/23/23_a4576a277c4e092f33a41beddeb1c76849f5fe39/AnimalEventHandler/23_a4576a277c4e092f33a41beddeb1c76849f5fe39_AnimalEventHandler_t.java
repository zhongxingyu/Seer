 package mods.natureoverhaul;
 
import java.util.Iterator;

import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAITaskEntry;
 import net.minecraft.entity.passive.EntityAnimal;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
 /**
  * Event for wild animals breeding, from Clinton Alexander idea.
  * @author Olivier
  *
  */
 public class AnimalEventHandler {
 	 private int breedRate;
 	 private boolean breed;
	public AnimalEventHandler(boolean wildAnimalsBreed, int wildAnimalBreedRate) {
 		this.breedRate=wildAnimalBreedRate;
 		this.breed=wildAnimalsBreed;
 	}
 
 	@ForgeSubscribe
     public void onLivingUpdateEvent(LivingUpdateEvent event){
     	if(breed && event.entityLiving instanceof EntityAnimal)
     	{
     		EntityAnimal ent =(EntityAnimal)event.entityLiving;
     		if(!ent.worldObj.isRemote && !ent.isChild() && !ent.isInLove()/*&& ent.breeding == 0*/) {
    			/*Iterator iterator = ent.tasks.taskEntries.iterator();
    			EntityAITaskEntry entityaitaskentry;
    			EntityAIMate matingAI;
    			while (iterator.hasNext())
                {
                    entityaitaskentry = (EntityAITaskEntry)iterator.next();
                    if(entityaitaskentry.action instanceof EntityAIMate){
                    	matingAI=(EntityAIMate) entityaitaskentry.action;
                    	break;
                    }TODO: continue this
                }*/
                    
     			if(ent.worldObj.rand.nextInt(breedRate) == 0) {
     				ent.inLove = 600;
     			}
     		}
     	}
     }
 
	public void set(boolean wildAnimalsBreed, int wildAnimalBreedRate) {
 		this.breedRate=wildAnimalBreedRate;
 		this.breed=wildAnimalsBreed;
 	}
 }
