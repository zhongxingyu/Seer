 package com.miravtech.checksbgn;
 
 import java.util.List;
 
 import javax.xml.bind.JAXBException;
 
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 import org.xml.sax.SAXException;
 
 import com.miravtech.checksbgn.CheckReport.ERRORCODES;
 
 public class CheckerTest {
 
 	@BeforeClass
 	public void setup() {
 
 	}
 
 	@Test
 	public void TestInvalidSchema() throws JAXBException, SAXException {
 		Checker c = new Checker();
 		List<CheckReport> ret = c.check(CheckerTest.class
 				.getResource("/invalidschema.xml"));
 		// System.out.println(ret.get(0));
 
 		if (ret.size() != 1)
 			throw new RuntimeException("One error was expected!");
 		if (ret.get(0).code != ERRORCODES.ERROR_SAX_PARSING)
 			throw new RuntimeException("Sax parsing error expected!");
 	}
 
 	@Test
 	public void TestReplicateID() throws JAXBException, SAXException {
 		Checker c = new Checker();
 		List<CheckReport> ret = c.check(CheckerTest.class
 				.getResource("/replicatedID.xml"));
 		if (ret.size() != 1)
 			throw new RuntimeException("One error was expected!");
 		if (ret.get(0).code != ERRORCODES.ERROR_DUPLICATE_ID)
 			throw new RuntimeException("ERROR_DUPLICATE_ID error expected!");
 	}
 
 	@Test
 	public void TestOK() throws JAXBException, SAXException {
 		Checker c = new Checker();
 		List<CheckReport> ret = c.check(CheckerTest.class
 				.getResource("/dectin1.xml"));
 		for (CheckReport r : ret)
 			System.out.println(r.toString());
 
 		if (ret.size() != 0)
 			throw new RuntimeException("No error was expected!");
 	}
 
 	@Test
 	public void TestInvalidCloneRef() throws JAXBException, SAXException {
 		Checker c = new Checker();
 		List<CheckReport> ret = c.check(CheckerTest.class
 				.getResource("/invalidcloneref.xml"));
 		if (ret.size() != 1)
 			throw new RuntimeException("One error was expected!");
 		if (ret.get(0).code != ERRORCODES.ERROR_INVALID_CLONE_REF)
 			throw new RuntimeException(
 					"ERROR_INVALID_CLONE_REF error expected!");
 
 	}
 
 	@Test
 	public void TestInvalidCloneRefType() throws JAXBException, SAXException {
 		Checker c = new Checker();
 		List<CheckReport> ret = c.check(CheckerTest.class
 				.getResource("/invalidclonereftype.xml"));
 		if (ret.size() != 1)
 			throw new RuntimeException("One error was expected!");
 		if (ret.get(0).code != ERRORCODES.ERROR_DIFFERENT_CLONE_TYPES)
 			throw new RuntimeException(
 					"ERROR_INVALID_CLONE_REF error expected!");
 	}
 
 	/**
 	 * Test for a bug found, no checking for nodes with empty ID
 	 * 
 	 * @throws JAXBException
 	 * @throws SAXException
 	 */
 	@Test
 	public void TestInvalidCloneRefType2() throws JAXBException, SAXException {
 		Checker c = new Checker();
 		List<CheckReport> ret = c.check(CheckerTest.class
 				.getResource("/invalidclonereftype2.xml"));
 		if (ret.size() != 2)
 			throw new RuntimeException("Two errors was expected!");
 	}
 
 	@Test
 	public void InvalidEquivalenceNode() throws JAXBException, SAXException {
 		Checker c = new Checker();
 		List<CheckReport> ret = c.check(CheckerTest.class
 				.getResource("/InvalidEquivalenceArc.xml"));
 		if (ret.size() != 2)
 			throw new RuntimeException("One error was expected!");
 		if (ret.get(0).code != ERRORCODES.ERROR_EQUIVALENCE_ARC_MUST_LINK_A_TAG)
 			throw new RuntimeException(
 					"ERROR_EQUIVALENCE_ARC_MUST_LINK_A_TAG error expected!");
 		if (ret.get(1).code != ERRORCODES.ERROR_EQUIVALENCE_ARC_MUST_LINK_A_TAG)
 			throw new RuntimeException(
 					"ERROR_EQUIVALENCE_ARC_MUST_LINK_A_TAG error expected!");
 
 	}
 
 	@Test
 	public void TestInvalidSubmap() throws JAXBException, SAXException {
 		Checker c = new Checker();
 		List<CheckReport> ret = c.check(CheckerTest.class
 				.getResource("/invalid_submap_test.xml"));
 		if (ret.size() != 1)
 			throw new RuntimeException("One error was expected!");
 		if (ret.get(0).code != ERRORCODES.SUBMAP_CAN_ONLY_CONTAIN_LABELS)
 			throw new RuntimeException(
 					"SUBMAP_CAN_ONLY_CONTAIN_LABELS error expected!");
 	}
 
 	@Test
 	public void TestInvalidNodeContainment() throws JAXBException, SAXException {
 		Checker c = new Checker();
 		List<CheckReport> ret = c.check(CheckerTest.class
 				.getResource("/AuxiliaryNodeInvalidContent.xml"));
 		if (ret.size() != 1)
 			throw new RuntimeException("One error was expected!");
 		if (ret.get(0).code != ERRORCODES.NODE_CANNOT_CONTAIN_OTHER_GLYPHS)
 			throw new RuntimeException(
 					"NODE_CANNOT_CONTAIN_OTHER_GLYPHS error expected!");
 	}
 
 	@Test
 	public void TestInvalidProcessContainment() throws JAXBException,
 			SAXException {
 		Checker c = new Checker();
 		List<CheckReport> ret = c.check(CheckerTest.class
 				.getResource("/Auxiliary_ProcessInvalidContent.xml"));
 		if (ret.size() != 1)
 			throw new RuntimeException("One error was expected!");
 		if (ret.get(0).code != ERRORCODES.PROCESSES_CANNOT_CONTAIN_OTHER_NODES)
 			throw new RuntimeException(
 					"PROCESSES_CANNOT_CONTAIN_OTHER_NODES error expected!");
 	}
 
 	@Test
 	public void TestInvalidNodeInEPN() throws JAXBException, SAXException {
 		Checker c = new Checker();
 		List<CheckReport> ret = c.check(CheckerTest.class
 				.getResource("/invalidnodecontainedinepn.xml"));
 		if (ret.size() != 1)
 			throw new RuntimeException("One error was expected!");
 		if (ret.get(0).code != ERRORCODES.INVALID_NODE_CONTAINED_IN_EPN)
 			throw new RuntimeException(
 					"INVALID_NODE_CONTAINED_IN_EPN error expected!");
 	}
 
 	@Test
 	public void TestEPNInEPN() throws JAXBException, SAXException {
 		Checker c = new Checker();
 		List<CheckReport> ret = c.check(CheckerTest.class
 				.getResource("/noncomplexcannothaveepn.xml"));
 		if (ret.size() != 1)
 			throw new RuntimeException("One error was expected!");
 		if (ret.get(0).code != ERRORCODES.NON_COMPLEX_EPN_CANNOT_CONTAIN_EPN)
 			throw new RuntimeException(
 					"NON_COMPLEX_EPN_CANNOT_CONTAIN_EPN error expected!");
 	}
 
 	@Test
 	public void TestBadMultimer() throws JAXBException, SAXException {
 		Checker c = new Checker();
 		List<CheckReport> ret = c.check(CheckerTest.class
 				.getResource("/badmultimer.xml"));
 		if (ret.size() != 1)
 			throw new RuntimeException("One error was expected!");
 		if (ret.get(0).code != ERRORCODES.ERROR_THIS_EPN_CANNOT_BE_MULTIMER)
 			throw new RuntimeException(
 					"ERROR_THIS_EPN_CANNOT_BE_MULTIMER error expected!");
 	}
 
 	@Test
 	public void TestBadSink() throws JAXBException, SAXException {
 		Checker c = new Checker();
 		List<CheckReport> ret = c.check(CheckerTest.class
 				.getResource("/badsink.xml"));
 		if (ret.size() != 1)
 			throw new RuntimeException("One error was expected!");
 		if (ret.get(0).code != ERRORCODES.ERROR_SINK_HAS_ONLY_PRODUCTION)
 			throw new RuntimeException(
 					"ERRORCODES.ERROR_SINK_HAS_ONLY_PRODUCTION error expected!");
 	}
 
 	@Test
 	public void TestBadSource() throws JAXBException, SAXException {
 		Checker c = new Checker();
 		List<CheckReport> ret = c.check(CheckerTest.class
 				.getResource("/badsource.xml"));
 		if (ret.size() != 1)
 			throw new RuntimeException("One error was expected!");
 		if (ret.get(0).code != ERRORCODES.ERROR_SOURCE_HAS_ONLY_CONSUMPTION)
 			throw new RuntimeException(
 					"ERRORCODES.ERROR_SOURCE_HAS_ONLY_CONSUMPTION error expected!");
 	}
 
 	@Test
 	public void TestNucleicAcidCannotBecatalyst() throws JAXBException,
 			SAXException {
 		Checker c = new Checker();
 		List<CheckReport> ret = c.check(CheckerTest.class
 				.getResource("/RNA_catalyst.xml"));
 		if (ret.size() != 1)
 			throw new RuntimeException("One error was expected!");
 		if (ret.get(0).code != ERRORCODES.ERROR_NUCLEIC_ACID_CANNOT_BE_CATALYST)
 			throw new RuntimeException(
 					"ERRORCODES.ERROR_NUCLEIC_ACID_CANNOT_BE_CATALYST error expected!");
 	}
 
	@Test
	public void TestCyclicCloneref() throws JAXBException, SAXException {
		Checker c = new Checker();
		List<CheckReport> ret = c.check(CheckerTest.class
				.getResource("/cyclic_cloneref.xml"));
		if (ret.size() != 3)
			throw new RuntimeException("One error was expected!");
		if (ret.get(0).code != ERRORCODES.ERROR_CYCLIC_CLONE_DEF)
			throw new RuntimeException("ERROR_CYCLIC_CLONE_DEF error expected!");
		if (ret.get(1).code != ERRORCODES.ERROR_CYCLIC_CLONE_DEF)
			throw new RuntimeException("ERROR_CYCLIC_CLONE_DEF error expected!");
		if (ret.get(2).code != ERRORCODES.ERROR_CYCLIC_CLONE_DEF)
			throw new RuntimeException("ERROR_CYCLIC_CLONE_DEF error expected!");
	}

 	// TODO ERRORCODES.ERROR_PROCESS_CANNOT_HAVE_LOGIC_OR_EQUIVALENCE_ARCS
 	// TODO ERRORCODES.ERROR_NOT_NODES_SHOULDHAVE2ARCS_AND_ONE_LOGIC
 	// TODO ERRORCODES.INVALID_LOGICAL_OPERATOR_NODE_CONNECTIONS
 
 	// TODO ERRORCODES.INVALID_CONNECTIONS_ON_TAG_NODE
 	// TODO THIS_PROCESS_MUST_HAVE_AT_LEAST_ONE_CONSUMPTION_AND_ONE_PRODUCTION
 
 	// TODO COMPARTMENTS_CANNOT_BE_CONTAINED
 
 }
