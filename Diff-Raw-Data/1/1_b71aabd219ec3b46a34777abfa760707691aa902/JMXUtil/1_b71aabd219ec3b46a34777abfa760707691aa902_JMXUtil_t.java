 package com.redshape.net.jmx;
 
 import com.redshape.utils.Commons;
 import org.apache.log4j.Logger;
 
 import javax.management.*;
 
 /**
  * @author nikelin
  * @date 21:02
  */
 public final class JMXUtil {
     private static final Logger log = Logger.getLogger(JMXUtil.class);
 
     private JMXFactory jmxFactory;
     
     public JMXUtil( JMXFactory jmxFactory ) {
         Commons.checkNotNull(jmxFactory);
 
         this.jmxFactory = jmxFactory;
     }
 
     public JMXFactory getJmxFactory() {
         return jmxFactory;
     }
 
     public String[] listMBeans() {
         return this.getJmxFactory().getMBeanServer().getDomains();
     }
 
     public String generateMBeanReport(ObjectName objectName, String className)
         throws JMXException {
         StringBuilder builder = new StringBuilder();
 
         builder.append("Retrieve the management information for the " + className).append("\n");
         builder.append("MBean using the getMBeanInfo() method of the MBeanServer").append("\n");
 
         MBeanServer mbs = this.getJmxFactory().getMBeanServer();
         MBeanInfo info;
         try {
             info = mbs.getMBeanInfo(objectName);
         } catch (Exception e) {
             throw new JMXException( e.getMessage(), e );
         }
 
         builder.append("CLASSNAME: \t" + info.getClassName()).append("\n");
         builder.append("DESCRIPTION: \t" + info.getDescription()).append("\n");
 
         builder.append("ATTRIBUTES").append("\n");
 
         MBeanAttributeInfo[] attrInfo = info.getAttributes();
 
         if (attrInfo.length == 0) {
             builder.append(" ** No attributes **");
         }
 
         for (int i = 0; i < attrInfo.length; i++) {
             builder.append(" ** NAME: \t" + attrInfo[i].getName()).append("\n");
             builder.append("    DESCR: \t" + attrInfo[i].getDescription()).append("\n");
             builder.append("    TYPE: \t" ).append( attrInfo[i].getType() ).append( "\tREAD: " )
                    .append( attrInfo[i].isReadable() )
                    .append("\tWRITE: ")
                    .append( attrInfo[i].isWritable())
                    .append("\n");
         }
 
         builder.append("CONSTRUCTORS");
         MBeanConstructorInfo[] constructorsInfo = info.getConstructors();
         for (int i = 0; i < constructorsInfo.length; i++) {
             builder.append(" ** NAME: \t" ).append( constructorsInfo[i].getName()).append("\n");
             builder.append("    DESCR: \t").append( constructorsInfo[i].getDescription()).append("\n");
             builder.append("    PARAM: \t").append( constructorsInfo[i].getSignature().length)
                                            .append(" parameter(s)").append("\n");
         }
 
         log.info("OPERATIONS");
         MBeanOperationInfo[] opInfo = info.getOperations();
         if (opInfo.length == 0) {
             builder.append(" ** No operations ** ");
         }
 
         for (int i = 0; i < opInfo.length; i++) {
             builder.append(" ** NAME: \t" ).append( opInfo[i].getName());
             builder.append("    DESCR: \t" ).append( opInfo[i].getDescription());
             builder.append("    PARAM: \t" ).append( opInfo[i].getSignature().length )
                    .append(" parameter(s)");
         }
         
         builder.append("NOTIFICATIONS");
         
         MBeanNotificationInfo[] notifInfo = info.getNotifications();
         if (notifInfo.length == 0) {
             log.info(" ** No notifications **");
         }
 
         for (int i = 0; i < notifInfo.length; i++) {
             builder.append(" ** NAME: \t").append( notifInfo[i].getName()).append("\n");
             builder.append("    DESCR: \t").append( notifInfo[i].getDescription() ).append("\n");
 
             String notifTypes[] = notifInfo[i].getNotifTypes();
             for (int j = 0; j < notifTypes.length; j++) {
                 builder.append("    TYPE: \t").append(notifTypes[j]).append("\n");
             }
         }
 
         return builder.toString();
     }
 
 
 }
