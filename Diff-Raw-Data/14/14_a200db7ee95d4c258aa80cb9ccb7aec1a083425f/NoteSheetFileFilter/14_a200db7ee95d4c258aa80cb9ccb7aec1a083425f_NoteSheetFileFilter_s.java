 // Copyright © 2011 Martin Ueding <dev@martin-ueding.de>
 
 /*
  * This file is part of jscribble.
  *
  * jscribble is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 2 of the License, or (at your option)
  * any later version.
  *
  * jscribble is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * jscribble.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package jscribble.notebook;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.regex.Pattern;
 
 /**
  * Filters filenames that belong to NoteSheet from a specific NoteBook.
  *
  * @author Martin Ueding <dev@martin-ueding.de>
  */
 public class NoteSheetFileFilter implements FilenameFilter {
 	/**
 	 * The pattern that every filename is matched against.
 	 */
 	private Pattern p = Pattern.compile("(\\d+)\\.png");
 
 	/**
 	 * Determines whether a file is accepted or not. A file is accepted if it
 	 * starts with the correct prefix and matches the regular expression.
 	 *
 	 * @param arg0 Ignored.
 	 * @param arg1 This is checked.
 	 */
 	@Override
 	public boolean accept(File arg0, String arg1) {
 		String[] nameparts = arg1.split(Pattern.quote(File.separator));
 		String fileBasename = nameparts[nameparts.length - 1];
		return p.matcher(fileBasename).matches();
 	}
 }
