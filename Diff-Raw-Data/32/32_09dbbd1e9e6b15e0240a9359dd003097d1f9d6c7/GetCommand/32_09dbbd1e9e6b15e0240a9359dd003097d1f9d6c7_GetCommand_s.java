 package org.cyclopsgroup.jmxterm.cmd;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.management.JMException;
 import javax.management.MBeanAttributeInfo;
 import javax.management.MBeanServerConnection;
 import javax.management.ObjectName;
 
 import org.cyclopsgroup.jcli.annotation.Argument;
 import org.cyclopsgroup.jcli.annotation.Cli;
 import org.cyclopsgroup.jcli.annotation.Option;
 import org.cyclopsgroup.jmxterm.Command;
 import org.cyclopsgroup.jmxterm.Session;
 import org.cyclopsgroup.jmxterm.SyntaxUtils;
 
 @Cli( name = "get", description = "Get value of attribute(s)" )
 public class GetCommand
     extends Command
 {
     public void displayAttributes( Session session )
         throws IOException, JMException
     {
         String beanName = BeanCommand.getBeanName( bean, domain, session );
         if ( beanName == null )
         {
             throw new IllegalArgumentException( "Bean isn't set yet, please use -b option or bean command" );
         }
         ObjectName name = new ObjectName( beanName );
         session.msg( "mbean = " + beanName + ":" );
         MBeanServerConnection con = session.getConnection().getConnector().getMBeanServerConnection();
         MBeanAttributeInfo[] ais = con.getMBeanInfo( name ).getAttributes();
        HashMap<String, MBeanAttributeInfo> attributeNames = new HashMap<String, MBeanAttributeInfo>();
         if ( attributes.contains( "*" ) )
         {
             for ( MBeanAttributeInfo ai : ais )
             {
                 attributeNames.put( ai.getName(), ai );
             }
         }
         else
         {
             for ( String arg : attributes )
             {
                 for ( MBeanAttributeInfo ai : ais )
                 {
                     if ( ai.getName().equals( arg ) )
                     {
                        attributeNames.put( ai.getName(), ai );
                         break;
                     }
                 }
             }
         }
        for ( String attributeName : this.attributes )
         {
            MBeanAttributeInfo i = attributeNames.get( attributeName );
             if ( i.isReadable() )
             {
                 Object result = con.getAttribute( name, attributeName );
                 if ( session.isAbbreviated() )
                 {
                     SyntaxUtils.printValue( session.output, result, 0, false );
                     session.output.println();
                 }
                 else
                 {
                     SyntaxUtils.printExpression( session.output, attributeName, result, i.getDescription(), 2,
                                                  showDescription );
                 }
             }
             else
             {
                 session.msg( "  " + i.getName() + " is not readable", i.getName() + "=?" );
             }
         }
     }
 
     private List<String> attributes = new ArrayList<String>();
 
     private String bean;
 
     private String domain;
 
     private boolean showDescription;
 
     @Option( name = "i", longName = "info", description = "Show detail information of each attribute" )
     public final void setShowDescription( boolean showDescription )
     {
         this.showDescription = showDescription;
     }
 
    @Option( name = "d", longName = "domain", description = "Domain of bean" )
     public final void setDomain( String domain )
     {
         this.domain = domain;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void execute( Session session )
         throws JMException, IOException
     {
         if ( attributes.isEmpty() )
         {
             throw new IllegalArgumentException( "Please specify at least one attribute" );
         }
         displayAttributes( session );
     }
 
     @Argument( requires = 1 )
     public final void setAttributes( List<String> attributes )
     {
         this.attributes = attributes;
     }
 
    @Option( name = "b", longName = "bean" )
     public final void setBean( String bean )
     {
         this.bean = bean;
     }
 }
