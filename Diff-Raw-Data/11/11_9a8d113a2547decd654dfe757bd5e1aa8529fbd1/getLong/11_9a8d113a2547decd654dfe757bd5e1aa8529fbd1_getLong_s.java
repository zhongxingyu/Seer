 package gnu.testlet.java.lang.Long;
 import gnu.testlet.Testlet;
 import gnu.testlet.TestHarness;
 
 public class getLong implements Testlet
 {
   public void test (TestHarness harness)
     {
       // Augment the System properties with the following.
       // Overwriting is bad because println needs the
       // platform-dependent line.separator property.
       Properties p = System.getProperties();
       p.put("e1", Long.toString(Long.MIN_VALUE));
       p.put("e2", Long.toString(Long.MAX_VALUE));
       p.put("e3", "0" + Long.toOctalString(Long.MIN_VALUE));
       p.put("e4", "0" + Long.toOctalString(Long.MAX_VALUE));
       p.put("e5", "0x" + Long.toHexString(Long.MIN_VALUE));
       p.put("e6", "0x" + Long.toHexString(Long.MAX_VALUE));
       p.put("e7", "0" + Long.toString(Long.MAX_VALUE, 8));
       p.put("e8", "#" + Long.toString(Long.MAX_VALUE, 16));
       p.put("e9", "");
       p.put("e10", " ");
       p.put("e11", "foo");
 
       harness.check (Long.getLong("e1").toString(), "-9223372036854775808");
       harness.check (Long.getLong("e2").toString(), "9223372036854775807");
       harness.check (Long.getLong("e3"), null);
       harness.check (Long.getLong("e4").toString(), "9223372036854775807");
       harness.check (Long.getLong("e5", 12345L).toString(), "12345");
       harness.check (Long.getLong("e6", new Long(56789L)).toString(),
 		     "9223372036854775807");
       harness.check (Long.getLong("e7", null).toString(),
 		     "9223372036854775807");
       harness.check (Long.getLong("e8", 12345).toString(),
 		     "9223372036854775807");
       harness.check (Long.getLong("e9", new Long(56789L)).toString(), "56789");
       harness.check (Long.getLong("e10", null), null);
       harness.check (Long.getLong("e11"), null);
       harness.check (Long.getLong("junk", 12345L).toString(), "12345");
       harness.check (Long.getLong("junk", new Long(56789L)).toString(),
 		     "56789");
       harness.check (Long.getLong("junk", null), null);
       harness.check (Long.getLong("junk"), null);
     }
 
  public getLon ()
     {
     }
 }
