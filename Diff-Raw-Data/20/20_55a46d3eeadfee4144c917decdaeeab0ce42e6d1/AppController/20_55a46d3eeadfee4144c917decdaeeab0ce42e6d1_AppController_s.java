 package src.nonui;
 
 import java.awt.FlowLayout;
 import java.awt.MouseInfo;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import src.ui.ImagePanelView;
 import src.ui.LabelPanelView;
 import src.ui.MenuBarView;
 import src.ui.ToolboxPanelView;
 import src.utils.LabelIO;
 import src.utils.LabelIO.LabelParseException;
 import src.utils.Point;
 import src.utils.Polygon;
 
 /**
  * Main controller class for the application.
  */
 public class AppController {
     // The main folder for the application.
     private static final String MAIN_FOLDER = System.getProperty("user.home") + "/ImageLabeller";
 
     // The farthest distance, in pixels, that the user can click from a point
     // and still 'select' it.
     private static final double EDITING_THRESHOLD_DISTANCE = 5.0;
 
     // The core Swing components that make up the application.
     private final JFrame appFrame = new JFrame("Image Labeller");
     private final MenuBarView menuBar = new MenuBarView(appFrame, this);
     private final ImagePanelView imagePanel = new ImagePanelView(this);
     private final LabelPanelView labelPanel = new LabelPanelView(appFrame, this);
     private final ToolboxPanelView toolboxPanel = new ToolboxPanelView(appFrame, this);
 
     Polygon currentPolygon = new Polygon();
     Polygon editedPolygon = new Polygon();
     private Map<String, Polygon> completedPolygons = new HashMap<String, Polygon>();
     private Point currentPoint = null; // Used when moving points.
 
     // The application state.
     private ApplicationState applicationState = ApplicationState.DEFAULT;
 
     // The current project.
     private String currentProjectName;
     private String currentImageName;
 
     public AppController() {
         appFrame.setLayout(new FlowLayout());
         appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         appFrame.add(imagePanel);
         appFrame.add(labelPanel);
         appFrame.setJMenuBar(menuBar);
 
         appFrame.pack();
         appFrame.setVisible(true);
         appFrame.setResizable(false);
 
         loadSettingsFile();
     }
 
     /**
      * Called when the image is clicked on.
      * 
      * @param x the x-coordinate of the mouse click
      * @param y the y-coordinate of the mouse click
      * @param doubleClick whether or not the user is double clicking
      */
     public void imageMouseClick(int x, int y, boolean doubleClick) {
         switch (applicationState) {
             case DEFAULT:
                 if (doubleClick) {
                     // Double clicking does nothing in the default state.
                     return;
                 }
                 imagePanel.repaint();
                 // TODO: Enter explicit editing at this point.
 
                 break;
             case ADDING_POLYGON:
                 if (doubleClick) {
                     finishedAddingPolygon();
                 } else {
                     currentPolygon.addPoint(new Point(x, y));
                     imagePanel.repaint();
                 }
                 break;
             case EDITING_POLYGON:
                 // TODO: Implement explicit editing logic.
                 break;
             default:
                 // TODO: Throw/show appropriate error.
         }
     }
 
     /**
      * Called when the mouse is dragged over the image.
      * 
      * @param x the x coordinate that the mouse is now at
      * @param y the y coordinate that the mouse is now at
      */
     public void imageMouseDrag(int x, int y) {
         switch (applicationState) {
             case DEFAULT:
                 // Do nothing
 
                 break;
             case ADDING_POLYGON:
                 // Do nothing.
                 break;
             case EDITING_POLYGON:
                 if (currentPoint != null) {
                     Point newPoint = new Point(x, y);
                     if (currentPolygon.replacePoint(currentPoint, newPoint)) {
                         currentPoint = newPoint;
                     }
                     editedPolygon = currentPolygon;
                     imagePanel.repaint();
                 }
                 break;
             default:
                 // TODO: Throw/show appropriate error.
         }
     }
 
     /**
      * Called when the mouse is pressed over the image (but not released
      * immediately, which generates a click instead).
      * 
      * @param x the x coordinate where the mouse was pressed
      * @param y the y coordinate where the mouse was pressed
      */
     public void imageMousePress(int x, int y) {
         switch (applicationState) {
             case DEFAULT:
                 selectClosestPoint(x, y);
                 break;
             case ADDING_POLYGON:
                 // Do nothing.
                 break;
             case EDITING_POLYGON:
                 // Implement explicit editing logic.
                 selectClosestPoint(x, y);
             default:
                 // TODO: Throw/show appropriate error.
         }
     }
 
     /**
      * Called when the mouse is released (from a press/drag, not a click) over
      * the image.
      */
     public void imageMouseReleased() {
         switch (applicationState) {
             case DEFAULT:
                 currentPoint = null;
                 currentPolygon = new Polygon();
                 imagePanel.repaint();
                 break;
             case ADDING_POLYGON:
                 // Do nothing.
                 break;
             case EDITING_POLYGON:
                 // Implement logic for explicit editing.
                 currentPoint = null;
                 currentPolygon = new Polygon();
                 break;
             default:
                 // TODO: Throw/show appropriate error.
         }
     }
 
     /**
      * Returns a list of the points of each completed polygon.
      */
     public List<List<Point>> getCompletedPolygonsPoints() {
         List<List<Point>> points = new ArrayList<List<Point>>(completedPolygons.size());
         for (Polygon polygon : completedPolygons.values()) {
             points.add(new ArrayList<Point>(polygon.getPoints()));
         }
         return points;
     }
 
     /**
      * Returns a list of the points in the current Polygon.
      */
     public List<Point> getCurrentPolygonPoints() {
         return currentPolygon.getPoints();
     }
 
     /**
      * Starts adding a new polygon.
      */
     public void startAddingNewPolygon() {
         applicationState = ApplicationState.ADDING_POLYGON;
 
         labelPanel.setAddButtonEnabled(false);
 
         java.awt.Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
         mouseLocation.setLocation(mouseLocation.getX(), mouseLocation.getY() + 20);
         toolboxPanel.setLocation(mouseLocation);
         toolboxPanel.setVisible(true);
     }
 
     /**
      * Renames a polygon.
      * 
      * @param oldName the old name for the polygon
      * @param newName the new name for the polygon
      */
     public void renamePolygon(String oldName, String newName) {
         Polygon polygon = completedPolygons.remove(oldName);
         if (polygon != null) {
             polygon.setName(newName);
             completedPolygons.put(newName, polygon);
         }
     }
 
     /**
      * Removes a polygon from the image.
      * 
      * @param name the name of the polygon to remove
      */
     public void removePolygon(String name) {
         Polygon removedPolygon = completedPolygons.remove(name);
         if (removedPolygon == editedPolygon) {
             applicationState = ApplicationState.DEFAULT;
         }
         imagePanel.repaint();
     }
 
     /**
      * Saves the current list of polygons to a file.
      * 
      * @param file the file to save to
      */
     public void saveLabels(File file) {
         try {
             LabelIO.writeLabels(file, new ArrayList<Polygon>(completedPolygons.values()));
         } catch (IOException e) {
             JOptionPane.showMessageDialog(appFrame, e.getMessage(), "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
     }
 
     /**
      * Loads in a new list of polygons from a file.
      * 
      * @param file the file to load from
      */
     public void loadLabels() {
 
         JFileChooser chooser = new JFileChooser();
         int returnValue = chooser.showOpenDialog(appFrame);
         if (returnValue == JFileChooser.APPROVE_OPTION) {
             File loadFile = chooser.getSelectedFile();
 
             try {
                 completedPolygons = LabelIO.readLabels(loadFile);
                 labelPanel.clear();
                 for (String name : completedPolygons.keySet()) {
                     labelPanel.addLabel(name);
                 }
                 imagePanel.repaint();
             } catch (LabelParseException e) {
                 JOptionPane.showMessageDialog(appFrame, e.getMessage(), "Error",
                         JOptionPane.ERROR_MESSAGE);
             }
         }
     }
 
     /**
      * Opens the image from a file.
      * 
      * @param file the file to open the image from
      */
     public void openImage(File file) {
         try {
             BufferedImage image = ImageIO.read(file);
 
             cancelAddingPolygon();
             completedPolygons.clear();
 
             imagePanel.setImage(image);
             labelPanel.clear();
         } catch (IOException e) {
             JOptionPane.showMessageDialog(appFrame, "Unable to open image.", "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
     }
 
     /**
      * Called when the "Done" button on the toolbox is clicked.
      */
     public void toolboxDoneButtonClicked() {
         switch (applicationState) {
             case DEFAULT:
                 // TODO: Throw/show appropriate error, as this shouldn't happen.
                 break;
             case ADDING_POLYGON:
                 finishedAddingPolygon();
                 break;
             case EDITING_POLYGON:
                 // TODO: Implement explicit editing of polygons.
                 break;
             default:
                 // TODO: Throw/show appropriate error.
         }
     }
 
     // TODO: Rename to "undo", and extend to cover states other than
     // ADDING_POLYGON.
     /**
      * Called when the user wishes to undo their last move when editing a
      * polygon.
      */
     public void undoLastVertex() {
         currentPolygon.removeLastPoint();
         imagePanel.repaint();
     }
 
     // TODO: Rename to "redo", and extend to cover states other than
     // ADDING_POLYGON.
     /**
      * Called when the user wishes to redo their last move when editing a
      * polygon.
      */
     public void redoLastVertex() {
         currentPolygon.redoPoint();
         imagePanel.repaint();
     }
 
     /**
      * Called when the "Cancel" button on the toolbar is clicked.
      */
     public void toolboxCancelButtonClicked() {
         switch (applicationState) {
             case DEFAULT:
                 // TODO: Throw/show appropriate error, as this shouldn't happen.
                 break;
             case ADDING_POLYGON:
                 cancelAddingPolygon();
                 break;
             case EDITING_POLYGON:
                 // TODO: Implement explicit editing of polygons.
                 break;
             default:
                 // TODO: Throw/show appropriate error.
         }
     }
 
     /**
      * Called when the toolbox window is closed.
      */
     public void toolboxWindowClosed() {
         switch (applicationState) {
             case DEFAULT:
                 // TODO: Throw/show appropriate error, as this shouldn't happen.
                 break;
             case ADDING_POLYGON:
                 cancelAddingPolygon();
                 break;
             case EDITING_POLYGON:
                 // TODO: Implement explicit editing of polygons.
                 break;
             default:
                 // TODO: Throw/show appropriate error.
         }
     }
 
     /**
      * Deletes all of the polygons.
      */
     public void deleteAllPolygons() {
         labelPanel.deleteAllPolygons();
         imagePanel.repaint();
     }
 
     /**
      * Deletes the currently selected polygons.
      */
     public void deleteSelectedPolygons() {
         labelPanel.deleteSelectedPolygons();
         imagePanel.repaint();
     }
 
     /**
      * Called when the user is finished adding the current polygon, either by
      * clicking on the starting point, double-clicking, or clicking the "Done"
      * button on the toolbox.
      */
     private void finishedAddingPolygon() {
         if (applicationState != ApplicationState.ADDING_POLYGON) {
             return;
         }
 
         if (currentPolygon.getPoints().size() < 3) {
             JOptionPane.showMessageDialog(appFrame, "A polygon must have 3 or more vertices.",
                     "Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         applicationState = ApplicationState.DEFAULT;
         labelPanel.setAddButtonEnabled(true);
         toolboxPanel.setVisible(false);
 
         String name = null;
         boolean hasName = false;
         while (!hasName) {
             String message = "Label Name";
             name = JOptionPane.showInputDialog(appFrame, message);
 
             // TODO: Should this totally cancel, or only cancel the "done"?
             // Occurs if the user hits the cancel option.
             if (name == null) {
                 currentPolygon = new Polygon();
                 imagePanel.repaint();
 
                 return;
             }
 
             name = name.trim();
             if (completedPolygons.containsKey(name)) {
                 JOptionPane.showMessageDialog(appFrame, "That name is already in use.", "Error",
                         JOptionPane.ERROR_MESSAGE);
             } else if (name.isEmpty()) {
                 JOptionPane.showMessageDialog(appFrame, "Blank names are not allowed.", "Error",
                         JOptionPane.ERROR_MESSAGE);
             } else {
                 hasName = true;
             }
             // imagePanel.setImage(null);
 
         }
 
         currentPolygon.setName(name);
         completedPolygons.put(name, currentPolygon);
         currentPolygon = new Polygon();
 
         labelPanel.addLabel(name);
 
         imagePanel.repaint();
     }
 
     /**
      * Called when the user cancels the polygon that they are currently adding.
      */
     private void cancelAddingPolygon() {
         applicationState = ApplicationState.DEFAULT;
         currentPolygon = new Polygon();
 
         labelPanel.setAddButtonEnabled(true);
         toolboxPanel.setVisible(false);
 
         imagePanel.repaint();
     }
 
     /**
      * Selects the closest point to a given target point.
      * 
      * @param x the x coordinate of the target
      * @param y the y coordinate of the target
      */
     private void selectClosestPoint(int x, int y) {
         Point targetPoint = new Point(x, y);
         Point closestPoint = null;
         Polygon closestPolygon = null;
 
         double smallestDistance = -1;
 
         for (Polygon polygon : completedPolygons.values()) {
             for (Point point : polygon.getPoints()) {
                 double distanceToTarget = targetPoint.distanceFrom(point);
 
                 if (distanceToTarget < smallestDistance || smallestDistance < 0) {
                     if (isSelected(polygon.getName())) {
                         smallestDistance = distanceToTarget;
                         closestPoint = point;
                         closestPolygon = polygon;
                     }
                 }
 
             }
         }
 
         if (smallestDistance >= 0 && smallestDistance < EDITING_THRESHOLD_DISTANCE) {
             applicationState = ApplicationState.EDITING_POLYGON;
             currentPoint = closestPoint;
             currentPolygon = closestPolygon;
         } else {
             applicationState = ApplicationState.DEFAULT;
         }
     }
 
     public Polygon getPolygon(String name) {
         return completedPolygons.get(name);
     }
 
     public void closeImage() {
         // TODO: Disable some menu items.
         currentImageName = null;
 
         imagePanel.setImage(null);
         labelPanel.clear();
 
         labelPanel.setAddButtonEnabled(false);
         labelPanel.setLoadButtonEnabled(false);
     }
 
     public void highlightSelected(List<String> highlightedNames) {
         if (applicationState == ApplicationState.EDITING_POLYGON
                 && !highlightedNames.contains(editedPolygon.getName())) {
             applicationState = ApplicationState.DEFAULT;
         }
 
         imagePanel.repaint();
     }
 
     public List<List<Point>> getSelectedPolygonsPoints() {
         List<Polygon> selectedPolygons = getSelectedPolygons();
         List<List<Point>> points = new ArrayList<List<Point>>(selectedPolygons.size());
         for (Polygon selectedPolygon : selectedPolygons) {
             points.add(selectedPolygon.getPoints());
         }
         return points;
     }
 
     public List<Polygon> getSelectedPolygons() {
         List<String> selectedNames = labelPanel.getSelectedNames();
         List<Polygon> selectedPolygons = new ArrayList<Polygon>(selectedNames.size());
 
         for (String name : selectedNames) {
             selectedPolygons.add(completedPolygons.get(name));
         }
 
         return selectedPolygons;
     }
 
     private boolean isSelected(String name) {
         List<Polygon> polygons = getSelectedPolygons();
         for (Polygon selected : polygons) {
             if (selected.getName() == name) {
                 return true;
             }
         }
         return false;
     }
 
     public List<Point> getEditedPolygonPoints() {
         if (applicationState == ApplicationState.EDITING_POLYGON) {
             return editedPolygon.getPoints();
         }
 
         return null;
     }
 
     public void newProject() {
         // Prompt for project name.
         String newProjectName = null;
         boolean hasName = false;
 
         File projectsDir = new File(MAIN_FOLDER + "/Projects");
         if (!projectsDir.exists()) {
             // TODO: Error somehow.
             System.err.println("projects dir doesnt exist...");
             return;
         }
 
         FileFilter directoryFilter = new FileFilter() {
             @Override
             public boolean accept(File pathname) {
                 return pathname.isDirectory();
             }
         };
         File directories[] = projectsDir.listFiles(directoryFilter);
 
         while (!hasName) {
             String message = "Project Name";
             newProjectName = JOptionPane.showInputDialog(appFrame, message);
 
             hasName = true;
 
             // Occurs if the user hits the cancel option.
             if (newProjectName == null) {
                 return;
             }
 
             newProjectName = newProjectName.trim();
 
             if (newProjectName.isEmpty()) {
                 JOptionPane.showMessageDialog(appFrame, "Blank names are not allowed.", "Error",
                         JOptionPane.ERROR_MESSAGE);
                 hasName = false;
                 continue;
             }
 
             // Check for duplicates.
             for (int i = 0; i < directories.length; i++) {
                 if (newProjectName.equals(directories[i].getName())) {
                     // TODO: Give the option to overwrite.
                     JOptionPane.showMessageDialog(appFrame, "That name is already in use.",
                             "Error", JOptionPane.ERROR_MESSAGE);
                     hasName = false;
                     break;
                 }
             }
         }
 
         // Create folders for new project.
         File newProjectDir = new File(projectsDir.getAbsolutePath() + "/" + newProjectName);
         File imageFolder = new File(newProjectDir.getAbsolutePath() + "/images");
         File labelsFolder = new File(newProjectDir.getAbsolutePath() + "/labels");
         if (!newProjectDir.mkdir() || !imageFolder.mkdir() || !labelsFolder.mkdir()) {
             // TODO: Error
             System.err.println("Unable to create dir");
             return;
         }
 
         // Set the current project.
         currentProjectName = newProjectName;
 
         // Clear everything interface wise, etc.
         closeImage();
 
         // Update the .settings file.
         writeToSettingsFile(currentProjectName, "");
     }
 
     public void closeProject() {
         // TODO: Shouldnt be able to close if no project open.
         if (currentProjectName == null) {
             return;
         }
 
         // TODO: Confirm?
 
         currentProjectName = null;
         closeImage();
 
         // Update the .settings file.
         writeToSettingsFile("", "");
     }
 
     public void openProject() {
         // Ask user to choose project.
         File projectsDir = new File(MAIN_FOLDER + "/Projects");
         if (!projectsDir.exists()) {
             // TODO: Error somehow.
             System.err.println("projects dir doesnt exist...");
             return;
         }
 
         FileFilter directoryFilter = new FileFilter() {
             @Override
             public boolean accept(File pathname) {
                 return pathname.isDirectory();
             }
         };
         File projects[] = projectsDir.listFiles(directoryFilter);
         if (projects.length == 0) {
             JOptionPane.showMessageDialog(appFrame, "There are no projects. Please create one.",
                     "Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         String projectNames[] = new String[projects.length];
         for (int i = 0; i < projectNames.length; i++) {
             projectNames[i] = projects[i].getName();
         }
 
         String newProjectName = (String) JOptionPane.showInputDialog(appFrame,
                 "Choose a project to open", "Open Project", JOptionPane.QUESTION_MESSAGE, null,
                 projectNames, projectNames[0]);
 
         if (newProjectName == null) {
             // User hit cancel.
             return;
         }
 
         // Close current project.
         closeImage();
 
         // Open other project.
         // TODO: Open images, etc.
         currentProjectName = newProjectName;
 
         // Update settings file.
         writeToSettingsFile(currentProjectName, "");
     }
 
     private boolean writeToSettingsFile(String projectName, String imageName) {
         File settingsFile = new File(MAIN_FOLDER + "/.settings");
         if (!settingsFile.canWrite()) {
             System.err.println("Can't write settings file...");
             return false;
         }
 
         try {
             BufferedWriter out = new BufferedWriter(new FileWriter(settingsFile, false));
 
             out.write(projectName);
             if (!projectName.isEmpty()) {
                 out.newLine();
 
                 out.write(imageName);
                 if (!imageName.isEmpty()) {
                     out.newLine();
                 }
             }
             out.close();
         } catch (IOException e) {
             // TODO: Error
             e.printStackTrace();
             return false;
         }
 
         return true;
     }
 
     public void importImage() {
         // User chooses image from file dialog.
         JFileChooser chooser = new JFileChooser();
         chooser.setDialogTitle("Choose an image to import");
         int returnValue = chooser.showOpenDialog(appFrame);
         if (returnValue != JFileChooser.APPROVE_OPTION) {
             return;
         }
 
         File imageFile = chooser.getSelectedFile();
         String imageName = imageFile.getName();
 
         // Check for filename conflict. If so, prompt user to overwrite, rename,
         // or cancel.
         File imagesDirectory = new File(MAIN_FOLDER + "/Projects/" + currentProjectName + "/images");
         if (!imagesDirectory.exists()) {
             // ERROR
             System.err.println("Images dir doesnt exist");
             return;
         }
 
         FileFilter fileFilter = new FileFilter() {
             @Override
             public boolean accept(File pathname) {
                 return pathname.isFile();
             }
         };
         File images[] = imagesDirectory.listFiles(fileFilter);
         String imageNames[] = new String[images.length];
         for (int i = 0; i < imageNames.length; i++) {
             imageNames[i] = images[i].getName();
         }
 
         Object[] options = { "Cancel", "Rename New Image", "Overwrite Old Image" };
 
         // Cannot just use getNameFromUser, as this is a complex case.
         boolean hasName = false;
         while (!hasName) {
             for (int i = 0; i < images.length; i++) {
                 System.out.println(images[i].getName());
                 if (imageName.equals(images[i].getName())) {
                     int result = JOptionPane.showOptionDialog(appFrame,
                             "Cannot import image due to duplicate name.", "Unable to import image",
                             JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                             options, options[2]);
                     if (result == 0) {
                         // Cancel.
                         return;
                     } else if (result == 1) {
                         // Rename
                         imageName = getNameFromUser("Image Name", imageNames, false);
                         if (imageName == null) {
                             // User cancelled.
                             return;
                         }
 
                         break;
                     } else {
                         // Overwrite
                         // TODO: Delete labels file.
                         break;
                     }
                 }
             }
 
             hasName = true;
         }
 
         // Copy the image over.
         File destFile = new File(imagesDirectory.getAbsolutePath() + "/" + imageName);
         try {
             copyFile(imageFile, destFile);
         } catch (IOException e) {
             // FAIL
             System.err.println("Ioexception");
             return;
         }
 
         // Open image up, no labels.
         openImage(destFile);
         currentImageName = imageName;
 
         writeToSettingsFile(currentProjectName, currentImageName);
     }
 
     private void copyFile(File sourceFile, File destFile) throws IOException {
         FileChannel source = null;
         FileChannel destination = null;
 
         try {
             if (!destFile.exists()) {
                 destFile.createNewFile();
             }
 
             source = new FileInputStream(sourceFile).getChannel();
             destination = new FileOutputStream(destFile).getChannel();
             destination.transferFrom(source, 0, source.size());
 
         } finally {
             if (source != null) {
                 source.close();
             }
             if (destination != null) {
                 destination.close();
             }
         }
 
     }
 
     private String getNameFromUser(String message, String[] disallowedNames, boolean allowEmpty) {
         String name = null;
         boolean hasName = false;
 
         while (!hasName) {
             name = JOptionPane.showInputDialog(appFrame, message);
 
             if (name == null) {
                 // User cancelled.
                 return null;
             }
 
             name = name.trim();
             if (name.isEmpty() && !allowEmpty) {
                 JOptionPane.showMessageDialog(appFrame, "Blank names are not allowed.", "Error",
                         JOptionPane.ERROR_MESSAGE);
                 continue;
             }
 
             // Check for duplicates.
             hasName = true;
             for (int i = 0; i < disallowedNames.length; i++) {
                 if (name.equals(disallowedNames[i])) {
                     JOptionPane.showMessageDialog(appFrame, "That name is already in use.",
                             "Error", JOptionPane.ERROR_MESSAGE);
                     hasName = false;
                 }
             }
         }
 
         return name;
     }
 
     public void openImage() {
         // User chooses image
         File imagesDirectory = new File(MAIN_FOLDER + "/Projects/" + currentProjectName + "/images");
 
         FileFilter fileFilter = new FileFilter() {
             @Override
             public boolean accept(File pathname) {
                 return pathname.isFile();
             }
         };
 
         File images[] = imagesDirectory.listFiles(fileFilter);
         if (images.length == 0) {
             JOptionPane.showMessageDialog(appFrame, "Project has no images. Please import some.",
                     "Error", JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         String imageNames[] = new String[images.length];
         for (int i = 0; i < imageNames.length; i++) {
             imageNames[i] = images[i].getName();
         }
 
         String newImageName = (String) JOptionPane.showInputDialog(appFrame,
                 "Choose an image to open", "Open Image", JOptionPane.QUESTION_MESSAGE, null,
                 imageNames, imageNames[0]);
 
         if (newImageName == null) {
             // User hit cancel.
             return;
         }
 
         // Open image up
         openImage(new File(imagesDirectory.getAbsoluteFile() + "/" + newImageName));
         currentImageName = newImageName;
 
         writeToSettingsFile(currentProjectName, currentImageName);
     }
 
     public void save() {
         if (currentImageName == null) {
             // TODO: Shouldnt be able to save if no image.
             return;
         }
 
         // Get current image name, turn into labels name.
         String labelName = removeExtension(currentImageName) + ".labels";
 
         // Save labels file
         File labelFile = new File(MAIN_FOLDER + "/Projects/" + currentProjectName + "/labels/"
                 + labelName);
         if (labelFile.exists()) {
             // TODO: Check with user?
         }
 
         saveLabels(labelFile);
     }
 
     private String removeExtension(String fileName) {
         int extensionIndex = fileName.lastIndexOf('.');
         if (extensionIndex == -1) {
             return fileName;
         }
         return fileName.substring(0, extensionIndex);
     }
 
     /**
      * Loads the data from the settings file.
      */
     private void loadSettingsFile() {
         File settingsFile = new File(MAIN_FOLDER + "/.settings");
         if (!settingsFile.exists() || !settingsFile.canRead()) {
             // Just ignore it.
             return;
         }
 
         String projectName = null;
         String imageName = null;
         BufferedReader in;
 
         try {
             in = new BufferedReader(new FileReader(settingsFile));
             projectName = in.readLine();
         } catch (IOException e) {
             return;
         }
 
         try {
             imageName = in.readLine();
         } catch (IOException e) {
             // Image names aren't needed.
         }
 
         // TODO: Check project exists.
 
         currentProjectName = projectName;
         currentImageName = imageName;
         if (imageName != null) {
             File imageFile = new File(MAIN_FOLDER + "/Projects/" + currentProjectName + "/images/"
                     + currentImageName);
             openImage(imageFile);
         }
     }
 }
