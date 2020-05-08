#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
 package ${package};
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Bootstrap {
     
     private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);
 
     /**
      * @param args
      */
     public static void main(String[] args) {
 	log.info("Hello world.");
     }
 
 }
