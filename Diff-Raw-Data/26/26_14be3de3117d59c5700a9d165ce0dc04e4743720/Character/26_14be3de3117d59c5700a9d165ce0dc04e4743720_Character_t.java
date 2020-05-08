 package Juggernaut;
 
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.PhysicsCollisionEvent;
 import com.jme3.bullet.collision.PhysicsCollisionListener;
 import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
 import com.jme3.bullet.control.CharacterControl;
 import com.jme3.input.KeyInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Spatial;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 
 public class Character  implements ActionListener, PhysicsCollisionListener{
     Main game;
     BulletAppState bulletAppState;
     // HUD elements
     public static float currentHealth = 100;
     private static float currentArmor = 25;
     private static float currentEnergy = 100;
 
     public static float maxHealth = 100;
     private static float maxArmor = 25;
     private static float maxEnergy = 100;
     private static float Pickup = 20;
     private static float currentLevel = 1;
     private static float currentExperience = 0;
     private static float maxExperience = 100;
     public static float currentScore = 0;
     public static float abiltyPoints =0;
     public static float attributePoints =0; 
 
     private boolean damageTaken = false;
     private static float incomingDamage = 0;
     private static float armorDelay = 3.0f;
     private static float rechargeDelay = 3.0f;
     private static float armorRechargeRate = 5.0f;
     public int enemiesDead = 0;
     
     private static float hoverEnergyCost = 60f;
     
     private boolean sprintActive;
     public static float sprintEnergyCost = 35f;
     public static float sprintSpeed =0;
     
     private boolean dashActive;
     public static float dashEnergyCost;
     public static float dashDistance;
     public static float dashDamage;
 
     //Skill Levels
     public static int sprintSkillLevel = 1;
     public static int dashSkillLevel = 1;
     public static int superJumpSkillLevel = 1;
     public static int hoverSkillLevel = 1;
     
     
     // Movement
     public static float movementSpeed = 0;
     public static float jumpHeight = 50;
     public static float superJumpHeight = 100;
 
     // Booleans
     private static boolean pickUpAdded = false;
     private static boolean isShooting = false;
     private static boolean shieldActive = false;
     private static boolean superJumpActive = false;
     private static boolean hoverActive = false;
     private static boolean isDead = false;
     
     private Weapon currentWeapon;
     private static Weapon weaponSlot1;
     private static Weapon weaponSlot2;
     private static Weapon weaponSlot3;
     public static float damageModifier = 1.0f;
     private float fireDelay =0;
     private Vector<Bullet> bullets= new Vector<Bullet>();
     
     Spatial ninja;
     private CharacterControl player;
 
     Spatial playerDebug;
     
     private Vector3f walkDirection = new Vector3f();
     private static boolean left = false;
     private static boolean right = false;
     
     private static boolean isFiring = false;
     
     Character(){
         
     }
 
 
     Character(Main gameRef, BulletAppState bulletAppStateRef){
         
         this.game = gameRef;
         this.bulletAppState = bulletAppStateRef;
         bulletAppState.getPhysicsSpace().addCollisionListener(this);
         //Load Ninja as filler for character model
         ninja = game.getAssetManager().loadModel("Models/Ninja/Ninja.mesh.xml");
         ninja.setName("Player");
         ninja.scale(0.02f, 0.02f, 0.02f);
 //        ninja.setLocalTranslation(new Vector3f(350, 316, 0));
         game.getRootNode().attachChild(ninja);
         CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1f, 2f);
         player = new CharacterControl(capsuleShape, .05f);
         player.setJumpSpeed(50);
         player.setFallSpeed(50);
         player.setGravity(120);        
         player.setViewDirection(new Vector3f(-1.0f, 0, 0));
         player.setCollideWithGroups(2);
         
         playerDebug = player.createDebugShape(game.getAssetManager());
         ninja.addControl(player);
         game.getRootNode().attachChild(playerDebug);
 
         bulletAppState.getPhysicsSpace().add(player);
       
         setUpKeys();
         
         weaponSlot1 = new Pistol(game);
         weaponSlot2 = new miniGun(game);
         weaponSlot3 = new laserRifle(game);
         currentWeapon = weaponSlot1;
         
         movementSpeed = 0.4f;
         
     }
     
     void Update(float dt){
         // Movement
         walkDirection.set( 0, 0, 0);
        
         if(right) { 
             walkDirection.addLocal(Vector3f.UNIT_X.clone().multLocal(movementSpeed));
             player.setViewDirection(walkDirection.negate());
        } else if(left) { 
            walkDirection.addLocal(Vector3f.UNIT_X.negate().multLocal(movementSpeed));
            player.setViewDirection(walkDirection.negate());
        } 
         
         player.setWalkDirection(walkDirection);
         player.setPhysicsLocation(new Vector3f(player.getPhysicsLocation().x, player.getPhysicsLocation().y, 0));
 
 
         playerDebug.setLocalTranslation(player.getPhysicsLocation());
         
         
         fireDelay += dt;
         
         if(isFiring){
             if(fireDelay > currentWeapon.getFireRate()){
                 fireWeapon();
                 fireDelay = 0;
             }            
         }
         
         
         for(int bulletItr = 0; bulletItr < bullets.size(); bulletItr++){
             Bullet testBullet = bullets.get(bulletItr);
             if(testBullet.LifeTime(dt) ){
                 testBullet.delete();
                 bullets.remove(bulletItr);
             }
 
         }
          
         
         if(hoverActive && currentEnergy > 0){
             sprintActive = false;
             hover(dt);
         } else {
             player.setFallSpeed(50);
         }
         
         if(sprintActive && currentEnergy > 0){
             sprint(dt);
         } else {
             movementSpeed = 0.4f;
         }
         
         if(currentEnergy < maxEnergy){
             if(!sprintActive && !hoverActive)
                 currentEnergy += 10f * dt;
         }
         
         if(damageTaken){
             game.damageTakenSound.play();
 
             if(currentArmor > 0){
                 if(incomingDamage > currentArmor){
                     incomingDamage -= currentArmor;
                     currentArmor = 0;
                     currentHealth -= incomingDamage;
                 }else{
                     currentArmor -= incomingDamage;
                 }
             }else{
                 currentHealth -= incomingDamage;
             }
             
             damageTaken = false;
             rechargeDelay = 0;
             
         } else {
             rechargeDelay += dt;
             
         }
         
         if(rechargeDelay > armorDelay){
            if(currentArmor <= 5) {
                game.shieldAddedSound.play();
            }
            
             currentArmor += 5 * dt;
             if(currentArmor >= maxArmor){
                 currentArmor = maxArmor;
                 rechargeDelay =0;
                 game.shieldAddedSound.stop();
             }
         }
         
         
         
         //enemiesDead == 66){
 //        enemiesDead = (int)currentExperience;
         if(game.boss.isDead()){
             Win();
         } else if(currentHealth <= 0){
             game.playerDeathSound.play();
             Lose();
         }
         
 //        System.out.print(currentHealth +"\n");
         game.getHud().bind(game.getNifty(), game.getHud().screen);
         game.getHud().updateHUD(healthPercentage(), armorPercentage(), energyPercentage(), currentWeapon.getCurrentAmmo(), expPercentage(), getScore() );
     }
 
     private void setUpKeys() {
         game.getInputManager().addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT),new KeyTrigger(KeyInput.KEY_A));
         game.getInputManager().addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT), new KeyTrigger(KeyInput.KEY_D));
         game.getInputManager().addMapping("Jump", new KeyTrigger(KeyInput.KEY_UP), new KeyTrigger(KeyInput.KEY_W));
         game.getInputManager().addMapping("Fire", new KeyTrigger(KeyInput.KEY_SPACE));
         game.getInputManager().addMapping("Weapon1", new KeyTrigger(KeyInput.KEY_1), new KeyTrigger(KeyInput.KEY_NUMPAD1));
         game.getInputManager().addMapping("Weapon2", new KeyTrigger(KeyInput.KEY_2), new KeyTrigger(KeyInput.KEY_NUMPAD2));
         game.getInputManager().addMapping("Weapon3", new KeyTrigger(KeyInput.KEY_3), new KeyTrigger(KeyInput.KEY_NUMPAD3));
         game.getInputManager().addMapping("SprintR", new KeyTrigger(KeyInput.KEY_RCONTROL));
         game.getInputManager().addMapping("SprintL", new KeyTrigger(KeyInput.KEY_LCONTROL));
         game.getInputManager().addMapping("HoverR", new KeyTrigger(KeyInput.KEY_RSHIFT));
         game.getInputManager().addMapping("HoverL", new KeyTrigger(KeyInput.KEY_LSHIFT));
         game.getInputManager().addListener(this, "Left");
         game.getInputManager().addListener(this, "Right");
         game.getInputManager().addListener(this, "Jump");
         game.getInputManager().addListener(this, "Fire");
         game.getInputManager().addListener(this, "Weapon1");
         game.getInputManager().addListener(this, "Weapon2");
         game.getInputManager().addListener(this, "Weapon3");
         game.getInputManager().addListener(this, "SprintR");
         game.getInputManager().addListener(this, "SprintL");
         game.getInputManager().addListener(this, "HoverR");
         game.getInputManager().addListener(this, "HoverL");
     }
     
     public void onAction(String binding, boolean value, float tpf){
         game.getHud().bind(game.getNifty(), game.getHud().screen);
         if(binding.equals("Left")){
             left = value;
         } else if(binding.equals("Right")){
             right = value;
         } else if(binding.equals("Jump")){
             player.jump();
         } else if(binding.equals("Fire")){
             isFiring = value; 
         } else if(binding.equals("SprintR") || binding.equals("SprintL")){
             sprintActive = value;
         } else if(binding.equals("HoverR") || binding.equals("HoverL")){
             hoverActive = value;
         }else if(bullets.isEmpty()){
             if(binding.equals("Weapon1")){
                 switchWeapon(1);
                 game.getHud().weapon1Clicked();
             }else if(binding.equals("Weapon2")){
                 switchWeapon(2);
                 game.getHud().weapon2Clicked();
             }else if(binding.equals("Weapon3")){
                 switchWeapon(3);
                 game.getHud().weapon3Clicked();
             }
         }
         
          
     }
     
     CharacterControl getControl(){
         return player;
     }
     
     public float updateHealth() {
 
         if (incomingDamage > 0 && currentArmor > 0) {
             currentHealth = updateHealth();
         } else if  (incomingDamage > 0 && currentArmor == 0) {
             currentHealth = currentHealth - incomingDamage;
         }
        
 
         return currentHealth;
 
     }
     
     public float updateArmor() {
         
         if(incomingDamage > 0 && currentArmor > 0) {
             currentArmor = currentArmor - incomingDamage;
         } 
         
         return currentArmor;
     }
 
     public float healthPercentage() {
         
         return (currentHealth / maxHealth) * 100 ;
         
 
     }
 
     public float armorPercentage() {
         
         return (currentArmor / maxArmor) * 100;
 
     }
 
     public float energyPercentage() {
   
         return (currentEnergy / maxEnergy) * 100;
 
     }
     
     public float expPercentage() {
   
         return (currentExperience / maxExperience) * 100;
 
     }
     
     public void fireWeapon(){
         if(currentWeapon == weaponSlot1 || currentWeapon.getCurrentAmmo() > 0){
             bullets.add(currentWeapon.Fire(damageModifier, player.getPhysicsLocation(), player.getViewDirection().normalize().negate(), game, bulletAppState));
        
            if(currentWeapon == weaponSlot1) {
                game.pistolSound.play();
            } else if (currentWeapon == weaponSlot2) {
                game.minigunSound.play();
            } else if (currentWeapon == weaponSlot3) {
                game.laserRifleSound.play();
            }
         }
     }
     public void upgradeDamageModifier(){
         damageModifier += .1f;
         attributePoints -= 1;
     }
     
     public void upgradeHealth(){
         maxHealth += 25;
         currentHealth = maxHealth;
         attributePoints -= 1;
     }
     
     public void upgradeArmor(){
         maxArmor += 25;
         armorDelay -= 0.2f;
         armorRechargeRate += 3.0f;
         currentArmor = maxArmor;
         attributePoints -=1;
     }
     
     public void upgradeHover(){
         hoverEnergyCost -= 3.0f;
         abiltyPoints -= 1;
     }
     
     public void upgradeSprint(){
         sprintSpeed += 0.1f;
         sprintEnergyCost -= 2.0f;
         abiltyPoints -= 1;        
     }
     
     public void checkXP(){
         if(currentExperience > maxExperience){
             LevelUp();
             currentExperience = maxExperience - currentExperience;
             maxExperience = currentLevel * 10;
         }
     }
 
     private void LevelUp() {
         currentLevel += 1;
         attributePoints += 2;
         if(currentLevel % 2 != 0){
             abiltyPoints += 1;
         }
         currentHealth = maxHealth;
         currentArmor = maxArmor;
         currentEnergy = maxEnergy;
         
     }
     
     private void switchWeapon(int weaponSlot){
         if(bullets.isEmpty()){
             if(weaponSlot == 1){
                 currentWeapon = weaponSlot1;
             } else if(weaponSlot == 2){
                 currentWeapon = weaponSlot2;
             } else if(weaponSlot == 3){
                 currentWeapon = weaponSlot3;
             }
         }
     }
     
     private void sprint(float dt){
         movementSpeed = 0.7f;
         currentEnergy -= sprintEnergyCost * dt;
     }
     
     private void hover(float dt){
         player.setFallSpeed(6);
         currentEnergy -= hoverEnergyCost * dt;
     }
     
     public Vector3f getPosition(){
         return player.getPhysicsLocation();
     }
     
     public Weapon EquippedWeapon(){
         return currentWeapon;
     }
     public Weapon WeaponSlot2()
     {
         
         return weaponSlot2;
     }
     public Weapon WeaponSlot3()
     {
         return weaponSlot3;
     }
     
     
     public float DamageOutput(){
         return currentWeapon.getDamage() * damageModifier;
     }
     public void healthPickup(float pickup){
         currentHealth += pickup;
         if(currentHealth > maxHealth){
             currentHealth = maxHealth;
         }
     }
     
     public void buyHealth(){
         
         
         currentHealth += 25;
         
         if(currentHealth > maxHealth)
         {
             currentHealth = maxHealth;
         }
         
         currentScore  -= 10;
         
     }
     
     public void buyMiniGunAmmo(){
         weaponSlot2.setCurrentAmmo(weaponSlot2.getCurrentAmmo() + 100);
         if(weaponSlot2.currentAmmo > weaponSlot2.maxAmmo)
         {
             weaponSlot2.currentAmmo = weaponSlot2.maxAmmo;
         
         }
         currentScore -= 30;
     }
     
     public void buyLaserRifleAmmo(){
         weaponSlot3.setCurrentAmmo(weaponSlot3.getCurrentAmmo() + 10);
          if(weaponSlot3.currentAmmo > weaponSlot3.maxAmmo)
         {
             weaponSlot3.currentAmmo = weaponSlot3.maxAmmo;
         
         }
         currentScore -= 50;
     }
     
     public float getPickup(){
         return Pickup;
     }
     
     public void takeDamage(float damage) {
         damageTaken = true;
         incomingDamage = damage;
     }
     
     public float getScore() {
         return currentScore;
     }
     
     
     public void addScore(float score) {
         currentScore += score;
     }
     
     public void addExperience(float exp) {
         currentExperience += exp;
         //System.out.print("BOOM  " + currentExperience + "\n");
         
         
         
     }
    
     public void collision(PhysicsCollisionEvent event) {
         if(event.getNodeA().getName().equals("Player") && event.getNodeB().getName().equals("Enemy")){
             takeDamage(25);
         }else if(event.getNodeA().getName().equals("Player") && event.getNodeB().getName().equals("Bullet"))
         {
             takeDamage(game.boss.getDamage());
         }
         
     }
 
     private void Win() {
         //Display Win Screen
         game.getHud().goToScreen("WinScreen");
     }
 
     private void Lose() {
         // If you lose, you are dead.
         isDead = true;
         
         //Display Lose Screen
         game.getHud().goToScreen("DeathScreen");
         
         // Remove the player from the game
         game.getRootNode().detachChild(playerDebug);
         game.getRootNode().detachChild(ninja);
         bulletAppState.getPhysicsSpace().remove(player);
     }
     
     public boolean isDead() {
         return isDead;
     }
 }
