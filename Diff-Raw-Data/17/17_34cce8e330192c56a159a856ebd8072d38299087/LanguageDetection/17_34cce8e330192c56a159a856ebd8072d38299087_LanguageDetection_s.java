 /*
  * SonarQube, open source software quality management tool.
  * Copyright (C) 2008-2013 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * SonarQube is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * SonarQube is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package org.sonar.batch.scan.filesystem;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.CoreProperties;
 import org.sonar.api.batch.fs.InputFile;
 import org.sonar.api.batch.fs.internal.PathPattern;
 import org.sonar.api.config.Settings;
 import org.sonar.api.resources.Language;
 import org.sonar.api.resources.Languages;
 import org.sonar.api.utils.MessageException;
 
 import javax.annotation.CheckForNull;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Detect language of a source file based on its suffix and configured patterns.
  */
 class LanguageDetection {
 
   private static final Logger LOG = LoggerFactory.getLogger(LanguageDetection.class);
 
   /**
    * Lower-case extension -> languages
    */
   private final Map<String, PathPattern[]> patternsByLanguage = Maps.newLinkedHashMap();
   private final List<String> languagesToConsider = Lists.newArrayList();
   private final String forcedLanguage;
 
   LanguageDetection(Settings settings, Languages languages) {
     for (Language language : languages.all()) {
       String[] filePatterns = settings.getStringArray(getFileLangPatternPropKey(language.getKey()));
       PathPattern[] pathPatterns = PathPattern.create(filePatterns);
       if (pathPatterns.length > 0) {
         patternsByLanguage.put(language.getKey(), pathPatterns);
       } else {
         // If no custom language pattern is defined then fallback to suffixes declared by language
         String[] patterns = Arrays.copyOf(language.getFileSuffixes(), language.getFileSuffixes().length);
         for (int i = 0; i < patterns.length; i++) {
           String suffix = patterns[i];
           String extension = sanitizeExtension(suffix);
           patterns[i] = "**/*." + extension;
         }
         PathPattern[] defaultLanguagePatterns = PathPattern.create(patterns);
         patternsByLanguage.put(language.getKey(), defaultLanguagePatterns);
         LOG.debug("Declared extensions of language " + language + " were converted to " + getDetails(language.getKey()));
       }
     }
 
     forcedLanguage = StringUtils.defaultIfBlank(settings.getString(CoreProperties.PROJECT_LANGUAGE_PROPERTY), null);
     // First try with lang patterns
     if (forcedLanguage != null) {
       if (!patternsByLanguage.containsKey(forcedLanguage)) {
         throw MessageException.of("No language is installed with key '" + forcedLanguage + "'. Please update property '" + CoreProperties.PROJECT_LANGUAGE_PROPERTY + "'");
       }
       languagesToConsider.add(forcedLanguage);
     } else {
       languagesToConsider.addAll(patternsByLanguage.keySet());
     }
   }
 
   Map<String, PathPattern[]> patternsByLanguage() {
     return patternsByLanguage;
   }
 
   @CheckForNull
   String language(InputFile inputFile) {
     String detectedLanguage = null;
     for (String languageKey : languagesToConsider) {
       PathPattern[] patterns = patternsByLanguage.get(languageKey);
       if (patterns != null) {
         for (PathPattern pathPattern : patterns) {
           if (pathPattern.match(inputFile, false)) {
             if (detectedLanguage == null) {
               detectedLanguage = languageKey;
               break;
             } else {
               // Language was already forced by another pattern
               throw MessageException.of("Language of file '" + inputFile.relativePath() + "' can not be decided as the file matches patterns of both " + getDetails(detectedLanguage)
                 + " and " + getDetails(languageKey));
             }
           }
         }
       }
     }
     if (detectedLanguage != null) {
      LOG.debug("Language of file '" + inputFile.relativePath() + "' was detected to be '" + detectedLanguage + "'");
       return detectedLanguage;
     }
 
     // Check if deprecated sonar.language is used and we are on a language without declared extensions
     if (forcedLanguage != null) {
       // Languages without declared suffixes match everything
       if (patternsByLanguage.get(forcedLanguage).length == 0) {
         return forcedLanguage;
       }
     }
     return null;
   }
 
   private String getFileLangPatternPropKey(String languageKey) {
     return "sonar.lang.patterns." + languageKey;
   }
 
   private String getDetails(String detectedLanguage) {
     return getFileLangPatternPropKey(detectedLanguage) + " : " + Joiner.on(",").join(patternsByLanguage.get(detectedLanguage));
   }
 
   static String sanitizeExtension(String suffix) {
     return StringUtils.lowerCase(StringUtils.removeStart(suffix, "."));
   }
 }
