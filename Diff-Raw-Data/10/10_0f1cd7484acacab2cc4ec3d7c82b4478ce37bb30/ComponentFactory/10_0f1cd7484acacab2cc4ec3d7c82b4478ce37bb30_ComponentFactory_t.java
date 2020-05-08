 package com.irr310.server.game;
 
 import com.irr310.common.Game;
 import com.irr310.common.tools.Vect3;
 import com.irr310.common.world.Component;
 import com.irr310.common.world.Part;
 import com.irr310.server.GameServer;
 
 public class ComponentFactory {
 
     public static Component createBigPropeller() {
         Component component = createSimpleComponent();
         Part part = component.getFirstPart();
         part.setMass(48d);
 
         part.setShape(new Vect3(4, 2, 4));
 
         generateRectangeSlots(component, part);
 
         return component;
     }
 
     public static Component createCamera() {
         Component component = createSimpleComponent();
         Part part = component.getFirstPart();
         part.setMass(1d);
 
         part.setShape(new Vect3(1, 1, 1));
 
         generateBoxSlots(component, part);
 
         return component;
     }
 
     public static Component createFactory() {
         Component component = createSimpleComponent();
         Part part = component.getFirstPart();
         part.setMass(48d);
 
         part.setShape(new Vect3(4, 4, 3));
 
         generateBoxSlots(component, part);
 
         return component;
     }
 
     public static Component createPVCell() {
         Component component = createSimpleComponent();
         Part part = component.getFirstPart();
         part.setMass(5d);
 
         part.setShape(new Vect3(6, 6, 1));
 
         generateRectangeSlots(component, part);
 
         return component;
     }
 
     public static Component createHangar() {
         Component component = createSimpleComponent();
         Part part = component.getFirstPart();
         part.setMass(48d);
 
         part.setShape(new Vect3(4, 4, 3));
 
         generateBoxSlots(component, part);
 
         return component;
     }
 
     public static Component createRefinery() {
         Component component = createSimpleComponent();
         Part part = component.getFirstPart();
         part.setMass(48d);
 
         part.setShape(new Vect3(4, 4, 3));
 
         generateBoxSlots(component, part);
 
         return component;
     }
 
     public static Component createTank() {
         Component component = createSimpleComponent();
         Part part = component.getFirstPart();
         part.setMass(20d);
 
         part.setShape(new Vect3(4, 7, 4));
 
         generateBoxSlots(component, part);
 
         return component;
     }
 
     public static Component createHarvester() {
         Component component = createSimpleComponent();
         Part part = component.getFirstPart();
         part.setMass(48d);
 
         part.setShape(new Vect3(4, 4, 3));
 
         generateBoxSlots(component, part);
 
         return component;
     }
 
     public static Component createKernel() {
         Component component = createSimpleComponent();
         Part part = component.getFirstPart();
         part.setMass(1d);
 
         part.setShape(new Vect3(1, 1, 1));
 
         generateBoxSlots(component, part);
 
         return component;
     }
 
     public static Component createGate() {
         Component component = createSimpleComponent();
         return component;
     }
 
     public static Component createBeam() {
         Component component = createSimpleComponent();
         return component;
     }
 
     public static Component createPiston() {
         Component component = createSimpleComponent();
         return component;
     }
 
     public static Component createTorqueEngine() {
         Component component = createSimpleComponent();
         return component;
     }
 
     // Tools
 
     private static Component createSimpleComponent() {
         Component component = new Component(GameServer.pickNewId());
         
         Part part = new Part(GameServer.pickNewId());
         component.addPart(part);
         Game.getInstance().getWorld().addPart(part);
         return component;
     }
 
     private static void generateBoxSlots(Component component, Part part) {
 
         Vect3 shape = part.getShape();
 
         component.addSlot(GameServer.pickNewId(), part, new Vect3(shape.x / 2, 0, 0));
         component.addSlot(GameServer.pickNewId(), part, new Vect3(-shape.x / 2, 0, 0));
         component.addSlot(GameServer.pickNewId(), part, new Vect3(0, shape.y / 2, 0));
         component.addSlot(GameServer.pickNewId(), part, new Vect3(0, -shape.y / 2, 0));
         component.addSlot(GameServer.pickNewId(), part, new Vect3(0, 0, shape.z / 2));
         component.addSlot(GameServer.pickNewId(), part, new Vect3(0, 0, -shape.z / 2));
 
     }
 
     private static void generateRectangeSlots(Component component, Part part) {
 
         Vect3 shape = part.getShape();
 
         component.addSlot(GameServer.pickNewId(), part, new Vect3(shape.x / 2, 0, 0));
         component.addSlot(GameServer.pickNewId(), part, new Vect3(-shape.x / 2, 0, 0));
        component.addSlot(GameServer.pickNewId(), part, new Vect3(0, shape.y / 2, 0));
        component.addSlot(GameServer.pickNewId(), part, new Vect3(0, -shape.y / 2, 0));
 
     }
 }
