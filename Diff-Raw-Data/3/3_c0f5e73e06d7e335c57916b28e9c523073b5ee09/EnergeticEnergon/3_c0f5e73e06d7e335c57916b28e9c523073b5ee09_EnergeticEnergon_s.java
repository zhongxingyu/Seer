 package team298;
 
 import battlecode.common.*;
 import static battlecode.common.GameConstants.*;
 import java.util.*;
 
 public class EnergeticEnergon extends Base {
 
     public MapStore map;
     public MexicanMessaging messaging;
     public NaughtyNavigation navigation;
     public SensationalSensing sensing;
     public ArrayList<EnergonTransferRequest> requests;
     public ArrayList<LowAllyRequest> lowAllyRequests;
     public int lowAllyRequestsTurn = 0;
     public double lowEnergonLevel;
 
     public EnergeticEnergon(NovaPlayer player) {
         super(player);
         map = player.map;
         messaging = player.messaging;
         navigation = player.navigation;
         sensing = player.sensing;
 
         requests = new ArrayList<EnergonTransferRequest>();
         lowAllyRequests = new ArrayList<LowAllyRequest>();
         lowEnergonLevel = controller.getRobotType().maxEnergon() * .3;
     }
 
     public void transferFlux(MapLocation location) {
         try {
             Robot robot = player.controller.senseAirRobotAtLocation(location);
             if(robot == null) {
                 location = sensing.senseClosestArchon();
                 if(location.distanceSquaredTo(player.controller.getLocation()) > 2) {
                     return;
                 }
             }
             
             double amount = player.controller.getFlux();
             player.controller.transferFlux(amount, location, RobotLevel.IN_AIR);
         } catch (Exception e) {
             pa("---Caught exception in transferFlux");
             e.printStackTrace();
         }
     }
     
     public void addRequest(MapLocation location, boolean isAirUnit, int amount) {
         requests.add(new EnergonTransferRequest(location, isAirUnit, amount));
     }
 
     public void addLowAllyRequest(MapLocation location, int level, int reserve, int max) {
         lowAllyRequests.add(new LowAllyRequest(location, level, reserve, max));
     }
 
     /**
      * Auto energon transfers
      */
     public void autoTransferEnergon() {
         //p("auto transfer energon");
         ArrayList<RobotInfo> robots = sensing.senseAlliedRobotInfoInSensorRange();
         for(RobotInfo robot : robots) {
             if(robot.type.isBuilding()) continue;
             if(robot.location.distanceSquaredTo(controller.getLocation()) > 2) continue;
             int amount = calculateEnergonRequestAmount(robot);
             if(amount >= 1) {
                 requests.add(new EnergonTransferRequest(robot.location, false, amount));
                 //p("adding request: "+requests.get(requests.size()-1).toString());
             }
         }
     }
 
     /**
      * Auto energon transfers between units
      */
     public void autoTransferEnergonBetweenUnits() {
         if(controller.getEnergonLevel() < controller.getRobotType().maxEnergon() / 2) {
             return;
         }
         if(!(player.isChainer || player.isTurret)) {
             RobotInfo min = null;
             ArrayList<RobotInfo> robots = sensing.senseAlliedRobotInfoInSensorRange();
             for(RobotInfo robot : robots) {
                 if(robot.type.isBuilding()) continue;
                 if(!robot.location.isAdjacentTo(controller.getLocation())) continue;
                 if(min == null) {
                     min = robot;
                 } else {
                     double percent = (robot.energonLevel + robot.energonReserve) / robot.type.maxEnergon();
                     double minpercent = (min.energonLevel + min.energonReserve) / min.type.maxEnergon();
                     if(percent < minpercent) {
                         min = robot;
                     }
                 }
             }
             if(min == null) {
                 return;
             }
             double amount = calculateEnergonRequestAmount(min);
             if(amount < 2) {
                 return;
             }
             transferEnergon(amount, min.location, min.type.isAirborne());
         } else {
             if(lowAllyRequestsTurn + 1 <= Clock.getRoundNum()) {
                 return;
             }
             LowAllyRequest min = null;
             double percent, minpercent = 500;
             for(LowAllyRequest cur : lowAllyRequests) {
                 if(!cur.location.isAdjacentTo(controller.getLocation())) {
                     continue;
                 }
                 if(min == null) {
                     min = cur;
                     minpercent = (min.level + min.reserve) / min.max;
                 } else {
                     percent = (cur.level + cur.reserve) / cur.max;
                     if(percent < minpercent) {
                         min = cur;
                         minpercent = percent;
                     }
                 }
             }
 
             if(min == null) {
                 return;
             }
 
             double amount = calculateEnergonRequestAmount(min.level, min.reserve, min.max);
             if(amount < 2) {
                 return;
             }
             amount = amount / 2;
 
             transferEnergon(amount, min.location, false);
         }
     }
 
     /**
      * Calculates the amount of energon needed to fill this robot so that neither the
      * energon reserve nor energon level overflows.  Returns -1 if less than 1 energon
      * is needed.
      */
     public int calculateEnergonRequestAmount(double currentLevel, double currentReserve, double maxLevel) {
         double maxReserve = GameConstants.ENERGON_RESERVE_SIZE;
         double eventualLevel = currentLevel + currentReserve;
 
         if(currentReserve >= maxReserve - 1) {
             return -1;
         }
 
         if(eventualLevel >= maxLevel - 1) {
             return -1;
         }
 
         double transferAmount = maxReserve - currentReserve;
 
         eventualLevel += transferAmount;
         if(eventualLevel >= maxLevel) {
             transferAmount -= (eventualLevel - maxLevel);
         }
 
         int requestAmount = (int) Math.round(transferAmount);
         if(requestAmount <= 1) {
             return -1;
         }
 
         return requestAmount;
     }
 
     public int calculateEnergonRequestAmount() {
         return calculateEnergonRequestAmount(controller.getEnergonLevel(), controller.getEnergonReserve(), controller.getMaxEnergonLevel());
     }
 
     public int calculateEnergonRequestAmount(RobotInfo info) {
         return calculateEnergonRequestAmount(info.energonLevel, info.energonReserve, info.maxEnergon);
     }
 
     /**
      * Returns true if the energon level is 90% of max
      */
     public boolean isEnergonFull() {
         return controller.getEnergonLevel() + controller.getEnergonReserve() > controller.getRobotType().maxEnergon() * .9;
     }
 
     /**
      * Returns true if the flux level is > 300
      */
     public boolean isFluxFull() {
         return controller.getFlux() > 300;
     }
 
     /**
      * Returns true if the energon level plus energon reserve is less than the starting energon level
      */
     public boolean isEnergonLow() {
         double currentLevel = controller.getEnergonLevel(), currentReserve = controller.getEnergonReserve();
         return (currentReserve == 0 && currentLevel < lowEnergonLevel);
     }
 
     public boolean isEnergonLow(RobotInfo info) {
         double currentLevel = info.energonLevel, currentReserve = info.energonReserve;
         return (currentReserve == 0 && currentLevel < info.maxEnergon * .3);
     }
 
     /**
      * Processes all of the energon requests in the requests ArrayList.
      * If multiple requests were asked for, the method will only give each robot a percentage
      * of the requested energon so the archon is not killed.
      */
     public void processEnergonTransferRequests() {
         double amount = controller.getEnergonLevel();
         if(amount < 5) return;
 
         if(player.isArchon) {
             autoTransferEnergon();
         }
 
         //p("processing requests");
         if(requests.size() > 0) {
             //p("multiple requests");
             double sum = 0;
             for(EnergonTransferRequest request : requests) {
                 sum += request.amount;
             }
 
             double percent = 1;
             if(amount - 5 < sum) {
                 percent = (sum) / (amount - 5);
             }
 
             //p("sum: "+sum+" percent: "+percent+" amount: "+amount);
             for(EnergonTransferRequest request : requests) {
                 int result = transferEnergon(request.amount * percent, request.location, request.isAirUnit);
             }
 
             requests.clear();
         }
     }
 
     /**
      * Performs all the steps to request an energon transfer.
      * First, it calculates an amount of energon to request, enough so that the neither
      * the reserve not level overflows.
      *
      * The robot then attempts to move adjacent to the closest archon.  However, if the
      * archon moves while the robot is moving, the robot will try 2 more times to get adjacent
      * to the robot.
      */
     public int requestEnergonTransfer() {
         int amount = calculateEnergonRequestAmount();
         if(amount == -1) {
             return Status.success;
         }
 
         int tries = 3;
         navigation.changeToArchonGoal(false);
         do {
             navigation.moveOnce(true);
             tries--;
         } while(tries > 0 && !navigation.goal.done());
 
         MapLocation closest = sensing.senseClosestArchon();
         if(closest == null || closest.distanceSquaredTo(controller.getLocation()) > 2) {
             return Status.fail;
         }
 
         messaging.sendLowEnergon(closest, calculateEnergonRequestAmount());
         controller.yield();
 
         return Status.success;
     }
 
     /**
      * Transfers the specified amount of energon to the robot as long as the robot is adjacent to the
      * archon, and the amount of energon requested will not reduce the archon's energon level to
      * just 1 energon.
      */
     public int transferEnergon(double amount, MapLocation location, boolean isAirUnit) {
         if(controller.getEnergonLevel() - 1 < amount) {
             return Status.notEnoughEnergon;
         }
 
         if(amount < 0) {
             return Status.success;
         }
 
         //System.out.println("in transfer");
         /*if(!location.isAdjacentTo(controller.getLocation()))
         return Status.fail;*/
 
         RobotLevel level = isAirUnit ? RobotLevel.IN_AIR : RobotLevel.ON_GROUND;
         try {
             if(controller.canSenseSquare(location) && ((isAirUnit && controller.senseAirRobotAtLocation(location) == null) ||
                     (!isAirUnit && controller.senseGroundRobotAtLocation(location) == null))) {
                 return Status.fail;
             }
             controller.transferUnitEnergon(amount, location, level);
         } catch(Exception e) {
             System.out.println("----Caught Exception in transferEnergon. amount: "+amount+
                     " location: "+location.toString()+" isAirUnit: "+isAirUnit+" level: "+
                     level.toString()+" Exception: "+e.toString());
             return Status.fail;
         }
         return Status.success;
     }
     
     /**
      * Represents a pending energon request.
      */
     class EnergonTransferRequest {
 
         public MapLocation location, archonLocation;
         public boolean isAirUnit;
         public double amount;
 
         public EnergonTransferRequest(MapLocation location, boolean isAirUnit, int amount) {
             this.location = location;
             this.isAirUnit = isAirUnit;
             this.amount = amount;
         }
 
         public EnergonTransferRequest(int amount, boolean isAirUnit) {
             this.isAirUnit = isAirUnit;
             this.amount = amount;
         }
 
         public String toString() {
             return "Location: " + location.toString() + " isAirUnit: " + isAirUnit + " Amount: " + amount;
         }
     }
 
     class LowAllyRequest {
 
         public MapLocation location;
         public int level, reserve, max;
 
         public LowAllyRequest(MapLocation location, int level, int reserve, int max) {
             this.location = location;
             this.level = level;
             this.reserve = reserve;
             this.max = max;
         }
     }
 }
