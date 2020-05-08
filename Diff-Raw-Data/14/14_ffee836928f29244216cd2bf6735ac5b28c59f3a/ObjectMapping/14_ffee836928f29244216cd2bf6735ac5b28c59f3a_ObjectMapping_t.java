 package org.fao.fi.vme.sync2.mapping;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.fao.fi.figis.devcon.FIGISDoc;
 import org.fao.fi.figis.devcon.RelatedFisheries;
 import org.fao.fi.figis.domain.ObservationDomain;
 import org.fao.fi.figis.domain.ObservationXml;
 import org.fao.fi.figis.domain.VmeObservationDomain;
 import org.fao.fi.vme.VmeException;
 import org.fao.fi.vme.domain.Vme;
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
 
 	private final FigisDocBuilder figisDocBuilder = new FigisDocBuilder();
 	private final JaxbMarshall marshall = new JaxbMarshall();
 	private final PeriodGrouping groupie2 = new PeriodGrouping();
 	private final SliceDuplicateFilter filter = new SliceDuplicateFilter();
 	private final PrimaryRule primaryRule = new PrimaryRule();
 	private final PrimaryRuleValidator primaryRuleValidator = new PrimaryRuleValidator();
 
 	public VmeObservationDomain mapVme2Figis2(Vme vme) {
 
 		List<DisseminationYearSlice> slices = groupie2.collect(vme);
 
 		// filter out the duplicates.
 		filter.filter(slices);
 
 		List<ObservationDomain> odList = new ArrayList<ObservationDomain>();
 
 		for (DisseminationYearSlice disseminationYearSlice : slices) {
 			ObservationDomain od = new DefaultObservationDomain().defineDefaultObservation();
 			List<ObservationXml> observationsPerLanguage = new ArrayList<ObservationXml>();
 			od.setObservationsPerLanguage(observationsPerLanguage);
 			od.setReportingYear(String.valueOf(disseminationYearSlice.getYear()));
 			odList.add(od);
 
 			FIGISDoc figisDoc = new FIGISDoc();
 			figisDocBuilder.dataEntryObjectSource(disseminationYearSlice.getVme().getRfmo().getId(), figisDoc);
 			figisDocBuilder.vme(vme, disseminationYearSlice.getYear(), figisDoc);
 			figisDocBuilder.fisheryArea(disseminationYearSlice.getFisheryAreasHistory(), figisDoc);
 			figisDocBuilder.vmesHistory(disseminationYearSlice.getVmesHistory(), figisDoc);
 			figisDocBuilder.specificMeasures(disseminationYearSlice.getSpecificMeasures(), figisDoc);
 			figisDocBuilder.profile(disseminationYearSlice.getProfile(), figisDoc);
 			figisDocBuilder.generalMeasures(disseminationYearSlice.getGeneralMeasure(), figisDoc);
 			figisDocBuilder.informationSource(disseminationYearSlice.getInformationSourceList(), figisDoc);
 
 			ObservationXml xml = new DefaultObservationXml().define();
 
 			String xmlString = marshall.marshalToString(figisDoc);
 
			if (xmlString.contains(RelatedFisheries.class.getSimpleName()) || xmlString.contains("<fi:Text xsi:nil=")
					|| xmlString.contains("<fi:Impacts/>")) {
 				System.out.println(xmlString);
				throw new VmeException(
						"Vme XML contains RelatedFisheries, <fi:Text xsi:nil= or <fi:Impacts/> is not correct.");
 
 			}
 			xml.setXml(xmlString);
 			observationsPerLanguage.add(xml);
 		}
 		VmeObservationDomain vod = new VmeObservationDomain();
 		vod.setObservationDomainList(odList);
 		primaryRule.apply(vod);
 		primaryRuleValidator.validate(vod);
 		return vod;
 	}
 	/**
 	 * @deprecated
 	 * @param vme
 	 * @return
 	 */
 	// public VmeObservationDomain mapVme2Figis(Vme vme) {
 	// // precondition
 	// if (vme.getRfmo() == null) {
 	// throw new VmeException("Detected Vme without Rfmo");
 	// }
 	//
 	// // logic
 	// Map<Integer, List<Year<?>>> map = groupie.collect(vme);// not processed here InformationSource, To be done
 	// Object[] years = map.keySet().toArray();
 	// for (Object object : years) {
 	// System.out.println("---------------" + object + " vme = " + vme.getId() + vme.getInventoryIdentifier());
 	// }
 	//
 	// List<ObservationDomain> odList = new ArrayList<ObservationDomain>();
 	//
 	// // every year results in one observation in English
 	// for (Object year : years) {
 	// ObservationDomain od = new DefaultObservationDomain().defineDefaultObservation();
 	// List<ObservationXml> observationsPerLanguage = new ArrayList<ObservationXml>();
 	// od.setObservationsPerLanguage(observationsPerLanguage);
 	// od.setReportingYear(year.toString());
 	// odList.add(od);
 	// ObservationXml xml = new DefaultObservationXml().define();
 	// observationsPerLanguage.add(xml);
 	//
 	// FIGISDoc figisDoc = new FIGISDoc();
 	// figisDocBuilder.vme(vme, year, figisDoc);
 	// figisDocBuilder.year(year, figisDoc);
 	// figisDocBuilder.rfmo(vme.getRfmo(), figisDoc);
 	// figisDocBuilder.informationSource(vme.getRfmo().getInformationSourceList(), figisDoc);
 	//
 	// // now we get all the year related objects for that vme. Do not get confused here! Here we are processing 1
 	// // year and for that year we need to get all the related yearObjects of 1 VME. The observation gets filled
 	// // up with the information for the processed year.
 	// List<Year<?>> l = map.get(year);
 	// for (Year<?> yearObject : l) {
 	//
 	// // see the discussion below this file, it could be that missing parts in the observation need to be
 	// // filled up with information from the previous years.
 	//
 	// if (yearObject instanceof SpecificMeasure) {
 	// figisDocBuilder.specificMeasures((SpecificMeasure) yearObject, figisDoc);
 	// }
 	// /*
 	// * if (yearObject instanceof VmeHistory) { figisDocBuilder.vmeHistory((VmeHistory) yearObject,
 	// * figisDoc); } if (yearObject instanceof RfmoHistory) { figisDocBuilder.rfmoHistory((RfmoHistory)
 	// * yearObject, figisDoc); }
 	// */
 	// if (yearObject instanceof Profile) {
 	// figisDocBuilder.profile((Profile) yearObject, figisDoc);
 	// }
 	// if (yearObject instanceof GeneralMeasure) {
 	// figisDocBuilder.generalMeasures((GeneralMeasure) yearObject, figisDoc);
 	// }
 	// }
 	// xml.setXml(marshall.marshalToString(figisDoc));
 	// }
 	// VmeObservationDomain vod = new VmeObservationDomain();
 	// vod.setObservationDomainList(odList);
 	// return vod;
 	// }
 	/*
 	 * 
 	 * Subject: FIGISDoc from the VME domain model
 	 * 
 	 * Hi Aureliano and Fabio,
 	 * 
 	 * Question. If a VME has some information for a given year, should the FIGIS VME observation XML then ‘copy’ the
 	 * missing information (if available) from the previous years?
 	 * 
 	 * What we do now is that only these elements will be added which are present in the VME domain for that year
 	 * (Option A).
 	 * 
 	 * Example: A VME has only GeneralMeasures for 2010 and SpecificMeasures for 2011. Would the FIGIS XML 2010
 	 * observation contain then only GeneralMeasures and the FIGIS XML 2011 observation only SpecificMeasures (Option
 	 * A)? Or would the FIGIS XML 2011 observation have both GeneralMeasures(2010) and SpecificMeasures(2011) (Option
 	 * B)?
 	 * 
 	 * Option A would only work if in the Tony DB and the VME domain DB the measures are repeated for every year, even
 	 * if they are the same.
 	 * 
 	 * Cheers, Erik
 	 */
 }
