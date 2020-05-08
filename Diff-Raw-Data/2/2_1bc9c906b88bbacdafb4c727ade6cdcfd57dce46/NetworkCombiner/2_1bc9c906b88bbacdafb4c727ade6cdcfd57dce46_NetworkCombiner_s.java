 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
  * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  * 
  * @author dmyersturnbull
  */
 package org.structnetalign.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.biojava.bio.structure.Atom;
 import org.biojava.bio.structure.StructureException;
 import org.biojava.bio.structure.align.ce.AbstractUserArgumentProcessor;
 import org.biojava.bio.structure.align.util.AtomCache;
 import org.biojava3.core.sequence.ProteinSequence;
 import org.biojava3.core.sequence.io.FastaReaderHelper;
 import org.structnetalign.weight.AtomCacheFactory;
 
 import psidev.psi.mi.xml.model.Entry;
 import psidev.psi.mi.xml.model.EntrySet;
 import psidev.psi.mi.xml.model.Interaction;
 import psidev.psi.mi.xml.model.Interactor;
 import psidev.psi.mi.xml.model.Participant;
 
 /**
  * A standalone utility that takes one or more PSI-MI XML networks and produces a combined network containing a random
  * subsample of the two networks. Specifically, interactors (vertices) are selected for inclusion at random, and an
  * interaction is included if and only if both of its participants are included. In this way, NetworkCombiner can remove
  * both vertices and edges.
  * 
  * @author dmyersturnbull
  * 
  */
 public class NetworkCombiner {
 
 	private static final Logger logger = LogManager.getLogger("org.structnetalign");
 
 	private static final String NEWLINE = "\n";
 
 	private static Random random = new Random();
 
 	private static final String URL = "http://www.uniprot.org/uniprot/%s.fasta";
 
 	private double probability = 0.05;
 
 	private boolean requireFasta = false;
 
 	private boolean requirePdb = false;
 
 	private boolean requireScop = false;
 	
 	private boolean removeLonely = false;
 
 	public static void main(String[] args) {
 
 		Options options = getOptions();
 		CommandLineParser parser = new GnuParser();
 		CommandLine cmd;
 		try {
 			cmd = parser.parse(options, args);
 		} catch (ParseException e) {
 			printUsage(e.getMessage(), options);
 			return;
 		}
 
 		File output = new File(cmd.getOptionValue("output"));
 
 		double probability;
 		try {
 			probability = Double.parseDouble(cmd.getOptionValue("probability"));
 		} catch (NumberFormatException e) {
 			printUsage("probability must be a floating-point number", options);
 			return;
 		}
 
 		String pdbDir = cmd.getOptionValue("pdb_dir");
 		if (pdbDir != null) {
 			System.setProperty(AbstractUserArgumentProcessor.PDB_DIR, pdbDir);
 			AtomCacheFactory.setCache(pdbDir);
 		}
 
 		boolean removeLonely = cmd.hasOption("remove_lonely");
 		boolean requirePdb = cmd.hasOption("require_pdb");
 		boolean requireScop = cmd.hasOption("require_scop");
 		boolean requireFasta = cmd.hasOption("require_fasta");
 
 		List<?> argList = cmd.getArgList();
 		File[] inputs = new File[argList.size()];
 		for (int i = 0; i < argList.size(); i++) {
 			inputs[i] = new File(argList.get(i).toString());
 		}
 
 		runCombiner(output, probability, requirePdb, requireScop, requireFasta, removeLonely, inputs);
 
 	}
 
 	/**
 	 * Prints an error message for {@code e} that shows causes and suppressed messages recursively. Just a little more
 	 * useful than {@code e.printStackTrace()}.
 	 * 
 	 * @param e
 	 */
 	public static void printError(Exception e) {
 		System.err.println(printError(e, ""));
 	}
 
 	public void setRemoveLonely(boolean removeLonely) {
 		this.removeLonely = removeLonely;
 	}
 
 	public static void runCombiner(File output, double probability, boolean requirePdb, boolean requireScop,
 			boolean requireFasta, boolean removeLonely, File... inputs) {
 		NetworkCombiner combiner = new NetworkCombiner();
 		combiner.setRequirePdb(requirePdb);
 		combiner.setRequireScop(requireScop);
 		combiner.setRequireFasta(requireFasta);
 		combiner.setProbability(probability);
 		combiner.setRemoveLonely(removeLonely);
 		combiner.combine(output, inputs);
 	}
 
 	private static Options getOptions() {
 		Options options = new Options();
 		options.addOption(OptionBuilder
 				.hasArg(true)
 				.withDescription(
 						"Required. The probability of including an interactor. Interactions associated with an excluded interactor will be removed.")
 				.isRequired(true).create("probability"));
 		options.addOption(OptionBuilder.hasArg(false)
 				.withDescription("Require each interactor to have a PDB structure").isRequired(false)
 				.create("require_pdb"));
		options.addOption(OptionBuilder.hasArg(false).withDescription("Require each interacto to haver a SCOP domain")
 				.isRequired(false).create("require_scop"));
 		options.addOption(OptionBuilder.hasArg(false)
 				.withDescription("Require each interactor to have a FASTA sequence").isRequired(false)
 				.create("require_fasta"));
 		options.addOption(OptionBuilder.hasArg(false).withDescription("Remove all interactors with no interactions")
 				.isRequired(false).create("remove_lonely"));
 		options.addOption(OptionBuilder.hasArg(false).withDescription("Require each interactor to have a Pfam entry")
 				.isRequired(false).create("require_pfam"));
 		options.addOption(OptionBuilder
 				.hasArg(true)
 				.withDescription(
 						"The directory containing cached PDB files. Defaults to the AtomCache default, which is probably in your system's temporary directory (e.g. /tmp). It is okay if this is an empty directory, but the directory must exist.")
 				.isRequired(false).create("pdb_dir"));
 		options.addOption(OptionBuilder.hasArg(true).withDescription("Required. The output PSI-MI25 XML file.")
 				.isRequired(true).create("output"));
 		return options;
 	}
 
 	private static ProteinSequence getSequenceForId(String uniProtId) throws Exception {
 		try (InputStream stream = new URL(String.format(URL, uniProtId)).openStream()) {
 			return FastaReaderHelper.readFastaProteinSequence(stream).get(uniProtId); // why does this throw Exception?
 		}
 	}
 
 	/**
 	 * @see #printError(Exception)
 	 */
 	private static String printError(Exception e, String tabs) {
 		StringBuilder sb = new StringBuilder();
 		Throwable prime = e;
 		while (prime != null) {
 			if (tabs.length() > 0) sb.append(tabs + "Cause:" + NEWLINE);
 			sb.append(tabs + prime.getClass().getSimpleName() + NEWLINE);
 			if (prime.getMessage() != null) sb.append(tabs + prime.getMessage() + NEWLINE);
 			if (prime instanceof Exception) {
 				StackTraceElement[] trace = ((Exception) prime).getStackTrace();
 				for (StackTraceElement element : trace) {
 					sb.append(tabs + element.toString() + NEWLINE);
 				}
 			}
 			prime = prime.getCause();
 			tabs += "\t";
 			sb.append(NEWLINE);
 		}
 		return sb.toString();
 	}
 
 	private static void printUsage(String note, Options options) {
 		if (note != null) System.out.println(note);
 		HelpFormatter hf = new HelpFormatter();
 		hf.printHelp("java -jar " + NetworkCombiner.class.getSimpleName() + ".jar [options] input1.xml input2.xml ...",
 				options);
 	}
 
 	public NetworkCombiner() {
 		super();
 	}
 
 	/**
 	 * 
 	 * @param probability
 	 *            The probability that an interactor/vertex will be retained
 	 */
 	public NetworkCombiner(double probability) {
 		super();
 		this.probability = probability;
 	}
 
 	public void combine(File output, File... inputs) {
 
 		EntrySet myEntrySet = new EntrySet();
 
 		int nInteractors = 0;
 		int nInteractions = 0;
 		
 		for (int i = 0; i < inputs.length; i++) {
 
 			EntrySet entrySet = NetworkUtils.readNetwork(inputs[i]);
 			
 			logger.info("Read entry set " + i + " at " + inputs[i]);
 
 			// do it this way so we don't have to read the first network twice
 			if (i == 0) {
 				myEntrySet.setVersion(entrySet.getVersion());
 				myEntrySet.setMinorVersion(entrySet.getMinorVersion());
 				myEntrySet.setLevel(entrySet.getLevel());
 			} else {
 				if (entrySet.getVersion() != myEntrySet.getVersion()) throw new IllegalArgumentException(
 						"Different major version numbers!");
 				if (entrySet.getVersion() != myEntrySet.getVersion()) throw new IllegalArgumentException(
 						"Different minor version numbers!");
 				if (entrySet.getLevel() != myEntrySet.getLevel()) throw new IllegalArgumentException(
 						"Different level numbers!");
 			}
 
 			int j = 1;
 			for (Entry entry : entrySet.getEntries()) {
 				Entry myEntry = includeVertices(entry);
 				myEntrySet.getEntries().add(myEntry);
 				nInteractors += myEntry.getInteractors().size();
 				nInteractions += myEntry.getInteractions().size();
 				logger.info("Included " + myEntry.getInteractors().size() + " interactors and " + myEntry.getInteractions().size() + " interactions from entry " + j + " in " + inputs[i]);
 				j++;
 			}
 
 			entrySet = null;
 			System.gc();
 		}
 
 		NetworkUtils.writeNetwork(myEntrySet, output);
 		logger.info("Wrote network containing " + nInteractors + " interactors and " + nInteractions + " interactions to " + output);
 	}
 
 	public void setProbability(double probability) {
 		this.probability = probability;
 	}
 
 	public void setRequireFasta(boolean requireFasta) {
 		this.requireFasta = requireFasta;
 	}
 
 	public void setRequirePdb(boolean requirePdb) {
 		this.requirePdb = requirePdb;
 	}
 
 	public void setRequireScop(boolean requireScop) {
 		this.requireScop = requireScop;
 	}
 
 	private boolean hasRequired(Interactor interactor) {
 
 		String uniProtId = NetworkUtils.getUniProtId(interactor);
 		if (uniProtId == null) {
 			logger.debug("Couldn't find UniProt Id for Id#" + interactor.getId());
 			return false; // always require a UniProt Id
 		}
 
 		if (requirePdb) {
 
 			String pdbIdAndChain = IdentifierMappingFactory.getMapping().uniProtToPdb(uniProtId);
 			if (pdbIdAndChain == null) {
 				logger.debug("Couldn't find PDB Id for " + uniProtId);
 				return false;
 			}
 
 			final AtomCache cache = AtomCacheFactory.getCache();
 			try {
 				Atom[] ca1 = cache.getAtoms(pdbIdAndChain);
 				if (ca1 == null) throw new StructureException("Structure is null");
 			} catch (IOException | StructureException e) {
 				logger.debug("Couldn't find PDB structure for " + uniProtId, e);
 				return false;
 			}
 
 			if (requireScop) {
 				String scopId = IdentifierMappingFactory.getMapping().uniProtToScop(uniProtId);
 				if (scopId == null) {
 					logger.debug("Couldn't find SCOP Id for " + uniProtId);
 					return false;
 				}
 			}
 
 		}
 
 		if (requireFasta) {
 			try {
 				ProteinSequence seq = getSequenceForId(uniProtId);
 				if (seq == null || seq.getLength() == 0) {
 					throw new Exception("Protein sequence is empty");
 				}
 			} catch (Exception e) {
 				logger.debug("Couldn't find FASTA sequence for " + uniProtId, e);
 				return false;
 			}
 		}
 
 		return true;
 
 	}
 
 	private Entry includeVertices(Entry entry) {
 
 		Entry myEntry = NetworkUtils.skeletonClone(entry);
 
 		Set<Integer> set = new HashSet<Integer>();
 		Collection<Interactor> interactors = entry.getInteractors();
 		for (Interactor interactor : interactors) {
 			final double r = random.nextDouble();
 			if (r <= probability && hasRequired(interactor)) {
 				set.add(interactor.getId());
 				myEntry.getInteractors().add(interactor);
 				logger.debug("Included interactor Id#" + interactor.getId());
 			}
 		}
 
 		// now add the edges
 		interactions: for (Interaction interaction : entry.getInteractions()) {
 
 			Collection<Participant> participants = interaction.getParticipants();
 
 			for (Participant participant : participants) {
 				final int id = participant.getInteractor().getId();
 				if (!set.contains(id)) {
 					continue interactions;
 				}
 			}
 
 			myEntry.getInteractions().add(interaction);
 			logger.debug("Included interaction Id#" + interaction.getId());
 
 		}
 		
 		return myEntry;
 
 	}
 
 }
