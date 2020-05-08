 package org.afpparser.afp.modca.triplets.fullyqualifiedname;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.afpparser.afp.modca.ParameterAsString;
 import org.afpparser.afp.modca.triplets.TripletIdentifiers;
 import org.afpparser.afp.modca.triplets.TripletTestCase;
 import org.junit.Before;
 import org.junit.Test;
 
 public class FQNCharStringDataTestCase extends TripletTestCase<FQNCharStringData> {
     private FQNCharStringData x;
     private FQNType type;
     private String expectedString;
     private int length;
 
     @Override
     @Before
     public void setUp() {
         expectedString = "Test String";
         length = 1;
         type = FQNType.font_charset_name_ref;
         this.x = new FQNCharStringData(length, expectedString, type);
         FQNCharStringData y = new FQNCharStringData(length, expectedString, type);
         FQNCharStringData z = new FQNCharStringData(length, expectedString, type);
         FQNCharStringData notEqual = new FQNCharStringData(length, "Not same string", type);
         setXYZ(x, y, z, notEqual);
     }
 
     @Test
     public void testGetters() {
         assertEquals(expectedString, x.getString());
         assertEquals(type, x.getFQNType());
         assertEquals(FQNFmt.character_string, x.getFormat());
         assertEquals(length, x.getLength());
         assertEquals(TripletIdentifiers.fully_qualified_name, x.getTid());
     }
 
     @Test
     @Override
     public void testGetParameters() {
         List<ParameterAsString> expectedParams = new ArrayList<ParameterAsString>();
        expectedParams.add(new ParameterAsString(type.name(), expectedString));
         parameterTester(expectedParams, x);
     }
 }
