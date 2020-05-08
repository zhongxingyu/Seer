 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.filters.plaintext;
 
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.filters.plaintext.base.BasePlainTextFilter;
 import net.sf.okapi.filters.plaintext.common.CompoundFilter;
 import net.sf.okapi.filters.plaintext.paragraphs.ParaPlainTextFilter;
import net.sf.okapi.filters.plaintext.Parameters;
 import net.sf.okapi.filters.plaintext.regex.RegexPlainTextFilter;
 import net.sf.okapi.filters.plaintext.spliced.SplicedLinesFilter;
 
 /**
  * Plain Text filter, processes text files encoded in ANSI, Unicode, UTF-8, UTF-16. Provides the byte-order mask detection. 
  * The filter is aware of the following line terminators:
  * <ul><li>Carriage return character followed immediately by a newline character ("\r\n")
  * <li>Newline (line feed) character ("\n")
  * <li>Stand-alone carriage return character ("\r")</ul><p> 
  * 
  * @version 0.1, 09.06.2009
  * @author Sergei Vasilyev
  */
 
 public class PlainTextFilter extends CompoundFilter{
 
 	public static final String FILTER_NAME	= "okf_plaintext";
 	public static final String FILTER_MIME	= MimeTypeMapper.PLAIN_TEXT_MIME_TYPE;
 	
 	public PlainTextFilter() {
 		
 		super();	
 		
 		setName(FILTER_NAME);
 		setMimeType(FILTER_MIME);
		setParameters(new Parameters());	// Plain Text Filter parameters
 		
 		addSubFilter(BasePlainTextFilter.class);
 		addSubFilter(ParaPlainTextFilter.class);
 		addSubFilter(SplicedLinesFilter.class);
 		addSubFilter(RegexPlainTextFilter.class);
 	}
 	
 }
