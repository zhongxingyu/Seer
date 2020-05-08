 /*
  *  StatCvs-XML - XML output for StatCvs.
  *
  *  Copyright by Steffen Pingel, Tammo van Lessen.
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  version 2 as published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package de.berlios.statcvs.xml.report;
 
 import java.util.Iterator;
 import java.util.SortedSet;
 import net.sf.statcvs.model.CvsContent;
 import net.sf.statcvs.model.CvsRevision;
 import net.sf.statcvs.model.SymbolicName;
 import net.sf.statcvs.util.IntegerMap;
 import de.berlios.statcvs.xml.I18n;
 import de.berlios.statcvs.xml.output.Report;
 import de.berlios.statcvs.xml.output.ReportElement;
 import de.berlios.statcvs.xml.output.ReportSettings;
 import de.berlios.statcvs.xml.output.TableElement;
 
 /**
  * @see de.berlios.statcvs.xml.report.CommitTable 
  * @author Steffen Pingel
  */
 public class SymbolicNameTable {
 
 	public static Report generate(CvsContent content, ReportSettings settings)
 	{
 		if (content.getSymbolicNames().isEmpty()) {
 			return null;
 		}
 		
		ReportElement root = new ReportElement(settings, I18n.tr("Commits%1"));
 
 		IntegerMap changesMap = new IntegerMap();
 		IntegerMap linesMap = new IntegerMap();
		IntegerMap linesAddedMap = new IntegerMap();
 
 		Iterator revIt = settings.getRevisionIterator(content);		
 		while (revIt.hasNext()) {
 			CvsRevision rev = (CvsRevision)revIt.next();
 			SortedSet symbolicNames = rev.getSymbolicNames();
 			if (symbolicNames != null) {
 				for (Iterator it = symbolicNames.iterator(); it.hasNext();) {
 					Object group = it.next();
 					changesMap.addInt(group, 1);
					linesMap.addInt(group, rev.getLinesDelta());
					linesAddedMap.addInt(group, rev.getNewLines());
 				}
 			}
 		}
 
 		TableElement table = new TableElement(settings, new String[] { 
			I18n.tr("Tag"), I18n.tr("Revisions"), I18n.tr("Lines of Code"), 
			I18n.tr("Added Lines of Code"), I18n.tr("Lines of Code per Change"), });
 			
 		int maxItems = settings.getLimit();
 		Iterator it = settings.getSymbolicNameIterator(content);
 		int count = 0;
 		while (it.hasNext() && count < maxItems) {
 			SymbolicName group = (SymbolicName)it.next();
 			table.addRow()
 				.addSymbolicName(group)
 				.addInteger("revisions", changesMap.get(group))
				.addInteger("loc", linesMap.get(group))
				.addInteger("locAdded", linesAddedMap.get(group))
				.addDouble("locPerRevision", (double)linesMap.get(group) / changesMap.get(group));
 			count++;
 		}
 		root.addContent(table);
 		return new Report(root, table);
 	}
 
 }
 
