 import com.sun.jna.Pointer;
 
 ///
 class FishSpecies
 {
   Pointer ptr;
   int ID;
   int iteration;
   public FishSpecies(Pointer p)
   {
     ptr = p;
     ID = Client.INSTANCE.fishSpeciesGetId(ptr);
     iteration = BaseAI.iteration;
   }
   boolean validify()
   {
     if(iteration == BaseAI.iteration) return true;
     for(int i = 0; i < BaseAI.fishSpeciess.length; i++)
     {
       if(BaseAI.fishSpeciess[i].ID == ID)
       {
         ptr = BaseAI.fishSpeciess[i].ptr;
         iteration = BaseAI.iteration;
         return true;
       }
     }
     throw new ExistentialError();
   }
 
     //commands
 
   ///Have a new fish spawn and join the fight!
   boolean spawn(int x, int y)
   {
     validify();
     return (Client.INSTANCE.fishSpeciesSpawn(ptr, x, y) == 0) ? false : true;
   }
 
     //getters
 
   ///Unique Identifier
   public int getId()
   {
     validify();
     return Client.INSTANCE.fishSpeciesGetId(ptr);
   }
   ///The fish species
   public String getSpecies()
   {
     validify();
     return Client.INSTANCE.fishSpeciesGetSpecies(ptr);
   }
   ///The amount of food it takes to raise this fish
   public int getCost()
   {
     validify();
     return Client.INSTANCE.fishSpeciesGetCost(ptr);
   }
   ///The maximum health of this fish
   public int getMaxHealth()
   {
     validify();
     return Client.INSTANCE.fishSpeciesGetMaxHealth(ptr);
   }
   ///The maximum number of movements in a turn
   public int getMaxMovement()
   {
     validify();
     return Client.INSTANCE.fishSpeciesGetMaxMovement(ptr);
   }
   ///The total weight the fish can carry
   public int getCarryCap()
   {
     validify();
     return Client.INSTANCE.fishSpeciesGetCarryCap(ptr);
   }
   ///The power of the fish's attack
   public int getAttackPower()
   {
     validify();
     return Client.INSTANCE.fishSpeciesGetAttackPower(ptr);
   }
   ///The attack arrange of the fish
   public int getRange()
   {
     validify();
     return Client.INSTANCE.fishSpeciesGetRange(ptr);
   }
   ///Maximum number of times this unit can attack per turn
   public int getMaxAttacks()
   {
     validify();
     return Client.INSTANCE.fishSpeciesGetMaxAttacks(ptr);
   }
   ///How many turns until you can spawn this fish species
   public int getTurnsTillAvailalbe()
   {
     validify();
     return Client.INSTANCE.fishSpeciesGetTurnsTillAvailalbe(ptr);
   }
   ///How many turns until you can no longer spawn this fish species
   public int getTurnsTillUnavailable()
   {
     validify();
     return Client.INSTANCE.fishSpeciesGetTurnsTillUnavailable(ptr);
   }
 
 }
