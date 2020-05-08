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
 package de.berlios.statcvs.xml.output;
 
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import net.sf.statcvs.model.Author;
 import net.sf.statcvs.model.Commit;
 import net.sf.statcvs.model.CvsFile;
 import net.sf.statcvs.model.CvsRevision;
 import net.sf.statcvs.model.Directory;
 import net.sf.statcvs.output.WebRepositoryIntegration;
 
import org.jdom.CDATA;
 import org.jdom.Element;
 
 import de.berlios.statcvs.xml.I18n;
 import de.berlios.statcvs.xml.model.Grouper;
 import de.berlios.statcvs.xml.model.Module;
 import de.berlios.statcvs.xml.util.Formatter;
 
 /**
  * @author Steffen Pingel
  */
 public class TableElement extends Element 
 						   implements Separable {
 
 	ReportSettings settings;
 	String[] headers;
 	
 	/**
 	 * @param name the name of the report
 	 */
 	public TableElement(ReportSettings settings, String[] headers) 
 	{
 		super("table");
 		
 		this.settings = settings;
 		this.headers = headers;
 		
 		if (headers != null) {
 			Element row = new Element("tr");
 			for (int i = 0; i < headers.length; i++) {
 				if (showColumn(i + 1)) {
 					row.addContent(new Element("th").addContent(headers[i]));
 				}
 			}
 			addContent(row);
 		}
 	}
 	
 	public boolean showColumn(int i)
 	{
 		return settings.getString("showColumns", "123456789")
 			.indexOf(Integer.toString(i)) != -1;
 	}
 	
 	public RowElement addRow()
 	{
 		RowElement row = new RowElement();
 		addContent(row);
 		return row;
 	}
 	
 	public void addRow(RowElement row)
 	{
 		addContent(row);
 	}
 
 	/**
 	 * @see de.berlios.statcvs.xml.output.Separable#getReportPage(int)
 	 */
 	public Element getPage(int page) {
 		if (!settings.isPaging()) {
 			return (Element)this.clone();	
 		} else {
 
 			int lower = page * settings.getItemsPerPage();
 			int upper = lower + settings.getItemsPerPage();
 			if (upper > getContent().size() - 1) {
 				upper = getContent().size() - 1;
 			}
 
 			List items = getContent().subList(lower + 1, upper + 1);
 			TableElement tablePart = new TableElement(settings, headers);
 			for (int i = 0; i < items.size(); i++) {
 				tablePart.addContent((Element)((Element)items.get(i)).clone());
 			}
 			return tablePart;
 		}
 	}
 
 	/**
 	 * @see de.berlios.statcvs.xml.output.Separable#getPageCount()
 	 */
 	public int getPageCount() 
 	{
 		int items = getContent().size() - 1; // -1 == header
 		return (isEmpty()) 
 			? 0 
 			: (settings.isPaging()) 
 			? (int)Math.ceil((double)items / settings.getItemsPerPage())
 			: 1;
 	}
 
 	public boolean isEmpty()
 	{
 		return getChildren().size() == 1;
 	}
 
 	public class RowElement extends Element
 	{
 		int columnCount = 0;
 		
 		public RowElement()
 		{
 			super("row");
 		}
 		
 		public Element addContent(Element element)
 		{
 			return (showColumn(++columnCount))
 				? super.addContent(element)
 				: null;
 		}
 		
 		public RowElement addInteger(String key, int value, double percentage)
 		{
 			Element number = new Element("number");
 			number.setAttribute("key", key);
 			number.setAttribute("value", "" + value);
 			if (settings.getBoolean("showPercent", true)) {
 				number.setAttribute("percentage", Formatter.formatNumber(percentage, 1));
 			}		
 			addContent(number);
 			return this;
 		}
 
 		public RowElement addInteger(String key, int value)
 		{
 			Element number = new Element("number");
 			number.setAttribute("key", key);
 			number.setAttribute("value", "" + value);
 			
 			addContent(number);
 			return this;
 		}
 		
 		public RowElement addDouble(String key, double value, double percentage)
 		{
 			Element number = new Element("number");
 			number.setAttribute("key", key);
 			number.setAttribute("value", Formatter.formatNumber(value, 2));
 			if (settings.getBoolean("showPercent", true)) {			
 				number.setAttribute("percentage", Formatter.formatNumber(percentage, 1));
 			}
 			
 			addContent(number);
 			return this;
 		}
 
 		public RowElement addDouble(String key, double value)
 		{
 			Element number = new Element("number");
 			number.setAttribute("key", key);
 			number.setAttribute("value", Formatter.formatNumber(value, 2));
 			
 			addContent(number);
 			return this;
 		}
 		
 		public RowElement addString(String key, String value) 
 		{
 			addContent(new Element("string").setAttribute("key", key).setAttribute("value", value));
 			return this;
 		}
 		
 		public RowElement addLink(String key, String value, String url)
 		{
 			addContent(new Element("link")
 							.setAttribute("key", key)
 							.setAttribute("value", value)
 							.setAttribute("url", url));
 			return this;
 		}
 		
 		public RowElement addImage(String key, String src)
 		{
 			addContent(new Element("image")
 							.setAttribute("src", src));
 			return this;	
 		}
 		
 		public RowElement addDirectory(Directory dir) 
 		{
 			addContent(new Element("directory").setAttribute("name", dir.getPath()));
 			return this;
 		}
 		
 		public RowElement addModule(Module module) 
 		{
 			addContent(new Element("module").setAttribute("name", module.getName()));
 			return this;
 		}
 		
 		public RowElement addAuthor(Author author) 
 		{
 			Element authEl = new Element("author");
 			authEl.setAttribute("name", author.getName());
 			authEl.setAttribute("fullname", settings.getFullname(author));
 			addContent(authEl);
 											
 			return this;
 		}
 		
 		public RowElement addFile(CvsFile file)
 		{
 			WebRepositoryIntegration webRepository = settings.getWebRepository(); 
 								
 			return addLink("file", file.getFilenameWithPath(),
 					(webRepository == null) ? "" : webRepository.getFileViewUrl(file));
 		}
 		
 		public RowElement addGroup(Grouper grouper, Object group)
 		{
 			grouper.addElement(group, this);
 			return this;
 		}
 		
 		public RowElement addDirectoryTree(Directory directory, int depth) 
 		{
 			Element element = new Element("directoryTree");
 			element.setAttribute("depth", "" + depth);
 			element.setAttribute("name", (directory.isRoot()) ? I18n.tr("[root]") : directory.getName());
 			element.setAttribute("path", directory.getPath());
 			if (directory.isEmpty()) {
 				element.setAttribute("removed", "true");
 			} 
 			addContent(element);
 			return this;
 		}
 
 		public RowElement addCommit(Commit commit)
 		{
 			Element comEl = new Element("commit");
 			comEl.setAttribute("changedfiles", ""+commit.getAffectedFiles().size());						
 
 			Iterator it = commit.getRevisions().iterator();
 			int locSum = 0;
 			while (it.hasNext()) {
 				CvsRevision each = (CvsRevision) it.next();
 				locSum += each.getNewLines();
 			}
 			comEl.setAttribute("changedlines", ""+locSum);
 
			//comEl.addContent(new Element("comment").setText(commit.getComment()));
			comEl.addContent(new Element("comment").addContent(new CDATA(commit.getComment())));
 
 			Element files = new Element("files");
 			comEl.addContent(files);
 			
 			Iterator revIt = commit.getRevisions().iterator();
 			while (revIt.hasNext()) {
 				CvsRevision rev = (CvsRevision)revIt.next();
 
 				Element file = new Element("file");
 				files.addContent(file);
 				file.setAttribute("name", rev.getFile().getFilename());
 				file.setAttribute("directory", rev.getFile().getDirectory().getPath());
 				file.setAttribute("revision", rev.getRevisionNumber());
 
 				// links to webrepo				
 				WebRepositoryIntegration webRepository = settings.getWebRepository();
 				if (webRepository != null) {
 					CvsRevision previous = rev.getPreviousRevision();
 					String url; 
 					if (previous == null) {
 						url = webRepository.getFileViewUrl(rev);
 					} else {
 						url = webRepository.getDiffUrl(previous, rev);
 					}
 					file.setAttribute("url", url);
 				}
 
 				if (rev.isInitialRevision()) {
 					file.setAttribute("action", "added");
 					if (rev.getFile().getCurrentLinesOfCode() != 0) {
 						file.setAttribute("lines", ""+rev.getLines());
 					}
 				} else if (rev.isDead()) {
 					file.setAttribute("action", "deleted");
 				} else {
 					file.setAttribute("action", "changed");
 					
 					int delta = rev.getLinesDelta();
 					int linesAdded = rev.getReplacedLines() + ((delta > 0) ? delta : 0);
 					int linesRemoved = rev.getReplacedLines() - ((delta < 0) ? delta : 0);
 
 					file.setAttribute("added", "" + linesAdded);
 					file.setAttribute("removed", "" + linesRemoved);
 				}
 				
 			}
 			
 			addContent(comEl);
 			return this;
 		}
 
 		/**
 		 * @param string
 		 * @param date
 		 */
 		public RowElement addDate(String key, Date date) 
 		{
 			return addString(key, Formatter.formatDate(date));
 		}
 
         /**
          * @param key
          * @param percent 0 <= percent <= 1
          */
         public RowElement addPercent(String key, double percent) {
 			Element number = new Element("number");
 			number.setAttribute("key", key);
 			number.setAttribute("value", Formatter.formatNumber(percent, 1));
 			
 			addContent(number);
 			return this;
         }
 		
 	}
 	
 }
