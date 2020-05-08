 package seg.jUCMNav.importexport.z151.unmarshal;
 
 //<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ -->
 //<!--  URNspec  -->
 //<!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ -->
 //<xsd:complexType name="URNspec">
 //  <xsd:sequence>
 //    <xsd:element name="name" type="xsd:string"/>
 //    <xsd:element minOccurs="0" name="ucmspec" type="UCMspec"/>
 //    <xsd:element maxOccurs="unbounded" minOccurs="0" name="metadata" type="Metadata"/>
 //    <xsd:element maxOccurs="unbounded" minOccurs="0" name="urnLinks" type="URNlink"/>
 //    <xsd:element minOccurs="0" name="grlspec" type="GRLspec"/>
 //    <xsd:element minOccurs="0" name="info" type="ConcreteURNspec"/>
 //    <xsd:element maxOccurs="unbounded" minOccurs="0" name="concerns" type="Concern"/>
 //  </xsd:sequence>
 //</xsd:complexType>
 
 import seg.jUCMNav.importexport.z151.generated.URNspec;
 import seg.jUCMNav.model.ModelCreationFactory;
 import seg.jUCMNav.model.util.MetadataHelper;
 import urncore.URNdefinition;
 
 public class URNspecUMHandler extends EObjectImplUMHandler {
 	public Object handle(Object obj, Object target, boolean isFullConstruction) {
 		URNspec elemZ = (URNspec) obj;
 		urncore.URNdefinition urnDef = null;
 		urn.URNspec elem = null;
 		if (null == elem) {
 			if (null == target) {
				elem = (urn.URNspec) ModelCreationFactory.getNewURNspec(false, false);
 				//elem = (urn.URNspec) ModelCreationFactory.getNewObject(urn,
 					//	urn.URNspec.class);
 				urn = elem;
 				urnDef = urn.getUrndef();
 				if (urnDef==null) urnDef = (URNdefinition) ModelCreationFactory.getNewObject(urn,
 						urncore.URNdefinition.class);
 			} else
 				elem = (urn.URNspec) target;
 		}
 		if (isFullConstruction) {
 			elem.setGrlspec((grl.GRLspec) process(elemZ.getGrlspec(), null,
 					true));
 			elem.setModified(elemZ.getInfo().getModified());
 			elem.setAuthor(elemZ.getInfo().getAuthor());
 			elem.setSpecVersion(elemZ.getInfo().getSpecVersion());
 			elem.setUrnVersion(elemZ.getInfo().getUrnVersion());
 //TODO			elem.setNextGlobalID();
 			elem.setUcmspec((ucm.UCMspec) process(elemZ.getUcmspec(), null, true));
 
 			urnDef.setUrnspec(elem);
 			//urnDef.getComponents(); handled by UCMspecUMHandler
 			//urnDef.getComponentTypes(); handled by UCMspecUMHandler
 			processList(elemZ.getConcerns(), urnDef.getConcerns(), true);
 			//urnDef.getResponsibilities(); handled by UCMspecUMHandler
 			//urnDef.getSpecDiagrams(); handled by GRLspecUMHandler and UCMspecUMHandler 
 
 			elem.setUrndef(urnDef);
 			elem.setName(elemZ.getName());
 			elem.setDescription(elemZ.getInfo().getDescription());
 			elem.setCreated(elemZ.getInfo().getCreated());
 			
 			// elem.getGrlspec();
 			processList(elemZ.getMetadata(), elem.getMetadata(), true);
 			// elem.getAuthor();
 			// elem.getCreated();
 			// elem.getModified();
 			// elem.getSpecVersion();
 			// elem.getUrnVersion();
 			// elem.getNextGlobalID();
 			// elem.getUcmspec();
 			// elem.getUrndef();
 			processList(elemZ.getUrnLinks(), elem.getUrnLinks(), true);
 			
 			// if there is many metadata named _Use0to100EvaluationRange, keep the last one
 			String value = MetadataHelper.getLastMetaData(elem, "_Use0to100EvaluationRange");
 			if (value !=null) {
 				MetadataHelper.removeMetaData(elem, "_Use0to100EvaluationRange"); // removes them all
 				MetadataHelper.addMetaData(elem, "_Use0to100EvaluationRange", value);
 			}
 			
 			// elem.getName();
 			// elem.getDescription();
 			// elem.getClass();
 			elem.setNextGlobalID(""+(Integer.parseInt(globelId)+2)); //$NON-NLS-1$
 			urn = elem;
 		}
 		return elem;
 	}
 }
