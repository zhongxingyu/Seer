 package edu.msoe.se2800.h4.jplot;
 
 import com.google.common.eventbus.EventBus;
 import edu.msoe.se2800.h4.FileIO;
 import edu.msoe.se2800.h4.IRobotController;
 import edu.msoe.se2800.h4.Logger;
 import edu.msoe.se2800.h4.StatsTimerDaemon;
 import edu.msoe.se2800.h4.administrationFeatures.DatabaseConnection;
 import edu.msoe.se2800.h4.administrationFeatures.LoginUI;
 import edu.msoe.se2800.h4.administrationFeatures.ResultInfo;
 import edu.msoe.se2800.h4.administrationFeatures.UserListController;
 import edu.msoe.se2800.h4.jplot.grid.Grid;
 import edu.msoe.se2800.h4.jplot.grid.GridInterface;
 import lejos.robotics.navigation.Waypoint;
 import lejos.robotics.pathfinding.Path;
 
 import javax.inject.Singleton;
 import javax.swing.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 @Singleton
 public class JPlotController {
 
     private static JPlotController instance = null;
 
     private int gridDensity = Constants.DEFAULT_GRID_DENSITY;
 
     private JPlotInterface jplot;
     private GridInterface grid;
     private List<Waypoint> oldList;
     private Waypoint highlightedPoint;
     private boolean closingForModeChange = false;
     private EventBus mEventBus;
     protected IRobotController robotController;
     private String currentUser = "";
     LoginUI login;
     private String username;
 
     public static JPlotController getInstance() {
         if (instance == null) {
             synchronized (JPlotController.class) {
                 if (instance == null) {
                     instance = new JPlotController();
                 }
             }
         }
         return instance;
     }
 
     public JPlotController() {
         oldList = new ArrayList<Waypoint>();
         instance = this;
     }
 
     public void init() {
         grid = new Grid();
         jplot = new JPlot(DatabaseConnection.UserTypes.OBSERVER, grid);
         jplot.initSubviews();
         jplot.getFrame().addWindowListener(new JPlotWindowListener());
         StatsTimerDaemon.start();
     }
 
     public GridInterface getGrid() {
         return grid;
     }
 
     public void changeMode(DatabaseConnection.UserTypes accessLevel) {
         DatabaseConnection.UserTypes mode = accessLevel;
         grid = new Grid();
         if (Constants.CURRENT_MODE == DatabaseConnection.UserTypes.OTHER) {
             robotController.setPath(new Path());
 
             for (Waypoint p : oldList) {
                 robotController.addWaypoint(p);
             }
         }
         Constants.CURRENT_MODE = mode;
         if (mode == DatabaseConnection.UserTypes.OTHER) {
             copyPoints();
             robotController.setPath(new Path());
         }
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 closingForModeChange = true;
                 jplot.getFrame().dispose();
                 jplot = new JPlot(Constants.CURRENT_MODE, grid);
                 if (Constants.CURRENT_MODE != DatabaseConnection.UserTypes.OBSERVER) {
                     jplot = new JPlotProgrammerDecorator(jplot);
                     if (Constants.CURRENT_MODE == DatabaseConnection.UserTypes.ADMIN) {
                         jplot = new JPlotAdminDecorator(jplot);
                     }
                 }
                 jplot.initSubviews();
                 closingForModeChange = false;
                 jplot.getFrame().addWindowListener(new JPlotWindowListener());
             }
         });
     }
 
     public Path getPath() {
         return robotController.getPath();
     }
 
     public Waypoint[] getPathPoints() {
         Waypoint[] points = new Waypoint[robotController.getPath().size()];
         robotController.getPath().toArray(points);
         return points;
     }
 
     public void addPoint(Waypoint point) {
         if (Constants.CURRENT_MODE != DatabaseConnection.UserTypes.OTHER) {
             robotController.addWaypoint(point);
 
         }else{
         	robotController.addWaypoint(point);
             robotController.goToImmediate(point);
         }
         if (jplot != null) {
             jplot.getFrame().repaint();
         }
     }
 
     public void removePoint(int indexOfPoint) {
         robotController.getPath().remove(indexOfPoint);
         jplot.getFrame().repaint();
     }
 
     public void copyPoints() {
         oldList.clear();
         for (Waypoint j : robotController.getPath()) {
             oldList.add(j);
         }
         robotController.setPath(new Path());
         grid.redraw();
     }
 
     public void zoomIn() {
         setGridDensity(getGridDensity() - 1);
         grid.redraw();
     }
 
     public void zoomOut() {
         setGridDensity(getGridDensity() + 1);
         grid.redraw();
     }
 
     public int getGridDensity() {
         return gridDensity;
     }
 
     public void setGridDensity(int density) {
         if (density > 1) {
             gridDensity = Math.min(density, 100);
             grid.redraw();
         } else {
             gridDensity = 1;
             grid.redraw();
         }
     }
 
     public Waypoint getHighlightedPoint() {
         return this.highlightedPoint;
     }
 
     public void setHighlightedPoint(int indexInPointsArray) {
         if (indexInPointsArray == -5) {
             this.highlightedPoint = null;
         } else {
             this.highlightedPoint = JPlotController.getInstance().getPath().get(indexInPointsArray);
         }
     }
 
     public ResultInfo createUser(String username, String password, DatabaseConnection.UserTypes role) {
         try {
             return DatabaseConnection.getInstance().addUser(username, password, role);
         } catch (IOException e) {
             return new ResultInfo("Error contacting the database.", false);
         }
     }
 
     public void listUsers() {
         new UserListController();
     }
 
 
     private class JPlotWindowListener extends WindowAdapter {
         @Override
         public void windowClosing(WindowEvent e) {
             //if closing because of mode change, do not want to log out
             if (!closingForModeChange) {
                 JPlotController.this.logOut();
             } else {
                 jplot.getFrame().dispose();
             }
         }
     }
 
     /**
      * launches the login screen
      *
      * @param rc the robotController to be used in this run
      */
     public void start(IRobotController rc) {
         checkNotNull(rc);
 
         this.robotController = rc;
         JFrame dummyFrame = new JFrame();
         dummyFrame.setUndecorated(true);
         dummyFrame.setVisible(true);
         login = new LoginUI(dummyFrame);
         dummyFrame.dispose();
         if (login.wasLoginSuccessful()) {
             Logger.INSTANCE.log(this.getClass().getSimpleName(),
                     "Logged in as: " + DatabaseConnection.getInstance().getLastSuccessfullyValidatedUser());
             this.currentUser = DatabaseConnection.getInstance().getLastSuccessfullyValidatedUser();
             this.init();
             try {
                 this.changeMode(DatabaseConnection.getInstance().getUserRole(login.getUsername()));
                 username = login.getUsername();
                 System.out.println(username);
             } catch (IOException e) {
                 System.out.println("Unable to retrieve user role and set grid mode");
             }
         } else {
             //log in cancelled. stop program execution
             System.exit(0);
         }
     }
 
     /**
      * logs out. makes sure robot is not running. Asks to save current path before logging out. relaunches the login screen.
      */
     public void logOut() {
         if (jplot == null) {
             throw new NullPointerException("Tried to log out when jplot was null");
         }
 
         if (!this.robotController.isMoving()) {
             try {
                 if (DatabaseConnection.getInstance().getUserRole(this.currentUser) != DatabaseConnection.UserTypes.OBSERVER) {
                     if (FileIO.getCurrentPathFile() != null || !JPlotController.this.getPath().isEmpty()) {
                         int result = JOptionPane
                                 .showConfirmDialog(null, "Do you wish to save your current Path?", "Save...?",
                                         JOptionPane.YES_NO_OPTION);
                         if (result == JOptionPane.YES_OPTION) {
                             FileIO.save();
                         }
                     }
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
         } else {
             JOptionPane.showMessageDialog(null, "Robot is running. Cannot log out.");
         }
 
 
        this.jplot.getFrame().dispose();
        //this.currentUser = "";
        Logger.INSTANCE.log(this.getClass().getSimpleName(),
                "Logged out of: " + DatabaseConnection.getInstance().getLastSuccessfullyValidatedUser());
     }
 
     public EventBus getEventBus() {
         if (mEventBus == null) {
             synchronized (this) {
                 mEventBus = new EventBus();
             }
         }
         return mEventBus;
     }
 
     public IRobotController getRobotController() {
         return robotController;
     }
 
 }
