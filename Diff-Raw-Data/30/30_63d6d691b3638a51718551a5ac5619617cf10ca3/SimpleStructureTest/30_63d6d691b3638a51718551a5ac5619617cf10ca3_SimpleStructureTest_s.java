 package org.clafer;
 
 import org.clafer.scope.Scope;
 import org.clafer.ast.AstAbstractClafer;
 import org.clafer.ast.AstConcreteClafer;
 import org.clafer.ast.AstModel;
 import static org.clafer.ast.Asts.*;
 import org.clafer.compiler.ClaferCompiler;
 import org.clafer.compiler.ClaferSolver;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 /**
  *
  * @author jimmy
  */
 public class SimpleStructureTest {
 
     /**
      * <pre>
      * abstract Object
      *     Name ?
      * abstract Animal : Object
      *     Tail ?
      * abstract Primate : Animal
      *     Bipedal ?
      * Human : Primate
      * Beaver : Animal
      * </pre>
      */
     @Test(timeout = 60000)
     public void testMultiLevelAbstract() {
         AstModel model = newModel();
 
         AstAbstractClafer object = model.addAbstractClafer("Object");
         object.addChild("Name").withCard(0, 1);
 
         AstAbstractClafer animal = model.addAbstractClafer("Animal").extending(object);
         animal.addChild("Tail").withCard(0, 1);
 
         AstAbstractClafer primate = model.addAbstractClafer("Primate").extending(animal);
         primate.addChild("Bipedal").withCard(0, 1);
 
         model.addChild("Human").withCard(1, 1).extending(primate);
         model.addChild("Beaver").withCard(1, 1).extending(animal);
 
         ClaferSolver solver = ClaferCompiler.compile(model, Scope.defaultScope(2).toScope());
         assertEquals(32, solver.allInstances().length);
     }
 
     /**
      * <pre>
      * xor Type
      *     Car ?
      *     Truck ?
      *     Van ?
      * </pre>
      */
     @Test(timeout = 60000)
     public void testXorGroupCardinality() {
         AstModel model = newModel();
 
         AstConcreteClafer type = model.addChild("Type").withCard(1, 1).withGroupCard(1, 1);
         type.addChild("Car").withCard(0, 1);
         type.addChild("Truck").withCard(0, 1);
         type.addChild("Van").withCard(0, 1);
 
         ClaferSolver solver = ClaferCompiler.compile(model, Scope.defaultScope(2).toScope());
         assertEquals(3, solver.allInstances().length);
     }
 
     /**
      * <pre>
      * xor Type
      *     Car ?
      *     Truck ?
      *     Van ?
      * </pre>
      */
     @Test(timeout = 60000)
     public void testCustomGroupCardinality() {
         AstModel model = newModel();
 
         AstConcreteClafer type = model.addChild("Type").withCard(1, 1).withGroupCard(2, 3);
         type.addChild("Car").withCard(0, 1);
         type.addChild("Truck").withCard(0, 1);
         type.addChild("Van").withCard(0, 1);
 
         ClaferSolver solver = ClaferCompiler.compile(model, Scope.defaultScope(2).toScope());
         assertEquals(4, solver.allInstances().length);
     }
 
     /**
      * <pre>
      * xor Type ?
      *     Car ?
      *     Truck ?
      *     Van ?
      * </pre>
      */
     @Test(timeout = 60000)
     public void testMaybeGroupCardinality() {
         AstModel model = newModel();
 
         AstConcreteClafer type = model.addChild("Type").withCard(0, 1).withGroupCard(1, 1);
         type.addChild("Car").withCard(0, 1);
         type.addChild("Truck").withCard(0, 1);
         type.addChild("Van").withCard(0, 1);
 
         ClaferSolver solver = ClaferCompiler.compile(model, Scope.defaultScope(2).toScope());
         assertEquals(4, solver.allInstances().length);
     }
 
     /**
      * <pre>
      * Person
      *     Age ->> integer 2
      * </pre>
      */
     @Test(timeout = 60000)
     public void testRefs() {
         AstModel model = newModel();
 
         AstConcreteClafer person = model.addChild("Person").withCard(1, 1);
         person.addChild("Age").withCard(2, 2).refTo(IntType);
 
         ClaferSolver solver = ClaferCompiler.compile(model, Scope.defaultScope(2).intLow(-2).intHigh(2).toScope());
         assertEquals(25, solver.allInstances().length);
     }
 
     /**
      * <pre>
      * Person
      *     Age -> integer 2
      * </pre>
      */
     @Test(timeout = 60000)
     public void testUniqueRefs() {
         AstModel model = newModel();
 
         AstConcreteClafer person = model.addChild("Person").withCard(1, 1);
         person.addChild("Age").withCard(2, 2).refToUnique(IntType);
 
         ClaferSolver solver = ClaferCompiler.compile(model, Scope.defaultScope(2).intLow(-2).intHigh(2).toScope());
         assertEquals(20, solver.allInstances().length);
     }
 
     /**
      * <pre>
      * Age ->> integer 2..3
      * </pre>
      */
     @Test(timeout = 60000)
     public void testTopLevelRefs() {
         AstModel model = newModel();
 
         model.addChild("Age").withCard(2, 3).refTo(IntType);
 
         ClaferSolver solver = ClaferCompiler.compile(model, Scope.defaultScope(3).intLow(-2).intHigh(2).toScope());
         assertEquals(150, solver.allInstances().length);
     }
 
     /**
      * <pre>
      * Age -> integer 2..3
      * </pre>
      */
     @Test(timeout = 60000)
     public void testTopLevelUniqueRefs() {
         AstModel model = newModel();
 
         model.addChild("Age").withCard(2, 3).refToUnique(IntType);
 
         ClaferSolver solver = ClaferCompiler.compile(model, Scope.defaultScope(3).intLow(-2).intHigh(2).toScope());
         assertEquals(80, solver.allInstances().length);
     }
 
     /**
      * <pre>
      * abstract Feature -> integer
      * Backup : Feature
      * Firewall : Feature 1..2
      * </pre>
      */
     @Test(timeout = 60000)
     public void testAbstractRefs() {
         AstModel model = newModel();
 
         AstAbstractClafer feature = model.addAbstractClafer("Feature").refTo(IntType);
         model.addChild("Backup").withCard(1, 1).extending(feature);
         model.addChild("Firewall").withCard(1, 2).extending(feature);
 
         ClaferSolver solver = ClaferCompiler.compile(model, Scope.defaultScope(2).intLow(-2).intHigh(2).toScope());
         assertEquals(150, solver.allInstances().length);
     }
 
     /**
      * <pre>
      * abstract Feature ->> integer
      * Backup : Feature
      * Firewall : Feature 1..2
      * </pre>
      */
     @Test(timeout = 60000)
     public void testAbstractUniqueRefs() {
         AstModel model = newModel();
 
         AstAbstractClafer feature = model.addAbstractClafer("Feature").refToUnique(IntType);
         model.addChild("Backup").withCard(1, 1).extending(feature);
         model.addChild("Firewall").withCard(1, 2).extending(feature);
 
         ClaferSolver solver = ClaferCompiler.compile(model, Scope.defaultScope(2).intLow(-2).intHigh(2).toScope());
         assertEquals(125, solver.allInstances().length);
     }
 
     /**
      * <pre>
      * Feature 1..2
      * Free -> Feature
      * </pre>
      */
     @Test(timeout = 60000)
     public void testRefToVariableClafer() {
         AstModel model = newModel();
 
         AstConcreteClafer feature = model.addChild("Feature").withCard(1, 2);
         model.addChild("Free").withCard(1, 1).refTo(feature);
 
         ClaferSolver solver = ClaferCompiler.compile(model, Scope.defaultScope(2).toScope());
         // Can be reduced to 2 with better symmetry breaking
          assertEquals(3, solver.allInstances().length);
     }
 }
