 import greenfoot.*;
 
 /**
  * Auto's botsen op elkaar bij straten waar teveel water is.
  * 
  * Project 42
  */
 public class FloodStreet extends Calamities
 {    
     ShutOff police_shutoff;
     public void addedToWorld(World world)
     {
         setImage("flood.gif");
         setDifficultyScore();
        Greenfoot.playSound("flood.WAV");
     }
     
     public void act()
     {
         super.act();
         checkClicked(); 
         checkIfIExpire(checkDifficulty());
         
         if (police_shutoff != null) {
             interventionTimer++;
             if (interventionTimer > 200) 
             {
                 ControlroomWorld world = (ControlroomWorld)getWorld();
                 int NumberOfSaved = (getExpireTimer()/10);
                 int NumberOfDeaths = ((checkDifficulty() - getExpireTimer())/40);
                 if (NumberOfDeaths > 0 && NumberOfSaved > 0) {
                     world.getNumberOfDeathsCounter().add(NumberOfDeaths);
                     world.getNumberOfSavedCounter().add(NumberOfSaved);
                 }
                 
                 world.removeObject(this);
                 world.removeObject(police_shutoff);
                 world.getScoreCounter().add(50);
                 world.addConsoleMessage("De straat is afgezet.");
                 world.getPoliceUnits().add(-1);
             }
         } else {
             interventionTimer = 0;
         }
 
     }
     
     /** Check if object has been clicked
      * If true, it checks whether the last object clicked was a Firefighter
      * If true, it deletes the Fire object and adds 50 to score
      */
     
     public void checkClicked() {
         if (Greenfoot.mouseClicked(this)) 
         {
             ControlroomWorld world = (ControlroomWorld)getWorld();
             if (world.getSelectedCharacter() == ControlroomWorld.Character.POLICE_SHUTOFF) {
                 int objectLocationX = getX()+2;
                 int objectLocationY = getY();
                 world.addObject(police_shutoff = new ShutOff(), objectLocationX, objectLocationY);
             }
         }
     }
     
     /** Check whether object has been in the world for too long
      * If true, removes the FloodStreet object and sets the timer back to 0
      * Difficulty argument decreases when progressing in the game, making objects expire faster
      */
     
     public void checkIfIExpire(int difficulty) {
        ControlroomWorld world = (ControlroomWorld)getWorld();
 
        if (getExpireTimer() > difficulty && police_shutoff == null)
        {
            int NumberOfDeaths = (getExpireTimer()/10); 
            if (NumberOfDeaths > 0) {
                world.getNumberOfDeathsCounter().add(NumberOfDeaths);
             }
            world.removeObject(this);
            world.loseLife();
            setExpireTimer(0);
        }
     }
 }
