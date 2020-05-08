 /**
  * Copyright (C) 2004 - 2009 Shopzilla, Inc. 
  * All rights reserved. Unauthorized disclosure or distribution is prohibited.
  */
 
 import com.pettermahlen.bygg.configuration.ByggConfiguration;
 import com.pettermahlen.bygg.execution.TargetDAG;
 import com.pettermahlen.byggTestPlugin.ByggTestPlugin;
 
 /**
  * TODO: document this class!
  *
  * @author Petter Måhlén
  * @since 04/03/2011
  */
 public class Configuration {
     public static ByggConfiguration configuration() {
         return new ByggConfiguration() {
             public TargetDAG getTargetDAG() {
                 // TODO: fix Janino-varargs-hack with String array below when there is an internal compiler available
                 return TargetDAG.DEFAULT
                        .add("plugin").executor(new ByggTestPlugin()).predecessors(new String[] {"test"}).build();
             }
         };
     }
 }
