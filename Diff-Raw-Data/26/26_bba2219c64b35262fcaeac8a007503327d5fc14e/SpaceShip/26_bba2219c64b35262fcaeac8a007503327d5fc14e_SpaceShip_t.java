 import java.util.ArrayList;
 
 public class SpaceShip {
     public static final short FLEA      = 0,
                               GNAT      = 1,
                               FIREFLY   = 2,
                               MOSQUITO  = 3,
                               BUMBLEBEE = 4;
 
     @SuppressWarnings("unused")
     private final short CARGOSIZE,
                         WEAPONSLOTS,
                         SHIELDSLOTS,
                         GADGETSLOTS,
                         CREWSIZE,
                         FUELSIZE,
                         TECHLEVEL,
                         FUELCOST,
                         BOUNTY,
                         OCCURENCE,
                         HULLSTRENGTH,
                         REPAIRCOST,
                         SIZE;
     
     @SuppressWarnings("unused")
     private final int PRICE;
     private final String NAME;
     private ArrayList<Player> crew;
     
     /**
      * This constructor generates a SpaceShip with all of the correct
      * stats, based on the type specified.
      * @param type The type of ship to generate:
      * SpaceShip.<TYPE>
      * 0 == flea
      * 1 == gnat
      * 2 == firefly
      * 3 == mosquito
      * 4 == bumblebee
      * default == gnat
      */
     public SpaceShip(int type) {
         switch(type) {
            case FLEA:
                 //flea
                 NAME = "Flea";
                 CARGOSIZE    = 10;
                 WEAPONSLOTS  = 0;
                 SHIELDSLOTS  = 0;
                 GADGETSLOTS  = 0;
                 CREWSIZE     = 1;
                 FUELSIZE     = 20;
                 TECHLEVEL    = 4;
                 FUELCOST     = 1;
                 BOUNTY       = 5;
                 OCCURENCE    = 2;
                 HULLSTRENGTH = 25;
                 REPAIRCOST   = 1;
                 SIZE         = 0;
                 PRICE        = 2000;
                 break;
            case GNAT:
                 //gnat
                 NAME = "Gnat";
                 CARGOSIZE    = 15;
                 WEAPONSLOTS  = 1;
                 SHIELDSLOTS  = 0;
                 GADGETSLOTS  = 1;
                 CREWSIZE     = 1;
                 FUELSIZE     = 14;
                 TECHLEVEL    = 5;
                 FUELCOST     = 2;
                 BOUNTY       = 50;
                 OCCURENCE    = 28;
                 HULLSTRENGTH = 100;
                 REPAIRCOST   = 1;
                 SIZE         = 1;
                 PRICE        = 10000;
                 break;
            case FIREFLY:
                 //firefly
                 NAME = "Firefly";
                 CARGOSIZE    = 20;
                 WEAPONSLOTS  = 1;
                 SHIELDSLOTS  = 1;
                 GADGETSLOTS  = 1;
                 CREWSIZE     = 1;
                 FUELSIZE     = 17;
                 TECHLEVEL    = 5;
                 FUELCOST     = 3;
                 BOUNTY       = 75;
                 OCCURENCE    = 20;
                 HULLSTRENGTH = 100;
                 REPAIRCOST   = 1;
                 SIZE         = 1;
                 PRICE        = 25000;
                 break;
            case MOSQUITO:
                 //mosquito
                 NAME = "Mosquito";
                 CARGOSIZE    = 15;
                 WEAPONSLOTS  = 2;
                 SHIELDSLOTS  = 1;
                 GADGETSLOTS  = 1;
                 CREWSIZE     = 1;
                 FUELSIZE     = 13;
                 TECHLEVEL    = 5;
                 FUELCOST     = 5;
                 BOUNTY       = 100;
                 OCCURENCE    = 20;
                 HULLSTRENGTH = 100;
                 REPAIRCOST   = 1;
                 SIZE         = 1;
                 PRICE        = 30000;
                 break;
            case BUMBLEBEE:
                 //bumblebee
                 NAME = "Bumblebee";
                 CARGOSIZE    = 25;
                 WEAPONSLOTS  = 1;
                 SHIELDSLOTS  = 1;
                 GADGETSLOTS  = 2;
                 CREWSIZE     = 2;
                 FUELSIZE     = 15;
                 TECHLEVEL    = 5;
                 FUELCOST     = 7;
                 BOUNTY       = 125;
                 OCCURENCE    = 15;
                 HULLSTRENGTH = 100;
                 REPAIRCOST   = 1;
                 SIZE         = 2;
                 PRICE        = 60000;
                 break;
             default:
                 //gnat
                 NAME = "Gnat";
                 CARGOSIZE    = 15;
                 WEAPONSLOTS  = 1;
                 SHIELDSLOTS  = 0;
                 GADGETSLOTS  = 1;
                 CREWSIZE     = 1;
                 FUELSIZE     = 14;
                 TECHLEVEL    = 5;
                 FUELCOST     = 2;
                 BOUNTY       = 50;
                 OCCURENCE    = 28;
                 HULLSTRENGTH = 100;
                 REPAIRCOST   = 1;
                 SIZE         = 1;
                 PRICE        = 10000;
                 break;
         }
     }
 
    /**
     * Add a crew member to the spaceship.
     * @param crewMem Crew member to add.
     * @return boolean: Was the addition successful?
     */
     public boolean addCrew(Player crewMem) {
         return crew.add(crewMem);
     }
 
    /**
     * Remove a crew member from the spaceship.
     * @param crewMem Crew member to remove.
     * @return boolean: Was the removal successful?
     */
     public boolean removeCrew(Player crewMem) {
         return crew.remove(crewMem);
     }
 
     public String toString() {
         return NAME;
     }
 }
