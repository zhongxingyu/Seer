 /*
  * Copyright (c) 2013 mgm technology partners GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mgmtp.perfload.core.client.web.response;
 
 import static com.google.common.base.Preconditions.checkArgument;
 
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import net.jcip.annotations.Immutable;
 import net.jcip.annotations.ThreadSafe;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.mgmtp.perfload.core.client.util.PlaceholderContainer;
 import com.mgmtp.perfload.core.client.web.config.annotations.AllowedStatusCodes;
 import com.mgmtp.perfload.core.client.web.config.annotations.ForbiddenStatusCodes;
 import com.mgmtp.perfload.core.client.web.template.RequestTemplate.DetailExtraction;
 
 /**
  * Default response parser implementation.
  * 
  * @author rnaegele
  */
 @Singleton
 @ThreadSafe
 @Immutable
 public final class DefaultResponseParser implements ResponseParser {
 	private final Logger log = LoggerFactory.getLogger(getClass());
 
 	private final List<Pattern> errorPatterns;
 	private final Set<Integer> allowedStatusCodes;
 	private final Set<Integer> forbiddenStatusCodes;
 
 	/**
 	 * @param allowedStatusCodes
 	 *            status codes that constitute a valid response; if empty, all status codes are
 	 *            valid
 	 * @param forbiddenStatusCodes
 	 *            status codes that constitutes an invalid response
 	 * @param errorPatterns
 	 *            A list of regular expression patterns used for parsing the response. A response is
 	 *            considered invalid if any of these patterns is found in the response using
 	 *            {@link Matcher#find()}.
 	 */
 	@Inject
 	public DefaultResponseParser(@AllowedStatusCodes final Set<Integer> allowedStatusCodes,
 			@ForbiddenStatusCodes final Set<Integer> forbiddenStatusCodes, final List<Pattern> errorPatterns) {
 		checkArgument(allowedStatusCodes != null, "Parameter 'allowdStatusCodes' may not be null.");
 		checkArgument(forbiddenStatusCodes != null, "Parameter 'forbiddenStatusCodes' may not be null.");
 		checkArgument(errorPatterns != null, "Parameter 'forbiddenStatusCodes' may not be null.");
 
 		this.allowedStatusCodes = ImmutableSet.copyOf(allowedStatusCodes);
 		this.forbiddenStatusCodes = ImmutableSet.copyOf(forbiddenStatusCodes);
 		this.errorPatterns = ImmutableList.copyOf(errorPatterns);
 	}
 
 	/**
 	 * Validates the response checking for forbidden status codes, valid status codes, and parsing
 	 * for errors in the response body, in that order.
 	 * 
 	 * @throws InvalidResponseException
 	 *             if the response is not valid
 	 */
 	@Override
 	public void validate(final ResponseInfo responseInfo) throws InvalidResponseException {
 		log.debug("Validating response...");
 
 		int statusCode = responseInfo.getStatusCode();
 		boolean success = !forbiddenStatusCodes.contains(statusCode);
 		if (!allowedStatusCodes.isEmpty() && !allowedStatusCodes.contains(statusCode)) {
 			success = false;
 		}
 		if (!success) {
 			throw new InvalidResponseException("Response code not allowed: " + statusCode);
 		}
 
 		String body = responseInfo.getResponseBodyAsString();
 		if (body != null) {
 			for (Pattern pattern : errorPatterns) {
 				Matcher matcher = pattern.matcher(body);
 				if (matcher.find()) {
 					throw new InvalidResponseException("Error pattern matched: " + pattern);
 				}
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
	 * @see DetailExtraction#DetailExtraction(String, String, String, String, String, String)
 	 */
 	@Override
 	public void extractDetails(final ResponseInfo responseInfo, final List<DetailExtraction> detailExtractions,
 			final PlaceholderContainer placeholderContainer) throws PatternNotFoundException {
 		log.debug("Extracting details from response...");
 
 		for (DetailExtraction detailExtraction : detailExtractions) {
 			String name = detailExtraction.getName();
 			boolean indexed = detailExtraction.isIndexed();
 			String regex = detailExtraction.getPattern();
 			Pattern pattern = Pattern.compile(regex);
 			Matcher matcher = pattern.matcher(responseInfo.getResponseBodyAsString());
 
 			boolean found = false;
 			for (int i = 0;; ++i) {
 				if (matcher.find()) {
 					found = true;
 					String extractedValue = matcher.group(detailExtraction.getGroupIndex());
 					if (indexed) {
 						// multiple matches possible, so don't break out of loop
 						String indexedName = name + "#" + i;
 						log.debug("Extracted indexed detail '{}': {}", indexedName, extractedValue);
 						placeholderContainer.put(indexedName, extractedValue);
 						responseInfo.addDetailExtractionName(indexedName);
 					} else {
 						log.debug("Extracted detail '{}': {}", name, extractedValue);
 						placeholderContainer.put(name, extractedValue);
 						responseInfo.addDetailExtractionName(name);
 						break;
 					}
 				} else {
 					break;
 				}
 			}
 			if (!found) {
 				String defaultValue = detailExtraction.getDefaultValue();
 				if (defaultValue != null) {
 					if (indexed) {
 						String indexedName = name + "#0";
 						log.info("Detail '{}' not found in response. Using default indexed value: {}", indexedName, defaultValue);
 						placeholderContainer.put(indexedName, defaultValue);
 						responseInfo.addDetailExtractionName(indexedName);
 					} else {
 						log.info("Detail '{}' not found in response. Using default value: {}", name, defaultValue);
 						placeholderContainer.put(name, defaultValue);
 						responseInfo.addDetailExtractionName(name);
 					}
 				} else if (detailExtraction.isFailIfNotFound()) {
 					throw new PatternNotFoundException("Pattern '" + pattern
 							+ "' not found in response and no default value set!");
 				}
 			}
 		}
 	}
 }
