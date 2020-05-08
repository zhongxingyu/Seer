 package org.cyclopsgroup.jmxterm.cmd;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.management.Attribute;
 import javax.management.JMException;
 import javax.management.MBeanAttributeInfo;
 import javax.management.MBeanInfo;
 import javax.management.MBeanServerConnection;
 import javax.management.ObjectName;
 
 import org.apache.commons.lang.Validate;
 import org.cyclopsgroup.jcli.annotation.Argument;
 import org.cyclopsgroup.jcli.annotation.Cli;
 import org.cyclopsgroup.jcli.annotation.Option;
 import org.cyclopsgroup.jmxterm.Command;
 import org.cyclopsgroup.jmxterm.Session;
 import org.cyclopsgroup.jmxterm.SyntaxUtils;
 
 @Cli( name = "set", description = "Set value of an attribute" )
 public class SetCommand
     extends Command
 {
     private List<String> arguments;
 
     private String bean;
 
     private String domain;
 
     /**
      * @inheritDoc
      */
     @Override
     public void execute( Session session )
         throws JMException, IOException
     {
         Validate.notNull( arguments, "Argument can't be NULL" );
         Validate.isTrue( arguments.size() >= 2, "At least two arguments are required" );
         String attributeName = arguments.get( 0 );
 
         String beanName = BeanCommand.getBeanName( bean, domain, session );
         ObjectName name = new ObjectName( beanName );
 
         MBeanServerConnection con = session.getConnection().getConnector().getMBeanServerConnection();
         MBeanInfo beanInfo = con.getMBeanInfo( new ObjectName( beanName ) );
         MBeanAttributeInfo attributeInfo = null;
         for ( MBeanAttributeInfo i : beanInfo.getAttributes() )
         {
             if ( i.getName().equals( attributeName ) )
             {
                 attributeInfo = i;
                 break;
             }
         }
         if ( attributeInfo == null )
         {
             throw new IllegalArgumentException( "Attribute " + attributeName + " is not sepcified" );
         }
         if ( !attributeInfo.isWritable() )
         {
             throw new IllegalArgumentException( "Attribute " + attributeName + " is not writable" );
         }
         Object value = SyntaxUtils.parse( arguments.get( 1 ), attributeInfo.getType() );
         con.setAttribute( name, new Attribute( attributeName, value ) );
     }
 
     @Argument( requires = 2, description = "name, value, value2..." )
     public final void setArguments( List<String> arguments )
     {
         Validate.notNull( arguments, "Arguments can't be NULL" );
         this.arguments = arguments;
     }
 
    @Option( name = "b", longName = "bean", description = "Bean name" )
     public final void setBean( String bean )
     {
         this.bean = bean;
     }
 
     @Option( name = "d", longName = "domain", description = "Domain under which the bean is" )
     public final void setDomain( String domain )
     {
         this.domain = domain;
     }
 }
