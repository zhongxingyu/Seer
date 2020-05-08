 // Copyright (c) 2012, Daniel Andersen (dani_ande@yahoo.dk)
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //
 // 1. Redistributions of source code must retain the above copyright notice, this
 //    list of conditions and the following disclaimer.
 // 2. Redistributions in binary form must reproduce the above copyright notice,
 //    this list of conditions and the following disclaimer in the documentation
 //    and/or other materials provided with the distribution.
 // 3. The name of the author may not be used to endorse or promote products derived
 //    from this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 // DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 // ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 // LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 // ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 // (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 // SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package com.trollsahead.qcumberless.engine;
 
 import com.trollsahead.qcumberless.gui.elements.*;
 import com.trollsahead.qcumberless.model.Locale;
 import com.trollsahead.qcumberless.model.PlayResult;
 import com.trollsahead.qcumberless.model.TagHistory;
 import com.trollsahead.qcumberless.util.ElementHelper;
 import com.trollsahead.qcumberless.util.HistoryHelper;
 import com.trollsahead.qcumberless.model.Step;
 import com.trollsahead.qcumberless.util.FileUtil;
 import com.trollsahead.qcumberless.util.Util;
 
 import java.io.*;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class FeatureLoader {
     public static void parseFeatureFilesAndPushToDesignerRoot(String[] files) {
         parseFeatureFilesAndPushToDesignerRoot(files, Element.ADD_STATE_NONE);
     }
 
     public static void parseFeatureFilesAndPushToDesignerRoot(String[] files, int addState) {
         DesignerEngine.resetFeatures();
         for (String filename : files) {
             DesignerEngine.featuresRoot.addChild(parseFeatureFile(filename, addState));
         }
         ElementHelper.unfoldAllScenariosIfNotTooMany();
     }
 
     public static FeatureElement parseFeatureFile(String filename, int addState) {
         BufferedReader in = null;
         try {
             in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
             StringBuilder feature = new StringBuilder();
             String line;
             while ((line = in.readLine()) != null) {
                 feature.append(line).append("\n");
             }
             return parseFeatureFile(feature, filename, addState);
         } catch (Exception e) {
             throw new RuntimeException("Error reading supported feature file " + filename, e);
         } finally {
             FileUtil.close(in);
         }
     }
 
     public static FeatureElement parseFeatureFile(StringBuilder source, String filename, int addState) {
         FeatureElement feature = new FeatureElement(BaseBarElement.ROOT_FEATURE_EDITOR);
         feature.setFilename(filename);
         ScenarioElement scenario = null;
         BackgroundElement background = null;
         StepElement step = null;
 
         String tags = null;
         String comment = null;
         boolean folded = true;
         boolean featureFolded = true;
         PlayResult playResult = null;
 
         for (String line : source.toString().split("\n")) {
             line = Util.removeTrailingSpaces(line);
             if (Util.isEmptyOrContainsOnlyTabs(line)) {
                 continue;
             }
             if (line.startsWith(getPlayResultPattern())) {
                 if ((addState & Element.ADD_STATE_RUN_OUTCOME) != 0) {
                     playResult = HistoryHelper.getPlayResultFromComment(line);
                 }
             } else if (line.matches(getFoldStatePattern())) {
                 if ((addState & Element.ADD_STATE_FOLD) != 0) {
                     folded = Boolean.parseBoolean(extractFoldState(line));
                 }
             } else if (line.matches(getFeaturePattern())) {
                 feature.setTitle(extractTitle(Pattern.compile(getFeaturePattern()), line));
                 feature.setTags(tags);
                 feature.setComment(comment);
                 feature.setPlayState(playResult);
                 setFoldState(feature, (addState & Element.ADD_STATE_FOLD) != 0, folded);
                 featureFolded = folded;
                 tags = null;
                 comment = null;
                 playResult = null;
             } else if (line.matches(getBackgroundPattern())) {
                 background = new BackgroundElement(BaseBarElement.ROOT_FEATURE_EDITOR);
                 background.setTitle("Background");
                 background.setTags(tags);
                 background.setComment(comment);
                 background.setPlayState(playResult);
                 tags = null;
                 comment = null;
                 playResult = null;
                 feature.addChild(background);
                 setFoldState(background, (addState & Element.ADD_STATE_FOLD) != 0, folded);
             } else if (line.matches(getScenarioPattern()) || line.matches(getScenarioOutlinePattern())) {
                 if (line.matches(getScenarioPattern())) {
                     scenario = new ScenarioElement(BaseBarElement.ROOT_FEATURE_EDITOR);
                     scenario.setTitle(extractTitle(Pattern.compile(getScenarioPattern()), line));
                 } else {
                     scenario = new ScenarioOutlineElement(BaseBarElement.ROOT_FEATURE_EDITOR);
                     scenario.setTitle(extractTitle(Pattern.compile(getScenarioOutlinePattern()), line));
                 }
                 scenario.setTags(tags);
                 scenario.setComment(comment);
                 scenario.setPlayState(playResult);
                 tags = null;
                 comment = null;
                 playResult = null;
                 feature.addChild(scenario);
                 setFoldState(scenario, (addState & Element.ADD_STATE_FOLD) != 0, folded);
             } else if (line.matches(getTagPattern())) {
                 tags = extractTags(line);
             } else if (line.matches(getCommentPattern())) {
                 comment = extractComment(line);
             } else if (line.matches(getExamplesPattern())) {
                 step = ((ScenarioOutlineElement) scenario).getExamplesElement();
                 ((ExamplesElement) step).clearTable();
             } else if (line.matches(getTableRowPattern())) {
                 step.addRowToTable(extractTableRow(line));
             } else {
                 if (comment != null) {
                     BaseBarElement commentElement = addStep(feature, background, scenario, comment);
                     setFoldState(commentElement, (addState & Element.ADD_STATE_FOLD) != 0, folded);
                 }
                 BaseBarElement element = addStep(feature, background, scenario, line);
                 if (element instanceof StepElement || element instanceof ExamplesElement) {
                     step = (StepElement) element;
                 }
                 if (element != null) {
                     element.setPlayState(playResult);
                     setFoldState(element, (addState & Element.ADD_STATE_FOLD) != 0, folded);
                 }
                 tags = null;
                 comment = null;
                 playResult = null;
             }
         }
         if (featureFolded) {
             feature.fold(false);
         } else {
             feature.unfold(false);
         }
         feature.animation.alphaAnimation.setAlpha(BaseBarElement.BAR_TRANSPARENCY);
         return feature;
     }
 
     private static void setFoldState(BaseBarElement element, boolean shouldUpdate, boolean folded) {
         if (!shouldUpdate) {
             return;
         }
         if (!element.isFoldable()) {
             element.animation.alphaAnimation.setAlpha(folded ? 0.0f : BaseBarElement.BAR_TRANSPARENCY);
             return;
         }
         if (folded) {
             element.fold();
         } else {
             element.unfold();
         }
     }
 
     public static Map<String, TagHistory> extractTagHistory(Map<String, TagHistory> tagHistory, String... features) {
         for (String filename : features) {
             BufferedReader in = null;
             try {
                 in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
                 String tags = null;
                 String line;
                 while ((line = in.readLine()) != null) {
                     line = Util.removeTrailingSpaces(line);
                     if (line.startsWith(getPlayResultPattern())) {
                         updateTagHistory(line, tags, tagHistory);
                         tags = null;
                     } else if (line.matches(getTagPattern())) {
                         tags = extractTags(line);
                     }
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             } finally {
                 FileUtil.close(in);
             }
         }
         return tagHistory;
     }
 
     private static void updateTagHistory(String line, String tags, Map<String, TagHistory> tagHistory) {
         if (Util.isEmpty(tags)) {
             return;
         }
         PlayResult playResult = HistoryHelper.getPlayResultFromComment(line);
         for (String tag : Util.stringToTagList(tags)) {
             TagHistory history = tagHistory.get(tag);
             if (history == null) {
                 history = new TagHistory(tag);
                 tagHistory.put(tag, history);
             }
             history.update(playResult);
         }
     }
 
     private static String[] extractTableRow(String line) {
         Matcher matcher = Pattern.compile(getTableRowPattern()).matcher(line);
         matcher.find();
         String[] cols = matcher.group(1).split("\\|");
         for (int i = 0; i < cols.length; i++) {
             cols[i] = cols[i].trim();
         }
         return cols;
     }
 
     private static BaseBarElement addStep(BaseBarElement feature, BaseBarElement background, BaseBarElement scenario, String line) {
         if (scenario != null) {
             return addStepToScenario(scenario, line);
         } else if (background != null) {
             return addStepToScenario(background, line);
         } else if (feature != null && !Util.isEmpty(line)) {
             feature.setTitle(feature.getTitle() + Util.PSEUDO_NEWLINE + line);
         }
         return null;
     }
 
     private static BaseBarElement addStepToScenario(BaseBarElement scenario, String line) {
         if (scenario == null || Util.isEmpty(line)) {
             return null;
         }
         BaseBarElement stepElement;
         if (Util.startsWithIgnoreWhitespace(line, "#")) {
             stepElement = new CommentElement(BaseBarElement.ROOT_FEATURE_EDITOR, Util.removeCommentFromLine(line));
         } else {
             stepElement = new StepElement(BaseBarElement.ROOT_FEATURE_EDITOR, line, findMatchingStep(line));
         }
         scenario.addChild(stepElement);
         return stepElement;
     }
 
     public static Step findMatchingStep(String line) {
         for (Step step : DesignerEngine.stepDefinitions) {
             if (step.matches(line)) {
                 return new Step(step, line);
             }
         }
         return new Step(line, false);
     }
 
     private static String extractComment(String line) {
         Matcher matcher = Pattern.compile(getCommentPattern()).matcher(line);
         matcher.find();
         return matcher.group(1);
     }
 
     public static String extractTags(String line) {
         Matcher matcher = Pattern.compile(getTagPattern()).matcher(line);
         matcher.find();
         return matcher.group(1);
     }
 
     private static String extractTitle(Pattern pattern, String line) {
         Matcher matcher = pattern.matcher(line);
         matcher.find();
         return matcher.group(1);
     }
 
     private static String extractFoldState(String line) {
         Matcher matcher = Pattern.compile(getFoldStatePattern()).matcher(line);
         matcher.find();
         return matcher.group(1);
     }
 
     private static String getFeaturePattern() {
         return Locale.getString("feature") + ": (.*)";
     }
 
     private static String getScenarioPattern() {
         return "^\\s*" + Locale.getString("scenario") + ": (.*)";
     }
 
     private static String getScenarioOutlinePattern() {
         return "^\\s*" + Locale.getString("scenario outline") + ": (.*)";
     }
 
     private static String getBackgroundPattern() {
         return "^\\s*" + Locale.getString("background") + ":";
     }
 
     public static String getTagPattern() {
         return "[^@]*(((@[^@\\s]*)\\s*)+)";
     }
 
     private static String getCommentPattern() {
         return "^\\s*(#.*)";
     }
 
     private static String getPlayResultPattern() {
         return HistoryHelper.COMMENT_QCUMBERLESS;
     }
 
     private static String getExamplesPattern() {
         return "^\\s*" + Locale.getString("Examples") + ":";
     }
 
     private static String getTableRowPattern() {
         return "^\\s*\\|(.*)\\|\\s*$";
     }
 
     private static String getFoldStatePattern() {
         return "^\\s*# foldstate: (.*)";
     }
 }
