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
 
 import net.sf.statcvs.model.CvsContent;
 import net.sf.statcvs.model.CvsRevision;
 import net.sf.statcvs.util.IntegerMap;
 
 import org.jdom.Element;
 
 import de.berlios.statcvs.xml.I18n;
 import de.berlios.statcvs.xml.model.AuthorGrouper;
 import de.berlios.statcvs.xml.model.Grouper;
 import de.berlios.statcvs.xml.output.Report;
 import de.berlios.statcvs.xml.output.ReportElement;
 import de.berlios.statcvs.xml.output.ReportSettings;
 import de.berlios.statcvs.xml.output.TableElement;
 import de.berlios.statcvs.xml.util.Formatter;
 
 /**
  * 
  * 
  * @author Steffen Pingel
  * @author Tammo van Lessen
  */
 public class CommitTable {
 
 	public static Report generate(CvsContent content, ReportSettings settings)
 	{
 		ReportElement root = new ReportElement(settings, I18n.tr("Commits%1"));
 		Grouper grouper = settings.getGrouper(new AuthorGrouper());
 
 		IntegerMap changesMap = new IntegerMap();
 		IntegerMap linesMap = new IntegerMap();
 		IntegerMap linesAddedMap = new IntegerMap();
 
 		Iterator revIt = settings.getRevisionIterator(content);		
 		while (revIt.hasNext()) {
 			CvsRevision rev = (CvsRevision)revIt.next();
 			Object group = grouper.getGroup(rev);
 			if (group != null) {
 				changesMap.addInt(group, 1);
 				linesMap.addInt(group, rev.getLinesDelta());
 				linesAddedMap.addInt(group, rev.getNewLines());
 			} 
 		}
 
 		TableElement table = new TableElement(settings, new String[] { 
 			grouper.getName(), I18n.tr("Revisions"), I18n.tr("Lines of Code"), 
 			I18n.tr("Added Lines of Code"), I18n.tr("Lines of Code per Change"), });
 			
 		int maxItems = settings.getLimit();
 		Iterator it;
 		String orderby = settings.getString("orderby");
 		if ("loc".equals(orderby)) {
 			it = linesMap.iteratorSortedByValueReverse();
 		}
 		else { 
 			it = changesMap.iteratorSortedByValueReverse();
 		}
 		int count = 0;
 		while (it.hasNext() && count < maxItems) {
 			Object group = it.next();
 			table.addRow()
 				.addGroup(grouper, group)
 				.addInteger("revisions", changesMap.get(group), changesMap.getPercent(group))
 				.addInteger("loc", linesMap.get(group), linesMap.getPercent(group))
 				.addInteger("locAdded", linesAddedMap.get(group), linesAddedMap.getPercent(group))
				.addDouble("locPerRevision", (double)linesMap.get(group) / changesMap.get(group));
 			count++;
 		}
 		root.addContent(table);
 		return new Report(root);
 	}
 
 }
 
