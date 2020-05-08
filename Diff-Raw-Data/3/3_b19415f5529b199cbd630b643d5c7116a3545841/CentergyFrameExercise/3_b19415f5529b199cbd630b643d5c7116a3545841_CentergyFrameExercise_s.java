 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package centergyframe;
 
 import edu.gatech.statics.exercise.BodySubset;
 import edu.gatech.statics.exercise.Schematic;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.math.Vector;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.modes.description.Description;
 import edu.gatech.statics.modes.description.layouts.ScrollbarLayout;
 import edu.gatech.statics.modes.distributed.DistributedExercise;
 import edu.gatech.statics.modes.distributed.objects.ConstantDistributedForce;
 import edu.gatech.statics.modes.distributed.objects.DistributedForce;
 import edu.gatech.statics.modes.distributed.objects.DistributedForceObject;
 import edu.gatech.statics.modes.distributed.ui.DistributedSelectModePanel;
 import edu.gatech.statics.modes.fbd.FreeBodyDiagram;
 import edu.gatech.statics.modes.frame.FrameUtil;
 import edu.gatech.statics.objects.DistanceMeasurement;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.objects.bodies.Bar;
 import edu.gatech.statics.objects.bodies.Beam;
 import edu.gatech.statics.objects.connectors.Connector2ForceMember2d;
 import edu.gatech.statics.objects.connectors.Fix2d;
 import edu.gatech.statics.objects.connectors.Pin2d;
 import edu.gatech.statics.objects.connectors.Roller2d;
 import edu.gatech.statics.objects.representations.ModelNode;
 import edu.gatech.statics.objects.representations.ModelRepresentation;
 import edu.gatech.statics.tasks.SolveConnectorTask;
 import edu.gatech.statics.ui.AbstractInterfaceConfiguration;
 import java.math.BigDecimal;
 /**
  *
  * @author Vignesh
 */
 public class CentergyFrameExercise extends DistributedExercise {
 
     // borrowed from FrameExercise
     @Override
     public AbstractInterfaceConfiguration createInterfaceConfiguration() {
         AbstractInterfaceConfiguration ic = (AbstractInterfaceConfiguration) super.createInterfaceConfiguration();
 
         // replace the mode default select mode panel with the frame one.
         ic.replaceModePanel(DistributedSelectModePanel.class, new SpecialSelectModePanel());
 
         return ic;
     }
 
     // borrowed from FrameExercise
     @Override
     protected FreeBodyDiagram createFreeBodyDiagram(BodySubset bodies) {
 
         // set up the special name for the whole frame, in frame problems
         if (FrameUtil.isWholeDiagram(bodies)) {
             bodies.setSpecialName(FrameUtil.whatToCallTheWholeDiagram);
         }
 
         return super.createFreeBodyDiagram(bodies);
     }
 
 
     @Override
     public Description getDescription() {
 
         Description description = new Description();
 
         description.setNarrative("As you may have heard, there was a partial collapse of the parking deck behind the Centergy Building" +
                                  "in Technology Square on Monday, June 29, 2009, around noon time. " +
                                  "Though not on Georgia Tech campus, some Tech employees do lease parking spaces within this deck. " +
                                  "Thankfully, nobody was injured.");
 
         description.setProblemStatement("The first picture shows two different supports that the firefighters built to maintain " +
                 "stability of the floors adjacent to where the floors collapsed. We want to analyze the top structure in this problem. " +
                 "To simplify the analysis, let’s only look at the front plane which can be simplified as shown in the bottom picture." +
                 " We can also simplify the analysis by assuming the entire structure is supporting 30 kips, which is the approximate weight " +
                 "of the slab of concrete and cars on top of it.");
 
         description.setGoals("Find the reaction forces at points A and F.");
 
         description.setLayout(new ScrollbarLayout());
 
         description.addImage("centergyframe/assets/screenshot.png");
         description.addImage("centergyframe/assets/mainpicture.png");
         description.addImage("centergyframe/assets/outsidebeam.png");
         description.addImage("centergyframe/assets/insidedeck.png");
 
         return description;
     }
 
     public void initExercise() {
 
         Unit.setSuffix(Unit.distance, " ft");
         Unit.setSuffix(Unit.moment, " kip*ft");
         Unit.setSuffix(Unit.force, " kip");
         Unit.setSuffix(Unit.forceOverDistance, " kip/ft");
 
 
     }
 
     @Override
     public void loadExercise() {
         Schematic schematic = getSchematic();
 
         Point A = new Point("A", "0", "0", "0");
         Point B = new Point("B", "0", "5", "0");
         Point C = new Point("C", "0", "10", "0");
         Point D = new Point("D", "4", "10", "0");
         Point E = new Point("E", "4", "5", "0");
         Point F = new Point("F", "4", "0", "0");
 
         A.createDefaultSchematicRepresentation();
         B.createDefaultSchematicRepresentation();
         C.createDefaultSchematicRepresentation();
         D.createDefaultSchematicRepresentation();
         E.createDefaultSchematicRepresentation();
         F.createDefaultSchematicRepresentation();
 
         schematic.add(A);
         schematic.add(B);
         schematic.add(C);
         schematic.add(D);
         schematic.add(E);
         schematic.add(F);
 
         Bar BE = new Bar("BE", B, E);
         Beam ABC = new Beam("AC", A, C);
         Beam CD = new Beam("CD", C, D);
         Beam DEF = new Beam("DF", D, F);
 
 
         //adding distributed forces for beam CD
         DistributedForce distributedtruss = new ConstantDistributedForce("centergyTrussForce", CD, C, D,
         new Vector(Unit.forceOverDistance, Vector3bd.UNIT_Y.negate(), new BigDecimal("15")));
         DistributedForceObject distributedtrussObject = new DistributedForceObject(distributedtruss, "1");
         //Added by Jayraj to fix no name problem
         //distributedtrussObject.setName("trussForce");
         CD.addObject(distributedtrussObject);
 
 
 
         //arrow graphic size and create schematic representation of arrows (adding to visual scene)
         int arrowDensity = 2;
         distributedtrussObject.createDefaultSchematicRepresentation(18f / 6, 2 * arrowDensity, 1.75f);
 
         //creating distance measurement between points
         DistanceMeasurement measureFull = new DistanceMeasurement(A, F);
         measureFull.createDefaultSchematicRepresentation();
         measureFull.setName("Measure AF");
         schematic.add(measureFull);
 
         //Adding pins to end points
         Pin2d endA = new Pin2d(A);
         endA.attachToWorld(ABC);
         endA.setName("support A");
         endA.createDefaultSchematicRepresentation();
 
         Roller2d endF = new Roller2d(F);
         endF.setDirection(Vector3bd.UNIT_Y);
         endF.attachToWorld(DEF);
         endF.setName("Support F");
         endF.createDefaultSchematicRepresentation();
 
         Connector2ForceMember2d connectorB = new Connector2ForceMember2d(B, BE);
         connectorB.attach(ABC, BE);
 
         Connector2ForceMember2d connectorE = new Connector2ForceMember2d(E, BE);
         connectorE.attach(DEF, BE);
 
         Fix2d fixC = new Fix2d(C);
         fixC.attach(ABC, CD);
         Fix2d fixD = new Fix2d(D);
         fixD.attach(DEF, CD);
 
 
 
         schematic.add(BE);
         schematic.add(ABC);
         schematic.add(CD);
         schematic.add(DEF);
 
         //Importing Models
         ModelNode modelNode = ModelNode.load("centergyframe/assets/", "centergyframe/assets/CenturgyB_collada.dae");
         modelNode.extractLights();
 
         //Specifying the root folder for all the model objects that need to be attached to the exercise elements (points)
         ModelRepresentation rep;
         String prefix = "VisualSceneNode/everything/everything_group1/Scene/";
 
         //Attaching a model object to it's respective exercise element
         rep = modelNode.extractElement(ABC, prefix+"pCube3");
         ABC.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(CD, prefix + "pCube16");
         CD.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(DEF, prefix + "pCube2");
         DEF.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(BE, prefix + "pCube27");
         BE.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.getRemainder(schematic.getBackground());
         schematic.getBackground().addRepresentation(rep);
 
         //Adding Tasks
         addTask(new SolveConnectorTask("Solve A", endA));
         addTask(new SolveConnectorTask("Solve F", endF));
     }
    
 }
 
 
 
 
