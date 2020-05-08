 package mods.natureoverhaul;
 
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
	public AnimalEventHandler(Boolean wildAnimalsBreed, int wildAnimalBreedRate) {
 		this.breedRate=wildAnimalBreedRate;
 		this.breed=wildAnimalsBreed;
 	}
 
 	@ForgeSubscribe
     public void onLivingUpdateEvent(LivingUpdateEvent event){
     	if(breed && event.entityLiving instanceof EntityAnimal)
     	{
     		EntityAnimal ent =(EntityAnimal)event.entityLiving;
     		if(!ent.worldObj.isRemote && !ent.isChild() && !ent.isInLove()/*&& ent.breeding == 0*/) {
     			if(ent.worldObj.rand.nextInt(breedRate) == 0) {
     				ent.inLove = 600;
     			}
     		}
     	}
     }
 
	public void set(Boolean wildAnimalsBreed, int wildAnimalBreedRate) {
 		this.breedRate=wildAnimalBreedRate;
 		this.breed=wildAnimalsBreed;
 	}
 }
