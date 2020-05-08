 package niagaraGUITests;
 
 import niagaraGUI.*;
 import org.junit.Test;
 import org.junit.Before;
 import org.junit.After;
 import static org.junit.Assert.*;
 import java.util.Hashtable;
 import java.util.Vector;
 
 
 public class NiagaraGUIXMLTests {
     DTDInterpreter intNothingFancy;
     Vector<String> operatorStrings;
     
     @Before public void setUp() { 
         //Set up the different kinds of interpreters we'll be using
         intNothingFancy = new DTDInterpreter("exampleFile1.dtd");
         
         
         //Set up different sets of operator names we'll be checking for
         operatorStrings = new Vector<String>();
         /** This is messy and derived pretty much from a copy-pasta from the default DTD.
          * I couldn't remember a good way to get a ton of stuff into a Collection.
          * @todo Clean this junk up
          */
         operatorStrings.add("select"); operatorStrings.add("unnest"); operatorStrings.add("sort"); operatorStrings.add("expression"); operatorStrings.add("avg");
         operatorStrings.add("slidingAvg"); operatorStrings.add("sum"); operatorStrings.add("slidingSum"); operatorStrings.add("max");
         operatorStrings.add("slidingMax"); operatorStrings.add("count"); operatorStrings.add("slidingCount"); operatorStrings.add("incrmax"); operatorStrings.add("incravg"); operatorStrings.add("select"); operatorStrings.add("dup"); operatorStrings.add("union"); operatorStrings.add("join");
         operatorStrings.add("dtdscan"); operatorStrings.add("resource"); operatorStrings.add("constant"); operatorStrings.add("timer");
         operatorStrings.add("prefix"); operatorStrings.add("punctuate"); operatorStrings.add("send"); operatorStrings.add("display"); operatorStrings.add("topn"); 
         operatorStrings.add("firehosescan"); operatorStrings.add("filescan"); operatorStrings.add("construct");
         operatorStrings.add("bucket"); operatorStrings.add("partitionavg"); operatorStrings.add("partitionmax");
         operatorStrings.add("accumulate"); operatorStrings.add("nest"); operatorStrings.add("magic_construct");
        operatorStrings.add("windowCount"); operatorStrings.add("windowMax"); operatorStrings.add("windowAverage "); operatorStrings.add("windowJoin");
         operatorStrings.add("store"); operatorStrings.add("load"); operatorStrings.add("xmlscan");
     }
     
     
     //Tests only that loading a DTDInterpreter with a filename will place the correct keys
     //into the hashtable.  This doesn't verify that the corresponding buckets have the correct
     // objects in them.
     @Test
     public void testOperatorTemplateTablePopulationFromConstructorSuccessPresentOnly() {
         Hashtable<String, OperatorTemplate> table = intNothingFancy.getTemplates();
         for (String s : operatorStrings) {
             assertTrue("Operator "+s+" not found (could be others missing)", table.containsKey(s));
         }
     }
 
 }
