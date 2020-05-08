 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.co.mtford.unfication;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.junit.*;
 import static org.junit.Assert.*;
 import uk.co.mtford.abduction.logic.*;
 
 /**
  *
  * @author mtford
  */
 public class UnifierTest {
     
     private static Unifier unifier;
     
     private static Predicate Knows;
     private static Constant John;
     private static Constant Jane;
     private static Variable x;
     
     private static Variable A;
     private static Variable B;
     private static Variable C;
     private static Predicate p;
     private static Predicate q;
     
     public UnifierTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         unifier = new Unifier();
         Knows = new Predicate("Knows", John, x);
         John = new Constant("John");
         Jane = new Constant("Jane");
         x = new Variable("x");
         
         A = new Variable("A");
         B = new Variable("B");
        C = new Variable("A");
         p = new Predicate("p", A, B);
         q = new Predicate("q", C);
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
     
     @Before
     public void setUp() {
     }
     
     @After
     public void tearDown() {
     }
     
     /** unify(Knows(John,x),Knows(John,Jane)) should return {x/Jane} */
     @Test
     public void simpleUnificationTest() throws CouldNotUnifyException {
         Map<Variable,IUnifiable> sub = unifier.unify(new Predicate("Knows", John, x), new Predicate("Knows", John, Jane));
         assertTrue(sub.size()==1);
         assertTrue(sub.containsKey(x)&&sub.get(x).equals(Jane));
     }
     
     /** unify(f(g(X)),f(Y)) should return {Y/g(X)} */
     @Test
     public void simpleFunctionUnificationTest() throws CouldNotUnifyException {
         Variable X = new Variable("X");
         Function g = new Function("g",X);
         Variable Y = new Variable("Y");
         Map<Variable,IUnifiable> sub1 = unifier.unify(new Function("f",g),
                                                       new Function("f",Y));
         Map<Variable,IUnifiable> sub2 = unifier.unify(new Function("f",Y),
                                                       new Function("f",g));
         assertTrue(sub1.size()==1&&sub2.size()==1);
         assertTrue(sub1.containsKey(Y)&&sub2.containsKey(Y));
         assertTrue(sub1.get(Y).equals(g)&&sub2.get(Y).equals(g));
     }
     
     /** unify(f(g(X),X),f(Y,a)) should return {X/a,Y/g(a)}  */
     @Test
     public void complexFunctionUnificationTest() throws CouldNotUnifyException {
         Variable X = new Variable("X");
         Constant a = new Constant("a");
         Function gOfX = new Function("g",X);
         Function gOfa = new Function("g",a);
         Variable Y = new Variable("Y");
         
         Map<Variable,IUnifiable> sub1 = unifier.unify(new Function("f",gOfX, X),
                                                       new Function("f",Y,a));
         Map<Variable,IUnifiable> sub2 = unifier.unify(new Function("f",Y, a),
                                                       new Function("f",gOfX, X));
         assertTrue(sub1.size()==2);
         assertTrue(sub2.size()==2);
         assertTrue(sub1.containsKey(X)&&sub1.containsKey(Y));
         assertTrue(sub2.containsKey(X)&&sub2.containsKey(Y));
         assertTrue(sub1.get(X).equals(a));
         assertTrue(sub2.get(Y).equals(gOfa));
     }
     
     /** unify(X = Y, Y = a) should return {X/a,Y/a} */
     @Test
     public void simpleEqualitiesUnificationTest() throws CouldNotUnifyException {
         Variable X = new Variable("X");
         Variable Y = new Variable("Y");
         Constant a = new Constant("a");
         Equality e1 = new Equality(X,Y);
         Equality e2 = new Equality(Y,a);
         
         Map<Variable,IUnifiable> sub1 = unifier.unify(e1,e2);
         Map<Variable,IUnifiable> sub2 = unifier.unify(e2,e1);
         
         assertTrue(sub1.size()==2);
         assertTrue(sub2.size()==2);
         assertTrue(sub1.containsKey(X)&&sub1.containsKey(Y));
         assertTrue(sub2.containsKey(X)&&sub2.containsKey(Y));
         assertTrue(sub1.get(X).equals(a));
         assertTrue(sub2.get(Y).equals(a));
     }
     
     /** occurs(A,P(A,B)) should return true */
     @Test
    public void occursTestA() {
         assertTrue(unifier.occurs(A, p, new HashMap<Variable,IUnifiable>()));
     }
     
      /** occurs(B,P(A,B)) should return true */
     @Test
    public void occursTestB() {
         assertTrue(unifier.occurs(B, p, new HashMap<Variable,IUnifiable>()));
     }
     
      /** occurs(C,P(A,B)) should return false */
     @Test
    public void occursTestC() {
         assertFalse(unifier.occurs(C, p, new HashMap<Variable,IUnifiable>()));
     }
     
 }
