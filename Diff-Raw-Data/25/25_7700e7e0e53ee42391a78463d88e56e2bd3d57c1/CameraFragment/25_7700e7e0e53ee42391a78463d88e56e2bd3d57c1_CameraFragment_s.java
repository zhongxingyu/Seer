 package com.jff.engine3d.view.java.components.settings;
 
 import com.jff.engine3d.model.camera.*;
 import com.jff.engine3d.model.entities.Point3D;
 import com.jff.engine3d.model.entities.RotationCoordinates;
 import com.jff.engine3d.model.logic.Controller;
 import com.jff.engine3d.model.logic.EngineManager;
 import com.jff.engine3d.model.scene.Scene;
 import com.jff.engine3d.view.java.components.utils.UIUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.*;
 
 public class CameraFragment extends Composite {
     private final Controller controller;
     private Text toX;
     private Text toY;
     private Text toZ;
 
     private Text fromX;
     private Text fromY;
     private Text fromZ;
 
     private Text rotateX;
     private Text rotateY;
     private Text rotateZ;
 
     private Text focalLength;
     private Slider focalLengthSlider;
 
     private Button fromRotate;
     private Button toRotate;
 
     private Slider rotateXSlider;
     private Slider rotateYSlider;
     private Slider rotateZSlider;
 
     private Button perspectiveButton;
     private Button parallelButton;
 
     public CameraFragment(Composite parent) {
         super(parent, SWT.NONE);
 
 
         RowLayout layout = new RowLayout(SWT.VERTICAL);
         layout.fill = true;
         this.setLayout(layout);
 
         EngineManager engineManager = EngineManager.getInstance();
 
         controller = engineManager.getController();
 
 
         createProjectionSettings();
 
         createFromSettings();
         createToSettings();
         createRotateSettings();
         createFocalLengthSettings();
 
     }
 
     private void createProjectionSettings() {
 
         Composite parent = this;
         Group group = new Group(parent, SWT.NONE);
 
         group.setText("Projection");
 
         GridLayout gridLayout = new GridLayout(2, true);
 
         group.setLayout(gridLayout);
 
         perspectiveButton = new Button(group, SWT.RADIO);
         perspectiveButton.setText("Perspective");
 
         parallelButton = new Button(group, SWT.RADIO);
         parallelButton.setText("Parallel");
 
         parallelButton.setSelection(true);
 
 
         SelectionListener listener = new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent selectionEvent) {
                 if (parallelButton.getSelection()) {
                     controller.setProjectionType(ProjectionType.PARALLEL);
                 } else {
 
                     controller.setProjectionType(ProjectionType.CENTRAL);
                 }
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent selectionEvent) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         };
         parallelButton.addSelectionListener(listener);
         perspectiveButton.addSelectionListener(listener);
 
 
     }
 
 
     private void createFromSettings() {
 
         Composite parent = this;
         Group group = new Group(parent, SWT.NONE);
 
         group.setText("Camera From");
 
         GridLayout gridLayout = new GridLayout(2, true);
 
         group.setLayout(gridLayout);
 
         Label textX = new Label(group, SWT.NONE);
         textX.setText("X");
         fromX = new Text(group, SWT.NONE);
         fromX.addListener(SWT.Verify, UIUtils.createVerifyIntegerListener());
 
         Label textY = new Label(group, SWT.NONE);
         textY.setText("Y");
         fromY = new Text(group, SWT.NONE);
         fromY.addListener(SWT.Verify, UIUtils.createVerifyIntegerListener());
 
         Label textZ = new Label(group, SWT.NONE);
         textZ.setText("Z");
         fromZ = new Text(group, SWT.NONE);
         fromZ.addListener(SWT.Verify, UIUtils.createVerifyIntegerListener());
 
         Button buttonOk = new Button(group, SWT.NONE);
         buttonOk.setText("OK");
         buttonOk.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent selectionEvent) {
 
                 try {
                     int x = UIUtils.retrieveInteger(fromX);
                     int y = UIUtils.retrieveInteger(fromY);
                     int z = UIUtils.retrieveInteger(fromZ);
 
                     Point3D point3D = new Point3D(x, y, z);
 
 
                     controller.setCameraFromCoordinates(point3D);
 
                 } catch (IllegalArgumentException e) {
                     String message = e.getMessage();
                     UIUtils.showMessage(message);
                 }
 
 
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent selectionEvent) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         });
 
     }
 
 
     private void createFocalLengthSettings() {
 
         Composite parent = this;
         Group group = new Group(parent, SWT.NONE);
 
         group.setText("FocalLength");
 
         GridLayout gridLayout = new GridLayout(2, true);
 
         group.setLayout(gridLayout);
 
         Label textFocalLength = new Label(group, SWT.NONE);
         textFocalLength.setText("Length");
         focalLength = new Text(group, SWT.NONE);
         focalLength.addListener(SWT.Verify, UIUtils.createVerifyPositiveIntegerListener());
 
         focalLengthSlider = new Slider(group, SWT.NONE);
         focalLengthSlider.setMinimum(1);
         focalLengthSlider.setMaximum(1000);
         focalLengthSlider.setSelection(500);
 
         focalLengthSlider.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent selectionEvent) {
                 int focalLengthValue = focalLengthSlider.getSelection();
 
                 applyFocalLength(focalLengthValue);
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent selectionEvent) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         });
 
         Button buttonOk = new Button(group, SWT.NONE);
         buttonOk.setText("OK");
         buttonOk.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent selectionEvent) {
 
                 try {
                     int focalLength = UIUtils.retrieveInteger(CameraFragment.this.focalLength);
 
 
                     applyFocalLength(focalLength);
 
                 } catch (IllegalArgumentException e) {
                     String message = e.getMessage();
                     UIUtils.showMessage(message);
                 }
 
 
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent selectionEvent) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         });
 
     }
 
     private void applyFocalLength(int focalLength) {
         controller.setCameraFocalLength(focalLength);
 
         this.focalLength.setText("" + focalLength);
         focalLengthSlider.setSelection(focalLength);
     }
 
     private void createToSettings() {
 
         Composite parent = this;
         Group group = new Group(parent, SWT.NONE);
 
         group.setText("Camera To");
 
         GridLayout gridLayout = new GridLayout(2, true);
 
         group.setLayout(gridLayout);
 
         Label textX = new Label(group, SWT.NONE);
         textX.setText("X");
         toX = new Text(group, SWT.NONE);
         toX.addListener(SWT.Verify, UIUtils.createVerifyIntegerListener());
 
         Label textY = new Label(group, SWT.NONE);
         textY.setText("Y");
         toY = new Text(group, SWT.NONE);
         toY.addListener(SWT.Verify, UIUtils.createVerifyIntegerListener());
 
         Label textZ = new Label(group, SWT.NONE);
         textZ.setText("Z");
         toZ = new Text(group, SWT.NONE);
         toZ.addListener(SWT.Verify, UIUtils.createVerifyIntegerListener());
 
         Button buttonOk = new Button(group, SWT.NONE);
         buttonOk.setText("OK");
         buttonOk.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent selectionEvent) {
 
                 try {
 
 
                     int x = UIUtils.retrieveInteger(toX);
                     int y = UIUtils.retrieveInteger(toY);
                     int z = UIUtils.retrieveInteger(toZ);
 
                     Point3D point3D = new Point3D(x, y, z);
 
 
                     controller.setCameraToCoordinates(point3D);
 
                 } catch (IllegalArgumentException e) {
                     String message = e.getMessage();
                     UIUtils.showMessage(message);
                 }
 
 
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent selectionEvent) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         });
 
     }
 
     private void createRotateSettings() {
 
         Composite parent = this;
         Group group = new Group(parent, SWT.NONE);
 
         group.setText("Rotate camera");
 
         GridLayout gridLayout = new GridLayout(2, false);
 
         group.setLayout(gridLayout);
 
 
         fromRotate = new Button(group, SWT.RADIO);
        fromRotate.setText("From");
 
         toRotate = new Button(group, SWT.RADIO);
        toRotate.setText("To");
 
         fromRotate.setSelection(true);
 
 
         parent = group;
         group = new Group(parent, SWT.NONE);
 
         gridLayout = new GridLayout(3, false);
 
         group.setLayout(gridLayout);
 
         final Label textXZ = new Label(group, SWT.NONE);
         textXZ.setText("X");
         rotateX = new Text(group, SWT.NONE);
         rotateX.addListener(SWT.Verify, UIUtils.createVerifyPositiveIntegerListener());
 
         rotateXSlider = new Slider(group, SWT.NONE);
         initRotateSlider(rotateXSlider);
 
 
         final Label textYZ = new Label(group, SWT.NONE);
         textYZ.setText("Y");
         rotateY = new Text(group, SWT.NONE);
         rotateY.addListener(SWT.Verify, UIUtils.createVerifyPositiveIntegerListener());
 
         rotateYSlider = new Slider(group, SWT.NONE);
         initRotateSlider(rotateYSlider);
 
         final Label textXY = new Label(group, SWT.NONE);
         textXY.setText("Z");
         rotateZ = new Text(group, SWT.NONE);
         rotateZ.addListener(SWT.Verify, UIUtils.createVerifyPositiveIntegerListener());
 
         rotateZSlider = new Slider(group, SWT.NONE);
         initRotateSlider(rotateZSlider);
 
         SelectionListener sliderListener = new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent selectionEvent) {
                 int x = rotateXSlider.getSelection();
                 int y = rotateYSlider.getSelection();
                 int z = rotateZSlider.getSelection();
                 applyCameraRotation(x, y, z);
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent selectionEvent) {
                 int x = rotateXSlider.getSelection();
                 int y = rotateYSlider.getSelection();
                 int z = rotateZSlider.getSelection();
                 applyCameraRotation(x, y, z);
             }
         };
         rotateXSlider.addSelectionListener(sliderListener);
         rotateYSlider.addSelectionListener(sliderListener);
         rotateZSlider.addSelectionListener(sliderListener);
 
         Button buttonOk = new Button(group, SWT.NONE);
         buttonOk.setText("OK");
         buttonOk.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent selectionEvent) {
 
                 try {
 
 
                     int x = UIUtils.retrieveInteger(rotateX);
                     int y = UIUtils.retrieveInteger(rotateY);
                     int z = UIUtils.retrieveInteger(rotateZ);
 
                     UIUtils.checkDegrees(x);
                     UIUtils.checkDegrees(y);
                     UIUtils.checkDegrees(z);
 
                     applyCameraRotation(x, y, z);
 
                 } catch (IllegalArgumentException e) {
                     String message = e.getMessage();
                     UIUtils.showMessage(message);
                 }
 
 
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent selectionEvent) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         });
 
     }
 
     private void applyCameraRotation(int x, int y, int z) {
         CameraRotateType rotateType;
         if (fromRotate.getSelection()) {
             rotateType = CameraRotateType.FROM;
         } else {
             rotateType = CameraRotateType.TO;
         }
 
         RotationCoordinates rotationCoordinates = new RotationCoordinates(x, y, z);
 
         controller.setCameraRotation(rotateType, rotationCoordinates);
 
         rotateXSlider.setSelection(x);
         rotateYSlider.setSelection(y);
         rotateZSlider.setSelection(z);
 
         rotateX.setText("" + x);
         rotateY.setText("" + y);
         rotateZ.setText("" + z);
     }
 
     private void initRotateSlider(Slider rotateSlider) {
         rotateSlider.setMinimum(0);
        rotateSlider.setMaximum(360);
 
         rotateSlider.setSelection(0);
     }
 
 
     public void sceneChanged(Scene scene) {
         fillSettings(scene);
     }
 
     private void fillSettings(Scene scene) {
         Camera camera = scene.getCamera();
 
         AbstractCameraProjection projection = camera.getProjection();
         switch (projection.getType()) {
 
             case PARALLEL:
                 parallelButton.setSelection(true);
                 perspectiveButton.setSelection(false);
                 break;
             case CENTRAL:
                 perspectiveButton.setSelection(true);
                 parallelButton.setSelection(false);
                 break;
         }
 
         CameraSettings cameraSettings = camera.getCameraSettings();
 
         int focalLengthValue = cameraSettings.getFocalLength();
         Point3D fromCoordinates = cameraSettings.getFromCoordinates();
         Point3D toCoordinates = cameraSettings.getToCoordinates();
         CameraRotateType rotateType = cameraSettings.getRotateType();
         RotationCoordinates rotationCoordinates = cameraSettings.getRotationCoordinates();
 
         this.focalLength.setText("" + focalLengthValue);
         focalLengthSlider.setSelection(focalLengthValue);
 
         rotateX.setText("" + (int) rotationCoordinates.getX());
         rotateY.setText("" + (int) rotationCoordinates.getY());
         rotateZ.setText("" + (int) rotationCoordinates.getZ());
 
         rotateXSlider.setSelection((int) rotationCoordinates.getX());
         rotateYSlider.setSelection((int) rotationCoordinates.getY());
         rotateZSlider.setSelection((int) rotationCoordinates.getZ());
 
         fromX.setText("" + (int) fromCoordinates.getX());
         fromY.setText("" + (int) fromCoordinates.getY());
         fromZ.setText("" + (int) fromCoordinates.getZ());
 
         toX.setText("" + (int) toCoordinates.getX());
         toY.setText("" + (int) toCoordinates.getY());
         toZ.setText("" + (int) toCoordinates.getZ());
 
         switch (rotateType) {
 
             case TO:
                 toRotate.setSelection(true);
                 break;
             case FROM:
                 fromRotate.setSelection(true);
                 break;
         }
 
     }
 }
