 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package magiccopier;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.animation.Timeline;
 import javafx.application.Platform;
 import javafx.beans.binding.DoubleBinding;
 import javafx.beans.property.DoubleProperty;
 import javafx.beans.property.ListProperty;
 import javafx.beans.property.LongProperty;
 import javafx.beans.property.ObjectProperty;
 import javafx.beans.property.SimpleDoubleProperty;
 import javafx.beans.property.SimpleListProperty;
 import javafx.beans.property.SimpleLongProperty;
 import javafx.beans.property.SimpleObjectProperty;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.concurrent.WorkerStateEvent;
 import javafx.event.EventHandler;
 import javax.swing.filechooser.FileSystemView;
 import magiccopier.StorageSize.StorageSizeUnit;
 
 /**
  *
  * @author abhishekmunie
  */
 public class MagicCopierPreferencesModel {
 
     private static final FileSystemView fsv = FileSystemView.getFileSystemView();
 
     private final ObjectProperty<File> destinationDirectory;
     private final LongProperty maxSize;
     private final ObjectProperty<StorageSizeUnit> maxSizeUnit;
     private final DoubleProperty sliderMaxValue;
     private final DoubleProperty scaledMaxSize;
     private final ListProperty<Drive> drivesList;
     private final DoubleProperty totalProgressProperty;
 
     private final Timer drivesUpdateTimer;
     private Timeline drivesValidator;
     private boolean drivesListIsInvalid;
     private DoubleBinding totalProgressBinding;
 
     /**
      *
      */
     public MagicCopierPreferencesModel() {
         try {
             Files.createDirectories(new File(System.getProperty("user.home"), "Magic Copier").toPath());
         } catch (IOException ex) {
             Logger.getLogger(MagicCopierPreferencesModel.class.getName()).log(Level.SEVERE, null, ex);
         }
         destinationDirectory = new SimpleObjectProperty<>(new File(System.getProperty("user.home"), "Magic Copier"));
         maxSize = new SimpleLongProperty(1l);
         maxSizeUnit = new SimpleObjectProperty<>(StorageSizeUnit.GegaBytes);
         sliderMaxValue = new SimpleDoubleProperty(100);
         scaledMaxSize = new SimpleDoubleProperty(scaleSize(maxSize.get(), maxSizeUnit.get(), sliderMaxValue.get()));
         totalProgressProperty = new SimpleDoubleProperty(0);
 
         maxSize.addListener(new ChangeListener<Number>() {
             @Override
             public void changed(ObservableValue<? extends Number> ov, Number oldMaxSize, Number newMaxSize) {
                 scaledMaxSize.set(scaleSize(maxSize.get(), maxSizeUnit.get(), sliderMaxValue.get()));
             }
         });
         maxSizeUnit.addListener(new ChangeListener<StorageSizeUnit>() {
             @Override
             public void changed(ObservableValue<? extends StorageSizeUnit> ov, StorageSizeUnit oldMaxSizeUnit, StorageSizeUnit newMaxSizeUnit) {
                 scaledMaxSize.set(scaleSize(maxSize.get(), maxSizeUnit.get(), sliderMaxValue.get()));
             }
         });
         scaledMaxSize.addListener(new ChangeListener<Number>() {
             @Override
             public void changed(ObservableValue<? extends Number> ov, Number oldScaledMaxSize, Number newScaledMaxSize) {
                if (newScaledMaxSize.doubleValue() == 1.0) {
                     setMaxSize(Long.MAX_VALUE, StorageSizeUnit.TeraBytes);
                 } else {
                     setMaxSizeFromScaledSize(newScaledMaxSize.doubleValue(), sliderMaxValue.get());
                 }
             }
         });
 
         drivesList = new SimpleListProperty<>(FXCollections.observableArrayList(getDrives()));
         drivesUpdateTimer = new Timer(true);
         drivesUpdateTimer.schedule(new TimerTask() {
             @Override
             public void run() {
                 boolean changed = false;
                 final Drive[] newDrives = getDrives();
 
                 for (Drive root : newDrives) {
                     int i = drivesList.indexOf(root);
                     if (i != -1) {
                         drivesList.get(i).exists = true;
                     } else {
                         root.exists = true;
                         addDrive(root);
                         changed = true;
                     }
                 }
                 if (newDrives.length != drivesList.size()) {
                     changed = true;
                 }
                 if (changed) {
                     System.out.println("Currently Connected Drives:");
                     ArrayList<Drive> drivesToBeRemoved = new ArrayList<>(drivesList.size());
                     for (Drive drive : drivesList) {
                         if (drive.exists) {
                             drive.exists = false;
                             System.out.println(drive.getAbsolutePath());
                         } else {
                             drivesToBeRemoved.add(drive);
                         }
                     }
                     for (Drive driveToBeRemoved : drivesToBeRemoved) {
                         removeDrive(driveToBeRemoved);
                     }
                 }
             }
         }, 1000, 1000);
     }
 
     void addDrive(final Drive driveToBeAdded) {
         Platform.runLater(new Runnable() {
             @Override
             public void run() {
                 try {
                     driveToBeAdded.initProgressPane();
                 } catch (IOException ex) {
                     Logger.getLogger(MagicCopierPreferencesModel.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 drivesList.add(driveToBeAdded);
                 driveToBeAdded.startCopy(destinationDirectory.get().toPath(), new EventHandler<WorkerStateEvent>() {
                     @Override
                     public void handle(WorkerStateEvent t) {
                         System.out.println("copied drive: " + driveToBeAdded.volumeName.get());
                     }
                 });
             }
         });
     }
 
     void removeDrive(final Drive driveToBeRemoved) {
         Platform.runLater(new Runnable() {
             @Override
             public void run() {
                 drivesList.remove(driveToBeRemoved);
             }
         });
     }
 
     static Drive[] getDrives() {
         Iterable<Path> roots = FileSystems.getDefault().getRootDirectories();
         ArrayList<Drive> drives = new ArrayList<>();
         for (Path drivePath : roots) {
             if (drivePath.toString().equals("/")) {
                 File[] rootFiles = new File("/Volumes").listFiles();
                 for (File file : rootFiles) {
                     drives.add(new Drive(file.toPath()));
                 }
                 break;
             }
             drives.add(new Drive(drivePath));
         }
         Drive[] d = new Drive[drives.size()];
         return drives.toArray(d);
     }
 
     public ObjectProperty<File> destinationDirectoryProperty() {
         return destinationDirectory;
     }
 
     public LongProperty maxSizeProperty() {
         return maxSize;
     }
 
     public ObjectProperty<StorageSizeUnit> maxSizeUnitProperty() {
         return maxSizeUnit;
     }
 
     public DoubleProperty scaledMaxSizeProperty() {
         return scaledMaxSize;
     }
 
     public DoubleProperty sliderMaxValueProperty() {
         return sliderMaxValue;
     }
 
     public ListProperty<Drive> drivesListProperty() {
         return drivesList;
     }
 
     public DoubleProperty totalProgressProperty() {
         return totalProgressProperty;
     }
 
     public File getDestinationDirectory() {
         return destinationDirectory.get();
     }
 
     public double getSacledMaxSize() {
         return scaleSize(maxSize.get(), maxSizeUnit.get(), sliderMaxValue.get());
     }
 
     public long getMaxSize() {
         return maxSize.get();
     }
 
     public StorageSizeUnit getMaxSizeUnit() {
         return maxSizeUnit.get();
     }
 
     public void setMaxSize(long maxSize, StorageSizeUnit maxSizeUnit) {
         this.maxSize.set(maxSize);
         this.maxSizeUnit.set(maxSizeUnit);
     }
 
     static double scaleSize(long size, StorageSizeUnit unit, double sliderMaxValue) {
         if (unit.equals(StorageSizeUnit.TeraBytes)) {
             return (0.75 + (0.25 * size / 1024)) * sliderMaxValue;
         } else if (unit.equals(StorageSizeUnit.GegaBytes)) {
             if (size < 128) {
                 return (0.25 + (0.25 * size / 128)) * sliderMaxValue;
             } else {
                 return (0.5 + (0.25 * (size-128) / (1024-128))) * sliderMaxValue;
             }
         } else if (unit.equals(StorageSizeUnit.MegaBytes)) {
             return (0.25 * size / 1024) * sliderMaxValue;
         } else {
             return 0;
         }
     }
 
     public void setMaxSizeFromScaledSize(double scaledSize, double sliderMaxValue) {
         if (scaledSize < (0.25 * sliderMaxValue)) {
             maxSize.set((long) (1024 * scaledSize / (0.25 * sliderMaxValue)));
             maxSizeUnit.set(StorageSizeUnit.MegaBytes);
         } else if (scaledSize < (0.5 * sliderMaxValue)) {
             maxSize.set((long) (128 * (scaledSize - (0.25 * sliderMaxValue)) / (0.25 * sliderMaxValue)));
             maxSizeUnit.set(StorageSizeUnit.GegaBytes);
         } else if (scaledSize < (0.75 * sliderMaxValue)) {
             maxSize.set((long) (128 + ((1024-128) * (scaledSize - (0.5 * sliderMaxValue)) / (0.25 * sliderMaxValue))));
             maxSizeUnit.set(StorageSizeUnit.GegaBytes);
         } else {
             maxSize.set((long) (1024 * (scaledSize - (0.75 * sliderMaxValue)) / (0.25 * sliderMaxValue)));
             maxSizeUnit.set(StorageSizeUnit.TeraBytes);
         }
     }
 
 }
