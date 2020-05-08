 package de.fhhannover.inform.trust.ifmapcli;
 
 /*
  * #%L
  * ====================================================
  *   _____                _     ____  _____ _   _ _   _
  *  |_   _|_ __ _   _ ___| |_  / __ \|  ___| | | | | | |
  *    | | | '__| | | / __| __|/ / _` | |_  | |_| | |_| |
  *    | | | |  | |_| \__ \ |_| | (_| |  _| |  _  |  _  |
  *    |_| |_|   \__,_|___/\__|\ \__,_|_|   |_| |_|_| |_|
  *                             \____/
  * 
  * =====================================================
  * 
  * Fachhochschule Hannover 
  * (University of Applied Sciences and Arts, Hannover)
  * Faculty IV, Dept. of Computer Science
  * Ricklinger Stadtweg 118, 30459 Hannover, Germany
  * 
  * Email: trust@f4-i.fh-hannover.de
  * Website: http://trust.inform.fh-hannover.de/
  * 
 * This file is part of Ifmapcli, version 0.0.3, implemented by the Trust@FHH 
  * research group at the Fachhochschule Hannover.
  * %%
  * Copyright (C) 2010 - 2013 Trust@FHH
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import javax.net.ssl.TrustManager;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.w3c.dom.Document;
 
 import de.fhhannover.inform.trust.ifmapcli.common.Common;
 import de.fhhannover.inform.trust.ifmapcli.common.Config;
 import de.fhhannover.inform.trust.ifmapj.IfmapJ;
 import de.fhhannover.inform.trust.ifmapj.IfmapJHelper;
 import de.fhhannover.inform.trust.ifmapj.channel.SSRC;
 import de.fhhannover.inform.trust.ifmapj.exception.EndSessionException;
 import de.fhhannover.inform.trust.ifmapj.exception.IfmapErrorResult;
 import de.fhhannover.inform.trust.ifmapj.exception.IfmapException;
 import de.fhhannover.inform.trust.ifmapj.exception.InitializationException;
 import de.fhhannover.inform.trust.ifmapj.identifier.Device;
 import de.fhhannover.inform.trust.ifmapj.identifier.Identifiers;
 import de.fhhannover.inform.trust.ifmapj.identifier.Identity;
 import de.fhhannover.inform.trust.ifmapj.identifier.IdentityType;
 import de.fhhannover.inform.trust.ifmapj.messages.PublishRequest;
 import de.fhhannover.inform.trust.ifmapj.messages.PublishUpdate;
 import de.fhhannover.inform.trust.ifmapj.messages.Requests;
 import de.fhhannover.inform.trust.ifmapj.metadata.StandardIfmapMetadataFactory;
 
 /**
  * This is a class to test performance of an IF-MAP 2.0 server. It was created
  * based on input from Juniper Networks. The goal is to measure performance of a
  * MAPS when a large number of publish and subscribe operations are done by one
  * client. The test setup is as follows:
  * 
  * - one IF-MAP client - send one subscribe message for the parent
  * identifier/node with max-depth of 1 - next, send many (thousands?) publish
  * messages - each publish will contain "update" elements to create a link with
  * the parent - lastly, do a poll
  * 
  * @author ib
  * 
  */
 public class Perf1 {
 
 	final static String CMD = "perf1";
 	static int counter = 0;
 
 	private SSRC mSsrc;
 	private StandardIfmapMetadataFactory mMetaFac = IfmapJ
 			.createStandardMetadataFactory();
 	private Device mRootNode = Identifiers.createDev("parentNode");
 	private Document mAuthBy = mMetaFac.createAuthBy();
 	private ArrayList<PublishRequest> mPublishRequests = new ArrayList<PublishRequest>(
 			1000);
 	
 	private int mSizeSprint;
 	private int mNumberOfSprints;
 	private int mNumberRequests;
 	private int mNumberUpdates;
 	
 	// CLI options parser stuff ( not the actual input params )
 	Options mOptions;
 	Option mNumRequestsOp;
 	Option mNumUpdatesOp;
 	Option mSizeSprintOp;
 	Option mNameOp;
 	Option mHelpOp;
 
 	// parsed command line options
 	CommandLine mCmdLine;
 
 	// configuration
 	Config mConfig;
 
 	public Perf1(String[] args) throws FileNotFoundException,
 			InitializationException {
 		mConfig = Common.loadEnvParams();
 		System.out.println(CMD + " uses config " + mConfig);
 		parseCommandLine(args);
 		initSsrc();
 		preparePublishRequests();
 	}
 
 	/**
 	 * Create session, start publishing, end session
 	 * 
 	 * @throws IfmapErrorResult
 	 * @throws IfmapException
 	 * @throws EndSessionException 
 	 */
 	public void start() throws IfmapErrorResult, IfmapException, EndSessionException {
 		mSsrc.newSession();
 		
 		long start = System.currentTimeMillis();
 		
 		for (int i = 0; i < mNumberOfSprints; i++) {
 			System.out.print("Do publish sprint " + i);
 			long startSprint = System.currentTimeMillis();
 			for (int j = i * mSizeSprint; j < (i * mSizeSprint) + mSizeSprint; j++) {
 				PublishRequest pr = mPublishRequests.get(j);
 				mSsrc.publish(pr);				
 			}
 			long endSprint = System.currentTimeMillis();
 			System.out.print(" done! -> ");
 			System.out.println("Duration: " + (endSprint - startSprint) + "ms");
 		}
 		
 		long end = System.currentTimeMillis();
 		System.out.println("Total Duration: " + (end - start) + "ms");
 		
 		mSsrc.endSession();
 	}
 
 	/**
 	 * 
 	 */
 	private void preparePublishRequests() {
 		PublishRequest pr;
 		PublishUpdate pu;
 		Identity id;
 
 		// create a certain number of publish requests
 		for (int i = 0; i < mNumberRequests; i++) {
 			pr = Requests.createPublishReq();
 			// create a certain number of publish updates
 			for (int j = 0; j < mNumberUpdates; j++) {
 				pu = Requests.createPublishUpdate();
 				// generate new Identifier
 				id = Identifiers.createIdentity(IdentityType.userName,
 						new Integer(Perf1.counter++).toString());
 				pu.setIdentifier1(mRootNode);
 				pu.setIdentifier2(id);
 				pu.addMetadata(mAuthBy);
 				pr.addPublishElement(pu);
 			}
 			mPublishRequests.add(pr);
 		}
 		
 	}
 
 	/**
 	 * parse the command line by using Apache commons-cli
 	 * 
 	 * @param args
 	 */
 	private void parseCommandLine(String[] args) {
 		mOptions = new Options();
 		// automatically generate the help statement
 		HelpFormatter formatter = new HelpFormatter();
 		formatter.setWidth(100);
 
 		// boolean options
 		mHelpOp = new Option("h", "help", false, "print this message");
 		mOptions.addOption(mHelpOp);
 
 		// argument options
 		// number of requests
 		OptionBuilder.hasArg();
 		OptionBuilder.isRequired();
 		OptionBuilder.withArgName("requests");
 		OptionBuilder.withType(Integer.class);
 		OptionBuilder.withDescription("number of publish requests");
 		mNumRequestsOp = OptionBuilder.create("r");
 		mOptions.addOption(mNumRequestsOp);
 
 		// update operations per request
 		OptionBuilder.hasArg();
 		OptionBuilder.isRequired();
 		OptionBuilder.withArgName("updates");
 		OptionBuilder.withDescription("number of update elements per request");
 		mNumUpdatesOp = OptionBuilder.create("u");
 		mOptions.addOption(mNumUpdatesOp);
 		
 		// requests per sprint
 		OptionBuilder.hasArg();
 		OptionBuilder.isRequired();
 		OptionBuilder.withArgName("sprint size");
 		OptionBuilder.withType(Integer.class);
 		OptionBuilder.withDescription("size of one sprint");
 		mSizeSprintOp = OptionBuilder.create("s");
 		mOptions.addOption(mSizeSprintOp);
 
 		// create the parser
 		CommandLineParser parser = new GnuParser();
 		try {
 			// parse the command line arguments
 			mCmdLine = parser.parse(mOptions, args);
 			
 			mNumberRequests = new Integer(mCmdLine.getOptionValue(mNumRequestsOp
 					.getOpt())).intValue();
 			mNumberUpdates = new Integer(mCmdLine.getOptionValue(mNumUpdatesOp
 					.getOpt())).intValue();
 			mSizeSprint = new Integer(mCmdLine.getOptionValue(mSizeSprintOp
 					.getOpt())).intValue();
 			if (mSizeSprint > mNumberRequests){
 				// there is only one sprint
 				mNumberOfSprints = 1;
 				mSizeSprint = mNumberRequests;
 			} else {
 				mNumberOfSprints = mNumberRequests / mSizeSprint;
 			}
 			
 		} catch (ParseException exp) {
 			// oops, something went wrong
 			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
 			formatter
 					.printHelp(
 							Perf1.CMD
 									+ " -r <number of requests> -u <number of updates per requests> ",
 							mOptions);
 			System.out.println(Common.USAGE);
 			System.exit(1);
 		}
 	}
 
 	/**
 	 * Load {@link TrustManager} instances and create {@link SSRC}.
 	 * 
 	 * @throws FileNotFoundException
 	 * @throws InitializationException
 	 */
 	private void initSsrc() throws FileNotFoundException,
 			InitializationException {
 		InputStream is = Common
 				.prepareTruststoreIs(mConfig.getTruststorePath());
 		TrustManager[] tms = IfmapJHelper.getTrustManagers(is,
 				mConfig.getTruststorePass());
 		mSsrc = IfmapJ.createSSRC(mConfig.getUrl(), mConfig.getUser(),
 				mConfig.getPass(), tms);
 	}
 
 	public static void main(String[] args) {
 		System.out.println("perf1");
 		long maxBytes = Runtime.getRuntime().maxMemory();
 		System.out.println("Max memory: " + maxBytes / 1024 / 1024 + "M");
 		try {
 			Perf1 p = new Perf1(args);
 			p.start();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InitializationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IfmapErrorResult e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IfmapException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (EndSessionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
