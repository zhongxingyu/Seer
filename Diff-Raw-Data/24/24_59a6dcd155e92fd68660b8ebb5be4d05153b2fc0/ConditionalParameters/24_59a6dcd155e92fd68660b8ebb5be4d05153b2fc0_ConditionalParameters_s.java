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
 
 package net.sf.okapi.filters.openxml;
 
 import java.util.Iterator;
 import java.util.TreeSet;
 
 import net.sf.okapi.common.BaseParameters;
 
 public class ConditionalParameters extends BaseParameters {
 	
 	public boolean bPreferenceTranslateDocProperties; // Word, Powerpoint, Excel Doc Properties
 	public boolean bPreferenceTranslateComments; // Word, Powerpoint, Excel Comments
 	public boolean bPreferenceTranslatePowerpointNotes; // Powerpoint Notes
 	public boolean bPreferenceTranslatePowerpointMasters; // Powerpoint Masters
 	public boolean bPreferenceTranslateWordHeadersFooters; // Word Headers and Footers
 	public boolean bPreferenceTranslateWordAllStyles; // Word false to exclude hsExcludeWordStyles
 	public boolean bPreferenceTranslateWordHidden; // Word Hidden text
 	public boolean bPreferenceTranslateExcelExcludeColors; // Excel exclude tsExcelExcludedColors
 	  // DWH 6-12-09 don't translate text in Excel in some colors 
 	public boolean bPreferenceTranslateExcelExcludeColumns; // Excel exclude specific cells
 	  // DWH 6-12-09 don't translate text in Excel in some specified cells
 	public TreeSet<String> tsExcelExcludedColors; // exclude if bPreferenceTranslateExcelExcludeColors
 	public TreeSet<String> tsExcelExcludedColumns; // exclude if bPreferenceTranslateExcelExcludeCells 
 	public TreeSet<String> tsExcludeWordStyles; // exclude if !bPreferenceTranslateWordAllStyles
 	
 	public ConditionalParameters () {
 		reset();
 		toString(); // fill the list
 	}
 	
 	public void reset () {
 		bPreferenceTranslateDocProperties = true; // Word, Powerpoint, Excel Doc Properties
 		bPreferenceTranslateComments = true; // Word, Powerpoint, Excel Comments
 		bPreferenceTranslatePowerpointNotes = true; // Powerpoint Notes
 		bPreferenceTranslatePowerpointMasters = true; // Powerpoint Masters
 		bPreferenceTranslateWordHeadersFooters = true; // Word Headers and Footers
 		bPreferenceTranslateWordAllStyles = true; // Word false to exclude tsExcludeWordStyles
 		bPreferenceTranslateWordHidden = true; // Word Hidden text
 		bPreferenceTranslateExcelExcludeColors = false; // Excel exclude tsExcelExcludedColors
 		bPreferenceTranslateExcelExcludeColumns = false; // Excel exclude specific cells
 		tsExcelExcludedColors = new TreeSet<String>(); // exclude if bPreferenceTranslateExcelExcludeColors
 		tsExcelExcludedColumns = new TreeSet<String>(); // exclude if bPreferenceTranslateExcelExcludeCells 
 		tsExcludeWordStyles = new TreeSet<String>(); // exclude if !bPreferenceTranslateWordAllStyles
 	}
 
 	public void fromString (String data) {
 		int i,siz;
 		reset();
 		buffer.fromString(data);
 		bPreferenceTranslateDocProperties = buffer.getBoolean("bPreferenceTranslateDocProperties", bPreferenceTranslateDocProperties);
 		bPreferenceTranslateComments = buffer.getBoolean("bPreferenceTranslateComments", bPreferenceTranslateComments);
 		bPreferenceTranslatePowerpointNotes = buffer.getBoolean("bPreferenceTranslatePowerpointNotes", bPreferenceTranslatePowerpointNotes);
 		bPreferenceTranslatePowerpointMasters = buffer.getBoolean("bPreferenceTranslatePowerpointMasters", bPreferenceTranslatePowerpointMasters);
 		bPreferenceTranslateWordHeadersFooters = buffer.getBoolean("bPreferenceTranslateWordHeadersFooters", bPreferenceTranslateWordHeadersFooters);
 		bPreferenceTranslateWordAllStyles = buffer.getBoolean("bPreferenceTranslateWordAllStyles", bPreferenceTranslateWordAllStyles);
 		bPreferenceTranslateWordHidden = buffer.getBoolean("bPreferenceTranslateWordHidden", bPreferenceTranslateWordHidden);
 		bPreferenceTranslateExcelExcludeColors = buffer.getBoolean("bPreferenceTranslateExcelExcludeColors", bPreferenceTranslateExcelExcludeColors);
 		bPreferenceTranslateExcelExcludeColumns = buffer.getBoolean("bPreferenceTranslateExcelExcludeCells", bPreferenceTranslateExcelExcludeColumns);
 
 		tsExcelExcludedColors = new TreeSet<String>();
 		siz = buffer.getInteger("tsExcelExcludedColors");
 		for(i=0;i<siz;i++)
 			tsExcelExcludedColors.add(buffer.getString('#'+(new Integer(i)).toString(), "F1F2F3F4"));
 
 		tsExcelExcludedColumns = new TreeSet<String>();
 		siz = buffer.getInteger("tsExcelExcludedColumns");
 		for(i=0;i<siz;i++)
 			tsExcelExcludedColumns.add(buffer.getString('#'+(new Integer(i)).toString(), "A1000"));
 
 		tsExcludeWordStyles = new TreeSet<String>();
 		siz = buffer.getInteger("tsExcludeWordStyles");
 		for(i=0;i<siz;i++)
 			tsExcludeWordStyles.add(buffer.getString('#'+(new Integer(i)).toString(), "zzzzz"));
 	}
 
 	@Override
 	public String toString ()
 	{
 		int i,siz;
 		Iterator it;
 		buffer.reset();
 		buffer.setBoolean("bPreferenceTranslateDocProperties", bPreferenceTranslateDocProperties);
 		buffer.setBoolean("bPreferenceTranslateComments", bPreferenceTranslateComments);
 		buffer.setBoolean("bPreferenceTranslatePowerpointNotes", bPreferenceTranslatePowerpointNotes);
 		buffer.setBoolean("bPreferenceTranslatePowerpointMasters", bPreferenceTranslatePowerpointMasters);
 		buffer.setBoolean("bPreferenceTranslateWordHeadersFooters", bPreferenceTranslateWordHeadersFooters);
 		buffer.setBoolean("bPreferenceTranslateWordAllStyles", bPreferenceTranslateWordAllStyles);
 		buffer.setBoolean("bPreferenceTranslateWordHidden", bPreferenceTranslateWordHidden);
 		buffer.setBoolean("bPreferenceTranslateExcelExcludeColors", bPreferenceTranslateExcelExcludeColors);
 		buffer.setBoolean("bPreferenceTranslateExcelExcludeColumns", bPreferenceTranslateExcelExcludeColumns);
 
 		if (tsExcelExcludedColors==null)
 			siz = 0;
 		else
 			siz = tsExcelExcludedColors.size();
 		buffer.setInteger("tsExcelExcludedColors", siz);
 		for(i=0,it=tsExcelExcludedColors.iterator();i<siz && it.hasNext();i++)
 		{
 			buffer.setString('#'+(new Integer(i)).toString(), (String)it.next());
 		}
 
 		if (tsExcelExcludedColumns==null)
 			siz = 0;
 		else
 			siz = tsExcelExcludedColumns.size();
 		buffer.setInteger("tsExcelExcludedColumns", siz);
 		for(i=0,it=tsExcelExcludedColumns.iterator();i<siz && it.hasNext();i++)
 		{
 			buffer.setString('#'+(new Integer(i)).toString(), (String)it.next());
 		}
 			
 		if (tsExcludeWordStyles==null)
 			siz = 0;
 		else
 			siz = tsExcludeWordStyles.size();
 		buffer.setInteger("tsExcludeWordStyles", siz);
 		for(i=0,it=tsExcludeWordStyles.iterator();i<siz && it.hasNext();i++)
 		{
 			buffer.setString('#'+(new Integer(i)).toString(), (String)it.next());
 		}
 			
 		return buffer.toString();
 	}
 }
