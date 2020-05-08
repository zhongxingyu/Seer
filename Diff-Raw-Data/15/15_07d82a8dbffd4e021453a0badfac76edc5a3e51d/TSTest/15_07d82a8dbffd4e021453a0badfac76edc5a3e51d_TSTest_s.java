 package test;
 
 import jason.asSemantics.Agent;
 import jason.asSemantics.Circumstance;
 import jason.asSemantics.Intention;
 import jason.asSemantics.Option;
 import jason.asSemantics.TransitionSystem;
 import jason.asSyntax.Literal;
 import jason.asSyntax.StringTerm;
 import jason.asSyntax.StringTermImpl;
 import jason.asSyntax.Structure;
 import jason.asSyntax.Trigger;
 
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Queue;
 
 import junit.framework.TestCase;
 
 /** JUnit test case for syntax package */
 public class TSTest extends TestCase {
 
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     public void testRelevant() {
         Agent ag = new Agent();
         Circumstance c = new Circumstance();
         StringTerm pt1 = new StringTermImpl("@t1 +a(X) : g(10) <- .print(\"ok 10\").");
         ag.getPL().add(pt1, new Structure("nosource"));
         ag.getPL().add(new StringTermImpl("@t2 +a(X) : true <- .print(\"ok 20\")."), new Structure("nosource"));
         ag.getPL().add(new StringTermImpl("@t3 +b : true <- true."), new Structure("nosource"));
         TransitionSystem ts = new TransitionSystem(ag, c, null, null);
         Literal content = Literal.parseLiteral("~alliance");
         content.addSource(new Structure("ag1"));
 
         Trigger te1 = Trigger.parseTrigger("+a(10)");
 
         try {
             List<Option> rp = ts.relevantPlans(te1);
             // System.out.println("RP="+rp);
             assertEquals(rp.size(), 2);
 
             rp = ts.applicablePlans(rp);
             // System.out.println("AP="+rp);
             assertEquals(rp.size(), 1);
 
             // Option opt = ag.selectOption(rp);
             // IntendedMeans im = new IntendedMeans(opt);
             // System.out.println(im);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         Trigger te2 = Trigger.parseTrigger("+a(20)");
 
         try {
             List<Option> rp = ts.relevantPlans(te2);
             // System.out.println("RP="+rp);
             assertEquals(rp.size(), 2);
 
             rp = ts.applicablePlans(rp);
             // System.out.println("AP="+rp);
             assertEquals(rp.size(), 1);
 
             //Option opt = ag.selectOption(rp);
             //IntendedMeans im = new IntendedMeans(opt);
             // System.out.println(im);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     public void testIntentionOrder() {
         Intention i1 = new Intention();
         Intention i2 = new Intention(); 
         
         Intention i3 = new Intention(); 
         i3.setAtomic(true);
         assertTrue(i3.isAtomic());
         
         Intention i4 = new Intention();
         
         Queue<Intention> q1 = new PriorityQueue<Intention>();
         q1.offer(i1);
         q1.offer(i2);
         q1.offer(i3);
         q1.offer(i4);
        assertTrue(q1.poll().getId() == 3);
         //System.out.println(q1.poll());
         //System.out.println(q1.poll());
         //System.out.println(q1.poll());
 
         /*
         List<Intention> l = new ArrayList<Intention>();
         l.add(i1);
         l.add(i2);
         l.add(i3);
         l.add(i4);
         Collections.sort(l);
         
         System.out.println(l);
         */
         
     }
 }
