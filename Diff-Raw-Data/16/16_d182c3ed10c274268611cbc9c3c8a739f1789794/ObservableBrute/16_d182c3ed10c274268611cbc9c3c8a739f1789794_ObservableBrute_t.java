 package brutes.client.game;
 
 import javafx.beans.property.ReadOnlyBooleanProperty;
 import javafx.beans.property.ReadOnlyBooleanWrapper;
 import javafx.beans.property.ReadOnlyIntegerProperty;
 import javafx.beans.property.ReadOnlyIntegerWrapper;
 import javafx.beans.property.ReadOnlyObjectProperty;
 import javafx.beans.property.ReadOnlyObjectWrapper;
 import javafx.beans.property.ReadOnlyStringProperty;
 import javafx.beans.property.ReadOnlyStringWrapper;
 import javafx.scene.image.Image;
 
 /**
  *
  * @author Karl
  */
 public class ObservableBrute {
     private ReadOnlyIntegerWrapper id;
     private ReadOnlyStringWrapper name;
     private ReadOnlyIntegerWrapper level;
     private ReadOnlyIntegerWrapper life;
     private ReadOnlyIntegerWrapper strength;
     private ReadOnlyIntegerWrapper speed;
     private ReadOnlyIntegerWrapper bonusStrength;
     private ReadOnlyIntegerWrapper bonusSpeed;
    private ReadOnlyObjectWrapper<Image> image;
     private ObservableBonus[] bonuses;
     private ReadOnlyBooleanWrapper isLoaded;
     
     public ObservableBrute(){
         this.isLoaded = new ReadOnlyBooleanWrapper();
         this.isLoaded.set(false);
         this.id = new ReadOnlyIntegerWrapper(0);
         this.name = new ReadOnlyStringWrapper(null);
         this.level = new ReadOnlyIntegerWrapper(0);
         this.life = new ReadOnlyIntegerWrapper(0);
         this.strength = new ReadOnlyIntegerWrapper(0);
         this.speed = new ReadOnlyIntegerWrapper(0);
         this.bonusStrength = new ReadOnlyIntegerWrapper(0);
         this.bonusSpeed = new ReadOnlyIntegerWrapper(0);
         this.image = new ReadOnlyObjectWrapper<>(null);
         this.bonuses = new ObservableBonus[Brute.MAX_BONUSES];
         for(int i = 0; i < Brute.MAX_BONUSES; i++){
             this.bonuses[i] = new ObservableBonus();
         }
     }
     
     public void loadBrute(Brute c){
         this.isLoaded.set(true);
         this.id.set(c.getId());
         this.name.set(c.getName());
         this.level.set(c.getLevel());
         this.life.set(c.getLife());
         this.strength.set(c.getStrength());
         this.speed.set(c.getSpeed());
         this.bonusStrength.set(c.getBonusStrength());
         this.bonusSpeed.set(c.getBonusSpeed());
        this.image.set(c.getImage());
         Bonus[] bonus = c.getBonuses();
         for(int i = 0; i < Brute.MAX_BONUSES; i++){
             this.bonuses[i].loadBonus(bonus[i]);
         }
     }
     public void unload() {
         this.isLoaded.set(false);
         this.id.set(0);
         this.name.set(null);
         this.level.set(0);
         this.life.set(0);
         this.strength.set(0);
         this.speed.set(0);
         this.bonusStrength.set(0);
         this.bonusSpeed.set(0);
         this.image.set(null);
         for(int i = 0; i < Brute.MAX_BONUSES; i++){
             this.bonuses[i].loadBonus(Bonus.EMPTY_BONUS);
         }
     }
     public ReadOnlyBooleanProperty isLoadedProperty(){
         return this.isLoaded.getReadOnlyProperty();
     }
 
     public ObservableBonus getBonus(int id){
         return this.bonuses[id];
     }
     public ReadOnlyIntegerProperty getIdProperty() {
         return this.id.getReadOnlyProperty();
     }
     public ReadOnlyStringProperty getNameProperty() {
         return this.name.getReadOnlyProperty();
     }
     public ReadOnlyIntegerProperty getLevelProperty() {
         return this.level.getReadOnlyProperty();
     }
     public ReadOnlyIntegerProperty getLifeProperty() {
         return this.life.getReadOnlyProperty();
     }
     public ReadOnlyIntegerProperty getStrengthProperty() {
         return this.strength.getReadOnlyProperty();
     }
     public ReadOnlyIntegerProperty getSpeedProperty() {
         return this.speed.getReadOnlyProperty();
     }
     public ReadOnlyIntegerProperty getBonusStrengthProperty() {
         return this.bonusStrength.getReadOnlyProperty();
     }
     public ReadOnlyIntegerProperty getBonusSpeedProperty() {
         return this.bonusSpeed.getReadOnlyProperty();
     }
     public ReadOnlyObjectProperty<Image> getImageProperty(){
         return this.image.getReadOnlyProperty();
     }
 }
