 package org.epstudios.epcoding;
 
 import java.util.ArrayList;
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
 	private boolean noAnalysis = false;
 
 	private static final List<String> mappingCodes = Arrays.asList("93609",
 			"93613");
 	private static final Set<String> mappingCodeSet = new HashSet<String>(
 			mappingCodes);
 	// below doesn't include AV node ablation or VT ablation as mapping not
 	// separately billable
 	private static final List<String> ablationCodes = Arrays.asList("93653",
 			"93656");
 	private static final Set<String> ablationCodeSet = new HashSet<String>(
 			ablationCodes);
 
 	// bad coding combos
 	private final static List<List<String>> combos = createCombos();
 
 	private final static List<List<String>> createCombos() {
 		List<List<String>> comboList = new ArrayList<List<String>>();
 		// too many cardioversion types
 		comboList.add(Arrays.asList("92960", "92961"));
 		// too many new PPMs
 		comboList.add(Arrays.asList("33206", "33207", "33208"));
 		// no in and out with an ILR
 		comboList.add(Arrays.asList("33282", "33284"));
 		// Can't combine different types of EP testing together
 		comboList.add(Arrays.asList("93600", "93619", "93620"));
 		// Don't combine gen replacement with gen removal
 		comboList.add(Arrays.asList("33227", "33233"));
 		comboList.add(Arrays.asList("33228", "33233"));
 		comboList.add(Arrays.asList("33229", "33233"));
 		comboList.add(Arrays.asList("33262", "33241"));
 		comboList.add(Arrays.asList("33263", "33241"));
 		comboList.add(Arrays.asList("33264", "33241"));
 		// multiple PPM generator replacements
 		comboList.add(Arrays.asList("33227", "33228", "33229"));
 		// TODO same for ICD
 		return comboList;
 	}
 
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
 
 	public void setNoAnalysis(boolean value) {
 		this.noAnalysis = value;
 	}
 
 	public String analysis() {
 		// Note that ERRORs should be tested first, then WARNINGs
 		// Note unicode doesn't consistently work on different devices.
 		// use ASCII! Maybe will work in iOS?
 		final String WARNING = "! "; // "?? "; // \u26A0 "; // ! in triangle
 		final String ERROR = "!! "; // "\u2620 "; // skull and bones
 									// "\u24CD "; x in circle
 		final String OK = ""; // "\u263A "; // smiley face
 		// quick exit for no codes selected
 		if (noPrimaryCodes && noSecondaryCodes)
 			return getMessage(WARNING, R.string.no_codes_selected_label,
 					R.string.empty_message);
 		// quick exit for noAnalysis
 		if (noAnalysis)
 			return getMessage(WARNING,
 					R.string.no_code_analysis_performed_message,
 					R.string.empty_message);
		// TODO fix this
		// no code checking in All & Misc Codes
		if (moduleHasNoSecondaryCodes)
			return getMessage(WARNING,
					R.string.no_code_analysis_performed_message,
					R.string.empty_message);
 		// Now the details checks
 		String message = "";
 		if (noPrimaryCodes)
 			message += getMessage(ERROR, R.string.no_primary_codes_message,
 					R.string.no_primary_codes_verbose_message);
		if (noSecondaryCodes) // already checked moduleHasNoSecondaryCodes
 			message += getMessage(WARNING, R.string.no_secondary_codes_message,
 					R.string.no_secondary_codes_verbose_message);
 		if (allAddOnCodes())
 			message += getMessage(ERROR, R.string.all_addons_error_message,
 					R.string.all_addons_verbose_error_message);
 		// check for forbidden code combos
 		// first make List and Set of code numbers
 		String[] codeNumbers = new String[codes.length];
 		for (int i = 0; i < codes.length; ++i) {
 			if (codes[i] != null)
 				codeNumbers[i] = codes[i].getCodeNumber();
 		}
 		List<String> codeNumberList = Arrays.asList(codeNumbers);
 		Set<String> codeNumberSet = new HashSet<String>(codeNumberList);
 		if (containsCodeNumbers(mappingCodes, codeNumberSet))
 			message += getCodingErrorMessage(ERROR, mappingCodes,
 					R.string.duplicate_mapping_codes_verbose_error_message);
 		if (noMappingCodesForAblation(codeNumberList))
 			message += getMessage(WARNING, R.string.no_mapping_codes_message,
 					R.string.no_mapping_codes_verbose_message);
 		if (avnAblationHasMappingCodes(codeNumberSet))
 			message += getMessage(WARNING,
 					R.string.mapping_with_avn_ablation_warning,
 					R.string.mapping_with_avn_ablation_verbose_warning);
 		if (avnAblationHasEpTestingCodes(codeNumberSet))
 			message += getMessage(WARNING,
 					R.string.ep_testing_with_avn_ablation_warning,
 					R.string.ep_testing_with_avn_ablation_verbose_warning);
 		// search for duplicates
 		// e.g. both cardioversion, ep testing with and without induction,
 		// combining pacer, defib codes incorrectly
 		String badComboCodes = getBadComboCodes(codeNumberSet);
 		if (badComboCodes.length() > 0) {
 			message += getMessageFromStrings(
 					ERROR,
 					badComboCodes,
 					context.getString(R.string.bad_combo_codes_verbose_error_message));
 		}
 		if (message.length() == 0) // no errors!
 			message = getMessage(OK, R.string.no_code_errors_message,
 					R.string.empty_message);
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
 
 	private boolean containsCodeNumbers(final List<String> badNumbers,
 			final Set<String> codeNumberSet) {
 		return codeNumberSet.containsAll(badNumbers);
 	}
 
 	private boolean noMappingCodesForAblation(final List<String> codeNumbers) {
 		boolean noMappingCodes = true;
 		boolean hasAblationCodes = false;
 		for (String codeNumber : codeNumbers) {
 			if (ablationCodeSet.contains(codeNumber))
 				hasAblationCodes = true;
 			if (mappingCodeSet.contains(codeNumber))
 				noMappingCodes = false;
 		}
 		return (hasAblationCodes && noMappingCodes);
 	}
 
 	private boolean avnAblationHasMappingCodes(final Set<String> codeNumbers) {
 		return codeNumbers.contains("93650")
 				&& (codeNumbers.contains("93609") || codeNumbers
 						.contains("93613"));
 	}
 
 	private boolean avnAblationHasEpTestingCodes(final Set<String> codeNumbers) {
 		return codeNumbers.contains("93650")
 				&& (codeNumbers.contains("93600")
 						|| codeNumbers.contains("93619") || codeNumbers
 							.contains("93620"));
 	}
 
	// private String getBadComboCodes(final Set<String> codeNumbers) {
	// String codes = "";
	// for (Combo badCombo : badCombos) {
	// if (codeNumbers.contains(badCombo.getS1())
	// && codeNumbers.contains(badCombo.getS2()))
	// codes += badCombo.toString();
	// }
	// return codes;
	// }

 	// test all bad combos
 	private String getBadComboCodes(final Set<String> codeNumbers) {
 		String codes = "";
 		for (List<String> combo : combos)
 			if (hasBadCombo(combo, codeNumbers))
 				codes += combo.toString();
 		return codes;
 
 	}
 
 	// test a list of codes to see if > 1 included
 	private boolean hasBadCombo(final List<String> badCodes,
 			final Set<String> codes) {
 		int count = 0;
 		for (String badCode : badCodes) {
 			if (codes.contains(badCode))
 				count++;
 		}
 		return count > 1;
 	}
 
 	// returns string of codes in this format: "[99999, 99991]"
 	private String getCodeString(final List<String> codeList) {
 		return codeList.toString();
 	}
 
 	private String getCodingErrorMessage(final String threat,
 			final List<String> codeList, final int details) {
 		return getMessageFromStrings(threat, getCodeString(codeList),
 				context.getString(details));
 	}
 
 	private String getMessage(final String threat, final int brief,
 			final int details) {
 		return getMessageFromStrings(threat, context.getString(brief),
 				context.getString(details));
 	}
 
 	private String getMessageFromStrings(final String threat,
 			final String brief, final String details) {
 		return "\n" + threat + (verbose ? brief + " " + details : brief) + "\n";
 	}
 }
