 //=============================================================================
 //===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
 //===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
 //===	and United Nations Environment Programme (UNEP)
 //===
 //===	This program is free software; you can redistribute it and/or modify
 //===	it under the terms of the GNU General Public License as published by
 //===	the Free Software Foundation; either version 2 of the License, or (at
 //===	your option) any later version.
 //===
 //===	This program is distributed in the hope that it will be useful, but
 //===	WITHOUT ANY WARRANTY; without even the implied warranty of
 //===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 //===	General Public License for more details.
 //===
 //===	You should have received a copy of the GNU General Public License
 //===	along with this program; if not, write to the Free Software
 //===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 //===
 //===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 //===	Rome - Italy. email: geonetwork@osgeo.org
 //==============================================================================
 
 package org.fao.geonet.guiservices.templates;
 
 import java.util.List;
 import jeeves.constants.Jeeves;
 import jeeves.interfaces.Service;
 import jeeves.server.ServiceConfig;
 import jeeves.server.context.ServiceContext;
 import jeeves.utils.Xml;
 import org.fao.geonet.GeonetContext;
 import org.fao.geonet.constants.Edit;
 import org.fao.geonet.constants.Geonet;
 import org.fao.geonet.kernel.search.MetaSearcher;
 import org.fao.geonet.kernel.search.SearchManager;
 import org.jdom.Element;
 
 //=============================================================================
 
 /** A simple service that returns all metadata templates that can be added
   */
 
 public class Get implements Service
 {
 	private String styleSheet;
 
 	private String arParams[] =
 	{
 		"extended", "off",
 		"remote",   "off",
 		"attrset",  "geo",
		"template", "on",
 		"any",      "",
 	};
 
 	//--------------------------------------------------------------------------
 	//---
 	//--- Init
 	//---
 	//--------------------------------------------------------------------------
 
 	public void init(String appPath, ServiceConfig params) throws Exception
 	{
 		styleSheet = appPath + Geonet.Path.STYLESHEETS +"/portal-present.xsl";
 	}
 
 	//--------------------------------------------------------------------------
 	//---
 	//--- API
 	//---
 	//--------------------------------------------------------------------------
 
 	public Element exec(Element params, ServiceContext context) throws Exception
 	{
 		Element result = search(context).setName(Jeeves.Elem.RESPONSE);
 		Element root   = new Element("root");
 
 		root.addContent(result);
 
		List list = Xml.transform(root, styleSheet).getChildren("metadata");
 
 		Element response = new Element("dummy");
 
 		for(int i=0; i<list.size(); i++)
 		{
 			Element elem = (Element) list.get(i);
 			Element info = elem.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);
 
 			String id       = info.getChildText(Edit.Info.Elem.ID);
 			String template = info.getChildText(Edit.Info.Elem.IS_TEMPLATE);
 
 			if (template.equals("y"))
 				response.addContent(buildRecord(id, elem.getChildText("title")));
 		}
 
 		return response;
 	}
 
 	//--------------------------------------------------------------------------
 	//---
 	//--- Private methods
 	//---
 	//--------------------------------------------------------------------------
 
 	private Element search(ServiceContext context) throws Exception
 	{
		//FIXME: use a faster search through templates

 		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
 
 		context.info("Creating searcher");
 
 		Element       params = buildParams();
 		ServiceConfig config = new ServiceConfig();
 
 		SearchManager searchMan = gc.getSearchmanager();
 		MetaSearcher  searcher  = searchMan.newSearcher(SearchManager.LUCENE, Geonet.File.SEARCH_LUCENE);
 
 		searcher.search(context, params, config);
 
 		params.addContent(new Element("from").setText("1"));
 		params.addContent(new Element("to").setText(searcher.getSize() +""));
 
 		Element result = searcher.present(context, params, config);
 
 		searcher.close();
 
 		return result;
 	}
 
 	//--------------------------------------------------------------------------
 
 	private Element buildParams()
 	{
 		Element params = new Element(Jeeves.Elem.REQUEST);
 
 		for(int i=0; i<arParams.length/2; i++)
 			params.addContent(new Element(arParams[i*2]).setText(arParams[i*2 +1]));
 
 		return params;
 	}
 
 	//--------------------------------------------------------------------------
 
 	private Element buildRecord(String id, String name)
 	{
 		Element el = new Element("record");
 
 		el.addContent(new Element("id")  .setText(id));
 		el.addContent(new Element("name").setText(name));
 
 		return el;
 	}
 }
 
 //=============================================================================
 
