 package org.gambi.tapestry5.cli.services.impl;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.ValidationException;
 import javax.validation.Validator;
 
 import org.apache.commons.cli.BasicParser;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.gambi.tapestry5.cli.data.ApplicationConfiguration;
 import org.gambi.tapestry5.cli.data.CLIOption;
 import org.gambi.tapestry5.cli.services.ApplicationConfigurationSource;
 import org.gambi.tapestry5.cli.services.CLIParser;
 import org.slf4j.Logger;
 
 public class CLIParserImpl implements CLIParser {
 
 	private Logger logger;
 	private ApplicationConfigurationSource applicationConfigurationSource;
 	private Validator validator;
 
 	private Collection<CLIOption> cliOptions;
 
 	// Internal implementation
 	private CommandLineParser parser;
 	private CommandLine parsedOptions;
 
 	// private Options configuration;
 
 	public CLIParserImpl(Logger logger,
 			ApplicationConfigurationSource applicationBeanSource,
 			Validator validator, Collection<CLIOption> _options) {
 
 		this.logger = logger;
 		this.validator = validator;
 		this.applicationConfigurationSource = applicationBeanSource;
 
 		validateAndMerge(_options);
 	}
 
 	/**
 	 * 
 	 */
 	private void validateAndMerge(Collection<CLIOption> _options) {
 		ArrayList<CLIOption> cliOptions = new ArrayList<CLIOption>();
 
 		for (CLIOption cliOption : _options) {
			if (!cliOptions.contains(_options)) {
				logger.debug("Adding " + _options);
 				cliOptions.add(cliOption);
 			} else {
				logger.debug("Merging options "
						+ cliOptions.get(cliOptions.indexOf(_options))
 						+ " with " + cliOption);
				cliOptions.get(cliOptions.indexOf(_options)).merge(cliOption);
 			}
 		}
 
 		// Validate (ideally here there are few options to check);
 		for (CLIOption cliOption1 : cliOptions) {
 			for (CLIOption cliOption2 : cliOptions) {
 				if (cliOption1.conflicts(cliOption2)) {
 
 					throw new IllegalArgumentException(
 							"Found conflicting CLIOptions: Option "
 									+ cliOption1 + " conflicts with "
 									+ cliOption2
 									+ ". Please revise your contributions !");
 				}
 			}
 		}
 
 		// Now update the variable
 		this.cliOptions = cliOptions;
 	}
 
 	/*
 	 * Initialize the objects to parse the command line
 	 * 
 	 * @return
 	 */
 	private Options setupParsing() {
 		Options configuration = new Options();
 		for (CLIOption cliOption : cliOptions) {
 
 			Option option = OptionBuilder.withLongOpt(cliOption.getLongOpt())
 					.hasArgs(cliOption.getnArgs())
 					.isRequired(cliOption.isRequired())
 					.withDescription(cliOption.getDescription())
 					.create(cliOption.getShortOpt());
 
 			logger.debug("Created option " + option);
 			configuration.addOption(option);
 		}
 		return configuration;
 	}
 
 	public void parse(String[] args) throws ParseException, ValidationException {
 		logger.debug("Parsing " + Arrays.toString(args));
 
 		Options configuration = setupParsing();
 		ApplicationConfiguration application = null;
 		try {
 			parser = new BasicParser();
 			// Parse the input line
 			parsedOptions = parser.parse(configuration, args);
 			// Gives value to each property of the application bean object
 
 			// NOT SURE ABOUT THE FORM HERE...
 			application = applicationConfigurationSource.get(parsedOptions);
 
 		} catch (ParseException exp) {
 			logger.error("Parsing failed.  Reason: " + exp.getMessage());
 
 			HelpFormatter formatter = new HelpFormatter();
 			formatter.printHelp("iter", configuration);
 
 			throw exp;
 		}
 
 		try {
 			boolean isValid = true;
 			for (Object property : application.getAllProperties()) {
 
 				Set<ConstraintViolation<Object>> result = validator
 						.validate(property);
 				for (ConstraintViolation<Object> viol : result) {
 					System.out.println("CLIParserImpl.validate() : "
 							+ viol.getMessage());
 				}
 				if (result.size() > 0) {
 					isValid = false;
 				}
 			}
 
 			if (!isValid) {
 				throw new ValidationException("The provided input is not valid");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new ValidationException();
 		}
 
 		try {
 
 			// For an easy use we export the options and the inputs as
 			// SystemProperties !
 			// Ideally we should get a reference to some SymbolProvider object
 			// to contribute... Or even, we should make CLIParse a
 			// SymbolProvider...
 
 			// TODO We cannot deal with String[] as inputs for the options !
 			for (Option option : parsedOptions.getOptions()) {
 				String symbolName = String.format("args:%s",
 						option.getLongOpt());
 				String symbolValue = option.getValue();
 				System.out.println("CLIParserImpl.parse(): Exporting "
 						+ symbolName + " == " + symbolValue);
 				System.getProperties().put(symbolName, symbolValue);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
