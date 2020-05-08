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
 public class ObservableBonus {
     private ReadOnlyIntegerWrapper id;
     private ReadOnlyStringWrapper name;
     private ReadOnlyIntegerWrapper level;
     private ReadOnlyIntegerWrapper strength;
     private ReadOnlyIntegerWrapper speed;
     private ReadOnlyObjectWrapper<Image> image;
     private ReadOnlyBooleanWrapper isLoaded;
     
     public ObservableBonus(){
         this.isLoaded = new ReadOnlyBooleanWrapper();
         this.isLoaded.set(false);
         this.id = new ReadOnlyIntegerWrapper(0);
         this.name = new ReadOnlyStringWrapper(null);
         this.level = new ReadOnlyIntegerWrapper(0);
         this.strength = new ReadOnlyIntegerWrapper(0);
         this.speed = new ReadOnlyIntegerWrapper(0);
         this.image = new ReadOnlyObjectWrapper<>(null);
     }
     
     public void loadBonus(Bonus b){
         this.id.set(b.getId());
         this.name.set(b.getName());
         this.level.set(b.getLevel());
         this.strength.set(b.getStrength());
         this.speed.set(b.getSpeed());
         this.image.set(b.getDataImage().getImage());
     }
     public void unload(){
         this.isLoaded.set(false);
         this.id.set(0);
         this.name.set(null);
         this.level.set(0);
         this.strength.set(0);
         this.speed.set(0);
         this.image.set(null);
     }
     public ReadOnlyBooleanProperty isLoadedProperty(){
         return this.isLoaded.getReadOnlyProperty();
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
     public ReadOnlyIntegerProperty getStrengthProperty() {
         return this.strength.getReadOnlyProperty();
     }
     public ReadOnlyIntegerProperty getSpeedProperty() {
         return this.speed.getReadOnlyProperty();
     }
     public ReadOnlyObjectProperty<Image> getImageProperty() {
         return this.image.getReadOnlyProperty();
     }
 }
