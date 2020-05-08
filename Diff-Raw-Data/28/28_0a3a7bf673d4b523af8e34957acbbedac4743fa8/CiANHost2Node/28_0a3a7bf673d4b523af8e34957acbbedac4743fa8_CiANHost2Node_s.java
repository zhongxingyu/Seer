 package ducks.driver;
 
 public class CiANHost2Node extends CiANBaseNode
 {
 
     public CiANHost2Node() {
         super();
     }
 
     @Override
     protected String[] getCiANArguments() {
        return new String[] { "Host 2", "standard", "-p", "CiAN/src/examples/simpleSimu/properties2.ini", "-c",
                "CiAN/src/examples/simpleSimu/simplehost2.hcfg" };
     }
 
 }
