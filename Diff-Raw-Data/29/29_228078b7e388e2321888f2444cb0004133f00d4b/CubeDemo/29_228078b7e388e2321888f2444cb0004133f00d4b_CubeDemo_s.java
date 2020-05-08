 /*
  * Copyright (C) 2010 Klaus Reimer <k@ailis.de>
  * See LICENSE.txt for licensing information.
  */
 
 package de.ailis.threedee.demo;
 
 import java.awt.Dimension;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 
 import de.ailis.threedee.builder.MaterialBuilder;
 import de.ailis.threedee.builder.MeshBuilder;
 import de.ailis.threedee.entities.DirectionalLight;
 import de.ailis.threedee.entities.Light;
 import de.ailis.threedee.entities.LightInstance;
 import de.ailis.threedee.entities.Material;
 import de.ailis.threedee.entities.Mesh;
 import de.ailis.threedee.entities.MeshInstance;
 import de.ailis.threedee.entities.Scene;
 import de.ailis.threedee.entities.SceneNode;
 import de.ailis.threedee.jogl.swing.SceneCanvas;
 import de.ailis.threedee.model.Color;
 
 
 /**
  * Simple demo showing a cube constructed with the MeshBuilder.
  *
  * @author Klaus Reimer (k@ailis.de)
  */
 
 public class CubeDemo
 {
     /**
      * Main method.
      *
      * @param args
      *            Command line arguments
      * @throws IOException
      *             When IO error occurs
      */
 
     public static void main(final String args[]) throws IOException
     {
         // Create the frame
         final JFrame frame = new JFrame();
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         // Create the canvas component displaying the scene
         final SceneCanvas canvas = new SceneCanvas();
         canvas.setPreferredSize(new Dimension(512, 512));
         frame.getContentPane().add(canvas);
 
 
         // Create the scene
         final Scene scene = canvas.getScene();
         final SceneNode root = scene.getRootNode();
 
         // Create a light
        final Light light = new DirectionalLight(Color.WHITE);
         final SceneNode lightNode = new SceneNode();
         final LightInstance lightInstance = new LightInstance(light);
         lightNode.addLight(lightInstance);
         root.enableLight(lightInstance);
         root.appendChild(lightNode);
        lightNode.rotateY((float) Math.PI / 2);
 
         // Build the cube mesh
         final MeshBuilder builder = new MeshBuilder();
         builder.addVertex(1, 1, 1);
         builder.addVertex(-1, 1, 1);
         builder.addVertex(-1, -1, 1);
         builder.addVertex(1, -1, 1);
         builder.addVertex(1, 1, -1);
         builder.addVertex(-1, 1, -1);
         builder.addVertex(-1, -1, -1);
         builder.addVertex(1, -1, -1);
         builder.useMaterial("front");
         builder.addElement(4, 0, 1, 2, 3);
         builder.useMaterial("back");
         builder.addElement(4, 4, 7, 6, 5);
         builder.useMaterial("top");
         builder.addElement(4, 4, 5, 1, 0);
         builder.useMaterial("bottom");
         builder.addElement(4, 3, 2, 6, 7);
         builder.useMaterial("left");
         builder.addElement(4, 1, 5, 6, 2);
         builder.useMaterial("right");
         builder.addElement(4, 0, 3, 7, 4);
         final Mesh cubeMesh = builder.build("cube");
 
        final Material frontMaterial = new MaterialBuilder().setId("frontMaterial").setDiffuseColor(Color.RED).build();
 
         // Create the cube mesh instance
         final MeshInstance cube = new MeshInstance(cubeMesh);
        //cube.bindMaterial("front", frontMaterial);
        //cube.bindMaterial("top", frontMaterial);
 
         // Create the cube scene node
         final SceneNode cubeNode = new SceneNode();
         cubeNode.addMesh(cube);
         cubeNode.translateZ(-10);
         cubeNode.getPhysics().getSpin().setX(0.2f).setY(0.1f).setZ(0.3f);
 
         // Add the cube node to the scene
         root.appendChild(cubeNode);
 
 
         // Display the frame
         frame.pack();
         frame.setVisible(true);
     }
 }
