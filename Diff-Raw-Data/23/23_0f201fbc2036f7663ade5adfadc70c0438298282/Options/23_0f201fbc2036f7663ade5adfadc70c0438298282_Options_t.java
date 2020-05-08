 package com.wickedspiral.jacss;
 
 import org.kohsuke.args4j.Option;
 
 /**
 * @author wasche
 * @since 10/1/13
 */
 public class Options
 {
     @Option( name = "-v", aliases = { "--verbose" }, required = false, metaVar = "VERBOSE",
         usage = "Print debugging information" )
     protected boolean verbose = false;
 
     @Option( name = "-d", aliases = { "--debug" }, required = false, metaVar = "DEBUG",
         usage = "Print additional debugging information" )
     protected boolean debug = false;
 
     @Option( name = "-f", aliases = { "--force" }, required = false, metaVar = "FORCE",
         usage = "Force re-compression" )
     protected boolean force = false;
 
     @Option( name = "--keep-trailing-semicolons", required = false,
         usage = "Do not strip semicolons on last style of a rule" )
     protected boolean keepTailingSemicolons = false;
    
    @Option( name = "--add-trailing-semicolons", required = false,
        usage = "Add trailing semicolons on last style of a rule" )
    protected boolean addTrailingSemicolons = false;
 
     @Option( name = "--no-collapse-zeroes", required = false, usage = "Do not drop leading zeroes inside rgba()" )
     protected boolean noCollapseZeroes = false;
 
     @Option( name = "--no-collapse-none", required = false, usage = "Do not collapse none to 0" )
     protected boolean noCollapseNone = false;
     
     @Option( name = "--compat-yui242", required = false, usage = "Match compatibility with YUI 2.4.2" )
     protected boolean yui242 = false;
 
     public void imply()
     {
         if ( yui242 )
         {
             keepTailingSemicolons = true;
            addTrailingSemicolons = true;
             noCollapseZeroes = true;
             noCollapseNone = true;
         }
     }
     
     public boolean isDebug()
     {
         return debug;
     }
 
     public boolean shouldCollapseNone()
     {
         return !noCollapseNone;
     }
 
     public boolean shouldCollapseZeroes()
     {
         return !noCollapseZeroes;
     }
 
     public boolean keepTailingSemicolons()
     {
        return keepTailingSemicolons || addTrailingSemicolons;
    }
    
    public boolean addTrailingSemicolons()
    {
        return addTrailingSemicolons;
     }
 }
