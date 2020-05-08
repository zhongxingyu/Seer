 package com.redshape.servlet.resources.impl;
 
 import com.redshape.servlet.resources.IWebResourceWriter;
 import com.redshape.servlet.resources.IWebResourcesHandler;
 import com.redshape.servlet.resources.types.Link;
 import com.redshape.servlet.resources.types.Script;
 import com.redshape.servlet.resources.types.Style;
 import com.redshape.servlet.views.ViewHelper;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nikelin
  * Date: 17.06.11
  * Time: 14:03
  * To change this template use File | Settings | File Templates.
  */
 public class WebResourcesHandler implements IWebResourcesHandler {
     private IWebResourceWriter writer;
 
     private List<Script> scripts = new ArrayList<Script>();
     private List<Style> styles = new ArrayList<Style>();
     private List<Link> links = new ArrayList<Link>();
 
     public void setWriter(IWebResourceWriter writer) {
         this.writer = writer;
     }
 
     @Override
     public void addScript(String type, String href) {
         Script script = new Script( type, href );
         if ( this.scripts.contains(script) ) {
             return;
         }
 
         this.scripts.add( script );
     }
 
     @Override
     public void addLink(String rel, String type, String href) {
         Link link = new Link( rel, type, href );
         if ( this.links.contains(link) ) {
             return;
         }
 
         this.links.add( link );
     }
 
     @Override
     public void addStylesheet( String type, String href ) {
         this.addStylesheet( type, href, Style.DEFAULT_MEDIA );
     }
 
     @Override
     public void addStylesheet(String type, String href, String media ) {
         Style style = new Style( type, href, media );
         if ( this.styles.contains(style) ) {
             return;
         }
 
         this.styles.add( style );
     }
 
     @Override
     public String printScripts() {
         StringBuilder builder = new StringBuilder();
         for ( Script script : this.scripts ) {
             builder.append(
                 String.format("<script type=\"%s\" src=\"%s\"></script>\n",
						script.getType(), ViewHelper.url( script.getHref() ) ) );
         }
 
         return builder.toString();
     }
 
     @Override
     public String printStyles() {
         StringBuilder builder = new StringBuilder();
         for ( Style style : this.styles) {
             builder.append(
                 String.format("<link rel=\"stylesheet\" type=\"%s\" href=\"%s\"/>",
                        style.getType(), ViewHelper.url( style.getHref() ) ) );
         }
 
         return builder.toString();
     }
 }
