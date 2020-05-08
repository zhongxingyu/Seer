 package org.epstudios.epcoding;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import android.content.Context;
 
 public class CodeAnalyzer {
 	private final Code[] codes;
 	private final boolean noPrimaryCodes;
 	private final boolean noSecondaryCodes;
 	private final boolean moduleHasNoSecondaryCodes;
 	private final Context context;
 	private boolean verbose = false;
 
 	private final List<String> duplicateMappingCodes = Arrays.asList("93609",
 			"93613");
 
 	// enum here for error flags??
 
 	public CodeAnalyzer(final Code[] codes, final boolean noPrimaryCodes,
 			final boolean noSecondaryCodes,
 			final boolean moduleHasNoSecondaryCodes, final Context context) {
 		this.codes = codes;
 		this.noPrimaryCodes = noPrimaryCodes;
 		this.noSecondaryCodes = noSecondaryCodes;
 		this.moduleHasNoSecondaryCodes = moduleHasNoSecondaryCodes;
 		this.context = context;
 	}
 
 	public void setVerbose(boolean verbose) {
 		this.verbose = verbose;
 	}
 
 	public String analysis() {
 		// Note unicode doesn't consistently work on different devices.
 		// use ASCII! Maybe will work in iOS?
 		final String WARNING = "?? "; // \u26A0 "; // ! in triangle
 		final String ERROR = "!! "; // "\u2620 "; // skull and bones
 									// "\u24CD "; x in circle
 		final String OK = ""; // "\u263A "; // smiley face
 
 		String message = "";
 		if (noPrimaryCodes && noSecondaryCodes)
 			message = getMessage(WARNING, R.string.no_codes_selected_label,
 					R.string.empty_message);
 		else {
 			if (noPrimaryCodes)
 				message += getMessage(ERROR, R.string.no_primary_codes_message,
 						R.string.no_primary_codes_verbose_message);
 			if (noSecondaryCodes && !moduleHasNoSecondaryCodes)
 				message += getMessage(WARNING,
 						R.string.no_secondary_codes_message,
 						R.string.no_secondary_codes_verbose_message);
 			if (allAddOnCodes())
 				message += getMessage(ERROR, R.string.all_addons_error_message,
 						R.string.all_addons_verbose_error_message);
 			// check for forbidden code combos
 			// first make array of code numbers
 			String[] codeNumbers = new String[codes.length];
 			for (int i = 0; i < codes.length; ++i) {
 				if (codes[i] != null)
 					codeNumbers[i] = codes[i].getCodeNumber();
 			}
 			Set<String> numberSet = new HashSet<String>(
 					Arrays.asList(codeNumbers));
 			if (containsCodesNumbers(duplicateMappingCodes, numberSet))
				message += getCodingErrorMessage(ERROR, duplicateMappingCodes,
 						R.string.duplicate_mapping_codes_verbose_error_message);
 			if (message.length() == 0) // no errors!
 				message = OK
 						+ context.getString(R.string.no_code_errors_message);
 		}
 		return message;
 	}
 
 	private boolean allAddOnCodes() {
 		boolean allAddOns = true;
 		for (int i = 0; i < codes.length; ++i) {
 			if (codes[i] != null && !codes[i].isAddOn())
 				allAddOns = false;
 		}
 		return allAddOns;
 	}
 
 	private boolean containsCodesNumbers(final List<String> badNumbers,
 			final Set<String> codeNumbers) {
 		return codeNumbers.containsAll(badNumbers);
 	}
 
	// returns string of codes in this format: "[99999, 99991]"
	private String getCodeString(final List<String> codeList) {
		return codeList.toString();
	}

	private String getCodingErrorMessage(final String threat,
			final List<String> codeList, final int details) {
		return threat
				+ (verbose ? getCodeString(codeList) + " "
						+ context.getString(details) : getCodeString(codeList))
				+ "\n";
	}

 	private String getMessage(final String threat, final int brief,
 			final int details) {
 		return threat
 				+ (verbose ? context.getString(brief) + " "
 						+ context.getString(details) : context.getString(brief))
 				+ "\n";
 	}
 }
