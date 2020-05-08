 package org.eclipse.rmf.reqif10.tests.uc000.tc4000;
 
 import static org.junit.Assert.assertFalse;
 
 import java.util.Iterator;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xml.type.AnyType;
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.eclipse.rmf.reqif10.tests.util.AbstractTestCase;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TC0004000ToolExtensionWithEcoreMetamodelTests extends AbstractTestCase {
 	static final String TEST_CASE_ID = "TC0004000";
 	static final String REFERENCE_DATA_FILENAME = getWorkingFileName(getReferenceDataFileName(TEST_CASE_ID, false));
 	static final String EXPORT_DATA_FILENAME = getWorkingFileName(getFirstExportFileName(TEST_CASE_ID, false));
 	static ReqIF originalReqIF = null;
 	static ReqIF loadedReqIF = null;
 
 	@BeforeClass
 	public static void setupOnce() throws Exception {
 		AbstractTestCase.setupOnce();
 		originalReqIF = new TC0004000ToolExtensionWithEcoreMetamodelModelBuilder().getReqIF();
 		saveReqIFFile(originalReqIF, REFERENCE_DATA_FILENAME);
 		loadedReqIF = loadReqIFFile(REFERENCE_DATA_FILENAME);
 	}
 
 	@Test
 	public void testNoXMLAnyType() {
 		Iterator<EObject> iterator = EcoreUtil.getAllContents(loadedReqIF, true);
 		while (iterator.hasNext()) {
 
 			EObject eObject = iterator.next();
 			System.out.println("checking element " + eObject);
 			assertFalse(eObject instanceof AnyType);
 		}
 	}
 
 }
