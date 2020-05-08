 package de.loggi.model;
 
 /**
  * Prototyping class to configure CLI parameters without boilerplate code.
  *
  * @author CptSpaetzle
  */
 public enum Parameter {
     TEMPLATE("t", "template", "path to template.json file", true, false),
    SOURCE("s", "source", "Source log file(s). Glob wildcard syntax is also supported, but make sure you wrap the whole glob in quotes and put semicolon at the end, like \"a/b/*.log;\", otherwise java will expand wildcards arguments before passing them to application", true, false),
     HELP("h", "help", "Display help and usage info", false, false),
     MAX_RECORD_LENGTH("maxRecordLength","", "Maximum length for single record before separator alert goes off (default 50k chars)", true, false),
     PROCESSOR_THREADS("threads", "", "Number of threads for column processors, default is (numberOfCPUs-1)", true, false),
     H2_SERVER_PORT("port", "", "H2 Console port", true, false),
     H2_USERNAME("user", "", "username for H2 Console (default: 'user')", true, false),
     H2_PASSWORD("password", "", "password for H2 Console (default: 'password')", true, false);
 
     String shortName;
     String longName;
     String description;
     boolean hasArgument;
     boolean required;
 
     private Parameter(String shortName, String longName, String description, boolean hasArgument, boolean required) {
         this.shortName = shortName;
         this.longName = longName;
         this.description = description;
         this.hasArgument = hasArgument;
         this.required = required;
     }
 
     public String getShortName() {
         return shortName;
     }
 
     public void setShortName(String shortName) {
         this.shortName = shortName;
     }
 
     public String getLongName() {
         return longName;
     }
 
     public void setLongName(String longName) {
         this.longName = longName;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public boolean isHasArgument() {
         return hasArgument;
     }
 
     public void setHasArgument(boolean hasArgument) {
         this.hasArgument = hasArgument;
     }
 
     public boolean isRequired() {
         return required;
     }
 
     public void setRequired(boolean required) {
         this.required = required;
     }
 
 }
