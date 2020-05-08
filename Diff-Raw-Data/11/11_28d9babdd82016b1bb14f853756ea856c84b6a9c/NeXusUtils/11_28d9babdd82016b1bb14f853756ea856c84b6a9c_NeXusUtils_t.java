 /*-
  * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
  * Facilities Council Daresbury Laboratory
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.data.nexus;
 
 import gda.configuration.properties.LocalProperties;
 import gda.data.NumTracker;
 import gda.data.metadata.GDAMetadataProvider;
 import gda.data.metadata.IMetadataEntry;
 import gda.data.metadata.Metadata;
 import gda.device.Detector;
 import gda.device.DeviceException;
 import gda.util.Version;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 
 import org.nexusformat.NeXusFileInterface;
 import org.nexusformat.NexusException;
 import org.nexusformat.NexusFile;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Utility methods for dealing with NeXus files.
  */
 public class NeXusUtils {
 
 	private static final Logger logger = LoggerFactory.getLogger(NeXusUtils.class);
 
 	private final static String DEFAULT_NUMBER_VALUE = "-1.0";
 	@SuppressWarnings("unused")
 	private final static String DEFAULT_STRING_VALUE = "Not Defined";
 
 	private static enum DataType {
 		/** Double */
 		DOUBLE,
 		/** Double Array */
 		DOUBLE_ARRAY,
 		/** Integer */
 		INTEGER,
 		/** Integer Array */
 		INTEGER_ARRAY,
 		/** Long */
 		LONG,
 		/** Long Array */
 		LONG_ARRAY,
 		/** String */
 		STRING,
 		/** Boolean */
 		BOOLEAN,
 		/** NXdata Class */
 		NXDATA
 	}
 
 	/**
 	 * Returns the number of dimensions (rank) required for the data section in a NeXus file. e.g. A single channel
 	 * counterTimer would have a rank=1. A multi channel counterTimer would have a rank=2. A two dimensional detector
 	 * (CCD) would have a rank=3.
 	 * 
 	 * @param detector
 	 * @return int
 	 * @throws DeviceException
 	 */
 	public static int getRank(Detector detector) throws DeviceException {
 		return getRank(detector.getDataDimensions());
 	}
 
 	/**
 	 * Returns the number of dimensions (rank) required for the data section in a NeXus file. e.g. A single channel
 	 * counterTimer would have a rank=1. A multi channel counterTimer would have a rank=2. A two dimensional detector
 	 * (CCD) would have a rank=3.
 	 * @param detectorDataDimensions 
 	 * @return int
 	 */
 	public static int getRank(int[] detectorDataDimensions) {
 		int rank = 0;
 
 		// The rank is just the dimensionality of the data and the scan dimension - hence the +1
 		rank = detectorDataDimensions.length+1;
 		return rank;
 	}
 	
 	/**
 	 * Returns the number of dimensions (rank) required for the data section in a NeXus file. e.g. A single channel
 	 * counterTimer would have a rank=1. A multi channel counterTimer would have a rank=2. A two dimensional detector
 	 * (CCD) would have a rank=3.
 	 * 
 	 * @param width
 	 * @param height
 	 * @return int
 	 */
 	public static int getRank(int width, int height)  {
 		int rank = 0;
 
 		if (width > 1) {
 			if (height > 1) {
 				rank = 3;
 			} else {
 				rank = 2;
 			}
 		} else {
 			rank = 1;
 		}
 		return rank;
 	}
 
 	/**
 	 * Returns the dimensions array for a given detector that can be used by the NeXus API to create a data section.
 	 * 
 	 * @param detector
 	 * @return int[]
 	 * @throws DeviceException
 	 */
 	public static int[] getDim(Detector detector) throws DeviceException {
 		int[] detectorDataDimensions;
 		int[] iDim;
 
 		detectorDataDimensions = detector.getDataDimensions();
 
 		iDim = getDim(detectorDataDimensions);
 
 		return iDim;
 	}
 
 	/**
 	 * Returns the dimensions array for a given width/height of data that can be used by the NeXus API to create a data
 	 * section.
 	 * 
 	 * @param dimension
 	 * @return int[]
 	 */
 	public static int[] getDim(int[] dimension) {
 		int[] iDim = new int[dimension.length + 1];
 		iDim[0] = NexusFile.NX_UNLIMITED;
 
 		for (int i = 0; i < dimension.length; i++) {
 			iDim[i + 1] = dimension[i];
 		}
 
 		return iDim;
 	}
 
 	/**
 	 * Routine for writing the elements required for the XESraw definition to a given NX_ENTRY
 	 * 
 	 * @param file
 	 * @param entryName
 	 * @throws NexusException
 	 * @throws IOException
 	 */
 	public static void writeXESraw(NeXusFileInterface file, String entryName) throws NexusException, IOException {
 
 		String beamline = LocalProperties.get("gda.instrument", "base");
 		NumTracker runNumber = new NumTracker(beamline);
 		Metadata metadata = GDAMetadataProvider.getInstance();
 
 		// First lets check to see if the entry exists
 		if (file.groupdir().get(entryName) == null) {
 			throw new NexusException("Creating XESraw: specified entry does not exist!");
 		}
 
 		logger.debug("NeXus: Applying XESraw to NXentry " + entryName);
 
 		// Now lets open the entry
 		file.opengroup(entryName, "NXentry");
 
 		try {
 			writeNexusString(file, "title", metadata.getMetadataValue(GDAMetadataProvider.TITLE));
 			writeNexusString(file, "investigation", metadata.getMetadataValue(GDAMetadataProvider.INVESTIGATION));
 			writeNexusString(file, "proposal_identifier", metadata.getMetadataValue(GDAMetadataProvider.PROPOSAL));
 			writeNexusString(file, "experiment_identifier", metadata.getMetadataValue(GDAMetadataProvider.EXPERIMENT_IDENTIFIER));
 			writeNexusString(file, "experiment_description",metadata.getMetadataValue(GDAMetadataProvider.EXPERIMENT_DESCRIPTION));
 			writeNexusString(file, "collection_identifier", metadata.getMetadataValue(GDAMetadataProvider.COLLECTION_IDENTIFIER));
 			writeNexusString(file, "collection_description", metadata.getMetadataValue(GDAMetadataProvider.COLLECTION_DESCRIPTION));
 			writeNexusString(file, "run_cycle", metadata.getMetadataValue(GDAMetadataProvider.FACILITY_RUN_CYCLE));
 			/* to allow unit tests to check for differences in files we need a way of forcing a certain version number */
 			if (LocalProperties.check("gda.data.scan.datawriter.setTime0")){
 				writeNexusString(file, "program_name", "GDA 7.11.0");
 			} else {
 				writeNexusString(file, "program_name", "GDA " + Version.getReleaseVersion());
 			}
 
 //			writeGeneralMetaData(file, metadata);
 						
 			
 		} catch (DeviceException e) {
 			logger.warn("XESraw: Problem reading one or more items of metadata.");
 		}
 
 		// Run Number
 		// writeNexusInteger(file, "entry_identifier", (int) runNumber.getCurrentFileNumber());
 		writeNexusString(file, "entry_identifier", String.valueOf(runNumber.getCurrentFileNumber()));
 
 		// Write the NXuser
 		write_NXuser(file);
 
 		// Make the NXinstrument
 		write_NXinstrument(file);
 
 		// TODO Check to see if there is a bending magnet or insertion device
 
 		// TODO write NXsample
 
 		// Close the NXentry
 		file.closegroup();
 	}
 
 	/**
 	 * Creates an NXinstrument entry at the current position in the NeXus file.
 	 * 
 	 * @param file
 	 *            The NeXus file handle
 	 * @throws NexusException
 	 */
 	public static void write_NXinstrument(NeXusFileInterface file) throws NexusException {
 
 		String beamline = LocalProperties.get("gda.instrument", "base");
 
 		// Make instrument if it's not there.
 		if (file.groupdir().get("instrument") == null) {
 			logger.debug("NeXus: Creating NXinstrument");
 			file.makegroup("instrument", "NXinstrument");
 		}
 		// Open the NXinstrument
 		file.opengroup("instrument", "NXinstrument");
 
 		writeNexusString(file, "name", beamline);
 
 		// These are other components that we may or may not have values for...
 		write_NXmonochromator(file);
 
 		write_NXinsertion_device(file);
 		
 		// Make the source
 		write_NXsource(file);
 
 		// Close the NXinstrument group before returning, so that we are at the same point in the file.
 		file.closegroup();
 
 	}
 
 	/**
 	 * Creates an NXuser(s) entry at the current position in the NeXus file.
 	 * 
 	 * @param file
 	 *            The NeXus file handle
 	 * @throws NexusException
 	 */
 	public static void write_NXuser(NeXusFileInterface file) throws NexusException {
 
 		Metadata metadata = GDAMetadataProvider.getInstance();
 
 		// TODO At the moment we will use the old implementation. Need to get a list of users from the ICAT.
 		file.makegroup("user01", "NXuser");
 		file.opengroup("user01", "NXuser");
 
 		try {
 			writeNexusString(file, "username", metadata.getMetadataValue("federalid"));
 		} catch (DeviceException e) {
 			logger.warn("NXuser: Problem reading one or more items of metadata.");
 		}
 
 		file.closegroup();
 	}
 
 	private static void writeGeneralMetaData(NeXusFileInterface file, Metadata metadata) throws NexusException{
		//TODO This dumps metadata that does not belong in any identified group. maybe use an interface
 		//to identify which metadata entries are to be dumped
 		file.makegroup("general_metadata", "NXuser");
 		file.opengroup("general_metadata", "NXuser");
 
 		try {
 			for (IMetadataEntry entry : metadata.getMetadataEntries()) {
 				String metadataValue = entry.getMetadataValue();
 				String name = entry.getName();
 				if( !metadataValue.isEmpty() && !name.isEmpty()){
 					if( file.groupdir().get(name) == null){
 						NeXusUtils.writeNexusString(file, name, metadataValue);
 					}
 				}
 			}
 		} catch (DeviceException e) {
 			logger.error("problem in writeGeneralMetaData",e);
 		}
 		file.closegroup();
 	}
 		
 	/**
 	 * Opens a NeXus file and returns the file handle.
 	 * 
 	 * @param filename
 	 * @return NeXus file handle
 	 * @throws NexusException
 	 */
 	public static GdaNexusFile openNexusFile(String filename) throws NexusException {
 		return new GdaNexusFile(filename, NexusFile.NXACC_RDWR);
 	}
 
 	/**
 	 * Opens a NeXus file as readonly and returns the file handle.
 	 * 
 	 * @param filename
 	 * @return NeXus file handle
 	 * @throws NexusException
 	 */
 	public static GdaNexusFile openNexusFileReadOnly(String filename) throws NexusException {
 		return new GdaNexusFile(filename, NexusFile.NXACC_READ);
 	}
 
 	/**
 	 * @param file
 	 * @throws NexusException
 	 */
 	public static void write_NXsource(NeXusFileInterface file) throws NexusException {
 
 		Metadata metadata = GDAMetadataProvider.getInstance();
 
 		// Make the source if it's not there.
 		if (file.groupdir().get("source") == null) {
 			logger.debug("NeXus: Creating NXsource");
 			file.makegroup("source", "NXsource");
 		}
 		// Open the NXsource
 		file.opengroup("source", "NXsource");
 
 		try {
 			writeNexusString(file, "name", metadata.getMetadataValue("facility.name", "gda.facility", "DLS"));
 			writeNexusString(file, "type", metadata.getMetadataValue("facility.type", "gda.facility.type",
 					"Synchrotron X-ray Source"));
 			writeNexusString(file, "probe", metadata.getMetadataValue("facility.probe", "gda.facility.probe", "x-ray"));
 //			writeNexusString(file, "mode", metadata.getMetadataValue("source.fillMode"));
 //			writeNexusString(file, "facility_mode", metadata.getMetadataValue("facility.mode"));
 //			writeNexusDouble(file, "frequency", Double.parseDouble(metadata.getMetadataValue("instrument.source.frequency", null, DEFAULT_NUMBER_VALUE)));
 //			writeNexusDouble(file, "voltage", Double.parseDouble(metadata.getMetadataValue("instrument.source.energy", null, DEFAULT_NUMBER_VALUE)) * 1000.0);
 			writeNexusDouble(file, "energy", Double.parseDouble(metadata.getMetadataValue("instrument.source.energy",	null, DEFAULT_NUMBER_VALUE)), "GeV");
 			writeNexusDouble(file, "current", Double.parseDouble(metadata.getMetadataValue("instrument.source.current",	null, DEFAULT_NUMBER_VALUE)), "mA");
 //			writeNexusString(file, "notes", metadata.getMetadataValue("facility.message"));
 		} catch (DeviceException e) {
 			logger.warn("NXsource: Problem reading one or more items of metadata.");
 		}
 
 		// Close the NXsource group before returning, so that we are at the same point in the file.
 		file.closegroup();
 	}
 
 	/**
 	 * @param list
 	 * @return true if any of the items in the list are defined in the metadata list.
 	 * @throws DeviceException
 	 */
 	public static boolean findMetaItems(ArrayList<String> list) throws DeviceException {
 
 		Metadata metadata = GDAMetadataProvider.getInstance();
 
 		for (String string : list) {
 			if ((metadata.getMetadataValue(string)).isEmpty() == false) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * @param file
 	 * @throws NexusException
 	 */
 	public static void write_NXmonochromator(NeXusFileInterface file) throws NexusException {
 		Metadata metadata = GDAMetadataProvider.getInstance();
 		boolean found = false;
 
 		ArrayList<String> items = new ArrayList<String>();
 		items.add("instrument.monochromator.name");
 		items.add("instrument.monochromator.wavelength");
 		items.add("instrument.monochromator.energy");
 
 		// Have a look to see if any of the metadata items have been defined.
 		try {
 			found = findMetaItems(items);
 		} catch (DeviceException e) {
 			logger.warn("NXmonochromator: Problem reading one or more items of metadata.");
 		}
 
 		if (found) {
 			// If we can't find the metadata object, then just create a blank one so that
 			// we fail gracefully on reading any metadata items.
 
 			// Make the source if it's not there.
 			if (file.groupdir().get("monochromator") == null) {
 				logger.debug("NeXus: Creating NXmonochromator");
 				file.makegroup("monochromator", "NXmonochromator");
 			}
 			// Open the NXmonochromator
 			file.opengroup("monochromator", "NXmonochromator");
 
 			try {
 				if (metadata.getMetadataValue("instrument.monochromator.name") != null)
 					writeNexusString(file, "name", metadata.getMetadataValue("instrument.monochromator.name"));
 				if (metadata.getMetadataValue("instrument.monochromator.energy") != null) {
 					writeNexusDouble(file, "energy", Double.parseDouble(metadata.getMetadataValue("instrument.monochromator.energy")), "keV");
 				} else if (metadata.getMetadataValue("instrument.monochromator.wavelength") != null) {
 					writeNexusDouble(file, "wavelength", Double.parseDouble(metadata.getMetadataValue("instrument.monochromator.wavelength")), "Angstrom");
 				}
 
 			} catch (DeviceException e) {
 				logger.warn("NXmonochromator: Problem reading one or more items of metadata.");
 			}
 
 			// Close the NXmonochromator
 			file.closegroup();
 		}
 
 	}
 
 	/**
 	 * @param file
 	 * @throws NexusException
 	 */
 	public static void write_NXinsertion_device(NeXusFileInterface file) throws NexusException {
 		boolean found = false;
 		Metadata metadata = GDAMetadataProvider.getInstance();
 
 		ArrayList<String> items = new ArrayList<String>();
 		items.add("instrument.insertion_device.name");
 		items.add("instrument.insertion_device.type");
 		items.add("instrument.insertion_device.gap");
 		items.add("instrument.insertion_device.taper");
 		items.add("instrument.insertion_device.phase");
 		items.add("instrument.insertion_device.poles");
 		items.add("instrument.insertion_device.length");
 		items.add("instrument.insertion_device.power");
 		items.add("instrument.insertion_device.energy");
 		items.add("instrument.insertion_device.bandwidth");
 		items.add("instrument.insertion_device.harmonic");
 		items.add("instrument.insertion_device.spectrum");
 
 		// Have a look to see if any of the metadata items have been defined.
 		try {
 			found = findMetaItems(items);
 		} catch (DeviceException e) {
 			logger.warn("NXinsertion_device: Problem reading one or more items of metadata.");
 		}
 
 		if (found) {
 			// If we can't find the metadata object, then just create a blank one so that
 			// we fail gracefully on reading any metadata items.
 
 			// Make the group if it's not there.
 			if (file.groupdir().get("insertion_device") == null) {
 				logger.debug("NeXus: Creating NXinsertion_device");
 				file.makegroup("insertion_device", "NXinsertion_device");
 			}
 			// Open the NXinsertion_device
 			file.opengroup("insertion_device", "NXinsertion_device");
 
 			try {
				String gap = metadata.getMetadataValue("instrument.insertion_device.gap");
				if (gap != null) {
					writeNexusDouble(file, "gap", Double.parseDouble(gap), "mm");
 				}
 			} catch (DeviceException e) {
				logger.warn("NXinsertion_device: Problem reading one or more items of metadata.");
 			}
 			// name
 			// type
 			// taper
 			// phase
 			// poles
 			// length
 			// power
 			// energy
 			// bandwidth
 			// harmonic
 			// spectrum
 
 			// Close the NXinsertion_device
 			file.closegroup();
 		}
 	}
 
 	/**
 	 * @param file
 	 * @param names
 	 * @param datatypes
 	 * @throws NexusException
 	 */
 	@SuppressWarnings("unused")
 	public static void writeMetadataList(NeXusFileInterface file, ArrayList<String> names, ArrayList<DataType> datatypes)
 			throws NexusException {
 		Metadata metadata = GDAMetadataProvider.getInstance();
 		// for (String item : names) {
 		// try {
 		// String value = metadata.getMetadataValue(item);
 		// if
 		// } catch (DeviceException e) {
 		// logger.warn("Problem reading one or more items of metadata.");
 		// }
 		// }
 
 	}
 
 	/**
 	 * @param file
 	 * @throws NexusException
 	 */
 	public static void write_NXbending_magnet(NeXusFileInterface file) throws NexusException {
 		Metadata metadata = GDAMetadataProvider.getInstance();
 		boolean found = false;
 
 		// Elements of this component
 		String name = "";
 		Double critical_energy = Double.parseDouble(DEFAULT_NUMBER_VALUE);
 		Double bending_radius = Double.parseDouble(DEFAULT_NUMBER_VALUE);
 		Double[] spectrum;
 
 		ArrayList<String> items = new ArrayList<String>();
 		items.add("instrument.bending_magnet.name");
 		items.add("instrument.bending_magnet.bending_radius");
 		items.add("instrument.bending_magnet.critical_energy");
 		items.add("instrument.bending_magnet.spectrum");
 
 		ArrayList<DataType> datatype = new ArrayList<DataType>();
 		datatype.add(DataType.STRING);
 		datatype.add(DataType.DOUBLE);
 		datatype.add(DataType.DOUBLE);
 		datatype.add(DataType.NXDATA);
 
 		// Have a look to see if any of the metadata items have been defined.
 		try {
 			found = findMetaItems(items);
 		} catch (DeviceException e) {
 			logger.warn("NXbending_magnet: Problem reading one or more items of metadata.");
 		}
 		if (found) {
 
 			// TODO Check to see if there are any Bending Magnet fields. Or maybe just one to indicate a BM ?
 
 			// Make the source if it's not there.
 			if (file.groupdir().get("bending_magnet") == null) {
 				logger.debug("NeXus: Creating NXbending_magnet");
 				file.makegroup("bending_magnet", "NXbending_magnet");
 			}
 			// Open the NXbending_magnet
 			file.opengroup("bending_magnet", "NXbending_magnet");
 
 			// Lets get the metadata values.
 			try {
 				name = metadata.getMetadataValue("instrument.bending_magnet.name");
 				bending_radius = Double.parseDouble(metadata.getMetadataValue(
 						"instrument.bending_magnet.bending_radius", null, DEFAULT_NUMBER_VALUE));
 				critical_energy = Double.parseDouble(metadata.getMetadataValue(
 						"instrument.bending_magnet.critical_energy", null, DEFAULT_NUMBER_VALUE));
 				String spectrumStr = metadata.getMetadataValue("instrument.bending_magnet.spectrum", null,
 						DEFAULT_NUMBER_VALUE);
 				String[] spectrumArray = spectrumStr.split(" ");
 				spectrum = new Double[spectrumArray.length];
 				for (int i = 0; i < spectrumArray.length; i++) {
 					spectrum[i] = Double.parseDouble(spectrumArray[i]);
 				}
 
 				// Now lets write these to the file
 				NeXusUtils.writeNexusString(file, "name", name);
 				NeXusUtils.writeNexusDouble(file, "bending_radius", bending_radius);
 				NeXusUtils.writeNexusDouble(file, "critical_energy", critical_energy);
 
 				file.makegroup("spectrum", "NXdata");
 				file.opengroup("spectrum", "NXdata");
 				NeXusUtils.writeNexusDoubleArray(file, "data", spectrum);
 				file.closegroup();
 
 			} catch (DeviceException e) {
 				logger.warn("NXbending_magnet: Problem reading one or more items of metadata.");
 			}
 
 			// Close the NXbending_magnet
 			file.closegroup();
 		}
 	}
 
 	/**
 	 * @param file
 	 * @throws NexusException
 	 */
 	public static void write_NXsample(NeXusFileInterface file) throws NexusException {
 		// TODO Check to see if there are any Sample fields.
 
 		// Make the source if it's not there.
 		if (file.groupdir().get("sample") == null) {
 			logger.debug("NeXus: Creating NXsample");
 			file.makegroup("sample", "NXsample");
 		}
 		// Open the NXsample
 		file.opengroup("sample", "NXsample");
 
 		// name
 		// chemical_formula
 		// temperature
 		// electric_field
 		// magnetic_field
 		// stress_field
 		// pressure
 		// changer_position
 		// unit_cell
 		// unit_cell_volume
 		// sample_orientation
 		// orientation_matrix
 		// mass
 		// density
 		// relative_molecular_mass
 		// type
 		// description
 		// preparation_date
 		// ...
 
 		// Close the NXsample
 		file.closegroup();
 	}
 
 	/**
 	 * Routine for writing the elements required for Small Angle Scattering definition to a given NX_ENTRY.
 	 * 
 	 * @param file
 	 * @param entryName
 	 */
 	public static void writeSAS(@SuppressWarnings("unused") NeXusFileInterface file, @SuppressWarnings("unused") String entryName) {
 		// Not implemented yet - give me chance!
 		logger.warn("SAS Definition: Trying to use an unimplemented feature.");
 	}
 
 	/**
 	 * Routine for writing the elements required for XAS/EXAFS definition to a given NX_ENTRY.
 	 * 
 	 * @param file
 	 * @param entryName
 	 */
 	public static void writeXAS(@SuppressWarnings("unused") NeXusFileInterface file, @SuppressWarnings("unused") String entryName) {
 		// Not implemented yet - give me chance!
 		logger.warn("XAS Definition: Trying to use an unimplemented feature.");
 	}
 
 	/**
 	 * Writes the String 'stringToWrite' into a field called 'name' at the current position in the NeXus file.
 	 * 
 	 * @param file
 	 * @param name
 	 * @param value
 	 * @throws NexusException
 	 */
 	public static void writeNexusString(NeXusFileInterface file, String name, String value) throws NexusException {
 		if(value == null || name == null || name.isEmpty() || value.isEmpty())
 			return;
 		byte [] bytes = value.getBytes();
 		int[] dimArray = new int[1];
 		dimArray[0] = bytes.length;
 		if (file.groupdir().get(name) == null) {
 			file.makedata(name, NexusFile.NX_CHAR, 1, dimArray);
 		}
 		file.opendata(name);
 		file.putdata(bytes);
 		file.closedata();		
 	}
 
 	/**
 	 * @param file
 	 * @param name
 	 * @param value
 	 * @throws NexusException
 	 */
 	public static void writeNexusStringAttribute(NeXusFileInterface file, String name, String value) throws NexusException {
 		file.putattr(name, value.getBytes(), NexusFile.NX_CHAR);
 	}
 
 	/**
 	 * Writes the integer 'value' into a field called 'name' at the current position in the NeXus file.
 	 * 
 	 * @param file
 	 * @param name
 	 * @param value
 	 * @throws NexusException
 	 */
 	public static void writeNexusInteger(NeXusFileInterface file, String name, int value) throws NexusException {
 		int[] dimArray = new int[1];
 		dimArray[0] = 1;
 		if (file.groupdir().get(name) == null) {
 			file.makedata(name, NexusFile.NX_INT32, 1, dimArray);
 		}
 		file.opendata(name);
 		int[] arr = { value };
 		file.putdata(arr);
 		file.closedata();
 	}
 
 	/**
 	 * Writes the integer array 'value' into a field called 'name' at the current position in the NeXus file.
 	 * 
 	 * @param file
 	 * @param name
 	 * @param value
 	 * @throws NexusException
 	 */
 	public static void writeNexusIntegerArray(NeXusFileInterface file, String name, int[] value) throws NexusException {
 		if (value.length == 0)
 			return;
 		int[] dimArray = new int[1];
 		dimArray[0] = value.length;
 		if (file.groupdir().get(name) == null) {
 			file.makedata(name, NexusFile.NX_INT32, 1, dimArray);
 		}
 		file.opendata(name);
 		file.putdata(value);
 		file.closedata();
 	}
 
 	/**
 	 * Writes the long 'value' into a field called 'name' at the current position in the NeXus file.
 	 * 
 	 * @param file
 	 * @param name
 	 * @param value
 	 * @throws NexusException
 	 */
 	public static void writeNexusLong(NeXusFileInterface file, String name, int value) throws NexusException {
 		int[] dimArray = new int[1];
 		dimArray[0] = 1;
 		if (file.groupdir().get(name) == null) {
 			file.makedata(name, NexusFile.NX_INT64, 1, dimArray);
 		}
 		file.opendata(name);
 		long[] arr = { value };
 		file.putdata(arr);
 		file.closedata();
 	}
 
 	/**
 	 * Writes the long array 'value' into a field called 'name' at the current position in the NeXus file.
 	 * 
 	 * @param file
 	 * @param name
 	 * @param value
 	 * @throws NexusException
 	 */
 	public static void writeNexusLongArray(NeXusFileInterface file, String name, long[] value) throws NexusException {
 		if (value.length == 0)
 			return;
 		int[] dimArray = new int[1];
 		dimArray[0] = value.length;
 		if (file.groupdir().get(name) == null) {
 			file.makedata(name, NexusFile.NX_INT64, 1, dimArray);
 		}
 		file.opendata(name);
 		file.putdata(value);
 		file.closedata();
 	}
 
 	/**
 	 * Writes the double 'value' into a field called 'name' at the current position in the NeXus file.
 	 * 
 	 * @param file
 	 * @param name
 	 * @param value
 	 * @throws NexusException
 	 */
 	public static void writeNexusDouble(NeXusFileInterface file, String name, double value, String units) throws NexusException {
 		int[] dimArray = new int[1];
 		dimArray[0] = 1;
 		if (file.groupdir().get(name) == null) {
 			file.makedata(name, NexusFile.NX_FLOAT64, 1, dimArray);
 		}
 		file.opendata(name);
 		double[] dataArray = { value };
 		file.putdata(dataArray);
 		if (units != null) {
 			file.putattr("units", units.getBytes(Charset.forName("UTF-8")), NexusFile.NX_CHAR);
 		}
 		file.closedata();
 	}
 	
 	/**
 	 * Writes the double 'value' into a field called 'name' at the current position in the NeXus file.
 	 * 
 	 * @param file
 	 * @param name
 	 * @param value
 	 * @throws NexusException
 	 */
 	public static void writeNexusDouble(NeXusFileInterface file, String name, double value) throws NexusException {
 		writeNexusDouble(file, name, value, null);
 	}
 
 	/**
 	 * @param file
 	 * @param name
 	 * @param value
 	 *            a string containing a double value
 	 * @throws NexusException
 	 */
 	public static void writeNexusDouble(NeXusFileInterface file, String name, String value) throws NexusException {
 		writeNexusDouble(file, name, Double.parseDouble(value));
 	}
 
 	/**
 	 * Writes the double array 'value' into a field called 'name' at the current position in the NeXus file.
 	 * 
 	 * @param file
 	 * @param name
 	 * @param value
 	 * @throws NexusException
 	 */
 	public static void writeNexusDoubleArray(NeXusFileInterface file, String name, double[] value) throws NexusException {
 		if (value.length == 0)
 			return;
 		int[] dimArray = new int[1];
 		dimArray[0] = value.length;
 		if (file.groupdir().get(name) == null) {
 			file.makedata(name, NexusFile.NX_FLOAT64, 1, dimArray);
 		}
 		file.opendata(name);
 		file.putdata(value);
 		file.closedata();
 	}
 
 	/**
 	 * Writes the double array 'value' into a field called 'name' at the current position in the NeXus file.
 	 * 
 	 * @param file
 	 * @param name
 	 * @param value
 	 * @throws NexusException
 	 */
 	public static void writeNexusDoubleArray(NeXusFileInterface file, String name, Double[] value) throws NexusException {
 		if (value != null && value.length > 0) {
 			int[] dimArray = new int[1];
 			dimArray[0] = value.length;
 			if (file.groupdir().get(name) == null) {
 				file.makedata(name, NexusFile.NX_FLOAT64, 1, dimArray);
 			}
 			file.opendata(name);
 			file.putdata(value);
 			file.closedata();
 		}
 	}
 
 	/**
 	 * Writes the boolean 'value' into a field called 'name' at the current position in the NeXus file.
 	 * 
 	 * @param file
 	 * @param name
 	 * @param value
 	 * @throws NexusException
 	 */
 	public static void writeNexusBoolean(NeXusFileInterface file, String name, boolean value) throws NexusException {
 		int[] dimArray = new int[1];
 		dimArray[0] = 1;
 		if (file.groupdir().get(name) == null) {
 			file.makedata(name, NexusFile.NX_BOOLEAN, 1, dimArray);
 		}
 		file.opendata(name);
 		file.putdata(value);
 		file.closedata();
 	}
 
 	/**
 	 * Appends the value 'double' into the field called 'name'. If 'name' does not exist then it will be created.
 	 * 
 	 * @param file
 	 * @param name
 	 * @param value
 	 * @throws NexusException
 	 */
 	public static void appendNexusDouble(NeXusFileInterface file, String name, double value) throws NexusException {
 		int[] dimArray = new int[1];
 		dimArray[0] = NexusGlobals.GDA_NX_UNLIMITED;
 		if (file.groupdir().get(name) == null) {
 			file.makedata(name, NexusFile.NX_FLOAT64, 1, dimArray);
 		}
 
 		file.opendata(name);
 
 		double[] dataArray = { value };
 		int[] dataStart = new int[1];
 		int[] dataLength = new int[1];
 		dataStart[0] = 1;
 		dataLength[0] = 1;
 
 		file.putslab(dataArray, dataStart, dataLength);
 		file.closedata();
 	}
 }
