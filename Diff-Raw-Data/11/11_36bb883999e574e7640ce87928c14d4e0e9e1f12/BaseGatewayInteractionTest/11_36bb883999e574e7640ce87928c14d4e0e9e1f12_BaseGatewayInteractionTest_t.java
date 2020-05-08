 package com.sociodyne.validation.gateway;
 
import com.sociodyne.edi.parser.EdiXmlAdapter;
 import com.sociodyne.test.Mock;
 import com.sociodyne.test.MockTest;
 import com.sociodyne.test.gateway.TestGatewayInteraction;
 
 import java.io.StringReader;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 
 public abstract class BaseGatewayInteractionTest<T extends TestGatewayInteraction>
     extends MockTest {
 	protected T interaction;
 
 	protected @Mock Logger logger;
 	
 	protected abstract Map<String,String> createFakeProperties();
 
 	protected abstract T createInteraction(byte[] response);
 
 	protected abstract byte[] createDefaultResponse();
 
 	protected abstract String getReceiver();
 
 	protected String extractBuffer(byte[] buffer) throws Exception {
 		return new String(buffer);
 	}
 
 	protected byte[] createBuffer(String string) throws Exception {
 		return string.getBytes();
 	}
 
 	protected Document createEmptyEDIDocument() throws Exception {
 		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><edi xmlns=\""
 		    + EdiXmlAdapter.NAMESPACE_URI + "\"/>";
 		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
 	}
 
 	public void testSimpleSubmit() throws Exception {
 	  replay();
 
 		TestGatewayInteraction interaction = createInteraction(createDefaultResponse());
 		interaction.open(createFakeProperties());
 		Document document = createEmptyEDIDocument();
 		Document response = interaction.submit(document);
 		byte[] requestBytes = interaction.getRequest();
 		String bufferString = extractBuffer(requestBytes); 
 		assertEquals(0, bufferString.length());
 		assertNotNull("Expected a valid response", response);
 	}
 
 	public void testEarlySubmit() throws Exception {
 	  replay();
 
 		try {
 			GatewayInteraction interaction = createInteraction(createDefaultResponse());
 			Document doc = createEmptyEDIDocument();
 			interaction.submit(doc);
 			fail("Expected an IllegalStateException");
 		} catch ( IllegalStateException e ) {
 			// Success!
 		}
 	}
 
 	public void testReceiveResponse() throws Exception {
 	  replay();
 
 		// From the CMS companion guide, Appendix B
 		String edi = "ISA*00*          *00*          *ZZ*CMS         *ZZ*T000000011     *090326*1705*U*00401*002417703*0*T*|~GS*HB*CMS*T000000011*20090326*1705*2381770*X*004010X092A1~ST*271*2377396~BHT*0022*11*ALL*20090326*17055389~HL*1**20*1~NM1*PR*2*CMS*****PI*CMS~HL*2*1*21*1~NM1*1P*2*TEST*****XX*1234567893~HL*3*2*22*0~TRN*2*TEST-TEST*9TEST     ~NM1*IL*1*SMITH*MARY****MI*123456789A~N3*123 MAIN STREET*~N4*ANYTOWN*MD*999999999~DMG*D8*19240901*F~INS*Y*18*001*25~DTP*307*RD8*20070101-20090325~EB*1*IND**MA~DTP*307*D8*19890801~EB*K**47*MA**33***LA*60~EB*C**47*MA**29*992~EB*F**47*MA**29***DY*60~EB*B**47*MA**29*248**DY*30~EB*F**AG*MA**29***DY*20~EB*B**AG*MA**29*124**DY*80~EB*1*IND**MB~DTP*307*D8*19890801~EB*C**96*MB**29*0~DTP*292*RD8*20090101-20091231~EB*C**96*MB**29*0~DTP*292*RD8*20080101-20081231~EB*C**96*MB**29*0~DTP*292*RD8*20070101-20071231~EB*D*IND**MB*********HC|G0389~DTP*348*D8*20070701~EB*D*IND**MB*********HC|77057~DTP*348*D8*20070101~EB*D*IND**MB*********HC|82270~DTP*348*D8*20070101~EB*D*IND**MB*********HC|Q0091~DTP*348*D8*20050701~EB*D*IND**MB*********HC|82951~DTP*348*D8*20050101~EB*D*IND**MB*********HC|84478~DTP*348*D8*20050101~EB*D*IND**MB*********HC|82950~DTP*348*D8*20050101~EB*D*IND**MB*********HC|82947~DTP*348*D8*20050101~EB*D*IND**MB*********HC|82465~DTP*348*D8*20050101~EB*D*IND**MB*********HC|80061~DTP*348*D8*20050101~EB*D*IND**MB*********HC|83718~DTP*348*D8*20050101~EB*D*IND**MB*********HC|G0202~DTP*348*D8*20040201~EB*D*IND**MB*********HC|G0328~DTP*348*D8*20040101~EB*D*IND**MB*********HC|G0118~DTP*348*D8*20020101~EB*D*IND**MB*********HC|G0117~DTP*348*D8*20020101~EB*D*IND**MB*********HC|G0101~DTP*348*D8*20010701~EB*D*IND**MB*********HC|P3000~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0148~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0147~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0145~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0144~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0143~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0121~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0123~DTP*348*D8*20010701~EB*D*IND**MB*********HC|G0105~DTP*348*D8*19980101~EB*D*IND**MB*********HC|G0106~DTP*348*D8*19980101~EB*D*IND**MB*********HC|82270~DTP*348*D8*19980101~EB*D*IND**MB*********HC|G0104~DTP*348*D8*19980101~EB*D*IND**MB*********HC|G0120~DTP*348*D8*19980101~EB*D*IND**MB*********HC|90732~DTP*348*D8*19890801~EB*F*IND*67*MB**29***P6*8~EB*F*IND*AD*MB**29*1840~DTP*292*RD8*20090101-20091231~EB*F*IND*AE*MB***29*1840~DTP*292*RD8*20090101-20091231~EB*F*IND*AD*MB**29*1810~DTP*292*RD8*20080101-20081231~EB*F*IND*AE*MB***29*1810~DTP*292*RD8*20080101-20081231~EB*F*IND*AD*MB**29*1780~DTP*292*RD8*20070101-20071231~EB*F*IND*AE*MB***29*1780~DTP*292*RD8*20070101-20071231~EB*X**45*MA**26~DTP*292*RD8*20080208-20081217~LS*2120~NM1*1P*2******XX*1427038314~LE*2120~EB*X**45*MA**26~DTP*292*D8*20061016~LS*2120~NM1*1P*2******SV*031549~LE*2120~EB*C*IND*10***29***DB*3~DTP*292*RD8*20090101-20091231~EB*C*IND*10***29***DB*3~DTP*292*RD8*20080101-20081231~EB*C*IND*10***29***DB*3~DTP*292*RD8*20070101-20071231~EB*R**88*OT~REF*18*H9999 999~DTP*292*D8*20070801~LS*2120~NM1*PR*2*ABC HEALTH PLAN~N3*123 MAIN STREET~N4*ANYTOWN*MD*999999999~PER*IC**TE*9999999999~LE*2120~EB*R**88*OT~REF*18*S9999 999~DTP*292*RD8*20070101-20070731~LS*2120~NM1*PR*2*ABC INSURANCE COMPANY~N3*123 MAIN STREET~N4*ANYTOWN*MD*999999999~PER*IC**TE*9999999999~LE*2120~EB*R**30*HN~REF*18*H9999 999~DTP*290*D8*20070801~LS*2120~NM1*PRP*2*ABC HEALTH PLAN~N3*123 MAIN STREET~N4*ANYTOWN*MD*999999999~PER*IC**TE*9999999999~LE*2120~EB*R*IND*30*14~DTP*290*D8*19960912~LS*2120~NM1*PRP*2*SMITH~LE*2120~SE*156*2377396~GE*1*2381770~IEA*1*002417703~";
 		Document document = createEmptyEDIDocument();
 		byte[] responseBytes = createBuffer(edi);
 		TestGatewayInteraction interaction = createInteraction(responseBytes);
 		interaction.open(createFakeProperties());
 		Document response = interaction.submit(document);
 		assertNotNull("Expected a valid response", response);
 	}
 }
