 /*
   Copyright (C) 2012 Petr Mrázek
 
   This software is provided 'as-is', without any express or implied
   warranty.  In no event will the authors be held liable for any damages
   arising from the use of this software.
 
   Permission is granted to anyone to use this software for any purpose,
   including commercial applications, and to alter it and redistribute it
   freely, subject to the following restrictions:
 
   1. The origin of this software must not be misrepresented; you must not
      claim that you wrote the original software. If you use this software
      in a product, an acknowledgment in the product documentation would be
      appreciated but is not required.
   2. Altered source versions must be plainly marked as such, and must not be
      misrepresented as being the original software.
   3. This notice may not be removed or altered from any source distribution.
 
   Petr Mrázek <peterix@gmail.com>
  */
 
 package peterix.friendsss;
 
 import java.lang.reflect.Field;
 
 import cpw.mods.fml.relauncher.ReflectionHelper;
 import net.minecraft.src.EntityLiving;
 import net.minecraft.src.EntityVillager;
 import net.minecraft.src.EntityZombie;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
 
 public class VillagerTargettedEvent
 {
 	Class VillagerClass;
 	Class ZombieClass;
 	Field attackTarget;
 	public VillagerTargettedEvent()
 	{
        attackTarget = ReflectionHelper.findField(EntityLiving.class, "attackTarget", "bM");
         attackTarget.setAccessible(true);
         VillagerClass = EntityVillager.class;
         ZombieClass = EntityZombie.class;
 	}
     @ForgeSubscribe
     public void onLivingEvent(LivingSetAttackTargetEvent event)
     {
     	if(event.target == null || event.entity == null)
     		return;
        if (event.target.getClass() == VillagerClass && ZombieClass.isAssignableFrom(event.entity.getClass()))
         {
         	EntityZombie zombocom = (EntityZombie) event.entity;
             try
             {
             	attackTarget.set(zombocom, null);
 			}
             catch (Exception e){}
         }
     }
 }
