 package com.enonic.cms.plugin.confluence.xml;
 
 import com.enonic.cms.plugin.confluence.model.PageModel;
 import com.enonic.cms.plugin.confluence.model.SpaceModel;
 import org.jdom.CDATA;
 import org.jdom.Document;
 import org.jdom.Element;
 
 public final class XmlModelBuilder
 {
     private static final String ELEMENT_KEY_PAGE = "menuitem";
 
     private static final String ELEMENT_KEY_ROOT = "menuitems";
 
     private static final String ATTRIBUTE_KEY_PAGEKEY = "key";
 
     private static final String ATTRIBUTE_KEY_TYPE = "type";
 
     private static final String ATTRIBUTE_KEY_PATH = "path";
 
     private static final String ELEMENT_KEY_DISPLAYNAME = "display-name";
 
     private static final String ELEMENT_KEY_NAME = "name";
 
     private static final String ATTRIBUTE_KEY_CHILDCOUNT = "child-count";
 
     private static final String ATTRIBUTE_KEY_ACTIVE = "active";
 
     private static final String ATTRIBUTE_KEY_CUSTOMKEY = "custom-key";
 
     private final String refererPageKey;
 
     private final PageModel activePage;
 
     public XmlModelBuilder( String refererPageKey, PageModel activePage )
     {
         this.refererPageKey = refererPageKey;
         this.activePage = activePage;
     }
 
     public Document build( SpaceModel spaceModel )
     {
         return new Document( buildElement( spaceModel ) );
     }
 
     private Element buildElement( SpaceModel spaceModel )
     {
         Element root = new Element( ELEMENT_KEY_ROOT );
 
         if ( spaceModel.getRootPage().getChildren() != null )
         {
             root.setAttribute( ATTRIBUTE_KEY_CHILDCOUNT, "1" );
         }
 
         root.addContent( buildElement( spaceModel.getRootPage(), true ) );
 
         return root;
     }
 
     private Element buildElement( PageModel model, boolean nested )
     {
         Element pageElement = createPageElement( model );
 
         pageElement.addContent( createNameElement( model ) );
         pageElement.addContent( createTitleElement( model ) );
 
         boolean hasChildren = !model.getChildren().isEmpty();
         if ( nested && hasChildren )
         {
             buildChildTree( model, nested, pageElement );
         }
 
         return pageElement;
     }
 
     private Element createTitleElement( PageModel model )
     {
         Element title = new Element( ELEMENT_KEY_DISPLAYNAME );
         title.setText( model.getTitle() );
         return title;
     }
 
     private Element createNameElement( PageModel model )
     {
         Element name = new Element( ELEMENT_KEY_NAME );
         name.setText( model.getTitle() );
         return name;
     }
 
     private Element createPageElement( PageModel model )
     {
         Element root = new Element( ELEMENT_KEY_PAGE );
 
         root.setAttribute( ATTRIBUTE_KEY_PAGEKEY, refererPageKey );
 
         root.setAttribute( ATTRIBUTE_KEY_TYPE, "custom" );
 
         root.setAttribute( ATTRIBUTE_KEY_CUSTOMKEY, model.getTitle() );
 
         if ( model.isActive( activePage.getId() ) )
         {
             root.setAttribute( ATTRIBUTE_KEY_ACTIVE, "true" );
             root.setAttribute( ATTRIBUTE_KEY_PATH, "true" );
         }
         else if ( model.isInPath( activePage ) )
         {
             root.setAttribute( ATTRIBUTE_KEY_PATH, "true" );
         }
 
         return root;
     }
 
     private void buildChildTree( PageModel model, boolean nested, Element root )
     {
         Element childTree = new Element( ELEMENT_KEY_ROOT );
         childTree.setAttribute( ATTRIBUTE_KEY_CHILDCOUNT, String.valueOf( model.getChildren() != null ? model.getChildren().size() : 0 ) );
 
         for ( PageModel child : model.getChildren() )
         {
             childTree.addContent( buildElement( child, nested ) );
         }
 
         root.addContent( childTree );
     }
 
     public Document build( PageModel model, String content )
         throws Exception
     {
         return new Document( buildElement( model, content ) );
     }
 
 
     private Element buildElement( PageModel model, String content )
         throws Exception
     {
         Element root = buildContentElement( model );
         if ( content != null )
         {
             Element tmp = new Element( "html" );
            tmp.addContent( LinkRewriter.rewriteLinks( content ) );
             root.addContent( tmp );
         }
         return root;
     }
 
     private Element buildContentElement( PageModel model )
     {
         Element contentElement = new Element( "page" );
 
         contentElement.setAttribute( "id", String.valueOf( model.getId() ) );
         contentElement.setAttribute( "title", model.getTitle() );
         contentElement.setAttribute( "name", model.getName() );
 
         return contentElement;
     }
 
 }
