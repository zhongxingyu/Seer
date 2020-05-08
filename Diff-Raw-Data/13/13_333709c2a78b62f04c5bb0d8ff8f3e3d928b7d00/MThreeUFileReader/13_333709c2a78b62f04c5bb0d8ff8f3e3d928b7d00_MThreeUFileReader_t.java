 /*
 Copyright  2011 Philipp Eichhorn.
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
 package de.fips.plugin.tinyaudioplayer.io;
 
 import static de.fips.plugin.tinyaudioplayer.io.TextLines.textLinesIn;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import lombok.Cleanup;
 import lombok.RequiredArgsConstructor;
 
 @RequiredArgsConstructor
 public class MThreeUFileReader {
 	private final IPlaylistFileVisitor visitor;
 
 	public void read(final File file) throws IOException {
 		visitor.visitBegin(file);
 		@Cleanup final TextLines lines = textLinesIn(file).ignoringEmptyLines();
 		boolean entryOpen = false;
 		int entryCounter = 0;
 		for (final String line : lines) {
 			if (line.startsWith("#")) {
 				if (line.toUpperCase().startsWith("#EXTINF")) {
 					visitor.visitEntryBegin();
 					entryOpen = true;
 					final int indA = line.indexOf(",", 0);
 					if (indA != -1) {
 						visitor.visitTitle(line.substring(indA + 1, line.length()));
 					}
 					final int indB = line.indexOf(":", 0);
 					if ((indB != -1) && (indB < indA)) {
 						visitor.visitLength(Long.valueOf( (line.substring(indB + 1, indA)).trim()));
 					}
 				} else {
 					visitor.visitComment(line);
 				}
 			} else {
 				if (!entryOpen) {
 					visitor.visitEntryBegin();
 				}
 				try {
					final URI uri = new URI(line);
					if (!uri.isAbsolute()) throw new URISyntaxException(line, "URI is not absolute");
					visitor.visitLocation(uri);
 				} catch (URISyntaxException  e) {
 					File f = new File(file.getParentFile(), line);
 					if (!f.exists()) {
 						f = new File(line);
 					}
 					visitor.visitLocation(f.toURI());
 				}
 				visitor.visitEntryEnd();
 				entryCounter++;
 				entryOpen = false;
 			}
 		}
 		visitor.visitNumberOfEntries(entryCounter);
 		visitor.visitEnd(file);
 	}
 }
