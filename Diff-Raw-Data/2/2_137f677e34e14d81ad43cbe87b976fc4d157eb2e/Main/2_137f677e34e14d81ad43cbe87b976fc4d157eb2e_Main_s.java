 /*
  * Copyright 2012 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of
  *       conditions and the following disclaimer.
  *
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list
  *       of conditions and the following disclaimer in the documentation and/or other materials
  *       provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those of the
  * authors and should not be interpreted as representing official policies, either expressed
  * or implied, of Michael Hoffer <info@michaelhoffer.de>.
  */
 package eu.mihosoft.vtk;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Rectangle2D;
 import java.io.File;
 import java.io.IOException;
 import java.util.Scanner;
 import javax.swing.*;
 import vtk.*;
 
 /**
  * Demo Application that demonstrates {@link VTKJPanel}.
  *
  * Based on http://www.particleincell.com/2011/vtk-java-visualization
  *
  * @author Michael Hoffer <info@michaelhoffer.de>
  */
 public class Main extends JPanel implements ActionListener {
 
     private static final long serialVersionUID = 1L;
     private VTKJPanel renWin;
     private vtkActor cutActor;
     private vtkActor isoActor;
     private JPanel buttons;
     private JToggleButton slicesButton;
     private JToggleButton isoButton;
 
     /*
      * Constructor - generates visualization pipeline and adds actors
      */
     public Main() {
 
         BorderLayout layout = new BorderLayout();
 
         setLayout(layout);
 
         /*
          * large center and small border areas
          */
 
         /*
          * sphere radius
          */
         double radius = 0.8;
 
         /**
          * ** 1) INPUT DATA: Sphere Implicit Function ***
          */
         vtkSphere sphere = new vtkSphere();
         sphere.SetRadius(radius);
 
         vtkSampleFunction sample = new vtkSampleFunction();
         sample.SetSampleDimensions(50, 50, 50);
         sample.SetImplicitFunction(sphere);
 
         /**
          * ** 2) PIPELINE 1: Isosurface Actor ***
          */
         /*
          * contour filter - will generate isosurfaces from 3D data
          */
         vtkContourFilter contour = new vtkContourFilter();
         contour.SetInputConnection(sample.GetOutputPort());
         contour.GenerateValues(3, 0, 1);
 
         /*
          * mapper, translates polygonal representation to graphics primitives
          */
         vtkPolyDataMapper isoMapper = new vtkPolyDataMapper();
         isoMapper.SetInputConnection(contour.GetOutputPort());
 
         /*
          * isosurface actor
          */
         isoActor = new vtkActor();
         isoActor.SetMapper(isoMapper);
 
         /**
          * ** 3) PIPELINE 2: Cutting Plane Actor ***
          */
         /*
          * define a plane in x-y plane and passing through the origin
          */
         vtkPlane plane = new vtkPlane();
         plane.SetOrigin(0, 0, 0);
         plane.SetNormal(0, 0, 1);
 
         /*
          * cutter, basically interpolates source data onto the plane
          */
         vtkCutter planeCut = new vtkCutter();
         planeCut.SetInputConnection(sample.GetOutputPort());
         planeCut.SetCutFunction(plane);
         /*
          * this will actually create 3 planes at the subspace where the implicit
          * function evaluates to -0.7, 0, 0.7 (0 would be original plane). In
          * our case this will create three x-y planes passing through z=-0.7,
          * z=0, and z=+0.7
          */
         planeCut.GenerateValues(3, -0.7, 0.7);
 
         /*
          * look up table, we want to reduce number of values to get discrete
          * bands
          */
         vtkLookupTable lut = new vtkLookupTable();
         lut.SetNumberOfTableValues(5);
 
         /*
          * mapper, using our custom LUT
          */
         vtkPolyDataMapper cutMapper = new vtkPolyDataMapper();
         cutMapper.SetInputConnection(planeCut.GetOutputPort());
         cutMapper.SetLookupTable(lut);
 
         /*
          * cutting plane actor, looks much better with flat shading
          */
         cutActor = new vtkActor();
         cutActor.SetMapper(cutMapper);
         cutActor.GetProperty().SetInterpolationToFlat();
 
         /**
          * ** 4) PIPELINE 3: Surface Geometry Actor ***
          */
         /*
          * create polygonal representation of a sphere
          */
         vtkSphereSource surf = new vtkSphereSource();
         surf.SetRadius(radius);
 
         /*
          * another mapper
          */
         vtkPolyDataMapper surfMapper = new vtkPolyDataMapper();
         surfMapper.SetInputConnection(surf.GetOutputPort());
 
         /*
          * surface geometry actor, turn on edges and apply flat shading
          */
         vtkActor surfActor = new vtkActor();
         surfActor.SetMapper(surfMapper);
         surfActor.GetProperty().EdgeVisibilityOn();
         surfActor.GetProperty().SetEdgeColor(0.2, 0.2, 0.2);
         surfActor.GetProperty().SetInterpolationToFlat();
 
 
 
         /**
          * ** 5) RENDER WINDOW ***
          */
         /*
          * vtkPanel - this is the interface between Java and VTK
          */
         renWin = new VTKJPanel() {
 
             @Override
             protected void paintComponent(Graphics g) {
 
                 Graphics2D g2 = (Graphics2D) g;
 
                 int numTilesX = getWidth() / 20;
                 int numTilesY = getHeight() / 20;
 
                 double xInc = getWidth() / numTilesX;
                 double yInc = getWidth() / numTilesX;
 
                 for (int yIndex = 0; yIndex < numTilesY + 1; yIndex++) {
                     for (int xIndex = 0; xIndex < numTilesX + 1; xIndex++) {
 
                         double x = xIndex * xInc;
                         double y = yIndex * yInc;
 
                         g2.setPaint(Color.lightGray);
                         g2.fill(new Rectangle2D.Double(x, y, xInc, yInc));
 
                         if (xIndex % 2 == 0 && yIndex % 2 == 0
                                 || xIndex % 2 == 1 && yIndex % 2 == 1) {
                             g2.setPaint(Color.GRAY);
                             g2.fill(new Rectangle2D.Double(x, y, xInc, yInc));
                         }
 
                     }
                 }
 
                 super.paintComponent(g);
             }
         };
 
         renWin.setOpaque(false);
 
         /*
          * add the surface geometry plus the isosurface
          */
         renWin.getRenderer().AddActor(surfActor);
         renWin.getRenderer().AddActor(isoActor);
 
         /*
          * the default zoom is whacky, zoom out to see the whole domain
          */
         renWin.getRenderer().GetActiveCamera().Dolly(0.15);
 
         /**
          * ** 6) CREATE PANEL FOR BUTTONS ***
          */
         buttons = new JPanel();
         buttons.setLayout(new GridLayout(1, 0));
 
         /*
          * isosurface button, clicked by default
          */
         isoButton = new JToggleButton("Isosurfaces", true);
         isoButton.addActionListener(this);
 
         /*
          * cutting planes button
          */
         slicesButton = new JToggleButton("Slices");
         slicesButton.addActionListener(this);
 
         JButton incBtn = new JButton("Increase Alpha");
         JButton decBtn = new JButton("Decrease Alpha");
 
         /*
          * add buttons to the panel
          */
         buttons.add(isoButton);
         buttons.add(slicesButton);
         buttons.add(incBtn);
         buttons.add(decBtn);
 
         incBtn.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
 
                 float alpha = renWin.getContentAlpha() + 0.1f;
                 alpha = Math.min(1.f, alpha);
 
                 renWin.setContentAlpha(alpha);
                 renWin.repaint();
             }
         });
 
         decBtn.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
 
                 float alpha = renWin.getContentAlpha() - 0.1f;
                 alpha = Math.max(0.f, alpha);
 
                 renWin.setContentAlpha(alpha);
                 renWin.repaint();
             }
         });
 
         /**
          * ** 7) POPULATE MAIN PANEL ***
          */
         add(renWin, BorderLayout.CENTER);
         add(buttons, BorderLayout.SOUTH);
     }
 
     /*
      * ActionListener that responds to button clicks Toggling iso/slices buttons
      * results in addition or removal of the corresponding actor
      */
     @Override
     public void actionPerformed(ActionEvent e) {
         /*
          * cutting planes button, add or remove cutActor
          */
         if (e.getSource().equals(slicesButton)) {
             if (slicesButton.isSelected()) {
                 renWin.getRenderer().AddActor(cutActor);
             } else {
                 renWin.getRenderer().RemoveActor(cutActor);
             }
             renWin.contentChanged();
             renWin.repaint();
         } /*
          * isosurface button, add or remove isoActor
          */ else if (e.getSource().equals(isoButton)) {
             if (isoButton.isSelected()) {
                 renWin.getRenderer().AddActor(isoActor);
             } else {
                 renWin.getRenderer().RemoveActor(isoActor);
             }
             renWin.contentChanged();
             renWin.repaint();
         }
     }
 
     public static void readLine(String title) {
         Scanner keyIn = new Scanner(System.in);
         System.out.print(title + " : Press the enter key to continue");
         keyIn.nextLine();
     }
     
     /**
      * Runs this application.
      *
      * @param s
      */
     public static void main(String s[]) throws IOException {
 
         File path = new File("natives");
 
         String arch = System.getProperty("os.arch");
         System.out.println("ARCH: " + arch);

        SysUtil.loadNativeLibrariesInFolder(path, true);
         
         // we use custom library loading. As we plan to use this project in
         // a flexible plugin based system it is necessary to load native
         // libraries without modifying the PATH variable on windows
         // Question: does Windows really only have one PATH and no library
         //           path variable? Please tell me if I am wrong.
         System.loadLibrary("jawt");
         SysUtil.loadLibraries(path.getAbsolutePath());
 
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
 
                 JFrame frame = new JFrame("VTKJPanel Demo");
                 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
                 Main panel = new Main();
 
                 frame.add(panel, BorderLayout.CENTER);
                 frame.setSize(600, 600);
                 frame.setLocationRelativeTo(null);
                 frame.setVisible(true);
 
                 panel.repaint();
             }
         });
     }
 }
