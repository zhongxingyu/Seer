 
 
 
 package src.game.base ;
 import src.game.common.* ;
 import src.game.actors.* ;
 import src.game.building.* ;
 import src.game.planet.* ;
 import src.graphics.common.* ;
 import src.graphics.jointed.* ;
 import src.graphics.widgets.HUD;
 import src.user.* ;
 import src.util.* ;
 
 
 
 /**  Trade ships come to deposit and collect personnel and cargo.
   */
 //
 //  NOTE:  This class has been prone to bugs where sprite position appears to
 //  'jitter' when passing over blocked tiles below, due to the mobile class
 //  attempting to 'correct' position after each update.  This class must treat
 //  all tiles as passable to compensate.
 
 
 
 public class Dropship extends Vehicle implements Inventory.Owner {
   
   
   
   /**  Fields, constants, constructors and save/load methods-
     */
   final static String
     FILE_DIR = "media/Vehicles/",
     XML_PATH = FILE_DIR+"VehicleModels.xml" ;
   final static Model
     FREIGHTER_MODEL = MS3DModel.loadMS3D(
       Dropship.class, FILE_DIR, "dropship.ms3d", 1.0f
     ).loadXMLInfo(XML_PATH, "Dropship") ;
   
   
   final static float
     LOWER_FLIGHT_HEIGHT = 5,
     UPPER_FLIGHT_RADIUS = 7,
     UPPER_FLIGHT_TIME   = 9,
     LOWER_FLIGHT_TIME   = 7,
     TOTAL_FLIGHT_TIME = UPPER_FLIGHT_TIME + LOWER_FLIGHT_TIME ;
   final public static int
     STAGE_DESCENT  = 0,
     STAGE_LANDED   = 1,
     STAGE_BOARDING = 2,
     STAGE_ASCENT   = 3,
     STAGE_AWAY     = 4 ;
   final public static int
     MAX_CAPACITY   = 100,
     MAX_PASSENGERS = 10 ;
   
   
   private Vec2D liftOff = new Vec2D() ;
   private Vec2D landPos = new Vec2D() ;
   private float initTime = 0 ;
   private int stage = STAGE_AWAY ;
   
   
   
   public Dropship() {
     super() ;
     attachSprite(FREIGHTER_MODEL.makeSprite()) ;
     this.stage = STAGE_AWAY ;
   }
   
   
   public Dropship(Session s) throws Exception {
     super(s) ;
     liftOff.set(s.loadFloat(), s.loadFloat()) ;
     landPos = new Vec2D().set(s.loadFloat(), s.loadFloat()) ;
     initTime = s.loadFloat() ;
     stage = s.loadInt() ;
   }
   
   
   public void saveState(Session s) throws Exception {
     super.saveState(s) ;
     s.saveFloat(liftOff.x) ; s.saveFloat(liftOff.y) ;
     s.saveFloat(landPos.x) ; s.saveFloat(landPos.y) ;
     s.saveFloat(initTime) ;
     s.saveInt(stage) ;
   }
   
   
   public int owningType() { return Element.VENUE_OWNS ; }
   public int pathingType() { return Tile.PATH_BLOCKS ; }
   public float height() { return 2.0f ; }
   public float radius() { return 2.0f ; }
   
   
 
 
   /**  Perform the actual exchange of goods and people.
     */
   public void updateAsScheduled(int numUpdates) {
   }
   
   
   public Behaviour jobFor(Actor actor) {
     if (stage == STAGE_BOARDING) {
       final Action boardAction = new Action(
         actor, this,
         this, "actionBoard",
         Action.STAND, "boarding "+this
       ) ;
       boardAction.setPriority(Behaviour.URGENT) ;
       return boardAction ;
     }
     //
     //  ...You also need to try making deliveries/collections, based on what
     //  nearby venues have either a shortage or excess of.
     
     return super.jobFor(actor) ;
   }
   
   
   public boolean actionBoard(Actor actor, Dropship ship) {
     actor.exitWorld() ;  //This may be a little extreme, but needed atm...
     ship.setInside(actor, true) ;
     return true ;
   }
   
   
   public void beginBoarding() {
     if (stage != STAGE_LANDED) I.complain("Cannot board until landed!") ;
     stage = STAGE_BOARDING ;
   }
   
   
   public boolean allBoarded() {
     boolean all = true ;
     for (Actor c : crew) if (c.aboard() != this) all = false ;
     return all ;
   }
   
   
   protected void offloadPassengers() {
     final int size = (int) Math.ceil(radius() * 2) ;
     final int EC[] = Spacing.entranceCoords(size, size, entranceFace) ;
     final Vec3D p = this.position ;
     final Tile exit = world.tileAt(p.x + EC[0], p.y + EC[1]) ;
     for (Mobile m : inside()) if (! m.inWorld()) {
       m.enterWorldAt(exit.x, exit.y, world) ;
     }
   }
   
   
   
   /**  Handling the business of ascent and landing-
     */
   protected boolean checkLandingArea(World world, Box2D area) {
     for (Tile t : world.tilesIn(area, false)) {
       if (t == null || t.owningType() > Element.ENVIRONMENT_OWNS) return false ;
     }
     return true ;
   }
   
   
   private void performLandingAt(World world, Box2D area) {
     for (Tile t : world.tilesIn(area, false)) {
       if (t.owner() != null) t.owner().setAsDestroyed() ;
     }
     area = new Box2D().setTo(area).expandBy(-1) ;
     for (Tile t : world.tilesIn(area, false)) {
       t.setOwner(this) ;
     }
     for (Tile t : world.tilesIn(area, false)) {
       for (Mobile m : t.inside()) {
         final Tile e = Spacing.nearestOpenTile(m.origin(), m) ;
         m.setPosition(e.x, e.y, world) ;
       }
     }
   }
   
   
   private void performTakeoff(World world, Box2D area) {
     area = new Box2D().setTo(area).expandBy(-1) ;
     for (Tile t : world.tilesIn(area, false)) {
       t.setOwner(null) ;
     }
   }
   
   
   private Box2D landArea() {
     final Box2D area = area(null) ;
     area.clipToMultiple(1) ;
     area.expandBy(1) ;
     I.say("Landing area is: "+area) ;
     return area ;
   }
   
   
   
   /**  Code for finding a suitable landing site-
     */
   public static Box2D findLandingSite(Dropship landing, final Base base) {
     //
     //  Firstly, determine a reasonable starting position-
     final World world = base.world ;
     final Tile midTile = world.tileAt(world.size / 2, world.size / 2) ;
     final Target nearest = world.presences.randomMatchNear(base, midTile, -1) ;
     if (nearest == null) return null ;
     final Tile init = Spacing.nearestOpenTile(world.tileAt(nearest), midTile) ;
     if (init == null) return null ;
     return findLandingSite(landing, init, base) ;
   }
   
   
   public static Box2D findLandingSite(
     final Dropship landing, final Tile init, final Base base
   ) {
     //
     //  Then, spread out to try and find a decent landing site-
     final Box2D area = landing.landArea() ;
     final TileSpread spread = new TileSpread(init) {
       protected boolean canAccess(Tile t) {
         if (Spacing.distance(t, init) > World.DEFAULT_SECTOR_SIZE) return false ;
         return ! t.blocked() ;
       }
       protected boolean canPlaceAt(Tile t) {
         area.xpos(t.x) ;
         area.ypos(t.y) ;
         return landing.checkLandingArea(base.world, area) ;
       }
     } ;
     spread.doSearch() ;
     if (spread.success()) return area ;
     return null ;
   }
   
   
   
  //
  //  TODO:  Fade out alpha during the final and initial stages of flight.
  
   public void beginDescent(Box2D area, World world) {
     if (inWorld()) I.complain("Already in world!") ;
     this.landPos.set(
       area.xpos() + (area.xdim() / 2),
       area.ypos() + (area.ydim() / 2)
     ) ;
     this.entranceFace = Venue.ENTRANCE_SOUTH ;
     this.liftOff.setFromAngle(Rand.num() * 360) ;
     liftOff.scale(UPPER_FLIGHT_RADIUS).add(landPos) ;
     liftOff.x = Visit.clamp(liftOff.x, 0, world.size - 1) ;
     liftOff.y = Visit.clamp(liftOff.y, 0, world.size - 1) ;
     this.initTime = world.currentTime() ;
     this.stage = STAGE_DESCENT ;
     
     final Vec3D p = getShipPos(0) ;
     enterWorldAt((int) p.x, (int) p.y, world) ;
     position.setTo(nextPosition.setTo(p)) ;
     rotation = nextRotation = getShipRot(0, true) ;
   }
   
   
   protected void completeDescent() {
     this.performLandingAt(world, landArea()) ;
     this.offloadPassengers() ;
     
     initTime = world.currentTime() ;
     stage = STAGE_LANDED ;
     ///for (Item item : cargo.allItems()) cargo.transfer(item, dropPoint) ;
   }
   
   
   public void beginAscent() {
     this.performTakeoff(world, landArea()) ;
     
     if (! inWorld()) I.complain("No longer in world!") ;
     ///if (dropPoint instanceof DropZone) dropPoint.exitWorld() ;
     initTime = world.currentTime() ;
     stage = STAGE_ASCENT ;
   }
   
   
   protected void completeAscent() {
     initTime = world.currentTime() ;
     exitWorld() ;
   }
   
   
   public int flightStage() {
     return stage ;
   }
   
   
   public boolean landed() {
     return stage > STAGE_DESCENT && stage < STAGE_ASCENT ;
   }
   
   
   public float timeSinceDescent(World world) {
     if (! landed()) {
       return -1 ;
       //I.complain("Can't get descent time!") ;
     }
     return world.currentTime() - initTime ;
   }
   
   
   public float timeSinceAscent(World world) {
     if (stage != STAGE_AWAY) {
       return -1 ;
       //I.complain("Can't get ascent time!") ;
     }
     return world.currentTime() - initTime ;
   }
   
   
   
   /**  Updating motion over time-
     */
   protected void updateAsMobile() {
     super.updateAsMobile() ;
     if (stage == STAGE_DESCENT) {
       float time = Math.min(world.currentTime() - initTime, TOTAL_FLIGHT_TIME) ;
       this.nextPosition.setTo(getShipPos(time)) ;
       this.nextRotation = getShipRot(time, true) ;
       if (time == TOTAL_FLIGHT_TIME) {
         completeDescent() ;
         stage = STAGE_LANDED ;
       }
     }
     if (stage == STAGE_ASCENT) {
       float time = TOTAL_FLIGHT_TIME - (world.currentTime() - initTime) ;
       if (time < 0) {
         completeAscent() ;
         stage = STAGE_AWAY ;
       }
       else {
         this.nextPosition.setTo(getShipPos(time)) ;
         this.nextRotation = getShipRot(time, true) ;
       }
     }
     /*
     I.say("Current position/rotation: "+position+"/"+rotation) ;
     I.say("Next position/rotation: "+nextPosition+"/"+nextRotation) ;
     I.say("Stage is: "+stage) ;
     //*/
   }
   
   
   public boolean blocksMotion(Boardable t) {
     return true ;
   }
   
   
   public void pathingAbort() {
     //  Not sure what to do here, exactly...
   }
   
   
   private Vec3D getShipPos(float time) {
     if (time < UPPER_FLIGHT_TIME) {
       final float
         progress = time / UPPER_FLIGHT_TIME,
         asAngle  = (float) Math.toRadians(progress * 90),
         horiz = (float) Math.sin(asAngle),
         vert  = (float) Math.cos(asAngle) ;
       final Vec3D p = new Vec3D(
         (landPos.x * horiz) + (liftOff.x * (1 - horiz)),
         (landPos.y * horiz) + (liftOff.y * (1 - horiz)),
         LOWER_FLIGHT_HEIGHT + (UPPER_FLIGHT_RADIUS * vert)
       ) ;
       //if (BaseUI.isPicked(this)) I.say("  ship position: "+p) ;
       //I.say("  Horiz/vert are: "+horiz+"/"+vert+", pos: "+p) ;
       return p ;
     }
     else {
       final float height = LOWER_FLIGHT_HEIGHT *
         (1 - ((time - UPPER_FLIGHT_TIME) / LOWER_FLIGHT_TIME)) ;
       return new Vec3D(landPos.x, landPos.y, height) ;
     }
   }
   
   
   private float getShipRot(float time, boolean descent) {
     ///if (true) return 0 ;
     final Vec2D initVec = new Vec2D(liftOff).sub(landPos).normalise() ;
     if (descent) initVec.scale(-1) ;
     final float rotate = time / (UPPER_FLIGHT_TIME + LOWER_FLIGHT_TIME) ;
     final float landRot = entranceFace * 360 / 4f ;
     final float
       initAngle = initVec.toAngle(),
       terpAngle = Vec2D.degreeDif(landRot, initAngle) * rotate ;
     return (initAngle + terpAngle + 360) % 360 ;
   }
   
   //  There is some kind of massive instability being caused here.
   
   
   
   /**  Rendering and interface methods-
     */
   public void renderFor(Rendering rendering, Base base) {
     final Sprite s = this.sprite() ;
     float progress = 1 ;
     if (stage == STAGE_DESCENT || stage == STAGE_ASCENT) {
       progress = world.timeWithinFrame() - initTime ;
       if (stage == STAGE_ASCENT) progress = TOTAL_FLIGHT_TIME - progress ;
       if (progress > UPPER_FLIGHT_TIME) {
         progress -= UPPER_FLIGHT_TIME ;
         progress /= LOWER_FLIGHT_TIME ;
       }
       else progress = 0 ;
     }
     s.setAnimation("descend", Visit.clamp(progress, 0, 1)) ;
     s.colour = Colour.transparency(progress) ;
     super.renderFor(rendering, base) ;
   }
   
   
   public String fullName() {
     return "Freighter" ;
   }
   
   
   public Composite portrait(HUD UI) {
     return null ;
   }
   
   
   public String helpInfo() {
     return null ;
   }
   
   
   public void writeInformation(Description d, int categoryID, HUD UI) {
   }
 }
 
 
 
 ///I.say("Performing boarding action...") ;
 
 /*
 public void setupWithStage(
   Base client, Box2D site, float facing, int stage, float timeElapsed
 ) {
   final World world = this.world == null ? client.world : this.world ;
   
   this.landingSite.setTo(site) ;
   this.landPos.set(
     site.xpos() + (site.xdim() / 2),
     site.ypos() + (site.ydim() / 2)
   ) ;
   this.entranceFace = facing ;
   this.liftOff.setFromAngle(Rand.num() * 360).scale(UPPER_FLIGHT_RADIUS) ;
   this.initTime = world.currentTime() - timeElapsed ;
   this.client = client ;
   this.stage = stage ;
   
   final boolean inWorld = stage != STAGE_AWAY ;
   if (inWorld && ! inWorld()) {
     final Vec3D pos = getShipPos(0) ;
     enterWorldAt((int) pos.x, (int) pos.y, world) ;
     updateAsMobile() ;
   }
   if (inWorld() && ! inWorld) {
     exitWorld() ;
   }
 }
 //*/
 
 /*
 public void writeInformation(Description text, int categoryID) {
   cargo.writeInformation(text) ;
 }
 
 public float scheduledInterval() { return 1 ; }
 
 public void updateOnSchedule(int numUpdates) {
 }
 //*/
 
 
 /*
 A parabolic curve is y = x^2.  Flip that on it's side, and you have y = sqrt(x),
 where y is lateral distance from the descent point.
 Add the minimum descent height.
 
 Parametric equation would be: X = 2at, Y = a(t^2)
 
 
 
 */
 
 
 
 
 
 
 
 
 
