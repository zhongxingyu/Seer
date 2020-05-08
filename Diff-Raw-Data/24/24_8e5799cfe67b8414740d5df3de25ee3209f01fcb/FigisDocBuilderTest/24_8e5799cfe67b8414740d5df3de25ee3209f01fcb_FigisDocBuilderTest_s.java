 package org.fao.fi.vme.sync2.mapping.xml;
 
 import java.util.List;
 
 import org.fao.fi.figis.devcon.FIGISDoc;
 import org.fao.fi.figis.devcon.HabitatBio;
 import org.fao.fi.figis.devcon.Impacts;
 import org.fao.fi.figis.devcon.Text;
 import org.fao.fi.figis.devcon.VME;
 import org.fao.fi.figis.devcon.VMEIdent;
 import org.fao.fi.vme.domain.Profile;
 import org.fao.fi.vme.domain.Vme;
 import org.fao.fi.vme.domain.util.MultiLingualStringUtil;
 import org.fao.fi.vme.test.VmeMock;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.vme.fimes.jaxb.JaxbMarshall;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 public class FigisDocBuilderTest {
 
 	FigisDocBuilder b = new FigisDocBuilder();
 	int nrOfYears = 2;
 	JaxbMarshall m = new JaxbMarshall();
 	MultiLingualStringUtil u = new MultiLingualStringUtil();
 
 	@Test
 	public void testSpecificMeasures() {
 		// TODO
 	}
 
 	@Test
 	public void testVmeHistory() {
 		// TODO
 	}
 
 	@Test
 	public void testRfmoHistory() {
 		// TODO
 	}
 
 	/**
 	 * 
 	 * descriptionBiological fi:FIGISDoc/fi:VME/fi:HabitatBio/fi:Text
 	 * 
 	 * descriptionPhysical fi:FIGISDoc/fi:VME/fi:HabitatBio/fi:GeoForm/fi:Text
 	 * 
 	 * descriptionImpact fi:FIGISDoc/fi:VME/fi:Impacts/fi
 	 */
	@Ignore
 	@Test
 	public void testProfile() {
 		String descriptionBiological = "111111111111111";
 		String descriptionPhysical = "22222222222222222";
 		String descriptionImpact = "333333333333333333333333333";
 
 		Profile profile = new Profile();
 		profile.setDescriptionBiological(u.english(descriptionBiological));
 		profile.setDescriptionPhisical(u.english(descriptionPhysical));
 		profile.setDescriptionImpact(u.english(descriptionImpact));
 
 		FIGISDoc figisDoc = new FIGISDoc();
 
 		// assumed the existence of VME.
 		figisDoc.setVME(new VME());
 
 		// perform the logic
 		b.profile(profile, figisDoc);
 
 		// test
 		List<Object> list = figisDoc.getVME().getOverviewsAndHabitatBiosAndImpacts();
 		assertEquals(2, list.size());
 
 		HabitatBio habitatBio = (HabitatBio) list.get(0);
 		Text text1 = (Text) habitatBio.getClimaticZonesAndDepthZonesAndDepthBehavs().get(0);
 		assertEquals(descriptionBiological, text1.getContent().get(0));
 
		Impacts Impacts = (Impacts) list.get(2);
 		Text text3 = (Text) Impacts.getTextsAndImagesAndTables().get(0);
 		assertEquals(descriptionImpact, text3.getContent().get(0));
 
 	}
 
 	@Test
 	public void testGeneralMeasures() {
 		// TODO
 	}
 
 	@Test
 	public void testVme() {
 		Vme vme = VmeMock.generateVme(nrOfYears);
 		FIGISDoc figisDoc = new FIGISDoc();
 		b.vme(vme, figisDoc);
 		assertNotNull(figisDoc.getVME());
 		assertNotNull(figisDoc.getVME().getVMEIdent());
 		String s = m.marshalToString(figisDoc);
 		System.out.println(s);
 		assertTrue(s.contains(VMEIdent.class.getSimpleName()));
 
 	}
 
 	@Test
 	public void testYear() {
 		// TODO
 	}
 
 }
