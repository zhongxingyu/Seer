 package eu.esdihumboldt.cst.corefunctions.inspire;
 
 
 
 import java.util.Collection;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.junit.Test;
 import org.opengis.feature.Feature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.FeatureType;
 
 import eu.esdihumboldt.cst.corefunctions.util.TypeLoader;
 import eu.esdihumboldt.goml.align.Cell;
 import eu.esdihumboldt.goml.oml.ext.Parameter;
 import eu.esdihumboldt.goml.oml.ext.Transformation;
 import eu.esdihumboldt.goml.omwg.Property;
 import eu.esdihumboldt.goml.rdf.About;
 import eu.esdihumboldt.goml.rdf.Resource;
 import eu.esdihumboldt.inspire.data.InspireIdentifier;
 
 
 public class IdentifierTest {
 	private final String sourceLocalname = "FT1";
 	private final String sourceLocalnamePropertyAID = "PropertyAID";
 	private final String sourceNamespace = "http://esdi-humboldt.eu";
 	
 	private final String targetLocalname = "FT2";
 	private final String targetLocalnamePropertyBID = "inspireId";
 	private final String targetNamespace = "http://xsdi.org";
 	
 	private String localID = "1";
 	private String countryname = "de";
 	private String provName = "TUM";
 	private String prodName = "SomeProduct";
 	private String version = "150782";
 	
 	@Test
 	public void testTransformTypeBinding() {
 		Cell cell = this.getCell();
 		
 		// build source and target Features
 		SimpleFeatureType sourcetype = this.getFeatureType(
 				this.sourceNamespace, 
 				this.sourceLocalname, 
 				new String[]{this.sourceLocalnamePropertyAID});
 		
 		String url = getClass().getResource(
				"inspire_v3.0_xsd/CadastralParcels.xsd").toString();
 			FeatureType targettype = TypeLoader.getType("CadastralZoning", url);
 		Feature source = SimpleFeatureBuilder.build(
 				sourcetype, new Object[]{"ID1"}, localID);
 		Feature target = SimpleFeatureBuilder.build(
 				(SimpleFeatureType) targettype, new Object[]{}, "2");
 		
 		// perform actual test
 		IdentifierFunction idf = new IdentifierFunction();
 		idf.configure(cell);
 
 		Feature transformed = idf.transform(source, target);
 		Object o = transformed.getProperty(this.targetLocalnamePropertyBID).getValue();
 		assertNotNull(o);
 		Object identifier = ((Collection)o).iterator().next();
 		assertNotNull(identifier);
 		
 	}
 	
 
 	@Test
 	public void testTransformFeatureFeatureStringBinding() {
 		// set up cell to use for testing
 		Cell cell = this.getCell();
 
 		// build source and target Features
 		SimpleFeatureType sourcetype = this.getFeatureType(
 				this.sourceNamespace, 
 				this.sourceLocalname, 
 				new String[]{this.sourceLocalnamePropertyAID});
 		SimpleFeatureType targettype = this.getFeatureType(
 				this.targetNamespace, 
 				this.targetLocalname, 
 				new String[]{this.targetLocalnamePropertyBID});
 		Feature source = SimpleFeatureBuilder.build(
 				sourcetype, new Object[]{"ID1"}, localID);
 		Feature target = SimpleFeatureBuilder.build(
 				targettype, new Object[]{}, "2");
 
 		// perform actual test
 		IdentifierFunction idf = new IdentifierFunction();
 		idf.configure(cell);
 
 		Feature neu = idf.transform(source, target);
 		System.out.println("With String Binding: "+neu.getProperty(
 				this.targetLocalnamePropertyBID).getValue().toString());
 		assertTrue(neu.getProperty(this.targetLocalnamePropertyBID).getValue()
 				.toString().equals(
 						IdentifierFunction.INSPIRE_IDENTIFIER_PREFIX + ":"
 								+ this.countryname + ":" + this.provName + ":"
 								+ this.prodName + ":" + this.sourceLocalname
 								+ ":" + this.localID + ":" + this.version));
 
 	}
 	
 	@Test
 	public void testTransformFeatureFeatureInspireIdentifierBinding() {
 
 		Cell cell = this.getCell();
 
 		// build source and target Features
 		SimpleFeatureTypeBuilder ftbuilder = new SimpleFeatureTypeBuilder();
 		ftbuilder.setName(this.sourceLocalname);
 		ftbuilder.setNamespaceURI(this.sourceNamespace);
 		ftbuilder.add(this.sourceLocalnamePropertyAID, String.class);
 		SimpleFeatureType sourcetype = ftbuilder.buildFeatureType();
 		
 		SimpleFeatureTypeBuilder ftbuilder2 = new SimpleFeatureTypeBuilder();
 		ftbuilder2.setName(this.targetLocalname);
 		ftbuilder2.setNamespaceURI(this.targetNamespace);
 		ftbuilder2.add(this.targetLocalnamePropertyBID, InspireIdentifier.class);
 		SimpleFeatureType targettype = ftbuilder2.buildFeatureType();
 		
 
 		Feature source = SimpleFeatureBuilder.build(
 				sourcetype, new Object[]{"ID1"}, "1");
 		Feature target = SimpleFeatureBuilder.build(
 				targettype, new Object[]{}, "2");
 
 		// perform actual test
 		IdentifierFunction idf = new IdentifierFunction();
 		idf.configure(cell);
 
 		Feature neu = idf.transform(source, target);
 		
 		
 		InspireIdentifier itemp = (InspireIdentifier)neu.getProperty(
 				this.targetLocalnamePropertyBID).getValue();
 		System.out.println("With Inspire Identifier Binding: " + IdentifierFunction.INSPIRE_IDENTIFIER_PREFIX+":"+itemp.getNameSpace()+":"+itemp.getLocalID()+":"+itemp.getVersionID());
 		
 		
 		InspireIdentifier expectedII = this.getInspireIDToCheck();
 		// this is the geographical name result
 		InspireIdentifier receivedII = (InspireIdentifier)target.getProperty(
 				this.targetLocalnamePropertyBID).getValue();
 		//check value
 		assertTrue(receivedII.equals(expectedII));
 
 	}
 	
 	private SimpleFeatureType getFeatureType(String featureTypeNamespace, 
 			String featureTypeName, String[] propertyNames) {
 	
 		SimpleFeatureType ft = null;
 		try {
 			SimpleFeatureTypeBuilder ftbuilder = new SimpleFeatureTypeBuilder();
 			ftbuilder.setName(featureTypeName);
 			ftbuilder.setNamespaceURI(featureTypeNamespace);
 			for (String s : propertyNames) {
 				ftbuilder.add(s, String.class);
 			}
 			ft = ftbuilder.buildFeatureType();
 		} catch (Exception ex) {
 			throw new RuntimeException(ex);
 		}
 		return ft;
 	}
 	
 	
 	private InspireIdentifier getInspireIDToCheck()
 	{
 		// build the expected inspireIdentifier as result
 		InspireIdentifier ii = new InspireIdentifier();
 		ii.setLocalID(localID);
 		ii.setNameSpace(this.countryname+":"+this.provName+":"+this.prodName+":"+this.sourceLocalname);
 		ii.setVersionID(this.version);
 		return ii;
 	}
 	
 	private Cell getCell() {
 		Cell cell = new Cell();
 		Transformation t = new Transformation();
 		t.setService(new Resource(IdentifierFunction.class.toString()));
 		t.getParameters().add(new Parameter("countryName", this.countryname));
 		t.getParameters().add(new Parameter("providerName", this.provName));
 		t.getParameters().add(new Parameter("productName", this.prodName));
 		t.getParameters().add(new Parameter("version", this.version));
 		Property p1 = new Property(new About(this.sourceNamespace, this.sourceLocalname, this.sourceLocalnamePropertyAID));
 		p1.setTransformation(t);
 		cell.setEntity1(p1);
 		cell.setEntity2(new Property(new About(this.targetNamespace, this.targetLocalname, this.targetLocalnamePropertyBID)));
 		return cell;
 	}
 }
