 package org.fao.fi.vme.msaccess.component;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 
 import org.fao.fi.vme.domain.model.Authority;
 import org.fao.fi.vme.domain.model.GeneralMeasure;
 import org.fao.fi.vme.domain.model.GeoRef;
 import org.fao.fi.vme.domain.model.InformationSource;
 import org.fao.fi.vme.domain.model.Profile;
 import org.fao.fi.vme.domain.model.Rfmo;
 import org.fao.fi.vme.domain.model.SpecificMeasure;
 import org.fao.fi.vme.domain.model.Vme;
 import org.fao.fi.vme.domain.model.extended.FisheryAreasHistory;
 import org.fao.fi.vme.domain.model.extended.VMEsHistory;
 import org.fao.fi.vme.msaccess.model.ObjectCollection;
 import org.jglue.cdiunit.ActivatedAlternatives;
 import org.jglue.cdiunit.CdiRunner;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.vme.dao.config.vme.VmeDB;
 import org.vme.dao.config.vme.VmeTestPersistenceUnitConfiguration;
 import org.vme.dao.config.vme.VmeDataBaseProducer;
 
 @RunWith(CdiRunner.class)
 @ActivatedAlternatives({ VmeTestPersistenceUnitConfiguration.class, VmeDataBaseProducer.class })
 public class VmeWriterTest {
 
 	@Inject
 	VmeWriter vmeWriter;
 
 	@Inject
 	@VmeDB
 	private EntityManager em;
 
 	/**
 	 * A integration test here is more or less the same as doing
 	 * VmeAccessDbImport. TODO write a unit test here.
 	 * 
 	 * 
 	 * 
 	 */
 	@Test
 	public void testPersist() {
 		Authority authority = new Authority();
 		GeneralMeasure generalMeasure = new GeneralMeasure();
 		GeoRef geoRef = new GeoRef();
 		FisheryAreasHistory fisheryAreasHistory = new FisheryAreasHistory();
 		VMEsHistory vmesHistory = new VMEsHistory();
 		InformationSource informationSource = new InformationSource();
 		Profile profile = new Profile();
 		Rfmo rfmo = new Rfmo();
 		rfmo.setId("10");
 		SpecificMeasure specificMeasure = new SpecificMeasure();
 		Vme vme = new Vme();
 
 		// add profile to Vme
 		List<Profile> profileList = new ArrayList<Profile>();
 		profileList.add(profile);
 		vme.setProfileList(profileList);
 		// bidirectional
 		profile.setVme(vme);
 
 		// add profile to Vme
 		specificMeasure.setVme(vme);
 		
 		List<SpecificMeasure> specificMeasureList = new ArrayList<SpecificMeasure>();
 		specificMeasureList.add(specificMeasure);
 		vme.setSpecificMeasureList(specificMeasureList);
 		// bidirectional
 
 		// add GeoRef to Vme
 		List<GeoRef> geoRefList = new ArrayList<GeoRef>();
 		geoRefList.add(geoRef);
 		vme.setGeoRefList(geoRefList);
 		// bidirectional
 		geoRef.setVme(vme);
 
 		// add rfmo to Vme
 		vme.setRfmo(rfmo);
 		List<Vme> listOfManagedVmes = new ArrayList<Vme>();
 		listOfManagedVmes.add(vme);
 		rfmo.setListOfManagedVmes(listOfManagedVmes);
 		// bidirectional
 		vme.setRfmo(rfmo);
 
 		// add InformationSource to SpecificMeasure
 		InformationSource is = new InformationSource();
 		is.setSpecificMeasureList(new ArrayList<SpecificMeasure>(Arrays.asList(new SpecificMeasure[] { specificMeasure })));
 		specificMeasure.setInformationSource(is);
 		// bidirectional
 		specificMeasure.setInformationSource(informationSource);
 
 		// add GeneralMeasure to Rfmo
 		List<GeneralMeasure> generalMeasureList = new ArrayList<GeneralMeasure>();
 		generalMeasureList.add(generalMeasure);
 		rfmo.setGeneralMeasureList(generalMeasureList);
 		// bidirectional
 		generalMeasure.setRfmo(rfmo);
 
 		// add FisheryAreasHistory to Rfmo
 		List<FisheryAreasHistory> hasFisheryAreasHistory = new ArrayList<FisheryAreasHistory>();
 		hasFisheryAreasHistory.add(fisheryAreasHistory);
 		rfmo.setHasFisheryAreasHistory(hasFisheryAreasHistory);
 		// bidirectional
 		fisheryAreasHistory.setRfmo(rfmo);
 
 		// add VMEsHistory to Rfmo
 		List<VMEsHistory> hasVmesHistory = new ArrayList<VMEsHistory>();
 		hasVmesHistory.add(vmesHistory);
 		rfmo.setHasVmesHistory(hasVmesHistory);
 		// bidirectional
 		vmesHistory.setRfmo(rfmo);
 
 		// add InformationSource to GeneralMeasure
 		List<InformationSource> informationSourceList = new ArrayList<InformationSource>();
 		informationSourceList.add(informationSource);
 		generalMeasure.setInformationSourceList(informationSourceList);
 		// bidirectional
 		informationSource.setGeneralMeasureList(new ArrayList<GeneralMeasure>(Arrays.asList(new GeneralMeasure[] { generalMeasure })));
 
 		List<ObjectCollection> l = new ArrayList<ObjectCollection>();
 		l.add(createObjectCollection(authority));
 		l.add(createObjectCollection(informationSource));
 		l.add(createObjectCollection(generalMeasure));
 		l.add(createObjectCollection(specificMeasure));
 		l.add(createObjectCollection(geoRef));
 		l.add(createObjectCollection(fisheryAreasHistory));
 		l.add(createObjectCollection(vmesHistory));
 		l.add(createObjectCollection(profile));
 		l.add(createObjectCollection(rfmo));
 		l.add(createObjectCollection(vme));
 
 		vmeWriter.persistNew(l);
 
 	}
 
 	private ObjectCollection createObjectCollection(Object o) {
 		ObjectCollection oc = new ObjectCollection();
 		oc.setClazz(o.getClass());
 		List<Object> objectList = new ArrayList<Object>();
 		oc.setObjectList(objectList);
 		oc.getObjectList().add(o);
 		return oc;
 	}
 
 }
