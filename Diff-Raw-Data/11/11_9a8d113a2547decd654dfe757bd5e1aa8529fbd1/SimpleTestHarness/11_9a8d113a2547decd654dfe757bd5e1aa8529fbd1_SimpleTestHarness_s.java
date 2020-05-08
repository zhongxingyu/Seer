 // Copyright (c) 1998  Cygnus Solutions
 
 // Written by Tom Tromey <tromey@cygnus.com>
 
 // KNOWN BUGS:
 // * Current usage is bogus.  Should scan directory tree for `*.java'
 //   files and read them.  A few things here:
 //   - should examine Tags and omit files we don't care about
 //   - should look for /*{ ... }*/ and treat contents as expected
 //     output of test.  In this case we should redirect System.out
 //     to a temp file we create.
 
 package gnu.testlet;
 
 public class SimpleTestHarness extends TestHarness
 {
   private int count = 0;
   private int failures = 0;
   private int total = 0;
   private boolean verbose = false; // FIXME: no way to set this.
   private String description;
 
   public void check (boolean result)
     {
       String d = description + " (number " + (count + 1) + ")";
       if (! result)
 	{
 	  System.out.println("FAIL: " + d);
 	  ++failures;
 	}
       else if (verbose)
 	{
 	  System.out.println("PASS: " + d);
 	}
       ++count;
       ++total;
     }
 
   private void runtest (String name)
     {
       // Try to ensure we start off with a reasonably clean slate.
       System.gc();
       System.runFinalization();
       count = 0;
 
       Testlet t = null;
       try
 	{
 	  Class k = Class.forName (name);
 	  t = (Testlet) (k.newInstance());
 	}
       catch (Throwable ex)
 	{
 	  System.out.println ("FAIL: uncaught exception loading " + name
 			      + ": " + ex.toString());
 	  ++failures;
 	  ++total;
 	}
 
       if (t != null)
 	{
 	  description = name;
 	  t.test (this);
 	}
     }
 
   private int done ()
     {
      System.out.println(failures + " of " + count + " tests failed");
       return failures > 0 ? 1 : 0;
     }
 
   private SimpleTestHarness ()
     {
     }
 
   // Each argument is the name of a test to run.  FIXME.
   public static void main (String[] args)
     {
       SimpleTestHarness harness = new SimpleTestHarness ();
       for (int i = 0; i < args.length; ++i)
 	harness.runtest(args[i]);
       System.exit(harness.done());
     }
 }
