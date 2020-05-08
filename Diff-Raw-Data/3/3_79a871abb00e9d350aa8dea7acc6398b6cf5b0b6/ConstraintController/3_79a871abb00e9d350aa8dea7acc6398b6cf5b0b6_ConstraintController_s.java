 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package interiores.business.controllers;
 
 import interiores.business.controllers.abstracted.InterioresController;
 import interiores.business.exceptions.NoRoomCreatedException;
 import interiores.business.models.Orientation;
 import interiores.business.models.WantedFurniture;
 import interiores.business.models.constraints.BinaryConstraint;
 import interiores.business.models.constraints.UnaryConstraint;
 import interiores.business.models.constraints.binary.MaxDistanceConstraint;
 import interiores.business.models.constraints.binary.MinDistanceConstraint;
 import interiores.business.models.constraints.unary.AreaConstraint;
 import interiores.business.models.constraints.unary.ColorConstraint;
 import interiores.business.models.constraints.unary.MaterialConstraint;
 import interiores.business.models.constraints.unary.ModelConstraint;
 import interiores.business.models.constraints.unary.OrientationConstraint;
 import interiores.business.models.constraints.unary.PriceConstraint;
 import interiores.business.models.constraints.unary.SizeConstraint;
 import interiores.business.models.constraints.unary.WallConstraint;
 import interiores.core.business.BusinessException;
 import interiores.core.data.JAXBDataController;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * Business Controller covering the use cases related to constraints
  * @author larribas
  */
 public class ConstraintController
     extends InterioresController {
     
     /**
      * Creates a particular instance of the constraint controller
      * @param data The data controller that will give access to the objects this controller will use
      */
     public ConstraintController(JAXBDataController data)
     {
         super(data);
     }
 
      /**
      * Gets all all the unary and binary constraints related to the specified wanted furniture
      * @param id Identifier of the furniture
      * @return A collection of both unary and binary constraints
      * @throws NoRoomCreatedException 
      */
     public Collection getConstraints(String id)
             throws NoRoomCreatedException
     {
         return getWishList().getConstraints(id);
     }
 
     /**
      * Creates a determined constraint and adds it to a furniture.
      * If a constraint of that type already existed, it is replaced
      * @param type The type of the constraint we want to add
      * @param parameters A list of parameters (its length depends on the type of constraint being defined)
      * @param furnitureID A valid ID of the furniture we want to apply the constraint to
      * @throws NoRoomCreatedException
      */
    public void add(String type, List<Object> parameters, String furnitureID) throws NoRoomCreatedException
     {
         if (type.equals("width") || type.equals("depth")) {
             // We get the SizeConstraint of that furniture. If there isn't one, we create it
             if (getWantedFurniture(furnitureID).getConstraint("size")==null)
                 getWantedFurniture(furnitureID).addConstraint("size", new SizeConstraint());
             
             SizeConstraint sc = (SizeConstraint) getWantedFurniture(furnitureID).getConstraint("size");
             int min = (Integer) parameters.get(0);
             int max = (Integer) parameters.get(1);
             
             if (type.equals("width")) sc.changeWidth(min,max);
             else sc.changeDepth(min, max);
         }
         else {
             UnaryConstraint uc = null;
 
             if (type.equals("color")) uc = new ColorConstraint((String) parameters.get(0));
             else if (type.equals("material")) uc = new MaterialConstraint((String) parameters.get(0));
             else if (type.equals("model")) uc = new ModelConstraint((String) parameters.get(0));
             else if (type.equals("orientation")) uc = new OrientationConstraint(Orientation.valueOf((String) parameters.get(0)));
             else if (type.equals("price")) uc = new PriceConstraint((Integer) parameters.get(0));
             else if (type.equals("position")) {
                 List<Point> validPositions = new ArrayList();
                 String mode = (String) parameters.get(0);
                 if (mode.equals("at")) validPositions.add(new Point((Integer) parameters.get(1), (Integer) parameters.get(2)));
                 else if (mode.equals("range")) {
                     for (int i = (Integer) parameters.get(1); i <= (Integer) parameters.get(3); i++)
                         for (int j = (Integer) parameters.get(2); j <= (Integer) parameters.get(4); j++)
                             validPositions.add(new Point(i,j));
                     
                     uc = new AreaConstraint(validPositions);
                 }
                 else if (mode.equals("walls")) {
                     String whichWalls = (String) parameters.get(1);
                     int roomWidth = getRoom().getWidth();
                     int roomDepth = getRoom().getDepth();
                     
                     Orientation[] orientations;
                     
                     if(whichWalls.equals("all"))
                         orientations = Orientation.values();
                     else
                         orientations = new Orientation[]{ Orientation.valueOf(whichWalls) };
                         
                     uc = new WallConstraint(roomWidth, roomDepth, orientations);
                 }
             }
 
             getWantedFurniture(furnitureID).addConstraint(type, uc);
         }
         
     }
     
     /**
      * Creates a determined binary constraint and adds it to a pair of furniture pieces.
      * If a constraint of that type already existed between the tow furniture pieces, it is replaced
      * @param type The type of the constraint we want to add
      * @param parameters A list of parameters (its length depends on the type of constraint being defined)
      * @param furn1 A valid ID of the first furniture component we want to apply the constraint to
      * @param furn2 A valid ID of the second furniture component we want to apply the constraint to
      * @throws BusinessException
      */
     public void add(String type, List<Object> parameters, String furn1, String furn2)
             throws BusinessException
     {
         if (type.equals("distance"))
         {
             String rel = (String) parameters.get(0);
             int dist = (Integer) parameters.get(1);
             BinaryConstraint bc = null;
             if (rel.equals("min")) bc = new MinDistanceConstraint(dist);
             else if (rel.equals("max")) bc = new MaxDistanceConstraint(dist);
             else throw new BusinessException(rel + " constraint doesn't exist");
             
             getWishList().addBinaryConstraint(rel, bc, furn1, furn2);
         }
     }
     
     /**
      * Removes a constraint of a specific type that has been defined over a certain piece of furniture.
      * If there was no constraint of that type over that furniture, it does nothing
      * @param ctype The type of the constraint we want to remove
      * @param furnitureID A valid ID of the piece of furniture whose constraint we want to remove
      * @throws NoRoomCreatedException
      */
     public void remove(String ctype, String furnitureID)
             throws NoRoomCreatedException
     {
         getWantedFurniture(furnitureID).removeConstraint(ctype);
     }
     
     /**
      * Removes a binary constraint of a specific type that has been defined over two certain pieces of furniture.
      * If there was no constraint of that type over those pieces of furniture, it does nothing
      * @param ctype The type of the constraint we want to remove
      * @param furn1 A valid ID of the first piece of furniture whose constraint we want to remove
      * @param furn2 A valid ID of the second piece of furniture whose constraint we want to remove
      * @throws NoRoomCreatedException
      */
     public void remove(String ctype, String furn1, String furn2)
             throws NoRoomCreatedException
     {
         getWishList().removeBinaryConstraint(ctype, furn1, furn2);
     }
     
     /**
      * Gets a specific piece of wanted furniture, defined by its ID within our wish list
      * @param id The ID of the furniture you want to obtain
      * @return The particular furniture piece with ID = id
      * @throws NoRoomCreatedException 
      */
     private WantedFurniture getWantedFurniture(String id)
             throws NoRoomCreatedException
     {
         return getWishList().getWantedFurniture(id);
     }
        
 }
