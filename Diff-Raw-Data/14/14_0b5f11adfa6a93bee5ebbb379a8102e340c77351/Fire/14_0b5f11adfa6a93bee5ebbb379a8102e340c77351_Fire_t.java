 import greenfoot.*; 
 
 /**
  * Door kortsluiting ontstaat brand
  * 
  * Project 42
  */
 
 public class Fire extends Calamities
 {
 
     Extinguish extinguisher;
     public void addedToWorld(World world)
     {
         setImage("fire.gif");
         setDifficultyScore();
     }
     
     public void act()
     {
         // Toont sprite, na een aantal seconden verdwijnt de class (en gaat er een leven weg)
         super.act();
         checkClicked();
         checkIfIExpire(checkDifficulty());
         
         if (extinguisher != null) {
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
                 world.removeObject(extinguisher);
                 world.getScoreCounter().add(50);
                 world.addConsoleMessage("De brand is geblust.");
                 world.getFirefighterUnits().add(-1);
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
             if (world.getSelectedCharacter() == ControlroomWorld.Character.FIREFIGHTER)
             {
                 int objectLocationX = getX()+2;
                 int objectLocationY = getY();
                 world.addObject(extinguisher = new Extinguish(), objectLocationX, objectLocationY);
             }
         }
         
     }
     
     /** Check whether object has been in the world for too long
      * If true, removes the Fire object and sets the timer back to 0
      * Difficulty argument decreases when progressing in the game, making objects expire faster
      */
     
     public void checkIfIExpire(int difficulty) {
        ControlroomWorld world = (ControlroomWorld)getWorld();
        if (getExpireTimer() > difficulty && extinguisher == null)
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
  
