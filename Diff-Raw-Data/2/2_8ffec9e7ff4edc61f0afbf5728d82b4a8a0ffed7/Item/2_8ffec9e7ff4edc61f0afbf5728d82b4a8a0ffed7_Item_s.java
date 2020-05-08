 package org.zoneproject.extractor.utils;
 
 /*
  * #%L
  * ZONE-utils
  * %%
  * Copyright (C) 2012 ZONE-project
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.RSS;
 import com.sun.syndication.feed.rss.Enclosure;
 import com.sun.syndication.feed.synd.SyndEnclosureImpl;
 import com.sun.syndication.feed.synd.SyndEntry;
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.jsoup.Jsoup;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class Item implements Serializable{
     private static final org.apache.log4j.Logger  logger = org.apache.log4j.Logger.getLogger(Item.class);
 private static final long serialVersionUID = 1L;
     public String uri;
     public ArrayList<Prop> values;
 
     public Item(){
         this("");
     }
     
     public Item(String source, SyndEntry entry){
         this(source, entry.getLink(),entry.getTitle(),entry.getDescription().getValue(),entry.getPublishedDate(),entry.getEnclosures());
     }
     
     public Item(String uri){
         this.uri = uri;
         values = new ArrayList<Prop>();
     }
     public Item(String uri, ResultSet set){
         this(uri, set,"?s","?p","?o");
     }
     public Item(String uri, ResultSet set, String s, String p, String o){
         this.uri = uri;
         values = new ArrayList<Prop>();
 
         while (set.hasNext()) {
             QuerySolution result = set.nextSolution();
             String relation = result.get(p).toString();
             String value = result.get(o).toString();
             values.add(new Prop(relation,value,result.get(o).isLiteral(),false));
         }
     }
     public Item(String uri, String sparqlResultInfos){
         this.uri = uri;
         values = new ArrayList<Prop>();
        DocumentBuilder builder;
         try {
             builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             org.w3c.dom.Document doc;
             InputSource is = new InputSource(new StringReader(sparqlResultInfos));
             NodeList allElements = builder.parse(is).getElementsByTagName("result");
             for(int i=0; i <allElements.getLength();i++){
                 NodeList cur = allElements.item(i).getChildNodes();
                 String relation = cur.item(1).getTextContent();
                 String value = cur.item(3).getTextContent();
                 values.add(new Prop(relation,value));
             }
             
         } catch (ParserConfigurationException ex){
             Logger.getLogger(VirtuosoDatabase.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SAXException ex){
             Logger.getLogger(VirtuosoDatabase.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex){
             Logger.getLogger(VirtuosoDatabase.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public Item(String source, String link, String title, String description, Date datePublication){
         this(source, link,title,description,datePublication,new ArrayList());
     }
     public Item(String source, String link, String title, String description, Date datePublication, List enclosure){
         try{
             this.uri = link;
             values = new ArrayList<Prop>();
             values.add(new Prop(RSS.link,link,true,false));
             values.add(new Prop(RSS.title,title));
             values.add(new Prop(RSS.description,Jsoup.parse(description).text()));
             if(datePublication != null){
                 values.add(new Prop(RSS.getURI()+"pubDate",datePublication.toString()));
                 values.add(new Prop(RSS.getURI()+"pubDateTime",Long.toString(datePublication.getTime())));
             }
             for(Object o : enclosure){
                 SyndEnclosureImpl e = (SyndEnclosureImpl)o;
                 values.add(new Prop(RSS.image,e.getUrl(),true,false));
             }
             values.add(new Prop("http://purl.org/rss/1.0/source", source,false,false));
         }
         catch(NullPointerException e){
             logger.error(e);
         }
         
     }
     
     public void addElement(String key, String content){
         values.add(new Prop(key, content));
     }
     
     public String getElement(Property key){
         return getElements(key.getURI())[0];
     }
     
     public String[] getElements(String key){
         ArrayList<String> result = new ArrayList<String>();
         for(int i=0; i < values.size();i++){
             if(values.get(i).getType().getURI().equals(key))
                 result.add(values.get(i).getValue());
         }
         return result.toArray(new String[result.size()]);
     }
     
     @Override
     public String toString(){
         String content = "Item description:"+this.getUri();
         
         Iterator i = values.iterator();
 
         while(i.hasNext()){
             Prop me = (Prop)i.next();
             String isL = "Uri";
             if(me.isLiteral()) isL = "Lit";
             String isS = " ";
             if(me.isIsSearchable()) isS = "*";
             content += "\n\t"+isS+" "+isL+" "+me.getType() + " : ";
            if (me.getValue().length() > 100){
                 content+= me.getValue().substring(0,100)+"...";
             }else{
                 content+= me.getValue();
             }
         }
         return content;
     }
 
     public Item(ArrayList<Prop> values) {
         this.values = values;
     }
     
     public String concat(){
         String result = this.getElement(RSS.title)+".\n "+this.getElement(RSS.description);
         return result.replaceAll("<[^>]*>", "");
     }
 
     public String getUri() {
         return uri;
     }
     
     public ArrayList<Prop> getElements(){
         return values;
     }
 
     public void setUri(String uri) {
         this.uri = uri;
     }
     
     public void addProp(Prop prop){
         this.values.add(prop);
     }
     
     public void addProps(ArrayList<Prop> props){
         this.values.addAll(props);
     }
     
     public Model getModel(){
         Model model = ModelFactory.createDefaultModel();
         Resource itemNode = model.createResource(uri);
         for(Prop prop : values){
             if(prop.isLiteral()){
                 itemNode.addLiteral(prop.getType(), model.createLiteral(prop.getValue()));
             }
             else{
                 itemNode.addProperty(prop.getType(), model.createResource(prop.getValue()));
             }
         }
         return model;
     }
     
     public String getTitle(){
         return getElement(RSS.title);
     }
     
     public String getDescription(){
         return getElement(RSS.description);
     }
 }
