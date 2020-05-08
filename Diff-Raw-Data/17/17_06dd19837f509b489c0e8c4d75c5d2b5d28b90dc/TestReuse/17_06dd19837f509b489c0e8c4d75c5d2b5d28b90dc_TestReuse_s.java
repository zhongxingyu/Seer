 package testcases.reuse;
 
 import org.testng.annotations.Test;
 
 import de.jpaw.bonaparte.core.BonaPortable;
 import de.jpaw.bonaparte.core.ByteArrayComposer;
 import de.jpaw.bonaparte.core.ByteArrayParser;
 import de.jpaw.bonaparte.core.MessageParserException;
 import de.jpaw.bonaparte.core.ObjectReuseStrategy;
 import de.jpaw.bonaparte.core.StringBuilderComposer;
 import de.jpaw.bonaparte.core.StringBuilderParser;
import de.jpaw.bonaparte.coretests.initializers.FillLists;
import de.jpaw.bonaparte.coretests.util.SimpleTestRunner;
 import de.jpaw.bonaparte.pojos.reuse.Body;
 import de.jpaw.bonaparte.pojos.reuse.Element;
 
 /**
  * The TestLists class.
  *
  * @author Michael Bischoff
  * @version $Revision$
  *
  *          This is a simple testcase which calls the SimpleTestRunner with a class
  *          consisting of all supported BonaPortable List types.
  */
 public class TestReuse {
 
     private Body setup() {
         Body r = new Body();
         r.w1 = new Element("hello");
         r.w2 = new Element("hello");    // same content
         r.w3 = r.w1;                    // same object
         return r;
     }
     
     private byte [] serializeByStrategyBAC(Body b, ObjectReuseStrategy strategy) {
         ByteArrayComposer bac;
         if (strategy == null) {
             bac = new ByteArrayComposer();
         } else {
             bac = new ByteArrayComposer(strategy);
         }
         bac.writeRecord(b);
         return bac.getBytes();
     }
     private byte [] serializeByStrategySBC(Body b, ObjectReuseStrategy strategy) {
         StringBuilderComposer sbc;
         StringBuilder buff = new StringBuilder(250);
         if (strategy == null) {
             sbc = new StringBuilderComposer(buff);
         } else {
             sbc = new StringBuilderComposer(buff, strategy);
         }
         sbc.writeRecord(b);
         return sbc.getBytes();
     }
     
     private Body roundtrip(Body b, ObjectReuseStrategy strategy, boolean useStringBuilderComposer, boolean useStringBuilderParser) throws MessageParserException {
         byte [] buffer;
         BonaPortable r;
         if (useStringBuilderComposer) {
             buffer = serializeByStrategySBC(b, strategy);
         } else {
             buffer = serializeByStrategyBAC(b, strategy);
         }
         if (useStringBuilderParser) {
             StringBuilder buff = new StringBuilder(new String(buffer));
             StringBuilderParser sbp = new StringBuilderParser(buff, 0, -1);
             r = sbp.readRecord();
         } else {
             r = new ByteArrayParser(buffer, 0, -1).readRecord();
         }
         return (Body)r;
     }
     
     @Test
     public void testLengthsBAC() throws Exception {
         Body b = setup();
         int l0 = serializeByStrategyBAC(b, null).length;
         int l1 = serializeByStrategyBAC(b, ObjectReuseStrategy.NONE).length;
         int l2 = serializeByStrategyBAC(b, ObjectReuseStrategy.BY_REFERENCE).length;
         int l3 = serializeByStrategyBAC(b, ObjectReuseStrategy.BY_CONTENTS).length;
         System.out.println("Lengths are " + l0 + "," + l1 + "," + l2 + "," + l3);
         assert(l0 == l1);
         assert(l1 > l2);
         assert(l2 > l3);
     }
     
     @Test
     public void testLengthsSBC() throws Exception {
         Body b = setup();
         int l0 = serializeByStrategySBC(b, null).length;
         int l1 = serializeByStrategySBC(b, ObjectReuseStrategy.NONE).length;
         int l2 = serializeByStrategySBC(b, ObjectReuseStrategy.BY_REFERENCE).length;
         int l3 = serializeByStrategySBC(b, ObjectReuseStrategy.BY_CONTENTS).length;
         System.out.println("Lengths are " + l0 + "," + l1 + "," + l2 + "," + l3);
         assert(l0 == l1);
         assert(l1 > l2);
         assert(l2 > l3);
     }
     
     @Test
     public void testObjectIdentitiesAfterDeserializationSBP() throws MessageParserException {
         Body b = setup();
         for (int i = 0; i < 4; ++i) {
             boolean useSBC = (i & 1) != 0;
             boolean useSBP = (i & 2) != 0;
             System.out.println("Testing with " + (useSBC ? "StringBuilder" : "ByteArray") + "Composer and " + (useSBP ? "StringBuilder" : "ByteArray") + "Parser");
             Body b0 = roundtrip(b, null, useSBC, useSBP);
             Body b1 = roundtrip(b, ObjectReuseStrategy.NONE, useSBC, useSBP);
             Body b2 = roundtrip(b, ObjectReuseStrategy.BY_REFERENCE, useSBC, useSBP);
             Body b3 = roundtrip(b, ObjectReuseStrategy.BY_CONTENTS, useSBC, useSBP);
 
             // default: no identity in the result
             assert (b0.w1 != b0.w2);
             assert (b0.w1 != b0.w3);
             assert (b0.w2 != b0.w3);
             // default: no identity in the result
             assert (b1.w1 != b1.w2);
             assert (b1.w1 != b1.w3);
             assert (b1.w2 != b1.w3);
             // default: no identity in the result
             assert (b2.w1 != b2.w2);
             assert (b2.w1 == b2.w3);
             assert (b2.w2 != b2.w3);
             // default: no identity in the result
             assert (b3.w1 == b3.w2);
             assert (b3.w1 == b3.w3);
             assert (b3.w2 == b3.w3);
         }
     }
 }
