 import processing.core.*; 
 import processing.xml.*; 
 
 import ddf.minim.*; 
 import ddf.minim.signals.*; 
 import ddf.minim.analysis.*; 
 import ddf.minim.effects.*; 
 
 import java.applet.*; 
 import java.awt.Dimension; 
 import java.awt.Frame; 
 import java.awt.event.MouseEvent; 
 import java.awt.event.KeyEvent; 
 import java.awt.event.FocusEvent; 
 import java.awt.Image; 
 import java.io.*; 
 import java.net.*; 
 import java.text.*; 
 import java.util.*; 
 import java.util.zip.*; 
 import java.util.regex.*; 
 
 public class Firefly_Ecosystem extends PApplet {
 
 /**
 Name: Maya Richman
 Title: Emerging Symphonies: Fireflies & Musical Landscapes
 Date: April 16, 2013
 
 **/
 //load audio libraries
 
 
 
 
 
 //Landscape
 Sky s;
 
 //Lantern
 Lantern lanternFull;
 Light lightSource;
 
 //keeps track of sky's attractor status
 boolean isAttractor = true;
 
 //Contains multiple friction spaces (make more flexible in formation
 FrictionStructure structure = new FrictionStructure();
 
 // users will decide how large the ecosystem is
 int numberMovers = 3 ; //default
 
 //holds all creatures who have explored no areas and have below 300 spirit
 ArrayList<Creature>withoutSpirit = new ArrayList<Creature>();
 
 //system that controls/organizes all creatures
 CreatureSystem system;
 
 //system that holds ocillators
 CreatureSystem ocillators;
 
 //holds audio sample
 ArrayList<AudioSample>sampleList = new ArrayList<AudioSample>();
 Minim minim;
 File path;
 String[] sounds;
 AudioSample wind, bootup, buzz;
 int sound;
 
 //color of the background
 float ground;
 
 public void setup() {
   size(900, 700);
   smooth();
 
   //set background color
   ground = 0;
 
   //sound setup
   minim = new Minim(this);
   path = new File("/Users/mayarichman/Documents/cart 353/Firefly_Ecosystem/bit_coin_sounds");
   sounds = path.list();
 
   //fill array with sounds for FrictionSpaces
   for (int j = 0; j < sounds.length;j++) {
     String song2 = "/bit_coin_sounds/" + sounds[j]; 
     AudioSample test = minim.loadSample(song2, 2048);   
     sampleList.add(test);
   }  
   //assign sounds for wind, changed by left and right arrows
   wind = minim.loadSample("winds.aif", 2048);
   //assign sounds for "bootup" ecosystem
   bootup = minim.loadSample("bootup-ecosystem.aif", 2048);
   bootup.trigger();
 
   buzz = minim.loadSample("buzzing-loud.mp3", 2048);
 
   //build Lantern
   lanternFull = new Lantern(width/2, 0, 100);
   lightSource = new Light(width/2, 100);
 
   //build landscape structure
   structure.makeStructure();
 
   //construct Sky with an Attractor
   s = new Sky(); 
 
   //initialize CreatureSystem
   system = new CreatureSystem(structure);
   ocillators = new CreatureSystem(structure);
 
   //construct initial creatures and fill arraylist
   for (int i = 0; i < numberMovers; i++) {
     system.addCreature(s);
   }
 }
 public void draw() {
   background(ground);
   //connect latern to Light and constrain length
   lanternFull.connect(lightSource);
   lanternFull.constrainLength(lightSource, 40, height);
 
   //update Light and check if dragged
   lightSource.update();
   lightSource.drag(mouseX, mouseY);
 
   //display rope between the Lantern and top of the screen, lantern and light
   lanternFull.displayLine(lightSource);
   lightSource.display();
   lanternFull.display();
 
   //if either CreatureSystem has hit the light remove them from the system
   system.hitLight(lightSource);
   ocillators.hitLight(lightSource);
 
   //update and display friction structure of friction spaces
   structure.update();
   structure.display();
 
   //display landscape and attractor
   s.display();
 
   //run creature systems
   system.run();
   ocillators.run();
 
 }
 //move attractor
 public void mousePressed() {
   //check if light was clicked
   lightSource.clicked(mouseX, mouseY);
   //add new attractor at mouse location, LATER: restrict where it can be placed?
   s.setAttractor(new Attractor(mouseX, mouseY)); 
   //trigger noise for new occilators
   buzz.trigger();
   for (int i = 0; i < numberMovers; i++) {
     //adds more creatures
     ocillators.addOscillator(s, mouseX, mouseY);
   }
 }
 public void mouseReleased() {
   //stop dragging the lantern
   lightSource.stopDragging();
 }
 public void keyPressed() {
   if (key == CODED) {
     //add wind to system from left
     if (keyCode == LEFT) {
       wind.trigger();
       system.addWind("L");
     } 
     //add wind to system from right
     if (keyCode == RIGHT) {
       wind.trigger();
       system.addWind("R");
     }
     //stop the wind
     if (keyCode == SHIFT) {
       system.addWind("Stop");
     }
     //increment the number of movers created when 'n' is pressed by 1
     if (keyCode == UP) {
       if (numberMovers!= 30) {
         numberMovers++;
       }
     } 
     //decrement the number of movers created when 'n' is pressed by 1
     if (keyCode == DOWN) {
       if (numberMovers > 0) {
         numberMovers--;
       }
     }
   }
   //clear the screen, start from begin
   if (key == 'c') {
     ground = 0;
     //remove movers
     system.clearSystem();
     ocillators.clearSystem();
     //clear current landscape display
     structure.clearStructure();
     //build new landscape display, IDEA: variables keep adding or subtracting for location so "scene" changes
     structure.makeStructure();
     //trigger construction sound
     bootup.trigger();
     for (int i = 0; i < numberMovers; i++) {
       //adds more creatures
       system.addCreature(s);
     }
   }
   //clears landscapes but leaves creatures without friction
   if (key == 'f') {
     //clear current landscape display
     structure.clearStructure();
     //clear friction so creatures move free
     system.clearFriction();
   }
   //add more creatures doesn't change or remove the landscape
   if (key == 'n') {
     for (int i = 0; i < numberMovers; i++) {
       //adds more creatures
       system.addCreature(s);
     }
   }
   //move landscape but keep creatures
   if (key == 'm') {
     //clear friction so creatures move free
     structure.clearStructure();
     structure.makeStructure();
     system.clearFriction();
   }
   //randomize or organize sounds
   if (key == 's') {
     structure.changeSoundProfile();
   }
   //reverses effect of attractor, makes into a repeller, vice versa
   if (key == 'r') {
     s.reverseAttractor();
   }
 }
 
 /**
  This class will have a visual element and is intended for creation of attractor objects placed by the user,
  the Popular Creature class has the attract method to attract other creatures
  **/
 class Attractor {
   float mass;    // Mass, tied to size
   float G;       // Gravitational Constant
   PVector location;   // Location
   PVector dragOffset;  // holds the offset for when object is clicked on
   PShape hands, welcome, grab;   //mouse svg
 
   Attractor(float _x, float _y) {
     location = new PVector(_x, _y);
     G = 1;
     //Hands designed by Veysel Kara from The Noun Project
     welcome = loadShape("welcome.svg");
     //Hands designed by R\u00e9my M\u00e9dard from The Noun Project
     grab = loadShape("grab.svg");
   }
   //show hand based upon repeller or attractor
   public void display() {
     if (isAttractor) {
       hands = welcome;
     }
     else {
       hands = grab;
     }
     //draw svg hands
     shape(hands, location.x, location.y, 75, 75);
   }
   //reverse Attractor force (repel,attract)
   public void reverseAttractor() {
     if (isAttractor) {
       isAttractor = false;
     }
     else {
       isAttractor = true;
     }
   }
   //is the current attractor repellling or attracting
   public boolean isAttractor() {
     return isAttractor;
   }
 }
 
 /**
  
  This is the main Creature class. 
  A Creature can belong to a CreatureSystem. All forces acted upon Creatures within a CreatureSystem are updated here. Creatures explore the space belonging to a certian Landscape
  and have a certain Movement that determines the way they move around the Landscape. A Creature initially flickers randomly and moves around bumping into
  FrictionSpaces, part of FrictionStructure's making music and illuminating the background. Some Creatures are attracted to an Attractor object that is moved by the user. As Creatures
  explore, they build halos of illumination that show them what structures are around them that they have not hit. They get also begin to fade and die in the CreatureSystem.
  
  **/
 class Creature {
   Movement m;   //movement associated with creature
   Landscape l;   //landscape associated with creature
   PVector location, velocity, acceleration, attracted, curiosity, wind, gravity;
   int maxSpeed, maxForce;   //set max speed for velocity
   float lifeforce, frictionValue; //friction applied from environment onto Creature, lifeforce sents length of time on screen
   float size; //dictates flucauating creature size
   boolean blink; //dictates flashing pattern
   float opacity; //opacity of creature, changes with lifeforce
   Random generator = new Random();
   float gaus = (float) generator.nextGaussian();
 
   float MAXLIFESPAN = 3000;   //set maximum life span for any one creature
   int halo = 255;   //starting halo opacity value for flickering
 
   float spirit; //spirit determines how quickly Creature follows the hand
   float sdSpirit = 25;   // Define a standard deviation
   float meanSpirit = 500;   // Define a mean value 
 
   float gausColor; //flucuating yellow color by gaussian distribution
   float haloOpacity; //opacity for halo showing total spaces explored
   float sdColor = 100; //standard deviation for gaus color
   float meanColor = 200; //mean gaus color
 
   int totalSpacesExplored; //holds all spaces explored by each creature
 
   //Perlin_Movement movementPerlin = new Perlin_Movement(random(100), random(200));
   Random_Movement r = new Random_Movement();
 
   Creature(Landscape defaultL, float seedx, float seedy) {
     m = new Perlin_Movement(random(seedx), random(seedy)); //sets initial movement as random seeded perlin
     l = defaultL; //set initial landscape
     maxSpeed = 2; //set max speed
     maxForce = 2; //set max force
     location = new PVector(random(l.getWidth()), random(l.getHeight())); //choose random location within bounds of_l
     velocity = new PVector(0, 0); //determines rate of movement
     acceleration = new PVector(0, 0); //changes velocity, or rate of movement which changes location (bigger steps)
     wind = new PVector(0, 0); //empty wind force
     gravity = new PVector(0, 0); //empty gravity force
     attracted = l.getAttractorLocation(); //set attractor to landscape-default
     lifeforce = MAXLIFESPAN; //set all lifeforce to max
     frictionValue = 0; //default 0 friction
     spirit = ( gaus * sdSpirit ) + meanSpirit; //gaussian distribution of spirit
     totalSpacesExplored = 0; //default spaces explored to 0
     blink = true;
     haloOpacity = 50; //default halo opacity
   }
   public void update() {
     //update values in Movement
     m.update();
     //apply movement associated with current creature
     applyForce(m.applyMovement()); 
 
     //apply wind force
     applyForce(wind);
 
     //apply friction
     PVector friction = velocity;
     friction.mult(-1); 
     friction.normalize();
     friction.mult(frictionValue);
     applyForce(friction);
 
     // only apply attraction if spirit is below 300, otherwise decrement spirit, IDEA: some creatures should never move toward the attractor
     if (spirit < 300) {
 
       //get the current location of the attractor for landscape
       attracted = l.getAttractorLocation(); 
 
       PVector desired = PVector.sub(attracted, location);   // Calculate direction of force
 
       //steer Creatures towards the Attractor otherwise repel Creatures from Attractor object
       if (l.isAttractor()) {
         desired.normalize();
         desired.mult(maxSpeed);
         PVector steer = PVector.sub(desired, velocity);
         steer.limit(maxForce);
         applyForce(steer);
       }
       else {
         desired.normalize();                           // Normalize vector (distance doesn't matter here, we just want this vector for direction)
         float d = desired.mag();                       // Distance between objects
         d = constrain(d, 5, 100);                    // Keep distance within a reasonable range
         float force = -1 * G / (d * d);            // Repelling force is inversely proportional to distance
         desired.mult(force);                           // Get force vector --> magnitude * direction
         applyForce(desired);
       }
       spirit--;
     }
     else { 
       // Idea: apply some acceleration based on total squares explored that have been touched
       // by each creature or willingness to not be attracted as related to total spaces explored
       // curiousity points to the bottom right, IDEA: Make fireflies more likely explore unexplored 
       // areas with more spirit and more places explored
       spirit--;
     }
 
     //Idea: assign gravity if close to death
     //    if (lifeforce < 200) {
     //      gravity = new PVector(0, 20.0);
     //    }
 
     //apply gravity
     applyForce(gravity); 
 
     velocity.add(acceleration);
     velocity.limit(maxSpeed);
     location.add(velocity);
 
     //decrease lifeforce
     lifeforce = lifeforce - 2;
   }
   public void display() {
 
     //size of firefly creature determined by random generator for un-patterned flickering effect
     float randomSize = random(12);
 
     //calculate size in a gaussian distribution for more ordered flickering effect
     gaus = (float) generator.nextGaussian();
     float sdSize = 4;
     float meanSize = 6;
     float gausSize = ( gaus * sdSize ) + meanSize;
 
     //calcluate gaussiacolor for firefly halos
     gaus = (float) generator.nextGaussian();
     gausColor = ( gaus * sdColor ) + meanColor;
 
     //if spirit is greater than 300 use random flickering pattern, otherwise occilate between size 6 and 15 for a standardized flicker simulating emergence
     if (spirit > 300) {
       //other option of size
       //size = gausSize;
       size = randomSize;
     }
     else {
       if (blink) {
         size = 6;
         blink = false;
       }
       else {
         size = 15;
         blink = true;
       }
     }
 
     //map lifeforce to opacity to fade when they get older and die :(
     opacity = map(lifeforce, 0, MAXLIFESPAN, 0, 255);
     stroke(255, 255, 200, opacity);
     strokeWeight(2);
     fill(255, 255, 100, opacity);
     ellipse(location.x, location.y, size, size);
 
     //flicker halo between white and black
     if (halo == 255) {
       halo = 0;
     }
     else {
       halo = 255;
     }
     fill(halo, haloOpacity);
     stroke(1);
     //make stroke color change with a gaussian pattern
     stroke(255, 255, gausColor, opacity);
     //halo showing how much exploration and enlightment the creatures have
     ellipse(location.x, location.y, size + totalSpacesExplored*4, size + totalSpacesExplored*4);
   }
   public void applyForce(PVector force) {
     acceleration.add(force);
   }
   //if lifeforce is 0 then the firefly is dead
   public boolean isDead() {
     return lifeforce == 0;
   }
   // assign Creature with particular movement (i.e. Perlin, Random)
   public void setMovement(Movement _m) {
     m = _m;
   }
   // returns the current movement applied to the Creature
   public Movement getMovement() {
     return m;
   }
   // assign Creature with particular Landcape (movement bounds)
   public void setLandscape(Landscape _l) {
     l = _l;
   }
   // returns the current landscape (movement bounds) applied to the Creature
   public Landscape getLandscape() {
     return l;
   }
   //return attracted force as PVector 
   public PVector getAttractorForce() { 
     return attracted;
   }
   //set attractor force for landscape's attractor
   public void setAttractorForce(PVector _attracted) { 
     attracted = _attracted;
   }
   //set the friction value applied to the acceleration to 0
   public void clearFriction() {
     frictionValue = 0;
   }
   //increment the spaces explored by the particular creature
   public void exploredSpace() {
     totalSpacesExplored++;
   }
   //apply wind force for a Creature
   public void setWindForce(PVector _wind) {
     wind = _wind;
   }
 }
 
 /**
  Class that organizes groups of Creatures, giving functionality to clear entire systems, run them and remove friction from all Creatures
  
  **/
 class CreatureSystem {
   ArrayList<Creature> system; //holds Creature
   FrictionStructure f; //FrictionStructure assosiated with creature system
   int seedx = 1; //seed for perlin
   int seedy = 10; //seed for perlin
 
   CreatureSystem(FrictionStructure _f) {
     system = new ArrayList<Creature>();
     f = _f;
   }
   //adds creature to creature system arraylist with perlin movement seed values
   public void addCreature(Landscape _l) {
     system.add(new Creature(_l, seedx, seedy));
     seedx += 1;
     seedy += 10;
   }
   //adds ocillating creature to system arraylist
   public void addOscillator(Landscape _l, float x, float y) {
     system.add(new Oscillator(_l, seedx, seedy, x, y));
     seedx += 1;
     seedy += 10;
   }
 
   //clears friction values for all creatures so they can fly freely
   public void clearFriction() {
     Iterator<Creature> it = system.iterator();
     while (it.hasNext ()) {
       Creature c = it.next();
       c.clearFriction();
     }
   }
   //runs through all creatures and updates their locations and removes them if necessary
   public void run() {
     Iterator<Creature> it = system.iterator();
     while (it.hasNext ()) {
       Creature c = it.next();
       f.contains(c);
       c.update();
       c.display();
       if (c.isDead()) {
         it.remove();
       }
     }
   }
   //method applies wind from direction specified by user
   public void addWind(String s) {
     PVector wind;
     Iterator<Creature> it = system.iterator();
 
     //add wind to the right for all Creatures
     if (s.equals("R")) {
       while (it.hasNext ()) {
         Creature c = it.next();
         wind = new PVector(5, 0);
         c.setWindForce(wind);
       }
     }
     //add wind to the left for all Creatures
     if (s.equals("L")) {
       while (it.hasNext ()) {
         Creature c = it.next();
         wind = new PVector(-5, 0);
         c.setWindForce(wind);
       }
     }
     if (s.equals("Stop")) {
       while (it.hasNext ()) {
         Creature c = it.next();
         wind = new PVector(0, 0);
         c.setWindForce(wind);
       }
     }
   }
   //iterates through system to check is creature is found in light location
   public void hitLight(Light l) {
     Iterator<Creature> it = system.iterator();
     while (it.hasNext ()) {
       Creature c = it.next();
       //if Creature has hit Lantern, remove from the array
       if (c.location.x > l.location.x && c.location.x < l.location.x + l.w && c.location.y > l.location.y && c.location.y < l.location.y + l.h) {
         //delete from arraylist
         it.remove();
       }
     }
   }
   //clears all Creatures from the ArrayList
   public void clearSystem() {
     system.clear();
   }
 }
 
 /**
  
  This class creates a FrictionSpace object which is a square in a location with different
  friction values associated which act upon the Creatures that fall within them. 
  FrictionSpace's contain and are bumped by Creatures,
  which turn blue and make a sound when they are hit.
  
  **/
 class FrictionSpace {
 
   float x, y, w, h; //parameters to construct the space
   PVector l; //current creatures location
   float c; //space's friction value
   float xoff = 0.1f; //for noise color
   PVector location; //location of space
   boolean isBumped; 
   boolean visitedBefore;
   boolean withinHalo;
 
   //construct landscape with particular size, location and friction value
   FrictionSpace(float _x, float _y, float _w, float _h, float _c) {
     location = new PVector(_x, _y);  
     x = _x;
     y = _y;
     w = _w;
     h = _h;
     c = _c;
     isBumped = false; //default has not been bumped by Creature
     visitedBefore = false; //default has not been visited more than once by Creature
     withinHalo = false; //default not within halo
   }
   //change color if mover has hit landscape
   public void displayTouched() {
     noStroke();
     fill(0, random(200) + (noise(xoff)*255)%255, random(200) + (noise(xoff)*255)%255, 50);
     rect(x, y, w, h);
     xoff++;
   }
   //display space
   public void display() {
     //if friction space is within halo of creature, illuminate slightly
     noStroke();
     if (withinHalo()) {
       fill(100, 0, 100, 30);
       rect(x, y, w, h);
     }
     else {
       fill(255, 0);
       rect(x, y, w, h);
     }
     xoff++;
   }
   public void update() {
   }
   //return true if FrictionSpace falls within any Creature's halo
   public boolean withinHalo() {
     return withinHalo;
   }
 
   //does landscape contain a particular mover?
   public boolean contains(Creature m) {
     l = m.location;
     //if isBumped is true then this frictionspace has been visited before
     if (isBumped) {
       visitedBefore = true;
     }
     //the space falls within a Creatures halo of illumination, based on the total squares explored, withinHalo is true
     if (l.x + m.totalSpacesExplored > x && l.x + m.totalSpacesExplored < x + w && l.y + m.totalSpacesExplored > y && l.y + m.totalSpacesExplored < y + h) {
       withinHalo = true;
     }
     //Creature's location falls within FrictionSpace
     if (l.x > x && l.x < x + w && l.y > y && l.y < y + h) {
      println(m + "in friction space");
       isBumped = true;
       return true;
     }
    else 
      if (m instanceof Oscillator) {
      Oscillator o = (Oscillator)m;
      return false;
      //      if (o.x > x && o.x < x + w && o.y > y && o.y < y + h) {
      //        return true;
      //      }
    }
     else {
       return false;
     }
   }
   //Returns whether landscape has been bumped at least once in order to change color in display
   public boolean bumped() {
     return isBumped;
   }
   //Returns whether FrictionSpace has been visited more than once, important for triggering sounds in FrictionStructure
   public boolean visitedBefore() {
     return visitedBefore;
   }
 }
 
 /**
  
  This class creates a FrictionStructure object which is a collection of FrictionSpaces with changing formations. This class is
  responsible for triggering visual changes and sounds from the FrictionSpaces.
  
  **/
 class FrictionStructure {
 
   ArrayList<FrictionSpace> landscapes = new ArrayList<FrictionSpace>(); //holds FrictionSpaces
   int n = 0; //limits the structures extent on sketch
   float frictionDecrease; //to increase and decrease friction values
   float frictionIncrease;
   boolean randomizeSound; //determines whether sound corresponds with friction values or randomized to be in the higher octaves
 
   FrictionStructure() {
     frictionDecrease = 0;
     frictionIncrease = 0;
     randomizeSound = false; //default is mapped to friction
   }
   public void update() {    
     for (FrictionSpace landscape : landscapes) {
       if (landscape.bumped()) {
         landscape.update();
       }
     }
   }
     //display FrictionSpaces that have been touched
     public void display() {
       for (FrictionSpace landscape : landscapes) {
         if (landscape.bumped()) {
           landscape.displayTouched();
         }
         else {
           landscape.display();
         }
       }
     }
     //clear entire structure of FrictionSpaces
     public void clearStructure() {
       landscapes.clear();
     }
     //Cycles through friction spaces and checks if current Creature falls within it
     public void contains(Creature _c) {
       for (FrictionSpace landscape : structure.landscapes) {
         //if landscape contains a creature, play the random sound and lighten the background color value
         if (landscape.contains(_c)) { 
           //assign friction value of current space to Creature's forces
           _c.frictionValue = landscape.c;
 
           //map sound to level of friction
           sound = soundPreference(landscape);
 
 
           //if landscape has been explored already don't make a sound
           if (!landscape.visitedBefore()) {
             //increment total explored space for Creature
             _c.exploredSpace();
             //play sound
             sampleList.get(sound).trigger();
             sampleList.get(sound).setGain(0.4f);
             //make background lighter
             ground = ground + 1;
           }
         }
       }
     }
     //set sound preference with current value of randomizeSound
     public int soundPreference(FrictionSpace f) {
       if (randomizeSound) {
         //use higher register sounds only
         return PApplet.parseInt(random(sampleList.size()/2, sampleList.size() - 1));
       }
       else {
         //map sounds to friction values on FrictionStructure
         return PApplet.parseInt(map(f.c, frictionDecrease, frictionIncrease, 0, sampleList.size() - 1));
       }
     }
     //trigger change in sound profile
     public void changeSoundProfile() {
       if (randomizeSound) {
         randomizeSound = false;
       }
       else {
         randomizeSound = true;
       }
     }
     //construct FrictionStructure landscape
     public void makeStructure() {
 
       //limit what n can be to avoid moving too far off screen or make an edges method to scale within bounding box
       n = n%35;
       
       //randomly changes total landscapes built between 0 and 40
       for (int i=0;i< random(40);i++) {
 
         landscapes.add(new FrictionSpace(0+n, 70+n, 5, 5, frictionDecrease));
         landscapes.add(new FrictionSpace(0+n, 100+n, 10, 10, frictionDecrease));
         landscapes.add(new FrictionSpace(0+n, 130+n, 10, 10, frictionDecrease));
 
         landscapes.add(new FrictionSpace(0+n, 200+n, 10, 10, frictionDecrease));
         landscapes.add(new FrictionSpace(0+n, 230+n, 10, 10, frictionDecrease));
         landscapes.add(new FrictionSpace(0+n, 260+n, 10, 10, frictionDecrease));
 
         landscapes.add(new FrictionSpace(70+n, 0+n, 10, 10, frictionDecrease));
         landscapes.add(new FrictionSpace(100+n, 0+n, 10, 10, frictionDecrease));
         landscapes.add(new FrictionSpace(130+n, 0+n, 10, 10, frictionDecrease));
 
         landscapes.add(new FrictionSpace(200+n, 0+n, 10, 10, frictionDecrease));
         landscapes.add(new FrictionSpace(230+n, 0+n, 10, 10, frictionDecrease));
         landscapes.add(new FrictionSpace(260+n, 0+n, 10, 10, frictionDecrease));
 
         landscapes.add(new FrictionSpace(width-n, height-70-n, 5, 5, frictionIncrease));
         landscapes.add(new FrictionSpace(width-n, height-100-n, 10, 10, frictionIncrease));
         landscapes.add(new FrictionSpace(width-n, height-130-n, 10, 10, frictionIncrease));
 
         landscapes.add(new FrictionSpace(width-n, height-200-n, 10, 10, frictionIncrease));
         landscapes.add(new FrictionSpace(width-n, height-230-n, 10, 10, frictionIncrease));
         landscapes.add(new FrictionSpace(width-n, height-260-n, 10, 10, frictionIncrease));
 
         landscapes.add(new FrictionSpace(width-70-n, height-0-n, 10, 10, frictionIncrease));
         landscapes.add(new FrictionSpace(width-100-n, height-0-n, 10, 10, frictionIncrease));
         landscapes.add(new FrictionSpace(width-130-n, height-0-n, 10, 10, frictionIncrease));
 
         landscapes.add(new FrictionSpace(width-200-n, height-0-n, 10, 10, frictionIncrease));
         landscapes.add(new FrictionSpace(width-230-n, height-0-n, 10, 10, frictionIncrease));
         landscapes.add(new FrictionSpace(width-260-n, height-0-n, 10, 10, frictionIncrease));
         n += 10;
         frictionIncrease++;
         frictionDecrease--;
       }
     }
   }
 
 /**
 The is the Landscape class, Landscapes have a dedicated attractor, a size and any number of display option
 **/
 abstract class Landscape {
   float lHeight;
   float lWidth; 
   PVector lCenter, lCorner;
   Attractor a;
 
   Landscape() {
     lHeight = height; //default height and width is the size of the applet
     lWidth = width;
     lCenter = new PVector(lWidth/2, lHeight/2);
     lCorner = new PVector(0, 0); //default
     a = new Attractor(lWidth/2, lHeight/2);
   }
   public void display() {
     a.display();
     noStroke();
     rect(lCorner.x, lCorner.y, lWidth, lHeight);
   }
   public float getHeight() {
     return lHeight;
   }
   public float getWidth() {
     return lWidth;
   }
   //assign new attractor to landscape
   public void setAttractor(Attractor _a) {
     a = _a;
   }
   //return the attractor
   public PVector getAttractorLocation() {
     return a.location;
   }
   public boolean isAttractor(){
    return a.isAttractor(); 
   }
   public void reverseAttractor(){
     a.reverseAttractor();
   }
 }
 
 /**
  This class creates Latern objects that are attached to Light objects. The methods are from Nature of Code for the Spring and Bob class.
  **/
 class Lantern {
 
   PVector location, velocity, acceleration; //dictates where latern can be pulled and how fast it travels
 
   float len; // rest length
   float k = 0.1f; //moves slower
 
   Lantern(float x, float y, int l) {
     location = new PVector(x, y);
     len = l;
   }
   // Calculate spring force
   public void connect(Light l) {
     // Vector pointing from anchor to bob location
     PVector force = PVector.sub(l.location, location);
     // What is distance
     float d = force.mag();
     // Stretch is difference between current distance and rest length
     float stretch = d - len;
 
     // Calculate force according to Hooke's Law
     // F = k * stretch
     force.normalize();
     force.mult(-1 * k * stretch);
     l.applyForce(force);
   }
   // Constrain the distance between bob and anchor between min and max
   public void constrainLength(Light l, float minlen, float maxlen) {
     PVector dir = PVector.sub(l.location, location);
     float d = dir.mag();
     // Is it too short?
     if (d < minlen) {
       dir.normalize();
       dir.mult(minlen);
       // Reset location and stop from moving (not realistic physics)
       l.location = PVector.add(location, dir);
       l.velocity.mult(0);
       // Is it too long?
     } 
     else if (d > maxlen) {
       dir.normalize();
       dir.mult(maxlen);
       // Reset location and stop from moving (not realistic physics)
       l.location = PVector.add(location, dir);
       l.velocity.mult(0);
     }
   }
   //display lantern object
   public void display() {
     noStroke();
     fill(255, 10);
     strokeWeight(1);
     rectMode(CENTER);
     rect(location.x, location.y, 10, 10);
   }
   //display line between lantern and light and origin point of the "rope"
   public void displayLine(Light l) {
     strokeWeight(2);
     stroke(0);
     line(l.location.x, l.location.y, location.x, location.y);
   }
 }
 /**
 The Light class is a moveable connectable object that shows a flickering yellow light and a lantern svg, 
 it can be attached to a Lantern object and moved around with spring motions. Almost like a bungee cord hung the lantern.
 **/
 class Light {
   float mass = 100;
   float damping = 0.98f;  // Arbitrary damping to simulate friction / drag 
 
   // For mouse interaction
   PVector dragOffset, location, velocity, acceleration;
   boolean dragging = false; //default not dragging
 
   PShape lanternsvg;
   
   Random generator = new Random();
   float gausColor; //for flicker of light
   float sdColor = 100;
   float meanColor = 200;
   float w, h; //width/height of light
 
   // Constructor
   Light(float x, float y) {
     location = new PVector(x, y);
     velocity = new PVector();
     acceleration = new PVector();
     dragOffset = new PVector();
     lanternsvg = loadShape("lantern.svg");
     w = 75;
     h = 75;
   } 
 
   // Standard Euler integration
   public void update() { 
     //apply gravity to the light source to drag it back to center
     PVector gravityLantern = new PVector(0, 2);
     applyForce(gravityLantern);
 
     velocity.add(acceleration);
     velocity.mult(damping);
     location.add(velocity);
     acceleration.mult(0);
   }
 
   // Newton's law: F = M * A
   public void applyForce(PVector force) {
     PVector f = force.get();
     f.div(mass);
     acceleration.add(f);
   }
   // Draw light and lantern svg
   public void display() {
     float gaus = (float) generator.nextGaussian();
     gausColor = ( gaus * sdColor ) + meanColor;
     noStroke();
     fill(255, 255, gausColor);
     if (dragging) {
       fill(255);
     }
     ellipse(location.x, location.y, random(70, 90), random(70, 90));
     shape(lanternsvg, location.x-37, location.y-35, 75, 75);
   } 
 
   // The methods below are for mouse interaction
 
   // This checks to see if we clicked on the light
   public void clicked(int mx, int my) {
     float d = dist(mx, my, location.x, location.y);
     if (d < mass) {
       dragging = true;
       dragOffset.x = location.x-mx;
       dragOffset.y = location.y-my;
     }
   }
   public void stopDragging() {
     dragging = false;
   }
 
   public void drag(int mx, int my) {
     if (dragging) {
       location.x = mx + dragOffset.x;
       location.y = my + dragOffset.y;
     }
   }
 }
 
 /**
  This class defines an abstract class Movement that can be applied to any Creature.
  **/
 abstract class Movement {
   Movement() {}
   public void update() {}
   public PVector applyMovement() { // this method will return a PVector to use in the Creature's applyForce() method, the PVector represents the amount acceleration changed
     return new PVector(random(1), random(1));  //return pvector with added value, in this case it is random
   }
 }
 
 /**
  
  This class creates Ocillator objects that hover around the Attractor (hand) and do not cause sound when they hover over the landscape.
  Original Code from Daniel Shiffman. These creatures do not flicker in unison like the others. They pivot around the location the are constructed with.
  
  **/
 
 class Oscillator extends Creature {   
 
   PVector angle;
   PVector velocity;
   PVector amplitude;
   PVector location;
 
   Oscillator(Landscape defaultL, float seedx, float seedy, float _x, float _y) {
     super(defaultL, seedx, seedy);
     angle = new PVector();
     velocity = new PVector(random(-0.05f, 0.05f), random(-0.05f, 0.05f));
     amplitude = new PVector(random(20, width/4), random(20, height/4));
     location = new PVector(_x, _y);
     blink = true;
   }   
 
   public void update() {
     //change angle's velocity for ocillation
     angle.add(velocity);
     velocity.add(acceleration);
     velocity.limit(maxSpeed);
     location.add(velocity);
     spirit--;
     //decrease lifeforce
     lifeforce = lifeforce - 2;
   }   
   public void display() {   
     float randomSize = random(12);
     float x = sin(angle.x)*amplitude.x;
     float y = sin(angle.y)*amplitude.y;
     //map lifeforce to opacity to fade when they get older and die :(
     float opacity = map(lifeforce, 0, MAXLIFESPAN, 0, 255);    //if spirit is greater than 300 use random flickering pattern, otherwise occilate between size 6 and 15 for a standardized flicker simulating emergence
     //if spirit is less than 300, blink, meaning occilate between two sizes, 6 and 12
     if (spirit > 300) {
       //size = gausSize;
       size = randomSize;
     }
     else {
       if (blink) {
         size = 6;
         blink = false;
       }
       else {
         size = 15;
         blink = true;
       }
     }
     pushMatrix();
     translate(x, y);
     //size of firefly creature determined by random generator for un-patterned flickering effect
     ellipse(location.x, location.y, randomSize, randomSize);
     stroke(255, 255, 10, opacity);
     fill(255, opacity);
     popMatrix();
   }
 }   
 
 /**
  This class defines a PerlinMovement which is an extension of the Movement class, that can be applied to a Creature.
  This ensures any implementation of the Movement class can apply to any Creatures.
  
  **/
 
 class Perlin_Movement extends Movement {
 
   float x, y, seedx, seedy; //seeds for perlin movement
   PVector force;
   
   Perlin_Movement(float _seedx, float _seedy) {
     seedx = _seedx;
     seedy = _seedy;
   }
   //map noise to acceleration
   public void update() {
     float accelerationNoiseX = noise(seedx);
     float accelerationNoiseY = noise(seedy);
     x = map(accelerationNoiseX, 0, 1, 10, -10);
     y = map(accelerationNoiseY, 0, 1, -10, 10);
     seedx =  seedx + 0.1f;
     seedy = seedy + 0.3f;
   }
   // this method will return a PVector to use in the Creature's applyForce() method, the PVector represents the amount acceleration changed
   public PVector applyMovement() {
     force = new PVector(x, y);
     //return pvector with added value, in this case it is random
     return force;
   }
 }
 
 /**
 Produces a RandomMovement that can be applied to any Creature
 **/
 class Random_Movement extends Movement {
   PVector force;
 
   Random_Movement() {
     force = new PVector(random(-1.0f, 1.0f), random(-1.0f, 1.0f));
   }
   public void update() {
     force = new PVector(random(-1.0f, 1.0f), random(-1.0f, 1.0f));
   }
   // this method will return a PVector to use in the Creature's applyForce() method, the PVector represents the amount acceleration changed
   public PVector applyMovement() { 
     //return pvector with added value, in this case it is random
     return force;
   }
 }
 
 /**
 This class defines a Sky object that takes up the entire sketch size and displays an attractor object. 
 This class has the potential to apply other bounds on the Creatures and designs on the background, but currently just shows the attractor. This is an underutilized class
 and was meant for more complex Landscape visuals in prior iterations
 **/
 class Sky extends Landscape {
   Sky() {
     lHeight = height; //default height and width is the size of the applet
     lWidth = width;
     lCenter = new PVector(lWidth/2, lHeight/2);
     lCorner = new PVector(0, 0); //default
   }
   public void display() {
     noStroke();
     a.display();
   }
 }
 
   static public void main(String args[]) {
     PApplet.main(new String[] { "--bgcolor=#FFFFFF", "Firefly_Ecosystem" });
   }
 }
