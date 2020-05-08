 package com.enonic.cms.plugin.confluence.xml;
 
 import com.enonic.cms.plugin.confluence.model.PageModel;
 import com.enonic.cms.plugin.confluence.model.SpaceModel;
 import org.jdom.CDATA;
 import org.jdom.Document;
 import org.jdom.Element;
 
 /**
  * Created by rmy - Date: Sep 30, 2009
  */
 public class XmlModelBuilderOldModel
 {
     public static Document build( SpaceModel model )
     {
         return new Document( buildElement( model ) );
     }
 
     public static Document build( PageModel model, String content )
         throws Exception
     {
         return new Document( buildElement( model, content ) );
     }
 
     private static Element buildElement( SpaceModel model )
     {
         Element root = new Element( "space" );
         root.setAttribute( "key", model.getKey() );
         root.setAttribute( "name", model.getName() );
         root.setAttribute( "homePage", String.valueOf( model.getHomePage() ) );
         root.addContent( buildElement( model.getRootPage(), true ) );
         return root;
     }
 
     private static Element buildElement( PageModel model, boolean nested )
     {
         Element root = new Element( "page" );
         root.setAttribute( "id", String.valueOf( model.getId() ) );
         root.setAttribute( "parentId", String.valueOf( model.getParent() != null ? model.getParent().getId() : "" ) );
         root.setAttribute( "title", model.getTitle() );
         root.setAttribute( "name", model.getName() );
 
         if ( nested )
         {
             for ( PageModel child : model.getChildren() )
             {
                 root.addContent( buildElement( child, nested ) );
             }
         }
 
         return root;
     }
 
     private static Element buildElement( PageModel model, String content )
         throws Exception
     {
         Element root = buildElement( model, false );
         if ( content != null )
         {
             Element tmp = new Element( "html" );
            tmp.addContent( new CDATA( LinkRewriter.rewriteLinks( content ) ) );
             root.addContent( tmp );
         }
 
         return root;
     }
 }
