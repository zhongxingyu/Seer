 package org.fao.fi.vme.sync2.mapping;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.fao.fi.figis.devcon.FIGISDoc;
 import org.fao.fi.figis.domain.ObservationDomain;
 import org.fao.fi.figis.domain.ObservationXml;
 import org.fao.fi.figis.domain.VmeObservationDomain;
 import org.fao.fi.vme.VmeException;
 import org.fao.fi.vme.domain.GeneralMeasures;
 import org.fao.fi.vme.domain.Profile;
 import org.fao.fi.vme.domain.SpecificMeasures;
 import org.fao.fi.vme.domain.Vme;
 import org.fao.fi.vme.domain.YearObject;
 import org.fao.fi.vme.sync2.mapping.xml.DefaultObservationXml;
 import org.fao.fi.vme.sync2.mapping.xml.FigisDocBuilder;
 import org.vme.fimes.jaxb.JaxbMarshall;
 
 /**
  * Stage A: domain objects without the year dimension: Vme and Rfmo.
  * 
  * Stage B: domain objects with the year dimension:
  * 
  * Vme trough History, SpecificMeasures, Profile
  * 
  * Vme-Rfmo through History, GeneralMesaures-InformationSource.
  * 
  * Stage B has only YearObject objects.
  * 
  * Algorithm steps: (1) Collect all YearObject objects. Generate the Figis objects per year.
  * 
  * 
  * 
  * @author Erik van Ingen
  * 
  */
 public class ObjectMapping {
 
 	private final YearGrouping groupie = new YearGrouping();
 	private final FigisDocBuilder figisDocBuilder = new FigisDocBuilder();
 	private final JaxbMarshall marshall = new JaxbMarshall();
 
 	public VmeObservationDomain mapVme2Figis(Vme vme) {
 		// precondition
 		if (vme.getRfmo() == null) {
 			throw new VmeException("Detected Vme without Rfmo");
 		}
 
 		// logic
 		Map<Integer, List<YearObject<?>>> map = groupie.collect(vme);// not processed here InformationSource, To be done
 		Object[] years = map.keySet().toArray();
 		List<ObservationDomain> odList = new ArrayList<ObservationDomain>();
 
 		// every year results in one observation in English
 		for (Object year : years) {
 			ObservationDomain od = new DefaultObservationDomain().defineDefaultObservationXml();
 			List<ObservationXml> observationsPerLanguage = new ArrayList<ObservationXml>();
 			od.setObservationsPerLanguage(observationsPerLanguage);
 			od.setReportingYear(year.toString());
 			odList.add(od);
 			ObservationXml xml = new DefaultObservationXml().defineDefaultObservationXml();
 			observationsPerLanguage.add(xml);
 
 			FIGISDoc figisDoc = new DefaultFigisDoc().defineDefaultFIGISDoc();
 			figisDocBuilder.vme(vme, figisDoc);
			figisDocBuilder.year(figisDoc, year);
 
 			// now we get all the year related objects for that vme. The observation gets filled up with the information
 			// for that year.
 			List<YearObject<?>> l = map.get(year);
 			for (YearObject<?> yearObject : l) {
 
 				if (yearObject instanceof SpecificMeasures) {
 					figisDocBuilder.specificMeasures((SpecificMeasures) yearObject, figisDoc);
 				}
 				if (yearObject instanceof VmeHistory) {
 					figisDocBuilder.vmeHistory((VmeHistory) yearObject, figisDoc);
 				}
 				if (yearObject instanceof RfmoHistory) {
 					figisDocBuilder.rfmoHistory((RfmoHistory) yearObject, figisDoc);
 				}
 				if (yearObject instanceof Profile) {
 					figisDocBuilder.profile((Profile) yearObject, figisDoc);
 				}
 				if (yearObject instanceof GeneralMeasures) {
 					figisDocBuilder.generalMeasures((GeneralMeasures) yearObject, figisDoc);
 				}
 			}
 			xml.setXml(marshall.marshalToString(figisDoc));
 		}
 		VmeObservationDomain vod = new VmeObservationDomain();
 		vod.setObservationDomainList(odList);
 		return vod;
 	}
 }
