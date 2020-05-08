 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package calliope.tests;
 
 import calliope.Connector;
 import calliope.db.Connection;
 import calliope.db.MongoConnection;
 import calliope.db.CouchConnection;
 import calliope.constants.HTMLNames;
 import calliope.constants.Database;
 import calliope.exception.AeseException;
 import calliope.tests.html.Element;
 import calliope.tests.html.HTML;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Test panel for boring internal tests
  * @author desmond
  */
 public class TestInternal extends Test
 {
     static String CENTRE_DIV_STYLE = "div#centre { width: 600px; "
            + "margin-left: auto; margin-right: auto; "
            +"background-color: white; padding: 10px }";
     public TestInternal()
     {
         description = "Runs some tests in a console";
     }
     /**
      * Display the test GUI
      * @param request the request to read from
      * @param urn the original URN
      * @return a formatted html String
      */
     @Override
     public void handle( HttpServletRequest request,
         HttpServletResponse response, String urn ) throws AeseException
     {
         //setDocID( request );
         doc = new HTML();
         super.handle( request, response, urn );
     }
     /**
      * Get the content of this test: a simple dropdown menu plus the 
      * html version's content
      * @return a select element object with appropriate attributes and children
      */
     @Override
     public Element getContent()
     {
         Element outer = new Element( HTMLNames.DIV );
         outer.addAttribute( HTMLNames.ID, "centre" );
         Element textArea = new Element( "textarea" );
         textArea.addAttribute( "rows", "20" );
         textArea.addAttribute( "cols", "50" );
         try
         {
             String value = "No database";
             Connection conn = Connector.getConnection();
             if ( conn instanceof MongoConnection )
             {
                 textArea.addText("Running MongoDB\n");
                 value = ((MongoConnection)conn).test();
             }
             else if ( conn instanceof CouchConnection )
             {
                 textArea.addText("Running CouchDB\n");
                 value = ((CouchConnection)conn).test();
             }
             textArea.addText( "DB Port: "+conn.getDbPort()+"\n" );
             textArea.addText( "WS Port: "+conn.getWsPort()+"\n" );
             textArea.addText( "Host: "+conn.getHost()+"\n" );
             String[] docs = conn.listCollection( Database.CONFIG ); 
             textArea.addText( docs.length+" documents in collection "
                 +Database.CONFIG+"\n" );
             docs = conn.listCollection( Database.CORTEX ); 
             textArea.addText( docs.length+" documents in collection "
                 +Database.CORTEX+"\n" );
             docs = conn.listCollection( Database.CORCODE ); 
             textArea.addText( docs.length+" documents in collection "
                 +Database.CORCODE+"\n" );
             docs = conn.listCollection( Database.CORFORM ); 
             textArea.addText( docs.length+" documents in collection "
                 +Database.CORFORM+"\n" );
             textArea.addText( value );
         }
         catch ( Exception e )
         {
             textArea.addText( e.getMessage() );
         }
         outer.addChild( textArea );
        return outer;
     }
 }
