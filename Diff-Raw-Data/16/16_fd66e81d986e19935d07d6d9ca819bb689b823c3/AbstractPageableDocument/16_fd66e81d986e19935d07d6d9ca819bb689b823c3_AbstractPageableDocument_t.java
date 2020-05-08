 /*
     StatCvs - CVS statistics generation 
     Copyright (C) 2002  Lukasz Pekacki <lukasz@pekacki.de>
     http://statcvs.sf.net/
     
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU Lesser General Public
     License as published by the Free Software Foundation; either
     version 2.1 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
     
 	$RCSfile: AbstractPageableDocument.java,v $
	$Date: 2003-06-17 23:08:49 $ 
 */
 package net.sf.statcvs.output.xml;
 
 import java.util.List;
 
 import org.jdom.Element;
 
 /**
  * AbstractPageableDocument
  * 
  * @author Tammo van Lessen
  */
 public abstract class AbstractPageableDocument extends StatCvsDocument implements Pageable {
 
 	private String pContentName;
 
 	private List pContent;
 
 	private int itemsPerPage;
 
 	/**
 	 * @param element
 	 * @param filename
 	 */
 	public AbstractPageableDocument(Element element, String filename, int itemsPerPage) {
 		super(element, filename);
 		setItemsPerPage(itemsPerPage);
 	}
 
 	/**
 	 * @param filename
 	 */
 	public AbstractPageableDocument(String filename, int itemsPerPage) {
 		super(filename);
 		setItemsPerPage(itemsPerPage);
 	}
 
 	/**
 	 * @see net.sf.statcvs.output.xml.Pageable#setItemsPerPage(int)
 	 */
 	public void setItemsPerPage(int items) {
 		itemsPerPage = items;
 	}
 
 	/**
 	 * @see net.sf.statcvs.output.xml.Pageable#getItemsPerPage()
 	 */
 	public int getItemsPerPage() {
 		return itemsPerPage;
 	}
 
 	/**
 	 * @see net.sf.statcvs.output.xml.Pageable#getPage(int)
 	 */
 	public Element getPage(int page) {
 		Element element = new Element(this.getRootElement().getName());
		Element child = new Element(pContentName);
		element.addContent(getHeader());
		element.addContent(child);
		element.addContent(getFooter());
		List elCont = child.getContent();
		
 		
 		List pageList = null;// = new ArrayList();
 
 		if ((pContent != null) && (page < getPageCount())) {
			element.setAttribute("page", ""+(page+1));
 			element.setAttribute("totalPages", ""+getPageCount());
 			
 			if (pContent.size() < (page*itemsPerPage)+itemsPerPage) {
 				pageList = pContent.subList(page*itemsPerPage, pContent.size());
 			} else {
 				pageList = pContent.subList(page*itemsPerPage, (page*itemsPerPage)+itemsPerPage);	
 			}
 		}
 		// this: elCont.add(pageList); make that element gets null, why?
 		// workaround:
 		for (int i=0; i < pageList.size(); i++) {
 			Element el = new Element("x");
 			elCont.add(((Element)pageList.get(i)).clone());//pageList.get(i));
 		}
 		return element;
 	}
 	
 	/**
 	 * @see net.sf.statcvs.output.xml.Pageable#getPageCount()
 	 */
 	public int getPageCount() {
 		return (int) Math.ceil((double)pContent.size() / (double)itemsPerPage);
 	}
 
 	public abstract Element getHeader();
 	public abstract Element getFooter();
 
 	public void setPageableContent(Element content) {
 		this.pContent = content.getChildren();
 		this.pContentName = content.getName();
 	}
 	
 	public String getFilename(int page) {
 		return (page == 0)?getFilename():getFilename()+"_"+page;
 	}
 
 }
