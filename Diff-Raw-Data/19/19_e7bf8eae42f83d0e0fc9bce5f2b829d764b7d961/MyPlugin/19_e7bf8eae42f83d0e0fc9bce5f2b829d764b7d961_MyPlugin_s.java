 package polly.rx;
 
 import java.util.Set;
 import java.util.TreeSet;
 
 import polly.rx.commands.AddTrainCommand;
 import polly.rx.commands.CloseTrainCommand;
 import polly.rx.commands.CrackerCommand;
 import polly.rx.commands.DeliverTrainCommand;
 import polly.rx.commands.IPCommand;
 import polly.rx.commands.MyTrainsCommand;
 import polly.rx.commands.MyVenadCommand;
 import polly.rx.commands.VenadCommand;
 import polly.rx.core.FleetDBManager;
 import polly.rx.core.ScoreBoardManager;
 import polly.rx.core.TrainManagerV2;
 import polly.rx.entities.BattleReport;
 import polly.rx.entities.BattleReportShip;
 import polly.rx.entities.BattleDrop;
 import polly.rx.entities.FleetScan;
 import polly.rx.entities.FleetScanHistoryEntry;
 import polly.rx.entities.FleetScanShip;
 import polly.rx.entities.ScoreBoardEntry;
 import polly.rx.entities.TrainEntity;
 import polly.rx.entities.TrainEntityV2;
 import polly.rx.http.BattleReportHttpAction;
 import polly.rx.http.BattleReportInfosHttpAction;
 import polly.rx.http.BattleReportFilterHttpAction;
 import polly.rx.http.FleetScanInfoHttpAction;
 import polly.rx.http.FleetShipInfoHttpAction;
 import polly.rx.http.FleetScanHttpAction;
 import polly.rx.http.MyTrainsHttpAction;
 import polly.rx.http.QueryOwnerHttpAction;
 import polly.rx.http.ScoreBoardCompareHttpAction;
import polly.rx.http.ScoreboardHttpAction;
import polly.rx.http.ScoreboardDetailsHttpAction;
 import polly.rx.http.TrainerHttpAction;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.PollyPlugin;
 import de.skuzzle.polly.sdk.constraints.Constraints;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.DisposingException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.exceptions.IncompatiblePluginException;
 import de.skuzzle.polly.sdk.exceptions.PluginException;
 import de.skuzzle.polly.sdk.exceptions.RoleException;
 import de.skuzzle.polly.sdk.roles.RoleManager;
 
 
 public class MyPlugin extends PollyPlugin {
 
     public final static String FLEET_MANAGER_ROLE = "polly.roles.FLEET_MANAGER";
     public final static String TRAINER_ROLE       = "polly.roles.TRAINER";
     public final static String SBE_MANAGER_ROLE   = "polly.roles.SBE_MANAGER";
     
     public final static String ADD_TRAIN_PERMISSION              = "polly.permission.ADD_TRAIN";
     public final static String DELIVER_TRAIN_PERMISSION          = "polly.permission.DELIVER_TRAIN";
     public final static String MYTRAINS_PERMISSION               = "polly.permission.MY_TRAINS";
     public final static String MY_VENAD_PERMISSION               = "polly.permission.MY_VENAD";
     public final static String CLOSE_TRAIN_PERMISSION            = "polly.permission.CLOSE_TRAIN";
     public final static String IP_PERMISSION                     = "polly.permission.IP";
     public final static String SBE_PERMISSION                    = "polly.permission.SBE";
     public final static String CRACKER_PERMISSION                = "polly.permission.CRACKER";
     public final static String VENAD    = "VENAD";
     public final static String CRACKER  = "CRACKER";
     
     private FleetDBManager fleetDBManager;
     private TrainManagerV2 trainManager;
     private DailyGreeter dailyGreeter;
     private ScoreBoardManager sbeManager;
     
     
     public MyPlugin(MyPolly myPolly) 
                 throws DuplicatedSignatureException, IncompatiblePluginException {
         super(myPolly);
         
         this.dailyGreeter = new DailyGreeter();
         this.dailyGreeter.deploy(myPolly.irc());
         
         
         /* capi train related */
         this.trainManager = new TrainManagerV2(myPolly);
         this.getMyPolly().persistence().registerEntity(TrainEntity.class);
         this.getMyPolly().persistence().registerEntity(TrainEntityV2.class);
         
         this.addCommand(new AddTrainCommand(myPolly, this.trainManager));
         this.addCommand(new CloseTrainCommand(myPolly, this.trainManager));
         this.addCommand(new MyTrainsCommand(myPolly, this.trainManager));
         this.addCommand(new DeliverTrainCommand(myPolly, this.trainManager));
         this.addCommand(new VenadCommand(myPolly));
         this.addCommand(new MyVenadCommand(myPolly));
         this.addCommand(new IPCommand(myPolly));
         this.addCommand(new CrackerCommand(myPolly));
         
         myPolly.web().addMenuUrl("Revorix", "MyTrains");
         myPolly.web().addMenuUrl("Revorix", "Trainer");
         
         myPolly.web().addHttpAction(new MyTrainsHttpAction(myPolly, this.trainManager));
         myPolly.web().addHttpAction(new TrainerHttpAction(myPolly, this.trainManager));
         
         /* fleet db related */
         
         
         this.fleetDBManager = new FleetDBManager(myPolly.persistence());
         
         myPolly.persistence().registerEntity(BattleReport.class);
         myPolly.persistence().registerEntity(BattleReportShip.class);
         myPolly.persistence().registerEntity(BattleDrop.class);
         myPolly.persistence().registerEntity(FleetScan.class);
         myPolly.persistence().registerEntity(FleetScanHistoryEntry.class);
         myPolly.persistence().registerEntity(FleetScanShip.class);
         
         myPolly.web().addHttpAction(new BattleReportInfosHttpAction(
             myPolly, this.fleetDBManager));
         myPolly.web().addHttpAction(new BattleReportHttpAction(
             myPolly, this.fleetDBManager));
         myPolly.web().addHttpAction(new BattleReportFilterHttpAction(
             myPolly, this.fleetDBManager));
         myPolly.web().addMenuUrl("Revorix", "Kampfberichte");
         
         
         myPolly.web().addMenuUrl("Revorix", "FleetScans");
         myPolly.web().addHttpAction(new FleetScanHttpAction(myPolly, this.fleetDBManager));
         myPolly.web().addHttpAction(new FleetShipInfoHttpAction(myPolly, this.fleetDBManager));
         myPolly.web().addHttpAction(new FleetScanInfoHttpAction(myPolly, this.fleetDBManager));
         myPolly.web().addHttpAction(new QueryOwnerHttpAction(myPolly, this.fleetDBManager));
         
         myPolly.persistence().registerEntity(ScoreBoardEntry.class);
         this.sbeManager = new ScoreBoardManager(myPolly.persistence());
         myPolly.web().addMenuUrl("Revorix", "Scoreboard");
        myPolly.web().addHttpAction(new ScoreboardHttpAction(myPolly, this.sbeManager));
        myPolly.web().addHttpAction(new ScoreboardDetailsHttpAction(myPolly, this.sbeManager));
         myPolly.web().addHttpAction(new ScoreBoardCompareHttpAction(myPolly, this.sbeManager));
     }
     
     
     
     @Override
     protected void actualDispose() throws DisposingException {
         super.actualDispose();
         this.dailyGreeter.undeploy(this.getMyPolly().irc());
     }
     
     
     
     @Override
     public Set<String> getContainedPermissions() {
         Set<String> result = new TreeSet<String>();
         result.add(FleetDBManager.ADD_FLEET_SCAN_PERMISSION);
         result.add(FleetDBManager.ADD_BATTLE_REPORT_PERMISSION);
         result.add(FleetDBManager.DELETE_BATTLE_REPORT_PERMISSION);
         result.add(FleetDBManager.VIEW_BATTLE_REPORT_PERMISSION);
         result.add(FleetDBManager.VIEW_FLEET_SCAN_PERMISSION);
         result.add(FleetDBManager.DELETE_FLEET_SCAN_PERMISSION);
         result.add(SBE_PERMISSION);
         result.add(CRACKER_PERMISSION);
         result.addAll(super.getContainedPermissions());
         return result;
     }
     
     
     
     @Override
     public void assignPermissions(RoleManager roleManager)
             throws RoleException, DatabaseException {
         
         roleManager.createRole(TRAINER_ROLE);
         roleManager.assignPermission(TRAINER_ROLE, ADD_TRAIN_PERMISSION);
         roleManager.assignPermission(TRAINER_ROLE, CLOSE_TRAIN_PERMISSION);
         roleManager.assignPermission(TRAINER_ROLE, DELIVER_TRAIN_PERMISSION);
         
         roleManager.assignPermission(RoleManager.DEFAULT_ROLE, MYTRAINS_PERMISSION);
         
         roleManager.createRole(FLEET_MANAGER_ROLE);
         roleManager.assignPermission(FLEET_MANAGER_ROLE, FleetDBManager.ADD_FLEET_SCAN_PERMISSION);
         roleManager.assignPermission(FLEET_MANAGER_ROLE, FleetDBManager.ADD_BATTLE_REPORT_PERMISSION);
         roleManager.assignPermission(FLEET_MANAGER_ROLE, FleetDBManager.VIEW_BATTLE_REPORT_PERMISSION);
         roleManager.assignPermission(FLEET_MANAGER_ROLE, FleetDBManager.VIEW_FLEET_SCAN_PERMISSION);
         
         roleManager.createRole(SBE_MANAGER_ROLE);
         roleManager.assignPermission(SBE_MANAGER_ROLE, SBE_PERMISSION);
         
         roleManager.assignPermission(RoleManager.DEFAULT_ROLE, IP_PERMISSION);
         roleManager.assignPermission(RoleManager.DEFAULT_ROLE, CRACKER_PERMISSION);
         super.assignPermissions(roleManager);
     }
 
     
     
     @Override
     public void onLoad() throws PluginException {
         try {
             this.getMyPolly().users().addAttribute(VENAD, "<unbekannt>");
             this.getMyPolly().users().addAttribute("AZ", "0", Constraints.INTEGER);
             this.getMyPolly().users().addAttribute(CRACKER, "0", Constraints.INTEGER);
         } catch (Exception ignore) {
             ignore.printStackTrace();
         }
         
         try {
             this.fleetDBManager.cleanInvalidBattleReports();
         } catch (DatabaseException e) {
             e.printStackTrace();
         }
     }
 }
