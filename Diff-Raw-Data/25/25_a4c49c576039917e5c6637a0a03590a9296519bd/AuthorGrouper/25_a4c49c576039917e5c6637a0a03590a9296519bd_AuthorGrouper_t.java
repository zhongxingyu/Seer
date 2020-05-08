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
 
 package de.berlios.statcvs.xml.model;
 
 import java.util.Iterator;
 
 import net.sf.statcvs.model.Author;
 import net.sf.statcvs.model.CvsContent;
 import net.sf.statcvs.model.CvsRevision;
 import de.berlios.statcvs.xml.I18n;
 import de.berlios.statcvs.xml.output.ReportSettings;
 import de.berlios.statcvs.xml.output.TableElement.RowElement;
 
 /**
  * @author Steffen Pingel
  */
 public class AuthorGrouper extends Grouper {
 
	public Author dummyAuthor;

 	public AuthorGrouper()
 	{
 		super("author", I18n.tr("Author"));
 	}
 
 	public void addElement(Object group,  RowElement row)
 	{
 		row.addAuthor((Author)group);
 	}
 
 	public Object getGroup(CvsRevision rev) 
 	{
		if (rev.getAuthor() != null) {
			return rev.getAuthor();
		}
		else {
			// assert rev.isBeginOfLog();
			if (dummyAuthor == null) {
				dummyAuthor = new Author("(Unknown)");
			}
			return dummyAuthor;
		}
 	}
 
 	/**
 	 *  @see de.berlios.statcvs.xml.model.Grouper#getGroups()
 	 */
 	public Iterator getGroups(CvsContent content, ReportSettings settings) 
 	{
 		return content.getAuthors().iterator();
 	}
 
 	/**
 	 *  @see de.berlios.statcvs.xml.model.Grouper#getValue(java.lang.Object)
 	 */
 	public String getName(Object group) 
 	{
 		return ((Author)group).getName();
 	}
 
 }
