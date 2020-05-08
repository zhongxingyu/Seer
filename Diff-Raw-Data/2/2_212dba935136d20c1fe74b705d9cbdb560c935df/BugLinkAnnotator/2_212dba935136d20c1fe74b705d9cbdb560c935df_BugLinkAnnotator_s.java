 // buglinky - A robot for adding bugtracker links to a wave
 // Copyright 2009 Eric Kidd
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package buglinky;
 
 import java.util.regex.Matcher;
 
 import com.google.wave.api.Range;
 import com.google.wave.api.TextView;
 
 /** Add bug links to a blip. */
 class BugLinkAnnotator extends Annotator {
 	/** The URL to a bug, minus the actual bug number. */
 	private String bugUrl;
 
 	/** Create a BugLinkAnnotator for the specified URL. */
 	public BugLinkAnnotator(String bugUrl) {
 		this.bugUrl = bugUrl;
 	}
 
 	/** Return a regular expression matching the text we want to process. */
 	protected String getPattern() {
		return "(?:bug|issue) #(\\d+)";
 	}
 	
 	/** Process a regular expression match. */
 	protected void processMatch(TextView doc, Range range, Matcher match) {
 		maybeAnnotate(doc, range, "link/manual", bugUrl.concat(match.group(1)));
 	}
 }
