 package liebenberg.closure_utilities.build;
 
 import javax.annotation.Nonnull;
 
 public class OptionsBase {
 
     public OptionsBase() {}
 
     public OptionsBase(@Nonnull OptionsBase parentOptions) {
         this.verbose = parentOptions.getVerbose();
         this.shouldCompile = parentOptions.getShouldCompile();
         this.shouldDebug = parentOptions.getShouldDebug();
     }
 
    private Boolean verbose = false;

     private Boolean shouldDebug = true;

     private Boolean shouldCompile = false;
 
     @Nonnull
     public Boolean getVerbose() {
         return verbose;
     }
 
     public void setVerbose(@Nonnull Boolean verbose) {
         this.verbose = verbose;
     }
 
     @Nonnull
     public Boolean getShouldDebug() {
         return shouldDebug;
     }
 
     public void setShouldDebug(@Nonnull final Boolean shouldDebug) {
         this.shouldDebug = shouldDebug;
     }
 
     @Nonnull
     public Boolean getShouldCompile() {
         return shouldCompile;
     }
 
     public void setShouldCompile(@Nonnull final Boolean shouldCompile) {
         this.shouldCompile = shouldCompile;
     }
 }
