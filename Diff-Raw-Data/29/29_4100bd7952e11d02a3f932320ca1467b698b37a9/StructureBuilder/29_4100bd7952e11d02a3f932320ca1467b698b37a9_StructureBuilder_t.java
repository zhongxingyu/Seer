/*
 * The MIT License
 *
 * Copyright (c) <2012> <Bruno P. Kinoshita>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
 package org.biouno.structure;
 
 import hudson.AbortException;
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.Hudson;
 import hudson.model.Label;
 import hudson.tasks.Builder;
 import hudson.util.ArgumentListBuilder;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.StringUtils;
 import org.biouno.structure.parser.MainParamsParser;
 import org.biouno.structure.parser.ParserException;
 import org.biouno.structure.util.Messages;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 /**
  * Structure builder.
  * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
  * @since 0.1
  */
 public class StructureBuilder extends Builder {
 	/*
 	 * Le logger.
 	 */
 	private static final Logger LOGGER = Logger.getLogger("org.biouno.structure");
 	/*
 	 * Structure command line option constants.
 	 */
 	public static final String MAINPARAMS_OPTION = "-m";
 	public static final String EXTRAPARAMS_OPTION = "-e";
 	public static final String MAXPOPS_OPTION = "-K";
 	public static final String NUMLOCI_OPTION = "-L";
 	public static final String NUMINDS_OPTION = "-N";
 	public static final String INFILE_OPTION = "-i";
 	public static final String OUTFILE_OPTION = "-o";
 	/*
 	 * Structure constants used for the output file name.
 	 */
 	public static final String STRUCTURE_RUN = "_run_";
 	public static final String STRUCTURE_RUN_OUTPUT_DIRECTORY = "structure_run_output";
 	public static final String STRUCTURE_OUTPUT_FILE_SUFFIX = "_f";
 	/*
 	 * Constants used for creating structure files.
 	 */
 	public static final String MAINPARAMS_PARAM_SET_K_PREFIX = "mainparams.param_set.k";
 	public static final String STRUCTURE_EXTRAMPARAMS_FILENAME = "extraparams";
 	public static final String STRUCTURE_FILES_ENCODING = "UTF-8";
 	/**
 	 * Le builder extension.
 	 */
 	@Extension
 	public static final StructureBuilderDescriptor DESCRIPTOR = new StructureBuilderDescriptor();
 	/**
 	 * Parses mainparams file, replacing values with the ones provided by the 
 	 * user in the jelly form (config.jelly).
 	 */
 	private final MainParamsParser parser;
 	/**
 	 * Structure installation.
 	 */
 	private final String structureInstallationName;
 	/**
 	 * Number of loci in data file (NUMLOCI).
 	 */
 	private final Integer numLoci;
 	/**
 	 * Number of diploid individuals in data file (NUMINDS).
 	 */
 	private final Integer numInds;
 	/**
 	 * Length of burn-in period (BURNIN).
 	 */
 	private final Long burnIn;
 	/**
 	 * Number of MCMC steps after burn-in (NUMREPS).
 	 */
 	private final Long numReps;
 	/**
 	 * Name of input data file (INFILE).
 	 */
 	private final String inFile;
 	/**
 	 * Name of output data file (OUTFILE).
 	 */
 	private final String outFile;
 	/**
 	 * Main parameters (mainparams).
 	 */
 	private final String mainParams;
 	/**
 	 * Extra parameters (extraparams).
 	 */
 	private final String extraParams;
 	/**
 	 * K.
 	 */
 	private final String kValue;
 	/**
 	 * Constructor with args, called from Jelly populating the object properties
 	 * from the form.
 	 * @param structureInstallationName
 	 * @param numLoci
 	 * @param numInds
 	 * @param burnIn
 	 * @param numReps
 	 * @param inFile
 	 * @param outFile
 	 * @param mainParams
 	 * @param extraParams
 	 * @param kValue
 	 */
 	@DataBoundConstructor
 	public StructureBuilder(String structureInstallationName,
 			Integer numLoci, Integer numInds, Long burnIn, Long numReps,
 			String inFile, String outFile, String mainParams,
 			String extraParams, String kValue) {
 		super();
 		this.structureInstallationName = structureInstallationName;
 		this.numLoci = numLoci;
 		this.numInds = numInds;
 		this.burnIn = burnIn;
 		this.numReps = numReps;
 		this.inFile = inFile;
 		this.outFile = outFile;
 		this.mainParams = mainParams;
 		this.extraParams = extraParams;
 		this.kValue = kValue;
 		parser = new MainParamsParser(numLoci, numInds, burnIn, numReps, inFile, outFile);
 	}
 	/**
 	 * @return the structureInstallationName
 	 */
 	public String getStructureInstallationName() {
 		return structureInstallationName;
 	}
 	/**
 	 * @return the numLoci
 	 */
 	public Integer getNumLoci() {
 		return numLoci;
 	}
 	/**
 	 * @return the numInds
 	 */
 	public Integer getNumInds() {
 		return numInds;
 	}
 	/**
 	 * @return the burnIn
 	 */
 	public Long getBurnIn() {
 		return burnIn;
 	}
 	/**
 	 * @return the numReps
 	 */
 	public Long getNumReps() {
 		return numReps;
 	}
 	/**
 	 * @return the inFile
 	 */
 	public String getInFile() {
 		return inFile;
 	}
 	/**
 	 * @return the outFile
 	 */
 	public String getOutFile() {
 		return outFile;
 	}
 	/**
 	 * @return the mainParams
 	 */
 	public String getMainParams() {
 		return mainParams;
 	}
 	/**
 	 * @return the extraParams
 	 */
 	public String getExtraParams() {
 		return extraParams;
 	}
 	/**
 	 * @return the value of k
 	 */
 	public String getKValue() {
 		return kValue;
 	}
 	/**
 	 * Creates one mainparam file for each K, and creates jobs for running 
 	 * structure using each mainparam file. Finally, the output files are 
 	 * sent back to this builder's job workspace. Then an action is included in 
 	 * the build, to render summary about the plug-in execution.
 	 * 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener) throws AbortException,InterruptedException, IOException {
 		listener.getLogger().println(Messages.StructureBuilder_InvokingStructure());
 
 		final EnvVars envVars = build.getEnvironment(listener);
 		envVars.overrideAll(build.getBuildVariables());
 		
 		// Get the structure installation used
 		final StructureInstallation structureInstallation = DESCRIPTOR.getInstallationByName(this.structureInstallationName);
 		if (structureInstallation == null) {
 			throw new AbortException(Messages.StructureBuilder_InvalidStructureInstallation());
 		}
 		
 		// Get K
 		int k;
 		try {
 			k = Integer.parseInt(envVars.expand(kValue));
 		} catch (NumberFormatException nfe) {
 			throw new AbortException("Couldn't expand K: " + nfe.getMessage());
 		}
 		
 		// Inform the user about some important info
 		listener.getLogger().println("Using structure " + structureInstallation.getName() + " at " + structureInstallation.getPathToExecutable());
 		listener.getLogger().println("K="+k);
 		
 		final FilePath workspace = build.getWorkspace();
 		
 		// Replace variables with the values provided by the user in the job configuration
 		String mainParamsFile = MAINPARAMS_PARAM_SET_K_PREFIX + k;
 		String extraParamsFile = STRUCTURE_EXTRAMPARAMS_FILENAME;
 		try {
 			final String mainParamContent = parser.parse(this.mainParams, k);
 			final FilePath mainparamsFilePath = new FilePath(workspace, mainParamsFile);
 			mainparamsFilePath.write(mainParamContent, STRUCTURE_FILES_ENCODING);
 			final FilePath extraparamsFilePath = new FilePath(workspace, extraParamsFile);
 			extraparamsFilePath.write(extraParams, STRUCTURE_FILES_ENCODING);
 		} catch(ParserException pe) {
 			pe.printStackTrace(listener.getLogger());
 			throw new AbortException(pe.getMessage());
 		}
 		
 		final String outputFile = envVars.expand(outFile);
 		
 		// Create structure command line
 		final ArgumentListBuilder args = this.createStructureArgs(structureInstallation, k, mainParamsFile, extraParamsFile, outputFile, workspace); 
 		
 		// Execute structure
 		// Env vars
 		final Map<String, String> env = build.getEnvironment(listener);
 		listener.getLogger().println(Messages.StructureKBuilder_ExecuteStructure(args.toStringWithQuote()));
 		final Integer exitCode = launcher.launch().cmds(args).envs(env)
 				.stdout(listener).pwd(build.getModuleRoot()).join();
 
 		if (exitCode != 0) {
 			listener.getLogger().println(Messages.StructureKBuilder_ExecuteStructureError(exitCode));
 			return Boolean.FALSE;
 		} else {
 			// If the command was executed with success, send the outfile back to the master
 			FilePath outFileFilePath = new FilePath(workspace, outputFile+STRUCTURE_OUTPUT_FILE_SUFFIX);
 			if(outFileFilePath.exists()) {
 				build.addAction(new StructureBuildSummaryAction(build, new String[]{outFileFilePath.getName()}, k));
 			} else {
 				listener.fatalError("Couldn't find structure output file. Expected " + outFileFilePath.getRemote());
 			}
 			
 			listener.getLogger().println(Messages.StructureKBuilder_ExecuteStructureSuccess());
 			return Boolean.TRUE;
 		}
 	}
 	/**
 	 * Creates structure args.
 	 * @param structure 
 	 * @param k
 	 * @param extraParamsFile 
 	 * @param mainParamsFile 
 	 * @param workspace 
 	 * @return ArgumentListBuilder
 	 */
 	private ArgumentListBuilder createStructureArgs(StructureInstallation structure, int k, String mainParamsFile, String extraParamsFile, String outputFile, FilePath workspace) {
 		ArgumentListBuilder args = new ArgumentListBuilder();
 		args.add(structure.getPathToExecutable());
 		// main params
 		args.add(MAINPARAMS_OPTION);
 		args.add(mainParamsFile);
 		// extra params
 		args.add(EXTRAPARAMS_OPTION);
 		args.add(extraParamsFile);
 		// max pops (K)
 		args.add(MAXPOPS_OPTION);
 		args.add(k);
 		// number of loci
 		if (numLoci != null && numLoci > 0) {
 			args.add(NUMLOCI_OPTION);
 			args.add(numLoci);
 		}
 		// number of individuals
 		if (numInds != null && numInds > 0) {
 			args.add(NUMINDS_OPTION);
 			args.add(numInds);
 		}
 		// input file
 		if (StringUtils.isNotBlank(inFile)) {
 			args.add(INFILE_OPTION);
 			args.add(new FilePath(workspace, inFile).getRemote());
 		}
 		// output file
 		if (StringUtils.isNotBlank(outputFile)) {
 			args.add(OUTFILE_OPTION);
 			args.add(new FilePath(workspace, outputFile).getRemote());
 		}
 		return args;
 	}
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public StructureBuilderDescriptor getDescriptor() {
 		return (StructureBuilderDescriptor) super.getDescriptor();
 	}
 	/**
 	 * Fills labels items.
 	 * @return ListBoxModel
 	 */
 	public Set<Label> getLabels() {
 		Set<Label> labels = Hudson.getInstance().getLabels();
 		return labels;
 	}
 }
