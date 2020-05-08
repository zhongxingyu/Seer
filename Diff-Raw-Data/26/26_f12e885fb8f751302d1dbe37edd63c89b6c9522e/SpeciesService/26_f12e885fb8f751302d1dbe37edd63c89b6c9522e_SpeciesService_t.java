 package com.id3alsolutions.service;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.InitializingBean;
 
 import com.id3alsolutions.domain.Measurement;
 import com.id3alsolutions.domain.Species;
 import com.id3alsolutions.domain.SpeciesList;
 
 public class SpeciesService implements InitializingBean {
 	private Logger logger = Logger.getLogger(SpeciesService.class);
 	String filePath;
 	private SpeciesList speciesList;
 
 	/**
 	 * @param name
 	 * @return
 	 */
 	public Species findByName(String name) {
 		return speciesList.findByName(name);
 	}
 
 	/**
 	 * @return
 	 */
 	public Iterable<Species> findAll() {
 		return speciesList.getSpecies();
 	}
 
 	/**
 	 * @param species
 	 * @return
 	 */
 	public Measurement calculateRootFlareInInches(Species species, int dbh) {
 		Double rootFlare = new Double(0.0);
 
 		Object retval = performDbhCalculation(
 				species.getRootFlareCalculation(), dbh);
 		if (retval instanceof Double) {
 			rootFlare = (Double) retval;
 			logger.debug("Calculated root flare of: '" + rootFlare
 					+ "' for species: '" + species.getName()
 					+ "' with dbh of: '" + dbh + "'");
 		}
 
 		return new Measurement(rootFlare);
 	}
 
 	/**
 	 * @param species
 	 * @return
 	 */
 	public Measurement calculateCrownWidthInFeet(Species species, int dbh) {
 		Double crownWidth = new Double(0.0);
 
 		Object retval = performDbhCalculation(
 				species.getCrownWidthCalculation(), dbh);
 		if (retval instanceof Double) {
 			crownWidth = (Double) retval;
 			logger.debug("Calculated crown width of: '" + crownWidth
 					+ "' for species: '" + species.getName()
 					+ "' with dbh of: '" + dbh + "'");
 		}
 
		return new Measurement(crownWidth.intValue() * Measurement.INCHES_PER_FOOT);
 	}
 
 	private Object performDbhCalculation(String calculation, int dbh) {
 		Object retval = null;
 		ScriptEngineManager mgr = new ScriptEngineManager();
 		ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
 		try {
 			String scriptToEval = calculation.replaceAll("\\$dbh",
 					Integer.toString(dbh));
 			retval = jsEngine.eval(scriptToEval);
 		} catch (ScriptException ex) {
 			ex.printStackTrace();
 		}
 		return retval;
 	}
 
 	public void serialize() {
 		FileWriter writer = null;
 		try {
 			writer = new FileWriter(new File(filePath));
 			JAXBContext context = JAXBContext.newInstance(SpeciesList.class);
 			Marshaller m = context.createMarshaller();
 			m.marshal(speciesList, writer);
 		} catch (JAXBException e) {
 			logger.error(e.getMessage(), e);
 		} catch (IOException e) {
 			logger.error(e.getMessage(), e);
 		} finally {
 			if (writer != null)
 				try {
 					writer.close();
 				} catch (IOException e) {
 					logger.error(e.getMessage(), e);
 				}
 		}
 	}
 
 	public void deserialize() {
 		logger.info("Loading species from file: " + filePath);
 		try {
 			JAXBContext context = JAXBContext.newInstance(SpeciesList.class);
 			Unmarshaller m = context.createUnmarshaller();
 			speciesList = (SpeciesList) m.unmarshal(new FileReader(new File(
 					filePath)));
 		} catch (JAXBException e) {
 			logger.error(e.getMessage(), e);
 		} catch (FileNotFoundException e) {
 			logger.error(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * @return the filePath
 	 */
 	public String getFilePath() {
 		return filePath;
 	}
 
 	/**
 	 * @param filePath
 	 *            the filePath to set
 	 */
 	public void setFilePath(String filePath) {
 		this.filePath = filePath;
 	}
 
 	/**
 	 * @return the speciesList
 	 */
 	public SpeciesList getSpeciesList() {
 		return speciesList;
 	}
 
 	/**
 	 * @param speciesList
 	 *            the speciesList to set
 	 */
 	public void setSpeciesList(SpeciesList speciesList) {
 		this.speciesList = speciesList;
 	}
 
 	public void afterPropertiesSet() throws Exception {
 		deserialize();
 	}
 }
