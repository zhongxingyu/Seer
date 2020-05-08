 package com.irr310.server.game;
 
 import com.irr310.common.Game;
 import com.irr310.common.tools.Vect3;
 import com.irr310.common.world.Component;
 import com.irr310.common.world.Ship;
 import com.irr310.common.world.World;
 import com.irr310.server.GameServer;
 
 public class ShipFactory {
 
     public static Ship createSimpleShip() {
 
         World world = Game.getInstance().getWorld();
         
         Ship newShip = new Ship(GameServer.pickNewId());
 
         // Kernel
         Component kernel = ComponentFactory.createKernel();
         kernel.setShipPosition(new Vect3(0, 0, 0));
         world.addComponent(kernel);
         newShip.assign(kernel);
 
         // Camera
         Component camera = ComponentFactory.createCamera();
         camera.setShipPosition(new Vect3(0, -1, 0));
         world.addComponent(camera);
         newShip.assign(camera);
 
         // Factory
         Component factory = ComponentFactory.createFactory();
         factory.setShipPosition(new Vect3(0, 0, -2));
         world.addComponent(factory);
         newShip.assign(factory);
 
         // Tank
         Component tank = ComponentFactory.createTank();
         tank.setShipPosition(new Vect3(0, 5.5, -2));
         world.addComponent(tank);
         newShip.assign(tank);
 
         // Harvester
         Component harvester = ComponentFactory.createHarvester();
         harvester.setShipPosition(new Vect3(0, 11, -2));
         world.addComponent(harvester);
         newShip.assign(harvester);
 
         // Refinery
         Component rafinery = ComponentFactory.createRefinery();
         rafinery.setShipPosition(new Vect3(0, -4, -2));
         world.addComponent(rafinery);
         newShip.assign(rafinery);
 
         // Hangar
         Component hangar = ComponentFactory.createHangar();
         hangar.setShipPosition(new Vect3(0, -8, -2));
         world.addComponent(hangar);
         newShip.assign(hangar);
 
         // PVCells
         Component frontLeft = ComponentFactory.createPVCell();
         frontLeft.setShipPosition(new Vect3(-5, 0, -2));
         world.addComponent(frontLeft);
         newShip.assign(frontLeft);
 
         Component backLeft = ComponentFactory.createPVCell();
         backLeft.setShipPosition(new Vect3(-5, -6, -2));
         world.addComponent(backLeft);
         newShip.assign(backLeft);
 
         Component frontRight = ComponentFactory.createPVCell();
         frontRight.setShipPosition(new Vect3(5, 0, -2));
         world.addComponent(frontRight);
         newShip.assign(frontRight);
 
         Component backRight = ComponentFactory.createPVCell();
         backRight.setShipPosition(new Vect3(5, -6, -2));
         world.addComponent(backRight);
         newShip.assign(backRight);
 
        // Engines
         Component mainLeftPropeller = ComponentFactory.createBigPropeller();
         mainLeftPropeller.setShipPosition(new Vect3(-10, 0, -2));
         world.addComponent(mainLeftPropeller);
         newShip.assign(mainLeftPropeller);
 
         Component mainRightPropeller = ComponentFactory.createBigPropeller();
         mainRightPropeller.setShipPosition(new Vect3(10, 0, -2));
         world.addComponent(mainRightPropeller);
         newShip.assign(mainRightPropeller);
 
         // Links
         newShip.link(kernel, camera, new Vect3(0., -0.5, 0.));
         newShip.link(kernel, factory, new Vect3(0, 0, -0.5));
         newShip.link(factory, tank, new Vect3(0, 2, -2));
         newShip.link(tank, harvester, new Vect3(0, 9, -2));
         newShip.link(factory, rafinery, new Vect3(0, -2, -2));
         newShip.link(rafinery, hangar, new Vect3(0, -6, -2));
         newShip.link(factory, frontLeft, new Vect3(-2, 0, -2));
         newShip.link(frontLeft, backLeft, new Vect3(-5, -3, -2));
         newShip.link(factory, frontRight, new Vect3(2, 0, -2));
         newShip.link(frontRight, backRight, new Vect3(5, -3, -2));
         newShip.link(frontLeft, mainLeftPropeller, new Vect3(-8, 0, -2));
         newShip.link(frontRight, mainRightPropeller, new Vect3(8, 0, -2));
 
         /*
          * // Engines LinearEngine mainLeftPropeller = new LinearEngine();
          * LinearEngine mainRightPropeller = new LinearEngine(); LinearEngine
          * leftPropeller = new LinearEngine(); LinearEngine rightPropeller = new
          * LinearEngine(); LinearEngine topPropeller = new LinearEngine();
          * LinearEngine bottomPropeller = new LinearEngine();
          * newShip.assign(camera); newShip.assign(harvester);
          * newShip.assign(factory); newShip.assign(rafinery);
          * newShip.assign(hangar); newShip.assign(frontLeft);
          * newShip.assign(backLeft); newShip.assign(frontRight);
          * newShip.assign(backRight); newShip.assign(mainLeftPropeller);
          * newShip.assign(mainRightPropeller); newShip.assign(leftPropeller);
          * newShip.assign(rightPropeller); newShip.assign(topPropeller);
          * newShip.assign(bottomPropeller); camera.setShipPosition(new Vect3(0,
          * 0, 0)); harvester.setShipPosition(new Vect3(0, 0, 0));
          * factory.setShipPosition(new Vect3(0, 0, 0));
          * rafinery.setShipPosition(new Vect3(0, 0, 0));
          * frontLeft.setShipPosition(new Vect3(0, 0, 0));
          * backLeft.setShipPosition(new Vect3(0, 0, 0));
          * frontRight.setShipPosition(new Vect3(0, 0, 0));
          * backRight.setShipPosition(new Vect3(0, 0, 0));
          * mainLeftPropeller.setShipPosition(new Vect3(0, 0, 0));
          * mainRightPropeller.setShipPosition(new Vect3(0, 0, 0));
          * leftPropeller.setShipPosition(new Vect3(0, 0, 0));
          * rightPropeller.setShipPosition(new Vect3(0, 0, 0));
          * topPropeller.setShipPosition(new Vect3(0, 0, 0));
          * bottomPropeller.setShipPosition(new Vect3(0, 0, 0));
          */
 
         // newShip.link(harvester.getSlot(1,1), harvester.getSlot(2, 1), 0);
 
         return newShip;
 
     }
 
     public static Ship createOre() {
         Ship ship = new Ship(GameServer.pickNewId());
         return ship;
     }
 
 }
